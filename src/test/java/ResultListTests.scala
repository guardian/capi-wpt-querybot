import app.api.JSONOperations
import app.api_utils.file_handling.{LocalFileOperations, S3Operations}
import app.api_utils.capi_queries.CapiRequests
import app.api_utils.model.{ArticleDefaultAverages, InteractiveDefaultAverages, LiveBlogDefaultAverages, PerformanceResultsObject}
import app.api_utils.reports.HtmlReportBuilder
import app.apiutils._
import app.api_utils.web_page_test.WebPageTest
import com.gu.contentapi.client.model.v1.{CapiDateTime, ContentFields, MembershipTier, Office}
import org.joda.time.DateTime
import org.scalatest._

/**
 * Created by mmcnamara on 01/06/16.
 */
abstract class ResultListUnitSpec extends FlatSpec with Matchers with
OptionValues with Inside with Inspectors

class ResultListTests extends ResultListUnitSpec with Matchers {
  val currentTime = DateTime.now
  val time1HourAgo = DateTime.now().minusHours(1)
  val time24HoursAgo = DateTime.now().minusHours(24)

  val capiTimeNow = new CapiDateTime {
    override def dateTime: Long = currentTime.getMillis
  }
  val capiTime1HourAgo = new CapiDateTime {
    override def dateTime: Long = time1HourAgo.getMillis
  }
  val capiTime24HoursAgo = new CapiDateTime {
    override def dateTime: Long = time24HoursAgo.getMillis
  }
  val capiTimeOld = new CapiDateTime {
    override def dateTime: Long = time24HoursAgo.getMillis - 1000
  }

  val emptyPerfResults: List[PerformanceResultsObject] = List()
  val iamTestingLocally = false
  /*#####################################################################################*/
  println("Job started at: " + DateTime.now)
  println("Local Testing Flag is set to: " + iamTestingLocally.toString)

  val jobStart = DateTime.now
  //  Define names of s3bucket, configuration and output Files
  val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
  val s3BucketName = "capi-wpt-querybot"
  val configFileName = "config.conf"
  val emailFileName = "addresses.conf"
  val interactiveSampleFileName = "interactivesamples.conf"
  val pagesWithInsecureElements = ""
  val visualsPagesFileName = "visuals.conf"

  val resultsFromPreviousTests = "resultsFromPreviousTests.csv"
//  val resultsFromPreviousTests = "interactives.csv"
// val resultsFromPreviousTests = "resultsFromPreviousTestsShortened.csv"
//val resultsFromPreviousTests = "shortenedresultstest.csv"
  val outputFile = "shortenedresultstest.csv"

  //Create new S3 Client
  val s3Interface = new S3Operations(s3BucketName, configFileName, emailFileName)
  var configArray: Array[String] = Array("", "", "", "", "", "")
  var urlFragments: List[String] = List()

