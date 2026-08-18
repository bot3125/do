package com.example.myapplicationwd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = MarketRepository()

    // Estados para los valores mínimos y máximos dinámicos
    private val _dolarMinimo = MutableStateFlow(3.52)
    val dolarMinimo: StateFlow<Double> = _dolarMinimo.asStateFlow()

    private val _dolarMaximo = MutableStateFlow(4.14)
    val dolarMaximo: StateFlow<Double> = _dolarMaximo.asStateFlow()

    private val _wldMaximo = MutableStateFlow(11.90)
    val wldMaximo: StateFlow<Double> = _wldMaximo.asStateFlow()

    init {
        cargarLimitesHistoricos()
    }

    // Descarga y actualiza los valores reales de forma automática
    fun cargarLimitesHistoricos() {
        viewModelScope.launch {
            val usdData = repository.getUsdPenMinMax()
            usdData?.let {
                _dolarMinimo.value = String.format("%.2f", it.first).toDouble()
                _dolarMaximo.value = String.format("%.2f", it.second).toDouble()
            }

            val wldData = repository.getWldMinMax()
            wldData?.let {
                _wldMaximo.value = String.format("%.2f", it.second).toDouble()
            }
        }
    }
}