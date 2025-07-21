/**
 * @file: CheckoutViewModel.kt
 * @description: ViewModel для экрана оформления заказа
 * @dependencies: Hilt, ViewModel, Use Cases, Coroutines
 * @created: 2024-12-19
 */
package com.pizzanat.app.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizzanat.app.domain.entities.SimpleAddressSuggestion
import com.pizzanat.app.domain.entities.CartItem
import com.pizzanat.app.domain.usecases.address.GetAddressSuggestionsUseCase
import com.pizzanat.app.domain.usecases.cart.GetCartItemsUseCase
import com.pizzanat.app.domain.usecases.user.GetUserDefaultDataUseCase
import com.pizzanat.app.domain.usecases.user.SaveUserPreferencesUseCase
import com.pizzanat.app.presentation.components.isValidPhoneNumber
import com.pizzanat.app.presentation.components.normalizePhoneForApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val totalItems: Int = 0,
    val deliveryAddress: String = "",
    val customerPhone: String = "+7",
    val customerName: String = "",
    val notes: String = "",
    val addressError: String? = null,
    val phoneError: String? = null,
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Новые поля для подсказок адресов
    val addressSuggestions: List<SimpleAddressSuggestion> = emptyList(),
    val isLoadingAddressSuggestions: Boolean = false,
    val addressSuggestionsError: String? = null
)

/**
 * Данные заказа для передачи в PaymentScreen
 */
