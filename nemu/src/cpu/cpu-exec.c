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

#include <cpu/cpu.h>
#include <cpu/decode.h>
#include <cpu/difftest.h>
#include <locale.h>
#include "../utils/ringbuf.h"
#include "../monitor/sdb/sdb.h"
#include "../utils/elf_read.h"
/* The assembly code of instructions executed is only output to the screen
 * when the number of instructions executed is less than this value.
 * This is useful when you use the `si' command.
 * You can modify this value as you want.
 */
#define MAX_INST_TO_PRINT 10

CPU_state cpu = {};
uint64_t g_nr_guest_inst = 0;
static uint64_t g_timer = 0; // unit: us
static bool g_print_step = false;
FILE *ftrace_log = NULL;

void device_update();
#ifdef CONFIG_ITRACE_COND 
int write_RingBuffer(RingBuffer *buffer,char *data); 
#endif
void print_space(int n);
int display_ftrace(Decode s, int n);

static void trace_and_difftest(Decode* _this, vaddr_t dnpc) {
#ifdef CONFIG_ITRACE_COND
    if (ITRACE_COND) { log_write("%s\n", _this->logbuf); }
#endif
    if (g_print_step) { IFDEF(CONFIG_ITRACE, puts(_this->logbuf)); }
    IFDEF(CONFIG_DIFFTEST, difftest_step(_this->pc, dnpc));
#ifdef CONFIG_WATCHPOINT
    for (WP* cur = head; cur != NULL; cur = cur->next) {
        bool success = true;
        word_t value = expr(cur->expr, &success);
        if (success) {
            cur->old_value = cur->new_value;
            cur->new_value = value;
            if (cur->new_value != cur->old_value) {
                nemu_state.state = NEMU_STOP;
                printf("Detect changes in No.%d watchpoint:\"%s\" at PC="FMT_WORD" \nOld value=%u\nNew value=%u\n", 
                    cur->NO, cur->expr,_this->pc, cur->old_value, cur->new_value);
            }
        }
        else {
            printf("Expression error!\n");
        }
    }
#endif
}

static void exec_once(Decode* s, vaddr_t pc) {
    s->pc = pc;
    s->snpc = pc;
    isa_exec_once(s);
    cpu.pc = s->dnpc;
#ifdef CONFIG_ITRACE
    char* p = s->logbuf;
    p += snprintf(p, sizeof(s->logbuf), FMT_WORD ":", s->pc);
    int ilen = s->snpc - s->pc;
    int i;
    uint8_t* inst = (uint8_t*)&s->isa.inst.val;
    for (i = ilen - 1; i >= 0; i--) {
        p += snprintf(p, 4, " %02x", inst[i]);
    }
    int ilen_max = MUXDEF(CONFIG_ISA_x86, 8, 4);
    int space_len = ilen_max - ilen;
    if (space_len < 0) space_len = 0;
    space_len = space_len * 3 + 1;
    memset(p, ' ', space_len);
    p += space_len;

#ifndef CONFIG_ISA_loongarch32r
    void disassemble(char* str, int size, uint64_t pc, uint8_t * code, int nbyte);
    disassemble(p, s->logbuf + sizeof(s->logbuf) - p,
        MUXDEF(CONFIG_ISA_x86, s->snpc, s->pc), (uint8_t*)&s->isa.inst.val, ilen);
#else
    p[0] = '\0'; // the upstream llvm does not support loongarch32r
#endif
#endif
}

static void execute(uint64_t n) {
    Decode s;
    #ifdef CONFIG_FTRACE
        int fun_hierachy=0;//to construct function hierachy
    #endif
    for (; n > 0; n--) {
        exec_once(&s, cpu.pc);
        g_nr_guest_inst++;
        #ifdef CONFIG_ITRACE_COND
            write_RingBuffer(buffer,s.logbuf);
        #endif
        trace_and_difftest(&s, cpu.pc);
        #ifdef CONFIG_FTRACE
            fun_hierachy=display_ftrace(s, fun_hierachy);
        #endif
        if (nemu_state.state != NEMU_RUNNING) break;
        IFDEF(CONFIG_DEVICE, device_update());
    }
}

