package app.drool.irascible.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.drool.irascible.R;
import app.drool.irascible.interfaces.ItemTouchHelperAdapter;
import app.drool.irascible.irc.IRCServerData;
import app.drool.irascible.Constants.PREFERENCES;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ServerListAdapter extends MultiSelectAdapter<ServerListAdapter.ServerListHolder> implements ItemTouchHelperAdapter {

    private final String TAG = getClass().getSimpleName();
    private Context context;
    private SharedPreferences savedServersPref;
    private ArrayList<IRCServerData> savedServers;
    private ServerListHolder.ClickListener clickListener;

    public ServerListAdapter(Context context, ServerListHolder.ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        savedServersPref = context.getSharedPreferences(PREFERENCES.FILES.savedServers, Context.MODE_PRIVATE);
        refreshServers();
    }

    private void refreshServers() {
        Gson gson = new Gson();
        String savedJSON = savedServersPref.getString(PREFERENCES.KEYS.savedServers, "");
        if (savedJSON.length() == 0)
            savedServers = new ArrayList<>();
        else
            savedServers = gson.fromJson(savedJSON, new TypeToken<ArrayList<IRCServerData>>(){}.getType());
    }

    private void updatePreferences() {
        Gson gson = new Gson();
        String json = gson.toJson(savedServers, new TypeToken<ArrayList<IRCServerData>>(){}.getType());
        savedServersPref.edit().putString(PREFERENCES.KEYS.savedServers, json).apply();
    }

    // MARK: Adding/removal of items

    public boolean hasServer(String serverName) {
        for (IRCServerData d : savedServers) {
            if (d.getServerName().contentEquals(serverName))
                return true;
        }

        return false;
    }

    public IRCServerData getServerData(int position) {
        if(position < savedServers.size()){
            return savedServers.get(position);
        }

        return null;
    }

    public void addServer(IRCServerData data) {
        savedServers.add(data);
        updatePreferences();
        clickListener.refreshItemCount();
        notifyItemInserted(savedServers.size());
    }

    public void removeServer(int position) {
        savedServers.remove(position);
        updatePreferences();
        clickListener.refreshItemCount();
        notifyItemRemoved(position);
    }

    public void editServer(IRCServerData server, int position) {
        savedServers.remove(position);
        savedServers.add(position, server);
        updatePreferences();
        notifyItemChanged(position);
    }

    public void removeServers(List<Integer> positions){
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        for(int position: positions)
            removeServer(position);
    }

    // MARK: Implementing ItemTouchHelperAdapter

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if(fromPosition < toPosition) {
            for(int i = fromPosition; i < toPosition; i++) {
                Collections.swap(savedServers, i, i + 1);
            }
        } else {
            for(int i = fromPosition; i > toPosition; i--)
                Collections.swap(savedServers, i, i-1);
        }
        updatePreferences();
        swapSelection(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemSwiped(int position) {
        removeServer(position); // Not supported
    }

    @Override
    public void endActionMode() {
        clickListener.endActionMode();
    }

    // MARK: View binding

    @Override
    public ServerListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout serverItem = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_server, parent, false);
        return new ServerListHolder(serverItem, clickListener);
    }

    @Override
    public void onBindViewHolder(final ServerListHolder holder, int position) {
        try {
            IRCServerData data = savedServers.get(position);
            holder.address.setText(data.getServerAddress());
            holder.port.setText(String.valueOf(data.getServerPort()));
            holder.name.setText(data.getServerName());
            holder.overlay.setVisibility(isSelected(position) ? View.VISIBLE : View.GONE);
            holder.nick.setText(data.getNickName());
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.listener != null)
                        holder.listener.onItemClicked(holder.getAdapterPosition());
                }
            });

            holder.card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(holder.listener != null)
                        holder.listener.onItemLongClicked(holder.getAdapterPosition());

                    return true;
                }
            });
        } catch (IndexOutOfBoundsException e) {
            holder.address.setText("");
            holder.name.setText("");
            holder.port.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return savedServers.size();
    }

    public static class ServerListHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_server_address) TextView address;
        @BindView(R.id.list_item_server_name) TextView name;
        @BindView(R.id.list_item_server_port) TextView port;
        @BindView(R.id.list_item_server_nick) TextView nick;
        @BindView(R.id.list_item_server_cardview) CardView card;
        @BindView(R.id.list_item_server_overlay) View overlay;

        private ClickListener listener;

        ServerListHolder(LinearLayout serverItem, ClickListener listener) {
            super(serverItem);
            ButterKnife.bind(this, itemView);
            this.listener = listener;
        }

        public interface ClickListener {
            void onItemClicked(int position);
            void onItemLongClicked(int position);
            void refreshItemCount();
            void endActionMode();
        }
    }
}
