package dev.gmarques.compras.ui.categories

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class CategoryDragDropHelperCallback(private val adapter: CategoryAdapter) : ItemTouchHelper.Callback() {
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

        adapter.moveCategory(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    /**
     * Quando o usuario soltar a view que ta arrastando, rodo um for no range de views que sofreu alteraçao de posiçao pra
     * atualizar elas no DB
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {

        adapter.updateDraggedCategories()

        super.clearView(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Não implementado porque swipe não está sendo usado
    }
}
