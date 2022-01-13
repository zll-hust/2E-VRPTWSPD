package com.zll.Algorithm;

import com.zll.twoEVRP.*;
import sun.awt.windows.WPrinterJob;

public class MyExchangeMove1E extends MyMove {
    private DummySatellites firstSatellite;
    private DummySatellites secondSatellite;
    private int firstRouteIndex;
    private int secondRouteIndex;

    private int firstPositionIndex;
    private int secondPositionIndex;

    private Customer beforeFirstSatellite;
    private Customer afterFirstSatellite;
    private Customer beforeSecondSatellite;
    private Customer afterSecondSatellite;

    public MyExchangeMove1E(MyExchangeMove1E move){
        this.instance = move.instance;
        this.firstRouteIndex = move.firstRouteIndex;
        this.secondRouteIndex = move.secondRouteIndex;
        this.firstPositionIndex = move.firstPositionIndex;
        this.secondPositionIndex = move.secondPositionIndex;

        arcs = new TabuArc[4];
        arcs[0] = new TabuArc(move.getArcs()[0]);
        arcs[1] = new TabuArc(move.getArcs()[1]);
        arcs[2] = new TabuArc(move.getArcs()[2]);
        arcs[3] = new TabuArc(move.getArcs()[3]);
    }

    public MyExchangeMove1E(Instance instance, int firstRouteIndex, int firstPositionIndex,
                            int secondRouteIndex, int secondPositionIndex, Solution sol) {
        this.instance = instance;
        this.firstRouteIndex = firstRouteIndex;
        this.secondRouteIndex = secondRouteIndex;
        this.firstPositionIndex = firstPositionIndex;
        this.secondPositionIndex = secondPositionIndex;

        getInfo(sol);

        // get tabu arc
        arcs = new TabuArc[4];
        arcs[0] = new TabuArc(beforeFirstSatellite.getId(), beforeFirstSatellite.getId2(), firstSatellite.getId(), firstSatellite.getId2());
        arcs[1] = new TabuArc(firstSatellite.getId(), firstSatellite.getId2(), afterFirstSatellite.getId(), afterFirstSatellite.getId2());
        arcs[2] = new TabuArc(beforeSecondSatellite.getId(), beforeSecondSatellite.getId2(), secondSatellite.getId(), secondSatellite.getId2());
        arcs[3] = new TabuArc(secondSatellite.getId(), secondSatellite.getId2(), afterSecondSatellite.getId(), afterSecondSatellite.getId2());
    }

    protected void getInfo(Solution sol){
        // get depot
        this.depot = sol.getDepot();

        // get route belonged
        BigVehicle firstRoute = sol.getDepot().getDeliveryVehicles().get(firstRouteIndex);
        firstSatellite = firstRoute.getSatellite(firstPositionIndex);

        BigVehicle secondRoute = sol.getDepot().getDeliveryVehicles().get(secondRouteIndex);
        secondSatellite = secondRoute.getSatellite(secondPositionIndex);

        // get before and after satellites
        if (firstPositionIndex == 0) {
            beforeFirstSatellite = depot;
        } else {
            beforeFirstSatellite = firstRoute.getSatellite(firstPositionIndex - 1);
        }
        if (firstPositionIndex == firstRoute.getSatelliteLength() - 1) {
            afterFirstSatellite = depot;
        } else {
            afterFirstSatellite = firstRoute.getSatellite(firstPositionIndex + 1);
        }

        if (secondPositionIndex == 0) {
            beforeSecondSatellite = depot;
        } else {
            beforeSecondSatellite = secondRoute.getSatellite(secondPositionIndex - 1);
        }
        if (secondPositionIndex == secondRoute.getSatelliteLength() - 1) {
            afterSecondSatellite = depot;
        } else {
            afterSecondSatellite = secondRoute.getSatellite(secondPositionIndex + 1);
        }
    }

    public boolean evaluteTwViol(Solution sol) {
        getInfo(sol);

        // get route
        BigVehicle firstRoute = depot.getDeliveryVehicles().get(firstRouteIndex);
        BigVehicle secondRoute = depot.getDeliveryVehicles().get(secondRouteIndex);

        double twViol = 0;
        double newTwViol = 0;

        // second satellite to first route
        double variation = - instance.getTravelTime(beforeFirstSatellite.getId(), firstSatellite.getId())
                - instance.getTravelTime(firstSatellite.getId(), afterFirstSatellite.getId())
                - firstSatellite.getDelivery().serviceDuration
                + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId())
                + instance.getTravelTime(secondSatellite.getId(), afterFirstSatellite.getId())
                + secondSatellite.getDelivery().serviceDuration;

