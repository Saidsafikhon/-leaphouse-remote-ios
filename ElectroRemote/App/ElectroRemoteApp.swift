import SwiftUI

@main
struct ElectroRemoteApp: App {
    @StateObject private var vm = CarViewModel()

    var body: some Scene {
        WindowGroup {
            RootView(vm: vm)
        }
    }
}

/// Ворота приложения: вход и его ответвления, ожидание парка, привязка, управление.
/// Ворота считаются из состояния, а не хранятся отдельно: сессия живёт в
/// настройках и переживает перезапуск.
struct RootView: View {
    @Environment(\.colorScheme) private var scheme
    @ObservedObject var vm: CarViewModel

    private enum Branch { case register, forgot }
    @State private var branch: Branch? = nil

    var body: some View {
        let palette: ElectroPalette = scheme == .dark ? .dark : .light
        Group {
            if !vm.loggedIn {
                switch branch {
                case .register:
                    RegisterScreen(vm: vm, onRegistered: { branch = nil }, onBack: { branch = nil })
                case .forgot:
                    ForgotPasswordScreen(vm: vm, onBack: { branch = nil })
                case nil:
                    LoginScreen(vm: vm, onLoggedIn: { branch = nil }, onRegister: { branch = .register }, onForgot: { branch = .forgot })
                }
            } else if !vm.parkKnown {
                // Пустой список до ответа сервера ещё ничего не значит.
                ParkGateScreen(note: vm.parkNote, onRetry: { vm.loadVehicles() }, onLogout: { vm.logout() })
            } else if vm.vehicles.isEmpty {
                // Управление доступно только тем, у кого есть привязанная машина.
                PairScreen(vm: vm, onPaired: { branch = nil })
            } else {
                PhoneControlScreen(vm: vm)
            }
        }
        .environment(\.palette, palette)
        .background(palette.background.ignoresSafeArea())
        .preferredColorScheme(nil)
        .onChange(of: vm.loggedIn) { _, on in if on { branch = nil } }
    }
}
