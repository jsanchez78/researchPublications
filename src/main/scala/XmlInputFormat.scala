import java.io.IOException

import org.apache.hadoop.fs.FSDataInputStream
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.DataOutputBuffer
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.FileSplit
import org.apache.hadoop.mapred.InputSplit
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapred.RecordReader
import org.apache.hadoop.mapred.Reporter
import org.apache.hadoop.mapred.TextInputFormat
import org.apache.hadoop.mapreduce.TaskAttemptContext
/**
 * Reads records that are delimited by a specifc begin/end tag.
 */
object XmlInputFormat {
  val START_TAG_KEY = "xmlinput.start"
  val END_TAG_KEY = "xmlinput.end"
  /**
   * XMLRecordReader class to read through a given xml document to output xml
   * blocks as records as specified by the start tag and end tag
   *
   */
  class XmlRecordReader @throws[IOException]
  (val split: FileSplit, val jobConf: JobConf) extends RecordReader[LongWritable, Text]{

    final private var startTag: Array[Array[Byte]] = null
    final private var endTag: Array[Array[Byte]] = null
    final private var start = 0L
    final private var `end` = 0L
    final private var fsin: FSDataInputStream = null
    final private val buffer = new DataOutputBuffer
    // Track the start tag which was matched and for which the file contents are to be buffered until the corresponding
    // end tag is encountered
    private var matching_tag = Array[Byte]()
    startTag = jobConf.getStrings(START_TAG_KEY).map(_.getBytes("utf-8"))
    endTag = jobConf.getStrings(END_TAG_KEY).map(_.getBytes("utf-8"))
    // open the file and seek to the start of the split
    start = split.getStart
    `end` = start + split.getLength
    val file: Path = split.getPath
    val fs: FileSystem = file.getFileSystem(jobConf)
    fsin = fs.open(split.getPath)
    fsin.seek(start)

    @throws[IOException]
    override def next(key: LongWritable, value: Text): Boolean = {
      if (fsin.getPos < `end`) if (readUntilMatch(startTag, false))
        try {
          buffer.write(matching_tag)
          if (readUntilMatch(endTag, true)) {
            key.set(fsin.getPos)
            value.set(buffer.getData, 0, buffer.getLength)
            return true
          }
        } finally buffer.reset
      false
    }

    override def createKey = new LongWritable

    override def createValue = new Text

    @throws[IOException]
    override def getPos: Long = fsin.getPos

    @throws[IOException]
    override def close(): Unit = {
      fsin.close()
    }

    @throws[IOException]
    override def getProgress: Float = (fsin.getPos - start) / (`end` - start).toFloat

    @throws[IOException]
    private def readUntilMatch(_match: Array[Array[Byte]], withinBlock: Boolean): Boolean = {
      val _match_counter: Array[Int] = _match.indices.map(_ => 0).toArray
      var i = 0
      while (true) {
        val b: Int = fsin.read
        // end of file:
        if (b == -1) return false
        // save to buffer:
        if (withinBlock) buffer.write(b)

        _match.indices.foreach{ tag_index =>
          val tag = _match(tag_index)

          if (b == tag(_match_counter(tag_index))) {
            i += 1
            // MATCH FOUND
            if (_match.length <= _match_counter(tag_index)){
              matching_tag = tag
              return true
            }
          }
          else
            _match_counter(tag_index) = 0 // RESET
        }
        if(!withinBlock && _match_counter.forall(_ == 0) && fsin.getPos >= `end`) return false
      }
      false
    }

  }

}

class XmlInputFormat extends TextInputFormat {
  @throws[IOException]
  override def getRecordReader(inputSplit: InputSplit, jobConf: JobConf, reporter: Reporter) = new XmlInputFormat.XmlRecordReader(inputSplit.asInstanceOf[FileSplit], jobConf)
}

