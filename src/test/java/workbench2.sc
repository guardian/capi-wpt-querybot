import app.apiutils.PageElementFromParameters

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



