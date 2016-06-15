import app.api.{PageWeightDashboardMobile, InteractiveEmailTemplate, PageWeightEmailTemplate}
import app.apiutils.{ResultsFromPreviousTests, PerformanceResultsObject}
import com.gu.contentapi.client.model.v1.{MembershipTier, Office, CapiDateTime, ContentFields}
import org.joda.time.DateTime
import org.scalatest._

/**
 * Created by mmcnamara on 26/05/16.
 */
abstract class HTMLPageUnitSpec extends FlatSpec with Matchers with
OptionValues with Inside with Inspectors

class HTMLPageTests extends HTMLPageUnitSpec with Matchers {
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
  val fakeOtherDashboardUrl = "http://www.theguardian.com/us"
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


  val combinedResultList = List(testResult1m, testResult1d, testResult2m, testResult3m, testResult3d, testResult4m, testResult4d, testResult5m,  testResult5d, testResult6m, testResult6d)
  val mobileResultList = List(testResult1m, testResult2m, testResult3m, testResult4m, testResult5m, testResult6m)
  val desktopResultList = List(testResult1d, testResult3d, testResult4d, testResult5d, testResult6d)

  //val prevResults = new ResultsFromPreviousTests(oldResultList)


  val capiResult1: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi1"), Option(capiTimeNow), Option(false))), "testResult1")
  val capiResult2: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi2"), Option(capiTime1HourAgo), Option(false))), "testResult2")
  val capiResult3: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi3"), Option(capiTimeOld), Option(false))), "testResult3")
  val capiResult4: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi4"), Option(capiTimeNow), Option(false))), "testResult4")
  val capiResult5: (Option[ContentFields],String) = (Option(makeContentStub(Option("capi5"), Option(capiTimeOld), Option(false))), "notInPreviousResults")

  val capiResultList1New: List[(Option[ContentFields],String)] = List(capiResult1, capiResult2, capiResult3, capiResult5)
  val capiResultList1New1Update: List[(Option[ContentFields],String)] = List(capiResult1, capiResult2, capiResult3, capiResult4, capiResult5)

/*  "An pageWeight Email list with 0 Results" should "contain results and page elements" in {
    val pageWeightEmail = new PageWeightEmailTemplate(List(), fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(pageWeightEmail.toString().contains(emptyListText))
  }*/

  "An results list with 1 Results" should "generate a pageweightDashboard with 1 result" in {
    val mobilePageWeightDashboard = new PageWeightDashboardMobile(combinedResultList, desktopResultList, mobileResultList)
    val htmlString = mobilePageWeightDashboard.toString()
   // val mobiletablebody = htmlString.substring(htmlString.indexOf("<tbody>"),htmlString.indexOf("</tbody>")+"</tbody>".length)
    val numberOfResults = countPageRows(htmlString)
    assert(numberOfResults == 22)
  }
/*
  "An pageWeight Email list with 2 Results" should "contain results and page elements" in {
    val pageWeightEmail = new PageWeightEmailTemplate(testResultList2results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(pageWeightEmail.toString().contains(singleResultText) && pageWeightEmail.toString().contains(twoResultsText))
  }

  "An pageWeight Email list with 3 Results" should "contain results and page elements" in {
    val pageWeightEmail = new PageWeightEmailTemplate(testResultList3results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(pageWeightEmail.toString().contains(singleResultText) && pageWeightEmail.toString().contains(twoResultsText) && pageWeightEmail.toString().contains(threeResultsText))
  }

  "An interactive Email list with 0 Results" should "contain results and page elements" in {
    val interactiveEmail = new InteractiveEmailTemplate(List(), fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(interactiveEmail.toString().contains(emptyListText))
  }

  "An interactive Email list with 1 Results" should "contain results and page elements" in {
    val interactiveEmail = new InteractiveEmailTemplate(testResultList1results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(interactiveEmail.toString().contains(singleResultText))
  }

  "An interactive Email list with 2 Results" should "contain results and page elements" in {
    val interactiveEmail = new InteractiveEmailTemplate(testResultList2results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(interactiveEmail.toString().contains(singleResultText) && interactiveEmail.toString().contains(twoResultsText))
  }

  "An interactive Email list with 3 Results" should "contain results and page elements" in {
    val interactiveEmail = new InteractiveEmailTemplate(testResultList3results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(interactiveEmail.toString().contains(singleResultText) && interactiveEmail.toString().contains(twoResultsText) && interactiveEmail.toString().contains(threeResultsText))
  }*/


  def countPageRows(page: String): Int = {
    countWord("pageclass", page)
  }


  def countWord(word:String, phrase: String): Int = {
    if(phrase.isEmpty){
      println("emptystring")
      0
    } else{
      if(phrase.contains(word)){
        println("combination found")
        val phrasefragment = phrase.substring(phrase.indexOf(word) + word.length,phrase.length)
        1 + countWord(word, phrasefragment)
      }else{
        println("no more instances of combination")
        0
      }
    }
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
