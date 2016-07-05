import app.api.S3Operations
import app.apiutils.{ResultsFromPreviousTests, PerformanceResultsObject}
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

  "When Remove Duplicates is called on a list it" should "return the correct list" in {
    val cleanedList = prevResults.removeDuplicates(listWithDupes)
    assert(cleanedList.length == 9)
//    println(cleanedList.map(result => (result.testUrl,result.typeOfTest)).mkString)
    val cleanedListArray = cleanedList.toArray
    assert((cleanedListArray(0).testUrl, cleanedListArray(0).typeOfTest) == (testResult1d.testUrl,testResult1d.typeOfTest))
    assert((cleanedListArray(1).testUrl, cleanedListArray(1).typeOfTest) == (testResult1m.testUrl,testResult1m.typeOfTest))
    assert((cleanedListArray(2).testUrl, cleanedListArray(2).typeOfTest) == (testResult2m.testUrl,testResult2m.typeOfTest))
    assert((cleanedListArray(3).testUrl, cleanedListArray(3).typeOfTest) == (testResult3d.testUrl,testResult3d.typeOfTest))
    assert((cleanedListArray(4).testUrl, cleanedListArray(4).typeOfTest) == (testResult4d.testUrl,testResult4d.typeOfTest))
    assert((cleanedListArray(5).testUrl, cleanedListArray(5).typeOfTest) == (testResult4m.testUrl,testResult4m.typeOfTest))
    assert((cleanedListArray(6).testUrl, cleanedListArray(6).typeOfTest) == (testResult5d.testUrl,testResult5d.typeOfTest))
    assert((cleanedListArray(7).testUrl, cleanedListArray(7).typeOfTest) == (testResult6d.testUrl,testResult6d.typeOfTest))
    assert((cleanedListArray(8).testUrl, cleanedListArray(8).typeOfTest) == (testResult6m.testUrl,testResult6m.typeOfTest))
  }

  "When removeDuplicates is called on a list with no duplicates it" should "return an identical list" in {
    val cleanedList = prevResults.removeDuplicates(oldResultList)
    assert(cleanedList == oldResultList)
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
