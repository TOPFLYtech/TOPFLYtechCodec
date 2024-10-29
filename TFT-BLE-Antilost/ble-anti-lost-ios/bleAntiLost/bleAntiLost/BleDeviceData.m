//
//  BleDeviceData.m
//  HBuilder
//
//  Created by china topflytech on 2024/3/19.
//  Copyright © 2024 DCloud. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BleDeviceData.h"


@implementation BleDeviceData
NSString * const MODEL_KNIGHTX_100 = @"KnightX 100";
NSString * const MODEL_KNIGHTX_300 = @"KnightX 300";
NSString * const MODEL_V0V_X10 = @"VOV X10";
+ (NSString *)parseProtocolWithType:(UInt8)protocolType {
    if (protocolType == 0x44 || protocolType == 0x45 || protocolType == 0x46) {
        return @"tc008";
    } else if (protocolType == 0x4d) {
        return @"tc009";
    } else if (protocolType == 0x52) {
        return @"tc010";
    } else if (protocolType == 0x58) {
        return @"tc011";
    } else {
        return @"";
    }
}

+ (NSString *)parseModelWithProtocolByte:(UInt8)protocolByte {
    switch (protocolByte) {
            case 0x44:
                return @"TLW2-12BL";
            case 0x4D:
                return @"TLP2-SFB";
            case 0x62:
                return @"tc015";
            case 0x68:
                return MODEL_KNIGHTX_100;
            case 0x6A:
                return MODEL_KNIGHTX_300;
            case 0x75:
                return MODEL_V0V_X10;
            default:
                return @"";
        }
}

+ (NSDictionary<NSNumber *, NSArray<NSNumber *> *> *)parseRawData:(NSArray<NSNumber *> *)rawData {
    NSMutableDictionary<NSNumber *, NSArray<NSNumber *> *> *result = [NSMutableDictionary dictionary];
    NSInteger index = 0;
    while (index < rawData.count) {
        NSInteger len = [rawData[index] integerValue];
        if (index + len + 1 <= rawData.count && len > 2) {
            UInt8 type = [rawData[index+1] unsignedCharValue];
            NSMutableArray<NSNumber *> *itemData = [NSMutableArray array];
            NSRange range = NSMakeRange(index + 2, len - 1);
            for (NSInteger i = range.location; i < NSMaxRange(range); i++) {
                [itemData addObject:rawData[i]];
            }
            
            if (type == 0x16) {
                if ([itemData[0] unsignedCharValue] == 0xaf && [itemData[1] unsignedCharValue] == 0xde) {
                    result[@(0xfe)] = itemData;
                } else if ([itemData[0] unsignedCharValue] == 0xaf && [itemData[1] unsignedCharValue] == 0xbe) {
                    result[@(0xfd)] = itemData;
                }
            } else {
                result[@(type)] = itemData;
            }
        }
        index += len + 1;
    }
    return result;
}

@end
