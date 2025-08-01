//
//  HistorySubLockHeader.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/6/5.
//  Copyright © 2025 com.tftiot. All rights reserved.
//


import Foundation
import UIKit
class HistorySubLockHeader:UITableViewHeaderFooterView{
    lazy var dateLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: 14)
            return label
        }()
        lazy var alarmLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: 14)
            return label
        }()
        lazy var lockStatusLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: 14)
            return label
        }()
        lazy var deviceStatusLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: 14)
            return label
        }()
        lazy var solarVoltageLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: 14)
            return label
        }()
        lazy var batteryVoltageLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: 14)
            return label
        }()
        lazy var batteryLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: 14)
            return label
        }()
        lazy var tempLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: 14)
            return label
        }()
        lazy var unclockIdLabel: UILabel = {
            let label = UILabel()
            label.textColor = UIColor.black
            label.font = UIFont.systemFont(ofSize: 14)
            return label
        }()
         
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        self.contentView.backgroundColor = UIColor.white 
        self.contentView.addSubview(dateLabel)
        self.contentView.addSubview(alarmLabel)
        self.contentView.addSubview(lockStatusLabel)
        self.contentView.addSubview(deviceStatusLabel)
        self.contentView.addSubview(solarVoltageLabel)
        self.contentView.addSubview(batteryVoltageLabel)
        self.contentView.addSubview(batteryLabel)
        self.contentView.addSubview(tempLabel)
        self.contentView.addSubview(unclockIdLabel)
        var labelX = 0
        let height = 40
        let labelY = 0
        self.dateLabel.frame = CGRect(x: labelX, y: labelY, width: 160, height: height)
        labelX += 160
        self.alarmLabel.frame = CGRect(x: labelX, y: labelY, width: 240, height: height)
        labelX += 240
        self.lockStatusLabel.frame = CGRect(x: labelX, y: labelY, width: 240, height: height)
        labelX += 240
        self.deviceStatusLabel.frame = CGRect(x: labelX, y: labelY, width: 240, height: height)
        labelX += 240
         self.solarVoltageLabel.frame = CGRect(x: labelX, y: labelY, width:100, height: height)
        labelX += 100
         self.batteryVoltageLabel.frame = CGRect(x: labelX, y: labelY, width:100, height: height)
        labelX += 100
        self.batteryLabel.frame = CGRect(x: labelX, y: labelY, width:80, height: height)
        labelX += 80
         self.tempLabel.frame = CGRect(x: labelX, y: labelY, width:80, height: height)
        labelX += 80
        self.unclockIdLabel.frame = CGRect(x: labelX, y: labelY, width: 160, height: height)
        labelX += 160
        self.dateLabel.text = NSLocalizedString("table_head_date", comment: "Date")
        self.dateLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dateLabel.numberOfLines = 0;
        self.dateLabel.textAlignment = .center
        self.alarmLabel.text = NSLocalizedString("table_head_alarm", comment: "Alarm")
        self.alarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.alarmLabel.numberOfLines = 0;
        self.alarmLabel.textAlignment = .center
        self.lockStatusLabel.text = NSLocalizedString("table_head_lock_status", comment: "Lock status")
        self.lockStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.lockStatusLabel.numberOfLines = 0;
        self.lockStatusLabel.textAlignment = .center
        self.deviceStatusLabel.text = NSLocalizedString("table_head_device_status", comment: "table_head_device_status")
        self.deviceStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.deviceStatusLabel.numberOfLines = 0;
        self.deviceStatusLabel.textAlignment = .center
        self.solarVoltageLabel.text = NSLocalizedString("solar_voltage", comment: "solar_voltage")
        self.solarVoltageLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.solarVoltageLabel.numberOfLines = 0;
        self.solarVoltageLabel.textAlignment = .center
        self.batteryVoltageLabel.text = NSLocalizedString("battery_voltage", comment: "battery_voltage")
        self.batteryVoltageLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.batteryVoltageLabel.numberOfLines = 0;
        self.batteryVoltageLabel.textAlignment = .center
        self.batteryLabel.text = NSLocalizedString("table_head_battery", comment: "Battery")
        self.batteryLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.batteryLabel.numberOfLines = 0;
        self.batteryLabel.textAlignment = .center
        self.tempLabel.text = NSLocalizedString("temp", comment: "temp")
        self.tempLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.tempLabel.numberOfLines = 0;
        self.tempLabel.textAlignment = .center
        self.unclockIdLabel.text = NSLocalizedString("unlock_id", comment: "unlock_id")
        self.unclockIdLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.unclockIdLabel.numberOfLines = 0;
        self.unclockIdLabel.textAlignment = .center
         
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
