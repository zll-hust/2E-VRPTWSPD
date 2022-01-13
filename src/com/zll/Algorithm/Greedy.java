package com.zll.Algorithm;

import com.zll.twoEVRP.*;

import java.util.ArrayList;
import java.util.List;

public class Greedy {

    private double[][] distanceMatrix;
    private Depot depot;
    private List<Satellite> satellites;
    private List<Customer> customers;
    private double pt;
    private Instance instance;

    private int satellitesNr;
    private int customersNr;
    private int totalNodeNr;

    public Greedy(Instance instance) {
        this.distanceMatrix = instance.getDistanceMatrix();
        this.customers = new ArrayList<Customer>();
        for (Customer c : instance.getCustomers())
            this.customers.add(new Customer(c));
        this.satellites = new ArrayList<Satellite>();
        for (Satellite s : instance.getSatellites())
            this.satellites.add(new Satellite(s));
        this.depot = new Depot(instance.getDepot());
        this.pt = instance.getPt();

        this.instance = instance;
    }


    // 按距离远近分配客户，将每个客户分配到距离最近的satellite
    private void AssignCustomers() {
        for (Customer c : customers) {
            double minDis = Double.POSITIVE_INFINITY;
            Satellite minS = null;
            for (Satellite s : satellites) {
                if (distanceMatrix[depot.getId()][s.getId()] + distanceMatrix[s.getId()][c.getId()] < minDis) {
                    minDis = distanceMatrix[depot.getId()][s.getId()] + distanceMatrix[s.getId()][c.getId()];
                    minS = s;
                }
            }
            minS.addAssiginedCustomers(c);
        }
    }

    // 找到最迟开始服务时间最早的客户/卫星
    public <T extends Customer> T FindMinLastStartingTime(ArrayList<T> cs) {
        double minLastStartingTime = Integer.MAX_VALUE;
        T first = null;
        for (T c : cs) {
            if (c.getEndTw() < minLastStartingTime) {
                minLastStartingTime = c.getEndTw();
                first = c;
            }
        }
        return first;
    }

    public Customer FindMinLastStartingTimeCustomerSatisfyTimeWin(ArrayList<Customer> cs, Satellite s) {
        double minLastStartingTime = Integer.MAX_VALUE;
        Customer first = null;
        for (Customer c : cs) {
            if (c.getEndTw() < minLastStartingTime && distanceMatrix[depot.getId()][s.getId()] + distanceMatrix[s.getId()][c.getId()] + pt * c.getDelivery().getDemand() <= c.getEndTw()) {
                minLastStartingTime = c.getEndTw();
                first = c;
            }
        }
        return first;
    }

