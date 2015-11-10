package vandy.mooc.videoapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.mime.TypedFile;

import static vandy.mooc.videoapp.VideoOpsFragment.*;



public class MainActivity extends ListActivity {

    // items related to the alarm to take a selfie
    private static final int REQUEST_TAKE_PHOTO = 0;

    private static final long INITIAL_DELAY = 2*60; //*1000;
    private static final long REPEAT_DELAY = 2*60; //*1000;

    private static final String ALARM_KEY = "alarms";
    private static final String SELFIE_KEY = "selfiePath";
    private PendingIntent mAlarmOperation;
    private SharedPreferences mSharedPreferences;

    VideoOpsFragment mVideoOpsFragment;
    private int selectedListItem = -1;
    ArrayAdapter<Video> adapter;

    private String mCurrentPhotoPath;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mCurrentSelfieName;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final long INTERVAL_TWO_MINUTES = 2 * 60 * 1000L;
    private static long selfie_no = 0;
    private static final String selfieName = "mySelfie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);
        mVideoOpsFragment = getOps();
        mVideoOpsFragment.mActivity = new WeakReference<MainActivity>(this);

        adapter = new ArrayAdapter<>(this, R.layout.rowlayout,
                R.id.text_title);
        setListAdapter(adapter);
        startActivity(new Intent(this, LoginActivity.class));

        mSharedPreferences = getSharedPreferences("selfie", Context.MODE_PRIVATE);

        setAlarm(null,false);
    }


    public void onLoginButtonClicked (View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    VideoOpsFragment getOps() {
        VideoOpsFragment result;
        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        result = (VideoOpsFragment) fm.findFragmentByTag("data");

        // create the fragment and data the first time
        if (result  == null) {
            // add the fragment
            result  = new VideoOpsFragment ();
            fm.beginTransaction().add(result , "data").commit();
            // load the data from the web

        }

        return result;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        selectedListItem = position;
    }




    public void likeButton(View view) {
        if (selectedListItem == -1)
        {Toast.makeText(this, "No video selected" , Toast.LENGTH_SHORT).show();}
        else {

            Video video = (Video) getListAdapter().getItem(selectedListItem);
            mVideoOpsFragment.likeVideo(video);


        }
    }



    public void uploadButton(View view) {
        // startActivity(new Intent(this, NewVideoActivity.class));
        dispatchTakePictureIntent();
    }

    public void downloadButton(View view) {
        if (selectedListItem == -1) {
            Toast.makeText(this, "No video selected", Toast.LENGTH_SHORT).show();
        } else {

            Video video = (Video) getListAdapter().getItem(selectedListItem);
            mVideoOpsFragment.getVideoData(video.getId(), video.getName());


        }
    }


    public void unlikeButton(View view) {
        if (selectedListItem == -1)
        {Toast.makeText(this, "No video selected" , Toast.LENGTH_SHORT).show();}
        else {
            Video video = (Video) getListAdapter().getItem(selectedListItem);
          mVideoOpsFragment.unlikeVideo(video);
        }
    }
    /*
    private File createImageFile() throws IOException {
        // Create an image file name
        mCurrentSelfieName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile = File.createTempFile(
                mCurrentSelfieName,
                ".jpg",
                getExternalFilesDir(null));

        mCurrentPhotoPath = "file:" + imageFile.getAbsolutePath();
        return imageFile;
    } **/

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
       // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPath = image.getCanonicalPath();
        Log.d(LOG_TAG,mCurrentPhotoPath + "************************************");
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("ERROR:", ex.getMessage());
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG,"*******************Inside results");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Rename temporary file as yyyyMMdd_HHmmss.jpg
            File photoFile = new File(mCurrentPhotoPath);
         //   File selfieFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), mCurrentSelfieName + ".jpg");
          //  photoFile.renameTo(selfieFile);

            Log.d(LOG_TAG, "*******************************path to file " + photoFile.getPath() + "***" + photoFile.getName());


            selfie_no++;
            String self_no = Long.toString(selfie_no);
            Video v = new Video(selfieName+self_no,0, photoFile.getPath());
            v.setId(selfie_no);
           // mVideoOpsFragment.addVideo(v);
            mVideoOpsFragment.setVideoData(v.getId(), new TypedFile("image/jpg", photoFile));

        }
        else {
            File photoFile = new File(mCurrentPhotoPath);
            photoFile.delete();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.selfie_list, menu);

        MenuItem item = menu.findItem(R.id.action_alarm);
        //Setting the original enable/disable value for alarms
        setAlarm(item, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_alarm) {
            Log.d(TAG,"click on toggle alarm");
            setAlarm(item,true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Triggers the alarm if needed.
     * Also set the correct label for the item if provided.
     * Also toggle the alarm setting if requested
     * @param item the menu item to edit the label
     * @param toggle if the alarm parameter needs to be toggled
     */
    protected void setAlarm(MenuItem item,boolean toggle) {
        //Setting the alarm
        if (mAlarmOperation == null) {
            Log.d(TAG,"initiating alarm operation");
            mAlarmOperation = PendingIntent.getBroadcast(
                    getApplicationContext(),
                    0,
                    new Intent(getApplicationContext(),AlarmReceiver.class),
                    0);
        }

        boolean alarmEnabled = mSharedPreferences.getBoolean(ALARM_KEY, true);
        if (toggle) {
            Log.d(TAG,"requesting alarm toggle");
            alarmEnabled = !alarmEnabled;
            mSharedPreferences.edit().putBoolean(ALARM_KEY, alarmEnabled).commit();
        }

        AlarmManager alarm = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
        if (alarmEnabled) {
            Log.i(TAG,"programming alarm");
            alarm.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime()+INITIAL_DELAY,
                    REPEAT_DELAY, mAlarmOperation);
        } else {
            Log.i(TAG,"alarm disabled, canceling");
            alarm.cancel(mAlarmOperation);
        }

        if (item != null) {
            if (alarmEnabled)
                item.setTitle(R.string.action_disable_alarm);
            else
                item.setTitle(R.string.action_enable_alarm);
        }
    }

}





