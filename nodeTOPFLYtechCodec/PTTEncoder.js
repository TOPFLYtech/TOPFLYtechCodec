var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
var EncoderHelper = require("./EncoderHelper")

var PTTEncoder = {
    getHeartbeatMsgReply:function (imei,serialNo,encryptType,aesKey){
        var command = [0x28, 0x28, 0x03]
        return EncoderHelper.getHeartbeatMsgReply(imei,serialNo,command,encryptType,aesKey);
    },
    getTalkStartMsgReply:function (imei,serialNo,status,encryptType,aesKey){
        var command = [0x28, 0x28, 0x04]
        return EncoderHelper.getNormalMsgReply(imei,serialNo,command,[status],encryptType,aesKey);
    },
    getTalkEndMsgReply:function (imei,serialNo,status,encryptType,aesKey){
        var command = [0x28, 0x28, 0x05]
        return EncoderHelper.getNormalMsgReply(imei,serialNo,command,[status],encryptType,aesKey);
    },
    getVoiceData:function (imei,serialNo,encodeType,voiceData,encryptType,aesKey){
        var command = [0x28, 0x28, 0x06]
        var content = [encodeType]
        var lenByte = ByteUtils.short2Bytes(voiceData.length)
        for(var i = 0;i < lenByte.length;i++){
            content.push(lenByte[i])
        }
        for(var i = 0;i < voiceData.length;i++){
            content.push(voiceData[i])
        }
        return EncoderHelper.getNormalMsgReply(imei,serialNo,command,content,encryptType,aesKey);
    },
    getListenStartData:function (imei,serialNo,encryptType,aesKey){
        var command = [0x28, 0x28, 0x07]
        return EncoderHelper.getNormalMsgReply(imei,serialNo,command,[],encryptType,aesKey);
    },
    getListenEndData:function (imei,serialNo,encryptType,aesKey){
        var command = [0x28, 0x28, 0x08]
        return EncoderHelper.getNormalMsgReply(imei,serialNo,command,[],encryptType,aesKey);
    },
}
module.exports = PTTEncoder;