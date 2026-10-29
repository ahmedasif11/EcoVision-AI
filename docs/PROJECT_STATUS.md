# Project status

**Current phase:** Phase 6 is done. The code remote is `https://github.com/ahmedasif11/EcoVision-AI`. Reports, the assignment checklist, and the project ZIP stay local through `.gitignore`.

**Waiting for:** Nothing further on the repository. Do not push to `https://github.com/Uzair390-Del/NumberGuessingAppTflite.git`.

## Completed work

- Cloned repository inspected on 2026-10-29.
- Original workflow read from source and summarized in `docs/TECHNICAL_NOTES.md`.
- Planning files, one Cursor rule, and four skills created.
- Original app debug APK built with Gradle 8.11.1 and JDK 17.
- APK installed on `emulator-5554`. Input `4` displayed `6.9983535`.
- Screenshot: `docs/evidence/original-sample-input-4.png`.
- No application source or Gradle dependency was changed. `local.properties` was added and is gitignored.
- Phase 2 downloaded WasteNet `tflite/model.tflite` and `tflite/labels.txt` from commit `8d793623ee43af5d4fd98b057163921e7060b693` into `models/wastenet/`.
- The model file is 20,675,188 bytes. A schema reader reported input `[1, 224, 224, 3]` float32 and output `[1, 12]` float32. The last operator is `SOFTMAX`. The contract is in `docs/TECHNICAL_NOTES.md`.
- The first download stopped at 3,899,374 bytes. The resumed download reached the server length.
- No EcoVision module, screen, or inference code was added during Phase 2. The model was not run on a photo.
- Phase 3 added the `:ecovision` module. `org.tensorflow:tensorflow-lite:2.17.0` is pinned. The activity uses `ActivityResultContracts.GetContent` with `image/*`. It does not load the model or show a category.
- `:ecovision:assembleDebug` succeeded. The APK was installed on `emulator-5554`. After dismissing the 16 KB dialog, the screen showed **Choose image** and `Choose an image. No category is shown yet.`
- A 160×160 PNG was pushed to `/sdcard/Pictures/ecovision-phase3.png` and chosen in the system photo picker. The status line changed to `Image selected. No category is shown yet.` The picture on screen is that green PNG. Screenshot: `docs/evidence/ecovision-phase3-image-selected.png`.
- During Phase 3, the cancel and unreadable-file messages were not exercised. Phase 4 later exercised the unreadable file. Cancel is still untested.
- The `:app` module was not edited. `settings.gradle` only gained `include ':ecovision'`.
- Phase 4 copied `models/wastenet/model.tflite` and `labels.txt` into `ecovision/src/main/assets/`. Both SHA-256 hashes match the Phase 2 files. Inside the debug APK, `assets/model.tflite` is stored uncompressed and is 20,675,188 bytes.
- The activity maps that asset into an `Interpreter`, resizes the selected bitmap to 224×224, divides the RGB channels by 255, and shows the `labels.txt` line for the largest of the 12 softmax scores. A fixed sentence is then chosen from that label. The interpreter is closed when the activity is destroyed.
- `:ecovision:assembleDebug` succeeded twice: `BUILD SUCCESSFUL in 12s`, then `BUILD SUCCESSFUL in 9s` after the unreadable-image path was adjusted. The final APK was installed on `emulator-5554` (`sdk_gphone16k_x86_64`, API 37, Android 17). The 16 KB dialog did not appear on this install. The first screen said `Choose an image to classify it.`
- `ecovision-metal-can.jpg` is the WasteNet example `metal118.jpg`, a photo of a drink can. The screen showed `Category: metal`, `Confidence: 100.0%`, and `Rinse the metal and put it in the metal recycling bin.` Logcat recorded `label=metal confidence=0.9999827`. Screenshot: `docs/evidence/ecovision-phase4-metal-can.png`.
- `ecovision-city.jpg` is a 640×480 city skyline downloaded from `https://picsum.photos/seed/cardboardbox/640/480`. It is not a waste photo. The screen showed `Category: trash`, `Confidence: 31.8%`, and `Put this in the general trash if local recycling does not accept it.` Logcat recorded `label=trash confidence=0.31802413`. Screenshot: `docs/evidence/ecovision-phase4-city.png`.
- `ecovision-not-an-image.jpg` is 16 bytes and is not a decodable picture. The picture and the previous category were cleared. The status said `That file could not be read as an image.` The app stayed open. Screenshot: `docs/evidence/ecovision-phase4-unreadable.png`.
- Cancelling the picker, a missing model file, and an exception thrown by `Interpreter.run` were not exercised on the emulator.
- Phase 5 did not rebuild or edit application source. The installed debug APK matches `ecovision/build/outputs/apk/debug/ecovision-debug.apk`. Both SHA-256 hashes are `e4791cf3de7d0bd9a2723d4b3a0e562df38a9db45aa1fef6d947313d92c8f40d`.
- On `emulator-5554` (`sdk_gphone16k_x86_64`, API 37, Android 17), a fresh launch showed the title `EcoVision`, the button `CHOOSE IMAGE`, and `Choose an image to classify it.` The 16 KB dialog did not appear. Screenshot: `docs/evidence/ecovision-phase5-first-screen.png`.
- The same launch then classified the pictures already on the device. `ecovision-metal-can.jpg` showed `Category: metal`, `Confidence: 100.0%`, and `Rinse the metal and put it in the metal recycling bin.` Logcat recorded `label=metal confidence=0.9999827`. Screenshot: `docs/evidence/ecovision-phase5-metal-can.png`.
- `ecovision-city.jpg` showed `Category: trash`, `Confidence: 31.8%`, and `Put this in the general trash if local recycling does not accept it.` Logcat recorded `label=trash confidence=0.31802413`. Screenshot: `docs/evidence/ecovision-phase5-city.png`.
- `ecovision-not-an-image.jpg` cleared the picture and the previous category. The status said `That file could not be read as an image.` MediaProvider opened that file, and there was no EcoVision inference log. Screenshot: `docs/evidence/ecovision-phase5-unreadable.png`.
- Each Phase 5 screenshot was opened and matches the screen text above. The Phase 4 screenshots remain in `docs/evidence/`. Cancelling the picker, a missing model, and an `Interpreter.run` exception are still untested.
- Phase 6 wrote `README.md`, `docs/reports/theory-report.pdf`, `docs/reports/practical-report.pdf`, and `docs/reports/code-explanation.pdf` from `docs/TECHNICAL_NOTES.md` and the Java sources. The markdown files next to the PDFs are the source text. No application source was edited during Phase 6.
- After Phase 6, unused Compose theme files, Kotlin example tests, and the unused `colors.xml` were removed from `:app`. Compose and the Kotlin plugins were removed from Gradle. `app/build.gradle` now lists each library once and sets `noCompress 'tflite'`. `MainActivity.java`, `activity_main.xml`, and `linear.tflite` were not rewritten. The root project name is `EcoVision AI`. Inference was not run again for this cleanup.
- `docs/NumberGuessingAppTflite-project.zip` was created after those files. `unzip -t` reported no errors. The archive contains `:app`, `:ecovision`, `models/wastenet/model.tflite`, and `ecovision/src/main/assets/model.tflite`. Both model entries are 20,675,188 bytes. `linear.tflite` in the archive is 660 bytes. It does not contain `local.properties`, a `build/` directory, `.gradle/`, or `.git/`.

