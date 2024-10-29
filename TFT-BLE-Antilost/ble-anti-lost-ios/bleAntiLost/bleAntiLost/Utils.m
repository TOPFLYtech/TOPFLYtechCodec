//
//  Utils.m
//  HBuilder
//
//  Created by china topflytech on 2024/3/19.
//  Copyright © 2024 DCloud. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Utils.h"
	
@implementation Utils

+ (BOOL)isDebug {
    static BOOL isDebug = NO;
    return isDebug;
}

+ (CGFloat)fontSize {
    static CGFloat fontSize = 12.0;
    return fontSize;
}

+ (NSString *)parseHardwareVersion:(NSString *)hardware {
    double hardwareF = [hardware doubleValue];
    if (hardwareF > 10.0) {
        double result = hardwareF / 10.0;
        return [NSString stringWithFormat:@"V%.1f", result];
    } else {
        double result = ((hardwareF - 1) + 10) / 10;
        return [NSString stringWithFormat:@"V%.1f", result];
    }
}

+ (NSArray<NSNumber *> *)hexStringToUInt8Array:(NSString *)hexString {
    hexString = [hexString stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    if ([hexString hasPrefix:@"0x"] || [hexString hasPrefix:@"0X"]) {
        hexString = [hexString substringFromIndex:2];
    }

    if (hexString.length % 2 != 0) {
        NSLog(@"Invalid hex string. Length must be even.");
        return nil;
    }

    NSMutableArray<NSNumber *> *byteArray = [NSMutableArray arrayWithCapacity:hexString.length / 2];
    for (NSUInteger i = 0; i < hexString.length; i += 2) {
        NSRange range = NSMakeRange(i, 2);
        NSString *byteString = [hexString substringWithRange:range];
        NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
        formatter.numberStyle = NSNumberFormatterDecimalStyle;
        NSNumber *byteValue = [formatter numberFromString:byteString];
        if (byteValue) {
            [byteArray addObject:byteValue];
        } else {
            NSLog(@"Invalid hex string. Contains non-hexadecimal characters.");
            return nil;
        }
    }
    return byteArray;
}

+ (NSString *)bytes2HexString:(NSData *)data startingAtIndex:(NSUInteger)index {
    if (data == nil || [data length] <= index) {
        return nil;
    }

    const uint8_t *rawBytes = (const uint8_t *)[data bytes];
    NSUInteger remainingLength = [data length] - index;
    NSMutableString *builder = [NSMutableString stringWithCapacity:remainingLength];

    for (NSUInteger i = index; i < index + remainingLength; ++i) {
        NSString *hex = [NSString stringWithFormat:@"%02x", rawBytes[i]];
        [builder appendString:hex];
    }
    return [builder copy];
}

+ (NSString *)uint8ToHexString:(uint8_t)value {
    return [NSString stringWithFormat:@"%02X", value].uppercaseString;
}

+ (float)getCurTempWithSourceTemp:(float)sourceTemp {
    if (sourceTemp == -999.0f) {
        return sourceTemp;
    }
    
    NSInteger tempUnit = [[NSUserDefaults standardUserDefaults] integerForKey:@"tempUnit"];
    if (tempUnit == 0) {
        return sourceTemp;
    } else {
        return 32.0f + sourceTemp * 1.8f;
    }
}

+ (float)getSourceTempWithSourceTemp:(float)sourceTemp {
    if (sourceTemp == -999.0f) {
        return sourceTemp;
    }
    
    NSInteger tempUnit = [[NSUserDefaults standardUserDefaults] integerForKey:@"tempUnit"];
    if (tempUnit == 0) {
        return sourceTemp;
    } else {
        return (sourceTemp - 32.0f) / 1.8f;
    }
}
+ (NSString *)getCurTempUnit {
    NSInteger tempUnit = [[NSUserDefaults standardUserDefaults] integerForKey:@"tempUnit"];
    if (tempUnit == 0) {
        return @"\u00B0C"; // Degree Celsius symbol
    } else {
        return @"\u00B0F"; // Degree Fahrenheit symbol
    }
}

+ (float)getCurPressureWithSourcePressure:(float)sourcePressure {
    NSInteger pressureUnit = [[NSUserDefaults standardUserDefaults] integerForKey:@"pressureUnit"];
    if (pressureUnit == 0) {
        return sourcePressure;
    } else {
        return sourcePressure / 0.1450377f;
    }
}

+ (NSString *)getCurPressureUnit {
    NSInteger pressureUnit = [[NSUserDefaults standardUserDefaults] integerForKey:@"pressureUnit"];
    if (pressureUnit == 0) {
        return @"Kpa";
    } else {
        return @"Psi";
    }
}

+ (int32_t)bytes2Integer:(const uint8_t *)bytes withOffset:(NSUInteger)offset {
    NSUInteger byteSize = sizeof(int32_t);
    if (offset + byteSize > sizeof(bytes)) {
        return 0; // 或者抛出异常，取决于你的错误处理策略
    }
    NSData *data = [NSData dataWithBytes:bytes + offset length:byteSize];
    const uint8_t *dataBytes = (const uint8_t *)[data bytes];
    return CFSwapInt32BigToHost(*(const int32_t *)dataBytes);
}

 
+ (BOOL)isHexadecimal:(NSString *)string {
    NSError *error = NULL;
    NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:@"^[0-9a-fA-F]+$" options:0 error:&error];
    if (!regex) {
        NSLog(@"Error creating regex: %@", error);
        return NO;
    }
    NSRange range = NSMakeRange(0, [string length]);
    NSTextCheckingResult *match = [regex firstMatchInString:string options:0 range:range];
    return match != nil;
}

 

+ (int16_t)data2Short:(NSData *)bytes atIndex:(NSInteger)offset {
    if (bytes.length > 2 && offset + 1 < bytes.length) {
        const uint8_t *bytePtr = [bytes bytes];
        return (int16_t)((bytePtr[offset] << 8) + bytePtr[offset + 1]);
    }
    return 0;
}

 

+ (int)characterToInt:(unichar)value {
    return (int)value;
}
 
 
 
 

+ (NSArray<NSNumber *> *)getInterActiveCmd:(NSString *)pwd cmdHead:(NSInteger)cmdHead content:(NSArray<NSNumber *> *)content {
    NSMutableArray<NSNumber *> *result = [NSMutableArray array];
       
       const char *pwdUTF8String = [pwd UTF8String];
       for (int i = 0; i < strlen(pwdUTF8String); i++) {
           [result addObject:@(pwdUTF8String[i])];
       }
       
       [result addObject:@(cmdHead)];
       
       for (NSNumber *num in content) {
           [result addObject:num];
       }
       
       NSInteger crcLen = pwd.length + 1 + content.count;
       UInt8 crcByte = [self calCrcWithCalArray:result len:crcLen];
       [result addObject:@(crcByte)];
       
       return result;
}

+ (UInt8)calCrcWithCalArray:(NSArray<NSNumber *> *)calArray len:(NSInteger)len {
    UInt8 crc = 0xff;
    NSInteger i = 0;
    NSInteger j = 0;
    
    while (j < len) {
        crc ^= [calArray[j] unsignedCharValue];
        i = 0;
        while (i < 8) {
            if (crc & 0x80) {
                crc = (crc << 1) ^ 0x31;
            } else {
                crc <<= 1;
            }
            i += 1;
        }
        j += 1;
    }
    
    return crc & 0xff;
}

  
@end
