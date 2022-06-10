//
//  HistoryS02Header.swift
//  tftble
//
//  Created by jeech on 2020/4/10.
//  Copyright © 2020 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
class HistoryS02Header:UITableViewHeaderFooterView{
    lazy var dateLabel: UILabel = {
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
    lazy var batteryLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    lazy var humidityLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: 14)
        return label
    }()
    lazy var propLabel: UILabel = {
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
        self.contentView.addSubview(dateLabel)
        self.contentView.addSubview(batteryLabel)
        self.contentView.addSubview(tempLabel)
        self.contentView.addSubview(humidityLabel)
        self.contentView.addSubview(propLabel)
        var labelX = 0
        let height = 40
        let labelY = 0
        self.dateLabel.frame = CGRect(x: labelX, y: labelY, width: 160, height: height)
        labelX += 160
        self.batteryLabel.frame = CGRect(x: labelX, y: labelY, width:80, height: height)
        labelX += 80
        self.tempLabel.frame = CGRect(x: labelX, y: labelY, width: 100, height: height)
        labelX += 100
        self.humidityLabel.frame = CGRect(x: labelX, y: labelY, width: 80, height: height)
        labelX += 80
        self.propLabel.frame = CGRect(x: labelX, y: labelY, width: 80, height: height)
        labelX += 80
        
        self.dateLabel.text = NSLocalizedString("date1", comment: "Date")
        self.batteryLabel.text = NSLocalizedString("battery1", comment: "Battery")
        self.tempLabel.text = NSLocalizedString("temp1", comment: "Temperature")
        self.humidityLabel.text = NSLocalizedString("humidity1", comment: "Humidity")
        self.propLabel.text = NSLocalizedString("light1", comment: "Light")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
