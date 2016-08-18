
  import app.HtmlReportBuilder
  import app.api.{SummaryPage, DataSummary, JSONOperations, S3Operations}
  import app.apiutils._
  import com.gu.contentapi.client.model.v1.{MembershipTier, Office, ContentFields, CapiDateTime}
  import org.joda.time.DateTime
  import org.scalatest._

  /**
   * Created by mmcnamara on 01/06/16.
   */
  abstract class SummaryUnitSpec extends FlatSpec with Matchers with
  OptionValues with Inside with Inspectors

  class SummaryTests extends SummaryUnitSpec with Matchers {
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

    val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
    val testResultsHandler = new ResultsFromPreviousTests(previousResults)
    println("\n\n\n ***** There are " + testResultsHandler.previousResults.length + " previous results in file  ********* \n\n\n")

    val dataSummary = new DataSummary(time1HourAgo, currentTime, 10, 20, emptyPerfResults,testResultsHandler)
    //write summaries to files
    val runSummaryFile = "runSummaryStringtest.txt"
    val runSummaryHTMLFile = "runSummaryHTMLStringtest.html"



      "Element summary" should "be returned as string" in {
        val elementSummary = dataSummary.summaryList.head
        val dataSummaryString = dataSummary.returnElementSummaryAsHTMLString(elementSummary)
        println("\n\n\n Single summary: \n" + dataSummaryString)
      }

      "Data Summary object " should " be able to produce a data summary from the results object" in {
        println("writing run summary data to new file")
        val summaryPage = new SummaryPage(dataSummary)
        val summaryPageHTMLString = summaryPage.toString()
        s3Interface.writeFileToS3(runSummaryFile, dataSummary.summaryDataToString())
        s3Interface.writeFileToS3(runSummaryHTMLFile, summaryPageHTMLString)
        dataSummary.printSummaryDataToScreen()
        assert(true)
      }


  }



