import UIKit
import ObjectiveC

private var tftTopBarKey: UInt8 = 0
private var tftTopBarTitleKey: UInt8 = 0
private var tftTopBarLeftButtonKey: UInt8 = 0
private var tftTopBarRightStackKey: UInt8 = 0
private var tftTopBarLastContentYKey: UInt8 = 0

extension UIViewController {
    private var tftTopBarView: UIView? {
        get { objc_getAssociatedObject(self, &tftTopBarKey) as? UIView }
        set { objc_setAssociatedObject(self, &tftTopBarKey, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC) }
    }

    private var tftTopBarTitleLabel: UILabel? {
        get { objc_getAssociatedObject(self, &tftTopBarTitleKey) as? UILabel }
        set { objc_setAssociatedObject(self, &tftTopBarTitleKey, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC) }
    }

    private var tftTopBarLeftButton: UIButton? {
        get { objc_getAssociatedObject(self, &tftTopBarLeftButtonKey) as? UIButton }
        set { objc_setAssociatedObject(self, &tftTopBarLeftButtonKey, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC) }
    }

    private var tftTopBarRightStack: UIStackView? {
        get { objc_getAssociatedObject(self, &tftTopBarRightStackKey) as? UIStackView }
        set { objc_setAssociatedObject(self, &tftTopBarRightStackKey, newValue, .OBJC_ASSOCIATION_RETAIN_NONATOMIC) }
    }

    private var tftLastAppliedContentY: CGFloat {
        get { (objc_getAssociatedObject(self, &tftTopBarLastContentYKey) as? NSNumber).map { CGFloat($0.doubleValue) } ?? 0 }
        set { objc_setAssociatedObject(self, &tftTopBarLastContentYKey, NSNumber(value: Double(newValue)), .OBJC_ASSOCIATION_RETAIN_NONATOMIC) }
    }

