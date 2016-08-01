import app.HtmlReportBuilder
import app.api.{DataSummary, JSONOperations, S3Operations}
import app.apiutils._
import com.gu.contentapi.client.model.v1.{MembershipTier, Office, ContentFields, CapiDateTime}
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
  val visualsPagesFileName = "visuals.conf"

 val resultsFromPreviousTests = "resultsFromPreviousTests.csv"
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

  //obtain list of items previously alerted on
  val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
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

/*  "Remove duplicates function" should "work as expected" in {
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
  //    val resultsFromPreviousTests = "resultsFromPreviousTestsTest.csv"
  //    val resultsFromPreviousTestsTestVersion = "resultsFromPreviousTestsTestOutput.csv"
  //   val resultsFromPreviousTests = "elementtestinput.csv"
  //   val resultsFromPreviousTestsTestVersion = "elementtestoutput.csv"
  //val resultsFromPreviousTests = "elementtestoutput.csv"
  val resultsFromPreviousTestsTestVersion = "elementtestoutputresuts.csv"
  val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
  val previousTestResultsHandler = new ResultsFromPreviousTests(previousResults)

  val originalLength = previousResults.length
  val listOfDupes = for (result <- previousTestResultsHandler.previousResults if previousTestResultsHandler.previousResults.map(page => (page.testUrl, page.typeOfTest)).count(_ == (result.testUrl,result.typeOfTest)) > 1) yield result
  var duplicatesPresent = false
  var allIsWell = false

  if (listOfDupes.nonEmpty) {
    println("\n\n\n\n ******** Duplicates found in previous results file! ****** \n Found " + listOfDupes.length + " duplicates")
    duplicatesPresent = true
  }
  val dedupedList = previousTestResultsHandler.removeDuplicates(previousTestResultsHandler.previousResults)
  if (duplicatesPresent && dedupedList.length < originalLength){
    println("duplicates found and deduped list has been shortened")
    println("checking for dupes")
    val secondTestForDupes = for (result <- dedupedList if dedupedList.map(page => (page.testUrl, page.typeOfTest)).count(_ == (result.testUrl,result.typeOfTest)) > 1) yield result
    if(secondTestForDupes.isEmpty){
      println("couldn't find any more dupes - we cleaned them all!")
      allIsWell = true
    } else{
      println("remove dupes did not remove all dupes")
    }
  } else{
    if(!duplicatesPresent && dedupedList.length == originalLength){
      allIsWell = true
      println("no duplicates found")
    }
  }
  assert(allIsWell)
}*/
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

/*  "vals in previous results object " should " be correct" in {
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
  }*/



  "Data Summary object " should " be able to produce a data summary from the results object" in {
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
  }

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

 /* "I" should "be able to get a list of resutls with no elements" in {
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

  def getResult(pageUrl: String, friendlyUrl: String, wptBaseUrl: String, wptApiKey: String, urlFragments: List[String] ): PerformanceResultsObject = {
    val xmlResultUrl = friendlyUrl.replaceAll("result","xmlResult")
    val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val result: PerformanceResultsObject = wpt.getResults(pageUrl, xmlResultUrl)
    result
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

}
