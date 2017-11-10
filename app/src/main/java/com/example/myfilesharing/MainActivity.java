package com.example.myfilesharing;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

//
// TODO: Not working yet. No image files available when running emulator
//       To show list of images available on local storage. User will select a image and send to
//       another app
public class MainActivity extends Activity {

    // The path to the root of this app's internal storage
    private File mPrivateRootDir;
    // The path to the "images" subdirectory
    private File mImagesDir;
    // Array of files in the images subdirectory
    File[] mImageFiles;

    private ListView mFileListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up an Intent to send back to apps that request a file
        final Intent mResultIntent =  new Intent("com.example.myapp.ACTION_RETURN_FILE");

        // Get the files/ subdirectory of internal storage
        mImagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Log.v("Kelvin", mImagesDir.getAbsolutePath());

        // Get the files in the images subdirectory
        mImageFiles = mImagesDir.listFiles();

        // Set the Activity's result to null to begin with
        setResult(Activity.RESULT_CANCELED, null);

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < mImageFiles.length; ++i) {
            list.add(mImageFiles[i].getAbsolutePath());
        }

        mFileListView = findViewById(R.id.list_view);

        // Define a new adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, list);

        // Assign adapter to listview
        mFileListView.setAdapter(adapter);

        // Define a listener that responds to clicks on a file in the ListView
        mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            /*
             * When a filename in the ListView is clicked, get its
             * content URI and send it to the requesting app
             */
            public void onItemClick(AdapterView<?> adapterView,
                                    View view,
                                    int position,
                                    long rowId) {

                // ListView clicked item value
                String itemValue = (String)mFileListView.getItemAtPosition(position);

                /*
                 * Get a File for the selected file name.
                 * Assume that the file names are in the
                 * mImageFilename array.
                 */
                File requestFile = new File(mImageFiles[position].getAbsolutePath());

                /*
                 * Most file-related method calls need to be in
                 * try-catch blocks.
                 */

                // Use the FileProvider to get a content URI
                try {
                    Uri fileUri = FileProvider.getUriForFile(
                            MainActivity.this,
                            "com.example.myfilesharing.fileprovider",
                            requestFile);

                    if (fileUri != null) {
                        // Grant temporary read permission to the content URI
                        mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        // Put the Uri and MIME type in the result Intent
                        mResultIntent.setDataAndType(
                                fileUri,
                                getContentResolver().getType(fileUri));

                        // Set the result
                        MainActivity.this.setResult(Activity.RESULT_OK, mResultIntent);

                    } else {
                            mResultIntent.setDataAndType(null, "");
                            MainActivity.this.setResult(RESULT_CANCELED, mResultIntent);
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e("File Selector", "The selected file can't be shared: "
                                + mImageFiles[position].getAbsolutePath());
                    }
                }
        });
    }

    public void onDoneClicked(View view) {
        finish();
    }
}
