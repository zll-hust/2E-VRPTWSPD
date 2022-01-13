package com.zll.Algorithm;

import com.zll.twoEVRP.*;

public class MyRelocateMove2E extends MyMove {
    private Customer customer;
    private int[] deleteRouteIndex;
    private int[] insertRouteIndex;

    private int deletePositionIndex;
    private int insertPositionIndex;

    private Customer beforeDeleteCustomer;
    private Customer afterDeleteCustomer;
    private Customer beforeInsertCustomer;
    private Customer afterInsertCustomer;

    private Customer beforeDeleteSatellite;
    private Customer afterDeleteSatellite;
    private Customer beforeInsertSatellite;
    private Customer afterInsertSatellite;


    public MyRelocateMove2E(MyRelocateMove2E move) {
        this.instance = move.instance;
        this.deleteRouteIndex = new int[]{move.deleteRouteIndex[0], move.deleteRouteIndex[1]};
        this.insertRouteIndex = new int[]{move.insertRouteIndex[0], move.insertRouteIndex[1]};
        this.deletePositionIndex = move.deletePositionIndex;
        this.insertPositionIndex = move.insertPositionIndex;

        arcs = new TabuArc[3];
        arcs[0] = new TabuArc(move.getArcs()[0]);
        arcs[1] = new TabuArc(move.getArcs()[1]);
        arcs[2] = new TabuArc(move.getArcs()[2]);
    }

    public MyRelocateMove2E(Instance instance, int[] deleteRouteIndex, int deletePositionIndex,
                            int[] insertRouteIndex, int insertPositionIndex, Solution sol) {
        this.instance = instance;
        this.deleteRouteIndex = deleteRouteIndex;
        this.insertRouteIndex = insertRouteIndex;
        this.deletePositionIndex = deletePositionIndex;
        this.insertPositionIndex = insertPositionIndex;

        getInfo(sol);

        // get tabu arc
        arcs = new TabuArc[3];
        arcs[0] = new TabuArc(beforeDeleteCustomer.getId(), beforeDeleteCustomer.getId2(), customer.getId(), customer.getId2());
        arcs[1] = new TabuArc(customer.getId(), customer.getId2(), afterDeleteCustomer.getId(), afterDeleteCustomer.getId2());
        arcs[2] = new TabuArc(beforeInsertCustomer.getId(), beforeInsertCustomer.getId2(), afterInsertCustomer.getId(), afterInsertCustomer.getId2());
    }

    protected void getInfo(Solution sol) {
        // get depot
        this.depot = sol.getDepot();

        // get route belonged
        DummySatellites deleteSatellite = sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex[0])
                .getSatellite(deleteRouteIndex[1]);
        SmallVehicle deleteRoute = deleteSatellite.getVehicle();

        DummySatellites insertSatellite = sol.getDepot().getDeliveryVehicles().get(insertRouteIndex[0])
                .getSatellite(insertRouteIndex[1]);
        SmallVehicle insertRoute = insertSatellite.getVehicle();

        // get before and after satellites
        if (deletePositionIndex == 0) {
            beforeDeleteCustomer = deleteSatellite;
        } else {
            beforeDeleteCustomer = deleteRoute.getCustomers().get(deletePositionIndex - 1);
        }
        if (deletePositionIndex == deleteRoute.getCustomers().size() - 1) {
            afterDeleteCustomer = deleteSatellite;
        } else {
            afterDeleteCustomer = deleteRoute.getCustomers().get(deletePositionIndex + 1);
        }

        if (insertPositionIndex == 0) {
            beforeInsertCustomer = insertSatellite;
        } else {
            beforeInsertCustomer = insertRoute.getCustomers().get(insertPositionIndex - 1);
        }
        if (insertPositionIndex == insertRoute.getCustomers().size()) {
            afterInsertCustomer = insertSatellite;
        } else {
            afterInsertCustomer = insertRoute.getCustomers().get(insertPositionIndex);
        }

