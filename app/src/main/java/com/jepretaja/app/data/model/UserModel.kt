package com.jepretaja.app.data.model

import com.google.firebase.firestore.DocumentId

data class UserModel(
    @DocumentId val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val province: String? = null,
    val role: String = "customer", // guest|customer|creator|admin|super_admin
    val photoUrl: String? = null,
    val status: String = "active",
    val fcmToken: String? = null,
)
