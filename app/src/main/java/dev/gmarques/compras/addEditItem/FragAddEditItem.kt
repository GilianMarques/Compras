package dev.gmarques.compras.addEditItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.FragmentAddEditItemBinding
import dev.gmarques.compras.objetos.Item

class FragAddEditItem : Fragment() {

    private lateinit var viewModel: AddEditItemViewModel
    private lateinit var binding: FragmentAddEditItemBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddEditItemBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = listOf("Material", "Design", "Components", "Android")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        (binding.textFieldNome.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        val args: FragAddEditItemArgs = FragAddEditItemArgs.fromBundle(requireArguments())

        val item = args.item
        binding.textFieldNome.editText!!.setText(item.nome)

        viewModel = ViewModelProvider(this)[AddEditItemViewModel::class.java]
    }

}