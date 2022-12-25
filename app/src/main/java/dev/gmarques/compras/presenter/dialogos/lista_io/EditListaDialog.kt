package dev.gmarques.compras.presenter.dialogos.lista_io

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.Extensions.mostrarTeclado
import dev.gmarques.compras.Extensions.ocultarTeclado
import dev.gmarques.compras.R
import dev.gmarques.compras.data.repositorios.ListaRepo
import dev.gmarques.compras.databinding.DialogEditListaBinding
import dev.gmarques.compras.domain.ConvencaoNome.formatarComoNomeValido
import dev.gmarques.compras.domain.entidades.Lista
import dev.gmarques.compras.presenter.Vibrador
import kotlinx.coroutines.runBlocking

class EditListaDialog(
    private val fragment: Fragment,
    private val listaOriginal: Lista,
    private val callback: (lista: Lista) -> Unit,
) {

    @Suppress("JoinDeclarationAndAssignment")
    private val binding: DialogEditListaBinding
    private val dialog: BottomSheetDialog
    private val lista = listaOriginal.clonar()

    init {
        binding = DialogEditListaBinding.inflate(fragment.layoutInflater)

        dialog = BottomSheetDialog(fragment.requireContext())
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        initToolbar()
        initBotoes()
        binding.edtNome.setText(listaOriginal.nome)
        binding.edtNome.mostrarTeclado()

    }

    private fun initBotoes() {

        binding.btnSalvar.setOnClickListener {
            salvarLista()
        }


        binding.btnCancelar.setOnClickListener {
            binding.edtNome.ocultarTeclado()
            dialog.dismiss()

        }


        binding.btnRemover.setOnClickListener {
            confirmarRemocaoDaLista()
        }

    }


    fun show() = dialog.show()

    private fun initToolbar() {

        binding.toolbar.title =
            String.format(fragment.getString(R.string.Editar_x), listaOriginal.nome)
        binding.toolbar.setNavigationOnClickListener {
            Vibrador.vibInteracao()
            dialog.dismiss()
        }

    }

    private fun confirmarRemocaoDaLista() {


    }


    private fun salvarLista() {
        val nome = binding.edtNome.text.toString().formatarComoNomeValido()

        if (nome.isEmpty()) notificarErro(R.string.Digite_um_nome_valido)
        else if (listaRepetida(nome)) notificarErro(R.string.Essa_lista_ja_existe_mude)
        else runBlocking {
            binding.edtNome.ocultarTeclado()
            lista.nome = nome
            dialog.dismiss()
            callback(lista)
        }


    }

    private fun listaRepetida(nome: String): Boolean = runBlocking {
        return@runBlocking if (nome == listaOriginal.nome) false
        else ListaRepo.getListaPorNome(nome) != null
    }

    private fun notificarErro(mensagem: Int) {
        Toast.makeText(fragment.context, fragment.getString(mensagem), Toast.LENGTH_LONG).show()
        Vibrador.vibErro()
    }


}