import app.apiutils.PageElementFromParameters
import com.gu.contentapi.client.model.v1.CapiDateTime
import org.joda.time.DateTime

val pageElementImage = new PageElementFromParameters("https://d15mj6e6qmt1na.cloudfront.net/attachments/13524706/counter-terrorism-radio-advert-encourages-mothers-to-talk-to-their-daughters-travelling-to-syri.mp3?audio_clip_id=2990345&uniquifier=937876&Expires=1472236780&Signature=huBzkNvJfghxoHu1t4LxMfFRjvVvTphKqkRjxP4Y3Z2SmMACrWkROybALdZ7yjj%7EwmlcGkNRw4bVNTIcgOXqkXqr4CCyvBF1JkgswtE7wva2eWIffD2CSfkODxaAH69cLnxVcf0XscX%7EnG%7EnT0IR46Uk%7ECn5AqUu5kY-ZKI8OOGX9EHXkfVRB%7EGCjGgeigs4jI-9gIDUqrJyX19Pgb2VOnugXf-QF90YyNb0VUrZOuAaV3aF42xRW0u2t-XkCg9ccksLUI%7Et84gkSInNEXBcusX9Kq667knjq6MdElCnJuEyfca3gAHcRamh7JWcqiZDRwYwBxZeG5QsOiaMCXmi3w__&Key-Pair-Id=APKAJS5FX6XHKL62XLYQ","type",1,1,1,1,1,10,10240,1,"ipAddress")
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
val hour = now.hourOfDay.get