        for(int i = firstPositionIndex; i < firstRoute.getSatelliteLength(); i++){
            twViol += firstRoute.getSatellite(i).getTwViol();
            if(i == firstPositionIndex){
                newTwViol += Math.max(0, beforeFirstSatellite.getDelivery().arriveTime + beforeFirstSatellite.getDelivery().serviceDuration + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId()) - secondSatellite.getEndTw());
            }else{
                newTwViol += Math.max(0, firstRoute.getSatellite(i).getDelivery().getArriveTime() + variation - firstRoute.getSatellite(i).getEndTw());
            }
        }

        // first satellite to second route
        variation = - instance.getTravelTime(beforeSecondSatellite.getId(), secondSatellite.getId())
                - instance.getTravelTime(secondSatellite.getId(), afterSecondSatellite.getId())
                - secondSatellite.getDelivery().getServiceDuration()
                + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId())
                + instance.getTravelTime(firstSatellite.getId(), afterSecondSatellite.getId())
                + firstSatellite.getDelivery().getServiceDuration();

        for(int i = secondPositionIndex; i < secondRoute.getSatelliteLength(); i++){
            twViol += secondRoute.getSatellite(i).getTwViol();
            if(i == secondPositionIndex){
                newTwViol += Math.max(0, beforeSecondSatellite.getDelivery().arriveTime + beforeSecondSatellite.getDelivery().serviceDuration + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId()) - firstSatellite.getEndTw());
            }else{
                newTwViol += Math.max(0, secondRoute.getSatellite(i).getDelivery().getArriveTime() + variation - secondRoute.getSatellite(i).getEndTw());
            }
        }


        if(newTwViol <= twViol)
            return true;
        else
            return false;
    }

    public double evaluate(Solution sol) {
        getInfo(sol);
        Cost newTotalCost = new Cost(sol.getTotalCost());

        BigVehicle firstRoute = sol.getDepot().getDeliveryVehicles().get(firstRouteIndex);
        BigVehicle secondRoute = sol.getDepot().getDeliveryVehicles().get(secondRouteIndex);

        Cost newCostDelete1E = evaluateFirstRoute1E(firstRoute, newTotalCost);
        Cost newCostInsert1E = evaluateSecondRoute1E(secondRoute, newTotalCost);

        newTotalCost.addCost(newCostDelete1E);
        newTotalCost.addCost(newCostInsert1E);
        newTotalCost.calculateTotal();
        return newTotalCost.getTotal();
    }

    public void operateOn(Solution sol) {
        getInfo(sol);

        // get route
        BigVehicle firstRoute = depot.getDeliveryVehicles().get(firstRouteIndex);
        BigVehicle secondRoute = depot.getDeliveryVehicles().get(secondRouteIndex);

        DummySatellites firstSatellite = firstRoute.getSatellite(firstPositionIndex);
        DummySatellites secondSatellite = secondRoute.getSatellite(secondPositionIndex);

        Cost initialCost1E = add1ECost(firstRoute, secondRoute);
        Cost initialCost2E = add2ECost(firstRoute, secondRoute);

        updateFirstRoute1E(firstRoute);
        updateSecondRoute1E(secondRoute);

        Cost newCost1E = add1ECost(firstRoute, secondRoute);
        Cost newCost2E = add2ECost(firstRoute, secondRoute);

        updateTotalCost(sol, initialCost1E, initialCost2E, newCost1E, newCost2E);
    }

    // second node to first route
    private Cost evaluateFirstRoute1E(BigVehicle bv, Cost totalCost) {
        Cost newCost = new Cost(bv.getCost());
        totalCost.cutCost(bv.getCost());

        double variation = - instance.getTravelTime(beforeFirstSatellite.getId(), firstSatellite.getId())
                - instance.getTravelTime(firstSatellite.getId(), afterFirstSatellite.getId())
                + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId())
                + instance.getTravelTime(secondSatellite.getId(), afterFirstSatellite.getId());
        newCost.distance += variation;
        newCost.load += - firstSatellite.getDelivery().getDemand() + secondSatellite.getDelivery().getDemand();
        newCost.setLoadViol(Math.max(0, newCost.load - bv.getCapacity()));

        // 时间的差值
        variation += - firstSatellite.getDelivery().getServiceDuration() + secondSatellite.getDelivery().getServiceDuration();

        double arriveTime;
        for (int i = firstPositionIndex + 1; i < bv.getSatelliteLength(); i++) {
            arriveTime = bv.getSatellite(i).getDelivery().arriveTime + variation;
            if (arriveTime > bv.getSatellite(i).getEndTw() || bv.getSatellite(i).getDelivery().arriveTime > bv.getSatellite(i).getEndTw()) {
                newCost.addCost(evaluateRoute2E(bv.getSatellite(i), variation));
                totalCost.cutCost(bv.getSatellite(i).getVehicle().getCost());
            } //TODO
        }

        // 对ds进行更新
        variation = beforeFirstSatellite.getDelivery().arriveTime + beforeFirstSatellite.getDelivery().getServiceDuration() + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId()) - secondSatellite.getDelivery().getArriveTime();
        arriveTime = secondSatellite.getDelivery().arriveTime;
        secondSatellite.getDelivery().arriveTime += variation;
        if (arriveTime > secondSatellite.getEndTw() || secondSatellite.getDelivery().arriveTime > secondSatellite.getEndTw()) {
            newCost.addCost(evaluateRoute2E(secondSatellite, variation));
            totalCost.cutCost(secondSatellite.getVehicle().getCost());
        }
        secondSatellite.getDelivery().arriveTime = arriveTime;

        return newCost;
    }

    // second node to first route
    private void updateFirstRoute1E(BigVehicle bv) {
        Cost varCost = bv.getCost();
        double variation = - instance.getTravelTime(beforeFirstSatellite.getId(), firstSatellite.getId())
                - instance.getTravelTime(firstSatellite.getId(), afterFirstSatellite.getId())
                + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId())
                + instance.getTravelTime(secondSatellite.getId(), afterFirstSatellite.getId());
        varCost.distance += variation;
        varCost.load += - firstSatellite.getDelivery().getDemand() + secondSatellite.getDelivery().getDemand();
        varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));
        // 时间的差值
        variation += - firstSatellite.getDelivery().getServiceDuration() + secondSatellite.getDelivery().getServiceDuration();

        for (int i = firstPositionIndex + 1; i < bv.getSatelliteLength(); i++) {
            bv.getSatellite(i).getDelivery().arriveTime += variation;
            update2ERoute(bv.getSatellite(i), variation);
            double twViol = Math.max(0, bv.getSatellite(i).getDelivery().getArriveTime() - bv.getSatellite(i).getEndTw());
//            varCost.twViol += -bv.getSatellite(i).getTwViol() + twViol;
            bv.getSatellite(i).setTwViol(twViol);
        }
        bv.removeSatellite(firstPositionIndex);
        bv.addSatellite(secondSatellite, firstPositionIndex);
        // 对ds进行更新
        variation = beforeFirstSatellite.getDelivery().arriveTime + beforeFirstSatellite.getDelivery().getServiceDuration() + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId()) - secondSatellite.getDelivery().getArriveTime();
        secondSatellite.getDelivery().arriveTime += variation;
        double twViol = Math.max(0, secondSatellite.getDelivery().getArriveTime() - secondSatellite.getEndTw());
