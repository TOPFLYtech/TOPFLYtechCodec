const mqtt = require('mqtt');
var ByteUtils = require("./ByteUtils")
var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
var Decoder = require("./Decoder")
var Encoder = require("./Encoder")
var ObdEncoder = require("./ObdEncoder")
var ObdDecoder = require("./ObdDecoder")
var PersonalAssetDecoder = require("./PersonalAssetDecoder")
var PersonalAssetEncoder = require("./PersonalAssetEncoder")
var moment = require("moment")
Decoder.encryptType = CryptoTool.MessageEncryptType.NONE
var curDecoder = PersonalAssetDecoder
var curEncoder = PersonalAssetEncoder


class MqttClient {
    constructor(imei,handleMessageCallback) {
        this.brokerUrl = "mqtt://192.168.1.251:9083";
        this.imei = imei;
        this.client = null;
        this.subTopic = `${this.imei}_S`;
        this.pubTopic = `${this.imei}_R`;
        this.handleMessageCallback = handleMessageCallback || this.defaultHandleMessage.bind(this);
    }

    defaultHandleMessage(message) {
        console.log(`Default handling for message on topic:`, message);
    }

    connect() {
        return new Promise((resolve, reject) => {
            this.client = mqtt.connect(this.brokerUrl);

            this.client.on('connect', () => {
                console.log(`Connected to MQTT Broker at ${this.brokerUrl}`);


                this.client.subscribe(this.subTopic, (err) => {
                    if (!err) {
                        console.log(`Subscribed to subTopic: ${this.subTopic}`);
                        resolve();
                    } else {
                        console.error('Failed to subscribe to subTopic:', err);
                        reject(err);
                    }
                });
            });

            this.client.on('error', (err) => {
                console.error('MQTT Connection Error:', err);
                reject(err);
            });

            this.client.on('message', (receivedTopic, receivedMessage) => {
                this.handleMessage(receivedTopic, receivedMessage);
            });
        });
    }

    publish(message) {
        if (!this.client || !this.client.connected) {
            throw new Error('Not connected to MQTT Broker');
        }
        var sendHexStr = ByteUtils.bytes2HexString(message,0)
        this.client.publish(this.pubTopic, Buffer.from(sendHexStr,"ascii"));
        console.log(`Published message to subTopic ${this.pubTopic}: ${sendHexStr}`);
    }

    handleMessage(subTopic, message) {
        const asciiMessage = message.toString('ascii');
        console.log(`Received message on subTopic ${subTopic}:`, asciiMessage);
        var hexBytesArray = ByteUtils.hexStringToByte(asciiMessage)

        this.handleMessageCallback(hexBytesArray);
    }

    disconnect() {
        if (this.client && this.client.connected) {
            this.client.end(() => {
                console.log('Disconnected from MQTT Broker');
            });
        }
    }
}

module.exports = MqttClient;





