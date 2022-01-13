package com.zll.Algorithm;

import com.zll.twoEVRP.*;

/**
 * @author： zll-hust
 * @date： 2020/12/12 23:42
 * @description： Satellites-Change neighborhoods
 */
public class SatelliteChangeMove extends MyMove {
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


    public SatelliteChangeMove(SatelliteChangeMove move) {
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

    public SatelliteChangeMove(Instance instance, int firstRouteIndex, int firstPositionIndex,
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
        arcs[3] = new TabuArc(secondSatellite.getId(), secondSatellite.getId2(), beforeSecondSatellite.getId(), beforeSecondSatellite.getId2());
    }


    protected void getInfo(Solution sol) {
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

    @Override
    public double evaluate(Solution s) {
        return 0;
    }

    public void operateOn(Solution sol) {
        getInfo(sol);

        // get route
        BigVehicle firstRoute = depot.getDeliveryVehicles().get(firstRouteIndex);
        BigVehicle secondRoute = depot.getDeliveryVehicles().get(secondRouteIndex);

        DummySatellites firstSatellite = firstRoute.getSatellite(firstPositionIndex);
        DummySatellites secondSatellite = secondRoute.getSatellite(secondPositionIndex);

        BigVehicle firstRoute3E = firstSatellite.getBelongedPickupBigVehicle();
        BigVehicle secondRoute3E = secondSatellite.getBelongedPickupBigVehicle();

        double firstSatelliteOldPickupDemand = firstSatellite.getPickup().getDemand();
        double secondSatelliteOldPickupDemand = secondSatellite.getPickup().getDemand();

        Cost initialCost1E = add1EAnd3ECost(firstRoute, secondRoute);
        Cost initialCost3E = add1EAnd3ECost(firstRoute3E, secondRoute3E);
        Cost initialCost2E = add2ECost(firstRoute, secondRoute);

        exchangeCustomer(firstSatellite, secondSatellite);

        updateFirstRoute1E(firstRoute, secondRoute);
        updateSecondRoute1E(secondRoute, firstRoute);

        updateSmallVehicleCost(firstSatellite, secondSatellite);

        updatePickRoute(firstRoute3E, firstSatellite, firstSatelliteOldPickupDemand);
        updatePickRoute(secondRoute3E, secondSatellite, secondSatelliteOldPickupDemand);

        Cost newCost1E = add1EAnd3ECost(firstRoute, secondRoute);
        Cost newCost3E = add1EAnd3ECost(firstRoute3E, secondRoute3E);
        Cost newCost2E = add2ECost(firstRoute, secondRoute);

        updateTotalCost(sol, initialCost1E, initialCost2E, initialCost3E, newCost1E, newCost2E, newCost3E);
    }

    private void exchangeCustomer(DummySatellites firstSatellite, DummySatellites secondSatellite) {
        SmallVehicle fs = firstSatellite.getVehicle();
        fs.setDummySatellite(secondSatellite);
        SmallVehicle ss = secondSatellite.getVehicle();
        ss.setDummySatellite(firstSatellite);

        firstSatellite.setVehicle(ss);
        secondSatellite.setVehicle(fs);

        Travel fsPick = firstSatellite.getPickup();
        Travel ssPick = secondSatellite.getPickup();
        Travel fsDelivery = firstSatellite.getDelivery();
        Travel ssDelivery = secondSatellite.getDelivery();

        firstSatellite.setPickup(ssPick);
        secondSatellite.setPickup(fsPick);
        firstSatellite.setDelivery(ssDelivery);
        secondSatellite.setDelivery(fsDelivery);
    }

    private void updateFirstRoute1E(BigVehicle bv, BigVehicle bv2) {
        Cost varCost = bv.getCost();
        double variation = -instance.getTravelTime(beforeFirstSatellite.getId(), firstSatellite.getId())
                - instance.getTravelTime(firstSatellite.getId(), afterFirstSatellite.getId())
                + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId())
                + instance.getTravelTime(secondSatellite.getId(), afterFirstSatellite.getId());
        varCost.distance += variation;

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
        secondSatellite.getDelivery().arriveTime = beforeFirstSatellite.getDelivery().arriveTime + beforeFirstSatellite.getDelivery().getServiceDuration() + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId());
        variation = -instance.getTravelTime(beforeFirstSatellite.getId(), firstSatellite.getId())
                - instance.getTravelTime(firstSatellite.getId(), secondSatellite.getVehicle().getCustomer(0).getId())
                + instance.getTravelTime(beforeFirstSatellite.getId(), secondSatellite.getId())
                + instance.getTravelTime(secondSatellite.getId(), secondSatellite.getVehicle().getCustomer(0).getId());
        update2ERoute(secondSatellite, variation);
        updateSatelliteEndTW(secondSatellite);
//        bv2.getCost().twViol += -secondSatellite.getTwViol();
//        varCost.twViol += twViol;
        double twViol = Math.max(0, secondSatellite.getDelivery().getArriveTime() - secondSatellite.getEndTw());
        secondSatellite.setTwViol(twViol);

        secondSatellite.setBelongedDeliveryBigVehicle(bv);
    }

