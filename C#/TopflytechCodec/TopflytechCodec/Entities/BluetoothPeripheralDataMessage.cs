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
    }
}
