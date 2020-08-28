package io.github.shogowada.scala.jsonrpc

import io.github.shogowada.scala.jsonrpc.Types.Id
import io.github.shogowada.scala.jsonrpc.serializers.JSONRPCPickler.{ReadWriter => RW, macroRW}

import scala.language.experimental.macros

object Models {

  case class JSONRPCMethod(jsonrpc: String, method: String)

  object JSONRPCMethod{
    implicit val rw: RW[JSONRPCMethod] = macroRW
  }

  case class JSONRPCId(jsonrpc: String, id: Id)

  object JSONRPCId{
    implicit val rw: RW[JSONRPCId] = macroRW
  }

  case class JSONRPCRequest[PARAMS]
  (
      jsonrpc: String,
      id: Id,
      method: String,
      params: PARAMS
  )

  object JSONRPCRequest {
    implicit def rw[T: RW]: RW[JSONRPCRequest[T]] = macroRW
  }

  case class JSONRPCNotification[PARAMS]
  (
      jsonrpc: String,
      method: String,
      params: PARAMS
  )

  object JSONRPCNotification{
    implicit def rw[T: RW]: RW[JSONRPCNotification[T]] = macroRW
  }

  case class JSONRPCResultResponse[RESULT]
  (
      jsonrpc: String,
      id: Id,
      result: RESULT
  )

  object JSONRPCResultResponse{
    implicit def rw[T: RW]: RW[JSONRPCResultResponse[T]] = macroRW
  }

  case class JSONRPCErrorResponse[+ERROR]
  (
      jsonrpc: String,
      id: Id,
      error: JSONRPCError[ERROR]
  )

  object JSONRPCErrorResponse{
    implicit def rw[T: RW]: RW[JSONRPCErrorResponse[T]] = macroRW
  }

  case class JSONRPCError[+ERROR]
  (
      code: Int,
      message: String,
      data: Option[ERROR]
  )

  object JSONRPCError{
    implicit def rw[T: RW]: RW[JSONRPCError[T]] = macroRW
  }

  object JSONRPCErrors {
    lazy val parseError = JSONRPCError(-32700, "Parse error", Option("Invalid JSON was received by the server. An error occurred on the server while parsing the JSON text."))
    lazy val invalidRequest = JSONRPCError(-32600, "Invalid Request", Option("The JSON sent is not a valid Request object."))
    lazy val methodNotFound = JSONRPCError(-32601, "Method not found", Option("The method does not exist / is not available."))
    lazy val invalidParams = JSONRPCError(-32602, "Invalid params", Option("Invalid method parameter(s)."))
    lazy val internalError = JSONRPCError(-32603, "Internal error", Option("Internal JSON-RPC error."))
  }

  class JSONRPCException[+ERROR](val maybeResponse: Option[JSONRPCErrorResponse[ERROR]]) extends RuntimeException

}
