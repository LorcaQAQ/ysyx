#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

char* int2string(int num);

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
  char *s=NULL;
  int num;
  while(*fmt){
    switch(*fmt++){
      case '%':
              fmtlen--;
              *out=*fmt;
              break;
      case 'd':  /*integer*/
              fmtlen--;
              num=va_arg(ap,int);
              s=int2string(num);
              while(*s){
                *out++=*s++;
                fmtlen++;
              }
              break;
      case 's': /*string*/
              fmtlen--;
              s=va_arg(ap,char *);
              while(*s){
                *out++=*s++;
                fmtlen++;
              }
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

char* int2string(int num){
  int i=0;
  char *str= (char *)malloc(32 * sizeof(char));
  if(num<0){//check whether negative number
    str[i++]='-';
    num=-num;
  }
  do{//
    str[i++]=num%10+'0';
    num/=10;
  }while(num);
  str[i]='\0';

  int j=0;
  if(str[0]=='-'){
    j=1;
    i++;
  }
  for(;j<i/2;j++){
    str[j]=str[j]+str[i-1-j];
    str[i-1-j]=str[j]-str[i-1-j];
    str[j]=str[j]-str[i-1-j];
  }
  return str;
}
#endif
