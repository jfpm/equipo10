package com.equipo10.dogapp.view.fragment

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.equipo10.dogapp.AdminCitasFragment
import com.equipo10.dogapp.R
import com.equipo10.dogapp.databinding.FragmentLoginBinding
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController



class LoginFragment<KeyGenParameterSpec> : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    lateinit var info: String

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.btnBiometria.setOnClickListener{
            checkDeviceHasBiometric()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getbiometricSupport()

    }

    fun getbiometricSupport(){
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Authentication error: $errString")
                    //Toast.makeText(FragmentA, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser("Authentication exito!")
                    //Toast.makeText(this@FragmentA,"Authentication exito!", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_loginFragment_to_adminCitasFragment)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    notifyUser("Authentication failed")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticaciòn con Biometria")
            .setSubtitle("ingrese su huella digital aqui")
            .setNegativeButtonText("Cancelar")
            .build()

        binding.btnBiometria.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    fun checkDeviceHasBiometric(){
        val biometricManager = BiometricManager.from(requireContext())
        when(biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)){
            BiometricManager.BIOMETRIC_SUCCESS->{
                Log.d("mi app","app can authenticate using biometric")
                info = "App can authenticate using biometrics."
                binding.btnBiometria.isEnabled=true

            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE->{
                Log.d("mi app","No biometric features available on this device.")
                info = "No biometric features available on this device."
                binding.btnBiometria.isEnabled=false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                info = "Biometric features are currently unavailable."
                binding.btnBiometria.isEnabled = false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                }
                binding.btnBiometria.isEnabled=false

                val intent = Intent(requireContext(), LoginFragment::class.java)
                mStartForResult.launch(intent)

            }
        }
        notifyUser(info)
    }

    private fun notifyUser(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    //metodo para lanzar actividad remplazo de starActivityForResult
    val mStartForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
        }
    }



}