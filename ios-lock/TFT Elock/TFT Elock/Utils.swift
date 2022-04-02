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
        for index in pos ..< bytes.count {
            var Str = bytes[index].description
            if Str.count == 1 {
                Str = "0 "+Str;
            }else {
                let low = Int(Str)!%16
                let hight = Int(Str)!/16
                Str = hexIntToStr(HexInt: hight) + hexIntToStr(HexInt: low)
            }
            hexStr += Str
        }
        return hexStr
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
     
    
} 
