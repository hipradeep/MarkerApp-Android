package com.hipradeep.markersapp.repsitories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.hipradeep.markersapp.models.Coords


class FirebaseRepositories() {

     lateinit var dataList: MutableLiveData<List<Coords>>
     lateinit var mFirestore: DatabaseReference

     //accessing the coordinates from cloud
    fun getDataFromFireStore(): MutableLiveData<List<Coords>> {
        dataList = MutableLiveData<List<Coords>>()
        mFirestore = FirebaseDatabase.getInstance().getReference("coords")
        mFirestore.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val blogList: MutableList<Coords> = ArrayList()
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(Coords::class.java)
                        Log.e("tag", "11" + user.toString())
                        blogList.add(user!!);
                    }
                } else {
                    Log.e("tag", "12" + snapshot.toString())
                }
                dataList.postValue(blogList);

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("tag", "13" + error.toString())
            }

        })

        return dataList;
    }


}