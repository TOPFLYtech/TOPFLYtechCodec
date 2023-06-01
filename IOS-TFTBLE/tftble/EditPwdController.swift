//
//  EditPwdController.swift
//  tftble
//
//  Created by jeech on 2019/12/26.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import CLXToast
import QMUIKit
class EditPwdController : UIViewController{
    private var oldPwdLabel:UILabel!
    private var oldPwdInput:UITextField!
    private var newPwdLabel:UILabel!
    private var newPwdInput:UITextField!
    private var repeatPwdLabel:UILabel!
    private var repeatPwdInput:UITextField!
    private var submitBtn:QMUIGhostButton!
    var connectStatusDelegate:SetConnectStatusDelegate?
    var oldPwd:String = ""
    private var fontSize:CGFloat = Utils.fontSize
    override func viewDidDisappear(_ animated: Bool) {
        self.connectStatusDelegate?.setConnectStatus()
    }
    @objc func dismissKeyboard()
       {
           view.endEditing(true)
       }
    override func viewDidLoad() {
        self.view.backgroundColor = UIColor.white
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = NSLocalizedString("edit_password", comment: "Edit password")
        self.navigationItem.titleView = titleLabel
        let descWidth = Int(KSize.width / 2)
        let contentX = Int(KSize.width / 2 + 10)
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(
                    target: self,
                    action: #selector(dismissKeyboard))

                tap.cancelsTouchesInView = false
                view.addGestureRecognizer(tap)
        
        var startLabelY:Int = 60
        var startInputY:Int = 75
        let lineHigh:Int = 60
        var lineY:Int = 120
        
        self.oldPwdLabel = UILabel()
        self.oldPwdLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.oldPwdLabel.textColor = UIColor.black
        self.oldPwdLabel.text = NSLocalizedString("old_password_desc", comment: "Old password:")
        self.oldPwdLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.oldPwdLabel.numberOfLines = 0;
        self.oldPwdLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.oldPwdLabel)
        self.oldPwdInput = UITextField()
        self.oldPwdInput.font = UIFont.systemFont(ofSize: fontSize)
        self.oldPwdInput.textColor = UIColor.black
        self.oldPwdInput.isSecureTextEntry = true
        self.oldPwdInput.borderStyle = UITextField.BorderStyle.bezel
        self.oldPwdInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.view.addSubview(self.oldPwdInput)
        let oldPwdLine = UIView()
        oldPwdLine.backgroundColor = UIColor.gray
        oldPwdLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(oldPwdLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.newPwdLabel = UILabel()
        self.newPwdLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.newPwdLabel.textColor = UIColor.black
        self.newPwdLabel.text = NSLocalizedString("new_password_desc", comment: "New Password:")
        self.newPwdLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.newPwdLabel.numberOfLines = 0;
        self.newPwdLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.newPwdLabel)
        self.newPwdInput = UITextField()
        self.newPwdInput.font = UIFont.systemFont(ofSize: fontSize)
        self.newPwdInput.textColor = UIColor.black
        self.newPwdInput.isSecureTextEntry = true
        self.newPwdInput.borderStyle = UITextField.BorderStyle.bezel
        self.newPwdInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.view.addSubview(self.newPwdInput)
        let newPwdLine = UIView()
        newPwdLine.backgroundColor = UIColor.gray
        newPwdLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(newPwdLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.repeatPwdLabel = UILabel()
        self.repeatPwdLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.repeatPwdLabel.textColor = UIColor.black
        self.repeatPwdLabel.text = NSLocalizedString("repeat_password_desc", comment: "Repeat Password:")
        self.repeatPwdLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.repeatPwdLabel.numberOfLines = 0;
        self.repeatPwdLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.repeatPwdLabel)
        self.repeatPwdInput = UITextField()
        self.repeatPwdInput.font = UIFont.systemFont(ofSize: fontSize)
        self.repeatPwdInput.isSecureTextEntry = true
        self.repeatPwdInput.textColor = UIColor.black
        self.repeatPwdInput.borderStyle = UITextField.BorderStyle.bezel
        self.repeatPwdInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.view.addSubview(self.repeatPwdInput)
        let repeatPwdLine = UIView()
        repeatPwdLine.backgroundColor = UIColor.gray
        repeatPwdLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(repeatPwdLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.submitBtn = QMUIGhostButton()
        self.submitBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.submitBtn.setTitle(NSLocalizedString("submit", comment: "Submit"), for: .normal)
        self.submitBtn.ghostColor = UIColor.colorPrimary 
        self.submitBtn.frame = CGRect(x: Int((KSize.width - 200) / 2), y: startLabelY + 10, width: 200, height: 30)
        self.submitBtn.addTarget(self, action: #selector(submit), for:.touchUpInside)
        self.view.addSubview(self.submitBtn)
    }
    var delegate : EditPwdDelegate?
    @objc private func submit(){
        let oldPwd = self.oldPwdInput.text ?? ""
        let newPwd = self.newPwdInput.text ?? ""
        let repeatPwd = self.repeatPwdInput.text ?? ""
        if oldPwd.count != 6 || newPwd.count != 6 || repeatPwd.count != 6 {
            Toast.hudBuilder.title(NSLocalizedString("pwd_value_error_warning", comment: "Value is incorrect!The length has to be 6 digits")).show()
            return
        }
        if oldPwd != self.oldPwd{
            Toast.hudBuilder.title(NSLocalizedString("old_password_error", comment: "Old password is wrong, please re-enter!")).show()
            return
        }
        if newPwd != repeatPwd{
            Toast.hudBuilder.title(NSLocalizedString("repeat_pwd_not_match", comment: "Repeat password is not patch, please re-enter!")).show()
            return
        }
        self.delegate?.setNewPwd(newPwd: newPwd)
        self.navigationController?.popViewController(animated: false)
    }
}
