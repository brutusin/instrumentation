package org.brutusin.instrumentation.performance;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author ignacio
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(Test.class);
        Test t = new Test();
        long nano = System.nanoTime();
        t.fact(20);
        System.out.println(System.nanoTime() - nano);

    }

    public long fact(long n) {
        if (n == 1) {
            return 1;
        } else {
            return fact(n - 1) * n;
        }
    }
}
