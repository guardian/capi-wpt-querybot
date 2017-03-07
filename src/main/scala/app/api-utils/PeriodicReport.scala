package app.api

import app.apiutils.{PerformanceResultsObject, ResultsFromPreviousTests}
import org.joda.time.DateTime


/**
 * Created by Michael on 05/10/2016.
 */


class PeriodicReport(jobStarted: DateTime, jobFinished: DateTime, numberOfPagesFromCapi: Int, numberOfPagesTested: Int, numberOfNewPageWeightAlerts: Int, numberOfPageWeightAlertsFixed: Int, numberOfInteractiveAlerts: Int, numberOfInteractiveAlertsFixed: Int, latestResults: List[PerformanceResultsObject], previousResultsObject: ResultsFromPreviousTests, alertsResultsObject: ResultsFromPreviousTests) {

  val startOfWeek: DateTime = jobFinished.minusDays(jobFinished.dayOfWeek.get())
  val startOfDay: DateTime = jobFinished.minusHours(jobFinished.hourOfDay.get())

  val resultsForWeekSoFar = previousResultsObject.previousResults.filter(isResultWithinTimePeriod(_,startOfWeek))
  val alertsForWeekSoFar =  alertsResultsObject.previousResults.filter(isResultWithinTimePeriod(_,startOfWeek))
  val weekSoFarPrevResults = new ResultsFromPreviousTests(resultsForWeekSoFar)
  val alertsForWeekSoFarPrevResults = new ResultsFromPreviousTests(alertsForWeekSoFar)

  val resultsForDaySoFar = resultsForWeekSoFar.filter(isResultWithinTimePeriod(_,startOfDay))
  val alertsForDaySoFar =  alertsForWeekSoFar.filter(isResultWithinTimePeriod(_,startOfWeek))
  val daySoFarPrevResults = new ResultsFromPreviousTests(resultsForDaySoFar)
  val alertsForDaySoFarPrevResults = new ResultsFromPreviousTests(alertsForDaySoFar)

  val alertsForCurrentRun = alertsForDaySoFar.filter(isResultWithinTimePeriod(_,jobStarted))
  //val currentRunPrevResults = new ResultsFromPreviousTests(latestResults)
  val emptyListPerfResults: List[PerformanceResultsObject] = List()
  val currentRunPrevResults = new ResultsFromPreviousTests(emptyListPerfResults)
  val alertsForCurrentRunPreviousResults = new ResultsFromPreviousTests(alertsForCurrentRun)

  val fullSummary = new DataSummary(jobStarted, jobFinished, numberOfPagesFromCapi, numberOfPagesTested, numberOfNewPageWeightAlerts, numberOfPageWeightAlertsFixed, numberOfInteractiveAlerts, numberOfInteractiveAlertsFixed, latestResults, previousResultsObject, alertsResultsObject)
  val summaryWeekSoFar = new DataSummary(jobStarted, jobFinished, numberOfPagesFromCapi, numberOfPagesTested, numberOfNewPageWeightAlerts, numberOfPageWeightAlertsFixed, numberOfInteractiveAlerts, numberOfInteractiveAlertsFixed, latestResults, weekSoFarPrevResults, alertsResultsObject)
  val summaryDaySoFar = new DataSummary(jobStarted, jobFinished, numberOfPagesFromCapi, numberOfPagesTested, numberOfNewPageWeightAlerts, numberOfPageWeightAlertsFixed, numberOfInteractiveAlerts, numberOfInteractiveAlertsFixed, latestResults, daySoFarPrevResults, alertsResultsObject)
  val summaryCurrentRun = new DataSummary(jobStarted, jobFinished, numberOfPagesFromCapi, numberOfPagesTested, numberOfNewPageWeightAlerts, numberOfPageWeightAlertsFixed, numberOfInteractiveAlerts, numberOfInteractiveAlertsFixed, latestResults, currentRunPrevResults, alertsForCurrentRunPreviousResults)

  val fullSummaryPage = new SummaryPage(fullSummary)
  val weekSummaryPage = new SummaryPage(summaryWeekSoFar)
  val daySummaryPage = new SummaryPage(summaryDaySoFar)
  val currentRunSummaryPage = new SummaryPage(summaryCurrentRun)


  def isResultWithinTimePeriod(result: PerformanceResultsObject, cutoff: DateTime): Boolean = {
    val resultDate = DateTime.parse(result.timeOfTest)
    resultDate.isAfter(cutoff.getMillis)
  }
}
