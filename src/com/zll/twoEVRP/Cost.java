package com.zll.twoEVRP;

public class Cost {
	public static double Alpha = 5;//5
	public static double Beta = 5;//5
	public static double Sita = 0.01;//0

	public double total;			 // sum of all the costs
	public double distance;
	public double load;			     // sum of all quantities

	public double time; // 只在greedy里使用一次,不进行复制

	public double waitingTime;		 // sum of all waiting times when arrives before start TW
	public double loadViol;		     // violation of the load
	public double twViol;            // violation of the time window


	public Cost(){
		this.total = 0;
		this.distance = 0;
		this.load = 0;
		this.waitingTime = 0;
		this.loadViol = 0;
		this.twViol = 0;
	}

	public Cost(Cost cost) {
		this.total = new Double(cost.total);
		this.distance = new Double(cost.distance);
		this.load = new Double(cost.load);
		this.waitingTime = new Double(cost.waitingTime);
		this.loadViol = new Double(cost.loadViol);
		this.twViol = new Double(cost.twViol);
	}

	public void initialize() {
		this.total = 0;
		this.distance = 0;
		this.load = 0;
		this.waitingTime = 0;
		this.loadViol = 0;
		this.twViol = 0;
	}

	public void addAndCutCost(Cost add, Cost add2, Cost cut, Cost cut2){
		addCost(add);
		addCost(add2);
		cutCost(cut);
		cutCost(cut2);
		calculateTotal();
	}

	public void addCost(Cost cost) {
		this.distance += cost.distance;
		this.load += cost.load;
		this.waitingTime += cost.waitingTime;
		this.loadViol += cost.loadViol;
		this.twViol += cost.twViol;
	}

	public void cutCost(Cost cost) {
		this.distance -= cost.distance;
		this.load -= cost.load;
		this.waitingTime -= cost.waitingTime;
		this.loadViol -= cost.loadViol;
		this.twViol -= cost.twViol;
	}


	public String toString() {
		StringBuffer print = new StringBuffer();//可以删增的字符串
		print.append("\n" + "| Cost =" + total);
		print.append("\n" + "| Distance =" + distance + " Load =" + load);
		print.append("\n" + "| LoadViol=" + loadViol + " TWViol=" + twViol);
		return print.toString();
	}

	public void calculateTotal() {
		total = Alpha * loadViol + distance + Beta * twViol;
//		if(loadViol > 0 || twViol > 0)
//			total *= 2;
	}

	public void updatePara(){
		if ( loadViol == 0 && Alpha >= 1 )
			Alpha /= ( 1 + Sita );
		else if ( loadViol != 0 && Alpha <= 100 )
			Alpha *= ( 1 + Sita );

		if ( twViol == 0 && Beta >= 1 )
			Beta /= ( 1 + Sita );
		else if ( twViol != 0 && Beta <= 100 )
			Beta *= ( 1 + Sita );
	}

	public void setLoadViol(double capacityviol) {
		this.loadViol = capacityviol;
	}

	public void setDistance(double durationviol) {
		this.distance = durationviol;
	}

	public void addLoadViol(double capacityviol) {
		this.loadViol += capacityviol;
	}

	public void addDurationViol(double durationviol) {
		this.distance += durationviol;
	}

	public void addTWViol(double TWviol) {
		this.twViol += TWviol;
	}

	public double getTotal() {
		return total;
	}

	public double getLoadViol() {
		return loadViol;
	}

	public double getDistance() {
		return distance;
	}

	public double getTwViol() {
		return twViol;
	}

    public boolean checkFeasible() {
    	if (this.loadViol == 0 && this.twViol == 0) {
    		return true;
    	} else {
    		return false;
    	}
    }

	public double getLoad() {
		return load;
	}

	public void setLoad(double load) {
		this.load = load;
	}

	public void addLoad(double load) {
		this.load += load;
	}

	public double getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(double waitingTime) {
		this.waitingTime = waitingTime;
	}

	public void addWaitingTime(double waitingTime) {
		this.waitingTime += waitingTime;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public void setTwViol(double twViol) {
		this.twViol = twViol;
	}

	public void addDistance(double distance){this.distance += distance;}

	public void addTwViol(double twViol) {
		this.twViol += twViol;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}
}
