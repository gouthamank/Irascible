package app.drool.irascible.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiSelectAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private final String TAG = this.getClass().getSimpleName();
    private SparseBooleanArray selectedItems;

    public MultiSelectAdapter() {
        selectedItems = new SparseBooleanArray();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> servers = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            servers.add(selectedItems.keyAt(i));
        }

        return servers;
    }

    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    public void toggleSelection(int position) {
        if (isSelected(position))
            selectedItems.delete(position);
        else
            selectedItems.append(position, true);

        notifyItemChanged(position);
    }

    public void swapSelection(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                if(isSelected(i) ^ isSelected(i + 1)) {
                    if(isSelected(i)) {
                        selectedItems.delete(i);
                        selectedItems.append(i + 1, true);
                    } else {
                        selectedItems.delete(i + 1);
                        selectedItems.append(i, true);
                    }
                }
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                if(isSelected(i) ^ isSelected(i - 1)) {
                    if(isSelected(i)) {
                        selectedItems.delete(i);
                        selectedItems.append(i - 1, true);
                    } else {
                        selectedItems.delete(i - 1);
                        selectedItems.append(i, true);
                    }
                }
            }
        }
    }

    public void clearSelection() {
        clearSelection(true);
    }

    public void clearSelection(boolean shouldNotify) {
        List<Integer> selections = getSelectedItems();
        selectedItems.clear();
        if (shouldNotify) {
            for (int i : selections) {
                notifyItemChanged(i);
            }
        }
    }

    public int getSelectedItemsCount() {
        return selectedItems.size();
    }
}
