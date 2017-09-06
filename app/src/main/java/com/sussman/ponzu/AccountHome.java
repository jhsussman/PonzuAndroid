package com.sussman.ponzu;


import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.*;

import android.view.*;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


public class AccountHome extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener,
        SecondFragment.OnFragmentInteractionListener, ThirdFragment.OnFragmentInteractionListener{
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private ViewPager viewPager=null;
    private SwipeAdapter swipeAdapter=null;


    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_home);
        final ActionBar actionBar=getActionBar();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager=(ViewPager)findViewById(R.id.viewPager);
        swipeAdapter = new SwipeAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(swipeAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("Tab 1");
        tabLayout.getTabAt(1).setText("Tab 2");
        tabLayout.getTabAt(2).setText("Tab 3");


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

       // databaseRef=database.getReferenceFromUrl("https://ponzu-5e308.firebaseio.com/").child("Users");

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutMenu:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AccountHome.this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.settingsMenu:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}