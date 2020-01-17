package services.apiutils


/**
 * Created by mmcnamara on 27/05/16.
 */

case class VisualsElementType(elementType: String, alt: String, canonicalUrl: String, bootUrl: String ) {
  val elementTypeDescription: String = elementType
  val altString: String = alt
  val canonicalUrlString: String = canonicalUrl
  val bootUrlString: String = bootUrl
}