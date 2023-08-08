package userData

data class User
    (
    val uid: String ?= null,
    val profileType: String ?= null,
    val imageUrl: String ?= null, // viewable
    var displayName: String ?= null, // viewable
    var age: String ?= null, // viewable
    var bio: String ?= null, // viewable
    var website: String ?= null, // viewable
    var latitude: Double ?= null,
    var longitude: Double ?= null,
    var influences: MutableList<String> ?= null, // viewable
    var instruments: MutableList<String> ?= null, // viewable
    var genres: MutableList<String> ?= null // viewable
    )
