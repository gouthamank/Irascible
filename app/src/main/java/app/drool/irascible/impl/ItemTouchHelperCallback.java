package app.drool.irascible.impl;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import app.drool.irascible.interfaces.ItemTouchHelperAdapter;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private ItemTouchHelperAdapter adapter;
    private boolean hasDragged = false;
    public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        }
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if(viewHolder.getAdapterPosition() != target.getAdapterPosition()) {
            adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            hasDragged = true;
        }
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onItemSwiped(viewHolder.getAdapterPosition());
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if(hasDragged)
            adapter.endActionMode();
        hasDragged = false;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    } // Disabled swipe to dismiss for now
}
