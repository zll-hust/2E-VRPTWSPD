package com.zll.Main;

import com.zll.Algorithm.*;
import com.zll.twoEVRP.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author zll_hust
 */
class Main {
//    public static void main(String[] args) throws IOException {
//        String instanceName = "100-5-1";
//        int seed = 0;
        
    public static double[] test(String instanceName, int seed, double pt) throws IOException {

        // print astringency
        double[] astr = new double[30000];
    	
        Parameters para = new Parameters(instanceName, seed);

        Instance instance = new Instance(para);
        instance.readInstanceFileType4(0.5, instanceName);
//        instance.readInstanceFileType2();

        Duration duration = new Duration();
        duration.reset();
        duration.start();

        CheckSolution check = new CheckSolution(instance);

        Greedy greedyVRP = new Greedy(instance);
        int[] dsNr = new int[instance.getTotalNodeNr()]; // 虚拟节点的数量，用于构建禁忌表
        Arrays.fill(dsNr, 1);
        Solution iniSolution = greedyVRP.FindSolution(dsNr);
        System.out.println("Initial solution Total Cost: " + iniSolution.getTotalCost().getTotal());

        Solution solution = new Solution(iniSolution);
        Solution bestSolution = new Solution(iniSolution);

        int tabuIterations = para.getTabuIterations();
        TabuSearch tabuSearchVRP = new TabuSearch(para, instance, dsNr);

//        MyMove chosenMove;
//        int[] bs = new int[7];
//        Arrays.fill(bs, 0);

        for (int i = 1; i <= 30005; i++) {
            if (i - bestSolution.iteration > 5000) {
                break;
            }

            ArrayList<MyMove> moves = new ArrayList<>();
            int first = para.getRandom().nextInt(6);
            int second = para.getRandom().nextInt(6);
            int third = 7; //para.getRandom().nextInt(6);
            while (second == first) {
                second = para.getRandom().nextInt(6);
            }
//            while (third == first || third == second) {
//                third = para.getRandom().nextInt(6);
//            }

            if (first == 0 || second == 0 || third == 0) {
                MyMove bestExchangeMove2E = tabuSearchVRP.findBestExchangeMove2E(solution, i, bestSolution);
                if (bestExchangeMove2E != null)
                    moves.add(bestExchangeMove2E);
            }

            if (first == 1 || second == 1 || third == 1) {
                MyMove bestRelocate1E = tabuSearchVRP.findBestRelocationMove1E(solution, i, bestSolution);
                if (bestRelocate1E != null)
                    moves.add(bestRelocate1E);
            }

            if (first == 4 || second == 4 || third == 4) {
                MyMove bestRelocate3E = tabuSearchVRP.findBestRelocationMove3E(solution, i, bestSolution);
                if (bestRelocate3E != null)
                    moves.add(bestRelocate3E);
            }

            if (first == 2 || second == 2 || third == 2) {
                MyMove bestRelocate2E = tabuSearchVRP.findBestRelocationMove2E(solution, i, bestSolution);
                if (bestRelocate2E != null)
                    moves.add(bestRelocate2E);
            }

            if (first == 3 || second == 3 || third == 3) {
                MyMove bestExchangeMove1E = tabuSearchVRP.findBestExchangeMove1E(solution, i, bestSolution);
                if (bestExchangeMove1E != null)
                    moves.add(bestExchangeMove1E);
            }

            if (first == 5 || second == 5 || third == 5) {
                MyMove bestExchangeMove3E = tabuSearchVRP.findBestExchangeMove3E(solution, i, bestSolution);
                if (bestExchangeMove3E != null)
                    moves.add(bestExchangeMove3E);
            }

            MyMove bestMove = MyMove.findBestMove(moves, para.getRandom());
            bestMove = MyMove.findBestMove(moves, para.getRandom());
            tabuSearchVRP.applyMove(solution, bestMove, i);
            solution.getTotalCost().updatePara();
            bestSolution.getTotalCost().calculateTotal();

            if (solution.getTotalCost().total < bestSolution.getTotalCost().total && solution.totalCost.loadViol == 0 && solution.totalCost.twViol == 0) {
                bestSolution = new Solution(solution);
                bestSolution.iteration = i;
                System.out.println("Iteration " + i + " - total cost: " + bestSolution.getTotalCost().getTotal() + " - distance: " + bestSolution.getTotalCost().getDistance());
            }
            
            astr[i] = bestSolution.getTotalCost().getTotal();

//
//            if (bestMove instanceof MyRelocateMove1E) {
//                bs[0]++;
//            } else if (bestMove instanceof MyRelocateMove2E) {
//                bs[1]++;
//            } else if (bestMove instanceof MyRelocateMove3E) {
//                bs[2]++;
//            } else if (bestMove instanceof MyExchangeMove1E) {
//                bs[3]++;
//            } else if (bestMove instanceof MyExchangeMove2E) {
//                bs[4]++;
//            } else if (bestMove instanceof MyExchangeMove3E) {
//                bs[5]++;
//            }
        }

        duration.stop();

//        System.out.println(bestSolution);
//        System.out.println(check.check(bestSolution));
//        System.out.println(Cost.Alpha + " " + Cost.Beta + " " + Cost.Sita);

        System.out.println("Best solution found at iteration " + bestSolution.iteration
                + ", Total Cost: " + bestSolution.getTotalCost().getTotal());
        System.out.println("Initial Cost: " + iniSolution.getTotalCost().getTotal());
//        for (int i = 0; i < 7; i++)
//            System.out.println(bs[i]);
        if (iniSolution.getTotalCost().twViol == 0) {
            System.out.println("存在可行解");
//            bestSolution.printSolution("/heuristic/" + instanceName + "可行");
        } else {
            System.out.println("不存在可行解");
//            bestSolution.printSolution("/heuristic/" + instanceName + "不可行");
        }


        bestSolution.printSolution("/heuristic/" + instanceName);

        System.out.println("time cost of this search: " +
                (double) (duration.getMinutes() * 60 + duration.getSeconds() + (double) duration.getMilliSeconds() / 1000));

        double[] str = bestSolution.getTestInfo();
        str[7] = (double) (duration.getHours() * 3600 + duration.getMinutes() * 60 + duration.getSeconds() + (double) duration.getMilliSeconds() / 1000);
        str[8] = bestSolution.iteration;
//        return str;
        return astr;
    }
}