using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class Rs232RfidMessage : Rs232DeviceMessage
    {
        private String rfid;

        public String Rfid
        {
            get { return rfid; }
            set { rfid = value; }
        }
    }
}
