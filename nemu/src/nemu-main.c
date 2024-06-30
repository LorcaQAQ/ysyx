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

#include <common.h>

void init_monitor(int, char *[]);
void am_init_monitor();
void engine_start();
int is_exit_status_bad();
word_t expr(char *e,bool *success);

int main(int argc, char *argv[]) {
  /* Initialize the monitor. */
#ifdef CONFIG_TARGET_AM
  am_init_monitor();
#else
  init_monitor(argc, argv);
#endif

  /* Start engine. */
  engine_start();

	/*	To check expression value */
	/*FILE *fp;
	fp=fopen("./src/input.txt","r");
	assert(fp!=NULL);
	for(int i=0;!feof(fp);i++){
		word_t true_result;
		char expr_str[65536]={};
		bool success=true;
		int ret;
		ret=fscanf(fp,"%u",&true_result);
		if(ret==EOF){
			printf("All the expressions have been fetched.\n");
		}
		char *fp2=fgets(expr_str,65536,fp);
		if(fp2!=NULL){
		
			int expr_index=0;
			while(expr_str[expr_index]!='\n'){
				expr_index++;
			}
			expr_str[expr_index]='\0';
			printf("We are checking the %d'th expression\n",i);
			word_t expr_result=expr(expr_str,&success);
			if(true_result!=expr_result){
				printf("The %d'th expression isn't correct,\nresult is: %u,\nexpression result is:%u\n",i,true_result,expr_result);
				assert(0);
			}
		}else{
			printf("All the expression have been successfully evaluated.\n");
		}
	}
	fclose(fp);*/


  return is_exit_status_bad();
}