    public void Construct2ndVRPTW() {
        for (Satellite s : satellites) {
            ArrayList<Customer> cusList = (ArrayList<Customer>) s.getAssignedcustomers().clone();

            if (cusList.size() == 0)
                continue;

            SmallVehicle currentVehicle = new SmallVehicle(s);

            Customer first = this.FindMinLastStartingTimeCustomerSatisfyTimeWin(cusList, s);
            if (first == null) {
                System.out.println("no answer satisfy time window");
                first = this.FindMinLastStartingTime(cusList);
            }
            currentVehicle.addCustomer(first);

            // 以第一个点的时间窗开始时间为初始时间
            first.getDelivery().setArriveTime(distanceMatrix[depot.getId()][s.getId()] + distanceMatrix[s.getId()][first.getId()] + pt * first.getDelivery().getDemand());
            currentVehicle.getCost().setTime(first.getDelivery().getArriveTime() + first.getDelivery().getServiceDuration());
            currentVehicle.getCost().addDistance(distanceMatrix[s.getId()][first.getId()] + distanceMatrix[first.getId()][s.getId()]);
            currentVehicle.getCost().addLoad(first.getDelivery().getDemand());
            cusList.remove(first);

            // 对所有未分配行程的客户进行循环
            while (true) {
                if (cusList.size() == 0) {
                    s.addRoute(currentVehicle);
                    break;
                }

                Customer lastInTheCurrentRoute = currentVehicle.getLastCustomer();
                double smallestSaving = Double.MAX_VALUE;
                Customer bestInsertCustomer = null;

                // 找到节省最少的客户插入
                for (Customer n : cusList) {
                    double saving = -distanceMatrix[lastInTheCurrentRoute.getId()][s.getId()] +
                            distanceMatrix[lastInTheCurrentRoute.getId()][n.getId()] +
                            distanceMatrix[n.getId()][s.getId()];

                    if ((saving < smallestSaving && evaluateInsertCustomer(currentVehicle, n))) {
                        smallestSaving = saving;
                        bestInsertCustomer = n;
                    }
                }

                // 如果找到可插入的最优客户点
                if (bestInsertCustomer != null) {
                    currentVehicle.addCustomer(bestInsertCustomer);
                    updateInsertCustomer(currentVehicle, bestInsertCustomer);

                    currentVehicle.getCost().addDistance(smallestSaving);
                    currentVehicle.getCost().addLoad(bestInsertCustomer.getDelivery().getDemand());
                    cusList.remove(bestInsertCustomer);
                } else {
                    s.addRoute(currentVehicle);

                    if (cusList.size() == 0)
                        break;

                    currentVehicle = new SmallVehicle(s);
                    first = this.FindMinLastStartingTimeCustomerSatisfyTimeWin(cusList, s);
                    if (first == null) {
                        System.out.println("no answer satisfy time window");
                        first = this.FindMinLastStartingTime(cusList);
                    }
                    currentVehicle.addCustomer(first);

                    // 以第一个点的时间窗开始时间为初始时间
                    first.getDelivery().setArriveTime(distanceMatrix[depot.getId()][s.getId()] + distanceMatrix[s.getId()][first.getId()] + pt * first.getDelivery().getDemand());
                    currentVehicle.getCost().setTime(first.getDelivery().getArriveTime() + first.getDelivery().getServiceDuration());
                    currentVehicle.getCost().addDistance(distanceMatrix[s.getId()][first.getId()] + distanceMatrix[first.getId()][s.getId()]);
                    currentVehicle.getCost().addLoad(first.getDelivery().getDemand());
                    cusList.remove(first);
                }
            }
        }

    }

    private boolean evaluateInsertCustomer(SmallVehicle sv, Customer c) {
        // 车辆载重约束
        if (sv.getCost().getLoad() + c.getDelivery().getDemand() > sv.getCapacity())
            return false;
        double time = sv.getFirstCustomer().getDelivery().getArriveTime() + pt * c.getDelivery().getDemand();
        for (int i = 0; i < sv.getCustomers().size() - 1; i++) {
            Customer cus = sv.getCustomer(i);
            if (time > cus.getEndTw()) {
                return false;
            }
            time = Math.max(cus.getStartTw(), time);
            time += cus.getDelivery().getServiceDuration() + instance.getDistanceMatrix()[cus.getId()][sv.getCustomer(i + 1).getId()];
        }
        if (time > sv.getLastCustomer().getEndTw())
            return false;
        time = Math.max(sv.getLastCustomer().getStartTw(), time);
        time += sv.getLastCustomer().getDelivery().getServiceDuration() + instance.getDistanceMatrix()[sv.getLastCustomer().getId()][c.getId()];
        if (time > c.getEndTw())
            return false;

        return true;
    }

    private void updateInsertCustomer(SmallVehicle sv, Customer c) {
        double time = sv.getFirstCustomer().getDelivery().getArriveTime() + pt * c.getDelivery().getDemand();
        for (int i = 0; i < sv.getCustomers().size() - 1; i++) {
            Customer cus = sv.getCustomer(i);
            cus.getDelivery().setArriveTime(time);
            cus.setTwViol(Math.max(0, cus.getDelivery().getArriveTime() - cus.getEndTw()));
            cus.setWaitingTime(Math.max(0, cus.getStartTw() - cus.getDelivery().getArriveTime()));

            time = Math.max(cus.getStartTw(), time);
            time += cus.getDelivery().getServiceDuration() + instance.getDistanceMatrix()[cus.getId()][sv.getCustomer(i + 1).getId()];
        }
        c.getDelivery().setArriveTime(time);
        c.setTwViol(Math.max(0, c.getDelivery().getArriveTime() - c.getEndTw()));
        c.setWaitingTime(Math.max(c.getStartTw() - c.getDelivery().getArriveTime(), 0));
    }

