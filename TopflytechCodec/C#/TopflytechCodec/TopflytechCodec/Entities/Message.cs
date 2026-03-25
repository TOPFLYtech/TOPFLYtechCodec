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
        private Boolean isNeedResp = true;
        public Boolean IsNeedResp
        {
            get { return isNeedResp; }
            set { isNeedResp = value; }
        }

        private int protocolHeadType;
        public int ProtocolHeadType
        {
            get { return protocolHeadType; }
            set { protocolHeadType = value; }
        }

        private int encryptType;
        public int EncryptType
        {
            get { return encryptType; }
            set { encryptType = value; }
        }

        private String protocol;
        public String Protocol
        {
            get { return protocol; }
            set { protocol = value; }
        }

        private String linkType;
        public String LinkType
        {
            get { return linkType; }
            set { linkType = value; }
        }

        private DateTime recvDate;
        public DateTime RecvDate
        {
            get { return recvDate; }
            set { recvDate = value; }
        }

        private String postResp;
        public String PostResp
        {
            get { return postResp; }
            set { postResp = value; }
        }
    }
}
