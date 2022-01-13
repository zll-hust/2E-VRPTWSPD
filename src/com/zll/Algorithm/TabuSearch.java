package com.zll.Algorithm;

import com.zll.twoEVRP.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class TabuSearch {

    //    private Random r;
    private int tabuHorizon;
    private double[][] distanceMatrix;
    private int[][][][] tabuMatrix; // 四维数组，每一个satellite有dummy satellite
    private Instance instance;
    private double rate; // 采样概率

    public TabuSearch(Parameters para, Instance instance, int[] dsNr) {
//        this.r = para.getRandom();
        this.rate = para.getRate();
        this.tabuHorizon = para.getTabuHorizon();
        this.tabuMatrix = new int[instance.getTotalNodeNr()][][][];
        for (int i = 0; i < this.tabuMatrix.length; i++) {
            this.tabuMatrix[i] = new int[dsNr[i]][][];
            for (int j = 0; j < this.tabuMatrix[i].length; j++) {
                this.tabuMatrix[i][j] = new int[instance.getTotalNodeNr()][];
                for (int k = 0; k < this.tabuMatrix[i][j].length; k++) {
                    this.tabuMatrix[i][j][k] = new int[dsNr[k]];
                    Arrays.fill(tabuMatrix[i][j][k], -1);
                }
            }
        }
        this.distanceMatrix = instance.getDistanceMatrix();
        this.instance = instance;
    }


    public MyRelocateMove2E findBestRelocationMove2E(Solution solution, int iteration, Solution bestSolution) throws IOException {

        MyRelocateMove2E bestMove = null;
        double bestMoveVal = Double.POSITIVE_INFINITY;

        for (int i = 0; i < solution.getDepot().getDeliveryVehicles().size(); i++) {
            BigVehicle bv = solution.getDepot().getDeliveryVehicles().get(i);
            for (int j = 0; j < bv.getDummySatellites().size(); j++) {
                DummySatellites ds = bv.getDummySatellites().get(j);
                SmallVehicle deleteRoute = ds.getVehicle();

                for (int n = 0; n < solution.getDepot().getDeliveryVehicles().size(); n++) {
                    BigVehicle bv2 = solution.getDepot().getDeliveryVehicles().get(n);

                    for (int m = 0; m < bv2.getDummySatellites().size(); m++) {

                        if (i == n)
                            continue;

                        for (int k = 0; k < deleteRoute.getCustomers().size(); k++) {

                            DummySatellites ds2 = bv2.getDummySatellites().get(m);
                            SmallVehicle insertRoute = ds2.getVehicle();

                            MyRelocateMove2E newMove;
                            double newObjVal;
                            boolean newMoveTabu;

                            // evaluate each position of the route to find the best insertion of the getCustomer(); start from 0 and consider also the last position
                            for (int l = 0; l <= insertRoute.getCustomersLength(); ++l) {

//                            if (r.nextDouble() > rate)
//                                continue;

                                newMove = new MyRelocateMove2E(instance, new int[]{i, j}, k, new int[]{n, m}, l, solution);
//                                if(!newMove.checkTwViol(solution))
//                                    continue;
                                newObjVal = ((int)(newMove.evaluate(solution) * 10000)) / 10000.0;
                                newMoveTabu = isTabu(newMove.getArcs()[0], newMove.getArcs()[1], newMove.getArcs()[2], iteration);

                                if (newObjVal < bestMoveVal) {
                                    if (newObjVal < bestSolution.getTotalCost().total || (!newMoveTabu)) {
                                        bestMove = new MyRelocateMove2E(newMove);
                                        bestMoveVal = newObjVal;
                                        bestMove.setObjVal(bestMoveVal);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestMove;
    }


    public MyExchangeMove2E findBestExchangeMove2E(Solution solution, int iteration, Solution bestSolution) throws IOException {

        MyExchangeMove2E bestMove = null;
        double bestMoveVal = Double.POSITIVE_INFINITY;

        for (int i = 0; i < solution.getDepot().getDeliveryVehicles().size(); i++) {
            BigVehicle bv = solution.getDepot().getDeliveryVehicles().get(i);
            for (int j = 0; j < bv.getDummySatellites().size(); j++) {
                DummySatellites ds = bv.getDummySatellites().get(j);
                SmallVehicle deleteRoute = ds.getVehicle();

                for (int n = 0; n < solution.getDepot().getDeliveryVehicles().size(); n++) {
                    BigVehicle bv2 = solution.getDepot().getDeliveryVehicles().get(n);

                    for (int m = 0; m < bv2.getDummySatellites().size(); m++) {

                        if (i == n)
                            continue;

                        for (int k = 0; k < deleteRoute.getCustomers().size(); k++) {

                            DummySatellites ds2 = bv2.getDummySatellites().get(m);
                            SmallVehicle insertRoute = ds2.getVehicle();

                            MyExchangeMove2E newMove;
                            double newObjVal;
                            boolean newMoveTabu;

                            // evaluate each position of the route to find the best insertion of the getCustomer(); start from 0 and consider also the last position
                            for (int l = 0; l < insertRoute.getCustomersLength(); ++l) {

//                                if (r.nextDouble() > rate)
//                                    continue;
                                newMove = new MyExchangeMove2E(instance, new int[]{i, j}, k, new int[]{n, m}, l, solution);
                                newObjVal = ((int)(newMove.evaluate(solution) * 10000)) / 10000.0;
                                newMoveTabu = isTabu(newMove.getArcs()[0], newMove.getArcs()[1], newMove.getArcs()[2], iteration);

                                if (newObjVal < bestMoveVal) {
                                    if (newObjVal < bestSolution.getTotalCost().total || (!newMoveTabu)) {
                                        bestMove = new MyExchangeMove2E(newMove);
                                        bestMoveVal = newObjVal;
                                        bestMove.setObjVal(bestMoveVal);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestMove;
    }


    public MyRelocateMove1E findBestRelocationMove1E(Solution solution, int iteration, Solution bestSolution) {

        MyRelocateMove1E bestMove = null;
        double bestMoveVal = Double.POSITIVE_INFINITY;

        for (int i = 0; i < solution.getDepot().getDeliveryVehicles().size(); i++) {
            BigVehicle bv = solution.getDepot().getDeliveryVehicles().get(i);

            for (int n = 0; n < solution.getDepot().getDeliveryVehicles().size(); n++) {
                BigVehicle bv2 = solution.getDepot().getDeliveryVehicles().get(n);

                if (i == n)
                    continue;

                for (int j = 0; j < bv.getSatelliteLength(); j++) {

                    MyRelocateMove1E newMove = null;
                    Cost newCost = null;
                    double newObjVal = 0;
                    boolean newMoveTabu = false;

                    for (int m = 0; m <= bv2.getSatelliteLength(); m++) {

                        newMove = new MyRelocateMove1E(instance, i, j, n, m, solution);
                        newObjVal = ((int)(newMove.evaluate(solution) * 10000)) / 10000.0;
                        newMoveTabu = isTabu(newMove.getArcs()[0], newMove.getArcs()[1], newMove.getArcs()[2], iteration);


//                        Solution s = new Solution(solution);
//                        newMove.operateOn(s);
//                        if (Math.abs(s.totalCost.total - newObjVal) > 0.0001) {
//                            System.out.println(s.totalCost.total);
//                            System.out.println(newObjVal);
//                            System.out.println();
//                            newObjVal = newMove.evaluate(solution);
//                        }


                        if (newObjVal < bestMoveVal) {
                            if (newObjVal < bestSolution.getTotalCost().total || (!newMoveTabu)) {
                                bestMove = new MyRelocateMove1E(newMove);
                                bestMoveVal = newObjVal;
                                bestMove.setObjVal(bestMoveVal);
                            }
                        }
                    }
                }
            }
        }
        return bestMove;
    }


    public MyExchangeMove1E findBestExchangeMove1E(Solution solution, int iteration, Solution bestSolution) {

        MyExchangeMove1E bestMove = null;
        double bestMoveVal = Double.POSITIVE_INFINITY;

        for (int i = 0; i < solution.getDepot().getDeliveryVehicles().size(); i++) {
            BigVehicle bv = solution.getDepot().getDeliveryVehicles().get(i);

            for (int n = 0; n < solution.getDepot().getDeliveryVehicles().size(); n++) {
                BigVehicle bv2 = solution.getDepot().getDeliveryVehicles().get(n);

                if (i == n)
                    continue;

                for (int j = 0; j < bv.getSatelliteLength(); j++) {

                    MyExchangeMove1E newMove = null;
                    Cost newCost = null;
                    double newObjVal = 0;
                    boolean newMoveTabu = false;

                    for (int m = 0; m < bv2.getSatelliteLength(); m++) {

                        newMove = new MyExchangeMove1E(instance, i, j, n, m, solution);
                        newObjVal = ((int)(newMove.evaluate(solution) * 10000)) / 10000.0;
                        newMoveTabu = isTabu(newMove.getArcs()[0], newMove.getArcs()[1], newMove.getArcs()[2], iteration);

                        if (newObjVal < bestMoveVal) {
                            if (newObjVal < bestSolution.getTotalCost().total || (!newMoveTabu)) {
                                bestMove = new MyExchangeMove1E(newMove);
                                bestMoveVal = newObjVal;
                                bestMove.setObjVal(bestMoveVal);
                            }
                        }
                    }
                }
            }
        }
        return bestMove;
    }


    public SatelliteChangeMove findBestSatelliteChangeMove(Solution solution, int iteration, Solution bestSolution) {

        SatelliteChangeMove bestMove = null;
        double bestMoveVal = Double.POSITIVE_INFINITY;

        for (int i = 0; i < solution.getDepot().getDeliveryVehicles().size(); i++) {
            BigVehicle bv = solution.getDepot().getDeliveryVehicles().get(i);

            for (int n = 0; n < solution.getDepot().getDeliveryVehicles().size(); n++) {
                BigVehicle bv2 = solution.getDepot().getDeliveryVehicles().get(n);

                if (i == n)
                    continue;

                for (int j = 0; j < bv.getSatelliteLength(); j++) {

                    SatelliteChangeMove newMove = null;
                    Cost newCost = null;
                    double newObjVal = 0;
                    boolean newMoveTabu = false;

                    for (int m = 0; m < bv2.getSatelliteLength(); m++) {

                        if (bv.getSatellite(j).getId() == bv2.getSatellite(m).getId())
                            continue;

                        newMove = new SatelliteChangeMove(instance, i, j, n, m, solution);

                        Solution s = new Solution(solution);
                        newMove.operateOn(s);
                        newCost = new Cost(s.getTotalCost());
                        newObjVal = newCost.total;
                        newMoveTabu = isTabu(newMove.getArcs()[0], newMove.getArcs()[1], newMove.getArcs()[2], iteration);

                        if (newObjVal < bestMoveVal) {
                            if (newObjVal < bestSolution.getTotalCost().total || (!newMoveTabu)) {
                                bestMove = new SatelliteChangeMove(newMove);
                                bestMoveVal = newObjVal;
                                bestMove.setObjVal(bestMoveVal);
                            }
                        }
                    }
                }
            }
        }
        return bestMove;
    }


    public MyRelocateMove3E findBestRelocationMove3E(Solution solution, int iteration, Solution bestSolution) {
        MyRelocateMove3E bestMove = null;
        double bestMoveVal = Double.POSITIVE_INFINITY;

        for (int i = 0; i < solution.getDepot().getPickupVehicles().size(); i++) {
            BigVehicle bv = solution.getDepot().getPickupVehicles().get(i);

            for (int n = 0; n < solution.getDepot().getPickupVehicles().size(); n++) {
                BigVehicle bv2 = solution.getDepot().getPickupVehicles().get(n);

                if (i == n)
                    continue;

                for (int j = 0; j < bv.getSatelliteLength(); j++) {
                    DummySatellites ds = bv.getDummySatellites().get(j);

//                    if (r.nextDouble() > rate)
//                        continue;

                    MyRelocateMove3E newMove = null;
                    double newObjVal = 0;
                    boolean newMoveTabu = false;

                    for (int m = 0; m <= bv2.getSatelliteLength(); m++) {

                        newMove = new MyRelocateMove3E(instance, i, j, n, m, solution);
                        newObjVal = newMove.evaluate(solution);
                        newMoveTabu = isTabu(newMove.getArcs()[0], newMove.getArcs()[1], newMove.getArcs()[2], iteration);

                        if (newObjVal < bestMoveVal) {
                            if (newObjVal < bestSolution.getTotalCost().total || (!newMoveTabu)) {
                                bestMove = new MyRelocateMove3E(newMove);
                                bestMoveVal = newObjVal;
                                bestMove.setObjVal(bestMoveVal);
                            }
                        }
                    }
                }
            }
        }
        return bestMove;
    }


    public MyExchangeMove3E findBestExchangeMove3E(Solution solution, int iteration, Solution bestSolution) {
        MyExchangeMove3E bestMove = null;
        double bestMoveVal = Double.POSITIVE_INFINITY;

        for (int i = 0; i < solution.getDepot().getPickupVehicles().size(); i++) {
            BigVehicle bv = solution.getDepot().getPickupVehicles().get(i);

            for (int n = 0; n < solution.getDepot().getPickupVehicles().size(); n++) {
                BigVehicle bv2 = solution.getDepot().getPickupVehicles().get(n);

                if (i == n)
                    continue;

                for (int j = 0; j < bv.getSatelliteLength(); j++) {
                    MyExchangeMove3E newMove = null;
                    double newObjVal = 0;
                    boolean newMoveTabu = false;

                    for (int m = 0; m < bv2.getSatelliteLength(); m++) {

//                        if (r.nextDouble() > rate)
//                            continue;

                        newMove = new MyExchangeMove3E(instance, i, j, n, m, solution);
                        newObjVal = newMove.evaluate(solution);
                        newMoveTabu = isTabu(newMove.getArcs()[0], newMove.getArcs()[1], newMove.getArcs()[2], iteration);

                        if (newObjVal < bestMoveVal) {
                            if (newObjVal < bestSolution.getTotalCost().total || (!newMoveTabu)) {
                                bestMove = new MyExchangeMove3E(newMove);
                                bestMoveVal = newObjVal;
                                bestMove.setObjVal(bestMoveVal);
                            }
                        }
                    }
                }
            }
        }
        return bestMove;
    }

    public void applyMove(Solution solution, MyMove bestMove, int iteration) {
        if (bestMove == null)
            return;
        bestMove.applyTabuArc(tabuMatrix, iteration, tabuHorizon);
        bestMove.operateOn(solution);
    }

    private boolean isTabu(TabuArc arc1, TabuArc arc2, TabuArc arc3, int iteration) {

        return (iteration <= tabuMatrix[arc1.getFrom1()][arc1.getFrom2()][arc1.getTo1()][arc1.getTo2()]) &&
                (iteration <= tabuMatrix[arc2.getFrom1()][arc2.getFrom2()][arc2.getTo1()][arc2.getTo2()]) &&
                (iteration <= tabuMatrix[arc3.getFrom1()][arc3.getFrom2()][arc3.getTo1()][arc3.getTo2()]);
    }

}