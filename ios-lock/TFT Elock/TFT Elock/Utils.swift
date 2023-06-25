//
//  Utils.swift
//  tftble
//
//  Created by jeech on 2019/12/20.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
import CoreBluetooth
class Utils{
    static let serviceId = CBUUID(string:"27760001-999C-4D6A-9FC4-C7272BE10900")
    static let writeUUID = CBUUID(string:"27760003-999C-4D6A-9FC4-C7272BE10900")
    static let notifyUUID = CBUUID(string:"27760003-999C-4D6A-9FC4-C7272BE10900")
    
    static let readDataServiceId = CBUUID(string:"27760001-999d-4d6a-9fc4-c7272be10900")
    static let readDataWriteUUID = CBUUID(string:"27760002-999d-4d6a-9fc4-c7272be10900")
    static let readDataNotifyUUID = CBUUID(string:"27760003-999d-4d6a-9fc4-c7272be10900")
    
    
    static let fontSize = 13
    
    
    static func hexString2Bytes(hexStr:String)->[UInt8]{
        let hexa = Array(hexStr)
        return stride(from: 0, to: hexStr.count, by: 2).flatMap { UInt8(String(hexa[$0..<$0.advanced(by: 2)]), radix: 16) }
    }
    
    
    
    
    static func isPurnInt(string: String) -> Bool {
        let scan: Scanner = Scanner(string: string)
        var val:Int = 0
        return scan.scanInt(&val) && scan.isAtEnd
    }
    
    
    static func bytes2HexString(bytes:[UInt8],pos:Int) -> String {
        var hexStr = ""
        if pos >= bytes.count{
            return hexStr
        }
        if bytes.isEmpty || pos >= bytes.count {
                return ""
            }

            var builder = ""
            for i in pos..<bytes.count {
                let hex = String(format: "%02x", bytes[i])
                builder += hex
            }

            return builder
    }
    static func hexIntToStr(HexInt:Int) -> String {
        var Str = ""
        if HexInt>9 {
            switch HexInt{
            case 10:
                Str = "A"
                break
            case 11:
                Str = "B"
                break
            case 12:
                Str = "C"
                break
            case 13:
                Str = "D"
                break
            case 14:
                Str = "E"
                break
            case 15:
                Str = "F"
                break
            default:
                Str = "0"
            }
        }else {
            Str = String(HexInt)
        }
        
        return Str
    }
    
    
    
    static func short2Bytes(number:Int) ->[UInt8]{
        var bytes = [UInt8](repeating: 0, count: 2)
        bytes[0] = 0
        bytes[1] = 0
        var i = 1
        var numb = number
        while i >= 0{
            bytes[i] = UInt8(numb % 256)
            numb >>= 8
            i -= 1
        }
        return bytes
    }
    
    
    static func uint8ToHexStr(value:UInt8) ->String{
        return String(format:"%02X",value).uppercased()
    }
    
    static func uint8ArrayToHexStr(value:[UInt8]) ->String{
        var i = 0;
        var result = ""
        while i < value.count{
            result.append(String(format: "%02x", value[i]))
            i+=1;
        }
        return result
    }
    
    
    
    static func bytes2Integer(buf:[UInt8],pos:Int) ->Int{
        var firstByte = 0;
        var secondByte = 0;
        var thirdByte = 0;
        var fourthByte = 0;
        var index = pos;
        firstByte = (0x000000FF & (Int (buf[index])));
        secondByte = (0x000000FF & (Int(buf[index + 1])));
        thirdByte = (0x000000FF & (Int (buf[index + 2])));
        fourthByte = (0x000000FF & (Int (buf[index + 3])));
        index = index + 4;
        return (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte);
    }
    
    static func bytes2Short(bytes:[UInt8],offset:Int) ->Int{
        if bytes.count >= 2 && bytes.count > offset + 1{
            return (Int(bytes[offset]) << 8) + Int(bytes[offset + 1])
        }
        return 0
    }
    
    static func splitSingleStrToCharArr(strToSplit:String) -> [Character] {
        let splitedCharArr:[Character] = Array(strToSplit)
        return splitedCharArr
    }
    
