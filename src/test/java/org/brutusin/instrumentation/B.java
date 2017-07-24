/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brutusin.instrumentation;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.brutusin.instrumentation.utils.Helper;

/**
 *
 * @author ignacio
 */
public class B extends A {

    private final int age;

    public static String joinTheJoyRide() {
        return "C'mon join the joyrideee!";
    }

    public B() {
        this(null, 0);
    }

    public B(String name, int age) {
        super(name);
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    static public boolean doubleIsDifferent(double d1, double d2, double delta) {
        if (Double.compare(d1, d2) == 0) {
            return false;
        }
        if ((Math.abs(d1 - d2) <= delta)) {
            return false;
        }

        return true;
    }

    static public boolean doubleIsDifferentInstrumented(double d1, double d2, double delta) {
        FrameData fd = FrameData.getInstance(null, "aaa", "bbb", new Object[]{d1, d2, delta});
        Callback.onStart(fd);

        if (Double.compare(d1, d2) == 0) {
            return false;
        }
        if ((Math.abs(d1 - d2) <= delta)) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) throws Exception {
        Class clazz = B.class;
        String className = clazz.getCanonicalName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName);
        byte[] bytes = IOUtils.toByteArray(is);
        Helper.viewByteCode(bytes);
    }
}
