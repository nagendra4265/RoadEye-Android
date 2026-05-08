package com.roadeye.ui.screens.complaint

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadeye.domain.model.Complaint
import com.roadeye.domain.model.ComplaintStatus
import com.roadeye.domain.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplaintDetailViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _complaint = MutableStateFlow<Complaint?>(null)
    val complaint: StateFlow<Complaint?> = _complaint.asStateFlow()

    fun loadComplaint(id: String) {
        viewModelScope.launch {
            complaintRepository.getComplaintById(id).collect { c ->
                _complaint.value = c
            }
        }
    }
}
