//
//  EditInstructionSequenceController.swift
//  tftble
//
 
//

import Foundation
import UIKit
import CLXToast

class EditInstructionSequenceController : UIViewController {
 
    private var etCmd:UITextView!
    private var submitBtn:QMUIGhostButton! 
    var connectStatusDelegate:SetConnectStatusDelegate?
    private var fontSize:CGFloat = Utils.fontSize
    
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
        self.tftInstallTopBar(title: NSLocalizedString("send_instruction_sequence", comment: "Send instruction sequence"))
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(
                    target: self,
                    action: #selector(dismissKeyboard))

                tap.cancelsTouchesInView = false
                view.addGestureRecognizer(tap)
        let viewWidth = Int(self.view.bounds.width)
        let navBottomY = Int(self.tftTopContentY())
        self.tftRememberInitialContentTop(CGFloat(navBottomY))
        let editorTopY = navBottomY + 16
        
        self.etCmd = UITextView()
        self.etCmd.font = UIFont.systemFont(ofSize: fontSize)
        self.etCmd.textColor = UIColor.black
        self.etCmd.layer.borderWidth=1
        self.etCmd.frame = CGRect(x:15,y:editorTopY,width:viewWidth - 30,height: 180)
        self.view.addSubview(self.etCmd)
       
       
        self.submitBtn = QMUIGhostButton()
        self.submitBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.submitBtn.setTitle(NSLocalizedString("submit", comment: "Submit"), for: .normal)
        self.submitBtn.ghostColor = UIColor.colorPrimary 
        self.submitBtn.frame = CGRect(x: Int((viewWidth - 200) / 2), y: editorTopY + 190, width: 200, height: 30)
        self.submitBtn.addTarget(self, action: #selector(submit), for:.touchUpInside)
        self.view.addSubview(self.submitBtn)
 
    
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.etCmd.resignFirstResponder()
    }

    var delegate : EditInstructionSequenceDelegate?
    @objc private func submit(){
        var cmdStr = etCmd.text ?? ""
        cmdStr = cmdStr.trimmingCharacters(in: .whitespacesAndNewlines)
        if cmdStr.count == 0{
            Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
            return 
        }
        if cmdStr.count > 400{
             Toast.hudBuilder.title(NSLocalizedString("send_cmd_invalid_len", comment: "The length is incorrect. The length cannot be greater than 200.")).show()
            return 
        }
        cmdStr = cmdStr.replacingOccurrences(of: "0x", with: "")
        cmdStr = cmdStr.replacingOccurrences(of: "0X", with: "")
        cmdStr = cmdStr.replacingOccurrences(of: " ", with: "")
        if Utils.isHexadecimal(cmdStr)  {
            print("All characters are in 0 to f or 0 to F.")
        } else {
            print("Not all characters are in 0 to f or 0 to F.")
                Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
                return
        } 
        self.delegate?.setCmd(cmd: cmdStr)
        self.navigationController?.popViewController(animated: false)
    }
}
