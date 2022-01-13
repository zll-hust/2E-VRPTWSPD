package com.zll.Algorithm;

import com.zll.twoEVRP.*;

public class MyExchangeMove3E extends MyMove {
    private int firstRouteIndex;
    private int secondRouteIndex;

    private int firstPositionIndex;
    private int secondPositionIndex;

    private DummySatellites firstSatellite;
    private DummySatellites secondSatellite;

    private Customer beforeFirstSatellite;
    private Customer afterFirstSatellite;
    private Customer beforeSecondSatellite;
    private Customer afterSecondSatellite;

    public MyExchangeMove3E(MyExchangeMove3E move){
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

    public MyExchangeMove3E(Instance instance, int firstRouteIndex, int firstPositionIndex,
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
        // get route belonged
        BigVehicle firstRoute = sol.getDepot().getPickupVehicles().get(firstRouteIndex);
        firstSatellite = firstRoute.getSatellite(firstPositionIndex);

        BigVehicle secondRoute = sol.getDepot().getPickupVehicles().get(secondRouteIndex);
        secondSatellite = secondRoute.getSatellite(secondPositionIndex);

        this.depot = sol.getDepot();

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


    public double evaluate(Solution sol) {
        Cost cost = new Cost(sol.getTotalCost());
        getInfo(sol);
        BigVehicle firstRoute = sol.getDepot().getPickupVehicles().get(firstRouteIndex);
        BigVehicle secondRoute = sol.getDepot().getPickupVehicles().get(secondRouteIndex);

        cost.cutCost(firstRoute.getCost());
        cost.cutCost(secondRoute.getCost());

        Cost firstRouteCost = new Cost(firstRoute.getCost());
        firstRouteCost.distance +=
                -instance.getTravelTime(beforeFirstSatellite.getId(), firstSatellite.getId())
                        - instance.getTravelTime(firstSatellite.getId(), afterFirstSatellite.getId())
                        + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId())
                        + instance.getTravelTime(secondSatellite.getId(), afterFirstSatellite.getId());
        firstRouteCost.load += -firstSatellite.getPickup().getDemand() + secondSatellite.getPickup().demand;
        firstRouteCost.setLoadViol(Math.max(0, firstRouteCost.load - firstRoute.getCapacity()));


        Cost secondRouteCost = new Cost(secondRoute.getCost());
        secondRouteCost.distance +=
                -instance.getTravelTime(beforeSecondSatellite.getId(), secondSatellite.getId())
                        - instance.getTravelTime(secondSatellite.getId(), afterSecondSatellite.getId())
                        + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId())
                        + instance.getTravelTime(firstSatellite.getId(), afterSecondSatellite.getId());
        secondRouteCost.load += firstSatellite.getPickup().getDemand() - secondSatellite.getPickup().demand;
        secondRouteCost.setLoadViol(Math.max(0, secondRouteCost.load - secondRoute.getCapacity()));

        cost.addCost(firstRouteCost);
        cost.addCost(secondRouteCost);
        cost.calculateTotal();

        return cost.total;
    }

    public void operateOn(Solution sol) {
        // 更新引用
        getInfo(sol);

        // get route
        BigVehicle firstRoute = sol.getDepot().getPickupVehicles().get(firstRouteIndex);
        BigVehicle secondRoute = sol.getDepot().getPickupVehicles().get(secondRouteIndex);

        Cost iniCost = add3ECost(firstRoute, secondRoute);

        // evalute 3e route(pickup)
        evaluateExchangePickRoute(firstRoute, secondSatellite, firstPositionIndex, 0);
        evaluateExchangePickRoute(secondRoute, firstSatellite, secondPositionIndex, 1);

        Cost newCost = add3ECost(firstRoute, secondRoute);

        updateTotalCost(sol, iniCost, newCost);
    }

    protected void evaluateExchangePickRoute(BigVehicle bv, DummySatellites ds, int position, int type) {
        Cost varCost = bv.getCost();

        if (type == 0) { // first route
            varCost.distance +=
                    -instance.getTravelTime(beforeFirstSatellite.getId(), firstSatellite.getId())
                            - instance.getTravelTime(firstSatellite.getId(), afterFirstSatellite.getId())
                            + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId())
                            + instance.getTravelTime(secondSatellite.getId(), afterFirstSatellite.getId());
            varCost.load += -firstSatellite.getPickup().getDemand() + secondSatellite.getPickup().demand;
        } else if (type == 1) { // second route
            varCost.distance +=
                    -instance.getTravelTime(beforeSecondSatellite.getId(), secondSatellite.getId())
                            - instance.getTravelTime(secondSatellite.getId(), afterSecondSatellite.getId())
                            + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId())
                            + instance.getTravelTime(firstSatellite.getId(), afterSecondSatellite.getId());
            varCost.load += firstSatellite.getPickup().getDemand() - secondSatellite.getPickup().demand;
        }
        bv.removeSatellite(position);
        bv.addSatellite(ds, position);
        ds.setBelongedPickupBigVehicle(bv);

        varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));
    }

    private Cost add3ECost(BigVehicle secondRoute, BigVehicle firstRoute) {
        Cost varCost = new Cost();
        varCost.addCost(secondRoute.getCost());
        varCost.addCost(firstRoute.getCost());

        return varCost;
    }
}
