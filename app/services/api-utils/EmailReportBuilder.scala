package app

import services.apiutils.{PageAverageObject, PerformanceResultsObject}
import org.joda.time.DateTime


class EmailReportBuilder(average: String, warning: String, alert: String, articleResultsUrl: String, liveBlogResultsUrl: String, interactiveResultsUrl: String, frontsResultsUrl: String) {

  val averageColor = average
  val warningColor = warning
  val alertColor = alert

  val articleResultsPage: String = articleResultsUrl
  val liveBlogResultsPage: String = liveBlogResultsUrl
  val interactiveResultsPage: String = interactiveResultsUrl
  val frontsResultsPage: String = frontsResultsUrl

  val hTMLPageFooter: String = ""
  val hTMLTableFooters: String = "</table>"
  val hTMLPageFooterStart: String = "\n<p><i>Job completed at: "
  val hTMLPageFooterEnd: String = "</i></p>\n</body>\n</html>"


  //Email
  //tags with inline styles for email
  val h1EmailTag: String = "<!DOCTYPE html>\n<html style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;font-family: sans-serif;-webkit-text-size-adjust: 100%;-ms-text-size-adjust: 100%;font-size: 10px;-webkit-tap-highlight-color: rgba(0,0,0,0);\">\n<head style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;\">\n  <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7\" crossorigin=\"anonymous\" style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;\">\n</head>\n<body style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;margin: 0;font-family: &quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif;font-size: 14px;line-height: 1.42857143;color: #333;background-color: #fff;\">\n<h1 style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;margin: .67em 0;font-size: 36px;font-family: inherit;font-weight: 500;line-height: 1.1;color: inherit;margin-top: 20px;margin-bottom: 10px;\">"
  val h2EmailTag: String = "<h2 style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;orphans: 3;widows: 3;page-break-after: avoid;font-family: inherit;font-weight: 500;line-height: 1.1;color: inherit;margin-top: 20px;margin-bottom: 10px;font-size: 30px;\">"
  val pEmailTag: String = "<p style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;orphans: 3;widows: 3;margin: 0 0 10px;\">"
  val tableEmailTag: String = "<table class=\"table table-striped\" style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;border-spacing: 0;border-collapse: collapse!important;background-color: transparent;width: 100%;max-width: 100%;margin-bottom: 20px;\" border=1px>"
  val tableHeaderRowEmailTag: String = "<tr style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;page-break-inside: avoid;\">"
  val tableHeaderCellEmailTag: String = "<th style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;padding: 0;text-align: left;background-color: #fff!important;\">"
  val tableNormalRowEmailTag: String = "<tr style=\"background-color: ;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;page-break-inside: avoid;\" #d9edf7\";\">"
  val tableNormalCellEmailTag: String = "<td style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;padding: 0;background-color: #fff!important;\">"

  val aHrefEmailStyle: String = "style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;background-color: transparent;color: #337ab7;text-decoration: underline;\""

  //email elements
  val emailPageHeader: String = "<!DOCTYPE html>\n<html style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;font-family: sans-serif;-webkit-text-size-adjust: 100%;-ms-text-size-adjust: 100%;font-size: 10px;-webkit-tap-highlight-color: rgba(0,0,0,0);\">\n<head style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;\">\n  <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7\" crossorigin=\"anonymous\" style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;\">\n</head>\n<body style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;margin: 0;font-family: &quot;Helvetica Neue&quot;,Helvetica,Arial,sans-serif;font-size: 14px;line-height: 1.42857143;color: #333;background-color: #fff;\">"
  val htmlTitleEmailArticle: String = h1EmailTag + "Current Performance of today's article pages</h1>"
  val htmlTitleEmailLiveBlog: String = h1EmailTag + "Current Performance of today's liveblog pages</h1>"
  val htmlTitleEmailInteractive: String = h1EmailTag + "Current Performance of today's interactive pages</h1>"
  val htmlTitleEmailFronts: String = h1EmailTag + "Current Performance of today's fronts</h1>"
  val hTMLEmailJobStarted: String =  pEmailTag + "Job started at: " + DateTime.now + "\n</p>"
  val hTMLAlertEmailTableHeaders: String = tableEmailTag + "\n" + tableHeaderRowEmailTag + "\n" + tableHeaderCellEmailTag + "Page Url</th>\n" + tableHeaderCellEmailTag + "Test VisualsElementType</th>\n" + tableHeaderCellEmailTag + "Status</th>\n" + tableHeaderCellEmailTag + "Full results here</th>\n" + "</tr>\n"
  val hTMLEmailTableFooters: String = "</table>"
  val hTMLEmailPageFooterStart: String = "\n"+ pEmailTag +"<i>Job completed at: "
  val hTMLEmailPageFooterEnd: String = "</i></p>\n</body>\n</html>"

