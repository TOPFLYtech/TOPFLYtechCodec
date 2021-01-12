using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class BleFuelData : BleData
    {
        private float voltage;

        public float Voltage
        {
            get { return voltage; }
            set { voltage = value; }
        }

        private float temp;

        public float Temp
        {
            get { return temp; }
            set { temp = value; }
        }

        private int value;
        public int Value
        {
            get { return value; }
            set { this.value = value; }
        }

        private int alarm;
        public int Alarm
        {
            get { return alarm; }
            set { alarm = value; }
        }

        private int online;
        public int Online
        {
            get { return online; }
            set { online = value; }
        }
        private int rssi;

        public int Rssi
        {
            get { return rssi; }
            set { rssi = value; }
        }
    }
}
