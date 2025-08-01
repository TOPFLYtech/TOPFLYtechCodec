//
//  BleDeviceData.swift
//  TFTElock
//
//  Created by jeech on 2021/6/8.
//  Copyright © 2021 com.tftiot. All rights reserved.
//

import Foundation
import CoreBluetooth
class BleDeviceData{
    static let unclockServiceId = CBUUID(string:"27760001-999C-4D6A-9FC4-C7272BE10900")
    static let unclockWriteUUID = CBUUID(string:"27760003-999C-4D6A-9FC4-C7272BE10900")
    static let unclockNotifyUUID = CBUUID(string:"27760003-999C-4D6A-9FC4-C7272BE10900")
    
    static let readDataServiceId = CBUUID(string:"27760001-999d-4d6a-9fc4-c7272be10900")
    static let readDataWriteUUID = CBUUID(string:"27760002-999d-4d6a-9fc4-c7272be10900")
    static let readDataNotifyUUID = CBUUID(string:"27760003-999d-4d6a-9fc4-c7272be10900")

    static let upgradeDataServiceId = CBUUID(string:"27770001-999c-4d6a-9fc4-c7272be10900")
    static let upgradePackageDataWriteUUID = CBUUID(string:"27770003-999c-4d6a-9fc4-c7272be10900")
    static let upgradeDataWriteNotifyUUID = CBUUID(string:"27770002-999c-4d6a-9fc4-c7272be10900")
    
    static let deviceReadyHead:[UInt8] = [0x20,0x00,0x01]
    static let getLockStatusHead:[UInt8] = [0x20,0x00,0x1D]
    static let getSubLockStatusHead:[UInt8] = [0x20, 0x00, 0x1F]
    static let unlockHead:[UInt8] = [0x60,0x07,0xDA]
    static let lockHead:[UInt8] = [0x60,0x07,0xDB]
    static let activeNetworkHead:[UInt8] = [0x60,0x07,0xDC]
    static let uploadStatusHead:[UInt8] = [0x30,0xA0,0x29]
    
    static let func_id_of_unlock = 2010
    static let func_id_of_lock = 2011
    static let func_id_of_activate_network = 2012
    static let func_id_of_timer = 12046
    static let func_id_of_ip1 = 12031
    static let func_id_of_ip2 = 12032
    static let func_id_of_change_unlock_pwd = 2013
    static let func_id_of_apn_addr = 12035
    static let func_id_of_apn_username = 12036
    static let func_id_of_apn_pwd = 12037
    static let func_id_of_ble_pwd_change = 2016
    
    static let func_id_of_sub_lock_version = 33
    static let func_id_of_sub_lock_boot_version = 34

    static let func_id_of_sub_lock_reboot = 2003
    static let func_id_of_sub_lock_shutdown = 2005
    static let func_id_of_sub_lock_factory_reset = 2006
    static let func_id_of_sub_lock_unlock = 2019
    static let func_id_of_sub_lock_lock = 2020
    static let func_id_of_sub_lock_device_name = 4033
    static let func_id_of_sub_lock_broadcast_interval = 4043
    static let func_id_of_sub_lock_long_range = 4035
    static let func_id_of_sub_lock_ble_transmitted_power = 4036
    static let func_id_of_sub_lock_led = 4037
    static let func_id_of_sub_lock_buzzer = 4038
    static let func_id_of_sub_lock_datetime = 4044
    static let func_id_of_sub_lock_device_id = 4041
    static let func_id_of_open_sub_lock = 2019
    static let func_id_of_sub_lock_alarm_open_set = 4040
    static let func_id_of_sub_lock_temp_alarm_set = 4042
    
    static let func_id_of_reset_default = 2026
    static let func_id_of_clear_his_data = 2025
    static let func_id_of_read_sub_lock_his_data = 1017
    
    static let CFG_RESTORE_NONE = 0
    static let CFG_RESTORE_IP = 1
    static let CFG_RESTORE_APN = 2
    static let CFG_RESTORE_TIMER = 3
    static let CFG_RESTORE_NFCIDLIST = 4
    static let CFG_RESTORE_SUBLOCKLIST = 5
    
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
    public var viewShowReadHisBtn:Bool = false
    public var viewIsExpand = false
    public var isNoSignal = false
    public var deviceId:String!
    public var lastRegDate:Date!
    public var isSupportReadHis = true
    
