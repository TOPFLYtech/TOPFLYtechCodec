//
//  SuperPwdResetController.swift
//  tftble
//
//  Created by china topflytech on 2024/5/21.
//  Copyright © 2024 com.tftiot. All rights reserved.
//
 

import UIKit
import iOSDFULibrary
import CoreBluetooth
import CLXToast
import QMUIKit
import ActionSheetPicker_3_0
 
class SuperPwdResetController:UIViewController,CBCentralManagerDelegate,CBPeripheralDelegate,  LoggerDelegate{
    func dfuStateDidChange(to state: DFUState) {
        print("dfuStateDidChange")
        print(state)
        switch state {
        case .completed:
            self.progressView.message = NSLocalizedString("upgrade_success", comment: "Upgrade Success")
            DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(5)) {
                self.progressView.dismiss()
                self.reConnectAndWaiting()
            }
            break
        case .aborted:
            self.progressView.message = NSLocalizedString("upgrade_aborted", comment: "Upgrade aborted")
            DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(5)) {
                self.progressView.dismiss()
                self.reConnectAndWaiting()
            }
            break
        case .connecting:
            self.progressView.message = NSLocalizedString("connecting_str", comment: "Connecting,please wait")
            break
        case .starting:
            self.progressView.message = NSLocalizedString("start_upgrading", comment: "Start upgrading")
            break
        default:
            
