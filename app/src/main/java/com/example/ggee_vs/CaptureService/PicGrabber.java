package com.example.ggee_vs.CaptureService;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.widget.TextView;

import com.example.ggee_vs.R;
import com.google.android.glass.content.Intents;

import java.io.File;

public class PicGrabber extends Activity {
    private static final int TAKE_PICTURE_REQUEST = 1;

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(intent, TAKE_PICTURE_REQUEST);
            ((TextView)findViewById(R.id.textViewTapToGrabPic)).setText(R.string.pic_grabbed);
        } catch (ActivityNotFoundException e) {
            ((TextView)findViewById(R.id.textViewTapToGrabPic)).setText(R.string.pic_grab_fail);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            String thumbnailPath = data.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);
            String picturePath = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);

            processPictureWhenReady(picturePath);
            // TODO: Show the thumbnail to the user while the full picture is being
            // processed.
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            // The picture is ready; process it.
        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath(),
                    FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
                // Protect against additional pending events after CLOSE_WRITE
                // or MOVED_TO is handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = affectedFile.equals(pictureFile);

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }

}