    static func characterToInt(value:Character) ->Int{
        var intFromCharacter:Int = 0
        for scalar in String(value).unicodeScalars
        {
            intFromCharacter = Int(scalar.value)
        }
        if intFromCharacter >= 97 {
            intFromCharacter -= 87
        }
        else if intFromCharacter >= 65 {
            intFromCharacter -= 55
        }else{
            intFromCharacter -= 48
        }
        return intFromCharacter
    }
    
    static func binaryCharArrayToLong(binaryArray:[Character]) ->Int{
        var result = 0
        for index in 0...(binaryArray.count-1){
            let curChar:Character = binaryArray[index]
            let curByte:Int = Int(String(curChar)) ?? 0
            result += curByte << (binaryArray.count - index - 1)
        }
        return result
    }
    
    static func arraysEqual(item1:[UInt8],item2:[UInt8]) ->Bool{
        if item1.count != item2.count{
            return false
        }
        var i = 0
        while i < item1.count{
            if item1[i] != item2[i]{
                return false
            }
            i+=1
        }
        return true
    }
    
    static func arraysCopyOfRange(src:[UInt8],from:Int,to:Int) ->[UInt8]{
        var result = [UInt8]()
        if src.count < to {
            return result
        }
        var startIndex = from
        if startIndex < 0{
            startIndex = 0
        }
        while startIndex < to{
            result.append(src[startIndex])
            startIndex+=1
        }
        return result
    }
    
    static func arraysCopyOfRange(src:[Character],from:Int,to:Int) ->[Character]{
        var result = [Character]()
        if src.count < to {
            return result
        }
        var startIndex = from
        if startIndex < 0{
            startIndex = 0
        }
        while startIndex < to{
            result.append(src[startIndex])
            startIndex+=1
        }
        return result
    }
    
    
    
    static func convertByteArrayToBinaryStr(byteArray:[UInt8]) ->String{
        var resultArray = [String]()
        var i = 0
        while i < byteArray.count{
            var curItem = String(byteArray[i],radix: 2)
            if curItem.count < 8{
                var j = curItem.count
                while j < 8{
                    curItem = "0" + curItem
                    j+=1
                }
            }
            resultArray.append(curItem)
            i+=1
        }
        return resultArray.joined(separator: "")
    }
    
