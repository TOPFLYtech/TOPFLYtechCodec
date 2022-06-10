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
