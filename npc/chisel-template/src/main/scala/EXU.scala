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

  val diff = Cat(0.U(1.W), io.val1) - Cat(0.U(1.W), io.val2)  // 扩展为 33 位
  val borrow = diff(32)  // 最高位为借位标志
  val diff_32 = diff(31, 0)  // 取低 32 位
  val eq = diff_32.orR
  val sum = io.val1 + io.val2
    io.result := MuxCase(0.U, Array(
      (io.alu_op === ALU_ADD) -> sum,
      (io.alu_op === ALU_JALR) -> ( sum &("hfffffffe".U)),
      (io.alu_op === ALU_SLTU) -> (Fill(31,0.U)##borrow).asUInt,
      (io.alu_op === ALU_BNE) -> (Fill(31,0.U)##eq).asUInt,
      (io.alu_op === ALU_SUB) -> diff_32,
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

