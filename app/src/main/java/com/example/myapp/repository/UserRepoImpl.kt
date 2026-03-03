package com.example.myapp.repository


import com.example.myapp.model.UserModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepoImpl : UserRepo {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Users")

    // ── Email / Password Login ────────────────────────────────────────────────

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

    // ── Email / Password Register ─────────────────────────────────────────────

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

    // ── Add User to Database ──────────────────────────────────────────────────

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

    // ── Forget Password ───────────────────────────────────────────────────────

    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    callback(true, "Password reset email sent to $email")
                else
                    callback(false, task.exception?.message ?: "Failed to send reset email")
            }
    }

    // ── Delete Account ────────────────────────────────────────────────────────

    override fun deleteAccount(
        userId: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            callback(false, "No signed-in user"); return
        }

        val email = user.email ?: run {
            callback(false, "Cannot determine user email"); return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential).addOnCompleteListener { reauth ->
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

    // ── Edit Profile ──────────────────────────────────────────────────────────

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

    // ── Get User by ID ────────────────────────────────────────────────────────

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
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

    // ── Get All Users ─────────────────────────────────────────────────────────

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