package c.khan.letschat;

import android.content.Intent;
import android.icu.text.DateTimePatternGenerator;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Tag;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.type.Date;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    Button sendbutton;
    EditText EditList;
    ListView ChatList;
    //ImageButton mPhotoPickerButton;

    private static final int RC_PHOTO_PICKER = 2;

    ArrayList<String> ListChat = new ArrayList<String>();
    ArrayList<String> ShowList = new ArrayList<String>();
    ArrayAdapter ChatAdpt;

    public static final String Chat_Length = "text_limit";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;


    String UserName, SelectedChat, MessageKey;

    private DatabaseReference FirebaseDB;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        sendbutton = (Button) findViewById(R.id.sendbutton);
        EditList = (EditText) findViewById(R.id.EditList);
        ChatList = (ListView) findViewById(R.id.ChatList);

        //mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);


        ChatAdpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ListChat);
        ChatList.setAdapter(ChatAdpt);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(Chat_Length, DEFAULT_MSG_LENGTH_LIMIT);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();



       /* mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Select Photo"), RC_PHOTO_PICKER);
            }
        });*/

        UserName = getIntent().getExtras().get("user_name").toString();
        SelectedChat = getIntent().getExtras().get("selected_topic").toString();
        setTitle("Chat with: "+SelectedChat);

        FirebaseDB = FirebaseDatabase.getInstance().getReference().child(SelectedChat);

        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> map = new HashMap<String, Object>();
                MessageKey = FirebaseDB.push().getKey();
                FirebaseDB.updateChildren(map);

                DatabaseReference FirebaseDB2 = FirebaseDB.child(MessageKey);
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put("Message", EditList.getText().toString());
                map2.put("User", UserName);
                FirebaseDB2.updateChildren(map2);

            }
        });

        FirebaseDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateChat(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateChat(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        EditList.setFilters(new InputFilter[]
                {
                        new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)

                });

    }



    public void updateChat(DataSnapshot dataSnapshot){
        String message, user, chat;
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()){
            message = (String)((DataSnapshot)i.next()).getValue();
            user = (String)((DataSnapshot)i.next()).getValue();
           // date = (Date) ((DataSnapshot)i.next()).getValue();

            chat =  user +": " + message ;
            ChatAdpt.insert(chat,0 );
            ChatAdpt.notifyDataSetChanged();
        }
    }

    public void fetchConfig(){
        long cacheExpiration = 3600;

        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()){
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFirebaseRemoteConfig.activateFetched();
                applylength();
            }


        })

        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error fetching config", e);
                applylength();
            }
        });
    }

    private void applylength(){
        Long textlength = mFirebaseRemoteConfig.getLong(Chat_Length);
        EditList.setFilters(new InputFilter[]
                {
                        new InputFilter.LengthFilter(textlength.intValue())

        });

        Log.d(TAG, Chat_Length + " = "+textlength);
    }
}