  //Get config settings
  println("Extracting configuration values")
  if (!iamTestingLocally) {
    println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
    val returnTuple = s3Interface.getConfig
    configArray = Array(returnTuple._1, returnTuple._2, returnTuple._3, returnTuple._4, returnTuple._5, returnTuple._6, returnTuple._7)
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


  val previousResults = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
  //val previousResultsHandler = new ResultsFromPreviousTests(previousResults)

  val urlsToTest = List(
    "https://www.theguardian.com/education/live/2016/aug/25/gcse-results-day-2016-uk-students-get-their-grades-live",
    "https://www.theguardian.com/community/gallery/2015/oct/07/baking-disasters-im-sure-it-tastes-nice-readers-share-their-worst",
    "https://www.theguardian.com/sport/2014/jul/03/tour-de-france-2014-yellow-jersey-contenders-chris-froome-stats-history",
    "https://www.theguardian.com/world/2015/mar/16/london-teenagers-stopped-syria-parents-islamic-state",
    "https://www.theguardian.com/music/musicblog/2010/sep/02/wiley-ustream",
    "https://www.theguardian.com/football/2016/jun/20/ngolo-kante-france-euro-2016-caen-jose-saez",
    "https://www.theguardian.com/lifeandstyle/2016/jun/04/dont-talk-politics-sex-ex-10-ways-ruin-a-date"
  )

  val resultUrls = Array(
    "http://wpt.gu-web.net/xmlResult/160826_16_GZ/",
    "http://wpt.gu-web.net/xmlResult/160826_MN_H0/",
    "http://wpt.gu-web.net/xmlResult/160826_11_H1/",
    "http://wpt.gu-web.net/xmlResult/160826_BF_H2/",
    "http://wpt.gu-web.net/xmlResult/160826_W5_H3/",
    "http://wpt.gu-web.net/xmlResult/160826_9H_H4/",
    "http://wpt.gu-web.net/xmlResult/160826_E2_H5/",
    "http://wpt.gu-web.net/xmlResult/160826_VQ_H6/",
    "http://wpt.gu-web.net/xmlResult/160826_ST_H7/",
    "http://wpt.gu-web.net/xmlResult/160826_QR_H8/",
    "http://wpt.gu-web.net/xmlResult/160826_0V_H9/",
    "http://wpt.gu-web.net/xmlResult/160826_FH_HA/",
    "http://wpt.gu-web.net/xmlResult/160826_KM_HB/",
    "http://wpt.gu-web.net/xmlResult/160826_TX_HC/",
    "http://wpt.gu-web.net/xmlResult/160826_8S_HD/",
    "http://wpt.gu-web.net/xmlResult/160826_MA_HE/",
    "http://wpt.gu-web.net/xmlResult/160826_K4_W7/",
    "http://wpt.gu-web.net/xmlResult/160826_3G_HG/",
    "http://wpt.gu-web.net/xmlResult/160826_1Y_HH/",
    "http://wpt.gu-web.net/xmlResult/160826_TR_HJ/",
    "http://wpt.gu-web.net/xmlResult/160826_7X_HK/",
    "http://wpt.gu-web.net/xmlResult/160826_A7_HM/",
    "http://wpt.gu-web.net/xmlResult/160826_RX_HN/",
    "http://wpt.gu-web.net/xmlResult/160826_ZN_HP/",
    "http://wpt.gu-web.net/xmlResult/160826_18_HQ/",
    "http://wpt.gu-web.net/xmlResult/160826_YS_HR/",
    "http://wpt.gu-web.net/xmlResult/160826_S1_NX/",
    "http://wpt.gu-web.net/xmlResult/160826_16_GZ/"
  )

  val resultUrls2 = Array(
  "http://wpt.gu-web.net/xmlResult/160826_H6_WP/"
  )

  //obtain list of items previously alerted on
//  val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
  /*    val localInput = new LocalFileOperations


val fakeDashboardUrl = "http://www.theguardian.com/uk"
val testResult1m = new PerformanceResultsObject("testResult1", "Android/3G", "mobileArticlespeedIndexHigh", 1, 1, 1, 1, 1, 1, 1, "mobileArticlespeedIndexHigh", true, true, true)
val testResult1d = new PerformanceResultsObject("testResult1", "Desktop", "mobileArticlespeedIndexHigh", 1, 1, 1, 1, 1, 1, 1, "mobileArticlespeedIndexHigh", true, true, true)
val testResult2m = new PerformanceResultsObject("testResult2", "Android/3G", "mobileArticletFpHigh", 2, 2, 2, 2, 2, 2, 2, "mobileArticletFpHigh", true, true, true)
val testResult3m = new PerformanceResultsObject("testResult3", "Android/3G", "testResult3", 3, 3, 3, 3, 3, 3, 3, "testResult3", true, true, true)
val testResult3d = new PerformanceResultsObject("testResult3", "Desktop", "testResult3", 3, 3, 3, 3, 3, 3, 3, "testResult3", true, true, true)
val testResult4m = new PerformanceResultsObject("testResult4", "Android/3G", "testResult4", 4, 4, 4, 4, 4, 4, 4, "testResult4", false, false, false)
val testResult4d = new PerformanceResultsObject("testResult4", "Desktop", "testResult4", 4, 4, 4, 4, 4, 4, 4, "testResult4", false, false, false)
val testResult5m = new PerformanceResultsObject("testResult5", "Android/3G", "testResult5", 5, 5, 5, 5, 5, 5, 5, "testResult5", false, false, false)
val testResult5d = new PerformanceResultsObject("testResult5", "Desktop", "testResult5", 5, 5, 5, 5, 5, 5, 5, "testResult5", false, false, false)
val testResult6m = new PerformanceResultsObject("testResult6", "Android/3G", "testResult6", 6, 6, 6, 6, 6, 6, 6, "testResult6", false, false, false)
val testResult6d = new PerformanceResultsObject("testResult6", "Desktop", "testResult6", 6, 6, 6, 6, 6, 6, 6, "testResult6", false, false, false)

testResult1m.setPageLastUpdated(Option(capiTimeNow))
testResult1d.setPageLastUpdated(Option(capiTimeNow))
testResult2m.setPageLastUpdated(Option(capiTime1HourAgo))
testResult3m.setPageLastUpdated(Option(capiTimeOld))
testResult3d.setPageLastUpdated(Option(capiTimeOld))
testResult4m.setPageLastUpdated(Option(capiTime1HourAgo))
testResult4d.setPageLastUpdated(Option(capiTime1HourAgo))
testResult5m.setPageLastUpdated(Option(capiTime24HoursAgo))
testResult5d.setPageLastUpdated(Option(capiTime24HoursAgo))
testResult6m.setPageLastUpdated(Option(capiTimeOld))
testResult6d.setPageLastUpdated(Option(capiTimeOld))


testResult1m.setFirstPublished(Option(capiTimeNow))
testResult1d.setFirstPublished(Option(capiTimeNow))
testResult2m.setFirstPublished(Option(capiTime1HourAgo))
testResult3m.setFirstPublished(Option(capiTimeOld))
testResult3d.setFirstPublished(Option(capiTimeOld))
testResult4m.setFirstPublished(Option(capiTime1HourAgo))
testResult4d.setFirstPublished(Option(capiTime1HourAgo))
testResult5m.setFirstPublished(Option(capiTime24HoursAgo))
testResult5d.setFirstPublished(Option(capiTime24HoursAgo))
testResult6m.setFirstPublished(Option(capiTimeOld))
testResult6d.setFirstPublished(Option(capiTimeOld))

testResult4m.setLiveBloggingNow(true)
testResult4d.setLiveBloggingNow(true)


val oldResultList = List(testResult1m, testResult1d, testResult2m, testResult3m, testResult3d, testResult4m, testResult4d, testResult5m,  testResult5d, testResult6m, testResult6d)

val listWithDupes = List(testResult1d, testResult1m, testResult2m, testResult2m, testResult1d, testResult3d, testResult4d, testResult4m, testResult4m, testResult5d, testResult4m, testResult6d, testResult6m, testResult6m)

val prevResults = new ResultsFromPreviousTests(oldResultList)


val capiResult1: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi1"), Option(capiTimeNow), Option(false))), "testResult1")
val capiResult2: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi2"), Option(capiTime1HourAgo), Option(false))), "testResult2")
val capiResult3: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi3"), Option(capiTimeOld), Option(false))), "testResult3")
val capiResult4: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi4"), Option(capiTimeNow), Option(false))), "testResult4")
val capiResult5: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi5"), Option(capiTimeOld), Option(false))), "notInPreviousResults")

val capiResultList1New: List[(Option[ContentFields],String)] = List(capiResult1, capiResult2, capiResult3, capiResult5)
val capiResultList1New1Update: List[(Option[ContentFields],String)] = List(capiResult1, capiResult2, capiResult3, capiResult4, capiResult5)
*/
/*
"A previous results object" should "have a list of pages with deduped active alerts and live liveblogs from the last 24 hours" in {
  println("prevResults.dedupedPreviousResultsToRestest.length == " + prevResults.dedupedPreviousResultsToRestest.length)
  assert(prevResults.dedupedPreviousResultsToRestest.length == 3)
}*/

/*"Passing a list of CAPI results to previous results object" should "return elements that have not been tested already" in {
  println("prevResults.returnPagesNotYetTested(capiResultList1New).length == " + prevResults.returnPagesNotYetTested(capiResultList1New).length)
  assert(prevResults.returnPagesNotYetTested(capiResultList1New).length == 1)
}*/

/*"A list of CAPI results contains an item that has been tested but has since been modified" should "be returned too" in {
  println("prevResults.returnPagesNotYetTested(capiResultList1New1Update).length == " + prevResults.returnPagesNotYetTested(capiResultList1New1Update).length)
//    println("urls returned == " +  prevResults.returnPagesNotYetTested(capiResultList1New1Update).map(_._2))
  assert(prevResults.returnPagesNotYetTested(capiResultList1New1Update).length == 2)
}*/

/*"All lists in previous results object" should "be conistent in number" in {
  assert(prevResults.checkConsistency())
}
*/
/*  "messing with capi queries etc " should "gimmie what I want what I really really want" in {

  val iamTestingLocally = false
  /*#####################################################################################*/
  println("Job started at: " + DateTime.now)
  println("Local Testing Flag is set to: " + iamTestingLocally.toString)

  val jobStart = DateTime.now
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
    configArray = Array(returnTuple._1, returnTuple._2, returnTuple._3, returnTuple._4, returnTuple._5, returnTuple._6, returnTuple._7)
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
  //val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
  //    val localInput = new LocalFileOperations
  //val previousResults: List[PerformanceResultsObject] = localInput.getResultsFile(resultsFromPreviousTests)
  val previousTestResultsHandler = new ResultsFromPreviousTests(previousResults)
  println("\n\n\n ***** There are " + previousTestResultsHandler.previousResults.length + " previous results in file  ********* \n\n\n")
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
  val articles: List[(Option[ContentFields], String)] = capiQuery.getUrlsForContentType("Article")
  val liveBlogs: List[(Option[ContentFields], String)] = capiQuery.getUrlsForContentType("LiveBlog")
  val interactives: List[(Option[ContentFields], String)] = capiQuery.getUrlsForContentType("Interactive")
  val fronts: List[(Option[ContentFields], String)] = capiQuery.getUrlsForContentType("Front")
  val videoPages: List[(Option[ContentFields], String)] = capiQuery.getUrlsForContentType("Video")
  val audioPages: List[(Option[ContentFields], String)] = capiQuery.getUrlsForContentType("Audio")
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

  println("\n\n\n\n\n******   sizes of all the things! *******" )
  println("size of pages to retest = " + pagesToRetest.length)
  println("size of article urls from CAPI = " + articleUrls.length)
  println("size of liveblog urls from CAPI = " + liveBlogUrls.length)
  println("size of interactive urls from CAPI = " + interactiveUrls.length)
}
*/
/*
"Remove duplicates function" should "work as expected" in {
  //Create new S3 Client
  val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
  val s3BucketName = "capi-wpt-querybot"
  val configFileName = "config.conf"
  val emailFileName = "addresses.conf"

  println("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
  val s3Interface = new S3Operations(s3BucketName, configFileName, emailFileName)
  var configArray: Array[String] = Array("", "", "", "", "", "")
  var urlFragments: List[String] = List()

  println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
  val returnTuple = s3Interface.getConfig
  configArray = Array(returnTuple._1,returnTuple._2,returnTuple._3,returnTuple._4,returnTuple._5,returnTuple._6,returnTuple._7)
  urlFragments = returnTuple._8

  val contentApiKey: String = configArray(0)
  val wptBaseUrl: String = configArray(1)
  val wptApiKey: String = configArray(2)
  val wptLocation: String = configArray(3)

  val resultsFromPreviousTests = "resultsFromPreviousTests.csv"
  val resultsFromPreviousTestsBigTest = "resultsFromPreviousTests_really-big.csv"
  val resultsOutput = "dedupedResultsFromPreviousTests_really-big.csv"

  val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTestsBigTest)
  val previousTestResultsHandler = new ResultsFromPreviousTests(previousResults)

  val originalLength = previousResults.length
  val listOfDupes = for (result <- previousTestResultsHandler.previousResults if previousTestResultsHandler.previousResults.map(page => (page.testUrl, page.typeOfTest, page.getPageLastUpdated)).count(_ == (result.testUrl,result.typeOfTest, result.getPageLastUpdated)) > 1) yield result
  var duplicatesPresent = false
  var allIsWell = false
  println("\n\n\n\n file contains " + previousTestResultsHandler.previousResults.length + " records. \n")

  if (listOfDupes.nonEmpty) {
    println("******** Duplicates found in previous results file! ****** \n Found " + listOfDupes.length + " duplicates")
    duplicatesPresent = true
  }
  val dedupedList = previousTestResultsHandler.dedupeList(previousTestResultsHandler.previousResults)
  if (duplicatesPresent && dedupedList.length < originalLength){
    println("duplicates found and deduped list has been shortened")
    println("checking for dupes")
    val secondTestForDupes = for (result <- dedupedList if dedupedList.map(page => (page.testUrl, page.typeOfTest, page.getPageLastUpdated)).count(_ == (result.testUrl,result.typeOfTest,result.getPageLastUpdated)) > 1) yield result
    if(secondTestForDupes.isEmpty){
      println("couldn't find any more dupes - we cleaned them all!")
      println("deduplicated List length is: " + dedupedList.length)
      allIsWell = true
    } else{
      println("remove dupes did not remove all dupes")
      println("deduplicated List length is: " + dedupedList.length)
      println("there are " + secondTestForDupes.length + " duplicate records remaining")
    }
  } else{
    if(!duplicatesPresent && dedupedList.length == originalLength){
      allIsWell = true
      println("no duplicates found")
      s3Interface.writeFileToS3(resultsOutput, dedupedList.map(_.toCSVString()).mkString)
    }
  }
  assert(allIsWell)
}*/
/*  "previous results to retest" should "display correct results" in {
    val resultsToRetest = previousResultsHandler.dedupedPreviousResultsToRestest
    println("dedupedPreviousResultsToRetest\n")
    println(resultsToRetest.map(_.testUrl + "\n"))

    val hasAlertCount = previousResultsHandler.dedupedPreviousResultsToRestest.count(_.hasAlert)
    println("\n\n\nhasAlertCount\n")
    println( hasAlertCount + " pages out of " + previousResultsHandler.dedupedPreviousResultsToRestest.length + " in list.")

    val hasPreviouslyAlerted = previousResultsHandler.hasPreviouslyAlerted
    println("\n\n\nhasPreviouslyAlerted\n")
    println(hasPreviouslyAlerted.map(_.testUrl + "\n"))

    val pagesNotYetAlertedOn = previousResultsHandler.returnPagesNotYetAlertedOn(resultsToRetest)
    val nonLiveblogs = pagesNotYetAlertedOn.filter(!_.liveBloggingNow.getOrElse(false))
    println("\n\n\n pagesNotYetAlertedOn\n")
    println(pagesNotYetAlertedOn.map(_.testUrl + "\n"))

    println("\n\n\n nonLiveblogs\n")
    println(nonLiveblogs.map(_.testUrl + "\n"))
    assert(nonLiveblogs.isEmpty)
  }
*/
/*
  "Getting data from results file" should " allow me to repopulate data from tests" in {
  //Create new S3 Client
  val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
  val s3BucketName = "capi-wpt-querybot"
  val configFileName = "config.conf"
  val emailFileName = "addresses.conf"

  println("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
  val s3Interface = new S3Operations(s3BucketName, configFileName, emailFileName)
  var configArray: Array[String] = Array("", "", "", "", "", "")
  var urlFragments: List[String] = List()

  println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
  val returnTuple = s3Interface.getConfig
  configArray = Array(returnTuple._1,returnTuple._2,returnTuple._3,returnTuple._4,returnTuple._5,returnTuple._6,returnTuple._7)
  urlFragments = returnTuple._8

  val contentApiKey: String = configArray(0)
  val wptBaseUrl: String = configArray(1)
  val wptApiKey: String = configArray(2)
  val wptLocation: String = configArray(3)



 // val resultsFromPreviousTests = "resultsFromPreviousTests.csv"
//    val resultsFromPreviousTests = "resultsFromPreviousTestsTest.csv"
//    val resultsFromPreviousTestsTestVersion = "resultsFromPreviousTestsTestOutput.csv"
//   val resultsFromPreviousTests = "elementtestinput.csv"
//   val resultsFromPreviousTestsTestVersion = "elementtestoutput.csv"
  val resultsFromPreviousTests = "elementtestoutput.csv"
  val resultsFromPreviousTestsTestVersion = "elementtestoutputresuts.csv"
  //obtain list of items previously alerted on
  val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
  val previousTestResultsHandler = new ResultsFromPreviousTests(previousResults)

  val resurrectedResults: List[PerformanceResultsObject] = previousTestResultsHandler.previousResults.map(result => {
    val newResult = getResult(result.testUrl, result.friendlyResultUrl, wptBaseUrl, wptApiKey, urlFragments)
    newResult.headline = result.headline
    newResult.pageType = result.pageType
    newResult.firstPublished = result.firstPublished
    newResult.pageLastUpdated = result.pageLastUpdated
    newResult.liveBloggingNow = result.liveBloggingNow
    newResult.alertStatusPageWeight = result.alertStatusPageWeight
    newResult.alertStatusPageSpeed = result.alertStatusPageSpeed
    newResult.pageWeightAlertDescription = result.pageWeightAlertDescription
    newResult.pageSpeedAlertDescription = result.pageSpeedAlertDescription

    println("newResult created: \n Elements in list are: \n " + newResult.editorialElementList.map(element => element.resource + "\n"))
    println("\n\n\nEd Elements to csv string:\n" + newResult.editorialElementList.map(_.toCSVString()))
    newResult
  })
  val resultsToRecordCSVString: String = resurrectedResults.map(_.toCSVString()).mkString
  s3Interface.writeFileToS3(resultsFromPreviousTestsTestVersion, resultsToRecordCSVString)
  assert(s3Interface.doesFileExist(resultsFromPreviousTestsTestVersion))
}
*/

