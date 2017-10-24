package app.apiutils

import org.joda.time.DateTime


class PageAverageObject(dtfp: Int, dtdc: Int, dsdc: Int, dtfl: Int, dsfl: Int, dcflprepaid: Double, dcflpostpaid: Double, dsi: Int, dsc: Int, mtfp: Int, mtdc: Int, msdc: Int, mtfl: Int, msfl: Int, mcflprepaid: Double, mcflpostpaid: Double, msi: Int, msc: Int, resultString: String) {

  val desktopTimeFirstPaintInMs: Int = dtfp
  lazy val desktopTimeFirstPaintInSeconds: Double = roundAt(3)(desktopTimeFirstPaintInMs.toDouble/1000)
  val desktopTimeDocCompleteInMs: Int = dtdc
  lazy val desktopTimeDocCompleteInSeconds: Double = roundAt(3)(desktopTimeDocCompleteInMs.toDouble/1000)
  val desktopKBInDocComplete: Double = dsdc
  lazy val desktopMBInDocComplete: Double = roundAt(3)(desktopKBInDocComplete.toDouble/1024)
  val desktopTimeFullyLoadedInMs: Int = dtfl
  lazy val desktopTimeFullyLoadedInSeconds: Double = roundAt(3)(desktopTimeFullyLoadedInMs.toDouble/1000)
  val desktopKBInFullyLoaded: Double = dsfl
  lazy val desktopMBInFullyLoaded: Double = roundAt(3)(desktopKBInFullyLoaded.toDouble/1024)
  val desktopEstUSPrePaidCost: Double = dcflprepaid
  val desktopEstUSPostPaidCost: Double = dcflpostpaid
  val desktopSpeedIndex: Int = dsi
  lazy val desktopAboveTheFoldCompleteInSec: Double = roundAt(3)(desktopSpeedIndex.toDouble/1000)
  val desktopSuccessCount = dsc

  val mobileTimeFirstPaintInMs: Int = mtfp
  lazy val mobileTimeFirstPaintInSeconds: Double = roundAt(3)(mobileTimeFirstPaintInMs.toDouble/1000)
  val mobileTimeDocCompleteInMs: Int = mtdc
  lazy val mobileTimeDocCompleteInSeconds: Double = roundAt(3)(mobileTimeDocCompleteInMs.toDouble/1000)
  val mobileKBInDocComplete: Double = msdc
  lazy val mobileMBInDocComplete: Double = roundAt(3)(mobileKBInDocComplete.toDouble/1024)
  val mobileTimeFullyLoadedInMs: Int = mtfl
  lazy val mobileTimeFullyLoadedInSeconds: Double = roundAt(3)(mobileTimeFullyLoadedInMs.toDouble/1000)
  val mobileKBInFullyLoaded: Double = msfl
  lazy val mobileMBInFullyLoaded: Double = roundAt(3)(mobileKBInFullyLoaded.toDouble/1024)
  val mobileEstUSPrePaidCost: Double = mcflprepaid
  val mobileEstUSPostPaidCost: Double = mcflpostpaid
  val mobileSpeedIndex: Int = msi
  lazy val mobileAboveTheFoldCompleteInSec: Double = roundAt(3)(desktopSpeedIndex.toDouble/1000)
  val mobileSuccessCount = msc

  val formattedHTMLResultString: String = resultString
  lazy val (desktopPart,mobilePart): (String,String) = formattedHTMLResultString.splitAt(formattedHTMLResultString.indexOf("<tr>",4))
  lazy val desktopHTMLResultString: String = desktopPart
  lazy val mobileHTMLResultString: String = mobilePart

  lazy val desktopTimeFirstPaintInMs80thPercentile: Double = (desktopTimeFirstPaintInMs * 80).toDouble/ 100
  lazy val desktopTimeDocCompleteInMs80thPercentile: Double = (desktopTimeDocCompleteInMs * 80).toDouble/ 100
  lazy val desktopKBInDocComplete80thPercentile: Double = (desktopKBInDocComplete * 80)/ 100
  lazy val desktopTimeFullyLoadedInMs80thPercentile: Double = (desktopTimeFullyLoadedInMs * 80).toDouble/ 100
  lazy val desktopKBInFullyLoaded80thPercentile:Double = (desktopKBInFullyLoaded * 80)/ 100
  lazy val desktopEstUSPrePaidCost80thPercentile: Double = (desktopEstUSPrePaidCost * 80)/ 100
  lazy val desktopEstUSPostPaidCost80thPercentile: Double = (desktopEstUSPostPaidCost * 80)/ 100
  lazy val desktopSpeedIndex80thPercentile: Double = (desktopSpeedIndex * 80).toDouble/ 100
  lazy val mobileTimeFirstPaintInMs80thPercentile: Double = (mobileTimeFirstPaintInMs * 80).toDouble/ 100
  lazy val mobileTimeDocCompleteInMs80thPercentile: Double = (mobileTimeDocCompleteInMs * 80).toDouble/ 100
  lazy val mobileKBInDocComplete80thPercentile: Double = (mobileKBInDocComplete * 80)/ 100
  lazy val mobileTimeFullyLoadedInMs80thPercentile: Double = (mobileTimeFullyLoadedInMs * 80).toDouble/ 100
  lazy val mobileKBInFullyLoaded80thPercentile: Double = (mobileKBInFullyLoaded * 80)/ 100
  lazy val mobileEstUSPrePaidCost80thPercentile: Double = (mobileEstUSPrePaidCost * 80)/ 100
  lazy val mobileEstUSPostPaidCost80thPercentile: Double = (mobileEstUSPostPaidCost * 80)/ 100
  lazy val mobileSpeedIndex80thPercentile: Double = (mobileSpeedIndex * 80).toDouble/100


