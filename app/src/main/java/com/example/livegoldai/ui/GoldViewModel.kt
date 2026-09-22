package com.example.livegoldai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livegoldai.R
import com.example.livegoldai.data.GoldApiService
import com.example.livegoldai.model.GoldAnalysisResult
import com.example.livegoldai.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MainScreenMode(
    val title: String,
    val hindiTitle: String,
    val badge: String,
    val icon: String
) {
    ALL(
        title = "ALL COCKPIT",
        hindiTitle = "Sabhi Feature",
        badge = "PRO",
        icon = "🎯"
    ),
    LIVE_MARKET(
        title = "LIVE STATUS",
        hindiTitle = "Abhi Kya Chal Raha Hai",
        badge = "REALTIME",
        icon = "🔴"
    ),
    PREDICTION(
        title = "AI PREDICTION",
        hindiTitle = "Agla Kya Hoga & Possibility",
        badge = "FORECAST",
        icon = "🔮"
    )
}

data class GoldUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val data: GoldAnalysisResult? = null,
    val selectedInterval: String = "4h",
    val selectedTab: Int = 0, // 0=All, 1=Trend, 2=Momentum, 3=Volatility, 4=S/R, 5=Candlestick
    val countdownSeconds: Int = 60,
    val errorMessage: String? = null,
    val apiKey: String = "8e1493529b8e42d9b0a9e557c3451db0",
    val priceAlertTarget: Double? = null,
    val isAlertTriggered: Boolean = false,
    val showLotCalculator: Boolean = false,
    val selectedSlPips: Double = 90.0,
    val showPriceAlertDialog: Boolean = false,
    val selectedLogoRes: Int = R.drawable.ic_vip_gold_crest,
    val showLogoSelectorDialog: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.DUBAI_ROYALE,
    val showThemeSelectorDialog: Boolean = false,
    val isCompactEasyView: Boolean = false,
    val mainScreenMode: MainScreenMode = MainScreenMode.ALL,
    val showPredictionDialog: Boolean = false
)

class GoldViewModel(
    private val apiService: GoldApiService = GoldApiService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoldUiState())
    val uiState: StateFlow<GoldUiState> = _uiState.asStateFlow()

    private var autoRefreshJob: Job? = null

    init {
        loadData(isInitial = true)
        startAutoRefreshLoop()
    }

    fun loadData(isInitial: Boolean = false) {
        viewModelScope.launch {
            if (isInitial) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            }

            val currentInterval = _uiState.value.selectedInterval
            val result = apiService.fetchAnalysis(interval = currentInterval)

            result.onSuccess { analysis ->
                val target = _uiState.value.priceAlertTarget
                val triggered = if (target != null) {
                    val p = analysis.currentPrice
                    kotlin.math.abs(p - target) <= 2.5
                } else false

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        data = analysis,
                        countdownSeconds = 60,
                        errorMessage = null,
                        isAlertTriggered = triggered || it.isAlertTriggered
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = err.localizedMessage ?: "Failed to update market data"
                    )
                }
            }
        }
    }

    fun setInterval(interval: String) {
        if (_uiState.value.selectedInterval == interval) return
        _uiState.update { it.copy(selectedInterval = interval, isLoading = true) }
        loadData(isInitial = true)
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun updateApiKey(newKey: String) {
        apiService.setApiKey(newKey)
        _uiState.update { it.copy(apiKey = newKey) }
        loadData(isInitial = false)
    }

    fun openLotCalculator(slPips: Double) {
        _uiState.update { it.copy(showLotCalculator = true, selectedSlPips = slPips) }
    }

    fun closeLotCalculator() {
        _uiState.update { it.copy(showLotCalculator = false) }
    }

    fun openPriceAlertDialog() {
        _uiState.update { it.copy(showPriceAlertDialog = true) }
    }

    fun closePriceAlertDialog() {
        _uiState.update { it.copy(showPriceAlertDialog = false) }
    }

    fun setPriceAlert(target: Double?) {
        _uiState.update { it.copy(priceAlertTarget = target, isAlertTriggered = false) }
    }

    fun dismissAlertBanner() {
        _uiState.update { it.copy(isAlertTriggered = false) }
    }

    fun openLogoSelector() {
        _uiState.update { it.copy(showLogoSelectorDialog = true) }
    }

    fun closeLogoSelector() {
        _uiState.update { it.copy(showLogoSelectorDialog = false) }
    }

    fun selectLogo(logoRes: Int) {
        _uiState.update { it.copy(selectedLogoRes = logoRes, showLogoSelectorDialog = false) }
    }

    fun openThemeSelector() {
        _uiState.update { it.copy(showThemeSelectorDialog = true) }
    }

    fun closeThemeSelector() {
        _uiState.update { it.copy(showThemeSelectorDialog = false) }
    }

    fun selectTheme(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode, showThemeSelectorDialog = false) }
    }

    fun toggleCompactEasyView() {
        _uiState.update { it.copy(isCompactEasyView = !it.isCompactEasyView) }
    }

    fun setMainScreenMode(mode: MainScreenMode) {
        _uiState.update { it.copy(mainScreenMode = mode) }
    }

    fun openPredictionDialog() {
        _uiState.update { it.copy(showPredictionDialog = true) }
    }

    fun closePredictionDialog() {
        _uiState.update { it.copy(showPredictionDialog = false) }
    }

    private fun startAutoRefreshLoop() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val currentSec = _uiState.value.countdownSeconds
                if (currentSec > 1) {
                    _uiState.update { it.copy(countdownSeconds = currentSec - 1) }
                } else {
                    _uiState.update { it.copy(countdownSeconds = 60) }
                    loadData(isInitial = false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
}