  /*"vals in previous results object " should " be correct" in {
    val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
    val testResultsHandler = new ResultsFromPreviousTests(previousResults)
    println("\n\n\n ***** There are " + testResultsHandler.previousResults.length + " previous results in file  ********* \n\n\n")
    println("resultsFromLast24Hours == " + testResultsHandler.resultsFromLast24Hours.length)
    println("oldResults == " + testResultsHandler.oldResults.length)
    println("previousResultsToRetest == " + testResultsHandler.previousResultsToRetest.length)
    println("recentButNoRetestRequired == " + testResultsHandler.recentButNoRetestRequired.length)
    println("desktopPreviousResultsToReTest == " + testResultsHandler.desktopPreviousResultsToReTest.length)
    println("mobilePreviousResultsToReTest == " + testResultsHandler.mobilePreviousResultsToReTest.length)
    println("dedupedMobilePreviousResultsToRetest == " + testResultsHandler.dedupedMobilePreviousResultsToRetest.length)
    println("dedupedPreviousResultsToRestest == " + testResultsHandler.dedupedPreviousResultsToRestest.length)
    println("resultsWithNoPageElements == " + testResultsHandler.resultsWithNoPageElements.length)

    val result = previousResults.tail.tail.head
    println("\n\n\n ******* checking contents of first editorial element: ")
    println("length of editorial elements list == " + result.editorialElementList.length)
    println("resource for first 3 elements == " + result.editorialElementList.head.resource + "\n" +
    result.editorialElementList.tail.head.resource + "\n" +
    result.editorialElementList.tail.tail.head.resource)
    println("info for first 3 elements == ")
    println("\nElement 1")
    println(result.editorialElementList.head.resource + "\n" +
      result.editorialElementList.head.contentType + "\n" +
      result.editorialElementList.head.requestStart + "\n" +
      result.editorialElementList.head.dnsLookUp + "\n" +
      result.editorialElementList.head.initialConnection + "\n" +
      result.editorialElementList.head.sslNegotiation + "\n" +
      result.editorialElementList.head.timeToFirstByte + "\n" +
      result.editorialElementList.head.contentDownload + "\n" +
      result.editorialElementList.head.bytesDownloaded + "\n" +
      result.editorialElementList.head.errorStatusCode + "\n" +
      result.editorialElementList.head.iP)

    println("\nElement 2")
    println(result.editorialElementList.tail.head.resource + "\n" +
      result.editorialElementList.tail.head.contentType + "\n" +
      result.editorialElementList.tail.head.requestStart + "\n" +
      result.editorialElementList.tail.head.dnsLookUp + "\n" +
      result.editorialElementList.tail.head.initialConnection + "\n" +
      result.editorialElementList.tail.head.sslNegotiation + "\n" +
      result.editorialElementList.tail.head.timeToFirstByte + "\n" +
      result.editorialElementList.tail.head.contentDownload + "\n" +
      result.editorialElementList.tail.head.bytesDownloaded + "\n" +
      result.editorialElementList.tail.head.errorStatusCode + "\n" +
      result.editorialElementList.tail.head.iP)

    println("\nElement 3")
    println(result.editorialElementList.tail.head.resource + "\n" +
      result.editorialElementList.tail.tail.head.contentType + "\n" +
      result.editorialElementList.tail.tail.head.requestStart + "\n" +
      result.editorialElementList.tail.tail.head.dnsLookUp + "\n" +
      result.editorialElementList.tail.tail.head.initialConnection + "\n" +
      result.editorialElementList.tail.tail.head.sslNegotiation + "\n" +
      result.editorialElementList.tail.tail.head.timeToFirstByte + "\n" +
      result.editorialElementList.tail.tail.head.contentDownload + "\n" +
      result.editorialElementList.tail.tail.head.bytesDownloaded + "\n" +
      result.editorialElementList.tail.tail.head.errorStatusCode + "\n" +
      result.editorialElementList.tail.tail.head.iP)

    println("Output of CSV String print: \n" + result.toCSVString())


   // val dataSummary = new DataSummary(time1HourAgo, currentTime, 10, 20, emptyPerfResults,testResultsHandler)
   // dataSummary.printSummaryDataToScreen()
    s3Interface.writeFileToS3(outputFile, previousResults.map(_.toCSVString()).mkString)
    assert(true)
  }
*/


/*  "Data Summary object " should " be able to produce a data summary from the results object" in {
 val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
    val testResultsHandler = new ResultsFromPreviousTests(previousResults)
    println("\n\n\n ***** There are " + testResultsHandler.previousResults.length + " previous results in file  ********* \n\n\n")

    val dataSummary = new DataSummary(time1HourAgo, currentTime, 10, 20, emptyPerfResults,testResultsHandler)
    dataSummary.printSummaryDataToScreen()
    //write summaries to files
    val runSummaryFile = "runSummarytest.txt"
    println("writing run summary data to new file")
    s3Interface.writeFileToS3(runSummaryFile, dataSummary.summaryDataToString())

    assert(true)
  }*/

/*  "previous alerts file" should "be repaired" in {
    val previousAlertsInput = "alerts/alerts.csv"
    val previousAlertsOutput = "alerts/pageWeightAlertsFromPreviousTests.csv"

    println("getting input file")
    val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(previousAlertsInput)
    println("repairing file")
    val unknownResults = previousResults.filter(_.typeOfTest.contains("Unknown"))
    val knownResults = previousResults.filter(!_.typeOfTest.contains("Unknown"))
    val fixedUnknownResults = getResultPages(unknownResults, urlFragments, wptBaseUrl, wptApiKey, wptLocation)
    val combinedResults = knownResults ::: fixedUnknownResults
    s3Interface.writeFileToS3(previousAlertsOutput, combinedResults.map(_.toCSVString()).mkString)
    assert(!combinedResults.map(_.timeToFirstByte).contains(-2))
  }*/

