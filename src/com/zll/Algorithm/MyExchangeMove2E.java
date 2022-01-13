package com.zll.Algorithm;

import com.zll.twoEVRP.*;

public class MyExchangeMove2E extends MyMove {
    private Customer firstCustomer;
    private Customer secondCustomer;
    private int[] firstRouteIndex;
    private int[] secondRouteIndex;

    private int firstPositionIndex;
    private int secondPositionIndex;

    private Customer beforeFirstCustomer;
    private Customer afterFirstCustomer;
    private Customer beforeSecondCustomer;
    private Customer afterSecondCustomer;

    private Customer beforeSecondSatellite;
    private Customer afterSecondSatellite;
    private Customer beforeFirstSatellite;
    private Customer afterFirstSatellite;

    public MyExchangeMove2E(MyExchangeMove2E move) {
        this.instance = move.instance;
        this.firstRouteIndex = new int[]{move.firstRouteIndex[0], move.firstRouteIndex[1]};
        this.secondRouteIndex = new int[]{move.secondRouteIndex[0], move.secondRouteIndex[1]};
        this.firstPositionIndex = move.firstPositionIndex;
        this.secondPositionIndex = move.secondPositionIndex;

        arcs = new TabuArc[4];
        arcs[0] = new TabuArc(move.getArcs()[0]);
        arcs[1] = new TabuArc(move.getArcs()[1]);
        arcs[2] = new TabuArc(move.getArcs()[2]);
        arcs[3] = new TabuArc(move.getArcs()[3]);
    }

    public MyExchangeMove2E(Instance instance, int[] firstRouteIndex, int firstPositionIndex,
                            int[] secondRouteIndex, int secondPositionIndex, Solution sol) {
        this.instance = instance;
        this.firstRouteIndex = firstRouteIndex;
        this.secondRouteIndex = secondRouteIndex;
        this.firstPositionIndex = firstPositionIndex;
        this.secondPositionIndex = secondPositionIndex;

        getInfo(sol);

        // get tabu arc
        arcs = new TabuArc[4];
        arcs[0] = new TabuArc(beforeFirstCustomer.getId(), beforeFirstCustomer.getId2(), firstCustomer.getId(), firstCustomer.getId2());
        arcs[1] = new TabuArc(firstCustomer.getId(), firstCustomer.getId2(), afterFirstCustomer.getId(), afterFirstCustomer.getId2());
        arcs[2] = new TabuArc(beforeSecondCustomer.getId(), beforeSecondCustomer.getId2(), secondCustomer.getId(), secondCustomer.getId2());
        arcs[3] = new TabuArc(secondCustomer.getId(), secondCustomer.getId2(), afterSecondCustomer.getId(), afterSecondCustomer.getId2());
    }

    protected void getInfo(Solution sol) {
        // get depot
        this.depot = sol.getDepot();

        // get route belonged
        DummySatellites firstSatellite = sol.getDepot().getDeliveryVehicles().get(firstRouteIndex[0])
                .getSatellite(firstRouteIndex[1]);
        SmallVehicle firstRoute = firstSatellite.getVehicle();

        DummySatellites secondSatellite = sol.getDepot().getDeliveryVehicles().get(secondRouteIndex[0])
                .getSatellite(secondRouteIndex[1]);
        SmallVehicle secondRoute = secondSatellite.getVehicle();

        this.firstCustomer = firstRoute.getCustomer(firstPositionIndex);
        this.secondCustomer = secondRoute.getCustomer(secondPositionIndex);

        // get before and after satellites
        if (firstPositionIndex == 0) {
            beforeFirstCustomer = firstSatellite;
        } else {
            beforeFirstCustomer = firstRoute.getCustomers().get(firstPositionIndex - 1);
        }
        if (firstPositionIndex == firstRoute.getCustomers().size() - 1) {
            afterFirstCustomer = firstSatellite;
        } else {
            afterFirstCustomer = firstRoute.getCustomers().get(firstPositionIndex + 1);
        }

        if (secondPositionIndex == 0) {
            beforeSecondCustomer = secondSatellite;
        } else {
            beforeSecondCustomer = secondRoute.getCustomers().get(secondPositionIndex - 1);
        }
        if (secondPositionIndex == secondRoute.getCustomers().size() - 1) {
            afterSecondCustomer = secondSatellite;
        } else {
            afterSecondCustomer = secondRoute.getCustomers().get(secondPositionIndex + 1);
        }

        // get satellite
        if (firstRouteIndex[1] == 0) {
            beforeFirstSatellite = depot;
        } else {
            beforeFirstSatellite = sol.getDepot().getDeliveryVehicles().get(firstRouteIndex[0])
                    .getSatellite(firstRouteIndex[1] - 1);
        }
        if (firstRouteIndex[1] + 1 == sol.getDepot().getDeliveryVehicles().get(firstRouteIndex[0]).getSatelliteLength()) {
            afterFirstSatellite = depot;
        } else {
            afterFirstSatellite = sol.getDepot().getDeliveryVehicles().get(firstRouteIndex[0])
                    .getSatellite(firstRouteIndex[1] + 1);
        }

        if (secondRouteIndex[1] == 0) {
            beforeSecondSatellite = depot;
        } else {
            beforeSecondSatellite = sol.getDepot().getDeliveryVehicles().get(secondRouteIndex[0])
                    .getSatellite(secondRouteIndex[1] - 1);
        }
        if (secondRouteIndex[1] + 1 == sol.getDepot().getDeliveryVehicles().get(secondRouteIndex[0]).getSatelliteLength()) {
            afterSecondSatellite = depot;
        } else {
            afterSecondSatellite = sol.getDepot().getDeliveryVehicles().get(secondRouteIndex[0])
                    .getSatellite(secondRouteIndex[1] + 1);
        }
    }

