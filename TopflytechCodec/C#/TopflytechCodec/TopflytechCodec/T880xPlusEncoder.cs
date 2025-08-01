using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec
{
    /// <summary>
    /// New device encoder.Model like 8806+,8806+r
    /// </summary>
    public class T880xPlusEncoder
    {

        private int encryptType = 0;
        private String aesKey;
        /// <summary>
        /// Instantiates a new T880xPlusEncoder.
        /// </summary>
        /// <param name="messageEncryptType">The message encrypt type .Use the value of MessageEncryptType.</param>
        /// <param name="aesKey">The aes key.If you do not use AES encryption, the value can be empty.</param>
        public T880xPlusEncoder(int messageEncryptType, String aesKey)
        {
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
        }

        public byte[] getSignInMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x01 };
            return Encoder.getSignInMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }


        public byte[] getHeartbeatMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x03 };
            return Encoder.getHeartbeatMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }


        public byte[] getLocationMsgReply(String imei, bool needSerialNo, int serialNo, int protocolHeadType)
        {
            byte[] command = { 0x25, 0x25, (byte)protocolHeadType };
            return Encoder.getLocationMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }


        public byte[] getLocationAlarmMsgReply(String imei, bool needSerialNo, int serialNo, int sourceAlarmCode, int protocolHeadType)
        {
            byte[] command = { 0x25, 0x25, (byte)protocolHeadType };
            return Encoder.getLocationAlarmMsgReply(imei, needSerialNo, serialNo, sourceAlarmCode, command, encryptType, aesKey);
        }



        public byte[] getGpsDriverBehaviorMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x05 };
            return Encoder.getGpsDriverBehavoirMsgReply(imei, serialNo, command, encryptType, aesKey);
        }


        public byte[] getAccelerationDriverBehaviorMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x06 };
            return Encoder.getAccelerationDriverBehaviorMsgReply(imei, serialNo, command, encryptType, aesKey);
        }


        public byte[] getAccelerationAlarmMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x07 };
            return Encoder.getAccelerationAlarmMsgReply(imei, serialNo, command, encryptType, aesKey);
        }


        public byte[] getBluetoothPeripheralMsgReply(String imei, int serialNo, int protocolHeadType)
        {
            byte[] command = { 0x25, 0x25, (byte)protocolHeadType };
            return Encoder.getBluetoothPeripheralDataMsgReply(imei, serialNo, command, encryptType, aesKey);
        }


        public byte[] getRS232MsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x09 };
            return Encoder.getRS232MsgReply(imei, serialNo, command, encryptType, aesKey);
        }
        public byte[] getNetworkMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x11 };
            return Encoder.getNetworkMsgReply(imei, serialNo, command, encryptType, aesKey);
        }

        public byte[] getConfigSettingMsg(String imei, String content)
        {
            byte[] command = { 0x25, 0x25, (byte)0x81 };
            return Encoder.getConfigSettingMsg(imei, content, command, encryptType, aesKey);
        }


        public byte[] getBrocastSmsMsg(String imei, String content)
        {
            byte[] command = { 0x25, 0x25, (byte)0x81 };
            return Encoder.getBrocastSmsMsg(imei, content.Trim(), command, encryptType, aesKey);
        }



        public byte[] getForwardMsg(String imei, String phoneNumb, String content)
        {
            byte[] command = { 0x25, 0x25, (byte)0x81 };
            return Encoder.getForwardSmsMsg(imei, phoneNumb, content.Trim(), command, encryptType, aesKey);
        }



        public byte[] getUSSDMsg(String imei, String content)
        {
            byte[] command = { 0x25, 0x25, (byte)0x81 };
            return Encoder.getUSSDMsg(imei, content.Trim(), command, encryptType, aesKey);
        }




        public byte[] getRS232ConfigSettingMsg(String imei, String content)
        {
            byte[] command = { 0x25, 0x25, (byte)0x82 };
            return Encoder.getConfigSettingMsg(imei, content.Trim(), command, encryptType, aesKey);
        }

        public byte[] getRS232ConfigSettingMsg(String imei, byte[] content, int protocolType)
        {
            byte[] command = { 0x25, 0x25, (byte)0x82 };
            return Encoder.get82ConfigSettingMsg(imei, content, command, protocolType, encryptType, aesKey);
        }

        public byte[] getWifiMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x15 };
            return Encoder.getWifiMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }

        public byte[] getRs485MsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x21 };
            return Encoder.getRs485MsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }
        public byte[] getOneWireMsgReply(String imei, bool needSerialNo, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x23 };
            return Encoder.getOneWireMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
        }

        public byte[] getObdMsgReply(String imei, int serialNo)
        {
            byte[] command = { 0x25, 0x25, 0x22 };
            return Encoder.getObdMsgReply(imei, serialNo, command, encryptType, aesKey);
        }

    }
}
