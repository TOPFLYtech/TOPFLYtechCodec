//
//  AppDelegate.swift
//  tftble
//
//  Created by jeech on 2019/12/11.
//  Copyright © 2019 com.tftiot. All rights reserved.
//

import UIKit


@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    
        // Override point for customization after application launch.
//        UITabBar.appearance().tintColor = UIColor.orange
//        UIApplication.shared.statusBarStyle = UIStatusBarStyle.lightContent;
            // 创建window
        let screen = UIScreen.main.bounds
        self.window = UIWindow.init(frame: screen)
        let viewController = ViewController()
//        let viewController = AlarmOpenSetViewController()
        let navigationController = UINavigationController(rootViewController: viewController)
        self.window?.rootViewController = navigationController
        self.window?.makeKeyAndVisible()
        if #available(iOS 15.0, *) {
           let navBarAppearance = UINavigationBarAppearance()
            // 背景色
            navBarAppearance.backgroundColor = UIColor.colorPrimary
            // 去掉半透明效果
            navBarAppearance.backgroundEffect = nil
            // 去除导航栏阴影（如果不设置clear，导航栏底下会有一条阴影线）
            navBarAppearance.shadowColor = UIColor.clear
            // 字体颜色
            navBarAppearance.titleTextAttributes = [NSAttributedString.Key.foregroundColor: UIColor.black]
            UINavigationBar.appearance().scrollEdgeAppearance = navBarAppearance
           UINavigationBar.appearance().standardAppearance = navBarAppearance // 常规页面。描述导航栏以标准高度
        }else{
           print("is not ios 15")
       }
        return true
    }

    // MARK: UISceneSession Lifecycle

//    @available(iOS 13.0, *)
//    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
//        // Called when a new scene session is being created.
//        // Use this method to select a configuration to create the new scene with.
//        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
//    }
//
//    @available(iOS 13.0, *)
//    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
//        // Called when the user discards a scene session.
//        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
//        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
//    }


}

