package config

import com.amazonaws.auth.{AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import play.api.Configuration
import services.{AwsInstanceTags, S3}

class Config(conf: Configuration) extends AwsInstanceTags {

  val stage: String = readTag("Stage") getOrElse "DEV"
  val appName: String = readTag("App") getOrElse "capi-wpt-querybot"
  val stack: String = readTag("Stack") getOrElse "frontend"
  val region: Region = services.EC2Client.region

  val awsCredentialsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("frontend"),
    new InstanceProfileCredentialsProvider(false)
  )

  val elkKinesisStream: String = conf.getOptional[String]("elk.kinesis.stream").getOrElse("")
  val elkLoggingEnabled: Boolean = conf.getOptional[Boolean]("elk.logging.enabled").getOrElse(false)

}
