package services


import com.gu.contentapi.client.model.v1._
import org.joda.time.DateTime
import play.api.Logger
import services.api._
import services.apiutils._

import scala.collection.parallel.immutable.ParSeq


object Querybot {
  def run(args: Array[String]) {
    /*  This value stops the forces the config to be read and the output file to be written locally rather than reading and writing from/to S3
    #####################    this should be set to false before merging!!!!################*/
    val iamTestingLocally = false
    /*#####################################################################################*/
    Logger.info("Job started at: " + DateTime.now)
    Logger.info("Local Testing Flag is set to: " + iamTestingLocally.toString)

    val jobStart = DateTime.now
    //  Define names of s3bucket, configuration and output Files
    val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
    val s3BucketName = "capi-wpt-querybot"
    val configFileName = "config.conf"
    val emailFileName = "addresses.conf"

    val articleOutputFilename = "articleperformancedata.html"
    val liveBlogOutputFilename = "liveblogperformancedata.html"
    val interactiveOutputFilename = "interactiveperformancedata.html"
    val frontsOutputFilename = "frontsperformancedata.html"
    val editorialPageweightFilename = "editorialpageweightdashboard.html"
    val editorialDesktopPageweightFilename = "editorialpageweightdashboarddesktop.html"
    val editorialMobilePageweightFilename = "editorialpageweightdashboardmobile.html"
    val currentDataSummaryPage = "summarypage.html"
    val currentPageWeightAlertSummaryPage = "pageweightalertsummarypage.html"
    val currentInteractiveSummaryPage = "interactivesummarypage.html"
    val currentWeeklySummaryPage = "weeklysummarypage.html"

    val dotcomPageSpeedFilename = "dotcompagespeeddashboard.html"

    val interactiveDashboardFilename = "interactivedashboard.html"
    val interactiveDashboardDesktopFilename = "interactivedashboarddesktop.html"
    val interactiveDashboardMobileFilename = "interactivedashboardmobile.html"

    val gLabsDashboardFilename = "glabs.html"
    val gLabsDashboardDesktopFilename = "glabsdashboarddesktop.html"
    val gLabsDashboardMobileFilename = "glabsdashboardmobile.html"

    val articleResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + articleOutputFilename
    val liveBlogResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + liveBlogOutputFilename
    val interactiveResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + interactiveOutputFilename
    val frontsResultsUrl: String = amazonDomain + "/" + s3BucketName + "/" + frontsOutputFilename

    val resultsFromPreviousTests = "resultsFromPreviousTests.csv"
    val pageWeightAlertsFromPreviousTests = "alerts/pageWeightAlertsFromPreviousTests.csv"
    val interactiveAlertsFromPreviousTests = "alerts/interactiveAlertsFromPreviousTests.csv"

    val alertsThatHaveBeenFixed = "alertsthathavebeenfixed.csv"
    val pagesWithInsecureElements = "pagesWithInsecureElements.csv"
    val duplicateResultList = "duplicateresultsfromlastrun.csv"
    val runLog = "runLog.csv"

    // summary files

    val jobStartHour = jobStart.hourOfDay.getAsString
    val jobStartDayOfWeek = jobStart.dayOfWeek.getAsString

    val runSummaryFile = "runLogs/runSummary" + jobStartDayOfWeek + "_" + jobStartHour + ".txt"
    val pageWeightAlertSummaryFile = "runLogs/pageWeightAlertSummary" + jobStartDayOfWeek + "_" + jobStartHour + ".txt"
    val interactiveAlertSummaryFile = "runLogs/interactiveAlertSummary" + jobStartDayOfWeek + "_" + jobStartHour + ".txt"


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
    var articleResults: String = htmlString.initialisePageForLiveblog + htmlString.initialiseTable
    var liveBlogResults: String = htmlString.initialisePageForLiveblog + htmlString.initialiseTable
    var interactiveResults: String = htmlString.initialisePageForInteractive + htmlString.interactiveTable


    //Initialize Page-Weight email alerts lists - these will be used to generate emails
    var articlePageWeightAlertList: List[PerformanceResultsObject] = List()
    var liveBlogPageWeightAlertList: List[PerformanceResultsObject] = List()
    var interactivePageWeightAlertList: List[PerformanceResultsObject] = List()
    var pageWeightAnchorId: Int = 0


    //Initialize Interactive email alerts lists - these will be used to generate emails
    var interactiveAlertList: List[PerformanceResultsObject] = List()
    var gLabsAlertList: List[PerformanceResultsObject] = List()


    //Create new S3 Client
    Logger.info("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
    val s3Interface = new S3Operations(s3BucketName, configFileName, emailFileName)
    var configArray: Array[String] = Array("", "", "", "", "", "")
    var urlFragments: List[String] = List()

    //Get config settings
    Logger.info("Extracting configuration values")
    if (!iamTestingLocally) {
      Logger.info(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
      val returnTuple = s3Interface.getConfig
      configArray = Array(returnTuple._1, returnTuple._2, returnTuple._3, returnTuple._4, returnTuple._5, returnTuple._6, returnTuple._7)
      urlFragments = returnTuple._8
    }
    else {
      Logger.info(DateTime.now + " retrieving local config file: " + configFileName)
      val configReader = new LocalFileOperations
      configArray = configReader.readInConfig(configFileName)
    }
    Logger.info("checking validity of config values")
    if ((configArray(0).length < 1) || (configArray(1).length < 1) || (configArray(2).length < 1) || (configArray(3).length < 1)) {
      Logger.info("problem extracting config\n" +
        "contentApiKey length: " + configArray(0).length + "\n" +
        "wptBaseUrl length: " + configArray(1).length + "\n" +
        "wptApiKey length: " + configArray(2).length + "\n" +
        "wptLocation length: " + configArray(3).length + "\n" +
        "emailUsername length: " + configArray(4).length + "\n" +
        "emailPassword length: " + configArray(5).length) + "\n" +
        "visuals URL length: " + configArray(6).length

      System exit 1
    }
    Logger.info("config values ok")
    val contentApiKey: String = configArray(0)
    val wptBaseUrl: String = configArray(1)
    val wptApiKey: String = configArray(2)
    val wptLocation: String = configArray(3)
    val emailUsername: String = configArray(4)
    val emailPassword: String = configArray(5)

    //obtain list of email addresses for alerting
    val emailAddresses: Array[List[String]] = s3Interface.getEmailAddresses
    val generalAlertsAddressList: List[String] = emailAddresses(0)
    val ukInteractiveAlertsAddressList: List[String] = emailAddresses(1)
    val usInteractiveAlertsAddressList: List[String] = emailAddresses(2)
    val auInteractiveAlertsAddressList: List[String] = emailAddresses(3)
    val globalGLabsAlertsAddressList: List[String] = emailAddresses(4)
    val ukGLabsAlertsAddressList: List[String] = emailAddresses(5)
    val usGLabsAlertsAddressList: List[String] = emailAddresses(6)
    val auGLabsAlertsAddressList: List[String] = emailAddresses(7)

    //Create Email Handler class
    val emailer: EmailOperations = new EmailOperations(emailUsername, emailPassword)

    //obtain list of interactive samples to determine average size
    //val listofLargeInteractives: List[String] = s3Interface.getUrls(interactiveSampleFileName)

    //obtain list of items previously alerted on
    val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests).take(5000)
    /*    val localInput = new LocalFileOperations
    val previousResults: List[PerformanceResultsObject] = localInput.getResultsFile(resultsFromPreviousTests)*/
    val previousTestResultsHandler = new ResultsFromPreviousTests(previousResults)
    Logger.info("\n\n\n ***** There are " + previousTestResultsHandler.previousResults.length + " previous results in file  ********* \n\n\n")
    val previousResultsToRetest = previousTestResultsHandler.dedupedPreviousResultsToRestest
    //    val previousResultsWithElementsAdded = previousTestResultsHandler.repairPreviousResultsList()
    val previousPageWeightAlerts: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(pageWeightAlertsFromPreviousTests)
    val previousInteractiveAlerts: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(interactiveAlertsFromPreviousTests)
    val previousPagesWithInsecureElements: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(pagesWithInsecureElements)


    //  Define new CAPI Query object
    val capiQuery = new ArticleUrls(contentApiKey)
    //get all content-type-lists
    val articles: List[(Option[ContentFields], Seq[Tag], String, Option[String])] = capiQuery.getUrlsForContentType("Article")
    val liveBlogs: List[(Option[ContentFields], Seq[Tag], String, Option[String])] = capiQuery.getUrlsForContentType("LiveBlog")
    val interactives: List[(Option[ContentFields], Seq[Tag], String, Option[String])] = capiQuery.getUrlsForContentType("Interactive")
    val fronts: List[(Option[ContentFields], Seq[Tag], String, Option[String])] = capiQuery.getUrlsForContentType("Front")
    val videoPages: List[(Option[ContentFields], Seq[Tag], String, Option[String])] = capiQuery.getUrlsForContentType("Video")
    val audioPages: List[(Option[ContentFields], Seq[Tag], String, Option[String])] = capiQuery.getUrlsForContentType("Audio")
    Logger.info(DateTime.now + " Closing Content API query connection")

    Logger.info("CAPI call summary: \n")
    Logger.info("Retrieved: " + articles.length + " article pages")
    Logger.info("Retrieved: " + liveBlogs.length + " liveblog pages")
    Logger.info("Retrieved: " + interactives.length + " intearactive pages")
    Logger.info("Retrieved: " + fronts.length + " fronts")
    Logger.info("Retrieved: " + videoPages.length + " video pages")
    Logger.info("Retrieved: " + audioPages.length + " audio pages")
    Logger.info((articles.length + liveBlogs.length + interactives.length + fronts.length + videoPages.length + audioPages.length) + " pages returned in total")

    capiQuery.shutDown()

    val newOrChangedArticles = previousTestResultsHandler.returnPagesNotYetTested(articles)
    val newOrChangedLiveBlogs = previousTestResultsHandler.returnPagesNotYetTested(liveBlogs)
    val newOrChangedInteractives = previousTestResultsHandler.returnPagesNotYetTested(interactives)

    val pagesToRetest: List[String] = previousResultsToRetest.map(_.testUrl)
    val articleUrls: List[String] = for (page <- newOrChangedArticles) yield page._3
    val liveBlogUrls: List[String] = for (page <- newOrChangedLiveBlogs) yield page._3
    val interactiveUrls: List[String] = for (page <- newOrChangedInteractives) yield page._3

    // sendPageWeightAlert all urls to webpagetest at once to enable parallel testing by test agents
    val urlsToSend: List[String] = (pagesToRetest ::: articleUrls ::: liveBlogUrls ::: interactiveUrls).distinct
    Logger.info("Combined list of urls: \n" + urlsToSend)

    val resultUrlList: List[(String, String)] = getResultPages(urlsToSend, urlFragments, wptBaseUrl, wptApiKey, wptLocation)
    // build result page listeners
    // first format alerts from previous test that arent in the new capi queries
    val previousArticlesToRetest: List[PerformanceResultsObject] = for (result <- previousResultsToRetest if result.getPageType.contains("Article")) yield result
    val previousLiveBlogsToRetest: List[PerformanceResultsObject] = for (result <- previousResultsToRetest if result.getPageType.contains("LiveBlog")) yield result
    val previousInteractivesToRetest: List[PerformanceResultsObject] = for (result <- previousResultsToRetest if result.getPageType.contains("Interactive")) yield result

    // munge into proper format and merge these with the capi results
    val previousArticlesReTestContentFieldsAndUrl = previousArticlesToRetest.map(result => (Option(makeContentStub(result.headline, result.pageLastUpdated, result.liveBloggingNow)), getTagList(result.gLabs), result.testUrl, result.createdBy))
    val previousLiveBlogReTestContentFieldsAndUrl = previousLiveBlogsToRetest.map(result => (Option(makeContentStub(result.headline, result.pageLastUpdated, result.liveBloggingNow)), getTagList(result.gLabs), result.testUrl, result.createdBy))
    val previousInteractiveReTestContentFieldsAndUrl = previousInteractivesToRetest.map(result => (Option(makeContentStub(result.headline, result.pageLastUpdated, result.liveBloggingNow)), getTagList(result.gLabs), result.testUrl, result.createdBy))

    val combinedArticleList: List[(Option[ContentFields], Seq[Tag], String, Option[String])] = previousArticlesReTestContentFieldsAndUrl ::: newOrChangedArticles
    val combinedLiveBlogList: List[(Option[ContentFields], Seq[Tag], String, Option[String])] = previousLiveBlogReTestContentFieldsAndUrl ::: newOrChangedLiveBlogs
    val combinedInteractiveList: List[(Option[ContentFields], Seq[Tag], String, Option[String])] = previousInteractiveReTestContentFieldsAndUrl ::: newOrChangedInteractives

    //create sorter object - contains functions for ordering lists of Performance Results Objects
    val sorter = new ListSorter

    //obtain results for articles
    if (combinedArticleList.nonEmpty) {
      Logger.info("Generating average values for articles")
      val articleAverages: PageAverageObject = new ArticleDefaultAverages(averageColor)
      articleResults = articleResults.concat(articleAverages.toHTMLString)

      val articleResultsList = listenForResultPages(combinedArticleList, "Article", resultUrlList, articleAverages, wptBaseUrl, wptApiKey, wptLocation, urlFragments)
      val getAnchorId: (List[PerformanceResultsObject], Int) = applyAnchorId(articleResultsList, pageWeightAnchorId)
      val articleResultsWithAnchor = getAnchorId._1
      pageWeightAnchorId = getAnchorId._2

      combinedResultsList = articleResultsWithAnchor
      Logger.info("\n \n \n article tests complete. \n tested " + articleResultsWithAnchor.length + "pages")
      Logger.info("Total number of results gathered so far " + combinedResultsList.length + "pages")

      Logger.info("About to sort article results list. Length of list is: " + articleResultsList.length)
      val sortedByWeightArticleResultsList = sorter.orderListByWeight(articleResultsWithAnchor)
      val sortedBySpeedArticleResultsList = sorter.orderListBySpeed(articleResultsWithAnchor)
      if (sortedByWeightArticleResultsList.isEmpty || sortedBySpeedArticleResultsList.isEmpty) {
        Logger.info("Sorting algorithm for articles has returned empty list. Aborting")
        System exit 1
      }
      val articleHTMLResults: List[String] = sortedByWeightArticleResultsList.map(x => htmlString.generateHTMLRow(x))
      // write article results to string
      //Create a list of alerting pages and write to string
      articlePageWeightAlertList = for (result <- sortedByWeightArticleResultsList if result.alertStatusPageWeight) yield result
      gLabsAlertList = for (result <- sortedByWeightArticleResultsList if (result.alertStatusPageWeight || result.alertStatusPageSpeed) && result.gLabs) yield result
      articleResults = articleResults.concat(articleHTMLResults.mkString)
      articleResults = articleResults + htmlString.closeTable + htmlString.closePage
      //write article results to file
      if (!iamTestingLocally) {
        Logger.info(DateTime.now + " Writing article results to S3")
        s3Interface.writeFileToS3(articleOutputFilename, articleResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(articleOutputFilename, articleResults)
        if (writeSuccess != 0) {
          Logger.info("problem writing local outputfile")
          System exit 1
        }
      }
      Logger.info("Article Performance Test Complete")

    } else {
      Logger.info("CAPI query found no article pages")
    }

    //obtain results for liveBlogs
    if (combinedLiveBlogList.nonEmpty) {
      Logger.info("Generating average values for liveblogs")
      val liveBlogAverages: PageAverageObject = new LiveBlogDefaultAverages(averageColor)
      liveBlogResults = liveBlogResults.concat(liveBlogAverages.toHTMLString)

      val liveBlogResultsList = listenForResultPages(combinedLiveBlogList, "LiveBlog", resultUrlList, liveBlogAverages, wptBaseUrl, wptApiKey, wptLocation, urlFragments)
      val getAnchorId: (List[PerformanceResultsObject], Int) = applyAnchorId(liveBlogResultsList, pageWeightAnchorId)
      val liveBlogResultsWithAnchor = getAnchorId._1
      pageWeightAnchorId = getAnchorId._2

      combinedResultsList = combinedResultsList ::: liveBlogResultsWithAnchor
      Logger.info("\n \n \n liveBlog tests complete. \n tested " + liveBlogResultsWithAnchor.length + "pages")
      Logger.info("Total number of results gathered so far " + combinedResultsList.length + "pages")
      val sortedLiveBlogResultsList = sorter.orderListByWeight(liveBlogResultsWithAnchor)
      if (sortedLiveBlogResultsList.isEmpty) {
        Logger.info("Sorting algorithm for Liveblogs has returned empty list. Aborting")
        System exit 1
      }
      val liveBlogHTMLResults: List[String] = sortedLiveBlogResultsList.map(x => htmlString.generateHTMLRow(x))
      // write liveblog results to string
      //Create a list of alerting pages and write to string
      liveBlogPageWeightAlertList = for (result <- sortedLiveBlogResultsList if result.alertStatusPageWeight) yield result
      gLabsAlertList = for (result <- sortedLiveBlogResultsList if (result.alertStatusPageWeight || result.alertStatusPageSpeed) && result.gLabs) yield result

      liveBlogResults = liveBlogResults.concat(liveBlogHTMLResults.mkString)
      liveBlogResults = liveBlogResults + htmlString.closeTable + htmlString.closePage
      //write liveblog results to file
      if (!iamTestingLocally) {
        Logger.info(DateTime.now + " Writing liveblog results to S3")
        s3Interface.writeFileToS3(liveBlogOutputFilename, liveBlogResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(liveBlogOutputFilename, liveBlogResults)
        if (writeSuccess != 0) {
          Logger.info("problem writing local outputfile")
          System exit 1
        }
      }
      Logger.info("LiveBlog Performance Test Complete")

    } else {
      Logger.info("CAPI query found no liveblogs")
    }

    if (combinedInteractiveList.nonEmpty) {
      Logger.info("Generating average values for interactives")
      //      val interactiveAverages: PageAverageObject = generateInteractiveAverages(listofLargeInteractives, wptBaseUrl, wptApiKey, wptLocation, interactiveItemLabel, averageColor)
      val interactiveAverages: PageAverageObject = new InteractiveDefaultAverages(averageColor)
      interactiveResults = interactiveResults.concat(interactiveAverages.toHTMLString)

      val interactiveResultsList = listenForResultPages(combinedInteractiveList, "Interactive", resultUrlList, interactiveAverages, wptBaseUrl, wptApiKey, wptLocation, urlFragments)
      val getAnchorId: (List[PerformanceResultsObject], Int) = applyAnchorId(interactiveResultsList, pageWeightAnchorId)
      val interactiveResultsWithAnchor = getAnchorId._1
      pageWeightAnchorId = getAnchorId._2

      combinedResultsList = combinedResultsList ::: interactiveResultsWithAnchor
      Logger.info("\n \n \n interactive tests complete. \n tested " + interactiveResultsWithAnchor.length + "pages")
      Logger.info("Total number of results gathered so far " + combinedResultsList.length + "pages")
      val sortedInteractiveResultsList = sorter.orderListByWeight(interactiveResultsWithAnchor)
      if (sortedInteractiveResultsList.isEmpty) {
        Logger.info("Sorting algorithm has returned empty list. Aborting")
        System exit 1
      }
      val interactiveHTMLResults: List[String] = sortedInteractiveResultsList.map(x => htmlString.interactiveHTMLRow(x))
      //generate interactive alert message body
      interactivePageWeightAlertList = for (result <- sortedInteractiveResultsList if result.alertStatusPageWeight) yield result
      interactiveAlertList = for (result <- sortedInteractiveResultsList if result.alertStatusPageWeight || result.alertStatusPageSpeed) yield result
      gLabsAlertList = interactiveAlertList.filter(_.gLabs)
      // write interactive results to string
      interactiveResults = interactiveResults.concat(interactiveHTMLResults.mkString)
      interactiveResults = interactiveResults + htmlString.closeTable + htmlString.closePage
      //write interactive results to file
      if (!iamTestingLocally) {
        Logger.info(DateTime.now + " Writing interactive results to S3")
        s3Interface.writeFileToS3(interactiveOutputFilename, interactiveResults)
      }
      else {
        val outputWriter = new LocalFileOperations
        val writeSuccess: Int = outputWriter.writeLocalResultFile(interactiveOutputFilename, interactiveResults)
        if (writeSuccess != 0) {
          Logger.info("problem writing local outputfile")
          System exit 1
        }
      }
      Logger.info("Interactive Performance Test Complete")

    } else {
      Logger.info("CAPI query found no interactives")
    }

    Logger.info("length of recent but no retest required list: " + previousTestResultsHandler.recentButNoRetestRequired.length)
    val sortedByWeightCombinedResults: List[PerformanceResultsObject] = sorter.orderListByWeight(combinedResultsList ::: previousTestResultsHandler.recentButNoRetestRequired)
    val combinedDesktopResultsList: List[PerformanceResultsObject] = for (result <- sortedByWeightCombinedResults if result.typeOfTest.contains("Desktop")) yield result
    val combinedMobileResultsList: List[PerformanceResultsObject] = for (result <- sortedByWeightCombinedResults if result.typeOfTest.contains("Android/3G")) yield result
    val combinedListLength = sortedByWeightCombinedResults.length
    Logger.info("\n \n \n Combining lists of results and sorting for dashboard pages.")
    Logger.info("length of sorted By Weight Combined List is: " + combinedListLength)
    //Generate lists for sortByWeight combined pages

    val sortedByWeightCombinedDesktopResults: List[PerformanceResultsObject] = sorter.sortHomogenousResultsByWeight(combinedDesktopResultsList)
    val sortedByWeightCombinedMobileResults: List[PerformanceResultsObject] = sorter.sortHomogenousResultsByWeight(combinedMobileResultsList)

    Logger.info("length of sorted By Weight Mobile List is: " + sortedByWeightCombinedDesktopResults.length)
    Logger.info("length of sorted By Weight Combined List is: " + sortedByWeightCombinedMobileResults.length)
    //  strip out errors
    val errorFreeSortedByWeightCombinedResults = for (result <- sortedByWeightCombinedResults if result.speedIndex > 0) yield result
    val errorFreeCombinedListLength = errorFreeSortedByWeightCombinedResults.length
    Logger.info("length of errorFreeSortedByWeightCombinedResults: " + errorFreeCombinedListLength)
    Logger.info((sortedByWeightCombinedResults.length - errorFreeSortedByWeightCombinedResults.length) + " records have been lost due to error")

    val editorialPageWeightDashboardDesktop = new PageWeightDashboardDesktop(sortedByWeightCombinedResults, sortedByWeightCombinedDesktopResults, sortedByWeightCombinedMobileResults)
    val editorialPageWeightDashboardMobile = new PageWeightDashboardMobile(sortedByWeightCombinedResults, sortedByWeightCombinedDesktopResults, sortedByWeightCombinedMobileResults)
    val editorialPageWeightDashboard = new PageWeightDashboardTabbed(sortedByWeightCombinedResults, sortedByWeightCombinedDesktopResults, sortedByWeightCombinedMobileResults)

    // record results
    val combinedResultsForFile = errorFreeSortedByWeightCombinedResults.filter(_.fullElementList.nonEmpty)

    Logger.info("combinedResultsForFile length = " + combinedResultsForFile.length)
    val resultsToRecord = (sorter.orderListByDatePublished(combinedResultsForFile) ::: previousTestResultsHandler.oldResults).take(5000)

    //val resultsToRecord = (combinedResultsForFile ::: previousResultsWithElementsAdded).distinct
    Logger.info("\n\n\n ***** There are " + resultsToRecord.length + " results to be saved to the previous results file  ********* \n\n\n")
    Logger.info("of these " + previousTestResultsHandler.oldResults.length + " are from old results")
    val resultsToRecordCSVString: String = resultsToRecord.map(_.toCSVString()).mkString

    //Generate Lists for sortBySpeed combined pages
    val sortedBySpeedCombinedResults: List[PerformanceResultsObject] = sorter.orderListBySpeed(combinedResultsList ::: previousTestResultsHandler.recentButNoRetestRequired)
    val sortedBySpeedCombinedDesktopResults: List[PerformanceResultsObject] = sorter.sortHomogenousResultsBySpeed(combinedDesktopResultsList ::: previousTestResultsHandler.recentButNoRetestRequired)
    val sortedBySpeedCombinedMobileResults: List[PerformanceResultsObject] = sorter.sortHomogenousResultsBySpeed(combinedMobileResultsList ::: previousTestResultsHandler.recentButNoRetestRequired)

    //Generate Lists for interactive pages
    val combinedInteractiveResultsList = for (result <- combinedResultsList ::: previousTestResultsHandler.recentButNoRetestRequired if result.getPageType.contains("Interactive") || result.getPageType.contains("interactive")) yield result
    val interactiveDesktopResults = for (result <- combinedDesktopResultsList ::: previousTestResultsHandler.recentButNoRetestRequired if result.getPageType.contains("Interactive") || result.getPageType.contains("interactive")) yield result
    val interactiveMobileResults = for (result <- combinedMobileResultsList ::: previousTestResultsHandler.recentButNoRetestRequired if result.getPageType.contains("Interactive") || result.getPageType.contains("interactive")) yield result

    val sortedInteractiveCombinedResults: List[PerformanceResultsObject] = sorter.orderInteractivesBySpeed(combinedInteractiveResultsList)
    val sortedInteractiveDesktopResults: List[PerformanceResultsObject] = sorter.sortHomogenousInteractiveResultsBySpeed(interactiveDesktopResults)
    val sortedInteractiveMobileResults: List[PerformanceResultsObject] = sorter.sortHomogenousInteractiveResultsBySpeed(interactiveMobileResults)

    val sortedGLabsCombinedResults: List[PerformanceResultsObject] = sortedByWeightCombinedResults.filter(_.gLabs)
    val sortedGLabsDesktopResults: List[PerformanceResultsObject] = sortedByWeightCombinedDesktopResults.filter(_.gLabs)
    val sortedGLabsMobileResults: List[PerformanceResultsObject] = sortedByWeightCombinedMobileResults.filter(_.gLabs)

    val dotcomPageSpeedDashboard = new PageSpeedDashboardTabbed(sortedBySpeedCombinedResults, sortedBySpeedCombinedDesktopResults, sortedBySpeedCombinedMobileResults)
    val interactiveDashboard = new InteractiveDashboardTabbed(sortedInteractiveCombinedResults, sortedInteractiveDesktopResults, sortedInteractiveMobileResults)
    val interactiveDashboardDesktop = new InteractiveDashboardDesktop(sortedInteractiveCombinedResults, sortedInteractiveDesktopResults, sortedInteractiveMobileResults)
    val interactiveDashboardMobile = new InteractiveDashboardMobile(sortedInteractiveCombinedResults, sortedInteractiveDesktopResults, sortedInteractiveMobileResults)

    val gLabsDashboard =  new GLabsDashboardTabbed(sortedGLabsCombinedResults, sortedGLabsDesktopResults, sortedGLabsMobileResults)
    val gLabsDashboardDesktop = new InteractiveDashboardDesktop(sortedGLabsCombinedResults, sortedGLabsDesktopResults, sortedGLabsMobileResults)
    val gLabsDashboardMobile = new InteractiveDashboardMobile(sortedGLabsCombinedResults, sortedGLabsDesktopResults, sortedGLabsMobileResults)

    val pageWeightAlertsPreviouslyFixed = s3Interface.getResultsFileFromS3(alertsThatHaveBeenFixed)
    val pageWeightAlertsFixedThisRun = for (alert <- previousTestResultsHandler.hasPreviouslyAlertedOnWeight if !(articlePageWeightAlertList ::: liveBlogPageWeightAlertList ::: interactivePageWeightAlertList).map(result => (result.testUrl, result.typeOfTest)).contains((alert.testUrl, alert.typeOfTest))) yield alert
    val pageWeightAlertsFixedCSVString = (pageWeightAlertsFixedThisRun ::: pageWeightAlertsPreviouslyFixed).map(_.toCSVString()).mkString

    val interactiveAlertsFixedThisRun = previousTestResultsHandler.hasPreviouslyAlerted.filter(_.getPageType.toLowerCase.contains("interactive"))

    val listOfDupes = for (result <- sortedByWeightCombinedResults if sortedByWeightCombinedResults.map(page => (page.testUrl, page.typeOfTest)).count(_ == (result.testUrl,result.typeOfTest)) > 1) yield result

    val listOfPagesWithInsecureElements = (combinedResultsList ::: previousPagesWithInsecureElements).filter(_.fullElementList.exists(test => test.resource.take(5).contains("http:")))

    if (listOfDupes.nonEmpty) {
      Logger.info("\n\n\n\n ******** Duplicates found in results! ****** \n Found " + listOfDupes.length + " duplicates")
      Logger.info("Duplicate urls are: \n" + listOfDupes.map(result => "url: " + result.testUrl + " TestType: " + result.typeOfTest + "\n"))
    }

    //write combined results to file
    if (!iamTestingLocally) {
      Logger.info(DateTime.now + " Writing liveblog results to S3")
      s3Interface.writeFileToS3(editorialDesktopPageweightFilename, editorialPageWeightDashboardDesktop.toString())
      s3Interface.writeFileToS3(editorialMobilePageweightFilename, editorialPageWeightDashboardMobile.toString())
      s3Interface.writeFileToS3(editorialPageweightFilename, editorialPageWeightDashboard.toString())
      s3Interface.writeFileToS3(dotcomPageSpeedFilename, dotcomPageSpeedDashboard.toString())
      s3Interface.writeFileToS3(interactiveDashboardFilename, interactiveDashboard.toString())
      s3Interface.writeFileToS3(interactiveDashboardDesktopFilename, interactiveDashboardDesktop.toString())
      s3Interface.writeFileToS3(interactiveDashboardMobileFilename, interactiveDashboardMobile.toString())
      s3Interface.writeFileToS3(gLabsDashboardFilename, gLabsDashboard.toString())
      s3Interface.writeFileToS3(gLabsDashboardDesktopFilename, gLabsDashboardDesktop.toString())
      s3Interface.writeFileToS3(gLabsDashboardMobileFilename, gLabsDashboardMobile.toString())
      s3Interface.writeFileToS3(resultsFromPreviousTests, resultsToRecordCSVString)
      s3Interface.writeFileToS3(alertsThatHaveBeenFixed, pageWeightAlertsFixedCSVString)
      s3Interface.writeFileToS3(pagesWithInsecureElements, listOfPagesWithInsecureElements.map(_.toCSVString()).mkString)
      s3Interface.writeFileToS3(duplicateResultList, listOfDupes.map(_.toCSVString()).mkString)
    }
    else {
      val outputWriter = new LocalFileOperations
      val writeSuccessPWDC: Int = outputWriter.writeLocalResultFile(editorialPageweightFilename, editorialPageWeightDashboard.toString())
      if (writeSuccessPWDC != 0) {
        Logger.info("problem writing local outputfile")
        System exit 1
      }
      val writeSuccessPWDD: Int = outputWriter.writeLocalResultFile(editorialDesktopPageweightFilename, editorialPageWeightDashboardDesktop.toString())
      if (writeSuccessPWDD != 0) {
        Logger.info("problem writing local outputfile")
        System exit 1
      }
      val writeSuccessPWDM: Int = outputWriter.writeLocalResultFile(editorialMobilePageweightFilename, editorialPageWeightDashboardMobile.toString())
      if (writeSuccessPWDM != 0) {
        Logger.info("problem writing local outputfile")
        System exit 1
      }
      val writeSuccessDCPSD: Int = outputWriter.writeLocalResultFile(dotcomPageSpeedFilename, dotcomPageSpeedDashboard.toString())
      if (writeSuccessDCPSD != 0) {
        Logger.info("problem writing local outputfile")
        System exit 1
      }
      val writeSuccessIPSD: Int = outputWriter.writeLocalResultFile(interactiveDashboardFilename, interactiveDashboard.toString())
      if (writeSuccessIPSD != 0) {
        Logger.info("problem writing local outputfile")
        System exit 1
      }
      val writeSuccessAlertsRecord: Int = outputWriter.writeLocalResultFile(resultsFromPreviousTests, resultsToRecordCSVString)
      if (writeSuccessAlertsRecord != 0) {
        Logger.info("problem writing local outputfile")
        System exit 1
      }

    }


    //check if alert items have already been sent in earlier run
    val newArticlePageWeightAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOnForWeight(articlePageWeightAlertList)
    val newLiveBlogPageWeightAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOnForWeight(liveBlogPageWeightAlertList)
    val newInteractivePageWeightAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOnForWeight(interactivePageWeightAlertList)
    //    val newFrontsPageWeightAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOn(frontsPageWeightAlertList)
    val newInteractiveAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOn(interactiveAlertList)
    val newGLabsAlertsList: List[PerformanceResultsObject] = previousTestResultsHandler.returnPagesNotYetAlertedOn(gLabsAlertList)

    val alertsToSend = (newArticlePageWeightAlertsList ::: newLiveBlogPageWeightAlertsList ::: newInteractivePageWeightAlertsList).filter(!_.gLabs)
    if (alertsToSend.nonEmpty) {
      Logger.info("There are new pageWeight alerts to send! There are " + alertsToSend.length + " new alerts")
      val pageWeightEmailAlerts = new PageWeightEmailTemplate(alertsToSend, amazonDomain + "/" + s3BucketName + "/" + editorialMobilePageweightFilename, amazonDomain + "/" + s3BucketName + "/" + editorialDesktopPageweightFilename)
      val pageWeightEmailSuccess = emailer.sendPageWeightAlert(generalAlertsAddressList, pageWeightEmailAlerts.toString())
      if (pageWeightEmailSuccess)
        Logger.info(DateTime.now + " Page-Weight Alert Emails sent successfully. ")
      else
        Logger.info(DateTime.now + "ERROR: Sending of Page-Weight Alert Emails failed")
    } else {
      Logger.info("No pages to alert on Page-Weight. Email not sent.")
    }

    if (newInteractiveAlertsList.nonEmpty) {
      Logger.info("dividing interactive's by their production office")
      val ukInteractives = newInteractiveAlertsList.filter(_.productionOffice.getOrElse("").contains("UK"))
      val usInteractives = newInteractiveAlertsList.filter(_.productionOffice.getOrElse("").contains("US"))
      val auInteractives = newInteractiveAlertsList.filter(_.productionOffice.getOrElse("").contains("AU"))
      Logger.info("There are new interactive email alerts to send - length of list is: " + newInteractiveAlertsList.length)
      Logger.info("There are " + ukInteractives.length + " alerts for the UK interactive office")
      Logger.info("There are " + usInteractives.length + " alerts for the US interactive office")
      Logger.info("There are " + auInteractives.length + " alerts for the AU interactive office")
      val ukInteractiveEmailAlerts = new InteractiveEmailTemplate(ukInteractives, amazonDomain + "/" + s3BucketName + "/" + interactiveDashboardMobileFilename, amazonDomain + "/" + s3BucketName + "/" + interactiveDashboardDesktopFilename)
      val usInteractiveEmailAlerts = new InteractiveEmailTemplate(usInteractives, amazonDomain + "/" + s3BucketName + "/" + interactiveDashboardMobileFilename, amazonDomain + "/" + s3BucketName + "/" + interactiveDashboardDesktopFilename)
      val auInteractiveEmailAlerts = new InteractiveEmailTemplate(auInteractives, amazonDomain + "/" + s3BucketName + "/" + interactiveDashboardMobileFilename, amazonDomain + "/" + s3BucketName + "/" + interactiveDashboardDesktopFilename)
      val ukInteractiveEmailSuccess = emailer.sendInteractiveAlert(ukInteractiveAlertsAddressList ::: ukInteractives.flatMap(page => page.createdBy), ukInteractiveEmailAlerts.toString())
      val usInteractiveEmailSuccess = emailer.sendInteractiveAlert(usInteractiveAlertsAddressList ::: usInteractives.flatMap(page => page.createdBy), usInteractiveEmailAlerts.toString())
      val auInteractiveEmailSuccess = emailer.sendInteractiveAlert(auInteractiveAlertsAddressList ::: auInteractives.flatMap(page => page.createdBy), auInteractiveEmailAlerts.toString())
      if (ukInteractiveEmailSuccess && usInteractiveEmailSuccess && auInteractiveEmailSuccess) {
        Logger.info("Interactive Alert email sent successfully.")
      } else {
        Logger.info("ERROR: Sending of Interactive Alert Emails failed")
      }
    } else {
      Logger.info("no interactive alerts to send, therefore Interactive Alert Email not sent.")
    }

    if (newGLabsAlertsList.nonEmpty) {
      Logger.info("There are new interactive email alerts to send - length of list is: " + newGLabsAlertsList.length)
      val ukGLabsAlerts = newGLabsAlertsList.filter(_.productionOffice.getOrElse("").contains("UK"))
      val usGLabsAlerts = newGLabsAlertsList.filter(_.productionOffice.getOrElse("").contains("US"))
      val auGLabsAlerts = newGLabsAlertsList.filter(_.productionOffice.getOrElse("").contains("AU"))
      Logger.info("There are new  GLabs email alerts to send - length of list is: " + newGLabsAlertsList.length)
      Logger.info("There are " + ukGLabsAlerts.length + " alerts for the UK GLabs office")
      Logger.info("There are " + usGLabsAlerts.length + " alerts for the US GLabs office")
      Logger.info("There are " + auGLabsAlerts.length + " alerts for the AU GLabs office")
      val ukFullGlabsEmailAddresses = ukGLabsAlertsAddressList ::: globalGLabsAlertsAddressList ::: ukGLabsAlerts.flatMap(page => page.createdBy)
      val usFullGlabsEmailAddresses = usGLabsAlertsAddressList ::: globalGLabsAlertsAddressList ::: usGLabsAlerts.flatMap(page => page.createdBy)
      val auFullGlabsEmailAddresses = auGLabsAlertsAddressList ::: globalGLabsAlertsAddressList ::: auGLabsAlerts.flatMap(page => page.createdBy)
      val ukGLabsEmailAlerts = new GLabsEmailTemplate(ukGLabsAlerts, amazonDomain + "/" + s3BucketName + "/" + gLabsDashboardMobileFilename, amazonDomain + "/" + s3BucketName + "/" + gLabsDashboardDesktopFilename)
      val usGLabsEmailAlerts = new GLabsEmailTemplate(usGLabsAlerts, amazonDomain + "/" + s3BucketName + "/" + gLabsDashboardMobileFilename, amazonDomain + "/" + s3BucketName + "/" + gLabsDashboardDesktopFilename)
      val auGLabsEmailAlerts = new GLabsEmailTemplate(auGLabsAlerts, amazonDomain + "/" + s3BucketName + "/" + gLabsDashboardMobileFilename, amazonDomain + "/" + s3BucketName + "/" + gLabsDashboardDesktopFilename)
      val ukGLabsEmailSuccess = {
        if(ukGLabsAlerts.nonEmpty){
        emailer.sendPaidContentAlert(ukFullGlabsEmailAddresses, ukGLabsEmailAlerts.toString())
      } else {
        true
      }
      }
      val usGLabsEmailSuccess = {
        if(usGLabsAlerts.nonEmpty){
        emailer.sendPaidContentAlert(usFullGlabsEmailAddresses, usGLabsEmailAlerts.toString())
      } else {
        true
        }
      }
      val auGLabsEmailSuccess = {
        if (auGLabsAlerts.nonEmpty) {
          emailer.sendPaidContentAlert(auFullGlabsEmailAddresses, auGLabsEmailAlerts.toString())
        } else {
          true
        }
      }
      if (ukGLabsEmailSuccess && usGLabsEmailSuccess && auGLabsEmailSuccess) {
        Logger.info("Paid Content Alert email sent successfully.")
      } else {
        Logger.info("ERROR: Sending of Paid Content Alert Emails failed")
      }
    } else {
      Logger.info("no Paid Content alerts to send, therefore Paid Content Alert Email not sent.")
    }


    //todo - this needs to have both pageweight and page-speed alerts
    /*val paidContentAlertsToSend = (newArticlePageWeightAlertsList ::: newLiveBlogPageWeightAlertsList ::: newInteractivePageWeightAlertsList).filter(_.gLabs)
    if (paidContentAlertsToSend.nonEmpty) {
      Logger.info("There are new paid Content alerts to send! There are " + alertsToSend.length + " new alerts")
      val paidContentEmailAlerts = new PageWeightEmailTemplate(paidContentAlertsToSend, amazonDomain + "/" + s3BucketName + "/" + editorialMobilePageweightFilename, amazonDomain + "/" + s3BucketName + "/" + editorialDesktopPageweightFilename)
      val paidContentEmailSuccess = emailer.sendPageWeightAlert(generalAlertsAddressList, paidContentEmailAlerts.toString())
      if (paidContentEmailSuccess)
        Logger.info(DateTime.now + " Paid-content Alert Emails sent successfully. ")
      else
        Logger.info(DateTime.now + "ERROR: Sending of Paid-content Alert Emails failed")
    } else {
      Logger.info("No pages to alert on Paid-content. Email not sent.")
    }*/

    val newPageWeightAlerts = newArticlePageWeightAlertsList ::: newLiveBlogPageWeightAlertsList ::: newInteractivePageWeightAlertsList
    // write pageWeight alerts results file
    s3Interface.writeFileToS3(pageWeightAlertsFromPreviousTests, (newPageWeightAlerts ::: previousPageWeightAlerts).map(_.toCSVString()).mkString )
    //write interactive alerts results file
    s3Interface.writeFileToS3(interactiveAlertsFromPreviousTests, (newInteractiveAlertsList ::: previousInteractiveAlerts).map(_.toCSVString()).mkString )


    val jobFinish = DateTime.now()
    val timeTaken = (jobFinish.getMillis - jobStart.getMillis).toDouble / (1000 * 60)
    val numberOfPagesTested = urlsToSend.length
    val numberOfPageWeightAlerts = combinedResultsList.count(_.alertStatusPageWeight)
    val percentageOfPageWeightAlerts = if(numberOfPagesTested > 0){
      numberOfPageWeightAlerts.toDouble/(numberOfPagesTested * 100)
    } else {
      0.0
    }
    val numberOfPageSpeedAlerts = combinedResultsList.count(_.alertStatusPageSpeed)
    val percentageOfPageSpeedAlerts = if(numberOfPagesTested > 0){
      numberOfPageSpeedAlerts.toDouble/(numberOfPagesTested * 100)
    } else {
      0.0
    }


    //generate summaries

        val numberNewDesktopArticleTests = newOrChangedArticles.count(!_._2.exists(_.id.contains("tone/advertisement-features")))
        val numberNewMobileArticleTests = newOrChangedArticles.count(!_._2.exists(_.id.contains("tone/advertisement-features")))
        val numberNewDesktopLiveBlogTests = newOrChangedLiveBlogs.count(!_._2.exists(_.id.contains("tone/advertisement-features")))
        val numberNewMobileLiveBlogTests = newOrChangedLiveBlogs.count(!_._2.exists(_.id.contains("tone/advertisement-features")))
        val numberNewDesktopInteractiveTests = newOrChangedInteractives.count(!_._2.exists(_.id.contains("tone/advertisement-features")))
        val numberNewMobileInteractiveTests = newOrChangedInteractives.count(!_._2.exists(_.id.contains("tone/advertisement-features")))
        val numberNewDesktopGLabsTests = {
          newOrChangedArticles.count(_._2.exists(_.id.contains("tone/advertisement-features"))) +
            newOrChangedLiveBlogs.count(_._2.exists(_.id.contains("tone/advertisement-features"))) +
            newOrChangedInteractives.count(_._2.exists(_.id.contains("tone/advertisement-features")))
        }
        val numberNewMobileGLabsTests = numberNewDesktopGLabsTests

        val nbrNewDesktopArticleAlerts = newArticlePageWeightAlertsList.count(_.typeOfTestName.contains("Desktop"))
        val nbrNewMobileArticleAlerts = newArticlePageWeightAlertsList.count(_.typeOfTestName.contains("Mobile"))
        val nbrNewDesktopLiveBlogAlerts = newLiveBlogPageWeightAlertsList.count(_.typeOfTestName.contains("Desktop"))
        val nbrNewMobileLiveBlogAlerts = newLiveBlogPageWeightAlertsList.count(_.typeOfTestName.contains("Mobile"))
        val nbrNewDesktopInteractiveAlerts = newInteractiveAlertsList.count(_.typeOfTestName.contains("Desktop"))
        val nbrNewMobileInteractiveAlerts = newInteractiveAlertsList.count(_.typeOfTestName.contains("Mobile"))
        val nbrNewDesktopGLabsAlerts = newGLabsAlertsList.count(_.typeOfTestName.contains("Desktop"))
        val nbrNewMobileGLabsAlerts = newGLabsAlertsList.count(_.typeOfTestName.contains("Mobile"))

        val nbrDesktopArticleAlertsResolved = pageWeightAlertsFixedThisRun.count(result => {result.getPageType.contains("Article") && result.typeOfTestName.contains("Desktop")})
        val nbrMobileArticleAlertsResolved = pageWeightAlertsFixedThisRun.count(result => {result.getPageType.contains("Article") && result.typeOfTestName.contains("Mobile")})
        val nbrDesktopLiveBlogAlertsResolved = pageWeightAlertsFixedThisRun.count(result => {result.getPageType.contains("LiveBlog") && result.typeOfTestName.contains("Desktop")})
        val nbrMobileLiveBlogAlertsResolved = pageWeightAlertsFixedThisRun.count(result => {result.getPageType.contains("LiveBlog") && result.typeOfTestName.contains("Mobile")})
        val nbrDesktopInteractiveAlertsResolved = interactiveAlertsFixedThisRun.count(result => {(!result.gLabs) && result.typeOfTestName.contains("Desktop")})
        val nbrMobileInteractiveAlertsResolved = interactiveAlertsFixedThisRun.count(result => {(!result.gLabs) && result.typeOfTestName.contains("Mobile")})
        val nbrDesktopGLabsAlertsResolved = pageWeightAlertsFixedThisRun.count(result => {result.gLabs && result.typeOfTestName.contains("Desktop")}) + interactiveAlertsFixedThisRun.count(result => {result.gLabs && result.typeOfTestName.contains("Desktop")})
        val nbrMobileGLabsAlertsResolved = pageWeightAlertsFixedThisRun.count(result => {result.gLabs && result.typeOfTestName.contains("Mobile")}) + interactiveAlertsFixedThisRun.count(result => {result.gLabs && result.typeOfTestName.contains("Mobile")})

        val numberCAPIRequests = articles.length + liveBlogs.length + interactives.length
        val resultSummary = new DataSummary(jobStart, jobFinish, numberCAPIRequests, numberNewDesktopArticleTests, numberNewMobileArticleTests, numberNewDesktopLiveBlogTests, numberNewMobileLiveBlogTests, numberNewDesktopInteractiveTests, numberNewMobileInteractiveTests, numberNewDesktopGLabsTests, numberNewMobileGLabsTests, nbrNewDesktopArticleAlerts, nbrDesktopArticleAlertsResolved, nbrNewMobileArticleAlerts, nbrMobileArticleAlertsResolved, nbrNewDesktopLiveBlogAlerts, nbrDesktopLiveBlogAlertsResolved, nbrNewMobileLiveBlogAlerts, nbrMobileLiveBlogAlertsResolved, nbrNewDesktopInteractiveAlerts, nbrDesktopInteractiveAlertsResolved, nbrNewMobileInteractiveAlerts, nbrMobileInteractiveAlertsResolved, nbrNewDesktopGLabsAlerts, nbrDesktopGLabsAlertsResolved, nbrNewMobileGLabsAlerts, nbrMobileGLabsAlertsResolved, combinedResultsList, previousTestResultsHandler, new ResultsFromPreviousTests(previousPageWeightAlerts))
        val pageWeightAlertsSummary = new DataSummary(jobStart, jobFinish, numberCAPIRequests, numberNewDesktopArticleTests, numberNewMobileArticleTests, numberNewDesktopLiveBlogTests, numberNewMobileLiveBlogTests, numberNewDesktopInteractiveTests, numberNewMobileInteractiveTests, numberNewDesktopGLabsTests, numberNewMobileGLabsTests, nbrNewDesktopArticleAlerts, nbrDesktopArticleAlertsResolved, nbrNewMobileArticleAlerts, nbrMobileArticleAlertsResolved, nbrNewDesktopLiveBlogAlerts, nbrDesktopLiveBlogAlertsResolved, nbrNewMobileLiveBlogAlerts, nbrMobileLiveBlogAlertsResolved, nbrNewDesktopInteractiveAlerts, nbrDesktopInteractiveAlertsResolved, nbrNewMobileInteractiveAlerts, nbrMobileInteractiveAlertsResolved, nbrNewDesktopGLabsAlerts, nbrDesktopGLabsAlertsResolved, nbrNewMobileGLabsAlerts, nbrMobileGLabsAlertsResolved, newPageWeightAlerts, new ResultsFromPreviousTests(previousPageWeightAlerts), new ResultsFromPreviousTests(previousPageWeightAlerts))
        val interactiveAlertsSummary = new DataSummary(jobStart, jobFinish, numberCAPIRequests, numberNewDesktopArticleTests, numberNewMobileArticleTests, numberNewDesktopLiveBlogTests, numberNewMobileLiveBlogTests, numberNewDesktopInteractiveTests, numberNewMobileInteractiveTests, numberNewDesktopGLabsTests, numberNewMobileGLabsTests, nbrNewDesktopArticleAlerts, nbrDesktopArticleAlertsResolved, nbrNewMobileArticleAlerts, nbrMobileArticleAlertsResolved, nbrNewDesktopLiveBlogAlerts, nbrDesktopLiveBlogAlertsResolved, nbrNewMobileLiveBlogAlerts, nbrMobileLiveBlogAlertsResolved, nbrNewDesktopInteractiveAlerts, nbrDesktopInteractiveAlertsResolved, nbrNewMobileInteractiveAlerts, nbrMobileInteractiveAlertsResolved, nbrNewDesktopGLabsAlerts, nbrDesktopGLabsAlertsResolved, nbrNewMobileGLabsAlerts, nbrMobileGLabsAlertsResolved, newInteractiveAlertsList, new ResultsFromPreviousTests(previousInteractiveAlerts), new ResultsFromPreviousTests(previousInteractiveAlerts))
        val periodicReport = new PeriodicReport(jobStart, jobFinish, numberCAPIRequests, numberOfPagesTested, numberNewDesktopArticleTests, numberNewMobileArticleTests, numberNewDesktopLiveBlogTests, numberNewMobileLiveBlogTests, numberNewDesktopInteractiveTests, numberNewMobileInteractiveTests, numberNewDesktopGLabsTests, numberNewMobileGLabsTests, nbrNewDesktopArticleAlerts, nbrDesktopArticleAlertsResolved, nbrNewMobileArticleAlerts, nbrMobileArticleAlertsResolved, nbrNewDesktopLiveBlogAlerts, nbrDesktopLiveBlogAlertsResolved, nbrNewMobileLiveBlogAlerts, nbrMobileLiveBlogAlertsResolved, nbrNewDesktopInteractiveAlerts, nbrDesktopInteractiveAlertsResolved, nbrNewMobileInteractiveAlerts, nbrMobileInteractiveAlertsResolved, nbrNewDesktopGLabsAlerts, nbrDesktopGLabsAlertsResolved, nbrNewMobileGLabsAlerts, nbrMobileGLabsAlertsResolved, combinedResultsList, previousTestResultsHandler, new ResultsFromPreviousTests(previousPageWeightAlerts))


    //generate summary pages
        val summaryHTMLPage = new SummaryPage(resultSummary)
        val pageWeightAlertSummaryHTMLPage = new SummaryPage(pageWeightAlertsSummary)
        val interactiveAlertSummaryHTMLPage = new SummaryPage(interactiveAlertsSummary)
        val periodicReportHTMLPage = new PeriodicReportPage(periodicReport)
    //write summary pages to file
        s3Interface.writeFileToS3(currentDataSummaryPage, summaryHTMLPage.toString())
        s3Interface.writeFileToS3(currentPageWeightAlertSummaryPage, pageWeightAlertSummaryHTMLPage.toString())
        s3Interface.writeFileToS3(currentInteractiveSummaryPage, interactiveAlertSummaryHTMLPage.toString())
        s3Interface.writeFileToS3(currentWeeklySummaryPage, periodicReportHTMLPage.toString())

    //write summaries to files
        Logger.info("writing run summary data to new file")
        s3Interface.writeFileToS3(runSummaryFile, resultSummary.summaryDataToString())
        s3Interface.writeFileToS3(pageWeightAlertSummaryFile, pageWeightAlertsSummary.summaryDataToString())
        s3Interface.writeFileToS3(interactiveAlertSummaryFile, interactiveAlertsSummary.summaryDataToString())

    if (jobStart.dayOfWeek.get == 7 && (jobStart.hourOfDay().get == 23 || jobFinish.hourOfDay().get == 23)){
      Logger.info("end of week!")

    }





    val runSummaryCSVString: String = jobStart + "," +
      jobFinish + "," +
      timeTaken + "," +
      articles.length + "," +
      liveBlogs.length + "," +
      interactives.length + "," +
      numberOfPagesTested + "," +
      pagesToRetest.length + "," +
      articleUrls.length + "," +
      liveBlogUrls.length + "," +
      interactiveUrls.length + "," +
      numberOfPageWeightAlerts + "," +
      percentageOfPageWeightAlerts + "," +
      numberOfPageSpeedAlerts + "," +
      percentageOfPageSpeedAlerts + "," +
      articlePageWeightAlertList.length + "," +
      liveBlogPageWeightAlertList.length + "," +
      interactivePageWeightAlertList.length + "," +
      newInteractiveAlertsList.length + "," +
      newArticlePageWeightAlertsList + "," +
      newLiveBlogPageWeightAlertsList + "," +
      newInteractivePageWeightAlertsList + "," +
      newInteractiveAlertsList + "," +
      listOfDupes.length + "\n"

    val runlogList = s3Interface.getCSVFileFromS3(runLog).take(1000)
    val runlogToWrite = runSummaryCSVString :: runlogList
    if(!iamTestingLocally){
      s3Interface.writeFileToS3(runLog, runlogToWrite.mkString)
    }


    Logger.info("Job completed at: " + jobFinish + "\nJob took " + timeTaken + " minutes to run.")
    Logger.info("Breakdown of articles returned from CAPI\n"+
      articles.length + " Article pages returned from CAPI\n" +
      liveBlogs.length + " LiveBlog pages returned from CAPI\n" +
      interactives.length + " Interactive pages returned")
    Logger.info("Number of pages tested: " + numberOfPagesTested + " pages.")
    Logger.info("Breakdown of pages seen as needing testing: \n" +
      pagesToRetest.length + " pages retested from previous run\n" +
      articleUrls.length + " Article pages returned from CAPI seen as untested\n" +
      liveBlogUrls.length + " LiveBlog pages returned from CAPI seen as untested\n" +
      interactiveUrls.length + " Interactive pages returned from CAPI seen as untested")
    Logger.info("Number of pageWeight Alerts found by job: " + numberOfPageWeightAlerts)
    Logger.info("This is roughly " + percentageOfPageWeightAlerts + "% of pages tested")
    Logger.info("Number of pageSpeed Alerts found by job: " + numberOfPageSpeedAlerts)
    Logger.info("This is roughly " + percentageOfPageSpeedAlerts + "% of pages tested")
    Logger.info("Breakdown of alerts: \n" +
      articlePageWeightAlertList.length + " Article pageWeight alerts\n" +
      liveBlogPageWeightAlertList.length + " LiveBlog pageWeight alerts\n" +
      interactivePageWeightAlertList.length + " Interactive pageWeight alerts\n" +
      interactiveAlertList.length + " Interactive weight or perfromance alerts.")
    Logger.info(listOfDupes.length + " Duplicate test results found")
    Logger.info("length of sorted By Weight Combined List is: " + combinedListLength)
    Logger.info("length of errorFreeSortedByWeightCombinedResults: " + errorFreeCombinedListLength)
    Logger.info("length of elements list in first result in combinedResultsList: " + combinedResultsList.head.fullElementList.length)
    Logger.info("length of elements list in first result in sortedByWeightCombinedResults: " + sortedByWeightCombinedResults.head.fullElementList.length)
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

  def listenForResultPages(capiPages: List[(Option[ContentFields], Seq[Tag], String, Option[String])], contentType: String, resultUrlList: List[(String, String)], averages: PageAverageObject, wptBaseUrl: String, wptApiKey: String, wptLocation: String, urlFragments: List[String]): List[PerformanceResultsObject] = {
    Logger.info("ListenForResultPages called with: \n\n" +
      " List of Urls: \n" + capiPages.map(page => page._3).mkString +
      "\n\nList of WebPage Test results: \n" + resultUrlList.mkString +
      "\n\nList of averages: \n" + averages.toHTMLString + "\n")

    val listenerList: List[WptResultPageListener] = capiPages.flatMap(page => {
      for (element <- resultUrlList if element._1 == page._3) yield new WptResultPageListener(element._1, contentType, page._1, page._2, element._2, page._4)
    })

    Logger.info("Listener List created: \n" + listenerList.map(element => "list element: \n" + "url: " + element.pageUrl + "\n" + "resulturl" + element.wptResultUrl + "\n"))

    val resultsList: ParSeq[WptResultPageListener] = listenerList.par.map(element => {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
      val newElement = new WptResultPageListener(element.pageUrl, element.pageType, element.pageFields, element.tagList, element.wptResultUrl, element.contentCreator)
      Logger.info("getting result for page element")
      newElement.testResults = wpt.getResults(newElement.pageUrl,newElement.wptResultUrl)
      Logger.info("result received\n setting headline")
      newElement.testResults.setHeadline(newElement.headline)
      Logger.info("headline set\n setting pagetype")
      newElement.testResults.setPageType(newElement.pageType)
      Logger.info("pagetype set\n setting FirstPublished")
      newElement.testResults.setFirstPublished(newElement.firstPublished)
      Logger.info("FirstPublished set\n setting LastUpdated")
      newElement.testResults.setPageLastUpdated(newElement.pageLastModified)
      Logger.info("Lastupdated set\n setting LiveBloggingNow")
      newElement.testResults.setLiveBloggingNow(newElement.liveBloggingNow.getOrElse(false))
      Logger.info("liveBloggingNow set\n setting gLabs flag")
      newElement.testResults.setGLabs(newElement.gLabs.toString)
      Logger.info("gLabs set \n setting production office")
      newElement.testResults.productionOffice = newElement.productionOffice
      Logger.info("production office set \n setting content creator")
      newElement.testResults.createdBy = newElement.contentCreator
      Logger.info("all variables set for element")
      newElement
    })
    Logger.info("Generating list of results objects from list of listener objects")
    val testResults = resultsList.map(element => element.testResults).toList
    Logger.info("setting alert status of results in list")
    val resultsWithAlerts: List[PerformanceResultsObject] = testResults.map(element => setAlertStatus(element, averages))
    Logger.info("about to return list of results")
    resultsWithAlerts
  }
    //Confirm alert status by retesting alerting urls - this has been removed as an attempt to reduce excessive load on the revised
    // - much cheaper and less powerful testing agents
    /*Logger.info("Confirming any items that have an alert")
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

  }*/

  def confirmAlert(initialResult: PerformanceResultsObject, averages: PageAverageObject, urlFragments: List[String],wptBaseUrl: String, wptApiKey: String, wptLocation: String): PerformanceResultsObject = {
    val webPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val testCount: Int = if (initialResult.timeToFirstByte > 1000) {
      5
    } else {
      3
    }
    Logger.info("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
    val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatus(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    AlertConfirmationTestResult
  }

  def setAlertStatus(resultObject: PerformanceResultsObject, averages: PageAverageObject): PerformanceResultsObject = {
    //  Add results to string which will eventually become the content of our results file
    if (resultObject.typeOfTest == "Desktop") {
      if (resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded) {
        Logger.info("PageWeight Alert Set")
        resultObject.pageWeightAlertDescription = "the page is too heavy. Please examine the list of embeds below for items that are unexpectedly large."
        resultObject.alertStatusPageWeight = true
      }
      else {
        Logger.info("PageWeight Alert not set")
        resultObject.alertStatusPageWeight = false
      }
      if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) ||
          (resultObject.speedIndex >= averages.desktopSpeedIndex)) {
        Logger.info("PageSpeed alert set")
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
        Logger.info("PageSpeed alert not set")
        resultObject.alertStatusPageSpeed = false
      }
    } else {
      //checking if status of mobile test needs an alert
      if (resultObject.kBInFullyLoaded >= averages.mobileKBInFullyLoaded) {
        Logger.info("PageWeight Alert Set")
        resultObject.pageWeightAlertDescription = "the page is too heavy. Please examine the list of embeds below for items that are unexpectedly large."
        resultObject.alertStatusPageWeight = true
      }
      else {
        Logger.info("PageWeight Alert not set")
        resultObject.alertStatusPageWeight = false
      }
      if ((resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) ||
        (resultObject.speedIndex >= averages.mobileSpeedIndex)) {
        Logger.info("PageSpeed alert set")
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
        Logger.info("PageSpeed alert not set")
        resultObject.alertStatusPageSpeed = false
      }
    }
    Logger.info("Returning test result with alert flags set to relevant values")
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
    Logger.info("TTFB for " + initialResult.testUrl + "\n therefore setting test count of: " + testCount)
    //   val AlertConfirmationTestResult: PerformanceResultsObject = setAlertStatusPageWeight(webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount), averages)
    webPageTest.testMultipleTimes(initialResult.testUrl, initialResult.typeOfTest, wptLocation, testCount)
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


  def findLatest(x: PerformanceResultsObject, y: PerformanceResultsObject): PerformanceResultsObject = {
    val timeX: Long = DateTime.parse(x.timeOfTest).getMillis
    val timeY: Long = DateTime.parse(y.timeOfTest).getMillis
    if (timeX > timeY)
      x
    else
      y
  }

  def getLatestResults(results: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val urls = for (urls <- results.map(_.testUrl)) yield urls
    val latestResults = for (url <- urls) yield {
      val resultsByUrl = results.filter(r => r.testUrl.contains(url))
      if(resultsByUrl.size > 1)
        resultsByUrl.reduceLeft(findLatest)
      else
        resultsByUrl.head
    }
    latestResults
  }

  def latestResultOnly(results: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {

    val resultsMobile: List[PerformanceResultsObject] = results.filter(_.typeOfTestName.contains("Mobile"))
    val resultsDesktop: List[PerformanceResultsObject] = results.filter(_.typeOfTest.contains("Desktop"))

    val latestResultsMobile = getLatestResults(resultsMobile)
    val latestResultsDesktop = getLatestResults(resultsDesktop)

    latestResultsMobile ::: latestResultsDesktop
  }


  def makeContentStub(passedHeadline: Option[String], passedLastModified: Option[CapiDateTime], passedLiveBloggingNow: Option[Boolean]): ContentFields = {
    val contentStub = new ContentFields {override def newspaperEditionDate: Option[CapiDateTime] = None

      override def internalStoryPackageCode: Option[Int] = None

      override def internalCommissionedWordcount: Option[Int] = None

      override def internalRevision: Option[Int] = None

      override def allowUgc: Option[Boolean] = None

      override def shortSocialShareText: Option[String] = None

      override def sensitive: Option[Boolean] = None

      override def shouldHideReaderRevenue: Option[Boolean] = None

      override def showAffiliateLinks: Option[Boolean] = None

      override def bodyText: Option[String] = None

      override def isLive: Option[Boolean] = passedLiveBloggingNow

      override def socialShareText: Option[String] = None

      override def internalShortId: Option[String] = None

      override def internalVideoCode: Option[String] = None

      override def charCount: Option[Int] = None

      override def internalContentCode: Option[Int] = None

      override def lang: Option[String] = None

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

  def getTagList(glabs: Boolean): List[Tag] = {
    if(glabs){

      val tag = new Tag {override def references: Seq[Reference] = Seq()

        override def bio: Option[String] = None

        override def paidContentCampaignColour: Option[String] = None

        override def `type`: TagType = TagType.Type

        override def entityIds: Option[collection.Set[String]] = None

        override def apiUrl: String = ""

        override def id: String = "uk-labs"

        override def r2ContributorId: Option[String] = None

        override def campaignInformationType: Option[String] = None

        override def bylineImageUrl: Option[String] = None

        override def lastName: Option[String] = None

        override def sectionName: Option[String] = None

        override def rcsId: Option[String] = None

        override def sectionId: Option[String] = None

        override def paidContentType: Option[String] = None

        override def emailAddress: Option[String] = None

        override def tagCategories: Option[collection.Set[String]] = None

        override def firstName: Option[String] = None

        override def twitterHandle: Option[String] = None

        override def webUrl: String = ""

        override def description: Option[String] = None

        override def bylineLargeImageUrl: Option[String] = None

        override def podcast: Option[Podcast] = None

        override def webTitle: String = ""

        override def activeSponsorships: Option[Seq[Sponsorship]] = None
      }

      List(tag)
    }
    else{
      val emptyTagList: List[Tag] = List()
      emptyTagList
    }
  }


  def jodaDateTimetoCapiDateTime(time: DateTime): CapiDateTime = {
    val iso8061String = time.toLocalDateTime.toString
    CapiDateTime.apply(time.getMillis, iso8061String)
  }

}


