package im.point.torgash.daspoint.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

import im.point.torgash.daspoint.R;

/*
    This class is for loading attributes. It helps programmatically change style, 
    which could be considered impossible at first glance.
     */
    public class StyleLoader {

        public StyleLoader() {

        }

        public static class StyleAttrs {
            public Drawable backGroundDrawable;
        }

        public StyleAttrs load(Context context, @StyleRes int styleResId) {
            final TypedArray styledAttributes = context.obtainStyledAttributes(styleResId, R.styleable.StyleableCommentsButton);
            return load(styledAttributes);
        }

        @NonNull
        private StyleAttrs load(TypedArray styledAttributes) {
            StyleAttrs styleAttrs = new StyleAttrs();
            try {
                styleAttrs.backGroundDrawable = styledAttributes.getDrawable(R.styleable.StyleableCommentsButton_CommentButtonBackgroundDrawable);

            } finally {
                styledAttributes.recycle();
            }
            return styleAttrs;
        }
    }