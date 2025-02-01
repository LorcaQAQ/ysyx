// See README.md for license details.

package exu

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage
import instructions.RV32I._
import consts.Consts._

class EXU extends Module {
  val io = IO(new Bundle {
    val alu_op = Input(UInt(ALU_LEN.W))
    val val1 = Input(UInt(32.W))
    val val2 = Input(UInt(32.W))
    val result = Output(UInt(32.W))
  })

    io.result := MuxCase(0.U, Array(
      (io.alu_op === ALU_ADD) -> (io.val1 + io.val2),
      (io.alu_op === ALU_JALR) -> ((io.val1 + io.val2)&("hfffffffe".U)),
    ))

}

/**
 * Generate Verilog sources and save it in file GCD.v
 
object EXUMain extends App {
  ChiselStage.emitSystemVerilogFile(
    new EXU,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-o=../vsrc", "--split-verilog")
  )
}
*/

