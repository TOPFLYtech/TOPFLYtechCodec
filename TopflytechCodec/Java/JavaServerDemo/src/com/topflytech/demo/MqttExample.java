package com.topflytech.demo;

import com.topflytech.codec.*;
import com.topflytech.codec.entities.*;
import io.netty.channel.Channel;

import java.io.IOException;
import java.util.List;

public class MqttExample {

    private String noObdImei = "518000518001517";
    private String obdImei = "518000518001518";
    private String personalObjImei = "518000518001519";
    private String mqttUrl = "tcp://192.168.1.251:9083";
    private SelfMqttClient noObdClient,obdClient,personalClient;
    private T880xdEncoder t880xdEncoder = new T880xdEncoder(MessageEncryptType.NONE,null);
    Decoder decoder = new Decoder(MessageEncryptType.NONE,null);
    private T880xPlusEncoder t880xPlusEncoder = new T880xPlusEncoder(MessageEncryptType.NONE,null);
    com.topflytech.codec.ObdDecoder obdDecoder = new com.topflytech.codec.ObdDecoder(MessageEncryptType.NONE,null);
    private PersonalAssetMsgEncoder personalAssetMsgEncoder = new PersonalAssetMsgEncoder(MessageEncryptType.NONE,null);
    PersonalAssetMsgDecoder personalAssetDecoder = new com.topflytech.codec.PersonalAssetMsgDecoder(MessageEncryptType.NONE,null);
    public void doMqttTest(){
        noObdClient = new SelfMqttClient(mqttUrl, noObdImei, new SelfMqttClient.MqttMsgCallback() {
            public void onReceiveMsg(byte[] hexBytes) {
                List<Message> messageList = decoder.decode(hexBytes);
                for (Message msg :
                        messageList) {
                    try {
                        dealNoObdMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        noObdClient.connect();
        obdClient = new SelfMqttClient(mqttUrl, obdImei, new SelfMqttClient.MqttMsgCallback() {
            public void onReceiveMsg(byte[] hexBytes) {
                List<Message> messageList = obdDecoder.decode(hexBytes);
                for (Message msg :
                        messageList) {
                    try {
                        dealObdMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        obdClient.connect();
        personalClient = new SelfMqttClient(mqttUrl, personalObjImei, new SelfMqttClient.MqttMsgCallback() {
            public void onReceiveMsg(byte[] hexBytes) {
                List<Message> messageList = personalAssetDecoder.decode(hexBytes);
                for (Message msg :
                        messageList) {
                    try {
                        dealPersonalAssetMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        personalClient.connect();
    }

    public void stopTest(){
        if(noObdClient != null){
            noObdClient.disconnect();
        }
        if(obdClient != null){
            obdClient.disconnect();
        }
        if(personalClient != null){
            personalClient.disconnect();
        }
    }


    private void dealPersonalAssetMessage(Message message) throws IOException {
        if (message instanceof SignInMessage){
            SignInMessage signInMessage = (SignInMessage)message;
            System.out.println("receive sign in message :" + signInMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getSignInMsgReply(signInMessage.getImei(), true, signInMessage.getSerialNo());
            personalClient.publicMessage(reply);
        }else if (message instanceof HeartbeatMessage){
            HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
            System.out.println("receive heartbeat message :" + heartbeatMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getHeartbeatMsgReply(heartbeatMessage.getImei(), true, heartbeatMessage.getSerialNo());
            personalClient.publicMessage(reply);
        }else if (message instanceof LocationInfoMessage){
            LocationInfoMessage locationInfoMessage = (LocationInfoMessage)message;
            System.out.println("receive location info message :" + locationInfoMessage.getImei());

            byte[] reply = personalAssetMsgEncoder.getLocationMsgReply(locationInfoMessage.getImei(), true, locationInfoMessage.getSerialNo(),locationInfoMessage.getProtocolHeadType());
            personalClient.publicMessage(reply);
        }else if (message instanceof LocationAlarmMessage){
            LocationAlarmMessage locationAlarmMessage = (LocationAlarmMessage)message;
            System.out.println("receive location alarm message :" + locationAlarmMessage.getImei() + " Alarm is : " + locationAlarmMessage.getOriginalAlarmCode());
            byte[] reply = personalAssetMsgEncoder.getLocationAlarmMsgReply(locationAlarmMessage.getImei(),true, locationAlarmMessage.getSerialNo(),locationAlarmMessage.getOriginalAlarmCode(), locationAlarmMessage.getProtocolHeadType());
            personalClient.publicMessage(reply);
        }else if (message instanceof ConfigMessage){
            ConfigMessage configMessage = (ConfigMessage)message;
            System.out.println("receive config message :" + configMessage.getImei() + " : " + configMessage.getConfigResultContent());
        }else if (message instanceof ForwardMessage){
            ForwardMessage forwardMessage = (ForwardMessage)message;
            System.out.println("receive forward message :" + forwardMessage.getImei() + " : " + forwardMessage.getContent());
        }else if (message instanceof USSDMessage){
            USSDMessage ussdMessage = (USSDMessage)message;
            System.out.println("receive forward message :" + ussdMessage.getImei() + " : " + ussdMessage.getContent());
        }else if (message instanceof RS232Message){
            RS232Message rs232Message = (RS232Message)message;
            System.out.println("receive RS232 message :" + rs232Message.getImei());
        }else if (message instanceof NetworkInfoMessage){
            NetworkInfoMessage NetworkInfoMessage = (NetworkInfoMessage)message;
            System.out.println("receive network info message :" + NetworkInfoMessage.getImei());
        }else if (message instanceof WifiMessage){
            WifiMessage wifiMessage = (WifiMessage)message;
            System.out.println("receive wifi message :" + wifiMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getWifiMsgReply(wifiMessage.getImei(), true, wifiMessage.getSerialNo());
            personalClient.publicMessage(reply);
        }else if (message instanceof LockMessage){
            LockMessage lockMessage = (LockMessage)message;
            System.out.println("receive lock message :" + lockMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getWifiMsgReply(lockMessage.getImei(), true, lockMessage.getSerialNo());
            personalClient.publicMessage(reply);
        }else if (message instanceof BluetoothPeripheralDataMessage){
            BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = (BluetoothPeripheralDataMessage)message;
            System.out.println("receive bluetooth ignition message :" + bluetoothPeripheralDataMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.getImei(), bluetoothPeripheralDataMessage.getSerialNo(),bluetoothPeripheralDataMessage.getProtocolHeadType());
            personalClient.publicMessage(reply);
        }
    }

    private void dealObdMessage(Message message) throws IOException {
        if (message instanceof SignInMessage){
            SignInMessage signInMessage = (SignInMessage)message;
            System.out.println("receive sign in message :" + signInMessage.getImei());
            byte[] reply = t880xdEncoder.getSignInMsgReply(signInMessage.getImei(), true, signInMessage.getSerialNo());
            obdClient.publicMessage(reply);
        }else if (message instanceof HeartbeatMessage){
            HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
            System.out.println("receive heartbeat message :" + heartbeatMessage.getImei());
            byte[] reply = t880xdEncoder.getHeartbeatMsgReply(heartbeatMessage.getImei(), true, heartbeatMessage.getSerialNo());
            obdClient.publicMessage(reply);
        }else if (message instanceof LocationInfoMessage){
            LocationInfoMessage locationInfoMessage = (LocationInfoMessage)message;
            System.out.println("receive location info message :" + locationInfoMessage.getImei());
            byte[] reply = t880xdEncoder.getLocationMsgReply(locationInfoMessage.getImei(), true, locationInfoMessage.getSerialNo(), locationInfoMessage.getProtocolHeadType());
            obdClient.publicMessage(reply);
        }else if (message instanceof LocationAlarmMessage){
            LocationAlarmMessage locationAlarmMessage = (LocationAlarmMessage)message;
            System.out.println("receive location alarm message :" + locationAlarmMessage.getImei() + " Alarm is : "+ locationAlarmMessage.getOriginalAlarmCode());
            byte[] reply = t880xdEncoder.getLocationAlarmMsgReply(locationAlarmMessage.getImei(),true, locationAlarmMessage.getSerialNo(),locationAlarmMessage.getOriginalAlarmCode(),locationAlarmMessage.getProtocolHeadType());
            obdClient.publicMessage(reply);
        }else if(message instanceof  GpsDriverBehaviorMessage){
            GpsDriverBehaviorMessage gpsDriverBehaviorMessage = (GpsDriverBehaviorMessage)message;
            System.out.println("receive gps driver behavior message :" + gpsDriverBehaviorMessage.getImei());
            System.out.println("behavior is :" + getGpsDriverBehaviorDescription(gpsDriverBehaviorMessage.getBehaviorType()));
            byte[] reply = t880xdEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.getImei(), gpsDriverBehaviorMessage.getSerialNo());
            obdClient.publicMessage(reply);

        }else if (message instanceof AccelerationDriverBehaviorMessage){
            AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = (AccelerationDriverBehaviorMessage)message;
            System.out.println("receive acceleration driver behavior message :" + accelerationDriverBehaviorMessage.getImei());
            System.out.println("behavior is :" + getGpsDriverBehaviorDescription(accelerationDriverBehaviorMessage.getBehaviorType()));
            byte[] reply = t880xdEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.getImei(), accelerationDriverBehaviorMessage.getSerialNo());
            obdClient.publicMessage(reply);
        }else if (message instanceof AccidentAccelerationMessage){
            AccidentAccelerationMessage accidentAccelerationMessage = (AccidentAccelerationMessage)message;
            System.out.println("receive accident acceleration message :" + accidentAccelerationMessage.getImei());
            byte[] reply = t880xdEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.getImei(), accidentAccelerationMessage.getSerialNo());
            obdClient.publicMessage(reply);
        }else if (message instanceof BluetoothPeripheralDataMessage){
            BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = (BluetoothPeripheralDataMessage)message;
            System.out.println("receive bluetooth ignition message :" + bluetoothPeripheralDataMessage.getImei());
            byte[] reply = t880xdEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.getImei(), bluetoothPeripheralDataMessage.getSerialNo(),bluetoothPeripheralDataMessage.getProtocolHeadType());
            obdClient.publicMessage(reply);
        }else if (message instanceof ConfigMessage){
            ConfigMessage configMessage = (ConfigMessage)message;
            System.out.println("receive config message :" + configMessage.getImei() + " : " + configMessage.getConfigResultContent());
        }else if (message instanceof ForwardMessage){
            ForwardMessage forwardMessage = (ForwardMessage)message;
            System.out.println("receive forward message :" + forwardMessage.getImei() + " : " + forwardMessage.getContent());
        }else if (message instanceof USSDMessage){
            USSDMessage ussdMessage = (USSDMessage)message;
            System.out.println("receive forward message :" + ussdMessage.getImei() + " : " + ussdMessage.getContent());
        }else if (message instanceof RS232Message){
            RS232Message rs232Message = (RS232Message)message;
            System.out.println("receive RS232 message :" + rs232Message.getImei());
        }else if (message instanceof NetworkInfoMessage){
            NetworkInfoMessage NetworkInfoMessage = (NetworkInfoMessage)message;
            System.out.println("receive network info message :" + NetworkInfoMessage.getImei());
            byte[] reply = t880xdEncoder.getNetworkMsgReply(NetworkInfoMessage.getImei(), NetworkInfoMessage.getSerialNo());
            obdClient.publicMessage(reply);
        }
    }


    private void dealNoObdMessage(Message message) throws IOException {
        if (message instanceof SignInMessage){
            SignInMessage signInMessage = (SignInMessage)message;
            System.out.println("receive sign in message :" + signInMessage.getImei());
            //8806 Plus or some new model device,need serial no,reply this message
            byte[] reply = t880xPlusEncoder.getSignInMsgReply(signInMessage.getImei(), true, signInMessage.getSerialNo());
            noObdClient.publicMessage(reply);
        }else if (message instanceof HeartbeatMessage){
            HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
            System.out.println("receive heartbeat message :" + heartbeatMessage.getImei());
            //8806 Plus or some new model device,need serial no,reply this message
            byte[] reply = t880xPlusEncoder.getHeartbeatMsgReply(heartbeatMessage.getImei(), true, heartbeatMessage.getSerialNo());
            noObdClient.publicMessage(reply);
        }else if (message instanceof LocationInfoMessage){
            LocationInfoMessage locationInfoMessage = (LocationInfoMessage)message;
            System.out.println("receive location info message :" + locationInfoMessage.getImei());
            //8806 Plus or some new model device, these is the code of 8806 plus.
            byte[] reply = t880xPlusEncoder.getLocationMsgReply(locationInfoMessage.getImei(), true, locationInfoMessage.getSerialNo(),message.getProtocolHeadType());
            noObdClient.publicMessage(reply);
        }else if (message instanceof LocationAlarmMessage){
            LocationAlarmMessage locationAlarmMessage = (LocationAlarmMessage)message;
            System.out.println("receive location alarm message :" + locationAlarmMessage.getImei() + " Alarm is : "+ locationAlarmMessage.getOriginalAlarmCode());
            //8806 Plus or some new model device,need serial no,reply this message
            byte[] reply = t880xPlusEncoder.getLocationAlarmMsgReply(locationAlarmMessage.getImei(),true, locationAlarmMessage.getSerialNo(),locationAlarmMessage.getOriginalAlarmCode(),locationAlarmMessage.getProtocolHeadType());
            noObdClient.publicMessage(reply);
        }else if(message instanceof  GpsDriverBehaviorMessage){
            GpsDriverBehaviorMessage gpsDriverBehaviorMessage = (GpsDriverBehaviorMessage)message;
            System.out.println("receive gps driver behavior message :" + gpsDriverBehaviorMessage.getImei());
            System.out.println("behavior is :" + getGpsDriverBehaviorDescription(gpsDriverBehaviorMessage.getBehaviorType()));
            //8806 Plus or some new model device,need serial no,reply this message
            byte[] reply = t880xPlusEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.getImei(), gpsDriverBehaviorMessage.getSerialNo());
            noObdClient.publicMessage(reply);

        }else if (message instanceof AccelerationDriverBehaviorMessage){
            AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = (AccelerationDriverBehaviorMessage)message;
            System.out.println("receive acceleration driver behavior message :" + accelerationDriverBehaviorMessage.getImei());
            System.out.println("behavior is :" + getGpsDriverBehaviorDescription(accelerationDriverBehaviorMessage.getBehaviorType()));
            //8806 Plus or some new model device,need serial no,reply this message
            byte[] reply = t880xPlusEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.getImei(), accelerationDriverBehaviorMessage.getSerialNo());
            noObdClient.publicMessage(reply);
        }else if (message instanceof AccidentAccelerationMessage){
            AccidentAccelerationMessage accidentAccelerationMessage = (AccidentAccelerationMessage)message;
            System.out.println("receive accident acceleration message :" + accidentAccelerationMessage.getImei());
            //8806 Plus or some new model device,need serial no,reply this message
            byte[] reply = t880xPlusEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.getImei(), accidentAccelerationMessage.getSerialNo());
            noObdClient.publicMessage(reply);
        }else if (message instanceof BluetoothPeripheralDataMessage){
            BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = (BluetoothPeripheralDataMessage)message;
            System.out.println("receive bluetooth ignition message :" + bluetoothPeripheralDataMessage.getImei());
            byte[] reply = t880xPlusEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.getImei(), bluetoothPeripheralDataMessage.getSerialNo(),bluetoothPeripheralDataMessage.getProtocolHeadType());
            noObdClient.publicMessage(reply);
        }else if (message instanceof ConfigMessage){
            ConfigMessage configMessage = (ConfigMessage)message;
            System.out.println("receive config message :" + configMessage.getImei() + " : " + configMessage.getConfigResultContent());
        }else if (message instanceof ForwardMessage){
            ForwardMessage forwardMessage = (ForwardMessage)message;
            System.out.println("receive forward message :" + forwardMessage.getImei() + " : " + forwardMessage.getContent());
        }else if (message instanceof USSDMessage){
            USSDMessage ussdMessage = (USSDMessage)message;
            System.out.println("receive forward message :" + ussdMessage.getImei() + " : " + ussdMessage.getContent());
        }else if (message instanceof RS232Message){
            RS232Message rs232Message = (RS232Message)message;
            System.out.println("receive RS232 message :" + rs232Message.getImei());
        }else if (message instanceof NetworkInfoMessage){
            NetworkInfoMessage NetworkInfoMessage = (NetworkInfoMessage)message;
            System.out.println("receive network info message :" + NetworkInfoMessage.getImei());
            byte[] reply = t880xPlusEncoder.getNetworkMsgReply(NetworkInfoMessage.getImei(), NetworkInfoMessage.getSerialNo());
            noObdClient.publicMessage(reply);
        }
    }

    public String getAccelerationDriverBehaviorDescription(int behaviorType){
        if (behaviorType == AccelerationDriverBehaviorMessage.BEHAVIOR_ACCELERATE){
            return "The vehicle bad acceleration";
        }else if (behaviorType == AccelerationDriverBehaviorMessage.BEHAVIOR_TURN_AND_BRAKE){
            return "The vehicle brake";
        }else {
            return "";
        }
    }

    public String getGpsDriverBehaviorDescription(int behaviorType){
        if (behaviorType == GpsDriverBehaviorType.HIGH_SPEED_ACCELERATE){
            return "The vehicle accelerates at the high speed.";
        }else if (behaviorType == GpsDriverBehaviorType.HIGH_SPEED_BRAKE){
            return "The vehicle brakes  at the high speed.";
        }else if (behaviorType == GpsDriverBehaviorType.MEDIUM_SPEED_ACCELERATE){
            return "The vehicle accelerates at the high speed.";
        }else if (behaviorType == GpsDriverBehaviorType.MEDIUM_SPEED_ACCELERATE){
            return "The vehicle brakes  at the high speed.";
        }else if (behaviorType == GpsDriverBehaviorType.LOW_SPEED_ACCELERATE){
            return "The vehicle accelerates at the high speed.";
        }else if (behaviorType == GpsDriverBehaviorType.LOW_SPEED_ACCELERATE){
            return "The vehicle brakes  at the high speed.";
        }else {
            return "";
        }
    }
}