## Decisions

| Decision | Reason | Approval |
| --- | --- | --- |
| Keep Java and XML | The sample activity and layout already use them. Unused Compose files, Kotlin tests, and the Kotlin plugin were removed after Phase 6. | Approved by the cleanup request |
| Add a new `:ecovision` module and leave `:app` unchanged | The assignment asks for a separate app and a clear distinction from the sample. | Approved with Phase 3. `:app` was not edited. |
| Select an image with `GetContent` and `image/*` | One gallery action. No camera permission. | Approved with Phase 3. Verified on `emulator-5554`. |
| Verify a waste-classification `.tflite` file before writing inference | The bundled `linear.tflite` accepts one number. It cannot classify images. | Approved with Phase 2. WasteNet was verified. |
| If that file cannot be verified quickly, switch to official MobileNet and say so in the app | Training a model, or guessing labels, would waste time or misrepresent the result. | Not used. The WasteNet contract was established. |
| Do not push to the original GitHub remote | `origin` points at the author's repository. | Proposed |

## Known issues

- `./gradlew` is mode `rw-rw-r--`. Use `bash ./gradlew`.
- Android Studio’s bundled Java is 25. Gradle 8.11.1 failed in that Studio sync with `Unsupported class file major version 69`. Command-line builds must keep `JAVA_HOME` on JDK 17.
- The default Gradle home was busy with that Studio daemon. The successful build used the already extracted Gradle 8.11.1 binary and `GRADLE_USER_HOME=$HOME/.gradle-numberguess-phase1`.
- The original `app/build.gradle` enabled Compose and used `noCompress; "tflite"`, which did not register an extension. After the cleanup, `:app:assembleDebug` and `:ecovision:assembleDebug` finished with `BUILD SUCCESSFUL in 42s`. In the new sample APK, `assets/linear.tflite` is stored uncompressed and is 660 bytes. The sample activity was not launched again.
- `org.tensorflow:tensorflow-lite:+` resolved to `2.17.0`, which pulled in `com.google.ai.edge.litert:litert:1.0.1`.
- On this API 37 emulator, Android showed a 16 KB page-size warning for `libtensorflowlite_jni.so` during earlier installs. The app continued after the dialog was dismissed and inference ran. A Phase 5 relaunch of the already installed EcoVision APK did not show that dialog.
- The sample still has no handling for non-numeric input, and it does not close the interpreter.
- Before Phase 3, the emulator had a separate installed app with package `com.example.ecovision`. That install was replaced by this module. EcoVision screenshots must show the title `EcoVision` and the status text from this module.
- `ai-edge-litert` did not install on Python 3.14. Pip reported `ResolutionImpossible` for `numpy>=1.23.2`. Phase 2 used the TensorFlow Lite FlatBuffer schema instead of a Python interpreter. Phase 4 ran the model on the emulator.
- The WasteNet model always returns one of its 12 labels. The city photograph was still given a category, `trash`, at about 31.8% confidence.
- `models/wastenet/` remains the checked copy. `ecovision/src/main/assets/` holds the same two files.
- `adb install -r` of the EcoVision debug APK replaced another installed app with package `com.example.ecovision`. Before replacement, `dumpsys` showed `versionName=1.0`, `minSdk=24`, `targetSdk=35`, and `lastUpdateTime=2026-10-29 19:38:19`. That APK was not saved. It was not part of this repository.

