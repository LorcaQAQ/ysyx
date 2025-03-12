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

#ifndef __SDB_H__
#define __SDB_H__

#include <stdint.h>
#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <stdbool.h>
uint32_t expr(char *e, bool *success);

#define NR_WP 32
#define ARRLEN(arr) (int)(sizeof(arr) / sizeof(arr[0]))
void sdb_set_batch_mode();

void sdb_mainloop() ;
void init_sdb();

typedef struct watchpoint {
	int NO;
	struct watchpoint* next;
	char expr[66532];
	uint32_t old_value;
	uint32_t new_value;

} WP;
WP *new_wp();
void free_wp(WP* wp);
extern WP *head;


#endif
