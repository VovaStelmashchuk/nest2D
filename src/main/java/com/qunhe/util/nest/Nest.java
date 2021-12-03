package com.qunhe.util.nest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qunhe.util.nest.algorithm.GeneticAlgorithm;
import com.qunhe.util.nest.algorithm.Individual;
import com.qunhe.util.nest.contest.ContestData;
import com.qunhe.util.nest.data.*;
import com.qunhe.util.nest.data.Vector;
import com.qunhe.util.nest.util.*;
import sun.nio.ch.IOUtil;

import java.util.*;
import static com.qunhe.util.nest.util.IOUtils.*;

/**
 * @author yisa
 */
public class Nest {
    private  NestPath binPath;
    private  List<NestPath> parts;
    private Config config;
    int loopCount ;
    private GeneticAlgorithm GA = null ;
    private Map<String,List<NestPath>> nfpCache;
    private static Gson gson = new GsonBuilder().create();
    private int launchcount =0;

    /**
     *  åˆ›å»ºä¸€ä¸ªæ–°çš„Nestå¯¹è±¡
     * @param binPath   åº•æ�¿å¤šè¾¹å½¢
     * @param parts     æ�¿ä»¶å¤šè¾¹å½¢åˆ—è¡¨
     * @param config    å�‚æ•°è®¾ç½®
     * @param count     è¿­ä»£è®¡ç®—æ¬¡æ•°
     */
    public Nest(NestPath binPath, List<NestPath> parts, Config config, int count) {
        this.binPath = binPath;
        this.parts = parts;
        this.config = config;
        this.loopCount = count;
        nfpCache = new HashMap<String, List<NestPath>>();
    }

