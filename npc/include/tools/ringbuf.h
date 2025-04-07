
#ifndef __RINGBUF_H_
#define __RINGBUF_H_

#include <common.h>


#define INSTRUCTION_SIZE 128 // 每条指令的最大长度
#define RBUFFER_SIZE 16  // 定义缓冲区大小

typedef struct {
    char log[RBUFFER_SIZE][INSTRUCTION_SIZE];
    int bufferlength;
    int write_index;
} RingBuffer;

RingBuffer *init_RingBuffer();

int write_RingBuffer(RingBuffer *buffer,char *data);


extern RingBuffer *buffer;

#endif