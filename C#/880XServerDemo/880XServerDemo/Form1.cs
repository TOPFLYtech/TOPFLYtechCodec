using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net.Sockets;
using System.Threading;
using System.Net;
using System.IO;
using TopflytechCodec;
using TopflytechCodec.Entities;
namespace _880XServerDemo
{
    public partial class topflytech880xServer : Form
    {
        public topflytech880xServer()
        {
            InitializeComponent();
           
            TextBox.CheckForIllegalCrossThreadCalls = false;
        }

      

        Thread threadWatch = null; 
        Socket socketWatch = null; 
         
        Dictionary<string, Socket> dict = new Dictionary<string, Socket>();

      
        Dictionary<string, Thread> dictThread = new Dictionary<string, Thread>();
        Dictionary<string, Double> lastValidLat = new Dictionary<string, Double>();
        Dictionary<string, Double> lastValidLng = new Dictionary<string, Double>();
        TopflytechCodec.Decoder decoder = new TopflytechCodec.Decoder(MessageEncryptType.NONE, "");
        TopflytechCodec.ObdDecoder obdDecoder = new TopflytechCodec.ObdDecoder(MessageEncryptType.NONE, "");
        TopflytechCodec.PersonalAssetMsgDecoder personalDecoder = new TopflytechCodec.PersonalAssetMsgDecoder(MessageEncryptType.NONE, "");

        TopflytechCodec.T880xPlusEncoder t880xPlusEncoder = new TopflytechCodec.T880xPlusEncoder(MessageEncryptType.NONE, "");
        TopflytechCodec.T880xdEncoder t880xdEncoder = new TopflytechCodec.T880xdEncoder(MessageEncryptType.NONE, "");
        TopflytechCodec.PersonalAssetMsgEncoder personalEncoder = new TopflytechCodec.PersonalAssetMsgEncoder(MessageEncryptType.NONE, "");
        bool isRunning = false;
        private void btnBeginListen_Click(object sender, EventArgs e)
        {
            if (isRunning)
            {
                return;
            }
            isRunning = true;
            socketWatch = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            IPAddress address = IPAddress.Parse(txtIP.Text.Trim());
            IPEndPoint endpoint = new IPEndPoint(address, int.Parse(txtPort.Text.Trim()));
            try
            {
                socketWatch.Bind(endpoint);
            }
            catch (SocketException ex)
            {
                ShowMsg("Bind IP Exception：" + ex.Message);
                return;
            }
            catch (Exception ex)
            {
                ShowMsg("Bind IP Exception：" + ex.Message);
                return;
            }
            socketWatch.Listen(10);
            threadWatch = new Thread(WatchConnection);
            threadWatch.IsBackground = true; 
            threadWatch.Start(); 
            ShowMsg("Server start success!~");

        }

