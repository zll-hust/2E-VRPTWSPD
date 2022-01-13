package com.zll.twoEVRP;

import java.util.ArrayList;

public class DummySatellites extends Satellite{
    private SmallVehicle vehicle;
    private BigVehicle belongedDeliveryBigVehicle;
    private BigVehicle belongedPickupBigVehicle;

    public DummySatellites() {
        super();
    }

    public DummySatellites(Satellite satellite) {
        this.id = satellite.id;
        this.xCoordinate = satellite.xCoordinate;
        this.yCoordinate = satellite.yCoordinate;
        this.capacity = satellite.capacity;
    }

    public DummySatellites(DummySatellites dummySatellites) {
        super(dummySatellites);
        this.id2 = dummySatellites.id2;
        this.vehicle = new SmallVehicle(dummySatellites.vehicle);
        this.vehicle.setDummySatellite(this);
    }

    public double[] getOldInfo(){
        double[] info = new double[4];
        info[0] = this.getDelivery().getDemand();
        info[1] = this.getDelivery().serviceDuration;
        info[2] = this.waitingTime;
        info[3] = this.twViol;

        return info;
    }

    @Override
    public String toString() {
        return "\n\t DummySatellites " + id + "-" + id2 +
                "[\n\t x=" + xCoordinate + " y=" + yCoordinate +
                "\n\t arrive time= " + getDelivery().arriveTime +
                "\n\t end time window= " + getEndTw() +
                "\n\t vehicle= " + vehicle +
                " ]";
    }

    public SmallVehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(SmallVehicle vehicle) {
        this.vehicle = vehicle;
    }

    public BigVehicle getBelongedDeliveryBigVehicle() {
        return belongedDeliveryBigVehicle;
    }

    public void setBelongedDeliveryBigVehicle(BigVehicle belongedDeliveryBigVehicle) {
        this.belongedDeliveryBigVehicle = belongedDeliveryBigVehicle;
    }

    public BigVehicle getBelongedPickupBigVehicle() {
        return belongedPickupBigVehicle;
    }

    public void setBelongedPickupBigVehicle(BigVehicle belongedPickupBigVehicle) {
        this.belongedPickupBigVehicle = belongedPickupBigVehicle;
    }
}
