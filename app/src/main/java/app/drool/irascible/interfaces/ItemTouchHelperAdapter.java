package app.drool.irascible.interfaces;

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);
    void onItemSwiped(int position);
    void endActionMode();
}
