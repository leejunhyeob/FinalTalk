package com.example.finaltalk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {// 푸시알람이 도착했다고 알려줌
        Log.d("messagingservice","onmessagereceived");
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();//보내는사람 Uid

        Log.d("sendFcm","내 Uid:"+userUid);



        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title").toString();
            String text = remoteMessage.getData().get("text").toString();

            Log.d("onMessageReceived","title:"+title);
            Log.d("onMessageReceived","text:"+text);

            sendNotification(title,text);
        }
    }

    private void sendNotification(String title,String text) { //푸시알람 만드는애

        Log.d("messagingservice","sendnotification");
        Log.d("messagingservice","title:"+title);
        Log.d("messagingservice","text:"+text);

        Intent intent = new Intent(this, MainActitivy.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//호출하는 액티비티가 스택에 존재할 경우에, 해당 액티비티를 최상위로 올리면서, 그 위에 존재하던 액티비티들은 모두 삭제를 하는 플래그


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);// 발생하는 이벤트를 사용자에게 알리는 클래스.
        //PendingIntent pendingIntent = PendingIntent.getActivity(MainActitivy.this, 0 ,intent , PendingIntent.FLAG_ONE_SHOT);   //pendingintent -> A한테 이 B인텐트를 C시점에 실행하라고 해라. 지금은 말고  //FLAG_ONE_SHOT -> 이걸로 생성한 pendingintent





        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channelid = new NotificationChannel("channelid","channelid",NotificationManager.IMPORTANCE_HIGH); //IMPORTANCE_DEFAULT ->기본 알림 중요도 : 모든 곳에서 보여주며, 소음이 발생하지만 시각적으로 방해 안받음
            notificationManager.createNotificationChannel(channelid);

            builder = new NotificationCompat.Builder(this, channelid.getId());
            builder.setAutoCancel(true)
                    .setSmallIcon(R.drawable.bell)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setFullScreenIntent(pendingIntent,true)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setTimeoutAfter(500);


            notificationManager.notify(0 , builder.build());//id: 정의해야하는 각 알림의 고유한 int값? notification의 고유 아이디?
        }else{
            builder = new NotificationCompat.Builder(this);
        }

        Log.d("finaltalkmyfirebase","style");

        notificationManager.notify(0, builder.build());

    }
}