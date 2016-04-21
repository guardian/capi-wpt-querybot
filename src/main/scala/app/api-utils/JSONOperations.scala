package app.api

import java.io.{FileInputStream, File}
import java.util.Date

import app.apiutils.PerformanceResultsObject
import com.fasterxml.jackson.databind.JsonNode
import netscape.javascript.JSUtil
import play.libs.Json

/**
 * Created by mmcnamara on 18/04/16.
 */
class JSONOperations() {

  def readAndParse(file: File) = {
    val stream = new FileInputStream(file)
    val json = try {  Json.parse(stream) } finally { stream.close() }

  }

  def writeToFile(results: List[PerformanceResultsObject], filename: String): Boolean = {
    val jsonList: List[JsonNode] = results.map(result => {Json.toJson(result)})


  }



}
