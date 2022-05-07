package com.qunhe.util.nest.jenetics_with_NFP;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.dom4j.DocumentException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.*;
import com.qunhe.util.nest.gui.guiUtil;
import com.qunhe.util.nest.util.*;

import de.lighti.clipper.*;
import de.lighti.clipper.Point.LongPoint;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.MeanAlterer;
import io.jenetics.Optimize;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.Phenotype;
import io.jenetics.SwapMutator;
import io.jenetics.engine.*;
import io.jenetics.util.*;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;




public class Rotation_NFP_Nesting implements Problem<Solution,DoubleGene, Double>{

	private final Codec<Solution,DoubleGene> code;
	
	static Phenotype<DoubleGene,Double> tmpBest = null;
	static Result tmpBestResult =null;

	private NestPath _binPolygon;
	Map<String,List<NestPath>> nfpCache=new HashMap<>();

	private final ISeq<NestPath> _list;
	//static ReentrantLock lock = new ReentrantLock();

	public Rotation_NFP_Nesting(ISeq<NestPath> lista,NestPath binpolygon ,double binw, double binh, int n_rot) 
	{
		_binPolygon = binpolygon;
		_list=Objects.requireNonNull(lista);
		
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
	                This for-loop will print the genes in ascending order.
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

            return scalar_fitness(paths, rotations);
        };
    }


	/**
	 * @param seq_nestpath
	 * @return Fitness of the model
	 */
	Double scalar_fitness(final ISeq<NestPath> seq_nestpath, double[] rotations) {

		List<NestPath> paths = seq_nestpath.asList();

		//convert double value to integer value
		final int[] intRotations = new int[rotations.length];
		for (int i=0; i<intRotations.length; ++i)
			intRotations[i] = (int) rotations[i];
		
		
		List<Integer> ids = new ArrayList<>();	
		for(int i = 0 ; i < paths.size(); i ++){
			ids.add(paths.get(i).getId());
			NestPath n = paths.get(i);
			if(n.getPossibleRotations()!= null)
			{
				n.setRotation(n.getPossibleRotations()[intRotations[i]]);
			}
		}

		/*--------------------------------------------------------------CREATE NFP CACHE-----------------------------------------------------------------*/

		List<NfpPair> nfpPairs = new ArrayList<>();
		NfpKey key = null;

		
		//FOR EVERY POLYGON CREATE NFP CACHE WITH BIN AND WITH PREVIOUS POLYGONS IN THE LIST
		for(int i = 0 ; i< paths.size();i++){
			NestPath part = paths.get(i);
			key = new NfpKey(_binPolygon.getId() , part.getId() , true , 0 , part.getRotation());

			if(!nfpCache.containsKey(key)) {
				nfpPairs.add(new NfpPair(_binPolygon, part, key));
			}
			for(int j = 0 ; j< i ; j ++){
				NestPath placed = paths.get(j);
				NfpKey keyed = new NfpKey(placed.getId() , part.getId() , false , placed.getRotation(), part.getRotation());
				if(!nfpCache.containsKey(keyed)) {
					nfpPairs.add(new NfpPair(placed, part, keyed));
				}
			}
		}

		// The first time nfpCache is empty, nfpCache stores Nfp ( List<NestPath> ) formed by two polygons corresponding to nfpKey

		Config config = new Config();
		Gson gson = new GsonBuilder().create();  

		List<ParallelData> generatedNfp = new ArrayList<>();

		for (NfpPair nfpPair : nfpPairs) {

			ParallelData data = NfpUtil.nfpGenerator(nfpPair, config);			
			generatedNfp.add(data);
		}

		for (ParallelData Nfp : generatedNfp) {
			String tkey = gson.toJson(Nfp.getKey());
			try
			{
			nfpCache.put(tkey, Nfp.value);
			}catch (Exception e) {
			System.out.println("errore");
			}
		}

		//String lotId = InputConfig.INPUT == null ? "" : InputConfig.INPUT.get(0).lotId;
		//IOUtils.saveNfpCache(nfpCache, Config.OUTPUT_DIR + "nfp" + lotId + ".txt");
		/*-------------------------------------------------------------------------------------------------------------------------------*/

		List<NestPath> placeListSlice = new ArrayList<>();

		for (int i = 0; i < paths.size(); i++) {
			placeListSlice.add(new NestPath(paths.get(i)));
		}

		paths=placeListSlice;

		//ROTATE ANY NESTPATH
		List<NestPath> rotated = new ArrayList<>();
		for (int i = 0; i < paths.size(); i++) {
			NestPath r = GeometryUtil.rotatePolygon2Polygon(paths.get(i), paths.get(i).getRotation());
			r.setRotation(paths.get(i).getRotation());
			r.setPossibleRotations(paths.get(i).getPossibleRotations());
			r.setSource(paths.get(i).getSource());
			r.setId(paths.get(i).getId());
			r.area=paths.get(i).area;
			rotated.add(r);
		}
		paths = rotated;
		List<List<Vector>> allplacements = new ArrayList<>();
		// Now the fitness is defined as the width of material used.
		double fitness = 0;
		double binarea = Math.abs(GeometryUtil.polygonArea(this._binPolygon));

		String key1 = null;
		List<NestPath> nfp = null;

		// Loops over all the Nestpaths passed to the function
		//If polygons can't fit on one bin, a new one is added, and so on until path.size() is zero
		while (paths.size() > 0) {	
			List<NestPath> placed = new ArrayList<>();		// polygons (NestPath) to place
			List<Vector> placements = new ArrayList<>();

			double minwidth = Double.MAX_VALUE;				//temporary fitness value

			// Loops over all the polygons (paths)
			for (int i = 0; i < paths.size(); i++) {
				NestPath path = paths.get(i);

				//inner NFP	***************************************************************
				key1 = gson.toJson(new NfpKey(-1, path.getId(), true, 0, path.getRotation()));
				if (!nfpCache.containsKey(key1)) {
					continue;
				}
				List<NestPath> binNfp = nfpCache.get(key1);
				// ensure exists
				boolean error = false;
				for (NestPath element : placed) {
					key1 = gson.toJson(new NfpKey(element.getId(), path.getId(), false, element.getRotation(), path.getRotation()));
					if (nfpCache.containsKey(key1)) nfp = nfpCache.get(key1);
					else {
						error = true;
						break;
					}
				}
				if (error) {
					continue;
				}//***************************************************************

				Vector position = null;
				if (placed.size() == 0) {
					// first placement , put it on the top-left
					for (NestPath element : binNfp) {
						for (int k = 0; k < element.size(); k++) {
							if (position == null || element.get(k).x - path.get(0).x < position.x) {
								position = new Vector(
										element.get(k).x - path.get(0).x,
										element.get(k).y - path.get(0).y,
										path.getId(),
										path.getRotation()
										);
							}
						}
					}
					placements.add(position);
					placed.add(path);
					continue;
				}
				Paths clipperBinNfp = new Paths();

				for (NestPath binNfpj : binNfp) {                	 
					clipperBinNfp.add(Placementworker.scaleUp2ClipperCoordinates(binNfpj));
				}
				DefaultClipper clipper = new DefaultClipper();
				Paths combinedNfp = new Paths();

				for (int j = 0; j < placed.size(); j++) {
					key1 = gson.toJson(new NfpKey(placed.get(j).getId(), path.getId(), false, placed.get(j).getRotation(), path.getRotation()));
					nfp = nfpCache.get(key1);

					if (nfp == null) {                    	                    	
						continue;
					}

					for (NestPath element : nfp) {
						Path clone = Placementworker.scaleUp2ClipperCoordinates(element);
						for (LongPoint element2 : clone) {
							long clx = element2.getX();
							long cly = element2.getY();
							element2.setX(clx + (long) (placements.get(j).x * Config.CLIIPER_SCALE));
							element2.setY(cly + (long) (placements.get(j).y * Config.CLIIPER_SCALE));
						}
						// TODO why clean again here
						clone = clone.cleanPolygon(0.0001 * Config.CLIIPER_SCALE);
						double area = Math.abs(clone.area());
						if (clone.size() > 2 && area > 0.1 * Config.CLIIPER_SCALE * Config.CLIIPER_SCALE) {
							clipper.addPath(clone, Clipper.PolyType.SUBJECT, true);
						}
					}
				}
				if (!clipper.execute(Clipper.ClipType.UNION, combinedNfp, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO)) {
					continue;
				}

				//difference with bin polygon
				Paths finalNfp = new Paths();
				clipper = new DefaultClipper();

				clipper.addPaths(combinedNfp, Clipper.PolyType.CLIP, true);
				clipper.addPaths(clipperBinNfp, Clipper.PolyType.SUBJECT, true);
				if (!clipper.execute(Clipper.ClipType.DIFFERENCE, finalNfp, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO)) {
					continue;
				}

				finalNfp = finalNfp.cleanPolygons(0.0001 * Config.CLIIPER_SCALE);
				for (int j = 0; j < finalNfp.size(); j++) {
					double area = Math.abs(finalNfp.get(j).area());
					if (finalNfp.get(j).size() < 3 || area < 0.1 * Config.CLIIPER_SCALE * Config.CLIIPER_SCALE) {
						finalNfp.remove(j);
						j--;
					}
				}
				
//				Iterator<Path> iter = finalNfp.iterator();
//				while (iter.hasNext()) {
//					Path p = iter.next();
//					double area = Math.abs(p.area());
//					if (p.size() < 3 || area < 0.1 * Config.CLIIPER_SCALE * Config.CLIIPER_SCALE) {
//						iter.remove();
//					}
//				}

				if (finalNfp == null || finalNfp.size() == 0) {
					continue;
				}

				List<NestPath> f = new ArrayList<>();
				for (Path element : finalNfp) {
					f.add(Placementworker.toNestCoordinates(element));
				}

				List<NestPath> finalNfpf = f;
				double minarea = Double.MIN_VALUE;
				double minX = Double.MAX_VALUE;
				NestPath nf = null;
				double area = Double.MIN_VALUE;
				Vector shifvector = null;
				for (NestPath element : finalNfpf) {
					nf = element;
					if (Math.abs(GeometryUtil.polygonArea(nf)) < 2) {	
						continue;
					}
					for (int k = 0; k < nf.size(); k++) {
						NestPath allpoints = new NestPath();
						for (int m = 0; m < placed.size(); m++) {
							for (int n = 0; n < placed.get(m).size(); n++) {
								allpoints.add(new Segment(placed.get(m).get(n).x + placements.get(m).x,
										placed.get(m).get(n).y + placements.get(m).y));
							}
						}
						shifvector = new Vector(
								nf.get(k).x - path.get(0).x,
								nf.get(k).y - path.get(0).y,
								path.getId(),
								path.getRotation(),
								combinedNfp
								);
						for (int m = 0; m < path.size(); m++) {
							allpoints.add(new Segment(path.get(m).x + shifvector.x, path.get(m).y + shifvector.y));
						}
						Bound rectBounds = GeometryUtil.getPolygonBounds(allpoints);

						area = rectBounds.getWidth() * 2 + rectBounds.getHeight();
						if (minarea == Double.MIN_VALUE
								|| area < minarea
								|| (GeometryUtil.almostEqual(minarea, area)
										&& (minX == Double.MIN_VALUE || shifvector.x < minX))) {
							minarea = area;
							minwidth = rectBounds.getWidth();
							position = shifvector;
							minX = shifvector.x;
						}
					}
				}
				if (position != null) {

					placed.add(path);				// polygon placed
					placements.add(position);		
				}
			}
			if (minwidth != Double.MAX_VALUE) {
				fitness += minwidth;				
			}

			for (int i = 0; i < placed.size(); i++) {
				int index = paths.indexOf(placed.get(i));
				if (index >= 0) {
					paths.remove(index);
				}
			}

			if (placements != null && placements.size() > 0) {
				// Added a new bin, add 1000 penalty to fitness
				fitness+=1000*(allplacements.size());
				allplacements.add(placements);
			} else {
				break; // something went wrong
			}

		}// End of while(paths.size>0)

		Result res = new Result(allplacements, fitness, paths, binarea);
		if (tmpBestResult==null || res.fitness<tmpBestResult.fitness)
		{
			tmpBestResult =res;
		}
		return fitness;

	}


	public static void main(String[] args) {

		double binWidth = 400;
		double binHeight = 400;

		NestPath bin = Util.createRectPolygon(binWidth, binHeight);
		List<NestPath> polygons=null;

		try {
			polygons = guiUtil.transferSvgIntoPolygons();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		   
		Config config = new Config();
		config.SPACING = 0;
		config.POPULATION_SIZE = 15;
		Config.BIN_HEIGHT=binHeight;
		Config.BIN_WIDTH=binWidth;
		Config.LIMIT=15;
		Config.NUMBER_OF_ROTATIONS=12;
		Config.MAX_SEC_DURATION=polygons.size()*10;
		Config.MAX_STEADY_FITNESS=15;
		Config.N_THREAD=10;

		List<NestPath> tree = CommonUtil.BuildTree(polygons , Config.CURVE_TOLERANCE);
		CommonUtil.offsetTree(tree, 0.5 * config.SPACING);    

		bin.config = config;
		for(NestPath nestPath: polygons){
			nestPath.config = config;
		}

		NestPath binPolygon=Util.CleanBin(bin);


		// A part may become not positionable after a rotation. TODO this can also be removed if we know that all parts are legal
		if(!Config.ASSUME_ALL_PARTS_PLACABLE) {
			List<Integer> integers = Nest.checkIfCanBePlaced(binPolygon, tree);
			List<NestPath> safeTree = new ArrayList<>();
			for (Integer i : integers) {
				safeTree.add(tree.get(i));
			}
			if(integers.size()<tree.size()) System.out.println(tree.size() - integers.size() +  "polygons can't be placed");
			tree = safeTree;
		}

		Util.cleanTree(tree);    

		    for(NestPath np:tree)
		    {		
		    	np.setPossibleNumberRotations(Config.NUMBER_OF_ROTATIONS);
		    }
		    
		ExecutorService executor = Executors.newFixedThreadPool(Config.N_THREAD);
		final ISeq<NestPath> paths = ISeq.<NestPath>of(tree);
		//NFP_Nesting nst = NFP_Nesting.of(tree,binPolygon,binWidth,binHeight);
		Rotation_NFP_Nesting nst = new Rotation_NFP_Nesting(paths, binPolygon, binWidth, binHeight, Config.NUMBER_OF_ROTATIONS);
		Engine<DoubleGene,Double> engine = Engine
				.builder(nst)
				.optimize(Optimize.MINIMUM)
				.populationSize(config.POPULATION_SIZE)
				.executor(executor)
				.alterers(
						new MeanAlterer<>(),
						new SwapMutator<>())
						
				.build();


		final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber(); 

		System.out.println("Starting nesting with rotation");
		
		Phenotype<DoubleGene,Double> best=
				engine.stream()
				.limit(Limits.bySteadyFitness(Config.MAX_STEADY_FITNESS))
				.limit(Limits.byExecutionTime(Duration.ofSeconds(Config.MAX_SEC_DURATION)))
				//.limit(Config.LIMIT)
				.peek(Rotation_NFP_Nesting::update)
				.peek(statistics)
				.collect(toBestPhenotype());

		System.out.println(statistics);
		//System.out.println(best);

		List<List<Placement>>appliedPlacement=Nest.applyPlacement(tmpBestResult, tree);
		try {
			List<String> strings = SvgUtil.svgGenerator(tree, appliedPlacement, binWidth, binHeight);
			guiUtil.saveSvgFile(strings, Config.OUTPUT_DIR+Config.OUTPUT_FILENAME,binWidth,binHeight);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Best Solution saved at " + Config.OUTPUT_DIR+"res.html");
		}
		executor.shutdownNow();
	}

	/**
	 * Function to be executed at every generation
	 * If the result is the best until now show a message
	 * @param result result of evaluation
	 */
	private static void update(final EvolutionResult<DoubleGene,Double> result)
	{
		System.out.println(result.generation() + " generation: ");
		if(tmpBest == null || tmpBest.compareTo(result.bestPhenotype())>0)
		{			
			tmpBest =result.bestPhenotype();			
			System.out.println("Found better fitness: " + tmpBest.fitness());
		}
		else
		{
			System.out.println("Better fitness is still: " + result.bestFitness());
		}
	}




}