  def generateHTMLRow(resultsObject: PerformanceResultsObject): String = {
    var returnString: String = ""
    //  Define new web-page-test API request and sendPageWeightAlert it the url to test
    //  Add results to string which will eventually become the content of our results file

    if (resultsObject.alertStatusPageWeight) {
        println("row should be red one of the items qualifies")
        returnString = "<tr style=\"background-color:" + alertColor + ";\">" + resultsObject.toHTMLSimpleTableCells() + "</tr>"
      } else {
      println("all fields within size limits")
      returnString = "<tr>" + resultsObject.toHTMLSimpleTableCells() + "</tr>"
    }
    println(DateTime.now + " returning results string to main thread")
    println(returnString)
    returnString

  }

  def interactiveHTMLRow(resultsObject: PerformanceResultsObject): String = {
    var returnString: String = ""
    //  Define new web-page-test API request and sendPageWeightAlert it the url to test
    //  Add results to string which will eventually become the content of our results file

    if (resultsObject.alertStatusPageWeight) {
        println("row should be red one of the items qualifies")
        returnString = "<tr style=\"background-color:" + alertColor + ";\">" + resultsObject.toHTMLInteractiveTableCells() + "</tr>"
      } else {
      println("all fields within size limits")
      returnString = "<tr>" + resultsObject.toHTMLInteractiveTableCells() + "</tr>"
    }
    println(DateTime.now + " returning results string to main thread")
    println(returnString)
    returnString

  }

  def closeTable: String = {
    hTMLTableFooters
  }

  def closePage: String = {
    hTMLPageFooterStart + DateTime.now() + hTMLPageFooterEnd
  }

  def generalAlertEmailHeadings(): String = {
    val messageString: String = emailPageHeader +
      h1EmailTag + "Page performance alerts" + "</h1>"
    messageString
  }

  def interactiveAlertEmailHeadings(): String = {
    val messageString: String = emailPageHeader +
      h1EmailTag + "Interactive page performance alerts" + "</h1>\n"
    messageString
  }

  def generateArticleAlertHeadings(): String = {
    h2EmailTag + "Article page performance alerts" + "</h2>\n"
  }

  def generateArticleAlertFooter(): String = {
    pEmailTag + "All alerts have been confirmed by retesting multiple times. Tests were run without ads so all page weight is due to content" +"</p>\n" +
      pEmailTag + "Full results for article pages can be viewed <a href=" + articleResultsPage + ">here</a></p>\n"
  }


  def generateLiveBlogAlertHeadings(): String = {
    h2EmailTag + "Liveblog performance alerts" + "</h2>\n"
  }

  def generateLiveBlogAlertFooter(): String = {
    pEmailTag + "All alerts have been confirmed by retesting multiple times. Tests were run without ads so all page weight is due to content" +"</p>\n" +
      pEmailTag + "Full results for liveblog pages can be viewed <a href=" + liveBlogResultsPage + ">here</a></p>\n"
  }

  def generateInteractiveAlertHeadings(): String = {
    h2EmailTag + "Interactive page performance alerts" + "</h2>\n"
  }

  def generateInteractiveAlertFooter(): String = {
    pEmailTag + "All alerts have been confirmed by retesting multiple times. Tests were run without ads so all page weight is due to content"+"</p>\n" +
      pEmailTag + "Full results for interactive pages can be viewed <a href=" + interactiveResultsPage + ">here</a></p>\n"
  }

  def generateFrontsAlertHeadings(): String = {
    h2EmailTag + "Fronts performance alerts" + "</h2>\n"
  }

  def generateFrontsAlertFooter(): String = {
    pEmailTag + "All alerts have been confirmed by retesting multiple times. Tests were run without ads so all page weight is due to content</p>\n" +
      pEmailTag + "Full results for fronts can be viewed <a href=" + frontsResultsPage + ">here</a></p>\n"
  }



