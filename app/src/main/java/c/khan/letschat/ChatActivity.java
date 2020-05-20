package c.khan.letschat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    Button sendbutton;
    EditText EditList;
    ListView ChatList;
    ImageButton mPhotoPickerButton;

    private static final int RC_PHOTO_PICKER = 2;

    ArrayList<String> ListChat = new ArrayList<String>();
    ArrayAdapter ChatAdpt;

    String UserName, SelectedChat, MessageKey;

    private DatabaseReference FirebaseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        sendbutton = (Button) findViewById(R.id.sendbutton);
        EditList = (EditText) findViewById(R.id.EditList);
        ChatList = (ListView) findViewById(R.id.ChatList);

        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);


        ChatAdpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ListChat);
        ChatList.setAdapter(ChatAdpt);



        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Select Photo"), RC_PHOTO_PICKER);
            }
        });

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

    }



    public void updateChat(DataSnapshot dataSnapshot){
        String message, user, chat;
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()){
            message = (String)((DataSnapshot)i.next()).getValue();
            user = (String)((DataSnapshot)i.next()).getValue();

            chat =  user +": " + message;
            ChatAdpt.insert(chat, 0);
            ChatAdpt.notifyDataSetChanged();
        }
    }
}
