package com.sussman.ponzu;

import android.accounts.Account;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.*;
import android.view.*;
import com.google.firebase.auth.*;
import com.google.android.gms.tasks.*;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.*;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseRef=null;
    private String userEmail="jonhsussman@gmail.com";
    private String userPassword="password";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProgressBar waiter=(ProgressBar)findViewById(R.id.wait);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };

        final EditText email=(EditText)findViewById(R.id.email);
        final EditText password=(EditText)findViewById(R.id.password);

        Button login = (Button)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                 userEmail=email.getText().toString().trim();
                 userPassword =password.getText().toString();
                if(userEmail.length()<1 ||
                        userPassword.length()<1){
                    Toast.makeText(MainActivity.this, "Please enter Username and Password",
                            Toast.LENGTH_LONG).show();
                }else {
                    View b=findViewById(R.id.login);
                    b.setVisibility(View.INVISIBLE);
                    View c=findViewById(R.id.wait);
                    c.setVisibility(View.VISIBLE);
                    logIn(userEmail, userPassword);
                }
            }
        });

        Button create = (Button)findViewById(R.id.createAccount);
        create.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(intent);
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
    public void logIn(String email, String password){
        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Incorrect Username/Password",
                                    Toast.LENGTH_LONG).show();
                            View b=findViewById(R.id.login);
                            b.setVisibility(View.VISIBLE);
                            View c=findViewById(R.id.wait);
                            c.setVisibility(View.INVISIBLE);
                            logIn(userEmail, userPassword);
                        }
                        else {
                            String UserID=mAuth.getCurrentUser().getUid();
                            databaseRef=database.getReferenceFromUrl("https://ponzu-5e308.firebaseio.com/").child("Users");
                            databaseRef.child(UserID).child("EnrolledClasses").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        Intent intent2 = new Intent(MainActivity.this, MyClassList.class);
                                        startActivity(intent2);

                                    }else{
                                        Intent intent1 = new Intent(MainActivity.this, ClassList.class);
                                        startActivity(intent1);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
    }
        public void onBackPressed() {
        moveTaskToBack(true);
    }
}