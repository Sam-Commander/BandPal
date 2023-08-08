package userData

data class PMatch(
    val uid: String ?= null,
    var matchedInfluences: MutableList<String> ?= null,
    var matchedGenres: MutableList<String> ?= null,
    val distanceToPMatch: Double ?= null,
    val imageUrl: String ?= null,
    val displayName: String ?= null
)
