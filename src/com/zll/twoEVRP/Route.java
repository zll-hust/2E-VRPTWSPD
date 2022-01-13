package com.zll.twoEVRP;

import java.util.ArrayList;

public class Route {
    protected int index;
    protected int startingTime;
    protected Cost cost;
    protected int capacity; // 车辆容量
    private static int id = 0;

    public Route() {
    	this.index = id++;
    	cost = new Cost();
    	this.startingTime = 0;
    	this.capacity = 0;
    }

    public Route(Route route) {
        this.index = route.index;
        this.cost = new Cost(route.cost);
        this.capacity = route.capacity;
        this.startingTime = route.startingTime;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
    }

    public int getIndex() {
        return index;
    }

    public Cost getCost() {
        return this.cost;
    }

    public void initializeTimes() {
        cost.initialize();
    }

    public int getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(int startingTime) {
        this.startingTime = startingTime;
    }
}

