import app.apiutils.PageElementFromParameters
import com.gu.contentapi.client.model.v1.CapiDateTime
import org.joda.time.DateTime

val pageElementImage = new PageElementFromParameters("https://i.guim.co.uk/img/media/1d7d0ba1f465cd21e2613867de4c797addc78cda/0_227_3500_2100/master/3500.jpg?w=1125&q=55&auto=format&usm=12&fit=max&s=bfaffda9157043b860f84312cc7ca311","type",1,1,1,1,1,10,10240,1,"ipAddress")
val pageElementYoutube = new PageElementFromParameters("https://s.ytimg.com/yts/jsbin/player-en_US-vflWoKF7f/base.js","type",1,1,1,1,1,10,10240,1,"ipAddress")
val pageElementTwitter = new PageElementFromParameters("https://pbs.twimg.com/media/Cnkqn-EXYAAjtAc.jpg","type",1,1,1,1,1,10,10240,1,"ipAddress")
val pageElementFacebook = new PageElementFromParameters("https://connect.facebook.net/en_US/sdk/xfbml.ad.js","type",1,1,1,1,1,10,10240,1,"ipAddress")
val pageElementFormstack = new PageElementFromParameters("https://static.formstack.com/forms/js/3/google-phone-lib_60aae7e185.js","type",1,1,1,1,1,10,10240,1,"ipAddress")


println("pageElementImage says:" + pageElementImage.identifyPageElementType())
println("pageElementYouTube says:" + pageElementYoutube.identifyPageElementType())
println("pageElementTwitter says:" + pageElementTwitter.identifyPageElementType())
println("pageElementFacebook says:" + pageElementFacebook.identifyPageElementType())
println("pageElementFormstack says:" + pageElementFormstack.identifyPageElementType())

val capitime1 = new CapiDateTime {
  override def dateTime: Long = 1469455344000L
}

val capitime2 = new CapiDateTime {
  override def dateTime: Long = 1469368741000L
}

val timediff = capitime1.dateTime - capitime2.dateTime
val hours = timediff.toDouble /(1000 * 60 * 60)

val capitime3 = new CapiDateTime {
  override def dateTime: Long = 1469342702000L
}

def capiTimeToString(time: CapiDateTime): String = {
  val capiDT:CapiDateTime = time
  val timeLong = capiDT.dateTime
  val moreUseableTime = new DateTime(timeLong)
  moreUseableTime.toDate.toString
}

println(capiTimeToString(capitime1))

println(capiTimeToString(capitime2))

println(capiTimeToString(capitime3))


val now = DateTime.now()
val hour = now.hourOfDay.getAsString

