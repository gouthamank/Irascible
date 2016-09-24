package app.drool.irascible.irc;

import java.io.Serializable;
import java.util.ArrayList;

import app.drool.irascible.utils.Utils;

@SuppressWarnings("unused")
public class IRCServerData implements Serializable{
    private String nickName;
    private String nickNameAlt;
    private String ident;
    private String realName;
    private String nickServPassword;
    private ArrayList<String> serverCommands;
    private String serverAddress;
    private int serverPort;
    private String serverName;

    public IRCServerData() {
        this.ident = "irascible";
        this.realName = "Irascible User";
        this.serverCommands = new ArrayList<>();
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public IRCServerData setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
        return this;
    }

    public int getServerPort() {
        return serverPort;
    }

    public IRCServerData setServerPort(int serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public IRCServerData setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public String getNickName() {
        return nickName;
    }

    public IRCServerData setNickName(String nickName) {
        this.nickName = nickName;
        return this;
    }

    public String getNickNameAlt() {
        return nickNameAlt;
    }

    public IRCServerData setNickNameAlt(String nickNameAlt) {
        this.nickNameAlt = nickNameAlt;
        return this;
    }

    public String getIdent() {
        return ident;
    }

    public IRCServerData setIdent(String ident) {
        this.ident = ident;
        return this;
    }

    public String getRealName() {
        return realName;
    }

    public IRCServerData setRealName(String realName) {
        this.realName = realName;
        return this;
    }

    public String getNickServPassword() {
        return nickServPassword;
    }

    public IRCServerData setNickServPassword(String nickServPassword) {
        this.nickServPassword = nickServPassword;
        return this;
    }

    public ArrayList<String> getServerCommands() {
        return serverCommands;
    }

    public String getServerCommandsStr() {
        return Utils.joinStrings(this.serverCommands, "\n");
    }

    public IRCServerData setServerCommands(ArrayList<String> serverCommands) {
        this.serverCommands = serverCommands;
        return this;
    }

    public IRCServerData addCommand(String command) {
        this.serverCommands.add(command);
        return this;
    }
}
