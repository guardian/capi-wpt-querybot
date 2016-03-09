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

  def getUrlsForContentType(contentType: String): List[String] = {
     contentType match {
      case("Article") => getArticleUrls
      case ("LiveBlog") =>  getLiveBlogUrls
      case ("Interactive") => getInteractiveUrls
      case ("Video") => getVideoUrls
      case ("Audio") => getAudioUrls
      case("Front") => getFrontsUrls
      case (_) => {
        val empty: List[String] = List()
        empty
      }
    }
  }

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
      .pageSize(20)
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
      .pageSize(20)
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
      .pageSize(20)
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

  def getVideoUrls: List[String] = {
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
      .pageSize(20)
      .orderBy("oldest")
      .contentType("video")
    println("Sending query to CAPI: \n" + liveBlogSearchQuery.toString)

    val apiResponse = contentApiClient.getResponse(liveBlogSearchQuery)

    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    println("CAPI has returned a response")
    val liveBlogUrlString: List[String] = for (result <- returnedResponse.results) yield {
      println("minBymin result: " + result.webUrl)
      result.webUrl }
    liveBlogUrlString
  }

  def getAudioUrls: List[String] = {
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
      .pageSize(20)
      .orderBy("oldest")
      .contentType("audio")
    println("Sending query to CAPI: \n" + liveBlogSearchQuery.toString)

    val apiResponse = contentApiClient.getResponse(liveBlogSearchQuery)

    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    println("CAPI has returned a response")
    val liveBlogUrlString: List[String] = for (result <- returnedResponse.results) yield {
      println("minBymin result: " + result.webUrl)
      result.webUrl }
    liveBlogUrlString
  }


  def getArticleUrls: List[String] = {
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
      .pageSize(20)
      .orderBy("oldest")
      .contentType("article")
    println("Sending query to CAPI: \n" + liveBlogSearchQuery.toString)

    val apiResponse = contentApiClient.getResponse(liveBlogSearchQuery)

    val returnedResponse = Await.result(apiResponse, (20, SECONDS))
    println("CAPI has returned a response")
    val liveBlogUrlString: List[String] = for (result <- returnedResponse.results) yield {
      println("minBymin result: " + result.webUrl)
      result.webUrl }
    liveBlogUrlString
  }

  def getFrontsUrls: List[String] = {
    val listofFronts: List[String] = List("http://www.theguardian.com/uk",
      "http://www.theguardian.com/us",
      "http://www.theguardian.com/au",
      "http://www.theguardian.com/uk-news",
      "http://www.theguardian.com/world",
      "http://www.theguardian.com/politics",
      "http://www.theguardian.com/uk/sport",
      "http://www.theguardian.com/football",
      "http://www.theguardian.com/uk/commentisfree",
      "http://www.theguardian.com/uk/culture",
      "http://www.theguardian.com/uk/business",
      "http://www.theguardian.com/uk/lifeandstyle",
      "http://www.theguardian.com/fashion",
      "http://www.theguardian.com/uk/environment",
      "http://www.theguardian.com/uk/technology",
      "http://www.theguardian.com/travel")
    listofFronts
  }

}

