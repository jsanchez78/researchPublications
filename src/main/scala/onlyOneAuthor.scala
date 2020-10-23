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
import scala.collection.mutable.ListBuffer
import scala.collection.{immutable, mutable}
import scala.xml.{SAXParseException, XML}

/*
*
*   TODO: Then, for each venue you will produce the list of publications that contains only one author.
*   Output:
*     (venue, [title, title, title]
*
* */
object onlyOneAuthor {
  //(venue, List[Text])
  class VenueMapper extends Mapper[Object, Text, Text, Text] {
    val word = new Text()
    // (venue, xml of articles w/ matching venue)
    override def map(key: Object, value: Text, context: Mapper[Object, Text, Text, Text]#Context): Unit = {
      val article = XML.loadString(value.toString)
      val venue = (article \ "@key").mkString
      word.set(venue)
      context.write(word, value)
    }
  }
  class TitlesReader extends Reducer[Text, Text, Text, Text] {
    // Cleaner type definition in reduce method signature
    type Context = Reducer[Text, Text, Text, Text]#Context
    override
    def reduce(key: Text, values: java.lang.Iterable[Text], context: Context): Unit = {
      val list = values.toList
      val list_of_titles = new mutable.ListBuffer[String]()
      try {
        list.forEach(v => {
          val article = XML.loadString(v.toString)
          val _title = (article \ "title").mkString
          // Get Publishers from function which safe checks
          val publishers = Test.getPublishers(article).get
          if(publishers.toList.length == 1) {
            val size = _title.length
            val title = _title.slice(7, size - 8)
            list_of_titles += title
          }
        })
      }
      catch {
        case e: SAXParseException => println("Unable to Parse!")
      }
      context.write(key, new Text(list_of_titles.mkString))
    }
  }
}