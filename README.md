> Check out https://github.com/ShiftLeftSecurity/bctrace instead. A more mature and stable evolution of this project

# org.brutusin:instrumentation [![Build Status](https://api.travis-ci.org/brutusin/instrumentation.svg?branch=master)](https://travis-ci.org/brutusin/instrumentation) [![Maven Central Latest Version](https://maven-badges.herokuapp.com/maven-central/org.brutusin/instrumentation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.brutusin/instrumentation/)
An extensible java agent framework that instruments programs running on the JVM (modifying the bytecode at class loading time), with the purpose of capturing method invocation events (start, finish, errors ...) and notifying custom listeners.

**Table of Contents**
- [org.brutusin:instrumentation](#orgbrutusininstrumentation)
  - [How it works](#how-it-works)
  - [Maven dependency](#maven-dependency)
  - [Example](#example)
    - [Implementation](#implementation)
    - [Packaging](#packaging)
    - [JRE launching](#jre-launching)
    - [Main stack](#main-stack)
    - [Brutusin dependent modules](#brutusin-dependent-modules)
    - [Support, bugs and requests](#support-bugs-and-requests)
    - [Authors](#authors)
    - [License](#license)
	
## How it works
The [java instrumentation package](http://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html) introduced in JDK1.5, provides a simple way to transform java-class definition at loading time, consisting basically in a `byte[]` to `byte[]` transformation, by the so called "java agents".

This module provides an agent ([org.brutusin.instrumentation.Agent](src/main/java/org/brutusin/instrumentation/Agent.java)) that creates an execution listener instance (from the name of a concrete class extending [org.brutusin.instrumentation.Interceptor](src/main/java/org/brutusin/instrumentation/Interceptor.java) passed from the JVM agent arguments) and, making use of the [ASM library](http://asm.ow2.org/), introduces a series of instructions in the method definitions of the classes to be loaded (classes and methods can be skipped) to notify these execution events to the listener.

From a simplified point of view, the dynamic transformation turns a method like this: 
```java
public Object foo(Object bar){
    return new Object();
}
```

into that:
```java
public Object foo(Object bar){
    onStart(bar);
    try{
        Object ret = new Object();
        onFinished(ret);
        return ret;
    } catch(Throwable th){
        onThrowable(th);
        throw th; // at bytecode level this is legal
    }
}
```

allowing your custom listener to be notified.

## Maven dependency 

```xml
<dependency>
    <groupId>org.brutusin</groupId>
    <artifactId>instrumentation</artifactId>
</dependency>
```
Click [here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.brutusin%22%20a%3A%22instrumentation%22) to see the latest available version released to the Maven Central Repository.

If you are not using maven and need help you can ask [here](https://github.com/brutusin/instrumentation/issues).

## Example
*See [logging-instrumentation](https://github.com/brutusin/logging-instrumentation) for a complete working example.*
### Implementation
Create the following listener implementation:

```java
package mypackage;

public class MyInterceptor extends Interceptor {

    @Override
    public void init(String arg) {
        System.out.println("Interceptor args: " + arg);
    }

    @Override
    public boolean interceptClass(String className, byte[] byteCode) {
        return true; // all classes can be intrumented
    }

    @Override
    public boolean interceptMethod(ClassNode cn, MethodNode mn) {
        return true; // all methods are instrumented
    }

    @Override
    protected void doOnStart(Method m, Object[] arg, String executionId) {
        System.out.println("doOnStart " + m + " " + executionId);
    }

    @Override
    protected void doOnThrowableThrown(Method m, Throwable throwable, String executionId) {
        System.out.println("doOnThrowableThrown " + m + " " + executionId);
    }

    @Override
    protected void doOnThrowableUncatched(Method m, Throwable throwable, String executionId) {
        System.out.println("doOnThrowableUncatched " + m + " " + executionId);
    }

    @Override
    protected void doOnFinish(Method m, Object result, String executionId) {
        System.out.println("doOnFinish " + m + " " + executionId);
    }
}
```
### Packaging
Create a [fat-jar](http://maven.apache.org/plugins/maven-assembly-plugin/descriptor-refs.html#jar-with-dependencies) with the previous class and its dependencies. Add the following attribute to  the manifest of the agent JAR:
```
Premain-Class: org.brutusin.instrumentation.Agent
```
Suppose this jar to be named `myagent.jar`
### JRE launching
Run (at least JRE 1.5) the desired java application with the following JVM options: (suppossing myagent.jar located in the working directory)
```
-javaagent:myagent.jar=mypackage.MyInterceptor;an_interceptor_optional_parameter
```

## Main stack
This module could not be possible without:
* [org.ow2.asm:asm-all](http://asm.ow2.org/)

## Brutusin dependent modules
* [org.brutusin:logging-instrumentation](https://github.com/brutusin/logging-instrumentation)

## Support, bugs and requests
https://github.com/brutusin/instrumentation/issues

## Authors

- Ignacio del Valle Alles (<https://github.com/idelvall/>)

Contributions are always welcome and greatly appreciated!

## License
Apache License, Version 2.0
http://www.apache.org/licenses/LICENSE-2.0


