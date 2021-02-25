package com.aman.augmentedimages;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String DATA_SD = "present_st.mp3";

    private CustomArFragment arFragment;
    private Menu menu;
    private boolean shouldAddModel = true;
    protected MediaPlayer player;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.v("object is run", getCurrentDateTimeString());

        setContentView(R.layout.activity_main);
        menu = findViewById(R.menu.menu);

        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        assert arFragment != null;
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getArSceneView().getScene().setOnUpdateListener(this::onUpdateFrame);

    }

    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        Collection<AugmentedImage> images = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage img : images) {
            if (img.getTrackingState() == TrackingState.TRACKING) {
                if (img.getName().equalsIgnoreCase("wolf") && shouldAddModel) {
                    placeObject(arFragment, img.createAnchor(img.getCenterPose()), Uri.parse("wolf.sfb"));
                    shouldAddModel = false;
                    Log.v("TRACKING",getCurrentDateTimeString());
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.v("TRACKING_2",getCurrentDateTimeString());

                    startPlayer(DATA_SD);

                }
            }
        }
    }

    public boolean setupAugmentedImageDb(Config config, Session session) {
        AugmentedImageDatabase db;
        Bitmap image = loadAugmentedImage();
        if (image == null)
            return false;

        db = new AugmentedImageDatabase(session);
        db.addImage("wolf", image);
        // модель

        config.setAugmentedImageDatabase(db);

        return true;
    }

    private Bitmap loadAugmentedImage() {
        // сравниваемая опорная картинка
        try (InputStream is = getAssets().open("airplane.jpg")) {
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            System.out.println("Exception : " + e.getLocalizedMessage());
        }

        return null;
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        ModelRenderable.builder()
                .setSource(fragment.getContext(), model)
                .build()
                .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return null;
                });
    }

    private void addNodeToScene
            (ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menuRun: {
                return true;
            }
            case R.id.menuListen: {
                startPlayer(DATA_SD);
                return true;
            }
            case R.id.pause: {
                player.pause();
                return true;
            }
            case R.id.menuExit: {
                this.finish();
                return true;
            }

        }
            return super.onOptionsItemSelected(item);

    }

    public void startPlayer(String filePath) {
        if (player == null) {
            player = new MediaPlayer();
            player = MediaPlayer.create(this, R.raw.present_st);
            Log.v("player.start()", getCurrentDateTimeString());
            player.start();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            try {
                player.release();
                player = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
    public SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.ENGLISH);

    public String getCurrentDateTimeString() {
         Date date = new Date();
         String str = sdf.format(date);
        return str;
    }
}

