#include <stdio.h>
#include <stdlib.h>
#include <Vysyx_23060303_cputop.h>
#include "verilated.h"
#include "svdpi.h"
#include "Vysyx_23060303_cputop__Dpi.h"
#include <verilated_vcd_c.h>

static const uint32_t inst[]={
0b00000000001100000000000010010011,//addi $ra,$$0,0x03
0b00000000001100001000000100010011,//addi $sp,$ra,0x03
0b11101111101100010000000110010011,//addi $gp,$sp,0xefb
0b00000000000100000000000001110011
};

static uint32_t *init_mem(int n){
  uint32_t *inst_list=(uint32_t *)malloc(n * sizeof(uint32_t));
  memcpy(inst_list,inst,sizeof(inst));
  return inst_list;
}
static uint32_t pmem_read(uint32_t *inst_list,uint32_t addr) {
  uint32_t ret = inst_list[(addr-0x80000000)/4];
  return ret;
}

static void single_cycle(Vysyx_23060303_cputop* top,VerilatedContext *contextp,VerilatedVcdC *wave) {
  top->clk = 0; contextp->timeInc(1);top->eval();wave->dump(contextp->time());//simulation time
  top->clk = 1; contextp->timeInc(1);top->eval();wave->dump(contextp->time());//simulation time
}

static void reset(int n,Vysyx_23060303_cputop* top,VerilatedContext *contextp,VerilatedVcdC *wave) {
  top->rst = 1;
  while (n -- > 0) 
  {
	single_cycle(top,contextp,wave);
  }
  top->rst = 0;
  wave->dump(contextp->time());
}


void stop_simulation() {
    // 打印停止仿真的消息
    printf("Simulation stopped by DPI call.\n");
    // 停止 Verilator 仿真
    Verilated::gotFinish(true);
}

int main(int argc,char** argv){

	uint32_t* inst_list=init_mem(4);

	VerilatedContext *contextp=new VerilatedContext;
	contextp->commandArgs(argc,argv);
	Vysyx_23060303_cputop* top=new Vysyx_23060303_cputop{contextp};
	VerilatedVcdC *wave =new VerilatedVcdC;//wave

	//wave configuration
	contextp->traceEverOn(true);
	top->trace(wave,5);
	wave->open("build/top.vcd");
	top->clk=1;

  	reset(5,top,contextp,wave);

	while (!contextp->gotFinish()) { 

		//contextp->timeInc(1);//simulation time

		top->inst=pmem_read(inst_list,top->pc);
		single_cycle(top,contextp,wave);
		//wave->dump(contextp->time());//output waveform
	}
	wave->close();
	delete top;
	delete contextp;
	return 0;
}