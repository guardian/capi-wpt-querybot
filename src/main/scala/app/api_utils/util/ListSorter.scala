package app.api_utils.util

import app.api_utils.model.PerformanceResultsObject


/**
 * Created by mmcnamara on 26/07/16.
 */
class ListSorter {

  //This is to resolve issues where there is a missing Desktop or Mobile test so the tuple sorting gets borked - it wont give a perfect sort in this case, but better than the current state of things.
  def returnValidListOfPairs(list: List[PerformanceResultsObject]): (List[PerformanceResultsObject],List[PerformanceResultsObject]) = {
    val desktopList = for (result <- list if result.typeOfTest.contains("Desktop")) yield result
    val mobileList = for (result <- list if result.typeOfTest.contains("Android/3G")) yield result
    val missingFromDesktop = for (result <- mobileList if!desktopList.map(_.testUrl).contains(result.testUrl)) yield result
    val missingFromMobile = for (result <- desktopList if!mobileList.map(_.testUrl).contains(result.testUrl)) yield result
    val validListOfPairs = for (result <- list if(!missingFromDesktop.map(_.testUrl).contains(result.testUrl)) && (!missingFromMobile.map(_.testUrl).contains(result.testUrl))) yield result
    println("list has been validated")
    (validListOfPairs, missingFromDesktop ::: missingFromMobile)
  }

  def returnTuplesAndExtras(resultList: List[PerformanceResultsObject]): (List[(PerformanceResultsObject, PerformanceResultsObject)], List[PerformanceResultsObject]) = {
    //var tupleList: List[PerformanceResultsObject] = List()
    val mobileList = resultList.filter(_.typeOfTestName.contains("Mobile"))
    val desktopList = resultList.filter(_.typeOfTestName.contains("Desktop"))
    val mobileCandidates = mobileList.filter(resultIsInList(_,desktopList))
    val desktopCandidates = desktopList.filter(resultIsInList(_,mobileList))

    val mobileLeftovers = mobileList.filter(!resultIsInList(_,desktopList))
    val desktopLeftovers = desktopList.filter(!resultIsInList(_,mobileList))
    val tupleList = desktopCandidates.map(result => (result, returnMatchingElement(result, mobileCandidates)))
    (tupleList, desktopLeftovers ::: mobileLeftovers)
  }

  def returnMatchingElement(result: PerformanceResultsObject, listOfMatchCandidates: List[PerformanceResultsObject]): PerformanceResultsObject ={
    val matchingResults = listOfMatchCandidates.filter(_.testUrl == result.testUrl).filter(_.getPageLastUpdated == result.getPageLastUpdated)
    if(matchingResults.isEmpty){
      println("Error - a valid matching pair should have been found.\n Check ListSorter -> returnValidListOfPairs")
      println("returning failed test object")
      new PerformanceResultsObject("Error in ListSorter -> returnValidListOfPairs", "Mobile", "", -1, -1, -1, -1, -1, -1, -1, "Error", false, false, false)
    }else{
      matchingResults.head
    }
  }

  def resultIsInList(result: PerformanceResultsObject, list: List[PerformanceResultsObject]): Boolean = {
    val resultIdentifiers = (result.testUrl, result.getPageLastUpdated)
    list.map(page => (page.testUrl, page.getPageLastUpdated)).contains(resultIdentifiers)
  }


  def orderListByDatePublished(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val tuplesAndExtras = returnTuplesAndExtras(list)
    val tuplesToSort = tuplesAndExtras._1
    val sortedTuples = sortTupleListByDatePublished(tuplesToSort)
    if(sortedTuples.exists(tuple => tuple._1.getPageLastUpdated == 0 || tuple._2.getPageLastUpdated == 0)) {println("No value for PageLastUpdated")}
    //val sortedExtras = tuplesAndExtras._2.sortWith(_.timeLastLaunchedAsLong() > _.timeLastLaunchedAsLong())
    val tuplesAsList = sortedTuples.flatMap(tuple => List(tuple._1, tuple._2))
    (tuplesAsList ::: tuplesAndExtras._2).sortWith(_.timeLastLaunchedAsLong() > _.timeLastLaunchedAsLong())
  }