        private void btnStopServer_Click(object sender, EventArgs e)
        {
            if (!isRunning)
            {
                return;
            }
            isRunning = false;
            Thread.Sleep(300);
            foreach (Socket socket in dict.Values)
            {
                socket.Close();
            }
            socketWatch.Close();
            ShowMsg("Server stop success!~");
             
        }

 
        void WatchConnection()
        {
            while (isRunning)
            {
                Socket socketConnection = null;
                try
                {
                    //if (socketWatch. <= 0)
                    //{
                    //    continue;
                    //}
                    socketConnection = socketWatch.Accept();
                }
                catch (SocketException ex)
                {
                    ShowMsg("Server Connect Exception：" + ex.Message);
                    break;
                }
                catch (Exception ex)
                {
                    ShowMsg("Server Connect Exception：" + ex.Message);
                    break;
                }
                lbOnline.Items.Add(socketConnection.RemoteEndPoint.ToString());
                dict.Add(socketConnection.RemoteEndPoint.ToString(), socketConnection);
                Thread threadCommunicate = new Thread(ReceiveMsg);
                threadCommunicate.IsBackground = true;
                threadCommunicate.Start(socketConnection); 

                dictThread.Add(socketConnection.RemoteEndPoint.ToString(), threadCommunicate);

                ShowMsg(string.Format("{0} upline. ", socketConnection.RemoteEndPoint.ToString()));
            }
        }

     
        void ReceiveMsg(object socketClientPara)
        {
            Socket socketClient = socketClientPara as Socket;
            while (isRunning)
            {
                byte[] arrMsgRevBuf = new byte[1024 * 1024 * 2];
                int length = -1;
                if (socketClient.Available <= 0)
                {
                    continue;
                }
                try
                {
                    length = socketClient.Receive(arrMsgRevBuf);
                }
                catch (SocketException ex)
                {
                    ShowMsg("Exception：" + ex.Message + ", RemoteEndPoint: " + socketClient.RemoteEndPoint.ToString());
                    dict.Remove(socketClient.RemoteEndPoint.ToString());
                    dictThread.Remove(socketClient.RemoteEndPoint.ToString());
                    lbOnline.Items.Remove(socketClient.RemoteEndPoint.ToString());
                    break;
                }
                catch (Exception ex)
                {
                    ShowMsg("Exception：" + ex.Message);
                    break;
                }
                byte[] arrMsgRev = new byte[length];
                Array.Copy(arrMsgRevBuf, arrMsgRev, length);
                if (rbNoObd.Checked)
                {
                    List<TopflytechCodec.Entities.Message> messageList = decoder.decode(arrMsgRev);
                    foreach (TopflytechCodec.Entities.Message message in messageList)
                    {
                        DealNoObdDeviceMessage(message, socketClient);
                    }
                }
                else if(rbObd.Checked)
                {
                    List<TopflytechCodec.Entities.Message> messageList = obdDecoder.decode(arrMsgRev);
                    foreach (TopflytechCodec.Entities.Message message in messageList)
                    {
                        DealObdDeviceMessage(message, socketClient);
                    }
                }
                else if (rbPersonal.Checked)
                {
                    List<TopflytechCodec.Entities.Message> messageList = personalDecoder.decode(arrMsgRev);
                    foreach (TopflytechCodec.Entities.Message message in messageList)
                    {
                        DealPersonalDeviceMessage(message, socketClient);
                    }
                }
                
            }
        }
         


        /**
         * Device model like :8803Pro,8806,8808A,8808B,8603. use this method. 
         */
        private void DealNoObdDeviceMessage(TopflytechCodec.Entities.Message message, Socket socket)
        {
            string strClientKey = socket.RemoteEndPoint.ToString();
            if (message is TopflytechCodec.Entities.SignInMessage)
            {
                SignInMessage signInMessage = (SignInMessage)message;
                ShowMsg("receive signIn Message imei :" + signInMessage.Imei + " serial no: " + signInMessage.SerialNo + " firmware :" + signInMessage.Firmware + " software: " + signInMessage.Software);
                //8806 Plus or some new model device,need serial no,reply this message
                //if (signInMessage.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                //    dict[strClientKey].Send(reply); 
                //}
                byte[] reply = t880xPlusEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                dict[strClientKey].Send(reply); 
            }
            else if (message is TopflytechCodec.Entities.HeartbeatMessage)
            {
                HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
                ShowMsg("receive heartbeat Message imei :" + heartbeatMessage.Imei + " serial no: " + heartbeatMessage.SerialNo);
                //8806 Plus or some new model device,need serial no,reply this message
                //if (heartbeatMessage.IsNeedResp) {
                //    byte[] reply = t880xPlusEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xPlusEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xPlusEncoder.getLocationMsgReply(locationInfoMessage.Imei, true, locationInfoMessage.SerialNo, message.ProtocolHeadType);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xPlusEncoder.getLocationAlarmMsgReply(locationAlarmMessage.Imei, true, locationAlarmMessage.SerialNo, locationAlarmMessage.OriginalAlarmCode, message.ProtocolHeadType);
                dict[strClientKey].Send(reply);
            }
            else if(message is  GpsDriverBehaviorMessage){
                GpsDriverBehaviorMessage gpsDriverBehaviorMessage = (GpsDriverBehaviorMessage)message;
                ShowMsg("receive gps driver behavior message :" + gpsDriverBehaviorMessage.Imei);
                ShowMsg("behavior is :" + getGpsDriverBehaviorDescription(gpsDriverBehaviorMessage.BehaviorType));
                //8806 Plus or some new model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.Imei, gpsDriverBehaviorMessage.SerialNo);
                //    dict[strClientKey].Send(reply); 
                //}
                byte[] reply = t880xPlusEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.Imei, gpsDriverBehaviorMessage.SerialNo);
                dict[strClientKey].Send(reply); 
            }
            else if (message is TopflytechCodec.Entities.ConfigMessage)
            {
                ConfigMessage configMessage = (ConfigMessage)message;
                ShowMsg("receive config Message imei :" + configMessage.Imei + " serial no: " + configMessage.SerialNo + " config Content:" + configMessage.ConfigResultContent); 
            }else if (message is TopflytechCodec.Entities.AccelerationDriverBehaviorMessage){
                AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = (AccelerationDriverBehaviorMessage)message;
                ShowMsg("receive acceleration driver behavior message :" + accelerationDriverBehaviorMessage.Imei);
                ShowMsg("behavior is :" + getGpsDriverBehaviorDescription(accelerationDriverBehaviorMessage.BehaviorType));
                //8806 Plus or some new model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.Imei, accelerationDriverBehaviorMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xPlusEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.Imei, accelerationDriverBehaviorMessage.SerialNo);
                dict[strClientKey].Send(reply);
                
            }else if (message is TopflytechCodec.Entities.AccidentAccelerationMessage){
                AccidentAccelerationMessage accidentAccelerationMessage = (AccidentAccelerationMessage)message;
                ShowMsg("receive accident acceleration message :" + accidentAccelerationMessage.Imei);
                //8806 Plus or some new model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.Imei, accidentAccelerationMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xPlusEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.Imei, accidentAccelerationMessage.SerialNo);
                dict[strClientKey].Send(reply);
            }
            else if (message is BluetoothPeripheralDataMessage)
            {
                BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = (BluetoothPeripheralDataMessage)message;
                ShowMsg("receive bluetooth peripheral data message:" + bluetoothPeripheralDataMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo, bluetoothPeripheralDataMessage.ProtocolHeadType);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xPlusEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo, bluetoothPeripheralDataMessage.ProtocolHeadType);
                dict[strClientKey].Send(reply);
            }
            else if (message is NetworkInfoMessage)
            {
                NetworkInfoMessage networkInfoMessage = (NetworkInfoMessage)message;
                ShowMsg("receive network info message :" + networkInfoMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xPlusEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xPlusEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xPlusEncoder.getRS232MsgReply(rs232Message.Imei, rs232Message.SerialNo);
                dict[strClientKey].Send(reply);
            }
        }

