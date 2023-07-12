using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class OneWireMessage : Message
    {
        private DateTime date;
        private bool isIgnition = false;
        private string deviceId;
        private byte[] oneWireData;

        public string DeviceId
        {
            get { return deviceId; }
            set { deviceId = value; }
        }

        public byte[] OneWireData
        {
            get { return oneWireData; }
            set { oneWireData = value; }
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
