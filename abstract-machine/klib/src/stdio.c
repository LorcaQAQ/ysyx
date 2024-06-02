#include <am.h>
#include <klib.h>
#include <klib-macros.h>
#include <stdarg.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

int printf(const char *fmt, ...) {
  panic("Not implemented");
}

int vsprintf(char *out, const char *fmt, va_list ap) {
  panic("Not implemented");
}

int sprintf(char *out, const char *fmt, ...) {
  va_list ap;
  va_start(ap,fmt);
  int n=strlen(fmt);
  for(int i=0;i<n;i++){
    if(*fmt!='%'){
      *out++=*fmt++;
    }
    else{
      if(*++fmt=='d'){
        *out++=va_arg(ap,int);
      } 
      else if(*++fmt=='s'){
        *out++=*va_arg(ap,const char*);
      } 
    }
  }
  va_end(ap);
  return n;
}

int snprintf(char *out, size_t n, const char *fmt, ...) {
  panic("Not implemented");
}

int vsnprintf(char *out, size_t n, const char *fmt, va_list ap) {
  panic("Not implemented");
}

#endif
