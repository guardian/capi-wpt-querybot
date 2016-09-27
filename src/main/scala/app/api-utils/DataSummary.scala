package app.api

import app.apiutils.{PageElementFromHTMLTableRow, ResultsFromPreviousTests, PerformanceResultsObject}
import com.gu.contentapi.client.model.v1.ContentType
import org.joda.time.DateTime


/**
 * Created by mmcnamara on 28/06/16.
 */
class DataSummary(jobStarted: DateTime, jobFinished: DateTime, numberOfPagesFromCapi: Int, numberOfPagesTested: Int, latestResults: List[PerformanceResultsObject], previousResultsObject: ResultsFromPreviousTests, alertsResultsObject: ResultsFromPreviousTests) {
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
                                percentageOfPagesWithEmbed: Double,
                                numberOfPageWeightAlerts: Int,
                                numberOfPageSpeedAlerts: Int,
                                percentageOfPageWeightAlerts: Double,
                                percentageOfPageSpeedAlerts: Double,
                                averageSizeOfEmbeds: Double,
                                averageTimeFirstPaint: Int,
                                averageSpeedIndexMs: Int)


  case class PageSummaryData(pageType: String,
                              testType: String,
                              numberOfPagesTested: Int,
                              numberOfPageWeightAlerts: Int,
                              percentageOfPageWeightAlerts: Double,
                              numberOfPageSpeedAlerts: Int,
                              percentageOfPageSpeedAlerts: Double,
                              averagePageWeight: Double,
                              averageTTFP: Int,
                              averageSpeedIndex: Int
                              )

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
  val alertsResultsHandler = alertsResultsObject
  val numberOfPagesRetestedFromLastRun: Int = previousResultsHandler.previousResultsToRetest.length

  val resultsFromRun: List[PerformanceResultsObject] = latestResults
  val previousResults: List[PerformanceResultsObject] = previousResultsHandler.previousResults
  val allResults: List[PerformanceResultsObject] = resultsFromRun ::: previousResults
  val allDesktopResults: List[PerformanceResultsObject] = allResults.filter(_.typeOfTestName.contains("Desktop"))
  val allMobileResults: List[PerformanceResultsObject] = allResults.filter(_.typeOfTestName.contains("Mobile"))

  val totalNumberOfTests = allResults.length
  val totalNumberOfDesktopTests = allDesktopResults.length
  val totalNumberOfMobileTests = allMobileResults.length

  val hasPreviouslyAlertedOnWeight: List[PerformanceResultsObject] = alertsResultsHandler.hasPreviouslyAlerted.filter(_.alertStatusPageWeight)
  val hasPreviouslyAlertedOnSpeed: List[PerformanceResultsObject] = previousResultsHandler.hasPreviouslyAlerted.filter(_.alertStatusPageSpeed)

  val todaysResults = for (result <- allResults if DateTime.parse(result.timeOfTest).getDayOfYear == today) yield result
  val yesterdaysResults = for (result <- allResults if DateTime.parse(result.timeOfTest).getDayOfYear != yesterday) yield result

  val activePageWeightAlerts = resultsFromRun.filter(_.alertStatusPageWeight):::previousResultsHandler.oldResults.filter(_.alertStatusPageWeight)
  val activePageSpeedAlerts = resultsFromRun.filter(_.alertStatusPageSpeed):::previousResultsHandler.oldResults.filter(_.alertStatusPageSpeed)
  val activeSlowButUnderWeight = activePageSpeedAlerts.filter(!_.alertStatusPageWeight)

  val hasAlertedOnWeightThisRun = resultsFromRun.filter(_.alertStatusPageWeight)
  val hasAlertedOnSpeedThisRun = resultsFromRun.filter(_.alertStatusPageSpeed)
  
  val newPageWeightAlerts = for (result <- hasAlertedOnWeightThisRun if !hasPreviouslyAlertedOnWeight.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result
  val newPageSpeedAlerts = for (result <- hasAlertedOnSpeedThisRun if !hasPreviouslyAlertedOnSpeed.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result

  val pageWeightAlertsThatHaveBeenResolvedThisRun = for (result <- hasPreviouslyAlertedOnWeight if !activePageWeightAlerts.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result
  val pageSpeedAlertsThatHaveBeenResolvedThisRun = for (result <- hasPreviouslyAlertedOnSpeed if !activePageSpeedAlerts.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result
  val pageSpeedAlertsResolvedForUnderweightPages = pageSpeedAlertsThatHaveBeenResolvedThisRun.filter(!_.alertStatusPageWeight)

  val dateOfOldestTest = previousResultsHandler.timeOfOldestTest
  val dateOfOldestAlert = alertsResultsHandler.timeOfOldestTest

  // todo - Need some way of persisting these values
  val numberOfPageWeightAlertsResolvedThisRun = pageWeightAlertsThatHaveBeenResolvedThisRun.length
  val numberOfPageSpeedAlertsResolvedThisRun = pageSpeedAlertsThatHaveBeenResolvedThisRun.length
  val numberOfPageSpeedAlertsResolvedForUnderWeightPagesThisRun = pageSpeedAlertsThatHaveBeenResolvedThisRun.length

  val totalPageWeightAlertsThatHaveBeenResolved = for (result <- alertsResultsHandler.previousResults if !activePageWeightAlerts.map(page => (page.testUrl, page.typeOfTest)).contains((result.testUrl, result.typeOfTest))) yield result
  val totalNumberOfPageWeightAlertsTriggered = alertsResultsHandler.previousResults.length
  val totalNumberOfPageWeightAlertsResolved = totalPageWeightAlertsThatHaveBeenResolved.length
  // todo - Use persisted values from previous runs
  //val numberOfPageWeightAlertsResolvedLast24Hrs = numberOfPageWeightAlertsResolvedThisRun + Some_value_we_store
  //val numberOfPageSpeedAlertsResolvedLast24Hrs = numberOfPageSpeedAlertsResolvedThisRun + Some_value_we_store
  //val numberOfPageSpeedAlertsResolvedForUnderWeightPagesLast24Hrs = numberOfPageSpeedAlertsResolvedForUnderWeightPagesThisRun + Some_value_we_store

  //val numberOfPageWeightAlertsResolvedSoFar = numberOfPageWeightAlertsResolvedThisRun + Some_value_we_store
  //val numberOfPageSpeedAlertsResolvedSoFar = numberOfPageSpeedAlertsResolvedThisRun + Some_value_we_store
  //val numberOfPageSpeedAlertsResolvedForUnderWeightPagesSoFar = numberOfPageSpeedAlertsResolvedForUnderWeightPagesThisRun + Some_value_we_store


  val articles = allResults.filter(_.pageType.contains("Article")).filter(!_.gLabs)
  val articlesDesktop = articles.filter(_.typeOfTestName.contains("Desktop"))
  val articlesMobile = articles.filter(_.typeOfTestName.contains("Mobile"))

  val interactives = allResults.filter(_.pageType.contains("Interactive")).filter(!_.gLabs)
  val interactivesDesktop = interactives.filter(_.typeOfTestName.contains("Desktop"))
  val interactivesMobile = interactives.filter(_.typeOfTestName.contains("Mobile"))

  val liveBlogs = allResults.filter(_.pageType.contains("LiveBlog")).filter(!_.gLabs)
  val liveBlogsDesktop = liveBlogs.filter(_.typeOfTestName.contains("Desktop"))
  val liveBlogsMobile = liveBlogs.filter(_.typeOfTestName.contains("Mobile"))

  val gLabs = allResults.filter(_.gLabs)
  val gLabsDesktop = gLabs.filter(_.typeOfTestName.contains("Desktop"))
  val gLabsMobile = gLabs.filter(_.typeOfTestName.contains("Mobile"))

  val isATotal = true
  val notATotal = false

  val articlesCombinedSummary = summarisePageType(articles, notATotal)
  val articlesDesktopSummary = summarisePageType(articlesDesktop, notATotal)
  val articlesMobileSummary = summarisePageType(articlesMobile, notATotal)

  val interactivesCombinedSummary = summarisePageType(interactives, notATotal)
  val interactivesDesktopSummary = summarisePageType(interactivesDesktop, notATotal)
  val interactivesMobileSummary = summarisePageType(interactivesMobile, notATotal)

  val liveBlogsCombinedSummary = summarisePageType(liveBlogs, notATotal)
  val liveBlogsDesktopSummary = summarisePageType(liveBlogsDesktop, notATotal)
  val liveBlogsMobileSummary = summarisePageType(liveBlogsMobile, notATotal)

  val gLabsCombinedSummary = summarisePageType(gLabs, notATotal)
  val gLabsDesktopSummary = summarisePageType(gLabsDesktop, notATotal)
  val gLabsMobileSummary = summarisePageType(gLabsMobile, notATotal)

  val totalCombinedSummary = summarisePageType(allResults, isATotal)
  val totalDesktopSummary = summarisePageType(allDesktopResults, isATotal)
  val totalMobileSummary = summarisePageType(allMobileResults, isATotal)


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
  val pagesWithM3U8Embed = allResults.filter(pageContainsElementType(_, "m3u8"))
  val pagesWithMP3Embed = allResults.filter(pageContainsElementType(_, "Audio Embed"))
  val pagesWithMP4Embed = allResults.filter(pageContainsElementType(_, "Video Embed"))
  val pagesWithParliamentLiveTvEmbed = allResults.filter(pageContainsElementType(_, "parliamentLiveTv"))
  val pagesWithScribdEmbed = allResults.filter(pageContainsElementType(_, "scribd"))
  val pagesWithSoundCloudEmbed = allResults.filter(pageContainsElementType(_, "soundCloud"))
  val pagesWithSpotifyEmbed = allResults.filter(pageContainsElementType(_, "spotify"))
  val pagesWithTwitterEmbed = allResults.filter(pageContainsElementType(_, "twitter"))
  val pagesWithUStreamEmbed = allResults.filter(pageContainsElementType(_, "uStream"))
  val pagesWithVevoEmbed = allResults.filter(pageContainsElementType(_, "vevo"))
  val pagesWithVideo3GPEmbed = allResults.filter(pageContainsElementType(_, "3gp"))
  val pagesWithVimeoEmbed = allResults.filter(pageContainsElementType(_, "vimeo"))
  val pagesWithVineEmbed = allResults.filter(pageContainsElementType(_, "vine"))
  val pagesWithWebPEmbed = allResults.filter(pageContainsElementType(_, "webp"))
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
  val m3u8: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("m3u8", List())
  val mP3: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("Audio Embed", List())
  val mP4: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("Video Embed", List())
  val parliamentLiveTv: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("parliamentLiveTv", List())
  val scribd: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("scribd", List())
  val soundCloud: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("soundCloud", List())
  val spotify: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("spotify", List())
  val twitter: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("twitter", List())
  val uStream: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("uStream", List())
  val vevo: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("vevo", List())
  val video3GP: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("3gp", List())
  val vimeo: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("vimeo", List())
  val vine: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("vine", List())
  val webp: List[PageElementFromHTMLTableRow] = mapOfElementsByType.getOrElse("webp", List())
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
  val m3u8Summary = summariseElement("m3u8 (iOS specific video format)", pagesWithM3U8Embed, m3u8)
  val mP3Summary = summariseElement("mp3 video format)", pagesWithMP3Embed, mP3)
  val mP4Summary = summariseElement("mp4 video format", pagesWithMP4Embed, mP4)
  val parliamentLiveTvSummary = summariseElement("parliamentLiveTv", pagesWithParliamentLiveTvEmbed, parliamentLiveTv)
  val scribdSummary = summariseElement("scribd", pagesWithScribdEmbed, scribd)
  val soundCloudSummary = summariseElement("soundCloud", pagesWithSoundCloudEmbed, soundCloud)
  val spotifySummary = summariseElement("spotify", pagesWithSpotifyEmbed, spotify)
  val twitterSummary = summariseElement("twitter", pagesWithTwitterEmbed, twitter)
  val uStreamSummary = summariseElement("uStream", pagesWithUStreamEmbed, uStream)
  val vevoSummary = summariseElement("vevo", pagesWithVevoEmbed, vevo)
  val video3GPSummary = summariseElement("3gp video format", pagesWithVideo3GPEmbed, video3GP)
  val vimeoSummary = summariseElement("vimeo", pagesWithVimeoEmbed, vimeo)
  val vineSummary = summariseElement("vine", pagesWithVineEmbed, vine)
  val webpSummary = summariseElement("webp video format", pagesWithWebPEmbed, webp)
  val youTubeSummary = summariseElement("youTube", pagesWithYouTubeEmbed, youTube)
  val unknownEmbedSummary = summariseElement("Unidentified element", pagesWithUnknownEmbed, unknownElement)

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
        m3u8Summary,
        mP3Summary,
        mP4Summary,
        parliamentLiveTvSummary,
        scribdSummary,
        soundCloudSummary,
        spotifySummary,
        twitterSummary,
        uStreamSummary,
        vevoSummary,
        video3GPSummary,
        vimeoSummary,
        vineSummary,
        webpSummary,
        youTubeSummary,
        unknownEmbedSummary
  )

  val sortedCombinedSummaryList = summaryList.sortWith(_.numberOfPagesWithEmbed > _.numberOfPagesWithEmbed)




  def getSamplePageArray: Array[Option[PerformanceResultsObject]] = {
    val audioboom = getPage(pagesWithAudioBoomEmbed)
    val brightcove = getPage(pagesWithBrightcoveEmbed)
    val cnn = getPage(pagesWithCNNEmbed)
    val dailymotion = getPage(pagesWithDailyMotionEmbed)
    val documentCloud = getPage(pagesWithDocumentCloudEmbed)
    val facebook = getPage(pagesWithFacebookEmbed)
    val formStack = getPage(pagesWithFormStackEmbed)
    val gif = getPage(pagesWithGifEmbed)
    val googleMaps = getPage(pagesWithGoogleMapsEmbed)
    val guardianAudio = getPage(pagesWithGuardianAudio)
    val guardianComments = getPage(pagesWithGuardianCommentsEmbed)
    val guardianVideo = getPage(pagesWithGuardianVideos)
    val guardianImages = getPage(pagesWithGuardianImages)
    val guardianUpload = getPage(pagesWithGuardianUpload)
    val guardianWitnessImage = getPage(pagesWithGuardianWitnessImageEmbed)
    val guardianWitnessVideo = getPage(pagesWithGuardianWitnessVideoEmbed)
    val hulu = getPage(pagesWithHuluEmbed)
    val image = getPage(pagesWithImageEmbed)
    val infoStrada = getPage(pagesWithInfoStradaEmbed)
    val instagram = getPage(pagesWithInstagramEmbed)
    val interactive = getPage(pagesWithInteractiveEmbed)
    val m3u8 = getPage(pagesWithM3U8Embed)
    val mp3 = getPage(pagesWithMP3Embed)
    val mp4 = getPage(pagesWithMP4Embed)
    val parliamentLiveTv = getPage(pagesWithParliamentLiveTvEmbed)
    val scribd = getPage(pagesWithScribdEmbed)
    val soundCloud = getPage(pagesWithSoundCloudEmbed)
    val spotify = getPage(pagesWithSpotifyEmbed)
    val twitter = getPage(pagesWithTwitterEmbed)
    val uStream = getPage(pagesWithUStreamEmbed)
    val vevo = getPage(pagesWithVevoEmbed)
    val video3GP = getPage(pagesWithVideo3GPEmbed)
    val vimeo = getPage(pagesWithVimeoEmbed)
    val vine = getPage(pagesWithVineEmbed)
    val webp = getPage(pagesWithWebPEmbed)
    val youTube = getPage(pagesWithYouTubeEmbed)
    val unknownEmbed = getPage(pagesWithUnknownEmbed)

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
      m3u8,
      mp3,
      mp4,
      parliamentLiveTv,
      scribd,
      soundCloud,
      spotify,
      twitter,
      uStream,
      vevo,
      video3GP,
      vimeo,
      vine,
      webp,
      youTube,
      unknownEmbed
    )
    stringArray
  }

  def getPage(pageList: List[PerformanceResultsObject]): Option[PerformanceResultsObject] = {
    if (pageList.nonEmpty) {
      Option(pageList.head)
    } else {
      None
    }
  }

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
    val checkEditorialElementList = testResult.editorialElementList.exists(_.determinedResourceType.contains(typeName))
    if(typeName.contains("audioBoom") && testResult.editorialElementList.exists(_.determinedResourceType.contains("audioBoom"))) {
      println("typeName is : " + typeName)
      println("resource types: " + testResult.editorialElementList.map(_.determinedResourceType + "\n").mkString)
      println("checkEditorialElementList result: " + checkEditorialElementList)
    }
    //val checkEditorialElementList: List[PageElementFromHTMLTableRow] = for (element <- testResult.editorialElementList if element.determinedResourceType.contains(typeName)) yield element
    val checkFullElementList = testResult.fullElementList.exists(_.determinedResourceType.contains(typeName))
    //val checkFullElementList: List[PageElementFromHTMLTableRow] = for (element <- testResult.fullElementList if element.determinedResourceType.contains(typeName)) yield element
    //    val checkFullElementList = result.fullElementList.map(_.identifyPageElementType().contains(typeName))
    //val result = checkEditorialElementList.nonEmpty || checkFullElementList.nonEmpty
    //result
    checkEditorialElementList || checkFullElementList
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
    val elementString: String = sortedCombinedSummaryList.map(elementData => returnElementSummaryAsHTMLString(elementData)).mkString
    runString + elementString
  }

  def summarisePageType(results: List[PerformanceResultsObject], total: Boolean): PageSummaryData = {
    if(results.nonEmpty) {
      val contentType = results.head.getPageType
      val pageType = {
        if (results.head.gLabs && !total) {
          "GLabs"
        } else {
          if (total) {
            "Total"
          } else {
            contentType
          }
        }
      }
      val testType = {
        if ((results.head.typeOfTestName.contains("Mobile") && (results.count(_.typeOfTestName.contains("Desktop")) == 0)) ||
          (results.head.typeOfTestName.contains("Desktop") && (results.count(_.typeOfTestName.contains("Mobile")) == 0))) {
          results.head.typeOfTestName
        } else {
          "Both Desktop and Mobile tests"
        }
      }

      val numberOfTests = results.length
      val numberOfPageWeightAlerts = results.count(_.alertStatusPageWeight)
      val percentageOfPageWeightAlerts = roundAt(2)((numberOfPageWeightAlerts * 100).toDouble / numberOfTests)
      val numberOfPageSpeedAlerts = results.count(_.alertStatusPageSpeed)
      val percentageOfPageSpeedAlerts = roundAt(2)((numberOfPageSpeedAlerts * 100).toDouble / numberOfTests)
      val averagePageWeight = roundAt(3)(results.map(_.kBInFullyLoaded).sum / results.length)
      val averageTTFP = (results.map(_.timeFirstPaintInMs).sum.toDouble / results.length).toInt
      val averageSpeedIndex = (results.map(_.speedIndex).sum.toDouble / results.length).toInt

      PageSummaryData(pageType, testType, numberOfTests, numberOfPageWeightAlerts, percentageOfPageWeightAlerts, numberOfPageSpeedAlerts, percentageOfPageSpeedAlerts, averagePageWeight, averageTTFP, averageSpeedIndex)
    } else {
      println("summarisePageType was passed an empty list!")
      PageSummaryData("Empty", "Desktop", 0, 0, 0, 0, 0, 0, 0, 0)
    }
  }

  def summariseElement(elementName: String, pagesWithEmbed: List[PerformanceResultsObject], listOfEmbeds: List[PageElementFromHTMLTableRow]): ElementSummaryData = {
    val title = elementName + ":\n"
    val numberOfPagesWithEmbed = pagesWithEmbed.length
    val numberOfPageWeightAlerts = pagesWithEmbed.count(_.alertStatusPageWeight)
    val numberOfPageSpeedAlerts = pagesWithEmbed.count(_.alertStatusPageSpeed)
    val percentageOfPagesWithEmbed = {
      if (numberOfPagesWithEmbed > 0) {
        roundAt(2)((numberOfPagesWithEmbed.toDouble / totalNumberOfTests) * 100)
      } else {
        0
      }
    }
    val percentageOfPageWeightAlerts = {
      if (numberOfPageWeightAlerts > 0) {
        roundAt(2)((numberOfPageWeightAlerts.toDouble / numberOfPagesWithEmbed) * 100)
      } else {
        0
      }
    }
    val percentageOfPageSpeedAlerts = {
      if (numberOfPageSpeedAlerts > 0) {
        roundAt(2)((numberOfPageSpeedAlerts.toDouble / numberOfPagesWithEmbed) * 100)
      } else {
        0
      }
    }
    val averageSizeOfEmbedsKb = roundAt(2)(listOfEmbeds.map(_.bytesDownloaded).sum.toDouble / (listOfEmbeds.length * 1024))
    val averageTimeFirstPaintMs = (pagesWithEmbed.map(_.timeFirstPaintInMs).sum.toDouble / pagesWithEmbed.length).toInt
    val averageSpeedIndexMs = (pagesWithEmbed.map(_.speedIndex).sum.toDouble / pagesWithEmbed.length).toInt

    new ElementSummaryData(title,
      numberOfPagesWithEmbed,
      percentageOfPagesWithEmbed,
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