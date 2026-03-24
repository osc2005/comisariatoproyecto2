package com.example.comisariatoproyecto.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class r_permisos {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()



    suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error al iniciar sesión: ${e.message}") // Opcional: para ver el error en el Logcat
            false
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLogged(): Boolean {
        return auth.currentUser != null
    }

}