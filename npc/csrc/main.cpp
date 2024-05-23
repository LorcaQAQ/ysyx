#include <stdio.h>
#include <stdlib.h>
#include "Vysyx_23060303_cputop.h"
#include "verilated.h"

#include <verilated_vcd_c.h>

static uint32_t inst[]={

}
static void single_cycle(Vysyx_23060303_cputop* top) {
  top.clk = 0; top.eval();
  top.clk = 1; top.eval();
}

static void reset(int n,Vysyx_23060303_cputop* top) {
  top.rst = 1;
  while (n -- > 0) single_cycle(top);
  top.rst = 0;
}


int main(int argc,char** argv){
	VerilatedContext *contextp=new VerilatedContext;
	contextp->commandArgs(argc,argv);
	Vysyx_23060303_cputop* top=new Vysyx_23060303_cputop{contextp};
	VerilatedVcdC *wave =new VerilatedVcdC;//wave
  pmem_write(paddr_t addr, int len, word_t data)
	//wave configuration
	contextp->traceEverOn(true);
	top->trace(wave,3);
	wave->open("top.vcd");

  reset(10,top);

	while (!contextp->gotFinish()) { 

		contextp->timeInc(1);//simulation time
		single_cycle(top);
		wave->dump(contextp->time());
	}
	wave->close();
	delete top;
	delete contextp;
	return 0;
}