    public double evaluate(Solution sol) {
        getInfo(sol);
        Cost newTotalCost = new Cost(sol.getTotalCost());

        // get route
        BigVehicle firstRoute1E = sol.getDepot().getDeliveryVehicles().get(firstRouteIndex[0]);
        BigVehicle secondRoute1E = sol.getDepot().getDeliveryVehicles().get(secondRouteIndex[0]);

        DummySatellites firstSatellite = firstRoute1E.getSatellite(firstRouteIndex[1]);
        DummySatellites secondSatellite = secondRoute1E.getSatellite(secondRouteIndex[1]);

        SmallVehicle firstRoute2E = firstSatellite.getVehicle();
        SmallVehicle secondRoute2E = secondSatellite.getVehicle();

        BigVehicle firstRoute3E = firstSatellite.getBelongedPickupBigVehicle();
        BigVehicle secondRoute3E = secondSatellite.getBelongedPickupBigVehicle();

        Cost newCostFirst2E = evaluateRoute2EFirst(firstSatellite, firstRoute2E, newTotalCost);
        Cost newCostSecond2E = evaluateRoute2ESecond(secondSatellite, secondRoute2E, newTotalCost);

        double newFirst1EDemand = firstSatellite.getDelivery().getDemand() - firstCustomer.getDelivery().demand + secondCustomer.getDelivery().demand;
        double newSecond1EDemand = secondSatellite.getDelivery().getDemand() - secondCustomer.getDelivery().demand + firstCustomer.getDelivery().demand;
        Cost newCostFirst1E = evaluateRoute1E(firstRoute1E, firstSatellite, beforeFirstSatellite, afterFirstSatellite, firstRouteIndex[1], newFirst1EDemand, newTotalCost);
        Cost newCostSecond1E = evaluateRoute1E(secondRoute1E, secondSatellite, beforeSecondSatellite, afterSecondSatellite, secondRouteIndex[1], newSecond1EDemand, newTotalCost);

        double newFirst3EDemand = firstSatellite.getPickup().getDemand() - firstCustomer.getPickup().demand + secondCustomer.getPickup().demand;
        double newSecond3EDemand = secondSatellite.getPickup().getDemand() - secondCustomer.getPickup().demand + firstCustomer.getPickup().demand;
        if (firstRoute3E != secondRoute3E) {
            Cost newCostFirst3E = evaluatePickRoute(firstRoute3E, firstSatellite, newFirst3EDemand, newTotalCost);
            Cost newCostSecond3E = evaluatePickRoute(secondRoute3E, secondSatellite, newSecond3EDemand, newTotalCost);
            newTotalCost.addCost(newCostFirst3E);
            newTotalCost.addCost(newCostSecond3E);
        } else {
            Cost newCost3E = evaluateIntraPickRoute(firstRoute3E, firstSatellite, secondSatellite, newFirst3EDemand, newSecond3EDemand, newTotalCost);
            newTotalCost.addCost(newCost3E);
        }


        newTotalCost.addCost(newCostFirst1E);
        newTotalCost.addCost(newCostSecond1E);
        newTotalCost.addCost(newCostFirst2E);
        newTotalCost.addCost(newCostSecond2E);
        newTotalCost.calculateTotal();
        return newTotalCost.total;
    }

