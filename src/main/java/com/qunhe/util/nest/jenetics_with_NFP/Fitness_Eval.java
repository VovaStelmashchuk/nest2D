package com.qunhe.util.nest.jenetics_with_NFP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qunhe.util.nest.config.Config;
import com.qunhe.util.nest.data.*;
import com.qunhe.util.nest.util.GeometryUtil;
import com.qunhe.util.nest.util.NfpUtil;
import com.qunhe.util.nest.util.Placementworker;

import de.lighti.clipper.*;
import de.lighti.clipper.Point.LongPoint;
import io.jenetics.util.ISeq;

/**
 * @author Alberto Gambarara
 *
 */
public class Fitness_Eval {

	ReentrantLock nfpCacheLock;
	Map<String,List<NestPath>> nfpCache;
	Result tmpBestResult;
	ReentrantLock tmpBestResultLock;
	NestPath binPolygon;

	public Fitness_Eval(NestPath bin)
	{
		binPolygon=bin;
		nfpCacheLock = new ReentrantLock(true);
		nfpCache = new HashMap<>();
		tmpBestResultLock = new ReentrantLock(true);
	}

	/**
	 * @param seq_nestpath
	 * @return Fitness of the model with no rotation
	 */
	Double scalar_fitness(final ISeq<NestPath> seq_nestpath) {

		return scalar_fitness(seq_nestpath,null,false);

	}