  def orderListByWeight(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {

    val tuplesAndExtras = returnTuplesAndExtras(list)
    val alertingTuplesToSort = for (tuple <- tuplesAndExtras._1 if tuple._1.alertStatusPageWeight || tuple._2.alertStatusPageWeight) yield tuple
    val nonAlertingTuplesToSort = for (tuple <- tuplesAndExtras._1 if !tuple._1.alertStatusPageWeight && !tuple._2.alertStatusPageWeight) yield tuple

    val sortedAlertTuples = sortTupleListByWeight(alertingTuplesToSort)
    val sortedNonAlertTuples = sortTupleListByWeight(nonAlertingTuplesToSort)

    val alertTuplesAsList = sortedAlertTuples.flatMap(tuple => List(tuple._1, tuple._2))
    val nonAlertTuplesAsList = sortedNonAlertTuples.flatMap(tuple => List(tuple._1, tuple._2))

    val alertingExtras = tuplesAndExtras._2.filter(_.alertStatusPageWeight)
    val nonAlertingExtras = tuplesAndExtras._2.filter(!_.alertStatusPageWeight)

    val sortedAlertingExtras = alertingExtras.sortWith(_.bytesInFullyLoaded > _.bytesInFullyLoaded)
    val sortedNonAlertingExtras = nonAlertingExtras.sortWith(_.bytesInFullyLoaded > _.bytesInFullyLoaded)

    alertTuplesAsList ::: sortedAlertingExtras ::: nonAlertTuplesAsList ::: sortedNonAlertingExtras
  }


  def orderListBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {

    val tuplesAndExtras = returnTuplesAndExtras(list)
    val alertingTuplesToSort = for (tuple <- tuplesAndExtras._1 if tuple._1.alertStatusPageSpeed || tuple._2.alertStatusPageSpeed) yield tuple
    val nonAlertingTuplesToSort = for (tuple <- tuplesAndExtras._1 if !tuple._1.alertStatusPageSpeed && !tuple._2.alertStatusPageSpeed) yield tuple

    val sortedAlertTuples = sortTupleListBySpeed(alertingTuplesToSort)
    val sortedNonAlertTuples = sortTupleListBySpeed(nonAlertingTuplesToSort)

    val alertTuplesAsList = sortedAlertTuples.flatMap(tuple => List(tuple._1, tuple._2))
    val nonAlertTuplesAsList = sortedNonAlertTuples.flatMap(tuple => List(tuple._1, tuple._2))

    val alertingExtras = tuplesAndExtras._2.filter(_.alertStatusPageSpeed)
    val nonAlertingExtras = tuplesAndExtras._2.filter(!_.alertStatusPageSpeed)

    val sortedAlertingExtras = alertingExtras.sortWith(_.speedIndex > _.speedIndex)
    val sortedNonAlertingExtras = nonAlertingExtras.sortWith(_.speedIndex > _.speedIndex)

    alertTuplesAsList ::: sortedAlertingExtras ::: nonAlertTuplesAsList ::: sortedNonAlertingExtras

  }


  def orderInteractivesBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {

    val tuplesAndExtras = returnTuplesAndExtras(list)
    val alertingTuplesToSort = for (tuple <- tuplesAndExtras._1 if tuple._1.alertStatusPageWeight || tuple._1.alertStatusPageSpeed || tuple._2.alertStatusPageWeight || tuple._2.alertStatusPageSpeed) yield tuple
    val nonAlertingTuplesToSort = for (tuple <- tuplesAndExtras._1 if !tuple._1.alertStatusPageWeight && !tuple._1.alertStatusPageSpeed && !tuple._2.alertStatusPageWeight && !tuple._2.alertStatusPageSpeed) yield tuple

    val sortedAlertTuples = sortTupleListBySpeed(alertingTuplesToSort)
    val sortedNonAlertTuples = sortTupleListBySpeed(nonAlertingTuplesToSort)

    val alertTuplesAsList = sortedAlertTuples.flatMap(tuple => List(tuple._1, tuple._2))
    val nonAlertTuplesAsList = sortedNonAlertTuples.flatMap(tuple => List(tuple._1, tuple._2))

    val alertingExtras = tuplesAndExtras._2.filter(_.alertStatusPageSpeed)
    val nonAlertingExtras = tuplesAndExtras._2.filter(!_.alertStatusPageSpeed)

    val sortedAlertingExtras = alertingExtras.sortWith(_.speedIndex > _.speedIndex)
    val sortedNonAlertingExtras = nonAlertingExtras.sortWith(_.speedIndex > _.speedIndex)

    alertTuplesAsList ::: sortedAlertingExtras ::: nonAlertTuplesAsList ::: sortedNonAlertingExtras


  }

  def sortHomogenousResultsByWeight(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val alertsResultsList: List[PerformanceResultsObject] = for (result <- list if result.alertStatusPageWeight) yield result
    val okResultsList: List[PerformanceResultsObject] = for (result <- list if !result.alertStatusPageWeight) yield result
    val sortedAlertList: List[PerformanceResultsObject] = alertsResultsList.sortWith(_.bytesInFullyLoaded > _.bytesInFullyLoaded)
    val sortedOkList: List[PerformanceResultsObject] = okResultsList.sortWith(_.bytesInFullyLoaded > _.bytesInFullyLoaded)
    sortedAlertList ::: sortedOkList
  }

  def sortHomogenousResultsBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val alertsResultsList: List[PerformanceResultsObject] = for (result <- list if result.alertStatusPageSpeed) yield result
    val okResultsList: List[PerformanceResultsObject] = for (result <- list if !result.alertStatusPageSpeed) yield result
    val sortedAlertList: List[PerformanceResultsObject] = alertsResultsList.sortWith(_.speedIndex > _.speedIndex)
    val sortedOkList: List[PerformanceResultsObject] = okResultsList.sortWith(_.speedIndex > _.speedIndex)
    sortedAlertList ::: sortedOkList
  }

  def sortHomogenousInteractiveResultsBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    val alertsResultsList: List[PerformanceResultsObject] = for (result <- list if result.alertStatusPageSpeed || result.alertStatusPageWeight) yield result
    val okResultsList: List[PerformanceResultsObject] = for (result <- list if !result.alertStatusPageSpeed && !result.alertStatusPageWeight) yield result
    val sortedAlertList: List[PerformanceResultsObject] = alertsResultsList.sortWith(_.speedIndex > _.speedIndex)
    val sortedOkList: List[PerformanceResultsObject] = okResultsList.sortWith(_.speedIndex > _.speedIndex)
    sortedAlertList ::: sortedOkList
  }




  def sortTupleListByWeight(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[(PerformanceResultsObject,PerformanceResultsObject)] = {
    list.sortWith{(leftE:(PerformanceResultsObject, PerformanceResultsObject),rightE:(PerformanceResultsObject, PerformanceResultsObject)) =>
      leftE._1.bytesInFullyLoaded + leftE._2.bytesInFullyLoaded > rightE._1.bytesInFullyLoaded + rightE._2.bytesInFullyLoaded}
  }

  def sortTupleListBySpeed(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[(PerformanceResultsObject,PerformanceResultsObject)] = {
    list.sortWith{(leftE:(PerformanceResultsObject, PerformanceResultsObject),rightE:(PerformanceResultsObject, PerformanceResultsObject)) =>
      leftE._1.speedIndex + leftE._2.speedIndex > rightE._1.speedIndex + rightE._2.speedIndex}
  }

  def sortTupleListByDatePublished(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[(PerformanceResultsObject,PerformanceResultsObject)] = {
    list.sortWith{(leftE:(PerformanceResultsObject, PerformanceResultsObject),rightE:(PerformanceResultsObject, PerformanceResultsObject)) =>
      leftE._1.timeLastLaunchedAsCAPITime().dateTime + leftE._2.timeLastLaunchedAsCAPITime().dateTime >= rightE._1.timeLastLaunchedAsCAPITime().dateTime + rightE._2.timeLastLaunchedAsCAPITime().dateTime}
  }

}
