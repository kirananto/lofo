/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.razor.lofo;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends BaseActivity implements
        View.OnClickListener,NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "ProfileActivity";
    private ViewGroup mProfileUi;
    private FirebaseAuth mAuth;
    private CircleImageView mProfilePhoto;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView mProfileUsername;
    public GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN = 103;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FacebookSdk.sdkInitialize(getApplicationContext());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .build()
                ).build();

        // Initialize authentication and set up callbacks
        mAuth = FirebaseAuth.getInstance();

        // GoogleApiClient with Sign In

        mProfileUi = (ViewGroup) findViewById(R.id.profile);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mProfilePhoto = (CircleImageView) findViewById(R.id.profile_user_photo);
        mProfilePhoto.setImageResource(R.drawable.anonymous);
        mProfileUsername = (TextView) findViewById(R.id.profile_user_name);
        findViewById(R.id.show_feeds_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    showSignedInUI(user);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    showSignedInUI(user);
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.sign_out_button:
                signout();
                break;
            case R.id.show_feeds_button:
                Intent feedsIntent = new Intent(this, FeedsActivity.class);
                startActivity(feedsIntent);
                break;
        }
    }

    public void signout()
    {

        mAuth.signOut();
        LoginManager.getInstance().logOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(this, WelcomeActivity.class));
    }

    public void showSignedInUI(FirebaseUser firebaseUser) {
        Log.d(TAG, "Showing signed in UI");
        mProfileUi.setVisibility(View.VISIBLE);
        mProfileUsername.setVisibility(View.VISIBLE);
        mProfilePhoto.setVisibility(View.VISIBLE);
        if (firebaseUser.getDisplayName() != null) {
            mProfileUsername.setText(firebaseUser.getDisplayName());
        }

        if (firebaseUser.getPhotoUrl() != null) {
            GlideUtil.loadProfileIcon(firebaseUser.getPhotoUrl().toString(), mProfilePhoto);
        }
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("displayName", firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Anonymous");
        updateValues.put("photoUrl", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);

        FirebaseUtil.getPeopleRef().child(firebaseUser.getUid()).updateChildren(
                updateValues,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                        if (firebaseError != null) {
                            Toast.makeText(ProfileActivity.this,
                                    "Couldn't save user data: " + firebaseError.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

     public void showSignedOutUI() {
        Log.d(TAG, "Showing signed out UI");
         //startActivity(new Intent(this, WelcomeActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
            dismissProgressDialog();
            showSignedInUI(currentUser);
        } else {
            showSignedOutUI();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_other) {
            Global.Category = "Other";
        } if (id == R.id.nav_electronics) {
            Global.Category = "Electronics";
        }if (id == R.id.nav_ornaments) {
            Global.Category = "Ornaments";
        }if (id == R.id.nav_goods) {
            Global.Category = "Goods";
        }if (id == R.id.nav_vehicles) {
            Global.Category = "Vehicles";
        }if (id == R.id.nav_people) {
            Global.Category = "People";
        } else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "SHARE");
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (id == R.id.nav_send) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "SHARE");
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (id == R.id.nav_mail) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","KIRAN ANTO", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "REGARDING CONTENT OF CSE BETA App");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
