#include <stdio.h>
#include <stdlib.h>
#include <Vysyx_23060303_cputop.h>
#include "verilated.h"

#include <verilated_vcd_c.h>

static const uint32_t inst[]={
0b0000000001100000000000010010011,//addi $ra,$$0,0x03
0b0000000001100001000000100010011,//addi $sp,$ra,0x03
0b1101111101100010000000110010011,//addi $gp,$sp,0x6fb
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

static void single_cycle(Vysyx_23060303_cputop* top) {
  top->clk = 0; top->eval();
  top->clk = 1; top->eval();
}

static void reset(int n,Vysyx_23060303_cputop* top) {
  top->rst = 1;
  while (n -- > 0) single_cycle(top);
  top->rst = 0;
}


int main(int argc,char** argv){

	uint32_t* inst_list=init_mem(3);

	VerilatedContext *contextp=new VerilatedContext;
	contextp->commandArgs(argc,argv);
	Vysyx_23060303_cputop* top=new Vysyx_23060303_cputop{contextp};
	VerilatedVcdC *wave =new VerilatedVcdC;//wave

	//wave configuration
	contextp->traceEverOn(true);
	top->trace(wave,5);
	wave->open("build/top.vcd");

  	reset(10,top);

	for(int i=0;i<4;i++) { 

		contextp->timeInc(1);//simulation time

		top->inst=pmem_read(inst_list,top->pc);
		single_cycle(top);
		wave->dump(contextp->time());
	}
	wave->close();
	delete top;
	delete contextp;
	return 0;
}