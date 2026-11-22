package com.example.smartmedicinebox.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartmedicinebox.data.Intake
import com.example.smartmedicinebox.data.Medicine
import com.example.smartmedicinebox.domain.BoxConnection
import com.example.smartmedicinebox.domain.BoxState
import com.example.smartmedicinebox.domain.MedicineRepository
import com.example.smartmedicinebox.domain.ReminderTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicineViewModel(
    private val repository: MedicineRepository,
    private val box: BoxConnection
) : ViewModel() {

    val medicines: StateFlow<List<Medicine>> =
        repository.medicines.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Newest first. */
    val history: StateFlow<List<Intake>> =
        repository.history.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val boxState: StateFlow<BoxState> = box.state

    /** Saves the medicine. Returns an error message to show, or null on success. */
    fun addMedicine(name: String, time: String, frequency: String): String? {
        if (name.isBlank() || time.isBlank() || frequency.isBlank()) {
            return "Veuillez remplir tous les champs."
        }
        if (!ReminderTime.isValid(time)) {
            return "Format incorrect. Exemple : 08:00"
        }

        viewModelScope.launch {
            repository.addMedicine(name.trim(), time, frequency.trim())
        }
        return null
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.deleteMedicine(medicine)
        }
    }

    fun connectBox() = box.connect()
    fun disconnectBox() = box.disconnect()
    fun sendTestReminder() = box.sendTestReminder()
}
