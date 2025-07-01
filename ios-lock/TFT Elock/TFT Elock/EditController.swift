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


class EditController:UIViewController,BleStatusCallback,URLSessionDownloadDelegate,UpgradeStatusCallback{
    func onUpgradeStatus(status: Int, percent: Float) {
        switch status{
        case A001SoftwareUpgradeManager.STATUS_OF_FINE_NOT_FIND:
            print("A001SoftwareUpgradeManager.STATUS_OF_FINE_NOT_FIND")
            Toast.hudBuilder.title(NSLocalizedString("download_file_fail", comment: "Password is error")).show()
            upgradeError()
        case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR:
            print("A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR")
            Toast.hudBuilder.title(NSLocalizedString("download_file_fail", comment: "Password is error")).show()
            upgradeError()
        case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_ERROR_UPGRADE_FILE:
            print("A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_ERROR_UPGRADE_FILE")
            Toast.hudBuilder.title(NSLocalizedString("error_upgrade_file", comment: "Password is error")).show()
            upgradeError()
        case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_SUCC:
            print("A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_SUCC")
            
            upgradeSucc()
        default:
            self.progressBar.setValue(Float(percent),animated: true)
        }
    }
    func upgradeError(){
        TftBleConnectManager.getInstance().setEnterUpgrade(enterUpgrade: false)
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(3)) {
            self.progressView.dismiss()
        }
    }
    
    func upgradeSucc(){
        TftBleConnectManager.getInstance().setEnterUpgrade(enterUpgrade: false)
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(3)) {
            self.progressView.dismiss()
            Toast.hudBuilder.title(NSLocalizedString("upgrade_succ", comment: "Upgrade succ")).show()
            self.navigationController?.popViewController(animated: true)
        }
