package com.rafaelcosta.app_modelo_login_jwt.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onOpenCadastro: () -> Unit,
    onOpenLista: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.loading -> CircularProgressIndicator()

            state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Erro: ${state.error}",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onLogout) {
                    Text("Fazer login novamente")
                }
            }

            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Bem-vindo, ${state.nome}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(state.email)
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onOpenCadastro,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Cadastrar Usuário")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onOpenLista,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Listar Usuários")
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        vm.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Sair", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}
