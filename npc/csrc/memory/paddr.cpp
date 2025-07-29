#include <memory/paddr.h>
#include <stdio.h>
#include <utils.h>
#include <common.h>
#include <VCore.h>
#include <isa.h>
#include "svdpi.h"
#include "VCore__Dpi.h"
#ifdef CONFIG_DIFFTEST
void difftest_skip_ref();
#endif
extern vluint64_t main_time;
static const uint32_t img[]={
0b00000000001100000000000010010011,//addi $ra,$$0,0x03
0b00000000101100001000000100010011,//addi	sp, ra, 11
0b11101111101100010000000110010011,//addi	gp, sp, -261
0b00000000000100000000000001110011
};

static uint8_t pmem[CONFIG_MSIZE] PG_ALIGN = {};
extern VCore* top;

void display_mem_read(paddr_t addr);
void display_mem_write(paddr_t addr, word_t data);

static void restart() {
  /* Set the initial program counter. */
  cpu.pc = RESET_VECTOR;

  /* The zero register is always 0. */
  cpu.gpr[0] = 0;
  cpu.csr.mstatus=0x1800;
}

static void out_of_bound(uint32_t addr) {
  printf("address = 0x%08x is out of bound of pmem [0x%08x, 0x%08x] at pc = " FMT_WORD"\n",
      addr, PMEM_LEFT, PMEM_RIGHT, top->io_pc);
    assert(0);
}

uint8_t* guest_to_host(uint32_t paddr) { return pmem + paddr - CONFIG_MBASE; }
void init_isa(){
  memcpy(guest_to_host(RESET_VECTOR), img, sizeof(img));
  restart();
}


extern "C" int pmem_read(int addr) {
  if (in_pmem(addr)) {
    IFDEF(CONFIG_MTRACE,display_mem_read(addr));
    uint32_t ret = *(uint32_t *)guest_to_host((uint32_t )addr);
    return ret;
  }
  #ifdef CONFIG_DEVICE
    if(addr==CONFIG_RTC_MMIO){//timer
      uint64_t us = get_time();
      uint32_t low=  (uint32_t)us;
#ifdef CONFIG_DIFFTEST
      difftest_skip_ref();
#endif
      return low;
    }else if(addr==CONFIG_RTC_MMIO+4){
      uint64_t us = get_time();
      uint32_t high=  (uint32_t)(us>>32);
#ifdef CONFIG_DIFFTEST
      difftest_skip_ref();
#endif
      return high;
    }else if (addr==CONFIG_SERIAL_MMIO) {
#ifdef CONFIG_DIFFTEST
      difftest_skip_ref();
#endif
      return 0;
    }
  #endif
  out_of_bound(addr);
  return 0;
}

extern "C" void pmem_write(int waddr, int wdata, char wmask) {
  // 总是往地址为`waddr & ~0x3u`的4字节按写掩码`wmask`写入`wdata`
  // `wmask`中每比特表示`wdata`中1个字节的掩码,
  // 如`wmask = 0x3`代表只写入最低2个字节, 内存中的其它字节保持不变
  //uint32_t aligned_waddr = waddr & ~0x3u;//对齐写入会引起错误

  

  if(in_pmem(waddr)){
    uint32_t new_data = *(uint32_t *)guest_to_host(waddr);
    uint8_t mask = (uint8_t)wmask;
    for(int i = 0; i < 4; i++) {
      if(mask & (1 << i)) {
        uint8_t byte_to_write = (wdata >> (i * 8)) & 0xFF;
        new_data = (new_data & ~(0xFF << (i * 8))) | (byte_to_write << (i * 8));
      }
    }
    IFDEF(CONFIG_MTRACE,display_mem_write(waddr,new_data));
    *(uint32_t *)guest_to_host((uint32_t)waddr)= new_data;
    return;
  } 
  #ifdef CONFIG_DEVICE
  if(waddr==CONFIG_SERIAL_MMIO){//timer
    uint8_t ch= wdata & 0xff;
    putc(ch, stderr);
#ifdef CONFIG_DIFFTEST
    difftest_skip_ref();
#endif
    return;
  }
  #endif
  out_of_bound(waddr);
}

uint32_t paddr_read(uint32_t addr, int len) {
  if (in_pmem(addr)) {
    return pmem_read(addr);
  }
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
void display_mem_read(paddr_t addr){
 printf(ANSI_FMT("Memory read ",ANSI_FG_YELLOW)" At " FMT_PADDR ", PC=" FMT_WORD"\n", addr, top->io_pc);
}
  
void display_mem_write(paddr_t addr, word_t data){
  printf(ANSI_FMT("Memory write",ANSI_FG_MAGENTA)" At " FMT_PADDR ", PC=" FMT_WORD", DATA is " FMT_WORD"\n", addr, top->io_pc,data);
}