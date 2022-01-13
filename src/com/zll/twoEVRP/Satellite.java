package com.zll.twoEVRP;

import java.util.ArrayList;

/**
 * Description:
 * 
 * @author zll-hust E-mail:zh20010728@126.com
 * @date 创建时间：2020年9月10日 下午3:50:34
 * @encode UTF-8
 */
public class Satellite extends Customer{
	protected int capacity; // satellites上小车的容量
	private ArrayList<SmallVehicle> vehicles;
	private ArrayList<Customer> assignedCustomers; // the list of customers that are assigned to this depot

	public Satellite() {
		super();
		this.capacity = -1;
		this.assignedCustomers = new ArrayList<Customer>();
		this.vehicles = new ArrayList<SmallVehicle>();
	}

	public Satellite(Satellite satellite){
		super(satellite);
		this.capacity = satellite.capacity;
		this.assignedCustomers = new ArrayList<Customer>();
		for(Customer c : satellite.getAssignedCustomers()){
			Customer nc = new Customer(c);
			assignedCustomers.add(nc);
		}
		this.vehicles = new ArrayList<SmallVehicle>();
		for(SmallVehicle sv : satellite.getVehicles())
			vehicles.add(new SmallVehicle(sv));
	}

	public String toString() {
		StringBuffer print = new StringBuffer();
		print.append("\n");
		print.append("\n" + "--- Satellite " + id + " -------------------------------------");
		print.append("\n" + "| x=" + xCoordinate + " y=" + yCoordinate);
		print.append("\n" + "| Capacity=" + capacity);
		print.append("\n" + "| AssignedCustomers: ");
		for (Customer c : assignedCustomers)
			print.append(c.getId() + " ; ");
		print.append("\n" + "| Small Vehicle in this satellite: ");
		for (int i = 0; i < vehicles.size(); ++i){
			print.append("\n" + vehicles.get(i));
		}
		print.append("\n" + "--------------------------------------------------");
		return print.toString();
	}

	/**
	 * 
	 * @return the list of assigned customers to depot in a string
	 */
	public String printAssignedCustomers() {
		StringBuffer print = new StringBuffer();
		print.append("\n" + "AssignedCustomers=");
		for (Customer customer : assignedCustomers) {
			print.append(" " + customer.getId());
		}
		print.append("\n");
		return print.toString();
	}

	/**
	 * 
	 * @param index
	 * @return the formated string with the angles of assigned customers to depot
	 */
	public String printAssignedCustomersAngles(int index) {
		StringBuffer print = new StringBuffer();
		print.append("\nDepot[" + index + "]---AssignedCustomers-------------\nCustomerNumber\t\tCustomerAngle\n");
		for (Customer customer : assignedCustomers) {
			print.append("\t" + customer.getId() + "\t\t\t\t" + customer.getAngleToDepot(index) + "\n");
		}
		print.append("---------------------------------------------------\n");
		return print.toString();
	}

	public int getAssignedCustomersNr() {
		return assignedCustomers.size();
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public void addAssignedCustomer(Customer customer) {
		assignedCustomers.add(customer);
	}

	public Customer getAssignedCustomer(int index) {
		return assignedCustomers.get(index);
	}

	public ArrayList<Customer> getAssignedCustomers() {
		return assignedCustomers;
	}

	public ArrayList<Customer> getAssignedcustomers() {
		return assignedCustomers;
	}

	public void setAssignedcustomers(ArrayList<Customer> assignedCustomers) {
		this.assignedCustomers = assignedCustomers;
	}

	public void addAssiginedCustomers(Customer c){this.assignedCustomers.add(c);}

	public int getCustomersNr(){return this.assignedCustomers.size();}

	public void addRoute(SmallVehicle vehicle){this.vehicles.add(vehicle);}

	public ArrayList<SmallVehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(ArrayList<SmallVehicle> vehicles) {
		this.vehicles = vehicles;
	}
}
