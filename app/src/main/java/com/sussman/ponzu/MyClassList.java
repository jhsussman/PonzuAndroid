package com.sussman.ponzu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MyClassList extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseRef=null;
    private ArrayList<String> myClassArrayList=new ArrayList();
    private ListView MyClassList;
    private ArrayAdapter<String> myArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_class_list);
        databaseRef=database.getReferenceFromUrl("https://ponzu-5e308.firebaseio.com/").child("Users");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };
        String UserID=mAuth.getCurrentUser().getUid();
        databaseRef.child(UserID).child("EnrolledClasses").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String addClass = dataSnapshot.getValue().toString();
                myClassArrayList.add(addClass);
                displayList();
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

        Button addClasses=(Button)findViewById(R.id.addMoreClasses);
        addClasses.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MyClassList.this, ClassList.class);
                startActivity(intent);
            }
        });

}

    public void displayList() {
        MyClassList = (ListView) findViewById(R.id.myClassList);
        MyClassList.setLongClickable(true);
        myArrayAdapter = new ArrayAdapter<String>(MyClassList.this, android.R.layout.simple_list_item_1, myClassArrayList);
        MyClassList.setAdapter(myArrayAdapter);
        registerForContextMenu(MyClassList);
        MyClassList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String classToEnter = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(MyClassList.this, AccountHome.class);
                intent.putExtra("course", classToEnter);
                startActivity(intent);
            }
        });

        MyClassList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String classToEdit = (String) parent.getItemAtPosition(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MyClassList.this);
                builder.setTitle(classToEdit);
                CharSequence[] menuOptions=new CharSequence[]{"Remove Class"};
                builder.setItems(menuOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int itemNum) {
                        if(itemNum==0){
                            String UserID=mAuth.getCurrentUser().getUid();
                            myClassArrayList.remove(classToEdit);
                            databaseRef.child(UserID).child("EnrolledClasses").setValue(myClassArrayList);
                            displayList();
                        }
                    }
                });

                builder.show();
                return true;
            }
        });
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
                myArrayAdapter.getFilter().filter(newText);
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
                    Intent intent = new Intent(MyClassList.this, MainActivity.class);
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