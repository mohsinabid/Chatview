package net.sofitech.chatview;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.greysonparrelli.permiso.Permiso;

import net.sofitech.chatview.model.*;
import net.sofitech.chatview.widgets.Emoji;
import net.sofitech.chatview.widgets.EmojiView;
import net.sofitech.chatview.widgets.MapsMarkerActivity;
import net.sofitech.chatview.widgets.SizeNotifierRelativeLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.codetail.animation.SupportAnimator;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class MainActivity extends AppCompatActivity implements SizeNotifierRelativeLayout.SizeNotifierRelativeLayoutDelegate, NotificationCenter.NotificationCenterDelegate {

    //    private ListView chatListView;
    private StickyListHeadersListView chatListView;
    private EditText chatEditText1;
    private ArrayList<ChatMessage> chatMessages;
    private ImageView enterChatView1, emojiButton;
    private ChatListAdapter listAdapter;
    private EmojiView emojiView;
    private SizeNotifierRelativeLayout sizeNotifierRelativeLayout;
    private boolean showingEmoji;
    private int keyboardHeight;
    private boolean keyboardVisible;
    private WindowManager.LayoutParams windowLayoutParams;
    View rootview;
    int current;
    TextView replying, replyText;
    ChatMessage replyingMsg;
    private int RC_CODE_PICKER = 2000;

    public int CONTACT_PICKER_REQUEST = 1;
    public int CAMERA_REQUEST = 1888;
    public int AUDIO_REQUEST = 2;

    public int REQUEST_CODE_OPEN_DIRECTORY = 3;
   public int PLACE_PICKER_REQUEST = 4;

    SupportAnimator animator;
    SupportAnimator animator_reverse;
    int cx,cy;
    float finalRadius;
    public GPSTracker gps;
    double lat,lng;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static ArrayList<net.sofitech.chatview.model.Location> sendLocation;

    @NonNull
    private ArrayList<Image> images = new ArrayList<>();
    @NonNull
    private ArrayList<File> allimages = new ArrayList<>();

    //final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");


    private EditText.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press

                EditText editText = (EditText) v;

                if (v == chatEditText1)

                {
                    sendMessage(editText.getText().toString(), UserType.OTHER);
                }

                chatEditText1.setText("");

                return true;
            }
            return false;

        }
    };

    private ImageView.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v == enterChatView1) {
                sendMessage(chatEditText1.getText().toString(), UserType.OTHER);
            }

            chatEditText1.setText("");

        }
    };

    private final TextWatcher watcher1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (chatEditText1.getText().toString().equals("")) {

            } else {
                enterChatView1.setImageResource(R.drawable.ic_chat_send);

            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() == 0) {
                enterChatView1.setImageResource(R.drawable.ic_chat_send);
            } else {
                enterChatView1.setImageResource(R.drawable.ic_chat_send_active);
            }
        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {

                return true;
            }
        }
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        AndroidUtilities.statusBarHeight = getStatusBarHeight();

        chatMessages = new ArrayList<>();
        sendLocation= new ArrayList<>();
        Permiso.getInstance().setActivity(getActivity());


        chatListView = (StickyListHeadersListView) findViewById(R.id.chat_list_view);

        chatEditText1 = (EditText) findViewById(R.id.chat_edit_text1);
        enterChatView1 = (ImageView) findViewById(R.id.enter_chat1);
        replying = (TextView) findViewById(R.id.extra);
        replyText = (TextView) findViewById(R.id.replyingtext);

        // Hide the emoji on click of edit text
        chatEditText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showingEmoji)
                    hideEmojiPopup();
            }
        });

        if (isMyServiceRunning(GPSTracker.class)) {
            Log.e("GPS", "Already Running");

        } else {
            Log.e("GPS", "Not Running");
            startService(new Intent(MainActivity.this, GPSTracker.class));

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

        emojiButton = (ImageView) findViewById(R.id.emojiButton);

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showEmojiPopup(!showingEmoji);
                showAttachmentPopup();
            }
        });

        listAdapter = new ChatListAdapter(chatMessages, this);

        chatListView.setAdapter(listAdapter);

        chatEditText1.setOnKeyListener(keyListener);

        enterChatView1.setOnClickListener(clickListener);

        chatEditText1.addTextChangedListener(watcher1);

        sizeNotifierRelativeLayout = (SizeNotifierRelativeLayout) findViewById(R.id.chat_layout);
        sizeNotifierRelativeLayout.delegate = this;

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);

        chatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                current = i;
                view.setBackgroundResource(0);
                view.setBackgroundResource(R.color.orange);
                rootview = view;

                return false;
            }
        });


        gps = new GPSTracker(this);
        SharedPreferences prefs = this.getSharedPreferences("key", Context.MODE_PRIVATE);

        if (gps.canGetLocation()) {

            lat = gps.getLatitude();
            lng = gps.getLongitude();
            editor=prefs.edit();
            editor.putString("lat", String.valueOf(lat));
            editor.putString("lng", String.valueOf(lng));
            editor.commit();


            // \n is for new line
            Toast.makeText(MainActivity.this, "Your Location is - \nLat: " + lat + "\nLong: " + lng, Toast.LENGTH_LONG).show();
            Log.e("Gps", " is turned oN");

        } else {

          //  gps.showSettingsAlert();
            Log.e("Gps", " is turned off");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manu_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_lang:
                replying.setVisibility(View.VISIBLE);
                replyText.setVisibility(View.VISIBLE);
                replyText.setText(chatMessages.get(current).getMessageText());
                this.replyingMsg = chatMessages.get(current);
                rootview.setBackgroundResource(0);

                return true;

        }
        return true;
    }


    void showAttachmentPopup() {

        final View dialogView = View.inflate(this,R.layout.att_dialog,null);

        Log.e("Dialog", "shown");
        final Dialog dlg = new Dialog(getActivity());
        dlg.setContentView(dialogView);
        //dlg.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
        //dlg.show();

        dlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        Window window = dlg.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        final LinearLayout layout=(LinearLayout)dlg.findViewById(R.id.dialog);

        wlp.gravity = Gravity.BOTTOM;
        wlp.verticalMargin = 0.12f;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);

        dlg.show();

        dialogView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);

                startAnim(dialogView);



            }
        });

        ImageView document = (ImageView) dlg.findViewById(R.id.document);
        ImageView camera = (ImageView) dlg.findViewById(R.id.camera);
        ImageView gallery = (ImageView) dlg.findViewById(R.id.gallery);
        ImageView audio = (ImageView) dlg.findViewById(R.id.audio);
        ImageView location = (ImageView) dlg.findViewById(R.id.location);
        ImageView contact = (ImageView) dlg.findViewById(R.id.contact);

        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
             //   revealShow(dialogView, true, dlg);

            }
        });
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        animator_reverse.addListener(new SupportAnimator.AnimatorListener() {

                            @Override
                            public void onAnimationStart() {

                            }

                            @Override
                            public void onAnimationEnd() {
                                if (dlg.isShowing()) {
                                    dlg.dismiss();
                                }
                            }

                            @Override
                            public void onAnimationCancel() {

                            }

                            @Override
                            public void onAnimationRepeat() {

                            }

                        });

                        animator_reverse.start();
                    }else
                    {
                        Animator anim = android.view.ViewAnimationUtils.
                                createCircularReveal(dialogView, cx, cy, finalRadius, 0);
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if(dlg.isShowing())
                                {
                                    dlg.dismiss();
                                }

                            }
                        });
                        anim.start();
                    }


                    return true;
                }

                return false;
            }
        });


        document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent intent = new Intent();
