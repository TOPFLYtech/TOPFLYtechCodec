using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class BluetoothPeripheralDataMessage:Message 
    {
        private DateTime date;

        public DateTime Date
        {
            get { return date; }
            set { date = value; }
        }
        private Boolean isHistoryData = false;

        public Boolean IsHistoryData
        {
            get { return isHistoryData; }
            set { isHistoryData = value; }
        }
        private Boolean isIgnition = false;

        public Boolean IsIgnition
        {
            get { return isIgnition; }
            set { isIgnition = value; }
        }
        private List<BleData> bleDataList;

        internal List<BleData> BleDataList
        {
            get { return bleDataList; }
            set { bleDataList = value; }
        }
        private int messageType;

        public int MessageType
        {
            get { return messageType; }
            set { messageType = value; }
        }

        private Boolean latlngValid = false;
        public Boolean LatlngValid
        {
            set { latlngValid = value; }
            get { return latlngValid; }
        }
        private Double latitude = 0.0;
        public Double Latitude
        {
            set { latitude = value; }
            get { return latitude; }
        }


        private Double longitude = 0.0;
        public Double Longitude
        {
            set { longitude = value; }
            get { return longitude; }
        }
        private Double altitude = 0.0;
        public Double Altitude
        {
            set { altitude = value; }
            get { return altitude; }
        }

        private float speed = 0.0f;
        public float Speed
        {
            set { speed = value; }
            get { return speed; }
        }
        private int azimuth = 0;
        public int Azimuth
        {
            set { azimuth = value; }
            get { return azimuth; }
        }
        private Boolean isHadLocationInfo = false;
        public Boolean IsHadLocationInfo
        {
            set { isHadLocationInfo = value; }
            get { return isHadLocationInfo; }
        }

        public static int MESSAGE_TYPE_TIRE = 0;
        public static int MESSAGE_TYPE_DRIVER = 1;
        public static int MESSAGE_TYPE_SOS = 2;
        public static int MESSAGE_TYPE_TEMP = 3;
        public static int MESSAGE_TYPE_DOOR = 4;
        public static int MESSAGE_TYPE_CTRL = 5;
        public static int MESSAGE_TYPE_FUEL = 6;
        public static int MESSAGE_TYPE_Customer2397 = 7;

        private Boolean is_4g_lbs = false;
        public Boolean Is_4g_lbs
        {
            get { return is_4g_lbs; }
            set { is_4g_lbs = value; }
        }
        private Int32 mcc_4g;
        public Int32 Mcc_4g
        {
            get { return mcc_4g; }
            set { mcc_4g = value; }
        }
        private Int32 mnc_4g;
        public Int32 Mnc_4g
        {
            get { return mnc_4g; }
            set { mnc_4g = value; }
        }
        private Int64 ci_4g;
        public Int64 Ci_4g
        {
            get { return ci_4g; }
            set { ci_4g = value; }
        }
        private Int32 earfcn_4g_1;
        public Int32 Earfcn_4g_1
        {
            get { return earfcn_4g_1; }
            set { earfcn_4g_1 = value; }
        }
        private Int32 pcid_4g_1;
        public Int32 Pcid_4g_1
        {
            get { return pcid_4g_1; }
            set { pcid_4g_1 = value; }
        }
        private Int32 earfcn_4g_2;
        public Int32 Earfcn_4g_2
        {
            get { return earfcn_4g_2; }
            set { earfcn_4g_2 = value; }
        }
        private Int32 pcid_4g_2;
        public Int32 Pcid_4g_2
        {
            get { return pcid_4g_2; }
            set { pcid_4g_2 = value; }
        }


        private Boolean is_2g_lbs = false;
        public Boolean Is_2g_lbs
        {
            get { return is_2g_lbs; }
            set { is_2g_lbs = value; }
        }
        private Int32 mcc_2g;
        public Int32 Mcc_2g
        {
            get { return mcc_2g; }
            set { mcc_2g = value; }
        }
        private Int32 mnc_2g;
        public Int32 Mnc_2g
        {
            get { return mnc_2g; }
            set { mnc_2g = value; }
        }
        private Int32 lac_2g_1;
        public Int32 Lac_2g_1
        {
            get { return lac_2g_1; }
            set { lac_2g_1 = value; }
        }
        private Int32 ci_2g_1;
        public Int32 Ci_2g_1
        {
            get { return ci_2g_1; }
            set { ci_2g_1 = value; }
        }
        private Int32 lac_2g_2;
        public Int32 Lac_2g_2
        {
            get { return lac_2g_2; }
            set { lac_2g_2 = value; }
        }
        private Int32 ci_2g_2;
        public Int32 Ci_2g_2
        {
            get { return ci_2g_2; }
            set { ci_2g_2 = value; }
        }
        private Int32 lac_2g_3;
        public Int32 Lac_2g_3
        {
            get { return lac_2g_3; }
            set { lac_2g_3 = value; }
        }
        private Int32 ci_2g_3;
        public Int32 Ci_2g_3
        {
            get { return ci_2g_3; }
            set { ci_2g_3 = value; }
        }
    }
}