    /**
     *  å¼€å§‹è¿›è¡ŒNestè®¡ç®—
     * @return
     */
    public  List<List<Placement>> startNest(){

        List<NestPath> tree = CommonUtil.BuildTree(parts , Config.CURVE_TOLERANCE);

        CommonUtil.offsetTree(tree , 0.5 * config.SPACING);
        binPath.config = config;
        for(NestPath nestPath: parts){
            nestPath.config = config;
        }
        NestPath binPolygon = NestPath.cleanNestPath(binPath);
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
            List<NestPath> safeTree = new ArrayList<NestPath>();
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


        double binPolygonWidth = xbinmax - xbinmin;
        double binPolygonHeight = ybinmax - ybinmin;

        if(GeometryUtil.polygonArea(binPolygon) > 0 ){
            binPolygon.reverse();
        }
        /**
         * ç¡®ä¿�ä¸ºé€†æ—¶é’ˆ TODO why?
         */
        for(int i = 0 ; i< tree.size(); i ++){
            Segment start = tree.get(i).get(0);
            Segment end = tree.get(i).get(tree.get(i).size()-1);
            if(start == end || GeometryUtil.almostEqual(start.x , end.x) && GeometryUtil.almostEqual(start.y , end.y)){
                tree.get(i).pop();
            }
            if(GeometryUtil.polygonArea(tree.get(i)) > 0 ){
                tree.get(i).reverse();
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
                	String lotId = Config.INPUT == null ? "" : Config.INPUT.get(0).lotId;
                    String file = Config.OUTPUT_DIR + lotId + "_" +i +"_" +
                        (int)(rate*10) + ".csv";
                    debug("Save to file "+file);
                    IOUtils.saveToMultiFile(file, appliedPlacement);
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
        for(int j = 0; j < best.placements.size();j++){
            //totalarea += Math.abs(GeometryUtil.polygonArea(binPolygon));
            for(int k = 0 ; k< best.placements.get(j).size() ; k ++){
                sumarea += Math.abs(GeometryUtil.polygonArea(tree.get(best.placements.get(j).get(k).id)));
            }
        }
        return (sumarea/totalarea)*100;
    }
    /**
     *  ä¸€æ¬¡è¿­ä»£è®¡ç®—
     * @param tree  åº•æ�¿
     * @param binPolygon    æ�¿ä»¶åˆ—è¡¨
     * @param config    è®¾ç½®
     * @return
     */
    public Result launchWorkers(List<NestPath> tree ,NestPath binPolygon ,Config config ){
        launchcount++;
        if(Config.IS_DEBUG){
            log("launchWorkers(): launching worker "+launchcount);
        }
        if(GA == null ){
            List<NestPath> adam = new ArrayList<NestPath>();
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
        for(int i = 0 ; i <GA.population.size(); i ++){
            if( GA.population.get(i).getFitness() <  0 ){
                individual = GA.population.get(i);
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

        // ä»¥ä¸Šä¸ºGA. Now we got a set of candidates

        List<NestPath> placelist = individual.getPlacement();
        List<Integer> rotations = individual.getRotation();

        List<Integer> ids = new ArrayList<Integer>();
        for(int i = 0 ; i < placelist.size(); i ++){
            ids.add(placelist.get(i).getId());
            placelist.get(i).setRotation(rotations.get(i));
        }
        if(Config.NFP_CACHE_PATH != null){
            debug("Loading nfp from file "+Config.NFP_CACHE_PATH);
            nfpCache = IOUtils.loadNfpCache(Config.NFP_CACHE_PATH);
        }
        List<NfpPair> nfpPairs = new ArrayList<NfpPair>();
        NfpKey key = null;
        /**
         * å¦‚æžœåœ¨nfpCacheé‡Œæ²¡æ‰¾åˆ°nfpKey åˆ™æ·»åŠ è¿›nfpPairs
         */
        for(int i = 0 ; i< placelist.size();i++){
            NestPath part = placelist.get(i);
            key = new NfpKey(binPolygon .getId() , part.getId() , true , 0 , part.getRotation());
            if(!nfpCache.containsKey(key)) {
                nfpPairs.add(new NfpPair(binPolygon, part, key));
            }
            for(int j = 0 ; j< i ; j ++){
                NestPath placed = placelist.get(j);
                NfpKey keyed = new NfpKey(placed.getId() , part.getId() , false , rotations.get(j), rotations.get(i));
                if(!nfpCache.containsKey(keyed)) {
                    nfpPairs.add(new NfpPair(placed, part, keyed));
                }
            }
        }

        if(Config.IS_DEBUG) {
            log("launchWorkers(): Generating nfp...nb of nfp pairs = "+nfpPairs.size());
        }

        /**
         * ç¬¬ä¸€æ¬¡nfpCacheä¸ºç©º ï¼ŒnfpCacheå­˜çš„æ˜¯nfpKeyæ‰€å¯¹åº”çš„ä¸¤ä¸ªpolygonæ‰€å½¢æˆ�çš„Nfp( List<NestPath> )
         */
        List<ParallelData> generatedNfp = new ArrayList<ParallelData>();
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
        for(int i = 0 ; i<generatedNfp.size() ; i++){
            ParallelData Nfp = generatedNfp.get(i);
            //TODO remove gson & generate a new key algorithm
            String tkey = gson.toJson(Nfp.getKey());
            nfpCache.put(tkey, Nfp.value);
        }
        log("Saving nfpCache.");
        String lotId = Config.INPUT == null ? "" : Config.INPUT.get(0).lotId;
        IOUtils.saveNfpCache(nfpCache, Config.OUTPUT_DIR+"nfp"+ lotId+".txt");

        debug("Launching placement worker...");
        // Here place parts according to the sequence specified by the individual
        Placementworker worker = new Placementworker(binPolygon,config,nfpCache);
        List<NestPath> placeListSlice = new ArrayList<NestPath>();

        for(int i = 0; i< placelist.size() ; i++){
            placeListSlice.add( new NestPath(placelist.get(i)));
        }
        // Some simplification:
        //List<List<NestPath>> data = new ArrayList<List<NestPath>>();
        //data.add(placeListSlice);
        List<Result> results = new ArrayList<Result>();
        //for(int i = 0 ;i <data.size() ; i++){
        Result result = worker.placePaths(placeListSlice);//data.get(i)
        results.add(result);
        //}
        if(results.size() == 0){
            return null;
        }
        individual.fitness = results.get(0).fitness;
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
     *  é€šè¿‡idä¸Žbidå°†translateå’Œrotateç»‘å®šåˆ°å¯¹åº”æ�¿ä»¶ä¸Š
     * @param best
     * @param tree
     * @return
     */
    public static List<List<Placement>> applyPlacement(Result best , List<NestPath> tree){
        List<List<Placement>> applyPlacement = new ArrayList<List<Placement>>();
        for(int i = 0; i<best.placements.size();i++){
            List<Placement> binTranslate = new ArrayList<Placement>();
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
     * åœ¨é�—ä¼ ç®—æ³•ä¸­æ¯�æ¬¡çª�å�˜æˆ–è€…æ˜¯äº¤é…�äº§ç”Ÿå‡ºæ–°çš„ç§�ç¾¤æ—¶ï¼Œå�¯èƒ½ä¼šå‡ºçŽ°æ�¿ä»¶ä¸Žæ—‹è½¬è§’åº¦ä¸�é€‚é…�çš„ç»“æžœï¼Œéœ€è¦�é‡�æ–°æ£€æŸ¥å¹¶é€‚é…�ã€‚
     * @param binPolygon
     * @param tree
     * @return
     */
    private static List<Integer>  checkIfCanBePlaced(NestPath binPolygon , List<NestPath> tree ){
        List<Integer> CanBePlacdPolygonIndex = new ArrayList<Integer>();
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
