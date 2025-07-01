//
//  ReadSubLockHisDataController.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/5/30.
//  Copyright © 2025 com.tftiot. All rights reserved.
//


import Foundation


import Foundation
import UIKit
import CoreBluetooth
import CLXToast
import ActionSheetPicker_3_0
import SwiftPopMenu
import MessageUI
import xlsxwriter

//extension UIView {
//
//    func addOnClickListener(target: AnyObject, action: Selector) {
//        let gr = UITapGestureRecognizer(target: target, action: action)
//        gr.numberOfTapsRequired = 1
//        isUserInteractionEnabled = true
//        addGestureRecognizer(gr)
//    }
//
//}


class ReadSubLockHisDataController:UIViewController,UITableViewDataSource,UITableViewDelegate,MFMailComposeViewControllerDelegate, HistorySelectDelegate,BleStatusCallback,UpgradeStatusCallback{
    
    
    func onUpgradeStatus(status: Int, percent: Float) {
         
    }
    
    
    func onUpgradeNotifyValue(_ value: [UInt8]) {
        
    }
    
    func onNotifyValue(_ value: [UInt8]) { 
        canSendMsgLock.lock()
        isCanSendMsg = true
        canSendMsgLock.unlock() 
        self.parseResp(value)
    }
    
    func onBleStatusCallback(_ connectStatus: Int) {
        switch connectStatus {
        case TftBleConnectManager.BLE_STATUS_OF_CLOSE:
            print("BLE Status: Closed")
            if self.waitingView != nil {
                self.waitingView.dismiss()
            }
            if self.waitingCancelDlg != nil {
                self.waitingCancelDlg.dismiss()
            }
            if self.progressView != nil{
                self.progressView.dismiss()
            }
            self.updateConnectStatusBtn(isConnect:false)
        case TftBleConnectManager.BLE_STATUS_OF_DISCONNECT:
            print("BLE Status: Disconnected")
            if self.waitingView != nil {
                self.waitingView.dismiss()
            }
            if self.waitingCancelDlg != nil {
                self.waitingCancelDlg.dismiss()
            }
            if self.progressView != nil{
                self.progressView.dismiss()
            }
            self.updateConnectStatusBtn(isConnect:false)
        case TftBleConnectManager.BLE_STATUS_OF_CONNECTING:
            print("BLE Status: Connecting")
            self.updateConnectStatusBtn(isConnect:false)
        case TftBleConnectManager.BLE_STATUS_OF_CONNECT_SUCC:
            print("BLE Status: Connected Successfully")
            self.updateConnectStatusBtn(isConnect:true)
            self.doAfterConnectSucc()
             if !TftBleConnectManager.getInstance().getDeviceReady() {
                 self.showWaitingCancelWin(title: NSLocalizedString("waitingDeviceReady", comment: "waitingDeviceReady") );

            }
            if self.waitingView != nil {
                self.waitingView.dismiss()
            }
            if self.waitingCancelDlg != nil {
                self.waitingCancelDlg.dismiss()
            }
        case TftBleConnectManager.BLE_STATUS_OF_SCANNING:
            print("BLE Status: Scanning")
            self.updateConnectStatusBtn(isConnect:false)
        default:
            print("Unknown BLE Status")
        }
        
    }
 
    func  doAfterConnectSucc(){
        allDatas.removeAll()
        tempDatas.removeAll() 
        self.currentPage = 1
        self.maxPage = 1
        self.pageNumberLabel.text = "1"
        self.maxPageNumberLabel.text = "1"
        self.reloadReportData()
    } 
  
    private var bleByteBuf:TopflytechByteBuf = TopflytechByteBuf()
    private var isLightWarning:Bool = false
    private var isReadData:Bool = false
    private var isDataReady:Bool = false
    private var lastReceiveDataDate:Date = Date()
    private var waitingCancelDlg:AEUIAlertView!
    let tableDateFormat = DateFormatter()
    var tempDatas = [BleDeviceData]()
    var allDatas = [BleDeviceData]()
    var byteBuf = TopflytechByteBuf()
    
    func parseResp(_ respContent: [UInt8]) {
        if respContent[0] == 0x20 && respContent[1] == 0x00 && respContent[2] == 0x01 {
             let status = respContent[5]
             if status == 0x01 {
                 if !TftBleConnectManager.getInstance().getDeviceReady(){
//                    sendGetData(false, false)
                     changeBlePwd(curPwd, curPwd)
                 }
                 TftBleConnectManager.getInstance().setDeviceReady(ready: true)
             }
        }else{
             var dataList = Utils.parseRespContent(content: respContent)
            for bleRespData in dataList {
                print("resp code:\(bleRespData.controlCode)")
                if bleRespData.type == BleRespData.READ_TYPE || bleRespData.type == BleRespData.WRITE_TYPE {
                     self.parseReadResp(bleRespData: bleRespData)
                }else{
                    if bleRespData.errorCode == BleRespData.ERROR_CODE_OF_PWD_ERROR {
                        Toast.hudBuilder.title(NSLocalizedString("ble_pwd_error", comment: "Ble password is error")).show()
                        showPwdWin(isNextShowDateDlg: false)
                    }else{
                        if TftBleConnectManager.getInstance().isOnThsView(){
                            Toast.hudBuilder.title(NSLocalizedString("fail", comment: "Fail")).show()
                        }
                    }
                    
                }
                
            }
        } 
    }
    
