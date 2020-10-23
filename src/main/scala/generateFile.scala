import java.io.{BufferedWriter, File, FileWriter}

import scala.collection.mutable.ListBuffer

object generateFile {
  def writeToFile(authors: ListBuffer[String]): Unit ={
    val file = new File("src/main/resources/output.csv")
    val bw = new BufferedWriter(new FileWriter(file))
    for(a <- authors)
      bw.write(a + ", ")
    bw.close()
  }
}
