package io.github.shogowada.scala.jsonrpc

import org.scalatest._
import org.scalatest.freespec._
import org.scalatest.matchers.should._

abstract class BaseSpec extends AsyncFreeSpec
    with OneInstancePerTest
    with Matchers
