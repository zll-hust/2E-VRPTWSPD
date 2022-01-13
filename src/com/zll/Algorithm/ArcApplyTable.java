package com.zll.Algorithm;

import java.util.Arrays;

public class ArcApplyTable {

    public int[][] arcApply;

    public ArcApplyTable(int customerNr) {
        this.arcApply = new int[customerNr][customerNr];
        for (int i = 0; i < arcApply.length; i++) {
            Arrays.fill(this.arcApply[i], 0);
        }
    }

}
