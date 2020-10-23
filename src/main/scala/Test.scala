import com.typesafe.config.ConfigFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.mahout.text.wikipedia.XmlInputFormat

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.xml.{Elem, NodeSeq}

object Test {

  def getPublishers(publication: Elem): Option[NodeSeq] ={
    val list_of_authors = publication \ "author"
    val editor_tags = publication \ "editor"
    // Only one type for the publisher tag
    if(list_of_authors.length > 0 && editor_tags.length > 0){
      println("NOT VALID XML")
      None
    }
    if (editor_tags.length > 0) Some(editor_tags) else Some(list_of_authors)
  }
  def getPublication(publication: Elem): ListBuffer[Option[String]] ={
    var l = new mutable.ListBuffer[Option[String]]()
    val title_tags = publication \ "title"
    val booktitle_tags = publication \ "booktitle"
    val journal_tags = publication \ "journal"
    // SANITY CHECK => Only one type for the publication
    if(title_tags.isEmpty && booktitle_tags.isEmpty && journal_tags.isEmpty){
      println("No Publication Found")
      None
    }
    if (title_tags.length > 0) {
      val title_tag = title_tags.mkString
      val size = title_tag.length
      val title = title_tag.slice(7, size - 8)
      l += Some(title)
    }
    if (booktitle_tags.length > 0){
      val booktitle_tag = booktitle_tags.mkString
      val size = booktitle_tag.length
      val booktitle = booktitle_tag.slice(11, size - 12)
      l += Some(booktitle)
    }
    if(journal_tags.length > 0){
      val journal_tag = journal_tags.mkString
      val size = journal_tag.length
      val journal = journal_tag.slice(9, size - 10)
      l += Some(journal)
    }
    l
  }
  def main(args: Array[String]): Unit = {
    // Load application settings
    val settings = new Settings(ConfigFactory.load())
    val configuration = new Configuration
    //configuration.getStrings() Use this for slice size of tags
    configuration.setStrings("xmlinput.start", settings.xmlInputStartTags: _*)
    configuration.setStrings("xmlinput.end", settings.xmlInputEndTags: _*)
    val job = Job.getInstance(configuration, "topAuthorsByVenue")
    job.setInputFormatClass(classOf[XmlInputFormat])
    job.setJarByClass(this.getClass)
    job.setMapperClass(classOf[topAuthorsByVenue.VenueMapper])
    //job.setCombinerClass(classOf[IntSumReader])
    job.setReducerClass(classOf[topAuthorsByVenue.TopAuthorReader])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])
    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))
    System.exit(if (job.waitForCompletion(true)) 0 else 1)
  }
}