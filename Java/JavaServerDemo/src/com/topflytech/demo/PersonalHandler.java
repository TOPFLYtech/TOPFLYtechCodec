package com.topflytech.demo;

import com.topflytech.codec.PersonalAssetMsgEncoder;
import com.topflytech.codec.entities.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;


/**
 * The type T 880 x handler.
 */
public class PersonalHandler extends SimpleChannelInboundHandler<Message> {

    private PersonalAssetMsgEncoder personalAssetMsgEncoder = new PersonalAssetMsgEncoder(MessageEncryptType.NONE,null);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        Channel channel = channelHandlerContext.channel();
        dealDeviceMessage(message, channel);
    }


    /**
     *
     * @param message
     * @param channel
     * @throws IOException
     */
    private void dealDeviceMessage(Message message, Channel channel) throws IOException {
        if (message instanceof SignInMessage){
            SignInMessage signInMessage = (SignInMessage)message;
            System.out.println("receive sign in message :" + signInMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getSignInMsgReply(signInMessage.getImei(), true, signInMessage.getSerialNo());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply sign in success" : "reply sign in fail");
                }
            });
        }else if (message instanceof HeartbeatMessage){
            HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
            System.out.println("receive heartbeat message :" + heartbeatMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getHeartbeatMsgReply(heartbeatMessage.getImei(), true, heartbeatMessage.getSerialNo());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply heartbeat success" : "reply heartbeat fail");
                }
            });
        }else if (message instanceof LocationInfoMessage){
            LocationInfoMessage locationInfoMessage = (LocationInfoMessage)message;
            System.out.println("receive location info message :" + locationInfoMessage.getImei());

//            byte[] reply = personalAssetMsgEncoder.getLocationMsgReply(locationInfoMessage.getImei(), true, locationInfoMessage.getSerialNo());
//            Utils.write(channel, reply, new Utils.WriteListener() {
//                @Override
//                public void messageRespond(boolean success) {
//                    System.out.println(success ? "reply location info success" : "reply location info fail");
//                }
//            });
        }else if (message instanceof LocationAlarmMessage){
            LocationAlarmMessage locationAlarmMessage = (LocationAlarmMessage)message;
            System.out.println("receive location alarm message :" + locationAlarmMessage.getImei() + " Alarm is : " + locationAlarmMessage.getOriginalAlarmCode());
            byte[] reply = personalAssetMsgEncoder.getLocationAlarmMsgReply(locationAlarmMessage.getImei(),true, locationAlarmMessage.getSerialNo(),locationAlarmMessage.getOriginalAlarmCode());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply location alarm success" : "reply location info fail");
                }
            });
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
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply wifi message success" : "reply location info fail");
                }
            });
        }else if (message instanceof LockMessage){
            LockMessage lockMessage = (LockMessage)message;
            System.out.println("receive lock message :" + lockMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getLockMsgReply(lockMessage.getImei(), true, lockMessage.getSerialNo(), lockMessage.getProtocolHeadType());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply lock message success" : "reply location info fail");
                }
            });
        }else if (message instanceof LockMessage){
            LockMessage lockMessage = (LockMessage)message;
            System.out.println("receive lock message :" + lockMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getWifiMsgReply(lockMessage.getImei(), true, lockMessage.getSerialNo());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply lock message success" : "reply location info fail");
                }
            });
        }else if (message instanceof BluetoothPeripheralDataMessage){
            BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = (BluetoothPeripheralDataMessage)message;
            System.out.println("receive bluetooth ignition message :" + bluetoothPeripheralDataMessage.getImei());
            byte[] reply = personalAssetMsgEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.getImei(), bluetoothPeripheralDataMessage.getSerialNo(),bluetoothPeripheralDataMessage.getProtocolHeadType());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply bluetooth ignition success" : "reply bluetooth ignition fail");
                }
            });
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
