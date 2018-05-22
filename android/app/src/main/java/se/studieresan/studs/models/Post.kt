package se.studieresan.studs.models

import se.studieresan.studs.R

data class ShareLocation(
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
                    "other" -> "Other"
                    else -> "Activity"
                }
            } else message
}

fun getIconForCategory(category: String): Int = when (category) {
    "attraction" -> R.drawable.ic_camera
    "drink" -> R.drawable.ic_drink
    "food" -> R.drawable.ic_food
    "other" -> R.drawable.ic_other
    else -> R.drawable.ic_other
}
