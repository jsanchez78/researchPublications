import java.util.StringTokenizer

import javax.xml.stream.XMLInputFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.{FileInputFormat, FileSplit, TextInputFormat}
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.mapreduce.{Job, Mapper, Reducer}
import org.apache.mahout.text.wikipedia.XmlInputFormat
import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`
import scala.xml.XML

object map_reduce {

  class TokenizerMapper extends Mapper[Object, Text, Text, IntWritable] {

    val one = new IntWritable(1)
    val word = new Text()
    override def map(key: Object, value: Text, context: Mapper[Object, Text, Text, IntWritable]#Context): Unit = {
     val s = value.toString
     val xml = XML.loadString(s)
      word.set(xml.text)
      context.write(word, new IntWritable(1))
    }
  }
  class IntSumReader extends Reducer[Text,IntWritable,Text,IntWritable] {
    // Cleaner type definition in reduce method signature
    type Context = Reducer[Text, IntWritable, Text, IntWritable]#Context
    override
    def reduce(key: Text, values: java.lang.Iterable[IntWritable], context: Context): Unit = {
      val sum = values.foldLeft(0){ (word, count) => word + count.get()}
      context.write(key, new IntWritable(sum))
    }
  }
}