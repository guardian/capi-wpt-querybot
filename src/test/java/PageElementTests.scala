import app.apiutils.{PageElementFromHTMLTableRow, WebPageTest, PageElement, LocalFileOperations}
import org.scalatest._

import scala.io.Source

/**
 * Created by mmcnamara on 01/03/16.
 */
  abstract class UnitSpec extends FlatSpec with Matchers with
  OptionValues with Inside with Inspectors

  class PageElementTests extends UnitSpec with Matchers {

    "A pageElementList" should "contain page elements" in {
      val wpt = new WebPageTest("foo", "bar", List())

      var htmlString: String = ""
      for (line <- Source.fromFile("webpagetestresultdetails.html").getLines()) {
        htmlString = htmlString + line
      }

      val pageElementList: List[PageElementFromHTMLTableRow] = wpt.generatePageElementList(wpt.trimToHTMLTable(htmlString))
      pageElementList.head.printElement()
      println("testing list is not empty")
      assert(pageElementList.nonEmpty)
    }

    "A non-empty pageElementList" should "contain page elements that have non-empty values" in {
      val wpt = new WebPageTest("foo", "bar", List())

      var htmlString: String = ""
      for (line <- Source.fromFile("webpagetestresultdetails.html").getLines()) {
        htmlString = htmlString + line
      }

      val pageElementList: List[PageElementFromHTMLTableRow] = wpt.generatePageElementList(wpt.trimToHTMLTable(htmlString))

      val checkall: List[Boolean] = pageElementList.map(element => {
        val nonEmptyElement: Boolean = (element.timeToFirstByte>0) ||
        (element.bytesDownloaded>0) ||
        (element.contentDownload>0) ||
        element.contentType.nonEmpty ||
        (element.dnsLookUp>0) ||
        (element.errorStatusCode>0) ||
        (element.initialConnection>0) ||
        element.iP.nonEmpty ||
        (element.requestStart>0) ||
        element.resource.nonEmpty ||
        (element.sslNegotiation>0)
        if (!nonEmptyElement) {println("This element is completely empty: \n" + element.returnString())}
        nonEmptyElement
      })
      assert(!checkall.contains(false))
    }

    "A sorted pageElementList" should "be sorted in order of largest bytesDownloaded to smallest" in {
      val wpt = new WebPageTest("foo", "bar", List())

      var htmlString: String = ""
      for (line <- Source.fromFile("webpagetestresultdetails.html").getLines()) {
        htmlString = htmlString + line
      }

      val pageElementList: List[PageElementFromHTMLTableRow] = wpt.generatePageElementList(wpt.trimToHTMLTable(htmlString))
      var sortedPageElementList: List[PageElementFromHTMLTableRow] = wpt.sortPageElementList(pageElementList)
      var isordered: Boolean = true
      var counter: Int = 1
      while (sortedPageElementList.tail.nonEmpty && isordered) {
        val current: Int = sortedPageElementList.head.bytesDownloaded
        val next: Int = sortedPageElementList.tail.head.bytesDownloaded
        isordered = (current >= next)
        if(!isordered){println("Element number: " + counter + "with size: " + pageElementList.head.bytesDownloaded + "should be greater than " + pageElementList.tail.head.bytesDownloaded)}
        counter += 1
        sortedPageElementList = sortedPageElementList.tail
      }
      if (isordered) {println("Tested list of " + counter + " elements. All are in order")}
      assert(isordered)
    }

 
  }



