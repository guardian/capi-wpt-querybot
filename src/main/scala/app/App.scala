package app

// note an _ instead of {} would get everything

import java.io._
import java.util

import app.api._
import app.apiutils._
import com.gu.contentapi.client.model.v1.ContentFields
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime
import play.api.libs.json.{Json, JsObject}
import play.libs.Json
import sbt.complete.Completion

import scala.collection.parallel.immutable.ParSeq
import scala.io.Source


object App {
  def main(args: Array[String]) {
    /*  This value stops the forces the config to be read and the output file to be written locally rather than reading and writing from/to S3
    #####################    this should be set to false before merging!!!!################*/
    val iamTestingLocally = false
    /*#####################################################################################*/
    println("Job started at: " + DateTime.now)
    println("Local Testing Flag is set to: " + iamTestingLocally.toString)

    //  Define names of s3bucket, configuration and output Files
    val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
    val s3BucketName = "capi-wpt-querybot"
    val configFileName = "config.conf"
    val emailFileName = "addresses.conf"
    val interactiveSampleFileName = "interactivesamples.conf"

    val articleOutputFilename = "articleperformancedata.html"
    val liveBlogOutputFilename = "liveblogperformancedata.html"
    val interactiveOutputFilename = "interactiveperformancedata.html"
    val videoOutputFilename = "videoperformancedata.html"
    val audioOutputFilename = "audioperformancedata.html"
    val frontsOutputFilename = "frontsperformancedata.html"
    val combinedOutputFilename = "combinedperformancedata.html"
    val combinedDesktopFilename = "combineddesktopperformancedata.html"
    val combinedMobileFilename = "combinedmobileperformancedata.html"
    val editorialPageweightFilename = "editorialpageweightdashboard.html"
    val editorialCombinedPageweightFilename = "editorialpageweightdashboardcombined.html"
    val editorialDesktopPageweightFilename = "editorialpageweightdashboarddesktop.html"
    val editorialMobilePageweightFilename = "editorialpageweightdashboardmobile.html"

    val articleResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + articleOutputFilename
    val liveBlogResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + liveBlogOutputFilename
    val interactiveResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + interactiveOutputFilename
    val frontsResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + frontsOutputFilename

    val articleCSVName = "accumulatedArticlePerformanceData.csv"
    val liveBlogCSVName = "accumulatedLiveblogPerformanceData.csv"
    val interactiveCSVName = "accumulatedInteractivePerformanceData.csv"
    val videoCSVName = "accumulatedVideoPerformanceData"
    val audioCSVName = "accumulatedAudioPerformanceData"
    val frontsCSVName = "accumulatedFrontsPerformanceData"



    //Define colors to be used for average values, warnings and alerts
    val averageColor: String = "#d9edf7"
    //    val warningColor: String = "#fcf8e3"
    val warningColor: String = "rgba(227, 251, 29, 0.32)"
    val alertColor: String = "#f2dede"

    //initialize combinedResultsList - this will be accumulate test results for the combined page
    var combinedResultsList: List[PerformanceResultsObject] = List()

    //  Initialize results string - this will be used to accumulate the results from each test so that only one write to file is needed.
    val htmlString = new HtmlStringOperations(averageColor, warningColor, alertColor, articleResultsUrl, liveBlogResultsUrl, interactiveResultsUrl, frontsResultsUrl)
    val newhtmlString = new HtmlReportBuilder(averageColor, warningColor, alertColor, articleResultsUrl, liveBlogResultsUrl, interactiveResultsUrl, frontsResultsUrl)
    var articleResults: String = htmlString.initialisePageForLiveblog + htmlString.initialiseTable
    var liveBlogResults: String = htmlString.initialisePageForLiveblog + htmlString.initialiseTable
    var interactiveResults: String = htmlString.initialisePageForInteractive + htmlString.interactiveTable
    var frontsResults: String = htmlString.initialisePageForFronts + htmlString.initialiseTable
    var audioResults: String = htmlString.initialisePageForLiveblog + htmlString.initialiseTable
    var videoResults: String = htmlString.initialisePageForLiveblog + htmlString.initialiseTable

    //Initialize email alerts string - this will be used to generate emails
    var articleAlertList: List[PerformanceResultsObject] = List()
    var liveBlogAlertList: List[PerformanceResultsObject] = List()
    var interactiveAlertList: List[PerformanceResultsObject] = List()
    var frontsAlertList: List[PerformanceResultsObject] = List()
    var audioAlertList: List[PerformanceResultsObject] = List()
    var videoAlertList: List[PerformanceResultsObject] = List()

    var articleAlertMessageBody: String = ""
    var liveBlogAlertMessageBody: String = ""
    var interactiveAlertMessageBody: String = ""
    var frontsAlertMessageBody: String = ""
    var audioAlertMessageBody: String = ""
    var videoAlertMessageBody: String = ""

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
        "emailPassword length: " + configArray(5).length) + "\n" +
        "visuals URL length: " + configArray(6).length

