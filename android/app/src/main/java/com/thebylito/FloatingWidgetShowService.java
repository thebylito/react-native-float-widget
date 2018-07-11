package com.thebylito;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.graphics.PixelFormat;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.floatingwidget.MainActivity;
import com.floatingwidget.R;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Random;


public class FloatingWidgetShowService extends Service {


    WindowManager windowManager;
    View floatingView, collapsedView, expandedView;
    TextView widgetTitle, widgetBody;
    ImageView imageIcon;
    WindowManager.LayoutParams params;
    ReactContext reactContext = null;

    public FloatingWidgetShowService() {
    }

    private void openWidget() {
        collapsedView.setVisibility(View.GONE);
        expandedView.setVisibility(View.VISIBLE);
    }

    private void closeWidget() {
        collapsedView.setVisibility(View.VISIBLE);
        expandedView.setVisibility(View.GONE);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private void createButton(){


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "ACTION_OPEN_WIDGET": {
                    openWidget();
                    break;
                }
                case "ACTION_CLOSE_WIDGET": {
                    closeWidget();
                    break;
                }
                case "ACTION_SETCOLOR_WIDGET": {

                    int newColor = intent.getIntExtra("COLOR", Color.parseColor(String.valueOf("#ffffff")));
                    expandedView.setBackgroundColor(newColor);
                    break;
                }
                case "ACTION_SET_TITLE_WIDGET": {
                    String title = intent.getStringExtra("TITLE");
                    widgetTitle.setText(title);
                    break;
                }
                case "ACTION_SET_BODY_WIDGET": {
                    String body = intent.getStringExtra("BODY");
                    widgetBody.setText(body);
                    break;
                }
                case "ACTION_SET_BODY_IMAGE": {
                    String imgUrl = intent.getStringExtra("URL");
                    Picasso.get().load(imgUrl).into(imageIcon);
                    break;
                }
                case "ACTION_CREATE_BUTTON": {
                    createButton();
                    break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final ReactInstanceManager reactInstanceManager =
                getReactNativeHost().getReactInstanceManager();
        ReactContext getReactContext = reactInstanceManager.getCurrentReactContext();
        reactContext = getReactContext;

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                2038,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        windowManager.addView(floatingView, params);

        expandedView = floatingView.findViewById(R.id.Layout_Expended);

        collapsedView = floatingView.findViewById(R.id.Layout_Collapsed);
        imageIcon = (ImageView) floatingView.findViewById(R.id.WebsiteLogoIcon);
        widgetTitle = (TextView) floatingView.findViewById(R.id.widgetTitle);
        widgetBody = (TextView) floatingView.findViewById(R.id.widgetBody);

        floatingView.findViewById(R.id.Widget_Close_Icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });

        expandedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                WritableMap args = new Arguments().createMap();

                Random rand = new Random();
                int  n = rand.nextInt(50) + 1;
                args.putInt("time", n);
                System.out.println(n);
                sendEvent(reactContext, "eventoTeste", args);
                closeWidget();
            }
        });


        floatingView.findViewById(R.id.MainParentRelativeLayout).setOnTouchListener(new View.OnTouchListener() {
            int X_Axis, Y_Axis;
            float TouchX, TouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        X_Axis = params.x;
                        Y_Axis = params.y;
                        TouchX = event.getRawX();
                        TouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:

                        collapsedView.setVisibility(View.GONE);
                        expandedView.setVisibility(View.VISIBLE);
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        params.x = X_Axis + (int) (event.getRawX() - TouchX);
                        params.y = Y_Axis + (int) (event.getRawY() - TouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }

    protected ReactNativeHost getReactNativeHost() {
        return ((ReactApplication) getApplication()).getReactNativeHost();
    }

}
