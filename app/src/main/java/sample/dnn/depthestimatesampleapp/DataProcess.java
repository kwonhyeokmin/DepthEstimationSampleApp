package sample.dnn.depthestimatesampleapp;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class DataProcess {
    final int INPUT_BATCH_SIZE = 1;
    final int INPUT_HEIGHT_SIZE = 225;
    final int INPUT_WIDTH_SIZE = 225;
    final int INPUT_CHANNEL_SIZE = 3;
    final String MODEL_FILE_NAME = "model.onnx";


    private Context context = null;
    private OrtEnvironment env = null;
    public OrtSession model = null;

    // constructor
    public DataProcess(Context context) {
        this.context = context;
        this.env = OrtEnvironment.getEnvironment();
    }

    public void loadOnnxModel() {
        if (this.model != null)
            return;
        try {
            AssetManager assetManager = this.context.getAssets();
            InputStream inputStream = assetManager.open(MODEL_FILE_NAME);
            byte[] buffer = new byte[8192];
            int bytesRead;

            // buffer read
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            byte[] modelBuffer = output.toByteArray();
            this.model = this.env.createSession(modelBuffer, new OrtSession.SessionOptions());

        } catch (IOException e) {
            throw new RuntimeException("Model path is not valid. Please check the model path");
        } catch (OrtException e) {
            throw new RuntimeException("Failed when load onnx model with buffer");
        }
    }

    public float[][] run(FloatBuffer buffer) {
        String inputName = this.model.getInputNames().iterator().next();
        float[][] output = null;
        long[] shape = {
                (long) INPUT_BATCH_SIZE,
                (long) INPUT_CHANNEL_SIZE,
                (long) INPUT_HEIGHT_SIZE,
                (long) INPUT_WIDTH_SIZE
        };
        try {
            OnnxTensor inputTensor = OnnxTensor.createTensor(this.env, buffer, shape);
            OrtSession.Result outputTensor = model.run(Collections.singletonMap(inputName, inputTensor));
            output = (float[][]) outputTensor.get(0).getValue();

            return output;
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
    }
}
