//
//  HistoryHeader.swift
//  TFT Elock
//
//  Created by china topflytech on 2023/5/22.
//  Copyright © 2023 com.tftiot. All rights reserved.
//


import Foundation
import UIKit
class HistoryHeader:UITableViewHeaderFooterView{
    lazy var logitudeLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    lazy var latitudeLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    lazy var dateLabel: UILabel = {
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
    lazy var speedLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    lazy var mileageLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    lazy var satelliteLabel: UILabel = {
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
    lazy var networkSignalLabel: UILabel = {
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
    
    override init(reuseIdentifier: String?) {
        super.init(reuseIdentifier: reuseIdentifier)
        self.contentView.backgroundColor = UIColor.white
        self.contentView.addSubview(logitudeLabel)
        self.contentView.addSubview(latitudeLabel)
        self.contentView.addSubview(dateLabel)
        self.contentView.addSubview(lockStatusLabel)
        self.contentView.addSubview(speedLabel)
        self.contentView.addSubview(mileageLabel)
        self.contentView.addSubview(satelliteLabel)
        self.contentView.addSubview(batteryLabel)
        self.contentView.addSubview(networkSignalLabel)
        self.contentView.addSubview(alarmLabel)
        var labelX = 0
        let height = 40
        let labelY = 0
        self.logitudeLabel.frame = CGRect(x: labelX, y: labelY, width: 200, height: height)
        labelX += 200
        self.latitudeLabel.frame = CGRect(x: labelX, y: labelY, width: 200, height: height)
        labelX += 200
        self.dateLabel.frame = CGRect(x: labelX, y: labelY, width: 160, height: height)
        labelX += 160
        self.lockStatusLabel.frame = CGRect(x: labelX, y: labelY, width: 240, height: height)
        labelX += 240
        self.speedLabel.frame = CGRect(x: labelX, y: labelY, width:100, height: height)
        labelX += 100
        self.mileageLabel.frame = CGRect(x: labelX, y: labelY, width:160, height: height)
        labelX += 160
        self.satelliteLabel.frame = CGRect(x: labelX, y: labelY, width:80, height: height)
        labelX += 80
        self.batteryLabel.frame = CGRect(x: labelX, y: labelY, width:80, height: height)
        labelX += 80
        self.networkSignalLabel.frame = CGRect(x: labelX, y: labelY, width: 80, height: height)
        labelX += 80
        self.alarmLabel.frame = CGRect(x: labelX, y: labelY, width: 240, height: height)
        labelX += 240
        self.logitudeLabel.text = NSLocalizedString("table_head_longitude", comment: "Longitude")
        self.logitudeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.logitudeLabel.numberOfLines = 0;
        self.logitudeLabel.textAlignment = .center
        self.latitudeLabel.text = NSLocalizedString("table_head_latitude", comment: "Latitude")
        self.latitudeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.latitudeLabel.numberOfLines = 0;
        self.latitudeLabel.textAlignment = .center
        self.dateLabel.text = NSLocalizedString("table_head_date", comment: "Date")
        self.dateLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dateLabel.numberOfLines = 0;
        self.dateLabel.textAlignment = .center
        self.lockStatusLabel.text = NSLocalizedString("table_head_lock_status", comment: "Lock status")
        self.lockStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.lockStatusLabel.numberOfLines = 0;
        self.lockStatusLabel.textAlignment = .center
        self.speedLabel.text = NSLocalizedString("table_head_speed", comment: "Speed(km/h)")
        self.speedLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.speedLabel.numberOfLines = 0;
        self.speedLabel.textAlignment = .center
        self.mileageLabel.text = NSLocalizedString("table_head_mileage", comment: "Mileage(meter)")
        self.mileageLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.mileageLabel.numberOfLines = 0;
        self.mileageLabel.textAlignment = .center
        self.satelliteLabel.text = NSLocalizedString("table_head_satellite", comment: "Satellite")
        self.satelliteLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.satelliteLabel.numberOfLines = 0;
        self.satelliteLabel.textAlignment = .center
        self.batteryLabel.text = NSLocalizedString("table_head_battery", comment: "Battery")
        self.batteryLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.batteryLabel.numberOfLines = 0;
        self.batteryLabel.textAlignment = .center
        self.networkSignalLabel.text = NSLocalizedString("table_head_network_signal", comment: "Network signal")
        self.networkSignalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.networkSignalLabel.numberOfLines = 0;
        self.networkSignalLabel.textAlignment = .center
        self.alarmLabel.text = NSLocalizedString("table_head_alarm", comment: "Alarm")
        self.alarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.alarmLabel.numberOfLines = 0;
        self.alarmLabel.textAlignment = .center
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
