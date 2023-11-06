#version 1.0.0
#Copyright ? 2012-2019 TOPFLYTECH Co., Limitd . All rights reserved.
from TopflytechCodec import *
import socket



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
        print ("receive signInMessage: " + message.imei)
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getSignInMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getSignInMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,HeartbeatMessage):
        print ("receive heartbeatMessage" + message.imei)
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getHeartbeatMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getHeartbeatMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,LocationInfoMessage):
        print ("receive locationInfoMessage" + message.imei)
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getLocationMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getLocationMsgReply(message.imei,True,message.serialNo,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,LocationAlarmMessage):
        print ("receive locationAlarmMessage" + message.imei + " Alarm is : " + str(message.originalAlarmCode))
        # some new model device,need serial no,reply this message
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getLocationAlarmMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getLocationAlarmMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,GpsDriverBehaviorMessage):
        print ("receive gpsDriverBehaviorMessage" + message.imei + " behavior is :" + getGpsDriverBehaviorDescription(message.behaviorType))
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getGpsDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getGpsDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,AccelerationDriverBehaviorMessage):
        print ("receive accelerationDriverBehaviorMessage" + message.imei + " behavior is :" + getGpsDriverBehaviorDescription(message.behaviorType))
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getAccelerationDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getAccelerationDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,AccidentAccelerationMessage):
        print ("receive accidentAccelerationMessage" + message.imei)
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getAccelerationAlarmMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getAccelerationAlarmMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,ConfigMessage):
        print ("receive configMessage: " + message.imei + " : " + message.configContent)
    elif isinstance(message,ForwardMessage):
        print ("receive forwardMessage: " + message.imei + " : " + message.content)
    elif isinstance(message,USSDMessage):
        print ("receive forwardMessage: " + message.imei + " : " + message.content)
    elif isinstance(message,ObdMessage):
        print ("receive OBD Message: " + message.imei)
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getObdMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getObdMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,WifiMessage):
        print ("receive WIFI Message: " + message.imei)
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getObdMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getWifiMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,BluetoothPeripheralDataMessage):
        print ("receive blue Message: " + message.imei)
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getBluetoothPeripheralMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getBluetoothPeripheralMsgReply(message.imei,True,message.serialNo,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,NetworkInfoMessage):
        print ("receive network info Message: " + message.imei)
        # if message.isNeedResp:
        #     reply = t880xdEncoder.getNetworkMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xdEncoder.getNetworkMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)


def dealNoObdDeviceMessage(message,socketClient):
    """
    Device model like :8806 plus, use this method.
    :param message:
    :param socketClient:
    :return:
    """
    if isinstance(message,SignInMessage):
        print ("receive signInMessage: " + message.imei)
        #8806 Plus or some new model device,need serial no,reply this message
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getSignInMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getSignInMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,HeartbeatMessage):
        print ("receive heartbeatMessage" + message.imei)
        #8806 Plus or some new model device,need serial no,reply this message
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getHeartbeatMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getHeartbeatMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,LocationInfoMessage):
        print ("receive locationInfoMessage" + message.imei)
        #8806 Plus or some new model device, these is the code of 8806 plus.
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getLocationMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode,message.protocolHeadType)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getLocationMsgReply(message.imei,True,message.serialNo,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,LocationAlarmMessage):
        print ("receive locationAlarmMessage" + message.imei + "Alarm is : " + str(message.originalAlarmCode))
        #8806 Plus or some new model device,need serial no,reply this message
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getLocationAlarmMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode,message.protocolHeadType)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getLocationAlarmMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,GpsDriverBehaviorMessage):
        print ("receive gpsDriverBehaviorMessage" + message.imei + " behavior is :" + getGpsDriverBehaviorDescription(message.behaviorType))
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getGpsDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getGpsDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,AccelerationDriverBehaviorMessage):
        print ("receive accelerationDriverBehaviorMessage" + message.imei + " behavior is :" + getGpsDriverBehaviorDescription(message.behaviorType))
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getAccelerationDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getAccelerationDriverBehaviorMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,AccidentAccelerationMessage):
        print ("receive accidentAccelerationMessage" + message.imei)
        #8806 Plus or some new model device,need serial no,reply this message
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getAccelerationAlarmMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getAccelerationAlarmMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,ConfigMessage):
        print ("receive configMessage: " + message.imei + " : " + message.configContent)
    elif isinstance(message,ForwardMessage):
        print ("receive forwardMessage: " + message.imei + " : " + message.content)
    elif isinstance(message,USSDMessage):
        print ("receive forwardMessage: " + message.imei + " : " + message.content)
    elif isinstance(message,RS232Message):
        print ("receive RS232 Message: " + message.imei)
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getRS232MsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getRS232MsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,BluetoothPeripheralDataMessage):
        print ("receive blue Message: " + message.imei)
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getBluetoothPeripheralMsgReply(message.imei,True,message.serialNo,message.protocolHeadType)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getBluetoothPeripheralMsgReply(message.imei,True,message.serialNo,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,NetworkInfoMessage):
        print ("receive network info Message: " + message.imei)
        # if message.isNeedResp:
        #     reply = t880xPlusEncoder.getNetworkMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = t880xPlusEncoder.getNetworkMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,WifiMessage):
        print ("receive wifi Message: " + message.imei)
        reply = t880xPlusEncoder.getWifiMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,OneWireMessage):
        print ("receive one wire Message: " + message.imei)
        reply = t880xPlusEncoder.getOneWireMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,RS485Message):
        print ("receive rs485 Message: " + message.imei)
        reply = t880xPlusEncoder.getRs485MsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,ObdMessage):
        print ("receive DTC(OBD) Message: " + message.imei)
        reply = t880xPlusEncoder.getObdMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)

