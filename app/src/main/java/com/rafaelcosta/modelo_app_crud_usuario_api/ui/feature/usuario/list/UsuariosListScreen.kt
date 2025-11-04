package com.rafaelcosta.modelo_app_crud_usuario_api.ui.feature.usuario.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun UsuariosListScreen(
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    vm: UsuariosListViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Text("+") } }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Row {
                Text("Usuários", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = vm::refresh) { Text("Sincronizar") }
            }

            Spacer(Modifier.height(8.dp))

            if (state.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            state.error?.let {
                Text("Erro: $it", color = MaterialTheme.colorScheme.error)
            }

            LazyColumn {
                items(state.items, key = { it.id }) { u ->
                    Card(
                        onClick = { onOpen(u.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            val fotoUrl = vm.fotoUrl(u.fotoPerfilUrl, u.fotoLocalPath)

                            if (!fotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = fotoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(8.dp))
                            }

                            Text(u.nome, style = MaterialTheme.typography.titleMedium)
                            Text(u.email)

                            Spacer(Modifier.height(8.dp))
                            Row {
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = { vm.delete(u.id) }) { Text("Excluir") }
                            }
                        }
                    }
                }
            }
        }
    }
}