 /* "results written with full element list" should "be able to be read in" in {
    val s3TestFile = "testResultsWithFullElementList.csv"
    println("length of results file: " + previousResults.length)
    val testData = previousResults.take(500).map(_.toCSVStringTestOutput()).mkString
    //val localFileOperations = new LocalFileOperations
    s3Interface.writeFileToS3(s3TestFile, testData)
    val readInResults = s3Interface.getResultsFileFromS3(s3TestFile)
    val testResultsHandler = new ResultsFromPreviousTests(readInResults)
    val testDataSummary = new DataSummary(time1HourAgo, currentTime, 10, 20, emptyPerfResults,testResultsHandler)
    testDataSummary.printSummaryDataToScreen()
    assert(readInResults.nonEmpty)
  }*/

/*  "number of results with empty ed element list" should " be 0 in repaired results list" in {
    val previousResultsHandler = new ResultsFromPreviousTests(previousResults)
    val numberOfResultsWithMissingElementsBeforeFix = previousResultsHandler.countResultsWithNoElements()
    val numberOfResultsWithEditorialElementsBeforeFix = previousResultsHandler.previousResults.count(_.editorialElementList.nonEmpty)
    val listWithAddedElements = previousResultsHandler.reAddPageElementsToPastResults()
    val numberOfEmptyElementLists = listWithAddedElements.count(_.editorialElementList.isEmpty)
    val numberOfResultsWithElements = listWithAddedElements.count(_.editorialElementList.nonEmpty)
    println("before:\n This many with no elements: " + numberOfResultsWithMissingElementsBeforeFix)
    println("This many with elements: " + numberOfResultsWithEditorialElementsBeforeFix)
    println("after\n This many with no elements: \" + " + numberOfEmptyElementLists)
    println("This many with elements: " + numberOfResultsWithElements)
    println(numberOfResultsWithElements - numberOfResultsWithEditorialElementsBeforeFix + " Results have been repaired")

    assert(numberOfEmptyElementLists == 0)
  }*/

