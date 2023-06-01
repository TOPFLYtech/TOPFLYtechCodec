//
//  EditPositiveNegativeWarningController.swift
//  tftble
//
 
//

import Foundation
import UIKit
import CLXToast
import QMUIKit
import ActionSheetPicker_3_0
class EditPositiveNegativeWarningController : UIViewController{
    private var portLabel:UILabel!
    private var portInput:UITextField!
    private var modeLabel:UILabel!
    private var modeInput:UITextField!
    private var highVoltageLabel:UILabel!
    private var highVoltageInput:UITextField!
    private var lowVoltageLabel:UILabel!
    private var lowVoltageInput:UITextField!
    private var samplingIntervalLabel:UILabel!
    private var samplingIntervalInput:UITextField!
    private var ditheringIntervalHighLabel:UILabel!
    private var ditheringIntervalHighInput:UITextField!
    private var ditheringIntervalLowLabel:UILabel!
    private var ditheringIntervalLowInput:UITextField!
    private var submitBtn:QMUIGhostButton!
    private var modeList:[String] = [String]()
    public var highVoltage:Int = -1
    public var lowVoltage:Int = -1
    public var port:Int = -1
    public var mode:Int = -1
    public var samplingInterval:Int = -1
    public var ditheringIntervalHigh:Int = -1
    public var ditheringIntervalLow:Int = -1
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
        self.modeList.append( NSLocalizedString("close", comment: "Close"))
        self.modeList.append( NSLocalizedString("open", comment: "Open"))
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = NSLocalizedString("positive_negative_warning", comment: "Ain event reporting mode")
        self.navigationItem.titleView = titleLabel
        let descWidth = Int(KSize.width / 2)
        let contentX = Int(KSize.width / 2 + 10)
        
