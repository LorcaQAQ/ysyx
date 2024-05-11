#include <Vlight.h>
#include <nvboard.h>

static TOP_NAME light;

void nvboard_bind_all_pins(TOP_NAME* top);

static void single_cycle() {
  light.clk = 0; light.eval();
  light.clk = 1; light.eval();
}

static void reset(int n) {
  light.rst = 1;
  while (n -- > 0) single_cycle();
  light.rst = 0;
}

int main() {
	nvboard_bind_all_pins(&light);
  nvboard_init();
  reset(10);
  while(1) {
    nvboard_update();
    single_cycle();
  }
}
