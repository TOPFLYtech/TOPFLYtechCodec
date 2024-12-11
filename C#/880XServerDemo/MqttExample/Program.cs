
using System;
using System.Collections.Generic; 
using System.Threading.Tasks;
using TopflytechCodec;
using TopflytechCodec.Entities;
namespace MqttExample
{
    internal class Program
    {
        static TopflytechCodec.Decoder decoder = new TopflytechCodec.Decoder(MessageEncryptType.NONE, "");
        static TopflytechCodec.ObdDecoder obdDecoder = new TopflytechCodec.ObdDecoder(MessageEncryptType.NONE, "");
        static TopflytechCodec.PersonalAssetMsgDecoder personalDecoder = new TopflytechCodec.PersonalAssetMsgDecoder(MessageEncryptType.NONE, "");
          
        static TopflytechCodec.T880xPlusEncoder t880xPlusEncoder = new TopflytechCodec.T880xPlusEncoder(MessageEncryptType.NONE, "");
        static TopflytechCodec.T880xdEncoder t880xdEncoder = new TopflytechCodec.T880xdEncoder(MessageEncryptType.NONE, "");
        static TopflytechCodec.PersonalAssetMsgEncoder personalEncoder = new TopflytechCodec.PersonalAssetMsgEncoder(MessageEncryptType.NONE, "");

        static SelfMqttClient selfMqttClient;

        static string protocolType = "NoObd";

        static readonly string PROTOCOL_TYPE_OF_NO_OBD = "NoObd";
        static readonly string PROTOCOL_TYPE_OF_OBD = "Obd";
        static readonly string PROTOCOL_TYPE_OF_PERSONAL = "Personal";
        static async Task Main(string[] args)
        {
            Func<byte[], Task> customHandleMessage = CustomHandleMessage;


            string brokerUrl = "192.168.1.251";
            string imei = "518000518001517";

            selfMqttClient = new SelfMqttClient(brokerUrl, 9083, imei, customHandleMessage);
            await selfMqttClient.ConnectAsync();


            Console.ReadLine();
            await selfMqttClient.DisconnectAsync(); 
        }
        private static Task CustomHandleMessage(byte[] payload)
        {
            if (protocolType.Equals(PROTOCOL_TYPE_OF_NO_OBD))
            {
                List<TopflytechCodec.Entities.Message> messageList = decoder.decode(payload);
                foreach (TopflytechCodec.Entities.Message message in messageList)
                {
                    DealNoObdDeviceMessage(message, selfMqttClient);
                }
            }
            if (protocolType.Equals(PROTOCOL_TYPE_OF_OBD))
            {
                List<TopflytechCodec.Entities.Message> messageList = obdDecoder.decode(payload);
                foreach (TopflytechCodec.Entities.Message message in messageList)
                {
                    DealObdDeviceMessage(message, selfMqttClient);
                }
            }
            if (protocolType.Equals(PROTOCOL_TYPE_OF_PERSONAL))
            {
                List<TopflytechCodec.Entities.Message> messageList = personalDecoder.decode(payload);
                foreach (TopflytechCodec.Entities.Message message in messageList)
                {
                    DealPersonalDeviceMessage(message, selfMqttClient);
                }
            }
            return Task.CompletedTask;
        }
        private static void ShowMsg(string msg)
        {
            Console.WriteLine(msg);
        }

