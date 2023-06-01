//
//  HIstoryReportController.swift
//  tftble
//
//  Created by jeech on 2020/4/8.
//  Copyright © 2020 com.tftiot. All rights reserved.
//

import Foundation
import UIKit
import ActionSheetPicker_3_0
import MessageUI
import CLXToast
import SwiftPopMenu
import PDFGenerator
import QMUIKit
import Charts
class HistoryReportController:UIViewController, UITableViewDataSource,UITableViewDelegate,MFMailComposeViewControllerDelegate{
   
    
    
    private var fontSize:CGFloat = Utils.fontSize
    public var deviceType="S04"
    public var deviceName="TH121"
    public var mac="221322112312"
    public var reportType:String! = "S04"
    public var id:String! = "221322112312"
    public var deviceModel:String!
    public var showBleHisData = [BleHisData]()
    public var startDate,endDate:Int!
    private var propOpenCount,propCloseCount,startBattery,endBattery,maxHumidity,minHumidity:Int!
    public var tempAlarmUp:Double=4095,tempAlarmDown:Double=4095
    public var humidityAlarmUp=4095,humidityAlarmDown=4095
    private var beginTemp,endTemp:String!
    private var beginHumidity,endHumidity,averageTemp,averageHumidity,maxTemp,minTemp,tempSum,humiditySum:Float!
    private var overMaxTempLimitCount,overMaxTempLimitTime,overMinTempLimitCount,overMinTempLimitTime,
    overMaxHumidityLimitCount,overMaxHumidityLimitTime,overMinHumidityLimitCount,overMinHumidityLimitTime:Int!
    private var mainView:UIScrollView!
    private var sumView:UIView!
    private var secondView:UIView!
    
    private var txDeviceName,txBleId,txReportCreateTime,txReportBeginTime,txReportEndTime,txBatteryBegin,txBatteryEnd,
    txReportProp,txOpenCount,txCloseCount,txS02TempHead,txS02HumidityHead,txS02TempStart,txS02HumidityStart,txS02TempEnd,txS02HumidityEnd,
    txS02TempMaxLimit,txS02HumidityMaxLimit,txS02TempMinLimit,txS02HumidityMinLimit,txS02TempAverage,txS02HumidityAverage,
    txS02TempMax,txS02HumidityMax,txS02TempMin,txS02HumidityMin,txS02TempOverHighCount,txS02TempOverHighTime,txS02HumidityOverHighCount,
    txS02HumidityOverHighTime,txS02TempOverLowCount,txS02TempOverLowTime,txS02HumidityOverLowCount,txS02HumidityOverLowTime,txS04TempHead,txS04TempStart,txS04TempEnd,
    txS04TempMaxLimit,txS04TempMinLimit,txS04TempAverage,txDeviceModel,
    txS04TempMax,txS04TempMin,txS04TempOverHighCount,txS04TempOverHighTime,
    txS04TempOverLowCount,txS04TempOverLowTime:UILabel!
    
    private var txDescDeviceName,txDescBleId,txDescDeviceModel,txDescReportCreateTime,txDescReportTime,txDescBattery,txDescReportProp,txDescOpenCount,txDescCloseCount,txDescTempHead,txDescHumidityHead,txDescStartValue,txDescEndValue,txDescMaxLimit,txDescMinLimit,txDescAverage,txDescMax,txDescMin,txDescOverHighCount,txDescOverHighTime,txDescOverLowCount,txDescOverLowTime:UILabel!
    
    private var dateFormatter:DateFormatter!
    var tempChartView: LineChartView!
    var humidityChartView: LineChartView!
    private var detailTableView:UITableView!
    private var showDetailList = [BleHisData]()
    private var btnPre:QMUIGhostButton!
    private var btnNext:QMUIGhostButton!
    private var currentPage = 1
    private var showItemCount = 10
    private var totalPage = 1
    private var downTriangleStr:String!
    private var btnPagePicker:UIButton!
    private var canSendEmail = false
    private var pageRangeList = [String]()
    private var waitingView:AEUIAlertView!
    private var waitingNoAnimationView:AEUIAlertView!
    var popMenu:SwiftPopMenu!
    
    override func viewWillDisappear(_ animated:Bool) {
            super.viewWillDisappear(animated)
            navigationController?.interactivePopGestureRecognizer?.isEnabled = true;
        }

        

        override func viewDidAppear(_ animated:Bool) {
            navigationController?.interactivePopGestureRecognizer?.isEnabled = false;
        }
    
    override func viewDidLoad() {
        super.viewDidLoad() 
        self.view.backgroundColor = UIColor.white
        let titleLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 40))
        titleLabel.text = NSLocalizedString("report", comment: "Report")
        self.navigationItem.titleView = titleLabel
        self.initRightBtn()
