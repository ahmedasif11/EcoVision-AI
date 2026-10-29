package com.example.ecovision;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * One screen: the user selects a gallery image, and WasteNet classifies it on this device.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EcoVision";

    private ImageView selectedImage;
    private TextView categoryView;
    private TextView confidenceView;
    private TextView disposalView;
    private TextView statusView;
    private WasteClassifier classifier;

    // One background thread runs the interpreter so the screen does not freeze.
    private final ExecutorService classificationExecutor = Executors.newSingleThreadExecutor();
    // A newer image selection replaces an older result that has not been shown yet.
    private int requestId;

    // GetContent opens the system picker. The argument is a MIME type.
    // The callback gets a content Uri, or null when the user leaves without choosing.
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::showSelectedImage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            // Keep the button and result lines clear of the status and navigation bars.
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        selectedImage = findViewById(R.id.selectedImage);
        categoryView = findViewById(R.id.categoryView);
        confidenceView = findViewById(R.id.confidenceView);
        disposalView = findViewById(R.id.disposalView);
        statusView = findViewById(R.id.statusView);
        Button chooseImage = findViewById(R.id.chooseImageButton);

        // A missing or unexpected model keeps the screen open and shows a message.
        try {
            classifier = new WasteClassifier(this);
        } catch (Exception exception) {
            classifier = null;
            Log.e(TAG, "Could not load the waste model", exception);
            statusView.setText(R.string.status_model_missing);
        }

        // image/* accepts pictures only. This does not request a camera permission.
        chooseImage.setOnClickListener(view -> pickImage.launch("image/*"));
    }

    /**
     * Draws the chosen picture, then asks the model for a category.
     * An empty or unreadable result replaces the picture with a message.
     */
    private void showSelectedImage(Uri uri) {
        if (uri == null) {
            statusView.setText(R.string.status_cancelled);
            return;
        }

        Bitmap bitmap;
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            if (stream == null) {
                showUnreadableImage();
                return;
            }
            // ARGB_8888 makes the red, green, and blue channels explicit before preprocessing.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(stream, null, options);
        } catch (IOException | OutOfMemoryError exception) {
            showUnreadableImage();
            return;
        }

        if (bitmap == null) {
            showUnreadableImage();
            return;
        }

        selectedImage.setImageBitmap(bitmap);
        clearResult();
        if (classifier == null) {
            statusView.setText(R.string.status_model_missing);
            return;
        }

        statusView.setText(R.string.status_classifying);
        int thisRequest = ++requestId;
        classificationExecutor.execute(() -> {
            try {
                WasteClassifier.Result result = classifier.classify(bitmap);
                runOnUiThread(() -> showResult(thisRequest, result));
            } catch (Exception exception) {
                Log.e(TAG, "Inference failed", exception);
                runOnUiThread(() -> showInferenceFailure(thisRequest));
            }
        });
    }

    /** Shows the label, its softmax score, and the disposal sentence for that label. */
    private void showResult(int thisRequest, WasteClassifier.Result result) {
        if (isDestroyed() || thisRequest != requestId) {
            return;
        }
        categoryView.setText(getString(R.string.category_line, result.label));
        // The last model operator is softmax, so this score is a probability from 0 to 1.
        String percent = String.format(Locale.US, "%.1f%%", result.confidence * 100f);
        confidenceView.setText(getString(R.string.confidence_line, percent));
        Log.i(TAG, "label=" + result.label + " confidence=" + result.confidence);
        disposalView.setText(result.disposal);
        statusView.setText(R.string.status_classified);
    }

    private void showInferenceFailure(int thisRequest) {
        if (isDestroyed() || thisRequest != requestId) {
            return;
        }
        clearResult();
        statusView.setText(R.string.status_inference_failed);
    }

    private void showUnreadableImage() {
        // Drop a classification that is still running for an older picture.
        requestId++;
        selectedImage.setImageDrawable(null);
        clearResult();
        statusView.setText(R.string.status_unreadable);
    }

    private void clearResult() {
        categoryView.setText("");
        confidenceView.setText("");
        disposalView.setText("");
    }

    @Override
    protected void onDestroy() {
        // Drop any result that finishes after the screen is gone.
        requestId++;
        // Close the interpreter on the same thread that runs it, after any current classification.
        classificationExecutor.execute(() -> {
            if (classifier != null) {
                classifier.close();
            }
        });
        classificationExecutor.shutdown();
        super.onDestroy();
    }
}
