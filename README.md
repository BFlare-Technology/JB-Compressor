# JB-Compressor

[![](https://jitpack.io/v/BFlare-Technology/JB-Compressor.svg)](https://jitpack.io/#BFlare-Technology/JB-Compressor)

JB-Compressor is a lightweight Android library for compressing image files and `Bitmap` objects with ease. It helps reduce file sizes while maintaining good quality.

---

## Features

- **File-based compression**: Compress images from file paths.
- **Bitmap compression**: Compress `Bitmap` objects directly.
- **Simple to use**: Easy to integrate into any Android project.

---

## Getting Started

### Prerequisites

- **Android Studio**: Arctic Fox or later.
- **Minimum SDK**: 21 (Android 5.0 Lollipop).
- **JitPack Repository**: Add JitPack as a dependency source.

### Installation

1. Add the JitPack repository to your **root `settings.gradle`** or **`build.gradle`**:
   ```gradle
   dependencyResolutionManagement {
       repositories {
           maven { url 'https://jitpack.io' }
       }
   }

   

2. Add the library dependency in your module-level build.gradle:
dependencies {
    implementation 'com.github.BFlare-Technology:JB-Compressor:1.0.0'
}

Usage
Compress an Image File
Create an instance of ImageCompressionHandler:
Pass the context to the constructor of ImageCompressionHandler.

Set the image URI string using setImageUriString():
Provide the URI of the image you want to compress.

Handle the compressed result:
The result will include:

compressedBitmap: The compressed Bitmap.
compressedBytes: The compressed image in byte array format, ideal for uploading to a server.
Here’s an example:
// Create an instance of ImageCompressionHandler
val imageCompressionHandler = ImageCompressionHandler(this) { compressedBitmap, compressedBytes ->
    // Handle the compressed results
    Log.d("JB-Compressor", "Compressed Bitmap: $compressedBitmap")
    Log.d("JB-Compressor", "Compressed Bytes: ${compressedBytes.size} bytes")
}

// Set the image URI string to compress the image
imageCompressionHandler.setImageUriString("content://path/to/image")
This approach ensures flexibility, allowing easy use of the compressed bitmap or byte array for further operations like uploading to a server or saving locally.


        
