using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec
{
    class TopflytechByteBuf
    {
        private byte[] selfBuf = new byte[4096];
        private int readIndex = 0;
        private int writeIndex = 0;
        private int capacity = 4096;
        private int markerReadIndex = 0;
        public void PutBuf(byte[] inBuf)
        {
            if (capacity - writeIndex >= inBuf.Length)
            {
                for (int i = 0; i < inBuf.Length; i++, writeIndex++)
                {
                    selfBuf[writeIndex] = inBuf[i];
                }
            }
            else
            {
                if (capacity - writeIndex + readIndex >= inBuf.Length)
                {
                    int currentDataLength = writeIndex - readIndex;
                    for (int i = 0; i < currentDataLength; i++)
                    {
                        selfBuf[i] = selfBuf[readIndex + i];
                    }
                    writeIndex = currentDataLength;
                    readIndex = 0;
                    markerReadIndex = 0;
                    for (int i = 0; i < inBuf.Length; i++, writeIndex++)
                    {
                        selfBuf[writeIndex] = inBuf[i];
                    }
                }
                else
                {
                    int needLength = ((writeIndex - readIndex + inBuf.Length) / 4096 + 1) * 4096;
                    byte[] tmp = new byte[needLength];
                    for (int i = 0; i < writeIndex - readIndex; i++)
                    {
                        tmp[i] = selfBuf[readIndex + i];
                    }
                    selfBuf = tmp;
                    writeIndex = writeIndex - readIndex;
                    capacity = needLength;
                    readIndex = 0;
                    markerReadIndex = 0;
                    for (int i = 0; i < inBuf.Length && inBuf[i] != (byte)'0'; i++, writeIndex++)
                    {
                        selfBuf[writeIndex] = inBuf[i];
                    }
                }
            }
        }

        public int GetReadableBytes()
        {
            return writeIndex - readIndex;
        }

        public int GetReadIndex()
        {
            return readIndex;
        }

        public byte GetByte(int index)
        {
            if (index >= writeIndex - readIndex)
            {
                return (byte)'0';
            }
            return selfBuf[readIndex + index];
        }


        public void MarkReaderIndex()
        {
            markerReadIndex = readIndex;
        }

        public void ResetReaderIndex()
        {
            readIndex = markerReadIndex;
        }

        public void SkipBytes(int length)
        {
            readIndex += length;
        }

        public byte[] ReadBytes(int length)
        {
            if (length > GetReadableBytes())
            {
                return null;
            }
            byte[] result = new byte[length];
            Array.Copy(selfBuf, readIndex, result, 0, length);
            readIndex += length;
            return result;
        }
    }
}
