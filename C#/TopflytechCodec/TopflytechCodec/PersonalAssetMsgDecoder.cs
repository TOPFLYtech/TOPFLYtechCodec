using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using TopflytechCodec.Entities;

namespace TopflytechCodec
{
    public class PersonalAssetMsgDecoder
    {
        private static int HEADER_LENGTH = 3;

        private static byte[] SIGNUP = { 0x27, 0x27, 0x01 };

        private static byte[] DATA = { 0x27, 0x27, 0x02 };

        private static byte[] HEARTBEAT = { 0x27, 0x27, 0x03 };

        private static byte[] ALARM = { 0x27, 0x27, 0x04 };
        private static byte[] CONFIG = { 0x27, 0x27, (byte)0x81 };

        private static byte[] NETWORK_INFO_DATA = { 0x27, 0x27, 0x05 };

         private static byte[] BLUETOOTH_DATA =   {0x27, 0x27, (byte)0x10};

        private int encryptType = 0;
        private String aesKey;
        public PersonalAssetMsgDecoder(int messageEncryptType, String aesKey)
        {
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
        }

        private static bool match(byte[] bytes)
        {
            return Utils.ArrayEquals(SIGNUP, bytes)
                    || Utils.ArrayEquals(HEARTBEAT, bytes)
                    || Utils.ArrayEquals(DATA, bytes)
                    || Utils.ArrayEquals(ALARM, bytes)
                    || Utils.ArrayEquals(CONFIG , bytes)
                    || Utils.ArrayEquals(NETWORK_INFO_DATA, bytes)
                    || Utils.ArrayEquals(BLUETOOTH_DATA, bytes);
        }
        private TopflytechByteBuf decoderBuf = new TopflytechByteBuf();


        public List<Message> decode(byte[] buf)
        {
            decoderBuf.PutBuf(buf);
            List<Message> messages = new List<Message>();
            if (decoderBuf.GetReadableBytes() < (HEADER_LENGTH + 2))
            {
                return messages;
            }
            byte[] bytes = new byte[3];
            while (decoderBuf.GetReadableBytes() > 5)
            {
                decoderBuf.MarkReaderIndex();
                bytes[0] = decoderBuf.GetByte(0);
                bytes[1] = decoderBuf.GetByte(1);
                bytes[2] = decoderBuf.GetByte(2);
                if (match(bytes))
                {
                    decoderBuf.SkipBytes(HEADER_LENGTH);
                    byte[] lengthBytes = decoderBuf.ReadBytes(2);
                    int packageLength = BytesUtils.Bytes2Short(lengthBytes, 0);
                    if (encryptType == MessageEncryptType.MD5)
                    {
                        packageLength = packageLength + 8;
                    }
                    else if (encryptType == MessageEncryptType.AES)
                    {
                        packageLength = Crypto.GetAesLength(packageLength);
                    }
                    decoderBuf.ResetReaderIndex();
                    if (packageLength > decoderBuf.GetReadableBytes())
                    {
                        break;
                    }
                    byte[] data = decoderBuf.ReadBytes(packageLength);
                    data = Crypto.DecryptData(data, encryptType, aesKey);
                    Message message = build(data);
                    if (message != null)
                    {
                        messages.Add(message);
                    }

                }
                else
                {
                    decoderBuf.SkipBytes(1);
                }
            }
            return messages;
        }

