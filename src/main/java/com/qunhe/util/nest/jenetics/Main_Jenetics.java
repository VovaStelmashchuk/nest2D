package com.qunhe.util.nest.jenetics;


import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dom4j.DocumentException;
import org.w3c.dom.svg.SVGDocument;

import com.qunhe.util.nest.Nest;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.gui.guiUtil;
import com.qunhe.util.nest.util.CommonUtil;
import com.qunhe.util.nest.util.GeometryUtil;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.Factory;
import static io.jenetics.engine.Limits.bySteadyFitness;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;





public class Main_Jenetics {

	
	// 2.) Definition of the fitness function.
    private static int eval_prova(Genotype<BitGene> gt) {
        return gt.chromosome()
            .as(BitChromosome.class)
            .bitCount();
    }
	
	
	public static void main_provs(String[] args) {

		// 1.) Define the genotype (factory) suitable
        //     for the problem.
        Factory<Genotype<BitGene>> gtf =
            Genotype.of(BitChromosome.of(10, 0.5));
 
        // 3.) Create the execution environment.
        Engine<BitGene, Integer> engine = Engine
            .builder(Main_Jenetics::eval_prova, gtf)
            .build();
 
        // 4.) Start the execution (evolution) and
        //     collect the result.
        Genotype<BitGene> result = engine.stream()
            .limit(100)
            .collect(EvolutionResult.toBestGenotype());
 
        System.out.println("Hello World:\n" + result);
		
	}
	
	
	
	public static NestPath generateRandomPositioning() {
		
		NestPath n=null;
		return n;
	}
	
	
	public static void main(String[] args) {
		
		NestPath bin = new NestPath();
		double binWidth = 500;
		double binHeight = 339.235;

		bin.add(0, 0);
		bin.add(binWidth, 0);
		bin.add(binWidth, binHeight);
		bin.add(0, binHeight);
		
		List<NestPath> polygons=null;
		int length=0;
		
		try {
			polygons = guiUtil.transferSvgIntoPolygons();
			 length= polygons.size();

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		List<NestPath> tree = CommonUtil.BuildTree(polygons , Config.CURVE_TOLERANCE);
		
		Config config = new Config();
		config.SPACING = 0;
		config.POPULATION_SIZE = 60;
		config.BIN_HEIGHT=binHeight;
		config.BIN_WIDTH=binWidth;
		
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
        
    
		
		//Factory<Genotype<AnyGene<NestPath>>> gtf = Genotype.of(AnyChromosome.of(Main_Jenetics::generateRandomPositioning,length));
		
        NestPath binPolygon1 = new NestPath();	//Contenitore quadrato
        double width = 400;
        double height = 400;
        binPolygon1.add(0, 0);
        binPolygon1.add(0, height);
        binPolygon1.add(width, height);
        binPolygon1.add(width, 0);
        NestPath outer = new NestPath();	// quadrato
        outer.add(600, 0);
        outer.add(600, 200);
        outer.add(800, 200);
        outer.add(800, 0);
        outer.setRotation(0);
        assert outer.getBid() == 1;
        NestPath inner = new NestPath();	//quadrato
        inner.add(650, 50);
        inner.add(650, 150);
        inner.add(750, 150);
        inner.add(750, 50);
        assert inner.getBid() == 2;
        //inner.setBid(2);
        NestPath little = new NestPath();	//triangolino
        little.add(900, 0);
        little.add(870, 20);
        little.add(930, 20);
        assert little.getBid() == 3;
        //little.setBid(3);
        NestPath rect = new NestPath();	//quadrato
        rect.add(1000, 0);
        rect.add(1000, 100);
        rect.add(1050, 100);
        rect.add(1050, 0);
        assert inner.getBid() == 4;
        NestPath squa = new NestPath();	//quadrato
        squa.add(1100, 0);
        squa.add(1100, 50);
        squa.add(1150, 50);
        squa.add(1250, 0);
        assert inner.getBid() == 5;
        little.setRotation(4);		// rotazione di 360/4 = 90° in senso antiorario
        
        // Inizializzazione in una lista
        List<NestPath> list = new ArrayList<>();
        list.add(inner);
        list.add(outer);
        list.add(little);
        list.add(rect);
        list.add(squa);
        
        // Scelta di fare il nesting all'interno delle figure
        Config config1 = new Config();
        config1.USE_HOLE = true;
        
        
        
        
        
        Fitness_Model fm = new Fitness_Model(list);
        
        Factory<Genotype<DoubleGene>> model = Model_Factory.of(list, 1400,1400);

        Engine<DoubleGene, Double> engine = Engine.builder(fm.getFitness(), model)
                .populationSize(100)
                .optimize(Optimize.MINIMUM)
                .alterers(
                        new Mutator<>(.05),
                        new MeanAlterer<>(.5)
                )
                .build();
        
        
        final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();
        final Phenotype<DoubleGene, Double> best = engine.stream()
                .limit(bySteadyFitness(40))
                .peek(statistics)
                .collect(toBestPhenotype());
        
        ArrayList<NestPath> rooms = (ArrayList<NestPath>) Model_Factory.convert(best.genotype(), list);
       
        SVGDocument docFinals;
        try {
			docFinals = guiUtil.CreateSvgFile(createsvg(rooms,binWidth,binHeight), binWidth, binHeight);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println(statistics);
        System.out.println(best);

		
		
		
		
		
	}
	
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
        	s += "<path d=\"";
        	for (int i = 0; i < nestPath.getSegments().size(); i++) {
        		if (i == 0) {
        			s += "M";
        		} else {
        			s += "L";
        		}
        		Segment segment = nestPath.get(i);
        		s += segment.x + " " + segment.y + " ";
        	}
        	s += "Z\" fill=\"#8498d1\" stroke=\"#010101\" stroke-width=\"1\" />" + " \n";
        	//s += "</g> \n";
        }
        //y += binHeight + 50;
        strings.add(s);
	
	return strings;

        
        
        
        
		}


	
}
