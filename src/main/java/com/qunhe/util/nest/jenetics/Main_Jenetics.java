package com.qunhe.util.nest.jenetics;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.dom4j.DocumentException;
import org.w3c.dom.svg.SVGDocument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.contest.InputConfig;
import com.qunhe.util.nest.data.*;
import com.qunhe.util.nest.gui.guiUtil;
import com.qunhe.util.nest.util.*;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.Factory;
import static io.jenetics.engine.Limits.bySteadyFitness;
import static com.qunhe.util.nest.util.IOUtils.debug;
import static com.qunhe.util.nest.util.IOUtils.log;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;


public class Main_Jenetics {

	public static Phenotype<DoubleGene, Double> tmpBest = null;

	
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
			e.printStackTrace();
		}
        final int MAX_SEC_DURATION=polygons.size()*5;
		
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
        	np.setPossibleNumberRotations(4);
        }
          
		
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        Fitness_Model fm = new Fitness_Model(tree,binWidth,binHeight);

        Factory<Genotype<DoubleGene>> model = Model_Factory.of(tree, binWidth,binHeight);

        final Constraint<DoubleGene, Double> constraint= new RepairingConstraint(tree);
        
        Engine<DoubleGene, Double> engine = Engine.builder(fm.getFitness(), model)
                .populationSize(config.POPULATION_SIZE)
                .optimize(Optimize.MINIMUM)
                //.offspringFraction(0.75)
                .alterers(
                        new Mutator<>(.25),
                        new MeanAlterer<>(.05),
                        new SwapMutator<>(0.25),
                        new UniformCrossover<>(0.05),
                        new MultiPointCrossover<>(0.05)
                        //partial alterer
                )
                .executor(executor)
                .constraint(constraint)
                .build();
        
        
        
        final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();   
        
        
        System.out.println("Starting nesting of " + polygons.size() + " polygons in " + binWidth +  " * "  + binHeight + " bin");
        System.out.println("population size: " + config.POPULATION_SIZE + " - Max duration: " + MAX_SEC_DURATION + "s");
        
        
        final Phenotype<DoubleGene, Double> best = engine.stream()
                .limit(bySteadyFitness(50))
                .limit(Limits.byExecutionTime(Duration.ofSeconds(MAX_SEC_DURATION)))
                .peek(Main_Jenetics::update)
                .peek(statistics)
                .collect(toBestPhenotype());

        
        ArrayList<NestPath> polys = CommonUtil.cloneArrayListNestpath(tree);
        List<List<Placement>> appliedplacement =  Model_Factory.convert(best.genotype(), tree);
               
        //compress(tree);
        List<String> res;

		try {
			res=createsvg(tree, binWidth, binHeight);
			guiUtil.saveSvgFile(res, Config.OUTPUT_DIR+"res.html",binWidth, binHeight);

		} catch (Exception e) {
			e.printStackTrace();
		}
  
        System.out.println(statistics);
        System.out.println(best);

		
	}
	
//	private static void compress(List<NestPath> list)
//	{
//		 for(int i=0; i<list.size();i++)
//	        {
//	        	NestPath pi = list.get(i);
//	        	pi.ZeroX();
//	        	for(int j=0;j<list.size();j++)
//	        	{
//	        		NestPath pj = list.get(j);
//	        		if(i!=j)
//	        		{
//	        			while(GeometryUtil.intersect(pi, pj))        				
//	        				{pi.translate(1, 0);}	        				
//	        		}	        			
//	        	}
//	        }
//	        for(int i=0; i<list.size();i++)
//	        {
//	        	NestPath pi = list.get(i);
//	        	pi.ZeroY();
//	        	for(int j=0;j<list.size();j++)
//	        	{
//	        		NestPath pj = list.get(j);
//	        		if(i!=j)
//	        		{
//	        			while(GeometryUtil.intersect(pi, pj))        				
//	        				{pi.translate(0, 1);}        				
//	        		}	        			
//	        	}
//	        }
//	}
	
	/**
	 * Function to be executed at every generation
	 * If the result is the best until now show a message
	 * @param result result of evaluation
	 */
	private static void update(final EvolutionResult<DoubleGene, Double> result)
    {
		if(tmpBest == null || tmpBest.compareTo(result.bestPhenotype())>0)
		{			
			tmpBest =result.bestPhenotype();
			System.out.println(result.generation() + " generation: ");
			System.out.println("Found better fitness: " + tmpBest.fitness());
		}    	
    }
	
	/**
	 * @param list		List of NestPaths nested
	 * @param binwidth	Width of bin
	 * @param binheight	height of bin
	 * @return
	 */
	public static List<String> createsvg(List<NestPath> list, double binwidth, double binheight)
	{
		
        List<String> strings = new ArrayList<>();
        String s = "    <rect x=\"0\" y=\"0\" width=\"" + binwidth + "\" height=\"" + binheight + "\"  fill=\"none\" stroke=\"#010101\" stroke-width=\"1\" />\n";
        
        for(int j=0; j<list.size(); j++)
        {
        	NestPath nestPath = list.get(j);
//        	double ox = placement.translate.x;
//        	double oy = placement.translate.y;
//        	double rotate = placement.rotate;
        	//s += "<g transform=\"translate(" + ox + x + " " + oy + y + ") rotate(" + rotate + ")\"> \n";
        	s += "<path id=\"" + nestPath.getBid() + "\" d=\"";
        	for (int i = 0; i < nestPath.getSegments().size(); i++) {
        		if (i == 0) {
        			s += "M";
        		} else {
        			s += "L";
        		}
        		Segment segment = nestPath.get(i);
        		s += segment.x + " " + segment.y + " ";
        	}
        	s += "Z\" fill=\"#8498d1\" stroke=\"#010101\" stroke-width=\0.5\" />" + " \n";
        	//s += "</g> \n";
        }
        //y += binHeight + 50;
        strings.add(s);
	
	return strings;

        
        
        
        
		}


	
}
