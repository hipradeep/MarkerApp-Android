package com.hipradeep.markersapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hipradeep.markersapp.repsitories.FirebaseRepositories

class MainViewModelFactory(private val firebaseRepositories: FirebaseRepositories) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(firebaseRepositories) as T
    }
}