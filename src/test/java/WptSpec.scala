import app.api.S3Operations
import app.apiutils.{PerformanceResultsObject, WebPageTest}
import org.scalatest.{FlatSpec, Matchers}

class WptSpec extends FlatSpec with Matchers{

  val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
  val s3BucketName = "capi-wpt-querybot"
  val configFileName = "config.conf"
  val emailFileName = "addresses.conf"

  val s3Ops = new S3Operations(s3BucketName, configFileName, emailFileName)

  val returnTuple = s3Ops.getConfig
  val configArray = Array(returnTuple._1, returnTuple._2, returnTuple._3, returnTuple._4, returnTuple._5, returnTuple._6, returnTuple._7)
  val urlFragments = returnTuple._8

  val contentApiKey: String = configArray(0)
  val wptBaseUrl: String = configArray(1)
  val wptApiKey: String = configArray(2)
  val wptLocation: String = configArray(3)

  val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)

  val dummyResultPageUrl = "http://wpt.gu-web.net/xmlResult/190416_Q9_1N/"

  "Requesting results for a result url" should "return those results" in {
    val result = wpt.getResults("https://aurl.com", dummyResultPageUrl)
    result shouldBe a[PerformanceResultsObject]
    result.editorialElementList.nonEmpty should be(true)
  }

  "A result page" should "generate a list of elements" in {

    val details = wpt.obtainPageRequestDetails(dummyResultPageUrl)
    details.nonEmpty should be(true)
  }



}
