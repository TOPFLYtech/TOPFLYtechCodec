//
//  EditNegativeTriggerSinglePulseController.swift
//  tftble
//
//  Created by china topflytech on 2025/4/29.
//  Copyright © 2025 com.tftiot. All rights reserved.
//


import Foundation
import UIKit
import CLXToast
class EditNegativeTriggerSinglePulseController : UIViewController,UITextFieldDelegate {
    private var portLabel:UILabel!
    private var portInput:UITextField!
    private var startLevelLabel:UILabel!
    private var startLevelInput:UITextField!
    private var pulseWidthTimeLabel:UILabel!
    private var pulseWidthTimeInput:UITextField!
    private var submitBtn:QMUIGhostButton!
    private var fontSize:CGFloat = Utils.fontSize
    
    public var port:Int = 0
    var connectStatusDelegate:SetConnectStatusDelegate?
    
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        self.tftHideTopBar()
        self.connectStatusDelegate?.setConnectStatus()
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
    }
    @objc func dismissKeyboard()
    {
        view.endEditing(true)
    }
    override func viewDidLoad() {
        self.view.backgroundColor = UIColor.white
        self.tftInstallTopBar(title: NSLocalizedString("relay_pulse", comment: "Pulse"))
        let viewWidth = Int(self.view.bounds.width)
        let navBottomY = Int(self.tftTopContentY())
        self.tftRememberInitialContentTop(CGFloat(navBottomY))
        let descWidth = Int(CGFloat(viewWidth) / 2.0)
        let contentX = descWidth + 10
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(
            target: self,
            action: #selector(dismissKeyboard))
        
        tap.cancelsTouchesInView = false
        view.addGestureRecognizer(tap)
        
        var startLabelY:Int = navBottomY + 10
        var startInputY:Int = navBottomY + 25
        let lineHigh:Int = 60
        var lineY:Int = navBottomY + 70
        self.portLabel = UILabel()
        self.portLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.portLabel.textColor = UIColor.black
        self.portLabel.text = NSLocalizedString("port", comment: "port")
        self.portLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.portLabel.numberOfLines = 0;
        self.portLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.portLabel)
        self.portInput = UITextField()
        self.portInput.font = UIFont.systemFont(ofSize: fontSize)
        self.portInput.textColor = UIColor.black
        self.portInput.borderStyle = UITextField.BorderStyle.bezel
        self.portInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.portInput.delegate = self
        self.portInput.text = "\(port)"
        self.portInput.isEnabled = false
        self.view.addSubview(self.portInput)
        let portLine = UIView()
        portLine.backgroundColor = UIColor.gray
        portLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(viewWidth), height: 0.5)
        self.view.addSubview(portLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
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
        startLevelLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(viewWidth), height: 0.5)
        self.view.addSubview(startLevelLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        
        self.pulseWidthTimeLabel = UILabel()
        self.pulseWidthTimeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.pulseWidthTimeLabel.textColor = UIColor.black
        self.pulseWidthTimeLabel.text = NSLocalizedString("pulse_width_time", comment: "pulse width time(ms)")
        self.pulseWidthTimeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.pulseWidthTimeLabel.numberOfLines = 0;
        self.pulseWidthTimeLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.pulseWidthTimeLabel)
        self.pulseWidthTimeInput = UITextField()
        self.pulseWidthTimeInput.font = UIFont.systemFont(ofSize: fontSize)
        self.pulseWidthTimeInput.textColor = UIColor.black
        self.pulseWidthTimeInput.borderStyle = UITextField.BorderStyle.bezel
        self.pulseWidthTimeInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.pulseWidthTimeInput.delegate = self
        self.view.addSubview(self.pulseWidthTimeInput)
        let pulseWidthTimeLine = UIView()
        pulseWidthTimeLine.backgroundColor = UIColor.gray
        pulseWidthTimeLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(viewWidth), height: 0.5)
        self.view.addSubview(pulseWidthTimeLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        
         
        self.submitBtn = QMUIGhostButton()
        self.submitBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.submitBtn.setTitle(NSLocalizedString("submit", comment: "Submit"), for: .normal)
        self.submitBtn.ghostColor = UIColor.colorPrimary
        self.submitBtn.frame = CGRect(x: Int((viewWidth - 200) / 2), y: startLabelY + 10, width: 200, height: 30)
        self.submitBtn.addTarget(self, action: #selector(submit), for:.touchUpInside)
        self.view.addSubview(self.submitBtn)
        
        
        
    }
    
    var delegate : EditNegativeTriggerSinglePulseDelegate?
    @objc private func submit(){
        
        let startLevel = self.startLevelInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let pulseWidthTime = self.pulseWidthTimeInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        
        if startLevel.isEmpty || pulseWidthTime.isEmpty{
            let errorMessage = NSLocalizedString("input_error", comment: "Input error!")
            Toast.hudBuilder.title(errorMessage).show()
            
            return
        }
        
        guard var startLevelInt = Int(startLevel), var pulseWidthTimeInt = Int(pulseWidthTime) else {
            let errorMessage = NSLocalizedString("input_error", comment: "Input error!")
            Toast.hudBuilder.title(errorMessage).show()
            return
        }
        if startLevel != "1" && startLevel != "0"{
            let warningMessage = NSLocalizedString("start_level_format_error", comment: "Start level must be 1 or 0")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }
        if pulseWidthTimeInt < 100 || pulseWidthTimeInt > 6553500
        {
            let warningMessage = NSLocalizedString("negative_trigger_pulse_input_error", comment: "pulse width time value is incorrect!It must between 100 and 6553500.")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }
        
        self.delegate?.settNegativeTriggerSinglePulseValue(port: port, startLevel: startLevelInt, pulseWidthTime: pulseWidthTimeInt/100)
        self.navigationController?.popViewController(animated: false)
    }
}
