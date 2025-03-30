#include <am.h>
#include <nemu.h>
#include <stdio.h>
#define SYNC_ADDR (VGACTL_ADDR + 4)

static int height = 0;
static int width = 0;

void __am_gpu_init() {
  uint32_t vgactl=inl(VGACTL_ADDR);
  height = vgactl & 0xffff;  // TODO: get the correct width
  width = (vgactl >> 16)& 0xffff;  // TODO: get the correct height
  // uint32_t *fb = (uint32_t *)(uintptr_t)FB_ADDR;
  // for (i = 0; i < w * h; i ++) fb[i] = i;
  // outl(SYNC_ADDR, 1);
}

void __am_gpu_config(AM_GPU_CONFIG_T *cfg) {
  // uint32_t vgactl=inl(VGACTL_ADDR);
  // height=vgactl & 0xffff;
  // width=(vgactl >> 16)& 0xffff;
  int vmemsz=height*width*sizeof(uint32_t);
  *cfg = (AM_GPU_CONFIG_T) {
    .present = true, .has_accel = false,
    .width = width, .height = height,
    .vmemsz = vmemsz
  };
}

void __am_gpu_fbdraw(AM_GPU_FBDRAW_T *ctl) {
  int w=ctl->w, h=ctl->h, x=ctl->x, y=ctl->y;
  int offset=0;
  uint32_t *pixels=ctl->pixels;
  if (ctl->sync) {
    outl(SYNC_ADDR, 1);
  }else {
    for(int j=0;j < h; j++){
      for(int i=0;i < w; i++){
        offset=width*( y + j ) + x + i;
        outl(FB_ADDR+offset*sizeof(uint32_t),pixels[w*j+i]);
      }
    }
  }
}

void __am_gpu_status(AM_GPU_STATUS_T *status) {
  status->ready = true;
}
