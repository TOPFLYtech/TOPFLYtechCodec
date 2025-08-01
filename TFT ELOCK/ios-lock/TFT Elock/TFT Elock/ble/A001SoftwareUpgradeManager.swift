//
//  A001SoftwareUpgradeManager.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/4/7.
//  Copyright © 2025 com.tftiot. All rights reserved.
//

import Foundation
protocol UpgradeStatusCallback{
    func onUpgradeStatus(status:Int,percent:Float)
}
class A001SoftwareUpgradeManager :BleStatusCallback {
    private var upgradeFileContentList: [[[UInt8]]] = []
    private var all4KPackageCrc: [Int] = []
    private var allBytes: [UInt8] = []
    private var isStop = false
    private var isSendFirst = false
    private var onePackLen = 245
    private var curStep = 0
    private var cur4KPackage = 0
    private var curPackage = 0
    private var restartFromHeadCount = 0
    private var sendPackageErrorCount = 0
    private var deviceTypeHead: [UInt8] = []
    private var lastWriteDate: Date?
    private var isWaitResponse = false

    static let STATUS_OF_FINE_NOT_FIND = -1
    static let STATUS_OF_UPGRADE_START = 0
    static let STATUS_OF_UPGRADE_WRITE_ONE_BUFFER = 1
    static let STATUS_OF_UPGRADE_CHECK_ONE_BUFFER_CRC = 2
    static let STATUS_OF_UPGRADE_CHECK_ALL_CRC = 3
    static let STATUS_OF_UPGRADE_WRITE_FLASH = 4
    static let STATUS_OF_UPGRADE_WRITE_END = 5
    static let STATUS_OF_UPGRADE_WRITE_SUCC = 6
    static let STATUS_OF_UPGRADE_UNKNOWN_ERROR = 7
    static let STATUS_OF_UPGRADE_WRITE_CUR_PACKAGE = 8
    static let STATUS_OF_UPGRADE_ERROR_UPGRADE_FILE = 9

    private var stepTimeoutMap: [Int: Int] = [
        0: 10,
        1: 10,
        2: 12,
        3: 15,
        4: 10,
        5: 10,
        6: 10,
        8: 10
    ]
    private func checkRespTimeout() -> Bool {
        guard let lastWriteDate = lastWriteDate else { return false }
        let now = Date()
        let timeout = stepTimeoutMap[curStep] ?? 10
        return now.timeIntervalSince(lastWriteDate) > Double(timeout)
    }
    func onNotifyValue(_ value: [UInt8]) {
        
    }
    
    func onBleStatusCallback(_ connectStatus: Int) {
        
    }
    
    func onUpgradeNotifyValue(_ value: [UInt8]) {
        receiveCmdResp(value)
    }


    @objc private func checkTaskStatus() {
        while !isStop {
            do {
                if !isStartUpgrade {
                    Thread.sleep(forTimeInterval: 1)
                    continue
                }
                if isWaitResponse {
                    if checkRespTimeout() {
                        doErrorCtrl(nextStep: curStep)
                        isWaitResponse = false
                    } else {
                        Thread.sleep(forTimeInterval: 0.05)
                        continue
                    }
                }

                switch curStep {
                case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_START:
                    isWaitResponse = true
                    if deviceTypeHead.count == 4 {
                        var content: [UInt8] = [0x01]
                        content.append(contentsOf: deviceTypeHead)
                        writeArray(content)
                    } else {
                        isStartUpgrade = false
                        callback?.onUpgradeStatus(status: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR, percent: -1)
                    }

                case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_ONE_BUFFER:
                    var content: [UInt8] = [0x02 , 0x00]
                    if cur4KPackage == upgradeFileContentList.count - 1,
                       curPackage == upgradeFileContentList[cur4KPackage].count {
                        content[1] = 0x01
                        writeArray(content)
                        isWaitResponse = true
                    } else {
                        curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_CUR_PACKAGE
                        continue
                    }

                case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_CHECK_ONE_BUFFER_CRC:
                    if upgradeFileContentList.count > cur4KPackage {
                        let crc = all4KPackageCrc[cur4KPackage]
                        var content: [UInt8] = [0x03, UInt8(crc & 0xFF)]
                        writeArray(content)
                        isWaitResponse = true
                    } else {
                        isStartUpgrade = false
                        callback?.onUpgradeStatus(status: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR, percent: -1)
                    }

                case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_FLASH:
                    isWaitResponse = true
                    writeArray([0x04])

                case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_CHECK_ALL_CRC:
                    let allCrc = self.calCrc(calArray: Array(allBytes[4...]), len: allBytes.count - 4)
                    var content: [UInt8] = [0x05, UInt8(allCrc & 0xFF)]
                    writeArray(content)
                    isWaitResponse = true

                case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_END:
                    isWaitResponse = true
                    writeArray([0x06])

                case A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_CUR_PACKAGE:
                    if upgradeFileContentList.count > cur4KPackage {
                        let curItem = upgradeFileContentList[cur4KPackage]
                        if curItem.count > curPackage {
                            let curWrite = curItem[curPackage]
                            writeDataArray(curWrite)
                            curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_ONE_BUFFER
                            curPackage += 1
                        } else {
                            curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_CHECK_ONE_BUFFER_CRC
                            isWaitResponse = false
                            continue
                        }
                    } else {
                        isStartUpgrade = false
                        callback?.onUpgradeStatus(status: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR, percent: -1)
                    }

                default:
                    break
                }
                Thread.sleep(forTimeInterval: 0.003)
            } catch {
                print("Error in checkTaskStatus: \(error)")
            }
        }
    }

