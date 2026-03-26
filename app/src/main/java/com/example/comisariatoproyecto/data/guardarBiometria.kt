package com.example.comisariatoproyecto.utils

import android.content.Context

class SessionPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(
        "comisariato_prefs", Context.MODE_PRIVATE
    )

    // Correo
    fun guardarCorreo(correo: String) =
        prefs.edit().putString("correo_guardado", correo).apply()

    fun obtenerCorreo(): String =
        prefs.getString("correo_guardado", "") ?: ""

    // Contraseña (necesaria para login silencioso con biometría)
    fun guardarPassword(password: String) =
        prefs.edit().putString("password_guardado", password).apply()

    fun obtenerPassword(): String =
        prefs.getString("password_guardado", "") ?: ""

    // Nombre real del empleado (para mostrar en UI)
    fun guardarNombre(nombre: String) =
        prefs.edit().putString("nombre_guardado", nombre).apply()

    fun obtenerNombre(): String =
        prefs.getString("nombre_guardado", "") ?: ""

    // Biometría
    fun activarBiometria() =
        prefs.edit().putBoolean("biometria_habilitada", true).apply()

    fun biometriaHabilitada(): Boolean =
        prefs.getBoolean("biometria_habilitada", false)

    // Limpiar todo al hacer logout
    fun limpiarSesionLocal() =
        prefs.edit().clear().apply()
}