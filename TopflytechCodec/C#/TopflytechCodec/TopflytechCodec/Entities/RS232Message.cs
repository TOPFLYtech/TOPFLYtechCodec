using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    /// <summary>
    /// The type RS232 message.Protocol number is 25 25 09.
    /// </summary>
    public class RS232Message:Message
    {
        private DateTime date;
        public System.DateTime Date
        {
            get { return date; }
            set { date = value; }
        }
        private bool isIgnition;
        /// <summary>
        /// Is vehicle ignition 
        /// </summary>
        public bool IsIgnition
        {
            get { return isIgnition; }
            set { isIgnition = value; }
        }

        private int rs232DataType;

        public int Rs232DataType
        {
            get { return rs232DataType; }
            set { rs232DataType = value; }
        }
        private List<Rs232DeviceMessage> rs232DeviceMessageList;

        internal List<Rs232DeviceMessage> Rs232DeviceMessageList
        {
            get { return rs232DeviceMessageList; }
            set { rs232DeviceMessageList = value; }
        }

        public static int OTHER_DEVICE_DATA = 0;
        public static int TIRE_DATA = 1;
        public static int RDID_DATA = 3;
        public static int FINGERPRINT_DATA = 2;
        public static int CAPACITOR_FUEL_DATA = 4;
        public static int ULTRASONIC_FUEL_DATA = 5;
    }
}
