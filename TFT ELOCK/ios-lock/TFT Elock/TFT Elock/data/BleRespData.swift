//
//  BleRespData.swift
//  TFTNordicATSetting
//
//  Created by jeech on 2021/7/15.
//  Copyright © 2021 com.tftiot. All rights reserved.
//

import Foundation
public class BleRespData{
    public var type:Int?
    public static let READ_TYPE = 0
    public static let WRITE_TYPE = 1
    public static let ERROR_TYPE = 2
    public var index:Int = -1
    public var controlCode:Int?
    public var data:[UInt8]?
    public var isEnd:Bool?
    public var errorCode:Int = 0
    public static let ERROR_CODE_OF_PWD_ERROR = 1;
}
