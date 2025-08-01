//
//  ViewController.m
//  bleAhtiLost
//
//  Created by china topflytech on 2024/4/11.
//

#import "ViewController.h"
#import <QMUIKit.h>
#import "UIColor+Nordic.h"
#import "TFTBleManager.h"
@interface ViewController ()
@property (nonatomic, strong) UITextField* imeiEt;
@property (nonatomic, strong) UILabel* connectStatusLb;
@property (nonatomic, strong) UILabel* signalLb;
@property (nonatomic, strong) UISwitch *bleConnectSwitch;
@property (nonatomic, strong) UISwitch *twoWayAntiLostSwitch;
@property (nonatomic, strong) UISwitch *searchModeSwitch;
@property (nonatomic, strong) UITextField* notificationDurationEt;
@property (nonatomic, strong) UITextField* notificationCountEt;
@property (nonatomic, strong) UIButton *soundCb;
@property (nonatomic, strong) UIButton *shockCb;
@property (nonatomic, strong) UITextField* mobileNotificationDurationEt;
@property (nonatomic, strong) UITextField* mobileNotificationCountEt;
@property (nonatomic, strong) TFTBleManager* tftBleManager;
@property (nonatomic, strong) UIImageView *deviceImage;
@property(assign, nonatomic) CGFloat viewOriginY;

@end

@implementation ViewController
- (void) dealloc {
    
    
}

- (void)leftButtonTapped:(id)sender {
    // 左侧按钮点击事件处理
}

- (void)rightButtonTapped:(id)sender {
    // 右侧按钮点击事件处理
}


- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.navigationItem.title = @"TFT BLE AntiLost";
    
    // 配置导航栏背景色、字体颜色等
    //    self.navigationController.navigationBar.backgroundColor = UIColor.nordicBlue;
    //    self.navigationController.navigationBar.tintColor =[UIColor blackColor];
    //
    //    [[UINavigationBar appearance] setTitleTextAttributes:@{NSForegroundColorAttributeName: [UIColor blackColor]}];
    if (@available(iOS 13.0, *)) {
        UINavigationBarAppearance *appearance = [[UINavigationBarAppearance alloc] init];
        [appearance configureWithOpaqueBackground]; // 确保背景是非透明的
        appearance.backgroundColor = UIColor.nordicBlue; // 设置背景色
        
        NSDictionary *titleTextAttributes = @{NSForegroundColorAttributeName : [UIColor blackColor]};
        appearance.titleTextAttributes = titleTextAttributes; // 设置标题文字颜色
        
        self.navigationController.navigationBar.standardAppearance = appearance;
        self.navigationController.navigationBar.scrollEdgeAppearance = appearance;
    } else {
        // iOS 13 之前的导航栏外观设置
        self.navigationController.navigationBar.barTintColor = UIColor.nordicBlue;
        self.navigationController.navigationBar.tintColor = [UIColor blackColor];
        NSDictionary *titleTextAttributes = @{NSForegroundColorAttributeName : [UIColor blackColor]};
        [self.navigationController.navigationBar setTitleTextAttributes:titleTextAttributes];
    }
    
    self.navigationController.navigationBar.translucent = NO; // 禁用半透明效果
    self.tftBleManager = [[TFTBleManager alloc] init];
    self.tftBleManager.didReceiveBleStatus = ^(NSString * imei,int status) {
        [self bleStatusCallback:imei connectStatus:status];
    };
    self.tftBleManager.didReceiveDataCallback = ^(NSString * imei,NSData *data) {
        if([self.connectImei isEqual:imei]){
            [self bleNotifyDataCallback:data];
        }
        
    };
    self.tftBleManager.didReceiveRssi= ^(NSString * imei,int rssi)
    {
        if([self.connectImei isEqual:imei]){
            self.signalLb.text = [NSString stringWithFormat:@"%d dBm", rssi];
        }
    };
    [self initView];
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
    NSString *enterImei = [defaults stringForKey:@"enterImei"];
    self.imeiEt.text = enterImei;
     self.imeiEt.text = @"869487060198073";
    self.connectImei = self.imeiEt.text;
    int soundWarning = [defaults integerForKey:@"soundWarning"];
    int shockWarning = [defaults integerForKey:@"shockWarning"];
    int notificationDuration = [defaults integerForKey:@"notificationDuration"];
    int notificationCount = [defaults integerForKey:@"notificationCount"];
    self.soundCb.selected = soundWarning == 1;
    self.shockCb.selected = shockWarning == 1;
    self.mobileNotificationDurationEt.text =  [NSString stringWithFormat:@"%d", notificationDuration];
    self.mobileNotificationCountEt.text =  [NSString stringWithFormat:@"%d", notificationCount];
    self.tftBleManager.warningCount = notificationCount;
    self.tftBleManager.warningDuration = notificationDuration;
    self.tftBleManager.needSound = soundWarning == 1;
    self.tftBleManager.needShock = shockWarning == 1;
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(dismissKeyboard)];
    [self.view addGestureRecognizer:tap];
    
    
    
//    _bleConnectSwitch.on = true;
//    _connectStatusLb.text = NSLocalizedString(@"connect_succ", @"connect_succ");
//    self.signalLb.text = [NSString stringWithFormat:@"%d dBm", -56];
//    self.notificationCountEt.text = @"3";
//    self.notificationDurationEt.text = @"5";
//    self.twoWayAntiLostSwitch.on = true;
}


