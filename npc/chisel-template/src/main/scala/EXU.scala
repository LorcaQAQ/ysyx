// See README.md for license details.

package exu

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage
import instructions.RV32I._
import consts.Consts._

/*  
class BarrelShift(w: Int) extends Module {
  val io = new Bundle {
    val in = Input(UInt(w.W))
    val shamt = Input(UInt(log2Up(w).W))
    val isLeft = Input(Bool())
    val isArith = Input(Bool())
    val out = Output(UInt(w.W))
  }
  val leftIn = Wire(Bool())
  leftIn := Mux(io.isArith, io.in(w-1).asBool, false.B) // 右移时从左边移入的位
  def layer (din: Seq[Bool], n: Int): Seq[Bool] = { // 描述第n级选择器如何排布
    val s = 1 << n   // 需要移动的位数
    def shiftRight(i: Int) = 
      if (i + s >= w) leftIn // 描述右移时第i位输出
      else din(i + s) 
    def shiftLeft (i: Int) = 
      if (i < s) false.B // 描述左移时第i位输出
      else din(i - s) 
    val sel = Cat(io.isLeft, io.shamt(n)) // 将移位方向和移位量作为选择器的选择信号
    din.zipWithIndex.map { case (b, i) =>                // 对于每一位输入b,
      VecInit(b, shiftRight(i), b, shiftLeft(i))(sel) } // 都从4种输入中选择一种作为输出
  }
  def barrelshift(din: Seq[Bool], k: Int): Seq[Bool] = // 描述有k级的桶形移位器如何排布
    if (k == 0) din  // 若移位器只有0级, 则结果和输入相同
    // 否则实例化一个有k-1级的桶形移位器和第k-1级选择器, 并将后者的输出作为前者的输入
    else barrelshift(layer(din, k - 1), k - 1)
  io.out := Cat(barrelshift(io.in.asBools, log2Up(w)).reverse) // 实例化一个有log2(w)级的桶形移位器
}*/
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
  val eq = !diff_32.orR
  val sum = io.val1 + io.val2
  val xor=io.val1 ^ io.val2

  val diff_s= io.val1.asSInt -  io.val2.asSInt
  val sign_val1=io.val1.asSInt(31)
  val sign_val2=io.val2.asSInt(31)
  val overflow = (sign_val1 =/= sign_val2) && (sign_val2 === diff_s(31))

  val le = Mux(overflow, !diff_s(31), diff_s(31))//less than or equal 
  val less =le && !eq
  /*  val sra= Module(new BarrelShift(32))
  sra.io.in := io.val1
  sra.io.shamt := io.val2(4,0)
  sra.io.isLeft := false.B
  sra.io.isArith := true.B
  val sravalue = Wire(UInt(32.W))
  sravalue := sra.io.out*/
  val sravalue = (io.val1.asSInt >> io.val2(4,0)).asUInt
  val sllvalue = io.val1 << io.val2(4,0)
  val srlvalue = io.val1 >> io.val2(4,0)
  val andvalue = io.val1 & io.val2
  val or=io.val1 | io.val2
    io.result := MuxCase(0.U, Array(
      (io.alu_op === ALU_ADD) -> sum,
      (io.alu_op === ALU_JALR) -> ( sum &("hfffffffe".U)),
      (io.alu_op === ALU_SLTU) -> (Fill(31,0.U)##borrow).asUInt,
      (io.alu_op === ALU_BNE) -> (Fill(31,0.U)## eq).asUInt,
      (io.alu_op === ALU_SUB) -> diff_32,
      (io.alu_op === ALU_XOR) -> xor,
      (io.alu_op === ALU_SRA) -> sravalue,
      (io.alu_op === ALU_SLL) -> sllvalue,
      (io.alu_op === ALU_AND) -> andvalue,
      (io.alu_op === ALU_OR) -> or,
      (io.alu_op === ALU_BGE) -> (Fill(31,0.U)##(le)).asUInt,
      (io.alu_op === ALU_BEQ) -> (Fill(31,0.U)## !eq).asUInt,
      (io.alu_op === ALU_SRL) -> srlvalue,
      (io.alu_op === ALU_BGEU) -> (Fill(31,0.U)## borrow).asUInt,
      (io.alu_op === ALU_BLTU) -> (Fill(31,0.U)## !borrow).asUInt,
      (io.alu_op === ALU_SLT) -> (Fill(31,0.U)##less).asUInt,
      (io.alu_op === ALU_BLT) -> (Fill(31,0.U)## !less).asUInt
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

