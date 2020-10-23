import java.text.SimpleDateFormat
import java.util.Date

object Date {
  def _max(s1: String, s2: String): String =  {
    if(Date.compare(s1,s2)){
      val _max = s1.split("-")(0).toInt max s2.split("-")(0).toInt
      return _max.toString
    }
    "Hello"
  }
  def compare(s: String, s1: String): Boolean ={
    val date1 = s.split("-")
    val date2 = s1.split("-")

    val new_year = if (date1(0).toInt > date2(0).toInt) date1 else date2
    val old_year = if (new_year == date1) date2 else date1

    val year = new_year(0).toInt
    val month1 = new_year(1).toInt
    val day1 = new_year(2).toInt

    val year2 = old_year(0).toInt
    val month2 = old_year(1).toInt
    val day2 = old_year(2).toInt

    // Within a year
    if(year - year2 <= 1 && year - year2 >= 0) {
      if (year != year2 && month2 < month1)
        return false
      if (year != year2 && month1 == month2 && day2 < day1) {
        return false
      }
      return true
    }
    false
  }
}
