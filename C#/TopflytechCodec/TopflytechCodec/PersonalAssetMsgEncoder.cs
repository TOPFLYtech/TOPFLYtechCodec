using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec
{
    public class PersonalAssetMsgEncoder
    {
        private int encryptType = 0;
        private String aesKey;
        public PersonalAssetMsgEncoder(int messageEncryptType, String aesKey)
        {
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
        }
        public byte[] getSignInMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x27, 0x27, 0x01 };
            return Encoder.getSignInMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }
        public byte[] getHeartbeatMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x27, 0x27, 0x03 };
            return Encoder.getHeartbeatMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }

        public byte[] getLocationMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x27, 0x27, 0x02 };
            return Encoder.getLocationMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }

        public byte[] getLocationAlarmMsgReply(String imei, bool needSerialNo, int serialNo, int sourceAlarmCode)
        {
            byte[] command = { 0x27, 0x27, 0x04 };
            return Encoder.getLocationAlarmMsgReply(imei, needSerialNo, serialNo, sourceAlarmCode, command, encryptType, aesKey);
        }
        public byte[] getBluetoothPeripheralMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x27, 0x27, 0x10 };
            return Encoder.getBluetoothPeripheralDataMsgReply(imei, serialNo, command, encryptType, aesKey);
        }
        public byte[] getBrocastSmsMsg(String imei, String content)
        {
            byte[] command = { 0x27, 0x27, (byte)0x81 };
            return Encoder.getBrocastSmsMsg(imei, content, command, encryptType, aesKey);
        }

        public byte[] getUSSDMsg(String imei, String content)
        {
            byte[] command = { 0x27, 0x27, (byte)0x81 };
            return Encoder.getUSSDMsg(imei, content, command, encryptType, aesKey);
        }

        public byte[] getConfigSettingMsg(String imei, String content)
        {
            byte[] command = { 0x27, 0x27, (byte)0x81 };
            return Encoder.getConfigSettingMsg(imei, content, command, encryptType, aesKey);
        } 
    }
}
