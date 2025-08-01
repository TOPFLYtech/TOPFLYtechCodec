//
//  QRCodeScannerViewController.m
//  bleAntiLost
//
//  Created by china topflytech on 2024/6/14.
//
// QRCodeScannerViewController.m
// QRCodeScannerViewController.m
#import "QRCodeScannerViewController.h"

@interface QRCodeScannerViewController ()
@property (nonatomic, strong) AVCaptureSession *captureSession;
@property (nonatomic, strong) AVCaptureVideoPreviewLayer *videoPreviewLayer;
@end

@implementation QRCodeScannerViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor blackColor];
    [self setupCaptureSession];
    [self startCaptureSession];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.captureSession stopRunning];
}

- (void)setupCaptureSession {
    NSError *error;
    self.captureSession = [[AVCaptureSession alloc] init];
    
    // 设置输入设备
    AVCaptureDevice *videoCaptureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    AVCaptureDeviceInput *videoInput = [AVCaptureDeviceInput deviceInputWithDevice:videoCaptureDevice error:&error];
    
    if (videoInput) {
        [self.captureSession addInput:videoInput];
    } else {
        NSLog(@"Error: %@", error);
        return;
    }
    
    // 设置输出设备
    AVCaptureMetadataOutput *metadataOutput = [[AVCaptureMetadataOutput alloc] init];
    [self.captureSession addOutput:metadataOutput];
    
    [metadataOutput setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
    [metadataOutput setMetadataObjectTypes:@[AVMetadataObjectTypeQRCode]];
    
    // 添加视频预览层
    self.videoPreviewLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.captureSession];
    self.videoPreviewLayer.frame = self.view.layer.bounds;
    self.videoPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    [self.view.layer addSublayer:self.videoPreviewLayer];
}

- (void)startCaptureSession {
    // 使用后台队列启动 AVCaptureSession
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
        [self.captureSession startRunning];
    });
}

// 处理扫描到的二维码
- (void)captureOutput:(AVCaptureOutput *)output didOutputMetadataObjects:(NSArray<__kindof AVMetadataObject *> *)metadataObjects fromConnection:(AVCaptureConnection *)connection {
    if (metadataObjects.count == 0) {
        return;
    }
    
    AVMetadataMachineReadableCodeObject *metadataObject = [metadataObjects objectAtIndex:0];
    if ([metadataObject.type isEqualToString:AVMetadataObjectTypeQRCode]) {
        NSString *qrCode = metadataObject.stringValue;
        NSLog(@"Scanned QR Code: %@", qrCode);
        
        // 停止扫描
        [self.captureSession stopRunning];
        
        // 使用主线程调用代理方法
        dispatch_async(dispatch_get_main_queue(), ^{
            if ([self.delegate respondsToSelector:@selector(didFindQRCode:)]) {
                [self.delegate didFindQRCode:qrCode];
            }
            [self dismissViewControllerAnimated:YES completion:nil];
        });
    }
}

@end
