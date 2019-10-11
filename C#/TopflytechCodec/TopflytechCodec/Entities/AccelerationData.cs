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

    }
}
