package com.roadeye.ui.screens.roleselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadeye.domain.model.UserRole
import com.roadeye.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    fun saveRole(role: String) {
        viewModelScope.launch {
            userRepository.saveUserRole(UserRole.valueOf(role))
        }
    }
}
