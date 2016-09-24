package app.drool.irascible.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import app.drool.irascible.Constants;
import app.drool.irascible.Constants.CODES;
import app.drool.irascible.Constants.PREFERENCES;
import app.drool.irascible.R;
import app.drool.irascible.adapters.ServerListAdapter;
import app.drool.irascible.impl.ItemTouchHelperCallback;
import app.drool.irascible.irc.IRCServerData;
import app.drool.irascible.services.BroadcastService;

public class MainActivity extends AppCompatActivity implements ServerListAdapter.ServerListHolder.ClickListener {

    private final ActionModeCallback actionModeCallback = new ActionModeCallback();
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private TextView noServersText;
    private RecyclerView serverList;
    private ServerListAdapter serverListAdapter;
    private SharedPreferences generalPrefs;
    private AlertDialog serverDialog;
    private boolean isEditMode = false;
    private int currentEditPosition = -1;
    private ActionMode actionMode;
    private IntentFilter intentFilter;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            startActivity(new Intent(MainActivity.this, ChatActivity.class));
            finish();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();

        intentFilter = new IntentFilter(Constants.SERVICE.ACTIONS.heartbeatFromService);

        Intent heartbeatIntent = new Intent(MainActivity.this, BroadcastService.class);
        heartbeatIntent.setAction(Constants.SERVICE.ACTIONS.heartbeatRequest);
        startService(heartbeatIntent);

