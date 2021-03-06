package services.api

import java.io._

import services.apiutils._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model._
import com.gu.contentapi.client.model.v1.CapiDateTime
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._
import org.joda.time.DateTime
import play.api.Logger

import scala.io.Source


/**
 * Created by mmcnamara on 09/02/16.
 */
class S3Operations(s3BucketName: String, configFile: String, emailFile: String) {
  val credentialsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("frontend"),
    InstanceProfileCredentialsProvider.getInstance())
  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(credentialsProvider).build()


  val bucket: String = s3BucketName
  val configFileName: String = configFile
  val emailFileName: String = emailFile


  def doesFileExist(fileKeyName: String): Boolean = {
    try {
      s3Client.getObjectMetadata(bucket, fileKeyName); true
    } catch {
      case ex: Exception => Logger.info("File: " + fileKeyName + " was not present \n"); false
    }
  }

  def getConfig: (String, String, String, String, String, String, String, List[String]) = {
    Logger.info(DateTime.now + " retrieving config from S3 bucket: " + bucket)

    Logger.info("Obtaining configfile: " + configFileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, configFileName))
    val objectData = s3Object.getObjectContent

    Logger.info("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    Logger.info("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)

    Logger.info("returning config object")
    val contentApiKey: String = conf.getString("content.api.key")
    val wptBaseUrl: String = conf.getString("wpt.api.baseUrl")
    val wptApiKey: String = conf.getString("wpt.api.key")
    val wptLocation = conf.getString("wpt.location")
    val emailUsername = conf.getString("email.username")
    val emailPassword = conf.getString("email.password")
    val visualsFeedUrl = conf.getString("visuals.page.list")
    val pageFragments: List[String] = conf.getStringList("page.fragments").toList
    if ((contentApiKey.length > 0) && (wptBaseUrl.length > 0) && (wptApiKey.length > 0) && (wptLocation.length > 0) && (emailUsername.length > 0) && (emailPassword.length > 0) && (visualsFeedUrl.length > 0)){
      Logger.info(DateTime.now + " Config retrieval successful. \n You are using the following webpagetest instance: " + wptBaseUrl)
      (contentApiKey, wptBaseUrl, wptApiKey, wptLocation, emailUsername, emailPassword, visualsFeedUrl, pageFragments)
    }
    else {
      Logger.info(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      ("", "", "", "", "", "", "", List())
    }

  }

  def getEmailAddresses: Array[List[String]] = {
    Logger.info(DateTime.now + " retrieving email file from S3 bucket: " + bucket)

    Logger.info("Obtaining list of emails: " + emailFileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, emailFileName))
    val objectData = s3Object.getObjectContent

    Logger.info("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    Logger.info("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)
    //Logger.info("conf: \n" + conf)

    Logger.info("returning config object")
    val generalAlerts = conf.getStringList("general.alerts").toList
    val ukInteractiveAlerts = conf.getStringList("uk.interactive.alerts").toList
    val usInteractiveAlerts = conf.getStringList("us.interactive.alerts").toList
    val auInteractiveAlerts = conf.getStringList("au.interactive.alerts").toList
    val globalGLabsAlerts = conf.getStringList("global.glabs.alerts").toList
    val ukGLabsAlerts = conf.getStringList("uk.glabs.alerts").toList
    val usGLabsAlerts = conf.getStringList("us.glabs.alerts").toList
    val auGLabsAlerts = conf.getStringList("au.glabs.alerts").toList
    if (generalAlerts.nonEmpty || ukInteractiveAlerts.nonEmpty || usInteractiveAlerts.nonEmpty || auInteractiveAlerts.nonEmpty || globalGLabsAlerts.nonEmpty || ukGLabsAlerts.nonEmpty || usGLabsAlerts.nonEmpty || auGLabsAlerts.nonEmpty){
      Logger.info(DateTime.now + " Config retrieval successful. \n You have retrieved the following users\n" +
        generalAlerts + "\n" +
        ukInteractiveAlerts + "\n" +
        usInteractiveAlerts + "\n" +
        auInteractiveAlerts + "\n" +
        globalGLabsAlerts + "\n" +
        ukGLabsAlerts + "\n" +
        usGLabsAlerts + "\n" +
        auGLabsAlerts)
      val returnArray = Array(generalAlerts, ukInteractiveAlerts, usInteractiveAlerts, auInteractiveAlerts, globalGLabsAlerts, ukGLabsAlerts, usGLabsAlerts, auGLabsAlerts)
      returnArray
    }
    else {
      Logger.info(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      Array()
    }

  }

  def getUrls(fileName: String): List[String] = {
    Logger.info(DateTime.now + " retrieving url file from S3 bucket: " + bucket)

    Logger.info("Obtaining list of urls: " + fileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, fileName))
    val objectData = s3Object.getObjectContent

    Logger.info("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    Logger.info("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)

    Logger.info("returning config object")
    val interactives = conf.getStringList("sample.large.interactives").toList
    if (interactives.nonEmpty){
      Logger.info(DateTime.now + " Config retrieval successful. \n You have retrieved the following users\n" + interactives)
      interactives
    }
    else {
      Logger.info(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      val emptyList: List[String] = List()
      emptyList
    }

  }

