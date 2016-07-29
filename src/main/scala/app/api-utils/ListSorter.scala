package app.apiutils




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


  def orderListByDatePublished(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    println("orderListByDatePublished called with " + list.length + "elements")
    val validatedList: (List[PerformanceResultsObject], List[PerformanceResultsObject]) = returnValidListOfPairs(list)
    println("validated list has " + validatedList._1.length + " paired items, and " + validatedList._2.length + " leftover items")
    val tupleList: List[(PerformanceResultsObject,PerformanceResultsObject)] = listSinglesToPairs(validatedList._1)
    println("tuple List returned " + tupleList.length + " tuples")
    println("listSinglesToPairs returned a list of " + tupleList.length + " pairs.")
    val sortedTuples = sortByDatePublished(tupleList)
    val recombinedSortedList = (sortedTuples ::: validatedList._2).sortWith(_.timeLastLaunchedAsLong() > _.timeLastLaunchedAsLong())
    recombinedSortedList
  }


  def orderListByWeight(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    println("orderListByWeightCalled with " + list.length + "elements")
    val validatedList = returnValidListOfPairs(list)
    println("validated list has " + validatedList._1.length + " paired items, and " + validatedList._2.length + " leftover items")
    val tupleList = listSinglesToPairs(validatedList._1)
    println("tuple List returned " + tupleList.length + " tuples")
    val leftOverAlerts = for (result <- validatedList._2 if result.alertStatusPageWeight) yield result
    val leftOverNormal = for (result <- validatedList._2 if !result.alertStatusPageWeight) yield result
    println("listSinglesToPairs returned a list of " + tupleList.length + " pairs.")
    val alertsList: List[(PerformanceResultsObject, PerformanceResultsObject)] = (for (element <- tupleList if element._1.alertStatusPageWeight || element._2.alertStatusPageWeight) yield element)
    val okList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if !element._1.alertStatusPageWeight && !element._2.alertStatusPageWeight) yield element

    sortByWeight(alertsList) ::: leftOverAlerts ::: sortByWeight(okList) ::: leftOverNormal
  }

  def orderListBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    println("orderListBySpeed called. \n It has " + list.length + " elements.")
    val validatedList = returnValidListOfPairs(list)
    println("validated list has " + validatedList._1.length + " paired items, and " + validatedList._2.length + " leftover items")
    val tupleList = listSinglesToPairs(validatedList._1)
    println("tuple List returned " + tupleList.length + " tuples")
    val leftOverAlerts = for (result <- validatedList._2 if result.alertStatusPageSpeed) yield result
    val leftOverNormal = for (result <- validatedList._2 if !result.alertStatusPageSpeed) yield result
    println("listSinglesToPairs returned a list of " + tupleList.length + " pairs.")
    val alertsList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if element._1.alertStatusPageSpeed || element._2.alertStatusPageSpeed) yield element
    val okList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if !element._1.alertStatusPageSpeed && !element._2.alertStatusPageSpeed) yield element

    sortBySpeed(alertsList) ::: leftOverAlerts ::: sortBySpeed(okList) ::: leftOverNormal
  }

  def orderInteractivesBySpeed(list: List[PerformanceResultsObject]): List[PerformanceResultsObject] = {
    println("orderInteractivesBySpeed called. \n It has " + list.length + " elements.")
    val validatedList = returnValidListOfPairs(list)
    val tupleList = listSinglesToPairs(validatedList._1)
    val leftOverAlerts = for (result <- validatedList._2 if result.alertStatusPageSpeed) yield result
    val leftOverNormal = for (result <- validatedList._2 if !result.alertStatusPageSpeed) yield result
    println("listSinglesToPairs returned a list of " + tupleList.length + " pairs.")
    val alertsList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if element._1.alertStatusPageSpeed || element._2.alertStatusPageSpeed || element._1.alertStatusPageWeight || element._2.alertStatusPageWeight) yield element
    val okList: List[(PerformanceResultsObject, PerformanceResultsObject)] = for (element <- tupleList if !element._1.alertStatusPageSpeed && !element._2.alertStatusPageSpeed && !element._1.alertStatusPageWeight && !element._2.alertStatusPageWeight) yield element

    sortBySpeed(alertsList) ::: leftOverAlerts ::: sortBySpeed(okList) ::: leftOverNormal
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

  def sortByWeight(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[PerformanceResultsObject] = {
    if(list.nonEmpty){
      val sortedTupleList = sortTupleListByWeight(list)
      makeList(sortedTupleList)
    }
    else {
      println("sortByWeight has noElements in list. Passing back empty list")
      val emptyList: List[PerformanceResultsObject] = List()
      emptyList
    }
  }

  def sortBySpeed(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[PerformanceResultsObject] = {
    if(list.nonEmpty){
      val sortedTupleList = sortTupleListBySpeed(list)
      makeList(sortedTupleList)
    }
    else {
      println("sortByWeight has noElements in list. Passing back empty list")
      val emptyList: List[PerformanceResultsObject] = List()
      emptyList
    }
  }

  def sortByDatePublished(list: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[PerformanceResultsObject] = {
    if(list.nonEmpty){
      val sortedTupleList = sortTupleListByDatePublished(list)
      makeList(sortedTupleList)
    }
    else {
      println("sortByDatePublished has noElements in list. Passing back empty list")
      val emptyList: List[PerformanceResultsObject] = List()
      emptyList
    }
  }

  def listSinglesToPairs(list: List[PerformanceResultsObject]): List[(PerformanceResultsObject, PerformanceResultsObject)] = {
    if (list.nonEmpty && list.length % 2 == 0) {
      println("list Singles to pairs has " + list.length + " perf results objects.")
      val tupleList = makeTuple(List((list.head, list.tail.head)), list.tail.tail)
      println("makeTuple called - returned a list of " + tupleList.length + "pairs")
      tupleList
    }
    else {
      if (list.isEmpty) {
        println("listSinglesToPairs passed empty list. Returning empty list of correct type")
        val returnList: List[(PerformanceResultsObject, PerformanceResultsObject)] = List()
        returnList
      } else {
        if(list.length == 1){
          println("listSinglesToPairs passed list of 1. Returning tuple of single element - will introduce a duplicate result")
          val dummyTestType = if(list.head.typeOfTest.contains("Desktop")){
            "Android/3G"
          } else {
            "Desktop"
          }
          List((list.head, new PerformanceResultsObject("Missing test of type", dummyTestType, " for the above url", -1, -1, -1, -1, -1, -1, -1, "", false, false, true)))
        } else {
          println("listSinglesToPairs has been passed an empty or odd number of elements: list has " + list.length + "elements")
          makeTuple(List((list.head, list.tail.head)), list.tail.tail)
        }
      }
    }
  }

  def makeTuple(tupleList: List[(PerformanceResultsObject, PerformanceResultsObject)], restOfList: List[PerformanceResultsObject]): List[(PerformanceResultsObject, PerformanceResultsObject)] = {
    println("maketuple function here: tuple list has: " + tupleList.length + " elements.\n" + "               and the rest of list has: " + restOfList.length + " elements remaining.")
    if (restOfList.isEmpty) {
      tupleList
    }
    else {
      if (restOfList.length < 2) {
        println("make tuple has odd number of items in list"); tupleList
      }
      else {
        makeTuple(tupleList ::: List((restOfList.head, restOfList.tail.head)), restOfList.tail.tail)
      }
    }
  }


  def makeList(tupleList: List[(PerformanceResultsObject,PerformanceResultsObject)]): List[PerformanceResultsObject] = {
    val fullList: List[PerformanceResultsObject] = tupleList.flatMap(a => List(a._1,a._2))
    fullList
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