        // get satellite
        if (insertRouteIndex[1] == 0) {
            beforeInsertSatellite = depot;
        } else {
            beforeInsertSatellite = sol.getDepot().getDeliveryVehicles().get(insertRouteIndex[0])
                    .getSatellite(insertRouteIndex[1] - 1);
        }
        if (insertRouteIndex[1] + 1 == sol.getDepot().getDeliveryVehicles().get(insertRouteIndex[0]).getSatelliteLength()) {
            afterInsertSatellite = depot;
        } else {
            afterInsertSatellite = sol.getDepot().getDeliveryVehicles().get(insertRouteIndex[0])
                    .getSatellite(insertRouteIndex[1] + 1);
        }

        if (deleteRouteIndex[1] == 0) {
            beforeDeleteSatellite = depot;
        } else {
            beforeDeleteSatellite = sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex[0])
                    .getSatellite(deleteRouteIndex[1] - 1);
        }
        if (deleteRouteIndex[1] + 1 == sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex[0]).getSatelliteLength()) {
            afterDeleteSatellite = depot;
        } else {
            afterDeleteSatellite = sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex[0])
                    .getSatellite(deleteRouteIndex[1] + 1);
        }

        this.customer = deleteRoute.getCustomer(deletePositionIndex);
    }

    public double evaluate(Solution sol) {
        getInfo(sol);
        Cost newTotalCost = new Cost(sol.getTotalCost());

        // get route
        BigVehicle deleteRoute1E = sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex[0]);
        BigVehicle insertRoute1E = sol.getDepot().getDeliveryVehicles().get(insertRouteIndex[0]);

        DummySatellites deleteSatellite = deleteRoute1E.getSatellite(deleteRouteIndex[1]);
        DummySatellites insertSatellite = insertRoute1E.getSatellite(insertRouteIndex[1]);

        SmallVehicle deleteRoute2E = deleteSatellite.getVehicle();
        SmallVehicle insertRoute2E = insertSatellite.getVehicle();

        BigVehicle deleteRoute3E = deleteSatellite.getBelongedPickupBigVehicle();
        BigVehicle insertRoute3E = insertSatellite.getBelongedPickupBigVehicle();

        Cost newCostDelete2E = evaluateDeleteRoute2E(deleteSatellite, deleteRoute2E, newTotalCost);
        Cost newCostInsert2E = evaluateInsertRoute2E(insertSatellite, insertRoute2E, newTotalCost);

        double newDelete1EDemand = deleteSatellite.getDelivery().getDemand() - customer.getDelivery().demand;
        double newInsert1EDemand = insertSatellite.getDelivery().getDemand() + customer.getDelivery().demand;
        Cost newCostDelete1E = evaluateRoute1E(deleteRoute1E, deleteSatellite, beforeDeleteSatellite, afterDeleteSatellite, deleteRouteIndex[1], newDelete1EDemand, newTotalCost);
        Cost newCostInsert1E = evaluateRoute1E(insertRoute1E, insertSatellite, beforeInsertSatellite, afterInsertSatellite, insertRouteIndex[1], newInsert1EDemand, newTotalCost);

        double newDelete3EDemand = deleteSatellite.getPickup().getDemand() - customer.getPickup().demand;
        double newInsert3EDemand = insertSatellite.getPickup().getDemand() + customer.getPickup().demand;
        if (deleteRoute3E != insertRoute3E) {
            Cost newCostDelete3E = evaluatePickRoute(deleteRoute3E, deleteSatellite, newDelete3EDemand, newTotalCost);
            Cost newCostInsert3E = evaluatePickRoute(insertRoute3E, insertSatellite, newInsert3EDemand, newTotalCost);
            newTotalCost.addCost(newCostDelete3E);
            newTotalCost.addCost(newCostInsert3E);
        } else {
            Cost newCost3E = evaluateIntraPickRoute(deleteRoute3E, deleteSatellite, insertSatellite, newDelete3EDemand, newInsert3EDemand, newTotalCost);
            newTotalCost.addCost(newCost3E);
        }


        newTotalCost.addCost(newCostDelete1E);
        newTotalCost.addCost(newCostInsert1E);
        newTotalCost.addCost(newCostDelete2E);
        newTotalCost.addCost(newCostInsert2E);
        newTotalCost.calculateTotal();
        return newTotalCost.total;
    }

    /*
     *  operate the swap move in @param Solution sol
     */
    public void operateOn(Solution sol) {
        getInfo(sol);

        // get route
        BigVehicle deleteRoute1E = sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex[0]);
        BigVehicle insertRoute1E = sol.getDepot().getDeliveryVehicles().get(insertRouteIndex[0]);

        DummySatellites deleteSatellite = deleteRoute1E.getSatellite(deleteRouteIndex[1]);
        DummySatellites insertSatellite = insertRoute1E.getSatellite(insertRouteIndex[1]);
        Customer beforeDeleteSatellite, afterDeleteSatellite;
        if (deleteRouteIndex[1] == 0) {
            beforeDeleteSatellite = depot;
        } else {
            beforeDeleteSatellite = deleteRoute1E.getSatellite(deleteRouteIndex[1] - 1);
        }
        if (deleteRouteIndex[1] == deleteRoute1E.getSatelliteLength() - 1) {
            afterDeleteSatellite = depot;
        } else {
            afterDeleteSatellite = deleteRoute1E.getSatellite(deleteRouteIndex[1] + 1);
        }

        SmallVehicle deleteRoute2E = deleteSatellite.getVehicle();
        SmallVehicle insertRoute2E = insertSatellite.getVehicle();

        double[] deleteSatelliteInfo = deleteSatellite.getOldInfo();
        double[] insertSatelliteInfo = insertSatellite.getOldInfo();

        // get 3e route information
        double deleteSatelliteOldPickupDemand = deleteSatellite.getPickup().getDemand();
        double insertSatelliteOldPickupDemand = insertSatellite.getPickup().getDemand();
        BigVehicle deleteRoute3E = deleteSatellite.getBelongedPickupBigVehicle();
        BigVehicle insertRoute3E = insertSatellite.getBelongedPickupBigVehicle();

        Cost old1ECost = add1EOr3ECost(insertRoute1E, deleteRoute1E);
        Cost old2ECost = add2ECost(insertRoute1E, deleteRoute1E, deleteSatellite, insertSatellite);
        Cost old3ECost = add1EOr3ECost(insertRoute3E, deleteRoute3E);

        // 更新删除/增加客户点后的卫星服务量/服务时间
        updateSatelliteServiceTime(deleteSatellite, customer, 0);
        updateSatelliteServiceTime(insertSatellite, customer, 1);

        // evaluate 2e insert route
        updateDeleteRoute2E(deleteSatellite, deleteRoute2E, customer, deletePositionIndex);
        updateInsertRoute2E(insertSatellite, insertRoute2E, customer, insertPositionIndex);

        // update time windows on satellite
        updateSatelliteEndTW(deleteSatellite);
        updateSatelliteEndTW(insertSatellite);

        // evaluate 1e route(delivery)
        updateRoute1E(deleteRoute1E, deleteSatellite, deleteRouteIndex[1], deleteSatelliteInfo, beforeDeleteSatellite, afterDeleteSatellite);
        updateRoute1E(insertRoute1E, insertSatellite, insertRouteIndex[1], insertSatelliteInfo);

        // evalute 3e route(pickup)
        updatePickRoute(deleteRoute3E, deleteSatellite, deleteSatelliteOldPickupDemand);
        updatePickRoute(insertRoute3E, insertSatellite, insertSatelliteOldPickupDemand);

        Cost new1ECost = add1EOr3ECost(insertRoute1E, deleteRoute1E);
        Cost new2ECost = add2ECost(insertRoute1E, deleteRoute1E, deleteSatellite, insertSatellite);
        Cost new3ECost = add1EOr3ECost(insertRoute3E, deleteRoute3E);

        updateTotalCost(sol, old1ECost, old2ECost, old3ECost, new1ECost, new2ECost, new3ECost);
    }

    public boolean checkTwViol(Solution sol){
        BigVehicle deleteRoute1E = sol.getDepot().getDeliveryVehicles().get(deleteRouteIndex[0]);
        BigVehicle insertRoute1E = sol.getDepot().getDeliveryVehicles().get(insertRouteIndex[0]);

        double deleteRouteTwViol = 0;
        for(int i = deleteRouteIndex[0]; i < deleteRoute1E.getSatelliteLength(); i++){
            deleteRouteTwViol += deleteRoute1E.getSatellite(i).getTwViol();
        }
        if(deleteRouteTwViol == 0){
            DummySatellites insertSatellite = insertRoute1E.getSatellite(insertRouteIndex[1]);
            SmallVehicle insertRoute2E = insertSatellite.getVehicle();
            if(insertRoute2E.getCost().getTwViol() > 0){
                return false;
            }
        }

        return true;
    }

    private void updateSatelliteServiceTime(DummySatellites ds, Customer c, int type) {
        if (type == 0) { // delete
            ds.getDelivery().demand -= c.getDelivery().getDemand();
            ds.getDelivery().serviceDuration = instance.getPt() * ds.getDelivery().getDemand();
            ds.getPickup().demand -= c.getPickup().getDemand();
        } else if (type == 1) { // insert
            ds.getDelivery().demand += c.getDelivery().getDemand();
            ds.getDelivery().serviceDuration = instance.getPt() * ds.getDelivery().getDemand();
            ds.getPickup().demand += c.getPickup().getDemand();
        }
    }

    /*
     *   the method will update the change caused by new satellite service time and new customer
     */
    public void updateInsertRoute2E(DummySatellites ds, SmallVehicle sv, Customer customer, int position) {
        sv.addCustomer(customer, position);
        sv.getCost().twViol = 0;
        sv.getCost().waitingTime = 0;
        sv.getCost().load += customer.getDelivery().demand;
        sv.getCost().distance += instance.getTravelTime(beforeInsertCustomer.getId(), customer.getId())
                + instance.getTravelTime(customer.getId(), afterInsertCustomer.getId())
                - instance.getTravelTime(beforeInsertCustomer.getId(), afterInsertCustomer.getId());

        double time = ds.getDelivery().getArriveTime() + ds.getDelivery().getServiceDuration() + instance.getDistanceMatrix()[ds.getId()][ds.getVehicle().getFirstCustomer().getId()];
        for (int i = 0; i < sv.getCustomers().size() - 1; i++) {
            Customer c = sv.getCustomer(i);
            c.getDelivery().setArriveTime(time);
            c.setTwViol(Math.max(0, c.getDelivery().getArriveTime() - c.getEndTw()));
            c.setWaitingTime(Math.max(0, c.getStartTw() - c.getDelivery().getArriveTime()));

            sv.getCost().addTWViol(c.getTwViol());
            sv.getCost().addWaitingTime(c.getWaitingTime());

            time = Math.max(c.getStartTw(), time);
            time += c.getDelivery().getServiceDuration() + instance.getDistanceMatrix()[c.getId()][sv.getCustomer(i + 1).getId()];
        }
        Customer c = sv.getCustomer(sv.getCustomers().size() - 1);
        c.getDelivery().setArriveTime(time);
        c.setTwViol(Math.max(0, c.getDelivery().getArriveTime() - c.getEndTw()));
        c.setWaitingTime(Math.max(0, c.getStartTw() - c.getDelivery().getArriveTime()));

        sv.getCost().addTWViol(c.getTwViol());
        sv.getCost().addWaitingTime(c.getWaitingTime());

        sv.getCost().setLoadViol(Math.max(0, sv.getCost().load - sv.getCapacity()));
    }

    public Cost evaluateInsertRoute2E(DummySatellites ds, SmallVehicle sv, Cost totalCost) {
        sv.addCustomer(customer, insertPositionIndex);

        Cost newCost = new Cost(sv.getCost());
        totalCost.cutCost(sv.getCost());
        newCost.twViol = 0;
        newCost.waitingTime = 0;
        newCost.load += customer.getDelivery().demand;
        newCost.distance += instance.getTravelTime(beforeInsertCustomer.getId(), customer.getId())
                + instance.getTravelTime(customer.getId(), afterInsertCustomer.getId())
                - instance.getTravelTime(beforeInsertCustomer.getId(), afterInsertCustomer.getId());

        double time = ds.getDelivery().getArriveTime() + instance.getPt() * newCost.load + instance.getDistanceMatrix()[ds.getId()][ds.getVehicle().getFirstCustomer().getId()];
        double arriveTime, twViol;
        for (int i = 0; i < sv.getCustomers().size() - 1; i++) {
            Customer c = sv.getCustomer(i);
            arriveTime = time;
            twViol = Math.max(0, arriveTime - c.getEndTw());

            newCost.addTWViol(twViol);

            time = Math.max(c.getStartTw(), time);
            time += c.getDelivery().getServiceDuration() + instance.getDistanceMatrix()[c.getId()][sv.getCustomer(i + 1).getId()];
        }
        Customer c = sv.getCustomer(sv.getCustomers().size() - 1);
        arriveTime = time;
        twViol = Math.max(0, arriveTime - c.getEndTw());

        newCost.addTWViol(twViol);
        newCost.setLoadViol(Math.max(0, newCost.load - sv.getCapacity()));

        sv.removeCustomer(insertPositionIndex);
        return newCost;
    }


    /*
     *   the method will update the change caused by new satellite service time and new customer
     */
    public void updateDeleteRoute2E(DummySatellites ds, SmallVehicle sv, Customer customer, int position) {
        sv.removeCustomer(position);

        if (sv.getCustomersLength() == 0) {
            // 必须初始化，后面计算会用到
            sv.getCost().initialize();
        } else {
            sv.getCost().twViol = 0;
            sv.getCost().waitingTime = 0;
            sv.getCost().load -= customer.getDelivery().demand;
            sv.getCost().distance += -instance.getTravelTime(beforeDeleteCustomer.getId(), customer.getId())
                    - instance.getTravelTime(customer.getId(), afterDeleteCustomer.getId())
                    + instance.getTravelTime(beforeDeleteCustomer.getId(), afterDeleteCustomer.getId());

            double time = ds.getDelivery().getArriveTime() + ds.getDelivery().getServiceDuration() + instance.getDistanceMatrix()[ds.getId()][ds.getVehicle().getFirstCustomer().getId()];
            for (int i = 0; i < sv.getCustomers().size() - 1; i++) {
                Customer c = sv.getCustomer(i);
                c.getDelivery().setArriveTime(time);
                c.setTwViol(Math.max(0, c.getDelivery().getArriveTime() - c.getEndTw()));
                c.setWaitingTime(Math.max(0, c.getStartTw() - c.getDelivery().getArriveTime()));

                sv.getCost().addTWViol(c.getTwViol());
                sv.getCost().addWaitingTime(c.getWaitingTime());

                time = Math.max(c.getStartTw(), time);
                time += c.getDelivery().getServiceDuration() + instance.getDistanceMatrix()[c.getId()][sv.getCustomer(i + 1).getId()];
            }
            Customer c = sv.getCustomer(sv.getCustomers().size() - 1);
            c.getDelivery().setArriveTime(time);
            c.setTwViol(Math.max(0, c.getDelivery().getArriveTime() - c.getEndTw()));
            c.setWaitingTime(Math.max(0, c.getStartTw() - c.getDelivery().getArriveTime()));

            sv.getCost().addTWViol(c.getTwViol());
            sv.getCost().addWaitingTime(c.getWaitingTime());

            sv.getCost().setLoadViol(Math.max(0, sv.getCost().load - sv.getCapacity()));
        }
    }

    public Cost evaluateDeleteRoute2E(DummySatellites ds, SmallVehicle sv, Cost totalCost) {
        sv.removeCustomer(deletePositionIndex);

        Cost newCost = new Cost(sv.getCost());
        totalCost.cutCost(sv.getCost());

        if (sv.getCustomersLength() == 0) {
            // 必须初始化，后面计算会用到
            newCost.initialize();
        } else {
            newCost.twViol = 0;
            newCost.waitingTime = 0;
            newCost.load -= customer.getDelivery().demand;
            newCost.distance += -instance.getTravelTime(beforeDeleteCustomer.getId(), customer.getId())
                    - instance.getTravelTime(customer.getId(), afterDeleteCustomer.getId())
                    + instance.getTravelTime(beforeDeleteCustomer.getId(), afterDeleteCustomer.getId());

            double time = ds.getDelivery().getArriveTime() + instance.getPt() * newCost.load + instance.getDistanceMatrix()[ds.getId()][ds.getVehicle().getFirstCustomer().getId()];
            double arriveTime, twViol;
            for (int i = 0; i < sv.getCustomers().size() - 1; i++) {
                Customer c = sv.getCustomer(i);
                arriveTime = time;
                twViol = Math.max(0, arriveTime - c.getEndTw());

                newCost.addTWViol(twViol);

                time = Math.max(c.getStartTw(), time);
                time += c.getDelivery().getServiceDuration() + instance.getDistanceMatrix()[c.getId()][sv.getCustomer(i + 1).getId()];
            }
            Customer c = sv.getCustomer(sv.getCustomers().size() - 1);
            arriveTime = time;
            twViol = Math.max(0, arriveTime - c.getEndTw());

            newCost.addTWViol(twViol);
            newCost.setLoadViol(Math.max(0, newCost.load - sv.getCapacity()));
        }
        sv.addCustomer(customer, deletePositionIndex);
        return newCost;
    }

    private Cost add1EOr3ECost(BigVehicle insertRoute, BigVehicle deleteRoute) {
        Cost varCost = new Cost();
        if (insertRoute != null){
            varCost.addCost(insertRoute.getCost());
            if (deleteRoute != null && insertRoute != deleteRoute)
                varCost.addCost(deleteRoute.getCost());
        }

        return varCost;
    }

    private Cost add2ECost(BigVehicle insertRoute, BigVehicle deleteRoute, DummySatellites deleteSatellite, DummySatellites insertSatellite) {
        Cost varCost = new Cost();
        varCost.addCost(deleteSatellite.getVehicle().getCost());
        varCost.addCost(insertSatellite.getVehicle().getCost());

        // 少了一个点，则从前面的位置开始加，累加的起始位置前移
        if (deleteSatellite.getVehicle().getCustomersLength() == 0) {
            for (int i = deleteRouteIndex[1]; i < deleteRoute.getSatelliteLength(); i++) {
                varCost.waitingTime += deleteRoute.getDummySatellites().get(i).getVehicle().getCost().waitingTime;
                varCost.twViol += deleteRoute.getDummySatellites().get(i).getVehicle().getCost().twViol;
            }
        } else {
            for (int i = deleteRouteIndex[1] + 1; i < deleteRoute.getSatelliteLength(); i++) {
                varCost.waitingTime += deleteRoute.getDummySatellites().get(i).getVehicle().getCost().waitingTime;
                varCost.twViol += deleteRoute.getDummySatellites().get(i).getVehicle().getCost().twViol;
            }
        }
        for (int i = insertRouteIndex[1] + 1; i < insertRoute.getSatelliteLength(); i++) {
            varCost.waitingTime += insertRoute.getDummySatellites().get(i).getVehicle().getCost().waitingTime;
            varCost.twViol += insertRoute.getDummySatellites().get(i).getVehicle().getCost().twViol;
        }

        return varCost;
    }
}