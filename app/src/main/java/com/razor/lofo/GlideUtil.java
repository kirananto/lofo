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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.MenuItem;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

class GlideUtil {
    public static void loadImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.blue_grey_500));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .crossFade()
                .centerCrop()
                .into(imageView);
    }

    public static void loadProfileIcon(String url, ImageView imageView) {
        Context context = imageView.getContext();
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_person_outline_black_24dp)
                .dontAnimate()
                .fitCenter()
                .into(imageView);
    }
    public static void loadProfileIcon(final Context context, String url, final MenuItem menu) {
                Glide.with(context)
                             .load(url)
                             .asBitmap()
                             .placeholder(R.drawable.ic_person_outline_black_24dp)
                             .dontAnimate()
                             .fitCenter()
                             .into(new SimpleTarget<Bitmap>(100,100) {
                             @Override
                             public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                     RoundedBitmapDrawable circularBitmapDrawable =
                                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                     circularBitmapDrawable.setCircular(true);
                                     menu.setIcon(circularBitmapDrawable);
                                 }
                          });
            }
}