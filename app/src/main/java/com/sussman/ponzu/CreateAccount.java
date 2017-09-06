package com.sussman.ponzu;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.content.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CreateAccount extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userEmail=null;
    private String userPassword=null;
    private String firstName=null;
    private String lastName=null;
    private String phoneNumber=null;
    private String UserID=null;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseRef=null;
    private FirebaseStorage storage= FirebaseStorage.getInstance();
    private StorageReference storageRef=null;
    private static final int CAM_REQUEST=1;
    private static final int GALLERY_REQUEST=2;
    private File picFile=null;
    private Uri selectedImage=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };
         databaseRef=database.getReferenceFromUrl("https://ponzu-5e308.firebaseio.com/").child("Users");
         storageRef=storage.getReferenceFromUrl("gs://ponzu-5e308.appspot.com/");

        try {
            picFile=File.createTempFile("image","jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final EditText newEmail=(EditText)findViewById(R.id.newEmail);
        final EditText newPassword=(EditText)findViewById(R.id.newPass);

        Button takeProfilePicture = (Button)findViewById(R.id.takeProfilePicture);
        takeProfilePicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    openCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button findProfilePicture = (Button)findViewById(R.id.selectFromCameraRoll);
        findProfilePicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    openGallery();
                } catch (IOException e) {
                }

            }
        });


        Button submit = (Button)findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                 userEmail=newEmail.getText().toString().trim();
                 userPassword =newPassword.getText().toString();
                EditText pass1=(EditText)findViewById(R.id.newPass);
                EditText pass2=(EditText)findViewById(R.id.confirmPassword);
                EditText first=(EditText)findViewById(R.id.firstName);
                firstName=first.getText().toString().trim();
                EditText last=(EditText)findViewById(R.id.lastName);
                lastName=last.getText().toString().trim();
                EditText phone=(EditText)findViewById(R.id.phone);
                phoneNumber=phone.getText().toString().trim();
                EditText email=(EditText)findViewById(R.id.newEmail);

                if(pass1.getText().toString().trim().length()<1 ||
                        pass2.getText().toString().trim().length()<1 ||
                        first.getText().toString().trim().length()<1 ||
                        last.getText().toString().trim().length()<1 ||
                        phone.getText().toString().trim().length()<1 ||
                        email.getText().toString().trim().length()<1) {
                    Toast.makeText(CreateAccount.this, "Please Complete All Fields",
                            Toast.LENGTH_LONG).show();
                }else if(!userEmail.contains("@usc.edu")||!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                    Toast.makeText(CreateAccount.this, "Check Email Format",
                            Toast.LENGTH_LONG).show();
                }else if(pass1.getText().length()<6) {
                    Toast.makeText(CreateAccount.this, "Password not Long Enough",
                            Toast.LENGTH_LONG).show();
                }else if(!pass1.getText().toString().equals(pass2.getText().toString())){
                    Toast.makeText(CreateAccount.this, "Passwords do Not Match",
                            Toast.LENGTH_LONG).show();
                }else {
                    createAccount(userEmail, userPassword);

                }
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

    public void createAccount(String email, String password){
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(CreateAccount.this, "Error Occured",
                                    Toast.LENGTH_LONG).show();
                        }else {
                            mAuth.signInWithEmailAndPassword(userEmail, userPassword);
                            UserID=mAuth.getCurrentUser().getUid();
                            databaseRef.child(UserID).child("FirstName").setValue(firstName);
                            databaseRef.child(UserID).child("LastName").setValue(lastName);
                            databaseRef.child(UserID).child("Email").setValue(userEmail);
                            databaseRef.child(UserID).child("Phone").setValue(phoneNumber);
                            storageRef=storageRef.child("ProfilePictures").child(UserID+".jpg");

                            UploadTask uploadTask = storageRef.putFile(selectedImage);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(CreateAccount.this, "Something went wrong", Toast.LENGTH_LONG).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(CreateAccount.this, "Image Submitted", Toast.LENGTH_LONG).show();

                                }
                            });
                            mAuth.getCurrentUser().sendEmailVerification();
                            AlertDialog alertDialog = new AlertDialog.Builder(CreateAccount.this).create();
                            alertDialog.setTitle("Account Created");
                            alertDialog.setMessage("Please check your email to verify your account, " +
                                    "then log in. ");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(CreateAccount.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                            alertDialog.show();

                        }
                    }
                });
    }

    public void openCamera() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAM_REQUEST);
        }
    }

    public void openGallery() throws IOException {
        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK);
        choosePictureIntent.setType("image/*");
        choosePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));
        startActivityForResult(choosePictureIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAM_REQUEST && resultCode == RESULT_OK) {
            ImageView profileImage=(ImageView)findViewById(R.id.profilePic);
            selectedImage=data.getData();
            profileImage.setImageURI(selectedImage);

        }
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            ImageView profileImage=(ImageView)findViewById(R.id.profilePic);
            selectedImage=data.getData();
            profileImage.setImageURI(selectedImage);
        }
    }

}