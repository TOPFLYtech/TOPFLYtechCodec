//
//  EditPulseSecondRelayController.swift
//  tftble
//
//  Created by china topflytech on 2024/8/30.
//  Copyright © 2024 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import CLXToast
import QMUIKit
class EditSecondPulseRelayController : UIViewController,UITextFieldDelegate {
    private var startLevelLabel:UILabel!
    private var startLevelInput:UITextField!
    private var highLevelPulseWidthTimeLabel:UILabel!
    private var highLevelPulseWidthTimeInput:UITextField!
    private var lowLevelPulseWidthTimeLabel:UILabel!
    private var lowLevelPulseWidthTimeInput:UITextField!
    private var pulseCountLabel:UILabel!
    private var pulseCountInput:UITextField!
    private var submitBtn:QMUIGhostButton!
    private var fontSize:CGFloat = Utils.fontSize
    

    var connectStatusDelegate:SetConnectStatusDelegate?
    
    
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
        titleLabel.text = NSLocalizedString("relay_pulse", comment: "Pulse")
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
        
        self.startLevelLabel = UILabel()
        self.startLevelLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.startLevelLabel.textColor = UIColor.black
        self.startLevelLabel.text = NSLocalizedString("start_level", comment: "Start level")
        self.startLevelLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.startLevelLabel.numberOfLines = 0;
        self.startLevelLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.startLevelLabel)
        self.startLevelInput = UITextField()
        self.startLevelInput.font = UIFont.systemFont(ofSize: fontSize)
        self.startLevelInput.textColor = UIColor.black
        self.startLevelInput.borderStyle = UITextField.BorderStyle.bezel
        self.startLevelInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.startLevelInput.delegate = self
        self.view.addSubview(self.startLevelInput)
        let startLevelLine = UIView()
        startLevelLine.backgroundColor = UIColor.gray
        startLevelLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(startLevelLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        
        
        self.highLevelPulseWidthTimeLabel = UILabel()
        self.highLevelPulseWidthTimeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.highLevelPulseWidthTimeLabel.textColor = UIColor.black
        self.highLevelPulseWidthTimeLabel.text = NSLocalizedString("high_level_pulse_width_time", comment: "High level pulse width time(ms)")
        self.highLevelPulseWidthTimeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.highLevelPulseWidthTimeLabel.numberOfLines = 0;
        self.highLevelPulseWidthTimeLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.highLevelPulseWidthTimeLabel)
        self.highLevelPulseWidthTimeInput = UITextField()
        self.highLevelPulseWidthTimeInput.font = UIFont.systemFont(ofSize: fontSize)
        self.highLevelPulseWidthTimeInput.textColor = UIColor.black
        self.highLevelPulseWidthTimeInput.borderStyle = UITextField.BorderStyle.bezel
        self.highLevelPulseWidthTimeInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.highLevelPulseWidthTimeInput.delegate = self
        self.view.addSubview(self.highLevelPulseWidthTimeInput)
        let highLevelPulseWidthTimeLine = UIView()
        highLevelPulseWidthTimeLine.backgroundColor = UIColor.gray
        highLevelPulseWidthTimeLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(highLevelPulseWidthTimeLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        
        self.lowLevelPulseWidthTimeLabel = UILabel()
        self.lowLevelPulseWidthTimeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.lowLevelPulseWidthTimeLabel.textColor = UIColor.black
        self.lowLevelPulseWidthTimeLabel.text = NSLocalizedString("low_level_pulse_width_time", comment: "Low level pulse width time(ms)")
        self.lowLevelPulseWidthTimeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.lowLevelPulseWidthTimeLabel.numberOfLines = 0;
        self.lowLevelPulseWidthTimeLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.lowLevelPulseWidthTimeLabel)
        self.lowLevelPulseWidthTimeInput = UITextField()
        self.lowLevelPulseWidthTimeInput.font = UIFont.systemFont(ofSize: fontSize)
        self.lowLevelPulseWidthTimeInput.textColor = UIColor.black
        self.lowLevelPulseWidthTimeInput.borderStyle = UITextField.BorderStyle.bezel
        self.lowLevelPulseWidthTimeInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.lowLevelPulseWidthTimeInput.delegate = self
        self.view.addSubview(self.lowLevelPulseWidthTimeInput)
        let lowLevelPulseWidthTimeLine = UIView()
        lowLevelPulseWidthTimeLine.backgroundColor = UIColor.gray
        lowLevelPulseWidthTimeLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(lowLevelPulseWidthTimeLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        
        self.pulseCountLabel = UILabel()
        self.pulseCountLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.pulseCountLabel.textColor = UIColor.black
        self.pulseCountLabel.text = NSLocalizedString("pulse_count", comment: "Pulse time")
        self.pulseCountLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.pulseCountLabel.numberOfLines = 0;
        self.pulseCountLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.pulseCountLabel)
        self.pulseCountInput = UITextField()
        self.pulseCountInput.font = UIFont.systemFont(ofSize: fontSize)
        self.pulseCountInput.textColor = UIColor.black
        self.pulseCountInput.borderStyle = UITextField.BorderStyle.bezel
        self.pulseCountInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.pulseCountInput.delegate = self
        self.view.addSubview(self.pulseCountInput)
        let pulseCountLine = UIView()
        pulseCountLine.backgroundColor = UIColor.gray
        pulseCountLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(pulseCountLine)
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
    
    var delegate : EditSecondPulseRelayDelegate?
    @objc private func submit(){
        
        let startLevel = self.startLevelInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let highLevelPulseWidthTime = self.highLevelPulseWidthTimeInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let lowLevelPulseWidthTime = self.lowLevelPulseWidthTimeInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let pulseCount = self.pulseCountInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

        if startLevel.isEmpty || highLevelPulseWidthTime.isEmpty || lowLevelPulseWidthTime.isEmpty || pulseCount.isEmpty {
            let errorMessage = NSLocalizedString("input_error", comment: "Input error!")
            Toast.hudBuilder.title(errorMessage).show()
          
            return
        }

        guard var startLevelInt = Int(startLevel), var highLevelPulseWidthTimeInt = Int(highLevelPulseWidthTime), let lowLevelPulseWidthTimeInt = Int(lowLevelPulseWidthTime), let pulseCountInt = Int(pulseCount) else {
            let errorMessage = NSLocalizedString("input_error", comment: "Input error!")
            Toast.hudBuilder.title(errorMessage).show()
            return
        }
        if startLevel != "1" && startLevel != "0"{
            let warningMessage = NSLocalizedString("start_level_format_error", comment: "Start level must be 1 or 0")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }
        if highLevelPulseWidthTimeInt < 100 || highLevelPulseWidthTimeInt > 25500
        {
            let warningMessage = NSLocalizedString("high_level_pulse_width_time_error", comment: "High level pulse width time value is incorrect!It must between 100 and 25500.")
            Toast.hudBuilder.title(warningMessage).show()
            return
        } 
        if    lowLevelPulseWidthTimeInt < 100 || lowLevelPulseWidthTimeInt > 25500
        {
            let warningMessage = NSLocalizedString("low_level_pulse_width_time_error", comment: "Low level pulse width time value is incorrect!It must between 100 and 25500.")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }

        if  pulseCountInt < 0 || pulseCountInt > 65535
        {
            let warningMessage = NSLocalizedString("pulse_count_error", comment: "Pulse count value is incorrect!It must between 0 and 65535.")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }
        self.delegate?.setSecondPulseRelayValue(startLevel: startLevelInt, highLevelPulseWidthTime: highLevelPulseWidthTimeInt  / 100, lowLevelPulseWidthTime: lowLevelPulseWidthTimeInt / 100, pulseCount: pulseCountInt)
        self.navigationController?.popViewController(animated: false)
    }
}
