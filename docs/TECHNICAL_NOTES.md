# Technical notes

Checked on 2026-10-29. The original app was built and run once on the emulator.

## Original repository

- URL: `https://github.com/Uzair390-Del/NumberGuessingAppTflite`
- Remote name: `origin`
- Branch: `master`, tracking `origin/master`
- History seen locally: `447cfc9` `Initial commit`
- There is no README in the clone.
- Root project name in `settings.gradle`: `EcoVision AI`
- Single module: `:app`
- Namespace and application id: `com.example.numberguessingtflite`
- Screen title string: `Number Guessing Tflite`

## Language and UI

The running screen is Java plus an XML layout.

- Activity: `app/src/main/java/com/example/numberguessingtflite/MainActivity.java`
- Layout: `app/src/main/res/layout/activity_main.xml`
- Theme parent: `Theme.AppCompat.Light.DarkActionBar`
- The layout is a `ConstraintLayout` with an `EditText` (`editText`), a `Button` (`button`, label `Send`), and a `TextView` (`textView`).

The clone included unused Compose theme files (`Theme.kt`, `Color.kt`, `Type.kt`), unused Kotlin example tests, and a `colors.xml` that the layout did not reference. Those files were removed on 2026-10-29 so the project is Java and XML. `MainActivity.java`, `activity_main.xml`, and `linear.tflite` were not rewritten. The root `build.gradle` applies only the Android application plugin.

## Android and Gradle versions

From the project files:

| Item | Value |
| --- | --- |
| Android Gradle Plugin | 8.9.1 |
| Gradle wrapper | 8.11.1 |
| compileSdk / targetSdk | 35 |
| minSdk | 24 |
| Java source and target | 11 |
| TensorFlow Lite dependency | `org.tensorflow:tensorflow-lite:+` |

The original `app/build.gradle` enabled Compose, listed AppCompat, Material, Activity, and ConstraintLayout twice, and wrote `noCompress; "tflite"`. The semicolon ended the statement before the extension string. That file was cleaned on 2026-10-29. Compose is off, each library is listed once, and `androidResources { noCompress 'tflite' }` is set. The sample activity still depends on `org.tensorflow:tensorflow-lite:+`.

## Machine facts

- SDK directory: `/home/ahmed-asif/Android/Sdk`
- Installed platforms include `android-35`, which matches `compileSdk 35`
- Installed build-tools include `35.0.0`
- `local.properties` was absent at the first inspection. Phase 1 created it. It is gitignored and points `sdk.dir` at `/home/ahmed-asif/Android/Sdk`.
- `ANDROID_HOME` was unset in the inspection shell
- JDKs present: OpenJDK 17 and Oracle JDK 27. `java -version` printed Java 27
- `./gradlew` is not executable (`-rw-rw-r--`)
- `adb devices` showed `emulator-5554`. Its API level was 37 and its release string was `17`. The sample was not installed.
- Python 3.14 is installed. The `tensorflow` module is not installed, so the model was not executed in Python.

## Model file

- Path: `app/src/main/assets/linear.tflite`
- Size: 660 bytes
- Header: FlatBuffer identifier `TFL3`
- Text inside the file: `TOCO Converted.`
- Tensor names found as strings:
  - `dense_1_input`
  - `sequential_1/dense_1/MatMul/ReadVariableOp/transpose`
  - `sequential_1/dense_1/MatMul_bias`
  - `Identity`

Those names describe one Dense layer: an input, a weight, a bias, and an output. They do not describe an image tensor.

Aligned float values read from the file bytes included approximately `-0.994326`, `1.998170`, and `255.0`. Their roles were not confirmed by an interpreter. Do not describe them as the regression formula.

## Build and run evidence

