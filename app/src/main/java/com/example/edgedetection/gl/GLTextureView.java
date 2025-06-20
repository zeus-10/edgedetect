package com.example.edgedetection.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

public class GLTextureView extends GLSurfaceView {
    private static final String TAG = "GLTextureView";
    private GLRenderer renderer;

    public GLTextureView(Context context) {
        super(context);
        init();
    }

    public GLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Log.d(TAG, "Initializing GLTextureView");
        setEGLContextClientVersion(2);
        renderer = new GLRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // Back to on-demand rendering
        Log.d(TAG, "GLTextureView initialized");
    }

    public void updateTexture(byte[] imageData, int width, int height) {
        Log.d(TAG, "updateTexture called with data length: " +
                (imageData != null ? imageData.length : "null") +
                " size: " + width + "x" + height);

        if (renderer != null) {
            queueEvent(() -> {
                renderer.updateTexture(imageData, width, height);
                Log.d(TAG, "Texture update queued");
            });
            requestRender(); // Request render after texture update
        } else {
            Log.e(TAG, "Renderer is null!");
        }
    }
}