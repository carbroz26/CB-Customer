import UIKit
import CBPartnerApp

@main
final class CBPartnerAppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        let window = UIWindow(frame: UIScreen.main.bounds)
        window.rootViewController = MainKt.MainViewController()
        window.makeKeyAndVisible()
        self.window = window
        return true
    }
}
