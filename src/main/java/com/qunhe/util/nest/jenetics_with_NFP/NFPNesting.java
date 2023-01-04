package com.qunhe.util.nest.jenetics_with_NFP;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

import org.dom4j.DocumentException;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.*;
import com.qunhe.util.nest.gui.guiUtil;
import com.qunhe.util.nest.util.*;

import io.jenetics.EnumGene;
import io.jenetics.Optimize;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.Phenotype;
import io.jenetics.SwapMutator;
import io.jenetics.engine.*;
import io.jenetics.util.*;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;

//Adapted from Franz Wilhelmstötter, Jenetics' Project owner - JENETICS LIBRARY USER’S MANUAL 7.0 - jenetics.io/manual/manual-7.0.0.pdf

public class NFPNesting implements Problem<ISeq<NestPath>, EnumGene<NestPath>, Double>{

	FitnessEval evaluator;
	private final ISeq<NestPath> _list;


	public NFPNesting(ISeq<NestPath> lista,NestPath binPolygon) 
	{
		_list=Objects.requireNonNull(lista);
		evaluator = new FitnessEval(binPolygon);
	}

	@Override
	public Codec<ISeq<NestPath>, EnumGene<NestPath>> codec() {
		return Codecs.ofPermutation(_list);
	}

	@Override
	public Function<ISeq<NestPath>, Double> fitness() {
		return evaluator::scalarFitness;
	}

	private static NFPNesting of (List<NestPath> l, NestPath binPol)
	{
		final MSeq<NestPath> paths = MSeq.ofLength(l.size());
		
		for ( int i = 0 ; i < l.size(); ++i ) 		
			paths.set(i, l.get(i));
		return new NFPNesting(paths.toISeq(),binPol);
	}


	public static void main(String[] args) {

		double binWidth = 200;
		double binHeight = 200;
		NestPath bin = Util.createRectPolygon(binWidth, binHeight);
		List<NestPath> polygons=null;

		try {
			polygons = guiUtil.transferSvgIntoPolygons();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		Config config = new Config();
		config.SPACING = 0;
		config.POPULATION_SIZE = 10;
		Config.BIN_HEIGHT=binHeight;
		Config.BIN_WIDTH=binWidth;
		Config.LIMIT=Integer.MAX_VALUE;
		config.MAX_SEC_DURATION=polygons.size()*1;
		config.MAX_STEADY_FITNESS=30;
		config.N_THREAD=5;

		List<NestPath> tree = CommonUtil.BuildTree(polygons , Config.CURVE_TOLERANCE);
		CommonUtil.offsetTree(tree, 0.5 * config.SPACING);    

		bin.config = config;
		for(NestPath nestPath: polygons)
			nestPath.config = config;
		

		NestPath binPolygon=Util.cleanBin(bin);
		Util.cleanTree(tree);    

		ExecutorService executor = Executors.newFixedThreadPool(config.N_THREAD);

		NFPNesting nst = NFPNesting.of(tree,binPolygon);
		Engine<EnumGene<NestPath>,Double> engine = Engine
				.builder(nst)
				.optimize(Optimize.MINIMUM)
				.populationSize(config.POPULATION_SIZE)
				.executor(executor)
				.alterers(
						new SwapMutator<>(0.2),
						new PartiallyMatchedCrossover<>(0.35)
						)
				.build();

		final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber(); 
		final Updater<EnumGene<NestPath>,Double> up = new Updater<>();

		System.out.println("Starting nesting without rotation of " + polygons.size() + " polygons on " + binWidth + " * " + binHeight +  " bin with Population: " + config.POPULATION_SIZE + " and threads: " + config.N_THREAD);

		Phenotype<EnumGene<NestPath>,Double> best=
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
