using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Text.RegularExpressions;
using TopflytechCodec.Entities;

namespace TopflytechCodec
{
    class Encoder
    {


        /// <summary>
        /// Get sign in msg reply byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param>
        /// <param name="needSerialNo">The need serial no</param>
        /// <param name="serialNo">The serial no</param>
        /// <param name="command">The command header</param>
        /// <returns>The msg reply byte [ ]</returns> 
        public static byte[] getSignInMsgReply(String imei, bool needSerialNo, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, needSerialNo, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }

        /// <summary>
        /// Get heartbeat msg reply byte  [ ].
        /// </summary>
        /// <param name="imei">The imei</param>
        /// <param name="needSerialNo">The need serial no</param>
        /// <param name="serialNo">The serial no</param>
        /// <param name="command">The command header</param>
        /// <returns>The msg reply byte [ ]</returns> 
        public static byte[] getHeartbeatMsgReply(String imei, bool needSerialNo, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, needSerialNo, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }

        public static byte[] getWifiMsgReply(String imei, bool needSerialNo, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, needSerialNo, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }
        public static byte[] getRs485MsgReply(String imei, bool needSerialNo, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, needSerialNo, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }
        public static byte[] getOneWireMsgReply(String imei, bool needSerialNo, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, needSerialNo, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }

        public static byte[] getNetworkMsgReply(String imei, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, true, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }
        public static byte[] getNormalMsgReply(String imei, int serialNo, byte[] command, byte[] content,int messageEncryptType, String aesKey)
        {
            byte[] data = Encode(imei, true, serialNo, command, content, 15 + content.Length);
            return Encrypt(data, messageEncryptType, aesKey);
        }
        /// <summary>
        /// Get location msg reply byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param>
        /// <param name="needSerialNo">The need serial no</param>
        /// <param name="serialNo">The serial no</param>
        /// <param name="command">The command header</param>
        /// <returns>The msg reply byte [ ]</returns> 
        public static byte[] getLocationMsgReply(String imei, bool needSerialNo, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, needSerialNo, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }


        /// <summary>
        /// Get location alarm msg reply byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param>
        /// <param name="needSerialNo">The need serial no</param>
        /// <param name="serialNo">The serial no</param>
        /// <param name="sourceAlarmCode">The source alarm code</param>
        /// <param name="command">The command header</param>
        /// <returns>The msg reply byte [ ]</returns>
        public static byte[] getLocationAlarmMsgReply(String imei, bool needSerialNo, int serialNo, int sourceAlarmCode, byte[] command, int messageEncryptType, String aesKey)
        {
            byte[] data = Encode(imei, needSerialNo, serialNo, command, new byte[] { (byte)sourceAlarmCode }, 0x10);
            return Encrypt(data, messageEncryptType, aesKey);
        }

        /// <summary>
        ///  Get gps driver behavoir msg reply byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param> 
        /// <param name="serialNo">The serial no</param>
        /// <param name="command">The command header</param>
        /// <returns>The msg reply byte [ ]</returns> 
        public static byte[] getGpsDriverBehavoirMsgReply(String imei, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, true, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }


        /// <summary>
        /// Get acceleration driver behavior msg reply byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param> 
        /// <param name="serialNo">The serial no</param>
        /// <param name="command">The command header</param>
        /// <returns>The msg reply byte [ ]</returns> 
        public static byte[] getAccelerationDriverBehaviorMsgReply(String imei, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, true, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }



        /// <summary>
        /// Get acceleration alarm msg reply byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param> 
        /// <param name="serialNo">The serial no</param>
        /// <param name="command">The command header</param>
        /// <returns>The msg reply byte [ ]</returns> 
        public static byte[] getAccelerationAlarmMsgReply(String imei, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, true, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }

        public static byte[] getBluetoothPeripheralDataMsgReply(String imei, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, true, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }


        /// <summary>
        /// Get RS232 msg reply byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param> 
        /// <param name="serialNo">The serial no</param>
        /// <param name="command">The command header</param>
        /// <returns>The msg reply byte [ ]</returns> 
        public static byte[] getRS232MsgReply(String imei, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, true, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }

