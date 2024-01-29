package com.cloudpos.whilelistofsim;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.cloudpos.utils.AidlController;
import com.cloudpos.utils.IAIDLListener;
import com.wizarpos.wizarviewagentassistant.aidl.ISystemExtApi;

import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener, IAIDLListener {
    private TextView message;
    private Handler handler;
    private HandleCallBack callBack;
    private TextView mOpen1, mOpen2, mClose, mRead;
    private ISystemExtApi systemExtApi;
    private ServiceConnection scanConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initParams();
    }

    private void initParams() {
        AidlController aidlController = new AidlController();
        aidlController.startAgentService(this, this);
    }

    @SuppressLint("MissingPermission")
    public void getSimInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        SubscriptionManager manager = SubscriptionManager.from(this);
        List<SubscriptionInfo> list = manager.getActiveSubscriptionInfoList();
        for (SubscriptionInfo info : list) {
            int mcc = info.getMcc();
            int mnc = info.getMnc();
            int solt = info.getSimSlotIndex() + 1;
            String imsi = telephonyManager.getSubscriberId(info.getSubscriptionId());
            callBack.sendResponse("SIM" + solt + ":\n  MCC+MNC = " + mcc + "," + mnc + "\n  IMSI = " + imsi);
        }
    }

    private void initUI() {
        handler = new Handler(handleCallBack);
        callBack = new HandleCallbackImpl(this, handler);
        message = (TextView) findViewById(R.id.message);
        mOpen1 = (TextView) findViewById(R.id.open1);
        mOpen2 = (TextView) findViewById(R.id.open2);
        mClose = (TextView) findViewById(R.id.close);
        mRead = (TextView) findViewById(R.id.read);
        mClose.setOnClickListener(this);
        mOpen1.setOnClickListener(this);
        mOpen2.setOnClickListener(this);
        mRead.setOnClickListener(this);
        message.setMovementMethod(ScrollingMovementMethod.getInstance());
        message.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open1:
                setDataEnabled(1, true);
                break;
            case R.id.open2:
                setDataEnabled(2, true);
                break;
            case R.id.close:
                setDataEnabled(1, false);
                break;
            case R.id.read:
                getSimInfo();
                break;
        }
    }

    private void setDataEnabled(int solt, boolean enabled) {
        try {
            boolean result = systemExtApi.setMobileDataEnabled(solt, enabled);
            String message = "close mobile data " + (result ? "success!" : "failure!");
            if (enabled) {
                message = result ? "open sim" + solt + " mobile data success !" : "open sim" + solt + " mobile data failure!";
            }
            callBack.sendResponse(result ? HandleCallBack.SUCCESS_CODE : HandleCallbackImpl.ERROR_CODE, message);
        } catch (RemoteException e) {
            e.printStackTrace();
            callBack.sendResponse(HandleCallbackImpl.ERROR_CODE, "set sim" + solt + " mobile data failure!");
        }
    }

    private Handler.Callback handleCallBack = msg -> {
        switch (msg.what) {
            case HandleCallbackImpl.SUCCESS_CODE:
                setTextcolor(msg.obj.toString(), Color.BLUE);
                break;
            case HandleCallbackImpl.ERROR_CODE:
                setTextcolor(msg.obj.toString(), Color.RED);
                break;
            default:
                setTextcolor(msg.obj.toString(), Color.BLACK);
                break;
        }
        return false;
    };

    private void setTextcolor(String msg, int color) {
        Spannable span = Spannable.Factory.getInstance().newSpannable(msg);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
        span.setSpan(colorSpan, 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.append(span);
        moveScroller(message);
    }

    private static void moveScroller(TextView text) {
        final int scrollAmount = text.getLayout().getLineTop(text.getLineCount()) - text.getHeight();
        int y = scrollAmount > 0 ? scrollAmount + 30 : 0;
        int x = 0;
        text.scrollTo(x, y);
    }

    @Override
    public void serviceConnected(Object objService, ServiceConnection connection) {
        if (objService instanceof ISystemExtApi) {
            systemExtApi = (ISystemExtApi) objService;
            scanConn = connection;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanConn != null) {
            unbindService(scanConn);
            systemExtApi = null;
        }
    }
}