    public static func getGTM0Date(bytes: [UInt8], startIndex: Int) -> Date {
        let dateData = Array(bytes[startIndex..<startIndex+6]) // 130512122356
        var datetime = Utils.bytes2HexString(bytes: dateData, pos: 0)
        datetime = "20" + datetime
        let year = Int(datetime.prefix(4))!
        let calendar = Calendar.current
        let curYear = calendar.component(.year, from: Date())
        var adjustedYear = year
        if year > curYear {
            adjustedYear = year - 100
        }
        let dateFormat = "yyyyMMddHHmmss"
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = dateFormat
        dateFormatter.timeZone = TimeZone(identifier: "GMT")
        
        if let date = dateFormatter.date(from: datetime) {
            // 使用生成的Date对象进行后续操作
            return date
        } else {
            // 解析失败，处理错误情况
            return Date()
        }
    }
    static func bytes2Float(_ bytes: [UInt8], _ offset: Int) -> Float {
        var value: Int32 = 0
        
        value = Int32(bytes[offset])
        value &= 0xff
        value |= (Int32(bytes[offset + 1]) << 8)
        value &= 0xffff
        value |= (Int32(bytes[offset + 2]) << 16)
        value &= 0xffffff
        value |= (Int32(bytes[offset + 3]) << 24)
        // 对value进行符号扩展，确保正确性
            if value & (1 << 31) != 0 {
                value |= ~0x7fffffff
            }
        return Float(bitPattern: UInt32(bitPattern: value))
    }
    static func unsigned4BytesToInt(_ buf: [UInt8], _ pos: Int) -> UInt32 {
        var firstByte: UInt32 = 0
        var secondByte: UInt32 = 0
        var thirdByte: UInt32 = 0
        var fourthByte: UInt32 = 0
        var index = pos
        firstByte = (0x000000FF & UInt32(buf[index]))
        secondByte = (0x000000FF & UInt32(buf[index + 1]))
        thirdByte = (0x000000FF & UInt32(buf[index + 2]))
        fourthByte = (0x000000FF & UInt32(buf[index + 3]))
        index = index + 4
        return (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte) & 0xFFFFFFFF
    }
    private static func parseDataMessage(data: [UInt8]) -> LocationMessage {
        //解析数据提取各种属性
        let serialNo = Utils.bytes2Short(bytes: data, offset: 5)
        let imei = Utils.decodeImei(bytes: data, index: 7)
        let date = Utils.getGTM0Date(bytes: data, startIndex: 17)
        let isGpsWorking = (data[15] & 0x20) == 0x00
        let isHistoryData = (data[15] & 0x80) != 0x00
        let latlngValid = (data[15] & 0x40) == 0x40
        let satelliteNumber = Int(data[15] & 0x1F)
        let altitude = latlngValid ? Utils.bytes2Float(data, 23) : 0
        let longitude = latlngValid ? Utils.bytes2Float(data, 27) : 0
        let latitude = latlngValid ? Utils.bytes2Float(data, 31) : 0
        var speedf: Float = 0.0
        if latlngValid {
            do {
                let bytesSpeed = Array(data[35...36])
                let strSp = Utils.bytes2HexString(bytes: bytesSpeed, pos: 0)
                if strSp.lowercased() != "ffff" {
                    let value1 = Int(strSp.prefix(3)) ?? 0
                    let value2 = Int(strSp.suffix(strSp.count - 3)) ?? 0
                    speedf = Float(String(format: "%d.%d",value1 , value2)) ?? 0
                }
            } catch {
                // 处理异常
            }
        }
        let azimuth = latlngValid ? Utils.bytes2Short(bytes: data, offset: 37) : 0
        var is_4g_lbs: Bool = false
        var mcc_4g: Int? = nil
        var mnc_4g: Int? = nil
        var ci_4g: Int64? = nil
        var earfcn_4g_1: Int? = nil
        var pcid_4g_1: Int? = nil
        var earfcn_4g_2: Int? = nil
        var pcid_4g_2: Int? = nil
        var is_2g_lbs: Bool = false
        var mcc_2g: Int? = nil
        var mnc_2g: Int? = nil
        var lac_2g_1: Int? = nil
        var ci_2g_1: Int? = nil
        var lac_2g_2: Int? = nil
        var ci_2g_2: Int? = nil
        var lac_2g_3: Int? = nil
        var ci_2g_3: Int? = nil
        if !latlngValid {
            let lbsByte = data[23]
            if (lbsByte & 0x80) == 0x80 {
                is_4g_lbs = true
            } else {
                is_2g_lbs = true
            }
        }
        if is_2g_lbs {
            mcc_2g = Utils.bytes2Short(bytes: data, offset: 23)
            mnc_2g = Utils.bytes2Short(bytes: data, offset: 25)
            lac_2g_1 = Utils.bytes2Short(bytes: data, offset: 27)
            ci_2g_1 = Utils.bytes2Short(bytes: data, offset: 29)
            lac_2g_2 = Utils.bytes2Short(bytes: data, offset: 31)
            ci_2g_2 = Utils.bytes2Short(bytes: data, offset: 33)
            lac_2g_3 = Utils.bytes2Short(bytes: data, offset: 35)
            ci_2g_3 = Utils.bytes2Short(bytes: data, offset: 37)
        }
        if is_4g_lbs {
            mcc_4g = Utils.bytes2Short(bytes: data, offset: 23) & 0x7FFF
            mnc_4g = Utils.bytes2Short(bytes: data, offset: 25)
            ci_4g = Int64(Utils.unsigned4BytesToInt(data, 27))
            earfcn_4g_1 = Utils.bytes2Short(bytes: data, offset: 31)
            pcid_4g_1 = Utils.bytes2Short(bytes: data, offset: 33)
            earfcn_4g_2 = Utils.bytes2Short(bytes: data, offset: 35)
            pcid_4g_2 = Utils.bytes2Short(bytes: data, offset: 37)
        }
        let axisXDirect = (data[39] & 0x80) == 0x80 ? 1 : -1
        let axisX = (Float((data[39] & 0x7F & 0xff) + (((data[40] & 0xf0) >> 4) & 0xff)) / 10.0) * Float(axisXDirect)
        
        let axisYDirect = (data[40] & 0x08) == 0x08 ? 1 : -1
        let axixYValue1 = ((((data[40] & 0x07) << 4) & 0xff) + (((data[41] & 0xf0) >> 4) & 0xff))
        let axisY = Float(axixYValue1 + (data[41] & 0x0F & 0xff)) / 10.0 * Float(axisYDirect)
        
        let axisZDirect = (data[42] & 0x80) == 0x80 ? 1 : -1
        let axisZ = (Float((data[42] & 0x7F & 0xff) + (((data[43] & 0xf0) >> 4) & 0xff)) / 10.0) * Float(axisZDirect)
        
        let batteryPercentBytes: [UInt8] = [data[44]]
        let batteryPercentStr = Utils.bytes2HexString(bytes: batteryPercentBytes, pos: 0)
        var batteryPercent = 100
        if batteryPercentStr.lowercased() == "ff" {
            batteryPercent = 100
        } else {
            if let percent = Int(batteryPercentStr) {
                batteryPercent = percent == 0 ? 100 : percent
            }
        }
        
        var deviceTemp: Float = -999
        if data[45] != 0xff {
            deviceTemp = Float(data[45] & 0x7F) * ((data[45] & 0x80) == 0x80 ? -1 : 1)
        }
        
        let lightSensorBytes: [UInt8] = [data[46]]
        let lightSensorStr = Utils.bytes2HexString(bytes: lightSensorBytes, pos: 0)
        var lightSensor: Float = 0
        if lightSensorStr.lowercased() == "ff" {
            lightSensor = -999
        } else {
            if let sensorValue = Int(lightSensorStr) {
                lightSensor = Float(sensorValue) / 10.0
            }
        }
        
        let batteryVoltageBytes: [UInt8] = [data[47]]
        let batteryVoltageStr = Utils.bytes2HexString(bytes: batteryVoltageBytes, pos: 0)
        var batteryVoltage: Float = 0
        if batteryVoltageStr.lowercased() == "ff" {
            batteryVoltage = -999
        } else {
            if let voltage = Int(batteryVoltageStr) {
                batteryVoltage = Float(voltage) / 10.0
            }
        }
        
        let solarVoltageBytes: [UInt8] = [data[48]]
        let solarVoltageStr = Utils.bytes2HexString(bytes: solarVoltageBytes, pos: 0)
        var solarVoltage: Float = 0
        if solarVoltageStr.lowercased() == "ff" {
            solarVoltage = -999
        } else {
            if let voltage = Int(solarVoltageStr) {
                solarVoltage = Float(voltage) / 10.0
            }
        }
        
        let mileage = Utils.unsigned4BytesToInt(data, 49)
        let status = Utils.bytes2Short(bytes: data, offset: 53)
        let network = (status & 0x7F0) >> 4
        let accOnInterval = Utils.bytes2Short(bytes: data, offset: 55)
        let accOffInterval = Utils.bytes2Integer(buf: data, pos: 57)
        var angleCompensation = Int(data[61] & 0xff)
        if angleCompensation < 0 {
            angleCompensation += 256
        }
        let distanceCompensation = Utils.bytes2Short(bytes: data, offset: 62)
        let heartbeatInterval = Int(data[64])
        let isUsbCharging = (status & 0x8000) == 0x8000
        let isSolarCharging = (status & 0x8) == 0x8
        let iopIgnition = (status & 0x4) == 0x4
        let alarmByte = data[16]
        var originalAlarmCode = Int(alarmByte)
        if originalAlarmCode < 0 {
            originalAlarmCode += 256
        }
        let command = Array(data[..<3])
        let isAlarmData = command[2] == 0x04
        let status1 = data[54]
        var smartPowerOpenStatus = "close"
        if (status1 & 0x01) == 0x01 {
            smartPowerOpenStatus = "open"
        }
        let status2 = data[66]
        let isLockSim = (status2 & 0x80) == 0x80
        let isLockDevice = (status2 & 0x40) == 0x40
        let AGPSEphemerisDataDownloadSettingStatus = (status2 & 0x20) == 0x10
        let gSensorSettingStatus = (status2 & 0x10) == 0x10
        let frontSensorSettingStatus = (status2 & 0x08) == 0x08
        let deviceRemoveAlarmSettingStatus = (status2 & 0x04) == 0x04
        let openCaseAlarmSettingStatus = (status2 & 0x02) == 0x02
        let deviceInternalTempReadingANdUploadingSettingStatus = (status2 & 0x01) == 0x01
        let status3 = data[67]
        var smartPowerSettingStatus = "disable"
        if (status3 & 0x80) == 0x80 {
            smartPowerSettingStatus = "enable"
        }
        var lockType: Int?
        if data.count >= 71 {
            lockType = Int(data[70])
        }
        let locationMessage = LocationMessage()
        locationMessage.protocolHeadType = Int(data[2])
        locationMessage.orignBytes = data
        // ...设置相应属性
        locationMessage.serialNo = serialNo
        //locationMessage.isNeedResp = isNeedResp
        locationMessage.networkSignal = network
        locationMessage.imei = imei
        locationMessage.isSolarCharging = isSolarCharging
        locationMessage.isUsbCharging = isUsbCharging
        locationMessage.samplingIntervalAccOn = accOnInterval
        locationMessage.samplingIntervalAccOff = accOffInterval
        locationMessage.angleCompensation = angleCompensation
        locationMessage.distanceCompensation = distanceCompensation
        locationMessage.gpsWorking = isGpsWorking
        locationMessage.isHistoryData = isHistoryData
        locationMessage.satelliteNumber = satelliteNumber
        locationMessage.heartbeatInterval = heartbeatInterval
        locationMessage.originalAlarmCode = originalAlarmCode
        locationMessage.mileage = Int64(mileage)
        locationMessage.iopIgnition = iopIgnition
        locationMessage.IOP = locationMessage.iopIgnition ?? false ? 0x4000 : 0x0000
        locationMessage.batteryCharge = batteryPercent
        locationMessage.date = date
        locationMessage.latlngValid = latlngValid
        locationMessage.altitude = Double(altitude)
        locationMessage.latitude = Double(latitude)
        locationMessage.longitude = Double(longitude)
        locationMessage.isLockSim = isLockSim
        locationMessage.isLockDevice = isLockDevice
        locationMessage.AGPSEphemerisDataDownloadSettingStatus = AGPSEphemerisDataDownloadSettingStatus
        locationMessage.gSensorSettingStatus = gSensorSettingStatus
        locationMessage.frontSensorSettingStatus = frontSensorSettingStatus
        locationMessage.deviceRemoveAlarmSettingStatus = deviceRemoveAlarmSettingStatus
        locationMessage.openCaseAlarmSettingStatus = openCaseAlarmSettingStatus
        locationMessage.deviceInternalTempReadingANdUploadingSettingStatus = deviceInternalTempReadingANdUploadingSettingStatus
        if locationMessage.latlngValid {
            locationMessage.speed = speedf
        } else {
            locationMessage.speed = 0.0
        }
        locationMessage.azimuth = azimuth
        locationMessage.axisX = axisX
        locationMessage.axisY = axisY
        locationMessage.axisZ = axisZ
        locationMessage.deviceTemp = deviceTemp
        locationMessage.lightSensor = lightSensor
        locationMessage.batteryVoltage = batteryVoltage
        locationMessage.solarVoltage = solarVoltage
        locationMessage.smartPowerSettingStatus = smartPowerSettingStatus
        locationMessage.smartPowerOpenStatus = smartPowerOpenStatus
        locationMessage.is_4g_lbs = is_4g_lbs
        locationMessage.is_2g_lbs = is_2g_lbs
        locationMessage.mcc_2g = mcc_2g
        locationMessage.mnc_2g = mnc_2g
        locationMessage.lac_2g_1 = lac_2g_1
        locationMessage.ci_2g_1 = ci_2g_1
        locationMessage.lac_2g_2 = lac_2g_2
        locationMessage.ci_2g_2 = ci_2g_2
        locationMessage.lac_2g_3 = lac_2g_3
        locationMessage.ci_2g_3 = ci_2g_3
        locationMessage.mcc_4g = mcc_4g
        locationMessage.mnc_4g = mnc_4g
        locationMessage.ci_4g = ci_4g
        locationMessage.earfcn_4g_1 = earfcn_4g_1
        locationMessage.pcid_4g_1 = pcid_4g_1
        locationMessage.earfcn_4g_2 = earfcn_4g_2
        locationMessage.pcid_4g_2 = pcid_4g_2
        locationMessage.lockType = lockType
        return locationMessage
    }
    
