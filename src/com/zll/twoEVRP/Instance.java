package com.zll.twoEVRP;

import com.zll.DataSet.Node;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;


public class Instance {
    private double pt; // 转运系数
    private Depot depot;
    private List<Satellite> satellites;
    private List<Customer> customers;
    private String filename;

    private int satellitesNr;
    private int customersNr;
    private int totalNodeNr;

    private int cap1E;
    private int cap2E;

    private double[][] distanceMatrix;


    public Instance(Parameters p) {
        this.filename = p.getInputFileName();
    }

    public void readInstanceFile() {
        try {
            this.customers = new ArrayList<Customer>();
            this.satellites = new ArrayList<Satellite>();
            this.depot = new Depot();
            this.totalNodeNr = 0;

            String data;
            Scanner in = new Scanner(new FileReader("./input/small instance/" + filename + ".txt"));

            // 转运系数
            data = in.nextLine();
            this.pt = Double.parseDouble(data);

            in.nextLine(); // skip unuseful lines

            // Depot Center
            data = in.nextLine();
            this.depot.setXCoordinate(Integer.parseInt(data.split("\\s+")[0]));
            this.depot.setYCoordinate(Integer.parseInt(data.split("\\s+")[1]));
            data = in.nextLine();
            this.depot.setCapacity(Integer.parseInt(data));
            this.depot.setId(totalNodeNr++);
            in.nextLine(); // skip unuseful lines

            // Satellite
            data = in.nextLine();
            this.satellitesNr = Integer.parseInt(data);
            for (int i = 0; i < this.satellitesNr; i++) {
                data = in.nextLine();
                Satellite s = new Satellite();
                s.setXCoordinate(Integer.parseInt(data.split("\\s+")[0]));
                s.setYCoordinate(Integer.parseInt(data.split("\\s+")[1]));
                data = in.nextLine();
                s.setCapacity(Integer.parseInt(data));
                s.setId(this.totalNodeNr++);
                this.satellites.add(s);
            }

            in.nextLine(); // skip unuseful lines

            //Customer
            data = in.nextLine();
            this.customersNr = Integer.parseInt(data);
            for (int i = 0; i < this.customersNr; i++) {
                data = in.nextLine();
                Customer c = new Customer();
                c.setXCoordinate(Integer.parseInt(data.split("\\s+")[0]));
                c.setYCoordinate(Integer.parseInt(data.split("\\s+")[1]));
                c.getDelivery().setDemand(Integer.parseInt(data.split("\\s+")[2]));
                c.getPickup().setDemand(Integer.parseInt(data.split("\\s+")[3]));
                c.setStartTw(Integer.parseInt(data.split("\\s+")[4]));
                c.setEndTw(Integer.parseInt(data.split("\\s+")[5]));
                c.getDelivery().setServiceDuration(Integer.parseInt(data.split("\\s+")[6]));
                c.setId(this.totalNodeNr++);
                this.customers.add(c);
            }

            in.close();


            double[][] coodinate = new double[totalNodeNr][2];
            coodinate[0][0] = depot.getXCoordinate();
            coodinate[0][1] = depot.getYCoordinate();
            for (Satellite s : satellites) {
                coodinate[s.getId()][0] = s.getXCoordinate();
                coodinate[s.getId()][1] = s.getYCoordinate();
            }
            for (Customer c : customers) {
                coodinate[c.getId()][0] = c.getXCoordinate();
                coodinate[c.getId()][1] = c.getYCoordinate();
            }

            this.distanceMatrix = new double[totalNodeNr][];
            for (int i = 0; i < distanceMatrix.length; i++) {
                distanceMatrix[i] = new double[totalNodeNr];
                Arrays.fill(distanceMatrix[i], 0);
            }
            for (int i = 0; i < distanceMatrix.length; i++) {
                for (int j = 0; j < distanceMatrix[i].length; j++) {
                    distanceMatrix[i][j] = distanceMatrix[j][i] = Math.sqrt(Math.pow(coodinate[i][0] - coodinate[j][0], 2) + Math.pow(coodinate[i][1] - coodinate[j][1], 2));
                }
            }

        } catch (FileNotFoundException e) {
            // File not found
            System.out.println("Instance File not found!");
            System.exit(-1);
        }
    }

