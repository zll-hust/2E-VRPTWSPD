package com.zll.twoEVRP;

import java.util.ArrayList;

public class CheckSolution {

    private double[][] distanceMatrix;
    private int totalNodeNr;
    public Instance instance;

    public CheckSolution(Instance instance) {
        this.distanceMatrix = instance.getDistanceMatrix();
        totalNodeNr = distanceMatrix.length;
        this.instance = instance;
    }

    public String check(Solution solution) {
        return "\nDelivery : " + CheckDeliveryRoute(solution) +
                "\n\nPickup : " + CheckPickupRoute(solution) +
                "\n check feasible: " + solution.getTotalCost().checkFeasible() +
                " time windows viol: " + solution.getTotalCost().getTwViol() +
                " load viol: " + solution.getTotalCost().getLoadViol();
    }

    public String CheckPickupRoute(Solution solution) {
        StringBuffer result = new StringBuffer();

        Depot depot = solution.getDepot();
        ArrayList<BigVehicle> vehicles = depot.getPickupVehicles();

        double totalDistance = 0;

        boolean total = true;

        for (int i = 0; i < vehicles.size(); i++) {
            BigVehicle bv = vehicles.get(i);

            double costInBigVehicle = 0;
            int loadInBigVehicle = 0;
            boolean checkCost = true;
            boolean checkLoad = true;

            DummySatellites ds = bv.getDummySatellites().get(0);
            costInBigVehicle += distanceMatrix[depot.getId()][ds.getId()];
            loadInBigVehicle += ds.getPickup().demand;

            for (int j = 1; j < bv.getDummySatellites().size(); j++) {
                ds = bv.getDummySatellites().get(j - 1);
                costInBigVehicle += distanceMatrix[ds.getId()][bv.getDummySatellites().get(j).getId()];
                loadInBigVehicle += bv.getDummySatellites().get(j).getPickup().demand;
            }

            costInBigVehicle += distanceMatrix[bv.getLastSatellite().getId()][depot.getId()];

            totalDistance += costInBigVehicle;

            if (Math.abs(bv.getCost().getDistance() - costInBigVehicle) > 0.001)
                checkCost = false;
            if (Math.abs(bv.getCost().getLoad() - loadInBigVehicle) > 0.001)
                checkLoad = false;

            if (!(checkCost && checkLoad)) total = false;

            result.append("\n\n check route " + i + ": "
                    + "\n check cost = " + costInBigVehicle + "  " + checkCost
                    + "\n check demand = " + loadInBigVehicle + "  " + checkLoad);

        }

        boolean checkTotalCost = true;
        if (Math.abs(totalDistance - solution.getCostForPickup1E().getDistance()) > 0.001)
            checkTotalCost = false;

        if (!(checkTotalCost)) total = false;

        result.append("\n\n check total cost = " + totalDistance + "  " + checkTotalCost);

        return result.toString();
    }

