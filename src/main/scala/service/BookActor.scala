package com.codinoverse
package service

import akka.actor.{Actor, ActorLogging}
import com.codinoverse.model.Book
import com.codinoverse.service.BookActor._

class BookActor extends Actor with ActorLogging {
  override def receive: Receive ={
    case GetAllBooks=>
      log.info(s"Getting All the Books From the BookStoreDB")
      sender() ! bookStoreDB.values.toList

    case GetBookById(id)=>
      log.info(s"Getting Book By Id $id")
      sender() ! bookStoreDB.get(id)

    case CreateBook(book)=>
      log.info(s"Creating a Book in the store with bookid $currentBookId")
      bookStoreDB+=(currentBookId->book)
      sender() ! BookCreated(currentBookId)
      currentBookId+=1

    case ModifyStock(id,amount)=>
      val book:Option[Book]=bookStoreDB.get(id)
      val newBook = book.map{
        case Book(name, author, publishedYear, quantity)=>Book(name, author, publishedYear, amount+quantity)
      }
      newBook.foreach(book=>bookStoreDB=bookStoreDB+(id->book))
      sender() ! newBook

    case SearchBooksInStock(instock)=>
      log.info(s"Searching for the Books in Stock")
      if (instock)
       sender() ! bookStoreDB.values.filter(book=>book.quantity>=1)
        else
       sender() ! bookStoreDB.values.filter(book=>book.quantity==0)

  }
}

object BookActor {
  var currentBookId:Int = 0
  var bookStoreDB:Map[Int,Book]=Map()
  case object GetAllBooks
  case class CreateBook(book:Book)
  case class GetBookById(id:Int)
  case class BookCreated(id:Int)
  case class ModifyStock(id:Int,quantity:Int)
  case class SearchBooksInStock(instock:Boolean)
}
