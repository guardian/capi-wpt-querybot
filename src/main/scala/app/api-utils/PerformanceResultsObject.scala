package app.apiutils

import org.joda.time.DateTime

import scala.xml.Elem


/**
 * Created by mmcnamara on 10/02/16.
 */
class PerformanceResultsObject(url:String, testType: String, urlforTestResults: String, tTFB: Int, tFP:Int, tDC: Int, bDC: Int, tFL: Int, bFL: Int, sI: Int, status: String, warning: Boolean, alert: Boolean, failedNeedsRetest: Boolean) {
// todo - add publication date; and setters and getters for same ; also add to csv string
  val timeOfTest: String = DateTime.now().toString
  val testUrl: String = url
  val typeOfTest: String = testType
  lazy val typeOfTestName = if(typeOfTest.contains("Desktop")){
    "Desktop"
  } else {
    "Mobile"
  }
  val friendlyResultUrl: String = urlforTestResults
  val timeToFirstByte: Int = tTFB
  val timeFirstPaintInMs: Int = tFP
  val timeFirstPaintInSec: Double = roundAt(3)(timeFirstPaintInMs.toDouble/1000)
  val timeDocCompleteInMs: Int = tDC
  val timeDocCompleteInSec: Double = roundAt(3)(timeDocCompleteInMs.toDouble/1000)
  val bytesInDocComplete: Int = bDC
  val kBInDocComplete: Int = roundAt(0)(bytesInDocComplete.toDouble/1024).toInt
  val mBInDocComplete: Double = roundAt(3)(bytesInDocComplete.toDouble/1048576)
  val timeFullyLoadedInMs: Int = tFL
  val timeFullyLoadedInSec: Int = roundAt(0)(timeFullyLoadedInMs.toDouble/1000).toInt
  val bytesInFullyLoaded: Int = bFL
  val kBInFullyLoaded: Double = roundAt(3)(bytesInFullyLoaded.toDouble/1024)
  val mBInFullyLoaded: Double = roundAt(3)(bytesInFullyLoaded.toDouble/1048576)
  val estUSPrePaidCost: Double = roundAt(3)((bytesInFullyLoaded.toDouble/1048576)*0.10)
  val estUSPostPaidCost: Double = roundAt(3)((bytesInFullyLoaded.toDouble/1048576)*0.06)
  val speedIndex: Int = sI
  val aboveTheFoldCompleteInSec: Double = roundAt(3)(speedIndex.toDouble/1000)
  val resultStatus:String = status
  var alertDescription: String = ""
  var warningStatus: Boolean = warning
  var alertStatus: Boolean = alert
  val brokenTest: Boolean = failedNeedsRetest

  var headline: Option[String] = None
  var pageType: Option[String] = None

  var fullElementList: List[PageElementFromHTMLTableRow] = List()
  var editorialElementList: List[PageElementFromHTMLTableRow] = List()
  var editorialElementListMaxSize: Int = 5

  def setHeadline(text: Option[String]):Unit = {headline = text}
  def setPageType(text: String):Unit = {pageType = Option(text)}


  def getPageType:String = {
    if(pageType.nonEmpty){
      pageType.mkString
    }
    else {
      "Unknown"
    }
  }

  def addtoElementList(element: PageElementFromHTMLTableRow): Boolean = {
    if (editorialElementList.length < editorialElementListMaxSize){
      editorialElementList = editorialElementList :+ element
      true
    }
    else{false}
  }


  def returnFullElementListByWeight(): List[PageElementFromHTMLTableRow] = {fullElementList.sortWith(_.bytesDownloaded > _.bytesDownloaded)}

  def populateEditorialElementList(elementList: List[PageElementFromHTMLTableRow]): Boolean = {
    val trimmedList = trimToEditorialElements(elementList)
    if(trimmedList.head.bytesDownloaded < trimmedList.tail.head.bytesDownloaded){
      println("Error: Attempt to feed an unordered list of page elements to Performance Results Object")
      false
    } else {
      var workingList: List[PageElementFromHTMLTableRow] = for (element <- trimmedList if element.isMedia()) yield element
      var roomInTheList: Boolean = true
      while(workingList.nonEmpty && roomInTheList) {
        roomInTheList = addtoElementList(workingList.head)
        workingList = workingList.tail
      }
      true
    }
  }

