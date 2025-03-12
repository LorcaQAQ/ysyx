#include <common.h>
#include <VCore.h>
#include "verilated.h"
#include "svdpi.h"
#include "VCore__Dpi.h"
#include <verilated_vcd_c.h>
#include <readline/readline.h>
#include <readline/history.h>
#include <getopt.h>

#include <utils.h>
#include <memory/paddr.h>
#include "monitor/monitor.h"
#include "monitor/sdb/sdb.h"
#include <generated/autoconf.h>
#include <cpu/decode.h>
#include <macro.h>
#include <tools/disasm.h>
#include <tools/ringbuf.h>
#include <tools/elf_read.h>
#include <isa.h>

#define MAX_INST_TO_PRINT 10
#define NR_GPR MUXDEF(CONFIG_RVE, 16, 32)
#define CLK_PERIOD 10    // 时钟周期（单位：ns）

static void single_cycle(VCore *top, VerilatedContext *contextp, VerilatedVcdC *wave);
static void reset(int n, VCore *top, VerilatedContext *contextp, VerilatedVcdC *wave);
extern "C" void stop_simulation();
extern "C" void npc_exec(uint32_t n);
extern void isa_reg_display();
extern void difftest_step(vaddr_t pc, vaddr_t npc);

extern svBitVecVal get_reg(int index);
extern svBitVecVal get_instr();

// global variables
VerilatedContext *contextp = new VerilatedContext;
VCore *top = NULL;
VerilatedVcdC *wave = NULL;

// #define CONFIG_WATCHPOINT 1
NPCState npc_state = {.state = NPC_STOP};
CPU_state cpu = {};

uint64_t g_nr_guest_inst = 0;
static uint64_t g_timer = 0; // unit: us
static bool g_print_step = false;

void init_monitor(int argc, char *argv[]);
int display_ftrace(Decode s, int n);
void print_space(int n);
void cpu_reg_update();
vluint64_t main_time = 0;
int main(int argc, char **argv)
{
  init_monitor(argc, argv);

  VerilatedContext *contextp = new VerilatedContext;
  contextp->commandArgs(argc, argv);
  top = new VCore{contextp};
  wave = new VerilatedVcdC; // wave
  // wave configuration
  contextp->traceEverOn(true);
  top->trace(wave, 99);
  wave->open("build/top.vcd");

  reset(3, top, contextp, wave);

  long img_size = load_img();
  assert(img_size != 0);
  sdb_mainloop();
  free(buffer);
  wave->close();
  delete top;
  delete contextp;
  return 0;
}

static void single_cycle(VCore *top, VerilatedContext *contextp, VerilatedVcdC *wave)
{
  top->clock = 1;
  top->eval();
  //wave->dump(contextp->time()); // simulation time
  //contextp->timeInc(1);
  wave->dump(main_time); // simulation time
  main_time += CLK_PERIOD / 2;
  top->clock = 0;
  top->eval();
  //wave->dump(contextp->time()); // simulation time
  //contextp->timeInc(1);
  wave->dump(main_time); // simulation time
  main_time += CLK_PERIOD / 2;
}

static void reset(int n, VCore *top, VerilatedContext *contextp, VerilatedVcdC *wave)
{
  top->reset = 1;
  n--;
  while (n-- > 0)
  {
    single_cycle(top, contextp, wave);
  }

  //add another cycle to make sure the reset is effective
  top->clock = 1;
  top->eval();
  wave->dump(main_time); // simulation time
  main_time += CLK_PERIOD / 2;
  top->clock = 0;
  top->reset = 0;
  top->eval();
  wave->dump(main_time); // simulation time
  main_time += CLK_PERIOD / 2;
}

void stop_simulation()
{
  // 打印停止仿真的消息
  printf("Simulation stopped by DPI call.\n");
  // 停止 Verilator 仿真
  contextp->gotFinish(true);
  npc_state.state = NPC_END;
  npc_state.halt_pc = top->io_pc;
  const svScope scope_reg = svGetScopeFromName("TOP.Core.regfile.reg_display");
  assert(scope_reg); // Check for nullptr if scope not found
  svSetScope(scope_reg);
  npc_state.halt_ret = get_reg(10);
}

static void trace_and_difftest(Decode *_this, uint32_t dnpc)
{
#ifdef CONFIG_ITRACE_COND
  if (ITRACE_COND)
  {
    log_write("%s\n", _this->logbuf);
  } //
#endif
  if (g_print_step)
  {
    IFDEF(CONFIG_ITRACE, puts(_this->logbuf));
  }
  IFDEF(CONFIG_DIFFTEST, difftest_step(_this->pc, dnpc));
#ifdef CONFIG_WATCHPOINT
  for (WP *cur = head; cur != NULL; cur = cur->next)
  {
    bool success = true;
    word_t value = expr(cur->expr, &success);
    if (success)
    {
      cur->old_value = cur->new_value;
      cur->new_value = value;
      if (cur->new_value != cur->old_value)
      {
        npc_state.state = NPC_STOP;
        printf("Detect changes in No.%d watchpoint:\"%s\"\nOld value=%u\nNew value=%u\n", cur->NO, cur->expr, cur->old_value, cur->new_value);
      }
    }
    else
    {
      printf("Expression error!\n");
    }
  }
#endif
}