    public String CheckDeliveryRoute(Solution solution) {

        StringBuffer result = new StringBuffer();

        boolean checkBigCost = true;
        boolean checkTimeWindows2E = true;
        boolean checkArrive = true;
        boolean checkBigLoad = true;
        boolean checkTotalCost = true;

        boolean total = true;

        Depot depot = solution.getDepot();
        ArrayList<BigVehicle> vehicles = depot.getDeliveryVehicles();

        double totalDistance = 0;
        double twViloationInSmallVehicle = 0;

        for (int i = 0; i < vehicles.size(); i++) {
            BigVehicle bv = vehicles.get(i);

            double costInBigVehicle = 0;
            double timeInBigVehicle = 0;
            int loadInBigVehicle = 0;
            boolean checkSmallCost = true;
            boolean checkSmallLoad = true;

            DummySatellites ds = bv.getDummySatellites().get(0);
            costInBigVehicle += distanceMatrix[depot.getId()][ds.getId()];
            timeInBigVehicle += distanceMatrix[depot.getId()][ds.getId()];
            if (Math.abs(timeInBigVehicle - ds.getDelivery().getArriveTime()) > 0.0001)
                checkArrive = false;
            timeInBigVehicle += ds.getDelivery().serviceDuration;

            for (int j = 0; j < bv.getDummySatellites().size(); j++) {

                ds = bv.getDummySatellites().get(j);

                SmallVehicle sv = ds.getVehicle();
                double timeInSmallVehicle = timeInBigVehicle + distanceMatrix[ds.getId()][sv.getCustomers().get(0).getId()];
                double costInSmallVehicle = distanceMatrix[ds.getId()][sv.getCustomers().get(0).getId()];
                int loadInSmallVehicle = sv.getCustomers().get(0).getDelivery().getDemand();

                if (Math.abs(timeInSmallVehicle - sv.getCustomers().get(0).getDelivery().getArriveTime()) > 0.0001)
                    checkArrive = false;

                timeInSmallVehicle = Math.max(sv.getCustomers().get(0).getStartTw(), timeInSmallVehicle);
                twViloationInSmallVehicle += Math.max(0, timeInSmallVehicle - sv.getCustomers().get(0).getEndTw());
                timeInSmallVehicle += sv.getCustomers().get(0).getDelivery().serviceDuration;

                for (int k = 1; k < sv.getCustomers().size(); k++) {
                    timeInSmallVehicle += distanceMatrix[sv.getCustomers().get(k - 1).getId()][sv.getCustomers().get(k).getId()];
                    costInSmallVehicle += distanceMatrix[sv.getCustomers().get(k - 1).getId()][sv.getCustomers().get(k).getId()];
                    loadInSmallVehicle += sv.getCustomers().get(k).getDelivery().getDemand();
                    twViloationInSmallVehicle += Math.max(0, timeInSmallVehicle - sv.getCustomers().get(k).getEndTw());

                    if (Math.abs(timeInSmallVehicle - sv.getCustomers().get(k).getDelivery().getArriveTime()) > 0.0001)
                        checkArrive = false;

                    timeInSmallVehicle = Math.max(sv.getCustomers().get(k).getStartTw(), timeInSmallVehicle);
                    timeInSmallVehicle += sv.getCustomers().get(k).getDelivery().serviceDuration;
                }

                costInSmallVehicle += distanceMatrix[sv.getLastCustomer().getId()][ds.getId()];

                totalDistance += costInSmallVehicle;

                if (Math.abs(sv.getCost().getDistance() - costInSmallVehicle) > 0.001)
                    checkSmallCost = false;
                if (Math.abs(sv.getCost().getLoad() - loadInSmallVehicle) > 0.001)
                    checkSmallLoad = false;

                if (!(checkSmallCost && checkBigLoad)) total = false;

                result.append("\n\n check small route " + i + "-" + j + " : "
                        + "\n check small cost = " + costInSmallVehicle + "  " + checkSmallCost
                        + "\n check small demand = " + loadInSmallVehicle + "  " + checkSmallLoad);

                if (j < bv.getDummySatellites().size() - 1) {
                    costInBigVehicle += distanceMatrix[bv.getDummySatellites().get(j).getId()][bv.getDummySatellites().get(j + 1).getId()];
                    timeInBigVehicle += distanceMatrix[bv.getDummySatellites().get(j).getId()][bv.getDummySatellites().get(j + 1).getId()];
                    if (Math.abs(timeInBigVehicle - bv.getDummySatellites().get(j + 1).getDelivery().arriveTime) > 0.0001)
                        checkArrive = false;
                    timeInBigVehicle += bv.getDummySatellites().get(j + 1).getDelivery().serviceDuration;
                }
                loadInBigVehicle += loadInSmallVehicle;
            }

            costInBigVehicle += distanceMatrix[bv.getLastSatellite().getId()][depot.getId()];

            totalDistance += costInBigVehicle;

            if (Math.abs(bv.getCost().getDistance() - costInBigVehicle) > 0.001)
                checkBigCost = false;
            if (Math.abs(bv.getCost().getLoad() - loadInBigVehicle) > 0.001)
                checkBigLoad = false;

            if (!(checkBigCost && checkBigLoad)) total = false;

            result.append("\n\n check big route " + i + ": "
                    + "\n check big cost = " + costInBigVehicle + "  " + checkBigCost
                    + "\n check big demand = " + loadInBigVehicle + "  " + checkBigLoad
            );
        }

        if (Math.abs(totalDistance - solution.getCostForDelivery1E().getDistance() - solution.getCostForDelivery2E().getDistance()) > 0.001) {
            checkTotalCost = false;
        }
        if (Math.abs(twViloationInSmallVehicle - solution.getCostForDelivery2E().twViol) > 0.001) {
            checkTimeWindows2E = false;
        }
        result.append("\n\n check total cost = " + checkTotalCost
                + "\n check time window viol 2E = " + checkTimeWindows2E
                + "\n check arrive time = " + checkArrive);

        return result.toString();
    }


