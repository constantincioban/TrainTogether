package com.konovus.traintogether.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.konovus.traintogether.data.remote.CollectionPath
import kotlinx.coroutines.tasks.await

class FirebaseAuth: IAuthHelper {

    override suspend fun resetPassword(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
            onResult(true, "Password reset email sent")
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Something went wrong")
        }
    }

    override suspend fun createUserAccount(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid

            if (userId.isNullOrEmpty()) {
                onResult(false, "User ID is null.")
                return
            }

            firestore.collection(CollectionPath.USERS.path).document(userId).set(
                mapOf(
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()

            onResult(true, userId)
        } catch (e: Exception) {
            Log.e("Auth", "Account creation failed", e)
            onResult(false, e.message)
        }
    }

    override fun signInWithEmailPassword(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userUid = FirebaseAuth.getInstance().currentUser?.uid
                        onResult(true, userUid)
                    } else {
                        val errorMessage = task.exception?.message ?: "Unknown error"
                        onResult(false, errorMessage)
                    }
                }
        } catch (e: Exception) {
            onResult(false, e.message)
        }
    }

    override fun signOut(onResult: (Boolean, String?) -> Unit) {
        try {
            FirebaseAuth.getInstance().signOut()
            onResult(true, "Successfully signed out")
        } catch (e: Exception) {
            Log.e("Auth", "Sign out failed", e)
            onResult(false, e.message ?: "Sign out failed")
        }
    }
}