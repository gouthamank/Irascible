package app.drool.irascible.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Date;
import java.util.LinkedList;

import app.drool.irascible.Constants.SERVICE;
import app.drool.irascible.R;
import app.drool.irascible.activities.ChatActivity;
import app.drool.irascible.irc.IRCClient;
import app.drool.irascible.irc.IRCCommand;
import app.drool.irascible.irc.IRCServerData;
import app.drool.irascible.utils.CacheUtils;

public class BroadcastService extends Service implements IRCClient.IRCClientListener{
    private final String TAG = this.getClass().getSimpleName();
    private boolean isPaused;
    private boolean shouldStopThreads = false;
    private final LinkedList<Intent> messageQueue = new LinkedList<>();
    private IRCServerData serverData = null;
    private IRCClient client = null;
    private boolean shouldExecuteCommands = false;
    private Notification notification;

    private final Thread networkThread = new Thread(new Runnable() {
        @Override
        public void run() {
            client = new IRCClient(BroadcastService.this, serverData);
            if (!client.connect()) {
                client.destroy();
                return;
            }
            if (!client.login()) {
                client.destroy();
                return;
            }
            client.startReadThread();

            //noinspection StatementWithEmptyBody
            while (!shouldExecuteCommands) { }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) { }
                    runServerCommands();
                }
            }).start();

            //noinspection StatementWithEmptyBody
            while (!shouldStopThreads) { }
            client.destroy();
        }
    });

    private final Thread broadcastThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!shouldStopThreads) {
                removeFromOutgoingQueue();
            }
        }
    });

    private void removeFromOutgoingQueue() {
        synchronized (messageQueue) {
            if (messageQueue.isEmpty()) {
                try {
                    messageQueue.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "broadcastThread: ", e);
                }
            }

            if (!isPaused) {
                Intent broadcastIntent = messageQueue.remove(0);
                sendBroadcast(broadcastIntent);
            }
        }
    }

    private void addToOutgoingQueue(Intent newIntent) {
        synchronized (messageQueue) {
            messageQueue.add(newIntent);
            messageQueue.notify();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch(intent.getAction()) {
            case SERVICE.ACTIONS.startService:
                Log.d(TAG, "onStartCommand: should start service");
                if (serverData == null) {
                    serverData = (IRCServerData) intent.getSerializableExtra("serverData");
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                    Intent clickIntent = new Intent(this, ChatActivity.class);
                    PendingIntent appIntent = PendingIntent.getActivity(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification = new NotificationCompat.Builder(this)
                            .setContentTitle("Irascible is running")
                            .setTicker("Irascible is running")
                            .setContentIntent(appIntent)
                            .setContentText("Connected to " + serverData.getServerName())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                            .setOngoing(true)

                            .build();


                    startForeground(1001, notification);
                    CacheUtils.clearSessionLog(BroadcastService.this);
                    networkThread.start();
                    broadcastThread.start();
                } else {
                    Log.d(TAG, "onStartCommand: ignored start request");
                }
                break;
            
            case SERVICE.ACTIONS.stopService:
                Log.d(TAG, "onStartCommand: should stop service");
                shouldStopThreads = true;
                stopForeground(true);
                CacheUtils.clearSessionLog(BroadcastService.this);
                stopSelf();
                break;

            case SERVICE.ACTIONS.pauseService:
                Log.d(TAG, "onStartCommend: should pause service");
                isPaused = true;
                break;

            case SERVICE.ACTIONS.resumeService:
                Log.d(TAG, "onStartCommand: should resume service");
                isPaused = false;
                break;

            case SERVICE.ACTIONS.broadcastFromActivity:
                if (client != null)
                    client.writeToBuffer(intent.getStringExtra("data"));
                break;

            case SERVICE.ACTIONS.heartbeatRequest:
                if (client != null) {
                    Intent heartbeatIntent = new Intent();
                    heartbeatIntent.setAction(SERVICE.ACTIONS.heartbeatFromService);
                    sendBroadcast(heartbeatIntent);
                }

            default:
                Log.d(TAG, "onStartCommand: unrecognized action received");
                break;
        }
        
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // IMPL : IRCClientListener
    @Override
    public void receiveMessageFromClient(String message) {
        Log.d(TAG, "receiveMessageFromClient: " + message);

        if (message.length() < 1)
            return;

        if (!shouldExecuteCommands && message.split(" ")[1].contentEquals("001"))
            shouldExecuteCommands = true;

        Date currentDate = new Date();
        String messageWithTimestamp = String.valueOf(currentDate.getTime()) + " "
                                    + message;

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SERVICE.ACTIONS.broadcastFromService);
        broadcastIntent.putExtra("data", messageWithTimestamp);
        addToOutgoingQueue(broadcastIntent);
    }

    @Override
    public void receiveErrorFromClient(String error) {
        Intent errorIntent = new Intent();
        errorIntent.setAction(SERVICE.ACTIONS.broadcastErrorFromService);
        errorIntent.putExtra("errorType", error);
        addToOutgoingQueue(errorIntent);
    }

    private void runServerCommands() {
        for (String line : serverData.getServerCommands()) {
            IRCCommand command = IRCCommand.parse(line, "server");
            if (command.isValid())
                client.writeToBuffer(command.getFormattedMessage());

            if (command.isSelfCommand()) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(SERVICE.ACTIONS.broadcastFromService);
                broadcastIntent.putExtra("dataSelf", command.getFormattedMessage());
                addToOutgoingQueue(broadcastIntent);
            }
        }
    }
}
