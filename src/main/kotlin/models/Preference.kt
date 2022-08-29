package models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import utils.Helper


enum class PreferenceVersion {
    V1
}

enum class PreferenceType {
    USER
}

enum class PreferenceStatus {
    ACTIVE,
    INACTIVE
}

enum class PreferenceCardinality {
    ALLOW,
    DENY
}

sealed class PreferenceCandidateValue


data class SingleCandidateItem<T>(
    val value: T
) : PreferenceCandidateValue() {
    fun match(target: T): Boolean =
        this.value == target
}

/**
 * multi resource item
 */
data class MultiCandidateItem<T>(
    val value: Set<T>
) : PreferenceCandidateValue() {
    fun match(target: Set<*>): Boolean =
        this.value.intersect(target).isNotEmpty()
}

data class AvailabilityItem(
    val dayIndex: Int,
    val startTime: Long,
    val endTime: Long
) {
    fun match(target: AvailabilityItem): Boolean =
        dayIndex == target.dayIndex && target.let {
            startTime <= it.startTime && endTime >= it.endTime
        }
}

data class UnavailabilityItem(
    val startTime: Long,
    val endTime: Long
) {
    fun match(target: UnavailabilityItem): Boolean =
        target.let {
            startTime <= it.startTime && endTime >= it.endTime
        }
}

interface Preference {
    val preferenceId : String
    val preferenceType: PreferenceType
    val version: PreferenceVersion
    val starAt: Long
    val expireAt: Long?
    val status: PreferenceStatus
    val value: PreferenceCandidateValue
    val cardinality: PreferenceCardinality
    fun matches(target: PreferenceCandidateValue): Boolean
    fun isActive() : Boolean = status == PreferenceStatus.ACTIVE &&
            expireAt?.let {Helper.currentDateTime() < it} == true
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SlotDurationPreference::class, name = "slot-pref"),
    JsonSubTypes.Type(value = AvailabilityPreference::class, name = "avail-pref"),
    JsonSubTypes.Type(value = HolidayPreference::class, name = "holiday-pref"),
    JsonSubTypes.Type(value = HolidayPreference::class, name = "slot-adv-pref"),
)
sealed class UserPreference(
    open val user: User,
    override val preferenceType: PreferenceType = PreferenceType.USER,
    override val preferenceId: String = Helper.generateUuid("PREF")
) : Preference


@JsonTypeName("slot-pref")
data class SlotDurationPreference(
    val durationInMilliseconds: Long,
    override val starAt: Long,
    override val user: User,
    override val status: PreferenceStatus = PreferenceStatus.ACTIVE,
    override val expireAt: Long? = null
) : UserPreference(user) {
    override val cardinality: PreferenceCardinality = PreferenceCardinality.ALLOW
    override val value: SingleCandidateItem<Long> = SingleCandidateItem(durationInMilliseconds)
    override val version: PreferenceVersion = PreferenceVersion.V1

    override fun matches(target: PreferenceCandidateValue): Boolean =
        target.let {
            require(target is SingleCandidateItem<*>)
            value.match((target as SingleCandidateItem<Long>).value)
        }
}

@JsonTypeName("avail-pref")
data class AvailabilityPreference(
    val availabilityDays: Set<AvailabilityItem>,
    override val starAt: Long,
    override val user: User,
    override val status: PreferenceStatus = PreferenceStatus.ACTIVE,
    override val expireAt: Long? = null,
) : UserPreference(user) {
    override val cardinality: PreferenceCardinality = PreferenceCardinality.ALLOW
    override val version: PreferenceVersion = PreferenceVersion.V1
    override val value: MultiCandidateItem<AvailabilityItem> = MultiCandidateItem(availabilityDays)
    override fun matches(target: PreferenceCandidateValue): Boolean =
        target.let {
            require(target is SingleCandidateItem<*>)
            val targetValue = (target as SingleCandidateItem<AvailabilityItem>).value
            availabilityDays.filter { it.dayIndex == targetValue.dayIndex }.any {
                it.match(targetValue)
            }
        }
    companion object{}
}


@JsonTypeName("holiday-pref")
data class HolidayPreference(
    val unavailabilityDays: UnavailabilityItem,
    override val starAt: Long,
    override val user: User,
    override val status: PreferenceStatus = PreferenceStatus.ACTIVE,
    override val expireAt: Long,
) : UserPreference(user) {
    init {
        require(expireAt > starAt){
            "End of holiday should be greater than start"
        }
    }
    override val cardinality: PreferenceCardinality = PreferenceCardinality.DENY
    override val version: PreferenceVersion = PreferenceVersion.V1
    override val value: SingleCandidateItem<UnavailabilityItem> = SingleCandidateItem(unavailabilityDays)
    override fun matches(target: PreferenceCandidateValue): Boolean =
        target.let {
            require(target is SingleCandidateItem<*>)
            val targetValue = (target as SingleCandidateItem<UnavailabilityItem>).value
            unavailabilityDays.match(targetValue)
        }
}


@JsonTypeName("slot-adv-pref")
data class SlotAdvancePreference(
    val durationInMinutes: Long,
    override val starAt: Long,
    override val user: User,
    override val status: PreferenceStatus = PreferenceStatus.ACTIVE,
    override val expireAt: Long? = null,
) : UserPreference(user) {
    override val cardinality: PreferenceCardinality = PreferenceCardinality.ALLOW
    override val version: PreferenceVersion = PreferenceVersion.V1
    override val value: SingleCandidateItem<Long> = SingleCandidateItem(durationInMinutes)
    override fun matches(target: PreferenceCandidateValue): Boolean =
        target.let {
            require(target is SingleCandidateItem<*>)
            val targetValue = (target as SingleCandidateItem<Long>).value
            Helper.getCurrentDateTimeBeforeNMinutes(durationInMinutes) >= targetValue
        }
}