  /*"I" should "be able to get a list of resutls with no elements" in {
    val previousResultsHandler = new ResultsFromPreviousTests(previousResults.take(500))
    val listWithElements = previousResultsHandler.previousResults.filter(_.editorialElementList.nonEmpty)
    val listWithNoElements = previousResultsHandler.previousResults.filter(_.editorialElementList.isEmpty)
    val listWithAddedElements = previousResultsHandler.reAddPageElementsToPastResults()
    val remainingResultsWithNoElements = listWithAddedElements.filter(_.editorialElementList.isEmpty)
    val resultsWithElementList = listWithAddedElements.filter(_.editorialElementList.nonEmpty)
    val resultsWeHaveFixed = for (result <- resultsWithElementList if !listWithElements.contains(result)) yield result
    val resultsWeHaveBroken = for (result <-remainingResultsWithNoElements if listWithElements.contains(result)) yield result
    println("Elements that were repaired: \n" + resultsWeHaveFixed.map(result => "Result Url: " + result.testUrl + "\n Result TestPage: " + result.friendlyResultUrl + "\n Editorial Element List: " + result.editorialElementList.map(_.toHTMLRowString()).mkString + "\n\n"))
    println("results with elements before repair: " + listWithElements.length)
    println("results with elements after repair: " + resultsWithElementList.length)
    println("results with no elements before repair:" + listWithNoElements.length)
    println("results with no elements after repair" + remainingResultsWithNoElements.length)
    println("difference between list with elements before and after is " + (resultsWithElementList.length - listWithElements.length))
    println("length of fixed results list = "+ resultsWeHaveFixed.length)
    println("length of broken results list = "+ resultsWeHaveBroken.length)
    assert(resultsWithElementList.length > listWithElements.length && resultsWithElementList.head.editorialElementList.nonEmpty)

  }*/