    private static func decodeImei(bytes: [UInt8], index: Int) -> String {
        if bytes.count > 0 && index > 0 && (bytes.count - index) >= 8 {
            let str = Utils.bytes2HexString(bytes: bytes, pos: index)
            return String(str[str.index(str.startIndex, offsetBy: 1)..<str.index(str.startIndex, offsetBy: 16)])
        }
        return ""
    }
    
    private static func parseLockMessage(bytes: [UInt8]) -> LocationMessage {
        //解析数据提取各种属性
        let serialNo = Utils.bytes2Short(bytes: bytes, offset: 5)
        let imei = Utils.decodeImei(bytes: bytes, index: 7)
        let date = Utils.getGTM0Date(bytes: bytes, startIndex: 17)
        let latlngValid = (bytes[21] & 0x40) != 0x00
        let isGpsWorking = (bytes[21] & 0x20) == 0x00
        let isHistoryData = (bytes[21] & 0x80) != 0x00
        let satelliteNumber = bytes[21] & 0x1F
        let altitude = latlngValid ? Utils.bytes2Float(bytes, 22) : 0
        let longitude = latlngValid ? Utils.bytes2Float(bytes, 26) : 0
        let latitude = latlngValid ? Utils.bytes2Float(bytes, 30) : 0
        var speedf: Float = 0.0
        if latlngValid {
            let bytesSpeed = Array(bytes[34...35])
            let strSp = Utils.bytes2HexString(bytes: bytesSpeed, pos: 0)
            if strSp.lowercased() != "ffff" {
                let sp = Float("\(Int(strSp.prefix(3))!).\(Int(strSp.suffix(3))!)")
                if let sp = sp {
                    speedf = sp
                }
            }
        }
        let azimuth = latlngValid ? Utils.bytes2Short(bytes: bytes, offset: 36) : 0
        var is_4g_lbs = false
        var mcc_4g: Int? = nil
        var mnc_4g: Int? = nil
        var ci_4g: Int64? = nil
        var earfcn_4g_1: Int? = nil
        var pcid_4g_1: Int? = nil
        var earfcn_4g_2: Int? = nil
        var pcid_4g_2: Int? = nil
        var is_2g_lbs = false
        var mcc_2g: Int? = nil
        var mnc_2g: Int? = nil
        var lac_2g_1: Int? = nil
        var ci_2g_1: Int? = nil
        var lac_2g_2: Int? = nil
        var ci_2g_2: Int? = nil
        var lac_2g_3: Int? = nil
        var ci_2g_3: Int? = nil
        if !latlngValid {
            let lbsByte = bytes[22]
            if (lbsByte & 0x80) == 0x80 {
                is_4g_lbs = true
            } else {
                is_2g_lbs = true
            }
        }
        if is_2g_lbs {
            mcc_2g = Int(Utils.bytes2Short(bytes: bytes, offset: 22))
            mnc_2g = Int(Utils.bytes2Short(bytes: bytes, offset: 24))
            lac_2g_1 = Int(Utils.bytes2Short(bytes: bytes, offset: 26))
            ci_2g_1 = Int(Utils.bytes2Short(bytes: bytes, offset: 28))
            lac_2g_2 = Int(Utils.bytes2Short(bytes: bytes, offset: 30))
            ci_2g_2 = Int(Utils.bytes2Short(bytes: bytes, offset: 32))
            lac_2g_3 = Int(Utils.bytes2Short(bytes: bytes, offset: 34))
            ci_2g_3 = Int(Utils.bytes2Short(bytes: bytes, offset: 36))
        }
        if is_4g_lbs {
            mcc_4g = Int(Utils.bytes2Short(bytes: bytes, offset: 22) & 0x7FFF)
            mnc_4g = Int(Utils.bytes2Short(bytes: bytes, offset: 24))
            ci_4g = Int64(Utils.unsigned4BytesToInt(bytes, 26))
            earfcn_4g_1 = Int(Utils.bytes2Short(bytes: bytes, offset: 30))
            pcid_4g_1 = Int(Utils.bytes2Short(bytes: bytes, offset: 32))
            earfcn_4g_2 = Int(Utils.bytes2Short(bytes: bytes, offset: 34))
            pcid_4g_2 = Int(Utils.bytes2Short(bytes: bytes, offset: 36))
        }
        var lockType = Int(bytes[38] & 0xff)
        if lockType < 0 {
            lockType += 256
        }
        let idLen = Int(bytes[39] & 0xff) * 2
        let idStr = Utils.bytes2HexString(bytes: bytes, pos: 40)
        var id = idStr
        if idStr.count > idLen {
            id = String(idStr.prefix(idLen))
        }
        id = id.uppercased()
        
        let lockMessage = LocationMessage()
        lockMessage.protocolHeadType = Int(bytes[2])
        lockMessage.orignBytes = bytes
        lockMessage.imei = imei
        lockMessage.date = date
        lockMessage.orignBytes = bytes
        lockMessage.latlngValid = latlngValid
        lockMessage.altitude = Double(altitude)
        lockMessage.longitude = Double(longitude)
        lockMessage.latitude = Double(latitude)
        lockMessage.latlngValid = latlngValid
        lockMessage.speed = speedf
        lockMessage.azimuth = azimuth
        lockMessage.lockType = lockType
        lockMessage.lockId = id
        lockMessage.isHistoryData = isHistoryData
        lockMessage.satelliteNumber = Int(satelliteNumber)
        lockMessage.gpsWorking = isGpsWorking
        lockMessage.is_4g_lbs = is_4g_lbs
        lockMessage.is_2g_lbs = is_2g_lbs
        lockMessage.mcc_2g = mcc_2g
        lockMessage.mnc_2g = mnc_2g
        lockMessage.lac_2g_1 = lac_2g_1
        lockMessage.ci_2g_1 = ci_2g_1
        lockMessage.lac_2g_2 = lac_2g_2
        lockMessage.ci_2g_2 = ci_2g_2
        lockMessage.lac_2g_3 = lac_2g_3
        lockMessage.ci_2g_3 = ci_2g_3
        lockMessage.mcc_4g = mcc_4g
        lockMessage.mnc_4g = mnc_4g
        lockMessage.ci_4g = ci_4g
        lockMessage.earfcn_4g_1 = earfcn_4g_1
        lockMessage.pcid_4g_1 = pcid_4g_1
        lockMessage.earfcn_4g_2 = earfcn_4g_2
        lockMessage.pcid_4g_2 = pcid_4g_2
        lockMessage.lockId = id
        return lockMessage
    }
    
