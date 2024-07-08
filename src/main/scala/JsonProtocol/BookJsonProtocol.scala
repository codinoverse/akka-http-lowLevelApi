package com.codinoverse
package JsonProtocol
import com.codinoverse.model.Book
import spray.json._

trait BookJsonProtocol extends DefaultJsonProtocol {
  implicit val bookProtocol = jsonFormat4(Book)

}
