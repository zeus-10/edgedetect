package com.example.edgedetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.example.edgedetection.gl.GLTextureView;
import com.example.edgedetection.jni.OpenCVProcessor;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_CODE = 1001;

    private GLTextureView glTextureView;
    private OpenCVProcessor openCVProcessor;
    private ExecutorService cameraExecutor;

    static {
        System.loadLibrary("edgedetection");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glTextureView = findViewById(R.id.gl_texture_view);
        openCVProcessor = new OpenCVProcessor();
        cameraExecutor = Executors.newSingleThreadExecutor();
        Log.d(TAG, stringFromJNI());

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindImageAnalysis(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindImageAnalysis(ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::processImage);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera", e);
        }
    }

    private void processImage(ImageProxy image) {

        if (isProcessing) {
            image.close();
            return;
        }

        isProcessing = true;

        try {
            Log.d(TAG, "Processing frame: " + image.getWidth() + "x" + image.getHeight() +
                    " format: " + image.getFormat());


            ImageProxy.PlaneProxy yPlane = image.getPlanes()[0];
            ByteBuffer yBuffer = yPlane.getBuffer();


            int pixelStride = yPlane.getPixelStride();
            int rowStride = yPlane.getRowStride();
            int rowPadding = rowStride - pixelStride * image.getWidth();

            byte[] yData;
            if (rowPadding == 0) {

                yData = new byte[yBuffer.remaining()];
                yBuffer.get(yData);
            } else {

                yData = new byte[image.getWidth() * image.getHeight()];
                for (int row = 0; row < image.getHeight(); row++) {
                    yBuffer.get(yData, row * image.getWidth(), image.getWidth());
                    if (row < image.getHeight() - 1) {
                        yBuffer.position(yBuffer.position() + rowPadding);
                    }
                }
            }

            Log.d(TAG, "Y data size: " + yData.length);


            byte[] processedFrame = openCVProcessor.processFrame(yData,
                    image.getWidth(), image.getHeight());

            if (processedFrame != null) {
                Log.d(TAG, "Processed frame size: " + processedFrame.length);
                runOnUiThread(() -> {
                    glTextureView.updateTexture(processedFrame,
                            image.getWidth(), image.getHeight());
                    isProcessing = false;
                });
            } else {
                Log.e(TAG, "Processed frame is null");
                isProcessing = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing frame", e);
            isProcessing = false;
        } finally {
            image.close();
        }
    }

    private volatile boolean isProcessing = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    public native String stringFromJNI();
}