//        bv2.getCost().twViol += -secondSatellite.getTwViol();
//        varCost.twViol += twViol;
        secondSatellite.setTwViol(twViol);

        update2ERoute(secondSatellite, variation);
        secondSatellite.setBelongedDeliveryBigVehicle(bv);
    }

    // second node to first route
    private Cost evaluateSecondRoute1E(BigVehicle bv, Cost totalCost) {
        Cost newCost = new Cost(bv.getCost());
        totalCost.cutCost(bv.getCost());

        double variation = - instance.getTravelTime(beforeSecondSatellite.getId(), secondSatellite.getId())
                - instance.getTravelTime(secondSatellite.getId(), afterSecondSatellite.getId())
                + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId())
                + instance.getTravelTime(firstSatellite.getId(), afterSecondSatellite.getId());
        newCost.distance += variation;
        newCost.load += firstSatellite.getDelivery().getDemand() - secondSatellite.getDelivery().getDemand();
        newCost.setLoadViol(Math.max(0, newCost.load - bv.getCapacity()));

        // 时间的差值
        variation += firstSatellite.getDelivery().getServiceDuration() - secondSatellite.getDelivery().getServiceDuration();

        double arriveTime;
        for (int i = secondPositionIndex + 1; i < bv.getSatelliteLength(); i++) {
            arriveTime = bv.getSatellite(i).getDelivery().arriveTime + variation;
            if (arriveTime > bv.getSatellite(i).getEndTw() || bv.getSatellite(i).getDelivery().arriveTime > bv.getSatellite(i).getEndTw()) {
                newCost.addCost(evaluateRoute2E(bv.getSatellite(i), variation));
                totalCost.cutCost(bv.getSatellite(i).getVehicle().getCost());
            } //TODO
        }

        // 对ds进行更新
        variation = beforeSecondSatellite.getDelivery().arriveTime + beforeSecondSatellite.getDelivery().getServiceDuration() + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId()) - firstSatellite.getDelivery().getArriveTime();
        arriveTime = firstSatellite.getDelivery().arriveTime;
        firstSatellite.getDelivery().arriveTime += variation;
        if (arriveTime > firstSatellite.getEndTw() || firstSatellite.getDelivery().arriveTime > firstSatellite.getEndTw()) {
            newCost.addCost(evaluateRoute2E(firstSatellite, variation));
            totalCost.cutCost(firstSatellite.getVehicle().getCost());
        }
        firstSatellite.getDelivery().arriveTime = arriveTime;

        return newCost;
    }

    // second node to first route
    private void updateSecondRoute1E(BigVehicle bv) {
        Cost varCost = bv.getCost();
        double variation = - instance.getTravelTime(beforeSecondSatellite.getId(), secondSatellite.getId())
                - instance.getTravelTime(secondSatellite.getId(), afterSecondSatellite.getId())
                + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId())
                + instance.getTravelTime(firstSatellite.getId(), afterSecondSatellite.getId());
        varCost.distance += variation;
        varCost.load += firstSatellite.getDelivery().getDemand() - secondSatellite.getDelivery().getDemand();
        varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));

        // 时间的差值
        variation += firstSatellite.getDelivery().getServiceDuration() - secondSatellite.getDelivery().getServiceDuration();

        for (int i = secondPositionIndex + 1; i < bv.getSatelliteLength(); i++) {
            bv.getSatellite(i).getDelivery().arriveTime += variation;
            update2ERoute(bv.getSatellite(i), variation);
            double twViol = Math.max(0, bv.getSatellite(i).getDelivery().getArriveTime() - bv.getSatellite(i).getEndTw());
//            varCost.twViol += -bv.getSatellite(i).getTwViol() + twViol;
            bv.getSatellite(i).setTwViol(twViol);
        }
        bv.removeSatellite(secondPositionIndex);
        bv.addSatellite(firstSatellite, secondPositionIndex);
        // 对ds进行更新
        variation = beforeSecondSatellite.getDelivery().arriveTime + beforeSecondSatellite.getDelivery().getServiceDuration() + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId()) - firstSatellite.getDelivery().getArriveTime();
        firstSatellite.getDelivery().arriveTime += variation;
        double twViol = Math.max(0, firstSatellite.getDelivery().getArriveTime() - firstSatellite.getEndTw());
