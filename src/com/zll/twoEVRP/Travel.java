package com.zll.twoEVRP;

/**
 * @author： zll-hust
 * @description： 客户点需要服务的数据
 */
public class Travel{
    public int demand;
    public double arriveTime;
    public double serviceDuration;

    public Travel(){
        demand = 0;
        arriveTime = 0;
        serviceDuration = 0;
    }

    public Travel(Travel t){
        demand = t.demand;
        arriveTime = t.arriveTime;
        serviceDuration = t.serviceDuration;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public double getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(double arriveTime) {
        this.arriveTime = arriveTime;
    }

    public double getServiceDuration() {
        return serviceDuration;
    }

    public void setServiceDuration(double serviceDuration) {
        this.serviceDuration = serviceDuration;
    }
}