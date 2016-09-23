package app.api

import app.apiutils.PerformanceResultsObject
import org.joda.time.DateTime

/**
 * Created by mmcnamara on 09/09/16.
 */
class WeeklyReport(dataSummary: DataSummary) {

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
    "<h1>Summary of Performance Data </h1>" + "\n" +
    "</div>" + "\n"

  val HTML_PAGE_TABS_LIST: String = "<div class=\"tabs\">" + "\n" +
    "<ul class=\"tab-links\">" + "\n" +
    "<li class=\"active\">" + "<a href=\"#runsummary\">Weekly run summary</a>" + "</li>" + "\n" +
    "<li>" + "<a href=\"#mobile\">Weekly mobile summary</a>" + "</li>" + "\n" +
    "<li>" + "<a href=\"#desktop\">Weekly desktop summary</a>" + "</li>" + "\n" +
    "<li>" + "<a href=\"#embedssummary\">Weekly summary of embeds</a>" + "</li>" + "\n" +
    "</ul>" + "\n"
  //close div added to HTML_FOOTER

  //Page Content
  val HTML_TAB_CONTENT: String = "<div id=\"tab-content\">" + "\n"

  val HTML_TAB_HEADER: String = "<div class=\"tab-content\">" + "\n"


  val HTML_MOBILE_TAB_CONTENT_HEADER: String = "<div id=\"mobile\" class=\"tab\">" + "\n" +
    "<p>" + "<h2>Summary for mobile tests" + "</h2>" + "</p>" + "\n"

  val HTML_DESKTOP_TAB_CONTENT_HEADER: String = "<div id=\"desktop\" class=\"tab\">" + "\n" +
    "<p>" + "<h2>Summary for desktop tests" + "</h2>" + "</p>" + "\n"

  val HTML_RUN_SUMMARY_TAB_CONTENT_HEADER: String = "<div id=\"runsummary\" class=\"tab active\">" + "\n" +
    "<p>" + "<h2>Summary of latest run" + "</h2>" + "</p>" + "\n"

  val HTML_EMBED_SUMMARY_TAB_CONTENT_HEADER: String = "<div id=\"embedssummary\" class=\"tab\">" + "\n" +
    "<p>" + "<h2>Summary of editorial embeds" + "</h2>" + "</p>" + "\n"

  val HTML_TAB_CONTENT_FOOTER: String = "</div>" + "\n"

  //Page Tables
  val HTML_MOBILE_EDITORIAL_EMBEDS_TABLE_HEADERS: String = "<p> Summary of editorial embeds on mobile pages </p>" + "<table id=\"elementSummary\">"+ "\n" +
    "<thead>" + "\n" + "<caption> Summary of editorial embeds from " + dataSummary.totalNumberOfTests + " pages tested on mobile</caption>" +
    "<tr>" + "<th>Type of Embed</th>" + "<th>Number of pages with this embed-type</th>" + "<th>Chance of a page containing this embed-type</th>" + "<th>Number of pages that alerted for pageWeight</th>" + "<th>Chance of this embed-type triggering a pageWeight alert</th>" + "<th>Number of Pages that alerted for pageSpeed</th>" + "<th>Chance of this embed-type triggering a pageSpeed alert</th>" + "<th>Average Size of this embed-type (KB)</th>" + "<th>Average Time to First Paint of a page with this embed-type</th>" + "<th>Average SpeedIndex of a page with this embed-type</th>" +  "</tr>"+ "\n" +
    "</thead>" +"\n" +
    "<tbody>"

