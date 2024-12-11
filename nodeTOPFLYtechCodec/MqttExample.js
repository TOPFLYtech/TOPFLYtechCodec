const mqtt = require('mqtt');
var ByteUtils = require("./ByteUtils")
var CryptoTool = require("./CryptoTool")
var Decoder = require("./Decoder")
var Encoder = require("./Encoder")
var ObdEncoder = require("./ObdEncoder")
var ObdDecoder = require("./ObdDecoder")
var PersonalAssetDecoder = require("./PersonalAssetDecoder")
var PersonalAssetEncoder = require("./PersonalAssetEncoder")
var moment = require("moment")
Decoder.encryptType = CryptoTool.MessageEncryptType.NONE
var curDecoder = Decoder
var curEncoder = Encoder
const MqttClient = require('./MqttClient');

const imei = '518000518001517';

const client = new MqttClient(imei, customHandleMessage);


// 自定义处理消息的回调函数
function customHandleMessage(message) {
    var messageList = curDecoder.decode(message)
    for(var messageIndex in messageList){
        var message = messageList[messageIndex]
        if(message.messageType == "signIn"){
            console.log("receive signIn message,imei:" + message.imei)
            var resp = curEncoder.getSignInMsgReply(message.imei,true,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
            client.publish(resp)
        }else if(message.messageType == "heartbeat"){
            console.log("receive heartbeat message,imei:" + message.imei)
            var resp = curEncoder.getHeartbeatMsgReply(message.imei,true,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "location"){
            console.log("receive location message,imei:" + message.imei + "," + moment(message.date._d).format("YYYY-MM-DD HH:mm:ss"))
            var resp = []
            if (message.isAlarmData){
                resp = curEncoder.getLocationAlarmMsgReply(message.imei,true,message.serialNo,message.originalAlarmCode,message.protocolHeadType,CryptoTool.MessageEncryptType.NONE,null)
            }else{
                resp = curEncoder.getLocationMsgReply(message.imei,true,message.serialNo,message.protocolHeadType,CryptoTool.MessageEncryptType.NONE,null)
            }
             client.publish(resp)
        }else if(message.messageType == "config"){
            console.log("receive config message,imei:" + message.imei + ",config content:" + message.content)
        }else if(message.messageType == "gpsDriverBehavior"){
            console.log("receive gpsDriverBehavior message,imei:" + message.imei)
            var resp = curEncoder.getGpsDriverBehaviorMsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "accelerationDriverBehavior"){
            console.log("receive accelerationDriverBehavior message,imei:" + message.imei)
            var resp = curEncoder.getAccelerationDriverBehaviorMsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "accidentAcceleration"){
            console.log("receive accidentAcceleration message,imei:" + message.imei)
            var resp = curEncoder.getAccelerationAlarmMsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "rs232Data"){
            console.log("receive rs232Data message,imei:" + message.imei)
            var resp = Encoder.getRS232MsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "bluetoothData"){
            console.log("receive bluetoothData message,imei:" + message.imei)
            var resp = curEncoder.getBluetoothPeripheralMsgReply(message.imei,message.serialNo,message.protocolHeadType,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "networkInfo"){
            console.log("receive networkInfo message,imei:" + message.imei)
            var resp = curEncoder.getNetworkMsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "wifi"){
            console.log("receive wifi message,imei:" + message.imei)
            var resp = curEncoder.getWifiMsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "lock"){
            console.log("receive lock message,imei:" + message.imei)
            var resp = PersonalAssetEncoder.getLockMsgReply(message.imei,true,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "obdData"){
            console.log("receive obd message,imei:" + message.imei)
            var resp = curEncoder.getObdMsgReply(message.imei,true,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "innerGeoData"){
            console.log("receive inner geo message,imei:" + message.imei)
            var resp = PersonalAssetEncoder.getInnerGeoDataMsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "wifiWithDeviceInfo"){
            console.log("receive wifi with device information message,imei:" + message.imei)
            var resp = PersonalAssetEncoder.getWifiWithDeviceInfoMsgReply(message.imei,message.serialNo,message.originalAlarmCode,message.protocolHeadType,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "rs485"){
            console.log("receive rs485 message,imei:" + message.imei)
            var resp = Encoder.getRs485MsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "oneWire"){
            console.log("receive oneWire message,imei:" + message.imei)
            var resp = Encoder.getOneWireMsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }else if(message.messageType == "deviceTempCollection"){
            console.log("receive device out temperature collection message,imei:" + message.imei)
            var resp = PersonalAssetEncoder.getDeviceTempCollectionMsgReply(message.imei,message.serialNo,CryptoTool.MessageEncryptType.NONE,null)
             client.publish(resp)
        }
    }
}


client.connect()
    .then(() => {
        // connect succ
    })
    .catch((error) => {
        console.error('Connection failed:', error);
    });


