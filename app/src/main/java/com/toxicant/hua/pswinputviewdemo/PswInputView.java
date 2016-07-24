package com.toxicant.hua.pswinputviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

/**
 * 仿照支付宝的密码输入控件
 * Created by hua on 2016/7/24.
 */
public class PswInputView extends View {
    private InputMethodManager input;//输入法管理
    private ArrayList<Integer> result;//输入结果保存
    private int count;//密码位数
    private int size;//默认每一格的大小
    private Paint mBorderPaint;//边界画笔
    private Paint mDotPaint;//掩盖点的画笔
    private int mBorderColor;//边界颜色
    private int mDotColor;//掩盖点的颜色
    private RectF mRoundRect;//外面的圆角矩形
    private int mRoundRadius;//圆角矩形的圆角程度
    public PswInputView(Context context) {
        super(context);
        init();
    }
    private InputCallBack inputCallBack;//输入完成的回调


    public interface InputCallBack{
        void onInputFinish(String result);
    }

    public PswInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PswInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化相关参数
     */
    void init(){
        final float dp=getResources().getDisplayMetrics().density;
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        input=(InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        result=new ArrayList<>();
        count=6;//默认6位密码
        size=(int)(dp*30);//默认30dp一格
        //color
        mBorderColor=Color.LTGRAY;
        mDotColor=Color.GRAY;
        mBorderPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStrokeWidth(2);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mBorderColor);
        mDotPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mDotPaint.setStrokeWidth(2);
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setColor(mDotColor);
        mRoundRect=new RectF();
        mRoundRadius=(int)(5*dp);
        this.setOnKeyListener(new MyKeyListener());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w=measureWidth(widthMeasureSpec);
        int h=measureHeight(heightMeasureSpec);
        int wsize=MeasureSpec.getSize(widthMeasureSpec);
        int hsize=MeasureSpec.getSize(heightMeasureSpec);
        //宽度没指定,但高度指定
        if (w==-1){
            if (h!=-1){
                w=h*count;//宽度=高*数量
                size=h;
            }else {//两个都不知道,默认宽高
                w=size*6;
                h=size;
            }
        }else {//宽度已知
            if (h==-1){//高度不知道
                h=w/6;
                size=h;
            }
        }
        setMeasuredDimension(Math.min(w,wsize),Math.min(h,hsize));
    }
    private int measureWidth(int widthMeasureSpec){
        //宽度
        int wmode=MeasureSpec.getMode(widthMeasureSpec);
        int wsize=MeasureSpec.getSize(widthMeasureSpec);
        if (wmode==MeasureSpec.AT_MOST){//wrap_content
            return -1;
        }
        return wsize;
    }
    private int measureHeight(int heightMeasureSpec){
        //高度
        int hmode=MeasureSpec.getMode(heightMeasureSpec);
        int hsize=MeasureSpec.getSize(heightMeasureSpec);
        if (hmode==MeasureSpec.AT_MOST){//wrap_content
            return -1;
        }
        return hsize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_DOWN){//点击控件弹出输入键盘
            requestFocus();
            input.showSoftInput(this,InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus){
            input.showSoftInput(this,InputMethodManager.SHOW_FORCED);
        }else {
            input.hideSoftInputFromInputMethod(this.getWindowToken(),0);
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus){
            input.hideSoftInputFromWindow(this.getWindowToken(),0);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int width=getWidth()-2;
        final int height=getHeight()-2;
        //先画个圆角矩形
        mRoundRect.set(0,0,width,height);
        canvas.drawRoundRect(mRoundRect,mRoundRadius,mRoundRadius,mBorderPaint);
        //画分割线
        for (int i=1;i<count;i++){
            final int x=i*size;
            canvas.drawLine(x,0,x,height,mBorderPaint);
        }
        //画掩盖点,
        // 这是前面定义的变量 private ArrayList<Integer> result;//输入结果保存
        int dotRadius=size/6;//圆圈占格子的三分之一
        for (int i=0;i<result.size();i++){
            final float x= (float) (size*(i+0.5));
            final float y=size/2;
            canvas.drawCircle(x,y,dotRadius,mDotPaint);
        }
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType= InputType.TYPE_CLASS_NUMBER;//输入类型为数字
        outAttrs.imeOptions=EditorInfo.IME_ACTION_DONE;
        return new MyInputConnection(this,false);
    }

    public void setInputCallBack(InputCallBack inputCallBack) {
        this.inputCallBack = inputCallBack;
    }
    public void clearResult(){
        result.clear();
        invalidate();
    }
    class MyInputConnection extends BaseInputConnection {
        public MyInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
           //这里是接受输入法的文本的，我们只处理数字，所以什么操作都不做
            return true;
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            //软键盘的删除键 DEL 无法直接监听，自己发送del事件
            if (beforeLength==1 && afterLength==0){
                return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }
    /**
     * 按键监听器
     */
    class MyKeyListener implements OnKeyListener{
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction()==KeyEvent.ACTION_DOWN){
                if (keyCode>= KeyEvent.KEYCODE_0 && keyCode<= KeyEvent.KEYCODE_9){//只处理数字
                    if (result.size()<count){
                        result.add(keyCode-7);
                        invalidate();
                        ensureFinishInput();
                    }
                    return true;
                }
                if (keyCode==KeyEvent.KEYCODE_DEL){
                    if (!result.isEmpty()){//不为空，删除最后一个
                        result.remove(result.size()-1);
                        invalidate();
                    }
                    return true;
                }
                if (keyCode==KeyEvent.KEYCODE_ENTER){
                    ensureFinishInput();
                    return true;
                }
            }
            return false;
        }//onKey

        /**
         * 判断是否输入完成，输入完成后调用callback
         */
        void ensureFinishInput(){
            if (result.size()==count && inputCallBack!=null){//输入完成
                StringBuffer sb=new StringBuffer();
                for (int i:result){
                    sb.append(i);
                }
                inputCallBack.onInputFinish(sb.toString());
            }
        }
    }//in class
}
