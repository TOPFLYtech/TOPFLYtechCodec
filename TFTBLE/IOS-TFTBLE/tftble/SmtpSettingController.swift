//
//  SmtpSettingController.swift
//  tftble
//
//  Created by jeech on 2020/4/14.
//  Copyright © 2020 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import MessageUI

class SmtpSettingController :UIViewController,MFMailComposeViewControllerDelegate{
    private var smtpServerLabel:UILabel!
    private var smtpServerEd:UITextField!
    private var smtpPortLabel:UILabel!
    private var smtpPortEd:UITextField!
    private var emailLabel:UILabel!
    private var emailEd:UITextField!
    private var senderNameLabel:UILabel!
    private var senderNameEd:UITextField!
    private var pwdLabel:UILabel!
    private var pwdEd:UITextField!
    private var receiveEmailLabel:UILabel!
    private var receiveEmailEd:UITextField!
    private var submitBtn:UIButton!
    
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
        self.tftAdjustContentBelowTopBarIfNeeded()
        self.tftEnsureTopContentClearance(padding: 8)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.white
        self.tftInstallTopBar(title: "Email setting")
        self.initUI()
    }
    
    private func initUI(){
        let viewWidth = Int(self.view.bounds.width)
        let navBottomY = Int(self.tftTopContentY())
        self.tftRememberInitialContentTop(CGFloat(navBottomY))
        let descWidth = Int(CGFloat(viewWidth) / 3.0) - 5
        let inputWidth = viewWidth - descWidth - 20
        let contentX = descWidth + 10
        let labelHight:Int = 50
        let descX = 5
        let inputHeight = 30
        var startLabelY:Int = navBottomY + 10
        var startInputY:Int = navBottomY + 25
        let lineHight:Int = 60
        var lineY:Int = navBottomY + 70
        smtpServerLabel = UILabel()
        self.initLabel(label: self.smtpServerLabel, content: "SMTP server:", x: descX, y: startLabelY, width: descWidth, height: labelHight, containView: self.view)
        smtpServerEd = UITextField()
        self.smtpServerEd.textColor = UIColor.black
        self.initTextField(input: smtpServerEd, x: contentX, y: startInputY, width: Int(inputWidth), height: inputHeight, contailView: self.view)
        self.initLineSplit(y: lineY, containView: self.view)
        startLabelY += lineHight
        lineY += lineHight
        startInputY += lineHight
        
        smtpPortLabel = UILabel()
        self.initLabel(label: self.smtpPortLabel, content: "SMTP port:", x: descX, y: startLabelY, width: descWidth, height: labelHight, containView: self.view)
        smtpPortEd = UITextField()
        self.smtpPortEd.textColor = UIColor.black
        self.initTextField(input: smtpPortEd, x: contentX, y: startInputY, width: Int(inputWidth), height: inputHeight, contailView: self.view)
        self.initLineSplit(y: lineY, containView: self.view)
        startLabelY += lineHight
        lineY += lineHight
        startInputY += lineHight
        
        emailLabel = UILabel()
        self.initLabel(label: self.emailLabel, content: "Email:", x: descX, y: startLabelY, width: descWidth, height: labelHight, containView: self.view)
        emailEd = UITextField()
        self.emailEd.textColor = UIColor.black
        self.initTextField(input: emailEd, x: contentX, y: startInputY, width: Int(inputWidth), height: inputHeight, contailView: self.view)
        self.initLineSplit(y: lineY, containView: self.view)
        startLabelY += lineHight
        lineY += lineHight
        startInputY += lineHight
        
        senderNameLabel = UILabel()
        self.initLabel(label: self.senderNameLabel, content: "Senders name:", x: descX, y: startLabelY, width: descWidth, height: labelHight, containView: self.view)
        senderNameEd = UITextField()
        self.senderNameEd.textColor = UIColor.black
        self.initTextField(input: senderNameEd, x: contentX, y: startInputY, width: Int(inputWidth), height: inputHeight, contailView: self.view)
        self.initLineSplit(y: lineY, containView: self.view)
        startLabelY += lineHight
        lineY += lineHight
        startInputY += lineHight
        
        receiveEmailLabel = UILabel()
        self.initLabel(label: self.receiveEmailLabel, content: "Receive email(Split by ;):", x: descX, y: startLabelY, width: descWidth, height: labelHight+20, containView: self.view)
        receiveEmailEd = UITextField()
        self.receiveEmailEd.textColor = UIColor.black
        self.initTextField(input: receiveEmailEd, x: contentX, y: startInputY+10, width: Int(inputWidth), height: inputHeight, contailView: self.view)
        self.initLineSplit(y: lineY+20, containView: self.view)
        startLabelY += lineHight+20
        lineY += lineHight+20
        startInputY += lineHight+20
        
        self.submitBtn = UIButton(type: .system)
        self.submitBtn.setTitle("Submit", for: .normal)
        self.submitBtn.setTitleColor(UIColor.black, for: .normal)
        self.submitBtn.layer.cornerRadius = 12.0
        self.submitBtn.layer.borderWidth = 1.0;//设置边框宽度
        self.submitBtn.backgroundColor = UIColor.nordicLightGray
        self.submitBtn.frame = CGRect(x: Int((viewWidth - 200) / 2), y: startLabelY + 10, width: 200, height: 30)
        self.submitBtn.addTarget(self, action: #selector(submit), for:.touchUpInside)
        self.view.addSubview(self.submitBtn)
    }
    
    @objc private func submit(){
        if !MFMailComposeViewController.canSendMail() {
        //不支持发送邮件
            print("不支持发送邮件")
        }
        else {
         //支持发送邮件
            print("支持发送邮件")
             let mail = MFMailComposeViewController()
             mail.navigationBar.tintColor = UIColor.blue //导航颜色
             mail.setToRecipients(["1024919409@qq.com"]) //设置收件地址
             mail.mailComposeDelegate = self //代理
            
             mail.setSubject("test tftble ")
             //发送文字
             mail.setMessageBody("test tftble", isHTML: false) //邮件主体内容
             //发送图片
            let path = Bundle.main.path(forResource: "log", ofType: "txt")
            let url = URL(fileURLWithPath: path!)
            do {
                
                let data = try Data(contentsOf: url)
                mail.addAttachmentData(data as Data, mimeType: "", fileName: "log.txt")
                
                 present(mail, animated: true, completion: nil)
            } catch let error as Error! {
                    print("读取本地数据出现错误!",error)
                }
             
        }
    }
    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        if result == MFMailComposeResult.sent {
            //发送成功
        }
        else if result == MFMailComposeResult.cancelled {
            //取消发送
        }
        else if result == MFMailComposeResult.failed {
            //发送失败
        }
        else {
            //已保存
        }
        dismiss(animated: true, completion: nil)
    }
    func initLineSplit(y:Int,containView:UIView){
        let smtpServer = UIView()
        smtpServer.backgroundColor = UIColor.gray
        smtpServer.frame = CGRect(x: 0, y: Double(y), width: Double(containView.bounds.width), height: 0.5)
        containView.addSubview(smtpServer)
    }
    
    func initLabel(label:UILabel,content:String,x:Int,y:Int,width:Int,height:Int,containView:UIView){
        label.frame = CGRect(x: x, y: y, width: width, height: height)
        label.lineBreakMode = NSLineBreakMode.byWordWrapping
        label.numberOfLines = 0
        label.text = content
        containView.addSubview(label)
    }
    
    func initTextField(input:UITextField,x:Int,y:Int,width:Int,height:Int,contailView:UIView){
        input.isSecureTextEntry = true
        input.borderStyle = UITextField.BorderStyle.bezel
        input.frame = CGRect(x:x,y:y,width:width,height: height)
        contailView.addSubview(input)
    }
    
}
