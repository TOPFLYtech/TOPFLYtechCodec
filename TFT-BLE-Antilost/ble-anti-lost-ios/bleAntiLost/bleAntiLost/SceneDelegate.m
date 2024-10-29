//
//  SceneDelegate.m
//  bleAhtiLost
//
//  Created by china topflytech on 2024/4/11.
//

#import "SceneDelegate.h"
#import "ViewController.h"
@interface SceneDelegate ()

@end

@implementation SceneDelegate


- (void)scene:(UIScene *)scene willConnectToSession:(UISceneSession *)session options:(UISceneConnectionOptions *)connectionOptions {
    // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
    // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
    // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
    // 创建自定义视图控制器实例
    ViewController *viewController = [[ViewController alloc] init]; 
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:viewController];
    // 检查导航栏是否隐藏
    BOOL isHidden = navigationController.navigationBarHidden;

    // 显示导航栏
    [navigationController setNavigationBarHidden:NO animated:YES];
    // 获取或创建 UIWindow 对象
    if (@available(iOS 13.0, *)) {
        // iOS 13 及以上版本
        self.window = [[UIWindow alloc] initWithWindowScene:(UIWindowScene *)scene];
    } else {
        // iOS 12 及以下版本，保持对老版本的支持
        self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    }

    // 设置视图控制器为根视图控制器
    self.window.rootViewController = navigationController;

    // 显示窗口
    [self.window makeKeyAndVisible];
}


- (void)sceneDidDisconnect:(UIScene *)scene {
    // Called as the scene is being released by the system.
    // This occurs shortly after the scene enters the background, or when its session is discarded.
    // Release any resources associated with this scene that can be re-created the next time the scene connects.
    // The scene may re-connect later, as its session was not necessarily discarded (see `application:didDiscardSceneSessions` instead).
}


- (void)sceneDidBecomeActive:(UIScene *)scene {
    // Called when the scene has moved from an inactive state to an active state.
    // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
}


- (void)sceneWillResignActive:(UIScene *)scene {
    // Called when the scene will move from an active state to an inactive state.
    // This may occur due to temporary interruptions (ex. an incoming phone call).
}


- (void)sceneWillEnterForeground:(UIScene *)scene {
    // Called as the scene transitions from the background to the foreground.
    // Use this method to undo the changes made on entering the background.
}


- (void)sceneDidEnterBackground:(UIScene *)scene {
    // Called as the scene transitions from the foreground to the background.
    // Use this method to save data, release shared resources, and store enough scene-specific state information
    // to restore the scene back to its current state.
}


@end
