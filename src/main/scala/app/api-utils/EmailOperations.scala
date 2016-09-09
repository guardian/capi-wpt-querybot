package app.apiutils

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

  /*def sendEmail(): Boolean = {
    val sMTPemailAddress: String = "pageperformancetesterbot@gmail.com"
    val sMTPemailPassword: String = "AirServicesAustralia"
    val sMTPportId: Int = 587
    //or 587
    val sMTPtLSSSLRequired: String = "yes"

    val mailer = Mailer(sMTPemailAddress, sMTPportId)
        .auth(true)
        .as(sMTPemailAddress,sMTPemailPassword)
        .sslSocketFactory
        .startTtls(true)()

    println(mailer.toString)



    var emailSuccess: Boolean = false
    val internetAddress = new InternetAddress(sMTPemailAddress, true)
    val toaddress = new InternetAddress("michael.mcnamara@guardian.co.uk")
    val ccaddress = new InternetAddress("m_w_mcnamara@hotmail.com")
    println(internetAddress.getAddress)
    println(internetAddress.toString)
    mailer(Envelope.from(new InternetAddress(sMTPemailAddress, true))
      .to(new InternetAddress("michael.mcnamara@guardian.co.uk", true))
      .subject("test email from PagePerformanceTesterBot")
      .content(Text("Yay!")))
      .onSuccess{
        case _ => println ("Email sent successfully")
                   emailSuccess = true
                  }
    val myEnvelope = new Envelope(internetAddress,Option("testing"), Seq(toaddress), Seq(ccaddress), Seq.empty[InternetAddress],None, None, Seq.empty[(String,String)],Text("This is a test second attempt"))
    mailer.apply(myEnvelope)


    emailSuccess
    }*/

  def sendPageWeightAlert(emailAddressList: List[String], messageBody: String):Boolean = {
//    val username: String = "Michael.McNamara@guardian.co.uk"
//    val altfrom: String = "pageperformancetesterbot@gmail.com"
//    val password: String = "keprlztdbnyvgqsc"
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
    //    val username: String = "Michael.McNamara@guardian.co.uk"
    //    val altfrom: String = "pageperformancetesterbot@gmail.com"
    //    val password: String = "keprlztdbnyvgqsc"
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
      message.setSubject("Interactive Performance Alert - The following interactive pages have been measured as too slow or too heavy")
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


  def sendPaidContentAlert(emailAddressList: List[String], messageBody: String):Boolean = {
    //    val username: String = "Michael.McNamara@guardian.co.uk"
    //    val altfrom: String = "pageperformancetesterbot@gmail.com"
    //    val password: String = "keprlztdbnyvgqsc"
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


