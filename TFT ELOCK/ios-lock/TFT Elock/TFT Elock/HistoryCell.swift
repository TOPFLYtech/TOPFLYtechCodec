//
//  HistoryCell.swift
//  TFT Elock
//
//  Created by china topflytech on 2023/5/25.
//  Copyright © 2023 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
class HistoryCell:UITableViewCell{
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
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
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
        
        self.logitudeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.logitudeLabel.numberOfLines = 0;
        self.logitudeLabel.textAlignment = .center
        self.latitudeLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.latitudeLabel.numberOfLines = 0;
        self.latitudeLabel.textAlignment = .center
        self.dateLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.dateLabel.numberOfLines = 0;
        self.dateLabel.textAlignment = .center
        self.lockStatusLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.lockStatusLabel.numberOfLines = 0;
        self.lockStatusLabel.textAlignment = .center
        self.speedLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.speedLabel.numberOfLines = 0;
        self.speedLabel.textAlignment = .center
        self.mileageLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.mileageLabel.numberOfLines = 0;
        self.mileageLabel.textAlignment = .center
        self.satelliteLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.satelliteLabel.numberOfLines = 0;
        self.satelliteLabel.textAlignment = .center
        self.batteryLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.batteryLabel.numberOfLines = 0;
        self.batteryLabel.textAlignment = .center
        self.networkSignalLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.networkSignalLabel.numberOfLines = 0;
        self.networkSignalLabel.textAlignment = .center
        self.alarmLabel.lineBreakMode = NSLineBreakMode.byWordWrapping;
        self.alarmLabel.numberOfLines = 0;
        self.alarmLabel.textAlignment = .center
         
    }
    static var contentViewWidth = 1540
    required init?(coder aDecoder: NSCoder) {
           fatalError("init(coder:) has not been implemented")
       }
       
       static let identifier = "HistoryCell"
       static func dequeueReusable(with tableView:UITableView) -> HistoryCell {
           let reusableCell = tableView.dequeueReusableCell(withIdentifier: HistoryCell.identifier)
           if  let cell = reusableCell as? HistoryCell {
               return cell
           }else{
               return HistoryCell.init(style: .default, reuseIdentifier: HistoryCell.identifier)
           }
       }
}
