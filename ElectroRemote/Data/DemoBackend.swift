import Foundation

/// Демо-режим для скриншотов App Store (`-screenshots` в аргументах запуска):
/// репозиторий не ходит в сеть и отдаёт заранее заготовленные данные.
/// В обычной сборке этот код не активируется.
enum Demo {
    static let enabled = ProcessInfo.processInfo.arguments.contains("-screenshots")

    static let vehicle = VehicleDto(vehicle_id: "veh-c16-demo", vin: "LFZ63AV53SD020502", name: "C16 *0502",
                                    provider: "simulator", remote_control_enabled: true, model: "C16")

    static func state() -> CarState {
        var s = CarState()
        s.link = .cloud
        s.security = .armed
        s.locked = true
        s.trunkOpen = false
        s.soc = 78
        s.rangeKm = 412
        s.odometerKm = 12480
        s.cabinTemp = 22
        s.outsideTemp = 27
        s.setTempLeft = 22
        s.setTempRight = 22
        s.location = GeoPoint(lat: 41.311081, lon: 69.240562, bearing: 135)
        s.raw = ["ac": "0", "temp_l": "22", "temp_r": "22", "fan": "3", "trunk": "0",
                 "window_fl": "0", "window_fr": "0", "window_rl": "0", "window_rr": "0",
                 "seat_vent_rl": "0", "seat_vent_rr": "0", "mirror_heat": "0"]
        s.updatedAt = nowMillis()
        return s
    }

    static let capabilities: CapabilitiesDto? = try? JSONDecoder().decode(
        CapabilitiesDto.self,
        from: Data(#"{"quick":["lock","trunk","climate","windows"],"groups":["climate","seats"],"voice":true,"scenes":true,"climate_schedule":true}"#.utf8)
    )

    static let scenes: [SceneTemplateDto] = (try? JSONDecoder().decode([SceneTemplateDto].self, from: Data(#"""
    [{"template_id":"s1","name":"Остудить к выходу","steps":[{"type":65537,"value":"1","title":"климат"},{"type":65574,"value":"20","title":"температура 20°"},{"type":65577,"value":"7","title":"обдув 7"}]},
     {"template_id":"s2","name":"Проветрить","steps":[{"type":196609,"value":"100","title":"окна открыть"},{"type":65545,"value":"1","title":"циркуляция"}]}]
    """#.utf8))) ?? []

    static let schedules: [ClimateScheduleDto] = (try? JSONDecoder().decode([ClimateScheduleDto].self, from: Data(#"""
    [{"schedule_id":"c1","hour":7,"minute":40,"weekdays":[0,1,2,3,4],"temp_c":22,"enabled":true}]
    """#.utf8))) ?? []

    static let voice: [VoiceIntentDto] = (try? JSONDecoder().decode([VoiceIntentDto].self, from: Data(#"""
    [{"intent":"ac_on","subsystem":"climate","phrases":["включи кондиционер","сделай прохладнее"]},
     {"intent":"windows_close","subsystem":"body","phrases":["закрой окна"]},
     {"intent":"trunk_open","subsystem":"body","phrases":["открой багажник"]}]
    """#.utf8))) ?? []

    static let news: [NewsItem] = (try? JSONDecoder().decode([NewsItem].self, from: Data(#"""
    [{"id":"n1","title":"Обновление LeapRemote","body":"Добавили профиль сидений с таймером и климат по расписанию.","kind":"news","created_at":"2026-09-18T09:30:00+00:00"},
     {"id":"n2","title":"Плановые работы","body":"В ночь на субботу сервер будет недоступен 10 минут.","kind":"alert","created_at":"2026-09-16T18:00:00+00:00"}]
    """#.utf8))) ?? []

    static let support: SupportDto? = try? JSONDecoder().decode(
        SupportDto.self, from: Data(#"{"phone":"+998 99 016-62-66","telegram":"@leapremote","site":"leapmotor.evon.uz"}"#.utf8)
    )
}
