package dev.gmarques.compras.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import dev.gmarques.compras.App
import dev.gmarques.compras.R
import dev.gmarques.compras.data.model.Product
import dev.gmarques.compras.data.repository.EstablishmentRepository
import dev.gmarques.compras.data.repository.ProductRepository
import dev.gmarques.compras.databinding.FragmentPriceListBinding
import dev.gmarques.compras.databinding.RvItemPriceBinding
import dev.gmarques.compras.domain.model.PriceHistory
import dev.gmarques.compras.domain.utils.ExtFun.Companion.adjustSaturation
import dev.gmarques.compras.domain.utils.ExtFun.Companion.toCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Carrega os dados de historico de preços de um produto e os exibe em uma view.
 * Adicione PricesHistoryViewComponent.view ao container desejado para exibir o conteudo.
 *
 * @param coroutineScope deve ser uma corrotina gerenciada, manual ou automaticamente para evitar memory leaks.
 * */
class PricesHistoryViewComponent(
    inflater: LayoutInflater,
    private val coroutineScope: CoroutineScope,
    private val product: Product,
    private val onPriceItemClick: (PriceHistory) -> Unit,
) {
    /**
     * Inclua essa view no container para exibir o conteudo
     */
    var view: View
        private set

    private val nulo = App.getContext().getString(R.string.Nulo)
    private var binding = FragmentPriceListBinding.inflate(inflater)
    private lateinit var priceAdapter: PriceAdapter

    init {
        view = binding.root
        setupRecyclerView()
        loadData()
    }

    private fun loadData() = coroutineScope.launch(IO) {

        val priceHistories = mutableListOf<PriceHistory>()

        val products = async {
            ProductRepository.getHistoryPricesProducts(product.name).getOrThrow().sortedBy { it.boughtDate }
        }.await()

        val establishments = async {
            EstablishmentRepository.getAllEstablishments().getOrThrow().associateBy { it.id }
        }.await()

        products.forEach {
            val establishment = establishments[it.marketId]

            priceHistories.add(PriceHistory(it.price, establishment?.name ?: nulo, establishment?.color ?: -1))
        }

        withContext(Main) { priceAdapter.submitItems(priceHistories) }

    }

    private fun setupRecyclerView() {
        priceAdapter = PriceAdapter(onPriceItemClick)
        binding.recyclerViewPrices.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = priceAdapter
        }
    }

    // Classe do Adapter dentro do Fragment
    inner class PriceAdapter(
        private val onItemClick: (PriceHistory) -> Unit,
    ) : RecyclerView.Adapter<PriceAdapter.PriceViewHolder>() {

        private var items: List<PriceHistory> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<RvItemPriceBinding>(
                inflater, R.layout.rv_item_price, parent, false
            )
            return PriceViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PriceViewHolder, position: Int) {
            val currentItem = items[position]
            holder.bind(currentItem)
        }

        override fun getItemCount(): Int = items.size

        @SuppressLint("NotifyDataSetChanged")
        fun submitItems(priceHistories: MutableList<PriceHistory>) {
            this.items = priceHistories.toList()
            notifyDataSetChanged()
        }

        inner class PriceViewHolder(private val binding: RvItemPriceBinding) : RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(item: PriceHistory) {
                binding.tvListName.text = item.price.toCurrency()

                if (item.color != -1) {
                    binding.ivPriceIcon.drawable?.mutate()?.setTint(item.color)

                    val saturatedColor = item.color.adjustSaturation(0.25f)
                    binding.ivPriceIcon.background?.mutate()?.apply {
                        if (this is GradientDrawable) setColor(saturatedColor)
                    }
                }

                binding.cvChild.setOnClickListener {
                    Vibrator.success()
                    onItemClick(item)
                }

                binding.ivExpand.setOnClickListener {
                    Vibrator.interaction()
                    binding.tvListName.text = "${item.price.toCurrency()} | ${item.establishmentName}"
                }
            }
        }
    }

}