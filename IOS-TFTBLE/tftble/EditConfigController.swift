//
//  EditConfig.swift
//  tftble
//
//  Created by jeech on 2019/12/18.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import UIKit
import iOSDFULibrary 
import CoreBluetooth
import CLXToast
import QMUIKit
import ActionSheetPicker_3_0
extension UILabel{
    var defaultFont: UIFont?{
        get { return self.font }
        set { self.font = newValue }
    }
}
class EditConfigController:UIViewController,CBCentralManagerDelegate,CBPeripheralDelegate,DFUServiceDelegate, DFUProgressDelegate, LoggerDelegate
,URLSessionDownloadDelegate{
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
    private lazy var session:URLSession = {
        //只执行一次
        let config = URLSessionConfiguration.default
        let currentSession = URLSession(configuration: config, delegate: self,
                                        delegateQueue: nil)
        return currentSession
        
    }()
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
    private let transmittedPowerList:[String] = ["4","0","-4","-8","-12","-16","-20"]
 
    private var saveIntervalList:[String] = []
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
        "relay":["read":109,"write":108]
    ]
    private var waitingView:AEUIAlertView!
    private var pwdAlert:AEUIAlertView!
    private var upgradeWarningView:AEAlertView!
    private var hardware = ""
    var software = ""
    private var netSoftwareVersion = "0"
    private var upgradePackageLink = ""
    private var editRangeValueType = ""
    private var newPwd : String!
    private var broadcastCycle = 0
    private var tempAlarmUp = 0.0
    private var tempAlarmDown = 0.0
    private var tempAlarmUpOpen = false
    private var tempAlarmDownOpen = false
    private var humidityAlarmUp = 0
    private var humidityAlarmDown = 0
    private var humidityAlarmUpOpen = false
    private var humidityAlarmDownOpen = false
    private var ledOpen = false
    private var relayStatus = false
    private var selfPeripheral:CBPeripheral!
    private var characteristic: CBCharacteristic?
    private var nameLabel:UILabel!
    private var nameContentLabel:UILabel!
    private var editNameBtn:QMUIGhostButton!
    private var modelLabel:UILabel!
    private var modelContentLabel:UILabel!
    private var hardwareLabel:UILabel!
    private var hardwareContentLabel:UILabel!
    private var debugUpgradeBtn:QMUIGhostButton!
    private var softwareLabel:UILabel!
    private var softwareContentLabel:UILabel!
    private var editSoftwareBtn:QMUIGhostButton!
    private var passwordLabel:UILabel!
    private var passwordContentLabel:UILabel!
    private var editPasswordBtn:QMUIGhostButton!
    private var broadcastCycleLabel:UILabel!
    private var broadcastCycleContentLabel:UILabel!
    private var transmittedPowerLabel:UILabel!
    private var transmittedPowerContentLabel:UILabel!
    private var editBroadcastCycleBtn:QMUIGhostButton!
    private var tempHighAlarmLabel:UILabel!
    private var tempHighAlarmContentLabel:UILabel!
    private var editTempHighAlarmBtn:QMUIGhostButton!
    private var tempLowAlarmLabel:UILabel!
    private var tempLowAlarmContentLabel:UILabel!
    private var editTempLowAlarmBtn:QMUIGhostButton!
    private var humidityHighAlarmLabel:UILabel!
    private var humidityHighAlarmContentLabel:UILabel!
    private var editHumidityHighAlarmBtn:QMUIGhostButton!
    private var humidityLowAlarmLabel:UILabel!
    private var humidityLowAlarmContentLabel:UILabel!
    private var editHumidityLowAlarmBtn:QMUIGhostButton!
    private var saveCountLabel:UILabel!
    private var saveCountContentLabel:UILabel!
    private var saveCountRefreshBtn:QMUIGhostButton!
    private var saveCountReadBtn:QMUIGhostButton!
    private var editTransmittedPowerBtn:QMUIGhostButton!
    private var recordControlLabel:UILabel!
    private var clearRecordLabel:UILabel!
    private var startRecordBtn:QMUIGhostButton!
    private var stopRecordBtn:QMUIGhostButton!
    private var clearRecordBtn:QMUIGhostButton!
    
    private var saveIntervalLabel:UILabel!
    private var saveIntervalContentLabel:UILabel!
    private var editSaveIntervalBtn:QMUIGhostButton!
    
    
    private var ledLabel:UILabel!
    private var ledSwitch:UISwitch!
    private var relayLabel:UILabel!
    private var relaySwitch:UISwitch!
    private var relayStatusLabel:UILabel!
    private var resetFactoryLabel:UILabel!
    private var editResetFactoryBtn:QMUIGhostButton!
    private var transmittedPower = 0
    private var isSaveRecordStatus = false
    private var pwdErrorWarning = false;
    private var saveCount = 0
    private var saveAlarmCount = 0
    private var startDate:Date!
    private var endDate:Date!
    private var allBleHisData:[BleHisData] = [BleHisData]()
    private var originHistoryList:[[[UInt8]]] = [[[UInt8]]]()
    private var curHistoryList:[[UInt8]] = [[UInt8]]()
    private var historyIndex = 0
    
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
        for var i in 6..<60{
            self.saveIntervalList.append(String(format:"%d",i * 10))
        }
        self.navigationItem.titleView = titleLabel
        self.initRightBtn(isConnect:false)
        self.view.backgroundColor = UIColor.white
        self.needConnect = true
        let mac = self.cbPeripheral.identifier.uuidString
        print(mac)
        //        Toast.waitingBuilder.prompt("Connectting").show()
        self.initUI()
        self.showWaitingWin(title: NSLocalizedString("connecting", comment: "Connecting"))
        self.checkUpdate(isEnterCheck: true) 
        
    }
    
    @objc private func refreshClick() {
        print ("refresh click")
        self.initStart = false
        if self.connected == true{
            self.centralManager.cancelPeripheralConnection(self.selfPeripheral)
        }
        self.centralManager.connect(self.selfPeripheral)
        self.notUpdateInit()
    }
    
    func checkUpdate(isEnterCheck:Bool){
        var deviceType = self.deviceType
        var curVerionInt = Int(self.software) ?? 0
        if (deviceType == "S02") && (curVerionInt <= 8){
            deviceType = "S01"
        }
        let url: NSURL = NSURL(string: "http://openapi.tftiot.com:8010/v1/ble-version?device_type=\(deviceType)")!
        let request: NSURLRequest = NSURLRequest(url: url as URL)
        self.showWaitingWin(title: "Waiting")
        NSURLConnection.sendAsynchronousRequest(request as URLRequest, queue: OperationQueue.main, completionHandler:{
            (response, data, error) -> Void in
            self.waitingView.dismiss()
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
                    let jsonData:NSDictionary = dict?["data"] as! NSDictionary
                    if jsonData != nil{
                        var version = jsonData["version"] as! String
                        var packageLink = jsonData["link"] as! String
                        self.upgradePackUrl = NSHomeDirectory() + "/Documents/dfu_app_\(self.deviceType)_V\(self.netSoftwareVersion).zip"
                        if version != nil && packageLink != nil{
                            version = version.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
                            var netVersionInt = Int(version) ?? 0
                            self.netSoftwareVersion = version
                            self.upgradePackageLink = packageLink
                            if !isEnterCheck{
                                if netVersionInt != curVerionInt {
                                    //need update
                                    self.editSoftwareBtn.isHidden = false
                                    self.showUpgradeWin()
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
                }else{
                    self.notUpdateInit()
                }
            }
        })
    }
    
    func notUpdateInit(){
        if self.onView{
            self.showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
            self.showPwdWin()
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
            if(self.isUpgrade){
                self.doUpgrade()
            }else{
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
        for characteristic: CBCharacteristic in service.characteristics! {
        
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
        let data = characteristic.value
        if data != nil{
            let bytes = [UInt8](data!)
            print(bytes)
            if(bytes.count > 1){
                let status = bytes[0]
                let type = bytes[1]
                if status == 0{
                    if type == UInt8(self.controlFunc["deviceName"]?["read"] ?? 0) ||
                        type == UInt8(self.controlFunc["deviceName"]?["write"] ?? 0){
                        self.readDeviceNameResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["password"]?["write"] ?? 0){
                        Toast.hudBuilder.title("password has been updated!").show()
                        self.confirmPwd = self.newPwd
                    }else if type == UInt8(self.controlFunc["broadcastCycle"]?["read"] ?? 0) ||
                        type == UInt8(self.controlFunc["broadcastCycle"]?["write"] ?? 0){
                        self.readBroadcastCycleResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["transmittedPower"]?["read"] ?? 0) ||
                        type == UInt8(self.controlFunc["transmittedPower"]?["write"] ?? 0){
                        self.readTransmittedPowerResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["firmware"]?["read"] ?? 0){
                        self.readVersionResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["saveCount"]?["read"] ?? 0){
                        self.readSaveCountResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["readOriginData"]?["read"] ?? 0){
                        self.readHistoryResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["readAlarm"]?["read"] ?? 0){
                        self.readNextAlarmResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["readNextAlarm"]?["read"] ?? 0){
                        self.readNextAlarmResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["startRecord"]?["write"] ?? 0){
                        self.readSaveCount()
                    }else if type == UInt8(self.controlFunc["stopRecord"]?["write"] ?? 0){
                        self.readSaveCount()
                    }else if type == UInt8(self.controlFunc["clearRecord"]?["write"] ?? 0){
                        self.readSaveCount()
                    }else if type == UInt8(self.controlFunc["saveInterval"]?["read"] ?? 0) ||
                        type == UInt8(self.controlFunc["saveInterval"]?["write"] ?? 0){
                        self.readSaveIntervalResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["humidityAlarm"]?["read"] ?? 0) ||
                        type == UInt8(self.controlFunc["humidityAlarm"]?["write"] ?? 0){
                        self.readHumidityAlarmResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["tempAlarm"]?["read"] ?? 0) ||
                        type == UInt8(self.controlFunc["tempAlarm"]?["write"] ?? 0){
                        self.readTempAlarmResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["ledOpen"]?["read"] ?? 0) ||
                        type == UInt8(self.controlFunc["ledOpen"]?["write"] ?? 0){
                        self.readLedOpenStatusResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["relay"]?["read"] ?? 0) ||
                        type == UInt8(self.controlFunc["relay"]?["write"] ?? 0){
                        self.readRelayStatusResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["resetFactory"]?["write"] ?? 0){
                        Toast.hudBuilder.title(NSLocalizedString("upgrade_success_warning",comment:"Factory Settings restored successfully, please enter the password to reconnect") ).show()
                        //                        self.readBroadcastCycle()
                        //                        self.readDeviceName()
                        //                        self.readVersion()
                        //                        self.readHumidityAlarm()
                        //                        self.readTempAlarm()
                        //                        self.readLedOpenStatus()
                        //                        self.readRelayStatus()
                        //                        self.fixTime()
                        self.showPwdWin()
                    }
                }else if(status == 7){
                    if type == UInt8(self.controlFunc["readAlarm"]?["read"] ?? 0){
                        self.readNextAlarmResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["readNextAlarm"]?["read"] ?? 0){
                        self.readNextAlarmResp(resp: bytes)
                    }
                }else if(status == 1){
                    self.waitingView.dismiss()
                    if(!self.pwdErrorWarning){
                        self.pwdErrorWarning = true;
                        Toast.hudBuilder.title(NSLocalizedString("password_error_warning", comment: "Password is error")).show()
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
    
    func readDeviceName(){
        self.readData(cmdHead: self.controlFunc["deviceName"]?["read"] ?? 0)
    }
    func readDeviceNameResp(resp:[UInt8]){
        var nameArray = [UInt8]()
        var index = 2
        while index < resp.count - 1{
            nameArray.append(resp[index])
            index+=1
        }
        let name = String(bytes:nameArray,encoding:.utf8)
        self.nameContentLabel.text = name
        self.waitingView.dismiss()
    }
    
    func readBroadcastCycle(){
        if !self.isCurrentDeviceTypeFunc(funcName: "broadcastCycle"){
            return
        }
        self.readData(cmdHead: self.controlFunc["broadcastCycle"]?["read"] ?? 0)
    }
    
    func readBroadcastCycleResp(resp:[UInt8]){
        let broadcast = (Int(resp[2]) << 8) + Int(resp[3])
        self.broadcastCycleContentLabel.text = String(format:"%ds",broadcast)
    }
    
    func readSaveInterval(){
        if !self.isCurrentDeviceTypeFunc(funcName: "saveInterval"){
            return
        }
        self.readData(cmdHead: self.controlFunc["saveInterval"]?["read"] ?? 0)
    }
    func readSaveIntervalResp(resp:[UInt8]){
        let saveInterval = (Int(resp[2]) << 8) + Int(resp[3])
        self.saveIntervalContentLabel.text = String(format:"%ds",saveInterval)
    }
    
    func readTransmittedPower(){
        if !self.isCurrentDeviceTypeFunc(funcName: "transmittedPower"){
            return
        }
        self.readData(cmdHead: self.controlFunc["transmittedPower"]?["read"] ?? 0)
    }
    
    func readTransmittedPowerResp(resp:[UInt8]){
        let readTransmittedPower = Int(resp[2]) > 128 ? Int(resp[2]) - 256 : Int(resp[2])
        self.transmittedPower = readTransmittedPower
        self.transmittedPowerContentLabel.text = String(format:"%d dBm",readTransmittedPower)
    }
    
    func readSaveCount(){
        if !self.isCurrentDeviceTypeFunc(funcName: "saveCount"){
            return
        }
        self.readData(cmdHead: self.controlFunc["saveCount"]?["read"] ?? 0)
    }
    
    func readSaveCountResp(resp:[UInt8]){
        self.waitingView.dismiss()
        self.isSaveRecordStatus = Int(resp[2]) == 0 ? false : true
        self.saveCount = (Int(resp[3]) << 8) + Int(resp[4])
        self.saveAlarmCount = (Int(resp[5]) << 8) + Int(resp[6])
        if(self.deviceType == "S02"){
            self.saveCountContentLabel.text = String(self.saveCount)
            if(self.saveCount == 0){
                self.saveCountReadBtn.isHidden = true
            }
        }else{
            self.saveCountContentLabel.text = String(self.saveAlarmCount)
            if(self.saveAlarmCount == 0){
                self.saveCountReadBtn.isHidden = true
            }
        }
        
        if(self.isSaveRecordStatus){
            self.startRecordBtn.isHidden = true
            self.stopRecordBtn.isHidden = false
        }else{
            self.startRecordBtn.isHidden = false
            self.stopRecordBtn.isHidden = true
        }
        
    }
    
    func readVersion(){
        if !self.isCurrentDeviceTypeFunc(funcName: "firmware"){
            return
        }
        self.readData(cmdHead: self.controlFunc["firmware"]?["read"] ?? 0)
    }
    func readVersionResp(resp:[UInt8]){
        if resp[2] == 2{
            self.modelContentLabel.text = "TSTH1-B"
        }else if resp[2] == 4{
            self.modelContentLabel.text = "TSDT1-B"
        }else if resp[2] == 5{
            self.modelContentLabel.text = "TSR1-B"
        }
        let hardware = Utils.parseHardwareVersion(hardware: Utils.uint8ToHexStr(value: resp[3]))
        self.hardwareContentLabel.text = hardware
        self.software = String(resp[4])
        if self.software != self.netSoftwareVersion{
            self.editSoftwareBtn.isHidden = false
        }
        self.softwareContentLabel.text = String.init(format:"V%d",resp[4])
    }
    
    func readHumidityAlarm(){
        if !self.isCurrentDeviceTypeFunc(funcName: "humidityAlarm"){
            return
        }
        self.readData(cmdHead: self.controlFunc["humidityAlarm"]?["read"] ?? 0)
    }
    
    func readHumidityAlarmResp(resp:[UInt8]){
        let humidityAlarmUpTemp = (Int(resp[2]) << 8) + Int(resp[3])
        let humidityAlarmDownTemp = (Int(resp[4]) << 8) + Int(resp[5])
        if humidityAlarmUpTemp == 4095{
            self.humidityAlarmUp = 0
            self.humidityAlarmUpOpen = false
            self.humidityHighAlarmContentLabel.text = ""
        }else{
            self.humidityAlarmUp = (humidityAlarmUpTemp & 0xfff)
            self.humidityAlarmUpOpen = true
            self.humidityHighAlarmContentLabel.text = String(self.humidityAlarmUp)
        }
        if humidityAlarmDownTemp == 4095{
            self.humidityAlarmDown = 0
            self.humidityAlarmDownOpen = false
            self.humidityLowAlarmContentLabel.text = ""
        }else{
            self.humidityAlarmDown = (humidityAlarmDownTemp & 0xfff)
            self.humidityAlarmDownOpen = true
            self.humidityLowAlarmContentLabel.text = String(self.humidityAlarmDown)
        }
    }
    
    func readTempAlarm(){
        if !self.isCurrentDeviceTypeFunc(funcName: "tempAlarm"){
            return
        }
        self.readData(cmdHead: self.controlFunc["tempAlarm"]?["read"] ?? 0)
    }
    
    func readTempAlarmResp(resp:[UInt8]){
        let tempAlarmUpTemp = (Int(resp[2]) << 8) + Int(resp[3])
        let tempAlarmDownTemp = (Int(resp[4]) << 8) + Int(resp[5])
        if tempAlarmUpTemp == 4095{
            self.tempAlarmUp = 0
            self.tempAlarmUpOpen = false
//            self.tempHighAlarmContentLabel.text = Utils.getCurTempUnit()
        }else{
            self.tempAlarmUpOpen = true
            if (tempAlarmUpTemp & 0x8000) == 0x8000{
                self.tempAlarmUp = Double(tempAlarmUpTemp & 0xfff) * -1 * 0.1
            }else{
                self.tempAlarmUp = Double(tempAlarmUpTemp & 0xfff)  * 0.1
            }
            print(self.tempAlarmUp)
            let curTempUp = Utils.getCurTemp(sourceTemp: Float(self.tempAlarmUp))
            self.tempHighAlarmContentLabel.text = String.init(format:"%.1f %@",curTempUp,Utils.getCurTempUnit())
        }
        if tempAlarmDownTemp == 4095{
            self.tempAlarmDown = 0
            self.tempAlarmDownOpen = false
//            self.tempLowAlarmContentLabel.text = Utils.getCurTempUnit()
        }else{
            if (tempAlarmDownTemp & 0x8000) == 0x8000{
                self.tempAlarmDown = Double(tempAlarmDownTemp & 0xfff) * -1 * 0.1
            }else{
                self.tempAlarmDown = Double(tempAlarmDownTemp & 0xfff) * 0.1
            }
            
            self.tempAlarmDownOpen = true
            let curTempDown = Utils.getCurTemp(sourceTemp: Float(self.tempAlarmDown))
            self.tempLowAlarmContentLabel.text = String.init(format:"%.1f %@",curTempDown,Utils.getCurTempUnit())
        }
    }
    
    func readLedOpenStatus(){
        if !self.isCurrentDeviceTypeFunc(funcName: "ledOpen"){
            return
        }
        self.readData(cmdHead: self.controlFunc["ledOpen"]?["read"] ?? 0)
    }
    
    func readLedOpenStatusResp(resp:[UInt8]){
        if resp[2] == 0{
            self.ledOpen = false;
            self.ledSwitch.isOn = false
        }else{
            self.ledOpen = true;
            self.ledSwitch.isOn = true
        }
    }
    
    func readRelayStatus(){
        if !self.isCurrentDeviceTypeFunc(funcName: "relay"){
            return
        }
        self.readData(cmdHead: self.controlFunc["relay"]?["read"] ?? 0)
    }
    
    func readRelayStatusResp(resp:[UInt8]){
        if resp[2] == 0{
            self.relayStatus = false;
            self.relaySwitch.isOn = false
            self.relayStatusLabel.text = "NC"
        }else{
            self.relayStatus = true;
            self.relaySwitch.isOn = true
            self.relayStatusLabel.text = "NO"
        }
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
        self.writeArrayData(cmdHead: self.controlFunc["time"]?["write"] ?? 0, content: data)
    }
    
    func readHistory(){
        if !self.isCurrentDeviceTypeFunc(funcName: "readOriginData") || self.historyIndex > 11{
            return
        }
        var data = [UInt8(self.historyIndex)]
        self.writeArrayData(cmdHead: self.controlFunc["readOriginData"]?["read"] ?? 0, content: data)
        self.historyIndex += 1
    }
    
    func readHistoryResp(resp:[UInt8]){
        if self.curHistoryList.count < 17{
            let realResp = Utils.arraysCopyOfRange(src: resp, from: 2, to: resp.count - 1)
            var isExist = false
            for i in 0..<self.curHistoryList.count{
                var item  = self.curHistoryList[i]
                if Utils.arraysEqual(item1: item, item2: realResp){
                    isExist = true
                    break
                }
            }
            if !isExist{
                self.curHistoryList.append(realResp)
            }
            let percent = Float(self.originHistoryList.count * 17 + curHistoryList.count) / 204.0
            self.waitingView.title = String(format:"%.2f%%",percent*100)
        }
        if self.curHistoryList.count == 17{
            originHistoryList.append(curHistoryList)
            let percent = Float(self.originHistoryList.count * 17) / 204.0
            self.waitingView.title = String(format:"%.2f%%",percent*100)
            self.curHistoryList = [[UInt8]]()
            if self.historyIndex < 12{
                self.readHistory()
            }else{
                var mergeData = Utils.merOriginHisData(originHistoryList: self.originHistoryList)
                self.allBleHisData.removeAll()
                var i = 0
                while i < mergeData.count{
                    var byteDataArray = mergeData[i]
                    do{
                        var dataCorrect = Utils.checkOriginHisDataCrc(byteDataArray: byteDataArray)
                        if dataCorrect{
                            var bleHisDataList = Utils.parseS02BleHisData(historyArray: byteDataArray)
                            var j = 0
                            while j < bleHisDataList.count{
                                self.allBleHisData.append(bleHisDataList[j])
                                j+=1
                            }
                        }
                    }catch let err as NSError{
                        print("\(err)")
                    }
                    i+=1
                }
                self.waitingView.dismiss()
                var bleHisDataList = [BleHisData]()
                i = 0
                while i < self.allBleHisData.count{
                    var bleHisData = self.allBleHisData[i]
                    if Double(bleHisData.dateStamp) <= self.endDate.timeIntervalSince1970 && Double(bleHisData.dateStamp) >= self.startDate.timeIntervalSince1970{
                        bleHisDataList.append(bleHisData)
                    }
                    i+=1
                }
                self.allBleHisData.removeAll()
                i = 0
                while i < bleHisDataList.count{
                    self.allBleHisData.append(bleHisDataList[i])
                    i+=1
                }
                if self.allBleHisData.count == 0{
                    Toast.hudBuilder.title(NSLocalizedString("not_found_data", comment: "Not found data")).show()
                    return
                }
                let historyView = HistoryReportController()
                historyView.startDate = Int(self.startDate.timeIntervalSince1970)
                historyView.endDate = Int(self.endDate.timeIntervalSince1970)
                historyView.mac = self.mac
                historyView.id = self.mac
                historyView.showBleHisData = self.allBleHisData
                if self.tempAlarmUpOpen{
                    historyView.tempAlarmUp = self.tempAlarmUp
                }else{
                    historyView.tempAlarmUp = 4095
                }
                if self.tempAlarmDownOpen{
                    historyView.tempAlarmDown = self.tempAlarmDown
                }else{
                    historyView.tempAlarmDown = 4095
                }
                if self.humidityAlarmUpOpen{
                    historyView.humidityAlarmUp = self.humidityAlarmUp
                }else{
                    historyView.humidityAlarmUp = 4095
                }
                if self.humidityAlarmDownOpen{
                    historyView.humidityAlarmDown = self.humidityAlarmDown
                }else{
                    historyView.humidityAlarmDown = 4095
                }
                
                historyView.deviceType = self.deviceType
                self.navigationController?.pushViewController(historyView, animated: false)
            }
        }
    }
    
    func readAlarm(){
        if !self.isCurrentDeviceTypeFunc(funcName: "readAlarm"){
            return
        }
        var startDateData = self.formatHex(intValue: Int(self.startDate.timeIntervalSince1970), len: 4)
        let endDateData = self.formatHex(intValue: Int(self.endDate.timeIntervalSince1970), len: 4)
        for byte in endDateData{
            startDateData.append(byte)
        }
        self.writeArrayData(cmdHead: self.controlFunc["readAlarm"]?["read"] ?? 0, content: startDateData)
    }
    
    func readNextAlarm(){
        if !self.isCurrentDeviceTypeFunc(funcName: "readAlarm"){
            return
        }
        let endDateData = self.formatHex(intValue: Int(self.endDate.timeIntervalSince1970), len: 4)
        
        self.writeArrayData(cmdHead: self.controlFunc["readNextAlarm"]?["read"] ?? 0, content: endDateData)
    }
    
    func parseAlarmData(realResp:[UInt8]) -> Int{
        if realResp == nil || realResp.count == 0{
            return 0
        }
        var newTime=0
        var i = 0
        while i < realResp.count{
            let timestamp = Utils.bytes2Integer(buf: realResp, pos: i+0)
            if timestamp > newTime{
                newTime = timestamp
            }
            let battery = realResp[i+4] & 0xff
            let tempSrc = Utils.bytes2Short(bytes: realResp, offset: i+5)
            let temp =  Float((tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1)) / 100.0
            let humidity = realResp[i+7] & 0xff
            let doorStatus = realResp[i+8] & 0xff
            let bleHisData = BleHisData()
            bleHisData.dateStamp = timestamp
            bleHisData.battery = Int(battery)
            bleHisData.prop = Int(doorStatus)
            bleHisData.temp = temp
            bleHisData.humidity = Int(humidity)
            bleHisData.alarm = realResp[i+9]
            allBleHisData.append(bleHisData)
            i+=10
        }
        var percentFloat = (Double(newTime) - self.startDate.timeIntervalSince1970) / 1.0 / (self.endDate.timeIntervalSince1970 - self.startDate.timeIntervalSince1970)
        percentFloat = percentFloat * 100
        let percent = String(format:"%.2f%%",percentFloat)
        self.waitingView.title = percent
        return newTime
    }
    
    func readNextAlarmResp(resp:[UInt8]){
        let realResp = Utils.arraysCopyOfRange(src: resp, from: 2, to: resp.count - 1)
        let time = self.parseAlarmData(realResp: realResp)
        if time != 0{
            self.readNextAlarm()
        }else{
            self.readAlarmSucc()
        }
    }
    
    func readAlarmSucc(){
        self.waitingView.dismiss()
        if self.allBleHisData.count == 0{
            Toast.hudBuilder.title(NSLocalizedString("not_found_data", comment: "Not found data")).show()
            return
        }
        let historyView = HistoryReportController()
        historyView.startDate = Int(self.startDate.timeIntervalSince1970)
        historyView.endDate = Int(self.endDate.timeIntervalSince1970)
        historyView.mac = self.mac
        historyView.id = self.mac
        historyView.showBleHisData = self.allBleHisData
        if self.tempAlarmUpOpen{
            historyView.tempAlarmUp = self.tempAlarmUp
        }else{
            historyView.tempAlarmUp = 4095
        }
        if self.tempAlarmDownOpen{
            historyView.tempAlarmDown = self.tempAlarmDown
        }else{
            historyView.tempAlarmDown = 4095
        }
        if self.humidityAlarmUpOpen{
            historyView.humidityAlarmUp = self.humidityAlarmUp
        }else{
            historyView.humidityAlarmUp = 4095
        }
        if self.humidityAlarmDownOpen{
            historyView.humidityAlarmDown = self.humidityAlarmDown
        }else{
            historyView.humidityAlarmDown = 4095
        }
        historyView.deviceType = self.deviceType
        self.navigationController?.pushViewController(historyView, animated: false)
    }
    
    func startRecord(){
        let now = Date()
        let data = self.formatHex(intValue: Int(now.timeIntervalSince1970), len: 4)
        self.writeArrayData(cmdHead: self.controlFunc["startRecord"]?["write"] ?? 0, content: data)
    }
    func stopRecord(){
        self.writeArrayData(cmdHead: self.controlFunc["stopRecord"]?["write"] ?? 0, content: [])
    }
    
    func clearRecord(){
        self.writeArrayData(cmdHead: self.controlFunc["clearRecord"]?["write"] ?? 0, content: [])
    }
    
    func writePwd(pwd:String){
        self.writeStrData(cmdHead: self.controlFunc["password"]?["write"] ?? 0, dataStr: pwd)
    }
    
    func writeDeviceName(name:String){
        self.writeStrData(cmdHead: self.controlFunc["deviceName"]?["write"] ?? 0, dataStr: name)
    }
    
    func writeTransmittedPower(value:Int){
        var uint8Value = value < 0 ? value + 256 : value
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["transmittedPower"]?["write"] ?? 0, content: data)
    }
    
    func writeBroadcastData(value:Int){
        let data = self.formatHex(intValue: value, len: 2)
        self.writeArrayData(cmdHead: self.controlFunc["broadcastCycle"]?["write"] ?? 0, content: data)
    }
    
    func writeSaveIntervalData(value:Int){
        let data = self.formatHex(intValue: value, len: 2)
        self.writeArrayData(cmdHead: self.controlFunc["saveInterval"]?["write"] ?? 0, content: data)
    }
    
    func writeHumidityAlarmData(upValue:Int,downValue:Int){
        var upData:[UInt8]
        if upValue < 0{
            upData = self.formatHex(intValue: ((-1 * upValue) | 0x8000), len: 2)
        }else{
            upData = self.formatHex(intValue: upValue, len: 2)
        }
        var downData:[UInt8]
        if downValue < 0{
            downData = self.formatHex(intValue: ((-1 * downValue) | 0x8000), len: 2)
        }else{
            downData = self.formatHex(intValue: downValue, len: 2)
        }
        for byte in downData{
            upData.append(byte)
        }
        print(upData)
        self.writeArrayData(cmdHead: self.controlFunc["humidityAlarm"]?["write"] ?? 0, content: upData)
    }
    
    func writeTempAlarmData(upValueInt:Int,downValueInt:Int){
        var upData:[UInt8]
        if upValueInt < 0{
            upData = self.formatHex(intValue: ((-1 * upValueInt) | 0x8000), len: 2)
        }else{
            upData = self.formatHex(intValue: upValueInt, len: 2)
        }
        var downData:[UInt8]
        if downValueInt < 0{
            downData = self.formatHex(intValue: ((-1 * downValueInt) | 0x8000), len: 2)
        }else{
            downData = self.formatHex(intValue: downValueInt, len: 2)
        }
        for byte in downData{
            upData.append(byte)
        }
        print(upData)
        self.writeArrayData(cmdHead: self.controlFunc["tempAlarm"]?["write"] ?? 0, content: upData)
    }
    func writeLedOpenStatus(){
        var data = [UInt8]()
        if self.ledSwitch.isOn {
            data.append(UInt8(1))
        }else{
            data.append(UInt8(0))
        }
        self.writeArrayData(cmdHead: self.controlFunc["ledOpen"]?["write"] ?? 0, content: data)
    }
    
    func writeRelayStatus(){
        var data = [UInt8]()
        if self.relaySwitch.isOn {
            data.append(UInt8(1))
        }else{
            data.append(UInt8(0))
        }
        self.writeArrayData(cmdHead: self.controlFunc["relay"]?["write"] ?? 0, content: data)
    }
    
    func writeResetFactory(){
        self.writeArrayData(cmdHead: self.controlFunc["resetFactory"]?["write"] ?? 0, content: [])
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
                    self.initStart = true
                    self.readBroadcastCycle()
                    self.readDeviceName()
                    self.readVersion()
                    self.readTransmittedPower()
                    self.readSaveCount()
                    self.readSaveInterval()
                    self.readHumidityAlarm()
                    self.readTempAlarm()
                    self.readLedOpenStatus()
                    self.readRelayStatus()
                    self.fixTime()
                }
            }
            //            Toast.currentWaiting?.dismiss(animated: true)
            
        } else {
            print("取消订阅")
        }
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
    
    @objc private func editDeviceName() {
        let editNameAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("device_name", comment: "Device name"), message: nil)
        editNameAlert.textField.placeholder = NSLocalizedString("device_name", comment: "Device name")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editNameAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let deviceName = String(editNameAlert.textField.text ?? "")
            if deviceName.count <= 8 && deviceName.count >= 3{
                self.writeDeviceName(name: deviceName)
                editNameAlert.dismiss()
            }else{
                Toast.hudBuilder.title(NSLocalizedString("incorrect_name_length", comment: "Incorrect name length!")).show()
            }
        }
        editNameAlert.addAction(action: action_one)
        editNameAlert.addAction(action: action_two)
        editNameAlert.show()
    }
    
    
    
    
    @objc private func editPassword() {
        let editPwd = EditPwdController()
        editPwd.delegate = self
        editPwd.connectStatusDelegate = self
        self.leaveViewNeedDisconnect = false
        editPwd.oldPwd = self.confirmPwd
        self.navigationController?.pushViewController(editPwd, animated: false)
    }
    
    @objc private func editTransmittedPower(sender:UIButton){
        var currentIndex = 0
        for var index in 0..<self.transmittedPowerList.count{
            if String(self.transmittedPower) == self.transmittedPowerList[index]{
                currentIndex = index
                break
            }
        }
        
         ActionSheetStringPicker.show(withTitle: "", rows: self.transmittedPowerList, initialSelection:currentIndex, doneBlock: {
                                picker, index, value in
//                                self.currentDateSelect = index
        //                        print("values = \(values)")
        //                        print("indexes = \(indexes)")
//                                self.initDateRangeValue()
            let transmittedPowerValue = Int(self.transmittedPowerList[index])
            self.writeTransmittedPower(value: transmittedPowerValue ?? 0)
                                return
                        }, cancel: { ActionMultipleStringCancelBlock in return }, origin: sender)
    }
    
    @objc private func editBroadcastCycle(){
        let editBroadcastCycleAlert = AEUIAlertView(style: .number, title: NSLocalizedString("broadcast_cycle", comment: "Broadcast cycle"), message: nil)
        editBroadcastCycleAlert.textField.placeholder = NSLocalizedString("broadcast_cycle", comment: "Broadcast cycle")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editBroadcastCycleAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let broadcastCycle = String(editBroadcastCycleAlert.textField.text ?? "")
            if broadcastCycle.count > 0{
                let broadcastCycleInt = Int(broadcastCycle) ?? 0
                if self.deviceType == "S05"{
                    if broadcastCycleInt >= 0 && broadcastCycleInt <= 10{
                        self.writeBroadcastData(value: broadcastCycleInt)
                        editBroadcastCycleAlert.dismiss()
                    }else{
                        Toast.hudBuilder.title(NSLocalizedString("s05_broadcast_cycle_value_length_error", comment: "Value is incorrect!It must between 0 and 10")).show()
                    }
                }else{
                    if broadcastCycleInt >= 5 && broadcastCycleInt <= 1800{
                        self.writeBroadcastData(value: broadcastCycleInt)
                        editBroadcastCycleAlert.dismiss()
                    }else{
                        Toast.hudBuilder.title(NSLocalizedString("broadcast_cycle_value_length_error", comment: "Value is incorrect!It must between 5 and 1800")).show()
                    }
                }
                
                
            }else{
                Toast.hudBuilder.title(NSLocalizedString("broadcast_cycle_value_length_error", comment: "Value is incorrect!It must between 5 and 1800")).show()
            }
        }
        editBroadcastCycleAlert.addAction(action: action_one)
        editBroadcastCycleAlert.addAction(action: action_two)
        editBroadcastCycleAlert.show()
    }
    @objc private func editTempAlarm(){
        self.leaveViewNeedDisconnect = false
        let editRangeValue = EditRangeValueController()
        editRangeValue.delegate = self
        editRangeValue.connectStatusDelegate = self
        self.editRangeValueType = "temp"
        editRangeValue.type = "temp"
        print(self.tempAlarmUp)
        if self.tempAlarmUpOpen{
            editRangeValue.high = String.init(format:"%.1f",Utils.getCurTemp(sourceTemp: Float(self.tempAlarmUp)))
        }else{
            editRangeValue.high = ""
        }
        if self.tempAlarmDownOpen{
            editRangeValue.low = String.init(format:"%.1f",Utils.getCurTemp(sourceTemp: Float(self.tempAlarmDown))) 
        }else{
            editRangeValue.low = ""
        } 
        editRangeValue.highOpen = self.tempAlarmUpOpen
        editRangeValue.lowOpen = self.tempAlarmDownOpen
        self.navigationController?.pushViewController(editRangeValue, animated: false)
    }
    
    @objc private func editHumidityAlarm(){
        self.leaveViewNeedDisconnect = false
        let editRangeValue = EditRangeValueController()
        editRangeValue.delegate = self
        editRangeValue.connectStatusDelegate = self
        self.editRangeValueType = "humidity"
        editRangeValue.type = "humidity"
        editRangeValue.high = self.humidityHighAlarmContentLabel.text ?? ""
        editRangeValue.low = self.humidityLowAlarmContentLabel.text ?? ""
        editRangeValue.highOpen = self.humidityAlarmUpOpen
        editRangeValue.lowOpen = self.humidityAlarmDownOpen
        self.navigationController?.pushViewController(editRangeValue, animated: false)
    }
    @objc private func editSaveIntervalClick(sender:UIButton){
//        let editSaveIntervalAlert = AEUIAlertView(style: .number, title: "Save interval", message: nil)
//        editSaveIntervalAlert.textField.placeholder = "Save interval"
//
//        let action_one = AEAlertAction(title: "Cancel", style: .cancel) { (action) in
//            editSaveIntervalAlert.dismiss()
//        }
//        let action_two = AEAlertAction(title: "Confirm", style: .defaulted) { (action) in
//            let broadcastCycle = String(editSaveIntervalAlert.textField.text ?? "")
//            if broadcastCycle.count > 0{
//                let saveIntervalInt = Int(broadcastCycle) ?? 0
//                if saveIntervalInt >= 10{
//                    self.writeSaveIntervalData(value: saveIntervalInt)
//                    editSaveIntervalAlert.dismiss()
//                }else{
//                    Toast.hudBuilder.title("Value is incorrect!It must be greater than 10").show()
//                }
//
//            }else{
//                Toast.hudBuilder.title("Incorrect name length!").show()
//            }
//        }
//        editSaveIntervalAlert.addAction(action: action_one)
//        editSaveIntervalAlert.addAction(action: action_two)
//        editSaveIntervalAlert.show()
        
         ActionSheetStringPicker.show(withTitle: "", rows: self.saveIntervalList, initialSelection:0, doneBlock: {
                                        picker, index, value in
        //                                self.currentDateSelect = index
                //                        print("values = \(values)")
                //                        print("indexes = \(indexes)")
        //                                self.initDateRangeValue()
                    let saveIntervalInt = Int(self.saveIntervalList[index])
            self.writeSaveIntervalData(value: saveIntervalInt ?? 60)
                                        return
                                }, cancel: { ActionMultipleStringCancelBlock in return }, origin: sender)
    }
    
    @objc private func resetFactoryClick() {
        let animV = AEUIAlertView(style: .textField, title: NSLocalizedString("reset_factory", comment:"Reset Factory"), message: nil)
        animV.textField.placeholder = NSLocalizedString("password", comment:"Password")
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            animV.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            
            let pwd = String(animV.textField.text ?? "")
            if pwd.count > 0{
                self.writeResetFactory()
                animV.dismiss()
            }
        }
        
        animV.addAction(action: action_one)
        animV.addAction(action: action_two)
        animV.show()
        
    }
    @objc func ledSwitchAction(senger:UISwitch){
        print("ledSwitchAction")
        self.writeLedOpenStatus()
    }
    
    @objc func relaySwitchAction(senger:UISwitch){
        if(Utils.isDebug){
            self.writeRelayStatus()
            return
        }
        let relayWarningView = AEAlertView(style: .defaulted)
        relayWarningView.message = NSLocalizedString("confirm_relay_warning", comment: "Try to switch relay?")
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.relaySwitch.isOn = self.relayStatus
            relayWarningView.dismiss()
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            relayWarningView.dismiss()
            self.writeRelayStatus()
        }
        relayWarningView.addAction(action: upgradeCancel)
        relayWarningView.addAction(action: upgradeConfirm)
        relayWarningView.show()
    }
    
    @objc func upgradeClick(){
        self.checkUpdate(isEnterCheck: false)
    }
    
    @objc func debugUpgradeClick(){
        let inputUrlAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("upgradePackageUrl", comment: "Upgrade package url"), message: nil)
        inputUrlAlert.textField.placeholder = NSLocalizedString("upgradePackageUrl", comment: "Upgrade package url")
        
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
                   self.needConnect = false
                   self.centralManager.cancelPeripheralConnection(self.selfPeripheral)
                   self.upgradePackUrl = NSHomeDirectory() + "/Documents/dfu_app_debug_upgrade.zip"
                   if FileTool.fileExists(filePath: self.upgradePackUrl){
                       FileTool.removeFile(self.upgradePackUrl)
                   }
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
    
    @objc func saveCountRefreshClick(){
        self.showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        self.readSaveCount()
    }
    @objc func readHistoryClick(){
        let editView = HistorySelectController()
        self.leaveViewNeedDisconnect = false
        editView.setDateDelegate = self
        self.navigationController?.pushViewController(editView, animated: false)
    }
    
    @objc func startRecordClick(){
        self.startRecord()
    }
    
    @objc func stopRecordClick(){
        self.stopRecord()
    }
    
    @objc func clearRecordClick(){
        self.clearRecord()
    }
    
    
    
    func doUpgrade(){
        self.waitingView.dismiss()
        self.showProgressBar()
        print("try to update")
        self.isUpgrade = false
        self.needConnect = false
        self.centralManager.cancelPeripheralConnection(self.selfPeripheral)
        let lastUpgradeFileUrl = NSHomeDirectory() + "/Documents/dfu_app_\(self.deviceType)_V\(self.software).zip"
        if FileTool.fileExists(filePath: lastUpgradeFileUrl){
            FileTool.removeFile(lastUpgradeFileUrl)
        }
        self.showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        if FileTool.fileExists(filePath: self.upgradePackUrl){
            self.upgradeDevice()
        }else{
            let downloadUrl = URL(string: self.upgradePackageLink)
            //请求
            let request = URLRequest(url: downloadUrl!)
            //下载任务
            let downloadTask = session.downloadTask(with: request)
            //使用resume方法启动任务
            downloadTask.resume()
        }
    }
    
    
    //下载代理方法，下载结束
    func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask,
                    didFinishDownloadingTo location: URL) {
        //下载结束
        print("下载结束")
        //输出下载文件原来的存放目录
        print("location:\(location)")
        //location位置转换
        let locationPath = location.path
        //拷贝到用户目录
        //创建文件管理器
        let fileManager = FileManager.default
        try! fileManager.moveItem(atPath: locationPath, toPath: self.upgradePackUrl)
        print("new location:\(self.upgradePackUrl)")
        self.upgradeDevice()
    }
    
    func upgradeDevice(){
        self.waitingView.dismiss()
        let url = URL(string:self.upgradePackUrl)
        if url != nil{
            let dfuFirmware = DFUFirmware(urlToZipFile: url!)!
            let initiator = DFUServiceInitiator().with(firmware: dfuFirmware)
            // Optional:
            // initiator.forceDfu = true/false // default false
            // initiator.packetReceiptNotificationParameter = N // default is 12
            initiator.logger = self // - to get log info
            initiator.delegate = self // - to be informed about current state and errors
            initiator.progressDelegate = self // - to show progress bar
            // initiator.peripheralSelector = ... // the default selector is used
            
            let controller = initiator.start(target: self.selfPeripheral)
        }
    }
    
    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        print("下载error：\(error)")
        if error != nil{
            DispatchQueue.main.async {
                if self.progressView != nil {
                    self.progressView.dismiss()
                }
                if self.waitingView != nil{
                    self.waitingView.dismiss()
                }
                
                Toast.hudBuilder.title(NSLocalizedString("downloadError", comment: "Download upgrade package error,please try again!")).show()
            }
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
    
    func initUI(){
        let scrollView = UIScrollView()
        scrollView.frame = self.view.bounds
        var scrollViewHeight:Float = 910
        if self.deviceType != "S02"{
            scrollViewHeight = 730
        }
        scrollView.contentSize = CGSize(width: KSize.width, height: CGFloat(scrollViewHeight))
        
        //        scrollView.isPagingEnabled = true
        scrollView.showsHorizontalScrollIndicator = false
        scrollView.showsVerticalScrollIndicator = false
        scrollView.scrollsToTop = false
        self.view.addSubview(scrollView)
        let descWidth = Int(KSize.width / 3)
        let contentX = Int(KSize.width / 3 + 10)
        let btnX = Int(KSize.width / 3 * 2 + 15)
        var startLabelY:Int = 0
        let lineHigh:Int = 60
        var lineY:Int = 60
        var btnY:Int = 15
        let btnHeight = 30
        self.nameLabel = UILabel()
        self.nameLabel.textColor = UIColor.black
        self.nameLabel.text = NSLocalizedString("name_desc", comment: "Name:")
        self.nameLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.nameLabel.numberOfLines = 0;
        self.nameLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        scrollView.addSubview(self.nameLabel)
        self.nameContentLabel = UILabel()
        self.nameContentLabel.textColor = UIColor.black
        self.nameContentLabel.text = ""
        self.nameContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
        scrollView.addSubview(self.nameContentLabel)
        self.editNameBtn = QMUIGhostButton()
        self.editNameBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editNameBtn.ghostColor = UIColor.colorPrimary
        self.editNameBtn.frame = CGRect(x: btnX, y: btnY, width: 60, height: btnHeight)
        self.editNameBtn.addTarget(self, action: #selector(editDeviceName), for:.touchUpInside)
        scrollView.addSubview(self.editNameBtn)
        let nameLine = UIView()
        nameLine.backgroundColor = UIColor.gray
        nameLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        scrollView.addSubview(nameLine)
        startLabelY += lineHigh
        lineY += lineHigh
        btnY += lineHigh
        self.modelLabel = UILabel()
        self.modelLabel.textColor = UIColor.black
        self.modelLabel.text = NSLocalizedString("device_model", comment: "Device model:")
        self.modelLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.modelLabel.numberOfLines = 0;
        self.modelLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        scrollView.addSubview(self.modelLabel)
        self.modelContentLabel = UILabel()
        self.modelContentLabel.text = ""
        self.modelContentLabel.textColor = UIColor.black
        self.modelContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
        scrollView.addSubview(self.modelContentLabel)
        let modelLine = UIView()
        modelLine.backgroundColor = UIColor.gray
        modelLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        scrollView.addSubview(modelLine)
        startLabelY += lineHigh
        lineY += lineHigh
        btnY += lineHigh
        self.hardwareLabel = UILabel()
        self.hardwareLabel.textColor = UIColor.black
        self.hardwareLabel.text = NSLocalizedString("hardware", comment: "Hardware:")
        self.hardwareLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.hardwareLabel.numberOfLines = 0;
        self.hardwareLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        scrollView.addSubview(self.hardwareLabel)
        self.hardwareContentLabel = UILabel()
        self.hardwareContentLabel.textColor = UIColor.black
        self.hardwareContentLabel.text = ""
        self.hardwareContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
        scrollView.addSubview(self.hardwareContentLabel)
        self.debugUpgradeBtn = QMUIGhostButton()
        self.debugUpgradeBtn.setTitle(NSLocalizedString("debugUpgrade", comment: "Debug Upgrade"), for: .normal)
        self.debugUpgradeBtn.ghostColor = UIColor.colorPrimary
        self.debugUpgradeBtn.frame = CGRect(x: btnX, y: btnY, width: 90, height: btnHeight)
        if(Utils.isDebug){
            self.debugUpgradeBtn.isHidden = false
        }else{
            self.debugUpgradeBtn.isHidden = true
        }
        self.debugUpgradeBtn.addTarget(self, action: #selector(debugUpgradeClick), for:.touchUpInside)
        scrollView.addSubview(self.debugUpgradeBtn)
        
        let hardwareLine = UIView()
        hardwareLine.backgroundColor = UIColor.gray
        hardwareLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        scrollView.addSubview(hardwareLine)
        startLabelY += lineHigh
        lineY += lineHigh
        btnY += lineHigh
        self.softwareLabel = UILabel()
        self.softwareLabel.textColor = UIColor.black
        self.softwareLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.softwareLabel.numberOfLines = 0;
        self.softwareLabel.text = NSLocalizedString("software", comment: "Software:")
        self.softwareLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        scrollView.addSubview(self.softwareLabel)
        self.softwareContentLabel = UILabel()
        self.softwareContentLabel.textColor = UIColor.black
        self.softwareContentLabel.text = ""
        self.softwareContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
        scrollView.addSubview(self.softwareContentLabel)
        self.editSoftwareBtn = QMUIGhostButton()
        self.editSoftwareBtn.setTitle(NSLocalizedString("upgrade", comment: "Upgrade"), for: .normal)
        self.editSoftwareBtn.ghostColor = UIColor.colorPrimary
        self.editSoftwareBtn.frame = CGRect(x: btnX, y: btnY, width: 90, height: btnHeight)
        self.editSoftwareBtn.isHidden = true
        self.editSoftwareBtn.addTarget(self, action: #selector(upgradeClick), for:.touchUpInside)
        scrollView.addSubview(self.editSoftwareBtn)
        let softwareLine = UIView()
        softwareLine.backgroundColor = UIColor.gray
        softwareLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        scrollView.addSubview(softwareLine)
        startLabelY += lineHigh
        lineY += lineHigh
        btnY += lineHigh
        self.passwordLabel = UILabel()
        self.passwordLabel.textColor = UIColor.black
        self.passwordLabel.text = NSLocalizedString("password_desc", comment: "Password:")
        self.passwordLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.passwordLabel.numberOfLines = 0;
        self.passwordLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        scrollView.addSubview(self.passwordLabel)
        self.passwordContentLabel = UILabel()
        self.passwordContentLabel.textColor = UIColor.black
        self.passwordContentLabel.text = "******"
        self.passwordContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
        scrollView.addSubview(self.passwordContentLabel)
        self.editPasswordBtn = QMUIGhostButton()
        self.editPasswordBtn.setTitle(NSLocalizedString(NSLocalizedString("edit", comment: "Edit"), comment: "Edit"), for: .normal)
        self.editPasswordBtn.ghostColor = UIColor.colorPrimary
        self.editPasswordBtn.frame = CGRect(x: btnX, y: btnY, width: 60, height: btnHeight)
        self.editPasswordBtn.addTarget(self, action: #selector(editPassword), for:.touchUpInside)
        scrollView.addSubview(self.editPasswordBtn)
        let passwordLine = UIView()
        passwordLine.backgroundColor = UIColor.gray
        passwordLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        scrollView.addSubview(passwordLine)
        startLabelY += lineHigh
        lineY += lineHigh
        btnY += lineHigh
        if self.isCurrentDeviceTypeFunc(funcName: "broadcastCycle"){
            self.broadcastCycleLabel = UILabel()
            self.broadcastCycleLabel.textColor = UIColor.black
            self.broadcastCycleLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.broadcastCycleLabel.numberOfLines = 0;
            self.broadcastCycleLabel.text = NSLocalizedString("broadcast_cycle_desc", comment: "Broadcast cycle:")
            self.broadcastCycleLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.broadcastCycleLabel.numberOfLines = 0;
            self.broadcastCycleLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.broadcastCycleLabel)
            self.broadcastCycleContentLabel = UILabel()
            self.broadcastCycleContentLabel.textColor = UIColor.black
            self.broadcastCycleContentLabel.text = ""
            self.broadcastCycleContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
            scrollView.addSubview(self.broadcastCycleContentLabel)
            self.editBroadcastCycleBtn = QMUIGhostButton()
            self.editBroadcastCycleBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
            self.editBroadcastCycleBtn.ghostColor = UIColor.colorPrimary
            self.editBroadcastCycleBtn.frame = CGRect(x: btnX, y: btnY, width: 60, height: btnHeight)
            self.editBroadcastCycleBtn.addTarget(self, action: #selector(editBroadcastCycle), for:.touchUpInside)
            scrollView.addSubview(self.editBroadcastCycleBtn)
            let broadcastCycleLine = UIView()
            broadcastCycleLine.backgroundColor = UIColor.gray
            broadcastCycleLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(broadcastCycleLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        if self.isCurrentDeviceTypeFunc(funcName: "transmittedPower"){
            self.transmittedPowerLabel = UILabel()
            self.transmittedPowerLabel.textColor = UIColor.black
            self.transmittedPowerLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.transmittedPowerLabel.numberOfLines = 0;
            self.transmittedPowerLabel.text = NSLocalizedString("transmitted_power", comment: "Transmitted power:")
            self.transmittedPowerLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.transmittedPowerLabel.numberOfLines = 0;
            self.transmittedPowerLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.transmittedPowerLabel)
            self.transmittedPowerContentLabel = UILabel()
            self.transmittedPowerContentLabel.textColor = UIColor.black
            self.transmittedPowerContentLabel.text = ""
            self.transmittedPowerContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
            scrollView.addSubview(self.transmittedPowerContentLabel)
            self.editTransmittedPowerBtn = QMUIGhostButton()
            self.editTransmittedPowerBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
            self.editTransmittedPowerBtn.ghostColor = UIColor.colorPrimary
            self.editTransmittedPowerBtn.frame = CGRect(x: btnX, y: btnY, width: 60, height: btnHeight)
            self.editTransmittedPowerBtn.addTarget(self, action: #selector(editTransmittedPower), for:.touchUpInside)
            scrollView.addSubview(self.editTransmittedPowerBtn)
            let transmittedPowerLine = UIView()
            transmittedPowerLine.backgroundColor = UIColor.gray
            transmittedPowerLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(transmittedPowerLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        if self.isCurrentDeviceTypeFunc(funcName: "saveInterval"){
            self.saveIntervalLabel = UILabel()
            self.saveIntervalLabel.textColor = UIColor.black
            self.saveIntervalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.saveIntervalLabel.numberOfLines = 0;
            self.saveIntervalLabel.text = NSLocalizedString("save_interval_desc", comment: "Save interval:")
            self.saveIntervalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.saveIntervalLabel.numberOfLines = 0;
            self.saveIntervalLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.saveIntervalLabel)
            self.saveIntervalContentLabel = UILabel()
            self.saveIntervalContentLabel.textColor = UIColor.black
            self.saveIntervalContentLabel.text = ""
            self.saveIntervalContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
            scrollView.addSubview(self.saveIntervalContentLabel)
            self.editSaveIntervalBtn = QMUIGhostButton()
            self.editSaveIntervalBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
            self.editSaveIntervalBtn.ghostColor = UIColor.colorPrimary
            self.editSaveIntervalBtn.frame = CGRect(x: btnX, y: btnY, width: 60, height: btnHeight)
            self.editSaveIntervalBtn.addTarget(self, action: #selector(editSaveIntervalClick), for:.touchUpInside)
            scrollView.addSubview(self.editSaveIntervalBtn)
            let saveIntervalLine = UIView()
            saveIntervalLine.backgroundColor = UIColor.gray
            saveIntervalLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(saveIntervalLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        if self.isCurrentDeviceTypeFunc(funcName: "tempAlarm"){
            self.tempHighAlarmLabel = UILabel()
            self.tempHighAlarmLabel.textColor = UIColor.black
            self.tempHighAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.tempHighAlarmLabel.numberOfLines = 0;
            self.tempHighAlarmLabel.text = NSLocalizedString("temperature_high_alarm_desc", comment: "Temperature high alarm:")
            self.tempHighAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.tempHighAlarmLabel.numberOfLines = 0;
            self.tempHighAlarmLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.tempHighAlarmLabel)
            self.tempHighAlarmContentLabel = UILabel()
            self.tempHighAlarmContentLabel.textColor = UIColor.black
            self.tempHighAlarmContentLabel.text = ""
            self.tempHighAlarmContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
            scrollView.addSubview(self.tempHighAlarmContentLabel)
            self.editTempHighAlarmBtn = QMUIGhostButton()
            self.editTempHighAlarmBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
            self.editTempHighAlarmBtn.ghostColor = UIColor.colorPrimary
            self.editTempHighAlarmBtn.frame = CGRect(x: btnX, y: btnY, width: 60, height: btnHeight)
            self.editTempHighAlarmBtn.addTarget(self, action: #selector(editTempAlarm), for:.touchUpInside)
            scrollView.addSubview(self.editTempHighAlarmBtn)
            let tempAlarmLine = UIView()
            tempAlarmLine.backgroundColor = UIColor.gray
            tempAlarmLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(tempAlarmLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
            self.tempLowAlarmLabel = UILabel()
            self.tempLowAlarmLabel.textColor = UIColor.black
            self.tempLowAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.tempLowAlarmLabel.numberOfLines = 0;
            self.tempLowAlarmLabel.text = NSLocalizedString("temperature_low_alarm_desc", comment: "Temperature low alarm:")
            self.tempLowAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.tempLowAlarmLabel.numberOfLines = 0;
            self.tempLowAlarmLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.tempLowAlarmLabel)
            self.tempLowAlarmContentLabel = UILabel()
            self.tempLowAlarmContentLabel.textColor = UIColor.black
            self.tempLowAlarmContentLabel.text = ""
            self.tempLowAlarmContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
            scrollView.addSubview(self.tempLowAlarmContentLabel)
            self.editTempLowAlarmBtn = QMUIGhostButton()
            self.editTempLowAlarmBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
            self.editTempLowAlarmBtn.ghostColor = UIColor.colorPrimary
            self.editTempLowAlarmBtn.frame = CGRect(x: btnX, y: btnY, width: 60, height: btnHeight)
            self.editTempLowAlarmBtn.addTarget(self, action: #selector(editTempAlarm), for:.touchUpInside)
            scrollView.addSubview(self.editTempLowAlarmBtn)
            let humidityAlarmLine = UIView()
            humidityAlarmLine.backgroundColor = UIColor.gray
            humidityAlarmLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(humidityAlarmLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        if self.isCurrentDeviceTypeFunc(funcName: "humidityAlarm"){
            self.humidityHighAlarmLabel = UILabel()
            self.humidityHighAlarmLabel.textColor = UIColor.black
            self.humidityHighAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.humidityHighAlarmLabel.numberOfLines = 0;
            self.humidityHighAlarmLabel.text = NSLocalizedString("humidity_high_alarm_desc", comment: "Humidity high alarm:")
            self.humidityHighAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.humidityHighAlarmLabel.numberOfLines = 0;
            self.humidityHighAlarmLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.humidityHighAlarmLabel)
            self.humidityHighAlarmContentLabel = UILabel()
            self.humidityHighAlarmContentLabel.textColor = UIColor.black
            self.humidityHighAlarmContentLabel.text = ""
            self.humidityHighAlarmContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
            scrollView.addSubview(self.humidityHighAlarmContentLabel)
            self.editHumidityHighAlarmBtn = QMUIGhostButton()
            self.editHumidityHighAlarmBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
            self.editHumidityHighAlarmBtn.ghostColor = UIColor.colorPrimary
            self.editHumidityHighAlarmBtn.frame = CGRect(x: btnX, y: btnY, width: 60, height: btnHeight)
            self.editHumidityHighAlarmBtn.addTarget(self, action: #selector(editHumidityAlarm), for:.touchUpInside)
            scrollView.addSubview(self.editHumidityHighAlarmBtn)
            let humidityHighAlarmLine = UIView()
            humidityHighAlarmLine.backgroundColor = UIColor.gray
            humidityHighAlarmLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(humidityHighAlarmLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
            self.humidityLowAlarmLabel = UILabel()
            self.humidityLowAlarmLabel.textColor = UIColor.black
            self.humidityLowAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.humidityLowAlarmLabel.numberOfLines = 0;
            self.humidityLowAlarmLabel.text = NSLocalizedString("humidity_low_alarm_desc", comment: "Humidity low alarm:")
            self.humidityLowAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.humidityLowAlarmLabel.numberOfLines = 0;
            self.humidityLowAlarmLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.humidityLowAlarmLabel)
            self.humidityLowAlarmContentLabel = UILabel()
            self.humidityLowAlarmContentLabel.textColor = UIColor.black
            self.humidityLowAlarmContentLabel.text = ""
            self.humidityLowAlarmContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 70, height: 60)
            scrollView.addSubview(self.humidityLowAlarmContentLabel)
            self.editHumidityLowAlarmBtn = QMUIGhostButton()
            self.editHumidityLowAlarmBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
            self.editHumidityLowAlarmBtn.ghostColor = UIColor.colorPrimary
            self.editHumidityLowAlarmBtn.frame = CGRect(x: btnX, y: btnY, width: 60, height: btnHeight)
            self.editHumidityLowAlarmBtn.addTarget(self, action: #selector(editHumidityAlarm), for:.touchUpInside)
            scrollView.addSubview(self.editHumidityLowAlarmBtn)
            let humidityLowAlarmLine = UIView()
            humidityLowAlarmLine.backgroundColor = UIColor.gray
            humidityLowAlarmLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(humidityLowAlarmLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        
        if(self.isCurrentDeviceTypeFunc(funcName: "startRecord")){
            self.recordControlLabel = UILabel()
            self.recordControlLabel.textColor = UIColor.black
            self.recordControlLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.recordControlLabel.numberOfLines = 0;
            self.recordControlLabel.text = NSLocalizedString("record_control_desc", comment: "Record control:")
            self.recordControlLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.recordControlLabel.numberOfLines = 0;
            self.recordControlLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.recordControlLabel)
            self.startRecordBtn = QMUIGhostButton()
            self.startRecordBtn.setTitle(NSLocalizedString("start_record", comment: "Start Record"), for: .normal)
            self.startRecordBtn.ghostColor = UIColor.colorPrimary
            self.startRecordBtn.frame = CGRect(x: contentX - 20, y: btnY, width: 120, height: btnHeight)
            self.startRecordBtn.addTarget(self, action: #selector(startRecordClick), for:.touchUpInside)
            scrollView.addSubview(self.startRecordBtn)
            self.stopRecordBtn = QMUIGhostButton()
            self.stopRecordBtn.setTitle(NSLocalizedString("stop_record", comment: "Stop Record"), for: .normal)
            self.stopRecordBtn.ghostColor = UIColor.colorPrimary
            self.stopRecordBtn.frame = CGRect(x: contentX - 20, y: btnY, width: 120, height: btnHeight)
            self.stopRecordBtn.addTarget(self, action: #selector(stopRecordClick), for:.touchUpInside)
            self.stopRecordBtn.isHidden = true
            scrollView.addSubview(self.stopRecordBtn)
            let saveCountLine = UIView()
            saveCountLine.backgroundColor = UIColor.gray
            saveCountLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(saveCountLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        if self.isCurrentDeviceTypeFunc(funcName: "saveCount"){
            self.saveCountLabel = UILabel()
            self.saveCountLabel.textColor = UIColor.black
            self.saveCountLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.saveCountLabel.numberOfLines = 0;
            self.saveCountLabel.text = NSLocalizedString("save_count_desc", comment: "Save count:")
            self.saveCountLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth - 10, height: 60)
            scrollView.addSubview(self.saveCountLabel)
            self.saveCountContentLabel = UILabel()
            self.saveCountContentLabel.textColor = UIColor.black
            self.saveCountContentLabel.text = ""
            self.saveCountContentLabel.frame = CGRect(x: contentX, y: startLabelY, width: 50, height: 60)
            scrollView.addSubview(self.saveCountContentLabel)
            self.saveCountRefreshBtn = QMUIGhostButton()
            self.saveCountRefreshBtn.setTitle(NSLocalizedString("refresh", comment:"Refresh"), for: .normal)
            self.saveCountRefreshBtn.ghostColor = UIColor.colorPrimary
            self.saveCountRefreshBtn.frame = CGRect(x: contentX + 53, y: btnY, width: 77, height: btnHeight)
            self.saveCountRefreshBtn.addTarget(self, action: #selector(saveCountRefreshClick), for:.touchUpInside)
            scrollView.addSubview(self.saveCountRefreshBtn)
            self.saveCountReadBtn = QMUIGhostButton()
            self.saveCountReadBtn.setTitle(NSLocalizedString("read", comment:"Read"), for: .normal)
            self.saveCountReadBtn.ghostColor = UIColor.colorPrimary
            self.saveCountReadBtn.frame = CGRect(x: btnX+28, y: btnY, width: 58, height: btnHeight)
            self.saveCountReadBtn.addTarget(self, action: #selector(readHistoryClick), for:.touchUpInside)
            scrollView.addSubview(self.saveCountReadBtn)
            let saveCountLine = UIView()
            saveCountLine.backgroundColor = UIColor.gray
            saveCountLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(saveCountLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        if(self.isCurrentDeviceTypeFunc(funcName: "startRecord")){
            self.clearRecordLabel = UILabel()
            self.clearRecordLabel.textColor = UIColor.black
            self.clearRecordLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.clearRecordLabel.numberOfLines = 0;
            self.clearRecordLabel.text = NSLocalizedString("clear_cache_desc", comment:"Clear cache:")
            self.clearRecordLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.clearRecordLabel.numberOfLines = 0;
            self.clearRecordLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.clearRecordLabel)
            self.clearRecordBtn = QMUIGhostButton()
            self.clearRecordBtn.setTitle(NSLocalizedString("clear_record", comment:"Clear record"), for: .normal)
            self.clearRecordBtn.ghostColor = UIColor.colorPrimary
            self.clearRecordBtn.frame = CGRect(x: contentX + 15, y: btnY, width: 120, height: btnHeight)
            self.clearRecordBtn.addTarget(self, action: #selector(clearRecordClick), for:.touchUpInside)
            scrollView.addSubview(self.clearRecordBtn)
            let saveCountLine = UIView()
            saveCountLine.backgroundColor = UIColor.gray
            saveCountLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(saveCountLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        if self.isCurrentDeviceTypeFunc(funcName: "relay"){
            self.relayLabel = UILabel()
            self.relayLabel.textColor = UIColor.black
            self.relayLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.relayLabel.numberOfLines = 0;
            self.relayLabel.text = NSLocalizedString("relay_desc", comment:"Relay:")
            self.relayLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.relayLabel)
            self.relaySwitch = UISwitch()
            self.relaySwitch.frame = CGRect(x: contentX, y: btnY - 5, width: 70, height: 60)
            self.relaySwitch.addTarget(self, action: #selector(relaySwitchAction),
                                       for:UIControl.Event.valueChanged)
            scrollView.addSubview(self.relaySwitch)
            self.relayStatusLabel = UILabel()
            self.relayStatusLabel.textColor = UIColor.black
            self.relayStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.relayStatusLabel.numberOfLines = 0;
            self.relayStatusLabel.frame = CGRect(x: btnX, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.relayStatusLabel)
            let relayLine = UIView()
            relayLine.backgroundColor = UIColor.gray
            relayLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(relayLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        if self.isCurrentDeviceTypeFunc(funcName: "ledOpen"){
            self.ledLabel = UILabel()
            self.ledLabel.textColor = UIColor.black
            self.ledLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.ledLabel.numberOfLines = 0;
            self.ledLabel.text = NSLocalizedString("led_desc", comment:"Led:")
            self.ledLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.ledLabel)
            self.ledSwitch = UISwitch()
            self.ledSwitch.frame = CGRect(x: contentX, y: btnY - 5, width: 70, height: 60)
            self.ledSwitch.addTarget(self, action: #selector(ledSwitchAction),
                                     for:UIControl.Event.valueChanged)
            scrollView.addSubview(self.ledSwitch)
            let ledLine = UIView()
            ledLine.backgroundColor = UIColor.gray
            ledLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(ledLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        if self.isCurrentDeviceTypeFunc(funcName: "resetFactory"){
            self.resetFactoryLabel = UILabel()
            self.resetFactoryLabel.textColor = UIColor.black
            self.resetFactoryLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
            self.resetFactoryLabel.numberOfLines = 0;
            self.resetFactoryLabel.text =  NSLocalizedString("reset_factory_desc", comment:"Reset factory:")
            self.resetFactoryLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
            scrollView.addSubview(self.resetFactoryLabel)
            self.editResetFactoryBtn = QMUIGhostButton()
            self.editResetFactoryBtn.setTitle(NSLocalizedString("reset_factory", comment:"Reset factory"), for: .normal)
            self.editResetFactoryBtn.ghostColor = UIColor.colorPrimary
            self.editResetFactoryBtn.addTarget(self, action: #selector(resetFactoryClick), for:.touchUpInside)
            self.editResetFactoryBtn.frame = CGRect(x: contentX, y: btnY, width: 120, height: btnHeight)
            scrollView.addSubview(self.editResetFactoryBtn)
            let resetFactoryLine = UIView()
            resetFactoryLine.backgroundColor = UIColor.gray
            resetFactoryLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
            scrollView.addSubview(resetFactoryLine)
            startLabelY += lineHigh
            lineY += lineHigh
            btnY += lineHigh
        }
        scrollView.contentSize = CGSize(width: KSize.width, height: CGFloat(startLabelY + 10))
        self.centralManager = CBCentralManager(delegate: self, queue: nil)
        
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
    
    func showUpgradeWin(){
        if self.upgradeWarningView != nil && !self.upgradeWarningView.isDismiss{
            self.upgradeWarningView.show()
            return
        }
        self.upgradeWarningView = AEAlertView(style: .defaulted)
        self.upgradeWarningView.title = NSLocalizedString("upgrade", comment:"Upgrade")
        self.upgradeWarningView.message = NSLocalizedString("new_version_found_warning", comment:"New version found,updated?")
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.upgradeWarningView.dismiss()
            if self.initStart == false{
                self.showWaitingWin(title: NSLocalizedString("waiting", comment:"Waiting"))
                self.showPwdWin()
            }
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            self.isUpgrade = true
            self.upgradeWarningView.dismiss()
            self.showWaitingWin(title: NSLocalizedString("waiting", comment:"Waiting"))
            if self.foundDevice {
                self.doUpgrade()
            }
        }
        self.upgradeWarningView.addAction(action: upgradeCancel)
        self.upgradeWarningView.addAction(action: upgradeConfirm)
        self.upgradeWarningView.show()
    }
    
    func showPwdWin(){
        if self.pwdAlert != nil && !self.pwdAlert.isDismiss{
            self.pwdAlert.show()
            return
        }
        self.pwdAlert = AEUIAlertView(style: .password, title: NSLocalizedString("enter_pwd_warning", comment:"Please enter your password"), message: nil)
        self.pwdAlert.textField.placeholder = NSLocalizedString("password", comment:"Password")
        if(Utils.isDebug){
            self.pwdAlert.textField.text = "654321"
        }
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.pwdAlert.dismiss()
            self.waitingView.dismiss()
            self.navigationController?.popViewController(animated: true)
            
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let pwd = String(self.pwdAlert.textField.text ?? "")
            self.pwdErrorWarning = false;
            if pwd.count == 6{
                self.pwdAlert.dismiss()
                self.confirmPwd = pwd 
                if self.connected == true{
                    self.initStart = true
                    self.readBroadcastCycle()
                    self.readDeviceName()
                    self.readVersion()
                    self.readTransmittedPower()
                    self.readSaveCount()
                    self.readSaveInterval()
                    self.readHumidityAlarm()
                    self.readTempAlarm()
                    self.readLedOpenStatus()
                    self.readRelayStatus()
                }
            }else{
                Toast.hudBuilder.title(NSLocalizedString("pwd_value_error_warning", comment: "Value is incorrect!The length has to be 6 digits")).show()
            }
        }
        self.pwdAlert.addAction(action: action_one)
        self.pwdAlert.addAction(action: action_two)
        self.pwdAlert.show()
    }
    
    func isCurrentDeviceTypeFunc(funcName:String) -> Bool{
        if (self.deviceType == "S04"){
            if (funcName == "transmittedPower"){
                if(Int(software) ?? 0 >= 13){
                    return true;
                }else{
                    return false;
                }
            }
            if (funcName == "firmware" || funcName == "password" || funcName == "resetFactory" || funcName == "broadcastCycle"
                || funcName == "tempAlarm" || funcName == "ledOpen" || funcName == "deviceName" || funcName == "startRecord"
                || funcName == "stopRecord" || funcName == "clearRecord"
                || funcName == "saveCount" || funcName == "readAlarm" || funcName == "readOriginData") {
                if(Int(software) ?? 0 <= 10 && (funcName == "saveInterval" || funcName == "saveCount"
                    || funcName == "readAlarm" || funcName == "readOriginData" || funcName == "startRecord")){
                    return false;
                }
                return true;
            }else{
                return false;
            }
        }else  if (self.deviceType == "S05"){
            if (funcName == "transmittedPower"){
                if(Int(software) ?? 0 >= 13){
                    return true;
                }else{
                    return false;
                }
            }
            if (funcName == "firmware" || funcName == "password" || funcName == "resetFactory" || funcName == "broadcastCycle"
                || funcName == "tempAlarm" || funcName == "ledOpen" || funcName == "deviceName" || funcName == "relay") {
                return true;
            }else{
                return false;
            }
        }else{
            if (funcName == "transmittedPower"){
                if(Int(software) ?? 0 >= 13){
                    return true;
                }else{
                    return false;
                }
            }
            if(Int(software) ?? 0 <= 10 && (funcName == "saveInterval" || funcName == "readHistory"
                || funcName == "saveCount" || funcName == "readAlarm" || funcName == "readOriginData" || funcName == "startRecord")){
                return false;
            }
            if (funcName == "relay"){
                return false;
            }else{
                return true;
            }
        }
    }
}
extension EditConfigController:EditPwdDelegate{
    
    func setNewPwd(newPwd: String) {
        print("set new pwd")
        print(newPwd)
        self.newPwd = newPwd
        self.writePwd(pwd: newPwd)
        self.leaveViewNeedDisconnect = true
    }
}


extension EditConfigController:EditRangeValueDelegate{
    func setRangeValue(high: Int, low: Int) {
        self.leaveViewNeedDisconnect = true
        if self.editRangeValueType == "temp"{ 
            self.writeTempAlarmData(upValueInt: high, downValueInt: low)
        }else if self.editRangeValueType == "humidity"{
            self.writeHumidityAlarmData(upValue: high, downValue: low)
        }
    }
    
}

extension EditConfigController:HistorySelectDelegate{
    func setSelectDate(startDate: Date, endDate: Date) {
        self.startDate = startDate
        self.endDate = endDate
        if(self.deviceType == "S02"){
            self.historyIndex = 0
            self.originHistoryList.removeAll()
            self.showWaitingWin(title: NSLocalizedString("loading", comment: "Loading"))
            self.readHistory()
        }else{
            self.showWaitingWin(title: NSLocalizedString("loading", comment: "Loading"))
            self.readAlarm()
        }
    }
    
    
}

extension EditConfigController:SetConnectStatusDelegate{
    func setConnectStatus() {
        self.leaveViewNeedDisconnect = true
    }
}
