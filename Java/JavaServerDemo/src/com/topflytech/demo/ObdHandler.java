package com.topflytech.demo;

import com.topflytech.codec.Event;
import com.topflytech.codec.T880xdEncoder;
import com.topflytech.codec.entities.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;


/**
 * The type T 880 x handler.
 */
public class ObdHandler extends SimpleChannelInboundHandler<Message> {

    private T880xdEncoder t880xdEncoder = new T880xdEncoder(MessageEncryptType.NONE,null);

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
            byte[] reply = t880xdEncoder.getSignInMsgReply(signInMessage.getImei(), true, signInMessage.getSerialNo());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply sign in success" : "reply sign in fail");
                }
            });
        }else if (message instanceof HeartbeatMessage){
            HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
            System.out.println("receive heartbeat message :" + heartbeatMessage.getImei());
            byte[] reply = t880xdEncoder.getHeartbeatMsgReply(heartbeatMessage.getImei(), true, heartbeatMessage.getSerialNo());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply heartbeat success" : "reply heartbeat fail");
                }
            });
        }else if (message instanceof LocationInfoMessage){
            LocationInfoMessage locationInfoMessage = (LocationInfoMessage)message;
            System.out.println("receive location info message :" + locationInfoMessage.getImei());

//            byte[] reply = t880xdEncoder.getLocationMsgReply(locationInfoMessage.getImei(), true, locationInfoMessage.getSerialNo());
//            Utils.write(channel, reply, new Utils.WriteListener() {
//                @Override
//                public void messageRespond(boolean success) {
//                    System.out.println(success ? "reply location info success" : "reply location info fail");
//                }
//            });
        }else if (message instanceof LocationAlarmMessage){
            LocationAlarmMessage locationAlarmMessage = (LocationAlarmMessage)message;
            System.out.println("receive location alarm message :" + locationAlarmMessage.getImei() + " Alarm is : "+ getEventDescription(locationAlarmMessage.getAlarm()));
            byte[] reply = t880xdEncoder.getLocationAlarmMsgReply(locationAlarmMessage.getImei(),true, locationAlarmMessage.getSerialNo(),locationAlarmMessage.getOriginalAlarmCode());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply location alarm success" : "reply location info fail");
                }
            });
        }else if(message instanceof  GpsDriverBehaviorMessage){
            GpsDriverBehaviorMessage gpsDriverBehaviorMessage = (GpsDriverBehaviorMessage)message;
            System.out.println("receive gps driver behavior message :" + gpsDriverBehaviorMessage.getImei());
            System.out.println("behavior is :" + getGpsDriverBehaviorDescription(gpsDriverBehaviorMessage.getBehaviorType()));
            byte[] reply = t880xdEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.getImei(), gpsDriverBehaviorMessage.getSerialNo());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply gps driver behavior message success" : "reply gps driver behavior message fail");
                }
            });

        }else if (message instanceof AccelerationDriverBehaviorMessage){
            AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = (AccelerationDriverBehaviorMessage)message;
            System.out.println("receive acceleration driver behavior message :" + accelerationDriverBehaviorMessage.getImei());
            System.out.println("behavior is :" + getGpsDriverBehaviorDescription(accelerationDriverBehaviorMessage.getBehaviorType()));
            byte[] reply = t880xdEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.getImei(), accelerationDriverBehaviorMessage.getSerialNo());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply acceleration driver behavior message success" : "reply acceleration driver behavior message fail");
                }
            });
        }else if (message instanceof AccidentAccelerationMessage){
            AccidentAccelerationMessage accidentAccelerationMessage = (AccidentAccelerationMessage)message;
            System.out.println("receive accident acceleration message :" + accidentAccelerationMessage.getImei());
            byte[] reply = t880xdEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.getImei(), accidentAccelerationMessage.getSerialNo());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply acceleration alarm success" : "reply acceleration alarm fail");
                }
            });
        }else if (message instanceof BluetoothIgnitionMessage){
            BluetoothIgnitionMessage bluetoothIgnitionMessage = (BluetoothIgnitionMessage)message;
            System.out.println("receive bluetooth ignition message :" + bluetoothIgnitionMessage.getImei());
            byte[] reply = t880xdEncoder.getBluetoothPeripheralMsgReply(bluetoothIgnitionMessage.getImei(), bluetoothIgnitionMessage.getSerialNo());
            Utils.write(channel, reply, new Utils.WriteListener() {
                @Override
                public void messageRespond(boolean success) {
                    System.out.println(success ? "reply bluetooth ignition success" : "reply bluetooth ignition fail");
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

    public String getEventDescription(int eventCode) {
        if(eventCode ==  Event.ALARM_EXTERNAL_POWER_LOST)
            return "External power disconnect;";
        else if(eventCode ==  Event.ALARM_LOW_BATTERY)
            return "Low power alarm(inner power voltage 3.5V)";
        else if(eventCode ==  Event.ALARM_SOS)
            return "SOS alarm";
        else if(eventCode ==  Event.ALARM_OVER_SPEED)
            return "Over speed alarm";
        else if(eventCode ==  Event.ALARM_GEOFENCE_IN)
            return "In Geofence alarm";
        else if(eventCode ==  Event.ALARM_GEOFENCE_OUT)
            return "Out Geofence alarm";
        else if(eventCode ==  Event.ALARM_TOWING)
            return "Drag alarm when set;";
        else if(eventCode ==  Event.ALARM_VIBRATION)
            return "Vibration alarm";
        else if(eventCode ==  Event.ADDRESS_REQUESTED)
            return "Device apply address";
        else if(eventCode ==  Event.ALARM_ANTI_THEFT)
            return "Anti-theft alarm";
        else if(eventCode ==  Event.FILL_TANK)
            return "Ananlog 1 voltage increase";
        else if(eventCode ==  Event.ALARM_FUEL_LEAK)
            return "Analog 1 voltage decrease";
        else if(eventCode ==  Event.IGNITION)
            return "ACC from 0 to 1";
        else if(eventCode ==  Event.PARKING)
            return "ACC from 1 to 0";
        else if(eventCode ==  Event.AC_ON)
            return "Air conditioning opens alarm";
        else if(eventCode ==  Event.AC_OFF)
            return "Air conditioning off alarm";
        else if(eventCode ==  Event.IDLE_START)
            return "One time idle start, you can define the idle timing";
        else if(eventCode ==  Event.IDLE_END)
            return "One time idle end";
        else if(eventCode ==  Event.GSM_JAMMER_DETECTION_START)
            return "GSM jammer detection start ,this need config";
        else if(eventCode ==  Event.GSM_JAMMER_DETECTION_END)
            return "GSM jammer detection end";
        else if(eventCode ==  Event.ALARM_EXTERNAL_POWER_RECOVER)
            return "External power recover";
        else if(eventCode ==  Event.ALARM_EXTERNAL_POWER_LOWER)
            return "External power lower than preset external power , this need config";
        else if(eventCode ==  Event.ALARM_RUDE_DRIVER)
            return "Rude driver alert, this need config";
        else if(eventCode ==  Event.ALARM_COLLISION)
            return "Collision alert, this need config";
        else if(eventCode ==  Event.ALARM_TURN_OVER)
            return "Turn over alert, this need config";
        else
            return "";
    }
}
