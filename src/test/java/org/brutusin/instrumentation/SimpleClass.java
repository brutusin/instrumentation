/*
 * Copyright 2014 brutusin.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *   
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.instrumentation;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.brutusin.instrumentation.utils.Helper;

public class SimpleClass {

    public static final String GREETING = "Hello world";

    public static String sayHello(String name) {
        return "Hello " + name + "!";
    }
  
    public static String sayHelloDate(int age, String name, double height) {
        return "Hello " + name + ", today is " + new Date(getStaticDate()) + "!";
    }

    public long getDate() {

        File f = new File("");
        sayHelloDate(11,"nacho",123.9);
        return System.currentTimeMillis();
    }

    public long getDateAsIntrumented(int a, Object b, Object c) {
        FrameData fd = FrameData.getInstance(this, "aaa", "bbb", new Object[]{a, b, c});
        Callback.onStart(fd);
        return System.currentTimeMillis();
    }

    public static long getStaticDate() {
        return System.currentTimeMillis();
    }

    public static void throwHello() {
        //throw new RuntimeException(GREETING);
    }
    
    public static void main(String[] args) throws Exception {
        Class clazz = SimpleClass.class;
        String className = clazz.getCanonicalName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName);
        byte[] bytes = IOUtils.toByteArray(is);
        Helper.viewByteCode(bytes);
    }
}
