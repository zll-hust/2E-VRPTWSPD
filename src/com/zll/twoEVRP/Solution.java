package com.zll.twoEVRP;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Solution {

    public int iteration;
    private Depot depot;
    private Instance instance;
    public Cost totalCost;
    private Cost costForDelivery2E;
    private Cost costForDelivery1E;
    private Cost costForPickup1E;
    public double runTime;

    public Solution(Depot depot, Instance instance) {
        this.depot = depot;
        this.instance = instance;
        this.totalCost = new Cost();
        this.costForDelivery1E = new Cost();
        this.costForDelivery2E = new Cost();
        this.costForPickup1E = new Cost();
        updateTotalCost();
    }

    public Solution(Solution s) {
        this.instance = s.getInstance();
        this.depot = new Depot(s.getDepot());
        this.totalCost = new Cost(s.getTotalCost());
        this.costForDelivery1E = new Cost(s.getCostForDelivery1E());
        this.costForDelivery2E = new Cost(s.getCostForDelivery2E());
        this.costForPickup1E = new Cost(s.getCostForPickup1E());
        this.iteration = 0;
    }

    public void iniCost() {
        this.totalCost.initialize();
        this.costForPickup1E.initialize();
        this.costForDelivery2E.initialize();
        this.costForDelivery1E.initialize();
    }

    public void addToTotal() {
        this.totalCost.initialize();
        this.totalCost.addCost(costForDelivery1E);
        this.totalCost.addCost(costForDelivery2E);
        this.totalCost.addCost(costForPickup1E);
        this.totalCost.calculateTotal();
    }

    public void updateTotalCost() {
        iniCost();
        for (BigVehicle bv : depot.getDeliveryVehicles()) {
            for (DummySatellites ds : bv.getDummySatellites())
                this.costForDelivery2E.addCost(ds.getVehicle().getCost());
            this.costForDelivery1E.addCost(bv.getCost());
        }
        for (BigVehicle bv : depot.getPickupVehicles()) {
            this.costForPickup1E.addCost(bv.getCost());
        }
        addToTotal();
    }

    public String toString() {
        updateTotalCost();
        StringBuffer print = new StringBuffer();
        for(int i = 0; i < 30; i++)
            print.append("*");
        print.append(depot);
        print.append("\n1st delivery cost is: " + costForDelivery1E.getDistance() +
                "\n2nd delivery cost is: " + costForDelivery2E.getDistance() +
                "\npickup cost is: " + costForPickup1E.getDistance() +
                "\ntotal distance is: " + totalCost.getDistance() +
                "\ntotal cost is: " + totalCost.getTotal());
        return print.toString();
    }

    public void printSolution(String filename) throws IOException {
        File file = new File("./output/" + filename);
        if(!file.exists()){//如果文件夹不存在
            file.mkdir();//创建文件夹
        }

        String filePath = "./output/" + filename + "/" + String.valueOf(Math.floor(totalCost.getDistance())) + ".txt";

        String path = filePath;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));

        writer.write(this.toString());
        writer.newLine();
        writer.write(String.valueOf(runTime));
        writer.close();
    }

    public void printTestInfo(){
        StringBuilder str = new StringBuilder();
        str.append("1E routes number:  " + this.getDepot().getDeliveryVehicles().size() + "\n");
        int nr = 0, ms = 0;
        for(BigVehicle bv : this.getDepot().getDeliveryVehicles()) {
            nr += bv.getSatelliteLength();
            if(ms < bv.getSatelliteLength())
                ms = bv.getSatelliteLength();
        }
        str.append("2E routes number:  " + nr + "\n");
        str.append("1E routes costs:  " + this.costForDelivery1E.distance + "\n");
        str.append("2E routes costs:  " + this.costForDelivery2E.distance + "\n");
        str.append("1Ep routes costs:  " + this.costForPickup1E.distance + "\n");
        str.append("most sa in 2E:  " + ms);

        System.out.println(str);
    }

    public double[] getTestInfo(){
        double[] str = new double[9];
        if(this.totalCost.distance == this.totalCost.total)
            str[0] = this.totalCost.distance;
        else
            str[0] = Double.POSITIVE_INFINITY;
        str[1] = this.getDepot().getDeliveryVehicles().size();
        int nr = 0, ms = 0;
        for(BigVehicle bv : this.getDepot().getDeliveryVehicles()) {
            nr += bv.getSatelliteLength();
            if(ms < bv.getSatelliteLength())
                ms = bv.getSatelliteLength();
        }
        str[2] = nr;
        str[3] = this.costForDelivery1E.distance;
        str[4] = this.costForDelivery2E.distance;
        str[5] = this.costForPickup1E.distance;
        str[6] = ms;

        return str;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Cost getTotalCost() {
        return this.totalCost;
    }

    public void setTotalCost(Cost totalCost) {
        this.totalCost = totalCost;
    }

    public void addDistance(double travelTime) {
        totalCost.distance += travelTime;
    }

    public void addLoad(double load) {
        totalCost.load += load;
    }

    public void addWaitingTime(double waitingTime) {
        totalCost.waitingTime += waitingTime;
    }

    public Cost getCostForDelivery2E() {
        return costForDelivery2E;
    }

    public void setCostForDelivery2E(Cost costForDelivery2E) {
        this.costForDelivery2E = costForDelivery2E;
    }

    public Cost getCostForDelivery1E() {
        return costForDelivery1E;
    }

    public void setCostForDelivery1E(Cost costForDelivery1E) {
        this.costForDelivery1E = costForDelivery1E;
    }

    public Cost getCostForPickup1E() {
        return costForPickup1E;
    }

    public void setCostForPickup1E(Cost costForPickup1E) {
        this.costForPickup1E = costForPickup1E;
    }
}