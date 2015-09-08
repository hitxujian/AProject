package xusheng.util.algorithm;

//Use simulate annealing to aggregate ranks

import fig.basic.LogInfo;
import kangqi.util.struct.MapDoubleHelper;

import java.util.*;

public class SimulateAnnealing {

	public static boolean verbose = false;
	
	//curRank & ranks: showing the rank for the i-th element
	public static double getTauDist(List<Integer> curRank, List<List<Integer>> ranks, List<Double> weights) {
		double tau = 0;
		int sz = curRank.size();
		for (int r = 0; r < ranks.size(); r++) {
			double w = weights.get(r);
			List<Integer> rank = ranks.get(r);
			for (int i = 0; i < sz; i++) {
				if (rank.get(i) == -1) continue;
				for (int j = i + 1; j < sz; j++) { 
					if (rank.get(j) == -1) continue;
					if ((curRank.get(i) - curRank.get(j)) * (rank.get(i) - rank.get(j)) < 0) {	//disagree
						tau += w;
					}
				}
			}
		}
		return tau;
	}
	
	public static int[] randomPair(Random rdm, int sz) {		//random a pair <x, y>, x != y
		int tot = sz * sz;
		int[] ret = null;
		while (true) {
			int r = rdm.nextInt(tot);
			int x = r / sz, y = r % sz;
			if (x != y) {
				ret = new int[] {x, y};
				break;
			}
		}
		return ret;
	}
	
	public static boolean inRange(int idx, int small, int large) { return idx >= small && idx <= large; }
	
	
	public static LinkedList<Integer> slide(LinkedList<Integer> curRank, int a, int b) {
		LinkedList<Integer> ret = new LinkedList<>(curRank);
		int ar = ret.remove(a);
		ret.add(b, ar);
		return ret;
	} 
	
