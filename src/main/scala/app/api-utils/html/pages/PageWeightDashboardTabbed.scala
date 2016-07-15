package app.api


import app.apiutils.PerformanceResultsObject
import org.joda.time.DateTime

/**
 * Created by mmcnamara on 15/04/16.
 */
class PageWeightDashboardTabbed(combinedResultsList: List[PerformanceResultsObject], desktopResultsList: List[PerformanceResultsObject], mobileResultsList: List[PerformanceResultsObject]) {

    val numberOfCombinedRecords = combinedResultsList.length
    val numberOfDesktopRecords = desktopResultsList.length
    val numberOfMobileRecords = mobileResultsList.length
    //HTML Page elements
    //Page Header
    val HTML_PAGE_HEAD: String = "<!DOCTYPE html><html lang=\"en\">" + "\n" +
      "<head> <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>" + "\n" +
      "<title>Editorial PageWeight Dashboard - [Editorial Pageweight Dashboard]</title>" + "\n" +
      "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\"/>" + "\n" +
      "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js\"></script>" + "\n" +
      "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js\"></script>" + "\n" +
      "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\" rel=\"stylesheet\"/>" + "\n" +
      "<link rel=\"stylesheet\" href=\"/capi-wpt-querybot/assets/css/tabs.css\"/>"+ "\n" +
      "<link rel=\"stylesheet\" href=\"/capi-wpt-querybot/assets/css/style.css\"/>"+ "\n" +
      "<script src=\"/capi-wpt-querybot/assets/js/script.js\"></script>" + "\n" +
      "<script src=\"/capi-wpt-querybot/assets/js/tabs.js\"></script>" + "\n" +
      "</head>"

  //Page Container
  val HTML_PAGE_CONTAINER: String = "<body>" + "\n" +
    "<div id=\"container\">" + "\n" +
    "<div id=\"head\">" + "\n" +
    "<h1>Current performance of today's Pages</h1>" + "\n" +
    "<p>Job started at: " + DateTime.now + "</p>" + "\n" +
    "</div>" + "\n"

  val HTML_PAGE_TABS_LIST: String = "<div class=\"tabs\">" + "\n" +
    "<ul class=\"tab-links\">" + "\n" +
    "<li class=\"lastRun\">" + "<a href=\"#mobile\">Summary of last run</a>" + "</li>" + "\n" +
    "<li>" + "<a href=\"#today\">Summary of the day so far view</a>" + "</li>" + "\n" +
    "<li>" + "<a href=\"#longTerm\">Long term summary</a>" + "</li>" + "\n" +
    "<li>" + "<a href=\"#editorialEmbed\">Review of editorial embeds</a>" + "</li>" + "\n" +
    "</ul>" + "\n"
  //close div added to HTML_FOOTER

  //Page Content
  val HTML_TAB_CONTENT: String = "<div id=\"tab-content\">" + "\n"

  val HTML_TAB_HEADER: String = "<div class=\"tab-content\">" + "\n"
  

  val HTML_LASTRUN_TAB_CONTENT_HEADER: String = "<div id=\"lastRun\" class=\"tab active\">" + "\n" +
    "<p>" + "<h2>Summary of most recent successful run" + "</h2>" + "</p>" + "\n"

  val HTML_TODAY_TAB_CONTENT_HEADER: String = "<div id=\"today\" class=\"tab\">" + "\n" +
    "<p>" + "<h2>Summary of todays runs" + "</h2>" + "</p>" + "\n"

  val HTML_LONGTERM_TAB_CONTENT_HEADER: String = "<div id=\"longTerm\" class=\"tab\">" + "\n" +
    "<p>" + "<h2>Longterm summary" + "</h2>" + "</p>" + "\n"

  val HTML_EDITORIALEMBEDS_TAB_CONTENT_HEADER: String = "<div id=\"editorialEmbed\" class=\"tab\">" + "\n" +
    "<p>" + "<h2>Long term review of editorial embeds found in tested pages" + "</h2>" + "</p>" + "\n"

  val HTML_TAB_CONTENT_FOOTER: String = "</div>" + "\n"

