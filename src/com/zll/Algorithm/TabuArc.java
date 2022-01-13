package com.zll.Algorithm;

class TabuArc {

    private int from1;
    private int from2;
    private int to1;
    private int to2;

    public TabuArc(int from1, int from2, int to1, int to2) {
        this.from1 = from1;
        this.from2 = from2;
        this.to1 = to1;
        this.to2 = to2;
    }

    public TabuArc(TabuArc ta) {
        this.from1 = ta.from1;
        this.from2 = ta.from2;
        this.to1 = ta.to1;
        this.to2 = ta.to2;
    }

    public int getFrom1() {
        return from1;
    }

    public void setFrom1(int from1) {
        this.from1 = from1;
    }

    public int getFrom2() {
        return from2;
    }

    public void setFrom2(int from2) {
        this.from2 = from2;
    }

    public int getTo1() {
        return to1;
    }

    public void setTo1(int to1) {
        this.to1 = to1;
    }

    public int getTo2() {
        return to2;
    }

    public void setTo2(int to2) {
        this.to2 = to2;
    }
}