 /* "refining results for a result" should "yield the result in question" in {
    val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val result = wpt.getResults("https://www.theguardian.com/dorset-cereals-bnb-awards/2016/aug/05/hay-barton-bb-a-taste-of-cornish-hospitality", "http://wpt.gu-web.net/xmlResult/160805_S5_BK/")
    println("result received")
    println(result.toCSVString())
    result.timeToFirstByte should be > 0
  }*/

  /*"not a test" should "produce a list of results for the list of urls to test provided" in {
    // val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val pageListResults = getResultPages(urlsToTest, urlFragments, wptBaseUrl, wptApiKey, wptLocation)
    val cAPIQuery = new ArticleUrls(contentApiKey)
    cAPIQuery.shutDown
    s3Interface.writeFileToS3("samplePagesToAddNew.csv", pageListResults.map(_.toCSVString()).mkString)
    assert(true)
  }*/


/*  "not a test" should "produce a list of results for the list of wpt results pages provided" in {
    val cAPIQuery = new ArticleUrls(contentApiKey)
    val resultUrlList = resultUrls2.toList
    val pageListResults: List[PerformanceResultsObject] = resultUrlList.map(url => getResult(url , wptBaseUrl, wptApiKey, urlFragments, cAPIQuery))
    cAPIQuery.shutDown
    s3Interface.writeFileToS3("samplePagesToAddNew2.csv", pageListResults.map(_.toCSVString()).mkString)
    assert(true)
  }*/

