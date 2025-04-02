#ifndef __ELF_READ_H_
#define __ELF_READ_H_



#include <elf.h>
#define FUNC_NUM 30 


typedef struct elf_function_list {
  char *name;
  struct elf_function_list *next;
  Elf32_Addr addr;
  Elf32_Addr offset;
} ELF_FUNC;

extern ELF_FUNC *func_pool;//to create a function pool
extern int func_cnt;//to estimate the number of function

int load_elf(char *elf_file);
#endif