    static func getLocationMessage(inBytes: [UInt8], decoderBuf: TopflytechByteBuf) -> [LocationMessage] {
        var result: [LocationMessage] = []
        
        decoderBuf.putBuf(inBytes)
        if decoderBuf.getReadableBytes() < 5 {
            return result
        }
        
        var bytes = [UInt8](repeating: 0, count: 3)
        while decoderBuf.getReadableBytes() > 5 {
            decoderBuf.markReaderIndex()
            
            // 解析协议头
            let byte0 = decoderBuf.getByte(at: 0)
            let byte1 = decoderBuf.getByte(at: 1)
            let byte2 = decoderBuf.getByte(at: 2)
            bytes[0] = byte0
            bytes[1] = byte1
            bytes[2] = byte2
            
            // 判断是否有效GPS数据包
            if (byte0 == 0x27 && byte1 == 0x27 && (byte2 == 0x02 || byte2 == 0x04 )) {
                decoderBuf.skipBytes(3)
                let lengthBytes = decoderBuf.readBytes(2) ?? [UInt8]()
                let packageLength = Utils.bytes2Short(bytes: lengthBytes, offset: 0)
                decoderBuf.resetReaderIndex()
                if packageLength <= 0 {
                    return result
                }
                if  packageLength > decoderBuf.getReadableBytes() {
                    return result;
                }
                // 解析数据包长度
                let data = decoderBuf.readBytes(packageLength)
                if data != nil {
                    
                    if byte2 == 0x17 {
                        
                        if let location = try? Utils.parseLockMessage(bytes: data ?? [UInt8]()) {
                            result.append(location)
                        }
                    } else {
                        
                        if let location = try? Utils.parseDataMessage(data: data ?? [UInt8]()) {
                            result.append(location)
                        }
                    }
                }
            }else {
                decoderBuf.skipBytes(1)
            } 
        }
        return result
    }
    
