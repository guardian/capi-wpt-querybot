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
/*    val HTML_REPORT_TABLE_HEADERS: String = "<table id=\"report\">"+ "\n" +
      "<thead>" + "\n" +
      "<tr> <th>Time Page Last Updated</th>" + "<th>Test VisualsElementType</th>" + "<th>Headline</th>" + "<th>VisualsElementType of Page</th>" + "<th>Time till page looks loaded</th>" + "<th>Page weight (MB)</th>" + "<th>Click for more details</th>" +  "</tr>"+ "\n" +
      "</thead>" +"\n" +
      "<tbody>"

    val HTML_PAGE_ELEMENT_TABLE_HEADERSPT1: String = "<tr>" + "\n" +
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
      "<div id=\"footer\">" + "</div>" + "\n" +
      "</div>" + "\n" +
      "</div>" + "\n" +
      "</body>" + "\n" +
      "</html>"


    //HTML_PAGE
    val HTML_PAGE: String = HTML_PAGE_HEAD + HTML_PAGE_CONTAINER +
      "<div>" + dataSummary.summaryDataToString() + "</div>" + HTML_FOOTER


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
