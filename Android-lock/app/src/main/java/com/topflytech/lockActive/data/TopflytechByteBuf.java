package com.topflytech.lockActive.data;

import java.util.Arrays;

/**
 * Created by admin on 2019/9/10.
 */
public class TopflytechByteBuf{
    private byte[] selfBuf = new byte[4096];
    private int readIndex = 0;
    private int writeIndex = 0;
    private int capacity = 4096;
    private int markerReadIndex = 0;
    public void putBuf(byte[] in){
        if (capacity - writeIndex >= in.length){
            for (int i = 0;i < in.length;i++,writeIndex++){
                selfBuf[writeIndex] = in[i];
            }
        }else{
            if (capacity - writeIndex + readIndex >= in.length){
                int currentDataLength = writeIndex - readIndex;
                for (int i = 0;i < currentDataLength;i++){
                    selfBuf[i] = selfBuf[readIndex + i];
                }
                writeIndex = currentDataLength;
                readIndex = 0;
                markerReadIndex = 0;
                for (int i = 0;i < in.length;i++,writeIndex++){
                    selfBuf[writeIndex] = in[i];
                }
            }else{
                int needLength = ((writeIndex - readIndex + in.length) / 4096 + 1) * 4096;
                byte[] tmp = new byte[needLength];
                for (int i = 0 ;i < writeIndex - readIndex;i++){
                    tmp[i] = selfBuf[readIndex + i];
                }
                selfBuf = tmp;
                capacity = needLength;
                writeIndex = writeIndex - readIndex;
                readIndex = 0;
                markerReadIndex = 0;
                for (int i = 0;i < in.length;i++,writeIndex++){
                    selfBuf[writeIndex] = in[i];
                }
            }
        }
    }

    public int getReadableBytes(){
        return writeIndex - readIndex;
    }

    public int getReadIndex(){
        return readIndex;
    }

    public byte getByte(int index){
        if (index >= writeIndex - readIndex){
            return '0';
        }
        return selfBuf[readIndex + index];
    }


    public void markReaderIndex(){
        markerReadIndex = readIndex;
    }

    public void resetReaderIndex(){
        readIndex = markerReadIndex;
    }

    public void skipBytes(int length){
        readIndex += length;
    }

    public byte[] readBytes(int length){
        if (length > getReadableBytes()){
            return null;
        }
        byte[] result = Arrays.copyOfRange(selfBuf, readIndex, readIndex + length);
        readIndex += length;
        return result;
    }
}