        public static byte[] getObdMsgReply(String imei, int serialNo, byte[] command, int messageEncryptType, String aesKey)
        {
            String content = "";
            byte[] data = Encode(imei, true, serialNo, command, System.Text.Encoding.Default.GetBytes(content), 0x0F);
            return Encrypt(data, messageEncryptType, aesKey);
        }



        /// <summary>
        /// Get config setting msg byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param>
        /// <param name="content">The config Content,also you can use sms command</param>
        /// <param name="command">The command header</param>
        /// <returns>The config setting msg byte [ ].</returns> 
        public static byte[] getConfigSettingMsg(String imei, String content, byte[] command, int messageEncryptType, String aesKey)
        {
            byte[] data = Encode(imei, false, 1, command, (byte)1, System.Text.Encoding.Default.GetBytes(content));
            return Encrypt(data, messageEncryptType, aesKey);
        }
        public static byte[] getObdConfigSettingMsg(String imei, byte[] content, byte[] command, int messageEncryptType, String aesKey)
        {
            int length = 15 + content.Length;
            byte[] data = Encode(imei, false, 1, command, content, length);
            return Encrypt(data, messageEncryptType, aesKey);
        }

        public static byte[] get82ConfigSettingMsg(String imei, byte[] content, byte[] command, int protocolType, int messageEncryptType, String aesKey)
        {
            int length = 16 + content.Length;
            byte[] data = Encode(imei, false, 1, command, (byte)protocolType, content, length);
            return Encrypt(data, messageEncryptType, aesKey);
        }

        /// <summary>
        /// Get brocast sms msg byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param>
        /// <param name="content">The brocast content</param>
        /// <param name="command">The command header</param>
        /// <returns>The brocast setting msg byte [ ].</returns> 
        public static byte[] getBrocastSmsMsg(String imei, String content, byte[] command, int messageEncryptType, String aesKey)
        {
            byte[] data = Encode(imei, false, 1, command, (byte)2, System.Text.Encoding.Unicode.GetBytes(content));
            return Encrypt(data, messageEncryptType, aesKey);
        }


        public static byte[] getForwardSmsMsg(String imei, String phoneNumb, String content, byte[] command, int messageEncryptType, String aesKey)
        {
            byte[] numberBytes = System.Text.Encoding.GetEncoding("UTF-16LE").GetBytes(phoneNumb);
            MemoryStream memoryStream = new MemoryStream();
            try
            {
                memoryStream.Write(numberBytes, 0, numberBytes.Length);
                for (int i = numberBytes.Length / 2 - 1; i < 20; i++)
                {
                    memoryStream.Write(new byte[] { 0x00, 0x00 }, 0, 2);
                }
                byte[] contentBytes = System.Text.Encoding.BigEndianUnicode.GetBytes(content);
                memoryStream.Write(contentBytes, 0, contentBytes.Length);
                byte[] data = Encode(imei, false, 1, command, (byte)3, memoryStream.ToArray());
                return Encrypt(data, messageEncryptType, aesKey);
            }
            finally
            {
                memoryStream.Close();
            }
        }

        public static byte[] getUSSDMsg(String imei, String content, byte[] command, int messageEncryptType, String aesKey)
        {
            byte[] data = Encode(imei, false, 1, command, (byte)5, System.Text.Encoding.Default.GetBytes(content));
            return Encrypt(data, messageEncryptType, aesKey);
        }

        private static string GBToUnicode(string text)
        {
            byte[] bytes = System.Text.Encoding.Unicode.GetBytes(text);
            string lowCode = "", temp = "";
            for (int i = 0; i < bytes.Length; i++)
            {
                if (i % 2 == 0)
                {
                    temp = System.Convert.ToString(bytes[i], 16);//取出元素4编码内容（两位16进制）
                    if (temp.Length < 2) temp = "0" + temp;
                }
                else
                {
                    string mytemp = Convert.ToString(bytes[i], 16);
                    if (mytemp.Length < 2) mytemp = "0" + mytemp;
                    lowCode = lowCode + @"\u" + mytemp + temp;//取出元素4编码内容（两位16进制）
                }
            }
            return lowCode;
        }


        private static string UniconToString(string str)
        {
            Regex reg = new Regex(@"(?i)\\[uU]([0-9a-f]{4})");
            return reg.Replace(str, delegate(Match m) { return ((char)Convert.ToInt32(m.Groups[1].Value, 16)).ToString(); });

        }
        private static byte[] EncodeImei(String imei)
        {
            return BytesUtils.HexString2Bytes("0" + imei);
        }


