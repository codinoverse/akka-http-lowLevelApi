package com.codinoverse
package api

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, RequestEntity, StatusCodes, Uri}
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.codinoverse.JsonProtocol.BookJsonProtocol
import com.codinoverse.model.Book
import com.codinoverse.service.BookActor
import com.codinoverse.service.BookActor._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import spray.json._


object LowLevelRestApI extends BookJsonProtocol{
  implicit val system = ActorSystem("BookStoreAPi")
  system.registerOnTermination(System.exit(0))
  implicit val materializer = Materializer
  implicit val executionContext = system.dispatcher
  implicit val timeOut = Timeout(3.seconds)
  val bookActor = system.actorOf(Props[BookActor],"BookActor")

  //Adding some records to the BookStoreDb
  val booksList = List(
    Book("Scala Programming","David Miller",2005,2),
    Book("Java and Basics","S.Chand",2018,8),
    Book("Mathematics-1","R.S.Agarwal",2004,9),
    Book("Harry Potter Part -1","J.K.Rowling",1999,0)
  )

  // Initializing books in the database by sending messages to the bookActor
  val bookFutures = booksList.map { book =>
    (bookActor ? CreateBook(book)).mapTo[BookCreated].map { created =>
      println(s"Book is created ${created.id}")
    }
  }

  // Waiting for all books to be created and logging the result
  Future.sequence(bookFutures).onComplete {
    case Success(_) => println("All books are created")
    case Failure(ex) => println("Error creating books", ex)
  }

  // Request handler for the HTTP server
 val requestHandler:HttpRequest=>Future[HttpResponse]={

   // Handle GET requests to /api/books
   case HttpRequest(HttpMethods.GET, uri@Uri.Path("/api/books"), value, entity, protocol)=>
     val query = uri.query()
    if (query.isEmpty) fetchIfEmpty() else fetchById(query)

   // Handle GET requests to /api/books/inventory
   case HttpRequest(HttpMethods.GET, uri@Uri.Path("/api/books/inventory"), value, entity, protocol)=>
     val query = uri.query()
     checkInStock(query)


   // Handle POST requests to /api/books
   case HttpRequest(HttpMethods.POST, Uri.Path("/api/books"), value, entity, protocol)=>
    postCreateBook(entity)


   // Handle POST requests to /api/books/inventory
   case HttpRequest(HttpMethods.POST, uri@Uri.Path("/api/books/inventory"), value, entity, protocol)=>
     addQUantity(uri.query())


   // Handle all other requests with a 404 Not Found response
   case request: HttpRequest=>
     Future{
       request.discardEntityBytes()
       HttpResponse(StatusCodes.NotFound)
     }


 }


  // Function to fetch a book by its ID
  def fetchById(query: Uri.Query):Future[HttpResponse]={
    val bookId = query.get("id").map(_.toInt)
    bookId match {
      case None => Future(HttpResponse(StatusCodes.BadRequest))
      case Some(id)=>
        val book = (bookActor ? GetBookById(id)).mapTo[Option[Book]]
        book.map{
          case Some(value) => HttpResponse(StatusCodes.OK,entity = HttpEntity(ContentTypes.`application/json`,value.toJson.prettyPrint))
          case None=>HttpResponse(StatusCodes.NotFound)
        }
    }
  }

  // Function to fetch all books if no query parameters are provided
  def fetchIfEmpty(): Future[HttpResponse] = {
    val futureListBooks: Future[List[Book]] = (bookActor ? GetAllBooks).mapTo[List[Book]]
    futureListBooks.map {
      books =>
        HttpResponse(
          StatusCodes.OK,
          entity = HttpEntity(
            ContentTypes.`application/json`,
            books.toJson.prettyPrint
          )
        )
    }
  }

  // Function to create a new book from a POST request

  def postCreateBook(entity: RequestEntity): Future[HttpResponse] = {
    val futureStrictEntity = entity.toStrict(3.seconds)
    futureStrictEntity.flatMap {
      strictEntity =>
        val bookJsonString = strictEntity.data.utf8String
        val book = bookJsonString.parseJson.convertTo[Book]
        val bookCreated = (bookActor ? CreateBook(book)).mapTo[BookCreated]
        bookCreated.map(created => HttpResponse(StatusCodes.OK, entity = s"Book is created with ${created.id}"))
    }
  }

  // Function to check if books are in stock based on query parameters
  def checkInStock(query: Uri.Query): Future[HttpResponse] = {
    val flag = query.get("instock").map(_.toBoolean)
    flag match {
      case Some(value) =>
        val futureBooksList = (bookActor ? SearchBooksInStock(value)).mapTo[List[Book]]
        futureBooksList.map {
          book =>
            HttpResponse(StatusCodes.OK, entity = HttpEntity(
              ContentTypes.`application/json`,
              book.toJson.prettyPrint
            ))
        }
      case None => Future(HttpResponse(StatusCodes.BadRequest))
    }

  }


  // Function to add quantity to a book's stock based on query parameters
  def addQUantity(query: Uri.Query): Future[HttpResponse] = {
 val bookId = query.get("id").map(_.toInt)
 val bookquantity = query.get("quantity").map(_.toInt)
 val bookResponse = for {
   id <- bookId
   quantity <- bookquantity
 } yield {
   val bookFutureOption = (bookActor ? ModifyStock(id, quantity)).mapTo[Option[Book]]
   bookFutureOption.map {
     case None => HttpResponse(StatusCodes.BadRequest)
     case Some(value) => HttpResponse(
       StatusCodes.OK,
       entity = HttpEntity(
         ContentTypes.`application/json`,
         s"new quantity of book with ${id} is ${value.quantity}"
       )
     )
   }


 }
 bookResponse.getOrElse(Future(HttpResponse(StatusCodes.BadRequest)))
  }


}