    public void readInstanceFileType2() {
        cap1E = 125;
        cap2E = 50;
        pt = 0.5;

        try {
            this.customers = new ArrayList<Customer>();
            this.satellites = new ArrayList<Satellite>();
            this.depot = new Depot();

            String data;
            Scanner in = new Scanner(new FileReader("./input/Set1/" + filename + ".txt"));

            String[] numbers = filename.split("-");
            this.satellitesNr = Integer.parseInt(numbers[1]);
            this.customersNr = Integer.parseInt(numbers[0]);
            this.totalNodeNr = 1 + satellitesNr + customersNr;
            int id = 1 + satellitesNr;

            //Customer
            for (int i = 0; i < this.customersNr; i++) {
                data = in.nextLine();
                Customer c = new Customer();
                c.setXCoordinate(Integer.parseInt(data.split("\\s+")[1]));
                c.setYCoordinate(Integer.parseInt(data.split("\\s+")[2]));
                c.setStartTw(Integer.parseInt(data.split("\\s+")[3]));
//                c.setStartTw(0);
                c.setEndTw(Integer.parseInt(data.split("\\s+")[4]));
//                c.setEndTw(10000);
                c.getDelivery().setDemand(Integer.parseInt(data.split("\\s+")[5]));
                c.getPickup().setDemand(c.getDelivery().getDemand() / 2); //todo
//                c.getPickup().setDemand(0);
                c.getDelivery().setServiceDuration(Integer.parseInt(data.split("\\s+")[6]));
                c.setId(id++);
                this.customers.add(c);
            }


            // Satellite
            id = 1;
            for (int i = 0; i < this.satellitesNr; i++) {
                data = in.nextLine();
                Satellite s = new Satellite();
                s.setXCoordinate(Integer.parseInt(data.split("\\s+")[1]));
                s.setYCoordinate(Integer.parseInt(data.split("\\s+")[2]));
                s.setCapacity(cap2E);
                s.setId(id++);
                this.satellites.add(s);
            }


            // Depot Center
            id = 0;
            data = in.nextLine();
            this.depot.setXCoordinate(Integer.parseInt(data.split("\\s+")[1]));
            this.depot.setYCoordinate(Integer.parseInt(data.split("\\s+")[2]));
            this.depot.setCapacity(cap1E);
            this.depot.setId(id++);


            in.close();


            double[][] coodinate = new double[totalNodeNr][2];
            coodinate[0][0] = depot.getXCoordinate();
            coodinate[0][1] = depot.getYCoordinate();
            for (Satellite s : satellites) {
                coodinate[s.getId()][0] = s.getXCoordinate();
                coodinate[s.getId()][1] = s.getYCoordinate();
            }
            for (Customer c : customers) {
                coodinate[c.getId()][0] = c.getXCoordinate();
                coodinate[c.getId()][1] = c.getYCoordinate();
            }

            this.distanceMatrix = new double[totalNodeNr][];
            for (int i = 0; i < distanceMatrix.length; i++) {
                distanceMatrix[i] = new double[totalNodeNr];
                Arrays.fill(distanceMatrix[i], 0);
            }
            for (int i = 0; i < distanceMatrix.length; i++) {
                for (int j = 0; j < distanceMatrix[i].length; j++) {
                    distanceMatrix[i][j] = distanceMatrix[j][i] = Math.sqrt(Math.pow(coodinate[i][0] - coodinate[j][0], 2) + Math.pow(coodinate[i][1] - coodinate[j][1], 2));
                }
            }


        } catch (FileNotFoundException e) {
            // File not found
            System.out.println("Instance File not found!");
            System.exit(-1);
        }
    }

