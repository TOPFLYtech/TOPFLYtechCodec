//
//  UniqueIDTool.swift
//  TFT Elock
//
//  Created by china topflytech on 2022/4/26.
//  Copyright © 2022 com.tftiot. All rights reserved.
//

import Foundation
import SAMKeychain
import UIKit
 
let appAccountKey = "tftElockKey"
public extension UIDevice {
/**
        从钥匙串读取idfv
     */
    var keychainIdfv: String {
        
        let idfv = identifierForVendor?.uuidString
        // is exist
        let lastKeyChianIdfv = SAMKeychain.password(forService: Bundle.main.bundleIdentifier!, account: appAccountKey)
        if lastKeyChianIdfv?.count ?? 0 > 0 {
            
            return lastKeyChianIdfv ?? ""
        }else {
            // save to keychain
            SAMKeychain.setPassword(idfv ?? "", forService: Bundle.main.bundleIdentifier!, account: appAccountKey)
        }
        return idfv ?? ""
    }
}
import CommonCrypto
public extension String {
    /* ################################################################## */
    /**
     - returns: the String, as an MD5 hash.
     */
    var md5: String {
        let str = self.cString(using: String.Encoding.utf8)
        let strLen = CUnsignedInt(self.lengthOfBytes(using: String.Encoding.utf8))
        let digestLen = Int(CC_MD5_DIGEST_LENGTH)
        let result = UnsafeMutablePointer<CUnsignedChar>.allocate(capacity: digestLen)
        CC_MD5(str!, strLen, result)

        let hash = NSMutableString()

        for i in 0..<digestLen {
            hash.appendFormat("%02x", result[i])
        }

        result.deallocate()
        return hash as String
    }
   
}
class UniqueIDTool{

    static func getMediaDrmID()->String{
        let keychainIdfv = UIDevice.current.keychainIdfv
        let md5 = keychainIdfv.md5
        var value:[UInt8] = [UInt8](repeating: 0, count: 6)
        for (i,char) in md5.enumerated(){
            value[i%6]  += UInt8(Utils.characterToInt(value: char))
        }
        value[0] += 17//The value of the first plus the Android flag a is 10,ios flag i is 17;
        
        return Utils.bytes2HexString(bytes: value, pos: 0)
        
    }
   
}

