# org.brutusin:bctrace  [![Build Status](https://api.travis-ci.org/brutusin/instrumentation.svg?branch=master)](https://travis-ci.org/brutusin/instrumentation) [![Maven Central Latest Version](https://maven-badges.herokuapp.com/maven-central/org.brutusin/instrumentation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.brutusin/instrumentation/)

An extensible java agent framework that instruments programs running on the JVM (modifying the bytecode at class loading time), with the purpose of capturing method invocation events (start, finish, errors ...) and notifying custom listeners.

**Table of Contents**
- [org.brutusin:bctrace](#orgbrutusinbctrace)
  - [How it works](#how-it-works)
  - [Usage](#usage)
  - [Registering hooks](#registering-hooks)
  - [API](#api)
  - [Maven dependencies](#maven-dependencies)
  - [Authors](#authors)
  - [License](#license)
	
## How it works
The [java instrumentation package](http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html) introduced in Java version 1.5, provides a simple way to transform java-class definition at loading time, consisting basically in a `byte[]` to `byte[]` transformation, by the so called "java agents".

Since Java version 1.6 these agents can perform also perform dynamic instrumentation, that is retransforming the bytecode of classes already loaded. 

This library provides an configurable agent ([org.brutusin.btrace.Init](src/main/java/org/brutusin/bctrace/Init.java)) aimed at injecting custom [hooks](src/main/java/org/brutusin/bctrace/spi/Hook.java) into the code of the specified methods of the target application.


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
## Usage
Agent projects making use of this library must create a **fat-jar** including all their dependencies. 
Agent jars must contain at least this entry in its manifest:
```
Premain-Class: org.brutusin.bctrace.Init
```
This fat-jar is the agent jar that will be passed as an argument to the java command:

```
-javaagent:thefat.jar
```

## Registering hooks
On agent bootstrap, a resource called `.bctrace` (if any) is read by the agent classloader (root namespace), where the initial (before class-loading) hook implementation class names are declared.

The agent also offers an API for registering hooks dynamically.

## API
These are the main types to consider:

### BcTrace
[`BcTrace`](src/main/java/org/brutusin/bctrace/Bctrace.java) class offers a singleton instance that allows to register/unregister hooks dinamically from code.

### Hook
[`Hook`](src/main/java/org/brutusin/bctrace/spi/Hook.java) class represents the main abstraction that client projects has to implement. Hooks are registered programatically using the previous API, or statically from the descriptor file (see ["registering hooks"](#registering-hooks)).

Hooks offer two main functionalities: 
- Filtering information (what methods to instrument)  
- Event callback (what actions to perform under the execution events ocurred in the intrumented methods)

### Instrumentation
On hook initialization, the framework passes a unique instance of [`Instrumentation`](https://github.com/ShiftLeftSecurity/instrumentation/blob/master/src/main/java/org/brutusin/bctrace/spi/Instrumentation.java)  to the hook instances, to provide them retransformation capabilities, as well as accounting of the classes affected they are instrumenting.

### MethodRegistry
[`MethodRegistry`](src/main/java/org/brutusin/bctrace/runtime/MethodRegistry.java) offers a singleton instance that provides O(1) id (int) to/from method translations.

### FrameData
[`FrameData`](src/main/java/org/brutusin/bctrace/runtime/FrameData.java) objects contain all the information about a execution frame, method, arguments and target object. This object are passed by the framework to the listeners for every execution event.

## Maven dependency 

```xml
<dependency>
    <groupId>org.brutusin</groupId>
    <artifactId>bctrace</artifactId>
</dependency>
```

## Main stack
This module could not be possible without:
* [org.ow2.asm:asm-all](http://asm.ow2.org/)

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


