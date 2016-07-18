package app.apiutils

import app.api.S3Operations
import com.gu.contentapi.client.model.v1.{CapiDateTime, ContentFields}
import org.joda.time.DateTime

/**
 * Created by mmcnamara on 31/05/16.
 */



class ResultsFromPreviousTests(resultsList: List[PerformanceResultsObject]) {

  //val fullResultsList = resultsList

  val cutoffTime: Long = DateTime.now.minusHours(24).getMillis
  val previousResults: List[PerformanceResultsObject] = removeDuplicates(resultsList)

  val resultsFromLast24Hours = for (result <- previousResults if result.mostRecentUpdate >= cutoffTime) yield result
  val oldResults = for (result <- previousResults if result.mostRecentUpdate < cutoffTime) yield result

  val previousResultsToRetest: List[PerformanceResultsObject] = for (result <- resultsFromLast24Hours if result.needsRetest()) yield result
  val recentButNoRetestRequired: List[PerformanceResultsObject] = for (result <- resultsFromLast24Hours if !result.needsRetest()) yield result
  val hasPreviouslyAlerted: List[PerformanceResultsObject] = for (result <- previousResultsToRetest if result.alertStatusPageWeight || result.alertStatusPageSpeed) yield result
  val hasPreviouslyAlertedOnWeight: List[PerformanceResultsObject] = previousResultsToRetest.filter(_.alertStatusPageWeight)

  val desktopPreviousResultsToReTest = for (result <- previousResultsToRetest if result.typeOfTest.contains("Desktop")) yield result
  val mobilePreviousResultsToReTest = for (result <- previousResultsToRetest if result.typeOfTest.contains("Android/3G")) yield result

  val dedupedMobilePreviousResultsToRetest = for (result <- mobilePreviousResultsToReTest if!desktopPreviousResultsToReTest.map(_.testUrl).contains(result.testUrl)) yield result
  val dedupedPreviousResultsToRestest: List[PerformanceResultsObject] = dedupedMobilePreviousResultsToRetest ::: desktopPreviousResultsToReTest

  val resultsWithNoPageElements = (recentButNoRetestRequired ::: oldResults).filter(_.editorialElementList.isEmpty)

  def returnPagesNotYetTested(list: List[(Option[ContentFields],String)]): List[(Option[ContentFields],String)] = {
    val pagesNotYetTested: List[(Option[ContentFields],String)] = for (page <- list if !previousResults.map(_.testUrl).contains(page._2)) yield page
    val pagesAlreadyTested:List[(Option[ContentFields],String)] = for (page <- list if previousResults.map(_.testUrl).contains(page._2)) yield page
    val testedPagesBothSourcesThatHaveChangedSinceLastTest = pagesAlreadyTested.flatMap(page => {
      for (result <- previousResults if result.testUrl.contains(page._2) && result.mostRecentUpdate < page._1.get.lastModified.getOrElse(new CapiDateTime {
        override def dateTime: Long = 0
      }).dateTime) yield page}).distinct
    println("pages that have been updated since last test: \n" + testedPagesBothSourcesThatHaveChangedSinceLastTest.map(_._2 + "\n").mkString)
    pagesNotYetTested ::: testedPagesBothSourcesThatHaveChangedSinceLastTest
    }

  def returnPagesNotYetAlertedOn(resultsList: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    for (result <- resultsList if !hasPreviouslyAlerted.map(_.testUrl).contains(result.testUrl)) yield result
  }

  /*def removeDuplicates(results: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val emptyResultList: List[PerformanceResultsObject] = List()
    cleanList(results, emptyResultList)
  }*/

  /*def cleanList(inputList: List[PerformanceResultsObject], expurgatedList: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
   if (inputList.isEmpty) {
     val emptyResultList: List[PerformanceResultsObject] = List()
     emptyResultList
   } else {
     if(isResultPresent(inputList.head, expurgatedList)){
       cleanList(inputList.tail, expurgatedList)
     } else {
       List(inputList.head) ::: cleanList(inputList.tail, expurgatedList ::: List(inputList.head))
     }
   }
  }*/

  def removeDuplicates(inputList: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    //val inputListArray = inputList.toArray
    val cleanList: List[PerformanceResultsObject] = for (result <- inputList if !isResultADupe(result, inputList )) yield result
    cleanList
  }

  def isResultADupe(result: PerformanceResultsObject, resultList: List[PerformanceResultsObject]): Boolean = {
    if(resultList.isEmpty){
      false
    } else {
      val resultIncidenceCount: Int = resultList.map(test => (test.testUrl, test.typeOfTest)).count(_ == (result.testUrl, result.typeOfTest))
      resultIncidenceCount > 1
    }
  }


