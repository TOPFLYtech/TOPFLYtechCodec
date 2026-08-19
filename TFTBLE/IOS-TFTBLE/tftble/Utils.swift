//
//  Utils.swift
//  tftble
//
//  Created by jeech on 2019/12/20.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import ObjectiveC

class Utils{
    static var isDebug:Bool = false
    static let fontSize:CGFloat = 12
    static func parseHardwareVersion(hardware:String) ->String{
        var hardwareF:Double = 0
        if hardware != nil{
            hardwareF = Double(hardware) as! Double
        }
        if hardwareF >= 10.0 {
            let result = hardwareF / 10.0
            return String.init(format:"V%.1f",result)
        }else{
            let result = (hardwareF - 1 + 10)/10
            return String.init(format:"V%.1f",result)
        }
        
    }
    static func parseS78910SoftwaeVersion(data:[UInt8],index:Int) ->String{
        // 检查数据长度是否足够
        if data.count < index + 2 {
            return "-"
        }
        let all = (Int(data[index]) << 8) + Int(data[index + 1])
        let version1 = data[index] >> 5
        let version2 = (all & 0x1FFF) >> 7
        let version3 = all & 0x7f
        var software = ""
        // 检查是否有testByte
        if data.count > index + 2 {
            let testByte = data[index + 2]
            if testByte != 0 {
                if let testStr = String(data: Data([testByte]), encoding: .utf8) {
                    return String(format: "%d.%d.%02d %@", version1, version2, version3, testStr)
                }
            }
        }
        return String(format: "%d.%d.%02d", version1, version2, version3)
    }
    static func hexStringToUInt8Array(_ hexString: String) -> [UInt8] {
        var hex = hexString.trimmingCharacters(in: .whitespacesAndNewlines)
        if hex.hasPrefix("0x") || hex.hasPrefix("0X") {
            hex = String(hex.dropFirst(2))
        }
        
        guard hex.count % 2 == 0 else {
            print("Invalid hex string. Length must be even.")
            return []
        }
        
        var byteArray = [UInt8]()
        for i in stride(from: 0, to: hex.count, by: 2) {
            let startIndex = hex.index(hex.startIndex, offsetBy: i)
            let endIndex = hex.index(startIndex, offsetBy: 2)
            let byteString = hex[startIndex..<endIndex]
            
            if let byteValue = UInt8(byteString, radix: 16) {
                byteArray.append(byteValue)
            } else {
                print("Invalid hex string. Contains non-hexadecimal characters.")
                return []
            }
        }
        
        return byteArray
    }
    
    static func uint8ArrayToHexStr(value:[UInt8]) ->String{
        let hexString = value.map { String(format: "%02hhx", $0) }.joined()
        return hexString
    }
    
    static func uint8ToHexStr(value:UInt8) ->String{
        return String(format:"%02X",value).uppercased()
    }
    
    static func getCurTemp(sourceTemp:Float) ->Float{
        if sourceTemp == -999{
            return sourceTemp
        }
        let tempUnit = UserDefaults.standard.integer(forKey: "tempUnit") 
        if tempUnit == 0{
            return sourceTemp
        }else{
            return 32 + sourceTemp * 1.8
        }
    }
    static func getSourceTemp(sourceTemp:Float) ->Float{
        if sourceTemp == -999{
            return sourceTemp
        }
        let tempUnit = UserDefaults.standard.integer(forKey: "tempUnit")
        if tempUnit == 0{
            return sourceTemp
        }else{
            return (sourceTemp - 32) / 1.8
        }
    }
    static func getCurTempUnit() -> String{
        let tempUnit = UserDefaults.standard.integer(forKey: "tempUnit")
        if tempUnit == 0{
            return "˚C"
        }else{
            return "˚F"
        }
    }
    
    static func getCurPressure(sourcePressure:Float) ->Float{
        let pressureUnit = UserDefaults.standard.integer(forKey: "pressureUnit")
        if pressureUnit == 0{
            return sourcePressure
        }else{
            return sourcePressure / 0.1450377
        }
    }
    
