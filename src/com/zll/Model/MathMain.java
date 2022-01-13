package com.zll.Model;

import com.zll.twoEVRP.*;
import ilog.concert.IloException;

import java.io.IOException;

/**
 * @author： zll-hust
 * @date： 2020/12/27 10:11
 * @description： CPLEX模型
 */
public class MathMain {

    public static String[] instance_accepted_15 = {"Ca1-2,3,15", "Ca1-3,5,15", "Ca2-2,3,15", "Ca3-3,5,15", "Cd5-3,5,15"};
    public static String[] instance_accepted_30 = {"Ca3-2,3,30", "Ca3-3,5,30", "Cd5-3,5,30"};
    public static String[] instance_accepted_50 = {"Ca3-2,3,50", "Ca3-3,5,50", "Cd5-3,5,50"};
    public static String[] instance_accepted_100 = {"Ca2-2,3,100", "Cd5-3,5,100", "Cd5-6,4,100"};

//    public static void main(String[] args) throws IOException, IloException {
//        Parameters para = new Parameters("s,2,7,9");
//        int dsn = 2, bvNr = 2;

    public static double[] test(String instanceName, int dsn, int bvNr) throws IloException, IOException {
        Parameters para = new Parameters(instanceName, 0);

        Instance instance = new Instance(para);
        instance.readInstanceFileType2();

        CheckSolution check = new CheckSolution(instance);

        Model model = new Model(instance, dsn, bvNr);
        model.buildModel();

        Duration duration = new Duration();
        duration.reset();
        duration.start();

        Solution bestSolution = model.solve();
        check.checkModelSolution(bestSolution);
        System.out.println(bestSolution);

        duration.stop();
        System.out.println("\n\n total time cost of our search = " +
                (double) (duration.getMinutes() * 60 + duration.getSeconds() + (double) duration.getMilliSeconds() / 1000));

        bestSolution.runTime = (double) (duration.getHours() * 3600 + duration.getMinutes() * 60 + duration.getSeconds() + (double) duration.getMilliSeconds() / 1000);
        bestSolution.printSolution("cplex/" + para.getInputFileName() + dsn + bvNr);

        double[] str = bestSolution.getTestInfo();
        str[7] = (double) (duration.getHours() * 3600 + duration.getMinutes() * 60 + duration.getSeconds() + (double) duration.getMilliSeconds() / 1000);
        return str;
    }
}