    private func changeBlePwd(_ oldPwd: String, _ newPwd: String) {
        do {
            var outputStream: [UInt8] = []
            outputStream.append(contentsOf: oldPwd.utf8)
            outputStream.append(contentsOf: newPwd.utf8)
             
            let arrayList = Utils.getWriteCmdContent(cmdCode: BleDeviceData.func_id_of_ble_pwd_change, content: outputStream,pwd:nil)
            TftBleConnectManager.getInstance().writeArrayContent(writeContentList: arrayList)
        } catch {
            print("Error changing BLE password: \(error)")
        }
    }
    private var multiRespDataQueueMap: [Int: [BleRespData]] = [:]
    private func getAllDataBytes(_ dataList: [BleRespData]) -> [UInt8] {
        var result: [UInt8] = []
        for data in dataList {
            if let dataBytes = data.data {
                result.append(contentsOf: dataBytes)
            }
        }
        return result
    }
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
    func parseReadResp(bleRespData:BleRespData){
        let code = bleRespData.controlCode
        if code == BleDeviceData.func_id_of_read_sub_lock_his_data{
            guard let respData = getMultiRespData(bleRespData, code!) else {
                return
            }
            if respData == nil || respData.count == 0{
                return
            }
            var isReadEnd = respData[0] == 0x00
            let dataCount = Int(respData[1])
            var readIndex = 2
            let dateFormatter = DateFormatter()
            // setup formate string for the date formatter
            dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss" 
      
            for i in 0..<dataCount{ 
                if readIndex  >= respData.count{
                    break
                }
                let bleDeviceData = BleDeviceData()
                let curPackageLen = Int(respData[readIndex])
                readIndex += 1
                var curPackageIndex = readIndex
                if curPackageIndex + curPackageLen > respData.count{
                    break
                }
                readIndex += curPackageLen
                let timestamp = Utils.unsigned4BytesToInt(respData,curPackageIndex)
                curPackageIndex += 4
                let alarm = respData[curPackageIndex]
                curPackageIndex += 1
                let lockType = respData[curPackageIndex]
                curPackageIndex += 1
                let deviceStatus = Utils.bytes2Short(bytes: respData, offset: curPackageIndex)
                curPackageIndex += 2
                let voltageTemp = Utils.bytes2Short(bytes: respData, offset: curPackageIndex)
                let voltage = Float(voltageTemp) / 1000
                curPackageIndex += 2
                let batteryPercent = respData[curPackageIndex]
                curPackageIndex += 1
                let solarVoltageTemp = Float(Utils.bytes2Short(bytes: respData, offset: curPackageIndex)) / 1000.0
                curPackageIndex += 2
                var temp = Int(respData[curPackageIndex] & 0xff)
                curPackageIndex += 1
                if temp == 0xff {
                    temp = -999
                }else {
                    temp = temp - 80
                }
                if curPackageIndex < readIndex{
                    let type = respData[curPackageIndex]
                    curPackageIndex += 1
                    if type == 0x01{
                        let carLen = Int(respData[curPackageIndex])
                        if curPackageIndex + 1 + carLen <= readIndex{
                            let macData = respData[curPackageIndex+1...curPackageIndex+carLen]
                            let mac = Utils.bytes2HexString(bytes:[UInt8](macData),pos: 0)
                            bleDeviceData.macId = mac as NSString
                             curPackageIndex += carLen + 1
                        }
                    }else if type == 0x00{ 
                        let carLen = Int(respData[curPackageIndex])
                        if curPackageIndex + 1 + carLen <= readIndex{
                            let nfcData = respData[curPackageIndex+1...curPackageIndex+carLen]
                            let nfc = Utils.bytes2HexString(bytes: [UInt8](nfcData),pos: 0)
                            bleDeviceData.nfcId = nfc as NSString
                             curPackageIndex += carLen + 1
                        }
                    }
                }
                if curPackageIndex < readIndex{
                    let type = respData[curPackageIndex]
                    curPackageIndex += 1
                    if type == 0x01{
                        let carLen = Int(respData[curPackageIndex])
                        if curPackageIndex + 1 + carLen <= readIndex{
                            let macData = respData[curPackageIndex+1...curPackageIndex+carLen]
                            let mac = Utils.bytes2HexString(bytes: [UInt8](macData),pos: 0)
                            bleDeviceData.macId = mac as NSString
                             curPackageIndex += carLen + 1
                        }
                    }else if type == 0x00{ 
                        let carLen = Int(respData[curPackageIndex])
                        if curPackageIndex + 1 + carLen <= readIndex{
                            let nfcData = respData[curPackageIndex+1...curPackageIndex+carLen]
                            let nfc = Utils.bytes2HexString(bytes: [UInt8](nfcData),pos: 0)
                            bleDeviceData.nfcId = nfc as NSString
                             curPackageIndex += carLen + 1
                        }
                    }
                }
                bleDeviceData.id = mac
                bleDeviceData.parseSubLockStatus(lockStatus:deviceStatus)
                bleDeviceData.voltage = voltage
                bleDeviceData.solarVoltage = solarVoltageTemp
                bleDeviceData.isSubLock = true
                bleDeviceData.temp = Float(temp)
                bleDeviceData.batteryPercent = Int(batteryPercent)
                let date = Date(timeIntervalSince1970: TimeInterval(timestamp))
                bleDeviceData.date = dateFormatter.string(from: date) 
                bleDeviceData.lockType = Int(lockType)
                bleDeviceData.model = BleDeviceData.MODEL_OF_SGX120B01
                if readEndDate != nil && timestamp > readEndDate{
                    isReadEnd = true
                    break
                }
                bleDeviceData.subLockAlarm = Int(alarm)
                tempReceiveDataCount+=1
                tempDatas.append(bleDeviceData)
                  if waitingCancelDlg != nil{
                    self.waitingCancelDlg.title = NSLocalizedString("receive_count", comment: "") + "\(tempDatas.count + allDatas.count)"
                }
                if !isReadEnd{
                    sendGetData(false, true)
                }else{
                    isReadData = false;
                    showReadDataWaiting(isReadData)
                    if waitingCancelDlg != nil{
                        waitingCancelDlg?.dismiss()
                    }
                    reloadReportData()
                }
                if(tempReceiveDataCount > 10){
                    tempReceiveDataCount = 0 
                    reloadReportData()
                }
            }


        }else if code == BleDeviceData.func_id_of_ble_pwd_change{
            sendGetData(false, false)
        }
    }
    
  
    
    func reloadReportData(){
        allDatas.append(contentsOf: tempDatas)
        tempDatas.removeAll()
        if(maxPage == currentPage){
            updatePageData(pageNumb: currentPage)
        }
        maxPage = allDatas.count % pageSize == 0 ? allDatas.count / pageSize : allDatas.count / pageSize + 1;
        maxPageNumberLabel.text = String(maxPage)
        if showDetailList.count == 0{
            updatePageData(pageNumb: currentPage)
        }
    }
    func  updatePageData(pageNumb:Int){
        if allDatas.count < (currentPage - 1) * pageSize{
            return
        }
        var startIndex = (pageNumb - 1) * pageSize
        if(startIndex <= 0){
            startIndex = 0
        }
        var endIndex = pageNumb * pageSize
        if(endIndex > allDatas.count){
            endIndex = allDatas.count
        }
        showDetailList.removeAll()
        for i in startIndex..<endIndex {
            let data = allDatas[i]
            showDetailList.append(data)
        }
        tableViewContainer.reloadData()
    }
    var tempReceiveDataCount:Int = 0
     