static void exec_once(Decode *s)
{
  s->pc = top->io_pc;
  s->snpc = top->io_pc + 4;
  //top->io_instr = pmem_read((uint32_t)top->io_pc);
  single_cycle(top, contextp, wave);
  cpu_reg_update();
  s->dnpc = top->io_pc;
#ifdef CONFIG_ITRACE
  char *p = s->logbuf;
  p += snprintf(p, sizeof(s->logbuf), FMT_WORD ":", top->io_pc);
  int ilen = s->snpc - s->pc;
  int i;
  uint8_t *inst = (uint8_t *)&top->io_instr;
  for (i = ilen - 1; i >= 0; i--)
  {
    p += snprintf(p, 4, " %02x", inst[i]);
  }
  int ilen_max = MUXDEF(CONFIG_ISA_x86, 8, 4);
  int space_len = ilen_max - ilen;
  if (space_len < 0)
    space_len = 0;
  space_len = space_len * 3 + 1;
  memset(p, ' ', space_len);
  p += space_len;

#ifndef CONFIG_ISA_loongarch32r
  void disassemble(char *str, int size, uint64_t pc, uint8_t *code, int nbyte);
  disassemble(p, s->logbuf + sizeof(s->logbuf) - p,
              MUXDEF(CONFIG_ISA_x86, s->snpc, s->pc), (uint8_t *)&top->io_instr, ilen);
#else
  p[0] = '\0'; // the upstream llvm does not support loongarch32r
#endif
#endif
}

void execute(uint32_t n)
{
  Decode s;
#ifdef CONFIG_FTRACE
  int fun_hierachy = 0; // to construct function hierachy
#endif
  for (int i = 0; (i < n) && (!contextp->gotFinish()); i++)
  {
    exec_once(&s);
    g_nr_guest_inst++;
    write_RingBuffer(buffer, s.logbuf);
    trace_and_difftest(&s, top->io_pc);
#ifdef CONFIG_FTRACE
    fun_hierachy = display_ftrace(s, fun_hierachy);
#endif
    IFDEF(CONFIG_DEVICE, device_update());
    if (npc_state.state != NPC_RUNNING) break;
    const svScope scope = svGetScopeFromName("TOP.Core.get_instruction");
    assert(scope); // Check for nullptr if scope not found
    svSetScope(scope);
    if (get_instr() == 0b00000000000100000000000001110011)
    {
      stop_simulation();
      break;
    }
  }
}

void npc_exec(uint32_t n)
{
  g_print_step = (n < MAX_INST_TO_PRINT);
  switch (npc_state.state)
  {
  case NPC_END:
  case NPC_ABORT:
    printf("Program execution has ended. To restart the program, exit NPC and run again.\n");
    return;
  default:
    npc_state.state = NPC_RUNNING;
  }

  uint64_t timer_start = get_time();
  execute(n);
  uint64_t timer_end = get_time();
  g_timer += timer_end - timer_start;

  switch (npc_state.state)
  {
  case NPC_RUNNING:
    npc_state.state = NPC_STOP;
    break;
  case NPC_END:
  case NPC_ABORT:
    printf("npc: %s at pc = " FMT_WORD "\n",
           ((npc_state.state == NPC_ABORT ? ANSI_FMT("ABORT", ANSI_FG_RED) : (npc_state.halt_ret == 0 ? ANSI_FMT("HIT GOOD TRAP", ANSI_FG_GREEN) : ANSI_FMT("HIT BAD TRAP", ANSI_FG_RED)))),
           top->io_pc);
    break;
  case NPC_QUIT:
    break;
  }
  // iringbuf
  if (npc_state.halt_ret != 0)
  {
    for (int i = 0; i < buffer->bufferlength; i++)
    {
      if (i == buffer->write_index - 1)
      {
        printf(" --> %s\n", buffer->log[i]);
      }
      else
      {
        printf("     %s\n", buffer->log[i]);
      }
    }
  }
  
}

static void statistic()
{
  IFNDEF(CONFIG_TARGET_AM, setlocale(LC_NUMERIC, ""));
#define NUMBERIC_FMT MUXDEF(CONFIG_TARGET_AM, "%", "%'") PRIu64
  Log("host time spent = " NUMBERIC_FMT " us", g_timer);
  Log("total guest instructions = " NUMBERIC_FMT, g_nr_guest_inst);
  if (g_timer > 0)
    Log("simulation frequency = " NUMBERIC_FMT " inst/s", g_nr_guest_inst * 1000000 / g_timer);
  else
    Log("Finish running in less than 1 us and can not calculate the simulation frequency");
}

void assert_fail_msg()
{
  isa_reg_display();
  statistic();
}

int display_ftrace(Decode s, int n)
{
  if ((BITS(top->io_instr, 6, 0) == 0b1101111) || ((BITS(top->io_instr, 6, 0) == 0b1100111) && (BITS(top->io_instr, 14, 12) == 0b000)))
  {
    if (top->io_instr == 0x00008067)
    { // ret
      n -= 1;
      for (int i = 0; i < func_cnt; i++)
      {
        if (s.dnpc >= func_pool[i].addr && s.dnpc <= (func_pool[i].addr + func_pool[i].offset))
        {
          printf("%x:", s.pc);
          print_space(n);
          printf("ret[%s@%x]\n", func_pool[i].name, s.dnpc);
        }
      }
    }
    else
    { // jal jalr
      for (int i = 0; i < func_cnt; i++)
      {
        if (s.dnpc == func_pool[i].addr)
        {
          printf("%x:", s.pc);
          print_space(n);
          printf("call [%s@0x%x]\n", func_pool[i].name, func_pool[i].addr);
        }
      }
      n += 1;
    }
  }

  return n;
}

void print_space(int n)
{
  for (int j = 0; j < n; j++)
    printf(" ");
}

void cpu_reg_update()
{
  const svScope scope_reg = svGetScopeFromName("TOP.Core.regfile.reg_display");
  assert(scope_reg); // Check for nullptr if scope not found
  svSetScope(scope_reg);
  for(int i=0;i<NR_GPR;i++)
  {
    cpu.gpr[i]=get_reg(i);
  }
  cpu.pc=top->io_pc;
}