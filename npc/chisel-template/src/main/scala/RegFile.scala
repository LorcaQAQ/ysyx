// See README.md for license details.

package regfile

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage
import iobundle._
import csr._
import consts.Consts._
class reg_display(data_width : Int,addr_width : Int) extends BlackBox(Map(
   "DATA_WIDTH" -> data_width, 
   "ADDR_WIDTH" -> addr_width
  ))with HasBlackBoxInline {
  val io = IO(new Bundle {
    val regfile = Input(UInt(((1<<addr_width)*data_width).W))

  })

  setInline("reg_display.v",
    s"""
    |module reg_display
    |#(
    | parameter DATA_WIDTH = 32,
    | parameter ADDR_WIDTH = 5
    |)
    |(
    |  input [(1<<ADDR_WIDTH)*(DATA_WIDTH)-1:0] regfile
    |);
    |
    |function bit [DATA_WIDTH-1:0] get_reg(int index);
    |  if (index >= 0 && index <(1<<ADDR_WIDTH)) begin
    |   get_reg=regfile[DATA_WIDTH*index +: DATA_WIDTH];
    |  end
    |  else begin
    |   get_reg={DATA_WIDTH{1'b0}};
    |  end
    |endfunction
    |export "DPI-C" function get_reg;
    |endmodule
    """.stripMargin)
}


class Reg_CSR(data_width : Int,addr_width : Int) extends Module {
  val io = IO(new Bundle {
    // val wdata = Input(UInt(data_width.W))
    // val waddr = Input(UInt(addr_width.W))
    // val wen = Input(Bool())
    // val raddr1 = Input(UInt(addr_width.W))
    // val raddr2 = Input(UInt(addr_width.W))
    // val rdata1 = Output(UInt(data_width.W))
    // val rdata2 = Output(UInt(data_width.W))
    val in_idu_to_regfile = Flipped(new idu_to_regfile)
    val in_idu_to_csr = Flipped(new idu_to_csr) 
    val out_regfile_to_exu = new regfile_to_exu()
    val out_csr_to_exu = new csr_to_exu()
    val in_from_mem = Flipped(Decoupled(new mem_to_wbu))
    val out_to_ifu = Decoupled(new wbu_to_ifu())
  })

  io.in_from_mem.ready := true.B
  val valid_to_ifu = RegInit(false.B)
  val s_idle :: s_wait_ready :: Nil = Enum(2)
  val state = RegInit(s_idle)
  val csr = Module(new CSRs(data_width))

  valid_to_ifu := MuxCase(0.U, Array(
    (io.in_from_mem.valid === true.B) -> true.B,
    (state === s_wait_ready && io.out_to_ifu.ready) -> false.B
  ))
  io.out_to_ifu.valid := valid_to_ifu
  
  state := MuxLookup(state, s_idle)(List(
    s_idle       -> Mux(valid_to_ifu, s_wait_ready, s_idle),
    s_wait_ready -> Mux(io.out_to_ifu.ready, s_idle, s_wait_ready)
  ))

  val alu_op = RegInit(0.U(ALU_LEN.W))
  val rf_wdata_sel = RegInit(0.U(REG_DATA_LEN.W))
  val rf_wen = RegInit(false.B)
  val rd_addr = RegInit(0.U(5.W))
  val result = RegInit(0.U(32.W))
  val mem_data = RegInit(0.U(32.W))
  val snpc = RegInit(0.U(32.W))
  val pc = RegInit(0.U(32.W))
  // val dnpc = RegInit(0.U(32.W))
  val csr_r_w_addr = RegInit(0.U(12.W))
  val csr_r_w_ctrl = RegInit(0.U(CSR_R_W_CTRL_LEN.W))

  val csr_result = RegInit(0.U(32.W))
  val jump_op = RegInit(0.U(JUMP_LEN.W))
  val imm = RegInit(0.U(32.W))

  alu_op := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.alu_op, alu_op)
  rf_wdata_sel := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.rf_wdata_sel, rf_wdata_sel)
  rf_wen := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.rf_wen, rf_wen)
  rd_addr := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.rd_addr, rd_addr)
  result := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.result, result)
  mem_data := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.mem_rdata, mem_data)
  snpc := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.snpc, snpc)
  pc := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.pc, pc)
  // dnpc := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.dnpc, dnpc)
  csr_r_w_addr := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.csr_r_w_addr, csr_r_w_addr)
  csr_r_w_ctrl := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.csr_r_w_ctrl, csr_r_w_ctrl)
  csr_result := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.csr_result, csr_result)
  jump_op := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.jump_op, jump_op)
  imm := Mux(io.in_from_mem.valid && io.in_from_mem.ready, io.in_from_mem.bits.imm, imm)

  val regfile = Reg(Vec(1 << addr_width,UInt(data_width.W)))
  val reg_display = Module(new reg_display(data_width,addr_width))


  reg_display.io.regfile := regfile.asUInt

  val wr_data = Mux(rf_wdata_sel === REG_DATA_ALU, result,
                  Mux(rf_wdata_sel === REG_DATA_MEM, mem_data,
                  Mux(rf_wdata_sel === REG_DATA_PC, snpc, 0.U)))
  regfile(rd_addr) := Mux(rf_wen, wr_data, regfile(rd_addr))


  // csr
  csr.io.csr_w_addr := csr_r_w_addr
  csr.io.csr_r_addr := io.in_idu_to_csr.csr_r_addr
  csr.io.csr_r_w_ctrl := csr_r_w_ctrl
  csr.io.csr_w_data := csr_result
  csr.io.pc := pc 

    //branch and jump control
    val jal_en = jump_op === JUMP || jump_op === JUMP_CSR
    val branch_en = jump_op === JUMP_COND&&
                    result === 0.U &&
                    ( alu_op ===ALU_BNE  ||   
                      alu_op ===ALU_BGE  ||
                      alu_op ===ALU_BEQ  ||
                      alu_op ===ALU_BGEU ||
                      alu_op ===ALU_BLTU ||
                      alu_op ===ALU_BLT  )
    val alu_pc = MuxCase(pc, Array(
    (jump_op === JUMP) -> result,
    (jump_op === NO_JUMP) -> pc,
    (jump_op === JUMP_COND) -> Mux(branch_en , pc+imm, pc),
    (jump_op === JUMP_CSR) -> csr.io.csr_pc
  ))
  val jump_en = jal_en || branch_en
  val dnpc = Mux(jump_en, alu_pc, snpc)

  io.out_csr_to_exu.csr := csr.io.csr_r_data

  //to exu
  io.out_regfile_to_exu.rs1 := regfile(io.in_idu_to_regfile.rs1_addr)
  io.out_regfile_to_exu.rs2 := regfile(io.in_idu_to_regfile.rs2_addr)
  regfile(0) := 0.U

  io.out_to_ifu.bits.alu_op := alu_op
  io.out_to_ifu.bits.jump_op := jump_op
  io.out_to_ifu.bits.result := result
  io.out_to_ifu.bits.csr_pc := csr.io.csr_pc
  io.out_to_ifu.bits.imm := imm
  io.out_to_ifu.bits.dnpc := dnpc
}



/**
 * Generate Verilog sources and save it in file GCD.v

object RegFileMain extends App {
  ChiselStage.emitSystemVerilogFile(
    new RegFile(32,5),
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-o=../", "--split-verilog")
  )
}
 */