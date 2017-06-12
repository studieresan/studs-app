package se.studieresan.studs.models

data class Location(
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var message: String = "",
        var key: String = "",
        var user: String? = "",
        var timestamp: Long = 0,
        var category: String = ""
)

fun getTimeAgo(timestamp: Long): String {
    val minutesAgo = (System.currentTimeMillis()/1000 - timestamp)/60
    if (minutesAgo.toInt() == 0) {
        return "just now"
    } else if (minutesAgo.toInt() == 1) {
        return "a minute ago"
    } else {
        return "$minutesAgo minutes ago"
    }
}
