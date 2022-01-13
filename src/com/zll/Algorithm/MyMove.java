package com.zll.Algorithm;

import com.zll.twoEVRP.*;

import java.util.*;

public abstract class MyMove implements Comparable<MyMove> {
    protected Instance instance;
    protected Depot depot;
    protected TabuArc[] arcs;
    protected double objVal = Double.POSITIVE_INFINITY;
    protected Random r;

    public abstract void operateOn(Solution s);

    public abstract double evaluate(Solution s);

    // 更新引用
    protected abstract void getInfo(Solution s);

    public void applyTabuArc(int[][][][] tabuMatrix, int iteration, int tabuHorizon) {
        for (TabuArc tabuArc : arcs) {
            tabuMatrix[tabuArc.getFrom1()][tabuArc.getFrom2()][tabuArc.getTo1()][tabuArc.getTo2()] = iteration + tabuHorizon;
        }
    }

    protected void updateSatelliteEndTW(DummySatellites ds) {
        SmallVehicle sv = ds.getVehicle();

        if (sv.getCustomers().size() == 0) {
            return;
        }

        double TS = Double.POSITIVE_INFINITY;
        double TSi, waitingTimeSum = 0, lastWaitingTime = 0;
        for (Customer c : sv.getCustomers()) {
            if (lastWaitingTime > 0 && c.getTwViol() > 0) {
                ds.setEndTw(-1);
                return;
            }
            TSi = c.getEndTw() - c.getDelivery().getArriveTime() + waitingTimeSum;
            waitingTimeSum += c.getWaitingTime();
            lastWaitingTime = c.getWaitingTime();
            if (TSi < TS)
                TS = TSi;
            if (TSi < TS)
                TS = TSi;
        }
        ds.setEndTw(sv.getFirstCustomer().getDelivery().getArriveTime() - instance.getDistanceMatrix()[ds.getId()][sv.getFirstCustomer().getId()] + TS - ds.getDelivery().getServiceDuration());
    }

    protected void updateRoute1E(BigVehicle bv, DummySatellites ds, int position, double[] oldInfo) {
        updateRoute1E(bv, ds, position, oldInfo, null, null);
    }

