var ByteUtils = require("./ByteUtils")
var CryptoTool = require("./CryptoTool")
var DecoderHelper = {
    parseLocationMessage:function(bytes) {
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        const packageLen = ByteUtils.byteToShort(bytes, 3);
        let positionIndex = 15;
        const checkSum = bytes[positionIndex];
        positionIndex++;



        const msgInfo = bytes[positionIndex];
        positionIndex++;
        const isHistory = (msgInfo & 0x80) === 0x80;
        const needAck = (msgInfo & 0x40) === 0x80;
        let encryptType = CryptoTool.MessageEncryptType.NONE;

        if ((msgInfo & 0x30) === 0) {
            encryptType =  CryptoTool.MessageEncryptType.NONE;
        } else if ((msgInfo & 0x10) === 0x10) {
            encryptType =  CryptoTool.MessageEncryptType.MD5;
        } else if ((msgInfo & 0x20) === 0x20) {
            encryptType =  CryptoTool.MessageEncryptType.AES;
        }

        const date = ByteUtils.getGTM0Date(bytes, positionIndex);
        positionIndex += 6;

        const type1Map = new Map();
        const type2Map = new Map();
        const type3Map = new Map();
        this.parseThreeTypeDataMap(bytes, packageLen, positionIndex, type1Map, type2Map, type3Map);

        var locationMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"location",
            date:date,
            needResp:needAck,
            encryptType: encryptType,
            isHistoryData:isHistory,
            protocolHeadType:bytes[2]
        }

        this.parseLocationTypeOneData(locationMessage, type1Map);
        this.parseLocationTypeTwoData(locationMessage, type2Map);
        this.parseLocationTypeThreeData(locationMessage, type3Map);
        if(!locationMessage.latlngValid && locationMessage.latitude != undefined && locationMessage.longitude != undefined  ){
            if(!(locationMessage.latitude == 0 && locationMessage.longitude == 0)){
                locationMessage.latlngValid = false;
            }
        }
        return locationMessage;
    },
    parseThreeTypeDataMap:function (bytes, packageLen, positionIndex, type1Map, type2Map, type3Map) {
        while (positionIndex + 2 <= packageLen) {
            let dataType = bytes[positionIndex];
            positionIndex++;
            let columnLen = bytes[positionIndex];
            if (columnLen < 0) {
                columnLen += 256;
            }
            if (dataType === 3) {
                columnLen = ByteUtils.byteToShort(bytes, positionIndex);
                positionIndex += 2;
            } else {
                positionIndex++;
            }

            if (dataType === 1) {
                for (let i = 0; i < columnLen; i++) {
                    if (positionIndex + 2 > packageLen) {
                        positionIndex += 2;
                        break;
                    }
                    let id = bytes[positionIndex];
                    positionIndex++;
                    if (id < 0) {
                        id += 256;
                    }
                    if (id <= 0x7f) {
                        // one byte
                        type1Map.set(id, [bytes[positionIndex]]);
                        positionIndex++;
                    } else if (id >= 0x80 && id <= 0xbf) {
                        if (positionIndex + 2 > packageLen) {
                            positionIndex += 2;
                            break;
                        }
                        // two bytes
                        type1Map.set(id, bytes.slice(positionIndex, positionIndex + 2));
                        positionIndex += 2;
                    } else if (id >= 0xc0 && id <= 0xef) {
                        if (positionIndex + 4 > packageLen) {
                            positionIndex += 2;
                            break;
                        }
                        // four bytes
                        type1Map.set(id, bytes.slice(positionIndex, positionIndex + 4));
                        positionIndex += 4;
                    } else if (id >= 0xf0) {
                        if (positionIndex + 8 > packageLen) {
                            positionIndex += 2;
                            break;
                        }
                        // eight bytes
                        type1Map.set(id, bytes.slice(positionIndex, positionIndex + 8));
                        positionIndex += 8;
                    }
                }
            } else if (dataType === 2) {
                for (let i = 0; i < columnLen; i++) {
                    if (positionIndex + 2 > packageLen) {
                        positionIndex += 2;
                        break;
                    }
                    let id = bytes[positionIndex];
                    positionIndex++;
                    if (id < 0) {
                        id += 256;
                    }
                    if (id <= 0x5f) {
                        // one byte
                        type2Map.set(id, [bytes[positionIndex]]);
                        positionIndex++;
                    } else if (id >= 0x60 && id <= 0x9f) {
                        if (positionIndex + 2 > packageLen) {
                            positionIndex += 2;
                            break;
                        }
                        // two bytes
                        type2Map.set(id, bytes.slice(positionIndex, positionIndex + 2));
                        positionIndex += 2;
                    } else if (id >= 0xa0) {
                        if (positionIndex + 4 > packageLen) {
                            positionIndex += 2;
                            break;
                        }
                        // four bytes
                        type2Map.set(id, bytes.slice(positionIndex, positionIndex + 4));
                        positionIndex += 4;
                    }
                }
            } else if (dataType === 3) {
                for (let i = 0; i < columnLen; i++) {
                    if (positionIndex + 3 > packageLen) {
                        positionIndex += 3;
                        break;
                    }
                    let id = ByteUtils.byteToShort(bytes, positionIndex);
                    positionIndex += 2;
                    let dataLen = bytes[positionIndex];
                    if (dataLen < 0) {
                        dataLen += 256;
                    }
                    positionIndex++;
                    if (positionIndex + dataLen > packageLen) {
                        positionIndex += dataLen;
                        break;
                    }
                    const dataContent = bytes.slice(positionIndex, positionIndex + dataLen);
                    positionIndex += dataLen;
                    type3Map.set(id, dataContent);
                }
            }
        }
    },
    isAllFF:function(array) {
        if (array == null || array.length === 0) {
            return false;
        }
        for (let i = 0; i < array.length; i++) {
            // In JavaScript, byte values are signed (-128 to 127)
            // 0xFF is represented as -1 in signed byte
            if (array[i] !== 255) {
                return false;
            }
        }
        return true;
    },
    parseLocationTypeThreeData:function(locationMessage, typeMap) {
        for (let [dataId, valueByte] of typeMap.entries()) {
            if (dataId === 0x01) {
                if(this.isAllFF(valueByte)){
                    locationMessage.latlngValid = false
                    continue;
                }
                const altitude = ByteUtils.bytes2Float(valueByte, 0);
                const longitude = ByteUtils.bytes2Float(valueByte, 4);
                const latitude = ByteUtils.bytes2Float(valueByte, 8);
                const azimuth = ByteUtils.byteToShort(valueByte, 12);
                let satelliteCount = valueByte[16];
                if (satelliteCount < 0)
                    satelliteCount += 256;

                locationMessage.latlngValid = true
                locationMessage.altitude = altitude
                locationMessage.latitude = latitude
                locationMessage.longitude = longitude
                locationMessage.azimuth =azimuth
                locationMessage.satelliteNumber = satelliteCount
            } else if (dataId === 0x02) {
                const mcc_2g = ByteUtils.byteToShort(valueByte, 0);
                const mnc_2g = ByteUtils.byteToShort(valueByte, 2);
                const lac_2g_1 = ByteUtils.byteToShort(valueByte, 4);
                const ci_2g_1 = ByteUtils.byteToShort(valueByte, 6);
                const lac_2g_2 = ByteUtils.byteToShort(valueByte, 8);
                const ci_2g_2 = ByteUtils.byteToShort(valueByte, 10);
                const lac_2g_3 = ByteUtils.byteToShort(valueByte, 12);
                const ci_2g_3 = ByteUtils.byteToShort(valueByte, 14);

                locationMessage.is_2g_lbs = true
                locationMessage.mcc_2g =mcc_2g
                locationMessage.mnc_2g =mnc_2g
                locationMessage.lac_2g_1 =lac_2g_1
                locationMessage.ci_2g_1 =ci_2g_1
                locationMessage.lac_2g_2 =lac_2g_2
                locationMessage.ci_2g_2 =ci_2g_2
                locationMessage.lac_2g_3 =lac_2g_3
                locationMessage.ci_2g_3 =ci_2g_3
            } else if (dataId === 0x03) {
                const mcc_4g = ByteUtils.byteToShort(valueByte, 0) & 0x7FFF;
                const mnc_4g = ByteUtils.byteToShort(valueByte, 2);
                const eci_4g = ByteUtils.byteToLong(valueByte, 4);
                const tac = ByteUtils.byteToShort(valueByte, 8);
                const pcid_4g_1 = ByteUtils.byteToShort(valueByte, 10);
                const pcid_4g_2 = ByteUtils.byteToShort(valueByte, 12);
                const pcid_4g_3 = ByteUtils.byteToShort(valueByte, 14);

                locationMessage.is_4g_lbs = true;
                locationMessage.mcc_4g =mcc_4g
                locationMessage.mnc_4g =mnc_4g
                locationMessage.eci_4g =eci_4g
                locationMessage.tac =tac
                locationMessage.pcid_4g_1 =pcid_4g_1
                locationMessage.pcid_4g_2 =pcid_4g_2
                locationMessage.pcid_4g_3 =pcid_4g_3
            } else if (dataId === 0x04) {
                const selfMac = ByteUtils.bytes2HexString(valueByte.slice(0, 6), 0);
                const ap1Mac = ByteUtils.bytes2HexString(valueByte.slice(6, 12), 0);
                const ap1Rssi = valueByte[12] > 127 ? valueByte[12] - 256 : valueByte[12];
                const ap2Mac = ByteUtils.bytes2HexString(valueByte.slice(13, 19), 0);
                const ap2Rssi = valueByte[19] > 127 ? valueByte[19] - 256 : valueByte[19];
                const ap3Mac = ByteUtils.bytes2HexString(valueByte.slice(20, 26), 0);
                const ap3Rssi = valueByte[26] > 127 ? valueByte[26] - 256 : valueByte[26];

                locationMessage.selfMac = selfMac.toUpperCase()
                locationMessage.ap1Mac = ap1Mac.toUpperCase()
                locationMessage.ap1Rssi = ap1Rssi
                locationMessage.ap2Mac = ap2Mac.toUpperCase()
                locationMessage.ap2Rssi = ap2Rssi
                locationMessage.ap3Mac = ap3Mac.toUpperCase()
                locationMessage.ap3Rssi = ap3Rssi
            } else if (dataId === 0x05) {
                const axisX = ByteUtils.byteToShortSigned(valueByte, 0);
                const axisY = ByteUtils.byteToShortSigned(valueByte, 2);
                const axisZ = ByteUtils.byteToShortSigned(valueByte, 4);
                locationMessage.axisX = axisX;
                locationMessage.axisY = axisY;
                locationMessage.axisZ = axisZ;
            } else if (dataId === 0x06) {
                const gyroscopeAxisX = ByteUtils.byteToShortSigned(valueByte, 0);
                const gyroscopeAxisY = ByteUtils.byteToShortSigned(valueByte, 2);
                const gyroscopeAxisZ = ByteUtils.byteToShortSigned(valueByte, 4);
                locationMessage.gyroscopeAxisX = gyroscopeAxisX;
                locationMessage.gyroscopeAxisY = gyroscopeAxisY;
                locationMessage.gyroscopeAxisZ = gyroscopeAxisZ;
            } else if (dataId === 0x07) {
                let accumulatingFuelConsumption = ByteUtils.byteToLong(valueByte, 0);
                if (accumulatingFuelConsumption < 0) {
                    accumulatingFuelConsumption += 4294967296;
                }
                if (accumulatingFuelConsumption === 4294967295) {
                    accumulatingFuelConsumption = -999;
                }
                let instantFuelConsumption = ByteUtils.byteToLong(valueByte, 4);
                if (instantFuelConsumption < 0) {
                    instantFuelConsumption += 4294967296;
                }
                if (instantFuelConsumption === 4294967295) {
                    instantFuelConsumption = -999;
                }
                const rpm = ByteUtils.byteToShort(valueByte, 8);
                const airInput = valueByte[10];
                const airPressure = valueByte[11];
                const coolingFluidTemp = valueByte[12] - 40;
                const airInflowTemp = valueByte[13] - 40;
                const engineLoad = valueByte[14];
                const throttlePosition = valueByte[15];
                let remainFuelRate = valueByte[16] & 0x7f;
                let remainFuelUnit = (valueByte[16] & 0x80) ? 1 : 0;
                if (valueByte[16] === 255) {
                    remainFuelRate = -999;
                    remainFuelUnit = -999;
                }

                locationMessage.accumulatingFuelConsumption = accumulatingFuelConsumption
                locationMessage.instantFuelConsumption = instantFuelConsumption
                locationMessage.rpm = rpm
                locationMessage.airInflowTemp = airInflowTemp
                locationMessage.airInput = airInput
                locationMessage.airPressure = airPressure
                locationMessage.coolingFluidTemp = coolingFluidTemp
                locationMessage.engineLoad = engineLoad
                locationMessage.throttlePosition = throttlePosition
                locationMessage.remainFuelRate = remainFuelRate
                locationMessage.remainFuelUnit = remainFuelUnit
            } else if (dataId === 0x08) {
                const remainPower = valueByte[0];
                const isCarCharge = valueByte[1] === 0x01;
                const dashboardSpeed = valueByte[2];
                const acceleratorPedalPosition = valueByte[3];
                const remainPowerMinDistance = ByteUtils.byteToLong(valueByte, 4);
                const remainPowerMaxDistance = ByteUtils.byteToLong(valueByte, 8);
                const carChargeVoltage = ByteUtils.byteToLong(valueByte, 12) / 1000.0;
                const carChargeElectricCurrent = ByteUtils.byteToLong(valueByte, 16);
                const carChargePower = ByteUtils.byteToLong(valueByte, 20);
                const fullRemainingTime = ByteUtils.byteToLong(valueByte, 24);
                const carBatteryEffectiveCapacity = ByteUtils.byteToLong(valueByte, 28);
                const carBatteryInitialCapacity = ByteUtils.byteToLong(valueByte, 32);
                const carTotalPowerConsumption = ByteUtils.byteToLong(valueByte, 36);

                locationMessage.remainPower = remainPower;
                locationMessage.carCharge = isCarCharge;
                locationMessage.isCarCharge = isCarCharge;
                locationMessage.dashboardSpeed = dashboardSpeed;
                locationMessage.acceleratorPedalPosition = acceleratorPedalPosition;
                locationMessage.remainPowerMinDistance = remainPowerMinDistance;
                locationMessage.remainPowerMaxDistance = remainPowerMaxDistance;
                locationMessage.remainPowerDistance = remainPowerMinDistance;
                locationMessage.carChargeVoltage = carChargeVoltage;
                locationMessage.carChargeElectricCurrent = carChargeElectricCurrent;
                locationMessage.carChargePower = carChargePower;
                locationMessage.fullRemainingTime = fullRemainingTime;
                locationMessage.carBatteryEffectiveCapacity = carBatteryEffectiveCapacity;
                locationMessage.carBatteryInitialCapacity = carBatteryInitialCapacity;
                locationMessage.carTotalPowerConsumption = carTotalPowerConsumption;
                locationMessage.isObdElectricData = true;
            } else if (dataId === 0x0D) {
                let remainPower = valueByte[0];
                if (remainPower < 0) remainPower += 256;
                if (remainPower === 255) remainPower = -999;
                const isCarCharge = valueByte[1] === 0x01;
                let dashboardSpeed = valueByte[2];
                if (dashboardSpeed < 0) dashboardSpeed += 256;
                if (dashboardSpeed === 255) dashboardSpeed = -999;
                let acceleratorPedalPosition = valueByte[3];
                if (acceleratorPedalPosition < 0) acceleratorPedalPosition += 256;
                if (acceleratorPedalPosition === 255) acceleratorPedalPosition = -999;
                let remainPowerDistance = ByteUtils.byteToLong(valueByte, 4);
                if (remainPowerDistance === 4294967295) remainPowerDistance = -999;
                let carChargeVoltageRaw = ByteUtils.byteToLong(valueByte, 8);
                let carChargeVoltage = carChargeVoltageRaw / 1000.0;
                if (carChargeVoltageRaw === 4294967295) carChargeVoltage = -999;
                let carChargeElectricCurrent = ByteUtils.byteToLong(valueByte, 12);
                if (carChargeElectricCurrent === 4294967295) carChargeElectricCurrent = -999;
                let carBatteryEffectiveCapacity = ByteUtils.byteToLong(valueByte, 16);
                if (carBatteryEffectiveCapacity === 4294967295) carBatteryEffectiveCapacity = -999;
                let carBatteryInitialCapacity = ByteUtils.byteToLong(valueByte, 20);
                if (carBatteryInitialCapacity === 4294967295) carBatteryInitialCapacity = -999;
                let carTotalPowerConsumption = ByteUtils.byteToLong(valueByte, 24);
                if (carTotalPowerConsumption === 4294967295) carTotalPowerConsumption = -999;
                locationMessage.remainPower = remainPower;
                locationMessage.carCharge = isCarCharge;
                locationMessage.isCarCharge = isCarCharge;
                locationMessage.dashboardSpeed = dashboardSpeed;
                locationMessage.acceleratorPedalPosition = acceleratorPedalPosition;
                locationMessage.remainPowerDistance = remainPowerDistance;
                locationMessage.carChargeVoltage = carChargeVoltage;
                locationMessage.carChargeElectricCurrent = carChargeElectricCurrent;
                locationMessage.carBatteryEffectiveCapacity = carBatteryEffectiveCapacity;
                locationMessage.carBatteryInitialCapacity = carBatteryInitialCapacity;
                locationMessage.carTotalPowerConsumption = carTotalPowerConsumption;
                locationMessage.isObdElectricData = true;
            }
        }
    },
    parseLocationTypeTwoData:function(locationMessage, typeMap) {
        for (let [dataId, valueByte] of typeMap.entries()) {
            if (dataId === 0x01) {
                let value = valueByte[0];
                if (value < 0) value += 256;
                locationMessage.originalAlarmCode = value;
            } else if (dataId === 0x02) {
                const value = valueByte[0];
                locationMessage.gpsWorking = (value & 0x80) === 0x80;
                const gpsEnable = (value & 0x40) === 0x40;
                locationMessage.gpsEnable = gpsEnable;
                locationMessage.latlngValid = (value & 0x04) === 0x04;
                locationMessage.isHistoryData = (value & 0x02) === 0x02;
            } else if (dataId === 0x03) {
                let value = valueByte[0];
                if (value < 0)
                    value += 256;
                locationMessage.networkSignal=value;
            } else if (dataId === 0x04) {
                const value = valueByte[0];
                locationMessage.output1 = (value & 0x01) ? 1 : 0;
                locationMessage.output2 = (value & 0x02) ? 1 : 0;
                locationMessage.output3 = (value & 0x04) ? 1 : 0;
                locationMessage.output4 = (value & 0x08) ? 1 : 0;
                locationMessage.output5 = (value & 0x10) ? 1 : 0;
                locationMessage.output6 = (value & 0x20) ? 1 : 0;
                locationMessage.output7 = (value & 0x40) ? 1 : 0;
                locationMessage.output8 = (value & 0x80) ? 1 : 0;
            } else if (dataId === 0x05) {
                let value = valueByte[0];
                if (value < 0) value += 256;
                locationMessage.input1 = (value & 0x01) ? 1 : 0;
                locationMessage.input2 = (value & 0x02) ? 1 : 0;
                locationMessage.input3 = (value & 0x04) ? 1 : 0;
                locationMessage.input4 = (value & 0x08) ? 1 : 0;
                locationMessage.input5 = (value & 0x10) ? 1 : 0;
                locationMessage.input6 = (value & 0x20) ? 1 : 0;
                locationMessage.input7 = (value & 0x40) ? 1 : 0;
                locationMessage.input8 = (value & 0x80) ? 1 : 0;
            } else if (dataId === 0x06) {
                const value = valueByte[0];
                locationMessage.hasThirdPartyObd = (value & 0x80) ? 1 : 0;
                locationMessage.exPowerConsumpStatus=(value & 0x40) ? 1 : 0;
                locationMessage.mileageSource = (value & 0x10) ? 1 : 0;
            } else if (dataId === 0x07) {
                let value = valueByte[0];
                if (value < 0) value += 256;
                locationMessage.batteryCharge = value;
            } else if (dataId === 0x09) {
                let value = valueByte[0];
                if (value < 0) value += 256;
            } else if (dataId === 0x10) {
                locationMessage.smartPowerSettingStatus = (valueByte[0] & 0x80) === 0x80 ? "enable" : "disable";
            } else if (dataId === 0x11) {
                locationMessage.flashLightOpen = (valueByte[0] & 0x80) === 0x80;
                locationMessage.logoLightOpen = (valueByte[0] & 0x40) === 0x40;
                locationMessage.buzzerOpen = (valueByte[0] & 0x20) === 0x20;
                locationMessage.lostModeOpen = (valueByte[0] & 0x10) === 0x10;
            } else if (dataId === 0x0A) {
                let deviceTemp = -999;
                if (valueByte[0] !== 0xff) {
                    deviceTemp = (valueByte[0] & 0x7F) * ((valueByte[0] & 0x80) ? -1 : 1);
                }
                locationMessage.deviceTemp = deviceTemp;
            } else if (dataId === 0x0B) {
                let value = valueByte[0];
                if (value < 0) value += 256;
                // TODO: Set appropriate locationMessage property if needed
            } else if (dataId === 0x0C || dataId === 0x0D) {
                let value = valueByte[0];
                if (value < 0) value += 256;
                // TODO: Set appropriate locationMessage property if needed
            } else if (dataId === 0x0E) {
                const batteryCanRecharge = (valueByte[0] & 0x80) === 0x80;
                locationMessage.batteryCanRecharge = batteryCanRecharge;
            } else if (dataId === 0x0F) {
                locationMessage.lockType = valueByte[0];
            } else if (dataId === 0x12) {
                locationMessage.relayStatus = valueByte[0] & 0xff;
            } else if (dataId === 0x13) {
                locationMessage.mileageSource = valueByte[0] === 0x01 ? 2 : 0;
            } else if (dataId === 0x14) {
                locationMessage.ignitionSource = valueByte[0] & 0xff;
            } else if (dataId === 0x15) {
                locationMessage.obdFuelType = valueByte[0] & 0xff;
            } else if (dataId === 0x16) {
                locationMessage.wifiScanInterval = valueByte[0] & 0xff;
            } else if (dataId === 0x60) {
                const voltage = ByteUtils.byteToShort(valueByte, 0);
                locationMessage.batteryVoltage = voltage / 1000.0;
            } else if (dataId === 0x61) {
                const value = valueByte[0];
                locationMessage.iopIgnition = (value & 0x80) === 0x80;
                locationMessage.iopACOn = (value & 0x40) === 0x40;
                locationMessage.isUsbCharging = (value & 0x10) === 0x10;
                locationMessage.isSolarCharging = (value & 0x08) === 0x08;
                locationMessage.isWirelessCharging = (value & 0x02) === 0x02;
                locationMessage.iop = locationMessage.iopIgnition ? 0x4000 : 0x0000;
                locationMessage.IOP = locationMessage.iop;
                locationMessage.smartPowerOpenStatus = (value & 0x04) ? "open" : "close";
            } else if (dataId === 0x62) {
                const speed = ByteUtils.byteToShort(valueByte, 0);
                locationMessage.speed = speed;
            } else if (dataId === 0x63) {
                const solarVoltage = ByteUtils.byteToShort(valueByte, 0);
                locationMessage.solarVoltage = solarVoltage / 1000.0;
            } else if (dataId === 0x64) {
                if(!this.isAllFF(valueByte)){
                    const isNegative = (valueByte[1] & 0x80) === 0x80;
                    const value = (isNegative ? -1 : 1) * (valueByte[0] + ((valueByte[1] & 0x7f) / 100.0));
                    locationMessage.externalTemp = value;
                }
            } else if (dataId === 0x65) {
                if(!this.isAllFF(valueByte)){
                    const isNegative = (valueByte[0] & 0x80) === 0x80;
                    const value = (isNegative ? -1 : 1) * ((valueByte[0] & 0x7f) + (valueByte[1] / 100.0));
                    locationMessage.externalHumidity = value;
                }
            } else if (dataId === 0x66) {
                if(!this.isAllFF(valueByte)){
                    const isNegative = (valueByte[1] & 0x80) === 0x80;
                    const value = (isNegative ? -1 : 1) * (valueByte[0] + ((valueByte[1] & 0x7f) / 100.0));
                    locationMessage.deviceHighPrecisionTemp = value;
                }
            } else if (dataId === 0x67) {
                const voltage = ByteUtils.byteToShort(valueByte, 0);
                locationMessage.analogInput1 = voltage / 1000.0;
            } else if (dataId === 0x68) {
                const voltage = ByteUtils.byteToShort(valueByte, 0);
                locationMessage.analogInput2 = voltage / 1000.0;
            } else if (dataId === 0x69) {
                const voltage = ByteUtils.byteToShort(valueByte, 0);
                locationMessage.analogInput3 = voltage / 1000.0;
            }  else if (dataId === 0xA0) {
                const mileage = ByteUtils.byteToLong(valueByte, 0);
                locationMessage.mileage = mileage;
            } else if (dataId === 0xA1) {
                const externalVoltage = ByteUtils.byteToLong(valueByte, 0);
                locationMessage.externalPowerVoltage = externalVoltage / 1000.0;
            } else if (dataId === 0xA2) {
                const lightIntensity = ByteUtils.byteToLong(valueByte, 0);
                locationMessage.lightIntensityValue = lightIntensity;
            }
        }
    },
    parseLocationTypeOneData:function(locationMessage, typeMap) {
        for (let [dataId, valueByte] of typeMap.entries()) {
            if (dataId === 0x03) {
                let value = valueByte[0];
                if (value < 0) value += 256;
                locationMessage.angleCompensation = value;
            } else if (dataId === 0x04) {
                let value = valueByte[0];
                if (value < 0) value += 256;
                locationMessage.overSpeedLimit = value;
                locationMessage.overspeedLimit = value;
            } else if (dataId === 0x05) {
                const value = valueByte[0];
                const isManagerConfigured1 = (value & 0x01) === 0x01;
                const isManagerConfigured2 = (value & 0x02) === 0x02;
                const isManagerConfigured3 = (value & 0x04) === 0x04;
                const isManagerConfigured4 = (value & 0x08) === 0x08;
                locationMessage.isManagerConfigured1 = isManagerConfigured1;
                locationMessage.isManagerConfigured2 = isManagerConfigured2;
                locationMessage.isManagerConfigured3 = isManagerConfigured3;
                locationMessage.isManagerConfigured4 = isManagerConfigured4;
            } else if (dataId === 0x06) {
                const value = valueByte[0];
                const isLockDevice = (value & 0x80) === 0x80;
                const isLockSim = (value & 0x40) === 0x40;
                const isLockApn = (value & 0x20) === 0x20;
                locationMessage.isLockDevice = isLockDevice;
                locationMessage.isLockSim = isLockSim;
                locationMessage.isLockApn = isLockApn;
            } else if (dataId === 0x07) {
                let value = valueByte[0];
                if (value < 0) value += 256;
                locationMessage.isSendSmsAlarmToManagerPhone = value === 1;
            } else if (dataId === 0x08) {
                const jammerDetectionStatus = (valueByte[0] & 0x0C);
                locationMessage.jammerDetectionStatus = jammerDetectionStatus;
            } else if (dataId === 0x09) {
                locationMessage.antitheftedStatus = valueByte[0] & 0xff;
            } else if (dataId === 0x80) {
                const distance = ByteUtils.byteToShort(valueByte, 0);
                locationMessage.distanceCompensation = distance;
            } else if (dataId === 0x81) {
                const dragThreshold = ByteUtils.byteToShort(valueByte, 0);
                locationMessage.dragThreshold = dragThreshold;
            } else if (dataId === 0x82) {
                const heartbeatInterval = ByteUtils.byteToShort(valueByte, 0);
                locationMessage.heartbeatInterval = heartbeatInterval;
            } else if (dataId === 0xC0) {
                const accOnInterval = ByteUtils.byteToLong(valueByte, 0);
                locationMessage.samplingIntervalAccOn = accOnInterval;
            } else if (dataId === 0xC1) {
                const accOffInterval = ByteUtils.byteToLong(valueByte, 0);
                locationMessage.samplingIntervalAccOff = accOffInterval;
            }
        }
    }



}
module.exports = DecoderHelper;