//                self.readFile()
        self.initData()
        self.initUI()
        self.makeTable()
        self.totalPage = self.showBleHisData.count / self.showItemCount
        if self.showBleHisData.count % self.showItemCount != 0{
            self.totalPage += 1
        }
        for i in 0..<self.totalPage{
            self.pageRangeList.append(String(i+1))
        }
        initTableData()
        self.mainView.contentSize = CGSize(width: KSize.width, height: self.btnNext.frame.height + self.btnNext.frame.origin.y + 10)
        self.checkCanSendEmail()
    }
    
    
    func showWaitingNoAnimationWin(){
        if self.waitingNoAnimationView != nil && !self.waitingNoAnimationView.isDismiss{
            self.waitingView.show()
            return
        }
        self.waitingNoAnimationView = AEUIAlertView(style: .defaulted, title: "", message: nil)
        self.waitingNoAnimationView.actions = []
        self.waitingNoAnimationView.message = NSLocalizedString("waiting", comment: "Waiting")
        self.waitingNoAnimationView.resetActions()
//        self.waitingNoAnimationView.textField.isHidden = true
        self.waitingNoAnimationView.show()
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(30)) {
            if !self.waitingNoAnimationView.isDismiss{
                Toast.hudBuilder.title(NSLocalizedString("timeout_warning", comment: "Timeout, please try again!")).show()
            }
            self.waitingNoAnimationView.dismiss()
        }
    }
    func showWaitingWin(title:String){
        if self.waitingView != nil && !self.waitingView.isDismiss{
            self.waitingView.show()
            return
        }
        self.waitingView = AEUIAlertView(style: .textField, title: title, message: nil)
        self.waitingView.actions = []
        self.waitingView.resetActions()
        
        let animation = UIView(frame: CGRect(x: 0, y: 0, width: 80, height: 80))
        let anim = AEBeginLineAnimation.initShow(in: animation.bounds, lineWidth: 4, lineColor: UIColor.blue)
        animation.addSubview(anim)
        
        self.waitingView.textField.isHidden = true
        self.waitingView.set(animation: animation, width: 80, height: 80)
        self.waitingView.show()
        DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(30)) {
            if !self.waitingView.isDismiss{
                Toast.hudBuilder.title(NSLocalizedString("timeout_warning", comment: "Timeout, please try again!")).show()
            }
            self.waitingView.dismiss()
        }
    }
    
    func initRightBtn(){
        let btn = UIButton.init(frame: CGRect.init(x: KSize.width - 50, y: 0, width: 25, height: 25))
        btn.setImage(UIImage(named:"ic_category.png"), for: .normal)
        let rightBarButtonItem =  UIBarButtonItem.init(customView: btn)
        btn.addTarget(self, action: #selector(self.rightClick), for: .touchUpInside)
        self.navigationItem.rightBarButtonItem = rightBarButtonItem
        
        
        //数据源（icon可不填）
        let popData = [(icon:"",title:NSLocalizedString("send_pdf", comment: "Send pdf")),
                       (icon:"",title:NSLocalizedString("send_excel", comment: "Send excel")),
                       (icon:"",title:NSLocalizedString("send_csv", comment: "Send csv"))]
        
        //设置Parameter（可不写）
        let parameters:[SwiftPopMenuConfigure] = [
            .PopMenuTextColor(UIColor.black),
            .popMenuItemHeight(44),
            .PopMenuTextFont(UIFont.systemFont(ofSize: 18))
        ]
        
        //init  (注意：arrow点是基于屏幕的位置)
        popMenu = SwiftPopMenu(menuWidth: 150, arrow: CGPoint(x: KSize.width - 10, y: 70), datas: popData,configures: parameters)
        
        //click
        popMenu.didSelectMenuBlock = { [weak self](index:Int)->Void in
            print ("block sßelect \(index)")
            self?.popMenu.dismiss()
            if index == 0{
                self?.sendPdf()
            }else if index == 1{
                self?.sendExcel()
            }else if index == 2{
                self?.sendCsv()
            }
        }
    }
    
    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        if result == MFMailComposeResult.sent {
            //发送成功
            Toast.hudBuilder.title(NSLocalizedString("send_success", comment: "Send success!")).show()
        }
        else if result == MFMailComposeResult.cancelled {
            //取消发送
        }
        else if result == MFMailComposeResult.failed {
            //发送失败
            Toast.hudBuilder.title(NSLocalizedString("send_fail", comment: "Send fail!")).show()
        }
        else {
            //已保存
        }
        dismiss(animated: true, completion: nil)
    }
    
    func testSendMail(){
        let mail = MFMailComposeViewController()
        mail.navigationBar.tintColor = UIColor.blue //导航颜色
        mail.setToRecipients(["1024919409@qq.com"]) //设置收件地址
        mail.mailComposeDelegate = self //代理
        
        mail.setSubject("test tftble ")
        //发送文字
        mail.setMessageBody("test tftble", isHTML: false) //邮件主体内容
        //发送图片
        let path = Bundle.main.path(forResource: "log", ofType: "txt")
        let url = URL(fileURLWithPath: path!)
        do {
            
            let data = try Data(contentsOf: url)
            mail.addAttachmentData(data as Data, mimeType: "", fileName: "log.txt")
            
            present(mail, animated: true, completion: nil)
        } catch let error as Error! {
            print("读取本地数据出现错误!",error)
        }
    }
    
    func getSaveName(fileType:String) ->String{
        let timeExt = String(format:"%@-%@",txReportBeginTime.text as! CVarArg,txReportEndTime.text as! CVarArg)
        if fileType == "pdf"{
            return id + "-" + timeExt + ".pdf";
        }else if fileType == "temp" {
            return id + "-temp-" + timeExt + ".png";
        }else if fileType == "humidity"{
            return id + "-humidity-" + timeExt + ".png";
        }else if fileType == "xlsx"{
            return id + "-" + timeExt + ".xlsx";
        }else if fileType == "csv"{
            return id + "-" + timeExt + ".csv";
        }
        return "";
    }
    
    
    
    
    func sendPdf(){
        print ("send pdf")
        self.checkCanSendEmail()
        if !canSendEmail{
            return
        }
        self.showWaitingNoAnimationWin()
        
        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.5) {
            var tempShowList = [BleHisData]()
            for var i in 0..<self.showDetailList.count{
                tempShowList.append(self.showDetailList[i])
            }
            self.showDetailList.removeAll()
            for var i in 0..<self.showBleHisData.count{
                self.showDetailList.append(self.showBleHisData[i])
            }
            self.detailTableView.reloadData()
            let page1 = PDFPage.view(self.sumView)
            let page2 = PDFPage.view(self.secondView)
            let page3 = PDFPage.image(self.tempChartView.getChartImage(transparent: true)!)
            
            
            self.detailTableView.reloadData()
            let page5 = PDFPage.view(self.detailTableView)
            var pages = [page1, page2, page3]
            if self.deviceType == "S02" || self.deviceType == "S10"{
                let page4 = PDFPage.image(self.humidityChartView.getChartImage(transparent: true)!)
                pages = [page1, page2, page3, page4]
            }
            pages.append(page5)
            let path = NSHomeDirectory() + "/Documents/sample1.pdf"
            let dst = URL(fileURLWithPath: path)
            do {
                try PDFGenerator.generate(pages, to: dst)
            } catch (let e) {
                print(e)
            }
            self.sendMail(path: path, fileType: "pdf")
            self.showDetailList.removeAll()
            for var i in 0..<tempShowList.count{
                self.showDetailList.append(tempShowList[i])
            }
            self.detailTableView.reloadData()
                        // update UI
            self.waitingNoAnimationView.dismiss()
        }
        
    }
    
    func sendMail(path:String,fileType:String){ 
        let mail = MFMailComposeViewController()
        mail.navigationBar.tintColor = UIColor.blue //导航颜色
        //        mail.setToRecipients(["1024919409@qq.com"]) //设置收件地址
        mail.mailComposeDelegate = self //代理
        let filename = self.getSaveName(fileType: fileType)
        mail.setSubject(filename)
        mail.setMessageBody(filename, isHTML: false) //邮件主体内容
        let url = URL(fileURLWithPath: path)
        do {
            
            let data = try Data(contentsOf: url)
            mail.addAttachmentData(data as Data, mimeType: "", fileName: filename)
            
            self.present(mail, animated: true, completion: nil)
        } catch let error as Error! {
            print("读取本地数据出现错误!",error)
        }
    }
    
    func sendExcel(){
        print ("send excel")
        self.checkCanSendEmail()
        if !canSendEmail{
            return
        }
        
        let path = NSHomeDirectory() + "/Documents/sample1.xlsx"
        let book = workbook_new(path)
        let sheet = workbook_add_worksheet(book, "sheet1")
        var rowIndex = 0
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescDeviceName.text, nil)
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txDeviceName.text, nil)
        rowIndex+=1
        
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescBleId.text, nil)
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txBleId.text, nil)
        rowIndex+=1
        
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescDeviceModel.text, nil)
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txDeviceModel.text, nil)
        rowIndex+=1
        
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescReportCreateTime.text, nil)
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txReportCreateTime.text, nil)
        rowIndex+=1
        
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescReportTime.text, nil)
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txReportBeginTime.text, nil)
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txReportEndTime.text, nil)
        rowIndex+=1
        
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescBattery.text, nil)
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txBatteryBegin.text, nil)
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txBatteryEnd.text, nil)
        rowIndex+=1
        
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescReportProp.text, nil)
        let propOpen = self.deviceType == "S02" || self.deviceType == "S10" ? NSLocalizedString("light", comment:"Light:") : NSLocalizedString("open_desc", comment: "Open:")
        let propClose = self.deviceType == "S02" || self.deviceType == "S10" ? NSLocalizedString("dark_desc", comment: "Dark:" ): NSLocalizedString("close_desc", comment: "Close:")
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, String(format:"%@%@",propOpen,txOpenCount.text as! CVarArg), nil)
        worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, String(format:"%@%@",propClose, txCloseCount.text as! CVarArg), nil)
        rowIndex+=1
        rowIndex+=1
        
        if self.deviceType == "S02" || self.deviceType == "S10"{
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, "", nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempHead.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityHead.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescStartValue.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempStart.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityStart.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescEndValue.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempEnd.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityEnd.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescMaxLimit.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempMaxLimit.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityMaxLimit.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescMinLimit.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempMinLimit.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityMinLimit.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescAverage.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempAverage.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityAverage.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescMax.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempMax.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityMax.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescMin.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempMin.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityMin.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescOverHighCount.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempOverHighCount.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityOverHighCount.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescOverHighTime.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempOverHighTime.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityOverHighTime.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescOverLowCount.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempOverLowCount.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityOverLowCount.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescOverLowTime.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS02TempOverLowTime.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 2, txS02HumidityOverLowTime.text, nil)
            rowIndex+=1
        }else{
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, "", nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempHead.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescStartValue.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempStart.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescEndValue.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempEnd.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescMaxLimit.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempMaxLimit.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescMinLimit.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempMinLimit.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescAverage.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempAverage.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescMax.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempMax.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescMin.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempMin.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescOverHighCount.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempOverHighCount.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescOverHighTime.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempOverHighTime.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescOverLowCount.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempOverLowCount.text, nil)
            rowIndex+=1
            
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 0, txDescOverLowTime.text, nil)
            worksheet_write_string(sheet, lxw_row_t(rowIndex), 1, txS04TempOverLowTime.text, nil)
            rowIndex+=1
        }
        
        var detailRowIndex = 55
        if self.deviceType != "S02" || self.deviceType == "S10"{
            detailRowIndex = 38
        }
        worksheet_write_string(sheet, lxw_row_t(detailRowIndex), 0, NSLocalizedString("date1", comment: "Date"), nil)
        worksheet_write_string(sheet, lxw_row_t(detailRowIndex), 1, NSLocalizedString("battery1", comment: "Battery"), nil)
        worksheet_write_string(sheet, lxw_row_t(detailRowIndex), 2, NSLocalizedString("temp1", comment: "Temperature"), nil)
        if deviceType == "S02" || self.deviceType == "S10"{
            worksheet_write_string(sheet, lxw_row_t(detailRowIndex), 3, NSLocalizedString("humidity1", comment: "Humidity"), nil)
            worksheet_write_string(sheet, lxw_row_t(detailRowIndex), 4, NSLocalizedString("light1", comment: "Light"), nil)
        }else{
            worksheet_write_string(sheet, lxw_row_t(detailRowIndex), 3, NSLocalizedString("door1", comment: "Door"), nil)
            worksheet_write_string(sheet, lxw_row_t(detailRowIndex), 4, NSLocalizedString("alarm1", comment: "Alarm"), nil)
        }
        detailRowIndex += 1
        
        
        for var index in 0..<showBleHisData.count - 1{
            var bleHisItem = showBleHisData[index]
            worksheet_write_string(sheet, lxw_row_t(detailRowIndex+index), 0, self.dateFormatter.string(from: Date(timeIntervalSince1970: Double(bleHisItem.dateStamp))), nil)
            worksheet_write_string(sheet, lxw_row_t(detailRowIndex+index), 1, String(format:"%d%%",bleHisItem.battery), nil)
            worksheet_write_number(sheet, lxw_row_t(detailRowIndex+index), 2, Double(Utils.getCurTemp(sourceTemp: bleHisItem.temp)), nil)
            
            if deviceType == "S02" || self.deviceType == "S10"{
                worksheet_write_number(sheet, lxw_row_t(detailRowIndex+index), 3, Double(bleHisItem.humidity), nil)
                let prop = bleHisItem.prop == 1 ? NSLocalizedString("light1", comment: "Light") : NSLocalizedString("dark", comment: "Dark")
                worksheet_write_string(sheet, lxw_row_t(detailRowIndex+index), 4, prop, nil)
            }else{
                let prop = bleHisItem.prop == 1 ? NSLocalizedString("open", comment: "Open") : NSLocalizedString("close", comment: "Close")
                worksheet_write_string(sheet, lxw_row_t(detailRowIndex+index), 3, prop, nil)
                worksheet_write_string(sheet, lxw_row_t(detailRowIndex), 4, Utils.getS04WarnDesc(warn: Int(bleHisItem.alarm)), nil)
            }
        }
        
        
        let tempChart = workbook_add_chart(book, UInt8(LXW_CHART_LINE.rawValue))
        let tempSeries = chart_add_series(tempChart, nil, "Sheet1!$A$1:$A$5");
        chart_series_set_name(tempSeries, NSLocalizedString("temp1", comment: "Temperature"))
        chart_series_set_categories(tempSeries, "Sheet1", lxw_row_t(detailRowIndex), 0, lxw_row_t(detailRowIndex+showBleHisData.count), 0); // "=Sheet1!$A$2:$A$7"
        chart_series_set_values(tempSeries, "Sheet1", lxw_row_t(detailRowIndex), 2, lxw_row_t(detailRowIndex+showBleHisData.count - 2), 2); // "=Sheet1!$C$2:$C$7"
        
        // Insert the chart into the worksheet
        worksheet_insert_chart(sheet, 20,0, tempChart);
        
        if self.deviceType == "S02" || self.deviceType == "S10"{
            let humidityChart = workbook_add_chart(book, UInt8(LXW_CHART_LINE.rawValue))
            let humiditySeries = chart_add_series(humidityChart, nil, "Sheet1!$A$1:$A$5");
            chart_series_set_name(humiditySeries, NSLocalizedString("humidity1", comment: "Humidity"))
            chart_series_set_categories(humiditySeries, "Sheet1", lxw_row_t(detailRowIndex), 0, lxw_row_t(detailRowIndex+showBleHisData.count), 0); // "=Sheet1!$A$2:$A$7"
            chart_series_set_values(humiditySeries, "Sheet1", lxw_row_t(detailRowIndex), 3, lxw_row_t(detailRowIndex+showBleHisData.count - 2), 3); // "=Sheet1!$C$2:$C$7"
            
            // Insert the chart into the worksheet
            worksheet_insert_chart(sheet, 38,0, humidityChart);
        }
        workbook_close(book);
        
        self.sendMail(path: path, fileType: "xlsx")
    }
    func sendCsv(){
        print ("send csv")
        self.checkCanSendEmail()
        if !canSendEmail{
            return
        }
        let path = NSHomeDirectory() + "/Documents/sample1.csv"
        let fileManager = FileManager.default
        fileManager.createFile(atPath: path, contents:nil, attributes:nil)
        let handle = FileHandle(forWritingAtPath:path)
        var StrItem = "\(NSLocalizedString("date1", comment: "Date")),\(NSLocalizedString("battery1", comment: "Battery")),\(NSLocalizedString("temp1", comment: "Temperature")),\(NSLocalizedString("humidity1", comment: "Humidity")),\(NSLocalizedString("light1", comment: "Light"))\n"
        if self.deviceType == "S04" || self.deviceType == "S08"{
            StrItem = "\(NSLocalizedString("date1", comment: "Date")),\(NSLocalizedString("battery1", comment: "Battery")),\(NSLocalizedString("temp1", comment: "Temperature")),\(NSLocalizedString("door1", comment: "Door")),\(NSLocalizedString("alarm1", comment: "Alarm"))\n"
        }
        handle?.write(StrItem.data(using: String.Encoding.utf8)!)
        for var i in 0..<self.showBleHisData.count{
            let bleHisItem = self.showBleHisData[i]
            if self.deviceType == "S02" || self.deviceType == "S10"{
                StrItem = self.dateFormatter.string(from: Date(timeIntervalSince1970: Double(bleHisItem.dateStamp))) + "," + String(format:"%d%%",bleHisItem.battery)
                    + "," + String(format:"%.2f",Utils.getCurTemp(sourceTemp: bleHisItem.temp)) + ","
                    + String(format:"%d",bleHisItem.humidity) + "," + (bleHisItem.prop == 1 ? NSLocalizedString("light1", comment: "Light") : NSLocalizedString("dark", comment: "Dark"))
                    + "\n"
            }else{
                StrItem = self.dateFormatter.string(from: Date(timeIntervalSince1970: Double(bleHisItem.dateStamp))) + "," + String(format:"%d%%",bleHisItem.battery) + "," +
                    String(format:"%.2f",Utils.getCurTemp(sourceTemp: bleHisItem.temp)) + "," + (bleHisItem.prop == 1 ? NSLocalizedString("open", comment: "Open") : NSLocalizedString("close", comment: "Close")) + "," + Utils.getS04WarnDesc(warn: Int(bleHisItem.alarm)) + "\n"
            }
            handle?.write(StrItem.data(using: String.Encoding.utf8)!)
        }
        self.sendMail(path: path, fileType: "csv")
        
    }
    
    @objc private func rightClick() {
        print ("refresh click")
        popMenu.show()
    }
    
    
    
    func checkCanSendEmail(){
        if !MFMailComposeViewController.canSendMail() {
            //不支持发送邮件
            Toast.hudBuilder.title(NSLocalizedString("can_not_send_email_need_set_in_system", comment: "Unable to send report email, please configure mailbox in IOS system")).show()
            self.canSendEmail = false
        }else{
            self.canSendEmail = true
        }
    }
    
    func initTableData(){
        self.btnPagePicker.setTitle(String(format: "%d %@", self.currentPage,self.downTriangleStr), for: .normal)
        self.showDetailList.removeAll()
        let startIndex = (self.currentPage - 1) * self.showItemCount
        var endIndex = self.currentPage * self.showItemCount
        if endIndex > self.showBleHisData.count{
            endIndex = self.showBleHisData.count
        }
        for var i in startIndex..<endIndex{
            showDetailList.append(self.showBleHisData[i])
        }
        detailTableView.reloadData()
    }
    
    func makeTable()
    {
        var tableScrollViewY = tempChartView.frame.origin.y + tempChartView.frame.height + 5
        if deviceType == "S02" || self.deviceType == "S10"{
            tableScrollViewY = humidityChartView.frame.origin.y + humidityChartView.frame.height + 5
        }
        var tableScrollView = UIScrollView()
        let tableHight = 40 * self.showItemCount + 40
        tableScrollView.frame = CGRect(x: 30, y: Int(tableScrollViewY), width: Int(KSize.width), height: tableHight+20)
        var detailTableViewWidth = HistoryS02Cell.contentViewWidth
        if self.deviceType == "S04" || self.deviceType == "S08"{
            detailTableViewWidth = HistoryS04Cell.contentViewWidth
        }
        tableScrollView.contentSize = CGSize(width: detailTableViewWidth + 20, height: tableHight)
        self.mainView.addSubview(tableScrollView)
        
        detailTableView = UITableView.init(frame: CGRect(x: 0, y: 0, width: detailTableViewWidth, height: tableHight), style:.plain)
        if #available(iOS 15.0, *){
            detailTableView.sectionHeaderTopPadding = 0
        }
        detailTableView.backgroundColor = UIColor.white
        detailTableView.delegate = self
        detailTableView.dataSource = self
        if deviceType == "S02" || self.deviceType == "S10"{
            let headView = HistoryS02Header()
            headView.frame = CGRect(x: 0, y: 0, width: KSize.width, height: 40)
            detailTableView.tableHeaderView = headView
        }else{
            let headView = HistoryS04Header()
            headView.frame = CGRect(x: 0, y: 0, width: KSize.width, height: 40)
            detailTableView.tableHeaderView = headView
        }
        detailTableView.tableFooterView?.isHidden = true;
        detailTableView.tableFooterView = UIView(frame: CGRect(x: 0, y: 0, width: KSize.width, height: 0.01))
        detailTableView.estimatedSectionHeaderHeight = 0;
        detailTableView.estimatedSectionFooterHeight = 0;
        detailTableView.estimatedRowHeight = 0;
        tableScrollView.addSubview(detailTableView)
        detailTableView.autoresizingMask = UIView.AutoresizingMask.flexibleWidth
        detailTableView.register(HistoryS02Cell.self,forCellReuseIdentifier:HistoryS02Cell.identifier)
        detailTableView.register(HistoryS04Cell.self,forCellReuseIdentifier:HistoryS04Cell.identifier)
        self.btnPre = QMUIGhostButton(frame: CGRect(x:5 , y: tableScrollView.frame.origin.y + tableScrollView.frame.height + 5, width: 80, height: 40))
        self.btnPre.setTitle(NSLocalizedString("previous", comment: "Previous"), for: .normal)
        self.btnPre.ghostColor = UIColor.colorPrimary
        self.btnPre.addTarget(self, action: #selector(btnPreClick), for:.touchUpInside)
        self.mainView.addSubview(self.btnPre)
        self.btnNext = QMUIGhostButton(frame: CGRect(x:KSize.width - 90 , y: tableScrollView.frame.origin.y + tableScrollView.frame.height + 5, width: 80, height: 40))
        self.btnNext.setTitle(NSLocalizedString("next", comment:"Next"), for: .normal)
        self.btnNext.ghostColor = UIColor.colorPrimary
        self.btnNext.addTarget(self, action: #selector(btnNextClick), for:.touchUpInside)
        self.mainView.addSubview(self.btnNext)
        
        self.downTriangleStr = "▼"
        let btnPageFrame = CGRect.init(x: KSize.width/2 - 50, y: tableScrollView.frame.origin.y + tableScrollView.frame.height + 5, width: 100, height: 30);
        self.btnPagePicker = UIButton.init(frame: btnPageFrame)
        self.btnPagePicker.setTitle(String(format: "%d %@", self.currentPage,self.downTriangleStr), for: .normal)
        self.btnPagePicker.setTitleColor(UIColor.brown, for: .normal)
        self.mainView.addSubview(self.btnPagePicker)
        self.btnPagePicker.addTarget(self, action: #selector(pageRangeClick), for: .touchUpInside)
    }
    
    @objc func pageRangeClick(sender:UIButton){
        ActionSheetStringPicker.show(withTitle: "", rows: self.pageRangeList, initialSelection:self.currentPage - 1, doneBlock: {
            picker, index, value in
            self.currentPage = index + 1
            //                        print("values = \(values)")
            //                        print("indexes = \(indexes)")
            self.initTableData()
            return
        }, cancel: { ActionMultipleStringCancelBlock in return }, origin: sender)
    }
    @objc func btnPreClick(sender:UIButton){
        if self.currentPage > 1{
            self.currentPage = self.currentPage - 1
            self.initTableData()
        }
    }
    @objc func btnNextClick(sender:UIButton){
        if self.currentPage < self.totalPage{
            self.currentPage = self.currentPage + 1
            self.initTableData()
        }
    }
    //MARK:table代理
    
    //段数
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    //行数
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.showDetailList.count
    }
    
    
    func tableView(_ tableView: UITableView, estimatedHeightForRowAt indexPath: IndexPath) -> CGFloat {
        return 40
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 40
    }
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 0.01
    }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        return 0.01
    }
    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return false
    }
    
    func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
        
        return indexPath
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let bleHisItem = self.showDetailList[indexPath.row]
        
        if deviceType == "S02" || self.deviceType == "S10"{
            let cell = (tableView.dequeueReusableCell(withIdentifier: HistoryS02Cell.identifier, for: indexPath)) as! HistoryS02Cell
            cell.dateLabel.text = self.dateFormatter.string(from: Date(timeIntervalSince1970: Double(bleHisItem.dateStamp)))
            cell.batteryLabel.text = String(format:"%d%%",bleHisItem.battery)
            cell.tempLabel.text = String(format:"%.2f",Utils.getCurTemp(sourceTemp: bleHisItem.temp))
            cell.humidityLabel.text = String(format:"%d",bleHisItem.humidity)
            cell.propLabel.text = bleHisItem.prop == 1 ? NSLocalizedString("light1", comment: "Light") : NSLocalizedString("dark", comment: "Dark")
            return cell
        }else{
            let cell = (tableView.dequeueReusableCell(withIdentifier: HistoryS04Cell.identifier, for: indexPath)) as! HistoryS04Cell
            cell.dateLabel.text = self.dateFormatter.string(from: Date(timeIntervalSince1970: Double(bleHisItem.dateStamp)))
            cell.batteryLabel.text = String(format:"%d%%",bleHisItem.battery)
            cell.tempLabel.text = String(format:"%.2f",Utils.getCurTemp(sourceTemp: bleHisItem.temp))
            cell.alarmLabel.text = Utils.getS04WarnDesc(warn: Int(bleHisItem.alarm ?? 0))
            cell.propLabel.text = bleHisItem.prop == 1 ? NSLocalizedString("open", comment: "Open") : NSLocalizedString("close", comment: "Close")
            return cell
        }
        
    }
    
    private var originHistroyList = [[UInt8]]();
    
    func readFile(){
        let path = Bundle.main.path(forResource: "flash", ofType: "bin")
        let url = URL(fileURLWithPath: path!)
        do {
            
            let data = try Data(contentsOf: url)
            var bytes = [UInt8](data)
            var i = 0;
            var j = 0;
            var originHistoryItem = [UInt8]()
            while i < bytes.count{
                let curByte = bytes[i]
                if j == 0 && i != 0{
                    originHistoryItem = [UInt8]()
                }
                originHistoryItem.append(curByte)
                i+=1
                j+=1
                if j == 1024 || i == bytes.count{
                    originHistroyList.append(originHistoryItem)
                    j=0
                }
            }
            print(originHistroyList.count)
            for index in 0...(originHistroyList.count-1){
                var originHistoryItem = originHistroyList[index]
                var bleHisDataList = Utils.parseS02BleHisData(historyArray: originHistoryItem);
                var bleIndex = 0
                while bleIndex < bleHisDataList.count{
                    var bleHisDataItem = bleHisDataList[bleIndex]
                    showBleHisData.append(bleHisDataItem)
                    bleIndex+=1
                }
                break
            }
        } catch let error as Error! {
            print("读取本地数据出现错误!",error)
        }
        
    }
    
    func initData(){
        self.dateFormatter = DateFormatter()
        self.dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        if showBleHisData.count == 0{
            return
        }
        let beginItem = showBleHisData[0]
        let endItem = showBleHisData[showBleHisData.count - 1]
        self.startBattery = beginItem.battery
        self.endBattery = endItem.battery
        self.beginTemp = String(format:"%.2f",Utils.getCurTemp(sourceTemp: beginItem.temp))
        self.endTemp = String(format:"%.2f",Utils.getCurTemp(sourceTemp: endItem.temp))
        self.beginHumidity = Float(beginItem.humidity)
        self.endHumidity = Float(endItem.humidity)
        self.propOpenCount = 0
        self.propCloseCount = 0
        self.maxTemp = 0
        self.minTemp = beginItem.temp
        self.maxHumidity = 0
        self.minHumidity = beginItem.humidity
        self.tempSum=0
        self.humiditySum = 0
        self.overMaxTempLimitCount = 0
        self.overMaxTempLimitTime = 0
        self.overMinTempLimitCount = 0
        self.overMinTempLimitTime = 0
        self.overMaxHumidityLimitCount = 0
        self.overMaxHumidityLimitTime = 0
        self.overMinHumidityLimitCount = 0
        self.overMinHumidityLimitTime = 0
        var beginTempUp = false,beginTempDown = false,beginHumidityUp = false,beginHumidityDown = false;
        var startTempCalDate = 0,endTempCalDate = 0,startHumidityCalDate = 0,endHumidityCalDate = 0;
        var i = 0
        while i < showBleHisData.count{
            let bleHisData = showBleHisData[i]
            tempSum+=bleHisData.temp
            humiditySum += Float(bleHisData.humidity)
            if bleHisData.prop == 1{
                propOpenCount+=1
            }else{
                propCloseCount+=1
            }
            if bleHisData.temp > self.maxTemp{
                self.maxTemp = bleHisData.temp
            }
            if bleHisData.temp < self.minTemp{
                self.minTemp = bleHisData.temp
            }
            if bleHisData.humidity > maxHumidity{
                self.maxHumidity = bleHisData.humidity
            }
            if bleHisData.humidity < self.minHumidity{
                self.minHumidity = bleHisData.humidity
            }
            if tempAlarmUp != 4095 && bleHisData.temp > Float(tempAlarmUp){
                if !beginTempUp{
                    beginTempUp = true
                    startTempCalDate = bleHisData.dateStamp
                }
                overMaxTempLimitCount+=1
            }else{
                if beginTempUp{
                    beginTempUp = false
                    endTempCalDate = bleHisData.dateStamp
                    overMaxTempLimitTime += endTempCalDate - startTempCalDate
                }
            }
            if tempAlarmDown != 4095 && bleHisData.temp < Float(tempAlarmDown){
                if !beginTempDown{
                    beginTempDown = true
                    startTempCalDate = bleHisData.dateStamp
                }
                overMinTempLimitCount+=1
            }else{
                if beginTempDown{
                    beginTempDown = false
                    endTempCalDate = bleHisData.dateStamp
                    overMinTempLimitTime += endTempCalDate - startTempCalDate
                }
            }
            if self.deviceType == "S02" || self.deviceType == "S10"{
                if humidityAlarmUp != 4095 && bleHisData.humidity > humidityAlarmUp{
                    if !beginHumidityUp{
                        beginHumidityUp = true
                        startHumidityCalDate = bleHisData.dateStamp
                    }
                    overMaxHumidityLimitCount+=1
                }else{
                    if beginHumidityUp{
                        beginHumidityUp = false
                        endHumidityCalDate = bleHisData.dateStamp
                        overMaxHumidityLimitTime += endHumidityCalDate - startHumidityCalDate
                    }
                }
                if humidityAlarmDown != 4095 && bleHisData.humidity < humidityAlarmDown{
                    if !beginHumidityDown{
                        beginHumidityDown = true
                        startHumidityCalDate = bleHisData.dateStamp
                    }
                    overMinHumidityLimitCount+=1
                }else{
                    if beginHumidityDown{
                        beginHumidityDown = false
                        endHumidityCalDate = bleHisData.dateStamp
                        overMinHumidityLimitTime += endHumidityCalDate - startHumidityCalDate
                    }
                }
            }
            i+=1
        }
        averageHumidity = Float(humiditySum) / Float(showBleHisData.count)
        averageTemp = tempSum / Float(showBleHisData.count)
        if self.deviceType == "S02"{
            self.deviceModel = "TSTH1-B"
        }else if self.deviceType == "S04"{
            self.deviceModel = "TSDT1-B"
        }else if self.deviceType == "S08"{
            self.deviceModel = "T-sense"
        }else if self.deviceType == "S10"{
            self.deviceModel = "T-one"
        }
    }
    
    
    
    func initUI(){
        self.mainView = UIScrollView()
        self.mainView.frame = self.view.bounds
        //为了让内容横向滚动，设置横向内容宽度为3个页面的宽度总和
        self.mainView.contentSize = CGSize(width: KSize.width, height: 2000)
        //        scrollView.isPagingEnabled = true
        self.mainView.showsHorizontalScrollIndicator = false
        self.mainView.showsVerticalScrollIndicator = false
        self.mainView.scrollsToTop = false
        self.view.addSubview(self.mainView)
        let descWidth = Int(KSize.width / 3) - 5
        let contentX = Int(KSize.width / 3 + 5)
        let content2X = Int(KSize.width / 3 * 2 + 5)
        var startLabelY:Int = 0
        let lineHigh:Int = 50
        let labelHigh:Int = 50
        let descX = 5
        //sum panel
        self.sumView = UIView.init()
        sumView.backgroundColor = UIColor.clear
        sumView.layer.cornerRadius = 5;
        sumView.layer.masksToBounds = true
        sumView.layer.borderWidth = 0.3
        sumView.layer.borderColor = UIColor.gray.cgColor
        sumView.isUserInteractionEnabled = true
        self.mainView.addSubview(sumView)
        self.txDescDeviceName = UILabel()
        self.initLabel(label: self.txDescDeviceName, content: NSLocalizedString("device_name_desc", comment: "Device name:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: sumView)
        self.txDeviceName = UILabel()
        self.initLabel(label: self.txDeviceName, content: self.deviceName, x: contentX, y: startLabelY, width: Int(KSize.width) - contentX, height: labelHigh, containView: sumView)
        startLabelY += lineHigh
        
        self.txDescBleId = UILabel()
        self.initLabel(label: self.txDescBleId, content: NSLocalizedString("id", comment: "ID:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: sumView)
        self.txBleId = UILabel()
        self.initLabel(label: self.txBleId, content: self.id, x: contentX, y: startLabelY, width: Int(KSize.width) - contentX, height: labelHigh, containView: sumView)
        startLabelY += lineHigh
        
        self.txDescDeviceModel = UILabel()
        self.initLabel(label: self.txDescDeviceModel, content: NSLocalizedString("device_name_desc", comment: "Device model:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: sumView)
        self.txDeviceModel = UILabel()
        self.initLabel(label: self.txDeviceModel, content: self.deviceModel, x: contentX, y: startLabelY, width: Int(KSize.width) - contentX, height: labelHigh, containView: sumView)
        startLabelY += lineHigh
        
        self.txDescReportCreateTime = UILabel()
        self.initLabel(label: self.txDescReportCreateTime, content: NSLocalizedString("report_generation_time_desc", comment: "Report generation time:"), x: descX, y: startLabelY, width: descWidth+40, height: labelHigh, containView: sumView)
        self.txReportCreateTime = UILabel()
        let reprtCreateDate = self.dateFormatter.string(from:Date())
        self.initLabel(label: self.txReportCreateTime, content: reprtCreateDate, x: contentX+40, y: startLabelY, width: Int(KSize.width) - contentX, height: labelHigh, containView: sumView)
        startLabelY += lineHigh
        
        self.txDescReportTime = UILabel()
        self.initLabel(label: self.txDescReportTime, content: NSLocalizedString("report_time_desc", comment: "Report time:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: sumView)
        let startDateStr = self.dateFormatter.string(from: Date(timeIntervalSince1970: Double(self.startDate)))
        self.txReportBeginTime = UILabel()
        self.initLabel(label: self.txReportBeginTime, content: startDateStr, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: sumView)
        let endDateStr = self.dateFormatter.string(from: Date(timeIntervalSince1970: Double(self.endDate)))
        self.txReportEndTime = UILabel()
        self.initLabel(label: self.txReportEndTime, content: endDateStr, x: content2X, y: startLabelY, width: descWidth - 20, height: labelHigh, containView: sumView)
        startLabelY += lineHigh
        
        self.txDescBattery = UILabel()
        self.initLabel(label: self.txDescBattery, content: NSLocalizedString("battery", comment: "Battery:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: sumView)
        self.txBatteryBegin = UILabel()
        let startBatteryStr = String(format:"%.0d%%",self.startBattery)
        self.initLabel(label: self.txBatteryBegin, content: startBatteryStr, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: sumView)
        self.txBatteryEnd = UILabel()
        let endBatteryStr = String(format:"%.0d%%",self.endBattery)
        self.initLabel(label: self.txBatteryEnd, content: endBatteryStr, x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: sumView)
        startLabelY += lineHigh
        
        var reportPropDesc = NSLocalizedString("door", comment: "Door:")
        var upImageFileName = "ic_door_open.png"
        var downImageFileName = "ic_door_close.png"
        if self.deviceType == "S02" || self.deviceType == "S10"{
            reportPropDesc = NSLocalizedString("light", comment: "Light:")
            upImageFileName = "ic_light_open.png"
            downImageFileName = "ic_light_close.png"
        }
        self.txDescReportProp = UILabel()
        self.initLabel(label: self.txDescReportProp, content: reportPropDesc, x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: sumView)
        let upImage = UIImageView.init(frame: CGRect(x: contentX, y: startLabelY, width: 30, height: 30))
        upImage.contentMode = .scaleAspectFit;
        upImage.image = UIImage (named: upImageFileName)
        upImage.backgroundColor = UIColor.clear
        sumView.addSubview(upImage)
        self.txOpenCount = UILabel()
        self.initLabel(label: self.txOpenCount, content: String(self.propOpenCount), x: contentX+30, y: startLabelY, width: 60, height: labelHigh, containView: sumView)
        let downImage = UIImageView.init(frame: CGRect(x: content2X, y: startLabelY, width: 30, height: 30))
        downImage.contentMode = .scaleAspectFit;
        downImage.image = UIImage (named: downImageFileName)
        downImage.backgroundColor = UIColor.clear
        sumView.addSubview(downImage)
        self.txCloseCount = UILabel()
        self.initLabel(label: self.txCloseCount, content: String(self.propCloseCount), x: content2X+30, y: startLabelY, width: 60, height: labelHigh, containView: sumView)
        startLabelY += lineHigh
        
        let sumViewFrame =  CGRect(x: 4, y: 5, width: Int(self.view.frame.size.width)-8, height: Int(self.txDescReportProp.frame.origin.y + self.txDescReportProp.frame.height) + 10)
        self.sumView.frame = sumViewFrame
        
        //next panel
        secondView = UIView.init()
        secondView.backgroundColor = UIColor.clear
        secondView.layer.cornerRadius = 5;
        secondView.layer.masksToBounds = true
        secondView.layer.borderWidth = 0.3
        secondView.layer.borderColor = UIColor.gray.cgColor
        secondView.isUserInteractionEnabled = true
        self.mainView.addSubview(secondView)
        startLabelY = 0
        if self.deviceType == "S02" || self.deviceType == "S10"{
            self.txS02TempHead = UILabel()
            self.initLabel(label: self.txS02TempHead, content: String(format:"%@(%@)",NSLocalizedString("temp1", comment: "Temperature"),Utils.getCurTempUnit()), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityHead = UILabel()
            self.initLabel(label: self.txS02HumidityHead, content: NSLocalizedString("humidity1", comment: "Humidity"), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescStartValue = UILabel()
            self.initLabel(label: self.txDescStartValue, content: NSLocalizedString("start_value_desc", comment: "Start value:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempStart = UILabel()
            self.initLabel(label: self.txS02TempStart, content: self.beginTemp, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityStart = UILabel()
            self.initLabel(label: self.txS02HumidityStart, content: String(self.beginHumidity), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescEndValue = UILabel()
            self.initLabel(label: self.txDescEndValue, content: NSLocalizedString("end_value_desc", comment: "End value:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempEnd = UILabel()
            self.initLabel(label: self.txS02TempEnd, content: self.endTemp, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityEnd = UILabel()
            self.initLabel(label: self.txS02HumidityEnd, content: String(self.endHumidity), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescMaxLimit = UILabel()
            self.initLabel(label: self.txDescMaxLimit, content:NSLocalizedString("high_alarm_desc", comment: "High alarm:") , x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempMaxLimit = UILabel()
            var tempAlarmUpStr = "-"
            if self.tempAlarmUp != 4095{
                tempAlarmUpStr = String(format:"%.2f",self.tempAlarmUp)
            }
            self.initLabel(label: self.txS02TempMaxLimit, content: tempAlarmUpStr, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityMaxLimit = UILabel()
            var humidityAlarmUpStr = "-"
            if self.humidityAlarmUp != 4095{
                humidityAlarmUpStr = String(self.humidityAlarmUp)
            }
            self.initLabel(label: self.txS02HumidityMaxLimit, content: humidityAlarmUpStr, x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            
            self.txDescMinLimit = UILabel()
            self.initLabel(label: self.txDescMinLimit, content: NSLocalizedString("low_alarm_desc", comment: "Low alarm:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempMinLimit = UILabel()
            var tempAlarmDownStr = "-"
            if self.tempAlarmDown != 4095{
                tempAlarmDownStr = String(format:"%.2f",self.tempAlarmDown)
            }
            self.initLabel(label: self.txS02TempMinLimit, content: tempAlarmDownStr, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityMinLimit = UILabel()
            var humidityAlarmDownStr = "-"
            if self.humidityAlarmDown != 4095{
                humidityAlarmDownStr = String(self.humidityAlarmDown)
            }
            self.initLabel(label: self.txS02HumidityMinLimit, content: humidityAlarmDownStr, x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescAverage = UILabel()
            self.initLabel(label: self.txDescAverage, content: NSLocalizedString("average_desc", comment: "Average:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempAverage = UILabel()
            self.initLabel(label: self.txS02TempAverage, content: String(format:"%.2f",Utils.getCurTemp(sourceTemp: self.averageTemp)), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityAverage = UILabel()
            self.initLabel(label: self.txS02HumidityAverage, content: String(format:"%.2f",self.averageHumidity), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescMax = UILabel()
            self.initLabel(label: self.txDescMax, content: NSLocalizedString("max_desc", comment:"Max:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempMax = UILabel()
            self.initLabel(label: self.txS02TempMax, content: String(format:"%.2f",Utils.getCurTemp(sourceTemp: self.maxTemp)), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityMax = UILabel()
            self.initLabel(label: self.txS02HumidityMax, content: String(self.maxHumidity), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescMin = UILabel()
            self.initLabel(label: self.txDescMin, content: NSLocalizedString("min_desc", comment: "Min:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempMin = UILabel()
            self.initLabel(label: self.txS02TempMin, content: String(format:"%.2f",Utils.getCurTemp(sourceTemp: self.minTemp)), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityMin = UILabel()
            self.initLabel(label: self.txS02HumidityMin, content: String(self.minHumidity), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescOverHighCount = UILabel()
            self.initLabel(label: self.txDescOverHighCount, content: NSLocalizedString("high_extreme_count_desc", comment: "High extreme count:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempOverHighCount = UILabel()
            self.initLabel(label: self.txS02TempOverHighCount, content: String(format:"%d",self.overMaxTempLimitCount), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityOverHighCount = UILabel()
            self.initLabel(label: self.txS02HumidityOverHighCount, content: String(format:"%d",self.overMaxHumidityLimitCount), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescOverHighTime = UILabel()
            self.initLabel(label: self.txDescOverHighTime, content: NSLocalizedString("high_extreme_time_desc", comment: "High extreme time:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempOverHighTime = UILabel()
            self.initLabel(label: self.txS02TempOverHighTime, content: self.calDiffTime(timeDiff:self.overMaxTempLimitTime), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityOverHighTime = UILabel()
            self.initLabel(label: self.txS02HumidityOverHighTime, content: self.calDiffTime(timeDiff:self.overMaxHumidityLimitTime), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescOverLowCount = UILabel()
            self.initLabel(label: self.txDescOverLowCount, content: NSLocalizedString("low_extreme_count_desc", comment: "Low extreme count:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempOverLowCount = UILabel()
            self.initLabel(label: self.txS02TempOverLowCount, content: String(format:"%d",self.overMaxTempLimitCount), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityOverLowCount = UILabel()
            self.initLabel(label: self.txS02HumidityOverLowCount, content: String(format:"%d",self.overMaxHumidityLimitCount), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescOverLowTime = UILabel()
            self.initLabel(label: self.txDescOverLowTime, content: NSLocalizedString("Low_extreme_time_desc", comment: "Low extreme time:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02TempOverLowTime = UILabel()
            self.initLabel(label: self.txS02TempOverLowTime, content: self.calDiffTime(timeDiff:self.overMaxTempLimitTime), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS02HumidityOverLowTime = UILabel()
            self.initLabel(label: self.txS02HumidityOverLowTime, content: self.calDiffTime(timeDiff:self.overMaxHumidityLimitTime), x: content2X, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            let secondViewFrame = CGRect(x: 4, y: sumView.frame.height + sumView.frame.origin.y + 10, width: self.view.frame.size.width-8, height: CGFloat(self.txDescOverLowTime.frame.origin.y + self.txDescOverLowTime.frame.height + 10))
            secondView.frame = secondViewFrame
            
        }else{
            self.txS04TempHead = UILabel()
            self.initLabel(label: self.txS04TempHead, content: NSLocalizedString("start_value_desc", comment: "Start value:"), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescStartValue = UILabel()
            self.initLabel(label: self.txDescStartValue, content: NSLocalizedString("start_value_desc", comment: "Start value:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempStart = UILabel()
            self.initLabel(label: self.txS04TempStart, content: self.beginTemp, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescEndValue = UILabel()
            self.initLabel(label: self.txDescEndValue, content: NSLocalizedString("end_value_desc", comment: "End value:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempEnd = UILabel()
            self.initLabel(label: self.txS04TempEnd, content: self.endTemp, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescMaxLimit = UILabel()
            self.initLabel(label: self.txDescMaxLimit, content: NSLocalizedString("high_alarm_desc", comment: "High alarm:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempMaxLimit = UILabel()
            var tempAlarmUpStr = "-"
            if self.tempAlarmUp != 4095{
                tempAlarmUpStr = String(format:"%.2f",self.tempAlarmUp)
            }
            self.initLabel(label: self.txS04TempMaxLimit, content: tempAlarmUpStr, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            
            self.txDescMinLimit = UILabel()
            self.initLabel(label: self.txDescMinLimit, content:  NSLocalizedString("low_alarm_desc", comment: "Low alarm:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempMinLimit = UILabel()
            var tempAlarmDownStr = "-"
            if self.tempAlarmDown != 4095{
                tempAlarmDownStr = String(format:"%.2f",self.tempAlarmDown)
            }
            self.initLabel(label: self.txS04TempMinLimit, content: tempAlarmDownStr, x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescAverage = UILabel()
            self.initLabel(label: self.txDescAverage, content:  NSLocalizedString("average_desc", comment: "Average:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempAverage = UILabel()
            self.initLabel(label: self.txS04TempAverage, content: String(format:"%.2f",Utils.getCurTemp(sourceTemp: self.averageTemp)), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescMax = UILabel()
            self.initLabel(label: self.txDescMax, content: NSLocalizedString("max_desc", comment:"Max:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempMax = UILabel()
            self.initLabel(label: self.txS04TempMax, content: String(format:"%.2f",Utils.getCurTemp(sourceTemp: self.maxTemp)), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescMin = UILabel()
            self.initLabel(label: self.txDescMin, content: NSLocalizedString("min_desc", comment: "Min:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempMin = UILabel()
            self.initLabel(label: self.txS04TempMax, content: String(format:"%.2f",Utils.getCurTemp(sourceTemp: self.minTemp)), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescOverHighCount = UILabel()
            self.initLabel(label: self.txDescOverHighCount, content: NSLocalizedString("high_extreme_count_desc", comment: "High extreme count:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempOverHighCount = UILabel()
            self.initLabel(label: self.txS04TempOverHighCount, content: String(format:"%d",self.overMaxTempLimitCount), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescOverHighTime = UILabel()
            self.initLabel(label: self.txDescOverHighTime, content:  NSLocalizedString("high_extreme_time_desc", comment: "High extreme time:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempOverHighTime = UILabel()
            self.initLabel(label: self.txS04TempOverHighTime, content: self.calDiffTime(timeDiff:self.overMaxTempLimitTime), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescOverLowCount = UILabel()
            self.initLabel(label: self.txDescOverLowCount, content: NSLocalizedString("low_extreme_count_desc", comment: "Low extreme count:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempOverLowCount = UILabel()
            self.initLabel(label: self.txS04TempOverLowCount, content: String(format:"%d",self.overMaxTempLimitCount), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            
            self.txDescOverLowTime = UILabel()
            self.initLabel(label: self.txDescOverLowTime, content: NSLocalizedString("Low_extreme_time_desc", comment: "Low extreme time:"), x: descX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            self.txS04TempOverLowTime = UILabel()
            self.initLabel(label: self.txS04TempOverLowTime, content: self.calDiffTime(timeDiff:self.overMaxTempLimitTime), x: contentX, y: startLabelY, width: descWidth, height: labelHigh, containView: secondView)
            startLabelY += lineHigh
            let secondViewFrame = CGRect(x: 4, y: sumView.frame.height + sumView.frame.origin.y + 10, width: self.view.frame.size.width-8, height: CGFloat(self.txDescOverLowTime.frame.origin.y + self.txDescOverLowTime.frame.height + 10))
            secondView.frame = secondViewFrame
        }
        self.initTempChart()
        self.initHumidityChart()
    }
    
    func initHumidityChart(){
        if self.deviceType != "S02" && self.deviceType != "S10"{
            return
        }
        humidityChartView = LineChartView()
        humidityChartView.frame = CGRect(x: 20, y: self.tempChartView.frame.origin.y + self.tempChartView.frame.height + 10, width: self.view.bounds.width - 40,
                                         height: 300)
        self.mainView.addSubview(humidityChartView)
        //折线图背景色
        humidityChartView.backgroundColor = UIColor.white
        //折线图无数据时显示的提示文字
        humidityChartView.noDataText = ""

        //折线图描述文字和样式
        //               chartView.chartDescription?.text = "考试成绩"
        humidityChartView.chartDescription.textColor = UIColor.black

        //设置交互样式
        //                chartView.scaleXEnabled = false
        humidityChartView.scaleYEnabled = false //取消Y轴缩放
        humidityChartView.doubleTapToZoomEnabled = false //双击缩放
        humidityChartView.dragEnabled = false //启用拖动手势
        humidityChartView.dragDecelerationEnabled = false //拖拽后是否有惯性效果
        humidityChartView.dragDecelerationFrictionCoef = 0.9 //拖拽后惯性效果摩擦系数(0~1)越小惯性越不明显
        humidityChartView.xAxis.labelPosition = .bottom
        humidityChartView.rightAxis.drawLabelsEnabled = false

        humidityChartView.xAxis.granularity = Double(self.showBleHisData.count / (Int(self.view.bounds.width) / 90))
        humidityChartView.xAxis.granularityEnabled = true

        //界限1
        if self.humidityAlarmUp != 4095{
            let limitLine1 = ChartLimitLine(limit: Double(self.humidityAlarmUp), label: NSLocalizedString("high_humidity_alarm", comment: "High humidity alarm"))
            humidityChartView.leftAxis.addLimitLine(limitLine1)
        }
        //
        //         //界限2
        if self.humidityAlarmDown != 4095{
            let limitLine2 = ChartLimitLine(limit: Double(self.humidityAlarmDown), label: NSLocalizedString("low_humidity_alarm", comment: "Low humidity alarm"))
            humidityChartView.leftAxis.addLimitLine(limitLine2)
        }

        humidityChartView.leftAxis.drawLimitLinesBehindDataEnabled = true
        var chartDateFormatter = DateFormatter()
        chartDateFormatter.dateFormat = "MM-dd HH:mm:ss"
        var xValues = [String]()
        //生成8条随机数据
        var dataEntries = [ChartDataEntry]()
        let step = Int(self.showBleHisData.count / 11)
        var xStep = 0
        for i in 0..<self.showBleHisData.count {
            let bleHisItem = self.showBleHisData[i]
            let entry = ChartDataEntry.init(x: Double(i), y: Double(bleHisItem.humidity))
            dataEntries.append(entry)
            xValues.append(chartDateFormatter.string(from: Date(timeIntervalSince1970: Double(bleHisItem.dateStamp))))


        }

        humidityChartView.xAxis.valueFormatter = IndexAxisValueFormatter(values: xValues)
        //这50条数据作为1根折线里的所有数据
        let chartDataSet = LineChartDataSet(entries: dataEntries, label: NSLocalizedString("humidity1", comment: "Humidity"))
        //目前折线图只包括1根折线
        let chartData = LineChartData(dataSets: [chartDataSet])
        chartDataSet.colors = [.nordicBlue]
        //修改线条大小
        chartDataSet.lineWidth = 2
        chartDataSet.drawCirclesEnabled = false
        chartDataSet.mode = .horizontalBezier
        chartDataSet.drawValuesEnabled = false
        //设置折现图数据
        humidityChartView.data = chartData
    }
    func initTempChart(){
        tempChartView = LineChartView()
        tempChartView.frame = CGRect(x: 20, y: self.secondView.frame.origin.y + self.secondView.frame.height + 10, width: self.view.bounds.width - 40,
                                     height: 300)
        self.mainView.addSubview(tempChartView)
        //折线图背景色
        tempChartView.backgroundColor = UIColor.white
        //折线图无数据时显示的提示文字
        tempChartView.noDataText = ""

        //折线图描述文字和样式
        //               chartView.chartDescription?.text = "考试成绩"
        tempChartView.chartDescription.textColor = UIColor.black

        //设置交互样式
        //                chartView.scaleXEnabled = false
        tempChartView.scaleYEnabled = false //取消Y轴缩放
        tempChartView.doubleTapToZoomEnabled = false //双击缩放
        tempChartView.dragEnabled = false //启用拖动手势
        tempChartView.dragDecelerationEnabled = false //拖拽后是否有惯性效果
        tempChartView.dragDecelerationFrictionCoef = 0.9 //拖拽后惯性效果摩擦系数(0~1)越小惯性越不明显
        tempChartView.xAxis.labelPosition = .bottom
        tempChartView.rightAxis.drawLabelsEnabled = false

        tempChartView.xAxis.granularity = Double(self.showBleHisData.count / (Int(self.view.bounds.width) / 90))
        tempChartView.xAxis.granularityEnabled = true

        //界限1
        if self.tempAlarmUp != 4095{
            let limitLine1 = ChartLimitLine(limit: Double(self.tempAlarmUp), label: NSLocalizedString("high_temperature_alarm", comment: "High temperature alarm"))
            tempChartView.leftAxis.addLimitLine(limitLine1)
        }
        //
        //         //界限2
        if self.tempAlarmDown != 4095{
            let limitLine2 = ChartLimitLine(limit: Double(self.tempAlarmDown), label: NSLocalizedString("low_temperature_alarm", comment: "Low temperature alarm"))
            tempChartView.leftAxis.addLimitLine(limitLine2)
        }

        tempChartView.leftAxis.drawLimitLinesBehindDataEnabled = true
        var chartDateFormatter = DateFormatter()
        chartDateFormatter.dateFormat = "MM-dd HH:mm:ss"
        var xValues = [String]()
        //生成8条随机数据
        var dataEntries = [ChartDataEntry]()
        let step = Int(self.showBleHisData.count / 11)
        var xStep = 0
        for i in 0..<self.showBleHisData.count {
            let bleHisItem = self.showBleHisData[i]
            let entry = ChartDataEntry.init(x: Double(i), y: Double(Utils.getCurTemp(sourceTemp: bleHisItem.temp)))
            dataEntries.append(entry)
            xValues.append(chartDateFormatter.string(from: Date(timeIntervalSince1970: Double(bleHisItem.dateStamp))))


        }

        tempChartView.xAxis.valueFormatter = IndexAxisValueFormatter(values: xValues)
        //这50条数据作为1根折线里的所有数据
        let chartDataSet = LineChartDataSet(entries: dataEntries, label: NSLocalizedString("temp1", comment: "Temperature"))
        //目前折线图只包括1根折线
        let chartData = LineChartData(dataSets: [chartDataSet])
        chartDataSet.colors = [.orange]
        //修改线条大小
        chartDataSet.lineWidth = 2
        chartDataSet.drawCirclesEnabled = false
        chartDataSet.mode = .horizontalBezier
        chartDataSet.drawValuesEnabled = false
        //设置折现图数据
        tempChartView.data = chartData
    }
    
    func calDiffTime(timeDiff:Int) ->String{
        let minute = timeDiff / 60
        let hour = minute / 60
        return String(format:"%02d:%02d:%02d",hour,minute%60,timeDiff%60)
    }
    
    func initLabel(label:UILabel,content:String,x:Int,y:Int,width:Int,height:Int,containView:UIView){
        label.frame = CGRect(x: x, y: y, width: width, height: height)
        label.lineBreakMode = NSLineBreakMode.byWordWrapping
        label.numberOfLines = 0
        label.text = content
        label.textColor = UIColor.black
        containView.addSubview(label)
    }
    
}