    public var isSubLock: Bool = false
    public var isCharging: Bool = false
    public var isChargingOverVoltage: Bool = false
    public var isLowPower: Bool = false
    public var isHighTemp: Bool = false
    public var isLowTemp: Bool = false
    public var isOpenLockCover: Bool = false
    public var isOpenBackCover: Bool = false
    public var isGpsPosition: Bool = false
    public var isGpsJamming: Bool = false
    public var solarVoltage: Float?
    public var batteryPercent: Int = 0
    public var temp: Float?
    
    public var lockType:Int!
    public var nfcId:NSString!
    public var macId:NSString!
    public var subLockAlarm:Int!
    
 
    public static let MODEL_OF_SGX120B01 = "SolarGuardX 120 B01"
    public static let MODEL_OF_SGX110 = "SolarGuardX 110"
    public static let MODEL_OF_TC019 = "SolarGuardX 200"
    public static let MODEL_OF_TC015 = "SolarGuardX 100"
    public static func isSubLockDevice(model: String) -> Bool {
        if model == BleDeviceData.MODEL_OF_SGX120B01 {
            return true
        }
        return false
    }
    
    public func isDeviceIdValidAndNoneZero() -> Bool {
        if let deviceId = self.deviceId {
            // 尝试将 deviceId 转换为 Int64（Swift 中的长整型）
            if let deviceIdInt = Int64(deviceId), deviceIdInt != 0 {
                return true
            }
        }
        return false
    }
    public func getAlarm() -> String {
        var resp = ""

        if isCharging {
            resp += NSLocalizedString("charging", comment: "") + ";"
        }
        if isChargingOverVoltage {
            resp += NSLocalizedString("is_charging_over_voltage", comment: "") + ";"
        }
        if isLowPower {
            resp += NSLocalizedString("battery_low", comment: "") + ";"
        }
        if isHighTemp {
            resp += NSLocalizedString("alarm_high_temperature", comment: "") + ";"
        }
        if isLowTemp {
            resp += NSLocalizedString("alarm_low_temperature", comment: "") + ";"
        }
        if isOpenLockCover {
            resp += NSLocalizedString("alarm_lock_storage_open", comment: "") + ";"
        }
        if isOpenBackCover {
            resp += NSLocalizedString("alarm_rear_cover_open", comment: "") + ";"
        }

        // 如果没有警报，返回空字符串
        return resp.isEmpty ? "" : resp
    }
    public static func isParentLockDeviceIdValid(_ deviceId: String?) -> Bool {
        guard let deviceId = deviceId else {
            return false
        }
        
        do {
            let temp = deviceId.uppercased().replacingOccurrences(of: "F", with: "")
            return !temp.isEmpty
        } catch {
            return false
        }
    }
    public func parseSubLockStatus(lockStatus: Int){
        let isCharging = (lockStatus & 0x01) == 0x01
        let isChargingOverVoltage = (lockStatus & 0x02) == 0x02
        let isLowPower = (lockStatus & 0x04) == 0x04
        let isHighTemp = (lockStatus & 0x08) == 0x08
        let isLowTemp = (lockStatus & 0x10) == 0x10
        let isOpenLockCover = (lockStatus & 0x20) == 0x20
        let isOpenBackCover = (lockStatus & 0x40) == 0x40
        let isGpsPosition = (lockStatus & 0x80) == 0x80
        let isGpsJamming = (lockStatus & 0x100) == 0x100
        self.isCharging = isCharging
        self.isChargingOverVoltage = isChargingOverVoltage
        self.isLowPower = isLowPower
        self.isHighTemp = isHighTemp
        self.isLowTemp = isLowTemp
        self.isOpenLockCover = isOpenLockCover
        self.isOpenBackCover = isOpenBackCover
        self.isGpsPosition = isGpsPosition
        self.isGpsJamming = isGpsJamming
    }

