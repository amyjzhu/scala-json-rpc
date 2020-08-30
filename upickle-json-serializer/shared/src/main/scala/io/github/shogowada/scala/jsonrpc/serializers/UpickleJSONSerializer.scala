package io.github.shogowada.scala.jsonrpc.serializers

import java.math.BigInteger

import io.github.shogowada.scala.jsonrpc.Models.{JSONRPCError, JSONRPCErrorResponse, JSONRPCId, JSONRPCMethod, JSONRPCNotification, JSONRPCRequest, JSONRPCResultResponse}
import upickle.core.Visitor

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Models {
}

object JSONRPCPickler extends upickle.AttributeTagged {
  implicit def OptionW[T: Writer]: Writer[Option[T]] = {
    OptionWriter[T]
  }

  implicit def OptionR[T: Reader]: Reader[Option[T]] = {
    OptionReader[T]
  }

  implicit def IdW: Writer[Either[String, BigDecimal]] = new Writer[Either[String, BigDecimal]] {
    def write0[V](out: Visitor[_, V], v: Either[String, BigDecimal]): V = {
      v match {
        case Left(v) => out.visitString(v, -1)
        case Right(v) => BigDecimalWriter.write0(out, v)
      }
    }
  }

  implicit def IdR: Reader[Either[String, BigDecimal]] = new SimpleReader[Either[String, BigDecimal]] {
    override def expectedMsg = "expected string or number"
    override def visitString(s: CharSequence, index: Int): Either[String, BigDecimal] = {
      try {
        Right(BigDecimalReader.visitString(s, -1))
      } catch {
        case _: java.lang.NumberFormatException => Left(s.toString)
      }
    }
  }
}

class UpickleJSONSerializer extends JSONSerializer {

  override def serialize[T](value: T): Option[String] = macro UpickleJSONSerializerMacro.serialize[T]

  override def deserialize[T](json: String): Option[T] = macro UpickleJSONSerializerMacro.deserialize[T]
}

object UpickleJSONSerializer {
  def apply() = new UpickleJSONSerializer

  import io.github.shogowada.scala.jsonrpc.serializers.JSONRPCPickler.{macroRW, ReadWriter => RW}

  implicit val rwJSONRPCMethod: RW[JSONRPCMethod] = macroRW

  implicit val rwJSONRPCId: RW[JSONRPCId] = macroRW

  implicit def rwJSONRPCRequest[T: RW]: RW[JSONRPCRequest[T]] = macroRW

  implicit def rwJSONRPCNotification[T: RW]: RW[JSONRPCNotification[T]] = macroRW

  implicit def rwJSONRPCResultResponse[T: RW]: RW[JSONRPCResultResponse[T]] = macroRW

  implicit def rwJSONRPCErrorResponse[T: RW]: RW[JSONRPCErrorResponse[T]] = macroRW

  implicit def rwJSONRPCError[T: RW]: RW[JSONRPCError[T]] = macroRW
}

object UpickleJSONSerializerMacro {
  def serialize[T](c: blackbox.Context)(value: c.Expr[T]): c.Expr[Option[String]] = {
    import c.universe._

    c.Expr[Option[String]](
      q"""
          scala.util.Try(io.github.shogowada.scala.jsonrpc.serializers.JSONRPCPickler.write($value)).toOption
          """
    )
  }

  def deserialize[T: c.WeakTypeTag](c: blackbox.Context)(json: c.Expr[String]): c.Expr[Option[T]] = {
    import c.universe._

    val deserializeType = weakTypeOf[T]

    c.Expr[Option[T]](
      q"""
          scala.util.Try(io.github.shogowada.scala.jsonrpc.serializers.JSONRPCPickler.read[$deserializeType]($json)).toOption
          """
    )
  }
}
