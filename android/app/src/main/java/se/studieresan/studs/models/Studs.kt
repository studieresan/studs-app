package se.studieresan.studs.models

import java.io.Serializable
import java.util.*

data class City(
        val name: String = "",
        val start: Date? = null,
        val end: Date? = null,
        val id: String = ""
): Serializable

enum class Category(val asString: String) {
    Food("food"),
    Drink("drink"),
    Attraction("attraction"),
    Other("other"),
}

data class Activity(
        val title: String? = null,
        val author: String = "",
        val description: String? = null,
        val created: Date? = null,
        val start: Date? = null,
        val end: Date? = null,
        val signupCloses: Date? = null,
        val category: String? = null,
        val price: String? = null,
        val location: Location = Location(),
        val isUserActivity: Boolean? = false,
        val id: String = "",
        val city: String? = null
): Serializable

data class Location(
        val latLng: Coordinate? = null,
        val address: String? = null
): Serializable

data class Coordinate(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
): Serializable

data class StudsUser(
        val id: String = "",
        val profile: Profile = Profile()
): Serializable

data class Profile(
        val email: String = "",
        val firstName: String = "",
        val lastName: String = "",
        val phone: String? = "",
        val picture: String = ""
): Serializable

data class Registration(
        val userId: String = "",
        val time: Date? = null,
        val registeredById: String = "",
        val id: String = "",
        val activityId: String = ""
): Serializable