        public static String getGpsDriverBehaviorDescription(int behaviorType)
        {
            if (behaviorType == GpsDriverBehaviorType.HIGH_SPEED_ACCELERATE)
            {
                return "The vehicle accelerates at the high speed.";
            }
            else if (behaviorType == GpsDriverBehaviorType.HIGH_SPEED_BRAKE)
            {
                return "The vehicle brakes  at the high speed.";
            }
            else if (behaviorType == GpsDriverBehaviorType.MEDIUM_SPEED_ACCELERATE)
            {
                return "The vehicle accelerates at the high speed.";
            }
            else if (behaviorType == GpsDriverBehaviorType.MEDIUM_SPEED_ACCELERATE)
            {
                return "The vehicle brakes  at the high speed.";
            }
            else if (behaviorType == GpsDriverBehaviorType.LOW_SPEED_ACCELERATE)
            {
                return "The vehicle accelerates at the high speed.";
            }
            else if (behaviorType == GpsDriverBehaviorType.LOW_SPEED_ACCELERATE)
            {
                return "The vehicle brakes  at the high speed.";
            }
            else
            {
                return "";
            }
        }
        /**
         * Device model like :8803Pro,8806,8808A,8808B,8603. use this method. 
         */
        private static void DealNoObdDeviceMessage(TopflytechCodec.Entities.Message message, SelfMqttClient client)
        {
            if (message is TopflytechCodec.Entities.SignInMessage)
            {
                SignInMessage signInMessage = (SignInMessage)message;
                ShowMsg("receive signIn Message imei :" + signInMessage.Imei + " serial no: " + signInMessage.SerialNo + " firmware :" + signInMessage.Firmware + " software: " + signInMessage.Software);
                //8806 Plus or some new model device,need serial no,reply this message
                //if (signInMessage.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                //     client.PublishAsync(reply); 
                //}
                byte[] reply = t880xPlusEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.HeartbeatMessage)
            {
                HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
                ShowMsg("receive heartbeat Message imei :" + heartbeatMessage.Imei + " serial no: " + heartbeatMessage.SerialNo);
                //8806 Plus or some new model device,need serial no,reply this message
                //if (heartbeatMessage.IsNeedResp) {
                //    byte[] reply = t880xPlusEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xPlusEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.LocationInfoMessage)
            {
                LocationInfoMessage locationInfoMessage = (LocationInfoMessage)message;
                ShowMsg("receive location Info Message imei :" + locationInfoMessage.Imei + " serial no: " + locationInfoMessage.SerialNo);
                ShowMsg("lat:" + locationInfoMessage.Latitude + " lng:" + locationInfoMessage.Longitude);
                //8806 Plus or some new model device, these is the code of 8806 plus.
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getLocationMsgReply(locationInfoMessage.Imei, true, locationInfoMessage.SerialNo, message.ProtocolHeadType);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xPlusEncoder.getLocationMsgReply(locationInfoMessage.Imei, true, locationInfoMessage.SerialNo, message.ProtocolHeadType);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.LocationAlarmMessage)
            {
                LocationAlarmMessage locationAlarmMessage = (LocationAlarmMessage)message;
                ShowMsg("receive location alarm Message imei :" + locationAlarmMessage.Imei + " serial no: " + locationAlarmMessage.SerialNo + " Alarm is : " + locationAlarmMessage.OriginalAlarmCode);
                ShowMsg("lat:" + locationAlarmMessage.Latitude + " lng:" + locationAlarmMessage.Longitude);
                //8806 Plus or some new model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getLocationAlarmMsgReply(locationAlarmMessage.Imei, true, locationAlarmMessage.SerialNo, locationAlarmMessage.OriginalAlarmCode, message.ProtocolHeadType);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xPlusEncoder.getLocationAlarmMsgReply(locationAlarmMessage.Imei, true, locationAlarmMessage.SerialNo, locationAlarmMessage.OriginalAlarmCode, message.ProtocolHeadType);
                client.PublishAsync(reply);
            }
            else if (message is GpsDriverBehaviorMessage)
            {
                GpsDriverBehaviorMessage gpsDriverBehaviorMessage = (GpsDriverBehaviorMessage)message;
                ShowMsg("receive gps driver behavior message :" + gpsDriverBehaviorMessage.Imei);
                ShowMsg("behavior is :" + getGpsDriverBehaviorDescription(gpsDriverBehaviorMessage.BehaviorType));
                //8806 Plus or some new model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.Imei, gpsDriverBehaviorMessage.SerialNo);
                //     client.PublishAsync(reply); 
                //}
                byte[] reply = t880xPlusEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.Imei, gpsDriverBehaviorMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.ConfigMessage)
            {
                ConfigMessage configMessage = (ConfigMessage)message;
                ShowMsg("receive config Message imei :" + configMessage.Imei + " serial no: " + configMessage.SerialNo + " config Content:" + configMessage.ConfigResultContent);
            }
            else if (message is TopflytechCodec.Entities.AccelerationDriverBehaviorMessage)
            {
                AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = (AccelerationDriverBehaviorMessage)message;
                ShowMsg("receive acceleration driver behavior message :" + accelerationDriverBehaviorMessage.Imei);
                ShowMsg("behavior is :" + getGpsDriverBehaviorDescription(accelerationDriverBehaviorMessage.BehaviorType));
                //8806 Plus or some new model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.Imei, accelerationDriverBehaviorMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xPlusEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.Imei, accelerationDriverBehaviorMessage.SerialNo);
                client.PublishAsync(reply);

            }
            else if (message is TopflytechCodec.Entities.AccidentAccelerationMessage)
            {
                AccidentAccelerationMessage accidentAccelerationMessage = (AccidentAccelerationMessage)message;
                ShowMsg("receive accident acceleration message :" + accidentAccelerationMessage.Imei);
                //8806 Plus or some new model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.Imei, accidentAccelerationMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xPlusEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.Imei, accidentAccelerationMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is BluetoothPeripheralDataMessage)
            {
                BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = (BluetoothPeripheralDataMessage)message;
                ShowMsg("receive bluetooth peripheral data message:" + bluetoothPeripheralDataMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo, bluetoothPeripheralDataMessage.ProtocolHeadType);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xPlusEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo, bluetoothPeripheralDataMessage.ProtocolHeadType);
                client.PublishAsync(reply);
            }
            else if (message is NetworkInfoMessage)
            {
                NetworkInfoMessage networkInfoMessage = (NetworkInfoMessage)message;
                ShowMsg("receive network info message :" + networkInfoMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xPlusEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is WifiMessage)
            {
                WifiMessage wifiMessage = (WifiMessage)message;
                ShowMsg("receive wifi location message :" + wifiMessage.Imei);
                byte[] reply = t880xPlusEncoder.getWifiMsgReply(wifiMessage.Imei, true, wifiMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is OneWireMessage)
            {
                OneWireMessage oneWire = (OneWireMessage)message;
                ShowMsg("receive one wire message :" + oneWire.Imei);
                byte[] reply = t880xPlusEncoder.getOneWireMsgReply(oneWire.Imei, true, oneWire.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is RS485Message)
            {
                RS485Message rs485 = (RS485Message)message;
                ShowMsg("receive RS485 message :" + rs485.Imei);
                byte[] reply = t880xPlusEncoder.getRs485MsgReply(rs485.Imei, true, rs485.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is ObdMessage)
            {
                ObdMessage obdMessage = (ObdMessage)message;
                ShowMsg("receive DTC(OBD) message :" + obdMessage.Imei);
                byte[] reply = t880xPlusEncoder.getObdMsgReply(obdMessage.Imei, obdMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is ForwardMessage)
            {
                ForwardMessage forwardMessage = (ForwardMessage)message;
                ShowMsg("receive debug message :" + forwardMessage.Imei + " : " + forwardMessage.Content);
            }
            else if (message is USSDMessage)
            {
                USSDMessage ussdMessage = (USSDMessage)message;
                ShowMsg("receive ussd message :" + ussdMessage.Imei + " : " + ussdMessage.Content);
            }
            else if (message is RS232Message)
            {
                RS232Message rs232Message = (RS232Message)message;
                ShowMsg("receive RS232 message :" + rs232Message.Imei);
                //if (message.IsNeedResp)
                //{

                //    byte[] reply = t880xPlusEncoder.getRS232MsgReply(rs232Message.Imei, rs232Message.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xPlusEncoder.getRS232MsgReply(rs232Message.Imei, rs232Message.SerialNo);
                client.PublishAsync(reply);
            }
        }

        private static void DealObdDeviceMessage(TopflytechCodec.Entities.Message message, SelfMqttClient client)
        {
            if (message is TopflytechCodec.Entities.SignInMessage)
            {
                SignInMessage signInMessage = (SignInMessage)message;
                ShowMsg("receive signIn Message imei :" + signInMessage.Imei + " serial no: " + signInMessage.SerialNo + " firmware :" + signInMessage.Firmware + " software: " + signInMessage.Software + "platform:" + signInMessage.Platform);
                //some  model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xdEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.HeartbeatMessage)
            {
                HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
                ShowMsg("receive heartbeat Message imei :" + heartbeatMessage.Imei + " serial no: " + heartbeatMessage.SerialNo);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xdEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.LocationInfoMessage)
            {
                LocationInfoMessage locationInfoMessage = (LocationInfoMessage)message;
                ShowMsg("receive location Info Message imei :" + locationInfoMessage.Imei + " serial no: " + locationInfoMessage.SerialNo);
                ShowMsg("lat:" + locationInfoMessage.Latitude + " lng:" + locationInfoMessage.Longitude);
                //some model device, these is the code of 8806 plus.
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getLocationMsgReply(locationInfoMessage.Imei, true, locationInfoMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xdEncoder.getLocationMsgReply(locationInfoMessage.Imei, true, locationInfoMessage.SerialNo, locationInfoMessage.ProtocolHeadType);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.LocationAlarmMessage)
            {
                LocationAlarmMessage locationAlarmMessage = (LocationAlarmMessage)message;
                ShowMsg("receive location alarm Message imei :" + locationAlarmMessage.Imei + " serial no: " + locationAlarmMessage.SerialNo + " Alarm is : " + locationAlarmMessage.OriginalAlarmCode);
                ShowMsg("lat:" + locationAlarmMessage.Latitude + " lng:" + locationAlarmMessage.Longitude);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getLocationAlarmMsgReply(locationAlarmMessage.Imei, true, locationAlarmMessage.SerialNo, locationAlarmMessage.OriginalAlarmCode);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xdEncoder.getLocationAlarmMsgReply(locationAlarmMessage.Imei, true, locationAlarmMessage.SerialNo, locationAlarmMessage.OriginalAlarmCode, locationAlarmMessage.ProtocolHeadType);
                client.PublishAsync(reply);
            }
            else if (message is GpsDriverBehaviorMessage)
            {
                GpsDriverBehaviorMessage gpsDriverBehaviorMessage = (GpsDriverBehaviorMessage)message;
                ShowMsg("receive gps driver behavior message :" + gpsDriverBehaviorMessage.Imei);
                ShowMsg("behavior is :" + getGpsDriverBehaviorDescription(gpsDriverBehaviorMessage.BehaviorType));
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.Imei, gpsDriverBehaviorMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xdEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.Imei, gpsDriverBehaviorMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is ObdMessage)
            {
                ObdMessage obdMsg = (ObdMessage)message;
                ShowMsg("receive OBD message :" + obdMsg.Imei);
                byte[] reply = t880xdEncoder.getObdMsgReply(obdMsg.Imei, obdMsg.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.ConfigMessage)
            {
                ConfigMessage configMessage = (ConfigMessage)message;
                ShowMsg("receive config Message imei :" + configMessage.Imei + " serial no: " + configMessage.SerialNo + " config Content:" + configMessage.ConfigResultContent);
            }
            else if (message is TopflytechCodec.Entities.AccelerationDriverBehaviorMessage)
            {
                AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = (AccelerationDriverBehaviorMessage)message;
                ShowMsg("receive acceleration driver behavior message :" + accelerationDriverBehaviorMessage.Imei);
                ShowMsg("behavior is :" + getGpsDriverBehaviorDescription(accelerationDriverBehaviorMessage.BehaviorType));
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.Imei, accelerationDriverBehaviorMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xdEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.Imei, accelerationDriverBehaviorMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.AccidentAccelerationMessage)
            {
                AccidentAccelerationMessage accidentAccelerationMessage = (AccidentAccelerationMessage)message;
                ShowMsg("receive accident acceleration message :" + accidentAccelerationMessage.Imei);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.Imei, accidentAccelerationMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xdEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.Imei, accidentAccelerationMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is BluetoothPeripheralDataMessage)
            {
                BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = (BluetoothPeripheralDataMessage)message;
                ShowMsg("receive bluetooth peripheral data message:" + bluetoothPeripheralDataMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xdEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo, bluetoothPeripheralDataMessage.ProtocolHeadType);
                client.PublishAsync(reply);
            }
            else if (message is NetworkInfoMessage)
            {
                NetworkInfoMessage networkInfoMessage = (NetworkInfoMessage)message;
                ShowMsg("receive network info message :" + networkInfoMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = t880xdEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is WifiMessage)
            {
                WifiMessage wifiMessage = (WifiMessage)message;
                ShowMsg("receive wifi location message :" + wifiMessage.Imei);
                byte[] reply = t880xdEncoder.getWifiMsgReply(wifiMessage.Imei, true, wifiMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is ForwardMessage)
            {
                ForwardMessage forwardMessage = (ForwardMessage)message;
                ShowMsg("receive debug message :" + forwardMessage.Imei + " : " + forwardMessage.Content);
            }
            else if (message is USSDMessage)
            {
                USSDMessage ussdMessage = (USSDMessage)message;
                ShowMsg("receive ussd message :" + ussdMessage.Imei + " : " + ussdMessage.Content);
            }

        }


        private static void DealPersonalDeviceMessage(TopflytechCodec.Entities.Message message, SelfMqttClient client)
        {
            if (message is TopflytechCodec.Entities.SignInMessage)
            {
                SignInMessage signInMessage = (SignInMessage)message;
                ShowMsg("receive signIn Message imei :" + signInMessage.Imei + " serial no: " + signInMessage.SerialNo + " firmware :" + signInMessage.Firmware + " software: " + signInMessage.Software + "platform:" + signInMessage.Platform);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = personalEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = personalEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.HeartbeatMessage)
            {
                HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
                ShowMsg("receive heartbeat Message imei :" + heartbeatMessage.Imei + " serial no: " + heartbeatMessage.SerialNo);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = personalEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                //     client.PublishAsync(reply);

                //}
                byte[] reply = personalEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.LocationInfoMessage)
            {
                LocationInfoMessage locationInfoMessage = (LocationInfoMessage)message;
                ShowMsg("receive location Info Message imei :" + locationInfoMessage.Imei + " serial no: " + locationInfoMessage.SerialNo);
                ShowMsg("lat:" + locationInfoMessage.Latitude + " lng:" + locationInfoMessage.Longitude);
                //some model device, these is the code of 8806 plus.
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = personalEncoder.getLocationMsgReply(locationInfoMessage.Imei, true, locationInfoMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = personalEncoder.getLocationMsgReply(locationInfoMessage.Imei, true, locationInfoMessage.SerialNo, locationInfoMessage.ProtocolHeadType);
                client.PublishAsync(reply);
            }
            else if (message is TopflytechCodec.Entities.LocationAlarmMessage)
            {
                LocationAlarmMessage locationAlarmMessage = (LocationAlarmMessage)message;
                ShowMsg("receive location alarm Message imei :" + locationAlarmMessage.Imei + " serial no: " + locationAlarmMessage.SerialNo + " Alarm is : " + locationAlarmMessage.OriginalAlarmCode);
                ShowMsg("lat:" + locationAlarmMessage.Latitude + " lng:" + locationAlarmMessage.Longitude);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = personalEncoder.getLocationAlarmMsgReply(locationAlarmMessage.Imei, true, locationAlarmMessage.SerialNo, locationAlarmMessage.OriginalAlarmCode);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = personalEncoder.getLocationAlarmMsgReply(locationAlarmMessage.Imei, true, locationAlarmMessage.SerialNo, locationAlarmMessage.OriginalAlarmCode, locationAlarmMessage.ProtocolHeadType);
                client.PublishAsync(reply);
            }
            else if (message is NetworkInfoMessage)
            {
                NetworkInfoMessage networkInfoMessage = (NetworkInfoMessage)message;
                ShowMsg("receive network info message :" + networkInfoMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = personalEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = personalEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is WifiMessage)
            {
                WifiMessage wifiMessage = (WifiMessage)message;
                ShowMsg("receive wifi location message :" + wifiMessage.Imei);
                byte[] reply = personalEncoder.getWifiMsgReply(wifiMessage.Imei, wifiMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is LockMessage)
            {
                LockMessage lockMessage = (LockMessage)message;
                ShowMsg("receive lock message :" + lockMessage.Imei + " id:" + lockMessage.LockId);
                byte[] reply = personalEncoder.getLockMsgReply(lockMessage.Imei, lockMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is InnerGeoDataMessage)
            {
                InnerGeoDataMessage innerGeoDataMessage = (InnerGeoDataMessage)message;
                ShowMsg("receive inner geo message :" + innerGeoDataMessage.Imei);
                byte[] reply = personalEncoder.getInnerGeoDataMsgReply(innerGeoDataMessage.Imei, true, innerGeoDataMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is DeviceTempCollectionMessage)
            {
                DeviceTempCollectionMessage deviceTempCollectionMessage = (DeviceTempCollectionMessage)message;
                ShowMsg("receive external device data message :" + deviceTempCollectionMessage.Imei);
                byte[] reply = personalEncoder.getDeviceTempCollectionMsgReply(deviceTempCollectionMessage.Imei, deviceTempCollectionMessage.SerialNo);
                client.PublishAsync(reply);
            }
            else if (message is ForwardMessage)
            {
                ForwardMessage forwardMessage = (ForwardMessage)message;
                ShowMsg("receive debug message :" + forwardMessage.Imei + " : " + forwardMessage.Content);
            }
            else if (message is USSDMessage)
            {
                USSDMessage ussdMessage = (USSDMessage)message;
                ShowMsg("receive ussd message :" + ussdMessage.Imei + " : " + ussdMessage.Content);
            }
            else if (message is BluetoothPeripheralDataMessage)
            {
                BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = (BluetoothPeripheralDataMessage)message;
                ShowMsg("receive bluetooth peripheral data message:" + bluetoothPeripheralDataMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = personalEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo);
                //     client.PublishAsync(reply);
                //}
                byte[] reply = personalEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo, bluetoothPeripheralDataMessage.ProtocolHeadType);
                client.PublishAsync(reply);
            }
            else if (message is WifiWithDeviceInfoMessage)
            {
                WifiWithDeviceInfoMessage wifiWithDeviceInfoMessage = (WifiWithDeviceInfoMessage)message;
                ShowMsg("receive wifi with device infomation message:" + wifiWithDeviceInfoMessage.Imei);
                byte[] reply = personalEncoder.getWifiWithDeviceInfoReply(wifiWithDeviceInfoMessage.Imei,
                    wifiWithDeviceInfoMessage.SerialNo, wifiWithDeviceInfoMessage.OriginalAlarmCode, wifiWithDeviceInfoMessage.ProtocolHeadType);
                client.PublishAsync(reply);
            }
        }
    }
}
