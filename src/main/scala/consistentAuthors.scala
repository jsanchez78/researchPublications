import java.util.StringTokenizer

import javax.xml.stream.XMLInputFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.{FileInputFormat, FileSplit, TextInputFormat}
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.mapreduce.{Job, Mapper, Reducer}
import org.apache.mahout.text.wikipedia.XmlInputFormat

import scala.collection.convert.ImplicitConversions.{`iterable AsScalaIterable`, `iterable asJava`}
import scala.collection.{immutable, mutable}
import scala.collection.mutable.ListBuffer
import scala.xml.{SAXParseException, XML}
/*
*   TODO:
*    Second, you will compute the list of authors who published without interruption for N years where 10 <= N
*   [a1, a2, a3, ... ] where an author published consistently for >= 10 years
*
*
*
* */
object consistentAuthors {
  class PublisherMapper extends Mapper[Object, Text, Text, IntWritable] {
    val publisher = new Text()
    val year_IntWritable = new IntWritable()
    override def map(key: Object, value: Text, context: Mapper[Object, Text, Text, IntWritable]#Context): Unit = {
      val article = XML.loadString(value.toString)
      // Get Publishers From Safe check function
      val publishers = Test.getPublishers(article).get.toList
      val year_string = (article \ "year").mkString
      val size = year_string.length
      val year = year_string.slice(6, size - 7).toInt
      // (Text, IntWritable) => (publisher, year_published)
      for (p <- publishers){
        val publisher_node = p.toString
        val size = publisher_node.length
        val _publisher = publisher_node.slice(8, size - 9)
        publisher.set(_publisher)
        year_IntWritable.set(year)
        context.write(publisher, year_IntWritable)
      }
    }
  }
  // (xml of articles to particular venue , List of top ten authors)
  class ConsistentPublishing extends Reducer[Text, IntWritable, Text, Text] {
    // Cleaner type definition in reduce method signature
    type Context = Reducer[Text, IntWritable, Text, Text]#Context
    override
    def reduce(key: Text, values: java.lang.Iterable[IntWritable], context: Context): Unit = {
      var consistent_authors = new mutable.ListBuffer[String]()
      var list_of_of_dates = new mutable.ListBuffer[Int]()
      val publications = new Text()
      val curr = key.toString //year
      try {
        values.forEach(v => {
          // Prepend to list
          list_of_of_dates += v.get()
          println(list_of_of_dates)
        })
      }
      catch {
        case e: SAXParseException => println("Unable to Parse!")
      }
        //Sort Elements by key (low => high)
        val sorted_list = list_of_of_dates.sortWith(_ < _)
        var soFar = 0
        // Traverse through valueSet for each author
        for(ListBuffer(a, b) <- sorted_list.grouped(2)){
          println(b - a)
          if(b - a == 0 || b - a == 1) {
            soFar += 2
          }
        }
        if (soFar >= 10) {
          consistent_authors += curr
          println(consistent_authors.mkString)
          publications.set("Published consistently for at least 10 years")
          println(consistent_authors.toString())
          val l = consistent_authors.distinct.mkString
          context.write(publications, new Text(l))
        }
    }
  }
}