package se.studieresan.studs.models

data class Todo(
        var address: String = "",
        var category: String = "",
        var city: String = "",
        var description: String = "",
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var name: String = "",
        var daynight: String = ""
)

