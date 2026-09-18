import SwiftUI
import MapKit

// MARK: - Сцены

/// Заготовки шагов: проверенные команды, из которых человек собирает сцену.
private struct Ingredient: Identifiable, Equatable {
    let title: String
    let step: SceneStepDto
    var id: String { title }
}

private let INGREDIENTS: [Ingredient] = [
    Ingredient(title: "Включить климат", step: SceneStepDto(type: Cmd.AC, value: "1", title: "климат")),
    Ingredient(title: "Выключить климат", step: SceneStepDto(type: Cmd.AC, value: "0", title: "климат выкл")),
    Ingredient(title: "Тепло, 24°", step: SceneStepDto(type: Cmd.TEMP_L, value: "24", title: "температура 24°")),
    Ingredient(title: "Прохладно, 20°", step: SceneStepDto(type: Cmd.TEMP_L, value: "20", title: "температура 20°")),
    Ingredient(title: "Обдув на максимум", step: SceneStepDto(type: Cmd.FAN, value: "7", title: "обдув 7")),
    Ingredient(title: "Открыть окна", step: SceneStepDto(type: Cmd.WINDOW_FL, value: "100", title: "окна открыть")),
    Ingredient(title: "Закрыть окна", step: SceneStepDto(type: Cmd.WINDOW_FL, value: "0", title: "окна закрыть")),
]

/// Пользовательские сцены: свой набор команд под одной кнопкой. Живут на сервере.
struct ScenesScreen: View {
    let scenes: [SceneTemplateDto]
    let onRun: (SceneTemplateDto) -> Void
    let onDelete: (SceneTemplateDto) -> Void
    let onCreate: (String, [SceneStepDto]) -> Void
    let onBack: () -> Void

    @State private var building = false

    var body: some View {
        ScreenScaffold(title: "Мои сцены", onBack: onBack) {
            if scenes.isEmpty && !building {
                EmptyNote(text: "Сцен пока нет. Соберите свою — например «остудить к выходу».")
            }
            ForEach(scenes) { scene in
                SceneRow(scene: scene, onRun: { onRun(scene) }, onDelete: { onDelete(scene) })
            }
            if building {
                SceneBuilder(onCancel: { building = false }, onSave: { name, steps in onCreate(name, steps); building = false })
            } else {
                ElectroButton(text: "Новая сцена", style: .secondary) { building = true }
            }
        }
    }
}

private struct SceneRow: View {
    @Environment(\.palette) private var p
    let scene: SceneTemplateDto
    let onRun: () -> Void
    let onDelete: () -> Void

    var body: some View {
        SectionCard(title: scene.name) {
            Text(scene.steps.map { $0.title.isEmpty ? "тип \($0.type)" : $0.title }.joined(separator: " · "))
                .font(ElectroType.caption).foregroundStyle(p.textMuted)
            HStack(spacing: Space.x2) {
                ControlTile(label: "Выполнить", icon: "play", state: .active, action: onRun)
                ControlTile(label: "Удалить", icon: "trash", action: onDelete)
            }
        }
    }
}

private struct SceneBuilder: View {
    @Environment(\.palette) private var p
    let onCancel: () -> Void
    let onSave: (String, [SceneStepDto]) -> Void

    @State private var name = ""
    @State private var chosen: [Ingredient] = []

    var body: some View {
        SectionCard(title: "Новая сцена") {
            TextField("Название", text: $name)
                .font(ElectroType.body).foregroundStyle(p.textPrimary).tint(p.accent)
                .padding(.horizontal, Space.x4).frame(height: 52)
                .background(p.surfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: Radius.sm, style: .continuous))
            Text("Шаги").font(ElectroType.caption).foregroundStyle(p.textSecondary)
            ForEach(INGREDIENTS) { ing in
                let on = chosen.contains(ing)
                Button {
                    if on { chosen.removeAll { $0 == ing } } else { chosen.append(ing) }
                } label: {
                    HStack {
                        Text(ing.title).font(ElectroType.body).foregroundStyle(on ? p.accent : p.textPrimary)
                        Spacer()
                        if on { Image(systemName: "plus").font(.system(size: 14)).foregroundStyle(p.accent) }
                    }
                    .padding(.vertical, 6)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
            }
            HStack(spacing: Space.x2) {
                ElectroButton(text: "Отмена", style: .ghost, action: onCancel)
                ElectroButton(
                    text: "Сохранить", style: .primary,
                    enabled: !name.trimmingCharacters(in: .whitespaces).isEmpty && !chosen.isEmpty
                ) { onSave(name.trimmingCharacters(in: .whitespaces), chosen.map { $0.step }) }
            }
        }
    }
}

// MARK: - Расписание климата

private let DAY_LABELS = ["Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"]

