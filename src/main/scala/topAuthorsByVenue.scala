import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, LongWritable, NullWritable, ObjectWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.mapreduce.{Job, Mapper, Reducer}
import org.apache.mahout.text.wikipedia.XmlInputFormat
import scala.collection.convert.ImplicitConversions.{`iterable AsScalaIterable`, `iterable asJava`}
import scala.collection.{immutable, mutable}
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks.break
import scala.xml.{SAXParseException, XML}

/*
*
*   TODO: Next, you will produce the list of publications for each venue that contain the highest number of authors for each of these venues.
*
*
* (venue, values)
*
*
* (venue, [publication1, publication2, .... , ]
* */
object topAuthorsByVenue {

  //(venue, List[Text])
  class VenueMapper extends Mapper[Object, Text, Text, Text] {
    val venue = new Text()
    val all_authors_for_venue = new Text()
    val l = List()
    // (venue, xml of articles w/ matching venue)
    override def map(key: Object, value: Text, context: Mapper[Object, Text, Text, Text]#Context): Unit = {
      val article = XML.loadString(value.toString)
      val venue_key = (article \ "@key").text.split("/")
      val _venue = venue_key(0) + "/" + venue_key(1)
      venue.set(_venue)
      context.write(venue, value)
    }
  }
  // (xml of articles to particular venue , List of top ten authors)
  class TopAuthorReader extends Reducer[Text, Text, Text, Text] {
    // Cleaner type definition in reduce method signature
    type Context = Reducer[Text, Text, Text, Text]#Context
    val top = new Text()
    override
    def reduce(key: Text, values: java.lang.Iterable[Text], context: Context): Unit = {
      val list = values.toList
      var soFar = 0   // Num of max publishers
      var list_of_publications = new mutable.ListBuffer[String]()
      try{
        list.forEach( v =>
        {
          val article = XML.loadString(v.toString)
          val venue_key = (article \ "@key").text.split("/")
          val current_venue = venue_key(0) + "/" + venue_key(1)
          // Matching venue
          if(key.toString.equals(current_venue)){
            // Get Publishers From Safe check function
            val publishers = Test.getPublishers(article).get.toList
            // Update Highest Number of authors
            soFar = soFar max publishers.length
          }
          // Not a Matching Venue
        }
        )
      }
      catch{
        case e: SAXParseException => println("Unable to Parse!")
      }
      // Traverse again and populate List[publications]
      list.forEach( v => {
        val article = XML.loadString(v.toString)
        val venue_key = (article \ "@key").text.split("/")
        val current_venue = venue_key(0) + "/" + venue_key(1)
        // Matching venue
        if (key.toString.equals(current_venue)) {
          val publishers = Test.getPublishers(article).get.toList
          if (soFar == publishers.length){
            val publication_list = Test.getPublication(article)
            publication_list.foreach(p =>
              list_of_publications += p.get + ", "
            )
          }
        }
      })
      // Write list of publications for that venue
      context.write(key, new Text(list_of_publications.distinct.mkString))
    }
  }
}
