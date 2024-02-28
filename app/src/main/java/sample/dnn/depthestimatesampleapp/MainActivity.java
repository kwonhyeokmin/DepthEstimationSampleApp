package sample.dnn.depthestimatesampleapp;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.FloatBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

// CameraX
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

// ONNX
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class MainActivity extends PermissionActivity {

    PreviewView mPreviewView;
    private DataProcess dataProcess = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mPreviewView = findViewById(R.id.previewView);
        setPermissions();

        // load dnn model pipeline
        this.dataProcess = new DataProcess(this);
        modelLoad();

        startCamera();
    }

    protected void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                .build();

        Preview preview = new Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build();
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(),
                image -> {
                    Bitmap bitmap = ImageUtils.imageToBitmap(image, dataProcess.INPUT_WIDTH_SIZE, dataProcess.INPUT_HEIGHT_SIZE);
                    FloatBuffer buffer = ImageUtils.bitmapToFloatBuffer(bitmap);
                    modelRunWithBuffer(buffer);
                });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
    }

    private void modelLoad() {
        this.dataProcess.loadOnnxModel();
    }

    private void modelRunWithBuffer(FloatBuffer buffer) {
        this.dataProcess.run(buffer);
    }
}