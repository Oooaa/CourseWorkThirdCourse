package packageName

import java.util.Date

import scala.collection.immutable

object DatesHandling {

  def addDaysToDate(date: Date, daysCount: Int) = {
    import java.util.Calendar
    val c = Calendar.getInstance
    c.setTime(date)
    c.add(Calendar.DATE, daysCount)
    c.getTime
  }

  def generateDatesList(acc: List[Date], from: Date, to: Date): List[Date] =
    if (from.after(to))
      acc
    else
      generateDatesList(acc :+ from, addDaysToDate(from, 1), to)


  def getDatesRange(left: Date, right: Date): immutable.Seq[Date] = {
    val isRightAfter = right.after(left)
    val min = if (isRightAfter) left else right
    val max = if (isRightAfter) right else left

    generateDatesList(List.empty, min, max)
  }

  def getDaysBetweenDate(leftDate: Date, rightDate: Date): Long = {
    val timeFromBeginning = Math.abs(leftDate.getTime - rightDate.getTime)
    timeFromBeginning / (24 * 60 * 60 * 1000)
  }

}