/// Расписание пред-климата: будильник живёт на сервере, тут — только редактор.
struct ScheduleScreen: View {
    let schedules: [ClimateScheduleDto]
    let onCreate: (ClimateScheduleRequest) -> Void
    let onToggle: (ClimateScheduleDto, Bool) -> Void
    let onDelete: (ClimateScheduleDto) -> Void
    let onBack: () -> Void

    @State private var adding = false

    var body: some View {
        ScreenScaffold(title: "Климат по расписанию", onBack: onBack) {
            if schedules.isEmpty && !adding {
                EmptyNote(text: "Расписаний нет. Задайте время — сервер прогреет или остудит салон к нему.")
            }
            ForEach(schedules) { s in
                ScheduleRow(s: s, onToggle: { on in onToggle(s, on) }, onDelete: { onDelete(s) })
            }
            if adding {
                ScheduleEditor(onCancel: { adding = false }, onSave: { req in onCreate(req); adding = false })
            } else {
                ElectroButton(text: "Новое расписание", style: .secondary) { adding = true }
            }
        }
    }
}

private struct ScheduleRow: View {
    @Environment(\.palette) private var p
    let s: ClimateScheduleDto
    let onToggle: (Bool) -> Void
    let onDelete: () -> Void

    var body: some View {
        SectionCard(title: String(format: "%02d:%02d", s.hour, s.minute)) {
            HStack(spacing: Space.x2) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(s.weekdays.isEmpty ? "Каждый день" : s.weekdays.sorted().map { DAY_LABELS[$0 % 7] }.joined(separator: " "))
                        .font(ElectroType.body).foregroundStyle(p.textPrimary)
                    Text("до \(s.temp_c)°").font(ElectroType.caption).foregroundStyle(p.textMuted)
                }
                Spacer()
                ElectroToggle(isOn: s.enabled, onChange: onToggle)
                Button(action: onDelete) {
                    Image(systemName: "trash").font(.system(size: 18)).foregroundStyle(p.textMuted).frame(width: 36, height: 36)
                }
                .buttonStyle(.plain)
            }
        }
    }
}

private struct ScheduleEditor: View {
    @Environment(\.palette) private var p
    let onCancel: () -> Void
    let onSave: (ClimateScheduleRequest) -> Void

    @State private var hour = 8
    @State private var minute = 0
    @State private var temp = 22
    @State private var days: Set<Int> = []

    var body: some View {
        SectionCard(title: "Новое расписание") {
            // Время: часы и минуты крупными ± — попасть пальцем в плюс проще, чем в поле.
            StepperRow(label: "Час", value: String(format: "%02d", hour)) { hour = ((hour + $0) % 24 + 24) % 24 }
            StepperRow(label: "Минуты", value: String(format: "%02d", minute)) { minute = ((minute + $0 * 5) % 60 + 60) % 60 }
            StepperRow(label: "Температура", value: "\(temp)°") { temp = min(max(temp + $0, Cmd.TEMP_MIN), Cmd.TEMP_MAX) }

            Text("Дни").font(ElectroType.caption).foregroundStyle(p.textSecondary)
            HStack(spacing: 6) {
                ForEach(0..<7, id: \.self) { i in
                    ControlChip(text: DAY_LABELS[i], selected: days.contains(i)) {
                        if days.contains(i) { days.remove(i) } else { days.insert(i) }
                    }
                }
            }
            if days.isEmpty {
                Text("Ни один день не выбран — сработает каждый день").font(ElectroType.caption).foregroundStyle(p.textMuted)
            }
            HStack(spacing: Space.x2) {
                ElectroButton(text: "Отмена", style: .ghost, action: onCancel)
                ElectroButton(text: "Сохранить", style: .primary) {
                    onSave(ClimateScheduleRequest(hour: hour, minute: minute, weekdays: days.sorted(), temp_c: temp, enabled: true))
                }
            }
        }
    }
}

private struct StepperRow: View {
    @Environment(\.palette) private var p
    let label: String
    let value: String
    let onStep: (Int) -> Void

    var body: some View {
        HStack {
            Text(label).font(ElectroType.body).foregroundStyle(p.textPrimary)
            Spacer()
            ControlChip(text: "−", width: 52) { onStep(-1) }
            Text(value).font(ElectroType.value).foregroundStyle(p.accent).padding(.horizontal, Space.x3)
            ControlChip(text: "+", width: 52) { onStep(1) }
        }
    }
}

// MARK: - Голос

/// Голосовые намерения штатного ассистента головы: список готовых намерений.
struct VoiceScreen: View {
    @Environment(\.palette) private var p
    let intents: [VoiceIntentDto]
    let onRun: (VoiceIntentDto) -> Void
    let onBack: () -> Void

