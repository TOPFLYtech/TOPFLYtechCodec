//
//  TFTBleManager.h
//  HBuilder-Hello
//
//  Created by china topflytech on 2024/3/18.
//  Copyright © 2024 DCloud. All rights reserved.
//

#ifndef TFTBleManager_h
#define TFTBleManager_h
#import <CoreBluetooth/CoreBluetooth.h>
#import <UserNotifications/UserNotifications.h>
//#import <AVFoundation/AVFoundation.h>
#import "BleDeviceData.h"
@interface TFTBleManager : NSObject <CBCentralManagerDelegate, CBPeripheralDelegate,UNUserNotificationCenterDelegate>
//@property (strong, nonatomic) AVPlayer *player;
@property (nonatomic, strong) NSMutableArray<UNNotificationRequest *> *pendingNotificationRequests;
@property (nonatomic, strong) CBCentralManager *centralManager; 
//@property (nonatomic, strong) CBPeripheral *connectedPeripheral;
@property (nonatomic, strong) NSMutableDictionary<NSString *, CBPeripheral *> *connectedPeripheralDict;
@property (nonatomic, strong) NSMutableDictionary<NSUUID *,NSString*> *bleIdImeiMap;
@property (nonatomic, strong) NSMutableDictionary<NSString *,NSNumber*> *imeiConnectStatusMap;
@property (nonatomic, strong) NSMutableDictionary<NSString *,NSDate*> *disconnectImeiMap;
@property (nonatomic, strong)NSMutableArray<NSString*> *breakConnectImeiArray;
@property (nonatomic, strong)NSMutableArray<CBPeripheral*> *lastConnectedPeripheralArray;
//@property (nonatomic, strong) CBPeripheral *lastConnectedPeripheral;
@property (nonatomic, strong) CBCharacteristic *characteristicToWrite;
@property (nonatomic, copy) void (^didReceiveDataCallback)(NSString*,NSData *);
@property (nonatomic, copy) void (^didReceiveBleStatus)(NSString*,int);
@property (nonatomic, copy) void (^didReceiveRssi)(NSString*,int);
@property (nonatomic, strong) CBUUID *serviceUUID;
@property (nonatomic, strong) CBUUID *characteristicUUID;
@property (nonatomic, strong) NSString *imei;
@property (nonatomic, strong) NSMutableArray<NSString *> *supportAntiLostModels;
@property Boolean isBleAvailable;
//@property Boolean connectSucc;
@property Boolean isScanning;
@property Boolean isConnectingBle;
@property Boolean isWaitConnect;
//@property NSString* curConnectImei;
@property Boolean isNeedConnectDevice;
@property BleDeviceData *connectBleDeviceData;
@property int warningDuration;
@property int warningCount;
@property Boolean needSound;
@property Boolean needShock;
@property Boolean isCurWarning; 
@property (nonatomic, strong) NSTimer *rssiTimer;
@property (nonatomic, strong) NSMutableDictionary<NSString *, NSMutableDictionary<NSString *, NSNumber *> *> *controlFunc;
@property (nonatomic, strong) NSMutableDictionary<NSString *, BleDeviceData *> *allDeviceMap;
extern const int BLE_STATUS_OF_CLOSE;
extern const int BLE_STATUS_OF_DISCONNECT;
extern const int BLE_STATUS_OF_CONNECTING;
extern const int BLE_STATUS_OF_CONNECT_SUCC;
extern const int BLE_STATUS_OF_SCANNING;
extern  NSString * const BLE_NOTIFY_VALUE;
- (instancetype)init;
- (void)startScanningForDevicesWithServiceUUID:(CBUUID *)serviceUUID;
//- (void)sendData:(NSData *)data;
- (void)setImei:(NSString *)imei;
//-(NSString*)getCurConnectImei;
- (void)startScanning;
- (void)stopScanning;
-(void)connect:(NSString*) imei;
-(void) disconnect:(NSString*) imei;
-(void) disconnectAll;
- (void)writeArrayDataWithCmdHead:(NSString*) imei cmdHead:(NSInteger)cmdHead content:(NSArray<NSNumber *> *)contentArray;
- (void)writeStrDataWithCmdHead:(NSString*) imei cmdHead:(NSInteger)cmdHead dataStr:(NSString *)dataStr;
- (void)antiLostConnConfig:(NSInteger)open;
- (void)setAntiLostBleStatus:(NSString*) imei twoWayAntiLost:(NSInteger)twoWayAntiLost singleVibrationDurationTime:(NSInteger)singleVibrationDurationTime repeatTime:(NSInteger)repeatTime ;
- (void)setAntiLostBleSearchMode:(NSString*) imei searchMode:(NSInteger)searchMode;
- (void)setAntiLostBleSilenceMode:(NSString*) imei silenceMode:(NSInteger)silenceMode;
- (int)getConnectStatus:(NSString*)imei;
- (void)startSendingPeriodicNotifications ;
+(void)requestNotificationPermission;
-(void) doActivelyDisconnectCb:(NSString *)imei; 
- (void)startVibrationLoopWithCount:(NSInteger)count;
-(void) connectDevice:(NSString*)imei;
-(NSString *) getCurConnectDeviceModel:(NSString *)imei;
-(void) saveNeedConnectDeviceToStore;
-(void) initNeedConnectDeviceFromStore;
@end

#endif /* TFTBleManager_h */
