package rabaigabor.nik.uni.obuda.hu.gimbalstabilizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TramNote on 2016.01.27..
 */
public class GraphView extends View {
    private final static int MAX_VALUES=65;
    private List<Float> values,values2,values3;
    private Path line,line2,line3;
    private Paint paint,paint2,paint3;
    public GraphView(Context context) {
        super(context);
        init();
    }
    //xml-ből hívás esetén attribútumokkal hívódik meg a konstruktor
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public GraphView(Context context, AttributeSet attrs, int defstyleAttr) {
        super(context, attrs, defstyleAttr);
        init();
    }
    private void init(){
        values=new ArrayList<Float>();
        for (int i=0;i<MAX_VALUES;i++){
            values.add(.0f);
        }
        this.line=new Path();
        this.paint=new Paint();
        this.paint.setColor(Color.RED); //pitch
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(3f);
        this.paint.setAntiAlias(true); //vonalakba nincs tördelés

        values2=new ArrayList<Float>();
        for (int i=0;i<MAX_VALUES;i++){
            values2.add(.0f);
        }
        this.line2=new Path();
        this.paint2=new Paint();
        this.paint2.setColor(Color.GREEN);  //roll
        this.paint2.setStyle(Paint.Style.STROKE);
        this.paint2.setStrokeWidth(3f);
        this.paint2.setAntiAlias(true); //vonalakba nincs tördelés

        values3=new ArrayList<Float>();
        for (int i=0;i<MAX_VALUES;i++){
            values3.add(.0f);
        }
        this.line3=new Path();
        this.paint3=new Paint();
        this.paint3.setColor(Color.BLUE);   //yaw
        this.paint3.setStyle(Paint.Style.STROKE);
        this.paint3.setStrokeWidth(3f);
        this.paint3.setAntiAlias(true); //vonalakba nincs tördelés
    }
    public void addValues(float pitch, float roll, float yaw){
        this.values.remove(0);
        this.values.add(pitch);

        this.values2.remove(0);
        this.values2.add(roll);

        this.values3.remove(0);
        this.values3.add(yaw);
        invalidate();
        //postInvalidate(); háttérszálból invalidál
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.getWidth(); //ez nem egyenlő view szélességével, ez nagyobb
        int width=getWidth();
        int height=getHeight();
        int width_step=width/MAX_VALUES;
        int height_offset=height>>1;//==height/2
        int height_step=height_offset/180;
        line.reset();
        line.moveTo(0,values.get(0)*height_step+height_offset);
        for (int i=1;i<MAX_VALUES;i++){
            line.lineTo(i*width_step,values.get(i)*height_step+height_offset);
        }

        line2.reset();
        line2.moveTo(0,values2.get(0)*height_step+height_offset);
        for (int i=1;i<MAX_VALUES;i++){
            line2.lineTo(i*width_step,values2.get(i)*height_step+height_offset);
        }

        line3.reset();
        line3.moveTo(0,values3.get(0)*height_step+height_offset);
        for (int i=1;i<MAX_VALUES;i++){
            line3.lineTo(i*width_step,values3.get(i)*height_step+height_offset);
        }
        canvas.drawPath(line,paint);
        canvas.drawPath(line2,paint2);
        canvas.drawPath(line3,paint3);
    }
}
