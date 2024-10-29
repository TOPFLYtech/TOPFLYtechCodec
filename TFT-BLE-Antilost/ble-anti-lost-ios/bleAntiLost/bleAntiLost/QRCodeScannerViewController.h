//
//  QRCodeScannerViewController.h
//  bleAntiLost
//
//  Created by china topflytech on 2024/6/14.
//

// QRCodeScannerViewController.h
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

@protocol QRCodeScannerDelegate <NSObject>
- (void)didFindQRCode:(NSString *)code;
@end

@interface QRCodeScannerViewController : UIViewController <AVCaptureMetadataOutputObjectsDelegate>
@property (nonatomic, weak) id<QRCodeScannerDelegate> delegate;
@end
