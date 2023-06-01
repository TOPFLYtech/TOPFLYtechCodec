//
//  EditPwdDelegate.swift
//  tftble
//
//  Created by jeech on 2019/12/26.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
protocol EditPwdDelegate {
    func setNewPwd(newPwd: String)
}

protocol SetConnectStatusDelegate {
    func setConnectStatus() 
}

protocol EditPositiveNegativeWarningValueDelegate {
    func setPositiveNegativeWarningValue(port:Int,mode:Int,highVoltage:Int,lowVoltage:Int,ditheringIntervalHigh:Int,
                    ditheringIntervalLow:Int,samplingInterval:Int)
}
protocol EditDoutOutputDelegate{
    func setDoutValue(dout0:Int,dout1:Int)
}

protocol EditInstructionSequenceDelegate{
    func setCmd(cmd:String)
}
protocol EditRS485CmdDelegate{
    func setRS485Cmd(cmd:String)
}
