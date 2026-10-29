# Implementation plan

Work in order. Finish one phase, report the result, and wait for approval before the next phase.

The original sample stays in the `:app` module. The new app will be a separate module named `:ecovision` if that structure is approved. Do not push to `https://github.com/Uzair390-Del/NumberGuessingAppTflite.git`. That remote belongs to the original author.

## Phase 0 — Inspect and plan

**Goal.** Understand the cloned sample and set up the planning files. Do not build the new app.

**Tasks.**

- Read the Java activity, XML layout, Gradle files, manifest, and `linear.tflite`.
- Check the local Android SDK, JDK, and whether a device is attached.
- Attempt a debug build only as evidence. Do not treat an unfinished build as success.
- Write the vision, this plan, the checklist, the status file, and the technical notes.
- Add one Cursor rule and four project skills.

**Files.** `docs/*.md`, `.cursor/rules/assignment-workflow.mdc`, `.cursor/skills/*/SKILL.md`. No application source.

**Completion criteria.** The planning files exist and describe only checked facts. The new app is not started.

**Verification.** Re-read the notes against `MainActivity.java`, `activity_main.xml`, and `app/build.gradle`.

**Assignment requirement.** Supports requirement 2 (understand the sample) and the project-management setup. Running the sample is still requirement 1 and is Phase 1.

## Phase 1 — Build and run the original sample

**Goal.** Prove whether the number-guessing app builds and runs. Record the real result.

**Tasks.**

- Use JDK 17 and `ANDROID_HOME=$HOME/Android/Sdk`. The default `java` on this machine is Java 27, which is the wrong runtime for this Android Gradle Plugin.
- Run `bash ./gradlew :app:assembleDebug --no-daemon`. `gradlew` is not executable.
- If the build fails, stop. Report the error. Do not edit the sample until the fix is approved.
- If the APK builds, install it on the attached emulator and enter a numeric value. Record the displayed number.
- Do not call that number a verified model formula. The app was not interpreted outside Android during inspection.

**Files expected to change.** None if the build succeeds. `local.properties` may be created locally and is gitignored. A source fix is a separate approval.

**Completion criteria.** Either a debug APK installs and a real numeric result is visible, or the exact build/run failure is recorded.

**Verification.** `bash ./gradlew :app:assembleDebug --no-daemon`, then install and launch on `emulator-5554` if it is still connected. Save a real screenshot only after the screen is visible.

**Assignment requirement.** Requirement 1. Requirement 2 stays incomplete until the written explanation matches this run.

## Phase 2 — Verify the image model

**Goal.** Choose a real `.tflite` file only after its contract is known. Do not write the classifier yet.

**Tasks.**

- First candidate: a small public waste-classification `.tflite` model with a visible license and a labels file. A 12-class MobileNetV2 project named WasteNet is the first place to look. This inspection did not confirm that a `.tflite` file is in that repository.
- From the file itself, record source URL, license, file size, input shape, input type, preprocessing, output shape, output type, and label order.
- If any of those facts cannot be established quickly, stop and ask for approval to switch to the fallback.
- Fallback: the official TensorFlow Lite MobileNet image-classification model used by TensorFlow's Android image-classification example. It is general object classification, not a waste classifier. The app title and report must say that. Do not map its labels into fake waste categories.
- Do not train a model.

**Files expected to change.** `docs/TECHNICAL_NOTES.md`, `docs/PROJECT_STATUS.md`, and `docs/ASSIGNMENT_CHECKLIST.md`. The model file is copied into the new module only after the contract is accepted.

**Completion criteria.** A model file is on disk and the notes contain the checked contract, or the phase stops with a documented reason to change approach.

**Verification.** Inspect the model with a TensorFlow Lite interpreter or another schema reader. Quote shapes from that tool output.

**Assignment requirement.** Requirements 3 and 7.

## Phase 3 — Add the EcoVision screen

