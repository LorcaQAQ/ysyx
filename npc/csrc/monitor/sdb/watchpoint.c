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

#include "sdb.h"

#define NR_WP 32

static WP wp_pool[NR_WP] = {};
static WP *free_ = NULL;
WP *head=NULL;

void init_wp_pool() {
  int i;
  for (i = 0; i < NR_WP; i ++) {
    wp_pool[i].NO = i;
    wp_pool[i].next = (i == NR_WP - 1 ? NULL : &wp_pool[i + 1]);
  }

  head = NULL;
  free_ = wp_pool;
}

/* TODO: Implement the functionality of watchpoint */
WP *new_wp(){
	if(free_==NULL){
		printf("There is no free space to save a watchpoint!\n");
		assert(0);
	}else{

		WP* wp=free_;
		free_ = free_->next;
		if (head == NULL) {
			//It is the first watchpoint to set
			head = wp;
			head->next = NULL;
		}
		else {
			//Set new watchpoint at the end of "head"
			WP* cur = head;
			while (cur->next != NULL) {
				cur = cur->next;
			}
			//Set the "next" of the new watchpoint to be NULL
			cur->next = wp;
			wp->next = NULL;
		}
		return wp;
	}
}

void free_wp(WP* wp) {
	WP* pre = head;
	if (free_ != NULL) {
		if (wp == head) {//wp is the first one in head
			head = head->next;
			wp->next = free_->next;
			free_->next = wp;
		}
		else {
			while (pre->next != wp) {//to find the pre-element of wp
				pre = pre->next;
			}
			pre->next = wp->next;
			wp->next = free_->next;
			free_->next = wp;
		}
	}
	else {//no element in free_
		if (wp == head) {
			head = head->next;
		}
		else {
			while (pre->next != wp) {
				pre = pre->next;
			}
			pre->next = wp->next;
			wp->next = free_->next;
		}
		free_ = wp;
		free_->next = NULL;
	}
}

