package com.zll.twoEVRP;

import java.util.ArrayList;
import java.util.List;

public class SmallVehicle extends Route {
    private DummySatellites dummySatellite;
    private List<Customer> customers;

    public SmallVehicle(Satellite s){
        super();
        this.capacity = s.getCapacity();
        customers = new ArrayList<>();
    }

    public SmallVehicle(SmallVehicle smallVehicle) {
        super(smallVehicle);
        this.customers = new ArrayList<Customer>();
        for (int i = 0; i < smallVehicle.customers.size(); ++i) {
            this.customers.add(new Customer(smallVehicle.getCustomer(i)));
        }
    }

    public Customer getCustomer(int index) {
        return this.customers.get(index);
    }

    public void removeCustomer(int index) {
        this.customers.remove(index);
    }

    public Customer getLastCustomer() {
        return this.customers.get(customers.size() - 1);
    }

    public Customer getFirstCustomer() {
        return this.customers.get(0);
    }

    public boolean isEmpty() {
        if (getCustomersLength() > 0)
            return false;
        else
            return true;
    }

    public int getCustomerIdWithIndex(int index) {
        return this.customers.get(index).getId();
    }

    public String toString() {
        StringBuffer print = new StringBuffer();
        print.append("[ customers number : " + getCustomersLength() + ", route: ");
        print.append(" " + this.dummySatellite.getId());
        for (int i = 0; i < this.customers.size(); ++i)
            print.append(" " + this.customers.get(i).getId());
        print.append(" " + this.dummySatellite.getId() + " {");
        for (int i = 0; i < this.customers.size(); ++i){
            print.append(this.customers.get(i));
        }
        print.append(" }\n\t 2-e route cost: " + cost + "]");
        return print.toString();
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    public void addCustomer(Customer customer) {
        this.customers.add(customer);
    }

    public void addCustomer(Customer customer, int index) {
        this.customers.add(index, customer);
    }

    public ArrayList<Customer> getCustomers() {
        return (ArrayList)this.customers;
    }

    public int getIndex() {
        return index;
    }

    public int getCustomersLength() {
        return this.customers.size();
    }

    public DummySatellites getDummySatellite() {
        return dummySatellite;
    }

    public void setDummySatellite(DummySatellites dummySatellite) {
        this.dummySatellite = dummySatellite;
    }
}