static void statistic() {
    IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
#define NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
    Log("host time spent = " NUMBERIC_FMT " us", g_timer);
    Log("total guest instructions = " NUMBERIC_FMT, g_nr_guest_inst);
    if (g_timer > 0) Log("simulation frequency = " NUMBERIC_FMT " inst/s", g_nr_guest_inst * 1000000 / g_timer);
    else Log("Finish running in less than 1 us and can not calculate the simulation frequency");
}

void assert_fail_msg() {
    isa_reg_display();
    statistic();
}

/* Simulate how the CPU works. */
void cpu_exec(uint64_t n) {
    g_print_step = (n < MAX_INST_TO_PRINT);
    switch (nemu_state.state) {
    case NEMU_END: case NEMU_ABORT:
        printf("Program execution has ended. To restart the program, exit NEMU and run again.\n");
        return;
    default: nemu_state.state = NEMU_RUNNING;
    }

    uint64_t timer_start = get_time();
    ftrace_log = fopen("ftrace.log", "a");
    execute(n);
    fclose(ftrace_log);
    uint64_t timer_end = get_time();
    g_timer += timer_end - timer_start;

    switch (nemu_state.state) {
    case NEMU_RUNNING: nemu_state.state = NEMU_STOP; break;

    case NEMU_END: case NEMU_ABORT:
        Log("nemu: %s at pc = " FMT_WORD,
            (nemu_state.state == NEMU_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED) :
                (nemu_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN) :
                    ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED))),
            nemu_state.halt_pc);
        // fall through
    case NEMU_QUIT: statistic();
    //iringbuf
    #ifdef CONFIG_ITRACE_COND
    if(nemu_state.halt_ret != 0){
        for(int i=0;i<buffer->bufferlength;i++){
        if (i== buffer->write_index-1) {
            printf(" --> %s\n",buffer->log[i]);
        }
        else {
            printf("     %s\n",buffer->log[i]);
        } 
        }
    }
    free(buffer);
    #endif
    
    }
}
void print_space(int n){
    for(int j=0;j<n;j++)
        printf(" ");
}

int display_ftrace(Decode s,int n){
    if((BITS(s.isa.inst.val,6,0)==0b1101111)||((BITS(s.isa.inst.val,6,0)==0b1100111)&&(BITS(s.isa.inst.val,14,12)==0b000)))
        {
           if(s.isa.inst.val==0x00008067)
           {//ret
                n-=1;
                for(int i=0;i<func_cnt;i++)
                {
                    if(s.dnpc>=func_pool[i].addr&&s.dnpc<=(func_pool[i].addr+func_pool[i].offset))
                    {
                        printf("0X%08x:",s.pc);
                        fprintf(ftrace_log, "0X%08x:", s.pc);
                        print_space(n);
                        printf(ANSI_FMT("Ret",ANSI_FG_MAGENTA)"[%s@%x]\n",func_pool[i].name,s.dnpc);
                        fprintf(ftrace_log, "Ret [%s@%x]\n",func_pool[i].name,s.dnpc);
                    }       
                }
           }else
           {//jal jalr
            for(int i=0;i<func_cnt;i++){
                if(s.dnpc==func_pool[i].addr)
                {
                    printf("0X%08x:",s.pc);
                    fprintf(ftrace_log, "0X%08x:", s.pc);
                    print_space(n);
                    printf(ANSI_FMT("Call",ANSI_FG_CYAN)"[%s@0x%x]\n",func_pool[i].name,func_pool[i].addr);
                    fprintf(ftrace_log, "Call[%s@0x%x]\n", func_pool[i].name, func_pool[i].addr);
                }
                
            }
           n+=1;
           }
        }

    return 0;
}