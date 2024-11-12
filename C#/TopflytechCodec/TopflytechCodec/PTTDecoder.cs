using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using TopflytechCodec.Entities;

namespace TopflytechCodec
{
    public class PTTDecoder
    {
        private const int HEADER_LENGTH = 16;
        private readonly byte[] HEARTBEAT = new byte[] { 0x00, 0x00, 0x01 };
        private readonly byte[] TALK_START = new byte[] { 0x00, 0x00, 0x02 };
        private readonly byte[] TALK_END = new byte[] { 0x00, 0x00, 0x03 };
        private readonly byte[] VOICE_DATA = new byte[] { 0x00, 0x00, 0x04 };

        private readonly int encryptType;
        private readonly string aesKey;

        public PTTDecoder(int messageEncryptType, string aesKey)
        {
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
        }
        public PTTDecoder(int messageEncryptType, String aesKey, int buffSize)
        { 
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
            this.decoderBuf = new TopflytechByteBuf(buffSize);
        }


        private TopflytechByteBuf decoderBuf = new TopflytechByteBuf();

        public List<Message> decode(byte[] buf)
        {
            decoderBuf.PutBuf(buf);
            List<Message> messages = new List<Message>();
            if (decoderBuf.GetReadableBytes() < (HEADER_LENGTH + 2))
            {
                return messages;
            }
            byte[] bytes = new byte[3];
            while (decoderBuf.GetReadableBytes() > 5)
            {
                decoderBuf.MarkReaderIndex();
                bytes[0] = decoderBuf.GetByte(0);
                bytes[1] = decoderBuf.GetByte(1);
                bytes[2] = decoderBuf.GetByte(2);
                if (match(bytes))
                {
                    decoderBuf.SkipBytes(HEADER_LENGTH);
                    byte[] lengthBytes = decoderBuf.ReadBytes(2);
                    int packageLength = BytesUtils.Bytes2Short(lengthBytes, 0);
                    if (encryptType == MessageEncryptType.MD5)
                    {
                        packageLength = packageLength + 8;
                    }
                    else if (encryptType == MessageEncryptType.AES)
                    {
                        packageLength = Crypto.GetAesLength(packageLength);
                    }
                    decoderBuf.ResetReaderIndex();
                    if (packageLength <= 0)
                    {
                        decoderBuf.SkipBytes(5);
                        break;
                    }
                    if (packageLength > decoderBuf.GetReadableBytes())
                    {
                        break;
                    }
                    byte[] data = decoderBuf.ReadBytes(packageLength);
                    data = Crypto.DecryptData(data, encryptType, aesKey);
                    Message message = build(data);
                    if (message != null)
                    {
                        messages.Add(message);
                    }

                }
                else
                {
                    decoderBuf.SkipBytes(1);
                }
            }
            return messages;
        }

        private bool match(byte[] bytes)
        {
            Debug.Assert(bytes.Length >= HEADER_LENGTH, "command match: length is not 3!");
            return Utils.ArrayEquals(TALK_START, bytes)
                || Utils.ArrayEquals(HEARTBEAT, bytes)
                || Utils.ArrayEquals(TALK_END, bytes)
                || Utils.ArrayEquals(VOICE_DATA, bytes);
        }



        private Message build(byte[] messageData)
        {
            if (messageData.Length < 3 || !(messageData[0] == 0x26 && messageData[1] == 0x26))
            {
                return null;
            } 

            switch (messageData[2])
            {
                case 0x01:
                    return ParseHeartbeatMessage(messageData);
                case 0x02:
                    return ParseTalkStartMessage(messageData);
                case 0x03:
                    return ParseTalkEndMessage(messageData);
                case 0x04:
                    return ParseVoiceMessage(messageData);
                default:
                    return null;
            }
        }

        private HeartbeatMessage ParseHeartbeatMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5); 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
            heartbeatMessage.OrignBytes = bytes;
            heartbeatMessage.SerialNo = serialNo; 
            heartbeatMessage.Imei = imei;
            return heartbeatMessage;
        }

        private TalkStartMessage ParseTalkStartMessage(byte[] bytes)
        {

            int serialNo = BytesUtils.Bytes2Short(bytes, 5); 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            TalkStartMessage talkStartMessage = new TalkStartMessage();
            talkStartMessage.OrignBytes = bytes;
            talkStartMessage.SerialNo = serialNo;
            talkStartMessage.Imei = imei;
            return talkStartMessage;
        }

        private TalkEndMessage ParseTalkEndMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            TalkEndMessage talkEndMessage = new TalkEndMessage();
            talkEndMessage.OrignBytes = bytes;
            talkEndMessage.SerialNo = serialNo;
            talkEndMessage.Imei = imei;
            return talkEndMessage;
        }

        private VoiceMessage ParseVoiceMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            VoiceMessage voiceMessage = new VoiceMessage();
            voiceMessage.OrignBytes = bytes;
            voiceMessage.SerialNo = serialNo;
            voiceMessage.Imei = imei;
            voiceMessage.EncodeType = bytes[15];
            int voiceLen = BytesUtils.Bytes2Short(bytes, 16);
            if (bytes.Length >= 18 + voiceLen)
            {
                byte[] voiceData = Utils.ArrayCopyOfRange(bytes, 18, 18 + voiceLen);
                voiceMessage.VoiceData = voiceData;
            }
            return voiceMessage;
        }
         
    }
}