    func showWaitingCancelWin(title:String){
        if self.waitingCancelDlg != nil && !self.waitingCancelDlg.isDismiss{
            self.waitingCancelDlg.title = title
            self.waitingCancelDlg.show()
            return
        }
        self.waitingCancelDlg = AEUIAlertView(style: .textField, title: title, message: nil)
        self.waitingCancelDlg.actions = []
        self.waitingCancelDlg.resetActions()
        
        let animation = UIView(frame: CGRect(x: 0, y: 0, width: 80, height: 80))
        let anim = AEBeginLineAnimation.initShow(in: animation.bounds, lineWidth: 4, lineColor: UIColor.blue)
        animation.addSubview(anim)
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
                           self.waitingCancelDlg.dismiss()
                           self.navigationController?.popViewController(animated: true)
                       }
                self.waitingCancelDlg.addAction(action: action_one)
        self.waitingCancelDlg.textField.isHidden = true
        self.waitingCancelDlg.set(animation: animation, width: 80, height: 80)
        self.waitingCancelDlg.show()
        
    }
    
    private var readStartDate: Int64 = -1, readEndDate: Int64 = -1
    private var startDateStr: String?, endDateStr: String?
    let getDataHead: [UInt8] = [0x60, 0x07, 0xE0]
    func getReadHisCmd(startDate: Int64,isGetNext: Bool) -> [UInt8] {
        var startDateByte = Utils.unSignedInt2Bytes(startDate) 
        var outputStream = Data()
        outputStream.append(0x00)
        if !isGetNext {
            outputStream.append(0x00)
             outputStream.append(Data(startDateByte)) 
        }else{
               outputStream.append(0xff)
        } 
       
        let content = [UInt8](outputStream)
        return Utils.getReadCmdContent(cmdCode: BleDeviceData.func_id_of_read_sub_lock_his_data, content: content)
    }
    
    private func sendGetData(_ checkDeviceReady: Bool, _ isGetNext: Bool) {
        if  readStartDate != -1 &&  readEndDate != -1 {
            let cmd = getReadHisCmd(startDate: readStartDate, isGetNext: isGetNext)
            if checkDeviceReady {
                TftBleConnectManager.getInstance().sendCheckDeviceReadyCmd()
            } else {
                isReadData = true
                showReadDataWaiting(isReadData)
                lastReceiveDataDate = Date()
                writeContent(content: cmd)
            }
        }
    }
    
    private func showReadDataWaiting(_ isShow: Bool) {
        if isShow {
            activityIndicatorView.isHidden = false
            activityIndicatorView.startAnimating()
        } else {
            activityIndicatorView.isHidden = true
            activityIndicatorView.stopAnimating()
        }
    }
     
    
   
    
    var allCmdWaitingSendCount = 0
    var sendMsgThread: Thread?
    var canSendMsgLock = NSCondition()
    var bleSensorCount = 0
    var model:String!
    private var progressView:AEAlertView!
    private var progressBar:MyProgress!
    private var waitingView:AEUIAlertView!
    private var pwdAlert:AEUIAlertView!
    var barLabel:UILabel!
//    var centralManager   : CBCentralManager!
    var cbPeripheral:CBPeripheral!
//    private var selfPeripheral:CBPeripheral!
//    private var notifyCharacteristic: CBCharacteristic?
//    private var writeCharacteristic: CBCharacteristic?
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
    private var curPwd:String!
    var lastCheckStatusDate:Date = Date()
    let getStatusTimeout = 4
    var sendMsgQueue:MsgQueue = MsgQueue()
    var sendMsgMultiQueue:MsgQueue = MsgQueue()
    var lockStatusImg:UIImageView!
    var lockStatusLabel:UILabel!
    private var uniqueID = ""
    override func viewDidDisappear(_ animated: Bool) {
         if self.leaveViewNeedDisconnect{
            TftBleConnectManager.getInstance().setOnThisView(false)
            TftBleConnectManager.getInstance().removeCallback(activityName: "ReadSubLockHisDataController")
            TftBleConnectManager.getInstance().disconnect()
        }
       
        self.onView = false
        self.needConnect = false
    }
    private var isShowPwdDlg = false
    func showPwdWin(isNextShowDateDlg:Bool){
        if self.isShowPwdDlg{
            return
        }
        self.isShowPwdDlg = true
        if self.pwdAlert != nil && !self.pwdAlert.isDismiss{
            self.pwdAlert.show()
            return
        }
        self.pwdAlert = AEUIAlertView(style: .password, title: NSLocalizedString("input_ble_open_lock_pwd", comment:"Please enter your password"), message: nil)
        self.pwdAlert.textField.placeholder = NSLocalizedString("input_ble_open_lock_pwd", comment:"Please enter your password")
      
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.pwdAlert.dismiss()
            self.navigationController?.popViewController(animated: true)
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let pwd = String(self.pwdAlert.textField.text ?? "")
            if pwd.count == 6{
                self.curPwd = pwd
                self.isShowPwdDlg = false
                self.pwdAlert.dismiss()
                if(isNextShowDateDlg){
                    self.chooseDate()
                }else{
                    self.afterSelectDateDoInView()
               }
            }else{
                Toast.hudBuilder.title(NSLocalizedString("pwd_format_error", comment: "Value is incorrect!The length has to be 6 digits")).show()
            }
        }
        self.pwdAlert.addAction(action: action_one)
        self.pwdAlert.addAction(action: action_two)
        self.pwdAlert.show()
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        tableDateFormat.dateFormat = "yyyy-MM-dd HH:mm:ss"
        tableDateFormat.locale = Locale(identifier: "en_US_POSIX")
        uniqueID = UniqueIDTool.getMediaDrmID()
        print("uid:\(uniqueID)")
        self.navigationController!.navigationBar.isTranslucent = false
        self.extendedLayoutIncludesOpaqueBars = true
