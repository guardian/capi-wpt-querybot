import app.api.{S3Operations, PageWeightEmailTemplate}
import app.apiutils._
import org.scalatest._

/**
 * Created by mmcnamara on 26/05/16.
 */
abstract class AlertUnitSpec extends FlatSpec with Matchers with
OptionValues with Inside with Inspectors

class AlertSettingTests extends AlertUnitSpec with Matchers {

  val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
  val s3BucketName = "capi-wpt-querybot"
  val configFileName = "config.conf"
  val emailFileName = "addresses.conf"
  val interactiveSampleFileName = "interactivesamples.conf"
  val visualsPagesFileName = "visuals.conf"

  val resultsFromPreviousTests = "resultsFromPreviousTests.csv"
  //val resultsFromPreviousTests = "resultsFromPreviousTestsGenerateSamplePages.csv"
  //val resultsFromPreviousTests = "resultFromPreviousTestsAmalgamated.csv"
  // val resultsFromPreviousTests = "resultsFromPreviousTestsShortened.csv"
  //val resultsFromPreviousTests = "shortenedresultstest.csv"
  val pageWeightAlertsFromPreviousTests = "alerts/pageWeightAlertsFromPreviousTests.csv"
  val outputFile = "summarytest.csv"

  //Create new S3 Client
  val s3Interface = new S3Operations(s3BucketName, configFileName, emailFileName)
  var configArray: Array[String] = Array("", "", "", "", "", "")
  var urlFragments: List[String] = List()

  val articlePerformanceAverages = new ArticleDefaultAverages()
  
  val mobileArticlespeedIndexHigh = new PerformanceResultsObject("mobileArticlespeedIndexHigh", "Android/3G", "mobileArticlespeedIndexHigh", 1, 1, 1, 1, 1, 1, articlePerformanceAverages.mobileSpeedIndex + 1, "mobileArticlespeedIndexHigh", false, false, false)
  val mobileArticletFpHigh = new PerformanceResultsObject("mobileArticletFpHigh", "Android/3G", "mobileArticletFpHigh", 2, articlePerformanceAverages.mobileTimeFirstPaintInMs + 1, 2, 2, 2, 2, 2, "mobileArticletFpHigh", false, false, false)
  val mobileArticleTfpAndSpeedIndexHigh = new PerformanceResultsObject("testResult3", "Android/3G", "testResult3", 3, articlePerformanceAverages.mobileTimeFirstPaintInMs + 1, 3, 3, 3, 3, articlePerformanceAverages.mobileSpeedIndex + 1, "testResult3", true, true, true)

  //alert description text:
  val speedIndexHighOnly = "Time till page looks loaded (SpeedIndex) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
  val tFPHighOnly = "Time till page is scrollable (time-to-first-paint) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
  val bothtFPandSpeedIndexAreHigh  =  "Time till page is scrollable (time-to-first-paint) and time till page looks loaded (SpeedIndex) are unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."

  "A Mobile Performance Result with a SpeedIndex above threshold" should "contain a proper alert message" in {
    val testResult = app.App.setAlertStatus(mobileArticlespeedIndexHigh, articlePerformanceAverages)
    println(testResult.pageWeightAlertDescription)
    //      println(pageWeightEmail.toString())
    assert(testResult.alertStatusPageSpeed && testResult.pageWeightAlertDescription.contains(speedIndexHighOnly))
  }

  "A Mobile Performance Result with a timeToFirstPaint above threshold" should "contain a proper alert message" in {
    val performanceAverages = new ArticleDefaultAverages()
    val testResult = app.App.setAlertStatus(mobileArticletFpHigh, articlePerformanceAverages)
    println(testResult.pageWeightAlertDescription)
    //      println(pageWeightEmail.toString())
    assert(testResult.alertStatusPageSpeed && testResult.pageWeightAlertDescription.contains(tFPHighOnly))
  }

  "A Mobile Performance Result with Both tFP and SpeedIndex above threshold" should "contain a proper alert message" in {
    val performanceAverages = new ArticleDefaultAverages()
    val testResult = app.App.setAlertStatus(mobileArticletFpHigh, articlePerformanceAverages)
    println(testResult.pageWeightAlertDescription)
    //      println(pageWeightEmail.toString())
    assert(testResult.alertStatusPageSpeed && testResult.pageWeightAlertDescription.contains(bothtFPandSpeedIndexAreHigh))
  }

  "Not a test - Revise the alert settings according to current page averages" should "work" in {
    val outputFile = "resultFromPreviousTestsAlertsUpdated.csv"
    val previousResults = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
    val previousArticleResults = previousResults.filter(_.getPageType.contains("Article"))
    val previousInteractiveResults = previousResults.filter(_.getPageType.contains("Interactive"))
    val previousLiveBlogResults = previousResults.filter(_.getPageType.contains("LiveBlog"))

    def setAlerts(result: PerformanceResultsObject): PerformanceResultsObject = {
      val articleAverages = new ArticleDefaultAverages()
      val interactiveAverages = new InteractiveDefaultAverages()
      val liveBlogAverages = new LiveBlogDefaultAverages()
      result.getPageType match {
        case "Article" => app.App.setAlertStatus(result, articleAverages)
        case "Interactive" => app.App.setAlertStatus(result, interactiveAverages)
        case "LiveBlog" => app.App.setAlertStatus(result, liveBlogAverages)
        case _ => {result}

      }
    }
    val updatedResults: List[PerformanceResultsObject] = previousResults.map(result => setAlerts(result))
    s3Interface.writeFileToS3(outputFile, updatedResults.map(_.toCSVString()).mkString)
    assert(updatedResults.nonEmpty)
  }


}
