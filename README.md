# Shutterfly Sample

A sample Android application built with modern, state-of-the-art practices. This project uses Clean Architecture, MVVM, and advanced gesture handling in Jetpack Compose.

## Overview

This application is a simple yet powerful image editor that allows users to drag images from a carousel onto a canvas. Once on the canvas, images can be selected, moved, resized, and deleted. All major actions are undoable and redoable.

The primary goal of this repository is to showcase a robust, scalable, and highly performant app structure that follows industry best practices.

## Features

- **Drag & Drop**: Long-press an image from the bottom carousel and drag it onto the canvas.
- **Pan & Zoom**: Select an image on the canvas and use pinch-to-zoom and one-finger-pan to manipulate it. 
- **Undo/Redo**: Every significant action (adding, deleting, or transforming an image) can be undone and redone.
- **Object Selection**: Tap any image to select it, indicated by a highlight border that perfectly matches the image's aspect ratio.
- **Dynamic UI**: The top action bar is contextual, showing undo, redo, and delete options only when an image is selected.
- **Bounded Canvas**: Images are constrained within the canvas area and cannot be dragged off-screen or over the UI bars.


## Architectural Blueprint

This project is built upon a foundation of **Clean Architecture** principles, ensuring a separation of concerns, scalability, and high testability.

+------------------------------------------------------+
|                    PRESENTATION LAYER                |
|       (Android Framework, Jetpack Compose, UI)       |
|                                                      |
| +--------------------------------------------------+ |
| |                  ViewModel (MVVM)                | |
| +--------------------------------------------------+ |
+------------------------|-----------------------------+
| (Depends on)
V
+------------------------------------------------------+
|                     DOMAIN LAYER                     |
|            (Core Business Logic & Rules)             |
|                                                      |
|  +-----------------+   +--------------------------+  |
|  |   Use Cases     |   |   Models (ImageOnCanvas) |  |
|  +-----------------+   +--------------------------+  |
|  |                 |   |   Repository Interface   |  |
|  +-----------------+   +--------------------------+  |
+------------------------|-----------------------------+
| (Depends on)
V
+------------------------------------------------------+
|                       DATA LAYER                     |
|           (Data Sources, Implementations)            |
|                                                      |
|     +------------------------------------------+     |
|     |     Repository Implementation (Local)    |     |
|     +------------------------------------------+     |
+------------------------------------------------------+



### Key Architectural Patterns

- #### Model-View-ViewModel (MVVM)
  The Presentation layer uses the MVVM pattern.
  - **View (`EditorScreen.kt`)**: A "dumb" Composable screen that observes state and sends user actions.
  - **ViewModel (`EditorViewModel.kt`)**: The "brain" that holds the UI state, contains business logic, and processes user actions.
  - **Model**: The `EditorUiState` data class, which serves as the single source of truth for the UI.
  - We strictly follow a **Unidirectional Data Flow (UDF)**, where state flows down from the ViewModel and events flow up from the UI.

- #### Repository Pattern
  The `ImageRepository` interface in the Domain layer defines a contract for how to get data. The `ImageRepositoryImpl` in the Data layer provides a concrete implementation (in this case, from local drawable resources). This abstraction makes it easy to swap out the data source in the future without changing the rest of the app.

- #### Command Pattern
  The undo/redo functionality is implemented using the Command Pattern. Every reversible action is encapsulated as a data object that inherits from the `ImageAction` sealed class. These "command" objects are stored in an `undoStack` and `redoStack` within the `EditorViewModel`.

- #### Dependency Injection with Koin
  We use Koin for dependency injection to provide dependencies (like Use Cases and Repositories) to the classes that need them. This decouples our classes and makes them easier to test and maintain.

---

## Tech Stack & Key Libraries

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture**:
  - Clean Architecture
  - MVVM with Unidirectional Data Flow (UDF)
  - Repository Pattern
- **State Management**: Kotlin Coroutines & `StateFlow`
- **Dependency Injection**: [Koin](https://insert-koin.io/)
- **Gestures**: Advanced, low-level pointer input handling for a seamless user experience.

---

## Getting Started

To get a local copy up and running, follow these simple steps.

1. Clone the repository:
   ```sh
   git clone [https://github.com/your-username/android-image-editor.git](https://github.com/your-username/android-image-editor.git)

2. Open the project in the latest stable version of Android Studio.
3. Let Gradle sync and download the required dependencies.
4. Build and run the application on an emulator or a physical device.

