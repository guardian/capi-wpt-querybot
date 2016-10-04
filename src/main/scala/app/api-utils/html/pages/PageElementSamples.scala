package app.api

import app.apiutils.PerformanceResultsObject
import org.joda.time.DateTime


/**
 * Created by mmcnamara on 24/08/16.
 */
class PageElementSamples(dataSummary: DataSummary) {

  //HTML Page elements
  //Page Header

  val sampleArray = dataSummary.getSamplePageArray
  val sampleArrayFromAlerts = dataSummary.getSamplePageArray

  val HTML_PAGE_HEAD: String = "<!DOCTYPE html><html lang=\"en\">" + "\n" +
    "<head> <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>" + "\n" +
    "<title>Editorial Element Sample Pages - [Editorial Element Sample Pages]</title>" + "\n" +
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
    "<h1>List of Sample Pages </h1>" + "\n" +
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
    "<thead>" + "\n" + "<caption> Sample pages for each embed type from job that finished at " + dataSummary.jobFinishTime.toDateTime +"</caption>" +
    "<tr>" + "<th>Type of Embed</th>" + "<th>page url</th>" + "</tr>"+ "\n" +
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
      "<p style = \"margin-left: 40px\">jobStarted at:                              " + dataSummary.jobStartedTime.toDateTime + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">jobFinished at:                             " + dataSummary.jobFinishTime.toDateTime + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Duration of Run:                            " + dataSummary.durationOfRunMin + " minutes." + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Number of pages from CAPI queries:          " + dataSummary.numberOfPagesFromCAPI + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Number of pages retested from previous run: " + dataSummary.numberOfPagesRetestedFromLastRun + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Number of pages tested:                     " + dataSummary.numberOfPagesSentToWPT + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Number of failed tests:                     " + dataSummary.numberOfFailedTests + "</p>" +  "\n" +
      "</div>" + "\n"
  }

  def summaryDataToHTMLString(): String = {
    val elementString: String = HTML_REPORT_TABLE_HEADERS +
      returnPageSamplesAsHTMLRows() +
      HTML_TABLE_END
    elementString
  }


  def returnPageSamplesAsHTMLRows(): String = {
    val row0 = "<tr>" + "<td>" + "audioboom: " + "</td>" + "<td>" + returnPageSample(0) + "</td>" + "</tr>" + "\n"
    val row1 = "<tr>" + "<td>" + "brightcove: " + "</td>" + "<td>"  + returnPageSample(1) + "</td>" + "</tr>" + "\n"
    val row2 =  "<tr>" + "<td>" + "cnn: " + "</td>" + "<td>"  + returnPageSample(2) + "</td>" + "</tr>" + "\n"
    val row3 =  "<tr>" + "<td>" + "dailymotion: " + "</td>" + "<td>"  + returnPageSample(3) + "</td>" + "</tr>" + "\n"
    val row4 =  "<tr>" + "<td>" + "datawrapper chart: " + "</td>" + "<td>"  + returnPageSample(4) + "</td>" + "</tr>" + "\n"
    val row5 =  "<tr>" + "<td>" + "documentCloud: " + "</td>" + "<td>"  + returnPageSample(5) + "</td>" + "</tr>" + "\n"
    val row6 =  "<tr>" + "<td>" + "facebook: " + "</td>" + "<td>"  + returnPageSample(6) + "</td>" + "</tr>" + "\n"
    val row7 =  "<tr>" + "<td>" + "formStack: " + "</td>" + "<td>"  + returnPageSample(7) + "</td>" + "</tr>" + "\n"
    val row8 =  "<tr>" + "<td>" + "gif: " + "</td>" + "<td>"  + returnPageSample(8) + "</td>" + "</tr>" + "\n"
    val row9 =  "<tr>" + "<td>" + "gLabs Embed: " + "</td>" + "<td>"  + returnPageSample(9) + "</td>" + "</tr>" + "\n"
    val row10 =  "<tr>" + "<td>" + "googleMaps: " + "</td>" + "<td>"  + returnPageSample(10) + "</td>" + "</tr>" + "\n"
    val row11 =  "<tr>" + "<td>" + "guardianAudio: " + "</td>" + "<td>"  + returnPageSample(11) + "</td>" + "</tr>" + "\n"
    val row12 =  "<tr>" + "<td>" + "guardianComments: " + "</td>" + "<td>"  + returnPageSample(12) + "</td>" + "</tr>" + "\n"
    val row13 =  "<tr>" + "<td>" + "guardianVideo: " + "</td>" + "<td>"  + returnPageSample(13) + "</td>" + "</tr>" + "\n"
    val row14 =  "<tr>" + "<td>" + "guardianImages: " + "</td>" + "<td>"  + returnPageSample(14) + "</td>" + "</tr>" + "\n"
    val row15 =  "<tr>" + "<td>" + "guardianUpload: " + "</td>" + "<td>"  + returnPageSample(15) + "</td>" + "</tr>" + "\n"
    val row16 =  "<tr>" + "<td>" + "guardianWitnessImage: " + "</td>" + "<td>"  + returnPageSample(16) + "</td>" + "</tr>" + "\n"
    val row17 =  "<tr>" + "<td>" + "guardianWitnessVideo: " + "</td>" + "<td>"  + returnPageSample(17) + "</td>" + "</tr>" + "\n"
    val row18 =  "<tr>" + "<td>" + "hulu: " + "</td>" + "<td>"  + returnPageSample(18) + "</td>" + "</tr>" + "\n"
    val row19 =  "<tr>" + "<td>" + "image: " + "</td>" + "<td>"  + returnPageSample(19) + "</td>" + "</tr>" + "\n"
    val row20 =  "<tr>" + "<td>" + "infoStrada: " + "</td>" + "<td>"  + returnPageSample(20) + "</td>" + "</tr>" + "\n"
    val row21 =  "<tr>" + "<td>" + "instagram: " + "</td>" + "<td>"  + returnPageSample(21) + "</td>" + "</tr>" + "\n"
    val row22 =  "<tr>" + "<td>" + "interactive: " + "</td>" + "<td>"  + returnPageSample(22) + "</td>" + "</tr>" + "\n"
    val row23 =  "<tr>" + "<td>" + "m3u8 (ios specific video format): " + "</td>" + "<td>"  + returnPageSample(23) + "</td>" + "</tr>" + "\n"
    val row24 =  "<tr>" + "<td>" + "mp3 audio: " + "</td>" + "<td>"  + returnPageSample(24) + "</td>" + "</tr>" + "\n"
    val row25 =  "<tr>" + "<td>" + "mp4 video: " + "</td>" + "<td>"  + returnPageSample(25) + "</td>" + "</tr>" + "\n"
    val row26 =  "<tr>" + "<td>" + "parliamentLiveTv: " + "</td>" + "<td>"  + returnPageSample(26) + "</td>" + "</tr>" + "\n"
    val row27 =  "<tr>" + "<td>" + "reuters: " + "</td>" + "<td>"  + returnPageSample(27) + "</td>" + "</tr>" + "\n"
    val row28 =  "<tr>" + "<td>" + "scribd: " + "</td>" + "<td>"  + returnPageSample(28) + "</td>" + "</tr>" + "\n"
    val row29 =  "<tr>" + "<td>" + "soundCloud: " + "</td>" + "<td>"  + returnPageSample(29) + "</td>" + "</tr>" + "\n"
    val row30 =  "<tr>" + "<td>" + "spotify: " + "</td>" + "<td>"  + returnPageSample(30) + "</td>" + "</tr>" + "\n"
    val row31 =  "<tr>" + "<td>" + "twitter: " + "</td>" + "<td>"  + returnPageSample(31) + "</td>" + "</tr>" + "\n"
    val row32 =  "<tr>" + "<td>" + "uStream: " + "</td>" + "<td>"  + returnPageSample(32) + "</td>" + "</tr>" + "\n"
    val row33 =  "<tr>" + "<td>" + "vevo: " + "</td>" + "<td>"  + returnPageSample(33) + "</td>" + "</tr>" + "\n"
    val row34 =  "<tr>" + "<td>" + "video format 3gp: " + "</td>" + "<td>"  + returnPageSample(34) + "</td>" + "</tr>" + "\n"
    val row35 =  "<tr>" + "<td>" + "vimeo: " + "</td>" + "<td>"  + returnPageSample(35) + "</td>" + "</tr>" + "\n"
    val row36 =  "<tr>" + "<td>" + "vine: " + "</td>" + "<td>"  + returnPageSample(36) + "</td>" + "</tr>" + "\n"
    val row37 =  "<tr>" + "<td>" + "webp video: " + "</td>" + "<td>"  + returnPageSample(37) + "</td>" + "</tr>" + "\n"
    val row38 =  "<tr>" + "<td>" + "youTube video: " + "</td>" + "<td>"  + returnPageSample(38) + "</td>" + "</tr>" + "\n"
    val row39 =  "<tr>" + "<td>" + "unknownEmbed: " + "</td>" + "<td>"  + returnPageSample(39) + "</td>" + "</tr>" + "\n"

    row0 + row1 + row2 + row3 + row4 + row5 + row6 + row7 + row8 + row9 + row10 + row11 + row12 + row13 + row14 + row15 + row16 +
    row17 + row18 + row19 + row20 + row21 + row22 + row23 + row24 + row25 + row26 + row27 + row28 + row29 + row30 + row31 + row32 +
    row33 + row34 + row35 + row36 + row37 + row38 + row39
  }

  def returnPageSample(index: Int): String = {
    val result = if(sampleArray(index).isEmpty){
        "no sample found"
    }  else {
      "<a href=\"" + sampleArray(index).get.testUrl + "\">" + sampleArray(index).get.testUrl + "</a>"
    }
    result
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

