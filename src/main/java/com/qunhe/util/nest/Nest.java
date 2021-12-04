package com.qunhe.util.nest;

import static com.qunhe.util.nest.util.IOUtils.debug;
import static com.qunhe.util.nest.util.IOUtils.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qunhe.util.nest.algorithm.GeneticAlgorithm;
import com.qunhe.util.nest.algorithm.Individual;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.contest.InputConfig;
import com.qunhe.util.nest.data.Bound;
import com.qunhe.util.nest.data.NestPath;
import com.qunhe.util.nest.data.NfpKey;
import com.qunhe.util.nest.data.NfpPair;
import com.qunhe.util.nest.data.ParallelData;
import com.qunhe.util.nest.data.Placement;
import com.qunhe.util.nest.data.Result;
import com.qunhe.util.nest.data.Segment;
import com.qunhe.util.nest.data.Vector;
import com.qunhe.util.nest.util.CommonUtil;
import com.qunhe.util.nest.util.GeometryUtil;
import com.qunhe.util.nest.util.IOUtils;
import com.qunhe.util.nest.util.NfpUtil;
import com.qunhe.util.nest.util.Placementworker;

/**
 * @author yisa
 */
public class Nest {
    private  NestPath binPath;
    private  List<NestPath> parts;
    private Config config;
    private int loopCount ;
    private GeneticAlgorithm GA = null ;
    private Map<String,List<NestPath>> nfpCache;
    private static Gson gson = new GsonBuilder().create();
    private int launchcount =0;

    /**
     *  Ã¥Ë†â€ºÃ¥Â»ÂºÃ¤Â¸â‚¬Ã¤Â¸ÂªÃ¦â€“Â°Ã§Å¡â€žNestÃ¥Â¯Â¹Ã¨Â±Â¡
     * @param binPath   Ã¥Âºâ€¢Ã¦ï¿½Â¿Ã¥Â¤Å¡Ã¨Â¾Â¹Ã¥Â½Â¢
     * @param parts     Ã¦ï¿½Â¿Ã¤Â»Â¶Ã¥Â¤Å¡Ã¨Â¾Â¹Ã¥Â½Â¢Ã¥Ë†â€”Ã¨Â¡Â¨
     * @param config    Ã¥ï¿½â€šÃ¦â€¢Â°Ã¨Â®Â¾Ã§Â½Â®
     * @param count     Ã¨Â¿Â­Ã¤Â»Â£Ã¨Â®Â¡Ã§Â®â€”Ã¦Â¬Â¡Ã¦â€¢Â°
     */
    public Nest(NestPath binPath, List<NestPath> parts, Config config, int count) {
        this.binPath = binPath;
        this.parts = parts;
        this.config = config;
        this.loopCount = count;
        nfpCache = new HashMap<>();
    }

