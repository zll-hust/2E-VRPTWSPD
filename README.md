# 2E-VRPTWSPD code & instances

This repository contains the codes and instances for:

- Zhou, H., Qin, H., Zhang, Z. et al. Two-echelon vehicle routing problem with time windows and simultaneous pickup and delivery. Soft Comput 26, 3345–3360 (2022). https://doi.org/10.1007/s00500-021-06712-2

Here is the abstract: 

This paper proposes a tabu search algorithm for the two-echelon vehicle routing problem with time windows and simultaneous pickup and delivery (2E-VRPTWSPD), which is a new variant of the two-echelon vehicle routing problem. 2E-VRPTWSPD involves three stages of routing: (1) the first-echelon vehicles start from the depot to deliver cargoes to satellites; (2) the second-echelon vehicles start from satellites to serve customers within time windows in a simultaneously pickup and delivery manner and finally return to their satellites with pickup cargoes; (3) the first-echelon vehicles start from the depot to collect cargoes on satellites. To solve this problem, we formulate it with a mathematical model. Then, we implement a variable neighborhood tabu search algorithm with a tailored solution representation to solve large-scale instances. Dummy satellites time windows are applied in our algorithm to speed up the search. Finally, we generate two benchmark instance sets to analyze the performance of our algorithm. Computational results show that our algorithm can obtain the optimal results for 10 out of 11 small-scale instances and produce promising solutions for 200-customer instances within a few minutes. Additional experiments indicate that the usage of dummy satellite time windows can save 19% computation time on average.

Before running the codes of the MILP model, please make sure you have imported CPLEX (my version is 12.6.3). *com.zll.Main.Test* is used to print the results to a .csv file, but it needs a *org.apache.commons.csv* jar. All codes are coded in Java SE 1.8.0. Some annotations written by Chinese are provided.

It is appreciated if you provide a citation of the our paper if this repository is useful for your research. 
