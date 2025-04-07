#include <getopt.h>
#include <utils.h>
#include <memory/paddr.h>
#include <tools/disasm.h>
#include <sdb.h>
#include <tools/ringbuf.h>
#include <tools/elf_read.h>
#include <assert.h>
#include <monitor.h>
// #include <device/deivce.h>

static char *log_file = NULL;
static char *diff_so_file = NULL;
static char *img_file = NULL;
static char *elf_file=NULL;
static int difftest_port = 1234;

void init_difftest(char *ref_so_file, long img_size, int port);
#ifdef CONFIG_ITRACE_COND
RingBuffer *buffer = NULL;
#endif
int func_cnt = 0;

ELF_FUNC *func_pool=NULL;

int parse_args(int argc, char *argv[])
{
  const struct option table[] = {
      {"batch", no_argument, NULL, 'b'},
      {"log", required_argument, NULL, 'l'},
      {"diff", required_argument, NULL, 'd'},
      {"port", required_argument, NULL, 'p'},
      {"help", no_argument, NULL, 'h'},
      {"ftrace", required_argument, NULL, 'f'},
      {0, 0, NULL, 0},
  };
  int o;
  while ((o = getopt_long(argc, argv, "-bhl:d:p:f:", table, NULL)) != -1)
  {
    switch (o)
    {
    case 'b':sdb_set_batch_mode();break;
    case 'p':sscanf(optarg, "%d", &difftest_port); break;
    case 'l':log_file = optarg;break;
    case 'd':diff_so_file = optarg; break;
    case 'f':elf_file = optarg;break;
    case 1:img_file = optarg;return 0;
    default:
      printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
      printf("\t-b,--batch              run with batch mode\n");
      printf("\t-l,--log=FILE           output log to FILE\n");
      printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
      printf("\t-p,--port=PORT          run DiffTest with port PORT\n");
      printf("\n");
      exit(0);
    }
  }
  return 0;
}
long load_img()
{
  if (img_file == NULL)
  {
    printf("No image is given. Use the default build-in image.\n");
    return 15; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  if (fp == NULL)
  {
    printf("Can not open '%s'", img_file);
    assert(0);
  }

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  printf("The image is %s, size = %ld\n", img_file, size);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(RESET_VECTOR), size, 1, fp);
  assert(ret == 1);

  fclose(fp);
  return size;
}

void init_monitor(int argc, char *argv[])
{
  /* Perform some global initialization. */

  /* Parse arguments. */
  parse_args(argc, argv);

  /* Set random seed. */
  init_rand();

  /* Open the log file. */
  init_log(log_file);

  /* Initialize memory. */
  init_mem();

  // /* Initialize devices. */
  // IFDEF(CONFIG_DEVICE, init_device());

  /* Perform ISA dependent initialization. */
  init_isa();

  /* Load the elf file. */
  #ifdef CONFIG_FTRACE
  int elf_size=load_elf(elf_file); 
  if(elf_size==0){
    printf("elf is not read successively!\n");
  }
  #endif

  /* Load the image to memory. This will overwrite the built-in image. */
  long img_size = load_img();

  /* Initialize differential testing. */
  init_difftest(diff_so_file, img_size, difftest_port);

  /* Initialize the simple debugger. */
  init_sdb();

  /* Initialize the ring buffer*/
  #ifdef CONFIG_ITRACE_COND
  buffer = init_RingBuffer();
  #endif
#ifndef CONFIG_ISA_loongarch32r
  IFDEF(CONFIG_ITRACE, init_disasm(
                           MUXDEF(CONFIG_ISA_x86, "i686",
                                  MUXDEF(CONFIG_ISA_mips32, "mipsel",
                                         MUXDEF(CONFIG_ISA_riscv,
                                                MUXDEF(CONFIG_RV64, "riscv64",
                                                       "riscv32"),
                                                "bad"))) "-pc-linux-gnu"));
#endif

  /* Display welcome message. */
  // welcome();
}