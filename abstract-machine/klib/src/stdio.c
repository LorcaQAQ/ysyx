#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

char* int2string(int num,char *str);

int printf(const char *fmt, ...) {
  va_list ap;
  va_start(ap,fmt);
  char start[100]={};
  char *out=start;
  int n=vsprintf(out,fmt,ap);
  for(int i=0;i<n;i++){
    putch(start[i]);
  }
  va_end(ap);
  return n;
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  char *start=out;
  int num;
  while(*fmt){
    switch(*fmt){
      case '%': /*conversion specification*/
              fmt++;
              int zero_padding=0;
              while(*fmt=='0'){
                zero_padding=1;
                fmt++;
              }
              int width=0;
              while(*fmt>='0'&&*fmt<='9'){
                width=width*10+(*fmt-'0');
                fmt++;
              }
              switch(*fmt){
                case 'd':  /*integer*/
                num=va_arg(ap,int);

                //get the number of digits of the integer
                char buffer[100]={};
                char *p=buffer;
                int2string(num,buffer);
                int padding=width-strlen(p);
                if(padding<0) padding=0;
                if(zero_padding==1){
                  if(num<0){
                    *out++='-';
                    p++;
                    padding=padding-1;
                    if (padding < 0) padding = 0;
                  }
                  while(padding--) *out++='0';
                }else{
                  while(padding--) *out++=' ';
                  if (num<0) *out++ = '-';
                }
                while(*p){
                  *out++=*p++;
                }
                //free(buffer);
                fmt++;
                break;
                case 's': /*string*/
                char *s=va_arg(ap,char *);
                while(*s){
                  *out++=*s++;
                }
                fmt++;
                break;
                default:break;
              }
              break;
      default:/*keep unchanged*/
            *out++=*fmt++;
            break;
    }
  }
  va_end(ap);
  *out='\0';
  return out-start;
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;
  va_start(ap, fmt);
  int n = vsprintf(out, fmt, ap);
  va_end(ap);
  return n;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

char* int2string(int num,char *str){
  char *s=str;
  int i=0;
  int dont_need_change_order=0;
  if(num<0){//check whether negative number
    if(num==-2147483648){//special case for INT_MIN
       strcpy(s, "-2147483648");
       return s+11;
    }else{
      s[i++]='-';
      if(num<=-1&&num>=-9) dont_need_change_order=1;//special case for small negative numbers
      num=-num; 
    }
  }
  do{//
    s[i++]=num%10+'0';
    num/=10;
  }while(num);
  //str[i]='\0';

  int j=0;
  if(s[0]=='-'){
    j=1;
    if(dont_need_change_order==0) i++;    
  }
  for(;j<i/2;j++){/*inverse the order of string which is converted from an integer*/
    s[j]=s[j]+s[i-1-j];
    s[i-1-j]=s[j]-s[i-1-j];
    s[j]=s[j]-s[i-1-j];
  }
  s=s+i;
  return s;
}
#endif
