package services.api

import java.io.{FileInputStream, File}

import services.apiutils.{VisualsElementType, Visuals}
import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Created by mmcnamara on 18/04/16.
 */
class JSONOperations() {

/*  def readAndParse(file: File) = {
    val stream = new FileInputStream(file)
    val json = try {  Json.parse(stream) } finally { stream.close() }

  }*/

  def stringToVisualsPages(jsonString: String): Seq[Visuals] = {
    val myJson = Json.parse(jsonString)

    implicit val typeReads: Reads[VisualsElementType] = (
  (JsPath \ "type").read[String] and
    (JsPath \ "alt").read[String] and
    (JsPath \ "canonicalUrl").read[String] and
    (JsPath \ "bootUrl").read[String]
  )(VisualsElementType.apply _)

    implicit val jsonPageReads: Reads[Visuals] = (
  (JsPath \ "pageType").read[String] and
    (JsPath \ "id").read[String] and
    (JsPath \ "webPublicationDate").read[String] and
    (JsPath \ "findDate").read[String] and
    (JsPath \ "headline").read[String] and
    (JsPath \ "url").read[String] and
    (JsPath \ "types").read[Seq[VisualsElementType]]
  )(Visuals.apply _)

/*    implicit val jsonPageArrayReads: Reads[Seq[Visuals]] = (
      (JsPath \ "pages").read[Seq[Visuals]]
      )(Seq[Visuals].apply _)
*/

    implicit val jsonPageArrayReads: Reads[Seq[Visuals]] = Reads.seq(jsonPageReads)

    myJson.validate[Seq[Visuals]] match {
      case s: JsSuccess[Seq[Visuals]] => {
        val visualsList = s.get
//        println("extracted page from json. page values are: \n" + visualsList.foreach(page => page.toString))
        visualsList
      }
      case e: JsError => {
        println("couldn't extract page from json")
        val emptyList: List[Visuals] = List()
        emptyList
      }
    }

  }


}