        private static byte[] Encode(String imei, bool useSerialNo, int serialNo, byte[] command, byte protocol, byte[] content)
        {
            MemoryStream memoryStream = new MemoryStream();
            try
            {
                memoryStream.Write(command, 0, command.Length);
                int packageSize = Math.Min(0x10, 32767);
                memoryStream.Write(BytesUtils.Short2Bytes(packageSize), 0, 2);
                if (useSerialNo)
                {
                    memoryStream.Write(BytesUtils.Short2Bytes(serialNo), 0, 2);
                }
                else
                {
                    memoryStream.Write(new byte[] { 0x00, 0x01 }, 0, 2);
                }
                byte[] imeiBytes = EncodeImei(imei);
                memoryStream.Write(imeiBytes, 0, imeiBytes.Length);
                memoryStream.WriteByte(protocol);
                memoryStream.Write(content, 0, content.Length);
                return memoryStream.ToArray();
            }
            finally
            {
                memoryStream.Close();
            }
        }

        private static byte[] Encode(String imei, bool useSerialNo, int serialNo, byte[] command, byte protocol, byte[] content, int lenth)
        {
            MemoryStream memoryStream = new MemoryStream();
            try
            {
                memoryStream.Write(command, 0, command.Length);
                memoryStream.Write(BytesUtils.Short2Bytes(lenth), 0, 2);
                if (useSerialNo)
                {
                    memoryStream.Write(BytesUtils.Short2Bytes(serialNo), 0, 2);
                }
                else
                {
                    memoryStream.Write(new byte[] { 0x00, 0x01 }, 0, 2);
                }
                byte[] imeiBytes = EncodeImei(imei);
                memoryStream.Write(imeiBytes, 0, imeiBytes.Length);
                memoryStream.WriteByte(protocol);
                memoryStream.Write(content, 0, content.Length);
                return memoryStream.ToArray();
            }
            finally
            {
                memoryStream.Close();
            }
        }

        private static byte[] Encode(String imei, bool useSerialNo, int serialNo, byte[] command, byte[] content, int lenth)
        {
            MemoryStream memoryStream = new MemoryStream();
            try
            {
                memoryStream.Write(command, 0, command.Length);
                memoryStream.Write(BytesUtils.Short2Bytes(lenth), 0, 2);
                if (useSerialNo)
                {
                    memoryStream.Write(BytesUtils.Short2Bytes(serialNo), 0, 2);
                }
                else
                {
                    memoryStream.Write(new byte[] { 0x00, 0x01 }, 0, 2);
                }
                byte[] imeiBytes = EncodeImei(imei);
                memoryStream.Write(imeiBytes, 0, imeiBytes.Length);
                memoryStream.Write(content, 0, content.Length);
                return memoryStream.ToArray();
            }
            finally
            {
                memoryStream.Close();
            }
        }



        private static byte[] Encrypt(byte[] data, int messageEncryptType, String aesKey)
        {
            if (messageEncryptType == MessageEncryptType.MD5)
            {
                byte[] md5Data = Crypto.MD5(data);
                MemoryStream memoryStream = new MemoryStream();
                try
                {
                    memoryStream.Write(data, 0, data.Length);
                    memoryStream.Write(md5Data, 0, md5Data.Length);
                    return memoryStream.ToArray();
                }
                finally
                {
                    memoryStream.Close();
                }
            }
            else if (messageEncryptType == MessageEncryptType.AES)
            {
                if (aesKey == null)
                {
                    return null;
                }
                if (data.Length <= 15)
                {
                    return data;
                }
                byte[] realData = new byte[data.Length - 15];
                Array.Copy(data, 15, realData, 0, data.Length - 15);
                byte[] aesData = Crypto.AESEncrypt(realData, aesKey);
                MemoryStream memoryStream = new MemoryStream();
                try
                {
                    memoryStream.Write(data, 0, data.Length);
                    memoryStream.Write(aesData, 0, aesData.Length);
                    return memoryStream.ToArray();
                }
                finally
                {
                    memoryStream.Close();
                }
            }
            else
            {
                return data;
            }
        }
    }
}
