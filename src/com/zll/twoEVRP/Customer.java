package com.zll.twoEVRP;

public class Customer {
    protected int id;                    // 序号
    protected double xCoordinate;            // x坐标
    protected double yCoordinate;            // y坐标
    protected double startTw;                // beginning of time window (earliest time for start of service),if any
    protected double endTw;                  // end of time window (latest time for start of service), if any
    protected double waitingTime;         // time to wait until arriveTime equal start time window
    protected double twViol;              // value of time window violation, 0 if none
    protected double[] anglesToDepots;    // 与仓库的角度

    protected Travel pickup;
    protected Travel delivery;

    protected int id2 = 0; // dummy的id,对depot和customer而言,永远为0

    public Customer() {
        xCoordinate = 0;
        yCoordinate = 0;
        startTw = 0;
        endTw = 0;
        waitingTime = 0;
        twViol = 0;

        pickup = new Travel();
        delivery = new Travel();
    }

    public Customer(Customer customer) {
        this.id = customer.id;
        this.xCoordinate = customer.xCoordinate;
        this.yCoordinate = customer.yCoordinate;
        this.startTw = customer.startTw;
        this.endTw = customer.endTw;
        this.waitingTime = new Double(customer.waitingTime);
        this.twViol = new Double(customer.twViol);
        this.delivery = new Travel(customer.delivery);
        this.pickup = new Travel(customer.pickup);
    }

    public String toString() {
        StringBuffer print = new StringBuffer();
        print.append("\n\t\t" + "Customer " + id + "[");
        print.append("\n\t\t" + "| x=" + xCoordinate + " y=" + yCoordinate);
        print.append("\n\t\t" + "| ServiceDuration=" + delivery.serviceDuration + " Demand=" + delivery.demand);
        print.append("\n\t\t" + "| StartTimeWindow=" + startTw + " EndTimeWindow=" + endTw);
        print.append("\n\t\t" + "| arrive time=" + getDelivery().arriveTime + " waiting time=" + waitingTime + " time window violation =" + twViol);
        print.append("]");
        return print.toString();

    }

    public int getId() {
        return this.id;
    }

    public void setId(int customernumber) {
        this.id = customernumber;
    }

    public double getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(double xcoordinate) {
        this.xCoordinate = xcoordinate;
    }

    public double getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(double ycoordinate) {
        this.yCoordinate = ycoordinate;
    }


    public double getStartTw() {
        return startTw;
    }

    public void setStartTw(int startTW) {
        this.startTw = startTW;
    }

    public double getEndTw() {
        return endTw;
    }

    public void setEndTw(double endTW) {
        this.endTw = endTW;
    }

    public double getAngleToDepot(int depotnr) {
        return anglesToDepots[depotnr];
    }

    public double[] getAnglesToDepots() {
        return anglesToDepots;
    }

    public void setAnglesToDepots(double[] anglestodepots) {
        this.anglesToDepots = anglestodepots;
    }

    public double getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(double waitingTime) {
        this.waitingTime = waitingTime;
    }

    public double getTwViol() {
        return twViol;
    }

    public void setTwViol(double twViol) {
        this.twViol = twViol;
    }

    public Travel getPickup() {
        return pickup;
    }

    public void setPickup(Travel pickup) {
        this.pickup = pickup;
    }

    public Travel getDelivery() {
        return delivery;
    }

    public void setDelivery(Travel delivery) {
        this.delivery = delivery;
    }

    public int getId2() {
        return id2;
    }

    public void setId2(int id2) {
        this.id2 = id2;
    }
}