    var body: some View {
        ScreenScaffold(title: "Голосовые команды", onBack: onBack) {
            if intents.isEmpty {
                EmptyNote(text: "У этой машины голосовых команд нет.")
            } else {
                SectionCard(title: "Скажите или нажмите") {
                    ForEach(intents) { intent in
                        Button { onRun(intent) } label: {
                            HStack(spacing: Space.x3) {
                                Image(systemName: "waveform").font(.system(size: 20)).foregroundStyle(p.accent)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(intent.phrases.first.map { $0.prefix(1).uppercased() + $0.dropFirst() } ?? intent.intent)
                                        .font(ElectroType.body).foregroundStyle(p.textPrimary)
                                    if intent.phrases.count > 1 {
                                        Text(intent.phrases.dropFirst().joined(separator: " · "))
                                            .font(ElectroType.caption).foregroundStyle(p.textMuted)
                                    }
                                }
                                Spacer()
                            }
                            .padding(.vertical, Space.x2)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }
}

// MARK: - Карта

/// Карта с локацией машины: MapKit вместо статичной картинки, «Маршрут» — в Apple Maps.
struct MapScreen: View {
    @Environment(\.palette) private var p
    let loc: GeoPoint?

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: Space.x2) {
                Text("Карта").font(ElectroType.headline).foregroundStyle(p.textPrimary)
                Text("· Leapmotor C16").font(ElectroType.body).foregroundStyle(p.textMuted)
                Spacer()
            }
            .padding(Space.x4)

            if let loc {
                HStack(spacing: Space.x3) {
                    Image(systemName: "location").font(.system(size: 20)).foregroundStyle(p.accent)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Положение автомобиля").font(ElectroType.caption).foregroundStyle(p.textMuted)
                        Text(String(format: "%.5f, %.5f", loc.lat, loc.lon)).font(ElectroType.body).foregroundStyle(p.textPrimary)
                        if let b = loc.bearing {
                            Text("Курс: \(compass(b)) \(Int(b))°").font(ElectroType.caption).foregroundStyle(p.textMuted)
                        }
                    }
                    Spacer()
                }
                .padding(Space.x3)
                .background(p.surface)
                .clipShape(RoundedRectangle(cornerRadius: Radius.md, style: .continuous))
                .padding(.horizontal, Space.x4)

                Spacer().frame(height: Space.x3)

                let coord = CLLocationCoordinate2D(latitude: loc.lat, longitude: loc.lon)
                Map(initialPosition: .region(MKCoordinateRegion(center: coord, latitudinalMeters: 600, longitudinalMeters: 600))) {
                    Marker("Leapmotor C16", systemImage: "car.fill", coordinate: coord).tint(p.accent)
                }
                .mapStyle(.standard)
                .clipShape(RoundedRectangle(cornerRadius: Radius.lg, style: .continuous))
                .padding(.horizontal, Space.x4)

                Spacer().frame(height: Space.x3)
                Button { openRoute(loc) } label: {
                    HStack(spacing: Space.x2) {
                        Image(systemName: "arrow.triangle.turn.up.right.diamond").font(.system(size: 18)).foregroundStyle(p.onAccent)
                        Text("Маршрут").font(ElectroType.body).foregroundStyle(p.onAccent)
                    }
                    .frame(maxWidth: .infinity).frame(height: ControlSize.button)
                    .background(p.accent)
                    .clipShape(RoundedRectangle(cornerRadius: Radius.sm, style: .continuous))
                }
                .buttonStyle(.plain)
                .padding(.horizontal, Space.x4)
                Spacer().frame(height: Space.x4)
            } else {
                Spacer()
                VStack(spacing: Space.x1) {
                    Image(systemName: "location").font(.system(size: 36)).foregroundStyle(p.textMuted)
                    Spacer().frame(height: Space.x3)
                    Text("Нет координат").font(ElectroType.body).foregroundStyle(p.textSecondary)
                    Text("Положение приходит с головы. Разбудите машину и дождитесь связи.")
                        .font(ElectroType.caption).foregroundStyle(p.textMuted).multilineTextAlignment(.center)
                        .padding(.horizontal, Space.x6)
                }
                Spacer()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(p.background)
    }

    /// Открыть точку авто в Apple Maps (маршрут «как доехать»).
    private func openRoute(_ point: GeoPoint) {
        let coord = CLLocationCoordinate2D(latitude: point.lat, longitude: point.lon)
        let item = MKMapItem(placemark: MKPlacemark(coordinate: coord))
        item.name = "Leapmotor C16"
        item.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving])
    }
}

private func compass(_ bearing: Double) -> String {
    let dirs = ["С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ"]
    let normalized = (bearing.truncatingRemainder(dividingBy: 360) + 360).truncatingRemainder(dividingBy: 360)
    return dirs[Int((normalized + 22.5) / 45) % 8]
}