  def generateAlertEmailBodyElement(alertList: List[PerformanceResultsObject], averages: PageAverageObject): String = {
  
    if(alertList.nonEmpty) {
      val desktopMessageString: String =
        if (alertList.exists(test => test.typeOfTest == "Desktop")) {
          h2EmailTag + "Desktop Alerts</h2>" +
            pEmailTag + "The following items have been found to either take too long to load or cost too much to view on a desktop browser</p>\n" +
            //            this.hTMLAlertEmailTableHeaders + "\n" +
            (for (test <- alertList if test.typeOfTest == "Desktop") yield hTMLAlertEmailTableHeaders + tableNormalRowEmailTag + test.toHTMLAlertMessageCells() + this.hTMLTableFooters).mkString
        }
        else {
          ""
        }

      val mobileMessageString: String =
        if (alertList.exists(test => test.typeOfTest == "Android/3G")) {
          h2EmailTag + "Mobile Alerts</h2>" +
            pEmailTag + "The following items have been found to either take too long to load or cost too much to view on a mobile device</p>\n" +
            //           this.hTMLAlertEmailTableHeaders + "\n" +
            (for (test <- alertList if test.typeOfTest == "Android/3G") yield hTMLAlertEmailTableHeaders + tableNormalRowEmailTag + test.toHTMLAlertMessageCells() + this.hTMLTableFooters).mkString

        }
        else {
          ""
        }
      val returnString: String = desktopMessageString + mobileMessageString
      returnString
    } else {
      ""
    }
  }


  def generateInteractiveAlertBodyElement(alertList: List[PerformanceResultsObject], averages: PageAverageObject): String = {
    if(alertList.nonEmpty) {
      val desktopMessageString: String =
        if (alertList.exists(test => test.typeOfTest == "Desktop")) {
          h2EmailTag + "Desktop Alerts</h2>" +
            pEmailTag + "The following items have been found to either take too long to load or cost too much to view on a desktop browser</p>\n" +
            //            this.hTMLAlertEmailTableHeaders + "\n" +
            (for (test <- alertList if test.typeOfTest == "Desktop") yield hTMLAlertEmailTableHeaders + tableNormalRowEmailTag + test.toInteractiveAlertMessageCells() + this.hTMLTableFooters).mkString
        }
        else {
          ""
        }

      val mobileMessageString: String =
        if (alertList.exists(test => test.typeOfTest == "Android/3G")) {
          h2EmailTag + "Mobile Alerts</h2>" +
            pEmailTag + "The following items have been found to either take too long to load or cost too much to view on a mobile device</p>\n" +
            //            this.hTMLAlertEmailTableHeaders + "\n" +
            (for (test <- alertList if test.typeOfTest == "Android/3G") yield hTMLAlertEmailTableHeaders + tableNormalRowEmailTag + test.toInteractiveAlertMessageCells() + this.hTMLTableFooters).mkString

        }
        else {
          ""
        }
      val returnString: String = desktopMessageString + mobileMessageString
      returnString
    } else {
      ""
    }
  }


  def generalAlertFullEmailBody(articleReport: String, liveBlogReport: String, interactiveReport: String, frontsReport: String): String = {

    val articleElement: String = {
      if(articleReport.contains("<tr")){
        generateLiveBlogAlertHeadings() +
          liveBlogReport+
          generateLiveBlogAlertFooter()}
      else {
        ""
      }
    }

    val liveBlogElement: String = {
      if(liveBlogReport.contains("<tr")){
        generateLiveBlogAlertHeadings() +
          liveBlogReport+
          generateLiveBlogAlertFooter()}
      else {
        ""
      }
    }

    val frontsElement: String = {
      if(frontsReport.contains("<tr")){
        generateFrontsAlertHeadings() +
          frontsReport +
          generateFrontsAlertFooter()}
      else {
        ""
      }
    }


    val fullEmailBody: String = {
      generalAlertEmailHeadings +
        articleElement +
        liveBlogElement +
        frontsElement +
        closePage
    }
    fullEmailBody
  }

  def interactiveAlertFullEmailBody(interactiveReport: String): String = {

    val interactiveElement: String = {
      if(interactiveReport.contains("<tr")){
        generateInteractiveAlertHeadings() +
          interactiveReport +
          generateInteractiveAlertFooter()}
      else {
        ""
      }
    }

    val fullEmailBody: String = {
      generalAlertEmailHeadings +
        interactiveElement +
        closePage
    }
    fullEmailBody
  }



}
