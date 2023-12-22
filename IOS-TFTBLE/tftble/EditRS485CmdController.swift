//
//  EditRS485CmdController.swift
//  tftble
//
 
//

import Foundation
import UIKit
import CLXToast
import QMUIKit
class EditRS485CmdController : UIViewController {
 
    private var etCmd:UITextView!
    private var submitBtn:QMUIGhostButton!
    
    private var cbIsHex: CheckBox!
    private var cbAddLineEnd: CheckBox!
    private var  isHexChecked:Bool!
    private var  isAddLineEndChecked:Bool!
    var connectStatusDelegate:SetConnectStatusDelegate?
 
    private var fontSize:CGFloat = Utils.fontSize
    override func viewDidDisappear(_ animated: Bool) {
        self.connectStatusDelegate?.setConnectStatus()
    }
    
    override func viewDidLoad() {
        self.view.backgroundColor = UIColor.white
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = NSLocalizedString("send_data", comment: "RS485 send")
        self.navigationItem.titleView = titleLabel
         
       
        self.etCmd = UITextView()
        self.etCmd.font = UIFont.systemFont(ofSize: fontSize)
        self.etCmd.textColor = UIColor.black
        self.etCmd.layer.borderWidth=1
        self.etCmd.frame = CGRect(x:15,y:80,width:Int(KSize.width) - 30,height: 180)
        self.view.addSubview(self.etCmd)
        
     
        
        self.cbIsHex = CheckBox.init()
        self.cbIsHex.frame = CGRect(x: 10, y: 270, width: 25, height: 25)
        self.cbIsHex.style = .tick
        self.cbIsHex.borderStyle = .roundedSquare(radius: 5)
        self.cbIsHex.addTarget(self, action: #selector(cbIsHexTapped), for: .valueChanged)
        view.addSubview( self.cbIsHex)
        let isHexLb = UILabel()
        isHexLb.font = UIFont.systemFont(ofSize: fontSize)
        isHexLb.text = NSLocalizedString("send_by_hex", comment: "Hex data")
        isHexLb.frame = CGRect(x: 40, y: 270, width: 250, height: 30)
        view.addSubview( isHexLb)
	
    	self.cbAddLineEnd = CheckBox.init()
        self.cbAddLineEnd.frame = CGRect(x: 10, y: 300, width: 25, height: 25)
        self.cbAddLineEnd.style = .tick
        self.cbAddLineEnd.borderStyle = .roundedSquare(radius: 5)
        self.cbAddLineEnd.addTarget(self, action: #selector(cbAddLineEndTapped), for: .valueChanged)
        view.addSubview( self.cbAddLineEnd)
        let addLineEndLb = UILabel()
        addLineEndLb.font = UIFont.systemFont(ofSize: fontSize)
        addLineEndLb.lineBreakMode = NSLineBreakMode.byWordWrapping;
        addLineEndLb.numberOfLines = 0;
        addLineEndLb.text = NSLocalizedString("add_line_end", comment: "Automatically add [0D 0A]at the end of sending")
        addLineEndLb.frame = CGRect(x: 40, y: 300, width: 250, height: 30)
        view.addSubview( addLineEndLb)
        self.submitBtn = QMUIGhostButton()
        self.submitBtn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        self.submitBtn.setTitle(NSLocalizedString("submit", comment: "Submit"), for: .normal)
        self.submitBtn.ghostColor = UIColor.colorPrimary 
        self.submitBtn.frame = CGRect(x: Int((KSize.width - 200) / 2), y: 340, width: 200, height: 30)
        self.submitBtn.addTarget(self, action: #selector(submit), for:.touchUpInside)
        self.view.addSubview(self.submitBtn)


        isHexChecked = UserDefaults.standard.bool(forKey: "rs485_send_data_use_hex")
        isAddLineEndChecked = UserDefaults.standard.bool(forKey: "rs485_send_data_add_line_end")
        self.cbAddLineEnd.isChecked = isAddLineEndChecked
        self.cbIsHex.isChecked = isHexChecked
    
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.etCmd.resignFirstResponder()
    }
    @objc func cbIsHexTapped(_ sender: CheckBox) {
        isHexChecked = self.cbIsHex.isChecked
        UserDefaults.standard.set(isHexChecked, forKey: "rs485_send_data_use_hex") 

    }
    @objc func cbAddLineEndTapped(_ sender: CheckBox) {
        isAddLineEndChecked = self.cbAddLineEnd.isChecked
        UserDefaults.standard.set(isAddLineEndChecked, forKey: "rs485_send_data_add_line_end")
    }

    var delegate : EditRS485CmdDelegate?
    @objc private func submit(){
        var cmdStr = etCmd.text ?? ""
        cmdStr = cmdStr.trimmingCharacters(in: .whitespacesAndNewlines)
        if cmdStr.count == 0{
            Toast.hudBuilder.title(NSLocalizedString("input_error", comment: "Input error!")).show()
            return 
        }
        if isHexChecked{
            var checkLen: Int = 400
            if isAddLineEndChecked{
                checkLen -= 4
            }
            if cmdStr.count > checkLen{
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
             if cmdStr.count % 2 != 0{
                Toast.hudBuilder.title(NSLocalizedString("hex_data_len_error", comment: "Hexadecimal data length must be an even number")).show()
                return 
            }
        }else{
            var checkLen: Int = 200
            if isAddLineEndChecked{
                checkLen -= 2
            }
            if cmdStr.count > checkLen{
                Toast.hudBuilder.title(NSLocalizedString("send_cmd_invalid_len", comment: "The length is incorrect. The length cannot be greater than 200.")).show()
                return 
            }
            let data = Data(cmdStr.utf8)
            cmdStr = Utils.uint8ToHexStr(value:data[0])
        } 
        if isAddLineEndChecked{
            cmdStr += "0d0a"
        }

        
        self.delegate?.setRS485Cmd(cmd: cmdStr)
        self.navigationController?.popViewController(animated: false)
    }
}
