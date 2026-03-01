package com.example.myapp.repository


import com.example.finpatch.data.repo.UserRepo
import com.example.myapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepoImpl : UserRepo {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Users")

    // ── Existing: Email / Password ────────────────────────────────────────────

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Login Success")
                else callback(false, it.exception?.message ?: "Login failed")
            }
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    callback(true, "Registration Success", auth.currentUser?.uid ?: "")
                else
                    callback(false, it.exception?.message ?: "Registration failed", "")
            }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Registration Success")
            else callback(false, it.exception?.message ?: "Database error")
        }
    }

    // ── NEW: Google Sign-In ───────────────────────────────────────────────────

    /**
     * Authenticates with Firebase using a Google ID token, then either:
     *  - Creates a new user record (first-time sign-in) using [username] and [currency]
     *  - OR leaves the existing record untouched (returning user)
     *
     * @param idToken     The ID token from GoogleSignInAccount
     * @param username    Display name chosen on LandingScreen
     * @param currency    Currency code chosen on LandingScreen
     * @param callback    (success, message)
     */
    override fun signInWithGoogle(
        idToken: String,
        username: String,
        currency: String,
        callback: (Boolean, String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { authTask ->
                if (!authTask.isSuccessful) {
                    callback(false, authTask.exception?.message ?: "Google auth failed")
                    return@addOnCompleteListener
                }

                val firebaseUser = auth.currentUser ?: run {
                    callback(false, "No user found after Google sign-in")
                    return@addOnCompleteListener
                }

                val userId = firebaseUser.uid
                val isNewUser = authTask.result?.additionalUserInfo?.isNewUser ?: false

                if (isNewUser) {
                    // ── First time: build UserModel and save ──────────────────
                    val nameParts = (firebaseUser.displayName ?: "").split(" ", limit = 2)
                    val model = UserModel(
                        id = userId,
                        firstName = nameParts.getOrElse(0) { username },
                        lastName = nameParts.getOrElse(1) { "" },
                        email = firebaseUser.email ?: "",
                        gender = "",
                        dob = "",
                        username = username,
                        currency = currency,
                        profilePhotoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    ref.child(userId).setValue(model).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) callback(true, "Account created successfully!")
                        else callback(false, dbTask.exception?.message ?: "Failed to save user data")
                    }
                } else {
                    // ── Returning user: don't overwrite, just sign in ─────────
                    callback(true, "Welcome back!")
                }
            }
    }

    // ── Existing: Forget Password ─────────────────────────────────────────────

    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    callback(true, "Password reset email sent to $email")
                else
                    callback(false, task.exception?.message ?: "Failed to send reset email")
            }
    }

    // ── Existing: Delete Account (fixed from code review) ────────────────────

    override fun deleteAccount(
        userId: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            callback(false, "No signed-in user"); return
        }

        // For Google sign-in users, re-auth requires a fresh Google credential.
        // Here we handle the email/password path; Google path should use
        // reauthenticateWithCredential(GoogleAuthProvider.getCredential(...))
        val email = user.email ?: run {
            callback(false, "Cannot determine user email"); return
        }

        val emailCredential = com.google.firebase.auth.EmailAuthProvider
            .getCredential(email, password)

        user.reauthenticate(emailCredential).addOnCompleteListener { reauth ->
            if (!reauth.isSuccessful) {
                callback(false, "Incorrect password"); return@addOnCompleteListener
            }
            ref.child(userId).removeValue().addOnCompleteListener { db ->
                if (!db.isSuccessful) {
                    callback(false, db.exception?.message ?: "DB delete failed")
                    return@addOnCompleteListener
                }
                user.delete().addOnCompleteListener { del ->
                    if (del.isSuccessful) callback(true, "Account deleted successfully")
                    else callback(false, del.exception?.message ?: "Auth delete failed")
                }
            }
        }
    }

    // ── Existing: Edit Profile ────────────────────────────────────────────────

    override fun editProfile(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).updateChildren(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) callback(true, "Profile updated successfully.")
                else callback(false, it.exception?.message ?: "Update failed")
            }
    }

    // ── Existing: Get User ────────────────────────────────────────────────────

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        // Changed to singleValueEvent (no persistent listener leak — see code review)
        ref.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) callback(true, "Profile fetched", user)
                else callback(false, "User not found", null)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllUser(callback: (Boolean, String, List<UserModel>) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val list = snapshot.children.mapNotNull { it.getValue(UserModel::class.java) }
                    callback(true, "All users fetched", list)
                } else {
                    callback(false, "No users found", emptyList())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }
}
