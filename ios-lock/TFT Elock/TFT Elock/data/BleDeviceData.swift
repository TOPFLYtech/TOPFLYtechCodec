//
//  BleDeviceData.swift
//  TFTElock
//
//  Created by jeech on 2021/6/8.
//  Copyright © 2021 com.tftiot. All rights reserved.
//

import Foundation
class BleDeviceData{
    public var deviceName:String!
    public var mac:String!
    public var imei:String!
    public var rssi:String!
    public var date:String!
    public var id:String!
    public var model:String!
    public var software:String!
    public var hardware:String!
    public var voltage:Float!
    
    
    static func parseModel(protocolByte:UInt8) -> String{
        if protocolByte == 0x62{
            return "SolarGuardX 100"
        }else if protocolByte == 0x65{
            return "SolarGuardX 200"
        }
        return ""
    }
    
    static func parseRawData(rawData:[UInt8]) ->[UInt8:[UInt8]]{
        var result:[UInt8:[UInt8]] = [UInt8:[UInt8]]()
        var index = 0
        while index < rawData.count{
            var len:Int = Int(rawData[index])
            if index + len + 1 <= rawData.count && len > 2 {
                var type = rawData[index+1]
                var itemData = Utils.arraysCopyOfRange(src: rawData, from: index+2, to: index+len+1)
                if type == 0x16 {
                    if itemData[0] == 0xaf && itemData[1] == 0xde {
                        result[0xfe] = itemData
                    }else if itemData[0] == 0xaf && itemData[1] == 0xbe{
                        result[0xfd] = itemData
                    }
                }else{
                    result[type] = itemData
                }
            }
            index += len + 1
        }
        return result
    }
}
