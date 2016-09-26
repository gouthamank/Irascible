package app.drool.irascible.irc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import app.drool.irascible.utils.Utils;

@SuppressWarnings("WeakerAccess")
public class IRCMessage implements Serializable {
    private boolean invalid = false;
    private String countVar1;
    private String countVar2;
    private String timestamp;
    private String messageCode;
    private String nickName;
    private String userName;
    private String userHost;
    private String originName;
    private String destinationName;
    private String messageContent;
    private String quitMessage;
    private String forwardOld;
    private String forwardNew;
    private String channelContext;
    private String topicSetBy;
    private String topicSetAt;
    private String messageRaw;

    public interface CODES {
        String welcomeMessage = "welcomeMessage"; // Blanket code for 001, 002,
                                                  // 003, 251, 255, 250
        String serverInfo = "004"; // Work on this
        String serverMoreInfo = "005"; // *
        String operatorCount = "252";
        String unknownConnectionCount = "253";
        String channelCount = "254";
        String localUserCount = "265";
        String globalUserCount = "266";

        String motdStart = "375"; // *
        String motdLine = "372";
        String motdEnd = "376"; // *

        String joinNotice = "JOIN";
        String quitNotice = "QUIT";
        String partNotice = "PART";
        String modeNotice = "MODE";
        String nickNotice = "NICK";
        String channelNotice = "NOTICE";
        String message = "PRIVMSG";
        String actionMessage = " ACTION";

        String channelForward = "470";
        String channelNoTopic = "331";
        String channelTopic = "332";
        String channelTopicTime = "333";
        String channelURL = "328";

        String userListSegment = "353";
        String userListEnd = "366";
    }

