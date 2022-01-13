package com.zll.DataSet;

import java.util.Objects;
import java.util.Random;

/**
 * @author： zll-hust
 * @date： 2021/1/20 8:48
 * @description： TODO
 */
public class Node {
    protected int xCoordinate;
    protected int yCoordinate;
    protected int startTw;
    protected int endTw;
    protected int deliveryDemand;
    protected int pickupDemand;

    public Node() {

    }

    public Node(int xCoordinate, int yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    public void generateCaNode(Random r, int x, int y){
        xCoordinate = x;
        yCoordinate = y;
        startTw = r.nextInt(500) + 300;
        endTw = r.nextInt(50) + 20 + startTw;
        if(r.nextDouble() < 0.5){
            deliveryDemand = 20;
        }else{
            deliveryDemand = 10;
        }
        pickupDemand = r.nextInt(deliveryDemand);
    }

    public void generateCbNode(Random r, int x, int y){
        xCoordinate = x;
        yCoordinate = y;
        startTw = r.nextInt(240) + 20;
        endTw = r.nextInt(20) + startTw;
        deliveryDemand = r.nextInt(20) + 5;
        pickupDemand = r.nextInt(deliveryDemand);
    }

    public void generateCcNode(Random r, int x, int y){
        xCoordinate = x;
        yCoordinate = y;
        startTw = r.nextInt(300) + 60;
        endTw = r.nextInt(90) + startTw;
        if(r.nextDouble() < 0.5){
            deliveryDemand = 20;
        }else{
            deliveryDemand = 10;
        }
        pickupDemand = r.nextInt(deliveryDemand);
    }

    public void generateCdNode(Random r, int x, int y){
        xCoordinate = x;
        yCoordinate = y;
        startTw = r.nextInt(300) + 60;
        endTw = r.nextInt(20) + startTw;
        if(r.nextDouble() < 0.5){
            deliveryDemand = 20;
        }else{
            deliveryDemand = 10;
        }
        pickupDemand = r.nextInt(deliveryDemand);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return xCoordinate == node.xCoordinate &&
                yCoordinate == node.yCoordinate;
    }


    public int getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public int getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public int getStartTw() {
        return startTw;
    }

    public void setStartTw(int startTw) {
        this.startTw = startTw;
    }

    public int getEndTw() {
        return endTw;
    }

    public void setEndTw(int endTw) {
        this.endTw = endTw;
    }

    public int getDeliveryDemand() {
        return deliveryDemand;
    }

    public void setDeliveryDemand(int deliveryDemand) {
        this.deliveryDemand = deliveryDemand;
    }

    public int getPickupDemand() {
        return pickupDemand;
    }

    public void setPickupDemand(int pickupDemand) {
        this.pickupDemand = pickupDemand;
    }
}
