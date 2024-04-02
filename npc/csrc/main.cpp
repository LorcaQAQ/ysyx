#include <stdio.h>
#include <Vlight.h>
#include <nvboard.h>
#include "verilated.h"

void single_cycle() {
	  top->clk = 0; top->eval();
		  top->clk = 1; top->eval();
}

void reset(int n) {
	  top->rst = 1;
		  while (n -- > 0) single_cycle();
			  top->rst = 0;
}

int main() {

	VerilatedContext *contextp=new VerilatedContext;
	contextp->commandArgs(argc,argv);
	Vlight *light=new Vlight{contextp};

	nvboard_bind_all_pins(&dut);
	nvboard_init();

	reset(10);
	while(1){
		single_cycle();
		nvboard_update():
	}
	nvboard_quit():
  return 0;
}
