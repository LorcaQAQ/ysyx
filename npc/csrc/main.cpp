#include <stdio.h>
#include <stdlib.h>
#include <VCore.h>
#include "verilated.h"
#include "svdpi.h"
#include "VCore__Dpi.h"
#include <verilated_vcd_c.h>
#include <readline/readline.h>
#include <readline/history.h>

#define RESET_VECTOR 0x80000000
#define PG_ALIGN __attribute__((aligned(4096)))
#define CONFIG_MSIZE 0x8000000
#define CONFIG_MBASE 0x80000000

static const uint32_t img[]={
0b00000000001100000000000010010011,//addi $ra,$$0,0x03
0b00000000101100001000000100010011,//addi $sp,$ra,0x03
0b11101111101100010000000110010011,//addi $gp,$sp,0xefb
0b00000000000100000000000001110011
};
static char *img_file = NULL;
static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};




static long load_img();
static void init_pmem();

uint8_t* guest_to_host(uint32_t paddr) { return pmem + paddr - CONFIG_MBASE; }
static uint32_t pmem_read(uint32_t addr);

static void single_cycle(VCore* top,VerilatedContext *contextp,VerilatedVcdC *wave);
static void reset(int n,VCore* top,VerilatedContext *contextp,VerilatedVcdC *wave);
void stop_simulation();

static char* rl_gets();


int main(int argc,char** argv){

	init_pmem();

	VerilatedContext *contextp=new VerilatedContext;
	contextp->commandArgs(argc,argv);
	VCore* top=new VCore{contextp};
	VerilatedVcdC *wave =new VerilatedVcdC;//wave

	//wave configuration
	contextp->traceEverOn(true);
	top->trace(wave,5);
	wave->open("build/top.vcd");

  	reset(4,top,contextp,wave);
	//for (; (img_file = rl_gets()) != NULL; ){
		//long img_size=load_img();
    //assert(img_size!=0);
		while (!contextp->gotFinish()) { 
			top->io_instr=pmem_read((uint32_t)top->io_pc);
			single_cycle(top,contextp,wave);
		}
			
	//}
	wave->close();	
	delete top;
	delete contextp;
	return 0;
}



static long load_img() {
  if (img_file == NULL) {
    printf("No image is given. Use the default build-in image.");
    return 4096; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  if(fp==NULL){
    printf("Can not open '%s'", img_file);
    assert(0);
  }

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  printf("The image is %s, size = %ld", img_file, size);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
  assert(ret == 1);

  fclose(fp);
  return size;
}


 void init_pmem(){
  memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));
}

static uint32_t pmem_read(uint32_t addr) {
  uint32_t ret = *(uint32_t *)guest_to_host(addr);
  return ret;
}


static void single_cycle(VCore* top,VerilatedContext *contextp,VerilatedVcdC *wave) {
  top->clock = 0; contextp->timeInc(1);top->eval();wave->dump(contextp->time());//simulation time
  top->clock = 1; contextp->timeInc(1);top->eval();wave->dump(contextp->time());//simulation time
}

static void reset(int n,VCore* top,VerilatedContext *contextp,VerilatedVcdC *wave) {
  top->reset = 1;
  while (n -- > 0) 
  {
	single_cycle(top,contextp,wave);
  }
  top->reset = 0;
  wave->dump(contextp->time());
}


void stop_simulation() {
    // 打印停止仿真的消息
    printf("Simulation stopped by DPI call.\n");
    // 停止 Verilator 仿真
    Verilated::gotFinish(true);
}

static char* rl_gets() {
  static char *line_read = NULL;

  if (line_read) {
    free(line_read);
    line_read = NULL;
  }

  line_read = readline("(Npc simulation file:) ");

  if (line_read && *line_read) {
    add_history(line_read);
  }

  return line_read;
}