//        bv2.getCost().twViol += -firstSatellite.getTwViol();
//        varCost.twViol += twViol;
        firstSatellite.setTwViol(twViol);

        update2ERoute(firstSatellite, variation);
        firstSatellite.setBelongedDeliveryBigVehicle(bv);
    }

    private Cost add1ECost(BigVehicle firstRoute, BigVehicle secondRoute) {
        Cost varCost = new Cost();
        varCost.addCost(firstRoute.getCost());
        varCost.addCost(secondRoute.getCost());

        return varCost;
    }

    private Cost add2ECost(BigVehicle firstRoute, BigVehicle secondRoute) {
        Cost varCost = new Cost();
        for (int i = firstPositionIndex; i < firstRoute.getSatelliteLength(); i++) {
            varCost.waitingTime += firstRoute.getDummySatellites().get(i).getVehicle().getCost().waitingTime;
            varCost.twViol += firstRoute.getDummySatellites().get(i).getVehicle().getCost().twViol;
        }
        for (int i = secondPositionIndex; i < secondRoute.getSatelliteLength(); i++) {
            varCost.waitingTime += secondRoute.getDummySatellites().get(i).getVehicle().getCost().waitingTime;
            varCost.twViol += secondRoute.getDummySatellites().get(i).getVehicle().getCost().twViol;
        }

        return varCost;
    }
}