package app.drool.irascible.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import app.drool.irascible.Constants;
import app.drool.irascible.R;
import app.drool.irascible.irc.IRCServerData;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ServerDetailsActivity extends AppCompatActivity {
    @BindView(R.id.activity_server_toolbar)
    Toolbar toolbar;

    @BindView(R.id.activity_server_servername_layout) TextInputLayout serverNameLayout;
    @BindView(R.id.activity_server_servername) TextInputEditText serverName;
    @BindView(R.id.activity_server_serveraddress_layout) TextInputLayout serverAddressLayout;
    @BindView(R.id.activity_server_serveraddress) TextInputEditText serverAddress;
    @BindView(R.id.activity_server_serverport_layout) TextInputLayout serverPortLayout;
    @BindView(R.id.activity_server_serverport) TextInputEditText serverPort;
    @BindView(R.id.activity_server_nickname_layout) TextInputLayout nickNameLayout;
    @BindView(R.id.activity_server_nickname) TextInputEditText nickName;
    @BindView(R.id.activity_server_nickname_alt_layout) TextInputLayout nickNameAltLayout;
    @BindView(R.id.activity_server_nickname_alt) TextInputEditText nickNameAlt;
    @BindView(R.id.activity_server_ident_layout) TextInputLayout identLayout;
    @BindView(R.id.activity_server_ident) TextInputEditText ident;
    @BindView(R.id.activity_server_realname_layout) TextInputLayout realNameLayout;
    @BindView(R.id.activity_server_realname) TextInputEditText realName;
    @BindView(R.id.activity_server_nickserv_password_layout) TextInputLayout nickservPasswordLayout;
    @BindView(R.id.activity_server_nickserv_password) TextInputEditText nickservPassword;
    @BindView(R.id.activity_server_commands_layout) TextInputLayout serverCommandsLayout;
    @BindView(R.id.activity_server_commands) TextInputEditText serverCommands;

    private boolean isEditMode;
    private int editedServerPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ButterKnife.bind(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        restoreFields();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_server_accept:
                if (checkFields()) {
                    IRCServerData serverData = new IRCServerData();
                    serverData.setServerAddress(serverAddress.getText().toString())
                            .setServerName(serverName.getText().toString())
                            .setServerPort(Integer.valueOf(serverPort.getText().toString()))
                            .setNickName(nickName.getText().toString())
                            .setNickNameAlt(nickNameAlt.getText().toString())
                            .setRealName(realName.getText().toString())
                            .setIdent(ident.getText().toString())
                            .setNickServPassword(nickservPassword.getText().toString());

                    String commands = serverCommands.getText().toString();
                    if (commands != null && commands.length() > 1) {
                        for (String command: commands.split("\n")) {
                            if (command.length() > 0)
                                serverData.addCommand(command);
                        }
                    }

                    Intent dataIntent = new Intent();
                    dataIntent.putExtra("serverData", serverData);
                    dataIntent.putExtra("editedServerPosition", editedServerPosition);
                    setResult(isEditMode ? Constants.CODES.addServerResponseEdited : Constants.CODES.addServerResponseAdded, dataIntent);
                    finish();
                }

                return false;

            case R.id.menu_server_discard:
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restoreFields() {
        Intent callingIntent = getIntent();
        serverAddress.setText(callingIntent.getStringExtra("serverAddress"));
        serverName.setText(callingIntent.getStringExtra("serverName"));
        serverPort.setText(callingIntent.getStringExtra("serverPort"));
        nickName.setText(callingIntent.getStringExtra("nickName"));
        IRCServerData serverData = (IRCServerData) callingIntent.getSerializableExtra("serverData");

        isEditMode = callingIntent.getBooleanExtra("isEditMode", false);
        editedServerPosition = callingIntent.getIntExtra("editedServerPosition", -1);

        if (serverData == null) return;

        nickNameAlt.setText(serverData.getNickNameAlt());
        ident.setText(serverData.getIdent());
        realName.setText(serverData.getRealName());
        nickservPassword.setText(serverData.getNickServPassword());
        serverCommands.setText(serverData.getServerCommandsStr());

    }

    private boolean checkFields() {
        if (serverAddress.getText().toString().length() < 1) {
            serverAddressLayout.setError(getString(R.string.dialog_main_server_address_error));
            return false;
        }
        else
            serverAddressLayout.setError("");

        if (serverPort.getText().toString().length() < 1) {
            serverPortLayout.setError(getString(R.string.dialog_main_server_port_error));
            return false;
        }
        else
            serverPortLayout.setError("");

        if (nickName.getText().toString().length() < 1) {
            nickNameLayout.setError(getString(R.string.dialog_main_server_nick_error));
            return false;
        }

        return true;
    }
}
