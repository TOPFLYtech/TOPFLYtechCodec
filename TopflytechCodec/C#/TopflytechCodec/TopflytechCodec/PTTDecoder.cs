using System;
using System.Collections.Generic;
using System.Diagnostics;
using TopflytechCodec.Entities;

namespace TopflytechCodec
{
    public class PTTDecoder
    {
        private const int HEADER_LENGTH = 3;
        private static readonly byte[] HEARTBEAT = { 0x28, 0x28, 0x03 };
        private static readonly byte[] TALK_START = { 0x28, 0x28, 0x04 };
        private static readonly byte[] TALK_END = { 0x28, 0x28, 0x05 };
        private static readonly byte[] VOICE_DATA = { 0x28, 0x28, 0x06 };

        private readonly int encryptType;
        private readonly string aesKey;
        private TopflytechByteBuf decoderBuf = new TopflytechByteBuf();

        public PTTDecoder(int messageEncryptType, string aesKey)
        {
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
        }

        public PTTDecoder(int messageEncryptType, string aesKey, int buffSize)
        {
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
            this.decoderBuf = new TopflytechByteBuf(buffSize);
        }

        private static bool match(byte[] bytes)
        {
            Debug.Assert(bytes.Length >= HEADER_LENGTH, "command match: length is not 3!");
            return Utils.ArrayEquals(TALK_START, bytes)
                   || Utils.ArrayEquals(HEARTBEAT, bytes)
                   || Utils.ArrayEquals(TALK_END, bytes)
                   || Utils.ArrayEquals(VOICE_DATA, bytes);
        }

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
                        packageLength += 8;
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
                    if (data != null)
                    {
                        Message message = build(data);
                        if (message != null)
                        {
                            messages.Add(message);
                        }
                    }
                }
                else
                {
                    decoderBuf.SkipBytes(1);
                }
            }
            return messages;
        }

        public Message build(byte[] bytes)
        {
            if (bytes != null && bytes.Length > HEADER_LENGTH)
            {
                switch (bytes[2])
                {
                    case 0x03:
                        return parseHeartbeat(bytes);
                    case 0x04:
                        return parseTalkStartMessage(bytes);
                    case 0x05:
                        return parseTalkEndMessage(bytes);
                    case 0x06:
                        return parseVoiceMessage(bytes);
                    default:
                        return null;
                }
            }
            return null;
        }

        private VoiceMessage parseVoiceMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            string imei = BytesUtils.IMEI.Decode(bytes, 7);
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

        private TalkEndMessage parseTalkEndMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            string imei = BytesUtils.IMEI.Decode(bytes, 7);
            TalkEndMessage talkEndMessage = new TalkEndMessage();
            talkEndMessage.OrignBytes = bytes;
            talkEndMessage.SerialNo = serialNo;
            talkEndMessage.Imei = imei;
            return talkEndMessage;
        }

        private TalkStartMessage parseTalkStartMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            string imei = BytesUtils.IMEI.Decode(bytes, 7);
            TalkStartMessage talkStartMessage = new TalkStartMessage();
            talkStartMessage.OrignBytes = bytes;
            talkStartMessage.SerialNo = serialNo;
            talkStartMessage.Imei = imei;
            return talkStartMessage;
        }

        private HeartbeatMessage parseHeartbeat(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            string imei = BytesUtils.IMEI.Decode(bytes, 7);
            HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
            heartbeatMessage.OrignBytes = bytes;
            heartbeatMessage.SerialNo = serialNo;
            heartbeatMessage.Imei = imei;
            return heartbeatMessage;
        }
    }
}
