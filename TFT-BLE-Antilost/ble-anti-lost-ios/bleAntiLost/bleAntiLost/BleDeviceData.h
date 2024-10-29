//
//  BleDeviceData.h
//  HBuilder-Hello
//
//  Created by china topflytech on 2024/3/19.
//  Copyright © 2024 DCloud. All rights reserved.
//

#ifndef BleDeviceData_h
#define BleDeviceData_h
#import <CoreBluetooth/CoreBluetooth.h>
@interface BleDeviceData : NSObject

@property (nonatomic, strong) NSString *deviceName;
@property (nonatomic, strong) NSString *mac;
@property (nonatomic, strong) NSString *imei;
@property NSInteger rssi;
@property (nonatomic, strong) NSString *date;
@property (nonatomic, strong) NSString *id;
@property (nonatomic, strong) NSString *protocolType;
@property (nonatomic, strong) NSString *model;
@property (nonatomic, strong) NSString *software;
@property (nonatomic, strong) NSString *hardware;
@property (nonatomic, strong) CBPeripheral * peripheral;
extern NSString * const MODEL_KNIGHTX_100;
extern NSString * const MODEL_KNIGHTX_300;
extern NSString * const MODEL_V0V_X10;
+ (NSString *)parseProtocolWithType:(UInt8)protocolType;
+ (NSString *)parseModelWithProtocolByte:(UInt8)protocolByte;
+ (NSDictionary<NSNumber *, NSArray<NSNumber *> *> *)parseRawData:(NSArray<NSNumber *> *)rawData;

@end
#endif /* BleDeviceData_h */
