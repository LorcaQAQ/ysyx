#include <Vlight.h>
#include <nvboard.h>

static TOP_NAME light;

void nvboard_bind_all_pins(TOP_NAME* top);

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
	nvboard_bind_all_pins(&light);
  nvboard_init();
  while(1) {
    nvboard_update();
    single_cycle();
  }
}
