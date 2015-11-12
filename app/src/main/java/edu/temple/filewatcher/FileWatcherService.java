package edu.temple.filewatcher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kevin on 11/1/15.
 */
public class FileWatcherService extends Service {

    private int NOTIFICATION = 10342456;
    private final IBinder mBinder = new LocalBinder();

    private ArrayList<String> files;
    private ArrayList<String> updates;
    private Map<String,byte[]> check;
    private int checkTime;
    private NotificationManager mNM;
    private long lastCheck;

    @Override
    public IBinder onBind(Intent intent) {
        files = new ArrayList<>();
        check = new HashMap<>();
        updates = new ArrayList<>();
        checkTime = -1;
        lastCheck = 0;
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Thread thread = new Thread(){
            @Override
            public void run(){
                startChecker();
            }
        };

        thread.start();

        return mBinder;
    }

    public void addFile(String file,int checkTime){
        Log.d("Add File","Entered");
        if(!files.contains(file)) {
            compareFile(file);
            files.add(file);
        }
        if(this.checkTime != checkTime){
            this.checkTime = checkTime;
        }

    }

    private void startChecker(){

        while(true) {
            if (!files.isEmpty() && (System.currentTimeMillis()>lastCheck+(checkTime*1000))) {
                lastCheck = System.currentTimeMillis();
                for (String file : files) {
                    compareFile(file);
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else{
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void compareFile(String newUrl){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            URL url = new URL(newUrl);

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String response = "",tmpResponse;

            tmpResponse = reader.readLine();
            while(tmpResponse != null){
                response = response + tmpResponse;
                tmpResponse = reader.readLine();
            }

            md.update(response.getBytes(),0,response.length());
            byte[] temp = md.digest();

            if(check.containsKey(newUrl)){
                if(!Arrays.equals(check.get(newUrl), temp)){
                    check.put(newUrl, temp);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                    String currentDateandTime = sdf.format(new Date());
                    updates.add(newUrl +" "+ currentDateandTime);
                    showNotification(newUrl);
                }
            }
            else{
                check.put(newUrl,temp);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String file) {
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.putExtra(getString(R.string.update_tag),updates);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_more)  // the status icon
                .setTicker(file)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("File Watch")  // the label of the entry
                .setContentText("Files Changed")  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.cancel(NOTIFICATION);
        mNM.notify(NOTIFICATION, notification);
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        super.onDestroy();
    }


    public class LocalBinder extends Binder{
        FileWatcherService getService(){
            return FileWatcherService.this;
        }
    }

}
