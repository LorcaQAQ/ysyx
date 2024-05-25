#include <stdio.h>
#include <Vlight.h>
#include <nvboard.h>
#include "verilated.h"

static TOP_NAME light;
void nvboard_bind_all_pins(TOP_NAME* light);

void single_cycle() {
	  light.clk = 0; light.eval();
		  light.clk = 1; light.eval();
}

void reset(int n) {
	  light.rst = 1;
		  while (n -- > 0) single_cycle();
			  light.rst = 0;
}

int main(int argc, char **argv) {

	VerilatedContext *contextp=new VerilatedContext;
	contextp->commandArgs(argc,argv);
	Vlight *light=new Vlight{contextp};

	nvboard_bind_all_pins(light);
	nvboard_init();

	reset(10);
	while(1){
		single_cycle();
		nvboard_update();
	}
	nvboard_quit();
  return 0;
}
