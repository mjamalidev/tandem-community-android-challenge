package dev.mjamalidev.tandemcommunity.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CommunityMember(
    val id: Int,
    val firstName: String,
    val topic: String,
    val pictureUrl: String,
    val nativeLanguages: List<String>,
    val learningLanguages: List<String>,
    val referenceCount: Int,
    val isLiked: Boolean,
) {
    val isNew: Boolean
        get() = referenceCount == 0
}
