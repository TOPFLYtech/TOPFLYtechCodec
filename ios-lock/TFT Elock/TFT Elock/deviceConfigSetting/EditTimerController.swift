//
//  EditTimerController.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/2/21.
//  Copyright © 2025 com.tftiot. All rights reserved.
//

import Foundation

import UIKit
import CLXToast 
class EditTimerController : UIViewController, UITextFieldDelegate{
    private var accOnLabel:UILabel!
    private var accOnInput:UITextField!
    private var accOffLabel:UILabel!
    private var accOffInput:UITextField!
    private var angleLabel:UILabel!
    private var angleInput:UITextField!
    private var distanceLabel:UILabel!
    private var distanceInput:UITextField!
    private var submitBtn:UIButton!
    private var fontSize:CGFloat = CGFloat(Utils.fontSize)
    public var accOn:Int!
    public var accOff:Int64!
    public var angle:Int!
    public var distance:Int!
    @objc func dismissKeyboard()
       {
           view.endEditing(true)
       }
    override func viewDidLoad() {
        self.view.backgroundColor = UIColor.white
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = NSLocalizedString("timer", comment: "timer")
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
        
        self.accOnLabel = UILabel()
        self.accOnLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.accOnLabel.textColor = UIColor.black
        self.accOnLabel.text = NSLocalizedString("acc_on", comment: "acc_on")
        self.accOnLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.accOnLabel.numberOfLines = 0;
        self.accOnLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.accOnLabel)
        self.accOnInput = UITextField()
        self.accOnInput.font = UIFont.systemFont(ofSize: fontSize)
        self.accOnInput.textColor = UIColor.black
        self.accOnInput.borderStyle = UITextField.BorderStyle.bezel
        self.accOnInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.accOnInput.text = accOn != nil ? String(accOn) ??  "" : ""
        self.accOnInput.delegate = self
        self.view.addSubview(self.accOnInput)
        let accOnLine = UIView()
        accOnLine.backgroundColor = UIColor.gray
        accOnLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(accOnLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.accOffLabel = UILabel()
        self.accOffLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.accOffLabel.textColor = UIColor.black
        self.accOffLabel.text = NSLocalizedString("acc_off", comment: "acc_off")
        self.accOffLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.accOffLabel.numberOfLines = 0;
        self.accOffLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.accOffLabel)
        self.accOffInput = UITextField()
        self.accOffInput.font = UIFont.systemFont(ofSize: fontSize)
        self.accOffInput.textColor = UIColor.black
        self.accOffInput.borderStyle = UITextField.BorderStyle.bezel
        self.accOffInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.accOffInput.text = accOff != nil ? String(accOff) ??  "" : ""
        self.accOffInput.delegate = self
        self.view.addSubview(self.accOffInput)
        let accOffLine = UIView()
        accOffLine.backgroundColor = UIColor.gray
        accOffLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(accOffLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.angleLabel = UILabel()
        self.angleLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.angleLabel.textColor = UIColor.black
        self.angleLabel.text = NSLocalizedString("angle", comment: "angle")
        self.angleLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.angleLabel.numberOfLines = 0;
        self.angleLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.angleLabel)
        self.angleInput = UITextField()
        self.angleInput.font = UIFont.systemFont(ofSize: fontSize)
        self.angleInput.textColor = UIColor.black
        self.angleInput.borderStyle = UITextField.BorderStyle.bezel
        self.angleInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.angleInput.text = angle != nil ? String(angle) ??  "" : ""
        self.angleInput.delegate = self
        self.view.addSubview(self.angleInput)
        let angleLine = UIView()
        angleLine.backgroundColor = UIColor.gray
        angleLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(angleLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.distanceLabel = UILabel()
        self.distanceLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.distanceLabel.textColor = UIColor.black
        self.distanceLabel.text = NSLocalizedString("distance", comment: "distance")
        self.distanceLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.distanceLabel.numberOfLines = 0;
        self.distanceLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.distanceLabel)
        self.distanceInput = UITextField()
        self.distanceInput.font = UIFont.systemFont(ofSize: fontSize)
        self.distanceInput.textColor = UIColor.black
        self.distanceInput.borderStyle = UITextField.BorderStyle.bezel
        self.distanceInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.distanceInput.text = distance != nil ? String(distance) ??  "" : ""
        self.distanceInput.delegate = self
        self.view.addSubview(self.distanceInput)
        let distanceLine = UIView()
        distanceLine.backgroundColor = UIColor.gray
        distanceLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(distanceLine)
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
        // Allow only numeric characters and backspace
        let allowedCharacters = CharacterSet.decimalDigits
        let characterSet = CharacterSet(charactersIn: string)
        
        return allowedCharacters.isSuperset(of: characterSet)
    }
    
    var delegate : EditTimerDelegate?
    @objc private func submit(){
        let accOnStr = self.accOnInput.text ?? ""
        let accOffStr = self.accOffInput.text ?? ""
        let angleStr = self.angleInput.text ?? ""
        let distanceStr = self.distanceInput.text ?? ""
      // 检查输入是否为空
        if accOnStr.isEmpty || accOffStr.isEmpty  || angleStr.isEmpty  || distanceStr.isEmpty {
            Toast.hudBuilder.title(NSLocalizedString("fix_input", comment: "please fix your input")).show()
            return
        }
        let accOnValue = Int(accOnStr) ?? -1
        let accOffValue = Int64(accOffStr) ?? -1
        let angleValue = Int(angleStr) ?? -1
        let distanceValue =  Int(distanceStr) ?? -1
        if (accOnValue < 5 || accOnValue > 65535) && accOnValue != 0 {
            Toast.hudBuilder.title(NSLocalizedString("acc_on_value_warning", comment: "")).show()
            return
        }

        if  accOffValue < 0 || accOffValue > 2147483647 || (accOffValue > 0 && accOffValue < 1200){
            Toast.hudBuilder.title(NSLocalizedString("acc_off_value_warning", comment: "")).show()
            return
        }

        if  (angleValue < 15 || angleValue > 180) && angleValue != 0 {
            Toast.hudBuilder.title(NSLocalizedString("angle_value_warning", comment: "")).show()
            return
        }

        if (distanceValue < 100 || distanceValue > 65535) && distanceValue != 0 {
            Toast.hudBuilder.title(NSLocalizedString("distance_value_warning", comment: "")).show()
            return
        }
        print("accon:\(accOnValue);accOff:\(accOffValue);angle:\(angleValue);distance:\(distanceValue)")
        self.delegate?.setNewTimer(accOn: accOnValue, accOff: accOffValue, angle: angleValue, distance: distanceValue)
        self.navigationController?.popViewController(animated: false)
    }
}