  /*"not a test" should "generate list of pages that request insecure elements" in {
    val previousResults = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
    val listOfPagesWithHTTPElements = previousResults.filter(_.fullElementList.exists(test => test.resource.take(5).contains("http:")))
    s3Interface.writeFileToS3("pagesWithInsecureElements.csv", listOfPagesWithHTTPElements.map(_.toCSVString()).mkString)
    println("written out file with " + listOfPagesWithHTTPElements.length + " rows")
    assert(true)
  }*/

  "not a test" should "generate list of pages with creation office and creator obtained from CAPI" in {
    val previousResults = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
    val capiQuery = new CapiRequests(contentApiKey)
    val resultsWithcreationOfficeandCreator = previousResults.map(result => {
      if(result.getPageType.contains("Interactive")){
        println("\n\n\n\n\n\n\n\n requesting capi result for: " + result.testUrl + "\n")
        val capiResult = capiQuery.getSinglePage(result.testUrl)
        val prod = capiResult._1.flatMap(_.productionOffice).map(_.name).getOrElse("Nothing there")
        val prodOffice = capiResult._1.flatMap(_.productionOffice.map(_.name)).getOrElse("Nothing there")
        println("\n\n\n CAPI RESULT: \n"  + "Result: \n" + capiResult.toString + "\n\n\n" + "attempt 1: \n" + prod + "\n" + "attempt 2: \n" + prodOffice + "\n\n\n" )
        result.productionOffice = capiResult._1.flatMap(_.productionOffice.map(_.name))
        result.createdBy = capiResult._4
        result
      } else
          result
    })

    //val resultsToWriteToFile = resultsWithcreationOfficeandCreator.map(_.toCSVString()).mkString
    //s3Interface.writeFileToS3("previousResultsWithCreatorInfo.csv", resultsToWriteToFile)
    //println("written out file with " + resultsWithcreationOfficeandCreator.length + " rows")
    assert(resultsWithcreationOfficeandCreator.filter(_.getPageType.contains("Interactive")).head.productionOffice.isDefined)
  }

