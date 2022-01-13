package com.zll.twoEVRP;

import java.util.ArrayList;
import java.util.List;

public class BigVehicle extends Route {
    private Depot depot;
    private List<DummySatellites> dummySatellites;

    public BigVehicle(Depot d) {
        super();
        this.depot = d;
        this.capacity = d.getCapacity();
        dummySatellites = new ArrayList<>();
    }

    public BigVehicle(BigVehicle bigVehicle) {
        super(bigVehicle);
        this.dummySatellites = new ArrayList<DummySatellites>();
    }

    public BigVehicle(BigVehicle bv, DummySatellites ds1, DummySatellites ds2) {
        super(bv);
        this.depot = bv.depot;
        this.dummySatellites = new ArrayList<DummySatellites>();
        for (DummySatellites ds : bv.getDummySatellites()) {
            if (ds.getId() == ds1.getId() && ds.getId2() == ds1.getId2())
                this.dummySatellites.add(ds1);
            else if (ds.getId() == ds2.getId() && ds.getId2() == ds2.getId2())
                this.dummySatellites.add(ds2);
            else
                this.dummySatellites.add(new DummySatellites(ds));
        }
    }

    public boolean isEmpty() {
        if (getSatelliteLength() > 0)
            return false;
        else
            return true;
    }

    public int getSatelliteIdWithIndex(int index) {
        return this.dummySatellites.get(index).getId();
    }

    public String toString() {
        StringBuffer print = new StringBuffer();
        print.append("satellite number : " + getSatelliteLength() + " :");
        print.append(" " + this.depot.getId());
        for (int i = 0; i < this.dummySatellites.size(); ++i) {
            print.append(" " + this.dummySatellites.get(i).getId() + "-" + this.dummySatellites.get(i).getId2());
        }
        print.append(" " + this.depot.getId());

        print.append("\n1-e route cost: " + cost);
        print.append("\nSatellites{");
        for (int i = 0; i < this.dummySatellites.size(); ++i) {
            print.append(dummySatellites.get(i) + "\n");
        }
        print.append("}\n");
        return print.toString();
    }

    public void setDummySatellites(ArrayList<DummySatellites> dummySatellites) {
        this.dummySatellites = dummySatellites;
    }

    public void addSatellite(DummySatellites satellite) {
        this.dummySatellites.add(satellite);
    }

    public void addSatellite(DummySatellites satellite, int index) {
        this.dummySatellites.add(index, satellite);
    }

    public List<DummySatellites> getDummySatellites() {
        return this.dummySatellites;
    }

    public int getIndex() {
        return index;
    }

    public int getSatelliteLength() {
        return this.dummySatellites.size();
    }

    public DummySatellites getSatellite(int index) {
        return this.dummySatellites.get(index);
    }

    public void removeSatellite(int index) {
        this.dummySatellites.remove(index);
    }

    public DummySatellites getLastSatellite() {
        return this.dummySatellites.get(dummySatellites.size() - 1);
    }

    public DummySatellites getFirstSatellite() {
        return this.dummySatellites.get(0);
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }
}
