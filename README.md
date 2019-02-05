# LoggerGenerator

## Abstract
LoggerGenerator is a code generator tool from a description file, following the practices from [Looking4Q Cutting Edge Logging Practices]
(https://looking4q.blogspot.com/2018/11/logging-cutting-edge-practices.html)

Gradle plugin and examples: https://github.com/albertonavarro/loggergenerator-gradle-plugin

Blog entry with more usage output including HTML documentation: https://looking4q.blogspot.com/2019/01/level-up-logs-and-elk-contract-first.html

Quick list of reasons to use a generator like this:

* Structured Arguments is required or desirable
* You might need consistency within a large project so same concepts are always represented by the same keys.
* By all means you require type consistency, bear in mind that logs won't make it to ElasticSearch if type inconsistency happens. And you don't know how else to enforce it.
* You value having some documentation about your logs that you can share with other teams, so they know how to search in your logs.

## Building LoggerGenerator
LoggerGenerator is made in Kotlin using Gradle as build tool.

`git clone https://github.com/albertonavarro/loggergenerator`

and 

`./gradlew clean build` 

## Using LoggerGenerator

LoggerGenerator takes few arguments and a description file to generate the utils java class.

--input `log descriptor path` => Takes the absolute path of the log description file. Formats and examples below.

--package-name `package name` => Takes the desired package for the generated code.

--codegen-output `output folder` => Takes the final desired destination for the code.

--class-name `name` => Logger Utils generated java class name.

--html-name `name` => Logger Utils generated html file name.

--html-output `output folder` => Takes the final desired destination for the html documentation.

--compat-1.7 | --compat-1.8 => Generates code with a set of features compabible with given Java version

### Log Description File format

Description is in YAML format with the following tree structure:

#### Version 1

```
Root
├ version: 1
├ project-name: Project name.
├ mappings: List of mapping entries (0..N)
│  ├ name: variable name
│  │ type: java class for the variable
│  │ description: free text to clear up ambiguities
│  ├ name: variable name
│  │ type: java class for the variable
│  │ description: free text to clear up ambiguities
│  ...
├ sayings: List of saying entries (0..N)
│  ├ code: saying code
│  │ message: log message
│  │ variables: required variables (0..N)
│  │  ├ name: mappings-declared name
│  │  ├ name: other mappings-declared name
│  │  └ name: yet another mappings-declared name
│  │ extradata: custom information for the log entry (0..N)
│  │  ├ key: value
│  │  └ key: value
│  ...
└ context: Set of keys that you might expect as part of the logging context system (MDC in slf4j)
   ├ key existing in mappings
   ├ other key existing in mapping
   ...
```
     
Example:

```yaml
version: 1
project-name: coin-example
mappings:
  - name: amount
    type: java.lang.Integer
    description: Amount of money to match, in minimum representation (no decimals).
  - name: combinations
    type: java.lang.Integer
    description: Total number of combinations of change.
  - name: coins
    type: int
    description: Number of coins in a combination.
  - name: iid
    type: java.util.UUID
    description: Interaction id.
sentences:
  - code: ResultCombinations
    message: "Number of combinations of getting change"
    variables:
      - amount
      - combinations
    extradata: {}
    defaultLevel: info
  - code: ResultMinimum
    message: "Minimum number of coins required"
    variables:
      - amount
      - coins
    extradata: {}
    defaultLevel: info
context:
  - iid
```

### Expected result

For the given example, and the parameters --package-name=com.navid.codegen and --compat-1.8, the expected output must look like this:

```java
package com.navid.codegen;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

import java.lang.Integer;
import java.lang.Iterable;
import net.logstash.logback.argument.StructuredArgument;
import org.slf4j.Logger;

public final class LoggerUtils {
  public static StructuredArgument kvAmount(Integer amount) {
    return keyValue("amount",amount);
  }

  public static StructuredArgument aAmount(Iterable<Integer> amount) {
    return new net.logstash.logback.marker.ObjectAppendingMarker("amount",amount);
  }

  public static StructuredArgument aAmount(Integer... amount) {
    return new net.logstash.logback.marker.ObjectAppendingMarker("amount",amount);
  }

  public static StructuredArgument kvCombinations(Integer combinations) {
    return keyValue("combinations",combinations);
  }

  public static StructuredArgument aCombinations(Iterable<Integer> combinations) {
    return new net.logstash.logback.marker.ObjectAppendingMarker("combinations",combinations);
  }

  public static StructuredArgument aCombinations(Integer... combinations) {
    return new net.logstash.logback.marker.ObjectAppendingMarker("combinations",combinations);
  }

  public static StructuredArgument kvCoins(int coins) {
    return keyValue("coins",coins);
  }

  public static StructuredArgument aCoins(Iterable<Integer> coins) {
    return new net.logstash.logback.marker.ObjectAppendingMarker("coins",coins);
  }

  public static StructuredArgument aCoins(int... coins) {
    return new net.logstash.logback.marker.ObjectAppendingMarker("coins",coins);
  }

  public static void auditResultCombinations(Logger logger, Integer amount, Integer combinations) {
    logger.info("Number of combinations of getting change {} {}",kvAmount(amount),kvCombinations(combinations));
  }

  public static void auditResultCombinations(TriConsumer logger, Integer amount,
      Integer combinations) {
    logger.accept("Number of combinations of getting change {} {}",kvAmount(amount),kvCombinations(combinations));
  }

  public static void auditResultMinimum(Logger logger, Integer amount, int coins) {
    logger.info("Minimum number of coins required {} {}",kvAmount(amount),kvCoins(coins));
  }

  public static void auditResultMinimum(TriConsumer logger, Integer amount, int coins) {
    logger.accept("Minimum number of coins required {} {}",kvAmount(amount),kvCoins(coins));
  }

  public interface MonoConsumer {
    void accept(String var1);
  }

  public interface BiConsumer {
    void accept(String var1, Object var2);
  }

  public interface TriConsumer {
    void accept(String var1, Object var2, Object var3);
  }

  public interface ManyConsumer {
    void accept(String var1, Object... var2);
  }
  
  public static void setContextIid(UUID iid) {
    org.slf4j.MDC.put("ctx.iid",String.valueOf(iid));
  }
    
  public static void removeContextIid() {
    org.slf4j.MDC.remove("iid");
  }
    
  public static void resetContext() {
    org.slf4j.MDC.clear();
  }
}
```

This code could be used like:

```
import static com.navid.codegen.LoggerUtils.*;
import static java.util.Arrays.asList;

[...]
        setContextIid(UUID.randomUUID());
        int amount = 12;
        int[] coins = {2, 4, 5};
        
        //using mappings outside audit sentences
        logger.info("Input (using array) {} {}", kvAmount(12), aCoins(coins));
        logger.info("Input (using iterable) {} {}", kvAmount(12), aCoins(asList(9,9,9)));

        //forcing log level in sentences
        auditResultCombinations(logger::warn, amount, change(coins, amount));

        //using default log level in sentences
        auditResultMinimum(logger, amount, minimumCoins(coins, amount));
        removeContextIid();
```

## Future roadmap

* Improving framework support, so other non-logback libraries are supported.
* Cardinality in sentences, and whether they are lists, arrays or varargs
* Improve naming customization
* Other languages
