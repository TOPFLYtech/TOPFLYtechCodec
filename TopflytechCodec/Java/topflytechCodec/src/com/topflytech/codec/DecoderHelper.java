package com.topflytech.codec;

import com.topflytech.codec.entities.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class DecoderHelper {

    public static Message parseLocationMessage(byte[] bytes) {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        int packageLen = BytesUtils.bytes2Short(bytes, 3);
        int positionIndex = 15;
        byte checkSum = bytes[positionIndex];
        positionIndex ++;
//        byte[] calCheckSumBytes = Arrays.copyOfRange(bytes,positionIndex,bytes.length);
//        byte crc8Value = BytesUtils.tftCrc8(calCheckSumBytes);
//        if(checkSum != crc8Value){
//            DebugMessage debugMessage = new DebugMessage();
//            debugMessage.setSerialNo(serialNo);
//            debugMessage.setImei(imei);
//            debugMessage.setOrignBytes(bytes);
//            return debugMessage;
//        }
        byte msgInfo = bytes[positionIndex];
        positionIndex++;
        boolean isHistory = (msgInfo & 0x80) == 0x80;
        boolean needAck = (msgInfo & 0x40) == 0x80;
        int encryptType =  MessageEncryptType.NONE;
        if((msgInfo & 0x30) == 0 ){
            encryptType = MessageEncryptType.NONE;
        }else if((msgInfo & 0x10) == 0x10){
            encryptType = MessageEncryptType.MD5;
        }else if((msgInfo & 0x20) == 0x20){
            encryptType = MessageEncryptType.AES;
        }
        if(encryptType == MessageEncryptType.AES){

        }else if(encryptType == MessageEncryptType.MD5){

        }
        Date date = TimeUtils.getGTM0Date(bytes, positionIndex);
        positionIndex+=6;
        HashMap<Integer,byte[]> type1Map = new HashMap<Integer, byte[]>();
        HashMap<Integer,byte[]> type2Map = new HashMap<Integer, byte[]>();
        HashMap<Integer,byte[]> type3Map = new HashMap<Integer, byte[]>();
        parseThreeTypeDataMap(bytes, packageLen, positionIndex, type1Map, type2Map, type3Map);
        LocationMessage locationMessage;
        if(bytes[2] == 0x64){
            locationMessage = new LocationAlarmMessage();
        }else{
            locationMessage = new LocationInfoMessage();
        }
        locationMessage.setOrignBytes(bytes);
        locationMessage.setImei(imei);
        locationMessage.setDate(date);
        locationMessage.setSerialNo(serialNo);
        locationMessage.setIsNeedResp(needAck);
        locationMessage.setEncryptType(encryptType);
        locationMessage.setProtocolHeadType(bytes[2]);
        locationMessage.setIsHistoryData(isHistory);
        parseLocationTypeOneData(locationMessage,type1Map);
        parseLocationTypeTwoData(locationMessage,type2Map);
        parseLocationTypeThreeData(locationMessage,type3Map);
        return locationMessage;
    }

    private static void parseThreeTypeDataMap(byte[] bytes, int packageLen, int positionIndex, HashMap<Integer, byte[]> type1Map, HashMap<Integer, byte[]> type2Map, HashMap<Integer, byte[]> type3Map) {
        while (positionIndex + 2 <= packageLen){
            byte dataType = bytes[positionIndex];
            positionIndex++;
            int columnLen = bytes[positionIndex];
            if(columnLen < 0){
                columnLen += 256;
            }
            if(dataType == 3){
                columnLen = BytesUtils.bytes2Short(bytes,positionIndex);
                positionIndex+=2;
            }else{
                positionIndex++;
            }


            if(dataType == 1){
                for(int i = 0;i < columnLen;i++){
                    if(positionIndex + 2 > packageLen){
                        positionIndex +=2;
                        break;
                    }
                    int id = bytes[positionIndex];
                    positionIndex++;
                    if(id < 0){
                        id += 256;
                    }
                    if (id <= 0x7f){
                        // one byte
                        type1Map.put(id, new byte[]{bytes[positionIndex]});
                        positionIndex++;
                    }else if(id >= 0x80 && id <= 0xbf){
                        if(positionIndex + 2 > packageLen){
                            positionIndex +=2;
                            break;
                        }
                        // two bytes
                        type1Map.put(id,Arrays.copyOfRange(bytes, positionIndex, positionIndex +2));
                        positionIndex +=2;
                    }else if(id >= 0xc0 && id <= 0xef){
                        if(positionIndex + 4 > packageLen){
                            positionIndex +=2;
                            break;
                        }
                        //four bytes
                        type1Map.put(id,Arrays.copyOfRange(bytes, positionIndex, positionIndex +4));
                        positionIndex +=4;
                    }else if(id >= 0xf0){
                        if(positionIndex + 8 > packageLen){
                            positionIndex +=2;
                            break;
                        }
                        //8 bytes
                        type1Map.put(id,Arrays.copyOfRange(bytes, positionIndex, positionIndex +8));
                        positionIndex +=8;
                    }
                }
            }else if(dataType == 2){
                for(int i = 0;i < columnLen;i++){
                    if(positionIndex + 2 > packageLen){
                        positionIndex +=2;
                        break;
                    }
                    int id = bytes[positionIndex];
                    positionIndex++;
                    if(id < 0){
                        id += 256;
                    }
                    if (id <= 0x5f){
                        // one byte
                        type2Map.put(id, new byte[]{bytes[positionIndex]});
                        positionIndex++;
                    }else if(id >= 0x60 && id <= 0x9f){
                        if(positionIndex + 2 > packageLen){
                            positionIndex +=2;
                            break;
                        }
                        // two bytes
                        type2Map.put(id,Arrays.copyOfRange(bytes, positionIndex, positionIndex +2));
                        positionIndex +=2;
                    }else if(id >= 0xa0){
                        if(positionIndex + 4 > packageLen){
                            positionIndex +=2;
                            break;
                        }
                        //four bytes
                        type2Map.put(id,Arrays.copyOfRange(bytes, positionIndex, positionIndex +4));
                        positionIndex +=4;
                    }
                }
            }else if(dataType == 3){
                for(int i = 0;i < columnLen;i++){
                    if(positionIndex + 3 > packageLen){
                        positionIndex +=3;
                        break;
                    }
                    int id = BytesUtils.bytes2Short(bytes, positionIndex);
                    positionIndex +=2;
                    int dataLen = bytes[positionIndex];
                    if(dataLen < 0){
                        dataLen += 256;
                    }
                    positionIndex++;
                    if(positionIndex + dataLen > packageLen){
                        positionIndex += dataLen;
                        break;
                    }
                    byte[] dataContent = Arrays.copyOfRange(bytes, positionIndex, positionIndex + dataLen);
                    positionIndex += dataLen;
                    type3Map.put(id,dataContent);
                }
            }
        }
    }

    private static void parseLocationTypeThreeData(LocationMessage locationMessage, HashMap<Integer, byte[]> typeMap) {
        for (Integer dataId:
                typeMap.keySet()) {
            if(dataId == 0x01){
                byte[] valueByte = typeMap.get(dataId);

                double altitude = BytesUtils.bytes2Float(valueByte, 0);
                double longitude = BytesUtils.bytes2Float(valueByte, 4);
                double latitude = BytesUtils.bytes2Float(valueByte, 8);
                int azimuth = BytesUtils.bytes2Short(valueByte, 12);
                int satelliteCount = valueByte[16];
                if(satelliteCount < 0){
                    satelliteCount += 256;
                }
                locationMessage.setLatlngValid(true);
                locationMessage.setAltitude(altitude);
                locationMessage.setLatitude(latitude);
                locationMessage.setLongitude(longitude);
                locationMessage.setAzimuth(azimuth);
                locationMessage.setSatelliteNumber(satelliteCount);
            }else if(dataId ==0x02){
                byte[] valueByte = typeMap.get(dataId);
                int mcc_2g = BytesUtils.bytes2Short(valueByte,0);
                int mnc_2g = BytesUtils.bytes2Short(valueByte,2);
                int lac_2g_1 = BytesUtils.bytes2Short(valueByte,4);
                int ci_2g_1 = BytesUtils.bytes2Short(valueByte,6);
                int lac_2g_2 = BytesUtils.bytes2Short(valueByte,8);
                int ci_2g_2 = BytesUtils.bytes2Short(valueByte,10);
                int lac_2g_3 = BytesUtils.bytes2Short(valueByte,12);
                int ci_2g_3 = BytesUtils.bytes2Short(valueByte,14);
                locationMessage.setIs_2g_lbs(true);
                locationMessage.setMcc_2g(mcc_2g);
                locationMessage.setMnc_2g(mnc_2g);
                locationMessage.setLac_2g_1(lac_2g_1);
                locationMessage.setCi_2g_1(ci_2g_1);
                locationMessage.setLac_2g_2(lac_2g_2);
                locationMessage.setCi_2g_2(ci_2g_2);
                locationMessage.setLac_2g_3(lac_2g_3);
                locationMessage.setCi_2g_3(ci_2g_3);
            }else if(dataId == 0x03){
                byte[] valueByte = typeMap.get(dataId);
                int mcc_4g = BytesUtils.bytes2Short(valueByte,0) & 0x7FFF;
                int mnc_4g = BytesUtils.bytes2Short(valueByte,2);
                long eci_4g = BytesUtils.unsigned4BytesToInt(valueByte, 4);
                int tac = BytesUtils.bytes2Short(valueByte, 8);
                int pcid_4g_1 = BytesUtils.bytes2Short(valueByte, 10);
                int pcid_4g_2 = BytesUtils.bytes2Short(valueByte, 12);
                int pcid_4g_3 = BytesUtils.bytes2Short(valueByte,14);
                locationMessage.setIs_4g_lbs(true);
                locationMessage.setMcc_4g(mcc_4g);
                locationMessage.setMnc_4g(mnc_4g);
                locationMessage.setEci_4g(eci_4g);
                locationMessage.setTac(tac);
                locationMessage.setPcid_4g_1(pcid_4g_1);
                locationMessage.setPcid_4g_2(pcid_4g_2);
                locationMessage.setPcid_4g_3(pcid_4g_3);
            }else if(dataId == 0x04){
                byte[] valueByte = typeMap.get(dataId);
                String selfMac =  BytesUtils.bytes2HexString(Arrays.copyOfRange(valueByte, 0, 6), 0);
                String ap1Mac =  BytesUtils.bytes2HexString(Arrays.copyOfRange(valueByte, 6, 12), 0);
                int ap1Rssi = (int)valueByte[12];
                String ap2Mac =  BytesUtils.bytes2HexString(Arrays.copyOfRange(valueByte,13,19),0);
                int ap2Rssi = (int)valueByte[19];
                String ap3Mac =  BytesUtils.bytes2HexString(Arrays.copyOfRange(valueByte,20,26),0);
                int ap3Rssi = (int)valueByte[26];
                locationMessage.setSelfMac(selfMac.toUpperCase());
                locationMessage.setAp1Mac(ap1Mac.toUpperCase());
                locationMessage.setAp1RSSI(ap1Rssi);
                locationMessage.setAp2Mac(ap2Mac.toUpperCase());
                locationMessage.setAp2RSSI(ap2Rssi);
                locationMessage.setAp3Mac(ap3Mac.toUpperCase());
                locationMessage.setAp3RSSI(ap3Rssi);
            }else if(dataId == 0x05){
                byte[] valueByte = typeMap.get(dataId);
                byte[] axisXByte = Arrays.copyOfRange(valueByte,0,2);
                BigInteger axisXB = new BigInteger(axisXByte);
                byte[] axisYByte = Arrays.copyOfRange(valueByte,2,4);
                BigInteger axisYB = new BigInteger(axisYByte);
                byte[] axisZByte = Arrays.copyOfRange(valueByte,4,6);
                BigInteger axisZB = new BigInteger(axisZByte);
                int axisX = axisXB.shortValue();
                int axisY = axisYB.shortValue();
                int axisZ =  axisZB.shortValue();
                locationMessage.setAxisX(axisX);
                locationMessage.setAxisY(axisY);
                locationMessage.setAxisZ(axisZ);
            }else if(dataId == 0x06){
                byte[] valueByte = typeMap.get(dataId);
                byte[] gyroscopeAxisXByte = Arrays.copyOfRange(valueByte,0,2);
                BigInteger gyroscopeAxisXB = new BigInteger(gyroscopeAxisXByte);
                byte[] gyroscopeAxisYByte = Arrays.copyOfRange(valueByte,2,4);
                BigInteger gyroscopeAxisYB = new BigInteger(gyroscopeAxisYByte);
                byte[] gyroscopeAxisZByte = Arrays.copyOfRange(valueByte, 4, 6);
                BigInteger gyroscopeAxisZB = new BigInteger(gyroscopeAxisZByte);
                int gyroscopeAxisX =  gyroscopeAxisXB.shortValue();
                int gyroscopeAxisY =  gyroscopeAxisYB.shortValue();
                int gyroscopeAxisZ =  gyroscopeAxisZB.shortValue();
                locationMessage.setGyroscopeAxisX(gyroscopeAxisX);
                locationMessage.setGyroscopeAxisY(gyroscopeAxisY);
                locationMessage.setGyroscopeAxisZ(gyroscopeAxisZ);
            }else if(dataId == 0x07){
                byte[] valueByte = typeMap.get(dataId);
                long accumulatingFuelConsumption = BytesUtils.unsigned4BytesToInt(valueByte, 0);
                if(accumulatingFuelConsumption == 4294967295l){
                    accumulatingFuelConsumption = -999;
                }
                long instantFuelConsumption =  BytesUtils.unsigned4BytesToInt(valueByte, 4);
                if(instantFuelConsumption == 4294967295l){
                    instantFuelConsumption = -999;
                }
                int rpm = BytesUtils.bytes2Short(valueByte, 8);
                if(rpm == 65535){
                    rpm = -999;
                }
                int airInput = (int)valueByte[10] < 0 ? (int)valueByte[10] + 256 : (int)valueByte[10];
                if(airInput == 255){
                    airInput = -999;
                }
                int airPressure = (int)valueByte[11] < 0 ? (int)valueByte[11] + 256 : (int)valueByte[11];
                if(airPressure == 255){
                    airPressure = -999;
                }
                int coolingFluidTemp = (int)valueByte[12] < 0 ? (int)valueByte[12] + 256 : (int)valueByte[12];
                if(coolingFluidTemp == 255){
                    coolingFluidTemp = -999;
                }else{
                    coolingFluidTemp = coolingFluidTemp - 40;
                }
                int airInflowTemp = (int)valueByte[13] < 0 ? (int)valueByte[13] + 256 : (int)valueByte[13];
                if(airInflowTemp == 255){
                    airInflowTemp = -999;
                }else {
                    airInflowTemp = airInflowTemp - 40;
                }
                int engineLoad = (int)valueByte[14] < 0 ? (int)valueByte[14] + 256 : (int)valueByte[14];
                if(engineLoad == 255){
                    engineLoad = -999;
                }
                int throttlePosition = (int)valueByte[15] < 0 ? (int)valueByte[15] + 256 : (int)valueByte[15];
                if(throttlePosition == 255){
                    throttlePosition = -999;
                }
                int remainFuelRate = valueByte[16] & 0x7f;
                int remainFuelUnit = (valueByte[16] & 0x80) == 0x80 ? 1 : 0;
                if(valueByte[16] == -1){ // == 0xff
                    remainFuelRate = -999;
                    remainFuelUnit = -999;
                }
                locationMessage.setAccumulatingFuelConsumption(accumulatingFuelConsumption);
                locationMessage.setInstantFuelConsumption(instantFuelConsumption);
                locationMessage.setRpm(rpm);
                locationMessage.setAirInflowTemp(airInflowTemp);
                locationMessage.setAirInput(airInput);
                locationMessage.setAirPressure(airPressure);
                locationMessage.setCoolingFluidTemp(coolingFluidTemp);
                locationMessage.setEngineLoad(engineLoad);
                locationMessage.setThrottlePosition(throttlePosition);
                locationMessage.setRemainFuelRate(remainFuelRate);
                locationMessage.setRemainFuelUnit(remainFuelUnit);
            }else if(dataId == 0x08){
                byte[] valueByte = typeMap.get(dataId);
                Integer remainPower = (int)valueByte[0] < 0 ? (int)valueByte[0] + 256 : (int)valueByte[0];
                if(remainPower == 255){
                    remainPower = -999;
                }
                Boolean isCarCharge = valueByte[1] == 0x01;
                Integer dashboardSpeed =  (int)valueByte[2] < 0 ? (int)valueByte[2] + 256 : (int)valueByte[2];
                if(dashboardSpeed == 255){
                    dashboardSpeed = -999;
                }
                Integer acceleratorPedalPosition = (int)valueByte[3] < 0 ? (int)valueByte[3] + 256 : (int)valueByte[3];
                if(acceleratorPedalPosition == 255){
                    acceleratorPedalPosition = -999;
                }
                Long remainPowerMinDistance = BytesUtils.unsigned4BytesToInt(valueByte,4);
                if(remainPowerMinDistance == 4294967295l){
                    remainPowerMinDistance = -999l;
                }
                Long remainPowerMaxDistance = BytesUtils.unsigned4BytesToInt(valueByte,8);
                if(remainPowerMaxDistance == 4294967295l){
                    remainPowerMaxDistance = -999l;
                }
                long carChargeVoltageTemp = BytesUtils.unsigned4BytesToInt(valueByte, 12);
                Float carChargeVoltage = carChargeVoltageTemp / 1000.0f;// V
                if(carChargeVoltageTemp == 4294967295l){
                    carChargeVoltage = -999f;
                }

                Long carChargeElectricCurrent = BytesUtils.unsigned4BytesToInt(valueByte,16);//mA
                if(carChargeElectricCurrent == 4294967295l){
                    carChargeElectricCurrent = -999l;
                }
                Long carChargePower = BytesUtils.unsigned4BytesToInt(valueByte,20);//mW
                if(carChargePower == 4294967295l){
                    carChargePower = -999l;
                }
                Long fullRemainingTime = BytesUtils.unsigned4BytesToInt(valueByte,24); //s
                if(fullRemainingTime == 4294967295l){
                    fullRemainingTime = -999l;
                }
                Long carBatteryEffectiveCapacity = BytesUtils.unsigned4BytesToInt(valueByte,28);//Wh
                if(carBatteryEffectiveCapacity == 4294967295l){
                    carBatteryEffectiveCapacity = -999l;
                }
                Long carBatteryInitialCapacity = BytesUtils.unsigned4BytesToInt(valueByte,32);//Wh
                if(carBatteryInitialCapacity == 4294967295l){
                    carBatteryInitialCapacity = -999l;
                }
                Long carTotalPowerConsumption = BytesUtils.unsigned4BytesToInt(valueByte,36);//Wh
                if(carTotalPowerConsumption == 4294967295l){
                    carTotalPowerConsumption = -999l;
                }
                locationMessage.setRemainPower(remainPower);
                locationMessage.setCarCharge(isCarCharge);
                locationMessage.setDashboardSpeed(dashboardSpeed);
                locationMessage.setAcceleratorPedalPosition(acceleratorPedalPosition);
                locationMessage.setRemainPowerMinDistance(remainPowerMinDistance);
                locationMessage.setRemainPowerMaxDistance(remainPowerMaxDistance);
                locationMessage.setCarChargeVoltage(carChargeVoltage);
                locationMessage.setCarChargeElectricCurrent(carChargeElectricCurrent);
                locationMessage.setCarChargePower(carChargePower);
                locationMessage.setFullRemainingTime(fullRemainingTime);
                locationMessage.setCarBatteryEffectiveCapacity(carBatteryEffectiveCapacity);
                locationMessage.setCarBatteryInitialCapacity(carBatteryInitialCapacity);
                locationMessage.setCarTotalPowerConsumption(carTotalPowerConsumption);
            }
        }
    }

    private static void parseLocationTypeTwoData(LocationMessage locationMessage, HashMap<Integer, byte[]> typeMap) {
        for (Integer dataId:
                typeMap.keySet()) {
            if(dataId == 0x01){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }
                locationMessage.setOriginalAlarmCode(value);
            }else if(dataId == 0x02){
                byte[] valueByte = typeMap.get(dataId);
                byte value = valueByte[0];
                locationMessage.setGpsWorking((value & 0x80) == 0x80);
                boolean gpsEnable = (value & 0x40) == 0x40;
                locationMessage.setGpsEnable(gpsEnable);
            }else if(dataId == 0x03){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }
                locationMessage.setNetworkSignal(value);
            } else if(dataId == 0x04){
                byte[] valueByte = typeMap.get(dataId);
                byte value = valueByte[0];
                //dout
                locationMessage.setOutput1((value & 0x01) == 0x01 ? 1 : 0);
                locationMessage.setOutput2((value & 0x02) == 0x02 ? 1 : 0);
                locationMessage.setOutput3((value & 0x04) == 0x04 ? 1 : 0);
                locationMessage.setOutput4((value & 0x08) == 0x08 ? 1 : 0);
                locationMessage.setOutput5((value & 0x10) == 0x10 ? 1 : 0);
                locationMessage.setOutput6((value & 0x20) == 0x20 ? 1 : 0);
                locationMessage.setOutput7((value & 0x40) == 0x40 ? 1 : 0);
                locationMessage.setOutput8((value & 0x80) == 0x80 ? 1 : 0);
            }else if(dataId == 0x05){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }
                //din
                locationMessage.setInput1((value & 0x01) == 0x01 ? 1 : 0);
                locationMessage.setInput2((value & 0x02) == 0x02 ? 1 : 0);
                locationMessage.setInput3((value & 0x04) == 0x04 ? 1 : 0);
                locationMessage.setInput4((value & 0x08) == 0x08 ? 1 : 0);
                locationMessage.setInput5((value & 0x10) == 0x10 ? 1 : 0);
                locationMessage.setInput6((value & 0x20) == 0x20 ? 1 : 0);
                locationMessage.setInput7((value & 0x40) == 0x40 ? 1 : 0);
                locationMessage.setInput8((value & 0x80) == 0x80 ? 1 : 0);
            }else if(dataId == 0x06){
                byte[] valueByte = typeMap.get(dataId);
                byte value = valueByte[0];
                locationMessage.setHasThirdPartyObd((value & 0x80) == 0x80 ? 1 : 0);
                locationMessage.setExPowerConsumpStatus((value & 0x40) == 0x40 ? 1 : 0);
            }else if(dataId == 0x07){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }
                locationMessage.setBatteryCharge(value);
            } else if(dataId == 0x09){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }

            } else if(dataId == 0x0A){
                byte[] valueByte = typeMap.get(dataId);
                float deviceTemp = -999;
                if (  valueByte[0] != 0xff){
                    deviceTemp = ( valueByte[0] & 0x7F) * (( valueByte[0] & 0x80) == 0x80 ? -1 : 1);
                }
                locationMessage.setDeviceTemp(deviceTemp);
            }  else if(dataId == 0x0B){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }

            } else if(dataId == 0x0C){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }

            } else if(dataId == 0x0D){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }

            } else if(dataId == 0x0E){
                byte[] valueByte = typeMap.get(dataId);
                boolean batteryCanRecharge = (valueByte[0] & 0x80) == 0x80;
                locationMessage.setBatteryCanRecharge(batteryCanRecharge);
            }else if(dataId == 0x0F){
                byte[] valueByte = typeMap.get(dataId);
                Integer lockType = (int)valueByte[0];
                locationMessage.setLockType(lockType);
            }else if(dataId == 0x60){
                byte[] valueByte = typeMap.get(dataId);
                int voltage = BytesUtils.bytes2Short(valueByte,0);
                locationMessage.setBatteryVoltage(voltage/1000.0f);
            } else if(dataId == 0x61){
                byte[] valueByte = typeMap.get(dataId);
                boolean accOn = (valueByte[0] & 0x80) == 0x80;
                boolean acOn = (valueByte[0] & 0x40) == 0x40;
                boolean externalPowerOn = (valueByte[0] & 0x20) == 0x20;
                boolean usbConnect = (valueByte[0] & 0x10) == 0x10;
                boolean isSolarCharge = (valueByte[0] & 0x08) == 0x08;
                boolean isSmartUpload = (valueByte[0] & 0x04) == 0x04;
                locationMessage.setIopIgnition(accOn);
                locationMessage.setIOP(accOn ? 0x4000l : 0x0000l);
                locationMessage.setIopACOn(acOn);
                locationMessage.setIsUsbCharging(usbConnect);
                locationMessage.setIsSolarCharging(isSolarCharge);
                String smartPowerOpenStatus = "close";
                if(isSmartUpload){
                    smartPowerOpenStatus = "open";
                }
                locationMessage.setSmartPowerOpenStatus(smartPowerOpenStatus);
            }else if(dataId == 0x62){
                byte[] valueByte = typeMap.get(dataId);
                int speed = BytesUtils.bytes2Short(valueByte,0);
                locationMessage.setSpeed((float)speed);
            }else if(dataId == 0x63){
                byte[] valueByte = typeMap.get(dataId);
                int solarVoltage = BytesUtils.bytes2Short(valueByte,0);
                locationMessage.setSolarVoltage(solarVoltage/ 10.0f);
            }else if(dataId == 0x64){
                byte[] valueByte = typeMap.get(dataId);
                boolean isNegative = (valueByte[0] & 0x80) == 0x80;
                float value = (isNegative ? -1 : 1) * ((valueByte[0] & 0x7f) + (valueByte[1] /100.0f));
                locationMessage.setExternalTemp(value);
            }else if(dataId == 0x65){
                byte[] valueByte = typeMap.get(dataId);
                boolean isNegative = (valueByte[0] & 0x80) == 0x80;
                float value = (isNegative ? -1 : 1) * ((valueByte[0] & 0x7f) + (valueByte[1] /100.0f));
                locationMessage.setExternalHumidity(value);
            }else if(dataId == 0xA0){
                byte[] valueByte = typeMap.get(dataId);
                long mileage = BytesUtils.unsigned4BytesToInt(valueByte,0);
                locationMessage.setMileage(mileage);
            }else if(dataId == 0xA1){
                byte[] valueByte = typeMap.get(dataId);
                long externalVoltage = BytesUtils.unsigned4BytesToInt(valueByte,0);
                locationMessage.setExternalPowerVoltage(externalVoltage / 1000.0f);
            }else if(dataId == 0xA2){
                byte[] valueByte = typeMap.get(dataId);
                long lightIntensity = BytesUtils.unsigned4BytesToInt(valueByte,0);
                locationMessage.setLightIntensityValue((int)lightIntensity);
            }
        }
    }

    private static void parseLocationTypeOneData(LocationMessage locationMessage, HashMap<Integer, byte[]> typeMap) {
        for (Integer dataId:
                typeMap.keySet()) {



            if(dataId == 0x03){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }
                locationMessage.setAngleCompensation(value);
            }else if(dataId == 0x04){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }
                locationMessage.setOverSpeedLimit(value);
            }else if(dataId == 0x05){
                byte[] valueByte = typeMap.get(dataId);
                byte value = valueByte[0];
                boolean isManagerConfigured1 = (value & 0x01) == 0x01;
                boolean isManagerConfigured2 = (value & 0x02) == 0x02;
                boolean isManagerConfigured3 = (value & 0x04) == 0x04;
                boolean isManagerConfigured4 = (value & 0x08) == 0x08;
                locationMessage.setIsManagerConfigured1(isManagerConfigured1);
                locationMessage.setIsManagerConfigured2(isManagerConfigured2);
                locationMessage.setIsManagerConfigured3(isManagerConfigured3);
                locationMessage.setIsManagerConfigured4(isManagerConfigured4);
            }else if(dataId == 0x06){
                byte[] valueByte = typeMap.get(dataId);
                byte value = valueByte[0];
                boolean isLockDevice = (value & 0x80) == 0x80;
                boolean isLockSim = (value & 0x40) == 0x40;
                boolean isLockApn = (value & 0x20) == 0x20;
                locationMessage.setIsLockDevice(isLockDevice);
                locationMessage.setIsLockSim(isLockSim);
                locationMessage.setIsLockApn(isLockApn);
            }else if(dataId == 0x07){
                byte[] valueByte = typeMap.get(dataId);
                int value = valueByte[0];
                if(value < 0){
                    value += 256;
                }
                locationMessage.setIsSendSmsAlarmToManagerPhone(value == 1);
            }else if(dataId == 0x08){
                byte[] valueByte = typeMap.get(dataId);
                int jammerDetectionStatus = (valueByte[0] & 0xC);
                locationMessage.setJammerDetectionStatus(jammerDetectionStatus);
            }else if(dataId == 0x80){
                byte[] valueByte = typeMap.get(dataId);
                int distance = BytesUtils.bytes2Short(valueByte,0);
                locationMessage.setDistanceCompensation(distance);
            }else if(dataId == 0x81){
                byte[] valueByte = typeMap.get(dataId);
                int dragThreshold = BytesUtils.bytes2Short(valueByte,0);
                locationMessage.setDragThreshold(dragThreshold);
            }else if(dataId == 0x82){
                byte[] valueByte = typeMap.get(dataId);
                int heartbeatInterval = BytesUtils.bytes2Short(valueByte,0);
                locationMessage.setHeartbeatInterval(heartbeatInterval);
            }else if(dataId == 0xC0){
                byte[] valueByte = typeMap.get(dataId);
                long accOnInterval = BytesUtils.unsigned4BytesToInt(valueByte,0);
                locationMessage.setSamplingIntervalAccOn(accOnInterval);
            }else if(dataId == 0xC1){
                byte[] valueByte = typeMap.get(dataId);
                long accOffInterval = BytesUtils.unsigned4BytesToInt(valueByte,0);
                locationMessage.setSamplingIntervalAccOff(accOffInterval);
            }
        }
    }
}
