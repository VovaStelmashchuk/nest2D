package com.qunhe.util.nest.jenetics_with_NFP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.dom4j.DocumentException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.contest.InputConfig;
import com.qunhe.util.nest.data.*;
import com.qunhe.util.nest.gui.guiUtil;
import com.qunhe.util.nest.jenetics.Main_Jenetics;
import com.qunhe.util.nest.util.*;

import de.lighti.clipper.Clipper;
import de.lighti.clipper.DefaultClipper;
import de.lighti.clipper.Path;
import de.lighti.clipper.Paths;
import de.lighti.clipper.Point.LongPoint;
import io.jenetics.DoubleGene;
import io.jenetics.EnumGene;
import io.jenetics.Optimize;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.Phenotype;
import io.jenetics.SwapMutator;
import io.jenetics.engine.*;
import io.jenetics.util.*;

import static com.qunhe.util.nest.util.IOUtils.debug;
import static com.qunhe.util.nest.util.IOUtils.log;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;


public class NFP_Nesting implements Problem<ISeq<NestPath>, EnumGene<NestPath>, Double>{

	static Phenotype<EnumGene<NestPath>,Double> tmpBest = null;
	private Double _binWidth,_binHeight;
	private NestPath _binPolygon;
    private Map<String, List<NestPath>> _nfpCache;

	
	
	private final ISeq<NestPath> _list;
	//private final List<NestPath> _list_asList;
	
	public NFP_Nesting(ISeq<NestPath> lista, double binw, double binh, Map<String, List<NestPath>> nfpCache) 
	{
		//_list_asList = lista.asList();
		_binWidth= binw;
		_binHeight=binh;
		_binPolygon = new NestPath();
		_nfpCache = nfpCache;
		_binPolygon.add(0, 0);
		_binPolygon.add(_binWidth, 0);
		_binPolygon.add(_binWidth, _binHeight);
		_binPolygon.add(0, _binHeight);
		_list=Objects.requireNonNull(lista);
		
	}
	
	

	@Override
	public Function<ISeq<NestPath>, Double> fitness() {//Calcolo fitness fittizzio

		
		return this::scalar_fitness;
		
//		return p -> IntStream.range(0, p.length())
//				.mapToDouble(i -> {
//					double penalty=0;
//					if((i+1)<p.length() && p.get(i).getBid() >p.get(i+1).getBid()) penalty++;
//						return penalty;}).sum();
//						
		
	}

	
	double scalar_fitness(final ISeq<NestPath> seq_nestpath) {
	    Gson gson = new GsonBuilder().create();

		double penalty=0;
		List<NestPath> paths = seq_nestpath.asList();
		List<NestPath> rotated = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            NestPath r = GeometryUtil.rotatePolygon2Polygon(paths.get(i), paths.get(i).getRotation());
            r.setRotation(paths.get(i).getRotation());
            r.setPossibleRotations(paths.get(i).getPossibleRotations());
            r.setSource(paths.get(i).getSource());
            r.setId(paths.get(i).getId());
            rotated.add(r);
        }
        paths = rotated;
        List<List<Vector>> allplacements = new ArrayList<>();
        // Now the fitness is defined as the width of material used.
        double fitness = 0;
        double binarea = Math.abs(GeometryUtil.polygonArea(this._binPolygon));
        
        String key = null;
        List<NestPath> nfp = null;
        
     // Loops over all the Nestpaths passed to the function
        while (paths.size() > 0) {
        	 List<NestPath> placed = new ArrayList<>();		// polygons (NestPath) to place
             List<Vector> placements = new ArrayList<>();
             
             double minwidth = Double.MAX_VALUE;				// valore che verrà assegnato alla fitness
             
             // Loops over all the polygons (paths)
             for (int i = 0; i < paths.size(); i++) {
                 NestPath path = paths.get(i);
                 
               //inner NFP	***************************************************************
                 key = gson.toJson(new NfpKey(-1, path.getId(), true, 0, path.getRotation()));
                 if (!_nfpCache.containsKey(key)) {
                     continue;
                 }
                 List<NestPath> binNfp = _nfpCache.get(key);
                 // ensure exists
                 boolean error = false;
                 for (NestPath element : placed) {
                     key = gson.toJson(new NfpKey(element.getId(), path.getId(), false, element.getRotation(), path.getRotation()));
                     if (_nfpCache.containsKey(key)) nfp = _nfpCache.get(key);
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
                     // first placement , put it on the left
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
                     key = gson.toJson(new NfpKey(placed.get(j).getId(), path.getId(), false, placed.get(j).getRotation(), path.getRotation()));
                     nfp = _nfpCache.get(key);
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

                     placed.add(path);				// polygon added
                     placements.add(position);
                 }
             }
             if (minwidth != Double.MAX_VALUE) {
                 fitness += minwidth ;/// binarea;
             	//fitness = minwidth;
             }
 	
             for (int i = 0; i < placed.size(); i++) {
                 int index = paths.indexOf(placed.get(i));
                 if (index >= 0) {
                     paths.remove(index);
                 }
             }

             

             if (placements != null && placements.size() > 0) {
                 // Add a new material
                 allplacements.add(placements);
             } else {
                 break; // something went wrong
             }

         }// End of while(paths.size>0)
        
