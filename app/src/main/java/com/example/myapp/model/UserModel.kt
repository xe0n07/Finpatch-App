package com.example.myapp.model

data class UserModel(
    val id: String = "",
    val username: String = "",       // chosen on LandingScreen
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val dob: String = "",
    val email: String = "",
    val currency: String = "USD",    // chosen on LandingScreen
    val profilePhotoUrl: String = "" // populated from Google account on Google sign-in
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id"              to id,
        "username"        to username,
        "firstName"       to firstName,
        "lastName"        to lastName,
        "gender"          to gender,
        "dob"             to dob,
        "email"           to email,
        "currency"        to currency,
        "profilePhotoUrl" to profilePhotoUrl
    )
}
