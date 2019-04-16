import app.api.{GLabsEmailTemplate, InteractiveEmailTemplate, PageWeightEmailTemplate}
import app.api_utils.emails.EmailOperations
import app.api_utils.model.PerformanceResultsObject
import org.scalatest._

/**
 * Created by mmcnamara on 26/05/16.
 */
abstract class EmailUnitSpec extends FlatSpec with Matchers with
OptionValues with Inside with Inspectors

class EmailTests extends EmailUnitSpec with Matchers {

  val testEmailAddresses = List("Insert.Email.Here@emailaddress.com")
  val testEmailUserName = "Insert.Email.Here@emailaddress.com"
  val testEmailPwd = "SomeTextHere"


  val fakeDashboardUrl = "http://www.theguardian.com/uk"
  val fakeOtherDashboardUrl = "http://www.theguardian.com/us"
  val testResult1 = new PerformanceResultsObject("mobileArticlespeedIndexHigh", "mobileArticlespeedIndexHigh", "mobileArticlespeedIndexHigh", 1, 1, 1, 1, 1, 1, 1, "mobileArticlespeedIndexHigh", true, true, true)
  val testResult2 = new PerformanceResultsObject("mobileArticletFpHigh", "mobileArticletFpHigh", "mobileArticletFpHigh", 2, 2, 2, 2, 2, 2, 2, "mobileArticletFpHigh", true, true, true)
  val testResult3 = new PerformanceResultsObject("testResult3", "testResult3", "testResult3", 3, 3, 3, 3, 3, 3, 3, "testResult3", true, true, true)

  val testResultInteractive1 = new PerformanceResultsObject("InteractivePWandPS", "Android/3G", "InteractivePWandPS", 1, 1, 1, 1, 1, 1, 1, "mobileArticlespeedIndexHigh", true, true, true)
  val testResultInteractive2 = new PerformanceResultsObject("InteractivePW", "Android/3G", "InteractivePW", 2, 2, 2, 2, 2, 2, 2, "mobileArticletFpHigh", true, false, true)
  val testResultInteractive3 = new PerformanceResultsObject("InteractivePS", "Android/3G", "InteractivePS", 3, 3, 3, 3, 3, 3, 3, "testResult3", false, true, true)

  val testResultInteractive4 = new PerformanceResultsObject("InteractivePWandPS", "Desktop", "InteractivePWandPS", 1, 1, 1, 1, 1, 1, 1, "mobileArticlespeedIndexHigh", true, true, true)
  val testResultInteractive5 = new PerformanceResultsObject("InteractivePW", "Desktop", "InteractivePW", 2, 2, 2, 2, 2, 2, 2, "mobileArticletFpHigh", true, false, true)
  val testResultInteractive6 = new PerformanceResultsObject("InteractivePS", "Desktop", "InteractivePS", 3, 3, 3, 3, 3, 3, 3, "testResult3", false, true, true)

  val testResultInteractive7 = new PerformanceResultsObject("InteractiveNoAlert", "Android/3G", "InteractiveNoAlert", 3, 3, 3, 3, 3, 3, 3, "NoAlert", false, false, true)
  val testResultInteractive8 = new PerformanceResultsObject("InteractiveNoAlert", "Desktop", "InteractiveNoAlert", 3, 3, 3, 3, 3, 3, 3, "NoAlert", false, false, true)

  val testResultListEmpty = List()
  val testResultList1results = List(testResult1)
  val testResultList2results = List(testResult1, testResult2)
  val testResultList3results = List(testResult1, testResult2, testResult3)

  val interactiveResultList = List(testResultInteractive1, testResultInteractive2, testResultInteractive3, testResultInteractive4, testResultInteractive5, testResultInteractive6)
  interactiveResultList.foreach(result => {result.setPageType("Interactive")
    result.setHeadline(Option("A headline"))})

  val interactiveNoAlertList = List(testResultInteractive7, testResultInteractive8)
  interactiveNoAlertList.foreach(result => {result.setPageType("Interactive")
    result.setHeadline(Option("A headline"))})


  val emptyListText = "I'm very sorry. This email was sent in error. Please ignore."
  val singleResultText = "mobileArticlespeedIndexHigh"
  val twoResultsText = "mobileArticletFpHigh"
  val threeResultsText = "testResult3"

  val interactive1Text = "InteractivePWandPS"
  val interactive2Text = "InteractivePW"
  val interactive3Text = "InteractivePS"

  val interactiveNoAlertText = "No alerts set"

  val emailOps = new EmailOperations(testEmailUserName,testEmailPwd)

