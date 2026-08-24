
import UIKit

class AlarmOpenSetViewController: UIViewController {
    private var stackTopConstraint: NSLayoutConstraint?
    
    @objc private func showPopMenuClick() {
        print("showPopMenuClick")
        self.delegate?.setNewAlarmValue(newValue: alarmOpenSet)
        navigationController?.popViewController(animated: true)
    }
    var alarmOpenSet = 0
    override func viewDidLoad() {
        super.viewDidLoad()
        self.tftInstallTopBar(
            title: NSLocalizedString("alarm_set", comment: "alarm_set"),
            rightIconName: "ic_okay.png",
            rightAction: #selector(showPopMenuClick)
        )
        view.backgroundColor = .white
        
        let stackView = UIStackView(arrangedSubviews: [
            createCheckbox(title: NSLocalizedString("enable_alarm_charging", comment: "enable_alarm_charging"), position: 0),
            createCheckbox(title: NSLocalizedString("enable_alarm_over_voltage", comment: "enable_alarm_over_voltage"), position: 1),
            createCheckbox(title: NSLocalizedString("enable_alarm_low_battery", comment: "enable_alarm_low_battery"), position: 2),
            createCheckbox(title: NSLocalizedString("enable_alarm_high_temperature", comment: "enable_alarm_high_temperature"), position: 3),
            createCheckbox(title: NSLocalizedString("enable_alarm_low_temperature", comment: "enable_alarm_low_temperature"), position:4),
            createCheckbox(title: NSLocalizedString("enable_alarm_lock_open", comment: "enable_alarm_lock_open"), position: 5),
            createCheckbox(title: NSLocalizedString("enable_alarm_back_cover_open", comment: "enable_alarm_back_cover_open"), position: 6),
            createCheckbox(title: NSLocalizedString("enable_alarm_gps_location", comment: "enable_alarm_gps_location"), position: 7, isVisible: false),
            createCheckbox(title: NSLocalizedString("enable_alarm_gps_interference", comment: "enable_alarm_gps_interference"), position: 8, isVisible: false),
            createCheckbox(title: NSLocalizedString("enable_alarm_lock_event", comment: "enable_alarm_lock_event"), position: 9),
            byteValueLabel
        ])
        
        stackView.axis = .vertical
        stackView.spacing = 8
        stackView.alignment = .leading
        stackView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(stackView)
        
        stackTopConstraint = stackView.topAnchor.constraint(equalTo: view.topAnchor, constant: 16)
        NSLayoutConstraint.activate([
            stackTopConstraint!,
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20)
        ])
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: false)
        self.tftShowTopBar()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: false)
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        self.tftLayoutTopBar()
        stackTopConstraint?.constant = self.tftTopContentY() + 16
    }
    var delegate:EditAlarmOpenDelegate?
    private func createCheckbox(title: String, position:Int,isVisible: Bool = true) -> UIView {
        // 创建一个容器视图来容纳按钮和标签
        let containerView = UIView()
        containerView.isHidden = !isVisible
            
            // 设置 containerView 的最小高度
            containerView.heightAnchor.constraint(equalToConstant: 30).isActive = true
        // 创建复选框按钮
        let checkbox = UIButton()
        checkbox.setImage(UIImage(systemName: "square"), for: .normal)
        checkbox.setImage(UIImage(systemName: "checkmark.square.fill"), for: .selected)
        checkbox.tintColor = .black
        checkbox.translatesAutoresizingMaskIntoConstraints = false
        checkbox.addTarget(self, action: #selector(toggleCheckbox(_:)), for: .touchUpInside)
        checkbox.isSelected = false
        checkbox.tag = position
        checkbox.isSelected = ((alarmOpenSet & (1 << position)) != 0)
        // 确保按钮的交互未被禁用
        checkbox.isUserInteractionEnabled = true
        
        // 创建标签
        let label = UILabel()
        label.text = title
        label.textColor = .black
        label.textAlignment = .left
        label.font = UIFont.systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        
        // 将按钮和标签添加到容器视图
        containerView.addSubview(checkbox)
        containerView.addSubview(label)
        
        // 设置约束
        NSLayoutConstraint.activate([
            checkbox.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            checkbox.centerYAnchor.constraint(equalTo: containerView.centerYAnchor),
            checkbox.widthAnchor.constraint(equalToConstant: 30),
            checkbox.heightAnchor.constraint(equalToConstant: 30),
            
            label.leadingAnchor.constraint(equalTo: checkbox.trailingAnchor, constant: 10),
            label.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            label.centerYAnchor.constraint(equalTo: containerView.centerYAnchor)
        ])
        
        return containerView
    }
    
    @objc private func toggleCheckbox(_ sender: UIButton) {
        sender.isSelected.toggle()
        print("Checkbox toggled: \(sender.isSelected)")
        var position = Int(sender.tag)
        updateByteValue(bitPosition: position, isChecked: sender.isSelected)
    }
    
    lazy var byteValueLabel: UILabel = {
        let label = UILabel()
        label.text = "Byte Value: 0"
        label.textColor = .black
        label.textAlignment = .left
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    
    private func updateByteValue(bitPosition: Int, isChecked: Bool) {
          if isChecked {
              alarmOpenSet |= (1 << bitPosition) // 设置对应位为 1
          } else {
              alarmOpenSet &= ~(1 << bitPosition) // 设置对应位为 0
          }
          byteValueLabel.text = "Byte Value: \(alarmOpenSet)" // 更新标签文本
      }
}