        var startLabelY:Int = 60
        var startInputY:Int = 75
        let lineHigh:Int = 60
        var lineY:Int = 120
        
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(
                    target: self,
                    action: #selector(dismissKeyboard))

                tap.cancelsTouchesInView = false
                view.addGestureRecognizer(tap)
        
        self.portLabel = UILabel()
        self.portLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.portLabel.textColor = UIColor.black
        self.portLabel.text = NSLocalizedString("port", comment: "Port")
        self.portLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.portLabel.numberOfLines = 0;
        self.portLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.portLabel)
        self.portInput = UITextField()
        self.portInput.font = UIFont.systemFont(ofSize: fontSize)
        self.portInput.textColor = UIColor.black
        self.portInput.isUserInteractionEnabled = false
        self.portInput.borderStyle = UITextField.BorderStyle.bezel
        self.portInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.view.addSubview(self.portInput)
        let oldPwdLine = UIView()
        oldPwdLine.backgroundColor = UIColor.gray
        oldPwdLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(oldPwdLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.modeLabel = UILabel()
        self.modeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.modeLabel.textColor = UIColor.black
        self.modeLabel.text = NSLocalizedString("mode", comment: "Mode")
        self.modeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.modeLabel.numberOfLines = 0;
        self.modeLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.modeLabel)
        self.modeInput = UITextField()
        self.modeInput.font = UIFont.systemFont(ofSize: fontSize)
        self.modeInput.textColor = UIColor.black
        self.modeInput.borderStyle = UITextField.BorderStyle.bezel
        self.modeInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(modeSelectTap))
        self.modeInput.addGestureRecognizer(tapGesture)
        self.view.addSubview(self.modeInput)
        let newPwdLine = UIView()
        newPwdLine.backgroundColor = UIColor.gray
        newPwdLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(newPwdLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.highVoltageLabel = UILabel()
        self.highVoltageLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.highVoltageLabel.textColor = UIColor.black
        self.highVoltageLabel.text = NSLocalizedString("high_voltage", comment: "High voltage")
        self.highVoltageLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.highVoltageLabel.numberOfLines = 0;
        self.highVoltageLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.highVoltageLabel)
        self.highVoltageInput = UITextField()
        self.highVoltageInput.font = UIFont.systemFont(ofSize: fontSize)
        self.highVoltageInput.textColor = UIColor.black
        self.highVoltageInput.borderStyle = UITextField.BorderStyle.bezel
        self.highVoltageInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.view.addSubview(self.highVoltageInput)
        let repeatPwdLine = UIView()
        repeatPwdLine.backgroundColor = UIColor.gray
        repeatPwdLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(repeatPwdLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.lowVoltageLabel = UILabel()
        self.lowVoltageLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.lowVoltageLabel.textColor = UIColor.black
        self.lowVoltageLabel.text = NSLocalizedString("low_voltage", comment: "Low voltage")
        self.lowVoltageLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.lowVoltageLabel.numberOfLines = 0;
        self.lowVoltageLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.lowVoltageLabel)
        self.lowVoltageInput = UITextField()
        self.lowVoltageInput.font = UIFont.systemFont(ofSize: fontSize)
        self.lowVoltageInput.textColor = UIColor.black
        self.lowVoltageInput.borderStyle = UITextField.BorderStyle.bezel
        self.lowVoltageInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.view.addSubview(self.lowVoltageInput)
        let lowVoltageLine = UIView()
        lowVoltageLine.backgroundColor = UIColor.gray
        lowVoltageLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(lowVoltageLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.samplingIntervalLabel = UILabel()
        self.samplingIntervalLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.samplingIntervalLabel.textColor = UIColor.black
        self.samplingIntervalLabel.text = NSLocalizedString("sampling_interval", comment: "Sampling interval")
        self.samplingIntervalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.samplingIntervalLabel.numberOfLines = 0;
        self.samplingIntervalLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.samplingIntervalLabel)
        self.samplingIntervalInput = UITextField()
        self.samplingIntervalInput.font = UIFont.systemFont(ofSize: fontSize)
        self.samplingIntervalInput.textColor = UIColor.black
        self.samplingIntervalInput.borderStyle = UITextField.BorderStyle.bezel
        self.samplingIntervalInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.view.addSubview(self.samplingIntervalInput)
        let samplingIntervalLine = UIView()
        samplingIntervalLine.backgroundColor = UIColor.gray
        samplingIntervalLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(samplingIntervalLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        self.ditheringIntervalHighLabel = UILabel()
        self.ditheringIntervalHighLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.ditheringIntervalHighLabel.textColor = UIColor.black
        self.ditheringIntervalHighLabel.text = NSLocalizedString("dithering_interval_high", comment: "High voltage jitter elimination interval")
        self.ditheringIntervalHighLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.ditheringIntervalHighLabel.numberOfLines = 0;
        self.ditheringIntervalHighLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.ditheringIntervalHighLabel)
        self.ditheringIntervalHighInput = UITextField()
        self.ditheringIntervalHighInput.font = UIFont.systemFont(ofSize: fontSize)
        self.ditheringIntervalHighInput.textColor = UIColor.black
        self.ditheringIntervalHighInput.borderStyle = UITextField.BorderStyle.bezel
        self.ditheringIntervalHighInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.view.addSubview(self.ditheringIntervalHighInput)
        let ditheringIntervalHighLine = UIView()
        ditheringIntervalHighLine.backgroundColor = UIColor.gray
        ditheringIntervalHighLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(ditheringIntervalHighLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
           self.ditheringIntervalLowLabel = UILabel()
        self.ditheringIntervalLowLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.ditheringIntervalLowLabel.textColor = UIColor.black
        self.ditheringIntervalLowLabel.text = NSLocalizedString("dithering_interval_low", comment: "Low voltage jitter elimination interval")
        self.ditheringIntervalLowLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.ditheringIntervalLowLabel.numberOfLines = 0;
        self.ditheringIntervalLowLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.ditheringIntervalLowLabel)
        self.ditheringIntervalLowInput = UITextField()
        self.ditheringIntervalLowInput.font = UIFont.systemFont(ofSize: fontSize)
        self.ditheringIntervalLowInput.textColor = UIColor.black
        self.ditheringIntervalLowInput.borderStyle = UITextField.BorderStyle.bezel
        self.ditheringIntervalLowInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.view.addSubview(self.ditheringIntervalLowInput)
        let ditheringIntervalLowLine = UIView()
        ditheringIntervalLowLine.backgroundColor = UIColor.gray
        ditheringIntervalLowLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(ditheringIntervalLowLine)
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


        if self.highVoltage != -1 {
            self.highVoltageInput.text = String(format: "%.2f", Float(self.highVoltage) / 100.0)
        }
        if self.lowVoltage != -1 {
            self.lowVoltageInput.text = String(format: "%.2f", Float(self.lowVoltage) / 100.0)
        }
        if self.port != -1 {
            self.portInput.text = String(format: "%d",port)
        }
        if self.mode != -1 {
           self.modeInput.text = self.modeList[self.mode]
        }
        if self.samplingInterval != -1 {
            self.samplingIntervalInput.text = String(format: "%d",self.samplingInterval)
        }
        if self.ditheringIntervalHigh != -1 {
            self.ditheringIntervalHighInput.text = String(format: "%d",self.ditheringIntervalHigh)
        }
        if self.ditheringIntervalLow != -1 {
            self.ditheringIntervalLowInput.text = String(format: "%d",self.ditheringIntervalLow)
        }
    }

    // 点击事件处理函数
    @objc func modeSelectTap() {
     
        // 取消文本框的第一响应者状态，以防止键盘弹出
        self.modeInput.resignFirstResponder()
        var selectIndex = self.mode
        if selectIndex < 0 || selectIndex >= self.modeList.count{
            selectIndex = 0
        }
         ActionSheetStringPicker.show(withTitle: "", rows: self.modeList, initialSelection:selectIndex, doneBlock: {
            picker, index, value in
            self.mode = index
            self.modeInput.text = self.modeList[index]
         }, cancel: { ActionMultipleStringCancelBlock in return }, origin: self.modeInput)
    }

    var delegate : EditPositiveNegativeWarningValueDelegate?
    @objc private func submit(){
        let portStr = self.portInput.text?.trimmingCharacters(in: .whitespacesAndNewlines)
        let highVoltageStr = self.highVoltageInput.text?.trimmingCharacters(in: .whitespacesAndNewlines)
        let lowVoltageStr = self.lowVoltageInput.text?.trimmingCharacters(in: .whitespacesAndNewlines)
        let samplingIntervalStr = self.samplingIntervalInput.text?.trimmingCharacters(in: .whitespacesAndNewlines)
        let ditheringIntervalHighStr = self.ditheringIntervalHighInput.text?.trimmingCharacters(in: .whitespacesAndNewlines)
        let ditheringIntervalLowStr = self.ditheringIntervalLowInput.text?.trimmingCharacters(in: .whitespacesAndNewlines)
        if( highVoltageStr?.count ?? -1 <= 0 || lowVoltageStr?.count ?? -1 <= 0 || ditheringIntervalHighStr?.count ?? -1 <= 0 ||
                        ditheringIntervalLowStr?.count ?? -1 <= 0 || samplingIntervalStr?.count ?? -1 <= 0
            ){
            Toast.hudBuilder.title(NSLocalizedString("fix_input", comment: "Please fix your input!")).show() 
            return;
        }
        var samplingIntervalInt = Int(samplingIntervalStr ?? "") ?? -1
        if samplingIntervalInt < 0 || samplingIntervalInt ?? 65536 > 65535{
            Toast.hudBuilder.title(NSLocalizedString("sampling_interval_error_warning", comment: "The sampling interval must be between 0 and 65535")).show() 
            return
        }
        var lowVoltageFloat = Float(lowVoltageStr ?? "0") ?? -1
        var highVoltageFloat = Float(highVoltageStr ?? "0") ?? -1
        if lowVoltageFloat < 0 || highVoltageFloat < 0 || highVoltageFloat - lowVoltageFloat < 0.5{
            Toast.hudBuilder.title(NSLocalizedString("high_voltage_low_voltage_error", comment: "The high voltage must be 0.5V higher than the low voltage")).show() 
            return
        }
        lowVoltageFloat = lowVoltageFloat*100
        highVoltageFloat = highVoltageFloat*100
        var ditheringIntervalHighInt = Int(ditheringIntervalHighStr ?? "-1") ?? -1
        var ditheringIntervalLowInt = Int(ditheringIntervalLowStr ?? "0") ??  -1
        if ditheringIntervalHighInt < 0 || ditheringIntervalHighInt > 255 || ditheringIntervalLowInt < 0 || ditheringIntervalLowInt > 255 {
            Toast.hudBuilder.title(NSLocalizedString("dithering_interval_error_warning", comment: "The jitter elimination interval must be between 0 and 255")).show() 
            return
        }
        self.delegate?.setPositiveNegativeWarningValue(port:Int(portStr!) ?? 0,mode:self.mode,highVoltage:Int(highVoltageFloat),lowVoltage:Int(lowVoltageFloat),ditheringIntervalHigh:ditheringIntervalHighInt,
                    ditheringIntervalLow:ditheringIntervalLowInt,samplingInterval:samplingIntervalInt)
        self.navigationController?.popViewController(animated: false)
    }
}
