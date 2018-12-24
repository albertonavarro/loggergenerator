# LoggerGenerator

## Abstract
LoggerGenerator is a code generator tool from a description file, following the practices from https://looking4q.blogspot.com/2018/11/logging-cutting-edge-practices.html 

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

--package `package name` => Takes the desired package for the generated code.

--output `output folder` => Takes the final desired destination for the code.

### Log Description File format

Description is in YAML format with the following tree structure:

#### Version 1

```
Root
├ version: 1
├ mappings: List of mapping entries (0..N)
│  ├ name: variable name
│  │ type: java class for the variable
│  │ description: free text to clear up ambiguities
│  ├ name: variable name
│  │ type: java class for the variable
│  │ description: free text to clear up ambiguities
│  ...
└ sayings: List of saying entries (0..N)
   ├ code: saying code
   │ message: log message
   │ variables: required variables (0..N)
   │  ├ name: mappings-declared name
   │  ├ name: other mappings-declared name
   │  └ name: yet another mappings-declared name
   │ extradata: custom information for the log entry (0..N)
   │  ├ key: value
   │  └ key: value
   ...
```
     
Example:

```yaml
version: 1
mappings:
  - name: objectId
    type: java.lang.String
    description: objectId represents the market object id
  - name: status
    type: java.lang.Number
    description: status represents the purchase status
sayings:
  - code: COD_1
    message: "something went wrong with this other thing"
    variables:
      - objectId
      - status
    extradata:
      key: value
```

### Expected result

For the given example, and the parameters --package=com.navid.codegen , the expected output must look like this:

```java
package com.navid.codegen;

import static net.logstash.logback.argument.StructuredArguments.*;

import java.lang.Iterable;
import java.lang.Number;
import java.lang.String;
import net.logstash.logback.argument.StructuredArgument;

public final class LoggerUtils {
  public static StructuredArgument kvObjectId(String objectId) {
    return keyValue("objectId",objectId);
  }

  public static StructuredArgument aObjectId(Iterable<String> objectId) {
    return array("objectId",objectId);
  }

  public static StructuredArgument aObjectId(String... objectId) {
    return array("objectId",objectId);
  }

  public static StructuredArgument kvStatus(Number status) {
    return keyValue("status",status);
  }

  public static StructuredArgument aStatus(Iterable<Number> status) {
    return array("status",status);
  }

  public static StructuredArgument aStatus(Number... status) {
    return array("status",status);
  }
}
```

This code could be used like:

```
import static com.navid.codegen.LoggerUtils.kvObjectId;

[...]

logger.info("Market has been created {}", kvObjectId(market.getId()));
```

## Future roadmap

* Generation of Sayings, they are mandated in the format but not yet generated.
* Generation of HTML for documentation.
* Improving framework support, so other non-logback libraries are supported.
* Improve naming customization
* Other languages
