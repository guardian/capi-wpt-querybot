package app.api

import app.apiutils.{PageElementFromHTMLTableRow, ResultsFromPreviousTests, PerformanceResultsObject}
import org.joda.time.DateTime


/**
 * Created by mmcnamara on 28/06/16.
 */
class DataSummary(jobStarted: DateTime, jobFinished: DateTime, numberOfPagesTested: Int, latestResults: List[PerformanceResultsObject], previousResultsObject: ResultsFromPreviousTests) {
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
  val timeNow = DateTime.now
  val today = timeNow.getDayOfYear
  val yesterday = timeNow.minusDays(1).getDayOfYear

  val jobStartedTime: Int
  val jobFinishTime: Int
  val durationOfRunMs = timeNow.getMillis - jobStarted.getMillis
  val durationOfRunS = durationOfRunMs.toDouble/1000
  val durationOfRunMin = durationOfRunS/60

  //last run

  - job finished at
  - time taken from start to finish
    - number of pages pulled from capi
    - number of pages sent from previous runs
  - number of wpt jobs sent
  - number of pageWeight alerts raised on non-interactive content
    - number of pageSpeed alerts raised on non-interactive content
    - number of page speed alerts raised where content was underweight
    - number of interactives tested
    - number of alerts raised on interactives
    - number of failed tests



  val previousResultsHandler = previousResultsObject

  val resultsFromRun: List[PerformanceResultsObject] = latestResults
  val previousResults: List[PerformanceResultsObject] = previousResultsHandler.fullResultsList
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

  val pageWeightAlertsThatHaveBeenResolved = for (result <- hasPreviouslyAlertedOnWeight if  !activePageWeightAlerts.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl,result.typeOfTest))) yield result
  val pageSpeedAlertsThatHaveBeenResolved = for (result <- hasPreviouslyAlertedOnSpeed if !activePageSpeedAlerts.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl,result.typeOfTest))) yield result
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
  val pagesWithFormStackEmbed = allResults.filter(pageContainsElementType(_, "formstack"))
  val pagesWithGoogleMapsEmbed = allResults.filter(pageContainsElementType(_, "googlemaps"))
  val pagesWithGuardianCommentsEmbed = allResults.filter(pageContainsElementType(_, "guardianComments"))
  val pagesWithGuardianVideos = allResults.filter(pageContainsElementType(_, "guardianVideos"))
  val pagesWithGuardianImages = allResults.filter(pageContainsElementType(_, "guardianImages"))
  val pagesWithGuardianWitnessImageEmbed = allResults.filter(pageContainsElementType(_, "guardianWitnessImage"))
  val pagesWithGuardianWitnessVideoEmbed = allResults.filter(pageContainsElementType(_, "guardianWitnessVideo"))
  val pagesWithHuluEmbed = allResults.filter(pageContainsElementType(_, "hulu"))
  val pagesWithInfoStradaEmbed = allResults.filter(pageContainsElementType(_, "infostrada"))
  val pagesWithScribdEmbed = allResults.filter(pageContainsElementType(_, "scribd"))
  val pagesWithSoundCloudEmbed = allResults.filter(pageContainsElementType(_, "soundcloud"))
  val pagesWithSpotifyEmbed = allResults.filter(pageContainsElementType(_, "spotify"))
  val pagesWithTwitterEmbed = allResults.filter(pageContainsElementType(_, "twitter"))
  val pagesWithVimeoEmbed = allResults.filter(pageContainsElementType(_, "vimeo"))
  val pagesWithYouTubeEmbed = allResults.filter(pageContainsElementType(_, "youTube"))
  val pagesWithParliamentLiveTvEmbed = allResults.filter(pageContainsElementType(_, "parliamentLiveTv"))
  val pagesWithFacebookEmbed = allResults.filter(pageContainsElementType(_, "facebook"))
  val pagesWithInstagramEmbed = allResults.filter(pageContainsElementType(_, "instagram"))
  val pagesWithUStreamEmbed = allResults.filter(pageContainsElementType(_, "uStream"))
  val pagesWithDocumentCloudEmbed = allResults.filter(pageContainsElementType(_, "documentCloud"))
  val pagesWithUnknownAudioEmbed = allResults.filter(pageContainsElementType(_, "Audio Embed"))
  val pagesWithUnknownVideoEmbed = allResults.filter(pageContainsElementType(_, "Video Embed"))

  val fullListOfEditorialElements = allResults.flatMap(result => result.editorialElementList)
  val mapOfElementsByType = fullListOfEditorialElements.groupBy(_.identifyPageElementType())

  val audioBoom: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("audioBoom", List())
  val brightcove: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("brightcove", List())
  val cnn: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("cnn", List())
  val dailymotion: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("dailymotion", List())
  val formstack: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("formstack", List())
  val googlemaps: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("googlemaps", List())
  val guardianComments: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianComments", List())
  val guardianVideos: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianVideos", List())
  val guardianImages: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianImages", List())
  val guardianWitnessImage: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianWitnessImage", List())
  val guardianWitnessVideo: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("guardianWitnessVideo", List())
  val hulu: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("hulu", List())
  val infostrada: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("infostrada", List())
  val scribd: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("scribd", List())
  val soundCloud: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("soundCloud", List())
  val spotify: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("spotify", List())
  val twitter: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("twitter", List())
  val vimeo: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("vimeo", List())
  val vine: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("vine", List())
  val youTube: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("youTube", List())
  val parliamentLiveTv: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("parliamentLiveTv", List())
  val facebook: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("facebook", List())
  val instagram: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("instagram", List())
  val uStream: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("uStream", List())
  val documentCloud: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("documentCloud", List())
  val otherAudio: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("Audio Embed", List())
  val otherVideo: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("Video Embed", List())
  val unknownElement: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("unknownElement", List())




  def sortPageElementByWeight(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] ={
    elementList.sortWith(_.bytesDownloaded > _.bytesDownloaded)
  }

  def sortPageElementBySpeed(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] ={
    elementList.sortWith(_.contentDownload > _.contentDownload)
  }

  def sortPageElementByType(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] = {
    elementList.sortWith(_.identifyPageElementType() > _.identifyPageElementType())
  }

  def pageContainsElementType(result: PerformanceResultsObject, typeName: String): Boolean = {
    val checkEditorialElementsList = result.editorialElementList.map(_.identifyPageElementType().contains(typeName))
    val checkFullElementList = result.fullElementList.map(_.identifyPageElementType().contains(typeName))
    checkEditorialElementsList.contains(true) || checkFullElementList.contains(true)
  }


  



}
