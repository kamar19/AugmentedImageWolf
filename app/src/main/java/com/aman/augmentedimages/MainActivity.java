package com.aman.augmentedimages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

import java.io.InputStream;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    private CustomArFragment arFragment;

    private boolean shouldAddModel = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
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

        config.setAugmentedImageDatabase(db);
        return true;
    }

    private Bitmap loadAugmentedImage() {
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
}
