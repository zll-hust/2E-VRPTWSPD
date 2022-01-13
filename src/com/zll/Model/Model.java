package com.zll.Model;

import com.sun.org.apache.xpath.internal.operations.String;
import com.zll.twoEVRP.*;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author： zll-hust
 * @date： 2020/12/27 10:14
 * @description： 2E-TVRPSPD数学模型
 */
public class Model {
    public Instance instance;
    public IloCplex model; // 定义cplex内部类的对象
    public double cost;
    public Solution solution;
    private List<Satellite> satellites;
    private List<Customer> customers;
    private double[][] distanceMatrix;

    public IloNumVar[][][] x1; // kij 1级delivery时k车是否经过ij边
    public IloNumVar[][][] x2; // kij 1级pickup时k车是否经过ij边
    public IloNumVar[][] w1; // ks satellite s 通过k车delivery的质量
    public IloNumVar[][] w2; // ks satellite s 通过k车pickup的质量
    public IloNumVar[][] u1; // ks satellite s 被k车服务的顺序 delivery
    public IloNumVar[][] u2; // ks satellite s 被k车服务的顺序 pickup

    public IloNumVar[][] f; // ij 2级边ij上流过的质量 delivery
    public IloNumVar[][] g; // ij 2级边ij上流过的质量 pickup
    public IloNumVar[][][] y; // vij 2级v车是否经过ij边
    public IloNumVar[][] z; // si 2级customer i是否被satellite s服务

    public IloNumVar[][] a1; // ks k车到达satellite s的时间
    public IloNumVar[][] a2; // vi v车到达customer i的时间
    public IloNumVar[] s1; // i customer i的服务时间
    public IloNumVar[][] s; // ks satellite s被车辆k服务的服务时间

    public boolean[][] A1;
    public boolean[][] A2;

    int dsNr;
    int a1Lenth;
    int a2Lenth;
    int bvNr;
    int svNr;
    Graph[] A1Graph;
    Graph[] A2Graph;

    public double M = 10000;
    public double TimeLimit = 7200;

    public Model(Instance instance, int dsn, int bvNr) {
        this.satellites = instance.getSatellites();
        this.customers = instance.getCustomers();
        this.distanceMatrix = instance.getDistanceMatrix();
        this.instance = instance;

//        dsn = 3;//instance.getCustomersNr();// / 2 + 1; //todo

        dsNr = instance.getSatellitesNr() * dsn;
        a1Lenth = dsNr + 1;
        a2Lenth = dsNr + instance.getCustomersNr();
        this.bvNr = bvNr; //3;//instance.getSatellitesNr() * dsn / 2;
        svNr = dsNr;

        A1Graph = new Graph[a1Lenth];
        int id = 0;
        A1Graph[id++] = new Graph(0, 0, 0, 0);
        for (int i = 0; i < instance.getSatellitesNr(); i++) {
            for (int j = 0; j < dsn; j++) {
                A1Graph[id++] = new Graph(instance.getSatellites().get(i).getId(), j, 1, i);
            }
        }

        A2Graph = new Graph[a2Lenth];
        id = 0;
        for (int i = 0; i < instance.getSatellitesNr(); i++) {
            for (int j = 0; j < dsn; j++) {
                A2Graph[id++] = new Graph(instance.getSatellites().get(i).getId(), j, 1, i);
            }
        }
        for (int i = 0; i < instance.getCustomersNr(); i++) {
            A2Graph[id++] = new Graph(instance.getCustomers().get(i).getId(), 0, 2, i);
        }
    }

    class Graph {
        int sId;
        int dsId;
        int n;
        int type; // 0 depot; 1 dummy satellite; 2 customer

        public Graph(int sId, int dsId, int type, int n) {
            this.sId = sId;
            this.dsId = dsId;
            this.type = type;
            this.n = n;
        }
    }

    public void buildGraph() {
        A1 = new boolean[a1Lenth][a1Lenth];
        A2 = new boolean[a2Lenth][a2Lenth];
        for (int i = 0; i < a1Lenth; i++) {
            Arrays.fill(A1[i], true);
            A1[i][i] = false;
        }

        for (int i = 0; i < a2Lenth; i++) {
            Arrays.fill(A2[i], true);
            A2[i][i] = false;
            if (i < dsNr) {
                for (int j = 0; j < dsNr; j++) {
                    A2[i][j] = false;
                }
            }
        }
    }

