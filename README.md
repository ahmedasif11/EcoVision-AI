# EcoVision AI — Smart Waste Classifier

This Android Studio project contains two apps.

| Module | Package | What it does |
| --- | --- | --- |
| `:app` | `com.example.numberguessingtflite` | The original number-guessing sample. It loads `linear.tflite`, sends one typed float through a TensorFlow Lite interpreter, and shows the returned float. |
| `:ecovision` | `com.example.ecovision` | A separate waste-photo screen. It selects an image already on the device, runs the WasteNet model on the device, and shows a category, a confidence percentage, and a fixed disposal sentence. |

The Android Studio project name is `EcoVision AI`. Run the `:ecovision` module for the waste classifier. The `:app` module is still the original number-guessing sample. Its activity, layout, and `linear.tflite` were not rewritten. Unused Compose theme files, Kotlin example tests, and the unused color file were removed from that module.

## Build requirements

- JDK 17. Gradle 8.11.1 does not run on the Java 25 runtime that Android Studio can bundle. A sync with that runtime failed with `Unsupported class file major version 69`.
- Android SDK platform 35 and build-tools. `compileSdk` is 35. `minSdk` is 24.
- `ANDROID_HOME` pointed at the SDK. `local.properties` must set `sdk.dir`. That file is gitignored.

`./gradlew` is not marked executable. Call it as `bash ./gradlew`.

The builds recorded for this project used the extracted Gradle 8.11.1 binary and a separate Gradle home, so a second wrapper download was not started:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export GRADLE_USER_HOME="$HOME/.gradle-numberguess-phase1"
GRADLE_BIN="$HOME/.gradle/wrapper/dists/gradle-8.11.1-bin"
GRADLE_BIN="$GRADLE_BIN/bpt9gzteqjrbo1mjrsomdt32c/gradle-8.11.1/bin/gradle"
"$GRADLE_BIN" :ecovision:assembleDebug --no-daemon --console=plain
```

Replace the `gradle` path with `bash ./gradlew` if the wrapper distribution is not already unpacked on that machine. Use JDK 17 either way.

The original sample uses the same environment and the task `:app:assembleDebug`.

## Run EcoVision

The debug APK is `ecovision/build/outputs/apk/debug/ecovision-debug.apk`.

```bash
adb install -r ecovision/build/outputs/apk/debug/ecovision-debug.apk
adb shell am start -n com.example.ecovision/.MainActivity
```

In Android Studio, run the `ecovision` module. Running `:app` opens the number-guessing sample.

On the device:

1. Open EcoVision. The first screen says `Choose an image to classify it.`
2. Tap **Choose image** and pick a photo. The app does not ask for a camera permission.
3. The screen shows the photo, `Category: ...`, `Confidence: ...`, a disposal sentence, and `Classified on this device.`

A file that is not a picture shows `That file could not be read as an image.` and clears any previous category.

On an API 37 emulator, an earlier install showed a 16 KB page-size warning for `libtensorflowlite_jni.so`. The app continued after that dialog was dismissed. Inference still ran.

## Run the original sample

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open **Number Guessing Tflite**, type a number, and tap **Send**. The checked run typed `4`. The screen showed `6.9983535`. Screenshot: `docs/evidence/original-sample-input-4.png`.

## What was tested

Device: `emulator-5554`, `sdk_gphone16k_x86_64`, Android 17, API 37.

| Screenshot | Result |
| --- | --- |
| `docs/evidence/ecovision-phase5-first-screen.png` | First screen, no category |
| `docs/evidence/ecovision-phase5-metal-can.png` | Drink-can photo: `metal`, `100.0%` |
| `docs/evidence/ecovision-phase5-city.png` | City skyline, not a waste photo: `trash`, `31.8%` |
| `docs/evidence/ecovision-phase5-unreadable.png` | 16-byte file: unreadable-image message, no category |

The city result is the model's choice among its 12 waste labels. It does not mean the skyline is trash. Cancelling the picker, a missing model file, and an exception from `Interpreter.run` were not exercised.

The written reports, the assignment checklist, and `docs/NumberGuessingAppTflite-project.zip` stay on this machine. `.gitignore` keeps them out of this GitHub repository.

This repository is [EcoVision-AI](https://github.com/ahmedasif11/EcoVision-AI). The original sample is [NumberGuessingAppTflite](https://github.com/Uzair390-Del/NumberGuessingAppTflite).

## Credits

- Original sample: [NumberGuessingAppTflite](https://github.com/Uzair390-Del/NumberGuessingAppTflite) by Uzair390-Del, commit `447cfc9`. The `:app` module is that sample.
- Waste model: [wastenet-garbage-classifier](https://github.com/KrisnaSantosa15/wastenet-garbage-classifier) by Krisna Santosa, commit `8d793623ee43af5d4fd98b057163921e7060b693`. License: [MIT](https://github.com/KrisnaSantosa15/wastenet-garbage-classifier/blob/main/LICENSE), copyright 2025 Krisna Santosa. The files used here are `tflite/model.tflite` and `tflite/labels.txt`, kept in `models/wastenet/` and copied to `ecovision/src/main/assets/`.
- Dataset named by that WasteNet repository: [Garbage Classification](https://www.kaggle.com/datasets/mostafaabla/garbage-classification). The Kaggle page shows `Database: Open Database, Contents: © Original Authors`. The dataset images were not downloaded into this project.
- The WasteNet training script starts from Keras `MobileNetV2` with ImageNet weights. Those weights are not a separate file in this project.
- On-device runtime: `org.tensorflow:tensorflow-lite:2.17.0`. EcoVision pins that version. The sample depends on `org.tensorflow:tensorflow-lite:+`, which resolved to `2.17.0` in the recorded debug build.
- The non-waste test photo `ecovision-city.jpg` was downloaded from `https://picsum.photos/seed/cardboardbox/640/480`. It is a 640 by 480 city skyline used only as a negative example.

Checked shapes, licenses, and run results are written in `docs/TECHNICAL_NOTES.md`.
