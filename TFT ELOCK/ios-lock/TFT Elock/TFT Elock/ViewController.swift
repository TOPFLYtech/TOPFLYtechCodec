//
//  ViewController.swift
//  tftble
//
//  Created by jeech on 2019/12/11.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import UIKit
import CLXToast
import CoreBluetooth
import swiftScan
import SwiftPopMenu

var KSize = UIScreen.main.bounds



extension String {
    
    //1, 截取规定下标之后的字符串
    
    func subStringFrom(index: Int)-> String {
        
        let temporaryString: String = self
        
        let temporaryIndex = temporaryString.index(temporaryString.startIndex, offsetBy: 3)
        
        return String(temporaryString[temporaryIndex...])
        
    }
    
    //2, 截取规定下标之前的字符串
    func subStringTo(index: Int) -> String {
        let temporaryString = self
        let temporaryIndex = temporaryString.index(temporaryString.startIndex, offsetBy: index)
        return String(temporaryString[...temporaryIndex])
        
    }
    
    func subStr(startIndex:Int,endIndex:Int)->String{
        let temporaryString = self
        let indexStart = temporaryString.index(temporaryString.startIndex, offsetBy: startIndex)
        let indexEnd = temporaryString.index(temporaryString.startIndex, offsetBy: endIndex)
        return String(temporaryString[indexStart..<indexEnd])
    }
    func hexStringToInt() -> Int {
        let str = self.uppercased()
        var sum = 0
        for i in str.utf8 {
            sum = sum * 16 + Int(i) - 48 // 0-9 从48开始
            if i >= 65 {                 // A-Z 从65开始，但有初始值10，所以应该是减去55
                sum -= 7
            }
        }
        return sum
    }
}

