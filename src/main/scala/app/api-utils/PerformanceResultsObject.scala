package app.apiutils

import org.joda.time.DateTime

import scala.xml.Elem


/**
 * Created by mmcnamara on 10/02/16.
 */
class PerformanceResultsObject(url:String, testType: String, tTFB: Int, tFP:Int, tDC: Int, bDC: Int, tFL: Int, bFL: Int, sI: Int, status: String, warning: Boolean, alert: Boolean) {
  val testUrl: String = url
  val typeOfTest: String = testType
  val timeToFirstByte: Int = tTFB
  val timeFirstPaintInMs: Int = tFP
  val timeFirstPaintInSec: Double = roundAt(2)(timeFirstPaintInMs/1000)
  val timeDocCompleteInMs: Int = tDC
  val timeDocCompleteInSec: Int = timeDocCompleteInMs/1000
  val bytesInDocComplete: Int = bDC
  val kBInDocComplete: Int = bytesInDocComplete/1024
  val mBInDocComplete: Double = roundAt(2)(bytesInDocComplete/1048576)
  val timeFullyLoadedInMs: Int = tFL
  val timeFullyLoadedInSec: Int = timeFullyLoadedInMs/1000
  val bytesInFullyLoaded: Int = bFL
  val kBInFullyLoaded: Int = bytesInFullyLoaded/1024
  val mBInFullyLoaded: Double = roundAt(2)(bytesInFullyLoaded/1048576)
  val estUSPrePaidCost: Double = roundAt(2)((bytesInFullyLoaded.toDouble/1048576)*0.10)
  val estUSPostPaidCost: Double = roundAt(2)((bytesInFullyLoaded.toDouble/1048576)*0.06)
  val speedIndex: Int = sI
  val aboveTheFoldCompleteInSec: Double = roundAt(2)(speedIndex/1000) 
  val resultStatus:String = status
  var alertDescription: String = ""
  var warningStatus: Boolean = warning
  var alertStatus: Boolean = alert

  def toStringList(): List[String] = {
    List(testUrl.toString + ", " + timeFirstPaintInMs.toString + "ms", timeDocCompleteInSec.toString + "s", mBInDocComplete + "MB" , timeFullyLoadedInSec.toString + "s", mBInFullyLoaded + "MB", speedIndex.toString, resultStatus)
  }

  def toHTMLTableCells(): String = {
    "<th>" + "<a href=" + testUrl + ">" + testUrl + "</a>" + " </th>" + "<td>" + timeFirstPaintInMs.toString + "ms </td><td>" +  timeDocCompleteInSec.toString + "s </td><td>" + mBInDocComplete + "MB </td><td>" + timeFullyLoadedInSec.toString + "s </td><td>" + mBInFullyLoaded + "MB </td><td> $(US)" + estUSPrePaidCost + "</td><td> $(US)" + estUSPrePaidCost + "</td><td>" + speedIndex.toString + " </td><td> " + genTestResultString() + "</td>"
  }

  def toHTMLSimpleTableCells(): String = {
   "<td>"+DateTime.now+"</td>"+"<td>"+typeOfTest+"</td>"+ "<th>" + "<a href=" + testUrl + ">" + testUrl + "</a>" + " </th>" +" <td>" + timeFirstPaintInSec.toString + "s </td>" + "<td>" + aboveTheFoldCompleteInSec.toString + "s </td>" + "<td>" + mBInFullyLoaded + "MB </td>" + "<td> $(US)" + estUSPrePaidCost + "</td>" + "<td> $(US)" + estUSPrePaidCost + "</td>" + "<td> " + genTestResultString() + "</td>"
  }

  def toHTMLAlertMessageCells(): String = {"<td>" + DateTime.now() + "</td>" + "<td>" + typeOfTest + "</td>" + "<td>" + "<a href=" + testUrl + ">" + testUrl + "</a>" + "</td>" + "<td>"+ genTestResultString() +"</td>"}

  override def toString(): String = {
    testUrl + ", " + timeFirstPaintInMs.toString + "ms, " + timeDocCompleteInSec.toString + "s, " + mBInDocComplete + "MB, " + timeFullyLoadedInSec.toString + "s, " + mBInFullyLoaded + "MB, " + speedIndex.toString + ", " + resultStatus
  }

  def genTestResultString(): String = {
    if(this.alertStatus)
    this.alertDescription
    else
      this.resultStatus
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

}
