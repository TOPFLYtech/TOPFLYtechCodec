using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class VoiceMessage : Message
    {
        private int encodeType;
        private byte[] voiceData;

        public int EncodeType
        {
            get { return encodeType; }
            set { encodeType = value; }
        }

        public byte[] VoiceData
        {
            get { return voiceData; }
            set { voiceData = value; }
        }
    }
}
