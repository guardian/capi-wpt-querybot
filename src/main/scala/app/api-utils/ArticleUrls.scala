package app.apiutils

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.SearchQuery
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ArticleUrls(key: String) {
  val testApi:String = key
  println("testApi = " + testApi)
  val contentApiClient = new GuardianContentClient(key)

  def getLiveBlogUrls: List[String] = {
    println("Running Capi Queries")
    val resultString: List[String] = getContentTypeLiveBlogUrls ++ getMinByMinUrls
    resultString.distinct
  }

  def shutDown = {
    println("Closing connection to Content API")
    contentApiClient.shutdown()
  }
  def getContentTypeLiveBlogUrls: List[String] = {
    println("Creating CAPI query")
    val until = DateTime.now
    val from = until.minusHours(24)

    val liveBlogSearchQuery = new SearchQuery()
      .fromDate(from)
      .toDate(until)
      .showBlocks("all")
      .showElements("all")
      .showFields("all")
      .showTags("all")
      .page(1)
      .pageSize(1)
      .orderBy("oldest")
      .contentType("liveblog")
    println("Sending query to CAPI: \n" + liveBlogSearchQuery.toString)

    val apiResponse = contentApiClient.getResponse(liveBlogSearchQuery)
    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    println("CAPI has returned response")
    val liveBlogUrlString: List[String] = for (result <- returnedResponse.results) yield {
      println("liveBlog result: " + result.webUrl)
        result.webUrl }
    liveBlogUrlString
  }

  def getMinByMinUrls: List[String] = {
    val until = DateTime.now
    val from = until.minusHours(24)

    val liveBlogSearchQuery = new SearchQuery()
      .fromDate(from)
      .toDate(until)
      .showBlocks("all")
      .showElements("all")
      .showFields("all")
      .showTags("all")
      .page(1)
      .pageSize(1)
      .orderBy("oldest")
      .tag("tone/minutebyminute")
    println("Sending query to CAPI: \n" + liveBlogSearchQuery.toString)

    val apiResponse = contentApiClient.getResponse(liveBlogSearchQuery)

    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    println("CAPI has returned a response")
    val liveBlogUrlString: List[String] = for (result <- returnedResponse.results) yield {
      println("minBymin result: " + result.webUrl)
      result.webUrl }
    liveBlogUrlString
  }


  def getInteractiveUrls: List[String] = {
    val until = DateTime.now
    val from = until.minusHours(24)

    val liveBlogSearchQuery = new SearchQuery()
      .fromDate(from)
      .toDate(until)
      .showBlocks("all")
      .showElements("all")
      .showFields("all")
      .showTags("all")
      .page(1)
      .pageSize(1)
      .orderBy("oldest")
      .contentType("interactive")
    println("Sending query to CAPI: \n" + liveBlogSearchQuery.toString)

    val apiResponse = contentApiClient.getResponse(liveBlogSearchQuery)

    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    println("CAPI has returned a response")
    val liveBlogUrlString: List[String] = for (result <- returnedResponse.results) yield {
      println("minBymin result: " + result.webUrl)
      result.webUrl }
    liveBlogUrlString
  }


}

