package app.api-utils

- utils

/**
 * Created by mmcnamara on 14/07/16.
 */
class WebPageTestPerformanceSummary {

  var msmaxTime = 0
  var msTimeBetweenPings = 0
  var msmaxTimeForMultipleTests = 0
  var msTimeBetweenPingsForMultipleTests = 0

  var numberOfPagesSent: Int = 0
  var numberOfTestResultsSought: Int = 0
  var numberOfSuccessfulTests: Int = 0
  var numberOfFailedTests: Int = 0
  var numberOfTestTimeOuts: Int = 0
  var averageIteratorCount: Double = 0


  var numberOfMultipleTestRequests: Int = 0
  var totalNumberOfTestsSentByMultipleRequests: Int = 0
  var numberOfTestResultsSoughtByMultipleTests: Int = 0
  var numberOfSuccessfulTestsForMultipleTests: Int = 0
  var numberOfFailedTestsForMultipleTests: Int = 0
  var numberOfTestTimeOutsForMultipleTests: Int = 0
  var averageIteratorCountForMultipleTests: Double = 0

  def updateWithNewWPTReport(summaryArray: Array[Int]) = {
    if(msmaxTime == 0){
      msmaxTime = summaryArray(0)
    }
    if(msTimeBetweenPings == 0){
      msTimeBetweenPings = summaryArray(1)
    }
    if(msmaxTimeForMultipleTests == 0){
      msmaxTimeForMultipleTests = summaryArray(2)
    }
    if(msTimeBetweenPingsForMultipleTests == 0){
      msTimeBetweenPingsForMultipleTests = summaryArray(3)
    }
      numberOfPagesSent += summaryArray(4)
      numberOfTestResultsSought += summaryArray(5)
      numberOfSuccessfulTests += summaryArray(6)
      numberOfFailedTests += summaryArray(7)
      numberOfTestTimeOuts += summaryArray(8)
      averageIteratorCount = (averageIteratorCount * (numberOfTestResultsSought - summaryArray(5)) + summaryArray(9))/numberOfTestResultsSought
      numberOfMultipleTestRequests += summaryArray(10)
      totalNumberOfTestsSentByMultipleRequests += summaryArray(11)
      numberOfTestResultsSoughtByMultipleTests += summaryArray(12)
      numberOfSuccessfulTestsForMultipleTests += summaryArray(13)
      numberOfFailedTestsForMultipleTests += summaryArray(14)
      numberOfTestTimeOutsForMultipleTests += summaryArray(15)
      averageIteratorCountForMultipleTests = (averageIteratorCountForMultipleTests * (numberOfTestResultsSoughtByMultipleTests - summaryArray(12)) + summaryArray(16))/numberOfTestResultsSoughtByMultipleTests
  }

  def toCSVRow(): String = {
    val rowString: String = msmaxTime + "," +
      msTimeBetweenPings + "," +
      msmaxTimeForMultipleTests + "," +
      msTimeBetweenPingsForMultipleTests + "," +
      numberOfPagesSent + "," +
      numberOfTestResultsSought + "," +
      numberOfSuccessfulTests + "," +
      numberOfFailedTests + "," +
      numberOfTestTimeOuts + "," +
      averageIteratorCount + "," +
      determineAverageTimeWaitedForResult(averageIteratorCount, msTimeBetweenPings) + "," +
      numberOfMultipleTestRequests + "," +
      totalNumberOfTestsSentByMultipleRequests + "," +
      numberOfTestResultsSoughtByMultipleTests + "," +
      numberOfSuccessfulTestsForMultipleTests + "," +
      numberOfFailedTestsForMultipleTests + "," +
      numberOfTestTimeOutsForMultipleTests + "," +
      averageIteratorCountForMultipleTests + "," +
      determineAverageTimeWaitedForResult(averageIteratorCountForMultipleTests, msTimeBetweenPingsForMultipleTests) + "\n"
    rowString
  }

  def determineAverageTimeWaitedForResult(avgNumberOfPings: Double, timeBetweenPings: Int): Int ={
    (avgNumberOfPings * timeBetweenPings).toInt
  }
}
