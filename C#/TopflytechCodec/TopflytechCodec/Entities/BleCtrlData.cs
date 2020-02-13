using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class BleCtrlData : BleData
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

        private int ctrlStatus;
        public int CtrlStatus
        {
            get { return ctrlStatus; }
            set { ctrlStatus = value; }
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
