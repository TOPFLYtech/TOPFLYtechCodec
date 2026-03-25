using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using TopflytechCodec.Entities;

namespace TopflytechCodec
{
    public class DecoderHelper
    {
        public static Message ParseLocationMessage(byte[] bytes)
        {
            int serialNo = BytesUtils.Bytes2Short(bytes, 5);
            string imei = BytesUtils.IMEI.Decode(bytes, 7);
            int packageLen = BytesUtils.Bytes2Short(bytes, 3);
            int positionIndex = 15;
            byte checkSum = bytes[positionIndex];
            positionIndex++; 
            byte msgInfo = bytes[positionIndex];
            positionIndex++;
            bool isHistory = (msgInfo & 0x80) == 0x80;
            bool needAck = (msgInfo & 0x40) == 0x40;
            int encryptType = MessageEncryptType.NONE;
            if ((msgInfo & 0x30) == 0)
            {
                encryptType = MessageEncryptType.NONE;
            }
            else if ((msgInfo & 0x10) == 0x10)
            {
                encryptType = MessageEncryptType.MD5;
            }
            else if ((msgInfo & 0x20) == 0x20)
            {
                encryptType = MessageEncryptType.AES;
            }
            if (encryptType == MessageEncryptType.AES)
            {

            }
            else if (encryptType == MessageEncryptType.MD5)
            {

            }
            DateTime date = Utils.getGTM0Date(bytes, positionIndex);
            positionIndex += 6;
            Dictionary<int, byte[]> type1Map = new Dictionary<int, byte[]>();
            Dictionary<int, byte[]> type2Map = new Dictionary<int, byte[]>();
            Dictionary<int, byte[]> type3Map = new Dictionary<int, byte[]>();
            ParseThreeTypeDataMap(bytes, packageLen, positionIndex, type1Map, type2Map, type3Map);
            LocationMessage locationMessage;
            if (bytes[2] == 0x64)
            {
                locationMessage = new LocationAlarmMessage();
            }
            else
            {
                locationMessage = new LocationInfoMessage();
            }
            locationMessage.OrignBytes = bytes;
            locationMessage.Imei = imei;
            locationMessage.Date = date;
            locationMessage.SerialNo = serialNo;
            locationMessage.IsNeedResp = needAck;
            locationMessage.EncryptType = encryptType;
            locationMessage.ProtocolHeadType = bytes[2];
            locationMessage.IsHistoryData = isHistory;
            ParseLocationTypeOneData(locationMessage, type1Map);
            ParseLocationTypeTwoData(locationMessage, type2Map);
            ParseLocationTypeThreeData(locationMessage, type3Map);
            if (!locationMessage.LatlngValid && !CheckLatitudeAndLongtitudeIsZero(locationMessage.Latitude, locationMessage.Longitude))
            {
                locationMessage.LatlngValid = true;
            }
            return locationMessage;
        }