    static func unSignedInt2Bytes(_ num: Int64) -> [UInt8] {
        var byteNum = [UInt8](repeating: 0, count: 4)
        for ix in 0..<4 {
        let offset = 32 - (ix + 1) * 8
        byteNum[ix] = UInt8((num >> offset) & 0xff)
        }
        return byteNum
    }
    static func getTimes() -> [Int] {
        
        var timers: [Int] = [] //  返回的数组
        
        let calendar: Calendar = Calendar(identifier: .gregorian)
        var comps: DateComponents = DateComponents()
        comps = calendar.dateComponents([.year,.month,.day, .weekday, .hour, .minute,.second], from: Date())
        
        timers.append(comps.year! % 2000)  // 年 ，后2位数
        timers.append(comps.month!)            // 月
        timers.append(comps.day!)                // 日
        timers.append(comps.hour!)               // 小时
        timers.append(comps.minute!)            // 分钟
        timers.append(comps.second!)            // 秒
        timers.append(comps.weekday! - 1)      //星期
        
        return timers;
    }
    
    static func getDateRangeList() ->[String]{
        let result = [NSLocalizedString("str_today", comment: "Today"),NSLocalizedString("str_yesterday", comment: "Yestory"),NSLocalizedString("str_last_3_day", comment: "Last 3 days"),
//                      NSLocalizedString("str_last_7_day", comment: "Last 7 days"),NSLocalizedString("str_last_week", comment: "Last week"),NSLocalizedString("str_this_month", comment: "This month"),NSLocalizedString("str_pre_month", comment: "Previous month"),
                      NSLocalizedString("str_custom", comment: "Custom")]
        return result
    }
    
