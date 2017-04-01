package com.kslau.nexus.samplebubbleheadservice;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by shen-mini-itx on 4/1/2017.
 */

public class BubbleHeadService extends Service {


    private static final String TAG = "BubbleHeadService";

    private WindowManager windowManager;
    private RelativeLayout bubbleHeadView, removeView;
    private ImageView bubbleHeadImg, removeImg;
    private LinearLayout bubbleLayout;
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
    private Point szWindow = new Point();
    private boolean isLeft = true, isExpandingLayout = false;
    private FrameLayout frameLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        handleStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bubbleHeadView != null)
            windowManager.removeView(bubbleHeadView);

        if (bubbleLayout != null)
            windowManager.removeView(bubbleLayout);

        if (removeView != null)
            windowManager.removeView(removeView);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d(TAG, "ChatHeadService.onBind()");
        return null;
    }

    private void handleStart() {

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(szWindow);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        //start remove view
        removeView = (RelativeLayout) layoutInflater.inflate(R.layout.layout_bubble_remove, null);
        WindowManager.LayoutParams paramRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        paramRemove.gravity = Gravity.TOP | Gravity.START;
        removeView.setVisibility(View.GONE);
        removeImg = (ImageView) removeView.findViewById(R.id.remove_img);
        windowManager.addView(removeView, paramRemove);

        //chat head view
        bubbleHeadView = (RelativeLayout) layoutInflater.inflate(R.layout.layout_bubble_head, null);
        bubbleHeadImg = (ImageView) bubbleHeadView.findViewById(R.id.bubble_head);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = getStatusBarHeight();
        windowManager.addView(bubbleHeadView, params);

        bubbleHeadView.setOnTouchListener(new View.OnTouchListener() {
            long time_start = 0, time_end = 0;
            boolean isLongClick = false, inBounded = false;
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();
            Runnable runnable_longClick = new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "Into runnable_longClick");

                    isLongClick = true;
                    removeView.setVisibility(View.VISIBLE);
                    bubbleLongClick();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) bubbleHeadView.getLayoutParams();

                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();
                int x_cord_Destination, y_cord_Destination;

                Log.d(TAG, "event x and y:" + x_cord + " , " + y_cord);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        time_start = System.currentTimeMillis();
                        handler_longClick.postDelayed(runnable_longClick, 600);

                        remove_img_width = removeImg.getLayoutParams().width;
                        remove_img_height = removeImg.getLayoutParams().height;

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;

                        if (bubbleLayout != null) {
                            bubbleLayout.setVisibility(View.GONE);
                        }
                        Log.d(TAG, "ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;

                        if (isLongClick) {
                            int x_bound_left = szWindow.x / 2 - (int) (remove_img_width * 0.5);
                            int x_bound_right = szWindow.x / 2 + (int) (remove_img_width * 0.5);
                            int y_bound_top = szWindow.y - (int) (remove_img_height * 0.5);

                            if ((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top) {
                                inBounded = true;

                                int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                                int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight() + 20));

                                if (removeImg.getLayoutParams().height == remove_img_height) {
                                    removeImg.getLayoutParams().height = (int) (remove_img_height * 1.5);
                                    removeImg.getLayoutParams().width = (int) (remove_img_width * 1.5);

                                    WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
                                    param_remove.x = x_cord_remove;
                                    param_remove.y = y_cord_remove;

                                    windowManager.updateViewLayout(removeView, param_remove);
                                }

                                layoutParams.x = x_cord_remove + (Math.abs(removeView.getWidth() - bubbleHeadView.getWidth())) / 2;
                                layoutParams.y = y_cord_remove + (Math.abs(removeView.getHeight() - bubbleHeadView.getHeight())) / 2;

                                windowManager.updateViewLayout(bubbleHeadView, layoutParams);
                                break;
                            } else {
                                inBounded = false;
                                removeImg.getLayoutParams().height = remove_img_height;
                                removeImg.getLayoutParams().width = remove_img_width;

                                WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
                                int x_cord_remove = (szWindow.x - removeView.getWidth()) / 2;
                                int y_cord_remove = szWindow.y - (removeView.getHeight() + getStatusBarHeight());

                                param_remove.x = x_cord_remove;
                                param_remove.y = y_cord_remove;

                                windowManager.updateViewLayout(removeView, param_remove);
                            }

                        }


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        windowManager.updateViewLayout(bubbleHeadView, layoutParams);
                        Log.d(TAG, "ACTION_MOVE");
                        break;
                    case MotionEvent.ACTION_UP:
                        isLongClick = false;
                        removeView.setVisibility(View.GONE);
                        removeImg.getLayoutParams().height = remove_img_height;
                        removeImg.getLayoutParams().width = remove_img_width;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        if (inBounded) {
                            stopService(new Intent(BubbleHeadService.this, BubbleHeadService.class));
                            inBounded = false;
                            break;
                        }

                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        if (Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5) {
                            time_end = System.currentTimeMillis();
                            if ((time_end - time_start) < 300) {
                                bubbleClick();
                                //Log.d(TAG, "time less than 300, bubble click");
                            }
                        } else {
                            isExpandingLayout = false;
                            //Log.d(TAG, "");
                        }

                        y_cord_Destination = y_init_margin + y_diff;

                        int BarHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (bubbleHeadView.getHeight() + BarHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (bubbleHeadView.getHeight() + BarHeight);
                        }
                        layoutParams.y = y_cord_Destination;


                        inBounded = false;
                        if (!isExpandingLayout) {
                            resetPosition(x_cord, y_cord);
                        }

                        Log.d(TAG, "ACTION_UP");
                        break;
                    default:
                        Log.d(TAG, "chatHeadView.setOnTouchListener  -> event.getAction() : default");
                        break;
                }
                return true;
            }
        });

        bubbleLayout = (LinearLayout) layoutInflater.inflate(R.layout.layout_bubble_block, null);

        WindowManager.LayoutParams paramsTxt = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        paramsTxt.gravity = Gravity.TOP | Gravity.LEFT;

        bubbleLayout.setVisibility(View.GONE);
        windowManager.addView(bubbleLayout, paramsTxt);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        windowManager.getDefaultDisplay().getSize(szWindow);

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) bubbleHeadView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "ChatHeadService.onConfigurationChanged -> landscap");

            if (bubbleLayout != null) {
                bubbleLayout.setVisibility(View.GONE);
            }

            if (layoutParams.y + (bubbleHeadView.getHeight() + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (bubbleHeadView.getHeight() + getStatusBarHeight());
                windowManager.updateViewLayout(bubbleHeadView, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x, 0);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "ChatHeadService.onConfigurationChanged -> portrait");

            if (bubbleLayout != null) {
                bubbleLayout.setVisibility(View.GONE);
            }

            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x, 0);
            }

        }
    }

    private void resetPosition(int x_cord_now, int y_cord_now) {
        Log.d(TAG, "resetPosition");
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now, y_cord_now);

        } else {
            isLeft = false;
            moveToRight(x_cord_now, y_cord_now);

        }

    }

    private void moveToLeft(final int x_cord_now, final int y_cord_now) {
        final int x = szWindow.x - x_cord_now;

        new CountDownTimer(300, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) bubbleHeadView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 3;
                mParams.x = 0 - (int) (double) bounceValue(step, x);
                mParams.y = y_cord_now - bubbleHeadView.getHeight();
                windowManager.updateViewLayout(bubbleHeadView, mParams);
            }

            public void onFinish() {
                mParams.x = 0;
                windowManager.updateViewLayout(bubbleHeadView, mParams);
            }
        }.start();
    }

    private void moveToRight(final int x_cord_now, final int y_cord_now) {
        new CountDownTimer(300, 5) {
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) bubbleHeadView.getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 3;
                mParams.x = szWindow.x + (int) (double) bounceValue(step, x_cord_now) - bubbleHeadView.getWidth();
                mParams.y = y_cord_now - bubbleHeadView.getHeight() + 20;
                windowManager.updateViewLayout(bubbleHeadView, mParams);
            }

            public void onFinish() {
                mParams.x = szWindow.x - bubbleHeadView.getWidth();
                windowManager.updateViewLayout(bubbleHeadView, mParams);
            }
        }.start();
    }

    private double bounceValue(long step, long scale) {
        double value = scale * java.lang.Math.exp(-0.055 * step) * java.lang.Math.cos(0.08 * step);
        return value;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }

        //Log.d(TAG, "getStatusBarHeight: " + result);
        return result;
    }

    private int getBottomVirtualButton() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private void bubbleLongClick() {
        Log.d(TAG, "Into ChatHeadService.bubbleLongClick() ");

        WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
        int x_cord_remove = (szWindow.x - removeView.getWidth()) / 2;
        int y_cord_remove = szWindow.y - (removeView.getHeight() + getStatusBarHeight());

        param_remove.x = x_cord_remove;
        param_remove.y = y_cord_remove;

        windowManager.updateViewLayout(removeView, param_remove);
    }

    private void bubbleClick() {

        if (bubbleLayout != null && bubbleHeadView != null) {

            if (!isExpandingLayout) {
                Log.d(TAG, "max Layout");

                WindowManager.LayoutParams paramChatHead = (WindowManager.LayoutParams) bubbleHeadView.getLayoutParams();
                WindowManager.LayoutParams paramLayout = (WindowManager.LayoutParams) bubbleLayout.getLayoutParams();

                bubbleLayout.getLayoutParams().height = szWindow.y - bubbleHeadView.getHeight() - getBottomVirtualButton() - 20;
                bubbleLayout.getLayoutParams().width = szWindow.x - (20 * 2);

                isExpandingLayout = true;
                if (isLeft) {
                    paramChatHead.x = 20;
                    paramChatHead.y = 20;

                    paramLayout.x = paramChatHead.x;
                    paramLayout.y = paramChatHead.y + bubbleHeadImg.getHeight() + 20;

                    bubbleLayout.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);


                } else {
                    paramChatHead.x = szWindow.x - bubbleHeadView.getWidth() - 20;
                    paramChatHead.y = 20;

                    paramLayout.x = 20;
                    paramLayout.y = paramChatHead.y + bubbleHeadImg.getHeight() + 20;

                    bubbleLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

                }

                bubbleLayout.setVisibility(View.VISIBLE);
                windowManager.updateViewLayout(bubbleLayout, paramLayout);
                windowManager.updateViewLayout(bubbleHeadView, paramChatHead);
            } else {
                Log.d(TAG, "min Layout");
                bubbleLayout.setVisibility(View.GONE);
                isExpandingLayout = false;

            }


        }
    }
}
