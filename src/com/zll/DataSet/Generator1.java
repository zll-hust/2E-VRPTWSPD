package com.zll.DataSet;

import java.io.*;
import java.util.Random;

/**
 * @author： zll-hust
 * @date： 2021/1/19 17:07
 * @description： TODO
 */
public class Generator1 {
    public static int[] cScale = new int[]{100, 300, 700, 1000};
    public static int[] sScale = new int[]{10, 20, 30, 40};
    public static int runTimes = 10;

    public static void main(String[] args) throws IOException {
        test();
    }

    public static void test() throws IOException {
        for (int i = 0; i < runTimes; i++) {
//            for(int j = 0; j < cScale.length; j++){
            int cNr = cScale[2];
            int sNr = sScale[2];
            int[] redius = new int[]{80, 100, 110, 120};
            double[] rate = new double[]{0.45, 0.3, 0.2, 0.05};
            Generator1 g = new Generator1(redius, cNr, sNr, rate);
            Node[] customers = g.randomGenerateCustomers();
            Node[] satellites = g.randomGenerateSatellites(customers);
            Node depot = g.randomGenerateDepot(customers, satellites);
            printToTxt(customers, satellites, depot, "Ca" + i + "," + sNr + "," + cNr);
//            }
        }
        System.out.println("\t######## print end ########");
    }

    public static void printToTxt(Node[] cs, Node[] ss, Node d, String filename) throws IOException {
        File f = new File("./output/" + filename + ".txt");//指定文件
        FileOutputStream fos = new FileOutputStream(f);//创建输出流fos并以f为参数
        OutputStreamWriter osw = new OutputStreamWriter(fos);//创建字符输出流对象osw并以fos为参数
        BufferedWriter bw = new BufferedWriter(osw);//创建一个带缓冲的输出流对象bw，并以osw为参数

        StringBuilder str = new StringBuilder();
        for(Node c : cs){
            str.append(" " + c.getxCoordinate() + "\t");
            str.append(c.getyCoordinate() + "\t");
            str.append(c.getStartTw() + "\t");
            str.append(c.getEndTw() + "\t");
            str.append(c.getDeliveryDemand() + "\t");
            str.append(c.getPickupDemand() + "\t");
            str.append("10" + "\n");
        }

        for(Node s : ss){
            str.append(" " + s.getxCoordinate() + "\t");
            str.append(s.getyCoordinate() + "\n");
        }

        str.append(" " + d.getxCoordinate() + "\t");
        str.append(d.getyCoordinate());

        bw.write(str.toString());//使用bw写入一行文字，为字符串形式String
        bw.newLine();//换行
        bw.close();//关闭并保存
    }


    public int[] redius;

    public int customerNr;
    public int satellitesNr;

    public double[] cAreaRate;

    public Random random;


    public Generator1(int[] redius, int customerNr, int satellitesNr, double[] cAreaRate) {
        this.redius = redius;
        this.customerNr = customerNr;
        this.satellitesNr = satellitesNr;
        this.cAreaRate = cAreaRate;
        random = new Random();
//        random.setSeed(0);
    }

    public Node[] randomGenerateCustomers() {
        Node[] nodes = new Node[customerNr];
        int[] cAreaNr = new int[4];
        for (int i = 0; i < 3; i++) {
            cAreaNr[i] = (int) Math.floor(customerNr * cAreaRate[i]);
        }
        cAreaNr[3] = customerNr - cAreaNr[0] - cAreaNr[1] - cAreaNr[2];

        int nId = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < cAreaNr[i]; j++) {
                boolean repeat = false;
                do {
                    int r = random.nextInt(redius[i]);
                    int angle = random.nextInt(360);
                    int x = (int) (r * Math.cos(Math.toRadians(angle)));
                    int y = (int) (r * Math.sin(Math.toRadians(angle)));
                    nodes[nId] = new Node();
                    nodes[nId].generateCaNode(random, x, y);

                    repeat = false;
                    for (int k = 0; k < nId; k++) {
                        if (nodes[k].equals(nodes[nId]))
                            repeat = true;
                    }
                } while (repeat);

                nId++;
            }
        }
        return nodes;
    }

    public Node[] randomGenerateSatellites(Node[] c) {
        Node[] nodes = new Node[satellitesNr];
        for (int i = 0; i < satellitesNr; i++) {
            boolean repeat = false;
            do {
                int lb = 360 / satellitesNr * i;
                int ub = 360 / satellitesNr * (i + 1);
                int r = random.nextInt(redius[1] - redius[0]) + redius[0];
                int angle = random.nextInt(ub - lb) + lb;
                int x = (int) (r * Math.cos(Math.toRadians(angle)));
                int y = (int) (r * Math.sin(Math.toRadians(angle)));
                nodes[i] = new Node(x, y);

                repeat = false;
                for (int j = 0; j < i; j++) {
                    if (nodes[j].equals(nodes[i]))
                        repeat = true;
                }
                for (int j = 0; j < c.length; j++) {
                    if (c[j].equals(nodes[i]))
                        repeat = true;
                }
            }
            while (repeat);
        }
        return nodes;
    }

    public Node randomGenerateDepot(Node[] c, Node[] s) {
        Node n;
        boolean repeat = false;
        do {
            int r = random.nextInt(redius[3] - redius[2]) + redius[2];
            int angle = random.nextInt(360);
            int x = (int) (r * Math.cos(Math.toRadians(angle)));
            int y = (int) (r * Math.sin(Math.toRadians(angle)));
            n = new Node(x, y);

            repeat = false;
            for (int j = 0; j < c.length; j++) {
                if (c[j].equals(n))
                    repeat = true;
            }
            for (int j = 0; j < s.length; j++) {
                if (s[j].equals(n))
                    repeat = true;
            }
        }
        while (repeat);
        return n;
    }
}