        generalPrefs = getSharedPreferences(PREFERENCES.FILES.generalPreferences, Context.MODE_PRIVATE);
        if (generalPrefs.getString(PREFERENCES.KEYS.defaultNick, null) == null) {
            generalPrefs.edit().putString(PREFERENCES.KEYS.defaultNick, PREFERENCES.VALUES.defaultNick).apply();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverDialog.show();
                setServerDialogLogic();
            }
        });
    }

    private void setupViews() {
        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        fab = (FloatingActionButton) findViewById(R.id.activity_main_fab);

        serverList = (RecyclerView) findViewById(R.id.activity_main_list_servers);
        serverList.setItemAnimator(new DefaultItemAnimator());
        serverListAdapter = new ServerListAdapter(MainActivity.this, this);
        if (getResources().getBoolean(R.bool.isTablet))
            serverList.setLayoutManager(new GridLayoutManager(MainActivity.this, getResources().getInteger(R.integer.numColumns)));
        else
            serverList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        serverList.setAdapter(serverListAdapter);

        noServersText = (TextView) findViewById(R.id.activity_main_text_no_servers);
        refreshItemCount();

        ItemTouchHelper.Callback itemTouchHelperCallback = new ItemTouchHelperCallback(serverListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(serverList);

        setSupportActionBar(toolbar);

        serverDialog = createServerDialog();
    }

    @SuppressWarnings("ConstantConditions")
    private void setServerDialogLogic() {
        final TextInputLayout addressLayout = (TextInputLayout) serverDialog.findViewById(R.id.dialog_servers_add_address_layout);
        final TextInputLayout portLayout = (TextInputLayout) serverDialog.findViewById(R.id.dialog_servers_add_port_layout);
        final TextInputLayout nameLayout = (TextInputLayout) serverDialog.findViewById(R.id.dialog_servers_add_name_layout);
        final TextInputLayout nickLayout = (TextInputLayout) serverDialog.findViewById(R.id.dialog_servers_add_nick_layout);

        final TextInputEditText addressEditText = (TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_address_edittext);
        final TextInputEditText portEditText = (TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_port_edittext);
        final TextInputEditText nameEditText = (TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_name_edittext);
        final TextInputEditText nickEditText = (TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_nick_edittext);

        // Restore selected server details if editing
        if (isEditMode) {
            IRCServerData editData = serverListAdapter.getServerData(currentEditPosition);
            addressEditText.setText(editData.getServerAddress());
            portEditText.setText(String.valueOf(editData.getServerPort()));
            nameEditText.setText(editData.getServerName());
            nickEditText.setText(editData.getNickName());
        } else {
            nickEditText.setText(generalPrefs.getString(PREFERENCES.KEYS.defaultNick, ""));
        }

        serverDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(isEditMode ? R.string.dialog_main_server_edit : R.string.dialog_main_server_positive);
        serverDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View v) {

                // Check for errors and add/replace as appropriate

                if (nameEditText.getText().toString().length() < 1 || serverListAdapter.hasServer(nameEditText.getText().toString())) {
                    nameLayout.setError(getString(R.string.dialog_main_server_name_error));
                    if (isEditMode && nameEditText.getText().toString().contentEquals(serverListAdapter.getServerData(currentEditPosition).getServerName()))
                        nameLayout.setError("");
                    else
                        return;
                } else
                    nameLayout.setError("");

                if (addressEditText.getText().toString().length() < 1) {
                    addressLayout.setError(getString(R.string.dialog_main_server_address_error));
                    return;
                }
                else
                    addressLayout.setError("");

                if (portEditText.getText().toString().length() < 1) {
                    portLayout.setError(getString(R.string.dialog_main_server_port_error));
                    return;
                }
                else
                    portLayout.setError("");

                if (nickEditText.getText().toString().length() < 1) {
                    nickLayout.setError(getString(R.string.dialog_main_server_nick_error));
                    return;
                }

                IRCServerData server = new IRCServerData();
                server.setServerAddress(addressEditText.getText().toString())
                        .setServerName(nameEditText.getText().toString())
                        .setServerPort(Integer.valueOf(portEditText.getText().toString()))
                        .setNickName(nickEditText.getText().toString());
                serverDialog.cancel();

                if (isEditMode) {
                    serverListAdapter.editServer(server, currentEditPosition);
                } else {
                    serverListAdapter.addServer(server);
                }
            }
        });

        serverDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverDialog.cancel();
            }
        });
        serverDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                addressLayout.setErrorEnabled(false);
                portLayout.setErrorEnabled(false);
                nameLayout.setErrorEnabled(false);
                nickLayout.setErrorEnabled(false);

                addressEditText.setText("");
                portEditText.setText("");
                nameEditText.setText("");
                nickEditText.setText("");

                setDialogEditMode(false, -1);
            }
        });

        serverDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addServerIntent = new Intent(MainActivity.this, ServerDetailsActivity.class);
                addServerIntent.putExtra("serverName", nameEditText.getText().toString());
                addServerIntent.putExtra("serverAddress", addressEditText.getText().toString());
                addServerIntent.putExtra("serverPort", portEditText.getText().toString());
                addServerIntent.putExtra("nickName", nickEditText.getText().toString());
                addServerIntent.putExtra("isEditMode", isEditMode);
                if (isEditMode) {
                    addServerIntent.putExtra("editedServerPosition", currentEditPosition);
                    addServerIntent.putExtra("serverData", serverListAdapter.getServerData(currentEditPosition));
                }

                serverDialog.cancel();
                startActivityForResult(addServerIntent, CODES.addServerRequestCode);
            }
        });
    }

    private AlertDialog createServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_server, null);
        builder.setView(dialogView);
        builder.setNegativeButton(R.string.dialog_main_server_negative, null);
        builder.setPositiveButton(isEditMode ? R.string.dialog_main_server_edit : R.string.dialog_main_server_positive, null);
        builder.setNeutralButton(R.string.dialog_main_server_neutral, null);

        return builder.create();
    }

    private void setDialogEditMode(boolean isEditMode, int currentEditPosition) {
        this.isEditMode = isEditMode;
        if (!isEditMode)
            this.currentEditPosition = -1;
        else
            this.currentEditPosition = currentEditPosition;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (actionMode != null) {
            outState.putBoolean("isMultiselectMode", true);
            ArrayList<Integer> selectedItems = (ArrayList<Integer>) serverListAdapter.getSelectedItems();
            outState.putIntegerArrayList("selectedItems", selectedItems);
        }

        outState.putBoolean("serverDialogIsShowing", serverDialog.isShowing());
        if (serverDialog.isShowing()) {
            outState.putBoolean("isEditMode", isEditMode);
            outState.putInt("currentEditPosition", currentEditPosition);
            outState.putString("serverDialogAddress",
                    ((TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_address_edittext)).getText().toString());
            outState.putString("serverDialogPort",
                    ((TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_port_edittext)).getText().toString());
            outState.putString("serverDialogName",
                    ((TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_name_edittext)).getText().toString());
            outState.putString("serverDialogNick",
                    ((TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_nick_edittext)).getText().toString());
            serverDialog.cancel();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.getBoolean("isMultiselectMode", false)) {
            actionMode = startSupportActionMode(actionModeCallback);
            ArrayList<Integer> selectedItems = savedInstanceState.getIntegerArrayList("selectedItems");
            for (int position : selectedItems) {
                onItemClicked(position);
            }
        }

        if (savedInstanceState.getBoolean("serverDialogIsShowing", false)) {
            setDialogEditMode(savedInstanceState.getBoolean("isEditMode"), savedInstanceState.getInt("currentEditPosition"));
            serverDialog.show();
            setServerDialogLogic();

            ((TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_address_edittext))
                    .setText(savedInstanceState.getString("serverDialogAddress"));
            ((TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_port_edittext))
                    .setText(savedInstanceState.getString("serverDialogPort"));
            ((TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_name_edittext))
                    .setText(savedInstanceState.getString("serverDialogName"));
            ((TextInputEditText) serverDialog.findViewById(R.id.dialog_servers_add_nick_edittext))
                    .setText(savedInstanceState.getString("serverDialogNick"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CODES.addServerRequestCode) return;
        if (resultCode != CODES.addServerResponseEdited && resultCode != CODES.addServerResponseAdded) return;

        IRCServerData serverData = (IRCServerData) data.getSerializableExtra("serverData");
        String name = serverData.getServerName();
        boolean isEditMode = resultCode == CODES.addServerResponseEdited;

        if (name.length() < 1 || serverListAdapter.hasServer(name)) {
            if(!(isEditMode && name.contentEquals(serverListAdapter.getServerData(data.getIntExtra("editedServerPosition", -1)).getServerName()))) {
                Snackbar.make(toolbar, R.string.dialog_main_server_name_error, Snackbar.LENGTH_LONG).show();
                return;
            }
        }

        if (!isEditMode) {
            serverListAdapter.addServer((IRCServerData) data.getSerializableExtra("serverData"));
        } else {
            serverListAdapter.editServer((IRCServerData) data.getSerializableExtra("serverData"), data.getIntExtra("editedServerPosition", -1));
        }
    }


    // IMPL: ServerListAdapter.ServerListHolder.ClickListener

    @Override
    public void onItemClicked(int position) {
        if (actionMode != null) {
            toggleSelection(position);
        } else {
            Intent startIntent = new Intent(MainActivity.this, ChatActivity.class);
            startIntent.putExtra("serverData", serverListAdapter.getServerData(position));
            startActivity(startIntent);
            finish();
        }
    }

    @Override
    public void onItemLongClicked(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
            toggleSelection(position);
        }
    }

    private void toggleSelection(int position) {
        serverListAdapter.toggleSelection(position);
        int count = serverListAdapter.getSelectedItemsCount();

        if (count == 0) {
            actionMode.finish();
            actionMode = null;
        } else {
            actionMode.setTitle(getString(R.string.menu_main_selectedcount, count));
            actionMode.getMenu().findItem(R.id.menu_main_actionmode_edit).setVisible(count == 1);
            actionMode.invalidate();
        }
    }

// END IMPL

    @Override
    public void refreshItemCount() {
        noServersText.setVisibility(serverListAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void endActionMode() {
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_main_actionmode, menu);
            fab.hide();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_main_actionmode_delete:
                    Snackbar.make(serverList,
                            getResources().getQuantityString(R.plurals.activity_main_list_servers_deleted,
                                    serverListAdapter.getSelectedItemsCount(),
                                    serverListAdapter.getSelectedItemsCount()),
                            Snackbar.LENGTH_LONG).show();
                    serverListAdapter.removeServers(serverListAdapter.getSelectedItems());
                    mode.finish();
                    return true;

                case R.id.menu_main_actionmode_edit:
                    if (serverListAdapter.getSelectedItemsCount() == 1) {
                        setDialogEditMode(true, serverListAdapter.getSelectedItems().get(0));
                        serverDialog.show();
                        setServerDialogLogic();
                    }
                    mode.finish();
                    return true;

                default:
                    mode.finish();
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            serverListAdapter.clearSelection();
            refreshItemCount();
            fab.show();
            actionMode = null;
            if (currentEditPosition != -1) serverListAdapter.notifyItemChanged(currentEditPosition);
        }
    }

}
