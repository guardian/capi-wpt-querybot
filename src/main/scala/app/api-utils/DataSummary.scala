package app.api

import app.apiutils.{PageElementFromHTMLTableRow, ResultsFromPreviousTests, PerformanceResultsObject}
import org.joda.time.DateTime


/**
 * Created by mmcnamara on 28/06/16.
 */
class DataSummary(jobStarted: DateTime, jobFinished: DateTime, numberOfPagesFromCapi: Int, numberOfPagesTested: Int, latestResults: List[PerformanceResultsObject], previousResultsObject: ResultsFromPreviousTests) {
  /*
Data summary
Its time to take note of pages tested each run,
breakdown by which were from CAPI and which were retested items
Time run took to complete
Number of error pages

List of pages for today
List of pages for yesterday
number of articles alerted on for pageWeight
number of articles alerted on for pageSpeed

number of prior pageWeight alerts that have been resolved
number of prior pageSpeed alerts that have been resolved

look through data - what are the main embeds
                  - what is the average weight of these embeds
                  - which embeds are most associated with pageWeight alert
                  - which embeds are most associated with pageSpeed alert

 */
  //current run tab metrics

  case class ElementSummaryData(title: String,
                                numberOfPagesWithEmbed: Int,
                                numberOfPageWeightAlerts: Int,
                                numberOfPageSpeedAlerts: Int,
                                percentageOfPageWeightAlerts: Int,
                                percentageOfPageSpeedAlerts: Int,
                                averageSizeOfEmbeds: Double,
                                averageTimeFirstPaint: Double,
                                averageSpeedIndexMs: Double)


val timeNow = DateTime.now
  val today = timeNow.getDayOfYear
  val yesterday = timeNow.minusDays(1).getDayOfYear

  val jobStartedTime = jobStarted
  val jobFinishTime = jobFinished
  val durationOfRunMs = jobFinished.getMillis - jobStarted.getMillis
  val durationOfRunS = durationOfRunMs.toDouble / 1000
  val durationOfRunMin = roundAt(2)(durationOfRunS / 60)

  //last run - number of pages pulled from capi
  val numberOfPagesFromCAPI: Int = numberOfPagesFromCapi
  val numberOfPagesSentToWPT: Int = numberOfPagesTested
  val numberOfFailedTests = latestResults.count(_.timeToFirstByte < 0)
  /*  - number of pageWeight alerts raised on non-interactive content
    - number of pageSpeed alerts raised on non-interactive content
    - number of page speed alerts raised where content was underweight
    - number of interactives tested
    - number of alerts raised on interactives
    - number of failed tests
*/


  val previousResultsHandler = previousResultsObject
  val numberOfPagesRetestedFromLastRun: Int = previousResultsHandler.previousResultsToRetest.length

  val resultsFromRun: List[PerformanceResultsObject] = latestResults
  val previousResults: List[PerformanceResultsObject] = previousResultsHandler.previousResults
  val allResults: List[PerformanceResultsObject] = resultsFromRun ::: previousResults
  val hasPreviouslyAlertedOnWeight: List[PerformanceResultsObject] = previousResultsHandler.hasPreviouslyAlerted.filter(_.alertStatusPageWeight)
  val hasPreviouslyAlertedOnSpeed: List[PerformanceResultsObject] = previousResultsHandler.hasPreviouslyAlerted.filter(_.alertStatusPageSpeed)

  val todaysResults = for (result <- allResults if DateTime.parse(result.timeOfTest).getDayOfYear == today) yield result
  val yesterdaysResults = for (result <- allResults if DateTime.parse(result.timeOfTest).getDayOfYear != yesterday) yield result

  val activePageWeightAlerts = resultsFromRun.filter(_.alertStatusPageWeight)
  val activePageSpeedAlerts = resultsFromRun.filter(_.alertStatusPageSpeed)
  val activeSlowButUnderWeight = activePageSpeedAlerts.filter(!_.alertStatusPageWeight)

