//
//  HistoryS04Cell.swift
//  tftble
//
//  Created by jeech on 2020/4/10.
//  Copyright © 2020 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import JXMarqueeView
class HistoryS04Cell : UITableViewCell{
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
        label.lineBreakMode = NSLineBreakMode.byWordWrapping
        label.numberOfLines = 0
        return label
    }()
    
    static var contentViewWidth = 600
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.contentView.backgroundColor = UIColor.white
        self.contentView.addSubview(dateLabel)
        self.contentView.addSubview(batteryLabel)
        self.contentView.addSubview(tempLabel)
        self.contentView.addSubview(propLabel)
        self.contentView.addSubview(alarmLabel)
//        let marqueeView = JXMarqueeView()
//        marqueeView.contentView = self.alarmLabel
//        marqueeView.contentMargin = 50
//        marqueeView.marqueeType = .left
//        self.contentView.addSubview(marqueeView)
        var labelX = 0
        let height = 40
        let labelY = 0
        self.dateLabel.frame = CGRect(x: labelX, y: labelY, width: 160, height: height)
        labelX += 160
        self.batteryLabel.frame = CGRect(x: labelX, y: labelY, width: 80, height: height)
        labelX += 80
        self.tempLabel.frame = CGRect(x: labelX, y: labelY, width: 100, height: height)
        labelX += 100
        self.propLabel.frame = CGRect(x: labelX, y: labelY, width: 80, height: height)
        labelX += 80
        self.alarmLabel.frame = CGRect(x: labelX, y: labelY, width: 380, height: height)
        labelX += 180
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    static let identifier = "HistoryS04Cell"
    static func dequeueReusable(with tableView:UITableView) -> HistoryS04Cell {
        let reusableCell = tableView.dequeueReusableCell(withIdentifier: HistoryS04Cell.identifier)
        if  let cell = reusableCell as? HistoryS04Cell {
            return cell
        }else{
            return HistoryS04Cell.init(style: .default, reuseIdentifier: HistoryS04Cell.identifier)
        }
    }
}