- Command used Gradle 8.11.1 from the extracted wrapper distribution, JDK 17, and `GRADLE_USER_HOME=$HOME/.gradle-numberguess-phase1`.
- `local.properties` points `sdk.dir` at `/home/ahmed-asif/Android/Sdk`.
- `org.tensorflow:tensorflow-lite:+` resolved to `2.17.0` and downloaded `com.google.ai.edge.litert:litert:1.0.1`.
- Output APK: `app/build/outputs/apk/debug/app-debug.apk`.
- `assets/linear.tflite` is stored in that APK, not compressed. Size in the APK is 660 bytes.
- Installed on `emulator-5554` with `adb install -r`.
- The emulator showed a 16 KB compatibility dialog naming `lib/x86_64/libtensorflowlite_jni.so`. After dismissal, the activity stayed open.
- UI dump after typing `4` and tapping Send:
  - `editText` text: `4`
  - `textView` text: `6.9983535`
- Screenshot of that screen: `docs/evidence/original-sample-input-4.png`.
- For this single input, `1.998170 * 4 + (-0.994326)` is about `6.998354`, which matches the displayed value within float rounding. Those two constants were previously read from the model file. This one run does not by itself document the full tensor metadata.

## How the sample is written to run

The source behavior below matches the run above.

1. **Load.** `loadModelFile()` opens asset `linear.tflite`, maps it read-only, and passes that buffer to `new Interpreter(...)`. A missing asset becomes a `RuntimeException` during startup.
2. **Input.** The button reads `editText`, parses it with `Float.parseFloat`, and stores it in `float[1]`. The field's `inputType` is `text`. Empty or non-numeric text is not handled and can throw `NumberFormatException`.
3. **Output.** The code allocates `float[1][1]`.
4. **Inference.** `interpreter.run(input, output)` runs on the calling thread.
5. **Display.** `textView` is set to `String.valueOf(output[0][0])`.

The view lookup and click listener are inside the window-insets listener. The interpreter is never closed.

The button path cannot accept an image. This model is not a waste classifier and must not be reused for EcoVision inference.

## EcoVision screen added in Phase 3

Checked on 2026-10-29. Phase 3 added image selection. Classification was added in the next section.

- Module `:ecovision`, namespace and application id `com.example.ecovision`.
- Activity: `ecovision/src/main/java/com/example/ecovision/MainActivity.java`.
- Layout: `ecovision/src/main/res/layout/activity_main.xml`.
- Theme parent: `Theme.AppCompat.Light.DarkActionBar`, with `android:windowOptOutEdgeToEdgeEnforcement` so target SDK 35 does not draw the button under the action bar.
- Image choice: `ActivityResultContracts.GetContent` and MIME type `image/*`. No camera permission.
- The selected bitmap is decoded with `BitmapFactory` in `ARGB_8888` and shown in `selectedImage`. The status text is one of the strings in `strings.xml`.
- `org.tensorflow:tensorflow-lite:2.17.0` is pinned in `ecovision/build.gradle`.
- `androidResources { noCompress 'tflite' }` is set. `assets/model.tflite` and `assets/labels.txt` are copies of `models/wastenet/`. Their SHA-256 hashes match the Phase 2 files.
- In the debug APK, `assets/model.tflite` has zip compress type 0 (stored) and size 20,675,188. `assets/labels.txt` is compressed. Labels are read with `AssetManager.open`, so that compression is expected.
- Build command that succeeded: extracted Gradle 8.11.1, JDK 17, `GRADLE_USER_HOME=$HOME/.gradle-numberguess-phase1`, task `:ecovision:assembleDebug`. Result: `BUILD SUCCESSFUL in 46s`, then a later rebuild `BUILD SUCCESSFUL in 11s`.
- Installed with `adb install -r` on `emulator-5554`. The same 16 KB page-size dialog as the sample appeared for `libtensorflowlite_jni.so`. It was dismissed.
- Phase 3 UI dump before a choice: button `CHOOSE IMAGE`, status `Choose an image. No category is shown yet.`
- After choosing `/sdcard/Pictures/ecovision-phase3.png` in the photo picker during Phase 3: status `Image selected. No category is shown yet.` Screenshot: `docs/evidence/ecovision-phase3-image-selected.png`.
- Installing this package replaced a different app already on the emulator. See `docs/PROJECT_STATUS.md`.

