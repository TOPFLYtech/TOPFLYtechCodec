using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class OneWireData
    {
        private byte _deviceType;
        private string _deviceId;
        private byte[] _oneWireContent;

        public byte DeviceType
        {
            get => _deviceType;
            set => _deviceType = value;
        }

        public string DeviceId
        {
            get => _deviceId;
            set => _deviceId = value;
        }

        public byte[] OneWireContent
        {
            get => _oneWireContent;
            set => _oneWireContent = value;
        }
    }
}
