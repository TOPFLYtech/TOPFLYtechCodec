//
//  EditPulseRelayController.swift
//  tftble
//

//

import Foundation
import UIKit
import CLXToast
import QMUIKit
class EditPulseRelayController : UIViewController,UITextFieldDelegate {
    private var cycleTimeLabel:UILabel!
    private var cycleTimeInput:UITextField!
    private var initEnableTimeLabel:UILabel!
    private var initEnableTimeInput:UITextField!
    private var toggleTimeLabel:UILabel!
    private var toggleTimeInput:UITextField!
    private var recoverTimeLabel:UILabel!
    private var recoverTimeInput:UITextField!
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
        
        self.cycleTimeLabel = UILabel()
        self.cycleTimeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.cycleTimeLabel.textColor = UIColor.black
        self.cycleTimeLabel.text = NSLocalizedString("cycle_time", comment: "Cycle time(ms)")
        self.cycleTimeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.cycleTimeLabel.numberOfLines = 0;
        self.cycleTimeLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.cycleTimeLabel)
        self.cycleTimeInput = UITextField()
        self.cycleTimeInput.font = UIFont.systemFont(ofSize: fontSize)
        self.cycleTimeInput.textColor = UIColor.black
        self.cycleTimeInput.borderStyle = UITextField.BorderStyle.bezel
        self.cycleTimeInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.cycleTimeInput.delegate = self
        self.view.addSubview(self.cycleTimeInput)
        let cycleTimeLine = UIView()
        cycleTimeLine.backgroundColor = UIColor.gray
        cycleTimeLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(cycleTimeLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        
        
        self.initEnableTimeLabel = UILabel()
        self.initEnableTimeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.initEnableTimeLabel.textColor = UIColor.black
        self.initEnableTimeLabel.text = NSLocalizedString("init_enable_time", comment: "Init enable time(ms)")
        self.initEnableTimeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.initEnableTimeLabel.numberOfLines = 0;
        self.initEnableTimeLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.initEnableTimeLabel)
        self.initEnableTimeInput = UITextField()
        self.initEnableTimeInput.font = UIFont.systemFont(ofSize: fontSize)
        self.initEnableTimeInput.textColor = UIColor.black
        self.initEnableTimeInput.borderStyle = UITextField.BorderStyle.bezel
        self.initEnableTimeInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.initEnableTimeInput.delegate = self
        self.view.addSubview(self.initEnableTimeInput)
        let initEnableTimeLine = UIView()
        initEnableTimeLine.backgroundColor = UIColor.gray
        initEnableTimeLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(initEnableTimeLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        
        self.toggleTimeLabel = UILabel()
        self.toggleTimeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.toggleTimeLabel.textColor = UIColor.black
        self.toggleTimeLabel.text = NSLocalizedString("toggle_time", comment: "Toggle times")
        self.toggleTimeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.toggleTimeLabel.numberOfLines = 0;
        self.toggleTimeLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.toggleTimeLabel)
        self.toggleTimeInput = UITextField()
        self.toggleTimeInput.font = UIFont.systemFont(ofSize: fontSize)
        self.toggleTimeInput.textColor = UIColor.black
        self.toggleTimeInput.borderStyle = UITextField.BorderStyle.bezel
        self.toggleTimeInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.toggleTimeInput.delegate = self
        self.view.addSubview(self.toggleTimeInput)
        let toggleTimeLine = UIView()
        toggleTimeLine.backgroundColor = UIColor.gray
        toggleTimeLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(toggleTimeLine)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        
        self.recoverTimeLabel = UILabel()
        self.recoverTimeLabel.font = UIFont.systemFont(ofSize: fontSize)
        self.recoverTimeLabel.textColor = UIColor.black
        self.recoverTimeLabel.text = NSLocalizedString("recover_time", comment: "Recover time(min)")
        self.recoverTimeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.recoverTimeLabel.numberOfLines = 0;
        self.recoverTimeLabel.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.recoverTimeLabel)
        self.recoverTimeInput = UITextField()
        self.recoverTimeInput.font = UIFont.systemFont(ofSize: fontSize)
        self.recoverTimeInput.textColor = UIColor.black
        self.recoverTimeInput.borderStyle = UITextField.BorderStyle.bezel
        self.recoverTimeInput.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.recoverTimeInput.delegate = self
        self.view.addSubview(self.recoverTimeInput)
        let recoverTimeLine = UIView()
        recoverTimeLine.backgroundColor = UIColor.gray
        recoverTimeLine.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(recoverTimeLine)
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
    
    var delegate : EditPulseRelayDelegate?
    @objc private func submit(){
        
        let cycleTime = self.cycleTimeInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let initEnableTime = self.initEnableTimeInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let toggleTime = self.toggleTimeInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let recoverTime = self.recoverTimeInput.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""

        if cycleTime.isEmpty || initEnableTime.isEmpty || toggleTime.isEmpty || recoverTime.isEmpty {
            let errorMessage = NSLocalizedString("input_error", comment: "Input error!")
            Toast.hudBuilder.title(errorMessage).show()
          
            return
        }

        guard var cycleTimeInt = Int(cycleTime), var initEnableTimeInt = Int(initEnableTime), let toggleTimeInt = Int(toggleTime), let recoverTimeInt = Int(recoverTime) else {
            let errorMessage = NSLocalizedString("input_error", comment: "Input error!")
            Toast.hudBuilder.title(errorMessage).show()
            return
        }
        cycleTimeInt = cycleTimeInt / 100 * 100
        initEnableTimeInt = initEnableTimeInt / 100 * 100
        if cycleTimeInt <= 0 || cycleTimeInt > 65535 {
            let warningMessage = NSLocalizedString("cycle_time_warning", comment: "Value must be between 100 and 65535.")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }

        if initEnableTimeInt <= 0 || initEnableTimeInt > 65535 {
            let warningMessage = NSLocalizedString("init_enable_time_warning", comment: "Value must be between 10 and 65535.")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }

        if toggleTimeInt <= 0 || toggleTimeInt > 65535 {
            let warningMessage = NSLocalizedString("toggle_time_warning", comment: "Value must be between 1 and 65535.")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }

        if recoverTimeInt < 0 || recoverTimeInt > 255 {
            let warningMessage = NSLocalizedString("recover_time_warning", comment: "Value must be between 1 and 255.")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }
        if cycleTimeInt < initEnableTimeInt {
            let warningMessage = NSLocalizedString("cycle_time_error_warning", comment: "Cycle time must be greater than init enable time.")
            Toast.hudBuilder.title(warningMessage).show()
            return
        }
        self.delegate?.setPulseRelayValue(cycleTime: cycleTimeInt, initEnableTime: initEnableTimeInt, toggleTime: toggleTimeInt, recoverTime: recoverTimeInt)
        self.navigationController?.popViewController(animated: false)
    }
}
