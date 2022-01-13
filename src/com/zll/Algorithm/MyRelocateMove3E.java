package com.zll.Algorithm;

import com.zll.twoEVRP.*;

public class MyRelocateMove3E extends MyMove {
    private int deleteRouteIndex;
    private int insertRouteIndex;

    private int deletePositionIndex;
    private int insertPositionIndex;

    private DummySatellites satellite;

    private Customer beforeDeleteCustomer;
    private Customer afterDeleteCustomer;
    private Customer beforeInsertCustomer;
    private Customer afterInsertCustomer;

    public MyRelocateMove3E(MyRelocateMove3E move) {
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

    public MyRelocateMove3E(Instance instance, int deleteRouteIndex, int deletePositionIndex,
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
        this.depot = sol.getDepot();

        // get route belonged
        BigVehicle deleteRoute = sol.getDepot().getPickupVehicles().get(deleteRouteIndex);
        BigVehicle insertRoute = sol.getDepot().getPickupVehicles().get(insertRouteIndex);

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

    public double evaluate(Solution sol) {
        Cost cost = new Cost(sol.getTotalCost());
        getInfo(sol);
        BigVehicle deleteRoute = sol.getDepot().getPickupVehicles().get(deleteRouteIndex);
        BigVehicle insertRoute = sol.getDepot().getPickupVehicles().get(insertRouteIndex);

        cost.cutCost(insertRoute.getCost());
        cost.cutCost(deleteRoute.getCost());

        Cost insertRouteCost = new Cost(insertRoute.getCost());
        insertRouteCost.load += satellite.getPickup().getDemand();
        insertRouteCost.setLoadViol(Math.max(0, insertRouteCost.load - insertRoute.getCapacity()));
        insertRouteCost.distance += instance.getTravelTime(beforeInsertCustomer.getId(), satellite.getId())
                + instance.getTravelTime(satellite.getId(), afterInsertCustomer.getId())
                - instance.getTravelTime(beforeInsertCustomer.getId(), afterInsertCustomer.getId());

        Cost deleteRouteCost = new Cost(deleteRoute.getCost());
        if (deleteRoute.getSatelliteLength() == 1) {
            deleteRouteCost.initialize();
        } else {
            deleteRouteCost.load -= satellite.getPickup().getDemand();
            deleteRouteCost.setLoadViol(Math.max(0, deleteRouteCost.load - deleteRoute.getCapacity()));
            deleteRouteCost.distance += -instance.getTravelTime(beforeDeleteCustomer.getId(), satellite.getId())
                    - instance.getTravelTime(satellite.getId(), afterDeleteCustomer.getId())
                    + instance.getTravelTime(beforeDeleteCustomer.getId(), afterDeleteCustomer.getId());
        }

        cost.addCost(insertRouteCost);
        cost.addCost(deleteRouteCost);
        cost.calculateTotal();

        return cost.total;
    }

    public void operateOn(Solution sol) {
        // 更新引用
        getInfo(sol);

        // get route
        BigVehicle deleteRoute = sol.getDepot().getPickupVehicles().get(deleteRouteIndex);
        BigVehicle insertRoute = sol.getDepot().getPickupVehicles().get(insertRouteIndex);

        Cost iniCost = add3ECost(insertRoute, deleteRoute);

        // evalute 3e route(pickup)
        updateDeletePickRoute(deleteRoute, satellite, deletePositionIndex);
        updateInsertPickRoute(insertRoute, satellite, insertPositionIndex);

        Cost newCost = add3ECost(insertRoute, deleteRoute);

        updateTotalCost(sol, iniCost, newCost);
    }

    protected void updateInsertPickRoute(BigVehicle bv, DummySatellites ds, int position) {
        Cost varCost = bv.getCost();
        varCost.load += ds.getPickup().getDemand();
        varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));
        varCost.distance += instance.getTravelTime(beforeInsertCustomer.getId(), ds.getId())
                + instance.getTravelTime(ds.getId(), afterInsertCustomer.getId())
                - instance.getTravelTime(beforeInsertCustomer.getId(), afterInsertCustomer.getId());
        bv.addSatellite(ds, position);
        ds.setBelongedPickupBigVehicle(bv);
    }

    protected void updateDeletePickRoute(BigVehicle bv, DummySatellites ds, int position) {
        Cost varCost = bv.getCost();
        // 只有一个点，去掉整条路径
        if (bv.getSatelliteLength() == 1) {
            varCost.initialize();
            depot.getPickupVehicles().remove(bv);
        } else {
            // 多个点
            varCost.load -= ds.getPickup().getDemand();
            varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));
            varCost.distance += -instance.getTravelTime(beforeDeleteCustomer.getId(), ds.getId())
                    - instance.getTravelTime(ds.getId(), afterDeleteCustomer.getId())
                    + instance.getTravelTime(beforeDeleteCustomer.getId(), afterDeleteCustomer.getId());
            bv.removeSatellite(position);
        }
    }

    private Cost add3ECost(BigVehicle insertRoute, BigVehicle deleteRoute) {
        Cost varCost = new Cost();
        varCost.addCost(insertRoute.getCost());
        varCost.addCost(deleteRoute.getCost());

        return varCost;
    }
}
