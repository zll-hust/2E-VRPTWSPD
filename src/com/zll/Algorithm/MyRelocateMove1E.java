package com.zll.Algorithm;

import com.zll.twoEVRP.*;

public class MyRelocateMove1E extends MyMove {
    private int deleteRouteIndex;
    private int insertRouteIndex;

    private int deletePositionIndex;
    private int insertPositionIndex;

    private DummySatellites satellite;

    private Customer beforeDeleteCustomer;
    private Customer afterDeleteCustomer;
    private Customer beforeInsertCustomer;
    private Customer afterInsertCustomer;

    public MyRelocateMove1E(MyRelocateMove1E move) {
        this.instance = move.instance;
        this.deleteRouteIndex = move.deleteRouteIndex;
        this.insertRouteIndex = move.insertRouteIndex;
        this.deletePositionIndex = move.deletePositionIndex;
        this.insertPositionIndex = move.insertPositionIndex;

        arcs = new TabuArc[3];
        arcs[0] = new TabuArc(move.getArcs()[0]);
        arcs[1] = new TabuArc(move.getArcs()[1]);
        arcs[2] = new TabuArc(move.getArcs()[2]);
    }

    public MyRelocateMove1E(Instance instance, int deleteRouteIndex, int deletePositionIndex,
                            int insertRouteIndex, int insertPositionIndex, Solution sol) {
        this.instance = instance;
        this.deleteRouteIndex = deleteRouteIndex;
        this.insertRouteIndex = insertRouteIndex;
        this.deletePositionIndex = deletePositionIndex;
        this.insertPositionIndex = insertPositionIndex;

        getInfo(sol);

        // get tabu arc
        arcs = new TabuArc[3];
        arcs[0] = new TabuArc(beforeDeleteCustomer.getId(), beforeDeleteCustomer.getId2(), satellite.getId(), satellite.getId2());
        arcs[1] = new TabuArc(satellite.getId(), satellite.getId2(), afterDeleteCustomer.getId(), afterDeleteCustomer.getId2());
        arcs[2] = new TabuArc(beforeInsertCustomer.getId(), beforeInsertCustomer.getId2(), afterInsertCustomer.getId(), afterInsertCustomer.getId2());
    }

    protected void getInfo(Solution sol) {
        // get depot
        this.depot = sol.getDepot();

        // get route belonged
        BigVehicle deleteRoute = sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex);
        BigVehicle insertRoute = sol.getDepot().getDeliveryVehicles().get(insertRouteIndex);

        // get before and after satellites
        if (deletePositionIndex == 0) {
            beforeDeleteCustomer = depot;
        } else {
            beforeDeleteCustomer = deleteRoute.getSatellite(deletePositionIndex - 1);
        }
        if (deletePositionIndex == deleteRoute.getSatelliteLength() - 1) {
            afterDeleteCustomer = depot;
        } else {
            afterDeleteCustomer = deleteRoute.getSatellite(deletePositionIndex + 1);
        }

        if (insertPositionIndex == 0) {
            beforeInsertCustomer = depot;
        } else {
            beforeInsertCustomer = insertRoute.getSatellite(insertPositionIndex - 1);
        }
        if (insertPositionIndex == insertRoute.getSatelliteLength()) {
            afterInsertCustomer = depot;
        } else {
            afterInsertCustomer = insertRoute.getSatellite(insertPositionIndex);
        }

