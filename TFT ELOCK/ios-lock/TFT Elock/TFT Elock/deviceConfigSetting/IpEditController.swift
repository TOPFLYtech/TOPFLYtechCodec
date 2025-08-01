//
//  IpEditController.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/2/20.
//  Copyright © 2025 com.tftiot. All rights reserved.
//

import Foundation

import UIKit
import CLXToast 
class IpEditController : UIViewController{
    private var ipLabel:UILabel!
    private var ipInput:UITextField!
    private var portLabel:UILabel!
    private var portInput:UITextField!
    private var submitBtn:UIButton!
    public var ipType:Int!
    public var domain:String!
    public var portStr:String!
    private var fontSize:CGFloat = CGFloat(Utils.fontSize)
 
    @objc func dismissKeyboard()
       {
           view.endEditing(true)
       }
    override func viewDidLoad() {
        self.view.backgroundColor = UIColor.white
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = NSLocalizedString("network", comment: "network")
        self.navigationItem.titleView = titleLabel
        let descWidth = Int(KSize.width / 2)
        let contentX = Int(KSize.width / 2 )
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(
                    target: self,
                    action: #selector(dismissKeyboard))

                tap.cancelsTouchesInView = false
                view.addGestureRecognizer(tap)
        
        var startLabelY:Int = 0
        var startInputY:Int = 15
        let lineHigh:Int = 50
        var lineY:Int = 50
        let labelHeight = 50
        let inputHeight = 30
        
        self.ipLabel = UILabel()
        self.ipLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.ipLabel.textColor = UIColor.black
        self.ipLabel.text = NSLocalizedString("ip_domain", comment: "ip_domain")
        self.ipLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.ipLabel.numberOfLines = 0;
        self.ipLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: labelHeight)
        self.view.addSubview(self.ipLabel)
        self.ipInput = UITextField()
        self.ipInput.font = UIFont.systemFont(ofSize: fontSize)
        self.ipInput.textColor = UIColor.black
        self.ipInput.borderStyle = UITextField.BorderStyle.bezel
        self.ipInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: inputHeight)
        self.ipInput.text = domain
        self.view.addSubview(self.ipInput)
        let ipLine = UIView()
        ipLine.backgroundColor = UIColor.gray
        ipLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(ipLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.portLabel = UILabel()
        self.portLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.portLabel.textColor = UIColor.black
        self.portLabel.text = NSLocalizedString("port", comment: "port")
        self.portLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.portLabel.numberOfLines = 0;
        self.portLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: labelHeight)
        self.view.addSubview(self.portLabel)
        self.portInput = UITextField()
        self.portInput.font = UIFont.systemFont(ofSize: fontSize)
        self.portInput.textColor = UIColor.black
        self.portInput.borderStyle = UITextField.BorderStyle.bezel
        self.portInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: inputHeight)
        self.portInput.text = portStr
        self.view.addSubview(self.portInput)
        let portLine = UIView()
        portLine.backgroundColor = UIColor.gray
        portLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(portLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.submitBtn = UIButton()
        self.submitBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.submitBtn.setTitle(NSLocalizedString("confirm", comment: "confirm"), for: .normal)
//        self.submitBtn.ghostColor = UIColor.colorPrimary
        self.submitBtn.setTitleColor(UIColor.nordicBlue, for: .normal)
        self.submitBtn.layer.cornerRadius = 5;
        self.submitBtn.layer.borderWidth = 1.0;
        self.submitBtn.layer.borderColor = UIColor.nordicBlue.cgColor
        self.submitBtn.frame = CGRect(x: Int((KSize.width - 200) / 2), y: startLabelY + 10, width: 200, height: 30)
        self.submitBtn.addTarget(self, action: #selector(submit), for:.touchUpInside)
        self.view.addSubview(self.submitBtn)
    }
    var delegate : EditIpDelegate?
    @objc private func submit(){
        let domain = self.ipInput.text ?? ""
        let portStr = self.portInput.text ?? ""
      // 检查输入是否为空
            if domain.isEmpty || portStr.isEmpty {
                Toast.hudBuilder.title(NSLocalizedString("fix_input", comment: "please fix your input")).show()
                return
            }
            
            // 检查域名长度
            if domain.trimmingCharacters(in: .whitespacesAndNewlines).count >= 50 {
                Toast.hudBuilder.title(NSLocalizedString("domain_len_error", comment: "")).show()
                return
            }
            
            // 尝试将端口字符串转换为整数
            guard let port = Int(portStr) else {
                Toast.hudBuilder.title(NSLocalizedString("port_invalid", comment: "")).show()
                return
            }
            
            // 检查端口范围
            if port < 0 || port > 65535 {
                Toast.hudBuilder.title(NSLocalizedString("port_warning", comment: "")).show()
                return
            }

        self.delegate?.setNewIp(ipType:ipType,ip: domain , port: port)
        self.navigationController?.popViewController(animated: false)
    }
}
