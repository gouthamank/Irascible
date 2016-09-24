package app.drool.irascible.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import java.util.HashMap;

import app.drool.irascible.fragments.IRCFragment;
import app.drool.irascible.irc.IRCMessage;
import app.drool.irascible.utils.CacheUtils;

public class IRCPagerAdapter extends FragmentStatePagerAdapter {
    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private HashMap<String, IRCFragment> tabMapFragments;
    private SparseArray<String> tabMapTitles;

    public IRCPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        tabMapTitles = new SparseArray<>();
        tabMapFragments = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        return tabMapFragments.get(tabMapTitles.get(position));
    }

    @Override
    public int getCount() {
        return tabMapFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabMapTitles.get(position);
    }

    @SuppressWarnings("WeakerAccess")
    public void addMessage(IRCMessage message) {
        if (message == null) return;

        if (message.getChannelContext() != null) {
            if (!tabMapFragments.containsKey(message.getChannelContext())) {
                IRCFragment newFragment = IRCFragment.newInstance(message.getChannelContext());
                tabMapTitles.put(getCount(), message.getChannelContext());
                tabMapFragments.put(message.getChannelContext(), newFragment);
                notifyDataSetChanged();
            }
            tabMapFragments.get(message.getChannelContext()).addMessage(message);
            CacheUtils.addToFragmentLog(context, message.getChannelContext(), message.getMessageRaw());
        } else {
            if (!tabMapFragments.containsKey("server")) {
                IRCFragment newFragment = IRCFragment.newInstance("server");
                tabMapTitles.put(getCount(), "server");
                tabMapFragments.put("server", newFragment);
                notifyDataSetChanged();
            }
            tabMapFragments.get("server").addMessage(message);
            CacheUtils.addToFragmentLog(context, "server", message.getMessageRaw());
        }

    }

    public void addMessage(String message) {
        addMessage(IRCMessage.parse(message));
        CacheUtils.addToSessionLog(context, message);
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
        for (String pageTitle: pageTitles) {
            IRCFragment newFragment = IRCFragment.newInstance(pageTitle);
            tabMapTitles.put(getCount(), pageTitle);
            tabMapFragments.put(pageTitle, newFragment);
        }
        notifyDataSetChanged();
    }
}