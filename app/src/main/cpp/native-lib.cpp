#include <jni.h>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <GLES2/gl2.h>

#define LOG_TAG "EdgeDetection"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetection_jni_OpenCVProcessor_processFrame(
        JNIEnv *env, jobject thiz, jbyteArray input, jint width, jint height) {

    // Get input data
    jbyte* inputData = env->GetByteArrayElements(input, nullptr);
    jsize inputSize = env->GetArrayLength(input);

    LOGI("Processing frame: width=%d, height=%d, inputSize=%d", width, height, inputSize);

    try {

        cv::Mat yMat(height, width, CV_8UC1, (unsigned char*)inputData);


        if (yMat.empty()) {
            LOGE("Input Mat is empty");
            env->ReleaseByteArrayElements(input, inputData, JNI_ABORT);
            return nullptr;
        }


        cv::Mat blurred;
        cv::GaussianBlur(yMat, blurred, cv::Size(5, 5), 1.4);


        cv::Mat edges;
        cv::Canny(blurred, edges, 50, 120, 3);


        cv::Mat kernel = cv::getStructuringElement(cv::MORPH_RECT, cv::Size(2, 2));
        cv::morphologyEx(edges, edges, cv::MORPH_CLOSE, kernel);


        cv::Mat resultMat;
        cv::cvtColor(edges, resultMat, cv::COLOR_GRAY2RGB);


        int outputSize = resultMat.total() * resultMat.channels();
        LOGI("Output size: %d", outputSize);

        jbyteArray output = env->NewByteArray(outputSize);
        env->SetByteArrayRegion(output, 0, outputSize,
                                reinterpret_cast<jbyte*>(resultMat.data));

        env->ReleaseByteArrayElements(input, inputData, JNI_ABORT);
        return output;

    } catch (const std::exception& e) {
        LOGE("Error processing frame: %s", e.what());
        env->ReleaseByteArrayElements(input, inputData, JNI_ABORT);
        return nullptr;
    }
}

JNIEXPORT jstring JNICALL
Java_com_example_edgedetection_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string hello = "OpenCV " + cv::getVersionString() + " loaded successfully!";
    return env->NewStringUTF(hello.c_str());
}

}