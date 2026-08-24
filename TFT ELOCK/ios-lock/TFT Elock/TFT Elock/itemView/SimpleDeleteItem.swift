//
//  SimpleDeleteItem.swift
//  TFT Elock
//
//  Created by china topflytech on 2025/2/27.
//  Copyright © 2025 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
class SimpleDeleteItem:  UITableViewCell {
   
    
    lazy var itemContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var deleteBtn:UIButton = {
        let btn = UIButton()
        btn.setTitle(NSLocalizedString("delete", comment: "delete"), for: .normal)
        btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        btn.layer.cornerRadius = 5;
        btn.layer.borderWidth = 1.0;
        btn.layer.borderColor = UIColor.nordicBlue.cgColor
        btn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return btn
    }()
    lazy var rootView :UIImageView = {
        let view = UIImageView()
        return view
    }()
    let oneLineHeight = 30
    static func getLayoutHeight(bleDeviceData:BleDeviceData) -> Int{
        return 30
    }
    
    func checkIsLostSignal(bleDeviceData:BleDeviceData) -> Bool{
        let now = Date()
        if bleDeviceData.lastRegDate != nil{
            let calendar = Calendar.current
            let components = calendar.dateComponents([.second], from:bleDeviceData.lastRegDate, to: now )
            if components.second ?? 0 > 300{
                return true
            }
        }
        return false
    }
     
    
    func initLayoutPosition()
    {
        let buttonWidth: CGFloat = 70
        let buttonRightInset: CGFloat = 8
        let labelLeftInset: CGFloat = 8
        let labelButtonGap: CGFloat = 12
        let rootWidth = max(0, self.bounds.width - 10)
        let buttonX = max(0, rootWidth - buttonWidth - buttonRightInset)
        let labelWidth = max(0, buttonX - labelLeftInset - labelButtonGap)

        self.rootView.frame = CGRect(x: 5, y: 5, width: rootWidth, height: CGFloat(oneLineHeight + 10))
        self.rootView.isUserInteractionEnabled=true

        self.itemContentLabel.frame = CGRect(x: labelLeftInset, y: 5, width: labelWidth, height: CGFloat(oneLineHeight - 5))
        self.deleteBtn.frame = CGRect(x: buttonX, y: 0, width: buttonWidth, height: CGFloat(oneLineHeight))
        self.rootView.backgroundColor = UIColor.white
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        self.initLayoutPosition()
    }
    
    
    func snpLayoutSubview(){
        self.selectionStyle = UITableViewCell.SelectionStyle.none
        self.contentView.addSubview(rootView)
        self.rootView.addSubview(itemContentLabel)
        self.rootView.addSubview(deleteBtn)
        
    }
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        snpLayoutSubview()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    static let identifier = "SimpleDeleteItem"
    static func dequeueReusable(with tableView:UITableView) -> SimpleDeleteItem {
        let reusableCell = tableView.dequeueReusableCell(withIdentifier: SimpleDeleteItem.identifier)
        if  let cell = reusableCell as? SimpleDeleteItem {
            return cell
        }else{
            return SimpleDeleteItem.init(style: .default, reuseIdentifier: SimpleDeleteItem.identifier)
        }
    }
}
