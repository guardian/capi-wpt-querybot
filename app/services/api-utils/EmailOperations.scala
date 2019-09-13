package services.apiutils

import javax.mail.internet.InternetAddress

//import courier._, Defaults._
import scala.concurrent.Await
import scala.xml.Text

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import javax.mail._
;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by mmcnamara on 16/02/16.
 */
class EmailOperations(passedUserName: String, passedPassword: String) {


  val username: String = passedUserName
  val password: String = passedPassword

  def sendPageWeightAlert(emailAddressList: List[String], messageBody: String):Boolean = {
    val internetAddressList: List[InternetAddress] = emailAddressList.map(emailAddress => new InternetAddress(emailAddress))

    val props: Properties = new Properties
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.ssl.enable", boolean2Boolean(true))
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.port", "465")

    props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    props.setProperty("mail.smtp.socketFactory.fallback", "false")
    props.setProperty("mail.smtp.port", "465")
    props.setProperty("mail.smtp.socketFactory.port", "465")

    val session: Session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        new PasswordAuthentication(username, password)
      })
    println("session id: " + session.toString)

    try {
      val message: Message = new MimeMessage(session)
      message.setFrom(new InternetAddress(username))
      message.setRecipients(Message.RecipientType.TO,
        internetAddressList.toArray)
      message.setSubject("PageWeight Alert - The following pages have been measured as too slow or expensive for customers to load")
      message.setContent(messageBody, "text/html")
      println("message deets: \n" + message.toString + "\n from: " + message.getFrom.toString + "\n to: " + message.getAllRecipients.toString + "\n Session: " + message.getSession.toString)
      Transport.send(message, message.getAllRecipients, username, password )
      println("Success - Your Email has been sent")
      true
    }
    catch {
      case e: MessagingException => println("Message Failed: \n" + e)
        false
    }

  }

  def sendInteractiveAlert(emailAddressList: List[String], messageBody: String):Boolean = {

    val internetAddressList: List[InternetAddress] = emailAddressList.map(emailAddress => new InternetAddress(emailAddress))

    val props: Properties = new Properties
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.ssl.enable", boolean2Boolean(true))
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.port", "465")

    props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    props.setProperty("mail.smtp.socketFactory.fallback", "false")
    props.setProperty("mail.smtp.port", "465")
    props.setProperty("mail.smtp.socketFactory.port", "465")

    val session: Session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        new PasswordAuthentication(username, password)
      })
    println("session id: " + session.toString)

    try {
      if(emailAddressList.nonEmpty) {
        true
      } else {
        val message: Message = new MimeMessage(session)
        message.setFrom(new InternetAddress(username))
        message.setRecipients(Message.RecipientType.TO,
          internetAddressList.toArray)
        message.setSubject("Interactive Performance Alert - The following interactive pages have been measured as too slow or too heavy")
        message.setContent(messageBody, "text/html")
        println("message deets: \n" + message.toString + "\n from: " + message.getFrom.toString + "\n to: " + message.getAllRecipients.toString + "\n Session: " + message.getSession.toString)
        Transport.send(message, message.getAllRecipients, username, password)
        println("Success - Your Email has been sent")
        true
      }
    }
    catch {
      case e: MessagingException => println("Message Failed: \n" + e)
        false
    }

  }


  def sendPaidContentAlert(emailAddressList: List[String], messageBody: String):Boolean = {

    val internetAddressList: List[InternetAddress] = emailAddressList.map(emailAddress => new InternetAddress(emailAddress))

    val props: Properties = new Properties
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.ssl.enable", boolean2Boolean(true))
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.port", "465")

    props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    props.setProperty("mail.smtp.socketFactory.fallback", "false")
    props.setProperty("mail.smtp.port", "465")
    props.setProperty("mail.smtp.socketFactory.port", "465")

    val session: Session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        new PasswordAuthentication(username, password)
      })
    println("session id: " + session.toString)

    try {
      val message: Message = new MimeMessage(session)
      message.setFrom(new InternetAddress(username))
      message.setRecipients(Message.RecipientType.TO,
        internetAddressList.toArray)
      message.setSubject("Paid Content Performance Alert - The following paid-content pages have been measured as too slow or too heavy")
      message.setContent(messageBody, "text/html")
      println("message deets: \n" + message.toString + "\n from: " + message.getFrom.toString + "\n to: " + message.getAllRecipients.toString + "\n Session: " + message.getSession.toString)
      Transport.send(message, message.getAllRecipients, username, password )
      println("Success - Your Email has been sent")
      true
    }
    catch {
      case e: MessagingException => println("Message Failed: \n" + e)
        false
    }

  }

}


