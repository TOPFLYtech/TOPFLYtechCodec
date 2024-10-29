//
//  Utils.h
//  HBuilder-Hello
//
//  Created by china topflytech on 2024/3/19.
//  Copyright © 2024 DCloud. All rights reserved.
//

#ifndef Utils_h
#define Utils_h
@interface Utils : NSObject

+ (BOOL)isDebug;
+ (CGFloat)fontSize;

+ (NSString *)parseHardwareVersion:(NSString *)hardware;

+ (NSArray<NSNumber *> *)hexStringToUInt8Array:(NSString *)hexString;
+ (NSString *)bytes2HexString:(NSData *)data startingAtIndex:(NSUInteger)index;
+ (NSString *)uint8ToHexString:(uint8_t)value;

+ (float)getCurTempWithSourceTemp:(float)sourceTemp;
+ (float)getSourceTempWithSourceTemp:(float)sourceTemp;
+ (NSString *)getCurTempUnit;

+ (float)getCurPressureWithSourcePressure:(float)sourcePressure;
+ (NSString *)getCurPressureUnit;

+ (int32_t)bytes2Integer:(const uint8_t *)bytes withOffset:(NSUInteger)offset; 

+ (BOOL)isHexadecimal:(NSString *)string;
 
+ (int16_t)data2Short:(NSData *)data atOffset:(int)offset;
 
+ (int)characterToInt:(unichar)value;
+(int) calCrc:(uint8_t *)calArray len:(int) len;
+(NSData*) getInterActiveCmd:(NSString*) pwd  cmdHead:(NSInteger) cmdHead  content:(NSData*)  content;

@end

#endif /* Utils_h */
