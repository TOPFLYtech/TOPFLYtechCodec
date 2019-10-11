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
    }
}
