 
import Foundation
import UIKit 
class BleDetailItem:  UITableViewCell {
    lazy var deviceNameLabel: UILabel = {
        let label = UILabel()
        label.text =  NSLocalizedString("ble_name", comment: "Device name:")
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var deviceNameContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var imeiLabel: UILabel = {
        let label = UILabel()
        label.text = "IMEI:"
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var imeiContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var dateLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        label.text = NSLocalizedString("date", comment: "Date:")
        return label
    }()
    lazy var dateContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var rssiLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        label.text = NSLocalizedString("rssi", comment: "RSSI:")
        return label
    }()
    lazy var rssiContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var modelLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        label.text = NSLocalizedString("model", comment: "Model:")
        return label
    }()
    lazy var modelContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var hardwareLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.text = NSLocalizedString("hardware", comment: "Hardware:")
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var hardwareContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var softwareLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        label.text = NSLocalizedString("software", comment:"Software:")
        return label
    }()
    lazy var softwareContentLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return label
    }()
    lazy var configLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        label.text = NSLocalizedString("configDesc", comment: "Config:")
        return label
    }()
    lazy var configBtn:UIButton = {
        let btn = UIButton()
        btn.setTitle(NSLocalizedString("unlock", comment: "Unlock"), for: .normal)
        btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        btn.layer.cornerRadius = 15;
        btn.layer.borderWidth = 1.0;
        btn.layer.borderColor = UIColor.nordicBlue.cgColor
        btn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return btn
    }()
    
    lazy var readDataLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.black
        label.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        label.text = NSLocalizedString("read_data", comment: "Read Data:")
        return label
    }()
    lazy var readDataBtn:UIButton = {
        let btn = UIButton()
        btn.setTitle(NSLocalizedString("read", comment: "Read"), for: .normal)
        btn.setTitleColor(UIColor.nordicBlue, for: .normal)
        btn.layer.cornerRadius = 15;
        btn.layer.borderWidth = 1.0;
        btn.layer.borderColor = UIColor.nordicBlue.cgColor
        btn.titleLabel!.font = UIFont.systemFont(ofSize: CGFloat(Utils.fontSize))
        return btn
    }()
    lazy var rootView :UIImageView = {
        let view = UIImageView()
        return view
    }()
    func initLayoutPosition()
    {
        let descWidth = self.bounds.size.width / 2 - 20
        let contentX = self.bounds.size.width / 2
        self.rootView.frame = CGRect(x: 5, y: 5, width: self.bounds.size.width-10, height: 290)
        self.rootView.isUserInteractionEnabled=true
        self.deviceNameLabel.frame = CGRect(x: 8, y: 8, width: descWidth, height: 30)
        self.deviceNameContentLabel.frame = CGRect(x: contentX, y: 8, width: self.bounds.size.width - contentX, height: 30)
        self.imeiLabel.frame = CGRect(x: 8, y: 38, width: descWidth, height: 30)
        self.imeiContentLabel.frame = CGRect(x: contentX, y: 38, width: self.bounds.size.width - contentX, height: 30)
        self.modelLabel.frame = CGRect(x: 8, y: 68, width: descWidth, height: 30)
        self.modelContentLabel.frame = CGRect(x: contentX, y: 68, width: self.bounds.size.width - contentX, height: 30)
        self.hardwareLabel.frame = CGRect(x: 8, y: 98, width: descWidth, height: 30)
        self.hardwareContentLabel.frame = CGRect(x: contentX, y: 98, width: self.bounds.size.width - contentX, height: 30)
        self.softwareLabel.frame = CGRect(x: 8, y: 128, width: descWidth, height: 30)
        self.softwareContentLabel.frame = CGRect(x: contentX, y: 128, width: self.bounds.size.width - contentX, height: 30)
        self.dateLabel.frame = CGRect(x: 8, y: 158, width: descWidth, height: 30)
        self.dateContentLabel.frame = CGRect(x: contentX, y: 158, width: self.bounds.size.width - contentX, height: 30)
        self.rssiLabel.frame = CGRect(x: 8, y: 188, width: descWidth, height: 30)
        self.rssiContentLabel.frame = CGRect(x: contentX, y: 188, width: self.bounds.size.width - contentX, height: 30)
        self.configLabel.frame = CGRect(x: 8, y: 218, width: descWidth, height: 30)
        self.configBtn.frame = CGRect(x: contentX, y: 223, width: 70, height: 25)
        self.readDataLabel.frame = CGRect(x: 8, y: 248, width: descWidth, height: 30)
        self.readDataBtn.frame = CGRect(x: contentX, y: 253, width: 70, height: 25)
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
        self.rootView.addSubview(hardwareLabel)
        self.rootView.addSubview(hardwareContentLabel)
        self.rootView.addSubview(softwareLabel)
        self.rootView.addSubview(softwareContentLabel)
        self.rootView.addSubview(configLabel)
        self.rootView.addSubview(configBtn)
        self.rootView.addSubview(imeiLabel)
        self.rootView.addSubview(imeiContentLabel)
        self.rootView.addSubview(readDataLabel)
        self.rootView.addSubview(readDataBtn)
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
