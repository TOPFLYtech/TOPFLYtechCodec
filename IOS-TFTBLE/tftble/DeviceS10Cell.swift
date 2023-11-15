//
//  DeviceS10Cell.swift
//  tftble
//
//  Created by jeech on 2019/12/30.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import JXMarqueeView
import QMUIKit
class DeviceS10Cell: UITableViewCell {
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
    lazy var batteryPercentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("batteryPercentDesc", comment: "Battery:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var batteryPercentContentLabel: UILabel = {
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
    lazy var broadcastTypeLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("BroadcastTypeDesc", comment: "Broadcast type:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var broadcastTypeContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    lazy var ambientLightLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("ambient_light_desc", comment: "Ambient Light:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var ambientLightContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    
    lazy var nidLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("nid_desc", comment: "Name space ID:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var nidContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    lazy var bidLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("bid_desc", comment: "Instance ID:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var bidContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    lazy var majorLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("major_desc", comment: "Beacon major set:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var majorContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    lazy var minorLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("minor_desc", comment: "Beacon minor set:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var minorContentLabel: UILabel = {
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
        btn.ghostColor = UIColor.colorPrimary
        return btn
    }()
    
    
    lazy var switchTempUnitBtn:QMUIGhostButton = {
        let btn = QMUIGhostButton()
        btn.titleLabel?.font = UIFont.systemFont(ofSize: fontSize)
        btn.setTitle("˚F", for: .normal)
        btn.ghostColor = UIColor.colorPrimary
        return btn
    }()
    lazy var humidityLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("humidity", comment: "Humidity:")
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    lazy var humidityContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    var marqueeView:JXMarqueeView!
//    override func layoutSubviews() {
//        super.layoutSubviews()
//        self.initLayoutPosition()
//    }
    func resetPosition(bleDeviceInfo:[String:String]){
        //        print("reset position")
        initDefineLayoutPosition(bleDeviceInfo: bleDeviceInfo)
    }
    
    func initDefineLayoutPosition(bleDeviceInfo:[String:String]){
        self.batteryLabel.isHidden = false
        self.batteryContentLabel.isHidden = false
        self.batteryPercentLabel.isHidden = false
        self.batteryPercentContentLabel.isHidden = false
        self.tempLabel.isHidden = false
        self.tempContentLabel.isHidden = false
        self.switchTempUnitBtn.isHidden = false
        self.humidityLabel.isHidden = false
        self.humidityContentLabel.isHidden = false
        self.ambientLightLabel.isHidden = false
        self.ambientLightContentLabel.isHidden = false
        self.majorLabel.isHidden = false
        self.majorContentLabel.isHidden = false
        self.minorLabel.isHidden = false
        self.minorContentLabel.isHidden = false
        self.warnLabel.isHidden = false
        self.warnContentLabel.isHidden = false
        marqueeView.isHidden = false
        self.nidLabel.isHidden = false
        self.nidContentLabel.isHidden = false
        self.bidLabel.isHidden = false
        self.bidContentLabel.isHidden = false 
        let descWidth = self.bounds.size.width / 2 - 20
        let contentX = self.bounds.size.width / 2
        
        let broadcastType = bleDeviceInfo["broadcastType"] as! String ?? ""
        var height:Int = 8
        self.rootView.isUserInteractionEnabled=true
        self.deviceNameLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
        self.deviceNameContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
        height += 30
        self.idLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
        self.idContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
        height += 30
        self.dateLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
        self.dateContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
        height += 30
        self.rssiLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
        self.rssiContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
        height += 30
        self.modelLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
        self.modelContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
        height += 30
        self.hardwareLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
        self.hardwareContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
        height += 30
        self.softwareLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
        self.softwareContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
        height += 30
        self.broadcastTypeLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
        self.broadcastTypeContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
        height += 30
        
        if broadcastType == "Eddystone UID"{
            self.batteryLabel.isHidden = true
            self.batteryContentLabel.isHidden = true
            self.batteryPercentLabel.isHidden = true
            self.batteryPercentContentLabel.isHidden = true
            self.tempLabel.isHidden = true
            self.tempContentLabel.isHidden = true
            self.switchTempUnitBtn.isHidden = true
            self.humidityLabel.isHidden = true
            self.humidityContentLabel.isHidden = true
            self.ambientLightLabel.isHidden = true
            self.ambientLightContentLabel.isHidden = true
            self.nidLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.nidContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.bidLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.bidContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.majorLabel.isHidden = true
            self.majorContentLabel.isHidden = true
            self.minorLabel.isHidden = true
            self.minorContentLabel.isHidden = true
            self.warnLabel.isHidden = true
            marqueeView.isHidden = true
            self.warnContentLabel.isHidden = true
            self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 350)
        }else if broadcastType == "Long range"{
            self.batteryLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.batteryContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.batteryPercentLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.batteryPercentContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.tempLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.tempContentLabel.frame = CGRect(x: Int(contentX), y: height, width: Int(self.bounds.size.width) - Int(contentX) - 70, height: 30)
            self.switchTempUnitBtn.frame = CGRect(x: Int(self.bounds.size.width) - 70, y: height, width: 50, height: 24)
            self.switchTempUnitBtn.setTitleShadowColor(UIColor.green,for: .highlighted)
            self.switchTempUnitBtn.setTitleShadowColor(UIColor.black,for: .normal)
            height += 30
            self.humidityLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.humidityContentLabel.frame = CGRect(x: contentX, y:CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            
            
            self.ambientLightLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.ambientLightContentLabel.frame = CGRect(x: contentX, y:CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.nidLabel.isHidden = true
            self.nidContentLabel.isHidden = true
            self.bidLabel.isHidden = true
            self.bidContentLabel.isHidden = true
            self.majorLabel.isHidden = true
            self.majorContentLabel.isHidden = true
            self.minorLabel.isHidden = true
            self.minorContentLabel.isHidden = true
            
            self.warnLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.warnContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
//            marqueeView.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 440)
        }else if broadcastType == "Beacon"{
            self.batteryLabel.isHidden = true
            self.batteryContentLabel.isHidden = true
            self.batteryPercentLabel.isHidden = true
            self.batteryPercentContentLabel.isHidden = true
            self.tempLabel.isHidden = true
            self.tempContentLabel.isHidden = true
            self.switchTempUnitBtn.isHidden = true
            self.humidityLabel.isHidden = true
            self.humidityContentLabel.isHidden = true
            self.ambientLightLabel.isHidden = true
            self.ambientLightContentLabel.isHidden = true
            self.nidLabel.isHidden = true
            self.nidContentLabel.isHidden = true
            self.bidLabel.isHidden = true
            self.bidContentLabel.isHidden = true
            self.majorLabel.isHidden = true
            self.majorContentLabel.isHidden = true
            self.minorLabel.isHidden = true
            self.minorContentLabel.isHidden = true
//            self.majorLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
//            self.majorContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
//            height += 30
//            self.minorLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
//            self.minorContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
//            height += 30
            
            self.warnLabel.isHidden = true
            marqueeView.isHidden = true
            self.warnContentLabel.isHidden = true
            self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 290)
        }else{
            self.batteryLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.batteryContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.batteryPercentLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.batteryPercentContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.tempLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.tempContentLabel.frame = CGRect(x: Int(contentX), y: height, width: Int(self.bounds.size.width) - Int(contentX) - 70, height: 30)
            self.switchTempUnitBtn.frame = CGRect(x: Int(self.bounds.size.width) - 70, y: height, width: 50, height: 24)
            self.switchTempUnitBtn.setTitleShadowColor(UIColor.green,for: .highlighted)
            self.switchTempUnitBtn.setTitleShadowColor(UIColor.black,for: .normal)
            height += 30
            self.humidityLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.humidityContentLabel.frame = CGRect(x: contentX, y:CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            
            self.ambientLightLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
            self.ambientLightContentLabel.frame = CGRect(x: contentX, y:CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.nidLabel.isHidden = true
            self.nidContentLabel.isHidden = true
            self.bidLabel.isHidden = true
            self.bidContentLabel.isHidden = true
            self.majorLabel.isHidden = true
            self.majorContentLabel.isHidden = true
            self.minorLabel.isHidden = true
            self.minorContentLabel.isHidden = true
            self.warnContentLabel.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
       
            self.warnLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
//            marqueeView.frame = CGRect(x: contentX, y: CGFloat(height), width: self.bounds.size.width - contentX, height: 30)
            height += 30
            self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 470)
        }
        self.configLabel.frame = CGRect(x: 8, y: height, width: Int(descWidth), height: 30)
        self.configBtn.frame = CGRect(x: Int(contentX), y: height, width: 80, height: 24)
        height += 30
        self.backgroundColor = UIColor.nordicLightGray
        self.rootView.backgroundColor = UIColor.white
        self.rootView.layer.cornerRadius = 8
        self.rootView.layer.masksToBounds = true
        self.rootView.layer.borderWidth = 1
        self.rootView.layer.borderColor = UIColor.nordicLightGray.cgColor
    }
    func initLayoutPosition(){
        
        let descWidth = self.bounds.size.width / 2 - 20
        let contentX = self.bounds.size.width / 2
        self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 380)
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
        self.batteryPercentLabel.frame = CGRect(x: 8, y: 248, width: descWidth, height: 30)
        self.batteryPercentContentLabel.frame = CGRect(x: contentX, y: 248, width: self.bounds.size.width - contentX, height: 30)
        self.humidityLabel.frame = CGRect(x: 8, y: 278, width: descWidth, height: 30)
        self.humidityContentLabel.frame = CGRect(x: contentX, y:278, width: self.bounds.size.width - contentX, height: 30)
        self.broadcastTypeLabel.frame = CGRect(x: 8, y: 308, width: descWidth, height: 30)
        self.broadcastTypeContentLabel.frame = CGRect(x: contentX, y: 308, width: self.bounds.size.width - contentX, height: 30)
        
        self.ambientLightLabel.frame = CGRect(x: 8, y: 338, width: descWidth, height: 30)
        self.ambientLightContentLabel.frame = CGRect(x: contentX, y:338, width: self.bounds.size.width - contentX, height: 30)
        self.nidLabel.frame = CGRect(x: 8, y: 368, width: descWidth, height: 30)
        self.nidContentLabel.frame = CGRect(x: contentX, y: 368, width: self.bounds.size.width - contentX, height: 30)
        self.bidLabel.frame = CGRect(x: 8, y: 398, width: descWidth, height: 30)
        self.bidContentLabel.frame = CGRect(x: contentX, y: 398, width: self.bounds.size.width - contentX, height: 30)
        self.majorLabel.frame = CGRect(x: 8, y: 428, width: descWidth, height: 30)
        self.majorContentLabel.frame = CGRect(x: contentX, y: 428, width: self.bounds.size.width - contentX, height: 30)
        self.minorLabel.frame = CGRect(x: 8, y: 458, width: descWidth, height: 30)
        self.minorContentLabel.frame = CGRect(x: contentX, y: 458, width: self.bounds.size.width - contentX, height: 30)
        
        self.warnLabel.frame = CGRect(x: 8, y: 488, width: descWidth, height: 30)
        self.warnContentLabel.frame = CGRect(x: contentX, y: 488, width: self.bounds.size.width - contentX, height: 30)
//        marqueeView.frame = CGRect(x: contentX, y: 488, width: self.bounds.size.width - contentX, height: 30)
        self.configLabel.frame = CGRect(x: 8, y: 518, width: descWidth, height: 30)
        self.configBtn.frame = CGRect(x: contentX, y: 518, width: 80, height: 24)
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
        
        self.rootView.addSubview(batteryPercentLabel)
        self.rootView.addSubview(batteryPercentContentLabel)
        self.rootView.addSubview(broadcastTypeLabel)
        self.rootView.addSubview(broadcastTypeContentLabel)
        self.rootView.addSubview(ambientLightLabel)
        self.rootView.addSubview(ambientLightContentLabel)
        self.rootView.addSubview(nidLabel)
        self.rootView.addSubview(nidContentLabel)
        self.rootView.addSubview(bidLabel)
        self.rootView.addSubview(bidContentLabel)
        self.rootView.addSubview(majorLabel)
        self.rootView.addSubview(majorContentLabel)
        self.rootView.addSubview(minorLabel)
        self.rootView.addSubview(minorContentLabel)
        
        self.rootView.addSubview(tempLabel)
        self.rootView.addSubview(tempContentLabel)
        self.rootView.addSubview(switchTempUnitBtn)
        
        
        self.rootView.addSubview(humidityLabel)
        self.rootView.addSubview(humidityContentLabel)
        self.rootView.addSubview(warnLabel)
        self.rootView.addSubview(warnContentLabel)
        self.rootView.addSubview(configLabel)
        self.rootView.addSubview(configBtn)
        self.rootView.addSubview(idLabel)
        self.rootView.addSubview(idContentLabel)
        
        
        marqueeView = JXMarqueeView()
        marqueeView.contentView = self.warnContentLabel
        marqueeView.contentMargin = 50
        marqueeView.marqueeType = .left
//        self.rootView.addSubview(marqueeView)
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
    
    static let identifier = "DeviceS10Cell"
    static func dequeueReusable(with tableView:UITableView) -> DeviceS10Cell {
        let reusableCell = tableView.dequeueReusableCell(withIdentifier: DeviceS10Cell.identifier)
        if  let cell = reusableCell as? DeviceS10Cell {
            return cell
        }else{
            return DeviceS10Cell.init(style: .default, reuseIdentifier: DeviceS10Cell.identifier)
        }
    }
}