//                intent.setType("*/*");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(intent,REQUEST_CODE_OPEN_DIRECTORY);

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword", "application/pdf"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);

                if (dlg.isShowing()) {
                    dlg.dismiss();
                }
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {

                    Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                                                 @Override
                                                                 public void onPermissionResult(Permiso.ResultSet resultSet) {
                                                                     if (resultSet.isPermissionGranted(Manifest.permission.CAMERA)) {
                                                                         Log.e("GRanted","Done Now");

                                                                         Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                                         startActivityForResult(cameraIntent, CAMERA_REQUEST);

                                                                         if (dlg.isShowing()) {
                                                                             dlg.dismiss();
                                                                         }

                                                                     } else {

                                                                         Log.e("GRanted","Done NoT");
                                                                     }
                                                                 }

                                                                 @Override
                                                                 public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                                                     Permiso.getInstance().showRationaleInDialog("ChatView", "For better user experience, please grant these permissions", null, callback);
                                                                 }
                                                             },

                             Manifest.permission.CAMERA


                    );

                }else {

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);

                    if (dlg.isShowing()) {
                        dlg.dismiss();
                    }

                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                start();
                if (dlg.isShowing()) {
                    dlg.dismiss();
                }
            }
        });

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, AUDIO_REQUEST);
                if (dlg.isShowing()) {
                    dlg.dismiss();
                }

            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Uri gmmIntentUri = Uri.parse("geo:0,0?q=");