## On-device inference added in Phase 4

Checked on 2026-10-29 against the final debug APK on `emulator-5554`.

- `WasteClassifier` maps `model.tflite` read-only with `openFd` and passes that buffer to `new Interpreter`. No GPU or NNAPI delegate is added.
- At load, the code checks float32 input `[1 or -1, 224, 224, 3]`, float32 output `[1 or -1, 12]`, and 12 label lines. A failed load leaves the screen open with `The waste model could not be loaded.` That failure was not provoked on the emulator because the asset loaded.
- Preprocessing stretches the bitmap to 224×224, reads Android ARGB pixels as red, green, then blue, and divides each channel by 255. This follows the Phase 2 contract.
- `Interpreter.run` uses `float[1][224][224][3]` in and `float[1][12]` out. The shown category is `labels.get(bestIndex)`, where `bestIndex` is the largest score. Equal scores keep the lower index.
- The score is a softmax probability. The screen formats it with `Locale.US` and one decimal percent. `0.9999827` is displayed as `100.0%`.
- The disposal sentence is a `switch` on the label string. It is not a model output.
- Classification runs on one background thread. `onDestroy` closes the interpreter on that same thread. Choosing an unreadable file increments the request id so an older result cannot replace the error.
- Final-APK results on `emulator-5554`, API 37, Android 17, device `sdk_gphone16k_x86_64`:

| File | What it is | Screen | Logcat confidence |
| --- | --- | --- | --- |
| `/sdcard/Pictures/ecovision-metal-can.jpg` | WasteNet `metal118.jpg`, a drink-can photo | `Category: metal`, `Confidence: 100.0%`, metal disposal sentence | `0.9999827` |
| `/sdcard/Pictures/ecovision-city.jpg` | 640×480 city skyline from `picsum.photos` | `Category: trash`, `Confidence: 31.8%`, trash disposal sentence | `0.31802413` |
| `/sdcard/Pictures/ecovision-not-an-image.jpg` | 16 bytes, not a decodable image | `That file could not be read as an image.` No category. | No inference log |

Screenshots: `docs/evidence/ecovision-phase4-metal-can.png`, `docs/evidence/ecovision-phase4-city.png`, and `docs/evidence/ecovision-phase4-unreadable.png`.

The city result is the model's forced choice among the 12 waste labels. It is not evidence that the photograph shows trash. Cancelling the picker and an exception from `Interpreter.run` were not exercised.

## Evidence captured in Phase 5

Checked on 2026-10-29. No application source was edited and no new APK was built. The package already installed on the emulator is the Phase 4 debug APK.

| Item | Value |
| --- | --- |
| Device | `emulator-5554`, `sdk_gphone16k_x86_64` |
| Android version | 17 |
| API level | 37 |
| Package | `com.example.ecovision` |
| Installed APK SHA-256 | `e4791cf3de7d0bd9a2723d4b3a0e562df38a9db45aa1fef6d947313d92c8f40d` |
| Local APK | `ecovision/build/outputs/apk/debug/ecovision-debug.apk`, same SHA-256 |

Launch command:

```bash
"$HOME/Android/Sdk/platform-tools/adb" -s emulator-5554 shell am force-stop com.example.ecovision
"$HOME/Android/Sdk/platform-tools/adb" -s emulator-5554 shell am start -n com.example.ecovision/.MainActivity
```

Screenshots were taken with `adb exec-out screencap -p` while that activity was in front. The 16 KB page-size dialog did not appear.