    static func getStartDate(selectedIndex:Int) -> Date{
        let calendar = Calendar.current
        let currentDate = Date()
        var components = calendar.dateComponents([Calendar.Component.day, Calendar.Component.hour, Calendar.Component.minute, Calendar.Component.month, Calendar.Component.year], from: currentDate)
        components.hour = 0
        components.minute = 0
        components.second = 0
        let dateArray = getTimes()
        guard let today = calendar.date(from: components) else { return currentDate }
        var calComponents = DateComponents()
        if selectedIndex == 0{
            return today
        }else if selectedIndex == 1{// yestorday
            calComponents.day = -1
            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
            return preDate
        }else if selectedIndex == 2{// last 3 days
            calComponents.day = -2
            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
            return preDate
        }
//        else if selectedIndex == 3{// last 7 days
//            calComponents.day = -6
//            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
//            return preDate
//        }else if selectedIndex == 4{// last week
//            calComponents.day = -6 - dateArray[6]
//            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
//            return preDate
//        }else if selectedIndex == 5{// this month
//            calComponents.day = 1 - dateArray[2]
//            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
//            return preDate
//        }else if selectedIndex == 6{//previous month
//            calComponents.day =  1 - dateArray[2]
//            calComponents.month = -1
//            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
//            return preDate
//        }
        return today
        
    }
    
    static func getEndDate(selectedIndex:Int) -> Date{
        let calendar = Calendar.current
        let currentDate = Date()
        var components = calendar.dateComponents([Calendar.Component.day, Calendar.Component.hour, Calendar.Component.minute, Calendar.Component.month, Calendar.Component.year], from: currentDate)
        components.hour = 23
        components.minute = 59
        components.second = 59
        let dateArray = getTimes()
        guard let today = calendar.date(from: components) else { return currentDate }
        var calComponents = DateComponents()
        if selectedIndex == 0{
            return today
        }else if selectedIndex == 1{// yestorday
            calComponents.day = -1
            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
            return preDate
        }else if selectedIndex == 2{// last 3 days
            return today
        }else if selectedIndex == 3{// last 7 days
            return today
        }else if selectedIndex == 4{// last week
            calComponents.day = -1 * dateArray[6]
            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
            return preDate
        }else if selectedIndex == 5{// this month
            return today
        }else if selectedIndex == 6{//previous month
            calComponents.day =  -1 * dateArray[2]
            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
            return preDate
        }
        return today
        
    }
}
