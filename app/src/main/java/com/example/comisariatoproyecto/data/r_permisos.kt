package com.example.comisariatoproyecto.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class r_permisos {

    // aqui en R_permisos esta el log in y el perfil. De aqui se jala
    //todo de estas dos pantallas

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "r_permisos"
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Login OK uid=${auth.currentUser?.uid}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Login ERROR: ${e.message}")
            false
        }
    }

    suspend fun obtenerMiPerfilEmpleado(): Empleado? {
        return try {
            val correo = auth.currentUser?.email
            Log.d(TAG, "Buscando perfil para correo=$correo")

            if (correo == null) {
                Log.w(TAG, "No hay sesión activa")
                return null
            }

            val snap = db.collection("empleados")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            Log.d(TAG, "Documentos encontrados: ${snap.size()}")

            if (snap.isEmpty) {
                Log.w(TAG, "Ningún empleado con correo=$correo")
                return null
            }

            val doc = snap.documents.first()
            Log.d(TAG, "Datos crudos Firestore: ${doc.data}")

            val empleado = doc.toObject(Empleado::class.java)
            Log.d(TAG, "Empleado deserializado OK: ${empleado?.nombreCompleto}")
            empleado

        } catch (e: Exception) {
            Log.e(TAG, "CRASH obtenerPerfil: ${e.javaClass.simpleName} → ${e.message}")
            null // retorna null en vez de crashear
        }
    }

     fun loginSilencioso(email: String, password: String): Boolean {
        return try {
            if (email.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "loginSilencioso: credenciales vacías")
                return false
            }
            auth.signInWithEmailAndPassword(email, password)
            Log.d(TAG, "loginSilencioso OK uid=${auth.currentUser?.uid}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "loginSilencioso ERROR: ${e.message}")
            false
        }
    }

    fun isLogged(): Boolean            = auth.currentUser != null
    fun obtenerUid(): String?          = auth.currentUser?.uid

    fun logout() {
        auth.signOut()
        Log.d(TAG, "Sesión cerrada")
    }
}