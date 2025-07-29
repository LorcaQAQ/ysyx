// See README.md for license details.

package exu

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage
import instructions.RV32I._
import consts.Consts._
import iobundle._
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
    // val alu_op = Input(UInt(ALU_LEN.W))
    // val val1 = Input(UInt(32.W))
    // val val2 = Input(UInt(32.W))
    // val result = Output(UInt(32.W))

    // val csr_alu_op = Input(UInt(CSR_ALU_CTRL_LEN.W))
    // val csr_result=Output(UInt(32.W))
    val in_from_idu = Flipped(Decoupled(new idu_to_exu))
    val in_from_regfile = Flipped(new regfile_to_exu)
    val in_from_csr = Flipped(new csr_to_exu)
    val out = Decoupled(new exu_to_mem)
  })

  io.in_from_idu.ready := true.B
  val valid_to_mem = RegInit(false.B)
  val s_idle :: s_wait_ready :: Nil = Enum(2)
  val state = RegInit(s_idle)
  valid_to_mem := MuxCase(0.U, Array(
    (io.in_from_idu.valid === true.B) -> true.B,
    (state === s_wait_ready && io.out.ready) -> false.B
  ))
  io.out.valid := valid_to_mem

  
  
  state := MuxLookup(state, s_idle)(List(
    s_idle       -> Mux(valid_to_mem, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out.ready, s_idle, s_wait_ready)
  ))


  val rd_addr=RegInit(0.U(5.W))
  val imm =RegInit(0.U(32.W))
  //write back control signals
  val rf_wen = RegInit(false.B)
  val rf_wdata_sel = RegInit(0.U(REG_DATA_LEN.W))
  val alu_op1_sel = RegInit(0.U(OP1_LEN.W))
  val alu_op2_sel = RegInit(0.U(OP2_LEN.W))
  val alu_op = RegInit(0.U(ALU_LEN.W))

  val jump_op = RegInit(0.U(JUMP_LEN.W))

  //mem control signals
  val mem_wen = RegInit(false.B)
  val mem_ren = RegInit(false.B)
  val load_store_range = RegInit(0.U(LOAD_LEN.W))

  val csr_alu_op = RegInit(0.U(CSR_ALU_CTRL_LEN.W))
  val csr_r_w_ctrl=RegInit(0.U(CSR_R_W_CTRL_LEN.W))
  val csr_r_w_addr=RegInit(0.U(12.W))

  val val1 = RegInit(0.U(32.W))
  val val2 = RegInit(0.U(32.W))
  
  
  val result = WireDefault(0.U(32.W))
  val csr_result = WireDefault(0.U(32.W))

  
  // val pc = RegEnbale(io.in_from_idu.pc,io.in_from_idu.valid && io.in_from_idu.ready,0.U(32.W))
  val pc = RegInit(0.U(32.W))
  val snpc = RegInit(0.U(32.W))
  snpc :=Mux(io.in_from_idu.valid && io.in_from_idu.ready,io.in_from_idu.bits.snpc,snpc)
  pc :=Mux(io.in_from_idu.valid && io.in_from_idu.ready,io.in_from_idu.bits.pc, pc)

  rd_addr := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.rd_addr, rd_addr)
  imm := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.imm, imm)
  rf_wen := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.rf_wen, rf_wen)
  rf_wdata_sel := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.rf_wdata_sel, rf_wdata_sel)
  alu_op := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.alu_op, alu_op)
  jump_op := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.jump_op, jump_op)
  mem_wen := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.mem_wen, mem_wen)
  mem_ren := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.mem_ren, mem_ren)
  load_store_range := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.load_store_range, load_store_range)
  csr_r_w_ctrl := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.csr_r_w_ctrl, csr_r_w_ctrl)
  csr_r_w_addr := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.csr_r_w_addr, csr_r_w_addr)

  val1 := Mux(io.in_from_idu.valid && io.in_from_idu.ready, 
  MuxLookup(io.in_from_idu.bits.alu_op1_sel, 0.U(32.W))(
        Seq(
            OP1_RS1 -> io.in_from_regfile.rs1,
            OP1_PC  -> io.in_from_idu.bits.pc //todo
        )
        ),val1)
  val2 := Mux(io.in_from_idu.valid && io.in_from_idu.ready, 
  MuxLookup(io.in_from_idu.bits.alu_op2_sel, 0.U(32.W))(
        Seq(
          OP2_RS2_IMMB -> io.in_from_regfile.rs2,
          OP2_RS2  -> io.in_from_regfile.rs2,
          OP2_IMMI -> io.in_from_idu.bits.imm,
          OP2_IMMU -> io.in_from_idu.bits.imm,
          OP2_IMMJ -> io.in_from_idu.bits.imm,
          OP2_IMMS -> io.in_from_idu.bits.imm,
          OP2_CSR ->  io.in_from_csr.csr
        )
    ),val2)


  csr_alu_op := Mux(io.in_from_idu.valid && io.in_from_idu.ready, io.in_from_idu.bits.csr_alu_op, csr_alu_op)

  val diff = Cat(0.U(1.W), val1) - Cat(0.U(1.W), val2)  // 扩展为 33 位
  val borrow = diff(32)  // 最高位为借位标志
  val diff_32 = diff(31, 0)  // 取低 32 位
  val eq = !diff_32.orR
  val sum = val1 + val2
  val xor=val1 ^ val2

  val diff_s= val1.asSInt -  val2.asSInt
  val sign_val1=val1.asSInt(31)
  val sign_val2=val2.asSInt(31)
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
  val sravalue = (val1.asSInt >> val2(4,0)).asUInt
  val sllvalue = val1 << val2(4,0)
  val srlvalue = val1 >> val2(4,0)
  val andvalue = val1 & val2
  val or=val1 | val2
  result := MuxCase(0.U, Array(
      (alu_op === ALU_ADD) -> sum,
      (alu_op === ALU_JALR) -> ( sum &("hfffffffe".U)),
      (alu_op === ALU_SLTU) -> (Fill(31,0.U)##borrow).asUInt,
      (alu_op === ALU_BNE) -> (Fill(31,0.U)## eq).asUInt,
      (alu_op === ALU_SUB) -> diff_32,
      (alu_op === ALU_XOR) -> xor,
      (alu_op === ALU_SRA) -> sravalue,
      (alu_op === ALU_SLL) -> sllvalue,
      (alu_op === ALU_AND) -> andvalue,
      (alu_op === ALU_OR) -> or,
      (alu_op === ALU_BGE) -> (Fill(31,0.U)##(le)).asUInt,
      (alu_op === ALU_BEQ) -> (Fill(31,0.U)## !eq).asUInt,
      (alu_op === ALU_SRL) -> srlvalue,
      (alu_op === ALU_BGEU) -> (Fill(31,0.U)## borrow).asUInt,
      (alu_op === ALU_BLTU) -> (Fill(31,0.U)## !borrow).asUInt,
      (alu_op === ALU_SLT) -> (Fill(31,0.U)##less).asUInt,
      (alu_op === ALU_BLT) -> (Fill(31,0.U)## !less).asUInt,
      (alu_op === ALU_COPY_CSR) -> val2
    ))
    csr_result :=  MuxCase(0.U, Array(
      (csr_alu_op === CSR_ALU_COPY) -> (val1),
      (csr_alu_op === CSR_ALU_OR)   -> (val1 | val2)
    ))

  //   //branch and jump control
  //   val jal_en = jump_op === JUMP || jump_op === JUMP_CSR
  //   val branch_en = jump_op === JUMP_COND&&
  //                   result === 0.U &&
  //                   ( alu_op ===ALU_BNE  ||   
  //                     alu_op ===ALU_BGE  ||
  //                     alu_op ===ALU_BEQ  ||
  //                     alu_op ===ALU_BGEU ||
  //                     alu_op ===ALU_BLTU ||
  //                     alu_op ===ALU_BLT  )
  //   val alu_pc = MuxCase(pc, Array(
  //   (jump_op === JUMP) -> result,
  //   (jump_op === NO_JUMP) -> pc,
  //   (jump_op === JUMP_COND) -> Mux(branch_en , pc+imm, pc),
  //   (jump_op === JUMP_CSR) -> csr_pc
  // ))
  // val jump_en = jal_en || branch_en
  // val dnpc = Mux(jump_en, alu_pc, snpc)

  io.out.bits.result :=result
  io.out.bits.csr_result := csr_result

  io.out.bits.load_store_range := load_store_range
  io.out.bits.mem_wen := mem_wen
  io.out.bits.mem_ren := mem_ren
  io.out.bits.rf_wen := rf_wen
  io.out.bits.rf_wdata_sel := rf_wdata_sel
  io.out.bits.rd_addr := rd_addr
  io.out.bits.imm := imm
  io.out.bits.jump_op := jump_op
  io.out.bits.alu_op :=alu_op
  io.out.bits.rs2 := io.in_from_regfile.rs2
  io.out.bits.snpc :=snpc
  io.out.bits.pc :=pc
  io.out.bits.csr_r_w_ctrl := csr_r_w_ctrl
  io.out.bits.csr_r_w_addr := csr_r_w_addr
  io.out.bits.csr_r_w_addr := csr_r_w_addr
  // io.out.bits.dnpc :=dnpc


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

