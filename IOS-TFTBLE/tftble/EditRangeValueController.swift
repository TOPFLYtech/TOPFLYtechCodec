//
//  EditRangeValue.swift
//  tftble
//
//  Created by jeech on 2019/12/26.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import CLXToast
import QMUIKit
protocol EditRangeValueDelegate {
    func setRangeValue(high: Int,low:Int)
}

class EditRangeValueController:UIViewController{
    private var highValueLabel:UILabel!
    private var highValueInput:UITextField!
    private var highValueSwitch:UISwitch!
    private var lowValueLabel:UILabel!
    private var lowValueInput:UITextField!
    private var lowValueSwitch:UISwitch!
    private var submitBtn:QMUIGhostButton!
    var delegate : EditRangeValueDelegate?
    var connectStatusDelegate : SetConnectStatusDelegate?
    var high = ""
    var low = ""
    var highOpen = false
    var lowOpen = false
    var type = ""
    var deviceType = ""
    private var fontSize:CGFloat = Utils.fontSize
    
    override func viewDidDisappear(_ animated: Bool) {
        self.connectStatusDelegate?.setConnectStatus()
    }
    @objc func dismissKeyboard()
       {
           view.endEditing(true)
       }
    override func viewDidLoad() {
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(
                    target: self,
                    action: #selector(dismissKeyboard))

                tap.cancelsTouchesInView = false
                view.addGestureRecognizer(tap)
        
        let titleLabel = UILabel()
        self.navigationItem.titleView = titleLabel
        if type == "temp"{
            titleLabel.frame = CGRect(x: 0, y: 0, width: 180, height: 40)
            let tempAlarmStr = NSLocalizedString("temp_alarm", comment: "Temperature alarm")
            titleLabel.text = "\(tempAlarmStr)(\(Utils.getCurTempUnit()))"
        }else if type == "humidity"{
            titleLabel.frame = CGRect(x: 0, y: 0, width: 130, height: 40)
            titleLabel.text = NSLocalizedString("humidity_alarm", comment: "Humidity alarm")
        }
        self.view.backgroundColor = UIColor.white
        let descWidth = Int(KSize.width / 3)
        let contentX = Int(KSize.width / 3 + 10)
        let btnX = Int(KSize.width / 3 * 2 + 15)
        var startLabelY:Int = 60
        var startInputY:Int = 75
        let lineHigh:Int = 60
        var lineY:Int = 120
        var btnY:Int = 75
        let btnHeight = 30
        self.highValueLabel = UILabel()
        self.highValueLabel.textColor = UIColor.black
        self.highValueLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.highValueLabel.text = NSLocalizedString("high_desc", comment: "High:")
        self.highValueLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.highValueLabel.numberOfLines = 0;
        self.highValueLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.highValueLabel)
        self.highValueInput = UITextField()
        self.highValueInput.textColor = UIColor.black
        self.highValueInput.font = UIFont.systemFont(ofSize: fontSize)
        self.highValueInput.borderStyle = UITextField.BorderStyle.bezel
        self.highValueInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.view.addSubview(self.highValueInput)
        self.highValueSwitch = UISwitch()
        self.highValueSwitch.frame = CGRect(x: btnX, y: btnY, width: 90, height: btnHeight)
        self.view.addSubview(self.highValueSwitch)
        let oldPwdLine = UIView()
        oldPwdLine.backgroundColor = UIColor.gray
        oldPwdLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(oldPwdLine)
        startLabelY += lineHigh
        lineY += lineHigh
        btnY += lineHigh
        startInputY += lineHigh
        self.lowValueLabel = UILabel()
        self.lowValueLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.lowValueLabel.textColor = UIColor.black
        self.lowValueLabel.text = NSLocalizedString("low_desc", comment: "Low:")
        self.lowValueLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.lowValueLabel.numberOfLines = 0;
        self.lowValueLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.lowValueLabel)
        self.lowValueInput = UITextField()
        self.lowValueInput.font = UIFont.systemFont(ofSize: fontSize)
        self.lowValueInput.textColor = UIColor.black
        self.lowValueInput.borderStyle = UITextField.BorderStyle.bezel
        self.lowValueInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.view.addSubview(self.lowValueInput)
        self.lowValueSwitch = UISwitch()
        self.lowValueSwitch.frame = CGRect(x: btnX, y: btnY, width: 90, height: btnHeight)
        self.view.addSubview(self.lowValueSwitch)
        let newPwdLine = UIView()
        newPwdLine.backgroundColor = UIColor.gray
        newPwdLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(newPwdLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.submitBtn = QMUIGhostButton()
        self.submitBtn.setTitle(NSLocalizedString("submit", comment: "Submit"), for: .normal)
        self.submitBtn.ghostColor = UIColor.colorPrimary
        self.submitBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.submitBtn.frame = CGRect(x: Int((KSize.width - 200) / 2), y: startLabelY + 10, width: 200, height: 30)
        self.submitBtn.addTarget(self, action: #selector(submit), for:.touchUpInside)
        self.view.addSubview(self.submitBtn)
        
        self.highValueInput.text = self.high
        self.lowValueInput.text = self.low
        self.highValueSwitch.isOn = self.highOpen
        self.lowValueSwitch.isOn = self.lowOpen
         
        
        self.highValueSwitch.addTarget(self, action: #selector(switchClickClick),
                                   for:UIControl.Event.valueChanged)
        self.lowValueSwitch.addTarget(self, action: #selector(switchClickClick),
                                   for:UIControl.Event.valueChanged)
    }
    @objc func switchClickClick(sender:UISwitch){
        let highValueOpen = self.highValueSwitch.isOn
        let lowValueOpen = self.lowValueSwitch.isOn
        if !highValueOpen && !lowValueOpen{
            self.highValueInput.text = ""
            self.lowValueInput.text = ""
        }
    }
    override func viewDidAppear(_ animated: Bool) {
        
//        self.highValueLabel.text = NSLocalizedString("high_desc", comment: "High:")
//        self.lowValueLabel.text = NSLocalizedString("low_desc", comment: "Low:")
//         self.submitBtn.setTitle(NSLocalizedString("submit", comment: "Submit"), for: .normal)
    }
    
    func isPurnFloat(string: String) -> Bool {
        let scan: Scanner = Scanner(string: string)
        var val:Float = 0
        return scan.scanFloat(&val) && scan.isAtEnd
    }
    func isPurnInt(string: String) -> Bool {
        let scan: Scanner = Scanner(string: string)
        var val:Int = 0
        return scan.scanInt(&val) && scan.isAtEnd
    }
   
    @objc private func submit(){
        if type == "temp"{
            self.submitTempAlarm()
        }else if type == "humidity"{
            self.submitHumidityAlarm()
        } 
    }
    
    func submitHumidityAlarm(){
        let highValue = self.highValueInput.text ?? ""
        let lowValue = self.lowValueInput.text ?? ""
        let highValueOpen = self.highValueSwitch.isOn
        let lowValueOpen = self.lowValueSwitch.isOn
        if (lowValue.count > 0 && !self.isPurnFloat(string: lowValue)) ||
            (highValue.count > 0 && !self.isPurnFloat(string: highValue)){
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect", comment: "Value is incorrect!")).show()
            return
        }
        if !highValueOpen && highValue.count > 0{
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect_need_turn_high_humidity_switch", comment: "Value is incorrect!Need to turn on the high Humidity warning switch.")).show()
            return
        }
        if highValueOpen && highValue.count == 0{
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect_need_set_high_humidity_value", comment: "Value is incorrect!Need to set the high humidity warning value.")).show()
            return
        }
        if !lowValueOpen && lowValue.count > 0{
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect_need_turn_low_humidity_switch", comment: "Value is incorrect!Need to turn on the low humidity warning switch.")).show()
            return
        }
        
        if lowValueOpen && lowValue.count == 0{
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect_need_set_low_humidity_value", comment: "Value is incorrect!Need to set the low humidity warning value.")).show()
            return
        }
        var saveHighValue:Int
        var saveLowValue:Int
        if !highValueOpen  || highValue.count == 0{
            saveHighValue = 4095
        }else{
            saveHighValue = Int((Float)(highValue)  ?? 0)
        }
        if !lowValueOpen  || lowValue.count == 0{
            saveLowValue = 4095
        }else{
            saveLowValue = Int((Float)(lowValue)  ?? 0)
        }
        if (saveHighValue < saveLowValue && saveLowValue != 4095 && saveHighValue != 4095){
            Toast.hudBuilder.title(NSLocalizedString("high_must_great_than_low", comment: "The highest value must be greater than the lowest value")).show()
                       return
        }
        if  (saveHighValue < saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue >= 100) ||
            (saveLowValue != 4095 && saveLowValue < 0){
            Toast.hudBuilder.title(NSLocalizedString("value_incorrect_out_of_range", comment: "Value is incorrect!It is out of range.")).show()
                       return
        }
        self.delegate?.setRangeValue(high: saveHighValue, low: saveLowValue)
        self.navigationController?.popViewController(animated: false)
    }
    
    func submitTempAlarm(){
       let highValue = self.highValueInput.text ?? ""
        let lowValue = self.lowValueInput.text ?? ""
        let highValueOpen = self.highValueSwitch.isOn
        let lowValueOpen = self.lowValueSwitch.isOn
        if (lowValue.count > 0 && !self.isPurnFloat(string: lowValue)) ||
            (highValue.count > 0 && !self.isPurnFloat(string: highValue)){
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect", comment: "Value is incorrect!")).show()
            return
        }
        if !highValueOpen && highValue.count > 0{
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect_need_turn_high_temp_switch", comment: "Value is incorrect!Need to turn on the high temperature warning switch.")).show()
            return
        }
        if highValueOpen && highValue.count == 0{
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect_need_set_high_temp_value", comment: "Value is incorrect!Need to set the high temperature warning value.")).show()
            return
        }
        if !lowValueOpen && lowValue.count > 0{
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect_need_turn_low_temp_switch", comment: "Value is incorrect!Need to turn on the low temperature warning switch.")).show()
            return
        }
        
        if lowValueOpen && lowValue.count == 0{
            Toast.hudBuilder.title(NSLocalizedString("value_is_incorrect_need_set_low_temp_value", comment: "Value is incorrect!Need to set the low temperature warning value.")).show()
            return
        }
        var saveHighValue:Int
        var saveLowValue:Int
        if !highValueOpen || highValue.count == 0{
            saveHighValue = 4095
        }else{
            saveHighValue = Int(Utils.getSourceTemp(sourceTemp: (Float(highValue) ?? 0.0)) * 10.0)
        }
        if !lowValueOpen  || lowValue.count == 0{
            saveLowValue = 4095
        }else{
            saveLowValue = Int(Utils.getSourceTemp(sourceTemp: (Float(lowValue) ?? 0.0)) * 10.0)
        }
        print(saveLowValue)
        print(saveHighValue)
        if (saveHighValue < saveLowValue && saveLowValue != 4095 && saveHighValue != 4095){
            Toast.hudBuilder.title(NSLocalizedString("high_must_great_than_low", comment: "The highest value must be greater than the lowest value")).show()
                       return
        }
        
        if deviceType == "S08"{
            if  (saveHighValue < saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue >= 800) ||
                (saveLowValue != 4095 && saveLowValue <= -300){
                let tempUnit = UserDefaults.standard.integer(forKey: "tempUnit")
                if tempUnit == 0{
                    Toast.hudBuilder.title(NSLocalizedString("s08_value_incorrect_out_of_temp_range", comment: "Value is incorrect!It must between -29 and 79.")).show()
                }else{
                    Toast.hudBuilder.title(NSLocalizedString("s08_value_incorrect_out_of_temp_range1", comment: "Value is incorrect!It must between -20 and 174.")).show()
                }
                return
            }
        }else if deviceType == "S10"{
            if  (saveHighValue < saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue >= 1500) ||
                (saveLowValue != 4095 && saveLowValue <= -550){
                let tempUnit = UserDefaults.standard.integer(forKey: "tempUnit")
                if tempUnit == 0{
                    Toast.hudBuilder.title(NSLocalizedString("s10_value_incorrect_out_of_temp_range", comment: "Value is incorrect!It must between -54 and 149.")).show()
                }else{
                    Toast.hudBuilder.title(NSLocalizedString("s10_value_incorrect_out_of_temp_range1", comment: "Value is incorrect!It must between -65 and 300.")).show()
                }
                return
            }
        }else{
            if  (saveHighValue < saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue >= 1000) ||
                (saveLowValue != 4095 && saveLowValue <= -400){
                let tempUnit = UserDefaults.standard.integer(forKey: "tempUnit")
                if tempUnit == 0{
                    Toast.hudBuilder.title(NSLocalizedString("value_incorrect_out_of_temp_range", comment: "Value is incorrect!It must between -39 and 99.")).show()
                }else{
                    Toast.hudBuilder.title(NSLocalizedString("value_incorrect_out_of_temp_range1", comment: "Value is incorrect!It must between -38 and 210.")).show()
                }
                return
            }
        }
        self.delegate?.setRangeValue(high: saveHighValue, low: saveLowValue)
        self.navigationController?.popViewController(animated: false)
    }
}
