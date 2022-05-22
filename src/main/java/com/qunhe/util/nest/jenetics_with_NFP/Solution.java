package com.qunhe.util.nest.jenetics_with_NFP;

import com.qunhe.util.nest.data.NestPath;

import io.jenetics.util.ISeq;

public class Solution {

	ISeq<NestPath> paths;
	double[] rotations;

	public Solution(ISeq<NestPath> paths, double[] rotations) {

		this.paths=paths;
		this.rotations=rotations;
		
	}

}
