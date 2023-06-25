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
    private var isBetaUpgrade = false
    private var foundDevice = false
    private var connectControl = ""
    private var progressView:AEAlertView!
    private var progressBar:MyProgress!
    private var transmittedPowerList:[String] = ["4","0","-4","-8","-12","-16","-20"]
    private var dinStatusEventList:[String] = [String]()
    private var saveIntervalList:[String] = [String]()
    private var oneWireWorkModeList:[String] = [String]()
    private var portList:[String] = [String]()
    private var rs485BaudRateList:[String] = ["1200", "2400", "4800", "9600", "14400", "19200", "28800", "31250", "38400", "56000", "57600", "76800", "115200", "230400", "250000"]
    private var broadcastTypeList:[String] = ["Eddystone","Beacon"]
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
    ]
    private var fontSize:CGFloat = Utils.fontSize
    private var waitingView:AEUIAlertView!
    private var pwdAlert:AEUIAlertView!
    private var upgradeWarningView:AEAlertView!
    private var betaUpgradeWarningView:AEAlertView!
    private var hardware = ""
    var software = ""
    private var netSoftwareVersion = "0"
    private var upgradePackageLink = ""
    private var betaNetSoftwareVersion = "0"
    private var betaUpgradePackageLink = ""
    private var betaUpgradePackUrl = ""
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
    private var lightSensorOpen = false
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
    private var betaUpgradeBtn:QMUIGhostButton!
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
    private var readAlarmLabel:UILabel!
    private var readAlarmContentLabel:UILabel!
    private var readAlarmRefreshBtn:QMUIGhostButton!
    private var readAlarmReadBtn:QMUIGhostButton!
    private var editTransmittedPowerBtn:QMUIGhostButton!
    private var recordControlLabel:UILabel!
    private var clearRecordLabel:UILabel!
    private var startRecordBtn:QMUIGhostButton!
    private var stopRecordBtn:QMUIGhostButton!
    private var clearRecordBtn:QMUIGhostButton!
    
    private var saveIntervalLabel:UILabel!
    private var saveIntervalContentLabel:UILabel!
    private var editSaveIntervalBtn:QMUIGhostButton!
    private var dinVoltageLabel:UILabel!
    private var dinVoltageContentLabel:UILabel!
    private var dinStatusEventLabel:UILabel!
    private var dinStatusEventContentLabel:UILabel!
    private var dinStatusEventTypeLabel:UILabel!
    private var dinStatusEventTypeContentLabel:UILabel!
    private var doutStatusLabel:UILabel!
    private var ainVoltageLabel:UILabel!
    private var positiveNegativeWarningLabel:UILabel!
    private var oneWireDeviceLabel:UILabel!
    private var oneWireWorkModeLabel:UILabel!
    private var oneWireWorkModeContentLabel:UILabel!
    private var sendInstructionSequenceLabel:UILabel!
    private var sequentialLabel:UILabel!
    private var sequentialContentLabel:UILabel!
    private var sendDataLabel:UILabel!
    private var rs485BaudRateLabel:UILabel!
    private var rs485BaudRateContentLabel:UILabel!
    private var rs485EnableLabel:UILabel!
    private var rs485EnableStatusLabel:UILabel!
    private var broadcastTypeLabel:UILabel!
    private var broadcastTypeContentLabel:UILabel!
    private var gSensorLabel:UILabel!
    private var gSensorStatusLabel:UILabel!
    private var gSensorSensitivityLabel:UILabel!
    private var gSensorSensitivityContentLabel:UILabel!
    private var gSensorDetectionDurationLabel:UILabel!
    private var gSensorDetectionDurationContentLabel:UILabel!
    private var gSensorDetectionIntervalLabel:UILabel!
    private var gSensorDetectionIntervalContentLabel:UILabel!
    private var beaconMajorSetLabel:UILabel!
    private var beaconMajorSetContentLabel:UILabel!
    private var beaconMinorSetLabel:UILabel!
    private var beaconMinorSetContentLabel:UILabel!
    private var eddystoneNidSetLabel:UILabel!
    private var eddystoneNidSetContentLabel:UILabel!
    private var eddystoneBidSetLabel:UILabel!
    private var eddystoneBidSetContentLabel:UILabel!
    private var longRangeLabel:UILabel!
    private var doorLabel:UILabel!
    private var shutdownLabel:UILabel!
    
    
    private var editDinVoltageBtn:QMUIGhostButton!
    private var editDinStatusEventBtn:QMUIGhostButton!
    private var doutStatusBtn:QMUIGhostButton!
    private var ainVoltageBtn:QMUIGhostButton!
    private var positiveNegativeWarningBtn:QMUIGhostButton!
    private var oneWireDeviceBtn:QMUIGhostButton!
    private var editOneWireWorkModeBtn:QMUIGhostButton!
    private var sendInstructionSequenceBtn:QMUIGhostButton!
    private var editSequentialBtn:QMUIGhostButton!
    private var sendDataBtn:QMUIGhostButton!
    private var editRs485BaudRateBtn:QMUIGhostButton!
    
    private var editBroadcastTypeBtn:QMUIGhostButton!
    private var editGSensorDetectionDurationBtn:QMUIGhostButton!
    private var editGSensorDetectionIntervalBtn:QMUIGhostButton!
    private var editBeaconMajorSetBtn:QMUIGhostButton!
    private var editBeaconMinorSetBtn:QMUIGhostButton!
    private var editEddystoneNidSetBtn:QMUIGhostButton!
    private var editEddystoneBidSetBtn:QMUIGhostButton!
    private var editShutdownBtn:QMUIGhostButton!
    private var editGSensorSensitivityBtn:QMUIGhostButton!
    
    private var rs485EnableSwitch:UISwitch!
    private var longRangeSwitch:UISwitch!
    private var doorSwitch:UISwitch!
    private var gSensorSwitch:UISwitch!
    
    private var ledLabel:UILabel!
    private var ledSwitch:UISwitch!
    private var relayLabel:UILabel!
    private var relaySwitch:UISwitch!
    private var relayStatusLabel:UILabel!
    private var lightSensorOpenLabel:UILabel!
    private var lightSensorOpenSwitch:UISwitch!
    private var lightSensorOpenStatusLabel:UILabel!
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
    private let dateFormatter = DateFormatter()
    
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
        self.dinStatusEventList.append( NSLocalizedString("close", comment: "Close"))
        self.dinStatusEventList.append( NSLocalizedString("din_status_event_rising_edge", comment: "Rising edge"))
        self.dinStatusEventList.append( NSLocalizedString("din_status_event_falling_edge", comment: "Falling edge"))
        self.dinStatusEventList.append( NSLocalizedString("din_status_event_bilateral_margin", comment: "Bilateral margin"))
        self.oneWireWorkModeList.append( NSLocalizedString("close", comment: "Close"))
        self.oneWireWorkModeList.append( NSLocalizedString("conventional_pull_up", comment: "Conventional pull-up"))
        self.oneWireWorkModeList.append( NSLocalizedString("strong_pull_up", comment: "Strong pull-up"))
        self.portList.append(NSLocalizedString("port", comment: "Port") + " 0")
        self.portList.append(NSLocalizedString("port", comment: "Port") + " 1")
        self.portList.append(NSLocalizedString("port", comment: "Port") + " 2")
        if deviceType == "S07"{
            broadcastTypeList[0] = "Eddystone T-button"
            broadcastTypeList.append("Eddystone UID")
            transmittedPowerList.removeFirst()
        }else if deviceType == "S08" {
            broadcastTypeList[0] = "Eddystone T-sense"
            broadcastTypeList.append("Eddystone UID")
        }else if deviceType == "S10" {
            broadcastTypeList[0] = "Eddystone T-one"
            broadcastTypeList.append("Eddystone UID")
        }else if deviceType == "S02"{
            transmittedPowerList.removeFirst()
        }
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
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
        self.checkBetaUpdate()
    }
    
    @objc private func refreshClick() {
        print ("refresh click")
        self.initStart = false
        if self.connected == true{
            self.centralManager.cancelPeripheralConnection(self.selfPeripheral)
        }
        if self.selfPeripheral != nil{
            self.centralManager.connect(self.selfPeripheral)
        }
      
        self.notUpdateInit()
    }
    func checkBetaUpdate(){
            var deviceType = self.deviceType
            var curVersion = self.software.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
            curVersion = curVersion.replacingOccurrences(of: ".", with: "")
            var curVerionInt = Int(curVersion) ?? 0
            if (deviceType == "S02") && (curVerionInt <= 8){
                deviceType = "S01"
            }
         var debugStr = "0"
         if Utils.isDebug{
            debugStr = "1"
         }
         let url: NSURL = NSURL(string: "http://openapi.tftiot.com:8050/v1/sensor-upgrade-control-out?opr_type=getSensorBetaVersion&device_type=\(deviceType)&mac=\(mac)&is_debug=\(debugStr)")!
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
                        if code == 0{
                            let jsonData:NSDictionary = dict?["data"] as! NSDictionary
                            if jsonData != nil{
                                var version = jsonData["version"] as! String
                                var packageLink = jsonData["link"] as! String
                                self.betaUpgradePackUrl = NSHomeDirectory() + "/Documents/dfu_app_\(self.deviceType)_V\(self.betaNetSoftwareVersion).zip"
                                if version != nil && packageLink != nil && version.count > 0 && packageLink.count > 0{
                                    version = version.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
                                    version = version.replacingOccurrences(of: ".", with: "")
                                    var netVersionInt = Int(version) ?? 0
                                    self.betaNetSoftwareVersion = version
                                    self.betaUpgradePackageLink = packageLink
                                   if netVersionInt > curVerionInt {
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
        var deviceType = self.deviceType
        var curVersion = self.software.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
        curVersion = curVersion.replacingOccurrences(of: ".", with: "")
        var curVerionInt = Int(curVersion) ?? 0
        if (deviceType == "S02") && (curVerionInt <= 8){
            deviceType = "S01"
        }
        let url: NSURL = NSURL(string: "http://openapi.tftiot.com:8050/v1/sensor-upgrade-control-out?opr_type=getSensorVersion&device_type=\(deviceType)")!
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
                    if code == 0{
                        let jsonData:NSDictionary = dict?["data"] as! NSDictionary
                        if jsonData != nil{
                            var version = jsonData["version"] as! String
                            var packageLink = jsonData["link"] as! String
                            self.upgradePackUrl = NSHomeDirectory() + "/Documents/dfu_app_\(self.deviceType)_V\(self.netSoftwareVersion).zip"
                            if version != nil && packageLink != nil && version.count > 0 && packageLink.count > 0{
                                version = version.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
                                version = version.replacingOccurrences(of: ".", with: "")
                                var netVersionInt = Int(version) ?? 0
                                self.netSoftwareVersion = version
                                self.upgradePackageLink = packageLink
                                if !isEnterCheck{
                                    if netVersionInt != curVerionInt && netVersionInt != 0 && curVerionInt != 0 {
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
                    
                }else{
                    self.notUpdateInit()
                }
            }
        })
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
            if(self.isUpgrade){
                self.doUpgrade()
            }else{
                print("find device ,try to connect")
                if self.needConnect{
                    self.centralManager.connect(self.selfPeripheral)
                }
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
                        self.confirmPwd = self.newPwd ?? ""
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
                    }else if type == UInt8(self.controlFunc["lightSensorOpen"]?["read"] ?? 0) ||
                                type == UInt8(self.controlFunc["lightSensorOpen"]?["write"] ?? 0){
                        self.readLightSensorOpenStatusResp(resp: bytes)
                    }else if type == UInt8(self.controlFunc["resetFactory"]?["write"] ?? 0){
                        Toast.hudBuilder.title(NSLocalizedString("upgrade_success_warning",comment:"Factory Settings restored successfully, please enter the password to reconnect") ).show()
                        
                        self.showPwdWin()
                    }
                    else if(type == UInt8(self.controlFunc["readDinVoltage"]?["read"] ?? 0)){
                        readDinVoltageResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["dinStatusEvent"]?["read"] ?? 0)
                            || type == UInt8(self.controlFunc["dinStatusEvent"]?["write"] ?? 0)){
                        readDinStatusEventResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["readDinStatusEventType"]?["read"] ?? 0)){
                        readDinStatusEventTypeResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["doutStatus"]?["read"] ?? 0)){
                        readDoutStatusResp(resp: bytes)
                    }else if(type == UInt8(self.controlFunc["doutStatus"]?["write"] ?? 0)){
                        Toast.hudBuilder.title(NSLocalizedString("success",comment:"Success") ).show()
                    }
                    else if(type == UInt8(self.controlFunc["readAinVoltage"]?["read"] ?? 0)){
                        readAinVoltageResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["setPositiveNegativeWarning"]?["read"] ?? 0)){
                        readPositiveNegativeWarningResp(resp: bytes)
                    }else if(type == UInt8(self.controlFunc["setPositiveNegativeWarning"]?["write"] ?? 0)){
                        Toast.hudBuilder.title(NSLocalizedString("success",comment:"Success") ).show()
                    }
                    else if(type == UInt8(self.controlFunc["getAinEvent"]?["read"] ?? 0)){
                        readAinEventResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["getOneWireDevice"]?["read"] ?? 0)){
                        readOneWireDeviceResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["sendCmdSequence"]?["write"] ?? 0)){
                        readSendInstructionSequenceResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["oneWireWorkMode"]?["read"] ?? 0)
                            || type == UInt8(self.controlFunc["oneWireWorkMode"]?["write"] ?? 0)){
                        readOneWireWorkModeResp(resp: bytes)
                    }else if(type == UInt8(self.controlFunc["rs485SendData"]?["read"] ?? 0)){
                        readRS485SendDataResp(value: bytes)
                    }else if(type == UInt8(self.controlFunc["rs485SendData"]?["write"] ?? 0)){
                        Toast.hudBuilder.title(NSLocalizedString("success",comment:"Success") ).show()
                    }else if(type == UInt8(self.controlFunc["rs485BaudRate"]?["read"] ?? 0)
                             || type == UInt8(self.controlFunc["rs485BaudRate"]?["write"] ?? 0)){
                        readRs485BaudRateResp(resp: bytes)
                    } else if(type == UInt8(self.controlFunc["rs485Enable"]?["read"] ?? 0)
                              || type == UInt8(self.controlFunc["rs485Enable"]?["write"] ?? 0)){
                        readRs485EnableStatusResp(resp: bytes)
                    }else if(type == UInt8(self.controlFunc["longRangeEnable"]?["read"] ?? 0) ||
                             type == UInt8(self.controlFunc["longRangeEnable"]?["write"] ?? 0)){
                        readLongRangeEnableStatusResp(resp: bytes)
                    }else if(type == UInt8(self.controlFunc["broadcastType"]?["read"] ?? 0)
                             || type == UInt8(self.controlFunc["broadcastType"]?["write"] ?? 0)){
                        readBroadcastTypeResp(resp: bytes)
                    }else if(type == UInt8(self.controlFunc["gSensorEnable"]?["read"] ?? 0)
                             || type == UInt8(self.controlFunc["gSensorEnable"]?["write"] ?? 0)){
                        readGSensorEnableStatusResp(resp: bytes)
                    }else if(type == UInt8(self.controlFunc["shutdown"]?["write"] ?? 0)){
                        Toast.hudBuilder.title(NSLocalizedString("success",comment:"Success") ).show()
                    }else if(type == UInt8(self.controlFunc["readVinVoltage"]?["read"] ?? 0)){
                        readVinVoltageResp(resp: bytes)
                    }else if(type == UInt8(self.controlFunc["doorEnable"]?["read"] ?? 0)
                             || type == UInt8(self.controlFunc["doorEnable"]?["write"] ?? 0)){
                        readDoorEnableStatusResp(resp: bytes)
                    } else if(type == UInt8(self.controlFunc["gSensorSensitivity"]?["read"] ?? 0)
                              || type == UInt8(self.controlFunc["gSensorSensitivity"]?["write"] ?? 0)){
                        readGSensorSensitivityResp(resp: bytes)
                    }      else if(type == UInt8(self.controlFunc["gSensorDetectionDuration"]?["read"] ?? 0)
                                   || type == UInt8(self.controlFunc["gSensorDetectionDuration"]?["write"] ?? 0)){
                        readGSensorDetectionDurationResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["gSensorDetectionInterval"]?["read"] ?? 0)
                            || type == UInt8(self.controlFunc["gSensorDetectionInterval"]?["write"] ?? 0)){
                        readGSensorDetectionIntervalResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["beaconMajorSet"]?["read"] ?? 0)
                            || type == UInt8(self.controlFunc["beaconMajorSet"]?["write"] ?? 0)){
                        readBeaconMajorSetResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["beaconMinorSet"]?["read"] ?? 0)
                            || type == UInt8(self.controlFunc["beaconMinorSet"]?["write"] ?? 0)){
                        readBeaconMinorSetResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["eddystoneNIDSet"]?["read"] ?? 0)
                            || type == UInt8(self.controlFunc["eddystoneNIDSet"]?["write"] ?? 0)){
                        readEddystoneNidSetResp(resp: bytes)
                    }
                    else if(type == UInt8(self.controlFunc["eddystoneBIDSet"]?["read"] ?? 0)
                            || type == UInt8(self.controlFunc["eddystoneBIDSet"]?["write"] ?? 0)){
                        readEddystoneBidSetResp(resp: bytes)
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
    
    func readDinVoltageResp(resp:[UInt8]){
        self.dinVoltageContentLabel.text = String(format:"%d",resp[2])
    }
    
    
    func readDinStatusEventResp(resp:[UInt8]){
        self.dinStatusEventContentLabel.text = self.dinStatusEventList[Int(resp[2])]
    }
    
    
    
    func readDinStatusEventTypeResp(resp:[UInt8]){
        let date = Date()
        let dateString = dateFormatter.string(from: date)
        if resp[2] == 1{
            self.dinStatusEventTypeContentLabel.text = "0 -> 1 " + dateString
        }else if resp[2] == 2{
            self.dinStatusEventTypeContentLabel.text = "1 -> 0 " + dateString
        }
    }
    
    func readBroadcastCycleResp(resp:[UInt8]){
        if deviceType == "S02" || deviceType == "S04" || deviceType == "S05"{
            let broadcast = (Int(resp[2]) << 8) + Int(resp[3])
            self.broadcastCycleContentLabel.text = String(format:"%ds",broadcast)
        }else{
            let broadcast =  Int(resp[2])
            self.broadcastCycleContentLabel.text = String(format:"%ds",broadcast)
        }
        
    }
    var dout0:Int = -1
    var dout1:Int = -1
    func readDoutStatusResp(resp:[UInt8]){
        if resp[2] == 0{
            dout0 = Int(resp[3])
        }else if resp[2] == 1{
            dout1 = Int(resp[3])
        }
        if dout0 != -1 && dout1 != -1{ 
            let editForm = EditDoutOutputController()
            editForm.delegate = self
            editForm.connectStatusDelegate = self
            self.leaveViewNeedDisconnect = false
            editForm.dout0 = dout0
            editForm.dout1 = dout1
            self.navigationController?.pushViewController(editForm, animated: false)
        }
    }
    var ain1:Int = -1
    var ain2:Int = -1
    var ain3:Int = -1
    var vin:Int = -1
    func readAinVoltageResp(resp:[UInt8]){
        if resp[2] == 0{
            ain1 = Utils.bytes2Short(bytes: resp, offset: 3)
        }else if resp[2] == 1{
            ain2 = Utils.bytes2Short(bytes: resp, offset: 3)
        }else if resp[2] == 2{
            ain3 = Utils.bytes2Short(bytes: resp, offset: 3)
        }
        self.showAinVinDlg()
    }
    
    func showAinVinDlg(){
        if ain1 != -1 && ain2 != -1 && ain3 != -1 && vin != -1{
            var warnMsg = String(format: "VIN:%.2fV;\nAIN0:%.2fV;\r\n AIN1:%.2fV;\r\n AIN2:%.2fV; ",Float(vin) / 100.0,Float(ain1) / 100.0,Float(ain2) / 100.0,Float(ain3) / 100.0)
            let warningDlg = AEAlertView(style: .defaulted)
            warningDlg.message = warnMsg
            let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
                
                warningDlg.dismiss()
            }
            let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
                warningDlg.dismiss()
                
            }
            warningDlg.addAction(action: upgradeCancel)
            warningDlg.addAction(action: upgradeConfirm)
            warningDlg.show()
        }
    }
    
    func readPositiveNegativeWarningResp(resp:[UInt8]){
        let port: Int = Int(resp[2])
        let mode: Int  = Int(resp[3])
        let highVoltage = Utils.bytes2Short(bytes: resp,offset: 4)
        let lowVoltage = Utils.bytes2Short(bytes: resp,offset: 6)
        let samplingInterval = Utils.bytes2Short(bytes:resp,offset:8)
        let ditheringIntervalHigh: Int  = Int(resp[10] & 0xff)
        let ditheringIntervalLow: Int  = Int(resp[11] & 0xff)
        print("readPositiveNegativeWarningResp:\(highVoltage),\(lowVoltage),\(samplingInterval),\(ditheringIntervalHigh),\(ditheringIntervalLow)")
        let editForm = EditPositiveNegativeWarningController()
        editForm.delegate = self
        editForm.connectStatusDelegate = self
        self.leaveViewNeedDisconnect = false
        editForm.highVoltage = highVoltage
        editForm.lowVoltage = lowVoltage
        editForm.port = port
        editForm.mode = mode
        editForm.samplingInterval = samplingInterval
        editForm.ditheringIntervalHigh = ditheringIntervalHigh
        editForm.ditheringIntervalLow = ditheringIntervalLow
        self.navigationController?.pushViewController(editForm, animated: false)
    }
    func readAinEventResp(resp:[UInt8]){
        var warningMsg = ""
        var voltage = Utils.bytes2Short(bytes:resp,offset:4)
        warningMsg += String(format:"AIN%d:%.2fV;\r\n ",Int(resp[2]),Float(voltage) / 100.0)
        let date = Date()
        let dateString = dateFormatter.string(from: date)
        if resp[3] == 1{
            warningMsg += "0 -> 1 " + dateString
        }else{
            warningMsg += "1 -> 0 " + dateString
        }
        let warningDlg = AEAlertView(style: .defaulted)
        warningDlg.message = warningMsg
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            warningDlg.dismiss()
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            warningDlg.dismiss()
            
        }
        warningDlg.addAction(action: upgradeCancel)
        warningDlg.addAction(action: upgradeConfirm)
        warningDlg.show()
    }
    func readOneWireDeviceResp(resp:[UInt8]){
        let deviceCount = resp[2]
        var warningMsg = ""
        if deviceCount > 0{
            var i = 0
            while i < deviceCount{
                if(i * 8 + 8 + 3 >= resp.count){
                    break;
                }
                let curDevice: Array<UInt8>.SubSequence = resp[i * 8 + 3...i * 8 + 8 + 3]
                var deviceDesc = "ROM" + String(i + 1) + ":" + Utils.uint8ToHexStr(value: curDevice[0])
                warningMsg += deviceDesc
                if i != deviceCount - 1{
                    warningMsg += "\r\n"
                }
                i+=1
            }
        }else{
            warningMsg =  NSLocalizedString("no_device", comment: "No device")
        }
        
        let warningDlg = AEAlertView(style: .defaulted)
        warningDlg.message = warningMsg
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            warningDlg.dismiss()
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            warningDlg.dismiss()
            
        }
        warningDlg.addAction(action: upgradeCancel)
        warningDlg.addAction(action: upgradeConfirm)
        warningDlg.show()
    }
    func readSendInstructionSequenceResp(resp:[UInt8]){
        var warningMsg = ""
        if resp.count >= 4{
            let msgByte = resp[2..<resp.count - 2]
            var warningMsg = Utils.uint8ArrayToHexStr(value: Array(msgByte))
            let range = warningMsg.index(warningMsg.startIndex, offsetBy: 0)..<warningMsg.endIndex
            warningMsg = String(warningMsg[range])
        }else{
            warningMsg = "Error message"
        }
        let warningDlg = AEAlertView(style: .defaulted)
        warningDlg.message = warningMsg
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            warningDlg.dismiss()
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            warningDlg.dismiss()
            let pasteboard = UIPasteboard.general
            pasteboard.string = warningMsg
            Toast.hudBuilder.title(NSLocalizedString("copy_to_clipboard",comment:"Copied to clipboard") ).show()
        }
        warningDlg.addAction(action: upgradeCancel)
        warningDlg.addAction(action: upgradeConfirm)
        warningDlg.show()
    }
    func readOneWireWorkModeResp(resp:[UInt8]){
        let value = resp[2]
        if(value < self.oneWireWorkModeList.count){
            self.oneWireWorkModeContentLabel.text = self.oneWireWorkModeList[Int(value)]
        }
    }
    
    private var rs485DataErrorCount: Int = 0
    private var lastCheckRs485DataTime:Date!
    private var lastRs485DataErrorShow:Date!
    func readRS485SendDataResp(value:[UInt8]){
        var warningMsg = ""
        let status = value[2]
        let warningDlg = AEAlertView(style: .defaulted)
        var isErrorMsg: Bool = false
        if(status == 0){
            let isUseHex = UserDefaults.standard.bool(forKey: "rs485_send_data_use_hex")
            if(value.count > 3){
                let content: Array<UInt8>.SubSequence = value[3...value.count - 1]
                if(isUseHex){
                    warningMsg = Utils.uint8ToHexStr(value: content[0])
                }else{
                    warningMsg = String(bytes: content, encoding: .utf8)!
                }
            }else{
                warningMsg = "Error msg"
            }
            rs485DataErrorCount = 0
        }else{
            isErrorMsg = true
            if((status & 0x01) == 0x01){
                warningMsg = "Receive buffer overflow"
            }else if((status & 0x02) == 0x02){
                warningMsg = "Overrun error"
            }else if((status & 0x04) == 0x04){
                warningMsg = "Parity error"
            }else if((status & 0x08) == 0x08){
                warningMsg = "Framing error occurred"
            }else if((status & 0x10) == 0x10){
                warningMsg = "Break condition"
            }else{
                warningMsg = "Unknown error"
            }
            if((status & 0x04) == 0x04 || (status & 0x08) == 0x08 || (status & 0x10) == 0x10){
                if(lastCheckRs485DataTime == nil){
                    lastCheckRs485DataTime = Date()
                    rs485DataErrorCount = 1;
                }else{
                    let now = Date()
                    if(now.timeIntervalSince1970 - lastCheckRs485DataTime.timeIntervalSince1970 > 3000){
                        rs485DataErrorCount = 1;
                        lastCheckRs485DataTime = Date()
                    }else{
                        rs485DataErrorCount += 1
                    }
                }
                if(rs485DataErrorCount > 6){
                    Toast.hudBuilder.title(NSLocalizedString("rs485_baud_set_error_warning",comment:"RS485 baud rate may be set incorrectly, please check") ).show()
                }
            }
            
        }
        if(isErrorMsg){
            if(lastRs485DataErrorShow == nil){
                lastRs485DataErrorShow = Date()
            }else{
                let now = Date()
                if(now.timeIntervalSince1970 - lastRs485DataErrorShow.timeIntervalSince1970 > 3000){
                    lastRs485DataErrorShow = Date()
                }else{
                    return
                }
            }
        }
        
        warningDlg.message = warningMsg
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            
            warningDlg.dismiss()
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            warningDlg.dismiss()
            let pasteboard = UIPasteboard.general
            pasteboard.string = warningMsg
            Toast.hudBuilder.title(NSLocalizedString("copy_to_clipboard",comment:"Copied to clipboard") ).show()
        }
        warningDlg.addAction(action: upgradeCancel)
        warningDlg.addAction(action: upgradeConfirm)
        warningDlg.show()
    }
    
    
    func readRs485BaudRateResp(resp:[UInt8]){
        self.rs485BaudRateContentLabel.text = String(format:"%d",Utils.bytes2Integer(buf: resp, pos: 2))
    }
    func readRs485EnableStatusResp(resp:[UInt8]){
        if resp[2] == 0{
            rs485EnableSwitch.isOn = false
        }else{
            rs485EnableSwitch.isOn = true
        }
    }
    func readLongRangeEnableStatusResp(resp:[UInt8]){
        if resp[2] == 0{
            longRangeSwitch.isOn = false
        }else{
            longRangeSwitch.isOn = true
        }
    }
    func readBroadcastTypeResp(resp:[UInt8]){
        let value = Int(resp[2])
        if value < self.broadcastTypeList.count {
            self.broadcastTypeContentLabel.text = self.broadcastTypeList[value]
            if self.deviceType == "S07"{
                if value == 0{
                    self.longRangeView.isHidden = true
                }else{
                    self.longRangeView.isHidden = false
                }
            }
            self.beaconMajorSetView.isHidden = true
            self.beaconMinorSetView.isHidden = true
            self.eddystoneNidSetView.isHidden = true
            self.eddystoneBidSetView.isHidden = true
            if self.deviceType == "S07" || self.deviceType == "S08" || self.deviceType == "S10"{
                if value == 1{
                    self.beaconMajorSetView.isHidden = false
                    self.beaconMinorSetView.isHidden = false
                    self.readBeaconMajorSet()
                    self.readBeaconMinorSet()
                }
                if value == 2{
                    self.eddystoneNidSetView.isHidden = false
                    self.eddystoneBidSetView.isHidden = false
                    self.readEddystoneBidSet()
                    self.readEddystoneNidSet()
                }
            }
        }
    }
    func readGSensorEnableStatusResp(resp:[UInt8]){
        self.gSensorDetectionDurationView.isHidden = true
        self.gSensorSensitivityView.isHidden = true
        self.gSensorDetectionIntervalView.isHidden = true
        if resp[2] == 0{
            self.gSensorSwitch.isOn = false
        }else{
            self.gSensorSwitch.isOn = true
            if deviceType == "S08"{
                self.gSensorDetectionDurationView.isHidden = false
                self.gSensorSensitivityView.isHidden = false
                self.gSensorDetectionIntervalView.isHidden = false
                self.readGSensorSensitivity()
                self.readGSensorDetectionDuration()
                self.readGSensorDetectionInterval()
            }
        }
    }
    func readVinVoltageResp(resp:[UInt8]){
        vin = Utils.bytes2Short(bytes: resp, offset: 2)
        showAinVinDlg()
    }
    func readDoorEnableStatusResp(resp:[UInt8]){
        if resp[2] == 0{
            doorSwitch.isOn = false
        }else{
            doorSwitch.isOn = true
        }
    }
    func readGSensorSensitivityResp(resp:[UInt8]){
        self.gSensorSensitivityContentLabel.text = String(format:"%d",Int(resp[2]))
    }
    func readGSensorDetectionDurationResp(resp:[UInt8]){
        self.gSensorDetectionDurationContentLabel.text = String(format:"%d",Int(resp[2]))
    }
    func readGSensorDetectionIntervalResp(resp:[UInt8]){
        self.gSensorDetectionIntervalContentLabel.text = String(format:"%d",Int(resp[2]))
    }
    func readBeaconMajorSetResp(resp:[UInt8]){
        self.beaconMajorSetContentLabel.text = String(format:"%d",Utils.bytes2Short(bytes: resp, offset: 2))
    }
    func readBeaconMinorSetResp(resp:[UInt8]){
        self.beaconMinorSetContentLabel.text = String(format:"%d",Utils.bytes2Short(bytes: resp, offset: 2))
    }
    func readEddystoneNidSetResp(resp:[UInt8]){
        var data = resp[2...resp.count - 2]
        if data.count > 0{
            self.eddystoneNidSetContentLabel.text = "0x" + Utils.uint8ArrayToHexStr(value: Array(data))
        }
    }
    func readEddystoneBidSetResp(resp:[UInt8]){
        var data = resp[2...resp.count - 2]
        if data.count > 0{
            self.eddystoneBidSetContentLabel.text = "0x" + Utils.uint8ArrayToHexStr(value: Array(data))
        }
        
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
        self.saveCountContentLabel.text = String(self.saveCount)
        if(self.saveCount == 0){
            self.saveCountReadBtn.isHidden = true
        }
        self.readAlarmContentLabel.text = String(self.saveAlarmCount)
        if(self.saveAlarmCount == 0){
            self.readAlarmReadBtn.isHidden = true
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
        if resp.count == 6{
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
            self.softwareContentLabel.text = String.init(format:"V%d",resp[4])
        }else if resp.count == 7 || resp.count == 8{
            if resp[2] == 0x07{
                self.modelContentLabel.text = "T-button"
            }else if resp[2] == 0x08{
                self.modelContentLabel.text = "T-sense"
            }else if resp[2] == 0x09{
                self.modelContentLabel.text = "T-hub"
            }else if resp[2] == 0x0a{
                self.modelContentLabel.text = "T-one"
            }
            let hardware = Utils.parseHardwareVersion(hardware: Utils.uint8ToHexStr(value: resp[3]))
            self.hardwareContentLabel.text = hardware
            let software = Utils.parseS78910SoftwaeVersion(data: resp, index: 4)
            self.software = software.replacingOccurrences(of: ".", with: "")
            self.softwareContentLabel.text = software
        }
        print("readVersionResp:\(self.netSoftwareVersion),\(self.software)")
        if self.software != self.netSoftwareVersion && self.netSoftwareVersion != "0"{
            self.editSoftwareBtn.isHidden = false
        }else{
            self.editSoftwareBtn.isHidden = true
        }
        var netVersionInt = Int(self.betaNetSoftwareVersion) ?? 0
        var curVersion = self.software.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "")
        curVersion = curVersion.replacingOccurrences(of: ".", with: "")
        var curVerionInt = Int(curVersion) ?? 0
        if netVersionInt > curVerionInt {
           self.betaUpgradeBtn.isHidden = false
        }else{
           self.betaUpgradeBtn.isHidden = true
        }
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
    
    func readDinStatusEvent(){
        if !self.isCurrentDeviceTypeFunc(funcName: "dinStatusEvent"){
            return
        }
        self.readData(cmdHead: self.controlFunc["dinStatusEvent"]?["read"] ?? 0)
    }
    func readDinVoltage(){
        if !self.isCurrentDeviceTypeFunc(funcName: "readDinVoltage"){
            return
        }
        self.readData(cmdHead: self.controlFunc["readDinVoltage"]?["read"] ?? 0)
    }
    
    func readDoutStatus(port:Int){
        if !self.isCurrentDeviceTypeFunc(funcName: "doutStatus"){
            return
        }
        var uint8Value = port < 0 ? port + 256 : port
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["doutStatus"]?["read"] ?? 0, content: data)
    }
    
    func writeDoutStatus(port:Int,value:Int){
        var uint8Port = port < 0 ? port + 256 : port
        var unit8Value = value < 0 ? value + 256 : value
        let data:[UInt8] = [UInt8(uint8Port),UInt8(unit8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["doutStatus"]?["write"] ?? 0, content: data)
    }
    
    func readAinStatus(port:Int){
        if !self.isCurrentDeviceTypeFunc(funcName: "readAinVoltage"){
            return
        }
        var uint8Value = port < 0 ? port + 256 : port
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["readAinVoltage"]?["read"] ?? 0, content: data)
    }
    
    func readPositiveNegativeWarning(port:Int){
        if !self.isCurrentDeviceTypeFunc(funcName: "setPositiveNegativeWarning"){
            return
        }
        var uint8Value = port < 0 ? port + 256 : port
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["setPositiveNegativeWarning"]?["read"] ?? 0, content: data)
    }
    
    func writePositiveNegativeWaning(port:Int,mode:Int,highVoltage:Int,lowVoltage:Int,samplingInterval:Int,ditheringIntervalHigh:Int,ditheringIntervalLow:Int){
        var data = [UInt8]()
        var uint8Port = port < 0 ? port + 256 : port
        var uint8Mode = mode < 0 ? mode + 256 : mode
        data.append(UInt8(uint8Port))
        data.append(UInt8(uint8Mode))
        var highVoltageData:[UInt8] = self.formatHex(intValue: highVoltage, len: 2)
        for byte in highVoltageData{
            data.append(byte)
        }
        var lowVoltageData:[UInt8] = self.formatHex(intValue: lowVoltage, len: 2)
        for byte in lowVoltageData{
            data.append(byte)
        }
        var samplingIntervalData:[UInt8] = self.formatHex(intValue: samplingInterval, len: 2)
        for byte in samplingIntervalData{
            data.append(byte)
        }
        var uint8DitheringIntervalHigh = ditheringIntervalHigh < 0 ? ditheringIntervalHigh + 256 : ditheringIntervalHigh
        var uint8DitheringIntervalLow = ditheringIntervalLow < 0 ? ditheringIntervalLow + 256 : ditheringIntervalLow
        data.append(UInt8(uint8DitheringIntervalHigh))
        data.append(UInt8(uint8DitheringIntervalLow))
        self.writeArrayData(cmdHead: self.controlFunc["setPositiveNegativeWarning"]?["write"] ?? 0, content: data)
    }
    
    func readOneWireDevice(){
        if !self.isCurrentDeviceTypeFunc(funcName: "getOneWireDevice"){
            return
        }
        self.readData(cmdHead: self.controlFunc["getOneWireDevice"]?["read"] ?? 0)
    }
    
    func writeSendInstructionSequence(cmd:String){
        let data = Utils.hexStringToUInt8Array(cmd)
        self.writeArrayData(cmdHead: self.controlFunc["sendCmdSequence"]?["write"] ?? 0, content: data)
    }
    
    
    func readOneWireWorkMode(){
        if !self.isCurrentDeviceTypeFunc(funcName: "oneWireWorkMode"){
            return
        }
        self.readData(cmdHead: self.controlFunc["oneWireWorkMode"]?["read"] ?? 0)
    }
    
    func writeOneWireWorkMode(value:Int){
        var uint8Value = value < 0 ? value + 256 : value
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["oneWireWorkMode"]?["write"] ?? 0, content: data)
    }
    
    func writeRS485SendData(cmd:String){
        let data = Utils.hexStringToUInt8Array(cmd)
        self.writeArrayData(cmdHead: self.controlFunc["rs485SendData"]?["write"] ?? 0, content: data)
    }
    
    func readRs485BaudRate(){
        if !self.isCurrentDeviceTypeFunc(funcName: "rs485BaudRate"){
            return
        }
        self.readData(cmdHead: self.controlFunc["rs485BaudRate"]?["read"] ?? 0)
    }
    
    func writeRs485BaudRate(value:Int){
        let data:[UInt8] = Utils.IntToBytes(intValue: value)
        self.writeArrayData(cmdHead: self.controlFunc["rs485BaudRate"]?["write"] ?? 0, content: data)
    }
    func readRs485Enable(){
        if !self.isCurrentDeviceTypeFunc(funcName: "rs485Enable"){
            return
        }
        self.readData(cmdHead: self.controlFunc["rs485Enable"]?["read"] ?? 0)
    }
    
    func writeRs485EnableStatus(){
        var data:[UInt8]
        if rs485EnableSwitch.isOn {
            data = [1]
        }else{
            data = [0]
        }
        self.writeArrayData(cmdHead: self.controlFunc["rs485Enable"]?["write"] ?? 0, content: data)
    }
    
    func readLongRangeEnable(){
        if !self.isCurrentDeviceTypeFunc(funcName: "longRangeEnable"){
            return
        }
        self.readData(cmdHead: self.controlFunc["longRangeEnable"]?["read"] ?? 0)
    }
    
    func writeLongRangeEnableStatus(){
        var data:[UInt8]
        if longRangeSwitch.isOn {
            data = [1]
        }else{
            data = [0]
        }
        self.writeArrayData(cmdHead: self.controlFunc["longRangeEnable"]?["write"] ?? 0, content: data)
    }
    
    func readBroadcastType(){
        if !self.isCurrentDeviceTypeFunc(funcName: "broadcastType"){
            return
        }
        self.readData(cmdHead: self.controlFunc["broadcastType"]?["read"] ?? 0)
    }
    
    func writeBroadcastType(value:Int){
        var uint8Value = value < 0 ? value + 256 : value
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["broadcastType"]?["write"] ?? 0, content: data)
    }
    
    
    func readGSensorEnable(){
        if !self.isCurrentDeviceTypeFunc(funcName: "gSensorEnable"){
            return
        }
        self.readData(cmdHead: self.controlFunc["gSensorEnable"]?["read"] ?? 0)
    }
    
    func writeGSensorEnableStatus(){
        var data:[UInt8]
        if gSensorSwitch.isOn {
            data = [1]
        }else{
            data = [0]
        }
        self.writeArrayData(cmdHead: self.controlFunc["gSensorEnable"]?["write"] ?? 0, content: data)
    }
    
    func writeShutdown(){
        self.writeArrayData(cmdHead: self.controlFunc["shutdown"]?["write"] ?? 0, content: [UInt8]())
    }
    
    func readVinVoltage(){
        if !self.isCurrentDeviceTypeFunc(funcName: "readVinVoltage"){
            return
        }
        self.readData(cmdHead: self.controlFunc["readVinVoltage"]?["read"] ?? 0)
    }
    
    func readDoorEnableStatus(){
        if !self.isCurrentDeviceTypeFunc(funcName: "doorEnable"){
            return
        }
        self.readData(cmdHead: self.controlFunc["doorEnable"]?["read"] ?? 0)
    }
    
    func writeDoorEnableStatus(){
        var data:[UInt8]
        if doorSwitch.isOn {
            data = [1]
        }else{
            data = [0]
        }
        self.writeArrayData(cmdHead: self.controlFunc["doorEnable"]?["write"] ?? 0, content: data)
    }
    
    func readGSensorSensitivity(){
        if !self.isCurrentDeviceTypeFunc(funcName: "gSensorSensitivity"){
            return
        }
        self.readData(cmdHead: self.controlFunc["gSensorSensitivity"]?["read"] ?? 0)
    }
    
    func writeGSensorSensitivity(value:Int){
        var uint8Value = value < 0 ? value + 256 : value
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["gSensorSensitivity"]?["write"] ?? 0, content: data)
    }
    
    func readGSensorDetectionDuration(){
        if !self.isCurrentDeviceTypeFunc(funcName: "gSensorDetectionDuration"){
            return
        }
        self.readData(cmdHead: self.controlFunc["gSensorDetectionDuration"]?["read"] ?? 0)
    }
    
    func writeGSensorDetectionDuration(value:Int){
        var uint8Value = value < 0 ? value + 256 : value
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["gSensorDetectionDuration"]?["write"] ?? 0, content: data)
    }
    
    func readGSensorDetectionInterval(){
        if !self.isCurrentDeviceTypeFunc(funcName: "gSensorDetectionInterval"){
            return
        }
        self.readData(cmdHead: self.controlFunc["gSensorDetectionInterval"]?["read"] ?? 0)
    }
    
    func writeGSensorDetectionInterval(value:Int){
        var uint8Value = value < 0 ? value + 256 : value
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["gSensorDetectionInterval"]?["write"] ?? 0, content: data)
    }
    
    func readBeaconMajorSet(){
        if !self.isCurrentDeviceTypeFunc(funcName: "beaconMajorSet"){
            return
        }
        self.readData(cmdHead: self.controlFunc["beaconMajorSet"]?["read"] ?? 0)
    }
    func readBeaconMinorSet(){
        if !self.isCurrentDeviceTypeFunc(funcName: "beaconMinorSet"){
            return
        }
        self.readData(cmdHead: self.controlFunc["beaconMinorSet"]?["read"] ?? 0)
    }
    func readEddystoneNidSet(){
        if !self.isCurrentDeviceTypeFunc(funcName: "eddystoneNIDSet"){
            return
        }
        self.readData(cmdHead: self.controlFunc["eddystoneNIDSet"]?["read"] ?? 0)
    }
    func readEddystoneBidSet(){
        if !self.isCurrentDeviceTypeFunc(funcName: "eddystoneBIDSet"){
            return
        }
        self.readData(cmdHead: self.controlFunc["eddystoneBIDSet"]?["read"] ?? 0)
    }
    
    
    func writeBeaconMajorSet(value:Int){
        self.writeArrayData(cmdHead: self.controlFunc["beaconMajorSet"]?["write"] ?? 0, content: self.formatHex(intValue: value, len: 2))
    }
    
    func writeBeaconMinorSet(value:Int){
        self.writeArrayData(cmdHead: self.controlFunc["beaconMinorSet"]?["write"] ?? 0, content: self.formatHex(intValue: value, len: 2))
    }
    
    func writeEddystoneNidSet(value:String){
        self.writeArrayData(cmdHead: self.controlFunc["eddystoneNIDSet"]?["write"] ?? 0, content: Utils.hexStringToUInt8Array(value))
    }
    
    func writeEddystoneBidSet(value:String){
        self.writeArrayData(cmdHead: self.controlFunc["eddystoneBIDSet"]?["write"] ?? 0, content: Utils.hexStringToUInt8Array(value))
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
    
    func readLightSensorOpenStatus(){
        if !self.isCurrentDeviceTypeFunc(funcName: "lightSensorOpen"){
            return
        }
        self.readData(cmdHead: self.controlFunc["lightSensorOpen"]?["read"] ?? 0)
    }
    func readLightSensorOpenStatusResp(resp:[UInt8]){
        if resp[2] == 0{
            self.lightSensorOpen = false;
            self.lightSensorOpenSwitch.isOn = false
        }else{
            self.lightSensorOpen = true;
            self.lightSensorOpenSwitch.isOn = true
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
        if !self.isCurrentDeviceTypeFunc(funcName: "time"){
            return
        }
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
    func writeDinStatusEvent(value:Int){
        var uint8Value = value < 0 ? value + 256 : value
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["dinStatusEvent"]?["write"] ?? 0, content: data)
    }
    func writeTransmittedPower(value:Int){
        var uint8Value = value < 0 ? value + 256 : value
        let data:[UInt8] = [UInt8(uint8Value)]
        self.writeArrayData(cmdHead: self.controlFunc["transmittedPower"]?["write"] ?? 0, content: data)
    }
    
    func writeBroadcastData(value:Int){
        if deviceType == "S02" || deviceType == "S04" || deviceType == "S05" {
            let data = self.formatHex(intValue: value, len: 2)
            self.writeArrayData(cmdHead: self.controlFunc["broadcastCycle"]?["write"] ?? 0, content: data)
        }else{
            let data = [UInt8(value)]
            self.writeArrayData(cmdHead: self.controlFunc["broadcastCycle"]?["write"] ?? 0, content: data)
        }
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
    
    func writeLightSensorOpenStatus(){
        var data = [UInt8]()
        if self.lightSensorOpenSwitch.isOn {
            data.append(UInt8(1))
        }else{
            data.append(UInt8(0))
        }
        self.writeArrayData(cmdHead: self.controlFunc["lightSensorOpen"]?["write"] ?? 0, content: data)
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
                    self.readTransmittedPower()
                    self.readBroadcastCycle()
                    self.readDeviceName()
                    self.readVersion()
                    self.readHumidityAlarm()
                    self.readTempAlarm()
                    self.readLedOpenStatus()
                    self.readRelayStatus()
                    self.readSaveCount()
                    self.readSaveInterval()
                    self.readLightSensorOpenStatus()
                    self.readRs485Enable()
                    self.readOneWireWorkMode()
                    self.readRs485BaudRate()
                    self.readBroadcastType()
                    self.readLongRangeEnable()
                    self.readGSensorEnable()
                    self.readDinStatusEvent()
                    self.readDinVoltage( )
                    self.readDoorEnableStatus()
                    self.fixTime()
                }
            }else{
                notUpdateInit()
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
    
    @objc private func doutStatusClick() {
        dout0 = -1
        dout1 = -1
        self.readDoutStatus(port: 0)
        self.readDoutStatus(port: 1)
    }
    @objc private func ainVoltageClick() {
        ain1 = -1
        ain2 = -1
        ain3 = -1
        vin = -1
        self.readAinStatus(port: 0)
        self.readAinStatus(port: 1)
        self.readAinStatus(port: 2)
        self.readVinVoltage()
    }
    @objc private func positiveNegativeWarningClick(sender:UIButton) {
        ActionSheetStringPicker.show(withTitle: "", rows: self.portList, initialSelection:0, doneBlock: {
            picker, index, value in
            self.readPositiveNegativeWarning(port: index)
            return
        }, cancel: { ActionMultipleStringCancelBlock in return }, origin: sender)
        
    }
    @objc private func oneWireDeviceClick() {
        self.readOneWireDevice()
    }
    @objc private func editOneWireWorkModeClick(sender:UIButton) {
        var currentIndex = 0
        var patchStr = self.oneWireWorkModeContentLabel.text
        for var index in 0..<self.oneWireWorkModeList.count{
            if patchStr == self.oneWireWorkModeList[index]{
                currentIndex = index
                break
            }
        }
        ActionSheetStringPicker.show(withTitle: "", rows: self.oneWireWorkModeList, initialSelection:currentIndex, doneBlock: {
            picker, index, value in
            self.writeOneWireWorkMode(value: index)
            return
        }, cancel: { ActionMultipleStringCancelBlock in return }, origin: sender)
    }
    @objc private func sendInstructionSequenceClick() {
        let editForm = EditInstructionSequenceController()
        editForm.delegate = self
        editForm.connectStatusDelegate = self
        self.leaveViewNeedDisconnect = false
        
        self.navigationController?.pushViewController(editForm, animated: false)
    }
    @objc private func editSequentialClick() {
        //device now is not need this function
    }
    @objc private func sendDataClick() {
        let editForm = EditRS485CmdController()
        editForm.delegate = self
        editForm.connectStatusDelegate = self
        self.leaveViewNeedDisconnect = false
        
        self.navigationController?.pushViewController(editForm, animated: false)
    }
    
    
    @objc private func editRs485BaudRateClick(sender:UIButton) {
        var currentIndex = 0
        var patchStr = self.rs485BaudRateContentLabel.text
        for var index in 0..<self.rs485BaudRateList.count{
            if patchStr == self.rs485BaudRateList[index]{
                currentIndex = index
                break
            }
        }
        ActionSheetStringPicker.show(withTitle: "", rows: self.rs485BaudRateList, initialSelection:currentIndex, doneBlock: {
            picker, index, value in
            self.writeRs485BaudRate(value: Int(self.rs485BaudRateList[index]) ?? 0)
            return
        }, cancel: { ActionMultipleStringCancelBlock in return }, origin: sender)
    }
    @objc private func editBroadcastTypeClick(sender:UIButton) {
        print("editBroadcastTypeClick")
        var currentIndex = 0
        var patchStr = self.broadcastTypeContentLabel.text
        for index in 0..<self.broadcastTypeList.count{
            if patchStr == self.broadcastTypeList[index]{
                currentIndex = index
                break
            }
        }
        ActionSheetStringPicker.show(withTitle: "", rows: self.broadcastTypeList, initialSelection:currentIndex, doneBlock: {
            picker, index, value in
            self.writeBroadcastType(value: index)
            return
        }, cancel: { ActionMultipleStringCancelBlock in return }, origin: sender)
    }
    @objc private func editGSensorSensitivityClick() {
        print("editGSensorSensitivityClick")
        let editGSensorSensitivityAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("gsensor_sensitivity", comment: "Gsensor sensitivity"), message: nil)
        editGSensorSensitivityAlert.textField.placeholder = NSLocalizedString("gsensor_sensitivity", comment: "Gsensor sensitivity")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editGSensorSensitivityAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let saveGSensorSensitivityValue = String(editGSensorSensitivityAlert.textField.text ?? "")
            let numbCharacterSet = CharacterSet(charactersIn: "0123456789").inverted
            if saveGSensorSensitivityValue.rangeOfCharacter(from: numbCharacterSet) != nil {
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                return
            }
            if saveGSensorSensitivityValue.count > 0{
                var gSensorSensitivity:Int = Int(saveGSensorSensitivityValue) ?? 0
                if gSensorSensitivity >= 20 && gSensorSensitivity <= 64{
                    self.writeGSensorSensitivity(value: gSensorSensitivity)
                    editGSensorSensitivityAlert.dismiss()
                }else{
                    Toast.hudBuilder.title(NSLocalizedString("gsensor_sensitivity_error_warning", comment: "Value is incorrect!It must between 20 and 64.")).show()
                }
                
            }else{
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
            }
        }
        editGSensorSensitivityAlert.addAction(action: action_one)
        editGSensorSensitivityAlert.addAction(action: action_two)
        editGSensorSensitivityAlert.show()
    }
    @objc private func editGSensorDetectionDurationClick() {
        let editGSensorDetectionDurationAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("gsensor_detection_duration", comment: "Gsensor detection duration"), message: nil)
        editGSensorDetectionDurationAlert.textField.placeholder = NSLocalizedString("gsensor_detection_duration", comment: "Gsensor detection duration")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editGSensorDetectionDurationAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let saveGSensorDetectionDurationValue = String(editGSensorDetectionDurationAlert.textField.text ?? "")
            let numbCharacterSet = CharacterSet(charactersIn: "0123456789").inverted
            if saveGSensorDetectionDurationValue.rangeOfCharacter(from: numbCharacterSet) != nil {
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                return
            }
            if saveGSensorDetectionDurationValue.count > 0{
                var gSensorDetectionDuration:Int = Int(saveGSensorDetectionDurationValue) ?? 0
                if gSensorDetectionDuration >= 3 && gSensorDetectionDuration <= 10{
                    self.writeGSensorDetectionDuration(value: gSensorDetectionDuration)
                    editGSensorDetectionDurationAlert.dismiss()
                }else{
                    Toast.hudBuilder.title(NSLocalizedString("gsensor_detection_duration_error_warning", comment: "Value is incorrect!It must between 3 and 10.")).show()
                }
                
            }else{
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
            }
        }
        editGSensorDetectionDurationAlert.addAction(action: action_one)
        editGSensorDetectionDurationAlert.addAction(action: action_two)
        editGSensorDetectionDurationAlert.show()
    }
    @objc private func editGSensorDetectionIntervalClick() {
        let editGSensorDetectionIntervalAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("gsensor_detection_interval", comment: "Gsensor detection interval"), message: nil)
        editGSensorDetectionIntervalAlert.textField.placeholder = NSLocalizedString("gsensor_detection_interval", comment: "Gsensor detection interval")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editGSensorDetectionIntervalAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let saveGSensorDetectionIntervalValue = String(editGSensorDetectionIntervalAlert.textField.text ?? "")
            let numbCharacterSet = CharacterSet(charactersIn: "0123456789").inverted
            if saveGSensorDetectionIntervalValue.rangeOfCharacter(from: numbCharacterSet) != nil {
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                return
            }
            if saveGSensorDetectionIntervalValue.count > 0{
                var gSensorDetectionInterval:Int = Int(saveGSensorDetectionIntervalValue) ?? 0
                if gSensorDetectionInterval >= 2 && gSensorDetectionInterval <= 180{
                    self.writeGSensorDetectionInterval(value: gSensorDetectionInterval)
                    editGSensorDetectionIntervalAlert.dismiss()
                }else{
                    Toast.hudBuilder.title(NSLocalizedString("gsensor_detection_duration_error_warning", comment: "Value is incorrect!It must between 2 and 180.")).show()
                }
                
            }else{
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
            }
        }
        editGSensorDetectionIntervalAlert.addAction(action: action_one)
        editGSensorDetectionIntervalAlert.addAction(action: action_two)
        editGSensorDetectionIntervalAlert.show()
    }
    @objc private func editBeaconMajorSetClick() {
        let editBeaconMajorSetAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("beacon_major_set", comment: "Beacon major set"), message: nil)
        editBeaconMajorSetAlert.textField.placeholder = NSLocalizedString("beacon_major_set", comment: "Beacon major set")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editBeaconMajorSetAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let saveBeaconMajorSetValue = String(editBeaconMajorSetAlert.textField.text ?? "")
            let numbCharacterSet = CharacterSet(charactersIn: "0123456789").inverted
            if saveBeaconMajorSetValue.rangeOfCharacter(from: numbCharacterSet) != nil {
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                return
            }
            if saveBeaconMajorSetValue.count > 0{
                var beaconMajorSet:Int = Int(saveBeaconMajorSetValue) ?? 0
                if beaconMajorSet >= 0 && beaconMajorSet <= 65535{
                    self.writeBeaconMajorSet(value: beaconMajorSet)
                    editBeaconMajorSetAlert.dismiss()
                }else{
                    Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                }
                
            }else{
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
            }
        }
        editBeaconMajorSetAlert.addAction(action: action_one)
        editBeaconMajorSetAlert.addAction(action: action_two)
        editBeaconMajorSetAlert.show()
    }
    @objc private func editBeaconMinorSetClick() {
        let editBeaconMinorSetAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("beacon_minor_set", comment: "Beacon minor set"), message: nil)
        editBeaconMinorSetAlert.textField.placeholder = NSLocalizedString("beacon_minor_set", comment: "Beacon minor set")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editBeaconMinorSetAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            let saveBeaconMinorSetValue = String(editBeaconMinorSetAlert.textField.text ?? "")
            let numbCharacterSet = CharacterSet(charactersIn: "0123456789").inverted
            if saveBeaconMinorSetValue.rangeOfCharacter(from: numbCharacterSet) != nil {
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                return
            }
            if saveBeaconMinorSetValue.count > 0{
                var beaconMinorSet:Int = Int(saveBeaconMinorSetValue) ?? 0
                if beaconMinorSet >= 0 && beaconMinorSet <= 65535{
                    self.writeBeaconMinorSet(value: beaconMinorSet)
                    editBeaconMinorSetAlert.dismiss()
                }else{
                    Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                }
                
            }else{
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
            }
        }
        editBeaconMinorSetAlert.addAction(action: action_one)
        editBeaconMinorSetAlert.addAction(action: action_two)
        editBeaconMinorSetAlert.show()
    }
    @objc private func editEddystoneNidSetClick() {
        let editEddystoneNidSetAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("eddystone_nid_set", comment: "Name space ID"), message: nil)
        editEddystoneNidSetAlert.textField.placeholder = NSLocalizedString("eddystone_nid_set", comment: "Name space ID")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editEddystoneNidSetAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            var saveEddystoneNidSetValue = String(editEddystoneNidSetAlert.textField.text ?? "")
            saveEddystoneNidSetValue = saveEddystoneNidSetValue.replacingOccurrences(of: "0x", with: "")
            saveEddystoneNidSetValue = saveEddystoneNidSetValue.replacingOccurrences(of: "0X", with: "")
            saveEddystoneNidSetValue = saveEddystoneNidSetValue.replacingOccurrences(of: " ", with: "")
            let hexLowercaseCharacterSet = CharacterSet(charactersIn: "0123456789abcdef").inverted
            let hexUppercaseCharacterSet = CharacterSet(charactersIn: "0123456789ABCDEF").inverted
            if saveEddystoneNidSetValue.rangeOfCharacter(from: hexLowercaseCharacterSet) == nil && saveEddystoneNidSetValue.rangeOfCharacter(from: hexUppercaseCharacterSet) == nil {
                print("All characters are in 0 to f or 0 to F.")
            } else {
                print("Not all characters are in 0 to f or 0 to F.")
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                return
            }
            if saveEddystoneNidSetValue.count == 20{
                self.writeEddystoneNidSet(value: saveEddystoneNidSetValue)
                editEddystoneNidSetAlert.dismiss()
                
            }else{
                Toast.hudBuilder.title(NSLocalizedString("eddystone_nid_set_len_error", comment: "The length must be 10 characters")).show()
            }
        }
        editEddystoneNidSetAlert.addAction(action: action_one)
        editEddystoneNidSetAlert.addAction(action: action_two)
        editEddystoneNidSetAlert.show()
    }
    @objc private func editEddystoneBidSetClick() {
        let editEddystoneBidSetAlert = AEUIAlertView(style: .textField, title: NSLocalizedString("eddystone_bid_set", comment: "Instance ID"), message: nil)
        editEddystoneBidSetAlert.textField.placeholder = NSLocalizedString("eddystone_bid_set", comment: "Instance ID")
        
        let action_one = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            editEddystoneBidSetAlert.dismiss()
        }
        let action_two = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            var saveEddystoneBidSetValue = String(editEddystoneBidSetAlert.textField.text ?? "")
            saveEddystoneBidSetValue = saveEddystoneBidSetValue.replacingOccurrences(of: "0x", with: "")
            saveEddystoneBidSetValue = saveEddystoneBidSetValue.replacingOccurrences(of: "0X", with: "")
            saveEddystoneBidSetValue = saveEddystoneBidSetValue.replacingOccurrences(of: " ", with: "")
            let hexLowercaseCharacterSet = CharacterSet(charactersIn: "0123456789abcdef").inverted
            let hexUppercaseCharacterSet = CharacterSet(charactersIn: "0123456789ABCDEF").inverted
            if saveEddystoneBidSetValue.rangeOfCharacter(from: hexLowercaseCharacterSet) == nil && saveEddystoneBidSetValue.rangeOfCharacter(from: hexUppercaseCharacterSet) == nil {
                print("All characters are in 0 to f or 0 to F.")
            } else {
                print("Not all characters are in 0 to f or 0 to F.")
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                return
            }
            if saveEddystoneBidSetValue.count == 12{
                self.writeEddystoneBidSet(value: saveEddystoneBidSetValue)
                editEddystoneBidSetAlert.dismiss()
                
            }else{
                Toast.hudBuilder.title(NSLocalizedString("eddystone_bid_set_len_error", comment: "The length must be 6 characters")).show()
            }
        }
        editEddystoneBidSetAlert.addAction(action: action_one)
        editEddystoneBidSetAlert.addAction(action: action_two)
        editEddystoneBidSetAlert.show()
    }
    @objc private func shutdownClick() {
        let shutdownWarningView = AEAlertView(style: .defaulted)
        shutdownWarningView.message = NSLocalizedString("confirm_shutdown_warning", comment: "Confirm shutdown?")
        let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
            self.relaySwitch.isOn = self.relayStatus
            shutdownWarningView.dismiss()
        }
        let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
            shutdownWarningView.dismiss()
            self.writeShutdown()
        }
        shutdownWarningView.addAction(action: upgradeCancel)
        shutdownWarningView.addAction(action: upgradeConfirm)
        shutdownWarningView.show()
    }
    @objc func ledSwitchAction(senger:UISwitch){
        print("ledSwitchAction")
        self.writeLedOpenStatus()
    }
    @objc func lightSeosorOpenSwitchAction(senger:UISwitch){
        print("lightSeosorOpenSwitchAction")
        self.writeLightSensorOpenStatus()
    }
    
    @objc func rs485EnableSwitchAction(senger:UISwitch){
        print("rs485EnableSwitchAction")
        self.writeRs485EnableStatus()
        
    }
    
    @objc func gSensorSwitchAction(senger:UISwitch){
        print("gSensorSwitchAction")
        self.writeGSensorEnableStatus()
        
    }
    @objc func longRangeSwitchAction(senger:UISwitch){
        print("longRangeSwitchAction")
        self.writeLongRangeEnableStatus()
        
    }
    @objc func doorSwitchAction(senger:UISwitch){
        print("doorSwitchAction")
        self.writeDoorEnableStatus()
        
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
    
    @objc func betaUpgradeClick(){
        if self.betaUpgradeWarningView != nil && !self.betaUpgradeWarningView.isDismiss{
                   self.betaUpgradeWarningView.show()
                   return
               }
               self.betaUpgradeWarningView = AEAlertView(style: .defaulted)
               self.betaUpgradeWarningView.title = NSLocalizedString("betaUpgrade", comment:"Beta upgrade")
               self.betaUpgradeWarningView.message = NSLocalizedString("new_beta_version_found_warning", comment:"New beta version found,updated?")
               let upgradeCancel = AEAlertAction(title: NSLocalizedString("cancel", comment: "Cancel"), style: .cancel) { (action) in
                   self.betaUpgradeWarningView.dismiss()
                   if self.initStart == false{
                       self.showWaitingWin(title: NSLocalizedString("waiting", comment:"Waiting"))
                       self.showPwdWin()
                   }
               }
               let upgradeConfirm = AEAlertAction(title: NSLocalizedString("confirm", comment: "Confirm"), style: .defaulted) { (action) in
                   self.isUpgrade = true
                   self.betaUpgradeWarningView.dismiss()
                   self.showWaitingWin(title: NSLocalizedString("waiting", comment:"Waiting"))
                   if self.foundDevice {
                       self.doBetaUpgrade()
                   }
               }
               self.betaUpgradeWarningView.addAction(action: upgradeCancel)
               self.betaUpgradeWarningView.addAction(action: upgradeConfirm)
               self.betaUpgradeWarningView.show()
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
    
    @objc func saveCountRefreshClick(){
        self.showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
        self.readSaveCount()
    }
    var isReadHis:Bool = false
    @objc func readHistoryClick(){
        let editView = HistorySelectController()
        self.leaveViewNeedDisconnect = false
        editView.setDateDelegate = self
        self.isReadHis = true
        self.navigationController?.pushViewController(editView, animated: false)
    }
    @objc func readAlarmClick(){
        let editView = HistorySelectController()
        self.leaveViewNeedDisconnect = false
        editView.setDateDelegate = self
        self.isReadHis = false
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
        self.isBetaUpgrade = false
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
    func doBetaUpgrade(){
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
            self.isBetaUpgrade = true
            if FileTool.fileExists(filePath: self.betaUpgradePackUrl){
                FileTool.removeFile(self.betaUpgradePackUrl)
            }
            self.showWaitingWin(title: NSLocalizedString("waiting", comment: "Waiting"))
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
        //输出下载文件原来的存放目录
        print("location:\(location)")
        //location位置转换
        let locationPath = location.path
        //拷贝到用户目录
        //创建文件管理器
        let fileManager = FileManager.default
        if self.isBetaUpgrade{
            try! fileManager.moveItem(atPath: locationPath, toPath: self.betaUpgradePackUrl)
            print("new location:\(self.betaUpgradePackUrl)")
        }else{
            try! fileManager.moveItem(atPath: locationPath, toPath: self.upgradePackUrl)
            print("new location:\(self.upgradePackUrl)")
        }
      
        self.upgradeDevice()
    }
    
    func upgradeDevice(){
        self.waitingView.dismiss()
        var url = URL(string:self.upgradePackUrl)
        if self.isBetaUpgrade{
            url = URL(string:self.betaUpgradePackUrl)
        }
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
    
    @objc private func editDinVoltage(sender:UIButton){
        self.readDinVoltage()
    }
    @objc private func editDinStatusEvent(sender:UIButton){
        var currentIndex = 0
        for var index in 0..<self.dinStatusEventList.count{
            if self.dinStatusEventContentLabel.text == self.dinStatusEventList[index]{
                currentIndex = index
                break
            }
        }
        
        ActionSheetStringPicker.show(withTitle: "", rows: self.dinStatusEventList, initialSelection:currentIndex, doneBlock: {
            picker, index, value in
            self.writeDinStatusEvent(value: index)
            return
        }, cancel: { ActionMultipleStringCancelBlock in return }, origin: sender)
    }
    
    
    var nameView:UIView!
    var modelView:UIView!
    var hardwareView:UIView!
    var softwareView:UIView!
    var passwordView:UIView!
    var transmittedPowerView:UIView!
    var broadcastCycleView:UIView!
    var saveIntervalView:UIView!
    var tempHighAlarmView:UIView!
    var tempLowAlarmView:UIView!
    var humidityHighAlarmView:UIView!
    var humidityLowAlarmView:UIView!
    var clearRecordView:UIView!
    var saveCountView:UIView!
    var relayView:UIView!
    var ledView:UIView!
    var lightSensorOpenView:UIView!
    var resetFactoryView:UIView!
    var recordControlView:UIView!
    var dinVoltageView:UIView!
    var dinStatusEventView:UIView!
    var dinStatusView:UIView!
    var doutStatusView:UIView!
    var ainVoltageView:UIView!
    var positiveNegativeWarningView:UIView!
    var oneWireDeviceView:UIView!
    var oneWireWorkModeView:UIView!
    var sendInstructionSequenceView:UIView!
    var sequentialView:UIView!
    var sendDataView:UIView!
    var rs485BaudRateView:UIView!
    var rs485EnableView:UIView!
    var broadcastTypeView:UIView!
    var gSensorView:UIView!
    var gSensorSensitivityView:UIView!
    var gSensorDetectionDurationView:UIView!
    var gSensorDetectionIntervalView:UIView!
    var beaconMajorSetView:UIView!
    var beaconMinorSetView:UIView!
    var eddystoneNidSetView:UIView!
    var eddystoneBidSetView:UIView!
    var longRangeView:UIView!
    var doorView:UIView!
    var shutdownView:UIView!
    var readAlarmView:UIView!
    func initUI(){
        let scrollView = UIScrollView()
        scrollView.frame = self.view.bounds
        var scrollViewHeight:Float = 910
        if self.deviceType != "S02"{
            scrollViewHeight = 730
        }
        
        
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
        
        nameView = UIView()
        nameView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        nameView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(nameView)
        
        self.nameLabel = UILabel()
        self.nameLabel.textColor = UIColor.black
        self.nameLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.nameLabel.text = NSLocalizedString("name_desc", comment: "Name:")
        self.nameLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.nameLabel.numberOfLines = 0;
        self.nameLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        nameView.addSubview(self.nameLabel)
        self.nameContentLabel = UILabel()
        self.nameContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.nameContentLabel.textColor = UIColor.black
        self.nameContentLabel.text = ""
        self.nameContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        nameView.addSubview(self.nameContentLabel)
        self.editNameBtn = QMUIGhostButton()
        self.editNameBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editNameBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editNameBtn.ghostColor = UIColor.colorPrimary
        self.editNameBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editNameBtn.isUserInteractionEnabled  = true
        self.editNameBtn.addTarget(self, action: #selector(editDeviceName), for:.touchUpInside)
        nameView.isUserInteractionEnabled  = true
        nameView.addSubview(self.editNameBtn)
        
        let nameLine = UIView()
        nameLine.backgroundColor = UIColor.gray
        nameLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        nameView.addSubview(nameLine)
        
        modelView = UIView()
        modelView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        modelView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(modelView)
        
        self.modelLabel = UILabel()
        self.modelLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.modelLabel.textColor = UIColor.black
        self.modelLabel.text = NSLocalizedString("device_model", comment: "Device model:")
        self.modelLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.modelLabel.numberOfLines = 0;
        self.modelLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        modelView.addSubview(self.modelLabel)
        self.modelContentLabel = UILabel()
        self.modelContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.modelContentLabel.text = ""
        self.modelContentLabel.textColor = UIColor.black
        self.modelContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        modelView.addSubview(self.modelContentLabel)
        let modelLine = UIView()
        modelLine.backgroundColor = UIColor.gray
        modelLine.frame =  CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        modelView.addSubview(modelLine)
        self.debugUpgradeBtn = QMUIGhostButton()
        self.debugUpgradeBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.debugUpgradeBtn.setTitle(NSLocalizedString("debugUpgrade", comment: "Debug Upgrade"), for: .normal)
        self.debugUpgradeBtn.ghostColor = UIColor.colorPrimary
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
        self.hardwareLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.hardwareLabel.text = NSLocalizedString("hardware", comment: "Hardware:")
        self.hardwareLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.hardwareLabel.numberOfLines = 0;
        self.hardwareLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        hardwareView.addSubview(self.hardwareLabel)
        self.hardwareContentLabel = UILabel()
        self.hardwareContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.hardwareContentLabel.textColor = UIColor.black
        self.hardwareContentLabel.text = ""
        self.hardwareContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        hardwareView.addSubview(self.hardwareContentLabel)
        self.betaUpgradeBtn = QMUIGhostButton()
        self.betaUpgradeBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.betaUpgradeBtn.setTitle(NSLocalizedString("betaUpgrade", comment: "Beta Upgrade"), for: .normal)
        self.betaUpgradeBtn.ghostColor = UIColor.colorPrimary
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
        self.softwareLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.softwareLabel.textColor = UIColor.black
        self.softwareLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.softwareLabel.numberOfLines = 0;
        self.softwareLabel.text = NSLocalizedString("software", comment: "Software:")
        self.softwareLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        softwareView.addSubview(self.softwareLabel)
        self.softwareContentLabel = UILabel()
        self.softwareContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.softwareContentLabel.textColor = UIColor.black
        self.softwareContentLabel.text = ""
        self.softwareContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        softwareView.addSubview(self.softwareContentLabel)
        self.editSoftwareBtn = QMUIGhostButton()
        self.editSoftwareBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editSoftwareBtn.setTitle(NSLocalizedString("upgrade", comment: "Upgrade"), for: .normal)
        self.editSoftwareBtn.ghostColor = UIColor.colorPrimary
        self.editSoftwareBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editSoftwareBtn.isHidden = true
        self.editSoftwareBtn.addTarget(self, action: #selector(upgradeClick), for:.touchUpInside)
        softwareView.addSubview(self.editSoftwareBtn)
        let softwareLine = UIView()
        softwareLine.backgroundColor = UIColor.gray
        softwareLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        softwareView.addSubview(softwareLine)
        
        
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
        self.passwordContentLabel = UILabel()
        self.passwordContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.passwordContentLabel.textColor = UIColor.black
        self.passwordContentLabel.text = "******"
        self.passwordContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        passwordView.addSubview(self.passwordContentLabel)
        self.editPasswordBtn = QMUIGhostButton()
        self.editPasswordBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editPasswordBtn.setTitle(NSLocalizedString(NSLocalizedString("edit", comment: "Edit"), comment: "Edit"), for: .normal)
        self.editPasswordBtn.ghostColor = UIColor.colorPrimary
        self.editPasswordBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editPasswordBtn.addTarget(self, action: #selector(editPassword), for:.touchUpInside)
        passwordView.addSubview(self.editPasswordBtn)
        let passwordLine = UIView()
        passwordLine.backgroundColor = UIColor.gray
        passwordLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        passwordView.addSubview(passwordLine)
        
        
        dinVoltageView = UIView()
        dinVoltageView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        dinVoltageView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(dinVoltageView)
        self.dinVoltageLabel = UILabel()
        self.dinVoltageLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.dinVoltageLabel.textColor = UIColor.black
        self.dinVoltageLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dinVoltageLabel.numberOfLines = 0;
        self.dinVoltageLabel.text = NSLocalizedString("din_voltage_desc", comment: "Din Level:")
        self.dinVoltageLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dinVoltageLabel.numberOfLines = 0;
        self.dinVoltageLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        dinVoltageView.addSubview(self.dinVoltageLabel)
        self.dinVoltageContentLabel = UILabel()
        self.dinVoltageContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.dinVoltageContentLabel.textColor = UIColor.black
        self.dinVoltageContentLabel.text = ""
        self.dinVoltageContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        dinVoltageView.addSubview(self.dinVoltageContentLabel)
        self.editDinVoltageBtn = QMUIGhostButton()
        self.editDinVoltageBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editDinVoltageBtn.setTitle(NSLocalizedString("refresh", comment: "Refresh"), for: .normal)
        self.editDinVoltageBtn.ghostColor = UIColor.colorPrimary
        self.editDinVoltageBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editDinVoltageBtn.addTarget(self, action: #selector(editDinVoltage), for:.touchUpInside)
        dinVoltageView.addSubview(self.editDinVoltageBtn)
        let dinVoltageLine = UIView()
        dinVoltageLine.backgroundColor = UIColor.gray
        dinVoltageLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        dinVoltageView.addSubview(dinVoltageLine)
        if self.isCurrentDeviceTypeFunc(funcName: "readDinVoltage"){
            dinVoltageView.isHidden = false
        }else{
            dinVoltageView.isHidden = true
        }
        dinStatusEventView = UIView()
        dinStatusEventView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        dinStatusEventView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(dinStatusEventView)
        self.dinStatusEventLabel = UILabel()
        self.dinStatusEventLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.dinStatusEventLabel.textColor = UIColor.black
        self.dinStatusEventLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dinStatusEventLabel.numberOfLines = 0;
        self.dinStatusEventLabel.text = NSLocalizedString("din_status_event_desc", comment: "Din event reporting mode:")
        self.dinStatusEventLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        dinStatusEventView.addSubview(self.dinStatusEventLabel)
        self.dinStatusEventContentLabel = UILabel()
        self.dinStatusEventContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.dinStatusEventContentLabel.textColor = UIColor.black
        self.dinStatusEventContentLabel.text = ""
        self.dinStatusEventContentLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dinStatusEventContentLabel.numberOfLines = 0;
        self.dinStatusEventContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        dinStatusEventView.addSubview(self.dinStatusEventContentLabel)
        self.editDinStatusEventBtn = QMUIGhostButton()
        self.editDinStatusEventBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editDinStatusEventBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editDinStatusEventBtn.ghostColor = UIColor.colorPrimary
        self.editDinStatusEventBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editDinStatusEventBtn.addTarget(self, action: #selector(editDinStatusEvent), for:.touchUpInside)
        dinStatusEventView.addSubview(self.editDinStatusEventBtn)
        let dinStatusEventLine = UIView()
        dinStatusEventLine.backgroundColor = UIColor.gray
        dinStatusEventLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        dinStatusEventView.addSubview(dinStatusEventLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "dinStatusEvent"){
            dinStatusEventView.isHidden = false
        }else{
            dinStatusEventView.isHidden = true
        }
        
        dinStatusView = UIView()
        dinStatusView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        dinStatusView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(dinStatusView)
        self.dinStatusEventTypeLabel = UILabel()
        self.dinStatusEventTypeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.dinStatusEventTypeLabel.textColor = UIColor.black
        self.dinStatusEventTypeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dinStatusEventTypeLabel.numberOfLines = 0;
        self.dinStatusEventTypeLabel.text = NSLocalizedString("din_status_event_type_desc", comment: "Din event report:")
        self.dinStatusEventTypeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dinStatusEventTypeLabel.numberOfLines = 0;
        self.dinStatusEventTypeLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        dinStatusView.addSubview(self.dinStatusEventTypeLabel)
        self.dinStatusEventTypeContentLabel = UILabel()
        self.dinStatusEventTypeContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.dinStatusEventTypeContentLabel.textColor = UIColor.black
        self.dinStatusEventTypeContentLabel.text = ""
        self.dinStatusEventTypeContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        dinStatusView.addSubview(self.dinStatusEventTypeContentLabel)
        
        let dinStatusEventTypeLine = UIView()
        dinStatusEventTypeLine.backgroundColor = UIColor.gray
        dinStatusEventTypeLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        dinStatusView.addSubview(dinStatusEventTypeLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "readDinStatusEventType"){
            dinStatusView.isHidden = false
        }else{
            dinStatusView.isHidden = true
        }
        
        doutStatusView = UIView()
        doutStatusView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        doutStatusView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(doutStatusView)
        self.doutStatusLabel = UILabel()
        self.doutStatusLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.doutStatusLabel.textColor = UIColor.black
        self.doutStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.doutStatusLabel.numberOfLines = 0;
        self.doutStatusLabel.text = NSLocalizedString("dout_status_desc", comment: "Dout status:")
        self.doutStatusLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        doutStatusView.addSubview(self.doutStatusLabel)
        self.doutStatusBtn = QMUIGhostButton()
        self.doutStatusBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.doutStatusBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.doutStatusBtn.ghostColor = UIColor.colorPrimary
        self.doutStatusBtn.frame = CGRect(x: contentX , y: 15, width: 100, height: btnHeight)
        self.doutStatusBtn.addTarget(self, action: #selector(doutStatusClick), for:.touchUpInside)
        doutStatusView.addSubview(self.doutStatusBtn)
        let doutStatusLine = UIView()
        doutStatusLine.backgroundColor = UIColor.gray
        doutStatusLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        doutStatusView.addSubview(doutStatusLine)
        if(self.isCurrentDeviceTypeFunc(funcName: "doutStatus")){
            doutStatusView.isHidden = false
        }else{
            doutStatusView.isHidden = true
        }
        
        ainVoltageView = UIView()
        ainVoltageView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        ainVoltageView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(ainVoltageView)
        self.ainVoltageLabel = UILabel()
        self.ainVoltageLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.ainVoltageLabel.textColor = UIColor.black
        self.ainVoltageLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.ainVoltageLabel.numberOfLines = 0;
        self.ainVoltageLabel.text = NSLocalizedString("ain_voltage_desc", comment: "Ain voltage:")
        self.ainVoltageLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        ainVoltageView.addSubview(self.ainVoltageLabel)
        self.ainVoltageBtn = QMUIGhostButton()
        self.ainVoltageBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.ainVoltageBtn.setTitle(NSLocalizedString("show", comment: "Show"), for: .normal)
        self.ainVoltageBtn.ghostColor = UIColor.colorPrimary
        self.ainVoltageBtn.frame = CGRect(x: contentX , y: 15, width: 100, height: btnHeight)
        self.ainVoltageBtn.addTarget(self, action: #selector(ainVoltageClick), for:.touchUpInside)
        ainVoltageView.addSubview(self.ainVoltageBtn)
        let ainVoltageLine = UIView()
        ainVoltageLine.backgroundColor = UIColor.gray
        ainVoltageLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        ainVoltageView.addSubview(ainVoltageLine)
        if(self.isCurrentDeviceTypeFunc(funcName: "readAinVoltage")){
            ainVoltageView.isHidden = false
        }else{
            ainVoltageView.isHidden = true
        }
        
        positiveNegativeWarningView = UIView()
        positiveNegativeWarningView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        positiveNegativeWarningView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(positiveNegativeWarningView)
        self.positiveNegativeWarningLabel = UILabel()
        self.positiveNegativeWarningLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.positiveNegativeWarningLabel.textColor = UIColor.black
        self.positiveNegativeWarningLabel.text = NSLocalizedString("positive_negative_warning_desc", comment: "Ain event reporting mode:")
        self.positiveNegativeWarningLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.positiveNegativeWarningLabel.numberOfLines = 0;
        self.positiveNegativeWarningLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        positiveNegativeWarningView.addSubview(self.positiveNegativeWarningLabel)
        self.positiveNegativeWarningBtn = QMUIGhostButton()
        self.positiveNegativeWarningBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.positiveNegativeWarningBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.positiveNegativeWarningBtn.ghostColor = UIColor.colorPrimary
        self.positiveNegativeWarningBtn.frame = CGRect(x: contentX , y: 15, width: 100, height: btnHeight)
        self.positiveNegativeWarningBtn.addTarget(self, action: #selector(positiveNegativeWarningClick), for:.touchUpInside)
        positiveNegativeWarningView.addSubview(self.positiveNegativeWarningBtn)
        let positiveNegativeWarningLine = UIView()
        positiveNegativeWarningLine.backgroundColor = UIColor.gray
        positiveNegativeWarningLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        positiveNegativeWarningView.addSubview(positiveNegativeWarningLine)
        if(self.isCurrentDeviceTypeFunc(funcName: "setPositiveNegativeWarning")){
            positiveNegativeWarningView.isHidden = false
        }else{
            positiveNegativeWarningView.isHidden = true
        }
        oneWireDeviceView = UIView()
        oneWireDeviceView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        oneWireDeviceView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(oneWireDeviceView)
        self.oneWireDeviceLabel = UILabel()
        self.oneWireDeviceLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.oneWireDeviceLabel.textColor = UIColor.black
        self.oneWireDeviceLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.oneWireDeviceLabel.numberOfLines = 0;
        self.oneWireDeviceLabel.text = NSLocalizedString("one_wire_device_desc", comment: "1-wire device:")
        self.oneWireDeviceLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        oneWireDeviceView.addSubview(self.oneWireDeviceLabel)
        self.oneWireDeviceBtn = QMUIGhostButton()
        self.oneWireDeviceBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.oneWireDeviceBtn.setTitle(NSLocalizedString("show", comment: "Show"), for: .normal)
        self.oneWireDeviceBtn.ghostColor = UIColor.colorPrimary
        self.oneWireDeviceBtn.frame = CGRect(x: contentX , y: 15, width: 100, height: btnHeight)
        self.oneWireDeviceBtn.addTarget(self, action: #selector(oneWireDeviceClick), for:.touchUpInside)
        oneWireDeviceView.addSubview(self.oneWireDeviceBtn)
        let oneWireDeviceLine = UIView()
        oneWireDeviceLine.backgroundColor = UIColor.gray
        oneWireDeviceLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        oneWireDeviceView.addSubview(oneWireDeviceLine)
        if(self.isCurrentDeviceTypeFunc(funcName: "getOneWireDevice")){
            oneWireDeviceView.isHidden = false
        }else{
            oneWireDeviceView.isHidden = true
        }
        oneWireWorkModeView = UIView()
        oneWireWorkModeView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        oneWireWorkModeView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(oneWireWorkModeView)
        self.oneWireWorkModeLabel = UILabel()
        self.oneWireWorkModeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.oneWireWorkModeLabel.textColor = UIColor.black
        self.oneWireWorkModeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.oneWireWorkModeLabel.numberOfLines = 0;
        self.oneWireWorkModeLabel.text = NSLocalizedString("one_wire_work_mode_desc", comment: "1-wire work mode:")
        self.oneWireWorkModeLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        oneWireWorkModeView.addSubview(self.oneWireWorkModeLabel)
        self.oneWireWorkModeContentLabel = UILabel()
        self.oneWireWorkModeContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.oneWireWorkModeContentLabel.textColor = UIColor.black
        self.oneWireWorkModeContentLabel.text = ""
        self.oneWireWorkModeContentLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.oneWireWorkModeContentLabel.numberOfLines = 0;
        self.oneWireWorkModeContentLabel.frame = CGRect(x: contentX + 5, y: 0, width: 70, height: 60)
        oneWireWorkModeView.addSubview(self.oneWireWorkModeContentLabel)
        self.editOneWireWorkModeBtn = QMUIGhostButton()
        self.editOneWireWorkModeBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editOneWireWorkModeBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editOneWireWorkModeBtn.ghostColor = UIColor.colorPrimary
        self.editOneWireWorkModeBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editOneWireWorkModeBtn.addTarget(self, action: #selector(editOneWireWorkModeClick), for:.touchUpInside)
        oneWireWorkModeView.addSubview(self.editOneWireWorkModeBtn)
        let oneWireWorkModeLine = UIView()
        oneWireWorkModeLine.backgroundColor = UIColor.gray
        oneWireWorkModeLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        oneWireWorkModeView.addSubview(oneWireWorkModeLine)
        if self.isCurrentDeviceTypeFunc(funcName: "oneWireWorkMode"){
            oneWireWorkModeView.isHidden = false
        }else{
            oneWireWorkModeView.isHidden = true
        }
        sendInstructionSequenceView = UIView()
        sendInstructionSequenceView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        sendInstructionSequenceView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(sendInstructionSequenceView)
        self.sendInstructionSequenceLabel = UILabel()
        self.sendInstructionSequenceLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.sendInstructionSequenceLabel.textColor = UIColor.black
        self.sendInstructionSequenceLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.sendInstructionSequenceLabel.numberOfLines = 0;
        self.sendInstructionSequenceLabel.text = NSLocalizedString("send_instructionSequence_desc", comment: "Send instruction sequence:")
        self.sendInstructionSequenceLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.sendInstructionSequenceLabel.numberOfLines = 0;
        self.sendInstructionSequenceLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        sendInstructionSequenceView.addSubview(self.sendInstructionSequenceLabel)
        self.sendInstructionSequenceBtn = QMUIGhostButton()
        self.sendInstructionSequenceBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.sendInstructionSequenceBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.sendInstructionSequenceBtn.ghostColor = UIColor.colorPrimary
        self.sendInstructionSequenceBtn.frame = CGRect(x: contentX , y: 15, width: 100, height: btnHeight)
        self.sendInstructionSequenceBtn.addTarget(self, action: #selector(sendInstructionSequenceClick), for:.touchUpInside)
        sendInstructionSequenceView.addSubview(self.sendInstructionSequenceBtn)
        let sendInstructionSequenceLine = UIView()
        sendInstructionSequenceLine.backgroundColor = UIColor.gray
        sendInstructionSequenceLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        sendInstructionSequenceView.addSubview(sendInstructionSequenceLine)
        if(self.isCurrentDeviceTypeFunc(funcName: "sendCmdSequence")){
            sendInstructionSequenceView.isHidden = false
        }else{
            sendInstructionSequenceView.isHidden = true
        }
        sequentialView = UIView()
        sequentialView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        sequentialView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(sequentialView)
        self.sequentialLabel = UILabel()
        self.sequentialLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.sequentialLabel.textColor = UIColor.black
        self.sequentialLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.sequentialLabel.numberOfLines = 0;
        self.sequentialLabel.text = NSLocalizedString("sequential_desc", comment: "Sequential:")
        self.sequentialLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.sequentialLabel.numberOfLines = 0;
        self.sequentialLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        sequentialView.addSubview(self.sequentialLabel)
        self.sequentialContentLabel = UILabel()
        self.sequentialContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.sequentialContentLabel.textColor = UIColor.black
        self.sequentialContentLabel.text = ""
        self.sequentialContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        sequentialView.addSubview(self.sequentialContentLabel)
        self.editSequentialBtn = QMUIGhostButton()
        self.editSequentialBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editSequentialBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editSequentialBtn.ghostColor = UIColor.colorPrimary
        self.editSequentialBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editSequentialBtn.addTarget(self, action: #selector(editSequentialClick), for:.touchUpInside)
        sequentialView.addSubview(self.editSequentialBtn)
        let sequentialLine = UIView()
        sequentialLine.backgroundColor = UIColor.gray
        sequentialLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        sequentialView.addSubview(sequentialLine)
        if self.isCurrentDeviceTypeFunc(funcName: "sequential"){
            sequentialView.isHidden = false
        }else{
            sequentialView.isHidden = true
        }
        sendDataView = UIView()
        sendDataView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        sendDataView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(sendDataView)
        self.sendDataLabel = UILabel()
        self.sendDataLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.sendDataLabel.textColor = UIColor.black
        self.sendDataLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.sendDataLabel.numberOfLines = 0;
        self.sendDataLabel.text = NSLocalizedString("rs485_send_desc", comment: "RS485 send:")
        self.sendDataLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.sendDataLabel.numberOfLines = 0;
        self.sendDataLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        sendDataView.addSubview(self.sendDataLabel)
        self.sendDataBtn = QMUIGhostButton()
        self.sendDataBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.sendDataBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.sendDataBtn.ghostColor = UIColor.colorPrimary
        self.sendDataBtn.frame = CGRect(x: contentX , y: 15, width: 100, height: btnHeight)
        self.sendDataBtn.addTarget(self, action: #selector(sendDataClick), for:.touchUpInside)
        sendDataView.addSubview(self.sendDataBtn)
        let sendDataLine = UIView()
        sendDataLine.backgroundColor = UIColor.gray
        sendDataLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        sendDataView.addSubview(sendDataLine)
        if(self.isCurrentDeviceTypeFunc(funcName: "rs485SendData")){
            sendDataView.isHidden = false
        }else{
            sendDataView.isHidden = true
        }
        rs485BaudRateView = UIView()
        rs485BaudRateView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        rs485BaudRateView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(rs485BaudRateView)
        self.rs485BaudRateLabel = UILabel()
        self.rs485BaudRateLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.rs485BaudRateLabel.textColor = UIColor.black
        self.rs485BaudRateLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.rs485BaudRateLabel.numberOfLines = 0;
        self.rs485BaudRateLabel.text = NSLocalizedString("rs485_baud_rate_desc", comment: "RS485 band rate:")
        self.rs485BaudRateLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.rs485BaudRateLabel.numberOfLines = 0;
        self.rs485BaudRateLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        rs485BaudRateView.addSubview(self.rs485BaudRateLabel)
        self.rs485BaudRateContentLabel = UILabel()
        self.rs485BaudRateContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.rs485BaudRateContentLabel.textColor = UIColor.black
        self.rs485BaudRateContentLabel.text = ""
        self.rs485BaudRateContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        rs485BaudRateView.addSubview(self.rs485BaudRateContentLabel)
        self.editRs485BaudRateBtn = QMUIGhostButton()
        self.editRs485BaudRateBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editRs485BaudRateBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editRs485BaudRateBtn.ghostColor = UIColor.colorPrimary
        self.editRs485BaudRateBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editRs485BaudRateBtn.addTarget(self, action: #selector(editRs485BaudRateClick), for:.touchUpInside)
        rs485BaudRateView.addSubview(self.editRs485BaudRateBtn)
        let rs485BaudRateLine = UIView()
        rs485BaudRateLine.backgroundColor = UIColor.gray
        rs485BaudRateLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        rs485BaudRateView.addSubview(rs485BaudRateLine)
        if self.isCurrentDeviceTypeFunc(funcName: "rs485BaudRate"){
            rs485BaudRateView.isHidden = false
        }else{
            rs485BaudRateView.isHidden = true
        }
        rs485EnableView = UIView()
        rs485EnableView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        rs485EnableView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(rs485EnableView)
        self.rs485EnableLabel = UILabel()
        self.rs485EnableLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.rs485EnableLabel.textColor = UIColor.black
        self.rs485EnableLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.rs485EnableLabel.numberOfLines = 0;
        self.rs485EnableLabel.text = NSLocalizedString("rs485_enable_desc", comment:"RS485 enable:")
        self.rs485EnableLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        rs485EnableView.addSubview(self.rs485EnableLabel)
        self.rs485EnableSwitch = UISwitch()
        self.rs485EnableSwitch.frame = CGRect(x: contentX, y: 10, width: 70, height: 45)
        self.rs485EnableSwitch.addTarget(self, action: #selector(rs485EnableSwitchAction),
                                         for:UIControl.Event.valueChanged)
        rs485EnableView.addSubview(self.rs485EnableSwitch)
        self.rs485EnableStatusLabel = UILabel()
        self.rs485EnableStatusLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.rs485EnableStatusLabel.textColor = UIColor.black
        self.rs485EnableStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.rs485EnableStatusLabel.numberOfLines = 0;
        self.rs485EnableStatusLabel.frame = CGRect(x: btnX, y: 0, width: descWidth, height: 60)
        rs485EnableView.addSubview(self.rs485EnableStatusLabel)
        let rs485EnableLine = UIView()
        rs485EnableLine.backgroundColor = UIColor.gray
        rs485EnableLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        rs485EnableView.addSubview(rs485EnableLine)
        if self.isCurrentDeviceTypeFunc(funcName: "rs485Enable"){
            rs485EnableView.isHidden = false
        }else{
            rs485EnableView.isHidden = true
        }
        
        
        transmittedPowerView = UIView()
        transmittedPowerView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        transmittedPowerView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(transmittedPowerView)
        
        self.transmittedPowerLabel = UILabel()
        self.transmittedPowerLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.transmittedPowerLabel.textColor = UIColor.black
        self.transmittedPowerLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.transmittedPowerLabel.numberOfLines = 0;
        self.transmittedPowerLabel.text = NSLocalizedString("transmitted_power", comment: "Transmitted power:")
        self.transmittedPowerLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.transmittedPowerLabel.numberOfLines = 0;
        self.transmittedPowerLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        transmittedPowerView.addSubview(self.transmittedPowerLabel)
        self.transmittedPowerContentLabel = UILabel()
        self.transmittedPowerContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.transmittedPowerContentLabel.textColor = UIColor.black
        self.transmittedPowerContentLabel.text = ""
        self.transmittedPowerContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        transmittedPowerView.addSubview(self.transmittedPowerContentLabel)
        self.editTransmittedPowerBtn = QMUIGhostButton()
        self.editTransmittedPowerBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editTransmittedPowerBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editTransmittedPowerBtn.ghostColor = UIColor.colorPrimary
        self.editTransmittedPowerBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editTransmittedPowerBtn.addTarget(self, action: #selector(editTransmittedPower), for:.touchUpInside)
        transmittedPowerView.addSubview(self.editTransmittedPowerBtn)
        let transmittedPowerLine = UIView()
        transmittedPowerLine.backgroundColor = UIColor.gray
        transmittedPowerLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        transmittedPowerView.addSubview(transmittedPowerLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "transmittedPower"){
            transmittedPowerView.isHidden = false
        }else{
            transmittedPowerView.isHidden = true
        }
        
        broadcastCycleView = UIView()
        broadcastCycleView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        broadcastCycleView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(broadcastCycleView)
        self.broadcastCycleLabel = UILabel()
        self.broadcastCycleLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.broadcastCycleLabel.textColor = UIColor.black
        self.broadcastCycleLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.broadcastCycleLabel.numberOfLines = 0;
        self.broadcastCycleLabel.text = NSLocalizedString("broadcast_cycle_desc", comment: "Broadcast cycle:")
        self.broadcastCycleLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.broadcastCycleLabel.numberOfLines = 0;
        self.broadcastCycleLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        broadcastCycleView.addSubview(self.broadcastCycleLabel)
        self.broadcastCycleContentLabel = UILabel()
        self.broadcastCycleContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.broadcastCycleContentLabel.textColor = UIColor.black
        self.broadcastCycleContentLabel.text = ""
        self.broadcastCycleContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        broadcastCycleView.addSubview(self.broadcastCycleContentLabel)
        self.editBroadcastCycleBtn = QMUIGhostButton()
        self.editBroadcastCycleBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editBroadcastCycleBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editBroadcastCycleBtn.ghostColor = UIColor.colorPrimary
        self.editBroadcastCycleBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editBroadcastCycleBtn.addTarget(self, action: #selector(editBroadcastCycle), for:.touchUpInside)
        broadcastCycleView.addSubview(self.editBroadcastCycleBtn)
        let broadcastCycleLine = UIView()
        broadcastCycleLine.backgroundColor = UIColor.gray
        broadcastCycleLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        broadcastCycleView.addSubview(broadcastCycleLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "broadcastCycle"){
            broadcastCycleView.isHidden = false
        }else{
            broadcastCycleView.isHidden = true
        }
        
        
        saveIntervalView = UIView()
        saveIntervalView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        saveIntervalView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(saveIntervalView)
        self.saveIntervalLabel = UILabel()
        self.saveIntervalLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.saveIntervalLabel.textColor = UIColor.black
        self.saveIntervalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.saveIntervalLabel.numberOfLines = 0;
        self.saveIntervalLabel.text = NSLocalizedString("save_interval_desc", comment: "Save interval:")
        self.saveIntervalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.saveIntervalLabel.numberOfLines = 0;
        self.saveIntervalLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        saveIntervalView.addSubview(self.saveIntervalLabel)
        self.saveIntervalContentLabel = UILabel()
        self.saveIntervalContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.saveIntervalContentLabel.textColor = UIColor.black
        self.saveIntervalContentLabel.text = ""
        self.saveIntervalContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        saveIntervalView.addSubview(self.saveIntervalContentLabel)
        self.editSaveIntervalBtn = QMUIGhostButton()
        self.editSaveIntervalBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editSaveIntervalBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editSaveIntervalBtn.ghostColor = UIColor.colorPrimary
        self.editSaveIntervalBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editSaveIntervalBtn.addTarget(self, action: #selector(editSaveIntervalClick), for:.touchUpInside)
        saveIntervalView.addSubview(self.editSaveIntervalBtn)
        let saveIntervalLine = UIView()
        saveIntervalLine.backgroundColor = UIColor.gray
        saveIntervalLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        saveIntervalView.addSubview(saveIntervalLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "saveInterval"){
            saveIntervalView.isHidden = false
        }else{
            saveIntervalView.isHidden = true
        }
        
        tempHighAlarmView = UIView()
        tempHighAlarmView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        tempHighAlarmView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(tempHighAlarmView)
        self.tempHighAlarmLabel = UILabel()
        self.tempHighAlarmLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.tempHighAlarmLabel.textColor = UIColor.black
        self.tempHighAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.tempHighAlarmLabel.numberOfLines = 0;
        self.tempHighAlarmLabel.text = NSLocalizedString("temperature_high_alarm_desc", comment: "Temperature high alarm:")
        self.tempHighAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.tempHighAlarmLabel.numberOfLines = 0;
        self.tempHighAlarmLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        tempHighAlarmView.addSubview(self.tempHighAlarmLabel)
        self.tempHighAlarmContentLabel = UILabel()
        self.tempHighAlarmContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.tempHighAlarmContentLabel.textColor = UIColor.black
        self.tempHighAlarmContentLabel.text = ""
        self.tempHighAlarmContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        tempHighAlarmView.addSubview(self.tempHighAlarmContentLabel)
        self.editTempHighAlarmBtn = QMUIGhostButton()
        self.editTempHighAlarmBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editTempHighAlarmBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editTempHighAlarmBtn.ghostColor = UIColor.colorPrimary
        self.editTempHighAlarmBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editTempHighAlarmBtn.addTarget(self, action: #selector(editTempAlarm), for:.touchUpInside)
        tempHighAlarmView.addSubview(self.editTempHighAlarmBtn)
        let tempAlarmLine = UIView()
        tempAlarmLine.backgroundColor = UIColor.gray
        tempAlarmLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        tempHighAlarmView.addSubview(tempAlarmLine)
        
        tempLowAlarmView = UIView()
        tempLowAlarmView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        tempLowAlarmView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(tempLowAlarmView)
        self.tempLowAlarmLabel = UILabel()
        self.tempLowAlarmLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.tempLowAlarmLabel.textColor = UIColor.black
        self.tempLowAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.tempLowAlarmLabel.numberOfLines = 0;
        self.tempLowAlarmLabel.text = NSLocalizedString("temperature_low_alarm_desc", comment: "Temperature low alarm:")
        self.tempLowAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.tempLowAlarmLabel.numberOfLines = 0;
        self.tempLowAlarmLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        tempLowAlarmView.addSubview(self.tempLowAlarmLabel)
        self.tempLowAlarmContentLabel = UILabel()
        self.tempLowAlarmContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.tempLowAlarmContentLabel.textColor = UIColor.black
        self.tempLowAlarmContentLabel.text = ""
        self.tempLowAlarmContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        tempLowAlarmView.addSubview(self.tempLowAlarmContentLabel)
        self.editTempLowAlarmBtn = QMUIGhostButton()
        self.editTempLowAlarmBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editTempLowAlarmBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editTempLowAlarmBtn.ghostColor = UIColor.colorPrimary
        self.editTempLowAlarmBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editTempLowAlarmBtn.addTarget(self, action: #selector(editTempAlarm), for:.touchUpInside)
        tempLowAlarmView.addSubview(self.editTempLowAlarmBtn)
        let humidityAlarmLine = UIView()
        humidityAlarmLine.backgroundColor = UIColor.gray
        humidityAlarmLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        tempLowAlarmView.addSubview(humidityAlarmLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "tempAlarm"){
            tempHighAlarmView.isHidden = false
            tempLowAlarmView.isHidden = false
        }else {
            tempHighAlarmView.isHidden = true
            tempLowAlarmView.isHidden = true
        }
        
        humidityHighAlarmView = UIView()
        humidityHighAlarmView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        humidityHighAlarmView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(humidityHighAlarmView)
        self.humidityHighAlarmLabel = UILabel()
        self.humidityHighAlarmLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.humidityHighAlarmLabel.textColor = UIColor.black
        self.humidityHighAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.humidityHighAlarmLabel.numberOfLines = 0;
        self.humidityHighAlarmLabel.text = NSLocalizedString("humidity_high_alarm_desc", comment: "Humidity high alarm:")
        self.humidityHighAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.humidityHighAlarmLabel.numberOfLines = 0;
        self.humidityHighAlarmLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        humidityHighAlarmView.addSubview(self.humidityHighAlarmLabel)
        self.humidityHighAlarmContentLabel = UILabel()
        self.humidityHighAlarmContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.humidityHighAlarmContentLabel.textColor = UIColor.black
        self.humidityHighAlarmContentLabel.text = ""
        self.humidityHighAlarmContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        humidityHighAlarmView.addSubview(self.humidityHighAlarmContentLabel)
        self.editHumidityHighAlarmBtn = QMUIGhostButton()
        self.editHumidityHighAlarmBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editHumidityHighAlarmBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editHumidityHighAlarmBtn.ghostColor = UIColor.colorPrimary
        self.editHumidityHighAlarmBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editHumidityHighAlarmBtn.addTarget(self, action: #selector(editHumidityAlarm), for:.touchUpInside)
        humidityHighAlarmView.addSubview(self.editHumidityHighAlarmBtn)
        let humidityHighAlarmLine = UIView()
        humidityHighAlarmLine.backgroundColor = UIColor.gray
        humidityHighAlarmLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        humidityHighAlarmView.addSubview(humidityHighAlarmLine)
        
        humidityLowAlarmView = UIView()
        humidityLowAlarmView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        humidityLowAlarmView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(humidityLowAlarmView)
        self.humidityLowAlarmLabel = UILabel()
        self.humidityLowAlarmLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.humidityLowAlarmLabel.textColor = UIColor.black
        self.humidityLowAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.humidityLowAlarmLabel.numberOfLines = 0;
        self.humidityLowAlarmLabel.text = NSLocalizedString("humidity_low_alarm_desc", comment: "Humidity low alarm:")
        self.humidityLowAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.humidityLowAlarmLabel.numberOfLines = 0;
        self.humidityLowAlarmLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        humidityLowAlarmView.addSubview(self.humidityLowAlarmLabel)
        self.humidityLowAlarmContentLabel = UILabel()
        self.humidityLowAlarmContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.humidityLowAlarmContentLabel.textColor = UIColor.black
        self.humidityLowAlarmContentLabel.text = ""
        self.humidityLowAlarmContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        humidityLowAlarmView.addSubview(self.humidityLowAlarmContentLabel)
        self.editHumidityLowAlarmBtn = QMUIGhostButton()
        self.editHumidityLowAlarmBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editHumidityLowAlarmBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editHumidityLowAlarmBtn.ghostColor = UIColor.colorPrimary
        self.editHumidityLowAlarmBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editHumidityLowAlarmBtn.addTarget(self, action: #selector(editHumidityAlarm), for:.touchUpInside)
        humidityLowAlarmView.addSubview(self.editHumidityLowAlarmBtn)
        let humidityLowAlarmLine = UIView()
        humidityLowAlarmLine.backgroundColor = UIColor.gray
        humidityLowAlarmLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        humidityLowAlarmView.addSubview(humidityLowAlarmLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "humidityAlarm"){
            humidityHighAlarmView.isHidden = false
            humidityLowAlarmView.isHidden = false
        }else{
            humidityHighAlarmView.isHidden = true
            humidityLowAlarmView.isHidden = true
        }
        recordControlView = UIView()
        recordControlView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        recordControlView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(recordControlView)
        self.recordControlLabel = UILabel()
        self.recordControlLabel.textColor = UIColor.black
        self.recordControlLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.recordControlLabel.numberOfLines = 0;
        self.recordControlLabel.text = NSLocalizedString("record_control_desc", comment: "Record control:")
        self.recordControlLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.recordControlLabel.numberOfLines = 0;
        self.recordControlLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        recordControlView.addSubview(self.recordControlLabel)
        self.startRecordBtn = QMUIGhostButton()
        self.startRecordBtn.setTitle(NSLocalizedString("start_record", comment: "Start Record"), for: .normal)
        self.startRecordBtn.ghostColor = UIColor.colorPrimary
        self.startRecordBtn.frame = CGRect(x: contentX - 20, y: 15, width: 120, height: btnHeight)
        self.startRecordBtn.addTarget(self, action: #selector(startRecordClick), for:.touchUpInside)
        recordControlView.addSubview(self.startRecordBtn)
        self.stopRecordBtn = QMUIGhostButton()
        self.stopRecordBtn.setTitle(NSLocalizedString("stop_record", comment: "Stop Record"), for: .normal)
        self.stopRecordBtn.ghostColor = UIColor.colorPrimary
        self.stopRecordBtn.frame = CGRect(x: contentX - 20, y: 15, width: 120, height: btnHeight)
        self.stopRecordBtn.addTarget(self, action: #selector(stopRecordClick), for:.touchUpInside)
        self.stopRecordBtn.isHidden = true
        recordControlView.addSubview(self.stopRecordBtn)
        let recordControlLine = UIView()
        recordControlLine.backgroundColor = UIColor.gray
        recordControlLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        recordControlView.addSubview(recordControlLine)
        if(self.isCurrentDeviceTypeFunc(funcName: "startRecord")){
            recordControlView.isHidden = false
        }else{
            recordControlView.isHidden = true
        }
        
        
        clearRecordView = UIView()
        clearRecordView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        clearRecordView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(clearRecordView)
        self.clearRecordLabel = UILabel()
        self.clearRecordLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.clearRecordLabel.textColor = UIColor.black
        self.clearRecordLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.clearRecordLabel.numberOfLines = 0;
        self.clearRecordLabel.text = NSLocalizedString("clear_cache_desc", comment:"Clear cache:")
        self.clearRecordLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.clearRecordLabel.numberOfLines = 0;
        self.clearRecordLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        clearRecordView.addSubview(self.clearRecordLabel)
        self.clearRecordBtn = QMUIGhostButton()
        self.clearRecordBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.clearRecordBtn.setTitle(NSLocalizedString("clear_record", comment:"Clear record"), for: .normal)
        self.clearRecordBtn.ghostColor = UIColor.colorPrimary
        self.clearRecordBtn.frame = CGRect(x: contentX + 15, y: 15, width: 120, height: btnHeight)
        self.clearRecordBtn.addTarget(self, action: #selector(clearRecordClick), for:.touchUpInside)
        clearRecordView.addSubview(self.clearRecordBtn)
        let clearRecordLine = UIView()
        clearRecordLine.backgroundColor = UIColor.gray
        clearRecordLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        clearRecordView.addSubview(clearRecordLine)
        
        
        if(self.isCurrentDeviceTypeFunc(funcName: "startRecord")){
            clearRecordView.isHidden = false
        }else{
            clearRecordView.isHidden = true
        }
        saveCountView = UIView()
        saveCountView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        saveCountView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(saveCountView)
        self.saveCountLabel = UILabel()
        self.saveCountLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.saveCountLabel.textColor = UIColor.black
        self.saveCountLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.saveCountLabel.numberOfLines = 0;
        self.saveCountLabel.text = NSLocalizedString("save_count_desc", comment: "Save count:")
        self.saveCountLabel.frame = CGRect(x: 15, y: 0, width: descWidth - 10, height: 60)
        saveCountView.addSubview(self.saveCountLabel)
        self.saveCountContentLabel = UILabel()
        self.saveCountContentLabel.textColor = UIColor.black
        self.saveCountContentLabel.text = ""
        self.saveCountContentLabel.frame = CGRect(x: contentX, y: 0, width: 50, height: 60)
        saveCountView.addSubview(self.saveCountContentLabel)
        self.saveCountRefreshBtn = QMUIGhostButton()
        self.saveCountRefreshBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.saveCountRefreshBtn.setTitle(NSLocalizedString("refresh", comment:"Refresh"), for: .normal)
        self.saveCountRefreshBtn.ghostColor = UIColor.colorPrimary
        self.saveCountRefreshBtn.frame = CGRect(x: contentX + 53, y: 15, width: 77, height: btnHeight)
        self.saveCountRefreshBtn.addTarget(self, action: #selector(saveCountRefreshClick), for:.touchUpInside)
        saveCountView.addSubview(self.saveCountRefreshBtn)
        self.saveCountReadBtn = QMUIGhostButton()
        self.saveCountReadBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.saveCountReadBtn.setTitle(NSLocalizedString("read", comment:"Read"), for: .normal)
        self.saveCountReadBtn.ghostColor = UIColor.colorPrimary
        self.saveCountReadBtn.frame = CGRect(x: btnX+28, y: 15, width: 58, height: btnHeight)
        self.saveCountReadBtn.addTarget(self, action: #selector(readHistoryClick), for:.touchUpInside)
        saveCountView.addSubview(self.saveCountReadBtn)
        let saveCountLine = UIView()
        saveCountLine.backgroundColor = UIColor.gray
        saveCountLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        saveCountView.addSubview(saveCountLine)
        if self.isCurrentDeviceTypeFunc(funcName: "saveCount"){
            saveCountView.isHidden = false
        }else{
            saveCountView.isHidden = true
        }
        readAlarmView = UIView()
        readAlarmView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        readAlarmView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(readAlarmView)
        self.readAlarmLabel = UILabel()
        self.readAlarmLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.readAlarmLabel.textColor = UIColor.black
        self.readAlarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.readAlarmLabel.numberOfLines = 0;
        self.readAlarmLabel.text = NSLocalizedString("save_count_desc", comment: "Save count:")
        self.readAlarmLabel.frame = CGRect(x: 15, y: 0, width: descWidth - 10, height: 60)
        readAlarmView.addSubview(self.readAlarmLabel)
        self.readAlarmContentLabel = UILabel()
        self.readAlarmContentLabel.textColor = UIColor.black
        self.readAlarmContentLabel.text = ""
        self.readAlarmContentLabel.frame = CGRect(x: contentX, y: 0, width: 50, height: 60)
        readAlarmView.addSubview(self.readAlarmContentLabel)
        self.readAlarmRefreshBtn = QMUIGhostButton()
        self.readAlarmRefreshBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.readAlarmRefreshBtn.setTitle(NSLocalizedString("refresh", comment:"Refresh"), for: .normal)
        self.readAlarmRefreshBtn.ghostColor = UIColor.colorPrimary
        self.readAlarmRefreshBtn.frame = CGRect(x: contentX + 53, y: 15, width: 77, height: btnHeight)
        self.readAlarmRefreshBtn.addTarget(self, action: #selector(saveCountRefreshClick), for:.touchUpInside)
        readAlarmView.addSubview(self.readAlarmRefreshBtn)
        self.readAlarmReadBtn = QMUIGhostButton()
        self.readAlarmReadBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.readAlarmReadBtn.setTitle(NSLocalizedString("read", comment:"Read"), for: .normal)
        self.readAlarmReadBtn.ghostColor = UIColor.colorPrimary
        self.readAlarmReadBtn.frame = CGRect(x: btnX+28, y: 15, width: 58, height: btnHeight)
        self.readAlarmReadBtn.addTarget(self, action: #selector(readHistoryClick), for:.touchUpInside)
        readAlarmView.addSubview(self.readAlarmReadBtn)
        let readAlarmLine = UIView()
        readAlarmLine.backgroundColor = UIColor.gray
        readAlarmLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        readAlarmView.addSubview(readAlarmLine)
        if self.isCurrentDeviceTypeFunc(funcName: "readAlarm"){
            readAlarmView.isHidden = false
        }else{
            readAlarmView.isHidden = true
        }
        
        broadcastTypeView = UIView()
        broadcastTypeView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        broadcastTypeView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(broadcastTypeView)
        self.broadcastTypeLabel = UILabel()
        self.broadcastTypeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.broadcastTypeLabel.textColor = UIColor.black
        self.broadcastTypeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.broadcastTypeLabel.numberOfLines = 0;
        self.broadcastTypeLabel.text = NSLocalizedString("broadcast_type_desc", comment: "Broadcast type:")
        self.broadcastTypeLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        broadcastTypeView.addSubview(self.broadcastTypeLabel)
        self.broadcastTypeContentLabel = UILabel()
        self.broadcastTypeContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.broadcastTypeContentLabel.textColor = UIColor.black
        self.broadcastTypeContentLabel.text = ""
        self.broadcastTypeContentLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.broadcastTypeContentLabel.numberOfLines = 0;
        self.broadcastTypeContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        broadcastTypeView.addSubview(self.broadcastTypeContentLabel)
        self.editBroadcastTypeBtn = QMUIGhostButton()
        self.editBroadcastTypeBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editBroadcastTypeBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editBroadcastTypeBtn.ghostColor = UIColor.colorPrimary
        self.editBroadcastTypeBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editBroadcastTypeBtn.addTarget(self, action: #selector(editBroadcastTypeClick), for:.touchUpInside)
        broadcastTypeView.addSubview(self.editBroadcastTypeBtn)
        let broadcastTypeLine = UIView()
        broadcastTypeLine.backgroundColor = UIColor.gray
        broadcastTypeLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        broadcastTypeView.addSubview(broadcastTypeLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "broadcastType"){
            broadcastTypeView.isHidden = false
        }else{
            broadcastTypeView.isHidden = true
        }
        
        
        
        gSensorView = UIView()
        gSensorView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        gSensorView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(gSensorView)
        self.gSensorLabel = UILabel()
        self.gSensorLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.gSensorLabel.textColor = UIColor.black
        self.gSensorLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.gSensorLabel.numberOfLines = 0;
        self.gSensorLabel.text = NSLocalizedString("gSensor_desc", comment:"GSensor:")
        self.gSensorLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        gSensorView.addSubview(self.gSensorLabel)
        self.gSensorSwitch = UISwitch()
        self.gSensorSwitch.frame = CGRect(x: contentX, y: 10, width: 70, height: 45)
        self.gSensorSwitch.addTarget(self, action: #selector(gSensorSwitchAction),
                                     for:UIControl.Event.valueChanged)
        gSensorView.addSubview(self.gSensorSwitch)
        self.gSensorStatusLabel = UILabel()
        self.gSensorStatusLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.gSensorStatusLabel.textColor = UIColor.black
        self.gSensorStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.gSensorStatusLabel.numberOfLines = 0;
        self.gSensorStatusLabel.frame = CGRect(x: btnX, y: 0, width: descWidth, height: 60)
        gSensorView.addSubview(self.gSensorStatusLabel)
        let gSensorLine = UIView()
        gSensorLine.backgroundColor = UIColor.gray
        gSensorLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        gSensorView.addSubview(gSensorLine)
        if self.isCurrentDeviceTypeFunc(funcName: "gSensorEnable"){
            gSensorView.isHidden = false
        }else{
            gSensorView.isHidden = true
        }
        
        gSensorSensitivityView = UIView()
        gSensorSensitivityView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        gSensorSensitivityView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(gSensorSensitivityView)
        self.gSensorSensitivityLabel = UILabel()
        self.gSensorSensitivityLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.gSensorSensitivityLabel.textColor = UIColor.black
        self.gSensorSensitivityLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.gSensorSensitivityLabel.numberOfLines = 0;
        self.gSensorSensitivityLabel.text = NSLocalizedString("gsensor_sensitivity_desc", comment: "Gsensor sensitivity:")
        self.gSensorSensitivityLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.gSensorSensitivityLabel.numberOfLines = 0;
        self.gSensorSensitivityLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        gSensorSensitivityView.addSubview(self.gSensorSensitivityLabel)
        self.gSensorSensitivityContentLabel = UILabel()
        self.gSensorSensitivityContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.gSensorSensitivityContentLabel.textColor = UIColor.black
        self.gSensorSensitivityContentLabel.text = ""
        self.gSensorSensitivityContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        gSensorSensitivityView.addSubview(self.gSensorSensitivityContentLabel)
        self.editGSensorSensitivityBtn = QMUIGhostButton()
        self.editGSensorSensitivityBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editGSensorSensitivityBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editGSensorSensitivityBtn.ghostColor = UIColor.colorPrimary
        self.editGSensorSensitivityBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editGSensorSensitivityBtn.addTarget(self, action: #selector(editGSensorSensitivityClick), for:.touchUpInside)
        gSensorSensitivityView.addSubview(self.editGSensorSensitivityBtn)
        let gSensorSensitivityLine = UIView()
        gSensorSensitivityLine.backgroundColor = UIColor.gray
        gSensorSensitivityLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        gSensorSensitivityView.addSubview(gSensorSensitivityLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "gSensorSensitivity"){
            gSensorSensitivityView.isHidden = false
        }else{
            gSensorSensitivityView.isHidden = true
        }
        
        gSensorDetectionDurationView = UIView()
        gSensorDetectionDurationView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        gSensorDetectionDurationView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(gSensorDetectionDurationView)
        self.gSensorDetectionDurationLabel = UILabel()
        self.gSensorDetectionDurationLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.gSensorDetectionDurationLabel.textColor = UIColor.black
        self.gSensorDetectionDurationLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.gSensorDetectionDurationLabel.numberOfLines = 0;
        self.gSensorDetectionDurationLabel.text = NSLocalizedString("gsensor_detection_duration_desc", comment: "Gsensor detection duration:")
        self.gSensorDetectionDurationLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.gSensorDetectionDurationLabel.numberOfLines = 0;
        self.gSensorDetectionDurationLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        gSensorDetectionDurationView.addSubview(self.gSensorDetectionDurationLabel)
        self.gSensorDetectionDurationContentLabel = UILabel()
        self.gSensorDetectionDurationContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.gSensorDetectionDurationContentLabel.textColor = UIColor.black
        self.gSensorDetectionDurationContentLabel.text = ""
        self.gSensorDetectionDurationContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        gSensorDetectionDurationView.addSubview(self.gSensorDetectionDurationContentLabel)
        self.editGSensorDetectionDurationBtn = QMUIGhostButton()
        self.editGSensorDetectionDurationBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editGSensorDetectionDurationBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editGSensorDetectionDurationBtn.ghostColor = UIColor.colorPrimary
        self.editGSensorDetectionDurationBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editGSensorDetectionDurationBtn.addTarget(self, action: #selector(editGSensorDetectionDurationClick), for:.touchUpInside)
        gSensorDetectionDurationView.addSubview(self.editGSensorDetectionDurationBtn)
        let gSensorDetectionDurationLine = UIView()
        gSensorDetectionDurationLine.backgroundColor = UIColor.gray
        gSensorDetectionDurationLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        gSensorDetectionDurationView.addSubview(gSensorDetectionDurationLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "gSensorDetectionDuration"){
            gSensorDetectionDurationView.isHidden = false
        }else{
            gSensorDetectionDurationView.isHidden = true
        }
        
        gSensorDetectionIntervalView = UIView()
        gSensorDetectionIntervalView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        gSensorDetectionIntervalView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(gSensorDetectionIntervalView)
        self.gSensorDetectionIntervalLabel = UILabel()
        self.gSensorDetectionIntervalLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.gSensorDetectionIntervalLabel.textColor = UIColor.black
        self.gSensorDetectionIntervalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.gSensorDetectionIntervalLabel.numberOfLines = 0;
        self.gSensorDetectionIntervalLabel.text = NSLocalizedString("gsensor_detection_interval_desc", comment: "Gsensor detection interval:")
        self.gSensorDetectionIntervalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.gSensorDetectionIntervalLabel.numberOfLines = 0;
        self.gSensorDetectionIntervalLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        gSensorDetectionIntervalView.addSubview(self.gSensorDetectionIntervalLabel)
        self.gSensorDetectionIntervalContentLabel = UILabel()
        self.gSensorDetectionIntervalContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.gSensorDetectionIntervalContentLabel.textColor = UIColor.black
        self.gSensorDetectionIntervalContentLabel.text = ""
        self.gSensorDetectionIntervalContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        gSensorDetectionIntervalView.addSubview(self.gSensorDetectionIntervalContentLabel)
        self.editGSensorDetectionIntervalBtn = QMUIGhostButton()
        self.editGSensorDetectionIntervalBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editGSensorDetectionIntervalBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editGSensorDetectionIntervalBtn.ghostColor = UIColor.colorPrimary
        self.editGSensorDetectionIntervalBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editGSensorDetectionIntervalBtn.addTarget(self, action: #selector(editGSensorDetectionIntervalClick), for:.touchUpInside)
        gSensorDetectionIntervalView.addSubview(self.editGSensorDetectionIntervalBtn)
        let gSensorDetectionIntervalLine = UIView()
        gSensorDetectionIntervalLine.backgroundColor = UIColor.gray
        gSensorDetectionIntervalLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        gSensorDetectionIntervalView.addSubview(gSensorDetectionIntervalLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "gSensorDetectionInterval"){
            gSensorDetectionIntervalView.isHidden = false
        }else{
            gSensorDetectionIntervalView.isHidden = true
        }
        
        beaconMajorSetView = UIView()
        beaconMajorSetView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        beaconMajorSetView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(beaconMajorSetView)
        self.beaconMajorSetLabel = UILabel()
        self.beaconMajorSetLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.beaconMajorSetLabel.textColor = UIColor.black
        self.beaconMajorSetLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.beaconMajorSetLabel.numberOfLines = 0;
        self.beaconMajorSetLabel.text = NSLocalizedString("beacon_major_set_desc", comment: "Beacon major set:")
        self.beaconMajorSetLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.beaconMajorSetLabel.numberOfLines = 0;
        self.beaconMajorSetLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        beaconMajorSetView.addSubview(self.beaconMajorSetLabel)
        self.beaconMajorSetContentLabel = UILabel()
        self.beaconMajorSetContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.beaconMajorSetContentLabel.textColor = UIColor.black
        self.beaconMajorSetContentLabel.text = ""
        self.beaconMajorSetContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        beaconMajorSetView.addSubview(self.beaconMajorSetContentLabel)
        self.editBeaconMajorSetBtn = QMUIGhostButton()
        self.editBeaconMajorSetBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editBeaconMajorSetBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editBeaconMajorSetBtn.ghostColor = UIColor.colorPrimary
        self.editBeaconMajorSetBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editBeaconMajorSetBtn.addTarget(self, action: #selector(editBeaconMajorSetClick), for:.touchUpInside)
        beaconMajorSetView.addSubview(self.editBeaconMajorSetBtn)
        let beaconMajorSetLine = UIView()
        beaconMajorSetLine.backgroundColor = UIColor.gray
        beaconMajorSetLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        beaconMajorSetView.addSubview(beaconMajorSetLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "beaconMajorSet"){
            beaconMajorSetView.isHidden = false
        }else{
            beaconMajorSetView.isHidden = true
        }
        
        beaconMinorSetView = UIView()
        beaconMinorSetView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        beaconMinorSetView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(beaconMinorSetView)
        self.beaconMinorSetLabel = UILabel()
        self.beaconMinorSetLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.beaconMinorSetLabel.textColor = UIColor.black
        self.beaconMinorSetLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.beaconMinorSetLabel.numberOfLines = 0;
        self.beaconMinorSetLabel.text = NSLocalizedString("beacon_minor_set_desc", comment: "Beacon minor set:")
        self.beaconMinorSetLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.beaconMinorSetLabel.numberOfLines = 0;
        self.beaconMinorSetLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        beaconMinorSetView.addSubview(self.beaconMinorSetLabel)
        self.beaconMinorSetContentLabel = UILabel()
        self.beaconMinorSetContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.beaconMinorSetContentLabel.textColor = UIColor.black
        self.beaconMinorSetContentLabel.text = ""
        self.beaconMinorSetContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        beaconMinorSetView.addSubview(self.beaconMinorSetContentLabel)
        self.editBeaconMinorSetBtn = QMUIGhostButton()
        self.editBeaconMinorSetBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editBeaconMinorSetBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editBeaconMinorSetBtn.ghostColor = UIColor.colorPrimary
        self.editBeaconMinorSetBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editBeaconMinorSetBtn.addTarget(self, action: #selector(editBeaconMinorSetClick), for:.touchUpInside)
        beaconMinorSetView.addSubview(self.editBeaconMinorSetBtn)
        let beaconMinorSetLine = UIView()
        beaconMinorSetLine.backgroundColor = UIColor.gray
        beaconMinorSetLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        beaconMinorSetView.addSubview(beaconMinorSetLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "beaconMinorSet"){
            beaconMinorSetView.isHidden = false
        }else{
            beaconMinorSetView.isHidden = true
        }
        
        
        eddystoneNidSetView = UIView()
        eddystoneNidSetView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        eddystoneNidSetView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(eddystoneNidSetView)
        self.eddystoneNidSetLabel = UILabel()
        self.eddystoneNidSetLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.eddystoneNidSetLabel.textColor = UIColor.black
        self.eddystoneNidSetLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.eddystoneNidSetLabel.numberOfLines = 0;
        self.eddystoneNidSetLabel.text = NSLocalizedString("eddystone_nid_set_desc", comment: "Name space ID:")
        self.eddystoneNidSetLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        eddystoneNidSetView.addSubview(self.eddystoneNidSetLabel)
        self.eddystoneNidSetContentLabel = UILabel()
        self.eddystoneNidSetContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.eddystoneNidSetContentLabel.textColor = UIColor.black
        self.eddystoneNidSetContentLabel.text = ""
        self.eddystoneNidSetContentLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.eddystoneNidSetContentLabel.numberOfLines = 0;
        self.eddystoneNidSetContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        eddystoneNidSetView.addSubview(self.eddystoneNidSetContentLabel)
        self.editEddystoneNidSetBtn = QMUIGhostButton()
        self.editEddystoneNidSetBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editEddystoneNidSetBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editEddystoneNidSetBtn.ghostColor = UIColor.colorPrimary
        self.editEddystoneNidSetBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editEddystoneNidSetBtn.addTarget(self, action: #selector(editEddystoneNidSetClick), for:.touchUpInside)
        eddystoneNidSetView.addSubview(self.editEddystoneNidSetBtn)
        let eddystoneNidSetLine = UIView()
        eddystoneNidSetLine.backgroundColor = UIColor.gray
        eddystoneNidSetLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        eddystoneNidSetView.addSubview(eddystoneNidSetLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "eddystoneNIDSet"){
            eddystoneNidSetView.isHidden = false
        }else{
            eddystoneNidSetView.isHidden = true
        }
        
        eddystoneBidSetView = UIView()
        eddystoneBidSetView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        eddystoneBidSetView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(eddystoneBidSetView)
        self.eddystoneBidSetLabel = UILabel()
        self.eddystoneBidSetLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.eddystoneBidSetLabel.textColor = UIColor.black
        self.eddystoneBidSetLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.eddystoneBidSetLabel.numberOfLines = 0;
        self.eddystoneBidSetLabel.text = NSLocalizedString("eddystone_bid_set_desc", comment: "Instance ID:")
        self.eddystoneBidSetLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        eddystoneBidSetView.addSubview(self.eddystoneBidSetLabel)
        self.eddystoneBidSetContentLabel = UILabel()
        self.eddystoneBidSetContentLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.eddystoneBidSetContentLabel.textColor = UIColor.black
        self.eddystoneBidSetContentLabel.text = ""
        self.eddystoneBidSetContentLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.eddystoneBidSetContentLabel.numberOfLines = 0;
        self.eddystoneBidSetContentLabel.frame = CGRect(x: contentX, y: 0, width: 70, height: 60)
        eddystoneBidSetView.addSubview(self.eddystoneBidSetContentLabel)
        self.editEddystoneBidSetBtn = QMUIGhostButton()
        self.editEddystoneBidSetBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editEddystoneBidSetBtn.setTitle(NSLocalizedString("edit", comment: "Edit"), for: .normal)
        self.editEddystoneBidSetBtn.ghostColor = UIColor.colorPrimary
        self.editEddystoneBidSetBtn.frame = CGRect(x: btnX, y: 15, width: 60, height: btnHeight)
        self.editEddystoneBidSetBtn.addTarget(self, action: #selector(editEddystoneBidSetClick), for:.touchUpInside)
        eddystoneBidSetView.addSubview(self.editEddystoneBidSetBtn)
        let eddystoneBidSetLine = UIView()
        eddystoneBidSetLine.backgroundColor = UIColor.gray
        eddystoneBidSetLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        eddystoneBidSetView.addSubview(eddystoneBidSetLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "eddystoneBIDSet"){
            eddystoneBidSetView.isHidden = false
        }else{
            eddystoneBidSetView.isHidden = true
        }
        
        longRangeView = UIView()
        longRangeView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        longRangeView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(longRangeView)
        self.longRangeLabel = UILabel()
        self.longRangeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.longRangeLabel.textColor = UIColor.black
        self.longRangeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.longRangeLabel.numberOfLines = 0;
        self.longRangeLabel.text = NSLocalizedString("long_range_desc", comment:"Long range:")
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
        
        if self.isCurrentDeviceTypeFunc(funcName: "longRangeEnable"){
            longRangeView.isHidden = false
        }else{
            longRangeView.isHidden = true
        }
        
        relayView = UIView()
        relayView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        relayView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(relayView)
        self.relayLabel = UILabel()
        self.relayLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.relayLabel.textColor = UIColor.black
        self.relayLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.relayLabel.numberOfLines = 0;
        self.relayLabel.text = NSLocalizedString("relay_desc", comment:"Relay:")
        self.relayLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        relayView.addSubview(self.relayLabel)
        self.relaySwitch = UISwitch()
        self.relaySwitch.frame = CGRect(x: contentX, y: 10, width: 70, height: 45)
        self.relaySwitch.addTarget(self, action: #selector(relaySwitchAction),
                                   for:UIControl.Event.valueChanged)
        relayView.addSubview(self.relaySwitch)
        self.relayStatusLabel = UILabel()
        self.relayStatusLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.relayStatusLabel.textColor = UIColor.black
        self.relayStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.relayStatusLabel.numberOfLines = 0;
        self.relayStatusLabel.frame = CGRect(x: btnX, y: 0, width: descWidth, height: 60)
        relayView.addSubview(self.relayStatusLabel)
        let relayLine = UIView()
        relayLine.backgroundColor = UIColor.gray
        relayLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        relayView.addSubview(relayLine)
        if self.isCurrentDeviceTypeFunc(funcName: "relay"){
            relayView.isHidden = false
        }else{
            relayView.isHidden = true
        }
        
        
        ledView = UIView()
        ledView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        ledView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(ledView)
        self.ledLabel = UILabel()
        self.ledLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.ledLabel.textColor = UIColor.black
        self.ledLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.ledLabel.numberOfLines = 0;
        self.ledLabel.text = NSLocalizedString("led_desc", comment:"Led:")
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
        
        if self.isCurrentDeviceTypeFunc(funcName: "ledOpen"){
            ledView.isHidden = false
        }else{
            ledView.isHidden = true
        }
        
        lightSensorOpenView = UIView()
        lightSensorOpenView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        lightSensorOpenView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(lightSensorOpenView)
        self.lightSensorOpenLabel = UILabel()
        self.lightSensorOpenLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.lightSensorOpenLabel.textColor = UIColor.black
        self.lightSensorOpenLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.lightSensorOpenLabel.numberOfLines = 0;
        self.lightSensorOpenLabel.text = NSLocalizedString("light_sense_open_desc", comment:"Light sense open:")
        self.lightSensorOpenLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        lightSensorOpenView.addSubview(self.lightSensorOpenLabel)
        self.lightSensorOpenSwitch = UISwitch()
        self.lightSensorOpenSwitch.frame = CGRect(x: contentX, y: 10, width: 70, height: 45)
        self.lightSensorOpenSwitch.addTarget(self, action: #selector(lightSeosorOpenSwitchAction),
                                             for:UIControl.Event.valueChanged)
        lightSensorOpenView.addSubview(self.lightSensorOpenSwitch)
        let lightSensorOpenLine = UIView()
        lightSensorOpenLine.backgroundColor = UIColor.gray
        lightSensorOpenLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        lightSensorOpenView.addSubview(lightSensorOpenLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "lightSensorOpen"){
            lightSensorOpenView.isHidden = false
        }else{
            lightSensorOpenView.isHidden = true
        }
        
        
        doorView = UIView()
        doorView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        doorView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(doorView)
        self.doorLabel = UILabel()
        self.doorLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.doorLabel.textColor = UIColor.black
        self.doorLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.doorLabel.numberOfLines = 0;
        self.doorLabel.text = NSLocalizedString("door_enable_desc", comment:"Door:")
        self.doorLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        doorView.addSubview(self.doorLabel)
        self.doorSwitch = UISwitch()
        self.doorSwitch.frame = CGRect(x: contentX, y: 10, width: 70, height: 30)
        self.doorSwitch.addTarget(self, action: #selector(doorSwitchAction),
                                  for:UIControl.Event.valueChanged)
        doorView.addSubview(self.doorSwitch)
        let doorLine = UIView()
        doorLine.backgroundColor = UIColor.gray
        doorLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        doorView.addSubview(doorLine)
        
        if self.isCurrentDeviceTypeFunc(funcName: "doorEnable"){
            doorView.isHidden = false
        }else{
            doorView.isHidden = true
        }
        
        shutdownView = UIView()
        shutdownView.heightAnchor.constraint(equalToConstant: 60).isActive = true
        shutdownView.widthAnchor.constraint(equalToConstant:  KSize.width).isActive = true // 设置视图的宽度
        stackView.addArrangedSubview(shutdownView)
        self.shutdownLabel = UILabel()
        self.shutdownLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.shutdownLabel.textColor = UIColor.black
        self.shutdownLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.shutdownLabel.numberOfLines = 0;
        self.shutdownLabel.text =  NSLocalizedString("shutdown_desc", comment:"Shutdown:")
        self.shutdownLabel.frame = CGRect(x: 15, y: 0, width: descWidth, height: 60)
        shutdownView.addSubview(self.shutdownLabel)
        self.editShutdownBtn = QMUIGhostButton()
        self.editShutdownBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.editShutdownBtn.setTitle(NSLocalizedString("shutdown", comment:"Shutdown"), for: .normal)
        self.editShutdownBtn.ghostColor = UIColor.colorPrimary
        self.editShutdownBtn.addTarget(self, action: #selector(shutdownClick), for:.touchUpInside)
        self.editShutdownBtn.frame = CGRect(x: contentX, y: 15, width: 120, height: btnHeight)
        shutdownView.addSubview(self.editShutdownBtn)
        let shutdownLine = UIView()
        shutdownLine.backgroundColor = UIColor.gray
        shutdownLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        shutdownView.addSubview(shutdownLine)
        if self.isCurrentDeviceTypeFunc(funcName: "shutdown"){
            shutdownView.isHidden = false
        }else{
            shutdownView.isHidden = true
        }
        
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
        self.editResetFactoryBtn.setTitle(NSLocalizedString("reset_factory", comment:"Reset factory"), for: .normal)
        self.editResetFactoryBtn.ghostColor = UIColor.colorPrimary
        self.editResetFactoryBtn.addTarget(self, action: #selector(resetFactoryClick), for:.touchUpInside)
        self.editResetFactoryBtn.frame = CGRect(x: contentX, y: 15, width: 120, height: btnHeight)
        resetFactoryView.addSubview(self.editResetFactoryBtn)
        let resetFactoryLine = UIView()
        resetFactoryLine.backgroundColor = UIColor.gray
        resetFactoryLine.frame = CGRect(x: 0, y: Double(60), width: Double(KSize.width), height: 0.5)
        resetFactoryView.addSubview(resetFactoryLine)
        if self.isCurrentDeviceTypeFunc(funcName: "resetFactory"){
            resetFactoryView.isHidden = false
        }else{
            resetFactoryView.isHidden = true
        }
        //        scrollView.contentSize =  CGSize(width: self.view.bounds.size.width, height: stackView.frame.size.height + 20)
        scrollView.contentSize = stackViewContainer.frame.size
        print("frame")
        print(stackView.frame)
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
        self.pwdAlert = AEUIAlertView(style: .password, title: NSLocalizedString("enter_pwd_warning", comment:"Please enter your password"), message: nil)
        self.pwdAlert.textField.placeholder = NSLocalizedString("password", comment:"Password")
        if(Utils.isDebug){
            self.pwdAlert.textField.text = "654321"
        }
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
                if self.connected == true{
                    self.initStart = true
                    self.readTransmittedPower()
                    self.readBroadcastCycle()
                    self.readDeviceName()
                    self.readVersion()
                    self.readHumidityAlarm()
                    self.readTempAlarm()
                    self.readLedOpenStatus()
                    self.readRelayStatus()
                    self.readSaveCount()
                    self.readSaveInterval()
                    self.readLightSensorOpenStatus()
                    self.readRs485Enable()
                    self.readOneWireWorkMode()
                    self.readRs485BaudRate()
                    self.readBroadcastType()
                    self.readLongRangeEnable()
                    self.readGSensorEnable()
                    self.readDinStatusEvent()
                    self.readDinVoltage( )
                    self.readDoorEnableStatus()
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
    
    func isCurrentDeviceTypeFunc(funcName:String) -> Bool{
        
        if deviceType == "S04"{
            if funcName == "transmittedPower"{
                if Int(software) ?? 0 >= 13{
                    return true;
                }else{
                    return false;
                }
            }
            if funcName == "firmware" || funcName == "password" || funcName == "resetFactory" || funcName == "broadcastCycle"
                || funcName == "tempAlarm" || funcName == "ledOpen" || funcName == "deviceName" || funcName == "startRecord"
                || funcName == "stopRecord" || funcName == "clearRecord"
                || funcName == "saveCount" || funcName == "readAlarm" || funcName == "readOriginData" || funcName == "time" {
                if Int(software) ?? 0 < 11 && (funcName == "saveInterval" || funcName == "saveCount"
                                               || funcName == "readAlarm" || funcName == "readOriginData" || funcName == "startRecord") {
                    return false;
                }
                return true;
            }else{
                return false;
            }
        }else  if deviceType == "S05"{
            if funcName == "transmittedPower"{
                if Int(software) ?? 0 >= 13{
                    return true;
                }else{
                    return false;
                }
            }
            if funcName == "firmware" || funcName == "password" || funcName == "resetFactory" || funcName == "broadcastCycle"
                || funcName == "tempAlarm" || funcName == "ledOpen" || funcName == "deviceName" || funcName == "relay"
                || funcName == "time"  {
                return true;
            }else{
                return false;
            }
        }else if deviceType == "S02"{
            if funcName == "transmittedPower"{
                if Int(software) ?? 0 >= 13 {
                    return true;
                }else{
                    return false;
                }
            }
            if funcName == "lightSensorOpen"{
                if Int(software) ?? 0 >= 20{
                    return true;
                }else{
                    return false;
                }
            }
            if Int(software) ?? 0 < 11 && (funcName == "saveInterval" || funcName == "readHistory"
                                           || funcName == "saveCount" || funcName == "readAlarm" || funcName == "readOriginData"  || funcName == "startRecord"){
                return false;
            }
            if funcName == "saveInterval" || funcName == "firmware" || funcName == "password" || funcName == "resetFactory" || funcName == "broadcastCycle"
                || funcName == "tempAlarm" || funcName == "ledOpen" || funcName == "deviceName" || funcName == "startRecord"
                || funcName == "stopRecord" || funcName == "clearRecord" || funcName == "readHistory" || funcName == "humidityAlarm"
                || funcName == "saveCount" || funcName == "readAlarm" || funcName == "readOriginData" || funcName == "transmittedPower"
                || funcName == "time"{
                return true;
            }else{
                return false;
            }
        }else if deviceType == "S09"{
            if funcName == "firmware" || funcName == "password" || funcName == "resetFactory"
                || funcName == "transmittedPower" || funcName == "ledOpen" || funcName == "deviceName"
                || funcName == "readDinVoltage" || funcName == "dinStatusEvent" || funcName == "doutStatus" || funcName == "readVinVoltage"
                || funcName == "readAinVoltage" || funcName == "setPositiveNegativeWarning" || funcName == "ainPositiveNegativeWarning" || funcName == "getOneWireDevice"
                || funcName == "sendCmdSequence" || funcName == "oneWireWorkMode" || funcName == "rs485SendData"
                || funcName == "rs485BaudRate" || funcName == "rs485Enable" || funcName == "longRangeEnable" || funcName == "getAinEvent" {
                return true;
            }else{
                return false;
            }
        }else if deviceType == "S08"{
            if funcName == "saveInterval" || funcName == "firmware" || funcName == "password" || funcName == "resetFactory" || funcName == "broadcastCycle"
                || funcName == "tempAlarm" || funcName == "ledOpen" || funcName == "deviceName" || funcName == "startRecord"
                || funcName == "stopRecord" || funcName == "clearRecord"
                || funcName == "saveCount" || funcName == "readAlarm" || funcName == "readOriginData" || funcName == "transmittedPower" || funcName == "longRangeEnable"
                || funcName == "broadcastType"  || funcName == "readHistory"
                || funcName == "time"    || funcName == "shutdown" || funcName == "doorEnable"  || funcName == "gSensorEnable"
                || funcName == "gSensorSensitivity" || funcName == "gSensorDetectionDuration" || funcName == "gSensorDetectionInterval"
                || funcName == "beaconMajorSet" || funcName == "beaconMinorSet" || funcName == "eddystoneNIDSet" || funcName == "eddystoneBIDSet" {
                return true;
            }
            return false;
        }else if deviceType == "S10"{
            if funcName == "saveInterval" || funcName == "firmware" || funcName == "password" || funcName == "resetFactory" || funcName == "broadcastCycle"
                || funcName == "tempAlarm" || funcName == "ledOpen" || funcName == "deviceName" || funcName == "startRecord"
                || funcName == "stopRecord" || funcName == "clearRecord"
                || funcName == "saveCount" || funcName == "readAlarm" || funcName == "readOriginData" || funcName == "transmittedPower" || funcName == "longRangeEnable"
                || funcName == "broadcastType" || funcName == "readHistory" || funcName == "humidityAlarm"
                || funcName == "time"   || funcName == "shutdown"
                || funcName == "beaconMajorSet" || funcName == "beaconMinorSet" || funcName == "eddystoneNIDSet" || funcName == "eddystoneBIDSet"
            {
                return true;
            }
            return false;
        }
        else if deviceType == "S07"{
            if funcName == "firmware" || funcName == "password" || funcName == "resetFactory" || funcName == "broadcastCycle"
                ||  funcName == "transmittedPower" || funcName == "longRangeEnable" || funcName == "deviceName"
                || funcName == "broadcastType"
                || funcName == "beaconMajorSet" || funcName == "beaconMinorSet" || funcName == "eddystoneNIDSet" || funcName == "eddystoneBIDSet" {
                return true;
            }
            return false;
        }else{
            return false;
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

extension EditConfigController:EditPositiveNegativeWarningValueDelegate{
    
    func setPositiveNegativeWarningValue(port:Int,mode:Int,highVoltage:Int,lowVoltage:Int,ditheringIntervalHigh:Int,
                                         ditheringIntervalLow:Int,samplingInterval:Int){
        print("setPositiveNegativeWarningValue")
        self.writePositiveNegativeWaning(port: port, mode: mode, highVoltage: highVoltage, lowVoltage: lowVoltage, samplingInterval: samplingInterval,
                                         ditheringIntervalHigh: ditheringIntervalHigh, ditheringIntervalLow: ditheringIntervalLow)
    }
}

extension EditConfigController:EditDoutOutputDelegate{
    func setDoutValue(dout0:Int,dout1:Int){
        self.writeDoutStatus(port:0,value:dout0)
        self.writeDoutStatus(port:1,value:dout1)
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

extension EditConfigController:EditInstructionSequenceDelegate{
    func setCmd(cmd:String){
        self.writeSendInstructionSequence(cmd:cmd)
    }
}

extension EditConfigController:EditRS485CmdDelegate{
    func setRS485Cmd(cmd:String){
        self.writeRS485SendData(cmd:cmd)
    }
}

extension EditConfigController:HistorySelectDelegate{
    func setSelectDate(startDate: Date, endDate: Date) {
        self.startDate = startDate
        self.endDate = endDate
        if(self.isReadHis){
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
