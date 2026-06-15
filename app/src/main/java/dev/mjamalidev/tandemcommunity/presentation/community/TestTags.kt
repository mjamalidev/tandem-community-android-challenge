package dev.mjamalidev.tandemcommunity.presentation.community

object CommunityTestTags {
    const val MEMBER_LIST = "member_list"
    const val INITIAL_LOADING = "initial_loading"
    const val INITIAL_ERROR = "initial_error"
    const val LOAD_MORE_ERROR = "load_more_error"
    const val LOAD_MORE = "load_more"
    const val RETRY = "retry"
    fun member(id: Int) = "member_$id"
    fun like(id: Int) = "like_$id"
}
