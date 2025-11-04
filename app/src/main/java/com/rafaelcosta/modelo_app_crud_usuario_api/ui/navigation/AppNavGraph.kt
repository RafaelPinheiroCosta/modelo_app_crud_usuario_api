package com.rafaelcosta.app_modelo_login_jwt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rafaelcosta.app_modelo_login_jwt.ui.home.HomeScreen
import com.rafaelcosta.app_modelo_login_jwt.ui.login.LoginScreen
import com.rafaelcosta.app_modelo_login_jwt.ui.session.AuthState
import com.rafaelcosta.app_modelo_login_jwt.ui.session.AuthStateViewModel
import com.rafaelcosta.modelo_app_crud_usuario_api.ui.feature.usuario.edit.UsuarioEditScreen
import com.rafaelcosta.modelo_app_crud_usuario_api.ui.feature.usuario.list.UsuariosListScreen
import com.rafaelcosta.modelo_app_crud_usuario_api.ui.navigation.Routes
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AppNavGraph(nav: NavHostController, modifier: Modifier = Modifier) {
    val vm: AuthStateViewModel = hiltViewModel()
    val authState = vm.state.collectAsState().value

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Routes.Home
        AuthState.Unauthenticated -> Routes.Login
        AuthState.Loading -> "splash"
    }

    NavHost(
        navController = nav,
        startDestination = startDestination
    ) {
        composable("splash") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home) {
            HomeScreen(
                onLogout = {
                    nav.navigate(Routes.Login) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                },
                onOpenCadastro = { nav.navigate(Routes.UsuarioForm) },
                onOpenLista = { nav.navigate(Routes.UsuarioList) }
            )
        }

        composable(Routes.UsuarioList) {
            UsuariosListScreen(
                onAdd = { nav.navigate(Routes.UsuarioForm) },
                onOpen = { id -> nav.navigate("${Routes.UsuarioForm}/$id") }
            )
        }

        composable(Routes.UsuarioForm) {
            UsuarioEditScreen(usuarioId = null, onDone = { nav.popBackStack() })
        }

        composable(
            route = "${Routes.UsuarioForm}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            UsuarioEditScreen(usuarioId = id, onDone = { nav.popBackStack() })
        }
    }
}