            break
        }
    }
    
    func dfuError(_ error: DFUError, didOccurWithMessage message: String) {
        print("dfuError")
        print(error)
        print(message)
        self.progressView.message = "Error \(error.rawValue): \(message)"
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(5)) {
            self.progressView.dismiss()
            self.reConnectAndWaiting()
        }
    }
    
    func dfuProgressDidChange(for part: Int, outOf totalParts: Int, to progress: Int, currentSpeedBytesPerSecond: Double, avgSpeedBytesPerSecond: Double) {
        print("dfuProgressDidChange")
        print(part)
        print(progress)
        self.progressBar.setValue(Float(progress),animated: true)
    }
    
    func logWith(_ level: LogLevel, message: String) {
        print("logWith")
        print(message)
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
 
    private var upgradePackUrl:String!
    private let Service_UUID = CBUUID(string:"27760001-999C-4D6A-9FC4-C7272BE10900")
    private let Characteristic_UUID = CBUUID(string:"27763561-999C-4D6A-9FC4-C7272BE10900")
    var centralManager   : CBCentralManager!
    var cbPeripheral:CBPeripheral!
    var mac = ""
    var deviceType = ""
    private var leaveViewNeedDisconnect = true
    private var onView = false
    private var confirmPwd = ""
    private var initStart = false
    private var connected = false
    private var needConnect = false
    private var isUpgrade = false
    private var foundDevice = false
    private var connectControl = ""
    private var progressView:AEAlertView!
    private var progressBar:MyProgress!
    
    private let controlFunc:[String:[String:Int]] = [
        "firmware": [ "read": 1 ],
        "saveCount": [ "read": 2 ],
        "readHistory": [ "read": 3, "len": 8 ],
        "readAlarm":["read":5,"len":8],
        "readNextHistory":["read":6,"len":4],
        "readNextAlarm":["read":7,"len":4],
        "readOriginData":["read":8,"len":1],
        "password": [ "write": 40, "len": 6 ],
        "resetFactory": [ "write": 41, "len": 0 ],
        "startRecord": [ "write": 42, "len": 0 ],
        "stopRecord": [ "write": 43, "len": 0 ],
        "clearRecord": [ "write": 44, "len": 0 ],
        "pattern": [ "read": 81, "write": 80, "len": 0, "min": 1, "max": 3 ],
        "broadcastInterval": [ "read": 83, "write": 82, "len": 2, "min": 20, "max": 2000 ],
        "broadcastCycle": [ "read": 85, "write": 84, "len": 2, "min": 5, "max": 1800 ],
        "broadcastDuration": [ "read": 87, "write": 86, "len": 2 ],
        "saveInterval": [ "read": 89, "write": 88, "len": 2 ],
        "transmittedPower": [ "read": 91, "write": 90, "len": 1 ],
        "saveCover": [ "read": 93, "write": 92, "len": 1 ],
        "tempAlarm": [ "read": 95, "write": 94, "len": 4 ],
        "humidityAlarm": [ "read": 97, "write": 96, "len": 4 ],
        "lumen": [ "read": 99, "write": 98, "len": 1 ],
        "time": [ "read": 101, "write": 100, "len": 4 ],
        "deviceName": [ "read": 103, "write": 102, "len": 8, ],
        "ledOpen": [ "read": 107, "write": 106 ],
        "relay": [ "read": 109, "write": 108 ],
        "lightSensorOpen": [ "read": 111, "write": 110 ],
        "readDinVoltage": [ "read": 0x91  ],
        "dinStatusEvent": [ "read": 0x93, "write": 0x92 ],
        "readDinStatusEventType": [ "read": 0x94  ],
        "doutStatus": [ "read": 0x96, "write": 0x95 ],
        "readAinVoltage": [ "read": 0x97  ],
        "setPositiveNegativeWarning": [ "read": 0x99, "write": 0x98 ],
        "getOneWireDevice": [ "read": 0x9B  ],
        "sendCmdSequence": [ "write": 0x9C, "len": 200 ],
        "sequential": [ "read": 0x9E, "write": 0x9D ],
        "oneWireWorkMode": [ "read": 0xA0, "write": 0x9F ],
        "rs485SendData": [ "read": 0xA2, "write": 0xA1,"len":200 ],
        "rs485BaudRate": [ "read": 0xA4, "write": 0xA3 ],
        "rs485Enable": [ "read": 0xA6, "write": 0xA5 ],
        "longRangeEnable": [ "read": 0x32, "write": 0x31 ],
        "broadcastType": [ "read": 0x83, "write": 0x82 ],
        "gSensorEnable": [ "read": 0x85, "write": 0x84 ],
        "shutdown":[ "write":0x86, ],
        "readVinVoltage":[ "read":0x30, ],
        "doorEnable":[ "read":0x88, "write":0x87, ],
        "gSensorSensitivity":[ "read":0x8a, "write":0x89, ],
        "gSensorDetectionDuration":[ "read":0x8c, "write":0x8b, ],
        "gSensorDetectionInterval":[ "read":0x8e, "write":0x8d, ],
        "beaconMajorSet":[ "read":0x78, "write":0x77, ],
        "beaconMinorSet":[ "read":0x7a, "write":0x79, ],
        "eddystoneNIDSet":[ "read":0x7c, "write":0x7b, ],
        "eddystoneBIDSet":[ "read":0x7e, "write":0x7d, ],
        "readExtSensorType": [ "read": 9 ],
        "btnTriggerTime":[ "read":0xc1, "write":0xc0, ],
    ]
    private var fontSize:CGFloat = Utils.fontSize
    private var waitingView:AEUIAlertView!
    private var pwdAlert:AEUIAlertView!
    
    
    private var selfPeripheral:CBPeripheral!
    private var characteristic: CBCharacteristic?
 
 
    private var resetFactoryLabel:UILabel!
    private var editResetFactoryBtn:QMUIGhostButton!
 
    private var pwdErrorWarning = false;
 
    override func viewDidDisappear(_ animated: Bool) {
        self.needConnect = false
        if self.leaveViewNeedDisconnect{
            if self.selfPeripheral != nil{
                self.centralManager?.cancelPeripheralConnection(self.selfPeripheral)
            }
        }
        self.onView = false
        self.pwdErrorWarning = false
    }
    
    func initRightBtn(isConnect:Bool){
        if isConnect{
            let rightBarButtonItem = UIBarButtonItem (barButtonSystemItem: UIBarButtonItem.SystemItem.refresh, target: self, action: #selector(self.refreshClick))
            self.navigationItem.rightBarButtonItem = rightBarButtonItem
        }else{
            let btn = UIButton.init(frame: CGRect.init(x: KSize.width - 50, y: 0, width: 25, height: 25))
            btn.setImage(UIImage(named:"ic_disconnect.png"), for: .normal)
            let rightBarButtonItem =  UIBarButtonItem.init(customView: btn)
            btn.addTarget(self, action: #selector(self.refreshClick), for: .touchUpInside)
            self.navigationItem.rightBarButtonItem = rightBarButtonItem
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        self.onView = true
        self.pwdErrorWarning = false
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.onView = true
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = mac
         
        self.navigationItem.titleView = titleLabel
        self.initRightBtn(isConnect:false)
        self.view.backgroundColor = UIColor.white
        self.needConnect = true
        let mac = self.cbPeripheral.identifier.uuidString
        print(mac)
        //        Toast.waitingBuilder.prompt("Connectting").show()
        self.initUI()
        self.showWaitingWin(title: NSLocalizedString("connecting", comment: "Connecting"),isShowCancel: true,isCancelDoClose: true)
 
    }
    
    @objc private func refreshClick() {
        print ("refresh click")
        self.initStart = false
        if self.connected == true{
            self.centralManager.cancelPeripheralConnection(self.selfPeripheral)
        }
        if self.selfPeripheral != nil{
            self.showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"),isShowCancel: true,isCancelDoClose: true)
            self.centralManager.connect(self.selfPeripheral)
        }
        
        self.notUpdateInit()
    }
     
    
    func notUpdateInit(){
        if self.onView{
            //            self.showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
            if self.confirmPwd == ""{
                self.showPwdWin()
            }
        }
    }
    
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("edit Connected")
        peripheral.delegate = self
        self.centralManager.stopScan()
        peripheral.discoverServices([self.Service_UUID])
    }
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        
        if peripheral.identifier == self.cbPeripheral.identifier{
            self.selfPeripheral = peripheral
            self.foundDevice = true
            self.selfPeripheral.delegate = self
            if self.needConnect{
                self.centralManager.connect(self.selfPeripheral)
            }
            
        }
    }
    
    func reConnectAndWaiting(){
        self.initStart = false
        self.centralManager.connect(self.selfPeripheral)
        self.notUpdateInit()
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverIncludedServicesFor service: CBService, error: Error?) {
        print("didDiscoverIncludedServicesFor")
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        print("edit didDiscoverServices")
        //        print(peripheral.services)
        for service: CBService in peripheral.services! {
            //            print("外设中的服务有：\(service)")
            if service.uuid == self.Service_UUID {
                print("find service id")
                let myService = service
                peripheral.discoverCharacteristics([Characteristic_UUID], for: myService)
                break
            }
           
        }
        
    }
    
    /** 发现特征 */
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        print("didDiscoverCharacteristicsFor")
        //        print("外设中的特征有：\(service.characteristics!)")
        for characteristic: CBCharacteristic in service.characteristics! {
            print("外设中的特征有：\(characteristic)")
            if characteristic.uuid == self.Characteristic_UUID {
                print("外设中的特征有：\(characteristic)")
                self.characteristic = characteristic
                peripheral.readValue(for: self.characteristic!)
                // 订阅
                peripheral.setNotifyValue(true, for: self.characteristic!)
                break
            }
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        //        print("写入数据")
    }
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        print("didUpdateValueFor")
        if !self.connected{
            return
        }
        if self.waitingView != nil {
            self.waitingView.dismiss()
        }
        let data = characteristic.value
        if data != nil{
            let bytes = [UInt8](data!)
            print(bytes)
            if(bytes.count > 1){
                let status = bytes[0]
                let type = bytes[1]
                if status == 0{
                    if type == UInt8(self.controlFunc["password"]?["write"] ?? 0){
                        Toast.hudBuilder.title(NSLocalizedString("password_has_been_reset",comment:"Password has been reset to 654321!") ).show()
                        self.navigationController?.popViewController(animated: true)
                    }else if type == UInt8(self.controlFunc["resetFactory"]?["write"] ?? 0){
                        Toast.hudBuilder.title(NSLocalizedString("factory_reset_succ",comment:"Factory Settings restored successfully, please enter the password to reconnect") ).show()
                        self.navigationController?.popViewController(animated: true)
                    }
                     
                }else if(status == 1){
                    self.waitingView.dismiss()
                    if(!self.pwdErrorWarning){
                        self.pwdErrorWarning = true;
                        Toast.hudBuilder.title(NSLocalizedString("super_password_is_error", comment: "Super password is error")).show()
                        self.initStart = false
                        self.showPwdWin()
                    }
                }else{
                    self.waitingView.dismiss()
                    print("Error,please try again!\(type)")
                    Toast.hudBuilder.title(NSLocalizedString("error_need_try_warning", comment: "Error,please try again!")).show()
                }
            }
        }
        
    }
    
    func readData(cmdHead:Int){
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("need_reconnect_warning", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let uint8Array = self.getInterActiveCmd(pwd: self.confirmPwd, cmdHead: cmdHead , content: [])
        print(uint8Array)
        let data = Data(bytes:uint8Array,count: uint8Array.count)
        self.selfPeripheral.writeValue(data, for: self.characteristic!, type: CBCharacteristicWriteType.withResponse)
    }
    
  
       
    func writeStrData(cmdHead:Int,dataStr:String){
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("need_reconnect_warning", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let dataArray = [UInt8](dataStr.utf8)
        let uint8Array = self.getInterActiveCmd(pwd: self.confirmPwd, cmdHead: cmdHead , content: dataArray)
        //        print(uint8Array)
        let data = Data(bytes:uint8Array,count: uint8Array.count)
        self.selfPeripheral.writeValue(data, for: self.characteristic!, type: CBCharacteristicWriteType.withResponse)
    }
    
    func writeArrayData(cmdHead:Int,content:[UInt8],inPwd:String){
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("need_reconnect_warning", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let uint8Array = self.getInterActiveCmd(pwd: inPwd, cmdHead: cmdHead , content: content)
        let data = Data(bytes:uint8Array,count: uint8Array.count)
        print(uint8Array)
        self.selfPeripheral.writeValue(data, for: self.characteristic!, type: CBCharacteristicWriteType.withResponse)
    }
    
    func writeArrayData(cmdHead:Int,content:[UInt8]){
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("need_reconnect_warning", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let uint8Array = self.getInterActiveCmd(pwd: self.confirmPwd, cmdHead: cmdHead , content: content)
        let data = Data(bytes:uint8Array,count: uint8Array.count)
        print(uint8Array)
        self.selfPeripheral.writeValue(data, for: self.characteristic!, type: CBCharacteristicWriteType.withResponse)
    }
    
    func fixTime(){
        let now = Date()
        let data = self.formatHex(intValue: Int(now.timeIntervalSince1970), len: 4)
        self.writeArrayData(cmdHead: self.controlFunc["time"]?["write"] ?? 0, content: data,inPwd: "topfly")
    }
     
    
    
    func writePwd(pwd:String){
        self.writeStrData(cmdHead: self.controlFunc["password"]?["write"] ?? 0, dataStr: pwd)
    }
     
    
    
    func formatHex(intValue:Int,len:Int) ->[UInt8]{
        var result = [UInt8]()
        var remainValue = intValue
        var i = len
        //        if intValue < 0{
        //            remainValue = intValue + 128
        //        }
        while i > 0{
            let divValue = 1 << ((i - 1) * 8)
            if divValue == 1 && i != 1 {
                result.append(0)
            } else {
                let value = UInt8(remainValue / divValue)
                remainValue = remainValue % divValue
                result.append(value)
            }
            i-=1
        }
        return result
    }
    
    
    
    /** 订阅状态 */
    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            print("订阅失败: \(error)")
            self.initRightBtn(isConnect:false)
            return
        }
        if characteristic.isNotifying {
            print("订阅成功")
            self.initRightBtn(isConnect:true)
            self.connected = true
            if self.confirmPwd != ""{
                self.waitingView.dismiss()
                if self.initStart == false{
                    self.doInitSucc()
                }
            }else{
                notUpdateInit()
            }
            //            Toast.currentWaiting?.dismiss(animated: true)
            
        } else {
            print("取消订阅")
        }
    }
    
    func doInitSucc(){
        self.initStart = true
       
        self.fixTime()
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("连接失败")
        self.connected = false
        self.initRightBtn(isConnect:false)
    }
    
    /** 断开连接 */
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("断开连接")
        self.connected = false
        self.initRightBtn(isConnect:false)
        // 重新连接
        if self.needConnect{
            Toast.hudBuilder.title(NSLocalizedString("need_reconect_manaual_warning", comment: "The current Bluetooth connection has been disconnected. If you need to reconnect, please manually click the refresh button in the upper right corner.")).show()
            //            central.connect(peripheral, options: nil)
        }
        
        
    }
    
    func getInterActiveCmd(pwd:String,cmdHead:Int,content:[UInt8]) ->[UInt8]{
        var result = [UInt8]()
        for char in pwd.utf8{
            result.append(char)
        }
        result.append(UInt8(cmdHead))
        for char in content{
            result.append(char)
        }
        let crcLen = pwd.count + 1 + content.count
        let crcByte = self.calCrc(calArray: result, len: crcLen)
        result.append(crcByte)
        return result
    }
    
    func calCrc(calArray:[UInt8],len:Int) ->UInt8{
        var crc:UInt8!
        crc = 0xff
        var i = 0
        var j = 0
        while j < len{
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
    
  
    
    
    
    @objc private func editPassword() {
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("need_reconnect_warning", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_reset_password_warning", comment: "Try to reset password?")
        animV.textField.placeholder = NSLocalizedString("password", comment:"Password")
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            self.writePwd(pwd:"654321")
            animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
    }
    
    func writeResetFactory(){
        self.writeArrayData(cmdHead: self.controlFunc["resetFactory"]?["write"] ?? 0, content: [])
    }
    
    
    @objc private func resetFactoryClick() {
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("need_reconnect_warning", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("reset_factory", comment: "Are you sure to restore the factory settings?")
        animV.textField.placeholder = NSLocalizedString("password", comment:"Password")
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            self.writeResetFactory()
            animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
        
    }
    
     
    
    
    
    
    private var passwordLabel:UILabel!
    private var passwordContentLabel:UILabel!
    private var editPasswordBtn:QMUIGhostButton!
     
    var passwordView:UIView!
   
    var resetFactoryView:UIView!
    
    func initUI(){
        let scrollView = UIScrollView()
        scrollView.frame = self.view.bounds
        var scrollViewHeight:Float = 150
       
        //        scrollView.isPagingEnabled = true
        scrollView.showsHorizontalScrollIndicator = false
        scrollView.showsVerticalScrollIndicator = false
        scrollView.scrollsToTop = false
        self.view.addSubview(scrollView)
        let stackViewContainer = UIView()
        stackViewContainer.translatesAutoresizingMaskIntoConstraints = false
        
        scrollView.addSubview(stackViewContainer)
        stackViewContainer.isUserInteractionEnabled = true
        let stackView = UIStackView()
        stackView.axis = .vertical // 垂直布局
        stackView.alignment = .leading // 子视图居中对齐
        stackView.distribution = .fillEqually
        stackView.spacing = 0
        stackView.isMultipleTouchEnabled  = true
        // 添加 UIStackView 到父视图
        stackViewContainer.addSubview(stackView)
        scrollView.isUserInteractionEnabled = true
        stackView.isUserInteractionEnabled = true
        // 设置约束
        stackView.translatesAutoresizingMaskIntoConstraints = false
        //        stackView.leadingAnchor.constraint(equalTo: stackViewContainer.leadingAnchor, constant: 0).isActive = true
        //        stackView.trailingAnchor.constraint(equalTo: stackViewContainer.trailingAnchor, constant: 0).isActive = true
        //        stackView.topAnchor.constraint(equalTo: stackViewContainer.topAnchor, constant: 0).isActive = true
        //        stackView.bottomAnchor.constraint(equalTo: stackViewContainer.bottomAnchor, constant: 0).isActive = true
        NSLayoutConstraint.activate([
            stackViewContainer.topAnchor.constraint(equalTo: scrollView.topAnchor),
            stackViewContainer.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor),
            stackViewContainer.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor),
            stackViewContainer.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor),
            stackViewContainer.widthAnchor.constraint(equalTo: scrollView.widthAnchor),
            stackView.topAnchor.constraint(equalTo: stackViewContainer.topAnchor),
            stackView.leadingAnchor.constraint(equalTo: stackViewContainer.leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: stackViewContainer.trailingAnchor),
            stackView.bottomAnchor.constraint(equalTo: stackViewContainer.bottomAnchor)
        ])
        
        
        let descWidth = Int(KSize.width / 3)
        let contentX = Int(KSize.width / 3 + 10)
        let btnX = Int(KSize.width / 3 * 2 + 15)
        var startLabelY:Int = 0
        let lineHigh:Int = 60
        var lineY:Int = 60
        var btnY:Int = 15
        let btnHeight = 30
        
       
        passwordView = UIView()
        passwordView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        passwordView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(passwordView)
        
        self.passwordLabel = UILabel()
        self.passwordLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.passwordLabel.textColor = UIColor.black
        self.passwordLabel.text = NSLocalizedString("password_desc", comment: "Password:")
        self.passwordLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.passwordLabel.numberOfLines = 0;
        self.passwordLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        passwordView.addSubview(self.passwordLabel)
       
        self.editPasswordBtn = QMUIGhostButton()
        self.editPasswordBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editPasswordBtn.setTitle(NSLocalizedString("reset", comment: "Reset"), for: .normal)
        self.editPasswordBtn.ghostColor = UIColor.colorPrimary
        self.editPasswordBtn.frame = CGRect(x: contentX, y: 15, width: 60, height: btnHeight)
        self.editPasswordBtn.addTarget(self, action: #selector(editPassword), for:.touchUpInside)
        passwordView.addSubview(self.editPasswordBtn)
        let passwordLine = UIView()
        passwordLine.backgroundColor = UIColor.gray
        passwordLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        passwordView.addSubview(passwordLine)
       
        
        resetFactoryView = UIView()
        resetFactoryView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        resetFactoryView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(resetFactoryView)
        self.resetFactoryLabel = UILabel()
        self.resetFactoryLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.resetFactoryLabel.textColor = UIColor.black
        self.resetFactoryLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.resetFactoryLabel.numberOfLines = 0;
        self.resetFactoryLabel.text =  NSLocalizedString("reset_factory_desc", comment:"Reset factory:")
        self.resetFactoryLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        resetFactoryView.addSubview(self.resetFactoryLabel)
        self.editResetFactoryBtn = QMUIGhostButton()
        self.editResetFactoryBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editResetFactoryBtn.setTitle(NSLocalizedString("reset_factory_btn", comment:"Reset factory"), for: .normal)
        self.editResetFactoryBtn.ghostColor = UIColor.colorPrimary
        self.editResetFactoryBtn.addTarget(self, action: #selector(resetFactoryClick), for:.touchUpInside)
        self.editResetFactoryBtn.frame = CGRect(x: contentX, y: 15, width: 120, height: btnHeight)
        resetFactoryView.addSubview(self.editResetFactoryBtn)
        let resetFactoryLine = UIView()
        resetFactoryLine.backgroundColor = UIColor.gray
        resetFactoryLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        resetFactoryView.addSubview(resetFactoryLine)
        resetFactoryView.isHidden = false
        //        scrollView.contentSize =  CGSize(width: self.view.bounds.size.width, height: stackView.frame.size.height + 20)
        scrollView.contentSize = stackViewContainer.frame.size
        print("frame")
        print(stackView.frame)
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
        
    }
    func showWaitingWin(title:String,isShowCancel:Bool?=false,isCancelDoClose:Bool?=false){
        if self.waitingView != nil && !self.waitingView.isDismiss{
            self.waitingView.show()
            return
        }
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
             
            self.waitingView.dismiss()
            if isCancelDoClose ?? false{
                self.navigationController?.popViewController(animated: true)
            }
            
        }
        self.waitingView = AEUIAlertView(style: .textField, title: title, message: nil)
        if isShowCancel ?? false{
            self.waitingView.actions = [action_one]
        }else{
            self.waitingView.actions = []
        }
     
        self.waitingView.resetActions()
        
        let animation = UIView(frame: CGRect(x: 0, y: 0, width: 80, height: 80))
        let anim = AEBeginLineAnimation.initShow(in: animation.bounds, lineWidth: 4, lineColor: UIColor.blue)
        animation.addSubview(anim)
        
        self.waitingView.textField.isHidden = true
        self.waitingView.set(animation: animation, width: 80, height: 80)
        self.waitingView.show()
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(60)) {
            if !self.waitingView.isDismiss{
                Toast.hudBuilder.title(NSLocalizedString("timeout_warning", comment:"Timeout, please try again!")).show()
            }
            self.waitingView.dismiss()
        }
    }
    
    func showProgressBar(){
        if self.progressView != nil && !self.progressView.isDismiss{
            self.progressView.show()
            return
        }
        progressView = AEAlertView(style: .defaulted)
        progressView.title = NSLocalizedString("upgrade", comment:"Upgrade")
        progressView.message = ""
        self.progressBar = MyProgress(frame:CGRect.init(x:30,y:20,width:180,height: 30))
        self.progressBar.tintColor = UIColor.green
        self.progressBar.minimumValue = 1
        self.progressBar.maximumValue = 100
        self.progressBar.thumbTintColor = UIColor.clear
        self.progressBar.setValue(0,animated: true)
        progressView.set(animation: self.progressBar, width: 300, height: 30)
        progressView.show()
    }
    
   
    
    private var isShowPwdDlg = false
    func showPwdWin(){
        if self.pwdAlert != nil && !self.pwdAlert.isDismiss{
            if !self.isShowPwdDlg{
                self.pwdAlert.show()
                self.isShowPwdDlg = true
            }
            return
        }
        self.pwdAlert = AEUIAlertView(style: .password, title: NSLocalizedString("input_super_device_password", comment:"Enter super password"), message: nil)
        self.pwdAlert.textField.placeholder = NSLocalizedString("password", comment:"Password")
       
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.isShowPwdDlg = false
            self.pwdAlert.dismiss()
            self.waitingView.dismiss()
            self.navigationController?.popViewController(animated: true)
            
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            self.isShowPwdDlg = false
            let pwd = String(self.pwdAlert.textField.text ?? "")
            self.pwdErrorWarning = false;
            if pwd.count == 6{
                self.pwdAlert.dismiss()
                self.confirmPwd = pwd
                self.showWaitingWin(title: NSLocalizedString("waiting", comment: "waiting"))
                if self.connected == true{
                    self.doInitSucc()
                }
            }else{
                Toast.hudBuilder.title(NSLocalizedString("pwd_value_error_warning", comment: "Value is incorrect!The length has to be 6 digits")).show()
            }
        }
        self.pwdAlert.addAction(action: action_one)
        self.pwdAlert.addAction(action: action_two)
        if !self.isShowPwdDlg{
            self.pwdAlert.show()
            self.isShowPwdDlg = true
        }
    }
    
    
}
