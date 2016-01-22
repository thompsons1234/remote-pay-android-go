package com.clover.remote.client.lib.example;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.clover.common2.Signature2;
import com.clover.common2.Signature2.Point;

/**
 * Created by blakewilliams on 1/11/16.
 */
public class SignatureView extends View {

    Signature2 signature;

    public SignatureView(Context ctx, AttributeSet attrSet) {
        super(ctx, attrSet);
    }

    public SignatureView(Context ctx) {
        super(ctx);
    }

    public void onDraw(Canvas canvas) {
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(1.5f);
        if(signature != null && signature.strokes != null) {
            for(Signature2.Stroke stroke : signature.strokes) {
                for(int i=0; i<stroke.points.size()-1; i++) {
                    Point pt1 = stroke.points.get(i);
                    Point pt2 = stroke.points.get(i+1);
                    canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, linePaint);
                }
            }
        }
    }

    public Signature2 getSignature() {
        return signature;
    }

    public void setSignature(Signature2 signature) {
        this.signature = signature;
    }
}
