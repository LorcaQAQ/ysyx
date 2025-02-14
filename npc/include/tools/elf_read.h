#ifndef __ELF_READ_H_
#define __ELF_READ_H_



#include <elf.h>
#define FUNC_NUM 30 


typedef struct elf_function_list {
  char name[50];
  struct elf_function_list *next;
  Elf32_Addr addr;
  Elf32_Addr offset;
} ELF_FUNC;

extern ELF_FUNC func_pool[FUNC_NUM];//to create a function pool
extern int func_cnt;//to estimate the number of function
#endif


