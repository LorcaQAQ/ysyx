// See README.md for license details.

package idu

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import chiseltest ._
import org. scalatest . flatspec. AnyFlatSpec
/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GCDSpec
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GCDSpec'
  * }}}
  * Testing from mill:
  * {{{
  * mill %NAME%.test.testOnly gcd.GCDSpec
  * }}}
  */
class IDUSpec extends AnyFlatSpec  with ChiselScalatestTester{
    "DUT" should "pass" in {
      test (new IDU ){ dut =>
        dut.io.instr.poke("h40d50533 ".U)
        //dut.clock.step ()
        println("Result is: " + dut.csignals(0).peekValue())
      }
    }
  }