//
//  ViewController.swift
//  tftble
//
//  Created by jeech on 2019/12/11.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import UIKit
import CLXToast
import iOSDFULibrary 
import CoreBluetooth
import swiftScan
import QMUIKit
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
        if self.count < endIndex || self.count < startIndex{
            return self
        }
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
    private var fuzzyKey = ""
    private var deviceInfoArray = [[String:String]]()
    private var allDeviceInfoArray = [[String:String]]()
    private var dataTable:UITableView!
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
    //    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
    //        print("Connected")
    //        peripheral.delegate = self
    //        self.centralManager.stopScan()
    //        peripheral.discoverServices(nil)
    //    }
    
    //    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
    //        print("didDiscoverServices")
    //    }
    //    func peripheral(_ peripheral: CBPeripheral, didDiscoverIncludedServicesFor service: CBService, error: Error?) {
    //        print("didDiscoverIncludedServicesFor")
    //    }
    
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
        
        if advertisementData["kCBAdvDataManufacturerData"]  != nil && advertisementData["kCBAdvDataServiceData"]  != nil{
            //receive uid msg
            let factData = advertisementData["kCBAdvDataManufacturerData"] as! Data
            if factData.count > 3 && (factData[2] == 0x07 || factData[2] == 0x08 || factData[2] == 0x09 || factData[2] == 0x0a) {
                
            }else{
                return
            }
            var i = 0
            
            let dict = advertisementData["kCBAdvDataServiceData"] as! NSDictionary
            var key = CBUUID(string:"FEAA")
            if dict[key] != nil && factData[0] == 0x00 && factData[1] == 0x10{
                let data = dict[key] as! Data
                var i = 0
                
                //                print(result)
                let mac = peripheral.identifier.uuidString
                //                        var data = self.hex2String(data: dict[key] as! NSData );
                
                let name = advertisementData["kCBAdvDataLocalName"] as! String
                //                print(name)
                var rssi = -9999
                if RSSI != nil{
                    rssi = Int(RSSI)
                }
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
                //                print(advertisementData)
                //paseEddystoneUid(factResult,result)
                self.parseEddystoneUID(deviceName: name ?? "", bleData: factData,bleData1: data, rssi: rssi, mac: mac)
            }
        }
        
        if advertisementData["kCBAdvDataManufacturerData"]  != nil {
            let data = advertisementData["kCBAdvDataManufacturerData"] as! Data
            
            var i = 0
            var result = ""
            while i < data.count{
                result.append(String(format: "%02x", data[i]))
                i+=1;
            }
            
            if result.count != 34{
                return
            }
            //            print(result)
            let head = result.subStr(startIndex: 0, endIndex: 2)
            if head == "ac" {
                let mac = peripheral.identifier.uuidString
                //                print(mac)
                var name = ""
                if advertisementData["kCBAdvDataLocalName"] != nil {
                    name = advertisementData["kCBAdvDataLocalName"] as! String
                }
                //                print(name)
                var rssi = -9999
                if RSSI != nil{
                    rssi = Int(RSSI)
                }
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
                
                self.parseBleTireData(deviceName: name ?? "", bleData: result, rssi: rssi, mac: mac)
            }
        }
        
        if advertisementData["kCBAdvDataServiceData"]  != nil {
            let dict = advertisementData["kCBAdvDataServiceData"] as! NSDictionary
            var key = CBUUID(string:"BFFF")
            
            if dict[key] != nil{
                let data = dict[key] as! Data
                var i = 0
                var result = ""
                while i < data.count{
                    result.append(String(format: "%02x", data[i]))
                    i+=1;
                }
                //                print(result)
                let mac = peripheral.identifier.uuidString
                //                        var data = self.hex2String(data: dict[key] as! NSData );
                
                var name = ""
                if advertisementData["kCBAdvDataLocalName"] != nil {
                    name = advertisementData["kCBAdvDataLocalName"] as! String
                }
                //                print(name)
                var rssi = -9999
                if RSSI != nil{
                    rssi = Int(RSSI)
                }
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
                
                self.parseBleData(deviceName: name, bleData: result, rssi: rssi, mac: mac)
                
            }
            
            key = CBUUID(string:"FEAA")
            if dict[key] != nil{
                let data = dict[key] as! Data
                if data.count > 0 && (data[0] == 0x07 || data[0] == 0x08 || data[0] == 0x09 || data[0] == 0x0a) {
                    
                }else{
                    return
                }
                var i = 0
                var result = ""
                while i < data.count{
                    result.append(String(format: "%02x", data[i]))
                    i+=1;
                }
                //                print(result)
                let mac = peripheral.identifier.uuidString
                //                        var data = self.hex2String(data: dict[key] as! NSData );
                var name = ""
                if advertisementData["kCBAdvDataLocalName"] != nil {
                    name = advertisementData["kCBAdvDataLocalName"] as! String
                }
                //                print(name)
                var rssi = -9999
                if RSSI != nil{
                    rssi = Int(RSSI)
                }
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
                //parseEddystone(result)
                self.parseEddystone(deviceName: name, bleData: data, rssi: rssi, mac: mac)
            }
            key = CBUUID(string:"6135")
            if dict[key] != nil{
                let data = dict[key] as! Data
                if data.count > 0 && (data[0] == 0x07 || data[0] == 0x08 || data[0] == 0x09 || data[0] == 0x0a) {
                    
                }else{
                    return
                }
                var i = 0
                var result = ""
                while i < data.count{
                    result.append(String(format: "%02x", data[i]))
                    i+=1;
                }
                //                print(result)
                let mac = peripheral.identifier.uuidString
                //                        var data = self.hex2String(data: dict[key] as! NSData );
                var name = ""
                if advertisementData["kCBAdvDataLocalName"] != nil {
                    name = advertisementData["kCBAdvDataLocalName"] as! String
                }
                //                print(name)
                var rssi = -9999
                if RSSI != nil{
                    rssi = Int(RSSI)
                }
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
                //parseEddystone(result)
                self.parseEddystone(deviceName: name, bleData: data, rssi: rssi, mac: mac)
            }
        }
    }
    var discoveredPeripherals:[(peripheral: CBPeripheral, mac: String?)] = []
    
    
    @objc private func leftClick() {
        print("leftClick")
        self.deviceInfoArray = []
        self.allDeviceInfoArray = []
        self.dataTable.reloadData()
        self.showWaitingWin(title:"Loading")
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
    
    @objc private func rightClick() {
        //     let historyView = HistoryReportController()
        //        historyView.startDate = Int(Date().timeIntervalSince1970)
        //        historyView.endDate = Int(Date().timeIntervalSince1970)
        //
        //      self.navigationController?.pushViewController(historyView, animated: false)
        //        let smtpView = SmtpSettingController()
        //        self.navigationController?.pushViewController(smtpView, animated: false)
        print("rightClick")
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
    func dataTableFuzzySearch(){
        var i = 0
        deviceInfoArray = []
        while i < allDeviceInfoArray.count{
            let deviceInfo = allDeviceInfoArray[i]
            let deviceName:String = deviceInfo["name"] ?? ""
            let id:String = deviceInfo["id"] ?? ""
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
                    deviceInfoArray.append(deviceInfo)
                }
            }else{
                deviceInfoArray.append(deviceInfo)
            }
            i+=1
        }
        dataTable.reloadData()
    }
    
    override func viewDidLoad() {
        print("viewDidLoad")
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.white
        self.view.autoresizingMask = UIView.AutoresizingMask.flexibleWidth
        self.view.backgroundColor = UIColor.nordicLightGray
        makeTable()
        dataTable.autoresizingMask = UIView.AutoresizingMask.flexibleWidth
        dataTable.register(DeviceS02Cell.self,forCellReuseIdentifier:DeviceS02Cell.identifier)
        dataTable.register(DeviceS04Cell.self,forCellReuseIdentifier:DeviceS04Cell.identifier)
        dataTable.register(DeviceS05Cell.self,forCellReuseIdentifier:DeviceS05Cell.identifier)
        dataTable.register(DeviceErrorCell.self,forCellReuseIdentifier:DeviceErrorCell.identifier)
        dataTable.register(DeviceTireCell.self,forCellReuseIdentifier:DeviceTireCell.identifier)
        dataTable.register(DeviceS07Cell.self,forCellReuseIdentifier:DeviceS07Cell.identifier)
        dataTable.register(DeviceS08Cell.self,forCellReuseIdentifier:DeviceS08Cell.identifier)
        dataTable.register(DeviceS09Cell.self,forCellReuseIdentifier:DeviceS09Cell.identifier)
        dataTable.register(DeviceS10Cell.self,forCellReuseIdentifier:DeviceS10Cell.identifier)
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        //        titleLabel.text = "Bluetooth sensor"
        titleLabel.text = NSLocalizedString("bluetooth_sensor", comment: "Bluetooth sensor")
        self.navigationItem.titleView = titleLabel
        let rightBarButtonItem = UIBarButtonItem (barButtonSystemItem: UIBarButtonItem.SystemItem.search, target: self, action: #selector(self.rightClick))
        let leftBarButtonItem = UIBarButtonItem (barButtonSystemItem: UIBarButtonItem.SystemItem.refresh, target: self, action: #selector(self.leftClick))
        self.navigationItem.rightBarButtonItem = rightBarButtonItem
        self.navigationItem.leftBarButtonItem = leftBarButtonItem
        navigationController?.navigationBar.barTintColor = UIColor.colorPrimary
        
        
        centralManager = CBCentralManager(delegate: self, queue: nil)
        self.searchBar = UISearchBar()
        self.searchBar.backgroundColor = UIColor.white
        self.searchBar.barTintColor = UIColor.white
        self.searchBar.frame = CGRect(x:0,y:0,width: KSize.width - 60,height:60)
        self.searchBar.delegate = self
        self.searchView = UIView()
        self.searchView.backgroundColor = UIColor.white
        self.searchView.frame = CGRect(x:0,y:60,width: KSize.width,height:60)
        self.searchView.isHidden = true
        self.scanBtn = UIButton()
        self.scanBtn.backgroundColor = UIColor.white
        self.scanBtn.frame = CGRect(x:KSize.width - 60,y:0,width: 60,height:60)
        self.scanBtn.setImage(UIImage (named: "scan.png"), for: UIControl.State.normal)
        self.scanBtn.addTarget(self, action: #selector(scanBtnClick), for: .touchUpInside)
        self.searchView.addSubview(self.scanBtn)
        self.searchView.addSubview(self.searchBar)
        self.view.addSubview(self.searchView)
    }
    
    
    func parseEddystoneUID(deviceName:String,bleData:Data,bleData1:Data,rssi:Int,mac:String){
        //        print("parseEddystoneUID " + deviceName)
        var deviceItem = [String:String]()
        if deviceName == ""{
            return
        }else{
            deviceItem.updateValue(deviceName, forKey: "name")
        }
        var bleData1Str = ""
        var i = 0
        while i < bleData1.count{
            bleData1Str.append(String(format: "%02x", bleData1[i]))
            i+=1;
        }
        let nid = bleData1Str.subStr(startIndex: 4, endIndex: 24)
        let bid = bleData1Str.subStr(startIndex: 25, endIndex: 36)
        var deviceType = "ErrorDevice"
        var model = ""
        if bleData[2] == 0x07{
            deviceType = "S07"
            model = "T-buton"
        }else if bleData[2] == 0x08{
            deviceType = "S08"
            model="T-sense"
        }else if bleData[2] == 0x09{
            deviceType = "S09"
            model="T-hub"
        }else if bleData[2] == 0x0a{
            deviceType = "S10"
            model = "T-one"
        }
        if deviceType == "ErrorDevice"{
            print("%@",bleData)
            print("parseEddystoneUID ErrorDevice " + deviceName)
            return
        }
        let hardware = Utils.parseHardwareVersion(hardware: String(format: "%02x", bleData[3]))
        let all = Utils.data2Short(bytes: bleData, offset: 4)
        let version1 = bleData[4] >> 5
        let version2 = (all & 0x1FFF) >> 7
        let version3 = all & 0x7f
        let testByte = bleData[6]
        var software = ""
        if testByte != 0{
            software = String(format: "%d.%d.%02d  %02x",version1,version2,version3, testByte)
        }else{
            software = String(format: "%d.%d.%02d",version1,version2,version3)
        }
        let broadcastType = "Eddystone UID"
        i = 7
        var id = ""
        while i < bleData.count{
            id.append(String(format: "%02x", bleData[i]))
            i+=1;
        }
        //        print("parseEddystoneUID,%@,%@,%@,%@,%@",deviceName,nid,bid,hardware,software)
        let flag = "-"
        let battery = "-"
        let batteryPercent = "-"
        let doorStatus = "-"
        let move = "-"
        let moveDetection = "-"
        let stopDetection = "-"
        let pitchAngle = "-"
        let rollAngle = "-"
        let deviceProp = "-"
        let major = "-"
        let minor = "-"
        let warnStr = ""
        let date = self.currentDateString()
        deviceItem.updateValue(date,forKey:"date")
        
        deviceItem.updateValue(mac, forKey: "mac")
        deviceItem.updateValue(id.uppercased(), forKey: "id")
        if rssi != -9999{
            let rssiStr = String.init(format: "%ddBm", rssi)
            deviceItem.updateValue(rssiStr, forKey: "rssi")
        }
        deviceItem.updateValue(deviceType, forKey: "deviceType")
        deviceItem.updateValue(model, forKey: "deviceTypeDesc")
        deviceItem.updateValue(hardware, forKey: "hardware")
        deviceItem.updateValue(software, forKey: "software")
        deviceItem.updateValue(broadcastType, forKey: "broadcastType")
        deviceItem.updateValue(flag, forKey: "flag")
        deviceItem.updateValue(battery, forKey: "battery")
        deviceItem.updateValue(batteryPercent, forKey: "batteryPercent")
        deviceItem.updateValue(move, forKey: "move")
        deviceItem.updateValue(moveDetection, forKey: "moveDetection")
        deviceItem.updateValue(stopDetection, forKey: "stopDetection")
        deviceItem.updateValue(pitchAngle, forKey: "pitchAngle")
        deviceItem.updateValue(rollAngle, forKey: "rollAngle")
        deviceItem.updateValue(deviceProp, forKey: "deviceProp")
        deviceItem.updateValue(major, forKey: "major")
        deviceItem.updateValue(minor, forKey: "minor")
        deviceItem.updateValue(nid, forKey: "nid")
        deviceItem.updateValue(bid, forKey: "bid")
        deviceItem.updateValue(warnStr, forKey: "warn")
        
        updateDeviceInfo(deviceItem: deviceItem,mac:mac,deviceName:deviceName,id:id)
    }
    func parseEddystone(deviceName:String,bleData:Data,rssi:Int,mac:String){
        //        print("parseEddystone")
        var deviceItem = [String:String]()
        if deviceName == ""{
            return
        }else{
            deviceItem.updateValue(deviceName, forKey: "name")
        }
        var deviceType = "ErrorDevice"
        var model = ""
        if bleData[0] == 0x07{
            deviceType = "S07"
            model = "T-buton"
        }else if bleData[0] == 0x08{
            deviceType = "S08"
            model="T-sense"
        }else if bleData[0] == 0x09{
            deviceType = "S09"
            model="T-hub"
        }else if bleData[0] == 0x0a{
            deviceType = "S10"
            model = "T-one"
        }
        if deviceType == "ErrorDevice"{
            return
        }
        let hardware = Utils.parseHardwareVersion(hardware: String(format: "%02x", bleData[1]))
        let all = (Int(bleData[2]) << 8) + Int(bleData[2 + 1])
        let version1 = bleData[2] >> 5
        let version2 = (all & 0x1FFF) >> 7
        let version3 = all & 0x7f
        let testByte = bleData[4]
        var software = ""
        if testByte != 0{
            software = String(format: "%d.%d.%02d  %02x",version1,version2,version3, testByte)
        }else{
            software = String(format: "%d.%d.%02d",version1,version2,version3)
        } 
        var broadcastType = "Eddystone"
        var i = 5
        var id = ""
        while i < 11{
            id.append(String(format: "%02x", bleData[i]))
            i+=1;
        }
        //        print("parseEddystone,%@,%@,%@",deviceName,hardware,software)
        var flag = "-"
        var battery = "-"
        var batteryPercent = "-"
        var doorStatus = "-"
        var move = "-"
        var moveDetection = "-"
        var stopDetection = "-"
        var pitchAngle = "-"
        var rollAngle = "-"
        var deviceProp = "-"
        var major = "-"
        var minor = "-"
        var nid = "-"
        var bid = "-"
        let date = self.currentDateString()
        deviceItem.updateValue(date,forKey:"date")
       
       
        deviceItem.updateValue(mac, forKey: "mac")
        deviceItem.updateValue(id.uppercased(), forKey: "id")
        if rssi != -9999{
            let rssiStr = String.init(format: "%ddBm", rssi)
            deviceItem.updateValue(rssiStr, forKey: "rssi")
        }
        deviceItem.updateValue(deviceType, forKey: "deviceType")
        deviceItem.updateValue(model, forKey: "deviceTypeDesc")
        deviceItem.updateValue(hardware, forKey: "hardware")
        var softwareStr = "V" + software
        var softwareInt = Int(software.replacingOccurrences(of: ".", with: "")) ?? 0
        deviceItem.updateValue(software, forKey: "software")
        deviceItem.updateValue(String(softwareInt), forKey: "softwareInt")
        if bleData[0] == 0x07{
            if((bleData[11] & 0x01) == 0x01){
                broadcastType = "Long range"
            }else  {
                broadcastType.append(" ")
                broadcastType.append(model)
            }
            battery = String(format: "%.3f V", Float(Utils.data2Short(bytes: bleData, offset: 12)) / 1000.0)
            batteryPercent = String(format: "%d%%", bleData[14] & 0xff )
            var warn =	bleData[15]
            
            if(bleData[16] == 0x01){
                flag = "SOS"
            }else if(bleData[16] == 0x02){
                flag = NSLocalizedString("identification", comment: "Identification")
            }
            var warnStr = getWarnDesc(deviceType: deviceType, warnByte: warn)
            deviceItem.updateValue(broadcastType, forKey: "broadcastType")
            deviceItem.updateValue(flag, forKey: "flag")
            deviceItem.updateValue(battery, forKey: "battery")
            deviceItem.updateValue(batteryPercent, forKey: "batteryPercent")
            deviceItem.updateValue(warnStr, forKey: "warn")
        }else if bleData[0] == 0x08{
            if((bleData[11] & 0x01) == 0x01){
                broadcastType = "Long range"
            }else  {
                broadcastType.append(" ")
                broadcastType.append(model)
            }
            var isGsensor = false
            if((bleData[11] & 0x02) == 0x02){
                isGsensor = true
            }else if((bleData[11] & 0x04) == 0x04){
                
            }
            battery = String(format: "%.3f V", Float(Utils.data2Short(bytes: bleData, offset: 12)) / 1000.0)
            batteryPercent = String(format: "%d%%", bleData[14] & 0xff )
            var tempSrc = Utils.data2Short(bytes: bleData, offset: 15)
            var temp = (tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1)
            deviceProp =  ""
            if(bleData[18] == 0x00){
                deviceProp =  NSLocalizedString("normal", comment: "Normal")
            }else{
                deviceProp = NSLocalizedString("strong_light", comment: "Strong light")
            }
            var humidityByte = bleData[17]
            var doorByte = bleData[19]
            var doorStatus = ""
            if(doorByte == 0x02){
                doorStatus = NSLocalizedString("disable", comment: "Disable")
            }else{
                doorStatus = doorByte == 0x01 ? NSLocalizedString("prop_door_open", comment: "Open") : NSLocalizedString("prop_door_close", comment: "Close")
            }
            var warnStr = ""
            if isGsensor{
                var moveStatus = (bleData[20] & 0xff) >> 6
                move = ""
                if(moveStatus == 0){
                    move = NSLocalizedString("move_static", comment: "Static")
                }else if(moveStatus == 1){
                    move = NSLocalizedString("move_move", comment: "Move")
                }else if(moveStatus == 2){
                    move = NSLocalizedString("move_forbidden", comment: "Forbidden")
                }
                deviceItem.updateValue(String(moveStatus), forKey: "moveStatus")
                var moveInt = Utils.data2Short(bytes: bleData, offset: 20)
                var stopInt = Utils.data2Short(bytes: bleData, offset: 21)
                moveDetection = String(format: "%d", (moveInt & 0x3FF8) >> 3)
                stopDetection = String(format: "%d", stopInt & 0x7ff)
                pitchAngle = String(format: "%d", bleData[23])
                rollAngle = String(format: "%d", Utils.data2Short(bytes: bleData, offset: 24))
                warnStr = getWarnDesc(deviceType: deviceType, warnByte: bleData[26])
            }else{
                warnStr = getWarnDesc(deviceType: deviceType, warnByte: bleData[20])
            }
            deviceItem.updateValue(doorStatus, forKey: "doorStatus")
            deviceItem.updateValue(broadcastType, forKey: "broadcastType")
            deviceItem.updateValue(String(temp), forKey: "sourceTemp")
            deviceItem.updateValue(flag, forKey: "flag")
            deviceItem.updateValue(battery, forKey: "battery")
            deviceItem.updateValue(batteryPercent, forKey: "batteryPercent")
            deviceItem.updateValue(move, forKey: "move")
            deviceItem.updateValue(moveDetection, forKey: "moveDetection")
            deviceItem.updateValue(stopDetection, forKey: "stopDetection")
            deviceItem.updateValue(pitchAngle, forKey: "pitchAngle")
            deviceItem.updateValue(rollAngle, forKey: "rollAngle")
            deviceItem.updateValue(deviceProp, forKey: "deviceProp")
            deviceItem.updateValue(major, forKey: "major")
            deviceItem.updateValue(minor, forKey: "minor")
            deviceItem.updateValue(nid, forKey: "nid")
            deviceItem.updateValue(bid, forKey: "bid")
            deviceItem.updateValue(warnStr, forKey: "warn")
        }else if bleData[0] == 0x09{
            broadcastType.append(" ")
            broadcastType.append(model)
            deviceItem.updateValue(broadcastType, forKey: "broadcastType")
        }else if bleData[0] == 0x0a{
            if((bleData[11] & 0x01) == 0x01){
                broadcastType = "Long range"
            }else  {
                broadcastType.append(" ")
                broadcastType.append(model)
            }
            var isGsensor = false
            if((bleData[11] & 0x02) == 0x02){
                isGsensor = true
            }else if((bleData[11] & 0x04) == 0x04){
                
            }
            var battery = String(format: "%.3f V", Float(Utils.data2Short(bytes: bleData, offset: 12)) / 1000.0)
            var batteryPercent = String(format: "%d%%", bleData[14] & 0xff )
            var tempSrc = Utils.data2Short(bytes: bleData, offset: 15)
            var temp = (tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1)
            var deviceProp =  ""
            if(bleData[18] == 0x00){
                deviceProp =  NSLocalizedString("normal", comment: "Normal")
            }else{
                deviceProp = NSLocalizedString("strong_light", comment: "Strong light")
            }
            var humidity = String(format: "%d%%",  bleData[17])
            var warnStr = getWarnDesc(deviceType: deviceType, warnByte: bleData[19])
            deviceItem.updateValue(String(temp), forKey: "sourceTemp")
            deviceItem.updateValue(humidity, forKey: "humidity")
            deviceItem.updateValue(broadcastType, forKey: "broadcastType")
            deviceItem.updateValue(flag, forKey: "flag")
            deviceItem.updateValue(battery, forKey: "battery")
            deviceItem.updateValue(batteryPercent, forKey: "batteryPercent")
            deviceItem.updateValue(move, forKey: "move")
            deviceItem.updateValue(moveDetection, forKey: "moveDetection")
            deviceItem.updateValue(stopDetection, forKey: "stopDetection")
            deviceItem.updateValue(pitchAngle, forKey: "pitchAngle")
            deviceItem.updateValue(rollAngle, forKey: "rollAngle")
            deviceItem.updateValue(deviceProp, forKey: "ambientLight")
            deviceItem.updateValue(major, forKey: "major")
            deviceItem.updateValue(minor, forKey: "minor")
            deviceItem.updateValue(nid, forKey: "nid")
            deviceItem.updateValue(bid, forKey: "bid")
            deviceItem.updateValue(warnStr, forKey: "warn")
        }
        
        updateDeviceInfo(deviceItem: deviceItem,mac:mac,deviceName:deviceName,id:id)
        
    }
    
    func parseBleTireData(deviceName:String,bleData:String,rssi:Int,mac:String){
        if Utils.isDebug{
            //            if rssi < -50 || rssi > 0{
            //                return
            //            }
        }
        var deviceItem = [String:String]()
        var deviceType = "tire"
        let date = self.currentDateString()
        let idReverse = bleData.subStr(startIndex: 22, endIndex: 34)
        let id = idReverse.subStr(startIndex: 10, endIndex: 12) + idReverse.subStr(startIndex: 8, endIndex: 10) + idReverse.subStr(startIndex: 6, endIndex: 8)
        + idReverse.subStr(startIndex: 4, endIndex: 6) + idReverse.subStr(startIndex: 2, endIndex: 4) + idReverse.subStr(startIndex: 0, endIndex: 2)
        let battery = bleData.subStr(startIndex:4,endIndex:6)
        let presssureStr = bleData.subStr(startIndex:6,endIndex:8)
        let tempStr = bleData.subStr(startIndex:8,endIndex:10)
        let statusStr = bleData.subStr(startIndex:10,endIndex:12)
        
        deviceItem.updateValue(date,forKey:"date")
        deviceItem.updateValue(deviceName, forKey: "name")
        deviceItem.updateValue(mac, forKey: "mac")
        deviceItem.updateValue(id.uppercased(), forKey: "id")
        if rssi != -9999{
            let rssiStr = String.init(format: "%ddBm", rssi)
            deviceItem.updateValue(rssiStr, forKey: "rssi")
        }
        deviceItem.updateValue("tire", forKey: "deviceType")
        deviceItem.updateValue("TPMS", forKey: "deviceTypeDesc")
        let batteryValue = battery.hexStringToInt()
        let batteryVlaueF = Float(batteryValue) * 0.01 + 1.22
        let batteryValueStr = String.init(format: "%.2f V", batteryVlaueF)
        deviceItem.updateValue(batteryValueStr, forKey: "battery")
        let pressureValue = presssureStr.hexStringToInt()
        let pressureValueF:Float = 1.572 * 2 * Float(pressureValue)
        deviceItem.updateValue(String(pressureValueF), forKey: "tirePressure")
        let tempValue = tempStr.hexStringToInt()
        let tempValueF = tempValue - 55
        deviceItem.updateValue(String(tempValueF), forKey: "sourceTemp")
        var statusDesc = ""
        if statusStr == "00"{
            statusDesc = NSLocalizedString("normal", comment:"Narmal")
        }else if statusStr == "01"{
            statusDesc = NSLocalizedString("leak", comment:"Leak")
        }else if statusStr == "02"{
            statusDesc = NSLocalizedString("inflation", comment:"Inflation")
        }else if statusStr == "03"{
            statusDesc = NSLocalizedString("startUp", comment:"start-up")
        }else if statusStr == "04"{
            statusDesc = NSLocalizedString("power_on", comment:"Power on")
        }else if statusStr == "05"{
            statusDesc = NSLocalizedString("awaken", comment:"Awaken")
        }
        deviceItem.updateValue(statusDesc, forKey: "status")
        
        updateDeviceInfo(deviceItem: deviceItem,mac:mac,deviceName:deviceName,id:id)
    }
    
    
    func updateDeviceInfo(deviceItem:[String:String],mac:String,deviceName:String,id:String ){
        var exist = false
        var i = 0
        let mac = deviceItem["mac"]
        while i < deviceInfoArray.count{
            let deviceInfo = deviceInfoArray[i]
            let macStr = deviceInfo["mac"]
            if mac == macStr{
                deviceInfoArray[i] = deviceItem
                exist = true
                break
            }
            i+=1
        }
        if !exist
        {
            if fuzzyKey != ""{
                let deviceRange = deviceName.range(of: fuzzyKey)
                var deviceNameLocation = -1
                if deviceRange != nil{
                    deviceNameLocation = deviceName.distance(from: deviceName.startIndex, to: deviceRange!.lowerBound)
                }
                let idRange = id.range(of:fuzzyKey)
                var idLocation = -1
                if idRange != nil{
                    idLocation = id.distance(from: id.startIndex, to: idRange!.lowerBound)
                }
                if deviceNameLocation != -1 || idLocation != -1{
                    deviceInfoArray.append(deviceItem)
                }
            }else{
                deviceInfoArray.append(deviceItem)
            }
            
        }
        exist = false
        i = 0
        while i < allDeviceInfoArray.count{
            let deviceInfo = allDeviceInfoArray[i]
            let macStr = deviceInfo["mac"]
            if mac == macStr{
                allDeviceInfoArray[i] = deviceItem
                exist = true
                break
            }
            i+=1
        }
        if !exist
        {
            allDeviceInfoArray.append(deviceItem)
        }
        if deviceInfoArray.count > 0{
            if self.waitingView != nil {
                self.waitingView.dismiss()
            }
        }
        dataTable.reloadData()
    }
    
    
    func getWarnDesc(deviceType:String,warnByte:UInt8) ->String{
        if(deviceType == "S02" ){
            var warnStr = ""
            if (warnByte & 0x01) == 0x01 {
                warnStr += NSLocalizedString("high_temperature_warning", comment:"High temperature warning;")
            }
            if (warnByte & 0x02) == 0x02 {
                warnStr += NSLocalizedString("high_humidity_warning", comment:"High humidity warning;")
            }
            if (warnByte & 0x10) == 0x10 {
                warnStr += NSLocalizedString("low_temperature_warning", comment:"Low temperature warning;")
            }
            if (warnByte & 0x20) == 0x20{
                warnStr += NSLocalizedString("low_humidity_warning", comment:"Low humidity warning;")
            }
            if (warnByte & 0x40) == 0x40{
                warnStr += NSLocalizedString("low_voltage_warning", comment:"Low voltage warning;")
            }
            return warnStr;
        }else if(deviceType=="S04"){
            var warnStr = ""
            if (warnByte & 0x01) == 0x01 {
                warnStr += NSLocalizedString("high_temperature_warning", comment:"High temperature warning;")
            }
            if (warnByte & 0x10) == 0x10 {
                warnStr += NSLocalizedString("low_temperature_warning", comment:"Low temperature warning;")
            }
            if (warnByte & 0x40) == 0x40{
                warnStr += NSLocalizedString("low_voltage_warning", comment:"Low voltage warning;")
            }
            if (warnByte & 0x80) == 0x80{
                warnStr += NSLocalizedString("door_change_warning", comment:"Door change warning;")
            }
            return warnStr
        }else if(deviceType=="S05"){
            var warnStr = ""
            if (warnByte & 0x01) == 0x01 {
                warnStr += NSLocalizedString("high_temperature_warning", comment:"High temperature warning;")
            }
            if (warnByte & 0x02) == 0x02 {
                warnStr += NSLocalizedString("high_humidity_warning", comment:"High humidity warning;")
            }
            if (warnByte & 0x10) == 0x10 {
                warnStr += NSLocalizedString("low_temperature_warning", comment:"Low temperature warning;")
            }
            if (warnByte & 0x20) == 0x20{
                warnStr += NSLocalizedString("low_humidity_warning", comment:"Low humidity warning;")
            }
            if (warnByte & 0x40) == 0x40{
                warnStr += NSLocalizedString("low_voltage_warning", comment:"Low voltage warning;")
            }
            return warnStr;
        }else if(deviceType=="tire"){
            var warnStr = ""
            if (warnByte & 0x01) == 0x01 {
                warnStr += NSLocalizedString("high_temperature_warning", comment:"High temperature warning;")
            }
            if (warnByte & 0x02) == 0x02 {
                warnStr += NSLocalizedString("high_humidity_warning", comment:"High humidity warning;")
            }
            if (warnByte & 0x10) == 0x10 {
                warnStr += NSLocalizedString("low_temperature_warning", comment:"Low temperature warning;")
            }
            if (warnByte & 0x20) == 0x20{
                warnStr += NSLocalizedString("low_humidity_warning", comment:"Low humidity warning;")
            }
            if (warnByte & 0x40) == 0x40{
                warnStr += NSLocalizedString("low_voltage_warning", comment:"Low voltage warning;")
            }
            return warnStr
        }else if(deviceType=="S07"){
            var warnStr = ""
            if (warnByte & 0x40) == 0x40{
                warnStr += NSLocalizedString("low_voltage_warning", comment:"Low voltage warning;")
            }
            return warnStr
        }else if(deviceType=="S08"){
            var warnStr = ""
            if (warnByte & 0x01) == 0x01 {
                warnStr += NSLocalizedString("high_temperature_warning", comment:"High temperature warning;")
            }
            if (warnByte & 0x10) == 0x10 {
                warnStr += NSLocalizedString("low_temperature_warning", comment:"Low temperature warning;")
            }
            if (warnByte & 0x40) == 0x40{
                warnStr += NSLocalizedString("low_voltage_warning", comment:"Low voltage warning;")
            }
            if (warnByte & 0x80) == 0x80{
                warnStr += NSLocalizedString("malfunction", comment:"Malfunction;")
            }
            return warnStr;
        }else if(deviceType=="S10"){
            var warnStr = ""
            if (warnByte & 0x01) == 0x01 {
                warnStr += NSLocalizedString("high_temperature_warning", comment:"High temperature warning;")
            }
            if (warnByte & 0x02) == 0x02 {
                warnStr += NSLocalizedString("high_humidity_warning", comment:"High humidity warning;")
            }
            if (warnByte & 0x10) == 0x10 {
                warnStr += NSLocalizedString("low_temperature_warning", comment:"Low temperature warning;")
            }
            if (warnByte & 0x20) == 0x20{
                warnStr += NSLocalizedString("low_humidity_warning", comment:"Low humidity warning;")
            }
            if (warnByte & 0x40) == 0x40{
                warnStr += NSLocalizedString("low_voltage_warning", comment:"Low voltage warning;")
            }
            if (warnByte & 0x80) == 0x80{
                warnStr += NSLocalizedString("malfunction", comment:"Malfunction;")
            }
            return warnStr
        }
        return ""
        
    }
    
    func parseBleData(deviceName:String,bleData:String,rssi:Int,mac:String){
        if Utils.isDebug{
            //            if rssi < -50 || rssi > 0{
            //                return
            //            }
        }
        var deviceItem = [String:String]()
        let deviceType = bleData.subStr(startIndex:2,endIndex:4)
        let hardwareStr = Utils.parseHardwareVersion(hardware: bleData.subStr(startIndex:4,endIndex:6))
        let software = bleData.subStr(startIndex:6,endIndex:8).hexStringToInt()
        let softwareStr = String.init(format: "V%d", software)
        var realData = bleData.subStr(startIndex:20,endIndex:bleData.characters.count).uppercased()
        realData = realData.replacingOccurrences(of: "FF", with: "")
        let date = self.currentDateString()
        //        print(date)
        deviceItem.updateValue(date,forKey:"date")
        let id = bleData.subStr(startIndex: 8, endIndex: 20).uppercased()
        deviceItem.updateValue(deviceName, forKey: "name")
        deviceItem.updateValue(mac, forKey: "mac")
        deviceItem.updateValue(id, forKey: "id")
        if rssi != -9999{
            let rssiStr = String.init(format: "%ddBm", rssi)
            deviceItem.updateValue(rssiStr, forKey: "rssi")
        }
        
        //        var softwareStr = String.init(format: "V%s", software)
        deviceItem.updateValue(hardwareStr, forKey: "hardware")
        deviceItem.updateValue(softwareStr, forKey: "software")
        deviceItem.updateValue(String(software), forKey: "softwareInt")
        let battery = bleData.subStr(startIndex:20,endIndex:22)
        if realData == "" || realData.characters.count == 0{
            deviceItem.updateValue("errorType", forKey: "deviceType")
            if deviceType == "02"{
                deviceItem.updateValue("Error TSTH1-B", forKey: "deviceTypeDesc")
            }else if deviceType == "04"{
                deviceItem.updateValue("Error TSDT1-B", forKey: "deviceTypeDesc")
            }else if deviceType == "05"{
                deviceItem.updateValue("Error TSR1-B", forKey: "deviceTypeDesc")
            }
        }else{
            let batteryValue = battery.hexStringToInt()
            let realBatteryVlaue = batteryValue & 0x7f;
            if (batteryValue & 0x80) == 0x80
            {
                let batteryVlaueF = Float(realBatteryVlaue) / 100.0 + 2
                let batteryValueStr = String.init(format: "%.2f V", batteryVlaueF)
                deviceItem.updateValue(batteryValueStr, forKey: "battery")
                
            }else{
                let batteryValueStr = String.init(format: "%d%%", realBatteryVlaue)
                deviceItem.updateValue(batteryValueStr, forKey: "battery")
            }
            if deviceType == "02"{
                deviceItem.updateValue("S02", forKey: "deviceType")
                deviceItem.updateValue("TSTH1-B", forKey: "deviceTypeDesc")
                
                let tempSrc = bleData.subStr(startIndex:22,endIndex:26).hexStringToInt()
                let temp =  Float((tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1)) / 100.0
                deviceItem.updateValue(String(temp), forKey: "sourceTemp")
                let humidity = bleData.subStr(startIndex:26,endIndex:28).hexStringToInt()
                let humidityStr = String.init(format: "%d%%", humidity)
                deviceItem.updateValue(humidityStr, forKey: "humidity")
                let lightSensorDisable = bleData.subStr(startIndex:28,endIndex:30).hexStringToInt()
                let lightStr = bleData.subStr(startIndex:30,endIndex:32)
                var light = lightStr == "01" ?  NSLocalizedString("light1", comment: "Light") : NSLocalizedString("dark", comment: "Dark")
                if (lightSensorDisable & 0x80) == 0x80 {
                    light = NSLocalizedString("lightSenseDisable", comment: "Light sense disable")
                }
                deviceItem.updateValue(light, forKey: "light")
                let warn = bleData.subStr(startIndex:32,endIndex:34).hexStringToInt()
                var warnStr = getWarnDesc(deviceType: deviceType, warnByte: UInt8(warn))
                deviceItem.updateValue(warnStr, forKey: "warn")
            }else if deviceType == "04"{
                deviceItem.updateValue("S04", forKey: "deviceType")
                deviceItem.updateValue("TSDT1-B", forKey: "deviceTypeDesc")
                let tempSrc = bleData.subStr(startIndex:22,endIndex:26).hexStringToInt()
                let temp =  Float((tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1)) / 100.0
                deviceItem.updateValue(String(temp), forKey: "sourceTemp")
                let doorSensor = bleData.subStr(startIndex:26,endIndex:28).hexStringToInt()
                if doorSensor & 0x01 == 0x01{
                    deviceItem.updateValue(NSLocalizedString("open", comment: "Open"), forKey: "doorSensor")
                }else{
                    deviceItem.updateValue(NSLocalizedString("close", comment: "Close"), forKey: "doorSensor")
                }
                let warn = bleData.subStr(startIndex:28,endIndex:30).hexStringToInt()
                let warnStr = getWarnDesc(deviceType: deviceType, warnByte: UInt8(warn))
                deviceItem.updateValue(warnStr, forKey: "warn")
            }else if deviceType == "05"{
                deviceItem.updateValue("S05", forKey: "deviceType")
                deviceItem.updateValue("TSR1-B", forKey: "deviceTypeDesc")
                let tempSrc = bleData.subStr(startIndex:22,endIndex:26).hexStringToInt()
                let temp =  Float((tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1)) / 100.0
                deviceItem.updateValue(String(temp), forKey: "sourceTemp")
                let relay = bleData.subStr(startIndex:26,endIndex:28).hexStringToInt()
                if relay & 0x01 == 0x01{
                    deviceItem.updateValue(NSLocalizedString("yes", comment: "Yes"), forKey: "relayStatus")
                }else{
                    deviceItem.updateValue(NSLocalizedString("no", comment: "No"), forKey: "relayStatus")
                }
                let warn = bleData.subStr(startIndex:28,endIndex:30).hexStringToInt()
                var warnStr = getWarnDesc(deviceType: deviceType, warnByte: UInt8(warn))
                deviceItem.updateValue(warnStr, forKey: "warn")
            }
        }
        updateDeviceInfo(deviceItem: deviceItem,mac:mac,deviceName:deviceName,id:id)
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
        dataTable=UITableView.init(frame: CGRect(x: 0, y: 0, width: KSize.width, height: KSize.height), style:.plain)
        dataTable.delegate = self
        dataTable.backgroundColor = UIColor.white
        dataTable.dataSource = self
        dataTable.tableHeaderView?.isHidden = true;
        dataTable.tableFooterView?.isHidden = true;
        dataTable.tableHeaderView = UIView(frame: CGRect(x: 0, y: 0, width: KSize.width, height: 0.01))
        dataTable.tableFooterView = UIView(frame: CGRect(x: 0, y: 0, width: KSize.width, height: 0.01))
        dataTable.estimatedSectionHeaderHeight = 0
        dataTable.estimatedSectionFooterHeight = 0
        dataTable.estimatedRowHeight = 0
        if #available(iOS 15.0, *){
            dataTable.sectionHeaderTopPadding = 0
        }
        self.view.addSubview(dataTable)
    }
    
    //MARK:table代理
    
    //段数
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    
    
    
    func tableView(_ tableView: UITableView, estimatedHeightForRowAt indexPath: IndexPath) -> CGFloat {
        let deviceInfo = deviceInfoArray[indexPath.row]
        if deviceInfo["deviceType"] == nil{
            return 230
        }
        let deviceType = deviceInfo["deviceType"] as! String ?? ""
        if Utils.isDebug{
            if deviceType == "S02"{
                return 460
            }else if deviceType == "errorType"{
                return 230
            }else if deviceType == "tire"{
                return 310
            }else if deviceType == "S07"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 360
                }else if broadcastType == "Long range"{
                    return 420
                }else if broadcastType == "Beacon"{
                    return 360
                }
                return 420
            }else if deviceType == "S08"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 390
                } else if broadcastType == "Beacon"{
                    return 300
                }else{
                    let move = deviceInfo["move"]
                    let moveStatus = deviceInfo["moveStatus"]
                    if move != "-"{
                        if moveStatus != "2"{
                            return 630
                        }else{
                            return 510
                        }
                    }else{
                        return 480
                    }
                }
                
            }else if deviceType == "S09"{
                return 270
            }else if deviceType == "S10"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 360
                }else if broadcastType == "Long range"{
                    return 450
                }else if broadcastType == "Beacon"{
                    return 360
                }
                return 480
            }else{
                return 430
            }
        }else{
            if deviceType == "S02"{
                return 420
            }else if deviceType == "errorType"{
                return 230
            }else if deviceType == "tire"{
                return 310
            }else if deviceType == "S07"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 360
                }else if broadcastType == "Long range"{
                    return 420
                }else if broadcastType == "Beacon"{
                    return 360
                }
                return 420
            }else if deviceType == "S08"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 390
                } else if broadcastType == "Beacon"{
                    return 300
                }else{
                    let move = deviceInfo["move"]
                    let moveStatus = deviceInfo["moveStatus"]
                    if move != "-"{
                        if moveStatus != "2"{
                            return 630
                        }else{
                            return 510
                        }
                    }else{
                        return 480
                    }
                }
                
            }else if deviceType == "S09"{
                return 270
            }else if deviceType == "S10"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 360
                }else if broadcastType == "Long range"{
                    return 450
                }else if broadcastType == "Beacon"{
                    return 360
                }
                return 480
            }else{
                return 390
            }
        }
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let deviceInfo = deviceInfoArray[indexPath.row]
        if deviceInfo["deviceType"] == nil{
            return 230
        }
        let deviceType = deviceInfo["deviceType"] as! String ?? ""
        if Utils.isDebug{
            if deviceType == "S02"{
                return 460
            }else if deviceType == "errorType"{
                return 230
            }else if deviceType == "tire"{
                return 310
            }else if deviceType == "S07"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 360
                }else if broadcastType == "Long range"{
                    return 420
                }else if broadcastType == "Beacon"{
                    return 360
                }
                return 420
            }else if deviceType == "S08"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 390
                } else if broadcastType == "Beacon"{
                    return 300
                }else{
                    let move = deviceInfo["move"]
                    let moveStatus = deviceInfo["moveStatus"]
                    if move != "-"{
                        if moveStatus != "2"{
                            return 630
                        }else{
                            return 510
                        }
                    }else{
                        return 480
                    }
                }
                
            }else if deviceType == "S09"{
                return 270
            }else if deviceType == "S10"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 360
                }else if broadcastType == "Long range"{
                    return 450
                }else if broadcastType == "Beacon"{
                    return 360
                }
                return 480
            }else{
                return 430
            }
        }else{
            if deviceType == "S02"{
                return 420
            }else if deviceType == "errorType"{
                return 230
            }else if deviceType == "tire"{
                return 310
            }else if deviceType == "S07"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 360
                }else if broadcastType == "Long range"{
                    return 420
                }else if broadcastType == "Beacon"{
                    return 360
                }
                return 420
            }else if deviceType == "S08"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 390
                } else if broadcastType == "Beacon"{
                    return 300
                }else{
                    let move = deviceInfo["move"]
                    let moveStatus = deviceInfo["moveStatus"]
                    if move != "-"{
                        if moveStatus != "2"{
                            return 630
                        }else{
                            return 510
                        }
                    }else{
                        return 480
                    }
                }
                
            }else if deviceType == "S09"{
                return 270
            }else if deviceType == "S10"{
                let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
                if broadcastType == "Eddystone UID"{
                    return 360
                }else if broadcastType == "Long range"{
                    return 450
                }else if broadcastType == "Beacon"{
                    return 360
                }
                return 480
            }else{
                return 390
            }
        }
    }
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 0.01
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 0.01
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return deviceInfoArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let deviceInfo = deviceInfoArray[indexPath.row]
        if deviceInfo["deviceType"] == nil{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceErrorCell.identifier, for: indexPath)) as! DeviceErrorCell
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            cell.hardwareContentLabel.text = deviceInfo["hardware"]
            cell.softwareContentLabel.text = deviceInfo["software"]
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            return cell
        }
        let deviceType = deviceInfo["deviceType"] as! String ?? ""
        if deviceType == "S02"{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceS02Cell.identifier, for: indexPath)) as! DeviceS02Cell
            cell.resetPosition()
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            //        print("cell table " + (deviceInfo["date"] ?? ""))
            cell.batteryContentLabel.text = deviceInfo["battery"]
            cell.hardwareContentLabel.text = deviceInfo["hardware"]
            cell.softwareContentLabel.text = deviceInfo["software"]
            cell.humidityContentLabel.text = deviceInfo["humidity"]
            cell.lightContentLabel.text = deviceInfo["light"]
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            cell.rssiContentLabel.text = deviceInfo["rssi"]
            let temp = Float(deviceInfo["sourceTemp"] ?? "0") ?? 0
            let curTemp = Utils.getCurTemp(sourceTemp: temp)
            let tempStr = String.init(format: "%.2f %@", curTemp,Utils.getCurTempUnit())
            cell.tempContentLabel.text = tempStr
            cell.warnContentLabel.text = deviceInfo["warn"]
            cell.configBtn.tag = indexPath.row
            cell.qrCodeBtn.tag = indexPath.row
            cell.switchTempUnitBtn.setTitle(Utils.getNextTempUnit(), for: .normal)
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(tap(tap:)))
            cell.configBtn.addGestureRecognizer(tapGesture)
            let qrCodeTap = UITapGestureRecognizer(target: self, action: #selector(qrCodeTap(tap:)))
            cell.qrCodeBtn.addGestureRecognizer(qrCodeTap)
            cell.switchTempUnitBtn.tag = indexPath.row
            let switchTempUnitTap = UITapGestureRecognizer(target: self, action: #selector(switchTempUnitTap(tap:)))
            cell.switchTempUnitBtn.addGestureRecognizer(switchTempUnitTap)
            return cell
        }else if deviceType == "S04"{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceS04Cell.identifier, for: indexPath)) as! DeviceS04Cell
            
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            //        print("cell table " + (deviceInfo["date"] ?? ""))
            cell.batteryContentLabel.text = deviceInfo["battery"]
            cell.hardwareContentLabel.text = deviceInfo["hardware"]
            cell.softwareContentLabel.text = deviceInfo["software"]
            cell.doorContentLabel.text = deviceInfo["doorSensor"]
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            cell.rssiContentLabel.text = deviceInfo["rssi"]
            let temp = Float(deviceInfo["sourceTemp"] ?? "0") ?? 0
            let curTemp = Utils.getCurTemp(sourceTemp: temp)
            let tempStr = String.init(format: "%.2f %@", curTemp,Utils.getCurTempUnit())
            cell.tempContentLabel.text = tempStr
            cell.warnContentLabel.text = deviceInfo["warn"]
            
            cell.configBtn.tag = indexPath.row
            cell.qrCodeBtn.tag = indexPath.row
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(tap(tap:)))
            cell.configBtn.addGestureRecognizer(tapGesture)
            let qrCodeTap = UITapGestureRecognizer(target: self, action: #selector(qrCodeTap(tap:)))
            cell.qrCodeBtn.addGestureRecognizer(qrCodeTap)
            cell.switchTempUnitBtn.tag = indexPath.row
            cell.switchTempUnitBtn.setTitle(Utils.getNextTempUnit(), for: .normal)
            let switchTempUnitTap = UITapGestureRecognizer(target: self, action: #selector(switchTempUnitTap(tap:)))
            cell.switchTempUnitBtn.addGestureRecognizer(switchTempUnitTap)
            return cell
        }else if deviceType == "errorType"{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceErrorCell.identifier, for: indexPath)) as! DeviceErrorCell
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            cell.hardwareContentLabel.text = deviceInfo["hardware"]
            cell.softwareContentLabel.text = deviceInfo["software"]
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            return cell
        }else if deviceType == "tire"{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceTireCell.identifier, for: indexPath)) as! DeviceTireCell
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            //        print("cell table " + (deviceInfo["date"] ?? ""))
            cell.batteryContentLabel.text = deviceInfo["battery"]
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            cell.rssiContentLabel.text = deviceInfo["rssi"]
            cell.statusContentLabel.text = deviceInfo["status"]
            let temp = Float(deviceInfo["sourceTemp"] ?? "0") ?? 0
            let curTemp = Utils.getCurTemp(sourceTemp: temp)
            let tempStr = String.init(format: "%.2f %@", curTemp,Utils.getCurTempUnit())
            cell.tempContentLabel.text = tempStr
            cell.switchTempUnitBtn.setTitle(Utils.getNextTempUnit(), for: .normal)
            cell.switchTempUnitBtn.tag = indexPath.row
            let switchTempUnitTap = UITapGestureRecognizer(target: self, action: #selector(switchTempUnitTap(tap:)))
            cell.switchTempUnitBtn.addGestureRecognizer(switchTempUnitTap)
            let tirePressure = Float(deviceInfo["tirePressure"] ?? "0") ?? 0
            let curTirePressure = Utils.getCurPressure(sourcePressure: tirePressure)
            let tirePressureStr = String.init(format: "%.2f %@", curTirePressure,Utils.getCurPressureUnit())
            cell.pressureContentLabel.text = tirePressureStr
            cell.switchPressureUnitBtn.setTitle(Utils.getNextPressureUnit(), for: .normal)
            cell.switchPressureUnitBtn.tag = indexPath.row
            let switchPressureUnitTap = UITapGestureRecognizer(target: self, action: #selector(switchPressureUnitTap(tap:)))
            cell.switchPressureUnitBtn.addGestureRecognizer(switchPressureUnitTap)
            return cell
        }else if deviceType == "S07"{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceS07Cell.identifier, for: indexPath)) as! DeviceS07Cell
            let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
            cell.resetPosition(broadcastType: broadcastType)
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            //        print("cell table " + (deviceInfo["date"] ?? ""))
            cell.batteryContentLabel.text = deviceInfo["battery"]
            cell.hardwareContentLabel.text = deviceInfo["hardware"]
            cell.softwareContentLabel.text = deviceInfo["software"]
            cell.batteryPercentContentLabel.text = deviceInfo["batteryPercent"]
            cell.flagContentLabel.text = deviceInfo["flag"]
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            cell.rssiContentLabel.text = deviceInfo["rssi"]
            cell.broadcastTypeContentLabel.text = deviceInfo["broadcastType"]
            cell.nidContentLabel.text = deviceInfo["nid"]
            cell.bidContentLabel.text = deviceInfo["bid"]
            cell.majorContentLabel.text = deviceInfo["major"]
            cell.minorContentLabel.text = deviceInfo["minor"]
            cell.warnContentLabel.text = deviceInfo["warn"]
            cell.configBtn.tag = indexPath.row
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(tap(tap:)))
            cell.configBtn.addGestureRecognizer(tapGesture)
            
            return cell
        }else if deviceType == "S08"{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceS08Cell.identifier, for: indexPath)) as! DeviceS08Cell
            let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
            cell.resetPosition(bleDeviceInfo: deviceInfo)
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            //        print("cell table " + (deviceInfo["date"] ?? ""))
            cell.batteryContentLabel.text = deviceInfo["battery"]
            cell.hardwareContentLabel.text = deviceInfo["hardware"]
            cell.softwareContentLabel.text = deviceInfo["software"]
            let temp = Float(deviceInfo["sourceTemp"] ?? "0") ?? 0
            let curTemp = Utils.getCurTemp(sourceTemp: temp)
            let tempStr = String.init(format: "%.2f %@", Float(curTemp) / 100.0,Utils.getCurTempUnit())
            cell.tempContentLabel.text = tempStr
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            cell.rssiContentLabel.text = deviceInfo["rssi"]
            cell.broadcastTypeContentLabel.text = deviceInfo["broadcastType"]
            cell.nidContentLabel.text = deviceInfo["nid"]
            cell.bidContentLabel.text = deviceInfo["bid"]
            cell.majorContentLabel.text = deviceInfo["major"]
            cell.minorContentLabel.text = deviceInfo["minor"]
            cell.moveContentLabel.text = deviceInfo["move"]
            cell.moveDetectionContentLabel.text = deviceInfo["moveDetection"]
            cell.stopDetectionContentLabel.text = deviceInfo["stopDetection"]
            cell.pitchContentLabel.text = deviceInfo["pitchAngle"]
            cell.rollContentLabel.text = deviceInfo["rollAngle"]
            cell.doorContentLabel.text = deviceInfo["doorStatus"]
            cell.ambientLightContentLabel.text = deviceInfo["deviceProp"]
            cell.batteryPercentContentLabel.text = deviceInfo["batteryPercent"]
            cell.warnContentLabel.text = deviceInfo["warn"]
            cell.configBtn.tag = indexPath.row
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(tap(tap:)))
            cell.configBtn.addGestureRecognizer(tapGesture)
            cell.switchTempUnitBtn.tag = indexPath.row
            cell.switchTempUnitBtn.setTitle(Utils.getNextTempUnit(), for: .normal)
            let switchTempUnitTap = UITapGestureRecognizer(target: self, action: #selector(switchTempUnitTap(tap:)))
            cell.switchTempUnitBtn.addGestureRecognizer(switchTempUnitTap)
            return cell
        }else if deviceType == "S09"{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceS09Cell.identifier, for: indexPath)) as! DeviceS09Cell
            let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
            cell.resetPosition(broadcastType: broadcastType)
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            //        print("cell table " + (deviceInfo["date"] ?? ""))
            cell.hardwareContentLabel.text = deviceInfo["hardware"]
            cell.softwareContentLabel.text = deviceInfo["software"]
            
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            cell.rssiContentLabel.text = deviceInfo["rssi"]
            cell.configBtn.tag = indexPath.row
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(tap(tap:)))
            cell.configBtn.addGestureRecognizer(tapGesture)
            
            return cell
        }else if deviceType == "S10"{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceS10Cell.identifier, for: indexPath)) as! DeviceS10Cell
            let broadcastType = deviceInfo["broadcastType"] as! String ?? ""
            cell.resetPosition(bleDeviceInfo: deviceInfo)
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            //        print("cell table " + (deviceInfo["date"] ?? ""))
            cell.batteryContentLabel.text = deviceInfo["battery"]
            cell.hardwareContentLabel.text = deviceInfo["hardware"]
            cell.softwareContentLabel.text = deviceInfo["software"]
            let temp = Float(deviceInfo["sourceTemp"] ?? "0") ?? 0
            let curTemp = Utils.getCurTemp(sourceTemp: temp)
            let tempStr = String.init(format: "%.2f %@", Float(curTemp) / 100.0,Utils.getCurTempUnit())
            cell.tempContentLabel.text = tempStr
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            cell.rssiContentLabel.text = deviceInfo["rssi"]
            cell.broadcastTypeContentLabel.text = deviceInfo["broadcastType"]
            cell.nidContentLabel.text = deviceInfo["nid"]
            cell.bidContentLabel.text = deviceInfo["bid"]
            cell.majorContentLabel.text = deviceInfo["major"]
            cell.minorContentLabel.text = deviceInfo["minor"]
            cell.batteryPercentContentLabel.text = deviceInfo["batteryPercent"]
            cell.ambientLightContentLabel.text = deviceInfo["ambientLight"]
            cell.warnContentLabel.text = deviceInfo["warn"]
            cell.humidityContentLabel.text = deviceInfo["humidity"]
            cell.configBtn.tag = indexPath.row
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(tap(tap:)))
            cell.configBtn.addGestureRecognizer(tapGesture)
            cell.switchTempUnitBtn.tag = indexPath.row
            cell.switchTempUnitBtn.setTitle(Utils.getNextTempUnit(), for: .normal)
            let switchTempUnitTap = UITapGestureRecognizer(target: self, action: #selector(switchTempUnitTap(tap:)))
            cell.switchTempUnitBtn.addGestureRecognizer(switchTempUnitTap)
            return cell
        }else{
            let cell = (tableView.dequeueReusableCell(withIdentifier: DeviceS05Cell.identifier, for: indexPath)) as! DeviceS05Cell
            
            cell.deviceNameContentLabel.text = deviceInfo["name"]
            cell.idContentLabel.text = deviceInfo["id"]
            cell.dateContentLabel.text = deviceInfo["date"]
            //        print("cell table " + (deviceInfo["date"] ?? ""))
            cell.batteryContentLabel.text = deviceInfo["battery"]
            cell.hardwareContentLabel.text = deviceInfo["hardware"]
            cell.softwareContentLabel.text = deviceInfo["software"]
            cell.relayContentLabel.text = deviceInfo["relayStatus"]
            cell.modelContentLabel.text = deviceInfo["deviceTypeDesc"]
            cell.rssiContentLabel.text = deviceInfo["rssi"]
            let temp = Float(deviceInfo["sourceTemp"] ?? "0") ?? 0
            let curTemp = Utils.getCurTemp(sourceTemp: temp)
            let tempStr = String.init(format: "%.2f %@", curTemp,Utils.getCurTempUnit())
            cell.tempContentLabel.text = tempStr
            cell.warnContentLabel.text = deviceInfo["warn"]
            cell.configBtn.tag = indexPath.row
            let tapGesture = UITapGestureRecognizer(target: self, action: #selector(tap(tap:)))
            cell.configBtn.addGestureRecognizer(tapGesture)
            cell.qrCodeBtn.tag = indexPath.row
            let qrCodeTap = UITapGestureRecognizer(target: self, action: #selector(qrCodeTap(tap:)))
            cell.qrCodeBtn.addGestureRecognizer(qrCodeTap)
            cell.switchTempUnitBtn.tag = indexPath.row
            cell.switchTempUnitBtn.setTitle(Utils.getNextTempUnit(), for: .normal)
            let switchTempUnitTap = UITapGestureRecognizer(target: self, action: #selector(switchTempUnitTap(tap:)))
            cell.switchTempUnitBtn.addGestureRecognizer(switchTempUnitTap)
            return cell
        }
        
    }
    @objc func switchTempUnitTap(tap: UITapGestureRecognizer) {
        print("switchTempUnitTap click tap")
        Utils.switchCurTempUnit()
        print(Utils.getCurTempUnit())
        dataTable.reloadData()
    }
    
    @objc func switchPressureUnitTap(tap: UITapGestureRecognizer) {
        print("switchPressureUnitTap click tap")
        Utils.switchCurPressureUnit()
        print(Utils.getCurPressureUnit())
        dataTable.reloadData()
    }
    @objc func qrCodeTap(tap: UITapGestureRecognizer) {
        print("qrCodeTap click tap")
        let index = tap.view?.tag
        if index != nil {
            let deviceInfo = deviceInfoArray[index ?? 0]
            let idStr = deviceInfo["id"]!
            if idStr != nil{
                let view = AEAlertView(style: .defaulted)
                
                let action_one = AEAlertAction(title: "Cancel", style: .cancel) { (action) in
                    view.dismiss()
                }
                let action_two = AEAlertAction(title: "Confirm", style: .defaulted) { (action) in
                    view.dismiss()
                }
                view.titleTopMargin = 0
                view.messageTopMargin = 0
                view.messageHeight = 0
                view.animationViewTopMargin = 0
                var qrImgView = UIImageView(frame: CGRect(x: 0, y: 0, width: 50, height: 50))
                let qrImg = LBXScanWrapper.createCode(codeType: "CIQRCodeGenerator", codeString: idStr, size: qrImgView.bounds.size, qrColor: UIColor.black, bkColor: UIColor.white)
                let logoImg = UIImage(named: "logo.png")
                qrImgView.image = self.getQRImageWithString(value: idStr)//LBXScanWrapper.addImageLogo(srcImg: qrImg!, logoImg: logoImg!, logoSize: CGSize(width: 30, height: 30))
                qrImgView.contentMode = .scaleAspectFill
                qrImgView.layer.masksToBounds = true
                view.set(animation: qrImgView, width: 50, height: 50)
                
                view.addAction(action: action_one)
                view.addAction(action: action_two)
                view.show()
            }
            
        }
        
    }
    
    func getQRImageWithString(value:String) ->UIImage{
        let qrFilter = CIFilter(name: "CIQRCodeGenerator")
        let stringData = value.data(using: .utf8)
        qrFilter?.setValue(stringData, forKey: "inputMessage")
        qrFilter?.setValue("H", forKey: "inputCorrectionLevel")
        return UIImage.init(ciImage: (qrFilter?.outputImage)!)
    }
    
    @objc func tap(tap: UITapGestureRecognizer) {
        print("config click tap")
        let index = tap.view?.tag
        if index != nil {
            let deviceInfo = deviceInfoArray[index ?? 0]
            let macStr = deviceInfo["mac"]
            print(macStr)
            for item in self.discoveredPeripherals{
                if item.mac == macStr {
                    let peripheral = item.peripheral
                    self.centralManager.stopScan()
                    let editView = EditConfigController()
                    editView.cbPeripheral = peripheral
                    editView.mac = deviceInfo["id"] ?? ""
                    editView.software = deviceInfo["softwareInt"] ?? ""
                    editView.deviceType = deviceInfo["deviceType"] ?? ""
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

