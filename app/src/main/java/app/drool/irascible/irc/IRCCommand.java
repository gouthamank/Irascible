package app.drool.irascible.irc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import app.drool.irascible.utils.Utils;

public class IRCCommand {
    private boolean isSelfCommand = false;
    private boolean isValid = true;
    private String formattedMessage;

    public interface COMMANDS {
        String action1 = "/action";
        String action2 = "/me";
        String message = "/msg";
        String join1 = "/join";
        String join2 = "/j";
        String nick = "/nick";
        String notice = "/notice";
        String part = "/part";
        String query = "/query";
        String quit = "/quit";
        String raw = "/raw";
    }

    public static IRCCommand parse(String rawText, String pageTitle) {
        IRCCommand command = new IRCCommand();
        command.setFormattedMessage("");
        LinkedList<String> messageParts = new LinkedList<>();
        Collections.addAll(messageParts, rawText.split(" "));
        for (String part: messageParts) {
            if (part.length() < 1) {
                command.setValid(false);
                break;
            }
        }

        switch(messageParts.get(0)) {
            case COMMANDS.action1:
            case COMMANDS.action2: {
                if (messageParts.size() < 2) {
                    command.setValid(false);
                    break;
                }
                command.appendFormattedMessage("PRIVMSG " + pageTitle + " :\u0001ACTION ");
                List<String> messageList = messageParts.subList(1, messageParts.size());
                command.appendFormattedMessage(Utils.joinStrings(messageList));
                command.appendFormattedMessage("\u0001");
                command.setSelfCommand(true);
                break;
            }

            case COMMANDS.message: {
                if (messageParts.size() < 2) {
                    command.setValid(false);
                    break;
                }
                command.appendFormattedMessage("PRIVMSG " + messageParts.get(1) + " :");
                List<String> messageList = messageParts.subList(2, messageParts.size());
                command.appendFormattedMessage(Utils.joinStrings(messageList));
                command.setSelfCommand(true);
                break;
            }

            case COMMANDS.join1:
            case COMMANDS.join2: {
                command.appendFormattedMessage("JOIN ");
                if (messageParts.size() < 2) {
                    command.setValid(false);
                    break;
                }
                List<String> messageList = messageParts.subList(1, messageParts.size());
                List<String> newMessageList = new ArrayList<>(messageList.size());
                for (String piece : messageList) {
                    if (piece.startsWith("#")) newMessageList.add(piece);
                    else newMessageList.add("#" + piece);
                }
                command.appendFormattedMessage(Utils.joinStrings(newMessageList));
                break;
            }

            case COMMANDS.nick: {
                if (messageParts.size() < 2) {
                    command.setValid(false);
                    break;
                }
                command.appendFormattedMessage("NICK ");
                command.appendFormattedMessage(messageParts.get(1));
                break;
            }

            case COMMANDS.notice: {
                if (messageParts.size() < 3) {
                    command.setValid(false);
                    break;
                }
                command.appendFormattedMessage("NOTICE ");
                List<String> messageList = messageParts.subList(1, messageParts.size());
                command.appendFormattedMessage(Utils.joinStrings(messageList));
                break;
            }

            case COMMANDS.part: {
                command.appendFormattedMessage("PART ");
                if (messageParts.size() > 1) {
                    command.appendFormattedMessage(messageParts.get(1));
                    if (messageParts.size() > 2) {
                        command.appendFormattedMessage(" :");
                        List<String> messageList = messageParts.subList(2, messageParts.size());
                        command.appendFormattedMessage(Utils.joinStrings(messageList));
                    }
                }
                break;
            }

            case COMMANDS.query: {
                command.appendFormattedMessage("PRIVMSG ");
                if (messageParts.size() <= 2 || messageParts.get(1).startsWith("#")) {
                    command.setValid(false);
                    break;
                }
                command.appendFormattedMessage(messageParts.get(1));
                command.appendFormattedMessage(" :");
                List<String> messageList = messageParts.subList(2, messageParts.size());
                command.appendFormattedMessage(Utils.joinStrings(messageList));
                command.setSelfCommand(true);
                break;
            }

            case COMMANDS.quit: {
                command.appendFormattedMessage("QUIT");
                break;
            }

            case COMMANDS.raw: {
                if (messageParts.size() < 2) {
                    command.setValid(false);
                    break;
                }
                List<String> messageList = messageParts.subList(1, messageParts.size());
                command.appendFormattedMessage(Utils.joinStrings(messageList));
                break;
            }

            default:
                command.setValid(false);
        }
        return command;
    }

    public boolean isSelfCommand() {
        return isSelfCommand;
    }

    public void setSelfCommand(boolean selfCommand) {
        isSelfCommand = selfCommand;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getFormattedMessage() {
        return formattedMessage;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    public void appendFormattedMessage(String piece) {
        this.formattedMessage += piece;
    }
}
