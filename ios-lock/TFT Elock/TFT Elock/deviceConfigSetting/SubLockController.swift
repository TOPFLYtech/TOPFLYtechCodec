//
//  SubLockController.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/4/7.
//  Copyright © 2025 com.tftiot. All rights reserved.
//
 

import Foundation
import UIKit
import SwiftPopMenu
import swiftScan
import CLXToast
class SubLockController: UIViewController, UITableViewDataSource, UITableViewDelegate , LBXScanViewControllerDelegate,BleStatusCallback {
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
        print(scanResult)
        self.leaveViewNeedDisconnect = true
        if curDealType == SCAN_DEAL_TYPE_OF_ADD{
            showAddSubLockIdPopup(checkSubLockId: scanResult.strScanned as! String)
        }else if curDealType == SCAN_DEAL_TYPE_OF_DELETE{
            deleteWaning(deleteSubLockId: scanResult.strScanned as! String)
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
    let func_id_of_read_sub_lock_id = 1018
    let func_id_of_add_sub_lock_id = 2017
    let func_id_of_delete_sub_lock_id = 2018
    var curDealSubLockId = ""
    var subLockIdSet: Set<String> = []
   func parseReadResp(bleRespData: BleRespData) {
        let code = bleRespData.controlCode
        if code == func_id_of_read_sub_lock_id {
            guard let respData = getMultiRespData(bleRespData,func_id_of_read_sub_lock_id), !respData.isEmpty else {
                return
            }
            
            var curReadSubLockIdList: [String] = []
            var i = 0
            
            while i < respData.count {
                var len = 6
                if i + len <= respData.count {
                    let id = Array(respData[i ..< i + len])
                    let idStr = Utils.bytes2HexString(bytes: id, pos: 0)
                    curReadSubLockIdList.append(idStr.uppercased())
                    i += len
                } else {
                    break
                }
            }
            
            subLockIdSet.formUnion(curReadSubLockIdList)
            if curReadSubLockIdList.count < 10 {
                subLockIds.append(contentsOf: subLockIdSet)
                self.waitingView.dismiss()
                tableView.reloadData()
            } else {
                readSubLockId(index: subLockIdSet.count + 1)
            }
        } else if code == func_id_of_add_sub_lock_id {
            subLockIds.append(curDealSubLockId)
            tableView.reloadData()
            self.waitingView.dismiss()
        } else if code == func_id_of_delete_sub_lock_id {
            if let index = subLockIds.firstIndex(of: curDealSubLockId.uppercased()) {
                subLockIds.remove(at: index)
            }
            if let index = subLockIds.firstIndex(of: curDealSubLockId.lowercased()) {
                subLockIds.remove(at: index)
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
    func readSubLockId(index:Int){
        if index == 1{
            subLockIdSet.removeAll()
        }
        var outputStream: [UInt8] = []
        outputStream.append(contentsOf: [(UInt8)(index)])
        outputStream.append(contentsOf: [(UInt8)(0x0a)])
        let content = Utils.getReadCmdContent(cmdCode: func_id_of_read_sub_lock_id, content: outputStream)
        TftBleConnectManager.getInstance().writeContent(content: content)
    }
    
    func addSubLockId(_ subLockId: String) {
        let uppercasedSubLockId = subLockId.uppercased()
        if subLockIds.contains(uppercasedSubLockId) {
            Toast.hudBuilder.title(NSLocalizedString("sub_lock_exists", comment: "sub-lock id already exists")).show()
            return
        }
        
        showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        curDealSubLockId = uppercasedSubLockId
        
        var outputStream: [UInt8] = []
        let subLockIdByte = Utils.hexString2Bytes(hexStr: uppercasedSubLockId)
        outputStream.append(contentsOf: subLockIdByte)
        
        let cmd = Utils.getWriteCmdContent(cmdCode: func_id_of_add_sub_lock_id, content: outputStream, pwd: blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
    }

    func saveSubLockMap(){
        if self.imei != nil && self.imei.count > 0{
            BleDeviceData.saveSubLockBindMap(imei: self.imei, subLockList: self.subLockIds)
        }
    }
    
    func deleteSubLockId(_ subLockId: String) {
        showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        curDealSubLockId = subLockId
        
        var outputStream: [UInt8] = []
        let subLockIdByte = Utils.hexString2Bytes(hexStr: curDealSubLockId)
        outputStream.append(contentsOf: subLockIdByte)
        
        let cmd = Utils.getWriteCmdContent(cmdCode: func_id_of_delete_sub_lock_id, content: outputStream, pwd: blePwd)
        TftBleConnectManager.getInstance().writeArrayContent(writeContentList: cmd)
    }
    var tableView: UITableView!
    var subLockIds: [String] = [ ]
    var blePwd = ""
    var imei = ""
    var leaveViewNeedDisconnect = true
    override func viewDidDisappear(_ animated: Bool) {
        if self.leaveViewNeedDisconnect{
            self.connectStatusDelegate?.setConnectStatus()
            TftBleConnectManager.getInstance().removeCallback(activityName: "SubLockController")
            saveSubLockMap()
        }
  
    }
    var connectStatusDelegate:SetConnectStatusDelegate?
    override func viewDidLoad() {
        super.viewDidLoad()
        TftBleConnectManager.getInstance().setCallback(activityName: "SubLockController", callback: self)
        self.initNavBar()
        setupTableView()
        
        self.showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        readSubLockId(index: 1);

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
       barLabel.text =  NSLocalizedString("subLock", comment: "sub-lock")
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
                self?.showAddSubLockIdPopup(checkSubLockId: "")
            }else if index == 1{
                self!.scanAndProcessSubLockId(dealType: self!.SCAN_DEAL_TYPE_OF_ADD)
            }else if index == 2{
                self!.scanAndProcessSubLockId(dealType:self!.SCAN_DEAL_TYPE_OF_DELETE)
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
    
   
    
     
    private func showAddSubLockIdPopup(checkSubLockId:String) {
        
        let addSubLockIdAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("enter_sub_lock_id", comment: "Add sublock"), message: nil)
        addSubLockIdAlert.textField.placeholder = NSLocalizedString("enter_sub_lock_id", comment: "Enter sub lock (12 hex digits)")
        addSubLockIdAlert.textField.text = checkSubLockId
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            addSubLockIdAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let subLockId = String(addSubLockIdAlert.textField.text ?? "")
            if subLockId.count == 12 && self.isValidSubLockId(subLockId){
                addSubLockIdAlert.dismiss()
                self.addSubLockId(subLockId)
            }else{
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "The length of the sub lock id must between 12.")).show()
            }
        }
        addSubLockIdAlert.addAction(action: action_one)
        addSubLockIdAlert.addAction(action: action_two)
        addSubLockIdAlert.show()
    }
    private var curDealType = 0
    let SCAN_DEAL_TYPE_OF_ADD:Int = 1
    let SCAN_DEAL_TYPE_OF_DELETE:Int = 2
    private func scanAndProcessSubLockId(dealType:Int) {
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
    
    private func addScannedSubLockId(_ subLockId: String) {
        guard isValidSubLockId(subLockId) && !subLockIds.contains(subLockId) else { return }
        subLockIds.append(subLockId)
    }
    
    private func deleteScannedSubLockId(_ subLockId: String) {
        if let index = subLockIds.firstIndex(of: subLockId) {
            subLockIds.remove(at: index)
        }
    }
    
    private func isValidSubLockId(_ subLockId: String) -> Bool {
        let hexPattern = "^([0-9A-Fa-f]{12})$"
        let regex = try! NSRegularExpression(pattern: hexPattern)
        return regex.firstMatch(in: subLockId, options: [], range: NSRange(location: 0, length: subLockId.utf16.count)) != nil
    }
     
    
    // MARK: - Table View Data Source and Delegate
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return subLockIds.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = (tableView.dequeueReusableCell(withIdentifier: SimpleDeleteItem.identifier, for: indexPath)) as! SimpleDeleteItem
        let subLockId = subLockIds[indexPath.row]
        cell.itemContentLabel.text = subLockId
        cell.deleteBtn.tag = indexPath.row
        cell.deleteBtn.addTarget(self, action: #selector(deleteButtonTapped(_:)), for: .touchUpInside)
        return cell
    }
    
    @objc private func deleteButtonTapped(_ sender: UIButton) {
        let row = sender.tag
        let subLockId = subLockIds[row]
        self.deleteWaning(deleteSubLockId: subLockId)
       
    }
    
    private func deleteWaning(deleteSubLockId:String){
        
        let animV = AEAlertView(style: .defaulted)
        animV.message = NSLocalizedString("confirm_to_delete_sub_lock_id", comment: "Are you sure you want to delete this Sub-lock id?") + " " + deleteSubLockId + "?"
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            if let index = self.subLockIds.firstIndex(of: deleteSubLockId) {
                self.deleteSubLockId(deleteSubLockId)
           }
            animV.dismiss()
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
    }
}
