package nesto.camera.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import nesto.camera.R;

/**
 * Created by Jack_long on 2015/7/7.
 * 对焦框
 */
public class CameraFocusView extends View {

    private Animation animation;

    private Canvas srcCanvas;
    private Bitmap srcBitmap;
    private final Paint paint;
    private int paintColor = Color.WHITE;
    private int paintWidth = 0;
    private int sizeWidth;
    private int sizeHeight;

    public CameraFocusView(Context context) {
        super(context);
        paint = new Paint();
        paint.setStrokeWidth(paintWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(paintColor);
    }

    public CameraFocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CameraFocusView, 0, 0);
        paintColor = a.getColor(R.styleable.CameraFocusView_bound_color, Color.WHITE);
        paintWidth = a.getDimensionPixelSize(R.styleable.CameraFocusView_bound_width, 0);
        a.recycle();

        paint = new Paint();
        paint.setStrokeWidth(paintWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(paintColor);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) return;
        sizeWidth = w;
        sizeHeight = h;

        srcBitmap = Bitmap.createBitmap(sizeWidth, sizeHeight, Bitmap.Config.ARGB_8888);
        srcCanvas = new Canvas(srcBitmap);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRect();
        canvas.drawBitmap(srcBitmap, 0, 0, null);
    }

    private void drawRect() {
        Rect rect = new Rect(5, 5, sizeWidth - 5, sizeHeight - 5);
        srcCanvas.drawColor(Color.TRANSPARENT);
        srcCanvas.drawRect(rect, paint);
        int length = (sizeHeight > sizeWidth ? sizeWidth : sizeHeight) / 10;
        srcCanvas.drawLine(sizeWidth / 2, 5, sizeWidth / 2, 5 + length, paint);
        srcCanvas.drawLine(sizeWidth / 2, sizeHeight - 5, sizeWidth / 2, sizeHeight - 5 - length, paint);
        srcCanvas.drawLine(5, sizeHeight / 2, 5 + length, sizeHeight / 2, paint);
        srcCanvas.drawLine(sizeWidth - 5, sizeHeight / 2, sizeWidth - 5 - length, sizeHeight / 2, paint);
    }

    public void startFocusing(float x, float y) {
        if (getParent() instanceof ViewGroup) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
            params.leftMargin = Math.round(x - getWidth() / 2);
            params.topMargin = Math.round(y - getWidth() / 2);
            params.rightMargin = Math.round(((ViewGroup) getParent()).getWidth() - getWidth() - params.leftMargin);
            params.bottomMargin = Math.round(((ViewGroup) getParent()).getHeight() - getWidth() - params.topMargin);
            setLayoutParams(params);
        } else {
            layout(Math.round(x - getWidth() / 2), Math.round(y - getWidth() / 2),
                    Math.round(x + getWidth() / 2), Math.round(y + getWidth() / 2));
        }
        setVisibility(VISIBLE);
        if (animation != null) {
            startAnimation(animation);
        }
    }

    public void stopFocusing() {
        clearAnimation();
        setVisibility(INVISIBLE);
    }

    public void setFocusAnimation(Animation animation) {
        this.animation = animation;
    }
}
