package dev.gmarques.compras.presenter.dialogos.lista_io

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.databinding.DialogAlternarListasBinding
import dev.gmarques.compras.databinding.ItemRvListaBinding
import dev.gmarques.compras.domain.entidades.Lista
import dev.gmarques.compras.presenter.Vibrador
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.FragListaDeCompras
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.FragListaDeComprasViewModel
import kotlinx.coroutines.launch

class AlternarListasDialog(
    private val fragListaDeCompras: FragListaDeCompras,
    private val viewModel: FragListaDeComprasViewModel,
    private val callback: (lista: Lista) -> Unit,
) {

    private lateinit var adapter: Adapter

    private val binding = DialogAlternarListasBinding.inflate(fragListaDeCompras.layoutInflater)
    private lateinit var dialog: BottomSheetDialog

    init {

        initDialogo()
        initToolbar()
        initRecyclerView()

    }

    private fun initDialogo() {
        dialog = BottomSheetDialog(fragListaDeCompras.requireContext())
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }


    private fun initRecyclerView() = fragListaDeCompras.lifecycleScope.launch {

        val listas = viewModel.getTodasAsListas()

        adapter = Adapter(listas)

        adapter.listaSelecionadaCallback = { listaSelecionada: Lista ->
            dialog.dismiss()
            Vibrador.vibInteracao()
            callback.invoke(listaSelecionada)
        }

        binding.rvListas.layoutManager = LinearLayoutManager(fragListaDeCompras.requireContext())
        binding.rvListas.adapter = adapter

    }

    private fun initToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            Vibrador.vibInteracao()
            dialog.dismiss()
        }

    }

    fun show() = dialog.show()

    inner class Adapter(val listas: List<Lista>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        lateinit var listaSelecionadaCallback: (lista: Lista) -> Unit

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): RecyclerView.ViewHolder {
            return Holder(ItemRvListaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indice: Int) =
                (holder as Holder).bind(indice)

        override fun getItemCount() = listas.size

        inner class Holder(val item: ItemRvListaBinding) : RecyclerView.ViewHolder(item.root) {
            fun bind(indice: Int) {
                item.tvItem.text = listas[indice].nome
                item.tvItem.setOnClickListener {
                    listaSelecionadaCallback.invoke(listas[indice])
                }
            }

        }
    }

}