package com.brunoshiroma.vibememory.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brunoshiroma.vibememory.bridge.NativeBenchmark
import com.brunoshiroma.vibememory.model.Measurement
import com.brunoshiroma.vibememory.model.SizeSelection
import com.brunoshiroma.vibememory.model.SizeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Holds the working-set size selections and drives the native benchmark run. */
class BenchmarkViewModel : ViewModel() {

    var sizes by mutableStateOf(SizeSelection.DEFAULTS)
        private set

    var iterationsText by mutableStateOf("20")

    var isRunning by mutableStateOf(false)
        private set

    var results by mutableStateOf<List<Measurement>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun addSize() {
        sizes = sizes + SizeSelection(1, SizeUnit.MIB)
    }

    fun removeSize(index: Int) {
        sizes = sizes.toMutableList().apply { removeAt(index) }
    }

    fun updateMagnitude(index: Int, magnitude: Long) {
        sizes = sizes.toMutableList().apply { this[index] = this[index].copy(magnitude = magnitude) }
    }

    fun updateUnit(index: Int, unit: SizeUnit) {
        sizes = sizes.toMutableList().apply { this[index] = this[index].copy(unit = unit) }
    }

    fun runBenchmark() {
        if (isRunning) return

        val iterations = iterationsText.toIntOrNull()
        if (iterations == null || iterations <= 0) {
            errorMessage = "Informe um número de iterações maior que zero."
            return
        }
        if (sizes.isEmpty()) {
            errorMessage = "Selecione ao menos um tamanho para testar."
            return
        }
        val invalidSize = sizes.firstOrNull { !it.isValid }
        if (invalidSize != null) {
            errorMessage = "Tamanho inválido: ${invalidSize.label} (deve ser múltiplo de 8 bytes)."
            return
        }

        errorMessage = null
        isRunning = true
        val sizesBytes = sizes.map { it.bytes }.toLongArray()
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.Default) {
                    NativeBenchmark.runBenchmark(sizesBytes, iterations)
                }
                results = Measurement.parseList(json)
            } catch (error: Exception) {
                results = emptyList()
                errorMessage = error.message ?: "Erro desconhecido ao executar o benchmark."
            } finally {
                isRunning = false
            }
        }
    }
}
