//
//  DeviceA002Cell.swift
//  tftble
//
//  Created by jeech on 2020/1/6.
//  Copyright © 2020 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import JXMarqueeView
import QMUIKit
class DeviceA002Cell: UITableViewCell {
    private var fontSize:CGFloat = Utils.fontSize
    lazy var deviceNameLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text =  NSLocalizedString("device_name_desc", comment: "Device name:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var deviceNameContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var idLabel: UILabel = {
        let label = UILabel()
        label.text = NSLocalizedString("id", comment: "ID:")
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var idContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var rootView :UIImageView = {
        let view = UIImageView()
        return view
    }()
    lazy var dateLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
         label.text = NSLocalizedString("date", comment: "Date:")
        return label
    }()
    lazy var dateContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var rssiLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("rssi", comment: "RSSI:")
        return label
    }()
    lazy var rssiContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var modelLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
         label.text = NSLocalizedString("device_model", comment: "Device model:")
        return label
    }()
    lazy var modelContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var hardwareLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("hardware", comment: "Hardware:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var hardwareContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var softwareLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("software", comment:"Software:")
        return label
    }()
    lazy var softwareContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var batteryLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("battery", comment: "Battery:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var batteryContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var inputLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
       label.text = NSLocalizedString("input_acc", comment: "ACC:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var inputContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var relayInputLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("relay_input", comment: "Relay Input:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var relayInputContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var negativeTriggerOneLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("negative_trigger_one", comment: "DOUT1:")
        return label
    }()
    lazy var negativeTriggerOneContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.lineBreakMode = NSLineBreakMode.byWordWrapping;
        label.numberOfLines = 0;
        return label
    }()
    lazy var negativeTriggerTwoLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("negative_trigger_two", comment: "DOUT2:")
        return label
    }()
    lazy var negativeTriggerTwoContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.lineBreakMode = NSLineBreakMode.byWordWrapping;
        label.numberOfLines = 0;
        return label
    }()
    lazy var relayOutputLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("relay_output", comment: "Relay status:")
        return label
    }()
    lazy var relayOutputContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.lineBreakMode = NSLineBreakMode.byWordWrapping;
        label.numberOfLines = 0;
        return label
    }()
    lazy var configLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("configDesc", comment: "Config:")
        return label
    }()
    lazy var configBtn:QMUIGhostButton = {
        let btn = QMUIGhostButton()
        btn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        btn.setTitle(NSLocalizedString("config", comment: "Config"), for: .normal)
//        btn.setTitleColor(UIColor.black, for: .normal)
//        //        btn.layer.borderColor = UIColor.blue.cgColor
//        btn.layer.cornerRadius = 6.0;//6.0是圆角的弧度，根据需求自己更改
//        btn.layer.borderWidth = 1.0;//设置边框宽度
//        btn.backgroundColor = UIColor.nordicLightGray
        btn.ghostColor = UIColor.colorPrimary
        return btn
    }()
    
    lazy var resetPwdLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: fontSize)
            label.text = NSLocalizedString("forget_pwd", comment: "Forget password?")
            return label
        }()
        lazy var resetPwdBtn:QMUIGhostButton = {
            let btn = QMUIGhostButton()
            btn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
           btn.setTitle(NSLocalizedString("reset", comment: "Reset"), for: .normal)
    //        btn.setTitleColor(UIColor.black, for: .normal)
    //        btn.layer.cornerRadius = 6.0;//6.0是圆角的弧度，根据需求自己更改
    //        btn.layer.borderWidth = 1.0;//设置边框宽度
    //        btn.backgroundColor = UIColor.nordicLightGray
            btn.ghostColor = UIColor.colorPrimary
            return btn
        }()
    
    var marqueeView:JXMarqueeView!
