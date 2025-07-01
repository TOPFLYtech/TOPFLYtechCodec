//
//  TempAlarmController.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/4/16.
//  Copyright © 2025 com.tftiot. All rights reserved.
//


import Foundation

import UIKit
import CLXToast
class TempAlarmController : UIViewController, UITextFieldDelegate{
    private var tempHighLabel:UILabel!
    private var tempHighInput:UITextField!
    private var tempLowLabel:UILabel!
    private var tempLowInput:UITextField!
    private var submitBtn:UIButton!
    private var fontSize:CGFloat = CGFloat(Utils.fontSize)
    public var tempHigh:Float!
    public var tempLow:Float!
    @objc func dismissKeyboard()
       {
           view.endEditing(true)
       }
    override func viewDidLoad() {
        self.view.backgroundColor = UIColor.white
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = NSLocalizedString("temp_alarm_set", comment: "temp_alarm_set")
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
        
        self.tempHighLabel = UILabel()
        self.tempHighLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.tempHighLabel.textColor = UIColor.black
        self.tempHighLabel.text = NSLocalizedString("high", comment: "high")
        self.tempHighLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.tempHighLabel.numberOfLines = 0;
        self.tempHighLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.tempHighLabel)
        self.tempHighInput = UITextField()
        self.tempHighInput.font = UIFont.systemFont(ofSize: fontSize)
        self.tempHighInput.textColor = UIColor.black
        self.tempHighInput.borderStyle = UITextField.BorderStyle.bezel
        self.tempHighInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.tempHighInput.text = tempHigh != nil ? String(Utils.getCurTemp(sourceTemp: tempHigh / 100)) ?? "" : ""
        self.tempHighInput.delegate = self
        self.view.addSubview(self.tempHighInput)
        let tempHighLine = UIView()
        tempHighLine.backgroundColor = UIColor.gray
        tempHighLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(tempHighLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.tempLowLabel = UILabel()
        self.tempLowLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.tempLowLabel.textColor = UIColor.black
        self.tempLowLabel.text = NSLocalizedString("low", comment: "low")
        self.tempLowLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.tempLowLabel.numberOfLines = 0;
        self.tempLowLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.tempLowLabel)
        self.tempLowInput = UITextField()
        self.tempLowInput.font = UIFont.systemFont(ofSize: fontSize)
        self.tempLowInput.textColor = UIColor.black
        self.tempLowInput.borderStyle = UITextField.BorderStyle.bezel
        self.tempLowInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.tempLowInput.text = tempLow != nil ? String(Utils.getCurTemp(sourceTemp: tempLow / 100)) ?? "" : ""
        self.tempLowInput.delegate = self
        self.view.addSubview(self.tempLowInput)
        let tempLowLine = UIView()
        tempLowLine.backgroundColor = UIColor.gray
        tempLowLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(tempLowLine)
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
    
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        // 允许的字符集：数字、小数点和负号
        let allowedCharacters = CharacterSet(charactersIn: "0123456789.-")
        let characterSet = CharacterSet(charactersIn: string)
        
        // 检查输入的字符是否在允许的字符集中
        guard allowedCharacters.isSuperset(of: characterSet) else {
            return false
        }
        
        // 获取当前文本框中的完整文本
        let currentText = (textField.text ?? "") as NSString
        let updatedText = currentText.replacingCharacters(in: range, with: string)
        
        // 检查是否包含多个小数点
        if updatedText.components(separatedBy: ".").count > 2 {
            return false
        }
        
        // 检查是否包含多个负号
        if updatedText.components(separatedBy: "-").count > 2 {
            return false
        }
        
        // 确保负号只能出现在开头
        if updatedText.contains("-") && !updatedText.hasPrefix("-") {
            return false
        }
        
        return true
    }
    
    var delegate : EditTempAlarmDelegate?
    @objc private func submit(){
        let tempHighStr = self.tempHighInput.text ?? ""
        let tempLowStr = self.tempLowInput.text ?? ""
      // 检查输入是否为空
        if tempHighStr.isEmpty || tempLowStr.isEmpty   {
            Toast.hudBuilder.title(NSLocalizedString("fix_input", comment: "please fix your input")).show()
            return
        }
        let tempHighValue = Float(tempHighStr) ?? -1
        let tempLowValue = Float(tempLowStr) ?? -1
        let tempHighCelsius = Int(Utils.getSourceTemp(sourceTemp: tempHighValue * 100))
        let tempLowCelsius = Int(Utils.getSourceTemp(sourceTemp: tempLowValue * 100))
        if tempHighCelsius <= tempLowCelsius{
            Toast.hudBuilder.title(NSLocalizedString("temp_high_low_warning", comment: "temp_high_low_warning")).show()
            return
        }
        if tempHighValue < -4000 || tempHighValue > 8500 || tempLowCelsius < -4000 || tempLowCelsius > 8500{
            if Utils.getCurTempUnit() == "˚C"{
                Toast.hudBuilder.title(NSLocalizedString("temp_range_warning_1", comment: "temp_range_warning_1")).show()
                return
            }else{
                Toast.hudBuilder.title(NSLocalizedString("temp_range_warning_2", comment: "temp_range_warning_2")).show()
                return
            }
          
        }
 
 
        print("tempHigh:\(tempHighCelsius);tempLow:\(tempLowCelsius) ")
        self.delegate?.setNewTemp(tempHigh: tempHighCelsius, tempLow: tempLowCelsius )
        self.navigationController?.popViewController(animated: false)
    }
}
