using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class NetworkInfoMessage : Message
    {
        private String networkOperator;

        public String NetworkOperator
        {
            get { return networkOperator; }
            set { networkOperator = value; }
        }
        private String accessTechnology;

        public String AccessTechnology
        {
            get { return accessTechnology; }
            set { accessTechnology = value; }
        }
        private String band;

        public String Band
        {
            get { return band; }
            set { band = value; }
        }
        private String imsi;

        public String Imsi
        {
            get { return imsi; }
            set { imsi = value; }
        }
        private DateTime date;

        public DateTime Date
        {
            get { return date; }
            set { date = value; }
        }
        private String iccid;

        public String Iccid
        {
            get { return iccid; }
            set { iccid = value; }
        }

        private String wifiSsid;
        public String WifiSsid
        {
            get { return wifiSsid; }
            set { wifiSsid = value; }
        }
        private String wifiMac;
        public String WifiMac
        {
            get { return wifiMac; }
            set { wifiMac = value; }
        }
    }
}