- (void)dismissKeyboard {
    [self.view endEditing:YES];
}
-(void) bleStatusCallback:(NSString *)imei connectStatus:(int)connectStatus{
    if(connectStatus == BLE_STATUS_OF_CONNECT_SUCC){
        _bleConnectSwitch.on = true;
        _connectStatusLb.text = NSLocalizedString(@"connect_succ", @"connect_succ");
        
        if([[_tftBleManager getCurConnectDeviceModel:imei] isEqual:MODEL_V0V_X10]){
            UIImage *image = [UIImage imageNamed:@"device_vovx10"]; // 从程序包资源加载图片
            self.deviceImage.image = image;
        }else{
            UIImage *image = [UIImage imageNamed:@"device_k100"]; // 从程序包资源加载图片
            self.deviceImage.image = image;
        }
    }else if(connectStatus == BLE_STATUS_OF_CLOSE){
        _bleConnectSwitch.on = false;
        _connectStatusLb.text = NSLocalizedString(@"ble_close", @"ble_close");
    }else if(connectStatus == BLE_STATUS_OF_DISCONNECT){
        _bleConnectSwitch.on = false;
        _connectStatusLb.text = NSLocalizedString(@"disconnected", @"disconnected");
    }else if(connectStatus == BLE_STATUS_OF_CONNECTING){
        _bleConnectSwitch.on = true;
        _connectStatusLb.text = NSLocalizedString(@"connecting", @"connecting");
    }else if(connectStatus == BLE_STATUS_OF_SCANNING){
        _bleConnectSwitch.on = true;
        _connectStatusLb.text = NSLocalizedString(@"scanning", @"scanning");
    }
}
-(void) bleNotifyDataCallback:(NSData *)data{
    NSLog(@"%@",data);
    NSUInteger dataLength = [data length];
    const uint8_t *bytes = [data bytes];
    if(dataLength > 1){
        int status = bytes[0];
        int type = bytes[1];
        if(status == 0){
            if (type == [self.tftBleManager.controlFunc[@"antiLostConn"][@"read"] intValue]
                || type == [self.tftBleManager.controlFunc[@"antiLostConn"][@"write"] intValue]
                || type == [self.tftBleManager.controlFunc[@"configParam"][@"read"] intValue]
                || type == [self.tftBleManager.controlFunc[@"configParam"][@"write"] intValue]){
                if(dataLength >= 5){
                    int twoWayAntiLost = bytes[2];
                    int notificationDuration = bytes[3]  ;
                    int notificationCount = bytes[4] ;
                    self.notificationDurationEt.text =  [NSString stringWithFormat:@"%d", notificationDuration];
                    self.notificationCountEt.text =  [NSString stringWithFormat:@"%d", notificationCount];
                    self.twoWayAntiLostSwitch.on = twoWayAntiLost == 1;
                    if(notificationDuration == 0 && notificationCount == 0){
                        [self.tftBleManager setAntiLostBleStatus:self.connectImei twoWayAntiLost:twoWayAntiLost singleVibrationDurationTime:3 repeatTime:5];
                    }
                }
            }else if(type ==[self.tftBleManager.controlFunc[@"searchMode"][@"read"] intValue]
                     || type ==[self.tftBleManager.controlFunc[@"searchMode"][@"write"] intValue]){
                int mode = bytes[2];
                self.searchModeSwitch.on = mode == 1;
            }else if(type ==[self.tftBleManager.controlFunc[@"silentMode"][@"read"] intValue]
                     || type ==[self.tftBleManager.controlFunc[@"silentMode"][@"write"] intValue]){
                
            }else if( type ==[self.tftBleManager.controlFunc[@"activelyDisconnect"][@"write"] intValue]){
                [self.tftBleManager doActivelyDisconnectCb:self.connectImei];
            }
        }else if (status == 1) {
            //password_is_error
            [self showWarningMsg:NSLocalizedString(@"password_is_error", @"password_is_error")];
        } else {
            //error_please_try_again
            [self showWarningMsg:NSLocalizedString(@"error_please_try_again", @"error_please_try_again")];
        }
    }
}




