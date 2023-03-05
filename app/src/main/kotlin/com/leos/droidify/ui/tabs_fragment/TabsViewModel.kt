package com.leos.droidify.ui.tabs_fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leos.core.datastore.UserPreferencesRepository
import com.leos.core.datastore.distinctMap
import com.leos.core.datastore.model.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabsViewModel @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

	val sortOrderFlow = userPreferencesRepository
		.userPreferencesFlow
		.distinctMap { it.sortOrder }

	fun setSortOrder(sortOrder: SortOrder) {
		viewModelScope.launch {
			userPreferencesRepository.setSortOrder(sortOrder)
		}
	}
}