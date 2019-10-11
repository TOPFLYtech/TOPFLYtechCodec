using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class BleTempData : BleData
    {
        private float voltage;

        public float Voltage
        {
            get { return voltage; }
            set { voltage = value; }
        }
        private int batteryPercent;

        public int BatteryPercent
        {
            get { return batteryPercent; }
            set { batteryPercent = value; }
        }
        private float temp;

        public float Temp
        {
            get { return temp; }
            set { temp = value; }
        }
        private float humidity;

        public float Humidity
        {
            get { return humidity; }
            set { humidity = value; }
        }
        private Boolean isOpenBox;

        public Boolean IsOpenBox
        {
            get { return isOpenBox; }
            set { isOpenBox = value; }
        }
        private int lightIntensity;

        public int LightIntensity
        {
            get { return lightIntensity; }
            set { lightIntensity = value; }
        }
        private int rssi;

        public int Rssi
        {
            get { return rssi; }
            set { rssi = value; }
        }
    }
}
