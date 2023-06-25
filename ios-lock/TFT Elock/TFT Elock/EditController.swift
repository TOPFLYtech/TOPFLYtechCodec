//
//  EditController.swift
//  TFTElock
//
//  Created by jeech on 2021/6/9.
//  Copyright © 2021 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import CoreBluetooth
import CLXToast 
import ActionSheetPicker_3_0



extension UIView {
    
    func addOnClickListener(target: AnyObject, action: Selector) {
        let gr = UITapGestureRecognizer(target: target, action: action)
        gr.numberOfTapsRequired = 1
        isUserInteractionEnabled = true
        addGestureRecognizer(gr)
    }
    
}


class EditController:UIViewController,CBCentralManagerDelegate,CBPeripheralDelegate{
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
        peripheral.discoverServices([Utils.serviceId,Utils.notifyUUID,Utils.writeUUID])
        
    }
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        if peripheral.identifier == self.cbPeripheral.identifier{
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
            if service.uuid == Utils.serviceId {
                let myServie = service
                print("find include service id")
                peripheral.discoverCharacteristics(nil, for: myServie)
                break;
            }
        }
    }
    
    var tryFindService = 100
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        print("edit didDiscoverServices")
                        print(peripheral.services)
        print(cbPeripheral.services)
        print(self.selfPeripheral.services)
        if peripheral.services!.count == 0 && tryFindService != 0{
            tryFindService -= 1
            print("retry to find service \(tryFindService)")
        
            DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(1)) {
                peripheral.discoverServices([Utils.serviceId,Utils.notifyUUID,Utils.writeUUID])
            }
            
        }
        for service: CBService in peripheral.services! {
                        print("didDiscoverServices 外设中的服务有：\(service)")
            if service.uuid == Utils.serviceId {
                let myServie = service
                print("find service id")
                peripheral.discoverCharacteristics(nil, for: myServie)
                peripheral.discoverIncludedServices([Utils.serviceId,Utils.notifyUUID,Utils.writeUUID],for:myServie)
                break;
            }
        }
    }
    
    /** 发现特征 */
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        print("didDiscoverCharacteristicsFor")
        //        print(service.characteristics)
        for c: CBCharacteristic in service.characteristics!{
            if c.uuid == Utils.notifyUUID{
                print("find notify c")
                self.notifyCharacteristic = c
                peripheral.setNotifyValue(true, for: self.notifyCharacteristic!)
            }
            if c.uuid == Utils.writeUUID{
                print("find write c")
                self.writeCharacteristic = c
            }
        }
    }
   
    //notify return data
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        let data = characteristic.value
        canSendMsgLock.lock()
        isCanSendMsg = true
        canSendMsgLock.unlock()
        if data != nil{
            let bytes = [UInt8](data!)
            print("writeContent,resp:")
            print(bytes)
            self.parseResp(respContent: bytes)
//            var dataList = Utils.parseRespContent(content: bytes)
//            for bleRespData in dataList{
//                print("writeContent,resp:code:" + String(bleRespData.controlCode!))
//                if bleRespData.type == BleRespData.READ_TYPE || bleRespData.type == BleRespData.WRITE_TYPE{
//                    self.parseReadResp(bleRespData: bleRespData)
//                }else{œ
//                    print("writeContent,resp:error code:" + String(bleRespData.controlCode!) + "-" + String(bleRespData.type!))
//                }
//            }
        }
    }
    
    
    func parseResp(respContent:[UInt8]){
        var i = 0;
        while i + 3 <= respContent.count{
            var head = [respContent[i],respContent[i+1],respContent[i+2]]
            if Utils.arraysEqual(item1: head, item2: deviceReadyHead){
                if(i + 6 <= respContent.count){
                    var status = respContent[i+5]
                    if status == 0x01{
                        isDeviceReady = true
                        self.getLockStatus()
                        DispatchQueue.main.async {
                            self.waitingView.dismiss()
                        }
                    }
                }
                i = i + 6
            }else if Utils.arraysEqual(item1: head, item2: getLockStatusHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        self.waitingView.dismiss()
                    }
                    self.parseLockType(lockType: lockType)
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: unlockHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        self.waitingView.dismiss()
                    }
                    self.parseLockType(lockType: lockType)
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: lockHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        self.waitingView.dismiss()
                    }
                    self.parseLockType(lockType: lockType)
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: activeNetworkHead){
                if (i + 5 <= respContent.count){
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        self.waitingView.dismiss()
                    }
                    showDetailMsg(msg: NSLocalizedString("activeNetworkCmdSend", comment: "The activation network command has been sent successfully"))
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: uploadStatusHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        self.waitingView.dismiss()
                    }
                    self.parseLockType(lockType: lockType)
                }
                i+=5
            }else{
                i = i + 1
            }
        }
    }
    
    //notify succ
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
            self.isCanSendMsg = true
            if !isDeviceReady {
                self.waitingView.title = NSLocalizedString("readBG9xStatus", comment: "Waiting device Ready")
                sendCheckDeviceReadyCmd()
            }
            
        } else {
            self.initRightBtn(isConnect:false)
            print("取消订阅")
        }
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("连接失败")
        self.initRightBtn(isConnect:false)
        self.connected = false
        self.isCanSendMsg = false
    }
    
    /** 断开连接 */
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("断开连接")
        self.initRightBtn(isConnect:false)
        self.connected = false
        self.isCanSendMsg = false
        // 重新连接
        if self.needConnect{
            self.showDetailMsg(msg: NSLocalizedString("disconnect_from_device", comment: "Disconnect from the device."))
            Toast.hudBuilder.title(NSLocalizedString("disconnect_from_device", comment: "Disconnect from the device.")).show()
            //            central.connect(peripheral, options: nil)
        }
        
        
    }
    
    var allCmdWaitingSendCount = 0
    var sendMsgThread: Thread?
    var canSendMsgLock = NSCondition()
    var bleSensorCount = 0
    private var progressView:AEAlertView!
    private var progressBar:MyProgress!
    private var waitingView:AEUIAlertView!
    private var pwdAlert:AEUIAlertView!
    var barLabel:UILabel!
    var centralManager   : CBCentralManager!
    var cbPeripheral:CBPeripheral!
    private var selfPeripheral:CBPeripheral!
    private var notifyCharacteristic: CBCharacteristic?
    private var writeCharacteristic: CBCharacteristic?
    var mac = ""
    var deviceType = ""
    var name = ""
    private var leaveViewNeedDisconnect = true
    private var onView = false
    private var initStart = false
    private var connected = false
    private var isDeviceReady = false
    private var isCanSendMsg = false
    private var needConnect = false
    private var isUpgrade = false
    private var foundDevice = false
    private var connectControl = ""
    var lastCheckStatusDate:Date = Date()
    let getStatusTimeout = 4
    var sendMsgQueue:MsgQueue = MsgQueue()
    var sendMsgMultiQueue:MsgQueue = MsgQueue()
    var lockStatusImg:UIImageView!
    var lockStatusLabel:UILabel!
    private var deviceReadyHead:[UInt8] = [0x20,0x00,0x01]
    private var getLockStatusHead:[UInt8] = [0x20,0x00,0x1D]
    private var unlockHead:[UInt8] = [0x60,0x07,0xDA]
    private var lockHead:[UInt8] = [0x60,0x07,0xDB]
    private var activeNetworkHead:[UInt8] = [0x60,0x07,0xDC]
    private var uploadStatusHead:[UInt8] = [0x30,0xA0,0x29]
    private var uniqueID = ""
    override func viewDidDisappear(_ animated: Bool) {
        self.centralManager.stopScan()
        self.centralManager.cancelPeripheralConnection(self.cbPeripheral)
        self.onView = false
        self.needConnect = false
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        uniqueID = UniqueIDTool.getMediaDrmID()
        print("uid:\(uniqueID)")
        self.navigationController!.navigationBar.isTranslucent = false
        self.extendedLayoutIncludesOpaqueBars = true
        self.initUI()
        self.onView = true
        self.needConnect = true
        let mac = self.cbPeripheral.identifier.uuidString
        self.initNavBar()
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
        self.sendMsgThread = Thread(target: self, selector: #selector(sendMsgThreadFunc), object: nil)
        self.sendMsgThread?.start()
        self.showWaitingWin(title: NSLocalizedString("connecting", comment: "Connecting"))
        print("view did load")
        
    }
    
    func initNavBar(){
        barLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        //        titleLabel.text = "Bluetooth sensor"
        barLabel.text = self.name
        self.navigationItem.titleView = barLabel
        self.initRightBtn(isConnect: false)
        navigationController?.navigationBar.barTintColor = UIColor.colorPrimary
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
    @objc private func refreshClick() {
        print ("refresh click")
        self.initStart = false
        if self.connected == true{
            self.centralManager.cancelPeripheralConnection(self.selfPeripheral)
        }
        self.centralManager.connect(self.selfPeripheral)
        
    }
    func asciiStringToBytes(str: String) -> [UInt8]{
            var bytes: [UInt8] = []
            for character in str.unicodeScalars {
                print(character)
                print(character.value)
            }
            return bytes
        }
    func showPwdWin(){
        if self.pwdAlert != nil && !self.pwdAlert.isDismiss{
            self.pwdAlert.show()
            return
        }
        self.pwdAlert = AEUIAlertView(style: .password, title: NSLocalizedString("input_ble_open_lock_pwd", comment:"Please enter your password"), message: nil)
        self.pwdAlert.textField.placeholder = NSLocalizedString("input_ble_open_lock_pwd", comment:"Please enter your password")
      
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.pwdAlert.dismiss()
            
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let pwd = String(self.pwdAlert.textField.text ?? "")
            if pwd.count == 6{
                self.pwdAlert.dismiss()
                let pwdArray = [UInt8](pwd.utf8)
                var needSend = self.getCmdContent(head: self.unlockHead, content: pwdArray,isNeedUniqueID: true)
                self.writeContent(content: needSend)
            }else{
                Toast.hudBuilder.title(NSLocalizedString("pwd_format_error", comment: "Value is incorrect!The length has to be 6 digits")).show()
            }
        }
        self.pwdAlert.addAction(action: action_one)
        self.pwdAlert.addAction(action: action_two)
        self.pwdAlert.show()
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

    @objc func sendMsgThreadFunc(){
        print("send msg thread start")
        while onView{
            if (sendMsgQueue.count == 0 && sendMsgMultiQueue.count == 0) || connected == false || isDeviceReady == false{
                if !isDeviceReady{  
                    Thread.sleep(forTimeInterval: 1)
                    canSendMsgLock.lock()
                    isCanSendMsg = false
                    canSendMsgLock.unlock() 
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
                        var needSendBytes = sendMsgMultiQueue.pop()
                        if needSendBytes != nil {
                            writeContent(content: needSendBytes!)
                        }
                    }
                }
            }
        }
    }
    
    func sendCheckDeviceReadyCmd(){
        self.writeContent(content: deviceReadyHead)
    }
    
    func writeContent(content:[UInt8]){
        if !connected{
            return
        }
        if content != nil && content.count > 0 {
            let data = Data(bytes:content,count: content.count)
            print("write:\(content)")
            self.selfPeripheral.writeValue(data, for: self.writeCharacteristic!, type: CBCharacteristicWriteType.withResponse)
        }
    }
    
    
    var dateLabel:UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: 14)
        label.text = NSLocalizedString("date", comment: "Date:")
        return label
    }()
    var dateDescLabel:UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    var uniqueIDLabel:UILabel = {
         let label = UILabel()
         label.textColor = UIColor.black
         label.font = UIFont.systemFont(ofSize: 14)
         label.text = NSLocalizedString("unique_id", comment: "Unique ID:")
         return label
     }()
     var uniqueIDDescLabel:UILabel = {
         let label = UILabel()
         label.textColor = UIColor.black
         label.font = UIFont.systemFont(ofSize: 14)
         return label
     }()

    func initUI(){
        self.view.backgroundColor = UIColor.white
        var contentY = 80
        let dateLabelWidth = 60
        let dateLabelX:Int = (Int)(self.view.bounds.size.width / 2 - 100)
        self.uniqueIDLabel.frame = CGRect(x: dateLabelX, y: contentY, width: 80, height: 30)
        self.uniqueIDLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.uniqueIDDescLabel.frame = CGRect(x: dateLabelX+80, y: contentY, width: 200, height: 30)
        self.uniqueIDDescLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.uniqueIDDescLabel.text = uniqueID
        self.view.addSubview(self.uniqueIDLabel)
        self.view.addSubview(self.uniqueIDDescLabel)
        contentY = contentY + 40
        self.dateLabel.frame = CGRect(x: dateLabelX, y: contentY, width: 60, height: 30)
        self.dateLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.dateDescLabel.frame = CGRect(x: dateLabelX+60, y: contentY, width: 200, height: 30)
        self.dateDescLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.view.addSubview(self.dateLabel)
        self.view.addSubview(self.dateDescLabel)
        contentY = contentY + 40
        lockStatusImg = UIImageView.init(frame: CGRect(x: (Int)(self.view.bounds.size.width / 2 - 100), y: contentY, width: 200, height: 200))
        lockStatusImg.contentMode = .scaleAspectFit;
        lockStatusImg.image = UIImage (named: "ic_lock.png")
        lockStatusImg.backgroundColor = UIColor.clear
        contentY = contentY + 200
        self.view.addSubview(lockStatusImg)
        self.lockStatusLabel = UILabel.init(frame: CGRect(x: 30, y: contentY, width: Int(self.view.bounds.size.width)-60, height: 60))
        self.lockStatusLabel.textAlignment = .center
        self.lockStatusLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.lockStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping
        self.lockStatusLabel.numberOfLines = 0
        self.view.addSubview(self.lockStatusLabel)
        contentY = contentY + 60
//        let lockBtn = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width / 3 / 2 - 40), y: contentY, width: 80, height: 30))
//        lockBtn.setTitle( NSLocalizedString("lock", comment: "Lock"), for: .normal)
//        lockBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
//        lockBtn.layer.cornerRadius = 15;
//        lockBtn.layer.borderWidth = 1.0;
//        lockBtn.layer.borderColor = UIColor.nordicBlue.cgColor
//        lockBtn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
//        lockBtn.addTarget(self, action: #selector(lockClick), for:.touchUpInside)
//        self.view.addSubview(lockBtn)
        let unlock = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width / 2 / 2  - 40), y: contentY, width: 80, height: 30))
        unlock.setTitle( NSLocalizedString("unlock", comment: "Unlock"), for: .normal)
        unlock.setTitleColor(UIColor.nordicBlue, for: .normal)
        unlock.layer.cornerRadius = 15;
        unlock.layer.borderWidth = 1.0;
        unlock.layer.borderColor = UIColor.nordicBlue.cgColor
        unlock.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        unlock.addTarget(self, action: #selector(unlockClick), for:.touchUpInside)
        self.view.addSubview(unlock)
//        let activeNetwork = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width -  self.view.bounds.size.width / 3 / 2 - 40), y: contentY, width: 80, height: 30))
        let activeNetwork = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width - self.view.bounds.size.width / 2 / 2  - 40), y: contentY, width: 80, height: 30))
        activeNetwork.setTitle( NSLocalizedString("active_network", comment: "Active Network"), for: .normal)
        activeNetwork.setTitleColor(UIColor.nordicBlue, for: .normal)
        activeNetwork.layer.cornerRadius = 15;
        activeNetwork.layer.borderWidth = 1.0;
        activeNetwork.layer.borderColor = UIColor.nordicBlue.cgColor
        activeNetwork.addTarget(self, action: #selector(activeNetworkClick), for:.touchUpInside)
        activeNetwork.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.view.addSubview(activeNetwork)
        contentY = contentY + 40
    }
    
    func getCmdContent(head:[UInt8],content:[UInt8],isNeedUniqueID:Bool)->[UInt8]{
        var result = [UInt8]()
        var len = content.count
        if isNeedUniqueID{
            len += 6
        }
        result.append(contentsOf: head)
        result.append((UInt8)(len))
        result.append(contentsOf: content)
        if isNeedUniqueID{
            result.append(contentsOf: Utils.hexString2Bytes(hexStr: self.uniqueID))
        }
        return result
    }
    
    
    @objc private func lockClick() {
        var needSend = self.getCmdContent(head: lockHead, content: [0x00],isNeedUniqueID: true)
        self.writeContent(content: needSend)
    }
    @objc private func unlockClick() {
        showPwdWin()
    }
    
    @objc private func activeNetworkClick() {
        var needSend = self.getCmdContent(head: activeNetworkHead, content: [0x00],isNeedUniqueID: true)
        self.writeContent(content: needSend)
    }
    
    func getLockStatus(){
        self.writeContent(content: getLockStatusHead)
    }
    
    func setLockRefreshTime(){
        lastCheckStatusDate = Date()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let dateStr = dateFormatter.string(from: lastCheckStatusDate)
        self.dateDescLabel.text = dateStr
    }
    
    func isDeviceLockErrorState(code:UInt8) -> Bool{
        var isError = false
        if code == 0x03 || code == 0x14 || code == 0x24 || code == 0x34 || code == 0x44 || code == 0x54
        {
            isError = true
        }
        return isError
    }
    
    func isDeviceLock(lockType:UInt8)->Bool{
        var isLock = false
        if lockType == 0x00 ||
            lockType == 0x02 ||
            lockType == 0x03 ||
            lockType == 0x05 ||
            lockType == 0x07 ||
            lockType == 0x12 ||
            lockType == 0x13 ||
            lockType == 0x14 ||
            lockType == 0x22 ||
            lockType == 0x23 ||
            lockType == 0x24 ||
            lockType == 0x32 ||
            lockType == 0x33 ||
            lockType == 0x34 ||
            lockType == 0x42 ||
            lockType == 0x43 ||
            lockType == 0x44 ||
            lockType == 0x52 ||
            lockType == 0x53 ||
            lockType == 0x54
        {
            isLock = true
        }
        return isLock
    }
    
    func isDeviceLockThreadTrimming(lockType:UInt8)->Bool{
        if lockType == 0x01
            || lockType == 0x16
            || lockType == 0x26
            || lockType == 0x36
            || lockType == 0x46
            || lockType == 0x56
        {
            return true
        }else{
            return false
        }
    }
    
    func setImgLockStatus(lockType:UInt8){
        if (lockType == 0xff || lockType == 0x04 || lockType == 0x09){
           return;
        }
        let isLock = self.isDeviceLock(lockType: lockType)
        let isTrimming = self.isDeviceLockThreadTrimming(lockType: lockType)
        let isLockError = self.isDeviceLockErrorState(code: lockType)
        if isTrimming{
            self.lockStatusImg.image = UIImage(named: "ic_suocut.png")
        }else if isLock{
            if isLockError{
                self.lockStatusImg.image = UIImage(named: "ic_lock_error.png")
            }else{
                self.lockStatusImg.image = UIImage(named: "ic_lock.png")
            }
        }else {
            if isLockError{
                self.lockStatusImg.image = UIImage(named: "ic_unlock_error.png")
            }else{
                self.lockStatusImg.image = UIImage(named: "ic_unlock.png")
            }
        }
    }
    
    func showDetailMsg(msg:String){
        self.lockStatusLabel.text = msg
    }
    
    func parseLockType(lockType:UInt8){
        self.setImgLockStatus(lockType: lockType)
        if lockType == 0x00{
            showDetailMsg(msg: NSLocalizedString("lock_status_00", comment: "lock_status_00"))
        }else if lockType == 0x01{
            showDetailMsg(msg: NSLocalizedString("lock_status_01", comment: "lock_status_01"))
        }else if lockType == 0x03{
            showDetailMsg(msg: NSLocalizedString("lock_status_03", comment: "lock_status_03"))
        }else if lockType == 0x04{
            showDetailMsg(msg: NSLocalizedString("lock_status_04", comment: "lock_status_04"))
        }else if lockType == 0x09{
            showDetailMsg(msg: NSLocalizedString("lock_status_09", comment: "lock_status_09"))
        }else if lockType == 0x05{
            showDetailMsg(msg: NSLocalizedString("lock_status_05", comment: "lock_status_05"))
        }else if lockType == 0x06{
            showDetailMsg(msg: NSLocalizedString("lock_status_06", comment: "lock_status_06"))
        }else if lockType == 0x07{
            showDetailMsg(msg: NSLocalizedString("lock_status_07", comment: "lock_status_07"))
        }else if lockType == 0x08{
            showDetailMsg(msg: NSLocalizedString("lock_status_08", comment: "lock_status_08"))
        }else if lockType == 0x11{
            showDetailMsg(msg: NSLocalizedString("lock_status_11", comment: "lock_status_11"))
        }else if lockType == 0x12{
            showDetailMsg(msg: NSLocalizedString("lock_status_12", comment: "lock_status_12"))
        }else if lockType == 0x13{
            showDetailMsg(msg: NSLocalizedString("lock_status_13", comment: "lock_status_13"))
        }else if lockType == 0x14{
            showDetailMsg(msg: NSLocalizedString("lock_status_14", comment: "lock_status_14"))
        }else if lockType == 0x15{
            showDetailMsg(msg: NSLocalizedString("lock_status_15", comment: "lock_status_15"))
        }else if lockType == 0x16{
            showDetailMsg(msg: NSLocalizedString("lock_status_16", comment: "lock_status_16"))
        }
        else if lockType == 0x21{
            showDetailMsg(msg: NSLocalizedString("lock_status_21", comment: "lock_status_21"))
        }else if lockType == 0x22{
            showDetailMsg(msg: NSLocalizedString("lock_status_22", comment: "lock_status_22"))
        }else if lockType == 0x23{
            showDetailMsg(msg: NSLocalizedString("lock_status_23", comment: "lock_status_23"))
        }else if lockType == 0x24{
            showDetailMsg(msg: NSLocalizedString("lock_status_24", comment: "lock_status_24"))
        }else if lockType == 0x25{
            showDetailMsg(msg: NSLocalizedString("lock_status_25", comment: "lock_status_25"))
        }else if lockType == 0x26{
            showDetailMsg(msg: NSLocalizedString("lock_status_26", comment: "lock_status_26"))
        }
        else if lockType == 0x31{
            showDetailMsg(msg: NSLocalizedString("lock_status_31", comment: "lock_status_31"))
        }else if lockType == 0x32{
            showDetailMsg(msg: NSLocalizedString("lock_status_32", comment: "lock_status_32"))
        }else if lockType == 0x33{
            showDetailMsg(msg: NSLocalizedString("lock_status_33", comment: "lock_status_33"))
        }else if lockType == 0x34{
            showDetailMsg(msg: NSLocalizedString("lock_status_34", comment: "lock_status_34"))
        }else if lockType == 0x35{
            showDetailMsg(msg: NSLocalizedString("lock_status_35", comment: "lock_status_35"))
        }else if lockType == 0x36{
            showDetailMsg(msg: NSLocalizedString("lock_status_36", comment: "lock_status_36"))
        }
        else if lockType == 0x41{
            showDetailMsg(msg: NSLocalizedString("lock_status_41", comment: "lock_status_41"))
        }else if lockType == 0x42{
            showDetailMsg(msg: NSLocalizedString("lock_status_42", comment: "lock_status_42"))
        }else if lockType == 0x43{
            showDetailMsg(msg: NSLocalizedString("lock_status_43", comment: "lock_status_43"))
        }else if lockType == 0x44{
            showDetailMsg(msg: NSLocalizedString("lock_status_44", comment: "lock_status_44"))
        }else if lockType == 0x45{
            showDetailMsg(msg: NSLocalizedString("lock_status_45", comment: "lock_status_45"))
        }else if lockType == 0x46{
            showDetailMsg(msg: NSLocalizedString("lock_status_46", comment: "lock_status_46"))
        }
        else if lockType == 0x51{
            showDetailMsg(msg: NSLocalizedString("lock_status_51", comment: "lock_status_51"))
        }else if lockType == 0x52{
            showDetailMsg(msg: NSLocalizedString("lock_status_52", comment: "lock_status_52"))
        }else if lockType == 0x53{
            showDetailMsg(msg: NSLocalizedString("lock_status_53", comment: "lock_status_53"))
        }else if lockType == 0x54{
            showDetailMsg(msg: NSLocalizedString("lock_status_54", comment: "lock_status_54"))
        }else if lockType == 0x55{
            showDetailMsg(msg: NSLocalizedString("lock_status_55", comment: "lock_status_55"))
        }else if lockType == 0x56{
            showDetailMsg(msg: NSLocalizedString("lock_status_56", comment: "lock_status_56"))
        }else if lockType == 0xff{
            showDetailMsg(msg: NSLocalizedString("pwd_error", comment: "pwd_error"))
        }
    }
}
