/**
*  Copyright (c) 2011, Arnaud Malapert
*  All rights reserved.
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*
*      * Redistributions of source code must retain the above copyright
*        notice, this list of conditions and the following disclaimer.
*      * Redistributions in binary form must reproduce the above copyright
*        notice, this list of conditions and the following disclaimer in the
*        documentation and/or other materials provided with the distribution.
*      * Neither the name of the Arnaud Malapert nor the
*        names of its contributors may be used to endorse or promote products
*        derived from this software without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
*  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
*  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pisco.shop;

import static choco.Choco.allDifferent;
import static choco.Choco.eq;
import static choco.Choco.makeIntVar;
import static choco.Choco.sum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;

/**
 *
 */
public class OpenShopGenerator {

    protected int jobduration;
    protected int lbd, ubd;   //duration of tasks lies in [lbd, ubd]
    protected double plbd, pubd; //duration of tasks defined as a pourcentage of the total job-machine duration
    protected int idx;

    protected int n;
    protected IntegerVariable[][] pij;
    protected CPModel m;
    protected CPSolver s;


    public OpenShopGenerator(int njobs, int jobduration, double pourcentlb, double pourcentub, int idx) {
        this.n = njobs;
        this.jobduration = jobduration;
        this.plbd = pourcentlb;
        this.pubd = pourcentub;
        this.lbd = (int) Math.round(((double) pourcentlb * jobduration) / 100d);
        this.ubd = (int) Math.round(((double) pourcentub * jobduration) / 100d);
        this.idx = idx;
    }

    public void generate() {
        m = new CPModel();
        s = new CPSolver();
        pij = new IntegerVariable[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < pij.length; j++) {
                pij[i][j] = makeIntVar("p", lbd, ubd,"cp:enum");
            }
        }
        int[] onecoef = new int[n];
        Arrays.fill(onecoef,1);
        for (int i = 0; i < n; i++) {
            m.addConstraint(eq(sum(pij[i]),jobduration));
            //m.addConstraint(equation(pij[i],onecoef,jobduration));                        
            m.addConstraint(allDifferent(pij[i]));
            IntegerVariable[] col = new IntegerVariable[n];
            for (int j = 0; j < n; j++) {
                col[j] = pij[j][i];
            }
            m.addConstraint(eq(sum(col),jobduration));
            //m.addConstraint(equation(col,onecoef,jobduration));
        }

        s.read(m);
        s.setGeometricRestart(100, 1);
        s.setRandomSelectors(0);
        s.solve();
        if (s.isFeasible()) {
         printGrid();
         outputGrid();
        } else System.out.println(" no solution ");
    }

    public String pNum(int val) {
       if (val < 10) return val + "  ";
       if (val < 100) return val + " ";
       return ""+val;
    }

    public void printGrid() {
        System.out.println("zarb_" + n + "_[" + lbd + "-" + ubd + "][" + jobduration + "]");        
        System.out.println("" + jobduration);
        System.out.println(n + " " + n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(pNum(s.getVar(pij[i][j]).getVal()) + " ");
            }
            System.out.println("");
        }
    }

    public void outputGrid() {
        try {
            File f = new File("data/zarb_" + n + "_" + idx + ".txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write("" + jobduration);
            bw.newLine();
            bw.write(n + " " + n);
            bw.newLine();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    bw.write(pNum(s.getVar(pij[i][j]).getVal()) + " ");
                }
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void genezarb() {
        OpenShopGenerator gene = null;
        //generate 8-8
        gene = new OpenShopGenerator(8, 1000, 1, 50, 0);  //[1-50]
        gene.generate();
        gene = new OpenShopGenerator(8, 1000, 3, 25, 1);  //[3-25]
        gene.generate();
        gene = new OpenShopGenerator(8, 1000, 5, 20, 2);  //[5-20]
        gene.generate();
        gene = new OpenShopGenerator(8, 1000, 7, 15, 3);  //[7-15]
        gene.generate();

        //generate 10-10
        gene = new OpenShopGenerator(10, 1000, 1, 50, 0); //[1-50]
        gene.generate();
        gene = new OpenShopGenerator(10, 1000, 3, 25, 1); //[3-25]
        gene.generate();
        gene = new OpenShopGenerator(10, 1000, 5, 20, 2); //[5-20]
        gene.generate();
        gene = new OpenShopGenerator(10, 1000, 7, 13, 3); //[7-13]
        gene.generate();

        //generate 15-15
        gene = new OpenShopGenerator(15, 1100, 1, 25, 0);  //[1-25]
        gene.generate();
        gene = new OpenShopGenerator(15, 1100, 3, 15, 1);  //[3-15]
        gene.generate();
        gene = new OpenShopGenerator(15, 1100, 5, 10, 2);  //[5-10]
        gene.generate();

        //generate 20-20
        gene = new OpenShopGenerator(20, 1300, 0.1, 10, 0);  //[1-25]
        gene.generate();
        gene = new OpenShopGenerator(20, 1300, 1, 10, 1);  //[3-15]
        gene.generate();
        gene = new OpenShopGenerator(20, 1300, 2, 10, 2);  //[5-10]
        gene.generate();
    }

    public static void main(String[] args) {
        genezarb();        
    }


}
