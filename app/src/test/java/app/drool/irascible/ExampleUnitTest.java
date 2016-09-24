package app.drool.irascible;

import org.junit.Test;

import app.drool.irascible.irc.IRCMessage;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    @Test
    public void parse_isCorrect() throws Exception {
        /*
        IRCMessage joinMessage = IRCMessage.parse("21321231 :droooool!~droooool@117.236.191.14 JOIN ##linux");
        System.out.println(joinMessage);
        IRCMessage quitMessage = IRCMessage.parse("21321231 :Me-35-M-IT!~me@41.187.116.218 QUIT :Quit: Kiss my shiny metal ass");
        System.out.println(quitMessage);
        IRCMessage channelNotice = IRCMessage.parse("213123213 :ChanServ!ChanServ@services. NOTICE droooool :[##linux] Please see https://freenode.linux.community/how-to-connect/ on how to register or identify your nick. By joining this channel you agree to abide by the channel rules and guidelines laid out at https://freenode.linux.community/channel-rules/. The official ##Linux bots log all channel activity and make it publicly available on https://linux.community and https://linux.chat.");
        System.out.println(channelNotice);
        IRCMessage channelMessage = IRCMessage.parse("123 :felden!~felden@unaffiliated/felden PRIVMSG ##linux :it was not uninstalled correctly and had to run rpm -e on it");
        System.out.println(channelMessage);
        IRCMessage forwardMessage = IRCMessage.parse("123123 :tepper.freenode.net 470 droooool #linux ##linux :Forwarding to another channel");
        System.out.println(forwardMessage);
        */
        IRCMessage initNotice = IRCMessage.parse("123123 :hitchcock.freenode.net NOTICE * :*** Looking up your hostname...");
        System.out.println(initNotice);
        assertEquals(1, 1);
    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}