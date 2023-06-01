//
//  EditDoutOutputController.swift
//  tftble
//
 
//

import Foundation
import UIKit
import CLXToast
import QMUIKit
class EditDoutOutputController : UIViewController,UITextFieldDelegate {
    private var dout0Label:UILabel!
    private var dout0Input:UITextField!
    private var dout1Label:UILabel!
    private var dout1Input:UITextField!
    private var modeList:[String]! = [String]()
    private var submitBtn:QMUIGhostButton!
    private var fontSize:CGFloat = Utils.fontSize
 
    public var dout0:Int = -1
    public var dout1:Int = -1 
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
        self.modeList.append( NSLocalizedString("close", comment: "Close"))
        self.modeList.append( NSLocalizedString("open", comment: "Open"))
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = NSLocalizedString("dout_status", comment: "Dout status")
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
        
        self.dout0Label = UILabel()
        self.dout0Label.font = UIFont.systemFont(ofSize: fontSize)
        self.dout0Label.textColor = UIColor.black
        self.dout0Label.text = NSLocalizedString("Dout 0", comment: "Dout 0")
        self.dout0Label.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dout0Label.numberOfLines = 0;
        self.dout0Label.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.dout0Label)
        self.dout0Input = UITextField()
        self.dout0Input.font = UIFont.systemFont(ofSize: fontSize)
        self.dout0Input.textColor = UIColor.black 
        self.dout0Input.borderStyle = UITextField.BorderStyle.bezel
        self.dout0Input.frame = CGRect(x:contentX,y:startInputY,width:descWidth - 20,height: 30)
        self.dout0Input.delegate = self
        self.view.addSubview(self.dout0Input)
        let dout0Line = UIView()
        dout0Line.backgroundColor = UIColor.gray
        dout0Line.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(dout0Line)
        startLabelY += lineHigh
        lineY += lineHigh
        startInputY += lineHigh
        
        self.dout1Label = UILabel()
        self.dout1Label.font = UIFont.systemFont(ofSize: fontSize)
        self.dout1Label.textColor = UIColor.black
        self.dout1Label.text = NSLocalizedString("Dout 1", comment: "Dout 1")
        self.dout1Label.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dout1Label.numberOfLines = 0;
        self.dout1Label.frame = CGRect(x: 15, y: startLabelY, width: descWidth, height: 60)
        self.view.addSubview(self.dout1Label)
        self.dout1Input = UITextField()
        self.dout1Input.font = UIFont.systemFont(ofSize: fontSize)
        self.dout1Input.textColor = UIColor.black
        self.dout1Input.borderStyle = UITextField.BorderStyle.bezel
        self.dout1Input.frame = CGRect(x:contentX,y:startInputY,width:descWidth  - 20,height: 30)
        self.dout1Input.delegate = self
        self.view.addSubview(self.dout1Input)
        let dout1Line = UIView()
        dout1Line.backgroundColor = UIColor.gray
        dout1Line.frame = CGRect(x: 0, y: Double(lineY), width: Double(KSize.width), height: 0.5)
        self.view.addSubview(dout1Line)
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
 
        if self.dout0 != -1 {
            self.dout0Input.text = String(format: "%d", dout0)    
        }
        if self.dout1 != -1 {
             self.dout1Input.text = String(format: "%d", dout1)  
        }
    
    }
 
    func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let allowedCharacters = CharacterSet(charactersIn: "01")
        let characterSet = CharacterSet(charactersIn: string)
        return allowedCharacters.isSuperset(of: characterSet)
    }

    var delegate : EditDoutOutputDelegate?
    @objc private func submit(){
        let dout0Str = self.dout0Input.text ?? ""
        let dout1Str = self.dout1Input.text ?? ""
        self.delegate?.setDoutValue(dout0: Int(dout0Str) ?? 0, dout1: Int(dout1Str) ?? 0)
        self.navigationController?.popViewController(animated: false)
    }
}
