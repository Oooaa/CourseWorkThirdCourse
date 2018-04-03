package packageName

import java.util.{Calendar, Date}

import scala.collection.immutable

object DatesHandling {

  def addDaysToDate(date: Date, daysCount: Int) = {
    addToDate(date, daysCount, Calendar.DATE)
  }

  def addToDate(date: Date, howMuch: Int, whatUnit: Int) = {
    import java.util.Calendar
    val c = Calendar.getInstance()
    c.setTime(date)
    c.add(whatUnit, howMuch)
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

  def generateRanges(howMuchRange: Int, oneRangeStep: Int, oneRangeUnit: Int): Seq[(Date, Date)] = {
    val today = new Date()
    (1 to howMuchRange)
      .map(_ * oneRangeStep)
      .map(x => addToDate(today, -x, oneRangeUnit))
      .foldLeft(List.empty[(Date, Date)], today)({ case ((lst, lastDate), newDate) => (lst :+ (lastDate, newDate), newDate) })
      ._1
  }

  def getDaysBetweenDate(leftDate: Date, rightDate: Date): Long = {
    val timeFromBeginning = Math.abs(leftDate.getTime - rightDate.getTime)
    timeFromBeginning / (24 * 60 * 60 * 1000)
  }

  def getLastDaysRange(n: Int): Seq[Date] = {
    val today = new Date()
    val downloadFromDate = addDaysToDate(today, n)
    getDatesRange(downloadFromDate, today)
  }

}
