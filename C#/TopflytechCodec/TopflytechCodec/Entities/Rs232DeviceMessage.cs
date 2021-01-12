using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class Rs232DeviceMessage
    {
        private byte[] rs232Data;

        public byte[] Rs232Data
        {
            get { return rs232Data; }
            set { rs232Data = value; }
        }
    }
}
