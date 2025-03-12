#include <memory/paddr.h>
#include <stdio.h>
//#include "../utils.h"
#include <utils.h>
#include <common.h>
#include <VCore.h>
#include <isa.h>
#include "svdpi.h"
#include "VCore__Dpi.h"
extern vluint64_t main_time;
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


extern "C" int pmem_read(int addr) {
  uint32_t ret = *(uint32_t *)guest_to_host((uint32_t )addr);
  return ret;
}

extern "C" void pmem_write(int waddr, int wdata) {
  // 总是往地址为`waddr & ~0x3u`的4字节按写掩码`wmask`写入`wdata`
  // `wmask`中每比特表示`wdata`中1个字节的掩码,
  // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
  *(uint32_t *)guest_to_host((uint32_t)waddr)= (uint32_t)wdata;
  return;
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