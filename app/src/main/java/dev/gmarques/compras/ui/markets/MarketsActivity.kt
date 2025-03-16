package dev.gmarques.compras.ui.markets

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Market
import dev.gmarques.compras.databinding.ActivityMarketsBinding

import dev.gmarques.compras.ui.Vibrator
import dev.gmarques.compras.ui.add_edit_market.AddEditMarketActivity

class MarketsActivity : AppCompatActivity(), MarketAdapter.Callback {

    private var fabHidden: Boolean = false
    private var selectionMode: Boolean = false
    private lateinit var binding: ActivityMarketsBinding
    private lateinit var viewModel: ActivityMarketsViewModel
    private lateinit var adapter: MarketAdapter

    companion object {

        const val SELECTION_MODE = "selection_mode"
        const val SELECTED_MARKET = "selected_market"

        fun newIntent(context: Context, selectionMode: Boolean = false): Intent {
            return Intent(context, MarketsActivity::class.java).apply {
                putExtra(SELECTION_MODE, selectionMode)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMarketsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ActivityMarketsViewModel::class.java]
        selectionMode = intent.getBooleanExtra(SELECTION_MODE, false)
        setupToolbar()
        setupRecyclerview()
        observeMarkets()
        setupFabAddMarket()
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

    private fun setupFabAddMarket() = binding.apply {
        fabAdd.setOnClickListener {
            startActivityAddMarket()
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

    private fun observeMarkets() {
        viewModel.marketsLd.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun setupRecyclerview() {

        adapter = MarketAdapter(this)

        val dragDropHelper = MarketDragDropHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(dragDropHelper)
        adapter.attachItemTouchHelper(touchHelper)

        touchHelper.attachToRecyclerView(binding.rv)

        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(this@MarketsActivity)


    }

    private fun observeViewmodelErrorMessages() {
        viewModel.errorEventLD.observe(this@MarketsActivity) { event ->
            Snackbar.make(binding.root, event, Snackbar.LENGTH_LONG).show()
            Vibrator.error()
        }
    }

    private fun startActivityAddMarket() {

        Vibrator.interaction()
        val intent = AddEditMarketActivity.newIntentAddMarket(this@MarketsActivity)
        startActivity(intent)
    }


    override fun rvMarketsOnDragAndDrop(toPosition: Int, market: Market) {
        viewModel.updateMarketPosition(market,toPosition)
    }

    override fun rvMarketsOnEditItemClick(market: Market) {
        Vibrator.interaction()
        val intent = AddEditMarketActivity.newIntentEditMarket(this@MarketsActivity, market.id)
        startActivity(intent)
    }

    override fun rvMarketsOnSelect(market: Market) {
        if (!selectionMode) return
        Vibrator.interaction()

        val resultIntent = Intent().apply { putExtra(SELECTED_MARKET, market) }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun rvMarketsOnRemove(market: Market) {
        val msg = String.format(getString(R.string.Deseja_mesmo_remover_x), market.name)

        val dialogBuilder = AlertDialog.Builder(this).setTitle(getString(R.string.Por_favor_confirme)).setMessage(msg)
            .setPositiveButton(getString(R.string.Remover)) { dialog, _ ->
                viewModel.removeMarket(market)
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.Cancelar)) { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }


}