    public void readInstanceFileType3() {
        int cap1E = 15000;
        int cap2E = 6000;
        customersNr = 12;
        satellitesNr = 2;

        this.customers = new ArrayList<Customer>();
        this.satellites = new ArrayList<Satellite>();
        this.depot = new Depot();
        int id = 0;
        this.depot.setCapacity(cap1E);
        this.depot.setId(id++);
        for (int i = 0; i < this.satellitesNr; i++) {
            Satellite s = new Satellite();
            s.setId(id++);
            s.setCapacity(cap2E);
            satellites.add(s);
        }
        for (int i = 0; i < this.customersNr; i++) {
            Customer c = new Customer();
            c.setId(id++);
            c.setXCoordinate(0);
            c.setYCoordinate(0);
            c.delivery.serviceDuration = 5;
            customers.add(c);
        }
        this.totalNodeNr = 1 + satellitesNr + customersNr;
        this.distanceMatrix = new double[totalNodeNr][totalNodeNr];

        this.pt = 0.005;

        try {
            String data;
            Scanner in = new Scanner(new FileReader("./input/instance/" + filename + "/" + filename + ".dat"));

            for (int i = 0; i < 13; i++)
                in.nextLine(); // skip unuseful lines

            for (int i = 0; i < this.satellitesNr + this.customersNr + 1; i++) {
                data = in.nextLine();
                for (int j = 0; j < this.satellitesNr + this.customersNr + 1; j++) {
                    if (i == j)
                        distanceMatrix[i][j] = 0;
                    else
                        distanceMatrix[i][j] = Integer.parseInt(data.split("\\s+")[j]);
                }
            }

            for (int i = 0; i < 5; i++)
                in.nextLine(); // skip unuseful lines

            for (int i = 0; i < this.customersNr; i++) {
                data = in.nextLine();
                customers.get(i).delivery.demand = Integer.parseInt(data.split("\\s+")[1]);
            }

            in.close();
        } catch (FileNotFoundException e) {
            // File not found
            System.out.println("Instance File not found!");
            System.exit(-1);
        }


        try {
            String data;
            Scanner in = new Scanner(new FileReader("./input/instance/" + filename + "/" + filename + ".txt"));

            for (int i = 0; i < this.customersNr; i++) {
                data = in.nextLine();
                customers.get(i).startTw = Integer.parseInt(data.split("\\s+")[0]);
                customers.get(i).endTw = Integer.parseInt(data.split("\\s+")[1]);
                customers.get(i).pickup.demand = Integer.parseInt(data.split("\\s+")[2]);
            }

            in.close();
        } catch (FileNotFoundException e) {
            // File not found
            System.out.println("Instance File not found!");
            System.exit(-1);
        }
    }


