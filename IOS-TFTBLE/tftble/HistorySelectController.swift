//
//  HistorySelectController.swift
//  tftble
//
//  Created by jeech on 2020/4/6.
//  Copyright © 2020 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import DateTimePicker
import ActionSheetPicker_3_0
import QMUIKit
class HistorySelectController: UIViewController, UIPickerViewDataSource, UIPickerViewDelegate {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return self.dateRangeList.count
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return 0
    }
    
    private var labelSelectDateStart:UIImageView!
    private var dateRangePickView:UIPickerView!
    private var btnSelectDateStart:UIButton!
    private var dateStart:Date!
    private var labelSelectDateEnd:UIImageView!
    private var btnSelectDateEnd:UIButton!
    private var dateEnd:Date!
    private var dateFormatter:DateFormatter!
    private var currentDateSelect:Int!
    private var datePicker:DateTimePicker!
    private var bannerImageView:UIImageView!
    private var startDateString:String!
    private var endDateString:String!
    private var dateRangeList:[String]!
    private var btnDateRangePicker:UIButton!
    private var downTriangleStr:String!
    private var submitBtn:QMUIGhostButton!
    public var setDateDelegate:HistorySelectDelegate?
 
    override func viewDidLoad() {
        super.viewDidLoad()
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = NSLocalizedString("select_datetime", comment: "Select datetime")
        self.navigationItem.titleView = titleLabel
        self.dateFormatter = DateFormatter()
        self.dateFormatter.dateFormat = "yyyy-MM-dd HH : mm"
        self.dateStart = Utils.getStartDate(selectedIndex: 0)
        self.dateEnd = Utils.getEndDate(selectedIndex: 0)
        self.currentDateSelect = 0
        self.dateRangeList = Utils.getDateRangeList()
        self.view.backgroundColor = UIColor.white
      
        let appFrame = UIScreen.main.bounds
 
        self.downTriangleStr = "▼"
        let btnDateRangeFrame = CGRect.init(x: appFrame.size.width/2 - 100, y: 80, width: 200, height: 30);
        self.btnDateRangePicker = UIButton.init(frame: btnDateRangeFrame)
        self.btnDateRangePicker.setTitle(String(format: "%@ %@", self.dateRangeList[self.currentDateSelect],self.downTriangleStr), for: .normal)
        self.btnDateRangePicker.setTitleColor(UIColor.brown, for: .normal)
        self.view.addSubview(self.btnDateRangePicker)
        self.btnDateRangePicker.addTarget(self, action: #selector(dateRangeClick), for: .touchUpInside)
        
        
        let view = UIView.init(frame: CGRect(x: 4, y: self.btnDateRangePicker.frame.origin.y+self.btnDateRangePicker.frame.size.height + 3, width: self.view.frame.size.width-8, height: 75))
        view.backgroundColor = UIColor.clear
        view.layer.cornerRadius = 5;
        view.layer.masksToBounds = true
        view.layer.borderWidth = 0.3
        view.layer.borderColor = UIColor.gray.cgColor
        view.isUserInteractionEnabled = true
        self.view.addSubview(view)
        
        self.bannerImageView = UIImageView.init(frame: CGRect(x: view.frame.origin.x + 3, y: view.frame.origin.y + 3,width: 65, height: 65))
        self.bannerImageView.contentMode = .scaleAspectFit;
        self.bannerImageView.image = UIImage (named: "ic_date.png")
        self.bannerImageView.backgroundColor = UIColor.clear
        self.view.addSubview(self.bannerImageView)
       
        
        self.labelSelectDateStart = UIImageView.init(frame: CGRect(x: view.frame.width - 35, y: 3,width: 30, height: 30))
        self.labelSelectDateStart.contentMode = .scaleAspectFit;
        self.labelSelectDateStart.image = UIImage (named: "ic_edit.png")
        self.labelSelectDateStart.backgroundColor = UIColor.clear
        view.addSubview(self.labelSelectDateStart)
        
        let btnSelectDateStartFrame = CGRect(x:65,y: 3, width: appFrame.size.width/2+65, height: 30)
        self.btnSelectDateStart = UIButton.init(frame: btnSelectDateStartFrame)
        self.btnSelectDateStart.setTitle(self.dateFormatter.string(from: self.dateStart), for: .normal)
        self.btnSelectDateStart.setTitleColor(UIColor.brown, for: .normal)
        self.btnSelectDateStart.addTarget(self, action: #selector(self.btnSelectedDateStartClick(sender:)), for:.touchUpInside)
        view.addSubview(self.btnSelectDateStart)
        
        
        self.labelSelectDateEnd = UIImageView.init(frame: CGRect(x:view.frame.width - 35, y: labelSelectDateStart.frame.height + 3,width: 30, height: 30))
        self.labelSelectDateEnd.contentMode = .scaleAspectFit;
        self.labelSelectDateEnd.image = UIImage (named: "ic_edit.png")
        self.labelSelectDateEnd.backgroundColor = UIColor.clear
        view.addSubview(self.labelSelectDateEnd)
        
        let btnSelectDateEndFrame = CGRect(x: 65,y: btnSelectDateStart.frame.origin.y + btnSelectDateStart.frame.height + 3, width: appFrame.size.width/2+65, height: 30)
        self.btnSelectDateEnd = UIButton.init(frame: btnSelectDateEndFrame)
        self.btnSelectDateEnd.setTitle(self.dateFormatter.string(from: self.dateEnd), for: .normal)
        self.btnSelectDateEnd.setTitleColor(UIColor.brown, for: .normal)
        self.btnSelectDateEnd.addTarget(self, action: #selector(self.btnSelectedDateEndClick(sender:)), for:.touchUpInside)
        view.addSubview(self.btnSelectDateEnd)
        
        self.submitBtn = QMUIGhostButton()
        self.submitBtn.setTitle(NSLocalizedString("submit", comment: "Submit"), for: .normal)
        self.submitBtn.ghostColor = UIColor.colorPrimary 
        self.submitBtn.frame = CGRect(x: Int((KSize.width - 200) / 2), y: Int(view.frame.origin.y) + Int(view.frame.height) + 10, width: 200, height: 30)
        self.submitBtn.addTarget(self, action: #selector(submit), for:.touchUpInside)
        self.view.addSubview(self.submitBtn)
        
        self.datePicker = DateTimePicker()
//        let min = Date().addingTimeInterval(-60 * 60 * 24 * 4)
//        let max = Date().addingTimeInterval(60 * 60 * 24 * 4)
//        self.datePicker = DateTimePicker.create(minimumDate: min, maximumDate: max)
//        self.datePicker.frame = CGRect(x: 0, y: 100, width: self.datePicker.frame.size.width, height: self.datePicker.frame.size.height)
//        self.view.addSubview(self.datePicker)
    }
    
    @objc func btnSelectedDateStartClick(sender:UIButton){
        let min = Date().addingTimeInterval(-60 * 60 * 24 * 64)
         let max = Date().addingTimeInterval(60 * 60 * 24 * 4)
         let picker = DateTimePicker.create(minimumDate: min, maximumDate: max)
         picker.frame = CGRect(x: 0, y: 100, width: picker.frame.size.width, height: picker.frame.size.height)
        picker.selectedDate = self.dateStart
        picker.completionHandler = {
               date in
            self.dateStart = date
             self.btnSelectDateStart.setTitle(self.dateFormatter.string(from: self.dateStart), for: .normal)
           }
        picker.show()
    }
    @objc func btnSelectedDateEndClick(sender:UIButton){
        let min = Date().addingTimeInterval(-60 * 60 * 24 * 64)
         let max = Date().addingTimeInterval(60 * 60 * 24 * 4)
         let picker = DateTimePicker.create(minimumDate: min, maximumDate: max)
         picker.frame = CGRect(x: 0, y: 100, width: picker.frame.size.width, height: picker.frame.size.height)
        picker.selectedDate = self.dateEnd
        picker.completionHandler = {
               date in
            self.dateEnd = date
             self.btnSelectDateEnd.setTitle(self.dateFormatter.string(from: self.dateEnd), for: .normal)
           }
        picker.show()
    }
    
    
    @objc func submit(){
        self.setDateDelegate?.setSelectDate(startDate: self.dateStart,endDate: self.dateEnd)
         self.navigationController?.popViewController(animated: false)
    }
    
    @objc func dateRangeClick(sender:UIButton){
         ActionSheetStringPicker.show(withTitle: "", rows: self.dateRangeList, initialSelection:self.currentDateSelect, doneBlock: {
                        picker, index, value in
                        self.currentDateSelect = index
//                        print("values = \(values)")
//                        print("indexes = \(indexes)")
                        self.initDateRangeValue()
                        return
                }, cancel: { ActionMultipleStringCancelBlock in return }, origin: sender)
    }
    
    func initDateRangeValue(){
        self.dateEnd = Utils.getEndDate(selectedIndex: self.currentDateSelect)
        self.dateStart = Utils.getStartDate(selectedIndex: self.currentDateSelect)
        self.btnSelectDateStart.setTitle(self.dateFormatter.string(from: self.dateStart), for: .normal)
        self.btnSelectDateEnd.setTitle(self.dateFormatter.string(from: self.dateEnd), for: .normal)
        self.btnDateRangePicker.setTitle(String(format: "%@ %@", self.dateRangeList[self.currentDateSelect],self.downTriangleStr), for: .normal)
    }
}