class ViewController: UIViewController, CBCentralManagerDelegate,CBPeripheralDelegate,UITableViewDataSource,UITableViewDelegate,UISearchBarDelegate
, LBXScanViewControllerDelegate
{
    func scanFinished(scanResult: LBXScanResult, error: String?) {
        print(scanResult)
        self.searchBar.text = scanResult.strScanned as! String
        fuzzyKey = scanResult.strScanned as! String
        self.dataTableFuzzySearch()
    }
    
    
    private var bleStateSucc = false
    private var centralManager   : CBCentralManager!
    private var waitingView:AEUIAlertView!
    private var isOpenFilter = false
    private var searchView:UIView!
    private var searchBar:UISearchBar!
    private var scanBtn:UIButton!
    private var closeSearchBtn:UIButton!
    private var fuzzyKey = ""
    private var bleDeviceInfoArray = [BleDeviceData]()
    private var allBleDeviceInfoArray = [BleDeviceData]()
    private var favoriteImeis = [String]()
    private var isShowFavorite = false
    private var dataTable:UITableView!
    private var uniqueID = ""
    private let favoriteImeisKey = "favoriteImeis"
    private let isShowFavoriteKey = "isShowFavorite"
    
    func initFavoriteImeis(){
        self.favoriteImeis = UserDefaults.standard.array(forKey: self.favoriteImeisKey) as? [String] ?? []
        self.isShowFavorite = UserDefaults.standard.bool(forKey: self.isShowFavoriteKey)
    }
    func saveFavoriteImeis(){
        UserDefaults.standard.set(self.favoriteImeis, forKey: self.favoriteImeisKey)
        UserDefaults.standard.set(self.isShowFavorite, forKey: self.isShowFavoriteKey)
        
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        let view = AEAlertView(style: .defaulted)
        let title = "Warning"
        var message = ""
        let action_one = AEAlertAction(title: "Cancel", style: .cancel) { (action) in
            view.dismiss()
        }
        let action_two = AEAlertAction(title: "Confirm", style: .defaulted) { (action) in
            view.dismiss()
        }
        switch central.state {
        case .unknown:
            print("central.state is .unknown")
            message = "The Bluetooth device is abnormal!"
        case .resetting:
            print("central.state is .resetting")
            message = "The Bluetooth device is abnormal!"
            bleStateSucc = false
        case .unsupported:
            print("central.state is .unsupported")
            message = "The Bluetooth device is abnormal!"
        case .unauthorized:
            print("central.state is .unauthorized")
            message = "The Bluetooth device is abnormal!"
        case .poweredOff:
            print("central.state is .poweredOff")
            message = "Bluetooth is not turned on. Please turn on Bluetooth."
            bleStateSucc = false
        case .poweredOn:
            print("central.state is .poweredOn")
            centralManager.scanForPeripherals(withServices: nil,options: [CBCentralManagerScanOptionAllowDuplicatesKey : true])
            bleStateSucc = true
        }
        if !bleStateSucc{
            view.title = title
            view.message = message
            
            view.addAction(action: action_one)
            view.addAction(action: action_two)
            view.show()
        }
        
    }
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("Connected")
        
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        print("didDiscoverServices")
    }
    func peripheral(_ peripheral: CBPeripheral, didDiscoverIncludedServicesFor service: CBService, error: Error?) {
        print("didDiscoverIncludedServicesFor")
    }
    
    func centralManager(_ central: CBCentralManager, connectionEventDidOccur event: CBConnectionEvent, for peripheral: CBPeripheral) {
        print("connectionEventDidOccur")
        print(event.rawValue)
    }
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("连接失败")
    }
    
    /** 断开连接 */
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("断开连接")
        // 重新连接
        central.connect(peripheral, options: nil)
        
    }
    
    
    func showWaitingWin(title:String){
        if self.waitingView != nil && !self.waitingView.isDismiss{
            self.waitingView.show()
            return
        }
        self.waitingView = AEUIAlertView(style: .textField, title: title, message: nil)
        self.waitingView.actions = []
        self.waitingView.resetActions()
        
        let animation = UIView(frame: CGRect(x: 0, y: 0, width: 80, height: 80))
        let anim = AEBeginLineAnimation.initShow(in: animation.bounds, lineWidth: 4, lineColor: UIColor.blue)
        animation.addSubview(anim)
        
        self.waitingView.textField.isHidden = true
        self.waitingView.set(animation: animation, width: 80, height: 80)
        self.waitingView.show()
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(30)) {
            if !self.waitingView.isDismiss{
                Toast.hudBuilder.title("Timeout, please try again!").show()
            }
            self.waitingView.dismiss()
        }
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        var rssi = -9999
        if RSSI != nil{
            rssi = Int(RSSI)
        }
        let mac = peripheral.identifier.uuidString
        //        print(mac)
        if advertisementData["kCBAdvDataServiceData"]  != nil {
            let dict = advertisementData["kCBAdvDataServiceData"] as! NSDictionary
            let key1 = CBUUID(string:"BEAF")
            let key2: CBUUID = CBUUID(string:"DEAF")
            let deviceIdKey = CBUUID(string:"AEAF")
            let subLockInfoKey: CBUUID = CBUUID(string:"FEAA")
            let subLockIdKey: CBUUID = CBUUID(string:"FEAB")
            if dict[key1] != nil && dict[key2] != nil && advertisementData["kCBAdvDataLocalName"] != nil{
                let name = advertisementData["kCBAdvDataLocalName"] as! String
                var exist = false
                for item in self.discoveredPeripherals{
                    if item.mac == mac{
                        exist = true
                        break
                    }
                }
                if !exist {
                    self.discoveredPeripherals.append((peripheral, mac))
                }
                var imeiData:[UInt8]? = nil,versionData:[UInt8]? = nil,deviceIdData:[UInt8]? = nil
                if dict[key1] != nil{
                    let data = dict[key1] as! Data
                    imeiData = [UInt8](data)
                }
                if dict[key2] != nil{
                    let data = dict[key2] as! Data
                    versionData = [UInt8](data)
                }
                if dict[deviceIdKey] != nil{
                    let data = dict[deviceIdKey] as! Data
                    deviceIdData = [UInt8](data)
                }
                self.parseBleData(deviceName: name, imeiData: imeiData!,versionData: versionData!,deviceIdData: deviceIdData, rssi: rssi, mac: mac)
                
            }else  if dict[subLockInfoKey] != nil && dict[subLockIdKey] != nil && advertisementData["kCBAdvDataLocalName"] != nil{
                let name = advertisementData["kCBAdvDataLocalName"] as! String
                var exist = false
                for item in self.discoveredPeripherals{
                    if item.mac == mac{
                        exist = true
                        break
                    }
                }
                if !exist {
                    self.discoveredPeripherals.append((peripheral, mac))
                }
                var imeiData:[UInt8]? = nil,subLockInfoData:[UInt8]? = nil,subLockIdData:[UInt8]? = nil
                if dict[key1] != nil{
                    let data = dict[key1] as! Data
                    imeiData = [UInt8](data)
                }
                if dict[subLockInfoKey] != nil{
                    let data = dict[subLockInfoKey] as! Data
                    subLockInfoData = [UInt8](data)
                }
                if dict[subLockIdKey] != nil{
                    let data = dict[subLockIdKey] as! Data
                    subLockIdData = [UInt8](data)
                }
                self.parseSubLockBleData(deviceName: name, versionData: subLockInfoData!,imeiData: subLockIdData!, rssi: rssi, mac: mac)
            }else if dict[key2] != nil{
                let data = dict[key2] as! Data
                var versionData = [UInt8](data)
                self.updateVersionData(mac: mac, versionData: versionData, rssi: rssi)
            }
        }else{
            //update rssi
            
            var i = 0
            
            while i < allBleDeviceInfoArray.count{
                let bleDeviceInfo = allBleDeviceInfoArray[i]
                let macStr = bleDeviceInfo.mac
                if mac == macStr{
                    bleDeviceInfo.rssi = String.init(format: "%d", rssi)
                    bleDeviceInfo.date = currentDateString()
                    bleDeviceInfo.lastRegDate = Date()
                    break
                }
                i+=1
            }
            
            i = 0
            while i < bleDeviceInfoArray.count{
                let bleDeviceInfo = bleDeviceInfoArray[i]
                let macStr = bleDeviceInfo.mac
                if mac == macStr{
                    bleDeviceInfo.rssi = String.init(format: "%d", rssi)
                    bleDeviceInfo.date = currentDateString()
                    bleDeviceInfo.lastRegDate = Date()
                    break
                }
                i+=1
            }
            if lastRefreshDate == nil{
                dataTable.reloadData()
                lastRefreshDate = Date()
            }
            let now = Date()
            if now.timeIntervalSince1970 - lastRefreshDate.timeIntervalSince1970 > 1{
                lastRefreshDate = Date()
                dataTable.reloadData()
            }
        }
    }
    func updateVersionData(mac:String,versionData:[UInt8],rssi:Int){
        var i = 0
        var software:String = "",hardware:String = "",_:String = "",model:String = ""
        var voltage:Float = 0.0
        if versionData != nil && versionData.count >= 5{
            let protocolByte = versionData[0]
            let versionByte = Utils.arraysCopyOfRange(src: versionData, from: 1, to: versionData.count)
            let versionStr = Utils.uint8ArrayToHexStr(value: versionByte)
            let hardwarePart1 = versionStr.subStr(startIndex: 0, endIndex: 1)
            let hardwarePart2 = versionStr.subStr(startIndex: 1, endIndex: 2)
            hardware = String.init(format: "V%@.%@", hardwarePart1,hardwarePart2)
            let softwareStr = versionStr.subStr(startIndex: 2, endIndex: 6)
            let softwareInt = Int(softwareStr) ?? 0
            software = String.init(format: "V%d", softwareInt)
            model = BleDeviceData.parseModel(protocolByte: protocolByte)
            let voltage1 = versionStr.subStr(startIndex: 6, endIndex: 7)
            let voltage2 = versionStr.subStr(startIndex: 7, endIndex: 8)
            let voltageStr = String.init(format: "%@.%@", voltage1,voltage2)
            voltage = Float(voltageStr)!
        }
        while i < allBleDeviceInfoArray.count{
            let bleDeviceInfo = allBleDeviceInfoArray[i]
            let macStr = bleDeviceInfo.mac
            if mac == macStr{
                bleDeviceInfo.rssi = String.init(format: "%d", rssi)
                bleDeviceInfo.date = currentDateString()
                bleDeviceInfo.hardware = hardware
                bleDeviceInfo.software = software
                bleDeviceInfo.model = model
                bleDeviceInfo.voltage = voltage
                break
            }
            i+=1
        }
        
        i = 0
        while i < bleDeviceInfoArray.count{
            let bleDeviceInfo = bleDeviceInfoArray[i]
            let macStr = bleDeviceInfo.mac
            if mac == macStr{
                bleDeviceInfo.rssi = String.init(format: "%d", rssi)
                bleDeviceInfo.date = currentDateString()
                bleDeviceInfo.hardware = hardware
                bleDeviceInfo.software = software
                bleDeviceInfo.model = model
                bleDeviceInfo.voltage = voltage
                break
            }
            i+=1
        }
        if lastRefreshDate == nil{
            dataTable.reloadData()
            lastRefreshDate = Date()
        }
        let now = Date()
        if now.timeIntervalSince1970 - lastRefreshDate.timeIntervalSince1970 > 1{
            lastRefreshDate = Date()
            dataTable.reloadData()
        }
    }
    var lastRefreshDate:Date = Date()
    func parseSubLockBleData(deviceName:String,versionData:[UInt8],imeiData:[UInt8],rssi:Int,mac:String){
        do{
            if imeiData != nil || versionData != nil {
                let versionInfo = versionData
                let protocolByte = versionInfo[0]
                if protocolByte != 0x0b {
                    return;
                }
                var imei:String = ""
                var deviceId:String = ""
                imei = Utils.bytes2HexString(bytes: imeiData, pos: 0)
                var protocolType:String = "",model:String =  BleDeviceData.MODEL_OF_SGX120B01
                let hardware = Utils.parseSGX120HardwareVersion(hardware: String(format: "%02x", versionInfo[1]))
                let software = Utils.parseSGX120SoftwaeVersion(data: [UInt8](versionInfo), index: 2)
                let lockType = versionInfo[5]
                let lockStatus = Utils.bytes2Short(bytes: versionInfo, offset: 6)
                
                let voltageTemp = Utils.bytes2Short(bytes: versionInfo, offset: 8)
                let voltage = Float(voltageTemp) / 1000
                let batteryPercent = versionInfo[10]
                let solarVoltageTemp = Float(Utils.bytes2Short(bytes: versionInfo, offset: 11)) / 1000
                if(versionInfo.count < 14){
                    return;
                }
                var temp = Int(versionInfo[13] & 0xff)
                if temp == 0xff {
                    temp = -999
                }else {
                    temp = temp - 80
                }
                deviceId = Utils.bytes2HexString(bytes: versionInfo, pos: 14)
                var bleDeviceData = BleDeviceData()
                bleDeviceData.hardware = hardware
                bleDeviceData.date = currentDateString()
                bleDeviceData.imei = imei.uppercased()
                bleDeviceData.deviceId = deviceId
                bleDeviceData.software = software
                bleDeviceData.model = model
                bleDeviceData.mac = mac
                bleDeviceData.id = imei.uppercased()
                bleDeviceData.voltage = voltage
                bleDeviceData.deviceName = deviceName
                bleDeviceData.isSupportReadHis = false
                bleDeviceData.lastRegDate = Date()
                bleDeviceData.rssi = String.init(format: "%d", rssi)
                
                bleDeviceData.isSubLock = true
                bleDeviceData.parseSubLockStatus(lockStatus:lockStatus)
                bleDeviceData.solarVoltage = solarVoltageTemp
                bleDeviceData.temp = Float(temp)
                bleDeviceData.viewShowReadHisBtn = BleDeviceData.isSupportReadHis(bleDeviceData: bleDeviceData)
                
                var exist = false
                var i = 0
                while i < allBleDeviceInfoArray.count{
                    let bleDeviceInfo = allBleDeviceInfoArray[i]
                    let macStr = bleDeviceInfo.mac
                    if mac == macStr{
                        bleDeviceData.viewIsExpand = bleDeviceInfo.viewIsExpand
                        allBleDeviceInfoArray[i] = bleDeviceData
                        exist = true
                        break
                    }
                    i+=1
                }
                if !exist
                {
                    allBleDeviceInfoArray.append(bleDeviceData)
                }
                exist = false
                i = 0
                while i < bleDeviceInfoArray.count{
                    let bleDeviceInfo = bleDeviceInfoArray[i]
                    let macStr = bleDeviceInfo.mac
                    if mac == macStr{
                        bleDeviceData.viewIsExpand = bleDeviceInfo.viewIsExpand
                        bleDeviceInfoArray[i] = bleDeviceData
                        exist = true
                        break
                    }
                    i+=1
                }
                if !exist
                {
                    self.updateBleItem(bleDeviceData: bleDeviceData)
                }
                if lastRefreshDate == nil{
                    dataTable.reloadData()
                    lastRefreshDate = Date()
                }
                let now = Date()
                if now.timeIntervalSince1970 - lastRefreshDate.timeIntervalSince1970 > 1{
                    lastRefreshDate = Date()
                    dataTable.reloadData()
                }
            }
        }catch{
            
        }
        
    }
    
    func parseBleData(deviceName:String,imeiData:[UInt8],versionData:[UInt8],deviceIdData:[UInt8]?,rssi:Int,mac:String){
        do{
            if imeiData != nil || versionData != nil {
                let versionInfo = versionData
                let imeiInfo = imeiData
                var imei:String = ""
                var deviceId:String = ""
                if imeiInfo != nil{
                    var imeiByte = Utils.arraysCopyOfRange(src: imeiInfo, from: 1, to: imeiInfo.count)
                    imei = String(bytes:imeiByte,encoding:.utf8)!
                }
                if deviceIdData != nil && deviceIdData!.count >= 5{
                    var deviceIdByte = Utils.arraysCopyOfRange(src: deviceIdData!, from: 0, to: deviceIdData!.count)
                    deviceId = Utils.bytes2HexString(bytes: deviceIdByte, pos: 0)
                }
                var software:String = "",hardware:String = "",protocolType:String = "",model:String = ""
                var protocolByte:UInt8 = 0x00
                var voltage:Float = 0.0
                var isSupportReadHis = true
                if versionInfo != nil && versionInfo.count >= 5{
                    protocolByte = versionInfo[0]
                }
                if protocolByte != 0x62 && protocolByte != 0x65
                    &&  protocolByte != 0x7b && protocolByte != 0x7c
                    &&  protocolByte != 119 && protocolByte != 120
                    &&  protocolByte != 121 && protocolByte != 122{
                    return;
                }
                if versionInfo != nil && versionInfo.count >= 5{
                    let versionByte = Utils.arraysCopyOfRange(src: versionInfo, from: 1, to: versionInfo.count)
                    var versionStr = Utils.uint8ArrayToHexStr(value: versionByte)
                    var hardwarePart1 = versionStr.subStr(startIndex: 0, endIndex: 1)
                    var hardwarePart2 = versionStr.subStr(startIndex: 1, endIndex: 2)
                    hardware = String.init(format: "V%@.%@", hardwarePart1,hardwarePart2)
                    let softwareStr = versionStr.subStr(startIndex: 2, endIndex: 6)
                    let softwareInt = Int(softwareStr)!
                    software = String.init(format: "V%d", softwareInt)
                    model = BleDeviceData.parseModel(protocolByte: protocolByte)
                    if versionInfo.count == 7{
                        let voltageStr = String.init(format: "%d.%d", versionInfo[4],versionInfo[5]);
                        voltage = (Float(voltageStr) ?? 0) / 10
                        isSupportReadHis = versionInfo[6] == 1
                    }else{
                        if versionInfo.count == 5{
                            var voltage1 = versionStr.subStr(startIndex: 6, endIndex: 7)
                            var voltage2 = versionStr.subStr(startIndex: 7, endIndex: 8)
                            let voltageStr = String.init(format: "%@.%@", voltage1,voltage2)
                            voltage = Float(voltageStr) ?? 0
                        }else{
                            let voltageStr = String.init(format: "%d.%d", versionInfo[4],versionInfo[5]);
                            voltage = (Float(voltageStr) ?? 0) / 10
                            
                        }
                    }
                    
                    
                }
                var bleDeviceData = BleDeviceData()
                bleDeviceData.hardware = hardware
                bleDeviceData.date = currentDateString()
                bleDeviceData.imei = imei
                bleDeviceData.deviceId = deviceId
                bleDeviceData.software = software
                bleDeviceData.model = model
                bleDeviceData.mac = mac
                bleDeviceData.voltage = voltage
                bleDeviceData.deviceName = deviceName
                bleDeviceData.isSupportReadHis = isSupportReadHis
                bleDeviceData.lastRegDate = Date()
                bleDeviceData.rssi = String.init(format: "%d", rssi)
                bleDeviceData.viewShowReadHisBtn = BleDeviceData.isSupportReadHis(bleDeviceData: bleDeviceData)
                
                var exist = false
                var i = 0
                while i < allBleDeviceInfoArray.count{
                    let bleDeviceInfo = allBleDeviceInfoArray[i]
                    let macStr = bleDeviceInfo.mac
                    if mac == macStr{
                        bleDeviceData.viewIsExpand = bleDeviceInfo.viewIsExpand
                        allBleDeviceInfoArray[i] = bleDeviceData
                        exist = true
                        break
                    }
                    i+=1
                }
                if !exist
                {
                    allBleDeviceInfoArray.append(bleDeviceData)
                }
                exist = false
                i = 0
                while i < bleDeviceInfoArray.count{
                    let bleDeviceInfo = bleDeviceInfoArray[i]
                    let macStr = bleDeviceInfo.mac
                    if mac == macStr{
                        bleDeviceData.viewIsExpand = bleDeviceInfo.viewIsExpand
                        bleDeviceInfoArray[i] = bleDeviceData
                        exist = true
                        break
                    }
                    i+=1
                }
                if !exist
                {
                    self.updateBleItem(bleDeviceData: bleDeviceData)
                }
                if lastRefreshDate == nil{
                    dataTable.reloadData()
                    lastRefreshDate = Date()
                }
                let now = Date()
                if now.timeIntervalSince1970 - lastRefreshDate.timeIntervalSince1970 > 1{
                    lastRefreshDate = Date()
                    dataTable.reloadData()
                }
                
            }
        }catch{
            
        }
        
    }
    var discoveredPeripherals:[(peripheral: CBPeripheral, mac: String?)] = []
    
    
    @objc private func leftClick() {
        print("leftClick")
        self.bleDeviceInfoArray.removeAll()
        self.allBleDeviceInfoArray.removeAll()
        self.dataTable.reloadData()
        self.showWaitingWin(title:NSLocalizedString("scanning", comment: "Scanning"))
        centralManager.stopScan()
        centralManager.scanForPeripherals(withServices: nil,options: [CBCentralManagerScanOptionAllowDuplicatesKey : true])
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(5)) {
            self.waitingView.dismiss()
        }
    }
    
    func createImageWithColor(color: UIColor) -> UIImage {
        let rect=CGRect(x: 0.0, y: 0.0, width: 1.0, height: 1.0)
        UIGraphicsBeginImageContext(rect.size)
        let context = UIGraphicsGetCurrentContext()
        context!.setFillColor(color.cgColor)
        context!.fill(rect)
        let theImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return theImage!
    }
    @objc private func scanBtnClick(){
        var style = LBXScanViewStyle()
        
        style.centerUpOffset = 60
        style.xScanRetangleOffset = 30
        
        if UIScreen.main.bounds.size.height <= 480 {
            //3.5inch 显示的扫码缩小
            style.centerUpOffset = 40
            style.xScanRetangleOffset = 20
        }
        
        style.color_NotRecoginitonArea = UIColor(red: 0.4, green: 0.4, blue: 0.4, alpha: 0.4)
        
        style.photoframeAngleStyle = LBXScanViewPhotoframeAngleStyle.Inner
        style.photoframeLineW = 2.0
        style.photoframeAngleW = 16
        style.photoframeAngleH = 16
        
        style.isNeedShowRetangle = false
        
        style.anmiationStyle = LBXScanViewAnimationStyle.NetGrid
        
        let vc = LBXScanViewController()
        
        vc.scanStyle = style
        vc.scanResultDelegate = self
        self.navigationController?.pushViewController(vc, animated: true)
        
    }
    
    @objc private func searchClick() {
        //     let historyView = HistoryReportController()
        //        historyView.startDate = Int(Date().timeIntervalSince1970)
        //        historyView.endDate = Int(Date().timeIntervalSince1970)
        //
        //      self.navigationController?.pushViewController(historyView, animated: false)
        //        let smtpView = SmtpSettingController()
        //        self.navigationController?.pushViewController(smtpView, animated: false)
        print("searchClick")
        if self.isOpenFilter {
            print ("hide")
            self.searchView.isHidden = true
            dataTable.frame.origin.y = 0
            dataTable.frame = CGRect(x: 0, y: 0, width: KSize.width, height: KSize.height)
            self.searchBar.text = ""
            self.searchBar.resignFirstResponder()
            fuzzyKey = ""
            self.dataTableFuzzySearch()
        }else{
            print ("show")
            self.searchView.isHidden = false
            dataTable.frame.origin.y = 120
            dataTable.frame = CGRect(x: 0, y: 120, width: KSize.width, height: KSize.height - 120)
        }
        self.isOpenFilter = !self.isOpenFilter
    }
    
    
    
    
    
    override func viewDidAppear(_ animated: Bool) {
        print("viewDidAppear")
        if self.bleStateSucc{
            centralManager.scanForPeripherals(withServices: nil,options: [CBCentralManagerScanOptionAllowDuplicatesKey : true])
        }else{
            
        }
        self.initParentSubRelationMap()
    }
    override func viewDidDisappear(_ animated: Bool) {
        print("viewDidDIsappear")
        //        self.centralManager.stopScan()
    }
    override func viewWillAppear(_ animated: Bool) {
        print("viewWillAppear")
    }
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        print(searchText)
        fuzzyKey = searchText
        self.dataTableFuzzySearch()
    }
    func updateBleItem(bleDeviceData:BleDeviceData){
        let deviceName:String = bleDeviceData.deviceName ?? ""
        let id:String = bleDeviceData.imei ?? ""
        
        if fuzzyKey != ""{
            let deviceRange = deviceName.uppercased().range(of: fuzzyKey.uppercased())
            var deviceNameLocation = -1
            if deviceRange != nil{
                deviceNameLocation = deviceName.distance(from: deviceName.startIndex, to: deviceRange!.lowerBound)
            }
            let idRange = id.uppercased().range(of:fuzzyKey.uppercased())
            var idLocation = -1
            if idRange != nil{
                idLocation = id.distance(from: id.startIndex, to: idRange!.lowerBound)
            }
            if deviceNameLocation != -1 || idLocation != -1{
                bleDeviceInfoArray.append(bleDeviceData)
            }
        }else{
            if isShowFavorite{
                if favoriteImeis.contains(bleDeviceData.imei){
                    bleDeviceInfoArray.append(bleDeviceData)
                }
            }else{
//                bleDeviceInfoArray.append(bleDeviceData)
                self.updateParentSubRelationPosition(bleDeviceData)
            }
        }
    }
    
    private var bigLockSubLockMap: [String: [String]] = [:]
        private var subLockParentLockMap: [String: [String]] = [:]

        private func initParentSubRelationMap() {
            bigLockSubLockMap.removeAll()
            subLockParentLockMap.removeAll()
            let parentList = BleDeviceData.getHadSubLockImeis()
            for parent in parentList {
                let subList = BleDeviceData.getSubLockBindMap(imei: parent)
                if subList != nil {
                    if !subList.isEmpty {
                        bigLockSubLockMap[parent] = subList
                        for sub in subList {
                            if subLockParentLockMap[sub] == nil {
                                subLockParentLockMap[sub] = []
                            }
                            subLockParentLockMap[sub]?.append(parent)
                        }
                    }
                }
            }
        }

        private func updateParentSubRelationPosition(_ bleDeviceData: BleDeviceData) {
            if bleDeviceData.isSubLock {
                let mac = bleDeviceData.imei
                let upperMac = mac!.uppercased()
                let lowerMac = mac!.lowercased()
                var parentList: [String]?
                if let list = subLockParentLockMap[upperMac] {
                    parentList = list
                } else if let list = subLockParentLockMap[lowerMac] {
                    parentList = list
                }

                if let parentList = parentList {
                    if parentList.count > 1 {
                        bleDeviceInfoArray.append(bleDeviceData)
                    } else {
                        let parentImei = parentList[0]
                        var isFindParent = false
                        for (index, item) in bleDeviceInfoArray.enumerated() {
                            if item.imei == parentImei {
                                bleDeviceInfoArray.insert(bleDeviceData, at: index + 1)
                                isFindParent = true
                                break
                            }
                        }
                        if !isFindParent {
                            bleDeviceInfoArray.append(bleDeviceData)
                        }
                    }
                } else {
                    bleDeviceInfoArray.append(bleDeviceData)
                }
            } else {
                if let subList = bigLockSubLockMap[bleDeviceData.imei] {
                    var subLockItems: [BleDeviceData] = []
                    for item in bleDeviceInfoArray {
                        let cleanId = item.imei.uppercased()
                        if subList.contains(cleanId) || subList.contains(cleanId.lowercased()) {
                            subLockItems.append(item)
                        }
                    }
                    if subLockItems.count > 0 {
                        for subLockItem in subLockItems {
                            for (index, item) in bleDeviceInfoArray.enumerated() {
                                if subLockItem.imei == item.imei {
                                    bleDeviceInfoArray.remove(at: index)
                                }
                            }
                        }
                    }
                    bleDeviceInfoArray.append(bleDeviceData)
                    for item in subLockItems {
                        bleDeviceInfoArray.append(item)
                    }
                } else {
                    bleDeviceInfoArray.append(bleDeviceData)
                }
            }
        }
    
    func updateAllItem(){
        var i = 0
        bleDeviceInfoArray.removeAll()
        while i < allBleDeviceInfoArray.count{
            let bleDeviceData = allBleDeviceInfoArray[i]
            self.updateBleItem(bleDeviceData:bleDeviceData);
            i+=1
        }
        dataTable.reloadData()
    }
    func dataTableFuzzySearch(){
        self.updateAllItem()
        if fuzzyKey.count == 15{
            var i=0
            while i < allBleDeviceInfoArray.count{
                let bleDeviceData = allBleDeviceInfoArray[i]
                let imei:String = bleDeviceData.imei ?? ""
                if imei == fuzzyKey{
                    let macStr = bleDeviceData.mac
                    print(macStr)
                    for item in self.discoveredPeripherals{
                        if item.mac == macStr {
                            let peripheral = item.peripheral
                            self.centralManager.stopScan()
                            let editView = UnlockController()
                            editView.cbPeripheral = peripheral
                            editView.name = bleDeviceData.deviceName
                            editView.model = bleDeviceData.model
                            editView.software = bleDeviceData.software
                            editView.deviceId = bleDeviceData.deviceId
                            editView.imei = bleDeviceData.imei
                            self.navigationController?.pushViewController(editView, animated: false)
                            break
                        }
                    }
                    break
                }
                i+=1
            }
        }
    }
    
    func regularCheckTimeoutSignal(){
        var i = 0
        let now = Date()
        var hadTimeout = false
        while i < self.bleDeviceInfoArray.count{
            let bleDeviceInfo = self.bleDeviceInfoArray[i]
            if bleDeviceInfo.lastRegDate != nil{
                let calendar = Calendar.current
                let components = calendar.dateComponents([.second], from: bleDeviceInfo.lastRegDate, to: now )
                if components.second ?? 0 > 300 && components.second ?? 0 < 345{
                    hadTimeout = true
                    break
                }
            }
            i+=1
        }
        if hadTimeout{
            dataTable.reloadData()
        }
    }
    private var clickCount = 0
    @objc private func titleBarTapped() {
        clickCount += 1
        if clickCount >= 10 {
            Utils.isDebug = true
            // 停止点击事件
            navigationController?.navigationBar.gestureRecognizers?.forEach {
                navigationController?.navigationBar.removeGestureRecognizer($0)
            }
        }else if clickCount > 6{
            Toast.hudBuilder.title("再点击\(10 - clickCount)次，打开Debug功能").show()
        }
        
        if clickCount == 1{
            // 在 20 秒后检查点击次数
            DispatchQueue.main.asyncAfter(deadline: .now() + 20) {
                if !Utils.isDebug {
                    // 20 秒内未达到点击次数要求
                    // 进行相应的处理逻辑
                    self.clickCount = 0
                }
            }
        }
    }
    override func viewDidLoad() {
        print("viewDidLoad")
        let delayInSeconds: Double = 3.0
        let timer = Timer.scheduledTimer(withTimeInterval: delayInSeconds, repeats: true) { _ in
            self.regularCheckTimeoutSignal()
        }
        // 将定时器添加到当前运行循环中（通常在App的主运行循环中）
        RunLoop.current.add(timer, forMode:.common)
        
        //        self.uniqueID = UniqueIDTool.getMediaDrmID()
        //        print("uid:\(self.uniqueID)")
        super.viewDidLoad()
        self.initFavoriteImeis()
        self.navigationController!.navigationBar.isTranslucent = false
        self.extendedLayoutIncludesOpaqueBars = true
        self.view.backgroundColor = UIColor.white
        self.view.autoresizingMask = UIView.AutoresizingMask.flexibleWidth
        makeTable()
        dataTable.autoresizingMask = UIView.AutoresizingMask.flexibleWidth
        dataTable.register(BleDetailItem.self,forCellReuseIdentifier:BleDetailItem.identifier)
        dataTable.register(NoReadDataBleDetailItem.self,forCellReuseIdentifier:NoReadDataBleDetailItem.identifier)
        self.initNavBar()
        centralManager = CBCentralManager(delegate: self, queue: nil)
        self.searchBar = UISearchBar()
        self.searchBar.backgroundColor = UIColor.white
        self.searchBar.barTintColor = UIColor.white
        self.searchBar.frame = CGRect(x:0,y:0,width: KSize.width - 80,height:60)
        self.searchBar.delegate = self
        self.searchView = UIView()
        self.searchView.backgroundColor = UIColor.white
        self.searchView.frame = CGRect(x:0,y:60,width: KSize.width,height:60)
        self.searchView.isHidden = true
        self.scanBtn = UIButton()
        self.scanBtn.backgroundColor = UIColor.white
        self.scanBtn.frame = CGRect(x:KSize.width - 80,y:10,width: 40,height:40)
        self.scanBtn.setImage(UIImage (named: "scan.png"), for: UIControl.State.normal)
        self.scanBtn.addTarget(self, action: #selector(scanBtnClick), for: .touchUpInside)
        
        self.closeSearchBtn = UIButton()
        self.closeSearchBtn.backgroundColor = UIColor.white
        self.closeSearchBtn.frame = CGRect(x:KSize.width - 35,y:18,width: 25,height:25)
        self.closeSearchBtn.setImage(UIImage (named: "ic_delete.png"), for: UIControl.State.normal)
        self.closeSearchBtn.addTarget(self, action: #selector(searchClick), for: .touchUpInside)
        
        self.searchView.addSubview(self.closeSearchBtn)
        self.searchView.addSubview(self.scanBtn)
        self.searchView.addSubview(self.searchBar)
        self.view.addSubview(self.searchView)
        // 设置标题栏点击事件监听器
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(titleBarTapped))
        navigationController?.navigationBar.addGestureRecognizer(tapGesture)
        let tap = UITapGestureRecognizer(target: self, action: #selector(dismissKeyboard))
        self.view.addGestureRecognizer(tap)
        
        
    }
    var barLabel:UILabel!
    var refreshBtn:UIButton!
    var favoriteBtn:UIButton!
    var rightMenuBtn:UIButton!
    let imageSize = CGSize(width: 24, height: 24)
    let renderer = UIGraphicsImageRenderer(size: CGSize(width: 24, height: 24))
    func initNavBar(){
        initPopMenu()
        barLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        //                titleLabel.text = "Bluetooth sensor"
        barLabel.text =  NSLocalizedString("main_bar_text", comment: "Bluetooth device")
        barLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.navigationItem.titleView = barLabel
        
        
        
        let rightMenuImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: "ic_list.png")
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
        var picName = isShowFavorite ? "ic_show_favorite.png" : "ic_hide_favorite.png"
        let favoriteImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: picName)
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
        let refreshImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: "ic_refresh.png")
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
        favoriteBtn = UIButton(type: .custom) as! UIButton
        favoriteBtn.setImage(favoriteImage, for: .normal)
        favoriteBtn.addTarget(self, action: #selector(switchFavoriteClick), for:.touchUpInside)
        favoriteBtn.frame = CGRectMake(0, 0, 30, 30)
        let infoBarBtn = UIBarButtonItem(customView: favoriteBtn)
        
        rightMenuBtn = UIButton(type: .custom) as! UIButton
        rightMenuBtn.setImage(rightMenuImage, for:.normal)
        rightMenuBtn.addTarget(self, action: #selector(showPopMenuClick), for:.touchUpInside)
        rightMenuBtn.frame = CGRectMake(0, 0, 30, 30)
        let searchBarBtn = UIBarButtonItem(customView: rightMenuBtn)
        
        refreshBtn = UIButton(type: .custom) as! UIButton
        refreshBtn.setImage(refreshImage, for: .normal)
        refreshBtn.addTarget(self, action: #selector(leftClick), for: .touchUpInside)
        refreshBtn.frame = CGRectMake(0, 0, 30, 30)
        let refreshBarBtn = UIBarButtonItem(customView: refreshBtn)
        self.navigationItem.setLeftBarButtonItems([refreshBarBtn ], animated: false)
        self.navigationItem.setRightBarButtonItems([  searchBarBtn, infoBarBtn], animated: false)
        
        navigationController?.navigationBar.barTintColor = UIColor.colorPrimary
        
        
    }
    @objc private func switchFavoriteClick() {
        print("switchFavoriteClick")
        isShowFavorite = !isShowFavorite;
        saveFavoriteImeis();
        var picName = isShowFavorite ? "ic_show_favorite.png" : "ic_hide_favorite.png"
        let favoriteImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: picName)
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
        favoriteBtn.setImage(favoriteImage, for: .normal)
        bleDeviceInfoArray.removeAll()
        self.updateAllItem()
        
    }
    @objc private func showPopMenuClick() {
        print("showPopMenuClick")
        self.popMenu.show()
    }
    @objc private func getInfoClick() {
        print("getInfoClick")
        let versionNumber = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "Unknown"
        Toast.hudBuilder.title("TFT ECLOCK:V" + versionNumber).show()
    }
    
    @objc func dismissKeyboard() {
        self.view.endEditing(true)
    }
    var popMenu:SwiftPopMenu!
    func initPopMenu(){
        
        //数据源（icon可不填）
        let popData = [(icon:"ic_search.png",title:NSLocalizedString("search", comment: "Search")),
                       (icon:"ic_info.png",title:NSLocalizedString("about", comment: "About"))]
        
        //设置Parameter（可不写）
        let parameters:[SwiftPopMenuConfigure] = [
            .PopMenuTextColor(UIColor.black),
            .popMenuItemHeight(44),
            .PopMenuTextFont(UIFont.systemFont(ofSize: 18))
        ]
        
        //init  (注意：arrow点是基于屏幕的位置)
        popMenu = SwiftPopMenu(menuWidth: 150, arrow: CGPoint(x: KSize.width - 10, y: 70), datas: popData,configures: parameters)
        
        //click
        popMenu.didSelectMenuBlock = { [weak self](index:Int)->Void in
            print ("block sßelect \(index)")
            self?.popMenu.dismiss()
            if index == 0{
                self?.searchClick()
            }else if index == 1{
                self?.getInfoClick()
            }
        }
    }
    
    
    
    func currentDateString() -> String{
        let dateFormatter = DateFormatter()
        // setup formate string for the date formatter
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        // format the current date and time by the date formatter
        let dateStr = dateFormatter.string(from: Date())
        return dateStr
    }
    func makeTable()
    {
        dataTable=UITableView.init(frame: CGRect(x: 0, y: 0, width: KSize.width, height: KSize.height - 10), style:.plain)
        dataTable.delegate = self
        dataTable.backgroundColor = UIColor.white
        dataTable.dataSource = self
        dataTable.tableHeaderView?.isHidden = true;
        dataTable.tableFooterView?.isHidden = true;
        dataTable.tableHeaderView = UIView(frame: CGRect(x: 0, y: 0, width: KSize.width, height: 0.01))
        dataTable.tableFooterView = UIView(frame: CGRect(x: 0, y: 0, width: KSize.width, height: 0.01))
        dataTable.estimatedSectionHeaderHeight = 0;
        dataTable.estimatedSectionFooterHeight = 0;
        dataTable.estimatedRowHeight = 0;
        if #available(iOS 15.0, *) {
            dataTable.sectionHeaderTopPadding = 0
        }else{
            print("is not ios 15")
        }
        self.view.addSubview(dataTable)
        
    }
    
    //MARK:table代理
    
    //段数
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    //行数
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return bleDeviceInfoArray.count
    }
    
    
    func tableView(_ tableView: UITableView, estimatedHeightForRowAt indexPath: IndexPath) -> CGFloat {
        let bleDeviceData = bleDeviceInfoArray[indexPath.row]
        return CGFloat(BleDetailItem.getLayoutHeight(bleDeviceData: bleDeviceData))
        
        
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let bleDeviceData = bleDeviceInfoArray[indexPath.row]
        return CGFloat(BleDetailItem.getLayoutHeight(bleDeviceData: bleDeviceData))
    }
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 0.01
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 0.01
    }
    
    @objc func switchTempUnitTap(tap: UITapGestureRecognizer) {
        print("switchTempUnitTap click tap")
        Utils.switchCurTempUnit()
        print(Utils.getCurTempUnit())
        dataTable.reloadData()
    }
    //cell
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let bleDeviceData = bleDeviceInfoArray[indexPath.row]
        let cell = (tableView.dequeueReusableCell(withIdentifier: BleDetailItem.identifier, for: indexPath)) as! BleDetailItem
        cell.deviceNameContentLabel.text = bleDeviceData.deviceName
        cell.dateContentLabel.text = bleDeviceData.date
        cell.rssiContentLabel.text = bleDeviceData.rssi + "dBm"
        cell.softwareContentLabel.text = bleDeviceData.software
        cell.hardwareContentLabel.text = bleDeviceData.hardware
        if bleDeviceData.isSubLock{
            cell.imeiContentLabel.text = "MAC:"+bleDeviceData.imei
        }else{
            cell.imeiContentLabel.text = "IMEI:"+bleDeviceData.imei
        }
        cell.modelContentLabel.text = bleDeviceData.model
        if(bleDeviceData.deviceId.count > 0){
            if bleDeviceData.isSubLock{
                if bleDeviceData.isDeviceIdValidAndNoneZero(){
                    cell.deviceIdContent.text = "ID:" + bleDeviceData.deviceId
                }else{
                    cell.deviceIdContent.text = ""
                }
            }else{
                if BleDeviceData.isParentLockDeviceIdValid(bleDeviceData.deviceId){
                    cell.deviceIdContent.text = "ID:" + bleDeviceData.deviceId
                }else{
                    cell.deviceIdContent.text = ""
                }
            }
        }else{
            cell.deviceIdContent.text = ""
        }
        if favoriteImeis.contains(bleDeviceData.imei){
            cell.favoriteImage.image = UIImage(named: "ic_show_favorite.png")
        }else{
            cell.favoriteImage.image = UIImage (named: "ic_hide_favorite.png")
        }
        if bleDeviceData.isOpenBackCover{
            cell.lockImage.image = UIImage (named: "ic_openbox.png")
        }else{
            cell.lockImage.image = UIImage (named: "ic_main_lock.png")
        }
        
        cell.batteryContentLabel.text = bleDeviceData.getBatteryPercnet()
        cell.solarVoltageContentLabel.text = String.init(format: "%.3fV", bleDeviceData.solarVoltage ?? 0)
        cell.batteryVoltageContentLabel.text = String.init(format: "%.3fV", bleDeviceData.voltage ?? 0)
        var tempStr = String.init(format: "%.2f %@", Utils.getCurTemp(sourceTemp: bleDeviceData.temp ?? 0),Utils.getCurTempUnit())
        cell.switchTempUnitBtn.tag = indexPath.row
        cell.switchTempUnitBtn.setTitle(Utils.getNextTempUnit(), for: .normal)
        let switchTempUnitTap = UITapGestureRecognizer(target: self, action: #selector(switchTempUnitTap(tap:)))
        cell.switchTempUnitBtn.addGestureRecognizer(switchTempUnitTap)
        cell.tempContentLabel.text = tempStr
        cell.alarmContentLabel.text = bleDeviceData.getAlarm()
        
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(tap(tap:)))
        cell.configBtn.addGestureRecognizer(tapGesture)
        cell.configBtn.tag = indexPath.row
        cell.favoriteImage.isUserInteractionEnabled = true
        let changeFavoriteGesture = UITapGestureRecognizer(target: self, action: #selector(changeDeviceFavorite(tap:)))
        cell.favoriteImage.addGestureRecognizer(changeFavoriteGesture)
        cell.favoriteImage.tag = indexPath.row
        let readDataGesture = UITapGestureRecognizer(target: self, action: #selector(readData(tap:)))
        cell.readDataBtn.addGestureRecognizer(readDataGesture)
        cell.readDataBtn.tag = indexPath.row
        let connectDeviceGesture = UITapGestureRecognizer(target: self, action: #selector(connectTap(tap:)))
        cell.connectDeviceBtn.addGestureRecognizer(connectDeviceGesture)
        cell.connectDeviceBtn.tag = indexPath.row
        let resetDeviceGesture = UITapGestureRecognizer(target: self, action: #selector(resetDevice(tap:)))
        cell.forgetPwdBtn.addGestureRecognizer(resetDeviceGesture)
        cell.forgetPwdBtn.tag = indexPath.row
        if bleDeviceData.isSubLock{
            let showParentLockTipsGesture = UITapGestureRecognizer(target: self, action: #selector(showParentLockTips(tap:)))
            cell.parentLockLabelTips.addGestureRecognizer(showParentLockTipsGesture)
            cell.parentLockLabelTips.tag = indexPath.row
            let showParentLockGesture = UITapGestureRecognizer(target: self, action: #selector(showParentLock(tap:)))
            cell.parentLockContentExtendLabel.addGestureRecognizer(showParentLockGesture)
            cell.parentLockContentExtendLabel.tag = indexPath.row
            let mac = bleDeviceData.id
            if subLockParentLockMap[mac!.uppercased()] != nil || subLockParentLockMap[mac!.lowercased()] != nil{
                var parentList = subLockParentLockMap[mac!.uppercased()]
                if parentList == nil{
                    parentList = subLockParentLockMap[mac!.lowercased()]
                }
                if parentList != nil{
                    if parentList!.count > 1{
                        cell.parentLockContentExtendLabel.isHidden = false
                    }else{
                        cell.parentLockContentExtendLabel.isHidden = true
                    }
                    cell.parentLockContentLabel.text = parentList![0]
                }
            }
        }else{
            cell.parentLockContentLabel.text = ""
        }
        
        cell.initLayout(bleDeviceData: bleDeviceData)
        let viewExpandGesture = UITapGestureRecognizer(target: self, action: #selector(viewExpandTap(tap:)))
        cell.rootView.addGestureRecognizer(viewExpandGesture)
        cell.rootView.tag = indexPath.row

        return cell
    }
    
    
    
    func getQRImageWithString(value:String) ->UIImage{
        let qrFilter = CIFilter(name: "CIQRCodeGenerator")
        let stringData = value.data(using: .utf8)
        qrFilter?.setValue(stringData, forKey: "inputMessage")
        qrFilter?.setValue("H", forKey: "inputCorrectionLevel")
        return UIImage.init(ciImage: (qrFilter?.outputImage)!)
    }
    
    
    @objc func changeDeviceFavorite(tap: UITapGestureRecognizer) {
        print("changeDeviceFavorite tap")
        let index = tap.view?.tag
        if index != nil {
            let bleDeviceData = bleDeviceInfoArray[index ?? 0]
            if !favoriteImeis.contains(bleDeviceData.imei){
                favoriteImeis.append(bleDeviceData.imei)
            }else{
                favoriteImeis.removeAll{ $0 == bleDeviceData.imei }
            }
            self.saveFavoriteImeis()
            self.dataTable.reloadData()
        }
    }
    @objc func readData(tap: UITapGestureRecognizer) {
        print("config click tap")
        let index = tap.view?.tag
        if index != nil {
            let bleDeviceData = bleDeviceInfoArray[index ?? 0]
            let macStr = bleDeviceData.mac
            print(macStr)
            for item in self.discoveredPeripherals{
                if item.mac == macStr {
                    let peripheral = item.peripheral
                    self.centralManager.stopScan()
                    if bleDeviceData.isSubLock{
                        let editView = ReadSubLockHisDataController()
                        editView.cbPeripheral = peripheral
                        editView.name = bleDeviceData.deviceName
                        editView.mac = bleDeviceData.mac
                        editView.model = bleDeviceData.model
                        self.navigationController?.pushViewController(editView, animated: false)
                    }else{
                        let editView = ReadHisDataViewController()
                        editView.cbPeripheral = peripheral
                        editView.name = bleDeviceData.deviceName
                        self.navigationController?.pushViewController(editView, animated: false)
                    }
                  
                }
            }
        }
    }
    @objc func resetDevice(tap: UITapGestureRecognizer) {
        print("resetDevice click tap")
        let index = tap.view?.tag
        if index != nil {
            let bleDeviceData = bleDeviceInfoArray[index ?? 0]
            let macStr = bleDeviceData.mac
            print(macStr)
            for item in self.discoveredPeripherals{
                if item.mac == macStr {
                    let peripheral = item.peripheral
                    self.centralManager.stopScan()
                    let editView = SuperPwdResetController()
                    editView.cbPeripheral = peripheral
//                    editView.name = bleDeviceData.deviceName
                    editView.mac = bleDeviceData.deviceName
                    self.navigationController?.pushViewController(editView, animated: false)
                }
            }
        }
    }
    
    @objc func showParentLock(tap: UITapGestureRecognizer) {
        print("showParentLock click tap")
        let index = tap.view?.tag
        if index != nil {
            let bleDeviceData = bleDeviceInfoArray[index ?? 0]
            let mac = bleDeviceData.id
            if subLockParentLockMap[mac!.uppercased()] != nil || subLockParentLockMap[mac!.lowercased()] != nil{
                var parentList = subLockParentLockMap[mac!.uppercased()]
                if parentList == nil{
                    parentList = subLockParentLockMap[mac!.lowercased()]
                }
                if parentList != nil{
                    let parentLockStr = parentList!.joined(separator: ";")
                    let alertController = UIAlertController(title: NSLocalizedString("parent_lock", comment: "parent_lock"), message: parentLockStr, preferredStyle: .alert)
                   // 添加一个“确定”按钮
                   let okAction = UIAlertAction(title: NSLocalizedString("confirm", comment: "confirm"), style: .default, handler: nil)
                   alertController.addAction(okAction)
                   // 显示 UIAlertController
                   present(alertController, animated: true, completion: nil)
                }
            }
        }
    }
    @objc func showParentLockTips(tap: UITapGestureRecognizer) {
        print("showParentLockTips click tap")
        Toast.hudBuilder.title(NSLocalizedString("sub_lock_relation_dependency", comment: "Letter lock relationships depend on having connected the parent lock and read its sub-locks on this phone, and this relationship is stored locally.")).show()
    }
     
    @objc func viewExpandTap(tap: UITapGestureRecognizer) {
        print("viewExpandTap tap")
        let index = tap.view?.tag
        if index != nil {
            let bleDeviceData = bleDeviceInfoArray[index ?? 0]
            bleDeviceData.viewIsExpand = !bleDeviceData.viewIsExpand
            dataTable.reloadData()
        }
    }
    
    
    @objc func tap(tap: UITapGestureRecognizer) {
        print("config click tap")
        let index = tap.view?.tag
        if index != nil {
            let bleDeviceData = bleDeviceInfoArray[index ?? 0]
            let macStr = bleDeviceData.mac
            print(macStr)
            for item in self.discoveredPeripherals{
                if item.mac == macStr {
                    let peripheral = item.peripheral
                    self.centralManager.stopScan()
                    let editView = UnlockController()
                    editView.cbPeripheral = peripheral
                    editView.name = bleDeviceData.deviceName
                    editView.model = bleDeviceData.model
                    editView.software = bleDeviceData.software
                    editView.deviceId = bleDeviceData.deviceId
                    editView.imei = bleDeviceData.imei
                    self.navigationController?.pushViewController(editView, animated: false)
                }
            }
        }
    }
    
    @objc func connectTap(tap: UITapGestureRecognizer) {
        print("config click tap")
        let index = tap.view?.tag
        if index != nil {
            let bleDeviceData = bleDeviceInfoArray[index ?? 0]
            let macStr = bleDeviceData.mac
            print(macStr)
            for item in self.discoveredPeripherals{
                if item.mac == macStr {
                    let peripheral = item.peripheral
                    self.centralManager.stopScan()
                    let editView = EditController()
                    editView.cbPeripheral = peripheral
                    editView.name = bleDeviceData.deviceName
                    editView.model = bleDeviceData.model
                    editView.software = bleDeviceData.software
                    editView.deviceId = bleDeviceData.deviceId
                    editView.imei = bleDeviceData.imei
                    self.navigationController?.pushViewController(editView, animated: false)
                }
            }
        }
    }
    
    
    //选中cell触发的代理
    //    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    //        print("indexPath.row = SelectRow第\(indexPath.row)行")
    //    }
    //取消选中cell时触发这个代理
    //    public func tableView(_ tableView: UITableView, didDeselectRowAt indexPath: IndexPath) {
    //        print("indexPath.row = DeselectRow第\(indexPath.row)行")
    //    }
    //允许编辑cell
    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return false
    }
    
    func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
        self.searchBar.resignFirstResponder()
        return indexPath
    }
    
    
    //    //右划触发删除按钮
    //    func tableView(_ tableView: UITableView, editingStyleForRowAt indexPath: IndexPath) -> UITableViewCell.EditingStyle {
    ////        return UITableViewCell.EditingStyle.init(rawValue: 1)!
    //    }
    //点击删除cell时触发
    
    
    
}

