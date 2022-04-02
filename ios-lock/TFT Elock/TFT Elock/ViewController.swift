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
    private var fuzzyKey = ""
    private var bleDeviceInfoArray = [BleDeviceData]()
    private var allBleDeviceInfoArray = [BleDeviceData]()
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
            let key2 = CBUUID(string:"DEAF")
            
            if dict[key1] != nil && dict[key2] != nil{
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
                var imeiData:[UInt8]? = nil,versionData:[UInt8]? = nil
                if dict[key1] != nil{
                    let data = dict[key1] as! Data
                    imeiData = [UInt8](data)
                }
                if dict[key2] != nil{
                    let data = dict[key2] as! Data
                    versionData = [UInt8](data)
                    
                }
                self.parseBleData(deviceName: name, imeiData: imeiData!,versionData: versionData!, rssi: rssi, mac: mac)
                
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
            let softwareInt = Int(softwareStr)!
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
    func parseBleData(deviceName:String,imeiData:[UInt8],versionData:[UInt8],rssi:Int,mac:String){
        if imeiData != nil || versionData != nil {
            let versionInfo = versionData
            let imeiInfo = imeiData
            var imei:String = ""
            if imeiInfo != nil{
                var imeiByte = Utils.arraysCopyOfRange(src: imeiInfo, from: 1, to: imeiInfo.count)
                imei = String(bytes:imeiByte,encoding:.utf8)!
            }
            var software:String = "",hardware:String = "",protocolType:String = "",model:String = ""
            var protocolByte:UInt8 = 0x00
            var voltage:Float = 0.0
            if versionInfo != nil && versionInfo.count >= 5{
                protocolByte = versionInfo[0]
                let versionByte = Utils.arraysCopyOfRange(src: versionInfo, from: 1, to: versionInfo.count)
                var versionStr = Utils.uint8ArrayToHexStr(value: versionByte)
                var hardwarePart1 = versionStr.subStr(startIndex: 0, endIndex: 1)
                var hardwarePart2 = versionStr.subStr(startIndex: 1, endIndex: 2)
                hardware = String.init(format: "V%@.%@", hardwarePart1,hardwarePart2)
                let softwareStr = versionStr.subStr(startIndex: 2, endIndex: 6)
                let softwareInt = Int(softwareStr)!
               software = String.init(format: "V%d", softwareInt)
               model = BleDeviceData.parseModel(protocolByte: protocolByte)
               var voltage1 = versionStr.subStr(startIndex: 6, endIndex: 7)
               var voltage2 = versionStr.subStr(startIndex: 7, endIndex: 8)
                let voltageStr = String.init(format: "%@.%@", voltage1,voltage2)
               voltage = Float(voltageStr)!
            }
            if protocolByte != 0x62{
                return;
            }
            var bleDeviceData = BleDeviceData()
            bleDeviceData.hardware = hardware
            bleDeviceData.date = currentDateString()
            bleDeviceData.imei = imei
            bleDeviceData.software = software
            bleDeviceData.model = model
            bleDeviceData.mac = mac
            bleDeviceData.voltage = voltage
            bleDeviceData.deviceName = deviceName
            bleDeviceData.rssi = String.init(format: "%d", rssi)
            var exist = false
            var i = 0
            while i < allBleDeviceInfoArray.count{
                let bleDeviceInfo = allBleDeviceInfoArray[i]
                let macStr = bleDeviceInfo.mac
                if mac == macStr{
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
                    bleDeviceInfoArray[i] = bleDeviceData
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
                    let macRange = mac.range(of:fuzzyKey)
                    var macLocation = -1
                    if macRange != nil{
                        macLocation = mac.distance(from: mac.startIndex, to: macRange!.lowerBound)
                    }
                    if deviceNameLocation != -1 || macLocation != -1{
                        bleDeviceInfoArray.append(bleDeviceData)
                    }
                }else{
                    bleDeviceInfoArray.append(bleDeviceData)
                }
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
    var discoveredPeripherals:[(peripheral: CBPeripheral, mac: String?)] = []
    
    
    @objc private func leftClick() {
        print("leftClick")
        self.bleDeviceInfoArray.removeAll()
        self.allBleDeviceInfoArray.removeAll()
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
        bleDeviceInfoArray.removeAll()
        while i < allBleDeviceInfoArray.count{
            let bleDeviceData = allBleDeviceInfoArray[i]
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
                bleDeviceInfoArray.append(bleDeviceData)
            }
            i+=1
        }
        dataTable.reloadData()
        if fuzzyKey.count == 15{
            i=0
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
                            let editView = EditController()
                            editView.cbPeripheral = peripheral
                            editView.name = bleDeviceData.deviceName
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
    
    override func viewDidLoad() {
        print("viewDidLoad")
        super.viewDidLoad()
        self.navigationController!.navigationBar.isTranslucent = false
        self.extendedLayoutIncludesOpaqueBars = true
        self.view.backgroundColor = UIColor.white
        self.view.autoresizingMask = UIView.AutoresizingMask.flexibleWidth
        makeTable()
        dataTable.autoresizingMask = UIView.AutoresizingMask.flexibleWidth
        dataTable.register(BleDetailItem.self,forCellReuseIdentifier:BleDetailItem.identifier)
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
//        titleLabel.text = "Bluetooth sensor"
        titleLabel.text = NSLocalizedString("main_bar_text", comment: "Bluetooth sensor")
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
        return 270
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 270
    }
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 0.01
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 0.01
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
        cell.imeiContentLabel.text = bleDeviceData.imei
        cell.modelContentLabel.text = bleDeviceData.model
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(tap(tap:)))
        cell.configBtn.addGestureRecognizer(tapGesture)
        cell.configBtn.tag = indexPath.row
        return cell
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

