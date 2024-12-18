﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using TopflytechCodec.Entities;
using System.IO;

namespace TopflytechCodec
{
    /// <summary>
    /// Topflytech all device Decoder
    /// </summary>
    public class Decoder
    {

        private const int HEADER_LENGTH = 3;


        private static byte[] SIGNUP = { 0x23, 0x23, 0x01 };

        private static byte[] DATA = { 0x23, 0x23, 0x02 };

        private static byte[] HEARTBEAT = { 0x23, 0x23, 0x03 };

        private static byte[] ALARM = { 0x23, 0x23, 0x04 };

        private static byte[] CONFIG = { 0x23, 0x23, (byte)0x81 };

        private static byte[] SIGNUP_880XPlUS = { 0x25, 0x25, 0x01 };

        private static byte[] DATA_880XPlUS = { 0x25, 0x25, 0x02 };

        private static byte[] HEARTBEAT_880XPlUS = { 0x25, 0x25, 0x03 };

        private static byte[] ALARM_880XPlUS = { 0x25, 0x25, 0x04 };

        private static byte[] CONFIG_880XPlUS = { 0x25, 0x25, (byte)0x81 };

        private static byte[] GPS_DRIVER_BEHAVIOR = { 0x25, 0x25, (byte)0x05 };
        private static byte[] ACCELERATION_DRIVER_BEHAVIOR = { 0x25, 0x25, (byte)0x06 };
        private static byte[] ACCELERATION_ALARM = { 0x25, 0x25, (byte)0x07 };
        private static byte[] BLUETOOTH_MAC = { 0x25, 0x25, (byte)0x08 };
        private static byte[] RS232 = { 0x25, 0x25, (byte)0x09 };
        private static byte[] BLUETOOTH_DATA = { 0x25, 0x25, (byte)0x10 };
        private static byte[] NETWORK_INFO_DATA = { 0x25, 0x25, (byte)0x11 };
        private static byte[] BLUETOOTH_SECOND_DATA = { 0x25, 0x25, (byte)0x12 };
        private static byte[] LOCATION_SECOND_DATA = { 0x25, 0x25, (byte)0x13 };
        private static byte[] ALARM_SECOND_DATA = { 0x25, 0x25, (byte)0x14 };
        private static byte[] LOCATION_DATA_WITH_SENSOR =  {0x25, 0x25, (byte)0x16};
        private static byte[] LOCATION_ALARM_WITH_SENSOR =  {0x25, 0x25, (byte)0x18};
        private static byte[] latlngInvalidData = {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
                                                     (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};

        private static byte[] rs232TireHead = { (byte)0x00, (byte)0x01 };
        private static byte[] rs232RfidHead = { (byte)0x00, (byte)0x03 };
        private static byte[] rs232FingerprintHead = { (byte)0x00, (byte)0x02, (byte)0x6C, (byte)0x62, (byte)0x63 };
        private static byte[] rs232CapacitorFuelHead = { (byte)0x00, (byte)0x04 };
        private static byte[] rs232UltrasonicFuelHead = { (byte)0x00, (byte)0x05 };
        private static byte[] WIFI_DATA = { 0x25, 0x25, (byte)0x15 };
        private static byte[] RS485_DATA = { 0x25, 0x25, (byte)0x21 };
        private static byte[] OBD_DATA = { 0x25, 0x25, (byte)0x22 };
        private static byte[] ONE_WIRE_DATA = { 0x25, 0x25, (byte)0x23 };
        private static byte[] LOCATION_SECOND_DATA_WITH_SENSOR = { 0x25, 0x25, (byte)0x33 };
        private static byte[] ALARM_SECOND_DATA_WITH_SENSOR = { 0x25, 0x25, (byte)0x34 };
        private static byte[] obdHead = { 0x55, (byte)0xAA };
        private static bool match(byte[] bytes)
        {
            return Utils.ArrayEquals(SIGNUP, bytes)
                || Utils.ArrayEquals(HEARTBEAT, bytes)
                || Utils.ArrayEquals(DATA, bytes)
                || Utils.ArrayEquals(ALARM, bytes)
                || Utils.ArrayEquals(CONFIG, bytes)
                || Utils.ArrayEquals(SIGNUP_880XPlUS, bytes)
                || Utils.ArrayEquals(HEARTBEAT_880XPlUS, bytes)
                || Utils.ArrayEquals(DATA_880XPlUS, bytes)
                || Utils.ArrayEquals(ALARM_880XPlUS, bytes)
                || Utils.ArrayEquals(CONFIG_880XPlUS, bytes)
                || Utils.ArrayEquals(GPS_DRIVER_BEHAVIOR, bytes)
                || Utils.ArrayEquals(ACCELERATION_DRIVER_BEHAVIOR, bytes)
                || Utils.ArrayEquals(ACCELERATION_ALARM, bytes)
                || Utils.ArrayEquals(BLUETOOTH_MAC, bytes)
                || Utils.ArrayEquals(BLUETOOTH_DATA, bytes)
                || Utils.ArrayEquals(NETWORK_INFO_DATA, bytes)
                || Utils.ArrayEquals(BLUETOOTH_SECOND_DATA, bytes)
                || Utils.ArrayEquals(LOCATION_SECOND_DATA, bytes)
                || Utils.ArrayEquals(ALARM_SECOND_DATA, bytes)
                || Utils.ArrayEquals(RS232, bytes)
                || Utils.ArrayEquals(LOCATION_DATA_WITH_SENSOR, bytes)
                || Utils.ArrayEquals(WIFI_DATA, bytes)
                || Utils.ArrayEquals(RS485_DATA, bytes)
                || Utils.ArrayEquals(OBD_DATA, bytes)
                || Utils.ArrayEquals(ONE_WIRE_DATA, bytes)
                || Utils.ArrayEquals(LOCATION_ALARM_WITH_SENSOR, bytes)
                || Utils.ArrayEquals(LOCATION_SECOND_DATA_WITH_SENSOR, bytes)
                || Utils.ArrayEquals(ALARM_SECOND_DATA_WITH_SENSOR, bytes);
        }





        private int encryptType = 0;
        private string aesKey;

        private static long MASK_IGNITION = 0x4000;
        private static long MASK_POWER_CUT = 0x8000;
        private static long MASK_AC = 0x2000;
        private static long IOP_RS232_DEVICE_VALID = 0x20;
        /// <summary>
        /// Instantiates a new Decoder.
        /// </summary>
        /// <param name="messageEncryptType">The message encrypt type .Use the value of MessageEncryptType</param>
        /// <param name="aesKey">The aes key.If you do not use AES encryption, the value can be empty.</param>
        public Decoder(int messageEncryptType, String aesKey)
        {
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;

        }
        public Decoder(int messageEncryptType, String aesKey, int buffSize)
        { 
            this.encryptType = messageEncryptType;
            this.aesKey = aesKey;
            this.decoderBuf = new TopflytechByteBuf(buffSize);
        }


        private TopflytechByteBuf decoderBuf = new TopflytechByteBuf();

        /// <summary>
        /// Docode list. You can get all message at once.
        /// </summary>
        /// <param name="buf">The buf</param>
        /// <returns>Return the list</returns> 
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
                    && ((bytes[0] == 0x23 && bytes[1] == 0x23) || (bytes[0] == 0x25 && bytes[1] == 0x25)))
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
                        RS232Message rs232Message = parseRS232Message(bytes);
                        return rs232Message;
                    case 0x10:
                        BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = parseBluetoothDataMessage(bytes);
                        return bluetoothPeripheralDataMessage;
                    case 0x11:
                        NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                        return networkInfoMessage;
                    case 0x12:
                        BluetoothPeripheralDataMessage bluetoothPeripheralDataSecondMessage = parseSecondBluetoothDataMessage(bytes);
                        return bluetoothPeripheralDataSecondMessage;
                    case 0x13:
                    case 0x14:
                    case 0x33:
                    case 0x34:
                        LocationMessage locationSecondMessage = parseSecondDataMessage(bytes);
                        return locationSecondMessage;
                    case 0x15:
                        WifiMessage wifiMessage = parseWifiMessage(bytes);
                        return wifiMessage;
                    case 0x21:
                        RS485Message rs485Message = parseRs485Message(bytes);
                        return rs485Message;
                    case 0x22:
                        ObdMessage obdMessage = parseObdMessage(bytes);
                        return obdMessage;
                    case 0x23:
                        OneWireMessage oneWireMessage = parseOneWireMessage(bytes);
                        return oneWireMessage;
                    case (byte)0x81:
                        Message message = parseInteractMessage(bytes);
                        return message;
                    default:
                        throw new Exception("The message type error!");
                }
            }
            return null;
        }

        private OneWireMessage parseOneWireMessage(byte[] bytes)
        {
            OneWireMessage oneWireMessage = new OneWireMessage();
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            DateTime gmt0 = Utils.getGTM0Date(bytes, 15);
            byte reverse = bytes[21];
            bool isIgnition = (bytes[22] & 0x01) == 0x01; 
            oneWireMessage.OrignBytes = bytes;
            oneWireMessage.Imei = imei;
            oneWireMessage.SerialNo = serialNo;
            oneWireMessage.Date = gmt0; 
            oneWireMessage.IsIgnition = isIgnition;

            bool latlngValid = (bytes[23] & 0x40) != 0x00;
            bool isGpsWorking = (bytes[23] & 0x20) == 0x00;
            bool isHistoryData = (bytes[23] & 0x80) != 0x00;
            int satelliteNumber = bytes[23] & 0x1F;


            byte[] latlngData = Utils.ArrayCopyOfRange(bytes, 24, 40);
            if (Utils.ArrayEquals(latlngData, latlngInvalidData))
            {
                latlngValid = false;
            }
            double altitude = latlngValid ? BytesUtils.Bytes2Float(bytes, 24) : 0.0;
            double latitude = latlngValid ? BytesUtils.Bytes2Float(bytes, 32) : 0.0;
            double longitude = latlngValid ? BytesUtils.Bytes2Float(bytes, 28) : 0.0;
            int azimuth = latlngValid ? BytesUtils.Bytes2Short(bytes, 38) : 0; 
            float speedf = 0.0f;
            byte[] bytesSpeed = Utils.ArrayCopyOfRange(bytes, 36, 38); ;
            String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
            if (strSp.Contains("f"))
            {
                speedf = -999f;
            }
            else
            {
                try
                {
                    speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));

                }
                catch (Exception e)
                {

                }
            }

            Boolean is_4g_lbs = false;
            Int32 mcc_4g = -1;
            Int32 mnc_4g = -1;
            Int64 eci_4g = -1;
            Int32 tac = -1;
            Int32 pcid_4g_1 = -1;
            Int32 pcid_4g_2 = -1;
            Int32 pcid_4g_3 = -1;
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
                byte lbsByte = bytes[24];
                if ((lbsByte & 0x80) == 0x80)
                {
                    is_4g_lbs = true;
                }
                else
                {
                    is_2g_lbs = true;
                }
            }
            if (is_2g_lbs)
            {
                mcc_2g = BytesUtils.Bytes2Short(bytes, 24);
                mnc_2g = BytesUtils.Bytes2Short(bytes, 26);
                lac_2g_1 = BytesUtils.Bytes2Short(bytes, 28);
                ci_2g_1 = BytesUtils.Bytes2Short(bytes, 30);
                lac_2g_2 = BytesUtils.Bytes2Short(bytes, 32);
                ci_2g_2 = BytesUtils.Bytes2Short(bytes, 34);
                lac_2g_3 = BytesUtils.Bytes2Short(bytes, 36);
                ci_2g_3 = BytesUtils.Bytes2Short(bytes, 38);
            }
            if (is_4g_lbs)
            {
                mcc_4g = BytesUtils.Bytes2Short(bytes, 24) & 0x7FFF;
                mnc_4g = BytesUtils.Bytes2Short(bytes, 26);
                eci_4g = BytesUtils.Byte2Int(bytes, 28);
                tac = BytesUtils.Bytes2Short(bytes, 32);
                pcid_4g_1 = BytesUtils.Bytes2Short(bytes, 34);
                pcid_4g_2 = BytesUtils.Bytes2Short(bytes, 36);
                pcid_4g_3 = BytesUtils.Bytes2Short(bytes, 38);
            }
            int positionIndex = 40;
            List<OneWireData> oneWireDataArrayList = new List<OneWireData>();
            while (positionIndex + 10 <= bytes.Length)
            {
                byte deviceType = bytes[positionIndex];
                positionIndex++;
                byte[] deviceIdByte = Utils.ArrayCopyOfRange(bytes, positionIndex, positionIndex + 8);
                String deviceId = BytesUtils.Bytes2HexString(deviceIdByte, 0);
                positionIndex += 8;
                int dataLen = bytes[positionIndex];
                if (dataLen < 0)
                {
                    dataLen += 256;
                }
                positionIndex++;
                if (positionIndex + dataLen > bytes.Length)
                {
                    break;
                }
                byte[] oneWireByte = Utils.ArrayCopyOfRange(bytes, positionIndex, positionIndex + dataLen);
                OneWireData oneWireData = new OneWireData();
                oneWireData.DeviceId= deviceId;
                oneWireData.DeviceType = deviceType ;
                oneWireData.OneWireContent = oneWireByte;
                positionIndex += dataLen;
                oneWireDataArrayList.Add(oneWireData);
            } 
            oneWireMessage.OneWireDataList = oneWireDataArrayList;
            oneWireMessage.GpsWorking = isGpsWorking;
            oneWireMessage.IsHistoryData = isHistoryData;
            oneWireMessage.SatelliteNumber = satelliteNumber;
            oneWireMessage.LatlngValid = latlngValid;
            oneWireMessage.Altitude = altitude;
            oneWireMessage.Latitude = latitude;
            oneWireMessage.Longitude = longitude;
            if (oneWireMessage.LatlngValid)
            {
                oneWireMessage.Speed = speedf;
            }
            else
            {
                oneWireMessage.Speed = 0.0f;
            }
            oneWireMessage.Azimuth = azimuth;
            oneWireMessage.Is_2g_lbs = is_2g_lbs;
            oneWireMessage.Is_4g_lbs = is_4g_lbs;
            oneWireMessage.Mcc_4g = mcc_4g;
            oneWireMessage.Mnc_4g = mnc_4g;
            oneWireMessage.Eci_4g = eci_4g;
            oneWireMessage.TAC = tac;
            oneWireMessage.Pcid_4g_1 = pcid_4g_1;
            oneWireMessage.Pcid_4g_2 = pcid_4g_2;
            oneWireMessage.Pcid_4g_3 = pcid_4g_3;
            oneWireMessage.Mcc_2g = mcc_2g;
            oneWireMessage.Mnc_2g = mnc_2g;
            oneWireMessage.Lac_2g_1 = lac_2g_1;
            oneWireMessage.Ci_2g_1 = ci_2g_1;
            oneWireMessage.Lac_2g_2 = lac_2g_2;
            oneWireMessage.Ci_2g_2 = ci_2g_2;
            oneWireMessage.Lac_2g_3 = lac_2g_3;
            oneWireMessage.Ci_2g_3 = ci_2g_3;
            return oneWireMessage;
        }

        private RS485Message parseRs485Message(byte[] bytes)
        {
            RS485Message rs485Message = new RS485Message();
            int serialNo = BytesUtils.Bytes2Short(bytes, 5); 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            DateTime gmt0 = Utils.getGTM0Date(bytes, 15);
            bool isIgnition = (bytes[21] & 0x01) == 0x01;
            int deviceId = bytes[22];
            byte[] rs485Data = Utils.ArrayCopyOfRange(bytes, 23, bytes.Length);
            rs485Message.OrignBytes = bytes;
            rs485Message.Imei = imei;
            rs485Message.SerialNo = serialNo;
            rs485Message.Date = gmt0; 
            rs485Message.DeviceId = deviceId;
            rs485Message.IsIgnition = isIgnition;
            rs485Message.Rs485Data = rs485Data;
            return rs485Message;
        }

        private WifiMessage parseWifiMessage(byte[] bytes)
        {
            WifiMessage wifiMessage = new WifiMessage();
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            DateTime gmt0 = Utils.getGTM0Date(bytes, 15);
            String selfMac = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 21, 27), 0);
            String ap1Mac = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 27, 33), 0);
            int ap1Rssi;
            int rssiTemp = (int)bytes[33] < 0 ? (int)bytes[33] + 256 : (int)bytes[33];
            if (rssiTemp == 255)
            {
                ap1Rssi = -999;
            }
            else
            {
                ap1Rssi = rssiTemp - 256;
            }
            String ap2Mac = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 34, 40), 0);
            int ap2Rssi;
            rssiTemp = (int)bytes[40] < 0 ? (int)bytes[40] + 256 : (int)bytes[40];
            if (rssiTemp == 255)
            {
                ap2Rssi = -999;
            }
            else
            {
                ap2Rssi = rssiTemp - 256;
            }
            String ap3Mac = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 41, 47), 0);
            int ap3Rssi;
            rssiTemp = (int)bytes[47] < 0 ? (int)bytes[47] + 256 : (int)bytes[47];
            if (rssiTemp == 255)
            {
                ap3Rssi = -999;
            }
            else
            {
                ap3Rssi = rssiTemp - 256;
            }
            wifiMessage.OrignBytes = bytes;
            wifiMessage.Imei = imei;
            wifiMessage.SerialNo = serialNo;
            wifiMessage.Date = gmt0;
            wifiMessage.SelfMac = selfMac.ToUpper();
            wifiMessage.Ap1Mac = ap1Mac.ToUpper();
            wifiMessage.Ap1RSSI = ap1Rssi;
            wifiMessage.Ap2Mac = ap2Mac.ToUpper();
            wifiMessage.Ap2RSSI = ap2Rssi;
            wifiMessage.Ap3Mac = ap3Mac.ToUpper();
            wifiMessage.Ap3RSSI = ap3Rssi;
            return wifiMessage;
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
                        if (data[0] == 0x41 && data[1] == 0x04 && data.Length > 3)
                        {
                            obdData.MessageType = ObdMessage.CLEAR_ERROR_CODE_MESSAGE;
                            obdData.ClearErrorCodeSuccess = data[2] == 0x01;
                        }
                        else if (data[0] == 0x41 && data[1] == 0x05 && data.Length > 2)
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
                        else if (data[0] == 0x41 && (data[1] == 0x03 || data[1] == 0x0A))
                        {
                            int errorCode = data[2];
                            byte[] errorDataByte = Utils.ArrayCopyOfRange(data, 3, data.Length - 1);
                            String errorDataStr = BytesUtils.Bytes2HexString(errorDataByte, 0);
                            if (errorDataStr != null)
                            {
                                String errorDataSum = "";
                                for (int i = 0; i + 6 <= errorDataStr.Length; i += 6)
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
            String networkOperator = Encoding.GetEncoding("UTF-16le").GetString(networkOperatorByte);
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

        private SignInMessage parseLoginMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            String str = BytesUtils.Bytes2HexString(bytes, 15);
            if (12 == str.Length)
            {
                char[] strChars = str.ToCharArray();
                String software = String.Format("V{0}.{1}.{2}", Convert.ToInt32(str.Substring(0, 1), 16), Convert.ToInt32(str.Substring(1, 1), 16), Convert.ToInt32(str.Substring(2, 1), 16));
                String firmware = String.Format("V{0}.{1}.{2}", Convert.ToInt32(str.Substring(3, 1), 16), Convert.ToInt32(str.Substring(4, 1), 16), Convert.ToInt32(str.Substring(5, 1), 16));
                String platform = str.Substring(6, 4);
                String hardware = String.Format("{0}.{1}", Convert.ToInt32(str.Substring(10, 1), 16), Convert.ToInt32(str.Substring(11, 1), 16));
                SignInMessage signInMessage = new SignInMessage();
                signInMessage.SerialNo = serialNo;
                //signInMessage.IsNeedResp = isNeedResp;
                signInMessage.Imei = imei;
                signInMessage.Software = software;
                signInMessage.Firmware = firmware;
                signInMessage.Platform = platform;
                signInMessage.Hareware = hardware;
                signInMessage.OrignBytes = bytes;
                return signInMessage;
            }
            else if (16 == str.Length)
            {
                String software = String.Format("V{1}.{2}.{3}", Convert.ToInt32(str.Substring(1, 1), 16), Convert.ToInt32(str.Substring(2, 1), 16), Convert.ToInt32(str.Substring(3, 1), 16));
                String firmware = String.Format("V{0}.{1}.{2}.{3}", Convert.ToInt32(str.Substring(10, 1), 16), Convert.ToInt32(str.Substring(11, 1), 16), Convert.ToInt32(str.Substring(12, 1), 16), Convert.ToInt32(str.Substring(13, 1), 16));
                String hardware = String.Format("{0}.{1}", Convert.ToInt32(str.Substring(14, 1), 16), Convert.ToInt32(str.Substring(15, 1), 16));
                SignInMessage signInMessage = new SignInMessage();
                signInMessage.SerialNo = serialNo;
                //signInMessage.IsNeedResp = isNeedResp;
                signInMessage.Imei = imei;
                signInMessage.Software = software;
                signInMessage.Firmware = firmware;
                signInMessage.Hareware = hardware;
                signInMessage.OrignBytes = bytes;
                return signInMessage;
            }
            else if (33 == bytes.Length)
            {
                String software = String.Format("V{1}.{2}.{3}", Convert.ToInt32(str.Substring(1, 1), 16), Convert.ToInt32(str.Substring(2, 1), 16), Convert.ToInt32(str.Substring(3, 1), 16));
                String firmware = String.Format("V{0}.{1}.{2}.{3}", Convert.ToInt32(str.Substring(10, 1), 16), Convert.ToInt32(str.Substring(11, 1), 16), Convert.ToInt32(str.Substring(12, 1), 16), Convert.ToInt32(str.Substring(13, 1), 16));
                String hardware = String.Format("{0}.{1}", Convert.ToInt32(str.Substring(14, 1), 16), Convert.ToInt32(str.Substring(15, 1), 16));
                String obdHardware = String.Format("{0}.{1}", Convert.ToInt32(str.Substring(15, 1), 16), Convert.ToInt32(str.Substring(16, 1), 16));
                String obdSoftware = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 24, 27), 0);
                if (!obdSoftware.ToLower().Equals("ffffff"))
                {
                    obdSoftware = String.Format("V%d.%d.%d", Convert.ToInt32(firmware.Substring(0, 2)), Convert.ToInt32(firmware.Substring(2, 2)), Convert.ToInt32(firmware.Substring(4, 2)));
                }
                else
                {
                    obdSoftware = null;
                }
                String obdBootSoftware = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 27, 30), 0);
                if (!obdBootSoftware.ToLower().Equals("ffffff"))
                {
                    obdBootSoftware = String.Format("V%d.%d.%d", Convert.ToInt32(obdBootSoftware.Substring(0, 2)), Convert.ToInt32(obdBootSoftware.Substring(2, 4)), Convert.ToInt32(obdBootSoftware.Substring(4, 6)));
                }
                else
                {
                    obdBootSoftware = null;
                }
                String obdDataSoftware = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(bytes, 30, 33), 0);
                if (!obdDataSoftware.ToLower().Equals("ffffff"))
                {
                    obdDataSoftware = String.Format("V%d.%d.%d", Convert.ToInt32(obdDataSoftware.Substring(0, 2)), Convert.ToInt32(obdDataSoftware.Substring(2, 4)), Convert.ToInt32(obdDataSoftware.Substring(4, 6)));
                }
                else
                {
                    obdDataSoftware = null;
                }
                SignInMessage signInMessage = new SignInMessage();
                signInMessage.SerialNo = serialNo;
                //signInMessage.IsNeedResp = isNeedResp;
                signInMessage.Imei = imei;
                signInMessage.Software = software;
                signInMessage.Firmware = firmware;
                signInMessage.Hareware = hardware;
                signInMessage.OrignBytes = bytes;
                signInMessage.ObdHareware = obdHardware;
                if (obdSoftware != null)
                {
                    signInMessage.ObdSoftware = obdSoftware;
                }
                if (obdBootSoftware != null)
                {
                    signInMessage.ObdBootVersion = obdBootSoftware;
                }
                if (obdDataSoftware != null)
                {
                    signInMessage.ObdDataVersion = obdDataSoftware;
                }
                return signInMessage;
            }
            else
            {
                throw new Exception("Error login message");
            }
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
            Boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
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
                        configMessage.Imei = imei;
                        //configMessage.IsNeedResp = isNeedResp;
                        return configMessage;
                    }
                case 0x03:
                    {
                        ForwardMessage forwardMessage = new ForwardMessage();
                        messageData = Encoding.GetEncoding("UTF-16LE").GetString(data);
                        forwardMessage.Content = messageData;
                        forwardMessage.OrignBytes = bytes;
                        forwardMessage.SerialNo = serialNo;
                        forwardMessage.Imei = imei;
                        //forwardMessage.IsNeedResp = isNeedResp;
                        return forwardMessage;
                    }
                case 0x05:
                    {
                        USSDMessage ussdMessage = new USSDMessage();
                        messageData = Encoding.GetEncoding("UTF-16lE").GetString(data);
                        ussdMessage.Content = messageData;
                        ussdMessage.OrignBytes = bytes;
                        ussdMessage.SerialNo = serialNo;
                        ussdMessage.Imei = imei;
                        //ussdMessage.IsNeedResp = isNeedResp;
                        return ussdMessage;
                    }
                default:
                    throw new Exception("Error config message");
            }

        }

        private RS232Message parseRS232Message(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            DateTime date = Utils.getGTM0Date(bytes, 15);
            bool isIgnition = (bytes[21] == 0x01);
            byte[] data = new byte[bytes.Length - 22];
            Array.Copy(bytes, 22, data, 0, bytes.Length - 22);
            RS232Message rs232Message = new RS232Message();
            rs232Message.Imei = imei;
            rs232Message.SerialNo = serialNo;
            //rs232Message.IsNeedResp = isNeedResp;
            rs232Message.OrignBytes = bytes;
            rs232Message.IsIgnition = isIgnition;
            rs232Message.Date = date;
            if (data.Length < 2)
            {
                return rs232Message;
            }
            byte[] rs232Head = { data[0], data[1] };
            byte[] fingerprintHead = null;
            if (data.Length > 4)
            {
                fingerprintHead = new byte[] { data[0], data[1], data[2], data[3], data[4] };
            }
            List<Rs232DeviceMessage> messageList = new List<Rs232DeviceMessage>();
            if (Utils.ArrayEquals(rs232Head, rs232TireHead))
            {
                rs232Message.Rs232DataType = RS232Message.TIRE_DATA;
                int dataCount = (data.Length - 2) / 7;
                int curIndex = 2;
                for (int i = 0; i < dataCount; i++)
                {
                    curIndex = i * 7 + 2;
                    Rs232TireMessage rs232TireMessage = new Rs232TireMessage();
                    int airPressureTmp = (int)data[curIndex + 4];
                    if (airPressureTmp < 0)
                    {
                        airPressureTmp += 256;
                    }
                    double airPressure = airPressureTmp * 1.572 * 2;
                    rs232TireMessage.AirPressure = airPressure;
                    int airTempTmp = (int)data[curIndex + 5];
                    if (airTempTmp < 0)
                    {
                        airTempTmp += 256;
                    }
                    int airTemp = airTempTmp - 55;
                    rs232TireMessage.AirTemp = airTemp;
                    int statusTmp = (int)data[curIndex + 6];
                    if (statusTmp < 0)
                    {
                        statusTmp += 256;
                    }
                    rs232TireMessage.Status = statusTmp;
                    int voltageTmp = (int)data[curIndex + 3];
                    if (voltageTmp < 0)
                    {
                        voltageTmp += 256;
                    }
                    double voltage = 0.01 * voltageTmp + 1.22;
                    rs232TireMessage.Voltage = voltage;
                    byte[] sensorIdByte = { data[curIndex], data[curIndex + 1], data[curIndex + 2] };
                    rs232TireMessage.SensorId = BytesUtils.Bytes2HexString(sensorIdByte, 0);
                    messageList.Add(rs232TireMessage);
                }
            }
            else if (Utils.ArrayEquals(rs232Head, rs232RfidHead))
            {
                rs232Message.Rs232DataType = RS232Message.RDID_DATA;
                int dataCount = data.Length / 10;
                int curIndex = 0;
                for (int i = 0; i < dataCount; i++)
                {
                    curIndex = i * 10;
                    Rs232RfidMessage rs232RfidMessage = new Rs232RfidMessage();
                    byte[] rfidByte = {data[curIndex + 2],data[curIndex + 3],data[curIndex + 4],data[curIndex + 5],
                        data[curIndex + 6],data[curIndex + 7],data[curIndex + 8],data[curIndex + 9]};
                    rs232RfidMessage.Rfid = System.Text.Encoding.UTF8.GetString(rfidByte);
                    messageList.Add(rs232RfidMessage);
                }
            }
            else if (fingerprintHead != null && Utils.ArrayEquals(fingerprintHead, rs232FingerprintHead))
            {
                rs232Message.Rs232DataType = RS232Message.FINGERPRINT_DATA;
                Rs232FingerprintMessage rs232FingerprintMessage = new Rs232FingerprintMessage();
                if (data[6] == 0x00)
                {
                    rs232FingerprintMessage.Status = Rs232FingerprintMessage.FINGERPRITN_MSG_STATUS_SUCC;
                }
                else
                {
                    rs232FingerprintMessage.Status = Rs232FingerprintMessage.FINGERPRINT_MSG_STATUS_ERROR;
                }
                if (data[5] == 0x25)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_CLOUND_REGISTER;
                    int dataIndex = (int)data[8];
                    rs232FingerprintMessage.FingerprintDataIndex = dataIndex;
                    int fingerprintId = (int)data[7];
                    if (fingerprintId < 0)
                    {
                        fingerprintId += 256;
                    }
                    rs232FingerprintMessage.FingerprintId = fingerprintId;
                    if (data.Length > 10)
                    {
                        rs232FingerprintMessage.Data = BytesUtils.Bytes2HexString(data, 10);
                    }
                }
                else if (data[5] == 0x71)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_PATCH;
                    int fingerprintId = (int)data[7];
                    if (fingerprintId < 0)
                    {
                        fingerprintId += 256;
                    }
                    rs232FingerprintMessage.FingerprintId = fingerprintId;
                }
                else if (data[5] == 0x73)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_DELETE;
                    int fingerprintId = (int)data[7];
                    if (fingerprintId < 0)
                    {
                        fingerprintId += 256;
                    }
                    rs232FingerprintMessage.FingerprintId = fingerprintId;
                }
                else if (data[5] == 0x78)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_WRITE_TEMPLATE;
                    int fingerprintId = (int)data[7];
                    if (fingerprintId < 0)
                    {
                        fingerprintId += 256;
                    }
                    rs232FingerprintMessage.FingerprintId = fingerprintId;
                    int remarkId = (int)data[8];
                    if (remarkId < 0)
                    {
                        remarkId += 256;
                    }
                    rs232FingerprintMessage.RemarkId = remarkId;
                }
                else if (data[5] == 0xA6)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_SET_PERMISSION;
                    int fingerprintId = (int)data[7];
                    if (fingerprintId < 0)
                    {
                        fingerprintId += 256;
                    }
                    rs232FingerprintMessage.FingerprintId = fingerprintId;
                }
                else if (data[5] == 0xA7)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_GET_PERMISSION;
                    int fingerprintId = (int)data[7];
                    if (fingerprintId < 0)
                    {
                        fingerprintId += 256;
                    }
                    rs232FingerprintMessage.FingerprintId = fingerprintId;
                }
                else if (data[5] == 0x54)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_ALL_CLEAR;
                }
                else if (data[5] == 0x74)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_GET_EMPTY_ID;
                    int fingerprintId = (int)data[7];
                    if (fingerprintId < 0)
                    {
                        fingerprintId += 256;
                    }
                    rs232FingerprintMessage.FingerprintId = fingerprintId;
                }
                else if (data[5] == 0xA5)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_SET_PATCH_PERMISSION;
                }
                else if (data[5] == 0xA3)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_REGISTER;
                    int fingerprintId = (int)data[7];
                    if (fingerprintId < 0)
                    {
                        fingerprintId += 256;
                    }
                    rs232FingerprintMessage.FingerprintId = fingerprintId;
                }
                else if (data[5] == 0x77)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_GET_TEMPLATE;
                    int dataIndex = (int)data[8];
                    rs232FingerprintMessage.FingerprintDataIndex = dataIndex;
                    int fingerprintId = (int)data[7];
                    if (fingerprintId < 0)
                    {
                        fingerprintId += 256;
                    }
                    rs232FingerprintMessage.FingerprintId = fingerprintId;
                    rs232FingerprintMessage.Data = BytesUtils.Bytes2HexString(data, 10);
                }
                else if (data[5] == 0x59)
                {
                    rs232FingerprintMessage.FingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_SET_DEVICE_ID;
                }
                messageList.Add(rs232FingerprintMessage);
            }
            else if (Utils.ArrayEquals(rs232Head, rs232CapacitorFuelHead))
            {
                rs232Message.Rs232DataType = RS232Message.CAPACITOR_FUEL_DATA;
                Rs232FuelMessage rs232FuelMessage = new Rs232FuelMessage();
                byte[] fuelData = new byte[4];
                Array.Copy(data, 2, fuelData, 0, fuelData.Length);
                if (fuelData.Length > 0)
                {
                    String fuelStr = System.Text.Encoding.UTF8.GetString(fuelData);
                    if (fuelStr.Trim().Length > 0)
                    {
                        rs232FuelMessage.FuelPercent = (float)(Convert.ToDouble(fuelStr) / 100);
                    }
                }
                rs232FuelMessage.Alarm = (int)(data[6]);
                messageList.Add(rs232FuelMessage);
            }
            else if (Utils.ArrayEquals(rs232Head, rs232UltrasonicFuelHead))
            {
                rs232Message.Rs232DataType = RS232Message.ULTRASONIC_FUEL_DATA;
                Rs232FuelMessage rs232FuelMessage = new Rs232FuelMessage();
                byte[] curHeightData = new byte[2];
                Array.Copy(data, 2, curHeightData, 0, curHeightData.Length);
                String curHeightStr = BytesUtils.Bytes2HexString(curHeightData, 0);
                if (curHeightStr.ToLower().Equals("ffff"))
                {
                    rs232FuelMessage.CurLiquidHeight = -999f;
                }
                else
                {
                    rs232FuelMessage.CurLiquidHeight = (float)Convert.ToDouble(curHeightStr) / 10;
                }
                byte[] tempData = new byte[2];
                Array.Copy(data, 4, tempData, 0, tempData.Length);
                String tempStr = BytesUtils.Bytes2HexString(tempData, 0);
                if (tempStr.ToLower().Equals("ffff"))
                {
                    rs232FuelMessage.Temp = -999f;
                }
                else
                {
                    rs232FuelMessage.Temp = Convert.ToInt32(tempStr.Substring(1, 3)) / 10.0f;
                    if (tempStr.Substring(0, 1).Equals("1"))
                    {
                        rs232FuelMessage.Temp = -1 * rs232FuelMessage.Temp;
                    }
                }
                byte[] fullHeightData = new byte[2];
                Array.Copy(data, 6, fullHeightData, 0, fullHeightData.Length);
                String fullHeightStr = BytesUtils.Bytes2HexString(fullHeightData, 0);
                if (fullHeightStr.ToLower().Equals("ffff"))
                {
                    rs232FuelMessage.FullLiquidHeight = -999f;
                }
                else
                {
                    rs232FuelMessage.FullLiquidHeight = (float)Convert.ToDouble(fullHeightStr);
                }
                rs232FuelMessage.LiquidType = (int)data[8] + 1;
                rs232FuelMessage.Alarm = (int)(data[9]);
                messageList.Add(rs232FuelMessage);
            }
            else
            {
                Rs232DeviceMessage rs232DeviceMessage = new Rs232DeviceMessage();
                messageList.Add(rs232DeviceMessage);
                rs232Message.Rs232DataType = RS232Message.OTHER_DEVICE_DATA;
            }
            rs232Message.Rs232DeviceMessageList = messageList;
            return rs232Message;
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
            float speedf = 0f;
            if (!strSp.Equals("ffff"))
            {
                try
                {
                    speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));

                }
                catch (Exception e)
                {

                }
            }
            gpsDriverBehaviorMessage.StartSpeed = speedf;
            int azimuth = BytesUtils.Bytes2Short(bytes, 36);
            gpsDriverBehaviorMessage.StartAzimuth = azimuth;

            DateTime endDate = Utils.getGTM0Date(bytes, 38);
            gpsDriverBehaviorMessage.EndDate = endDate;
            gpsDriverBehaviorMessage.EndAltitude = BytesUtils.Bytes2Float(bytes, 44);
            gpsDriverBehaviorMessage.EndLongitude = BytesUtils.Bytes2Float(bytes, 48);
            gpsDriverBehaviorMessage.EndLatitude = BytesUtils.Bytes2Float(bytes, 52);
            Array.Copy(bytes, 56, bytesSpeed, 0, 2);
            strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0); 
            try
            {
                if (strSp.Contains("f"))
                {
                    speedf = -999f;
                }
                else
                {
                    speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));

                }
            }
            catch (Exception e)
            {

            }
            gpsDriverBehaviorMessage.EndSpeed = speedf;
            azimuth = BytesUtils.Bytes2Short(bytes, 58);
            gpsDriverBehaviorMessage.EndAzimuth = azimuth;
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
                int curParseIndex = beginIdex + i * 28;
                AccelerationData accidentAcceleration = getAccidentAccelerationData(bytes, imei, curParseIndex);
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

        private AccelerationData getAccidentAccelerationData(byte[] bytes, String imei, int curParseIndex)
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
            float speedf = 0.0f;
            try
            {
                if (strSp.Contains("f"))
                {
                    speedf = -999f;
                }
                else
                {
                    speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));

                }
            }
            catch (Exception e)
            {

            }
            acceleration.Speed = speedf;
            int azimuth = BytesUtils.Bytes2Short(bytes, curParseIndex + 26);
            Boolean is_4g_lbs = false;
            Int32 mcc_4g = -1;
            Int32 mnc_4g = -1;
            Int64 eci_4g = -1;
            Int32 tac = -1;
            Int32 pcid_4g_1 = -1;
            Int32 pcid_4g_2 = -1;
            Int32 pcid_4g_3 = -1;
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
                if ((lbsByte & 0x80) == 0x80)
                { 
                    is_4g_lbs = true;
                }
                else
                {
                    is_2g_lbs = true;
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
                mcc_4g = BytesUtils.Bytes2Short(bytes, curParseIndex + 12) & 0x7FFF;
                mnc_4g = BytesUtils.Bytes2Short(bytes, curParseIndex + 14);
                eci_4g = BytesUtils.Byte2Int(bytes, curParseIndex + 16);
                tac = BytesUtils.Bytes2Short(bytes, curParseIndex + 20);
                pcid_4g_1 = BytesUtils.Bytes2Short(bytes, curParseIndex + 22);
                pcid_4g_2 = BytesUtils.Bytes2Short(bytes, curParseIndex + 24);
                pcid_4g_3 = BytesUtils.Bytes2Short(bytes, curParseIndex + 26);
            }
            acceleration.Azimuth = azimuth;
            acceleration.Is_2g_lbs = is_2g_lbs;
            acceleration.Is_4g_lbs = is_4g_lbs;
            acceleration.Mcc_4g = mcc_4g;
            acceleration.Mnc_4g = mnc_4g;
            acceleration.Eci_4g = eci_4g;
            acceleration.TAC = tac;
            acceleration.Pcid_4g_1 = pcid_4g_1;
            acceleration.Pcid_4g_2 = pcid_4g_2;
            acceleration.Pcid_4g_3 = pcid_4g_3;
            acceleration.Mcc_2g = mcc_2g;
            acceleration.Mnc_2g = mnc_2g;
            acceleration.Lac_2g_1 = lac_2g_1;
            acceleration.Ci_2g_1 = ci_2g_1;
            acceleration.Lac_2g_2 = lac_2g_2;
            acceleration.Ci_2g_2 = ci_2g_2;
            acceleration.Lac_2g_3 = lac_2g_3;
            acceleration.Ci_2g_3 = ci_2g_3;
            return acceleration;
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
            if (latlngValid)
            {
                acceleration.Altitude = BytesUtils.Bytes2Float(bytes, curParseIndex + 12);
                acceleration.Longitude = BytesUtils.Bytes2Float(bytes, curParseIndex + 16);
                acceleration.Latitude = BytesUtils.Bytes2Float(bytes, curParseIndex + 20);
                byte[] bytesSpeed = new byte[2];
                Array.Copy(bytes, curParseIndex + 24, bytesSpeed, 0, 2);
                String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
                float speedf = 0.0f;
                try
                {
                    if (strSp.Contains("f"))
                    {
                        speedf = -999f;
                    }
                    else
                    {
                        speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));

                    }
                }
                catch (Exception e)
                {

                }
                acceleration.Speed = speedf;
                int azimuth = BytesUtils.Bytes2Short(bytes, curParseIndex + 26);
                acceleration.Azimuth = azimuth;
            }
            
            Boolean is_4g_lbs = false;
            Int32 mcc_4g = -1;
            Int32 mnc_4g = -1;
            Int64 eci_4g = -1;
            Int32 tac = -1;
            Int32 pcid_4g_1 = -1;
            Int32 pcid_4g_2 = -1;
            Int32 pcid_4g_3 = -1;
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
                if ((lbsByte & 0x80) == 0x80)
                { 
                    is_4g_lbs = true;
                }
                else
                {
                    is_2g_lbs = true;
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
                mcc_4g = BytesUtils.Bytes2Short(bytes, curParseIndex + 12) & 0x7FFF;
                mnc_4g = BytesUtils.Bytes2Short(bytes, curParseIndex + 14);
                eci_4g = BytesUtils.Byte2Int(bytes, curParseIndex + 16);
                tac = BytesUtils.Bytes2Short(bytes, curParseIndex + 20);
                pcid_4g_1 = BytesUtils.Bytes2Short(bytes, curParseIndex + 22);
                pcid_4g_2 = BytesUtils.Bytes2Short(bytes, curParseIndex + 24);
                pcid_4g_3 = BytesUtils.Bytes2Short(bytes, curParseIndex + 26);
            }
           
            if (bytes.Length > 44)
            {
                int gyroscopeAxisXDirect = (bytes[curParseIndex + 28] & 0x80) == 0x80 ? 1 : -1;
                float gyroscopeAxisX = (((bytes[curParseIndex + 28] & 0x7F & 0xff) << 4) + ((bytes[curParseIndex + 29] & 0xf0) >> 4)) * gyroscopeAxisXDirect;
                acceleration.GyroscopeAxisX = gyroscopeAxisX;
                int gyroscopeAxisYDirect = (bytes[curParseIndex + 29] & 0x08) == 0x08 ? 1 : -1;
                float gyroscopeAxisY = ((((bytes[curParseIndex + 29] & 0x07) << 4) & 0xff) + (bytes[curParseIndex + 30] & 0xff)) * gyroscopeAxisYDirect;
                acceleration.GyroscopeAxisY = gyroscopeAxisY;
                int gyroscopeAxisZDirect = (bytes[curParseIndex + 31] & 0x80) == 0x80 ? 1 : -1;
                float gyroscopeAxisZ = (((bytes[curParseIndex + 31] & 0x7F & 0xff) << 4) + ((bytes[curParseIndex + 32] & 0xf0) >> 4)) * gyroscopeAxisZDirect;
                acceleration.GyroscopeAxisZ = gyroscopeAxisZ;
            }
            acceleration.Is_2g_lbs = is_2g_lbs;
            acceleration.Is_4g_lbs = is_4g_lbs;
            acceleration.Mcc_4g = mcc_4g;
            acceleration.Mnc_4g = mnc_4g;
            acceleration.Eci_4g = eci_4g;
            acceleration.TAC = tac;
            acceleration.Pcid_4g_1 = pcid_4g_1;
            acceleration.Pcid_4g_2 = pcid_4g_2;
            acceleration.Pcid_4g_3 = pcid_4g_3;
            acceleration.Mcc_2g = mcc_2g;
            acceleration.Mnc_2g = mnc_2g;
            acceleration.Lac_2g_1 = lac_2g_1;
            acceleration.Ci_2g_1 = ci_2g_1;
            acceleration.Lac_2g_2 = lac_2g_2;
            acceleration.Ci_2g_2 = ci_2g_2;
            acceleration.Lac_2g_3 = lac_2g_3;
            acceleration.Ci_2g_3 = ci_2g_3;
            return acceleration;
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
            double longitude = latlngValid ? BytesUtils.Bytes2Short(bytes, 27) : 0.0;
            double latitude = latlngValid ? BytesUtils.Bytes2Short(bytes, 31) : 0.0;
            int azimuth = latlngValid ? BytesUtils.Bytes2Short(bytes, 37) : 0;
            float speedf = 0.0f;
            try
            {
                byte[] bytesSpeed = new byte[2];
                Array.Copy(bytes, 35, bytesSpeed, 0, 2);
                String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
                if (strSp.Contains("f"))
                {
                    speedf = -999f;
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
            Int64 eci_4g = -1;
            Int32 tac = -1;
            Int32 pcid_4g_1 = -1;
            Int32 pcid_4g_2 = -1;
            Int32 pcid_4g_3 = -1;
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
                if ((lbsByte & 0x80) == 0x80)
                { 
                    is_4g_lbs = true;
                }
                else
                {
                    is_2g_lbs = true;
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
                mcc_4g = BytesUtils.Bytes2Short(bytes, 23) & 0x7FFF;
                mnc_4g = BytesUtils.Bytes2Short(bytes, 25);
                eci_4g = BytesUtils.Byte2Int(bytes, 27);
                tac = BytesUtils.Bytes2Short(bytes, 31);
                pcid_4g_1 = BytesUtils.Bytes2Short(bytes, 33);
                pcid_4g_2 = BytesUtils.Bytes2Short(bytes, 35);
                pcid_4g_3 = BytesUtils.Bytes2Short(bytes, 37);
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
            bluetoothPeripheralDataMessage.Eci_4g = eci_4g;
            bluetoothPeripheralDataMessage.TAC = tac;
            bluetoothPeripheralDataMessage.Pcid_4g_1 = pcid_4g_1;
            bluetoothPeripheralDataMessage.Pcid_4g_2 = pcid_4g_2;
            bluetoothPeripheralDataMessage.Pcid_4g_3 = pcid_4g_3;
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
            if (bleData[0] == 0x00 && bleData[1] == 0x00)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_MIX; 
                int positionIndex = 2;
                while (positionIndex + 2 < bleData.Length)
                {
                    if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x01)
                    {
                        positionIndex += 2;
                        if (positionIndex + 10 <= bleData.Length)
                        {
                            BleTireData bleTireData = getBleTireData(bleData, positionIndex);
                            bleDataList.Add(bleTireData);
                            positionIndex += 10;
                            continue;
                        }
                        else
                        {
                            break;
                        }

                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x02)
                    {
                        positionIndex += 2;
                        if (positionIndex + 8 <= bleData.Length)
                        {
                            BleAlertData bleAlertData = getBleAlertDataWithoutLocation(latlngValid, isHisData, altitude, longitude, latitude, azimuth, speedf, is_4g_lbs, mcc_4g, mnc_4g, eci_4g, tac, pcid_4g_1, pcid_4g_2, pcid_4g_3, is_2g_lbs, mcc_2g, mnc_2g, lac_2g_1, ci_2g_1, lac_2g_2, ci_2g_2, lac_2g_3, ci_2g_3, bleData);
                            bleDataList.Add(bleAlertData);
                            positionIndex += 8;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x03)
                    {
                        positionIndex += 2;
                        if (positionIndex + 8 <= bleData.Length)
                        {
                            BleDriverSignInData bleDriverSignInData = getBleDriverSiginInDataWithoutLocation(latlngValid, isHisData, altitude, longitude, latitude, azimuth, speedf, is_4g_lbs, mcc_4g, mnc_4g, eci_4g, tac, pcid_4g_1, pcid_4g_2, pcid_4g_3, is_2g_lbs, mcc_2g, mnc_2g, lac_2g_1, ci_2g_1, lac_2g_2, ci_2g_2, lac_2g_3, ci_2g_3, bleData);
                            bleDataList.Add(bleDriverSignInData);
                            positionIndex += 8;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x04)
                    {
                        positionIndex += 2;
                        if (positionIndex + 15 <= bleData.Length)
                        {
                            BleTempData bleTempData = getBleTempData(bleData, positionIndex);
                            bleDataList.Add(bleTempData);
                            positionIndex += 15;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x05)
                    {
                        positionIndex += 2;
                        if (positionIndex + 12 <= bleData.Length)
                        {
                            BleDoorData bleDoorData = getBleDoorData(bleData, positionIndex);
                            bleDataList.Add(bleDoorData);
                            positionIndex += 12;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x06)
                    {
                        positionIndex += 2;
                        if (positionIndex + 12 <= bleData.Length)
                        {
                            BleCtrlData bleCtrlData = getBleCtrlData(bleData, positionIndex);
                            bleDataList.Add(bleCtrlData);
                            positionIndex += 12;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x07)
                    {
                        positionIndex += 2;
                        if (positionIndex + 15 <= bleData.Length)
                        {
                            BleFuelData bleFuelData = getBleFuelData(bleData, positionIndex);
                            bleDataList.Add(bleFuelData);
                            positionIndex += 15;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                }

            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x01)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE;
                for (int i = 2; i+10 <= bleData.Length; i += 10)
                {
                    BleTireData bleTireData = getBleTireData(bleData, i); 
                    bleDataList.Add(bleTireData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x02)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS;
                BleAlertData bleAlertData = getBleAlertDataWithoutLocation(latlngValid, isHisData, altitude, longitude, latitude, azimuth, speedf, is_4g_lbs, mcc_4g, mnc_4g, eci_4g, tac, pcid_4g_1, pcid_4g_2, pcid_4g_3, is_2g_lbs, mcc_2g, mnc_2g, lac_2g_1, ci_2g_1, lac_2g_2, ci_2g_2, lac_2g_3, ci_2g_3, bleData);
                bleDataList.Add(bleAlertData);
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x03)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER;
                BleDriverSignInData bleDriverSignInData = getBleDriverSiginInDataWithoutLocation(latlngValid, isHisData, altitude, longitude, latitude, azimuth, speedf, is_4g_lbs, mcc_4g, mnc_4g, eci_4g, tac, pcid_4g_1, pcid_4g_2, pcid_4g_3, is_2g_lbs, mcc_2g, mnc_2g, lac_2g_1, ci_2g_1, lac_2g_2, ci_2g_2, lac_2g_3, ci_2g_3, bleData);
                bleDataList.Add(bleDriverSignInData);
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x04)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP;
                for (int i = 2; i + 15<= bleData.Length; i += 15)
                {
                    BleTempData bleTempData = getBleTempData(bleData, i); 
                    bleDataList.Add(bleTempData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x05)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR;
                for (int i = 2; i+12 <= bleData.Length; i += 12)
                {
                    BleDoorData bleDoorData = getBleDoorData(bleData, i); 
                    bleDataList.Add(bleDoorData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x06)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL;
                for (int i = 2; i+12 <= bleData.Length; i += 12)
                {
                    BleCtrlData bleCtrlData = getBleCtrlData(bleData, i); 
                    bleDataList.Add(bleCtrlData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x07)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL;
                for (int i = 2; i+15 <= bleData.Length; i += 15)
                {
                    BleFuelData bleFuelData = getBleFuelData(bleData, i); 
                    bleDataList.Add(bleFuelData);
                }
            }
            bluetoothPeripheralDataMessage.BleDataList = bleDataList;
            return bluetoothPeripheralDataMessage;

        }

        private static BleDriverSignInData getBleDriverSiginInDataWithoutLocation(bool latlngValid, bool isHisData, double altitude, double longitude, double latitude, int azimuth, float speedf, bool is_4g_lbs, int mcc_4g, int mnc_4g, long eci_4g, int tac, int pcid_4g_1, int pcid_4g_2, int pcid_4g_3, bool is_2g_lbs, int mcc_2g, int mnc_2g, int lac_2g_1, int ci_2g_1, int lac_2g_2, int ci_2g_2, int lac_2g_3, int ci_2g_3, byte[] bleData)
        {
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
            bleDriverSignInData.Eci_4g = eci_4g;
            bleDriverSignInData.TAC = tac;
            bleDriverSignInData.Pcid_4g_1 = pcid_4g_1;
            bleDriverSignInData.Pcid_4g_2 = pcid_4g_2;
            bleDriverSignInData.Pcid_4g_3 = pcid_4g_3;
            bleDriverSignInData.Mcc_2g = mcc_2g;
            bleDriverSignInData.Mnc_2g = mnc_2g;
            bleDriverSignInData.Lac_2g_1 = lac_2g_1;
            bleDriverSignInData.Ci_2g_1 = ci_2g_1;
            bleDriverSignInData.Lac_2g_2 = lac_2g_2;
            bleDriverSignInData.Ci_2g_2 = ci_2g_2;
            bleDriverSignInData.Lac_2g_3 = lac_2g_3;
            bleDriverSignInData.Ci_2g_3 = ci_2g_3;
            return bleDriverSignInData;
        }

        private static BleAlertData getBleAlertDataWithoutLocation(bool latlngValid, bool isHisData, double altitude, double longitude, double latitude, int azimuth, float speedf, bool is_4g_lbs, int mcc_4g, int mnc_4g, long eci_4g, int tac, int pcid_4g_1, int pcid_4g_2, int pcid_4g_3, bool is_2g_lbs, int mcc_2g, int mnc_2g, int lac_2g_1, int ci_2g_1, int lac_2g_2, int ci_2g_2, int lac_2g_3, int ci_2g_3, byte[] bleData)
        {
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
            bleAlertData.Eci_4g = eci_4g;
            bleAlertData.TAC = tac;
            bleAlertData.Pcid_4g_1 = pcid_4g_1;
            bleAlertData.Pcid_4g_2 = pcid_4g_2;
            bleAlertData.Pcid_4g_3 = pcid_4g_3;
            bleAlertData.Mcc_2g = mcc_2g;
            bleAlertData.Mnc_2g = mnc_2g;
            bleAlertData.Lac_2g_1 = lac_2g_1;
            bleAlertData.Ci_2g_1 = ci_2g_1;
            bleAlertData.Lac_2g_2 = lac_2g_2;
            bleAlertData.Ci_2g_2 = ci_2g_2;
            bleAlertData.Lac_2g_3 = lac_2g_3;
            bleAlertData.Ci_2g_3 = ci_2g_3;
            return bleAlertData;
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
            bluetoothPeripheralDataMessage.ProtocolHeadType = 0x10;
            bluetoothPeripheralDataMessage.OrignBytes = bytes;
            bluetoothPeripheralDataMessage.IsHistoryData = (bytes[15] & 0x80) != 0x00;
            bluetoothPeripheralDataMessage.SerialNo = serialNo;
            //bluetoothPeripheralDataMessage.IsNeedResp = isNeedResp;
            bluetoothPeripheralDataMessage.Imei = imei;
            byte[] bleData = new byte[bytes.Length - 22];
            Array.Copy(bytes, 22, bleData, 0, bleData.Length);
            List<BleData> bleDataList = new List<BleData>();
            if (bleData[0] == 0x00 && bleData[1] == 0x00)
            { 
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE;
                int positionIndex = 2;
                while (positionIndex + 2 < bleData.Length)
                {
                    if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x01)
                    {
                        positionIndex += 2;
                        if (positionIndex + 10 <= bleData.Length)
                        {
                            BleTireData bleTireData = getBleTireData(bleData, positionIndex);
                            bleDataList.Add(bleTireData);
                            positionIndex += 10;
                            continue;
                        }
                        else
                        {
                            break;
                        }

                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x02)
                    {
                        positionIndex += 2;
                        if (positionIndex + 25 <= bleData.Length)
                        {
                            BleAlertData bleAlertData = getBleAlertData(bleData);
                            bleDataList.Add(bleAlertData);
                            positionIndex += 25;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x03)
                    {
                        positionIndex += 2;
                        if (positionIndex + 25 <= bleData.Length)
                        {
                            BleDriverSignInData bleDriverSignInData = getBleDriverSignInData(bleData);
                            bleDataList.Add(bleDriverSignInData);
                            positionIndex += 25;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x04)
                    {
                        positionIndex += 2;
                        if (positionIndex + 15 <= bleData.Length)
                        {
                            BleTempData bleTempData = getBleTempData(bleData, positionIndex);
                            bleDataList.Add(bleTempData);
                            positionIndex += 15;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x05)
                    {
                        positionIndex += 2;
                        if (positionIndex + 12 <= bleData.Length)
                        {
                            BleDoorData bleDoorData = getBleDoorData(bleData, positionIndex);
                            bleDataList.Add(bleDoorData);
                            positionIndex += 12;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x06)
                    {
                        positionIndex += 2;
                        if (positionIndex + 12 <= bleData.Length)
                        {
                            BleCtrlData bleCtrlData = getBleCtrlData(bleData, positionIndex);
                            bleDataList.Add(bleCtrlData);
                            positionIndex += 12;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x07)
                    {
                        positionIndex += 2;
                        if (positionIndex + 15 <= bleData.Length)
                        {
                            BleFuelData bleFuelData = getBleFuelData(bleData, positionIndex);
                            bleDataList.Add(bleFuelData);
                            positionIndex += 15;
                            continue;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x01)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE;
                for (int i = 2; i+10 <= bleData.Length; i += 10)
                {
                    BleTireData bleTireData = getBleTireData(bleData, i);
                    bleDataList.Add(bleTireData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x02)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS;
                BleAlertData bleAlertData = getBleAlertData(bleData);
                bleDataList.Add(bleAlertData);
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x03)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER;
                BleDriverSignInData bleDriverSignInData = getBleDriverSignInData(bleData);
                bleDataList.Add(bleDriverSignInData);
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x04)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP;
                for (int i = 2; i+15 <= bleData.Length; i += 15)
                {
                    BleTempData bleTempData = getBleTempData(bleData, i);
                    bleDataList.Add(bleTempData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x05)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR;
                for (int i = 2; i+12 <= bleData.Length; i += 12)
                {
                    BleDoorData bleDoorData = getBleDoorData(bleData, i);
                    bleDataList.Add(bleDoorData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x06)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL;
                for (int i = 2; i+12 <= bleData.Length; i += 12)
                {
                    BleCtrlData bleCtrlData = getBleCtrlData(bleData, i);
                    bleDataList.Add(bleCtrlData);
                }
            }
            else if (bleData[0] == 0x00 && bleData[1] == 0x07)
            {
                bluetoothPeripheralDataMessage.MessageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL;
                for (int i = 2; i+15 <= bleData.Length; i += 15)
                {
                    BleFuelData bleFuelData = getBleFuelData(bleData, i);
                    bleDataList.Add(bleFuelData);
                }
            } 
            bluetoothPeripheralDataMessage.BleDataList = bleDataList;
            return bluetoothPeripheralDataMessage;
        }

        private static BleAlertData getBleAlertData(byte[] bleData)
        {
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
                    speedf = -999f;
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
            Int64 eci_4g = -1;
            Int32 tac = -1;
            Int32 pcid_4g_1 = -1;
            Int32 pcid_4g_2 = -1;
            Int32 pcid_4g_3 = -1;
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
                if ((lbsByte & 0x80) == 0x80)
                {
                    is_4g_lbs = true;
                }
                else
                {
                    is_2g_lbs = true;
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
                mcc_4g = BytesUtils.Bytes2Short(bleData, 11) & 0x7FFF;
                mnc_4g = BytesUtils.Bytes2Short(bleData, 13);
                eci_4g = BytesUtils.Byte2Int(bleData, 15);
                tac = BytesUtils.Bytes2Short(bleData, 19);
                pcid_4g_1 = BytesUtils.Bytes2Short(bleData, 21);
                pcid_4g_2 = BytesUtils.Bytes2Short(bleData, 23);
                pcid_4g_3 = BytesUtils.Bytes2Short(bleData, 25);
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
            bleAlertData.Eci_4g = eci_4g;
            bleAlertData.TAC = tac;
            bleAlertData.Pcid_4g_1 = pcid_4g_1;
            bleAlertData.Pcid_4g_2 = pcid_4g_2;
            bleAlertData.Pcid_4g_3 = pcid_4g_3;
            bleAlertData.Mcc_2g = mcc_2g;
            bleAlertData.Mnc_2g = mnc_2g;
            bleAlertData.Lac_2g_1 = lac_2g_1;
            bleAlertData.Ci_2g_1 = ci_2g_1;
            bleAlertData.Lac_2g_2 = lac_2g_2;
            bleAlertData.Ci_2g_2 = ci_2g_2;
            bleAlertData.Lac_2g_3 = lac_2g_3;
            bleAlertData.Ci_2g_3 = ci_2g_3;
            return bleAlertData;
        }

        private static BleDriverSignInData getBleDriverSignInData(byte[] bleData)
        {
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
                    speedf = -999f;
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
            Int64 eci_4g = -1;
            Int32 tac = -1;
            Int32 pcid_4g_1 = -1;
            Int32 pcid_4g_2 = -1;
            Int32 pcid_4g_3 = -1;
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
                if ((lbsByte & 0x80) == 0x80)
                {
                    is_4g_lbs = true;
                }
                else
                {
                    is_2g_lbs = true;
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
                mcc_4g = BytesUtils.Bytes2Short(bleData, 11) & 0x7FFF;
                mnc_4g = BytesUtils.Bytes2Short(bleData, 13);
                eci_4g = BytesUtils.Byte2Int(bleData, 15);
                tac = BytesUtils.Bytes2Short(bleData, 19);
                pcid_4g_1 = BytesUtils.Bytes2Short(bleData, 21);
                pcid_4g_2 = BytesUtils.Bytes2Short(bleData, 23);
                pcid_4g_3 = BytesUtils.Bytes2Short(bleData, 25);
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
            bleDriverSignInData.Eci_4g = eci_4g;
            bleDriverSignInData.TAC = tac;
            bleDriverSignInData.Pcid_4g_1 = pcid_4g_1;
            bleDriverSignInData.Pcid_4g_2 = pcid_4g_2;
            bleDriverSignInData.Pcid_4g_3 = pcid_4g_3;
            bleDriverSignInData.Mcc_2g = mcc_2g;
            bleDriverSignInData.Mnc_2g = mnc_2g;
            bleDriverSignInData.Lac_2g_1 = lac_2g_1;
            bleDriverSignInData.Ci_2g_1 = ci_2g_1;
            bleDriverSignInData.Lac_2g_2 = lac_2g_2;
            bleDriverSignInData.Ci_2g_2 = ci_2g_2;
            bleDriverSignInData.Lac_2g_3 = lac_2g_3;
            bleDriverSignInData.Ci_2g_3 = ci_2g_3;
            return bleDriverSignInData;
        }

        private static BleDoorData getBleDoorData(byte[] bleData, int i)
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
            return bleDoorData;
        }

        private static BleFuelData getBleFuelData(byte[] bleData, int i)
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
            return bleFuelData;
        }

        private static BleCtrlData getBleCtrlData(byte[] bleData, int i)
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
            return bleCtrlData;
        }

        private static BleTempData getBleTempData(byte[] bleData, int i)
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
            return bleTempData;
        }

        private static BleTireData getBleTireData(byte[] bleData, int i)
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
            return bleTireData;
        }

        private LocationMessage parseSecondDataMessage(byte[] bytes)
        {
            byte[] command = new byte[HEADER_LENGTH];
            Array.Copy(bytes, 0, command, 0, HEADER_LENGTH);
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000; 
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            byte[] data = new byte[bytes.Length - 15];
            Array.Copy(bytes, 15, data, 0, bytes.Length - 15);
            int samplingIntervalAccOn = BytesUtils.Bytes2Short(data, 0);
            int samplingIntervalAccOff = BytesUtils.Bytes2Short(data, 2);
            int angleCompensation = (int)data[4];
            int distanceCompensation = BytesUtils.Bytes2Short(data, 5);
            short limit = (short)BytesUtils.Bytes2Short(data, 7);
            short speed = (short)((0x7F80 & limit) >> 7);
            if ((0x8000 & limit) != 0)
            {
                speed = (short)(speed * 1.609344);
            }
            int protocolHead = bytes[2];
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
            int smsLanguageType = data[13] & 0xF;
            bool isRelayWaiting = ((data[13] & 0xC0) != 0x00) && ((data[13] & 0x80) == 0x00);
            int dragThreshold = BytesUtils.Bytes2Short(data, 14);

            long iop = (long)BytesUtils.Bytes2Short(data, 16);
            bool iopIgnition = (iop & MASK_IGNITION) == MASK_IGNITION;
            bool iopPowerCutOff = (iop & MASK_POWER_CUT) == MASK_POWER_CUT;
            bool iopACOn = (iop & MASK_AC) == MASK_AC;
            int input1 = (iop & 0x100) == 0x100 ? 1 : 0;
            int input2 = (iop & 0x2000) == 0x2000 ? 1 : 0;
            int input3 = (iop & 0x1000) == 0x1000 ? 1 : 0;
            int input4 = (iop & 0x800) == 0x800 ? 1 : 0;
            int input5 = (iop & 0x400) == 0x400 ? 1 : 0;
            int input6 = (iop & 0x200) == 0x200 ? 1 : 0;
            bool isHadFmsData = (iop & 0x80) == 0x80;
            bool isMileageSrcIsFms = (iop & 0x40) == 0x40;
            int output1 = (data[18] & 0x20) == 0x20 ? 1 : 0;
            int output2 = (data[18] & 0x10) == 0x10 ? 1 : 0;
            int output3 = (data[18] & 0x8) == 0x8 ? 1 : 0;
            bool output12V = (data[18] & 0x20) == 0x20;
            bool outputVout = (data[18] & 0x40) == 0x40; 
            int accdetSettingStatus = (iop & 0x1) ==  0x1  ? 1 : 0;
            String str = BytesUtils.Bytes2HexString(data, 20);
            float analoginput = 0;
            if (!str.ToLower().Equals("ffff"))
            {
                try
                {
                    analoginput = (float)(Convert.ToDouble(String.Format("{0}.{1}", str.Substring(0, 2), str.Substring(2, 2))));
                }
                catch (Exception e)
                {
                    Console.Write(e.Message);
                }
            }
            else
            {
                analoginput = -999;
            }

            str = BytesUtils.Bytes2HexString(data, 22);
            float analoginput2 = 0;
            if (!str.ToLower().StartsWith("ffff"))
            {
                try
                {
                    analoginput2 = (float)(Convert.ToDouble(String.Format("{0}.{1}", str.Substring(0, 2), str.Substring(2, 2))));
                }
                catch (Exception e)
                {
                    Console.Write(e.Message);
                }
            }
            else
            {
                analoginput2 = -999;
            }

            str = BytesUtils.Bytes2HexString(data, 24);
            float analoginput3 = 0;
            if (!str.ToLower().StartsWith("ffff"))
            {
                try
                {
                    analoginput3 = (float)(Convert.ToDouble(String.Format("{0}.{1}", str.Substring(0, 2),str.Substring(2, 2))));
                }
                catch (Exception e)
                {
                    Console.Write(e.Message);
                }
            }
            else
            {
                analoginput3 = -999;
            }
            long lastMileageDiff = (long)BytesUtils.Byte2Int(data, 26);
 
            byte alarmByte = data[30];
            int originalAlarmCode = (int)alarmByte;
            bool isAlarmData = command[2] == 0x14 || command[2] == 0x34;
            int externalPowerReduceValue = (data[31] & 0x11);
            bool isSendSmsAlarmToManagerPhone = (data[31] & 0x20) == 0x20;
            bool isSendSmsAlarmWhenDigitalInput2Change = (data[31] & 0x10) == 0x10;
            int jammerDetectionStatus = (data[31] & 0xC);
            long mileage = (long)BytesUtils.Byte2Int(data, 32);
            byte[] batteryBytes = new byte[] { data[36] };
            String batteryStr = BytesUtils.Bytes2HexString(batteryBytes, 0);
            DateTime gmt0 = Utils.getGTM0Date(data, 37);
            bool latlngValid = (data[9] & 0x40) != 0x00;
            byte[] latlngData = new byte[latlngInvalidData.Length];
            Array.Copy(bytes, 43, latlngData, 0, latlngInvalidData.Length);
            if (Utils.ArrayEquals(latlngData, latlngInvalidData))
            {
                latlngValid = false;
            }
            double altitude = latlngValid ? BytesUtils.Bytes2Float(data, 43) : 0.0;
            double latitude = latlngValid ? BytesUtils.Bytes2Float(data, 51) : 0.0;
            double longitude = latlngValid ? BytesUtils.Bytes2Float(data, 47) : 0.0;
            float speedf = 0.0f;
            try
            {
                if (latlngValid)
                {
                    byte[] bytesSpeed = new byte[2];
                    Array.Copy(data, 55, bytesSpeed, 0, 2);
                    String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
                    speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));
                }
            }
            catch (Exception e)
            {
            }
            int azimuth = latlngValid ? BytesUtils.Bytes2Short(data, 57) : 0;
            Boolean is_4g_lbs = false;
            Int32 mcc_4g = -1;
            Int32 mnc_4g = -1;
            Int64 eci_4g = -1;
            Int32 tac = -1;
            Int32 pcid_4g_1 = -1;
            Int32 pcid_4g_2 = -1;
            Int32 pcid_4g_3 = -1;
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
                byte lbsByte = data[43];
                if ((lbsByte & 0x80) == 0x80)
                {
                    is_4g_lbs = true; 
                }
                else
                {
                    is_2g_lbs = true;
                }
            }
            if (is_2g_lbs)
            {
                mcc_2g = BytesUtils.Bytes2Short(data, 43);
                mnc_2g = BytesUtils.Bytes2Short(data, 45);
                lac_2g_1 = BytesUtils.Bytes2Short(data, 47);
                ci_2g_1 = BytesUtils.Bytes2Short(data, 49);
                lac_2g_2 = BytesUtils.Bytes2Short(data, 51);
                ci_2g_2 = BytesUtils.Bytes2Short(data, 53);
                lac_2g_3 = BytesUtils.Bytes2Short(data, 55);
                ci_2g_3 = BytesUtils.Bytes2Short(data, 57);
            }
            if (is_4g_lbs)
            {
                mcc_4g = BytesUtils.Bytes2Short(data, 43) & 0x7FFF;
                mnc_4g = BytesUtils.Bytes2Short(data, 45);
                eci_4g = BytesUtils.Byte2Int(data, 47);
                tac = BytesUtils.Bytes2Short(data, 51);
                pcid_4g_1 = BytesUtils.Bytes2Short(data, 53);
                pcid_4g_2 = BytesUtils.Bytes2Short(data, 55);
                pcid_4g_3 = BytesUtils.Bytes2Short(data, 57);
            }
            float batteryVoltage = 0f;

            byte[] batteryVoltageBytes = new byte[2];
            Array.Copy(data, 59, batteryVoltageBytes, 0, 2);
            String batteryVoltageStr = BytesUtils.Bytes2HexString(batteryVoltageBytes, 0);

            try
            {
                batteryVoltage = (float)Convert.ToDouble(batteryVoltageStr) / 100.0f;
            }
            catch (Exception e)
            {

            }
            float externalPowerVoltage = 0f;
            byte[] externalPowerVoltageBytes = new byte[2];
            Array.Copy(data, 61, externalPowerVoltageBytes, 0, 2);
            String externalPowerVoltageStr = BytesUtils.Bytes2HexString(externalPowerVoltageBytes, 0);
            try
            {
                externalPowerVoltage = (float)Convert.ToDouble(externalPowerVoltageStr) / 100.0f;
            }
            catch (Exception e)
            {

            }
            int rpm = 0;
            rpm = BytesUtils.Bytes2Short(data, 63);
            if (rpm == 32768 || rpm == 65535)
            {
                rpm = -999;
            }
            bool isSmartUploadSupport = (data[65] & 0x8) == 0x8;
            bool supportChangeBattery = (data[66] & 0x8) == 0x8;
            float deviceTemp = -999;
            if (data.Length >= 69 && data[68] != 0xff)
            {
                deviceTemp = (data[68] & 0x7F) * ((data[68] & 0x80) == 0x80 ? -1 : 1);
            }
            int fmsSpeed = data[66];
            LocationMessage message;
            if (isAlarmData)
            {
                message = new LocationAlarmMessage(); 
            }
            else
            {
                message = new LocationInfoMessage(); 
            }
            message.ProtocolHeadType = protocolHead;
            message.IsHadFmsData = isHadFmsData;
            int curPositionIndex = 74;
            if (data.Length >= 74)
            {
                int hdop = BytesUtils.Bytes2Short(data, 69);
                message.Hdop = hdop;
                if (isHadFmsData)
                {
                    lastMileageDiff = -999; 
                    long engineHours = BytesUtils.Byte2Int(data, 26);
                    if (engineHours == 4294967295l)
                    {
                        engineHours = -999;
                    }
                    int coolingFluidTemp = (int)data[71] < 0 ? (int)data[71] + 256 : (int)data[71];
                    if (coolingFluidTemp == 255)
                    {
                        coolingFluidTemp = -999;
                    }
                    else
                    {
                        coolingFluidTemp = coolingFluidTemp - 40;
                    }
                    int remainFuel = (int)data[72];
                    if (remainFuel == 255)
                    {
                        remainFuel = -999;
                    }
                    int engineLoad = (int)data[73];
                    if (engineLoad == 255)
                    {
                        engineLoad = -999;
                    }
                    message.FmsEngineHours= engineHours;
                    message.RemainFuelRate= remainFuel;
                    message.EngineLoad = engineLoad;
                    message.CoolingFluidTemp = coolingFluidTemp;
                    message.FmsSpeed = fmsSpeed;
                    if (data.Length >= 78 && isHadFmsData)
                    {
                        long fmsAccumulatingFuelConsumption = BytesUtils.Byte2Int(data, 74);
                        if (fmsAccumulatingFuelConsumption == 4294967295l)
                        {
                            fmsAccumulatingFuelConsumption = -999;
                        }
                        else
                        {
                            fmsAccumulatingFuelConsumption = fmsAccumulatingFuelConsumption * 1000;
                        }
                        message.FmsAccumulatingFuelConsumption = fmsAccumulatingFuelConsumption;
                        curPositionIndex += 4;
                    }
                    supportChangeBattery = false;
                }
            }
            if ((protocolHead == 0x33 || protocolHead == 0x34) && data.Length >= curPositionIndex + 12)
            {
                byte[] axisXByte = Utils.ArrayCopyOfRange(data, curPositionIndex, curPositionIndex + 2);
                int axisX = BytesUtils.bytes2SingleShort(axisXByte,0);
                curPositionIndex += 2;
                byte[] axisYByte = Utils.ArrayCopyOfRange(data, curPositionIndex, curPositionIndex + 2);
                int axisY = BytesUtils.bytes2SingleShort(axisYByte, 0);
                curPositionIndex += 2;
                byte[] axisZByte = Utils.ArrayCopyOfRange(data, curPositionIndex, curPositionIndex + 2);
                int axisZ = BytesUtils.bytes2SingleShort(axisZByte, 0);
                curPositionIndex += 2;
                byte[] gyroscopeAxisXByte = Utils.ArrayCopyOfRange(data, curPositionIndex, curPositionIndex + 2);
                int gyroscopeAxisX = BytesUtils.bytes2SingleShort(gyroscopeAxisXByte, 0);
                curPositionIndex += 2;
                byte[] gyroscopeAxisYByte = Utils.ArrayCopyOfRange(data, curPositionIndex, curPositionIndex + 2);
                int gyroscopeAxisY = BytesUtils.bytes2SingleShort(gyroscopeAxisYByte, 0);
                curPositionIndex += 2;
                byte[] gyroscopeAxisZByte = Utils.ArrayCopyOfRange(data, curPositionIndex, curPositionIndex + 2);
                int gyroscopeAxisZ = BytesUtils.bytes2SingleShort(gyroscopeAxisZByte, 0);
                curPositionIndex += 2; 
                message.AxisX = axisX;
                message.AxisY = axisY;
                message.AxisZ = axisZ;
                message.GyroscopeAxisX = gyroscopeAxisX;
                message.GyroscopeAxisY = gyroscopeAxisY;
                message.GyroscopeAxisZ = gyroscopeAxisZ;

            }
            if (isMileageSrcIsFms)
            {
                message.MileageSource = 2;
            }
            message.OrignBytes = bytes;
            message.SerialNo = serialNo;
            //message.IsNeedResp = isNeedResp;
            message.Imei = imei;
            message.NetworkSignal = networkSignal;
            message.SamplingIntervalAccOn = samplingIntervalAccOn;
            message.SamplingIntervalAccOff = samplingIntervalAccOff;
            message.AngleCompensation = angleCompensation;
            message.DistanceCompensation = distanceCompensation;
            message.OverspeedLimit = (int)speed;// 统一单位为 km/h
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
            message.IopACOn = iopACOn;
            message.Output12V = output12V;
            message.Output1 = output1;
            message.Output2 = output2;
            message.Output3 = output3;
            message.OutputVout = outputVout;
            message.AnalogInput1 = analoginput;
            message.AnalogInput2 = analoginput2;
            message.AnalogInput3 = analoginput3; 
            message.AccdetSettingStatus = accdetSettingStatus;
            message.Rpm = rpm;
            message.OriginalAlarmCode = originalAlarmCode;
            message.Mileage = mileage;
            message.DeviceTemp = deviceTemp;
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
            message.Input1 = input1;
            message.Input2 = input2;
            message.Input3 = input3;
            message.Input4 = input4;
            message.Input5 = input5;
            message.Input6 = input6;
            message.LastMileageDiff = lastMileageDiff; 
            message.BatteryVoltage = batteryVoltage;
            message.IsSmartUploadSupport = isSmartUploadSupport;
            message.SupportChangeBattery = supportChangeBattery;
            message.Is_2g_lbs = is_2g_lbs;
            message.Is_4g_lbs = is_4g_lbs;
            message.Mcc_4g = mcc_4g;
            message.Mnc_4g = mnc_4g;
            message.Eci_4g = eci_4g;
            message.TAC = tac;
            message.Pcid_4g_1 = pcid_4g_1;
            message.Pcid_4g_2 = pcid_4g_2;
            message.Pcid_4g_3 = pcid_4g_3;
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
            return message;

        }

        private LocationMessage parseDataMessage(byte[] bytes)
        {
            byte[] command = new byte[HEADER_LENGTH];
            Array.Copy(bytes, 0, command, 0, HEADER_LENGTH);
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            //Boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
            String imei = BytesUtils.IMEI.Decode(bytes, 7);
            byte[] data = new byte[bytes.Length - 15];
            Array.Copy(bytes, 15, data, 0, bytes.Length - 15);
            int samplingIntervalAccOn = BytesUtils.Bytes2Short(data, 0);
            int samplingIntervalAccOff = BytesUtils.Bytes2Short(data, 2);
            int angleCompensation = (int)data[4];
            int distanceCompensation = BytesUtils.Bytes2Short(data, 5);
            short limit = (short)BytesUtils.Bytes2Short(data, 7);
            short speed = (short)((0x7F80 & limit) >> 7);
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
            int smsLanguageType = data[13] & 0xF;
            bool isRelayWaiting = ((data[13] & 0xC0) != 0x00) && ((data[13] & 0x80) == 0x00);
            int dragThreshold = BytesUtils.Bytes2Short(data, 14);
            long iop = (long)BytesUtils.Bytes2Short(data, 16);
            bool iopIgnition = (iop & MASK_IGNITION) == MASK_IGNITION;
            bool iopPowerCutOff = (iop & MASK_POWER_CUT) == MASK_POWER_CUT;
            bool iopACOn = (iop & MASK_AC) == MASK_AC;
            bool iopRs232DeviceValid = (iop & IOP_RS232_DEVICE_VALID) != IOP_RS232_DEVICE_VALID;
            int input1 = iopIgnition ? 1 : 0;
            int input2 = iopACOn ? 1 : 0;
            int input3 = (iop & 0x1000) == 0x1000 ? 1 : 0;
            int input4 = (iop & 0x800) == 0x800 ? 1 : 0;
            int input5 = (iop & 0x10) == 0x10 ? 1 : 0;
            int input6 = (iop & 0x08) == 0x08 ? 1 : 0;
            int output1 = (iop & 0x0400) == 0x0400 ? 1 : 0;
            int output2 = (iop * 0x200) == 0x200 ? 1 : 0;
            int output3 = (iop * 0x100) == 0x100 ? 1 : 0;
            int output12V = (iop * 0x10) == 0x10 ? 1 : 0;
            int outputVout = (iop * 0x8) == 0x8 ? 1 : 0;
            int speakerStatus = (iop & 0x40) ==  0x40  ? 1 : 0;
            int rs232PowerOf5V = (iop & 0x20) ==  0x20  ? 1 : 0;
            int accdetSettingStatus = (iop & 0x1) ==  0x1  ? 1 : 0;
            String str = BytesUtils.Bytes2HexString(data, 18);
            float analoginput = 0;
            try
            {
                analoginput = (float)(Convert.ToDouble(String.Format("{0}.{1}", str.Substring(0, 2), str.Substring(2, 2))));
            }
            catch (Exception e)
            {
                Console.Write(e.Message);
            }
            str = BytesUtils.Bytes2HexString(data, 20);
            float analoginput2 = 0;
            try
            {
                analoginput2 = (float)(Convert.ToDouble(String.Format("{0}.{1}", str.Substring(0, 2), str.Substring(2, 2))));
            }
            catch (Exception e)
            {
                Console.Write(e.Message);
            }
            byte alarmByte = data[22];
            int originalAlarmCode = (int)alarmByte;
            bool isAlarmData = command[2] == 0x04 || command[2] == 0x18;
            bool isSendSmsAlarmToManagerPhone = (data[23] & 0x20) == 0x20;
            bool isSendSmsAlarmWhenDigitalInput2Change = (data[23] & 0x10) == 0x10;
            int jammerDetectionStatus = (data[23] & 0xC);
            long mileage = (long)BytesUtils.Byte2Int(data, 24);
            byte[] batteryBytes = new byte[] { data[28] };
            String batteryStr = BytesUtils.Bytes2HexString(batteryBytes, 0);
            DateTime gmt0 = Utils.getGTM0Date(data, 29);
            bool latlngValid = (data[9] & 0x40) != 0x00;
            byte[] latlngData = new byte[latlngInvalidData.Length];
            Array.Copy(bytes, 35, latlngData, 0, latlngInvalidData.Length);
            if (Utils.ArrayEquals(latlngData, latlngInvalidData))
            {
                latlngValid = false;
            }
            double altitude = latlngValid ? BytesUtils.Bytes2Float(data, 35) : 0.0;
            double latitude = latlngValid ? BytesUtils.Bytes2Float(data, 43) : 0.0;
            double longitude = latlngValid ? BytesUtils.Bytes2Float(data, 39) : 0.0;
            byte[] bytesSpeed = new byte[2];
            Array.Copy(data, 47, bytesSpeed, 0, 2);
            String strSp = BytesUtils.Bytes2HexString(bytesSpeed, 0);
            float speedf = 0;
            int azimuth = 0;
            if (latlngValid && strSp != "FFFF" && strSp != "ffff")
            {
                try
                {
                    speedf = (float)Convert.ToDouble(String.Format("{0}.{1}", Convert.ToInt32(strSp.Substring(0, 3)), Convert.ToInt32(strSp.Substring(3, strSp.Length - 3))));

                }
                catch (Exception e)
                {

                }
                azimuth = latlngValid ? BytesUtils.Bytes2Short(data, 49) : 0;
            }
            Boolean is_4g_lbs = false;
            Int32 mcc_4g = -1;
            Int32 mnc_4g = -1;
            Int64 eci_4g = -1;
            Int32 tac = -1;
            Int32 pcid_4g_1 = -1;
            Int32 pcid_4g_2 = -1;
            Int32 pcid_4g_3 = -1;
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
                byte lbsByte = data[35];
                if ((lbsByte & 0x80) == 0x80)
                { 
                    is_4g_lbs = true;
                }
                else
                {
                    is_2g_lbs = true;
                }
            }
            if (is_2g_lbs)
            {
                mcc_2g = BytesUtils.Bytes2Short(data, 35);
                mnc_2g = BytesUtils.Bytes2Short(data, 37);
                lac_2g_1 = BytesUtils.Bytes2Short(data, 39);
                ci_2g_1 = BytesUtils.Bytes2Short(data, 41);
                lac_2g_2 = BytesUtils.Bytes2Short(data, 43);
                ci_2g_2 = BytesUtils.Bytes2Short(data, 45);
                lac_2g_3 = BytesUtils.Bytes2Short(data, 47);
                ci_2g_3 = BytesUtils.Bytes2Short(data, 49);
            }
            if (is_4g_lbs)
            {
                mcc_4g = BytesUtils.Bytes2Short(data, 35) & 0x7FFF;
                mnc_4g = BytesUtils.Bytes2Short(data, 37);
                eci_4g = BytesUtils.Byte2Int(data, 39);
                tac = BytesUtils.Bytes2Short(data, 43);
                pcid_4g_1 = BytesUtils.Bytes2Short(data, 45);
                pcid_4g_2 = BytesUtils.Bytes2Short(data, 47);
                pcid_4g_3 = BytesUtils.Bytes2Short(data, 49);
            }

            float externalPowerVoltage = 0f;
            if (data.Length >= 53)
            {
                byte[] externalPowerVoltageBytes = new byte[2];
                Array.Copy(data, 51, externalPowerVoltageBytes, 0, 2);
                String externalPowerVoltageStr = BytesUtils.Bytes2HexString(externalPowerVoltageBytes, 0);
                externalPowerVoltage = (float)Convert.ToDouble(externalPowerVoltageStr) / 100.0f;
            }
            LocationMessage message;
            if (isAlarmData)
            {
                message = new LocationAlarmMessage();
                message.ProtocolHeadType = 0x04;
            }
            else
            {
                message = new LocationInfoMessage();
                message.ProtocolHeadType = 0x02;
            }
             int protocolHead = bytes[2];
            if ((protocolHead == 0x16 || protocolHead == 0x18) && data.Length >= 65){
                byte[] axisXByte = Utils.ArrayCopyOfRange(data,53,55);
                int axisX = BytesUtils.bytes2SingleShort(axisXByte,0); 
                byte[] axisYByte = Utils.ArrayCopyOfRange(data,55,57);
                int axisY = BytesUtils.bytes2SingleShort(axisYByte,0);
                byte[] axisZByte = Utils.ArrayCopyOfRange(data,57,59);
                int axisZ = BytesUtils.bytes2SingleShort(axisZByte,0); 
                byte[] gyroscopeAxisXByte = Utils.ArrayCopyOfRange(data,59,61);
                int gyroscopeAxisX = BytesUtils.bytes2SingleShort(gyroscopeAxisXByte,0);
                byte[] gyroscopeAxisYByte = Utils.ArrayCopyOfRange(data,61,63);
                int gyroscopeAxisY = BytesUtils.bytes2SingleShort(gyroscopeAxisYByte,0);
                byte[] gyroscopeAxisZByte = Utils.ArrayCopyOfRange(data, 63, 65);
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
            message.NetworkSignal = networkSignal;
            message.SamplingIntervalAccOn = samplingIntervalAccOn;
            message.SamplingIntervalAccOff = samplingIntervalAccOff;
            message.AngleCompensation = angleCompensation;
            message.DistanceCompensation = distanceCompensation;
            message.OverspeedLimit = (int)speed;// 统一单位为 km/h
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
            message.Rs232DeviceValid = iopRs232DeviceValid;
            message.IopIgnition = iopIgnition;
            message.IopPowerCutOff = iopPowerCutOff;
            message.IopACOn = iopACOn;
            message.Input1 = input1;
            message.Input2 = input2;
            message.Input3 = input3;
            message.Input4 = input4;
            message.Input5 = input5;
            message.Input6 = input6;
            message.Output1 = output1;
            message.Output2 = output2;
            message.Output3 = output3;
            message.AnalogInput1 = analoginput;
            message.AnalogInput2 = analoginput2; 
            message.SpeakerStatus = speakerStatus;
            message.Rs232PowerOf5V = rs232PowerOf5V;
            message.AccdetSettingStatus = accdetSettingStatus; 
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
            message.Is_2g_lbs = is_2g_lbs;
            message.Is_4g_lbs = is_4g_lbs;
            message.Mcc_4g = mcc_4g;
            message.Mnc_4g = mnc_4g;
            message.Eci_4g = eci_4g;
            message.TAC = tac;
            message.Pcid_4g_1 = pcid_4g_1;
            message.Pcid_4g_2 = pcid_4g_2;
            message.Pcid_4g_3 = pcid_4g_3;
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
            return message;
        }

    }
}
