package com.talla.mytracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.talla.mytracker.databinding.FragmentPermissionBinding
import com.talla.mytracker.utill.Permissions
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog


class PermissionFragment : Fragment(), EasyPermissions.PermissionCallbacks
{
  private lateinit var binding:FragmentPermissionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPermissionBinding.inflate(layoutInflater, container, false)

        binding.continueBtn.setOnClickListener {
            if (Permissions.hasLocationPermission(requireContext())){
                findNavController().navigate(R.id.action_permissionFragment_to_mapsFragment)
            }else{
               Permissions.reqLocationPermissions(this)
            }
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms))
        {
           SettingsDialog.Builder(requireActivity()).build().show()
        }else{
            Permissions.reqLocationPermissions(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        findNavController().navigate(R.id.action_permissionFragment_to_mapsFragment)
    }

}