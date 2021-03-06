package services.apiutils

import okhttp3.{HttpUrl, OkHttpClient, Request, Response}
import org.joda.time.DateTime
import play.api.Logger

import scala.xml.Elem


/**
 * Created by mmcnamara on 14/12/15.
 */
class WebPageTest(baseUrl: String, passedKey: String, urlFragments: List[String]) {

  val apiBaseUrl:String = baseUrl
  val apihost: String = baseUrl.split("http://")(1)
  val apiKey:String = passedKey
  val fragments: String = urlFragments.map(x => "#" + x).mkString

  val wptResponseFormat:String = "xml"
  implicit val httpClient: OkHttpClient = new OkHttpClient()

  val msmaxTime: Int = 6000000
  val msTimeBetweenPings: Int = 30000
  val maxCount: Int = roundAt(0)(msmaxTime.toDouble / msTimeBetweenPings).toInt

  val msmaxTimeForMultipleTests: Int = 1200000
  val msTimeBetweenPingsForMultipleTests: Int = 5000
  val maxCountForMultipleTests: Int = roundAt(0)(msmaxTimeForMultipleTests.toDouble / msTimeBetweenPingsForMultipleTests).toInt


  var numberOfPagesSent: Int = 0
  var numberOfTestResultsSought: Int = 0
  var numberOfSuccessfulTests: Int = 0
  var numberOfFailedTests: Int = 0
  var numberOfTestTimeOuts: Int = 0
  var averageIteratorCount: Int = 0

  var numberOfMultipleTestRequests: Int = 0
  var totalNumberOfTestsSentByMultipleRequests: Int = 0
  var numberOfTestResultsSoughtByMultipleTests: Int = 0
  var numberOfSuccessfulTestsForMultipleTests: Int = 0
  var numberOfFailedTestsForMultipleTests: Int = 0
  var numberOfTestTimeOutsForMultipleTests: Int = 0
  var averageIteratorCountForMultipleTests: Int = 0

  def desktopChromeCableTest(gnmPageUrl:String, highPriority: Boolean = false): PerformanceResultsObject = {
    Logger.info("Sending desktop webpagetest request to WPT API")
    if (highPriority) {
      val resultPage: String = sendHighPriorityPage(gnmPageUrl)
      Logger.info("Accessing results at: " + resultPage)
      val testResults: PerformanceResultsObject = getResults(gnmPageUrl, resultPage)
      Logger.info("Results returned")
      testResults
    }else {
      val resultPage: String = sendPage(gnmPageUrl)
      Logger.info("Accessing results at: " + resultPage)
      val testResults: PerformanceResultsObject = getResults(gnmPageUrl, resultPage)
      Logger.info("Results returned")
      testResults
    }
  }

  def mobileChrome3GTest(gnmPageUrl:String, wptLocation: String, highPriority: Boolean = false): PerformanceResultsObject = {
    Logger.info("Sending mobile webpagetest request to WPT API")
    if(highPriority){
      val resultPage: String = sendHighPriorityMobile3GPage(gnmPageUrl, wptLocation)
      Logger.info("Accessing results at: " + resultPage)
      val testResults: PerformanceResultsObject = getResults(gnmPageUrl, resultPage)
      testResults
    }else {
      val resultPage: String = sendMobile3GPage(gnmPageUrl, wptLocation)
      Logger.info("Accessing results at: " + resultPage)
      val testResults: PerformanceResultsObject = getResults(gnmPageUrl, resultPage)
      testResults
    }
  }

