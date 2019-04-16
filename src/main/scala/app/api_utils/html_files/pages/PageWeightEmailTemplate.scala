package app.api

import app.api_utils.model.PerformanceResultsObject
import org.joda.time.DateTime

/**
 * Created by mmcnamara on 15/04/16.
 */
class PageWeightEmailTemplate (resultsList: List[PerformanceResultsObject], mobileUrl: String, desktopUrl: String) {

  //HTML Page elements
  //Page Header
  val mobileDashboardUrl = mobileUrl
  val desktopDashboardUrl = desktopUrl


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
    "<h1>Alert: The following pages may be too heavy</h1>" + "\n" +
    "<p>Job started at: " + DateTime.now + "</p>" + "\n" +
    "</div>"

  //Page Content
  val HTML_PAGE_CONTENT: String = "<div id=\"content\">" + "\n"

  //Page Tables
  val HTML_REPORT_TABLE_HEADERS: String = "<table id=\"report\">"+ "\n" +
    "<tbody>" + "\n" +
    "<tr><th>You have been alerted because the following pages have been found to be too heavy and require review.</th>" + "</tr>"+ "\n"


  val HTML_PAGE_ELEMENT_TABLE_HEADERS: String = "<tr>" + "\n" +
    "<td colspan=\"12\">" + "Main cause seems to be these elements, which weigh in at: </td>" + "\n"

  val HTML_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n"

  val HTML_PAGE_ELEMENT_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n" + "</td>" + "\n" + "</tr>" + "\n"

  //Page Footer
  val HTML_FOOTER: String = "</div>" + "\n" +
    "<div id=\"footer\">"  + "</div>" + "\n" +
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
       " is weighing in at " + result.mBInFullyLoaded + " MB, for " + result.typeOfTestName + " pageviews." +  "</td>" + "</tr>" + "\n" +
      "<tr class=\"pageclass default\">" + "<td> <a href = \"" + getDashboardUrl(result) + "#" + result.anchorId.getOrElse("") + "\"> Click here for more information on how to resolve this.</a>" + "</td>" + "</tr>" + "\n" +
        "<tr class=\"pageclass default\">" + "<td>  </td>" + "</tr>" + "\n"
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

  def getDashboardUrl(result: PerformanceResultsObject): String = {
    val url: String = result.typeOfTestName match {
    case "Desktop" => desktopDashboardUrl
    case "Mobile" => mobileDashboardUrl
    case _ => desktopDashboardUrl
    }
    url
  }
  

  // Access Methods

  override def toString(): String = {
    if(resultsList.isEmpty){
      println("Interactive Email Template called and passed an Empty list.")
      return "I'm very sorry. This email was sent in error. Please ignore."
    } else {
      println("Page Weight email list: \n" + resultsList)
      HTML_PAGE
    }
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

