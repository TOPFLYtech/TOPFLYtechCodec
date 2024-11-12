using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec
{
    class BytesUtils
    {
        public static int bytes2SingleShort(byte[] bytes,int offset){
            if(bytes.Length < offset + 2){
                return 0;
            }
            byte first = bytes[offset];
            byte second = bytes[offset + 1];
            int firstValue = first & 0x7F;
            int sourceValue = (first & 0x80) == 0x80 ? -32768 : 0;
            int incValue = (firstValue << 8) + (int)second;
            return sourceValue + incValue;
        }
        public static int Bytes2Short(byte[] bytes, int offset)
        {
            if (bytes != null && bytes.Length > 0 && bytes.Length > offset)
            {
                if ((bytes.Length - offset) >= 2)
                {
                    short s = (short)(bytes[offset + 1] & 0xFF);
                    return ((int)s) | ((bytes[offset] << 8) & 0xFF00);
                }
            }
            return 0;
        }

        public static float Bytes2Float(byte[] bytes, int offset)
        {
            long value;

            value = bytes[offset];
            value &= 0xff;
            value |= ((long)bytes[offset + 1] << 8);
            value &= 0xffff;
            value |= ((long)bytes[offset + 2] << 16);
            value &= 0xffffff;
            value |= ((long)bytes[offset + 3] << 24);
            byte[] tempBytes = BitConverter.GetBytes(value);
            float f = BitConverter.ToSingle(tempBytes, 0);
            return f;
        }


        public static String Bytes2HexString(byte[] bytes, int index)
        {
            if (bytes == null || bytes.Length <= 0 || index >= bytes.Length)
            {
                return null;
            }

            StringBuilder builder = new StringBuilder("");

            for (int i = index; i < bytes.Length; ++i)
            {
                String hex = (bytes[i] & 0xFF).ToString("X");
                if (hex.Length < 2)
                {
                    builder.Append('0');
                }

                builder.Append(hex);
            }
            return builder.ToString();
        }


        public static byte[] HexString2Bytes(String hexStr)
        {
            String hex = hexStr.Replace("0x", "");
            if (hex.Length % 2 != 0)
            {
                hex = "0" + hex;
            }
            int size = hex.Length / 2;
            byte[] result = new byte[size];
            for (int index = 0; index < hex.Length; index += 2)
            {
                result[index / 2] = Convert.ToByte(Convert.ToInt32(hex.Substring(index, 2), 16));
            }
            return result;

        }

        public static byte[] Short2Bytes(int number)
        {
            byte[] bytes = new byte[2];
            for (int i = 1; i >= 0;i--)
            {
                bytes[i] = (byte)(number % 256);
                number >>= 8;
            }
            return bytes;
        }


        public static int Byte2Int(byte[] bytes, int offset)
        {
            byte[] intData = new byte[4];
            intData[0] = bytes[offset + 3];
            intData[1] = bytes[offset + 2];
            intData[2] = bytes[offset + 1];
            intData[3] = bytes[offset];
            return BitConverter.ToInt32(intData, 0);
        }


        public static string GetDateStr(byte[] data, int startIndex)
        {
            byte[] dateBytes = new byte[6];
            Array.Copy(data, startIndex, dateBytes, 0, 6);
            return "20" + BytesUtils.Bytes2HexString(dateBytes, 0);
        }

        public static byte TftCrc8(byte[] ptr)
        {
            byte crc = 0xff;
            int i;
            int index = 0;
            while (index < ptr.Length)
            {
                crc ^= ptr[index];
                for (i = 0; i < 8; i++)
                {
                    if ((crc & 0x80) != 0)
                        crc = (byte)((crc << 1) ^ 0x31);
                    else
                        crc <<= 1;
                }
                index++;
            }
            return crc;
        }

        public static class IMEI
        {
            public static string Decode(byte[] bytes, int index)
            {
                if (bytes != null && index > 0 && (bytes.Length - index)>=8)
                {
                    string str = BytesUtils.Bytes2HexString(bytes, index);
                    return str.Substring(1, 15);
                }
                throw new ArgumentException("invalid bytes length & index!");
            }
             
        }


    }



}
