package com.talla.mytracker.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.talla.mytracker.R
import com.talla.mytracker.utill.Permissions

class MainActivity : AppCompatActivity()
{
  private lateinit var navController:NavController

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController=findNavController(R.id.navController)
        defaultCheck()
    }

    fun defaultCheck(){
       if (Permissions.hasLocationPermission(this)){
           navController.navigate(R.id.action_permissionFragment_to_mapsFragment)
       }
    }

}