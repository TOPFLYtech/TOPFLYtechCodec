using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class Rs232TireMessage :Rs232DeviceMessage
    {
        private String sensorId;

        public String SensorId
        {
            get { return sensorId; }
            set { sensorId = value; }
        }
        private double voltage = 0;

        public double Voltage
        {
            get { return voltage; }
            set { voltage = value; }
        }
        private double airPressure = 0;

        public double AirPressure
        {
            get { return airPressure; }
            set { airPressure = value; }
        }
        private int airTemp = 0;

        public int AirTemp
        {
            get { return airTemp; }
            set { airTemp = value; }
        }
        private int status = 0;

        public int Status
        {
            get { return status; }
            set { status = value; }
        }
    }
}
