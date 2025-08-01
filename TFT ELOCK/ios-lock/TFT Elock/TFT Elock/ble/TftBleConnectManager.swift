//
//  TftBleConnectManager.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/1/23.
//  Copyright © 2025 com.tftiot. All rights reserved.
//

import Foundation
import CoreBluetooth
protocol BleStatusCallback {
    func onNotifyValue(_ value: [UInt8])
    func onBleStatusCallback(_ connectStatus: Int)
    func onUpgradeNotifyValue(_ value: [UInt8])
}

class TftBleConnectManager: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    
    static let BLE_STATUS_OF_CLOSE = -1
    static let BLE_STATUS_OF_DISCONNECT = 0
    static let BLE_STATUS_OF_CONNECTING = 1
    static let BLE_STATUS_OF_CONNECT_SUCC = 2
    static let BLE_STATUS_OF_SCANNING = 3
    var canSendMsgLock = NSCondition()
    private static var instance: TftBleConnectManager?
    private let lockQueue = DispatchQueue(label: "com.example.lockQueue")
    private var notifyUnclockCharacteristic: CBCharacteristic?
    private var writeUnclockCharacteristic: CBCharacteristic?
    private var notifyUpgradeWriteCharacteristic: CBCharacteristic?
    private var writeUpgradePackageCharacteristic: CBCharacteristic?
    private var centralManager: CBCentralManager!
    private var characteristic: CBCharacteristic?
    var sendMsgQueue:MsgQueue = MsgQueue()
    var sendMsgMultiQueue:MsgQueue = MsgQueue()
    private var isCanSendMsg = true
    private var isDeviceReady = false
    private var isNeedCheckDeviceReady = true
    private var enterUpgrade = false
    private var onThisView = false
    private var connectSucc = false
    private var isNeedGetLockStatus = true
    private var macAddress: String?
    private var needConnect = false
    private var foundDevice = false
    private var isSubLock = false

    var sendMsgThread: Thread?
    private var bleNotifyCallbackMap = [String: BleStatusCallback]()
    private var bleUpgradeNotifyCallbackMap = [String: BleStatusCallback]()
    var cbPeripheral:CBPeripheral!
    private var selfPeripheral:CBPeripheral!
    var lastCheckStatusDate:Date = Date()
    let getStatusTimeout = 4
    private override init() {}
    
    func isOnThsView() -> Bool{
        return onThisView
    }
    
    func isEnterUpgrade() -> Bool{
        return enterUpgrade
    }
    
    func setEnterUpgrade(enterUpgrade:Bool){
        self.enterUpgrade = enterUpgrade
    }
    func needCheckDeviceReady() -> Bool{
        return isNeedCheckDeviceReady
    }
    
    func setIsNeedCheckDeviceReady(isNeedCheckDeviceReady:Bool){
        self.isNeedCheckDeviceReady = isNeedCheckDeviceReady
    }
    func needGetLockStatus() -> Bool{
        return isNeedGetLockStatus
    }
    
    func setIsNeedGetLockStatus(isNeedGetLockStatus:Bool){
        self.isNeedGetLockStatus = isNeedGetLockStatus
    }
    
    class func getInstance() -> TftBleConnectManager {
        if instance == nil {
            instance = TftBleConnectManager()
            
        }
        return instance!
    }
    
    func initManager() {
        centralManager = CBCentralManager(delegate: self, queue: nil)
        self.sendMsgThread = Thread(target: self, selector: #selector(sendMsgThreadFunc), object: nil)
        self.sendMsgThread?.start()
    }
    
    func setDeviceReady(ready:Bool){
        isDeviceReady = ready
    }
    
    func getDeviceReady() -> Bool{
        return isDeviceReady
    }
    func isConnectSucc() -> Bool{
        return connectSucc
    }
    
    func setOnThisView(_ onThisView: Bool) {
        self.onThisView = onThisView
    }
    
    
    @objc func sendMsgThreadFunc(){
        print("send msg thread start")
        while onThisView{
            if (sendMsgQueue.count == 0 && sendMsgMultiQueue.count == 0) || connectSucc == false || isDeviceReady == false{
                if !isDeviceReady{
                    Thread.sleep(forTimeInterval: 1)
                    if(isNeedCheckDeviceReady || isNeedGetLockStatus){
                        canSendMsgLock.lock()
                        isCanSendMsg = false
                        canSendMsgLock.unlock()
                    }
                    sendCheckDeviceReadyCmd();
                }else{
                    var now = Date()
                    if (Int(now.timeIntervalSince1970) - Int(self.lastCheckStatusDate.timeIntervalSince1970)) > self.getStatusTimeout {
                        self.getLockStatus()
                    }
                    Thread.sleep(forTimeInterval: 0.5)
                }
                continue
            }
            if isCanSendMsg {
                if sendMsgQueue.count != 0 {
                    canSendMsgLock.lock()
                    if isCanSendMsg {
                        isCanSendMsg = false
                        var needSendBytes = sendMsgQueue.pop()
                        if needSendBytes != nil {
                            writeContent(content: needSendBytes!)
                        }
                        canSendMsgLock.unlock()
                    }
                    DispatchQueue.main.async {
                        
                    }
                }else{
                    while sendMsgMultiQueue.count != 0{
                        do{
                            var needSendBytes = sendMsgMultiQueue.pop()
                            if needSendBytes != nil {
                                writeContent(content: needSendBytes!)
                            }
                        }catch{
                            
                        }
                       
                    }
                }
            }
        }
    }
    func connect(connectPeripheral:CBPeripheral,isSubLock:Bool) {
        
        if connectPeripheral == nil{
            return
        }
        
        if macAddress != nil && connectPeripheral.identifier.uuidString != macAddress {
            disconnect()
        }
        enterUpgrade = false
        self.needConnect = true
        self.isSubLock = isSubLock
        cbPeripheral = connectPeripheral
        selfPeripheral = nil
        macAddress = connectPeripheral.identifier.uuidString
        
        centralManager.connect(cbPeripheral, options: nil)
        cbPeripheral.delegate = self
    }
    func disconnect() {
        if selfPeripheral != nil {
            centralManager.cancelPeripheralConnection(selfPeripheral)
        }
        self.needConnect = false
        connectSucc = false
        enterUpgrade = false
        isDeviceReady = false
    }
    
    //notify succ
    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            print("订阅失败: \(error)")
            notifyBleStatus(TftBleConnectManager.BLE_STATUS_OF_DISCONNECT)
            self.connectSucc = false
            self.isCanSendMsg = false
            return
        }
        if characteristic.isNotifying {
            print("订阅成功")
            notifyBleStatus(TftBleConnectManager.BLE_STATUS_OF_CONNECT_SUCC)
            self.connectSucc = true
            self.isCanSendMsg = true
            if !isDeviceReady {
                sendCheckDeviceReadyCmd()
            }
            
        } else {
            notifyBleStatus(TftBleConnectManager.BLE_STATUS_OF_DISCONNECT)
            print("取消订阅")
        }
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("连接失败")
        print("订阅失败: \(error)")
        notifyBleStatus(TftBleConnectManager.BLE_STATUS_OF_DISCONNECT)
        self.connectSucc = false
        self.isCanSendMsg = false
    }
    
    /** 断开连接 */
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("断开连接")
        print("订阅失败: \(error)")
        notifyBleStatus(TftBleConnectManager.BLE_STATUS_OF_DISCONNECT)
        self.connectSucc = false
        self.isCanSendMsg = false
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .unknown:
            print("central.state is .unknown")
        case .resetting:
            print("central.state is .resetting")
        case .unsupported:
            print("central.state is .unsupported")
        case .unauthorized:
            print("central.state is .unauthorized")
        case .poweredOff:
            print("central.state is .poweredOff")
        case .poweredOn:
            print("central.state is .poweredOn connect")
            self.centralManager.scanForPeripherals(withServices: nil,options: [CBCentralManagerScanOptionAllowDuplicatesKey : true])
        }
    }
    
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("edit Connected")
        peripheral.delegate = self
        self.centralManager.stopScan()
        
        if(isSubLock){
//            peripheral.discoverServices([BleDeviceData.upgradeDataServiceId,BleDeviceData.upgradeDataWriteNotifyUUID,BleDeviceData.upgradePackageDataWriteUUID])
            peripheral.discoverServices([BleDeviceData.upgradeDataServiceId,BleDeviceData.unclockServiceId,BleDeviceData.unclockServiceId])
        }else{
            peripheral.discoverServices([BleDeviceData.unclockServiceId,BleDeviceData.unclockNotifyUUID,BleDeviceData.unclockWriteUUID])
        }
    }
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        if self.cbPeripheral != nil && peripheral.identifier == self.cbPeripheral.identifier{
            self.selfPeripheral = peripheral
            self.foundDevice = true
            self.selfPeripheral.delegate = self
            if self.needConnect{
                print("connect to device...")
                self.centralManager.connect(self.selfPeripheral)
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverIncludedServicesFor service: CBService, error: Error?) {
        print("didDiscoverIncludedServicesFor")
        //        print(peripheral.services)
        for service: CBService in peripheral.services! {
            //            print("didDiscoverIncludedServicesFor 外设中的服务有：\(service)")
            if service.uuid == BleDeviceData.unclockServiceId {
                let myServie = service
                print("find unclock service id")
                peripheral.discoverCharacteristics(nil, for: myServie)
                 
            }
            
            if service.uuid == BleDeviceData.upgradeDataServiceId && isSubLock {
                let myServie = service
                print("find upgrade service id")
                peripheral.discoverCharacteristics(nil, for: myServie)
                 
            }
        }
    }
    
    var tryFindService = 100
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        print("edit didDiscoverServices")
//        print(peripheral.services)
//        print(cbPeripheral.services)
//        print(self.selfPeripheral.services)
        if peripheral.services!.count == 0 && tryFindService != 0{
            tryFindService -= 1
            print("retry to find service \(tryFindService)")
            
            DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(1)) {
                peripheral.discoverServices([BleDeviceData.unclockServiceId,BleDeviceData.unclockNotifyUUID,BleDeviceData.unclockWriteUUID])
                if self.isSubLock{
                    peripheral.discoverServices([BleDeviceData.upgradeDataServiceId,BleDeviceData.upgradeDataWriteNotifyUUID,BleDeviceData.upgradePackageDataWriteUUID])
                }
            }
        }
        for service: CBService in peripheral.services! {
            print("didDiscoverServices 外设中的服务有：\(service)")
            if service.uuid == BleDeviceData.unclockServiceId {
                let myServie = service
                print("find service id")
                peripheral.discoverCharacteristics(nil, for: myServie)
                peripheral.discoverIncludedServices([BleDeviceData.unclockServiceId,BleDeviceData.unclockNotifyUUID,BleDeviceData.unclockWriteUUID],for:myServie)
               
            }
            if service.uuid == BleDeviceData.upgradeDataServiceId  && isSubLock {
                let myServie = service
                print("find service id")
                peripheral.discoverCharacteristics(nil, for: myServie)
                peripheral.discoverIncludedServices([BleDeviceData.upgradeDataServiceId,BleDeviceData.upgradeDataWriteNotifyUUID,BleDeviceData.upgradePackageDataWriteUUID],for:myServie)
               
            }
        }
    }
    
    /** 发现特征 */
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        print("didDiscoverCharacteristicsFor")
        //        print(service.characteristics)
        for c: CBCharacteristic in service.characteristics!{
            if c.uuid == BleDeviceData.unclockNotifyUUID{
                print("find notify c")
                self.notifyUnclockCharacteristic = c
                peripheral.setNotifyValue(true, for: self.notifyUnclockCharacteristic!)
            }
            if c.uuid == BleDeviceData.unclockWriteUUID{
                print("find write c")
                self.writeUnclockCharacteristic = c
            }
            if c.uuid == BleDeviceData.upgradeDataWriteNotifyUUID{
                print("find notify c")
                self.notifyUpgradeWriteCharacteristic = c
                peripheral.setNotifyValue(true, for: self.notifyUpgradeWriteCharacteristic!)
            }
            if c.uuid == BleDeviceData.upgradePackageDataWriteUUID{
                print("find write c")
                self.writeUpgradePackageCharacteristic = c
            }
        }
    }
    
    //notify return data
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        let data = characteristic.value
        canSendMsgLock.lock()
        isCanSendMsg = true
        canSendMsgLock.unlock()
        isCanSendMsg = true
        lastCheckStatusDate = Date()
        let bytes = [UInt8](data!)
        if data != nil{
            if characteristic.uuid == self.notifyUnclockCharacteristic?.uuid{
                print("writeContent,resp:\(bytes)")
                notifyCallback(bytes)
            }else if characteristic.uuid == self.notifyUpgradeWriteCharacteristic?.uuid{
                let bytes = [UInt8](data!)
                print("writeUpgradeContent,resp:\(bytes)")
                notifyUpgradeCallback(bytes)
            }
        }
       
    }
    
    
    
    
    public func sendCheckDeviceReadyCmd() {
        if(!isNeedCheckDeviceReady){
            return;
        }
        let deviceReadyHead: [UInt8] = BleDeviceData.deviceReadyHead
        writeContent(content:deviceReadyHead )
    }
    
    public func getLockStatus() {
        if(enterUpgrade || !isNeedGetLockStatus){
            return
        }
        var getLockStatusHead: [UInt8] = BleDeviceData.getLockStatusHead
        if(isSubLock){
            getLockStatusHead = BleDeviceData.getSubLockStatusHead
        }

        writeContent(content:getLockStatusHead)
    }
    
    public func writeArrayContent(writeContentList: [[UInt8]]) {
        if writeContentList.count > 1 {
            var multiQueueItem = [Data]()
            for item in writeContentList {
                sendMsgMultiQueue.push(item)
            }
            
        } else if let singleItem = writeContentList.first {
            sendMsgQueue.push(singleItem)
        }
    }
    
    
    func writeContent(content:[UInt8]){
        if !connectSucc{
            return
        }
        if self.selfPeripheral != nil && content != nil && content.count > 0 {
            let data = Data(bytes:content,count: content.count)
            print("write:\(content)")
            self.selfPeripheral.writeValue(data, for: self.writeUnclockCharacteristic!, type: CBCharacteristicWriteType.withResponse)
        }
    }
    
    func setCallback(activityName: String, callback: BleStatusCallback) {
        bleNotifyCallbackMap[activityName] = callback
    }
    
    func removeCallback(activityName: String) {
        bleNotifyCallbackMap.removeValue(forKey: activityName)
    }
    
    func setUpgradeCallback(activityName: String, callback: BleStatusCallback) {
        bleUpgradeNotifyCallbackMap[activityName] = callback
    }
    
    func removeUpgradeCallback(activityName: String) {
        bleUpgradeNotifyCallbackMap.removeValue(forKey: activityName)
    }
    
    private func notifyCallback(_ value: [UInt8]) {
        for (_, callback) in bleNotifyCallbackMap {
            callback.onNotifyValue(value)
        }
    }
    
    private func notifyUpgradeCallback(_ value: [UInt8]) {
        for (_, callback) in bleUpgradeNotifyCallbackMap {
            callback.onUpgradeNotifyValue(value)
        }
    }
    
    private func notifyBleStatus(_ connectStatus: Int) {
        for (_, callback) in bleNotifyCallbackMap {
            callback.onBleStatusCallback(connectStatus)
        }
    }
    
    func writeUpgradePackageDataArray(content: [UInt8]) {
        if !connectSucc {
            return
        }
        print("upgrade package write:" + Utils.bytes2HexString(bytes: content, pos: 0))
        if let characteristic = self.writeUpgradePackageCharacteristic {
            let data = Data(bytes: content, count: content.count)
            self.selfPeripheral.writeValue(data, for: characteristic, type: .withoutResponse)
        }
    }

    func writeUpgradeCmdDataArray(content: [UInt8]) {
        if !connectSucc {
            return
        }
        print("upgrade cmd write:" + Utils.bytes2HexString(bytes: content, pos: 0))
        if let characteristic = self.notifyUpgradeWriteCharacteristic {
            let data = Data(bytes: content, count: content.count)
            self.selfPeripheral.writeValue(data, for: characteristic, type: .withResponse)
        }
    }
}
