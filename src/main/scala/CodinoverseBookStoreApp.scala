package com.codinoverse

import JsonProtocol.BookJsonProtocol

import akka.http.scaladsl.Http
import com.codinoverse.api.LowLevelRestApI._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object CodinoverseBookStoreApp extends App{

  val bindingServer = Http().newServerAt("localhost",8085).bind(requestHandler)

  StdIn.readLine()
  bindingServer
    .flatMap(_.unbind()) // Trigger unbinding from the port
    .onComplete(_ => system.terminate()) // And shutdown when done

  Await.result(system.whenTerminated, Duration.Inf)



}