data class OrderData(
    val cartItems: List<CartItem>,
    val totalPrice: Double,
    val deliveryAddress: String,
    val customerPhone: String,
    val customerName: String,
    val notes: String
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val getAddressSuggestionsUseCase: GetAddressSuggestionsUseCase,
    private val getUserDefaultDataUseCase: GetUserDefaultDataUseCase,
    private val saveUserPreferencesUseCase: SaveUserPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()
    
    // Сохраненные данные заказа для передачи в PaymentScreen
    private var _savedOrderData: OrderData? = null
    val savedOrderData: OrderData? get() = _savedOrderData

    init {
        loadCartItems()
        loadUserDefaultData()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getCartItemsUseCase().collect { cartItems ->
                val totalPrice = cartItems.sumOf { it.totalPrice }
                val totalItems = cartItems.sumOf { it.quantity }
                
                _uiState.value = _uiState.value.copy(
                    cartItems = cartItems,
                    totalPrice = totalPrice,
                    totalItems = totalItems,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Загрузка данных пользователя по умолчанию для автозаполнения
     */
    private fun loadUserDefaultData() {
        viewModelScope.launch {
            try {
                android.util.Log.d("CheckoutViewModel", "🔄 Загрузка данных пользователя для автозаполнения...")
                
                val currentState = _uiState.value
                android.util.Log.d("CheckoutViewModel", "📋 Текущее состояние полей:")
                android.util.Log.d("CheckoutViewModel", "  Текущий адрес: '${currentState.deliveryAddress}'")
                android.util.Log.d("CheckoutViewModel", "  Текущий телефон: '${currentState.customerPhone}'")
                android.util.Log.d("CheckoutViewModel", "  Текущее имя: '${currentState.customerName}'")
                
                val result = getUserDefaultDataUseCase()
                if (result.isSuccess) {
                    val defaultData = result.getOrNull()
                    if (defaultData != null) {
                        android.util.Log.d("CheckoutViewModel", "✅ Полученные данные по умолчанию:")
                        android.util.Log.d("CheckoutViewModel", "  Имя: '${defaultData.customerName}'")
                        android.util.Log.d("CheckoutViewModel", "  Телефон: '${defaultData.customerPhone}'")
                        android.util.Log.d("CheckoutViewModel", "  Адрес: '${defaultData.deliveryAddress}'")
                        
                        // Условия автозаполнения
                        val shouldFillAddress = currentState.deliveryAddress.isBlank() && defaultData.deliveryAddress.isNotBlank()
                        val shouldFillPhone = (currentState.customerPhone.isBlank() || currentState.customerPhone == "+7") && defaultData.customerPhone.isNotBlank() && defaultData.customerPhone != "+7"
                        val shouldFillName = currentState.customerName.isBlank() && defaultData.customerName.isNotBlank()
                        
                        android.util.Log.d("CheckoutViewModel", "🔍 Условия заполнения:")
                        android.util.Log.d("CheckoutViewModel", "  Заполнить адрес: $shouldFillAddress")
                        android.util.Log.d("CheckoutViewModel", "  Заполнить телефон: $shouldFillPhone")
                        android.util.Log.d("CheckoutViewModel", "  Заполнить имя: $shouldFillName")
                        
                        // Обновляем состояние только если поля пустые
                        _uiState.value = _uiState.value.copy(
                            deliveryAddress = if (shouldFillAddress) defaultData.deliveryAddress else currentState.deliveryAddress,
                            customerPhone = if (shouldFillPhone) defaultData.customerPhone else currentState.customerPhone,
                            customerName = if (shouldFillName) defaultData.customerName else currentState.customerName
                        )
                        
                        val finalState = _uiState.value
                        android.util.Log.d("CheckoutViewModel", "🎯 Финальное состояние полей:")
                        android.util.Log.d("CheckoutViewModel", "  Итоговый адрес: '${finalState.deliveryAddress}'")
                        android.util.Log.d("CheckoutViewModel", "  Итоговый телефон: '${finalState.customerPhone}'")
                        android.util.Log.d("CheckoutViewModel", "  Итоговое имя: '${finalState.customerName}'")
                        
                    } else {
                        android.util.Log.w("CheckoutViewModel", "⚠️ Данные по умолчанию пустые")
                    }
                } else {
                    android.util.Log.w("CheckoutViewModel", "⚠️ Ошибка загрузки данных по умолчанию: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("CheckoutViewModel", "❌ Исключение при автозаполнении: ${e.message}")
                // Не критично - форма остается с пустыми полями
            }
        }
    }

    fun updateDeliveryAddress(address: String) {
        _uiState.value = _uiState.value.copy(
            deliveryAddress = address,
            addressError = null
        )
        
        // Сохраняем адрес как предпочтение пользователя (с задержкой чтобы не спамить)
        if (address.isNotBlank() && address.length > 10) {
            viewModelScope.launch {
                saveUserPreferencesUseCase.saveLastDeliveryAddress(address)
            }
        }
    }

    fun updateCustomerPhone(phone: String) {
        _uiState.value = _uiState.value.copy(
            customerPhone = phone,
            phoneError = null
        )
        
        // Сохраняем телефон как предпочтение пользователя
        if (phone.isNotBlank() && phone != "+7" && phone.length >= 10) {
            viewModelScope.launch {
                saveUserPreferencesUseCase.saveLastCustomerPhone(phone)
            }
        }
    }

    fun updateCustomerName(name: String) {
        _uiState.value = _uiState.value.copy(
            customerName = name,
            nameError = null
        )
        
        // Сохраняем имя как предпочтение пользователя
        if (name.isNotBlank() && name.length >= 2) {
            viewModelScope.launch {
                saveUserPreferencesUseCase.saveLastCustomerName(name)
            }
        }
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    /**
     * Валидирует поля формы
     * @return true если все поля валидны
     */
    fun validateFields(): Boolean {
        return validateFieldsInternal()
    }
    
    /**
     * Внутренняя валидация полей формы
     */
    private fun validateFieldsInternal(): Boolean {
        var isValid = true
        val currentState = _uiState.value
        
        val addressError = when {
            currentState.deliveryAddress.isBlank() -> "Адрес доставки обязателен"
            currentState.deliveryAddress.length < 10 -> "Адрес слишком короткий"
            else -> null
        }
        
        val phoneError = when {
            currentState.customerPhone.isBlank() || currentState.customerPhone == "+7" -> "Введите номер телефона"
            !isValidPhoneNumber(currentState.customerPhone) -> "Введите корректный номер телефона"
            else -> null
        }
        
        val nameError = when {
            currentState.customerName.isBlank() -> "Имя получателя обязательно"
            currentState.customerName.length < 2 -> "Имя слишком короткое"
            else -> null
        }
        
        if (addressError != null || phoneError != null || nameError != null) {
            isValid = false
        }
        
        _uiState.value = _uiState.value.copy(
            addressError = addressError,
            phoneError = phoneError,
            nameError = nameError
        )
        
        return isValid
    }
    
    /**
     * Сохраняет данные заказа для передачи в PaymentScreen
     */
    fun saveOrderData() {
        val currentState = _uiState.value
        _savedOrderData = OrderData(
            cartItems = currentState.cartItems,
            totalPrice = currentState.totalPrice,
            deliveryAddress = currentState.deliveryAddress,
            customerPhone = normalizePhoneForApi(currentState.customerPhone),
            customerName = currentState.customerName,
            notes = currentState.notes
        )
        
        // Сохраняем финальные данные заказа как предпочтения пользователя
        viewModelScope.launch {
            android.util.Log.d("CheckoutViewModel", "💾 Сохраняем финальные данные заказа как предпочтения")
            saveUserPreferencesUseCase.saveOrderData(
                deliveryAddress = currentState.deliveryAddress,
                customerPhone = currentState.customerPhone,
                customerName = currentState.customerName
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Получение подсказок адресов на основе введенного текста
     */
    fun getAddressSuggestions(query: String) {
        if (query.length < 3) {
            _uiState.value = _uiState.value.copy(
                addressSuggestions = emptyList(),
                isLoadingAddressSuggestions = false,
                addressSuggestionsError = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingAddressSuggestions = true,
                addressSuggestionsError = null
            )

            getAddressSuggestionsUseCase(query)
                .onSuccess { suggestions ->
                    _uiState.value = _uiState.value.copy(
                        addressSuggestions = suggestions,
                        isLoadingAddressSuggestions = false,
                        addressSuggestionsError = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        addressSuggestions = emptyList(),
                        isLoadingAddressSuggestions = false,
                        addressSuggestionsError = error.message
                    )
                }
        }
    }

    /**
     * Выбор подсказки адреса
     */
    fun selectAddressSuggestion(suggestion: SimpleAddressSuggestion) {
        _uiState.value = _uiState.value.copy(
            deliveryAddress = suggestion.shortAddress,
            addressSuggestions = emptyList(),
            addressError = null,
            addressSuggestionsError = null
        )
    }

    /**
     * Очистка подсказок адресов
     */
    fun clearAddressSuggestions() {
        _uiState.value = _uiState.value.copy(
            addressSuggestions = emptyList(),
            isLoadingAddressSuggestions = false,
            addressSuggestionsError = null
        )
    }
    
    /**
     * Принудительное обновление данных по умолчанию (для кнопки "Заполнить автоматически")
     */
    fun reloadUserDefaultData() {
        android.util.Log.d("CheckoutViewModel", "🔄 Принудительное обновление автозаполнения...")
        loadUserDefaultData()
    }
} 