package com.alakamandawalk.popcorn;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

public class BlurTransformation implements Transformation {

    private Context context;

    public BlurTransformation(Context context) {
        this.context = context;
    }

    @Override
    public Bitmap transform(Bitmap source) {

        Bitmap result = blurImage(source);
        source.recycle();

        return result;
    }

    private Bitmap blurImage(Bitmap source) {

        Bitmap outBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation allIn = Allocation.createFromBitmap(rs, source);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);
        allOut.copyTo(outBitmap);

        rs.destroy();
        allIn.destroy();
        return outBitmap;
    }

    @Override
    public String key() {
        return "BlurTransformation";
    }
}
