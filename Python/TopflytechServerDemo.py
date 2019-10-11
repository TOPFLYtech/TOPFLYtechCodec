#version 1.0.0
#Copyright ? 2012-2019 TOPFLYTECH Co., Limitd . All rights reserved.
from TopflytechCodec import *
import socket


def getEventDescription(eventCode):
    if eventCode ==  Event.ALARM_EXTERNAL_POWER_LOST:
            return "External power disconnect"
    elif eventCode ==  Event.ALARM_LOW_BATTERY:
        return "Low power alarm inner power voltage 3.5V "
    elif eventCode ==  Event.ALARM_SOS:
        return "SOS alarm"
    elif eventCode ==  Event.ALARM_OVER_SPEED:
        return "Over speed alarm"
    elif eventCode ==  Event.ALARM_GEOFENCE_IN:
        return "In Geofence alarm"
    elif eventCode ==  Event.ALARM_GEOFENCE_OUT:
        return "Out Geofence alarm"
    elif eventCode ==  Event.ALARM_TOWING:
        return "Drag alarm when set"
    elif eventCode ==  Event.ALARM_VIBRATION:
        return "Vibration alarm"
    elif eventCode ==  Event.ADDRESS_REQUESTED:
        return "Device apply address"
    elif eventCode ==  Event.ALARM_ANTI_THEFT:
        return "Anti-theft alarm"
    elif eventCode ==  Event.FILL_TANK:
        return "Ananlog 1 voltage increase"
    elif eventCode ==  Event.ALARM_FUEL_LEAK:
        return "Analog 1 voltage decrease"
    elif eventCode ==  Event.IGNITION:
        return "ACC from 0 to 1"
    elif eventCode ==  Event.PARKING:
        return "ACC from 1 to 0"
    elif eventCode ==  Event.AC_ON:
        return "Air conditioning opens alarm"
    elif eventCode ==  Event.AC_OFF:
        return "Air conditioning off alarm"
    elif eventCode ==  Event.IDLE_START:
        return "One time idle start, you can define the idle timing"
    elif eventCode ==  Event.IDLE_END:
        return "One time idle end"
    elif eventCode ==  Event.GSM_JAMMER_DETECTION_START:
        return "GSM jammer detection start ,this need config"
    elif eventCode ==  Event.GSM_JAMMER_DETECTION_END:
        return "GSM jammer detection end"
    elif eventCode ==  Event.ALARM_EXTERNAL_POWER_RECOVER:
        return "External power recover"
    elif eventCode ==  Event.ALARM_EXTERNAL_POWER_LOWER:
        return "External power lower than preset external power , this need config"
    elif eventCode ==  Event.ALARM_RUDE_DRIVER:
        return "Rude driver alert, this need config"
    elif eventCode ==  Event.ALARM_COLLISION:
        return "Collision alert, this need config"
    elif eventCode ==  Event.ALARM_TURN_OVER:
        return "Turn over alert, this need config"
    else:
        return ""
def getGpsDriverBehaviorDescription(behaviorType):
    if behaviorType == GpsDriverBehaviorType.HIGH_SPEED_ACCELERATE:
        return "The vehicle accelerates at the high speed."
    elif behaviorType == GpsDriverBehaviorType.HIGH_SPEED_BRAKE:
        return "The vehicle brakes  at the high speed."
    elif behaviorType == GpsDriverBehaviorType.MEDIUM_SPEED_ACCELERATE:
        return "The vehicle accelerates at the high speed."
    elif behaviorType == GpsDriverBehaviorType.MEDIUM_SPEED_ACCELERATE:
        return "The vehicle brakes  at the high speed."
    elif behaviorType == GpsDriverBehaviorType.LOW_SPEED_ACCELERATE:
        return "The vehicle accelerates at the high speed."
    elif behaviorType == GpsDriverBehaviorType.LOW_SPEED_ACCELERATE:
        return "The vehicle brakes  at the high speed."
    else:
        return ""



t880xPlusEncoder = T880xPlusEncoder(MessageEncryptType.NONE,"")
t880xdEncoder = T880xdEncoder(MessageEncryptType.NONE,"")
personalEncoder = PersonalAssetMsgEncoder(MessageEncryptType.NONE,"")

def dealObdDeviceMessage(message,socketClient):
    if isinstance(message,SignInMessage):
        print "receive signInMessage: " + message.imei
        reply = t880xdEncoder.getSignInMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,HeartbeatMessage):
        print "receive heartbeatMessage" + message.imei
        reply = t880xdEncoder.getHeartbeatMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,LocationInfoMessage):
        print "receive locationInfoMessage" + message.imei
    elif isinstance(message,LocationAlarmMessage):
        print "receive locationAlarmMessage" + message.imei + "Alarm is : " + getEventDescription(message.alarm)
        # some new model device,need serial no,reply this message
        reply = t880xdEncoder.getLocationAlarmMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode)
        socketClient.send(reply)
    elif isinstance(message,GpsDriverBehaviorMessage):
        print "receive gpsDriverBehaviorMessage" + message.imei + " behavior is :" + getGpsDriverBehaviorDescription(message.behaviorType)
        reply = t880xdEncoder.getGpsDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,AccelerationDriverBehaviorMessage):
        print "receive accelerationDriverBehaviorMessage" + message.imei + " behavior is :" + getGpsDriverBehaviorDescription(message.behaviorType)
        reply = t880xdEncoder.getAccelerationDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,AccidentAccelerationMessage):
        print "receive accidentAccelerationMessage" + message.imei
        reply = t880xdEncoder.getAccelerationAlarmMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,ConfigMessage):
        print "receive configMessage: " + message.imei + " : " + message.configContent
    elif isinstance(message,ForwardMessage):
        print "receive forwardMessage: " + message.imei + " : " + message.content
    elif isinstance(message,USSDMessage):
        print "receive forwardMessage: " + message.imei + " : " + message.content
    elif isinstance(message,ObdMessage):
        print "receive OBD Message: " + message.imei
        reply = t880xdEncoder.getObdMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,BluetoothPeripheralDataMessage):
        print "receive blue Message: " + message.imei
        reply = t880xdEncoder.getBluetoothPeripheralMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,NetworkInfoMessage):
        print "receive network info Message: " + message.imei