    public void checkModelSolution(Solution solution) {
        CheckModelDeliveryRoute(solution);
        CheckModelPickupRoute(solution);
        solution.addToTotal();
        System.out.println("total distance: " + solution.getTotalCost().distance +
                "\ttotal twViol: " + solution.getTotalCost().twViol +
                "\ttotal loadViol: " + solution.getTotalCost().loadViol);
    }

    private void CheckModelDeliveryRoute(Solution solution) {
        Depot depot = solution.getDepot();
        ArrayList<BigVehicle> vehicles = depot.getDeliveryVehicles();

        double distance1E = 0, distance2E = 0;
        double load1E = 0;
        double twViloationInSmallVehicle = 0;
        double loadViloationInSV = 0;
        double loadViloationInBV = 0;

        for (int i = 0; i < vehicles.size(); i++) {
            BigVehicle bv = vehicles.get(i);

            double costInBigVehicle = 0;
            double timeInBigVehicle = 0;
            int loadInBigVehicle = 0;
            boolean checkSmallCost = true;
            boolean checkSmallLoad = true;

            DummySatellites ds = bv.getDummySatellites().get(0);
            costInBigVehicle += distanceMatrix[depot.getId()][ds.getId()];
            timeInBigVehicle += distanceMatrix[depot.getId()][ds.getId()];
            ds.getDelivery().setArriveTime(timeInBigVehicle);
            SmallVehicle sv = ds.getVehicle();
            for (int k = 0; k < sv.getCustomers().size(); k++) {
                ds.getDelivery().demand += sv.getCustomer(k).getDelivery().demand;
                ds.getPickup().demand += sv.getCustomer(k).getPickup().demand;
            }
            ds.getDelivery().serviceDuration = instance.getPt() * ds.getDelivery().demand;
            ds.getPickup().serviceDuration = instance.getPt() * ds.getPickup().demand;
            timeInBigVehicle += ds.getDelivery().serviceDuration;

            for (int j = 0; j < bv.getDummySatellites().size(); j++) {

                ds = bv.getDummySatellites().get(j);

                sv = ds.getVehicle();
                double timeInSmallVehicle = timeInBigVehicle + distanceMatrix[ds.getId()][sv.getCustomers().get(0).getId()];
                double costInSmallVehicle = distanceMatrix[ds.getId()][sv.getCustomers().get(0).getId()];
                double loadInSmallVehicle = sv.getCustomers().get(0).getDelivery().getDemand();

                sv.getCustomers().get(0).getDelivery().arriveTime = timeInSmallVehicle;

                timeInSmallVehicle = Math.max(sv.getCustomers().get(0).getStartTw(), timeInSmallVehicle);
                twViloationInSmallVehicle += Math.max(0, timeInSmallVehicle - sv.getCustomers().get(0).getEndTw());
                timeInSmallVehicle += sv.getCustomers().get(0).getDelivery().serviceDuration;

                for (int k = 1; k < sv.getCustomers().size(); k++) {
                    timeInSmallVehicle += distanceMatrix[sv.getCustomers().get(k - 1).getId()][sv.getCustomers().get(k).getId()];
                    costInSmallVehicle += distanceMatrix[sv.getCustomers().get(k - 1).getId()][sv.getCustomers().get(k).getId()];
                    loadInSmallVehicle += sv.getCustomers().get(k).getDelivery().getDemand();
                    twViloationInSmallVehicle += Math.max(0, timeInSmallVehicle - sv.getCustomers().get(k).getEndTw());

                    sv.getCustomers().get(k).getDelivery().arriveTime = timeInSmallVehicle;

                    timeInSmallVehicle = Math.max(sv.getCustomers().get(k).getStartTw(), timeInSmallVehicle);
                    timeInSmallVehicle += sv.getCustomers().get(k).getDelivery().serviceDuration;
                }

                costInSmallVehicle += distanceMatrix[sv.getLastCustomer().getId()][ds.getId()];

                distance2E += costInSmallVehicle;

                sv.getCost().distance = costInSmallVehicle;
                sv.getCost().load = loadInSmallVehicle;
                loadViloationInSV += Math.max(0.0, sv.getCost().load - (double)sv.getCapacity());

                if (j < bv.getDummySatellites().size() - 1) {
                    costInBigVehicle += distanceMatrix[bv.getDummySatellites().get(j).getId()][bv.getDummySatellites().get(j + 1).getId()];
                    timeInBigVehicle += distanceMatrix[bv.getDummySatellites().get(j).getId()][bv.getDummySatellites().get(j + 1).getId()];
                    bv.getDummySatellites().get(j + 1).getDelivery().arriveTime = timeInBigVehicle;

                    sv = bv.getDummySatellites().get(j + 1).getVehicle();
                    for (int k = 0; k < sv.getCustomers().size(); k++) {
                        bv.getDummySatellites().get(j + 1).getDelivery().demand += sv.getCustomer(k).getDelivery().demand;
                        bv.getDummySatellites().get(j + 1).getPickup().demand += sv.getCustomer(k).getPickup().demand;
                    }
                    bv.getDummySatellites().get(j + 1).getDelivery().serviceDuration = instance.getPt() * bv.getDummySatellites().get(j + 1).getDelivery().demand;

                    timeInBigVehicle += bv.getDummySatellites().get(j + 1).getDelivery().serviceDuration;
                }
                loadInBigVehicle += loadInSmallVehicle;
            }

            costInBigVehicle += distanceMatrix[bv.getLastSatellite().getId()][depot.getId()];

            distance1E += costInBigVehicle;
            load1E += loadInBigVehicle;

            bv.getCost().distance = costInBigVehicle;
            bv.getCost().load = loadInBigVehicle;
            loadViloationInBV += Math.max(0.0, bv.getCost().load - (double)bv.getCapacity());
        }

        solution.getCostForDelivery1E().distance = distance1E;
        solution.getCostForDelivery1E().load = load1E;
        solution.getCostForDelivery1E().loadViol = loadViloationInBV;
        solution.getCostForDelivery2E().distance = distance2E;
        solution.getCostForDelivery2E().loadViol = loadViloationInSV;
        solution.getCostForDelivery2E().twViol = twViloationInSmallVehicle;
    }

