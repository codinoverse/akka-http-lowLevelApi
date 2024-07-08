package com.codinoverse

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.Materializer
import akka.util.{ByteString, Timeout}
import com.codinoverse.JsonProtocol.BookJsonProtocol
import com.codinoverse.api.LowLevelRestApI
import com.codinoverse.model.Book
import com.codinoverse.service.BookActor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import spray.json._

class LowLvelRestApiSpec extends AnyFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with BookJsonProtocol{
  implicit val sys = ActorSystem("BookStoreAPiTest")
  implicit val executionContext = sys.dispatcher
  implicit val timeOut = Timeout(3.seconds)
  implicit val mat = Materializer
  val bookActor = system.actorOf(Props[BookActor], "BookActor")
  implicit val patienceConfi= PatienceConfig(timeout = 3.seconds)

  val lowLevelRestApi = LowLevelRestApI


  "LowLevelRestApi" should "return all books for GET /api/books" in {
    val request = HttpRequest(HttpMethods.GET, uri = "/api/books")

    val responseFuture: Future[HttpResponse] = lowLevelRestApi.requestHandler(request)

    responseFuture.map { response =>
      response.status shouldEqual StatusCodes.OK
      response.entity.contentType shouldEqual ContentTypes.`application/json`
      val responseString = response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).futureValue.utf8String
      responseString should include("Scala Programming")
    }
  }

  "LowLevelRestApi" should "return all books for GET /api/books?id=1" in {
    val request = HttpRequest(HttpMethods.GET, uri = "/api/books?id=1")

    val responseFuture: Future[HttpResponse] = lowLevelRestApi.requestHandler(request)

    responseFuture.map { response =>
      response.status shouldEqual StatusCodes.OK
      response.entity.contentType shouldEqual ContentTypes.`application/json`
      val responseString = response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).futureValue.utf8String
      responseString should include("Scala Programming")
    }
  }

  "LowLevelRestApi" should "create a new book for POST /api/books" in {
    val newBook = Book("Test Book","Test Author",2021,10)
    val entity = HttpEntity(ContentTypes.`application/json`,newBook.toJson.prettyPrint)
    val request = HttpRequest(HttpMethods.POST, uri = "/api/books",entity=entity)

    val responseFuture: Future[HttpResponse] = lowLevelRestApi.requestHandler(request)

    responseFuture.map { response =>
      response.status shouldEqual StatusCodes.OK
      response.entity.contentType shouldEqual ContentTypes.`application/json`
      val responseString = response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).futureValue.utf8String
      responseString should include("Book is Created")
    }
  }
  "LowLevelRestApi" should "check books in stock for GET /api/books/inventory?instock=true" in {
    val request = HttpRequest(HttpMethods.GET, uri = "/api/books/inventory?instock=true")

    val responseFuture: Future[HttpResponse] = lowLevelRestApi.requestHandler(request)

    responseFuture.map { response =>
      response.status shouldEqual StatusCodes.OK
      response.entity.contentType shouldEqual ContentTypes.`application/json`
      val responseString = response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).futureValue.utf8String
      responseString should include("Scala Programming")
    }
  }
  "LowLevelRestApi" should "add quantity to a book for POST /api/books/inventory?id=1&quantity=5" in {
    val request = HttpRequest(HttpMethods.POST, uri = "/api/books/inventory?id=1&quantity=5")
    val responseFuture: Future[HttpResponse] = lowLevelRestApi.requestHandler(request)
    responseFuture.map { response =>
      response.status shouldEqual StatusCodes.OK
      val responseString = response.entity.dataBytes.runFold(ByteString(""))(_ ++ _).futureValue.utf8String
      responseString should include("quantity of 5 is added to 1")
    }
  }

  "LowLevelRestApi" should "return 404 for unknown routes" in {
    val request = HttpRequest(HttpMethods.GET, uri = "/api/unknown")

    val responseFuture: Future[HttpResponse] = lowLevelRestApi.requestHandler(request)

    responseFuture.map { response =>
      response.status shouldEqual StatusCodes.NotFound

    }
  }








}
