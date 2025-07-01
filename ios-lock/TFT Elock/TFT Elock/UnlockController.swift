//
//  UnlockController.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/5/6.
//  Copyright © 2025 com.tftiot. All rights reserved.
//


import Foundation
import UIKit
import CoreBluetooth
import CLXToast
import ActionSheetPicker_3_0

 


class UnlockController:UIViewController,BleStatusCallback,UpgradeStatusCallback{
    func onUpgradeStatus(status: Int, percent: Float) {
         
    }
    
    
    func onUpgradeNotifyValue(_ value: [UInt8]) {
        
    }
    
    func onNotifyValue(_ value: [UInt8]) {
        self.parseResp(respContent: value)
    }
    
    func onBleStatusCallback(_ connectStatus: Int) {
        switch connectStatus {
        case TftBleConnectManager.BLE_STATUS_OF_CLOSE:
            print("BLE Status: Closed")
            if self.waitingView != nil {
                self.waitingView.dismiss()
            }
            if self.progressView != nil{
                self.progressView.dismiss()
            }
            self.initRightBtn(isConnect:false)
        case TftBleConnectManager.BLE_STATUS_OF_DISCONNECT:
            print("BLE Status: Disconnected")
            if self.waitingView != nil {
                self.waitingView.dismiss()
            }
            if self.progressView != nil{
                self.progressView.dismiss()
            }
            self.initRightBtn(isConnect:false)
        case TftBleConnectManager.BLE_STATUS_OF_CONNECTING:
            print("BLE Status: Connecting")
            self.initRightBtn(isConnect:false)
        case TftBleConnectManager.BLE_STATUS_OF_CONNECT_SUCC:
            print("BLE Status: Connected Successfully")
            self.initRightBtn(isConnect:true)
        case TftBleConnectManager.BLE_STATUS_OF_SCANNING:
            print("BLE Status: Scanning")
            self.initRightBtn(isConnect:false)
        default:
            print("Unknown BLE Status")
        }
        
    }
    
    
    
    
    func parseResp(respContent:[UInt8]){
        var i = 0;
        while i + 3 <= respContent.count{
            var head = [respContent[i],respContent[i+1],respContent[i+2]]
            if Utils.arraysEqual(item1: head, item2: BleDeviceData.deviceReadyHead){
                if(i + 6 <= respContent.count){
                    var status = respContent[i+5]
                    if status == 0x01{
                        TftBleConnectManager.getInstance().setDeviceReady(ready: true)
                        TftBleConnectManager.getInstance().getLockStatus()
                        readOtherStatus()
                        DispatchQueue.main.async {
                            self.waitingView.dismiss() 
                        }
                    }
                }
                i = i + 6
            }else if Utils.arraysEqual(item1: head, item2: BleDeviceData.getLockStatusHead) || Utils.arraysEqual(item1: head, item2: BleDeviceData.getSubLockStatusHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        self.waitingView.dismiss()
                    }
                    self.parseLockType(lockType: lockType)
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: BleDeviceData.unlockHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        self.waitingView.dismiss()
                    }
                    self.parseLockType(lockType: lockType)
                }else{
                    let errorCode = respContent[i+3] & 0x7f;
                    if(errorCode == BleRespData.ERROR_CODE_OF_PWD_ERROR){
                        //                        Toast.makeText(EditActivity.this,R.string.pwd_error,Toast.LENGTH_SHORT).show();
                        Toast.hudBuilder.title(NSLocalizedString("pwd_error", comment: "Password is error")).show()
                    }
                    
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: BleDeviceData.lockHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        self.waitingView.dismiss()
                    }
                    self.parseLockType(lockType: lockType)
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: BleDeviceData.activeNetworkHead){
                if (i + 5 <= respContent.count){
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        self.waitingView.dismiss()
                    }
                    showDetailMsg(msg: NSLocalizedString("activeNetworkCmdSend", comment: "The activation network command has been sent successfully"))
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: BleDeviceData.uploadStatusHead){
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
                var dataList = Utils.parseRespContent(content: respContent)
                for bleRespData in dataList {
                    print("resp code:\(bleRespData.controlCode)")
                    if bleRespData.type == BleRespData.READ_TYPE || bleRespData.type == BleRespData.WRITE_TYPE {
                        
                    }else{
                        if bleRespData.errorCode == BleRespData.ERROR_CODE_OF_PWD_ERROR {
                            if bleRespData.controlCode == BleDeviceData.func_id_of_unlock || bleRespData.controlCode == BleDeviceData.func_id_of_sub_lock_unlock{
                                Toast.hudBuilder.title(NSLocalizedString("unlock_pwd_error", comment: "Ble password is error")).show()
                                showUnClockPwdWin()
                            }else{
                                Toast.hudBuilder.title(NSLocalizedString("ble_pwd_error", comment: "Ble password is error")).show()
                            }
                         
                        }else{
                            if TftBleConnectManager.getInstance().isOnThsView(){
                                Toast.hudBuilder.title(NSLocalizedString("fail", comment: "Fail")).show()
                            }
                        }
                        
                    }
                   
                }
                i = i + respContent.count
            }
        }
    }
    
    
    
     
    
    private var multiRespDataQueueMap: [Int: [BleRespData]] = [:]
    
    private func getMultiRespData(_ bleRespData: BleRespData, _ code: Int) -> [UInt8]? {
        if bleRespData.isEnd ?? false {
            var dataList: [BleRespData] = []
            if let queue = multiRespDataQueueMap[code] {
                for item in queue {
                    dataList.append(item)
                }
                dataList.append(bleRespData)
            } else {
                dataList.append(bleRespData)
            }
            let res = getAllDataBytes(dataList)
            multiRespDataQueueMap.removeValue(forKey: code)
            return res
        } else {
            if multiRespDataQueueMap[code] == nil {
                multiRespDataQueueMap[code] = []
            }
            multiRespDataQueueMap[code]?.append(bleRespData)
        }
        return nil
    }
    
     
    
    private func getAllDataBytes(_ dataList: [BleRespData]) -> [UInt8] {
        var result: [UInt8] = []
        for data in dataList {
            if let dataBytes = data.data {
                result.append(contentsOf: dataBytes)
            }
        }
        return result
    }
    
    var allCmdWaitingSendCount = 0
    var sendMsgThread: Thread?
    var canSendMsgLock = NSCondition()
    var bleSensorCount = 0
    private var progressView:AEAlertView!
    private var progressBar:MyProgress!
    private var waitingView:AEUIAlertView!
    private var pwdAlert:AEUIAlertView!
    private var blePwdAlert:AEUIAlertView!
    private var betaUpgradeWarningView:AEAlertView!
    private var upgradeWarningView:AEAlertView!
    var barLabel:UILabel!
    var blePwd:String!
    var newPwd:String!
    var cbPeripheral:CBPeripheral!
    var mac = ""
    var deviceType = ""
    var name = ""
    private var leaveViewNeedDisconnect = true
    private var initStart = false
    private var isUpgrade = false
    private var foundDevice = false
    private var connectControl = ""
    var lastCheckStatusDate:Date = Date()
    let getStatusTimeout = 4
    var sendMsgQueue:MsgQueue = MsgQueue()
    var sendMsgMultiQueue:MsgQueue = MsgQueue()
    var lockStatusImg:UIImageView!
    var lockStatusLabel:UILabel!
    
    
    var alarmOpenValue:Int! = 0
    private var accOn:Int!
    private var accOff:Int64!
    private var angle:Int!
    private var distance:Int!
    
    var model:String!
    var deviceId:String!
    var software:String!
    var imei:String!
    private var parentLockInitViewFuncs: [Int] = []
    private var subLockInitViewFuncs: [Int] = []
    
    private var uniqueID = ""
    override func viewDidDisappear(_ animated: Bool) {
        if self.leaveViewNeedDisconnect{
            TftBleConnectManager.getInstance().setOnThisView(false)
            TftBleConnectManager.getInstance().removeCallback(activityName: "UnlockController")
            TftBleConnectManager.getInstance().disconnect()
        }
    }
    private var a001SoftwareUpgradeManager:A001SoftwareUpgradeManager!
    override func viewDidLoad() {
        super.viewDidLoad()
        uniqueID = UniqueIDTool.getMediaDrmID()
       
        print("uid:\(uniqueID)")
        self.navigationController!.navigationBar.isTranslucent = false
        self.extendedLayoutIncludesOpaqueBars = true
        self.initUI()
        let mac = self.cbPeripheral.identifier.uuidString
        self.initNavBar()
        TftBleConnectManager.getInstance().initManager()
        TftBleConnectManager.getInstance().setOnThisView(true)
        TftBleConnectManager.getInstance().setCallback(activityName: "UnlockController", callback: self)
        TftBleConnectManager.getInstance().setIsNeedGetLockStatus(isNeedGetLockStatus: true)
        a001SoftwareUpgradeManager = A001SoftwareUpgradeManager(callback: self)
        TftBleConnectManager.getInstance().connect(connectPeripheral: self.cbPeripheral,isSubLock: BleDeviceData.isSubLockDevice(model: self.model))
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
        self.showWaitingWin(title: NSLocalizedString("connecting", comment: "Connecting"))
        TftBleConnectManager.getInstance().disconnect()
        TftBleConnectManager.getInstance().connect(connectPeripheral: self.cbPeripheral,isSubLock: BleDeviceData.isSubLockDevice(model: self.model))
        
    }
    func asciiStringToBytes(str: String) -> [UInt8]{
        var bytes: [UInt8] = []
        for character in str.unicodeScalars {
            print(character)
            print(character.value)
        }
        return bytes
    }
    func showUnClockPwdWin(){
        if self.blePwdAlert != nil && !self.blePwdAlert.isDismiss{
            self.blePwdAlert.show()
            return
        }
        self.blePwdAlert = AEUIAlertView(style: .password, title: NSLocalizedString("input_ble_open_lock_pwd", comment:"Please enter your password"), message: nil)
        self.blePwdAlert.textField.placeholder = NSLocalizedString("input_ble_open_lock_pwd", comment:"Please enter your password")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.blePwdAlert.dismiss()
            
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let pwd = String(self.blePwdAlert.textField.text ?? "")
            if pwd.count == 6{
                self.blePwdAlert.dismiss()
                let pwdArray = [UInt8](pwd.utf8)
                if BleDeviceData.isSubLockDevice(model: self.model){
                    var needSend:[UInt8] = []
                    needSend.append(UInt8(2))
                    needSend.append(contentsOf: pwdArray)
                    needSend.append(contentsOf: Utils.hexString2Bytes(hexStr: self.uniqueID))
                    let content = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_unlock, content: needSend, pwd: nil)
                    TftBleConnectManager.getInstance().writeArrayContent(writeContentList: content)
                }else{
                    var needSend = self.getCmdContent(head: BleDeviceData.unlockHead, content: pwdArray,isNeedUniqueID: true)
                    TftBleConnectManager.getInstance().writeContent(content: needSend)
                }
             
            }else{
                Toast.hudBuilder.title(NSLocalizedString("pwd_format_error", comment: "Value is incorrect!The length has to be 6 digits")).show()
            }
        }
        self.blePwdAlert.addAction(action: action_one)
        self.blePwdAlert.addAction(action: action_two)
        self.blePwdAlert.show()
    }
    private var isShowPwdDlg = false
    func showAccessPwdWin(){
        if !BleDeviceData.isSupportConfig(model: model, version: software, deviceId: deviceId){
            return
        }
        if self.pwdAlert != nil && !self.pwdAlert.isDismiss{
            if !self.isShowPwdDlg{
                self.pwdAlert.show()
                self.isShowPwdDlg = true
            }
            return
        }
        self.pwdAlert = AEUIAlertView(style: .password, title: NSLocalizedString("input_ble_pwd", comment:"Please enter your bletooth password"), message: nil)
        self.pwdAlert.textField.placeholder = NSLocalizedString("input_ble_pwd", comment:"Please enter your bletooth password")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.isShowPwdDlg = false
            self.pwdAlert.dismiss()
            self.navigationController?.popViewController(animated: true)
            
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let pwd = String(self.pwdAlert.textField.text ?? "")
            if pwd.count == 6{
                self.isShowPwdDlg = false
                self.pwdAlert.dismiss()
                self.blePwd = pwd
                self.changeBlePwd(pwd, pwd)
            }else{
                Toast.hudBuilder.title(NSLocalizedString("pwd_format_error", comment: "Value is incorrect!The length has to be 6 digits")).show()
            }
        }
        self.pwdAlert.addAction(action: action_one)
        self.pwdAlert.addAction(action: action_two)
        if !self.isShowPwdDlg{
            self.pwdAlert.show()
            self.isShowPwdDlg = true
        }
    }
    private func changeBlePwd(_ oldPwd: String, _ newPwd: String) {
        do {
            var outputStream: [UInt8] = []
            outputStream.append(contentsOf: oldPwd.utf8)
            outputStream.append(contentsOf: newPwd.utf8)
            
            self.newPwd = newPwd
            
            let arrayList = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_ble_pwd_change, content: outputStream,pwd:nil)
            TftBleConnectManager.getInstance().writeArrayContent(writeContentList: arrayList)
        } catch {
            print("Error changing BLE password: \(error)")
        }
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
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(130)) {
            if !self.waitingView.isDismiss{
                Toast.hudBuilder.title("Timeout, please try again!").show()
            }
            self.waitingView.dismiss()
        }
    }
    
    func sendCheckDeviceReadyCmd(){
        TftBleConnectManager.getInstance().writeContent(content: BleDeviceData.deviceReadyHead)
    }
    var activeNetworkBtn:UIButton!
    var unlockBtn:UIButton!
    var unlockBtnY:Int!
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
    
     
     
    func notUpdateInit(){
        
    } 
    
    private func fixTime() {
        // Placeholder implementation
        let now = Date()
        let timeData = Utils.formatHex(intValue: Int(now.timeIntervalSince1970), len: 4)
        var data = [UInt8]()
        data.append(UInt8(0))
        data.append(contentsOf: timeData)
        let content = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_datetime, content: data,pwd: nil)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: content)
    }
    
    func readOtherStatus() { 
        if BleDeviceData.isSubLockDevice(model: model) {
            fixTime()
        }
         
    }
    private func initDiffUI() {  
        if BleDeviceData.isSubLockDevice(model: model) { 
            activeNetworkBtn.isHidden = true
            unlockBtn.frame = CGRect(x: (Int)(self.view.bounds.size.width / 2  - 40), y: unlockBtnY, width: 80, height: 30) 
        } else {
             
            activeNetworkBtn.isHidden = false
            unlockBtn.frame = CGRect(x: (Int)(self.view.bounds.size.width / 2 / 2  - 40), y: unlockBtnY, width: 80, height: 30)
            
        }
    } 
    func initUI(){
        self.view.backgroundColor = UIColor.white
        var contentY = 70
        let dateLabelWidth = 60
        let dateLabelX:Int = (Int)(self.view.bounds.size.width / 2 - 100)
        self.uniqueIDLabel.frame = CGRect(x: dateLabelX, y: contentY, width: 80, height: 30)
        self.uniqueIDLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.uniqueIDDescLabel.frame = CGRect(x: dateLabelX+80, y: contentY, width: 200, height: 30)
        self.uniqueIDDescLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.uniqueIDDescLabel.text = uniqueID
        self.view.addSubview(self.uniqueIDLabel)
        self.view.addSubview(self.uniqueIDDescLabel)
        contentY = contentY + 25
        self.dateLabel.frame = CGRect(x: dateLabelX, y: contentY, width: 60, height: 30)
        self.dateLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.dateDescLabel.frame = CGRect(x: dateLabelX+60, y: contentY, width: 200, height: 30)
        self.dateDescLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.view.addSubview(self.dateLabel)
        self.view.addSubview(self.dateDescLabel)
        contentY = contentY + 25
        lockStatusImg = UIImageView.init(frame: CGRect(x: (Int)(self.view.bounds.size.width / 2 - 75), y: contentY, width: 150, height: 150))
        lockStatusImg.contentMode = .scaleAspectFit;
        lockStatusImg.image = UIImage (named: "ic_lock.png")
        lockStatusImg.backgroundColor = UIColor.clear
        contentY = contentY + 150
        self.view.addSubview(lockStatusImg)
        self.lockStatusLabel = UILabel.init(frame: CGRect(x: 30, y: contentY, width: Int(self.view.bounds.size.width)-60, height: 60))
        self.lockStatusLabel.textAlignment = .center
        self.lockStatusLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.lockStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping
        self.lockStatusLabel.numberOfLines = 0
        self.view.addSubview(self.lockStatusLabel)
        contentY = contentY + 50
        //        let lockBtn = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width / 3 / 2 - 40), y: contentY, width: 80, height: 30))
        //        lockBtn.setTitle( NSLocalizedString("lock", comment: "Lock"), for: .normal)
        //        lockBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        //        lockBtn.layer.cornerRadius = 15;
        //        lockBtn.layer.borderWidth = 1.0;
        //        lockBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        //        lockBtn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        //        lockBtn.addTarget(self, action: #selector(lockClick), for:.touchUpInside)
        //        self.view.addSubview(lockBtn)
        unlockBtnY = contentY
        unlockBtn = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width / 2 / 2  - 40), y: unlockBtnY, width: 80, height: 30))
        unlockBtn.setTitle( NSLocalizedString("unlock", comment: "Unlock"), for: .normal)
        unlockBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        unlockBtn.layer.cornerRadius = 15;
        unlockBtn.layer.borderWidth = 1.0;
        unlockBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        unlockBtn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        unlockBtn.addTarget(self, action: #selector(unlockClick), for:.touchUpInside)
        self.view.addSubview(unlockBtn)
        //        let activeNetwork = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width -  self.view.bounds.size.width / 3 / 2 - 40), y: contentY, width: 80, height: 30))
        activeNetworkBtn = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width - self.view.bounds.size.width / 2 / 2  - 40), y: contentY, width: 80, height: 30))
        activeNetworkBtn.setTitle( NSLocalizedString("active_network", comment: "Active Network"), for: .normal)
        activeNetworkBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        activeNetworkBtn.layer.cornerRadius = 15;
        activeNetworkBtn.layer.borderWidth = 1.0;
        activeNetworkBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        activeNetworkBtn.addTarget(self, action: #selector(activeNetworkClick), for:.touchUpInside)
        activeNetworkBtn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.view.addSubview(activeNetworkBtn)
        contentY = contentY + 30
        self.initDiffUI()
         
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
        var needSend = self.getCmdContent(head: BleDeviceData.lockHead, content: [0x00],isNeedUniqueID: true)
        TftBleConnectManager.getInstance().writeContent(content: needSend)
    }
    @objc private func unlockClick() {
        showUnClockPwdWin()
    }
    
    @objc private func activeNetworkClick() {
        var needSend = self.getCmdContent(head: BleDeviceData.activeNetworkHead, content: [0x00],isNeedUniqueID: true)
        TftBleConnectManager.getInstance().writeContent(content: needSend)
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
            lockType == 0x54 ||
            lockType == 0x17 ||
            lockType == 0x27 ||
            lockType == 0x37 ||
            lockType == 0x47 ||
            lockType == 0x57
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
        if (lockType == 0xff || lockType == 0x04 || lockType == 0x09 || lockType == 0x0a){
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
        }else if lockType == 0x0a{
            showDetailMsg(msg: NSLocalizedString("lock_status_0a", comment: "lock_status_0a"))
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
        }else if lockType == 0x17{
            showDetailMsg(msg: NSLocalizedString("lock_status_17", comment: "lock_status_17"))
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
        }else if lockType == 0x27{
            showDetailMsg(msg: NSLocalizedString("lock_status_27", comment: "lock_status_27"))
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
        }else if lockType == 0x37{
            showDetailMsg(msg: NSLocalizedString("lock_status_37", comment: "lock_status_37"))
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
        }else if lockType == 0x47{
            showDetailMsg(msg: NSLocalizedString("lock_status_47", comment: "lock_status_47"))
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
        }else if lockType == 0x57{
            showDetailMsg(msg: NSLocalizedString("lock_status_57", comment: "lock_status_57"))
        }else if lockType == 0xff{
            showDetailMsg(msg: NSLocalizedString("pwd_error", comment: "pwd_error"))
        }
    }
} 
extension UnlockController:SetConnectStatusDelegate{
    func setConnectStatus() {
        TftBleConnectManager.getInstance().setOnThisView(true)
        self.leaveViewNeedDisconnect = true
    }
} 
