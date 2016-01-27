package app

// note an _ instead of {} would get everything

import java.io._

import app.apiutils.{ArticleUrls, WebPageTest}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
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
    val s3BucketName = "capi-wpt-querybot"
    val configFileName = "config.conf"
    val outputFileName = "liveBlogPerformanceData.html"
    val simpleOutputFileName = "liveBlogPerformanceDataExpurgated.html"
    val interactiveOutputFilename = "interactivePerformanceData.html"

    //  Initialize results string - this will be used to accumulate the results from each test so that only one write to file is needed.
    val averageColor: String = "\"grey\""
    val warningColor: String = "\"#FFFF00\""
    val alertColor = "\"#FF0000\""
    val hTMLPageHeader:String = "<!DOCTYPE html>\n<html>\n<body>\n"
    val hTMLTitleLiveblog:String = "<h1>Currrent Performance of today's Liveblogs</h1>"
    val hTMLTitleInteractive:String = "<h1>Currrent Performance of today's Interactives</h1>"
    val hTMLJobStarted: String = "Job started at: " + DateTime.now + "\n"
    val hTMLTableHeaders:String = "<table border=\"1\">\n<tr bgcolor=" +averageColor +">\n<th>Time Last Tested</th>\n<th>Test Type</th>\n<th>Article Url</th>\n<th>Time to First Paint</th>\n<th>Time to Document Complete</th>\n<th>kB transferred at Document Complete</th>\n<th>Time to Fully Loaded</th>\n<th>kB transferred at Fully Loaded</th>\n<th>Cost at $0.05(US) per MB</th>\n<th>Speed Index</th>\n<th>Status</th>\n</tr>\n"
    val hTMLSimpleTableHeaders:String = "<table border=\"1\">\n<tr bgcolor="+ averageColor +">\n<th>Time Last Tested</th>\n<th>Test Type</th>\n<th>Article Url</th>\n<th>Time to Document Complete</th>\n<th>kB transferred</th>\n<th>Cost at $0.05(US) per MB</th>\n<th>Speed Index</th>\n<th>Status</th>\n</tr>\n"
    val hTMLTableFooters:String = "</table>"
    val hTMLPageFooterStart: String =  "\n<p><i>Job completed at: "
    val hTMLPageFooterEnd: String = "</i></p>\n</body>\n<html>"
    var results: String = hTMLPageHeader + hTMLTitleLiveblog + hTMLJobStarted + hTMLTableHeaders
    var simplifiedResults: String = hTMLPageHeader + hTMLTitleLiveblog + hTMLJobStarted + hTMLSimpleTableHeaders

    var interactiveResults: String = hTMLPageHeader + hTMLTitleInteractive + hTMLJobStarted + hTMLSimpleTableHeaders

    var contentApiKey: String = ""
    var wptBaseUrl: String = ""
    var wptApiKey: String = ""
    var wptLocation: String = ""

    //initialize rogues Gallery - will set this up as a file another time
    val roguesGallery: List[String] = List("http://www.theguardian.com/film/filmblog/live/2015/oct/21/back-to-the-future-day-live-experience-21-october-2015-round-the-world",
      "http://www.theguardian.com/music/live/2016/jan/11/david-bowie-dies-of-cancer-aged-69-reports",
      "http://www.theguardian.com/world/live/2015/nov/14/paris-terror-attacks-attackers-dead-mass-killing-live-updates",
      "http://www.theguardian.com/us-news/live/2015/oct/13/cnn-democratic-debate-bernie-sanders-hillary-clinton-las-vegas",
      "http://www.theguardian.com/politics/blog/live/2015/may/07/election-2015-live-final-votes-cast-as-battle-for-power-looms")

    println("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
    val s3Client = new AmazonS3Client()
    if(!iamTestingLocally) {
      println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
      val conf = getS3Config(s3Client, s3BucketName, configFileName)
      contentApiKey = conf.getString("content.api.key")
      wptBaseUrl = conf.getString("wpt.api.baseUrl")
      wptApiKey = conf.getString("wpt.api.key")
      wptLocation=conf.getString("wpt.location")
      if ((contentApiKey.length > 0) && (wptBaseUrl.length > 0) && (wptApiKey.length > 0))
        println(DateTime.now +" Config retrieval successful. \n You are using the following webpagetest instance: " + wptBaseUrl)
      else {
        println(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
        System.exit(1)
      }
    } else
      {
            println(DateTime.now + " retrieving local config file: " + configFileName)
            for (line <- Source.fromFile(configFileName).getLines()) {
              if (line.contains("content.api.key")) {
                println("capi key found")
                contentApiKey = line.takeRight((line.length - line.indexOf("=")) - 1)
              }
              if (line.contains("wpt.api.baseUrl")) {
                println("wpt url found")
                wptBaseUrl = line.takeRight((line.length - line.indexOf("=")) - 1)
              }
              if (line.contains("wpt.api.key")) {
                println("wpt api key found")
                wptApiKey = line.takeRight((line.length - line.indexOf("=")) - 1)
              }
              if (line.contains("wpt.location")) {
                println("wpt location found")
                wptLocation = line.takeRight((line.length - line.indexOf("=")) - 1)
              }
            }
              if ((contentApiKey.length > 0) && (wptBaseUrl.length > 0) && (wptApiKey.length > 0)){
                println(DateTime.now + " Config retrieval successful. \n You are using the following webpagetest instance: " + wptBaseUrl)}
              else {
                println(DateTime.now + "ERROR: Problem retrieving config file - one or more parameters not retrieved")
                println(contentApiKey + "," + wptBaseUrl + "," + wptApiKey)
                System.exit(1)
              }
      }

    //  Define new CAPI Query object
    val articleUrlList = new ArticleUrls(contentApiKey)
    //  Request a list of urls from Content API
    val articleUrls: List[String] = articleUrlList.getLiveBlogUrls
    println(DateTime.now + " Closing Liveblog Content API query connection")
    articleUrlList.shutDown
    if (articleUrls.isEmpty) {
      println(DateTime.now + " WARNING: No results returned from Content API for LiveBlog Queries")
      results = results.concat("<tr><th>No Liveblogs found to test</th></tr>")
      simplifiedResults = simplifiedResults.concat("<tr><th>No Liveblogs found to test</th></tr>")
    }
    else {
            println("Combined results from LiveBLog CAPI calls")
            articleUrls.foreach(println)
            println("Generating average values for migrated liveblogs")
            val migratedLiveBlogAverages: PageAverageObject = testRoguesGallery(roguesGallery ,wptBaseUrl, wptApiKey, wptLocation)
            simplifiedResults = simplifiedResults.concat(migratedLiveBlogAverages.toHTMLString)
            println("Performance testing liveblogs")
            // Send each article URL to the webPageTest API and obtain resulting data
            val testResults: List[List[String]] = articleUrls.map(url => testUrlReturnHtml(url, wptBaseUrl, wptApiKey, wptLocation, migratedLiveBlogAverages, warningColor, alertColor))
            // Add results to a single string so that we only need to write to S3 once (S3 will only take complete objects).
            val resultsList: List[String] = testResults.map(x => x.head)
            val simplifiedResultsList : List[String] = testResults.map(x => x.tail.head)

            results = results.concat(resultsList.mkString)
            simplifiedResults = simplifiedResults.concat(simplifiedResultsList.mkString)
            println(DateTime.now + " Results added to accumulator string \n")
        }
    results = results.concat(hTMLTableFooters)
    results = results.concat(hTMLPageFooterStart + DateTime.now + hTMLPageFooterEnd)
    simplifiedResults = simplifiedResults.concat(hTMLTableFooters)
    simplifiedResults = simplifiedResults.concat("<p> List of urls used to generate averages: </p> <table border=\"1\">" + roguesGallery.map(url => "<tr><td>" + url + "</td></tr>").mkString + "</table>")
    simplifiedResults = simplifiedResults.concat(hTMLPageFooterStart + DateTime.now + hTMLPageFooterEnd)
    if (!iamTestingLocally) {
      println(DateTime.now + " Writing the following to S3:\n" + results + "\n")
      s3Client.putObject(new PutObjectRequest(s3BucketName, outputFileName, createOutputFile(outputFileName, results)))
      val aclDevFile: AccessControlList = s3Client.getObjectAcl(s3BucketName, outputFileName)
      aclDevFile.grantPermission(GroupGrantee.AllUsers, Permission.Read)
      s3Client.setObjectAcl(s3BucketName, outputFileName, aclDevFile)

      println(DateTime.now + " Writing the following to S3:\n" + simplifiedResults + "\n")
      s3Client.putObject(new PutObjectRequest(s3BucketName, simpleOutputFileName, createOutputFile(simpleOutputFileName, simplifiedResults)))
      val aclSimple: AccessControlList = s3Client.getObjectAcl(s3BucketName, simpleOutputFileName)
      aclSimple.grantPermission(GroupGrantee.AllUsers, Permission.Read)
      s3Client.setObjectAcl(s3BucketName, simpleOutputFileName, aclSimple)

    }
    else {
      val output: FileWriter = new FileWriter(outputFileName)
      val simplifiedOutput: FileWriter = new FileWriter(simpleOutputFileName)
      println(DateTime.now + " Writing the following to local file " + outputFileName + ":\n" + results)
      output.write(results)
      output.close()
      println(DateTime.now + " Writing to file: " + outputFileName + " complete. \n")
      simplifiedOutput.write(simplifiedResults)
      simplifiedOutput.close()
      println(DateTime.now + " Writing to file: " + simpleOutputFileName + " complete. \n")
    }
    println(DateTime.now + " The following records written to " + outputFileName + ":\n" + results)
    println(DateTime.now + " The following records written to " + simpleOutputFileName + ":\n" + simplifiedResults)
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
      val interactiveTestResults: List[List[String]] = interactiveUrls.map(url => testUrlReturnHtml(url, wptBaseUrl, wptApiKey, wptLocation, new PageAverageObject(1, 1, 1, 1, 1, 1.0, 1, 1, 1, 1, 1, 1, 1, 1.0, 1, 1, "<tr><td>Please add a list of overly large liveblog pages.</td></tr>"), warningColor, alertColor))
      // Add results to a single string so that we only need to write to S3 once (S3 will only take complete objects).
      val interactiveResultsList: List[String] = interactiveTestResults.map(x => x.head)
      val simplifiedResultsList : List[String] = interactiveTestResults.map(x => x.tail.head)

      interactiveResults = interactiveResults.concat(simplifiedResultsList.mkString)
      println(DateTime.now + " Results added to accumulator string \n")
    }
    interactiveResults = interactiveResults.concat(hTMLTableFooters)
    interactiveResults = interactiveResults.concat(hTMLPageFooterStart + DateTime.now + hTMLPageFooterEnd)

    if (!iamTestingLocally) {
      println(DateTime.now + " Writing the following to S3:\n" + interactiveResults + "\n")
      s3Client.putObject(new PutObjectRequest(s3BucketName, interactiveOutputFilename, createOutputFile(interactiveOutputFilename, interactiveResults)))
      val interactivesAclDevFile: AccessControlList = s3Client.getObjectAcl(s3BucketName, interactiveOutputFilename)
      interactivesAclDevFile.grantPermission(GroupGrantee.AllUsers, Permission.Read)
      s3Client.setObjectAcl(s3BucketName, interactiveOutputFilename, interactivesAclDevFile)
    }
    else {
      val interactiveOutput: FileWriter = new FileWriter(interactiveOutputFilename)
      println(DateTime.now + " Writing the following to local file: " + interactiveOutputFilename + ":\n" + interactiveResults)
      interactiveOutput.write(results)
      interactiveOutput.close()
      println(DateTime.now + " Writing to file: " + interactiveOutputFilename + " complete. \n")
    }
    println(DateTime.now + " Job complete")
  }


  def getS3Config(s3Client: AmazonS3Client, bucketName: String, configFileName: String): Config = {
    println("Obtaining configfile: " + configFileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucketName, configFileName))
    val objectData = s3Object.getObjectContent
    println("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString
    println("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)
    println("returning config object")
    conf
  }

  def createOutputFile(fileName: String, content: String): File = {
    println("creating output file")
    val file: File = File.createTempFile(fileName.takeWhile(_ != '.'), fileName.dropWhile(_ != '.'))
    file.deleteOnExit()
    val writer: Writer = new OutputStreamWriter(new FileOutputStream(file))
    writer.write(content)
    writer.close()
    println("returning File object")
    file
  }


  def testUrlReturnHtml(url: String, wptBaseUrl: String, wptApiKey: String, wptLocation: String, averages: PageAverageObject, warningColor: String, alertColor: String): List[String] = {
    var returnString: String = ""
    var simpleReturnString: String = ""
    //  Define new web-page-test API request and send it the url to test
    println(DateTime.now + " creating new WebPageTest object with this base URL: " + wptBaseUrl)
    val webpageTest: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    println(DateTime.now + " calling methods to test url: " + url + " on desktop")
    val webPageDesktopTestResults: webpageTest.ResultElement = webpageTest.desktopChromeCableTest(url)
    println(DateTime.now + " calling methods to test url: " + url + " on emulated 3G mobile")
    val webPageMobileTestResults: webpageTest.ResultElement = webpageTest.mobileChrome3GTest(url, wptLocation)
    //  Add results to string which will eventually become the content of our results file
    println(DateTime.now + " Adding results of desktop test to simple results string")
    returnString = returnString.concat("<tr><td>" + DateTime.now + "</td><td>Desktop</td>" + webPageDesktopTestResults.toHTMLTableCells() + "</tr>")
    returnString = returnString.concat("<tr><td>" + DateTime.now + "</td><td>Android/3G</td>" + webPageMobileTestResults.toHTMLTableCells() + "</tr>")

    if((webPageDesktopTestResults.timeDocComplete >= averages.desktopTimeDocComplete80thPercentile) ||
      (webPageDesktopTestResults.bytesInFullyLoaded >= averages.desktopKBInFullyLoaded80thPercentile) ||
      (webPageDesktopTestResults.costAt5CentsPerMB >= averages.desktopCostAt5CentsPerMB80thPercentile) ||
      (webPageDesktopTestResults.speedIndex >= averages.desktopSpeedIndex80thPercentile))
          {
            if((webPageDesktopTestResults.timeDocComplete >= averages.desktopTimeDocComplete) ||
              (webPageDesktopTestResults.bytesInFullyLoaded >= averages.desktopKBInFullyLoaded) ||
              (webPageDesktopTestResults.costAt5CentsPerMB >= averages.desktopCostAt5CentsPerMB) ||
              (webPageDesktopTestResults.speedIndex >= averages.desktopSpeedIndex))
              {
                println("row should be red one of the items qualifies")
                simpleReturnString = simpleReturnString.concat("<tr bgcolor=" + alertColor + "><td>" + DateTime.now + "</td><td>Desktop</td>" + webPageDesktopTestResults.toHTMLSimpleTableCells() + "</tr>")
              }
              else {
                      println("row should be yellow one of the items qualifies")
                      simpleReturnString = simpleReturnString.concat("<tr bgcolor=" + warningColor + "><td>" + DateTime.now + "</td><td>Desktop</td>" + webPageDesktopTestResults.toHTMLSimpleTableCells() + "</tr>")
            }
          }
    else
          {
            println("all fields within size limits")
            simpleReturnString = simpleReturnString.concat("<tr><td>" + DateTime.now + "</td><td>Desktop</td>" + webPageDesktopTestResults.toHTMLSimpleTableCells() + "</tr>")
          }
    println(DateTime.now + " Adding results of mobile test to simple results string")
    if((webPageMobileTestResults.timeDocComplete >= averages.mobileTimeDocComplete80thPercentile) ||
      (webPageMobileTestResults.bytesInFullyLoaded >= averages.mobileKBInFullyLoaded80thPercentile) ||
      (webPageMobileTestResults.costAt5CentsPerMB >= averages.mobileCostAt5CentsPerMB80thPercentile) ||
      (webPageMobileTestResults.speedIndex >= averages.mobileSpeedIndex80thPercentile))
          {
            if((webPageMobileTestResults.timeDocComplete >= averages.mobileTimeDocComplete80thPercentile) ||
              (webPageMobileTestResults.bytesInFullyLoaded >= averages.mobileKBInFullyLoaded80thPercentile) ||
              (webPageMobileTestResults.costAt5CentsPerMB >= averages.mobileCostAt5CentsPerMB80thPercentile) ||
              (webPageMobileTestResults.speedIndex >= averages.mobileSpeedIndex80thPercentile))
              {
                println("row should be red one of the items qualifies")
                simpleReturnString = simpleReturnString.concat("<tr>bgcolor=" + alertColor + "<td>" + DateTime.now + "</td><td>Android/3G</td>" + webPageMobileTestResults.toHTMLSimpleTableCells() + "</tr>")
              }
            else {
                println("row should be yellow one of the items qualifies")
                simpleReturnString = simpleReturnString.concat("<tr>bgcolor=" + warningColor + "<td>" + DateTime.now + "</td><td>Android/3G</td>" + webPageMobileTestResults.toHTMLSimpleTableCells() + "</tr>")
            }
          }
    else
          {

            println("row should be yellow one of the items qualifies")
            simpleReturnString = simpleReturnString.concat("<tr><td>" + DateTime.now + "</td><td>Android/3G</td>" + webPageMobileTestResults.toHTMLSimpleTableCells() + "</tr>")
          }
    println(DateTime.now + " returning results string to main thread")
    List(returnString, simpleReturnString)
  }

  def testRoguesGallery(urlList: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String ): PageAverageObject = {
    val webpageTest: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey)
    var returnString: String = ""

    var desktopTimeFirstPaint: Int = 0
    var desktopTimeDocComplete: Int = 0
    var desktopKBInDoccomplete: Int = 0
    var desktopTimeFullyLoaded: Int = 0
    var desktopKBInFullyLoaded: Int = 0
    var desktopCostAt5CentsPerMB: Double = 0
    var desktopSpeedIndex: Int = 0
    var desktopSuccessCount = 0

    var mobileTimeFirstPaint: Int = 0
    var mobileTimeDocComplete: Int = 0
    var mobileKBInDoccomplete: Int = 0
    var mobileTimeFullyLoaded: Int = 0
    var mobileKBInFullyLoaded: Int = 0
    var mobileCostAt5CentsPerMB: Double = 0
    var mobileSpeedIndex: Int = 0
    var mobileSuccessCount = 0

    def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s}

      urlList.foreach(url => {
      val webPageDesktopTestResults: webpageTest.ResultElement = webpageTest.desktopChromeCableTest(url)
      val webPageMobileTestResults: webpageTest.ResultElement = webpageTest.mobileChrome3GTest(url, wptLocation)
      if (webPageDesktopTestResults.resultStatus == "Test Success"){
        desktopTimeFirstPaint += webPageDesktopTestResults.timeFirstPaint/1000
        desktopTimeDocComplete += webPageDesktopTestResults.timeDocComplete/1000
        desktopKBInDoccomplete += webPageDesktopTestResults.bytesInDoccomplete/1000
        desktopTimeFullyLoaded += webPageDesktopTestResults.timeFullyLoaded/1000
        desktopKBInFullyLoaded += webPageDesktopTestResults.bytesInFullyLoaded/1000
        desktopCostAt5CentsPerMB += webPageDesktopTestResults.costAt5CentsPerMB
        desktopSpeedIndex += webPageDesktopTestResults.speedIndex
        desktopSuccessCount += 1

        mobileTimeFirstPaint += webPageMobileTestResults.timeFirstPaint/1000
        mobileTimeDocComplete += webPageMobileTestResults.timeDocComplete/1000
        mobileKBInDoccomplete += webPageMobileTestResults.bytesInDoccomplete/1000
        mobileTimeFullyLoaded += webPageMobileTestResults.timeFullyLoaded/1000
        mobileKBInFullyLoaded += webPageMobileTestResults.bytesInFullyLoaded/1000
        mobileCostAt5CentsPerMB += webPageMobileTestResults.costAt5CentsPerMB
        mobileSpeedIndex += webPageMobileTestResults.speedIndex
        mobileSuccessCount += 1
      }
    })
    returnString = returnString.concat("<tr bgcolor=\"#B0C0E0><td>" + DateTime.now + "</td><td>Desktop</td>")
    if(desktopSuccessCount > 1){
      returnString = returnString.concat("<td>" + "Average of " + desktopSuccessCount + " liveblogs that were migrated due to size </td>"
        + "<td>" + desktopTimeDocComplete/desktopSuccessCount + "s</td>"
        + "<td>" + desktopKBInFullyLoaded/desktopSuccessCount + "kB</td>"
        + "<td> $(US)" + roundAt(2)(desktopCostAt5CentsPerMB/desktopSuccessCount) + "</td>"
        + "<td>" + desktopSpeedIndex/desktopSuccessCount + "</td>"
        + "<td>" + desktopSuccessCount + " urls Tested Successfully</td></tr>"
      )}
      else{
      if (desktopSuccessCount == 1) {
        returnString = returnString.concat("<td>" + "Example of a liveblog migrated due to size </td>"
          + "<td>" + desktopTimeDocComplete + "s</td>"
          + "<td>" + desktopKBInFullyLoaded + "kB</td>"
          + "<td> $(US)" + desktopCostAt5CentsPerMB + "</td>"
          + "<td>" + desktopSpeedIndex + "</td>"
          + "<td>" + desktopSuccessCount + " urls Tested Successfully</td></tr>"
        )
      }
      else {
        returnString = returnString.concat("<td>" + "All tests of migrated liveblogs Failed </td>"
          + "<td>" + desktopTimeDocComplete + "s</td>"
          + "<td>" + desktopKBInFullyLoaded + "kB</td>"
          + "<td> $US" + desktopCostAt5CentsPerMB + "</td>"
          + "<td>" + desktopSpeedIndex + "</td>"
          + "<td>" + desktopSuccessCount + " urls Tested Successfully</td></tr>"
        )
      }
    }

    returnString = returnString.concat("<tr bgcolor=\"#B0C0E0\"><td>" + DateTime.now + "</td><td>Android/3G</td>")
    if(mobileSuccessCount > 1){
      returnString = returnString.concat("<td>" + "Average of " + mobileSuccessCount + " liveblogs that were migrated due to size </td>"
        + "<td>" + mobileTimeDocComplete/desktopSuccessCount + "s</td>"
        + "<td>" + mobileKBInFullyLoaded/desktopSuccessCount + "kB</td>"
        + "<td> $(US)" + roundAt(2)(desktopCostAt5CentsPerMB/desktopSuccessCount) + "</td>"
        + "<td>" + mobileSpeedIndex/desktopSuccessCount + "</td>"
        + "<td>" + mobileSuccessCount + " urls Tested Successfully</td></tr>"
      )}
    else{
      if (mobileSuccessCount == 1) {
        returnString = returnString.concat("<td>" + "Example of a liveblog migrated due to size </td>"
          + "<td>" + mobileTimeDocComplete + "s</td>"
          + "<td>" + mobileKBInFullyLoaded + "kB</td>"
          + "<td> $(US)" + mobileCostAt5CentsPerMB + "</td>"
          + "<td>" + mobileSpeedIndex + "</td>"
          + "<td>" + mobileSuccessCount + " urls Tested Successfully</td></tr>"
        )
      }
      else {
        returnString = returnString.concat("<td>" + "All tests of migrated liveblogs Failed </td>"
          + "<td>" + mobileTimeDocComplete + "s</td>"
          + "<td>" + mobileKBInFullyLoaded + "kB</td>"
          + "<td> $US" + mobileCostAt5CentsPerMB + "</td>"
          + "<td>" + mobileSpeedIndex + "</td>"
          + "<td>" + mobileSuccessCount + " urls Tested Successfully</td></tr>"
        )
      }
    }

    new PageAverageObject(
      desktopTimeFirstPaint,
      desktopTimeDocComplete,
      desktopKBInDoccomplete,
      desktopTimeFullyLoaded,
      desktopKBInFullyLoaded,
      desktopCostAt5CentsPerMB,
      desktopSpeedIndex,
      desktopSuccessCount,
      mobileTimeFirstPaint,
      mobileTimeDocComplete,
      mobileKBInDoccomplete,
      mobileTimeFullyLoaded,
      mobileKBInFullyLoaded,
      mobileCostAt5CentsPerMB,
      mobileSpeedIndex,
      mobileSuccessCount,
      returnString
    )
  }

  class PageAverageObject(dtfp: Int, dtdc: Int, dsdc: Int, dtfl: Int, dsfl: Int, dcfl: Double, dsi: Int, dsc: Int, mtfp: Int, mtdc: Int, msdc: Int, mtfl: Int, msfl: Int, mcfl: Double, msi: Int, msc: Int, resultString: String) {


    val desktopTimeFirstPaint: Int = dtfp
    val desktopTimeDocComplete: Int = dtdc
    val desktopKBInDoccomplete: Int = dsdc
    val desktopTimeFullyLoaded: Int = dtfl
    val desktopKBInFullyLoaded: Int = dsfl
    val desktopCostAt5CentsPerMB: Double = dcfl
    val desktopSpeedIndex: Int = dsi
    val desktopSuccessCount = dsc

    val mobileTimeFirstPaint: Int = mtfp
    val mobileTimeDocComplete: Int = mtdc
    val mobileKBInDocComplete: Int = msdc
    val mobileTimeFullyLoaded: Int = mtfl
    val mobileKBInFullyLoaded: Int = msfl
    val mobileCostAt5CentsPerMB: Double = mcfl
    val mobileSpeedIndex: Int = msi
    val mobileSuccessCount = msc

    val formattedHTMLResultString: String = resultString

    val desktopTimeFirstPaint80thPercentile: Int = (desktopTimeFirstPaint*80)/100
    val desktopTimeDocComplete80thPercentile: Int = (desktopTimeDocComplete*80)/100
    val desktopKBInDoccomplete80thPercentile: Int = (desktopKBInDoccomplete*80)/100
    val desktopTimeFullyLoaded80thPercentile: Int = (desktopTimeFullyLoaded*80)/100
    val desktopKBInFullyLoaded80thPercentile: Int = (desktopKBInFullyLoaded*80)/100
    val desktopCostAt5CentsPerMB80thPercentile: Double = (desktopCostAt5CentsPerMB*80)/100
    val desktopSpeedIndex80thPercentile: Int = (desktopSpeedIndex*80)/100
    val mobileTimeFirstPaint80thPercentile: Int = (mobileTimeFirstPaint*80)/100
    val mobileTimeDocComplete80thPercentile: Int = (mobileTimeDocComplete*80)/100
    val mobileKBInDocComplete80thPercentile: Int = (mobileKBInDocComplete*80)/100
    val mobileTimeFullyLoaded80thPercentile: Int = (mobileTimeFullyLoaded*80)/100
    val mobileKBInFullyLoaded80thPercentile: Int = (mobileKBInFullyLoaded*80)/100
    val mobileCostAt5CentsPerMB80thPercentile: Double = (mobileCostAt5CentsPerMB*80)/100
    val mobileSpeedIndex80thPercentile: Int = (mobileSpeedIndex*80)/100

    def toHTMLString:String = formattedHTMLResultString

  }
}

