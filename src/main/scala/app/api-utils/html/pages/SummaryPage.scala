  package app.api


  import app.apiutils.PerformanceResultsObject
  import org.joda.time.DateTime

  /**
   * Created by mmcnamara on 15/04/16.
   */
  class SummaryPage(dataSummary: DataSummary) {

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

/*    val HTML_PAGE_TABS_LIST: String = "<div class=\"tabs\">" + "\n" +
      "<ul class=\"tab-links\">" + "\n" +
      "<li class=\"active\">" + "<a href=\"#mobile\">Mobile view</a>" + "</li>" + "\n" +
      "<li>" + "<a href=\"#desktop\">Desktop view</a>" + "</li>" + "\n" +
      "<li>" + "<a href=\"#combined\">Combined view</a>" + "</li>" + "\n" +
      "</ul>" + "\n"
    //close div added to HTML_FOOTER*/

    //Page Content
 /*   val HTML_TAB_CONTENT: String = "<div id=\"tab-content\">" + "\n"

    val HTML_TAB_HEADER: String = "<div class=\"tab-content\">" + "\n"


    val HTML_MOBILE_TAB_CONTENT_HEADER: String = "<div id=\"mobile\" class=\"tab active\">" + "\n" +
      "<p>" + "<h2>Mobile view" + "</h2>" + "</p>" + "\n"

    val HTML_DESKTOP_TAB_CONTENT_HEADER: String = "<div id=\"desktop\" class=\"tab\">" + "\n" +
      "<p>" + "<h2>Desktop view" + "</h2>" + "</p>" + "\n"

    val HTML_COMBINED_TAB_CONTENT_HEADER: String = "<div id=\"combined\" class=\"tab\">" + "\n" +
      "<p>" + "<h2>Combined view" + "</h2>" + "</p>" + "\n"

    val HTML_TAB_CONTENT_FOOTER: String = "</div>" + "\n"
*/
    //Page Tables
    val HTML_REPORT_TABLE_HEADERS: String = "<table id=\"elementSummary\">"+ "\n" +
      "<thead>" + "\n" + "<caption> Summary of Editorial Embeds from " + dataSummary.totalNumberOfTests + " tested pages</caption>" +
      "<tr>" + "<th>Type of Embed</th>" + "<th>Number of pages with this embed-type</th>" + "<th>Chance of a page containing this embed-type</th>" + "<th>Number of pages that alerted for pageWeight</th>" + "<th>Chance of this embed-type triggering a pageWeight alert</th>" + "<th>Number of Pages that alerted for pageSpeed</th>" + "<th>Chance of this embed-type triggering a pageSpeed alert</th>" + "<th>Average Size of this embed-type (KB)</th>" + "<th>Average Time to First Paint of a page with this embed-type</th>" + "<th>Average SpeedIndex of a page with this embed-type</th>" +  "</tr>"+ "\n" +
      "</thead>" +"\n" +
      "<tbody>"

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
      summaryDataToHTMLString() + HTML_FOOTER

    def runDataToHTMLString(): String = {
      "<div>\n" +
        "<h3>Job Summary:" + "</h3>" + "\n" +
        "<p style = \"margin-left: 100px\">jobStarted at:                                 " + dataSummary.jobStartedTime.toDateTime + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">jobFinished at:                                " + dataSummary.jobFinishTime.toDateTime + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Duration of Run:                               " + dataSummary.durationOfRunMin + " minutes." + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pages from CAPI queries:             " + dataSummary.numberOfPagesFromCAPI + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pages retested from previous run:    " + dataSummary.numberOfPagesRetestedFromLastRun + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pages tested:                        " + dataSummary.numberOfPagesSentToWPT + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of new pageWeight alerts:               " + dataSummary.newPageWeightAlerts.length + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of new pageSpeed alerts:                " + dataSummary.newPageSpeedAlerts.length + "</p>" +  "\n" +
        //"<p style = \"margin-left: 100px\">Number of pageWeight alerts resolved this run: " + dataSummary.numberOfPageWeightAlertsResolvedThisRun + "</p>" +  "\n" +
        //"<p style = \"margin-left: 100px\">Number of pageSpeed alerts resolved this run:  " + dataSummary.numberOfPageSpeedAlertsResolvedThisRun + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of failed tests:                        " + dataSummary.numberOfFailedTests + "</p>" +  "\n" +
        "</div>" + "\n"
    }

    def pageDataToHTMLString(): String = {
      val pageSummaryHeader = "<div>\n" +
        "<h3>Page Summary:" + "</h3>" + "\n"
      val alertsBreakdown = "<div>" + "\n" +
        "<p style = \"margin-left: 40px\">Breakdown of Alerts:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Total Number of tests:                                 " + dataSummary.allResults.length + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Total Number of pageWeight alerts:                     " + dataSummary.numberOfPageWeightAlertsInTotal + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Total Number of pageSpeed alerts:                      " + dataSummary.numberOfPageSpeedAlertsInTotal + "</p>" +  "\n" +
        //"<p style = \"margin-left: 100px\">Total Number of pageWeight alerts resolved:            " + dataSummary.pageWeightAlertsThatHaveBeenResolved.length + "</p>" +  "\n" +
        //"<p style = \"margin-left: 100px\">Total Number of pageSpeed alerts resolved:             " + dataSummary.pageSpeedAlertsThatHaveBeenResolved.length + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Articles:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Number of article pages:                               " + dataSummary.articles.length + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pageWeight alerts on article pages:          " + dataSummary.numberOfPageWeightAlertsOnArticlePages + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pageSpeed alerts on article pages:           " + dataSummary.numberOfPageSpeedAlertsOnArticlePages + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Percentage of pageWeight alerts on article pages:      " + roundAt(2)(100 * dataSummary.numberOfPageWeightAlertsOnArticlePages.toDouble / dataSummary.articles.length) + "%</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Percentage of pageSpeed alerts on article pages:       " + roundAt(2)(100 * dataSummary.numberOfPageSpeedAlertsOnArticlePages.toDouble / dataSummary.articles.length) + "%</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Interactives:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Number of interactive pages:                           " + dataSummary.interactives.length + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pageWeight alerts on interactive pages:      " + dataSummary.numberOfPageWeightAlertsOnInteractivePages + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pageSpeed alerts on interactive pages:       " + dataSummary.numberOfPageSpeedAlertsOnInteractivePages + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Percentage of pageWeight alerts on interactive pages:  " + roundAt(2)(100 * dataSummary.numberOfPageWeightAlertsOnInteractivePages.toDouble / dataSummary.interactives.length) + "%</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Percentage of pageSpeed alerts on interactive pages:  " + roundAt(2)(100 * dataSummary.numberOfPageSpeedAlertsOnInteractivePages.toDouble / dataSummary.interactives.length) + "%</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">LiveBlogs:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Number of liveblog pages:                              " + dataSummary.liveBlogs.length + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pageWeight alerts on liveblog pages:         " + dataSummary.numberOfPageWeightAlertsOnLiveBlogPages + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pageSpeed alerts on liveblog pages:          " + dataSummary.numberOfPageSpeedAlertsOnLiveBlogPages + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Percentage of pageWeight alerts on liveblog pages:     " + roundAt(2)(100 * dataSummary.numberOfPageWeightAlertsOnLiveBlogPages.toDouble / dataSummary.liveBlogs.length) + "%</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Percentage of pageSpeed alerts on liveblog pages:     " + roundAt(2)(100 * dataSummary.numberOfPageSpeedAlertsOnLiveBlogPages.toDouble / dataSummary.liveBlogs.length) + "%</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">GLabs Pages:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Number of gLabs pages:                                 " + dataSummary.gLabs.length + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pageWeight alerts on gLabs pages:            " + dataSummary.numberOfPageWeightAlertsOnGLabsPages + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Number of pageSpeed alerts on gLabs pages:             " + dataSummary.numberOfPageSpeedAlertsOnGLabsPages + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Percentage of pageWeight alerts on gLabs pages:        " + roundAt(2)(100 * dataSummary.numberOfPageWeightAlertsOnGLabsPages.toDouble / dataSummary.gLabs.length) + "%</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Percentage of pageSpeed alerts on gLabs pages:        " + roundAt(2)(100 * dataSummary.numberOfPageSpeedAlertsOnGLabsPages.toDouble / dataSummary.gLabs.length) + "%</p>" +  "\n" +
       "</div>" + "\n"
      val pageAverages = "<div>" + "\n" +
        "<p style = \"margin-left: 40px\">Page Averages:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Articles:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Average pageWeight of article pages:                   " + dataSummary.averageArticlePageWeight + " KB" + "</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Average time-to-first-paint of article pages:          " + dataSummary.averageArticleTTFP + " ms</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Average speed-index of article pages:                  " + dataSummary.averageArticleSpeedIndex + " ms</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Interactives:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Average pageWeight of interactive pages:               " + dataSummary.averageInteractivePageWeight + " KB</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Average time-to-first-paint of interactive pages:      " + dataSummary.averageInteractiveTTFP + " ms</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Average speed-index of interactive pages:              " + dataSummary.averageInteractiveSpeedIndex + " ms</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">LiveBlogs:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Average pageWeight of liveBlog pages:                  " + dataSummary.averageLiveBlogPageWeight + " KB</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Average time-to-first-paint of liveBlog pages:         " + dataSummary.averageLiveBlogTTFP + " ms</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Average speed-index of liveBlog pages:                 " + dataSummary.averageLiveBlogSpeedIndex + " ms</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">GLabs:</p>" + "\n" +
        "<p style = \"margin-left: 100px\">Average pageWeight of gLabs pages:                     " + dataSummary.averageGLabsPageWeight + " KB</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Average time-to-first-paint of gLabs pages:            " + dataSummary.averageGLabsTTFP + " ms</p>" +  "\n" +
        "<p style = \"margin-left: 100px\">Average speed-index of gLabs pages:                    " + dataSummary.averageGLabsSpeedIndex + " ms</p>" +  "\n" +
        "</div>" + "\n"
      val pageSummaryFooter = "</div>" + "\n"

      pageSummaryHeader + alertsBreakdown + pageAverages + pageSummaryFooter
    }

    def summaryDataToHTMLString(): String = {
      val elementString: String = HTML_REPORT_TABLE_HEADERS +
        runDataToHTMLString() + pageDataToHTMLString() +
        dataSummary.sortedSummaryList.map(elementData => returnElementSummaryAsHTMLRow(elementData)).mkString +
        HTML_TABLE_END
      elementString
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
