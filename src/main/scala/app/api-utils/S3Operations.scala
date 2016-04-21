package app.api

import java.io.{FileOutputStream, OutputStreamWriter, Writer, File}

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.config.{ConfigFactory, Config}
import play.api.libs.json.JsObject
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import play.api.libs.json



/**
 * Created by mmcnamara on 09/02/16.
 */
class S3Operations(s3BucketName: String, configFile: String, emailFile: String) {
  val s3Client: AmazonS3Client = new AmazonS3Client()
  val bucket: String = s3BucketName
  val configFileName = configFile
  val emailFileName = emailFile


  def getConfig: Array[String] = {
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
    if ((contentApiKey.length > 0) && (wptBaseUrl.length > 0) && (wptApiKey.length > 0) && (wptLocation.length > 0) && (emailUsername.length > 0) && (emailPassword.length > 0) && (visualsFeedUrl.length > 0)){
      println(DateTime.now + " Config retrieval successful. \n You are using the following webpagetest instance: " + wptBaseUrl)
      val returnArray = Array(contentApiKey, wptBaseUrl, wptApiKey, wptLocation, emailUsername, emailPassword, visualsFeedUrl)
      returnArray
    }
    else {
      println(DateTime.now + " ERROR: Problem retrieving config file - one or more parameters not retrieved")
      s3Client.shutdown()
      Array()
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

  def writeJsonToS3(fileName:String, results: JsObject): Unit = {
    println(DateTime.now + " Writing jSon file to S3:\n")
    s3Client.putObject(new PutObjectRequest(s3BucketName, fileName, createJsonFile(fileName, JsObject.unapply(results)))
    val acl: AccessControlList = s3Client.getObjectAcl(bucket, fileName)
    acl.grantPermission(GroupGrantee.AllUsers, Permission.Read)
    s3Client.setObjectAcl(bucket, fileName, acl)

  }

  def createJsonFile(fileName: String, content: List[JsonNode]): File = {
    println("creating output file")
    val file: File = File.createTempFile(fileName.takeWhile(_ != '.'), fileName.dropWhile(_ != '.'))
    file.deleteOnExit()
    val writer: Writer = new OutputStreamWriter(new FileOutputStream(file))
    play.api.libs.json
    writer.close()
    println("returning File object")
    file
  }


  def closeS3Client(): Unit = s3Client.shutdown()

}