        private static void ParseThreeTypeDataMap(byte[] bytes, int packageLen, int positionIndex, Dictionary<int, byte[]> type1Map, Dictionary<int, byte[]> type2Map, Dictionary<int, byte[]> type3Map)
        {
            while (positionIndex + 2 <= packageLen)
            {
                byte dataType = bytes[positionIndex];
                positionIndex++;
                int columnLen = bytes[positionIndex];
                if (columnLen < 0)
                {
                    columnLen += 256;
                }
                if (dataType == 3)
                {
                    columnLen = BytesUtils.Bytes2Short(bytes, positionIndex);
                    positionIndex += 2;
                }
                else
                {
                    positionIndex++;
                }

                if (dataType == 1)
                {
                    for (int i = 0; i < columnLen; i++)
                    {
                        if (positionIndex + 2 > packageLen)
                        {
                            positionIndex += 2;
                            break;
                        }
                        int id = bytes[positionIndex];
                        positionIndex++;
                        if (id < 0)
                        {
                            id += 256;
                        }
                        if (id <= 0x7f)
                        {
                            // one byte
                            type1Map[id] = new byte[] { bytes[positionIndex] };
                            positionIndex++;
                        }
                        else if (id >= 0x80 && id <= 0xbf)
                        {
                            if (positionIndex + 2 > packageLen)
                            {
                                positionIndex += 2;
                                break;
                            }
                            // two bytes
                            type1Map[id] = new byte[2];
                            Array.Copy(bytes, positionIndex, type1Map[id], 0, 2);
                            positionIndex += 2;
                        }
                        else if (id >= 0xc0 && id <= 0xef)
                        {
                            if (positionIndex + 4 > packageLen)
                            {
                                positionIndex += 2;
                                break;
                            }
                            // four bytes
                            type1Map[id] = new byte[4];
                            Array.Copy(bytes, positionIndex, type1Map[id], 0, 4);
                            positionIndex += 4;
                        }
                        else if (id >= 0xf0)
                        {
                            if (positionIndex + 8 > packageLen)
                            {
                                positionIndex += 2;
                                break;
                            }
                            // eight bytes
                            type1Map[id] = new byte[8];
                            Array.Copy(bytes, positionIndex, type1Map[id], 0, 8);
                            positionIndex += 8;
                        }
                    }
                }
                else if (dataType == 2)
                {
                    for (int i = 0; i < columnLen; i++)
                    {
                        if (positionIndex + 2 > packageLen)
                        {
                            positionIndex += 2;
                            break;
                        }
                        int id = bytes[positionIndex];
                        positionIndex++;
                        if (id < 0)
                        {
                            id += 256;
                        }
                        if (id <= 0x5f)
                        {
                            // one byte
                            type2Map[id] = new byte[] { bytes[positionIndex] };
                            positionIndex++;
                        }
                        else if (id >= 0x60 && id <= 0x9f)
                        {
                            if (positionIndex + 2 > packageLen)
                            {
                                positionIndex += 2;
                                break;
                            }
                            // two bytes
                            type2Map[id] = new byte[2];
                            Array.Copy(bytes, positionIndex, type2Map[id], 0, 2);
                            positionIndex += 2;
                        }
                        else if (id >= 0xa0)
                        {
                            if (positionIndex + 4 > packageLen)
                            {
                                positionIndex += 2;
                                break;
                            }
                            // four bytes
                            type2Map[id] = new byte[4];
                            Array.Copy(bytes, positionIndex, type2Map[id], 0, 4);
                            positionIndex += 4;
                        }
                    }
                }
                else if (dataType == 3)
                {
                    for (int i = 0; i < columnLen; i++)
                    {
                        if (positionIndex + 3 > packageLen)
                        {
                            positionIndex += 3;
                            break;
                        }
                        int id = BytesUtils.Bytes2Short(bytes, positionIndex);
                        positionIndex += 2;
                        int dataLen = bytes[positionIndex];
                        if (dataLen < 0)
                        {
                            dataLen += 256;
                        }
                        positionIndex++;
                        if (positionIndex + dataLen > packageLen)
                        {
                            positionIndex += dataLen;
                            break;
                        }
                        byte[] dataContent = new byte[dataLen];
                        Array.Copy(bytes, positionIndex, dataContent, 0, dataLen);
                        positionIndex += dataLen;
                        type3Map[id] = dataContent;
                    }
                }
            }
        }
        private static bool IsAllFF(byte[] array)
        {
            if (array == null || array.Length == 0)
            {
                return false;
            }
            foreach (byte b in array)
            {
                if (b != 0xFF)
                {
                    return false;
                }
            }
            return true;
        }

        private static bool CheckLatitudeAndLongtitudeIsZero(double lat, double lng)
        {
            if (lat < -300 || lat > 300 || lng < -300 || lng > 300)
            {
                return true;
            }
            return lat == 0.0d && lng == 0.0d;
        }

