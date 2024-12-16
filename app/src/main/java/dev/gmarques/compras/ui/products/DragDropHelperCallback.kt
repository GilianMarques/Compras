package dev.gmarques.compras.ui.products

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragDropHelperCallback(private val adapter: ProductAdapter) : ItemTouchHelper.Callback() {

    private var initialPos: Int = -1
    private var finalPos: Int = -1

    override fun isLongPressDragEnabled(): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {

        if (initialPos == -1) initialPos = viewHolder.adapterPosition
        finalPos = target.adapterPosition

        adapter.dragProducts(viewHolder.adapterPosition, target.adapterPosition)

        return true
    }

    /**
     * Quando o usuario soltar a view que ta arrastando, rodo um for no range de views que sofreu alteraçao de posiçao pra
     * atualizar elas no DB
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {

        if (finalPos < 0) return // se o usuario nao mover a view o suficiente, finalPos pode permanecer com -1 causando erro

        val biggerValue = maxOf(initialPos, finalPos)
        val smallerValue = minOf(initialPos, finalPos)

        adapter.updateDraggedProducts(biggerValue, smallerValue)
        initialPos = -1
        finalPos = -1

        super.clearView(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Não implementado porque swipe não está sendo usado
    }
}