  //Page Tables
  /*val HTML_REPORT_TABLE_HEADERS: String = "<table id=\"report\">"+ "\n" +
    "<thead>" + "\n" +
    "<tr> <th>Time Page Last Updated</th>" + "<th>Test VisualsElementType</th>" + "<th>Headline</th>" + "<th>VisualsElementType of Page</th>" + "<th>Time till page looks loaded</th>" + "<th>Page weight (MB)</th>" + "<th>Click for more details</th>" +  "</tr>"+ "\n" +
    "</thead>" +"\n" +
    "<tbody>"
*/

  /*val HTML_PAGE_ELEMENT_TABLE_HEADERSPT1: String = "<tr>" + "\n" +
    "<td colspan=\"12\">" + "<table id=\"data\" class=\"data\">" + "\n"

  val HTML_PAGE_ELEMENT_TABLE_HEADERSPT2: String =
    "<caption>List of 5 heaviest elements on page - Recommend reviewing these items </caption>" + "\n" +
    "<thead>" + "\n" +
    "<tr>" + "<th>Resource</th>" + "<th>Content VisualsElementType</th>" + "<th>Bytes Transferred</th>" + "</tr>" + "\n" +
    "</thead>" +"\n" +
    "<tbody>"

  val HTML_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n"

  val HTML_PAGE_ELEMENT_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n" + "</td>" + "\n" + "</tr>" + "\n"
  */
  //Page Footer
  val HTML_FOOTER: String = "</div>" + "\n" +
    "<div id=\"footer\">" + "<p>Job completed at: [DATA]</p>" + "</div>" + "\n" +
    "</div>" + "\n" +
    "</div>" + "\n" +
    "</body>" + "\n" +
    "</html>"


  //HTML_PAGE
  val HTML_PAGE: String = HTML_PAGE_HEAD + HTML_PAGE_CONTAINER + HTML_PAGE_TABS_LIST +
    HTML_TAB_CONTENT + HTML_LASTRUN_TAB_CONTENT_HEADER + generateLastRunData() + HTML_TAB_CONTENT_FOOTER +
    HTML_TODAY_TAB_CONTENT_HEADER + generateDailyRunData() + HTML_TAB_CONTENT_FOOTER +
    HTML_LONGTERM_TAB_CONTENT_HEADER + generateLongTermRunData() + HTML_TAB_CONTENT_FOOTER +
    HTML_EDITORIALEMBEDS_TAB_CONTENT_HEADER + generateEditorialEmbedSummary + HTML_TAB_CONTENT_FOOTER +
    HTML_FOOTER


    //page generation methods
/*    def generateHTMLTable(resultsList: List[PerformanceResultsObject]): String = {
      HTML_REPORT_TABLE_HEADERS + "\n" + generateHTMLDataRows(resultsList) + "\n" + HTML_TABLE_END
    }

    def generateHTMLDataRows(resultsList: List[PerformanceResultsObject]): String = {
      (for (result <- resultsList) yield {

        if(result.alertStatusPageWeight){
          "<tr class=\"pageclass " + getAlertClass(result) + "\">" + result.toHTMLPageWeightTableCells() + "<td><div class=\"arrow\"><a id=" + result.anchorId.getOrElse("") + "></a></div></td></tr>" + "\n" +
          generatePageElementTable(result)
        } else {
          "<tr class=\"pageclass " + getAlertClass(result) + "\">" + result.toHTMLPageWeightTableCells() + "<td><div>" + "<a id=" + result.anchorId.getOrElse("") + "></a>" + "</div></td></tr>" + "\n"
          }
      }).mkString

    }

    def generatePageElementTable(resultsObject: PerformanceResultsObject): String = {
        HTML_PAGE_ELEMENT_TABLE_HEADERSPT1 + "\n"  +
          "<caption class=\"" + getAlertClass(resultsObject) + "\">" + "Alert was triggered because: " + resultsObject.genTestResultString() + "</caption>" +
        HTML_PAGE_ELEMENT_TABLE_HEADERSPT2 + getHTMLForPageElements(resultsObject) + HTML_PAGE_ELEMENT_TABLE_END
    }

    def getHTMLForPageElements(resultsObject: PerformanceResultsObject): String = {
      if (resultsObject.getPageType.contains("Interactive") || resultsObject.getPageType.contains("interactive")){
        resultsObject.returnHTMLFullElementList()
      } else {
        resultsObject.returnHTMLEditorialElementList()
      }
    }

  def getAlertClass(resultsObject: PerformanceResultsObject): String = {
    if (resultsObject.alertStatusPageWeight) {
      "alert"
    } else {
      "default"
    }
  }*/

