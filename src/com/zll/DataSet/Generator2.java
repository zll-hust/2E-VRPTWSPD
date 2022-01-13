package com.zll.DataSet;

import com.zll.twoEVRP.Customer;
import com.zll.twoEVRP.Satellite;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * @author： zll-hust
 * @date： 2021/1/21 15:39
 * @description： TODO
 */
public class Generator2 {
    public double[][] distanceMatrix;
    public String filename;
    public Node[] customers;
    public int customersNr;
    public int satellitesNr;
    public int[] demands;
    public Random r;
    public int l0;
    public int serviceDuration;

    public Generator2(String filename, int customersNr, int satellitesNr) {
        this.customersNr = customersNr;
        this.satellitesNr = satellitesNr;
        this.filename = filename;
        this.distanceMatrix = new double[customersNr + satellitesNr + 1][customersNr + satellitesNr + 1];
        this.customers = new Node[customersNr];
        for (int i = 0; i < customersNr; i++)
            customers[i] = new Node();
        this.demands = new int[customersNr];
        r = new Random();
//        r.setSeed(0);

        l0 = 800;
        serviceDuration = 10;
    }

    public void generateTw() {
        for (int i = 0; i < customersNr; i++) {
            double minDis = Double.POSITIVE_INFINITY;
            int sId = 0;
            for (int j = 0; j < satellitesNr; j++) {
                if (distanceMatrix[0][1 + j] + distanceMatrix[1 + j][1 + satellitesNr + i] < minDis) {
                    minDis = distanceMatrix[0][1 + j] + distanceMatrix[1 + j][1 + satellitesNr + i];
                    sId = j;
                }
            }

            int center = (int) (r.nextInt((int) (l0 - minDis - serviceDuration - minDis)) + minDis);
            int width = r.nextInt(15) + 5;
            customers[i].startTw = center - width;
            customers[i].endTw = center + width;
        }
    }

    public void generatePDemand() {
        for (int i = 0; i < customersNr; i++) {
//            customers[i].pickupDemand = r.nextInt(demands[i] / 100) * 100;
            customers[i].pickupDemand = r.nextInt(customers[i].deliveryDemand);
        }
    }

    public void readFile1() {
        try {
            String data;
            Scanner in = new Scanner(new FileReader("./input/Set1 - aggiornato/" + filename + ".dat"));

            for (int i = 0; i < 13; i++)
                in.nextLine(); // skip unuseful lines

            for (int i = 0; i < this.satellitesNr + this.customersNr + 1; i++) {
                data = in.nextLine();
                for (int j = 0; j < this.satellitesNr + this.customersNr + 1; j++) {
                    distanceMatrix[i][j] = Integer.parseInt(data.split("\\s+")[j]);
                }
            }

            for (int i = 0; i < 5; i++)
                in.nextLine(); // skip unuseful lines

            for (int i = 0; i < this.customersNr; i++) {
                data = in.nextLine();
                demands[i] = Integer.parseInt(data.split("\\s+")[1]);
            }

            in.close();
        } catch (FileNotFoundException e) {
            // File not found
            System.out.println("Instance File not found!");
            System.exit(-1);
        }
    }