  def getResult(resultUrl: String, wptBaseUrl: String, wptApiKey: String, urlFragments: List[String], articleUrls: CapiRequests): PerformanceResultsObject = {
    //val xmlResultUrl = friendlyUrl.replaceAll("result","xmlResult")
    val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val pageUrl = "unknown"
    val newResult = wpt.getResults(pageUrl, resultUrl)
    val cAPIPage = articleUrls.getSinglePage(newResult.testUrl)
    newResult.headline = cAPIPage._1.get.headline
    newResult.pageType = Option("Article")
    newResult.firstPublished = cAPIPage._1.get.firstPublicationDate
    newResult.pageLastUpdated = cAPIPage._1.get.lastModified
    newResult.liveBloggingNow = cAPIPage._1.get.liveBloggingNow
    setAlertStatus(newResult)
    newResult
  }

  def getInteractiveResult(resultUrl: String, wptBaseUrl: String, wptApiKey: String, urlFragments: List[String], articleUrls: CapiRequests): Option[PerformanceResultsObject] = {
    //val xmlResultUrl = friendlyUrl.replaceAll("result","xmlResult")
    val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val pageUrl = "unknown"
    val cAPIPage = articleUrls.getInteractives(1).headOption
    val populatedResult = cAPIPage.map { page =>
      val requestedPage = wpt.sendPage(page._3)
      val newResult = wpt.getResults(page._3, requestedPage)
      newResult.headline = page._1.get.headline
      newResult.pageType = Option("Interactive")
      newResult.firstPublished = page._1.get.firstPublicationDate
      newResult.pageLastUpdated = page._1.get.lastModified
      newResult.liveBloggingNow = page._1.get.liveBloggingNow
      setAlertStatus(newResult)
      newResult
    }
    populatedResult
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

  def getResultPages(results: List[String], urlFragments: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String): List[PerformanceResultsObject] = {
    val wpt: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val cAPIQuery = new CapiRequests(contentApiKey)
    val desktopResultList: List[PerformanceResultsObject] = results.map(page => {
      val cAPIPage = cAPIQuery.getSinglePage(page)
      val testresultpage =  wpt.sendPage(page)
      val newResult = wpt.getResults(page, testresultpage)
      newResult.headline = cAPIPage._1.get.headline
      newResult.pageType = Option("Article")
      newResult.firstPublished = cAPIPage._1.get.firstPublicationDate
      newResult.pageLastUpdated = cAPIPage._1.get.lastModified
      newResult.liveBloggingNow = cAPIPage._1.get.liveBloggingNow
      setAlertStatus(newResult)
    })
    val mobileResultList: List[PerformanceResultsObject] = results.map(page => {
      val cAPIPage = cAPIQuery.getSinglePage(page)
      val testresultpage =  wpt.sendMobile3GPage(page, wptLocation)
      val newResult = wpt.getResults(page, testresultpage)
      newResult.headline = cAPIPage._1.get.headline
      newResult.pageType = Option("Article")
      newResult.firstPublished = cAPIPage._1.get.firstPublicationDate
      newResult.pageLastUpdated = cAPIPage._1.get.lastModified
      newResult.liveBloggingNow = cAPIPage._1.get.liveBloggingNow
      setAlertStatus(newResult)
    })
    val combinedResults = desktopResultList ::: mobileResultList
    cAPIQuery.shutDown
    combinedResults.sortWith(_.testUrl > _.testUrl)
  }

  def setAlertStatus(resultObject: PerformanceResultsObject): PerformanceResultsObject = {
    //  Add results to string which will eventually become the content of our results file
    val averages = {
      if (resultObject.pageType.contains("Article")) {
        new ArticleDefaultAverages("Color does not apply")
      } else {
        if (resultObject.pageType.contains("LiveBlog")) {
          new LiveBlogDefaultAverages("Color does not apply")
        } else {
          new InteractiveDefaultAverages("Color does not apply")
        }
      }
    }

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
}
