var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
const TopflytechByteBuf = require("./TopflytechByteBuf")

var PTTDecoder = {
    HEADER_LENGTH:3,
    HEARTBEAT :  [0x28, 0x28, 0x03],
    TALK_START :  [0x28, 0x28, 0x04],
    TALK_END :  [0x28, 0x28, 0x05],
    VOICE_DATA :  [0x28, 0x28, 0x06],
    encryptType:0,
    aesKey:"",
    match:function (bytes){
        if(bytes.length != this.HEADER_LENGTH){
            return false
        }
        return ByteUtils.arrayEquals(this.HEARTBEAT, bytes)
            || ByteUtils.arrayEquals(this.TALK_START, bytes)
            || ByteUtils.arrayEquals(this.TALK_END, bytes)
            || ByteUtils.arrayEquals(this.VOICE_DATA, bytes) ;
    },

    decode(buf){
        TopflytechByteBuf.putBuf(buf);
        var messages = [];
        if (TopflytechByteBuf.getReadableBytes() < (this.HEADER_LENGTH + 2)){
            return messages;
        }
        var foundHead = false;
        var bytes= [3]
        while (TopflytechByteBuf.getReadableBytes() > 5){
            TopflytechByteBuf.markReaderIndex();
            bytes[0] = TopflytechByteBuf.getByte(0);
            bytes[1] = TopflytechByteBuf.getByte(1);
            bytes[2] = TopflytechByteBuf.getByte(2);
            if (this.match(bytes)){
                foundHead = true;
                TopflytechByteBuf.skipBytes(this.HEADER_LENGTH);
                var lengthBytes = TopflytechByteBuf.readBytes(2);
                var packageLength = ByteUtils.byteToShort(lengthBytes, 0);
                if (this.encryptType == CryptoTool.MessageEncryptType.MD5){
                    packageLength = packageLength + 8;
                }else if(this.encryptType == CryptoTool.MessageEncryptType.AES){
                    packageLength = CryptoTool.AES.getAesLength(packageLength);
                }
                TopflytechByteBuf.resetReaderIndex();
                if(packageLength <= 0){
                    TopflytechByteBuf.skipBytes(5);
                    break;
                }
                if (packageLength > TopflytechByteBuf.getReadableBytes()){
                    break;
                }
                var data = TopflytechByteBuf.readBytes(packageLength);
                data = CryptoTool.decryptData(data, this.encryptType, this.aesKey);
                if (data != null){
                    try {
                        var message = this.build(data);
                        if (message != null){
                            messages.push(message);
                        }
                    }catch (e){
                        console.log(e)
                    }
                }
            }else{
                TopflytechByteBuf.skipBytes(1);
            }
        }
        return messages
    },
    build:function(bytes){
        if (bytes != null && bytes.length > this.HEADER_LENGTH
            && ((bytes[0] == 0x28 && bytes[1] == 0x28))) {
            switch (bytes[2]) { 
                case 0x03:
                    var heartbeatMessage = this.parseHeartbeat(bytes);
                    return heartbeatMessage;
                case 0x04:
                    var talkStartMessage = parseTalkStartMessage(bytes);
                    return talkStartMessage;
                case 0x05:
                    var talkEndMessage = parseTalkEndMessage(bytes);
                    return talkEndMessage;
                case 0x06:
                    var voiceMessage = parseVoiceMessage(bytes);
                    return voiceMessage;
                default:
                    return null;
            }
        }
    },
    parseHeartbeat:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var heartbeatMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"heartbeat",
        }
        return heartbeatMessage
    },
    parseTalkStartMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var talkStartMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"talkStart",
        }
        return talkStartMessage
    },
    parseTalkEndMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var talkEndMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"talkEnd",
        }
        return talkEndMessage
    },
    parseVoiceMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var voiceMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"talkVoice",
        }
        voiceMessage.encodeType = bytes[15]
        var voiceLen = ByteUtils.byteToShort(bytes,16);
        if(bytes.length >= 18 + voiceLen){
            var voiceData = ByteUtils.arrayOfRange(bytes,18,18 + voiceLen)
            voiceMessage.voiceData = voiceData 
        }
        return voiceMessage
    },
}


module.exports = PTTDecoder;