        Result res = new Result(allplacements, fitness, paths, binarea);
		return fitness;
		
	}
	
	
	



	@Override
	public Codec<ISeq<NestPath>, EnumGene<NestPath>> codec() {
		return Codecs.ofPermutation(_list);
	}

	
	private static NFP_Nesting of (List<NestPath> l, double binw, double binh, Map<String, List<NestPath>>nfpCache)
	{
		final MSeq<NestPath> paths = MSeq.ofLength(l.size());
		
		final var random = RandomRegistry.random();
		
		for ( int i = 0 ; i < l.size(); ++i ) {
			
			paths.set(i, l.get(i));
		}
		
		
		for(int j=paths.length()-1; j>0;--j)
		{
			final int i = random.nextInt(j+1);
			final NestPath tmp=paths.get(i);
			paths.set(i, paths.get(j));
			paths.set(j, tmp);
		}
		
		return new NFP_Nesting(paths.toISeq(),binw,binh,nfpCache);
		
	}
	
	private static void update(final EvolutionResult<EnumGene<NestPath>,Double> result)
    {
		if(tmpBest == null || tmpBest.compareTo(result.bestPhenotype())>0)
		{			
			tmpBest =result.bestPhenotype();
			System.out.println(result.generation() + " generation: ");
			System.out.println("Found better fitness: " + tmpBest.fitness());
			System.out.println( "-".repeat((int)Math.round(tmpBest.fitness()*10)));			
		}    	
    }

	
