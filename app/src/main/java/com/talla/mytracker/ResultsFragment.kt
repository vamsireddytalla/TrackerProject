package com.talla.mytracker

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.talla.mytracker.databinding.FragmentResultsBinding

class ResultsFragment : BottomSheetDialogFragment() {
    private val args: ResultsFragmentArgs by navArgs()
    private lateinit var binding: FragmentResultsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentResultsBinding.inflate(inflater, container, false)

        binding.distanceTravelled.text = getString(R.string.result, args.result.distance)
        Toast.makeText(requireContext(), args.result.distance.toString(), Toast.LENGTH_SHORT).show()
        binding.timeValue.text = args.result.time


        binding.shareButton.setOnClickListener {
            shareData()
        }

        return binding.root
    }

    private fun shareData() {
        val share_intent=Intent().apply {
            action=Intent.ACTION_SEND
            type="text/plain"
            putExtra(Intent.EXTRA_TEXT,"I went ${args.result.distance} km in ${args.result.time}")
        }
        startActivity(share_intent)
    }

}