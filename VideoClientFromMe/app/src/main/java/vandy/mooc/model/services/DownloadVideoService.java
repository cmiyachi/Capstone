package vandy.mooc.model.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import vandy.mooc.model.mediator.VideoDataMediator;

/**
 * Created by cmiyachi on 7/12/2015.
 */
public class DownloadVideoService extends IntentService {

        /**
         * Custom Action that will be used to send Broadcast to the
         * VideoListActivity.
         */
        public static final String ACTION_DOWNLOAD_SERVICE_RESPONSE =
                "vandy.mooc.services.DownloadVideoService.RESPONSE";

        /**
         * It is used by Notification Manager to send Notifications.
         */
        private static final int NOTIFICATION_ID = 1;

        /**
         * VideoDataMediator mediates the communication between Video
         * Service and local storage in the Android device.
         */
        private VideoDataMediator mVideoMediator;

        /**
         * Manages the Notification displayed in System UI.
         */
        private NotificationManager mNotifyManager;

        /**
         * Builder used to build the Notification.
         */
        private NotificationCompat.Builder mBuilder;

        /**
         * Constructor for UploadVideoService.
         *
         * @param name
         */
        public DownloadVideoService(String name) {
        super("DownloadVideoService");
    }

        /**
         * Default Constructor for DownloadVideoService.
         *
         */
        public DownloadVideoService() {
        super("DownloadVideoService");
    }

        /**
         * Factory method that makes the explicit intent another Activity
         * uses to call this Service.
         *
         * @param context
         * @return
         */
    public static Intent makeIntent(Context context, Long videoId, String videoName) {
        return new Intent(context,
                DownloadVideoService.class)
                .putExtra("VIDEO_ID", videoId)
                .putExtra("VIDEO_NAME", videoName);
    }

    /**
     * Hook method that is invoked on the worker thread with a request
     * to process. Only one Intent is processed at a time, but the
     * processing happens on a worker thread that runs independently
     * from other application logic.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent)  {
        // Starts the Notification to show the progress of video
        // upload.
        startNotification();

        // Create VideoDataMediator that will mediate the communication
        // between Server and Android Storage.
        mVideoMediator =
                new VideoDataMediator();

        // Check if Video download is successful.
     finishNotification(mVideoMediator.downloadVideo(getApplicationContext(), intent.getLongExtra("VIDEO_ID", 1),
               intent.getStringExtra("VIDEO_NAME")));

        // Send the Broadcast to VideoListActivity that the Video
        // Download is completed.
        sendBroadcast();
    }

    /**
     * Send the Broadcast to Activity that the Video Download is
     * completed.
     */
    private void sendBroadcast(){
        // Use a LocalBroadcastManager to restrict the scope of this
        // Intent to the VideoDownloadClient application.
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(ACTION_DOWNLOAD_SERVICE_RESPONSE)
                        .addCategory(Intent.CATEGORY_DEFAULT));
    }

    /**
     * Finish the Notification after the Video is Downloaded.
     *
     * @param status
     */
    private void finishNotification(String status) {
        // When the loop is finished, updates the notification.
        mBuilder.setContentTitle(status)
                // Removes the progress bar.
                .setProgress (0,
                        0,
                        false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentText("")
                .setTicker(status);

        // Build the Notification with the given
        // Notification Id.
        mNotifyManager.notify(NOTIFICATION_ID,
                mBuilder.build());
    }

    /**
     * Starts the Notification to show the progress of video download.
     */
    private void startNotification() {
        // Gets access to the Android Notification Service.
        mNotifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the Notification and set a progress indicator for an
        // operation of indeterminate length.
        mBuilder = new NotificationCompat
                .Builder(this)
                .setContentTitle("Video Download")
                .setContentText("Download in progress")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setTicker("Downloading video")
                .setProgress(0,
                        0,
                        true);

        // Build and issue the notification.
        mNotifyManager.notify(NOTIFICATION_ID,
                mBuilder.build());
    }
}
