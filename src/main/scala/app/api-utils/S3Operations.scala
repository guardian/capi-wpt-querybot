package app.api

import java.io._

import app.apiutils._
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.gu.contentapi.client.model.v1.CapiDateTime
import com.typesafe.config.{ConfigFactory, Config}
import scala.collection.JavaConversions._
import org.joda.time.DateTime

import scala.io.Source


/**
 * Created by mmcnamara on 09/02/16.
 */
class S3Operations(s3BucketName: String, configFile: String, emailFile: String) {
  val s3Client: AmazonS3Client = new AmazonS3Client()
  val bucket: String = s3BucketName
  val configFileName = configFile
  val emailFileName = emailFile


  def doesFileExist(fileKeyName: String): Boolean = {
    try {
      s3Client.getObjectMetadata(bucket, fileKeyName); true
    } catch {
      case ex: Exception => println("File: " + fileKeyName + " was not present \n"); false
    }
  }

  def getConfig: (String, String, String, String, String, String, String, List[String]) = {
    println(DateTime.now + " retrieving config from S3 bucket: " + bucket)

    println("Obtaining configfile: " + configFileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, configFileName))
    val objectData = s3Object.getObjectContent

    println("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    println("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)

    println("returning config object")
    val contentApiKey: String = conf.getString("content.api.key")
    val wptBaseUrl: String = conf.getString("wpt.api.baseUrl")
    val wptApiKey: String = conf.getString("wpt.api.key")
    val wptLocation = conf.getString("wpt.location")
    val emailUsername = conf.getString("email.username")
    val emailPassword = conf.getString("email.password")
    val visualsFeedUrl = conf.getString("visuals.page.list")
    val pageFragments: List[String] = conf.getStringList("page.fragments").toList
    if ((contentApiKey.length > 0) && (wptBaseUrl.length > 0) && (wptApiKey.length > 0) && (wptLocation.length > 0) && (emailUsername.length > 0) && (emailPassword.length > 0) && (visualsFeedUrl.length > 0)){
      println(DateTime.now + " Config retrieval successful. \n You are using the following webpagetest instance: " + wptBaseUrl)
      (contentApiKey, wptBaseUrl, wptApiKey, wptLocation, emailUsername, emailPassword, visualsFeedUrl, pageFragments)
    }
    else {
      println(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      ("", "", "", "", "", "", "", List())
    }

  }

  def getEmailAddresses: Array[List[String]] = {
    println(DateTime.now + " retrieving email file from S3 bucket: " + bucket)

    println("Obtaining list of emails: " + emailFileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, emailFileName))
    val objectData = s3Object.getObjectContent

    println("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    println("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)
    println("conf: \n" + conf)

    println("returning config object")
    val generalAlerts = conf.getStringList("general.alerts").toList
    val interactiveAlerts = conf.getStringList("interactive.alerts").toList
    if (generalAlerts.nonEmpty || interactiveAlerts.nonEmpty){
      println(DateTime.now + " Config retrieval successful. \n You have retrieved the following users\n" +
        generalAlerts + "\n" +
        interactiveAlerts + "\n")
      val returnArray = Array(generalAlerts, interactiveAlerts)
      returnArray
    }
    else {
      println(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      Array()
    }

  }

  def getUrls(fileName: String): List[String] = {
    println(DateTime.now + " retrieving url file from S3 bucket: " + bucket)

    println("Obtaining list of urls: " + fileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, fileName))
    val objectData = s3Object.getObjectContent

    println("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    println("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)

    println("returning config object")
    val interactives = conf.getStringList("sample.large.interactives").toList
    if (interactives.nonEmpty){
      println(DateTime.now + " Config retrieval successful. \n You have retrieved the following users\n" + interactives)
      interactives
    }
    else {
      println(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      val emptyList: List[String] = List()
      emptyList
    }

  }

/*  def getliveBlogList(fileName: String): List[String] = {
    println(DateTime.now + " retrieving url file from S3 bucket: " + bucket)

    println("Obtaining list of urls: " + fileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, fileName))
    val objectData = s3Object.getObjectContent

    println("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    println("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)
    println("conf: \n" + conf)

    println("returning config object")
    val interactives = conf.getStringList("sample.large.interactives").toList
    if (interactives.nonEmpty){
      println(DateTime.now + " Config retrieval successful. \n You have retrieved the following users\n" + interactives)
      interactives
    }
    else {
      println(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      List()
    }

  }*/

  def getResultsFileFromS3(fileName:String): List[PerformanceResultsObject] = {
// todo - update to include new fields
    if (doesFileExist(fileName)) {
      val s3Response = s3Client.getObject(new GetObjectRequest(s3BucketName, fileName))
      val objectData = s3Response.getObjectContent
      val myData = scala.io.Source.fromInputStream(objectData).getLines()
      val resultsIterator = for (line <- myData) yield {
        val data: Array[String] = line.split(",")
        var result = new PerformanceResultsObject(data(1),
          data(7),
          data(8),
          data(9).toInt,
          data(10).toInt,
          data(11).toInt,
          data(12).toInt,
          data(13).toInt,
          data(14).toInt,
          data(15).toInt,
          data(16),
          data(17).toBoolean,
          data(18).toBoolean,
          data(19).toBoolean)
        //todo - get element list
        val elementArray = data.drop(20)
        println("**** Element Array pulled from file \n" + "length = " + elementArray.length)
        if ((elementArray(2).toInt > 0) && (data(9).toInt > -1)){
        println("calling getElementListFromArray")
        val elementList = getElementListFromArray(elementArray)
        if(elementList.nonEmpty) {
            {
              println("calling populateEditorialElementsList.")
//             println("calling populateEditorialElementsList. \n elementList to be passed is:\n" + elementList.map(element => element.toHTMLRowString() + "\n"))
            result.populateEditorialElementList(elementList)
            }
          }
        }
        result.setHeadline(Option(data(2)))
        result.setPageType(data(3))
        val firstPublishedTime: Option[CapiDateTime] = result.stringtoCAPITime(data(4))
        result.setPageLastUpdated(firstPublishedTime)
        val lastUpdateTime: Option[CapiDateTime] = result.stringtoCAPITime(data(5))
        result.setPageLastUpdated(lastUpdateTime)
        result.setLiveBloggingNow(data(6))
        result
      }
      resultsIterator.toList
    } else {
      val emptyList: List[PerformanceResultsObject] = List()
      emptyList
    }
  }

