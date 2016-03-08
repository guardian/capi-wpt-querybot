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
    val iamTestingLocally = false
    /*#####################################################################################*/
    println("Job started at: " + DateTime.now)
    println("Local Testing Flag is set to: " + iamTestingLocally.toString)

    //  Define names of s3bucket, configuration and output Files
    val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
    val s3BucketName = "capi-wpt-querybot"
    val configFileName = "config.conf"

    val articleCSVName = "accumulatedArticlePerformanceData.csv"
    val liveBlogCSVName = "accumulatedLiveblogPerformanceData.csv"
    val interactiveCSVName = "accumulatedInteractivePerformanceData.csv"
    val videoCSVName = "accumulatedVideoPerformanceData"
    val audioCSVName = "accumulatedAudioPerformanceData"
    val frontsCSVName = "accumulatedFrontsPerformanceData"

    //    val liveBlogResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + simpleOutputFileName

    //Define colors to be used for average values, warnings and alerts

    //  Initialize results string - this will be used to accumulate the results from each test so that only one write to file is needed.


    var articleCSVResults: String = ""
    var liveBlogCSVResults: String = ""
    var interactiveCSVResults: String = ""
    var videoCSVResults: String = ""
    var audioCSVResults: String = ""
    var frontsCSVResults: String = ""

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

    //Get Articles
    articleCSVResults = runAllTestsForContentType("Article", contentApiKey, wptBaseUrl, wptApiKey, wptLocation)
    println(DateTime.now + " Results added to accumulator string \n")
    if (articleCSVResults.isEmpty) {
      println(DateTime.now + " WARNING: No results returned from Content API for Article Queries")
    } else {
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing Article results to S3")
        s3Interface.writeFileToS3(articleCSVName, articleCSVResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(articleCSVName, articleCSVResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
    }

    //Get LiveBlogs
    liveBlogCSVResults = runAllTestsForContentType("LiveBlog", contentApiKey, wptBaseUrl, wptApiKey, wptLocation)
    println(DateTime.now + " Results added to accumulator string \n")
    if (liveBlogCSVResults.isEmpty) {
      println(DateTime.now + " WARNING: No results returned from Content API for LiveBlog Queries")
    } else {
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing liveblog results to S3")
        s3Interface.writeFileToS3(liveBlogCSVName, liveBlogCSVResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(liveBlogCSVName, liveBlogCSVResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
    }

    //Get Interactives
    interactiveCSVResults = runAllTestsForContentType("Interactive", contentApiKey, wptBaseUrl, wptApiKey, wptLocation)
    println(DateTime.now + " Results added to accumulator string \n")
    if (interactiveCSVResults.isEmpty) {
      println(DateTime.now + " WARNING: No results returned from Content API for Interactive Queries")
    } else {
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing Interactive results to S3")
        s3Interface.writeFileToS3(interactiveCSVName, interactiveCSVResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(interactiveCSVName, interactiveCSVResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
    }

    //Get Videos
    videoCSVResults = runAllTestsForContentType("Video", contentApiKey, wptBaseUrl, wptApiKey, wptLocation)
    println(DateTime.now + " Results added to accumulator string \n")
    if (videoCSVResults.isEmpty) {
      println(DateTime.now + " WARNING: No results returned from Content API for Video Queries")
    } else {
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing Video results to S3")
        s3Interface.writeFileToS3(videoCSVName, videoCSVResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(videoCSVName, videoCSVResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
    }

    //Get Audio
    audioCSVResults = runAllTestsForContentType("Audio", contentApiKey, wptBaseUrl, wptApiKey, wptLocation)
    println(DateTime.now + " Results added to accumulator string \n")
    if (audioCSVResults.isEmpty) {
      println(DateTime.now + " WARNING: No results returned from Content API for Audio Queries")
    } else {
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing Audio results to S3")
        s3Interface.writeFileToS3(audioCSVName, audioCSVResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(audioCSVName, audioCSVResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
    }

    //Get Fronts
    frontsCSVResults = runAllTestsForContentType("Front", contentApiKey, wptBaseUrl, wptApiKey, wptLocation)
    println(DateTime.now + " Results added to accumulator string \n")
    if (frontsCSVResults.isEmpty) {
      println(DateTime.now + " WARNING: No results returned from Content API for Fronts Queries")
    } else {
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing Fronts results to S3")
        s3Interface.writeFileToS3(frontsCSVName, frontsCSVResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(frontsCSVName, frontsCSVResults)
        if (writeSuccess != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
      }
    }

  }



def runAllTestsForContentType(contentType:String, contentApiKey:String, wptBaseUrl:String, wptApiKey: String, wptLocation:String):String  = {
  var resultString: String = "testUrl,timeOfTest,resultStatus,timeFirstPaintInMs,timeDocCompleteInMs,bytesInDocComplete,timeFullyLoadedInMs,bytesInFullyLoaded,speedIndex,element1.resource,element1.contentType,element1.bytesDownloaded,element2.resource,element2.contentType,element2.bytesDownloaded,element3.resource,element3.contentType,element3.bytesDownloaded,element4.resource,element4.contentType,element4.bytesDownloaded,element5.resource,element5.contentType,element5.bytesDownloaded\n"

  val capiHandler = new ArticleUrls(contentApiKey)
  val urlList: List[String] = capiHandler.getUrlsForContentType(contentType)
  println(DateTime.now + " Closing Liveblog Content API query connection")
  capiHandler.shutDown
  // check results returned from CAPI and extract data form liveblogs if there are any
  if (urlList.nonEmpty) {

    val testResults: List[PerformanceResultsObject] = urlList.flatMap(url => {
      val webpageTest: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
      val webPageDesktopTestResults: PerformanceResultsObject = webpageTest.desktopChromeCableTest(url)
      val webPageMobileTestResults: PerformanceResultsObject = webpageTest.mobileChrome3GTest(url, wptLocation)
      Array(webPageDesktopTestResults, webPageMobileTestResults)
    })
    //Confirm alert status by retesting alerting urls
    val confirmedTestResults = testResults.map(x => {
      if (x.brokenTest) {
        val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
        val testCount: Int = if(x.timeToFirstByte > 1000) {5} else {3}
        webPageTest.testMultipleTimes(x.testUrl, x.typeOfTest, wptLocation, testCount)
      }
      else
        x
    })
    val resultsList: List[String] = confirmedTestResults.map(x => x.toCSVString())
    resultString = resultString + resultsList.mkString
  }
  else {
    println(DateTime.now + " WARNING: No results returned from Content API for LiveBlog Queries")
    resultString = ""
  }
  resultString
}




  def retestUrl(initialResult: PerformanceResultsObject,wptBaseUrl: String, wptApiKey: String, wptLocation: String): PerformanceResultsObject ={
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    val testCount: Int = if(initialResult.timeToFirstByte > 1000) {5} else {3}
    println("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
 //   val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatus(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount)
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