	/**
	 * @param seq_nestpath
	 * @param rotations array of that contains the step of rotation to use for each polygon 
	 * @param useRot	allow to use rotation or not
	 * @return
	 */
	Double scalar_fitness(final ISeq<NestPath> seq_nestpath, double[] rotations, boolean useRot) {

		if(binPolygon==null)
			throw new NullPointerException("bin is null");


		List<NestPath> paths = seq_nestpath.asList();

		if(useRot) {
			//convert double value to integer value
			final int[] intRotations = new int[rotations.length];
			for (int i=0; i<intRotations.length; ++i)
				intRotations[i] = (int) rotations[i];

			List<Integer> ids = new ArrayList<>();	
			for(int i = 0 ; i < paths.size(); i ++){
				ids.add(paths.get(i).getId());
				NestPath n = paths.get(i);
				if(n.getPossibleRotations()!= null)
					n.setRotation(n.getPossibleRotations()[intRotations[i]]);
			}

		}
		/*--------------------------------------------------------------CREATE NFP CACHE-----------------------------------------------------------------*/
		List<NfpPair> nfpPairs = new ArrayList<>();
		NfpKey key = null;

		//FOR EACH POLYGON CREATE NFP CACHE WITH BIN AND WITH PREVIOUS POLYGONS IN THE LIST
		for(int i = 0 ; i< paths.size();i++){
			NestPath part = paths.get(i);
			key = new NfpKey(binPolygon.getId() , part.getId() , true , 0 , part.getRotation());
			nfpCacheLock.lock();

			if(!nfpCache.containsKey(key))
				nfpPairs.add(new NfpPair(binPolygon, part, key));
			
			nfpCacheLock.unlock();

			for(int j = 0 ; j< i ; j ++){
				NestPath placed = paths.get(j);
				NfpKey keyed = new NfpKey(placed.getId() , part.getId() , false , placed.getRotation(), part.getRotation());
				nfpCacheLock.lock();

				if(!nfpCache.containsKey(keyed))
					nfpPairs.add(new NfpPair(placed, part, keyed));
				
				nfpCacheLock.unlock();
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
			nfpCacheLock.lock();
			nfpCache.put(tkey, Nfp.value);
			nfpCacheLock.unlock();
		}

		//String lotId = InputConfig.INPUT == null ? "" : InputConfig.INPUT.get(0).lotId;
		//IOUtils.saveNfpCache(nfpCache, Config.OUTPUT_DIR + "nfp" + lotId + ".txt");
		/*-------------------------------------------------------------------------------------------------------------------------------*/

		List<NestPath> placeListSlice = new ArrayList<>();

		for (int i = 0; i < paths.size(); i++)
			placeListSlice.add(new NestPath(paths.get(i)));

		paths=placeListSlice;

		if(useRot) {
			//ROTATE ANY NESTPATH
			List<NestPath> rotated = new ArrayList<>();
			for (int i = 0; i < paths.size(); i++) {
				NestPath p = paths.get(i);
				NestPath r = GeometryUtil.rotatePolygon2Polygon(p, p.getRotation());
				r.setRotation(p.getRotation());
				r.setPossibleRotations(p.getPossibleRotations());
				r.setSource(p.getSource());
				r.setId(p.getId());
				r.area=p.area;
				rotated.add(r);
			}
			paths = rotated;
		}
		List<List<Vector>> allplacements = new ArrayList<>();
		// Now the fitness is defined as the width of material used.
		double fitness = 0;
		double binarea = Math.abs(GeometryUtil.polygonArea(binPolygon));

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
				nfpCacheLock.lock();

				if (!nfpCache.containsKey(key1)) {
					nfpCacheLock.unlock();
					continue;
				}
				List<NestPath> binNfp = nfpCache.get(key1);
				nfpCacheLock.unlock();

				// ensure exists
				boolean error = false;
				for (NestPath element : placed) {
					key1 = gson.toJson(new NfpKey(element.getId(), path.getId(), false, element.getRotation(), path.getRotation()));
					nfpCacheLock.lock();

					if (nfpCache.containsKey(key1))
						nfp = nfpCache.get(key1);
					else {
						error = true;
						break;
					}
					nfpCacheLock.unlock();

				}
				if (error) {
					nfpCacheLock.unlock();

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

				for (NestPath binNfpj : binNfp)                	 
					clipperBinNfp.add(Placementworker.scaleUp2ClipperCoordinates(binNfpj));
				
				DefaultClipper clipper = new DefaultClipper();
				Paths combinedNfp = new Paths();

				for (int j = 0; j < placed.size(); j++) {
					key1 = gson.toJson(new NfpKey(placed.get(j).getId(), path.getId(), false, placed.get(j).getRotation(), path.getRotation()));
					nfpCacheLock.lock();					

					nfp = nfpCache.get(key1);
					nfpCacheLock.unlock();

					if (nfp == null)                  	                    	
						continue;

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
				if (!clipper.execute(Clipper.ClipType.UNION, combinedNfp, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO))
					continue;

				//difference with bin polygon
				Paths finalNfp = new Paths();
				clipper = new DefaultClipper();

				clipper.addPaths(combinedNfp, Clipper.PolyType.CLIP, true);
				clipper.addPaths(clipperBinNfp, Clipper.PolyType.SUBJECT, true);
				if (!clipper.execute(Clipper.ClipType.DIFFERENCE, finalNfp, Clipper.PolyFillType.NON_ZERO, Clipper.PolyFillType.NON_ZERO))
					continue;

				finalNfp = finalNfp.cleanPolygons(0.0001 * Config.CLIIPER_SCALE);
				for (int j = 0; j < finalNfp.size(); j++) {
					double area = Math.abs(finalNfp.get(j).area());
					if (finalNfp.get(j).size() < 3 || area < 0.1 * Config.CLIIPER_SCALE * Config.CLIIPER_SCALE) {
						finalNfp.remove(j);
						j--;
					}
				}

				if (finalNfp == null || finalNfp.size() == 0)
					continue;

				List<NestPath> f = new ArrayList<>();
				for (Path element : finalNfp)
					f.add(Placementworker.toNestCoordinates(element));

				List<NestPath> finalNfpf = f;
				double minarea = Double.MIN_VALUE;

				double minX = Double.MAX_VALUE;
				NestPath nf = null;
				double area = Double.MIN_VALUE;
				Vector shifvector = null;
				for (NestPath element : finalNfpf) {
					nf = element;
					if (Math.abs(GeometryUtil.polygonArea(nf)) < 2)	
						continue;
					
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
						for (int m = 0; m < path.size(); m++) 
							allpoints.add(new Segment(path.get(m).x + shifvector.x, path.get(m).y + shifvector.y));
						
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
			if (minwidth != Double.MAX_VALUE)
				fitness += minwidth;				

			for (int i = 0; i < placed.size(); i++) {
				int index = paths.indexOf(placed.get(i));
				if (index >= 0) 
					paths.remove(index);
				
			}

			if (placements != null && placements.size() > 0) {
				// Added a new bin, add 1000 penalty to fitness
				fitness+=1000*(allplacements.size());
				allplacements.add(placements);
			} else 
				break; // something went wrong
			

		}// End of while(paths.size>0)

		Result res = new Result(allplacements, fitness, paths, binarea);
		tmpBestResultLock.lock();
		if (tmpBestResult==null || res.fitness<tmpBestResult.fitness)
			tmpBestResult =res;

		tmpBestResultLock.unlock();
		return fitness;
	}

}