    public void GenerateDummySatellites(int[] dsNr) {
        int count = 0;
        for (Satellite s : satellites) {
            int picked = 0;
            dsNr[s.getId()] = s.getVehicles().size();
            for (int i = 0; i < s.getVehicles().size(); i++) {
                SmallVehicle sv = s.getVehicles().get(i);
                DummySatellites nS = new DummySatellites(s);
                sv.setDummySatellite(nS);
                double TS = Double.POSITIVE_INFINITY;
                double TSi;
                double waitingTimeSum = 0, lastWaitingTime = 0;
                int totalDemand = 0;
                int totalPickUp = 0;
                boolean flag = true; // 存在可行时间窗
                for (Customer c : sv.getCustomers()) {
                    if (lastWaitingTime > 0 && c.getTwViol() > 0) {
                        flag = false;
                    }
                    TSi = c.getEndTw() - c.getDelivery().getArriveTime() + waitingTimeSum;
                    waitingTimeSum += c.getWaitingTime();
                    lastWaitingTime = c.getWaitingTime();
                    if (TSi < TS)
                        TS = TSi;
                    totalDemand += c.getDelivery().getDemand();
                    totalPickUp += c.getPickup().getDemand();
                }

                nS.setId2(i);
                nS.setVehicle(sv);
                nS.getDelivery().setDemand(totalDemand);
                nS.getPickup().setDemand(totalPickUp);
                nS.getDelivery().setServiceDuration(totalDemand * pt);
                nS.getPickup().setServiceDuration(totalPickUp * pt);

                if(flag)
                    nS.setEndTw(sv.getFirstCustomer().getDelivery().getArriveTime() - distanceMatrix[s.getId()][sv.getFirstCustomer().getId()] + TS - nS.getDelivery().getServiceDuration());
                else
                    nS.setEndTw(-1);

                depot.addSatellites(nS);
                picked += totalPickUp;
            }
            s.getPickup().setDemand(picked);
        }
    }

