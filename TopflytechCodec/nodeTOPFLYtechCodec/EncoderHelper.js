var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
var EncoderHelper = {
    encodeImei:function (imei){
        if(!imei || imei.length != 15){
            return [15]
        }
        return ByteUtils.hexStringToByte("0" + imei)
    },
    encode:function (imei,needSerialNo,serialNo,command,content,length){
        var result = []
        for(var i = 0;i < command.length;i++){
            result.push(command[i])
        }
        var lenByte = ByteUtils.short2Bytes(length)
        for(var i = 0;i < lenByte.length;i++){
            result.push(lenByte[i])
        }
        if(needSerialNo){
            var serialNoByte = ByteUtils.short2Bytes(serialNo)
            for(var i = 0;i < serialNoByte.length;i++){
                result.push(serialNoByte[i])
            }
        }else{
            result.push(0x00)
            result.push(0x01)
        }
        var imeiByte = this.encodeImei(imei)
        for(var i = 0;i < imeiByte.length;i++){
            result.push(imeiByte[i])
        }
        for(var i = 0;i < content.length;i++){
            result.push(content[i])
        }
        return result
    },
    encodeConfig:function (imei,needSerialNo,serialNo,command,protocol,content){
        var result = []
        for(var i = 0;i < command.length;i++){
            result.push(command[i])
        }
        result.push(0x00)
        result.push(0x10)
        if(needSerialNo){
            var serialNoByte = ByteUtils.short2Bytes(serialNo)
            for(var i = 0;i < serialNoByte.length;i++){
                result.push(serialNoByte[i])
            }
        }else{
            result.push(0x00)
            result.push(0x01)
        }
        var imeiByte = this.encodeImei(imei)
        for(var i = 0;i < imeiByte.length;i++){
            result.push(imeiByte[i])
        }
        result.push(protocol)
        for(var i = 0;i < content.length;i++){
            result.push(content[i])
        }
        return result
    },
    encodeConfigWithLen:function (imei,needSerialNo,serialNo,command,protocol,content,length){
        var result = []
        for(var i = 0;i < command.length;i++){
            result.push(command[i])
        }
        var lenByte = ByteUtils.short2Bytes(length)
        for(var i = 0;i < lenByte.length;i++){
            result.push(lenByte[i])
        }
        if(needSerialNo){
            var serialNoByte = ByteUtils.short2Bytes(serialNo)
            for(var i = 0;i < serialNoByte.length;i++){
                result.push(serialNoByte[i])
            }
        }else{
            result.push(0x00)
            result.push(0x01)
        }
        var imeiByte = this.encodeImei(imei)
        for(var i = 0;i < imeiByte.length;i++){
            result.push(imeiByte[i])
        }
        result.push(protocol)
        for(var i = 0;i < content.length;i++){
            result.push(content[i])
        }
        return result
    },
    getSignInMsgReply:function (imei,needSerialNo,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,needSerialNo,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getWifiMsgReply:function (imei,needSerialNo,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,needSerialNo,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getRs485MsgReply:function (imei,needSerialNo,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,needSerialNo,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getOneWireMsgReply:function (imei,needSerialNo,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,needSerialNo,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getHeartbeatMsgReply:function (imei,needSerialNo,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,needSerialNo,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getLocationMsgReply:function (imei,needSerialNo,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,needSerialNo,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getLocationAlarmMsgReply:function (imei,needSerialNo,serialNo,sourceAlarmCode,command,messageEncryptType,aesKey){
        var data = this.encode(imei,needSerialNo,serialNo, command, [sourceAlarmCode],0x10);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getGpsDriverBehaviorMsgReply:function (imei,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,true,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getAccelerationDriverBehaviorMsgReply:function (imei,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,true,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getAccelerationAlarmMsgReply:function (imei,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,true,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getBluetoothPeripheralDataMsgReply:function (imei,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,true,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getRS232MsgReply:function (imei,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,true,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getObdMsgReply:function (imei,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,true,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getConfigSettingMsg:function (imei,content,command,messageEncryptType,aesKey){
        var data = this.encodeConfig(imei, false, 1, command, 1, ByteUtils.stringToByte(content,""));
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getObdConfigSettingMsg:function(imei,contentByte,command,messageEncryptType,aesKey){
        var data = this.encodeConfig(imei, false, 1, command, 1, contentByte);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    get82ConfigSettingMsg:function (imei,contentByte,command,protocolType,messageEncryptType,aesKey){
        var length = 16 + contentByte.length
        var data = this.encodeConfigWithLen(imei, false, 1, command, protocolType, contentByte,length);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getBroadcastSmsMsg:function (imei,content,command,messageEncryptType,aesKey){
        var data = this.encodeConfig(imei, false, 1, command, 2, ByteUtils.stringToByte(content,"ascii"));
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getForwardSmsMsg:function (imei,phoneNumbStr,content,command,messageEncryptType,aesKey){
        var phoneNumbByte = ByteUtils.stringToByte(phoneNumbStr,"ascii")
        var contentByte = ByteUtils.stringToByte(content,"ascii")
        var realContent = []
        for(var i = 0;i < phoneNumbByte.length;i++){
            realContent.push(phoneNumbByte[i])
        }
        for(var i = 0;i < contentByte.length;i++){
            realContent.push(contentByte[i])
        }
        var data = this.encodeConfig(imei,false,1,command,3,realContent)
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getUSSDMsg:function (imei,content,command,messageEncryptType,aesKey){
        var data = this.encodeConfigWithLen(imei, false, 1, command, 5, 0x0f);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getNetworkMsgReply:function (imei,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,true,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getNormalMsgReply:function (imei,serialNo,command,content,messageEncryptType,aesKey){
        var data = this.encode(imei,true,serialNo, command, content,15 + content.length);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
    getLockMsgReply:function (imei,needSerialNo,serialNo,command,messageEncryptType,aesKey){
        var data = this.encode(imei,needSerialNo,serialNo, command, [],0x0F);
        return CryptoTool.encrypt(data,messageEncryptType,aesKey);
    },
}

module.exports = EncoderHelper;
