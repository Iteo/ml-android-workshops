package com.iteo.mlworkshops

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.Frame
import kotlinx.android.synthetic.main.activity_face_recognition.text
import kotlinx.android.synthetic.main.activity_camera_with_overlay.camera





private const val TAG = "FaceRecognitionScreen"

class FaceRecognitionActivity : AppCompatActivity() {

    var detectionInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_recognition)

        camera.setLifecycleOwner { this.lifecycle }
        camera.addFrameProcessor { frame: Frame ->

            if (detectionInProgress) {
                return@addFrameProcessor
            }

            detectionInProgress = true
            Log.d(com.iteo.mlworkshops.TAG, "Rotation: ${frame.rotation}")
            val metadata = FirebaseVisionImageMetadata.Builder()
                .setWidth(frame.size.width)
                .setHeight(frame.size.height)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(frame.visionImageRotation())
                .build()

            val realTimeOpts = FirebaseVisionFaceDetectorOptions.Builder()
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build()

            val image = FirebaseVisionImage.fromByteArray(frame.data, metadata)
            val detector = FirebaseVision.getInstance().getVisionFaceDetector(realTimeOpts)

            val result = detector.detectInImage(image)
                .addOnSuccessListener { result ->
                    result.forEach { face ->

                        text.text = ""
                        if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            val smileProb = face.getSmilingProbability()
                            Log.d(TAG, "Smile prob: $smileProb")

                            if(smileProb > 0.8) {
                                text.text = "Great Smile!!"
                            }
                        }
                    }
                    detectionInProgress = false
                }
                .addOnFailureListener { failure ->
                    Log.e(com.iteo.mlworkshops.TAG, "Failure", failure)
                }
        }
    }
}

fun createFaceRecognitionIntent(context: Context) = Intent(context, FaceRecognitionActivity::class.java)
