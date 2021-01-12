using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class Rs232FuelMessage : Rs232DeviceMessage
    {
        private float fuelPercent;

        public float FuelPercent
        {
            get { return fuelPercent; }
            set { fuelPercent = value; }
        }
        private float temp;

        public float Temp
        {
            get { return temp; }
            set { temp = value; }
        }
        private int liquidType;

        public int LiquidType
        {
            get { return liquidType; }
            set { liquidType = value; }
        }
        private float curLiquidHeight;

        public float CurLiquidHeight
        {
            get { return curLiquidHeight; }
            set { curLiquidHeight = value; }
        }
        private float fullLiquidHeight;

        public float FullLiquidHeight
        {
            get { return fullLiquidHeight; }
            set { fullLiquidHeight = value; }
        }
        private int alarm = 0;

        public int Alarm
        {
            get { return alarm; }
            set { alarm = value; }
        }


       
        public static int LIQUID_TYPE_OF_DIESEL = 1;
        public static int LIQUID_TYPE_OF_PETROL = 2;
        public static int LIQUID_TYPE_OF_WATER = 3;

        public static int ALARM_NONE = 0;
        public static int ALARM_FILL_TANK = 1;
        public static int ALARM_FUEL_LEAK = 2;
    }
}
