package org.mupdfdemo2.activity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

//see https://github.com/Walkud/Android-Up/blob/master/mystudy/WalkudApp/src/main/java/com/walkud/self/module/survive/service/RestartService.java
//see http://blog.csdn.net/xyang81/article/details/13004299
public class MuPDFRestartService extends Service {
	private final static boolean D = false;
	private final static String TAG = "MuPDFRestartService";
	
	public final static String EXTRA_PID =  "EXTRA_PID";
	
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int pid = intent.getIntExtra(EXTRA_PID, 0);
            if (D) {
            	Log.e(TAG, "onStartCommand KillProcess:" + pid);
            }
            if (pid != 0) {
                android.os.Process.killProcess(pid);
            } else {
            	android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }
}