    private void CheckModelPickupRoute(Solution solution) {
        Depot depot = solution.getDepot();
        ArrayList<BigVehicle> vehicles = depot.getPickupVehicles();

        double totalDistance = 0;
        double loadViolInBV = 0, load1E = 0;

        for (int i = 0; i < vehicles.size(); i++) {
            BigVehicle bv = vehicles.get(i);

            double costInBigVehicle = 0;
            int loadInBigVehicle = 0;

            DummySatellites ds = bv.getDummySatellites().get(0);
            costInBigVehicle += distanceMatrix[depot.getId()][ds.getId()];
            loadInBigVehicle += ds.getPickup().demand;

            for (int j = 1; j < bv.getDummySatellites().size(); j++) {
                ds = bv.getDummySatellites().get(j - 1);
                costInBigVehicle += distanceMatrix[ds.getId()][bv.getDummySatellites().get(j).getId()];
                loadInBigVehicle += bv.getDummySatellites().get(j).getPickup().demand;
            }

            costInBigVehicle += distanceMatrix[bv.getLastSatellite().getId()][depot.getId()];
            totalDistance += costInBigVehicle;
            bv.getCost().distance = costInBigVehicle;
            bv.getCost().load = loadInBigVehicle;
            load1E += loadInBigVehicle;
            loadViolInBV += Math.max(0, bv.getCost().load - bv.getCapacity());
        }

        solution.getCostForPickup1E().distance = totalDistance;
        solution.getCostForPickup1E().loadViol = loadViolInBV;
        solution.getCostForPickup1E().load = load1E;
    }
}