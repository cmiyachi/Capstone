package vandy.mooc.videoapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class NewVideoActivity extends Activity {
EditText name, duration, url;
    private String mCurrentPhotoPath;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mCurrentSelfieName;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final long INTERVAL_TWO_MINUTES = 2 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_video);
        /**
        name = (EditText)findViewById(R.id.editText_name);
        duration  = (EditText)findViewById(R.id.editText_duration);
        url = (EditText)findViewById(R.id.editText_url);
**/


    }
    public void AddNewVideoButtonPressed (View v) {


            final Video video = new Video(); // (name.getText().toString(),
                                 //    new Long (duration.getText().toString()),
                                  //   url.getText().toString());

                     //  VideoOpsFragment.instance.addVideo(video);


           dispatchTakePictureIntent();




        finish();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        mCurrentSelfieName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile = File.createTempFile(
                mCurrentSelfieName,
                ".jpg",
                getExternalFilesDir(null));

        mCurrentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
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
            File selfieFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), mCurrentSelfieName + ".jpg");
            photoFile.renameTo(selfieFile);

            Log.d(LOG_TAG,"*******************************path to file "+photoFile.getPath()+"***"+photoFile.getName());

        }
        else {
            File photoFile = new File(mCurrentPhotoPath);
            photoFile.delete();
        }
    }
}
