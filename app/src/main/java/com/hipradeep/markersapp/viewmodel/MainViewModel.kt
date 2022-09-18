package com.hipradeep.markersapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.hipradeep.markersapp.models.Coords
import com.hipradeep.markersapp.repsitories.FirebaseRepositories

class MainViewModel(private val firebaseRepositories: FirebaseRepositories) : ViewModel() {


    //get Coordinates form Repository into view model to observe
    fun getCoords(): LiveData<List<Coords>> {
        return firebaseRepositories.getDataFromFireStore()
    }



}