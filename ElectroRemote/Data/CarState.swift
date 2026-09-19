import Foundation

/// Канал, по которому сейчас доступна машина.
enum CarLink { case none, cloud }

struct GeoPoint: Equatable {
    let lat: Double
    let lon: Double
    var bearing: Double? = nil
}

struct Doors: Equatable {
    var frontLeft = false
    var frontRight = false
    var rearLeft = false
    var rearRight = false
    var anyOpen: Bool { frontLeft || frontRight || rearLeft || rearRight }
}

/// Охрана. Отдельно от замков: «заперто» не значит «на охране».
enum Security { case armed, disarmed, unknown }

/// Разобранное состояние машины — зеркало `CarState.kt`.
struct CarState: Equatable {
    var link: CarLink = .none
    var raw: [String: String] = [:]
    var security: Security = .unknown
    var vin: String? = nil
    var soc: Int? = nil
    var rangeKm: Int? = nil
    var locked: Bool? = nil
    var trunkOpen: Bool? = nil
    var hoodOpen: Bool = false
    var doors = Doors()
    var cabinTemp: Double? = nil
    var outsideTemp: Double? = nil
    var setTempLeft: Int? = nil
    var setTempRight: Int? = nil
    var fan: Int? = nil
    var acOn: Bool? = nil
    var odometerKm: Int? = nil
    var speedKmh: Int? = nil
    var chargeMinutes: Int? = nil
    var location: GeoPoint? = nil
    var climateAutoOffMinutes: Int = 0
    /// Время снимка, мс от эпохи; 0 — ещё не снимали.
    var updatedAt: Int64 = 0

    var online: Bool { link != .none }

    /// Значение сигнала по имени; ключи головы приходят как "имя(id)".
    func signal(_ name: String) -> String? { raw.sig(name) }

    static func fromCloud(_ dto: StatusDto, now: Int64) -> CarState {
        var raw: [String: String] = [:]
        for (k, v) in dto.raw ?? [:] {
            let t = v.text
            if !t.isEmpty && t != "null" { raw[k] = t }
        }
        func num(_ n: String) -> Double? { raw.sig(n).flatMap { Double($0) } }
        func flag(_ n: String) -> Bool? { raw.sig(n).map { $0 != "0" } }

        var s = CarState()
        s.link = dto.online ? .cloud : .none
        s.raw = raw
        switch dto.security_state {
        case "ARMED": s.security = .armed
        case "DISARMED": s.security = .disarmed
        default: s.security = .unknown
        }
        s.soc = dto.battery_percent ?? num("soc").map { Int($0) }
        s.rangeKm = dto.range_km ?? num("range_ev").map { Int($0) }
        s.odometerKm = dto.odometer_km ?? num("odometer").map { Int($0) }
        switch dto.doors {
        case "LOCKED": s.locked = true
        case "UNLOCKED": s.locked = false
        default: s.locked = flag("lock")
        }
        s.trunkOpen = flag("trunk")
        s.acOn = dto.climate_on
        s.cabinTemp = num("cabin_temp")
        s.outsideTemp = num("outside_temp")
        s.setTempLeft = num("temp_l").map { Int($0) }
        s.setTempRight = num("temp_r").map { Int($0) }
        s.fan = num("fan").map { Int($0) }
        s.speedKmh = num("speed").map { Int($0) }
        s.chargeMinutes = num("charge_time").map { Int($0) }
        if let lat = dto.latitude, let lon = dto.longitude { s.location = GeoPoint(lat: lat, lon: lon) }
        s.climateAutoOffMinutes = dto.climate_auto_off_minutes ?? 0
        s.updatedAt = now
        return s
    }
}

extension Double {
    /// Температура для показа: целые — без дробной части («24», не «24.0»).
    var asTemp: String {
        self == self.rounded() ? String(Int(self)) : String(self)
    }
}

extension Dictionary where Key == String, Value == String {
    /// Ключи головы приходят как "имя(id)"; пустые значения считаем отсутствующими.
    func sig(_ name: String) -> String? {
        let v = self[name] ?? self.first(where: { $0.key.hasPrefix(name + "(") })?.value
        guard let v, !v.isEmpty, v != "null" else { return nil }
        return v
    }
}

func nowMillis() -> Int64 { Int64(Date().timeIntervalSince1970 * 1000) }