  //query full list
  def returnAllPageWeightAlerts(): List[PerformanceResultsObject] = {
    previousResults.filter(_.alertStatusPageWeight)
  }

  def returnAllPageSpeedAlerts(): List[PerformanceResultsObject] = {
    previousResults.filter(_.alertStatusPageSpeed)
  }

  def returnPageSpeedAlertsWithinSizeLimits(): List[PerformanceResultsObject] = {
    returnAllPageSpeedAlerts().filter(!_.alertStatusPageWeight)
  }



  def checkConsistency(): Boolean = {
        if (!(((previousResultsToRetest.length + recentButNoRetestRequired.length) == resultsFromLast24Hours.length) && ((resultsFromLast24Hours.length + oldResults.length) == previousResults.length))) {
          println("ERROR: previous results list handling is borked!")
          println("Previous Results to retest length == " + previousResultsToRetest.length + "\n")
          println("Unchanged previous results length == " + recentButNoRetestRequired.length + "\n")
          println("Results from last 24 hours length == " + resultsFromLast24Hours.length + "\n")
          println("Old results length == " + oldResults.length + "\n")
          println("Original list of previous results length == " + previousResults.length + "\n")
          if (!((previousResultsToRetest.length + recentButNoRetestRequired.length) == resultsFromLast24Hours.length)) {
            println("Results to test and unchanged results from last 24 hours dont add up correctly \n")
          }
          if (!((resultsFromLast24Hours.length + oldResults.length) == previousResults.length)) {
            println("Results from last 24 hours and old results dont add up \n")
          }
          false
        } else {
          println("Retrieved results from file\n")
          println(previousResults.length + " results retrieved in total")
          println(resultsFromLast24Hours.length + " results for last 24 hours")
          println(previousResultsToRetest.length + " results will be elegible for retest")
          println(dedupedPreviousResultsToRestest + " results are not duplicates and will actually be retested")
          println(recentButNoRetestRequired.length + " results will be listed but not tested")
          true
    }
  }


