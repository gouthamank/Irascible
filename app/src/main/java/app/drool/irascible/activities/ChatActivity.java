package app.drool.irascible.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.util.Date;

import app.drool.irascible.Constants;
import app.drool.irascible.Constants.SERVICE;
import app.drool.irascible.R;
import app.drool.irascible.adapters.IRCPagerAdapter;
import app.drool.irascible.adapters.UserListAdapter;
import app.drool.irascible.fragments.IRCFragment;
import app.drool.irascible.interfaces.MessageAddedListener;
import app.drool.irascible.interfaces.SelfNickChangedListener;
import app.drool.irascible.irc.IRCCommand;
import app.drool.irascible.irc.IRCMessage;
import app.drool.irascible.irc.IRCServerData;
import app.drool.irascible.services.BroadcastService;
import app.drool.irascible.utils.CacheUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity implements MessageAddedListener, SelfNickChangedListener {
    @BindView(R.id.activity_chat_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_chat_viewpager)
    ViewPager viewPager;
    @BindView(R.id.activity_chat_tablayout)
    TabLayout tabLayout;
    @BindView(R.id.activity_chat_edittext_input)
    EditText textInput;
    @BindView(R.id.activity_chat_button_send)
    ImageButton sendButton;
    IRCPagerAdapter pagerAdapter;
    private IntentFilter intentFilter;
    private boolean isTabNonServerFrag;
    private IRCServerData serverData;
    private String selfNick;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.contentEquals(SERVICE.ACTIONS.broadcastFromService)) {
                if (pagerAdapter != null) {
                    if (intent.hasExtra("data"))
                        pagerAdapter.addMessage(intent.getStringExtra("data"));
                    if (intent.hasExtra("dataSelf"))
                        addSelfMessage(intent.getStringExtra("dataSelf"));
                }

            } else if (intentAction.contentEquals(SERVICE.ACTIONS.broadcastErrorFromService)) {
                Snackbar.make(viewPager, intent.getExtras().getString("errorType"), Snackbar.LENGTH_INDEFINITE).show();
                textInput.setEnabled(false);
                sendButton.setEnabled(false);
            }
        }
    };
    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if (pagerAdapter == null) isTabNonServerFrag = false;
            else
                isTabNonServerFrag = !pagerAdapter.getPageTitle(tab.getPosition()).toString().contentEquals("server");

            if (tab.getText().toString().startsWith("* ")) {
                tab.setText(tab.getText().toString().substring(2));
            }
            invalidateOptionsMenu();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            textInput.setText("");
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        Intent pauseIntent = new Intent(ChatActivity.this, BroadcastService.class);
        pauseIntent.setAction(SERVICE.ACTIONS.pauseService);
        startService(pauseIntent);

        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent resumeIntent = new Intent(ChatActivity.this, BroadcastService.class);
        resumeIntent.setAction(SERVICE.ACTIONS.resumeService);
        startService(resumeIntent);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intentFilter = new IntentFilter(SERVICE.ACTIONS.broadcastFromService);
        intentFilter.addAction(SERVICE.ACTIONS.broadcastErrorFromService);

        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        toolbar.setTitle("Irascible");
        setSupportActionBar(toolbar);

        pagerAdapter = new IRCPagerAdapter(getSupportFragmentManager(), ChatActivity.this);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (getIntent().getExtras() != null)
            serverData = (IRCServerData) getIntent().getSerializableExtra("serverData");

        startBroadcastService(serverData);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textInput.getText().toString().length() <= 0) return;
                String rawText = textInput.getText().toString();
                if (rawText.startsWith("/")) {
                    IRCCommand command = IRCCommand.parse(rawText, tabLayout.getTabAt(viewPager.getCurrentItem()).getText().toString());
                    if (command.isValid()) {
                        sendMessageToService(command.getFormattedMessage());
                        if (command.isSelfCommand())
                            addSelfMessage(command.getFormattedMessage());
                    } else {
                        Toast.makeText(ChatActivity.this, R.string.activity_chat_command_notsupported, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    if (viewPager.getCurrentItem() == 0) return;
                    String currentTab = tabLayout.getTabAt(viewPager.getCurrentItem()).getText().toString();
                    String formattedCommand = "PRIVMSG " + currentTab + " :" +
                            textInput.getText().toString();
                    sendMessageToService(formattedCommand);
                    addSelfMessage(formattedCommand);
                }
                textInput.setText("");
            }
        });

        tabLayout.addOnTabSelectedListener(tabSelectedListener);
    }

    @Override
    public void onBackPressed() {
        confirmExit();
    }

    private void confirmExit() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ChatActivity.this);
        dialogBuilder.setTitle("Exit Irascible?");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                sendMessageToService("QUIT");
                stopBroadcastService();
                finish();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.create().show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selfNick", selfNick);
        if (pagerAdapter != null) {
            outState.putInt("selectedPage", tabLayout.getSelectedTabPosition());
            outState.putStringArray("pageTitles", pagerAdapter.getPageTitles());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selfNick = savedInstanceState.getString("selfNick");
        if (pagerAdapter != null) {
            String[] pageTitles = savedInstanceState.getStringArray("pageTitles");
            if (pageTitles != null && pagerAdapter != null)
                pagerAdapter.restorePageTitles(pageTitles);

            TabLayout.Tab prevSelectedTab = tabLayout.getTabAt(savedInstanceState.getInt("selectedPage", 0));
            if (prevSelectedTab != null)
                prevSelectedTab.select();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menu.findItem(R.id.menu_chat_userlist).setVisible(false);
        menu.findItem(R.id.menu_chat_stop_service).setVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_chat_userlist).setVisible(isTabNonServerFrag);
        menu.findItem(R.id.menu_chat_stop_service).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_chat_stop_service:
                confirmExit();
                return true;

            case R.id.menu_chat_userlist:
                showUserList();
            default:
                break;
        }

        return false;
    }

    private void startBroadcastService(IRCServerData serverData) {
        if (serverData == null)
            return;

        toolbar.setTitle(serverData.getServerName());
        selfNick = serverData.getNickName();

        Intent startIntent = new Intent(ChatActivity.this, BroadcastService.class);
        startIntent.setAction(SERVICE.ACTIONS.startService);
        startIntent.putExtra("serverData", serverData);
        startService(startIntent);
    }

    private void sendMessageToService(String message) {
        Intent sendIntent = new Intent(ChatActivity.this, BroadcastService.class);
        sendIntent.setAction(Constants.SERVICE.ACTIONS.broadcastFromActivity);
        sendIntent.putExtra("data", message);
        startService(sendIntent);
    }

    private void addSelfMessage(String formattedMessage) {
        Date currentDate = new Date();
        String messageWithTimestamp = currentDate.getTime() + " :"
                + selfNick + "!" + serverData.getIdent() + "@" + "self" + " "
                + formattedMessage;
        IRCMessage selfMessage = IRCMessage.parse(messageWithTimestamp);
        if (!selfMessage.isInvalid() && selfMessage.getChannelContext() != null)
            pagerAdapter.addMessage(IRCMessage.parse(messageWithTimestamp));
    }

    private void stopBroadcastService() {
        Intent stopIntent = new Intent(ChatActivity.this, BroadcastService.class);
        stopIntent.setAction(SERVICE.ACTIONS.stopService);
        startService(stopIntent);

        if (pagerAdapter != null) {
            for (String title : pagerAdapter.getPageTitles()) {
                CacheUtils.clearFragmentLog(ChatActivity.this, title);
            }
        }
    }

    private void showUserList() {
        if (pagerAdapter == null) return;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        String[] userList = ((IRCFragment) pagerAdapter.getItem(tabLayout.getSelectedTabPosition())).getListAdapter().getUserList();
        ListAdapter userListAdapter = new UserListAdapter(this, userList);
        dialogBuilder.setAdapter(userListAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialogBuilder.create().show();
    }

    public void appendToEditText(String username) {
        if (textInput.getText().toString().length() == 0)
            textInput.append(username + ": ");
        else
            textInput.append(" " + username);
    }

    // IMPL: MessageAddedListener

    @Override
    public void onMessageAdded(int tabPosition) {
        if (tabLayout.getSelectedTabPosition() != tabPosition) {
            TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
            if (!tab.getText().toString().startsWith("* "))
                tab.setText("* " + tab.getText());
        }
    }

    // IMPL: SelfNickChangedListener

    @Override
    public void onSelfNickChanged(String newNick) {
        selfNick = newNick;
    }
}
