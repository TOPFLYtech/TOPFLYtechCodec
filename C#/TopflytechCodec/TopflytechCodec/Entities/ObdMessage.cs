using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    class ObdMessage:Message
    {
        private String errorCode;

        public String ErrorCode
        {
            get { return errorCode; }
            set { errorCode = value; }
        }
        private String errorData;

        public String ErrorData
        {
            get { return errorData; }
            set { errorData = value; }
        }
        private DateTime date;

        public DateTime Date
        {
            get { return date; }
            set { date = value; }
        }


        private int messageType;

        public int MessageType
        {
            get { return messageType; }
            set { messageType = value; }
        }
        private String vin;

        public String Vin
        {
            get { return vin; }
            set { vin = value; }
        }
        private bool clearErrorCodeSuccess = false;

        public bool ClearErrorCodeSuccess
        {
            get { return clearErrorCodeSuccess; }
            set { clearErrorCodeSuccess = value; }
        }

        public static int ERROR_CODE_MESSAGE = 0;
        public static int VIN_MESSAGE = 1;
        public static int CLEAR_ERROR_CODE_MESSAGE = 2;
    }
}