**Goal.** Add a separate Java/XML app module that can select an image and show messages. It must not invent a prediction.

**Tasks.**

- Add `:ecovision` in `settings.gradle`.
- Create its manifest, one layout, strings, and `MainActivity`.
- Use `ActivityResultContracts.GetContent` with type `image/*`.
- Show the selected image and a status message.
- Pin a specific `org.tensorflow:tensorflow-lite` version. Do not copy the original `+` version.
- Leave the original `:app` module unchanged.

**Files expected to change.** `settings.gradle`, `ecovision/build.gradle`, `ecovision/src/main/AndroidManifest.xml`, `ecovision/src/main/java/.../MainActivity.java`, `ecovision/src/main/res/layout/activity_main.xml`, `ecovision/src/main/res/values/strings.xml`, and the new module's theme resources.

**Completion criteria.** The new module builds. The user can select an image and see it. No category is shown yet.

**Verification.** `bash ./gradlew :ecovision:assembleDebug --no-daemon`, then open the new app on the emulator and select an image.

**Assignment requirement.** Requirement 3, partially.

## Phase 4 — Run real on-device inference

**Goal.** Preprocess the selected image, run the verified model, and display its result.

**Tasks.**

- Load the model from assets with a mapped buffer, using the same idea as the sample.
- Resize and convert pixels to the verified input type and layout. Match RGB order and normalization to the model notes. Do not assume `/255` or `/127.5` until Phase 2 recorded it.
- Run `Interpreter.run`.
- Show the top category. Show confidence only when the output meaning is known.
- Add one short disposal sentence per verified label.
- Close the interpreter when the activity is destroyed.
- Catch unreadable images, a missing model, and interpreter failures, and show a message.
- Comment the loading, preprocessing, inference, and result code so it can be explained in class.

**Files expected to change.** The EcoVision activity, a small helper if the activity becomes hard to read, `assets/` model and labels, and `docs/TECHNICAL_NOTES.md`.

**Completion criteria.** A selected photo produces a label from the model. A bad file shows an error instead of a crash. No label is hardcoded as the prediction.

**Verification.** Build `:ecovision:assembleDebug`, install it, and classify at least two real images plus one invalid selection.

**Assignment requirement.** Requirements 3 and 4.

## Phase 5 — Test and collect evidence

**Goal.** Collect real screenshots and note any failure.

**Tasks.**

- On the running app, capture: the first screen, a successful classification, the confidence line when it exists, the disposal line, and one error message.
- Store images under `docs/evidence/`.
- Write the device, Android version, and the command used.

**Files expected to change.** `docs/evidence/*`, `docs/PROJECT_STATUS.md`, `docs/ASSIGNMENT_CHECKLIST.md`.

**Completion criteria.** Every screenshot is a file captured from the running app during this phase.

**Verification.** Open each image and confirm it matches the described screen.

**Assignment requirement.** Requirement 5.

## Phase 6 — Reports, README, and submission package

**Goal.** Prepare the written submission from the verified notes and evidence.

**Tasks.**

- Theory report PDF: what on-device inference is, and how the sample loads a model, passes input, reads output, and shows the result.
- Practical report PDF: how EcoVision was built, how to run it, and what was tested.
- Code-snippets PDF: short excerpts with explanations. Do not paste whole files.
- README with run steps, the original repository acknowledgement, and model, library, and dataset credits.
- ZIP of the Android Studio project, excluding `.gradle`, `build`, and `local.properties`.
- Create the student's own GitHub repository only when asked. Do not push to the original remote.

**Files expected to change.** `README.md`, `docs/reports/*`, the ZIP, and the checklist.

**Completion criteria.** Each deliverable exists and its claims match `docs/TECHNICAL_NOTES.md` and `docs/evidence/`.

**Verification.** Read the reports against the notes. Open the ZIP and confirm the project still contains both modules and the model.

**Assignment requirement.** Requirements 2, 6, and 7.