def dealPersonalDeviceMessage(message,socketClient):
    """
    Device model like :8806 plus, use this method.
    :param message:
    :param socketClient:
    :return:
    """
    if isinstance(message,SignInMessage):
        print ("receive signInMessage: " + message.imei)
        # if message.isNeedResp:
        #     reply = personalEncoder.getSignInMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = personalEncoder.getSignInMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,HeartbeatMessage):
        print ("receive heartbeatMessage" + message.imei)
        # if message.isNeedResp:
        #     reply = personalEncoder.getHeartbeatMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = personalEncoder.getHeartbeatMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,LocationInfoMessage):
        print ("receive locationInfoMessage" + message.imei)
        # if message.isNeedResp:
        #     reply = personalEncoder.getLocationMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode)
        #     socketClient.send(reply)
        reply = personalEncoder.getLocationMsgReply(message.imei,True,message.serialNo,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,LocationAlarmMessage):
        print ("receive locationAlarmMessage" + message.imei + " Alarm is : " + str(message.originalAlarmCode))
        # if message.isNeedResp:
        #     reply = personalEncoder.getLocationAlarmMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode)
        #     socketClient.send(reply)
        reply = personalEncoder.getLocationAlarmMsgReply(message.imei,True,message.serialNo,message.originalAlarmCode,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,ConfigMessage):
        print ("receive configMessage: " + message.imei + " : " + message.configContent)
    elif isinstance(message,ForwardMessage):
        print ("receive forwardMessage: " + message.imei + " : " + message.content)
    elif isinstance(message,USSDMessage):
        print ("receive forwardMessage: " + message.imei + " : " + message.content)
    elif isinstance(message,BluetoothPeripheralDataMessage):
        print ("receive blue Message: " + message.imei)
        # if message.isNeedResp:
        #     reply = personalEncoder.getBluetoothPeripheralMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = personalEncoder.getBluetoothPeripheralMsgReply(message.imei,True,message.serialNo,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,WifiMessage):
        print ("receive wifi location Message: " + message.imei)
        reply = personalEncoder.getWifiMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,InnerGeoDataMessage):
        print ("receive inner geo data Message: " + message.imei)
        reply = personalEncoder.getInnerGeoDataMsgReply(message.imei,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message, DeviceTempCollectionMessage):
        print ("receive device extra temp data Message: " + message.imei)
        reply = personalEncoder.getDeviceTempCollectionMsgReply(message.imei, message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,WifiWithDeviceInfoMessage):
        print ("receive wifi with device info Message: " + message.imei)
        reply = personalEncoder.getWifiWithDeviceInfoMsgReply(message.imei,message.serialNo,message.originalAlarmCode,message.protocolHeadType)
        socketClient.send(reply)
    elif isinstance(message,LockMessage):
        print ("receive lock Message: " + message.imei)
        reply = personalEncoder.getLockMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)
    elif isinstance(message,NetworkInfoMessage):
        print ("receive network info Message: " + message.imei)
        # if message.isNeedResp:
        #     reply = personalEncoder.getNetworkMsgReply(message.imei,True,message.serialNo)
        #     socketClient.send(reply)
        reply = personalEncoder.getNetworkMsgReply(message.imei,True,message.serialNo)
        socketClient.send(reply)

if __name__ == "__main__":
    HOST, PORT = "192.168.1.32", 1001
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
            try:
                data = c.recv(2048)
                if not data:
                    print("End of file from client. Resetting")
                    break

                messageList = decoder.decode(data)
                for message in messageList:
                    # dealNoObdDeviceMessage(message,c)
                    # dealObdDeviceMessage(message,c)
                    dealPersonalDeviceMessage(message,c)
            except Exception as e:
                print(repr(e))
                break

        c.close()





