package com.rafaelcosta.modelo_app_crud_usuario_api.ui.feature.usuario.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioEditScreen(
    usuarioId: String?,
    onDone: () -> Unit,
    vm: UsuarioEditViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(usuarioId) { vm.load(usuarioId) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (state.isNew) "Novo usuário" else "Editar usuário") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            state.fotoUri?.let { uri ->
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = uri,
                    contentDescription = "Foto escolhida",
                    modifier = Modifier.size(96.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            PickersRow(
                fotoUri = state.fotoUri,
                anexosCount = state.anexosUris.size,
                onPickFoto = vm::onFotoPicked,
                onPickAnexos = vm::onAnexosPicked
            )

            OutlinedTextField(
                value = state.nome, onValueChange = vm::onNome,
                label = { Text("Nome") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = vm::onEmail,
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.cpf,
                onValueChange = vm::onCpf,
                label = { Text("CPF") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.senha, onValueChange = vm::onSenha,
                label = { Text(if (state.isNew) "Senha (obrigatória)" else "Senha (opcional)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { vm.save(usuarioId, onDone) }, enabled = !state.loading) {
                Text(if (state.loading) "Salvando..." else "Salvar")
            }
            state.error?.let {
                Spacer(Modifier.height(8.dp)); Text(
                it,
                color = MaterialTheme.colorScheme.error
            )
            }
        }
    }
}

@Composable
private fun PickersRow(
    fotoUri: Uri?,
    anexosCount: Int,
    onPickFoto: (Uri?) -> Unit,
    onPickAnexos: (List<Uri>) -> Unit
) {
    val pickFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onPickFoto(uri) }
    )
    val pickAnexos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris -> onPickAnexos(uris) }
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            pickFoto.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }) {
            Text(if (fotoUri == null) "Escolher foto" else "Trocar foto")
        }
        Button(onClick = { pickAnexos.launch(arrayOf("*/*")) }) {
            Text("Anexos ($anexosCount)")
        }
    }
}
