//
//  DeviceDfu.swift
//  tftble
//
//  Created by jeech on 2020/1/18.
//  Copyright © 2020 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import JXMarqueeView
import QMUIKit
class DeviceDfuCell: UITableViewCell {
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
        btn.setTitle(NSLocalizedString("upgrade", comment: "Upgrade"), for: .normal)
        btn.ghostColor = UIColor.colorPrimary
        return btn
    }()
    
    
     override func layoutSubviews() {
         super.layoutSubviews()
        self.initLayoutPosition()
     }
    func initLayoutPosition(){
        let descWidth = self.bounds.size.width / 2 - 20
        let contentX = self.bounds.size.width / 2
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
        self.configLabel.frame = CGRect(x: 8, y: 158, width: descWidth, height: 30)
        self.configBtn.frame = CGRect(x: contentX, y: 158, width: 80, height: 24)
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
        self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 200)
        self.rootView.addSubview(idLabel)
        self.rootView.addSubview(idContentLabel)
        self.rootView.addSubview(configLabel)
        self.rootView.addSubview(configBtn)
        self.initLayoutPosition()
        
    }
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        snpLayoutSubview()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    static let identifier = "DeviceDfuCell"
    static func dequeueReusable(with tableView:UITableView) -> DeviceDfuCell {
        let reusableCell = tableView.dequeueReusableCell(withIdentifier: DeviceDfuCell.identifier)
        if  let cell = reusableCell as? DeviceDfuCell {
            return cell
        }else{
            return DeviceDfuCell.init(style: .default, reuseIdentifier: DeviceDfuCell.identifier)
        }
    }
}
