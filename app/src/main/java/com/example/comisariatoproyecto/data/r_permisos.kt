package com.example.comisariatoproyecto.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class r_permisos {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "r_permisos"
    }

    // ─── LOGIN CON FIREBASE AUTH ────────────────────────────────────────────
    suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Login OK uid=${auth.currentUser?.uid}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Login FAILED tipo=${e.javaClass.simpleName} mensaje=${e.message}")
            false
        }
    }

    // ─── LOGIN SILENCIOSO PARA BIOMETRÍA ────────────────────────────────────
    suspend fun loginSilencioso(email: String, password: String): Boolean {
        return try {
            if (email.isEmpty() || password.isEmpty()) return false
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "loginSilencioso OK")
            true
        } catch (e: Exception) {
            Log.e(TAG, "loginSilencioso ERROR: ${e.message}")
            false
        }
    }

    // ─── OBTENER USUARIO LOGUEADO DESDE COLECCIÓN "usuarios" ────────────────
    suspend fun obtenerMiUsuario(): Usuario? {
        return withContext(Dispatchers.IO) {
            try {
                val correo = auth.currentUser?.email?.trim()
                Log.d(TAG, "Buscando usuario con correo=$correo")

                if (correo.isNullOrEmpty()) {
                    Log.w(TAG, "No hay sesión activa")
                    return@withContext null
                }

                val snap = db.collection("usuarios")
                    .whereEqualTo("correo", correo)
                    .get()
                    .await()

                Log.d(TAG, "Documentos encontrados en usuarios: ${snap.size()}")

                if (snap.isEmpty) {
                    Log.w(TAG, "Sin usuario con correo=$correo")
                    return@withContext null
                }

                val doc = snap.documents.first()
                Log.d(TAG, "Datos crudos usuario: ${doc.data}")

                val usuario = doc.toObject(Usuario::class.java)
                Log.d(TAG, "Usuario deserializado: ${usuario?.nombre}")

                return@withContext usuario

            } catch (e: Exception) {
                Log.e(TAG, "CRASH obtenerMiUsuario: ${e.javaClass.simpleName} → ${e.message}")
                return@withContext null
            }
        }
    }

    // ─── OBTENER PERFIL EMPLEADO RELACIONADO AL USUARIO LOGUEADO ────────────
    suspend fun obtenerMiPerfilEmpleado(): Empleado? {
        return withContext(Dispatchers.IO) {
            try {
                val correo = auth.currentUser?.email?.trim()
                Log.d(TAG, "Buscando perfil empleado para correo=$correo")

                if (correo.isNullOrEmpty()) {
                    Log.w(TAG, "No hay sesión activa")
                    return@withContext null
                }

                // 1. Buscar usuario por correo
                val usuarioSnap = db.collection("usuarios")
                    .whereEqualTo("correo", correo)
                    .get()
                    .await()

                Log.d(TAG, "Documentos encontrados en usuarios: ${usuarioSnap.size()}")

                if (usuarioSnap.isEmpty) {
                    Log.w(TAG, "No se encontró usuario con correo=$correo")
                    return@withContext null
                }

                val usuarioDoc = usuarioSnap.documents.first()
                Log.d(TAG, "Datos crudos usuario para empleado: ${usuarioDoc.data}")

                val empleadoId = usuarioDoc.getString("empleadoId")?.trim()

                if (empleadoId.isNullOrEmpty()) {
                    Log.w(TAG, "El usuario no tiene empleadoId")
                    return@withContext null
                }

                Log.d(TAG, "empleadoId encontrado: $empleadoId")

                // 2. Buscar empleado por ID de documento
                val empleadoDoc = db.collection("empleados")
                    .document(empleadoId)
                    .get()
                    .await()

                if (!empleadoDoc.exists()) {
                    Log.w(TAG, "No existe empleado con id=$empleadoId")
                    return@withContext null
                }

                Log.d(TAG, "Datos crudos empleado: ${empleadoDoc.data}")

                val empleado = empleadoDoc.toObject(Empleado::class.java)
                Log.d(TAG, "Empleado deserializado: ${empleado?.nombreCompleto}")

                return@withContext empleado

            } catch (e: Exception) {
                Log.e(TAG, "CRASH obtenerMiPerfilEmpleado: ${e.javaClass.simpleName} → ${e.message}")
                return@withContext null
            }
        }
    }

    fun obtenerCorreoActual(): String? = auth.currentUser?.email

    fun isLogged(): Boolean = auth.currentUser != null

    fun obtenerUid(): String? = auth.currentUser?.uid

    fun logout() {
        auth.signOut()
        Log.d(TAG, "Sesión cerrada")
    }
    suspend fun obtenerMiPerfilCompleto(): Pair<Usuario?, Empleado?> {
        return withContext(Dispatchers.IO) {
            try {
                val correo = auth.currentUser?.email?.trim()
                if (correo.isNullOrEmpty()) return@withContext Pair(null, null)

                // 1. Traer usuario
                val usuarioSnap = db.collection("usuarios")
                    .whereEqualTo("correo", correo)
                    .get()
                    .await()

                if (usuarioSnap.isEmpty) return@withContext Pair(null, null)

                val usuarioDoc = usuarioSnap.documents.first()
                val usuario = usuarioDoc.toObject(Usuario::class.java)

                // 2. Traer empleado con el empleadoId del usuario
                val empleadoId = usuarioDoc.getString("empleadoId")?.trim()
                if (empleadoId.isNullOrEmpty()) return@withContext Pair(usuario, null)

                val empleadoDoc = db.collection("empleados")
                    .document(empleadoId)
                    .get()
                    .await()

                val empleado = if (empleadoDoc.exists())
                    empleadoDoc.toObject(Empleado::class.java)
                else null

                return@withContext Pair(usuario, empleado)

            } catch (e: Exception) {
                Log.e(TAG, "CRASH obtenerMiPerfilCompleto: ${e.message}")
                return@withContext Pair(null, null)
            }
        }
    }

}