    public static func isSupportClearHisAndResetDefault(model: String, version: String) -> Bool {
        if model == BleDeviceData.MODEL_OF_SGX120B01 {
            return false
        }else if model == BleDeviceData.MODEL_OF_TC015{
            return false
        }else if model == BleDeviceData.MODEL_OF_TC019{
            let curVersion = (version ?? "").replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "").replacingOccurrences(of: ".", with: "")
            if curVersion >= "116" {
                return true
            } else {
                return false
            }
        }
        else  {
            let curVersion = (version ?? "").replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "").replacingOccurrences(of: ".", with: "")
            if curVersion >= "106" {
                return true
            } else {
                return false
            }
        }
    }

    
    public static func isSupportConfig(model: String, version: String, deviceId: String?) -> Bool {
        if model == BleDeviceData.MODEL_OF_SGX120B01 {
            return true
        }else if model == BleDeviceData.MODEL_OF_TC015{
            return false
        }
        else if   let deviceId = deviceId, !deviceId.trimmingCharacters(in: .whitespaces).isEmpty {
            let curVersion = (version ?? "").replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "").replacingOccurrences(of: ".", with: "")
            if curVersion >= "105" {
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    
    static func parseModel(protocolByte:UInt8) -> String{
        if protocolByte == 0x62{
            return BleDeviceData.MODEL_OF_TC015
        }else if protocolByte == 0x65 ||  protocolByte == 0x7b || protocolByte == 0x7c{
            return BleDeviceData.MODEL_OF_TC019
        }else if protocolByte == 119 || protocolByte == 120 || protocolByte == 121 || protocolByte == 122{
            return BleDeviceData.MODEL_OF_SGX110
        }
        return ""
    }
    
    static func isSupportReadHis(bleDeviceData:BleDeviceData) -> Bool{
        if bleDeviceData.model == BleDeviceData.MODEL_OF_TC019 || bleDeviceData.model == BleDeviceData.MODEL_OF_SGX110{
            return bleDeviceData.isSupportReadHis
        }else if bleDeviceData.model ==  BleDeviceData.MODEL_OF_SGX120B01{
            let curVersion = bleDeviceData.software.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "").replacingOccurrences(of: ".", with: "") 
            if curVersion >= "1002" {
                return true
            } else {
                return false
            }
        }
        return false
    }
    func getBatteryPercnet() ->String{
        return "\(self.getRealBatteryPercent(voltage: voltage))%"
    }
    
    private func getRealBatteryPercent(voltage: Float) -> Int {
        if voltage >= 4.1 {
            return 100
        } else if voltage >= 3.8 {
            return Int(60 + (voltage * 1000 - 3800) * 40 / (4100 - 3800))
        } else if voltage >= 3.45 {
            return Int(10 + (voltage * 1000 - 3450) * 50 / (3800 - 3450))
        } else if voltage >= 3.2 {
            return Int(1 + (voltage * 1000 - 3200) * 9 / (3450 - 3200))
        } else {
            return 1
        }
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
    
    
    // 定义常量
    private static let PREFS_NAME = "BleSubLockInfo"

    // 保存子锁绑定映射
    static func saveSubLockBindMap( imei: String, subLockList: [String]) {
        let key = "sub_locks_\(imei)"
        let prefs = UserDefaults.standard
        var parentLock = prefs.string(forKey: "parent_locks") ?? ""
        if parentLock.isEmpty {
            parentLock = imei
        } else {
            if !parentLock.contains(imei) {
                parentLock = "\(parentLock),\(imei)"
            }
        }
        let joined = subLockList.joined(separator: ",")
        prefs.set(joined, forKey: key)
        prefs.set(parentLock, forKey: "parent_locks")
        prefs.synchronize()
    }

    // 获取子锁绑定映射
    static func getSubLockBindMap(imei: String) -> [String] {
        let key = "sub_locks_\(imei)"
        let prefs = UserDefaults.standard
        let joined = prefs.string(forKey: key) ?? ""
        if joined.isEmpty {
            return []
        }
        return joined.components(separatedBy: ",")
    }
    static func getHadSubLockImeis() -> [String] {
         let prefs = UserDefaults.standard
         let joined = prefs.string(forKey: "parent_locks") ?? ""
         if joined.isEmpty {
             return []
         }
         let set = Set(joined.components(separatedBy: ","))
         return Array(set)
     }
}
