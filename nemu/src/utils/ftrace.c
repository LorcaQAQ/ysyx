#include "./elf_read.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <isa.h>

int load_elf(char *elf_file){
    FILE *fp=fopen(elf_file,"r");
    if (elf_file == NULL) {
    Log("No elf is given.");
    return 0; // built-in image size
  }
  Assert(fp, "Can not open '%s'", elf_file);
  fseek(fp, 0, SEEK_SET);
   
  Elf32_Ehdr ehdr;
  int32_t a=fread(&ehdr,sizeof(Elf32_Ehdr),1,fp);

  if (a==0)
	{
		printf("fail to read head\n");
		exit(0);
	}

	// 判断elf文件类型
	if (ehdr.e_ident[0] != 0x7F ||ehdr.e_ident[1] != 'E' ||ehdr.e_ident[2] != 'L' ||ehdr.e_ident[3] != 'F')
	{
		printf("Not a ELF file\n");
	}


  Elf32_Shdr *shdr=(Elf32_Shdr *)malloc(sizeof(Elf32_Shdr)*ehdr.e_shnum);

  if(shdr==NULL)
  {
      printf("shdr malloc failed\n");
      exit(0);
  }

  a=fseek(fp,ehdr.e_shoff,SEEK_SET);
  if(a!=0)
  {
      printf("shdr malloc failed\n");
      exit(0);
  }
  a=fread(shdr,sizeof(Elf32_Shdr)*ehdr.e_shnum,1,fp);
  /*
  printf("%ld\n",sizeof(Elf32_Shdr));
  printf("%d\n",ehdr.e_shnum);
   */ 
  if(a==0)
  {
      printf("fread  shdr failed\n");
      exit(0);
  }

  rewind(fp);
  a=fseek(fp,shdr[ehdr.e_shstrndx].sh_offset,SEEK_SET);
  if(a!=0)
  {
      printf("fseek shdr sh_offset failed\n");
      exit(0);
  }

    
  char shstrtab[shdr[ehdr.e_shstrndx].sh_size];//read the section head name table
  char *temp=shstrtab;

  a=fread(shstrtab,shdr[ehdr.e_shstrndx].sh_size,1,fp);
  uint32_t strtaboff=0;
  uint32_t symtaboff=0;
  int str_index=0;//get the index of string table
  int sym_index=0;
  for(int i=0;i<ehdr.e_shnum;i++)
  {
    temp = shstrtab;
		temp = temp + shdr[i].sh_name;
    if (strcmp(temp, ".symtab") == 0) 
    {//该section名称
		  symtaboff=shdr[i].sh_offset;
      sym_index=i;/*
      printf("节的名称: %s\n", temp);
		  printf("节首的偏移: %x\n", shdr[i].sh_offset);
      printf("符号表的词条数：%d\n",shdr[i].sh_entsize);*/
    }else if(strcmp(temp, ".strtab") == 0)
    {
        strtaboff=shdr[i].sh_offset;
        str_index=i;
        /*
        printf("节的名称: %s\n", temp);
		    printf("节首的偏移: %x\n", shdr[i].sh_offset);*/
    }
  }

  rewind(fp);
  a=fseek(fp,strtaboff,SEEK_SET);
  if(a!=0)
  {
      printf("fseek string name table offset failed\n");
      exit(0);
  }
  char strtab[shdr[str_index].sh_size];//read the section head name table
  temp=strtab;
  a=fread(strtab,shdr[str_index].sh_size,1,fp);

  rewind(fp);

  a=fseek(fp,symtaboff,SEEK_SET);
  Elf32_Sym *esym=(Elf32_Sym *)malloc(shdr[sym_index].sh_size);
  uint32_t sym_num=shdr[sym_index].sh_size / shdr[sym_index].sh_entsize;

  a=fread(esym,shdr[sym_index].sh_size,1,fp);
  for(int i=0;i<sym_num;i++)
  {
      temp=strtab;
      temp=strtab+esym[i].st_name;
        
      if(ELF32_ST_TYPE(esym[i].st_info)==STT_FUNC)
      {
        //printf("函数名:%s\t",temp);
        //strcpy(func_pool[func_cnt].name,temp);
        //printf("地址%x\t",esym[i].st_value);
        //func_pool[func_cnt].addr=esym[i].st_value;
        //printf("+%x\n",esym[i].st_size);
        //func_pool[func_cnt].offset=esym[i].st_size;
        func_cnt+=1;
      }  
  }
  func_pool=(ELF_FUNC *)malloc(sizeof(ELF_FUNC)*func_cnt);

  for(int i=0;i<sym_num;i++)
  {
      temp=strtab;
      temp=strtab+esym[i].st_name;
      int j=0; 
      if(ELF32_ST_TYPE(esym[i].st_info)==STT_FUNC)
      {
        //printf("函数名:%s\t",temp);
        func_pool[j].name = strdup(temp);
        //printf("地址%x\t",esym[i].st_value);
        func_pool[j].addr=esym[i].st_value;
        //printf("+%x\n",esym[i].st_size);
        func_pool[j].offset=esym[i].st_size;
        j+=1;
      }  
  }
return 1;
}