  val newPageWeightAlerts = for (result <- activePageWeightAlerts if !hasPreviouslyAlertedOnWeight.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result
  val newPageSpeedAlerts = for (result <- activePageSpeedAlerts if !hasPreviouslyAlertedOnSpeed.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result

  val pageWeightAlertsThatHaveBeenResolved = for (result <- hasPreviouslyAlertedOnWeight if !activePageWeightAlerts.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result
  val pageSpeedAlertsThatHaveBeenResolved = for (result <- hasPreviouslyAlertedOnSpeed if !activePageSpeedAlerts.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result
  val pageSpeedAlertsResolvedForUnderweightPages = pageSpeedAlertsThatHaveBeenResolved.filter(!_.alertStatusPageWeight)

  // todo - Need some way of persisting these values
  val numberOfPageWeightAlertsResolvedThisRun = pageWeightAlertsThatHaveBeenResolved.length
  val numberOfPageSpeedAlertsResolvedThisRun = pageSpeedAlertsThatHaveBeenResolved.length
  val numberOfPageSpeedAlertsResolvedForUnderWeightPagesThisRun = pageSpeedAlertsThatHaveBeenResolved.length

  // todo - Use persisted values from previous runs
  //val numberOfPageWeightAlertsResolvedLast24Hrs = numberOfPageWeightAlertsResolvedThisRun + Some_value_we_store
  //val numberOfPageSpeedAlertsResolvedLast24Hrs = numberOfPageSpeedAlertsResolvedThisRun + Some_value_we_store
  //val numberOfPageSpeedAlertsResolvedForUnderWeightPagesLast24Hrs = numberOfPageSpeedAlertsResolvedForUnderWeightPagesThisRun + Some_value_we_store

  //val numberOfPageWeightAlertsResolvedSoFar = numberOfPageWeightAlertsResolvedThisRun + Some_value_we_store
  //val numberOfPageSpeedAlertsResolvedSoFar = numberOfPageSpeedAlertsResolvedThisRun + Some_value_we_store
  //val numberOfPageSpeedAlertsResolvedForUnderWeightPagesSoFar = numberOfPageSpeedAlertsResolvedForUnderWeightPagesThisRun + Some_value_we_store


  val pagesWithAudioBoomEmbed = allResults.filter(pageContainsElementType(_, "audioBoom"))
  val pagesWithBrightcoveEmbed = allResults.filter(pageContainsElementType(_, "brightcove"))
  val pagesWithCNNEmbed = allResults.filter(pageContainsElementType(_, "cnn"))
  val pagesWithDailyMotionEmbed = allResults.filter(pageContainsElementType(_, "dailymotion"))
  val pagesWithDocumentCloudEmbed = allResults.filter(pageContainsElementType(_, "documentCloud"))
  val pagesWithFacebookEmbed = allResults.filter(pageContainsElementType(_, "facebook"))
  val pagesWithFormStackEmbed = allResults.filter(pageContainsElementType(_, "formstack"))
  val pagesWithGifEmbed = allResults.filter(pageContainsElementType(_, "Gif Embed"))
  val pagesWithGoogleMapsEmbed = allResults.filter(pageContainsElementType(_, "googlemaps"))
  val pagesWithGuardianAudio = allResults.filter(pageContainsElementType(_, "guardianAudio"))
  val pagesWithGuardianCommentsEmbed = allResults.filter(pageContainsElementType(_, "guardianComments"))
  val pagesWithGuardianVideos = allResults.filter(pageContainsElementType(_, "guardianVideos"))
  val pagesWithGuardianImages = allResults.filter(pageContainsElementType(_, "guardianImages"))
  val pagesWithGuardianUpload = allResults.filter(pageContainsElementType(_, "guardianUpload"))
  val pagesWithGuardianWitnessImageEmbed = allResults.filter(pageContainsElementType(_, "guardianWitnessImage"))
  val pagesWithGuardianWitnessVideoEmbed = allResults.filter(pageContainsElementType(_, "guardianWitnessVideo"))
  val pagesWithHuluEmbed = allResults.filter(pageContainsElementType(_, "hulu"))
  val pagesWithImageEmbed = allResults.filter(pageContainsElementType(_, "Image Embed"))
  val pagesWithInfoStradaEmbed = allResults.filter(pageContainsElementType(_, "infostrada"))
  val pagesWithInstagramEmbed = allResults.filter(pageContainsElementType(_, "instagram"))
  val pagesWithInteractiveEmbed = allResults.filter(pageContainsElementType(_, "interactive"))
  val pagesWithMP3Embed = allResults.filter(pageContainsElementType(_, "Audio Embed"))
  val pagesWithMP4Embed = allResults.filter(pageContainsElementType(_, "Video Embed"))
  val pagesWithParliamentLiveTvEmbed = allResults.filter(pageContainsElementType(_, "parliamentLiveTv"))
  val pagesWithScribdEmbed = allResults.filter(pageContainsElementType(_, "scribd"))
  val pagesWithSoundCloudEmbed = allResults.filter(pageContainsElementType(_, "soundcloud"))
  val pagesWithSpotifyEmbed = allResults.filter(pageContainsElementType(_, "spotify"))
  val pagesWithTwitterEmbed = allResults.filter(pageContainsElementType(_, "twitter"))
  val pagesWithUStreamEmbed = allResults.filter(pageContainsElementType(_, "uStream"))
  val pagesWithVevoEmbed = allResults.filter(pageContainsElementType(_, "vevo"))
  val pagesWithVimeoEmbed = allResults.filter(pageContainsElementType(_, "vimeo"))
  val pagesWithVineEmbed = allResults.filter(pageContainsElementType(_, "vine"))
  val pagesWithYouTubeEmbed = allResults.filter(pageContainsElementType(_, "youTube"))
  val pagesWithUnknownEmbed = allResults.filter(pageContainsElementType(_, "unknownElement"))

  val fullListOfEditorialElements = allResults.flatMap(result => result.editorialElementList)
  val mapOfElementsByType = fullListOfEditorialElements.groupBy(_.identifyPageElementType())

  val audioBoom: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("audioBoom", List())
  val brightcove: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("brightcove", List())
  val cnn: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("cnn", List())
  val dailymotion: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("dailymotion", List())
  val documentCloud: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("documentCloud", List())
  val facebook: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("facebook", List())
  val formstack: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("formstack", List())
  val gif: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("Gif Embed", List())
  val googlemaps: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("googlemaps", List())
  val guardianAudio: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianAudio", List())
  val guardianComments: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianComments", List())
  val guardianVideos: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianVideos", List())
  val guardianImages: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianImages", List())
  val guardianUpload: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianUpload", List())
  val guardianWitnessImage: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianWitnessImage", List())
  val guardianWitnessVideo: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianWitnessVideo", List())
  val hulu: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("hulu", List())
  val imageEmbed: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("Image Embed", List())
  val infostrada: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("infostrada", List())
  val instagram: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("instagram", List())
  val interactive: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("interactive", List())
  val mP3: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("Audio Embed", List())
  val mP4: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("Video Embed", List())
  val parliamentLiveTv: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("parliamentLiveTv", List())
  val scribd: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("scribd", List())
  val soundCloud: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("soundCloud", List())
  val spotify: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("spotify", List())
  val twitter: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("twitter", List())
  val uStream: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("uStream", List())
  val vevo: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("vevo", List())
  val vimeo: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("vimeo", List())
  val vine: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("vine", List())
  val youTube: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("youTube", List())
  val unknownElement: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("unknownElement", List())


  // create element summaries for the printing
  val audioBoomSummary = summariseElement("audioBoom", pagesWithAudioBoomEmbed, audioBoom)
  val brightcoveSummary = summariseElement("brightcove", pagesWithBrightcoveEmbed, brightcove)
  val cnnSummary = summariseElement("cnn", pagesWithCNNEmbed, cnn)
  val dailymotionSummary = summariseElement("dailymotion", pagesWithDailyMotionEmbed, dailymotion)
  val documentCloudSummary = summariseElement("documentCloud", pagesWithDocumentCloudEmbed, documentCloud)
  val facebookSummary = summariseElement("facebook", pagesWithFacebookEmbed, facebook)
  val formstackSummary = summariseElement("formstack", pagesWithFormStackEmbed, formstack)
  val gifSummary = summariseElement("gif", pagesWithGifEmbed, gif)
  val googlemapsSummary = summariseElement("googlemaps", pagesWithGoogleMapsEmbed, googlemaps)
  val guardianAudioSummary = summariseElement("guardianAudio", pagesWithGuardianAudio, guardianAudio)
  val guardianCommentsSummary = summariseElement("guardianComments", pagesWithGuardianCommentsEmbed, guardianComments)
  val guardianVideosSummary = summariseElement("guardianVideos", pagesWithGuardianVideos, guardianVideos)
  val guardianImagesSummary = summariseElement("guardianImages", pagesWithGuardianImages, guardianImages)
  val guardianUploadSummary = summariseElement("guardianUpload", pagesWithGuardianUpload, guardianUpload)
  val guardianWitnessImageSummary = summariseElement("guardianWitnessImage", pagesWithGuardianWitnessImageEmbed, guardianWitnessImage)
  val guardianWitnessVideoSummary = summariseElement("guardianWitnessVideo", pagesWithGuardianWitnessVideoEmbed, guardianWitnessVideo)
  val huluSummary = summariseElement("hulu", pagesWithHuluEmbed, hulu)
  val imageEmbedSummary = summariseElement("image Embed", pagesWithImageEmbed, imageEmbed)
  val infostradaSummary = summariseElement("infostrada", pagesWithInfoStradaEmbed, infostrada)
  val instagramSummary = summariseElement("instagram", pagesWithInstagramEmbed, instagram)
  val interactiveSummary = summariseElement("interactive", pagesWithInteractiveEmbed, interactive)
  val mP3Summary = summariseElement("Audio Embed", pagesWithMP3Embed, mP3)
  val mP4Summary = summariseElement("Video Embed", pagesWithMP4Embed, mP4)
  val parliamentLiveTvSummary = summariseElement("parliamentLiveTv", pagesWithParliamentLiveTvEmbed, parliamentLiveTv)
  val scribdSummary = summariseElement("scribd", pagesWithScribdEmbed, scribd)
  val soundCloudSummary = summariseElement("soundCloud", pagesWithSoundCloudEmbed, soundCloud)
  val spotifySummary = summariseElement("spotify", pagesWithSpotifyEmbed, spotify)
  val twitterSummary = summariseElement("twitter", pagesWithTwitterEmbed, twitter)
  val uStreamSummary = summariseElement("uStream", pagesWithUStreamEmbed, uStream)
  val vevoSummary = summariseElement("vevo", pagesWithVevoEmbed, vevo)
  val vimeoSummary = summariseElement("vimeo", pagesWithVimeoEmbed, vimeo)
  val vineSummary = summariseElement("vine", pagesWithVineEmbed, vine)
  val youTubeSummary = summariseElement("youTube", pagesWithYouTubeEmbed, youTube)
  val unknownEmbedSummary = summariseElement("unknownElement", pagesWithUnknownEmbed, unknownElement)

  val summaryList: List[ElementSummaryData] = List(
        audioBoomSummary,
        brightcoveSummary,
        cnnSummary,
        dailymotionSummary,
        documentCloudSummary,
        facebookSummary,
        formstackSummary,
        gifSummary,
        googlemapsSummary,
        guardianAudioSummary,
        guardianCommentsSummary,
        guardianVideosSummary,
        guardianImagesSummary,
        guardianUploadSummary,
        guardianWitnessImageSummary,
        guardianWitnessVideoSummary,
        huluSummary,
        imageEmbedSummary,
        infostradaSummary,
        instagramSummary,
        interactiveSummary,
        mP3Summary,
        mP4Summary,
        parliamentLiveTvSummary,
        scribdSummary,
        soundCloudSummary,
        spotifySummary,
        twitterSummary,
        uStreamSummary,
        vevoSummary,
        vimeoSummary,
        vineSummary,
        youTubeSummary,
        unknownEmbedSummary
  )

  val sortedSummaryList = summaryList.sortWith(_.percentageOfPageWeightAlerts > _.percentageOfPageWeightAlerts)


  def sortPageElementByWeight(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] = {
    elementList.sortWith(_.bytesDownloaded > _.bytesDownloaded)
  }

  def sortPageElementBySpeed(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] = {
    elementList.sortWith(_.contentDownload > _.contentDownload)
  }

  def sortPageElementByType(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] = {
    elementList.sortWith(_.identifyPageElementType() > _.identifyPageElementType())
  }

  def pageContainsElementType(testResult: PerformanceResultsObject, typeName: String): Boolean = {
    val checkEditorialElementList: List[PageElementFromHTMLTableRow] = for (element <- testResult.editorialElementList if element.identifyPageElementType().contains(typeName)) yield element
    val checkFullElementList: List[PageElementFromHTMLTableRow] = for (element <- testResult.fullElementList if element.identifyPageElementType().contains(typeName)) yield element
    //    val checkFullElementList = result.fullElementList.map(_.identifyPageElementType().contains(typeName))
    val result = checkEditorialElementList.nonEmpty || checkFullElementList.nonEmpty
    result
  }



  def summaryDataToString(): String = {
    val runString: String = "Job Summary: \n" + "\n" +
      "jobStarted at: " + jobStartedTime.toDateTime + "\n" +
      "jobFinished at: " + jobFinishTime.toDateTime + "\n" +
      "Duration of Run: " + durationOfRunMin + " minutes." + "\n" +
      "Number of pages from CAPI queries: " + numberOfPagesFromCAPI + "\n" +
      "Number of pages retested from previous run: " + numberOfPagesRetestedFromLastRun + "\n" +
      "Number of pages tested: " + numberOfPagesSentToWPT + "\n" +
      "Number of failed tests: " + numberOfFailedTests + "\n" +
      "**** \n\n" + "\n"
    val elementString: String = summaryList.map(elementData => returnElementSummaryAsString(elementData)).mkString
    runString + elementString
  }

  def printSummaryDataToScreen(): Unit = {
    println("\n\n\n\n ****************** SUMMARY DATA ********************\n")
    println(summaryDataToString())
    println("\n\n")
    println("\n ****************************************************\n\n\n\n")
  }

  def summaryDataToHTMLString(): String = {
    val runString: String = "<div>\n" +
      "<h3>Job Summary:" + "</h3>" + "\n" +
      "<p style = \"margin-left: 40px\">jobStarted at:      " + jobStartedTime.toDateTime + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">jobFinished at:     " + jobFinishTime.toDateTime + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Duration of Run:    " + durationOfRunMin + " minutes." + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Number of pages from CAPI queries: " + numberOfPagesFromCAPI + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Number of pages retested from previous run: " + numberOfPagesRetestedFromLastRun + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Number of pages tested: " + numberOfPagesSentToWPT + "</p>" +  "\n" +
      "<p style = \"margin-left: 40px\">Number of failed tests: " + numberOfFailedTests + "</p>" +  "\n" +
      "</div>" + "\n"
    val elementString: String = sortedSummaryList.map(elementData => returnElementSummaryAsHTMLString(elementData)).mkString
    runString + elementString
  }

  def summariseElement(elementName: String, pagesWithEmbed: List[PerformanceResultsObject], listOfEmbeds: List[PageElementFromHTMLTableRow]): ElementSummaryData = {
    val title = elementName + ":\n"
    val numberOfPagesWithEmbed = pagesWithEmbed.length
    val numberOfPageWeightAlerts = pagesWithEmbed.count(_.alertStatusPageWeight)
    val numberOfPageSpeedAlerts = pagesWithEmbed.count(_.alertStatusPageSpeed)
    val percentageOfPageWeightAlerts = {
      if (numberOfPageWeightAlerts > 0) {
        ((numberOfPageWeightAlerts.toDouble / numberOfPagesWithEmbed) * 100).toInt
      } else {
        0
      }
    }
    val percentageOfPageSpeedAlerts = {
      if (numberOfPageSpeedAlerts > 0) {
        ((numberOfPageSpeedAlerts.toDouble / numberOfPagesWithEmbed) * 100).toInt
      } else {
        0
      }
    }
    val averageSizeOfEmbedsKb = roundAt(2)(listOfEmbeds.map(_.bytesDownloaded).sum.toDouble / (listOfEmbeds.length * 1024))
    val averageTimeFirstPaintMs = (pagesWithEmbed.map(_.timeFirstPaintInMs).sum.toDouble / pagesWithEmbed.length).toInt
    val averageSpeedIndexMs = (pagesWithEmbed.map(_.speedIndex).sum.toDouble / pagesWithEmbed.length).toInt

    new ElementSummaryData(title,
      numberOfPagesWithEmbed,
      numberOfPageWeightAlerts,
      numberOfPageSpeedAlerts,
      percentageOfPageWeightAlerts,
      percentageOfPageSpeedAlerts,
      averageSizeOfEmbedsKb,
      averageTimeFirstPaintMs,
      averageSpeedIndexMs)
  }

  def returnElementSummaryAsString(elementSummary: ElementSummaryData): String = {
    elementSummary.title + "\n" +
      "Number of Pages with this embed-type: " + elementSummary.numberOfPagesWithEmbed + "\n" +
      "Number of Pages with  this embed-type that alerted for pageWeight: " + elementSummary.numberOfPageWeightAlerts + "\n" +
      "Average Size of this embed-type: " + elementSummary.averageSizeOfEmbeds + " KB \n" +
      "Number of Pages with this embed-type that alerted for pageSpeed: " + elementSummary.numberOfPageSpeedAlerts + "\n" +
      "Average Time to First Paint of a page with this embed-type: " + elementSummary.averageTimeFirstPaint + " ms \n" +
      "Average SpeedIndex of a page with this embed-type: " + elementSummary.averageSpeedIndexMs + " ms \n" +
      "Chance of a page with this embed-type triggering a pageWeight alert: " + elementSummary.percentageOfPageWeightAlerts + "% \n" +
      "Chance of a page with this embed-type triggering a pageSpeed alert: " + elementSummary.percentageOfPageSpeedAlerts + " % \n \n"
  }


  def returnElementSummaryAsHTMLString(elementSummary: ElementSummaryData): String = {
    "<div>\n" +
    "<h3> " + elementSummary.title + " </h3>\n" +
      "<p style = \"margin-left: 40px\"> Number of Pages with this embed-type: " + elementSummary.numberOfPagesWithEmbed + " </p>\n" +
      "<p style = \"margin-left: 40px\"> Number of Pages with  this embed-type that alerted for pageWeight: " + elementSummary.numberOfPageWeightAlerts + " </p>\n" +
      "<p style = \"margin-left: 40px\"> Average Size of this embed-type: " + elementSummary.averageSizeOfEmbeds + " KB </p>\n" +
      "<p style = \"margin-left: 40px\"> Number of Pages with this embed-type that alerted for pageSpeed: " + elementSummary.numberOfPageSpeedAlerts + " </p>\n" +
      "<p style = \"margin-left: 40px\"> Average Time to First Paint of a page with this embed-type: " + elementSummary.averageTimeFirstPaint + " ms </p>\n" +
      "<p style = \"margin-left: 40px\"> Average SpeedIndex of a page with this embed-type: " + elementSummary.averageSpeedIndexMs + " ms </p>\n" +
      "<p style = \"margin-left: 40px\"> Chance of a page with this embed-type triggering a pageWeight alert: " + elementSummary.percentageOfPageWeightAlerts + "% </p>\n" +
      "<p style = \"margin-left: 40px\"> Chance of a page with this embed-type triggering a pageSpeed alert: " + elementSummary.percentageOfPageSpeedAlerts + " % </p>\n" +
      "<p>  </p>" +
      "</div> \n"
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

}