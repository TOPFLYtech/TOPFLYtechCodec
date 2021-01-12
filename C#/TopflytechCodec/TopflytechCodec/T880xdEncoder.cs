using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec
{
    public class T880xdEncoder
    {
        private int encryptType = 0;
        private String aesKey;
        public T880xdEncoder(int messageEncryptType, String aesKey)
        {
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
        }

        public byte[] getSignInMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x26, 0x26, 0x01 };
            return Encoder.getSignInMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }

        public byte[] getHeartbeatMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x26, 0x26, 0x03 };
            return Encoder.getHeartbeatMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }


        public byte[] getLocationMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x26, 0x26, 0x02 };
            return Encoder.getLocationMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }


        public byte[] getLocationAlarmMsgReply(String imei, bool needSerialNo, int serialNo, int sourceAlarmCode)
        {
            byte[] command = { 0x26, 0x26, 0x04 };
            return Encoder.getLocationAlarmMsgReply(imei, needSerialNo, serialNo, sourceAlarmCode, command, encryptType, aesKey);
        }



        public byte[] getGpsDriverBehaviorMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x26, 0x26, 0x05 };
            return Encoder.getGpsDriverBehavoirMsgReply(imei, serialNo, command, encryptType, aesKey);
        }


        public byte[] getAccelerationDriverBehaviorMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x26, 0x26, 0x06 };
            return Encoder.getAccelerationDriverBehaviorMsgReply(imei, serialNo, command, encryptType, aesKey);
        }


        public byte[] getAccelerationAlarmMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x26, 0x26, 0x07 };
            return Encoder.getAccelerationAlarmMsgReply(imei, serialNo, command, encryptType, aesKey);
        }


        public byte[] getBluetoothPeripheralMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x26, 0x26, 0x10 };
            return Encoder.getBluetoothPeripheralDataMsgReply(imei, serialNo, command, encryptType, aesKey);
        }


        public byte[] getObdMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x26, 0x26, 0x09 };
            return Encoder.getObdMsgReply(imei, serialNo, command, encryptType, aesKey);
        }

        public byte[] getConfigSettingMsg(String imei, String content)
        {
            byte[] command = { 0x26, 0x26, (byte)0x81 };
            return Encoder.getConfigSettingMsg(imei, content, command, encryptType, aesKey);
        }


        public byte[] getBrocastSmsMsg(String imei, String content)
        {
            byte[] command = { 0x26, 0x26, (byte)0x81 };
            return Encoder.getBrocastSmsMsg(imei, content, command, encryptType, aesKey);
        }



        public byte[] getForwardMsg(String imei, String phoneNumb, String content)
        {
            byte[] command = { 0x26, 0x26, (byte)0x81 };
            return Encoder.getForwardSmsMsg(imei, phoneNumb, content, command, encryptType, aesKey);
        }



        public byte[] getUSSDMsg(String imei, String content)
        {
            byte[] command = { 0x26, 0x26, (byte)0x81 };
            return Encoder.getUSSDMsg(imei, content, command, encryptType, aesKey);
        }



        public byte[] getObdConfigSettingMsg(String imei, String content)
        {
            byte[] command = { 0x26, 0x26, (byte)0x82 };
            return Encoder.getConfigSettingMsg(imei, content, command, encryptType, aesKey);
        }


        public byte[] getClearObdErrorCodeMsg(String imei)
        {
            byte[] content = { (byte)0x55, (byte)0xAA, (byte)0x00, (byte)0x03, (byte)0x01, (byte)0x04, (byte)0x06, (byte)0x0D, (byte)0x0A };
            byte[] command = { 0x26, 0x26, (byte)0x82 };
            return Encoder.getObdConfigSettingMsg(imei, content, command, encryptType, aesKey);
        }

        public byte[] getObdVinMsg(String imei)
        {
            byte[] content = { (byte)0x55, (byte)0xAA, (byte)0x00, (byte)0x03, (byte)0x01, (byte)0x05, (byte)0x07, (byte)0x0D, (byte)0x0A };
            byte[] command = { 0x26, 0x26, (byte)0x82 };
            return Encoder.getObdConfigSettingMsg(imei, content, command, encryptType, aesKey);
        }

        public byte[] getNetworkMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x26, 0x26, 0x11 };
            return Encoder.getNetworkMsgReply(imei, serialNo, command, encryptType, aesKey);
        }

    }

}
