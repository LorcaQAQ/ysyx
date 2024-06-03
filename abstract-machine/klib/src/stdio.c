#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

char* int2string(int num,char *str);

int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;
  va_start(ap,fmt);
  int fmtlen=0;
  int num;
  while(*fmt){
    switch(*fmt){
      case '%':
              fmtlen--;
              fmt++;
              break;
      case 'd':  /*integer*/
              fmtlen--;
              num=va_arg(ap,int);
              int2string(num,out);
              ++out;
              fmt++;
              break;
      case 's': /*string*/
              fmtlen--;
              char *s=va_arg(ap,char *);
              while(*s){
                *out++=*s++;
                fmtlen++;
              }
              fmt++;
              break;
      /*case ' ':
              fmtlen++;
              *out++=' ';
              break;*/
      default:
            *out++=*fmt++;
            fmtlen++;
            break;
    }
  }
  *out='\0';
  return fmtlen;
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
  if(num<0){//check whether negative number
    s[i++]='-';
    num=-num;
  }
  do{//
    s[i++]=num%10+'0';
    num/=10;
  }while(num);
  //str[i]='\0';

  int j=0;
  if(s[0]=='-'){
    j=1;
    i++;
  }
  for(;j<i/2;j++){
    s[j]=s[j]+s[i-1-j];
    s[i-1-j]=s[j]-s[i-1-j];
    s[j]=s[j]-s[i-1-j];
  }
  return s;
}
#endif
