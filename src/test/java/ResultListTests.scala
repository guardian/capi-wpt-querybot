import app.api.S3Operations
import app.apiutils.{WebPageTest, EmailOperations, ResultsFromPreviousTests, PerformanceResultsObject}
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

  "A previous results object" should "have a list of pages with deduped active alerts and live liveblogs from the last 24 hours" in {
    println("prevResults.dedupedPreviousResultsToRestest.length == " + prevResults.dedupedPreviousResultsToRestest.length)
    assert(prevResults.dedupedPreviousResultsToRestest.length == 3)
  }

  "Passing a list of CAPI results to previous results object" should "return elements that have not been tested already" in {
    println("prevResults.returnPagesNotYetTested(capiResultList1New).length == " + prevResults.returnPagesNotYetTested(capiResultList1New).length)
    assert(prevResults.returnPagesNotYetTested(capiResultList1New).length == 1)
  }

  "A list of CAPI results contains an item that has been tested but has since been modified" should "be returned too" in {
    println("prevResults.returnPagesNotYetTested(capiResultList1New1Update).length == " + prevResults.returnPagesNotYetTested(capiResultList1New1Update).length)
//    println("urls returned == " +  prevResults.returnPagesNotYetTested(capiResultList1New1Update).map(_._2))
    assert(prevResults.returnPagesNotYetTested(capiResultList1New1Update).length == 2)
  }

  "All lists in previous results object" should "be conistent in number" in {
    assert(prevResults.checkConsistency())
  }


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

    val resurrectedResults: List[PerformanceResultsObject] = previousTestResultsHandler.fullResultsList.map(result => {
      val newResult = getResult(result.friendlyResultUrl, wptBaseUrl, wptApiKey, urlFragments)
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

  def getResult(friendlyUrl: String, wptBaseUrl: String, wptApiKey: String, urlFragments: List[String] ): PerformanceResultsObject = {
    val xmlResultUrl = friendlyUrl.replaceAll("result","xmlResult")
    val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val result: PerformanceResultsObject = wpt.getResults(xmlResultUrl)
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
