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
  val timeNow = DateTime.now
  val today = timeNow.getDayOfYear
  val yesterday = timeNow.minusDays(1).getDayOfYear

  val jobStartedTime = jobStarted
  val jobFinishTime = jobFinished
  val durationOfRunMs = jobFinished.getMillis - jobStarted.getMillis
  val durationOfRunS = durationOfRunMs.toDouble/1000
  val durationOfRunMin = durationOfRunS/60

  //last run - number of pages pulled from capi
  val numberOfPagesFromCAPI: Int = numberOfPagesFromCapi
  val numberOfPagesSentToWPT: Int = numberOfPagesTested
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


 def printSummaryDataToScreen(): Unit ={
   println("\n\n\n\n ****************** SUMMARY DATA ********************\n")
   println("Job Summary: \n")
   println("jobStarted at: " + jobStartedTime.toDateTime)
   println("jobFinished at: " + jobFinishTime.toDateTime)
   println("Duration of Run: " + durationOfRunMin + " minutes." )
   println("Number of pages from CAPI queries: " + numberOfPagesFromCAPI)
   println("Number of pages retested from previous run: " + numberOfPagesRetestedFromLastRun)
   println("Number of pages tested: " + numberOfPagesSentToWPT)
   println("**** \n\n")
   println("*************Element Summary:****************************\n")
   println("AudioBoom: \n")
   println("Number of Pages with Audioboom embed: " + pagesWithAudioBoomEmbed.length )
   println("Number of Pages with Audioboom embed that alerted for pageWeight: " + pagesWithAudioBoomEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with Audioboom embed that alerted for pageSpeed: " + pagesWithAudioBoomEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of AudioBoom embed: " + audioBoom.map(_.bytesDownloaded).sum.toDouble/(audioBoom.length * 1024) + " kB")
   println("\n\n")
   println("Brightcove: \n")
   println("Number of Pages with Brightcove embed: " + pagesWithBrightcoveEmbed.length )
   println("Number of Pages with Brightcove embed that alerted for pageWeight: " + pagesWithBrightcoveEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with Brightcove embed that alerted for pageSpeed: " + pagesWithBrightcoveEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of Brightcove embed: " + brightcove.map(_.bytesDownloaded).sum.toDouble/(brightcove.length * 1024) + " kB")
   println("\n\n")
   println("CNN: \n")
   println("Number of Pages with CNN embed: " + pagesWithCNNEmbed.length )
   println("Number of Pages with CNN embed that alerted for pageWeight: " + pagesWithCNNEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with CNN embed that alerted for pageSpeed: " + pagesWithCNNEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of CNN embed: " + cnn.map(_.bytesDownloaded).sum.toDouble/(cnn.length * 1024) + " kB")
   println("\n\n")
   println("DailyMotion: \n")
   println("Number of Pages with DailyMotion embed: " + pagesWithDailyMotionEmbed.length )
   println("Number of Pages with DailyMotion embed that alerted for pageWeight: " + pagesWithDailyMotionEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with DailyMotion embed that alerted for pageSpeed: " + pagesWithDailyMotionEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of DailyMotion embed: " + dailymotion.map(_.bytesDownloaded).sum.toDouble/(dailymotion.length * 1024) + " kB")
   println("\n\n")
   println("FormStack: \n")
   println("Number of Pages with FormStack embed: " + pagesWithFormStackEmbed.length )
   println("Number of Pages with FormStack embed that alerted for pageWeight: " + pagesWithFormStackEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with FormStack embed that alerted for pageSpeed: " + pagesWithFormStackEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of FormStack embed: " + formstack.map(_.bytesDownloaded).sum.toDouble/(formstack.length * 1024) + " kB")
   println("\n\n")
   println("GoogleMaps: \n")
   println("Number of Pages with GoogleMaps embed: " + pagesWithGoogleMapsEmbed.length )
   println("Number of Pages with GoogleMaps embed that alerted for pageWeight: " + pagesWithGoogleMapsEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with GoogleMaps embed that alerted for pageSpeed: " + pagesWithGoogleMapsEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of GoogleMaps embed: " + googlemaps.map(_.bytesDownloaded).sum.toDouble/(googlemaps.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Comments: \n")
   println("Number of Pages with Guardian Comments embed: " + pagesWithGuardianCommentsEmbed.length )
   println("Number of Pages with Guardian Comments embed that alerted for pageWeight: " + pagesWithGuardianCommentsEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Comments embed that alerted for pageSpeed: " + pagesWithGuardianCommentsEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of Guardian Comments embed: " + guardianComments.map(_.bytesDownloaded).sum.toDouble/(guardianComments.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Videos: \n")
   println("Number of Pages with Guardian Videos embed: " + pagesWithGuardianVideos.length )
   println("Number of Pages with Guardian Videos embed that alerted for pageWeight: " + pagesWithGuardianVideos.filter(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Videos embed that alerted for pageSpeed: " + pagesWithGuardianVideos.filter(_.alertStatusPageSpeed))
   println("Average size of Guardian Videos embed: " + guardianVideos.map(_.bytesDownloaded).sum.toDouble/(guardianVideos.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Images: \n")
   println("Number of Pages with Guardian Images embed: " + pagesWithGuardianImages.length )
   println("Number of Pages with Guardian Images embed that alerted for pageWeight: " + pagesWithGuardianImages.filter(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Images embed that alerted for pageSpeed: " + pagesWithGuardianImages.filter(_.alertStatusPageSpeed))
   println("Average size of Guardian Images embed: " + guardianImages.map(_.bytesDownloaded).sum.toDouble/(guardianImages.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Witness Image: \n")
   println("Number of Pages with Guardian Witness Image embed: " + pagesWithGuardianWitnessImageEmbed.length )
   println("Number of Pages with Guardian Witness Image embed that alerted for pageWeight: " + pagesWithGuardianWitnessImageEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Witness Image embed that alerted for pageSpeed: " + pagesWithGuardianWitnessImageEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of Guardian Witness Image embed: " + guardianWitnessImage.map(_.bytesDownloaded).sum.toDouble/(guardianWitnessImage.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Witness Video: \n")
   println("Number of Pages with Guardian Witness Video embed: " + pagesWithGuardianWitnessVideoEmbed.length )
   println("Number of Pages with Guardian Witness Video embed that alerted for pageWeight: " + pagesWithGuardianWitnessVideoEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Witness Video embed that alerted for pageSpeed: " + pagesWithGuardianWitnessVideoEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of Guardian Witness Video embed: " + guardianWitnessVideo.map(_.bytesDownloaded).sum.toDouble/(guardianWitnessVideo.length * 1024) + " kB")
   println("\n\n")
   println("Hulu: \n")
   println("Number of Pages with Hulu embed: " + pagesWithHuluEmbed.length )
   println("Number of Pages with Hulu embed that alerted for pageWeight: " + pagesWithHuluEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with Hulu embed that alerted for pageSpeed: " + pagesWithHuluEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of Hulu embed: " + hulu.map(_.bytesDownloaded).sum.toDouble/(hulu.length * 1024) + " kB")
   println("\n\n")
   println("InfoStrada: \n")
   println("Number of Pages with InfoStrada embed: " + pagesWithInfoStradaEmbed.length )
   println("Number of Pages with InfoStrada embed that alerted for pageWeight: " + pagesWithInfoStradaEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with InfoStrada embed that alerted for pageSpeed: " + pagesWithInfoStradaEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of InfoStrada embed: " + infostrada.map(_.bytesDownloaded).sum.toDouble/(infostrada.length * 1024) + " kB")
   println("\n\n")
   println("Scribd: \n")
   println("Number of Pages with Scribd embed: " + pagesWithScribdEmbed.length )
   println("Number of Pages with Scribd embed that alerted for pageWeight: " + pagesWithScribdEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with Scribd embed that alerted for pageSpeed: " + pagesWithScribdEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of Scribd embed: " + scribd.map(_.bytesDownloaded).sum.toDouble/(scribd.length * 1024) + " kB")
   println("\n\n")
   println("SoundCloud: \n")
   println("Number of Pages with SoundCloud embed: " + pagesWithSoundCloudEmbed.length )
   println("Number of Pages with SoundCloud embed that alerted for pageWeight: " + pagesWithSoundCloudEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with SoundCloud embed that alerted for pageSpeed: " + pagesWithSoundCloudEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of SoundCloud embed: " + soundCloud.map(_.bytesDownloaded).sum.toDouble/(soundCloud.length * 1024) + " kB")
   println("\n\n")
   println("Spotify: \n")
   println("Number of Pages with Spotify embed: " + pagesWithSpotifyEmbed.length )
   println("Number of Pages with Spotify embed that alerted for pageWeight: " + pagesWithSpotifyEmbed.filter(_.alertStatusPageWeight))
   println("Number of Pages with Spotify embed that alerted for pageSpeed: " + pagesWithSpotifyEmbed.filter(_.alertStatusPageSpeed))
   println("Average size of Spotify embed: " + spotify.map(_.bytesDownloaded).sum.toDouble/(spotify.length * 1024) + " kB")
   println("\n\n")

   println("\n ****************************************************\n\n\n\n")
 }



}
