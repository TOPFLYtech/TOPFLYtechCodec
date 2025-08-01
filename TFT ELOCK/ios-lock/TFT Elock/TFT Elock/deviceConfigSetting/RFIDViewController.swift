//
//  RFIDViewController.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/2/24.
//  Copyright © 2025 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import SwiftPopMenu
import swiftScan
import CLXToast
class RFIDViewController: UIViewController, UITableViewDataSource, UITableViewDelegate , LBXScanViewControllerDelegate,BleStatusCallback {
    func onUpgradeNotifyValue(_ value: [UInt8]) {
         
    }
    
    func onNotifyValue(_ value: [UInt8]) {
        self.parseResp(respContent: value)
    }
    
    func onBleStatusCallback(_ connectStatus: Int) {
        switch connectStatus {
            case TftBleConnectManager.BLE_STATUS_OF_CLOSE:
                print("BLE Status: Closed")
            case TftBleConnectManager.BLE_STATUS_OF_DISCONNECT:
                print("BLE Status: Disconnected")
                Toast.hudBuilder.title(NSLocalizedString("disconnect_from_device", comment: "Connect is close")).show()
            case TftBleConnectManager.BLE_STATUS_OF_CONNECTING:
                print("BLE Status: Connecting")
            case TftBleConnectManager.BLE_STATUS_OF_CONNECT_SUCC:
                print("BLE Status: Connected Successfully")
            case TftBleConnectManager.BLE_STATUS_OF_SCANNING:
                print("BLE Status: Scanning")
            default:
                print("Unknown BLE Status")
            }
    }
    
    func scanFinished(scanResult: swiftScan.LBXScanResult, error: String?) {
        self.leaveViewNeedDisconnect = true
        print(scanResult)
        if curDealType == SCAN_DEAL_TYPE_OF_ADD{
            showAddRfidPopup(checkRfid: scanResult.strScanned as! String)
        }else if curDealType == SCAN_DEAL_TYPE_OF_DELETE{
            deleteWaning(deleteRfid: scanResult.strScanned as! String)
        }
    }
  
