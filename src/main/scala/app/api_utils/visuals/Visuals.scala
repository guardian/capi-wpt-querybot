package app.api_utils.visuals

import app.api_utils.model.VisualsElementType
import com.gu.contentapi.client.model.v1.CapiDateTime
import org.joda.time.DateTime

/**
 * Created by mmcnamara on 27/05/16.
 */

case class Visuals(pageType: String, id: String, webPublicationDate: String, findDate: String, headline: String, url: String, types: Seq[VisualsElementType]) {
  val typeOfPage: String = setPageType(pageType)
  val pageId: String = id
  val pageWebPublicationDate: CapiDateTime = convertStringToCapiDateTime(webPublicationDate)
  val pageFindDate: CapiDateTime = convertStringToCapiDateTime(findDate)
  val pageHeadline: String = headline
  val pageUrl: String = url
  val seqOfTypes: Seq[VisualsElementType] = types


  def convertStringToCapiDateTime(time: String): CapiDateTime = {
    val timeAsDateTime = new DateTime(time)
    val timeAsLong = timeAsDateTime.getMillis
    val iso8061String = timeAsDateTime.toLocalDateTime.toString
    CapiDateTime.apply(timeAsLong, iso8061String)
  }

  def setPageType(givenType: String): String = {
    if(!givenType.contains("nteractive")){
      givenType + " with Interactive Elements"
    } else {
      givenType
    }
  }

}
