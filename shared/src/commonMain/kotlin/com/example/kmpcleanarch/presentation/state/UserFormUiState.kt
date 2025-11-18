package com.example.kmpcleanarch.presentation.state

/**
 * UI State for User Form Screen (Create/Edit)
 */
data class UserFormUiState(
    val userId: Long = 0,
    val email: String = "",
    val name: String = "",
    val role: String = "user",
    val avatarUrl: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaveSuccessful: Boolean = false,
    val availableRoles: List<String> = listOf("admin", "user", "moderator")
) {
    val isEditMode: Boolean
        get() = userId > 0

    val title: String
        get() = if (isEditMode) "Edit User" else "Create User"

    val saveButtonText: String
        get() = if (isEditMode) "Update" else "Create"
}
