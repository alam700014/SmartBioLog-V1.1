package com.android.fortunaattendancesystem.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.android.fortunaattendancesystem.R;
import com.android.fortunaattendancesystem.singleton.PhotoData;

import java.io.IOException;

public class CameraPreviewActivity extends Activity implements SurfaceHolder.Callback {

    Button btnCapture, btnCancel;
    private SurfaceView sv;
    private SurfaceHolder sHolder;
    private Camera.Parameters parameters;

    public Camera mCamera;
    public byte[] pictureData;
    public boolean safeToTakePicture = false;
    public boolean hasCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        btnCapture = (Button) findViewById(R.id.btnCapture);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        sv = (SurfaceView) findViewById(R.id.surface_camera);
        //Get a surface
        sHolder = sv.getHolder();
        //add the callback interface methods defined below as the Surface View callbacks
        sHolder.addCallback(this);
        //tells Android that this surface will have its data constantly replaced
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, mCall);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              finish();
            }
        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("TEST", "Surface Created");
        hasCamera = checkCameraHardware(CameraPreviewActivity.this);
        if (hasCamera) {
            mCamera = Camera.open();
            if (mCamera != null) {
                try {
                    //setCameraDisplayOrientation(this,1,mCamera);
                    mCamera.setDisplayOrientation(0);
                    mCamera.setPreviewDisplay(surfaceHolder);
                    mCamera.startPreview();
                    safeToTakePicture = true;
                    // previewing = true;
                } catch (IOException e) {
                    mCamera.release();
                    mCamera = null;
                }
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("TEST", "Surface Changed");

        if (hasCamera) {

            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                Log.d("TEST", "Error stoping camera preview: " + e.getMessage());
            }


            Camera.Parameters parameters = mCamera.getParameters();
            //parameters.set("jpeg-quality", 100);
           // parameters.set("orientation", "portrait");
           // parameters.setRotation(90);
           // parameters.setPictureSize(320, 240);

//            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
//            for (int count = 0; count < sizes.size(); count++) {
//                Log.d("TEST", "Width:" + sizes.get(count).width);
//                Log.d("TEST", "Height" + sizes.get(count).height);
//            }
           // parameters.setPictureSize(1248, 719);
          //  mCamera.setParameters(parameters);

            try {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                Log.d("TEST", "Error starting camera preview: " + e.getMessage());
            }

           // mCamera.startPreview();
            safeToTakePicture = true;


//            01-07 23:26:10.263 3868-3868/com.android.fortunabioattendance D/TEST: Width:2592
//            01-07 23:26:10.263 3868-3868/com.android.fortunabioattendance D/TEST: Height1944
//            01-07 23:26:10.263 3868-3868/com.android.fortunabioattendance D/TEST: Width:1600
//            01-07 23:26:10.263 3868-3868/com.android.fortunabioattendance D/TEST: Height1200
//            01-07 23:26:10.263 3868-3868/com.android.fortunabioattendance D/TEST: Width:1280
//            01-07 23:26:10.263 3868-3868/com.android.fortunabioattendance D/TEST: Height720


        }


//        if (hasCamera) {
//            Camera.Parameters parameters = mCamera.getParameters();
//            parameters.set("jpeg-quality", 100);
//            parameters.set("orientation", "portrait");
//            parameters.setRotation(90);
//
//            List <Camera.Size> allSizes = parameters.getSupportedPictureSizes();
//            Camera.Size size = allSizes.get(0); // get top size
//            for (int ii = 0; ii < allSizes.size(); ii++) {
//                if (allSizes.get(ii).width > size.width)
//                    size = allSizes.get(ii);
//            }
//
//
//            // parameters.setPictureSize(320, 240);
//            //parameters.setPictureSize(250, 200);
//
//            parameters.setPictureSize(size.width, size.height);
//
//
//            mCamera.setParameters(parameters);
//            mCamera.startPreview();
//            safeToTakePicture = true;
//        }


        //List <Size> sizes = parameters.getSupportedPreviewSizes();
        //Camera.Size optimalSize = getOptimalPreviewSize(sizes, 300, 150);
        //parameters.setPreviewSize(100, 100);


        //get camera parameters
//        parameters = mCamera.getParameters();
//        parameters.setRotation(90);
//        parameters.setPictureSize(300, 150);
//        parameters.setJpegQuality(100);


//        parameters.setPictureSize(320, 240);

//        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
//        for(int count=0;count<sizes.size();count++){
//            Log.d("TEST","Width:"+sizes.get(count).width);
//            Log.d("TEST","Height"+sizes.get(count).height);
//        }
//        Camera.Size x = sizes.get(0);
//        Log.d("TEST", "Default Width:" + x.width + " Height:" + x.height);

//        mCamera.setParameters(parameters);


        //set camera parameters
//        mCamera.setParameters(parameters);
//        mCamera.startPreview();

        //sets what code should be executed after the picture is taken
//        mCall = new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] data, Camera camera) {
//
//                mCamera.startPreview();
//                pictureData = new byte[data.length];
//                pictureData = data;
//
//                //Log.d("TEST","On Picture Taken");
//                // Log.d("TEST","Pic Data:"+data);
//                // Log.d("TEST","Pic Data Len:"+data.length);
//
//                //decode the data obtained by the camera into a Bitmap
//                // Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
//                //set the iv_image
//                // iv_image.setImageBitmap(bmp);
//            }
//        };

        // mCamera.takePicture(null, null, mCall);
    }


    public Camera.PictureCallback mCall = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera arg1) {

            Log.d("TEST","Picture Taken");

            Log.d("TEST","HasCamera:"+hasCamera);

            if (hasCamera) {
                PhotoData.getInstance().setCapturedPhotoData(data);

              //  Intent intent=new Intent(CameraPreviewActivity.this, EmployeeEnrollmentActivity.class);
               // startActivityForResult(intent, 1);

                setResult(1, new Intent());
                finish();


//                mCamera.startPreview();
//                safeToTakePicture = true;
//                pictureData = null;
//                pictureData = new byte[data.length];
//                System.arraycopy(data, 0, pictureData, 0, data.length);
//               // Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//                Intent intent=new Intent(CameraPreviewActivity.this,EmployeeEnrollmentActivity.class);
//                intent.putExtra("key1", "value1");
//                setResult(RESULT_OK, intent);
//                finish();


                //Bitmap correctBmp = Bitmap.createBitmap(bitmapPicture, 0, 0, bitmapPicture.getWidth(), bitmapPicture.getHeight(), null, true);

                // Bitmap photo = (Bitmap) data.getExtras().get("data");


//                image.setImageBitmap(bitmapPicture);
//                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byteimage = stream.toByteArray();
//                if (byteimage != null) {
//                    image.setImageBitmap(BitmapFactory.decodeByteArray(byteimage, 0, byteimage.length));
//                } else {
//                    image.setImageResource(R.drawable.dummyphoto);
//                }
            }
        }
    };


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d("TEST", "Surface Destroyed");
        if (hasCamera) {
            //stop the preview
            mCamera.stopPreview();
            //release the camera
            mCamera.release();
            //unbind the camera from this object
            mCamera = null;
            // previewing = false;
        }
    }


    public boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        } else {
            return false;
        }
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;

            }

        }
        return cameraId;
    }
}
