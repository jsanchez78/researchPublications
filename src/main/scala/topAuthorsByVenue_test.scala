import java.util

import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, NullWritable, Text}
import org.apache.hadoop.mapreduce.{Job, Mapper, Reducer}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.mahout.text.wikipedia.XmlInputFormat

import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks.break
import scala.xml.XML
import org.apache.hadoop.conf.Configuration

/*
    MAP
    (venue, values)
    (venue , <author, int>)
*
*
*   REDUCE
    (venue , List[author])

* */

object topAuthorsByVenue_test {
  class TokenizerMapper extends Mapper[Object, Text, Text, util.TreeMap[String, Int]] {
    val author_to_num = new util.TreeMap[String, Int]
    val one = new Text()
    val word = new Text()

    override def map(key: Object, value: Text, context: Mapper[Object, Text, Text, util.TreeMap[String, Int]]#Context): Unit = {
      val s = value.toString
      val article = XML.loadString(s)
      val venue = article \ "@key"
      val authors = (article \ "author")
      word.set(venue.text)

      for (a <- authors) {
        val author = a.mkString
        val soFar = author_to_num.getOrDefault(author, 1)
        author_to_num.put(author, soFar)
      }
      if (author_to_num.size() > 10) {
        author_to_num.remove(author_to_num.firstKey())
      }

      context.write(word, author_to_num)
    }
  }
  /*
    type Context = Mapper[Object, Text, Text, IntWritable]#Context
    override def cleanup(context: Context): Unit = {
      author_to_num.forEach(
        (author, n) => context.write()
      )
      for (t <- author_to_num.values())
        context.write(, t)
    }
  }
   */

  class IntSumReader extends Reducer[Text,Text,Text,Text] {
    val word = new Text()
    // Cleaner type definition in reduce method signature
    type Context = Reducer[Text, Text, Text, Text]#Context
    override def reduce(key: Text, values: java.lang.Iterable[Text], context: Context): Unit = {
      val s = key.toString
      val article = XML.loadString(s)
      val venue = article \ "@key"
      word.set(venue.text)
      val authors = article \ "author"
      val top_ten = new ListBuffer[String]
      // Creating empty HashMap
      val authors_map = new mutable.HashMap[String, Int]
      // Populate Map (author, num)
      for(a <- authors){
        authors_map.put(a.text,  authors_map.getOrElse(a.text, 1) + 1)
      }
      //Sort Elements by key (high => low)
      val sorted_list = mutable.ListMap(authors_map.toSeq.sortWith(_._2 > _._2):_*)
      var i = 0
      for(a <- sorted_list.keysIterator){
        if (i == 10)
          break
        top_ten.append(a)
        i += 1
      }
      // (venue, List[authors])
      context.write(word, new Text(top_ten.toList.mkString))
    }
  }
}