  def getElementListFromArray(elementArray: Array[String]): List[PageElementFromHTMLTableRow] = {
   // println("\n\n\n\n  **************** elementArray: **************** \n"+ elementArray.map(element => element.toString + "\n").mkString + "\n\n\n\n\n\n" )
    if(elementArray.nonEmpty){
      val length = elementArray.length
      println("element Array length = " + elementArray.length)
      var index = 0
      var elementList: List[PageElementFromHTMLTableRow] = List()
      while(index < length-1){
        println("index = " + index)
        println("building element")
 /*       println("\n\n\n\n******* building element \n " +
          "Index" + elementArray(index) + "\n" +
          "Index +1" + elementArray(index+1) + "\n" +
          "Index +2 " + elementArray(index+2) + "\n" +
          "Index +3 " + elementArray(index+3) + "\n" +
          "Index +4 " + elementArray(index+4) + "\n" +
          "Index +5 " + elementArray(index+5) + "\n" +
          "Index +6 " + elementArray(index+6) + "\n" +
          "Index +7 " + elementArray(index+7) + "\n" +
          "Index +8 " + elementArray(index+8) + "\n" +
          "Index +9 " + elementArray(index+9) + "\n" +
          "Index +10 " + elementArray(index+10) + "\n" +
          "\n ********* \n\n\n\n")*/
        if((elementArray(index+2).length > 8) || elementArray(index+2).matches("-")){
          val newElement: PageElementFromHTMLTableRow = new PageElementFromParameters(elementArray(index) + elementArray(index + 1),
          elementArray(index + 2),
          elementArray(index + 3).toInt,
          elementArray(index + 4).toInt,
          elementArray(index + 5).toInt,
          elementArray(index + 6).toInt,
          elementArray(index + 7).toInt,
          elementArray(index + 8).toInt,
          elementArray(index + 9).toInt,
          elementArray(index + 10).toInt,
          elementArray(index + 11)).convertToPageElementFromHTMLTableRow()
          elementList = elementList ::: List(newElement)
          index = index + 12
        }else {
          val newElement: PageElementFromHTMLTableRow = new PageElementFromParameters(elementArray(index),
            elementArray(index + 1),
            elementArray(index + 2).toInt,
            elementArray(index + 3).toInt,
            elementArray(index + 4).toInt,
            elementArray(index + 5).toInt,
            elementArray(index + 6).toInt,
            elementArray(index + 7).toInt,
            elementArray(index + 8).toInt,
            elementArray(index + 9).toInt,
            elementArray(index + 10)).convertToPageElementFromHTMLTableRow()
          elementList = elementList ::: List(newElement)
          index = index + 11
        }
      }
      println("\n\nElementList Populated. \nLength of list: " + elementList.length)
      //println("Element List Contents: \n" + elementList.map(element => element.toCSVString() + "\n"))
    elementList
  }else{
      println("No elements found - returning empty list \n this case should never be reached as it breaks things")
      val emptyList: List[PageElementFromHTMLTableRow] = List()
      emptyList
    }
  }

  def getCSVFileFromS3(fileName: String): List[String] = {
    if (doesFileExist(fileName)) {
      val s3Response = s3Client.getObject(new GetObjectRequest(s3BucketName, fileName))
      val objectData = s3Response.getObjectContent
      val myData = scala.io.Source.fromInputStream(objectData).getLines()
      val resultsIterator = for (line <- myData) yield {
      line
      }
      resultsIterator.toList
    }else{
      val emptyList: List[String] = List()
      emptyList
    }
  }

  def getVisualsFileFromS3(fileName:String): String = {
    if (doesFileExist(fileName)) {
      val s3Response = s3Client.getObject(new GetObjectRequest(s3BucketName, fileName))
      val objectData = s3Response.getObjectContent
      val myData = scala.io.Source.fromInputStream(objectData).getLines().toString()
      myData
    } else {
        ""
      }
    }

  def writeFileToS3(fileName:String, outputString: String): Unit ={
    println(DateTime.now + " Writing the following to S3:\n" + outputString + "\n")
    s3Client.putObject(new PutObjectRequest(s3BucketName, fileName, createOutputFile(fileName, outputString)))
    val acl: AccessControlList = s3Client.getObjectAcl(bucket, fileName)
    acl.grantPermission(GroupGrantee.AllUsers, Permission.Read)
    s3Client.setObjectAcl(bucket, fileName, acl)

  }

  def createOutputFile(fileName: String, content: String): File = {
    println("creating output file")
    val file: File = File.createTempFile(fileName.takeWhile(_ != '.'), fileName.dropWhile(_ != '.'))
    file.deleteOnExit()
    val writer: Writer = new OutputStreamWriter(new FileOutputStream(file))
    writer.write(content)
    writer.close()
    println("returning File object")
    file
  }



  def closeS3Client(): Unit = s3Client.shutdown()

}
