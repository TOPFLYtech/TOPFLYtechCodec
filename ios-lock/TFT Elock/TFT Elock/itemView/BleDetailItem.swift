 
import Foundation
import UIKit 
class BleDetailItem:  UITableViewCell {
    lazy var lockImage:UIImageView = {
        let image =  UIImageView()
        image.contentMode = .scaleAspectFit;
        image.image = UIImage (named: "ic_main_lock.png")
        image.backgroundColor = UIColor.clear
        return image
    }()
    lazy var favoriteImage:UIImageView = {
        let image =  UIImageView()
        image.contentMode = .scaleAspectFit;
        image.image = UIImage (named: "ic_hide_favorite.png")
        image.backgroundColor = UIColor.clear
        return image
    }()
    lazy var signalImage:UIImageView = {
        let image =  UIImageView()
        image.contentMode = .scaleAspectFit;
        image.image = UIImage (named: "ic_full_signal.png")
        image.backgroundColor = UIColor.clear
        return image
    }()
    lazy var deviceNameLabel: UILabel = {
        let label = UILabel()
        label.text =  NSLocalizedString("ble_name", comment: "Device name:")
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var deviceNameContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var imeiLabel: UILabel = {
        let label = UILabel()
        label.text = "IMEI:"
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var imeiContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var deviceIdContent: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var batteryContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var dateLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("date", comment: "Date:")
        return label
    }()
    lazy var dateContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var rssiLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("rssi", comment: "RSSI:")
        return label
    }()
    lazy var rssiContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var modelLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("model", comment: "Model:")
        return label
    }()
    lazy var modelContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var hardwareLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("hardware", comment: "Hardware:")
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var hardwareContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var softwareLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("software", comment:"Software:")
        return label
    }()
    lazy var softwareContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    
    
    lazy var solarVoltageLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("solar_voltage", comment:"Solar Voltage:")
        return label
    }()
    lazy var solarVoltageContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    
    lazy var batteryVoltageLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("battery_voltage", comment:"Battery Voltage:")
        return label
    }()
    lazy var batteryVoltageContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    
    
    lazy var tempLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("temp", comment:"Tempurature:")
        return label
    }()
    lazy var tempContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    lazy var switchTempUnitBtn:UIButton = {
        let btn = UIButton()
        btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        btn.layer.cornerRadius = 5;
        btn.layer.borderWidth = 1.0;
        btn.layer.borderColor = UIColor.nordicBlue.cgColor
        btn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        btn.setTitle("˚F", for: .normal)
        return btn
    }()
    lazy var alarmLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("table_head_alarm", comment:"Alarm:")
        return label
    }()
    lazy var alarmContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    
    lazy var parentLockLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("parent_lock", comment:"Parent Lock:")
        return label
    }()
    lazy var parentLockContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    
    lazy var parentLockContentExtendLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.nordicBlue
        label.text = "..."
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return label
    }()
    
    
    lazy var configLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("configDesc", comment: "Config:")
        return label
    }()
    lazy var configBtn:UIButton = {
        let btn = UIButton()
        btn.setTitle(NSLocalizedString("unlock", comment: "Unlock"), for: .normal)
        btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        btn.layer.cornerRadius = 5;
        btn.layer.borderWidth = 1.0;
        btn.layer.borderColor = UIColor.nordicBlue.cgColor
        btn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return btn
    }()
    
    lazy var readDataLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("read_data", comment: "Read Data:")
        return label
    }()
    lazy var readDataBtn:UIButton = {
        let btn = UIButton()
        btn.setTitle(NSLocalizedString("read", comment: "Read"), for: .normal)
        btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        btn.layer.cornerRadius = 5;
        btn.layer.borderWidth = 1.0;
        btn.layer.borderColor = UIColor.nordicBlue.cgColor
        btn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return btn
    }()
    
    lazy var connectDeviceLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        label.text = NSLocalizedString("connect", comment: "connect")
        return label
    }()
    lazy var connectDeviceBtn:UIButton = {
        let btn = UIButton()
        btn.setTitle(NSLocalizedString("connect", comment: "connect"), for: .normal)
        btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        btn.layer.cornerRadius = 5;
        btn.layer.borderWidth = 1.0;
        btn.layer.borderColor = UIColor.nordicBlue.cgColor
        btn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
        return btn
    }()
    
    lazy var forgetPwdLabel: UILabel = {
         let label = UILabel()
         label.textColor = UIColor.black
         label.font = UIFont.systemFont(ofSize: CGFloat(Utils.mainViewFontsize))
         label.text = NSLocalizedString("forget_pwd", comment: "Forget password:")
         return label
     }()
    lazy var parentLockLabelTips:UIImageView = {
        let image =  UIImageView()
        image.contentMode = .scaleAspectFit;
        image.image = UIImage (named: "ic_info.png")
        image.backgroundColor = UIColor.clear
        return image
    }()
     lazy var forgetPwdBtn:UIButton = {
         let btn = UIButton()
         btn.setTitle(NSLocalizedString("reset", comment: "Reset"), for: .normal)
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
    static let oneLineHeight = 20
    static let btnLineHeight = 25
    static func getLayoutHeight(bleDeviceData:BleDeviceData) -> Int32{
        var parentLockHeight:Int = 165
        var subLockHeight:Int = 295
        if BleDeviceData.isSupportConfig(model: bleDeviceData.model, version:bleDeviceData.software , deviceId: bleDeviceData.deviceId){
            parentLockHeight += btnLineHeight
            subLockHeight += btnLineHeight
        }
        if bleDeviceData.viewShowReadHisBtn{
            parentLockHeight += btnLineHeight
            subLockHeight += btnLineHeight
        }
        if bleDeviceData.viewIsExpand{
            if bleDeviceData.isSubLock{
                return Int32(subLockHeight)
            }else{
                return Int32(parentLockHeight)
            }
        }
        return 85
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
    
    func initLayout(bleDeviceData:BleDeviceData){
        let left = 60
        let right = 110
       
        
        var curHigh = 8
        let contentLen = Int(self.bounds.size.width) - left - right - 10
        let rightContentLeft = left + contentLen
        var rootViewHeight = 110
        let descWidth = self.bounds.size.width / 2 - 20
        let contentX = self.bounds.size.width / 2
        
        if self.checkIsLostSignal(bleDeviceData: bleDeviceData){
            self.signalImage.image = UIImage(named: "ic_no_signal.png")
        }else{
            self.signalImage.image = UIImage (named: "ic_full_signal.png")
        }
  
        self.lockImage.frame = CGRect(x: 8, y: 8, width: 40, height: 40)
        self.favoriteImage.frame = CGRect(x: 15, y: 50, width: 22, height: 22)
        
        self.deviceNameContentLabel.frame = CGRect(x: left, y: curHigh, width: contentLen, height: BleDetailItem.oneLineHeight)
        curHigh+=BleDetailItem.oneLineHeight
        self.imeiContentLabel.frame = CGRect(x: left, y: curHigh, width: contentLen, height: BleDetailItem.oneLineHeight)
        curHigh+=BleDetailItem.oneLineHeight
        self.deviceIdContent.frame = CGRect(x: left, y: curHigh, width: contentLen, height: BleDetailItem.oneLineHeight)
     
        self.configBtn.frame = CGRect(x: rightContentLeft + 20, y: 20, width: 70, height: BleDetailItem.oneLineHeight)
        self.signalImage.frame = CGRect(x: rightContentLeft, y: curHigh, width: 15, height: 15)
        self.rssiContentLabel.frame = CGRect(x: rightContentLeft+18, y: curHigh, width: 55, height: BleDetailItem.oneLineHeight)
        self.batteryContentLabel.frame = CGRect(x: rightContentLeft+70, y: curHigh, width: 35, height: BleDetailItem.oneLineHeight)
        curHigh+=BleDetailItem.oneLineHeight
     
        self.forgetPwdBtn.isHidden = true
        self.forgetPwdLabel.isHidden = true
        self.modelLabel.isHidden = true
        self.modelContentLabel.isHidden = true
        self.hardwareLabel.isHidden = true
        self.hardwareContentLabel.isHidden = true
        self.softwareLabel.isHidden = true
        self.softwareContentLabel.isHidden = true
        self.dateLabel.isHidden = true
        self.dateContentLabel.isHidden = true
        self.readDataLabel.isHidden = true
        self.readDataBtn.isHidden = true
        self.solarVoltageLabel.isHidden = true
        self.solarVoltageContentLabel.isHidden = true
        self.batteryVoltageLabel.isHidden = true
        self.batteryVoltageContentLabel.isHidden = true
        self.tempLabel.isHidden = true
        self.tempContentLabel.isHidden = true
        self.alarmLabel.isHidden = true
        self.alarmContentLabel.isHidden = true
        self.parentLockLabel.isHidden = true
        self.parentLockContentLabel.isHidden = true
        self.switchTempUnitBtn.isHidden = true
        self.configLabel.isHidden = true
        self.parentLockLabelTips.isHidden = true
        self.parentLockContentExtendLabel.isHidden = true
        self.connectDeviceBtn.isHidden = true
        self.connectDeviceLabel.isHidden = true
        if bleDeviceData.viewIsExpand{
            self.modelLabel.isHidden = false
            self.modelContentLabel.isHidden = false
            self.hardwareLabel.isHidden = false
            self.hardwareContentLabel.isHidden = false
            self.softwareLabel.isHidden = false
            self.softwareContentLabel.isHidden = false
            self.dateLabel.isHidden = false
            self.dateContentLabel.isHidden = false
            self.modelLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
            self.modelContentLabel.frame = CGRect(x: contentX, y: CGFloat(curHigh), width: self.bounds.size.width - contentX, height: 30)
            curHigh+=BleDetailItem.oneLineHeight
            self.hardwareLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
            self.hardwareContentLabel.frame = CGRect(x: contentX, y: CGFloat(curHigh), width: self.bounds.size.width - contentX, height: 30)
            curHigh+=BleDetailItem.oneLineHeight
            self.softwareLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
            self.softwareContentLabel.frame = CGRect(x: contentX, y: CGFloat(curHigh), width: self.bounds.size.width - contentX, height: 30)
            curHigh+=BleDetailItem.oneLineHeight
            
            if bleDeviceData.isSubLock{
                self.solarVoltageLabel.isHidden = false
                self.solarVoltageContentLabel.isHidden = false
                self.batteryVoltageLabel.isHidden = false
                self.batteryVoltageContentLabel.isHidden = false
                self.tempLabel.isHidden = false
                self.tempContentLabel.isHidden = false
                self.alarmLabel.isHidden = false
                self.alarmContentLabel.isHidden = false
                self.parentLockLabel.isHidden = false
                self.parentLockContentLabel.isHidden = false
                self.switchTempUnitBtn.isHidden = false
                self.parentLockLabelTips.isHidden = false 
                
                self.solarVoltageLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
                self.solarVoltageContentLabel.frame = CGRect(x: contentX, y: CGFloat(curHigh), width: self.bounds.size.width - contentX, height: 30)
                curHigh+=BleDetailItem.oneLineHeight
                self.batteryVoltageLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
                self.batteryVoltageContentLabel.frame = CGRect(x: contentX, y: CGFloat(curHigh), width: self.bounds.size.width - contentX, height: 30)
                curHigh+=BleDetailItem.oneLineHeight
                self.tempLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
                self.tempContentLabel.frame = CGRect(x: contentX, y: CGFloat(curHigh), width: self.bounds.size.width - contentX - 70, height: 30)
                self.switchTempUnitBtn.frame = CGRect(x: self.bounds.size.width - 70, y: CGFloat(curHigh), width: 50, height: 24)
//                self.switchTempUnitBtn.setTitleShadowColor(UIColor.green,for: .highlighted)
//                self.switchTempUnitBtn.setTitleShadowColor(UIColor.black,for: .normal)
                curHigh+=BleDetailItem.oneLineHeight
                self.alarmLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
                self.alarmContentLabel.frame = CGRect(x: contentX, y: CGFloat(curHigh), width: self.bounds.size.width - contentX, height: 30)
                curHigh+=BleDetailItem.oneLineHeight
                self.parentLockLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth - 40), height: BleDetailItem.oneLineHeight)
                self.parentLockContentLabel.frame = CGRect(x: contentX, y: CGFloat(curHigh), width: self.bounds.size.width - contentX - 30, height: 30)
                self.parentLockLabelTips.frame = CGRect(x: contentX - 40, y: CGFloat(curHigh + 10), width: 15, height: 15)
                self.parentLockContentExtendLabel.frame = CGRect(x: self.bounds.size.width - 30, y: CGFloat(curHigh), width: 30, height: 30)
                self.parentLockLabelTips.isUserInteractionEnabled = true
                self.parentLockContentExtendLabel.isUserInteractionEnabled = true
                curHigh+=BleDetailItem.oneLineHeight
            }
            
            self.dateLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
            self.dateContentLabel.frame = CGRect(x: contentX, y: CGFloat(curHigh), width: self.bounds.size.width - contentX, height: 30)
            curHigh+=BleDetailItem.oneLineHeight
            if BleDeviceData.isSupportConfig(model: bleDeviceData.model, version:bleDeviceData.software , deviceId: bleDeviceData.deviceId){
                self.connectDeviceLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
                self.connectDeviceBtn.frame = CGRect(x: Int(contentX), y: curHigh + 3, width: 70, height: BleDetailItem.oneLineHeight)
                curHigh+=BleDetailItem.btnLineHeight
                self.connectDeviceLabel.isHidden = false
                self.connectDeviceBtn.isHidden = false
            }
            if bleDeviceData.viewShowReadHisBtn{
                self.readDataLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
                self.readDataBtn.frame = CGRect(x: Int(contentX), y: curHigh + 3, width: 70, height: BleDetailItem.oneLineHeight)
                curHigh+=BleDetailItem.btnLineHeight
                self.readDataLabel.isHidden = false
                self.readDataBtn.isHidden = false
            }
            if bleDeviceData.isSubLock{
                self.forgetPwdBtn.isHidden = false
                self.forgetPwdLabel.isHidden = false
                self.forgetPwdLabel.frame = CGRect(x: 8, y: curHigh, width: Int(descWidth), height: BleDetailItem.oneLineHeight)
                self.forgetPwdBtn.frame = CGRect(x: Int(contentX), y: curHigh + 3, width: 70, height: BleDetailItem.oneLineHeight)
                curHigh+=BleDetailItem.btnLineHeight
            }
        }
        self.rootView.frame = CGRect(x: 5, y: 5, width: Int(self.bounds.size.width)-10, height: curHigh+12)
        self.rootView.isUserInteractionEnabled=true
    }
    
    func initLayoutPosition()
    {
        let descWidth = self.bounds.size.width / 2 - 20
        let contentX = self.bounds.size.width / 2
        let oneLineHeight = 30
        self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 290)
        self.rootView.isUserInteractionEnabled=true
        self.deviceNameLabel.frame = CGRect(x: 8, y: 8, width: Int(descWidth), height: oneLineHeight)
        self.deviceNameContentLabel.frame = CGRect(x: contentX, y: 8, width: self.bounds.size.width - contentX, height: CGFloat(oneLineHeight))
        self.imeiLabel.frame = CGRect(x: 8, y: 38, width: Int(descWidth), height: oneLineHeight)
        self.imeiContentLabel.frame = CGRect(x: contentX, y: 38, width: self.bounds.size.width - contentX, height: CGFloat(oneLineHeight))
        self.modelLabel.frame = CGRect(x: 8, y: 68, width: Int(descWidth), height: oneLineHeight)
        self.modelContentLabel.frame = CGRect(x: contentX, y: 68, width: self.bounds.size.width - contentX, height: CGFloat(oneLineHeight))
        self.hardwareLabel.frame = CGRect(x: 8, y: 98, width: Int(descWidth), height: oneLineHeight)
        self.hardwareContentLabel.frame = CGRect(x: contentX, y: 98, width: self.bounds.size.width - contentX, height: CGFloat(oneLineHeight))
        self.softwareLabel.frame = CGRect(x: 8, y: 128, width: Int(descWidth), height: oneLineHeight)
        self.softwareContentLabel.frame = CGRect(x: contentX, y: 128, width: self.bounds.size.width - contentX, height: CGFloat(oneLineHeight))
        self.dateLabel.frame = CGRect(x: 8, y: 158, width: Int(descWidth), height: oneLineHeight)
        self.dateContentLabel.frame = CGRect(x: contentX, y: 158, width: self.bounds.size.width - contentX, height: CGFloat(oneLineHeight))
        self.rssiLabel.frame = CGRect(x: 8, y: 188, width: Int(descWidth), height: oneLineHeight)
        self.rssiContentLabel.frame = CGRect(x: contentX, y: 188, width: self.bounds.size.width - contentX, height: CGFloat(oneLineHeight))
        self.configLabel.frame = CGRect(x: 8, y: 218, width: Int(descWidth), height: oneLineHeight)
        self.configBtn.frame = CGRect(x: Int(contentX), y: 223, width: 70, height: oneLineHeight)
        self.readDataLabel.frame = CGRect(x: 8, y: 248, width: Int(descWidth), height: oneLineHeight)
        self.readDataBtn.frame = CGRect(x: Int(contentX), y: 253, width: 70, height: oneLineHeight)
        self.rootView.backgroundColor = UIColor.white
        self.rootView.layer.cornerRadius = 8
        self.rootView.layer.masksToBounds = true
        self.rootView.layer.borderWidth = 1
        self.rootView.layer.borderColor = UIColor.nordicLightGray.cgColor
    }
    
    
    func snpLayoutSubview(){
        self.selectionStyle = UITableViewCell.SelectionStyle.none
        self.contentView.addSubview(rootView)
        self.rootView.addSubview(deviceNameContentLabel)
        self.rootView.addSubview(dateLabel)
        self.rootView.addSubview(dateContentLabel)
        self.rootView.addSubview(rssiContentLabel)
        self.rootView.addSubview(modelLabel)
        self.rootView.addSubview(modelContentLabel)
        self.rootView.addSubview(hardwareLabel)
        self.rootView.addSubview(hardwareContentLabel)
        self.rootView.addSubview(softwareLabel)
        self.rootView.addSubview(softwareContentLabel)
        self.rootView.addSubview(configLabel)
        self.rootView.addSubview(configBtn)
        self.rootView.addSubview(imeiContentLabel)
        self.rootView.addSubview(readDataLabel)
        self.rootView.addSubview(readDataBtn)
        self.rootView.addSubview(lockImage)
        self.rootView.addSubview(self.deviceIdContent)
        self.rootView.addSubview(signalImage)
        self.rootView.addSubview(batteryContentLabel)
        self.rootView.addSubview(favoriteImage)
        self.rootView.addSubview(solarVoltageLabel)
        self.rootView.addSubview(solarVoltageContentLabel)
        self.rootView.addSubview(batteryVoltageLabel)
        self.rootView.addSubview(batteryVoltageContentLabel)
        self.rootView.addSubview(tempLabel)
        self.rootView.addSubview(tempContentLabel)
        self.rootView.addSubview(alarmLabel)
        self.rootView.addSubview(alarmContentLabel)
        self.rootView.addSubview(parentLockLabel)
        self.rootView.addSubview(parentLockContentLabel)
        self.rootView.addSubview(switchTempUnitBtn)
        self.rootView.addSubview(forgetPwdBtn)
        self.rootView.addSubview(forgetPwdLabel)
        self.rootView.addSubview(parentLockLabelTips)
        self.rootView.addSubview(parentLockContentExtendLabel)
        self.rootView.addSubview(connectDeviceLabel)
        self.rootView.addSubview(connectDeviceBtn)
        self.initLayoutPosition()
        
    }
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        snpLayoutSubview()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    static let identifier = "BleDetailItem"
    static func dequeueReusable(with tableView:UITableView) -> BleDetailItem {
        let reusableCell = tableView.dequeueReusableCell(withIdentifier: BleDetailItem.identifier)
        if  let cell = reusableCell as? BleDetailItem {
            return cell
        }else{
            return BleDetailItem.init(style: .default, reuseIdentifier: BleDetailItem.identifier)
        }
    }
}