  def this() {
    this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "")
  }

  def toHTMLString: String = formattedHTMLResultString

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) .toDouble/ s}
}


class ArticleDefaultAverages() extends PageAverageObject {
  override val desktopTimeFirstPaintInMs: Int = (2 * 1000).toInt
  override val desktopTimeDocCompleteInMs: Int = 15 * 1000
  override val desktopKBInDocComplete: Double = 4 * 1024
  override val desktopTimeFullyLoadedInMs: Int = 20 * 1000
  override val desktopKBInFullyLoaded: Double = 4096
  override val desktopEstUSPrePaidCost: Double = 0.60
  override val desktopEstUSPostPaidCost: Double = 0.50
  override val desktopSpeedIndex: Int = 1482
  override val desktopSuccessCount = 1

  override val mobileTimeFirstPaintInMs: Int = 3 * 1000
  override val mobileTimeDocCompleteInMs: Int = 15 * 1000
  override val mobileKBInDocComplete: Double = 3072
  override val mobileTimeFullyLoadedInMs: Int = 20 * 1000
  override val mobileKBInFullyLoaded: Double = 3584
  override val mobileEstUSPrePaidCost: Double = 0.40
  override val mobileEstUSPostPaidCost: Double = 0.30
  override val mobileSpeedIndex: Int = 5000
  override val mobileSuccessCount = 1
}

class LiveBlogDefaultAverages() extends PageAverageObject {
  override val desktopTimeFirstPaintInMs: Int = (2 * 1000).toInt
  override val desktopTimeDocCompleteInMs: Int = 15 * 1000
  override val desktopKBInDocComplete: Double = 3200
  override val desktopTimeFullyLoadedInMs: Int = 20 * 1000
  override val desktopKBInFullyLoaded: Double = 5120
  override val desktopEstUSPrePaidCost: Double = 0.60
  override val desktopEstUSPostPaidCost: Double = 0.50
  override val desktopSpeedIndex: Int = 2074
  override val desktopSuccessCount = 1

  override val mobileTimeFirstPaintInMs: Int = 3 * 1000
  override val mobileTimeDocCompleteInMs: Int = 15 * 1000
  override val mobileKBInDocComplete: Double = 3000
  override val mobileTimeFullyLoadedInMs: Int = 20 * 1000
  override val mobileKBInFullyLoaded: Double = 4096
  override val mobileEstUSPrePaidCost: Double = 0.40
  override val mobileEstUSPostPaidCost: Double = 0.30
  override val mobileSpeedIndex: Int = 5000
  override val mobileSuccessCount = 1
}

class InteractiveDefaultAverages() extends PageAverageObject {
  override val desktopTimeFirstPaintInMs: Int = (2.5 * 1000).toInt
  override val desktopTimeDocCompleteInMs: Int = 15 * 1000
  override val desktopKBInDocComplete: Double = 2900
  override val desktopTimeFullyLoadedInMs: Int = 20 * 1000
  override val desktopKBInFullyLoaded: Double = 5120//2906
  override val desktopEstUSPrePaidCost: Double = 0.60
  override val desktopEstUSPostPaidCost: Double = 0.50
  override val desktopSpeedIndex: Int = 4000
  override val desktopSuccessCount = 1

  override val mobileTimeFirstPaintInMs: Int = 3 * 1000
  override val mobileTimeDocCompleteInMs: Int = 15 * 1000
  override val mobileKBInDocComplete: Double = 2318
  override val mobileTimeFullyLoadedInMs: Int = 20 * 1000
  override val mobileKBInFullyLoaded: Double = 4096
  override val mobileEstUSPrePaidCost: Double = 0.40
  override val mobileEstUSPostPaidCost: Double = 0.30
  override val mobileSpeedIndex: Int = 7000
  override val mobileSuccessCount = 1
}



class FrontsDefaultAverages() extends PageAverageObject() {
  override val desktopTimeFirstPaintInMs: Int = (2 * 1000).toInt
  override val desktopTimeDocCompleteInMs: Int = 15 * 1000
  override val desktopKBInDocComplete: Double = 3 * 1024
  override val desktopTimeFullyLoadedInMs: Int = 20 * 1000
  override val desktopKBInFullyLoaded: Double = 4 * 1024
  override val desktopEstUSPrePaidCost: Double = 0.60
  override val desktopEstUSPostPaidCost: Double = 0.50
  override val desktopSpeedIndex: Int = 1717
  override val desktopSuccessCount = 1

  override val mobileTimeFirstPaintInMs: Int = 2 * 1000
  override val mobileTimeDocCompleteInMs: Int = 15 * 1000
  override val mobileKBInDocComplete: Double = 3 * 1024
  override val mobileTimeFullyLoadedInMs: Int = 20 * 1000
  override val mobileKBInFullyLoaded: Double = 4 * 1024
  override val mobileEstUSPrePaidCost: Double = 0.40
  override val mobileEstUSPostPaidCost: Double = 0.30
  override val mobileSpeedIndex: Int = 4694
  override val mobileSuccessCount = 1
}