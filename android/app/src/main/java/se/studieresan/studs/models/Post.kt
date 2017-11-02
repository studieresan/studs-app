package se.studieresan.studs.models

import se.studieresan.studs.R

data class Location(
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var message: String = "",
        var key: String = "",
        var user: String? = "",
        var timestamp: Long = 0,
        var category: String = ""
) {

    fun getTimeAgo(): String {
        val minutesAgo = (System.currentTimeMillis()/1000 - timestamp)/60
        if (minutesAgo.toInt() == 0) {
            return "just now"
        } else if (minutesAgo.toInt() == 1) {
            return "a minute ago"
        } else {
            return "$minutesAgo minutes ago"
        }
    }

    fun getDescriptionForCategory() =
            if (message.isEmpty()) {
                when (category) {
                    "eat" -> "Eating"
                    "drink" -> "Drinking"
                    "shopping" -> "Shopping"
                    else -> "Activity"
                }
            } else message
}

fun getIconForCategory(category: String): Int = when (category) {
    "eat" -> R.drawable.ic_restaurant_black_24dp
    "drink" -> R.drawable.ic_local_bar_black_24dp
    "shopping" -> R.drawable.ic_local_mall_black_24dp
    "activity" -> R.drawable.ic_local_see_black_24dp
    "living" -> R.drawable.ic_local_hotel_black_24dp
    else -> R.drawable.ic_location_on_black_24dp
}