    func parseResp(respContent:[UInt8]){
        var dataList = Utils.parseRespContent(content: respContent)
       for bleRespData in dataList {
           print("resp code:\(bleRespData.controlCode)")
           if bleRespData.type == BleRespData.READ_TYPE || bleRespData.type == BleRespData.WRITE_TYPE {
               self.parseReadResp(bleRespData: bleRespData)
           }else{
               if bleRespData.errorCode == BleRespData.ERROR_CODE_OF_PWD_ERROR {
                   Toast.hudBuilder.title(NSLocalizedString("ble_pwd_error", comment: "Ble password is error")).show()
               }else{
                   Toast.hudBuilder.title(NSLocalizedString("fail", comment: "Fail")).show()
               }
           }
       }
    }
    private var waitingView:AEUIAlertView!
    let func_id_of_read_rfid = 1016
    let func_id_of_add_rfid = 2014
    let func_id_of_delete_rfid = 2015
    var curDealRfid = ""
    var rfidSet: Set<String> = []
   func parseReadResp(bleRespData: BleRespData) {
        let code = bleRespData.controlCode
        if code == func_id_of_read_rfid {
            guard let respData = getMultiRespData(bleRespData,func_id_of_read_rfid), !respData.isEmpty else {
                return
            }
            
            var curReadRfidList: [String] = []
            print("rfid set :\(rfidSet.count);curReadRfidList \(curReadRfidList.count);respData:\(respData.count)")
            var allLen = Int(respData[0])
            if allLen < 0 {
                allLen += 256
            }
            
            var i = 1
            if i + allLen > respData.count {
                return
            }
            
            while i + 1 < respData.count {
                var len = Int(respData[i])
                if len < 0 {
                    len += 256
                }
                if i + 1 + len <= respData.count {
                    let id = Array(respData[i + 1..<i + 1 + len])
                    let idStr = Utils.bytes2HexString(bytes: id, pos: 0)
                    print("get rfid:\(idStr)")
                    curReadRfidList.append(idStr.uppercased())
                    i += 1 + len
                } else {
                    break
                }
            }
            print("rfid set :\(rfidSet.count);curReadRfidList \(curReadRfidList.count)")
            rfidSet.formUnion(curReadRfidList)
            print("rfid set :\(rfidSet.count);curReadRfidList \(curReadRfidList.count)")
            if curReadRfidList.count < 10 {
                rfids.append(contentsOf: rfidSet)
                print("rfids  :\(rfids.count)")
                self.waitingView.dismiss()
                tableView.reloadData()
            } else {
                readRfid(index: rfidSet.count + 1)
            }
        } else if code == func_id_of_add_rfid {
            rfids.append(curDealRfid)
            tableView.reloadData()
            self.waitingView.dismiss()
        } else if code == func_id_of_delete_rfid {
            if let index = rfids.firstIndex(of: curDealRfid.uppercased()) {
                rfids.remove(at: index)
            }
            if let index = rfids.firstIndex(of: curDealRfid.lowercased()) {
                rfids.remove(at: index)
            }
            self.waitingView.dismiss()
            tableView.reloadData()
        }
        print("code: \(code)")
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
    func readRfid(index:Int){
        if index == 1{
            rfidSet.removeAll()
        }
        var outputStream: [UInt8] = []
        outputStream.append(contentsOf: Utils.short2Bytes(number: index))
        outputStream.append(contentsOf: [(UInt8)(0x0a)])
        let content = Utils.getReadCmdContent(cmdCode: func_id_of_read_rfid, content: outputStream)
        TftBleConnectManager.getInstance().writeContent(content: content)
    }
    
    func addRfid(_ rfid: String) {
        let uppercasedRfid = rfid.uppercased()
        if rfids.contains(uppercasedRfid) {
            Toast.hudBuilder.title(NSLocalizedString("rfid_exists", comment: "RFID already exists")).show()
            return
        }
        
        showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        curDealRfid = uppercasedRfid
        
        var outputStream: [UInt8] = []
        let rfidByte = Utils.hexString2Bytes(hexStr: uppercasedRfid)
        outputStream.append(contentsOf: [UInt8(rfidByte.count)])
        outputStream.append(contentsOf: rfidByte)
        
        let cmd = Utils.getWriteCmdContent(cmdCode: func_id_of_add_rfid, content: outputStream, pwd: blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
    }

    func deleteRfid(_ rfid: String) {
        showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        curDealRfid = rfid
        
        var outputStream: [UInt8] = []
        let rfidByte = Utils.hexString2Bytes(hexStr: curDealRfid)
        outputStream.append(contentsOf: [UInt8(rfidByte.count)])
        outputStream.append(contentsOf: rfidByte)
        
        let cmd = Utils.getWriteCmdContent(cmdCode: func_id_of_delete_rfid, content: outputStream, pwd: blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
    }
    var tableView: UITableView!
    var rfids: [String] = [ ]
    var blePwd = ""
    var leaveViewNeedDisconnect = true
    override func viewDidDisappear(_ animated: Bool) {
        if self.leaveViewNeedDisconnect{
            self.connectStatusDelegate?.setConnectStatus()
            TftBleConnectManager.getInstance().removeCallback(activityName: "RfidViewController")
        }
    }
    var connectStatusDelegate:SetConnectStatusDelegate?
   
    override func viewDidLoad() {
        super.viewDidLoad()
        TftBleConnectManager.getInstance().setCallback(activityName: "RfidViewController", callback: self)
        self.initNavBar()
        setupTableView()
        self.showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        readRfid(index: 1);

    }
    
    var barLabel:UILabel!
    var leftBtn:UIButton!
    var favoriteBtn:UIButton!
    var rightMenuBtn:UIButton!
    let imageSize = CGSize(width: 24, height: 24)
    let renderer = UIGraphicsImageRenderer(size: CGSize(width: 24, height: 24))
    func initNavBar(){
        initPopMenu()
        barLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
       //                titleLabel.text = "Bluetooth sensor"
       barLabel.text =  "RFID"
       barLabel.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
       self.navigationItem.titleView = barLabel
       let rightMenuImage = renderer.image { (context) in
           // 绘制图像
           let originalImage = UIImage(named: "ic_list.png")
           originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
       }
          
       
       rightMenuBtn = UIButton(type: .custom) as! UIButton
       rightMenuBtn.setImage(rightMenuImage, for:.normal)
       rightMenuBtn.addTarget(self, action: #selector(showPopMenuClick), for:.touchUpInside)
       rightMenuBtn.frame = CGRectMake(0, 0, 30, 30)
       let rightMenuBtn = UIBarButtonItem(customView: rightMenuBtn)
        let leftImage = renderer.image { (context) in
            // 绘制图像
            let originalImage = UIImage(named: "ic_back.png")
            originalImage?.draw(in: CGRect(origin: .zero, size: imageSize))
        }
           
       leftBtn = UIButton(type: .custom) as! UIButton
       leftBtn.setImage(leftImage, for: .normal)
       leftBtn.addTarget(self, action: #selector(leftClick), for: .touchUpInside)
       leftBtn.frame = CGRectMake(0, 0, 30, 30)
       let leftBarBtn = UIBarButtonItem(customView: leftBtn)
        
       self.navigationItem.setLeftBarButtonItems([leftBarBtn ], animated: false)
       self.navigationItem.setRightBarButtonItems([  rightMenuBtn], animated: false)
       
       navigationController?.navigationBar.barTintColor = UIColor.colorPrimary
    
        
    }
    @objc private func leftClick() {
        navigationController?.popViewController(animated: true)
    }
    
    @objc private func showPopMenuClick() {
        print("showPopMenuClick")
        self.popMenu.show()
    }
 
    var popMenu:SwiftPopMenu!
    func initPopMenu(){
        
        //数据源（icon可不填）
        let popData = [(icon:"ic_add.png",title:NSLocalizedString("add", comment: "Add")),
            (icon:"scan.png",title:NSLocalizedString("add", comment: "Add")),
            (icon:"scan.png",title:NSLocalizedString("delete", comment: "delete")),
                       ]
        
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
                self?.showAddRfidPopup(checkRfid: "")
            }else if index == 1{
                self!.scanAndProcessRfid(dealType: self!.SCAN_DEAL_TYPE_OF_ADD)
            }else if index == 2{
                self!.scanAndProcessRfid(dealType:self!.SCAN_DEAL_TYPE_OF_DELETE)
            }
        }
    }
    private func setupTableView() {
        // 实例化 tableView
        tableView = UITableView(frame: view.bounds, style: .plain)
        
        // 添加 tableView 到视图层次结构中
        view.addSubview(tableView)
        
        // 设置自动布局约束
        tableView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            tableView.topAnchor.constraint(equalTo: topLayoutGuide.bottomAnchor),
            tableView.bottomAnchor.constraint(equalTo: bottomLayoutGuide.topAnchor),
            tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])
        
        // 设置 dataSource 和 delegate
        tableView.dataSource = self
        tableView.delegate = self
        
        // 注册单元格
        tableView.register(SimpleDeleteItem.self,forCellReuseIdentifier:SimpleDeleteItem.identifier)
    }
    
   
    
     
    private func showAddRfidPopup(checkRfid:String) {
        
        let addRfidAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("enter_rfid", comment: "Add RFID"), message: nil)
        addRfidAlert.textField.placeholder = NSLocalizedString("enter_rfid", comment: "Enter RFID (8 or 10 hex digits)")
        addRfidAlert.textField.text = checkRfid
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            addRfidAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let rfid = String(addRfidAlert.textField.text ?? "")
            if (rfid.count == 8 || rfid.count == 10) && self.isValidRfid(rfid){
//                self.rfids.append(rfid)
//                self.tableView.reloadData()
//                let dataArray = [UInt8](rfid.utf8)
                addRfidAlert.dismiss()
                self.addRfid(rfid)
            }else{
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "The length of the apn_pwd must between 0 and 49.")).show()
            }
        }
        addRfidAlert.addAction(action: action_one)
        addRfidAlert.addAction(action: action_two)
        addRfidAlert.show()
    }
    private var curDealType = 0
    let SCAN_DEAL_TYPE_OF_ADD:Int = 1
    let SCAN_DEAL_TYPE_OF_DELETE:Int = 2
    private func scanAndProcessRfid(dealType:Int) {
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
        curDealType = dealType
        let vc = LBXScanViewController()
        
        vc.scanStyle = style
        vc.scanResultDelegate = self
        self.leaveViewNeedDisconnect = false
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    private func addScannedRfid(_ rfid: String) {
        guard isValidRfid(rfid) && !rfids.contains(rfid) else { return }
        rfids.append(rfid)
    }
    
    private func deleteScannedRfid(_ rfid: String) {
        if let index = rfids.firstIndex(of: rfid) {
            rfids.remove(at: index)
        }
    }
    
    private func isValidRfid(_ rfid: String) -> Bool {
        let hexPattern = "^([0-9A-Fa-f]{8}|[0-9A-Fa-f]{10})$"
        let regex = try! NSRegularExpression(pattern: hexPattern)
        return regex.firstMatch(in: rfid, options: [], range: NSRange(location: 0, length: rfid.utf16.count)) != nil
    }
    
    
    
    // MARK: - Table View Data Source and Delegate
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return rfids.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = (tableView.dequeueReusableCell(withIdentifier: SimpleDeleteItem.identifier, for: indexPath)) as! SimpleDeleteItem
        let rfid = rfids[indexPath.row]
        cell.itemContentLabel.text = rfid
        cell.deleteBtn.tag = indexPath.row
        cell.deleteBtn.addTarget(self, action: #selector(deleteButtonTapped(_:)), for: .touchUpInside)
        return cell
    }
    
    @objc private func deleteButtonTapped(_ sender: UIButton) {
        let row = sender.tag
        let rfid = rfids[row]
        self.deleteWaning(deleteRfid: rfid)
       
    }
    
    private func deleteWaning(deleteRfid:String){
        
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_to_delete_rfid", comment: "Are you sure you want to delete this RFID?") + " " + deleteRfid + "?"
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            if let index = self.rfids.firstIndex(of: deleteRfid) {
//               self.rfids.remove(at: index)
//               self.tableView.reloadData() // 刷新表格视图
                self.deleteRfid(deleteRfid)
           }
           animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
    }
}

