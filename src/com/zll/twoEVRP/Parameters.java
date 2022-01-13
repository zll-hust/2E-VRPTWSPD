package com.zll.twoEVRP;

import java.util.Random;

public class Parameters {

    private String inputFileName;
    private int tabuHorizon;
    private int tabuIterations;
    private int iterationNoImprove;
    private double rate;
    private int seed;
    private Random ran;

    public Parameters(String fileName, int seed) {
        inputFileName = fileName;
        tabuHorizon = 30;
        tabuIterations = 50;
//        rate = 0.7;
//        seed = 3;
//        ran = new Random(seed);
        ran = new Random();
    }

    public String toString() {
        StringBuffer print = new StringBuffer();
        print.append("\n" + "--- Parameters: -------------------------------------");
        print.append("\n" + "------------------------------------------------------");
        return print.toString();
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public int getTabuHorizon() {
        return tabuHorizon;
    }

    public void setTabuHorizon(int tabuHorizon) {
        this.tabuHorizon = tabuHorizon;
    }

    public int getTabuIterations() {
        return tabuIterations;
    }

    public void setTabuIterations(int tabuIterations) {
        this.tabuIterations = tabuIterations;
    }

    public int getIterationNoImprove() {
        return this.iterationNoImprove;
    }

    public void setIterationNoImprove(int iteration) {
        this.iterationNoImprove = iteration;
    }

    public Random getRandom() {
        return this.ran;
    }

    public void setRandomSeed(int seed) {
        this.seed = seed;
        ran = new Random(seed);
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
