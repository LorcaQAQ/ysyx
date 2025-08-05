package lfsr
import chisel3._
import chisel3.util._

class LFSR(val n: Int = 5) extends Module {
  val io = IO(new Bundle {
    val enable = Input(Bool())
    val out = Output(UInt(n.W))
  })

  // 寄存器存储当前状态
  val lfsrReg = RegInit(1.U(n.W)) // 通常不能初始化为0，1是常用初始值

  // 自定义反馈位 (X^16 + X^14 + X^13 + X^11 + 1 for 16-bit)
  val taps = Seq(4,2) // 位索引从 0 开始

  // 计算反馈值
  val feedback = taps.map(i => lfsrReg(i)).reduce(_ ^ _)

  // 移位并插入反馈位
  when(io.enable) {
    lfsrReg := Cat(lfsrReg(n - 2, 0), feedback)
  }

  io.out := lfsrReg
}

