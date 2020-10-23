# CS441 - Engineering Distributed Objects for Cloud Computing

# HW 02 - Distributed Computation On Research Publications

## Overview
The objective is to process the raw [XML-based DBLP](https://dblp.uni-trier.de)
 dataset and perform various parallel distributed computation on the set via the [Hadoop Map-Reduce framework](https://hadoop.apache.org).

## Results
![](src/Screenshots/Task5Compile.png)
![](src/Screenshots/Task5Processing.png)

![](src/Screenshots/Task5Output.png)

## Installation

To use this framework, be sure to have the following software installed:  

* [HDP Sandbox](https://www.cloudera.com/downloads/hortonworks-sandbox.html)
* [sbt 1.3.x ](https://www.scala-sbt.org/download.html)
* [Scala  2.13.x](https://www.scala-lang.org/download/) 
* [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)

To install this package into a project:  

1.  Download cs441_Fall2020_HW01

    ```  
    git clone https://jsanch75@bitbucket.org/jsanch75/jacob_sanchez_hw2.git
    ```
2. Navigate to the project root folder  

    ```
    cd CS441/
    ```
3. Execute the command on the command line:

    ```
    sbt clean compile assembly  
    ```  
4. Run Hadoop Command on Sandbox

```
 hadoop jar target/scala-2.13/CS441-assembly-0.1.jar src/main/resources/dblp.xml src/main/resources/output
```  
   
## Map Reduce Jobs

1. Compute a spreadsheet or an CSV file that shows top ten published authors at each venue.
2. Compute the list of authors who published without interruption for N years where 10 <= N.
3. For each venue you will produce the list of publications that contains only one author.
4. Produce the list of publications for each venue that contain the highest number of authors for each of these venues.
5. Produce the list of top 100 authors in the descending order who publish with most co-authors and the list of 100 authors who publish without any co-authors.

### Mapping Input XML

The various mappers extract the publication tags from the [dblp.xml file](https://dblp.uni-trier.de/xml/) associated with the shards via Scala's [scala-xml](https://scala.github.io/scala-xml/api/1.2.0/scala/xml/) module.
```
(<author> or <editor> )
```

### Reducing The Output From The Mappers
Depending on the reducer, the key is mapping to an IntWritable, MapWritable, or Text for the output.

1. Reducer maps (Text, Text) => list of publications per venue.
2. Reducer maps (Text, IntWritable) => List of publishers where the value is >= 10.
3. Reducer maps (Text, Text) => List of publication titles
4. Reducer maps (Text, Text) => (venue, List of publications)
5. (Text, MapWritable) => List of authors based on MapWritable key 


### Future Work

The Mappers and Reducers can easily incorporate the functionality of supporting multi-tag class to differentiate 
between the following:
```
<phdthesis> , <masterthesis>, etc...
```