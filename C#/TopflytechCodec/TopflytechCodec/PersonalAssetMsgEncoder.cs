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

        public byte[] getLockMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x27, 0x27, 0x17 };
            return Encoder.getWifiMsgReply(imei, true, serialNo, command, encryptType, aesKey);
        }

        public byte[] getWifiMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x27, 0x27, 0x15 };
            return Encoder.getWifiMsgReply(imei, true, serialNo, command, encryptType, aesKey);
        }

        public byte[] getLocationMsgReply(String imei, bool needSerialNo, int serialNo, int protocolHeadType)
        {
            byte[] command = { 0x27, 0x27, (byte)protocolHeadType };
            return Encoder.getLocationMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }

        public byte[] getLocationAlarmMsgReply(String imei, bool needSerialNo, int serialNo, int sourceAlarmCode, int protocolHeadType)
        {
            byte[] command = { 0x27, 0x27, (byte)protocolHeadType };
            return Encoder.getLocationAlarmMsgReply(imei, needSerialNo, serialNo, sourceAlarmCode, command, encryptType, aesKey);
        }
        public byte[] getBluetoothPeripheralMsgReply(String imei, int serialNo, int protocolHeadType)
        {
            byte[] command = { 0x27, 0x27, (byte)protocolHeadType };
            return Encoder.getBluetoothPeripheralDataMsgReply(imei, serialNo, command, encryptType, aesKey);
        }
        public byte[] getBrocastSmsMsg(String imei, String content)
        {
            byte[] command = { 0x27, 0x27, (byte)0x81 };
            return Encoder.getBrocastSmsMsg(imei, content.Trim(), command, encryptType, aesKey);
        }

        public byte[] getUSSDMsg(String imei, String content)
        {
            byte[] command = { 0x27, 0x27, (byte)0x81 };
            return Encoder.getUSSDMsg(imei, content.Trim(), command, encryptType, aesKey);
        }

        public byte[] getConfigSettingMsg(String imei, String content)
        {
            byte[] command = { 0x27, 0x27, (byte)0x81 };
            return Encoder.getConfigSettingMsg(imei, content.Trim(), command, encryptType, aesKey);
        }


        public byte[] getNetworkMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x27, 0x27, 0x05 };
            return Encoder.getNetworkMsgReply(imei, serialNo, command, encryptType, aesKey);
        }

        public byte[] getDeviceTempCollectionMsgReply(String imei, int serialNo) 
        {
            byte[] command = { 0x27, 0x27, 0x26 };
            byte[] content = new byte[] { };
            return Encoder.getNormalMsgReply(imei, serialNo, command, content, encryptType, aesKey);
        }

    public byte[] getInnerGeoDataMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x27, 0x27, 0x20 };
            return Encoder.getNormalMsgReply(imei, serialNo, command, new byte[] {},encryptType, aesKey);
        }

        public byte[] getWifiWithDeviceInfoReply(String imei, int serialNo, int alarmCode,int protocolHeadType)
        {
            byte[] command = { 0x27, 0x27, (byte)protocolHeadType };
            byte[] content;
            if (alarmCode != 0)
            {
                content = new byte[] { (byte)alarmCode };
            }
            else
            {
                content = new byte[] { };
            }
            return Encoder.getNormalMsgReply(imei, serialNo, command, content, encryptType, aesKey);
        }
    }
}
