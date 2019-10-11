using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using TopflytechCodec.Entities;

namespace TopflytechCodec
{
    /// <summary>
    /// Old device encoder.Model like 8806,8803PRO
    /// </summary>
    public class T880XEncoder
    {
        /// <summary>
        /// Get sign in msg reply byte [ ].
        /// </summary>
        /// <param name="imei">The imei</param>
        /// <param name="needSerialNo">The need serial no</param>
        /// <param name="serialNo">The serial no</param> 
        /// <returns>The msg reply byte [ ]</returns> 
    public static byte[] getSignInMsgReply(String imei,bool needSerialNo,int serialNo)  {
        byte[] command = {0x23, 0x23, 0x01};
        return Encoder.getSignInMsgReply(imei, needSerialNo, serialNo, command, MessageEncryptType.NONE, null);
    }

    /// <summary>
    /// Get heartbeat msg reply byte  [ ].
    /// </summary>
    /// <param name="imei">The imei</param>
    /// <param name="needSerialNo">The need serial no</param>
    /// <param name="serialNo">The serial no</param> 
    /// <returns>The msg reply byte [ ]</returns> 
    public static byte[] getHeartbeatMsgReply(String imei,bool needSerialNo,int serialNo)  {
        byte[] command = {0x23, 0x23, 0x03};
        return Encoder.getHeartbeatMsgReply(imei, needSerialNo, serialNo, command, MessageEncryptType.NONE, null);
    }

    /// <summary>
    /// Get location msg reply byte [ ].
    /// </summary>
    /// <param name="imei">The imei</param>
    /// <param name="needSerialNo">The need serial no</param>
    /// <param name="serialNo">The serial no</param> 
    /// <returns>The msg reply byte [ ]</returns> 
    public static byte[] getLocationMsgReply(String imei,bool needSerialNo,int serialNo){
        byte[] command = {0x23, 0x23, 0x02};
        return Encoder.getLocationMsgReply(imei, needSerialNo, serialNo, command, MessageEncryptType.NONE, null);
    }

    /// <summary>
    /// Get location alarm msg reply byte [ ].
    /// </summary>
    /// <param name="imei">The imei</param>
    /// <param name="needSerialNo">The need serial no</param>
    /// <param name="serialNo">The serial no</param>
    /// <param name="sourceAlarmCode">The source alarm code</param> 
    /// <returns>The msg reply byte [ ]</returns>
    public static byte[] getLocationAlarmMsgReply(String imei,bool needSerialNo,int serialNo,int sourceAlarmCode){
        byte[] command = {0x23, 0x23, 0x04};
        return Encoder.getLocationAlarmMsgReply(imei, needSerialNo, serialNo, sourceAlarmCode, command, MessageEncryptType.NONE, null);
    }


    /// <summary>
    /// Get config setting msg byte [ ].
    /// </summary>
    /// <param name="imei">The imei</param>
    /// <param name="content">The config Content,also you can use sms command</param> 
    /// <returns>The config setting msg byte [ ].</returns> 
    public static byte[] getConfigSettingMsg(String imei,String content)  {
        byte[] command = {0x23, 0x23, (byte)0x81};
        return Encoder.getConfigSettingMsg(imei, content, command, MessageEncryptType.NONE, null);
    }

    /// <summary>
    /// Get brocast sms msg byte [ ].
    /// </summary>
    /// <param name="imei">The imei</param>
    /// <param name="content">The brocast content</param> 
    /// <returns>The brocast setting msg byte [ ].</returns> 
    public static byte[] getBrocastSmsMsg(String imei,String content)  {
        byte[] command = {0x23, 0x23, (byte)0x81};
        return Encoder.getBrocastSmsMsg(imei, content, command, MessageEncryptType.NONE, null);
    }
    }
}