//    override func layoutSubviews() {
//        super.layoutSubviews()
//       self.initLayoutPosition()
//    }
    
    
     func resetPosition(version:String){
         //        print("reset position")
         let curVersion = version.replacingOccurrences(of: "V", with: "").replacingOccurrences(of: "v", with: "").replacingOccurrences(of: ".", with: "")
         initLayoutPosition()
     }
     
    func initLayoutPosition(){ 
        let descWidth:Int = Int(self.bounds.size.width / 2 - 20)
        let contentX:Int = Int(self.bounds.size.width / 2)
         
        var height:Int = 8
        let contentLabelWidth:Int = Int(self.bounds.size.width) - contentX
        self.rootView.isUserInteractionEnabled=true
        self.deviceNameLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.deviceNameContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.idLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.idContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.dateLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.dateContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.rssiLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.rssiContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.modelLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.modelContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.hardwareLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.hardwareContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.softwareLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.softwareContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.batteryLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.batteryContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        
        self.inputLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.inputContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.relayInputLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.relayInputContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.negativeTriggerOneLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.negativeTriggerOneContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.negativeTriggerTwoLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.negativeTriggerTwoContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        self.relayOutputLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.relayOutputContentLabel.frame = CGRect(x: contentX, y: height, width: contentLabelWidth, height: 30)
        height += 30
        
        self.configLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.configBtn.frame = CGRect(x: contentX, y: height, width: 80, height: 24)
        height += 30
        self.resetPwdLabel.frame = CGRect(x: 8, y: height, width: descWidth, height: 30)
        self.resetPwdBtn.frame = CGRect(x: contentX, y: height, width: 80, height: 24)
        height += 30
        self.backgroundColor = UIColor.nordicLightGray
        self.rootView.backgroundColor = UIColor.white
        self.rootView.layer.cornerRadius = 8
        self.rootView.layer.masksToBounds = true
        self.rootView.layer.borderWidth = 1
        self.rootView.layer.borderColor = UIColor.nordicLightGray.cgColor
        self.rootView.frame = CGRect(x: 5, y: 5, width: Int(self.bounds.size.width)-10, height: height + 12)

    }
    func snpLayoutSubview(){
        self.selectionStyle = UITableViewCell.SelectionStyle.none
        self.contentView.addSubview(rootView)
        self.rootView.addSubview(deviceNameLabel)
        self.rootView.addSubview(deviceNameContentLabel)
        self.rootView.addSubview(dateLabel)
        self.rootView.addSubview(dateContentLabel)
        self.rootView.addSubview(rssiLabel)
        self.rootView.addSubview(rssiContentLabel)
        self.rootView.addSubview(modelLabel)
        self.rootView.addSubview(modelContentLabel)
        self.rootView.addSubview(hardwareLabel)
        self.rootView.addSubview(hardwareContentLabel)
        self.rootView.addSubview(softwareLabel)
        self.rootView.addSubview(softwareContentLabel)
        self.rootView.addSubview(batteryLabel)
        self.rootView.addSubview(batteryContentLabel)
        self.rootView.addSubview(inputLabel)
        self.rootView.addSubview(inputContentLabel)
        self.rootView.addSubview(relayInputLabel)
        self.rootView.addSubview(relayInputContentLabel)
        self.rootView.addSubview(negativeTriggerOneLabel)
        self.rootView.addSubview(negativeTriggerOneContentLabel)
        self.rootView.addSubview(negativeTriggerTwoLabel)
        self.rootView.addSubview(negativeTriggerTwoContentLabel)
        self.rootView.addSubview(relayOutputLabel)
        self.rootView.addSubview(relayOutputContentLabel)
        self.rootView.addSubview(configLabel)
        self.rootView.addSubview(configBtn)
        self.rootView.addSubview(idLabel)
        self.rootView.addSubview(idContentLabel)
        self.rootView.addSubview(self.resetPwdLabel)
        self.rootView.addSubview(self.resetPwdBtn)
        let descWidth = self.bounds.size.width / 2 - 20
        let contentX = self.bounds.size.width / 2
       
         
        self.initLayoutPosition()
    }
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        snpLayoutSubview()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    static let identifier = "DeviceA002Cell"
    static func dequeueReusable(with tableView:UITableView) -> DeviceA002Cell {
        let reusableCell = tableView.dequeueReusableCell(withIdentifier: DeviceA002Cell.identifier)
        if  let cell = reusableCell as? DeviceA002Cell {
            return cell
        }else{
            return DeviceA002Cell.init(style: .default, reuseIdentifier: DeviceA002Cell.identifier)
        }
    }
}

