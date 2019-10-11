using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    /// <summary>
    /// Message is the base class for all decoded messages
    /// </summary>
    public abstract class Message
    {
        private String imei;
        public System.String Imei
        {
            get { return imei; }
            set { imei = value; }
        }
        private int serialNo;
        /// <summary>
        /// The serial number of the message,The serial number is counted on the device
        /// </summary>
        public int SerialNo
        {
            get { return serialNo; }
            set { serialNo = value; }
        }
        private byte[] orignBytes; 
        /// <summary>
        /// The original bytes.
        /// </summary>
        public byte[] OrignBytes
        {
            get { return orignBytes; }
            set { orignBytes = value; }
        }
    }
}
