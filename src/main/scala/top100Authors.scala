import org.apache.hadoop.io.{IntWritable, MapWritable, Text, Writable}
import org.apache.hadoop.mapreduce.{Mapper, Reducer}

import scala.collection.convert.ImplicitConversions.{`collection asJava`, `iterable AsScalaIterable`}
import scala.collection.mutable
import scala.xml.{SAXParseException, XML}

/*
*   TODO:  Finally, you will produce the list of top 100 authors in the descending order who publish with most co-authors and the list of 100 authors who publish without any co-authors.
*
*
*   ( "Top 100 authors w/ most co-authors", List[a1, a2, ... ])
*   ( "Top 100 authors w/ only one author", List[a1, a2, ... ])
*
* */
object top100Authors {
  //(venue, List[Text])
  class top100Mapper extends Mapper[Object, Text, Text, MapWritable] {
    val word = new Text()
    val publisher_to_num = new MapWritable()
    // (venue, xml of articles w/ matching venue)
    override def map(key: Object, value: Text, context: Mapper[Object, Text, Text, MapWritable]#Context): Unit = {
      val article = XML.loadString(value.toString)
      // Get Publishers From Safe check function
      val publishers = Test.getPublishers(article).get.toList
      // Author
      if (publishers.length == 1){
        publishers.foreach( p => {
          val publisher_node = p.toString
          val size = publisher_node.length
          val _publisher = publisher_node.slice(8, size - 9)
          publisher_to_num.put(new Text(_publisher), new IntWritable(1))
          word.set("Author")
        })
      }
      // Co-author
      else{
        publishers.foreach({ p =>
          val publisher_node = p.toString
          val size = publisher_node.length
          val _publisher = publisher_node.slice(8, size - 9)
          publisher_to_num.put(new Text(_publisher), new IntWritable(publishers.length - 1))
          word.set("Co-author")
        })
      }
      // Key Value is irrelevant in this case
      context.write(word, publisher_to_num)
    }
  }
  class top100Reader extends Reducer[Text, MapWritable, Text, Text] {
    val top100 = new Text()
    // Cleaner type definition in reduce method signature
    type Context = Reducer[Text, MapWritable, Text, Text]#Context
    override
    def reduce(key: Text, values: java.lang.Iterable[MapWritable], context: Context): Unit = {
      val list = values.toList
      val author_to_num = new mutable.HashMap[String, Int]()
      var soFar = 0
      try {
        list.forEach(l => {
          l.forEach( (k,v) => {
            val author = k.asInstanceOf[Text].toString
            val num = v.asInstanceOf[IntWritable].get()
            author_to_num.put(author, num)
          }
          )
        })
      }
      catch {
        case e: SAXParseException => println("Unable to Parse!")
      }
      // Generate list of tuples
      val top100_coauthors = new mutable.ListBuffer[(String, Int)]()
      author_to_num.foreach( pair => top100_coauthors :+  (pair._1, pair._2))
      // Sort list of tuples
      val sorted_list = top100_coauthors.sortWith(_._2 > _._2)
      val tmp = new mutable.ListBuffer[String]
      val n = if(sorted_list.length == 100) 100 else sorted_list.length
      for (_ <- 0 to n) {
        sorted_list.foreach( pair => tmp :+ (pair._1 + ": " + pair._2 + ", "))
        top100.set(tmp.mkString)
        context.write(key, top100)
      }
    }
  }
}
