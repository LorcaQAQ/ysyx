#include <stdio.h>


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
  printf("Hello, ysyx!\n");
  return 0;
}
