package userData

data class UserSearchData(
    var searchInstr: String ?= null,
    var searchRadius: Int ?= null,
    var oldestBracket: String ?= null
)