  val HTML_DESKTOP_EDITORIAL_EMBEDS_TABLE_HEADERS: String = "<p> Summary of editorial embeds on desktop pages </p>" + "<table id=\"elementSummary\">"+ "\n" +
    "<thead>" + "\n" + "<caption> Summary of editorial embeds from " + dataSummary.totalNumberOfTests + " pages tested on mobile and desktop</caption>" +
    "<tr>" + "<th>Type of Embed</th>" + "<th>Number of pages with this embed-type</th>" + "<th>Chance of a page containing this embed-type</th>" + "<th>Number of pages that alerted for pageWeight</th>" + "<th>Chance of this embed-type triggering a pageWeight alert</th>" + "<th>Number of Pages that alerted for pageSpeed</th>" + "<th>Chance of this embed-type triggering a pageSpeed alert</th>" + "<th>Average Size of this embed-type (KB)</th>" + "<th>Average Time to First Paint of a page with this embed-type</th>" + "<th>Average SpeedIndex of a page with this embed-type</th>" +  "</tr>"+ "\n" +
    "</thead>" +"\n" +
    "<tbody>"

  val HTML_COMBINED_EDITORIAL_EMBEDS_TABLE_HEADERS: String = "<p> Summary of editorial embeds on mobile pages </p>" + "<table id=\"elementSummary\">"+ "\n" +
    "<thead>" + "\n" + "<caption> Summary of editorial embeds from " + dataSummary.totalNumberOfTests + " pages tested on mobile and desktop</caption>" +
    "<tr>" + "<th>Type of Embed</th>" + "<th>Number of pages with this embed-type</th>" + "<th>Chance of a page containing this embed-type</th>" + "<th>Number of pages that alerted for pageWeight</th>" + "<th>Chance of this embed-type triggering a pageWeight alert</th>" + "<th>Number of Pages that alerted for pageSpeed</th>" + "<th>Chance of this embed-type triggering a pageSpeed alert</th>" + "<th>Average Size of this embed-type (KB)</th>" + "<th>Average Time to First Paint of a page with this embed-type</th>" + "<th>Average SpeedIndex of a page with this embed-type</th>" +  "</tr>"+ "\n" +
    "</thead>" +"\n" +
    "<tbody>"

  val HTML_PAGE_SUMMARY_MOBILE_TABLE_HEADERS: String =       "<table id=\"pageSummary\">"+ "\n" +
    "<thead>" + "\n" + "<caption> Summary of " + dataSummary.totalNumberOfMobileTests + " pages tested on Mobile</caption>" +
    "<tr>" + "<th>Type of Page</th>" + "<th>Number of tests for this page type</th>" + "<th>Number of pageweight alerts</th>" + "<th>Percentage of pageweight alerts</th>" + "<th>Number of pagespeed alerts</th>" + "<th>Percentage of pagespeed alerts</th>" + "<th>Average Size (KB)</th>" + "<th>Average Time to First Paint (ms)</th>" + "<th>Average SpeedIndex (ms)</th>" +  "</tr>"+ "\n" +
    "</thead>" +"\n" +
    "<tbody>" + "\n"

  val HTML_PAGE_SUMMARY_DESKTOP_TABLE_HEADERS: String =       "<table id=\"pageSummary\">"+ "\n" +
    "<thead>" + "\n" + "<caption> Summary of " + dataSummary.totalNumberOfDesktopTests + " pages tested on Desktop</caption>" +
    "<tr>" + "<th>Type of Page</th>" + "<th>Number of tests for this page type</th>" + "<th>Number of pageweight alerts</th>" + "<th>Percentage of pageweight alerts</th>" + "<th>Number of pagespeed alerts</th>" + "<th>Percentage of pagespeed alerts</th>" + "<th>Average Size (KB)</th>" + "<th>Average Time to First Paint (ms)</th>" + "<th>Average SpeedIndex (ms)</th>" +  "</tr>"+ "\n" +
    "</thead>" +"\n" +
    "<tbody>" + "\n"


  val HTML_ELEMENT_SUMMARY_TABLE_HEADERSPT1: String = "<tr>" + "\n" +
    "<td colspan=\"12\">" + "<table id=\"data\" class=\"data\">" + "\n" +
    "<caption>"


  val HTML_ELEMENT_SUMMARY_TABLE_HEADERSPT2: String =
    "</caption>" + "\n" +
      "<thead>" + "\n" +
      "<tr>" + "<th>Data</th>" + "<th>Value</th>" +
      "</thead>" +"\n" +
      "<tbody>"

