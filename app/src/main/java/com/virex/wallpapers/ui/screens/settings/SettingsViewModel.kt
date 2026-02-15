package com.virex.wallpapers.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virex.wallpapers.data.local.AppLanguage
import com.virex.wallpapers.data.local.LocaleHelper
import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.model.BillingState
import com.virex.wallpapers.data.repository.BillingRepository
import com.virex.wallpapers.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Settings ViewModel */
@HiltViewModel
class SettingsViewModel
@Inject
constructor(
        private val repository: WallpaperRepository,
        private val billingRepository: BillingRepository,
        private val preferencesDataStore: PreferencesDataStore,
        private val localeHelper: LocaleHelper
) : ViewModel() {

    val isPro: StateFlow<Boolean> =
            preferencesDataStore.isPro.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val billingState: StateFlow<BillingState> = billingRepository.billingState

    val cacheSize: StateFlow<Long> =
            repository.getCacheSize().stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing.asStateFlow()

    private val _currentLanguage = MutableStateFlow(localeHelper.getCurrentLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    val availableLanguages: List<AppLanguage> = localeHelper.getAvailableLanguages()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    private var restoreJob: Job? = null

    fun clearCache() {
        viewModelScope.launch {
            _isClearing.value = true
            try {
                repository.clearCache()
                _message.emit("Cache cleared successfully")
            } catch (e: Exception) {
                _message.emit("Failed to clear cache")
            } finally {
                _isClearing.value = false
            }
        }
    }

    fun restorePurchases() {
        restoreJob?.cancel()
        billingRepository.restorePurchases()
        restoreJob =
                viewModelScope.launch {
                    billingRepository
                            .billingState
                            .dropWhile { it is BillingState.Loading }
                            .first()
                            .let { state ->
                                when (state) {
                                    is BillingState.Purchased -> {
                                        _message.emit("Purchase restored successfully!")
                                    }
                                    is BillingState.NotPurchased -> {
                                        _message.emit("No previous purchase found")
                                    }
                                    is BillingState.Error -> {
                                        _message.emit(
                                                state.message.ifBlank { "Ошибка восстановления" }
                                        )
                                    }
                                    else -> {}
                                }
                            }
                }
    }

    fun getFormattedCacheSize(): String {
        val size = cacheSize.value
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }

    /** Show language selection dialog */
    fun showLanguageDialog() {
        _showLanguageDialog.value = true
    }

    /** Hide language selection dialog */
    fun hideLanguageDialog() {
        _showLanguageDialog.value = false
    }

    /** Set app language */
    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            localeHelper.setLanguage(language)
            _currentLanguage.value = language
            hideLanguageDialog()
        }
    }

    /** DEBUG: Toggle PRO status for testing */
    fun debugTogglePro() {
        viewModelScope.launch {
            val currentPro = isPro.value
            preferencesDataStore.setProStatus(!currentPro)
            if (!currentPro) {
                _message.emit("🔓 PRO UNLOCKED (Debug Mode)")
            } else {
                _message.emit("🔒 PRO LOCKED (Debug Mode)")
            }
        }
    }
}
