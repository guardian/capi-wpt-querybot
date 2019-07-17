//import app.api.{ListSorter, S3Operations}
//import app.apiutils.{ResultsFromPreviousTests, LocalFileOperations, PerformanceResultsObject}
//import com.gu.contentapi.client.model.v1.CapiDateTime
//import org.joda.time.DateTime
//import org.scalatest._
//
///**
// * Created by mmcnamara on 26/07/16.
// */
//
//  abstract class SorterTestsUnitSpec extends FlatSpec with Matchers with
//  OptionValues with Inside with Inspectors
//
//  class SorterTests extends SorterTestsUnitSpec with Matchers {
//    val currentTime = DateTime.now
//    val time1HourAgo = DateTime.now().minusHours(1)
//    val time24HoursAgo = DateTime.now().minusHours(24)
//    val timeOld = DateTime.now().minusHours(24).minusSeconds(1)
//
//    val capiTimeNow = {
//      val iso8061String = currentTime.toLocalDateTime.toString
//      CapiDateTime.apply(currentTime.getMillis, iso8061String)
//    }
//    {
//      val iso8061String = time1HourAgo.toLocalDateTime.toString
//      CapiDateTime.apply(time1HourAgo.getMillis, iso8061String)
//    }
//
//    val capiTime24HoursAgo = {
//      val iso8061String = time24HoursAgo.toLocalDateTime.toString
//      CapiDateTime.apply(time24HoursAgo.getMillis, iso8061String)
//    }
//
//    val capiTimeOld = {
//      val iso8061String = timeOld.toLocalDateTime.toString
//      CapiDateTime.apply(timeOld.getMillis, iso8061String)
//    }
//
//    val emptyPerfResults: List[PerformanceResultsObject] = List()
//    val iamTestingLocally = false
//    /*#####################################################################################*/
//    println("Job started at: " + DateTime.now)
//    println("Local Testing Flag is set to: " + iamTestingLocally.toString)
//
//    val jobStart = DateTime.now
//    //  Define names of s3bucket, configuration and output Files
//    val amazonDomain = "https://s3-eu-west-1.amazonaws.com"
//    val s3BucketName = "capi-wpt-querybot"
//    val configFileName = "config.conf"
//    val emailFileName = "addresses.conf"
//    val interactiveSampleFileName = "interactivesamples.conf"
//    val visualsPagesFileName = "visuals.conf"
//
//    val resultsFromPreviousTests = "resultsFromPreviousTests.csv"
//    val resultsFromSorterTest1 = "resultsFromPreviousTestsSorter1.csv"
//    val resultsFromSorterTest2 = "resultsFromPreviousTestsSorter2.csv"
//    val reallyBigtest = "resultsFromPreviousTests_really-big.csv"
//
//
//    //Create new S3 Client
//    val s3Interface = new S3Operations(s3BucketName, configFileName, emailFileName)
//    var configArray: Array[String] = Array("", "", "", "", "", "")
//    var urlFragments: List[String] = List()
//
//    //Get config settings
//    println("Extracting configuration values")
//    if (!iamTestingLocally) {
//      println(DateTime.now + " retrieving config from S3 bucket: " + s3BucketName)
//      val returnTuple = s3Interface.getConfig
//      configArray = Array(returnTuple._1, returnTuple._2, returnTuple._3, returnTuple._4, returnTuple._5, returnTuple._6, returnTuple._7)
//      urlFragments = returnTuple._8
//    }
//    else {
//      println(DateTime.now + " retrieving local config file: " + configFileName)
//      val configReader = new LocalFileOperations
//      configArray = configReader.readInConfig(configFileName)
//    }
//    println("checking validity of config values")
//    if ((configArray(0).length < 1) || (configArray(1).length < 1) || (configArray(2).length < 1) || (configArray(3).length < 1)) {
//      println("problem extracting config\n" +
//        "contentApiKey length: " + configArray(0).length + "\n" +
//        "wptBaseUrl length: " + configArray(1).length + "\n" +
//        "wptApiKey length: " + configArray(2).length + "\n" +
//        "wptLocation length: " + configArray(3).length + "\n" +
//        "emailUsername length: " + configArray(4).length + "\n" +
//        "emailPassword length: " + configArray(5).length) + "\n" +
//        "visuals URL length: " + configArray(6).length
//
//      System exit 1
//    }
//    println("config values ok")
//    val contentApiKey: String = configArray(0)
//    val wptBaseUrl: String = configArray(1)
//    val wptApiKey: String = configArray(2)
//    val wptLocation: String = configArray(3)
//    val emailUsername: String = configArray(4)
//    val emailPassword: String = configArray(5)
//    val visualsApiUrl: String = configArray(6)
//
//    //obtain list of items previously alerted on
//    val previousResults: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(reallyBigtest)
//    val previousResultsHandler = new ResultsFromPreviousTests(previousResults)
//    val sorter = new ListSorter
//
//    "OrderedByDateLastPublished called on a list of results " should "return a list ordered by dateLastPublished" in {
//      println("sorting by weight")
//      //val reorderByWeight = sorter.orderListByWeight(previousResults)
//      val reorderByWeight = sorter.orderListByWeight(previousResults)
//      val resultsHandler = new ResultsFromPreviousTests(reorderByWeight)
//      if (reorderByWeight.nonEmpty) {
//        println("sorting by date")
//        val reorderedList = sorter.orderListByDatePublished(resultsHandler.previousResults)
//        println("First Record pageLastUpdated: " + previousResults.head.pageLastUpdated.isEmpty)
//        println("First Record first Published: " + previousResults.head.firstPublished.isEmpty)
//        s3Interface.writeFileToS3(resultsFromSorterTest1, reorderedList.map(_.toCSVString()).mkString)
//        assert((reorderedList.head.timeLastLaunchedAsLong() >= reorderedList.tail.tail.head.timeLastLaunchedAsLong()) && (reorderedList.head.timeLastLaunchedAsLong() >= reorderedList.last.timeLastLaunchedAsLong()) && reorderedList.length == resultsHandler.previousResults.length)
//      } else {
//        println("reorder by weight lost all results")
//          assert(reorderByWeight.length > 1)
//        }
//    }
//
//    "OrderedByDateLastPublished called on a list of results a second time " should "return a list ordered by dateLastPublished" in {
//      val resultsFromLastTest: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromSorterTest1)
//      val reorderByWeight = sorter.orderListByWeight(resultsFromLastTest)
//      val reorderedList = sorter.orderListByDatePublished(reorderByWeight)
//      s3Interface.writeFileToS3(resultsFromSorterTest2, reorderedList.map(_.toCSVString()).mkString)
//      assert((reorderedList.head.timeLastLaunchedAsLong() > reorderedList.tail.tail.head.timeLastLaunchedAsLong()) && (reorderedList.head.timeLastLaunchedAsLong() > reorderedList.last.timeLastLaunchedAsLong()) && reorderedList.length == resultsFromLastTest.length)
//    }
//
//    "OrderedByDateLastPublished called on a list of results that is 24 hour old" should "return a list ordered by dateLastPublished" in {
//      val resultsFromLastTest: List[PerformanceResultsObject] = s3Interface.getResultsFileFromS3(resultsFromSorterTest1)
//      val resultsHandler = new ResultsFromPreviousTests(resultsFromLastTest)
//      val reorderedList = sorter.orderListByDatePublished(resultsHandler.oldResults)
//      s3Interface.writeFileToS3(resultsFromSorterTest2, reorderedList.map(_.toCSVString()).mkString)
//      assert((reorderedList.head.timeLastLaunchedAsLong() > reorderedList.tail.tail.head.timeLastLaunchedAsLong()) && (reorderedList.head.timeLastLaunchedAsLong() > reorderedList.last.timeLastLaunchedAsLong()) && reorderedList.length == resultsHandler.oldResults.length)
//    }
//
//
//    "OrderedByPageWeight called on a list of results " should "return a list ordered by pageWeight" in {
//      val reorderedList = sorter.orderListByWeight(previousResultsHandler.previousResults)
//      assert((reorderedList.head.bytesInFullyLoaded >= reorderedList.tail.head.bytesInFullyLoaded) && (reorderedList.head.bytesInFullyLoaded >= reorderedList.last.bytesInFullyLoaded) && reorderedList.length == previousResultsHandler.previousResults.length)
//    }
//
//    "OrderedByPageSpeed called on a list of results " should "return a list ordered by pageWeight" in {
//      val reorderedList = sorter.orderListBySpeed(previousResultsHandler.previousResults)
//      assert((reorderedList.head.bytesInFullyLoaded >= reorderedList.tail.head.bytesInFullyLoaded) && (reorderedList.head.bytesInFullyLoaded >= reorderedList.last.bytesInFullyLoaded) && reorderedList.length == previousResultsHandler.previousResults.length)
//    }
//
//    "OrderedInteractivesByPageSpeed called on a list of results " should "return a list ordered by pageWeight" in {
//      val reorderedList = sorter.orderInteractivesBySpeed(previousResultsHandler.previousResults)
//      assert((reorderedList.head.bytesInFullyLoaded >= reorderedList.tail.head.bytesInFullyLoaded) && (reorderedList.head.bytesInFullyLoaded >= reorderedList.last.bytesInFullyLoaded) && reorderedList.length == previousResultsHandler.previousResults.length)
//    }
//
//
//    /*
//    "OrderByDatePublished called on large list" should "return same number of records" in {
//      val sizeOfResults = previousResults.length
//      val sortedList = sorter.orderListByDatePublished2(previousResults)
//      val sizeOfSortedList = sortedList.length
//      sizeOfSortedList shouldEqual sizeOfResults
//    }*/
//  }