    public void readFile2() {
        try {
            String data;
            Scanner in = new Scanner(new FileReader("./input/instance/Set5_" + filename + ".dat"));

            for (int i = 0; i < 2; i++)
                in.nextLine(); // skip unuseful lines

            data = in.nextLine();
            int cap1E = Integer.parseInt(data.split(",")[1]);

            for (int i = 0; i < 2; i++)
                in.nextLine(); // skip unuseful lines

            data = in.nextLine();
            int cap2E = Integer.parseInt(data.split(",")[2]);

            for (int i = 0; i < 2; i++)
                in.nextLine(); // skip unuseful lines

            data = in.nextLine();
            int satelliteNr = data.split("\\s+").length - 1;
            Node[] satellites = new Node[satelliteNr];
            Node depot = new Node();
            depot.xCoordinate = Integer.parseInt(data.split("\\s+")[0].split(",")[0]);
            depot.yCoordinate = Integer.parseInt(data.split("\\s+")[0].split(",")[1]);

            for (int i = 0; i < satelliteNr; i++) {
                satellites[i] = new Node();
                satellites[i].xCoordinate = Integer.parseInt(data.split("\\s+")[i + 1].split(",")[0]);
                satellites[i].yCoordinate = Integer.parseInt(data.split("\\s+")[i + 1].split(",")[1]);
            }

            for (int i = 0; i < 2; i++)
                in.nextLine(); // skip unuseful lines

            data = in.nextLine();
            customersNr = data.split("\\s+").length;
            this.customers = new Node[customersNr];

            for (int i = 0; i < customersNr; i++) {
                customers[i] = new Node();
                customers[i].xCoordinate = Integer.parseInt(data.split("\\s+")[i].split(",")[0]);
                customers[i].yCoordinate = Integer.parseInt(data.split("\\s+")[i].split(",")[1]);
                customers[i].deliveryDemand = Integer.parseInt(data.split("\\s+")[i].split(",")[2]);
            }

            int[][] coodinate = new int[1 + satelliteNr + customersNr][2];
            int id = 0;
            coodinate[id][0] = depot.xCoordinate;
            coodinate[id++][1] = depot.yCoordinate;
            for (Node s : satellites) {
                coodinate[id][0] = s.xCoordinate;
                coodinate[id++][1] = s.yCoordinate;
            }
            for (Node c : customers) {
                coodinate[id][0] = c.xCoordinate;
                coodinate[id++][1] = c.yCoordinate;
            }
            this.distanceMatrix = new double[1 + satelliteNr + customersNr][1 + satelliteNr + customersNr];
            for (int i = 0; i < distanceMatrix.length; i++) {
                for (int j = 0; j < distanceMatrix[i].length; j++) {
                    distanceMatrix[i][j] = distanceMatrix[j][i] = Math.sqrt(Math.pow(coodinate[i][0] - coodinate[j][0], 2) + Math.pow(coodinate[i][1] - coodinate[j][1], 2));
                }
            }

            in.close();
        } catch (FileNotFoundException e) {
            // File not found
            System.out.println("Instance File not found!");
            System.exit(-1);
        }
    }


    public static void printToTxt(Node[] cs, String filename) throws IOException {
        File file = new File("./output/instance/" + filename);//指定文件
        if(!file.exists()){//如果文件夹不存在
            file.mkdir();//创建文件夹
        }

        file = new File("./output/instance/" + filename + "/" + filename + ".txt");//指定文件
        FileOutputStream fos = new FileOutputStream(file);//创建输出流fos并以f为参数
        OutputStreamWriter osw = new OutputStreamWriter(fos);//创建字符输出流对象osw并以fos为参数
        BufferedWriter bw = new BufferedWriter(osw);//创建一个带缓冲的输出流对象bw，并以osw为参数

        StringBuilder str = new StringBuilder();
        for (Node c : cs) {
            str.append(c.startTw + "\t");
            str.append(c.endTw + "\t");
            str.append(c.pickupDemand + "\n");
        }

        bw.write(str.toString());//使用bw写入一行文字，为字符串形式String
        bw.newLine();//换行
        bw.close();//关闭并保存
    }

    public static int runTimes = 5;
    public static String[] s_3 = {"200-10-2b"};

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < runTimes; i++) {
            for (int j = 0; j < s_3.length; j++) {
                int cNr = 100;
                int sNr = 10;
                String filename = s_3[j];
                Generator2 g = new Generator2(filename, cNr, sNr);
                g.readFile2();
                g.generatePDemand();
                g.generateTw();
                printToTxt(g.customers, filename + i);
            }
        }
        System.out.println("\t######## print end ########");
    }
}