    static func getCurPressureUnit() -> String{
        let pressureUnit = UserDefaults.standard.integer(forKey: "pressureUnit")
        if pressureUnit == 0{
            return "Kpa"
        }else{
            return "Psi"
        }
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
    
    static func IntToBytes(intValue:Int)->[UInt8]{
        var byteArray = [UInt8]()
        
        byteArray.append(UInt8((intValue >> 24) & 0xff))
        byteArray.append(UInt8((intValue >> 16) & 0xff))
        byteArray.append(UInt8((intValue >> 8) & 0xff))
        byteArray.append(UInt8(intValue & 0xff))
        return byteArray;
    }
    
    static func isHexadecimal(_ string: String) -> Bool {
        let regex = try! NSRegularExpression(pattern: "^[0-9a-fA-F]+$")
        let range = NSRange(location: 0, length: string.utf16.count)
        return regex.firstMatch(in: string, options: [], range: range) != nil
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
    static func unSignedInt2Bytes(_ num: Int64) -> [UInt8] {
        var byteNum = [UInt8](repeating: 0, count: 4)
        for ix in 0..<4 {
            let offset = 32 - (ix + 1) * 8
            byteNum[ix] = UInt8((num >> offset) & 0xff)
        }
        return byteNum
    }
    static func bytes2Short(bytes:[UInt8],offset:Int) ->Int{
        if bytes.count > 2 && bytes.count > offset + 1{
            return (Int(bytes[offset]) << 8) + Int(bytes[offset + 1])
        }
        return 0
    }
    static func data2Short(bytes:Data,offset:Int) ->Int{
        if bytes.count > 2 && bytes.count > offset + 1{
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
    
    static func getNextTempUnit() -> String{
        let tempUnit = UserDefaults.standard.integer(forKey: "tempUnit")
        if tempUnit == 0{
            return "˚F"
        }else{
            return "˚C"
        }
    }
    static func switchCurTempUnit(){
        let tempUnit = UserDefaults.standard.integer(forKey: "tempUnit")
        if tempUnit == 0{
            UserDefaults.standard.set(1, forKey: "tempUnit")
        }else{
            UserDefaults.standard.set(0, forKey: "tempUnit")
        }
    }
    
    static func getNextPressureUnit() -> String{
        let pressureUnit = UserDefaults.standard.integer(forKey: "pressureUnit")
        if pressureUnit == 0{
            return "Psi"
        }else{
            return "Kpa"
        }
    }
    static func switchCurPressureUnit(){
        let pressureUnit = UserDefaults.standard.integer(forKey: "pressureUnit")
        if pressureUnit == 0{
            UserDefaults.standard.set(1, forKey: "pressureUnit")
        }else{
            UserDefaults.standard.set(0, forKey: "pressureUnit")
        }
    }
    
    static func getS04WarnDesc(warn:Int) ->String{
        var warnStr = ""
        if (warn & 0x01) == 0x01 {
            warnStr += NSLocalizedString("high_temperature_warning", comment:"High temperature warning;")
        }
        if (warn & 0x10) == 0x10 {
            warnStr += NSLocalizedString("low_temperature_warning", comment:"Low temperature warning;")
        }
        if (warn & 0x40) == 0x40{
            warnStr += NSLocalizedString("low_voltage_warning", comment:"Low voltage warning;")
        }
        if (warn & 0x80) == 0x80{
            warnStr += NSLocalizedString("door_change_warning", comment:"Door change warning;")
        }
        return warnStr
    }
    
    static func sortOriginHisData(originHistoryList:[[[UInt8]]]){
        var tempItem:[[UInt8]]
        var i = 0
        while i < originHistoryList.count - 1{
            var itemI = originHistoryList[i]
            var indexI = itemI[0][0] & 0xff
            var j = i+1
            while j < originHistoryList.count{
                var itemJ = originHistoryList[j]
                var indexJ = itemI[0][0] & 0xff
                if indexJ < indexI{
                    tempItem = itemI
                    itemI = itemJ
                    itemJ = tempItem
                }
                j+=1
            }
            i+=1
        }
        var itemIndex = 0
        while itemIndex < originHistoryList.count{
            i = 0
            var temp:[UInt8]
            var item = originHistoryList[itemIndex]
            while i < item.count - 1{
                var itemI = item[i]
                var indexI = Utils.bytes2Short(bytes: itemI, offset: 0)
                var j = i+1
                while j < item.count{
                    var itemJ = item[j]
                    var indexJ = Utils.bytes2Short(bytes: itemJ, offset: 0)
                    if indexJ < indexI{
                        temp = itemI
                        itemI = itemJ
                        itemJ = temp
                    }
                    j+=1
                }
                i+=1
            }
            itemIndex += 1
        }
    }
    
    static func bytes2HexString(bytes: [UInt8], index: Int) -> String? {
        // 检查输入的有效性
        guard !bytes.isEmpty, index < bytes.count else {
            return nil
        }

        var builder = ""

        for i in index..<bytes.count {
            let byte = bytes[i]
            // 将字节转换为十六进制字符串
            let hex = String(format: "%02x", byte)
            builder.append(hex)
        }
        
        return builder
    }
    
    static func merOriginHisData(originHistoryList:[[[UInt8]]]) ->[[UInt8]]{
        var result = [[UInt8]]()
        Utils.sortOriginHisData(originHistoryList: originHistoryList)
        var itemIndex = 0
        while itemIndex < originHistoryList.count{
            var item = originHistoryList[itemIndex]
            var i = 1
            var itemAllData = [UInt8]()
            while i < item.count{
                var byteData = item[i]
                var j = 2
                while j < byteData.count{
                    itemAllData.append(byteData[j])
                    j+=1
                }
                i += 1
            }
            result.append(itemAllData)
            itemIndex += 1
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
    static func dealS10InvalidData(dataList: [BleHisData]) {
        let THRESHOLD: Double = 0.0001
        for item in dataList {
            if abs(Double(item.temp) - 144.4) < THRESHOLD {
                item.temp = -999
            }
            if abs(Double(item.humidity) - 126) < THRESHOLD {
                item.humidity = -999
            }
        }
    }
    static func parseS10BleGX112HisData(historyArray:[UInt8]) ->[BleHisData]{
        let timeIntervalTemp = Int(historyArray[1] & 0x3f) * Int((historyArray[1] & 0x40 == 0x40) ? 10 : 5)
        let dataCount = Utils.bytes2Short(bytes: historyArray,offset: 2)
        let timeInterval = Int(timeIntervalTemp)
        let timestamp = Utils.bytes2Integer(buf: historyArray,pos: 4)
        var curTimestamp = 0
        var result = [BleHisData]()
        if dataCount == 0{
            dealS10InvalidData(dataList:result)
            return result;
        }
        for j in 0..<4{
            var startIndex = 0
            if j == 0{
                startIndex = 8;
                curTimestamp = timestamp
            }
            let remainByte = Utils.arraysCopyOfRange(src: historyArray, from: j * 256 + startIndex, to: j * 256 + 256)
            let remainBinaryStr = self.convertByteArrayToBinaryStr(byteArray: remainByte)
            let remainBinaryChar = Utils.splitSingleStrToCharArr(strToSplit: remainBinaryStr)
            var i = 0
            
            var tempCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar, from: i, to: i + 12)
            i+=12
            var batteryCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar,from: i,to: i+8)
            i+=8
            var tempLong = Utils.binaryCharArrayToLong(binaryArray: tempCharArray)
            var battery = Utils.binaryCharArrayToLong(binaryArray: batteryCharArray)
            var temp = Float(tempLong) * 0.1 - 100
            var item = BleHisData()
            item.temp = temp
            item.battery = Int(battery)
            item.humidity = -999
            item.dateStamp = curTimestamp
            result.append(item)
            if result.count >= dataCount{
                return result;
            }
            curTimestamp += timeInterval
            var dataSize = remainBinaryChar.count
            while i < dataSize{
                var charItem = remainBinaryChar[i]
                i+=1
                if charItem == "0"{//not change
                    var lastItem = result[result.count - 1]
                    var curItem = BleHisData()
                    curItem.dateStamp = curTimestamp
                    curItem.humidity = lastItem.humidity
                    curItem.temp = lastItem.temp
                    curItem.battery = lastItem.battery
                    curItem.prop = lastItem.prop
                    result.append(curItem)
                    if result.count >= dataCount{
                        return result
                    }
                    curTimestamp += timeInterval
                }else{
                    if i+1 >= dataSize{
                        break
                    }
                    let type = remainBinaryChar[i]
                    i+=1
                    if type == "0"{// 12 bit temp
                        if i+12 >= dataSize{
                            break
                        }
                        tempCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar, from: i, to: i + 12)
                        i+=12
                        tempLong = Utils.binaryCharArrayToLong(binaryArray: tempCharArray)
                        var lastItem = result[result.count - 1]
                        temp = Float(tempLong) * 0.1 - 100
                        var curItem = BleHisData()
                        curItem.dateStamp = curTimestamp
                        curItem.humidity = lastItem.humidity
                        curItem.temp = temp
                        curItem.battery = lastItem.battery
                        curItem.prop = lastItem.prop
                        result.append(curItem)
                        if result.count >= dataCount{
                            return result
                        }
                        curTimestamp += timeInterval
                    }else{
                        if i+1 >= dataSize{
                            break
                        }
                        var lastItem = result[result.count - 1]
                        let isAdd = remainBinaryChar[i] == "0" ? 1 : -1
                        i+=1
                        var curItem = BleHisData()
                        curItem.dateStamp = curTimestamp
                        curItem.humidity = lastItem.humidity
                        curItem.temp = lastItem.temp + Float(isAdd) * 0.1
                        curItem.battery = lastItem.battery
                        curItem.prop = lastItem.prop
                        result.append(curItem)
                        if result.count >= dataCount{
                            return result
                        }
                        curTimestamp += timeInterval
                        
                    }
                }
            }
            
        }
        return result;
        
    }
    
    static func parseS10BleGXHT30HisData(historyArray: [UInt8]) -> [BleHisData] {
            let timeIntervalTemp = Int(historyArray[1] & 0x3f) * Int((historyArray[1] & 0x40 == 0x40) ? 10 : 5)
            let dataCount = Utils.bytes2Short(bytes: historyArray, offset: 2)
            let timeInterval = Int(timeIntervalTemp)
            let timestamp = Utils.bytes2Integer(buf: historyArray, pos: 4)
            var curTimestamp = 0
            var result = [BleHisData]()
            
            print("time :\(timestamp);timeInterval \(timeInterval)")
            
            if dataCount == 0 {
                return result
            }
            
            for j in 0...4 {
                var startIndex = 0
                if j == 0 {
                    startIndex = 8
                    curTimestamp = timestamp
                }
                
                let remainByte = Utils.arraysCopyOfRange(src: historyArray, from: j * 256 + startIndex, to: j * 256 + 256)
                // 假设 convertByteArrayToBinaryStr 已经在类中定义，与提供的示例一致
                var remainBinaryStr = self.convertByteArrayToBinaryStr(byteArray: remainByte)
                
                // 补齐位数逻辑 (对应 Java: if(remainBinaryStr.length() < remainByte.length * 8)...)
                // 注意：convertByteArrayToBinaryStr 通常已经处理了补0，但为了保持逻辑一致，保留检查
                if remainBinaryStr.count < remainByte.count * 8 {
                    let needFixLen = remainByte.count * 8 - remainBinaryStr.count
                    for _ in 0..<needFixLen {
                        remainBinaryStr = "0" + remainBinaryStr
                    }
                }
                
                let remainBinaryChar = Utils.splitSingleStrToCharArr(strToSplit: remainBinaryStr)
                var i = 0
                
                // 解析首个完整数据包
                // 12 bit Temp
                var tempCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar, from: i, to: i + 12)
                i += 12
                // 6 bit Humidity
                var humidityCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar, from: i, to: i + 6)
                i += 6
                // 8 bit Battery
                var batteryCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar, from: i, to: i + 8)
                i += 8
                
                let tempLong = Utils.binaryCharArrayToLong(binaryArray: tempCharArray)
                let humidityLong = Utils.binaryCharArrayToLong(binaryArray: humidityCharArray)
                let battery = Utils.binaryCharArrayToLong(binaryArray: batteryCharArray)
                
                var item = BleHisData()
                item.temp = Float(tempLong) * 0.1 - 100
                item.battery = Int(battery)
                item.dateStamp = curTimestamp
                item.humidity = Int(humidityLong * 2)
                result.append(item)
                
                if result.count >= dataCount {
                    return result
                }
                
                curTimestamp += timeInterval
                let dataSize = remainBinaryChar.count
                
                // 循环解析压缩数据
                while i < dataSize {
                    let charItem = remainBinaryChar[i]
                    i += 1
                    
                    if charItem == "0" { // not change
                        let lastItem = result[result.count - 1]
                        var curItem = BleHisData()
                        curItem.dateStamp = curTimestamp
                        curItem.temp = lastItem.temp
                        curItem.battery = lastItem.battery
                        curItem.prop = lastItem.prop
                        curItem.humidity = lastItem.humidity
                        result.append(curItem)
                        
                        if result.count >= dataCount {
                            return result
                        }
                        curTimestamp += timeInterval
                    } else {
                        if i + 1 >= dataSize {
                            break
                        }
                        let type = remainBinaryChar[i]
                        i += 1
                        
                        if type == "0" { // 18 bit refresh (12 temp + 6 humidity)
                            if i + 18 >= dataSize {
                                break
                            }
                            tempCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar, from: i, to: i + 12)
                            i += 12
                            humidityCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar, from: i, to: i + 6)
                            i += 6
                            
                            let tLong = Utils.binaryCharArrayToLong(binaryArray: tempCharArray)
                            let hLong = Utils.binaryCharArrayToLong(binaryArray: humidityCharArray)
                            
                            var curItem = BleHisData()
                            curItem.temp = Float(tLong) * 0.1 - 100
                            curItem.battery = Int(battery) // 注意：这里使用了最初解析的battery，与Java逻辑一致
                            curItem.dateStamp = curTimestamp
                            curItem.humidity = Int(hLong * 2)
                            result.append(curItem)
                            
                            if result.count >= dataCount {
                                return result
                            }
                            curTimestamp += timeInterval
                            
                        } else { // type == '1', incremental change
                            if i + 2 >= dataSize {
                                break
                            }
                            let firstChar = remainBinaryChar[i]
                            i += 1
                            let secondChar = remainBinaryChar[i]
                            i += 1
                            
                            if firstChar == "0" && secondChar == "0" {
                                // Copy last item strictly
                                let lastItem = result[result.count - 1]
                                var curItem = BleHisData()
                                curItem.dateStamp = curTimestamp
                                curItem.humidity = lastItem.humidity
                                curItem.temp = lastItem.temp
                                curItem.battery = lastItem.battery
                                curItem.prop = lastItem.prop
                                result.append(curItem)
                                
                                if result.count >= dataCount {
                                    return result
                                }
                                curTimestamp += timeInterval
                                
                            } else if firstChar == "0" && secondChar == "1" { // Humidity change
                                if i + 1 >= dataSize {
                                    break
                                }
                                let changeChar = remainBinaryChar[i]
                                i += 1
                                
                                let lastItem = result[result.count - 1]
                                var curItem = BleHisData()
                                curItem.dateStamp = curTimestamp
                                curItem.temp = lastItem.temp
                                curItem.battery = lastItem.battery
                                curItem.prop = lastItem.prop
                                
                                if changeChar == "0" {
                                    curItem.humidity = lastItem.humidity + 2
                                } else {
                                    curItem.humidity = lastItem.humidity - 2
                                }
                                result.append(curItem)
                                
                                if result.count >= dataCount {
                                    return result
                                }
                                curTimestamp += timeInterval
                                
                            } else if firstChar == "1" && secondChar == "0" { // Temp change
                                if i + 1 >= dataSize {
                                    break
                                }
                                let changeChar = remainBinaryChar[i]
                                i += 1
                                
                                let lastItem = result[result.count - 1]
                                var curItem = BleHisData()
                                curItem.dateStamp = curTimestamp
                                curItem.humidity = lastItem.humidity
                                curItem.battery = lastItem.battery
                                curItem.prop = lastItem.prop
                                
                                if changeChar == "0" {
                                    curItem.temp = lastItem.temp + 0.1
                                } else {
                                    curItem.temp = lastItem.temp - 0.1
                                }
                                result.append(curItem)
                                
                                if result.count >= dataCount {
                                    return result
                                }
                                curTimestamp += timeInterval
                                
                            } else { // firstChar == "1" && secondChar == "1" -> Both Change
                                // Temp change
                                if i + 1 >= dataSize {
                                    break // basic safety check, though we technically need 2 bits
                                }
                                let changeTempChar = remainBinaryChar[i]
                                i += 1
                                
                                var tempVal: Float = 0.0
                                let lastItem = result[result.count - 1]
                                
                                if changeTempChar == "0" {
                                    tempVal = lastItem.temp + 0.1
                                } else {
                                    tempVal = lastItem.temp - 0.1
                                }
                                
                                // Humidity change
                                if i + 1 > dataSize { // check strict bound
                                    break
                                }
                                let changeHumidityChar = remainBinaryChar[i]
                                i += 1
                                
                                var humVal: Int = 0
                                if changeHumidityChar == "0" {
                                    humVal = lastItem.humidity + 2
                                } else {
                                    humVal = lastItem.humidity - 2
                                }
                                
                                var curItem = BleHisData()
                                curItem.dateStamp = curTimestamp
                                curItem.humidity = humVal
                                curItem.temp = tempVal
                                curItem.battery = lastItem.battery
                                curItem.prop = lastItem.prop
                                result.append(curItem)
                                
                                if result.count >= dataCount {
                                    return result
                                }
                                curTimestamp += timeInterval
                            }
                        }
                    }
                }
            }
            return result
        }
    
    
    static func parseS02BleHisData(historyArray:[UInt8]) ->[BleHisData]{
        var timeIntervalTemp = Int(historyArray[1] & 0x3f) * Int((historyArray[1] & 0x40 == 0x40) ? 10 : 5)
        var dataCount = Utils.bytes2Short(bytes: historyArray,offset: 2)
        var timeInterval = Int(timeIntervalTemp)
        var timestamp = Utils.bytes2Integer(buf: historyArray,pos: 4)
        var curTimestamp = 0
        var result = [BleHisData]()
        if dataCount == 0{
            return result;
        }
        for j in 0..<4{
            var startIndex = 0
            if j == 0{
                startIndex = 8;
                curTimestamp = timestamp
            }
            var item = BleHisData()
            var prop = (historyArray[j * 256 + startIndex + 0] & 0x80) == 0x80 ? 1 : 0;
            var value = Utils.bytes2Short(bytes: historyArray,offset: j * 256 + startIndex + 0) & 0x7fff
            var temp = Float(((value & 0x7FC0) >> 6) * 2 - 300 )
            temp = temp  / 10.0
            var humidity = (value & 0x3F) * 2
            var battery = historyArray[j * 256 + startIndex + 2] & 0xff
            item.prop = prop
            item.temp = temp
            item.battery = Int(battery)
            item.humidity = humidity
            item.dateStamp = curTimestamp
            result.append(item)
            if result.count >= dataCount{
                return result;
            }
            curTimestamp += timeInterval
            var remainByte = Utils.arraysCopyOfRange(src: historyArray, from: j * 256 + startIndex + 3, to: j * 256 + 256)
            var remainBinaryStr = self.convertByteArrayToBinaryStr(byteArray: remainByte)
            var remainBinaryChar = Utils.splitSingleStrToCharArr(strToSplit: remainBinaryStr)
            var i = 0;
            var dataSize = remainBinaryChar.count
            while i < dataSize{
                var charItem = remainBinaryChar[i]
                i+=1
                if charItem == "0"{//not change
                    var lastItem = result[result.count - 1]
                    var curItem = BleHisData()
                    curItem.dateStamp = curTimestamp
                    curItem.humidity = lastItem.humidity
                    curItem.temp = lastItem.temp
                    curItem.battery = lastItem.battery
                    curItem.prop = lastItem.prop
                    result.append(curItem)
                    if result.count >= dataCount{
                        return result
                    }
                    curTimestamp += timeInterval
                }else{
                    if i >= dataSize{
                        break
                    }
                    let propStatus = remainBinaryChar[i]
                    i+=1
                    if i >= dataSize{
                        break
                    }
                    let tempHumidityStatus = remainBinaryChar[i]
                    i+=1
                    if tempHumidityStatus == "1"{
                        if(i+2 > dataSize){
                            break
                        }
                        let firstChar = remainBinaryChar[i]
                        i+=1
                        let secondChar = remainBinaryChar[i]
                        i+=1
                        if firstChar == "0" && secondChar == "0"{
                            let lastItem = result[result.count - 1]
                            let curItem = BleHisData()
                            curItem.dateStamp = curTimestamp
                            curItem.humidity = lastItem.humidity
                            curItem.temp = lastItem.temp
                            curItem.battery = lastItem.battery
                            curItem.prop = propStatus == "1" ? 1 : 0
                            result.append(curItem)
                            if result.count >= dataCount{
                                return result
                            }
                            curTimestamp += timeInterval;
                        }else if firstChar == "0" && secondChar == "1"{//humidity change
                            if i >= dataSize{
                                break
                            }
                            var changeChar = remainBinaryChar[i];
                            i+=1
                            if changeChar == "0"{
                                var lastItem = result[result.count - 1]
                                var curItem = BleHisData()
                                curItem.dateStamp = curTimestamp
                                curItem.humidity = lastItem.humidity + 2
                                curItem.temp = lastItem.temp
                                curItem.battery = lastItem.battery
                                curItem.prop = propStatus == "1" ? 1 : 0
                                result.append(curItem)
                                if result.count >= dataCount{
                                    return result
                                }
                                curTimestamp += timeInterval;
                            }else{
                                var lastItem = result[result.count - 1]
                                var curItem = BleHisData()
                                curItem.dateStamp = curTimestamp
                                curItem.humidity = lastItem.humidity - 2
                                curItem.temp = lastItem.temp
                                curItem.battery = lastItem.battery
                                curItem.prop = propStatus == "1" ? 1 : 0
                                result.append(curItem)
                                if result.count >= dataCount{
                                    return result
                                }
                                curTimestamp += timeInterval;
                            }
                        }else if firstChar == "1" && secondChar == "0" {//temp change
                            if i >= dataSize{
                                break
                            }
                            var changeChar = remainBinaryChar[i]
                            i+=1
                            if changeChar == "0"{
                                var lastItem = result[result.count - 1]
                                var curItem = BleHisData()
                                curItem.dateStamp = curTimestamp
                                curItem.humidity = lastItem.humidity
                                curItem.temp = lastItem.temp + 0.2
                                curItem.battery = lastItem.battery
                                curItem.prop = propStatus == "1" ? 1 : 0
                                result.append(curItem)
                                if result.count >= dataCount{
                                    return result
                                }
                                curTimestamp += timeInterval
                            }else{
                                var lastItem = result[result.count - 1]
                                var curItem = BleHisData()
                                curItem.dateStamp = curTimestamp
                                curItem.humidity = lastItem.humidity
                                curItem.temp = lastItem.temp - 0.2
                                curItem.battery = lastItem.battery
                                curItem.prop = propStatus == "1" ? 1 : 0
                                result.append(curItem)
                                if result.count >= dataCount{
                                    return result
                                }
                                curTimestamp += timeInterval;
                            }
                        }else{
                            if i+2 > dataSize{
                                break
                            }
                            var changeFirstChar = remainBinaryChar[i]
                            i+=1
                            var changeSecondChar = remainBinaryChar[i]
                            i+=1
                            var tempChange = changeFirstChar == "0" ? 0.2 : -0.2
                            var humidityChange = changeSecondChar == "0" ? 2 : -2
                            var lastItem = result[result.count - 1]
                            var curItem = BleHisData()
                            curItem.dateStamp = curTimestamp
                            curItem.humidity = lastItem.humidity + humidityChange
                            curItem.temp = (lastItem.temp ?? 0) + Float(tempChange)
                            curItem.battery = lastItem.battery
                            curItem.prop = propStatus == "1" ? 1 : 0
                            result.append(curItem)
                            if result.count >= dataCount{
                                return result
                            }
                            curTimestamp += timeInterval
                        }
                    }else{
                        if i+15 > dataSize{
                            break
                        }
                        var tempCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar, from: i, to: i + 9)
                        i+=9
                        var humidityCharArray = Utils.arraysCopyOfRange(src: remainBinaryChar,from: i,to: i+6)
                        i+=6
                        var tempLong = Utils.binaryCharArrayToLong(binaryArray: tempCharArray)
                        var humidityLong = Utils.binaryCharArrayToLong(binaryArray: humidityCharArray)
                        var curTemp = Float(tempLong * 2 - 300 )
                        curTemp = curTemp / 10.0
                        var curHumidity = humidityLong * 2
                        var lastItem = result[result.count - 1]
                        var curItem = BleHisData()
                        curItem.dateStamp = curTimestamp
                        curItem.humidity = curHumidity
                        curItem.temp = curTemp
                        curItem.battery = lastItem.battery
                        curItem.prop = propStatus == "1" ? 1 : 0
                        result.append(curItem)
                        if result.count >= dataCount{
                            return result
                        }
                        curTimestamp += timeInterval
                    }
                }
            }
            
        }
        
        
        return result;
        
    }
    
    static func checkOriginHisDataCrc(byteDataArray:[UInt8]) ->Bool{
        let srcCrc = byteDataArray[0] & 0xff
        let myCalCrc = self.calHisDataCrc(calArray: byteDataArray, startIndex: 1, len: 1023)
        if(myCalCrc == srcCrc){
            return true
        }else{
            return false
        }
    }
    
    static func calHisDataCrc(calArray:[UInt8],startIndex:Int,len:Int) ->UInt8{
        var crc:UInt8!
        crc = 0xff
        var i = 0
        var j = startIndex
        let endIndex = startIndex + len
        if endIndex > calArray.count{
            return 0
        }
        while j < endIndex{
            crc = crc ^ calArray[j]
            i = 0
            while i < 8{
                if (crc & 0x80) != 0{
                    crc = (crc << 1) ^ 0x31
                }else{
                    crc = crc << 1
                }
                i+=1
            }
            j+=1
        }
        return crc & 0xff
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
        let result = [NSLocalizedString("today", comment: "Today"),NSLocalizedString("yestory", comment: "Yestory"),NSLocalizedString("last_3_days", comment: "Last 3 days"),NSLocalizedString("last_7_days", comment: "Last 7 days"),NSLocalizedString("last_week", comment: "Last week"),NSLocalizedString("this_month", comment: "This month"),NSLocalizedString("previous_month", comment: "Previous month"),NSLocalizedString("custom", comment: "Custom")]
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
        }else if selectedIndex == 3{// last 7 days
            calComponents.day = -6
            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
            return preDate
        }else if selectedIndex == 4{// last week
            calComponents.day = -6 - dateArray[6]
            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
            return preDate
        }else if selectedIndex == 5{// this month
            calComponents.day = 1 - dateArray[2]
            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
            return preDate
        }else if selectedIndex == 6{//previous month
            calComponents.day =  1 - dateArray[2]
            calComponents.month = -1
            let preDate = calendar.date(byAdding: calComponents, to: today) ?? currentDate
            return preDate
        }
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

