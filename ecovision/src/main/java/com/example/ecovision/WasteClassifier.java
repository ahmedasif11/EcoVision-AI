package com.example.ecovision;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the WasteNet model and turns one bitmap into a category.
 * The displayed category is the labels.txt line for the largest softmax score.
 */
public class WasteClassifier {

    static final int INPUT_SIZE = 224;
    private static final int CLASS_COUNT = 12;

    private Interpreter interpreter;
    private final List<String> labels;

    /** One model result. The disposal sentence is a fixed rule, not a model output. */
    public static class Result {
        public final String label;
        public final float confidence;
        public final String disposal;

        Result(String label, float confidence, String disposal) {
            this.label = label;
            this.confidence = confidence;
            this.disposal = disposal;
        }
    }

    /**
     * Maps model.tflite into memory and reads labels.txt.
     * Throws if the asset is missing or the tensors do not match the checked contract.
     */
    public WasteClassifier(Context context) throws IOException {
        // Same idea as the number-guessing sample: map the asset, then give that buffer to Interpreter.
        // No GPU or NNAPI delegate is added, so this interpreter stays on the CPU.
        interpreter = new Interpreter(loadModelFile(context));
        try {
            labels = readLabels(context);
            checkContract();
        } catch (IOException | IllegalArgumentException exception) {
            close();
            throw exception;
        }
    }

    /**
     * Resizes the bitmap, scales pixels to 0–1, and runs the interpreter.
     * Call this off the UI thread. The returned label is chosen from the output scores.
     */
    public synchronized Result classify(Bitmap bitmap) {
        if (interpreter == null) {
            throw new IllegalStateException("The interpreter is closed");
        }

        // Training resized every photo to 224 by 224 and divided each channel by 255.
        // filter=true resamples the pixels. The picture is stretched if its shape is not square.
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        float[][][][] input = new float[1][INPUT_SIZE][INPUT_SIZE][3];
        try {
            fillInput(scaled, input);
        } finally {
            // The activity still displays the original bitmap, so only the copy is released.
            if (scaled != bitmap) {
                scaled.recycle();
            }
        }

        // Output is one softmax row. The twelve values are class probabilities.
        float[][] output = new float[1][CLASS_COUNT];
        interpreter.run(input, output);

        int bestIndex = 0;
        for (int index = 1; index < CLASS_COUNT; index++) {
            if (output[0][index] > output[0][bestIndex]) {
                bestIndex = index;
            }
        }
        String label = labels.get(bestIndex);
        return new Result(label, output[0][bestIndex], disposalFor(label));
    }

    /** Closes the native interpreter. Safe to call more than once. */
    public synchronized void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    private void checkContract() throws IOException {
        if (labels.size() != CLASS_COUNT) {
            throw new IOException("labels.txt does not contain 12 labels");
        }
        if (interpreter.getInputTensor(0).dataType() != DataType.FLOAT32
                || interpreter.getOutputTensor(0).dataType() != DataType.FLOAT32) {
            throw new IOException("The model tensors are not float32");
        }

        int[] inputShape = interpreter.getInputTensor(0).shape();
        int[] outputShape = interpreter.getOutputTensor(0).shape();
        // The stored batch is 1. A signature batch of -1 means the file allows a variable batch.
        boolean inputMatches = inputShape.length == 4
                && (inputShape[0] == 1 || inputShape[0] == -1)
                && inputShape[1] == INPUT_SIZE
                && inputShape[2] == INPUT_SIZE
                && inputShape[3] == 3;
        boolean outputMatches = outputShape.length == 2
                && (outputShape[0] == 1 || outputShape[0] == -1)
                && outputShape[1] == CLASS_COUNT;
        if (!inputMatches || !outputMatches) {
            throw new IOException("The model shape does not match the checked contract");
        }
    }

    /**
     * Copies RGB values into an NHWC array.
     * Android pixels are ARGB. Color.red, Color.green, and Color.blue keep red first.
     * The training script does not swap red and blue, and it does not use Keras preprocess_input.
     */
    private static void fillInput(Bitmap scaled, float[][][][] input) {
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        scaled.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int pixel = pixels[y * INPUT_SIZE + x];
                input[0][y][x][0] = Color.red(pixel) / 255.0f;
                input[0][y][x][1] = Color.green(pixel) / 255.0f;
                input[0][y][x][2] = Color.blue(pixel) / 255.0f;
            }
        }
    }

    private static MappedByteBuffer loadModelFile(Context context) throws IOException {
        // openFd works only when the APK stores the .tflite file uncompressed.
        try (AssetFileDescriptor descriptor = context.getAssets().openFd("model.tflite");
                FileInputStream stream = new FileInputStream(descriptor.getFileDescriptor())) {
            FileChannel channel = stream.getChannel();
            long start = descriptor.getStartOffset();
            long length = descriptor.getDeclaredLength();
            if (length == AssetFileDescriptor.UNKNOWN_LENGTH) {
                length = descriptor.getLength();
            }
            return channel.map(FileChannel.MapMode.READ_ONLY, start, length);
        }
    }

    private static List<String> readLabels(Context context) throws IOException {
        List<String> labels = new ArrayList<>();
        // The line number is the class index: line 0 is battery, line 11 is white-glass.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open("labels.txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String label = line.trim();
                if (!label.isEmpty()) {
                    labels.add(label);
                }
            }
        }
        return labels;
    }

    /** Fixed disposal hint for a verified label. This lookup is not another model. */
    private static String disposalFor(String label) {
        switch (label) {
            case "battery":
                return "Take batteries to a battery recycling point. Do not put them in household trash.";
            case "biological":
                return "Put food scraps and garden waste in a compost or organic bin.";
            case "brown-glass":
                return "Rinse the brown glass and put it in the glass recycling bin.";
            case "cardboard":
                return "Flatten clean cardboard and put it in the cardboard recycling bin.";
            case "clothes":
                return "Donate clothes that can still be worn, or take them to a textile recycling point.";
            case "green-glass":
                return "Rinse the green glass and put it in the glass recycling bin.";
            case "metal":
                return "Rinse the metal and put it in the metal recycling bin.";
            case "paper":
                return "Keep the paper dry and put it in the paper recycling bin.";
            case "plastic":
                return "Rinse the plastic and recycle it where that type is accepted.";
            case "shoes":
                return "Donate wearable shoes, or take worn-out shoes to a shoe recycling point.";
            case "trash":
                return "Put this in the general trash if local recycling does not accept it.";
            case "white-glass":
                return "Rinse the clear glass and put it in the glass recycling bin.";
            default:
                return "No disposal hint is stored for this label.";
        }
    }
}