  def repairPreviousResultsList(): List[PerformanceResultsObject] = {
    //Create new S3 Client
    val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
    val s3BucketName = "capi-wpt-querybot"
    val configFileName = "config.conf"
    val emailFileName = "addresses.conf"

    println("defining new S3 Client (this is done regardless but only used if 'iamTestingLocally' flag is set to false)")
    val s3Interface = new S3Operations(s3BucketName, configFileName, emailFileName)
    var configArray: Array[String] = Array("", "", "", "", "", "")
    var urlFragments: List[String] = List()

    println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
    val returnTuple = s3Interface.getConfig
    configArray = Array(returnTuple._1,returnTuple._2,returnTuple._3,returnTuple._4,returnTuple._5,returnTuple._6,returnTuple._7)
    urlFragments = returnTuple._8

    val contentApiKey: String = configArray(0)
    val wptBaseUrl: String = configArray(1)
    val wptApiKey: String = configArray(2)
    val wptLocation: String = configArray(3)



    // val resultsFromPreviousTests = "resultsFromPreviousTests.csv"
    //    val resultsFromPreviousTests = "resultsFromPreviousTestsTest.csv"
    //    val resultsFromPreviousTestsTestVersion = "resultsFromPreviousTestsTestOutput.csv"
    //   val resultsFromPreviousTests = "elementtestinput.csv"
    //   val resultsFromPreviousTestsTestVersion = "elementtestoutput.csv"

    val outputFile = "repairedPreviousResultsList.csv"
    //obtain list of items previously alerted on
    //val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromPreviousTests)
   // val previousTestResultsHandler = new ResultsFromPreviousTests(previousResults)

    val urlsToRetest = resultsWithNoPageElements.map(_.testUrl).distinct
    val containerSize: Int = if(urlsToRetest.length > 100){urlsToRetest.length / 100}else{urlsToRetest.length}
    println("urls to retest: " + urlsToRetest.length)
    println("container size: " + containerSize)
    val retestingList: List[List[String]] = if(urlsToRetest.nonEmpty){
      urlsToRetest.grouped(containerSize).toList
    }else{
      val emptyListOfLists: List[List[String]] = List()
      emptyListOfLists
    }

    val urlAndResults: List[(String, String, String)] = retestingList.flatMap(list => {
      val urlAndResultListFragment: List[(String,String, String)] = sendResultPages(list, urlFragments, wptBaseUrl, wptApiKey, wptLocation)
      if(urlsToRetest.length > 100){
        Thread.sleep(1000*60*15)
      }
      urlAndResultListFragment
    })

    val resultsWithElementListAdded: List[PerformanceResultsObject] = resultsWithNoPageElements.flatMap(result => {
      for (urlSet <- urlAndResults if urlSet._1.contains(result.testUrl)) yield {
        if(urlSet._1.contains("wpt.gu-web.net")){
          val newResult = getResult(urlSet._1, wptBaseUrl, wptApiKey, urlFragments)
          newResult.headline = result.headline
          newResult.pageType = result.pageType
          newResult.firstPublished = result.firstPublished
          newResult.pageLastUpdated = result.pageLastUpdated
          newResult.liveBloggingNow = result.liveBloggingNow
          newResult.alertStatusPageWeight = result.alertStatusPageWeight
          newResult.alertStatusPageSpeed = result.alertStatusPageSpeed
          newResult.pageWeightAlertDescription = result.pageWeightAlertDescription
          newResult.pageSpeedAlertDescription = result.pageSpeedAlertDescription
          newResult
        } else {
          if (result.typeOfTest.contains("Desktop")) {
            val newResult = getResult(urlSet._2, wptBaseUrl, wptApiKey, urlFragments)
            newResult.headline = result.headline
            newResult.pageType = result.pageType
            newResult.firstPublished = result.firstPublished
            newResult.pageLastUpdated = result.pageLastUpdated
            newResult.liveBloggingNow = result.liveBloggingNow
            newResult.alertStatusPageWeight = result.alertStatusPageWeight
            newResult.alertStatusPageSpeed = result.alertStatusPageSpeed
            newResult.pageWeightAlertDescription = result.pageWeightAlertDescription
            newResult.pageSpeedAlertDescription = result.pageSpeedAlertDescription

            println("newResult created: \n Elements in list are: \n " + newResult.editorialElementList.map(element => element.resource + "\n"))
            println("\n\n\nEd Elements to csv string:\n" + newResult.editorialElementList.map(_.toCSVString()))
            newResult
          } else {
            val newResult = getResult(urlSet._3, wptBaseUrl, wptApiKey, urlFragments)
            newResult.headline = result.headline
            newResult.pageType = result.pageType
            newResult.firstPublished = result.firstPublished
            newResult.pageLastUpdated = result.pageLastUpdated
            newResult.liveBloggingNow = result.liveBloggingNow
            newResult.alertStatusPageWeight = result.alertStatusPageWeight
            newResult.alertStatusPageSpeed = result.alertStatusPageSpeed
            newResult.pageWeightAlertDescription = result.pageWeightAlertDescription
            newResult.pageSpeedAlertDescription = result.pageSpeedAlertDescription

            println("newResult created: \n Elements in list are: \n " + newResult.editorialElementList.map(element => element.resource + "\n"))
            println("\n\n\nEd Elements to csv string:\n" + newResult.editorialElementList.map(_.toCSVString()))
            newResult

          }
        }
     }
    })

    val unchangedResults: List[PerformanceResultsObject] = for (result <- (recentButNoRetestRequired ::: oldResults) if !resultsWithElementListAdded.map(_.testUrl).contains(result.testUrl)) yield result

    val repairedResultsList: List[PerformanceResultsObject] = unchangedResults ::: resultsWithElementListAdded

    val resultsToRecordCSVString: String = repairedResultsList.map(_.toCSVString()).mkString
    s3Interface.writeFileToS3(outputFile, resultsToRecordCSVString)

    repairedResultsList
  }


  def sendResultPages(urlList: List[String], urlFragments: List[String], wptBaseUrl: String, wptApiKey: String, wptLocation: String): List[(String, String, String)] = {
    val wpt: WebPageTest = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val resultList: List[(String, String, String)] = urlList.map(url => {
        if(url.contains("wpt.gu-web.net")){
          (url, url, url)
        } else {
          val desktopResult: String = wpt.sendPage(url)
          val mobileResult: String = wpt.sendMobile3GPage(url, wptLocation)
          (url, desktopResult, mobileResult)
        }
      })
    resultList
  }

  def getResult(friendlyUrl: String, wptBaseUrl: String, wptApiKey: String, urlFragments: List[String] ): PerformanceResultsObject = {
    val xmlResultUrl = friendlyUrl.replaceAll("result","xmlResult")
    val wpt = new WebPageTest(wptBaseUrl, wptApiKey, urlFragments)
    val result: PerformanceResultsObject = wpt.getResults(xmlResultUrl)
    result
  }




}


