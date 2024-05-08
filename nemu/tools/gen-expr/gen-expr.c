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

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <string.h>

// this should be enough
static char buf[65536] = {};
static char code_buf[65536 + 128] = {}; // a little larger than `buf`
static char *code_format =
"#include <stdio.h>\n"
"int main() { "
"  unsigned result = %s; "
"  printf(\"%%u\", result); "
"  return 0; "
"}";
static int expr_len=0;
static int choose(int n);
static void gen(char bracket);
static void gen_num();
static void gen_rand_op();
static int buf_index=0;
static void gen_space();
//static void gen_negative();
static void gen_rand_logic_not();
static void gen_rand_logic_op();
static void gen_rand_and_or_op();
static void gen_rand_expr() {
	int branch;
		if(expr_len<=65532){
			branch=choose(7);
		}else{
			branch=0;
		}
		  switch (branch) {
				  case 0: gen_num(); break;
					case 1: expr_len+=3;gen('('); gen_rand_expr(); gen(')'); break;
					case 2: expr_len+=2;gen_space();gen_rand_expr();break;
					case 3: expr_len+=2; gen_rand_logic_not();gen_rand_expr();break;
					case 4: expr_len += 4; gen_rand_expr(); gen_rand_logic_op(); gen_rand_expr(); break;
					case 5:expr_len += 4; gen_rand_expr(); gen_rand_and_or_op(); gen_rand_expr(); break;
					default: expr_len+=3;gen_rand_expr(); gen_rand_op(); gen_rand_expr(); break;
			}
}

int main(int argc, char *argv[]) {
  int seed = time(0);
  srand(seed);
  int loop = 1;
  if (argc > 1) {
    sscanf(argv[1], "%d", &loop);
  }
  int i;
  for (i = 0; i < loop; i ++) {
    buf_index=0;
		expr_len=0;
    gen_rand_expr();
		buf[buf_index]='\0';
    sprintf(code_buf, code_format, buf);

    FILE *fp = fopen("/tmp/.code.c", "w");
    assert(fp != NULL);
    fputs(code_buf, fp);
    fclose(fp);

    int ret = system("gcc /tmp/.code.c -Werror -o /tmp/.expr");
    if (ret != 0) continue;

    fp = popen("/tmp/.expr", "r");
    assert(fp != NULL);

    int result;
    ret = fscanf(fp, "%d", &result);
    pclose(fp);

    printf("%u %s\n", result, buf);
  }
  return 0;
}

static void gen(char bracket){
	buf[buf_index]=bracket;
	buf_index++;
}
static void gen_num(){
	int rand_num=rand()%10;
	while(rand_num==0&&buf[buf_index]=='/'){
		rand_num=rand()%10;
	}
	char rand_num_str;
	rand_num_str=rand_num+'0';
	buf[buf_index]=rand_num_str;
	buf_index++;
}
static void gen_rand_op(){
	switch(choose(4)){
		case 0:buf[buf_index]='+';break;
		case 1:buf[buf_index]='-';break;
		case 2:buf[buf_index]='*';break;
		default:buf[buf_index]='/';break;
	}
	buf_index++;
}
static void gen_space(){
	buf[buf_index]=' ';
	buf_index++;
}

static int choose(int n){
	int randnum=rand()%n;
	return randnum;
}

/*static void gen_negative(){
	buf[buf_index]='-';
	buf_index++;
}*/
static void gen_rand_logic_op() {
	switch (choose(6)) {
	case 0:buf[buf_index++] = '>'; buf[buf_index++] = '='; break;
	case 1:buf[buf_index++] = '<'; buf[buf_index++] = '='; break;
	case 2:buf[buf_index++] = '='; buf[buf_index++] = '='; break;
	case 3:buf[buf_index++] = '!'; buf[buf_index++] = '='; break;
	case 4:buf[buf_index++] = '>'; break;
	default:buf[buf_index++] = '<'; break;
	}
}
static void gen_rand_and_or_op() {
	switch (choose(2)) {
	case 0:buf[buf_index++] = '|';buf[buf_index++] = '|'; break;
	default:buf[buf_index++] = '&'; buf[buf_index++] = '&';break;
	}
}
static void gen_rand_logic_not() {
	buf[buf_index++] = '!';
}

