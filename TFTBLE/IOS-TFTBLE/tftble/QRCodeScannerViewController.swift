//
//  QRCodeScannerViewController.swift
//  tftble
//
//  Created by china topflytech on 2023/12/21.
//  Copyright © 2023 com.tftiot. All rights reserved.
//

import AVFoundation
import UIKit

class QRCodeScannerViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {
    var captureSession: AVCaptureSession!
    var videoPreviewLayer: AVCaptureVideoPreviewLayer!
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        self.tftHideTopBar()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.tftShowTopBar()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        self.navigationController?.setNavigationBarHidden(true, animated: false)
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        self.tftLayoutTopBar()
        let topY = self.tftTopContentY()
        videoPreviewLayer?.frame = CGRect(x: 0, y: topY, width: view.bounds.width, height: view.bounds.height - topY)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.black
        self.tftInstallTopBar(title: NSLocalizedString("qrcode_scan", comment: "QR code scanning"))
        // 创建 AVCaptureSession 实例
        captureSession = AVCaptureSession()
        
        // 获取摄像头设备
        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else { return }
        
        // 创建 AVCaptureDeviceInput 实例
        guard let videoInput = try? AVCaptureDeviceInput(device: videoCaptureDevice) else { return }
        
        // 将输入设备添加到会话
        if captureSession.canAddInput(videoInput) {
            captureSession.addInput(videoInput)
        } else {
            return
        }
        
        // 创建 AVCaptureMetadataOutput 实例
        let metadataOutput = AVCaptureMetadataOutput()
        
        // 将输出添加到会话
        if captureSession.canAddOutput(metadataOutput) {
            captureSession.addOutput(metadataOutput)
            
            // 设置委托和扫描类型
            metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            metadataOutput.metadataObjectTypes = [.qr]
        } else {
            return
        }
        
        // 创建预览图层
        videoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        videoPreviewLayer.videoGravity = .resizeAspectFill
        let topY = self.tftTopContentY()
        videoPreviewLayer.frame = CGRect(x: 0, y: topY, width: view.bounds.width, height: view.bounds.height - topY)
        view.layer.addSublayer(videoPreviewLayer)
        
        // 启动会话
        captureSession.startRunning()
    }
    var delegate : QrCodeScanDelegate?
    // 代理方法，处理扫描结果
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        guard let metadataObject = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
              let qrCodeString = metadataObject.stringValue else {
            return
        }
        
        // 处理扫描到的二维码字符串
        print("扫描到的二维码：\(qrCodeString)")
        
        // 停止会话
        captureSession.stopRunning()
        self.delegate?.setQrcodeValue(value: qrCodeString)
        self.navigationController?.popViewController(animated: false)
    }
}