    private func writeDataArray(_ content: [UInt8]) {
        lastWriteDate = Date()
        TftBleConnectManager.getInstance().writeUpgradePackageDataArray(content: content)
    }

    private func writeArray(_ content: [UInt8]) {
        lastWriteDate = Date()
        TftBleConnectManager.getInstance().writeUpgradeCmdDataArray(content: content)
    }
 
    private var mac: String?
    private var name: String?

    public func startUpgrade(path: String) {
        resetStatus()
        do {
            allBytes = try readFileToByteArray(filePath: path)
            splitSrcData()
            isStartUpgrade = true
        } catch {
            callback?.onUpgradeStatus(status: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR, percent: 0)
        }
    }

    public func stopUpgrade() {
        isStartUpgrade = false
    }

    public func getStartUpgrade() -> Bool {
        return isStartUpgrade
    }
    private func splitSrcData() {
        let allLen = allBytes.count
        deviceTypeHead = Array(allBytes[0..<4])
        var index = 4
        while index < allLen {
            let end = min(index + 4096, allLen)
            let curItem = Array(allBytes[index..<end])
            parse4KBytes(item: curItem)
            index += 4096
        }
    }

    private func parse4KBytes(item: [UInt8]) {
        let allLen = item.count
        var result: [[UInt8]] = []
        var index = 0
        while index < allLen {
            let end = min(index + onePackLen, allLen)
            let curItem = Array(item[index..<end])
            result.append(curItem)
            index += onePackLen
        }
        let crc = self.calCrc(calArray: item, len: item.count)
        all4KPackageCrc.append(crc)
        upgradeFileContentList.append(result)
    }
    
    private func readFileToByteArray(filePath: String) throws -> [UInt8] {
        let fileURL = URL(fileURLWithPath: filePath)
        guard FileManager.default.fileExists(atPath: filePath) else {
            callback?.onUpgradeStatus(status: A001SoftwareUpgradeManager.STATUS_OF_FINE_NOT_FIND, percent: 0)
            return []
        }
        let fileAttributes = try FileManager.default.attributesOfItem(atPath: filePath)
        guard let fileSize = fileAttributes[.size] as? Int, fileSize <= Int.max else {
            callback?.onUpgradeStatus(status: A001SoftwareUpgradeManager.STATUS_OF_FINE_NOT_FIND, percent: 0)
            return []
        }
        return try Data(contentsOf: fileURL).map { $0 }
    }
    
    
    
    public func resetStatus() {
        curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_START
        curPackage = 0
        cur4KPackage = 0
        upgradeFileContentList.removeAll()
        all4KPackageCrc.removeAll()
        deviceTypeHead = []
        allBytes = []
        isSendFirst = false
        sendPackageErrorCount = 0
        restartFromHeadCount = 0
    }

    private func returnToHeadRestartUpgrade() {
        curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_START
        isSendFirst = false
        curPackage = 0
        cur4KPackage = 0
        sendPackageErrorCount = 0
    }

    public func stopService() {
        isStop = true
    }
    