  "An pageWeight Email list with 0 Results" should "contain results and page elements" in {
    val pageWeightEmail = new PageWeightEmailTemplate(List(), fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(pageWeightEmail.toString().contains(emptyListText))
  }

  "An pageWeight Email list with 1 Results" should "contain results and page elements" in {
    val pageWeightEmail = new PageWeightEmailTemplate(testResultList1results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(pageWeightEmail.toString().contains(singleResultText))
  }

  "An pageWeight Email list with 2 Results" should "contain results and page elements" in {
    val pageWeightEmail = new PageWeightEmailTemplate(testResultList2results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(pageWeightEmail.toString().contains(singleResultText) && pageWeightEmail.toString().contains(twoResultsText))
  }

  "An pageWeight Email list with 3 Results" should "contain results and page elements" in {
    val pageWeightEmail = new PageWeightEmailTemplate(testResultList3results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(pageWeightEmail.toString().contains(singleResultText) && pageWeightEmail.toString().contains(twoResultsText) && pageWeightEmail.toString().contains(threeResultsText))
  }

  "An interactive Email list with 0 Results" should "contain results and page elements" in {
    val interactiveEmail = new InteractiveEmailTemplate(List(), fakeDashboardUrl, fakeOtherDashboardUrl)
    println(interactiveEmail.toString())
    assert(interactiveEmail.toString().contains(emptyListText))
  }

  "An interactive Email list with 1 Results" should "contain results and page elements" in {
    val interactiveEmail = new InteractiveEmailTemplate(testResultList1results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(interactiveEmail.toString().contains(singleResultText))
  }

  "An interactive Email list with 2 Results" should "contain results and page elements" in {
    val interactiveEmail = new InteractiveEmailTemplate(testResultList2results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(interactiveEmail.toString().contains(singleResultText) && interactiveEmail.toString().contains(twoResultsText))
  }

  "An interactive Email list with 3 Results" should "contain results and page elements" in {
    val interactiveEmail = new InteractiveEmailTemplate(testResultList3results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(interactiveEmail.toString().contains(singleResultText) && interactiveEmail.toString().contains(twoResultsText) && interactiveEmail.toString().contains(threeResultsText))
  }

  "An interactive Email list with 6 Results" should "contain results and page elements" in {
    val interactiveEmail = new InteractiveEmailTemplate(interactiveResultList, fakeDashboardUrl, fakeOtherDashboardUrl)
    println(interactiveEmail.toString())
    assert(interactiveEmail.toString().contains(interactive1Text) && interactiveEmail.toString().contains(interactive2Text) && interactiveEmail.toString().contains(interactive3Text))
  }

  "An interactive Email list with 2 Results of which neither has an alert" should "contain this was sent in error message" in {
    val interactiveEmail = new InteractiveEmailTemplate(interactiveNoAlertList, fakeDashboardUrl, fakeOtherDashboardUrl)
    println(interactiveEmail.toString())
    assert(interactiveEmail.toString().contains(interactiveNoAlertText))
  }


  "A Paid-Content Email list with 0 Results" should "contain results and page elements" in {
    val paidContentEmail = new GLabsEmailTemplate(List(), fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(paidContentEmail.toString().contains(emptyListText))
  }

  "A Paid-Content Email list with 1 Results" should "contain results and page elements" in {
    val paidContentEmail = new GLabsEmailTemplate(testResultList1results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(paidContentEmail.toString().contains(singleResultText))
  }

  "A Paid-Content Email list with 2 Results" should "contain results and page elements" in {
    val paidContentEmail = new GLabsEmailTemplate(testResultList2results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(paidContentEmail.toString().contains(singleResultText) && paidContentEmail.toString().contains(twoResultsText))
  }

  "A Paid-Content Email list with 3 Results" should "contain results and page elements" in {
    val paidContentEmail = new GLabsEmailTemplate(testResultList3results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    assert(paidContentEmail.toString().contains(singleResultText) && paidContentEmail.toString().contains(twoResultsText) && paidContentEmail.toString().contains(threeResultsText))
  }

  "An interactive Email" should "contain a list of the relevant people to send to" in {
    testResult1.setCreator("testResult1Creator@test.com")
    testResult1.setProductionOffice("US")

  }

 /* "A Paid-Content Email list with 3 Results" should "send succesfully" in {
    val paidContentEmail = new GLabsEmailTemplate(testResultList3results, fakeDashboardUrl, fakeOtherDashboardUrl)
    //      println(pageWeightEmail.toString())
    val emailSendSuccess = emailOps.sendPaidContentAlert(testEmailAddresses,paidContentEmail.toString())
    assert(paidContentEmail.toString().contains(singleResultText) && paidContentEmail.toString().contains(twoResultsText) && paidContentEmail.toString().contains(threeResultsText) && emailSendSuccess)
  }*/


}