//                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                        mapIntent.setPackage("com.google.android.apps.maps");
//                        startActivity(mapIntent);
//                    }
//                }, 1000);


//                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//
//                try {
//                    startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
//                } catch (GooglePlayServicesRepairableException e) {
//                    Log.e("Not",e.getLocalizedMessage());
//                    e.printStackTrace();
//                } catch (GooglePlayServicesNotAvailableException e) {
//                    e.printStackTrace();
//                    Log.e("Not",e.getLocalizedMessage());
//                }
                startActivity(new Intent(MainActivity.this, MapsMarkerActivity.class));

                if (dlg.isShowing()) {
                    dlg.dismiss();
                }

            }
        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                new MultiContactPicker.Builder(MainActivity.this) //Activity/fragment context
//                        .theme(R.style.MyCustomPickerTheme) //Optional - default: Inherits project style
//                        .hideScrollbar(false) //Optional - default: false
//                        .showTrack(true) //Optional - default: true
//                        .handleColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)) //Optional - default: Green
//                        .bubbleColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)) //Optional - default: Green
//                        .textColor(Color.WHITE) //Optional - default: White
//                        .showPickerForResult(CONTACT_PICKER_REQUEST);

                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContact, CONTACT_PICKER_REQUEST);
                if (dlg.isShowing()) {
                    dlg.dismiss();
                }

            }


        });

    }

    private void startAnim(View dialogView) {
        cx = (dialogView.getLeft() + dialogView.getRight()) / 2;
        cy = (dialogView.getTop() + dialogView.getBottom()) / 2;

        // get the final radius for the clipping circle
        int dx = Math.max(cx, dialogView.getWidth() - cx);
        int dy = Math.max(cy, dialogView.getHeight() - cy);
        finalRadius = (float) Math.hypot(dx, dy);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Your previously written code

            animator = (SupportAnimator) ViewAnimationUtils.createCircularReveal(dialogView, cx, cy, 0, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(700);
            animator_reverse=animator.reverse();
            animator.start();

        } else {
            // Same code here but import ViewAnimationUtils from android.view.ViewAnimationUtils
            Animator anim = android.view.ViewAnimationUtils.createCircularReveal(dialogView, cx, cy, 0, finalRadius);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.setDuration(700);
            anim.start();
        }

    }

    public void start() {

        ImagePicker imagePicker = ImagePicker.create(this)
                .theme(R.style.ImagePickerTheme)
                .folderMode(true) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select"); // image selection title


        imagePicker.limit(10) // max images can be selected (99 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(RC_CODE_PICKER); // start image picker activity with request code
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("request", String.valueOf(requestCode));
        Log.e("result", String.valueOf(resultCode));

        if (requestCode == RC_CODE_PICKER ) {
            images = (ArrayList<Image>) ImagePicker.getImages(data);
            printImages(images);
            return;
        } else if (requestCode == CONTACT_PICKER_REQUEST)
        {

//            if(resultCode == RESULT_OK) {
//                List<ContactResult> results = MultiContactPicker.obtainResult(data);
//                Log.e("MyTag", results.get(0).getDisplayName());
//            } else if(resultCode == RESULT_CANCELED){
//                System.out.println("User closed the picker without selecting items.");
//            }

            Uri contactData = data.getData();

            Cursor c = getContentResolver().query(contactData, null, null, null, null);

                int phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex2 = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
                String num = c.getString(phoneIndex);
                String num2 = c.getString(phoneIndex2);

                Toast.makeText(MainActivity.this, "Number=" + num + num2, Toast.LENGTH_LONG).show();


        } else if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode==RESULT_OK) {
                // Get the Uri of the selected file
//                Uri uri = data.getData();
//                String uriString = uri.toString();
//                File myFile = new File(uriString);
//                String path = myFile.getAbsolutePath();
//                String displayName = null;
//
//                if (uriString.startsWith("content://")) {
//                    Cursor cursor = null;
//                    try {
//                        cursor = getContentResolver().query(uri, null, null, null, null);
//                        if (cursor != null && cursor.moveToFirst()) {
//                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                            Log.e("file", displayName);
//                        }
//                    } finally {
//                        cursor.close();
//                    }
//                } else if (uriString.startsWith("file://")) {
//
//                    displayName = myFile.getName();
//                    Log.e("filename", displayName);
//                }
                Log.e("Doc","Here");

            String FilePath = data.getData().getPath();
            Log.e("FIle",FilePath);


        } else if (requestCode == CAMERA_REQUEST && resultCode==RESULT_OK)
            {
                Log.e("Cam","Here");
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Log.e("image", photo.toString());
//            imageView.setImageBitmap(photo);

            } else if (requestCode == AUDIO_REQUEST && resultCode==RESULT_OK) {
                Log.e("Audio","Here");
                //the selected audio.
                Uri uri = data.getData();
                File file=new File(uri.getPath());
                Log.e("Path", uri.getPath());

            try {
                Log.e("ABS PATH", file.getAbsolutePath() + " CAN PATH" + file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Log.e("CANONICAL NAME", file.getCanonicalFile().getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("Filename",file.getName());

            }
        else if(requestCode==PLACE_PICKER_REQUEST)
        {
            Log.e("Loc","Here");
            Place place = PlacePicker.getPlace(data, this);
            String toastMsg = String.format("Place: %s", place.getName());
            Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
        Log.e("ON","Activity");
    }

    private void printImages(List<Image> images) {
        if (images == null) return;

        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0, l = images.size(); i < l; i++) {
            stringBuffer.append(images.get(i).getPath()).append("\n");
            String uri = stringBuffer.toString();
            File f = new File(uri);
            allimages.add(f);
        }
        Log.e("HAHA", String.valueOf(images.size()) + " photo selected");


        // photographUrl.setText(stringBuffer.toString());
    }

    private void sendMessage(final String messageText, final UserType userType) {
        replying.setVisibility(View.GONE);
        replyText.setVisibility(View.GONE);
        if (messageText.trim().length() == 0)
            return;

        final ChatMessage message = new ChatMessage();
        if (this.replyingMsg != null)
            message.setExtraMessage(this.replyingMsg.getMessageText());
        message.setMessageStatus(Status.SENT);
        message.setMessageText(messageText);
        message.setUserType(userType);
        message.setMessageTime(new Date().getTime());
        chatMessages.add(message);

        if (listAdapter != null)
            listAdapter.notifyDataSetChanged();

        // Mark message as delivered after one second

        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

        exec.schedule(new Runnable() {
            @Override
            public void run() {
                message.setMessageStatus(Status.DELIVERED);

                final ChatMessage message = new ChatMessage();
                message.setMessageStatus(Status.SENT);
                message.setMessageText(messageText);
                message.setUserType(UserType.SELF);
                message.setMessageTime(new Date().getTime());
                chatMessages.add(message);

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        replyingMsg = null;
                        listAdapter.notifyDataSetChanged();
                    }
                });


            }
        }, 1, TimeUnit.SECONDS);

    }

    private Activity getActivity() {
        return this;
    }


    /**
     * Show or hide the emoji popup
     *
     * @param show
     */
    private void showEmojiPopup(boolean show) {
        showingEmoji = show;

        if (show) {
            if (emojiView == null) {
                if (getActivity() == null) {
                    return;
                }
                emojiView = new EmojiView(getActivity());

                emojiView.setListener(new EmojiView.Listener() {
                    public void onBackspace() {
                        chatEditText1.dispatchKeyEvent(new KeyEvent(0, 67));
                    }

                    public void onEmojiSelected(String symbol) {
                        int i = chatEditText1.getSelectionEnd();
                        if (i < 0) {
                            i = 0;
                        }
                        try {
                            CharSequence localCharSequence = Emoji.replaceEmoji(symbol, chatEditText1.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20));
                            chatEditText1.setText(chatEditText1.getText().insert(i, localCharSequence));
                            int j = i + localCharSequence.length();
                            chatEditText1.setSelection(j, j);
                        } catch (Exception e) {
                            Log.e(Constants.TAG, "Error showing emoji");
                        }
                    }
                });


                windowLayoutParams = new WindowManager.LayoutParams();
                windowLayoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                if (Build.VERSION.SDK_INT >= 21) {
                    windowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                } else {
                    windowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
                    windowLayoutParams.token = getActivity().getWindow().getDecorView().getWindowToken();
                }
                windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            }

            final int currentHeight;

            if (keyboardHeight <= 0)
                keyboardHeight = App.getInstance().getSharedPreferences("emoji", 0).getInt("kbd_height", AndroidUtilities.dp(200));

            currentHeight = keyboardHeight;

            WindowManager wm = (WindowManager) App.getInstance().getSystemService(Activity.WINDOW_SERVICE);

            windowLayoutParams.height = currentHeight;
            windowLayoutParams.width = AndroidUtilities.displaySize.x;

            try {
                if (emojiView.getParent() != null) {
                    wm.removeViewImmediate(emojiView);
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, e.getMessage());
            }

            try {
                wm.addView(emojiView, windowLayoutParams);
            } catch (Exception e) {
                Log.e(Constants.TAG, e.getMessage());
                return;
            }

            if (!keyboardVisible) {
                if (sizeNotifierRelativeLayout != null) {
                    sizeNotifierRelativeLayout.setPadding(0, 0, 0, currentHeight);
                }

                return;
            }

        } else {
            removeEmojiWindow();
            if (sizeNotifierRelativeLayout != null) {
                sizeNotifierRelativeLayout.post(new Runnable() {
                    public void run() {
                        if (sizeNotifierRelativeLayout != null) {
                            sizeNotifierRelativeLayout.setPadding(0, 0, 0, 0);
                        }
                    }
                });
            }
        }


    }


    /**
     * Remove emoji window
     */
    private void removeEmojiWindow() {
        if (emojiView == null) {
            return;
        }
        try {
            if (emojiView.getParent() != null) {
                WindowManager wm = (WindowManager) App.getInstance().getSystemService(Context.WINDOW_SERVICE);
                wm.removeViewImmediate(emojiView);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, e.getMessage());
        }
    }


    /**
     * Hides the emoji popup
     */
    public void hideEmojiPopup() {
        if (showingEmoji) {
            showEmojiPopup(false);
        }
    }

    /**
     * Check if the emoji popup is showing
     *
     * @return
     */
    public boolean isEmojiPopupShowing() {
        return showingEmoji;
    }


    /**
     * Updates emoji views when they are complete loading
     *
     * @param id
     * @param args
     */
    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.emojiDidLoaded) {
            if (emojiView != null) {
                emojiView.invalidateViews();
            }

            if (chatListView != null) {
                //chatListView.invalidateViews();
            }
        }
    }

    @Override
    public void onSizeChanged(int height) {

        Rect localRect = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);

        WindowManager wm = (WindowManager) App.getInstance().getSystemService(Activity.WINDOW_SERVICE);
        if (wm == null || wm.getDefaultDisplay() == null) {
            return;
        }


        if (height > AndroidUtilities.dp(50) && keyboardVisible) {
            keyboardHeight = height;
            App.getInstance().getSharedPreferences("emoji", 0).edit().putInt("kbd_height", keyboardHeight).commit();
        }


        if (showingEmoji) {
            int newHeight = 0;

            newHeight = keyboardHeight;

            if (windowLayoutParams.width != AndroidUtilities.displaySize.x || windowLayoutParams.height != newHeight) {
                windowLayoutParams.width = AndroidUtilities.displaySize.x;
                windowLayoutParams.height = newHeight;

                wm.updateViewLayout(emojiView, windowLayoutParams);
                if (!keyboardVisible) {
                    sizeNotifierRelativeLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if (sizeNotifierRelativeLayout != null) {
                                sizeNotifierRelativeLayout.setPadding(0, 0, 0, windowLayoutParams.height);
                                sizeNotifierRelativeLayout.requestLayout();
                            }
                        }
                    });
                }
            }
        }


        boolean oldValue = keyboardVisible;
        keyboardVisible = height > 0;
        if (keyboardVisible && sizeNotifierRelativeLayout.getPaddingBottom() > 0) {
            showEmojiPopup(false);
        } else if (!keyboardVisible && keyboardVisible != oldValue && showingEmoji) {
            showEmojiPopup(false);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
    }

    /**
     * Get the system status bar height
     *
     * @return
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onPause() {
        super.onPause();

        hideEmojiPopup();
    }
}
