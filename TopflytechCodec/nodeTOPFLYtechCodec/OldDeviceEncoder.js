var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
var EncoderHelper = require("./EncoderHelper")
/**
 * Old Device Encoder like 8806,8803PRO
 */
var OldDeviceEncoder = {
    getSignInMsgReply:function (imei,needSerialNo,serialNo){
        var command = [0x23, 0x23, 0x01]
        return EncoderHelper.getSignInMsgReply(imei,needSerialNo,serialNo,command,CryptoTool.MessageEncryptType.NONE,null)
    },
    getHeartbeatMsgReply:function (imei,needSerialNo,serialNo){
        var command = [0x23, 0x23, 0x03]
        return EncoderHelper.getHeartbeatMsgReply(imei,needSerialNo,serialNo,command,CryptoTool.MessageEncryptType.NONE,null)
    },
    getLocationMsgReply:function (imei,needSerialNo,serialNo){
        var command = [0x23, 0x23, 0x02]
        return EncoderHelper.getLocationMsgReply(imei,needSerialNo,serialNo,command,CryptoTool.MessageEncryptType.NONE,null)
    },
    getLocationAlarmMsgReply:function (imei,needSerialNo,serialNo,sourceAlarmCode){
        var command = [0x23, 0x23, 0x02]
        return EncoderHelper.getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,command,CryptoTool.MessageEncryptType.NONE,null)
    },
    getConfigSettingMsg:function (imei,content){
        var command = [0x23, 0x23, 0x81]
        return EncoderHelper.getConfigSettingMsg(imei,content,command,CryptoTool.MessageEncryptType.NONE,null)
    },
    getBroadcastSmsMsg:function (imei,content){
        var command = [0x23, 0x23, 0x81]
        return EncoderHelper.getBroadcastSmsMsg(imei,content,command,CryptoTool.MessageEncryptType.NONE,null)
    }
}
module.exports = OldDeviceEncoder;
