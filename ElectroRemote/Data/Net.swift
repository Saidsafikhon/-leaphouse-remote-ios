import Foundation

/// Убрать из строки любые адреса: URL, домены, IP[:порт]. Адрес нашего сайта и
/// IP сервера не должны утекать на экран через тексты ошибок.
func scrubAddresses(_ text: String?) -> String? {
    guard let text, !text.trimmingCharacters(in: .whitespaces).isEmpty else { return text }
    var s = text
    func rep(_ pattern: String, _ with: String, _ opts: NSRegularExpression.Options = [.caseInsensitive]) {
        guard let re = try? NSRegularExpression(pattern: pattern, options: opts) else { return }
        s = re.stringByReplacingMatches(in: s, range: NSRange(s.startIndex..., in: s), withTemplate: with)
    }
    rep("https?://\\S+", "сервер")
    rep("\\b[a-z0-9-]+(?:\\.[a-z0-9-]+)+\\.[a-z]{2,}\\b", "сервер")
    rep("\\b[a-z0-9-]+\\.[a-z]{2,}\\b", "сервер")
    rep("/?\\b(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d+)?\\b", "сервер", [])
    rep("(?:сервер[\\s/:]+){1,}сервер", "сервер")
    rep("\\s+", " ", [])
    return s.trimmingCharacters(in: .whitespaces)
}

/// Обобщённая причина сетевого сбоя без адресов. nil — это не про сеть.
func friendlyNetworkError(_ error: Error) -> String? {
    if let e = error as? URLError {
        switch e.code {
        case .cannotFindHost, .dnsLookupFailed, .notConnectedToInternet, .networkConnectionLost:
            return "Нет связи с сервером — проверьте интернет"
        case .timedOut:
            return "Сервер не ответил вовремя"
        case .secureConnectionFailed, .serverCertificateUntrusted, .serverCertificateHasBadDate,
             .serverCertificateHasUnknownRoot, .serverCertificateNotYetValid, .clientCertificateRejected:
            return "Не удалось установить защищённое соединение"
        case .cannotConnectToHost, .cannotLoadFromNetwork:
            return "Сервер недоступен"
        default:
            return "Сервер недоступен"
        }
    }
    return nil
}