    public void readInstanceFileType4(double pt, String instanceName) {
        customersNr = Integer.parseInt(instanceName.split("-")[0]);
        satellitesNr = Integer.parseInt(instanceName.split("-")[1]);

        this.customers = new ArrayList<Customer>();
        this.satellites = new ArrayList<Satellite>();
        this.depot = new Depot();

        this.totalNodeNr = 1 + satellitesNr + customersNr;
        this.distanceMatrix = new double[totalNodeNr][totalNodeNr];

        this.pt = pt;//0.5;

        try {
            String data = "./input/Set2/Set5_" + filename + ".dat";
            Scanner in = new Scanner(new FileReader("./input/Set2/" + filename + "/Set5_" + filename + ".dat"));

            for (int i = 0; i < 2; i++)
                in.nextLine(); // skip unuseful lines

            data = in.nextLine();
            cap1E = Integer.parseInt(data.split(",")[1]);

            for (int i = 0; i < 2; i++)
                in.nextLine(); // skip unuseful lines

            data = in.nextLine();
            cap2E = Integer.parseInt(data.split(",")[2]);

            for (int i = 0; i < 2; i++)
                in.nextLine(); // skip unuseful lines

            data = in.nextLine();
            satellitesNr = data.split("\\s+").length - 1;

            int id = 0;
            depot.xCoordinate = Integer.parseInt(data.split("\\s+")[0].split(",")[0]);
            depot.yCoordinate = Integer.parseInt(data.split("\\s+")[0].split(",")[1]);
            this.depot.setCapacity(cap1E);
            this.depot.setId(id++);

            for (int i = 0; i < satellitesNr; i++) {
                Satellite s = new Satellite();
                s.xCoordinate = Integer.parseInt(data.split("\\s+")[i + 1].split(",")[0]);
                s.yCoordinate = Integer.parseInt(data.split("\\s+")[i + 1].split(",")[1]);
                s.setId(id++);
                s.setCapacity(cap2E);
                satellites.add(s);
            }

            for (int i = 0; i < 2; i++)
                in.nextLine(); // skip unuseful lines

            data = in.nextLine();
            customersNr = data.split("\\s+").length;

            for (int i = 0; i < this.customersNr; i++) {
                Customer c = new Customer();
                c.setId(id++);
                c.xCoordinate = Integer.parseInt(data.split("\\s+")[i].split(",")[0]);
                c.yCoordinate = Integer.parseInt(data.split("\\s+")[i].split(",")[1]);
                c.delivery.demand = Integer.parseInt(data.split("\\s+")[i].split(",")[2]);
                c.delivery.serviceDuration = 10;
                customers.add(c);
            }


            double[][] coodinate = new double[totalNodeNr][2];
            coodinate[0][0] = depot.getXCoordinate();
            coodinate[0][1] = depot.getYCoordinate();
            for (Satellite s : satellites) {
                coodinate[s.getId()][0] = s.getXCoordinate();
                coodinate[s.getId()][1] = s.getYCoordinate();
            }
            for (Customer c : customers) {
                coodinate[c.getId()][0] = c.getXCoordinate();
                coodinate[c.getId()][1] = c.getYCoordinate();
            }

            this.distanceMatrix = new double[totalNodeNr][];
            for (int i = 0; i < distanceMatrix.length; i++) {
                distanceMatrix[i] = new double[totalNodeNr];
                Arrays.fill(distanceMatrix[i], 0);
            }
            for (int i = 0; i < distanceMatrix.length; i++) {
                for (int j = 0; j < distanceMatrix[i].length; j++) {
                    distanceMatrix[i][j] = distanceMatrix[j][i] = Math.sqrt(Math.pow(coodinate[i][0] - coodinate[j][0], 2) + Math.pow(coodinate[i][1] - coodinate[j][1], 2));
                }
            }

            in.close();
        } catch (FileNotFoundException e) {
            // File not found
            System.out.println("Instance File not found!");
            System.exit(-1);
        }


        try {
            String data;
            Scanner in = new Scanner(new FileReader("./input/Set2/" + filename + "/" + filename + ".txt"));

            for (int i = 0; i < this.customersNr; i++) {
                data = in.nextLine();
                customers.get(i).startTw = Integer.parseInt(data.split("\\s+")[0]);
                customers.get(i).endTw = Integer.parseInt(data.split("\\s+")[1]);
                customers.get(i).pickup.demand = Integer.parseInt(data.split("\\s+")[2]);
            }

            in.close();
        } catch (FileNotFoundException e) {
            // File not found
            System.out.println("Instance File not found!");
            System.exit(-1);
        }
    }

    public double getTravelTime(int nr1, int nr2) {
        return distanceMatrix[nr1][nr2];
    }

    public List<Customer> getCustomers() {
        return new ArrayList<>(this.customers);
    }

    public double[][] getDistanceMatrix() {
        return this.distanceMatrix;
    }

    public Depot getDepot() {
        return depot;
    }

    public List<Satellite> getSatellites() {
        return satellites;
    }

    public double getPt() {
        return pt;
    }

    public void setPt(double pt) {
        this.pt = pt;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public void setSatellites(List<Satellite> satellites) {
        this.satellites = satellites;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getSatellitesNr() {
        return satellitesNr;
    }

    public void setSatellitesNr(int satellitesNr) {
        this.satellitesNr = satellitesNr;
    }

    public int getCustomersNr() {
        return customersNr;
    }

    public void setCustomersNr(int customersNr) {
        this.customersNr = customersNr;
    }

    public int getTotalNodeNr() {
        return totalNodeNr;
    }

    public void setTotalNodeNr(int totalNodeNr) {
        this.totalNodeNr = totalNodeNr;
    }

    public void setDistanceMatrix(double[][] distanceMatrix) {
        this.distanceMatrix = distanceMatrix;
    }

    public int getCap1E() {
        return cap1E;
    }

    public void setCap1E(int cap1E) {
        this.cap1E = cap1E;
    }

    public int getCap2E() {
        return cap2E;
    }

    public void setCap2E(int cap2E) {
        this.cap2E = cap2E;
    }
}