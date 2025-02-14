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
#include <cpu/cpu.h>
bool isa_difftest_checkregs(CPU_state *ref_r, vaddr_t pc) {
  for (int i = 0; i < NR_GPR; i++) {
    if(ref_r->gpr[i]!=cpu.gpr[i]) {
       Log("Difftest: %s at pc = " FMT_WORD,
            ANSI_FMT("ERROR", ANSI_FG_RED),pc);
        Log("The value of reg %d is different, ref: 0x%08x, dut: 0x%08x", i, ref_r->gpr[i], cpu.gpr[i]); 
      return false;
    }
  }
  return true;
}

void isa_difftest_attach() {
}
