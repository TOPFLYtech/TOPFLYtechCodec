using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class Rs232FingerprintMessage:Rs232DeviceMessage
    {
        private int fingerprintId;

        public int FingerprintId
        {
            get { return fingerprintId; }
            set { fingerprintId = value; }
        }
        private String data;

        public String Data
        {
            get { return data; }
            set { data = value; }
        }
        private int fingerprintType;

        public int FingerprintType
        {
            get { return fingerprintType; }
            set { fingerprintType = value; }
        }
        private int status;

        public int Status
        {
            get { return status; }
            set { status = value; }
        }
        private int fingerprintDataIndex;

        public int FingerprintDataIndex
        {
            get { return fingerprintDataIndex; }
            set { fingerprintDataIndex = value; }
        }
        private int remarkId;

        public int RemarkId
        {
            get { return remarkId; }
            set { remarkId = value; }
        }
        public static int FINGERPRINT_TYPE_OF_NONE = 0;
        public static int FINGERPRINT_TYPE_OF_CLOUND_REGISTER = 1;
        public static int FINGERPRINT_TYPE_OF_PATCH = 2;
        public static int FINGERPRINT_TYPE_OF_DELETE = 3;
        public static int FINGERPRINT_TYPE_GET_TEMPLATE = 4;
        public static int FINGERPRINT_TYPE_WRITE_TEMPLATE = 5;
        public static int FINGERPRINT_TYPE_OF_ALL_CLEAR = 6;
        public static int FINGERPRINT_TYPE_OF_SET_PERMISSION = 7;
        public static int FINGERPRINT_TYPE_OF_GET_PERMISSION = 8;
        public static int FINGERPRINT_TYPE_OF_GET_EMPTY_ID = 9;
        public static int FINGERPRINT_TYPE_OF_SET_PATCH_PERMISSION = 10;
        public static int FINGERPRINT_TYPE_OF_SET_DEVICE_ID = 11;
        public static int FINGERPRINT_TYPE_OF_REGISTER = 11;
        public static int FINGERPRITN_MSG_STATUS_SUCC = 0;
        public static int FINGERPRINT_MSG_STATUS_ERROR = 1;
    }
}
