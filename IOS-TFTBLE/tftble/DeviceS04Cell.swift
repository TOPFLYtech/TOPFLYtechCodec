//
//  DeviceS04Cell.swift
//  tftble
//
//  Created by jeech on 2019/12/30.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import JXMarqueeView
import QMUIKit
class DeviceS04Cell: UITableViewCell {
    private var fontSize:CGFloat = Utils.fontSize
    lazy var deviceNameLabel: UILabel = {
        let label = UILabel()
        label.text =  NSLocalizedString("device_name_desc", comment: "Device name:")
        label.textColor = UIColor.black
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
    lazy var tempLabel: UILabel = {
        let label = UILabel()
         label.textColor = UIColor.black
        label.text = NSLocalizedString("temperature", comment: "Temperature:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var tempContentLabel: UILabel = {
        let label = UILabel()
         label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var doorLabel: UILabel = {
        let label = UILabel()
         label.textColor = UIColor.black
        label.text = NSLocalizedString("door", comment: "Door:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var doorContentLabel: UILabel = {
        let label = UILabel()
         label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var warnLabel: UILabel = {
        let label = UILabel()
         label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text =  NSLocalizedString("warn", comment: "Warn:")
        return label
    }()
    lazy var warnContentLabel: UILabel = {
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
        label.text =  NSLocalizedString("configDesc", comment: "Config:")
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
    
    lazy var qrCodeLabel: UILabel = {
            let label = UILabel()
         label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: fontSize)
            label.text = NSLocalizedString("id_qr_code", comment: "ID QR Code:")
            return label
        }()
        lazy var qrCodeBtn:QMUIGhostButton = {
            let btn = QMUIGhostButton()
            btn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
            btn.setTitle(NSLocalizedString("qr_code", comment: "QR Code"), for: .normal)
//            btn.setTitleColor(UIColor.black, for: .normal)
    //        btn.layer.borderColor = UIColor.blue.cgColor
//            btn.layer.cornerRadius = 6.0;//6.0是圆角的弧度，根据需求自己更改
//            btn.layer.borderWidth = 1.0;//设置边框宽度
//            btn.backgroundColor = UIColor.nordicLightGray
            btn.ghostColor = UIColor.colorPrimary
            return btn
        }()
    
    lazy var switchTempUnitBtn:QMUIGhostButton = {
        let btn = QMUIGhostButton()
        btn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        btn.setTitle("˚F", for: .normal)
//        btn.setTitleColor(UIColor.black, for: .normal)
//        //        btn.layer.borderColor = UIColor.blue.cgColor
//        btn.layer.cornerRadius = 6.0;//6.0是圆角的弧度，根据需求自己更改
//        btn.layer.borderWidth = 1.0;//设置边框宽度
//        btn.backgroundColor = UIColor.nordicLightGray
        btn.ghostColor = UIColor.colorPrimary
        return btn
    }()
    var marqueeView:JXMarqueeView!
    override func layoutSubviews() {
        super.layoutSubviews()
       self.initLayoutPosition()
    }
    
    
    func initLayoutPosition(){
        let descWidth = self.bounds.size.width / 2 - 20
        let contentX = self.bounds.size.width / 2
        if Utils.isDebug{
           self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 420)
        }else{
            self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 380)
        }
        self.rootView.isUserInteractionEnabled=true
        self.deviceNameLabel.frame = CGRect(x: 8, y: 8, width: descWidth, height: 30)
        self.deviceNameContentLabel.frame = CGRect(x: contentX, y: 8, width: self.bounds.size.width - contentX, height: 30)
        self.idLabel.frame = CGRect(x: 8, y: 38, width: descWidth, height: 30)
        self.idContentLabel.frame = CGRect(x: contentX, y: 38, width: self.bounds.size.width - contentX, height: 30)
        self.dateLabel.frame = CGRect(x: 8, y: 68, width: descWidth, height: 30)
        self.dateContentLabel.frame = CGRect(x: contentX, y: 68, width: self.bounds.size.width - contentX, height: 30)
        self.rssiLabel.frame = CGRect(x: 8, y: 98, width: descWidth, height: 30)
        self.rssiContentLabel.frame = CGRect(x: contentX, y: 98, width: self.bounds.size.width - contentX, height: 30)
        self.modelLabel.frame = CGRect(x: 8, y: 128, width: descWidth, height: 30)
        self.modelContentLabel.frame = CGRect(x: contentX, y: 128, width: self.bounds.size.width - contentX, height: 30)
        self.hardwareLabel.frame = CGRect(x: 8, y: 158, width: descWidth, height: 30)
        self.hardwareContentLabel.frame = CGRect(x: contentX, y: 158, width: self.bounds.size.width - contentX, height: 30)
        self.softwareLabel.frame = CGRect(x: 8, y: 188, width: descWidth, height: 30)
        self.softwareContentLabel.frame = CGRect(x: contentX, y: 188, width: self.bounds.size.width - contentX, height: 30)
        self.batteryLabel.frame = CGRect(x: 8, y: 218, width: descWidth, height: 30)
        self.batteryContentLabel.frame = CGRect(x: contentX, y: 218, width: self.bounds.size.width - contentX, height: 30)
        self.tempLabel.frame = CGRect(x: 8, y: 248, width: descWidth, height: 30)
        self.tempContentLabel.frame = CGRect(x: contentX, y: 248, width: self.bounds.size.width - contentX - 70, height: 30)
        self.switchTempUnitBtn.frame = CGRect(x: self.bounds.size.width - 70, y: 248, width: 50, height: 24)
        self.doorLabel.frame = CGRect(x: 8, y: 278, width: descWidth, height: 30)
        self.doorContentLabel.frame = CGRect(x: contentX, y: 278, width: self.bounds.size.width - contentX, height: 30)
        self.warnLabel.frame = CGRect(x: 8, y: 308, width: descWidth, height: 30)
        warnContentLabel.frame = CGRect(x: contentX, y: 308, width: self.bounds.size.width - contentX, height: 30)
        self.configLabel.frame = CGRect(x: 8, y: 338, width: descWidth, height: 30)
        self.configBtn.frame = CGRect(x: contentX, y: 338, width: 80, height: 24)
        self.qrCodeLabel.frame = CGRect(x: 8, y: 368, width: descWidth, height: 30)
        self.qrCodeBtn.frame = CGRect(x: contentX, y: 378, width: 80, height: 24)
        self.backgroundColor = UIColor.nordicLightGray
        self.rootView.backgroundColor = UIColor.white
        self.rootView.layer.cornerRadius = 8
        self.rootView.layer.masksToBounds = true
        self.rootView.layer.borderWidth = 1
        self.rootView.layer.borderColor = UIColor.nordicLightGray.cgColor
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
        self.rootView.addSubview(tempLabel)
        self.rootView.addSubview(tempContentLabel)
        self.rootView.addSubview(doorLabel)
        self.rootView.addSubview(doorContentLabel)
        self.rootView.addSubview(warnLabel)
        self.rootView.addSubview(warnContentLabel)
        self.rootView.addSubview(configLabel)
        self.rootView.addSubview(configBtn)
        self.rootView.addSubview(idLabel)
        self.rootView.addSubview(idContentLabel)
        self.rootView.addSubview(switchTempUnitBtn)
        if Utils.isDebug{
           self.rootView.addSubview(qrCodeLabel)
           self.rootView.addSubview(qrCodeBtn)
        }
        
        
//        marqueeView = JXMarqueeView()
//        marqueeView.contentView = self.warnContentLabel
//        marqueeView.contentMargin = 50
//        marqueeView.marqueeType = .left
        self.rootView.addSubview(warnContentLabel)
         self.initLayoutPosition()
        
    }
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        snpLayoutSubview()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    static let identifier = "DeviceS04Cell"
    static func dequeueReusable(with tableView:UITableView) -> DeviceS04Cell {
        let reusableCell = tableView.dequeueReusableCell(withIdentifier: DeviceS04Cell.identifier)
        if  let cell = reusableCell as? DeviceS04Cell {
            return cell
        }else{
            return DeviceS04Cell.init(style: .default, reuseIdentifier: DeviceS04Cell.identifier)
        }
    }
}
