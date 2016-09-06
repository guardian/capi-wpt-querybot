
  import app.HtmlReportBuilder
  import app.api._
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
    //val resultsFromPreviousTests = "resultFromPreviousTestsAlertsUpdated.csv"
    //val resultsFromPreviousTests = "resultsFromPreviousTestsGenerateSamplePages.csv"
    //val resultsFromPreviousTests = "resultFromPreviousTestsAmalgamated.csv"
    // val resultsFromPreviousTests = "resultsFromPreviousTestsShortened.csv"
    //val resultsFromPreviousTests = "shortenedresultstest.csv"
    val pageWeightAlertsFromPreviousTests = "alerts/pageWeightAlertsFromPreviousTests.csv"
    val outputFile = "summarytest.csv"

    val runSummaryFile = "runSummaryStringtestTestAlerts.txt"
    //val runSummaryHTMLFile = "runSummaryHTMLTestAlerts.html"
    val runSummaryHTMLFile = "summarypagetest.html"


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
    val previousPageWeightAlerts: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(pageWeightAlertsFromPreviousTests)
    val testResultsHandler = new ResultsFromPreviousTests(previousResults)
    val alertsResultsHandler = new ResultsFromPreviousTests(previousPageWeightAlerts)
    println("\n\n\n ***** There are " + testResultsHandler.allResults.length + " previous results in file  ********* \n\n\n")
    val audioboomcounter = previousResults.filter(_.editorialElementList.map(element => element.resource.contains("audio_clip_id")).contains(true))
    println("**** audioBoom counter: " + audioboomcounter.length)


    val dataSummary = new DataSummary(time1HourAgo, currentTime, 10, 20, emptyPerfResults, testResultsHandler, alertsResultsHandler)
    //write summaries to files
    val localFiles = new LocalFileOperations

    "Element summary" should "be returned as string" in {
      val elementSummary = dataSummary.summaryList.head
      val dataSummaryString = dataSummary.returnElementSummaryAsHTMLString(elementSummary)
      println("\n\n\n Single summary: \n" + dataSummaryString)
    }

    "Data Summary object " should " be able to produce a data summary from the results object" in {
      println("writing run summary data to new file")
      val summaryPage = new SummaryPage(dataSummary)
      val summaryPageHTMLString = summaryPage.toString()
      //s3Interface.writeFileToS3(runSummaryFile, dataSummary.summaryDataToString())
      //s3Interface.writeFileToS3(runSummaryHTMLFile, summaryPageHTMLString)

      //localFiles.writeLocalResultFile(runSummaryFile, dataSummary.summaryDataToString())
      //localFiles.writeLocalResultFile(runSummaryHTMLFile, summaryPageHTMLString)
      s3Interface.writeFileToS3(runSummaryFile, dataSummary.summaryDataToString())
      s3Interface.writeFileToS3(runSummaryHTMLFile, summaryPageHTMLString)
      dataSummary.printSummaryDataToScreen()
      assert(true)
    }

  /*  "Not a test PageElementSamples page " should "be populated and display correctly when I run this" in {
      val listAudioBoomElement = previousResults.filter(_.testUrl.contains("/world/2015/mar/16/london-teenagers-stopped-syria-parents-islamic-state"))
      val pageAndElements = listAudioBoomElement.map(page => (page.testUrl, page.editorialElementList.map(_.resource).mkString, page.editorialElementList.map(_.determinedResourceType)))
      val samplePage = new PageElementSamples(dataSummary)
//      localFiles.writeLocalResultFile("pageElementSamplePages.html", samplePage.toString())
      s3Interface.writeFileToS3("pageElementSamplePages.html", samplePage.toString())
      assert(true)
    }*/

