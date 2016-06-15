package app.apiutils

import app.api.S3Operations
import com.gu.contentapi.client.model.v1.{CapiDateTime, ContentFields}
import org.joda.time.DateTime

/**
 * Created by mmcnamara on 31/05/16.
 */



class ResultsFromPreviousTests(resultsList: List[PerformanceResultsObject]) {

  val cutoffTime: Long = DateTime.now.minusHours(24).getMillis
  val previousResults: List[PerformanceResultsObject] = resultsList

  val resultsFromLast24Hours = for (result <- previousResults if result.mostRecentUpdate >= cutoffTime) yield result
  val oldResults = for (result <- previousResults if result.mostRecentUpdate < cutoffTime) yield result

  val previousResultsToRetest: List[PerformanceResultsObject] = for (result <- resultsFromLast24Hours if result.needsRetest()) yield result
  val recentButNoRetestRequired: List[PerformanceResultsObject] = for (result <- resultsFromLast24Hours if !result.needsRetest()) yield result
  val hasPreviouslyAlerted: List[PerformanceResultsObject] = for (result <- previousResultsToRetest if result.alertStatusPageWeight || result.alertStatusPageSpeed) yield result

  val desktopPreviousResultsToReTest = for (result <- previousResultsToRetest if result.typeOfTest.contains("Desktop")) yield result
  val mobilePreviousResultsToReTest = for (result <- previousResultsToRetest if result.typeOfTest.contains("Android/3G")) yield result

  val dedupedMobilePreviousResultsToRetest = for (result <- mobilePreviousResultsToReTest if!desktopPreviousResultsToReTest.map(_.testUrl).contains(result.testUrl)) yield result
  val dedupedPreviousResultsToRestest: List[PerformanceResultsObject] = dedupedMobilePreviousResultsToRetest ::: desktopPreviousResultsToReTest

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


}


