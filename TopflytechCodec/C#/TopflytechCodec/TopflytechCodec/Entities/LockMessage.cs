using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class LockMessage : Message
    {   private bool latlngValid = false;
        /// <summary>
        /// Is GPS data or LBS data. when GPS data,return true.
        /// </summary>
        public bool LatlngValid
        {
            get { return latlngValid; }
            set { latlngValid = value; }
        }
     private double latitude = 0.0;
        public double Latitude
        {
            get { return latitude; }
            set { latitude = value; }
        }
      
        private double longitude = 0.0;
        public double Longitude
        {
            get { return longitude; }
            set { longitude = value; }
        }
        private double altitude = 0.0;
        public double Altitude
        {
            get { return altitude; }
            set { altitude = value; }
        }
        private float speed = 0.0f;  
        /// <summary>
        /// The speed.The unit is km / h
        /// </summary>
        public float Speed
        {
            get { return speed; }
            set { speed = value; }
        }      
      
        private int azimuth = 0;   
        /// <summary>
        /// The vehicle current azimuth.
        /// </summary>
        public int Azimuth
        {
            get { return azimuth; }
            set { azimuth = value; }
        }     
        private int lockType;
        public int LockType
        {
            get { return lockType; }
            set { lockType = value; }
        }
     
        private String lockId;
        public String LockId
        {
            get { return lockId; }
            set { lockId = value; }
        }
        private DateTime date;     
        /// <summary>
        /// Gets date.The message snapshot time
        /// </summary>
     
        public System.DateTime Date
        {
            get { return date; }
            set { date = value; }
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
        private Int64 eci_4g;
        public Int64 Eci_4g
        {
            get { return eci_4g; }
            set { eci_4g = value; }
        }
        private Int32 tac;
        public Int32 TAC
        {
            get { return tac; }
            set { tac = value; }
        }
        private Int32 pcid_4g_1;
        public Int32 Pcid_4g_1
        {
            get { return pcid_4g_1; }
            set { pcid_4g_1 = value; }
        }
        private Int32 pcid_4g_2;
        public Int32 Pcid_4g_2
        {
            get { return pcid_4g_2; }
            set { pcid_4g_2 = value; }
        }
        private Int32 pcid_4g_3;
        public Int32 Pcid_4g_3
        {
            get { return pcid_4g_3; }
            set { pcid_4g_3 = value; }
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

        private bool isHistoryData = false;
        public bool IsHistoryData
        {
            get { return isHistoryData; }
            set { isHistoryData = value; }
        }
        private bool gpsWorking = false;
        /// <summary>
        ///  Is gps working or sleeping. when gps working ,return true, otherwise return false.
        /// </summary>
        public bool GpsWorking
        {
            get { return gpsWorking; }
            set { gpsWorking = value; }
        }
        private int satelliteNumber;
        public int SatelliteNumber
        {
            get { return satelliteNumber; }
            set { satelliteNumber = value; }
        }
    }
}