  def sendPage(gnmPageUrl:String): String = {
    Logger.info("Forming desktop webpage test query")
    val getUrl: HttpUrl = new HttpUrl.Builder()
    .scheme("http")
    .host(apihost)
    .addPathSegment("/runtest.php")
    .addQueryParameter("f", wptResponseFormat)
    .addQueryParameter("k", apiKey)
    .addQueryParameter("fvonly", "1")
    .addQueryParameter("priority", "3")
    .addQueryParameter("url", gnmPageUrl + fragments)
    .build()

    val request: Request = new Request.Builder()
      .url(getUrl)
      .get()
      .build()

    Logger.info("sending request: " + request.toString)
    val response: Response = httpClient.newCall(request).execute()
    Logger.info("Response: \n" + response )
    val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
    Logger.info("response received: \n" + responseXML.text)
    val resultPage: String =  (responseXML \\ "xmlUrl").text
    Logger.info(resultPage)
    numberOfPagesSent += 1
    resultPage
  }

  def sendHighPriorityPage(gnmPageUrl:String): String = {
    Logger.info("Forming high prioirty desktop webpage test query")
    val getUrl: HttpUrl = new HttpUrl.Builder()
    .scheme("http")
    .host(apihost)
    .addPathSegment("/runtest.php")
    .addQueryParameter("f", wptResponseFormat)
    .addQueryParameter("k", apiKey)
    .addQueryParameter("fvonly", "1")
    .addQueryParameter("priority", "1")
    .addQueryParameter("url", gnmPageUrl + fragments)
    .build()

    val request: Request = new Request.Builder()
      .url(getUrl)
      .get()
      .build()

    Logger.info("sending request: " + request.toString)
    val response: Response = httpClient.newCall(request).execute()
    val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
    Logger.info("response received: \n" + responseXML.text)
    val resultPage: String =  (responseXML \\ "xmlUrl").text
    Logger.info(resultPage)
    numberOfPagesSent += 1
    resultPage
  }


  def sendMobile3GPage(gnmPageUrl:String, wptLocation: String): String = {
    Logger.info("Forming high-priority mobile 3G webpage test query")
//    val getUrl: String = apiBaseUrl + "/runtest.php?" + "&f=" + wptResponseFormat + "&k=" + apiKey + "&mobile=1&mobileDevice=Nexus5&location=" + wptLocation + ":Chrome.3G" + "&url=" + gnmPageUrl + "noads"
    val getUrl: HttpUrl = new HttpUrl.Builder()
    .scheme("http")
    .host(apihost)
    .addPathSegment("/runtest.php")
    .addQueryParameter("f", wptResponseFormat)
    .addQueryParameter("k", apiKey)
    .addQueryParameter("fvonly", "1")
    .addQueryParameter("priority", "3")
    .addQueryParameter("mobile", "1")
    .addQueryParameter("mobileDevice", "Nexus5")
    .addQueryParameter("location", wptLocation + ":Chrome.3G")
    .addQueryParameter("url", gnmPageUrl + fragments)
    .build()

    val request: Request = new Request.Builder()
      .url(getUrl)
      .get()
      .build()

    Logger.info("sending request: " + request.toString)
    val response: Response = httpClient.newCall(request).execute()
    val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
    Logger.info("response received: \n" + responseXML.text)
    val resultPage: String =  (responseXML \\ "xmlUrl").text
    numberOfPagesSent += 1
    resultPage
  }

  def sendHighPriorityMobile3GPage(gnmPageUrl:String, wptLocation: String): String = {
    Logger.info("Forming mobile 3G webpage test query")
    val getUrl: HttpUrl = new HttpUrl.Builder()
      .scheme("http")
      .host(apihost)
      .addPathSegment("/runtest.php")
      .addQueryParameter("f", wptResponseFormat)
      .addQueryParameter("k", apiKey)
      .addQueryParameter("fvonly", "1")
      .addQueryParameter("mobile", "1")
      .addQueryParameter("mobileDevice", "Nexus5")
      .addQueryParameter("location", wptLocation + ":Chrome.3G")
      .addQueryParameter("priority", "1")
      .addQueryParameter("url", gnmPageUrl + fragments)
      .build()

    val request: Request = new Request.Builder()
      .url(getUrl)
      .get()
      .build()

    Logger.info("sending request: " + request.toString)
    val response: Response = httpClient.newCall(request).execute()
    val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
    Logger.info("response received: \n" + responseXML.text)
    val resultPage: String =  (responseXML \\ "xmlUrl").text
    numberOfPagesSent += + 1
    resultPage
  }