private final class TFTTopBarState {
    let topBarView = UIView()
    let titleLabel = UILabel()
    let leftButton = UIButton(type: .custom)
    let rightButton = UIButton(type: .custom)
    var hasRightButton = false
}

private var tftTopBarStateKey: UInt8 = 0
private var tftInitialContentTopKey: UInt8 = 0
private var tftAppliedContentDeltaKey: UInt8 = 0

extension UIViewController {
    private var tftTopBarState: TFTTopBarState? {
        get { objc_getAssociatedObject(self, &tftTopBarStateKey) as? TFTTopBarState }
        set { objc_setAssociatedObject(self, &tftTopBarStateKey, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC) }
    }
    
    private var tftInitialContentTop: CGFloat? {
        get { objc_getAssociatedObject(self, &tftInitialContentTopKey) as? CGFloat }
        set { objc_setAssociatedObject(self, &tftInitialContentTopKey, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC) }
    }
    
    private var tftAppliedContentDelta: CGFloat {
        get { (objc_getAssociatedObject(self, &tftAppliedContentDeltaKey) as? CGFloat) ?? 0 }
        set { objc_setAssociatedObject(self, &tftAppliedContentDeltaKey, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC) }
    }
    
    func tftInstallTopBar(title: String, rightIconName: String? = nil, rightAction: Selector? = nil) {
        if self.tftTopBarState == nil {
            let state = TFTTopBarState()
            state.topBarView.backgroundColor = UIColor.colorPrimary
            state.titleLabel.textAlignment = .center
            state.titleLabel.font = UIFont.boldSystemFont(ofSize: 22)
            state.leftButton.frame = CGRect(x: 0, y: 0, width: 30, height: 30)
            state.leftButton.setImage(self.tftRenderTopIcon(named: "ic_back.png") ?? self.tftRenderTopIcon(named: "player_back"), for: .normal)
            state.leftButton.addTarget(self, action: #selector(tftBackClick), for: .touchUpInside)
            state.rightButton.frame = CGRect(x: 0, y: 0, width: 30, height: 30)
            state.topBarView.addSubview(state.leftButton)
            state.topBarView.addSubview(state.titleLabel)
            self.view.addSubview(state.topBarView)
            self.tftTopBarState = state
        }
        
        guard let state = self.tftTopBarState else { return }
        state.titleLabel.text = title
        state.rightButton.removeFromSuperview()
        state.hasRightButton = false
        
        if let rightIconName = rightIconName, let rightAction = rightAction {
            state.hasRightButton = true
            state.rightButton.setImage(self.tftRenderTopIcon(named: rightIconName), for: .normal)
            state.rightButton.removeTarget(nil, action: nil, for: .allEvents)
            state.rightButton.addTarget(self, action: rightAction, for: .touchUpInside)
            state.topBarView.addSubview(state.rightButton)
        }
        
        self.navigationItem.hidesBackButton = true
        self.navigationItem.leftBarButtonItem = nil
        self.navigationItem.rightBarButtonItem = nil
        self.tftLayoutTopBar()
    }
    
    func tftLayoutTopBar() {
        guard let state = self.tftTopBarState else { return }
        // Re-apply hidden nav bar each layout pass to avoid iOS 26 restoring it unexpectedly.
        self.navigationController?.setNavigationBarHidden(true, animated: false)
        self.navigationItem.hidesBackButton = true
        self.navigationItem.leftBarButtonItem = nil
        self.navigationItem.rightBarButtonItem = nil
        let safeTop = self.tftCurrentSafeTopInset()
        let barHeight = safeTop + 56
        let width = self.view.bounds.width
        state.topBarView.frame = CGRect(x: 0, y: 0, width: width, height: barHeight)
        state.leftButton.frame = CGRect(x: 12, y: safeTop + 13, width: 30, height: 30)
        if state.hasRightButton {
            state.rightButton.frame = CGRect(x: width - 44, y: safeTop + 13, width: 30, height: 30)
        }
        state.titleLabel.frame = CGRect(x: 56, y: safeTop + 8, width: width - 112, height: 40)
        self.view.bringSubviewToFront(state.topBarView)
    }
    
    func tftShowTopBar() {
        self.navigationController?.setNavigationBarHidden(true, animated: false)
        self.navigationItem.hidesBackButton = true
        self.navigationItem.leftBarButtonItem = nil
        self.navigationItem.rightBarButtonItem = nil
        self.tftLayoutTopBar()
    }
    
    func tftHideTopBar() {
        self.navigationController?.setNavigationBarHidden(false, animated: false)
    }
    
    func tftTopContentY() -> CGFloat {
        return self.tftCurrentSafeTopInset() + 56
    }
    
    func tftRememberInitialContentTop(_ top: CGFloat) {
        if self.tftInitialContentTop == nil {
            self.tftInitialContentTop = top
        }
    }
    
    func tftAdjustContentBelowTopBarIfNeeded() {
        guard let state = self.tftTopBarState, let initialTop = self.tftInitialContentTop else { return }
        let targetTop = self.tftTopContentY()
        let newDelta = targetTop - initialTop
        let step = newDelta - self.tftAppliedContentDelta
        if abs(step) < 0.5 { return }
        for subview in self.view.subviews where subview !== state.topBarView {
            subview.frame = subview.frame.offsetBy(dx: 0, dy: step)
        }
        self.tftAppliedContentDelta = newDelta
    }
    
    func tftEnsureTopContentClearance(padding: CGFloat = 8) {
        guard let state = self.tftTopBarState else { return }
        let minAllowedY = self.tftTopContentY() + padding
        var currentMinY = CGFloat.greatestFiniteMagnitude
        for subview in self.view.subviews where subview !== state.topBarView {
            currentMinY = min(currentMinY, subview.frame.minY)
        }
        if currentMinY == CGFloat.greatestFiniteMagnitude {
            return
        }
        let shift = minAllowedY - currentMinY
        if shift <= 0.5 {
            return
        }
        for subview in self.view.subviews where subview !== state.topBarView {
            subview.frame = subview.frame.offsetBy(dx: 0, dy: shift)
        }
        self.tftAppliedContentDelta += shift
    }
    
    func tftSetTopBarDebugBorder(color: UIColor, width: CGFloat) {
        guard let state = self.tftTopBarState else { return }
        state.topBarView.layer.borderColor = color.cgColor
        state.topBarView.layer.borderWidth = width
    }
    
    func tftCurrentSafeTopInset() -> CGFloat {
        return self.tftSafeTopInset()
    }
    
    @objc private func tftBackClick() {
        self.navigationController?.popViewController(animated: true)
    }
    
    private func tftRenderTopIcon(named: String) -> UIImage? {
        guard let original = UIImage(named: named) else { return nil }
        let size = CGSize(width: 24, height: 24)
        let renderer = UIGraphicsImageRenderer(size: size)
        return renderer.image { _ in
            original.draw(in: CGRect(origin: .zero, size: size))
        }
    }
    
    private func tftSafeTopInset() -> CGFloat {
        let inset = self.view.safeAreaInsets.top
        if inset > 0 { return inset }
        if let navInset = self.navigationController?.view.safeAreaInsets.top, navInset > 0 {
            return navInset
        }
        if let windowInset = self.view.window?.safeAreaInsets.top, windowInset > 0 {
            return windowInset
        }
        if #available(iOS 13.0, *) {
            if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let statusHeight = scene.statusBarManager?.statusBarFrame.height,
               statusHeight > 0 {
                return statusHeight
            }
            for scene in UIApplication.shared.connectedScenes {
                if let windowScene = scene as? UIWindowScene {
                    if let window = windowScene.windows.first(where: { $0.isKeyWindow }) {
                        return window.safeAreaInsets.top
                    }
                }
            }
            if let keyWindow = UIApplication.shared.windows.first(where: { $0.isKeyWindow }) {
                let keyInset = keyWindow.safeAreaInsets.top
                if keyInset > 0 {
                    return keyInset
                }
            }
        } else {
            let statusHeight = UIApplication.shared.statusBarFrame.height
            if statusHeight > 0 {
                return statusHeight
            }
        }
        return 20
    }
}

final class QMUIGhostButton: UIButton {
    var ghostColor: UIColor = .systemBlue {
        didSet {
            applyGhostStyle()
        }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        commonInit()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        commonInit()
    }

    override var isEnabled: Bool {
        didSet {
            alpha = isEnabled ? 1.0 : 0.5
        }
    }

    private func commonInit() {
        layer.cornerRadius = 12
        layer.borderWidth = 1
        clipsToBounds = true
        titleLabel?.font = UIFont.systemFont(ofSize: 12)
        applyGhostStyle()
    }

    private func applyGhostStyle() {
        setTitleColor(ghostColor, for: .normal)
        layer.borderColor = ghostColor.cgColor
    }
}