  def trimToEditorialElements(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] = {
    val returnList: List[PageElementFromHTMLTableRow] = for (element <- elementList if element.contentType.contains("image") || element.contentType.contains("video") || element.contentType.contains("application")) yield element
    returnList
  }

  def toStringList(): List[String] = {
    List(testUrl.toString + ", " + timeFirstPaintInMs.toString + "ms", timeDocCompleteInSec.toString + "s", mBInDocComplete + "MB" , timeFullyLoadedInSec.toString + "s", mBInFullyLoaded + "MB", speedIndex.toString, resultStatus)
  }

  def toCSVString(): String = {
    timeOfTest + "," + testUrl.toString + "," + typeOfTest + "," + friendlyResultUrl + ","  + timeToFirstByte.toString + "," + timeFirstPaintInMs.toString + "," + timeDocCompleteInMs + "," + bytesInDocComplete + "," + timeFullyLoadedInMs + "," + bytesInFullyLoaded + "," + speedIndex + "," + resultStatus + "," + warningStatus + "," + alertStatus + "," + brokenTest + "," + editorialElementList.map(element => "," + element.resource + "," + element.contentType + "," + element.bytesDownloaded ).mkString + fillRemainingGapsAndNewline()
  }

  def toFullHTMLTableCells(): String = {
    "<td>" + "<a href=" + testUrl + ">" + headline.getOrElse(testUrl) + "</a>" + " </td>" + "<td>" + getPageType + "</td>" +  "<td>" + timeFirstPaintInMs.toString + "ms </td><td>" +  timeDocCompleteInSec.toString + "s </td><td>" + mBInDocComplete + "MB </td><td>" + timeFullyLoadedInSec.toString + "s </td><td>" + mBInFullyLoaded + "MB </td><td> $(US)" + estUSPrePaidCost + "</td><td> $(US)" + estUSPostPaidCost + "</td><td>" + speedIndex.toString + " </td><td> " + genTestResultString() + "</td>"
  }

  def toHTMLSimpleTableCells(): String = {
   "<td>"+DateTime.now+"</td>"+"<td>"+typeOfTestName+"</td>"+ "<td>" + "<a href=" + testUrl + ">" + headline.getOrElse(testUrl) + "</a>" + " </td>"+ "<td>" + getPageType + "</td>" + " <td>" + timeFirstPaintInMs.toString + "ms </td>" + "<td>" + aboveTheFoldCompleteInSec.toString + "s </td>" + "<td>" + mBInFullyLoaded + "MB </td>" + "<td> $(US)" + estUSPrePaidCost + "</td>" + "<td> $(US)" + estUSPostPaidCost + "</td>" + "<td> " + genTestResultString() + "</td>"
  }

  def toHTMLBasicTableCells(): String = {
    "<td>"+DateTime.now+"</td>"+"<td>"+typeOfTestName+"</td>"+ "<td>" + "<a href=" + testUrl + ">" + headline.getOrElse(testUrl) + "</a>" + " </td>" + "<td>" + getPageType + "</td>" + "<td>" + aboveTheFoldCompleteInSec.toString + "s </td>" + "<td>" + mBInFullyLoaded + "MB </td>"
  }

  def toHTMLInteractiveTableCells(): String = {
    "<td>"+DateTime.now+"</td>"+"<td>"+typeOfTestName+"</td>"+ "<td>" + "<a href=" + testUrl + ">" + headline.getOrElse(testUrl) + "</a>" + " </td>" + "<td>" + getPageType + "</td>" + " <td>" + timeFirstPaintInSec.toString + "s </td>" + "<td>" + aboveTheFoldCompleteInSec.toString + "s </td>" + "<td>" + mBInFullyLoaded + "MB </td>" + "</td>" + "<td> " + genTestResultString() + "</td>" + "<td>" + "<a href=" + friendlyResultUrl + ">" + "Click here to see full results." + "</a>" + "</td>"
  }

  def returnHTMLFullElementList(): String = {
    val firstFive:List[PageElementFromHTMLTableRow] = fullElementList.take(5)
    val pageElementString: String = (for (element <- firstFive) yield element.toHTMLRowString()).mkString
    val returnString = pageElementString + "<tr class=\"datarow\">" + "<td colspan=\"12\">" + "<a href=" + friendlyResultUrl + ">" + "See full test results here" + "</a>" + "</td>" + "</tr>"
    returnString
  }

