package io.github.shogowada.scala.jsonrpc

import io.github.shogowada.scala.jsonrpc.serializers.JSONRPCPickler.{ReadWriter => RW, macroRW, readwriter}

import scala.concurrent.Future

object Types {
  type Id = Either[String, Integer]

  type JSONSender = (String) => Future[Option[String]]
}