        private static void ParseLocationTypeThreeData(LocationMessage locationMessage, Dictionary<int, byte[]> typeMap)
        {
            foreach (var dataId in typeMap.Keys)
            {
                if (dataId == 0x01)
                {
                    byte[] valueByte = typeMap[dataId];
                    if (IsAllFF(valueByte))
                    {
                        locationMessage.LatlngValid = false;
                        continue;
                    }
                    double altitude = BytesUtils.Bytes2Float(valueByte, 0);
                    double longitude = BytesUtils.Bytes2Float(valueByte, 4);
                    double latitude = BytesUtils.Bytes2Float(valueByte, 8);
                    int azimuth = BytesUtils.Bytes2Short(valueByte, 12);
                    int satelliteCount = valueByte[16];
                    if (satelliteCount < 0)
                    {
                        satelliteCount += 256;
                    }
                    locationMessage.LatlngValid = true;
                    locationMessage.Altitude = altitude;
                    locationMessage.Latitude = latitude;
                    locationMessage.Longitude = longitude;
                    locationMessage.Azimuth = azimuth;
                    locationMessage.SatelliteNumber = satelliteCount;
                }
                else if (dataId == 0x02)
                {
                    byte[] valueByte = typeMap[dataId];
                    int mcc_2g = BytesUtils.Bytes2Short(valueByte, 0);
                    int mnc_2g = BytesUtils.Bytes2Short(valueByte, 2);
                    int lac_2g_1 = BytesUtils.Bytes2Short(valueByte, 4);
                    int ci_2g_1 = BytesUtils.Bytes2Short(valueByte, 6);
                    int lac_2g_2 = BytesUtils.Bytes2Short(valueByte, 8);
                    int ci_2g_2 = BytesUtils.Bytes2Short(valueByte, 10);
                    int lac_2g_3 = BytesUtils.Bytes2Short(valueByte, 12);
                    int ci_2g_3 = BytesUtils.Bytes2Short(valueByte, 14);
                    locationMessage.Is_2g_lbs = true;
                    locationMessage.Mcc_2g = mcc_2g;
                    locationMessage.Mnc_2g = mnc_2g;
                    locationMessage.Lac_2g_1 = lac_2g_1;
                    locationMessage.Ci_2g_1 = ci_2g_1;
                    locationMessage.Lac_2g_2 = lac_2g_2;
                    locationMessage.Ci_2g_2 = ci_2g_2;
                    locationMessage.Lac_2g_3 = lac_2g_3;
                    locationMessage.Ci_2g_3 = ci_2g_3;
                }
                else if (dataId == 0x03)
                {
                    byte[] valueByte = typeMap[dataId];
                    int mcc_4g = BytesUtils.Bytes2Short(valueByte, 0) & 0x7FFF;
                    int mnc_4g = BytesUtils.Bytes2Short(valueByte, 2);
                    long eci_4g = BytesUtils.Byte2Int(valueByte, 4);
                    int tac = BytesUtils.Bytes2Short(valueByte, 8);
                    int pcid_4g_1 = BytesUtils.Bytes2Short(valueByte, 10);
                    int pcid_4g_2 = BytesUtils.Bytes2Short(valueByte, 12);
                    int pcid_4g_3 = BytesUtils.Bytes2Short(valueByte, 14);
                    locationMessage.Is_4g_lbs = true;
                    locationMessage.Mcc_4g = mcc_4g;
                    locationMessage.Mnc_4g = mnc_4g;
                    locationMessage.Eci_4g = eci_4g;
                    locationMessage.TAC = tac;
                    locationMessage.Pcid_4g_1 = pcid_4g_1;
                    locationMessage.Pcid_4g_2 = pcid_4g_2;
                    locationMessage.Pcid_4g_3 = pcid_4g_3;
                }
                else if (dataId == 0x04)
                {
                    byte[] valueByte = typeMap[dataId];
                    string selfMac = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(valueByte, 0, 6), 0);
                    string ap1Mac = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(valueByte, 6, 12), 0);
                    int ap1Rssi = (int)valueByte[12];
                    if (ap1Rssi == 255)
                    {
                        ap1Rssi = -999;
                    }
                    else if (ap1Rssi > 127)
                    {
                        ap1Rssi -= 256;
                    }
                    string ap2Mac = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(valueByte, 13, 19), 0);
                    int ap2Rssi = (int)valueByte[19];
                    if (ap2Rssi == 255)
                    {
                        ap2Rssi = -999;
                    }
                    else if (ap2Rssi > 127)
                    {
                        ap2Rssi -= 256;
                    }
                    string ap3Mac = BytesUtils.Bytes2HexString(Utils.ArrayCopyOfRange(valueByte, 20, 26), 0);
                    int ap3Rssi = (int)valueByte[26];
                    if (ap3Rssi == 255)
                    {
                        ap3Rssi = -999;
                    }
                    else if (ap3Rssi > 127)
                    {
                        ap3Rssi -= 256;
                    }
                    locationMessage.SelfMac = selfMac.ToUpper();
                    locationMessage.Ap1Mac = ap1Mac.ToUpper();
                    locationMessage.Ap1RSSI = ap1Rssi;
                    locationMessage.Ap2Mac = ap2Mac.ToUpper();
                    locationMessage.Ap2RSSI = ap2Rssi;
                    locationMessage.Ap3Mac = ap3Mac.ToUpper();
                    locationMessage.Ap3RSSI = ap3Rssi;
                }
                else if (dataId == 0x05)
                {
                    byte[] valueByte = typeMap[dataId];
                    byte[] axisXByte = Utils.ArrayCopyOfRange(valueByte, 0, 2) ;
                    int axisX = BytesUtils.bytes2SingleShort(axisXByte,0);
                    byte[] axisYByte = Utils.ArrayCopyOfRange(valueByte, 2, 4) ;
                    int axisY = BytesUtils.bytes2SingleShort(axisYByte, 0);
                    byte[] axisZByte = Utils.ArrayCopyOfRange(valueByte, 4, 6);
                    int axisZ = BytesUtils.bytes2SingleShort(axisZByte, 0);
                    locationMessage.AxisX = axisX;
                    locationMessage.AxisY = axisY;
                    locationMessage.AxisZ = axisZ;
                }
                else if (dataId == 0x06)
                {
                    byte[] valueByte = typeMap[dataId];
                    byte[] gyroscopeAxisXByte = Utils.ArrayCopyOfRange(valueByte, 0, 2);
                    int gyroscopeAxisX = BytesUtils.bytes2SingleShort(gyroscopeAxisXByte, 0);
                    byte[] gyroscopeAxisYByte = Utils.ArrayCopyOfRange(valueByte, 2, 4);
                    int gyroscopeAxisY = BytesUtils.bytes2SingleShort(gyroscopeAxisYByte, 0);
                    byte[] gyroscopeAxisZByte = Utils.ArrayCopyOfRange(valueByte, 4, 6);
                    int gyroscopeAxisZ = BytesUtils.bytes2SingleShort(gyroscopeAxisZByte, 0);
                    locationMessage.GyroscopeAxisX = gyroscopeAxisX;
                    locationMessage.GyroscopeAxisY = gyroscopeAxisY;
                    locationMessage.GyroscopeAxisZ = gyroscopeAxisZ;
                }
                else if (dataId == 0x07)
                {
                    byte[] valueByte = typeMap[dataId];
                    long accumulatingFuelConsumption = BytesUtils.Byte2Int(valueByte, 0);
                    if (accumulatingFuelConsumption == 4294967295L)
                    {
                        accumulatingFuelConsumption = -999;
                    }
                    long instantFuelConsumption = BytesUtils.Byte2Int(valueByte, 4);
                    if (instantFuelConsumption == 4294967295L)
                    {
                        instantFuelConsumption = -999;
                    }
                    int rpm = BytesUtils.Bytes2Short(valueByte, 8);
                    if (rpm == 65535)
                    {
                        rpm = -999;
                    }
                    int airInput = (int)valueByte[10] < 0 ? (int)valueByte[10] + 256 : (int)valueByte[10];
                    if (airInput == 255)
                    {
                        airInput = -999;
                    }
                    int airPressure = (int)valueByte[11] < 0 ? (int)valueByte[11] + 256 : (int)valueByte[11];
                    if (airPressure == 255)
                    {
                        airPressure = -999;
                    }
                    int coolingFluidTemp = (int)valueByte[12] < 0 ? (int)valueByte[12] + 256 : (int)valueByte[12];
                    if (coolingFluidTemp == 255)
                    {
                        coolingFluidTemp = -999;
                    }
                    else
                    {
                        coolingFluidTemp = coolingFluidTemp - 40;
                    }
                    int airInflowTemp = (int)valueByte[13] < 0 ? (int)valueByte[13] + 256 : (int)valueByte[13];
                    if (airInflowTemp == 255)
                    {
                        airInflowTemp = -999;
                    }
                    else
                    {
                        airInflowTemp = airInflowTemp - 40;
                    }
                    int engineLoad = (int)valueByte[14] < 0 ? (int)valueByte[14] + 256 : (int)valueByte[14];
                    if (engineLoad == 255)
                    {
                        engineLoad = -999;
                    }
                    int throttlePosition = (int)valueByte[15] < 0 ? (int)valueByte[15] + 256 : (int)valueByte[15];
                    if (throttlePosition == 255)
                    {
                        throttlePosition = -999;
                    }
                    int remainFuelRate = valueByte[16] & 0x7f;
                    int remainFuelUnit = (valueByte[16] & 0x80) == 0x80 ? 1 : 0;
                    if (valueByte[16] == 0xFF)
                    {
                        remainFuelRate = -999;
                        remainFuelUnit = -999;
                    }
                    locationMessage.IsObdElectricData = false;
                    locationMessage.AccumulatingFuelConsumption = accumulatingFuelConsumption;
                    locationMessage.InstantFuelConsumption = instantFuelConsumption;
                    locationMessage.Rpm = rpm;
                    locationMessage.AirInflowTemp = airInflowTemp;
                    locationMessage.AirInput = airInput;
                    locationMessage.AirPressure = airPressure;
                    locationMessage.CoolingFluidTemp = coolingFluidTemp;
                    locationMessage.EngineLoad = engineLoad;
                    locationMessage.ThrottlePosition = throttlePosition;
                    locationMessage.RemainFuelRate = remainFuelRate;
                    locationMessage.RemainFuelUnit = remainFuelUnit;
                }
                else if (dataId == 0x08)
                {
                    byte[] valueByte = typeMap[dataId];
                    int remainPower = (int)valueByte[0] < 0 ? (int)valueByte[0] + 256 : (int)valueByte[0];
                    if (remainPower == 255)
                    {
                        remainPower = -999;
                    }
                    bool isCarCharge = valueByte[1] == 0x01;
                    int dashboardSpeed = (int)valueByte[2] < 0 ? (int)valueByte[2] + 256 : (int)valueByte[2];
                    if (dashboardSpeed == 255)
                    {
                        dashboardSpeed = -999;
                    }
                    int acceleratorPedalPosition = (int)valueByte[3] < 0 ? (int)valueByte[3] + 256 : (int)valueByte[3];
                    if (acceleratorPedalPosition == 255)
                    {
                        acceleratorPedalPosition = -999;
                    }
                    long remainPowerMinDistance = BytesUtils.Byte2Int(valueByte, 4);
                    if (remainPowerMinDistance == 4294967295L)
                    {
                        remainPowerMinDistance = -999L;
                    }
                    long remainPowerMaxDistance = BytesUtils.Byte2Int(valueByte, 8);
                    if (remainPowerMaxDistance == 4294967295L)
                    {
                        remainPowerMaxDistance = -999L;
                    }
                    long carChargeVoltageTemp = BytesUtils.Byte2Int(valueByte, 12);
                    float carChargeVoltage = carChargeVoltageTemp / 1000.0f;
                    if (carChargeVoltageTemp == 4294967295L)
                    {
                        carChargeVoltage = -999f;
                    }
                    long carChargeElectricCurrent = BytesUtils.Byte2Int(valueByte, 16);
                    if (carChargeElectricCurrent == 4294967295L)
                    {
                        carChargeElectricCurrent = -999L;
                    }
                    long carChargePower = BytesUtils.Byte2Int(valueByte, 20);
                    if (carChargePower == 4294967295L)
                    {
                        carChargePower = -999L;
                    }
                    long fullRemainingTime = BytesUtils.Byte2Int(valueByte, 24);
                    if (fullRemainingTime == 4294967295L)
                    {
                        fullRemainingTime = -999L;
                    }
                    long carBatteryEffectiveCapacity = BytesUtils.Byte2Int(valueByte, 28);
                    if (carBatteryEffectiveCapacity == 4294967295L)
                    {
                        carBatteryEffectiveCapacity = -999L;
                    }
                    long carBatteryInitialCapacity = BytesUtils.Byte2Int(valueByte, 32);
                    if (carBatteryInitialCapacity == 4294967295L)
                    {
                        carBatteryInitialCapacity = -999L;
                    }
                    long carTotalPowerConsumption = BytesUtils.Byte2Int(valueByte, 36);
                    if (carTotalPowerConsumption == 4294967295L)
                    {
                        carTotalPowerConsumption = -999L;
                    }
                    locationMessage.RemainPower = remainPower;
                    locationMessage.IsCarCharge = isCarCharge;
                    locationMessage.DashboardSpeed = dashboardSpeed;
                    locationMessage.AcceleratorPedalPosition = acceleratorPedalPosition;
                    locationMessage.IsObdElectricData = true;
                    locationMessage.RemainPowerDistance = remainPowerMinDistance;
                    locationMessage.RemainPowerMinDistance = remainPowerMinDistance;
                    locationMessage.RemainPowerMaxDistance = remainPowerMaxDistance;
                    locationMessage.CarChargeVoltage = carChargeVoltage;
                    locationMessage.CarChargeElectricCurrent = carChargeElectricCurrent;
                    locationMessage.CarChargePower = carChargePower;
                    locationMessage.FullRemainingTime = fullRemainingTime;
                    locationMessage.CarBatteryEffectiveCapacity = carBatteryEffectiveCapacity;
                    locationMessage.CarBatteryInitialCapacity = carBatteryInitialCapacity;
                    locationMessage.CarTotalPowerConsumption = carTotalPowerConsumption;
                }
                else if (dataId == 0x0D)
                {
                    byte[] valueByte = typeMap[dataId];
                    int remainPower = (int)valueByte[0] < 0 ? (int)valueByte[0] + 256 : (int)valueByte[0];
                    if (remainPower == 255)
                    {
                        remainPower = -999;
                    }
                    bool isCarCharge = valueByte[1] == 0x01;
                    int dashboardSpeed = (int)valueByte[2] < 0 ? (int)valueByte[2] + 256 : (int)valueByte[2];
                    if (dashboardSpeed == 255)
                    {
                        dashboardSpeed = -999;
                    }
                    int acceleratorPedalPosition = (int)valueByte[3] < 0 ? (int)valueByte[3] + 256 : (int)valueByte[3];
                    if (acceleratorPedalPosition == 255)
                    {
                        acceleratorPedalPosition = -999;
                    }
                    long remainPowerDistance = BytesUtils.Byte2Int(valueByte, 4);
                    if (remainPowerDistance == 4294967295L)
                    {
                        remainPowerDistance = -999L;
                    }
                    long carChargeVoltageTemp = BytesUtils.Byte2Int(valueByte, 8);
                    float carChargeVoltage = carChargeVoltageTemp / 1000.0f;
                    if (carChargeVoltageTemp == 4294967295L)
                    {
                        carChargeVoltage = -999f;
                    }
                    long carChargeElectricCurrent = BytesUtils.Byte2Int(valueByte, 12);
                    if (carChargeElectricCurrent == 4294967295L)
                    {
                        carChargeElectricCurrent = -999L;
                    }
                    long carBatteryEffectiveCapacity = BytesUtils.Byte2Int(valueByte, 16);
                    if (carBatteryEffectiveCapacity == 4294967295L)
                    {
                        carBatteryEffectiveCapacity = -999L;
                    }
                    long carBatteryInitialCapacity = BytesUtils.Byte2Int(valueByte, 20);
                    if (carBatteryInitialCapacity == 4294967295L)
                    {
                        carBatteryInitialCapacity = -999L;
                    }
                    long carTotalPowerConsumption = BytesUtils.Byte2Int(valueByte, 24);
                    if (carTotalPowerConsumption == 4294967295L)
                    {
                        carTotalPowerConsumption = -999L;
                    }
                    locationMessage.IsObdElectricData = true;
                    locationMessage.RemainPower = remainPower;
                    locationMessage.IsCarCharge = isCarCharge;
                    locationMessage.DashboardSpeed = dashboardSpeed;
                    locationMessage.AcceleratorPedalPosition = acceleratorPedalPosition;
                    locationMessage.RemainPowerDistance = remainPowerDistance;
                    locationMessage.CarChargeVoltage = carChargeVoltage;
                    locationMessage.CarChargeElectricCurrent = carChargeElectricCurrent;
                    locationMessage.CarBatteryEffectiveCapacity = carBatteryEffectiveCapacity;
                    locationMessage.CarBatteryInitialCapacity = carBatteryInitialCapacity;
                    locationMessage.CarTotalPowerConsumption = carTotalPowerConsumption;
                }
            }
        }

        private static void ParseLocationTypeTwoData(LocationMessage locationMessage, Dictionary<int, byte[]> typeMap)
        {
            foreach (var dataId in typeMap.Keys)
            {
                if (dataId == 0x01)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                    locationMessage.OriginalAlarmCode = value;
                }
                else if (dataId == 0x02)
                {
                    byte[] valueByte = typeMap[dataId];
                    byte value = valueByte[0];
                    locationMessage.GpsWorking = (value & 0x80) == 0x80;
                    bool gpsEnable = (value & 0x40) == 0x40;
                    locationMessage.GpsEnable = gpsEnable;
                    bool latlngValid = (value & 0x04) == 0x04;
                    locationMessage.LatlngValid = latlngValid;
                    bool isHistory = (value & 0x02) == 0x02;
                    locationMessage.IsHistoryData = isHistory;
                }
                else if (dataId == 0x03)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                    locationMessage.NetworkSignal = value;
                }
                else if (dataId == 0x04)
                {
                    byte[] valueByte = typeMap[dataId];
                    byte value = valueByte[0];
                    locationMessage.Output1 = (value & 0x01) == 0x01 ? 1 : 0;
                    locationMessage.Output2 = (value & 0x02) == 0x02 ? 1 : 0;
                    locationMessage.Output3 = (value & 0x04) == 0x04 ? 1 : 0;
                    locationMessage.Output4 = (value & 0x08) == 0x08 ? 1 : 0;
                    locationMessage.Output5 = (value & 0x10) == 0x10 ? 1 : 0;
                    locationMessage.Output6 = (value & 0x20) == 0x20 ? 1 : 0;
                    locationMessage.Output7 = (value & 0x40) == 0x40 ? 1 : 0;
                    locationMessage.Output8 = (value & 0x80) == 0x80 ? 1 : 0;
                }
                else if (dataId == 0x05)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                    locationMessage.Input1 = (value & 0x01) == 0x01 ? 1 : 0;
                    locationMessage.Input2 = (value & 0x02) == 0x02 ? 1 : 0;
                    locationMessage.Input3 = (value & 0x04) == 0x04 ? 1 : 0;
                    locationMessage.Input4 = (value & 0x08) == 0x08 ? 1 : 0;
                    locationMessage.Input5 = (value & 0x10) == 0x10 ? 1 : 0;
                    locationMessage.Input6 = (value & 0x20) == 0x20 ? 1 : 0;
                    locationMessage.Input7 = (value & 0x40) == 0x40 ? 1 : 0;
                    locationMessage.Input8 = (value & 0x80) == 0x80 ? 1 : 0;
                }
                else if (dataId == 0x06)
                {
                    byte[] valueByte = typeMap[dataId];
                    byte value = valueByte[0];
                    locationMessage.HasThirdPartyObd = (value & 0x80) == 0x80 ? 1 : 0;
                    locationMessage.ExPowerConsumpStatus = (value & 0x40) == 0x40 ? 1 : 0;
                    bool isEcuMileage = (value & 0x10) == 0x10;
                    locationMessage.MileageSource = isEcuMileage ? 1 : 0;
                }
                else if (dataId == 0x07)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                    locationMessage.BatteryCharge = value;
                }
                else if (dataId == 0x09)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                }
                else if (dataId == 0x0A)
                {
                    byte[] valueByte = typeMap[dataId];
                    float deviceTemp = -999;
                    if (valueByte[0] != 0xff)
                    {
                        deviceTemp = (valueByte[0] & 0x7F) * ((valueByte[0] & 0x80) == 0x80 ? -1 : 1);
                    }
                    locationMessage.DeviceTemp = deviceTemp;
                }
                else if (dataId == 0x0B)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                }
                else if (dataId == 0x0C)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                }
                else if (dataId == 0x0D)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                }
                else if (dataId == 0x0E)
                {
                    byte[] valueByte = typeMap[dataId];
                    bool batteryCanRecharge = (valueByte[0] & 0x80) == 0x80;
                    locationMessage.BatteryCanRecharge = batteryCanRecharge;
                }
                else if (dataId == 0x0F)
                {
                    byte[] valueByte = typeMap[dataId];
                    int lockType = (int)valueByte[0];
                    locationMessage.LockType = lockType;
                }
                else if (dataId == 0x10)
                {
                    byte[] valueByte = typeMap[dataId];
                    locationMessage.SmartPowerSettingStatus = (valueByte[0] & 0x80) == 0x80 ? "enable" : "disable";
                }
                else if (dataId == 0x11)
                {
                    byte[] valueByte = typeMap[dataId];
                    locationMessage.FlashLightOpen = (valueByte[0] & 0x80) == 0x80;
                    locationMessage.LogoLightOpen = (valueByte[0] & 0x40) == 0x40;
                    locationMessage.BuzzerOpen = (valueByte[0] & 0x20) == 0x20;
                    locationMessage.LostModeOpen = (valueByte[0] & 0x10) == 0x10;
                }
                else if (dataId == 0x12)
                {
                    byte[] valueByte = typeMap[dataId];
                    locationMessage.RelayStatus = valueByte[0] & 0xff;
                }
                else if (dataId == 0x13)
                {
                    byte[] valueByte = typeMap[dataId];
                    locationMessage.MileageSource = valueByte[0] == 0x01 ? 2 : 0;
                }
                else if (dataId == 0x14)
                {
                    byte[] valueByte = typeMap[dataId];
                    locationMessage.IgnitionSource = valueByte[0] & 0xff;
                }
                else if (dataId == 0x15)
                {
                    byte[] valueByte = typeMap[dataId];
                    locationMessage.ObdFuelType = valueByte[0] & 0xff;
                }
                else if (dataId == 0x16)
                {
                    byte[] valueByte = typeMap[dataId];
                    locationMessage.WifiScanInterval = valueByte[0] & 0xff;
                }
                else if (dataId == 0x60)
                {
                    byte[] valueByte = typeMap[dataId];
                    int voltage = BytesUtils.Bytes2Short(valueByte, 0);
                    locationMessage.BatteryVoltage = voltage / 1000.0f;
                }
                else if (dataId == 0x61)
                {
                    byte[] valueByte = typeMap[dataId];
                    bool accOn = (valueByte[0] & 0x80) == 0x80;
                    bool acOn = (valueByte[0] & 0x40) == 0x40;
                    bool externalPowerOn = (valueByte[0] & 0x20) == 0x20;
                    bool usbConnect = (valueByte[0] & 0x10) == 0x10;
                    bool isSolarCharge = (valueByte[0] & 0x08) == 0x08;
                    bool isSmartUpload = (valueByte[0] & 0x04) == 0x04;
                    bool isWirelessCharging = (valueByte[0] & 0x02) == 0x02;
                    locationMessage.IopIgnition = accOn;
                    locationMessage.IOP = accOn ? 0x4000L : 0x0000L;
                    locationMessage.IopACOn = acOn;
                    locationMessage.IsUsbCharging = usbConnect;
                    locationMessage.IsSolarCharging = isSolarCharge;
                    locationMessage.WirelessCharging = isWirelessCharging;
                    string smartPowerOpenStatus = "close";
                    if (isSmartUpload)
                    {
                        smartPowerOpenStatus = "open";
                    }
                    locationMessage.SmartPowerOpenStatus = smartPowerOpenStatus;
                }
                else if (dataId == 0x62)
                {
                    byte[] valueByte = typeMap[dataId];
                    int speed = BytesUtils.Bytes2Short(valueByte, 0);
                    locationMessage.Speed = (float)speed;
                }
                else if (dataId == 0x63)
                {
                    byte[] valueByte = typeMap[dataId];
                    int solarVoltage = BytesUtils.Bytes2Short(valueByte, 0);
                    locationMessage.SolarVoltage = solarVoltage / 1000.0f;
                }
                else if (dataId == 0x64)
                {
                    byte[] valueByte = typeMap[dataId];
                    if (!IsAllFF(valueByte))
                    {
                        bool isNegative = (valueByte[1] & 0x80) == 0x80;
                        float value = (isNegative ? -1 : 1) * (valueByte[0] + ((valueByte[1] & 0x7f) / 100.0f));
                        locationMessage.ExternalTemp = value;
                    }
                }
                else if (dataId == 0x65)
                {
                    byte[] valueByte = typeMap[dataId];
                    if (!IsAllFF(valueByte))
                    {
                        bool isNegative = (valueByte[0] & 0x80) == 0x80;
                        float value = (isNegative ? -1 : 1) * ((valueByte[0] & 0x7f) + (valueByte[1] / 100.0f));
                        locationMessage.ExternalHumidity = value;
                    }
                }
                else if (dataId == 0x66)
                {
                    byte[] valueByte = typeMap[dataId];
                    if (!IsAllFF(valueByte))
                    {
                        bool isNegative = (valueByte[1] & 0x80) == 0x80;
                        float value = (isNegative ? -1 : 1) * (valueByte[0] + ((valueByte[1] & 0x7f) / 100.0f));
                        locationMessage.DeviceHighPrecisionTemp = value;
                    }
                }
                else if (dataId == 0x67)
                {
                    byte[] valueByte = typeMap[dataId];
                    int voltage = BytesUtils.Bytes2Short(valueByte, 0);
                    locationMessage.AnalogInput1 = voltage / 1000.0f;
                }
                else if (dataId == 0x68)
                {
                    byte[] valueByte = typeMap[dataId];
                    int voltage = BytesUtils.Bytes2Short(valueByte, 0);
                    locationMessage.AnalogInput2 = voltage / 1000.0f;
                }
                else if (dataId == 0x69)
                {
                    byte[] valueByte = typeMap[dataId];
                    int voltage = BytesUtils.Bytes2Short(valueByte, 0);
                    locationMessage.AnalogInput3 = voltage / 1000.0f;
                }
                else if (dataId == 0xA0)
                {
                    byte[] valueByte = typeMap[dataId];
                    long mileage = BytesUtils.Byte2Int(valueByte, 0);
                    locationMessage.Mileage = mileage;
                }
                else if (dataId == 0xA1)
                {
                    byte[] valueByte = typeMap[dataId];
                    long externalVoltage = BytesUtils.Byte2Int(valueByte, 0);
                    locationMessage.ExternalPowerVoltage = externalVoltage / 1000.0f;
                }
                else if (dataId == 0xA2)
                {
                    byte[] valueByte = typeMap[dataId];
                    long lightIntensity = BytesUtils.Byte2Int(valueByte, 0);
                    locationMessage.LightIntensityValue = (int)lightIntensity;
                }
            }
        }

        private static void ParseLocationTypeOneData(LocationMessage locationMessage, Dictionary<int, byte[]> typeMap)
        {
            foreach (var dataId in typeMap.Keys)
            {
                if (dataId == 0x03)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                    locationMessage.AngleCompensation = value;
                }
                else if (dataId == 0x04)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                    locationMessage.OverspeedLimit = value;
                }
                else if (dataId == 0x05)
                {
                    byte[] valueByte = typeMap[dataId];
                    byte value = valueByte[0];
                    bool isManagerConfigured1 = (value & 0x01) == 0x01;
                    bool isManagerConfigured2 = (value & 0x02) == 0x02;
                    bool isManagerConfigured3 = (value & 0x04) == 0x04;
                    bool isManagerConfigured4 = (value & 0x08) == 0x08;
                    locationMessage.IsManagerConfigured1 = isManagerConfigured1;
                    locationMessage.IsManagerConfigured2 = isManagerConfigured2;
                    locationMessage.IsManagerConfigured3 = isManagerConfigured3;
                    locationMessage.IsManagerConfigured4 = isManagerConfigured4;
                }
                else if (dataId == 0x06)
                {
                    byte[] valueByte = typeMap[dataId];
                    byte value = valueByte[0];
                    bool isLockDevice = (value & 0x80) == 0x80;
                    bool isLockSim = (value & 0x40) == 0x40;
                    bool isLockApn = (value & 0x20) == 0x20;
                    locationMessage.IsLockDevice = isLockDevice;
                    locationMessage.IsLockSim = isLockSim;
                    locationMessage.IsLockApn = isLockApn;
                }
                else if (dataId == 0x07)
                {
                    byte[] valueByte = typeMap[dataId];
                    int value = valueByte[0];
                    if (value < 0)
                    {
                        value += 256;
                    }
                    locationMessage.IsSendSmsAlarmToManagerPhone = value == 1;
                }
                else if (dataId == 0x08)
                {
                    byte[] valueByte = typeMap[dataId];
                    int jammerDetectionStatus = (valueByte[0] & 0xC);
                    locationMessage.JammerDetectionStatus = jammerDetectionStatus;
                }
                else if (dataId == 0x09)
                {
                    byte[] valueByte = typeMap[dataId];
                    locationMessage.AntitheftedStatus = valueByte[0] & 0xff;
                }
                else if (dataId == 0x80)
                {
                    byte[] valueByte = typeMap[dataId];
                    int distance = BytesUtils.Bytes2Short(valueByte, 0);
                    locationMessage.DistanceCompensation = distance;
                }
                else if (dataId == 0x81)
                {
                    byte[] valueByte = typeMap[dataId];
                    int dragThreshold = BytesUtils.Bytes2Short(valueByte, 0);
                    locationMessage.DragThreshold = dragThreshold;
                }
                else if (dataId == 0x82)
                {
                    byte[] valueByte = typeMap[dataId];
                    int heartbeatInterval = BytesUtils.Bytes2Short(valueByte, 0);
                    locationMessage.HeartbeatInterval = heartbeatInterval;
                }
                else if (dataId == 0xC0)
                {
                    byte[] valueByte = typeMap[dataId];
                    long accOnInterval = BytesUtils.Byte2Int(valueByte, 0);
                    locationMessage.SamplingIntervalAccOn = accOnInterval;
                }
                else if (dataId == 0xC1)
                {
                    byte[] valueByte = typeMap[dataId];
                    long accOffInterval = BytesUtils.Byte2Int(valueByte, 0);
                    locationMessage.SamplingIntervalAccOff = accOffInterval;
                }
            }
        }
    }
}
