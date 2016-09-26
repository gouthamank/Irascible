package app.drool.irascible.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import app.drool.irascible.R;
import app.drool.irascible.adapters.MessageListAdapter;
import app.drool.irascible.irc.IRCMessage;
import butterknife.BindView;
import butterknife.ButterKnife;

public class IRCFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.fragment_chat_list_messages)
    RecyclerView messageList;

    private String channelContext;
    private MessageListAdapter listAdapter;
    private LinearLayoutManager layoutManager;

    public static IRCFragment newInstance(String channelContext) {
        Bundle args = new Bundle();
        args.putString("channelContext", channelContext);
        IRCFragment fragment = new IRCFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        channelContext = getArguments().getString("channelContext");
        listAdapter = new MessageListAdapter(getContext(), channelContext);
        layoutManager = new LinearLayoutManager(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_chat_page, container, false);
        ButterKnife.bind(this, relativeLayout);
        return relativeLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        messageList.setLayoutManager(layoutManager);
        messageList.setAdapter(listAdapter);
    }

    public void addMessage(IRCMessage message) {
        if (listAdapter != null) {
            if (listAdapter.getItemCount() - layoutManager.findLastVisibleItemPosition() <= 2) {
                listAdapter.addMessage(message);
                layoutManager.scrollToPosition(listAdapter.getItemCount() - 1);
            } else
                listAdapter.addMessage(message);
        }
    }

    public MessageListAdapter getListAdapter() {
        return listAdapter;
    }

}
