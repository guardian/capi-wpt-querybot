package app.api_utils.web_page_test

import app.App.setAlertStatus
import app.api_utils.capi_queries.ContentPage
import app.api_utils.model.{PageAverageObject, PerformanceResultsObject}
import com.gu.contentapi.client.model.v1.{CapiDateTime, ContentFields, Office, Tag}
import scala.collection.parallel.immutable.ParSeq

/**
 * Created by mmcnamara on 15/03/16.
 */

case class WptTestRequest(pageUrl: String,
                           pageType: String,
                           pageFields: Option[ContentFields],
                           tagList: Seq[Tag],
                           wptResultUrl: String,
                           contentCreator: Option[String]
                          )

case class WptTestResult(pageUrl: String,
                         pageType: String,
                         pageFields: Option[ContentFields],
                         headline: Option[String],
                         firstPublished: Option[CapiDateTime],
                         pageLastModified: Option[CapiDateTime],
                         liveBloggingNow: Option[Boolean],
                         productionOffice: Option[Office],
                         tagList: Seq[Tag],
                         gLabs: Boolean,
                         wptResultUrl: String,
                         contentCreator: Option[String],
                         testResult: PerformanceResultsObject
                                 )

object WptTestResult{

  def listenForResultPages(
                            capiPages: List[ContentPage],
                            contentType: String,
                            resultUrlList: List[(String, String)],
                            averages: PageAverageObject,
                            wptBaseUrl: String,
                            wptApiKey: String,
                            wptLocation: String,
                            urlFragments: List[String]
                          ): List[PerformanceResultsObject] = {

    val listenerList: List[WptTestRequest] = capiPages.flatMap(page => {
      for (element <- resultUrlList if element._1 == page.webUrl) yield WptTestRequest(element._1, contentType, page.fields, page.tags, element._2, page.creatorEmail)
    })

    val resultsList: ParSeq[WptTestResult] = listenerList.par.map(testRequest => {
      val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
      val result = new WptTestResult(
        testRequest.pageUrl,
        testRequest.pageType,
        testRequest.pageFields,
        testRequest.pageFields.get.headline,
        testRequest.pageFields.get.firstPublicationDate,
        testRequest.pageFields.get.lastModified,
        testRequest.pageFields.get.liveBloggingNow,
        testRequest.pageFields.get.productionOffice,
        testRequest.tagList,
        testRequest.tagList.exists(_.id.contains("tone/advertisement-features")),
        testRequest.wptResultUrl,
        testRequest.contentCreator,
        wpt.getResults(testRequest.pageUrl, testRequest.wptResultUrl)
      )
      result
    })
    val testResults = resultsList.map(result => result.testResult).toList

    testResults.map(setAlertStatus(_, averages))
  }

  def setAlertStatus(resultObject: PerformanceResultsObject, averages: PageAverageObject): PerformanceResultsObject = {
    //  Add results to string which will eventually become the content of our results file
    if (resultObject.typeOfTest == "Desktop") {
      if (resultObject.kBInFullyLoaded >= averages.desktopKBInFullyLoaded) {
        println("PageWeight Alert Set")
        resultObject.pageWeightAlertDescription = "the page is too heavy. Please examine the list of embeds below for items that are unexpectedly large."
        resultObject.alertStatusPageWeight = true
      }
      else {
        println("PageWeight Alert not set")
        resultObject.alertStatusPageWeight = false
      }
      if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) ||
        (resultObject.speedIndex >= averages.desktopSpeedIndex)) {
        println("PageSpeed alert set")
        resultObject.alertStatusPageSpeed = true
        if ((resultObject.timeFirstPaintInMs >= averages.desktopTimeFirstPaintInMs) && (resultObject.speedIndex >= averages.desktopSpeedIndex)) {
          resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) and time till page looks loaded (SpeedIndex) are unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
        } else {
          if (resultObject.speedIndex >= averages.desktopSpeedIndex) {
            resultObject.pageSpeedAlertDescription = "Time till page looks loaded (SpeedIndex) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
          else {
            resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
        }
      } else {
        println("PageSpeed alert not set")
        resultObject.alertStatusPageSpeed = false
      }
    } else {
      //checking if status of mobile test needs an alert
      if (resultObject.kBInFullyLoaded >= averages.mobileKBInFullyLoaded) {
        println("PageWeight Alert Set")
        resultObject.pageWeightAlertDescription = "the page is too heavy. Please examine the list of embeds below for items that are unexpectedly large."
        resultObject.alertStatusPageWeight = true
      }
      else {
        println("PageWeight Alert not set")
        resultObject.alertStatusPageWeight = false
      }
      if ((resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) ||
        (resultObject.speedIndex >= averages.mobileSpeedIndex)) {
        println("PageSpeed alert set")
        resultObject.alertStatusPageSpeed = true
        if ((resultObject.timeFirstPaintInMs >= averages.mobileTimeFirstPaintInMs) && (resultObject.speedIndex >= averages.mobileSpeedIndex)) {
          resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) and time till page looks loaded (SpeedIndex) are unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
        } else {
          if (resultObject.speedIndex >= averages.mobileSpeedIndex) {
            resultObject.pageSpeedAlertDescription = "Time till page looks loaded (SpeedIndex) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
          else {
            resultObject.pageSpeedAlertDescription = "Time till page is scrollable (time-to-first-paint) is unusually high. Please investigate page elements below or contact <a href=mailto:\"dotcom.health@guardian.co.uk\">the dotcom-health team</a> for assistance."
          }
        }
      } else {
        println("PageSpeed alert not set")
        resultObject.alertStatusPageSpeed = false
      }
    }
    println("Returning test result with alert flags set to relevant values")
    resultObject
  }


}





