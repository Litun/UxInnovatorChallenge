import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let token = Bundle.main.object(forInfoDictionaryKey: "GO_REST_TOKEN") as? String ?? ""
        return MainViewControllerKt.MainViewController(goRestToken: token)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