    /**
     *  Ã¥Â¼â‚¬Ã¥Â§â€¹Ã¨Â¿â€ºÃ¨Â¡Å’NestÃ¨Â®Â¡Ã§Â®â€”
     * @return
     */
    public  List<List<Placement>> startNest(){

        List<NestPath> tree = CommonUtil.BuildTree(parts , Config.CURVE_TOLERANCE);

        CommonUtil.offsetTree(tree , 0.5 * config.SPACING);
        binPath.config = config;
        for(NestPath nestPath: parts){
            nestPath.config = config;
        }
        NestPath binPolygon = CommonUtil.cleanNestPath(binPath);
        // Bound binBound = GeometryUtil.getPolygonBounds(binPolygon);
        if(Config.BOUND_SPACING > 0 ){
            List<NestPath> offsetBin = CommonUtil.polygonOffset(binPolygon , - Config.BOUND_SPACING);
            if(offsetBin.size() == 1 ){
                binPolygon = offsetBin.get(0);
            }
        }
        binPolygon.setId(-1);
        // A part may become unplacable after a rotation. TODO this can also be removed if we know that all parts are legal
        if(!Config.ASSUME_ALL_PARTS_PLACABLE) {
            List<Integer> integers = checkIfCanBePlaced(binPolygon, tree);
            List<NestPath> safeTree = new ArrayList<>();
            for (Integer i : integers) {
                safeTree.add(tree.get(i));
            }
            tree = safeTree;
        }
        double xbinmax = binPolygon.get(0).x;
        double xbinmin = binPolygon.get(0).x;
        double ybinmax = binPolygon.get(0).y;
        double ybinmin = binPolygon.get(0).y;
        // Find min max
        for(int i = 1 ; i<binPolygon.size(); i ++){
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
        for(int i=0; i<binPolygon.size(); i++){
            binPolygon.get(i).x -= xbinmin;
            binPolygon.get(i).y -= ybinmin;
        }


        //double binPolygonWidth = xbinmax - xbinmin;
        //double binPolygonHeight = ybinmax - ybinmin;

        if(GeometryUtil.polygonArea(binPolygon) > 0 ){
            binPolygon.reverse();
        }
        /**
         * Ã§Â¡Â®Ã¤Â¿ï¿½Ã¤Â¸ÂºÃ©â‚¬â€ Ã¦â€”Â¶Ã©â€™Ë† TODO why?
         */
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

        launchcount = 0;
        Result best = null;
        List<List<Placement>> appliedPlacement = null;
        for(int i = 0; i<loopCount;i++ ){
            Result result = launchWorkers(tree , binPolygon , config);
            if(i == 0 ||  best.fitness > result.fitness){
                best = result;

                double rate = computeUseRate(best, tree);
                log("Loop "+i+" width = "+best.fitness+"; use rate = " + rate);
                appliedPlacement = applyPlacement(best,tree);
                try {
                	String lotId = InputConfig.INPUT == null ? "" : InputConfig.INPUT.get(0).lotId;
                    String file = Config.OUTPUT_DIR + lotId + "_" +i +"_" +
                        (int)(rate*10) + ".csv";
                    debug("Save to file "+file);
                    IOUtils.saveToMultiFile(file, appliedPlacement, InputConfig.INPUT_POLY);
                }catch (Exception e){
                    log(e);
                }
            }
        }
        //appliedPlacement = applyPlacement(best,tree);
        return appliedPlacement;
    }

    
    public double computeUseRate(Result best, List<NestPath> tree){
        //Log new result
        double sumarea = 0;
        double totalarea = Config.BIN_HEIGHT*best.fitness;
        for (List<Vector> element : best.placements) {
            //totalarea += Math.abs(GeometryUtil.polygonArea(binPolygon));
            for (Vector element2 : element) {
                sumarea += Math.abs(GeometryUtil.polygonArea(tree.get(element2.id)));
            }
        }
        return (sumarea/totalarea)*100;
    }
    /**
     *  Ã¤Â¸â‚¬Ã¦Â¬Â¡Ã¨Â¿Â­Ã¤Â»Â£Ã¨Â®Â¡Ã§Â®â€”
     * @param tree  Ã¥Âºâ€¢Ã¦ï¿½Â¿
     * @param binPolygon    Ã¦ï¿½Â¿Ã¤Â»Â¶Ã¥Ë†â€”Ã¨Â¡Â¨
     * @param config    Ã¨Â®Â¾Ã§Â½Â®
     * @return
     */
    
    public Result launchWorkers(List<NestPath> tree ,NestPath binPolygon ,Config config ){
        launchcount++;
        if(Config.IS_DEBUG){
            log("launchWorkers(): launching worker "+launchcount);
        }
        if(GA == null ){
            List<NestPath> adam = new ArrayList<>();
            for(NestPath nestPath : tree ){
                NestPath clone  = new NestPath(nestPath);
                adam.add(clone);
            }
            for(NestPath nestPath: adam){
                nestPath.area = GeometryUtil.polygonArea(nestPath);
            }
            Collections.sort(adam);
            GA = new GeneticAlgorithm(adam , binPolygon  , config);
        }

        Individual individual = null;
        for (Individual element : GA.population) {
            if( element.getFitness() <  0 ){
                individual = element;
                break;
            }
        }
//        if(individual == null ){
//            GA.generation();
//            individual = GA.population.get(1);
//        }
        if(launchcount > 1 && individual == null ){
            GA.generation();
            individual = GA.population.get(1);
        }
        if(Config.IS_DEBUG) {
            log("launchWorkers(): GA: individual ready.");
        }

        // Ã¤Â»Â¥Ã¤Â¸Å Ã¤Â¸ÂºGA. Now we got a set of candidates

        List<NestPath> placelist = individual.getPlacement();
        List<Integer> rotations = individual.getRotation();

        List<Integer> ids = new ArrayList<>();
        for(int i = 0 ; i < placelist.size(); i ++){
            ids.add(placelist.get(i).getId());
            placelist.get(i).setRotation(rotations.get(i));
        }
        if(Config.NFP_CACHE_PATH != null){
            debug("Loading nfp from file "+Config.NFP_CACHE_PATH);
            nfpCache = IOUtils.loadNfpCache(Config.NFP_CACHE_PATH);
        }
        List<NfpPair> nfpPairs = new ArrayList<>();
        NfpKey key = null;
        /**
         * Ã¥Â¦â€šÃ¦Å¾Å“Ã¥Å“Â¨nfpCacheÃ©â€¡Å’Ã¦Â²Â¡Ã¦â€°Â¾Ã¥Ë†Â°nfpKey Ã¥Ë†â„¢Ã¦Â·Â»Ã¥Å Â Ã¨Â¿â€ºnfpPairs
         */
        for(int i = 0 ; i< placelist.size();i++){
            NestPath part = placelist.get(i);
            key = new NfpKey(binPolygon .getId() , part.getId() , true , 0 , part.getRotation());
            // ATTENZIONE sarà sempre false
            if(!nfpCache.containsKey(key)) {
                nfpPairs.add(new NfpPair(binPolygon, part, key));
            }
            for(int j = 0 ; j< i ; j ++){
                NestPath placed = placelist.get(j);
                NfpKey keyed = new NfpKey(placed.getId() , part.getId() , false , rotations.get(j), rotations.get(i));
                // ATTENZIONE sarà sempre false
                if(!nfpCache.containsKey(keyed)) {
                    nfpPairs.add(new NfpPair(placed, part, keyed));
                }
            }
        }

        if(Config.IS_DEBUG) {
            log("launchWorkers(): Generating nfp...nb of nfp pairs = "+nfpPairs.size());
        }

        /**
         * Ã§Â¬Â¬Ã¤Â¸â‚¬Ã¦Â¬Â¡nfpCacheÃ¤Â¸ÂºÃ§Â©Âº Ã¯Â¼Å’nfpCacheÃ¥Â­ËœÃ§Å¡â€žÃ¦ËœÂ¯nfpKeyÃ¦â€°â‚¬Ã¥Â¯Â¹Ã¥Âºâ€�Ã§Å¡â€žÃ¤Â¸Â¤Ã¤Â¸ÂªpolygonÃ¦â€°â‚¬Ã¥Â½Â¢Ã¦Ë†ï¿½Ã§Å¡â€žNfp( List<NestPath> )
         */
        List<ParallelData> generatedNfp = new ArrayList<>();
        int cnt = 0;
        for(NfpPair nfpPair :nfpPairs){
            if(++cnt % 1000 == 0 ){
                //debug(" nfp generated.");
                debug("Generating nfp "+cnt+": "+nfpPair.getA().bid+","+nfpPair.getB().bid);
            }
            ParallelData data = NfpUtil.nfpGenerator(nfpPair,config);
            if(data == null){
                debug("Null nfp "+cnt+": "+nfpPair.getA().bid+","+nfpPair.getB().bid);
            }
            generatedNfp.add(data);
        }
        for (ParallelData Nfp : generatedNfp) {
            //TODO remove gson & generate a new key algorithm
            String tkey = gson.toJson(Nfp.getKey());
            nfpCache.put(tkey, Nfp.value);
        }
        log("Saving nfpCache.");
        String lotId = InputConfig.INPUT == null ? "" : InputConfig.INPUT.get(0).lotId;
        IOUtils.saveNfpCache(nfpCache, Config.OUTPUT_DIR+"nfp"+ lotId+".txt");

        debug("Launching placement worker...");
        // Here place parts according to the sequence specified by the individual
        Placementworker worker = new Placementworker(binPolygon,config,nfpCache);
        List<NestPath> placeListSlice = new ArrayList<>();

        for(int i = 0; i< placelist.size() ; i++){
            placeListSlice.add( new NestPath(placelist.get(i)));
        }
        // Some simplification:
        //List<List<NestPath>> data = new ArrayList<List<NestPath>>();
        //data.add(placeListSlice);
        List<Result> results = new ArrayList<>();
        //for(int i = 0 ;i <data.size() ; i++){
        Result result = worker.placePaths(placeListSlice);//data.get(i)
        results.add(result);
        //}
        if(results.size() == 0){
            return null;
        }
        individual.setFitness(results.get(0).fitness);
        Result bestResult = results.get(0);
        for(int i = 1; i <results.size() ; i++) {
            if (results.get(i).fitness < bestResult.fitness) {
                bestResult = results.get(i);
            }
        }
        debug("launchWorkers(): current best fitness = "+bestResult.fitness);
        return bestResult;
    }

    /**
     *  Ã©â‚¬Å¡Ã¨Â¿â€¡idÃ¤Â¸Å½bidÃ¥Â°â€ translateÃ¥â€™Å’rotateÃ§Â»â€˜Ã¥Â®Å¡Ã¥Ë†Â°Ã¥Â¯Â¹Ã¥Âºâ€�Ã¦ï¿½Â¿Ã¤Â»Â¶Ã¤Â¸Å 
     * @param best
     * @param tree
     * @return
     */
    
    public static List<List<Placement>> applyPlacement(Result best , List<NestPath> tree){
        List<List<Placement>> applyPlacement = new ArrayList<>();
        for(int i = 0; i<best.placements.size();i++){
            List<Placement> binTranslate = new ArrayList<>();
            for(int j = 0 ; j <best.placements.get(i).size(); j ++){
                Vector v = best.placements.get(i).get(j);
                NestPath nestPath = tree.get(v.id);
                for(NestPath child : nestPath.getChildren()){
                    Placement chPlacement = new Placement(child.bid , new Segment(v.x,v.y) , v.rotation);
                    binTranslate.add(chPlacement);
                }
                Placement placement = new Placement(nestPath.bid , new Segment(v.x,v.y) , v.rotation);
                binTranslate.add(placement);
            }
            applyPlacement.add(binTranslate);
        }
        return applyPlacement;
    }


    /**
     * Ã¥Å“Â¨Ã©ï¿½â€”Ã¤Â¼Â Ã§Â®â€”Ã¦Â³â€¢Ã¤Â¸Â­Ã¦Â¯ï¿½Ã¦Â¬Â¡Ã§Âªï¿½Ã¥ï¿½ËœÃ¦Ë†â€“Ã¨â‚¬â€¦Ã¦ËœÂ¯Ã¤ÂºÂ¤Ã©â€¦ï¿½Ã¤ÂºÂ§Ã§â€�Å¸Ã¥â€¡ÂºÃ¦â€“Â°Ã§Å¡â€žÃ§Â§ï¿½Ã§Â¾Â¤Ã¦â€”Â¶Ã¯Â¼Å’Ã¥ï¿½Â¯Ã¨Æ’Â½Ã¤Â¼Å¡Ã¥â€¡ÂºÃ§Å½Â°Ã¦ï¿½Â¿Ã¤Â»Â¶Ã¤Â¸Å½Ã¦â€”â€¹Ã¨Â½Â¬Ã¨Â§â€™Ã¥ÂºÂ¦Ã¤Â¸ï¿½Ã©â‚¬â€šÃ©â€¦ï¿½Ã§Å¡â€žÃ§Â»â€œÃ¦Å¾Å“Ã¯Â¼Å’Ã©Å“â‚¬Ã¨Â¦ï¿½Ã©â€¡ï¿½Ã¦â€“Â°Ã¦Â£â‚¬Ã¦Å¸Â¥Ã¥Â¹Â¶Ã©â‚¬â€šÃ©â€¦ï¿½Ã£â‚¬â€š
     * @param binPolygon
     * @param tree
     * @return
     */
    private static List<Integer>  checkIfCanBePlaced(NestPath binPolygon , List<NestPath> tree ){
        List<Integer> CanBePlacdPolygonIndex = new ArrayList<>();
        Bound binBound = GeometryUtil.getPolygonBounds(binPolygon);
        for(int i = 0; i <tree.size() ; i++ ){
            NestPath nestPath = tree.get(i);
            if(nestPath.getRotation() == 0 ){
                Bound bound = GeometryUtil.getPolygonBounds(nestPath);
                if(bound.width < binBound.width && bound.height < binBound.height ){
                    CanBePlacdPolygonIndex.add(i);
                    continue;
                }
            }
            else{
                for(int j = 0 ; j<nestPath.getRotation() ; j ++){
                    Bound rotatedBound = GeometryUtil.rotatePolygon(nestPath , (360/nestPath.getRotation()) * j );
                    if(rotatedBound.width < binBound.width && rotatedBound.height < binBound.height ){
                        CanBePlacdPolygonIndex.add(i);
                        break;
                    }
                }
            }
        }
        return CanBePlacdPolygonIndex;
    }



    public  void add(NestPath np ){
        parts.add(np);
    }

    public  NestPath getBinPath() {
        return binPath;
    }

    public  List<NestPath> getParts() {
        return parts;
    }

    public void setBinPath(NestPath binPath) {
        this.binPath = binPath;
    }

    public void setParts(List<NestPath> parts) {
        this.parts = parts;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

}
