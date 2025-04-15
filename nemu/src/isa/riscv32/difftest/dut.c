/***************************************************************************************
* Copyright (c) 2014-2022 Zihao Yu, Nanjing University
*
* NEMU is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

#include <isa.h>
#include <cpu/difftest.h>
#include "../local-include/reg.h"
#define NR_GPR MUXDEF(CONFIG_RVE, 16, 32)
//#define FMT_WORD MUXDEF(CONFIG_ISA64, "0x%016" PRIx64, "0x%08" PRIx32)
#include <cpu/cpu.h>
bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  if (cpu.pc != ref_r->pc) {
    Log("Difftest: %s DUT's next pc = " FMT_WORD " is different from REF next pc = " FMT_WORD,
            ANSI_FMT("ERROR", ANSI_FG_RED), cpu.pc, ref_r->pc);
    return false;
  }
  for (int i = 0; i < NR_GPR; i++) {
    if(ref_r->gpr[i]!=cpu.gpr[i]) {
       Log("Difftest: %s at pc = " FMT_WORD,
            ANSI_FMT("ERROR", ANSI_FG_RED),pc);
      Log("GPR %s is different. DUT's gpr[%d] = " FMT_WORD " is different from REF gpr[%d] = " FMT_WORD,
        reg_name(i), i, cpu.gpr[i], i, ref_r->gpr[i]);
      //printf("The error happens at:"FMT_WORD"\n",pc);
      return false;
    }
  }
  if(ref_r->csr.mstatus!=cpu.csr.mstatus){
    Log("Difftest: %s at pc = " FMT_WORD,
      ANSI_FMT("ERROR", ANSI_FG_RED),pc);
    Log("Difftest: %s DUT's mstatus = " FMT_WORD " is different from REF mstatus = " FMT_WORD,
      ANSI_FMT("ERROR", ANSI_FG_RED), cpu.csr.mstatus, ref_r->csr.mstatus);
    return false;
  }
  if(ref_r->csr.mepc!=cpu.csr.mepc){
    Log("Difftest: %s at pc = " FMT_WORD,
      ANSI_FMT("ERROR", ANSI_FG_RED),pc);
    Log("Difftest: %s DUT's mepc = " FMT_WORD " is different from REF mepc = " FMT_WORD,
         ANSI_FMT("ERROR", ANSI_FG_RED), cpu.csr.mepc, ref_r->csr.mepc);
    return false;
  }
  if(ref_r->csr.mcause!=cpu.csr.mcause){
    Log("Difftest: %s at pc = " FMT_WORD,
      ANSI_FMT("ERROR", ANSI_FG_RED),pc);
    Log("Difftest: %s DUT's mcause = " FMT_WORD " is different from REF mcause = " FMT_WORD,
        ANSI_FMT("ERROR", ANSI_FG_RED), cpu.csr.mcause, ref_r->csr.mcause);
    return false;
  }
  if(ref_r->csr.mtvec!=cpu.csr.mtvec){
    Log("Difftest: %s at pc = " FMT_WORD,
      ANSI_FMT("ERROR", ANSI_FG_RED),pc);
    Log("Difftest: %s DUT's mtvec = " FMT_WORD " is different from REF mtvec = " FMT_WORD,
      ANSI_FMT("ERROR", ANSI_FG_RED), cpu.csr.mtvec, ref_r->csr.mtvec);
    return false;
  }
  return true;
}

void isa_difftest_attach() {
}
