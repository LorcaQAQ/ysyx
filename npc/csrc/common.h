#ifndef __COMMON_H__
#define __COMMON_H__

#define RESET_VECTOR 0x80000000
#define PG_ALIGN __attribute__((aligned(4096)))
#define CONFIG_MSIZE 0x8000000
#define CONFIG_MBASE 0x80000000

#endif