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

/* We use the POSIX regex functions to process regular expressions.
 * Type 'man regex' for more information about POSIX regex functions.
 */
#include <regex.h>

enum {
  TK_NOTYPE = 256, TK_NUM,TK_EQ,

  /* TODO: Add more token types */

};

static struct rule {
  const char *regex;
  int token_type;
} rules[] = {

  /* TODO: Add more rules.
   * Pay attention to the precedence level of different rules.
   */

  {" +", TK_NOTYPE},    // spaces
  {"\\+", '+'},         // plus
	{"\\-", '-'},         // sub
	{"\\*", '*'},					// multi
	{"/",   '/'},	        // division
	{"\\(", '('},					// left bracket
	{"\\)", ')'},					// right bracket
	{"[0-9]+", TK_NUM},			// numbers 
  {"==", TK_EQ},        // equal
};

#define NR_REGEX ARRLEN(rules)

static regex_t re[NR_REGEX] = {};

/* Rules are used for many times.
 * Therefore we compile them only once before any usage.
 */
void init_regex() {
  int i;
  char error_msg[128];
  int ret;

  for (i = 0; i < NR_REGEX; i ++) {
    ret = regcomp(&re[i], rules[i].regex, REG_EXTENDED);
    if (ret != 0) {
      regerror(ret, &re[i], error_msg, 128);
      panic("regex compilation failed: %s\n%s", error_msg, rules[i].regex);
    }
  }
}

typedef struct token {
  int type;
  char str[32];
} Token;

static Token tokens[66532] __attribute__((used)) = {};//change from 32 to 66532
static int nr_token __attribute__((used))  = 0;

static bool make_token(char *e) {
  int position = 0;
  int i;
  regmatch_t pmatch;

  nr_token = 0;

  while (e[position] != '\0') {
    /* Try all rules one by one. */
    for (i = 0; i < NR_REGEX; i ++) {
      if (regexec(&re[i], e + position, 1, &pmatch, 0) == 0 && pmatch.rm_so == 0) {
        char *substr_start = e + position;
        int substr_len = pmatch.rm_eo;

        Log("match rules[%d] = \"%s\" at position %d with len %d: %.*s",
            i, rules[i].regex, position, substr_len, substr_len, substr_start);

        position += substr_len;

        /* TODO: Now a new token is recognized with rules[i]. Add codes
         * to record the token in the array `tokens'. For certain types
         * of tokens, some extra actions should be performed.
         */

        switch (rules[i].token_type) {
					case TK_NOTYPE:break;
					//arithematic operator
					case '+':tokens[nr_token].type='+';nr_token++;
									 break;
					case '-':tokens[nr_token].type='-';nr_token++;
									 break;
					case '*':tokens[nr_token].type='*';nr_token++;
									 break;
					case '/':tokens[nr_token].type='/';nr_token++;
									 break;
					case '(':tokens[nr_token].type='(';nr_token++;
									 break;
					case ')':tokens[nr_token].type=')';nr_token++;
									 break;
					case TK_NUM:
									 if(substr_len<=32){
										tokens[nr_token].type=TK_NUM;
										strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
										nr_token++;
									 }
									 else{
										 printf("The length of oprand should be less than 32 in position:%d\n",position);
										 return false;
									 }
									 break;
					case TK_EQ:
									 tokens[nr_token].type=TK_EQ;
									 nr_token++;
									 break;
									
          default: printf("There is no type corresponding to the expression[%d]\n",position);
									 return false;
        }

        break;
      }
    }

    if (i == NR_REGEX) {
      printf("no match at position %d\n%s\n%*.s^\n", position, e, position, "");
      return false;
    }
  }

  return true;
}
static bool check_parentheses(int p,int q){
	bool checked;//check if the parentheses are paired
	int mark=0;//a mark to record the pair of parentheses
	if(tokens[p].type=='('&&tokens[q].type==')'){
		checked=true;
	}else{
		checked=false;
	}
	for(int i=p;i<q;i++){
		if(tokens[i].type==')'){
			mark--;
		}
		else if(tokens[i].type=='('){
				mark++;
		}
		if(mark<0||(mark!=0&&i==q)){
			printf("The parentheses are not paired");
			assert(0);
		}
		else if(mark==0&&i!=q){
			//printf("The expression cannot be parenthesized by the outermost parentheses\n");
			checked=false;
		}else if(mark==0&&i==q&&checked==true){
			//printf("The expression can be parenthesized");
			return true;
		}
	}
	
	return checked;
}

static bool check_neg(int p,int q){
	if(tokens[p].type=='-'&&(check_parentheses(p,q)||p==q-1)){
		return true;
	}
	else{
		return false;
	}

}

static int position_main_operator(int p,int q){
	int position=q;
	int mark=0;
	for(int i=q;i>p;i--)
	{
		if(tokens[i].type==')')
			mark++;
		else if(tokens[i].type=='(')
			mark--;
		if((tokens[i].type=='+'||(tokens[i].type=='-'&&(tokens[i-1].type==TK_NUM||tokens[i-1].type==')'))||tokens[i].type=='*'||tokens[i].type=='/')&&mark==0)
		{//the tokens is +,-,*,/ and it is not within parenthese.
			if(tokens[position].type=='+'||tokens[position].type=='-'){
				//if the token i have chosen is + or -
				continue;
			}
			else if(tokens[position].type=='*'||tokens[position].type=='/'){
				//if the token i have chosen is * or /
				if(tokens[i].type=='+'||tokens[i].type=='-'){
					position=i;
				}
				else {
					continue;
				}
			}else{
				position=i;
			}
		}
		else{
			continue;
		}
	}
	return position;

	}


static int eval(int p,int q){	
	  if (p > q) {
			    printf("Bad expression\n");//
					assert(0);												//
				
			  }
		  else if (p == q) {
				    /* Single token.
						 *      * For now this token should be a number.
						 *           * Return the value of the number.
						 *                */
				int num;
				sscanf(tokens[p].str,"%d",&num);
				return num;
			}
			else if (check_parentheses(p, q) == true) {
					    /* The expression is surrounded by a matched pair of parentheses.
							 *      * If that is the case, just throw away the parentheses.
							 *           */
					    return eval(p + 1, q - 1);
			}
			else if(check_neg(p,q)==true){
				/*At the head of the expression, there is a negative symbol*/
				return -eval(p+1,q);
			}
				else
			{
								int val1;
								int val2;
								int op_position;
						    op_position = position_main_operator(p,q);
								val1 = eval(p, op_position - 1);
								val2 = eval(op_position + 1, q);

								switch (tokens[op_position].type) {
											case '+': return val1 + val2;
											case '-': return val1 - val2;
											case '*': return val1*val2;
											case '/': return val1/val2;
											default: assert(0);
											}
			}
}


word_t expr(char *e, bool *success) {
  if (!make_token(e)) {
    *success = false;
    return 0;
  }

  /* TODO: Insert codes to evaluate the expression. */
	if(nr_token>0) nr_token--;
  int result;
	result=eval(0,nr_token);	
	return (word_t)result;
}
