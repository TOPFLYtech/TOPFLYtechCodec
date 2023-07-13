var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
var EncoderHelper = require("./EncoderHelper")
/**
 *  Device Encoder like TLP-P
 */
var PersonalAssetEncoder = {
    getSignInMsgReply:function (imei,needSerialNo,serialNo,encryptType,aesKey){
        var command = [0x27, 0x27, 0x01]
        return EncoderHelper.getSignInMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getHeartbeatMsgReply:function (imei,needSerialNo,serialNo,encryptType,aesKey){
        var command = [0x27, 0x27, 0x03]
        return EncoderHelper.getHeartbeatMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getLocationMsgReply:function (imei,needSerialNo,serialNo,protocolHeadType,encryptType,aesKey){
        var command = [0x27, 0x27, protocolHeadType]
        return EncoderHelper.getLocationMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getLocationAlarmMsgReply:function (imei,needSerialNo,serialNo,sourceAlarmCode,protocolHeadType,encryptType,aesKey){
        var command = [0x27, 0x27, protocolHeadType]
        return EncoderHelper.getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,command,encryptType,aesKey);
    },
    getBroadcastSmsMsg:function (imei,content,encryptType,aesKey){
        var command = [0x27, 0x27, 0x81]
        return EncoderHelper.getBroadcastSmsMsg(imei,content,command,encryptType,aesKey);
    },
    getUSSDMsg:function (imei,content,encryptType,aesKey){
        var command = [0x27, 0x27, 0x81]
        return EncoderHelper.getUSSDMsg(imei,content,command,encryptType,aesKey);
    },
    getNetworkMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x27, 0x27, 0x05]
        return EncoderHelper.getNetworkMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getConfigSettingMsg:function (imei,content,encryptType,aesKey){
        var command = [0x27, 0x27, 0x81]
        return EncoderHelper.getConfigSettingMsg(imei,content,command,encryptType,aesKey);
    },
    getBluetoothPeripheralMsgReply:function (imei,serialNo,protocolHeadType,encryptType,aesKey){
        var command = [0x27, 0x27, protocolHeadType]
        return EncoderHelper.getBluetoothPeripheralDataMsgReply(imei,serialNo,command,encryptType,aesKey)
    },
    getWifiMsgReply:function (imei,needSerialNo,serialNo,encryptType,aesKey){
        var command = [0x27, 0x27, 0x15]
        return EncoderHelper.getWifiMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getLockMsgReply:function (imei,needSerialNo,serialNo,encryptType,aesKey){
        var command = [0x27, 0x27, 0x17]
        return EncoderHelper.getLockMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    },
    getInnerGeoDataMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x27, 0x27, 0x20]
        return EncoderHelper.getNormalMsgReply(imei,serialNo,command,[],encryptType,aesKey);
    },
    getWifiWithDeviceInfoMsgReply:function (imei,serialNo,sourceAlarmCode,protocolHeadType,encryptType,aesKey){
        var command = [0x27, 0x27, protocolHeadType]
        var content;
        if(sourceAlarmCode != 0){
            content = [sourceAlarmCode]
        }else{
            content = []
        }
        return EncoderHelper.getNormalMsgReply(imei,serialNo,command,content,encryptType,aesKey);
    },
}

module.exports = PersonalAssetEncoder
