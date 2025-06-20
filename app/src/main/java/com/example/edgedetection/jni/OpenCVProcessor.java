package com.example.edgedetection.jni;

public class OpenCVProcessor {
    static {
        if (!loadOpenCVLibrary()) {
            System.loadLibrary("edgedetection");
        }
    }

    private static boolean loadOpenCVLibrary() {
        try {
            System.loadLibrary("opencv_java4");
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    public native byte[] processFrame(byte[] input, int width, int height);
}