/*    "Not a test but I " should "be able to get a list of pages with an example of each embed" in {

      def getPage(pageList: List[PerformanceResultsObject]): String = {
        if (pageList.nonEmpty) {
          pageList.head.testUrl
        } else {
          "coming soon"
        }
      }

      val audioboom: String = getPage(dataSummary.pagesWithAudioBoomEmbed)
      val brightcove: String = getPage(dataSummary.pagesWithBrightcoveEmbed)
      val cnn: String = getPage(dataSummary.pagesWithCNNEmbed)
      val dailymotion: String = getPage(dataSummary.pagesWithDailyMotionEmbed)
      val documentCloud: String = getPage(dataSummary.pagesWithDocumentCloudEmbed)
      val facebook: String = getPage(dataSummary.pagesWithFacebookEmbed)
      val formStack: String = getPage(dataSummary.pagesWithFormStackEmbed)
      val gif: String = getPage(dataSummary.pagesWithGifEmbed)
      val googleMaps: String = getPage(dataSummary.pagesWithGoogleMapsEmbed)
      val guardianAudio: String = getPage(dataSummary.pagesWithGuardianAudio)
      val guardianComments: String = getPage(dataSummary.pagesWithGuardianCommentsEmbed)
      val guardianVideo: String = getPage(dataSummary.pagesWithGuardianVideos)
      val guardianImages: String = getPage(dataSummary.pagesWithGuardianImages)
      val guardianUpload: String = getPage(dataSummary.pagesWithGuardianUpload)
      val guardianWitnessImage: String = getPage(dataSummary.pagesWithGuardianWitnessImageEmbed)
      val guardianWitnessVideo: String = getPage(dataSummary.pagesWithGuardianWitnessVideoEmbed)
      val hulu: String = getPage(dataSummary.pagesWithHuluEmbed)
      val image: String = getPage(dataSummary.pagesWithImageEmbed)
      val infoStrada: String = getPage(dataSummary.pagesWithInfoStradaEmbed)
      val instagram: String = getPage(dataSummary.pagesWithInstagramEmbed)
      val interactive: String = getPage(dataSummary.pagesWithInteractiveEmbed)
      val mp3: String = getPage(dataSummary.pagesWithMP3Embed)
      val mp4: String = getPage(dataSummary.pagesWithMP4Embed)
      val parliamentLiveTv: String = getPage(dataSummary.pagesWithParliamentLiveTvEmbed)
      val scribd: String = getPage(dataSummary.pagesWithScribdEmbed)
      val soundCloud: String = getPage(dataSummary.pagesWithSoundCloudEmbed)
      val spotify: String = getPage(dataSummary.pagesWithSpotifyEmbed)
      val twitter: String = getPage(dataSummary.pagesWithTwitterEmbed)
      val uStream: String = getPage(dataSummary.pagesWithUStreamEmbed)
      val vevo: String = getPage(dataSummary.pagesWithVevoEmbed)
      val vimeo: String = getPage(dataSummary.pagesWithVimeoEmbed)
      val vine: String = getPage(dataSummary.pagesWithVineEmbed)
      val youTube: String = getPage(dataSummary.pagesWithYouTubeEmbed)
      val unknownEmbed: String = getPage(dataSummary.pagesWithUnknownEmbed)

      val stringArray = Array(
        audioboom,
        brightcove,
        cnn,
        dailymotion,
        documentCloud,
        facebook,
        formStack,
        gif,
        googleMaps,
        guardianAudio,
        guardianComments,
        guardianVideo,
        guardianImages,
        guardianUpload,
        guardianWitnessImage,
        guardianWitnessVideo,
        hulu,
        image,
        infoStrada,
        instagram,
        interactive,
        mp3,
        mp4,
        parliamentLiveTv,
        scribd,
        soundCloud,
        spotify,
        twitter,
        uStream,
        vevo,
        vimeo,
        vine,
        youTube,
        unknownEmbed
      )

      val output: String =
        "audioboom: " + stringArray(0) + "\n" +
          "brightcove: " + stringArray(1) + "\n" +
          "cnn: " + stringArray(2) + "\n" +
          "dailymotion: " + stringArray(3) + "\n" +
          "documentCloud: " + stringArray(4) + "\n" +
          "facebook: " + stringArray(5) + "\n" +
          "formStack: " + stringArray(6) + "\n" +
          "gif: " + stringArray(7) + "\n" +
          "googleMaps: " + stringArray(8) + "\n" +
          "guardianAudio: " + stringArray(9) + "\n" +
          "guardianComments: " + stringArray(10) + "\n" +
          "guardianVideo: " + stringArray(11) + "\n" +
          "guardianImages: " + stringArray(12) + "\n" +
          "guardianUpload: " + stringArray(13) + "\n" +
          "guardianWitnessImage: " + stringArray(14) + "\n" +
          "guardianWitnessVideo: " + stringArray(15) + "\n" +
          "hulu: " + stringArray(16) + "\n" +
          "image: " + stringArray(17) + "\n" +
          "infoStrada: " + stringArray(18) + "\n" +
          "instagram: " + stringArray(19) + "\n" +
          "interactive: " + stringArray(20) + "\n" +
          "mp3: " + stringArray(21) + "\n" +
          "mp4: " + stringArray(22) + "\n" +
          "parliamentLiveTv: " + stringArray(23) + "\n" +
          "scribd: " + stringArray(24) + "\n" +
          "soundCloud: " + stringArray(25) + "\n" +
          "spotify: " + stringArray(26) + "\n" +
          "twitter: " + stringArray(27) + "\n" +
          "uStream: " + stringArray(28) + "\n" +
          "vevo: " + stringArray(29) + "\n" +
          "vimeo: " + stringArray(30) + "\n" +
          "vine: " + stringArray(31) + "\n" +
          "youTube: " + stringArray(32) + "\n" +
          "unknownEmbed: " + stringArray(33) + "\n"

      s3Interface.writeFileToS3("samplePagesForEmbedsFromAlerts.txt", "Sample Pages for each embed type: \n \n" + output)
      assert(true)
    }*/

  }



