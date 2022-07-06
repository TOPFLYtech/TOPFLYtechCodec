var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
var EncoderHelper = require("./EncoderHelper")
/**
 *  Device Encoder like TLD1-D,TLD2-D
 */
var ObdEncoder = {
    getSignInMsgReply:function (imei,needSerialNo,serialNo,encryptType,aesKey){
        var command = [0x26, 0x26, 0x01]
        return EncoderHelper.getSignInMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getHeartbeatMsgReply:function (imei,needSerialNo,serialNo,encryptType,aesKey){
        var command = [0x26, 0x26, 0x03]
        return EncoderHelper.getHeartbeatMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getLocationMsgReply:function (imei,needSerialNo,serialNo,protocolHeadType,encryptType,aesKey){
        var command = [0x26, 0x26, protocolHeadType]
        return EncoderHelper.getLocationMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getLocationAlarmMsgReply:function (imei,needSerialNo,serialNo,sourceAlarmCode,protocolHeadType,encryptType,aesKey){
        var command = [0x26, 0x26, protocolHeadType]
        return EncoderHelper.getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,command,encryptType,aesKey);
    },
    getGpsDriverBehaviorMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x26, 0x26, 0x05]
        return EncoderHelper.getGpsDriverBehaviorMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getAccelerationDriverBehaviorMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x26, 0x26, 0x06]
        return EncoderHelper.getAccelerationDriverBehaviorMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getAccelerationAlarmMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x26, 0x26, 0x07]
        return EncoderHelper.getAccelerationAlarmMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getBluetoothPeripheralMsgReply:function (imei,serialNo,protocolHeadType,encryptType,aesKey){
        var command = [0x26, 0x26, protocolHeadType]
        return EncoderHelper.getBluetoothPeripheralDataMsgReply(imei,serialNo,command,encryptType,aesKey)
    },
    getNetworkMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x26, 0x26, 0x11]
        return EncoderHelper.getNetworkMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getObdMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x26, 0x26, 0x09]
        return EncoderHelper.getObdMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getConfigSettingMsg:function (imei,content,encryptType,aesKey){
        var command = [0x26, 0x26, 0x81]
        return EncoderHelper.getConfigSettingMsg(imei,content,command,encryptType,aesKey);
    },
    getBroadcastSmsMsg:function (imei,content,encryptType,aesKey){
        var command = [0x26, 0x26, 0x81]
        return EncoderHelper.getBroadcastSmsMsg(imei,content,command,encryptType,aesKey);
    },
    getForwardMsg:function (imei,phoneNumbStr,content,encryptType,aesKey){
        var command = [0x26, 0x26, 0x81]
        return EncoderHelper.getForwardSmsMsg(imei,phoneNumbStr,content,command,encryptType,aesKey)
    },
    getUSSDMsg:function (imei,content,encryptType,aesKey){
        var command = [0x26, 0x26, 0x81]
        return EncoderHelper.getUSSDMsg(imei,content,command,encryptType,aesKey);
    },
    getObdConfigSettingMsg:function (imei,content,encryptType,aesKey){
        var command = [0x26, 0x26, 0x82]
        return EncoderHelper.getConfigSettingMsg(imei,content,command,encryptType,aesKey);
    },
    getClearObdErrorCodeMsg:function (imei,encryptType,aesKey){
        var content = [0x55,0xAA,0x00,0x03,0x01,0x04,0x06,0x0D,0x0A];
        var command = [0x26, 0x26, 0x82];
        return EncoderHelper.getObdConfigSettingMsg(imei,content,command,encryptType,aesKey)
    },
    getObdVinMsg:function (imei,encryptType,aesKey){
        var content = [0x55,0xAA,0x00,0x03,0x01,0x05,0x07,0x0D,0x0A]
        var command = [0x26, 0x26, 0x82]
        return EncoderHelper.getObdConfigSettingMsg(imei,content,command,encryptType,aesKey)
    }
}
module.exports = ObdEncoder;
