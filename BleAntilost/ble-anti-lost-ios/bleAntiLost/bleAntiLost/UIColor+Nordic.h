//
//  UIColor.m
//  bleAntiLost
//
//  Created by china topflytech on 2024/4/15.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface UIColor (DynamicColor)

@end

@implementation UIColor (DynamicColor)


+ (UIColor *)nordicBlue {
    return [UIColor colorWithRed:0.0 green:0.7181802392 blue:0.8448022008 alpha:1.0];
}

+ (UIColor *)nordicLake {
    return [UIColor colorWithRed:0.0 green:0.5483048558 blue:0.8252354264 alpha:1.0];
}

+ (UIColor *)nordicRed {
    return [UIColor colorWithRed:0.9567440152 green:0.2853084803 blue:0.3770255744 alpha:1.0];
}

+ (UIColor *)nordicRedDark {
    return [UIColor colorWithRed:0.8138422955 green:0.24269408 blue:0.3188471754 alpha:1.0];
}

+ (UIColor *)nordicSun {
    return [UIColor colorWithRed:1.0 green:0.8319787979 blue:0.0 alpha:1.0];
}

+ (UIColor *)nordicGrass {
    return [UIColor colorWithRed:0.8486783504 green:0.8850693107 blue:0.0 alpha:1.0];
}

+ (UIColor *)nordicFall {
    return [UIColor colorWithRed:0.9759542346 green:0.5849055648 blue:0.2069504261 alpha:1.0];
}

+ (UIColor *)darkNordicFall {
    return [UIColor colorWithRed:0.4599588935 green:0.2726793679 blue:0.09748239234 alpha:1.0];
}

+ (UIColor *)nordicDarkGray {
    return [UIColor colorWithRed:0.2590435743 green:0.3151275516 blue:0.353839159 alpha:1.0];
}

+ (UIColor *)nordicMediumGray {
    return [UIColor colorWithRed:0.5353743434 green:0.5965531468 blue:0.6396299005 alpha:1.0];
}

+ (UIColor *)nordicLightGray {
    return [UIColor colorWithRed:0.8790807724 green:0.9051030278 blue:0.9087315202 alpha:1.0];
}

+ (UIColor *)almostWhite {
    return [UIColor colorWithRed:0.8374180198 green:0.8374378085 blue:0.8374271393 alpha:1.0];
}

+ (UIColor *)colorPrimary {
    return [UIColor colorWithRed:24.0/255.0 green:194.0/255.0 blue:214.0/255.0 alpha:1.0];
}

+ (UIColor *)colorPrimaryDark {
    return [UIColor colorWithRed:26.0/255.0 green:165.0/255.0 blue:181.0/255.0 alpha:1.0];
}

+ (UIColor *)tableViewSeparator {
    return [UIColor colorWithRed:0.8243665099 green:0.8215891719 blue:0.8374734521 alpha:1.0];
}

+ (UIColor *)tableViewBackground {
    return [UIColor colorWithRed:0.9499699473 green:0.9504894614 blue:0.965736568 alpha:1.0];
}

@end
