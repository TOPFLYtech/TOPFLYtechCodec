using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace TopflytechCodec
{
    public class PTTEncoder
    {
        private int encryptType = 0;
        private string aesKey;

        public PTTEncoder(int messageEncryptType, string aesKey)
        {
            if (messageEncryptType < 0 || messageEncryptType >= 3)
            {
                throw new ArgumentException("Message encrypt type error!");
            }
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
        }

        public byte[] GetHeartbeatMsgReply(string imei, int serialNo)
        {
            byte[] command = { 0x28, 0x28, 0x03 };
            return Encoder.getHeartbeatMsgReply(imei, true, serialNo, command, encryptType, aesKey);
        }

        public byte[] GetTalkStartMsgReply(string imei, int serialNo, int status)
        {
            byte[] command = { 0x28, 0x28, 0x04 };
            byte[] content = { (byte)status };
            return Encoder.getNormalMsgReply(imei, serialNo, command, content, encryptType, aesKey);
        }

        public byte[] GetTalkEndMsgReply(string imei, int serialNo, int status)
        {
            byte[] command = { 0x28, 0x28, 0x05 };
            byte[] content = { (byte)status };
            return Encoder.getNormalMsgReply(imei, serialNo, command, content, encryptType, aesKey);
        }

        public byte[] GetVoiceData(string imei, int serialNo, int encodeType, byte[] voiceData)
        {
            byte[] command = { 0x28, 0x28, 0x06 };
            using (MemoryStream contentStream = new MemoryStream())
            {
                contentStream.WriteByte((byte)encodeType);
                contentStream.Write(BytesUtils.Short2Bytes((short)voiceData.Length), 0, 2);
                contentStream.Write(voiceData, 0, voiceData.Length);
                byte[] content = contentStream.ToArray();
                return Encoder.getNormalMsgReply(imei, serialNo, command, content, encryptType, aesKey);
            }
        }

        public byte[] GetListenStartData(string imei, int serialNo)
        {
            byte[] command = { 0x28, 0x28, 0x07 };
            return Encoder.getNormalMsgReply(imei, serialNo, command, new byte[] { }, encryptType, aesKey);
        }

        public byte[] GetListenEndData(string imei, int serialNo)
        {
            byte[] command = { 0x28, 0x28, 0x08 };
            return Encoder.getNormalMsgReply(imei, serialNo, command, new byte[] { }, encryptType, aesKey);
        }
    }
}
