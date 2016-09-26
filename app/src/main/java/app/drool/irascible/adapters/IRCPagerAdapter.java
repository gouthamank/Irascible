package app.drool.irascible.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import java.util.HashMap;

import app.drool.irascible.fragments.IRCFragment;
import app.drool.irascible.interfaces.MessageAddedListener;
import app.drool.irascible.interfaces.SelfNickChangedListener;
import app.drool.irascible.irc.IRCMessage;
import app.drool.irascible.utils.CacheUtils;

public class IRCPagerAdapter extends FragmentStatePagerAdapter {
    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private MessageAddedListener messageAddedListener;
    private SelfNickChangedListener nickChangedListener;
    private HashMap<String, IRCFragment> tabMapFragments;
    private SparseArray<String> tabMapTitles;
    private HashMap<String, Integer> tabMapPositions;

    public IRCPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        if (context instanceof MessageAddedListener)
            messageAddedListener = (MessageAddedListener) context;
        if (context instanceof SelfNickChangedListener)
            nickChangedListener = (SelfNickChangedListener) context;

        tabMapTitles = new SparseArray<>();
        tabMapFragments = new HashMap<>();
        tabMapPositions = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        return tabMapFragments.get(tabMapTitles.get(position));
    }

    public void addMessage(String message) {
        addMessage(IRCMessage.parse(message));
        CacheUtils.addToSessionLog(context, message);
    }

    @SuppressWarnings("WeakerAccess")
    public void addMessage(IRCMessage message) {
        if (message == null) return;
        if (message.getMessageCode() == null) return;

        if (message.getMessageCode().contentEquals(IRCMessage.CODES.nickNotice) ||
                message.getMessageCode().contentEquals(IRCMessage.CODES.quitNotice)) {
            for (String tab : tabMapFragments.keySet()) {
                message.setChannelContext(tab);
                tabMapFragments.get(tab).addMessage(message);
                if (messageAddedListener != null && tabMapPositions.containsKey(tab))
                    messageAddedListener.onMessageAdded(tabMapPositions.get(tab));
            }
            if (nickChangedListener != null && message.getMessageCode().contentEquals(IRCMessage.CODES.nickNotice))
                nickChangedListener.onSelfNickChanged(message.getMessageContent());

        } else if (message.getChannelContext() != null) {

            if (!tabMapFragments.containsKey(message.getChannelContext())) {
                IRCFragment newFragment = IRCFragment.newInstance(message.getChannelContext());
                tabMapTitles.put(getCount(), message.getChannelContext());
                tabMapPositions.put(message.getChannelContext(), getCount());
                tabMapFragments.put(message.getChannelContext(), newFragment);
                notifyDataSetChanged();
            }
            tabMapFragments.get(message.getChannelContext()).addMessage(message);
            if (messageAddedListener != null && tabMapPositions.containsKey(message.getChannelContext()))
                messageAddedListener.onMessageAdded(tabMapPositions.get(message.getChannelContext()));
            CacheUtils.addToFragmentLog(context, message.getChannelContext(), message.getMessageRaw());

        } else {

            if (!tabMapFragments.containsKey("server")) {
                IRCFragment newFragment = IRCFragment.newInstance("server");
                tabMapTitles.put(getCount(), "server");
                tabMapPositions.put("server", getCount());
                tabMapFragments.put("server", newFragment);
                notifyDataSetChanged();
            }
            tabMapFragments.get("server").addMessage(message);
            if (messageAddedListener != null && tabMapPositions.containsKey("server"))
                messageAddedListener.onMessageAdded(tabMapPositions.get("server"));
            CacheUtils.addToFragmentLog(context, "server", message.getMessageRaw());
        }

    }

    @Override
    public int getCount() {
        return tabMapFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabMapTitles.get(position);
    }

    // getPageTitles() and restorePageTitles(String[]) are used to restore state when parent activity is recreated
    public String[] getPageTitles() {
        String[] pageTitles = new String[getCount()];
        for (int i = 0; i < getCount(); i++) {
            pageTitles[i] = tabMapTitles.get(i);
        }
        return pageTitles;
    }

    public void restorePageTitles(String[] pageTitles) {
        for (String pageTitle : pageTitles) {
            IRCFragment newFragment = IRCFragment.newInstance(pageTitle);
            tabMapTitles.put(getCount(), pageTitle);
            tabMapFragments.put(pageTitle, newFragment);
        }
        notifyDataSetChanged();
    }
}
