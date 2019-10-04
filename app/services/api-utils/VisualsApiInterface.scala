package services.apiutils

import okhttp3.{OkHttpClient, Request, Response}
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._



/**
 * Created by mmcnamara on 30/03/16.
 */
class VisualsApiInterface(url: String) {

  val apiUrl: String = url
  implicit val httpClient = new OkHttpClient()

  def getTodaysPages(): List[String] = {
    Logger.info("Requesting result url:" + apiUrl)
    val request: Request = new Request.Builder()
      .url(apiUrl)
      .get()
      .build()
    val response: Response = httpClient.newCall(request).execute()
//    val responseJSON: Json = argonaut.Json.
    Logger.info("Api call response = \n"  + response.body.toString)
    List(response.body().toString)

  }
/*
  case class JsonPage(pageType: String, id: String, webPublicationDate: String, findDate: String, headline: String, url: String, types: Seq[VisualsElementType])
  case class VisualsElementType(elementType: String, alt: String, canonicalUrl: String, bootUrl: String )

  //val jsonString = "{\"pageType\": \"article\", \"id\": \"law/2016/may/23/supreme-court-ruling-timothy-tyrone-foster-death-sentence\", \"webPublicationDate\": \"2016-05-23T14:38:05Z\", \"findDate\": \"2016-05-23T14:58:04.912Z\", \"headline\": \"US supreme court voids Georgia man's death sentence over racial bias on jury\", \"url\": \"https://www.theguardian.com/law/2016/may/23/supreme-court-ruling-timothy-tyrone-foster-death-sentence\"}"

  val jsonString = """
"{pages":[{
  "pageType": "article",
  "id": "law/2016/may/23/supreme-court-ruling-timothy-tyrone-foster-death-sentence",
  "webPublicationDate": "2016-05-23T14:38:05Z",
  "findDate": "2016-05-23T14:58:04.912Z",
  "headline": "US supreme court voids Georgia man's death sentence over racial bias on jury",
  "url": "https://www.theguardian.com/law/2016/may/23/supreme-court-ruling-timothy-tyrone-foster-death-sentence",
  "types": [
  {
    "type": "iframe",
    "alt": "The prosecution’s notes revealed prosecutors’ focus on the black people in the jury pool",
    "canonicalUrl": "https://interactive.guim.co.uk/embed/documentcloud/index.html?docid=2841205-supreme-court-timothy-tyrone-foster",
    "bootUrl": "https://interactive.guim.co.uk/embed/iframe-wrapper/0.1/boot.js"
  }
  ]
},
{
  "pageType": "article",
  "id": "world/2016/may/23/far-right-candidate-defeated-austrian-presidential-election-norbert-hofer",
  "webPublicationDate": "2016-05-23T14:27:01Z",
  "findDate": "2016-05-23T15:17:04.531Z",
  "headline": "Far-right candidate narrowly defeated in Austrian presidential election",
  "url": "https://www.theguardian.com/world/2016/may/23/far-right-candidate-defeated-austrian-presidential-election-norbert-hofer",
  "types": [
  {
    "type": "iframe",
    "alt": "Austrian election result",
    "canonicalUrl": "https://interactive.guim.co.uk/charts/embed/may/2016-05-23T15:12:49/embed.html",
    "bootUrl": "https://interactive.guim.co.uk/embed/iframe-wrapper/0.1/boot.js"
  }
  ]
}]}"""
  /*,
  {
    "pageType": "article",
    "id": "politics/2016/may/23/cameron-warns-against-self-destruct-vote-to-leave-eu",
    "webPublicationDate": "2016-05-23T10:28:47Z",
    "findDate": "2016-05-23T10:50:04.259Z",
    "headline": "Cameron warns against 'self-destruct' vote to leave EU",
    "url": "https://www.theguardian.com/politics/2016/may/23/cameron-warns-against-self-destruct-vote-to-leave-eu",
    "types": [
    {
      "type": "iframe",
      "alt": "Brexit explained: numbers carousel",
      "canonicalUrl": "https://interactive.guim.co.uk/testing/2016/05/brexit-companion/embed/embed.html?sheet=beginner&id=numbers_1&format=carousel",
      "bootUrl": "https://interactive.guim.co.uk/embed/iframe-wrapper/0.1/boot.js"
    }
    ]
  }]"""*/

  val myjson = Json.parse(jsonString)
  //val page = Json.fromJson(json, jsonPage.getClass)
  Logger.info("Hi")
  Logger.info(myjson.toString())
  val pageTypeReads: Reads[String] = (JsPath \ "pageType").read[String]
  val idReads: Reads[String] = (JsPath \ "id").read[String]
  val webPubDateReads: Reads[String] = (JsPath \ "webPublicationDate").read[String]
  val findDateReads: Reads[String] = (JsPath \ "findDate").read[String]
  val headlineReads: Reads[String] = (JsPath \ "headline").read[String]
  val urlReads: Reads[String] = (JsPath \ "url").read[String]
  /*implicit val jsonPageReadsBuilder =
    (JsPath \ "pageType").read[String] and
      (JsPath \ "id").read[String] and
      (JsPath \ "webPublicationDate").read[String] and
      (JsPath \ "findDate").read[String] and
      (JsPath \ "headline").read[String] and
      (JsPath \ "url").read[String]


  implicit val jsonPageReads: Reads[JsonPage] = jsonPageReadsBuilder.apply(JsonPage.apply _)
  */

  implicit val typeReads: Reads[VisualsElementType] = (
  (JsPath \ "type").read[String] and
    (JsPath \ "alt").read[String] and
    (JsPath \ "canonicalUrl").read[String] and
    (JsPath \ "bootUrl").read[String]
  )(VisualsElementType.apply _)

  implicit val JsonPageReads: Reads[JsonPage] = (
  (JsPath \ "pageType").read[String] and
    (JsPath \ "id").read[String] and
    (JsPath \ "webPublicationDate").read[String] and
    (JsPath \ "findDate").read[String] and
    (JsPath \ "headline").read[String] and
    (JsPath \ "url").read[String] and
    (JsPath \ "types").read[Seq[VisualsElementType]]
  )(JsonPage.apply _)

  implicit val jsonPageArrayReads: Reads[Seq[JsonPage]] =
    (JsPath \ "pages").read[Seq[JsonPage]]


  myjson.validate[Seq[JsonPage]] match {
    case s: JsSuccess[Seq[JsonPage]] => {
      val jsonPage = s.get
      Logger.info("extracted page from json. page values are: \n" + jsonPage.foreach(page => page.toString))
    }
    case e: JsError => {
      Logger.info("couldn't extract page from json")
    }
  }

*/


}
