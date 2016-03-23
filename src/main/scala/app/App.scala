package app

// note an _ instead of {} would get everything

import java.io._
import java.util

import app.api.{WptResultPageListener, S3Operations}
import app.apiutils._
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime
import sbt.complete.Completion

import scala.collection.parallel.immutable.ParSeq
import scala.io.Source


object App {
  def main(args: Array[String]) {
    /*  This value stops the forces the config to be read and the output file to be written locally rather than reading and writing from/to S3
    #####################    this should be set to false before merging!!!!################*/
    val iamTestingLocally = true
    /*#####################################################################################*/
    println("Job started at: " + DateTime.now)
    println("Local Testing Flag is set to: " + iamTestingLocally.toString)

    //  Define names of s3bucket, configuration and output Files
    val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
    val s3BucketName = "capi-wpt-querybot"
    val configFileName = "config.conf"
    val emailFileName = "addresses.conf"
    val interactiveSampleFileName = "interactivesamples.conf"

    val outputFileName = "liveBlogPerformanceData.html"
    val simpleOutputFileName = "liveBlogPerformanceDataExpurgated.html"
    val interactiveOutputFilename = "interactivePerformanceData.html"
    val videoOutputFilename =  "videoPerformanceData.html"
    val audioOutputFilename = "audioPerformanceData.html"
    val frontsOutputFilename = "frontsData.html"

    val liveBlogResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + simpleOutputFileName
    val interactiveResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + interactiveOutputFilename
    val frontsResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + frontsOutputFilename

    val articleCSVName = "accumulatedArticlePerformanceData.csv"
    val liveBlogCSVName = "accumulatedLiveblogPerformanceData.csv"
    val interactiveCSVName = "accumulatedInteractivePerformanceData.csv"
    val videoCSVName = "accumulatedVideoPerformanceData"
    val audioCSVName = "accumulatedAudioPerformanceData"
    val frontsCSVName = "accumulatedFrontsPerformanceData"



    //Define colors to be used for average values, warnings and alerts
    val averageColor: String = "\"#d9edf7\""
    val warningColor: String = "\"#fcf8e3\""
    val alertColor: String = "\"#f2dede\""

    //  Initialize results string - this will be used to accumulate the results from each test so that only one write to file is needed.
    val htmlString = new HtmlStringOperations(averageColor, warningColor, alertColor, liveBlogResultsUrl, interactiveResultsUrl, frontsResultsUrl)
    var liveBlogResults: String = htmlString.initialisePageForLiveblog + htmlString.interactiveTable
    var interactiveResults: String = htmlString.initialisePageForInteractive + htmlString.initialiseTable
    var frontsResults: String = htmlString.initialisePageForFronts + htmlString.initialiseTable

    //Initialize email alerts string - this will be used to generate emails
    var liveBlogAlertList: List[PerformanceResultsObject] = List()
    var interactiveAlertList: List[PerformanceResultsObject] = List()
    var frontsAlertList: List[PerformanceResultsObject] = List()


    var liveBlogAlertMessageBody: String = ""
    var interactiveAlertMessageBody: String = ""
    var frontsAlertMessageBody: String = ""
    
    val interactiveItemLabel: String = "Interactive"


    // var articleCSVResults: String = ""
    //  var liveBlogCSVResults: String = ""
    // var interactiveCSVResults: String = ""
    //  var videoCSVResults: String = ""
    //  var audioCSVResults: String = ""
    //  var frontsCSVResults: String = ""

    //Create new S3 Client
    println("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
    val s3Interface = new S3Operations(s3BucketName, configFileName, emailFileName)
    var configArray: Array[String] = Array("", "", "", "", "", "")

    //Get config settings
    println("Extracting configuration values")
    if (!iamTestingLocally) {
      println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
      configArray = s3Interface.getConfig
    }
    else {
      println(DateTime.now + " retrieving local config file: " + configFileName)
      val configReader = new LocalFileOperations
      configArray = configReader.readInConfig(configFileName)
    }
    println("checking validity of config values")
    if ((configArray(0).length < 1) || (configArray(1).length < 1) || (configArray(2).length < 1) || (configArray(3).length < 1)) {
      println("problem extracting config\n" +
        "contentApiKey length: " + configArray(0).length + "\n" +
        "wptBaseUrl length: " + configArray(1).length + "\n" +
        "wptApiKey length: " + configArray(2).length + "\n" +
        "wptLocation length: " + configArray(3).length + "\n" +
        "emailUsername length: " + configArray(4).length + "\n" +
        "emailPassword length: " + configArray(5).length)
      System exit 1
    }
    println("config values ok")
    val contentApiKey: String = configArray(0)
    val wptBaseUrl: String = configArray(1)
    val wptApiKey: String = configArray(2)
    val wptLocation: String = configArray(3)
    val emailUsername: String = configArray(4)
    val emailPassword: String = configArray(5)

    //obtain list of email addresses for alerting
    val emailAddresses: Array[List[String]] = s3Interface.getEmailAddresses
    val generalAlertsAddressList: List[String] = emailAddresses(0)
    val interactiveAlertsAddressList: List[String] = emailAddresses(1)

    //obtain list of interactive samples to determine average size
    val listofLargeInteractives: List[String] = s3Interface.getUrls(interactiveSampleFileName)


    //Create Email Handler class
    val emailer: EmailOperations = new EmailOperations(emailUsername, emailPassword)

    //  Define new CAPI Query object
    val capiQuery = new ArticleUrls(contentApiKey)
    //get all content-type-lists
    val liveBlogUrls: List[String] = capiQuery.getMinByMinUrls
    val interactiveUrls: List[String] = capiQuery.getInteractiveUrls
    val listofFronts: List[String] = capiQuery.getFrontsUrls
    println(DateTime.now + " Closing Content API query connection")
    capiQuery.shutDown


    // send all urls to webpagetest at once to enable parallel testing by test agents
    val urlsToSend: List[String] = (liveBlogUrls ::: interactiveUrls ::: listofFronts).distinct
    println("Combined list of urls: \n" + urlsToSend)
    val resultUrlList: List[(String, String)] = getResultPages(urlsToSend, wptBaseUrl, wptApiKey, wptLocation)


    if (liveBlogUrls.nonEmpty) {
      println("Generating average values for liveblogs")
      val liveBlogAverages: PageAverageObject = new LiveBlogDefaultAverages(averageColor)
      liveBlogResults = liveBlogResults.concat(liveBlogAverages.toHTMLString)
      val liveBlogResultsList = listenForResultPages(liveBlogUrls, resultUrlList, liveBlogAverages, wptBaseUrl, wptApiKey, wptLocation)
      val liveBlogHTMLResults: List[String] = liveBlogResultsList.map(x => htmlString.generateHTMLRow(x))
      // write liveblog results to string
      //Create a list of alerting pages and write to string
      liveBlogAlertList = for (result <- liveBlogResultsList if result.alertStatus) yield result
      liveBlogAlertMessageBody = htmlString.generateAlertEmailBodyElement(liveBlogAlertList, liveBlogAverages)

      liveBlogResults = liveBlogResults.concat(liveBlogHTMLResults.mkString)
      liveBlogResults = liveBlogResults + htmlString.closeTable + htmlString.closePage
      //write liveblog results to file
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing liveblog results to S3")
        s3Interface.writeFileToS3(simpleOutputFileName, liveBlogResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(simpleOutputFileName, liveBlogResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
      println("LiveBlog Performance Test Complete")

    } else {
      println("CAPI query found no liveblogs")
    }

    if (interactiveUrls.nonEmpty) {
      println("Generating average values for interactives")
      val interactiveAverages: PageAverageObject = generatePageAverages(listofLargeInteractives, wptBaseUrl, wptApiKey, wptLocation, interactiveItemLabel, averageColor)
      interactiveResults = interactiveResults.concat(interactiveAverages.toHTMLString)

      val interactiveResultsList = listenForResultPages(interactiveUrls, resultUrlList, interactiveAverages, wptBaseUrl, wptApiKey, wptLocation)
      val interactiveHTMLResults: List[String] = interactiveResultsList.map(x => htmlString.interactiveHTMLRow(x))
      //generate interactive alert message body
      interactiveAlertList = for (result <- interactiveResultsList if result.alertStatus) yield result
      interactiveAlertMessageBody = htmlString.generateAlertEmailBodyElement(interactiveAlertList, interactiveAverages)
      // write interactive results to string
      interactiveResults = interactiveResults.concat(interactiveHTMLResults.mkString)
      interactiveResults = interactiveResults + htmlString.closeTable + htmlString.closePage
      //write interactive results to file
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing liveblog results to S3")
        s3Interface.writeFileToS3(interactiveOutputFilename, interactiveResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(interactiveOutputFilename, interactiveResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
      println("Interactive Performance Test Complete")

    } else {
      println("CAPI query found no interactives")
    }

    if (listofFronts.nonEmpty) {
      println("Generating average values for fronts")
      val frontsAverages: PageAverageObject = new FrontsDefaultAverages
      frontsResults = frontsResults.concat(frontsAverages.toHTMLString)

      val frontsResultsList = listenForResultPages(listofFronts, resultUrlList, frontsAverages, wptBaseUrl, wptApiKey, wptLocation)
      val frontsHTMLResults: List[String] = frontsResultsList.map(x => htmlString.generateHTMLRow(x))
      //Create a list of alerting pages and write to string
      frontsAlertList = for (result <- frontsResultsList if result.alertStatus) yield result
      frontsAlertMessageBody = htmlString.generateAlertEmailBodyElement(frontsAlertList, frontsAverages)
      // write fronts results to string
      frontsResults = frontsResults.concat(frontsHTMLResults.mkString)
      frontsResults = frontsResults + htmlString.closeTable + htmlString.closePage
      //write fronts results to file
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing liveblog results to S3")
        s3Interface.writeFileToS3(frontsOutputFilename, frontsResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(frontsOutputFilename, frontsResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
      println("Fronts Performance Test Complete")

    } else {
      println("CAPI query found no Fronts")
    }

    if (liveBlogAlertList.nonEmpty || frontsAlertList.nonEmpty) {
      println("\n\n liveBlogAlertList contains: " + liveBlogAlertList.length + " pages")
      println("\n\n frontsAlertList contains: " + frontsAlertList.length + " pages")
      println("\n\n ***** \n\n" + "liveblog Alert body:\n" + liveBlogAlertMessageBody)
       println("\n\n ***** \n\n" + "fronts Alert Body:\n" + frontsAlertMessageBody)
      println("\n\n ***** \n\n" + "Full email Body:\n" + htmlString.generalAlertFullEmailBody(liveBlogAlertMessageBody, interactiveAlertMessageBody, frontsAlertMessageBody))
      println("compiling and sending email")
      val emailSuccess = emailer.send(generalAlertsAddressList, htmlString.generalAlertFullEmailBody(liveBlogAlertMessageBody, interactiveAlertMessageBody,  frontsAlertMessageBody))
      if (emailSuccess)
        println(DateTime.now + " General Alert Emails sent successfully. ")
      else
        println(DateTime.now + "ERROR: Job completed, but sending of general Alert Emails failed")
    } else {
      println("No pages to alert on. Email not sent. \n Job complete")
    }

    if (interactiveAlertList.nonEmpty) {
      println("\n\n interactiveAlertList contains: " + interactiveAlertList.length + " pages")
      println("\n\n ***** \n\n" + "interactive Alert Body:\n" + interactiveAlertMessageBody)
      println("\n\n ***** \n\n" + "Full interactive email Body:\n" + htmlString.interactiveAlertFullEmailBody(interactiveAlertMessageBody))
      println("compiling and sending email")
      val emailSuccess = emailer.send(interactiveAlertsAddressList, htmlString.interactiveAlertFullEmailBody(interactiveAlertMessageBody))
      if (emailSuccess)
        println(DateTime.now + " Interactive Emails sent successfully. \n Job complete")
      else
        println(DateTime.now + "ERROR: Job completed, but sending of Interactve Emails failed")
    } else {
      println("No pages to alert on. Email not sent. \n Job complete")
    }

  }


  def getResultPages(urlList: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String): List[(String,String)] = {
    val wpt: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    val desktopResults: List[(String, String)] = urlList.map(page => { (page, wpt.sendPage(page)) })
    val mobileResults: List[(String, String)] = urlList.map(page => { (page, wpt.sendMobile3GPage(page, wptLocation)) })
    desktopResults ::: mobileResults
  }

  def listenForResultPages(capiUrls: List[String], resultUrlList: List[(String, String)], averages: PageAverageObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String): List[PerformanceResultsObject] = {
    println("ListenForResultPages called with: \n\n" +
      " List of Urls: \n" + capiUrls.mkString +
      "\n\nList of WebPage Test results: \n" + resultUrlList.mkString +
      "\n\nList of averages: \n" + averages.toHTMLString + "\n")

    val listenerList: List[WptResultPageListener] = capiUrls.flatMap(url => {
      for (element <- resultUrlList if element._1 == url) yield new WptResultPageListener(element._1, "LiveBlog", element._2)
    })

    println("Listener List created: \n" + listenerList.map(element => "list element: \n"+ "url: " + element.pageUrl + "\n" + "resulturl" + element.wptResultUrl + "\n"))

    val liveBlogResultsList: ParSeq[WptResultPageListener] = listenerList.par.map(element => {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey)
      val newElement = new WptResultPageListener(element.pageUrl, element.pageType, element.wptResultUrl)
      newElement.testResults = wpt.getResults(newElement.wptResultUrl)
      newElement
    })
    val testResults = liveBlogResultsList.map(element => element.testResults).toList
    val resultsWithAlerts: List[PerformanceResultsObject] = testResults.map(element => setAlertStatus(element, averages))

    //Confirm alert status by retesting alerting urls
    println("Confirming any items that have an alert")
    val confirmedTestResults = resultsWithAlerts.map(x => {
      if(x.alertStatus)
        confirmAlert(x, averages, wptBaseUrl, wptApiKey, wptLocation)
      else
        x
    })
    confirmedTestResults
  }

  def confirmAlert(initialResult: PerformanceResultsObject, averages: PageAverageObject,wptBaseUrl: String, wptApiKey: String, wptLocation: String): PerformanceResultsObject ={
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    val testCount: Int = if(initialResult.timeToFirstByte > 1000) {5} else {3}
    println("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
    val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatus(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    AlertConfirmationTestResult
  }

  def setAlertStatus(resultObject: PerformanceResultsObject, averages: PageAverageObject): PerformanceResultsObject ={
    //  Add results to string which will eventually become the content of our results file
    if(resultObject.typeOfTest == "Desktop") {
      if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs80thPercentile) ||
        (resultObject.speedIndex >= averages.desktopSpeedIndex80thPercentile) ||
        (resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded80thPercentile) ||
        (resultObject.estUSPrePaidCost >= averages.desktopEstUSPrePaidCost80thPercentile) ||
        (resultObject.estUSPostPaidCost >= averages.desktopEstUSPostPaidCost80thPercentile)) {
        if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) ||
          (resultObject.speedIndex >= averages.desktopSpeedIndex) ||
          (resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded)) {
          println("row should be red one of the items qualifies")
          if(resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) {resultObject.alertDescription = "<p>Page takes " + resultObject.timeFirstPaintInSec + "s" + " for text to load and page to become scrollable. Should only take " + averages.desktopTimeFirstPaintInSeconds + "s.</p>"}
          if(resultObject.speedIndex >= averages.desktopSpeedIndex) {resultObject.alertDescription = "<p>Page takes " + averages.desktopAboveTheFoldCompleteInSec + "To render visible images etc. It should take " + averages.desktopAboveTheFoldCompleteInSec + "s.</P>"}
          if(resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded) {resultObject.alertDescription = resultObject.alertDescription +  "<p>Page is too heavy. Size is: " + resultObject.kBInFullyLoaded + "KB. It should be less than: " + averages.desktopKBInFullyLoaded + "KB.</p>"}
          println(resultObject.alertDescription)
          resultObject.warningStatus = true
          resultObject.alertStatus = true
        }
        else {
          println("row should be yellow one of the items qualifies")
          resultObject.warningStatus = true
          resultObject.alertStatus = false
        }
      }
      else {
        println("all fields within size limits")
        resultObject.warningStatus = false
        resultObject.alertStatus = false
      }
    } else {
      //checking if status of mobile test needs an alert
      if ((resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs80thPercentile) ||
        (resultObject.speedIndex >= averages.mobileSpeedIndex80thPercentile) ||
        (resultObject.kBInFullyLoaded >= averages.mobileKBInFullyLoaded80thPercentile) ||
        (resultObject.estUSPrePaidCost >= averages.mobileEstUSPrePaidCost80thPercentile) ||
        (resultObject.estUSPostPaidCost >= averages.mobileEstUSPostPaidCost80thPercentile)) {
        if ((resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) ||
          (resultObject.speedIndex >= averages.mobileSpeedIndex) ||
          (resultObject.kBInFullyLoaded >= averages.mobileKBInFullyLoaded)){
          println("warning and alert statuses set to true")
          if(resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) {resultObject.alertDescription = "<p>Page takes " + resultObject.timeFirstPaintInSec + "s" + " for text to load and page to become scrollable. Should only take " + averages.mobileTimeFirstPaintInSeconds + "s.</p>"}
          if(resultObject.speedIndex >= averages.mobileSpeedIndex) {resultObject.alertDescription = "<p>Page takes " + averages.mobileAboveTheFoldCompleteInSec + "To render visible images etc. It should take " + averages.mobileAboveTheFoldCompleteInSec + "s or less.</p>"}
          if(resultObject.kBInFullyLoaded >= averages.mobileKBInFullyLoaded) {resultObject.alertDescription = resultObject.alertDescription +  "<p>Page is too heavy. Size is: " + resultObject.kBInFullyLoaded + "KB. It should be less than: " + averages.mobileKBInFullyLoaded + "KB.</p>"}
          resultObject.warningStatus = true
          resultObject.alertStatus = true
        }
        else {
          println("warning status set to true")
          resultObject.warningStatus = true
          resultObject.alertStatus = false
        }
      }
      else {
        println("all fields within size limits - both warning and alert status set to false")
        resultObject.warningStatus = false
        resultObject.alertStatus = false
      }
    }
    println("Returning test result with alert flags set to relevant values")
    resultObject
  }

  def generatePageAverages(urlList: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String, itemtype: String, averageColor: String): PageAverageObject = {
    val setHighPriority: Boolean = true
    val webpageTest: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey)

    val resultsList: List[Array[PerformanceResultsObject]] = urlList.map(url => {
      val webPageDesktopTestResults: PerformanceResultsObject = webpageTest.desktopChromeCableTest(url, setHighPriority)
      val webPageMobileTestResults: PerformanceResultsObject = webpageTest.mobileChrome3GTest(url, wptLocation, setHighPriority)
      val combinedResults = Array(webPageDesktopTestResults, webPageMobileTestResults)
      combinedResults
    })

    val pageAverages: PageAverageObject = new GeneratedInteractiveAverages(resultsList, averageColor)
    pageAverages
  }
  

  def retestUrl(initialResult: PerformanceResultsObject,wptBaseUrl: String, wptApiKey: String, wptLocation: String): PerformanceResultsObject ={
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    val testCount: Int = if(initialResult.timeToFirstByte > 1000) {5} else {3}
    println("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
 //   val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatus(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount)
  }


}


