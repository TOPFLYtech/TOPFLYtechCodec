using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class RS485Message : Message
    {
        private DateTime date;
        private bool isIgnition = false;
        private int deviceId;
        private byte[] rs485Data;

        public int DeviceId
        {
            get { return deviceId; }
            set { deviceId = value; }
        }

        public byte[] Rs485Data
        {
            get { return rs485Data; }
            set { rs485Data = value; }
        }

        public DateTime Date
        {
            get { return date; }
            set { date = value; }
        }

        public bool IsIgnition
        {
            get { return isIgnition; }
            set { isIgnition = value; }
        }
    }
}
