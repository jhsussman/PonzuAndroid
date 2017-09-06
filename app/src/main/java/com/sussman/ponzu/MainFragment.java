package com.sussman.ponzu;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private static final int CAM_REQUEST=1;
    private static final int REQUEST_VIDEO_CAPTURE=2;
    private StorageReference imageUpload=null;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseRef=null;
    private File file=null;
    private Uri imageFile=null;
    private String course=null;
    private String UserID=null;
    private EditText questionText=null;
    private String theTextOfTheQuestion=null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());



        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAM_REQUEST && resultCode == RESULT_OK) {
            storage= FirebaseStorage.getInstance();
            imageUpload = storage.getReferenceFromUrl("gs://ponzu-5e308.appspot.com/");
            imageFile=data.getData();
            UserID=mAuth.getCurrentUser().getUid();

            final StorageReference picRef=imageUpload.child(course).child("QuestionPictures").child(randomFileName()+".jpg");
            UploadTask uploadTask = picRef.putFile(imageFile);

            final ProgressDialog progress = ProgressDialog.show(getActivity(), "Uploading Photo",
                    "Please Wait", true);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    progress.dismiss();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (theTextOfTheQuestion==null || theTextOfTheQuestion.equals("")){
                        theTextOfTheQuestion="(Only Picture)";
                    }
                    //databaseRef.child(UserID).child("QuestionPictures").setValue(picRef.getDownloadUrl().toString());
                    StorageMetadata questionMetadata = new StorageMetadata.Builder()
                            .setContentType("image/jpg")
                            .setCustomMetadata("QuestionText", theTextOfTheQuestion)
                            .build();
                    if(theTextOfTheQuestion!=null){
                        picRef.updateMetadata(questionMetadata);
                    }
                    progress.dismiss();
                    Toast.makeText(getActivity(), "Image Submitted", Toast.LENGTH_LONG).show();

                }
            });
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            storage= FirebaseStorage.getInstance();
            imageUpload = storage.getReferenceFromUrl("gs://ponzu-5e308.appspot.com/");
            imageFile=data.getData();
            UserID=mAuth.getCurrentUser().getUid();

            final StorageReference vidRef=imageUpload.child(course).child("QuestionVideos").child(randomFileName()+".mp4");
            UploadTask uploadTask = vidRef.putFile(imageFile);

            final ProgressDialog progress = ProgressDialog.show(getActivity(), "Uploading Video",
                    "Please Wait", true);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    progress.dismiss();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (theTextOfTheQuestion==null || theTextOfTheQuestion.equals("")){
                        theTextOfTheQuestion="(Only Video)";
                    }
                    //databaseRef.child(UserID).child("QuestionPictures").setValue(picRef.getDownloadUrl().toString());
                    StorageMetadata questionMetadata = new StorageMetadata.Builder()
                            .setContentType("video/mp4")
                            .setCustomMetadata("QuestionText", theTextOfTheQuestion)
                            .build();
                    if(theTextOfTheQuestion!=null){
                        vidRef.updateMetadata(questionMetadata);
                    }
                    progress.dismiss();
                    Toast.makeText(getActivity(), "Video Submitted", Toast.LENGTH_LONG).show();

                }
            });
        }


    }

    public void openCamera() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAM_REQUEST);
        }
    }

    private void openVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public String randomFileName(){
        UUID uid = UUID.randomUUID();
        String fileName=uid.toString();
        return fileName;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Bundle bundle=getArguments();
        String message=Integer.toString(bundle.getInt("count"));
        int screenNumber=bundle.getInt("count");

        Intent thisIntent=getActivity().getIntent();
        course=thisIntent.getStringExtra("course");
        TextView welcome=(TextView)view.findViewById(R.id.welcomeTo);
        welcome.setText("Welcome to "+course);
        questionText=(EditText)view.findViewById(R.id.questionText);
        theTextOfTheQuestion=questionText.getText().toString();

        Button takePicture = (Button)view.findViewById(R.id.takePicture);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {
                try {
                    openCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button takeVideo = (Button)view.findViewById(R.id.takeVideo);
        takeVideo.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {
                    openVideo();
            }
        });


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