        private void DealObdDeviceMessage(TopflytechCodec.Entities.Message message, Socket socket)
        {
            string strClientKey = socket.RemoteEndPoint.ToString();
            if (message is TopflytechCodec.Entities.SignInMessage)
            {
                SignInMessage signInMessage = (SignInMessage)message;
                ShowMsg("receive signIn Message imei :" + signInMessage.Imei + " serial no: " + signInMessage.SerialNo + " firmware :" + signInMessage.Firmware + " software: " + signInMessage.Software + "platform:" + signInMessage.Platform);
                //some  model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xdEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                dict[strClientKey].Send(reply);
            }
            else if (message is TopflytechCodec.Entities.HeartbeatMessage)
            {
                HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
                ShowMsg("receive heartbeat Message imei :" + heartbeatMessage.Imei + " serial no: " + heartbeatMessage.SerialNo);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xdEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xdEncoder.getLocationMsgReply(locationInfoMessage.Imei, true, locationInfoMessage.SerialNo,locationInfoMessage.ProtocolHeadType);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xdEncoder.getLocationAlarmMsgReply(locationAlarmMessage.Imei, true, locationAlarmMessage.SerialNo, locationAlarmMessage.OriginalAlarmCode, locationAlarmMessage.ProtocolHeadType);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xdEncoder.getGpsDriverBehaviorMsgReply(gpsDriverBehaviorMessage.Imei, gpsDriverBehaviorMessage.SerialNo);
                dict[strClientKey].Send(reply);
            }
            else if (message is ObdMessage)
            {
                ObdMessage obdMsg = (ObdMessage)message;
                ShowMsg("receive OBD message :" + obdMsg.Imei);
                byte[] reply = t880xdEncoder.getObdMsgReply(obdMsg.Imei, obdMsg.SerialNo);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xdEncoder.getAccelerationDriverBehaviorMsgReply(accelerationDriverBehaviorMessage.Imei, accelerationDriverBehaviorMessage.SerialNo);
                dict[strClientKey].Send(reply);
            }
            else if (message is TopflytechCodec.Entities.AccidentAccelerationMessage)
            {
                AccidentAccelerationMessage accidentAccelerationMessage = (AccidentAccelerationMessage)message;
                ShowMsg("receive accident acceleration message :" + accidentAccelerationMessage.Imei);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.Imei, accidentAccelerationMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xdEncoder.getAccelerationAlarmMsgReply(accidentAccelerationMessage.Imei, accidentAccelerationMessage.SerialNo);
                dict[strClientKey].Send(reply);
            }
            else if (message is BluetoothPeripheralDataMessage)
            {
                BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = (BluetoothPeripheralDataMessage)message;
                ShowMsg("receive bluetooth peripheral data message:" + bluetoothPeripheralDataMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xdEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo);
                dict[strClientKey].Send(reply);
            }
            else if (message is NetworkInfoMessage)
            {
                NetworkInfoMessage networkInfoMessage = (NetworkInfoMessage)message;
                ShowMsg("receive network info message :" + networkInfoMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = t880xdEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = t880xdEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                dict[strClientKey].Send(reply);
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


        private void DealPersonalDeviceMessage(TopflytechCodec.Entities.Message message, Socket socket)
        {
            string strClientKey = socket.RemoteEndPoint.ToString();
            if (message is TopflytechCodec.Entities.SignInMessage)
            {
                SignInMessage signInMessage = (SignInMessage)message;
                ShowMsg("receive signIn Message imei :" + signInMessage.Imei + " serial no: " + signInMessage.SerialNo + " firmware :" + signInMessage.Firmware + " software: " + signInMessage.Software + "platform:" + signInMessage.Platform);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = personalEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = personalEncoder.getSignInMsgReply(signInMessage.Imei, true, signInMessage.SerialNo);
                dict[strClientKey].Send(reply);
            }
            else if (message is TopflytechCodec.Entities.HeartbeatMessage)
            {
                HeartbeatMessage heartbeatMessage = (HeartbeatMessage)message;
                ShowMsg("receive heartbeat Message imei :" + heartbeatMessage.Imei + " serial no: " + heartbeatMessage.SerialNo);
                //some model device,need serial no,reply this message
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = personalEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                //    dict[strClientKey].Send(reply);

                //}
                byte[] reply = personalEncoder.getHeartbeatMsgReply(heartbeatMessage.Imei, true, heartbeatMessage.SerialNo);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = personalEncoder.getLocationMsgReply(locationInfoMessage.Imei, true, locationInfoMessage.SerialNo);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = personalEncoder.getLocationAlarmMsgReply(locationAlarmMessage.Imei, true, locationAlarmMessage.SerialNo, locationAlarmMessage.OriginalAlarmCode);
                dict[strClientKey].Send(reply);
            }
            else if (message is NetworkInfoMessage)
            {
                NetworkInfoMessage networkInfoMessage = (NetworkInfoMessage)message;
                ShowMsg("receive network info message :" + networkInfoMessage.Imei);
                //if (message.IsNeedResp)
                //{
                //    byte[] reply = personalEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = personalEncoder.getNetworkMsgReply(networkInfoMessage.Imei, networkInfoMessage.SerialNo);
                dict[strClientKey].Send(reply);
            }
            else if (message is WifiMessage)
            {
                WifiMessage wifiMessage = (WifiMessage)message;
                ShowMsg("receive wifi location message :" + wifiMessage.Imei);
                byte[] reply = personalEncoder.getWifiMsgReply(wifiMessage.Imei, wifiMessage.SerialNo);
                dict[strClientKey].Send(reply);
            }
            else if (message is LockMessage)
            {
                LockMessage lockMessage = (LockMessage)message;
                ShowMsg("receive lock message :" + lockMessage.Imei + " id:" + lockMessage.LockId);
                byte[] reply = personalEncoder.getLockMsgReply(lockMessage.Imei, lockMessage.SerialNo);
                dict[strClientKey].Send(reply);
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
                //    dict[strClientKey].Send(reply);
                //}
                byte[] reply = personalEncoder.getBluetoothPeripheralMsgReply(bluetoothPeripheralDataMessage.Imei, bluetoothPeripheralDataMessage.SerialNo);
                dict[strClientKey].Send(reply);
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
         

        private void ShowMsg(string msg)
        {
            txtMsg.AppendText(msg + "\r\n");
        }
        

        private string ByteToString(byte[] InBytes)
        {
            string StringOut = "";
            foreach (byte InByte in InBytes)
            {
                StringOut = StringOut + String.Format("{0:X2} ", InByte);
            }
            return StringOut;
        }

        private string byteToLog(byte[] command)
        { 
            return "[" + ByteToString(command) +"]"; 
        }

      

       

         
        
         
    }
}