        this.satellite = deleteRoute.getSatellite(deletePositionIndex);
    }


    public boolean evaluteTwViol(Solution sol) {
        getInfo(sol);

        // get route
        BigVehicle deleteRoute = depot.getDeliveryVehicles().get(deleteRouteIndex);
        BigVehicle insertRoute = depot.getDeliveryVehicles().get(insertRouteIndex);

        double twViol = 0;
        double newTwViol = 0;

        // second satellite to first route
        double variation = -instance.getTravelTime(beforeDeleteCustomer.getId(), satellite.getId())
                - instance.getTravelTime(satellite.getId(), afterDeleteCustomer.getId())
                - satellite.getDelivery().serviceDuration
                + instance.getTravelTime(beforeDeleteCustomer.getId(), afterDeleteCustomer.getId());

        for (int i = deletePositionIndex; i < deleteRoute.getSatelliteLength(); i++) {
            twViol += deleteRoute.getSatellite(i).getTwViol();
            if (i > deletePositionIndex) {
                newTwViol += Math.max(0, deleteRoute.getSatellite(i).getDelivery().getArriveTime() + variation - deleteRoute.getSatellite(i).getEndTw());
            }
        }

        // first satellite to second route
        variation = -instance.getTravelTime(beforeInsertCustomer.getId(), afterInsertCustomer.getId())
                + instance.getTravelTime(beforeInsertCustomer.getId(), satellite.getId())
                + instance.getTravelTime(satellite.getId(), afterInsertCustomer.getId())
                + satellite.getDelivery().getServiceDuration();

        newTwViol += Math.max(0, beforeInsertCustomer.getDelivery().arriveTime + beforeInsertCustomer.getDelivery().serviceDuration + instance.getTravelTime(beforeInsertCustomer.getId(), satellite.getId()) - satellite.getEndTw());
        for (int i = insertPositionIndex; i < insertRoute.getSatelliteLength(); i++) {
            twViol += insertRoute.getSatellite(i).getTwViol();
            newTwViol += Math.max(0, insertRoute.getSatellite(i).getDelivery().getArriveTime() + variation - insertRoute.getSatellite(i).getEndTw());
        }


        if (newTwViol <= twViol)
            return true;
        else
            return false;
    }

    public double evaluate(Solution sol) {
        getInfo(sol);
        Cost newTotalCost = new Cost(sol.getTotalCost());

        BigVehicle deleteRoute = sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex);
        BigVehicle insertRoute = sol.getDepot().getDeliveryVehicles().get(insertRouteIndex);

        Cost newCostDelete1E = evaluateDeleteRoute1E(deleteRoute, newTotalCost);
        Cost newCostInsert1E = evaluateInsertRoute1E(insertRoute, newTotalCost);

        newTotalCost.addCost(newCostDelete1E);
        newTotalCost.addCost(newCostInsert1E);
        newTotalCost.calculateTotal();
        return newTotalCost.getTotal();
    }

    public void operateOn(Solution sol) {
        getInfo(sol);

        // get route
        BigVehicle deleteRoute = sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex);
        BigVehicle insertRoute = sol.getDepot().getDeliveryVehicles().get(insertRouteIndex);

        Cost initialCost1E = add1ECost(deleteRoute, insertRoute);
        Cost initialCost2E = add2ECost(deleteRoute, insertRoute);

        updateDeleteRoute1E(deleteRoute);
        updateInsertRoute1E(insertRoute);

        Cost newCost1E = add1ECost(deleteRoute, insertRoute);
        Cost newCost2E = add2ECost(deleteRoute, insertRoute);

        updateTotalCost(sol, initialCost1E, initialCost2E, newCost1E, newCost2E);
    }

    protected Cost evaluateDeleteRoute1E(BigVehicle bv, Cost totalCost) {
        Cost newCost = new Cost(bv.getCost());
        totalCost.cutCost(bv.getCost());

        // 距离的差值
        double variation = -instance.getTravelTime(beforeDeleteCustomer.getId(), satellite.getId())
                - instance.getTravelTime(satellite.getId(), afterDeleteCustomer.getId())
                + instance.getTravelTime(beforeDeleteCustomer.getId(), afterDeleteCustomer.getId());
        newCost.distance += variation;
        newCost.load -= satellite.getDelivery().getDemand();
        newCost.setLoadViol(Math.max(0, newCost.load - bv.getCapacity()));

        // 时间的差值
        variation -= satellite.getDelivery().getServiceDuration();

        double arriveTime;
        for (int i = deletePositionIndex + 1; i < bv.getSatelliteLength(); i++) {
            arriveTime = bv.getSatellite(i).getDelivery().arriveTime + variation;
            if (arriveTime > bv.getSatellite(i).getEndTw() || bv.getSatellite(i).getDelivery().arriveTime > bv.getSatellite(i).getEndTw()) {
                newCost.addCost(evaluateRoute2E(bv.getSatellite(i), variation));
                totalCost.cutCost(bv.getSatellite(i).getVehicle().getCost());
            } //TODO
        }

        if (bv.getSatelliteLength() == 1) {
            newCost.initialize();
        }

        return newCost;
    }

    // 更新所有节点的服务时间
    private void updateDeleteRoute1E(BigVehicle bv) {
        Cost varCost = bv.getCost();
        // 距离的差值
        double variation = -instance.getTravelTime(beforeDeleteCustomer.getId(), satellite.getId())
                - instance.getTravelTime(satellite.getId(), afterDeleteCustomer.getId())
                + instance.getTravelTime(beforeDeleteCustomer.getId(), afterDeleteCustomer.getId());
        varCost.distance += variation;
        varCost.load -= satellite.getDelivery().getDemand();
        varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));
        // 时间的差值
        variation -= satellite.getDelivery().getServiceDuration();
        for (int i = deletePositionIndex + 1; i < bv.getSatelliteLength(); i++) {
            // 由于没有等待时间，直接加上新的时间
            bv.getSatellite(i).getDelivery().arriveTime += variation;
            // 更新对二级路径的影响
            update2ERoute(bv.getSatellite(i), variation);
            // 更新该路径的时间窗
            double twViol = Math.max(0, bv.getSatellite(i).getDelivery().getArriveTime() - bv.getSatellite(i).getEndTw());
//            varCost.twViol += -bv.getSatellite(i).getTwViol() + twViol;
            bv.getSatellite(i).setTwViol(twViol);
        }
        bv.removeSatellite(deletePositionIndex);
        if (bv.getSatelliteLength() == 0) {
            varCost.initialize();
            depot.getDeliveryVehicles().remove(bv);
        }
    }

    private Cost evaluateInsertRoute1E(BigVehicle bv, Cost totalCost) {
        Cost newCost = new Cost(bv.getCost());
        totalCost.cutCost(bv.getCost());

        // 距离的差值
        double variation = instance.getTravelTime(beforeInsertCustomer.getId(), satellite.getId())
                + instance.getTravelTime(satellite.getId(), afterInsertCustomer.getId())
                - instance.getTravelTime(beforeInsertCustomer.getId(), afterInsertCustomer.getId());
        newCost.distance += variation;
        newCost.load += satellite.getDelivery().getDemand();
        newCost.setLoadViol(Math.max(0, newCost.load - bv.getCapacity()));
        variation += satellite.getDelivery().getServiceDuration();

        double arriveTime;
        for (int i = insertPositionIndex; i < bv.getSatelliteLength(); i++) {
            arriveTime = bv.getSatellite(i).getDelivery().arriveTime + variation;
            if (arriveTime > bv.getSatellite(i).getEndTw() || bv.getSatellite(i).getDelivery().arriveTime > bv.getSatellite(i).getEndTw()) {
                newCost.addCost(evaluateRoute2E(bv.getSatellite(i), variation));
                totalCost.cutCost(bv.getSatellite(i).getVehicle().getCost());
            } // TODO
        }

        // 对ds进行更新
        variation = beforeInsertCustomer.getDelivery().arriveTime + beforeInsertCustomer.getDelivery().getServiceDuration() + instance.getTravelTime(beforeInsertCustomer.getId(), satellite.getId()) - satellite.getDelivery().getArriveTime();
        arriveTime = satellite.getDelivery().arriveTime;
        satellite.getDelivery().arriveTime += variation;
//        if (arriveTime > satellite.getEndTw() || satellite.getDelivery().arriveTime > satellite.getEndTw()) {
            newCost.addCost(evaluateRoute2E(satellite, variation));
            totalCost.cutCost(satellite.getVehicle().getCost());
//        }
        satellite.getDelivery().arriveTime = arriveTime;
        return newCost;
    }

    private void updateInsertRoute1E(BigVehicle bv) {
        Cost varCost = bv.getCost();
        double variation = instance.getTravelTime(beforeInsertCustomer.getId(), satellite.getId())
                + instance.getTravelTime(satellite.getId(), afterInsertCustomer.getId())
                - instance.getTravelTime(beforeInsertCustomer.getId(), afterInsertCustomer.getId());
        varCost.distance += variation;
        varCost.load += satellite.getDelivery().getDemand();
        varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));
        variation += satellite.getDelivery().getServiceDuration();
        for (int i = insertPositionIndex; i < bv.getSatelliteLength(); i++) {
            bv.getSatellite(i).getDelivery().arriveTime += variation;
            update2ERoute(bv.getSatellite(i), variation);
            double twViol = Math.max(0, bv.getSatellite(i).getDelivery().getArriveTime() - bv.getSatellite(i).getEndTw());
//            varCost.twViol += -bv.getSatellite(i).getTwViol() + twViol;
            bv.getSatellite(i).setTwViol(twViol);
        }
        bv.addSatellite(satellite, insertPositionIndex);
        // 对ds进行更新
        variation = beforeInsertCustomer.getDelivery().arriveTime + beforeInsertCustomer.getDelivery().getServiceDuration() + instance.getTravelTime(beforeInsertCustomer.getId(), satellite.getId()) - satellite.getDelivery().getArriveTime();
        satellite.getDelivery().arriveTime += variation;
        double twViol = Math.max(0, satellite.getDelivery().getArriveTime() - satellite.getEndTw());
//        varCost.twViol += twViol;
        satellite.setTwViol(twViol);

        update2ERoute(satellite, variation);
        satellite.setBelongedDeliveryBigVehicle(bv);
    }

    private Cost add2ECost(BigVehicle deleteRoute, BigVehicle insertRoute) {
        Cost varCost = new Cost();
        for (int i = deletePositionIndex; i < deleteRoute.getSatelliteLength(); i++) {
            varCost.waitingTime += deleteRoute.getDummySatellites().get(i).getVehicle().getCost().waitingTime;
            varCost.twViol += deleteRoute.getDummySatellites().get(i).getVehicle().getCost().twViol;
        }
        for (int i = insertPositionIndex; i < insertRoute.getSatelliteLength(); i++) {
            varCost.waitingTime += insertRoute.getDummySatellites().get(i).getVehicle().getCost().waitingTime;
            varCost.twViol += insertRoute.getDummySatellites().get(i).getVehicle().getCost().twViol;
        }

        return varCost;
    }

    private Cost add1ECost(BigVehicle deleteRoute, BigVehicle insertRoute) {
        Cost varCost = new Cost();
        varCost.addCost(deleteRoute.getCost());
        varCost.addCost(insertRoute.getCost());

        return varCost;
    }
}