  def getResults(pageUrl: String, resultUrl: String):PerformanceResultsObject = {
    Logger.info("Requesting result url:" + resultUrl)
    numberOfTestResultsSought += 1
    val request: Request = new Request.Builder()
      .url(resultUrl)
      .get()
      .build()
    Logger.info("request is:" + request)
    var response: Response = httpClient.newCall(request).execute()
    Logger.info("Processing response and checking if results are ready")
    var testResults: Elem = scala.xml.XML.loadString(response.body.string)
    var iterator: Int = 0
    while (((testResults \\ "statusCode").text.toInt != 200) && ((testResults \\ "statusCode").text.toInt != 404) && (iterator < maxCount)) {
      Logger.info(DateTime.now + " " + (testResults \\ "statusCode").text + " statusCode response - test not ready. " + iterator + " of " + maxCount + " attempts\n")
      Thread.sleep(msTimeBetweenPings)
      iterator += 1
      response = httpClient.newCall(request).execute()
      testResults = scala.xml.XML.loadString(response.body.string)
    }
    averageIteratorCount = ((averageIteratorCount * (numberOfTestResultsSought - 1)) + iterator) / numberOfTestResultsSought
    if(iterator < maxCount) {
      if ((testResults \\ "statusCode").text.toInt == 200) {
        //Add one final request as occasionally 200 code comes before the data we want.
        Thread.sleep(3000)
        response = httpClient.newCall(request).execute()
        testResults = scala.xml.XML.loadString(response.body.string)
        if ((testResults \\ "response" \ "data" \ "successfulFVRuns").text.toInt > 0) {
          Logger.info("\n" + DateTime.now + " statusCode == 200: Page ready after " + ((iterator + 1) * msTimeBetweenPings).toDouble / 1000 + " seconds\n Refining results")
          try {
            val elementsList: List[PageElementFromHTMLTableRow] = obtainPageRequestDetails(resultUrl)
            numberOfSuccessfulTests += 1
            refineResults(pageUrl, testResults, elementsList)
          } catch {
            case _: Throwable => {
              Logger.info("Page failed for some reason")
              numberOfFailedTests += 1
              failedTestUnknown(pageUrl, testResults)
            }
          }
        } else {
          Logger.info(DateTime.now + " Test results show 0 successful runs ")
          numberOfFailedTests += 1
          failedTestNoSuccessfulRuns(pageUrl, testResults)
        }
      } else {
        if ((testResults \\ "statusCode").text.toInt == 404) {
          Logger.info(DateTime.now + " Test returned 404 error. Test ID: " + resultUrl + " is not valid")
          numberOfFailedTests += 1
          numberOfTestTimeOuts += 1
          failedTestUnknown(pageUrl, testResults)
        } else {
          Logger.info(DateTime.now + " Test failed for unknown reason. Test ID: " + resultUrl)
          numberOfFailedTests += 1
          numberOfTestTimeOuts += 1
          failedTestUnknown(pageUrl, testResults)
        }
      }
    } else {
      Logger.info(DateTime.now + " Test timed out after " + ((iterator + 1) * msTimeBetweenPings).toDouble / 1000 + " seconds")
      Logger.info("Test id is: "+ resultUrl)
      numberOfFailedTests += 1
      numberOfTestTimeOuts += 1
      failedTestTimeout(pageUrl, resultUrl)
    }
  }

