using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class BleCustomer2397SensorData : BleData
    {
        private int rssi;

        public int Rssi
        {
            get { return rssi; }
            set { rssi = value; }
        }
        private byte[] rawData;

        public byte[] RawData
        {
            get { return rawData; }
            set { rawData = value; }
        } 
    }
}
