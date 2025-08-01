//
//  FileTools.swift
//  Swift下载文件
//
//  Created by majianjie on 2017/1/11.
//  Copyright © 2017年 majianjie. All rights reserved.
//

import UIKit

private let kCachePath = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.cachesDirectory, FileManager.SearchPathDomainMask.userDomainMask, true).first
private let kTempPath = NSTemporaryDirectory()

class DownLoader: NSObject  {

    fileprivate var downLoadedPath : String?
    fileprivate var downLoadingPath : String?
    fileprivate var outputStream : OutputStream?
    fileprivate var tmpSize : CLongLong = 0
    fileprivate var totalSize : CLongLong = 0
    
    
    fileprivate lazy var session : URLSession  = {
        
        let config = URLSessionConfiguration.default
        let session = URLSession(configuration: config, delegate: self, delegateQueue: OperationQueue.main)
        return session
        
    }()
    
    func downLoader(url : NSURL) {
        
        let fileName = url.lastPathComponent

        guard url.lastPathComponent != nil else {
            print("url有问题")
            return
        }
        
        self.downLoadingPath = kTempPath + "/" + fileName!
        self.downLoadedPath = kCachePath! + "/" + fileName!
        
        //检查当前路径是否已经下载了该文件
        if FileTool.fileExists(filePath: self.downLoadedPath!) {
            print("文件以及下载完成")
            return
        }
        
        print(self.downLoadingPath ?? "")
        
        //如果没有下载完成 就看是否有临时文件
        if !FileTool.fileExists(filePath: self.downLoadingPath!) {
            //不存在的话 直接开始下载
            self.downLoadWithURL(url as URL, 0)
            
            return;
        }
        
        
        //已经下载了的 先计算 下载的大小,然后继续下载
        tmpSize = FileTool.fileSize(self.downLoadingPath!)
        self.downLoadWithURL(url as URL, tmpSize)
        
    }
    
    
    // MARK:- 开始请求资源
    func downLoadWithURL(_ url : URL, _ offset : CLongLong) {
        
        var request = NSURLRequest(url: url, cachePolicy: NSURLRequest.CachePolicy.reloadIgnoringCacheData, timeoutInterval: 0) as URLRequest
        
        request.setValue("bytes=%lld-", forHTTPHeaderField: "Range")
        
       let dataTask = self.session.dataTask(with: request)
        
        dataTask.resume()
    }
    
    
}


extension DownLoader : URLSessionDataDelegate {
    
    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive response: URLResponse, completionHandler: @escaping (URLSession.ResponseDisposition) -> Swift.Void){
        
        print(response)
        let resp = response as! HTTPURLResponse
//        self.totalSize = resp.allHeaderFields["Content-Length"] as! CLongLong?
        
//        self.totalSize = String(resp.allHeaderFields["Content-Length"]).components(separatedBy: "/").last).CLongLong
        
        let string = resp.allHeaderFields["Content-Length"] as! String
        
        let stri : String = string.components(separatedBy: "/").last!
                
        self.totalSize = CLongLong(stri)!
        
        
        
       // 比对本地大小, 和 总大小
        if (self.tmpSize == self.totalSize) {
            
            // 1. 移动到下载完成文件夹
            print("移动文件到下载完成")
            FileTool.moveFile(self.downLoadingPath!, self.downLoadedPath!)
            // 2. 取消本次请求
            completionHandler(URLSession.ResponseDisposition.cancel);
            return;
        }
        
        if (self.tmpSize > self.totalSize) {
            
            // 1. 删除临时缓存
            print("删除临时缓存")
            FileTool.removeFile(self.downLoadingPath!)
            
            
            // 2. 从0 开始下载
            print("重新开始下载")
            self.downLoader(url: resp.url! as NSURL)
            //             [self downLoadWithURL:response.URL offset:0];
            // 3. 取消请求
            completionHandler(URLSession.ResponseDisposition.cancel);
            
            return;
            
        }
        
        
        
        // 继续接受数据
        // 确定开始下载数据
        self.outputStream = OutputStream(toFileAtPath: self.downLoadingPath!, append: true)
        
        self.outputStream?.open()
        completionHandler(URLSession.ResponseDisposition.allow);
        
        
        
    }
    
    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive data: Data){
        
        var buffer = [UInt8](repeating: 0, count: data.count)
        data.copyBytes(to: &buffer, count: data.count)
        
        self.outputStream?.write(buffer, maxLength: data.count)

        
//        let uint8Ptr = UnsafeMutablePointer<UInt8>.allocate(capacity: data.count)
//        uint8Ptr.initialize(from: data)
//        let rawPtr = UnsafeRawPointer(uint8Ptr)
//        self.outputStream?.write(rawPtr, maxLength: da.length)

    }
    
    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?){
        
        print("请求完成")
        
        
        if (error == nil) {
            
            // 不一定是成功
            // 数据是肯定可以请求完毕
            // 判断, 本地缓存 == 文件总大小 {filename: filesize: md5:xxx}
            // 如果等于 => 验证, 是否文件完整(file md5 )
            
            //
           
        }else {
            print("有问题")
        }
        
        self.outputStream?.close()
        
        
    }
    
    
    
}
