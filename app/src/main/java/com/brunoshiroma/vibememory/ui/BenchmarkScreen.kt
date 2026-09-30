package com.brunoshiroma.vibememory.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.brunoshiroma.vibememory.model.Measurement
import com.brunoshiroma.vibememory.model.SizeSelection
import com.brunoshiroma.vibememory.model.SizeUnit
import com.brunoshiroma.vibememory.model.formatSizeBytes

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BenchmarkScreen(viewModel: BenchmarkViewModel) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Memory & Cache Benchmark") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text("Tamanhos a testar", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            viewModel.sizes.forEachIndexed { index, selection ->
                SizeRow(
                    selection = selection,
                    onMagnitudeChange = { viewModel.updateMagnitude(index, it) },
                    onUnitChange = { viewModel.updateUnit(index, it) },
                    onRemove = { viewModel.removeSize(index) },
                )
            }
            TextButton(onClick = { viewModel.addSize() }) {
                Text("+ Adicionar tamanho")
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = viewModel.iterationsText,
                onValueChange = { viewModel.iterationsText = it },
                label = { Text("Iterações") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.runBenchmark() },
                enabled = !viewModel.isRunning,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (viewModel.isRunning) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Executar benchmark")
                }
            }

            viewModel.errorMessage?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(message, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            if (viewModel.results.isNotEmpty()) {
                Text("Resultados", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                ResultsTable(viewModel.results)
            }
        }
    }
}

@Composable
private fun SizeRow(
    selection: SizeSelection,
    onMagnitudeChange: (Long) -> Unit,
    onUnitChange: (SizeUnit) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        OutlinedTextField(
            value = selection.magnitude.toString(),
            onValueChange = { text -> text.toLongOrNull()?.let(onMagnitudeChange) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(96.dp),
        )
        Spacer(Modifier.width(8.dp))
        UnitDropdown(
            selected = selection.unit,
            onSelected = onUnitChange,
            modifier = Modifier.width(120.dp),
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onRemove) {
            Text("Remover")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    selected: SizeUnit,
    onSelected: (SizeUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Unidade") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SizeUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.label) },
                    onClick = {
                        onSelected(unit)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ResultsTable(results: List<Measurement>) {
    LazyColumn {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                HeaderCell("Operação", 1.4f)
                HeaderCell("Tamanho", 1f)
                HeaderCell("GB/s", 0.8f)
                HeaderCell("ns/elem", 0.8f)
            }
            HorizontalDivider()
        }
        items(results) { measurement ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Cell(measurement.operation, 1.4f)
                Cell(formatSizeBytes(measurement.sizeBytes), 1f)
                Cell("%.2f".format(measurement.gigabytesPerSecond), 0.8f)
                Cell("%.2f".format(measurement.nanosecondsPerElement), 0.8f)
            }
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(weight),
    )
}

@Composable
private fun RowScope.Cell(text: String, weight: Float) {
    Text(text = text, modifier = Modifier.weight(weight))
}
