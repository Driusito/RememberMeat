package com.example.remembermeat.view

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.example.remembermeat.entity.Product
import com.example.remembermeat.parseCents
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    onSave: (Product) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var store by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var originalPrice by rememberSaveable { mutableStateOf("") }
    var expiryEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // --- cámara ---
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            photoPath?.let { File(it).delete() }
            photoPath = pendingPath
        } else {
            pendingPath?.let { File(it).delete() }
        }
        pendingPath = null
    }

    fun takePhoto() {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(dir, "IMG_${System.currentTimeMillis()}.jpg")
        pendingPath = file.absolutePath
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        camera.launch(uri)
    }

    fun cancel() {
        photoPath?.let { File(it).delete() }
        onCancel()
    }

    BackHandler { cancel() }

    val priceCents = parseCents(price)
    val originalCents = parseCents(originalPrice)
    val day = expiryEpochDay
    val canSave = name.isNotBlank() && store.isNotBlank() &&
            priceCents != null && day != null &&
            (originalPrice.isBlank() || (originalCents ?: 0) > 0)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nuevo producto", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Producto") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = store, onValueChange = { store = it },
                label = { Text("Tienda") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price, onValueChange = { price = it },
                    label = { Text("Precio (€)") },
                    isError = price.isNotBlank() && priceCents == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = originalPrice, onValueChange = { originalPrice = it },
                    label = { Text("Precio original") },
                    isError = originalPrice.isNotBlank() && (originalCents ?: 0) <= 0,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    day?.let {
                        "Caduca: " + LocalDate.ofEpochDay(it)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    } ?: "Elegir fecha de caducidad"
                )
            }

            photoPath?.let { path ->
                AsyncImage(
                    model = File(path),
                    contentDescription = "Foto del producto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            OutlinedButton(onClick = { takePhoto() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (photoPath == null) "Hacer foto" else "Repetir foto")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { cancel() }, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                Button(
                    enabled = canSave,
                    onClick = {
                        if (priceCents != null && day != null) {
                            onSave(
                                Product(
                                    name = name.trim(),
                                    store = store.trim(),
                                    priceCents = priceCents,
                                    originalPriceCents = if (originalPrice.isBlank()) null else originalCents,
                                    expiryDate = LocalDate.ofEpochDay(day),
                                    photoPath = photoPath
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Guardar") }
            }
        }
    }

    if (showDatePicker) {
        val dayMillis = 86_400_000L
        val state = rememberDatePickerState(
            initialSelectedDateMillis = expiryEpochDay?.let { it * dayMillis },
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis >= LocalDate.now().toEpochDay() * dayMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { expiryEpochDay = it / dayMillis }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = state) }
    }
}