    public void Construct1stVRPTW(Solution sol) {
        ArrayList<DummySatellites> sateList = (ArrayList<DummySatellites>) depot.getAssignedSatellites().clone();
        BigVehicle currentVehicle = new BigVehicle(depot);

        DummySatellites first = FindMinLastStartingTime(sateList);
        currentVehicle.addSatellite(first);
        first.setBelongedDeliveryBigVehicle(currentVehicle);
        first.getDelivery().setArriveTime(distanceMatrix[depot.getId()][first.getId()]);
        first.setTwViol(Math.max(0, first.getDelivery().getArriveTime() - first.getEndTw()));

        currentVehicle.getCost().setTime(first.getDelivery().getArriveTime() + first.getDelivery().getServiceDuration());
        currentVehicle.getCost().setTwViol(first.getTwViol());
        currentVehicle.getCost().addDistance(distanceMatrix[depot.getId()][first.getId()] + distanceMatrix[first.getId()][depot.getId()]);
        currentVehicle.getCost().addLoad(first.getDelivery().getDemand());
        sateList.remove(first);

        // 对所有未分配行程的客户进行循环
        while (true) {
            if (sateList.size() == 0) {
                depot.addDeliveryRoute(currentVehicle);
                sol.getCostForDelivery1E().addCost(currentVehicle.getCost());
                break;
            }

            DummySatellites lastInTheCurrentRoute = currentVehicle.getLastSatellite();
            double smallestSaving = Double.MAX_VALUE;
            DummySatellites bestInsertCustomer = null;

            // 找到节省最少的客户插入
            for (DummySatellites n : sateList) {
                double saving = -distanceMatrix[lastInTheCurrentRoute.getId()][depot.getId()] +
                        distanceMatrix[lastInTheCurrentRoute.getId()][n.getId()] +
                        distanceMatrix[n.getId()][depot.getId()];

                if ((saving < smallestSaving) &&
                        (currentVehicle.getCost().getLoad() + n.getDelivery().getDemand() <= currentVehicle.getCapacity()) &&
                        currentVehicle.getCost().getTime() + distanceMatrix[lastInTheCurrentRoute.getId()][n.getId()] <= n.getEndTw()) {
                    smallestSaving = saving;
                    bestInsertCustomer = n;
                }
            }

            // 如果找到可插入的最优客户点
            if (bestInsertCustomer != null) {
                bestInsertCustomer.getDelivery().setArriveTime(currentVehicle.getCost().getTime() + distanceMatrix[currentVehicle.getLastSatellite().getId()][bestInsertCustomer.getId()]);
                bestInsertCustomer.setTwViol(Math.max(0, bestInsertCustomer.getDelivery().getArriveTime() - bestInsertCustomer.getEndTw()));

                currentVehicle.addSatellite(bestInsertCustomer);
                bestInsertCustomer.setBelongedDeliveryBigVehicle(currentVehicle);

                currentVehicle.getCost().setTime(bestInsertCustomer.getDelivery().getArriveTime() + bestInsertCustomer.getDelivery().getServiceDuration());
                currentVehicle.getCost().setTwViol(bestInsertCustomer.getTwViol());
                currentVehicle.getCost().addDistance(smallestSaving);
                currentVehicle.getCost().addLoad(bestInsertCustomer.getDelivery().getDemand());
                sateList.remove(bestInsertCustomer);

            } else {
                depot.addDeliveryRoute(currentVehicle);
                sol.getCostForDelivery1E().addCost(currentVehicle.getCost());

                if (sateList.size() == 0)
                    break;

                currentVehicle = new BigVehicle(depot);
                first = FindMinLastStartingTime(sateList);
                currentVehicle.addSatellite(first);
                first.setBelongedDeliveryBigVehicle(currentVehicle);
                first.getDelivery().setArriveTime(distanceMatrix[depot.getId()][first.getId()]);
                first.setTwViol(Math.max(0, first.getDelivery().getArriveTime() - first.getEndTw()));

                currentVehicle.getCost().setTime(first.getDelivery().getArriveTime() + first.getDelivery().getServiceDuration());
                currentVehicle.getCost().setTwViol(first.getTwViol());
                currentVehicle.getCost().addDistance(distanceMatrix[depot.getId()][first.getId()] + distanceMatrix[first.getId()][depot.getId()]);
                currentVehicle.getCost().addLoad(first.getDelivery().getDemand());
                sateList.remove(first);
            }
        }
    }

    private void UpdateArrivalTime() {
        for (BigVehicle bv : depot.getDeliveryVehicles()) {
            for (DummySatellites ds : bv.getDummySatellites()) {
                SmallVehicle currentRoute = ds.getVehicle();
                currentRoute.getCost().time = ds.getDelivery().getArriveTime() + ds.getDelivery().getServiceDuration() + distanceMatrix[ds.getId()][ds.getVehicle().getFirstCustomer().getId()];
                for (int i = 0; i < currentRoute.getCustomers().size() - 1; i++) {
                    Customer c = currentRoute.getCustomer(i);
                    c.getDelivery().setArriveTime(currentRoute.getCost().time);
                    c.setTwViol(Math.max(0, c.getDelivery().getArriveTime() - c.getEndTw()));
                    c.setWaitingTime(Math.max(0, c.getStartTw() - c.getDelivery().getArriveTime()));

                    currentRoute.getCost().addTWViol(c.getTwViol());
                    currentRoute.getCost().addWaitingTime(c.getWaitingTime());

                    currentRoute.getCost().time = Math.max(c.getStartTw(), currentRoute.getCost().time);
                    currentRoute.getCost().time += c.getDelivery().getServiceDuration() + distanceMatrix[c.getId()][currentRoute.getCustomer(i + 1).getId()];
                }
                Customer c = currentRoute.getCustomer(currentRoute.getCustomers().size() - 1);
                c.getDelivery().setArriveTime(currentRoute.getCost().time);
                c.setTwViol(Math.max(0, c.getDelivery().getArriveTime() - c.getEndTw()));
                c.setWaitingTime(Math.max(0, c.getStartTw() - c.getDelivery().getArriveTime()));

                currentRoute.getCost().addTWViol(c.getTwViol());
                currentRoute.getCost().addWaitingTime(c.getWaitingTime());
            }
        }
    }