public static void main(String[] args) {

	NestPath bin = new NestPath();
	double binWidth = 400;
	double binHeight = 400;

	bin.add(0, 0);
	bin.add(binWidth, 0);
	bin.add(binWidth, binHeight);
	bin.add(0, binHeight);
	
	List<NestPath> polygons=null;

	try {
		polygons = guiUtil.transferSvgIntoPolygons();
	} catch (DocumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    final int MAX_SEC_DURATION=polygons.size()*10;

	
	Config config = new Config();
	config.SPACING = 0;
	config.POPULATION_SIZE = polygons.size()*20;
	Config.BIN_HEIGHT=binHeight;
	Config.BIN_WIDTH=binWidth;
	
	List<NestPath> tree = CommonUtil.BuildTree(polygons , Config.CURVE_TOLERANCE);
    CommonUtil.offsetTree(tree, 0.5 * config.SPACING);
    
	
	NestPath binPolygon = CommonUtil.cleanNestPath(bin);	//conversione di un eventuale binPath self intersecting in un poligono semplice
    // Bound binBound = GeometryUtil.getPolygonBounds(binPolygon);
    if(Config.BOUND_SPACING > 0 ){
        List<NestPath> offsetBin = CommonUtil.polygonOffset(binPolygon , - Config.BOUND_SPACING);
        if(offsetBin.size() == 1 ){
            binPolygon = offsetBin.get(0);
        }
    }
    binPolygon.setId(-1);
    // A part may become not positionable after a rotation. TODO this can also be removed if we know that all parts are legal
    if(!Config.ASSUME_ALL_PARTS_PLACABLE) {
        List<Integer> integers = Nest.checkIfCanBePlaced(binPolygon, tree);
        List<NestPath> safeTree = new ArrayList<>();
        for (Integer i : integers) {
            safeTree.add(tree.get(i));
        }
        tree = safeTree;
    }
    
    
    
    /*VENGONO SETTATE LE COORDINATE MAX E MIN DEL SINGOLO BINPATH PER POI POTERLO TRASLARE NELL'ORIGINE*/
    double xbinmax = binPolygon.get(0).x;	// get.(0) = prende il primo segmento dei 4 (coordinate del primo vertice), se si assume che la superficie sia rettangolare
    double xbinmin = binPolygon.get(0).x;
    double ybinmax = binPolygon.get(0).y;
    double ybinmin = binPolygon.get(0).y;
    // Find min max
    for(int i = 1 ; i<binPolygon.size(); i++){
        if(binPolygon.get(i).x > xbinmax ){
            xbinmax = binPolygon.get(i).x;
        }
        else if (binPolygon.get(i).x < xbinmin ){
            xbinmin = binPolygon.get(i) .x;
        }

        if(binPolygon.get(i).y > ybinmax ){
            ybinmax = binPolygon.get(i).y;
        }
        else if (binPolygon.get(i). y <ybinmin ){
            ybinmin = binPolygon.get(i).y;
        }
    }

    /*VIENE TRASLATO IL POLIGONO BINPATH NELL'ORIGINE*/
    for(int i=0; i<binPolygon.size(); i++){				
        binPolygon.get(i).x -= xbinmin;
        binPolygon.get(i).y -= ybinmin;
    }
    
    if(GeometryUtil.polygonArea(binPolygon) > 0 ){
        binPolygon.reverse();
    }
    
    for (NestPath element : tree) {
        Segment start = element.get(0);
        Segment end = element.get(element.size()-1);
        if(start == end || GeometryUtil.almostEqual(start.x , end.x) && GeometryUtil.almostEqual(start.y , end.y)){
            element.pop();
        }
        if(GeometryUtil.polygonArea(element) > 0 ){
            element.reverse();
        }
    }
	
    for(NestPath np:tree)
    {

    	np.Zerolize();
    	np.translate((binWidth - np.getMaxX())/2, (binHeight - np.getMaxY())/2);
    	
    	//rectangles are already set with 4 possible rotation
    	np.setPossibleNumberRotations(4);
    }
    
    
    /*--------------------------------------------------------------CREATE NFP CACHE-----------------------------------------------------------------*/
    Map<String,List<NestPath>> nfpCache=new HashMap<>();
    
    if(Config.NFP_CACHE_PATH != null){
        debug("Loading nfp from file "+Config.NFP_CACHE_PATH);
        nfpCache = IOUtils.loadNfpCache(Config.NFP_CACHE_PATH);
    }
    
    List<NfpPair> nfpPairs = new ArrayList<>();
    NfpKey key = null;
    
    for(int i = 0 ; i< tree.size();i++){
        NestPath part = tree.get(i);
        key = new NfpKey(binPolygon.getId() , part.getId() , true , 0 , part.getRotation());
        
        if(!nfpCache.containsKey(key)) {
            nfpPairs.add(new NfpPair(binPolygon, part, key));
        }
        for(int j = 0 ; j< i ; j ++){
            NestPath placed = tree.get(j);
            NfpKey keyed = new NfpKey(placed.getId() , part.getId() , false , placed.getRotation(), part.getRotation());
            if(!nfpCache.containsKey(keyed)) {
                nfpPairs.add(new NfpPair(placed, part, keyed));
            }
        }
    }
    
    
    if (Config.IS_DEBUG) {
		log("launchWorkers(): Generating nfp...nb of nfp pairs = " + nfpPairs.size());
	}
	// The first time nfpCache is empty, nfpCache stores Nfp ( List<NestPath> ) formed by two polygons corresponding to nfpKey

    
    
//    NfpKey nfpKey = new NfpKey(A.getId(),B.getId(),false,0 ,0 );
//    NfpPair nfpPair = new NfpPair(A,B, nfpKey);
//    ParallelData parallelData = NfpUtil.nfpGenerator(nfpPair,config);
//    
    Gson gson = new GsonBuilder().create();        
	List<ParallelData> generatedNfp = new ArrayList<>();

    for (NfpPair nfpPair : nfpPairs) {
		
		ParallelData data = NfpUtil.nfpGenerator(nfpPair, config);			
		generatedNfp.add(data);
	}
    
	for (ParallelData Nfp : generatedNfp) {
		String tkey = gson.toJson(Nfp.getKey());
		nfpCache.put(tkey, Nfp.value);
	}

	log("Saving nfpCache.");
	String lotId = InputConfig.INPUT == null ? "" : InputConfig.INPUT.get(0).lotId;
	IOUtils.saveNfpCache(nfpCache, Config.OUTPUT_DIR + "nfp" + lotId + ".txt");
	
	
    
    
    NFP_Nesting nst = NFP_Nesting.of(tree,binWidth,binHeight,nfpCache);
    Engine<EnumGene<NestPath>,Double> engine = Engine
    		.builder(nst)
    		.optimize(Optimize.MINIMUM)
    		.populationSize(500)
    		.alterers(
    				new SwapMutator<>(0.2),
    				new PartiallyMatchedCrossover<>(0.35)
    				)
    		.build();
    
    final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();   

    Phenotype<EnumGene<NestPath>,Double> best=
    		engine.stream()
    		.limit(Limits.bySteadyFitness(50))
    		.limit(250)
            .peek(NFP_Nesting::update)
    		.peek(statistics)
    		.collect(toBestPhenotype());
		
    System.out.println(statistics);
    System.out.println(best);
	}
	
	
	
	
}
