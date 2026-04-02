package com.example.comisariatoproyecto.utils

import android.content.Context

/**
 * Clase encargada de manejar la sesión local del usuario utilizando SharedPreferences.
 *
 * Permite guardar y recuperar datos básicos como:
 * - Correo
 * - Contraseña
 * - Nombre
 * - Estado de biometría
 * - Estado de registro del usuario
 *
 * También permite limpiar toda la sesión almacenada.
 */
class SessionPrefs(context: Context) {

    /**
     * Instancia de SharedPreferences donde se guardan los datos.
     */
    private val prefs = context.getSharedPreferences(
        "comisariato_prefs", Context.MODE_PRIVATE
    )

    /**
     * Guarda el correo del usuario.
     * @param correo Correo electrónico del usuario.
     */
    fun guardarCorreo(correo: String) =
        prefs.edit().putString("correo_guardado", correo).apply()

    /**
     * Obtiene el correo almacenado.
     * @return Correo guardado o cadena vacía si no existe.
     */
    fun obtenerCorreo(): String =
        prefs.getString("correo_guardado", "") ?: ""

    /**
     * Guarda la contraseña del usuario.
     * ⚠️ Nota: No es recomendable guardar contraseñas en texto plano en producción.
     * @param password Contraseña del usuario.
     */
    fun guardarPassword(password: String) =
        prefs.edit().putString("password_guardado", password).apply()

    /**
     * Obtiene la contraseña almacenada.
     * @return Contraseña guardada o cadena vacía si no existe.
     */
    fun obtenerPassword(): String =
        prefs.getString("password_guardado", "") ?: ""

    /**
     * Guarda el nombre del usuario.
     * @param nombre Nombre del usuario.
     */
    fun guardarNombre(nombre: String) =
        prefs.edit().putString("nombre_guardado", nombre).apply()

    /**
     * Obtiene el nombre almacenado.
     * @return Nombre guardado o cadena vacía si no existe.
     */
    fun obtenerNombre(): String =
        prefs.getString("nombre_guardado", "") ?: ""

    /**
     * Activa el uso de autenticación biométrica.
     */
    fun activarBiometria() =
        prefs.edit().putBoolean("biometria_habilitada", true).apply()

    /**
     * Desactiva el uso de autenticación biométrica.
     */
    fun desactivarBiometria() =
        prefs.edit().putBoolean("biometria_habilitada", false).apply()

    /**
     * Verifica si la biometría está habilitada.
     * @return true si está activa, false en caso contrario.
     */
    fun biometriaHabilitada(): Boolean =
        prefs.getBoolean("biometria_habilitada", false)

    /**
     * Marca que el usuario ya inició sesión al menos una vez.
     * Se usa para saber si se debe mostrar login completo o solo biometría.
     */
    fun marcarUsuarioRegistrado() =
        prefs.edit().putBoolean("usuario_registrado", true).apply()

    /**
     * Verifica si ya hay un usuario registrado en el dispositivo.
     * @return true si ya existe sesión previa, false si es primer ingreso.
     */
    fun hayUsuarioRegistrado(): Boolean =
        prefs.getBoolean("usuario_registrado", false)

    /**
     * Limpia toda la información de la sesión local.
     * (Se usa normalmente al cerrar sesión).
     */
    fun limpiarSesionLocal() =
        prefs.edit().clear().apply()
}