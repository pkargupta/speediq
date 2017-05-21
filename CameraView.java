package com.agnik.priyankakargupta.speediq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class CameraView extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "VelocityFragment";

    private Camera mCamera;
    private CameraPreview mCameraPreview;

    private Activity mActivity;

    private ArrayList<Bitmap> images;
    private static final int NUM_IMAGES = 3;
    private static final long DELTA_TIME = 1000 * 1;
    private static final double MAX_SPEED_MPS_VEHICLE = 53.6448;
    private static final double MAX_SPEED_MPS_WALKING = 1.34112;
    private static double MAX_SPEED;
    private static boolean isWalking = true;
    private static final double MPS_TO_MPH = 2.2369356;
    private Button setcolor;
    private View capturebutton;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public static double speed;
    private float mLocationSpeed;
    private double mLocationRate;
    public String selectedcolor;
    public static String classification;
    public int selectedx;
    public int selectedy;
    public Bitmap bitmap;
    public FrameLayout cameraframe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
        setcolor = (Button) findViewById(R.id.setcolor);
        capturebutton = (View) findViewById(R.id.start_button);
        cameraframe = (FrameLayout) findViewById(R.id.camera_frame);
        cameraframe.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                selectedx = (int) event.getX();
                selectedy = (int) event.getY();
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        images.clear();
                        Toast.makeText(CameraView.this, "pressed x: " + selectedx, Toast.LENGTH_SHORT).show();
                        new ImageAsyncTask().execute();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        break;

                    case MotionEvent.ACTION_UP:

                        break;
                }

                return true;
            }
        });
        setcolor.setOnClickListener(new android.view.View.OnClickListener() {

            public void onClick(View v) {
                //To register the button with context menu.
                registerForContextMenu(setcolor);
                openContextMenu(setcolor);

            }
        });
        //selectedcolor = "red";
        mActivity = this;
        MAX_SPEED = isWalking ? MAX_SPEED_MPS_WALKING : MAX_SPEED_MPS_VEHICLE;
        setupCamera(findViewById(android.R.id.content));
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch(v.getId()) {
            case R.id.setcolor:
            menu.setHeaderTitle("Select the color of the vehicle");
            menu.add(0, v.getId(), 0, "red");//groupId, itemId, order, title
            menu.add(0, v.getId(), 0, "orange");
            menu.add(0, v.getId(), 0, "yellow");
            menu.add(0, v.getId(), 0, "green");
            menu.add(0, v.getId(), 0, "blue");
            menu.add(0, v.getId(), 0, "cyan");
            menu.add(0, v.getId(), 0, "magenta");
            menu.add(0, v.getId(), 0, "grey");
            menu.add(0, v.getId(), 0, "black");
            menu.add(0, v.getId(), 0, "white");
                break;
            case R.id.start_button:
                menu.setHeaderTitle("How would you classify the speed?");
                menu.add(0, v.getId(), 0, "Safe");//groupId, itemId, order, title
                menu.add(0, v.getId(), 0, "Risky");
                menu.add(0, v.getId(), 0, "Dangerous");

        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getItemId() == R.id.setcolor) {
            setcolor.setText(item.getTitle());
            selectedcolor = String.valueOf(item.getTitle());
        }
        else if(item.getItemId() == R.id.start_button){
            classification = String.valueOf(item.getTitle());
            Intent intent = new Intent(CameraView.this, SpeedActivity.class);
            startActivity(intent);
        }
        return true;
    }
    private void setupCamera(View view) {
        if (checkCameraHardware(view.getContext())) {
            mCamera = getCameraInstance();
            if (mCamera != null) {
                images = new ArrayList<>();
                mCameraPreview = new CameraPreview(view.getContext(), mCamera);
                FrameLayout fl = (FrameLayout) view.findViewById(R.id.camera_frame);
                fl.addView(mCameraPreview);

                Camera.Parameters cp = mCamera.getParameters();
                List<Camera.Size> sizes = cp.getSupportedPictureSizes();
                cp.setPictureSize(sizes.get(sizes.size() - 1).width, sizes.get(sizes.size() - 1).height);


                // Add a listener to the Capture button
                capturebutton = view.findViewById(R.id.start_button);
                capturebutton.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // get an image from the
                                images.clear();
                                Toast.makeText(CameraView.this, "button clicked",Toast.LENGTH_SHORT).show();
                                //mCamera.takePicture(null, null, mPicture);
                                new ImageAsyncTask().execute();

                            }
                        }
                );
                // Create an instance of GoogleAPIClient.
                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();
                }
                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(DELTA_TIME);
                mLocationRequest.setFastestInterval(DELTA_TIME);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);

            } else {
                //TODO ask for camera permission and try again
            }
        }

    }
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), options);
                images.add(bitmap);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            mCamera.startPreview();
        }
    };
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CES2017");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
        /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        SurfaceHolder mHolder;
        Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                Display display = ((WindowManager) getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

                if (display.getRotation() == Surface.ROTATION_0) {
                    parameters.setPreviewSize(h, w);
                    mCamera.setDisplayOrientation(90);
                }

                if (display.getRotation() == Surface.ROTATION_90) {
                    parameters.setPreviewSize(w, h);
                    mCamera.setDisplayOrientation(0);
                }

                if (display.getRotation() == Surface.ROTATION_180) {
                    parameters.setPreviewSize(h, w);
                    mCamera.setDisplayOrientation(270);
                }

                if (display.getRotation() == Surface.ROTATION_270) {
                    parameters.setPreviewSize(w, h);
                    mCamera.setDisplayOrientation(180);
                }
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private class ImageAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < NUM_IMAGES; i++) {
                mCamera.takePicture(null, null, mPicture);
                try {
                    Thread.sleep(DELTA_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Toast.makeText(mActivity, "DONE!", Toast.LENGTH_LONG).show();
            getRate(images, DELTA_TIME/1000);
            speed = Math.abs(getTargetSpeed());
            Log.d(TAG, "target speed:" + speed);
            //TODO pass on to next screen
            Log.d(TAG, "Bitmap array size: " + images.size());
            registerForContextMenu(capturebutton);
            openContextMenu(capturebutton);
        }
    }
    public double getTargetSpeed(){
        Log.d(TAG, "location speed: " + mLocationSpeed + ", location rate: " + mLocationRate + ", other: " + (MAX_SPEED-mLocationSpeed));
        double result = (mLocationSpeed + mLocationRate * (MAX_SPEED-mLocationSpeed)) * MPS_TO_MPH;
        Log.d(TAG, "" + result);
        return result;
    }

    public double getRate(ArrayList<Bitmap> images, double time){
        int c1 = 0;
        int c2 = 0;
        int d1 = 0;
        int d2 = 0;
        PixelColor pc = new PixelColor(mActivity);

        for (int j = 0; j < images.size() - 1; j++) {
            d1 = 0;
            d2 = 0;
            Bitmap bitmapa = images.get(j);
            Bitmap bitmapb = images.get(j+1);
            Toast.makeText(CameraView.this, "selectedx:" + selectedx, Toast.LENGTH_SHORT).show();
            if(selectedx != 0) {
                //selectedpixel based on the user touch event
                int selectedpixel = bitmapa.getPixel(selectedx, selectedy);
                int selectedr = Color.red(selectedpixel);
                int selectedg = Color.green(selectedpixel);
                int selectedb = Color.blue(selectedpixel);
                float selectedhsv[] = new float[3];
                Color.RGBToHSV(selectedr, selectedg, selectedb, selectedhsv);
                selectedcolor = pc.getColor(selectedhsv);
                Toast.makeText(CameraView.this, "selectedcolor: " + selectedcolor, Toast.LENGTH_SHORT).show();
            }

            pc.setSelectedColor(selectedcolor);

            int[] pixels = new int[bitmapa.getWidth() * bitmapa.getHeight()];
            bitmapa.getPixels(pixels, 0, bitmapa.getWidth(), 0, 0, bitmapa.getWidth(), bitmapa.getHeight());
            for (int i = 0; i < pixels.length; i++) {
                int r = Color.red(pixels[i]);
                int g = Color.green(pixels[i]);
                int b = Color.blue(pixels[i]);
                float hsv[] = new float[3];
                Color.RGBToHSV(r, g, b, hsv);
                if (pc.isColor(hsv)) {
                    c1++;
                    d1++;
                }
            }

            bitmapb.getPixels(pixels, 0, bitmapb.getWidth(), 0, 0, bitmapb.getWidth(), bitmapb.getHeight());
            for (int i = 0; i < pixels.length; i++) {
                int r = Color.red(pixels[i]);
                int g = Color.green(pixels[i]);
                int b = Color.blue(pixels[i]);
                float hsv[] = new float[3];
                Color.RGBToHSV(r, g, b, hsv);
                if (pc.isColor(hsv)) {
                    c2++;
                    d2++;
                }
            }
            Log.d(TAG, "d1: " + d1);
            Log.d(TAG, "d2: " + d2);
            Log.d(TAG, "rate: " +  (d1 - d2)/(d1*time));
        }

        Log.d(TAG, "C1: " + c1);
        Log.d(TAG, "C2: " + c2);
        if (c1 > 0)
            mLocationRate = (c1 - c2)/(c1*time);
        else
            mLocationRate = 0;

        return mLocationRate;
    }
    }

