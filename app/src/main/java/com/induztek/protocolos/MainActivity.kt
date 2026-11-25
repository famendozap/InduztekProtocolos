package com.induztek.protocolos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.induztek.protocolos.ui.InduztekApp
import com.induztek.protocolos.ui.theme.InduztekProtocolosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                InduztekProtocolosTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        InduztekApp(application = application)
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e("MainActivity", "Error iniciando app: ${e.message}", e)
            Toast.makeText(this, "Error al iniciar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
