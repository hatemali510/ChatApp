package com.example.hatem.chatapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hatem on 09/01/2017.
 */
//link to question in stackoverflow about showing image in popup window but i edit it to match with picasso
//http://stackoverflow.com/questions/6044793/popupwindow-with-image
public class chat_room extends AppCompatActivity {
    ImageButton send_mess;
    private static final int x=2;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> chatlist;
    EditText input_message;
    ArrayList<DataSnapshot> arratofds;
    String username, roomname;
    private DatabaseReference root;
    private String temp_key;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    Random random ;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    private StorageReference mstorage;
    private  ProgressDialog pd ;
    ImageButton uploadPhoto;
    DataSnapshot ds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_layout);
        send_mess = (ImageButton) findViewById(R.id.send_message);
        input_message = (EditText) findViewById(R.id.editText);
        username = getIntent().getExtras().get("user_name").toString();
        roomname = getIntent().getExtras().get("room_name").toString();
        chatlist = new ArrayList<>();
        arratofds = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView2);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chatlist);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ds = arratofds.get(position);
                String url, check;
                url = (String) ds.child("msg").getValue();
                check = (String) ds.child("type").getValue();
                if (check.equals("text")) {
                    listView.setClickable(false);
                }
                if (check.equals("image")) {
                    listView.setClickable(true);
                    loadPhoto(url);
                }
            }
        });
        input_message.setHint("type you message...");
        setTitle("room - " + roomname);
        root = FirebaseDatabase.getInstance().getReference().child(roomname);
        mstorage = FirebaseStorage.getInstance().getReference();
        pd = new ProgressDialog(this);

        send_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    temp_key = root.push().getKey();
                    root.updateChildren(map);
                    DatabaseReference message_root = root.child(temp_key);
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("name", username);
                    map2.put("msg", input_message.getText().toString());
                    map2.put("type", "text");
                    message_root.updateChildren(map2);
                    input_message.setText("");
            }
        });
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatlist.clear();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    arratofds.add(messageSnapshot);
                    String completemessage = "";
                    String name = (String) messageSnapshot.child("name").getValue();
                    String message = (String) messageSnapshot.child("msg").getValue();
                    if (message.contains("https://firebasestorage.googleapis.com/v0/b/chatapp-5e327.appspot.com/o/photos")) {
                        message = "Tap to view photo ... !!";
                    }
                    completemessage = name + " : " + message;
                    chatlist.add(completemessage);
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        random = new Random();
        uploadPhoto = (ImageButton) findViewById(R.id.photo);
        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, x);
            }
        });
        /*
        buttonStart = (Button) findViewById(R.id.imageButton);


        buttonStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                String filename=CreateRandomAudioFileName(5) ;
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        input_message.setText("Voice is recording ....");
                        if (checkPermission()) {
                            AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +filename + ".mp3";
                            MediaRecorderReady();
                            try {
                                mediaRecorder.prepare();
                                mediaRecorder.start();

                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //make voice
                        } else {
                            requestPermission();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        input_message.setText("");
                        mediaRecorder.stop();
                        Map<String, Object> map = new HashMap<String, Object>();
                        temp_key = root.push().getKey();
                        root.updateChildren(map);

                        DatabaseReference message_root = root.child(temp_key);
                        Map<String, Object> map2 = new HashMap<String, Object>();
                        map2.put("name", username);
                        map2.put("msg","");
                        map2.put("type","voice_mesage");
                        message_root.updateChildren(map2);
                        uploadAudio(filename);
                        break;
                }
                return false;
            }
        });


    }
    */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==x && resultCode==RESULT_OK){
            pd.setMessage("Uploading ... ");
            pd.show();
            Uri uri=data.getData();
            StorageReference filepath=mstorage.child("photos").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri imageurl=taskSnapshot.getDownloadUrl();
                    Map<String, Object> map = new HashMap<String, Object>();
                    temp_key = root.push().getKey();
                    root.updateChildren(map);

                    DatabaseReference message_root = root.child(temp_key);
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("name", username);
                    map2.put("msg",imageurl+"");
                    map2.put("type","image");
                    message_root.updateChildren(map2);
                    pd.dismiss();
                }
            });
        }
    }

    private void loadPhoto(String path) {
        AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.custom_fullimage_dialog,
                (ViewGroup) findViewById(R.id.layout_root));
        ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
        Picasso.with(this).load(path).into(image);
        imageDialog.setView(layout);
        imageDialog.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener(){

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });


        imageDialog.create();
        imageDialog.show();
    }
//------------------------------------------------------------------------------------------------
    ///////////////////////////////////////////////////////////////////////////
    //         unused function was implemented to voice message which not    //
    //////////////////////////////////////////////////////////////////////////
    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(chat_room.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(chat_room.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(chat_room.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }
    private void uploadAudio(String id) {
        pd.setMessage("uploading ... ");
        pd.show();
        StorageReference filepath = mstorage.child("Audio").child(id+".mp4");
        Uri uri = Uri.fromFile(new File(AudioSavePathInDevice));
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
            }


        });



    }

}