  def generateLastRunData(): String = {
    "<div><h2>Data from Last Successful Run</h2> </div>\n" +
    "<div>" +
      "<p>Job started at: " + jobStartTime + "</p>\n" +
      "<p> Job finished at: " + jobFinishTime + "</p>\n" +
      "<p> Time taken for job to complete: " + runDuration + "</p>\n" +
      "<p> Number of pages pulled from CAPI: " + cAPIPageCount + "</p>\n" +
      "<p> Number of pages from previous runs retested" + pageRetestedCount + "</p>\n" +
      "<p> Number of pages sent to wpt instance for testing: " + wptPageCount + "</p>\n" +
      "<p> Number of failed Tests: " + failedTests + "</p>\n" +
      "<p> Number of pageWeightAlerts raised: " + pageWeightRunTotal + "</p>\n" +
      "<p> Number of pageSpeedAlerts raised: " + pageSpeedRunTotal + "</p>\n" +
      "<p> Number of pageSpeedAlerts raised with no corresponding pageWeight alert: " + pageWeightNoPageSpeedRunTotal + "</p>\n" +
      "<p> Number of pageWeightAlerts raised on Non-Interactive content: " + pageWeightNonInteractive + "</p>\n" +
      "<p> Number of pageSpeedAlerts raised on Non-Interactive content: " + pageSpeedNonInteractive + "</p>\n" +
      "<p> Number of pageSpeedAlerts raised with no corresponding pageWeight alert on Non-Interactive content: " + pageWeightNoPageSpeedNonInteractive + "</p>\n" +
      "<p> Number of pageWeightAlerts raised on Interactive content: " + pageWeightInteractive + "</p>\n" +
      "<p> Number of pageSpeedAlerts raised on Interactive content: " + pageSpeedInteractive + "</p>\n" +
      "<p> Number of pageSpeedAlerts raised with no corresponding pageWeight alert on Interactive content: " + pageWeightNoPageSpeedInteractive + "</p>\n" +
      "</div>\n"
  }

  def generateDailyRunData(): String = {
    "<div><h2>Data from Todays Runs</h2> </div>\n" +
      "<div>" +
      "<p> Average Time taken for job to complete: " + averageRunDurationToday + "</p>\n" +
      "<p> Total number of pages pulled from CAPI: " + cAPIPageCountToday + "</p>\n" +
      "<p> Total number of pages from previous runs retested" + pageRetestedCountToday + "</p>\n" +
      "<p> Total number of pages sent to wpt instance for testing: " + wptPageCountToday + "</p>\n" +
      "<p> Total number of failed Tests: " + failedTestsToday + "</p>\n" +
      "<p> Total number of pageWeightAlerts raised: " + pageWeightToday + "</p>\n" +
      "<p> Total number of pageSpeedAlerts raised: " + pageSpeedToday + "</p>\n" +
      "<p> Total number of pageSpeedAlerts raised with no corresponding pageWeight alert: " + pageWeightNoPageSpeedToday + "</p>\n" +
      "<p> Total number of pageWeightAlerts raised on Non-Interactive content: " + pageWeightNonInteractiveToday + "</p>\n" +
      "<p> Total number of pageSpeedAlerts raised on Non-Interactive content: " + pageSpeedNonInteractiveToday + "</p>\n" +
      "<p> Total number of pageSpeedAlerts raised with no corresponding pageWeight alert on Non-Interactive content: " + pageWeightNoPageSpeedNonInteractiveToday + "</p>\n" +
      "<p> Total number of pageWeightAlerts raised on Interactive content: " + pageWeightInteractiveToday + "</p>\n" +
      "<p> Total number of pageSpeedAlerts raised on Interactive content: " + pageSpeedInteractiveToday + "</p>\n" +
      "<p> Total number of pageSpeedAlerts raised with no corresponding pageWeight alert on Interactive content: " + pageWeightNoPageSpeedInteractiveToday + "</p>\n" +
      "<p> Daily probability of non-interactivePage having a pageWeightAlert: " + nonInteractivePageWeightProbabilityToday + "</p>\n" +
      "<p> Daily probability of non-interactivePage having a pageSpeedAlert: " + nonInteractivePageSpeedProbabilityToday + "</p>\n" +
      "<p> Daily probability of a non-interactivePage within pageWeightBounds having a pageSpeedAlert: " + nonInteractiveUnderWeightPageSpeedProbabilityToday + "</p>\n" +
      "</div>\n"
  }

