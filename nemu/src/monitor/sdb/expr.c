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
#include <memory/paddr.h>
enum {
  TK_NOTYPE = 256, TK_NUM,EQU,NEG,REG,HEX,NEQ,LEQ,GEQ,OR,AND,NOT,LESS,GREATER,DEREF

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
	{"\\/",   '/'},	        // division
	{"\\(", '('},					// left bracket
	{"\\)", ')'},					// right bracket
	{"[0][xX0-9a-fA-F]+",HEX},//hex  
	{"[0-9]+", TK_NUM},			// numbers 
  {"==", EQU},        // equal
	{"\\${1,2}[a-z]*[0-9]*",REG},		//reg
	
	{"\\!=",NEQ},							//not equal
	{"\\<\\=",LEQ},   				//less than or euqal
	{"\\>\\=",GEQ}, 					//greater or equal
	{"\\|\\|",OR},						//logic or
	{"\\&\\&",AND},						//logic and
	{"\\!",NOT},							//logic not
	{"\\<",LESS},							//LESS		
	{">",GREATER},					//GREATER
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
					case EQU:
									 tokens[nr_token].type=EQU;
									 nr_token++;
									 break;
					case REG:
									 tokens[nr_token].type=REG;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
									 nr_token++;
									 break;
					case HEX:
									 tokens[nr_token].type=HEX;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
									 nr_token++;
									 break;
					case NEQ:
									 tokens[nr_token].type=NEQ;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
									 nr_token++;
									 break;
					case LEQ:
									 tokens[nr_token].type=LEQ;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
									 nr_token++;
									 break;
					case GEQ:
									 tokens[nr_token].type=GEQ;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
									 nr_token++;
									 break;
					case OR:
									 tokens[nr_token].type=OR;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
									 nr_token++;
									 break;
					case AND:
									 tokens[nr_token].type=AND;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
									 nr_token++;
									 break;
					case NOT:
									 tokens[nr_token].type=NOT;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
									 nr_token++;
									 break;
					case LESS:
									 tokens[nr_token].type=LESS;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
									 nr_token++;
									 break;
					case GREATER:
									 tokens[nr_token].type=GREATER;
									 strncpy(tokens[nr_token].str,&e[position-substr_len],substr_len);
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


static int position_main_operator(int p,int q){
	int position=q;
	int mark=0;
	for(int i=q;i>p;i--)
	{
		if(tokens[i].type==')')
			mark++;
		else if(tokens[i].type=='(')
			mark--;
		if((tokens[i].type==AND||tokens[i].type==AND)&&mark==0){//the token is &&,||
			position=i;
			break;
		}
		else if((tokens[i].type==EQU||tokens[i].type==NEQ||tokens[i].type==LEQ||tokens[i].type==GEQ||tokens[i].type==LESS||tokens[i].type==GREATER)&&mark==0){
			position=i;
			break;
		}
		else if((tokens[i].type=='+'||tokens[i].type=='-'||tokens[i].type=='*'||tokens[i].type=='/')&&mark==0)
		{//the tokens is +,-,*,/ and it is not within parenthese.
			if(tokens[i].type=='+'||tokens[i].type=='-'){
				//if the token i is + or -
				position=i;
				break;
			}
			else if(tokens[position].type=='*'||tokens[position].type=='/'){
				//if the token i have chosen is * or /
				if(tokens[i].type=='+'||tokens[i].type=='-'){
					position=i;
					break;
				}
				else {
					continue;
				}
			}else{
				position=i;
			}
		}
	}
	return position;

	}


static int eval(int p,int q){	
	  if (p > q) {
			printf("Bad expression at p:%d and q:%d\n",p,q);//
		  assert(0);												//
				
		}
		else if (p == q) {
			/* Single token.
			 *      * For now this token should be a number.
			 *           * Return the value of the number.
			 *                */
			int num;
			switch(tokens[q].type){
				case TK_NUM:
					sscanf(tokens[q].str,"%d",&num);
					return num;
				case REG:
					bool success;
					num=isa_reg_str2val(tokens[q].str,&success);
					if(success==false){
						printf("False return val from register\n");
						assert(0);
					}
					return num;
				case HEX:
					num=strtol(tokens[q].str,NULL,16);
					return num;
				default:printf("There doesn't exist any type to match the token");assert(0);
			}
		}
		else if (check_parentheses(p, q) == true) {
				/* The expression is surrounded by a matched pair of parentheses.
				 *      * If that is the case, just throw away the parentheses.
				 *           */

			return eval(p + 1, q - 1);

		}
		else{
								int val1=1;
								int val2=1;
								int op_position;
								op_position = position_main_operator(p,q);
								if(op_position==q){
									switch(tokens[p].type){
										case NEG: return -eval(p+1,q);
										case DEREF: return paddr_read(eval(p+1,q),4);
										case NOT: return !eval(p+1,q);
									}
								}
								else{
									val1 = eval(p, op_position - 1);
									val2 = eval(op_position + 1, q);
								}


								switch (tokens[op_position].type) {
											case '+': return val1 + val2;
											case '-': return val1 - val2;
											case '*': return val1*val2;
											case '/': return val1/val2;
											case EQU: return val1==val2;
											case NEQ: return val1!=val2;
											case LEQ: return val1<=val2;
											case GEQ: return val1>=val2;
											case OR:  return val1||val2;
											case AND: return val1&&val2;
											case LESS:return val1<val2;
											case GREATER: return val1>val2;
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
	if (tokens[0].type=='-'){
			tokens[0].type=NEG;
		}
	for(int i=1;i<nr_token;i++){
		if(tokens[i].type=='-'&&!(tokens[i-1].type==TK_NUM||tokens[i-1].type==')')){
			tokens[i].type=NEG;
		}	
	}
	for(int i=0;i<nr_token;i++){
		if(tokens[i].type=='*'&&(i==0||!(tokens[i-1].type==TK_NUM||tokens[i-1].type==')'||tokens[i-1].type==REG||tokens[i-1].type==HEX))){
			tokens[i].type=DEREF;
		}	
	}
  int result;
	result=eval(0,nr_token);	
	return (word_t)result;
}
