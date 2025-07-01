//
//  SuperPwdResetController.swift
//  tftble
//
//  Created by china topflytech on 2024/5/21.
//  Copyright © 2024 com.tftiot. All rights reserved.
//
 

import UIKit
import CoreBluetooth
import CLXToast
import ActionSheetPicker_3_0
 
class SuperPwdResetController:UIViewController,CBCentralManagerDelegate,CBPeripheralDelegate {
     
    
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
    var name = ""
     
    private var fontSize:CGFloat = CGFloat(Utils.fontSize)
    private var waitingView:AEUIAlertView!
    private var pwdAlert:AEUIAlertView!
    
    
    private var selfPeripheral:CBPeripheral!
    private var characteristic: CBCharacteristic?
 
 
    private var resetFactoryLabel:UILabel!
    private var editResetFactoryBtn:UIButton!
 
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
        peripheral.discoverServices([BleDeviceData.unclockServiceId])
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
            if service.uuid == BleDeviceData.unclockServiceId {
                print("find service id")
                let myService = service
                peripheral.discoverCharacteristics([BleDeviceData.unclockNotifyUUID], for: myService)
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
            if characteristic.uuid == BleDeviceData.unclockNotifyUUID {
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
            var dataList = Utils.parseRespContent(content: bytes)
            for bleRespData in dataList {
                print("resp code:\(bleRespData.controlCode)")
                if bleRespData.type == BleRespData.READ_TYPE || bleRespData.type == BleRespData.WRITE_TYPE {
                    let code = bleRespData.controlCode
                    if code == BleDeviceData.func_id_of_sub_lock_factory_reset{
                        Toast.hudBuilder.title(NSLocalizedString("factory_reset_succ",comment:"Factory Settings restored successfully, please enter the password to reconnect") ).show()
                        self.navigationController?.popViewController(animated: true)
                    }else if code == BleDeviceData.func_id_of_ble_pwd_change{
                        Toast.hudBuilder.title(NSLocalizedString("password_has_been_reset",comment:"Password has been reset to 654321!") ).show()
                    }else if code == BleDeviceData.func_id_of_change_unlock_pwd{
                        Toast.hudBuilder.title(NSLocalizedString("password_has_been_reset",comment:"Unclock Password has been reset to 654321!") ).show()
                       
                    }
                }else{
                    if bleRespData.errorCode == BleRespData.ERROR_CODE_OF_PWD_ERROR {
                        self.waitingView.dismiss()
                        if(!self.pwdErrorWarning){
                            self.pwdErrorWarning = true;
                            Toast.hudBuilder.title(NSLocalizedString("super_password_is_error", comment: "Super password is error")).show()
                            self.initStart = false
                            self.showPwdWin()
                        }
                    }else{
                        self.waitingView.dismiss()
                        print("Error,please try again!")
                        Toast.hudBuilder.title(NSLocalizedString("error_need_try_warning", comment: "Error,please try again!")).show()
                    }
                }
            }
        }
        
    }
    
    func readData(cmdHead:Int){
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let uint8Array = self.getInterActiveCmd(pwd: self.confirmPwd, cmdHead: cmdHead , content: [])
        print(uint8Array)
        let data = Data(bytes:uint8Array,count: uint8Array.count)
        self.selfPeripheral.writeValue(data, for: self.characteristic!, type: CBCharacteristicWriteType.withResponse)
    }
    
  
       
    func writeStrData(cmdHead:Int,dataStr:String){
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
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
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let uint8Array = self.getInterActiveCmd(pwd: inPwd, cmdHead: cmdHead , content: content)
        let data = Data(bytes:uint8Array,count: uint8Array.count)
        print(uint8Array)
        self.selfPeripheral.writeValue(data, for: self.characteristic!, type: CBCharacteristicWriteType.withResponse)
    }
    
    func writeArrayData(cmdHead:Int,content:[UInt8]){
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let uint8Array = self.getInterActiveCmd(pwd: self.confirmPwd, cmdHead: cmdHead , content: content)
        let data = Data(bytes:uint8Array,count: uint8Array.count)
        print(uint8Array)
        self.selfPeripheral.writeValue(data, for: self.characteristic!, type: CBCharacteristicWriteType.withResponse)
    }
    
