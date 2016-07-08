package app.api

import app.apiutils.{PageElementFromHTMLTableRow, ResultsFromPreviousTests, PerformanceResultsObject}
import org.joda.time.DateTime


/**
 * Created by mmcnamara on 28/06/16.
 */
class DataSummary(jobStarted: DateTime,numberOfPagesTested: Int, latestResults: List[PerformanceResultsObject], previousResultsObject: ResultsFromPreviousTests) {
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
  val timeNow = DateTime.now
  val today = timeNow.getDayOfMonth

  val durationOfRunMs = timeNow.getMillis - jobStarted.getMillis
  val durationOfRunS = durationOfRunMs.toDouble/1000
  val durationOfRunMin = durationOfRunS/60

  val previousResultsHandler = previousResultsObject

  val resultsFromRun: List[PerformanceResultsObject] = latestResults
  val previousResults: List[PerformanceResultsObject] = previousResultsHandler.oldResults
  val allResults: List[PerformanceResultsObject] = resultsFromRun ::: previousResults
  val hasPreviouslyAlertedOnWeight: List[PerformanceResultsObject] = previousResultsHandler.hasPreviouslyAlerted.filter(_.alertStatusPageWeight)
  val hasPreviouslyAlertedOnSpeed: List[PerformanceResultsObject] = previousResultsHandler.hasPreviouslyAlerted.filter(_.alertStatusPageSpeed)

  val todaysResults = for (result <- allResults if DateTime.parse(result.timeOfTest).getDayOfMonth == today) yield result
  val yesterdaysResults = for (result <- allResults if DateTime.parse(result.timeOfTest).getDayOfMonth != today) yield result

  val activePageWeightAlerts = resultsFromRun.filter(_.alertStatusPageWeight)
  val activePageSpeedAlerts = resultsFromRun.filter(_.alertStatusPageSpeed)
  val activeSlowButUnderWeight = activePageSpeedAlerts.filter(!_.alertStatusPageWeight)

  val newPageWeightAlerts = for (result <- activePageWeightAlerts if !hasPreviouslyAlertedOnWeight.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result
  val newPageSpeedAlerts = for (result <- activePageSpeedAlerts if !hasPreviouslyAlertedOnSpeed.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result

  val pageWeightAlertsThatHaveBeenResolved = for (result <- hasPreviouslyAlertedOnWeight if  !activePageWeightAlerts.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl,result.typeOfTest))) yield result
  val pageSpeedAlertsThatHaveBeenResolved = for (result <- hasPreviouslyAlertedOnSpeed if !activePageSpeedAlerts.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl,result.typeOfTest))) yield result
  val pageSpeedAlertsResolvedForUnderweightPages = pageSpeedAlertsThatHaveBeenResolved.filter(!_.alertStatusPageWeight)

  val numberOfPageWeightAlertsResolvedLast24Hrs = pageWeightAlertsThatHaveBeenResolved.length
  val numberOfPageSpeedAlertsResolvedLast24Hrs = pageSpeedAlertsThatHaveBeenResolved.length

  val fulllistOfElementsFromPageWeightAlertPages: List[PageElementFromHTMLTableRow] = sortPageElementByWeight((newPageWeightAlerts ::: hasPreviouslyAlertedOnWeight).flatMap(_.fullElementList))
  val fulllistOfElementsFromPageSpeedAlertPages: List[PageElementFromHTMLTableRow] = sortPageElementBySpeed((newPageSpeedAlerts ::: hasPreviouslyAlertedOnSpeed).flatMap(_.fullElementList))

  val sortedlistOfElementsFromPageWeightAlertPages: List[PageElementFromHTMLTableRow] = sortPageElementByWeight((newPageWeightAlerts ::: hasPreviouslyAlertedOnWeight).flatMap(result => result.trimToEditorialElements(result.fullElementList)))
  val sortedlistOfElementsFromPageSpeedAlertPages: List[PageElementFromHTMLTableRow] = sortPageElementBySpeed((newPageSpeedAlerts ::: hasPreviouslyAlertedOnSpeed).flatMap(result => result.trimToEditorialElements(result.fullElementList)))

  def sortPageElementByWeight(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] ={
    elementList.sortWith(_.bytesDownloaded > _.bytesDownloaded)
  }

  def sortPageElementBySpeed(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] ={
    elementList.sortWith(_.contentDownload > _.contentDownload)
  }

  def sortPageElementByType(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] = {
    elementList
  }

 /* def identifyPageElementType(element: PageElementFromHTMLTableRow): String = {
    val audioBoomCss = List("audioboom_core", "audioboom_styles")
    val audioBoomImages = List("audioboom_color")
    val audioBoomAudioFile = List("audio_clip_id")
    val brightcove = List("bcsecure", "player.h-cdn.com/loader.js")
    val cnn = List("cnn")
    val dailymotion = List("dailymotion")
    val formstack = ""
    val googlemaps = ""
    val guardianComments = ""
    val guardianVideos = ""
    val guardianWitnessText = ""
    val guardianWitnessImage = ""
    val guardianWitnessVideo = ""
    val hulu = ""
    val infostrada = ""
    val scribd = ""
    val soundCloud = ""
    val spotifySong = ""
    val spotifyAlbum = ""
    val spotifyPlaylists = ""
    val twitter = ""
    val vimeo = ""
    val vine = ""
    val youTube = ""
    val parliamentliveTv = ""
    val facebook = ""
    val instagram = ""
    val uStream = ""
    val documentCloud = ""
    val audio = List(".mp3")
    val otherVideo = List(".mp4")
    match(element.resource){
      case
    }

  }*/



}
