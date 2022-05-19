package com.qunhe.util.nest.jenetics_with_NFP;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.dom4j.DocumentException;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.*;
import com.qunhe.util.nest.gui.guiUtil;
import com.qunhe.util.nest.util.*;

import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.MeanAlterer;
import io.jenetics.Optimize;
import io.jenetics.Phenotype;
import io.jenetics.SwapMutator;
import io.jenetics.engine.*;
import io.jenetics.util.*;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;

//Adapted from Franz Wilhelmstötter, Jenetics' Project owner - stackoverflow.com/questions/72127848/custom-genotype-in-jenetics-for-nesting

public class Rotation_NFP_Nesting implements Problem<Solution,DoubleGene, Double>{

	private final Codec<Solution,DoubleGene> code;
	Fitness_Eval evaluator;

	public Rotation_NFP_Nesting(ISeq<NestPath> lista,NestPath binpolygon ,double binw, double binh, int n_rot) 
	{
		evaluator = new Fitness_Eval(binpolygon);

		code = Codec.of(
				Genotype.of(
						// Encoding the order of the `NestPath` as double chromosome.
						// The order is given by the sorted gene values.
						DoubleChromosome.of(DoubleRange.of(0, 1), lista.length()),
						// Encoding the rotation of each `NestPath`.
						DoubleChromosome.of(DoubleRange.of(0, n_rot), lista.length())
						),
				gt -> {
					/*
	                The `order` array contains the sorted indexes.
	                This for-loop would print the genes in ascending order.
	                for (var index : order) {
	                    System.out.println(gt.get(0).get(index));
	                }
					 */
					final int[] order = ProxySorter.sort(gt.get(0));

					// Uses the `order` indexes to "permute" path elements.
					final ISeq<NestPath> pseq = IntStream.of(order)
							.mapToObj(lista::get)
							.collect(ISeq.toISeq());

					// The second chromosome just contains rotation values.
					final double[] rotations = gt.get(1)
							.as(DoubleChromosome.class)
							.toArray();

					return new Solution(pseq, rotations);
				}
				);
	}

	@Override
	public Codec<Solution, DoubleGene> codec() {
		return code;
	}

	@Override
	public Function<Solution, Double> fitness() {
		return solution -> {
			final ISeq<NestPath> paths = solution.paths;
			final double[] rotations = solution.rotations;
			return evaluator.scalar_fitness(paths,rotations,true);
		};
	}

	public static void main(String[] args) {

		double binWidth = 420;
		double binHeight = 420;

		NestPath bin = Util.createRectPolygon(binWidth, binHeight);
		List<NestPath> polygons=null;

		try {
			polygons = guiUtil.transferSvgIntoPolygons();
		} catch (DocumentException e) {
			e.printStackTrace();
			return;
		}

		Config config = new Config();
		config.SPACING = 0;
		config.POPULATION_SIZE = 10;
		config.BIN_HEIGHT=binHeight;
		config.BIN_WIDTH=binWidth;
		config.LIMIT=Integer.MAX_VALUE;
		config.NUMBER_OF_ROTATIONS=4;
		config.MAX_SEC_DURATION=polygons.size()*1;
		config.MAX_STEADY_FITNESS=25;
		config.N_THREAD=10;

		List<NestPath> tree = CommonUtil.BuildTree(polygons , Config.CURVE_TOLERANCE);
		CommonUtil.offsetTree(tree, 0.5 * config.SPACING);    

		bin.config = config;
		for(NestPath nestPath: polygons)
			nestPath.config = config;

		NestPath binPolygon=Util.CleanBin(bin);


		// A part may become not positionable after a rotation. TODO this can also be removed if we know that all parts are legal
		if(!Config.ASSUME_ALL_PARTS_PLACABLE) {
			List<Integer> integers = Nest.checkIfCanBePlaced(binPolygon, tree);
			List<NestPath> safeTree = new ArrayList<>();
			for (Integer i : integers)
				safeTree.add(tree.get(i));
			
			if(integers.size()<tree.size()) System.out.println(tree.size() - integers.size() +  "polygons can't be placed");
			tree = safeTree;
		}

		Util.cleanTree(tree);    

		for(NestPath np:tree)		
			np.setPossibleNumberRotations(config.NUMBER_OF_ROTATIONS);

		ExecutorService executor = Executors.newFixedThreadPool(config.N_THREAD);
		final ISeq<NestPath> paths = ISeq.<NestPath>of(tree);

		Rotation_NFP_Nesting nst = new Rotation_NFP_Nesting(paths, binPolygon, binWidth, binHeight, config.NUMBER_OF_ROTATIONS);
		Engine<DoubleGene,Double> engine = Engine
				.builder(nst)
				.optimize(Optimize.MINIMUM)
				.populationSize(config.POPULATION_SIZE)
				.executor(executor)
				.alterers(
						new MeanAlterer<>(0.35),
						new SwapMutator<>(0.35))

				.build();


		final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber(); 
		final Updater<DoubleGene> up = new Updater<>();

		System.out.println("Starting nesting with " + config.NUMBER_OF_ROTATIONS  +  " rotation steps of " + polygons.size() + " polygons on " + binWidth + " * " + binHeight +  " bin with Population: " + config.POPULATION_SIZE + " and threads: " + config.N_THREAD);

		Phenotype<DoubleGene,Double> best=
				engine.stream()
				.limit(Limits.bySteadyFitness(config.MAX_STEADY_FITNESS))
				.limit(Limits.byExecutionTime(Duration.ofSeconds(config.MAX_SEC_DURATION)))
				.limit(Config.LIMIT)
				.peek(up::update)
				.peek(statistics)
				.collect(toBestPhenotype());

		System.out.println(statistics);
		//System.out.println(best);
		executor.shutdownNow();

		List<List<Placement>>appliedPlacement=Nest.applyPlacement(nst.evaluator.tmpBestResult, tree);
		try {
			List<String> strings = SvgUtil.svgGenerator(tree, appliedPlacement, binWidth, binHeight);
			guiUtil.saveSvgFile(strings, Config.OUTPUT_DIR+config.OUTPUT_FILENAME,binWidth,binHeight);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Best Solution saved at " + Config.OUTPUT_DIR+config.OUTPUT_FILENAME);
		}
	}
}