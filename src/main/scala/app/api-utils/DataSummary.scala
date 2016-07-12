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
  val today = timeNow.getDayOfYear
  val yesterday = timeNow.minusDays(1).getDayOfYear

  val durationOfRunMs = timeNow.getMillis - jobStarted.getMillis
  val durationOfRunS = durationOfRunMs.toDouble/1000
  val durationOfRunMin = durationOfRunS/60

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
    val audioBoomFurniture = List("audioboom")
    val audioBoomAudioFile = List("audio_clip_id")
    val brightcove = List("bcsecure", "player.h-cdn.com/loader.js")
    val cnn = List("cnn")
    val dailymotion = List("dailymotion")
    val formstack = List("formstack")
    val googlemaps = List("maps.google.com")
    val guardianComments = List("comment-permalink","profile.theguardian.com", "avatar.guim.co.uk/user")
    val guardianVideos = List("cdn.theguardian.tv")
    val guardianImages = List("i.guim.co.uk/img/media/")
    val guardianWitnessText = "" - cant see anything
    val guardianWitnessImage = List("n0tice-static.s3.amazonaws.com/image/")
    val guardianWitnessVideo = List("https://n0tice-static.s3.amazonaws.com/video/thumbnails", "googlevideo.com")
    val hulu = need a sample page - cant find a working embed""
    val infostrada = need a sample page ""
    val scribd = need a sample page""
    val soundCloud = ""
    val spotifySong = ""
    val spotifyAlbum = ""
    val spotifyPlaylists = ""
    val twitter = List("twitter")
    val vimeo = ""
    val vine = ""
    val youTube = List("ytimg")
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
