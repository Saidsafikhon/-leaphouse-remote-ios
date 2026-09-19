package uz.electro.remote.data

/** Канал, по которому сейчас доступна машина. */
enum class Link {
    /** Сервер машину не видит: голова не на связи или сервер недоступен. */
    NONE,

    /** Через наш backend — единственный путь к машине. */
    CLOUD,
}

data class GeoPoint(val lat: Double, val lon: Double, val bearing: Double? = null)

data class Doors(
    val frontLeft: Boolean = false,
    val frontRight: Boolean = false,
    val rearLeft: Boolean = false,
    val rearRight: Boolean = false,
) {
    val anyOpen: Boolean get() = frontLeft || frontRight || rearLeft || rearRight
}

/**
 * Разобранное состояние машины: экраны читают типизированные поля, а не
 * выковыривают строки из сырой карты сигналов на каждой отрисовке.
 *
 * `raw` оставлен для диагностики и для сигналов, под которые ещё нет поля.
 */
/**
 * Охрана. Отдельно от замков намеренно: снятие с охраны обязано оставить двери
 * запертыми, и «заперто» не значит «на охране». Сервер их различает, и экран
 * обязан различать тоже.
 */
enum class Security { ARMED, DISARMED, UNKNOWN }

data class CarState(
    val link: Link = Link.NONE,
    val raw: Map<String, String> = emptyMap(),
    val security: Security = Security.UNKNOWN,
    val vin: String? = null,
    val soc: Int? = null,
    val rangeKm: Int? = null,
    val locked: Boolean? = null,
    val trunkOpen: Boolean? = null,
    //: капот и двери по отдельности голова не отдаёт — в её карте сигналов
    //: есть только общий замок. Поля оставлены под будущую прошивку.
    val hoodOpen: Boolean = false,
    val doors: Doors = Doors(),
    val cabinTemp: Double? = null,
    val outsideTemp: Double? = null,
    val setTempLeft: Int? = null,
    val setTempRight: Int? = null,
    val fan: Int? = null,
    val acOn: Boolean? = null,
    val odometerKm: Int? = null,
    val speedKmh: Int? = null,
    /** Минут до полной зарядки; null — не заряжается или сигнала нет. */
    val chargeMinutes: Int? = null,
    val location: GeoPoint? = null,
    /** Через сколько минут сервер сам погасит климат; 0 — не гасит. */
    val climateAutoOffMinutes: Int = 0,
    val updatedAt: Long = 0L,
) {
    val online: Boolean get() = link != Link.NONE

    /**
     * Значение сигнала по имени — для того, что ещё не вынесено в поля.
     * Ключи головы приходят как "имя(id)", VIN — просто "vin".
     */
    fun signal(name: String): String? = raw.sig(name)

    companion object {
        fun fromCloud(dto: StatusDto, now: Long): CarState {
            // Значения сигналов приходят как есть — числа станут "23.0", и
            // разбирать их надо через toFloat, а не toInt.
            val raw = dto.raw.orEmpty()
                .mapValues { (_, v) -> v.toString() }
                .filterValues { it.isNotBlank() && it != "null" }

            fun num(name: String): Float? = raw.sig(name)?.toFloatOrNull()
            fun flag(name: String): Boolean? = raw.sig(name)?.let { it != "0" }

            return CarState(
                link = if (dto.online) Link.CLOUD else Link.NONE,
                raw = raw,
                security = when (dto.security_state) {
                    "ARMED" -> Security.ARMED
                    "DISARMED" -> Security.DISARMED
                    else -> Security.UNKNOWN
                },
                soc = dto.battery_percent ?: num("soc")?.toInt(),
                // Запас хода и пробег машина считает сама; из сигналов берём
                // только когда сервер не дал готового поля.
                rangeKm = dto.range_km ?: num("range_ev")?.toInt(),
                odometerKm = dto.odometer_km ?: num("odometer")?.toInt(),
                locked = when (dto.doors) {
                    "LOCKED" -> true
                    "UNLOCKED" -> false
                    else -> flag("lock")
                },
                trunkOpen = flag("trunk"),
                acOn = dto.climate_on,
                cabinTemp = num("cabin_temp")?.toDouble(),
                outsideTemp = num("outside_temp")?.toDouble(),
                setTempLeft = num("temp_l")?.toInt(),
                setTempRight = num("temp_r")?.toInt(),
                fan = num("fan")?.toInt(),
                speedKmh = num("speed")?.toInt(),
                chargeMinutes = num("charge_time")?.toInt(),
                // Точка — только когда есть обе координаты.
                location = dto.latitude?.let { lat ->
                    dto.longitude?.let { lon -> GeoPoint(lat, lon) }
                },
                climateAutoOffMinutes = dto.climate_auto_off_minutes,
                updatedAt = now,
            )
        }
    }
}

/**
 * Температура для показа: целые — без дробной части.
 *
 * Голова отдаёт «24.0», и без этого на экране стояло бы «в салоне 24.0°».
 * Дробь не отбрасываем совсем: половинки градуса машина отдаёт настоящие.
 */
fun Double.asTemp(): String =
    if (this == this.toInt().toDouble()) this.toInt().toString() else this.toString()

/** Ключи головы приходят как "имя(id)"; пустые значения считаем отсутствующими. */
internal fun Map<String, String>.sig(name: String): String? =
    (this[name] ?: entries.firstOrNull { it.key.startsWith("$name(") }?.value)
        ?.takeIf { it.isNotBlank() && it != "null" }
