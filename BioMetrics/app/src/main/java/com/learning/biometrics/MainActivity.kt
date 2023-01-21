package com.learning.biometrics

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import com.learning.biometrics.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        biometricPrompt = BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "onAuthenticationSucceeded: ")
                Toast.makeText(this@MainActivity, "Authentication Successful", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "onAuthenticationFailed: Authentication Failed")
                Toast.makeText(this@MainActivity, "Authentication Failed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(
                    this@MainActivity,
                    "Error code:$errorCode,message:$errString",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        promptInfo = PromptInfo.Builder().setTitle("Biometric Login for App")
            .setSubtitle("Login Using Biometrics with the app")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        binding.btnFingerPrint.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
        checkAndStartBiometricAuthentication()
    }

    private val startActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Enroll Intent: Enroll Intent Result is OK")
            }
        }

    private fun checkAndStartBiometricAuthentication() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d(
                    TAG,
                    "checkBiometricAuthentication: Biometric features are currently unavailable."
                )
                binding.tvMessage.text = "Biometric features are currently unavailable."
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }
                    startActivityForResult.launch(enrollIntent)

                } else {
                    Log.d(TAG, "checkBiometricAuthentication: Biometric is Not Enrolled")
                }
                binding.tvMessage.text = " Biometric is Not Enrolled"
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d(
                    TAG,
                    "checkBiometricAuthentication: No biometric features available on this device."
                )
                binding.tvMessage.text = "No biometric features available on this device."
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.d(TAG, "checkBiometricAuthentication: Security Update required")
                binding.tvMessage.text = "Security Update required"
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Log.d(TAG, "checkBiometricAuthentication: Error Unsupported")
                binding.tvMessage.text = "Error Unsupported"
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Log.d(TAG, "checkBiometricAuthentication: Error Unknown Status")
                binding.tvMessage.text = "Error Unknown Status"
            }
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "checkBiometricAuthentication: Biometric Success")
                binding.tvMessage.text = "Biometric Success"
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}