  val HTML_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n"

  val HTML_PAGE_ELEMENT_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n" + "</td>" + "\n" + "</tr>" + "\n"

  //Page Footer
  val HTML_FOOTER: String = "</div>" + "\n" +
    "<div id=\"footer\">" + "</div>" + "\n" +
    "</div>" + "\n" +
    "</div>" + "\n" +
    "</body>" + "\n" +
    "</html>"


  //HTML_PAGE
  val HTML_PAGE: String = HTML_PAGE_HEAD + HTML_PAGE_CONTAINER +
    HTML_PAGE_TABS_LIST +
    HTML_TAB_CONTENT +
    HTML_MOBILE_TAB_CONTENT_HEADER +
    mobilePageSummaryToHTMLString() + "<p>&nbsp;</p>" +
    //mobileElementSummaryToHTMLString() +
    HTML_TAB_CONTENT_FOOTER +
    HTML_DESKTOP_TAB_CONTENT_HEADER +
    desktopPageSummaryToHTMLString() + "<p>&nbsp;</p>" +
    //desktopElementSummaryToHTMLString() +
    HTML_TAB_CONTENT_FOOTER +
    HTML_RUN_SUMMARY_TAB_CONTENT_HEADER +
    runDataToHTMLString() ++ "<p>&nbsp;</p>" +
    HTML_TAB_CONTENT_FOOTER +
    HTML_EMBED_SUMMARY_TAB_CONTENT_HEADER +
    combinedElementSummaryToHTMLString()
  HTML_TAB_CONTENT_FOOTER +
    HTML_FOOTER


  def runDataToHTMLString(): String = {
    "<div>\n" +
      "<h3>Job Summary:" + "</h3>" + "\n" +
      "<p style = \"margin-left: 100px\">Number of pages from CAPI queries:             " + dataSummary.numberOfPagesFromCAPI + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Number of pages retested from previous run:    " + dataSummary.numberOfPagesRetestedFromLastRun + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Number of pages tested:                        " + dataSummary.numberOfPagesSentToWPT + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Number of new pageWeight alerts:               " + dataSummary.newPageWeightAlerts.length + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Number of new pageSpeed alerts:                " + dataSummary.newPageSpeedAlerts.length + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Number of pageWeight alerts resolved this run: " + dataSummary.numberOfPageWeightAlertsResolvedThisRun + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Number of pageSpeed alerts resolved this run:  " + dataSummary.numberOfPageSpeedAlertsResolvedThisRun + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Date of oldest test on record:                           " + dataSummary.dateOfOldestTest + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Date of oldest alert on record:                          " + dataSummary.dateOfOldestTest + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Total Number of pageWeight alerts triggered to date:   " + dataSummary.totalNumberOfPageWeightAlertsTriggered + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Total Number of pageWeight alerts resolved to date:    " + dataSummary.totalNumberOfPageWeightAlertsResolved + "</p>" +  "\n" +
      "<p style = \"margin-left: 100px\">Number of failed tests:                        " + dataSummary.numberOfFailedTests + "</p>" +  "\n" +
      "</div>" + "\n"
  }


  def mobileElementSummaryToHTMLString(): String = {
    val elementString: String = "<div><p>" + HTML_MOBILE_EDITORIAL_EMBEDS_TABLE_HEADERS +
      dataSummary.sortedCombinedSummaryList.map(elementData => returnElementSummaryAsHTMLRow(elementData)).mkString +
      HTML_TABLE_END + "</p></div>"
    elementString
  }

  def desktopElementSummaryToHTMLString(): String = {
    val elementString: String = "<div><p>" + HTML_DESKTOP_EDITORIAL_EMBEDS_TABLE_HEADERS +
      dataSummary.sortedCombinedSummaryList.map(elementData => returnElementSummaryAsHTMLRow(elementData)).mkString +
      HTML_TABLE_END + "</p></div>"
    elementString
  }

