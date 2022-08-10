using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using TopflytechCodec.Entities;

namespace TopflytechCodec
{
    public class ObdDecoder
    {
        private static int HEADER_LENGTH = 3;

        private static byte[] SIGNUP = { 0x26, 0x26, 0x01 };

        private static byte[] DATA = { 0x26, 0x26, 0x02 };

        private static byte[] HEARTBEAT = { 0x26, 0x26, 0x03 };

        private static byte[] ALARM = { 0x26, 0x26, 0x04 };

        private static byte[] CONFIG = { 0x26, 0x26, (byte)0x81 };
        private static byte[] GPS_DRIVER_BEHAVIOR = { 0x26, 0x26, (byte)0x05 };
        private static byte[] ACCELERATION_DRIVER_BEHAVIOR = { 0x26, 0x26, (byte)0x06 };
        private static byte[] ACCELERATION_ALARM = { 0x26, 0x26, (byte)0x07 };
        private static byte[] BLUETOOTH_MAC = { 0x26, 0x26, (byte)0x08 };
        private static byte[] OBD_DATA = { 0x26, 0x26, (byte)0x09 };
        private static byte[] BLUETOOTH_DATA = { 0x26, 0x26, (byte)0x10 };
        private static byte[] NETWORK_INFO_DATA = { 0x26, 0x26, (byte)0x11 };
        private static byte[] LOCATION_DATA_WITH_SENSOR =  {0x26, 0x26, (byte)0x16};
        private static byte[] LOCATION_ALARM_WITH_SENSOR =  {0x26, 0x26, (byte)0x18};
        private static byte[] latlngInvalidData = {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
            (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};

        private static byte[] obdHead = { 0x55, (byte)0xAA };
        private int encryptType = 0;
        private String aesKey;
        private static long MASK_IGNITION = 0x4000;
        private static long MASK_POWER_CUT = 0x8000;
        private static long MASK_AC = 0x2000;

        public ObdDecoder(int messageEncryptType, String aesKey)
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
                    || Utils.ArrayEquals(CONFIG, bytes)
                    || Utils.ArrayEquals(GPS_DRIVER_BEHAVIOR, bytes)
                    || Utils.ArrayEquals(ACCELERATION_DRIVER_BEHAVIOR, bytes)
                    || Utils.ArrayEquals(ACCELERATION_ALARM, bytes)
                    || Utils.ArrayEquals(BLUETOOTH_MAC, bytes)
                    || Utils.ArrayEquals(OBD_DATA, bytes)
                    || Utils.ArrayEquals(BLUETOOTH_DATA, bytes)
                    || Utils.ArrayEquals(NETWORK_INFO_DATA, bytes)
                    || Utils.ArrayEquals(LOCATION_DATA_WITH_SENSOR, bytes)
                    || Utils.ArrayEquals(LOCATION_ALARM_WITH_SENSOR, bytes);
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
                    if (packageLength <= 0)
                    {
                        decoderBuf.SkipBytes(5);
                        break;
                    }
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


        private Message build(byte[] bytes)
        {
            if (bytes != null && bytes.Length > HEADER_LENGTH
                    && ((bytes[0] == 0x26 && bytes[1] == 0x26)))
            {
                switch (bytes[2])
                {
                    case 0x01:
                        SignInMessage signInMessage = parseLoginMessage(bytes);
                        return signInMessage;
                    case 0x03:
                        HeartbeatMessage heartbeatMessage = parseHeartbeat(bytes);
                        return heartbeatMessage;
                    case 0x02:
                    case 0x04:
                    case 0x16:
                    case 0x18:
                        LocationMessage locationMessage = parseDataMessage(bytes);
                        return locationMessage;
                    case 0x05:
                        GpsDriverBehaviorMessage gpsDriverBehaviorMessage = parseGpsDriverBehavorMessage(bytes);
                        return gpsDriverBehaviorMessage;
                    case 0x06:
                        AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = parseAccelerationDriverBehaviorMessage(bytes);
                        return accelerationDriverBehaviorMessage;
                    case 0x07:
                        AccidentAccelerationMessage accidentAccelerationMessage = parseAccelerationAlarmMessage(bytes);
                        return accidentAccelerationMessage;
                    case 0x09:
                        ObdMessage obdMessage = parseObdMessage(bytes);
                        return obdMessage;
                    case 0x10:
                        BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = parseBluetoothDataMessage(bytes);
                        return bluetoothPeripheralDataMessage;
                    case 0x11:
                        NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                        return networkInfoMessage;
                    case 0x12:
                        BluetoothPeripheralDataMessage bluetoothPeripheralDataSecondMessage = parseSecondBluetoothDataMessage(bytes);
                        return bluetoothPeripheralDataSecondMessage;
                    case (byte)0x81:
                        Message message = parseInteractMessage(bytes);
                        return message;
                    default:
                        return null;
                }
            }
            return null;
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

        private BluetoothPeripheralDataMessage parseSecondBluetoothDataMessage(byte[] bytes)
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
            bluetoothPeripheralDataMessage.ProtocolHeadType = 0x12;
            bluetoothPeripheralDataMessage.OrignBytes = bytes;
            bluetoothPeripheralDataMessage.IsHistoryData = (bytes[15] & 0x80) != 0x00;
            bluetoothPeripheralDataMessage.SerialNo = serialNo;
            //bluetoothPeripheralDataMessage.IsNeedResp = isNeedResp;
            bluetoothPeripheralDataMessage.Imei = imei;
            bool latlngValid = (bytes[22] & 0x40) == 0x40;
            bool isHisData = (bytes[22] & 0x80) == 0x80;
            bluetoothPeripheralDataMessage.LatlngValid = latlngValid;
            bluetoothPeripheralDataMessage.IsHistoryData = isHisData;
            double altitude = latlngValid ? BytesUtils.Bytes2Short(bytes, 23) : 0.0;
            double latitude = latlngValid ? BytesUtils.Bytes2Short(bytes, 27) : 0.0;
            double longitude = latlngValid ? BytesUtils.Bytes2Short(bytes, 31) : 0.0;
            int azimuth = latlngValid ? BytesUtils.Bytes2Short(bytes, 37) : 0;
            float speedf = 0.0f;
            try
            {
                byte[] bytesSpeed = new byte[2];
                Array.Copy(bytes, 35, bytesSpeed, 0, 2);
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
                byte lbsByte = bytes[23];
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
                mcc_2g = BytesUtils.Bytes2Short(bytes, 23);
                mnc_2g = BytesUtils.Bytes2Short(bytes, 25);
                lac_2g_1 = BytesUtils.Bytes2Short(bytes, 27);
                ci_2g_1 = BytesUtils.Bytes2Short(bytes, 29);
                lac_2g_2 = BytesUtils.Bytes2Short(bytes, 31);
                ci_2g_2 = BytesUtils.Bytes2Short(bytes, 33);
                lac_2g_3 = BytesUtils.Bytes2Short(bytes, 35);
                ci_2g_3 = BytesUtils.Bytes2Short(bytes, 37);
            }
            if (is_4g_lbs)
            {
                mcc_4g = BytesUtils.Bytes2Short(bytes, 23);
                mnc_4g = BytesUtils.Bytes2Short(bytes, 25);
                ci_4g = BytesUtils.Byte2Int(bytes, 27);
                earfcn_4g_1 = BytesUtils.Bytes2Short(bytes, 31);
                pcid_4g_1 = BytesUtils.Bytes2Short(bytes, 33);
                earfcn_4g_2 = BytesUtils.Bytes2Short(bytes, 35);
                pcid_4g_2 = BytesUtils.Bytes2Short(bytes, 37);
            }
            bluetoothPeripheralDataMessage.IsHadLocationInfo = true;
            bluetoothPeripheralDataMessage.Altitude = altitude;
            bluetoothPeripheralDataMessage.Azimuth = azimuth;
            bluetoothPeripheralDataMessage.Latitude = latitude;
            bluetoothPeripheralDataMessage.Longitude = longitude;
            bluetoothPeripheralDataMessage.Speed = speedf;
            bluetoothPeripheralDataMessage.Is_2g_lbs = is_2g_lbs;
            bluetoothPeripheralDataMessage.Is_4g_lbs = is_4g_lbs;
            bluetoothPeripheralDataMessage.Mcc_4g = mcc_4g;
            bluetoothPeripheralDataMessage.Mnc_4g = mnc_4g;
            bluetoothPeripheralDataMessage.Ci_4g = ci_4g;
            bluetoothPeripheralDataMessage.Earfcn_4g_1 = earfcn_4g_1;
            bluetoothPeripheralDataMessage.Pcid_4g_1 = pcid_4g_1;
            bluetoothPeripheralDataMessage.Earfcn_4g_2 = earfcn_4g_2;
            bluetoothPeripheralDataMessage.Pcid_4g_2 = pcid_4g_2;
            bluetoothPeripheralDataMessage.Mcc_2g = mcc_2g;
            bluetoothPeripheralDataMessage.Mnc_2g = mnc_2g;
            bluetoothPeripheralDataMessage.Lac_2g_1 = lac_2g_1;
            bluetoothPeripheralDataMessage.Ci_2g_1 = ci_2g_1;
            bluetoothPeripheralDataMessage.Lac_2g_2 = lac_2g_2;
            bluetoothPeripheralDataMessage.Ci_2g_2 = ci_2g_2;
            bluetoothPeripheralDataMessage.Lac_2g_3 = lac_2g_3;
            bluetoothPeripheralDataMessage.Ci_2g_3 = ci_2g_3;

            byte[] bleData = new byte[bytes.Length - 39];
            Array.Copy(bytes, 39, bleData, 0, bleData.Length);
            List<BleData> bleDataList = new List<BleData>();
            if (bleData[0] == 0x00 && bleData[1] == 0x01)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE;
                for (int i = 2; i+10 <= bleData.Length; i += 10)
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

                bleAlertData.AlertType = alert;
                bleAlertData.Altitude = altitude;
                bleAlertData.Azimuth = azimuth;
                bleAlertData.InnerVoltage = voltage;
                bleAlertData.IsHistoryData = isHisData;
                bleAlertData.Latitude = latitude;
                bleAlertData.LatlngValid = latlngValid;
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

                bleDriverSignInData.Alert = alert;
                bleDriverSignInData.Altitude = altitude;
                bleDriverSignInData.Azimuth = azimuth;
                bleDriverSignInData.Voltage = voltage;
                bleDriverSignInData.IsHistoryData = isHisData;
                bleDriverSignInData.Latitude = latitude;
                bleDriverSignInData.LatlngValid = latlngValid;
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
                for (int i = 2; i+15 <= bleData.Length; i += 15)
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
                    int lightIntensity;
                    if (lightTemp == 65535)
                    {
                        lightIntensity = -999;
                    }
                    else
                    {
                        lightIntensity = lightTemp & 0x0001;
                    }
                    int rssiTemp = (int)bleData[i + 14] < 0 ? (int)bleData[i + 14] + 256 : (int)bleData[i + 14];
                    int rssi;
                    if (rssiTemp == 255)
                    {
                        rssi = -999;
                    }
                    else
                    {
                        rssi = rssiTemp - 256;
                    }
                    bleTempData.Rssi = rssi;
                    bleTempData.Mac = mac;
                    bleTempData.LightIntensity = lightIntensity;
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
                for (int i = 2; i+12 <= bleData.Length; i += 12)
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
                    int doorStatus = (int)bleData[i + 10] < 0 ? (int)bleData[i + 10] + 256 : (int)bleData[i + 10];
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
                        rssi = rssiTemp - 256;
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
                for (int i = 2; i+12 <= bleData.Length; i += 12)
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
                    int ctrlStatus = (int)bleData[i + 10] < 0 ? (int)bleData[i + 10] + 256 : (int)bleData[i + 10];
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
                        rssi = rssiTemp - 256;
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
                for (int i = 2; i+15 <= bleData.Length; i += 15)
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
                        rssi = rssiTemp - 256;
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
                for (int i = 2; i+10 <= bleData.Length; i += 10)
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
                    //            bool isTireLeaks = (bleData[i+5] == 0x01);
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
                for (int i = 2; i+15 <= bleData.Length; i += 15)
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
                    int lightIntensity;
                    if (lightTemp == 65535)
                    {
                        lightIntensity = -999;
                    }
                    else
                    {
                        lightIntensity = lightTemp & 0x0001;
                    }
                    int rssiTemp = (int)bleData[i + 14] < 0 ? (int)bleData[i + 14] + 256 : (int)bleData[i + 14];
                    int rssi;
                    if (rssiTemp == 255)
                    {
                        rssi = -999;
                    }
                    else
                    {
                        rssi = rssiTemp - 256;
                    }
                    bleTempData.Rssi = rssi;
                    bleTempData.Mac = mac;
                    bleTempData.LightIntensity = lightIntensity;
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
                for (int i = 2; i+12 <= bleData.Length; i += 12)
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
                        rssi = rssiTemp - 256;
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
                for (int i = 2; i+12 <= bleData.Length; i += 12)
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
                        rssi = rssiTemp - 256;
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
                for (int i = 2; i+15 <= bleData.Length; i += 15)
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
                        rssi = rssiTemp - 256;
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


        private SignInMessage parseLoginMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            String str = BytesUtils.Bytes2HexString(bytes, 15);
            char[] strChars = str.ToCharArray();
            String software = String.Format("V{0}.{1}.{2}", Convert.ToInt32(str.Substring(0, 1), 16), Convert.ToInt32(str.Substring(1, 1), 16), Convert.ToInt32(str.Substring(2, 1), 16));
            String firmware = String.Format("V{0}.{1}.{2}", Convert.ToInt32(str.Substring(3, 1), 16), Convert.ToInt32(str.Substring(4, 1), 16), Convert.ToInt32(str.Substring(5, 1), 16));
            String platform = str.Substring(6, 4);
            String hardware = String.Format("{0}.{1}", Convert.ToInt32(str.Substring(10, 1), 16), Convert.ToInt32(str.Substring(11, 1), 16));
            int obdV1 = (int)bytes[21];
            int obdV2 = (int)bytes[22];
            int obdV3 = (int)bytes[23];
            if (obdV1 < 0)
            {
                obdV1 += 256;
            }
            if (obdV2 < 0)
            {
                obdV2 += 256;
            }
            if (obdV3 < 0)
            {
                obdV3 += 256;
            }
            SignInMessage signInMessage = new SignInMessage();
            String obdSoftware = String.Format("V{0}.{1}.{2}", obdV1, obdV2, obdV3);
            String obdHardware = String.Format("{0}.{1}", strChars[18], strChars[19]);
            signInMessage.SerialNo = serialNo;
            //signInMessage.IsNeedResp = isNeedResp;
            signInMessage.Imei = imei;
            signInMessage.Software = software;
            signInMessage.Firmware = firmware;
            signInMessage.Platform = platform;
            signInMessage.Hareware = hardware;
            signInMessage.OrignBytes = bytes;
            signInMessage.ObdHareware = obdHardware;
            signInMessage.ObdSoftware = obdSoftware;
            return signInMessage;
        }


        private HeartbeatMessage parseHeartbeat(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
            heartbeatMessage.OrignBytes = bytes;
            heartbeatMessage.SerialNo = serialNo;
            //heartbeatMessage.IsNeedResp = isNeedResp;
            heartbeatMessage.Imei = imei;
            return heartbeatMessage;
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

        private ObdMessage parseObdMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            DateTime date = Utils.getGTM0Date(bytes, 15);

            ObdMessage obdData = new ObdMessage();
            obdData.Imei = imei;
            obdData.OrignBytes = bytes;
            obdData.SerialNo = serialNo;
            //obdData.IsNeedResp = isNeedResp;
            obdData.Date = date;
            byte[] obdBytes = Utils.ArrayCopyOfRange(bytes, 21, bytes.Length);

            byte[] head = new byte[2];
            head[0] = obdBytes[0];
            head[1] = obdBytes[1];
            if (Utils.ArrayEquals(head, obdHead))
            {
                obdBytes[2] = (byte)(obdBytes[2] & 0x0F);//去除高位
                int length = BytesUtils.Bytes2Short(obdBytes, 2);
                if (length > 0)
                {
                    try
                    {
                        byte[] data = Utils.ArrayCopyOfRange(obdBytes, 4, 4 + length);
                        if ((data[0] & 0x41) == 0x41 && data[1] == 0x04 && data.Length > 3)
                        {
                            obdData.MessageType = ObdMessage.CLEAR_ERROR_CODE_MESSAGE;
                            obdData.ClearErrorCodeSuccess = data[2] == 0x01;
                        }
                        else if ((data[0] & 0x41) == 0x41 && data[1] == 0x05 && data.Length > 2)
                        {
                            byte[] vinData = Utils.ArrayCopyOfRange(data, 2, data.Length - 1);
                            bool dataValid = false;
                            foreach (byte item in vinData)
                            {
                                if ((item & 0xFF) != 0xFF)
                                {
                                    dataValid = true;
                                }
                            }
                            if (vinData.Length > 0 && dataValid)
                            {
                                obdData.MessageType = ObdMessage.VIN_MESSAGE;
                                obdData.Vin = System.Text.Encoding.UTF8.GetString(vinData);
                            }
                        }
                        else if ((data[0] & 0x41) == 0x41 && (data[1] == 0x03 || data[1] == 0x0A))
                        {
                            int errorCode = data[2];
                            byte[] errorDataByte = Utils.ArrayCopyOfRange(data, 3, data.Length - 1);
                            String errorDataStr = BytesUtils.Bytes2HexString(errorDataByte, 0);
                            if (errorDataStr != null)
                            {
                                String errorDataSum = "";
                                for (int i = 0; i+6 <= errorDataStr.Length; i += 6)
                                {
                                    String errorDataItem = errorDataStr.Substring(i, 6);
                                    String srcFlag = errorDataItem.Substring(0, 1);
                                    String errorDataCode = getObdErrorFlag(srcFlag) + errorDataItem.Substring(1, 4);
                                    if (!errorDataSum.Contains(errorDataCode))
                                    {
                                        if (i != 0)
                                        {
                                            errorDataSum += ";";
                                        }
                                        errorDataSum += errorDataCode;
                                    }
                                    if (i + 6 >= errorDataStr.Length)
                                    {
                                        break;
                                    }
                                }
                                obdData.MessageType = ObdMessage.ERROR_CODE_MESSAGE;
                                obdData.ErrorCode = getObdErrorCode(errorCode);
                                obdData.ErrorData = errorDataSum;
                            }
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }
            }
            return obdData;
        }

        private String getObdErrorCode(int errorCode)
        {
            if (errorCode == 0)
            {
                return "J1979";
            }
            else if (errorCode == 1)
            {
                return "J1939";
            }
            return "";
        }

        private String getObdErrorFlag(String srcFlag)
        {
            byte[] data = BytesUtils.HexString2Bytes(srcFlag);
            if (data[0] >= 0 && data[0] < 4)
            {
                return "P" + (int)(data[0]);
            }
            else if (data[0] >= 4 && data[0] < 8)
            {
                return "C" + (int)(data[0] - 4);
            }
            else if (data[0] >= 8 && data[0] < 12)
            {
                return "B" + (int)(data[0] - 8);
            }
            else
            {
                return "U" + (int)(data[0] - 12);
            }
        }




        private GpsDriverBehaviorMessage parseGpsDriverBehavorMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            GpsDriverBehaviorMessage gpsDriverBehaviorMessage = new GpsDriverBehaviorMessage();
            int behaviorType = (int)bytes[15];
            gpsDriverBehaviorMessage.SerialNo = serialNo;
            //gpsDriverBehaviorMessage.IsNeedResp = isNeedResp;
            gpsDriverBehaviorMessage.Imei = imei;
            gpsDriverBehaviorMessage.BehaviorType = behaviorType;
            gpsDriverBehaviorMessage.OrignBytes = bytes;
            DateTime startDate = Utils.getGTM0Date(bytes, 16);
            gpsDriverBehaviorMessage.StartDate = startDate;
            gpsDriverBehaviorMessage.StartAltitude = BytesUtils.Bytes2Float(bytes, 22);
            gpsDriverBehaviorMessage.StartLongitude = BytesUtils.Bytes2Float(bytes, 26);
            gpsDriverBehaviorMessage.StartLatitude = BytesUtils.Bytes2Float(bytes, 30);
            byte[] bytesSpeed = new byte[2];
            Array.Copy(bytes, 34, bytesSpeed, 0, 2);
            String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
            float speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));
            gpsDriverBehaviorMessage.StartSpeed = speedf;
            int azimuth = BytesUtils.Bytes2Short(bytes, 36);
            gpsDriverBehaviorMessage.StartAzimuth = azimuth;

            int rpm = BytesUtils.Bytes2Short(bytes, 38);
            if (rpm == 65535)
            {
                rpm = -1;
            }
            gpsDriverBehaviorMessage.StartRpm = rpm;


            DateTime endDate = Utils.getGTM0Date(bytes, 40);
            gpsDriverBehaviorMessage.EndDate = endDate;
            gpsDriverBehaviorMessage.EndAltitude = BytesUtils.Bytes2Float(bytes, 46);
            gpsDriverBehaviorMessage.EndLongitude = BytesUtils.Bytes2Float(bytes, 50);
            gpsDriverBehaviorMessage.EndLatitude = BytesUtils.Bytes2Float(bytes, 54);
            Array.Copy(bytes, 58, bytesSpeed, 0, 2);
            strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
            speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));
            gpsDriverBehaviorMessage.EndSpeed = speedf;
            azimuth = BytesUtils.Bytes2Short(bytes, 60);
            gpsDriverBehaviorMessage.EndAzimuth = azimuth;
            rpm = BytesUtils.Bytes2Short(bytes, 62);
            if (rpm == 65535)
            {
                rpm = -1;
            }
            gpsDriverBehaviorMessage.EndRpm = rpm;
            return gpsDriverBehaviorMessage;
        }

        private AccidentAccelerationMessage parseAccelerationAlarmMessage(byte[] bytes)
        {
            int length = BytesUtils.Bytes2Short(bytes, 3);
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            AccidentAccelerationMessage accidentAccelerationMessage = new AccidentAccelerationMessage();

            accidentAccelerationMessage.SerialNo = serialNo;
            //accidentAccelerationMessage.IsNeedResp = isNeedResp;
            accidentAccelerationMessage.Imei = imei;
            accidentAccelerationMessage.OrignBytes = bytes;
            int dataLength = length - 16;
            int dataCount = dataLength / 28;
            int beginIdex = 16;
            List<AccelerationData> accidentAccelerationList = new List<AccelerationData>();
            for (int i = 0; i < dataCount; i++)
            {
                int curParseIndex = beginIdex + i * 30;
                AccelerationData accidentAcceleration = getAccelerationData(bytes, imei, curParseIndex);
                accidentAccelerationList.Add(accidentAcceleration);
            }
            accidentAccelerationMessage.AccelerationList = accidentAccelerationList;
            return accidentAccelerationMessage;
        }

        private AccelerationDriverBehaviorMessage parseAccelerationDriverBehaviorMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            AccelerationDriverBehaviorMessage message = new AccelerationDriverBehaviorMessage();
            int length = BytesUtils.Bytes2Short(bytes, 3);
            int behavior = (int)bytes[15];
            message.SerialNo = serialNo;
            //message.IsNeedResp = isNeedResp;
            message.Imei = imei;
            message.BehaviorType = behavior;
            message.OrignBytes = bytes;

            int beginIdex = 16;
            AccelerationData acceleration = getAccelerationData(bytes, imei, beginIdex);
            message.AccelerationData = acceleration;
            return message;
        }

        private AccelerationData getAccelerationData(byte[] bytes, String imei, int curParseIndex)
        {
            AccelerationData acceleration = new AccelerationData();
            acceleration.Imei = imei;
            acceleration.Date = Utils.getGTM0Date(bytes, curParseIndex);
            bool isGpsWorking = (bytes[curParseIndex + 6] & 0x20) == 0x00;
            bool isHistoryData = (bytes[curParseIndex + 6] & 0x80) != 0x00;
            int satelliteNumber = bytes[curParseIndex + 6] & 0x1F;
            bool latlngValid = (bytes[curParseIndex + 6] & 0x40) != 0x00;
            acceleration.IsHistoryData = isHistoryData;
            acceleration.GpsWorking = isGpsWorking;
            acceleration.SatelliteNumber = satelliteNumber;
            acceleration.LatlngValid = latlngValid;
            int axisXDirect = (bytes[curParseIndex + 7] & 0x80) == 0x80 ? 1 : -1;
            float axisX = ((bytes[curParseIndex + 7] & 0x7F & 0xff) + (((bytes[curParseIndex + 8] & 0xf0) >> 4) & 0xff) / 10.0f) * axisXDirect;
            acceleration.AxisX = axisX;
            int axisYDirect = (bytes[curParseIndex + 8] & 0x08) == 0x08 ? 1 : -1;
            float axisY = (((((bytes[curParseIndex + 8] & 0x07) << 4) & 0xff) + (((bytes[curParseIndex + 9] & 0xf0) >> 4) & 0xff)) + (bytes[curParseIndex + 9] & 0x0F & 0xff) / 10.0f) * axisYDirect;
            acceleration.AxisY = axisY;
            int axisZDirect = (bytes[curParseIndex + 10] & 0x80) == 0x80 ? 1 : -1;
            float axisZ = ((bytes[curParseIndex + 10] & 0x7F & 0xff) + (((bytes[curParseIndex + 11] & 0xf0) >> 4) & 0xff) / 10.0f) * axisZDirect;
            acceleration.AxisZ = axisZ;

            acceleration.Altitude = BytesUtils.Bytes2Float(bytes, curParseIndex + 12);
            acceleration.Longitude = BytesUtils.Bytes2Float(bytes, curParseIndex + 16);
            acceleration.Latitude = BytesUtils.Bytes2Float(bytes, curParseIndex + 20);
            byte[] bytesSpeed = new byte[2];
            Array.Copy(bytes, curParseIndex + 24, bytesSpeed, 0, 2);
            String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
            float speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));
            acceleration.Speed = speedf;
            int azimuth = BytesUtils.Bytes2Short(bytes, curParseIndex + 26);
            acceleration.Azimuth = azimuth;

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
                byte lbsByte = bytes[curParseIndex + 12];
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
                mcc_2g = BytesUtils.Bytes2Short(bytes, curParseIndex + 12);
                mnc_2g = BytesUtils.Bytes2Short(bytes, curParseIndex + 14);
                lac_2g_1 = BytesUtils.Bytes2Short(bytes, curParseIndex + 16);
                ci_2g_1 = BytesUtils.Bytes2Short(bytes, curParseIndex + 18);
                lac_2g_2 = BytesUtils.Bytes2Short(bytes, curParseIndex + 20);
                ci_2g_2 = BytesUtils.Bytes2Short(bytes, curParseIndex + 22);
                lac_2g_3 = BytesUtils.Bytes2Short(bytes, curParseIndex + 24);
                ci_2g_3 = BytesUtils.Bytes2Short(bytes, curParseIndex + 26);
            }
            if (is_4g_lbs)
            {
                mcc_4g = BytesUtils.Bytes2Short(bytes, curParseIndex + 12);
                mnc_4g = BytesUtils.Bytes2Short(bytes, curParseIndex + 14);
                ci_4g = BytesUtils.Byte2Int(bytes, curParseIndex + 16);
                earfcn_4g_1 = BytesUtils.Bytes2Short(bytes, curParseIndex + 20);
                pcid_4g_1 = BytesUtils.Bytes2Short(bytes, curParseIndex + 22);
                earfcn_4g_2 = BytesUtils.Bytes2Short(bytes, curParseIndex + 24);
                pcid_4g_2 = BytesUtils.Bytes2Short(bytes, curParseIndex + 26);
            }
            int rpm = BytesUtils.Bytes2Short(bytes, curParseIndex + 28);
            if (rpm == 65535)
            {
                rpm = -1;
            }
            acceleration.Is_2g_lbs = is_2g_lbs;
            acceleration.Is_4g_lbs = is_4g_lbs;
            acceleration.Mcc_4g = mcc_4g;
            acceleration.Mnc_4g = mnc_4g;
            acceleration.Ci_4g = ci_4g;
            acceleration.Earfcn_4g_1 = earfcn_4g_1;
            acceleration.Pcid_4g_1 = pcid_4g_1;
            acceleration.Earfcn_4g_2 = earfcn_4g_2;
            acceleration.Pcid_4g_2 = pcid_4g_2;
            acceleration.Mcc_2g = mcc_2g;
            acceleration.Mnc_2g = mnc_2g;
            acceleration.Lac_2g_1 = lac_2g_1;
            acceleration.Ci_2g_1 = ci_2g_1;
            acceleration.Lac_2g_2 = lac_2g_2;
            acceleration.Ci_2g_2 = ci_2g_2;
            acceleration.Lac_2g_3 = lac_2g_3;
            acceleration.Ci_2g_3 = ci_2g_3;
            acceleration.Rpm = rpm;
            return acceleration;
        }

        private LocationMessage parseDataMessage(byte[] bytes)
        {
            byte[] command = new byte[HEADER_LENGTH];
            Array.Copy(bytes, 0, command, 0, HEADER_LENGTH);
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            byte[] data = Utils.ArrayCopyOfRange(bytes, 15, bytes.Length);
            int samplingIntervalAccOn = BytesUtils.Bytes2Short(data, 0);
            int samplingIntervalAccOff = BytesUtils.Bytes2Short(data, 2);
            int angleCompensation = (int)data[4];
            int distanceCompensation = BytesUtils.Bytes2Short(data, 5);
            short limit = (short)BytesUtils.Bytes2Short(data, 7);
            int speed = ((0x7F80 & limit)) >> 7;
            if ((0x8000 & limit) != 0)
            {
                speed = (short)(speed * 1.609344);
            }
            int networkSignal = limit & 0x7F;
            bool isGpsWorking = (data[9] & 0x20) == 0x00;
            bool isHistoryData = (data[9] & 0x80) != 0x00;
            int satelliteNumber = data[9] & 0x1F;
            int gSensorSensitivity = (data[10] & 0xF0) >> 4;
            bool isManagerConfigured1 = (data[10] & 0x01) != 0x00;
            bool isManagerConfigured2 = (data[10] & 0x02) != 0x00;
            bool isManagerConfigured3 = (data[10] & 0x04) != 0x00;
            bool isManagerConfigured4 = (data[10] & 0x08) != 0x00;
            int antitheftedStatus = (data[11] & 0x10) != 0x00 ? 1 : 0;
            int heartbeatInterval = data[12] & 0x00FF;
            int relayStatus = data[13] & 0x3F;
            int rlyMode =  data[13] & 0xCF;
            int ignitionSource = data[13] & 0xf; 
            int smsLanguageType = data[13] & 0xF;
            bool isRelayWaiting = ((data[13] & 0xC0) != 0x00) && ((data[13] & 0x80) == 0x00);
            int dragThreshold = BytesUtils.Bytes2Short(data, 14);
            long iop = (long)BytesUtils.Bytes2Short(data, 16);
            bool iopIgnition = (iop & MASK_IGNITION) == MASK_IGNITION;
            bool iopPowerCutOff = (iop & MASK_POWER_CUT) == MASK_POWER_CUT;
            bool iopACOn = (iop & MASK_AC) == MASK_AC;
            int input1 = iopIgnition ? 1 : 0;
            int input2 = iopACOn ? 1 : 0;
            int output1 = (iop & 0x0400) == 0x0400 ? 1 : 0;
            int speakerStatus = (iop & 0x40) ==  0x40  ? 1 : 0;
            int rs232PowerOf5V = (iop & 0x20) ==  0x20  ? 1 : 0;
            int hasThirdPartyObd = (iop & 0x10) == 0x10 ? 1 : 0;
            int exPowerConsumpStatus = 0;
            if ((iop & 0x03) == 0x01)
            {
                exPowerConsumpStatus = 2;
            }
            else if ((iop & 0x03) == 0x02)
            {
                exPowerConsumpStatus = 1;
            }
            else
            {
                exPowerConsumpStatus = 0;
            }
            byte alarmByte = data[18];
            int originalAlarmCode = (int)alarmByte;
            bool isSendSmsAlarmToManagerPhone = (data[19] & 0x20) == 0x20;
            bool isSendSmsAlarmWhenDigitalInput2Change = (data[19] & 0x10) == 0x10;
            int jammerDetectionStatus = (data[19] & 0xC);
            int mileageSource = (data[19] & 0x01) == 0x01 ? 0 : 1;
            bool isAlarmData = command[2] == 0x04 || command[2] == 0x18;
            long mileage = BytesUtils.Byte2Int(data, 20);
            byte[] batteryBytes = new byte[] { data[24] };
            String batteryStr = BytesUtils.Bytes2HexString(batteryBytes, 0);
            DateTime gmt0 = Utils.getGTM0Date(data, 25);
            bool latlngValid = (data[9] & 0x40) != 0x00;
            byte[] latlngData = Utils.ArrayCopyOfRange(data, 31, 47);
            if (Utils.ArrayEquals(latlngData, latlngInvalidData))
            {
                latlngValid = false;
            }
            double altitude = latlngValid ? BytesUtils.Bytes2Float(data, 31) : 0.0;
            double latitude = latlngValid ? BytesUtils.Bytes2Float(data, 39) : 0.0;
            double longitude = latlngValid ? BytesUtils.Bytes2Float(data, 35) : 0.0;
            int azimuth = latlngValid ? BytesUtils.Bytes2Short(data, 45) : 0;
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
                byte lbsByte = data[31];
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
                mcc_2g = BytesUtils.Bytes2Short(data, 31);
                mnc_2g = BytesUtils.Bytes2Short(data, 33);
                lac_2g_1 = BytesUtils.Bytes2Short(data, 35);
                ci_2g_1 = BytesUtils.Bytes2Short(data, 37);
                lac_2g_2 = BytesUtils.Bytes2Short(data, 39);
                ci_2g_2 = BytesUtils.Bytes2Short(data, 41);
                lac_2g_3 = BytesUtils.Bytes2Short(data, 43);
                ci_2g_3 = BytesUtils.Bytes2Short(data, 45);
            }
            if (is_4g_lbs)
            {
                mcc_4g = BytesUtils.Bytes2Short(data, 31);
                mnc_4g = BytesUtils.Bytes2Short(data, 33);
                ci_4g = BytesUtils.Byte2Int(data, 35);
                earfcn_4g_1 = BytesUtils.Bytes2Short(data, 39);
                pcid_4g_1 = BytesUtils.Bytes2Short(data, 41);
                earfcn_4g_2 = BytesUtils.Bytes2Short(data, 43);
                pcid_4g_2 = BytesUtils.Bytes2Short(data, 45);
            }
            float externalPowerVoltage = 0f;
            byte[] externalPowerVoltageBytes = Utils.ArrayCopyOfRange(data, 47, 49);
            String externalPowerVoltageStr = BytesUtils.Bytes2HexString(externalPowerVoltageBytes, 0);
            externalPowerVoltage = (float)Convert.ToDouble(externalPowerVoltageStr) / 100.0f;
            float speedf = 0.0f;
            byte[] bytesSpeed = Utils.ArrayCopyOfRange(data, 49, 51);
            String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
            if (strSp.Contains("f"))
            {
                speedf = -1f;
            }
            else
            {
                speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));
            }

            long accumulatingFuelConsumption = BytesUtils.Byte2Int(data, 51);
            if (accumulatingFuelConsumption == 4294967295L)
            {
                accumulatingFuelConsumption = -999;
            }
            long instantFuelConsumption = BytesUtils.Byte2Int(data, 55);
            if (instantFuelConsumption == 4294967295L)
            {
                instantFuelConsumption = -999;
            }
            int rpm = BytesUtils.Bytes2Short(data, 59);
            if (rpm == 65535)
            {
                rpm = -999;
            }
            int airInput = (int)data[61] < 0 ? (int)data[61] + 256 : (int)data[61];
            if (airInput == 255)
            {
                airInput = -999;
            }
            int airPressure = (int)data[62] < 0 ? (int)data[62] + 256 : (int)data[62];
            if (airPressure == 255)
            {
                airPressure = -999;
            }
            int coolingFluidTemp = (int)data[63] < 0 ? (int)data[63] + 256 : (int)data[63];
            if (coolingFluidTemp == 255)
            {
                coolingFluidTemp = -999;
            }
            else
            {
                coolingFluidTemp = coolingFluidTemp - 40;
            }
            int airInflowTemp = (int)data[64] < 0 ? (int)data[64] + 256 : (int)data[64];
            if (airInflowTemp == 255)
            {
                airInflowTemp = -999;
            }
            else
            {
                airInflowTemp = airInflowTemp - 40;
            }
            int engineLoad = (int)data[65] < 0 ? (int)data[65] + 256 : (int)data[65];
            if (engineLoad == 255)
            {
                engineLoad = -999;
            }
            int throttlePosition = (int)data[66] < 0 ? (int)data[66] + 256 : (int)data[66];
            if (throttlePosition == 255)
            {
                throttlePosition = -999;
            }
            int remainFuelRate = data[67] & 0x7f;
            int remainFuelUnit = (data[67] & 0x80) == 0x80 ? 1 : 0;
            LocationMessage message;
            if (isAlarmData)
            {
                message = new LocationAlarmMessage();
            }
            else
            {
                message = new LocationInfoMessage();
            }
            int protocolHead = bytes[2];
            if ((protocolHead == 0x16 || protocolHead == 0x18) && data.Length >= 80){
                byte[] axisXByte = Utils.ArrayCopyOfRange(data,68,70);
                int axisX = BytesUtils.bytes2SingleShort(axisXByte,0); 
                byte[] axisYByte = Utils.ArrayCopyOfRange(data,70,72);
                int axisY = BytesUtils.bytes2SingleShort(axisYByte,0); 
                byte[] axisZByte= Utils.ArrayCopyOfRange(data,72,74);
                int axisZ  = BytesUtils.bytes2SingleShort(axisZByte,0);  
                byte[] gyroscopeAxisXByte = Utils.ArrayCopyOfRange(data,74,76);
                int gyroscopeAxisX = BytesUtils.bytes2SingleShort(gyroscopeAxisXByte,0); 
                byte[] gyroscopeAxisYByte = Utils.ArrayCopyOfRange(data,76,78);
                int gyroscopeAxisY = BytesUtils.bytes2SingleShort(gyroscopeAxisYByte,0); 
                byte[] gyroscopeAxisZByte = Utils.ArrayCopyOfRange(data, 78, 80);
                int gyroscopeAxisZ = BytesUtils.bytes2SingleShort(gyroscopeAxisZByte,0);  
                message.AxisX = axisX;
                message.AxisY = axisY;
                message.AxisZ = axisZ;
                message.GyroscopeAxisX = gyroscopeAxisX;
                message.GyroscopeAxisY = gyroscopeAxisY;
                message.GyroscopeAxisZ = gyroscopeAxisZ;
            }
            message.ProtocolHeadType = protocolHead; 
            message.OrignBytes = bytes;
            message.SerialNo = serialNo;
            //message.IsNeedResp = isNeedResp;
            message.Imei = imei;
            message.Input1 = input1;
            message.Input2 = input2;
            message.Output1 = output1;
            message.NetworkSignal = networkSignal;
            message.SamplingIntervalAccOn = samplingIntervalAccOn;
            message.SamplingIntervalAccOff = samplingIntervalAccOff;
            message.AngleCompensation = angleCompensation;
            message.DistanceCompensation = distanceCompensation;
            message.OverspeedLimit = (int)speed;
            message.GpsWorking = isGpsWorking;
            message.IsHistoryData = isHistoryData;
            message.SatelliteNumber = satelliteNumber;
            message.GSensorSensitivity = gSensorSensitivity;
            message.IsManagerConfigured1 = isManagerConfigured1;
            message.IsManagerConfigured2 = isManagerConfigured2;
            message.IsManagerConfigured3 = isManagerConfigured3;
            message.IsManagerConfigured4 = isManagerConfigured4;
            message.AntitheftedStatus = antitheftedStatus;
            message.HeartbeatInterval = heartbeatInterval;
            message.RelayStatus = relayStatus;
            message.RlyMode = rlyMode;
            message.SmsLanguageType = smsLanguageType;
            message.IsRelayWaiting = isRelayWaiting;
            message.DragThreshold = dragThreshold;
            message.IOP = iop;
            message.IopIgnition = iopIgnition;
            message.IopPowerCutOff = iopPowerCutOff;
            message.SpeakerStatus = speakerStatus;
            message.Rs232PowerOf5V = rs232PowerOf5V;
            message.IopACOn = iopACOn;
            message.OriginalAlarmCode = originalAlarmCode;
            message.Mileage = mileage;
            try
            {
                int charge = Convert.ToInt32(batteryStr);
                if (0 == charge)
                {
                    charge = 100;
                }
                message.BatteryCharge = charge;
            }
            catch (Exception e)
            {
                throw new Exception("parse battery error !!! imei :" + imei + "error str :" + batteryStr);
            }
            message.Date = gmt0;
            message.LatlngValid = latlngValid;
            message.Altitude = altitude;
            message.Latitude = latitude;
            message.Longitude = longitude;
            if (message.LatlngValid)
            {
                message.Speed = speedf;
            }
            else
            {
                message.Speed = 0.0f;
            }
            message.Azimuth = azimuth;
            message.ExternalPowerVoltage = externalPowerVoltage;
            message.AccumulatingFuelConsumption = accumulatingFuelConsumption;
            message.InstantFuelConsumption = instantFuelConsumption;
            message.Rpm = rpm;
            message.AirInflowTemp = airInflowTemp;
            message.AirInput = airInput;
            message.AirPressure = airPressure;
            message.CoolingFluidTemp = coolingFluidTemp;
            message.EngineLoad = engineLoad;
            message.ThrottlePosition = throttlePosition;
            message.RemainFuelRate = remainFuelRate;
            message.Azimuth = azimuth;
            message.ExternalPowerVoltage = externalPowerVoltage;
            message.Is_2g_lbs = is_2g_lbs;
            message.Is_4g_lbs = is_4g_lbs;
            message.Mcc_4g = mcc_4g;
            message.Mnc_4g = mnc_4g;
            message.Ci_4g = ci_4g;
            message.Earfcn_4g_1 = earfcn_4g_1;
            message.Pcid_4g_1 = pcid_4g_1;
            message.Earfcn_4g_2 = earfcn_4g_2;
            message.Pcid_4g_2 = pcid_4g_2;
            message.Mcc_2g = mcc_2g;
            message.Mnc_2g = mnc_2g;
            message.Lac_2g_1 = lac_2g_1;
            message.Ci_2g_1 = ci_2g_1;
            message.Lac_2g_2 = lac_2g_2;
            message.Ci_2g_2 = ci_2g_2;
            message.Lac_2g_3 = lac_2g_3;
            message.Ci_2g_3 = ci_2g_3;
            message.IsSendSmsAlarmWhenDigitalInput2Change = isSendSmsAlarmWhenDigitalInput2Change;
            message.IsSendSmsAlarmToManagerPhone = isSendSmsAlarmToManagerPhone;
            message.JammerDetectionStatus = jammerDetectionStatus;
            message.IgnitionSource = ignitionSource;
            message.HasThirdPartyObd = hasThirdPartyObd;
            message.ExPowerConsumpStatus = exPowerConsumpStatus;
            message.RemainFuelUnit = remainFuelUnit;
            message.MileageSource = mileageSource;
            return message;
        }
    }
}