  def refineResults(pageUrl: String, rawXMLResult: Elem, elementsList: List[PageElementFromHTMLTableRow]): PerformanceResultsObject = {
    Logger.info("parsing the XML results")
    numberOfTestResultsSought += 1
    try {
      val testUrl: String = (rawXMLResult \\ "response" \ "data" \ "testUrl").text.toString.split("#noads")(0)
      val testType: String = {
        if ((rawXMLResult \\ "response" \ "data" \ "from").text.toString.contains("Emulated Nexus 5")) {
          "Android/3G"
        } else {
          "Desktop"
        }
      }
      val testSummaryPage: String = (rawXMLResult \\ "response" \ "data" \ "summary").text.toString
      val timeToFirstByte: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "TTFB").text.toInt
      val firstPaint: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "firstPaint").text.toDouble.toInt
      Logger.info("firstPaint = " + firstPaint)
      val docTime: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "docTime").text.toInt
      Logger.info("docTime = " + docTime)
      val bytesInDoc: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "bytesInDoc").text.toInt
      Logger.info("bytesInDoc = " + bytesInDoc)
      val fullyLoadedTime: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "fullyLoaded").text.toInt
      Logger.info("Time to Fully loaded = " + fullyLoadedTime)
      val totalbytesIn: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "bytesIn").text.toInt
      Logger.info("Total bytes = " + totalbytesIn)
      val speedIndex: Int = (rawXMLResult \\ "response" \ "data" \ "run" \ "firstView" \ "results" \ "SpeedIndex").text.toInt
      Logger.info("SpeedIndex = " + speedIndex)
      val status: String = "Test Success"

      Logger.info("Creating PerformanceResultsObject")
      val result: PerformanceResultsObject = new PerformanceResultsObject(testUrl, testType, testSummaryPage, timeToFirstByte, firstPaint, docTime, bytesInDoc, fullyLoadedTime, totalbytesIn, speedIndex, status, false, false, false)
      Logger.info("sorting page element list")
      val sortedElementList = sortPageElementList(elementsList)
      Logger.info("populating full element list")
      result.fullElementList = sortedElementList
      Logger.info("populating editorial element list")
      Logger.info("list length: " + sortedElementList.length)
      //Logger.info("list contains: \n" + sortedElementList.map(_.toHTMLRowString() + "\n").mkString)
      result.populateEditorialElementList(sortedElementList)
      //Logger.info("Result string: " + result.toHTMLSimpleTableCells())
      //Logger.info("List of heaviest page Elements contains " + result.editorialElementList.length + " elements")
      Logger.info("Returning PerformanceResultsObject")
      result
    } catch {
      case _: Throwable => {
        Logger.info("Page failed for some reason while refining results")
        failedTestUnknown(pageUrl, rawXMLResult)
      }
    }
  }

  def testMultipleTimes(url: String, typeOfTest: String, wptLocation: String, testCount: Int): PerformanceResultsObject = {
      Logger.info("Alert registered on url: " + url + "\n" + "verify by retesting " + testCount + " times and taking median value")
      numberOfMultipleTestRequests += testCount
      if(typeOfTest.contains("Desktop")){
        Logger.info("Forming desktop webpage test query to confirm alert status")
        val getUrl: HttpUrl = new HttpUrl.Builder()
          .scheme("http")
          .host(apihost)
          .addPathSegment("/runtest.php")
          .addQueryParameter("f", wptResponseFormat)
          .addQueryParameter("k", apiKey)
          .addQueryParameter("runs", testCount.toString)
          .addQueryParameter("priority", "1")
          .addQueryParameter("url", url + fragments)
          .build()

        val request: Request = new Request.Builder()
          .url(getUrl)
          .get()
          .build()

        Logger.info("sending request: " + request.toString)
        val response: Response = httpClient.newCall(request).execute()
        val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
        val resultPage: String =  (responseXML \\ "xmlUrl").text
        Logger.info(resultPage)
        val testResultObject: PerformanceResultsObject = getMultipleResults(url, resultPage)
        testResultObject
    }
    else{
        Logger.info("Forming mobile 3G webpage test query to confirm alert status")
        val getUrl: HttpUrl = new HttpUrl.Builder()
          .scheme("http")
          .host(apihost)
          .addPathSegment("/runtest.php")
          .addQueryParameter("f", wptResponseFormat)
          .addQueryParameter("k", apiKey)
          .addQueryParameter("mobile", "1")
          .addQueryParameter("mobileDevice", "Nexus5")
          .addQueryParameter("location", wptLocation + ":Chrome.3G")
          .addQueryParameter("runs", testCount.toString)
          .addQueryParameter("priority", "1")
          .addQueryParameter("url", url + fragments)
          .build()

        val request: Request = new Request.Builder()
          .url(getUrl)
          .get()
          .build()

        Logger.info("sending request: " + request.toString)
        val response: Response = httpClient.newCall(request).execute()
        val responseXML: Elem = scala.xml.XML.loadString(response.body.string)
        val resultPage: String =  (responseXML \\ "xmlUrl").text
        Logger.info(resultPage)
        val testResultObject: PerformanceResultsObject = getMultipleResults(url, resultPage)
        testResultObject
      }
  }


  def getMultipleResults(pageUrl: String, resultUrl: String): PerformanceResultsObject = {
    numberOfTestResultsSoughtByMultipleTests += 1
    Logger.info("Requesting url:" + resultUrl)
    val request: Request = new Request.Builder()
      .url(resultUrl)
      .get()
      .build()
    var response: Response = httpClient.newCall(request).execute()
    Logger.info("Processing response and checking if results are ready")
    var testResults: Elem = scala.xml.XML.loadString(response.body.string)
    var iterator: Int = 0
    while (((testResults \\ "statusCode").text.toInt != 200) && (iterator < maxCountForMultipleTests)) {
      Logger.info(DateTime.now + " " + (testResults \\ "statusCode").text + " statusCode response - test not ready. " + iterator + " of " + maxCountForMultipleTests + " attempts\n")
      Thread.sleep(msTimeBetweenPingsForMultipleTests)
      iterator += 1
      response = httpClient.newCall(request).execute()
      testResults = scala.xml.XML.loadString(response.body.string)
    }
    averageIteratorCountForMultipleTests = ((averageIteratorCountForMultipleTests * (numberOfTestResultsSoughtByMultipleTests - 1)) + iterator)/numberOfTestResultsSoughtByMultipleTests
    if ((testResults \\ "statusCode").text.toInt == 200) {
      Logger.info("Add one final request as occasionally 200 code comes before the data we want.")
      Thread.sleep(15000)
      response = httpClient.newCall(request).execute()
      testResults = scala.xml.XML.loadString(response.body.string)
      if ((testResults \\ "response" \ "data" \ "successfulFVRuns").text.toInt > 0) {
        Logger.info("\n" + DateTime.now + " statusCode == 200: Page ready after " + roundAt(0)(((iterator + 1) * msTimeBetweenPings).toDouble / 1000).toInt + " seconds\n Refining results")
        try {
          val elementsList: List[PageElementFromHTMLTableRow] = obtainPageRequestDetails(resultUrl)
          numberOfSuccessfulTestsForMultipleTests += 1
          refineMultipleResults(pageUrl, testResults, elementsList)
        } catch {
          case _: Throwable => {
            Logger.info("Page failed for some reason")
            numberOfFailedTestsForMultipleTests += 1
            failedTestUnknown(pageUrl, testResults)
          }
        }
      } else {
        Logger.info(DateTime.now + " Test results show 0 successful runs ")
        numberOfFailedTestsForMultipleTests += 1
        failedTestNoSuccessfulRuns(pageUrl, testResults)
      }
    } else {
      Logger.info(DateTime.now + " Test timed out after " + roundAt(0)(((iterator + 1) * msTimeBetweenPings) / 1000).toInt + " seconds")
      numberOfFailedTestsForMultipleTests +=1
      numberOfTestTimeOutsForMultipleTests += 2
      failedTestTimeout(pageUrl, resultUrl)
    }
  }

  def refineMultipleResults(pageUrl: String, rawXMLResult: Elem, elementsList: List[PageElementFromHTMLTableRow]): PerformanceResultsObject = {
    Logger.info("parsing the XML results")
    try {
      val testUrl: String = (rawXMLResult \\ "response" \ "data" \ "testUrl").text.toString.split("#noads")(0)
      val testType: String = if ((rawXMLResult \\ "response" \ "data" \ "from").text.toString.contains("Emulated Nexus 5")) {
        "Android/3G"
      } else {
        "Desktop"
      }
      val testSummaryPage: String = (rawXMLResult \\ "response" \ "data" \ "summary").text.toString
      val timeToFirstByte: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "TTFB").text.toInt
      val firstPaint: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "firstPaint").text.toInt
      Logger.info("firstPaint = " + firstPaint)
      val docTime: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "docTime").text.toInt
      Logger.info("docTime = " + docTime)
      val bytesInDoc: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "bytesInDoc").text.toInt
      Logger.info("bytesInDoc = " + bytesInDoc)
      val fullyLoadedTime: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "fullyLoaded").text.toInt
      Logger.info("Time to Fully loaded = " + fullyLoadedTime)
      val totalbytesIn: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "bytesIn").text.toInt
      Logger.info("Total bytes = " + totalbytesIn)
      val speedIndex: Int = (rawXMLResult \\ "response" \ "data" \ "median" \ "firstView" \ "SpeedIndex").text.toInt
      Logger.info("SpeedIndex = " + speedIndex)
      val status: String = "Test Success"
      Logger.info("Creating PerformanceResultsObject")
      val result: PerformanceResultsObject = new PerformanceResultsObject(testUrl, testType, testSummaryPage, timeToFirstByte, firstPaint, docTime, bytesInDoc, fullyLoadedTime, totalbytesIn, speedIndex, status, false, false, false)
      val sortedElementList = sortPageElementList(elementsList)
      result.fullElementList = sortedElementList
      result.populateEditorialElementList(sortedElementList)
      //Logger.info("Result string: " + result.toHTMLSimpleTableCells())
      //Logger.info("List of heaviest page Elements contains " + result.editorialElementList.length + " elements")
      Logger.info("Returning PerformanceResultsObject")
      result
    } catch {
      case _: Throwable => {
        Logger.info("Page failed for some reason")
        failedTestUnknown(pageUrl, rawXMLResult)
      }
    }
  }

  def obtainPageRequestDetails(webpageTestResultUrl: String): List[PageElementFromHTMLTableRow] = {
    Logger.info("getting pageRequest Details")
    val sliceStart: Int = apiBaseUrl.length + "/xmlResult/".length
    val sliceEnd: Int = webpageTestResultUrl.length - 1
    val testId: String = webpageTestResultUrl.slice(sliceStart,sliceEnd)
    val resultDetailsPage: String =  apiBaseUrl + "/result/" + testId + "/1/details/"
    Logger.info(s"fetching resultDetailsPage $resultDetailsPage")
    val request:Request  = new Request.Builder()
      .url(resultDetailsPage)
      .get()
      .build()
    val response: Response = httpClient.newCall(request).execute()
    val responseString:String = response.body.string
    val tableString: String = trimToHTMLTable(responseString)
    val pageElementList: List[PageElementFromHTMLTableRow] = generatePageElementList(tableString)
    Logger.info("List generated - contains: " + pageElementList.length + " elements.")
    pageElementList
  }


  def trimToHTMLTable(pageHTML: String): String = {
    //    val responseStringXML: Elem = scala.xml.XML.loadString(response.body.string)
    val responseStringOuterTableStart: Int = pageHTML.indexOf("<table class=\"tableDetails details center\">")
    val responseStringOuterTableEnd: Int = pageHTML.indexOf("</table>", responseStringOuterTableStart)
    val outerTableString: String = pageHTML.slice(responseStringOuterTableStart, responseStringOuterTableEnd)
    val innerTableStart: Int = outerTableString.indexOf("<tbody>")
    val innerTableEnd: Int = outerTableString.indexOf("</tbody>")
    val innerTableString: String = outerTableString.slice(innerTableStart, innerTableEnd)
    val tableDataRows: String = innerTableString.slice(innerTableString.indexOf("<tr>"), innerTableString.length)
    tableDataRows
  }

  def generatePageElementList(htmlTableRows: String): List[PageElementFromHTMLTableRow] = {
    var restOfTable: String = htmlTableRows
    var pageElementList: List[PageElementFromHTMLTableRow] = List()
    var counter: Int = 0
    while (restOfTable.nonEmpty){
      val (currentRow, rest): (String, String) = restOfTable.splitAt(restOfTable.indexOf("</tr>")+5)
      pageElementList = pageElementList :+ new PageElementFromHTMLTableRow(currentRow)
      restOfTable = rest
      counter += 1
    }
    pageElementList
  }

  def failedTestNoSuccessfulRuns(url: String, rawResults: Elem): PerformanceResultsObject = {
    val failIndicator: Int = -1
    val testType: String = if((rawResults \\ "response" \ "data" \ "from").text.toString.contains("Emulated Nexus 5")){"Android/3G"}else{"Desktop"}
    val testSummaryPage: String = (rawResults \\ "response" \ "data" \ "summary").text.toString
    val failComment: String = "No successful runs of test"
    val failElement: PerformanceResultsObject = new PerformanceResultsObject(url, testType, testSummaryPage, failIndicator, failIndicator,failIndicator,failIndicator,failIndicator,failIndicator,failIndicator, failComment, false, false, true)
    failElement
  }

  def failedTestTimeout(url: String, resultUrl: String): PerformanceResultsObject = {
    val failIndicator: Int = -1
    val testType: String = "Unknown"
    val failComment: String = "Test request timed out"
    // set warning status as result may have timed out due to very large page
    val failElement: PerformanceResultsObject = new PerformanceResultsObject(url, testType, failComment, failIndicator, failIndicator,failIndicator,failIndicator,failIndicator,failIndicator,failIndicator, resultUrl, true, true, true)
    failElement
  }

  def failedTestUnknown(url: String, rawResults: Elem): PerformanceResultsObject = {
    val failIndicator: Int = -2
    val testType: String = "Unknown"
    val failComment: String = "Test failed for unknown reason"
    // set warning status as result may have timed out due to very large page
    val failElement: PerformanceResultsObject = new PerformanceResultsObject(url, testType, failComment, failIndicator, failIndicator,failIndicator,failIndicator,failIndicator,failIndicator,failIndicator, failComment, true, true, true)
    failElement
  }

  def sortPageElementList(elementList: List[PageElementFromHTMLTableRow]):List[PageElementFromHTMLTableRow] = {
    elementList.sortWith(_.bytesDownloaded > _.bytesDownloaded)
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }



  def returnSummary(): Array[Int] = {
    val summaryArray: Array[Int] = Array (msmaxTime,
          msTimeBetweenPings,
          msmaxTimeForMultipleTests,
          msTimeBetweenPingsForMultipleTests,
          numberOfPagesSent,
          numberOfTestResultsSought,
          numberOfSuccessfulTests,
          numberOfFailedTests,
          numberOfTestTimeOuts,
          averageIteratorCount,
          numberOfMultipleTestRequests,
          totalNumberOfTestsSentByMultipleRequests,
          numberOfTestResultsSoughtByMultipleTests,
          numberOfSuccessfulTestsForMultipleTests,
          numberOfFailedTestsForMultipleTests,
          numberOfTestTimeOutsForMultipleTests,
          averageIteratorCountForMultipleTests)
      summaryArray
  }
}


//todo - add url into results so we can use it in result element - makes the whole html thing easier- get from xml?
