package com.topflytech.codec;

import java.nio.ByteBuffer;


class BytesUtils {

    /**
     * Converts an array of characters to a hexadecimal string
     * @param bytes bytes array
     * @param index start index
     * @return Hexadecimal string
     */
    public static String bytes2HexString(final byte[] bytes, int index) {
        if (bytes == null || bytes.length <= 0 || index >= bytes.length) {
            return null;
        }

        StringBuilder builder = new StringBuilder("");

        for (int i = index; i < bytes.length; ++i) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                builder.append('0');
            }

            builder.append(hex);
        }
        return builder.toString();
    }

    /**
     * A hexadecimal string is converted to an array of characters
     * @param hexStr Hexadecimal string
     * @return byte[]
     */
    public static byte[] hexString2Bytes(String hexStr) {
        String hex = hexStr.replace("0x", "");

        StringBuffer buffer = new StringBuffer(hex);
        if (buffer.length() % 2 != 0) {
            buffer.insert(0, '0');
        }

        final int size = buffer.length() / 2;
        byte[] bytes = new byte[size];

        for (int i = 0; i < size; ++i) {
            bytes[i] = (byte) Integer.parseInt(buffer.substring(i * 2, (i + 1) * 2), 16);
        }

        return bytes;
    }

    public static long bytes2Long(byte[] bytes) {
        long l = 0;
        for(int i = 0; i < bytes.length; i++) {
            l = l << 8;
            l |= bytes[i];
        }
        return l;
    }
    /**
     * Binary string converted to Long
     * @param binaryStr Binary string etc. "1100000"
     * @return long or 0 if error
     */
    public static long binStr2Long(final byte[] binaryStr) {
        try {
            return Long.parseLong(new String(binaryStr), 2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * unsigned short converts to byte array
     * @param number input
     * @return byte[2]
     */
    public static byte[] short2Bytes(int number) {
        byte[] bytes = new byte[2];
        for (int i = 1; i >= 0; i--) {
            bytes[i] = (byte)(number % 256);
            number >>= 8;
        }
        return bytes;
    }

    /**
     * 2字节转换为 unsigned short
     * @param bytes input ,at least 2 bytes
     * @param offset start index
     * @return unsigned short
     * @throws IllegalArgumentException  invalid bytes length!
     */
    public static int bytes2Short(byte[] bytes, int offset) {
        if (bytes != null && bytes.length > 0 && bytes.length > offset) {
            if ((bytes.length - offset) >= 2) {
                short s = (short)(bytes[offset + 1] & 0xFF);
                return ((int) s) | ((bytes[offset] << 8) & 0xFF00);
            }
        }

        throw new IllegalArgumentException("invalid bytes length!");
    }
    /**
     * 2字节转换为 signed short
     * @param bytes input ,at least 2 bytes
     * @param offset start index
     * @return signed short
     * @throws IllegalArgumentException  invalid bytes length!
     */
    public static int byte2SignedShort(byte[] bytes, int offset) {
        if (bytes != null && bytes.length > 0 && bytes.length > offset) {
            if ((bytes.length - offset) >= 2) {
                short s = (short)(bytes[offset + 1] & 0xFF);
                int value = ((int) s) | ((bytes[offset] << 8) & 0xFF00);
                if ((value & 0x8000) > 0) {
                    return value - 0x10000;
                }
                return value;
            }
        }

        throw new IllegalArgumentException("invalid bytes length!");
    }

    /**
     * Byte to integer conversion
     * @param bytes  input ,at least 4 bytes
     * @param offset start index
     * @return integer
     */
    public static int bytes2Integer(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes, offset, 4).asIntBuffer().get();
    }
    public static long unsigned4BytesToInt(byte[] buf, int pos) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = pos;
        firstByte = (0x000000FF & ((int) buf[index]));
        secondByte = (0x000000FF & ((int) buf[index + 1]));
        thirdByte = (0x000000FF & ((int) buf[index + 2]));
        fourthByte = (0x000000FF & ((int) buf[index + 3]));
        index = index + 4;
        return ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
    }
    /**
     * Convert bytes to floating point
     *
     * @param bytes input ,at least 4 bytes
     * @param offset start index
     * @return float result
     */
    public static float bytes2Float(byte[] bytes, int offset) {
        int value;

        value = bytes[offset];
        value &= 0xff;
        value |= ((long) bytes[offset + 1] << 8);
        value &= 0xffff;
        value |= ((long) bytes[offset + 2] << 16);
        value &= 0xffffff;
        value |= ((long) bytes[offset + 3] << 24);

        return Float.intBitsToFloat(value);
    }


    public static class IMEI {

        /**
         * @param bytes the input byte
         * @param index start index
         * @return imei
         */
        public static String decode(byte[] bytes, int index) {
            if (bytes != null && index > 0 && (bytes.length - index) >= 8) {
                String str = BytesUtils.bytes2HexString(bytes, index);

                return str.substring(1, 16);
            }
            throw new IllegalArgumentException("invalid bytes length & index!");
        }

        /**
         * @param imei the imei
         * @return byte
         */
        public static byte[] encode(String imei) {
            assert imei != null && 15 == imei.length() : "invalid imei length!";
            return BytesUtils.hexString2Bytes("0" + imei);
        }
    }


    public static byte tftCrc8(byte[] ptr) {
        byte crc = (byte) 0xff;
        int i;
        int index = 0;
        while (index < ptr.length) {
            crc ^= ptr[index];
            for (i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0)
                    crc = (byte) ((crc << 1) ^ 0x31);
                else
                    crc <<= 1;
            }
            index++;
        }
        return crc;
    }

}