  def returnHTMLEditorialElementList(): String = {
    (for (element <- editorialElementList) yield element.toHTMLRowString()).mkString
  }

  def returnHTMLFullPageElementRows(): String = {
    (for (element <- fullElementList) yield element.toHTMLRowString()).mkString
  }

  def returnElementTableRows(): String = {

           editorialElementList.map(element => element.emailHTMLString()).mkString
  }

  def toHTMLAlertMessageCells(): String = {
    //Email
    //tags with inline styles for email
    val pEmailTag: String = "<p style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;orphans: 3;widows: 3;margin: 0 0 10px;\">"
    val tableNormalRowEmailTag: String = "<tr style=\"background-color: ;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;page-break-inside: avoid;\" #d9edf7\";\">"
    val tableNormalCellEmailTag: String = "<td style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;padding: 0;background-color: #fff!important;\">"
    val tableMergedRowEmailTag: String = "<td style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;padding: 0;background-color: #fff!important;\" colspan=\"3\">"
    val aHrefEmailStyle: String = "style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;background-color: transparent;color: #337ab7;text-decoration: underline;\""

    tableNormalRowEmailTag + tableNormalCellEmailTag + "<a href=" + testUrl + aHrefEmailStyle + ">" + headline.getOrElse(testUrl) + "</a>" + "</td>" + tableNormalCellEmailTag + typeOfTest + "</td>" + tableNormalCellEmailTag + genTestResultString() +"</td>" + tableNormalCellEmailTag + "<a href=" + friendlyResultUrl + aHrefEmailStyle + ">" + "Click here to see full results." + "</a>" + "</td>" + "</tr>\n" +
    tableMergedRowEmailTag +"List of 5 heaviest elements on page - Recommend reviewing these items </tr>\n" +
    tableNormalRowEmailTag + tableNormalCellEmailTag + "Resource" + "</td>" + tableNormalCellEmailTag + "Content Type" + "</td>" + tableNormalCellEmailTag + "Bytes Transferred" + "</td>" + "</tr>\n" +
      editorialElementList.map(element => element.emailHTMLString()).mkString
  }

  def toInteractiveAlertMessageCells(): String = {
    //Email
    //tags with inline styles for email
    val pEmailTag: String = "<p style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;orphans: 3;widows: 3;margin: 0 0 10px;\">"
    val tableNormalRowEmailTag: String = "<tr style=\"background-color: ;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;page-break-inside: avoid;\" #d9edf7\";\">"
    val tableNormalCellEmailTag: String = "<td style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;padding: 0;background-color: #fff!important;\">"
    val tableMergedRowEmailTag: String = "<td style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;padding: 0;background-color: #fff!important;\" colspan=\"3\">"
    val aHrefEmailStyle: String = "style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;background-color: transparent;color: #337ab7;text-decoration: underline;\""

    val firstFive:List[PageElementFromHTMLTableRow] = fullElementList.take(5)

    tableNormalRowEmailTag + tableNormalCellEmailTag + "<a href=" + testUrl + aHrefEmailStyle + ">" + headline.getOrElse(testUrl) + "</a>" + "</td>" + tableNormalCellEmailTag + typeOfTest + "</td>" + tableNormalCellEmailTag + genTestResultString() +"</td>" + tableNormalCellEmailTag + "<a href=" + friendlyResultUrl + aHrefEmailStyle + ">" + "Click here to see full results." + "</a>" + "</td>" + "</tr>\n" +
      tableMergedRowEmailTag +"List of 5 heaviest elements on page - Recommend reviewing these items </tr>\n" +
      tableNormalRowEmailTag + tableNormalCellEmailTag + "Resource" + "</td>" + tableNormalCellEmailTag + "Content Type" + "</td>" + tableNormalCellEmailTag + "Bytes Transferred" + "</td>" + "</tr>\n" +
      firstFive.map(element => element.emailHTMLString()).mkString
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
    var accumulator: Int = editorialElementList.length
    var returnString: String = ""
    while (accumulator < editorialElementListMaxSize-1){
      returnString = returnString + ","
      accumulator += 1
    }
    returnString = returnString + "\n"
    returnString
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

}
