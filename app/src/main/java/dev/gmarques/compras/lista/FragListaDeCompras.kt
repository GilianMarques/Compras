package dev.gmarques.compras.lista

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.gmarques.compras.databinding.FragmentFragListaDeComprasBinding

class FragListaDeCompras : Fragment() {


    private lateinit var viewModel: FragListaDeComprasViewModel
    private lateinit var binding: FragmentFragListaDeComprasBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFragListaDeComprasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FragListaDeComprasViewModel::class.java]
        viewModel.init()

        Log.d("FragListaDeCompras", "onViewCreated: ")

    }


}