//        self.makeSomeData()
        self.initUI()
        self.initNavBar()
        TftBleConnectManager.getInstance().initManager()
        TftBleConnectManager.getInstance().setOnThisView(true)
        TftBleConnectManager.getInstance().setCallback(activityName: "ReadSubLockHisDataController", callback: self)
        TftBleConnectManager.getInstance().setIsNeedGetLockStatus(isNeedGetLockStatus: false)
        initSendMsgThread()
        self.showPwdWin(isNextShowDateDlg: true)
       
        print("view did load")
    }
    func initSendMsgThread(){
        if sendMsgThreadworking{
            return
        }
        self.sendMsgThread = Thread(target: self, selector: #selector(sendMsgThreadFunc), object: nil)
        self.sendMsgThread?.start()
    }
    func connectDevice(){
        self.onView = true
        self.needConnect = true
//        let mac = self.cbPeripheral.identifier.uuidString
//        self.centralManager = CBCentralManager(delegate: self, queue: nil)
        TftBleConnectManager.getInstance().connect(connectPeripheral: self.cbPeripheral, isSubLock:BleDeviceData.isSubLockDevice(model: self.model))
        self.showWaitingCancelWin(title: NSLocalizedString("connecting", comment: "Connecting"))
    }
    var refreshConnectBtn:UIButton!
    var lightAlarmBtn:UIButton!
    var menuSelectBtn:UIButton!
    let imageSize = CGSize(width: 24, height: 24)
    let renderer = UIGraphicsImageRenderer(size: CGSize(width: 24, height: 24))
    func initNavBar(){
        barLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        //                titleLabel.text = "Bluetooth sensor"
        barLabel.text =  self.name
        barLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        self.navigationItem.titleView = barLabel
        
        
        
        let menuImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: "ic_list.png")
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
        let lightImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: "light_blue.png")
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
        let refreshImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: "ic_disconnect.png")
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
        lightAlarmBtn = UIButton(type: .custom) as! UIButton
        lightAlarmBtn.setImage(lightImage, for: .normal)
        lightAlarmBtn.addTarget(self, action: #selector(lightAlarmCancelClick), for:.touchUpInside)
        lightAlarmBtn.frame = CGRectMake(0, 0, 30, 30)
        let lightBarBtn = UIBarButtonItem(customView: lightAlarmBtn)
        
        menuSelectBtn = UIButton(type: .custom) as! UIButton
        menuSelectBtn.setImage(menuImage, for:.normal)
        menuSelectBtn.addTarget(self, action: #selector(menuSelectClick), for:.touchUpInside)
        menuSelectBtn.frame = CGRectMake(0, 0, 30, 30)
        let menuBarBtn = UIBarButtonItem(customView: menuSelectBtn)
        
        refreshConnectBtn = UIButton(type: .custom) as! UIButton
        refreshConnectBtn.setImage(refreshImage, for: .normal)
        refreshConnectBtn.addTarget(self, action: #selector(refreshClick), for: .touchUpInside)
        refreshConnectBtn.frame = CGRectMake(0, 0, 30, 30)
        let refreshConnectBarBtn = UIBarButtonItem(customView: refreshConnectBtn)
        
        self.navigationItem.setRightBarButtonItems([refreshConnectBarBtn, menuBarBtn], animated: false)
        
        navigationController?.navigationBar.barTintColor = UIColor.colorPrimary
    }
    let prevButton = UIButton()
    let nextButton = UIButton()
    let pageNumberLabel = UILabel()
    let maxPageNumberLabel = UILabel()
    let activityIndicatorView = UIActivityIndicatorView(style: .gray)
    var pageSize = 15
    var currentPage = 1
    var maxPage = 10
    private var tableViewContainer:UITableView!
    private var showDetailList = [BleDeviceData]()
    @objc func changePageTap(_ gesture: UITapGestureRecognizer) {
        // 处理点击事件的代码
        print("View was tapped!")
        let editPageAlert = AEUIAlertView(style: .number, title: NSLocalizedString("jump_to", comment: "Jump To"), message: nil)
        editPageAlert.textField.placeholder = ""
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editPageAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let pageStr = String(editPageAlert.textField.text ?? "")
            if pageStr.count > 0{
                let page = Int(pageStr) ?? 0
                if(page <= 0 || page > self.maxPage){
                    Toast.hudBuilder.title(NSLocalizedString("page_out_of_range", comment: "Page number is out of range, please re-enter")).show()
                    return
                }
                self.currentPage = page
                self.pageNumberLabel.text = String(self.currentPage)
                self.updatePageData(pageNumb: page)
                editPageAlert.dismiss()
            }else{
                Toast.hudBuilder.title(NSLocalizedString("fix_input", comment: "Please fix your input!")).show()
            }
        }
        editPageAlert.addAction(action: action_one)
        editPageAlert.addAction(action: action_two)
        editPageAlert.show()
    }
    @objc func nextPageBtnClick(){
        print("nextPageBtnClick!")
        doNextPage()
    }
    @objc func prePageBtnClick(){
        print("prePageBtnClick!")
        doPrevPage()
    }
    
    func doNextPage(){
        if currentPage >= maxPage{
            return
        }
        currentPage += 1
        pageNumberLabel.text = String(currentPage)
        updatePageData(pageNumb: currentPage)
    }
    func doPrevPage(){
        if currentPage <= 1{
            return
        }
        currentPage -= 1
        pageNumberLabel.text = String(currentPage)
        updatePageData(pageNumb: currentPage)
    }
    
    func initUI(){
        self.view.backgroundColor = UIColor.white
        //     prevButton.setTitle("△", for: .normal)
        prevButton.backgroundColor = UIColor.almostWhite
        let prevImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: "ic_page_prev.png")
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
        prevButton.setImage(prevImage, for: .normal)
        prevButton.frame = CGRect(x: 5, y: 75, width: 30, height: 30)
        prevButton.addTarget(self, action: #selector(prePageBtnClick), for: .touchUpInside)
        self.view.addSubview(prevButton)
        let pageInfoView = UIView()
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(changePageTap(_:)))
        // 将手势识别器添加到 UIView 上
        pageInfoView.addGestureRecognizer(tapGesture)
        pageInfoView.frame = CGRect(x: 5, y: 110, width: 30, height: 90)
        self.view.addSubview(pageInfoView)
        pageInfoView.backgroundColor = UIColor.white
        pageInfoView.layer.cornerRadius = 10.0
        pageInfoView.layer.masksToBounds = true
        pageInfoView.layer.shouldRasterize = true
        pageInfoView.layer.rasterizationScale = UIScreen.main.scale
        pageInfoView.layer.borderWidth = 1
        pageInfoView.layer.borderColor = UIColor.black.cgColor
        
        pageNumberLabel.text = "1"
        pageNumberLabel.textAlignment = .center
        pageNumberLabel.frame = CGRect(x: 0, y: 0, width: 30, height: 30)
        let divPageLabel = UILabel()
        divPageLabel.frame = CGRect(x: 0, y: 30, width: 30, height: 30)
        divPageLabel.text = "/"
        divPageLabel.textAlignment = .center
        maxPageNumberLabel.frame = CGRect(x: 0, y: 60, width: 30, height: 30)
        maxPageNumberLabel.text = "20"
        maxPageNumberLabel.textAlignment = .center
        
        pageInfoView.addSubview(pageNumberLabel)
        pageInfoView.addSubview(divPageLabel)
        pageInfoView.addSubview(maxPageNumberLabel)
        nextButton.backgroundColor = UIColor.almostWhite
        let nextImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: "ic_page_next.png")
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
        nextButton.setImage(nextImage, for: .normal)
        nextButton.frame = CGRect(x: 5, y:205, width: 30, height: 30)
        nextButton.addTarget(self, action: #selector(nextPageBtnClick), for: .touchUpInside)
        self.view.addSubview(nextButton)
        activityIndicatorView.frame = CGRect(x: 5, y: 235, width: 30, height: 30)
        activityIndicatorView.startAnimating()
        self.view.addSubview(activityIndicatorView)
        
        
        makeTable()
        
        
        let popData = [(icon:"",title:NSLocalizedString("choose_date", comment: "Choose date")),
                       (icon:"",title:NSLocalizedString("send_excel", comment: "Send excel")) ]
        
        //设置Parameter（可不写）
        let parameters:[SwiftPopMenuConfigure] = [
            .PopMenuTextColor(UIColor.black),
            .popMenuItemHeight(44),
            .PopMenuTextFont(UIFont.systemFont(ofSize: CGFloat(Utils.fontSize)))
        ]
        
        //init  (注意：arrow点是基于屏幕的位置)
        popMenu = SwiftPopMenu(menuWidth: 150, arrow: CGPoint(x: KSize.width - 10, y: 70), datas: popData,configures: parameters)
        
        //click
        popMenu.didSelectMenuBlock = { [weak self](index:Int)->Void in
            print ("block sßelect \(index)")
            self?.popMenu.dismiss()
            if index == 0{
                self?.chooseDate()
            }else if index == 1{
                self?.sendExcel()
            }
        }
    }
    func makeTable()
    {
        self.pageSize = Int((self.view.bounds.height - 100) / 40)
        let tableHight = 40 * self.pageSize + 40
        let contentWidth = HistorySubLockCell.contentViewWidth
        tableViewContainer = UITableView()
        tableViewContainer.frame = CGRect(x: 0, y: 0, width:contentWidth , height: Int(UIScreen.main.bounds.height))
        
        tableViewContainer.showsHorizontalScrollIndicator = true
        //        self.view.addSubview(tableViewContainer)
        let scrollView = UIScrollView(frame: CGRect(x: 35, y: 0, width: Int(UIScreen.main.bounds.width) - 35, height: Int(UIScreen.main.bounds.height)))
        scrollView.showsHorizontalScrollIndicator = true
        scrollView.contentSize = CGSize(width: contentWidth+5, height: Int(UIScreen.main.bounds.height))
        scrollView.addSubview(tableViewContainer)
        self.view.addSubview(scrollView)
        if #available(iOS 15.0, *) {
            tableViewContainer.sectionHeaderTopPadding = 0
        } else{
            print("is not ios 15")
        }
        
        tableViewContainer.backgroundColor = UIColor.white
        tableViewContainer.delegate = self
        tableViewContainer.dataSource = self
        let headView = HistorySubLockHeader()
        headView.frame = CGRect(x: 0, y: 0, width: KSize.width, height: 40)
        tableViewContainer.tableHeaderView = headView
        tableViewContainer.tableFooterView?.isHidden = true;
        tableViewContainer.tableFooterView = UIView(frame: CGRect(x: 0, y: 0, width: KSize.width, height: 0.01))
        tableViewContainer.estimatedSectionHeaderHeight = 0;
        tableViewContainer.estimatedSectionFooterHeight = 0;
        tableViewContainer.estimatedRowHeight = 0;
        tableViewContainer.autoresizingMask = UIView.AutoresizingMask.flexibleWidth
        tableViewContainer.register(HistorySubLockCell.self,forCellReuseIdentifier:HistorySubLockCell.identifier)
        let swipeUp = UISwipeGestureRecognizer(target: self, action: #selector(handleSwipeGesture(_:)))
          swipeUp.direction = .up
          tableViewContainer.addGestureRecognizer(swipeUp)

          let swipeDown = UISwipeGestureRecognizer(target: self, action: #selector(handleSwipeGesture(_:)))
          swipeDown.direction = .down
          tableViewContainer.addGestureRecognizer(swipeDown)
        reloadReportData()
    }
    @objc func handleSwipeGesture(_ gesture: UISwipeGestureRecognizer) {
        switch gesture.direction {
        case .up:
            // 处理上划手势
            print("touch up")
            doNextPage()
            break
        case .down:
            // 处理下划手势
            print("touch down")
            doPrevPage()
            break
        default:
            break
        }
    }
    //MARK:table代理
    
    //段数
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    //行数
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.showDetailList.count
    }
    
    
    func tableView(_ tableView: UITableView, estimatedHeightForRowAt indexPath: IndexPath) -> CGFloat {
        return 40
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 40
    }
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 0.01
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 0.01
    }
    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return false
    }
    
    func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
        
        return indexPath
    }
    func parseSubLockAlarm(alarmCode: Int) -> String {
        var code = alarmCode
        if code < 0 {
            code += 256
        }

        switch code {
        case 0x00: return NSLocalizedString("lock_status_00", comment: "")
        case 0x01: return NSLocalizedString("lock_status_01", comment: "")
        case 0x03: return NSLocalizedString("lock_status_03", comment: "")
        case 0x04: return NSLocalizedString("lock_status_04", comment: "")
        case 0x09: return NSLocalizedString("lock_status_09", comment: "")
        case 0x0a: return NSLocalizedString("lock_status_0a", comment: "")
        case 0x05: return NSLocalizedString("lock_status_05", comment: "")
        case 0x06: return NSLocalizedString("lock_status_06", comment: "")
        case 0x07: return NSLocalizedString("lock_status_07", comment: "")
        case 0x08: return NSLocalizedString("lock_status_08", comment: "")
        case 0x11: return NSLocalizedString("lock_status_11", comment: "")
        case 0x12: return NSLocalizedString("lock_status_12", comment: "")
        case 0x13: return NSLocalizedString("lock_status_13", comment: "")
        case 0x14: return NSLocalizedString("lock_status_14", comment: "")
        case 0x15: return NSLocalizedString("lock_status_15", comment: "")
        case 0x16: return NSLocalizedString("lock_status_16", comment: "")
        case 0x17: return NSLocalizedString("lock_status_17", comment: "")

        case 0x21: return NSLocalizedString("lock_status_21", comment: "")
        case 0x22: return NSLocalizedString("lock_status_22", comment: "")
        case 0x23: return NSLocalizedString("lock_status_23", comment: "")
        case 0x24: return NSLocalizedString("lock_status_24", comment: "")
        case 0x25: return NSLocalizedString("lock_status_25", comment: "")
        case 0x26: return NSLocalizedString("lock_status_26", comment: "")
        case 0x27: return NSLocalizedString("lock_status_27", comment: "")

        case 0x31: return NSLocalizedString("lock_status_31", comment: "")
        case 0x32: return NSLocalizedString("lock_status_32", comment: "")
        case 0x33: return NSLocalizedString("lock_status_33", comment: "")
        case 0x34: return NSLocalizedString("lock_status_34", comment: "")
        case 0x35: return NSLocalizedString("lock_status_35", comment: "")
        case 0x36: return NSLocalizedString("lock_status_36", comment: "")
        case 0x37: return NSLocalizedString("lock_status_37", comment: "")

        case 0x41: return NSLocalizedString("lock_status_41", comment: "")
        case 0x42: return NSLocalizedString("lock_status_42", comment: "")
        case 0x43: return NSLocalizedString("lock_status_43", comment: "")
        case 0x44: return NSLocalizedString("lock_status_44", comment: "")
        case 0x45: return NSLocalizedString("lock_status_45", comment: "")
        case 0x46: return NSLocalizedString("lock_status_46", comment: "")
        case 0x47: return NSLocalizedString("lock_status_47", comment: "")

        case 0x51: return NSLocalizedString("lock_status_51", comment: "")
        case 0x52: return NSLocalizedString("lock_status_52", comment: "")
        case 0x53: return NSLocalizedString("lock_status_53", comment: "")
        case 0x54: return NSLocalizedString("lock_status_54", comment: "")
        case 0x55: return NSLocalizedString("lock_status_55", comment: "")
        case 0x56: return NSLocalizedString("lock_status_56", comment: "")
        case 0x57: return NSLocalizedString("lock_status_57", comment: "")

        case 0xff: return NSLocalizedString("pwd_error", comment: "")

        case 0xe0: return NSLocalizedString("charge_end", comment: "")
        case 0xe1: return NSLocalizedString("charge_over_voltage_recover", comment: "")
        case 0xe2: return NSLocalizedString("battery_lower_recover", comment: "")
        case 0xe3: return NSLocalizedString("high_temp_recover", comment: "")
        case 0xe4: return NSLocalizedString("low_temp_recover", comment: "")
        case 0xe5: return NSLocalizedString("lock_open_recover", comment: "")
        case 0xe6: return NSLocalizedString("back_cover_open_recover", comment: "")
        case 0xe7: return NSLocalizedString("gps_location_lost", comment: "")
        case 0xe8: return NSLocalizedString("gps_interference_recover", comment: "")

        case 0xf0: return NSLocalizedString("charge_start", comment: "")
        case 0xf1: return NSLocalizedString("charge_over_voltage", comment: "")
        case 0xf2: return NSLocalizedString("battery_lower", comment: "")
        case 0xf3: return NSLocalizedString("high_temp", comment: "")
        case 0xf4: return NSLocalizedString("low_temp", comment: "")
        case 0xf5: return NSLocalizedString("lock_open", comment: "")
        case 0xf6: return NSLocalizedString("back_cover_open", comment: "")
        case 0xf7: return NSLocalizedString("gps_location", comment: "")
        case 0xf8: return NSLocalizedString("gps_interference", comment: "")

        default:
            return ""
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let bleHisItem = self.showDetailList[indexPath.row]
        
        let cell = (tableView.dequeueReusableCell(withIdentifier: HistorySubLockCell.identifier, for: indexPath)) as! HistorySubLockCell
        cell.dateLabel.text = bleHisItem.date
        cell.alarmLabel.text = parseSubLockAlarm(alarmCode: bleHisItem.subLockAlarm)
        cell.lockStatusLabel.text = parseLockType(lockType: (UInt8)(bleHisItem.lockType))
        cell.deviceStatusLabel.text = bleHisItem.getAlarm()
        cell.solarVoltageLabel.text = String(format: "%.2f V", bleHisItem.solarVoltage!)
        cell.batteryVoltageLabel.text = String(format: "%.2f V", bleHisItem.voltage!)
        cell.batteryLabel.text = "\(bleHisItem.batteryPercent) %"
        cell.tempLabel.text = String.init(format: "%.2f %@", Utils.getCurTemp(sourceTemp: bleHisItem.temp ?? 0),Utils.getCurTempUnit())
        if bleHisItem.nfcId != nil && bleHisItem.nfcId.length == 0{
            cell.unclockIdLabel.text = bleHisItem.nfcId as String?
        }else{
            cell.unclockIdLabel.text = bleHisItem.macId as String?
        }
        return cell
        
    }
    var popMenu:SwiftPopMenu!
    @objc private func menuSelectClick(){
        print("bmenuSelectClick")
        popMenu.show()
        
    }
    @objc private func lightAlarmCancelClick(){
        if !self.isLightWarning{
            return
        }
        let lightAlarmWaringView = AEAlertView(style: .defaulted)
        lightAlarmWaringView.message = NSLocalizedString("cancel_light_warning", comment: "TAttempt to cancel the light alarm?")
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            
            lightAlarmWaringView.dismiss()
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            lightAlarmWaringView.dismiss()
            self.sendCancelLightWarning()
        }
        lightAlarmWaringView.addAction(action: upgradeCancel)
        lightAlarmWaringView.addAction(action: upgradeConfirm)
        lightAlarmWaringView.show()
    }
    private func chooseDate(){
        let editView = HistorySelectController()
        self.leaveViewNeedDisconnect = false
        editView.setDateDelegate = self
        self.navigationController?.pushViewController(editView, animated: false)
    }
    private var isFirstGetData = true
    func setSelectDate(startDate: Date, endDate: Date) {
        self.leaveViewNeedDisconnect = true
        self.readStartDate = Int64(startDate.timeIntervalSince1970)
        self.readEndDate = Int64(endDate.timeIntervalSince1970)
        self.onView = true
        self.needConnect = true
        initSendMsgThread()
        self.afterSelectDateDoInView()
    }
    
    func afterSelectDateDoInView(){
        if(isFirstGetData){
            isFirstGetData = false
            connectDevice()
        }else{ 
            allDatas.removeAll()
            tempDatas.removeAll()
            currentPage = 1
            maxPage = 1
            maxPageNumberLabel.text = String(maxPage)
            pageNumberLabel.text = "1"
            reloadReportData();
//            if(isDeviceReady){
//                self.showWaitingCancelWin(title: NSLocalizedString("readBG9xStatus", comment: "Waiting device Ready"))
//            }else{
//                self.showWaitingCancelWin(title: NSLocalizedString("reading_data", comment: "Reading data"))
//                
//            }
//            print("afterSelectDateDoInView")
//            sendGetData(true,false);
            changeBlePwd(curPwd, curPwd)
        }
    }
    private func sendExcel(){
        if allDatas.count <= 0 {
            Toast.hudBuilder.title(NSLocalizedString("please_repair_data_first", comment: "Please prepare the data first")).show()
            return
        }
        print ("send excel")
        self.checkCanSendEmail()
        if !canSendEmail{
            return
        }
        
        let path = NSHomeDirectory() + "/Documents/sample1.xlsx"
        let book = workbook_new(path)
        let sheet = workbook_add_worksheet(book, "sheet1")
        worksheet_write_string(sheet, lxw_row_t(0), 0, NSLocalizedString("table_head_date", comment: "table_head_date") , nil)
        worksheet_write_string(sheet, lxw_row_t(0), 1, NSLocalizedString("table_head_alarm", comment: "table_head_alarm") , nil)
        worksheet_write_string(sheet, lxw_row_t(0), 2, NSLocalizedString("table_head_lock_status", comment: "table_head_lock_status") , nil)
        worksheet_write_string(sheet, lxw_row_t(0), 3, NSLocalizedString("table_head_device_status", comment: "table_head_device_status") , nil)
        worksheet_write_string(sheet, lxw_row_t(0), 4, NSLocalizedString("solar_voltage", comment: "solar_voltage") , nil)
        worksheet_write_string(sheet, lxw_row_t(0), 5, NSLocalizedString("battery_voltage", comment: "battery_voltage") , nil)
        worksheet_write_string(sheet, lxw_row_t(0), 6, NSLocalizedString("table_head_battery", comment: "table_head_battery") , nil)
        worksheet_write_string(sheet, lxw_row_t(0), 7, NSLocalizedString("temp", comment: "temp") , nil)
        worksheet_write_string(sheet, lxw_row_t(0), 8, NSLocalizedString("unlock_id", comment: "Network unlock_id") , nil)
        var rowIndex = 0
        for rowIndex in 1...allDatas.count - 1{
            var bleHisItem = allDatas[rowIndex]
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, bleHisItem.date , nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, parseSubLockAlarm(alarmCode: bleHisItem.subLockAlarm) , nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, parseLockType(lockType: (UInt8)(bleHisItem.lockType)) , nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 3, bleHisItem.getAlarm() , nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 4, String(format: "%.2f V", bleHisItem.solarVoltage!), nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 5, String(format: "%.2f V", bleHisItem.voltage!) , nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 6, "\(bleHisItem.batteryPercent) %" , nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 7, String.init(format: "%.2f %@", Utils.getCurTemp(sourceTemp: bleHisItem.temp ?? 0),Utils.getCurTempUnit()) , nil)
            if bleHisItem.nfcId != nil && bleHisItem.nfcId.length == 0{
                worksheet_write_string(sheet, lxw_row_t(rowIndex), 8, bleHisItem.nfcId as String? , nil)
            }else{
                worksheet_write_string(sheet, lxw_row_t(rowIndex), 8, bleHisItem.macId as String?, nil)
            }
         
        }
        
        
        workbook_close(book);
        
        self.sendMail(path: path, fileType: "xlsx")
    }
    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
            if result == MFMailComposeResult.sent {
                //发送成功
                Toast.hudBuilder.title(NSLocalizedString("send_success", comment: "Send success!")).show()
            }
            else if result == MFMailComposeResult.cancelled {
                //取消发送
            }
            else if result == MFMailComposeResult.failed {
                //发送失败
                Toast.hudBuilder.title(NSLocalizedString("send_fail", comment: "Send fail!")).show()
            }
            else {
                //已保存
            }
            dismiss(animated: true, completion: nil)
        }
    func sendMail(path:String,fileType:String){
        let mail = MFMailComposeViewController()
        mail.navigationBar.tintColor = UIColor.blue //导航颜色
        //        mail.setToRecipients(["1024919409@qq.com"]) //设置收件地址
        mail.mailComposeDelegate = self //代理
        let filename = self.getSaveName(fileType: fileType)
        mail.setSubject(filename)
        mail.setMessageBody(filename, isHTML: false) //邮件主体内容
        let url = URL(fileURLWithPath: path)
        do {
            
            let data = try Data(contentsOf: url)
            mail.addAttachmentData(data as Data, mimeType: "", fileName: filename)
            
            self.present(mail, animated: true, completion: nil)
        } catch let error as Error! {
            print("读取本地数据出现错误!",error)
        }
    }
    func getSaveName(fileType:String) ->String{
        let timeExt = String(format:"%@-%@",tableDateFormat.string(from: Date(timeIntervalSince1970: TimeInterval(readStartDate))) as! CVarArg,tableDateFormat.string(from: Date(timeIntervalSince1970: TimeInterval(readEndDate))) as! CVarArg)
        if fileType == "pdf"{
            return   "TFT_ELOCK_DATA-" + timeExt + ".pdf";
        } else if fileType == "xlsx"{
            return   "TFT_ELOCK_DATA-" + timeExt + ".xlsx";
        }else if fileType == "csv"{
            return  "TFT_ELOCK_DATA-" + timeExt + ".csv";
        }
        return "";
    }
    
    func updateConnectStatusBtn(isConnect:Bool){
        if isConnect{
            let image = renderer.image { (context) in
                // 绘制图像
                let originalImage = UIImage(named: "ic_refresh.png")
                originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
            }
            refreshConnectBtn.setImage(image, for: .normal)
        }else{
            let image = renderer.image { (context) in
                // 绘制图像
                let originalImage = UIImage(named: "ic_disconnect.png")
                originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
            }
            refreshConnectBtn.setImage(image, for: .normal)
        }
    }
    @objc private func refreshClick() {
        print ("refresh click")
        
        self.initStart = false
        self.showWaitingCancelWin(title: NSLocalizedString("connecting", comment: "Connecting"))
        TftBleConnectManager.getInstance().disconnect()
        TftBleConnectManager.getInstance().connect(connectPeripheral: self.cbPeripheral,isSubLock: BleDeviceData.isSubLockDevice(model: self.model))
        
        self.initStart = false
     
        self.connected = false
        self.isCanSendMsg = false
        self.isDeviceReady = false
        self.updateConnectStatusBtn(isConnect:false)
        
        
    }
    func asciiStringToBytes(str: String) -> [UInt8]{
        var bytes: [UInt8] = []
        for character in str.unicodeScalars {
            print(character)
            print(character.value)
        }
        return bytes
    }
 
    func showWaitingDlg(title:String){
        if self.waitingView != nil && !self.waitingView.isDismiss{
            self.waitingView.title = title
            self.waitingView.show()
            DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(70)) {
                if !self.waitingView.isDismiss{
                    Toast.hudBuilder.title(NSLocalizedString("timeout_and_warn_retry", comment: "Timeout, please try again!")).show()
                }
                self.waitingView.dismiss()
            }
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
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(70)) {
            if !self.waitingView.isDismiss{
                Toast.hudBuilder.title(NSLocalizedString("timeout_and_warn_retry", comment: "Timeout, please try again!")).show()
            }
            self.waitingView.dismiss()
        }
    }
    private var sendMsgThreadworking = false
    @objc func sendMsgThreadFunc(){
        print("send msg thread start")
        while onView{
            sendMsgThreadworking = true
            if isReadData{
                let now = Date()
                if now.timeIntervalSince(lastReceiveDataDate) > 60000 {
                    print("timeout reset to read")
                    DispatchQueue.main.async {
                        Toast.hudBuilder.title(NSLocalizedString("read_data_timeout", comment: "Reading data timeout, please try againDisconnect from the device.")).show()
                        if self.waitingCancelDlg != nil {
                            self.waitingCancelDlg.dismiss()
                        }
                    }
                }
            }
            if sendMsgQueue.count != 0{
                var needSendBytes = sendMsgQueue.pop()
                if needSendBytes != nil {
                    print("test write")
                    writeContent(content: needSendBytes!)
                }
                Thread.sleep(forTimeInterval: 1)
            }else{
                Thread.sleep(forTimeInterval: 3)
               
            }
            
        }
        sendMsgThreadworking = false
        print("send msg thread end")
    }
    
    private func sendCancelLightWarning() {
        let head: [UInt8] = [0x60, 0x07, 0xE1]
        var result = [UInt8]()
        result.append(contentsOf: head)
        result.append(0x07)
        if(curPwd != nil){
            result.append(contentsOf: [UInt8](curPwd.utf8))
        }
        result.append(0x01)
        writeContent(content: result)
    }
    
    private func readLightStatus() {
        let head: [UInt8] = [0x60, 0x07, 0xE1]
        var result = [UInt8]()
        result.append(contentsOf: head)
        result.append(0x06)
        if(curPwd != nil){
            result.append(contentsOf: [UInt8](curPwd.utf8))
        }
        writeContent(content: result)
    }
    
     
    func writeContent(content:[UInt8]){
        TftBleConnectManager.getInstance().writeContent(content: content)
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
    
    func parseLockType(lockType: UInt8) -> String {
        switch lockType {
        case 0x00:
            return NSLocalizedString("lock_status_00", comment: "lock_status_00")
        case 0x01:
            return NSLocalizedString("lock_status_01", comment: "lock_status_01")
        case 0x03:
            return NSLocalizedString("lock_status_03", comment: "lock_status_03")
        case 0x04:
            return NSLocalizedString("lock_status_04", comment: "lock_status_04")
        case 0x09:
            return NSLocalizedString("lock_status_09", comment: "lock_status_09")
        case 0x05:
            return NSLocalizedString("lock_status_05", comment: "lock_status_05")
        case 0x06:
            return NSLocalizedString("lock_status_06", comment: "lock_status_06")
        case 0x07:
            return NSLocalizedString("lock_status_07", comment: "lock_status_07")
        case 0x08:
            return NSLocalizedString("lock_status_08", comment: "lock_status_08")
        case 0x11:
            return NSLocalizedString("lock_status_11", comment: "lock_status_11")
        case 0x12:
            return NSLocalizedString("lock_status_12", comment: "lock_status_12")
        case 0x13:
            return NSLocalizedString("lock_status_13", comment: "lock_status_13")
        case 0x14:
            return NSLocalizedString("lock_status_14", comment: "lock_status_14")
        case 0x15:
            return NSLocalizedString("lock_status_15", comment: "lock_status_15")
        case 0x16:
            return NSLocalizedString("lock_status_16", comment: "lock_status_16")
        case 0x17:
            return NSLocalizedString("lock_status_17", comment: "lock_status_17")
        case 0x21:
            return NSLocalizedString("lock_status_21", comment: "lock_status_21")
        case 0x22:
            return NSLocalizedString("lock_status_22", comment: "lock_status_22")
        case 0x23:
            return NSLocalizedString("lock_status_23", comment: "lock_status_23")
        case 0x24:
            return NSLocalizedString("lock_status_24", comment: "lock_status_24")
        case 0x25:
            return NSLocalizedString("lock_status_25", comment: "lock_status_25")
        case 0x26:
            return NSLocalizedString("lock_status_26", comment: "lock_status_26")
        case 0x27:
            return NSLocalizedString("lock_status_27", comment: "lock_status_27")
        case 0x31:
            return NSLocalizedString("lock_status_31", comment: "lock_status_31")
        case 0x32:
            return NSLocalizedString("lock_status_32", comment: "lock_status_32")
        case 0x33:
            return NSLocalizedString("lock_status_33", comment: "lock_status_33")
        case 0x34:
            return NSLocalizedString("lock_status_34", comment: "lock_status_34")
        case 0x35:
            return NSLocalizedString("lock_status_35", comment: "lock_status_35")
        case 0x36:
            return NSLocalizedString("lock_status_36", comment: "lock_status_36")
        case 0x37:
            return NSLocalizedString("lock_status_37", comment: "lock_status_37")
        case 0x41:
            return NSLocalizedString("lock_status_41", comment: "lock_status_41")
        case 0x42:
            return NSLocalizedString("lock_status_42", comment: "lock_status_42")
        case 0x43:
            return NSLocalizedString("lock_status_43", comment: "lock_status_43")
        case 0x44:
            return NSLocalizedString("lock_status_44", comment: "lock_status_44")
        case 0x45:
            return NSLocalizedString("lock_status_45", comment: "lock_status_45")
        case 0x46:
            return NSLocalizedString("lock_status_46", comment: "lock_status_46")
        case 0x47:
            return NSLocalizedString("lock_status_47", comment: "lock_status_47")
        case 0x51:
            return NSLocalizedString("lock_status_51", comment: "lock_status_51")
        case 0x52:
            return NSLocalizedString("lock_status_52", comment: "lock_status_52")
        case 0x53:
            return NSLocalizedString("lock_status_53", comment: "lock_status_53")
        case 0x54:
            return NSLocalizedString("lock_status_54", comment: "lock_status_54")
        case 0x55:
            return NSLocalizedString("lock_status_55", comment: "lock_status_55")
        case 0x56:
            return NSLocalizedString("lock_status_56", comment: "lock_status_56")
        case 0x57:
            return NSLocalizedString("lock_status_57", comment: "lock_status_57")
        case 0xff:
            return NSLocalizedString("pwd_error", comment: "pwd_error")
        default:
            return ""
        }
    }
    
    func getAlarmDesc(_ alarmCode: Int?) -> String {
        guard let alarmCode = alarmCode else {
            return ""
        }
        let alarmCodeHex = Int(String(format: "%X", alarmCode)) ?? 0
        switch alarmCodeHex {
        case 1:
            return NSLocalizedString("alarm_1", comment: "")
        case 2:
            return NSLocalizedString("alarm_2", comment: "")
        case 3:
            return NSLocalizedString("alarm_3", comment: "")
        case 4:
            return NSLocalizedString("alarm_4", comment: "")
        case 5:
            return NSLocalizedString("alarm_5", comment: "")
        case 6:
            return NSLocalizedString("alarm_6", comment: "")
        case 7:
            return NSLocalizedString("alarm_7", comment: "")
        case 8:
            return NSLocalizedString("alarm_8", comment: "")
        case 9:
            return NSLocalizedString("alarm_9", comment: "")
        case 10:
            return NSLocalizedString("alarm_10", comment: "")
        case 11:
            return NSLocalizedString("alarm_11", comment: "")
        case 12:
            return NSLocalizedString("alarm_12", comment: "")
        case 13:
            return NSLocalizedString("alarm_13", comment: "")
        case 14:
            return NSLocalizedString("alarm_14", comment: "")
        case 15:
            return NSLocalizedString("alarm_15", comment: "")
        case 16:
            return NSLocalizedString("alarm_16", comment: "")
        case 17:
            return NSLocalizedString("alarm_17", comment: "")
        case 18:
            return NSLocalizedString("alarm_18", comment: "")
        case 19:
            return NSLocalizedString("alarm_19", comment: "")
        case 20:
            return NSLocalizedString("alarm_20", comment: "")
        case 21:
            return NSLocalizedString("alarm_21", comment: "")
        case 22:
            return NSLocalizedString("alarm_22", comment: "")
        case 23:
            return NSLocalizedString("alarm_23", comment: "")
        case 24:
            return NSLocalizedString("alarm_24", comment: "")
        case 25:
            return NSLocalizedString("alarm_25", comment: "")
        case 26:
            return NSLocalizedString("alarm_26", comment: "")
        case 27:
            return NSLocalizedString("alarm_27", comment: "")
        case 28:
            return NSLocalizedString("alarm_28", comment: "")
        case 29:
            return NSLocalizedString("alarm_29", comment: "")
        case 30:
            return NSLocalizedString("alarm_30", comment: "")
        case 31:
            return NSLocalizedString("alarm_31", comment: "")
        case 32:
            return NSLocalizedString("alarm_32", comment: "")
        case 66:
            return NSLocalizedString("alarm_66", comment: "")
        case 67:
            return NSLocalizedString("alarm_67", comment: "")
        case 70:
            return NSLocalizedString("alarm_70", comment: "")
        case 71:
            return NSLocalizedString("alarm_71", comment: "")
        case 72:
            return NSLocalizedString("alarm_72", comment: "")
        case 74:
            return NSLocalizedString("alarm_74", comment: "")
        case 77:
            return NSLocalizedString("alarm_77", comment: "")
        default:
            return ""
        }
    }
    private var canSendEmail = false
    func checkCanSendEmail(){
        if !MFMailComposeViewController.canSendMail() {
            //不支持发送邮件
            Toast.hudBuilder.title(NSLocalizedString("can_not_send_email_need_set_in_system", comment: "Unable to send report email, please configure mailbox in IOS system")).show()
            self.canSendEmail = false
        }else{
            self.canSendEmail = true
        }
    }
}

