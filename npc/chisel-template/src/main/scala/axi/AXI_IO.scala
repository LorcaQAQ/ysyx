package axi_io
import chisel3._
import chisel3.util._
class axi_mst_write_address extends Bundle {
    val awaddr = Output(UInt(32.W))
    val awport = Output(UInt(3.W))
}

class axi_mst_write_data extends Bundle {
    val wdata = Output(UInt(32.W))
    val wstrb = Output(UInt(4.W))
}

class axi_mst_write_response extends Bundle{
    val bresp = Input(UInt(2.W))
}

class axi_mst_read_address extends Bundle{
    val araddr = Output(UInt(32.W))
    val arport = Output(UInt(3.W))
}

class axi_mst_read_data extends Bundle{
    val rdata = Input(UInt(32.W))
    val rresp = Input(UInt(2.W))
}

object AXI_Consts{
    val AXI_DATA_WIDTH = 32
    val AXI_ADDR_WIDTH = 32
    /* * AXI response codes
     * 00: OKAY
     * 01: EXOKAY
     * 10: SLVERR
     * 11: DECERR
     */
    val RRESP_OK = 0.U(2.W)
    val RRESP_SLVERR = 2.U(2.W)
    val RRESP_DECERR = 3.U(2.W)
    /* AXI data ports */
    val DATA_PORT = "b000".U(3.W) // Data port
    val INSTR_PORT = "b001".U(3.W) // Instruction port
}