def dealNoObdDeviceMessage(message,socketClient):
    """
    Device model like :8806 plus, use this method.
    :param message:
    :param socketClient:
    :return:
    """
    if isinstance(message,SignInMessage):
        print "receive signInMessage: " + message.imei
        #8806 Plus or some new model device,need serial no,reply this message
        reply = t880xPlusEncoder.getSignInMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,HeartbeatMessage):
        print "receive heartbeatMessage" + message.imei
        #8806 Plus or some new model device,need serial no,reply this message
        reply = t880xPlusEncoder.getHeartbeatMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,LocationInfoMessage):
        print "receive locationInfoMessage" + message.imei
        #8806 Plus or some new model device, these is the code of 8806 plus.
    elif isinstance(message,LocationAlarmMessage):
        print "receive locationAlarmMessage" + message.imei + "Alarm is : " + getEventDescription(message.alarm)
        #8806 Plus or some new model device,need serial no,reply this message
        reply = t880xPlusEncoder.getLocationAlarmMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode)
        socketClient.send(reply)
    elif isinstance(message,GpsDriverBehaviorMessage):
        print "receive gpsDriverBehaviorMessage" + message.imei + " behavior is :" + getGpsDriverBehaviorDescription(message.behaviorType)
        reply = t880xPlusEncoder.getGpsDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,AccelerationDriverBehaviorMessage):
        print "receive accelerationDriverBehaviorMessage" + message.imei + " behavior is :" + getGpsDriverBehaviorDescription(message.behaviorType)
        reply = t880xPlusEncoder.getAccelerationDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,AccidentAccelerationMessage):
        print "receive accidentAccelerationMessage" + message.imei
        #8806 Plus or some new model device,need serial no,reply this message
        reply = t880xPlusEncoder.getAccelerationAlarmMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,ConfigMessage):
        print "receive configMessage: " + message.imei + " : " + message.configContent
    elif isinstance(message,ForwardMessage):
        print "receive forwardMessage: " + message.imei + " : " + message.content
    elif isinstance(message,USSDMessage):
        print "receive forwardMessage: " + message.imei + " : " + message.content
    elif isinstance(message,RS232Message):
        print "receive RS232 Message: " + message.imei
        reply = t880xPlusEncoder.getRS232MsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,BluetoothPeripheralDataMessage):
        print "receive blue Message: " + message.imei
        reply = t880xPlusEncoder.getBluetoothPeripheralMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,NetworkInfoMessage):
        print "receive network info Message: " + message.imei

def dealPersonalDeviceMessage(message,socketClient):
    """
    Device model like :8806 plus, use this method.
    :param message:
    :param socketClient:
    :return:
    """
    if isinstance(message,SignInMessage):
        print "receive signInMessage: " + message.imei
        reply = personalEncoder.getSignInMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,HeartbeatMessage):
        print "receive heartbeatMessage" + message.imei
        reply = personalEncoder.getHeartbeatMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,LocationInfoMessage):
        print "receive locationInfoMessage" + message.imei
    elif isinstance(message,LocationAlarmMessage):
        print "receive locationAlarmMessage" + message.imei + "Alarm is : " + getEventDescription(message.alarm)
        reply = personalEncoder.getLocationAlarmMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode)
        socketClient.send(reply)
    elif isinstance(message,ConfigMessage):
        print "receive configMessage: " + message.imei + " : " + message.configContent
    elif isinstance(message,ForwardMessage):
        print "receive forwardMessage: " + message.imei + " : " + message.content
    elif isinstance(message,USSDMessage):
        print "receive forwardMessage: " + message.imei + " : " + message.content
    elif isinstance(message,NetworkInfoMessage):
        print "receive network info Message: " + message.imei

if __name__ == "__main__":
    HOST, PORT = "192.168.1.8", 1001
    s = socket.socket()
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((HOST, PORT))
    s.listen(5)
    print("Listening on address %s. Kill server with Ctrl-C" %
      str((HOST, PORT)))
    while True:
        c, addr = s.accept()
        print("\nConnection received from %s" % str(addr))
        # decoder = Decoder(MessageEncryptType.NONE,"")
        # decoder = ObdDecoder(MessageEncryptType.NONE,"")
        decoder = PersonalAssetMsgDecoder(MessageEncryptType.NONE,"")
        while True:
            data = c.recv(2048)
            if not data:
                print("End of file from client. Resetting")
                break

            messageList = decoder.decode(data)
            for message in messageList:
                # dealNoObdDeviceMessage(message,c)
                # dealObdDeviceMessage(message,c)
                dealPersonalDeviceMessage(message,c)

        c.close()





