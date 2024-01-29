package com.cloudpos.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.wizarpos.wizarviewagentassistant.aidl.ISystemExtApi;

public class AidlController {


    public static final String TAG = "AidlController";
    private IAIDLListener aidlListener ;
    private ServiceConnection connection;

    private final String DESC_AGENT_SERVICE ="com.wizarpos.wizarviewagentassistant.aidl.ISystemExtApi";

    protected class ServiceConnectionImpl implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                String temp = service.getInterfaceDescriptor();
                Log.d(TAG, "onServiceConnected : " + temp);
                Object objService = null;
                if(temp.equals(DESC_AGENT_SERVICE)) {
                    objService = ISystemExtApi.Stub.asInterface(service);
                }else {
                    Log.e(TAG, "The corresponding AIDL interface cannot be recognized");
                }
                if(objService != null){
                    aidlListener.serviceConnected(objService, connection);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connection = null;
        }

    }

    protected void setListener(IAIDLListener aidlListener ){
        this.aidlListener = aidlListener;
    }

    protected boolean startConnectService(Context host, ComponentName comp, IAIDLListener aidlListener) {
        setListener(aidlListener);
        Intent intent = new Intent();
        intent.setComponent(comp);
        connection = new ServiceConnectionImpl();
        boolean isSuccess = host.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        return isSuccess;
    }

    public boolean startAgentService(Context host, IAIDLListener aidlListener) {
        ComponentName comp = new ComponentName(
                "com.wizarpos.wizarviewagentassistant",
                "com.wizarpos.wizarviewagentassistant.SystemExtApiService");
        boolean isSuccess = startConnectService(host,comp,aidlListener);
        Log.d("DEBUG", "bind wizarviewagentassistant service");
        return isSuccess;
    }
}