  def generateLongTermRunData(): String = {
    "<div><h2>Cumulative Data Summary</h2> </div>\n" +
      "<div>" +
      "<p> Average Time taken for job to complete: " + averageRunDurationLongTerm + "</p>\n" +
      "<p> Average Daily number of pages pulled from CAPI: " + cAPIPageDailyAverage + "</p>\n" +
      "<p> Average Daily number of pages from previous runs retested" + pageRetestedCountDailyAverage+ "</p>\n" +
      "<p> Average Daily number of pages sent to wpt instance for testing: " + wptPageCountDailyAverage+ "</p>\n" +
      "<p> Average Daily number of failed Tests: " + failedTestsDailyAverage + "</p>\n" +
      "<p> Average Daily number of pageWeightAlerts raised: " + pageWeightDailyAverage + "</p>\n" +
      "<p> Average Daily number of pageSpeedAlerts raised: " + pageSpeedDailyAverage + "</p>\n" +
      "<p> Average Daily number of pageSpeedAlerts raised with no corresponding pageWeight alert: " + pageWeightNoPageSpeedDailyAverage + "</p>\n" +
      "<p> Average Daily number of pageWeightAlerts raised on Non-Interactive content: " + pageWeightNonInteractiveDailyAverage + "</p>\n" +
      "<p> Average Daily number of pageSpeedAlerts raised on Non-Interactive content: " + pageSpeedNonInteractiveDailyAverage + "</p>\n" +
      "<p> Average Daily number of pageSpeedAlerts raised with no corresponding pageWeight alert on Non-Interactive content: " + pageWeightNoPageSpeedNonInteractiveDailyAverage + "</p>\n" +
      "<p> Average Daily number of pageWeightAlerts raised on Interactive content: " + pageWeightInteractiveDailyAverage + "</p>\n" +
      "<p> Average Daily number of pageSpeedAlerts raised on Interactive content: " + pageSpeedInteractiveDailyAverage + "</p>\n" +
      "<p> Average Daily number of pageSpeedAlerts raised with no corresponding pageWeight alert on Interactive content: " + pageWeightNoPageSpeedInteractiveDailyAverage+ "</p>\n" +
      "<p> Long term probability of non-interactivePage having a pageWeightAlert: " + nonInteractivePageWeightProbabilityLongTerm + "</p>\n" +
      "<p> Long term probability of non-interactivePage having a pageSpeedAlert: " + nonInteractivePageSpeedProbabilityLongTerm + "</p>\n" +
      "<p> Long term probability of a non-interactivePage within pageWeightBounds having a pageSpeedAlert: " + nonInteractiveUnderWeightPageSpeedProbabilityLongTerm + "</p>\n" +
      "</div>\n"
  }


  // Access Methods

    override def toString(): String = {
      println("\n PAGEWEIGHTDASHBOARD--TABBED CREATED \n" +
        "Number of combined records " + numberOfCombinedRecords +
        "Number of desktop records " + numberOfDesktopRecords +
        "Number of mobile records " + numberOfMobileRecords + "\n \n \n")
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
