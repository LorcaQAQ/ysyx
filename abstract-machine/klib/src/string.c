#include <klib.h>
#include <klib-macros.h>
#include <stdint.h>

#if !defined(__ISA_NATIVE__) || defined(__NATIVE_USE_KLIB__)

size_t strlen(const char *s) {
  size_t len=0;
  assert(s);
  while(*s!='\0'){
    len++;
    s++;
  }
  return len;
}

char *strcpy(char *dst, const char *src) {
  assert((dst!=NULL)&&(src!=NULL));
  size_t i;
  for(i=0;src[i]!='\0';i++){
    dst[i]=src[i];
  }
  dst[i]='\0';
  return dst;
}

char *strncpy(char *dst, const char *src, size_t n) {
  assert((dst!=NULL)&&(src!=NULL));
  size_t i;
  for(i=0;i<n&&src[i]!='\0';i++){
    dst[i]=src[i];
  }
  for(;i<n;i++){
    dst[i]='\0';
  }
  return dst;
}

char *strcat(char *dst, const char *src) {
  size_t dst_len = strlen(dst);
  int i;

  for (i = 0 ; src[i] != '\0' ; i++)
    dst[dst_len + i] = src[i];
  dst[dst_len + i] = '\0';
  return dst;
}

int strcmp(const char *s1, const char *s2) {
   assert(s1);
   assert(s2);
   int i=0;
   while((s1[i]==s2[i])&&(s1[i]!='\0'||s2[i]!='\0')){
      i++;
   }
    return s1[i]-s2[i];

  
}

int strncmp(const char *s1, const char *s2, size_t n) {
  assert(s1);
  assert(s2);
  int i=0;
  while((s1[i]==s2[i])&&(s1[i]!='\0'||s2[i]!='\0')&&i<n){
    i++;
  }
  return s1[i]-s2[i];
}

void *memset(void *s, int c, size_t n) {
  char *schar=s;
  while(n--){
    *schar++=c;
  }
  return s;
}

void *memmove(void *dst, const void *src, size_t n) {
  char *tmp;
  const char *s=src;
  if(dst<=src){
    tmp=dst;
    while(n--){
      *tmp++=*s++;
    }
  }else{
    tmp=dst;
    tmp+=n;
    s+=n;
    while(n--){
      *--tmp=*--s;
    }
  }
  return dst;
}

void *memcpy(void *out, const void *in, size_t n) {
  const char *src=in;
  char *dst=out;
  while(n--){
    *dst++=*src++;
  }
  return out;
}

int memcmp(const void *s1, const void *s2, size_t n) {
  int res=0;
  const char *sc1=s1;
  const char *sc2=s2;
  while(n--){
    if((sc1++)-(sc2++)!=0){
      break;
    }
  }
  return res;
}

#endif