        public Message build(byte[] bytes){
            if (bytes != null && bytes.Length > HEADER_LENGTH
                    && (bytes[0] == 0x27 && bytes[1] == 0x27)) {
                switch (bytes[2]) {
                    case 0x01:
                        SignInMessage signInMessage = parseLoginMessage(bytes);
                        return signInMessage;
                    case 0x03:
                        HeartbeatMessage heartbeatMessage = parseHeartbeat(bytes);
                        return heartbeatMessage;
                    case 0x02:
                    case 0x04:
                        LocationMessage locationMessage = parseDataMessage(bytes);
                        return locationMessage;
                    case 0x05:
                        NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                        return networkInfoMessage;
                    case 0x10:
                        BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = parseBluetoothDataMessage(bytes);
                        return bluetoothPeripheralDataMessage;
                    case (byte)0x81:
                        Message message =  parseInteractMessage(bytes);
                        return message;
                    default:
                        return null;
                }
            }
            return null;
        }
        private BluetoothPeripheralDataMessage parseBluetoothDataMessage(byte[] bytes)
        {
            BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = new BluetoothPeripheralDataMessage();
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            if ((bytes[21] & 0x01) == 0x01)
            {
                bluetoothPeripheralDataMessage.IsIgnition = true;
            }
            else
            {
                bluetoothPeripheralDataMessage.IsIgnition = false;
            }
            bluetoothPeripheralDataMessage.Date = Utils.getGTM0Date(bytes, 15);
            bluetoothPeripheralDataMessage.OrignBytes = bytes;
            bluetoothPeripheralDataMessage.IsHistoryData = (bytes[15] & 0x80) != 0x00;
            bluetoothPeripheralDataMessage.SerialNo = serialNo;
            //bluetoothPeripheralDataMessage.IsNeedResp = isNeedResp;
            bluetoothPeripheralDataMessage.Imei = imei;
            byte[] bleData = new byte[bytes.Length - 22];
            Array.Copy(bytes, 22, bleData, 0, bleData.Length);
            List<BleData> bleDataList = new List<BleData>();
            if (bleData[0] == 0x00 && bleData[1] == 0x01)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE;
                for (int i = 2; i < bleData.Length; i += 10)
                {
                    BleTireData bleTireData = new BleTireData();
                    byte[] macArray = new byte[6];
                    Array.Copy(bleData, i, macArray, 0, 6);
                    String mac = BytesUtils.Bytes2HexString(macArray, 0);
                    int voltageTmp = (int)bleData[i + 6] < 0 ? (int)bleData[i + 6] + 256 : (int)bleData[i + 6];
                    double voltage;
                    if (voltageTmp == 255)
                    {
                        voltage = -999;
                    }
                    else
                    {
                        voltage = 1.22 + 0.01 * voltageTmp;
                    }
                    int airPressureTmp = (int)bleData[i + 7] < 0 ? (int)bleData[i + 7] + 256 : (int)bleData[i + 7];
                    double airPressure;
                    if (airPressureTmp == 255)
                    {
                        airPressure = -999;
                    }
                    else
                    {
                        airPressure = 1.572 * 2 * airPressureTmp;
                    }
                    int airTempTmp = (int)bleData[i + 8] < 0 ? (int)bleData[i + 8] + 256 : (int)bleData[i + 8];
                    int airTemp;
                    if (airTempTmp == 255)
                    {
                        airTemp = -999;
                    }
                    else
                    {
                        airTemp = airTempTmp - 55;
                    }
                    //            boolean isTireLeaks = (bleData[i+5] == 0x01);
                    bleTireData.Mac = mac;
                    bleTireData.Voltage = voltage;
                    bleTireData.AirPressure = airPressure;
                    bleTireData.AirTemp = airTemp;
                    //            bleTireData.setIsTireLeaks(isTireLeaks);
                    int alarm = (int)bleData[i + 9];
                    if (alarm == -1)
                    {
                        alarm = 0;
                    }
                    bleTireData.Status = alarm;
                    bleDataList.Add(bleTireData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x02)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS;
                BleAlertData bleAlertData = new BleAlertData();
                byte[] macArray = new byte[6];
                Array.Copy(bleData, 2, macArray, 0, 6);
                String mac = BytesUtils.Bytes2HexString(macArray, 0);
                String voltageStr = BytesUtils.Bytes2HexString(bleData, 8).Substring(0, 2);
                float voltage = 0;
                try
                {
                    voltage = (float)Convert.ToDouble(voltageStr) / 10;
                }
                catch (Exception e)
                {

                }
                byte alertByte = bleData[9];
                int alert = alertByte == 0x01 ? BleAlertData.ALERT_TYPE_LOW_BATTERY : BleAlertData.ALERT_TYPE_SOS;
                bool isHistoryData = (bleData[10] & 0x80) != 0x00;
                bool latlngValid = (bleData[10] & 0x40) != 0x00;
                int satelliteNumber = bleData[10] & 0x1F;
                double altitude = latlngValid ? BytesUtils.Bytes2Float(bleData, 11) : 0.0;
                double longitude = latlngValid ? BytesUtils.Bytes2Float(bleData, 15) : 0.0;
                double latitude = latlngValid ? BytesUtils.Bytes2Float(bleData, 19) : 0.0;
                int azimuth = latlngValid ? BytesUtils.Bytes2Short(bleData, 25) : 0;
                float speedf = 0.0f;
                try
                {
                    byte[] bytesSpeed = new byte[2];
                    Array.Copy(bleData, 23, bytesSpeed, 0, 2);
                    String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
                    if (strSp.Contains("f"))
                    {
                        speedf = -1f;
                    }
                    else
                    {
                        speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));
                    }
                }
                catch (Exception e)
                {

                }
                Boolean is_4g_lbs = false;
                Int32 mcc_4g = -1;
                Int32 mnc_4g = -1;
                Int64 ci_4g = -1;
                Int32 earfcn_4g_1 = -1;
                Int32 pcid_4g_1 = -1;
                Int32 earfcn_4g_2 = -1;
                Int32 pcid_4g_2 = -1;
                Boolean is_2g_lbs = false;
                Int32 mcc_2g = -1;
                Int32 mnc_2g = -1;
                Int32 lac_2g_1 = -1;
                Int32 ci_2g_1 = -1;
                Int32 lac_2g_2 = -1;
                Int32 ci_2g_2 = -1;
                Int32 lac_2g_3 = -1;
                Int32 ci_2g_3 = -1;
                if (!latlngValid)
                {
                    byte lbsByte = bleData[11];
                    if ((lbsByte & 0x8) == 0x8)
                    {
                        is_2g_lbs = true;
                    }
                    else
                    {
                        is_4g_lbs = true;
                    }
                }
                if (is_2g_lbs)
                {
                    mcc_2g = BytesUtils.Bytes2Short(bleData, 11);
                    mnc_2g = BytesUtils.Bytes2Short(bleData, 13);
                    lac_2g_1 = BytesUtils.Bytes2Short(bleData, 15);
                    ci_2g_1 = BytesUtils.Bytes2Short(bleData, 17);
                    lac_2g_2 = BytesUtils.Bytes2Short(bleData, 19);
                    ci_2g_2 = BytesUtils.Bytes2Short(bleData, 21);
                    lac_2g_3 = BytesUtils.Bytes2Short(bleData, 23);
                    ci_2g_3 = BytesUtils.Bytes2Short(bleData, 25);
                }
                if (is_4g_lbs)
                {
                    mcc_4g = BytesUtils.Bytes2Short(bleData, 11);
                    mnc_4g = BytesUtils.Bytes2Short(bleData, 13);
                    ci_4g = BytesUtils.Byte2Int(bleData, 15);
                    earfcn_4g_1 = BytesUtils.Bytes2Short(bleData, 19);
                    pcid_4g_1 = BytesUtils.Bytes2Short(bleData, 21);
                    earfcn_4g_2 = BytesUtils.Bytes2Short(bleData, 23);
                    pcid_4g_2 = BytesUtils.Bytes2Short(bleData, 25);
                }
                bleAlertData.AlertType = alert;
                bleAlertData.Altitude = altitude;
                bleAlertData.Azimuth = azimuth;
                bleAlertData.InnerVoltage = voltage;
                bleAlertData.IsHistoryData = isHistoryData;
                bleAlertData.Latitude = latitude;
                bleAlertData.LatlngValid = latlngValid;
                bleAlertData.SatelliteCount = satelliteNumber;
                bleAlertData.Longitude = longitude;
                bleAlertData.Mac = mac;
                bleAlertData.Speed = speedf;
                bleAlertData.Is_2g_lbs = is_2g_lbs;
                bleAlertData.Is_4g_lbs = is_4g_lbs;
                bleAlertData.Mcc_4g = mcc_4g;
                bleAlertData.Mnc_4g = mnc_4g;
                bleAlertData.Ci_4g = ci_4g;
                bleAlertData.Earfcn_4g_1 = earfcn_4g_1;
                bleAlertData.Pcid_4g_1 = pcid_4g_1;
                bleAlertData.Earfcn_4g_2 = earfcn_4g_2;
                bleAlertData.Pcid_4g_2 = pcid_4g_2;
                bleAlertData.Mcc_2g = mcc_2g;
                bleAlertData.Mnc_2g = mnc_2g;
                bleAlertData.Lac_2g_1 = lac_2g_1;
                bleAlertData.Ci_2g_1 = ci_2g_1;
                bleAlertData.Lac_2g_2 = lac_2g_2;
                bleAlertData.Ci_2g_2 = ci_2g_2;
                bleAlertData.Lac_2g_3 = lac_2g_3;
                bleAlertData.Ci_2g_3 = ci_2g_3;
                bleDataList.Add(bleAlertData);
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x03)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER;
                BleDriverSignInData bleDriverSignInData = new BleDriverSignInData();
                byte[] macArray = new byte[6];
                Array.Copy(bleData, 2, macArray, 0, 6);
                String mac = BytesUtils.Bytes2HexString(macArray, 0);
                String voltageStr = BytesUtils.Bytes2HexString(bleData, 8).Substring(0, 2);
                float voltage = 0;
                try
                {
                    voltage = (float)Convert.ToDouble(voltageStr) / 10;
                }
                catch (Exception e)
                {
                }
                byte alertByte = bleData[9];
                int alert = alertByte == 0x01 ? BleDriverSignInData.ALERT_TYPE_LOW_BATTERY : BleDriverSignInData.ALERT_TYPE_DRIVER;
                bool isHistoryData = (bleData[10] & 0x80) != 0x00;
                bool latlngValid = (bleData[10] & 0x40) != 0x00;
                int satelliteNumber = bleData[10] & 0x1F;
                double altitude = latlngValid ? BytesUtils.Bytes2Float(bleData, 11) : 0.0;
                double longitude = latlngValid ? BytesUtils.Bytes2Float(bleData, 15) : 0.0;
                double latitude = latlngValid ? BytesUtils.Bytes2Float(bleData, 19) : 0.0;
                int azimuth = latlngValid ? BytesUtils.Bytes2Short(bleData, 25) : 0;
                float speedf = 0.0f;
                try
                {
                    byte[] bytesSpeed = new byte[2];
                    Array.Copy(bleData, 23, bytesSpeed, 0, 2);
                    String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
                    if (strSp.Contains("f"))
                    {
                        speedf = -1f;
                    }
                    else
                    {
                        speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));
                    }
                }
                catch (Exception e)
                {
                }
                Boolean is_4g_lbs = false;
                Int32 mcc_4g = -1;
                Int32 mnc_4g = -1;
                Int64 ci_4g = -1;
                Int32 earfcn_4g_1 = -1;
                Int32 pcid_4g_1 = -1;
                Int32 earfcn_4g_2 = -1;
                Int32 pcid_4g_2 = -1;
                Boolean is_2g_lbs = false;
                Int32 mcc_2g = -1;
                Int32 mnc_2g = -1;
                Int32 lac_2g_1 = -1;
                Int32 ci_2g_1 = -1;
                Int32 lac_2g_2 = -1;
                Int32 ci_2g_2 = -1;
                Int32 lac_2g_3 = -1;
                Int32 ci_2g_3 = -1;
                if (!latlngValid)
                {
                    byte lbsByte = bleData[11];
                    if ((lbsByte & 0x8) == 0x8)
                    {
                        is_2g_lbs = true;
                    }
                    else
                    {
                        is_4g_lbs = true;
                    }
                }
                if (is_2g_lbs)
                {
                    mcc_2g = BytesUtils.Bytes2Short(bleData, 11);
                    mnc_2g = BytesUtils.Bytes2Short(bleData, 13);
                    lac_2g_1 = BytesUtils.Bytes2Short(bleData, 15);
                    ci_2g_1 = BytesUtils.Bytes2Short(bleData, 17);
                    lac_2g_2 = BytesUtils.Bytes2Short(bleData, 19);
                    ci_2g_2 = BytesUtils.Bytes2Short(bleData, 21);
                    lac_2g_3 = BytesUtils.Bytes2Short(bleData, 23);
                    ci_2g_3 = BytesUtils.Bytes2Short(bleData, 25);
                }
                if (is_4g_lbs)
                {
                    mcc_4g = BytesUtils.Bytes2Short(bleData, 11);
                    mnc_4g = BytesUtils.Bytes2Short(bleData, 13);
                    ci_4g = BytesUtils.Byte2Int(bleData, 15);
                    earfcn_4g_1 = BytesUtils.Bytes2Short(bleData, 19);
                    pcid_4g_1 = BytesUtils.Bytes2Short(bleData, 21);
                    earfcn_4g_2 = BytesUtils.Bytes2Short(bleData, 23);
                    pcid_4g_2 = BytesUtils.Bytes2Short(bleData, 25);
                }
                bleDriverSignInData.Alert = alert;
                bleDriverSignInData.Altitude = altitude;
                bleDriverSignInData.Azimuth = azimuth;
                bleDriverSignInData.Voltage = voltage;
                bleDriverSignInData.IsHistoryData = isHistoryData;
                bleDriverSignInData.Latitude = latitude;
                bleDriverSignInData.LatlngValid = latlngValid;
                bleDriverSignInData.SatelliteCount = satelliteNumber;
                bleDriverSignInData.Longitude = longitude;
                bleDriverSignInData.Mac = mac;
                bleDriverSignInData.Speed = speedf;
                bleDriverSignInData.Is_2g_lbs = is_2g_lbs;
                bleDriverSignInData.Is_4g_lbs = is_4g_lbs;
                bleDriverSignInData.Mcc_4g = mcc_4g;
                bleDriverSignInData.Mnc_4g = mnc_4g;
                bleDriverSignInData.Ci_4g = ci_4g;
                bleDriverSignInData.Earfcn_4g_1 = earfcn_4g_1;
                bleDriverSignInData.Pcid_4g_1 = pcid_4g_1;
                bleDriverSignInData.Earfcn_4g_2 = earfcn_4g_2;
                bleDriverSignInData.Pcid_4g_2 = pcid_4g_2;
                bleDriverSignInData.Mcc_2g = mcc_2g;
                bleDriverSignInData.Mnc_2g = mnc_2g;
                bleDriverSignInData.Lac_2g_1 = lac_2g_1;
                bleDriverSignInData.Ci_2g_1 = ci_2g_1;
                bleDriverSignInData.Lac_2g_2 = lac_2g_2;
                bleDriverSignInData.Ci_2g_2 = ci_2g_2;
                bleDriverSignInData.Lac_2g_3 = lac_2g_3;
                bleDriverSignInData.Ci_2g_3 = ci_2g_3;
                bleDataList.Add(bleDriverSignInData);
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x04)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP;
                for (int i = 2; i < bleData.Length; i += 15)
                {
                    BleTempData bleTempData = new BleTempData();
                    byte[] macArray = new byte[6];
                    Array.Copy(bleData, i + 0, macArray, 0, 6);
                    String mac = BytesUtils.Bytes2HexString(macArray, 0);
                    if (mac.StartsWith("0000"))
                    {
                        mac = mac.Substring(4, 8);
                    }
                    int voltageTmp = (int)bleData[i + 6] < 0 ? (int)bleData[i + 6] + 256 : (int)bleData[i + 6];
                    float voltage;
                    if (voltageTmp == 255)
                    {
                        voltage = -999;
                    }
                    else
                    {
                        voltage = 2 + 0.01f * voltageTmp;
                    }
                    int batteryPercentTemp = (int)bleData[i + 7] < 0 ? (int)bleData[i + 7] + 256 : (int)bleData[i + 7];
                    int batteryPercent;
                    if (batteryPercentTemp == 255)
                    {
                        batteryPercent = -999;
                    }
                    else
                    {
                        batteryPercent = batteryPercentTemp;
                    }
                    int temperatureTemp = BytesUtils.Bytes2Short(bleData, i + 8);
                    int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                    float temperature;
                    if (temperatureTemp == 65535)
                    {
                        temperature = -999;
                    }
                    else
                    {
                        temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                    }
                    int humidityTemp = BytesUtils.Bytes2Short(bleData, i + 10);
                    float humidity;
                    if (humidityTemp == 65535)
                    {
                        humidity = -999;
                    }
                    else
                    {
                        humidity = humidityTemp * 0.01f;
                    }
                    int lightTemp = BytesUtils.Bytes2Short(bleData, i + 12);
                    bool isOpenBox = false;
                    int lightIntensity;
                    if (lightTemp == 65535)
                    {
                        lightIntensity = -999;
                    }
                    else
                    {
                        lightIntensity = lightTemp & 0xfff;
                        isOpenBox = (0x8000 & lightTemp) == 0x8000;
                    }
                    int rssiTemp = (int)bleData[i + 14] < 0 ? (int)bleData[i + 14] + 256 : (int)bleData[i + 14];
                    int rssi;
                    if (rssiTemp == 255)
                    {
                        rssi = -999;
                    }
                    else
                    {
                        rssi = rssiTemp - 128;
                    }
                    bleTempData.Rssi = rssi;
                    bleTempData.Mac = mac;
                    bleTempData.LightIntensity = lightIntensity;
                    bleTempData.IsOpenBox = isOpenBox;
                    bleTempData.Humidity = (float)Math.Round(humidity, 2, MidpointRounding.AwayFromZero);
                    bleTempData.Voltage = (float)Math.Round(voltage, 2, MidpointRounding.AwayFromZero);
                    bleTempData.BatteryPercent = batteryPercent;
                    bleTempData.Temp = (float)Math.Round(temperature, 2, MidpointRounding.AwayFromZero);
                    bleDataList.Add(bleTempData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x05)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR;
                for (int i = 2; i < bleData.Length; i += 12)
                {
                    BleDoorData bleDoorData = new BleDoorData();
                    byte[] macArray = new byte[6];
                    Array.Copy(bleData, i + 0, macArray, 0, 6);
                    String mac = BytesUtils.Bytes2HexString(macArray, 0);
                    if (mac.StartsWith("0000"))
                    {
                        mac = mac.Substring(4, 8);
                    }
                    int voltageTmp = (int)bleData[i + 6] < 0 ? (int)bleData[i + 6] + 256 : (int)bleData[i + 6];
                    float voltage;
                    if (voltageTmp == 255)
                    {
                        voltage = -999;
                    }
                    else
                    {
                        voltage = 2 + 0.01f * voltageTmp;
                    }
                    int batteryPercentTemp = (int)bleData[i + 7] < 0 ? (int)bleData[i + 7] + 256 : (int)bleData[i + 7];
                    int batteryPercent;
                    if (batteryPercentTemp == 255)
                    {
                        batteryPercent = -999;
                    }
                    else
                    {
                        batteryPercent = batteryPercentTemp;
                    }
                    int temperatureTemp = BytesUtils.Bytes2Short(bleData, i + 8);
                    int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                    float temperature;
                    if (temperatureTemp == 65535)
                    {
                        temperature = -999;
                    }
                    else
                    {
                        temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                    }
                    int doorStatus = bleData[i + 10];
                    int online = 1;
                    if (doorStatus == 255)
                    {
                        doorStatus = -999;
                        online = 0;
                    }
                    int rssiTemp = (int)bleData[i + 11] < 0 ? (int)bleData[i + 11] + 256 : (int)bleData[i + 11];
                    int rssi;
                    if (rssiTemp == 255)
                    {
                        rssi = -999;
                    }
                    else
                    {
                        rssi = rssiTemp - 128;
                    }
                    bleDoorData.Rssi = rssi;
                    bleDoorData.Mac = mac;
                    bleDoorData.DoorStatus = doorStatus;
                    bleDoorData.Online = online;
                    bleDoorData.Voltage = (float)Math.Round(voltage, 2, MidpointRounding.AwayFromZero);
                    bleDoorData.BatteryPercent = batteryPercent;
                    bleDoorData.Temp = (float)Math.Round(temperature, 2, MidpointRounding.AwayFromZero);
                    bleDataList.Add(bleDoorData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x06)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL;
                for (int i = 2; i < bleData.Length; i += 12)
                {
                    BleCtrlData bleCtrlData = new BleCtrlData();
                    byte[] macArray = new byte[6];
                    Array.Copy(bleData, i + 0, macArray, 0, 6);
                    String mac = BytesUtils.Bytes2HexString(macArray, 0);
                    if (mac.StartsWith("0000"))
                    {
                        mac = mac.Substring(4, 8);
                    }
                    int voltageTmp = (int)bleData[i + 6] < 0 ? (int)bleData[i + 6] + 256 : (int)bleData[i + 6];
                    float voltage;
                    if (voltageTmp == 255)
                    {
                        voltage = -999;
                    }
                    else
                    {
                        voltage = 2 + 0.01f * voltageTmp;
                    }
                    int batteryPercentTemp = (int)bleData[i + 7] < 0 ? (int)bleData[i + 7] + 256 : (int)bleData[i + 7];
                    int batteryPercent;
                    if (batteryPercentTemp == 255)
                    {
                        batteryPercent = -999;
                    }
                    else
                    {
                        batteryPercent = batteryPercentTemp;
                    }
                    int temperatureTemp = BytesUtils.Bytes2Short(bleData, i + 8);
                    int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                    float temperature;
                    if (temperatureTemp == 65535)
                    {
                        temperature = -999;
                    }
                    else
                    {
                        temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                    }
                    int ctrlStatus = bleData[i + 10];
                    int online = 1;
                    if (ctrlStatus == 255)
                    {
                        ctrlStatus = -999;
                        online = 0;
                    }
                    int rssiTemp = (int)bleData[i + 11] < 0 ? (int)bleData[i + 11] + 256 : (int)bleData[i + 11];
                    int rssi;
                    if (rssiTemp == 255)
                    {
                        rssi = -999;
                    }
                    else
                    {
                        rssi = rssiTemp - 128;
                    }
                    bleCtrlData.Rssi = rssi;
                    bleCtrlData.Mac = mac;
                    bleCtrlData.CtrlStatus = ctrlStatus;
                    bleCtrlData.Online = online;
                    bleCtrlData.Voltage = (float)Math.Round(voltage, 2, MidpointRounding.AwayFromZero);
                    bleCtrlData.BatteryPercent = batteryPercent;
                    bleCtrlData.Temp = (float)Math.Round(temperature, 2, MidpointRounding.AwayFromZero);
                    bleDataList.Add(bleCtrlData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x07)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL;
                for (int i = 2; i < bleData.Length; i += 15)
                {
                    BleFuelData bleFuelData = new BleFuelData();
                    byte[] macArray = new byte[6];
                    Array.Copy(bleData, i + 0, macArray, 0, 6);
                    String mac = BytesUtils.Bytes2HexString(macArray, 0);
                    if (mac.StartsWith("0000"))
                    {
                        mac = mac.Substring(4, 8);
                    }
                    int voltageTmp = (int)bleData[i + 6] < 0 ? (int)bleData[i + 6] + 256 : (int)bleData[i + 6];
                    float voltage;
                    if (voltageTmp == 255)
                    {
                        voltage = -999;
                    }
                    else
                    {
                        voltage = 2 + 0.01f * voltageTmp;
                    }
                    int valueTemp = BytesUtils.Bytes2Short(bleData, i + 7);
                    int value;
                    if (valueTemp == 255)
                    {
                        value = -999;
                    }
                    else
                    {
                        value = valueTemp;
                    }
                    int temperatureTemp = BytesUtils.Bytes2Short(bleData, i + 9);
                    int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                    float temperature;
                    if (temperatureTemp == 65535)
                    {
                        temperature = -999;
                    }
                    else
                    {
                        temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                    }
                    int status = (int)bleData[i + 13] < 0 ? (int)bleData[i + 13] + 256 : (int)bleData[i + 13];
                    int online = 1;
                    if (status == 255)
                    {
                        status = 0;
                        online = 0;
                    }
                    int rssiTemp = (int)bleData[i + 14] < 0 ? (int)bleData[i + 14] + 256 : (int)bleData[i + 14];
                    int rssi;
                    if (rssiTemp == 255)
                    {
                        rssi = -999;
                    }
                    else
                    {
                        rssi = rssiTemp - 128;
                    }
                    bleFuelData.Rssi = rssi;
                    bleFuelData.Mac = mac;
                    bleFuelData.Alarm = status;
                    bleFuelData.Online = online;
                    bleFuelData.Voltage = (float)Math.Round(voltage, 2, MidpointRounding.AwayFromZero);
                    bleFuelData.Value = value;
                    bleFuelData.Temp = (float)Math.Round(temperature, 2, MidpointRounding.AwayFromZero);
                    bleDataList.Add(bleFuelData);
                }
            }
            bluetoothPeripheralDataMessage.BleDataList = bleDataList;
            return bluetoothPeripheralDataMessage;
        }

        private LocationMessage parseDataMessage(byte[] data) {
        int serialNo = BytesUtils.Bytes2Short(data, 5);
        //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
        String imei = BytesUtils.IMEI.Decode(data, 7);
        DateTime date = Utils.getGTM0Date(data, 17);
        bool isGpsWorking = (data[15] & 0x20) == 0x00;
        bool isHistoryData = (data[15] & 0x80) != 0x00;
        bool latlngValid = (data[15] & 0x40) == 0x40;
        int satelliteNumber = data[15] & 0x1F;
        double altitude = latlngValid ? BytesUtils.Bytes2Float(data,23) : 0;
        double longitude = latlngValid ? BytesUtils.Bytes2Float(data,27) : 0;
        double latitude = latlngValid ? BytesUtils.Bytes2Float(data,31) : 0;
        float speedf = 0.0f;
        if (latlngValid)
        {
            byte[] bytesSpeed = Utils.ArrayCopyOfRange(data, 35, 37);
            String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
            speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));
        }
        int azimuth = latlngValid ? BytesUtils.Bytes2Short(data, 37) : 0;
        Boolean is_4g_lbs = false;
        Int32 mcc_4g = -1;
        Int32 mnc_4g = -1;
        Int64 ci_4g = -1;
        Int32 earfcn_4g_1 = -1;
        Int32 pcid_4g_1 = -1;
        Int32 earfcn_4g_2 = -1;
        Int32 pcid_4g_2 = -1;
        Boolean is_2g_lbs = false;
        Int32 mcc_2g = -1;
        Int32 mnc_2g = -1;
        Int32 lac_2g_1 = -1;
        Int32 ci_2g_1 = -1;
        Int32 lac_2g_2 = -1;
        Int32 ci_2g_2 = -1;
        Int32 lac_2g_3 = -1;
        Int32 ci_2g_3 = -1;
        if (!latlngValid)
        {
            byte lbsByte = data[23];
            if ((lbsByte & 0x8) == 0x8)
            {
                is_2g_lbs = true;
            }
            else
            {
                is_4g_lbs = true;
            }
        }
        if (is_2g_lbs)
        {
            mcc_2g = BytesUtils.Bytes2Short(data, 23);
            mnc_2g = BytesUtils.Bytes2Short(data, 25);
            lac_2g_1 = BytesUtils.Bytes2Short(data, 27);
            ci_2g_1 = BytesUtils.Bytes2Short(data, 29);
            lac_2g_2 = BytesUtils.Bytes2Short(data, 31);
            ci_2g_2 = BytesUtils.Bytes2Short(data, 33);
            lac_2g_3 = BytesUtils.Bytes2Short(data, 35);
            ci_2g_3 = BytesUtils.Bytes2Short(data, 37);
        }
        if (is_4g_lbs)
        {
            mcc_4g = BytesUtils.Bytes2Short(data, 23);
            mnc_4g = BytesUtils.Bytes2Short(data, 25);
            ci_4g = BytesUtils.Byte2Int(data, 27);
            earfcn_4g_1 = BytesUtils.Bytes2Short(data, 31);
            pcid_4g_1 = BytesUtils.Bytes2Short(data, 33);
            earfcn_4g_2 = BytesUtils.Bytes2Short(data, 35);
            pcid_4g_2 = BytesUtils.Bytes2Short(data, 37);
        }
        int axisXDirect = (data[39] & 0x80) == 0x80 ? 1 : -1;
        float axisX = ((data[39] & 0x7F & 0xff) + (((data[40] & 0xf0) >> 4) & 0xff) /10.0f) * axisXDirect;

        int axisYDirect = (data[40] & 0x08) == 0x08 ? 1 : -1;
        float axisY = (((((data[40] & 0x07) << 4) & 0xff) + (((data[41] & 0xf0) >> 4) & 0xff)) + (data[41] & 0x0F & 0xff)/10.0f)* axisYDirect;

        int axisZDirect = (data[42] & 0x80) == 0x80 ? 1 : -1;
        float axisZ = ((data[42] & 0x7F & 0xff) + (((data[43] & 0xf0) >> 4) & 0xff) /10.0f) * axisZDirect;

        byte[] batteryPercentBytes = new byte[]{data[44]};
        String batteryPercentStr = BytesUtils.Bytes2HexString(batteryPercentBytes, 0);
        int batteryPercent = 100;
        if(batteryPercentStr.ToLower().Equals("ff")){
            batteryPercent = 100;
        }else{
            batteryPercent = Convert.ToInt32(batteryPercentStr);
            if (0 == batteryPercent)
            {
                batteryPercent = 100;
            }
        }

        float deviceTemp = (data[45] & 0x7F) * ((data[45] & 0x80) == 0x80 ? -1 : 1);
        byte[] lightSensorBytes = new byte[]{data[46]};
        String lightSensorStr = BytesUtils.Bytes2HexString(lightSensorBytes, 0);
        float lightSensor = 0;
        if (lightSensorStr.ToLower().Equals("ff"))
        {
            lightSensor = -1;
        }else{
            lightSensor = Convert.ToInt32(lightSensorStr) / 10.0f;

        }

        byte[] batteryVoltageBytes = new byte[]{data[47]};
        String batteryVoltageStr = BytesUtils.Bytes2HexString(batteryVoltageBytes, 0);
        float batteryVoltage = 0;
        if(batteryVoltageStr.ToLower().Equals("ff")){
            batteryVoltage = -1;
        }else{
            batteryVoltage = Convert.ToInt32(batteryVoltageStr) / 10.0f;
        }
        byte[] solarVoltageBytes = new byte[]{data[48]};
        String solarVoltageStr = BytesUtils.Bytes2HexString(solarVoltageBytes, 0);
        float solarVoltage = 0;
        if(solarVoltageStr.ToLower().Equals("ff")){
            solarVoltage = -1;
        }else{
            solarVoltage = Convert.ToInt32(solarVoltageStr) / 10.0f;
        }

        long mileage = BytesUtils.Byte2Int(data, 49);
        int status = BytesUtils.Bytes2Short(data, 53);
        int network = (status & 0x7F0) >> 4;
        int accOnInterval = BytesUtils.Bytes2Short(data, 55);
        int accOffInterval = BytesUtils.Bytes2Short(data, 57);
        int angleCompensation = (int) data[61];
        int distanceCompensation = BytesUtils.Bytes2Short(data, 62);
        int heartbeatInterval = (int) data[64];
        bool isUsbCharging = (status & 0x8000) == 0x8000;
        bool isSolarCharging = (status & 0x8) == 0x8;
        bool iopIgnition = (status & 0x4) == 0x4;
        byte alarmByte = data[16];
        int originalAlarmCode = (int) alarmByte;
        byte[] command = new byte[HEADER_LENGTH];
        Array.Copy(data, 0, command, 0, HEADER_LENGTH);
        bool isAlarmData = command[2] == 0x04;
        byte status1 = data[57];
        String smartPowerOpenStatus = "close";
        if ((status1 & 0x01) == 0x01)
        {
            smartPowerOpenStatus = "enable";
        }
        byte status3 = data[67];
        String smartPowerSettingStatus = "disable";
        if ((status3 & 0x80) == 0x80)
        {
            smartPowerSettingStatus = "enable";
        }
        LocationMessage locationMessage;
        if (isAlarmData){
            locationMessage = new LocationAlarmMessage();
        }else {
            locationMessage = new LocationInfoMessage();
        }
        locationMessage.OrignBytes=data;
        locationMessage.SerialNo=serialNo;
        //locationMessage.IsNeedResp = isNeedResp;
        locationMessage.NetworkSignal=network;
        locationMessage.Imei=imei;
        locationMessage.IsSolarCharging=isSolarCharging;
        locationMessage.IsUsbCharging=isUsbCharging;
        locationMessage.SamplingIntervalAccOn=accOnInterval;
        locationMessage.SamplingIntervalAccOff=accOffInterval;
        locationMessage.AngleCompensation=angleCompensation;
        locationMessage.DistanceCompensation=distanceCompensation;
        locationMessage.GpsWorking=isGpsWorking;
        locationMessage.IsHistoryData=isHistoryData;
        locationMessage.SatelliteNumber=satelliteNumber;
        locationMessage.HeartbeatInterval=heartbeatInterval;
        locationMessage.OriginalAlarmCode=originalAlarmCode; 
        locationMessage.Mileage=mileage;
        locationMessage.IopIgnition=iopIgnition;
        locationMessage.IOP=iopIgnition ? 0x4000L :0x0000L;
        locationMessage.BatteryCharge=batteryPercent; 
        locationMessage.Date=date;
        locationMessage.LatlngValid=latlngValid;
        locationMessage.Altitude=altitude;
        locationMessage.Latitude=latitude;
        locationMessage.Longitude=longitude;
        if(locationMessage.LatlngValid) {
            locationMessage.Speed=speedf;
        } else {
            locationMessage.Speed=0.0f;
        }
        locationMessage.Azimuth=azimuth;
        locationMessage.AxisX=axisX;
        locationMessage.AxisY=axisY;
        locationMessage.AxisZ=axisZ;
        locationMessage.DeviceTemp=deviceTemp;
        locationMessage.LightSensor=lightSensor;
        locationMessage.BatteryVoltage=batteryVoltage;
        locationMessage.SolarVoltage=solarVoltage;
        locationMessage.SmartPowerOpenStatus = smartPowerOpenStatus;
        locationMessage.SmartPowerSettingStatus = smartPowerSettingStatus;
        locationMessage.Is_2g_lbs = is_2g_lbs;
        locationMessage.Is_4g_lbs = is_4g_lbs;
        locationMessage.Mcc_4g = mcc_4g;
        locationMessage.Mnc_4g = mnc_4g;
        locationMessage.Ci_4g = ci_4g;
        locationMessage.Earfcn_4g_1 = earfcn_4g_1;
        locationMessage.Pcid_4g_1 = pcid_4g_1;
        locationMessage.Earfcn_4g_2 = earfcn_4g_2;
        locationMessage.Pcid_4g_2 = pcid_4g_2;
        locationMessage.Mcc_2g = mcc_2g;
        locationMessage.Mnc_2g = mnc_2g;
        locationMessage.Lac_2g_1 = lac_2g_1;
        locationMessage.Ci_2g_1 = ci_2g_1;
        locationMessage.Lac_2g_2 = lac_2g_2;
        locationMessage.Ci_2g_2 = ci_2g_2;
        locationMessage.Lac_2g_3 = lac_2g_3;
        locationMessage.Ci_2g_3 = ci_2g_3;
        return locationMessage;
    }
     
    

    private SignInMessage parseLoginMessage(byte[] bytes)  {
        int serialNo = BytesUtils.Bytes2Short(bytes, 5);
        //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
        String imei = BytesUtils.IMEI.Decode(bytes, 7);
        String str = BytesUtils.Bytes2HexString(bytes, 15);
        String software = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 15, 17), 0);
        software = String.Format("V{0}.{1}.{2}", Convert.ToInt32(software.Substring(1, 1), 16), Convert.ToInt32(software.Substring(2, 1), 16), Convert.ToInt32(software.Substring(3, 1), 16));
        String firmware = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 20, 22), 0);
        firmware = String.Format("V{0}.{1}.{2}", firmware.Substring(0, 1), firmware.Substring(1, 1), firmware.Substring(2, 2));
        String hardware = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 22, 23), 0);
        hardware = String.Format("V{0}.{1}", hardware.Substring(0, 1), hardware.Substring(1, 1));
        SignInMessage signInMessage = new SignInMessage();
        signInMessage.SerialNo=serialNo;
        //signInMessage.IsNeedResp = isNeedResp;
        signInMessage.Imei=imei;
        signInMessage.Software=software;
        signInMessage.Firmware=firmware;
        signInMessage.Hareware=hardware;
        signInMessage.OrignBytes=bytes;
        return signInMessage;
    }
    private HeartbeatMessage parseHeartbeat(byte[] bytes)
    {
        int serialNo = BytesUtils.Bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.Decode(bytes, 7);
        //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
        HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
        heartbeatMessage.OrignBytes = bytes;
        heartbeatMessage.SerialNo = serialNo;
        //heartbeatMessage.IsNeedResp = isNeedResp;
        heartbeatMessage.Imei = imei;
        return heartbeatMessage;
    }

    private NetworkInfoMessage parseNetworkInfoMessage(byte[] bytes)
    {
        NetworkInfoMessage networkInfoMessage = new NetworkInfoMessage();
        int serialNo = BytesUtils.Bytes2Short(bytes, 5);
        //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        String imei = BytesUtils.IMEI.Decode(bytes, 7);
        DateTime gmt0 = Utils.getGTM0Date(bytes, 15);
        int networkOperatorLen = bytes[21];
        int networkOperatorStartIndex = 22;
        byte[] networkOperatorByte = Utils.ArrayCopyOfRange(bytes, networkOperatorStartIndex, networkOperatorStartIndex + networkOperatorLen);
        String networkOperator = Encoding.GetEncoding("UTF-16LE").GetString(networkOperatorByte); ;
        int accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen];
        int accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        byte[] accessTechnologyByte = Utils.ArrayCopyOfRange(bytes, accessTechnologyStartIndex, accessTechnologyStartIndex + accessTechnologyLen);
        String accessTechnology = System.Text.Encoding.UTF8.GetString(accessTechnologyByte);
        int bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen];
        int bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1;
        byte[] bandLenByte = Utils.ArrayCopyOfRange(bytes, bandStartIndex, bandStartIndex + bandLen);
        String band = System.Text.Encoding.UTF8.GetString(bandLenByte);
        int msgLen = BytesUtils.Bytes2Short(bytes, 3);
        if (msgLen > bandStartIndex + bandLen)
        {
            int IMSILen = bytes[bandStartIndex + bandLen];
            int IMSIStartIndex = bandStartIndex + bandLen + 1;
            byte[] IMSILenByte = Utils.ArrayCopyOfRange(bytes, IMSIStartIndex, IMSIStartIndex + IMSILen);
            String IMSI = System.Text.Encoding.UTF8.GetString(IMSILenByte);
            networkInfoMessage.Imsi = IMSI;
            if (msgLen > IMSIStartIndex + IMSILen)
            {
                int iccidLen = bytes[IMSIStartIndex + IMSILen];
                int iccidStartIndex = IMSIStartIndex + IMSILen + 1;
                byte[] iccidLenByte = Utils.ArrayCopyOfRange(bytes, iccidStartIndex, iccidStartIndex + iccidLen);
                String iccid = System.Text.Encoding.UTF8.GetString(iccidLenByte);
                networkInfoMessage.Iccid = iccid;
            }
        }
        networkInfoMessage.SerialNo = serialNo;
        //networkInfoMessage.IsNeedResp = isNeedResp;
        networkInfoMessage.Imei = imei;
        networkInfoMessage.OrignBytes = bytes;
        networkInfoMessage.Date = gmt0;
        networkInfoMessage.AccessTechnology = accessTechnology;
        networkInfoMessage.NetworkOperator = networkOperator;
        networkInfoMessage.Band = band;

        return networkInfoMessage;
    }

    private Message parseInteractMessage(byte[] bytes)
    {
        int serialNo = BytesUtils.Bytes2Short(bytes, 5);
        //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
        String imei = BytesUtils.IMEI.Decode(bytes, 7);
        byte protocol = bytes[15];
        byte[] data = new byte[bytes.Length - 16];
        Array.Copy(bytes, 16, data, 0, bytes.Length - 16);
        String messageData;
        switch (protocol)
        {
            case 0x01:
                {
                    ConfigMessage configMessage = new ConfigMessage();
                    messageData = Encoding.GetEncoding("UTF-16LE").GetString(data);
                    configMessage.ConfigResultContent = messageData;
                    configMessage.OrignBytes = bytes;
                    configMessage.SerialNo = serialNo;
                    //configMessage.IsNeedResp = isNeedResp;
                    configMessage.Imei = imei;
                    return configMessage;
                }
            case 0x03:
                {
                    ForwardMessage forwardMessage = new ForwardMessage();
                    messageData = Encoding.GetEncoding("UTF-16LE").GetString(data);
                    forwardMessage.Content = messageData;
                    forwardMessage.OrignBytes = bytes;
                    forwardMessage.SerialNo = serialNo;
                    //forwardMessage.IsNeedResp = isNeedResp;
                    forwardMessage.Imei = imei;
                    return forwardMessage;
                }
            case 0x05:
                {
                    USSDMessage ussdMessage = new USSDMessage();
                    messageData = Encoding.GetEncoding("UTF-16lE").GetString(data);
                    ussdMessage.Content = messageData;
                    ussdMessage.OrignBytes = bytes;
                    ussdMessage.SerialNo = serialNo;
                    //ussdMessage.IsNeedResp = isNeedResp;
                    ussdMessage.Imei = imei;
                    return ussdMessage;
                }
            default:
                throw new Exception("Error config message");
        }

    }

    }
}
