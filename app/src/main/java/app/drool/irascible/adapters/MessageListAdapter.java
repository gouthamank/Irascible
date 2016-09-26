package app.drool.irascible.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.laurencedawson.activetextview.ActiveTextView;

import java.text.Collator;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

import app.drool.irascible.R;
import app.drool.irascible.irc.IRCMessage;
import app.drool.irascible.utils.CacheUtils;
import app.drool.irascible.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private LinkedList<IRCMessage> messages;
    private String channelContext;
    private TreeSet<String> userList;

    public MessageListAdapter(Context context, String channelContext) {
        this.context = context;
        this.channelContext = channelContext;
        messages = new LinkedList<>();

        String prevLog = CacheUtils.getFragmentLog(context, channelContext);
        if (prevLog == null || prevLog.length() < 1) return;
        for (String line : prevLog.split("\n")) {
            addMessage(IRCMessage.parse(line));
        }
        notifyDataSetChanged();
    }

    // Public access methods
    public void addMessage(String message) {
        addMessage(IRCMessage.parse(message));
    }

    public void addMessage(IRCMessage message) {
        if (message == null || message.getMessageCode() == null)
            return;

        if (userList == null)
            userList = new TreeSet<>(Collator.getInstance());

        // Don't add this message if it doesn't belong to the fragment this adapter is attached to
        try {
            if (!message.getChannelContext().contentEquals(channelContext))
                return;
        } catch (NullPointerException e) {
            if (!channelContext.contentEquals("server"))
                return;
        }

        switch (message.getMessageCode()) {
            case IRCMessage.CODES.userListSegment:
                String[] userListSegment = message.getMessageContent().split(" ");
                Collections.addAll(userList, userListSegment);
                break;

            case IRCMessage.CODES.userListEnd:
                break;

            case IRCMessage.CODES.joinNotice:
                userList.add(message.getNickName());
                messages.add(message);
                break;

            case IRCMessage.CODES.partNotice:
            case IRCMessage.CODES.quitNotice:
                if (userList.contains(message.getNickName())) {
                    userList.remove(message.getNickName());
                    messages.add(message);
                }
                break;

            case IRCMessage.CODES.nickNotice:
                if (userList.contains(message.getNickName())) {
                    userList.remove(message.getNickName());
                    userList.add(message.getMessageContent());
                    messages.add(message);
                }
                break;

            default:
                messages.add(message);
        }

        notifyDataSetChanged();
    }

    // View Binding

    @Override
    public MessageListAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_irc_message, parent, false);
        return new MessageViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(MessageListAdapter.MessageViewHolder holder, int position) {
        IRCMessage message = messages.get(position);

        holder.content.setTypeface(null, Typeface.NORMAL);
        if (message.isInvalid()) {
            holder.author.setVisibility(View.GONE);
            holder.timestamp.setVisibility(View.GONE);
            holder.content.setVisibility(View.VISIBLE);
            holder.content.setText(message.getMessageContent());
            holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light));
        } else {
            holder.timestamp.setVisibility(View.VISIBLE);
            holder.timestamp.setText("(" +
                    Utils.getReadableDate(message.getTimestamp()) +
                    ")");
            holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light));

            switch(message.getMessageCode()) {
                case IRCMessage.CODES.welcomeMessage:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getMessageContent());
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    break;

                case IRCMessage.CODES.serverInfo:
                case IRCMessage.CODES.serverMoreInfo:
                case IRCMessage.CODES.motdStart:
                case IRCMessage.CODES.motdEnd:
                    holder.author.setVisibility(View.GONE);
                    holder.timestamp.setVisibility(View.GONE);
                    holder.content.setVisibility(View.GONE);
                    break;

                case IRCMessage.CODES.operatorCount:
                    holder.author.setVisibility(View.GONE);
                    holder.timestamp.setVisibility(View.VISIBLE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getCountVar1() + " operators online");
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    break;

                case IRCMessage.CODES.unknownConnectionCount:
                    holder.author.setVisibility(View.GONE);
                    holder.timestamp.setVisibility(View.VISIBLE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getCountVar1() + " unknown connection(s)");
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    break;

                case IRCMessage.CODES.channelCount:
                    holder.author.setVisibility(View.GONE);
                    holder.timestamp.setVisibility(View.VISIBLE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getCountVar1() + " channels created");
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    break;

                case IRCMessage.CODES.localUserCount:
                    holder.author.setVisibility(View.GONE);
                    holder.timestamp.setVisibility(View.VISIBLE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getCountVar1() + " local users online (Max " + message.getCountVar2() + ")");
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    break;

                case IRCMessage.CODES.globalUserCount:
                    holder.author.setVisibility(View.GONE);
                    holder.timestamp.setVisibility(View.VISIBLE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getCountVar1() + " global users online (Max " + message.getCountVar2() + ")");
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    break;

                case IRCMessage.CODES.motdLine:
                    holder.author.setVisibility(View.GONE);
                    holder.timestamp.setVisibility(View.GONE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(Html.fromHtml(message.getMessageContent()));
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                    break;

                case IRCMessage.CODES.channelTopic:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(Html.fromHtml(message.getMessageContent()));
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light));
                    break;

                case IRCMessage.CODES.channelTopicTime:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText("Topic set by " + message.getTopicSetBy());
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light));
                    break;

                case IRCMessage.CODES.message:
                    holder.author.setVisibility(View.VISIBLE);
                    holder.author.setText(message.getNickName());
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(Html.fromHtml(message.getMessageContent()));
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light));
                    break;

                case IRCMessage.CODES.actionMessage:
                    holder.author.setVisibility(View.VISIBLE);
                    holder.author.setText(message.getNickName());
                    holder.author.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getMessageContent());
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                    holder.content.setTypeface(null, Typeface.ITALIC);
                    break;

                case IRCMessage.CODES.noSuchNickChannel:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText("No such nick/channel: " + message.getChannelContext());
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_light));
                    break;

                case IRCMessage.CODES.joinNotice:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getNickName() + " has joined " + message.getChannelContext());
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                    break;

                case IRCMessage.CODES.nickNotice:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getNickName() + " has changed their name to " + message.getMessageContent());
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_light));
                    break;

                case IRCMessage.CODES.partNotice:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getNickName() + " has left " + message.getChannelContext());
                    if (message.getMessageContent() != null)
                        holder.content.append(" (" + message.getMessageContent() + ")");
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    break;

                case IRCMessage.CODES.modeNotice:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setVisibility(View.VISIBLE);
                    holder.content.setText(message.getOriginName() + " sets modes on " +
                                            message.getDestinationName() + ": " +
                                            message.getMessageContent());
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                    break;

                case IRCMessage.CODES.quitNotice:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setText(message.getNickName() + " has quit. (" + message.getMessageContent() + ")");
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    break;

                case IRCMessage.CODES.channelNotice:
                    holder.author.setVisibility(View.VISIBLE);
                    holder.author.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
                    holder.author.setText(message.getOriginName());
                    holder.content.setText(Html.fromHtml(message.getMessageContent()));
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
                    break;

                default:
                    holder.author.setVisibility(View.GONE);
                    holder.content.setText(message.getMessageContent());
                    holder.content.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light));
            }

            holder.content.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    try {
                        Intent webIntent = new Intent(Intent.ACTION_VIEW);
                        webIntent.setData(Uri.parse(url));
                        context.startActivity(webIntent);
                    } catch (Exception e) { }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public String[] getUserList() {
        return userList.toArray(new String[userList.size()]);
    }

    // View Holder

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_irc_message_author) TextView author;
        @BindView(R.id.list_item_irc_message_content) ActiveTextView content;
        @BindView(R.id.list_item_irc_message_timestamp) TextView timestamp;

        MessageViewHolder(LinearLayout l) {
            super(l);
            ButterKnife.bind(this, l);
        }
    }

}
