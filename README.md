# CS441 - Engineering Distributed Objects for Cloud Computing

# HW 02 Distributed Computation On Research Publications

## Overview
The objective is to process the raw [XML-based DBLP](https://dblp.uni-trier.de)
 dataset and perform various parallel distributed computation on the set via the [Hadoop Map-Reduce framework](https://hadoop.apache.org).

## Installation

To use this framework, be sure to have the following software installed:  

* [HDP Sandbox](https://www.cloudera.com/downloads/hortonworks-sandbox.html)
* [sbt 1.3.x ](https://www.scala-sbt.org/download.html)
* [Scala  2.13.x](https://www.scala-lang.org/download/) 
* [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)

To install this package into a project:  

1.  Download cs441_Fall2020_HW01

    ```  
    git clone https://jsanch75@bitbucket.org/jsanch75/cs441_fall2020_hw01.git
    ```
2. Navigate to the project root folder  

    ```
    cd cs441_Fall2020_HW01/  
    ```
3. Execute the command on the command line:

    ```
    sbt clean compile assembly  
    ```  
   
   
## Map Reduce Job

### Mapping Input XML

The various mappers extract the publication tags from the [dblp.xml file](https://dblp.uni-trier.de/xml/) associated with the shards via Scala's [scala-xml](https://scala.github.io/scala-xml/api/1.2.0/scala/xml/) module.
```
(<author> or <editor> )
```

### Reducing The Output From The Mappers

Depending on the reducer, the key is mapping to an IntWritable, MapWritable, or Text for the output.

