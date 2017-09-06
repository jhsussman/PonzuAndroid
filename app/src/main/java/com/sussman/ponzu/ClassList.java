package com.sussman.ponzu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ClassList extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseStorage storage=FirebaseStorage.getInstance();
    private File listFile;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseRef=null;
    private ArrayList<String> classArrayList=new ArrayList();
    private ArrayList<String> myExistingClassArrayList=new ArrayList();
    private ArrayList<String> myClassArrayList=new ArrayList();
    private ListView USCClassList;
    private Checks[] checkBoxes;
    private CheckboxAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_list);
        databaseRef=database.getReferenceFromUrl("https://ponzu-5e308.firebaseio.com/").child("Users");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };

        StorageReference listRef = storage.getReferenceFromUrl("gs://ponzu-5e308.appspot.com/USCClassList.txt");
        String UserID=mAuth.getCurrentUser().getUid();
        databaseRef.child(UserID).child("EnrolledClasses").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String addClass = dataSnapshot.getValue().toString();
                myExistingClassArrayList.add(addClass);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        try {
            listFile = File.createTempFile("USCClassList", "txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        listRef.getFile(listFile).addOnSuccessListener(ClassList.this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)  {
                try {
                BufferedReader reader = new BufferedReader(new FileReader(listFile));
                    String classItem=reader.readLine();
                while(classItem != null) {
                    classArrayList.add(classItem);
                    classItem=reader.readLine();
                }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                checkBoxify();
                displayList();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int errorCode = ((StorageException) exception).getErrorCode();
                String errorMessage = exception.getMessage();
                Toast.makeText(ClassList.this, errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });

        Button submitClasses=(Button)findViewById(R.id.submitClassList);
        submitClasses.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(myClassArrayList.isEmpty()){
                    Toast.makeText(ClassList.this, "Please select at least one class.",
                            Toast.LENGTH_LONG).show();
                }else {
                    String UserID = mAuth.getCurrentUser().getUid();
                    for(int j=0; j<myExistingClassArrayList.size(); j++){
                        if(myClassArrayList.contains(myExistingClassArrayList.get(j))){
                            Toast.makeText(ClassList.this, myExistingClassArrayList.get(j).toString()+" was not added. Already in course list.",
                                    Toast.LENGTH_LONG).show();
                        }else{
                            myClassArrayList.add(myExistingClassArrayList.get(j).toString());
                            Collections.sort(myClassArrayList);
                        }
                    }
                        databaseRef.child(UserID).child("EnrolledClasses").setValue(myClassArrayList).addOnSuccessListener(ClassList.this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent = new Intent(ClassList.this, MyClassList.class);
                                startActivity(intent);
                            }
                        });

                }
            }
        });

    }

    public void displayList(){
        USCClassList = (ListView)findViewById(R.id.classList);
        arrayAdapter=new CheckboxAdapter(this, checkBoxes, classArrayList);
        USCClassList.setAdapter(arrayAdapter);
        USCClassList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox justClicked=(CheckBox)view.findViewById(R.id.checkBox1);
                justClicked.toggle();
                TextView className=(TextView)view.findViewById(R.id.specificClassText);
                if(justClicked.isChecked()==true){
                    myClassArrayList.add(className.getText().toString());
                }else{
                    myClassArrayList.remove(className.getText().toString());
                }
            }});
    }

    public void checkBoxify(){
        int numChecks=classArrayList.size();
        checkBoxes=new Checks[numChecks];
        for(int i=0; i<numChecks; i++){
            checkBoxes[i]=new Checks(classArrayList.get(i), 0);
        }
    }
    @Override
    public void onStart() {
       super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        MenuItem item=menu.findItem(R.id.classSearch);
        SearchView searchView=(SearchView)item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query){
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText){
                arrayAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle item selection
            switch (item.getItemId()) {
                case R.id.logoutMenu:
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(ClassList.this, MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.settingsMenu:
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

    @Override
    public void onBackPressed() {

    }

}