| Screenshot | File opened by the picker | Screen |
| --- | --- | --- |
| `docs/evidence/ecovision-phase5-first-screen.png` | None | Title `EcoVision`, button `CHOOSE IMAGE`, status `Choose an image to classify it.` |
| `docs/evidence/ecovision-phase5-metal-can.png` | `/sdcard/Pictures/ecovision-metal-can.jpg` | `Category: metal`, `Confidence: 100.0%`, metal disposal sentence. Logcat: `label=metal confidence=0.9999827` |
| `docs/evidence/ecovision-phase5-city.png` | `/sdcard/Pictures/ecovision-city.jpg` | `Category: trash`, `Confidence: 31.8%`, trash disposal sentence. Logcat: `label=trash confidence=0.31802413` |
| `docs/evidence/ecovision-phase5-unreadable.png` | `/sdcard/Pictures/ecovision-not-an-image.jpg` | Picture and category cleared. Status: `That file could not be read as an image.` MediaProvider opened the file. No EcoVision inference log. |

The Phase 4 copies of the classification and error screens remain in `docs/evidence/`. Cancelling the picker, a missing model, and an exception from `Interpreter.run` were still not exercised.

## Image model checked in Phase 2

Checked on 2026-10-29 from the downloaded file. Phase 4 later ran this same file on the emulator. The runs are recorded in the EcoVision section above.

### Source and license

- Repository: `https://github.com/KrisnaSantosa15/wastenet-garbage-classifier`
- Commit shown on the repository page: `8d793623ee43af5d4fd98b057163921e7060b693`
- Downloaded files:
  - `tflite/model.tflite` saved as `models/wastenet/model.tflite`
  - `tflite/labels.txt` saved as `models/wastenet/labels.txt`
- Project license: MIT, copyright 2025 Krisna Santosa. The license file is at `https://github.com/KrisnaSantosa15/wastenet-garbage-classifier/blob/main/LICENSE`.
- Dataset named by that repository: `https://www.kaggle.com/datasets/mostafaabla/garbage-classification`
- Dataset license shown on the Kaggle page: `Database: Open Database, Contents: © Original Authors`
- The training script loads Keras `MobileNetV2` with `weights='imagenet'`. Those base weights are not a separate file in this project.
- The dataset images were not downloaded.

The official MobileNet fallback was not used.

### File facts

| Item | Value |
| --- | --- |
| Size | 20,675,188 bytes |
| SHA-256 | `3c4c9d6c480f2c1f3a4aedd2e23fd89a5bf7c0fa5cf2993af1d8e01599f12433` |
| FlatBuffer identifier | `TFL3` |
| Description inside the file | `MLIR Converted.` |
| Metadata `min_runtime_version` | `1.6.0` |
| Signature key | `serving_default` |
| Signature input | `input_layer_3`, tensor 0 |
| Signature output | `output_0`, tensor 178 |
| `labels.txt` SHA-256 | `0fc725398ea9a773d32dcf93ddab374a2f2a787cc9a21a2258cb2a351d521a36` |

The first download stopped at 3,899,374 bytes. A resumed download finished at the server length, 20,675,188 bytes.

### How the tensors were read

`ai-edge-litert` did not install in a Python 3.14 virtual environment. Pip stopped with `ResolutionImpossible` while looking for `numpy>=1.23.2`. The shapes below come from the FlatBuffers schema `tensorflow/lite/schema/schema.fbs` at TensorFlow tag `v2.16.1`, read with the `flatbuffers` Python package. In that schema, tensor type `0` is `FLOAT32` and builtin operator `25` is `SOFTMAX`. The schema says a builtin-op shape is batch, height, width, then channels.

Quoted reader output:

