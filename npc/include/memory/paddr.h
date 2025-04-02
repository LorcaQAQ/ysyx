#ifndef __PADDR_H__
#define __PADDR_H__


#include <stdint.h>
#include <utils.h>
#include <common.h>
#include <string.h>
#include "svdpi.h"
#include "VCore__Dpi.h"
#define PMEM_LEFT  ((paddr_t)CONFIG_MBASE)
#define PMEM_RIGHT ((paddr_t)CONFIG_MBASE + CONFIG_MSIZE - 1)
#define RESET_VECTOR (PMEM_LEFT + CONFIG_PC_RESET_OFFSET)



void init_isa();
void init_mem();
extern "C" int pmem_read(int addr);
uint8_t* guest_to_host(uint32_t paddr) ;
uint32_t paddr_read(uint32_t addr, int len);

static inline bool in_pmem(uint32_t addr) {
    return addr - CONFIG_MBASE < CONFIG_MSIZE;
  }


#endif