    func tftCurrentSafeTopInset() -> CGFloat {
        if #available(iOS 13.0, *) {
            if let inset = UIApplication.shared.windows.first(where: { $0.isKeyWindow })?.safeAreaInsets.top, inset > 0 {
                return inset
            }
        }
        let inset = view.safeAreaInsets.top
        if inset > 0 {
            return inset
        }
        return UIApplication.shared.statusBarFrame.height
    }

    func tftTopContentY() -> CGFloat {
        return tftCurrentSafeTopInset() + 52
    }

    @objc private func tftDefaultBackAction() {
        navigationController?.popViewController(animated: true)
    }

    func tftInstallTopBar(title: String, rightIconName: String? = nil, rightAction: Selector? = nil) {
        let topBar: UIView
        let titleLabel: UILabel
        let leftButton: UIButton
        let rightStack: UIStackView

        if let existing = tftTopBarView,
           let existingTitle = tftTopBarTitleLabel,
           let existingLeft = tftTopBarLeftButton,
           let existingStack = tftTopBarRightStack {
            topBar = existing
            titleLabel = existingTitle
            leftButton = existingLeft
            rightStack = existingStack
        } else {
            topBar = UIView()
            topBar.backgroundColor = UIColor.colorPrimary

            titleLabel = UILabel()
            titleLabel.textColor = .white
            titleLabel.textAlignment = .center
            titleLabel.font = UIFont.boldSystemFont(ofSize: 22)

            leftButton = UIButton(type: .custom)
            leftButton.setImage(UIImage(named: "ic_back.png"), for: .normal)
            leftButton.addTarget(self, action: #selector(tftDefaultBackAction), for: .touchUpInside)

            rightStack = UIStackView()
            rightStack.axis = .horizontal
            rightStack.alignment = .center
            rightStack.distribution = .fillEqually
            rightStack.spacing = 8

            topBar.addSubview(titleLabel)
            topBar.addSubview(leftButton)
            topBar.addSubview(rightStack)
            view.addSubview(topBar)

            tftTopBarView = topBar
            tftTopBarTitleLabel = titleLabel
            tftTopBarLeftButton = leftButton
            tftTopBarRightStack = rightStack
        }

        titleLabel.text = title
        tftSetTopBarRightButtons([])
        if let iconName = rightIconName, let action = rightAction {
            tftSetTopBarRightButtons([(iconName, action)])
        }
        tftLayoutTopBar()
        tftShowTopBar()
    }

    func tftSetTopBarTitle(_ title: String) {
        tftTopBarTitleLabel?.text = title
    }

    func tftSetTopBarRightButtons(_ buttons: [(iconName: String, action: Selector)]) {
        guard let rightStack = tftTopBarRightStack else { return }
        rightStack.arrangedSubviews.forEach { item in
            rightStack.removeArrangedSubview(item)
            item.removeFromSuperview()
        }

        for (iconName, action) in buttons {
            let button = UIButton(type: .custom)
            button.setImage(UIImage(named: iconName), for: .normal)
            button.frame = CGRect(x: 0, y: 0, width: 32, height: 32)
            button.widthAnchor.constraint(equalToConstant: 32).isActive = true
            button.heightAnchor.constraint(equalToConstant: 32).isActive = true
            button.addTarget(self, action: action, for: .touchUpInside)
            rightStack.addArrangedSubview(button)
        }
    }

    func tftSetTopBarRightCustomButtons(_ buttons: [UIButton]) {
        guard let rightStack = tftTopBarRightStack else { return }
        rightStack.arrangedSubviews.forEach { item in
            rightStack.removeArrangedSubview(item)
            item.removeFromSuperview()
        }
        for button in buttons {
            let width = max(24, button.frame.width > 0 ? button.frame.width : 32)
            let height = max(24, button.frame.height > 0 ? button.frame.height : 32)
            button.widthAnchor.constraint(equalToConstant: width).isActive = true
            button.heightAnchor.constraint(equalToConstant: height).isActive = true
            rightStack.addArrangedSubview(button)
        }
    }

    func tftLayoutTopBar() {
        guard let topBar = tftTopBarView,
              let titleLabel = tftTopBarTitleLabel,
              let leftButton = tftTopBarLeftButton,
              let rightStack = tftTopBarRightStack else {
            return
        }

        let safeTop = tftCurrentSafeTopInset()
        let barHeight: CGFloat = 52
        let width = view.bounds.width

        topBar.frame = CGRect(x: 0, y: 0, width: width, height: safeTop + barHeight)

        let buttonY = safeTop + (barHeight - 32) / 2
        leftButton.frame = CGRect(x: 10, y: buttonY, width: 32, height: 32)

        let rightWidth = max(CGFloat(rightStack.arrangedSubviews.count) * 32 + CGFloat(max(0, rightStack.arrangedSubviews.count - 1)) * 8, 32)
        rightStack.frame = CGRect(x: width - 10 - rightWidth, y: buttonY, width: rightWidth, height: 32)

        let leftLimit = leftButton.frame.maxX + 8
        let rightLimit = rightStack.frame.minX - 8
        let titleWidth = max(0, rightLimit - leftLimit)
        titleLabel.frame = CGRect(x: leftLimit, y: safeTop, width: titleWidth, height: barHeight)

        view.bringSubviewToFront(topBar)
    }

    func tftAdjustContentBelowTopBarIfNeeded() {
        let targetTop = tftTopContentY()
        let delta = targetTop - tftLastAppliedContentY
        if abs(delta) < 0.5 {
            return
        }

        guard let topBar = tftTopBarView else {
            tftLastAppliedContentY = targetTop
            return
        }

        for subview in view.subviews where subview !== topBar {
            if !subview.translatesAutoresizingMaskIntoConstraints {
                continue
            }
            var frame = subview.frame
            frame.origin.y += delta
            subview.frame = frame
        }
        tftLastAppliedContentY = targetTop
    }

    func tftShowTopBar() {
        tftTopBarView?.isHidden = false
    }

    func tftHideTopBar() {
        tftTopBarView?.isHidden = true
    }
}
