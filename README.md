# Real-Time Edge Detection Viewer

An Android application that captures camera frames, processes them using OpenCV Canny edge detection in C++, and displays the results in real-time using OpenGL ES 2.0.



### Core Features 
- **Camera Feed Integration**
  - Real-time camera capture using CameraX API
  - YUV420_888 format handling with proper stride management
  - Efficient frame processing with background threading

- **Frame Processing via OpenCV (C++)**
  - Native C++ implementation using JNI bridge
  - Canny edge detection with Gaussian blur preprocessing
  - Morphological operations for edge enhancement
  - Optimized grayscale (Y-plane) processing

- **Real-time OpenGL ES Rendering**
  - Custom OpenGL ES 2.0 renderer with vertex/fragment shaders
  - Efficient texture streaming and updates
  - Smooth real-time performance (10-15 FPS minimum)

### Technical Implementation
- **Modular Architecture**: Clean separation between Java UI, JNI bridge, and native processing
- **Memory Management**: Proper resource cleanup and efficient buffer handling
- **Error Handling**: Comprehensive error checking for OpenCV, OpenGL, and camera operations
- **Thread Safety**: Background processing with UI thread synchronization

## 📷 Screenshots
![WhatsApp Image 2025-06-20 at 17 17 43_e514ea4f](https://github.com/user-attachments/assets/ffc470ed-2ecb-45c3-8c0a-b005323cd1f9)
![WhatsApp Image 2025-06-20 at 17 17 43_f3875020](https://github.com/user-attachments/assets/378d9faf-70f2-443e-a651-a832b3f8a0c1)
![WhatsApp Image 2025-06-20 at 17 17 42_a5ee27f1](https://github.com/user-attachments/assets/ba031912-490e-4c8c-b757-2a1183738f5b)
![WhatsApp Image 2025-06-20 at 17 17 43_5002a169](https://github.com/user-attachments/assets/6dae64a4-190c-4bf6-a76a-29bd321f9035)
![WhatsApp Image 2025-06-20 at 17 17 41_eb78951b](https://github.com/user-attachments/assets/ef1f0f12-fc8f-4e98-8219-bfab73053ec5)
![WhatsApp Image 2025-06-20 at 17 17 42_7ed5b9fd](https://github.com/user-attachments/assets/3adb79ff-0df2-4172-b812-de8bb3406faa)
![WhatsApp Image 2025-06-20 at 17 17 44_b3cbcac3](https://github.com/user-attachments/assets/7ac02a02-93c7-4f37-974d-079353644dc1)








The app displays:
- **White edges** on black background showing detected object boundaries
- **Real-time processing** of camera feed
- **Responsive edge detection** when moving camera across objects

## ⚙ Setup Instructions

### Prerequisites
- Android Studio 4.2 or later
- Android NDK 25.1.8937393 or later
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 34

### OpenCV Dependencies Setup

1. **Download OpenCV Android SDK**
   ```bash
   # Download from https://opencv.org/releases/
   # Extract opencv-android-sdk.zip
   ```

2. **Copy OpenCV Files**
   ```bash
   # Copy native libraries
   cp -r OpenCV-android-sdk/sdk/native/ app/src/main/cpp/opencv/
   
   # Copy Java wrapper (if using OpenCV Java API)
   cp -r OpenCV-android-sdk/sdk/java/ app/src/main/java/opencv/
   ```

3. **Project Structure**
   ```
   app/src/main/
   ├── cpp/
   │   ├── opencv/           # OpenCV native libraries
   │   │   ├── jni/
   │   │   └── libs/
   │   ├── CMakeLists.txt
   │   ├── native-lib.cpp
   │   └── opencv_processor.cpp
   ├── java/com/example/edgedetector/
   │   ├── MainActivity.java
   │   ├── gl/
   │   │   ├── GLRenderer.java
   │   │   └── GLTextureView.java
   │   └── jni/
   │       └── OpenCVProcessor.java
   └── res/layout/
       └── activity_main.xml
   ```

### Build Configuration

1. **Configure build.gradle (Module: app)**
   ```gradle
   android {
       ndkVersion "25.1.8937393"
       externalNativeBuild {
           cmake {
               path "src/main/cpp/CMakeLists.txt"
           }
       }
   }
   ```

2. **Build Project**
   ```bash
   # In Android Studio
   Build → Clean Project
   Build → Rebuild Project
   ```

3. **Run on Device**
   - Deploy to physical Android device (recommended for camera testing)
   - Grant camera permissions when prompted



## Architecture Overview

### Data Flow Pipeline
```
Camera → ImageAnalysis → JNI Bridge → OpenCV C++ → JNI Bridge → OpenGL → Display
```

### Component Architecture

#### 1. **Java Layer (UI & Camera)**
- **MainActivity**: Orchestrates camera setup and lifecycle management
- **CameraX Integration**: Handles camera permissions, configuration, and frame capture
- **GLTextureView**: Custom OpenGL surface view for rendering processed frames

#### 2. **JNI Bridge Layer**
- **OpenCVProcessor.java**: Java interface for native method calls
- **native-lib.cpp**: JNI implementation bridging Java and C++
- **Memory Management**: Efficient byte array handling between Java and native code

#### 3. **Native Processing Layer (C++)**
- **OpenCV Integration**: Computer vision processing using OpenCV C++ API
- **Canny Edge Detection**: Multi-stage edge detection algorithm
  - Gaussian blur for noise reduction
  - Canny algorithm with dual thresholding
  - Morphological operations for edge enhancement
- **Format Conversion**: YUV to grayscale to RGB processing

#### 4. **Rendering Layer (OpenGL ES)**
- **GLRenderer**: Custom OpenGL ES 2.0 renderer
- **Shader Programs**: Vertex and fragment shaders for texture mapping
- **Texture Streaming**: Efficient real-time texture updates

### Frame Processing Flow

1. **Camera Capture**
   ```
   CameraX → YUV420_888 ImageProxy → Y-plane extraction
   ```

2. **Native Processing**
   ```java
   byte[] yData → JNI → cv::Mat → GaussianBlur → Canny → cvtColor → byte[]
   ```

3. **OpenGL Rendering**
   ```
   byte[] → ByteBuffer → glTexImage2D → Fragment Shader → Screen
   ```


## 📦 Dependencies

```gradle
// Camera
implementation 'androidx.camera:camera-core:1.3.0'
implementation 'androidx.camera:camera-camera2:1.3.0'
implementation 'androidx.camera:camera-lifecycle:1.3.0'
implementation 'androidx.camera:camera-view:1.3.0'

// Native
// OpenCV C++ (included as native libraries)
// OpenGL ES 2.0 (system)
// Android NDK
```


