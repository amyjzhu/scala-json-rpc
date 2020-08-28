package io.github.shogowada.scala.jsonrpc.serializers

import upickle.core.Visitor

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object JSONRPCPickler extends upickle.AttributeTagged {
  implicit def OptionW[T: Writer]: Writer[Option[T]] = {
    OptionWriter[T]
  }

  implicit def OptionR[T: Reader]: Reader[Option[T]] = {
    OptionReader[T]
  }

  implicit def IdW: Writer[Either[String, Integer]] = new Writer[Either[String, Integer]] {
    def write0[V](out: Visitor[_, V], v: Either[String, Integer]) = {
      v match {
        case Left(v) => out.visitString(v, -1)
        case Right(v) => out.visitInt32(v, -1)
      }
    }
  }

  implicit def IdR: Reader[Either[String, Integer]] = new SimpleReader[Either[String, Integer]] {
    override def expectedMsg = "expected string or integer"
    override def visitString(s: CharSequence, index: Int) = Left(s.toString)
    override def visitInt32(d: Int, index: Int) = Right(d)
    override def visitInt64(d: Long, index: Int) = Right(d.toInt)
    override def visitUInt64(d: Long, index: Int) = Right(d.toInt)
  }
}

class UpickleJSONSerializer extends JSONSerializer {
  override def serialize[T](value: T): Option[String] = macro UpickleJSONSerializerMacro.serialize[T]

  override def deserialize[T](json: String): Option[T] = macro UpickleJSONSerializerMacro.deserialize[T]
}

object UpickleJSONSerializer {
  def apply() = new UpickleJSONSerializer
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
