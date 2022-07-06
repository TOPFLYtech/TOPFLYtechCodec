var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
var EncoderHelper = require("./EncoderHelper")
/**
 *  New Device Encoder like 8806+,8806+r
 */
var Encoder = {
    getSignInMsgReply:function (imei,needSerialNo,serialNo,encryptType,aesKey){
        var command = [0x25, 0x25, 0x01]
        return EncoderHelper.getSignInMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getHeartbeatMsgReply:function (imei,needSerialNo,serialNo,encryptType,aesKey){
        var command = [0x25, 0x25, 0x03]
        return EncoderHelper.getHeartbeatMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getLocationMsgReply:function (imei,needSerialNo,serialNo,protocolHeadType,encryptType,aesKey){
        var command = [0x25, 0x25, protocolHeadType]
        return EncoderHelper.getLocationMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getLocationAlarmMsgReply:function (imei,needSerialNo,serialNo,sourceAlarmCode,protocolHeadType,encryptType,aesKey){
        var command = [0x25, 0x25, protocolHeadType]
        return EncoderHelper.getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,command,encryptType,aesKey);
    },
    getGpsDriverBehaviorMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x25, 0x25, 0x05]
        return EncoderHelper.getGpsDriverBehaviorMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getAccelerationDriverBehaviorMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x25, 0x25, 0x06]
        return EncoderHelper.getAccelerationDriverBehaviorMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getAccelerationAlarmMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x25, 0x25, 0x07]
        return EncoderHelper.getAccelerationAlarmMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getBluetoothPeripheralMsgReply:function (imei,serialNo,protocolHeadType,encryptType,aesKey){
        var command = [0x25, 0x25, protocolHeadType]
        return EncoderHelper.getBluetoothPeripheralDataMsgReply(imei,serialNo,command,encryptType,aesKey)
    },
    getRS232MsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x25, 0x25, 0x09]
        return EncoderHelper.getRS232MsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getNetworkMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x25, 0x25, 0x11]
        return EncoderHelper.getNetworkMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getConfigSettingMsg:function (imei,content,encryptType,aesKey){
        var command = [0x25, 0x25, 0x81]
        return EncoderHelper.getConfigSettingMsg(imei,content,command,encryptType,aesKey);
    },
    getBroadcastSmsMsg:function (imei,content,encryptType,aesKey){
        var command = [0x25, 0x25, 0x81]
        return EncoderHelper.getBroadcastSmsMsg(imei,content,command,encryptType,aesKey);
    },
    getForwardMsg:function (imei,phoneNumbStr,content,encryptType,aesKey){
        var command = [0x25, 0x25, 0x81]
        return EncoderHelper.getForwardSmsMsg(imei,phoneNumbStr,content,command,encryptType,aesKey)
    },
    getUSSDMsg:function (imei,content,encryptType,aesKey){
        var command = [0x25, 0x25, 0x81]
        return EncoderHelper.getUSSDMsg(imei,content,command,encryptType,aesKey);
    },
    getRS232ConfigSettingMsg:function (imei,content,encryptType,aesKey){
        var command = [0x25, 0x25, 0x82]
        return EncoderHelper.getConfigSettingMsg(imei,content,command,encryptType,aesKey);
    },
    getRS232ConfigSettingMsg:function (imei,contentByte,protocolType,encryptType,aesKey){
        var command = [0x25, 0x25, 0x82]
        return EncoderHelper.get82ConfigSettingMsg(imei,contentByte,command,protocolType,encryptType,aesKey);
    },
}
module.exports = Encoder;
