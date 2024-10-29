//
//  ViewController.h
//  bleAhtiLost
//
//  Created by china topflytech on 2024/4/11.
//

#import <UIKit/UIKit.h>
#import "QRCodeScannerViewController.h"
@interface ViewController : UIViewController <UITextFieldDelegate,QRCodeScannerDelegate>
@property (nonatomic, strong) NSString *connectImei;

@end