    private void updateSecondRoute1E(BigVehicle bv, BigVehicle bv2) {
        Cost varCost = bv.getCost();
        double variation = -instance.getTravelTime(beforeSecondSatellite.getId(), secondSatellite.getId())
                - instance.getTravelTime(secondSatellite.getId(), afterSecondSatellite.getId())
                + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId())
                + instance.getTravelTime(firstSatellite.getId(), afterSecondSatellite.getId());
        varCost.distance += variation;

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
        firstSatellite.getDelivery().arriveTime = beforeSecondSatellite.getDelivery().arriveTime + beforeSecondSatellite.getDelivery().getServiceDuration() + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId());
        variation = -instance.getTravelTime(beforeSecondSatellite.getId(), secondSatellite.getId())
                - instance.getTravelTime(secondSatellite.getId(), firstSatellite.getVehicle().getCustomer(0).getId())
                + instance.getTravelTime(beforeSecondSatellite.getId(), firstSatellite.getId())
                + instance.getTravelTime(firstSatellite.getId(), firstSatellite.getVehicle().getCustomer(0).getId());
        update2ERoute(firstSatellite, variation);
        updateSatelliteEndTW(firstSatellite);
//        bv2.getCost().twViol += -secondSatellite.getTwViol();
//        varCost.twViol += twViol;
        double twViol = Math.max(0, firstSatellite.getDelivery().getArriveTime() - firstSatellite.getEndTw());
        firstSatellite.setTwViol(twViol);

        firstSatellite.setBelongedDeliveryBigVehicle(bv);
    }

    private void updateSmallVehicleCost(DummySatellites firstSatellite, DummySatellites secondSatellite) {
        firstSatellite.getVehicle().getCost().distance += -instance.getTravelTime(secondSatellite.getId(), firstSatellite.getVehicle().getCustomer(0).getId())
                - instance.getTravelTime(firstSatellite.getVehicle().getLastCustomer().getId(), secondSatellite.getId())
                + instance.getTravelTime(firstSatellite.getId(), firstSatellite.getVehicle().getCustomer(0).getId())
                + instance.getTravelTime(firstSatellite.getVehicle().getLastCustomer().getId(), firstSatellite.getId());

        secondSatellite.getVehicle().getCost().distance += -instance.getTravelTime(firstSatellite.getId(), secondSatellite.getVehicle().getCustomer(0).getId())
                - instance.getTravelTime(secondSatellite.getVehicle().getLastCustomer().getId(), firstSatellite.getId())
                + instance.getTravelTime(secondSatellite.getId(), secondSatellite.getVehicle().getCustomer(0).getId())
                + instance.getTravelTime(secondSatellite.getVehicle().getLastCustomer().getId(), secondSatellite.getId());
    }

    private Cost add1EAnd3ECost(BigVehicle firstRoute, BigVehicle secondRoute) {
        Cost varCost = new Cost();
        varCost.addCost(firstRoute.getCost());
        varCost.addCost(secondRoute.getCost());

        return varCost;
    }

    private Cost add2ECost(BigVehicle firstRoute, BigVehicle secondRoute) {
        Cost varCost = new Cost();
        varCost.distance += firstRoute.getDummySatellites().get(firstPositionIndex).getVehicle().getCost().getDistance();
        varCost.distance += secondRoute.getDummySatellites().get(secondPositionIndex).getVehicle().getCost().getDistance();

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
