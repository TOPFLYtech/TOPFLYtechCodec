using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class BleDriverSignInData : BleData
    {
        public static int ALERT_TYPE_DRIVER = 0;
        public static int ALERT_TYPE_LOW_BATTERY = 1;


        private float voltage;

        public float Voltage
        {
            get { return voltage; }
            set { voltage = value; }
        }
        private int alert;

        public int Alert
        {
            get { return alert; }
            set { alert = value; }
        }
        private Boolean latlngValid = false;//current is gps data or lbs data

        public Boolean LatlngValid
        {
            get { return latlngValid; }
            set { latlngValid = value; }
        }
        private Boolean isHistoryData = false;

        public Boolean IsHistoryData
        {
            get { return isHistoryData; }
            set { isHistoryData = value; }
        }
        private int satelliteCount = 0;

        public int SatelliteCount
        {
            get { return satelliteCount; }
            set { satelliteCount = value; }
        }
        private Double latitude = 0.0;

        public Double Latitude
        {
            get { return latitude; }
            set { latitude = value; }
        }
        private Double longitude = 0.0;

        public Double Longitude
        {
            get { return longitude; }
            set { longitude = value; }
        }
        private Double altitude = 0.0;

        public Double Altitude
        {
            get { return altitude; }
            set { altitude = value; }
        }
        private float speed = 0.0f;

        public float Speed
        {
            get { return speed; }
            set { speed = value; }
        }
        private int azimuth = 0;

        public int Azimuth
        {
            get { return azimuth; }
            set { azimuth = value; }
        }

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
