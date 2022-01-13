package com.zll.twoEVRP;

/**
 * This class takes calculates duration in hours,minutes,seconds and milliseconds.
 * Usage may be by setting the values of start and end times or duration or by calling
 * start and stop which gets the system time in those moments.
 * @author zll_hust
 *
 */
public class Duration {
    private long startTime 		= 0;
    private long endTime 		= 0;
    private long duration 		= 0;
    private long milliSeconds 	= 0;
	private long seconds 		= 0;
	private long minutes 		= 0;
	private long hours 			= 0;

	/**
	 * Constructor
	 */
	public Duration(){}
	
	/**
	 * Constructor with duration passed as argument
	 * @param duration
	 */
	public Duration(long duration){
    	this.duration = duration;
    	updateDuration();
    	
    }
	
	/**
	 * This function updates hours, minutes, seconds and milliseconds based on duration.
	 * Call this function each time duration changes
	 */
	private void updateDuration(){
		this.milliSeconds 	= this.duration % 1000;
        this.seconds 		= this.duration / 1000 % 60;         
        this.minutes 		= this.duration / (60 * 1000) % 60;          
        this.hours 			= this.duration / (60 * 60 * 1000);
	}

	/**
	 * This function sets duration
	 * @param duration
	 */
	public void setDuration(long duration){
		this.duration = duration;
		updateDuration();
	}
	
	/**
	 * This function takes start and end time, set them and duration.
	 * @param startTime
	 * @param endTime
	 */
	public void setDuration(long startTime, long endTime){
		this.startTime 	= startTime;
		this.endTime 	= endTime;
		this.duration 	= endTime - endTime;
		updateDuration();
	}
	
	/**
	 * Sets the start time to the actual current time
	 */
	public void start(){
		this.startTime = System.currentTimeMillis();
	}
	
	/**
	 * Sets the end time to the actual current time
	 */
	public void stop(){
		this.endTime = System.currentTimeMillis();
		this.duration = this.endTime - this.startTime;
		updateDuration();
	}
	
	/**
	 * Sets the start time to the given parameter
	 * @param startTime
	 */
	public void setStartTime(long startTime){
		this.startTime = startTime;
	}
	
	/**
	 * Sets the end time to the given parameter and calculate duration 
	 * @param endTime
	 */
	public void setEndTime(long endTime){
		this.endTime = endTime;
		this.duration = endTime - startTime;
		updateDuration();
	}
	
	/**
	 * Resets all the counters
	 */
	public void reset(){
		this.startTime = 0;
		this.endTime = 0;
		this.duration = 0;
		this.hours = 0;
		this.minutes = 0;
		this.seconds = 0;
		this.milliSeconds = 0;
	}
	
	/**
	 * Print duration formated as [HH:MM:SS:ms]
	 */
	public String printDoted(){
		StringBuffer print = new StringBuffer();
		print.append(this.hours + ":");
		print.append(this.minutes + ":");
		print.append(this.seconds + ":");
		print.append(this.milliSeconds);
		return print.toString();
	}
	
	/**
	 * Print duration formated as [MM:SS]
	 * @return
	 */
	public String printMinSec(){
		StringBuffer print = new StringBuffer();
		print.append(minutes + ":" + seconds);
		return print.toString();
	}

	/**
	 * Print duration formated as hours minutes seconds milliseconds
	 */
	public String toString(){
		StringBuffer print = new StringBuffer();
		print.append(this.hours + "h ");
		print.append(this.minutes + "m ");
		print.append(this.seconds + "s ");
		print.append(this.milliSeconds + "ms");
		return print.toString();
	}
	/**
	 * @return the miliSeconds
	 */
	public long getMilliSeconds() {
		return milliSeconds;
	}

	/**
	 * @return the seconds
	 */
	public long getSeconds() {
		return seconds;
	}

	/**
	 * @return the minutes
	 */
	public long getMinutes() {
		return minutes;
	}

	/**
	 * @return the hours
	 */
	public long getHours() {
		return hours;
	}
}
