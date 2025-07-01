//
//  EditPwdDelegate.swift
//  tftble
//
//  Created by jeech on 2019/12/26.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
protocol EditLockPwdDelegate {
    func setNewPwd(oldPwd:String,newPwd: String)
}
protocol EditIpDelegate {
    func setNewIp(ipType:Int,ip:String,port: Int)
}
protocol EditTimerDelegate {
    func setNewTimer(accOn:Int,accOff:Int64,angle:Int,distance: Int)
}
protocol EditAccessPwdDelegate {
    func setNewPwd(newPwd: String)
}

protocol SetConnectStatusDelegate {
    func setConnectStatus() 
}
 

protocol EditInstructionSequenceDelegate{
    func setCmd(cmd:String)
}
 

protocol QrCodeScanDelegate{
    func setQrcodeValue(value:String)
} 

protocol EditTempAlarmDelegate{
    func setNewTemp(tempHigh:Int,tempLow:Int)
}

protocol EditAlarmOpenDelegate{
    func setNewAlarmValue(newValue:Int)
}
