//
//  FileTool.swift
//  Swift下载文件
//
//  Created by majianjie on 2017/1/11.
//  Copyright © 2017年 majianjie. All rights reserved.
//

import UIKit

class FileTool: NSObject {

    
    // MARK:- 判断文件目录是否存在
    class func fileExists(filePath : String) -> Bool {
        
        if (filePath.characters.count == 0) {
            return false
        }
        return FileManager.default.fileExists(atPath: filePath)
    }
    
    // MARK:- 获取文件的大小
    class func  fileSize(_ filePath : String) ->CLongLong{
    
        if !self.fileExists(filePath: filePath) {
            return 0
        }
        
        let fileInfo = try! FileManager.default.attributesOfItem(atPath: filePath)
        
        return fileInfo[FileAttributeKey.size] as! CLongLong
        
    }
    
    // MARK:- 移动文件
    class func moveFile(_ fromPath : String , _ toPath : String){
        
        if self.fileSize(fromPath)  == 0 {
            return
        }
        try! FileManager.default.moveItem(atPath: fromPath, toPath: toPath)
    }
    
    class func removeFile(_ filePath : String){
       try! FileManager.default.removeItem(atPath: filePath)
    }
    
    
}
