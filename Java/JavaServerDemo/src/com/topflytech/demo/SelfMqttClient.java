package com.topflytech.demo;

import org.eclipse.paho.client.mqttv3.*;

public class SelfMqttClient {

    String brokerUrl = "tcp://192.168.1.251:9083";
    String imei = ""; //
    String subTopic;
    String pubTopic;
    private MqttClient mqttClient;
    private MqttMsgCallback msgCb;
    public SelfMqttClient(String brokerUrl, String imei, MqttMsgCallback callback) {
        this.brokerUrl = brokerUrl;
        this.imei = imei;
        this.subTopic = imei + "_S";
        this.pubTopic = imei + "_R";
        this.msgCb = callback;
    }

    public void publicMessage(byte[] hexBytes){
        if(!mqttClient.isConnected()){
            return;
        }
        String hexString = Utils.bytes2HexString(hexBytes,0);
        MqttMessage message = new MqttMessage(hexString.getBytes());
        int qos = 1;
        message.setQos(qos);
        try {
            mqttClient.publish(pubTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        try {
            if(!mqttClient.isConnected()){
                return;
            }
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connect(){
        try {
            // 创建 MQTT 客户端实例
            mqttClient = new MqttClient(brokerUrl, imei);

            // 设置回调函数
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String subTopic, MqttMessage message) {
                    try{
                        String payload = new String(message.getPayload());
                        System.out.println("Received message on subTopic " + subTopic + ": " + payload);
                        byte[] hexBytes = Utils.hexString2Bytes(payload);
                        if(msgCb != null){
                            msgCb.onReceiveMsg(hexBytes);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Delivery complete for token: " + token.toString());
                }
            });

            // 连接到 MQTT 代理
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            mqttClient.connect(options);

            if (mqttClient.isConnected()) {
                System.out.println("Connected to MQTT Broker at " + mqttClient.getServerURI());

                // 订阅主题
                int qos = 0; // Quality of Service level
                mqttClient.subscribe(subTopic, qos);
                System.out.println("Successfully subscribed to subTopic: " + subTopic + " with QoS " + qos);


            } else {
                System.out.println("Failed to connect to MQTT Broker.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface MqttMsgCallback {
        void onReceiveMsg(byte[] hexBytes);
    }
}
