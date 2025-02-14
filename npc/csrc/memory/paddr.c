#include <memory/paddr.h>
#include <stdio.h>
//#include "../utils.h"
#include <utils.h>
#include <common.h>
#include <VCore.h>
#include <isa.h>
static const uint32_t img[]={
0b00000000001100000000000010010011,//addi $ra,$$0,0x03
0b00000000101100001000000100010011,//addi $sp,$ra,0x03
0b11101111101100010000000110010011,//addi $gp,$sp,0xefb
0b00000000000100000000000001110011
};

static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
extern VCore* top;

static void restart() {
  /* Set the initial program counter. */
  cpu.pc = RESET_VECTOR;

  /* The zero register is always 0. */
  cpu.gpr[0] = 0;
}

static void out_of_bound(uint32_t addr) {
  printf("address = 0x%08x is out of bound of pmem [0x%08x, 0x%08x] at pc = " FMT_WORD,
      addr, PMEM_LEFT, PMEM_RIGHT, top->io_pc);
}

uint8_t* guest_to_host(uint32_t paddr) { return pmem + paddr - CONFIG_MBASE; }
void init_isa(){
  memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));
  restart();
}


uint32_t pmem_read(uint32_t addr) {
  uint32_t ret = *(uint32_t *)guest_to_host(addr);
  return ret;
}
uint32_t paddr_read(uint32_t addr, int len) {
  //IFDEF(CONFIG_MTRACE,display_mem_read(addr));
  if (in_pmem(addr)) return pmem_read(addr);
  //IFDEF(CONFIG_DEVICE, return mmio_read(addr, len));
  out_of_bound(addr);
  return 0;
}
void init_mem() {
  #if   defined(CONFIG_PMEM_MALLOC)
    pmem = malloc(CONFIG_MSIZE);
    assert(pmem);
  #endif
    IFDEF(CONFIG_MEM_RANDOM, memset(pmem, rand(), CONFIG_MSIZE));
    Log("physical memory area [" FMT_PADDR ", " FMT_PADDR "]", PMEM_LEFT, PMEM_RIGHT);
  }