    protected void updateRoute1E(BigVehicle bv, DummySatellites ds, int position, double[] oldInfo
            , Customer beforeDeleteSatellite, Customer afterDeleteSatellite) {
        Cost varCost = bv.getCost();
        // 移除一个点后整条路径都不存在了,置0
        if (ds.getVehicle().getCustomers().size() == 0 && bv.getDummySatellites().size() == 1) {
            varCost.initialize();
            depot.getDeliveryVehicles().remove(bv);
            depot.getAssignedSatellites().remove(ds);
        } else {
            double variation;
            if (ds.getDelivery().getDemand() != 0) {
                // 过去、现在都在路径中，variation改变量为Service Duration改变量
                variation = ds.getDelivery().getServiceDuration() - oldInfo[1];
                double twViol = Math.max(0, ds.getDelivery().getArriveTime() - ds.getEndTw());
//                varCost.twViol += -ds.getTwViol() + twViol;
                ds.setTwViol(twViol);
            } else {
                // 过去在路径中，现在不在路径中，相当于移除出路径，variation要计算距离
                variation = -instance.getTravelTime(beforeDeleteSatellite.getId(), ds.getId())
                        - oldInfo[1] // 卫星上货物的转运时间
                        - instance.getTravelTime(ds.getId(), afterDeleteSatellite.getId())
                        + instance.getTravelTime(beforeDeleteSatellite.getId(), afterDeleteSatellite.getId());
                varCost.distance += -instance.getTravelTime(beforeDeleteSatellite.getId(), ds.getId())
                        - instance.getTravelTime(ds.getId(), afterDeleteSatellite.getId())
                        + instance.getTravelTime(beforeDeleteSatellite.getId(), afterDeleteSatellite.getId());
//                varCost.twViol += -ds.getTwViol();
            }

            for (int i = position + 1; i < bv.getSatelliteLength(); i++) {
                bv.getSatellite(i).getDelivery().arriveTime += variation;
                update2ERoute(bv.getSatellite(i), variation);
                double twViol = Math.max(0, bv.getSatellite(i).getDelivery().getArriveTime() - bv.getSatellite(i).getEndTw());
//                varCost.twViol += -bv.getSatellite(i).getTwViol() + twViol;
                bv.getSatellite(i).setTwViol(twViol);
            }

            varCost.load += -oldInfo[0] + ds.getDelivery().getDemand();
            varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));
            // 移除satellite
            if (ds.getDelivery().getDemand() == 0) {
                bv.removeSatellite(position);
                depot.getAssignedSatellites().remove(ds);
            }
        }
    }


    protected void update2ERoute(DummySatellites ds, double variation) {
        SmallVehicle sv = ds.getVehicle();
        Cost varCost = sv.getCost();

        double arriveCustomer = 0;
        double arriveNextCustomer = 0;
        double waitingTimeCustomer = 0;
        double waitingTimeNextCustomer = 0;
        double twViolCustomer = 0;
        double twViolNextCustomer = 0;

        if (sv.getCustomers().size() == 1) {
            Customer customer = sv.getCustomer(0);
            arriveCustomer = customer.getDelivery().getArriveTime() + variation;
            waitingTimeCustomer = Math.max(0, customer.getStartTw() - arriveCustomer);
            twViolCustomer = Math.max(0, arriveCustomer - customer.getEndTw());

            varCost.waitingTime = waitingTimeCustomer;
            varCost.twViol = twViolCustomer;

            customer.getDelivery().setArriveTime(arriveCustomer);
            customer.setWaitingTime(waitingTimeCustomer);
            customer.setTwViol(twViolCustomer);
        } else {
            Customer customer = sv.getCustomer(0);
            Customer customerAfter = sv.getCustomer(1);
            arriveCustomer = customer.getDelivery().getArriveTime() + variation;
            waitingTimeCustomer = Math.max(0, customer.getStartTw() - arriveCustomer);
            // time window violation of the customer if any
            twViolCustomer = Math.max(0, arriveCustomer - customer.getEndTw());
            // before arrive time at the customer after
            arriveNextCustomer = Math.max(customer.getStartTw(), arriveCustomer)
                    + customer.getDelivery().getServiceDuration()
                    + instance.getTravelTime(customer.getId(), customerAfter.getId());
            // waiting time for the customer after if any
            waitingTimeNextCustomer = Math.max(0, customerAfter.getStartTw() - arriveNextCustomer);
            // time window violation of the customer after if any
            twViolNextCustomer = Math.max(0, arriveNextCustomer - customerAfter.getEndTw());

            // variation of the waiting time
            varCost.waitingTime += customer.getWaitingTime() - customerAfter.getWaitingTime() + waitingTimeCustomer + waitingTimeNextCustomer;
            // variation of the time windows violation
            varCost.twViol += -customer.getTwViol() - customerAfter.getTwViol() + twViolCustomer + twViolNextCustomer;

            variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getDelivery().getArriveTime()
                    - customerAfter.getWaitingTime();

            customer.getDelivery().setArriveTime(arriveCustomer);
            customer.setWaitingTime(waitingTimeCustomer);
            customer.setTwViol(twViolCustomer);
            customerAfter.getDelivery().setArriveTime(arriveNextCustomer);
            customerAfter.setWaitingTime(waitingTimeNextCustomer);
            customerAfter.setTwViol(twViolNextCustomer);

            // if there is a variation update the nodes after too
            int i = 2;
            while (variation != 0 && i < sv.getCustomersLength()) {
                customerAfter = sv.getCustomer(i);
                // arrive at the customer after
                arriveNextCustomer = customerAfter.getDelivery().getArriveTime() + variation;
                waitingTimeNextCustomer = Math.max(0, customerAfter.getStartTw() - arriveNextCustomer);
                twViolNextCustomer = Math.max(0, arriveNextCustomer - customerAfter.getEndTw());
                // variation of the waiting time
                varCost.waitingTime += -customerAfter.getWaitingTime() + waitingTimeNextCustomer;
                // variation of the time windows violation
                varCost.twViol += -customerAfter.getTwViol() + twViolNextCustomer;

                variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getDelivery().getArriveTime()
                        - customerAfter.getWaitingTime();

                customerAfter.getDelivery().setArriveTime(arriveNextCustomer);
                customerAfter.setWaitingTime(waitingTimeNextCustomer);
                customerAfter.setTwViol(twViolNextCustomer);

                i++;
            } // end while
        }

        varCost.setLoadViol(Math.max(0, varCost.load - sv.getCapacity()));
    }

    protected Cost evaluateRoute1E(BigVehicle bv, DummySatellites ds,
                                   Customer beforeSatellite, Customer afterSatellite,
                                   int position, double newDemand, Cost totalCost) {
        Cost newCost = new Cost(bv.getCost());
        totalCost.cutCost(bv.getCost());

        newCost.load = bv.getCost().load - ds.getDelivery().getDemand() + newDemand;
        newCost.setLoadViol(Math.max(0, newCost.load - bv.getCapacity()));

        double variation;
        if (newDemand != 0) {
            // 过去、现在都在路径中，variation改变量为Service Duration改变量
            variation = (newDemand - ds.getDelivery().getDemand()) * instance.getPt();
        } else {
            // 过去在路径中，现在不在路径中，相当于移除出路径，variation要计算距离
            variation = -instance.getTravelTime(beforeSatellite.getId(), ds.getId())
                    - ds.getDelivery().getServiceDuration() // 卫星上货物的转运时间
                    - instance.getTravelTime(ds.getId(), afterSatellite.getId())
                    + instance.getTravelTime(beforeSatellite.getId(), afterSatellite.getId());
            newCost.distance += -instance.getTravelTime(beforeSatellite.getId(), ds.getId())
                    - instance.getTravelTime(ds.getId(), afterSatellite.getId())
                    + instance.getTravelTime(beforeSatellite.getId(), afterSatellite.getId());
        }

        double arriveTime;
        for (int i = position + 1; i < bv.getSatelliteLength(); i++) {
            arriveTime = bv.getSatellite(i).getDelivery().arriveTime + variation;
            if (arriveTime > bv.getSatellite(i).getEndTw() || bv.getSatellite(i).getDelivery().arriveTime > bv.getSatellite(i).getEndTw()) {
                newCost.addCost(evaluateRoute2E(bv.getSatellite(i), variation));
                totalCost.cutCost(bv.getSatellite(i).getVehicle().getCost());
            } //todo
        }

        return newCost;
    }


    protected Cost evaluateRoute2E(DummySatellites ds, double variation) {
        SmallVehicle sv = ds.getVehicle();
        Cost newCost = new Cost(sv.getCost());

        double arriveCustomer = 0;
        double arriveNextCustomer = 0;
        double waitingTimeCustomer = 0;
        double waitingTimeNextCustomer = 0;
        double twViolCustomer = 0;
        double twViolNextCustomer = 0;

        if (sv.getCustomers().size() == 1) {
            Customer customer = sv.getCustomer(0);
            arriveCustomer = customer.getDelivery().getArriveTime() + variation;
            waitingTimeCustomer = Math.max(0, customer.getStartTw() - arriveCustomer);
            twViolCustomer = Math.max(0, arriveCustomer - customer.getEndTw());

            newCost.twViol = twViolCustomer;
        } else {
            Customer customer = sv.getCustomer(0);
            Customer customerAfter = sv.getCustomer(1);
            arriveCustomer = customer.getDelivery().getArriveTime() + variation;
            waitingTimeCustomer = Math.max(0, customer.getStartTw() - arriveCustomer);
            // time window violation of the customer if any
            twViolCustomer = Math.max(0, arriveCustomer - customer.getEndTw());
            // before arrive time at the customer after
            arriveNextCustomer = Math.max(customer.getStartTw(), arriveCustomer)
                    + customer.getDelivery().getServiceDuration()
                    + instance.getTravelTime(customer.getId(), customerAfter.getId());
            // waiting time for the customer after if any
            waitingTimeNextCustomer = Math.max(0, customerAfter.getStartTw() - arriveNextCustomer);
            // time window violation of the customer after if any
            twViolNextCustomer = Math.max(0, arriveNextCustomer - customerAfter.getEndTw());

            // variation of the time windows violation
            newCost.twViol += -customer.getTwViol() - customerAfter.getTwViol() + twViolCustomer + twViolNextCustomer;

            variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getDelivery().getArriveTime()
                    - customerAfter.getWaitingTime();

            // if there is a variation update the nodes after too
            int i = 2;
            while (variation != 0 && i < sv.getCustomersLength()) {
                customerAfter = sv.getCustomer(i);
                // arrive at the customer after
                arriveNextCustomer = customerAfter.getDelivery().getArriveTime() + variation;
                waitingTimeNextCustomer = Math.max(0, customerAfter.getStartTw() - arriveNextCustomer);
                twViolNextCustomer = Math.max(0, arriveNextCustomer - customerAfter.getEndTw());

                // variation of the time windows violation
                newCost.twViol += -customerAfter.getTwViol() + twViolNextCustomer;

                variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getDelivery().getArriveTime()
                        - customerAfter.getWaitingTime();

                i++;
            } // end while
        }
        return newCost;
    }


    protected void updatePickRoute(BigVehicle bv, DummySatellites ds, double oldDemand) {
        if (bv == null && ds.getPickup().getDemand() == 0) {
            // 无需拾取
            return;
        } else if (bv == null && ds.getPickup().getDemand() != 0) {
            // 原先路径不存在（不需要拾取），加入2级节点后1级节点需要拾取，则新增车辆
            bv = new BigVehicle(depot);
            Cost varCost = bv.getCost();
            varCost.load += ds.getPickup().getDemand();
            varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));
            varCost.distance += instance.getTravelTime(depot.getId(), ds.getId()) + instance.getTravelTime(ds.getId(), depot.getId());
            bv.addSatellite(ds);
            ds.setBelongedPickupBigVehicle(bv);
        }

        Cost varCost = bv.getCost();

        // 原先就存在于路径中，改变后依旧存在
        if (ds.getPickup().getDemand() != 0 && oldDemand != 0) {
            varCost.load += -oldDemand + ds.getPickup().getDemand();
            varCost.setLoadViol(Math.max(0, varCost.load - bv.getCapacity()));
        } else if (ds.getPickup().getDemand() == 0 && oldDemand != 0) {
            // 原先存在于路径中，改变之后删去了
            varCost.load += -oldDemand;
            if (bv.getDummySatellites().size() == 1) {
                // 路径只有一个节点，删除后路径删除
                varCost.initialize();
                depot.getPickupVehicles().remove(bv);
            } else {
                // 路径有多个节点
                int position = 0;
                for (int i = 0; i < bv.getSatelliteLength(); i++) {
                    if (bv.getSatellite(i).getId() == ds.getId() && bv.getSatellite(i).getId2() == ds.getId2()) {
                        position = i;
                        break;
                    }
                }
                if (position == bv.getDummySatellites().size() - 1) {
                    DummySatellites satelliteBefore = bv.getSatellite(position - 1);
                    varCost.distance += -instance.getTravelTime(satelliteBefore.getId(), ds.getId())
                            - instance.getTravelTime(ds.getId(), depot.getId())
                            + instance.getTravelTime(satelliteBefore.getId(), depot.getId());
                } else {
                    DummySatellites satelliteAfter = bv.getSatellite(position + 1);
                    if (position == 0) {
                        // variation of the travel time
                        varCost.distance += -instance.getTravelTime(depot.getId(), ds.getId())
                                - instance.getTravelTime(ds.getId(), satelliteAfter.getId())
                                + instance.getTravelTime(depot.getId(), satelliteAfter.getId());

                        // insertion in the middle of the list
                    } else {
                        DummySatellites satelliteBefore = bv.getSatellite(position - 1);
                        varCost.distance += -instance.getTravelTime(satelliteBefore.getId(), ds.getId())
                                - instance.getTravelTime(ds.getId(), satelliteAfter.getId())
                                + instance.getTravelTime(satelliteBefore.getId(), satelliteAfter.getId());
                    }
                }
                bv.getDummySatellites().remove(ds);
                ds.setBelongedPickupBigVehicle(null);
            }
        }
    }

    protected Cost evaluatePickRoute(BigVehicle bv, DummySatellites ds, double newDemand, Cost totalCost) {

        if (newDemand == 0 && ds.getPickup().getDemand() == 0) {
            // 无需拾取
            return new Cost();
        } else if (newDemand != 0 && ds.getPickup().getDemand() == 0) {
            // 原先路径不存在（不需要拾取），加入2级节点后1级节点需要拾取，则新增车辆
            Cost newCost = new Cost();
            newCost.load += newDemand;
            newCost.setLoadViol(Math.max(0, newCost.load - instance.getCap1E()));
            newCost.distance += instance.getTravelTime(depot.getId(), ds.getId()) + instance.getTravelTime(ds.getId(), depot.getId());
            return newCost;
        }

        Cost newCost = new Cost(bv.getCost());
        totalCost.cutCost(bv.getCost());

        // 原先就存在于路径中，改变后依旧存在
        if (newDemand != 0 && ds.getPickup().demand != 0) {
            newCost.load += -ds.getPickup().demand + newDemand;
            newCost.setLoadViol(Math.max(0, newCost.load - bv.getCapacity()));
        } else if (newDemand == 0 && ds.getPickup().demand != 0) {
            // 原先存在于路径中，改变之后删去了
            newCost.load += -ds.getPickup().demand;
            if (bv.getDummySatellites().size() == 1) {
                // 路径只有一个节点，删除后路径删除
                newCost.initialize();
            } else {
                // 路径有多个节点
                int position = 0;
                for (int i = 0; i < bv.getSatelliteLength(); i++) {
                    if (bv.getSatellite(i).getId() == ds.getId() && bv.getSatellite(i).getId2() == ds.getId2()) {
                        position = i;
                        break;
                    }
                }
                if (position == bv.getDummySatellites().size() - 1) {
                    DummySatellites satelliteBefore = bv.getSatellite(position - 1);
                    newCost.distance += -instance.getTravelTime(satelliteBefore.getId(), ds.getId())
                            - instance.getTravelTime(ds.getId(), depot.getId())
                            + instance.getTravelTime(satelliteBefore.getId(), depot.getId());
                } else {
                    DummySatellites satelliteAfter = bv.getSatellite(position + 1);
                    if (position == 0) {
                        // variation of the travel time
                        newCost.distance += -instance.getTravelTime(depot.getId(), ds.getId())
                                - instance.getTravelTime(ds.getId(), satelliteAfter.getId())
                                + instance.getTravelTime(depot.getId(), satelliteAfter.getId());

                        // insertion in the middle of the list
                    } else {
                        DummySatellites satelliteBefore = bv.getSatellite(position - 1);
                        newCost.distance += -instance.getTravelTime(satelliteBefore.getId(), ds.getId())
                                - instance.getTravelTime(ds.getId(), satelliteAfter.getId())
                                + instance.getTravelTime(satelliteBefore.getId(), satelliteAfter.getId());
                    }
                }
            }
        }
        return newCost;
    }

    protected Cost evaluateIntraPickRoute(BigVehicle bv, DummySatellites ds1, DummySatellites ds2, double newDemand1, double newDemand2, Cost totalCost) {
        if (bv == null) {
            // 无需拾取
            return new Cost();
        }

        Cost newCost = new Cost(bv.getCost());
        totalCost.cutCost(bv.getCost());
        DummySatellites ds;

        // 原先都存在于路径中，改变后依旧存在，则无需改变
        if (newDemand1 != 0 && newDemand2 != 0) {

        } else {
            // 原先都存在于路径中，改变之后有一个点删去了
            if (newDemand1 == 0)
                ds = ds1;
            else
                ds = ds2;
            int position = 0;
            for (int i = 0; i < bv.getSatelliteLength(); i++) {
                if (bv.getSatellite(i).getId() == ds.getId() && bv.getSatellite(i).getId2() == ds.getId2()) {
                    position = i;
                    break;
                }
            }
            if (position == bv.getDummySatellites().size() - 1) {
                DummySatellites satelliteBefore = bv.getSatellite(position - 1);
                newCost.distance += -instance.getTravelTime(satelliteBefore.getId(), ds.getId())
                        - instance.getTravelTime(ds.getId(), depot.getId())
                        + instance.getTravelTime(satelliteBefore.getId(), depot.getId());
            } else {
                DummySatellites satelliteAfter = bv.getSatellite(position + 1);
                if (position == 0) {
                    // variation of the travel time
                    newCost.distance += -instance.getTravelTime(depot.getId(), ds.getId())
                            - instance.getTravelTime(ds.getId(), satelliteAfter.getId())
                            + instance.getTravelTime(depot.getId(), satelliteAfter.getId());

                    // insertion in the middle of the list
                } else {
                    DummySatellites satelliteBefore = bv.getSatellite(position - 1);
                    newCost.distance += -instance.getTravelTime(satelliteBefore.getId(), ds.getId())
                            - instance.getTravelTime(ds.getId(), satelliteAfter.getId())
                            + instance.getTravelTime(satelliteBefore.getId(), satelliteAfter.getId());
                }
            }
        }

        return newCost;
    }

    protected void updateTotalCost(Solution sol,
                                   Cost iniCost1E, Cost iniCost2E, Cost iniCost3E,
                                   Cost newCost1E, Cost newCost2E, Cost newCost3E) {
        sol.getCostForDelivery1E().cutCost(iniCost1E);
        sol.getCostForDelivery1E().addCost(newCost1E);
        sol.getCostForDelivery2E().cutCost(iniCost2E);
        sol.getCostForDelivery2E().addCost(newCost2E);
        sol.getCostForPickup1E().cutCost(iniCost3E);
        sol.getCostForPickup1E().addCost(newCost3E);
        sol.addToTotal();
    }

    protected void updateTotalCost(Solution sol,
                                   Cost iniCost1E, Cost iniCost2E,
                                   Cost newCost1E, Cost newCost2E) {
        sol.getCostForDelivery1E().cutCost(iniCost1E);
        sol.getCostForDelivery1E().addCost(newCost1E);
        sol.getCostForDelivery2E().cutCost(iniCost2E);
        sol.getCostForDelivery2E().addCost(newCost2E);
        sol.addToTotal();
    }

    protected void updateTotalCost(Solution sol,
                                   Cost iniCost3E,
                                   Cost newCost3E) {
        sol.getCostForPickup1E().cutCost(iniCost3E);
        sol.getCostForPickup1E().addCost(newCost3E);
        sol.addToTotal();
    }

    public static MyMove findBestMove(List<MyMove> moves, Random r) {
        Collections.sort(moves, new Comparator<MyMove>() {
            @Override
            public int compare(MyMove o1, MyMove o2) {
                // 返回值为int类型，大于0表示正序，小于0表示逆序
                if (o1.getObjVal() - o2.getObjVal() > 0)
                    return 1;
                else if (o1.getObjVal() == o2.getObjVal())
                    return 0;
                else
                    return -1;
            }
        });
        if (moves.size() == 0)
            return null;

        ArrayList<MyMove> ms = new ArrayList<>();
        for (MyMove move : moves) {
            if (move.getObjVal() == moves.get(0).getObjVal()) {
                ms.add(move);
            } else {
                break;
            }
        }

        return ms.get(r.nextInt(ms.size()));
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public TabuArc[] getArcs() {
        return arcs;
    }

    public void setArcs(TabuArc[] arcs) {
        this.arcs = arcs;
    }

    public double getObjVal() {
        return objVal;
    }

    public void setObjVal(double objVal) {
        this.objVal = objVal;
    }

    @Override
    public int compareTo(MyMove o) {
        if (o.getObjVal() - objVal > 0)
            return 1;
        else {
            return 0;
        }
    }
}
