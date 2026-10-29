# EcoVision AI — project vision

Working title: **EcoVision AI — Smart Waste Classifier**.

This is a small BSCS Android assignment. The goal is a working on-device demo that a student can explain, not a production system.

## Problem

People often do not know which bin a waste item belongs in. A phone can help if it looks at a photo and suggests a category without sending the photo to a server.

## Intended users

- The student, who must demonstrate and explain the app to a teacher.
- A person who already has a photo of one waste item and wants a simple category and disposal hint.

## Purpose

Show genuine on-device inference with TensorFlow Lite:

1. The user selects an image stored on the device.
2. The app prepares that image for a compatible `.tflite` model.
3. The TensorFlow Lite interpreter runs locally.
4. The screen shows the predicted category and, when the model output supports it, a confidence score.
5. A short rule-based disposal sentence may be shown for that category.

## Objectives

- Keep the original Number Guessing sample in the repository and explain it from the real source code.
- Add a separate, small Android app that performs real on-device image classification.
- Use Java and XML, matching the original sample.
- Handle a bad image, a failed model load, and a failed inference without crashing.
- Document only behavior that has been checked.

## Scope

One Android Studio project containing:

- the original sample module, left as the reference app;
- one new application module with one main screen.

The new screen can select an image, run the local model, and show the result.

## Features

- Select one image from the device.
- Preprocess it to the verified model input.
- Run TensorFlow Lite on the device. No network call is part of inference.
- Show the predicted category.
- Show a confidence score only when the model output is a probability, or when the report clearly says the score is something else.
- Show one short disposal suggestion from a fixed rule for the predicted label.
- Show a clear message for invalid input and runtime errors.

## Non-goals

- Accounts, login, databases, and cloud backends.
- Online AI APIs.
- Training a model from scratch.
- A camera capture flow, unless image selection alone cannot be tested.
- Multiple screens, history, maps, accounts, or social features.
- GPU or NNAPI delegates.
- Hardcoded labels that pretend to be model predictions.
- Replacing or rewriting the original number-guessing sample unless a build error forces a small, approved fix.

## Acknowledgements required later

The original sample comes from [NumberGuessingAppTflite](https://github.com/Uzair390-Del/NumberGuessingAppTflite). EcoVision runs the WasteNet model checked in Phase 2. Its source, license, and tensor contract are in `docs/TECHNICAL_NOTES.md`.
