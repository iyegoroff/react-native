#import <UIKit/UIKit.h>

@interface UIColor (Gradient)

+ (UIColor *)colorWithGradientStart:(NSArray<NSNumber *> *)start
                            withEnd:(NSArray<NSNumber *> *)end
                      withLocations:(NSArray<NSNumber *> *)locations
                          withFrame:(CGRect)frame
                          andColors:(NSArray<UIColor *> *)colors;

@end
