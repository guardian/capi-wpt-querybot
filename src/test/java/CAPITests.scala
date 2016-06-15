import app.api.S3Operations
import app.apiutils.{ArticleUrls, LocalFileOperations, ResultsFromPreviousTests, PerformanceResultsObject}
import com.gu.contentapi.client.model.v1.{MembershipTier, Office, ContentFields, CapiDateTime}
import org.joda.time.DateTime
import org.scalatest._

/**
 * Created by mmcnamara on 01/06/16.
 */
abstract class CAPITestUnitSpec extends FlatSpec with Matchers with
OptionValues with Inside with Inspectors

class CAPITests extends CAPITestUnitSpec with Matchers {
  val iamTestingLocally = false
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

  def getCAPISettings(): Array[String] ={
    println("Extracting configuration values")
    var configArray: Array[String] = Array("", "", "", "", "", "")
    var urlFragments: List[String] = List()
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
    configArray
  }



  val s3BucketName = "capi-wpt-querybot"
  val configFileName = "config.conf"
  val emailFileName = "addresses.conf"


  val s3Interface = new S3Operations(s3BucketName, configFileName, emailFileName)
  val configArray: Array[String] = getCAPISettings()
  val contentApiKey: String = configArray(0)

  val capiHandler = new ArticleUrls(contentApiKey)

  "A request to CAPI for Articles" should "return 1 or more results" in {
    val resultsList = capiHandler.getUrlsForContentType("Article")
    assert(resultsList.nonEmpty)
  }

}
