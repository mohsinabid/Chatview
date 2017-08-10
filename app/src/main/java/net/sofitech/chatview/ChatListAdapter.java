package net.sofitech.chatview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


import net.sofitech.chatview.model.ChatMessage;
import net.sofitech.chatview.model.Status;
import net.sofitech.chatview.model.UserType;
import net.sofitech.chatview.widgets.Emoji;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by madhur on 17/01/15.
 */
public class ChatListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private ArrayList<ChatMessage> chatMessages;
    private Context context;
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private LayoutInflater inflater;

    public ChatListAdapter(ArrayList<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        ChatMessage message = chatMessages.get(position);
        ViewHolder1 holder1;
        ViewHolder2 holder2;

        if (message.getUserType() == UserType.SELF) {
            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_user1_item, null, false);
                holder1 = new ViewHolder1();


                holder1.messageTextView = (TextView) v.findViewById(R.id.message_text);
                holder1.timeTextView = (TextView) v.findViewById(R.id.time_text);
                holder1.firstlinearLayout=(LinearLayout)v.findViewById(R.id.hideLinear);
                holder1.extra1=(TextView)v.findViewById(R.id.extraText);

                v.setTag(holder1);
            } else {
                v = convertView;
                holder1 = (ViewHolder1) v.getTag();

            }

            if(message.getExtraMessage()!=null)
            {
                holder1.extra1.setText(message.getExtraMessage());
                holder1.firstlinearLayout.setVisibility(View.VISIBLE);
            }
            holder1.messageTextView.setText(Emoji.replaceEmoji(message.getMessageText(), holder1.messageTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(16)));
            holder1.timeTextView.setText(SIMPLE_DATE_FORMAT.format(message.getMessageTime()));

        } else if (message.getUserType() == UserType.OTHER) {

            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_user2_item, null, false);

                holder2 = new ViewHolder2();

                holder2.messageTextView = (TextView) v.findViewById(R.id.message_text);
                holder2.timeTextView = (TextView) v.findViewById(R.id.time_text);
                holder2.messageStatus = (ImageView) v.findViewById(R.id.user_reply_status);
                holder2.secondlinearLayout=(LinearLayout)v.findViewById(R.id.hideLinear);
                holder2.extra2=(TextView)v.findViewById(R.id.extraText);
                v.setTag(holder2);

            } else {
                v = convertView;
                holder2 = (ViewHolder2) v.getTag();

            }

            if(message.getExtraMessage()!=null)
            {
                holder2.extra2.setText(message.getExtraMessage());
                holder2.secondlinearLayout.setVisibility(View.VISIBLE);
            }
            holder2.messageTextView.setText(Emoji.replaceEmoji(message.getMessageText(), holder2.messageTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(16) ));
            //holder2.messageTextView.setText(message.getMessageText());
            holder2.timeTextView.setText(SIMPLE_DATE_FORMAT.format(message.getMessageTime()));

            if (message.getMessageStatus() == Status.DELIVERED) {
                holder2.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_double_tick));
            } else if (message.getMessageStatus() == Status.SENT) {
                holder2.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_single_tick));

            }


        }


        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return message.getUserType().ordinal();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.header_layout, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        String headerText = formattedDate(getDateLong(chatMessages.get(position).getMessageTime()));
        holder.text.setText(headerText);
        return convertView;
    }

    public long getDateLong(long time)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public String formattedDate(long time)
    {

        String dateFormat = "dd MMM yyyy";
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return formatter.format(calendar.getTime());

    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon

        //String dateString = new SimpleDateFormat("MM/dd/yyyy").format(new Date(chatMessages.get(position).getMessageTime()));

//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis( chatMessages.get(position).getMessageTime() );
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);


        return getDateLong( chatMessages.get(position).getMessageTime() );//calendar.getTimeInMillis();//countries[position].subSequence(0, 1).charAt(0);
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView text;
    }

    private class ViewHolder1 {
        public TextView messageTextView;
        public TextView timeTextView;
        public LinearLayout firstlinearLayout;
        public TextView extra1;


    }

    private class ViewHolder2 {
        public ImageView messageStatus;
        public TextView messageTextView;
        public TextView timeTextView;
        public LinearLayout secondlinearLayout;
        public TextView extra2;

    }
}
