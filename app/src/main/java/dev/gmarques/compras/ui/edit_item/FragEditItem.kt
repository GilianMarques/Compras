package dev.gmarques.compras.ui.edit_item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.FragEditItemBinding
import dev.gmarques.compras.io.repositorios.ItemRepo
import kotlinx.coroutines.launch

class FragEditItem : Fragment() {

    private lateinit var viewModel: EditItemViewModel
    private lateinit var binding: FragEditItemBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragEditItemBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EditItemViewModel::class.java]

        val args: FragEditItemArgs = FragEditItemArgs.fromBundle(requireArguments())
        viewModel.item = args.item


        initEdtNome()
        binding.fabConcluir.setOnClickListener {
            viewModel.verificarEntradasEFechar()
        }
    }

    private fun initEdtNome() {
        binding.edtNome.addTextChangedListener { it ->

            if (it?.isNotEmpty() == true) lifecycleScope.launch {
                val items = ItemRepo.getItens(it.toString())
                val nomes = ArrayList<String>()
                items.forEach { nomes.add(it.nome) }
                val adapter = ArrayAdapter(requireContext(), R.layout.list_item, nomes)
                (binding.textFieldNome.editText as? AutoCompleteTextView)?.setAdapter(adapter)
            }
        }
    }

}