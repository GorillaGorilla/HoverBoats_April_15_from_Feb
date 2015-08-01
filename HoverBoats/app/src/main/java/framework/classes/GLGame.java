package framework.classes;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import framework.interfaces.Audio;
import framework.interfaces.FileIO;
import framework.interfaces.Game;
import framework.interfaces.Graphics;
import framework.interfaces.Input;
import framework.interfaces.Screen;

/**
 * Created by New PC 2012 on 23/12/2014.
 */
public class GLGame extends Activity implements Game, Renderer {
    enum GLGameState {
        Initialized,
        Running,
        Paused,
        Finished,
        Idle
    }
    GLSurfaceView glView;
    GLGraphics glGraphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    GLGameState state = GLGameState.Initialized;
    Object stateChanged = new Object();
    long startTime = System.nanoTime();
    WakeLock wakeLock;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        glView = new GLSurfaceView(this);
        glView.setRenderer(this);
        setContentView(glView);
        glGraphics = new GLGraphics(glView);
        fileIO = new AndroidFileIO(this);
        audio = new AndroidAudio(this);
        input = new AndroidInput(this, glView, 1, 1);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GLGame");
        log("created");
    }

    @Override
    public void onResume() {
        super.onResume();
        glView.onResume();
        wakeLock.acquire();
        log("resumed");
    }
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glGraphics.setGL(gl);
        synchronized(stateChanged) {
            if(state == GLGameState.Initialized)
                screen = getStartScreen();
            state = GLGameState.Running;
            screen.resume();
            startTime = System.nanoTime();
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    public void onDrawFrame(GL10 gl) {
        GLGameState state = null;
        synchronized(stateChanged) {
            state = this.state;
        }
        if(state == GLGameState.Running) {
            float deltaTime = (System.nanoTime()-startTime) / 1000000000.0f;
            startTime = System.nanoTime();
            screen.update(deltaTime);
            screen.present(deltaTime);
        }
        if(state == GLGameState.Paused) {
            screen.pause();
            synchronized(stateChanged) {
                this.state = GLGameState.Idle;
                stateChanged.notifyAll();
            }
        }
        if(state == GLGameState.Finished) {
            screen.pause();
            screen.dispose();
            synchronized(stateChanged) {
                this.state = GLGameState.Idle;
                stateChanged.notifyAll();
            }
        }
    }
    @Override
    public void onPause() {
        synchronized(stateChanged) {
            if(isFinishing())
                state = GLGameState.Finished;
            else
                state = GLGameState.Paused;
            while(true) {
                try {
                    stateChanged.wait();
                    break;
                } catch(InterruptedException e) {
                }
            }
        }
        wakeLock.release();
        glView.onPause();
        super.onPause();
        log("paused");
        if (isFinishing()) {
            log("finishing");
        }
    }
    public GLGraphics getGLGraphics() {
        return glGraphics;
    }
    public Input getInput() {
        return input;
    }
    public FileIO getFileIO() {
        return fileIO;
    }
    public Graphics getGraphics() {
        throw new IllegalStateException("We are using OpenGL!");
    }
    public Audio getAudio() {
        return audio;
    }

    public void setScreen(Screen newScreen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");
        this.screen.pause();
        this.screen.dispose();
        newScreen.resume();
        newScreen.update(0);
        this.screen = newScreen;
    }
    private void log(String text) {
        Log.d("LifeCycleTest", text);
    }

    public Screen getCurrentScreen() {
        return screen;
    }

    @Override
    public Screen getStartScreen() {
        return null;
    }


}
