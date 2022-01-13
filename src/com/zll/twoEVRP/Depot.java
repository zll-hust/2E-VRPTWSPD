package com.zll.twoEVRP;

import java.util.ArrayList;

public class Depot extends Customer{
	private ArrayList<BigVehicle> deliveryVehicles;
	private ArrayList<BigVehicle> pickupVehicles;
	private ArrayList<DummySatellites> assignedSatellites;
	protected int capacity;


	public Depot(){
		super();
		this.capacity = -1;
		this.assignedSatellites = new ArrayList<DummySatellites>();
		this.deliveryVehicles = new ArrayList<BigVehicle>();
		this.pickupVehicles = new ArrayList<BigVehicle>();
	}

	public Depot(Depot depot){
		super(depot);
		this.capacity = depot.capacity;

		this.assignedSatellites = new ArrayList<>();
		for(DummySatellites ds : depot.getAssignedSatellites())
			assignedSatellites.add(new DummySatellites(ds));

		this.deliveryVehicles = new ArrayList<>();
		this.pickupVehicles = new ArrayList<>();
		
		for(BigVehicle bv : depot.getDeliveryVehicles()){
			BigVehicle nbv = new BigVehicle(bv);
			
			for (int j = 0; j < bv.getDummySatellites().size(); ++j) {
				for(int i = 0; i < assignedSatellites.size(); i++) {
					if(bv.getSatellite(j).getId2() == assignedSatellites.get(i).getId2() &&
							bv.getSatellite(j).getId() == assignedSatellites.get(i).getId()) {
						assignedSatellites.get(i).setBelongedDeliveryBigVehicle(nbv);
						nbv.getDummySatellites().add(assignedSatellites.get(i));
						break;
					}
				}
			}
			nbv.setDepot(this);
			deliveryVehicles.add(nbv);
		}
		
		for(BigVehicle bv : depot.getPickupVehicles()){
			BigVehicle nbv = new BigVehicle(bv);
			
			for (int j = 0; j < bv.getDummySatellites().size(); ++j) {
				for(int i = 0; i < assignedSatellites.size(); i++) {
					if(bv.getSatellite(j).getId2() == assignedSatellites.get(i).getId2() &&
							bv.getSatellite(j).getId() == assignedSatellites.get(i).getId()) {
						assignedSatellites.get(i).setBelongedPickupBigVehicle(nbv);
						nbv.getDummySatellites().add(assignedSatellites.get(i));
						break;
					}
				}
			}
			nbv.setDepot(this);
			pickupVehicles.add(nbv);
		}
	}

	public String toString() {
		StringBuffer print = new StringBuffer();
		for(int i = 0; i < deliveryVehicles.size(); i++){
			print.append("\nRoute delivery " + i + " : " + deliveryVehicles.get(i));
			for(int j = 0; j < 30; j++)
				print.append("-");
		}

		int count = 0;
		for(BigVehicle bv : pickupVehicles){
			print.append("\nRoute pickup " + count++ + " : ");
			print.append("\nsatellite number : " + bv.getSatelliteLength() + " =");
			print.append(" " + this.getId());
			for (int i = 0; i < bv.getDummySatellites().size(); ++i){
				print.append(" " + bv.getDummySatellites().get(i).getId() + "-" + bv.getDummySatellites().get(i).getId2());
			}
			print.append(" " + this.getId());
			for (int i = 0; i < bv.getDummySatellites().size(); ++i){
				DummySatellites d = bv.getDummySatellites().get(i);
				print.append("\n\t DummySatellites " + d.id + "-" + d.getId2() +
						" [\n\t x=" + d.getXCoordinate() + " y=" + d.getYCoordinate() +
						"\n\t arrive time= " + d.getDelivery().arriveTime +
						" ]");
			}
			print.append("\ntotal cost in vehicle: " + bv.getCost());
			print.append("\n--------------------------------------------------");
		}

		return print.toString();
	}

	public void addSatellites(DummySatellites s){this.assignedSatellites.add(s);}

	public void addDeliveryRoute(BigVehicle vehicle){this.deliveryVehicles.add(vehicle);}

	public ArrayList<BigVehicle> getDeliveryVehicles() {
		return deliveryVehicles;
	}

	public void setDeliveryVehicles(ArrayList<BigVehicle> vehicles) {
		this.deliveryVehicles = vehicles;
	}

	public void addPickupRoute(BigVehicle vehicle){this.pickupVehicles.add(vehicle);}

	public ArrayList<BigVehicle> getPickupVehicles() {
		return pickupVehicles;
	}

	public void setPickupVehicles(ArrayList<BigVehicle> pickupVehicles) {
		this.pickupVehicles = pickupVehicles;
	}

	public ArrayList<DummySatellites> getAssignedSatellites() {
		return assignedSatellites;
	}

	public void setAssignedSatellites(ArrayList<DummySatellites> assignedSatellites) {
		this.assignedSatellites = assignedSatellites;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
}