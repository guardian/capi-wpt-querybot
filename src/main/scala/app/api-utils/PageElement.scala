package app.apiutils

import play.api.libs.json._

/**
 * Created by mmcnamara on 25/02/16.
 */
abstract  class PageElement {

  val resource: String
  val contentType: String
  val requestStart: Int
  val dnsLookUp: Int
  val initialConnection: Int
  val sslNegotiation: Int
  val timeToFirstByte: Int 
  val contentDownload: Int
  val bytesDownloaded: Int
  val errorStatusCode: Int
  val iP: String
  lazy val sizeInKB: Double = roundAt(2)(bytesDownloaded.toDouble/1024)
  lazy val sizeInMB: Double = roundAt(2)(bytesDownloaded.toDouble/(1024 * 1024))

  def toHTMLTableRow(): String = {
    "<tr>" +
      "<td>" + resource + "</td>" +
      "<td>" + contentType + "</td>" +
      "<td>" + requestStart.toString + "ms</td>" +
      "<td>" + dnsLookUp + "ms</td>" +
      "<td>" + initialConnection + "ms</td>" +
      "<td>" + sslNegotiation + "ms</td>" +
      "<td>" + timeToFirstByte + "ms</td>" +
      "<td>" + contentDownload + "ms</td>" +
      "<td>" + bytesDownloaded + "bytes</td>" +
      "<td>" + errorStatusCode + "</td>" +
      "<td>" + iP + "</td>" +
      "</tr>"
  }

  def stringToMilliseconds(time: String): Int ={
    if(!time.contains("-")) {
      if (time.contains("ms")) {
        val cleanTime: String = time.replaceAll(",", "")
        cleanTime.slice(0, time.indexOf(" ms")).toInt
      } else {
        if (time.contains(" s")) {
          (time.slice(0, time.indexOf(" s")).toDouble * 1000).toInt
        } else {
          0
        }
      }
    } else
      0
  }

  def stringToBytes(size: String): Int ={
    if(!size.contains("-")) {
      if (size.contains("KB") || size.contains("MB")) {
        if (size.contains("MB")) {
          (size.slice(0, size.indexOf(" MB")).toDouble * 1024 * 1024).toInt
        } else {
          val cleanSize: String = size.replaceAll(",", "")
          (cleanSize.slice(0, size.indexOf(" KB")).toDouble * 1024).toInt
        }
      } else {
        val cleanSize = size.replaceAll(",", "")
        cleanSize.slice(0, size.indexOf(" ")).toInt
      }
    } else {
      0
    }
  }


  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

}


class PageElementFromString(rowResource: String,
                  rowContentType: String,
                  rowRequestStart: String,
                  rowDNSLookUp: String,
                  rowInitialConnection: String,
                  rowSSLNegotiation: String,
                  rowTimeToFirstByte: String,
                  rowContentDownload: String,
                  rowBytesDownloaded: String,
                  rowErrorStatusCode: String,
                  rowIP: String) extends PageElement{

  override val resource: String = rowResource
  override val contentType: String = rowContentType
  override val requestStart: Int = stringToMilliseconds(rowRequestStart)
  override val dnsLookUp: Int = stringToMilliseconds(rowDNSLookUp)
  override val initialConnection: Int = stringToMilliseconds(rowInitialConnection)
  override val sslNegotiation: Int = stringToMilliseconds(rowSSLNegotiation)
  override val timeToFirstByte: Int = stringToMilliseconds(rowTimeToFirstByte)
  override val contentDownload: Int = stringToMilliseconds(rowContentDownload)
  override val bytesDownloaded: Int = stringToBytes(rowBytesDownloaded)
  override val errorStatusCode: Int = rowErrorStatusCode.toInt
  override val iP: String = rowIP
}

class PageElementFromHTMLTableRow(htmlTableRow: String) extends PageElement{
  //assumes string starts with "<tr>" and ends with "</tr>" and that elements are encapsulated by <td class=["Some kind of class"]> .... </td>
  val requestNumberClassname: String = "reqNum"
  val resourceClassname: String = "reqUrl"
  val contentTypeClassname: String = "reqMime"
  val requestStartClassname: String = "reqStart"
  val dnsLookupClassname: String = "reqDNS"
  val initialConnectionClassname: String = "reqSocket"
  val sslNegotiationClassname: String = "reqSSL"
  val timeToFirstByteClassname: String = "reqTTFB"
  val contentDownloadClassname: String = "reqDownload"
  val bytesDownloadedClassname: String = "reqBytes"
  val errorStatusCodeClassname: String = "reqResult"
  val iPClassname: String = "ReqIP"

  val cleanString: String = htmlTableRow.replaceAll("<tr>","").replaceAll("</tr>", "").replaceAll(" odd","").replaceAll(" even","").replaceAll("Render", "").replaceAll("Doc","").replaceAll(" warning", "").replaceAll(" error", "")

  val resourceHTMLElement: String = getDataFromHTMLTableElement(cleanString, resourceClassname)

  override val resource: String = resourceHTMLElement.substring(resourceHTMLElement.indexOf("http"),resourceHTMLElement.indexOf("\"",resourceHTMLElement.indexOf("http")))

  override val contentType: String = getDataFromHTMLTableElement(cleanString, contentTypeClassname)

  val requestStartString: String = getDataFromHTMLTableElement(cleanString, requestStartClassname)
  override val requestStart: Int = stringToMilliseconds(requestStartString)

  val dnsLookUpString: String = getDataFromHTMLTableElement(cleanString, dnsLookupClassname)
  override val dnsLookUp: Int = stringToMilliseconds(dnsLookUpString)