	public static double deltaDistSlide(List<Integer> curRank, List<List<Integer>> ranks, List<Double> weights, int a, int b) {
		int idxMin = Math.min(a, b), idxMax = Math.max(a, b), sz = curRank.size();
		double tau = 0;
		for (int r = 0; r < ranks.size(); r++) {
			double w = weights.get(r);
			List<Integer> rank = ranks.get(r);
			for (int i = 0; i < sz; i++) {
				if (rank.get(i) < 0) continue;
				for (int j = i + 1; j < sz; j++) {
					if (rank.get(j) < 0) continue;
					if (!inRange(i, idxMin, idxMax) && !inRange(j, idxMin, idxMax)) continue;
					if ((curRank.get(i) - curRank.get(j)) * (rank.get(i) - rank.get(j)) < 0)	//disagree
						tau += w;
				}
			}
		}
		return tau;
	}
	
	
	public static List<Integer> annealing(List<List<Integer>> ranks, List<Double> weights, 
			LinkedList<Integer> initialRank, double tempBegin, double tempEnd, double coolingFactor, int totIter) {
		Random rdm = new Random(System.currentTimeMillis());
		int sz = initialRank.size();
		
		LinkedList<Integer> bestRank = new LinkedList<>(initialRank);
		double bestTau = getTauDist(bestRank, ranks, weights);
		if (verbose) LogInfo.logs("Initial, Rank: %s, Disagree: %.6f", bestRank.toString(), bestTau);
		
		
		for (int iter = 0; iter < totIter; iter++) {
			double temp = tempBegin;
			LinkedList<Integer> curRank = new LinkedList<>(bestRank);		//each iteration starts from best Rank
			double curTau = bestTau;
			
			while (temp > tempEnd) {
				int[] index = randomPair(rdm, sz);
				LinkedList<Integer> newRank = slide(curRank, index[0], index[1]);
				int outIntervalSize = sz - (Math.abs(index[1] - index[0]) + 1);		//the size of non-affected elements
				double newTau = 0;
				if (outIntervalSize * (outIntervalSize - 1) > sz * (sz - 1) / 2) {
					double tauBefore = deltaDistSlide(curRank, ranks, weights, index[0], index[1]);
					double tauAfter = deltaDistSlide(newRank, ranks, weights, index[0], index[1]);
					newTau = curTau + tauAfter - tauBefore;
				} else 
					newTau = getTauDist(newRank, ranks, weights);
				
				double diff = newTau - curTau;
				if (diff < 0 || Math.exp(-diff / temp) > rdm.nextDouble()) {
					curRank = newRank;
					curTau = newTau;
				}
				if (curTau < bestTau) {
					bestRank = new LinkedList<>(curRank);
					bestTau = curTau;
				}
				temp *= coolingFactor;
			}
			if (verbose) LogInfo.logs("Iteration %d, Rank: %s, Disagree: %.6f", iter, bestRank.toString(), bestTau);
		}
		return bestRank;
	}

	
	public static List<String> rankingAggreg(List<List<String>> lists, List<Double> weights) {
		//Item --> Hash
		HashMap<String, Integer> hash = new HashMap<>();
		ArrayList<String> uhash = new ArrayList<>();
		for (List<String> list : lists) {
			for (String elem : list) {
				if (!hash.containsKey(elem)) {
					hash.put(elem, uhash.size());
					uhash.add(elem);
				}
			}
		}
		int sz = uhash.size();
		
		//Now Generating Ranking List
		ArrayList<List<Integer>> ranks = new ArrayList<>();
		for (List<String> list : lists) {
			Integer[] rList = new Integer[sz];
			for (int i = 0; i < sz; i++) rList[i] = -1;
			for (int i = 0; i < list.size(); i++)
				rList[hash.get(list.get(i))] = i;
			List<Integer> rank = Arrays.asList(rList);
			if (verbose) LogInfo.logs("Partial Rank: %s", rank.toString());
			ranks.add(rank);
		}
		
		//Set Initial Rank
		HashMap<Integer, Double> scoreMap = new HashMap<Integer, Double>();
		for (int r = 0; r < ranks.size(); r++) {
			List<Integer> rank = ranks.get(r);
			double w = weights.get(r);
			for (int i = 0; i < sz; i++) {
				int rk = rank.get(i);
				if (rk >= 0)
					new MapDoubleHelper<Integer>().addToMapDouble(scoreMap, i, w * 1.0 / (rk + 1));
			}
		}
		ArrayList<Map.Entry<Integer, Double>> srtList = new MapDoubleHelper<Integer>().sort(scoreMap);
		Integer[] initList = new Integer[sz];
		for (int i = 0; i < srtList.size(); i++) 
			initList[srtList.get(i).getKey()] = i;
		LinkedList<Integer> initialRank = new LinkedList<>(Arrays.asList(initList));
		
		//Simulate Annealing
		double tempBegin = 300, tempEnd = 0.01, coolingFactor = 0.9;
		int totIter = 30;
		List<Integer> finalRank = annealing(ranks, weights, initialRank, tempBegin, tempEnd, coolingFactor, totIter);
		
		//Return
		String[] retList = new String[sz];
		for (int i = 0; i < sz; i++) {
			int rank = finalRank.get(i);
			retList[rank] = uhash.get(i);
		}
		return Arrays.asList(retList);
	}
	
	public static void main(String[] args) throws Exception {
		String[] listArr1 = new String[] {"h", "d", "j", "i", "a", "e", "c", "b", "f", "g"};
		String[] listArr2 = new String[] {"d", "j", "h", "b", "i", "c", "a", "f", "e"};
		String[] listArr3 = new String[] {"h", "d", "j", "a", "i", "b", "e", "c"};
		ArrayList<List<String>> lists = new ArrayList<>();
		lists.add(Arrays.asList(listArr1));
		lists.add(Arrays.asList(listArr2));
		lists.add(Arrays.asList(listArr3));
		Double[] weightArr = new Double[] {1.0, 1.0, 1.0};
		List<Double> weights = Arrays.asList(weightArr);
		
		List<String> retList = rankingAggreg(lists, weights);
		System.out.println(retList.toString());
	}
}
