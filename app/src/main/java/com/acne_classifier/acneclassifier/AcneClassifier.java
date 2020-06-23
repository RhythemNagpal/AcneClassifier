package com.acne_classifier.acneclassifier;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class AcneClassifier implements Classifier{
    private static final int MAX_RESULTS = 2;
    private static final int BATCH_SIZE = 1;
    private static final int PIXEL_SIZE = 3;
    private static final float THRESHOLD = 0.1f;

    private static final int IMAGE_MEAN = 150;
    private static final float IMAGE_STD = 255.0f;

    private Interpreter interpreter;
    private int inputSize;
    private List<String> labelList;
    private boolean quant;

    private AcneClassifier() {

    }

    static AcneClassifier create(AssetManager assetManager,
                             String modelPath,
                             String labelPath,
                             int inputSize,
                             boolean quant) throws IOException {

        AcneClassifier classifier = new AcneClassifier();
        classifier.interpreter = new Interpreter(classifier.loadModelFile(assetManager, modelPath), new Interpreter.Options());
        classifier.labelList = classifier.loadLabelList(assetManager, labelPath);
        classifier.inputSize = inputSize;
        classifier.quant = quant;

        return classifier;
    }

    @Override
    public List<Classifier.Recognition> recognizeImage(Bitmap bitmap) {
        FloatBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);

            float[][] result = new float[1][labelList.size()];
            interpreter.run(byteBuffer, result);
            return getSortedResultFloat(result);


    }

    @Override
    public void close() {
        interpreter.close();
        interpreter = null;
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private FloatBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        FloatBuffer byteBuffer;
        ByteBuffer bb;
        if(quant) {
            bb= ByteBuffer.allocateDirect(BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE * 4);
        } else {
            bb = ByteBuffer.allocateDirect(4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
        }

        byteBuffer= bb.order(ByteOrder.nativeOrder()).asFloatBuffer();
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                if(quant){
                    byteBuffer.put(((val >> 16) & 0xFF));
                    byteBuffer.put((val >> 8) & 0xFF);
                    byteBuffer.put((val & 0xFF));
                } else {
                    byteBuffer.put((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    byteBuffer.put((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    byteBuffer.put((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                }

            }
        }
        Log.e("pixels",String.valueOf(intValues.length));

        /*for (int pixelValue:intValues) {
            int r = (int) ((pixelValue >> 16) & 0xFF);
            int g = (int) ((pixelValue >> 8) & 0xFF);
            int b = (int) (pixelValue  & 0xFF);

            // Convert RGB to grayscale and normalize pixel value to [0..1].
            Float normalizedPixelValue = (r + g + b) / 3.0f;
            byteBuffer.putFloat(normalizedPixelValue);
        }*/
        return byteBuffer;
    }

    /*@SuppressLint("DefaultLocale")
    private List<Classifier.Recognition> getSortedResultByte(Float[][] labelProbArray) {
        ByteBuffer.allocate(4).putFloat(labelProbArray).array();

        PriorityQueue<Classifier.Recognition> pq =
                new PriorityQueue<>(
                        MAX_RESULTS,
                        new Comparator<Classifier.Recognition>() {
                            @Override
                            public int compare(Classifier.Recognition lhs, Classifier.Recognition rhs) {
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (int i = 0; i < labelList.size(); ++i) {
            float confidence = (labelProbArray[0][i] & 0xff) / 255.0f;
            if (confidence > THRESHOLD) {
                pq.add(new Classifier.Recognition("" + i,
                        labelList.size() > i ? labelList.get(i) : "unknown",
                        confidence, quant));
            }
        }

        final ArrayList<Classifier.Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }*/

    @SuppressLint("DefaultLocale")
    private List<Classifier.Recognition> getSortedResultFloat(float[][] labelProbArray) {

        PriorityQueue<Classifier.Recognition> pq =
                new PriorityQueue<>(
                        MAX_RESULTS,
                        new Comparator<Classifier.Recognition>() {
                            @Override
                            public int compare(Classifier.Recognition lhs, Classifier.Recognition rhs) {
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (int i = 0; i < labelList.size(); ++i) {
            float confidence = labelProbArray[0][i];
            if (confidence > THRESHOLD) {
                pq.add(new Classifier.Recognition("" + i,
                        labelList.size() > i ? labelList.get(i) : "unknown",
                        confidence, quant));
            }
        }

        final ArrayList<Classifier.Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }

        return recognitions;
    }

}

