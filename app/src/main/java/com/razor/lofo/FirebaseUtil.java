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

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.razor.lofo.Models.Author;

class FirebaseUtil {
    public static DatabaseReference getBaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static Author getAuthor() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        return new Author(user.getDisplayName(), user.getPhotoUrl().toString(), user.getUid());
    }

    public static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("people").child(getCurrentUserId());
        }
        return null;
    }

    public static DatabaseReference getPostsRef() {
        return getBaseRef().child("posts");
    }

    public static Query getPostMissingRef() {
        Log.e("CATEGORY",Global.getCategory());
        if(Global.getCategory() != "General") {
            //TODO    Fix this.. here the first condition is not validated
            return getBaseRef().child("posts").orderByChild("lf").equalTo("Missing").getRef().orderByChild("categ").equalTo(Global.getCategory());
        } else
            return getBaseRef().child("posts").orderByChild("lf").equalTo("Missing");

    }
    public static Query getPostFoundRef() {
        if(Global.getCategory() != "General") {
            //TODO    Fix this.. here the first condition is not validated
            return getBaseRef().child("posts").orderByChild("lf").equalTo("Found").getRef().orderByChild("categ").equalTo(Global.getCategory());
        } else
            return getBaseRef().child("posts").orderByChild("lf").equalTo("Found");
    }

    public static String getPostsPath() {
        return "posts/";
    }


    public static String getPeoplePath() {
        return "people/";
    }

    public static DatabaseReference getPeopleRef() {
        return getBaseRef().child("people");
    }

    public static DatabaseReference getCommentsRef() {
        return getBaseRef().child("comments");
    }

    public static DatabaseReference getFeedRef() {
        return getBaseRef().child("feed");
    }

    public static DatabaseReference getLikesRef() {
        return getBaseRef().child("likes");
    }

    public static DatabaseReference getFollowersRef() {
        return getBaseRef().child("followers");
    }
}
