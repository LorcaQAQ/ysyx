#include <stdio.h>

#include <tools/ringbuf.h>
RingBuffer *init_RingBuffer(){
    RingBuffer *buffer= (RingBuffer *)calloc(1,sizeof(RingBuffer));
    buffer->bufferlength=0;
    buffer->write_index=0;    
    return buffer;
}

int write_RingBuffer(RingBuffer *buffer,char *data){
    void *result = strcpy(buffer->log[(buffer->write_index)], data);
    assert(result!=NULL);
    buffer->write_index=(buffer->write_index+1) % RBUFFER_SIZE;
    if(buffer->bufferlength<RBUFFER_SIZE){
        buffer->bufferlength+=1;
    }

    return buffer->bufferlength;

}




