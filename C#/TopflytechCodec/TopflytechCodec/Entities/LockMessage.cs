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
    }
}
