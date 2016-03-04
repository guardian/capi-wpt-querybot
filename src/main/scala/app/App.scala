package app

// note an _ instead of {} would get everything

import java.io._
import java.util

import app.api.S3Operations
import app.apiutils._
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime

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
    val outputFileName = "liveBlogPerformanceData.html"
    val simpleOutputFileName = "liveBlogPerformanceDataExpurgated.html"
    val interactiveOutputFilename = "interactivePerformanceData.html"
    val frontsOutputFilename = "frontsData.html"

    val liveBlogResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + simpleOutputFileName
    val interactiveResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + interactiveOutputFilename
    val frontsResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + frontsOutputFilename

    //Define colors to be used for average values, warnings and alerts
    val averageColor: String = "\"grey\""
    val warningColor: String = "\"#FFFF00\""
    val alertColor = "\"#FF0000\""

    //  Initialize results string - this will be used to accumulate the results from each test so that only one write to file is needed.
    val htmlString = new HtmlStringOperations(averageColor, warningColor, alertColor, liveBlogResultsUrl, interactiveResultsUrl, frontsResultsUrl)
    var simplifiedResults: String = htmlString.initialisePageForLiveblog + htmlString.initialiseTable
    var interactiveResults: String = htmlString.initialisePageForInteractive + htmlString.initialiseTable
    var frontsResults: String = htmlString.initialisePageForFronts + htmlString.initialiseTable

    //Initialize email alerts string - this will be used to generate emails
    var liveBlogAlertMessageBody: String = ""
    var interactiveAlertMessageBody: String = ""
    var frontsAlertMessageBody: String = ""

    //Initialise List of sample items to be used to make alerting levels for different content types
    val listofLargeInteractives: List[String] = List("http://www.theguardian.com/us-news/2015/sep/01/moving-targets-police-shootings-vehicles-the-counted")
    val interactiveItemLabel: String = "Interactive"

    val listofFronts: List[String] = List("http://www.theguardian.com/uk"/*,
      "http://www.theguardian.com/us",
      "http://www.theguardian.com/au",
      "http://www.theguardian.com/uk-news",
      "http://www.theguardian.com/world",
      "http://www.theguardian.com/politics",
      "http://www.theguardian.com/uk/sport",
      "http://www.theguardian.com/football",
      "http://www.theguardian.com/uk/commentisfree",
      "http://www.theguardian.com/uk/culture",
      "http://www.theguardian.com/uk/business",
      "http://www.theguardian.com/uk/lifeandstyle",
      "http://www.theguardian.com/fashion",
      "http://www.theguardian.com/uk/environment",
      "http://www.theguardian.com/uk/technology",
      "http://www.theguardian.com/travel"*/)
    val frontsItemlabel: String = "Front"

    //Initialise List of email contacts (todo - this must be put in a file before getting any real folk)
    val emailAddressList: List[String] = List("michael.mcnamara@guardian.co.uk", "m_w_mcnamara@hotmail.com")

    //Create new S3 Client
    println("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
    val s3Interface = new S3Operations(s3BucketName, configFileName)
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

    //Create Email Handler class
    val emailer: EmailOperations = new EmailOperations(emailUsername, emailPassword)


    //  Define new CAPI Query object
    val articleUrlList = new ArticleUrls(contentApiKey)
    val articleUrls: List[String] = articleUrlList.getLiveBlogUrls
    println(DateTime.now + " Closing Liveblog Content API query connection")
    articleUrlList.shutDown
    // check results returned from CAPI and extract data form liveblogs if there are any
    if (articleUrls.nonEmpty) {
      println("Combined results from LiveBLog CAPI calls")
      articleUrls.foreach(println)
      println("Generating average values for migrated liveblogs")
      val migratedLiveBlogAverages: PageAverageObject = new LiveBlogDefaultAverages
      simplifiedResults = simplifiedResults.concat(migratedLiveBlogAverages.toHTMLString)
      println("Performance testing liveblogs")
      // Send each article URL to the webPageTest API and obtain resulting data
      val testResults: List[PerformanceResultsObject] = articleUrls.flatMap(url => {
        testUrl(url, wptBaseUrl, wptApiKey, wptLocation, migratedLiveBlogAverages)
      })
      //Confirm alert status by retesting alerting urls
      val confirmedTestResults = testResults.map(x => {
        if(x.alertStatus)
          confirmAlert(x, migratedLiveBlogAverages, wptBaseUrl, wptApiKey, wptLocation)
        else
          x
      })
      //Create a list of alerting pages and write to string
      val liveBlogAlertList: List[PerformanceResultsObject] = for (result <- confirmedTestResults if result.alertStatus) yield result
      liveBlogAlertMessageBody = htmlString.generateAlertEmailBodyElement(liveBlogAlertList, migratedLiveBlogAverages)

      val resultsList: List[String] = confirmedTestResults.map(x => htmlString.generateHTMLRow(x))
      simplifiedResults = simplifiedResults.concat(resultsList.mkString)
      println(DateTime.now + " Results added to accumulator string \n")
    }
    else {
      println(DateTime.now + " WARNING: No results returned from Content API for LiveBlog Queries")
      simplifiedResults = simplifiedResults.concat("<tr><th>No Liveblogs found to test</th></tr>")
    }

    //close off table in HTML string
    simplifiedResults = simplifiedResults + htmlString.closeTable
    // add closing decoration in HTML string so that page is complete
    simplifiedResults = simplifiedResults + htmlString.closePage

    //write liveblog results
    if (!iamTestingLocally) {
      println(DateTime.now + " Writing liveblog results to S3")
      s3Interface.writeFileToS3(simpleOutputFileName, simplifiedResults)
    }
    else {
      val outputWriter = new LocalFileOperations
      val writeSuccess: Int = outputWriter.writeLocalResultFile(simpleOutputFileName, simplifiedResults)
      if (writeSuccess != 0) {
        println("problem writing local outputfile")
        System exit 1
      }
    }
      println("LiveBlog Performance Test Complete")
      //  Define new CAPI Query object
      val interactiveUrlList = new ArticleUrls(contentApiKey)
      //  Request a list of urls from Content API
      val interactiveUrls: List[String] = interactiveUrlList.getInteractiveUrls
      println(DateTime.now + " Closing Interactive Content API query connection")
      interactiveUrlList.shutDown
      if (interactiveUrls.isEmpty) {
        println(DateTime.now + " WARNING: No results returned from Content API for Interactive Query")
        interactiveResults = interactiveResults.concat("<tr><th>No Interactives found to test</th></tr>")
      }
      else {
        // Send each article URL to the webPageTest API and obtain resulting data
        println("Results from Interactive CAPI calls")
        interactiveUrls.foreach(println)
        println("Generating average values for migrated Interactives")
        val averageInteractivesPerformance: PageAverageObject = generatePageAverages(listofLargeInteractives, wptBaseUrl, wptApiKey, wptLocation, interactiveItemLabel)
        interactiveResults = interactiveResults.concat(averageInteractivesPerformance.toHTMLString)

        val interactiveTestResults: List[PerformanceResultsObject] = interactiveUrls.flatMap(url => {
          testUrl(url, wptBaseUrl, wptApiKey, wptLocation, averageInteractivesPerformance)
        })
        val confirmedInteractiveResults = interactiveTestResults.map(x => {
          if (x.alertStatus || x.brokenTest) {
            println("alert status or broken test detected on " + x.testUrl + "\n" + "Retesting to confirm")
            confirmAlert(x, averageInteractivesPerformance, wptBaseUrl, wptApiKey, wptLocation)
          }
          else {
            println("test ok and no alert status detected - leaving untouched")
            x
          }
        })
        //Create a list of alerting pages and write to string
        val interactiveAlertList: List[PerformanceResultsObject] = for (result <- confirmedInteractiveResults if result.alertStatus) yield result
        interactiveAlertMessageBody = htmlString.generateAlertEmailBodyElement(interactiveAlertList, averageInteractivesPerformance)

        val simplifiedInteractiveResultsList: List[String] = confirmedInteractiveResults.map(x => htmlString.generateHTMLRow(x))
        interactiveResults = interactiveResults.concat(simplifiedInteractiveResultsList.mkString)
        println(DateTime.now + " Results added to accumulator string \n")
      }
      interactiveResults = interactiveResults.concat(htmlString.closeTable + htmlString.closePage)

      if (!iamTestingLocally) {
        println(DateTime.now + " Writing interactive results to S3")
        s3Interface.writeFileToS3(interactiveOutputFilename, interactiveResults)
        s3Interface.closeS3Client()
      }
      else {
        val interactiveOutput: FileWriter = new FileWriter(interactiveOutputFilename)
        println(DateTime.now + " Writing the following to local file: " + interactiveOutputFilename + ":\n" + interactiveResults)
        interactiveOutput.write(interactiveResults)
        interactiveOutput.close()
        println(DateTime.now + " Writing to file: " + interactiveOutputFilename + " complete. \n")
      }

    println("Interactive Performance Test Complete")
  
    //  Define new CAPI Query object
    //val interactiveUrlList = new ArticleUrls(contentApiKey)
    //  Request a list of urls from Content API
    //val interactiveUrls: List[String] = interactiveUrlList.getInteractiveUrls
    //println(DateTime.now + " Closing Interactive Content API query connection")
    //interactiveUrlList.shutDown
    if (listofFronts.isEmpty) {
      println(DateTime.now + " WARNING: No results returned from Content API for Fronts Query")
      interactiveResults = interactiveResults.concat("<tr><th>No Interactives found to test</th></tr>")
    }
    else {
      // Send each article URL to the webPageTest API and obtain resulting data
      println("Results from Fronts CAPI calls")
      listofFronts.foreach(println)
      println("Generating benchmark metrics for judging fronts")
      val averageFrontsPerformance: PageAverageObject = new FrontsDefaultAverages
      frontsResults = interactiveResults.concat(averageFrontsPerformance.toHTMLString)

      val frontsTestResults: List[PerformanceResultsObject] = listofFronts.flatMap(url => {
        testUrl(url, wptBaseUrl, wptApiKey, wptLocation, averageFrontsPerformance)
      })
      val confirmedFrontsResults = frontsTestResults.map(x => {
        if (x.alertStatus) {
          println("alert status detected on " + x.testUrl + "\n" + "Retesting to confirm")
          confirmAlert(x, averageFrontsPerformance, wptBaseUrl, wptApiKey, wptLocation)
        }
        else {
          println("no alert status detected - leaving untouched")
          x
        }
      })
      //Create a list of alerting pages and write to string
      val frontsAlertList: List[PerformanceResultsObject] = for (result <- confirmedFrontsResults if result.alertStatus) yield result
      frontsAlertMessageBody = htmlString.generateAlertEmailBodyElement(frontsAlertList, averageFrontsPerformance)

      val simplifiedInteractiveResultsList: List[String] = confirmedFrontsResults.map(x => htmlString.generateHTMLRow(x))
      frontsResults = interactiveResults.concat(simplifiedInteractiveResultsList.mkString)
      println(DateTime.now + " Results added to accumulator string \n")
    }
    frontsResults = interactiveResults.concat(htmlString.closeTable + htmlString.closePage)

    if (!iamTestingLocally) {
      println(DateTime.now + " Writing fronts results to S3")
      s3Interface.writeFileToS3(frontsOutputFilename, frontsResults)
      s3Interface.closeS3Client()
    }
    else {
      val frontsOutput: FileWriter = new FileWriter(frontsOutputFilename)
      println(DateTime.now + " Writing results to local file: " + frontsOutputFilename + ":\n" + frontsResults)
      frontsOutput.write(frontsResults)
      frontsOutput.close()
      println(DateTime.now + " Writing results to file: " + frontsOutputFilename + " complete. \n")
    }

    println("Fronts Performance Test Complete")

      if (liveBlogAlertMessageBody.nonEmpty || interactiveAlertMessageBody.nonEmpty || frontsAlertMessageBody.nonEmpty) {
        println("\n\n ***** \n\n" + "liveblog Alert body:\n" + liveBlogAlertMessageBody)
        println("\n\n ***** \n\n" + "interactive Alert Body:\n" + interactiveAlertMessageBody)
        println("\n\n ***** \n\n" + "fronts Alert Body:\n" + frontsAlertMessageBody)
        println("\n\n ***** \n\n" + "Full email Body:\n" + htmlString.generateFullAlertEmailBody(liveBlogAlertMessageBody, interactiveAlertMessageBody, frontsAlertMessageBody))
        println("compiling and sending email")
        val emailSuccess = emailer.send(emailAddressList, htmlString.generateFullAlertEmailBody(liveBlogAlertMessageBody, interactiveAlertMessageBody,  frontsAlertMessageBody))
        if (emailSuccess)
          println(DateTime.now + " Emails sent successfully. \n Job complete")
        else
          println(DateTime.now + "ERROR: Job completed, but sending of emails failed")
      } else {
        println("No pages to alert on. Email not sent. \n Job complete")
      }
    }


  def testUrl(url: String, wptBaseUrl: String, wptApiKey: String, wptLocation: String, averages: PageAverageObject): Array[PerformanceResultsObject] = {
    //  Define new web-page-test API request and send it the url to test
    println(DateTime.now + " creating new WebPageTest object with this base URL: " + wptBaseUrl)
    val webpageTest: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    println(DateTime.now + " calling methods to test url: " + url + " on desktop")
    val webPageDesktopTestResults: PerformanceResultsObject = webpageTest.desktopChromeCableTest(url)
    println(DateTime.now + " calling methods to test url: " + url + " on emulated 3G mobile")
    val webPageMobileTestResults: PerformanceResultsObject = webpageTest.mobileChrome3GTest(url, wptLocation)
    //  Add results to string which will eventually become the content of our results file
    val desktopTestWithAlertsSet: PerformanceResultsObject = setAlertStatus(webPageDesktopTestResults,averages)
    val mobileTestWithAlertsSet: PerformanceResultsObject = setAlertStatus(webPageMobileTestResults,averages)
    println("Returning desktop and mobile results")
    Array(desktopTestWithAlertsSet, mobileTestWithAlertsSet)
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

    def generatePageAverages(urlList: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String, itemtype: String): PageAverageObject = {
      val webpageTest: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey)

      val resultsList: List[Array[PerformanceResultsObject]] = urlList.map(url => {
        val webPageDesktopTestResults: PerformanceResultsObject = webpageTest.desktopChromeCableTest(url)
        val webPageMobileTestResults: PerformanceResultsObject = webpageTest.mobileChrome3GTest(url, wptLocation)
        val combinedResults = Array(webPageDesktopTestResults, webPageMobileTestResults)
        combinedResults
      })

      val pageAverages: PageAverageObject = new GeneratedPageAverages(resultsList)
      pageAverages
    }

}