## Next exact action

The code remote is `https://github.com/ahmedasif11/EcoVision-AI`. Do not push to `https://github.com/Uzair390-Del/NumberGuessingAppTflite.git`.

The written submission stays local: the three PDFs in `docs/reports/`, `docs/ASSIGNMENT_CHECKLIST.md`, and `docs/NumberGuessingAppTflite-project.zip`.

Resume command for another EcoVision debug build:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export GRADLE_USER_HOME="$HOME/.gradle-numberguess-phase1"
"$HOME/.gradle/wrapper/dists/gradle-8.11.1-bin/bpt9gzteqjrbo1mjrsomdt32c/gradle-8.11.1/bin/gradle" :ecovision:assembleDebug --no-daemon --console=plain
```

Phase 5 did not run Gradle. During an earlier EcoVision build, `bash ./gradlew` was started once. It began downloading Gradle 8.11.1 into `GRADLE_USER_HOME` and was stopped. The successful build used the extracted Gradle 8.11.1 binary above.

Resume command for another original-app build:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export GRADLE_USER_HOME="$HOME/.gradle-numberguess-phase1"
"$HOME/.gradle/wrapper/dists/gradle-8.11.1-bin/bpt9gzteqjrbo1mjrsomdt32c/gradle-8.11.1/bin/gradle" :app:assembleDebug --no-daemon --console=plain
```

## Commands used during inspection

```bash
git status -sb
git remote -v
git log -5 --oneline
java -version
ls "$HOME/Android/Sdk/platforms"
"$HOME/Android/Sdk/platform-tools/adb" devices
```

The Phase 1 build later finished. Its result is recorded in the completed-work list.