      System exit 1
    }
    println("config values ok")
    val contentApiKey: String = configArray(0)
    val wptBaseUrl: String = configArray(1)
    val wptApiKey: String = configArray(2)
    val wptLocation: String = configArray(3)
    val emailUsername: String = configArray(4)
    val emailPassword: String = configArray(5)
    val visualsApiUrl: String = configArray(6)

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
    val articles: List[(Option[ContentFields],String)] = capiQuery.getArticles
    val liveBlogs: List[(Option[ContentFields],String)] = capiQuery.getMinByMins
    val interactives: List[(Option[ContentFields],String)] = capiQuery.getInteractives
    val fronts:  List[(Option[ContentFields],String)] = capiQuery.getFronts
    val videoPages: List[(Option[ContentFields],String)] = capiQuery.getVideoPages
    val audioPages: List[(Option[ContentFields],String)] = capiQuery.getAudioPages
    println(DateTime.now + " Closing Content API query connection")
    capiQuery.shutDown

    val articleUrls: List[String] = for (page <- articles) yield page._2
    val liveBlogUrls: List[String] = for (page <- liveBlogs) yield page._2
    val interactiveUrls: List[String] = for (page <- interactives) yield page._2
    val frontsUrls: List[String] = for (page <- fronts) yield page._2
    val videoUrls: List[String] = for (page <- videoPages) yield page._2
    val audioUrls: List[String] = for (page <- audioPages) yield page._2

    //get all pages from the visuals team api

    val visualsApi = new VisualsApiInterface(visualsApiUrl)
    val urls = visualsApi.getTodaysPages()
    println("\n\n\n\n returned URLs: \n" + urls)


    // send all urls to webpagetest at once to enable parallel testing by test agents
    //    val urlsToSend: List[String] = (articleUrls:::liveBlogUrls ::: interactiveUrls ::: frontsUrls ::: videoUrls ::: audioUrls).distinct
    val urlsToSend: List[String] = (articleUrls ::: liveBlogUrls ::: interactiveUrls ::: frontsUrls).distinct
    println("Combined list of urls: \n" + urlsToSend)
    val resultUrlList: List[(String, String)] = getResultPages(urlsToSend, wptBaseUrl, wptApiKey, wptLocation)


    if (articleUrls.nonEmpty) {
      println("Generating average values for articles")
      val articleAverages: PageAverageObject = new LiveBlogDefaultAverages(averageColor)
      articleResults = articleResults.concat(articleAverages.toHTMLString)

      val articleResultsList = listenForResultPages(articles, "article", resultUrlList, articleAverages, wptBaseUrl, wptApiKey, wptLocation)
      combinedResultsList = combinedResultsList ::: articleResultsList
      println("About to sort article results list. Length of list is: " + articleResultsList.length)
      val sortedArticleResultsList = orderList(articleResultsList)
      if(sortedArticleResultsList.isEmpty) {
        println("Sorting algorithm for articles has returned empty list. Aborting")
        System exit 1
      }

      val articleHTMLResults: List[String] = sortedArticleResultsList.map(x => htmlString.generateHTMLRow(x))
      // write article results to string
      //Create a list of alerting pages and write to string
      articleAlertList = for (result <- sortedArticleResultsList if result.alertStatus) yield result
      articleAlertMessageBody = htmlString.generateAlertEmailBodyElement(articleAlertList, articleAverages)

      articleResults = articleResults.concat(articleHTMLResults.mkString)
      articleResults = articleResults + htmlString.closeTable + htmlString.closePage
      //write article results to file
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing article results to S3")
        s3Interface.writeFileToS3(articleOutputFilename, articleResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(articleOutputFilename, articleResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
      println("Article Performance Test Complete")

    } else {
      println("CAPI query found no article pages")
    }


    if (liveBlogUrls.nonEmpty) {
      println("Generating average values for liveblogs")
      val liveBlogAverages: PageAverageObject = new LiveBlogDefaultAverages(averageColor)
      liveBlogResults = liveBlogResults.concat(liveBlogAverages.toHTMLString)

      val liveBlogResultsList = listenForResultPages(liveBlogs, "liveBlog", resultUrlList, liveBlogAverages, wptBaseUrl, wptApiKey, wptLocation)
      combinedResultsList = combinedResultsList ::: liveBlogResultsList
      val sortedLiveBlogResultsList = orderList(liveBlogResultsList)
      if(sortedLiveBlogResultsList.isEmpty) {
        println("Sorting algorithm for Liveblogs has returned empty list. Aborting")
        System exit 1
      }
      val liveBlogHTMLResults: List[String] = sortedLiveBlogResultsList.map(x => htmlString.generateHTMLRow(x))
      // write liveblog results to string
      //Create a list of alerting pages and write to string
      liveBlogAlertList = for (result <- sortedLiveBlogResultsList if result.alertStatus) yield result
      liveBlogAlertMessageBody = htmlString.generateAlertEmailBodyElement(liveBlogAlertList, liveBlogAverages)

      liveBlogResults = liveBlogResults.concat(liveBlogHTMLResults.mkString)
      liveBlogResults = liveBlogResults + htmlString.closeTable + htmlString.closePage
      //write liveblog results to file
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing liveblog results to S3")
        s3Interface.writeFileToS3(liveBlogOutputFilename, liveBlogResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(liveBlogOutputFilename, liveBlogResults)
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
      val interactiveAverages: PageAverageObject = generateInteractiveAverages(listofLargeInteractives, wptBaseUrl, wptApiKey, wptLocation, interactiveItemLabel, averageColor)
      interactiveResults = interactiveResults.concat(interactiveAverages.toHTMLString)

      val interactiveResultsList = listenForResultPages(interactives, "interactive", resultUrlList, interactiveAverages, wptBaseUrl, wptApiKey, wptLocation)
      combinedResultsList = combinedResultsList ::: interactiveResultsList
      val sortedInteractiveResultsList = orderList(interactiveResultsList)
      if(sortedInteractiveResultsList.isEmpty) {
        println("Sorting algorithm has returned empty list. Aborting")
        System exit 1
      }

      val interactiveHTMLResults: List[String] = sortedInteractiveResultsList.map(x => htmlString.interactiveHTMLRow(x))
      //generate interactive alert message body
      interactiveAlertList = for (result <- sortedInteractiveResultsList if result.alertStatus) yield result
      interactiveAlertMessageBody = htmlString.generateInteractiveAlertBodyElement(interactiveAlertList, interactiveAverages)
      // write interactive results to string
      interactiveResults = interactiveResults.concat(interactiveHTMLResults.mkString)
      interactiveResults = interactiveResults + htmlString.closeTable + htmlString.closePage
      //write interactive results to file
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing interactive results to S3")
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

    if (frontsUrls.nonEmpty) {
      println("Generating average values for fronts")
      val frontsAverages: PageAverageObject = new FrontsDefaultAverages(averageColor)
      frontsResults = frontsResults.concat(frontsAverages.toHTMLString)

      val frontsResultsList = listenForResultPages(fronts, "front", resultUrlList, frontsAverages, wptBaseUrl, wptApiKey, wptLocation)
      combinedResultsList = combinedResultsList ::: frontsResultsList
      val sortedFrontsResultsList = orderList(frontsResultsList)
      if(sortedFrontsResultsList.isEmpty) {
        println("Sorting algorithm for fronts has returned empty list. Aborting")
        System exit 1
      }
      val frontsHTMLResults: List[String] = sortedFrontsResultsList.map(x => htmlString.generateHTMLRow(x))
      //Create a list of alerting pages and write to string
      frontsAlertList = for (result <- sortedFrontsResultsList if result.alertStatus) yield result
      frontsAlertMessageBody = htmlString.generateAlertEmailBodyElement(frontsAlertList, frontsAverages)
      // write fronts results to string
      frontsResults = frontsResults.concat(frontsHTMLResults.mkString)
      frontsResults = frontsResults + htmlString.closeTable + htmlString.closePage
      //write fronts results to file
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing fronts results to S3")
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

    if(combinedResultsList.nonEmpty) {
      val sortedCombinedResults: List[PerformanceResultsObject] = orderList(combinedResultsList)
      val combinedDesktopResultsList: List[PerformanceResultsObject] = for (result <- combinedResultsList if result.typeOfTest.contains("Desktop")) yield result
      val sortedCombinedDesktopResults: List[PerformanceResultsObject] = sortHomogenousResults(combinedDesktopResultsList)
      val combinedMobileResultsList: List[PerformanceResultsObject] = for (result <- combinedResultsList if result.typeOfTest.contains("Android/3G")) yield result
      val sortedCombinedMobileResults: List[PerformanceResultsObject] = sortHomogenousResults(combinedMobileResultsList)

      val combinedHTMLResults: List[String] = sortedCombinedResults.map(x => htmlString.generateHTMLRow(x))
      val combinedDesktopHTMLResults: List[String] = sortedCombinedDesktopResults.map(x => htmlString.generateHTMLRow(x))
      val combinedMobileHTMLResults: List[String] = sortedCombinedMobileResults.map(x => htmlString.generateHTMLRow(x))

      val combinedBasicHTMLResults: List[String] = sortedCombinedResults.map(x => htmlString.generatePageWeightDashboardHTMLRow(x))
      val combinedBasicDesktopHTMLResults: List[String] = sortedCombinedDesktopResults.map(x => htmlString.generatePageWeightDashboardHTMLRow(x))
      val combinedBasicMobileHTMLResults: List[String] = sortedCombinedMobileResults.map(x => htmlString.generatePageWeightDashboardHTMLRow(x))

      val combinedResults: String = htmlString.initialisePageForCombined +
        htmlString.initialiseTable +
        combinedHTMLResults.mkString +
        htmlString.closeTable + htmlString.closePage

      val combinedDesktopResults: String = htmlString.initialisePageForCombined +
        htmlString.initialiseTable +
        combinedDesktopHTMLResults.mkString +
        htmlString.closeTable + htmlString.closePage

      val combinedMobileResults: String = htmlString.initialisePageForCombined +
        htmlString.initialiseTable +
        combinedMobileHTMLResults.mkString +
        htmlString.closeTable + htmlString.closePage

      val combinedJsonList: JsObject = Json.obj("PageResults" -> Json.arr(for (result <- sortedCombinedResults) yield result.toJson()))

//      val editorialPageWeightDashboard: String = newhtmlString.generateHTMLPage(sortedCombinedResults)
      val editorialPageWeightDashboardCombined = new PageWeightDashboardCombined(sortedCombinedResults)
//      val editorialPageWeightDashboardDesktop: String = newhtmlString.generateHTMLPage(sortedCombinedDesktopResults)
      val editorialPageWeightDashboardDesktop = new PageWeightDashboardDesktop(sortedCombinedDesktopResults)
//      val editorialPageWeightDashboardMobile: String = newhtmlString.generateHTMLPage(sortedCombinedMobileResults)
      val editorialPageWeightDashboardMobile = new PageWeightDashboardMobile(sortedCombinedMobileResults)

      val editorialPageWeightDashboard = new PageWeightDashboardTabbed(sortedCombinedResults, sortedCombinedDesktopResults, sortedCombinedMobileResults)
      //write fronts results to file
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing liveblog results to S3")
        s3Interface.writeFileToS3(combinedOutputFilename, combinedResults)
        s3Interface.writeFileToS3(combinedDesktopFilename, combinedDesktopResults)
        s3Interface.writeFileToS3(combinedMobileFilename, combinedMobileResults)

        s3Interface.writeFileToS3(editorialCombinedPageweightFilename, editorialPageWeightDashboardCombined.toString())
        s3Interface.writeFileToS3(editorialDesktopPageweightFilename, editorialPageWeightDashboardDesktop.toString())
        s3Interface.writeFileToS3(editorialMobilePageweightFilename, editorialPageWeightDashboardMobile.toString())
        s3Interface.writeFileToS3(editorialPageweightFilename, editorialPageWeightDashboard.toString())



      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccessCombined: Int = outputWriter.writeLocalResultFile(combinedOutputFilename, combinedResults)
        if (writeSuccessCombined != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
        val writeSuccessCMobile: Int = outputWriter.writeLocalResultFile(combinedDesktopFilename, combinedDesktopResults)
        if (writeSuccessCMobile != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
        val writeSuccessCDesktop: Int = outputWriter.writeLocalResultFile(combinedMobileFilename, combinedMobileResults)
        if (writeSuccessCDesktop != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
        val writeSuccessPWDC: Int = outputWriter.writeLocalResultFile(editorialPageweightFilename, editorialPageWeightDashboard.toString())
        if (writeSuccessPWDC != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
        val writeSuccessPWDD: Int = outputWriter.writeLocalResultFile(editorialDesktopPageweightFilename, editorialPageWeightDashboardDesktop.toString())
        if (writeSuccessPWDD != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
        val writeSuccessPWDM: Int = outputWriter.writeLocalResultFile(editorialMobilePageweightFilename, editorialPageWeightDashboardMobile.toString())
        if (writeSuccessPWDM != 0) {
          println("problem writing local outputfile")
          System exit 1
        }

      }
    }


    if (articleAlertList.nonEmpty || liveBlogAlertList.nonEmpty || frontsAlertList.nonEmpty) {
      println("\n\n articleAlertList contains: " + articleAlertList.length + " pages")
      println("\n\n liveBlogAlertList contains: " + liveBlogAlertList.length + " pages")
      println("\n\n frontsAlertList contains: " + frontsAlertList.length + " pages")
      println("\n\n ***** \n\n" + "article Alert body:\n" + liveBlogAlertMessageBody)
      println("\n\n ***** \n\n" + "liveblog Alert body:\n" + liveBlogAlertMessageBody)
       println("\n\n ***** \n\n" + "fronts Alert Body:\n" + frontsAlertMessageBody)
      println("\n\n ***** \n\n" + "Full email Body:\n" + htmlString.generalAlertFullEmailBody(articleAlertMessageBody, liveBlogAlertMessageBody, frontsAlertMessageBody))
      println("compiling and sending email")
      val emailSuccess = emailer.send(generalAlertsAddressList, htmlString.generalAlertFullEmailBody(articleAlertMessageBody, liveBlogAlertMessageBody, frontsAlertMessageBody))
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

  def getResultPages(urlList: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String): List[(String, String)] = {
    val wpt: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    val desktopResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.sendPage(page))
    })
    val mobileResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.sendMobile3GPage(page, wptLocation))
    })
    desktopResults ::: mobileResults
  }

  def listenForResultPages(capiPages: List[(Option[ContentFields],String)], contentType: String, resultUrlList: List[(String, String)], averages: PageAverageObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String): List[PerformanceResultsObject] = {
    println("ListenForResultPages called with: \n\n" +
      " List of Urls: \n" + capiPages.map(page => page._2).mkString +
      "\n\nList of WebPage Test results: \n" + resultUrlList.mkString +
      "\n\nList of averages: \n" + averages.toHTMLString + "\n")

    val listenerList: List[WptResultPageListener] = capiPages.flatMap(page => {
      for (element <- resultUrlList if element._1 == page._2) yield new WptResultPageListener(element._1, contentType, page._1, element._2)
    })

    println("Listener List created: \n" + listenerList.map(element => "list element: \n" + "url: " + element.pageUrl + "\n" + "resulturl" + element.wptResultUrl + "\n"))

    val resultsList: ParSeq[WptResultPageListener] = listenerList.par.map(element => {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey)
      val newElement = new WptResultPageListener(element.pageUrl, element.pageType, element.pageFields,element.wptResultUrl)
      newElement.testResults = wpt.getResults(newElement.wptResultUrl)
      newElement.testResults.setHeadline(newElement.headline)
      newElement.testResults.setPageType(newElement.pageType)
      newElement
    })
    val testResults = resultsList.map(element => element.testResults).toList
    val resultsWithAlerts: List[PerformanceResultsObject] = testResults.map(element => setAlertStatus(element, averages))

    //Confirm alert status by retesting alerting urls
    println("Confirming any items that have an alert")
    val confirmedTestResults = resultsWithAlerts.map(x => {
      if (x.alertStatus)
        confirmAlert(x, averages, wptBaseUrl, wptApiKey, wptLocation)
      else
        x
    })
    confirmedTestResults
  }

  def confirmAlert(initialResult: PerformanceResultsObject, averages: PageAverageObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String): PerformanceResultsObject = {
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    val testCount: Int = if (initialResult.timeToFirstByte > 1000) {
      5
    } else {
      3
    }
    println("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
    val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatus(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    AlertConfirmationTestResult
  }

  def setAlertStatus(resultObject: PerformanceResultsObject, averages: PageAverageObject): PerformanceResultsObject = {
    //  Add results to string which will eventually become the content of our results file
    if (resultObject.typeOfTest == "Desktop") {
      if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs80thPercentile) ||
        (resultObject.speedIndex >= averages.desktopSpeedIndex80thPercentile) ||
        (resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded80thPercentile) ||
        (resultObject.estUSPrePaidCost >= averages.desktopEstUSPrePaidCost80thPercentile) ||
        (resultObject.estUSPostPaidCost >= averages.desktopEstUSPostPaidCost80thPercentile)) {
        if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) ||
          (resultObject.speedIndex >= averages.desktopSpeedIndex) ||
          (resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded)) {
          println("row should be red one of the items qualifies")
          if (resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) {
            resultObject.alertDescription = "<p>Page takes " + resultObject.timeFirstPaintInSec + "s" + " for text to load and page to become scrollable. Should only take " + averages.desktopTimeFirstPaintInSeconds + "s.</p>"
          }
          if (resultObject.speedIndex >= averages.desktopSpeedIndex) {
            resultObject.alertDescription = "<p>Page takes " + resultObject.aboveTheFoldCompleteInSec + "To render visible images etc. It should take " + averages.desktopAboveTheFoldCompleteInSec + "s.</P>"
          }
          if (resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded) {
            resultObject.alertDescription = resultObject.alertDescription + "<p>Page is too heavy. Size is: " + resultObject.kBInFullyLoaded + "KB. It should be less than: " + averages.desktopKBInFullyLoaded + "KB.</p>"
          }
          println(resultObject.alertDescription)
          resultObject.warningStatus = true
          resultObject.alertStatus = true
        }
        else {
          println("Qualifies for result status but this status has been removed for a trial period")
          // println("row should be yellow one of the items qualifies")
          resultObject.warningStatus = false
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
          (resultObject.kBInFullyLoaded >= averages.mobileKBInFullyLoaded)) {
          println("warning and alert statuses set to true")
          if (resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) {
            resultObject.alertDescription = "<p>Page takes " + resultObject.timeFirstPaintInSec + "s" + " for text to load and page to become scrollable. Should only take " + averages.mobileTimeFirstPaintInSeconds + "s.</p>"
          }
          if (resultObject.speedIndex >= averages.mobileSpeedIndex) {
            resultObject.alertDescription = "<p>Page takes " + resultObject.aboveTheFoldCompleteInSec + "s " + "To render visible images etc. It should take " + averages.mobileAboveTheFoldCompleteInSec + "s or less.</p>"
          }
          if (resultObject.kBInFullyLoaded >= averages.mobileKBInFullyLoaded) {
            resultObject.alertDescription = resultObject.alertDescription + "<p>Page is too heavy. Size is: " + resultObject.kBInFullyLoaded + "KB. It should be less than: " + averages.mobileKBInFullyLoaded + "KB.</p>"
          }
          resultObject.warningStatus = true
          resultObject.alertStatus = true
        }
        else {
          println("Qualifies for result status but this status has been removed for a trial period")
          //          println("warning status set to true")
          resultObject.warningStatus = false
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

  def generateInteractiveAverages(urlList: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String, itemtype: String, averageColor: String): PageAverageObject = {
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


  def retestUrl(initialResult: PerformanceResultsObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String): PerformanceResultsObject = {
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    val testCount: Int = if (initialResult.timeToFirstByte > 1000) {
      5
    } else {
      3
    }
    println("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
    //   val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatus(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount)
  }


  def orderList(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    if(list.length % 2 == 0) {
      println("orderList called. \n It has " + list.length + " elements.")
      val tupleList = listSinglesToPairs(list)
      println("listSinglesToPairs returned a list of " + tupleList.length + " pairs.")
      val alertsList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if element._1.alertStatus || element._2.alertStatus) yield element
      val warningList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if element._1.warningStatus || element._2.warningStatus) yield element
      val okList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if !element._1.alertStatus && !element._1.warningStatus && !element._2.alertStatus && !element._2.warningStatus) yield element

      sortByWeight(alertsList) ::: sortByWeight(warningList) ::: sortByWeight(okList)
    }
    else{
      println("orderList has odd number of elements. List length is: " + list.length)
      List()
    }
  }

  def sortHomogenousResults(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val alertsResultsList: List[PerformanceResultsObject] = for (result <- list if result.alertStatus) yield result
    val warningResultsList: List[PerformanceResultsObject] = for (result <- list if result.warningStatus && !result.alertStatus) yield result
    val okResultsList: List[PerformanceResultsObject] = for (result <- list if !result.alertStatus && !result.warningStatus) yield result
    val sortedAlertList: List[PerformanceResultsObject] = alertsResultsList.sortWith(_.bytesInFullyLoaded > _.bytesInFullyLoaded)
    val sortedWarningList: List[PerformanceResultsObject] = warningResultsList.sortWith(_.bytesInFullyLoaded > _.bytesInFullyLoaded)
    val sortedOkList: List[PerformanceResultsObject] = okResultsList.sortWith(_.bytesInFullyLoaded > _.bytesInFullyLoaded)
    sortedAlertList ::: sortedWarningList ::: sortedOkList
  }

  def sortByWeight(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[PerformanceResultsObject] = {
    if(list.nonEmpty){
      val sortedTupleList = sortTupleList(list)
      makeList(sortedTupleList)
    }
    else {
        println("sortByWeight has noElements in list. Passing back empty list")
        List()
    }
  }


  def listSinglesToPairs(list: List[PerformanceResultsObject]): List[(PerformanceResultsObject, PerformanceResultsObject)] = {
    if (list.nonEmpty && list.length % 2 == 0) {
      println("list Singles to pairs has " + list.length + " perf results objects.")
      val tupleList = makeTuple(List((list.head, list.tail.head)), list.tail.tail)
      println("makeTuple called - returned a list of " + tupleList.length + "pairs")
      tupleList
    }
    else {
      println("listSinglesToPairs has been passed an empty or odd number of elements: list has " + list.length + "elements" )
      makeTuple(List((list.head, list.tail.head)), list.tail.tail)
    }
  }

  def makeTuple(tupleList: List[(PerformanceResultsObject, PerformanceResultsObject)], restOfList: List[PerformanceResultsObject]): List[(PerformanceResultsObject, PerformanceResultsObject)] = {
    println("maketuple function here: tuple list has: " + tupleList.length + " elements.\n" + "               and the rest of list has: " + restOfList.length + " elements remaining.")
    if (restOfList.isEmpty) {
      tupleList
    }
    else {
      if (restOfList.length < 2) {
        println("make tuple has odd number of items in list"); tupleList
      }
      else {
        makeTuple(tupleList ::: List((restOfList.head, restOfList.tail.head)), restOfList.tail.tail)
      }
    }
  }


  def makeList(tupleList: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[PerformanceResultsObject] = {
    val fullList: List[PerformanceResultsObject] = tupleList.flatMap(a => List(a._1,a._2))
    fullList
  }

  def sortTupleList(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[(PerformanceResultsObject,PerformanceResultsObject)] = {
    list.sortWith{(leftE:(PerformanceResultsObject, PerformanceResultsObject),rightE:(PerformanceResultsObject, PerformanceResultsObject)) =>
      leftE._1.bytesInFullyLoaded + leftE._2.bytesInFullyLoaded > rightE._1.bytesInFullyLoaded + rightE._2.bytesInFullyLoaded}
  }

}


