using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    /// <summary>
    /// The type Acceleration data.
    /// </summary>
    public class AccelerationData
    {

        private string imei;

        public string Imei
        {
            get { return imei; }
            set { imei = value; }
        }
        private DateTime date;
        public System.DateTime Date
        {
            get { return date; }
            set { date = value; }
        }
        private float axisX;
        /// <summary>
        /// The acceleration of x axis.
        /// </summary>
        public float AxisX
        {
            get { return axisX; }
            set { axisX = value; }
        }
        private float axisY;
        /// <summary>
        /// The acceleration of y axis
        /// </summary>
        public float AxisY
        {
            get { return axisY; }
            set { axisY = value; }
        }
        private float axisZ;
        /// <summary>
        /// The acceleration of z axis
        /// </summary>
        public float AxisZ
        {
            get { return axisZ; }
            set { axisZ = value; }
        }
        private float speed;
        /// <summary>
        /// The speed
        /// </summary>
        public float Speed
        {
            get { return speed; }
            set { speed = value; }
        }
        private double latitude;
        /// <summary>
        /// the latitude
        /// </summary>
        public double Latitude
        {
            get { return latitude; }
            set { latitude = value; }
        }
        private double longitude;
        public double Longitude
        {
            get { return longitude; }
            set { longitude = value; }
        }
        private double altitude;
        public double Altitude
        {
            get { return altitude; }
            set { altitude = value; }
        }
        private int azimuth;
        public int Azimuth
        {
            get { return azimuth; }
            set { azimuth = value; }
        }
        private int satelliteNumber;
        /// <summary>
        /// The satellite number
        /// </summary>
        public int SatelliteNumber
        {
            get { return satelliteNumber; }
            set { satelliteNumber = value; }
        }
        private bool gpsWorking = false;
        /// <summary>
        /// Is gps working or sleeping. when gps working ,return true, otherwise return false.
        /// </summary>
        public bool GpsWorking
        {
            get { return gpsWorking; }
            set { gpsWorking = value; }
        }
        private bool latlngValid = false;
        /// <summary>
        /// Is GPS data or LBS data. when GPS data,return true.
        /// </summary>
        public bool LatlngValid
        {
            get { return latlngValid; }
            set { latlngValid = value; }
        }
        private bool isHistoryData = false;
        public bool IsHistoryData
        {
            get { return isHistoryData; }
            set { isHistoryData = value; }
        }
        private int rpm;

        public int Rpm
        {
            get { return rpm; }
            set { rpm = value; }
        }

        private float gyroscopeAxisX;
        public float GyroscopeAxisX
        {
            get { return gyroscopeAxisX; }
            set { gyroscopeAxisX = value; }
        }
        private float gyroscopeAxisY;
        public float GyroscopeAxisY
        {
            get { return gyroscopeAxisY; }
            set { gyroscopeAxisY = value; }
        }
        private float gyroscopeAxisZ;
        public float GyroscopeAxisZ
        {
            get { return gyroscopeAxisZ; }
            set { gyroscopeAxisZ = value; }
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
    }
}
