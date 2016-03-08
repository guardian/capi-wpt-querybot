package app.apiutils

import org.joda.time.DateTime

import scala.xml.Elem


/**
 * Created by mmcnamara on 10/02/16.
 */
class PerformanceResultsObject(url:String, testType: String, tTFB: Int, tFP:Int, tDC: Int, bDC: Int, tFL: Int, bFL: Int, sI: Int, status: String, warning: Boolean, alert: Boolean, failedNeedsRetest: Boolean) {
  val timeOfTest: String = DateTime.now().toString
  val testUrl: String = url
  val typeOfTest: String = testType
  val timeToFirstByte: Int = tTFB
  val timeFirstPaintInMs: Int = tFP
  val timeFirstPaintInSec: Double = roundAt(3)(timeFirstPaintInMs/1000)
  val timeDocCompleteInMs: Int = tDC
  val timeDocCompleteInSec: Double = roundAt(3)(timeDocCompleteInMs/1000)
  val bytesInDocComplete: Int = bDC
  val kBInDocComplete: Int = bytesInDocComplete/1024
  val mBInDocComplete: Double = roundAt(3)(bytesInDocComplete/1048576)
  val timeFullyLoadedInMs: Int = tFL
  val timeFullyLoadedInSec: Int = timeFullyLoadedInMs/1000
  val bytesInFullyLoaded: Int = bFL
  val kBInFullyLoaded: Int = bytesInFullyLoaded/1024
  val mBInFullyLoaded: Double = roundAt(3)(bytesInFullyLoaded/1048576)
  val estUSPrePaidCost: Double = roundAt(3)((bytesInFullyLoaded.toDouble/1048576)*0.10)
  val estUSPostPaidCost: Double = roundAt(3)((bytesInFullyLoaded.toDouble/1048576)*0.06)
  val speedIndex: Int = sI
  val aboveTheFoldCompleteInSec: Double = roundAt(3)(speedIndex/1000)
  val resultStatus:String = status
  var alertDescription: String = ""
  var warningStatus: Boolean = warning
  var alertStatus: Boolean = alert
  val brokenTest: Boolean = failedNeedsRetest

  var heavyElementList: List[PageElementFromHTMLTableRow] = List()
  var elementListMaxSize: Int = 5

  def addtoElementList(element: PageElementFromHTMLTableRow): Boolean = {
    if (heavyElementList.length < elementListMaxSize){
      heavyElementList = heavyElementList :+ element
      true
    }
    else{false}
  }

  def takeElementsFromSortedList(elementList: List[PageElementFromHTMLTableRow]): Boolean = {
    if(elementList.head.bytesDownloaded < elementList.tail.head.bytesDownloaded){
      println("Error: Attempt to feed an unordered list of page elements to Performance Results Object")
      false
    } else {
      var workingList: List[PageElementFromHTMLTableRow] = elementList
      var roomInTheList: Boolean = true
      while(workingList.nonEmpty && roomInTheList) {
        roomInTheList = addtoElementList(workingList.head)
        workingList = workingList.tail
      }
      true
    }
  }

  def toStringList(): List[String] = {
    List(testUrl.toString + ", " + timeFirstPaintInMs.toString + "ms", timeDocCompleteInSec.toString + "s", mBInDocComplete + "MB" , timeFullyLoadedInSec.toString + "s", mBInFullyLoaded + "MB", speedIndex.toString, resultStatus)
  }

  def toCSVString(): String = {
    testUrl.toString + "," + timeOfTest + "," + resultStatus + "," +  timeFirstPaintInMs.toString + "," + timeDocCompleteInMs + "," + bytesInDocComplete + "," + timeFullyLoadedInMs + "," + bytesInFullyLoaded + "," + speedIndex + "," + heavyElementList.map(element => "," + element.resource + "," + element.contentType + "," + element.bytesDownloaded ).mkString + fillRemainingGapsAndNewline()
  }

  def toHTMLTableCells(): String = {
    "<th>" + "<a href=" + testUrl + ">" + testUrl + "</a>" + " </th>" + "<td>" + timeFirstPaintInMs.toString + "ms </td><td>" +  timeDocCompleteInSec.toString + "s </td><td>" + mBInDocComplete + "MB </td><td>" + timeFullyLoadedInSec.toString + "s </td><td>" + mBInFullyLoaded + "MB </td><td> $(US)" + estUSPrePaidCost + "</td><td> $(US)" + estUSPrePaidCost + "</td><td>" + speedIndex.toString + " </td><td> " + genTestResultString() + "</td>"
  }

  def toHTMLSimpleTableCells(): String = {
   "<td>"+DateTime.now+"</td>"+"<td>"+typeOfTest+"</td>"+ "<th>" + "<a href=" + testUrl + ">" + testUrl + "</a>" + " </th>" +" <td>" + timeFirstPaintInMs.toString + "ms </td>" + "<td>" + aboveTheFoldCompleteInSec.toString + "s </td>" + "<td>" + mBInFullyLoaded + "MB </td>" + "<td> $(US)" + estUSPrePaidCost + "</td>" + "<td> $(US)" + estUSPrePaidCost + "</td>" + "<td> " + genTestResultString() + "</td>"
  }

  def toHTMLAlertMessageCells(): String = {
    "<a href=" + testUrl + ">" + testUrl + "</a>" + "</td>" + "<td>" + typeOfTest + "</td>" + "<td>"+ genTestResultString() +"</td>" +
    "<tr>List of 5 heaviest elements on page - Recommend reviewing these items </tr>" +
    "<tr><td>Resource</td><td>Content Type</td><td>Bytes Transferred</td></tr>" +
      heavyElementList.map(element => element.alertHTMLString()).mkString
  }

  override def toString(): String = {
    testUrl + ", " + timeFirstPaintInMs.toString + "ms, " + timeDocCompleteInSec.toString + "s, " + mBInDocComplete + "MB, " + timeFullyLoadedInSec.toString + "s, " + mBInFullyLoaded + "MB, " + speedIndex.toString + ", " + resultStatus
  }

  def genTestResultString(): String = {
    if(this.alertStatus)
    this.alertDescription
    else
      this.resultStatus
  }

  def fillRemainingGapsAndNewline(): String ={
    var accumulator: Int = heavyElementList.length
    var returnString: String = ""
    while (accumulator < elementListMaxSize-1){
      returnString = returnString + ","
      accumulator += 1
    }
    returnString = returnString + "\n"
    returnString
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

}