  val initialConnectionString: String = getDataFromHTMLTableElement(cleanString, initialConnectionClassname)
  override val initialConnection: Int = stringToMilliseconds(initialConnectionString)

  val sslNegotiationString: String = getDataFromHTMLTableElement(cleanString, sslNegotiationClassname)
  override val sslNegotiation: Int = stringToMilliseconds(sslNegotiationString)

  val timeToFirstByteString: String = getDataFromHTMLTableElement(cleanString, timeToFirstByteClassname)
  override val timeToFirstByte: Int = stringToMilliseconds(timeToFirstByteString)

  val contentDownloadString: String = getDataFromHTMLTableElement(cleanString, contentDownloadClassname)
  override val contentDownload: Int = stringToMilliseconds(contentDownloadString)

  val bytesDownloadedString: String = getDataFromHTMLTableElement(cleanString, bytesDownloadedClassname)
  override val bytesDownloaded: Int = stringToBytes(bytesDownloadedString)

  val errorStatusCodeString: String = getDataFromHTMLTableElement(cleanString, errorStatusCodeClassname)
  override val errorStatusCode: Int = errorStatusCodeString.toInt

  override val iP: String = getDataFromHTMLTableElement(cleanString, iPClassname)

  def getDataFromHTMLTableElement(tableRow: String, classname: String): String = {
    val returnString: String = tableRow.substring(tableRow.indexOf(classname)+ classname.length + 2,tableRow.indexOf("</td>", tableRow.indexOf(classname)+ classname.length + 2))
    returnString
  }
  def returnString():String = {
    val returnString:String  = "Resource: " + resource + ", \n" +
      "Content Type: " + contentType + ", \n" +
      "Request Start Time: " + requestStart.toString + "ms, \n" +
      "DNS Look-up Time: " + dnsLookUp + "ms, \n" +
      "Initial Connection Time: " + initialConnection + "ms, \n" +
      "SSL Negotiation Time: " + sslNegotiation + "ms, \n" +
      "Time to First Byte: " + timeToFirstByte + "ms, \n" +
      "Content Download Time: " + contentDownload + "ms, \n" +
      "Bytes Downloaded: " + bytesDownloaded + "bytes, \n" +
      "Error Status Code: " + errorStatusCode + ", \n" +
      "IP Address: " + iP
    returnString
  }

  def printElement():Unit = {
    println("Resource: " + this.resource + ", \n")
    println("Content Type: " + this.contentType + ", \n")
    println("Request Start Time: " + this.requestStart.toString + "ms, \n")
    println("DNS Look-up Time: " + this.dnsLookUp + "ms, \n")
    println("Initial Connection Time: " + this.initialConnection + "ms, \n")
    println("SSL Negotiation Time: " + this.sslNegotiation + "ms, \n")
    println("Time to First Byte: " + this.timeToFirstByte + "ms, \n")
    println("Content Download Time: " + this.contentDownload + "ms, \n")
    println("Bytes Downloaded: " + this.bytesDownloaded + "bytes, \n")
    println("Error Status Code: " + this.errorStatusCode + ", \n")
    println("IP Address: " + this.iP)
  }

  def toHTMLRowString():String = {
    val returnString: String = "<tr class=\"datarow\">" +
      "<td><a href = \"" + resource + "\">" + resource + "</a></td>" +
      "<td>" + contentType + "</td>" +
      "<td>" + sizeInKB + "KB</td>" +
      "</tr>" + "\n"
    returnString
  }

  def toJsonObject(): JsObject = {
    val json: JsObject = JsObject(Seq(
      "resource" -> JsString(resource),
      "contentType" -> JsString(contentType),
      "requestStart" -> JsNumber(requestStart),
      "dnsLookUp" -> JsNumber(dnsLookUp),
      "initialConnection" -> JsNumber(initialConnection),
      "sslNegotiation" -> JsNumber(sslNegotiation),
      "timeToFirstByte" -> JsNumber(timeToFirstByte),
      "ontentDownload" -> JsNumber(contentDownload),
      "bytesDownloaded" -> JsNumber(bytesDownloaded),
      "errorStatusCode" -> JsNumber(errorStatusCode),
      "iP" -> JsString(iP)
      )
    )
    json
  }

  def alertHTMLString():String = {
    val returnString: String = "<tr>" +
      "<td><a href = \"" + resource + "\">" + resource + "</a></td>" +
      "<td>" + contentType + "</td>" +
      "<td>" + sizeInKB + "KB</td>" +
      "</tr>"
    returnString
  }

  def emailHTMLString():String = {
    val tableNormalRowEmailTag: String = "<tr style=\"background-color: ;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;page-break-inside: avoid;\" #d9edf7\";\">"
    val tableNormalCellEmailTag: String = "<td style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;padding: 0;background-color: #fff!important;\">"
    val aHrefEmailStyle: String = "style=\"-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;background-color: transparent;color: #337ab7;text-decoration: underline;\""

    val returnString: String = tableNormalRowEmailTag +
      tableNormalCellEmailTag + "<a href = \"" + resource + "\">" + resource + aHrefEmailStyle +"</a></td>" +
      tableNormalCellEmailTag + contentType + "</td>" +
      tableNormalCellEmailTag + sizeInKB + "KB</td>" +
      "</tr>"
    returnString


  }

  def isMedia():Boolean = {
      contentType.contains("image") ||
      contentType.contains("video") ||
      contentType.contains("media")
  }
}