     func fixTime() {
        // Placeholder implementation
        let now = Date()
        let timeData = Utils.formatHex(intValue: Int(now.timeIntervalSince1970), len: 4)
        var data = [UInt8]()
        data.append(UInt8(0))
        data.append(contentsOf: timeData)
        let content = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_datetime, content: data,pwd: nil)
        self.writeArrayContent(writeContentList:content )
    }
     
    public func writeArrayContent(writeContentList: [[UInt8]]) {
        for item in writeContentList {
            let data = Data(bytes:item,count: item.count)
            print("write :\(item)")
            self.selfPeripheral.writeValue(data, for: self.characteristic!, type: CBCharacteristicWriteType.withResponse)
        }
    }
    
    func writePwd(pwd:String){
        do {
            var outputStream: [UInt8] = []
            outputStream.append(contentsOf: self.confirmPwd.utf8)
            outputStream.append(contentsOf: pwd.utf8)
              
            let arrayList = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_ble_pwd_change , content: outputStream,pwd:nil)
            self.writeArrayContent(writeContentList:arrayList )
        } catch {
            print("Error changing BLE password: \(error)")
        }
    }
    
    func writeUnclockPwd(pwd:String){
        do {
            var outputStream: [UInt8] = []
            outputStream.append(contentsOf: self.confirmPwd.utf8)
            outputStream.append(contentsOf: pwd.utf8)
              
            let arrayList = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_change_unlock_pwd, content: outputStream,pwd:self.confirmPwd)
            self.writeArrayContent(writeContentList:arrayList )
        } catch {
            print("Error changing unclock BLE password: \(error)")
        }
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
    
  
    
    @objc private func editUnclockPwd() {
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_reset_unclock_password_warning", comment: "Try to reset password?")
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            self.writeUnclockPwd(pwd:"654321")
            animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
    }
    
    @objc private func editPassword() {
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_reset_ble_password_warning", comment: "Try to reset password?")
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
        let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_factory_reset, content: [0x00],pwd: self.confirmPwd)
        self.writeArrayContent(writeContentList:cmd ) 
    }
    
    
    @objc private func resetFactoryClick() {
        if !self.connected{
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_reset_factory_warning", comment: "Are you sure to restore the factory settings?") 
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
    private var editPasswordBtn:UIButton!
     
    var passwordView:UIView!
   
    var resetFactoryView:UIView!
    private var unclockPwdLabel:UILabel!
     private var unclockPwdContentLabel:UILabel!
     private var editUnclockPwdBtn:UIButton!
      
     var unclockPwdView:UIView!
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
        self.passwordLabel.text = NSLocalizedString("ble_pwd", comment: "ble_pwd")
        self.passwordLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.passwordLabel.numberOfLines = 0;
        self.passwordLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        passwordView.addSubview(self.passwordLabel)
       
        self.editPasswordBtn = UIButton()
        self.editPasswordBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editPasswordBtn.setTitle(NSLocalizedString("reset", comment: "Reset"), for: .normal)
//        self.editPasswordBtn.ghostColor = UIColor.colorPrimary
        self.editPasswordBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editPasswordBtn.layer.cornerRadius = 15;
        self.editPasswordBtn.layer.borderWidth = 1.0;
        self.editPasswordBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editPasswordBtn.frame = CGRect(x: contentX, y: 15, width: 60, height: btnHeight)
        self.editPasswordBtn.addTarget(self, action: #selector(editPassword), for:.touchUpInside)
        passwordView.addSubview(self.editPasswordBtn)
        let passwordLine = UIView()
        passwordLine.backgroundColor = UIColor.gray
        passwordLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        passwordView.addSubview(passwordLine)
       
        unclockPwdView = UIView()
        unclockPwdView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        unclockPwdView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(unclockPwdView)
        
        self.unclockPwdLabel = UILabel()
        self.unclockPwdLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.unclockPwdLabel.textColor = UIColor.black
        self.unclockPwdLabel.text = NSLocalizedString("unlock_pwd", comment: "unlock_pwd")
        self.unclockPwdLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.unclockPwdLabel.numberOfLines = 0;
        self.unclockPwdLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        unclockPwdView.addSubview(self.unclockPwdLabel)
       
        self.editUnclockPwdBtn = UIButton()
        self.editUnclockPwdBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editUnclockPwdBtn.setTitle(NSLocalizedString("reset", comment: "Reset"), for: .normal)
//        self.editUnclockPwdBtn.ghostColor = UIColor.colorPrimary
        self.editUnclockPwdBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editUnclockPwdBtn.layer.cornerRadius = 15;
        self.editUnclockPwdBtn.layer.borderWidth = 1.0;
        self.editUnclockPwdBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editUnclockPwdBtn.frame = CGRect(x: contentX, y: 15, width: 60, height: btnHeight)
        self.editUnclockPwdBtn.addTarget(self, action: #selector(editUnclockPwd), for:.touchUpInside)
        unclockPwdView.addSubview(self.editUnclockPwdBtn)
        let unclockPwdLine = UIView()
        unclockPwdLine.backgroundColor = UIColor.gray
        unclockPwdLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        unclockPwdView.addSubview(unclockPwdLine)
        
        resetFactoryView = UIView()
        resetFactoryView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        resetFactoryView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(resetFactoryView)
        self.resetFactoryLabel = UILabel()
        self.resetFactoryLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.resetFactoryLabel.textColor = UIColor.black
        self.resetFactoryLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.resetFactoryLabel.numberOfLines = 0;
        self.resetFactoryLabel.text =  NSLocalizedString("factory_reset", comment:"factory_reset")
        self.resetFactoryLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        resetFactoryView.addSubview(self.resetFactoryLabel)
        self.editResetFactoryBtn = UIButton()
        self.editResetFactoryBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editResetFactoryBtn.setTitle(NSLocalizedString("factory_reset", comment:"factory_reset"), for: .normal)
//        self.editResetFactoryBtn.ghostColor = UIColor.colorPrimary
        self.editResetFactoryBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editResetFactoryBtn.layer.cornerRadius = 15;
        self.editResetFactoryBtn.layer.borderWidth = 1.0;
        self.editResetFactoryBtn.layer.borderColor = UIColor.nordicBlue.cgColor
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
        self.pwdAlert.textField.placeholder = NSLocalizedString("input_super_device_password", comment:"Password")
       
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
