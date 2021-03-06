package services.api

import services.apiutils.PerformanceResultsObject
import com.gu.contentapi.client.model.v1.{Tag, CapiDateTime, ContentFields}

/**
 * Created by mmcnamara on 15/03/16.
 */
class WptResultPageListener(page: String, tone: String, fields: Option[ContentFields], tags: Seq[Tag],resultUrl: String, createdBy: Option[String]) {

  val pageUrl: String = page
  val pageType: String = tone
  val pageFields: Option[ContentFields] = fields
  val tagList: Seq[Tag] = tags
  val headline: Option[String] = pageFields.flatMap(_.headline)
  val firstPublished: Option[CapiDateTime] = pageFields.flatMap(_.firstPublicationDate)
  val pageLastModified: Option[CapiDateTime] = pageFields.flatMap(_.lastModified)
  val liveBloggingNow: Option[Boolean] = pageFields.flatMap(_.liveBloggingNow)
  var productionOffice: Option[String] = pageFields.flatMap(_.productionOffice.map(_.name))
  var contentCreator: Option[String] = createdBy
  val gLabs = tagList.exists(_.id.contains("tone/advertisement-features"))
  val wptResultUrl: String = resultUrl
  var testComplete: Boolean = false
  var confirmationNeeded: Boolean = false
  var wptConfirmationResultUrl: String = ""
  var confirmationComplete: Boolean = false
  var testResults: PerformanceResultsObject = null
  
}
