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




  def sortPageElementByWeight(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] ={
    elementList.sortWith(_.bytesDownloaded > _.bytesDownloaded)
  }

  def sortPageElementBySpeed(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] ={
    elementList.sortWith(_.contentDownload > _.contentDownload)
  }

  def sortPageElementByType(elementList: List[PageElementFromHTMLTableRow]): List[PageElementFromHTMLTableRow] = {
    elementList.sortWith(_.identifyPageElementType() > _.identifyPageElementType())
  }

  def pageContainsElementType(testResult: PerformanceResultsObject, typeName: String): Boolean = {
    val checkEditorialElementList: List[PageElementFromHTMLTableRow] = for (element <- testResult.editorialElementList if element.identifyPageElementType().contains(typeName)) yield element
    val checkFullElementList: List[PageElementFromHTMLTableRow] =  for (element <- testResult.fullElementList if element.identifyPageElementType().contains(typeName)) yield element
    //    val checkFullElementList = result.fullElementList.map(_.identifyPageElementType().contains(typeName))
    val result = checkEditorialElementList.nonEmpty || checkFullElementList.nonEmpty
    result
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
   println("numberOfPagesExamined: " + allResults.length )
   println("AudioBoom: \n")
   println("Number of Pages with Audioboom embed: " + pagesWithAudioBoomEmbed.length )
   println("Number of Pages with Audioboom embed that alerted for pageWeight: " + pagesWithAudioBoomEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Audioboom embed that alerted for pageSpeed: " + pagesWithAudioBoomEmbed.count(_.alertStatusPageSpeed))
   println("Average size of AudioBoom embed: " + audioBoom.map(_.bytesDownloaded).sum.toDouble/(audioBoom.length * 1024) + " kB")
   println("\n\n")
   println("Brightcove: \n")
   println("Number of Pages with Brightcove embed: " + pagesWithBrightcoveEmbed.length )
   println("Number of Pages with Brightcove embed that alerted for pageWeight: " + pagesWithBrightcoveEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Brightcove embed that alerted for pageSpeed: " + pagesWithBrightcoveEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Brightcove embed: " + brightcove.map(_.bytesDownloaded).sum.toDouble/(brightcove.length * 1024) + " kB")
   println("\n\n")
   println("CNN: \n")
   println("Number of Pages with CNN embed: " + pagesWithCNNEmbed.length )
   println("Number of Pages with CNN embed that alerted for pageWeight: " + pagesWithCNNEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with CNN embed that alerted for pageSpeed: " + pagesWithCNNEmbed.count(_.alertStatusPageSpeed))
   println("Average size of CNN embed: " + cnn.map(_.bytesDownloaded).sum.toDouble/(cnn.length * 1024) + " kB")
   println("\n\n")
   println("DailyMotion: \n")
   println("Number of Pages with DailyMotion embed: " + pagesWithDailyMotionEmbed.length )
   println("Number of Pages with DailyMotion embed that alerted for pageWeight: " + pagesWithDailyMotionEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with DailyMotion embed that alerted for pageSpeed: " + pagesWithDailyMotionEmbed.count(_.alertStatusPageSpeed))
   println("Average size of DailyMotion embed: " + dailymotion.map(_.bytesDownloaded).sum.toDouble/(dailymotion.length * 1024) + " kB")
   println("\n\n")
   println("Document Cloud: \n")
   println("Number of Pages with Document Cloud embed: " + pagesWithDocumentCloudEmbed.length )
   println("Number of Pages with Document Cloud embed that alerted for pageWeight: " + pagesWithDocumentCloudEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Document Cloud embed that alerted for pageSpeed: " + pagesWithDocumentCloudEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Document Cloud embed: " + documentCloud.map(_.bytesDownloaded).sum.toDouble/(documentCloud.length * 1024) + " kB")
   println("\n\n")
   println("Facebook: \n")
   println("Number of Pages with Facebook embed: " + pagesWithFacebookEmbed.length )
   println("Number of Pages with Facebook embed that alerted for pageWeight: " + pagesWithFacebookEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Facebook embed that alerted for pageSpeed: " + pagesWithFacebookEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Facebook embed: " + facebook.map(_.bytesDownloaded).sum.toDouble/(facebook.length * 1024) + " kB")
   println("\n\n")
   println("FormStack: \n")
   println("Number of Pages with FormStack embed: " + pagesWithFormStackEmbed.length )
   println("Number of Pages with FormStack embed that alerted for pageWeight: " + pagesWithFormStackEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with FormStack embed that alerted for pageSpeed: " + pagesWithFormStackEmbed.count(_.alertStatusPageSpeed))
   println("Average size of FormStack embed: " + formstack.map(_.bytesDownloaded).sum.toDouble/(formstack.length * 1024) + " kB")
   println("\n\n")
   println("Gif embed: \n")
   println("Number of Pages with Gif embed: " + pagesWithGifEmbed.length )
   println("Number of Pages with Gif embed that alerted for pageWeight: " + pagesWithGifEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Gif embed that alerted for pageSpeed: " + pagesWithGifEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Gif embed: " + gif.map(_.bytesDownloaded).sum.toDouble/(gif.length * 1024) + " kB")
   println("\n\n")
   println("GoogleMaps: \n")
   println("Number of Pages with GoogleMaps embed: " + pagesWithGoogleMapsEmbed.length )
   println("Number of Pages with GoogleMaps embed that alerted for pageWeight: " + pagesWithGoogleMapsEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with GoogleMaps embed that alerted for pageSpeed: " + pagesWithGoogleMapsEmbed.count(_.alertStatusPageSpeed))
   println("Average size of GoogleMaps embed: " + googlemaps.map(_.bytesDownloaded).sum.toDouble/(googlemaps.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Audio: \n")
   println("Number of Pages with Guardian Audio embed: " + pagesWithGuardianAudio.length )
   println("Number of Pages with Guardian Audio embed that alerted for pageWeight: " + pagesWithGuardianAudio.count(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Audio embed that alerted for pageSpeed: " + pagesWithGuardianAudio.count(_.alertStatusPageSpeed))
   println("Average size of Guardian Audio embed: " + guardianAudio.map(_.bytesDownloaded).sum.toDouble/(guardianAudio.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Comments: \n")
   println("Number of Pages with Guardian Comments embed: " + pagesWithGuardianCommentsEmbed.length )
   println("Number of Pages with Guardian Comments embed that alerted for pageWeight: " + pagesWithGuardianCommentsEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Comments embed that alerted for pageSpeed: " + pagesWithGuardianCommentsEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Guardian Comments embed: " + guardianComments.map(_.bytesDownloaded).sum.toDouble/(guardianComments.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Videos: \n")
   println("Number of Pages with Guardian Videos embed: " + pagesWithGuardianVideos.length )
   println("Number of Pages with Guardian Videos embed that alerted for pageWeight: " + pagesWithGuardianVideos.count(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Videos embed that alerted for pageSpeed: " + pagesWithGuardianVideos.count(_.alertStatusPageSpeed))
   println("Average size of Guardian Videos embed: " + guardianVideos.map(_.bytesDownloaded).sum.toDouble/(guardianVideos.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Images: \n")
   println("Number of Pages with Guardian Images embed: " + pagesWithGuardianImages.length )
   println("Number of Pages with Guardian Images embed that alerted for pageWeight: " + pagesWithGuardianImages.count(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Images embed that alerted for pageSpeed: " + pagesWithGuardianImages.count(_.alertStatusPageSpeed))
   println("Average size of Guardian Images embed: " + guardianImages.map(_.bytesDownloaded).sum.toDouble/(guardianImages.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Upload: \n")
   println("Number of Pages with Guardian Upload embed: " + pagesWithGuardianUpload.length )
   println("Number of Pages with Guardian Upload embed that alerted for pageWeight: " + pagesWithGuardianUpload.count(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Upload embed that alerted for pageSpeed: " + pagesWithGuardianUpload.count(_.alertStatusPageSpeed))
   println("Average size of Guardian Upload embed: " + guardianUpload.map(_.bytesDownloaded).sum.toDouble/(guardianUpload.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Witness Image: \n")
   println("Number of Pages with Guardian Witness Image embed: " + pagesWithGuardianWitnessImageEmbed.length )
   println("Number of Pages with Guardian Witness Image embed that alerted for pageWeight: " + pagesWithGuardianWitnessImageEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Witness Image embed that alerted for pageSpeed: " + pagesWithGuardianWitnessImageEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Guardian Witness Image embed: " + guardianWitnessImage.map(_.bytesDownloaded).sum.toDouble/(guardianWitnessImage.length * 1024) + " kB")
   println("\n\n")
   println("Guardian Witness Video: \n")
   println("Number of Pages with Guardian Witness Video embed: " + pagesWithGuardianWitnessVideoEmbed.length )
   println("Number of Pages with Guardian Witness Video embed that alerted for pageWeight: " + pagesWithGuardianWitnessVideoEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Guardian Witness Video embed that alerted for pageSpeed: " + pagesWithGuardianWitnessVideoEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Guardian Witness Video embed: " + guardianWitnessVideo.map(_.bytesDownloaded).sum.toDouble/(guardianWitnessVideo.length * 1024) + " kB")
   println("\n\n")
   println("Hulu: \n")
   println("Number of Pages with Hulu embed: " + pagesWithHuluEmbed.length )
   println("Number of Pages with Hulu embed that alerted for pageWeight: " + pagesWithHuluEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Hulu embed that alerted for pageSpeed: " + pagesWithHuluEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Hulu embed: " + hulu.map(_.bytesDownloaded).sum.toDouble/(hulu.length * 1024) + " kB")
   println("\n\n")
   println("Image Embed: \n")
   println("Number of Pages with Image Embed embed: " + pagesWithImageEmbed.length )
   println("Number of Pages with Image Embed embed that alerted for pageWeight: " + pagesWithImageEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Image Embed embed that alerted for pageSpeed: " + pagesWithImageEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Image Embed embed: " + imageEmbed.map(_.bytesDownloaded).sum.toDouble/(imageEmbed.length * 1024) + " kB")
   println("\n\n")
   println("Instagram: \n")
   println("Number of Pages with Instagram embed: " + pagesWithInstagramEmbed.length )
   println("Number of Pages with Instagram embed that alerted for pageWeight: " + pagesWithInstagramEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Instagram embed that alerted for pageSpeed: " + pagesWithInstagramEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Instagram embed: " + instagram.map(_.bytesDownloaded).sum.toDouble/(instagram.length * 1024) + " kB")
   println("\n\n")
   println("InfoStrada: \n")
   println("Number of Pages with InfoStrada embed: " + pagesWithInfoStradaEmbed.length )
   println("Number of Pages with InfoStrada embed that alerted for pageWeight: " + pagesWithInfoStradaEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with InfoStrada embed that alerted for pageSpeed: " + pagesWithInfoStradaEmbed.count(_.alertStatusPageSpeed))
   println("Average size of InfoStrada embed: " + infostrada.map(_.bytesDownloaded).sum.toDouble/(infostrada.length * 1024) + " kB")
   println("\n\n")
   println("ParliamentLive TV: \n")
   println("Number of Pages with ParliamentLive TV embed: " + pagesWithParliamentLiveTvEmbed.length )
   println("Number of Pages with ParliamentLive TV embed that alerted for pageWeight: " + pagesWithParliamentLiveTvEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with ParliamentLive TV embed that alerted for pageSpeed: " + pagesWithParliamentLiveTvEmbed.count(_.alertStatusPageSpeed))
   println("Average size of ParliamentLive TV embed: " + parliamentLiveTv.map(_.bytesDownloaded).sum.toDouble/(parliamentLiveTv.length * 1024) + " kB")
   println("\n\n")
   println("Scribd: \n")
   println("Number of Pages with Scribd embed: " + pagesWithScribdEmbed.length )
   println("Number of Pages with Scribd embed that alerted for pageWeight: " + pagesWithScribdEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Scribd embed that alerted for pageSpeed: " + pagesWithScribdEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Scribd embed: " + scribd.map(_.bytesDownloaded).sum.toDouble/(scribd.length * 1024) + " kB")
   println("\n\n")
   println("SoundCloud: \n")
   println("Number of Pages with SoundCloud embed: " + pagesWithSoundCloudEmbed.length )
   println("Number of Pages with SoundCloud embed that alerted for pageWeight: " + pagesWithSoundCloudEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with SoundCloud embed that alerted for pageSpeed: " + pagesWithSoundCloudEmbed.count(_.alertStatusPageSpeed))
   println("Average size of SoundCloud embed: " + soundCloud.map(_.bytesDownloaded).sum.toDouble/(soundCloud.length * 1024) + " kB")
   println("\n\n")
   println("Spotify: \n")
   println("Number of Pages with Spotify embed: " + pagesWithSpotifyEmbed.length )
   println("Number of Pages with Spotify embed that alerted for pageWeight: " + pagesWithSpotifyEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Spotify embed that alerted for pageSpeed: " + pagesWithSpotifyEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Spotify embed: " + spotify.map(_.bytesDownloaded).sum.toDouble/(spotify.length * 1024) + " kB")
   println("\n\n")
   println("Twitter: \n")
   println("Number of Pages with Twitter embed: " + pagesWithTwitterEmbed.length )
   println("Number of Pages with Twitter embed that alerted for pageWeight: " + pagesWithTwitterEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Twitter embed that alerted for pageSpeed: " + pagesWithTwitterEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Twitter embed: " + twitter.map(_.bytesDownloaded).sum.toDouble/(twitter.length * 1024) + " kB")
   println("\n\n")
   println("UStream: \n")
   println("Number of Pages with UStream embed: " + pagesWithUStreamEmbed.length )
   println("Number of Pages with UStream embed that alerted for pageWeight: " + pagesWithUStreamEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with UStream embed that alerted for pageSpeed: " + pagesWithUStreamEmbed.count(_.alertStatusPageSpeed))
   println("Average size of UStream embed: " + uStream.map(_.bytesDownloaded).sum.toDouble/(uStream.length * 1024) + " kB")
   println("\n\n")
   println("Vevo: \n")
   println("Number of Pages with Vevo embed: " + pagesWithVevoEmbed.length )
   println("Number of Pages with Vevo embed that alerted for pageWeight: " + pagesWithVevoEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Vevo embed that alerted for pageSpeed: " + pagesWithVevoEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Vevo embed: " + vevo.map(_.bytesDownloaded).sum.toDouble/(vevo.length * 1024) + " kB")
   println("\n\n")
   println("Vimeo: \n")
   println("Number of Pages with Vimeo embed: " + pagesWithVimeoEmbed.length )
   println("Number of Pages with Vimeo embed that alerted for pageWeight: " + pagesWithVimeoEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Vimeo embed that alerted for pageSpeed: " + pagesWithVimeoEmbed.count(_.alertStatusPageSpeed))
   println("Average size of Vimeo embed: " + vimeo.map(_.bytesDownloaded).sum.toDouble/(vimeo.length * 1024) + " kB")
   println("\n\n")
   println("YouTube: \n")
   println("Number of Pages with YouTube embed: " + pagesWithYouTubeEmbed.length )
   println("Number of Pages with YouTube embed that alerted for pageWeight: " + pagesWithYouTubeEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with Youtube embed that alerted for pageSpeed: " + pagesWithYouTubeEmbed.count(_.alertStatusPageSpeed))
   println("Average size of YouTube embed: " + youTube.map(_.bytesDownloaded).sum.toDouble/(youTube.length * 1024) + " kB")
   println("\n\n")
   println("Audio MP3: \n")
   println("Number of Pages with Audio MP3 embeds: " + pagesWithMP3Embed.length )
   println("Number of Pages with  Audio MP3 embeds that alerted for pageWeight: " + pagesWithMP3Embed.count(_.alertStatusPageWeight))
   println("Number of Pages with Audio MP3 embeds that alerted for pageSpeed: " + pagesWithMP3Embed.count(_.alertStatusPageSpeed))
   println("Average size of Audio MP3 embeds: " + mP3.map(_.bytesDownloaded).sum.toDouble/(mP3.length * 1024) + " kB")
   println("\n\n")
   println("Video MP4: \n")
   println("Number of Pages with Video MP4 embeds: " + pagesWithMP4Embed.length )
   println("Number of Pages with  Video MP4 embeds that alerted for pageWeight: " + pagesWithMP4Embed.count(_.alertStatusPageWeight))
   println("Number of Pages with Video MP4 embeds that alerted for pageSpeed: " + pagesWithMP4Embed.count(_.alertStatusPageSpeed))
   println("Average size of Video MP4 embeds: " + mP4.map(_.bytesDownloaded).sum.toDouble/(mP4.length * 1024) + " kB")
   println("\n\n")
   println("Unknown Embed: \n")
   println("Number of Pages with unknown embeds: " + pagesWithUnknownEmbed.length )
   println("Number of Pages with  unknown embeds that alerted for pageWeight: " + pagesWithUnknownEmbed.count(_.alertStatusPageWeight))
   println("Number of Pages with unknown embeds that alerted for pageSpeed: " + pagesWithUnknownEmbed.count(_.alertStatusPageSpeed))
   println("Average size of unknown embeds: " + unknownElement.map(_.bytesDownloaded).sum.toDouble/(unknownElement.length * 1024) + " kB")
   println("\n\n")
   println("\n ****************************************************\n\n\n\n")
 }

  def summaryDataToString(): String ={
    val runString: String = "\n\n\n\n ****************** SUMMARY DATA ********************\n" + "\n" +
      "Job Summary: \n" + "\n" +
      "jobStarted at: " + jobStartedTime.toDateTime + "\n" +
      "jobFinished at: " + jobFinishTime.toDateTime + "\n" +
      "Duration of Run: " + durationOfRunMin + " minutes."  + "\n" +
      "Number of pages from CAPI queries: " + numberOfPagesFromCAPI + "\n" +
      "Number of pages retested from previous run: " + numberOfPagesRetestedFromLastRun + "\n" +
      "Number of pages tested: " + numberOfPagesSentToWPT + "\n" +
      "**** \n\n" + "\n"
    val elementSummary: String = "*************Element Summary:****************************\n" + "\n" +
      "numberOfPagesExamined: " + allResults.length + "\n"
    val audioBoomString =   "AudioBoom: \n" + "\n" +
      "Number of Pages with Audioboom embed: " + pagesWithAudioBoomEmbed.length + "\n" +
      "Number of Pages with Audioboom embed that alerted for pageWeight: " + pagesWithAudioBoomEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Audioboom embed that alerted for pageSpeed: " + pagesWithAudioBoomEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of AudioBoom embed: " + audioBoom.map(_.bytesDownloaded).sum.toDouble/(audioBoom.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val brightCoveString =  "Brightcove: \n" + "\n" +
      "Number of Pages with Brightcove embed: " + pagesWithBrightcoveEmbed.length  + "\n" +
      "Number of Pages with Brightcove embed that alerted for pageWeight: " + pagesWithBrightcoveEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Brightcove embed that alerted for pageSpeed: " + pagesWithBrightcoveEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Brightcove embed: " + brightcove.map(_.bytesDownloaded).sum.toDouble/(brightcove.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val cnnString =  "CNN: \n" + "\n" +
      "Number of Pages with CNN embed: " + pagesWithCNNEmbed.length  + "\n" +
      "Number of Pages with CNN embed that alerted for pageWeight: " + pagesWithCNNEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with CNN embed that alerted for pageSpeed: " + pagesWithCNNEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of CNN embed: " + cnn.map(_.bytesDownloaded).sum.toDouble/(cnn.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val dailyMotionString =  "DailyMotion: \n" + "\n" +
      "Number of Pages with DailyMotion embed: " + pagesWithDailyMotionEmbed.length  + "\n" +
      "Number of Pages with DailyMotion embed that alerted for pageWeight: " + pagesWithDailyMotionEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with DailyMotion embed that alerted for pageSpeed: " + pagesWithDailyMotionEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of DailyMotion embed: " + dailymotion.map(_.bytesDownloaded).sum.toDouble/(dailymotion.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val documentCloudString =  "Document Cloud: \n" + "\n" +
      "Number of Pages with Document Cloud embed: " + pagesWithDocumentCloudEmbed.length  + "\n" +
      "Number of Pages with Document Cloud embed that alerted for pageWeight: " + pagesWithDocumentCloudEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Document Cloud embed that alerted for pageSpeed: " + pagesWithDocumentCloudEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Document Cloud embed: " + documentCloud.map(_.bytesDownloaded).sum.toDouble/(documentCloud.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val facebookString = "Facebook: \n" + "\n" +
      "Number of Pages with Facebook embed: " + pagesWithFacebookEmbed.length  + "\n" +
      "Number of Pages with Facebook embed that alerted for pageWeight: " + pagesWithFacebookEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Facebook embed that alerted for pageSpeed: " + pagesWithFacebookEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Facebook embed: " + facebook.map(_.bytesDownloaded).sum.toDouble/(facebook.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val formstackString = "FormStack: \n" + "\n" +
      "Number of Pages with FormStack embed: " + pagesWithFormStackEmbed.length  + "\n" +
      "Number of Pages with FormStack embed that alerted for pageWeight: " + pagesWithFormStackEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with FormStack embed that alerted for pageSpeed: " + pagesWithFormStackEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of FormStack embed: " + formstack.map(_.bytesDownloaded).sum.toDouble/(formstack.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val gifEmbedString =  "Gif embed: \n" + "\n" +
      "Number of Pages with Gif embed: " + pagesWithGifEmbed.length  + "\n" +
      "Number of Pages with Gif embed that alerted for pageWeight: " + pagesWithGifEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Gif embed that alerted for pageSpeed: " + pagesWithGifEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Gif embed: " + gif.map(_.bytesDownloaded).sum.toDouble/(gif.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val googleMapsString = "GoogleMaps: \n" + "\n" +
      "Number of Pages with GoogleMaps embed: " + pagesWithGoogleMapsEmbed.length  + "\n" +
      "Number of Pages with GoogleMaps embed that alerted for pageWeight: " + pagesWithGoogleMapsEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with GoogleMaps embed that alerted for pageSpeed: " + pagesWithGoogleMapsEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of GoogleMaps embed: " + googlemaps.map(_.bytesDownloaded).sum.toDouble/(googlemaps.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val guardianAudioString =  "Guardian Audio: \n" + "\n" +
      "Number of Pages with Guardian Audio embed: " + pagesWithGuardianAudio.length  + "\n" +
      "Number of Pages with Guardian Audio embed that alerted for pageWeight: " + pagesWithGuardianAudio.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Guardian Audio embed that alerted for pageSpeed: " + pagesWithGuardianAudio.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Guardian Audio embed: " + guardianAudio.map(_.bytesDownloaded).sum.toDouble/(guardianAudio.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val guardianCommentsString =  "Guardian Comments: \n" + "\n" +
      "Number of Pages with Guardian Comments embed: " + pagesWithGuardianCommentsEmbed.length  + "\n" +
      "Number of Pages with Guardian Comments embed that alerted for pageWeight: " + pagesWithGuardianCommentsEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Guardian Comments embed that alerted for pageSpeed: " + pagesWithGuardianCommentsEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Guardian Comments embed: " + guardianComments.map(_.bytesDownloaded).sum.toDouble/(guardianComments.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val guardianVideosString =  "Guardian Videos: \n" + "\n" +
      "Number of Pages with Guardian Videos embed: " + pagesWithGuardianVideos.length  + "\n" +
      "Number of Pages with Guardian Videos embed that alerted for pageWeight: " + pagesWithGuardianVideos.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Guardian Videos embed that alerted for pageSpeed: " + pagesWithGuardianVideos.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Guardian Videos embed: " + guardianVideos.map(_.bytesDownloaded).sum.toDouble/(guardianVideos.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val guardianImagesString = "Guardian Images: \n" + "\n" +
      "Number of Pages with Guardian Images embed: " + pagesWithGuardianImages.length  + "\n" +
      "Number of Pages with Guardian Images embed that alerted for pageWeight: " + pagesWithGuardianImages.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Guardian Images embed that alerted for pageSpeed: " + pagesWithGuardianImages.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Guardian Images embed: " + guardianImages.map(_.bytesDownloaded).sum.toDouble/(guardianImages.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val guardianUploadString =  "Guardian Upload: \n" + "\n" +
      "Number of Pages with Guardian Upload embed: " + pagesWithGuardianUpload.length  + "\n" +
      "Number of Pages with Guardian Upload embed that alerted for pageWeight: " + pagesWithGuardianUpload.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Guardian Upload embed that alerted for pageSpeed: " + pagesWithGuardianUpload.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Guardian Upload embed: " + guardianUpload.map(_.bytesDownloaded).sum.toDouble/(guardianUpload.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val guardianWitnessImageString =  "Guardian Witness Image: \n" + "\n" +
      "Number of Pages with Guardian Witness Image embed: " + pagesWithGuardianWitnessImageEmbed.length  + "\n" +
      "Number of Pages with Guardian Witness Image embed that alerted for pageWeight: " + pagesWithGuardianWitnessImageEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Guardian Witness Image embed that alerted for pageSpeed: " + pagesWithGuardianWitnessImageEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Guardian Witness Image embed: " + guardianWitnessImage.map(_.bytesDownloaded).sum.toDouble/(guardianWitnessImage.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
     val guardianWitnessVideoString = "Guardian Witness Video: \n" + "\n" +
      "Number of Pages with Guardian Witness Video embed: " + pagesWithGuardianWitnessVideoEmbed.length  + "\n" +
      "Number of Pages with Guardian Witness Video embed that alerted for pageWeight: " + pagesWithGuardianWitnessVideoEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Guardian Witness Video embed that alerted for pageSpeed: " + pagesWithGuardianWitnessVideoEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Guardian Witness Video embed: " + guardianWitnessVideo.map(_.bytesDownloaded).sum.toDouble/(guardianWitnessVideo.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val huluString =  "Hulu: \n" + "\n" +
      "Number of Pages with Hulu embed: " + pagesWithHuluEmbed.length  + "\n" +
      "Number of Pages with Hulu embed that alerted for pageWeight: " + pagesWithHuluEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Hulu embed that alerted for pageSpeed: " + pagesWithHuluEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Hulu embed: " + hulu.map(_.bytesDownloaded).sum.toDouble/(hulu.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val imageEmbedString =  "Image Embed: \n" + "\n" +
      "Number of Pages with Image Embed embed: " + pagesWithImageEmbed.length  + "\n" +
      "Number of Pages with Image Embed embed that alerted for pageWeight: " + pagesWithImageEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Image Embed embed that alerted for pageSpeed: " + pagesWithImageEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Image Embed embed: " + imageEmbed.map(_.bytesDownloaded).sum.toDouble/(imageEmbed.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val instagramString =  "Instagram: \n" + "\n" +
      "Number of Pages with Instagram embed: " + pagesWithInstagramEmbed.length  + "\n" +
      "Number of Pages with Instagram embed that alerted for pageWeight: " + pagesWithInstagramEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Instagram embed that alerted for pageSpeed: " + pagesWithInstagramEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Instagram embed: " + instagram.map(_.bytesDownloaded).sum.toDouble/(instagram.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val infoStradaString =  "InfoStrada: \n" + "\n" +
      "Number of Pages with InfoStrada embed: " + pagesWithInfoStradaEmbed.length  + "\n" +
      "Number of Pages with InfoStrada embed that alerted for pageWeight: " + pagesWithInfoStradaEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with InfoStrada embed that alerted for pageSpeed: " + pagesWithInfoStradaEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of InfoStrada embed: " + infostrada.map(_.bytesDownloaded).sum.toDouble/(infostrada.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val parliamentLiveTVString =  "ParliamentLive TV: \n" + "\n" +
      "Number of Pages with ParliamentLive TV embed: " + pagesWithParliamentLiveTvEmbed.length  + "\n" +
      "Number of Pages with ParliamentLive TV embed that alerted for pageWeight: " + pagesWithParliamentLiveTvEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with ParliamentLive TV embed that alerted for pageSpeed: " + pagesWithParliamentLiveTvEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of ParliamentLive TV embed: " + parliamentLiveTv.map(_.bytesDownloaded).sum.toDouble/(parliamentLiveTv.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val scribdString =  "Scribd: \n" + "\n" +
      "Number of Pages with Scribd embed: " + pagesWithScribdEmbed.length  + "\n" +
      "Number of Pages with Scribd embed that alerted for pageWeight: " + pagesWithScribdEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Scribd embed that alerted for pageSpeed: " + pagesWithScribdEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Scribd embed: " + scribd.map(_.bytesDownloaded).sum.toDouble/(scribd.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val soundCloudString =  "SoundCloud: \n" + "\n" +
      "Number of Pages with SoundCloud embed: " + pagesWithSoundCloudEmbed.length  + "\n" +
      "Number of Pages with SoundCloud embed that alerted for pageWeight: " + pagesWithSoundCloudEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with SoundCloud embed that alerted for pageSpeed: " + pagesWithSoundCloudEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of SoundCloud embed: " + soundCloud.map(_.bytesDownloaded).sum.toDouble/(soundCloud.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val spotifyString =  "Spotify: \n" + "\n" +
      "Number of Pages with Spotify embed: " + pagesWithSpotifyEmbed.length  + "\n" +
      "Number of Pages with Spotify embed that alerted for pageWeight: " + pagesWithSpotifyEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Spotify embed that alerted for pageSpeed: " + pagesWithSpotifyEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Spotify embed: " + spotify.map(_.bytesDownloaded).sum.toDouble/(spotify.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val twitterString =  "Twitter: \n" + "\n" +
      "Number of Pages with Twitter embed: " + pagesWithTwitterEmbed.length  + "\n" +
      "Number of Pages with Twitter embed that alerted for pageWeight: " + pagesWithTwitterEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Twitter embed that alerted for pageSpeed: " + pagesWithTwitterEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Twitter embed: " + twitter.map(_.bytesDownloaded).sum.toDouble/(twitter.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val uStreamString =  "UStream: \n" + "\n" +
      "Number of Pages with UStream embed: " + pagesWithUStreamEmbed.length  + "\n" +
      "Number of Pages with UStream embed that alerted for pageWeight: " + pagesWithUStreamEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with UStream embed that alerted for pageSpeed: " + pagesWithUStreamEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of UStream embed: " + uStream.map(_.bytesDownloaded).sum.toDouble/(uStream.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val vevoString =  "Vevo: \n" + "\n" +
      "Number of Pages with Vevo embed: " + pagesWithVevoEmbed.length  + "\n" +
      "Number of Pages with Vevo embed that alerted for pageWeight: " + pagesWithVevoEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Vevo embed that alerted for pageSpeed: " + pagesWithVevoEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Vevo embed: " + vevo.map(_.bytesDownloaded).sum.toDouble/(vevo.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val vimeoString =  "Vimeo: \n" + "\n" +
      "Number of Pages with Vimeo embed: " + pagesWithVimeoEmbed.length  + "\n" +
      "Number of Pages with Vimeo embed that alerted for pageWeight: " + pagesWithVimeoEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Vimeo embed that alerted for pageSpeed: " + pagesWithVimeoEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Vimeo embed: " + vimeo.map(_.bytesDownloaded).sum.toDouble/(vimeo.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val youTubeString =  "YouTube: \n" + "\n" +
      "Number of Pages with YouTube embed: " + pagesWithYouTubeEmbed.length  + "\n" +
      "Number of Pages with YouTube embed that alerted for pageWeight: " + pagesWithYouTubeEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Youtube embed that alerted for pageSpeed: " + pagesWithYouTubeEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of YouTube embed: " + youTube.map(_.bytesDownloaded).sum.toDouble/(youTube.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val audioMP3String =  "Audio MP3: \n" + "\n" +
      "Number of Pages with Audio MP3 embeds: " + pagesWithMP3Embed.length  + "\n" +
      "Number of Pages with  Audio MP3 embeds that alerted for pageWeight: " + pagesWithMP3Embed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Audio MP3 embeds that alerted for pageSpeed: " + pagesWithMP3Embed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Audio MP3 embeds: " + mP3.map(_.bytesDownloaded).sum.toDouble/(mP3.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val videoMP4String =  "Video MP4: \n" + "\n" +
      "Number of Pages with Video MP4 embeds: " + pagesWithMP4Embed.length  + "\n" +
      "Number of Pages with  Video MP4 embeds that alerted for pageWeight: " + pagesWithMP4Embed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with Video MP4 embeds that alerted for pageSpeed: " + pagesWithMP4Embed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of Video MP4 embeds: " + mP4.map(_.bytesDownloaded).sum.toDouble/(mP4.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val unknownEmbedString =  "Unknown Embed: \n" + "\n" +
      "Number of Pages with unknown embeds: " + pagesWithUnknownEmbed.length  + "\n" +
      "Number of Pages with  unknown embeds that alerted for pageWeight: " + pagesWithUnknownEmbed.count(_.alertStatusPageWeight) + "\n" +
      "Number of Pages with unknown embeds that alerted for pageSpeed: " + pagesWithUnknownEmbed.count(_.alertStatusPageSpeed) + "\n" +
      "Average size of unknown embeds: " + unknownElement.map(_.bytesDownloaded).sum.toDouble/(unknownElement.length * 1024) + " kB" + "\n" +
      "\n\n" + "\n"
    val endString =  "\n ****************************************************\n\n\n\n"

    val summaryString = runString + elementSummary +
    audioBoomString +
    brightCoveString +
    cnnString +
    dailyMotionString +
    documentCloudString +
    facebookString +
    formstackString +
    gifEmbedString +
    googleMapsString +
    guardianAudioString +
    guardianCommentsString +
    guardianVideosString +
    guardianImagesString +
    guardianUploadString +
    guardianWitnessImageString +
    guardianWitnessVideoString +
    huluString +
    imageEmbedString +
    instagramString +
    infoStradaString +
    parliamentLiveTVString +
    scribdString +
    soundCloudString +
    spotifyString +
    twitterString +
    uStreamString +
    vevoString +
    vimeoString +
    youTubeString +
    audioMP3String +
    videoMP4String +
    unknownEmbedString +
    endString

    summaryString
  }

}
