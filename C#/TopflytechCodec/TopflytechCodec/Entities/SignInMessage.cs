using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    /// <summary>
    /// The type Sign in message.
    /// Older devices like 8806,8803Pro,You need to respond to the message to the device, otherwise the device will not send other data.
    /// The new device, like the 8806 plus, needs to be based on the device configuration to decide whether or not to respond to the message.
    /// Protocol number is 25 25 01.
    /// </summary>
    public class SignInMessage : Message
    {
        private String software;
        /// <summary>
        /// The software version.like 1.1.1
        /// </summary>
        public System.String Software
        {
            get { return software; }
            set { software = value; }
        }
        private String firmware;
        /// <summary>
        /// The firmware version.like 1.1
        /// </summary>
        public System.String Firmware
        {
            get { return firmware; }
            set { firmware = value; }
        }
        private String hareware;
        /// <summary>
        /// The hareware version.like 5.0
        /// </summary>
        public System.String Hareware
        {
            get { return hareware; }
            set { hareware = value; }
        }
        private String platform;
        /// <summary>
        /// The platform. like 6250
        /// </summary>
        public System.String Platform
        {
            get { return platform; }
            set { platform = value; }
        }
        private String obdSoftware;

        public String ObdSoftware
        {
            get { return obdSoftware; }
            set { obdSoftware = value; }
        }
        private String obdHareware;

        public String ObdHareware
        {
            get { return obdHareware; }
            set { obdHareware = value; }
        } 
    }
}
