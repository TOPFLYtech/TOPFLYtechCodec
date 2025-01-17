//
//  DialogSubViewLanCell.swift
//  tftble
//
//  Created by jeech onR 2019/12/12.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import JXMarqueeView
import QMUIKit
class DeviceS09Cell: UITableViewCell {
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
    

    lazy var input0Label: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("input0", comment:"input0:")
        return label
    }()
    lazy var input0ContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    lazy var output0Label: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("output0", comment:"output0:")
        return label
    }()
    lazy var output0ContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    lazy var output1Label: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("output1", comment:"output1:")
        return label
    }()
    lazy var output1ContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    
    lazy var analog0Label: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("analogInput0", comment:"analogInput0:")
        return label
    }()
    lazy var analog0ContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    lazy var analog1Label: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("analogInput1", comment:"analogInput1:")
        return label
    }()
    lazy var analog1ContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        return label
    }()
    
    lazy var analog2Label: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
        label.text = NSLocalizedString("analogInput2", comment:"analogInput2:")
        return label
    }()
    lazy var analog2ContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: fontSize)
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
        btn.ghostColor = UIColor.colorPrimary
        return btn
    }()
    
    
    //    override func layoutSubviews() {
    //        super.layoutSubviews()
    //       self.initLayoutPosition()
    //    }
    
    func resetPosition(broadcastType:String){
        //        print("reset position")
        
    }
    
    
    func initLayoutPosition(){
        let descWidth = self.bounds.size.width / 2 - 20
        let contentX = self.bounds.size.width / 2
        self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 440)
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
        
        self.input0Label.frame = CGRect(x: 8, y: 218, width: descWidth, height: 30)
        self.input0ContentLabel.frame = CGRect(x: contentX, y: 218, width: self.bounds.size.width - contentX, height: 30)
        self.output0Label.frame = CGRect(x: 8, y: 248, width: descWidth, height: 30)
        self.output0ContentLabel.frame = CGRect(x: contentX, y: 248, width: self.bounds.size.width - contentX, height: 30)
        self.output1Label.frame = CGRect(x: 8, y: 278, width: descWidth, height: 30)
        self.output1ContentLabel.frame = CGRect(x: contentX, y: 278, width: self.bounds.size.width - contentX, height: 30)
        self.analog0Label.frame = CGRect(x: 8, y: 308, width: descWidth, height: 30)
        self.analog0ContentLabel.frame = CGRect(x: contentX, y: 308, width: self.bounds.size.width - contentX, height: 30)
        self.analog1Label.frame = CGRect(x: 8, y: 338, width: descWidth, height: 30)
        self.analog1ContentLabel.frame = CGRect(x: contentX, y: 338, width: self.bounds.size.width - contentX, height: 30)
        self.analog2Label.frame = CGRect(x: 8, y: 368, width: descWidth, height: 30)
        self.analog2ContentLabel.frame = CGRect(x: contentX, y: 368, width: self.bounds.size.width - contentX, height: 30)
        
        self.configLabel.frame = CGRect(x: 8, y: 398, width: descWidth, height: 30)
        self.configBtn.frame = CGRect(x: contentX, y: 398, width: 80, height: 24)
        self.configBtn.setTitleShadowColor(UIColor.green,for: .highlighted)
        self.configBtn.setTitleShadowColor(UIColor.black,for: .normal)
        
        
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
        
        self.rootView.addSubview(input0Label)
        self.rootView.addSubview(input0ContentLabel)
        self.rootView.addSubview(output0Label)
        self.rootView.addSubview(output0ContentLabel)
        self.rootView.addSubview(output1Label)
        self.rootView.addSubview(output1ContentLabel)
        self.rootView.addSubview(analog0Label)
        self.rootView.addSubview(analog0ContentLabel)
        self.rootView.addSubview(analog1Label)
        self.rootView.addSubview(analog1ContentLabel)
        self.rootView.addSubview(analog2Label)
        self.rootView.addSubview(analog2ContentLabel)
        
        self.rootView.addSubview(configLabel)
        self.rootView.addSubview(configBtn)
        self.rootView.addSubview(idLabel)
        self.rootView.addSubview(idContentLabel)
        
        self.initLayoutPosition()
        
    }
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        snpLayoutSubview()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    static let identifier = "DeviceS09Cell"
    static func dequeueReusable(with tableView:UITableView) -> DeviceS09Cell {
        let reusableCell = tableView.dequeueReusableCell(withIdentifier: DeviceS09Cell.identifier)
        if  let cell = reusableCell as? DeviceS09Cell {
            return cell
        }else{
            return DeviceS09Cell.init(style: .default, reuseIdentifier: DeviceS09Cell.identifier)
        }
    }
    
    
}
