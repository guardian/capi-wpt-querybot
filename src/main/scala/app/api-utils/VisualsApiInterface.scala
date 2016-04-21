package app.apiutils

import com.squareup.okhttp.{OkHttpClient, Response, Request}

import scala.util.parsing.json._
import scala.xml.Elem

import argonaut._, Argonaut._


/**
 * Created by mmcnamara on 30/03/16.
 */
class VisualsApiInterface(url: String) {

  val apiUrl: String = url
  implicit val httpClient = new OkHttpClient()

  def getTodaysPages(): String = {

    println("Requesting result url:" + apiUrl)
    val request = new Request.Builder()
      .url(apiUrl)
      .get()
      .build()


// val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
    val response: Response = httpClient.newCall(request).execute()

    val responseJSON = scala.util.parsing.json.JSON.parseFull(response.body().toString)
    println("response: " + responseJSON)
    responseJSON.toString
    }
}
