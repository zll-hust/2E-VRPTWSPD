package com.zll.Main;

import com.zll.Model.MathMain;
//import ilog.concert.IloException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author： zll-hust
 * @date： 2020/12/9 16:26
 * @description： 批量测试
 */
public class Test {

    public static String[] s_1 = {"7-2-1", "7-2-1b", "7-2-2", "7-2-2b", "7-2-3", "7-2-3b",
            "10-2-1", "10-2-1b", "10-2-2", "10-2-2b", "10-2-3", "10-2-3b",
            "12-2-1", "12-2-1b", "12-2-2", "12-2-2b", "12-2-3", "12-2-3b"};//"7-2-1b",

    public static String[] s_2 = {"100-5-1", "100-5-1b", "100-5-2", "100-5-2b", "100-5-3", "100-5-3b",
            "100-10-1", "100-10-1b", "100-10-2", "100-10-2b", "100-10-3", "100-10-3b",
            "200-10-1", "200-10-1b", "200-10-2", "200-10-2b", "200-10-3", "200-10-3b"};

    public static String[] s_3 = {"100-5-1", "100-5-2", "100-5-2b", "100-5-3b",

            "100-10-1", "100-10-2", "100-10-3",};

    public static String[] chosenInstanceSet = {
    		"100-10-1", "100-10-1b", "100-10-2", "100-10-2b", "100-10-3", "100-10-3b"  };
    public static int runTimes = 1;

    public static void main(String[] args) throws IOException { //, IloException
//        printMethod1();

//        printMethod2(4, 2, s_1);
//        printMethod2(3, 2, s_1);
        
        printMethod3();
        printMethod3();
        printMethod3();
//        printMethod3();
//        printMethod3();
    }

    private static void printMethod0() throws IOException {

        double[][][] results = new double[chosenInstanceSet.length][runTimes][];
        double[][][] rr = new double[chosenInstanceSet.length][runTimes][9];
        for (int j = 0; j < chosenInstanceSet.length; j++) {
            for (int i = 0; i < runTimes; i++) {
                results[j][i] = Main.test(chosenInstanceSet[j], runTimes + 60, 0.5);
            }
        }
        printToCSV2("./output/2E-VRP " + System.currentTimeMillis() + ".csv", results);
        System.out.println("\t######## print end ########");
    }


    private static void printMethod1() throws IOException {

        double[][][] results = new double[chosenInstanceSet.length][runTimes][];
        double[][][] rr = new double[chosenInstanceSet.length][3][9];
        for (int j = 0; j < chosenInstanceSet.length; j++) {
            for (int i = 0; i < runTimes; i++) {
                results[j][i] = Main.test(chosenInstanceSet[j], 5, 0.5);
            }

            double smallest = Double.POSITIVE_INFINITY, biggest = Double.NEGATIVE_INFINITY;
            Arrays.fill(rr[j][0], 0);
            int sId = 0, bId = 0;
            for (int i = 0; i < runTimes; i++) {
                if (results[j][i][0] < smallest) {
                    smallest = results[j][i][0];
                    sId = i;
                }
                if (results[j][i][0] > biggest) {
                    biggest = results[j][i][0];
                    bId = i;
                }
                for (int k = 0; k < rr[j][0].length; k++) {
                    rr[j][0][k] += results[j][i][k];
                }
            }
            for (int k = 0; k < rr[j][0].length; k++) {
                rr[j][0][k] /= runTimes;
                rr[j][1][k] = results[j][bId][k];
                rr[j][2][k] = results[j][sId][k];
            }
        }
        printToCSV2("./output/2E-VRP " + System.currentTimeMillis() + ".csv", rr);
        System.out.println("\t######## print end ########");
    }

//    private static void printMethod2(int dsn, int bvNr, String[] set) throws IloException, IOException {
//
//        for (int i = 0; i < 1; i++) {
//            double[][] results = new double[set.length][];
//            int instanceId1 = 0;
//            for (String instance : set) {
//                results[instanceId1] = MathMain.test(instance, dsn, bvNr);
//                instanceId1++;
//            }
//
//            printToCSV("./output/2E-VRP " + System.currentTimeMillis() + ".csv", results);
//
//            System.out.println("\t######## print end ########");
//        }
//    }
    
    
    private static void printMethod3() throws IOException {

        double[][] results = new double[chosenInstanceSet.length][30000];
        for (int j = 0; j < chosenInstanceSet.length; j++) {
        	results[j] = Main.test(chosenInstanceSet[j], runTimes + 60, 0.5);
        }
        printToCSV3("./output/2E-VRP " + System.currentTimeMillis() + ".csv", results);
        System.out.println("\t######## print end ########");
    }

    

    public static void printToCSV(String FILE_NAME, double[][] results) {
        final String[] FILE_HEADER = {"Ins", "Obj", "1ENr", "2ENr", "1ECost", "2ECost", "pkCost", "mSNr", "runTime"};

        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER);

        try {
            fileWriter = new FileWriter(FILE_NAME);
            csvPrinter = new CSVPrinter(fileWriter, csvFormat);

            for (int i = 0; i < results.length; i++) {
                List<String> record = new ArrayList<>();
                record.add(chosenInstanceSet[i]);
                for (double d : results[i]) {
                    record.add(String.valueOf(d));
                }
                csvPrinter.printRecord(record);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvPrinter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void printToCSV2(String FILE_NAME, double[][][] results) {
        final String[] FILE_HEADER = {"Ins", "Obj", "1ENr", "2ENr", "1ECost", "2ECost", "pkCost", "mSNr", "runTime"};

        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER);

        try {
            fileWriter = new FileWriter(FILE_NAME);
            csvPrinter = new CSVPrinter(fileWriter, csvFormat);
            for (int j = 0; j < 3; j++) { // runTimes
                for (int i = 0; i < results.length; i++) {
                    List<String> record = new ArrayList<>();
                    record.add(chosenInstanceSet[i]);
                    for (double d : results[i][j]) {
                        record.add(String.valueOf(d));
                    }
                    csvPrinter.printRecord(record);
                }
                csvPrinter.printRecord();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvPrinter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public static void printToCSV3(String FILE_NAME, double[][] results) {
        final String[] FILE_HEADER = {"Iter", "Obj"};

        FileWriter fileWriter = null;
        CSVPrinter csvPrinter = null;
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER);

        try {
            fileWriter = new FileWriter(FILE_NAME);
            csvPrinter = new CSVPrinter(fileWriter, csvFormat);
            
            List<String> record = new ArrayList<>();
            for (int i = 0; i < chosenInstanceSet.length; i++) {
            	record.add(chosenInstanceSet[i]);
            }
            csvPrinter.printRecord(record);
            
            for (int j = 0; j < 30000; j++) { // runTimes
                record = new ArrayList<>();
                record.add(String.valueOf(j));
                for (int i = 0; i < results.length; i++) {
                	record.add(String.valueOf(results[i][j]));
                }
                csvPrinter.printRecord(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvPrinter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
