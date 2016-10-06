package app.api


/**
 * Created by Michael on 05/10/2016.
 */
class PeriodicReportPage(periodicReport: PeriodicReport) {

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
    "<ul class=\"tab-links-meta\">" + "\n" +
    "<li class=\"selected\">" + "<a href=\"#fullsummary\">View entire period</a>" + "</li>" + "\n" +
    "<li>" + "<a href=\"#weeksummary\">View week so far</a>" + "</li>" + "\n" +
    "<li class>" + "<a href=\"#daysummary\">View today so far</a>" + "</li>" + "\n" +
    "<li class>" + "<a href=\"#runsummary\">View latest run</a>" + "</li>" + "\n" +
    "</ul>" + "\n"
  //close div added to HTML_FOOTER

  //Page Content
  val HTML_TAB_CONTENT: String = "<div id=\"tab-content\">" + "\n"

  val HTML_TAB_HEADER: String = "<div class=\"tab-content\">" + "\n"


  val HTML_FULL_SUMMARY_TAB_CONTENT_HEADER: String = "<div id=\"fullsummary\" class=\"meta-tab selected\">" + "\n" +
    "<p>" + "<h2>Summary of full period" + "</h2>" + "</p>" + "\n"

  val HTML_WEEK_SUMMARY_TAB_CONTENT_HEADER: String = "<div id=\"weeksummary\" class=\"meta-tab\">" + "\n" +
    "<p>" + "<h2>Summary of current week" + "</h2>" + "</p>" + "\n"

  val HTML_DAY_SUMMARY_TAB_CONTENT_HEADER: String = "<div id=\"/daysummary\" class=\"meta-tab\">" + "\n" +
    "<p>" + "<h2>Summary of current day" + "</h2>" + "</p>" + "\n"

  val HTML_RUN_SUMMARY_TAB_CONTENT_HEADER: String = "<div id=\"runsummary\" class=\"meta-tab\">" + "\n" +
    "<p>" + "<h2>Summary of latest run" + "</h2>" + "</p>" + "\n"

  val HTML_TAB_CONTENT_FOOTER: String = "</div>" + "\n"

  val HTML_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n"

  val HTML_PAGE_ELEMENT_TABLE_END: String = "</tbody>" + "\n" + "</table>"+ "\n" + "</td>" + "\n" + "</tr>" + "\n"

  //Page Footer
  val HTML_FOOTER: String = "</div>" + "\n" +
    "<div id=\"footer\">" + "</div>" + "\n" +
    "</div>" + "\n" +
    "</div>" + "\n" +
    "</body>" + "\n" +
    "</html>"

  //summary data
  val fullSummaryPage = new SummaryPage(periodicReport.fullSummary)
  val weekSummaryPage = new SummaryPage(periodicReport.summaryWeekSoFar)
  val daySummaryPage = new SummaryPage(periodicReport.summaryDaySoFar)
  val runSummaryPage = new SummaryPage(periodicReport.summaryCurrentRun)

  //HTML_PAGE
  val HTML_PAGE: String = HTML_PAGE_HEAD + HTML_PAGE_CONTAINER +
    HTML_PAGE_TABS_LIST +
    HTML_TAB_CONTENT +
    HTML_DAY_SUMMARY_TAB_CONTENT_HEADER +
    fullSummaryPage.toHTML_TAB_String() + "<p>&nbsp;</p>" +
    HTML_TAB_CONTENT_FOOTER +
    HTML_WEEK_SUMMARY_TAB_CONTENT_HEADER +
    weekSummaryPage.toHTML_TAB_String().replace("active", "") + "<p>&nbsp;</p>" +
    HTML_TAB_CONTENT_FOOTER +
    HTML_DAY_SUMMARY_TAB_CONTENT_HEADER +
    daySummaryPage.toHTML_TAB_String().replace("active", "") + "<p>&nbsp;</p>" +
    HTML_TAB_CONTENT_FOOTER +
    HTML_RUN_SUMMARY_TAB_CONTENT_HEADER +
    runSummaryPage.toHTML_TAB_String().replace("active", "") +
    HTML_TAB_CONTENT_FOOTER +
    HTML_FOOTER


  override def toString: String = {
    HTML_PAGE
  }

}
