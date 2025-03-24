package dev.gmarques.compras.ui.stablishments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import dev.gmarques.compras.ui.MyActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.databinding.ActivityEstablishmentsBinding
import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.add_edit_establishment.AddEditEstablishmentActivity

class EstablishmentsActivity: MyActivity(), EstablishmentAdapter.Callback {

    private var fabHidden: Boolean = false
    private var selectionMode: Boolean = false
    private lateinit var binding: ActivityEstablishmentsBinding
    private lateinit var viewModel: ActivityEstablishmentsViewModel
    private lateinit var adapter: EstablishmentAdapter

    companion object {

        const val SELECTION_MODE = "selection_mode"
        const val SELECTED_ESTABLISHMENT = "selected_establishment"

        fun newIntent(context: Context, selectionMode: Boolean = false): Intent {
            return Intent(context, EstablishmentsActivity::class.java).apply {
                putExtra(SELECTION_MODE, selectionMode)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEstablishmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ActivityEstablishmentsViewModel::class.java]
        selectionMode = intent.getBooleanExtra(SELECTION_MODE, false)
        setupToolbar()
        setupRecyclerview()
        observeEstablishments()
        setupFabAddEstablishment()
        observeViewmodelErrorMessages()
    }

    /**
     * Configura a toolbar da activity.
     */
    private fun setupToolbar() = binding.toolbar.apply {
        tvActivityTitle.text =
            if (selectionMode) getString(R.string.Selecionar_estabelecimento) else getString(R.string.Gerenciar_estabelecimentos)
        ivGoBack.setOnClickListener { Vibrator.interaction();finish() }
        ivMenu.visibility = GONE
    }

    private fun setupFabAddEstablishment() = binding.apply {
        fabAdd.setOnClickListener {
            startActivityAddEstablishment()
        }



        rv.addOnScrollListener(object : OnScrollListener() {


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {  // Rolando para cima - Esconde o FAB
                    if (fabHidden) return

                    fabAdd.animate().translationY(fabAdd.height.toFloat() * 2).alpha(0f).setStartDelay(100).setDuration(200L)
                        .start()
                    fabHidden = true

                } else if (dy < 0) { // Rolando para baixo - Mostra o FAB
                    if (!fabHidden) return

                    fabAdd.animate().translationY(0f).alpha(1f).setStartDelay(100).setDuration(200L).start()
                    fabHidden = false
                }
            }
        })
    }

    private fun observeEstablishments() {
        viewModel.establishmentsLd.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun setupRecyclerview() {

        adapter = EstablishmentAdapter(this)

        val dragDropHelper = EstablishmentDragDropHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(dragDropHelper)
        adapter.attachItemTouchHelper(touchHelper)

        touchHelper.attachToRecyclerView(binding.rv)

        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(this@EstablishmentsActivity)


    }

    private fun observeViewmodelErrorMessages() {
        viewModel.errorEventLD.observe(this@EstablishmentsActivity) { event ->
            Snackbar.make(binding.root, event, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun startActivityAddEstablishment() {

        Vibrator.interaction()
        val intent = AddEditEstablishmentActivity.newIntentAddEstablishment(this@EstablishmentsActivity)
        startActivity(intent)
    }


    override fun rvEstablishmentsOnDragAndDrop(toPosition: Int, establishment: Establishment) {
        viewModel.updateEstablishmentPosition(establishment,toPosition)
    }

    override fun rvEstablishmentsOnEditItemClick(establishment: Establishment) {
        Vibrator.interaction()
        val intent = AddEditEstablishmentActivity.newIntentEditEstablishment(this@EstablishmentsActivity, establishment.id)
        startActivity(intent)
    }

    override fun rvEstablishmentsOnSelect(establishment: Establishment) {
        if (!selectionMode) return
        Vibrator.interaction()

        val resultIntent = Intent().apply { putExtra(SELECTED_ESTABLISHMENT, establishment) }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun rvEstablishmentsOnRemove(establishment: Establishment) {
        val msg = String.format(getString(R.string.Deseja_mesmo_remover_x), establishment.name)

        val dialogBuilder = MaterialAlertDialogBuilder(this).setTitle(getString(R.string.Por_favor_confirme)).setMessage(msg)
            .setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                viewModel.removeEstablishment(establishment)
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }


}