  def combinedElementSummaryToHTMLString(): String = {
    val elementString: String = "<div><p>" + HTML_COMBINED_EDITORIAL_EMBEDS_TABLE_HEADERS +
      dataSummary.sortedCombinedSummaryList.map(elementData => returnElementSummaryAsHTMLRow(elementData)).mkString +
      HTML_TABLE_END + "</p></div>"
    elementString
  }

  def mobilePageSummaryToHTMLString(): String = {
    "<div><p>Summary of mobile pages: </p>" +
      HTML_PAGE_SUMMARY_MOBILE_TABLE_HEADERS +
      returnPageSummaryAsHTMLRows(dataSummary.articlesMobileSummary) +
      returnPageSummaryAsHTMLRows(dataSummary.interactivesMobileSummary) +
      returnPageSummaryAsHTMLRows(dataSummary.liveBlogsMobileSummary) +
      returnPageSummaryAsHTMLRows(dataSummary.gLabsMobileSummary) +
      returnPageSummaryAsHTMLRows(dataSummary.totalMobileSummary) +
      HTML_TABLE_END +
      "</div>"
  }

  def desktopPageSummaryToHTMLString(): String = {
    "<div><p>Summary of desktop pages: </p>" +
      HTML_PAGE_SUMMARY_DESKTOP_TABLE_HEADERS +
      returnPageSummaryAsHTMLRows(dataSummary.articlesDesktopSummary) +
      returnPageSummaryAsHTMLRows(dataSummary.interactivesDesktopSummary) +
      returnPageSummaryAsHTMLRows(dataSummary.liveBlogsDesktopSummary) +
      returnPageSummaryAsHTMLRows(dataSummary.gLabsDesktopSummary) +
      returnPageSummaryAsHTMLRows(dataSummary.totalDesktopSummary) +
      HTML_TABLE_END +
      "</div>"
  }

  def returnPageSummaryAsHTMLRows(pageSummary: dataSummary.PageSummaryData):  String = {
    "<tr>"+ "\n" +
      "<td>" + pageSummary.pageType + "</td>" + "\n" +
      "<td>" + pageSummary.testType + "</td>" + "\n" +
      "<td>" + pageSummary.numberOfPageWeightAlerts + "</td>" + "\n" +
      "<td>" + pageSummary.percentageOfPageWeightAlerts + "</td>" + "\n" +
      "<td>" + pageSummary.numberOfPageSpeedAlerts + "</td>" + "\n" +
      "<td>" + pageSummary.percentageOfPageSpeedAlerts + "</td>" + "\n" +
      "<td>" + pageSummary.averagePageWeight + "</td>" + "\n" +
      "<td>" + pageSummary.averageTTFP + "</td>" + "\n" +
      "<td>" + pageSummary.averageSpeedIndex + "</td>" + "\n" +
      "<tr>"
  }


  def returnElementSummaryAsHTMLRow(elementSummary: dataSummary.ElementSummaryData): String = {
    "<tr>" + "\n" +
      "<td>" + elementSummary.title + "</td>" + "\n" +
      "<td>" + elementSummary.numberOfPagesWithEmbed + "</td>" + "\n" +
      "<td>" + elementSummary.percentageOfPagesWithEmbed + "%"+ "</td>" + "\n" +
      "<td>" + elementSummary.numberOfPageWeightAlerts + "</td>" + "\n" +
      "<td>" + elementSummary.percentageOfPageWeightAlerts + "%"+ "</td>" + "\n" +
      "<td>" + elementSummary.numberOfPageSpeedAlerts + "</td>" + "\n" +
      "<td>" + elementSummary.percentageOfPageSpeedAlerts + " %"+ "</td>" + "\n" +
      "<td>" + elementSummary.averageSizeOfEmbeds + " KB" + "</td>" + "\n" +
      "<td>" + elementSummary.averageTimeFirstPaint + " ms" + "</td>" + "\n" +
      "<td>" + elementSummary.averageSpeedIndexMs + " ms"+ "</td>" + "\n" +
      "</tr>" + "\n"
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

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