    @Override
    public void operateOn(Solution sol) {
        getInfo(sol);

        // get route
        BigVehicle firstRoute1E = sol.getDepot().getDeliveryVehicles().get(firstRouteIndex[0]);
        BigVehicle secondRoute1E = sol.getDepot().getDeliveryVehicles().get(secondRouteIndex[0]);

        DummySatellites firstSatellite = firstRoute1E.getSatellite(firstRouteIndex[1]);
        DummySatellites secondSatellite = secondRoute1E.getSatellite(secondRouteIndex[1]);

        SmallVehicle firstRoute2E = firstSatellite.getVehicle();
        SmallVehicle secondRoute2E = secondSatellite.getVehicle();

        // get 1e route information
        double[] firstSatelliteInfo = firstSatellite.getOldInfo();
        double[] secondSatelliteInfo = secondSatellite.getOldInfo();

        // get 3e route information
        double firstSatelliteOldPickupDemand = firstSatellite.getPickup().getDemand();
        double secondSatelliteOldPickupDemand = secondSatellite.getPickup().getDemand();
        BigVehicle firstRoute3E = firstSatellite.getBelongedPickupBigVehicle();
        BigVehicle secondRoute3E = secondSatellite.getBelongedPickupBigVehicle();

        Cost old1ECost = add1EOr3ECost(firstRoute1E, secondRoute1E);
        Cost old2ECost = add2ECost(firstRoute1E, secondRoute1E, firstSatellite, secondSatellite);
        Cost old3ECost = add1EOr3ECost(firstRoute3E, secondRoute3E);

        updateSatelliteServiceTime(firstSatellite, firstCustomer, secondCustomer);
        updateSatelliteServiceTime(secondSatellite, secondCustomer, firstCustomer);

        updateRoute2E(firstSatellite, firstRoute2E, secondCustomer, firstPositionIndex, 0);
        updateRoute2E(secondSatellite, secondRoute2E, firstCustomer, secondPositionIndex, 1);

        updateSatelliteEndTW(firstSatellite);
        updateSatelliteEndTW(secondSatellite);

        updateRoute1E(firstRoute1E, firstSatellite, firstRouteIndex[1], firstSatelliteInfo);
        updateRoute1E(secondRoute1E, secondSatellite, secondRouteIndex[1], secondSatelliteInfo);

        updatePickRoute(firstRoute3E, firstSatellite, firstSatelliteOldPickupDemand);
        updatePickRoute(secondRoute3E, secondSatellite, secondSatelliteOldPickupDemand);

        Cost new1ECost = add1EOr3ECost(firstRoute1E, secondRoute1E);
        Cost new2ECost = add2ECost(firstRoute1E, secondRoute1E, firstSatellite, secondSatellite);
        Cost new3ECost = add1EOr3ECost(firstRoute3E, secondRoute3E);

        updateTotalCost(sol, old1ECost, old2ECost, old3ECost, new1ECost, new2ECost, new3ECost);
    }

    // 在对应satellite上删除origin，增加other
    private void updateSatelliteServiceTime(DummySatellites ds, Customer origin, Customer other) {
        ds.getDelivery().demand += -origin.getDelivery().getDemand() + other.getDelivery().getDemand();
        ds.getDelivery().serviceDuration = instance.getPt() * ds.getDelivery().getDemand();
        ds.getPickup().demand += -origin.getPickup().getDemand() + other.getPickup().getDemand();
    }