//        TftBleConnectManager.getInstance().disconnect()
//        TftBleConnectManager.getInstance().connect(connectPeripheral: self.cbPeripheral,isSubLock: BleDeviceData.isSubLockDevice(model: self.model))
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
            if self.waitingCancelView != nil{
                self.waitingCancelView.dismiss()
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
            if self.waitingCancelView != nil{
                self.waitingCancelView.dismiss()
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
                            if self.waitingView != nil{
                                self.waitingView.dismiss()
                            }
                            if self.waitingCancelView != nil{
                                self.waitingCancelView.dismiss()
                            }
                            self.showAccessPwdWin()
                        }
                    }
                }
                i = i + 6
            }else if Utils.arraysEqual(item1: head, item2: BleDeviceData.getLockStatusHead) || Utils.arraysEqual(item1: head, item2: BleDeviceData.getSubLockStatusHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        if self.waitingView != nil{
                            self.waitingView.dismiss()
                        }
                        if self.waitingCancelView != nil{
                            self.waitingCancelView.dismiss()
                        }
                    }
                    self.parseLockType(lockType: lockType)
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: BleDeviceData.unlockHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        if self.waitingView != nil{
                            self.waitingView.dismiss()
                        }
                        if self.waitingCancelView != nil{
                            self.waitingCancelView.dismiss()
                        }
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
                        if self.waitingView != nil{
                            self.waitingView.dismiss()
                        }
                        if self.waitingCancelView != nil{
                            self.waitingCancelView.dismiss()
                        }
                    }
                    self.parseLockType(lockType: lockType)
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: BleDeviceData.activeNetworkHead){
                if (i + 5 <= respContent.count){
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        if self.waitingView != nil{
                            self.waitingView.dismiss()
                        }
                        if self.waitingCancelView != nil{
                            self.waitingCancelView.dismiss()
                        }
                    }
                    showDetailMsg(msg: NSLocalizedString("activeNetworkCmdSend", comment: "The activation network command has been sent successfully"))
                }
                i+=5
            }else if Utils.arraysEqual(item1: head, item2: BleDeviceData.uploadStatusHead){
                if (i + 5 <= respContent.count){
                    var lockType = respContent[i+4]
                    self.setLockRefreshTime()
                    DispatchQueue.main.async {
                        if self.waitingView != nil{
                            self.waitingView.dismiss()
                        }
                        if self.waitingCancelView != nil{
                            self.waitingCancelView.dismiss()
                        }
                    }
                    self.parseLockType(lockType: lockType)
                }
                i+=5
            }else{
                var dataList = Utils.parseRespContent(content: respContent)
                for bleRespData in dataList {
                    print("resp code:\(bleRespData.controlCode)")
                    if bleRespData.type == BleRespData.READ_TYPE || bleRespData.type == BleRespData.WRITE_TYPE {
                        self.parseReadResp(bleRespData: bleRespData)
                    }else{
                        if bleRespData.errorCode == BleRespData.ERROR_CODE_OF_PWD_ERROR {
                            Toast.hudBuilder.title(NSLocalizedString("ble_pwd_error", comment: "Ble password is error")).show() 
                            showAccessPwdWin()
                        }else{
                            if TftBleConnectManager.getInstance().isOnThsView(){
                                Toast.hudBuilder.title(NSLocalizedString("fail", comment: "Fail")).show()
                            }
                        }
                        if bleRespData.controlCode == BleDeviceData.func_id_of_sub_lock_led {
                            ledSwitch.isOn = !ledSwitch.isOn
                        }else if bleRespData.controlCode == BleDeviceData.func_id_of_sub_lock_buzzer {
                            buzzerSwitch.isOn = !buzzerSwitch.isOn
                        }else if bleRespData.controlCode == BleDeviceData.func_id_of_sub_lock_long_range {
                            longRangeSwitch.isOn = !longRangeSwitch.isOn
                        }
                    }
                   
                }
                i = i + respContent.count
            }
        }
    }
    
    
    
    func parseReadResp(bleRespData:BleRespData){
        let code = bleRespData.controlCode
        if code == 1 {
            if bleRespData.data![1] == 0x01 {
                TftBleConnectManager.getInstance().setDeviceReady(ready: true)
            }
        }else if code == BleDeviceData.func_id_of_ip1{
            parseServerConfig(bleRespData, BleDeviceData.func_id_of_ip1)
        }else if code == BleDeviceData.func_id_of_ip2{
            parseServerConfig(bleRespData, BleDeviceData.func_id_of_ip2)
        }else if code == BleDeviceData.func_id_of_apn_addr{
            let respData = getMultiRespData(bleRespData, BleDeviceData.func_id_of_apn_addr)
            if respData == nil{
                return
            }
            if respData != nil{
                if respData?.count == 1 && respData![0] == 0x00{
                    apnAddrContentLabel.text = ""
                }else{
                    let value = String(bytes: respData!, encoding: .utf8) ?? ""
                    apnAddrContentLabel.text = value
                }
            }
        }else if code == BleDeviceData.func_id_of_apn_username{
            let respData = getMultiRespData(bleRespData, BleDeviceData.func_id_of_apn_username)
            if respData == nil{
                return
            }
            if respData != nil{
                if respData?.count == 1 && respData![0] == 0x00{
                    apnUsernameContentLabel.text = ""
                }else{
                    let value = String(bytes: respData!, encoding: .utf8) ?? ""
                    apnUsernameContentLabel.text = value
                }
            }
        }else if code == BleDeviceData.func_id_of_apn_pwd{
            let respData = getMultiRespData(bleRespData, BleDeviceData.func_id_of_apn_pwd)
            if respData == nil{
                return
            }
            if respData != nil{
                if respData?.count == 1 && respData![0] == 0x00{
                    apnPwdContentLabel.text = ""
                }else{
                    let value = String(bytes: respData!, encoding: .utf8) ?? ""
                    apnPwdContentLabel.text = value
                }
            }
        }else if code == BleDeviceData.func_id_of_timer{
            self.accOn = Utils.bytes2Short(bytes: bleRespData.data!, offset: 0)
            self.accOff = Utils.unsigned4BytesToInt(bleRespData.data!, 2)
            self.angle = (Int)(bleRespData.data![6])
            self.distance = Utils.bytes2Short(bytes: bleRespData.data!, offset: 7)
            let value = "\(self.accOn):\(self.accOff):\(self.angle):\(self.distance)"
            if let accOn = self.accOn, let accOff = self.accOff, let angle = self.angle, let distance = self.distance {
                timerContentLabel.text = "\(accOn):\(accOff):\(angle):\(distance)"
            } else {
                // 处理可选类型为 nil 的情况
                timerContentLabel.text = ""
            }
        }else if code == BleDeviceData.func_id_of_ble_pwd_change{
            self.blePwd = newPwd
            Toast.hudBuilder.title(NSLocalizedString("success", comment: "Success")).show()
        }else if code == BleDeviceData.func_id_of_change_unlock_pwd{
            Toast.hudBuilder.title(NSLocalizedString("success", comment: "Success")).show()
        }else if code == BleDeviceData.func_id_of_sub_lock_led{
            ledSwitch.isOn = bleRespData.data![0] == 0x01
        }else if code == BleDeviceData.func_id_of_sub_lock_buzzer{
            buzzerSwitch.isOn = bleRespData.data![0] == 0x01
        }else if code == BleDeviceData.func_id_of_sub_lock_long_range{
            longRangeSwitch.isOn = bleRespData.data![0] == 0x01
        }else if code == BleDeviceData.func_id_of_sub_lock_device_name{
            let respData = getMultiRespData(bleRespData, BleDeviceData.func_id_of_sub_lock_device_name)
            let value = String(bytes: respData!, encoding: .utf8) ?? ""
            nameContentLabel.text = value
        }else if code == BleDeviceData.func_id_of_sub_lock_version{
            let respData = bleRespData.data!
            if respData.count < 5{
                return
            }
            if respData[0] == 0x0b{
                model = BleDeviceData.MODEL_OF_SGX120B01
            }else{
                model = ""
            }
            modelContentLabel.text = model
            let hardware = Utils.parseHardwareVersion(respData[1])
            let software = Utils.parseSoftwareVersion(respData, 2)
            hardwareContentLabel.text = hardware
            softwareContentLabel.text = software
            initDiffUI()
            
        }else if code == BleDeviceData.func_id_of_sub_lock_boot_version {
            guard let respData = bleRespData.data, !respData.isEmpty else {
                return
            }
            
            if respData.count >= 2 {
                let firstByte = Int(respData[0])
                let remainingData = String(bytes: respData[1...], encoding: .utf8) ?? ""
                bootVersionContentLabel.text = "\(firstByte) \(remainingData)"
            } else {
                let firstByte = Int(respData[0])
                bootVersionContentLabel.text = "\(firstByte)"
            }
        }else if code == BleDeviceData.func_id_of_sub_lock_temp_alarm_set{
            do{
                tempAlarmHigh = Float(try Utils.convertBigEndianToSignedInt(bleRespData.data!,offset: 0));
                tempAlarmLow = Float(try Utils.convertBigEndianToSignedInt(bleRespData.data!,offset: 2));
                var value = "\(Utils.getCurTemp(sourceTemp: tempAlarmLow / 100))\(Utils.getCurTempUnit()) ~ \(Utils.getCurTemp(sourceTemp: tempAlarmHigh / 100))\(Utils.getCurTempUnit())"
                tempAlarmContentLabel.text = value
            }catch {
                
            }
        }else if code == BleDeviceData.func_id_of_sub_lock_alarm_open_set{
            alarmOpenValue = Utils.bytes2Short(bytes: bleRespData.data!, offset: 0)
            if bleRespData.type == BleRespData.WRITE_TYPE{
                Toast.hudBuilder.title(NSLocalizedString("success", comment: "Success")).show()
            }
        }else if code == BleDeviceData.func_id_of_sub_lock_ble_transmitted_power{
            transmittedPowerContentLabel.text = "\(bleRespData.data![0]) dBm"
        }else if code == BleDeviceData.func_id_of_sub_lock_broadcast_interval{
            broadcastIntervalContentLabel.text = "\(bleRespData.data![0]) s"
        }else if code == BleDeviceData.func_id_of_sub_lock_device_id{
            let respData = getMultiRespData(bleRespData, BleDeviceData.func_id_of_sub_lock_device_id)
            if respData == nil{
                return
            }
            let value = Utils.bytes2HexString(bytes: respData!, pos: 0)
            deviceIdContentLabel.text = value
        }else if code == BleDeviceData.func_id_of_clear_his_data{
            Toast.hudBuilder.title(NSLocalizedString("success", comment: "Success")).show()
        }else if code == BleDeviceData.func_id_of_reset_default{
            if waitCheckResetDefaultValueItems.isEmpty{
                Toast.hudBuilder.title(NSLocalizedString("success", comment: "Success")).show()
                self.readOtherStatus()
                DispatchQueue.main.async {
                    if self.waitingView != nil{
                        self.waitingView.dismiss()
                    }
                    if self.waitingCancelView != nil{
                        self.waitingCancelView.dismiss()
                    }
                }
            }else{
                self.doResetDefaultValue()
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
    
    private func parseServerConfig(_ bleRespData: BleRespData, _ code: Int) {
        guard let respData = getMultiRespData(bleRespData, code) else {
            return
        }
        
        let port = Utils.bytes2Short(bytes: respData, offset: 0)
        let ipType = respData[2]
        let domainByte = Array(respData[3..<respData.count])
        var domain = ""
        
        if ipType == 0x01 {
            let addr1 = Int(respData[3]) >= 0 ? Int(respData[3]) : Int(respData[3]) + 256
            let addr2 = Int(respData[4]) >= 0 ? Int(respData[4]) : Int(respData[4]) + 256
            let addr3 = Int(respData[5]) >= 0 ? Int(respData[5]) : Int(respData[5]) + 256
            let addr4 = Int(respData[6]) >= 0 ? Int(respData[6]) : Int(respData[6]) + 256
            domain = "\(addr1).\(addr2).\(addr3).\(addr4)"
        } else {
            domain = String(bytes: domainByte, encoding: .utf8) ?? ""
        }
        
        let ipCmdStr = "\(code)_0"
        let portCmdStr = "\(code)_1"
        print("\(ipCmdStr):\(domain) ; \(portCmdStr):\(port)")
        switch code {
        case BleDeviceData.func_id_of_ip1:
            // Set text to UI elements (placeholder)
            print("IP1: \(domain), Port1: \(port)")
            ip1ContentLabel.text = domain
            port1ContentLabel.text = String(port)
        case BleDeviceData.func_id_of_ip2:
            // Set text to UI elements (placeholder)
            print("IP2: \(domain), Port2: \(port)")
            ip2ContentLabel.text = domain
            port2ContentLabel.text = String(port)
        default:
            break
        }
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
    private var waitingCancelView:AEUIAlertView!
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
            TftBleConnectManager.getInstance().removeCallback(activityName: "EditController")
            TftBleConnectManager.getInstance().disconnect()
        }
    }
    private var a001SoftwareUpgradeManager:A001SoftwareUpgradeManager!
    override func viewDidLoad() {
        super.viewDidLoad()
        uniqueID = UniqueIDTool.getMediaDrmID()
        parentLockInitViewFuncs = [
            BleDeviceData.func_id_of_timer,
            BleDeviceData.func_id_of_ip1,
            BleDeviceData.func_id_of_ip2,
            BleDeviceData.func_id_of_apn_addr,
            BleDeviceData.func_id_of_apn_username,
            BleDeviceData.func_id_of_apn_pwd
        ]
        
        subLockInitViewFuncs = [
            BleDeviceData.func_id_of_sub_lock_version,
            BleDeviceData.func_id_of_sub_lock_device_name,
            BleDeviceData.func_id_of_sub_lock_broadcast_interval,
            BleDeviceData.func_id_of_sub_lock_long_range,
            BleDeviceData.func_id_of_sub_lock_ble_transmitted_power,
            BleDeviceData.func_id_of_sub_lock_led,
            BleDeviceData.func_id_of_sub_lock_boot_version,
            BleDeviceData.func_id_of_sub_lock_buzzer,
            BleDeviceData.func_id_of_sub_lock_device_id,
            BleDeviceData.func_id_of_sub_lock_alarm_open_set,
            BleDeviceData.func_id_of_sub_lock_temp_alarm_set
        ]
        print("uid:\(uniqueID)")
        self.navigationController!.navigationBar.isTranslucent = false
        self.extendedLayoutIncludesOpaqueBars = true
        self.initUI()
        let mac = self.cbPeripheral.identifier.uuidString
        self.initNavBar()
        TftBleConnectManager.getInstance().initManager()
        TftBleConnectManager.getInstance().setOnThisView(true)
        TftBleConnectManager.getInstance().setCallback(activityName: "EditController", callback: self)
        TftBleConnectManager.getInstance().setIsNeedGetLockStatus(isNeedGetLockStatus: false)
        a001SoftwareUpgradeManager = A001SoftwareUpgradeManager(callback: self)
        TftBleConnectManager.getInstance().connect(connectPeripheral: self.cbPeripheral,isSubLock: BleDeviceData.isSubLockDevice(model: self.model))
        self.showWaitingCancelWin(title: NSLocalizedString("connecting", comment: "Connecting"))
        print("view did load")
        self.checkUpdate(isEnterCheck: true)
        self.checkBetaUpdate()
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
        self.showWaitingCancelWin(title: NSLocalizedString("connecting", comment: "Connecting"))
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
                    let content = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_unlock, content: needSend, pwd: nil)
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
    
    private func writeDomain(ipType:Int,ip:String,port: Int){
        var funcId = BleDeviceData.func_id_of_ip1
        if ipType == 2{
            funcId = BleDeviceData.func_id_of_ip2
        }
        let isIpMode = Utils.isIpMode(domain: ip)
        var outputStream: [UInt8] = []
        outputStream.append(contentsOf: Utils.short2Bytes(number: port))
        outputStream.append(contentsOf: [isIpMode ? 0x01 : 0x00])
        outputStream.append(contentsOf: Utils.getDomainByte(isIpModeBool: isIpMode, domain: ip))
        let arrayList = Utils.getWriteCmdContent(cmdCode: funcId, content: outputStream,pwd:self.blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: arrayList)
        
    }
    
    private func writeAlarmOpenSet(alarmOpenSet:Int){
        var outputStream: [UInt8] = Utils.short2Bytes(number: alarmOpenSet)
        let arrayList = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_alarm_open_set, content: outputStream,pwd:self.blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: arrayList)
    }
    private func writeTempAlarm(tempHigh:Int,tempLow:Int){
        var outputStream: [UInt8] = []
        outputStream.append(contentsOf: Utils.convertSignedIntToBigEndian(tempHigh))
        outputStream.append(contentsOf: Utils.convertSignedIntToBigEndian(tempLow))
        let arrayList = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_temp_alarm_set, content: outputStream,pwd:self.blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: arrayList)
    }
    private func writeTimer(accOn:Int,accOff:Int64,angle:Int,distance: Int){
        var outputStream: [UInt8] = []
        outputStream.append(contentsOf: Utils.short2Bytes(number: accOn))
        outputStream.append(contentsOf: Utils.unSignedInt2Bytes(accOff))
        outputStream.append(contentsOf: [(UInt8)(angle)])
        outputStream.append(contentsOf: Utils.short2Bytes(number: distance))
        let arrayList = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_timer, content: outputStream,pwd:self.blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: arrayList)
    }
    
    private func changeUnlockPwd(_ oldPwd: String, _ newPwd: String) {
        do {
            var outputStream: [UInt8] = []
            outputStream.append(contentsOf: oldPwd.utf8)
            outputStream.append(contentsOf: newPwd.utf8)
            
            let arrayList = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_change_unlock_pwd, content: outputStream,pwd:self.blePwd)
            
            TftBleConnectManager.getInstance().writeArrayContent(writeContentList: arrayList)
        } catch {
            print("Error changing BLE password: \(error)")
        }
    }
    func showWaitingWin(title:String){
        if self.waitingView != nil && !self.waitingView.isDismiss{
            self.waitingView.title = title
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
//        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(130)) {
//            if !self.waitingView.isDismiss{
//                Toast.hudBuilder.title("Timeout, please try again!").show()
//            }
//            self.waitingView.dismiss()
//        }
    }
    func showWaitingCancelWin(title:String){
            if self.waitingCancelView != nil && !self.waitingCancelView.isDismiss{
                self.waitingCancelView.title = title
                self.waitingCancelView.show()
                return
            }
            self.waitingCancelView = AEUIAlertView(style: .textField, title: title, message: nil)
            self.waitingCancelView.actions = []
            self.waitingCancelView.resetActions()
            
            let animation = UIView(frame: CGRect(x: 0, y: 0, width: 80, height: 80))
            let anim = AEBeginLineAnimation.initShow(in: animation.bounds, lineWidth: 4, lineColor: UIColor.blue)
            animation.addSubview(anim)
            let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
                   
                   self.waitingCancelView.dismiss()
                   self.navigationController?.popViewController(animated: true)
                   
               }
        self.waitingCancelView.addAction(action: action_one)
            self.waitingCancelView.textField.isHidden = true
            self.waitingCancelView.set(animation: animation, width: 80, height: 80)
            self.waitingCancelView.show()
          
        }
    
    func sendCheckDeviceReadyCmd(){
        TftBleConnectManager.getInstance().writeContent(content: BleDeviceData.deviceReadyHead)
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
    var timerView:UIView!
    var rfidView:UIView!
    var subLockView:UIView!
    var ip1View:UIView!
    var ip2View:UIView!
    var port1View:UIView!
    var port2View:UIView!
    var apnAddrView:UIView!
    var apnPwdView:UIView!
    var apnUsernameView:UIView!
    var softwareView:UIView!
    var rebootView:UIView!
    var shutdownView:UIView!
    var resetFactoryView:UIView!
    var longRangeView:UIView!
    var ledView:UIView!
    var buzzerView:UIView!
    var modelView:UIView!
    var hardwareView:UIView!
    var nameView:UIView!
    var deviceIdView:UIView!
    var blePwdView:UIView!
    var unclockPwdView:UIView!
    var bootVersionView:UIView!
    var alarmSetView:UIView!
    var tempAlarmView:UIView!
    var broadcastIntervalView:UIView!
    var transmittedPowerView:UIView!
    var resetDefaultView:UIView!
    var clearHisDataView :UIView!
    
    var nameLabel:UILabel!
    var nameContentLabel:UILabel!
    var editNameBtn:UIButton!
    var deviceIdLabel:UILabel!
    var deviceIdContentLabel:UILabel!
    var editDeviceIdBtn:UIButton!
    var modelLabel:UILabel!
    var modelContentLabel:UILabel!
    var debugUpgradeBtn:UIButton!
    var hardwareLabel:UILabel!
    var hardwareContentLabel:UILabel!
    var betaUpgradeBtn:UIButton!
    var softwareLabel:UILabel!
    var softwareContentLabel:UILabel!
    var editSoftwareBtn:UIButton!
    var bootVersionLabel:UILabel!
    var bootVersionContentLabel:UILabel!
    var editBootVersionBtn:UIButton!
    var timerLabel:UILabel!
    var timerContentLabel:UILabel!
    var editTimerBtn:UIButton!
    var rfidLabel:UILabel!
    var rfidContentLabel:UILabel!
    var editRfidBtn:UIButton!
    var subLockLabel:UILabel!
    var subLockContentLabel:UILabel!
    var editSubLockBtn:UIButton!
    var ip1Label:UILabel!
    var ip1ContentLabel:UILabel!
    var editIp1Btn:UIButton!
    var port1Label:UILabel!
    var port1ContentLabel:UILabel!
    var editPort1Btn:UIButton!
    var ip2Label:UILabel!
    var ip2ContentLabel:UILabel!
    var editIp2Btn:UIButton!
    var port2Label:UILabel!
    var port2ContentLabel:UILabel!
    var editPort2Btn:UIButton!
    var apnAddrLabel:UILabel!
    var apnAddrContentLabel:UILabel!
    var editApnAddrBtn:UIButton!
    var apnUsernameLabel:UILabel!
    var apnUsernameContentLabel:UILabel!
    var editApnUsernameBtn:UIButton!
    var apnPwdLabel:UILabel!
    var apnPwdContentLabel:UILabel!
    var editApnPwdBtn:UIButton!
    var rebootLabel:UILabel!
    var editRebootBtn:UIButton!
    var shutdownLabel:UILabel!
    var editShutdownBtn:UIButton!
    var resetFactoryLabel:UILabel!
    var editResetFactoryBtn:UIButton!
    var longRangeLabel:UILabel!
    var longRangeSwitch:UISwitch!
    var ledLabel:UILabel!
    var ledSwitch:UISwitch!
    var buzzerLabel:UILabel!
    var buzzerSwitch:UISwitch!
    var blePwdLabel:UILabel!
    var blePwdContentLabel:UILabel!
    var editBlePwdBtn:UIButton!
    var unclockPwdLabel:UILabel!
    var unclockPwdContentLabel:UILabel!
    var editUnclockPwdBtn:UIButton!
    var activeNetworkBtn:UIButton!
    var unlockBtn:UIButton!
    var unlockBtnY:Int!
    var alarmSetLabel:UILabel!
    var alarmSetContentLabel:UILabel!
    var editAlarmSetBtn:UIButton!
    var tempAlarmLabel:UILabel!
    var tempAlarmContentLabel:UILabel!
    var editTempAlarmBtn:UIButton!
    var broadcastIntervalLabel:UILabel!
    var broadcastIntervalContentLabel:UILabel!
    var editBroadcastIntervalBtn:UIButton!
    var transmittedPowerLabel:UILabel!
    var transmittedPowerContentLabel:UILabel!
    var editTransmittedPowerBtn:UIButton!
    var resetDefaultLabel:UILabel!
    var editResetDefaultBtn:UIButton!
    var clearHisDataLabel:UILabel!
    var editClearHisDataBtn:UIButton!
    var configScrollView:UIScrollView!
    var upgradePackUrl:String!
    var netSoftwareVersion:String!
    var upgradePackageLink:String!
    var tempAlarmHigh:Float!
    var tempAlarmLow:Float!
    var isBetaUpgrade = false
    private var betaNetSoftwareVersion = "0"
    private var betaUpgradePackageLink = ""
    private var betaUpgradePackUrl = ""
    private lazy var session:URLSession = {
        //只执行一次
        let config = URLSessionConfiguration.default
        let currentSession = URLSession(configuration: config, delegate: self,
                                        delegateQueue: nil)
        return currentSession
        
    }()
    func checkBetaUpdate(){
        if(!BleDeviceData.isSubLockDevice(model: model)){
            return;
        }
        var deviceType = BleDeviceData.MODEL_OF_SGX120B01;
        
        let curVersion = self.software.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
        var curVerionInt = Int(curVersion) ?? 0
        var debugStr = "0"
        if Utils.isDebug{
            debugStr = "1"
        }
        let url: NSURL = NSURL(string: "http://openapi.tftiot.com:8050/v1/sensor-upgrade-control-out?opr_type=getSensorBetaVersion&device_type=\(deviceType)&mac=\(mac)&is_debug=\(debugStr)")!
        let request: NSURLRequest = NSURLRequest(url: url as URL)
        self.showWaitingWin(title: NSLocalizedString("waiting", comment:"Waiting"))
        NSURLConnection.sendAsynchronousRequest(request as URLRequest, queue: OperationQueue.main, completionHandler:{
            (response, data, error) -> Void in
            if self.waitingView != nil{
                self.waitingView.dismiss()
            }
            if (error != nil) {
                //Handle Error here
                print(error)
                self.notUpdateInit()
            }else{
                //Handle data in NSData type
                var dict: NSDictionary? = nil
                do{
                    dict =  try JSONSerialization.jsonObject(with: data!, options: JSONSerialization.ReadingOptions.mutableLeaves) as! NSDictionary
                }catch{
                }
                print("%@",dict)
                if dict != nil{
                    var code:Int = dict?["code"] as! Int
                    print(String(code))
                    if code == 0{
                        let jsonData:NSDictionary = dict?["data"] as! NSDictionary
                        if jsonData != nil{
                            var version = jsonData["version"] as! String
                            var packageLink = jsonData["link"] as! String
                            self.betaUpgradePackUrl = NSHomeDirectory() + "/Documents/dfu_app_\(self.deviceType)_V\(self.betaNetSoftwareVersion).zip"
                            if version != nil && packageLink != nil && version.count > 0 && packageLink.count > 0{
                                version = version.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
                                print("beta version:\(self.mac),\(version)")
                                self.betaNetSoftwareVersion = version
                                self.betaUpgradePackageLink = packageLink
                                print("check beat update:\(self.betaNetSoftwareVersion),\(curVersion)")
                                if  self.betaNetSoftwareVersion > curVersion {
                                    self.betaUpgradeBtn.isHidden = false
                                }else{
                                    self.betaUpgradeBtn.isHidden = true
                                }
                            }
                        }
                    }
                    
                }
            }
        })
    }
    func checkUpdate(isEnterCheck:Bool){
        if(!BleDeviceData.isSubLockDevice(model: model)){
            return;
        }
        var deviceType = BleDeviceData.MODEL_OF_SGX120B01;
        
        var curVersion = self.software.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
        var curVerionInt = Int(curVersion) ?? 0
        
        print("deviceType:\(deviceType)")
        let url: NSURL = NSURL(string: "http://openapi.tftiot.com:8050/v1/sensor-upgrade-control-out?opr_type=getSensorVersion&device_type=\(deviceType)")!

        let request: NSURLRequest = NSURLRequest(url: url as URL)
        self.showWaitingWin(title: NSLocalizedString("waiting", comment:"Waiting"))
        NSURLConnection.sendAsynchronousRequest(request as URLRequest, queue: OperationQueue.main, completionHandler:{
            (response, data, error) -> Void in
            if self.waitingView != nil{
                self.waitingView.dismiss()
            }
            if (error != nil) {
                //Handle Error here
                print(error)
                self.notUpdateInit()
            }else{
                //Handle data in NSData type
                var dict: NSDictionary? = nil
                do{
                    dict =  try JSONSerialization.jsonObject(with: data!, options: JSONSerialization.ReadingOptions.mutableLeaves) as! NSDictionary
                }catch{
                }
                print("%@",dict)
                if dict != nil{
                    var code:Int = dict?["code"] as! Int
                    print(String(code))
                    if code == 0{
                        let jsonData:NSDictionary = dict?["data"] as! NSDictionary
                        if jsonData != nil{
                            var version = jsonData["version"] as! String
                            var packageLink = jsonData["link"] as! String
                            self.upgradePackUrl = NSHomeDirectory() + "/Documents/dfu_app_\(self.deviceType)_V\(self.netSoftwareVersion).zip"
                            if version != nil && packageLink != nil && version.count > 0 && packageLink.count > 0{
                                version = version.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
                                self.netSoftwareVersion = version
                                self.upgradePackageLink = packageLink
                                print("check update:\(self.netSoftwareVersion),\(curVersion),\(self.software)")
                                if curVersion != version && self.software != "" && self.software != "0"{
                                    //need update
                                    self.editSoftwareBtn.isHidden = false
                                    if !isEnterCheck{
                                        self.showUpgradeWin()
                                    }else{
                                        self.notUpdateInit()
                                    }
                                }else{
                                    self.editSoftwareBtn.isHidden = true
                                    self.notUpdateInit()
                                }
                                
                            }else{
                                self.notUpdateInit()
                            }
                        }else{
                            self.notUpdateInit()
                        }
                    }else{
                        self.notUpdateInit()
                    }
                    
                }else{
                    self.notUpdateInit()
                }
            }
        })
    }
    func showUpgradeWin(){
        if self.upgradeWarningView != nil && !self.upgradeWarningView.isDismiss{
            self.upgradeWarningView.show()
            return
        }
        self.upgradeWarningView = AEAlertView(style: .defaulted)
        self.upgradeWarningView.title = NSLocalizedString("upgrade", comment:"Upgrade")
        self.upgradeWarningView.message = NSLocalizedString("upgrade_confirm", comment:"New version found,updated?")
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.upgradeWarningView.dismiss()
            
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            self.isUpgrade = true
            self.upgradeWarningView.dismiss()
            self.showWaitingWin(title: NSLocalizedString("waiting", comment:"Waiting"))
            self.doUpgrade()
        }
        self.upgradeWarningView.addAction(action: upgradeCancel)
        self.upgradeWarningView.addAction(action: upgradeConfirm)
        self.upgradeWarningView.show()
    }
    func notUpdateInit(){
        
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
    func doUpgrade(){
        if self.waitingView != nil{
            self.waitingView.dismiss()
        }
        self.showProgressBar()
        print("try to update")
        self.isUpgrade = false
        let lastUpgradeFileUrl = NSHomeDirectory() + "/Documents/dfu_app_\(self.deviceType)_V\(self.software).zip"
        if FileTool.fileExists(filePath: lastUpgradeFileUrl){
            FileTool.removeFile(lastUpgradeFileUrl)
        }
        self.isBetaUpgrade = false
        self.showProgressBar()
        let downloadUrl = URL(string: self.upgradePackageLink)
        //请求
        let request = URLRequest(url: downloadUrl!)
        //下载任务
        let downloadTask = session.downloadTask(with: request)
        //使用resume方法启动任务
        downloadTask.resume()
    }
    func doBetaUpgrade(){
        if self.waitingView != nil{
            self.waitingView.dismiss()
        }
        self.showProgressBar()
        print("try to update")
        self.isUpgrade = false
        let lastUpgradeFileUrl = NSHomeDirectory() + "/Documents/dfu_app_\(self.deviceType)_V\(self.software).zip"
        if FileTool.fileExists(filePath: lastUpgradeFileUrl){
            FileTool.removeFile(lastUpgradeFileUrl)
        }
        self.isBetaUpgrade = true
        if FileTool.fileExists(filePath: self.betaUpgradePackUrl){
            FileTool.removeFile(self.betaUpgradePackUrl)
        }
        self.showProgressBar()
        let downloadUrl = URL(string: self.betaUpgradePackageLink)
        //请求
        let request = URLRequest(url: downloadUrl!)
        //下载任务
        let downloadTask = session.downloadTask(with: request)
        //使用resume方法启动任务
        downloadTask.resume()
    }
    
    //下载代理方法，下载结束
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask,
                    didFinishDownloadingTo location: URL) {
        //下载结束
        print("下载结束")
        if let httpResponse = downloadTask.response as? HTTPURLResponse {
            if httpResponse.statusCode == 200 {
                // 下载成功
                //输出下载文件原来的存放目录
                print("location:\(location)")
                //location位置转换
                let locationPath = location.path
                //拷贝到用户目录
                //创建文件管理器
                let fileManager = FileManager.default
                if self.isBetaUpgrade{
                    do {
                        try fileManager.removeItem(atPath: self.betaUpgradePackUrl)
                        print("File deleted successfully")
                    } catch {
                        print("Error deleting file: \(error)")
                    }
                    do {
                        try fileManager.moveItem(atPath: locationPath, toPath: self.betaUpgradePackUrl)
                        print("new location:\(self.betaUpgradePackUrl)")
                        self.upgradeDevice()
                    } catch let error as NSError {
                        print("移动文件失败：\(error.localizedDescription)")
                        self.downloadFail()
                    }
                    
                }else{
                    do {
                        try fileManager.removeItem(atPath: self.upgradePackUrl)
                        print("File deleted successfully")
                    } catch {
                        print("Error deleting file: \(error)")
                    }
                    do {
                        try fileManager.moveItem(atPath: locationPath, toPath: self.upgradePackUrl)
                        print("new location:\(self.upgradePackUrl)")
                        self.upgradeDevice()
                    } catch let error as NSError {
                        print("移动文件失败：\(error.localizedDescription)")
                        self.downloadFail()
                    }
                    
                }
            } else {
                // 下载失败
                self.downloadFail()
            }
        }else{
            self.downloadFail()
        }
        
    }
    
    func upgradeDevice(){
        if self.waitingView != nil{
            self.waitingView.dismiss()
        }
        var url = URL(string:self.upgradePackUrl)
        var filePath = self.upgradePackUrl
        if self.isBetaUpgrade{
            url = URL(string:self.betaUpgradePackUrl)
            filePath = self.betaUpgradePackUrl
        }
        let fileManager = FileManager.default
        
        
        if fileManager.fileExists(atPath: filePath ?? "") {
            print("File exists")
            TftBleConnectManager.getInstance().setEnterUpgrade(enterUpgrade: true)
            a001SoftwareUpgradeManager.startUpgrade(path: filePath!)
        } else {
            self.downloadFail()
        }
        //            if url != nil{
        //                do {
        //
        //                } catch let error as NSError {
        //                    self.downloadFail()
        //                }
        //
        //            }
    }
    
    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        print("下载error：\(error)")
        if error != nil{
            self.downloadFail()
        }
        
    }
    
    func downloadFail(){
        DispatchQueue.main.async {
            if self.progressView != nil {
                self.progressView.dismiss()
            }
            if self.waitingView != nil{
                self.waitingView.dismiss()
            }
            
            Toast.hudBuilder.title(NSLocalizedString("download_file_fail", comment: "Download upgrade package error,please try again!")).show()
        }
    }
    
    //下载代理方法，监听下载进度
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask,
                    didWriteData bytesWritten: Int64, totalBytesWritten: Int64,
                    totalBytesExpectedToWrite: Int64) {
        //获取进度
        let written:CGFloat = (CGFloat)(totalBytesWritten)
        let total:CGFloat = (CGFloat)(totalBytesExpectedToWrite)
        let pro:CGFloat = written/total
        print("下载进度：\(pro)")
    }
    
    //下载代理方法，下载偏移
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask,
                    didResumeAtOffset fileOffset: Int64, expectedTotalBytes: Int64) {
        print("下载偏移")
        //下载偏移，主要用于暂停续传
    }
    @objc func upgradeClick(){
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        self.checkUpdate(isEnterCheck: false)
    }
    
    @objc func betaUpgradeClick(){
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        if self.betaUpgradeWarningView != nil && !self.betaUpgradeWarningView.isDismiss{
            self.betaUpgradeWarningView.show()
            return
        }
        self.betaUpgradeWarningView = AEAlertView(style: .defaulted)
        self.betaUpgradeWarningView.title = NSLocalizedString("betaUpgrade", comment:"Beta upgrade")
        self.betaUpgradeWarningView.message = NSLocalizedString("new_beta_version_found_warning", comment:"New beta version found,updated?")
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.betaUpgradeWarningView.dismiss()
            
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            self.isUpgrade = true
            self.betaUpgradeWarningView.dismiss()
            self.showWaitingWin(title: NSLocalizedString("waiting", comment:"Waiting"))
            self.doBetaUpgrade()
        }
        self.betaUpgradeWarningView.addAction(action: upgradeCancel)
        self.betaUpgradeWarningView.addAction(action: upgradeConfirm)
        self.betaUpgradeWarningView.show()
    }
    
    @objc func debugUpgradeClick(){
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let inputUrlAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("input_debug_upgrade_url", comment: "Upgrade package url"), message: nil)
        inputUrlAlert.textField.placeholder = NSLocalizedString("input_debug_upgrade_url", comment: "Upgrade package url")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            inputUrlAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let url = String(inputUrlAlert.textField.text ?? "")
            if url.count > 0{
                self.upgradePackageLink = url
                print(self.upgradePackUrl)
                inputUrlAlert.dismiss()
                self.showProgressBar()
                print("try to update")
                self.isUpgrade = false
                self.upgradePackUrl = NSHomeDirectory() + "/Documents/dfu_app_debug_upgrade.zip"
                if FileTool.fileExists(filePath: self.upgradePackUrl){
                    FileTool.removeFile(self.upgradePackUrl)
                }
                self.isBetaUpgrade = false
                let downloadUrl = URL(string: self.upgradePackageLink)
                //请求
                let request = URLRequest(url: downloadUrl!)
                //下载任务
                let downloadTask = self.session.downloadTask(with: request)
                //使用resume方法启动任务
                downloadTask.resume()
            }else{
                Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect", comment: "Incorrect length!")).show()
            }
        }
        inputUrlAlert.addAction(action: action_one)
        inputUrlAlert.addAction(action: action_two)
        inputUrlAlert.show()
    }
    @objc private func editBlePwd() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editPwd = EditAccessPwdController()
        editPwd.delegate = self
        self.leaveViewNeedDisconnect = false
        editPwd.confirmPwd = self.blePwd
        self.navigationController?.pushViewController(editPwd, animated: false)
    }
    @objc private func editUnclockPwd() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editPwd = EditLockPwdController()
        editPwd.delegate = self
        self.leaveViewNeedDisconnect = false
        self.navigationController?.pushViewController(editPwd, animated: false)
    }
    @objc private func editDeviceApnAddr() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editNameAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("apn_addr", comment: "apn_addr"), message: nil)
        editNameAlert.textField.placeholder = NSLocalizedString("apn_addr", comment: "apn_addr")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editNameAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let apnAddr = String(editNameAlert.textField.text ?? "")
            if apnAddr.count <= 49 && apnAddr.count >= 0{
                var dataArray = [UInt8](apnAddr.utf8)
                if apnAddr.count == 0{
                    dataArray = [0x00]
                }
                let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_apn_addr, content: dataArray,pwd: self.blePwd)
                TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
                editNameAlert.dismiss()
            }else{
                Toast.hudBuilder.title(NSLocalizedString("apn_addr_len_error", comment: "The length of the apn_addr must between 0 and 49.")).show()
            }
        }
        editNameAlert.addAction(action: action_one)
        editNameAlert.addAction(action: action_two)
        editNameAlert.show()
    }
    @objc private func editDeviceApnPwd() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editNameAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("apn_pwd", comment: "apn_pwd"), message: nil)
        editNameAlert.textField.placeholder = NSLocalizedString("apn_pwd", comment: "apn_pwd")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editNameAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let name = String(editNameAlert.textField.text ?? "")
            if name.count <= 49 && name.count >= 0{
                var dataArray = [UInt8](name.utf8)
                if name.count == 0{
                    dataArray = [0x00]
                }
                let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_apn_pwd, content: dataArray,pwd: self.blePwd)
                TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
                editNameAlert.dismiss()
            }else{
                Toast.hudBuilder.title(NSLocalizedString("apn_pwd_len_error", comment: "The length of the apn_pwd must between 0 and 49.")).show()
            }
        }
        editNameAlert.addAction(action: action_one)
        editNameAlert.addAction(action: action_two)
        editNameAlert.show()
    }
    
    @objc private func editAlarmSet() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editView = AlarmOpenSetViewController()
        editView.delegate = self
        self.leaveViewNeedDisconnect = false
        editView.alarmOpenSet = self.alarmOpenValue
        self.navigationController?.pushViewController(editView, animated: false)
    }
    @objc private func editDeviceTempAlarm() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editView = TempAlarmController()
        editView.delegate = self
        self.leaveViewNeedDisconnect = false
        editView.tempLow = self.tempAlarmLow
        editView.tempHigh = self.tempAlarmHigh
        self.navigationController?.pushViewController(editView, animated: false)
       
    }
    @objc private func editDeviceBroadcastInterval() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editBroadcastIntervalAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("broadcast_interval", comment: "apn_pwd"), message: nil)
        editBroadcastIntervalAlert.textField.placeholder = NSLocalizedString("broadcast_interval", comment: "apn_pwd")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editBroadcastIntervalAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let broadcastIntervalStr = String(editBroadcastIntervalAlert.textField.text ?? "")
            let broadcastInterval = Int(broadcastIntervalStr) ?? -1
            if broadcastInterval <= 10 && broadcastInterval >= 1{
                var dataArray = [(UInt8)(broadcastInterval)]
                let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_broadcast_interval, content: dataArray,pwd: self.blePwd)
                TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
                editBroadcastIntervalAlert.dismiss()
            }else{
                Toast.hudBuilder.title(NSLocalizedString("broadcast_interval_range", comment: "The length of the apn_pwd must between 0 and 49.")).show()
            }
        }
        editBroadcastIntervalAlert.addAction(action: action_one)
        editBroadcastIntervalAlert.addAction(action: action_two)
        editBroadcastIntervalAlert.show()
    }
    @objc private func editDeviceTransmittedPower() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editTransmittedPowerAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("transmitted_power", comment: "transmitted_power"), message: nil)
        editTransmittedPowerAlert.textField.placeholder = NSLocalizedString("transmitted_power", comment: "transmitted_power")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editTransmittedPowerAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let transmittedPowerStr = String(editTransmittedPowerAlert.textField.text ?? "")
            let transmittedPower = Int(transmittedPowerStr) ?? -1
            if transmittedPower <= 10 && transmittedPower >= 1{
                var dataArray = [(UInt8)(transmittedPower)]
                let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_ble_transmitted_power, content: dataArray,pwd: self.blePwd)
                TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
                editTransmittedPowerAlert.dismiss()
            }else{
                Toast.hudBuilder.title(NSLocalizedString("transmitted_power_range", comment: "The length of the apn_pwd must between 0 and 49.")).show()
            }
        }
        editTransmittedPowerAlert.addAction(action: action_one)
        editTransmittedPowerAlert.addAction(action: action_two)
        editTransmittedPowerAlert.show()
    }
    
    @objc private func editDeviceApnUsername() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editNameAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("apn_username", comment: "APN username"), message: nil)
        editNameAlert.textField.placeholder = NSLocalizedString("apn_username", comment: "APN username")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editNameAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let apnUsername = String(editNameAlert.textField.text ?? "")
            if apnUsername.count <= 49 && apnUsername.count >= 0{
                var dataArray = [UInt8](apnUsername.utf8)
                if apnUsername.count == 0{
                    dataArray = [0x00]
                }
                let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_apn_username, content: dataArray,pwd: self.blePwd)
                TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
                editNameAlert.dismiss()
            }else{
                Toast.hudBuilder.title(NSLocalizedString("apn_username_len_error", comment: "The apn username is too long and cannot exceed 49 bytes.")).show()
            }
        }
        editNameAlert.addAction(action: action_one)
        editNameAlert.addAction(action: action_two)
        editNameAlert.show()
    }
    @objc private func editDeviceIp1() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editView = IpEditController()
        editView.delegate = self
        self.leaveViewNeedDisconnect = false
        editView.ipType = 1
        editView.domain = ip1ContentLabel.text
        editView.portStr = port1ContentLabel.text
        self.navigationController?.pushViewController(editView, animated: false)
    }
    @objc private func editDeviceIp2() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editView = IpEditController()
        editView.delegate = self
        self.leaveViewNeedDisconnect = false
        editView.ipType = 2
        editView.domain = ip2ContentLabel.text
        editView.portStr = port2ContentLabel.text
        self.navigationController?.pushViewController(editView, animated: false)
    }
    @objc private func editDeviceName() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editNameAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("ble_name", comment: "Device name"), message: nil)
        editNameAlert.textField.placeholder = NSLocalizedString("ble_name", comment: "Device name")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editNameAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let deviceName = String(editNameAlert.textField.text ?? "")
            if deviceName.count <= 16 && deviceName.count >= 3{
                let dataArray = [UInt8](deviceName.utf8)
                let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_device_name, content: dataArray,pwd: self.blePwd)
                TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
                editNameAlert.dismiss()
            }else{
                Toast.hudBuilder.title(NSLocalizedString("device_name_len_error", comment: "The length of the device name must between 3 and 16.")).show()
            }
        }
        editNameAlert.addAction(action: action_one)
        editNameAlert.addAction(action: action_two)
        editNameAlert.show()
    }
    @objc private func editDevicePort1() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editView = IpEditController()
        editView.delegate = self
        self.leaveViewNeedDisconnect = false
        editView.ipType = 1
        editView.domain = ip1ContentLabel.text
        editView.portStr = port1ContentLabel.text
        self.navigationController?.pushViewController(editView, animated: false)
    }
    @objc private func editDevicePort2() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editView = IpEditController()
        editView.delegate = self
        self.leaveViewNeedDisconnect = false
        editView.ipType = 2
        editView.domain = ip2ContentLabel.text
        editView.portStr = port2ContentLabel.text
        self.navigationController?.pushViewController(editView, animated: false)
    }
    @objc private func editDeviceTimer() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editView = EditTimerController()
        editView.delegate = self
        editView.accOn = self.accOn
        editView.accOff = self.accOff
        editView.angle = self.angle
        editView.distance = self.distance
        self.leaveViewNeedDisconnect = false
        
        self.navigationController?.pushViewController(editView, animated: false)
    }
    @objc private func editRfid() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editView = RFIDViewController()
        editView.blePwd = self.blePwd
        editView.connectStatusDelegate = self
        self.leaveViewNeedDisconnect = false
        self.navigationController?.pushViewController(editView, animated: false)
    }
    @objc private func editSubLock() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let editView = SubLockController()
        editView.blePwd = self.blePwd
        editView.imei = self.imei
        editView.connectStatusDelegate = self
        self.leaveViewNeedDisconnect = false
        self.navigationController?.pushViewController(editView, animated: false)
    }
    
    @objc private func shutdownClick() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_shutdown_warning", comment: "Are you sure to restore the factory settings?")
        animV.textField.placeholder = NSLocalizedString("ble_pwd", comment:"Password")
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_shutdown, content: [0x00],pwd: self.blePwd)
            TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
            animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
    }
    @objc private func rebootClick() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_reboot_warning", comment: "Are you sure to restore the factory settings?")
        animV.textField.placeholder = NSLocalizedString("ble_pwd", comment:"Password")
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_reboot, content: [0x00],pwd: self.blePwd)
            TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
            animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
    }
    
    private var waitCheckResetDefaultValueItems:[Int] = []
    
    private func doResetDefaultValue(){
        if waitCheckResetDefaultValueItems.isEmpty{
            return
        }
        let resetItem = waitCheckResetDefaultValueItems.removeFirst()
        if resetItem == nil{
            return
        }
        showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_reset_default, content: [UInt8(resetItem)],pwd: self.blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)    }
    
    
    @objc private func  resetDefaultClick(){
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_reset_default_warning", comment: "Are you sure to restore Default Settings?")
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            self.waitCheckResetDefaultValueItems.append(BleDeviceData.CFG_RESTORE_IP);
            self.waitCheckResetDefaultValueItems.append(BleDeviceData.CFG_RESTORE_APN);
            self.waitCheckResetDefaultValueItems.append(BleDeviceData.CFG_RESTORE_TIMER);
            self.waitCheckResetDefaultValueItems.append(BleDeviceData.CFG_RESTORE_NFCIDLIST);
            self.waitCheckResetDefaultValueItems.append(BleDeviceData.CFG_RESTORE_SUBLOCKLIST);
            self.doResetDefaultValue()
            animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
    }
    
    @objc private func  clearHisDataClick(){
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_clear_his_data_warning", comment: "Are you sure to clear history data?")
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_clear_his_data, content: [0x00],pwd: self.blePwd)
            TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
            animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
    }
    
    @objc private func resetFactoryClick() {
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_reset_factory_warning", comment: "Are you sure to restore the factory settings?") 
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_factory_reset, content: [0x00],pwd: self.blePwd)
            TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
            animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
    }
    
    
    @objc func ledSwitchAction(sender:UISwitch){
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            self.ledSwitch.isOn = !self.ledSwitch.isOn
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        print("ledSwitchAction")
        var data = [UInt8]()
        if self.ledSwitch.isOn {
            data.append(UInt8(1))
        }else{
            data.append(UInt8(0))
        }
        let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_led, content: data,pwd: self.blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
    }
    
    
    @objc func longRangeSwitchAction(sender:UISwitch){
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            self.longRangeSwitch.isOn = !self.longRangeSwitch.isOn
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        print("longRangeSwitchAction")
        var data = [UInt8]()
        if self.longRangeSwitch.isOn {
            data.append(UInt8(1))
        }else{
            data.append(UInt8(0))
        }
        let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_long_range, content: data,pwd: self.blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
    }
    @objc func buzzerSwitchAction(sender:UISwitch){
        if !TftBleConnectManager.getInstance().isConnectSucc(){
            self.buzzerSwitch.isOn = !self.buzzerSwitch.isOn
            Toast.hudBuilder.title(NSLocalizedString("disconnect_please_connect_manually", comment: "Has been disconnected, please reconnect manually!")).show()
            return
        }
        print("buzzerSwitchAction")
        var data = [UInt8]()
        if self.buzzerSwitch.isOn {
            data.append(UInt8(1))
        }else{
            data.append(UInt8(0))
        }
        let cmd = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_sub_lock_buzzer, content: data,pwd: self.blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
    }
    private func getInitViewFuncs() -> [Int] {
        if BleDeviceData.isSubLockDevice(model: model) {
            print("getInitViewFuncs sub lock")
            return subLockInitViewFuncs
        } else {
            print("getInitViewFuncs parent lock")
            return parentLockInitViewFuncs
        }
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
        if !BleDeviceData.isSupportConfig(model: model, version: software, deviceId: deviceId){
            return
        }
        if BleDeviceData.isSubLockDevice(model: model) {
            fixTime()
        }
        
        let curInitViewFuncs = getInitViewFuncs()
        var commands: [[UInt8]] = []
        
        for functionId in curInitViewFuncs {
            let cmd = Utils.getReadCmdContent(cmdCode: functionId, content: nil)
            commands.append(cmd)
        }
        
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: commands)
    }
    private func initDiffUI() {
        timerView.isHidden = true
        subLockView.isHidden = true
        ip1View.isHidden = true
        port1View.isHidden = true
        ip2View.isHidden = true
        port2View.isHidden = true
        apnAddrView.isHidden = true
        apnPwdView.isHidden = true
        apnUsernameView.isHidden = true
        deviceIdView.isHidden = true
        softwareView.isHidden = true
        rebootView.isHidden = true
        shutdownView.isHidden = true
        resetFactoryView.isHidden = true
        nameView.isHidden = true
        longRangeView.isHidden = true
        ledView.isHidden = true
        buzzerView.isHidden = true
        modelView.isHidden = true
        hardwareView.isHidden = true
        bootVersionView.isHidden = true
        alarmSetView.isHidden = true
        tempAlarmView.isHidden = true
        broadcastIntervalView.isHidden = true
        transmittedPowerView.isHidden = true
        resetDefaultView.isHidden = true
        clearHisDataView.isHidden = true
        
        if !BleDeviceData.isSupportConfig(model: model, version:software , deviceId: deviceId){
            configScrollView.isHidden = true
            return
        }
        configScrollView.isHidden = false
        if BleDeviceData.isSubLockDevice(model: model) {
            timerView.isHidden = true
            subLockView.isHidden = true
            ip1View.isHidden = true
            port1View.isHidden = true
            ip2View.isHidden = true
            port2View.isHidden = true
            apnAddrView.isHidden = true
            apnPwdView.isHidden = true
            apnUsernameView.isHidden = true
            activeNetworkBtn.isHidden = true
            unlockBtn.frame = CGRect(x: (Int)(self.view.bounds.size.width / 2  - 40), y: unlockBtnY, width: 80, height: 30)
            
            deviceIdView.isHidden = false
            softwareView.isHidden = false
            rebootView.isHidden = false
            shutdownView.isHidden = false
            resetFactoryView.isHidden = false
            nameView.isHidden = false
            longRangeView.isHidden = false
            ledView.isHidden = false
            buzzerView.isHidden = false
            modelView.isHidden = false
            hardwareView.isHidden = false
            if Utils.isDebug{
                bootVersionView.isHidden = false
            }else{
                bootVersionView.isHidden = true
            }
            
            alarmSetView.isHidden = false
            tempAlarmView.isHidden = false
            broadcastIntervalView.isHidden = false
            transmittedPowerView.isHidden = false
        } else {
            timerView.isHidden = false
            subLockView.isHidden = false
            ip1View.isHidden = false
            port1View.isHidden = false
            ip2View.isHidden = false
            port2View.isHidden = false
            apnAddrView.isHidden = false
            apnPwdView.isHidden = false
            apnUsernameView.isHidden = false
            activeNetworkBtn.isHidden = false
            unlockBtn.frame = CGRect(x: (Int)(self.view.bounds.size.width / 2 / 2  - 40), y: unlockBtnY, width: 80, height: 30)
            deviceIdView.isHidden = true
            softwareView.isHidden = true
            rebootView.isHidden = true
            shutdownView.isHidden = true
            resetFactoryView.isHidden = true
            nameView.isHidden = true
            longRangeView.isHidden = true
            ledView.isHidden = true
            buzzerView.isHidden = true
            modelView.isHidden = true
            hardwareView.isHidden = true
            alarmSetView.isHidden = true
            tempAlarmView.isHidden = true
            broadcastIntervalView.isHidden = true
            transmittedPowerView.isHidden = true
            
            if BleDeviceData.isSupportClearHisAndResetDefault(model: model, version: software){
                resetDefaultView.isHidden = false
                clearHisDataView.isHidden = false
                rebootView.isHidden = false
            }else{
                resetDefaultView.isHidden = true
                clearHisDataView.isHidden = true
                rebootView.isHidden = true
            }
        }
    }
    func initConfigUI(startY:Int){
        configScrollView = UIScrollView()
        configScrollView.frame = CGRect(x: 0, y: startY + 5, width: Int(KSize.width), height: Int(KSize.height) - startY)
        var scrollViewHeight:Float = 910
        //        scrollView.isPagingEnabled = true
        configScrollView.showsHorizontalScrollIndicator = false
        configScrollView.showsVerticalScrollIndicator = false
        configScrollView.scrollsToTop = false
        self.view.addSubview(configScrollView)
        let stackViewContainer = UIView()
        stackViewContainer.translatesAutoresizingMaskIntoConstraints = false
        
        configScrollView.addSubview(stackViewContainer)
        stackViewContainer.isUserInteractionEnabled = true
        let stackView = UIStackView()
        stackView.axis = .vertical // 垂直布局
        stackView.alignment = .leading // 子视图居中对齐
        stackView.distribution = .fillEqually
        stackView.spacing = 0
        stackView.isMultipleTouchEnabled  = true
        // 添加 UIStackView 到父视图
        stackViewContainer.addSubview(stackView)
        configScrollView.isUserInteractionEnabled = true
        stackView.isUserInteractionEnabled = true
        // 设置约束
        stackView.translatesAutoresizingMaskIntoConstraints = false
        //        stackView.leadingAnchor.constraint(equalTo: stackViewContainer.leadingAnchor, constant: 0).isActive = true
        //        stackView.trailingAnchor.constraint(equalTo: stackViewContainer.trailingAnchor, constant: 0).isActive = true
        //        stackView.topAnchor.constraint(equalTo: stackViewContainer.topAnchor, constant: 0).isActive = true
        //        stackView.bottomAnchor.constraint(equalTo: stackViewContainer.bottomAnchor, constant: 0).isActive = true
        NSLayoutConstraint.activate([
            stackViewContainer.topAnchor.constraint(equalTo: configScrollView.topAnchor),
            stackViewContainer.leadingAnchor.constraint(equalTo: configScrollView.leadingAnchor),
            stackViewContainer.trailingAnchor.constraint(equalTo: configScrollView.trailingAnchor),
            stackViewContainer.bottomAnchor.constraint(equalTo: configScrollView.bottomAnchor),
            stackViewContainer.widthAnchor.constraint(equalTo: configScrollView.widthAnchor),
            stackView.topAnchor.constraint(equalTo: stackViewContainer.topAnchor),
            stackView.leadingAnchor.constraint(equalTo: stackViewContainer.leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: stackViewContainer.trailingAnchor),
            stackView.bottomAnchor.constraint(equalTo: stackViewContainer.bottomAnchor)
        ])
        
        
        let descWidth = Int(KSize.width / 3)
        let contentX = Int(KSize.width / 3 + 10)
        let btnX = Int(KSize.width / 3 * 2 + 30)
        var startLabelY:Int = 0
        let lineHigh:Int = 60
        var lineY:Int = 60
        var btnY:Int = 15
        let btnHeight = 30
        
        let topLine = UIView()
        topLine.backgroundColor = UIColor.gray
        topLine.frame = CGRect(x: 0, y: Double(0), width: Double(KSize.width), height: 0.5)
        stackView.addSubview(topLine)
        
        nameView = UIView()
        nameView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        nameView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(nameView)
        
        self.nameLabel = UILabel()
        self.nameLabel.textColor = UIColor.black
        self.nameLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.nameLabel.text = NSLocalizedString("ble_name", comment: "Name:")
        self.nameLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.nameLabel.numberOfLines = 0;
        self.nameLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        nameView.addSubview(self.nameLabel)
        self.nameContentLabel = UILabel()
        self.nameContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.nameContentLabel.textColor = UIColor.black
        self.nameContentLabel.text = ""
        self.nameContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        nameView.addSubview(self.nameContentLabel)
        self.editNameBtn = UIButton()
        self.editNameBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editNameBtn.isUserInteractionEnabled  = true
        self.editNameBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editNameBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        
        self.editNameBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editNameBtn.layer.cornerRadius = 15;
        self.editNameBtn.layer.borderWidth = 1.0;
        self.editNameBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        
        
        self.editNameBtn.addTarget(self, action: #selector(editDeviceName), for:.touchUpInside)
        nameView.isUserInteractionEnabled  = true
        nameView.addSubview(self.editNameBtn)
        
        let nameLine = UIView()
        nameLine.backgroundColor = UIColor.gray
        nameLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        nameView.addSubview(nameLine)
        
        deviceIdView = UIView()
        deviceIdView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        deviceIdView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(deviceIdView)
        
        self.deviceIdLabel = UILabel()
        self.deviceIdLabel.textColor = UIColor.black
        self.deviceIdLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.deviceIdLabel.text = NSLocalizedString("device_id", comment: "Name:")
        self.deviceIdLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.deviceIdLabel.numberOfLines = 0;
        self.deviceIdLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        deviceIdView.addSubview(self.deviceIdLabel)
        self.deviceIdContentLabel = UILabel()
        self.deviceIdContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.deviceIdContentLabel.textColor = UIColor.black
        self.deviceIdContentLabel.text = ""
        self.deviceIdContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        deviceIdView.addSubview(self.deviceIdContentLabel)
        //        self.editDeviceIdBtn = UIButton()
        //        self.editDeviceIdBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        //        self.editDeviceIdBtn.isUserInteractionEnabled  = true
        //        self.editDeviceIdBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        //        self.editDeviceIdBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        //
        //        self.editDeviceIdBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        //        self.editDeviceIdBtn.layer.cornerRadius = 15;
        //        self.editDeviceIdBtn.layer.borderWidth = 1.0;
        //        self.editDeviceIdBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        //
        //
        //        self.editDeviceIdBtn.addTarget(self, action: #selector(editDeviceName), for:.touchUpInside)
        //        deviceIdView.isUserInteractionEnabled  = true
        //        deviceIdView.addSubview(self.editDeviceIdBtn)
        
        let deviceIdLine = UIView()
        deviceIdLine.backgroundColor = UIColor.gray
        deviceIdLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        deviceIdView.addSubview(deviceIdLine)
        
        modelView = UIView()
        modelView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        modelView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(modelView)
        
        self.modelLabel = UILabel()
        self.modelLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.modelLabel.textColor = UIColor.black
        self.modelLabel.text = NSLocalizedString("model", comment: "Model:")
        self.modelLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.modelLabel.numberOfLines = 0;
        self.modelLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        modelView.addSubview(self.modelLabel)
        self.modelContentLabel = UILabel()
        self.modelContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.modelContentLabel.text = ""
        self.modelContentLabel.textColor = UIColor.black
        self.modelContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        modelView.addSubview(self.modelContentLabel)
        let modelLine = UIView()
        modelLine.backgroundColor = UIColor.gray
        modelLine.frame =  CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        modelView.addSubview(modelLine)
        self.debugUpgradeBtn = UIButton()
        self.debugUpgradeBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.debugUpgradeBtn.setTitle(NSLocalizedString("debug_upgrade", comment: "Debug Upgrade"), for: .normal)
        self.debugUpgradeBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.debugUpgradeBtn.layer.cornerRadius = 15;
        self.debugUpgradeBtn.layer.borderWidth = 1.0;
        self.debugUpgradeBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.debugUpgradeBtn.frame = CGRect(x: btnX, y: 15, width: 90, height: btnHeight)
        if(Utils.isDebug){
            self.debugUpgradeBtn.isHidden = false
        }else{
            self.debugUpgradeBtn.isHidden = true
        }
        self.debugUpgradeBtn.addTarget(self, action: #selector(debugUpgradeClick), for:.touchUpInside)
        modelView.addSubview(self.debugUpgradeBtn)
        
        
        hardwareView = UIView()
        hardwareView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        hardwareView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(hardwareView)
        
        self.hardwareLabel = UILabel()
        self.hardwareLabel.textColor = UIColor.black
        self.hardwareLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.hardwareLabel.text = NSLocalizedString("hardware", comment: "Hardware:")
        self.hardwareLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.hardwareLabel.numberOfLines = 0;
        self.hardwareLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        hardwareView.addSubview(self.hardwareLabel)
        self.hardwareContentLabel = UILabel()
        self.hardwareContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.hardwareContentLabel.textColor = UIColor.black
        self.hardwareContentLabel.text = ""
        self.hardwareContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        hardwareView.addSubview(self.hardwareContentLabel)
        self.betaUpgradeBtn = UIButton()
        self.betaUpgradeBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.betaUpgradeBtn.setTitle(NSLocalizedString("beta_upgrade", comment: "Beta Upgrade"), for: .normal)
        self.betaUpgradeBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.betaUpgradeBtn.layer.cornerRadius = 15;
        self.betaUpgradeBtn.layer.borderWidth = 1.0;
        self.betaUpgradeBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.betaUpgradeBtn.frame = CGRect(x: btnX, y: 15, width: 90, height: btnHeight)
        self.betaUpgradeBtn.isHidden = true
        self.betaUpgradeBtn.addTarget(self, action: #selector(betaUpgradeClick), for:.touchUpInside)
        hardwareView.addSubview(self.betaUpgradeBtn)
        
        let hardwareLine = UIView()
        hardwareLine.backgroundColor = UIColor.gray
        hardwareLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        hardwareView.addSubview(hardwareLine)
        
        
        softwareView = UIView()
        softwareView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        softwareView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(softwareView)
        
        
        self.softwareLabel = UILabel()
        self.softwareLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.softwareLabel.textColor = UIColor.black
        self.softwareLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.softwareLabel.numberOfLines = 0;
        self.softwareLabel.text = NSLocalizedString("software", comment: "Software:")
        self.softwareLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        softwareView.addSubview(self.softwareLabel)
        self.softwareContentLabel = UILabel()
        self.softwareContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.softwareContentLabel.textColor = UIColor.black
        self.softwareContentLabel.text = ""
        self.softwareContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        softwareView.addSubview(self.softwareContentLabel)
        self.editSoftwareBtn = UIButton()
        self.editSoftwareBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editSoftwareBtn.setTitle(NSLocalizedString("upgrade", comment: "Upgrade"), for: .normal)
        self.editSoftwareBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editSoftwareBtn.layer.cornerRadius = 15;
        self.editSoftwareBtn.layer.borderWidth = 1.0;
        self.editSoftwareBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editSoftwareBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editSoftwareBtn.isHidden = true
        self.editSoftwareBtn.addTarget(self, action: #selector(upgradeClick), for:.touchUpInside)
        softwareView.addSubview(self.editSoftwareBtn)
        let softwareLine = UIView()
        softwareLine.backgroundColor = UIColor.gray
        softwareLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        softwareView.addSubview(softwareLine)
        
        
        bootVersionView = UIView()
        bootVersionView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        bootVersionView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(bootVersionView)
       
        self.bootVersionLabel = UILabel()
        self.bootVersionLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.bootVersionLabel.textColor = UIColor.black
        self.bootVersionLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.bootVersionLabel.numberOfLines = 0;
        self.bootVersionLabel.text = NSLocalizedString("boot_version", comment: "Boot version:")
        self.bootVersionLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        bootVersionView.addSubview(self.bootVersionLabel)
        self.bootVersionContentLabel = UILabel()
        self.bootVersionContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.bootVersionContentLabel.textColor = UIColor.black
        self.bootVersionContentLabel.text = ""
        self.bootVersionContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        bootVersionView.addSubview(self.bootVersionContentLabel)
        self.editBootVersionBtn = UIButton()
        self.editBootVersionBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editBootVersionBtn.setTitle(NSLocalizedString("upgrade", comment: "Upgrade"), for: .normal)
        self.editBootVersionBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editBootVersionBtn.layer.cornerRadius = 15;
        self.editBootVersionBtn.layer.borderWidth = 1.0;
        self.editBootVersionBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editBootVersionBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editBootVersionBtn.isHidden = true
        self.editBootVersionBtn.addTarget(self, action: #selector(upgradeClick), for:.touchUpInside)
        bootVersionView.addSubview(self.editBootVersionBtn)
        let bootVersionLine = UIView()
        bootVersionLine.backgroundColor = UIColor.gray
        bootVersionLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        bootVersionView.addSubview(bootVersionLine)
        
        blePwdView = UIView()
        blePwdView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        blePwdView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(blePwdView)
        
        self.blePwdLabel = UILabel()
        self.blePwdLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.blePwdLabel.textColor = UIColor.black
        self.blePwdLabel.text = NSLocalizedString("ble_pwd", comment: "blePwd:")
        self.blePwdLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.blePwdLabel.numberOfLines = 0;
        self.blePwdLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        blePwdView.addSubview(self.blePwdLabel)
        self.blePwdContentLabel = UILabel()
        self.blePwdContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.blePwdContentLabel.textColor = UIColor.black
        self.blePwdContentLabel.text = ""
        self.blePwdContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        blePwdView.addSubview(self.blePwdContentLabel)
        self.editBlePwdBtn = UIButton()
        self.editBlePwdBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editBlePwdBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editBlePwdBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editBlePwdBtn.layer.cornerRadius = 15;
        self.editBlePwdBtn.layer.borderWidth = 1.0;
        self.editBlePwdBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editBlePwdBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editBlePwdBtn.addTarget(self, action: #selector(editBlePwd), for:.touchUpInside)
        blePwdView.addSubview(self.editBlePwdBtn)
        let blePwdLine = UIView()
        blePwdLine.backgroundColor = UIColor.gray
        blePwdLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        blePwdView.addSubview(blePwdLine)
        
        
        unclockPwdView = UIView()
        unclockPwdView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        unclockPwdView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(unclockPwdView)
        self.unclockPwdLabel = UILabel()
        self.unclockPwdLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.unclockPwdLabel.textColor = UIColor.black
        self.unclockPwdLabel.text = NSLocalizedString("unlock_pwd", comment: "unclockPwd:")
        self.unclockPwdLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.unclockPwdLabel.numberOfLines = 0;
        self.unclockPwdLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        unclockPwdView.addSubview(self.unclockPwdLabel)
        self.unclockPwdContentLabel = UILabel()
        self.unclockPwdContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.unclockPwdContentLabel.textColor = UIColor.black
        self.unclockPwdContentLabel.text = ""
        self.unclockPwdContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        unclockPwdView.addSubview(self.unclockPwdContentLabel)
        self.editUnclockPwdBtn = UIButton()
        self.editUnclockPwdBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editUnclockPwdBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editUnclockPwdBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editUnclockPwdBtn.layer.cornerRadius = 15;
        self.editUnclockPwdBtn.layer.borderWidth = 1.0;
        self.editUnclockPwdBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editUnclockPwdBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editUnclockPwdBtn.addTarget(self, action: #selector(editUnclockPwd), for:.touchUpInside)
        unclockPwdView.addSubview(self.editUnclockPwdBtn)
        let unclockPwdLine = UIView()
        unclockPwdLine.backgroundColor = UIColor.gray
        unclockPwdLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        unclockPwdView.addSubview(unclockPwdLine)
        
        timerView = UIView()
        timerView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        timerView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(timerView)
        self.timerLabel = UILabel()
        self.timerLabel.textColor = UIColor.black
        self.timerLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.timerLabel.text = NSLocalizedString("timer", comment: "timer:")
        self.timerLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.timerLabel.numberOfLines = 0;
        self.timerLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        timerView.addSubview(self.timerLabel)
        self.timerContentLabel = UILabel()
        self.timerContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.timerContentLabel.textColor = UIColor.black
        self.timerContentLabel.text = ""
        self.timerContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        timerView.addSubview(self.timerContentLabel)
        self.editTimerBtn = UIButton()
        self.editTimerBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editTimerBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editTimerBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editTimerBtn.layer.cornerRadius = 15;
        self.editTimerBtn.layer.borderWidth = 1.0;
        self.editTimerBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editTimerBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editTimerBtn.isUserInteractionEnabled  = true
        self.editTimerBtn.addTarget(self, action: #selector(editDeviceTimer), for:.touchUpInside)
        timerView.isUserInteractionEnabled  = true
        timerView.addSubview(self.editTimerBtn)
        
        let timerLine = UIView()
        timerLine.backgroundColor = UIColor.gray
        timerLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        timerView.addSubview(timerLine)
        
        rfidView = UIView()
        rfidView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        rfidView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(rfidView)
        self.rfidLabel = UILabel()
        self.rfidLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.rfidLabel.textColor = UIColor.black
        self.rfidLabel.text = "RFID"
        self.rfidLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.rfidLabel.numberOfLines = 0;
        self.rfidLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        rfidView.addSubview(self.rfidLabel)
        self.rfidContentLabel = UILabel()
        self.rfidContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.rfidContentLabel.textColor = UIColor.black
        self.rfidContentLabel.text = ""
        self.rfidContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        rfidView.addSubview(self.rfidContentLabel)
        self.editRfidBtn = UIButton()
        self.editRfidBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editRfidBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editRfidBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editRfidBtn.layer.cornerRadius = 15;
        self.editRfidBtn.layer.borderWidth = 1.0;
        self.editRfidBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editRfidBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editRfidBtn.addTarget(self, action: #selector(editRfid), for:.touchUpInside)
        rfidView.addSubview(self.editRfidBtn)
        let rfidLine = UIView()
        rfidLine.backgroundColor = UIColor.gray
        rfidLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        rfidView.addSubview(rfidLine)
        
        subLockView = UIView()
        subLockView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        subLockView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(subLockView)
        self.subLockLabel = UILabel()
        self.subLockLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.subLockLabel.textColor = UIColor.black
        self.subLockLabel.text = NSLocalizedString("sub_lock_title", comment: "subLock:")
        self.subLockLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.subLockLabel.numberOfLines = 0;
        self.subLockLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        subLockView.addSubview(self.subLockLabel)
        self.subLockContentLabel = UILabel()
        self.subLockContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.subLockContentLabel.textColor = UIColor.black
        self.subLockContentLabel.text = ""
        self.subLockContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        subLockView.addSubview(self.subLockContentLabel)
        self.editSubLockBtn = UIButton()
        self.editSubLockBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editSubLockBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editSubLockBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editSubLockBtn.layer.cornerRadius = 15;
        self.editSubLockBtn.layer.borderWidth = 1.0;
        self.editSubLockBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editSubLockBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editSubLockBtn.addTarget(self, action: #selector(editSubLock), for:.touchUpInside)
        subLockView.addSubview(self.editSubLockBtn)
        let subLockLine = UIView()
        subLockLine.backgroundColor = UIColor.gray
        subLockLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        subLockView.addSubview(subLockLine)
        
        ip1View = UIView()
        ip1View.heightAnchor.constraint(equalToConstant: 60).isActive = true
        ip1View.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(ip1View)
        self.ip1Label = UILabel()
        self.ip1Label.textColor = UIColor.black
        self.ip1Label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.ip1Label.text = NSLocalizedString("ip1", comment: "ip1:")
        self.ip1Label.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.ip1Label.numberOfLines = 0;
        self.ip1Label.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        ip1View.addSubview(self.ip1Label)
        self.ip1ContentLabel = UILabel()
        self.ip1ContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.ip1ContentLabel.textColor = UIColor.black
        self.ip1ContentLabel.text = ""
        self.ip1ContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        ip1View.addSubview(self.ip1ContentLabel)
        self.editIp1Btn = UIButton()
        self.editIp1Btn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editIp1Btn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editIp1Btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editIp1Btn.layer.cornerRadius = 15;
        self.editIp1Btn.layer.borderWidth = 1.0;
        self.editIp1Btn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editIp1Btn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editIp1Btn.isUserInteractionEnabled  = true
        self.editIp1Btn.addTarget(self, action: #selector(editDeviceIp1), for:.touchUpInside)
        ip1View.isUserInteractionEnabled  = true
        ip1View.addSubview(self.editIp1Btn)
        let ip1Line = UIView()
        ip1Line.backgroundColor = UIColor.gray
        ip1Line.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        ip1View.addSubview(ip1Line)
        port1View = UIView()
        port1View.heightAnchor.constraint(equalToConstant: 60).isActive = true
        port1View.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(port1View)
        self.port1Label = UILabel()
        self.port1Label.textColor = UIColor.black
        self.port1Label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.port1Label.text = NSLocalizedString("port1", comment: "port1:")
        self.port1Label.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.port1Label.numberOfLines = 0;
        self.port1Label.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        port1View.addSubview(self.port1Label)
        self.port1ContentLabel = UILabel()
        self.port1ContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.port1ContentLabel.textColor = UIColor.black
        self.port1ContentLabel.text = ""
        self.port1ContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        port1View.addSubview(self.port1ContentLabel)
        self.editPort1Btn = UIButton()
        self.editPort1Btn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editPort1Btn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editPort1Btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editPort1Btn.layer.cornerRadius = 15;
        self.editPort1Btn.layer.borderWidth = 1.0;
        self.editPort1Btn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editPort1Btn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editPort1Btn.isUserInteractionEnabled  = true
        self.editPort1Btn.addTarget(self, action: #selector(editDevicePort1), for:.touchUpInside)
        port1View.isUserInteractionEnabled  = true
        port1View.addSubview(self.editPort1Btn)
        let port1Line = UIView()
        port1Line.backgroundColor = UIColor.gray
        port1Line.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        port1View.addSubview(port1Line)
        
        ip2View = UIView()
        ip2View.heightAnchor.constraint(equalToConstant: 60).isActive = true
        ip2View.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(ip2View)
        self.ip2Label = UILabel()
        self.ip2Label.textColor = UIColor.black
        self.ip2Label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.ip2Label.text = NSLocalizedString("ip2", comment: "ip2:")
        self.ip2Label.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.ip2Label.numberOfLines = 0;
        self.ip2Label.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        ip2View.addSubview(self.ip2Label)
        self.ip2ContentLabel = UILabel()
        self.ip2ContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.ip2ContentLabel.textColor = UIColor.black
        self.ip2ContentLabel.text = ""
        self.ip2ContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        ip2View.addSubview(self.ip2ContentLabel)
        self.editIp2Btn = UIButton()
        self.editIp2Btn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editIp2Btn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editIp2Btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editIp2Btn.layer.cornerRadius = 15;
        self.editIp2Btn.layer.borderWidth = 1.0;
        self.editIp2Btn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editIp2Btn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editIp2Btn.isUserInteractionEnabled  = true
        self.editIp2Btn.addTarget(self, action: #selector(editDeviceIp2), for:.touchUpInside)
        ip2View.isUserInteractionEnabled  = true
        ip2View.addSubview(self.editIp2Btn)
        let ip2Line = UIView()
        ip2Line.backgroundColor = UIColor.gray
        ip2Line.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        ip2View.addSubview(ip2Line)
        
        port2View = UIView()
        port2View.heightAnchor.constraint(equalToConstant: 60).isActive = true
        port2View.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(port2View)
        self.port2Label = UILabel()
        self.port2Label.textColor = UIColor.black
        self.port2Label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.port2Label.text = NSLocalizedString("port2", comment: "port2:")
        self.port2Label.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.port2Label.numberOfLines = 0;
        self.port2Label.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        port2View.addSubview(self.port2Label)
        self.port2ContentLabel = UILabel()
        self.port2ContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.port2ContentLabel.textColor = UIColor.black
        self.port2ContentLabel.text = ""
        self.port2ContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        port2View.addSubview(self.port2ContentLabel)
        self.editPort2Btn = UIButton()
        self.editPort2Btn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editPort2Btn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editPort2Btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editPort2Btn.layer.cornerRadius = 15;
        self.editPort2Btn.layer.borderWidth = 1.0;
        self.editPort2Btn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editPort2Btn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editPort2Btn.isUserInteractionEnabled  = true
        self.editPort2Btn.addTarget(self, action: #selector(editDevicePort2), for:.touchUpInside)
        port2View.isUserInteractionEnabled  = true
        port2View.addSubview(self.editPort2Btn)
        let port2Line = UIView()
        port2Line.backgroundColor = UIColor.gray
        port2Line.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        port2View.addSubview(port2Line)
        
        apnAddrView = UIView()
        apnAddrView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        apnAddrView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(apnAddrView)
        self.apnAddrLabel = UILabel()
        self.apnAddrLabel.textColor = UIColor.black
        self.apnAddrLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.apnAddrLabel.text = NSLocalizedString("apn_addr", comment: "apnAddr:")
        self.apnAddrLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.apnAddrLabel.numberOfLines = 0;
        self.apnAddrLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        apnAddrView.addSubview(self.apnAddrLabel)
        self.apnAddrContentLabel = UILabel()
        self.apnAddrContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.apnAddrContentLabel.textColor = UIColor.black
        self.apnAddrContentLabel.text = ""
        self.apnAddrContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        apnAddrView.addSubview(self.apnAddrContentLabel)
        self.editApnAddrBtn = UIButton()
        self.editApnAddrBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editApnAddrBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editApnAddrBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editApnAddrBtn.layer.cornerRadius = 15;
        self.editApnAddrBtn.layer.borderWidth = 1.0;
        self.editApnAddrBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editApnAddrBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editApnAddrBtn.isUserInteractionEnabled  = true
        self.editApnAddrBtn.addTarget(self, action: #selector(editDeviceApnAddr), for:.touchUpInside)
        apnAddrView.isUserInteractionEnabled  = true
        apnAddrView.addSubview(self.editApnAddrBtn)
        let apnAddrLine = UIView()
        apnAddrLine.backgroundColor = UIColor.gray
        apnAddrLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        apnAddrView.addSubview(apnAddrLine)
        
        apnUsernameView = UIView()
        apnUsernameView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        apnUsernameView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(apnUsernameView)
        self.apnUsernameLabel = UILabel()
        self.apnUsernameLabel.textColor = UIColor.black
        self.apnUsernameLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.apnUsernameLabel.text = NSLocalizedString("apn_username", comment: "apnUsername:")
        self.apnUsernameLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.apnUsernameLabel.numberOfLines = 0;
        self.apnUsernameLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        apnUsernameView.addSubview(self.apnUsernameLabel)
        self.apnUsernameContentLabel = UILabel()
        self.apnUsernameContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.apnUsernameContentLabel.textColor = UIColor.black
        self.apnUsernameContentLabel.text = ""
        self.apnUsernameContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        apnUsernameView.addSubview(self.apnUsernameContentLabel)
        self.editApnUsernameBtn = UIButton()
        self.editApnUsernameBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editApnUsernameBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editApnUsernameBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editApnUsernameBtn.layer.cornerRadius = 15;
        self.editApnUsernameBtn.layer.borderWidth = 1.0;
        self.editApnUsernameBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editApnUsernameBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editApnUsernameBtn.isUserInteractionEnabled  = true
        self.editApnUsernameBtn.addTarget(self, action: #selector(editDeviceApnUsername), for:.touchUpInside)
        apnUsernameView.isUserInteractionEnabled  = true
        apnUsernameView.addSubview(self.editApnUsernameBtn)
        let apnNameLine = UIView()
        apnNameLine.backgroundColor = UIColor.gray
        apnNameLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        apnUsernameView.addSubview(apnNameLine)
        
        apnPwdView = UIView()
        apnPwdView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        apnPwdView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(apnPwdView)
        self.apnPwdLabel = UILabel()
        self.apnPwdLabel.textColor = UIColor.black
        self.apnPwdLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.apnPwdLabel.text = NSLocalizedString("apn_pwd", comment: "apnPwd:")
        self.apnPwdLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.apnPwdLabel.numberOfLines = 0;
        self.apnPwdLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        apnPwdView.addSubview(self.apnPwdLabel)
        self.apnPwdContentLabel = UILabel()
        self.apnPwdContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.apnPwdContentLabel.textColor = UIColor.black
        self.apnPwdContentLabel.text = ""
        self.apnPwdContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        apnPwdView.addSubview(self.apnPwdContentLabel)
        self.editApnPwdBtn = UIButton()
        self.editApnPwdBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editApnPwdBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editApnPwdBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editApnPwdBtn.layer.cornerRadius = 15;
        self.editApnPwdBtn.layer.borderWidth = 1.0;
        self.editApnPwdBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editApnPwdBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editApnPwdBtn.isUserInteractionEnabled  = true
        self.editApnPwdBtn.addTarget(self, action: #selector(editDeviceApnPwd), for:.touchUpInside)
        apnPwdView.isUserInteractionEnabled  = true
        apnPwdView.addSubview(self.editApnPwdBtn)
        let apnPwdLine = UIView()
        apnPwdLine.backgroundColor = UIColor.gray
        apnPwdLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        apnPwdView.addSubview(apnPwdLine)
        
        alarmSetView = UIView()
        alarmSetView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        alarmSetView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(alarmSetView)
        self.alarmSetLabel = UILabel()
        self.alarmSetLabel.textColor = UIColor.black
        self.alarmSetLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.alarmSetLabel.text = NSLocalizedString("alarm_set", comment: "alarmSet:")
        self.alarmSetLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.alarmSetLabel.numberOfLines = 0;
        self.alarmSetLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        alarmSetView.addSubview(self.alarmSetLabel)
        self.alarmSetContentLabel = UILabel()
        self.alarmSetContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.alarmSetContentLabel.textColor = UIColor.black
        self.alarmSetContentLabel.text = ""
        self.alarmSetContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        alarmSetView.addSubview(self.alarmSetContentLabel)
        self.editAlarmSetBtn = UIButton()
        self.editAlarmSetBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editAlarmSetBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editAlarmSetBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editAlarmSetBtn.layer.cornerRadius = 15;
        self.editAlarmSetBtn.layer.borderWidth = 1.0;
        self.editAlarmSetBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editAlarmSetBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editAlarmSetBtn.isUserInteractionEnabled  = true
        self.editAlarmSetBtn.addTarget(self, action: #selector(editAlarmSet), for:.touchUpInside)
        alarmSetView.isUserInteractionEnabled  = true
        alarmSetView.addSubview(self.editAlarmSetBtn)
        let alarmSetLine = UIView()
        alarmSetLine.backgroundColor = UIColor.gray
        alarmSetLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        alarmSetView.addSubview(alarmSetLine)
        tempAlarmView = UIView()
        tempAlarmView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        tempAlarmView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(tempAlarmView)
        self.tempAlarmLabel = UILabel()
        self.tempAlarmLabel.textColor = UIColor.black
        self.tempAlarmLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.tempAlarmLabel.text = NSLocalizedString("temp_alarm_set", comment: "tempAlarm:")
        self.tempAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.tempAlarmLabel.numberOfLines = 0;
        self.tempAlarmLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        tempAlarmView.addSubview(self.tempAlarmLabel)
        self.tempAlarmContentLabel = UILabel()
        self.tempAlarmContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.tempAlarmContentLabel.textColor = UIColor.black
        self.tempAlarmContentLabel.text = ""
        self.tempAlarmContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        tempAlarmView.addSubview(self.tempAlarmContentLabel)
        self.editTempAlarmBtn = UIButton()
        self.editTempAlarmBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editTempAlarmBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editTempAlarmBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editTempAlarmBtn.layer.cornerRadius = 15;
        self.editTempAlarmBtn.layer.borderWidth = 1.0;
        self.editTempAlarmBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editTempAlarmBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editTempAlarmBtn.isUserInteractionEnabled  = true
        self.editTempAlarmBtn.addTarget(self, action: #selector(editDeviceTempAlarm), for:.touchUpInside)
        tempAlarmView.isUserInteractionEnabled  = true
        tempAlarmView.addSubview(self.editTempAlarmBtn)
        let tempAlarmLine = UIView()
        tempAlarmLine.backgroundColor = UIColor.gray
        tempAlarmLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        tempAlarmView.addSubview(tempAlarmLine)
        
        broadcastIntervalView = UIView()
        broadcastIntervalView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        broadcastIntervalView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(broadcastIntervalView)
        self.broadcastIntervalLabel = UILabel()
        self.broadcastIntervalLabel.textColor = UIColor.black
        self.broadcastIntervalLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.broadcastIntervalLabel.text = NSLocalizedString("broadcast_interval", comment: "broadcastInterval:")
        self.broadcastIntervalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.broadcastIntervalLabel.numberOfLines = 0;
        self.broadcastIntervalLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        broadcastIntervalView.addSubview(self.broadcastIntervalLabel)
        self.broadcastIntervalContentLabel = UILabel()
        self.broadcastIntervalContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.broadcastIntervalContentLabel.textColor = UIColor.black
        self.broadcastIntervalContentLabel.text = ""
        self.broadcastIntervalContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        broadcastIntervalView.addSubview(self.broadcastIntervalContentLabel)
        self.editBroadcastIntervalBtn = UIButton()
        self.editBroadcastIntervalBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editBroadcastIntervalBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editBroadcastIntervalBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editBroadcastIntervalBtn.layer.cornerRadius = 15;
        self.editBroadcastIntervalBtn.layer.borderWidth = 1.0;
        self.editBroadcastIntervalBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editBroadcastIntervalBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editBroadcastIntervalBtn.isUserInteractionEnabled  = true
        self.editBroadcastIntervalBtn.addTarget(self, action: #selector(editDeviceBroadcastInterval), for:.touchUpInside)
        broadcastIntervalView.isUserInteractionEnabled  = true
        broadcastIntervalView.addSubview(self.editBroadcastIntervalBtn)
        let broadcastIntervalLine = UIView()
        broadcastIntervalLine.backgroundColor = UIColor.gray
        broadcastIntervalLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        broadcastIntervalView.addSubview(broadcastIntervalLine)
        
        
        longRangeView = UIView()
        longRangeView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        longRangeView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(longRangeView)
        self.longRangeLabel = UILabel()
        self.longRangeLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.longRangeLabel.textColor = UIColor.black
        self.longRangeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.longRangeLabel.numberOfLines = 0;
        self.longRangeLabel.text = "Long range"
        
        self.longRangeLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        longRangeView.addSubview(self.longRangeLabel)
        self.longRangeSwitch = UISwitch()
        self.longRangeSwitch.frame = CGRect(x: contentX, y: 10, width: 70, height: 30)
        self.longRangeSwitch.addTarget(self, action: #selector(longRangeSwitchAction),
                                       for:UIControl.Event.valueChanged)
        longRangeView.addSubview(self.longRangeSwitch)
        let longRangeLine = UIView()
        longRangeLine.backgroundColor = UIColor.gray
        longRangeLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        longRangeView.addSubview(longRangeLine)
        
        transmittedPowerView = UIView()
        transmittedPowerView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        transmittedPowerView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(transmittedPowerView)
        self.transmittedPowerLabel = UILabel()
        self.transmittedPowerLabel.textColor = UIColor.black
        self.transmittedPowerLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.transmittedPowerLabel.text = NSLocalizedString("transmitted_power", comment: "transmittedPower:")
        self.transmittedPowerLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.transmittedPowerLabel.numberOfLines = 0;
        self.transmittedPowerLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        transmittedPowerView.addSubview(self.transmittedPowerLabel)
        self.transmittedPowerContentLabel = UILabel()
        self.transmittedPowerContentLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.transmittedPowerContentLabel.textColor = UIColor.black
        self.transmittedPowerContentLabel.text = ""
        self.transmittedPowerContentLabel.frame = CGRect(x: contentX, y: 0, width: contentX, height: 60)
        transmittedPowerView.addSubview(self.transmittedPowerContentLabel)
        self.editTransmittedPowerBtn = UIButton()
        self.editTransmittedPowerBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editTransmittedPowerBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editTransmittedPowerBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editTransmittedPowerBtn.layer.cornerRadius = 15;
        self.editTransmittedPowerBtn.layer.borderWidth = 1.0;
        self.editTransmittedPowerBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editTransmittedPowerBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editTransmittedPowerBtn.isUserInteractionEnabled  = true
        self.editTransmittedPowerBtn.addTarget(self, action: #selector(editDeviceTransmittedPower), for:.touchUpInside)
        transmittedPowerView.isUserInteractionEnabled  = true
        transmittedPowerView.addSubview(self.editTransmittedPowerBtn)
        let transmittedPowerLine = UIView()
        transmittedPowerLine.backgroundColor = UIColor.gray
        transmittedPowerLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        transmittedPowerView.addSubview(transmittedPowerLine)
        
        ledView = UIView()
        ledView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        ledView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(ledView)
        self.ledLabel = UILabel()
        self.ledLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.ledLabel.textColor = UIColor.black
        self.ledLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.ledLabel.numberOfLines = 0;
        self.ledLabel.text = "LED"
        self.ledLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        ledView.addSubview(self.ledLabel)
        self.ledSwitch = UISwitch()
        self.ledSwitch.frame = CGRect(x: contentX, y: 10, width: 70, height: 30)
        self.ledSwitch.addTarget(self, action: #selector(ledSwitchAction),
                                 for:UIControl.Event.valueChanged)
        ledView.addSubview(self.ledSwitch)
        let ledLine = UIView()
        ledLine.backgroundColor = UIColor.gray
        ledLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        ledView.addSubview(ledLine)
        
        buzzerView = UIView()
        buzzerView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        buzzerView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(buzzerView)
        self.buzzerLabel = UILabel()
        self.buzzerLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.buzzerLabel.textColor = UIColor.black
        self.buzzerLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.buzzerLabel.numberOfLines = 0;
        self.buzzerLabel.text = NSLocalizedString("buzzer", comment:"Buzzer")
        self.buzzerLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        buzzerView.addSubview(self.buzzerLabel)
        self.buzzerSwitch = UISwitch()
        self.buzzerSwitch.frame = CGRect(x: contentX, y: 10, width: 70, height: 30)
        self.buzzerSwitch.addTarget(self, action: #selector(buzzerSwitchAction),
                                    for:UIControl.Event.valueChanged)
        buzzerView.addSubview(self.buzzerSwitch)
        let buzzerLine = UIView()
        buzzerLine.backgroundColor = UIColor.gray
        buzzerLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        buzzerView.addSubview(buzzerLine)
        
        
        clearHisDataView = UIView()
        clearHisDataView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        clearHisDataView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(clearHisDataView)
        self.clearHisDataLabel = UILabel()
        self.clearHisDataLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.clearHisDataLabel.textColor = UIColor.black
        self.clearHisDataLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.clearHisDataLabel.numberOfLines = 0;
        self.clearHisDataLabel.text =  NSLocalizedString("clear_his_data", comment:"clear_his_data:")
        self.clearHisDataLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        clearHisDataView.addSubview(self.clearHisDataLabel)
        self.editClearHisDataBtn = UIButton()
        self.editClearHisDataBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editClearHisDataBtn.setTitle(NSLocalizedString("clear_his_data", comment:"clear_his_data"), for: .normal)
        self.editClearHisDataBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editClearHisDataBtn.layer.cornerRadius = 15;
        self.editClearHisDataBtn.layer.borderWidth = 1.0;
        self.editClearHisDataBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editClearHisDataBtn.addTarget(self, action: #selector(clearHisDataClick), for:.touchUpInside)
        self.editClearHisDataBtn.frame = CGRect(x: contentX, y: 15, width: 120, height: btnHeight)
        clearHisDataView.addSubview(self.editClearHisDataBtn)
        let clearHisDataLine = UIView()
        clearHisDataLine.backgroundColor = UIColor.gray
        clearHisDataLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        clearHisDataView.addSubview(clearHisDataLine)
        
        
        resetDefaultView = UIView()
        resetDefaultView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        resetDefaultView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(resetDefaultView)
        self.resetDefaultLabel = UILabel()
        self.resetDefaultLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.resetDefaultLabel.textColor = UIColor.black
        self.resetDefaultLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.resetDefaultLabel.numberOfLines = 0;
        self.resetDefaultLabel.text =  NSLocalizedString("reset_default", comment:"reset_default")
        self.resetDefaultLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        resetDefaultView.addSubview(self.resetDefaultLabel)
        self.editResetDefaultBtn = UIButton()
        self.editResetDefaultBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editResetDefaultBtn.setTitle(NSLocalizedString("reset_default", comment:"reset_default"), for: .normal)
        self.editResetDefaultBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editResetDefaultBtn.layer.cornerRadius = 15;
        self.editResetDefaultBtn.layer.borderWidth = 1.0;
        self.editResetDefaultBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editResetDefaultBtn.addTarget(self, action: #selector(resetDefaultClick), for:.touchUpInside)
        self.editResetDefaultBtn.frame = CGRect(x: contentX, y: 15, width: 120, height: btnHeight)
        resetDefaultView.addSubview(self.editResetDefaultBtn)
        let resetDefaultLine = UIView()
        resetDefaultLine.backgroundColor = UIColor.gray
        resetDefaultLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        resetDefaultView.addSubview(resetDefaultLine)
        
        rebootView = UIView()
        rebootView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        rebootView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(rebootView)
        self.rebootLabel = UILabel()
        self.rebootLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.rebootLabel.textColor = UIColor.black
        self.rebootLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.rebootLabel.numberOfLines = 0;
        self.rebootLabel.text =  NSLocalizedString("reboot", comment:"reboot:")
        self.rebootLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        rebootView.addSubview(self.rebootLabel)
        self.editRebootBtn = UIButton()
        self.editRebootBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editRebootBtn.setTitle(NSLocalizedString("reboot", comment:"reboot"), for: .normal)
        self.editRebootBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editRebootBtn.layer.cornerRadius = 15;
        self.editRebootBtn.layer.borderWidth = 1.0;
        self.editRebootBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editRebootBtn.addTarget(self, action: #selector(rebootClick), for:.touchUpInside)
        self.editRebootBtn.frame = CGRect(x: contentX, y: 15, width: 120, height: btnHeight)
        rebootView.addSubview(self.editRebootBtn)
        let rebootLine = UIView()
        rebootLine.backgroundColor = UIColor.gray
        rebootLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        rebootView.addSubview(rebootLine)
        
        shutdownView = UIView()
        shutdownView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        shutdownView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(shutdownView)
        self.shutdownLabel = UILabel()
        self.shutdownLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.shutdownLabel.textColor = UIColor.black
        self.shutdownLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.shutdownLabel.numberOfLines = 0;
        self.shutdownLabel.text =  NSLocalizedString("shutdown", comment:"Shutdown:")
        self.shutdownLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        shutdownView.addSubview(self.shutdownLabel)
        self.editShutdownBtn = UIButton()
        self.editShutdownBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editShutdownBtn.setTitle(NSLocalizedString("shutdown", comment:"Shutdown"), for: .normal)
        self.editShutdownBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.editShutdownBtn.layer.cornerRadius = 15;
        self.editShutdownBtn.layer.borderWidth = 1.0;
        self.editShutdownBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.editShutdownBtn.addTarget(self, action: #selector(shutdownClick), for:.touchUpInside)
        self.editShutdownBtn.frame = CGRect(x: contentX, y: 15, width: 120, height: btnHeight)
        shutdownView.addSubview(self.editShutdownBtn)
        let shutdownLine = UIView()
        shutdownLine.backgroundColor = UIColor.gray
        shutdownLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        shutdownView.addSubview(shutdownLine)
        
        
        
        resetFactoryView = UIView()
        resetFactoryView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        resetFactoryView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(resetFactoryView)
        self.resetFactoryLabel = UILabel()
        self.resetFactoryLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.resetFactoryLabel.textColor = UIColor.black
        self.resetFactoryLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.resetFactoryLabel.numberOfLines = 0;
        self.resetFactoryLabel.text =  NSLocalizedString("factory_reset", comment:"Reset factory:")
        self.resetFactoryLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        resetFactoryView.addSubview(self.resetFactoryLabel)
        self.editResetFactoryBtn = UIButton()
        self.editResetFactoryBtn.titleLabel?.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.editResetFactoryBtn.setTitle(NSLocalizedString("factory_reset", comment:"Reset factory"), for: .normal)
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
        
        initDiffUI()
        
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
        // self.view.addSubview(self.uniqueIDLabel)
        // self.view.addSubview(self.uniqueIDDescLabel)
        // contentY = contentY + 25
        self.dateLabel.frame = CGRect(x: dateLabelX, y: contentY, width: 60, height: 30)
        self.dateLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.dateDescLabel.frame = CGRect(x: dateLabelX+60, y: contentY, width: 200, height: 30)
        self.dateDescLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        // self.view.addSubview(self.dateLabel)
        // self.view.addSubview(self.dateDescLabel)
        // contentY = contentY + 25
        lockStatusImg = UIImageView.init(frame: CGRect(x: (Int)(self.view.bounds.size.width / 2 - 75), y: contentY, width: 150, height: 150))
        lockStatusImg.contentMode = .scaleAspectFit;
        lockStatusImg.image = UIImage (named: "ic_lock.png")
        lockStatusImg.backgroundColor = UIColor.clear
        // contentY = contentY + 150
        // self.view.addSubview(lockStatusImg)
        self.lockStatusLabel = UILabel.init(frame: CGRect(x: 30, y: contentY, width: Int(self.view.bounds.size.width)-60, height: 60))
        self.lockStatusLabel.textAlignment = .center
        self.lockStatusLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.lockStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping
        self.lockStatusLabel.numberOfLines = 0
        // self.view.addSubview(self.lockStatusLabel)
        // contentY = contentY + 50
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
        // self.view.addSubview(unlockBtn)
        //        let activeNetwork = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width -  self.view.bounds.size.width / 3 / 2 - 40), y: contentY, width: 80, height: 30))
        activeNetworkBtn = UIButton.init(frame:CGRect(x: (Int)(self.view.bounds.size.width - self.view.bounds.size.width / 2 / 2  - 40), y: contentY, width: 80, height: 30))
        activeNetworkBtn.setTitle( NSLocalizedString("active_network", comment: "Active Network"), for: .normal)
        activeNetworkBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        activeNetworkBtn.layer.cornerRadius = 15;
        activeNetworkBtn.layer.borderWidth = 1.0;
        activeNetworkBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        activeNetworkBtn.addTarget(self, action: #selector(activeNetworkClick), for:.touchUpInside)
        activeNetworkBtn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        // self.view.addSubview(activeNetworkBtn)
        // contentY = contentY + 30
        
        initConfigUI(startY: contentY)
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
extension EditController:EditAccessPwdDelegate{
    
    func setNewPwd(newPwd: String) {
        print("set new pwd")
        print(newPwd)
        TftBleConnectManager.getInstance().setOnThisView(true)
        self.newPwd = newPwd
        self.changeBlePwd(self.blePwd, newPwd)
    }
}
extension EditController:EditLockPwdDelegate{
    
    func setNewPwd(oldPwd:String,newPwd: String) {
        print("set new pwd")
        print(newPwd)
        TftBleConnectManager.getInstance().setOnThisView(true)
        self.leaveViewNeedDisconnect = true
        self.changeUnlockPwd(oldPwd, newPwd)
    }
}
extension EditController:EditIpDelegate{
    
    func setNewIp(ipType:Int,ip:String,port: Int){
        print("setNewIp")
        TftBleConnectManager.getInstance().setOnThisView(true)
        self.leaveViewNeedDisconnect = true
        self.writeDomain(ipType: ipType, ip: ip, port: port)
    }
}

extension EditController:EditTimerDelegate{
    func setNewTimer(accOn:Int,accOff:Int64,angle:Int,distance: Int){
        print("setNewTimer")
        TftBleConnectManager.getInstance().setOnThisView(true)
        self.leaveViewNeedDisconnect = true
        self.writeTimer(accOn: accOn, accOff: accOff, angle: angle, distance: distance)
    }
    
}
extension EditController:SetConnectStatusDelegate{
    func setConnectStatus() {
        TftBleConnectManager.getInstance().setOnThisView(true)
        self.leaveViewNeedDisconnect = true
    }
}
extension EditController:EditTempAlarmDelegate{
    func setNewTemp(tempHigh: Int, tempLow: Int) {
        TftBleConnectManager.getInstance().setOnThisView(true)
        self.leaveViewNeedDisconnect = true
        self.writeTempAlarm(tempHigh: tempHigh, tempLow: tempLow)
    }
    
    
}
extension EditController:EditAlarmOpenDelegate{
    func setNewAlarmValue(newValue: Int) {
        TftBleConnectManager.getInstance().setOnThisView(true)
        self.leaveViewNeedDisconnect = true
        self.writeAlarmOpenSet(alarmOpenSet: newValue)
    }
    
    
}