    private void Construct3rdVRPTW(Solution sol) {
        ArrayList<DummySatellites> sateList = new ArrayList<>();
        for (DummySatellites ds : depot.getAssignedSatellites())
            if (ds.getPickup().demand != 0)
                sateList.add(ds);

        if (sateList.size() == 0) {
            return;
        }

        BigVehicle currentVehicle = new BigVehicle(depot);

        DummySatellites first = sateList.remove(0);
        currentVehicle.addSatellite(first);
        first.setBelongedPickupBigVehicle(currentVehicle);
        first.getPickup().setArriveTime(distanceMatrix[depot.getId()][first.getId()]);
        currentVehicle.getCost().setTime(first.getPickup().getArriveTime() + first.getPickup().getServiceDuration());
        currentVehicle.getCost().addDistance(distanceMatrix[depot.getId()][first.getId()] + distanceMatrix[first.getId()][depot.getId()]);
        currentVehicle.getCost().addLoad(first.getPickup().getDemand());
        sateList.remove(first);

        // 对所有未分配行程的客户进行循环
        while (true) {
            if (sateList.size() == 0) {
                depot.addPickupRoute(currentVehicle);
                sol.getCostForPickup1E().distance += currentVehicle.getCost().getDistance();
                break;
            }

            DummySatellites lastInTheCurrentRoute = currentVehicle.getLastSatellite();
            double smallestSaving = Double.MAX_VALUE;
            DummySatellites bestInsertCustomer = null;

            // 找到节省最少的客户插入
            for (DummySatellites n : sateList) {
                double saving = -distanceMatrix[lastInTheCurrentRoute.getId()][depot.getId()] +
                        distanceMatrix[lastInTheCurrentRoute.getId()][n.getId()] +
                        distanceMatrix[n.getId()][depot.getId()];

                if ((saving < smallestSaving) &&
                        (currentVehicle.getCost().getLoad() + n.getPickup().getDemand() <= currentVehicle.getCapacity())) {
                    smallestSaving = saving;
                    bestInsertCustomer = n;
                }
            }

            // 如果找到可插入的最优客户点
            if (bestInsertCustomer != null) {
                currentVehicle.addSatellite(bestInsertCustomer);
                bestInsertCustomer.setBelongedPickupBigVehicle(currentVehicle);

                bestInsertCustomer.getPickup().setArriveTime(currentVehicle.getCost().getTime() + distanceMatrix[currentVehicle.getLastSatellite().getId()][bestInsertCustomer.getId()]);
                currentVehicle.getCost().setTime(bestInsertCustomer.getPickup().getArriveTime() + bestInsertCustomer.getPickup().getServiceDuration());

                currentVehicle.getCost().addDistance(smallestSaving);

                currentVehicle.getCost().addLoad(bestInsertCustomer.getPickup().getDemand());
                sateList.remove(bestInsertCustomer);
            } else {
                depot.addPickupRoute(currentVehicle);
                sol.getCostForPickup1E().distance += currentVehicle.getCost().getDistance();

                if (sateList.size() == 0)
                    break;

                currentVehicle = new BigVehicle(depot);
                first = sateList.remove(0);
                currentVehicle.addSatellite(first);
                first.setBelongedPickupBigVehicle(currentVehicle);
                first.getPickup().setArriveTime(distanceMatrix[depot.getId()][first.getId()]);
                currentVehicle.getCost().setTime(first.getPickup().getArriveTime() + first.getPickup().getServiceDuration());
                currentVehicle.getCost().addDistance(distanceMatrix[depot.getId()][first.getId()] + distanceMatrix[first.getId()][depot.getId()]);
                currentVehicle.getCost().addLoad(first.getPickup().getDemand());
                sateList.remove(first);
            }
        }
    }

    public Solution FindSolution(int[] dsNr) {
        AssignCustomers();
        Construct2ndVRPTW();
        GenerateDummySatellites(dsNr);
        Solution sol = new Solution(depot, instance);
        Construct1stVRPTW(sol);
        UpdateArrivalTime();
        Construct3rdVRPTW(sol);
        sol.updateTotalCost();
        return sol;
    }
}