    public static IRCMessage parse(String messageRaw) {
        LinkedList<String> messageParts = new LinkedList<>();
        Collections.addAll(messageParts, messageRaw.trim().split(" "));
        IRCMessage message = new IRCMessage();

        if (messageParts.size() <= 1) {
            message.setMessageContent(messageRaw);
            message.setInvalid(true);
            return message;
        }

        message.setMessageRaw(messageRaw);
        message.setTimestamp(messageParts.get(0));

        if (!messageParts.get(1).startsWith(":")) {
            message.setMessageContent(messageRaw);
            message.setInvalid(true);
            return message;
        }

        switch(messageParts.get(2)) {
            case "001":
            case "002":
            case "003":
            case "251":
            case "255":
            case "250": {
                message.setMessageCode(CODES.welcomeMessage);
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                message.setChannelContext(null);
                List<String> messageList = messageParts.subList(4, messageParts.size());
                message.setMessageContent(Utils.joinStrings(messageList).substring(1));
                break;
            }

            case CODES.serverInfo: {
                message.setMessageCode(CODES.serverInfo);
                message.setChannelContext(null);
                break;
            }

            case CODES.serverMoreInfo: {
                message.setMessageCode(CODES.serverMoreInfo);
                message.setChannelContext(null);
                break;
            }

            case CODES.operatorCount:
            case CODES.unknownConnectionCount:
            case CODES.channelCount: {
                message.setMessageCode(messageParts.get(2));
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                message.setCountVar1(messageParts.get(4));
                message.setChannelContext(null);
                List<String> messageList = messageParts.subList(5, messageParts.size());
                message.setMessageContent(Utils.joinStrings(messageList).substring(1));
                break;
            }

            case CODES.localUserCount:
            case CODES.globalUserCount: {
                message.setMessageCode(messageParts.get(2));
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                message.setCountVar1(messageParts.get(4));
                message.setCountVar2(messageParts.get(5));
                message.setChannelContext(null);
                List<String> messageList = messageParts.subList(6, messageParts.size());
                message.setMessageContent(Utils.joinStrings(messageList).substring(1));
                break;
            }


            case CODES.motdStart: {
                message.setMessageCode(CODES.motdStart);
                message.setChannelContext(null);
                break;
            }

            case CODES.motdLine: {
                message.setMessageCode(CODES.motdLine);
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                message.setChannelContext(null);

                List<String> messageContent = new ArrayList<>();
                String[] origMessageList = messageParts.subList(4, messageParts.size()).toArray(new String[messageParts.size() - 4]);
                for (int i = 0; i < origMessageList.length; i++) {
                    String piece = origMessageList[i];
                    if (i == 0) piece = piece.substring(1);
                    if (piece.startsWith("https://") || piece.startsWith("http://") || piece.startsWith("www.")) {
                        messageContent.add("<a href = \"" + piece + "\">" + piece + "</a>");
                    }
                    else messageContent.add(piece);
                }
                message.setMessageContent(Utils.joinStrings(messageContent));
                break;
            }

            case CODES.motdEnd: {
                message.setMessageCode(CODES.motdEnd);
                message.setChannelContext(null);
                break;
            }

            case CODES.joinNotice: {
                message.setMessageCode(CODES.joinNotice);
                String userBlock = messageParts.get(1).substring(1);
                userBlock = userBlock.replace("!", " ").replace("@", " ");
                String[] userBlockSplit = userBlock.split(" ");
                if (userBlockSplit.length != 3) {
                    message.setMessageContent(messageRaw);
                    message.setInvalid(true);
                    break;
                }
                message.setNickName(userBlockSplit[0]);
                message.setUserName(userBlockSplit[1]);
                message.setUserHost(userBlockSplit[2]);
                message.setChannelContext(messageParts.get(3));
                break;
            }

            case CODES.quitNotice: {
                message.setMessageCode(CODES.quitNotice);
                String userBlock = messageParts.get(1).substring(1);
                userBlock = userBlock.replace("!", " ").replace("@", " ");
                String[] userBlockSplit = userBlock.split(" ");
                if (userBlockSplit.length != 3) {
                    message.setMessageContent(messageRaw);
                    message.setInvalid(true);
                    break;
                }
                message.setNickName(userBlockSplit[0]);
                message.setUserName(userBlockSplit[1]);
                message.setUserHost(userBlockSplit[2]);
                if (!messageParts.get(3).startsWith(":")) {
                    message.setMessageContent(messageRaw);
                    message.setInvalid(true);
                    break;
                }
                List<String> quitMessageTag = messageParts.subList(3, messageParts.size());
                message.setMessageContent(Utils.joinStrings(quitMessageTag).substring(1));
                message.setChannelContext(null);
                break;
            }

            case CODES.partNotice: {
                message.setMessageCode(CODES.partNotice);
                String userBlock = messageParts.get(1).substring(1);
                userBlock = userBlock.replace("!", " ").replace("@", " ");
                String[] userBlockSplit = userBlock.split(" ");
                if (userBlockSplit.length != 3) {
                    message.setMessageContent(messageRaw);
                    message.setInvalid(true);
                    break;
                }
                message.setNickName(userBlockSplit[0]);
                message.setUserName(userBlockSplit[1]);
                message.setUserHost(userBlockSplit[2]);
                message.setChannelContext(messageParts.get(3));
                List<String> partMessageTag = messageParts.subList(4, messageParts.size());
                if (partMessageTag.size() > 0)
                    message.setMessageContent(Utils.joinStrings(partMessageTag).substring(1));
                break;
            }

            case CODES.modeNotice: {
                message.setMessageCode(CODES.modeNotice);
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                List<String> messageList = messageParts.subList(4, messageParts.size());
                message.setMessageContent(Utils.joinStrings(messageList).substring(1));
                break;
            }

            case CODES.nickNotice: {
                message.setMessageCode(CODES.nickNotice);
                String userBlock = messageParts.get(1).substring(1);
                userBlock = userBlock.replace("!", " ").replace("@", " ");
                String[] userBlockSplit = userBlock.split(" ");
                if (userBlockSplit.length != 3) {
                    message.setMessageContent(messageRaw);
                    message.setInvalid(true);
                    break;
                }
                message.setNickName(userBlockSplit[0]);
                message.setUserName(userBlockSplit[1]);
                message.setUserHost(userBlockSplit[2]);

                List<String> messageList = messageParts.subList(3, messageParts.size());
                message.setMessageContent(Utils.joinStrings(messageList).substring(1));
                break;
            }

            case CODES.channelNotice: {
                message.setMessageCode(CODES.channelNotice);
                message.setDestinationName(messageParts.get(3));
                List<String> messageContent = new ArrayList<>();
                String[] origMessageList = messageParts.subList(4, messageParts.size()).toArray(new String[messageParts.size() - 4]);
                for (int i = 0; i < origMessageList.length; i++) {
                    String piece = origMessageList[i];
                    if (i == 0) piece = piece.substring(1);
                    if (piece.startsWith("https://") || piece.startsWith("http://") || piece.startsWith("www.")) {
                        messageContent.add("<a href = \"" + piece + "\">" + piece + "</a>");
                    }
                    else messageContent.add(piece);
                }
                message.setMessageContent(Utils.joinStrings(messageContent));
                break;
            }

            case CODES.message: {
                message.setMessageCode(CODES.message);
                String userBlock = messageParts.get(1).substring(1);
                userBlock = userBlock.replace("!", " ").replace("@", " ");
                String[] userBlockSplit = userBlock.split(" ");
                if (userBlockSplit.length != 3) {
                    message.setMessageContent(messageRaw);
                    message.setInvalid(true);
                    break;
                }
                message.setNickName(userBlockSplit[0]);
                message.setUserName(userBlockSplit[1]);
                message.setUserHost(userBlockSplit[2]);
                message.setChannelContext(messageParts.get(3));
                List<String> messageContent = new ArrayList<>();
                String[] origMessageList = messageParts.subList(4, messageParts.size()).toArray(new String[messageParts.size() - 4]);
                for (int i = 0; i < origMessageList.length; i++) {
                    String piece = origMessageList[i];
                    if (i == 0) piece = piece.substring(1);
                    if (piece.startsWith("https://") || piece.startsWith("http://") || piece.startsWith("www.")) {
                        messageContent.add("<a href = \"" + piece + "\">" + piece + "</a>");
                    }
                    else messageContent.add(piece);
                }
                message.setMessageContent(Utils.joinStrings(messageContent));
                if (message.getMessageContent().startsWith("\u0001ACTION")) {
                    message.setMessageCode(CODES.actionMessage);
                    String oldContent = message.getMessageContent();
                    String newContent = oldContent.replaceFirst("\u0001ACTION", "");
                    message.setMessageContent(newContent);
                }
                break;
            }

            case CODES.channelForward: {
                message.setMessageCode(CODES.channelForward);
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                message.setForwardOld(messageParts.get(4));
                message.setForwardNew(messageParts.get(5));
                List<String> messageContent = messageParts.subList(6, messageParts.size());
                message.setMessageContent(Utils.joinStrings(messageContent).substring(1));
                message.setChannelContext(messageParts.get(5));
                break;
            }

            case CODES.channelNoTopic: {
                message.setMessageCode(CODES.channelNoTopic);
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                message.setChannelContext(messageParts.get(4));
                message.setMessageContent("");
                break;
            }

            case CODES.channelTopic: {
                message.setMessageCode(CODES.channelTopic);
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                message.setChannelContext(messageParts.get(4));

                List<String> messageContent = new ArrayList<>();
                String[] origMessageList = messageParts.subList(5, messageParts.size()).toArray(new String[messageParts.size() - 5]);
                for (int i = 0; i < origMessageList.length; i++) {
                    String piece = origMessageList[i];
                    if (i == 0) piece = piece.substring(1);
                    if (piece.startsWith("https://") || piece.startsWith("http://") || piece.startsWith("www.")) {
                        messageContent.add("<a href = \"" + piece + "\">" + piece + "</a>");
                    }
                    else messageContent.add(piece);
                }
                message.setMessageContent(Utils.joinStrings(messageContent));
                break;
            }

            case CODES.channelTopicTime: {
                message.setMessageCode(CODES.channelTopicTime);
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                message.setChannelContext(messageParts.get(4));
                message.setTopicSetBy(messageParts.get(5));
                message.setTopicSetAt(messageParts.get(6));
                break;
            }

            case CODES.channelURL: {
                message.setMessageCode(CODES.channelURL);
                message.setOriginName(messageParts.get(1).substring(1));
                message.setDestinationName(messageParts.get(3));
                message.setChannelContext(messageParts.get(4));
                message.setMessageContent(messageParts.get(5).substring(1));
                break;
            }

            case CODES.userListSegment: {
                message.setMessageCode(CODES.userListSegment);
                message.setDestinationName(messageParts.get(3));
                message.setChannelContext(messageParts.get(5));
                List<String> messageContent = messageParts.subList(6, messageParts.size());
                message.setMessageContent(Utils.joinStrings(messageContent).substring(1));
                break;
            }

            case CODES.userListEnd: {
                message.setMessageCode(CODES.userListEnd);
                message.setDestinationName(messageParts.get(3));
                message.setChannelContext(messageParts.get(4));
                break;
            }

            default: {
                message.setInvalid(true);
                message.setMessageContent(messageRaw);
            }
        }
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public String getCountVar1() {
        return countVar1;
    }

    public void setCountVar1(String countVar1) {
        this.countVar1 = countVar1;
    }

    public String getCountVar2() {
        return countVar2;
    }

    public void setCountVar2(String countVar2) {
        this.countVar2 = countVar2;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserHost() {
        return userHost;
    }

    public void setUserHost(String userHost) {
        this.userHost = userHost;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getQuitMessage() {
        return quitMessage;
    }

    public void setQuitMessage(String quitMessage) {
        this.quitMessage = quitMessage;
    }

    public String getForwardOld() {
        return forwardOld;
    }

    public void setForwardOld(String forwardOld) {
        this.forwardOld = forwardOld;
    }

    public String getForwardNew() {
        return forwardNew;
    }

    public void setForwardNew(String forwardNew) {
        this.forwardNew = forwardNew;
    }

    public String getTopicSetBy() {
        return topicSetBy;
    }

    public void setTopicSetBy(String topicSetBy) {
        this.topicSetBy = topicSetBy;
    }

    public String getTopicSetAt() {
        return topicSetAt;
    }

    public void setTopicSetAt(String topicSetAt) {
        this.topicSetAt = topicSetAt;
    }

    public String getChannelContext() {
        return channelContext;
    }

    public void setChannelContext(String channelContext) {
        this.channelContext = channelContext;
    }

    public String getMessageRaw() {
        return messageRaw;
    }

    public void setMessageRaw(String messageRaw) {
        this.messageRaw = messageRaw;
    }

    @Override
    public String toString() {
        if (invalid)
            return "Invalid message";

        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder buffer = new StringBuilder();
        buffer.append("Timestamp: ").append(getTimestamp());
        buffer.append("\nCode: ").append(getMessageCode());
        buffer.append("\nVar 1: ").append(getCountVar1());
        buffer.append("\nVar 2: ").append(getCountVar2());
        buffer.append("\nNickname: ").append(getNickName());
        buffer.append("\nUsername: ").append(getUserName());
        buffer.append("\nUser Host: ").append(getUserHost());
        buffer.append("\nOrigin: ").append(getOriginName());
        buffer.append("\nDestination: ").append(getDestinationName());
        buffer.append("\nChannel Context: ").append(getChannelContext());
        buffer.append("\nContent: ").append(getMessageContent());
        buffer.append("\nQuit Message: ").append(getQuitMessage());
        buffer.append("\nForward Old: ").append(getForwardOld());
        buffer.append("\nForward New: ").append(getForwardNew());
        buffer.append("\nSet-By: ").append(getTopicSetBy());
        buffer.append("\nSet-At: ").append(getTopicSetAt());
        buffer.append("\n");

        return buffer.toString();
    }
}