/*  def getliveBlogList(fileName: String): List[String] = {
    Logger.info(DateTime.now + " retrieving url file from S3 bucket: " + bucket)

    Logger.info("Obtaining list of urls: " + fileName + " from S3")
    val s3Object = s3Client.getObject(new GetObjectRequest(bucket, fileName))
    val objectData = s3Object.getObjectContent

    Logger.info("Converting to string")
    val configString = scala.io.Source.fromInputStream(objectData).mkString

    Logger.info("calling parseString on ConfigFactory object")
    val conf = ConfigFactory.parseString(configString)
    Logger.info("conf: \n" + conf)

    Logger.info("returning config object")
    val interactives = conf.getStringList("sample.large.interactives").toList
    if (interactives.nonEmpty){
      Logger.info(DateTime.now + " Config retrieval successful. \n You have retrieved the following users\n" + interactives)
      interactives
    }
    else {
      Logger.info(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      List()
    }

  }*/

  def jodaDateTimeToCapiDateTime(time: DateTime): CapiDateTime = {
    val iso8061String = time.toLocalDateTime.toString
    CapiDateTime.apply(time.getMillis, iso8061String)
  }

  def dateWithin24Hrs(lastUpdateTime: Option[CapiDateTime]): Boolean = {
    val cutoffTime: CapiDateTime = jodaDateTimeToCapiDateTime(DateTime.now.minusHours(24))
    if (lastUpdateTime.isEmpty)
      false
    else {
      if (lastUpdateTime.get.dateTime > cutoffTime.dateTime)
        true
      else
        false
    }
  }

  def getResultsFileFromS3(fileName:String): List[PerformanceResultsObject] = {
// todo - update to include new fields
    Logger.info(s"In S3Operations.getResultsFromS3 calling bucketName: $s3BucketName - filename: $fileName")
    if (doesFileExist(fileName)) {
      val s3Response = s3Client.getObject(new GetObjectRequest(s3BucketName, fileName))
      val objectData = s3Response.getObjectContent
      val myData = scala.io.Source.fromInputStream(objectData).getLines()
      val resultsIterator = for (line <- myData) yield {
        val data: Array[String] = line.split(",")
        var result = new PerformanceResultsObject(data(1),
          data(8),
          data(9),
          data(10).toInt,
          data(11).toInt,
          data(12).toInt,
          data(13).toInt,
          data(14).toInt,
          data(15).toInt,
          data(16).toInt,
          data(17),
          data(18).toBoolean,
          data(19).toBoolean,
          data(20).toBoolean)
        if (data.length > 22) {
        val elementArray = data.drop(23)
        //val elementArray = data.drop(21)
        //            Logger.info("elementArray: " + elementArray.map(_.toString + "\n").mkString)
        if (elementArray.length > 9) {
          if ((elementArray(9).toInt > 0) && (data(10).toInt > -1)) {
            val elementList = getElementListFromArray(elementArray)
            if (elementList.nonEmpty) {
              result.fullElementList = elementList
              result.populateEditorialElementList(elementList)
            } else {
              Logger.info("returned list from getElementListFromArray is empty")
            }
          } else {
            Logger.info("Data in element array is not valid.\n")
            Logger.info("elementArray(2).toInt gives: " + elementArray(2).toInt)
            Logger.info("data(9).toInt gives: " + data(10).toInt)
          }
        } else {
          Logger.info("no elements present for this result")
        }
        result.setHeadline(Option(data(2)))
        result.setPageType(data(3))
        val firstPublishedTime: Option[CapiDateTime] = result.stringtoCAPITime(data(4))
        result.setFirstPublished(firstPublishedTime)
        val lastUpdateTime: Option[CapiDateTime] = result.stringtoCAPITime(data(5))
        result.setPageLastUpdated(lastUpdateTime)
          if(dateWithin24Hrs(lastUpdateTime))
            result.setLiveBloggingNow(data(6))
          else
            result.setLiveBloggingNow(false)
        result.setGLabs(data(7))
        result.setProductionOffice(data(21))
        result.setCreator(data(22))
        result
      } else {
          result
        }
      }
      resultsIterator.toList
    } else {
    val emptyList: List[PerformanceResultsObject] = List()
    emptyList
    }
  }

  def getElementListFromArray(elementArray: Array[String]): List[PageElementFromHTMLTableRow] = {
    if(elementArray.nonEmpty){
      val length = elementArray.length
      var index = 0
      var elementList: List[PageElementFromHTMLTableRow] = List()
      while((index < length-1) && (!elementArray(index + 1).matches(""))){
        if((elementArray(index+2).length > 8) || elementArray(index+2).matches("-")){
          val newElement: PageElementFromHTMLTableRow = new PageElementFromParameters(elementArray(index) +
          elementArray(index + 1),
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
          val newElementFromParameters = new PageElementFromParameters(elementArray(index),
            elementArray(index + 1),
            elementArray(index + 2).toInt,
            elementArray(index + 3).toInt,
            elementArray(index + 4).toInt,
            elementArray(index + 5).toInt,
            elementArray(index + 6).toInt,
            elementArray(index + 7).toInt,
            elementArray(index + 8).toInt,
            elementArray(index + 9).toInt,
            elementArray(index + 10))
            val newElement = newElementFromParameters.convertToPageElementFromHTMLTableRow()
          elementList = elementList ::: List(newElement)
          index = index + 11
        }
      }
    elementList
  }else{
      Logger.info("No elements found - returning empty list \n this case should never be reached as it breaks things")
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
    Logger.info(DateTime.now + " Writing the following file to S3:\n" + fileName + "\n")
    s3Client.putObject(new PutObjectRequest(s3BucketName, fileName, createOutputFile(fileName, outputString)))
    val acl: AccessControlList = s3Client.getObjectAcl(bucket, fileName)
    acl.grantPermission(GroupGrantee.AllUsers, Permission.Read)
    s3Client.setObjectAcl(bucket, fileName, acl)

  }

  def createOutputFile(fileName: String, content: String): File = {
    Logger.info("creating output file")
    val file: File = File.createTempFile(fileName.takeWhile(_ != '.'), fileName.dropWhile(_ != '.'))
    file.deleteOnExit()
    val writer: Writer = new OutputStreamWriter(new FileOutputStream(file))
    writer.write(content)
    writer.close()
    Logger.info("returning File object")
    file
  }



  def closeS3Client(): Unit = s3Client.shutdown()

}