- (void)initView{
    
    CGRect KSize = UIScreen.mainScreen.bounds;
    self.view.backgroundColor = UIColor.whiteColor;
    
    
    UIScrollView *scrollView = [[UIScrollView alloc] init];
    scrollView.frame = self.view.bounds;
    CGFloat scrollViewHeight = KSize.size.height ;
    //    scrollView.frame =CGRectMake(0, 80, self.view.bounds.size.width, self.view.bounds.size.height - 80);
    
    // scrollView.isPagingEnabled = true;
    scrollView.showsHorizontalScrollIndicator = NO;
    scrollView.showsVerticalScrollIndicator = NO;
    scrollView.scrollsToTop = NO;
    [self.view addSubview:scrollView];
    
    UIView *stackViewContainer = [[UIView alloc] init];
    stackViewContainer.translatesAutoresizingMaskIntoConstraints = NO;
    
    [scrollView addSubview:stackViewContainer];
    stackViewContainer.userInteractionEnabled = YES;
    
    UIStackView *stackView = [[UIStackView alloc] init];
    stackView.axis = UILayoutConstraintAxisVertical; // 垂直布局
    stackView.alignment = UIStackViewAlignmentLeading; // 子视图居左对齐
    stackView.distribution = UIStackViewDistributionFillProportionally;
    stackView.spacing = 0;
    stackView.multipleTouchEnabled = YES; // 允许多点触控
    
    [stackViewContainer addSubview:stackView];
    scrollView.userInteractionEnabled = YES;
    stackView.userInteractionEnabled = YES;
    stackView.axis = UILayoutConstraintAxisVertical;
    stackView.distribution = UIStackViewDistributionEqualSpacing;
    //    stackView.alignment = UIStackViewAlignmentFill;
    // 设置约束
    stackView.translatesAutoresizingMaskIntoConstraints = NO;
    
    NSLayoutConstraint *topConstraint = [NSLayoutConstraint constraintWithItem:stackViewContainer attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:scrollView attribute:NSLayoutAttributeTop multiplier:1.0 constant:0.0];
    NSLayoutConstraint *leadingConstraint = [NSLayoutConstraint constraintWithItem:stackViewContainer attribute:NSLayoutAttributeLeading relatedBy:NSLayoutRelationEqual toItem:scrollView attribute:NSLayoutAttributeLeading multiplier:1.0 constant:0.0];
    NSLayoutConstraint *trailingConstraint = [NSLayoutConstraint constraintWithItem:stackViewContainer attribute:NSLayoutAttributeTrailing relatedBy:NSLayoutRelationEqual toItem:scrollView attribute:NSLayoutAttributeTrailing multiplier:1.0 constant:0.0];
    NSLayoutConstraint *bottomConstraint = [NSLayoutConstraint constraintWithItem:stackViewContainer attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:scrollView attribute:NSLayoutAttributeBottom multiplier:1.0 constant:0.0];
    NSLayoutConstraint *widthConstraint = [NSLayoutConstraint constraintWithItem:stackViewContainer attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:scrollView attribute:NSLayoutAttributeWidth multiplier:1.0 constant:0.0];
    
    NSLayoutConstraint *stackViewTopConstraint = [NSLayoutConstraint constraintWithItem:stackView attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:stackViewContainer attribute:NSLayoutAttributeTop multiplier:1.0 constant:0.0];
    NSLayoutConstraint *stackViewLeadingConstraint = [NSLayoutConstraint constraintWithItem:stackView attribute:NSLayoutAttributeLeading relatedBy:NSLayoutRelationEqual toItem:stackViewContainer attribute:NSLayoutAttributeLeading multiplier:1.0 constant:0.0];
    NSLayoutConstraint *stackViewTrailingConstraint = [NSLayoutConstraint constraintWithItem:stackView attribute:NSLayoutAttributeTrailing relatedBy:NSLayoutRelationEqual toItem:stackViewContainer attribute:NSLayoutAttributeTrailing multiplier:1.0 constant:0.0];
    NSLayoutConstraint *stackViewBottomConstraint = [NSLayoutConstraint constraintWithItem:stackView attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:stackViewContainer attribute:NSLayoutAttributeBottom multiplier:1.0 constant:0.0];
    
    NSArray *constraints = @[topConstraint, leadingConstraint, trailingConstraint, bottomConstraint, widthConstraint, stackViewTopConstraint, stackViewLeadingConstraint, stackViewTrailingConstraint, stackViewBottomConstraint];
    [NSLayoutConstraint activateConstraints:constraints];
    
    
    
    CGFloat descWidth = KSize.size.width / 2.0 - 30;
    CGFloat contentX = KSize.size.width / 2.0;
    CGFloat btnX = KSize.size.width / 2.0  + 155.0;
    
    int startLabelY = 0;
    CGFloat lineHigh = 50.0;
    int lineY = 50;
    int btnY = 15;
    CGFloat btnHeight = 30.0;
    CGFloat fontSize = 13;
    
    UIView *imeiView = [[UIView alloc] init];
    NSLayoutConstraint *heightConstraint = [imeiView.heightAnchor constraintEqualToConstant:lineHigh];
    heightConstraint.active = YES; // 设置高度约束
    NSLayoutConstraint *imeiWidthConstraint = [imeiView.widthAnchor     constraintEqualToConstant:KSize.size.width];
    imeiWidthConstraint.active = YES; // 设置宽度约束
    [stackView addArrangedSubview:imeiView]; // 将硬件视图添加到堆叠视图中
    
    UILabel *imeiLabel = [[UILabel alloc] init];
    imeiLabel.textColor = [UIColor blackColor];
    imeiLabel.font = [UIFont systemFontOfSize:fontSize];
    imeiLabel.text = @"IMEI:";
    imeiLabel.lineBreakMode = NSLineBreakByWordWrapping;
    imeiLabel.numberOfLines = 0;
    imeiLabel.textAlignment = NSTextAlignmentCenter;
    imeiLabel.frame = CGRectMake(15,20, descWidth, 30);
    [imeiView addSubview:imeiLabel];
    
    // 创建 imeiEt
    self.imeiEt = [[UITextField alloc] init];
    self.imeiEt.font = [UIFont systemFontOfSize:fontSize];
    self.imeiEt.textColor = [UIColor blackColor];
    self.imeiEt.text = @"";
    self.imeiEt.delegate = self;
    self.imeiEt.layer.cornerRadius = 10.0;
    self.imeiEt.frame = CGRectMake(contentX, 20, 140, 30);
    self.imeiEt.layer.borderWidth = 1.0f; // 设置边框宽度为1像素
    self.imeiEt.layer.borderColor = [UIColor grayColor].CGColor; // 设置边框颜色为灰色
    [imeiView addSubview:self.imeiEt];
    
    // 创建 scanImeiBtn
    QMUIButton *scanImeiBtn = [[QMUIButton alloc] init];
    scanImeiBtn.titleLabel.font = [UIFont systemFontOfSize:fontSize];
    [scanImeiBtn setImage:[UIImage imageNamed:@"ic_scan"] forState:UIControlStateNormal];
    scanImeiBtn.tintColor = UIColor.colorPrimary;
    scanImeiBtn.frame = CGRectMake(contentX + 145, 20, 30, 30);
    scanImeiBtn.userInteractionEnabled = YES;
    [scanImeiBtn addTarget:self action:@selector(scanImeiBtnClick:)   forControlEvents:UIControlEventTouchUpInside];
    imeiView.userInteractionEnabled = YES;
    [imeiView addSubview:scanImeiBtn];
    
    UIView *bleConnectView =[[UIView alloc] initWithFrame:CGRectMake(0, 0, KSize.size.width, lineHigh)];
    [bleConnectView.heightAnchor constraintEqualToConstant:lineHigh].active = YES; // 设置高度约束
    [bleConnectView.widthAnchor  constraintEqualToConstant:KSize.size.width].active = YES;
    //    NSLayoutConstraint *bleConeectHeightConstraint = [NSLayoutConstraint constraintWithItem:bleConnectView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:lineHigh];
    bleConnectView.translatesAutoresizingMaskIntoConstraints = NO;
    //    [bleConnectView addConstraint:bleConeectHeightConstraint];
    [stackView addArrangedSubview:bleConnectView];
    UILabel *bleConnectLabel = [[UILabel alloc] init];
    bleConnectLabel.textColor = [UIColor blackColor];
    bleConnectLabel.textAlignment = NSTextAlignmentCenter;
    bleConnectLabel.font = [UIFont systemFontOfSize:fontSize];
    bleConnectLabel.text = NSLocalizedString(@"ble_connect", @"Ble connection:");
    bleConnectLabel.lineBreakMode = NSLineBreakByWordWrapping;
    bleConnectLabel.numberOfLines = 0;
    bleConnectLabel.frame = CGRectMake(15, 0, descWidth, lineHigh);
    [bleConnectView addSubview:bleConnectLabel];
    bleConnectView.userInteractionEnabled = YES;
    self.bleConnectSwitch = [[UISwitch alloc] initWithFrame:CGRectMake(contentX, 10, 70, 30)];
    self.bleConnectSwitch.on = NO;
    [self.bleConnectSwitch addTarget:self action:@selector(bleConnectValueChanged:) forControlEvents:UIControlEventValueChanged];
    
    // 添加Switch到视图上
    [bleConnectView addSubview:self.bleConnectSwitch];
    
    
    UIView *containerView =[[UIView alloc] initWithFrame:CGRectMake(0, 0, KSize.size.width, 120)];
    
    NSLayoutConstraint *heightConstraint1 = [NSLayoutConstraint constraintWithItem:containerView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:120];
    containerView.translatesAutoresizingMaskIntoConstraints = NO;
    [containerView addConstraint:heightConstraint1];
    CGRect customViewFrame = CGRectInset(containerView.bounds, 5, 5); // 调整这里的值来控制外边距大小
    UIView *connectInfoView = [[UIView alloc] initWithFrame:customViewFrame];
    [containerView addSubview:connectInfoView];
    // 设置圆角半径
    connectInfoView.layer.cornerRadius = 10.0; // 根据需要调整圆角大小，这里的值为边长的 1/10
    // 设置边框宽度
    connectInfoView.layer.borderWidth = 1.0; // 根据需要调整边框宽度，这里的值为 1.0 点
    // 设置边框颜色
    connectInfoView.layer.borderColor = [UIColor blackColor].CGColor; // 使用您希望的边框颜色
    // 使裁剪边界适应视图的形状（圆角和边框），避免内容超出圆角区域
    connectInfoView.layer.masksToBounds = YES;
    NSLayoutConstraint *connectInfoWidthConstraint = [connectInfoView.widthAnchor  constraintEqualToConstant:KSize.size.width];
    connectInfoWidthConstraint.active = YES; // 设置宽度约束
    
    [stackView addArrangedSubview:containerView];
    UIImage *image = [UIImage imageNamed:@"device_k100"]; // 从程序包资源加载图片
    self.deviceImage = [[UIImageView alloc] initWithFrame:CGRectMake(30, 15, 40, 80)];
    self.deviceImage.image = image;
    [connectInfoView addSubview: self.deviceImage];
    
    UIImage *connectStatusImage = [UIImage imageNamed:@"ic_ble"]; // 从程序包资源加载图片
    UIImageView *connectStatusImageView = [[UIImageView alloc] initWithFrame:CGRectMake(110, 25, 20, 20)];
    connectStatusImageView.image = connectStatusImage;
    [connectInfoView addSubview:connectStatusImageView];
    UILabel *connectStatusLabel = [[UILabel alloc] init];
    connectStatusLabel.textColor = [UIColor blackColor];
    connectStatusLabel.font = [UIFont systemFontOfSize:fontSize];
    connectStatusLabel.text = NSLocalizedString(@"connect_status", @"Connection status:");
    connectStatusLabel.lineBreakMode = NSLineBreakByWordWrapping;
    connectStatusLabel.numberOfLines = 0;
    connectStatusLabel.frame = CGRectMake(135, 10, 80, lineHigh);
    [connectInfoView addSubview:connectStatusLabel];
    self.connectStatusLb = [[UILabel alloc] init];
    self.connectStatusLb.textColor = [UIColor blackColor];
    self.connectStatusLb.font = [UIFont systemFontOfSize:fontSize];
    self.connectStatusLb.lineBreakMode = NSLineBreakByWordWrapping;
    self.connectStatusLb.numberOfLines = 0;
    self.connectStatusLb.frame = CGRectMake(135 + 80 + 5, 10, 140, lineHigh);
    [connectInfoView addSubview:self.connectStatusLb];
    
    
    UIImage *signalImage = [UIImage imageNamed:@"ic_signal"]; // 从程序包资源加载图片
    UIImageView *signalImageView = [[UIImageView alloc] initWithFrame:CGRectMake(110, 65, 20, 20)];
    signalImageView.image = signalImage;
    [connectInfoView addSubview:signalImageView];
    UILabel *signalLabel = [[UILabel alloc] init];
    signalLabel.textColor = [UIColor blackColor];
    signalLabel.font = [UIFont systemFontOfSize:fontSize];
    signalLabel.text = NSLocalizedString(@"signal", @"Signal:");
    signalLabel.lineBreakMode = NSLineBreakByWordWrapping;
    signalLabel.numberOfLines = 0;
    signalLabel.frame = CGRectMake(135, 50, 80, lineHigh);
    [connectInfoView addSubview:signalLabel];
    self.signalLb = [[UILabel alloc] init];
    self.signalLb.textColor = [UIColor blackColor];
    self.signalLb.font = [UIFont systemFontOfSize:fontSize];
    self.signalLb.lineBreakMode = NSLineBreakByWordWrapping;
    self.signalLb.numberOfLines = 0;
    self.signalLb.frame = CGRectMake(135 + 80 + 5, 50, 80, lineHigh);
    [connectInfoView addSubview:self.signalLb];
    UIView *setContainerView =[[UIView alloc] initWithFrame:CGRectMake(0, 0, KSize.size.width, 420)];
    [setContainerView.heightAnchor constraintEqualToConstant:420].active = YES; // 设置高度约束
    [setContainerView.widthAnchor  constraintEqualToConstant:KSize.size.width].active = YES;
    //    NSLayoutConstraint *heightConstraint2 = [NSLayoutConstraint constraintWithItem:setContainerView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:420];
    setContainerView.translatesAutoresizingMaskIntoConstraints = NO;
    //    [setContainerView addConstraint:heightConstraint2];
    CGRect setCustomViewFrame = CGRectInset(setContainerView.bounds, 5, 5); // 调整这里的值来控制外边距大小
    UIView *setConnectInfoView = [[UIView alloc] initWithFrame:setCustomViewFrame];
    [setContainerView addSubview:setConnectInfoView];
    // 设置圆角半径
    setConnectInfoView.layer.cornerRadius = 10.0; // 根据需要调整圆角大小，这里的值为边长的 1/10
    // 设置边框宽度
    setConnectInfoView.layer.borderWidth = 1.0; // 根据需要调整边框宽度，这里的值为 1.0 点
    
    // 设置边框颜色
    setConnectInfoView.layer.borderColor = [UIColor blackColor].CGColor; // 使用您希望的边框颜色
    // 使裁剪边界适应视图的形状（圆角和边框），避免内容超出圆角区域
    setConnectInfoView.layer.masksToBounds = YES;
    setContainerView.translatesAutoresizingMaskIntoConstraints = NO;
    [stackView addArrangedSubview:setContainerView];
    
    UILabel *twoWayAntiLostLabel = [[UILabel alloc] init];
    twoWayAntiLostLabel.textColor = [UIColor blackColor];
    twoWayAntiLostLabel.textAlignment = NSTextAlignmentCenter;
    twoWayAntiLostLabel.font = [UIFont systemFontOfSize:fontSize];
    twoWayAntiLostLabel.text = NSLocalizedString(@"two_way_anti_lost", @"Two-way anti-lost:");
    twoWayAntiLostLabel.lineBreakMode = NSLineBreakByWordWrapping;
    twoWayAntiLostLabel.numberOfLines = 0;
    twoWayAntiLostLabel.frame = CGRectMake(10, 0, descWidth, lineHigh);
    [setContainerView addSubview:twoWayAntiLostLabel];
    self.twoWayAntiLostSwitch = [[UISwitch alloc] initWithFrame:CGRectMake(contentX, 10, 70, 30)];
    self.twoWayAntiLostSwitch.on = NO;
    [self.twoWayAntiLostSwitch addTarget:self action:@selector(twoWayAntiLostValueChanged:) forControlEvents:UIControlEventValueChanged];
    [setContainerView addSubview:self.twoWayAntiLostSwitch];
    
    UILabel *searchModeLabel = [[UILabel alloc] init];
    searchModeLabel.textColor = [UIColor blackColor];
    searchModeLabel.textAlignment = NSTextAlignmentCenter;
    searchModeLabel.font = [UIFont systemFontOfSize:fontSize];
    searchModeLabel.text = NSLocalizedString(@"search_mode", @"Search mode:");
    searchModeLabel.lineBreakMode = NSLineBreakByWordWrapping;
    searchModeLabel.numberOfLines = 0;
    searchModeLabel.frame = CGRectMake(10, 50, descWidth, lineHigh);
    [setContainerView addSubview:searchModeLabel];
    self.searchModeSwitch = [[UISwitch alloc] initWithFrame:CGRectMake(contentX, 60, 70, 30)];
    self.searchModeSwitch.on = NO;
    [self.searchModeSwitch addTarget:self action:@selector(searchModeValueChanged:) forControlEvents:UIControlEventValueChanged];
    [setContainerView addSubview:self.searchModeSwitch];
    
    UILabel *notificationDurationLabel = [[UILabel alloc] init];
    notificationDurationLabel.textColor = [UIColor blackColor];
    notificationDurationLabel.font = [UIFont systemFontOfSize:fontSize];
    notificationDurationLabel.text = NSLocalizedString(@"notification_duration", @"notification_duration:");
    notificationDurationLabel.lineBreakMode = NSLineBreakByWordWrapping;
    notificationDurationLabel.numberOfLines = 0;
    notificationDurationLabel.textAlignment = NSTextAlignmentCenter;
    notificationDurationLabel.frame = CGRectMake(15, 120, descWidth, lineHigh);
    [setContainerView addSubview:notificationDurationLabel];
    
    // 创建 notificationDurationEt
    self.notificationDurationEt = [[UITextField alloc] init];
    self.notificationDurationEt.font = [UIFont systemFontOfSize:fontSize];
    self.notificationDurationEt.textColor = [UIColor blackColor];
    self.notificationDurationEt.text = @"";
    self.notificationDurationEt.layer.cornerRadius = 10.0;
    self.notificationDurationEt.frame = CGRectMake(contentX, 120, 140, 30);
    self.notificationDurationEt.layer.borderWidth = 1.0f; // 设置边框宽度为1像素
    self.notificationDurationEt.layer.borderColor = [UIColor grayColor].CGColor; // 设置边框颜色为灰色
    self.notificationDurationEt.delegate = self;
    [setContainerView addSubview:self.notificationDurationEt];
    
    UILabel *notificationCountLabel = [[UILabel alloc] init];
    notificationCountLabel.textColor = [UIColor blackColor];
    notificationCountLabel.font = [UIFont systemFontOfSize:fontSize];
    notificationCountLabel.text = NSLocalizedString(@"notification_count", @"notification_count:");
    notificationCountLabel.lineBreakMode = NSLineBreakByWordWrapping;
    notificationCountLabel.numberOfLines = 0;
    notificationCountLabel.textAlignment = NSTextAlignmentCenter;
    notificationCountLabel.frame = CGRectMake(15, 170, descWidth, lineHigh);
    [setContainerView addSubview:notificationCountLabel];
    
    // 创建 notificationCountEt
    self.notificationCountEt = [[UITextField alloc] init];
    self.notificationCountEt.font = [UIFont systemFontOfSize:fontSize];
    self.notificationCountEt.textColor = [UIColor blackColor];
    self.notificationCountEt.text = @"";
    self.notificationCountEt.layer.cornerRadius = 10.0;
    self.notificationCountEt.frame = CGRectMake(contentX, 170, 140, 30);
    self.notificationCountEt.layer.borderWidth = 1.0f; // 设置边框宽度为1像素
    self.notificationCountEt.layer.borderColor = [UIColor grayColor].CGColor; // 设置边框颜色为灰色
    self.notificationCountEt.delegate = self;
    [setContainerView addSubview:self.notificationCountEt];
    
    UILabel *moblieWarnTypeLabel = [[UILabel alloc] init];
    moblieWarnTypeLabel.text = NSLocalizedString(@"mobile_notification_type", @"mobile_notification_type:");
    moblieWarnTypeLabel.lineBreakMode = NSLineBreakByWordWrapping;
    moblieWarnTypeLabel.numberOfLines = 0;
    moblieWarnTypeLabel.font = [UIFont systemFontOfSize:fontSize];
    moblieWarnTypeLabel.textAlignment = NSTextAlignmentCenter;
    moblieWarnTypeLabel.frame = CGRectMake(15, 220, descWidth, lineHigh);
    moblieWarnTypeLabel.textColor = [UIColor blackColor];
    [setContainerView addSubview:moblieWarnTypeLabel];
    
    
    _soundCb = [UIButton buttonWithType:UIButtonTypeCustom];
    _soundCb.frame = CGRectMake(descWidth+30, 230, 20, 20); // 设置按钮的frame位置和大小
    [_soundCb setImage:[UIImage imageNamed:@"ic_uncheckbox"] forState:UIControlStateNormal];
    [_soundCb setImage:[UIImage imageNamed:@"ic_checkbox"] forState:UIControlStateSelected];
    [_soundCb addTarget:self action:@selector(toggleCheckBox:) forControlEvents:UIControlEventTouchUpInside];
    _soundCb.selected = NO; // 更新按钮的显示状态
    [setContainerView addSubview:_soundCb]; // 将按钮添加到视图上
    UILabel *soundLabel = [[UILabel alloc] init];
    soundLabel.text = NSLocalizedString(@"sound", @"sound");
    soundLabel.lineBreakMode = NSLineBreakByWordWrapping;
    soundLabel.numberOfLines = 0;
    soundLabel.font = [UIFont systemFontOfSize:fontSize];
    soundLabel.textAlignment = NSTextAlignmentCenter;
    soundLabel.frame = CGRectMake(descWidth+50, 225, 60, 30);
    soundLabel.textColor = [UIColor blackColor];
    [setContainerView addSubview:soundLabel];
    _shockCb = [UIButton buttonWithType:UIButtonTypeCustom];
    _shockCb.frame = CGRectMake(descWidth+130, 230, 20, 20); // 设置按钮的frame位置和大小
    [_shockCb setImage:[UIImage imageNamed:@"ic_uncheckbox"] forState:UIControlStateNormal];
    [_shockCb setImage:[UIImage imageNamed:@"ic_checkbox"] forState:UIControlStateSelected];
    [_shockCb addTarget:self action:@selector(toggleCheckBox:) forControlEvents:UIControlEventTouchUpInside];
    _shockCb.selected = NO; // 更新按钮的显示状态
    [setContainerView addSubview:_shockCb]; // 将按钮添加到视图上
    UILabel *shockLabel = [[UILabel alloc] init];
    shockLabel.text = NSLocalizedString(@"shock", @"shock");
    shockLabel.lineBreakMode = NSLineBreakByWordWrapping;
    shockLabel.numberOfLines = 0;
    shockLabel.font = [UIFont systemFontOfSize:fontSize];
    shockLabel.textAlignment = NSTextAlignmentCenter;
    shockLabel.frame = CGRectMake(descWidth+150, 225, 60, 30);
    shockLabel.textColor = [UIColor blackColor];
    [setContainerView addSubview:shockLabel];
    
    UILabel *mobileNotificationDurationLabel = [[UILabel alloc] init];
    mobileNotificationDurationLabel.textColor = [UIColor blackColor];
    mobileNotificationDurationLabel.font = [UIFont systemFontOfSize:fontSize];
    mobileNotificationDurationLabel.text = NSLocalizedString(@"mobile_notification_duration", @"mobile_notification_duration:");
    mobileNotificationDurationLabel.lineBreakMode = NSLineBreakByWordWrapping;
    mobileNotificationDurationLabel.numberOfLines = 0;
    mobileNotificationDurationLabel.textAlignment = NSTextAlignmentCenter;
    mobileNotificationDurationLabel.frame = CGRectMake(15, 270, descWidth, lineHigh);
    [setContainerView addSubview:mobileNotificationDurationLabel];
    
    // 创建 mobileNotificationDurationEt
    self.mobileNotificationDurationEt = [[UITextField alloc] init];
    self.mobileNotificationDurationEt.font = [UIFont systemFontOfSize:fontSize];
    self.mobileNotificationDurationEt.textColor = [UIColor blackColor];
    self.mobileNotificationDurationEt.text = @"";
    self.mobileNotificationDurationEt.layer.cornerRadius = 10.0;
    self.mobileNotificationDurationEt.frame = CGRectMake(contentX, 270, 140, 30);
    self.mobileNotificationDurationEt.layer.borderWidth = 1.0f; // 设置边框宽度为1像素
    self.mobileNotificationDurationEt.layer.borderColor = [UIColor grayColor].CGColor; // 设置边框颜色为灰色
    self.mobileNotificationDurationEt.delegate = self;
    [setContainerView addSubview:self.mobileNotificationDurationEt];
    
    UILabel *mobileNotificationCountLabel = [[UILabel alloc] init];
    mobileNotificationCountLabel.textColor = [UIColor blackColor];
    mobileNotificationCountLabel.font = [UIFont systemFontOfSize:fontSize];
    mobileNotificationCountLabel.text = NSLocalizedString(@"mobile_notification_count", @"mobile_notification_count:");
    mobileNotificationCountLabel.lineBreakMode = NSLineBreakByWordWrapping;
    mobileNotificationCountLabel.numberOfLines = 0;
    mobileNotificationCountLabel.textAlignment = NSTextAlignmentCenter;
    mobileNotificationCountLabel.frame = CGRectMake(15, 320, descWidth, lineHigh);
    [setContainerView addSubview:mobileNotificationCountLabel];
    
    // 创建 mobileNotificationCountEt
    self.mobileNotificationCountEt = [[UITextField alloc] init];
    self.mobileNotificationCountEt.font = [UIFont systemFontOfSize:fontSize];
    self.mobileNotificationCountEt.textColor = [UIColor blackColor];
    self.mobileNotificationCountEt.text = @"";
    self.mobileNotificationCountEt.layer.cornerRadius = 10.0;
    self.mobileNotificationCountEt.frame = CGRectMake(contentX, 320, 140, 30);
    self.mobileNotificationCountEt.layer.borderWidth = 1.0f; // 设置边框宽度为1像素
    self.mobileNotificationCountEt.layer.borderColor = [UIColor grayColor].CGColor; // 设置边框颜色为灰色
    self.mobileNotificationCountEt.delegate = self;
    [setContainerView addSubview:self.mobileNotificationCountEt];
    
    QMUIButton *submitNotficationSettingBtn = [[QMUIButton alloc] init];
    submitNotficationSettingBtn.titleLabel.font = [UIFont systemFontOfSize:fontSize];
    [submitNotficationSettingBtn setTitle:NSLocalizedString(@"confirm", @"confirm") forState:UIControlStateNormal];
    submitNotficationSettingBtn.tintColor = UIColor.colorPrimary;
    submitNotficationSettingBtn.frame = CGRectMake(120, 370, 180, btnHeight);
    submitNotficationSettingBtn.userInteractionEnabled = YES;
    submitNotficationSettingBtn.layer.cornerRadius = 10.0; // 根据需要调整圆角大小，这里的值为边长的 1/10
    // 设置边框宽度
    submitNotficationSettingBtn.layer.borderWidth = 1.0; // 根据需要调整边框宽度，这里的值为 1.0 点
    // 设置边框颜色
    submitNotficationSettingBtn.layer.borderColor = [UIColor colorPrimary].CGColor; // 使用您希望的边框颜色
    [submitNotficationSettingBtn addTarget:self action:@selector(submitNotficationSettingBtnClick:)   forControlEvents:UIControlEventTouchUpInside];
    
    [setContainerView addSubview:submitNotficationSettingBtn];
    scrollView.contentSize = stackViewContainer.frame.size;
}
- (void)submitNotficationSettingBtnClick:(UIButton *)sender {
    NSLog(@"Button was tapped!");
    // 在这里添加响应按钮点击的具体逻辑
    
    Boolean needSound = self.soundCb.isSelected;
    Boolean needShock = self.shockCb.isSelected;
    NSString *notificationCountStr = self.notificationCountEt.text;
    if (notificationCountStr.length == 0) {
        [self showWarningMsg:NSLocalizedString(@"notification_count_check", @"notification_count_check")];
        return;
    }
    NSString *notificationDurationStr = self.notificationDurationEt.text;
    if (notificationDurationStr.length == 0) {
        [self showWarningMsg:NSLocalizedString(@"notification_duration_check", @"notification_duration_check")];
        return;
    }
    NSString *mobileNotificationCountStr = self.mobileNotificationCountEt.text;
    if (mobileNotificationCountStr.length == 0) {
        [self showWarningMsg:NSLocalizedString(@"mobile_notification_count_check", @"mobile_notification_count_check")];
        return;
    }
    NSString *mobileNotificationDurationStr = self.mobileNotificationDurationEt.text;
    if (mobileNotificationDurationStr.length == 0) {
        [self showWarningMsg:NSLocalizedString(@"mobile_notification_duration_check", @"mobile_notification_duration_check")];
        return;
    }
    int notificationCount =  atoi([notificationCountStr UTF8String]);
    int notificationDuration =  atoi([notificationDurationStr UTF8String]);
    int mobileNotificationCount =  atoi([mobileNotificationCountStr UTF8String]);
    int mobileNotificationDuration =  atoi([mobileNotificationDurationStr UTF8String]);
    if(notificationCount == 0 || notificationDuration == 0 || notificationCount > 255 || notificationDuration >255){
        [self showWarningMsg:NSLocalizedString(@"device_invalid_set_value_warning", @"device_invalid_set_value_warning")];
        return;
    }
    if(mobileNotificationCount == 0 || mobileNotificationDuration == 0 || mobileNotificationCount > 255 || mobileNotificationDuration >255){
        [self showWarningMsg:NSLocalizedString(@"mobile_invalid_set_value_warning", @"mobile_invalid_set_value_warning")];
        return;
    }
    int twoWayAntiLost = self.twoWayAntiLostSwitch.on ? 1 : 0;
    self.tftBleManager.needShock = needShock;
    self.tftBleManager.needSound = needSound;
    self.tftBleManager.warningCount = mobileNotificationCount;
    self.tftBleManager.warningDuration = mobileNotificationDuration;
    [self.tftBleManager setAntiLostBleStatus:self.connectImei twoWayAntiLost:twoWayAntiLost singleVibrationDurationTime:notificationDuration repeatTime:notificationCount];
    NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
    [self showWarningMsg:NSLocalizedString(@"success", @"success")];
    [defaults setObject:@(needSound ? 1 : 0) forKey:@"soundWarning"];
    [defaults setObject:@(needSound ? 1 : 0) forKey:@"shockWarning"];
    [defaults setObject:@(mobileNotificationDuration) forKey:@"notificationDuration"];
    [defaults setObject:@(mobileNotificationCount) forKey:@"notificationCount"];
    [defaults synchronize];
    //        [_tftBleManager startSendingPeriodicNotifications];
    //        UNMutableNotificationContent *content = [[UNMutableNotificationContent alloc] init];
    //        content.title = @"通知标题";
    //        content.body = @"这是通知内容";
    //        content.sound = [UNNotificationSound defaultSound];
    //        UNTimeIntervalNotificationTrigger *trigger = [UNTimeIntervalNotificationTrigger triggerWithTimeInterval:1 repeats:NO];
    //        UNNotificationRequest *request = [UNNotificationRequest requestWithIdentifier:@"MyNotification" content:content trigger:trigger];
    //        [[UNUserNotificationCenter currentNotificationCenter] addNotificationRequest:request withCompletionHandler:^(NSError * _Nullable error) {
    //            if (!error) {
    //                NSLog(@"通知添加成功");
    //            }
    //        }];
    
}
- (void)scanImeiBtnClick:(UIButton *)sender {
    NSLog(@"Button was tapped!");
    // 在这里添加响应按钮点击的具体逻辑
    QRCodeScannerViewController *scannerVC = [[QRCodeScannerViewController alloc] init];
    scannerVC.delegate = self;
    [self presentViewController:scannerVC animated:YES completion:nil];
    
}
- (void)didFindQRCode:(NSString *)code {
    self.imeiEt.text = code;
    NSLog(@"Received QR Code: %@", code);
}
- (void)twoWayAntiLostValueChanged:(UISwitch *)sender {
    NSString *notificationCountStr = self.notificationCountEt.text;
    if (notificationCountStr.length == 0) {
        [self showWarningMsg:NSLocalizedString(@"notification_count_check", @"notification_count_check")];
        self.twoWayAntiLostSwitch.on = !self.twoWayAntiLostSwitch.on;
        return;
    }
    NSString *notificationDurationStr = self.notificationDurationEt.text;
    if (notificationDurationStr.length == 0) {
        [self showWarningMsg:NSLocalizedString(@"notification_duration_check", @"notification_duration_check")];
        self.twoWayAntiLostSwitch.on = !self.twoWayAntiLostSwitch.on;
        return;
    }
    int notificationCount =  atoi([notificationCountStr UTF8String]);
    int notificationDuration =  atoi([notificationDurationStr UTF8String]);
    int twoWayAntiLost = self.twoWayAntiLostSwitch.on ? 1 : 0;
    [self.tftBleManager setAntiLostBleStatus:self.connectImei twoWayAntiLost:twoWayAntiLost singleVibrationDurationTime:notificationDuration repeatTime:notificationCount];
}
- (void)searchModeValueChanged:(UISwitch *)sender {
    int searchMode = self.searchModeSwitch.on ? 1 : 0;
    [self.tftBleManager setAntiLostBleSearchMode:self.connectImei searchMode:searchMode];
}
- (void)bleConnectValueChanged:(UISwitch *)sender {
    if(self.bleConnectSwitch.on){
        
        NSString * imei = self.imeiEt.text;
        NSString *imeiTrim = [imei stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
        
        if(imeiTrim.length != 15){
            [self showWarningMsg:NSLocalizedString(@"imei_check_error", @"imei_check_error")];
            sender.on = NO;
            return;
        }
        if([self.tftBleManager getConnectStatus:imeiTrim] == BLE_STATUS_OF_CONNECT_SUCC){
            [self.tftBleManager disconnectAll];
            [self.tftBleManager stopScanning];
            self.imeiEt.enabled = true;
            return;
        }
        // 获取 NSUserDefaults 实例
        NSUserDefaults *defaults = [[NSUserDefaults alloc] initWithSuiteName:BLE_NOTIFY_VALUE];
        // 设置字符串值
        [defaults setObject:imeiTrim forKey:@"enterImei"];
        // 保存更改
        [defaults synchronize];
        self.imeiEt.enabled = false;
        self.connectImei = imeiTrim;
        [_tftBleManager connectDevice:imeiTrim];
    }else{
        self.imeiEt.enabled = true;
        [self.tftBleManager stopScanning];
        [self.tftBleManager disconnectAll];
      
    }
    
}

-(void) showWarningMsg:(NSString *)msg{
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"" message:msg preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"confirm", @"confirm") style:UIAlertActionStyleCancel handler:nil];
    [alert addAction:cancelAction];
    [self presentViewController:alert animated:YES completion:nil];
}



- (void)toggleCheckBox:(UIButton *)sender {
    sender.selected = !sender.selected; // 更新按钮的显示状态
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string {
    // 检查输入的字符是否为数字
    NSCharacterSet *nonNumericSet = [[NSCharacterSet decimalDigitCharacterSet] invertedSet];
    BOOL containsNonNumeric = [string rangeOfCharacterFromSet:nonNumericSet].location != NSNotFound;
    
    // 禁止输入小数点
    if ([string containsString:@"."]) {
        containsNonNumeric = YES;
    }
    
    // 禁止输入负号
    if (textField.text.length == 0 && [string hasPrefix:@"-"]) {
        containsNonNumeric = YES;
    }
    
    // 如果输入的字符包含非数字字符（小数点或负号），阻止输入
    return !containsNonNumeric;
}

@end
