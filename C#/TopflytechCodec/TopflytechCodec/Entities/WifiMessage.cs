using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class WifiMessage : Message
    {
        private DateTime date;
        public DateTime Date
        {
            get { return date; }
            set { date = value; }
        }
        private String selfMac;
        public String SelfMac
        {
            get { return selfMac; }
            set { selfMac = value; }
        }
        private String ap1Mac;
        public String Ap1Mac
        {
            get { return ap1Mac; }
            set { ap1Mac = value; }
        }
        private int ap1RSSI;
        public int Ap1RSSI
        {
            get { return ap1RSSI; }
            set { ap1RSSI = value; }
        }
        private String ap2Mac;
        public String Ap2Mac
        {
            get { return ap2Mac; }
            set { ap2Mac = value; }
        }
        private int ap2RSSI;
        public int Ap2RSSI
        {
            get { return ap2RSSI; }
            set { ap2RSSI = value; }
        }
        private String ap3Mac;
        public String Ap3Mac
        {
            get { return ap3Mac; }
            set { ap3Mac = value; }
        }
        private int ap3RSSI;
        public int Ap3RSSI
        {
            get { return ap3RSSI; }
            set { ap3RSSI = value; }
        }



    }
}
