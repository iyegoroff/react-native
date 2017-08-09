#import "UIColor+Gradient.h"
#import <objc/runtime.h>

@implementation UIColor (Gradient)

+ (void)setGradientImage:(UIImage *)gradientImage
{
  objc_setAssociatedObject(self, @selector(gradientImage), gradientImage, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

+ (UIImage *)gradientImage
{
  return objc_getAssociatedObject(self, @selector(gradientImage));
}

+ (UIColor *)colorWithGradientStart:(NSArray<NSNumber *> *)start
                            withEnd:(NSArray<NSNumber *> *)end
                      withLocations:(NSArray<NSNumber *> *)locations
                          withFrame:(CGRect)frame
                          andColors:(NSArray<UIColor *> *)colors
{
  CAGradientLayer *backgroundGradientLayer = [CAGradientLayer layer];
  
  backgroundGradientLayer.frame = frame;
  
  NSMutableArray *cgColors = [[NSMutableArray alloc] init];
  for (UIColor *color in colors) {
    [cgColors addObject:(id)[color CGColor]];
  }
  
  backgroundGradientLayer.colors = cgColors;
  backgroundGradientLayer.locations = locations;
  
  [backgroundGradientLayer setStartPoint:CGPointMake(start[0].floatValue, start[1].floatValue)];
  [backgroundGradientLayer setEndPoint:CGPointMake(end[0].floatValue, end[1].floatValue)];
  
  UIGraphicsBeginImageContextWithOptions(backgroundGradientLayer.bounds.size, NO, [UIScreen mainScreen].scale);
  [backgroundGradientLayer renderInContext:UIGraphicsGetCurrentContext()];
  UIImage *backgroundColorImage = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();
  
  [self setGradientImage:backgroundColorImage];
  
  return [UIColor colorWithPatternImage:backgroundColorImage];
}

@end
