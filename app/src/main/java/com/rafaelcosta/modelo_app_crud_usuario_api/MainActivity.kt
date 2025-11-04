package com.rafaelcosta.modelo_app_crud_usuario_api

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rafaelcosta.app_modelo_login_jwt.ui.navigation.AppNavGraph
import com.rafaelcosta.modelo_app_crud_usuario_api.ui.theme.Modelo_app_crud_usuario_apiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Modelo_app_crud_usuario_apiTheme {
                val nav = rememberNavController()
                Scaffold () { paddingValues ->
                    AppNavGraph(nav, Modifier.padding(paddingValues))
                }
            }
        }
    }
}
