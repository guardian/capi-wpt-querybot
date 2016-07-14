package app

// note an _ instead of {} would get everything

import java.io._
import java.util

import app.api._
import app.apiutils._
import com.gu.contentapi.client.model.v1.{Office, MembershipTier, CapiDateTime, ContentFields}
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime
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
    val visualsPagesFileName = "visuals.conf"

    val articleOutputFilename = "articleperformancedata.html"
    val liveBlogOutputFilename = "liveblogperformancedata.html"
    val interactiveOutputFilename = "interactiveperformancedata.html"
    val frontsOutputFilename = "frontsperformancedata.html"
    val editorialPageweightFilename = "editorialpageweightdashboard.html"
    val editorialDesktopPageweightFilename = "editorialpageweightdashboarddesktop.html"
    val editorialMobilePageweightFilename = "editorialpageweightdashboardmobile.html"

    val dotcomPageSpeedFilename = "dotcompagespeeddashboard.html"

    val interactiveDashboardFilename = "interactivedashboard.html"
    val interactiveDashboardDesktopFilename = "interactivedashboarddesktop.html"
    val interactiveDashboardMobileFilename = "interactivedashboardmobile.html"

    val articleResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + articleOutputFilename
    val liveBlogResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + liveBlogOutputFilename
    val interactiveResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + interactiveOutputFilename
    val frontsResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + frontsOutputFilename

    val articleCSVName = "accumulatedArticlePerformanceData.csv"
    val liveBlogCSVName = "accumulatedLiveblogPerformanceData.csv"
    val interactiveCSVName = "accumulatedInteractivePerformanceData.csv"
    val videoCSVName = "accumulatedVideoPerformanceData.csv"
    val audioCSVName = "accumulatedAudioPerformanceData.csv"
    val frontsCSVName = "accumulatedFrontsPerformanceData.csv"

    val resultsFromPreviousTests = "resultsFromPreviousTests.csv"


    val alertsThatHaveBeenFixed = "alertsthathavebeenfixed.csv"
    val duplicateResultList = "duplicateresultsfromlastrun.csv"


    //Define colors to be used for average values, warnings and alerts
    val averageColor: String = "#d9edf7"
    //    val warningColor: String = "#fcf8e3"
    val warningColor: String = "rgba(227, 251, 29, 0.32)"
    val alertColor: String = "#f2dede"

    //initialize combinedResultsLists - these will be used to sort and accumulate test results
    // for the combined page and for long term storage file
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

    //Initialize Page-Weight email alerts lists - these will be used to generate emails

    var articlePageWeightAlertList: List[PerformanceResultsObject] = List()
    var liveBlogPageWeightAlertList: List[PerformanceResultsObject] = List()
    var interactivePageWeightAlertList: List[PerformanceResultsObject] = List()
    var frontsPageWeightAlertList: List[PerformanceResultsObject] = List()
    var audioPageWeightAlertList: List[PerformanceResultsObject] = List()
    var videoPageWeightAlertList: List[PerformanceResultsObject] = List()

    var pageWeightAnchorId: Int = 0

    //Initialize Interactive email alerts lists - these will be used to generate emails
    var interactiveAlertList: List[PerformanceResultsObject] = List()

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
    var urlFragments: List[String] = List()

    //Get config settings
    println("Extracting configuration values")
    if (!iamTestingLocally) {
      println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
      val returnTuple = s3Interface.getConfig
      configArray = Array(returnTuple._1,returnTuple._2,returnTuple._3,returnTuple._4,returnTuple._5,returnTuple._6,returnTuple._7)
      urlFragments = returnTuple._8
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

    //Create Email Handler class
    val emailer: EmailOperations = new EmailOperations(emailUsername, emailPassword)

    //obtain list of interactive samples to determine average size
    //val listofLargeInteractives: List[String] = s3Interface.getUrls(interactiveSampleFileName)

    //obtain list of items previously alerted on
    val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
/*    val localInput = new LocalFileOperations
    val previousResults: List[PerformanceResultsObject] = localInput.getResultsFile(resultsFromPreviousTests)*/
    val previousTestResultsHandler = new ResultsFromPreviousTests(previousResults)
    println("\n\n\n ***** There are " +  previousTestResultsHandler.fullResultsList.length + " previous results in file  ********* \n\n\n")
    val previousResultsToRetest = previousTestResultsHandler.dedupedPreviousResultsToRestest
//    val previousResultsWithElementsAdded = previousTestResultsHandler.repairPreviousResultsList()

    //validate list handling
    val cutoffTime: Long = DateTime.now.minusHours(24).getMillis
    val visualPagesString: String = s3Interface.getVisualsFileFromS3(visualsPagesFileName)
    val jsonHandler: JSONOperations = new JSONOperations
 //   val visualPagesSeq: Seq[Visuals] = jsonHandler.stringToVisualsPages(visualPagesString)
    val visualPagesSeq: Seq[Visuals] = Seq()

    val untestedVisualsTeamPages: List[Visuals] = (for (visual <- visualPagesSeq if !previousResults.map(_.testUrl).contains(visual.pageUrl)) yield visual).toList
    val untestedVisualsTeamPagesFromToday: List[Visuals] = for (visual <- untestedVisualsTeamPages if visual.pageWebPublicationDate.dateTime >= cutoffTime) yield visual



    //  Define new CAPI Query object
    val capiQuery = new ArticleUrls(contentApiKey)
    //get all content-type-lists
    val articles: List[(Option[ContentFields],String)] = capiQuery.getUrlsForContentType("Article")
    val liveBlogs: List[(Option[ContentFields],String)] = capiQuery.getUrlsForContentType("LiveBlog")
    val interactives: List[(Option[ContentFields],String)] = capiQuery.getUrlsForContentType("Interactive")
    val fronts:  List[(Option[ContentFields],String)] = capiQuery.getUrlsForContentType("Front")
    val videoPages: List[(Option[ContentFields],String)] = capiQuery.getUrlsForContentType("Video")
    val audioPages: List[(Option[ContentFields],String)] = capiQuery.getUrlsForContentType("Audio")
    println(DateTime.now + " Closing Content API query connection")
    capiQuery.shutDown

    println("CAPI call summary: \n")
    println("Retrieved: " + articles.length + " article pages")
    println("Retrieved: " + liveBlogs.length + " liveblog pages")
    println("Retrieved: " + interactives.length + " intearactive pages")
    println("Retrieved: " + fronts.length + " fronts")
    println("Retrieved: " + videoPages.length + " video pages")
    println("Retrieved: " + audioPages.length + " audio pages")
    println((articles.length + liveBlogs.length + interactives.length + fronts.length + videoPages.length + audioPages.length) + " pages returned in total")

    val newOrChangedArticles = previousTestResultsHandler.returnPagesNotYetTested(articles)
    val newOrChangedLiveBlogs = previousTestResultsHandler.returnPagesNotYetTested(liveBlogs)
    val newOrChangedInteractives = previousTestResultsHandler.returnPagesNotYetTested(interactives)
    val newOrChangedVideoPages = previousTestResultsHandler.returnPagesNotYetTested(videoPages)
    val newOrChangedAudioPages = previousTestResultsHandler.returnPagesNotYetTested(audioPages)

    //val combinedCapiResults = articles ::: liveBlogs ::: interactives ::: fronts

 //todo - work in visuals list
 //   val visualsCapiResults = for(result <- combinedCapiResults if untestedVisualsTeamPagesFromToday.map(_.pageUrl).contains(result._2)) yield result
 //   val nonVisualsCapiResults = for(result <- combinedCapiResults if !untestedVisualsTeamPagesFromToday.map(_.pageUrl).contains(result._2)) yield result

 //   val nonCAPIResultsToRetest = for (result <- previousResultsToRetest if !combinedCapiResults.map(_._2).contains(result.testUrl)) yield result

//    val dedupedResultsToRetestUrls: List[String] = for (result <- nonCAPIResultsToRetest) yield result.testUrl
    val pagesToRetest: List[String] = previousResultsToRetest.map(_.testUrl)
    val articleUrls: List[String] = for (page <- newOrChangedArticles) yield page._2
    val liveBlogUrls: List[String] = for (page <- newOrChangedLiveBlogs) yield page._2
    val interactiveUrls: List[String] = for (page <- newOrChangedInteractives) yield page._2
    val frontsUrls: List[String] = for (page <- fronts) yield page._2
    val videoUrls: List[String] = for (page <- newOrChangedVideoPages) yield page._2
    val audioUrls: List[String] = for (page <- newOrChangedAudioPages) yield page._2

    //get all pages from the visuals team api


    // sendPageWeightAlert all urls to webpagetest at once to enable parallel testing by test agents
    val urlsToSend: List[String] = (pagesToRetest ::: articleUrls ::: liveBlogUrls ::: interactiveUrls).distinct
    println("Combined list of urls: \n" + urlsToSend)

    val resultUrlList: List[(String, String)] = getResultPages(urlsToSend, urlFragments, wptBaseUrl, wptApiKey, wptLocation)
    // build result page listeners
    // first format alerts from previous test that arent in the new capi queries
    val previousArticlesToRetest: List[PerformanceResultsObject] = for (result <- previousResultsToRetest if result.getPageType.contains("Article")) yield result
    val previousLiveBlogsToRetest: List[PerformanceResultsObject] = for (result <- previousResultsToRetest if result.getPageType.contains("LiveBlog")) yield result
    val previousInteractivesToRetest: List[PerformanceResultsObject] = for (result <- previousResultsToRetest if result.getPageType.contains("Interactive")) yield result

    // munge into proper format and merge these with the capi results
    val previousArticlesReTestContentFieldsAndUrl = previousArticlesToRetest.map(result => (Option(makeContentStub(result.headline, result.pageLastUpdated, result.liveBloggingNow)), result.testUrl))
    val previousLiveBlogReTestContentFieldsAndUrl = previousLiveBlogsToRetest.map(result => (Option(makeContentStub(result.headline, result.pageLastUpdated, result.liveBloggingNow)), result.testUrl))
    val previousInteractiveReTestContentFieldsAndUrl = previousInteractivesToRetest.map(result => (Option(makeContentStub(result.headline, result.pageLastUpdated, result.liveBloggingNow)), result.testUrl))

    val combinedArticleList: List[(Option[ContentFields],String)] = previousArticlesReTestContentFieldsAndUrl ::: newOrChangedArticles
    val combinedLiveBlogList: List[(Option[ContentFields],String)] = previousLiveBlogReTestContentFieldsAndUrl ::: newOrChangedLiveBlogs
    val combinedInteractiveList: List[(Option[ContentFields],String)] = previousInteractiveReTestContentFieldsAndUrl ::: newOrChangedInteractives


    //obtain results for articles
    if (combinedArticleList.nonEmpty) {
      println("Generating average values for articles")
      val articleAverages: PageAverageObject = new ArticleDefaultAverages(averageColor)
      articleResults = articleResults.concat(articleAverages.toHTMLString)

      val articleResultsList = listenForResultPages(combinedArticleList, "Article", resultUrlList, articleAverages, wptBaseUrl, wptApiKey, wptLocation, urlFragments)
      val getAnchorId: (List[PerformanceResultsObject], Int) = applyAnchorId(articleResultsList, pageWeightAnchorId)
      val articleResultsWithAnchor = getAnchorId._1
      pageWeightAnchorId = getAnchorId._2

      combinedResultsList = articleResultsWithAnchor
      println("\n \n \n article tests complete. \n tested " + articleResultsWithAnchor.length + "pages")
      println("Total number of results gathered so far " + combinedResultsList.length + "pages")

      println("About to sort article results list. Length of list is: " + articleResultsList.length)
      val sortedByWeightArticleResultsList = orderListByWeight(articleResultsWithAnchor)
      val sortedBySpeedArticleResultsList = orderListBySpeed(articleResultsWithAnchor)
      if(sortedByWeightArticleResultsList.isEmpty || sortedBySpeedArticleResultsList.isEmpty) {
        println("Sorting algorithm for articles has returned empty list. Aborting")
        System exit 1
      }
      val articleHTMLResults: List[String] = sortedByWeightArticleResultsList.map(x => htmlString.generateHTMLRow(x))
      // write article results to string
      //Create a list of alerting pages and write to string
      articlePageWeightAlertList = for (result <- sortedByWeightArticleResultsList if result.alertStatusPageWeight) yield result
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

    //obtain results for liveBlogs
    if (combinedLiveBlogList.nonEmpty) {
      println("Generating average values for liveblogs")
      val liveBlogAverages: PageAverageObject = new LiveBlogDefaultAverages(averageColor)
      liveBlogResults = liveBlogResults.concat(liveBlogAverages.toHTMLString)

      val liveBlogResultsList = listenForResultPages(combinedLiveBlogList, "LiveBlog", resultUrlList, liveBlogAverages, wptBaseUrl, wptApiKey, wptLocation, urlFragments)
      val getAnchorId: (List[PerformanceResultsObject], Int) = applyAnchorId(liveBlogResultsList, pageWeightAnchorId)
      val liveBlogResultsWithAnchor = getAnchorId._1
      pageWeightAnchorId = getAnchorId._2

      combinedResultsList = combinedResultsList ::: liveBlogResultsWithAnchor
      println("\n \n \n liveBlog tests complete. \n tested " + liveBlogResultsWithAnchor.length + "pages")
      println("Total number of results gathered so far " + combinedResultsList.length + "pages")
      val sortedLiveBlogResultsList = orderListByWeight(liveBlogResultsWithAnchor)
      if(sortedLiveBlogResultsList.isEmpty) {
        println("Sorting algorithm for Liveblogs has returned empty list. Aborting")
        System exit 1
      }
      val liveBlogHTMLResults: List[String] = sortedLiveBlogResultsList.map(x => htmlString.generateHTMLRow(x))
      // write liveblog results to string
      //Create a list of alerting pages and write to string
      liveBlogPageWeightAlertList = for (result <- sortedLiveBlogResultsList if result.alertStatusPageWeight) yield result

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

    if (combinedInteractiveList.nonEmpty) {
      println("Generating average values for interactives")
//      val interactiveAverages: PageAverageObject = generateInteractiveAverages(listofLargeInteractives, wptBaseUrl, wptApiKey, wptLocation, interactiveItemLabel, averageColor)
      val interactiveAverages: PageAverageObject = new InteractiveDefaultAverages(averageColor)
      interactiveResults = interactiveResults.concat(interactiveAverages.toHTMLString)

      val interactiveResultsList = listenForResultPages(combinedInteractiveList, "Interactive", resultUrlList, interactiveAverages, wptBaseUrl, wptApiKey, wptLocation, urlFragments)
      val getAnchorId: (List[PerformanceResultsObject], Int) = applyAnchorId(interactiveResultsList, pageWeightAnchorId)
      val interactiveResultsWithAnchor = getAnchorId._1
      pageWeightAnchorId = getAnchorId._2

      combinedResultsList = combinedResultsList ::: interactiveResultsWithAnchor
      println("\n \n \n interactive tests complete. \n tested " + interactiveResultsWithAnchor.length + "pages")
      println("Total number of results gathered so far " + combinedResultsList.length + "pages")
      val sortedInteractiveResultsList = orderListByWeight(interactiveResultsWithAnchor)
      if(sortedInteractiveResultsList.isEmpty) {
        println("Sorting algorithm has returned empty list. Aborting")
        System exit 1
      }
      val interactiveHTMLResults: List[String] = sortedInteractiveResultsList.map(x => htmlString.interactiveHTMLRow(x))
      //generate interactive alert message body
      interactivePageWeightAlertList = for (result <- sortedInteractiveResultsList if result.alertStatusPageWeight) yield result
      interactiveAlertList = for (result <- sortedInteractiveResultsList if result.alertStatusPageWeight || result.alertStatusPageSpeed) yield result
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

    /*if (frontsUrls.nonEmpty) {
      println("Generating average values for fronts")
      val frontsAverages: PageAverageObject = new FrontsDefaultAverages(averageColor)
      frontsResults = frontsResults.concat(frontsAverages.toHTMLString)

      val frontsResultsList = listenForResultPages(fronts, "Front", resultUrlList, frontsAverages, wptBaseUrl, wptApiKey, wptLocation, urlFragments)
      val getAnchorId: (List[PerformanceResultsObject], Int) = applyAnchorId(frontsResultsList, pageWeightAnchorId)
      val frontsResultsWithAnchor = getAnchorId._1
      pageWeightAnchorId = getAnchorId._2

      //      combinedResultsList = combinedResultsList ::: frontsResultsWithAnchor
      val sortedFrontsResultsList = orderListByWeight(frontsResultsWithAnchor)
      if(sortedFrontsResultsList.isEmpty) {
        println("Sorting algorithm for fronts has returned empty list. Aborting")
        System exit 1
      }
      val frontsHTMLResults: List[String] = sortedFrontsResultsList.map(x => htmlString.generateHTMLRow(x))
      //Create a list of alerting pages and write to string
      frontsPageWeightAlertList = for (result <- sortedFrontsResultsList if result.alertStatusPageWeight) yield result

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
    }*/

    val sortedByWeightCombinedResults: List[PerformanceResultsObject] = orderListByWeight(combinedResultsList :::  previousTestResultsHandler.recentButNoRetestRequired)
    val combinedDesktopResultsList: List[PerformanceResultsObject] = for (result <- sortedByWeightCombinedResults if result.typeOfTest.contains("Desktop")) yield result
    val combinedMobileResultsList: List[PerformanceResultsObject] = for (result <- sortedByWeightCombinedResults if result.typeOfTest.contains("Android/3G")) yield result
    println("\n \n \n Combining lists of results and sorting for dashboard pages.")
    println("length of sorted By Weight Combined List is: " + sortedByWeightCombinedResults.length)
    //Generate lists for sortByWeight combined pages

    val sortedByWeightCombinedDesktopResults: List[PerformanceResultsObject] = sortHomogenousResultsByWeight(combinedDesktopResultsList)
    val sortedCombinedByWeightMobileResults: List[PerformanceResultsObject] = sortHomogenousResultsByWeight(combinedMobileResultsList)

    println("length of sorted By Weight Mobile List is: " + sortedByWeightCombinedDesktopResults.length)
    println("length of sorted By Weight Combined List is: " + sortedCombinedByWeightMobileResults.length)
    //  strip out errors
    val errorFreeSortedByWeightCombinedResults = for (result <- sortedByWeightCombinedResults if result.speedIndex > 0) yield result

    val editorialPageWeightDashboardDesktop = new PageWeightDashboardDesktop(sortedByWeightCombinedResults, sortedByWeightCombinedDesktopResults, sortedCombinedByWeightMobileResults)
    val editorialPageWeightDashboardMobile = new PageWeightDashboardMobile(sortedByWeightCombinedResults, sortedByWeightCombinedDesktopResults, sortedCombinedByWeightMobileResults)
    val editorialPageWeightDashboard = new PageWeightDashboardTabbed(sortedByWeightCombinedResults, sortedByWeightCombinedDesktopResults, sortedCombinedByWeightMobileResults)

//record results
    val combinedResultsForFile = errorFreeSortedByWeightCombinedResults.filter(_.fullElementList.nonEmpty)
    val resultsToRecord = (combinedResultsForFile ::: previousTestResultsHandler.recentButNoRetestRequired ::: previousTestResultsHandler.oldResults).distinct
//val resultsToRecord = (combinedResultsForFile ::: previousResultsWithElementsAdded).distinct
    println("\n\n\n ***** There are " +  resultsToRecord.length + " results to be saved to the previous results file  ********* \n\n\n")
    val resultsToRecordCSVString: String = resultsToRecord.map(_.toCSVString()).mkString

//Generate Lists for sortBySpeed combined pages
    val sortedBySpeedCombinedResults: List[PerformanceResultsObject] = orderListBySpeed(combinedResultsList ::: previousTestResultsHandler.recentButNoRetestRequired)
    val sortedBySpeedCombinedDesktopResults: List[PerformanceResultsObject] = sortHomogenousResultsBySpeed(combinedDesktopResultsList ::: previousTestResultsHandler.recentButNoRetestRequired)
    val sortedBySpeedCombinedMobileResults: List[PerformanceResultsObject] = sortHomogenousResultsBySpeed(combinedMobileResultsList :::  previousTestResultsHandler.recentButNoRetestRequired)

//Generate Lists for interactive pages
    val combinedInteractiveResultsList = for (result <- combinedResultsList :::  previousTestResultsHandler.recentButNoRetestRequired  if (result.getPageType.contains("Interactive") || result.getPageType.contains("interactive"))) yield result
    val interactiveDesktopResults = for (result <- combinedDesktopResultsList :::  previousTestResultsHandler.recentButNoRetestRequired if (result.getPageType.contains("Interactive") || result.getPageType.contains("interactive"))) yield result
    val interactiveMobileResults = for (result <- combinedMobileResultsList :::  previousTestResultsHandler.recentButNoRetestRequired if (result.getPageType.contains("Interactive") || result.getPageType.contains("interactive"))) yield result

    val sortedInteractiveCombinedResults: List[PerformanceResultsObject] = orderInteractivesBySpeed(combinedInteractiveResultsList)
    val sortedInteractiveDesktopResults: List[PerformanceResultsObject] = sortHomogenousInteractiveResultsBySpeed(interactiveDesktopResults)
    val sortedInteractiveMobileResults: List[PerformanceResultsObject] = sortHomogenousInteractiveResultsBySpeed(interactiveMobileResults)

    val dotcomPageSpeedDashboard = new PageSpeedDashboardTabbed(sortedBySpeedCombinedResults, sortedBySpeedCombinedDesktopResults, sortedBySpeedCombinedMobileResults)
    val interactiveDashboard = new InteractiveDashboardTabbed(sortedInteractiveCombinedResults, sortedInteractiveDesktopResults, sortedInteractiveMobileResults)
    val interactiveDashboardDesktop = new InteractiveDashboardDesktop(sortedInteractiveCombinedResults, sortedInteractiveDesktopResults, sortedInteractiveMobileResults)
    val interactiveDashboardMobile = new InteractiveDashboardMobile(sortedInteractiveCombinedResults, sortedInteractiveDesktopResults, sortedInteractiveMobileResults)

    val pageWeightAlertsPreviouslyFixed = s3Interface.getResultsFileFromS3("alertsthathavebeenfixed.csv")
    val pageWeightAlertsFixedThisRun = for (alert <- previousTestResultsHandler.hasPreviouslyAlertedOnWeight if !(articlePageWeightAlertList ::: liveBlogPageWeightAlertList ::: interactivePageWeightAlertList).map(result => (result.testUrl, result.typeOfTest)).contains((alert.testUrl,alert.typeOfTest))) yield alert
    val pageWeightAlertsFixedCSVString = (pageWeightAlertsFixedThisRun ::: pageWeightAlertsPreviouslyFixed).map(_.toCSVString()).mkString

    val listOfDupes = for (result <- sortedByWeightCombinedResults if sortedByWeightCombinedResults.count(_ == result) > 1 ) yield result

    if(listOfDupes.nonEmpty) {
      println("\n\n\n\n ******** Duplicates found in results! ****** \n Found " + listOfDupes.length + " duplicates")
      println("Duplicate urls are: \n" + listOfDupes.map(result => "url: " + result.testUrl + " TestType: " + result.typeOfTest + "\n"))
    }

      //write combined results to file
      if (!iamTestingLocally) {
        println(DateTime.now + " Writing liveblog results to S3")
        s3Interface.writeFileToS3(editorialDesktopPageweightFilename, editorialPageWeightDashboardDesktop.toString())
        s3Interface.writeFileToS3(editorialMobilePageweightFilename, editorialPageWeightDashboardMobile.toString())
        s3Interface.writeFileToS3(editorialPageweightFilename, editorialPageWeightDashboard.toString())
        s3Interface.writeFileToS3(dotcomPageSpeedFilename, dotcomPageSpeedDashboard.toString())
        s3Interface.writeFileToS3(interactiveDashboardFilename, interactiveDashboard.toString())
        s3Interface.writeFileToS3(interactiveDashboardDesktopFilename, interactiveDashboardDesktop.toString())
        s3Interface.writeFileToS3(interactiveDashboardMobileFilename, interactiveDashboardMobile.toString())
        s3Interface.writeFileToS3(resultsFromPreviousTests, resultsToRecordCSVString)
        s3Interface.writeFileToS3(alertsThatHaveBeenFixed, pageWeightAlertsFixedCSVString)
        s3Interface.writeFileToS3(duplicateResultList, listOfDupes.map(_.toCSVString()).mkString)
      }
      else {
        val outputWriter = new LocalFileOperations
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
        val writeSuccessDCPSD: Int = outputWriter.writeLocalResultFile(dotcomPageSpeedFilename, dotcomPageSpeedDashboard.toString())
        if (writeSuccessDCPSD != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
        val writeSuccessIPSD: Int = outputWriter.writeLocalResultFile(interactiveDashboardFilename, interactiveDashboard.toString())
        if (writeSuccessIPSD != 0) {
          println("problem writing local outputfile")
          System exit 1
        }
        val writeSuccessAlertsRecord: Int = outputWriter.writeLocalResultFile(resultsFromPreviousTests, resultsToRecordCSVString)
        if (writeSuccessAlertsRecord != 0) {
          println("problem writing local outputfile")
          System exit 1
        }

      }



    //check if alert items have already been sent in earlier run
    val newArticlePageWeightAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOn(articlePageWeightAlertList)
    val newLiveBlogPageWeightAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOn(liveBlogPageWeightAlertList)
    val newInteractivePageWeightAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOn(interactivePageWeightAlertList)
//    val newFrontsPageWeightAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOn(frontsPageWeightAlertList)
    val newInteractiveAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOn(interactiveAlertList)

    val alertsToSend = newArticlePageWeightAlertsList ::: newLiveBlogPageWeightAlertsList ::: newInteractivePageWeightAlertsList
    if (alertsToSend.nonEmpty) {
      println("There are new pageWeight alerts to send! There are " + alertsToSend + " new alerts")
      val pageWeightEmailAlerts = new PageWeightEmailTemplate(alertsToSend, amazonDomain + "/" + s3BucketName + "/" + editorialMobilePageweightFilename, amazonDomain + "/" + s3BucketName + "/" + editorialDesktopPageweightFilename )
      val pageWeightEmailSuccess = emailer.sendPageWeightAlert(generalAlertsAddressList, pageWeightEmailAlerts.toString())
      if (pageWeightEmailSuccess)
        println(DateTime.now + " Page-Weight Alert Emails sent successfully. ")
      else
        println(DateTime.now + "ERROR: Sending of Page-Weight Alert Emails failed")
    }else {
      println("No pages to alert on Page-Weight. Email not sent.")
    }

    if (newInteractiveAlertsList.nonEmpty) {
      println("There are new interactive email alerts to send - length of list is: " + newInteractiveAlertsList.length)
      val interactiveEmailAlerts = new InteractiveEmailTemplate(newInteractiveAlertsList, amazonDomain + "/" + s3BucketName + "/" + interactiveDashboardMobileFilename, amazonDomain + "/" + s3BucketName + "/" + interactiveDashboardDesktopFilename )
      val interactiveEmailSuccess = emailer.sendInteractiveAlert(interactiveAlertsAddressList, interactiveEmailAlerts.toString())
      if (interactiveEmailSuccess) {
        println("Interactive Alert email sent successfully.")
      } else {
        println("ERROR: Sending of Interactive Alert Emails failed")
      }
    } else {
      println("no interactive alerts to send, therefore Interactive Alert Email not sent.")
    }
    println("Job complete")
  }

  def getResultPages(urlList: List[String], urlFragments: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String): List[(String, String)] = {
    val wpt: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val desktopResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.sendPage(page))
    })
    val mobileResults: List[(String, String)] = urlList.map(page => {
      (page, wpt.sendMobile3GPage(page, wptLocation))
    })
    desktopResults ::: mobileResults
  }

  def listenForResultPages(capiPages: List[(Option[ContentFields],String)], contentType: String, resultUrlList: List[(String, String)], averages: PageAverageObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String, urlFragments: List[String]): List[PerformanceResultsObject] = {
    println("ListenForResultPages called with: \n\n" +
      " List of Urls: \n" + capiPages.map(page => page._2).mkString +
      "\n\nList of WebPage Test results: \n" + resultUrlList.mkString +
      "\n\nList of averages: \n" + averages.toHTMLString + "\n")

    val listenerList: List[WptResultPageListener] = capiPages.flatMap(page => {
      for (element <- resultUrlList if element._1 == page._2) yield new WptResultPageListener(element._1, contentType, page._1, element._2)
    })

    println("Listener List created: \n" + listenerList.map(element => "list element: \n" + "url: " + element.pageUrl + "\n" + "resulturl" + element.wptResultUrl + "\n"))

    val resultsList: ParSeq[WptResultPageListener] = listenerList.par.map(element => {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
      val newElement = new WptResultPageListener(element.pageUrl, element.pageType, element.pageFields,element.wptResultUrl)
      println("getting result for page element")
      newElement.testResults = wpt.getResults(newElement.wptResultUrl)
      println("result received\n setting headline")
      newElement.testResults.setHeadline(newElement.headline)
      println("headline set\n setting pagetype")
      newElement.testResults.setPageType(newElement.pageType)
      println("pagetype set\n setting FirstPublished")
      newElement.testResults.setFirstPublished(newElement.firstPublished)
      println("FirstPublished set\n setting LastUpdated")
      newElement.testResults.setPageLastUpdated(newElement.pageLastModified)
      println("Lastupdated set\n setting LiveBloggingNow")
      newElement.testResults.setLiveBloggingNow(newElement.liveBloggingNow.getOrElse(false))
      println("all variables set for element")
      newElement
    })
    val testResults = resultsList.map(element => element.testResults).toList
    val resultsWithAlerts: List[PerformanceResultsObject] = testResults.map(element => setAlertStatus(element, averages))

    //Confirm alert status by retesting alerting urls
    println("Confirming any items that have an alert")
    val confirmedTestResults = resultsWithAlerts.map(x => {
      if (x.alertStatusPageWeight || (x.timeFirstPaintInMs == -1)) {
        val confirmedResult: PerformanceResultsObject = confirmAlert(x, averages, urlFragments, wptBaseUrl, wptApiKey ,wptLocation)
        confirmedResult.headline = x.headline
        confirmedResult.pageType = x.pageType
        confirmedResult.firstPublished = x.firstPublished
        confirmedResult.pageLastUpdated = x.pageLastUpdated
        confirmedResult.liveBloggingNow = x.liveBloggingNow
        confirmedResult
      }
      else
        x
    })
    confirmedTestResults
  }

  def confirmAlert(initialResult: PerformanceResultsObject, averages: PageAverageObject, urlFragments: List[String],wptBaseUrl: String, wptApiKey: String, wptLocation: String): PerformanceResultsObject = {
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
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
      if (resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded) {
        println("PageWeight Alert Set")
        resultObject.pageWeightAlertDescription = "the page is too heavy. Please examine the list of embeds below for items that are unexpectedly large."
        resultObject.alertStatusPageWeight = true
      }
      else {
        println("PageWeight Alert not set")
        resultObject.alertStatusPageWeight = false
      }
      if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) ||
          (resultObject.speedIndex >= averages.desktopSpeedIndex)) {
        println("PageSpeed alert set")
        resultObject.alertStatusPageSpeed = true
        if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) && (resultObject.speedIndex >= averages.desktopSpeedIndex)) {
          resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) and time till page looks loaded (SpeedIndex) are unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
        } else {
          if (resultObject.speedIndex >= averages.desktopSpeedIndex) {
            resultObject.pageSpeedAlertDescription = "Time till page looks loaded (SpeedIndex) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
          else {
            resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
        }
      } else {
        println("PageSpeed alert not set")
        resultObject.alertStatusPageSpeed = false
      }
    } else {
      //checking if status of mobile test needs an alert
      if (resultObject.kBInFullyLoaded >= averages.mobileKBInFullyLoaded) {
        println("PageWeight Alert Set")
        resultObject.pageWeightAlertDescription = "the page is too heavy. Please examine the list of embeds below for items that are unexpectedly large."
        resultObject.alertStatusPageWeight = true
      }
      else {
        println("PageWeight Alert not set")
        resultObject.alertStatusPageWeight = false
      }
      if ((resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) ||
        (resultObject.speedIndex >= averages.mobileSpeedIndex)) {
        println("PageSpeed alert set")
        resultObject.alertStatusPageSpeed = true
        if ((resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) && (resultObject.speedIndex >= averages.mobileSpeedIndex)) {
          resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) and time till page looks loaded (SpeedIndex) are unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
        } else {
          if (resultObject.speedIndex >= averages.mobileSpeedIndex) {
            resultObject.pageSpeedAlertDescription = "Time till page looks loaded (SpeedIndex) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
          else {
            resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
        }
      } else {
        println("PageSpeed alert not set")
        resultObject.alertStatusPageSpeed = false
      }
    }
    println("Returning test result with alert flags set to relevant values")
    resultObject
  }

  def generateInteractiveAverages(urlList: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String, urlFragments: List[String], itemtype: String, averageColor: String): PageAverageObject = {
    val setHighPriority: Boolean = true
    val webpageTest: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)

    val resultsList: List[Array[PerformanceResultsObject]] = urlList.map(url => {
      val webPageDesktopTestResults: PerformanceResultsObject = webpageTest.desktopChromeCableTest(url, setHighPriority)
      val webPageMobileTestResults: PerformanceResultsObject = webpageTest.mobileChrome3GTest(url, wptLocation, setHighPriority)
      val combinedResults = Array(webPageDesktopTestResults, webPageMobileTestResults)
      combinedResults
    })

    val pageAverages: PageAverageObject = new GeneratedInteractiveAverages(resultsList, averageColor)
    pageAverages
  }


  def retestUrl(initialResult: PerformanceResultsObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String, urlFragments: List[String]): PerformanceResultsObject = {
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val testCount: Int = if (initialResult.timeToFirstByte > 1000) {
      5
    } else {
      3
    }
    println("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
    //   val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatusPageWeight(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount)
  }

  //This is to resolve issues where there is a missing Desktop or Mobile test so the tuple sorting gets borked - it wont give a perfect sort in this case, but better than the current state of things.
  def returnValidListOfPairs(list: List[PerformanceResultsObject]): (List[PerformanceResultsObject],List[PerformanceResultsObject]) = {
    val desktopList = for (result <- list if result.typeOfTest.contains("Desktop")) yield result
    val mobileList = for (result <- list if result.typeOfTest.contains("Android/3G")) yield result
    val missingFromDesktop = for (result <- mobileList if!desktopList.map(_.testUrl).contains(result.testUrl)) yield result
    val missingFromMobile = for (result <- desktopList if!mobileList.map(_.testUrl).contains(result.testUrl)) yield result
    val validListOfPairs = for (result <- list if(!missingFromDesktop.map(_.testUrl).contains(result.testUrl)) && (!missingFromMobile.map(_.testUrl).contains(result.testUrl))) yield result
    println("list has been validated")
    (validListOfPairs, missingFromDesktop ::: missingFromMobile)
  }

  def orderListByWeight(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    println("orderListByWeightCalled with " + list.length + "elements")
      val validatedList = returnValidListOfPairs(list)
      println("validated list has " + validatedList._1.length + " paired items, and " + validatedList._2.length + " leftover items")
      val tupleList = listSinglesToPairs(validatedList._1)
      println("tuple List returned " + tupleList.length + " tuples")
      val leftOverAlerts = for (result <- validatedList._2 if result.alertStatusPageWeight) yield result
      val leftOverNormal = for (result <- validatedList._2 if !result.alertStatusPageWeight) yield result
      println("listSinglesToPairs returned a list of " + tupleList.length + " pairs.")
      val alertsList: List[(PerformanceResultsObject, PerformanceResultsObject)] = (for (element <- tupleList if element._1.alertStatusPageWeight || element._2.alertStatusPageWeight) yield element)
      val okList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if !element._1.alertStatusPageWeight && !element._2.alertStatusPageWeight) yield element

      sortByWeight(alertsList) ::: leftOverAlerts ::: sortByWeight(okList) ::: leftOverNormal
  }

  def orderListBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
      println("orderListBySpeed called. \n It has " + list.length + " elements.")
      val validatedList = returnValidListOfPairs(list)
    println("validated list has " + validatedList._1.length + " paired items, and " + validatedList._2.length + " leftover items")
      val tupleList = listSinglesToPairs(validatedList._1)
      println("tuple List returned " + tupleList.length + " tuples")
      val leftOverAlerts = for (result <- validatedList._2 if result.alertStatusPageSpeed) yield result
      val leftOverNormal = for (result <- validatedList._2 if !result.alertStatusPageSpeed) yield result
      println("listSinglesToPairs returned a list of " + tupleList.length + " pairs.")
      val alertsList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if element._1.alertStatusPageSpeed || element._2.alertStatusPageSpeed) yield element
      val okList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if !element._1.alertStatusPageSpeed && !element._2.alertStatusPageSpeed) yield element

      sortBySpeed(alertsList) ::: leftOverAlerts ::: sortBySpeed(okList) ::: leftOverNormal
  }

  def orderInteractivesBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
      println("orderInteractivesBySpeed called. \n It has " + list.length + " elements.")
      val validatedList = returnValidListOfPairs(list)
      val tupleList = listSinglesToPairs(validatedList._1)
      val leftOverAlerts = for (result <- validatedList._2 if result.alertStatusPageSpeed) yield result
      val leftOverNormal = for (result <- validatedList._2 if !result.alertStatusPageSpeed) yield result
      println("listSinglesToPairs returned a list of " + tupleList.length + " pairs.")
      val alertsList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if element._1.alertStatusPageSpeed || element._2.alertStatusPageSpeed || element._1.alertStatusPageWeight || element._2.alertStatusPageWeight) yield element
      val okList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if !element._1.alertStatusPageSpeed && !element._2.alertStatusPageSpeed && !element._1.alertStatusPageWeight && !element._2.alertStatusPageWeight) yield element

      sortBySpeed(alertsList) ::: leftOverAlerts ::: sortBySpeed(okList) ::: leftOverNormal
  }

  def sortHomogenousResultsByWeight(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val alertsResultsList: List[PerformanceResultsObject] = for (result <- list if result.alertStatusPageWeight) yield result
    val okResultsList: List[PerformanceResultsObject] = for (result <- list if !result.alertStatusPageWeight) yield result
    val sortedAlertList: List[PerformanceResultsObject] = alertsResultsList.sortWith(_.bytesInFullyLoaded > _.bytesInFullyLoaded)
    val sortedOkList: List[PerformanceResultsObject] = okResultsList.sortWith(_.bytesInFullyLoaded > _.bytesInFullyLoaded)
    sortedAlertList ::: sortedOkList
  }

  def sortHomogenousResultsBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val alertsResultsList: List[PerformanceResultsObject] = for (result <- list if result.alertStatusPageSpeed) yield result
    val okResultsList: List[PerformanceResultsObject] = for (result <- list if !result.alertStatusPageSpeed) yield result
    val sortedAlertList: List[PerformanceResultsObject] = alertsResultsList.sortWith(_.speedIndex > _.speedIndex)
    val sortedOkList: List[PerformanceResultsObject] = okResultsList.sortWith(_.speedIndex > _.speedIndex)
    sortedAlertList ::: sortedOkList
  }

  def sortHomogenousInteractiveResultsBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val alertsResultsList: List[PerformanceResultsObject] = for (result <- list if result.alertStatusPageSpeed || result.alertStatusPageWeight) yield result
    val okResultsList: List[PerformanceResultsObject] = for (result <- list if !result.alertStatusPageSpeed && !result.alertStatusPageWeight) yield result
    val sortedAlertList: List[PerformanceResultsObject] = alertsResultsList.sortWith(_.speedIndex > _.speedIndex)
    val sortedOkList: List[PerformanceResultsObject] = okResultsList.sortWith(_.speedIndex > _.speedIndex)
    sortedAlertList ::: sortedOkList
  }

  def sortByWeight(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[PerformanceResultsObject] = {
    if(list.nonEmpty){
      val sortedTupleList = sortTupleListByWeight(list)
      makeList(sortedTupleList)
    }
    else {
        println("sortByWeight has noElements in list. Passing back empty list")
        List()
    }
  }

  def sortBySpeed(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[PerformanceResultsObject] = {
    if(list.nonEmpty){
      val sortedTupleList = sortTupleListBySpeed(list)
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
      if (list.isEmpty) {
        println("listSinglesToPairs passed empty list. Returning empty list of correct type")
        val returnList: List[(PerformanceResultsObject, PerformanceResultsObject)] = List()
        returnList
      } else {
        if(list.length == 1){
          println("listSinglesToPairs passed list of 1. Returning tuple of single element - will introduce a duplicate result")
          val dummyTestType = if(list.head.typeOfTest.contains("Desktop")){
            "Android/3G"
          } else {
            "Desktop"
          }
          List((list.head, new PerformanceResultsObject("Missing test of type", dummyTestType, " for the above url", -1, -1, -1, -1, -1, -1, -1, "", false, false, true)))
        } else {
          println("listSinglesToPairs has been passed an empty or odd number of elements: list has " + list.length + "elements")
          makeTuple(List((list.head, list.tail.head)), list.tail.tail)
        }
      }
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

  def sortTupleListByWeight(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[(PerformanceResultsObject,PerformanceResultsObject)] = {
    list.sortWith{(leftE:(PerformanceResultsObject, PerformanceResultsObject),rightE:(PerformanceResultsObject, PerformanceResultsObject)) =>
      leftE._1.bytesInFullyLoaded + leftE._2.bytesInFullyLoaded > rightE._1.bytesInFullyLoaded + rightE._2.bytesInFullyLoaded}
  }

  def sortTupleListBySpeed(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[(PerformanceResultsObject,PerformanceResultsObject)] = {
    list.sortWith{(leftE:(PerformanceResultsObject, PerformanceResultsObject),rightE:(PerformanceResultsObject, PerformanceResultsObject)) =>
      leftE._1.speedIndex + leftE._2.speedIndex > rightE._1.speedIndex + rightE._2.speedIndex}
  }

  def applyAnchorId(resultsObjectList: List[PerformanceResultsObject], lastIDAssigned: Int): (List[PerformanceResultsObject], Int) = {
    var iterator = lastIDAssigned + 1
    val resultList = for (result <- resultsObjectList) yield {
      result.anchorId = Option(result.headline.getOrElse(iterator) + result.typeOfTest)
      iterator = iterator + 1
      result
    }
    (resultList,iterator)
  }

  def makeContentStub(passedHeadline: Option[String], passedLastModified: Option[CapiDateTime], passedLiveBloggingNow: Option[Boolean]): ContentFields = {
    val contentStub = new ContentFields {override def newspaperEditionDate: Option[CapiDateTime] = None

      override def internalStoryPackageCode: Option[Int] = None

      override def displayHint: Option[String] = None

      override def legallySensitive: Option[Boolean] = None

      override def creationDate: Option[CapiDateTime] = None

      override def shouldHideAdverts: Option[Boolean] = None

      override def wordcount: Option[Int] = None

      override def thumbnail: Option[String] = None

      override def liveBloggingNow: Option[Boolean] = passedLiveBloggingNow

      override def showInRelatedContent: Option[Boolean] = None

      override def internalComposerCode: Option[String] = None

      override def lastModified: Option[CapiDateTime] = passedLastModified

      override def byline: Option[String] = None

      override def isInappropriateForSponsorship: Option[Boolean] = None

      override def commentable: Option[Boolean] = None

      override def trailText: Option[String] = None

      override def internalPageCode: Option[Int] = None

      override def main: Option[String] = None

      override def body: Option[String] = None

      override def productionOffice: Option[Office] = None

      override def newspaperPageNumber: Option[Int] = None

      override def shortUrl: Option[String] = None

      override def publication: Option[String] = None

      override def secureThumbnail: Option[String] = None

      override def contributorBio: Option[String] = None

      override def firstPublicationDate: Option[CapiDateTime] = None

      override def isPremoderated: Option[Boolean] = None

      override def membershipAccess: Option[MembershipTier] = None

      override def scheduledPublicationDate: Option[CapiDateTime] = None

      override def starRating: Option[Int] = None

      override def hasStoryPackage: Option[Boolean] = None

      override def headline: Option[String] = passedHeadline

      override def commentCloseDate: Option[CapiDateTime] = None

      override def internalOctopusCode: Option[String] = None

      override def standfirst: Option[String] = None
    }
  contentStub
  }

  def jodaDateTimetoCapiDateTime(time: DateTime): CapiDateTime = {
    new CapiDateTime {
      override def dateTime: Long = time.getMillis
    }
  }

}