    func receiveCmdResp(_ data: [UInt8]) {
        guard isStartUpgrade else { return }
        
        if data.count < 2 {
            callback?.onUpgradeStatus(status: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR, percent: 0)
            return
        }
        
        switch data[0] {
        case 0x81:
            if data[1] == 0x01 {
                updateProgress()
                curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_ONE_BUFFER
                isWaitResponse = false
            } else if data[1] == 0x02 {
                isStartUpgrade = false
                callback?.onUpgradeStatus(status: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR, percent: -1)
            } else {
                doErrorCtrl(nextStep: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_START)
            }
            
        case 0x82:
            if data[1] == 0x01 {
                if !isSendFirst {
                    isSendFirst = true
                    curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_CUR_PACKAGE
                } else {
                    if upgradeFileContentList.count > cur4KPackage {
                        let curItem = upgradeFileContentList[cur4KPackage]
                        if curItem.count > curPackage {
                            curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_CUR_PACKAGE
                        } else {
                            curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_CHECK_ONE_BUFFER_CRC
                        }
                    }
                }
                updateProgress()
                isWaitResponse = false
            } else {
                curPackage -= 1
                if curPackage < 0 {
                    cur4KPackage -= 1
                    if cur4KPackage < 0 {
                        cur4KPackage = 0
                    }
                }
                doErrorCtrl(nextStep: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_ONE_BUFFER)
            }
            
        case 0x83:
            if data[1] == 0x01 {
                cur4KPackage += 1
                curPackage = 0
                curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_FLASH
                updateProgress()
                isWaitResponse = false
            } else {
                curPackage = 0
                doErrorCtrl(nextStep: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_ONE_BUFFER)
            }
            
        case 0x84:
            if data[1] == 0x01 {
                if upgradeFileContentList.count > cur4KPackage {
                    curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_ONE_BUFFER
                    updateProgress()
                } else {
                    curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_CHECK_ALL_CRC
                }
                isWaitResponse = false
            } else {
                cur4KPackage -= 1
                curPackage = 0
                updateProgress()
                doErrorCtrl(nextStep: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_ONE_BUFFER)
            }
            
        case 0x85:
            if data[1] == 0x01 {
                curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_END
                updateProgress()
                isWaitResponse = false
            } else {
                restartFromHeadCount += 1
                returnToHeadRestartUpgrade()
                isWaitResponse = false
            }
            
        case 0x86:
            if data[1] == 0x01 {
                curStep = A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_SUCC
                updateProgress()
            } else {
                restartFromHeadCount += 1
                returnToHeadRestartUpgrade()
                isWaitResponse = false
            }
            
        default:
            break
        }
    }

    private func updateProgress() {
        var allPackage = 0
        for item in upgradeFileContentList {
            allPackage += item.count
        }
        let percent = Float(cur4KPackage * 17 + curPackage) / Float(allPackage) * 100
        callback?.onUpgradeStatus(status: curStep, percent: percent)
    }

    private func doErrorCtrl(nextStep: Int) {
        sendPackageErrorCount += 1
        if sendPackageErrorCount > 10 {
            restartFromHeadCount += 1
            sendPackageErrorCount = 0
            if restartFromHeadCount > 3 {
                isStartUpgrade = false
                callback?.onUpgradeStatus(status: A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR, percent: -1)
            } else {
                returnToHeadRestartUpgrade()
                isWaitResponse = false
            }
        } else {
            curStep = nextStep
            isWaitResponse = false
        }
    }
    private var callback: UpgradeStatusCallback?
    private var isStartUpgrade = false

    init( callback: UpgradeStatusCallback) {
        
        self.callback = callback
        let workerThread = Thread(target: self, selector: #selector(checkTaskStatus), object: nil)
        workerThread.start()
        TftBleConnectManager.getInstance().setUpgradeCallback(activityName: "A001SoftwareUpgradeManager", callback: self)
    }
 
 
    private func calCrc(calArray: [UInt8], len: Int) -> Int {
        var crc = 0xff
        for j in 0..<len {
            crc = crc ^ Int(calArray[j])
            for _ in 0..<8 {
                if (crc & 0x80) == 0x80 {
                    crc = (crc << 1) ^ 0x31
                } else {
                    crc = crc << 1
                }
            }
        }
        return crc & 0xff
    }
  

}