```text
tensor 0 name 'serving_default_input_layer_3:0' shape [1, 224, 224, 3] shape_signature [-1, 224, 224, 3] type 0 buffer 1 quant_scale_count 0 quant_zero_count 0
tensor 178 name 'StatefulPartitionedCall_1:0' shape [1, 12] shape_signature [-1, 12] type 0 buffer 179 quant_scale_count 0 quant_zero_count 0
op 0 CONV_2D inputs [0, 109, 110] outputs [111]
op 67 SOFTMAX inputs [177] outputs [178]
last_ops ['CONV_2D', 'CONV_2D', 'CONV_2D', 'MAX_POOL_2D', 'MEAN', 'RESHAPE', 'FULLY_CONNECTED', 'SOFTMAX']
```

### Contract for later Android code

- Input: float32, NHWC, 224×224, 3 channels. Use a batch of 1. The shape signature allows a variable batch, and the stored shape is `[1, 224, 224, 3]`.
- The input tensor has no quantization scale or zero point.
- The first operator is `CONV_2D` and it reads the input tensor. Pixel scaling is outside the graph.
- Preprocessing comes from `garbage-classification.py` in the same repository, which writes `tflite/model.tflite`. Training and validation use `rescale=1./255`. The inference helper decodes 3 channels, resizes to `(224, 224)`, divides by `255.0`, and adds a batch dimension. The script does not call Keras `preprocess_input`, so the values stay in the range 0 to 1.
- The script uses TensorFlow image decoding and Keras directory loading. It does not swap red and blue.
- Output: float32 `[1, 12]`, with no quantization parameters.
- The last operator is `SOFTMAX`, so the 12 numbers are class probabilities.
- The model does not store the class names. The index is the line number in `labels.txt`, starting at 0:

| Index | Label |
| --- | --- |
| 0 | battery |
| 1 | biological |
| 2 | brown-glass |
| 3 | cardboard |
| 4 | clothes |
| 5 | green-glass |
| 6 | metal |
| 7 | paper |
| 8 | plastic |
| 9 | shoes |
| 10 | trash |
| 11 | white-glass |

The training script writes this same order to `tflite/labels.txt`.

`models/wastenet/` remains the checked copy. Phase 4 also placed the same two files in `ecovision/src/main/assets/`. They are not in `:app`.

## Submission files written in Phase 6

Checked on 2026-10-29. No application source was edited.

| File | Role |
| --- | --- |
| `docs/reports/theory-report.md` and `theory-report.pdf` | On-device inference, then how the sample loads `linear.tflite`, passes one float, reads one float, and shows it |
| `docs/reports/practical-report.md` and `practical-report.pdf` | How EcoVision was built, the checked model contract, how to run it, and the tests that were run |
| `docs/reports/code-explanation.md` and `code-explanation.pdf` | Short excerpts of loading, preprocessing, `Interpreter.run`, display, and closing the interpreter |
| `README.md` | Run steps for `:app` and `:ecovision`, plus repository, model, dataset, and library credits |
| `docs/reports/build_pdfs.py` | Formats the three markdown reports into the PDFs |
| `docs/NumberGuessingAppTflite-project.zip` | Android Studio project archive |

The practical report quotes the metal and trash disposal sentences that were seen on the emulator. The other ten sentences are copied from `WasteClassifier.disposalFor`. The report says they were not each displayed during testing.

The ZIP excludes `.gradle/`, every `build/` directory, `local.properties`, and `.git/`. After the archive was built, `unzip -t` reported no errors. The listing contains `app/src/main/assets/linear.tflite` at 660 bytes, `models/wastenet/model.tflite` at 20,675,188 bytes, and `ecovision/src/main/assets/model.tflite` at 20,675,188 bytes. A search of the listing found no `local.properties`, `build/`, `.gradle/`, or `.git/` entry.

The student's repository is `https://github.com/ahmedasif11/EcoVision-AI`. `docs/reports/`, `docs/ASSIGNMENT_CHECKLIST.md`, and `docs/NumberGuessingAppTflite-project.zip` are listed in `.gitignore` and are not pushed. The original sample remote is `https://github.com/Uzair390-Del/NumberGuessingAppTflite.git`.
