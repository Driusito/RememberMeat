package com.example.remembermeat.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.remembermeat.daysLeft
import com.example.remembermeat.entity.Product
import com.example.remembermeat.expiryText
import com.example.remembermeat.formatPrice
import com.example.remembermeat.ui.theme.Sticker
import com.example.remembermeat.ui.theme.StickerInk
import com.example.remembermeat.ui.theme.urgencyColor
import com.example.remembermeat.viewmodel.ProductViewModel
import java.io.File

@Composable
fun AppRoot(viewModel: ProductViewModel = viewModel()) {
    var showForm by rememberSaveable { mutableStateOf(false) }

    if (showForm) {
        ProductFormScreen(
            onSave = { product ->
                viewModel.add(product)
                showForm = false
            },
            onCancel = { showForm = false }
        )
    } else {
        ProductListScreen(
            onAddClick = { showForm = true },
            viewModel = viewModel
        )
    }
}

@Composable
fun ProductListScreen(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = viewModel()
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (products.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Text("Apuntar producto") }
            }
        }
    ) { padding ->
        if (products.isEmpty()) {
            EmptyState(onAddClick, Modifier.padding(padding))
        } else {
            // la lista ya llega ordenada por caducidad, así que los grupos salen en orden
            val groups = products.groupBy { sectionOf(daysLeft(it.expiryDate)) }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                item { Header(products.first()) }
                groups.forEach { (title, group) ->
                    item(key = "section-$title") { SectionTitle(title) }
                    items(group, key = { it.id }) { product ->
                        SwipeRow(product, onRequestDelete = { toDelete = product })
                    }
                }
            }
        }
    }

    toDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Borrar ${product.name}") },
            text = { Text("Se eliminará también su foto. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(product)
                    toDelete = null
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

private fun sectionOf(days: Long) = when {
    days < 0L -> "Caducados"
    days <= 1L -> "Hoy y mañana"
    days <= 7L -> "Esta semana"
    else -> "Más adelante"
}

@Composable
private fun Header(next: Product) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text("Contrarreloj", style = MaterialTheme.typography.headlineMedium)
        Text(
            "${next.name}: ${expiryText(daysLeft(next.expiryDate)).lowercase()}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeRow(product: Product, onRequestDelete: () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onRequestDelete()
            false // la tarjeta vuelve a su sitio; el borrado real lo confirma el diálogo
        }
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    "Borrar",
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    ) { ProductCard(product) }
}

@Composable
private fun EmptyState(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Aún no has apuntado nada", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Cuando veas algo rebajado por caducidad, apúntalo con su fecha y te avisamos el día antes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddClick) { Text("Apuntar producto") }
    }
}

@Composable
fun ProductCard(product: Product) {
    val days = daysLeft(product.expiryDate)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            DaysBadge(days)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                product.photoPath?.let { path ->
                    AsyncImage(
                        model = File(path),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        product.store,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PriceSticker(product)
            }
        }
    }
}

@Composable
private fun DaysBadge(days: Long) {
    val (top, bottom) = when {
        days < 0L -> "Caducó" to null
        days == 0L -> "Hoy" to null
        days == 1L -> "Mañana" to null
        else -> days.toString() to "días"
    }
    Column(
        modifier = Modifier
            .width(76.dp)
            .fillMaxHeight()
            .background(urgencyColor(days)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            top,
            color = Color.White,
            style = if (bottom == null) MaterialTheme.typography.labelLarge
            else MaterialTheme.typography.headlineMedium
        )
        bottom?.let {
            Text(
                it,
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun PriceSticker(product: Product) {
    Text(
        formatPrice(product.priceCents),
        style = MaterialTheme.typography.titleMedium,
        color = StickerInk,
        modifier = Modifier
            .rotate(-3f)
            .background(Sticker, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}