    // 函数功能：解模型，并生成车辆路径和得到目标值
    public Solution solve() throws IloException {
        if (model.solve() == false) {
            // 模型不可解
            System.out.println("problem should not solve false!!!");
            return null; // 若不可解，则直接跳出solve函数
        } else {
            System.out.println("problem should solved.");
            AssignCustomers();//todo
            showDetails();
            System.out.println("obj = " + model.getObjValue());
            solution = createSolution();
        }
        return solution;
    }

    public void buildModel() throws IloException {
        // model
        model = new IloCplex();
        model.setOut(null);
        model.setParam(IloCplex.DoubleParam.TiLim, TimeLimit);
        // variables
        x1 = new IloNumVar[bvNr][a1Lenth][a1Lenth];
        x2 = new IloNumVar[bvNr][a1Lenth][a1Lenth];
        w1 = new IloNumVar[bvNr][dsNr];
        w2 = new IloNumVar[bvNr][dsNr];
        u1 = new IloNumVar[bvNr][dsNr];
        u2 = new IloNumVar[bvNr][dsNr];

        f = new IloNumVar[a2Lenth][a2Lenth];
        g = new IloNumVar[a2Lenth][a2Lenth];
        y = new IloNumVar[svNr][a2Lenth][a2Lenth];

        a1 = new IloNumVar[bvNr][dsNr];
        a2 = new IloNumVar[svNr][instance.getCustomersNr()];
        s = new IloNumVar[bvNr][dsNr];

        buildGraph();

        for (int i = 0; i < a1Lenth; i++) {
            for (int j = 0; j < a1Lenth; j++) {
                for (int k = 0; k < bvNr; k++) {
                    // 公式(30)
                    if (A1[i][j]) {
                        x1[k][i][j] = model.numVar(0, 1, IloNumVarType.Bool, "x1_" + k + "," + i + "," + j);
                        x2[k][i][j] = model.numVar(0, 1, IloNumVarType.Bool, "x2_" + k + "," + i + "," + j);
                    }
                }
            }
        }

        for (int k = 0; k < bvNr; k++) {
            for (int s = 0; s < dsNr; s++) {
                // 公式(31)
                w1[k][s] = model.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "w1_" + k + "," + s);
                w2[k][s] = model.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "w2_" + k + "," + s);
                u1[k][s] = model.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "u1_" + k + "," + s);
                u2[k][s] = model.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "u2_" + k + "," + s);
            }
        }

        for (int i = 0; i < a2Lenth; i++) {
            for (int j = 0; j < a2Lenth; j++) {
                // 公式(32)
                if (A2[i][j]) {
                    f[i][j] = model.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "f_" + i + "," + j);
                    g[i][j] = model.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "g_" + i + "," + j);
                }
                for (int v = 0; v < svNr; v++) {
                    // 公式(30)
                    y[v][i][j] = model.numVar(0, 1, IloNumVarType.Bool, "y_" + v + "," + i + "," + j);
                }
            }
        }

        for (int k = 0; k < bvNr; k++) {
            for (int j = 0; j < dsNr; j++) {
                // 公式(33)
                s[k][j] = model.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "s_" + k + "," + j);
                a1[k][j] = model.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "a1_" + k + "," + j);
            }
        }

        for (int v = 0; v < svNr; v++) {
            for (int i = 0; i < instance.getCustomersNr(); i++) {
                // 公式(34)
                a2[v][i] = model.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "a2_" + v + "," + i);
            }
        }

        // 加入目标函数
        // 公式(1)
        IloNumExpr obj = model.numExpr();
        for (int i = 0; i < a1Lenth; i++) {
            for (int j = 0; j < a1Lenth; j++) {
                if (A1[i][j]) {
                    for (int k = 0; k < bvNr; k++) {
                        obj = model.sum(obj, model.prod(distanceMatrix[A1Graph[i].sId][A1Graph[j].sId], x1[k][i][j]));
                        obj = model.sum(obj, model.prod(distanceMatrix[A1Graph[i].sId][A1Graph[j].sId], x2[k][i][j]));
                    }
                }
            }
        }
        for (int i = 0; i < a2Lenth; i++) {
            for (int j = 0; j < a2Lenth; j++) {
                if (A2[i][j]) {
                    for (int v = 0; v < svNr; v++) {
                        obj = model.sum(obj, model.prod(distanceMatrix[A2Graph[i].sId][A2Graph[j].sId], y[v][i][j]));
                    }
                }
            }
        }
        model.addMinimize(obj);

        // 加入约束
        // 公式(2)
        for (int i = 0; i < a1Lenth; i++) {
            for (int k = 0; k < bvNr; k++) {
                IloNumExpr expr1 = model.numExpr();
                IloNumExpr expr2 = model.numExpr();
                for (int j = 0; j < a1Lenth; j++) {
                    if (A1[i][j]) {
                        expr1 = model.sum(expr1, x1[k][i][j]);
                        expr2 = model.sum(expr2, x1[k][j][i]);
                    }
                }
                model.addEq(expr1, expr2);
            }
        }

        // 加入约束
        // 公式(3)
        for (int i = 0; i < a1Lenth; i++) {
            for (int k = 0; k < bvNr; k++) {
                IloNumExpr expr1 = model.numExpr();
                IloNumExpr expr2 = model.numExpr();
                for (int j = 0; j < a1Lenth; j++) {
                    if (A1[i][j]) {
                        expr1 = model.sum(expr1, x2[k][i][j]);
                        expr2 = model.sum(expr2, x2[k][j][i]);
                    }
                }
                model.addEq(expr1, expr2);
            }
        }

        // 加入约束
        // 公式(4)
        for (int k = 0; k < bvNr; k++) {
            IloNumExpr expr1 = model.numExpr();
            for (int j = 0; j < a1Lenth; j++) {
                if (A1[0][j]) {
                    expr1 = model.sum(expr1, x1[k][0][j]);
                }
            }
            model.addLe(expr1, 1);
        }

        // 加入约束
        // 公式(4)
        for (int k = 0; k < bvNr; k++) {
            IloNumExpr expr1 = model.numExpr();
            for (int j = 0; j < a1Lenth; j++) {
                if (A1[0][j]) {
                    expr1 = model.sum(expr1, x2[k][0][j]);
                }
            }
            model.addLe(expr1, 1);
        }

        // 加入约束
        // 公式(4)
        for (int i = 1; i < a1Lenth; i++) {
            IloNumExpr expr1 = model.numExpr();
            for (int j = 0; j < a1Lenth; j++) {
                if (A1[i][j]) {
                    for (int k = 0; k < bvNr; k++) {
                        expr1 = model.sum(expr1, x1[k][i][j]);
                    }
                }
            }
            model.addLe(expr1, 1);
        }

        // 加入约束
        // 公式(5)
        for (int i = 1; i < a1Lenth; i++) {
            IloNumExpr expr1 = model.numExpr();
            for (int j = 0; j < a1Lenth; j++) {
                if (A1[i][j]) {
                    for (int k = 0; k < bvNr; k++) {
                        expr1 = model.sum(expr1, x2[k][i][j]);
                    }
                }
            }
            model.addLe(expr1, 1);
        }

        // 加入约束
        // 公式(6)
        for (int i = 1; i < a1Lenth; i++) {
            for (int j = 1; j < a1Lenth; j++) {
                if (A1[i][j]) {
                    for (int k = 0; k < bvNr; k++) {
                        IloNumExpr expr1 = model.numExpr();
                        IloNumExpr expr2 = model.numExpr();
                        expr1 = model.sum(expr1, model.sum(u1[k][i - 1], 1));
                        expr2 = model.sum(expr2, u1[k][j - 1]);
                        expr2 = model.sum(expr2, model.prod(M, model.diff(1, x1[k][i][j])));
                        model.addLe(expr1, expr2);
                    }
                }
            }
        }

        // 加入约束
        // 公式(7)
        for (int j = 1; j < a1Lenth; j++) {
            int i = 0;
            if (A1[i][j]) {
                for (int k = 0; k < bvNr; k++) {
                    IloNumExpr expr1 = model.numExpr();
                    IloNumExpr expr2 = model.numExpr();
                    expr1 = model.sum(expr1, 1);
                    expr2 = model.sum(expr2, u1[k][j - 1]);
                    expr2 = model.sum(expr2, model.prod(M, model.diff(1, x1[k][i][j])));
                    model.addLe(expr1, expr2);
                }
            }
        }


        // 加入约束
        // 公式(8)
        for (int i = 1; i < a1Lenth; i++) {
            for (int j = 1; j < a1Lenth; j++) {
                if (A1[i][j]) {
                    for (int k = 0; k < bvNr; k++) {
                        IloNumExpr expr1 = model.numExpr();
                        IloNumExpr expr2 = model.numExpr();
                        expr1 = model.sum(expr1, model.sum(u2[k][i - 1], 1));
                        expr2 = model.sum(expr2, u2[k][j - 1]);
                        expr2 = model.sum(expr2, model.prod(M, model.diff(1, x2[k][i][j])));
                        model.addLe(expr1, expr2);
                    }
                }
            }
        }

        // 加入约束
        // 公式(9)
        for (int j = 1; j < a1Lenth; j++) {
            int i = 0;
            if (A1[i][j]) {
                for (int k = 0; k < bvNr; k++) {
                    IloNumExpr expr1 = model.numExpr();
                    IloNumExpr expr2 = model.numExpr();
                    expr1 = model.sum(expr1, 1);
                    expr2 = model.sum(expr2, u2[k][j - 1]);
                    expr2 = model.sum(expr2, model.prod(M, model.diff(1, x2[k][i][j])));
                    model.addLe(expr1, expr2);
                }
            }
        }

        // 加入约束
        // 公式(10)
        for (int s = 1; s < a1Lenth; s++) {
            for (int k = 0; k < bvNr; k++) {
                IloNumExpr expr1 = model.numExpr();
                IloNumExpr expr2 = model.numExpr();
                expr1 = model.sum(expr1, w1[k][s - 1]);
                for (int i = 0; i < a1Lenth; i++) {
                    if (A1[s][i]) {
                        expr2 = model.sum(expr2, x1[k][s][i]);
                    }
                }
                expr2 = model.prod(expr2, M);
                model.addLe(expr1, expr2);
            }
        }

        // 加入约束
        // 公式(11)
        for (int s = 1; s < a1Lenth; s++) {
            for (int k = 0; k < bvNr; k++) {
                IloNumExpr expr1 = model.numExpr();
                IloNumExpr expr2 = model.numExpr();
                expr1 = model.sum(expr1, w2[k][s - 1]);
                for (int i = 0; i < a1Lenth; i++) {
                    if (A1[s][i]) {
                        expr2 = model.sum(expr2, x2[k][s][i]);
                    }
                }
                expr2 = model.prod(expr2, M);
                model.addLe(expr1, expr2);
            }
        }

        // 加入约束
        // 公式(12)
        for (int k = 0; k < bvNr; k++) {
            IloNumExpr expr1 = model.numExpr();
            for (int s = 1; s < a1Lenth; s++) {
                expr1 = model.sum(expr1, w1[k][s - 1]);
            }
            model.addLe(expr1, instance.getCap1E());
        }

        // 加入约束
        // 公式(13)
        for (int k = 0; k < bvNr; k++) {
            IloNumExpr expr1 = model.numExpr();
            for (int s = 1; s < a1Lenth; s++) {
                expr1 = model.sum(expr1, w2[k][s - 1]);
            }
            model.addLe(expr1, instance.getCap1E());
        }

        // 加入约束
        // 公式(14)
        for (int i = 0; i < a2Lenth; i++) {
            for (int v = 0; v < svNr; v++) {
                IloNumExpr expr1 = model.numExpr();
                IloNumExpr expr2 = model.numExpr();
                for (int j = 0; j < a2Lenth; j++) {
                    if (A2[i][j]) {
                        expr1 = model.sum(expr1, y[v][i][j]);
                        expr2 = model.sum(expr2, y[v][j][i]);
                    }
                }
                model.addEq(expr1, expr2);
            }
        }

        // 加入约束
        // 公式(15)
        for (int i = dsNr; i < a2Lenth; i++) {
            IloNumExpr expr1 = model.numExpr();
            for (int v = 0; v < svNr; v++) {
                for (int j = 0; j < a2Lenth; j++) {
                    if (A2[i][j]) {
                        expr1 = model.sum(expr1, y[v][i][j]);
                    }
                }
            }
            model.addEq(expr1, 1);
        }

        // 加入约束
        // 公式(16)
        for (int i = 1; i < a1Lenth; i++) {
            IloNumExpr expr1 = model.numExpr();
            IloNumExpr expr2 = model.numExpr();
            for (int j = 0; j < a2Lenth; j++) {
                if (A2[i - 1][j]) {
                    for (int v = 0; v < svNr; v++) {
                        expr1 = model.sum(expr1, y[v][i - 1][j]);
                    }
                }
            }
            for (int j = 0; j < a1Lenth; j++) {
                if (A1[i][j]) {
                    for (int k = 0; k < bvNr; k++) {
                        expr2 = model.sum(expr2, x1[k][i][j]);
                    }
                }
            }
            model.addEq(expr1, expr2);
        }

        // 加入约束
        // 公式(17)
        for (int v = 0; v < svNr; v++) {
            IloNumExpr expr1 = model.numExpr();
            for (int i = 0; i < dsNr; i++) {
                for (int j = 0; j < a2Lenth; j++) {
                    if (A2[i][j]) {
                        expr1 = model.sum(expr1, y[v][i][j]);
                    }
                }
            }
            model.addLe(expr1, 1);
        }

        // 加入约束
        // 公式(18)
        for (int i = dsNr; i < a2Lenth; i++) {
            IloNumExpr expr1 = model.numExpr();
            IloNumExpr expr2 = model.numExpr();
            for (int j = 0; j < a2Lenth; j++) {
                if (A2[i][j]) {
                    expr1 = model.sum(expr1, f[j][i]);
                    expr2 = model.sum(expr2, f[i][j]);
                }
            }
            expr2 = model.sum(expr2, instance.getCustomers().get(A2Graph[i].n).getDelivery().getDemand());
            model.addEq(expr1, expr2);
        }

        // 加入约束
        // 公式(19)
        for (int i = dsNr; i < a2Lenth; i++) {
            IloNumExpr expr1 = model.numExpr();
            IloNumExpr expr2 = model.numExpr();
            for (int j = 0; j < a2Lenth; j++) {
                if (A2[i][j]) {
                    expr1 = model.sum(expr1, g[j][i]);
                    expr2 = model.sum(expr2, g[i][j]);
                }
            }
            expr2 = model.diff(expr2, instance.getCustomers().get(A2Graph[i].n).getPickup().getDemand());
            model.addEq(expr1, expr2);
        }

        // 加入约束
        // 公式(20)
        for (int i = 0; i < a2Lenth; i++) {
            for (int j = 0; j < a2Lenth; j++) {
                if (A2[i][j]) {
                    IloNumExpr expr1 = model.numExpr();
                    IloNumExpr expr2 = model.numExpr();
                    IloNumExpr expr3 = model.numExpr();
                    IloNumExpr subExpr = model.numExpr();

                    for (int v = 0; v < svNr; v++) {
                        subExpr = model.sum(subExpr, y[v][i][j]);
                    }

                    expr2 = model.sum(expr2, f[i][j]);
                    expr2 = model.sum(expr2, g[i][j]);

                    if (A2Graph[j].type == 1) {
                        expr3 = model.sum(expr3, model.prod(subExpr, instance.getCap2E()));
                        expr3 = model.sum(expr3, model.prod(subExpr, instance.getCustomers().get(A2Graph[i].n).getPickup().getDemand() - instance.getCustomers().get(A2Graph[i].n).getDelivery().getDemand()));
                        model.addLe(expr2, expr3);
                    } else if (A2Graph[i].type == 1) {
                        expr1 = model.sum(expr1, model.prod(subExpr, instance.getCustomers().get(A2Graph[j].n).getDelivery().getDemand() - instance.getCustomers().get(A2Graph[j].n).getPickup().getDemand()));
                        expr3 = model.sum(expr3, model.prod(subExpr, instance.getCap2E()));
                        model.addLe(expr2, expr3);
                        model.addLe(expr1, expr2);
                    } else {
                        expr1 = model.sum(expr1, model.prod(subExpr, instance.getCustomers().get(A2Graph[j].n).getDelivery().getDemand() - instance.getCustomers().get(A2Graph[j].n).getPickup().getDemand()));
                        expr3 = model.sum(expr3, model.prod(subExpr, instance.getCap2E()));
                        expr3 = model.sum(expr3, model.prod(subExpr, instance.getCustomers().get(A2Graph[i].n).getPickup().getDemand() - instance.getCustomers().get(A2Graph[i].n).getDelivery().getDemand()));
                        model.addLe(expr1, expr2);
                        model.addLe(expr2, expr3);
                    }
                }
            }
        }

        // 加入约束 //todo
        // 公式(21)
        for (int s = 0; s < dsNr; s++) {
            IloNumExpr expr1 = model.numExpr();
            IloNumExpr expr2 = model.numExpr();
            for (int k = 0; k < bvNr; k++) {
                expr1 = model.sum(expr1, w1[k][s]);
            }
            for (int i = 0; i < a2Lenth; i++) {
                if (A2[s][i]) {
                    expr2 = model.sum(expr2, f[s][i]);
                }
            }
            model.addEq(expr1, expr2);
        }

        // 加入约束
        // 公式(22)
        for (int s = 0; s < dsNr; s++) {
            IloNumExpr expr1 = model.numExpr();
            IloNumExpr expr2 = model.numExpr();
            for (int k = 0; k < bvNr; k++) {
                expr1 = model.sum(expr1, w2[k][s]);
            }
            for (int i = 0; i < a2Lenth; i++) {
                if (A2[s][i]) {
                    expr2 = model.sum(expr2, g[i][s]);
                }
            }
            model.addEq(expr1, expr2);
        }

        // 加入约束
        // 公式(23)
        for (int k = 0; k < bvNr; k++) {
            for (int j = 0; j < dsNr; j++) {
                IloNumExpr expr1 = model.numExpr();
                IloNumExpr expr2 = model.numExpr();
                expr1 = model.sum(expr1, s[k][j]);
                expr2 = model.sum(expr2, model.prod(instance.getPt(), w1[k][j]));
                model.addEq(expr1, expr2);
            }
        }

        // 加入约束
        // 公式(24)
        for (int i = 1; i < a1Lenth; i++) {
            for (int j = 1; j < a1Lenth; j++) {
                if (A1[i][j]) {
                    for (int k = 0; k < bvNr; k++) {
                        IloNumExpr expr1 = model.numExpr();
                        IloNumExpr expr2 = model.numExpr();
                        expr1 = model.sum(expr1, a1[k][j - 1]);
                        expr1 = model.sum(expr1, model.prod(M, model.diff(1, x1[k][i][j])));
                        expr2 = model.sum(expr2, a1[k][i - 1]);
                        expr2 = model.sum(expr2, distanceMatrix[A1Graph[i].sId][A1Graph[j].sId]);
                        expr2 = model.sum(expr2, s[k][i - 1]);
                        model.addGe(expr1, expr2);
                    }
                }
            }
        }

        // 加入约束
        // 公式(25)
        for (int j = 1; j < a1Lenth; j++) {
            int i = 0;
            if (A1[i][j]) {
                for (int k = 0; k < bvNr; k++) {
                    IloNumExpr expr1 = model.numExpr();
                    IloNumExpr expr2 = model.numExpr();
                    expr1 = model.sum(expr1, a1[k][j - 1]);
                    expr1 = model.sum(expr1, model.prod(M, model.diff(1, x1[k][i][j])));
                    expr2 = model.sum(expr2, distanceMatrix[A1Graph[i].sId][A1Graph[j].sId]);
                    model.addGe(expr1, expr2);
                }
            }
        }

        // 加入约束
        // 公式(26)
        for (int i = 1; i < a1Lenth; i++) {
            for (int j = dsNr; j < a2Lenth; j++) {
                for (int k = 0; k < bvNr; k++) {
                    for (int v = 0; v < svNr; v++) {
                        IloNumExpr expr1 = model.numExpr();
                        IloNumExpr expr2 = model.numExpr();
                        IloNumExpr subExpr = model.numExpr();
                        expr1 = model.sum(expr1, a2[v][A2Graph[j].n]);
                        for (int h = 0; h < a1Lenth; h++) {
                            if (A1[h][i]) {
                                subExpr = model.sum(subExpr, x1[k][h][i]);
                            }
                        }
                        subExpr = model.sum(subExpr, y[v][i - 1][j]);
                        expr1 = model.sum(expr1, model.prod(M, model.diff(2, subExpr)));
                        expr2 = model.sum(expr2, a1[k][i - 1]);
                        expr2 = model.sum(expr2, distanceMatrix[A1Graph[i].sId][A2Graph[j].sId]);
                        expr2 = model.sum(expr2, s[k][i - 1]);
                        model.addGe(expr1, expr2);
                    }
                }
            }
        }

        // 加入约束
        // 公式(27)
        for (int i = dsNr; i < a2Lenth; i++) {
            for (int j = dsNr; j < a2Lenth; j++) {
                if (A2[i][j]) {
                    for (int v = 0; v < svNr; v++) {
                        IloNumExpr expr1 = model.numExpr();
                        IloNumExpr expr2 = model.numExpr();
                        expr1 = model.sum(expr1, a2[v][A2Graph[j].n]);
                        expr1 = model.sum(expr1, model.prod(M, model.diff(1, y[v][i][j])));
                        expr2 = model.sum(expr2, a2[v][A2Graph[i].n]);
                        expr2 = model.sum(expr2, distanceMatrix[A2Graph[i].sId][A2Graph[j].sId]);
                        expr2 = model.sum(expr2, customers.get(A2Graph[i].n).getDelivery().serviceDuration);
                        model.addGe(expr1, expr2);
                    }
                }
            }
        }

        // 加入约束
        // 公式(28)
        for (int i = 0; i < instance.getCustomersNr(); i++) {
            for (int v = 0; v < svNr; v++) {
                IloNumExpr expr = model.numExpr();
                expr = model.sum(expr, a2[v][i]);
                expr = model.diff(expr, customers.get(i).getStartTw());
                model.addGe(expr, 0);
            }
        }

        // 加入约束
        // 公式(29)
        for (int i = 0; i < instance.getCustomersNr(); i++) {
            for (int v = 0; v < svNr; v++) {
                IloNumExpr expr = model.numExpr();
                expr = model.sum(expr, a2[v][i]);
                expr = model.diff(expr, customers.get(i).getEndTw());
                model.addLe(expr, 0);
            }
        }

        // print information
        model.exportModel("model.lp");
    }

    private Solution createSolution() throws IloException {
        Depot depot = new Depot(instance.getDepot());
        Solution solution = new Solution(depot, instance);
        int[] id2 = new int[instance.getSatellitesNr()];
        Arrays.fill(id2, 0);

        ArrayList<DummySatellites> dss = new ArrayList<>();
        ArrayList<Integer> dssId = new ArrayList<>();

        for (int k = 0; k < bvNr; k++) {
            BigVehicle bv = new BigVehicle(depot);
            ArrayList<Integer> ss = new ArrayList<>();
            int before = 0;
            here:
            while (true) {
                for (int i = 0; i < a1Lenth; i++) {
                    if (A1[before][i] && Math.abs(model.getValue(x1[k][before][i]) - 1) < 0.0001) {
                        if (i == 0)
                            break;
                        ss.add(i);
                        before = i;
                        continue here;
                    }
                }
                break;
            }
            if (ss.size() != 0) {
                for (int i = 0; i < ss.size(); i++) {
                    Satellite s = satellites.get(A1Graph[ss.get(i)].n);
                    DummySatellites nS = new DummySatellites(s);

                    nS.setId2(id2[A1Graph[ss.get(i)].n]++);
                    SmallVehicle sv = new SmallVehicle(s);

                    for (int v = 0; v < svNr; v++) {
                        before = ss.get(i) - 1;
                        here:
                        while (true) {
                            for (int j = dsNr; j < a2Lenth; j++) {
                                if (A2[before][j] && Math.abs(model.getValue(y[v][before][j]) - 1) < 0.0001) {
                                    if (j == ss.get(i) - 1)
                                        break;
                                    sv.addCustomer(customers.get(A2Graph[j].n));
                                    before = j;
                                    continue here;
                                }
                            }
                            break;
                        }
                    }

                    dss.add(nS);
                    dssId.add(ss.get(i));
                    nS.setVehicle(sv);
                    sv.setDummySatellite(nS);
                    bv.addSatellite(nS);
                }
                depot.addDeliveryRoute(bv);
            }
        }

        for (int k = 0; k < bvNr; k++) {
            BigVehicle bv = new BigVehicle(depot);
            ArrayList<Integer> ss = new ArrayList<>();
            int before = 0;
            here:
            while (true) {
                for (int i = 0; i < a1Lenth; i++) {
                    if (A1[before][i] && Math.abs(model.getValue(x2[k][before][i]) - 1) < 0.0001) {
                        if (i == 0)
                            break;
                        ss.add(i);
                        before = i;
                        continue here;
                    }
                }
                break;
            }
            if (ss.size() != 0) {
                for (int i = 0; i < ss.size(); i++) {
                    for (int j = 0; j < dss.size(); j++) {
                        if (dssId.get(j) == ss.get(i)) {
                            bv.addSatellite(dss.get(j));
                            break;
                        }
                    }
                }
                depot.addPickupRoute(bv);
            }
        }

        return solution;
    }


    private void showDetails() throws IloException {
        StringBuilder str = new StringBuilder();
        for (int k = 0; k < bvNr; k++) {
            for (int i = 0; i < a1Lenth; i++) {
                for (int j = 0; j < a1Lenth; j++) {
                    if (A1[i][j]) {
                        str.append("x1_{" + k + i + j + "}=" + model.getValue(x1[k][i][j]) + ";");
                    }
                }
                str.append("\n");
            }
        }

        for (int k = 0; k < bvNr; k++) {
            for (int i = 0; i < a1Lenth; i++) {
                for (int j = 0; j < a1Lenth; j++) {
                    if (A1[i][j]) {
                        str.append("x2_{" + k + i + j + "}=" + model.getValue(x2[k][i][j]) + ";");
                    }
                }
                str.append("\n");
            }
        }

        for (int k = 0; k < bvNr; k++) {
            for (int i = 0; i < dsNr; i++) {
                str.append("w1_{" + k + i + "}=" + model.getValue(w1[k][i]) + ";");
            }
            str.append("\n");
        }

        for (int k = 0; k < bvNr; k++) {
            for (int i = 0; i < dsNr; i++) {
                str.append("w2_{" + k + i + "}=" + model.getValue(w2[k][i]) + ";");
            }
            str.append("\n");
        }

        for (int i = 0; i < a2Lenth; i++) {
            for (int j = 0; j < a2Lenth; j++) {
                if (A2[i][j]) {
                    str.append("f_{" + i + j + "}=" + model.getValue(f[i][j]) + ";");
                }
            }
            str.append("\n");
        }

        for (int i = 0; i < a2Lenth; i++) {
            for (int j = 0; j < a2Lenth; j++) {
                if (A2[i][j]) {
                    str.append("g_{" + i + j + "}=" + model.getValue(g[i][j]) + ";");
                }
            }
            str.append("\n");
        }

        for (int v = 0; v < svNr; v++) {
            for (int i = 0; i < a2Lenth; i++) {
                for (int j = 0; j < a2Lenth; j++) {
                    if (A2[i][j]) {
                        str.append("y_{" + v + i + j + "}=" + model.getValue(y[v][i][j]) + ";");
                    }
                }
                str.append("\n");
            }
        }

        for (int k = 0; k < bvNr; k++) {
            for (int i = 0; i < dsNr; i++) {
                str.append("a1_{" + k + i + "}=" + model.getValue(a1[k][i]) + ";");
            }
            str.append("\n");
        }

        for (int v = 0; v < svNr; v++) {
            for (int i = 0; i < instance.getCustomersNr(); i++) {
                str.append("a2_{" + v + i + "}=" + model.getValue(a2[v][i]) + ";");
            }
            str.append("\n");
        }

        for (int k = 0; k < bvNr; k++) {
            for (int i = 0; i < dsNr; i++) {
                str.append("u1_{" + k + i + "}=" + model.getValue(u1[k][i]) + ";");
            }
            str.append("\n");
        }

        for (int k = 0; k < bvNr; k++) {
            for (int i = 0; i < dsNr; i++) {
                str.append("u2_{" + k + i + "}=" + model.getValue(u2[k][i]) + ";");
            }
            str.append("\n");
        }


        System.out.println(str);
    }

    private void AssignCustomers() throws IloException {
        StringBuilder str = new StringBuilder();
        for (int k = 0; k < bvNr; k++) {
            str.append("first-echelon route" + k + ":" + "\n");
            int before = 0;
            str.append(0 + "-");
            here:
            while (true) {
                for (int i = 0; i < a1Lenth; i++) {
                    if (A1[before][i] && model.getValue(x1[k][before][i]) == 1) {
                        if (i == 0)
                            break;
                        str.append(i + "-");
                        before = i;
                        continue here;
                    }
                }
                break;
            }
            str.append(0 + "\n");
        }

        for (int v = 0; v < svNr; v++) {
            str.append("second-echelon route" + v + ":" + "\n");
            for (int i = 0; i < dsNr; i++) {
                int before = i;
                str.append((i + 1) + "-");
                here:
                while (true) {
                    for (int j = dsNr; j < a2Lenth; j++) {
                        if (A2[before][j] && model.getValue(y[v][before][j]) == 1) {
                            if (j == i)
                                break;
                            str.append((j + 1) + "-");
                            before = j;
                            continue here;
                        }
                    }
                    break;
                }
                str.append((i + 1) + "\n");
            }
        }

        for (int k = 0; k < bvNr; k++) {
            str.append("first-echelon route" + k + ":" + "\n");
            int before = 0;
            str.append(0 + "-");
            here:
            while (true) {
                for (int i = 0; i < a1Lenth; i++) {
                    if (A1[before][i] && Math.abs(model.getValue(x2[k][before][i]) - 1) < 0.0001) {
                        if (i == 0)
                            break;
                        str.append(i + "-");
                        before = i;
                        continue here;
                    }
                }
                break;
            }
            str.append(0 + "\n");
        }
//        System.out.println(str);
    }
}