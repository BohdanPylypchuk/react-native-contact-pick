#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(ContactPick, NSObject)

RCT_EXTERN_METHOD(pickContact: (NSDictionary)options
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

@end
