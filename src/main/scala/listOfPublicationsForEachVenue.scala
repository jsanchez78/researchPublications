import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.{Mapper, Reducer}
import org.apache.mahout.text.wikipedia.XmlInputFormat
import scala.collection.convert.ImplicitConversions.{`iterable AsScalaIterable`, `iterable asJava`}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.xml.{SAXParseException, XML}

/*
TODO:
    Next, you will produce the list of publications for each venue that contain the highest number of authors for each of these venues.

    Publications:
     [<mastersthesis, <phdthesis, <book, <proceedings, <inproceedings, <article, <incollection]
    Input:
      (venue, values)
    Output:
      venue, [title1, title2, ... ]
    */

object listOfPublicationsForEachVenue {
  class TokenizerMapper extends Mapper[Object, Text, Text, Text] {
    val publisher = new Text()
    // Creating empty HashMap
    val venue_to_articles = new mutable.HashMap[String, List[Text]]
    // (venue, xml of articles w/ matching venue)
    override def map(key: Object, value: Text, context: Mapper[Object, Text, Text, Text]#Context): Unit = {
      val xml = XML.loadString(value.toString)
      val editor = xml \ "editor"
      val author = xml \ "author"
      val tmp = if(editor.isEmpty) author else editor
      publisher.set(tmp.mkString)
      context.write(publisher, value)
    }
  }
  // (xml of articles to particular venue , List of top ten authors)
  class IntSumReader extends Reducer[Text, Text, Text, Text] {
    // Cleaner type definition in reduce method signature
    type Context = Reducer[Text, Text, Text, Text]#Context
    override
    def reduce(key: Text, values: java.lang.Iterable[Text], context: Context): Unit = {
      val list = values.toList
      var list_of_titles = new ListBuffer[String]()
      try {
        list.forEach(v => {
          val article = XML.loadString(v.toString)
          val t = article \ "title"
          val s = t.mkString
          val size = s.length
          list_of_titles.append(s.slice(7, size - 8))
        })
      }
      catch {
        case e: SAXParseException => println("Unable to Parse!")
      }
      // Populate Title list
      val list_of_authors = list_of_titles.mkString
      println(list_of_authors)
      // (venue, List[authors])
      context.write(key, new Text(list_of_authors))
    }
  }
}