    private void updateRoute2E(DummySatellites ds, SmallVehicle sv, Customer customer, int position, int type) {
        sv.getCustomers().set(position, customer);

        sv.getCost().twViol = 0;
        sv.getCost().waitingTime = 0;

        if (type == 0) { // first route
            sv.getCost().distance +=
                    -instance.getTravelTime(beforeFirstCustomer.getId(), firstCustomer.getId())
                            - instance.getTravelTime(firstCustomer.getId(), afterFirstCustomer.getId())
                            + instance.getTravelTime(beforeFirstCustomer.getId(), secondCustomer.getId())
                            + instance.getTravelTime(secondCustomer.getId(), afterFirstCustomer.getId());
            sv.getCost().load += -firstCustomer.getDelivery().getDemand() + secondCustomer.getDelivery().demand;
        } else if (type == 1) { // second route
            sv.getCost().distance +=
                    -instance.getTravelTime(beforeSecondCustomer.getId(), secondCustomer.getId())
                            - instance.getTravelTime(secondCustomer.getId(), afterSecondCustomer.getId())
                            + instance.getTravelTime(beforeSecondCustomer.getId(), firstCustomer.getId())
                            + instance.getTravelTime(firstCustomer.getId(), afterSecondCustomer.getId());
            sv.getCost().load += firstCustomer.getDelivery().getDemand() - secondCustomer.getDelivery().demand;
        }

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

    private Cost evaluateRoute2EFirst(DummySatellites ds, SmallVehicle sv, Cost totalCost) {
        sv.getCustomers().set(firstPositionIndex, secondCustomer);

        Cost newCost = new Cost(sv.getCost());
        totalCost.cutCost(sv.getCost());
        newCost.twViol = 0;
        newCost.waitingTime = 0;
        newCost.distance +=
                -instance.getTravelTime(beforeFirstCustomer.getId(), firstCustomer.getId())
                        - instance.getTravelTime(firstCustomer.getId(), afterFirstCustomer.getId())
                        + instance.getTravelTime(beforeFirstCustomer.getId(), secondCustomer.getId())
                        + instance.getTravelTime(secondCustomer.getId(), afterFirstCustomer.getId());
        newCost.load += -firstCustomer.getDelivery().getDemand() + secondCustomer.getDelivery().demand;

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

        sv.getCustomers().set(firstPositionIndex, firstCustomer);

        return newCost;
    }

    private Cost evaluateRoute2ESecond(DummySatellites ds, SmallVehicle sv, Cost totalCost) {
        sv.getCustomers().set(secondPositionIndex, firstCustomer);

        Cost newCost = new Cost(sv.getCost());
        totalCost.cutCost(sv.getCost());
        newCost.twViol = 0;
        newCost.waitingTime = 0;
        newCost.distance +=
                -instance.getTravelTime(beforeSecondCustomer.getId(), secondCustomer.getId())
                        - instance.getTravelTime(secondCustomer.getId(), afterSecondCustomer.getId())
                        + instance.getTravelTime(beforeSecondCustomer.getId(), firstCustomer.getId())
                        + instance.getTravelTime(firstCustomer.getId(), afterSecondCustomer.getId());
        newCost.load += firstCustomer.getDelivery().getDemand() - secondCustomer.getDelivery().demand;

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

        sv.getCustomers().set(secondPositionIndex, secondCustomer);

        return newCost;
    }

    private Cost add1EOr3ECost(BigVehicle firstRoute, BigVehicle secondRoute) {
        Cost varCost = new Cost();
        if (firstRoute != null) {
            varCost.addCost(firstRoute.getCost());
            if (secondRoute != null && firstRoute != secondRoute)
                varCost.addCost(secondRoute.getCost());
        }

        return varCost;
    }

    private Cost add2ECost(BigVehicle firstRoute, BigVehicle secondRoute, DummySatellites firstSatellite, DummySatellites secondSatellite) {
        Cost varCost = new Cost();
        varCost.addCost(firstSatellite.getVehicle().getCost());
        varCost.addCost(secondSatellite.getVehicle().getCost());

        for (int i = firstRouteIndex[1] + 1; i < firstRoute.getSatelliteLength(); i++) {
            varCost.waitingTime += firstRoute.getDummySatellites().get(i).getVehicle().getCost().waitingTime;
            varCost.twViol += firstRoute.getDummySatellites().get(i).getVehicle().getCost().twViol;
        }

        for (int i = secondRouteIndex[1] + 1; i < secondRoute.getSatelliteLength(); i++) {
            varCost.waitingTime += secondRoute.getDummySatellites().get(i).getVehicle().getCost().waitingTime;
            varCost.twViol += secondRoute.getDummySatellites().get(i).getVehicle().getCost().twViol;
        }

        return varCost;
    }
}
