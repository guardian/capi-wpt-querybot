package app.api

import app.apiutils.PerformanceResultsObject
import org.joda.time.DateTime

/**
 * Created by mmcnamara on 15/04/16.
 */
class PageWeightEmailTemplate (resultsList: List[PerformanceResultsObject]) {

  //HTML Page elements
  //Page Header
  val dashboardUrl = "https://s3-eu-west-1.amazonaws.com/capi-wpt-querybot/editorialpageweightdashboard.html"

  val HTML_PAGE_HEAD: String = "<!DOCTYPE html><html lang=\"en\">" + "\n" +
    "<head> <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>" + "\n" +
    "<title>Daily REPORT - [Pageweight Dashboard - Combined]</title>" + "\n" +
    "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\"/>" + "\n" +
    "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js\"></script>" + "\n" +
    "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js\"></script>" + "\n" +
    "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\" rel=\"stylesheet\"/>" + "\n" +
    "<link rel=\"stylesheet\" href=\"/capi-wpt-querybot/assets/css/style.css\"/>"+ "\n" +
    "<script src=\"/capi-wpt-querybot/assets/js/script.js\"></script>" + "\n" +
    "</head>"

  //Page Container
  val HTML_PAGE_CONTAINER: String = "<body>" + "\n" +
    "<div id=\"container\">" + "\n" +
    "<div id=\"head\">" + "\n" +
    "<h1>Alert: Pageweight problems detected</h1>" + "\n" +
    "<p>Job started at: " + DateTime.now + "</p>" + "\n" +
    "</div>"

  //Page Content
  val HTML_PAGE_CONTENT: String = "<div id=\"content\">" + "\n"

  //Page Tables
  val HTML_REPORT_TABLE_HEADERS: String = "<table id=\"report\">"+ "\n" +
    "<thead>" + "\n" +
    "<tr> <th>You have been alerted because the following pages have been found to be too heavy and require investigation</th>" + "</tr>"+ "\n" +
    "</thead>" +"\n" +
    "<tbody>"

  val HTML_PAGE_ELEMENT_TABLE_HEADERS: String = "<tr>" + "\n" +
    "<td colspan=\"12\">" + "Main cause seems to be these elements, which weigh in at: </td>" + "\n"

  val HTML_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n"

  val HTML_PAGE_ELEMENT_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n" + "</td>" + "\n" + "</tr>" + "\n"

  //Page Footer
  val HTML_FOOTER: String = "</div>" + "\n" +
    "<div id=\"footer\">" + "<p>" + "<a href=\"" + dashboardUrl + "\">" + "click here for more information"+ "</a>" + "</p>" + "</div>" + "\n" +
    "</div>" + "\n" +
    "</body>" + "\n" +
    "</html>"


  //HTML_PAGE
  val HTML_PAGE: String = HTML_PAGE_HEAD + HTML_PAGE_CONTAINER + HTML_PAGE_CONTENT + generateHTMLTable(resultsList) + HTML_FOOTER


  //page generation methods
  def generateHTMLTable(resultsList: List[PerformanceResultsObject]): String = {
    HTML_REPORT_TABLE_HEADERS + "\n" + generateHTMLDataRows(resultsList) + "\n" + HTML_TABLE_END
  }

  def generateHTMLDataRows(resultsList: List[PerformanceResultsObject]): String = {
    (for (result <- resultsList) yield {
      "<tr class=\"pageclass default\">" + "<td> The article: " + "<a href=\"" + result.testUrl + "\">" + result.headline.getOrElse(result.testUrl) + "</a>" +
       "is weighing in at " + result.mBInFullyLoaded + " MB." + "</td>" + "</tr>" + "\n" +
        generatePageElementTable(result)
    }).mkString

  }

  def generatePageElementTable(resultsObject: PerformanceResultsObject): String = {
    HTML_PAGE_ELEMENT_TABLE_HEADERS + "\n"  + getHTMLForPageElements(resultsObject) + HTML_PAGE_ELEMENT_TABLE_END
  }

  def getHTMLForPageElements(resultsObject: PerformanceResultsObject): String = {
      resultsObject.returnEmailEditorialElementList()
  }

  def getAlertClass(resultsObject: PerformanceResultsObject): String = {
    if (resultsObject.alertStatusPageWeight) {
      "alert"
    } else {
      "default"
    }
  }

  // Access Methods

  override def toString(): String = {
    HTML_PAGE
  }


  //  def initialisePageForArticle: String = {
  //    hTMLPageHeader + hTMLTitleArticle + hTMLJobStarted
  //  }
  //
  //  def initialisePageForLiveblog: String = {
  //    hTMLPageHeader + hTMLTitleLiveblog + hTMLJobStarted
  //  }
  //
  //  def initialisePageForInteractive: String = {
  //    hTMLPageHeader + hTMLTitleInteractive + hTMLJobStarted
  //  }
  //
  //  def initialisePageForFronts: String = {
  //    hTMLPageHeader + hTMLTitleFronts + hTMLJobStarted
  //  }
  //
  //  def initialiseTable: String = {
  //    hTMLSimpleTableHeaders
  //  }
  //
  //  def interactiveTable: String = {
  //    hTMLInteractiveTableHeaders
  //  }


}
