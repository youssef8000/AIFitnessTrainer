package com.example.aifitnesstrainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class view_camera extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    Button finish_squat;
    int PERMISSION_REQUESTS = 1;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    // Base pose detector with streaming frames, when depending on the pose-detection sdk
    PoseDetectorOptions options =
            new PoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                    .build();

    PoseDetector poseDetector = PoseDetection.getClient(options);
    Canvas canvas;
    Paint mPaint = new Paint();
    Paint paint = new Paint();
    Paint ErrorPaint = new Paint();
    Display display;
    Bitmap bitmap4Save;
    ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    ArrayList<Bitmap> bitmap4DisplayArrayList = new ArrayList<>();
    ArrayList<Pose> poseArrayList = new ArrayList<>();
    boolean isRunning = false;
    List<Integer> kneeAngles = new ArrayList<>();
    List<Integer> hipAngles = new ArrayList<>();
    List<Integer> ankleAngles = new ArrayList<>();
    List<Integer> seqState = new ArrayList<>();
    List<String> userFeedback = new ArrayList<>();
    int current_score=0;
    int incorrect_score=0;
    int correct_score=0;

    @ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_camera);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView = findViewById(R.id.preview);
        display = findViewById(R.id.display);

        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(10);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5);

        ErrorPaint.setColor(Color.RED);
        ErrorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        ErrorPaint.setStrokeWidth(5);
        finish_squat= findViewById(R.id.finish);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
            }
        }, ContextCompat.getMainExecutor(this));
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
    }
    Runnable RunMlkit = new Runnable() {
        @Override
        public void run() {
            poseDetector.process(InputImage.fromBitmap(bitmapArrayList.get(0),0)).addOnSuccessListener(new OnSuccessListener<Pose>() {
                @Override
                public void onSuccess(Pose pose) {
                    poseArrayList.add(pose);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    };
    @ExperimentalGetImage
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
//                         enable the following line if RGBA output is needed.
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ActivityCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                ByteBuffer byteBuffer = imageProxy.getImage().getPlanes()[0].getBuffer();
                byteBuffer.rewind();
                Bitmap bitmap = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(byteBuffer);
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                matrix.postScale(-1,1);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,imageProxy.getWidth(), imageProxy.getHeight(),matrix,false);
                bitmapArrayList.add(rotatedBitmap);

                if (poseArrayList.size() >= 1) {
                    canvas = new Canvas(bitmapArrayList.get(0));
                    for (PoseLandmark poseLandmark : poseArrayList.get(0).getAllPoseLandmarks()) {

                        if (poseLandmark.getLandmarkType() == PoseLandmark.NOSE ||
//                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_SHOULDER ||
                               poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_SHOULDER ||
//                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_ELBOW ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_ELBOW ||
//                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_WRIST ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_WRIST ||
//                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_HIP ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_HIP ||
//                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_KNEE ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_KNEE ||
//                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_ANKLE ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_ANKLE ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_PINKY ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_PINKY ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_INDEX ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_INDEX ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_THUMB ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_THUMB ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_HEEL ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_HEEL ||
//                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_FOOT_INDEX ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_FOOT_INDEX ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_EYE_INNER ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_EYE ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_EYE_OUTER ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_EYE_INNER ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_EYE ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_EYE_OUTER ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_EAR ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_EAR ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_MOUTH ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_MOUTH) {
                            continue;
                        }
                        // Draw circle for other landmarks
                        canvas.drawCircle(poseLandmark.getPosition().x, poseLandmark.getPosition().y, 3, mPaint);
                    }
                    databaseHelper = new DatabaseHelper(getApplicationContext());
                    SharedPreferences preferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
                    String userEmail = preferences.getString("user_email", "");
                    user_goal lastUserGoal = databaseHelper.getUsergoalByEmail(userEmail);
                    EditText goalEditText = findViewById(R.id.goalEditText);
                    EditText correct_scoree = findViewById(R.id.correct_score);
                    EditText incorrect_scoree = findViewById(R.id.incorrect_score);
                    int goal = lastUserGoal.getgoal();

                    PoseLandmark elbowr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_ELBOW);
                    PoseLandmark wristr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_WRIST);
                    PoseLandmark shoulderr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                    PoseLandmark hipr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_HIP);
                    PoseLandmark kneer = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_KNEE);
                    PoseLandmark ankler = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_ANKLE);
                    PoseLandmark footr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
                    EditText errormessage = findViewById(R.id.errorEditText);
                    EditText ErrorKneeMessage = findViewById(R.id.kneeError);
                    EditText ErrorHipMessage = findViewById(R.id.hipError);
                    EditText ErrorAnkleMessage = findViewById(R.id.ankleError);

                    if (hipr != null && kneer != null && ankler != null && shoulderr != null && wristr != null
                            && elbowr != null&& footr != null) {
                        drawLineBetweenLandmarks(wristr, elbowr);
                        drawLineBetweenLandmarks(elbowr, shoulderr);
                        drawLineBetweenLandmarks(shoulderr, hipr);
                        drawLineBetweenLandmarks(hipr, kneer);
                        drawLineBetweenLandmarks(kneer, ankler);
                        drawLineBetweenLandmarks(ankler,footr);

                        double[] hipCoord = { hipr.getPosition().x, hipr.getPosition().y };
                        double[] shoulderCoord = { shoulderr.getPosition().x,  shoulderr.getPosition().y };
                        double[] kneeCoord = { kneer.getPosition().x, kneer.getPosition().y };
                        double[] ankleCoord = { ankler.getPosition().x, ankler.getPosition().y };

                        double kneeAngleDegrees = findAngle(hipCoord, new double[]{ kneeCoord[0], 0 }, kneeCoord);
                        double hipAngleDegrees = findAngle(shoulderCoord, new double[]{ hipCoord[0], 0 }, hipCoord);
                        double ankleAngleDegrees = findAngle(kneeCoord, new double[]{ ankleCoord[0], 0 }, ankleCoord);

                        int roundedKneeFlexionAngle = (int) Math.round(kneeAngleDegrees);
                        int roundedHipFlexionAngle = (int) Math.round(hipAngleDegrees);
                        int roundedAnkleDorsiflexionAngle = (int) Math.round(ankleAngleDegrees);

                        kneeAngles.add(roundedKneeFlexionAngle);
                        hipAngles.add(roundedHipFlexionAngle);
                        ankleAngles.add(roundedAnkleDorsiflexionAngle);
                        Collections.sort(kneeAngles, Collections.reverseOrder());
                        int greatestKneeAngle = !kneeAngles.isEmpty() ? kneeAngles.get(0) : 0;
                        Collections.sort(hipAngles, Collections.reverseOrder());
                        int greatestHipAngle = !hipAngles.isEmpty() ? hipAngles.get(0) : 0;
                        Collections.sort(ankleAngles, Collections.reverseOrder());
                        int greatestAnkleAngle = !ankleAngles.isEmpty() ? ankleAngles.get(0) : 0;
                        int current_state = getState(roundedKneeFlexionAngle);
                        if (!seqState.contains(current_state)) {
                            seqState.add(current_state);
                        }
                        Collections.sort(seqState, Collections.reverseOrder());
                        if(seqState.get(0)==3 && current_state==1){
                            int previous_score=current_score;
                            current_score++;
                            int new_score=current_score;
                            if(greatestKneeAngle> 95){
                                incorrect_score++;
                                userFeedback.add("Your knee angle is too deep.");
                            } else if (greatestKneeAngle>50 && greatestKneeAngle<80) {
                                incorrect_score++;
                                userFeedback.add("Lower your hip to correct the knee angle.");
                            } else if (greatestHipAngle>45 ) {
                                incorrect_score++;
                                userFeedback.add("You are bending backward.");
                            } else if (greatestHipAngle<20 && greatestHipAngle>10) {
                                incorrect_score++;
                                userFeedback.add("You are bending forward.");
                            } else if ( greatestAnkleAngle>40 ) {
                                incorrect_score++;
                                userFeedback.add("Your knee is falling over your toe.");
                            } else{
                                correct_score++;
                            }

                            if (new_score >previous_score) {
                                // Clear all lists
                                kneeAngles.clear();
                                hipAngles.clear();
                                ankleAngles.clear();
                                seqState.clear();
                                seqState.add(0); // Add the initial state or any appropriate value
                            }
                        }
                        goalEditText.setText("Goal: "+current_score+" / "+goal);
                        correct_scoree.setText("Correct: "+correct_score);
                        incorrect_scoree.setText("InCorrect: "+incorrect_score);

                        if(current_score==goal){
                            finish_squat.setVisibility(View.VISIBLE);
                        }
                        finish_squat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String email = userEmail.toString();
                                String ex_name = lastUserGoal.getname().toString();
                                int goal = Integer.parseInt(goalEditText.getText().toString().split(" / ")[1]);
                                int correctScore = Integer.parseInt(correct_scoree.getText().toString().split(": ")[1]);
                                int incorrectScore = Integer.parseInt(incorrect_scoree.getText().toString().split(": ")[1]);
                                double accuracy = (double) correctScore / (correctScore + incorrectScore);
                                String workoutFeedback = TextUtils.join(", ", userFeedback);
                                boolean inserted = databaseHelper.insertuserfeedback(email, ex_name, goal, correctScore, incorrectScore, accuracy, workoutFeedback);
                                if (inserted) {
                                    Toast.makeText(view_camera.this, "you can see feedback on the exercise.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Feedback.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(view_camera.this, "Failed Inserted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        // Define the starting and ending points for the vertical line
                        float hipStartX = (float) hipCoord[0];
                        float hipStartY = (float) hipCoord[1] - 50;
                        float hipEndY = (float) hipCoord[1] + 20;
                        float kneeStartX = (float) kneeCoord[0];
                        float kneeStartY = (float) kneeCoord[1] - 50;
                        float kneeEndY = (float) kneeCoord[1] + 20;
                        float ankleStartX = (float) ankleCoord[0];
                        float ankleStartY = (float) ankleCoord[1] - 50;
                        float ankleEndY = (float) ankleCoord[1] + 20;

                        // Create a Paint object for drawing the line
                        Paint linePaint = new Paint();
                        linePaint.setColor(Color.BLACK);
                        linePaint.setStrokeWidth(3);

                        // Draw the vertical line on the bitmap
                        canvas.drawLine(kneeStartX, kneeStartY, kneeStartX, kneeEndY, linePaint);
                        canvas.drawLine(hipStartX, hipStartY, hipStartX, hipEndY, linePaint);
                        canvas.drawLine(ankleStartX, ankleStartY, ankleStartX, ankleEndY, linePaint);

                        // Draw the dotted line at the ankle coordinate
                        drawDottedLine(bitmap, hipCoord, hipCoord[1] - 50, hipCoord[1] + 20, Color.BLACK);
                        drawDottedLine(bitmap, kneeCoord, kneeCoord[1] - 50, kneeCoord[1] + 20, Color.BLACK);
                        drawDottedLine(bitmap, ankleCoord, ankleCoord[1] - 50, ankleCoord[1] + 20, Color.WHITE); // Adjust color as needed

                        // Update EditText fields
                        EditText kneeEditText = findViewById(R.id.angleknee);
                        kneeEditText.setText("angle of knee: "+roundedKneeFlexionAngle + " " + greatestKneeAngle+" "+current_state
                                +" "+seqState.get(0));
                        EditText hipEditText = findViewById(R.id.anglehip);
                        hipEditText.setText("angle of hip: " + roundedHipFlexionAngle+" "+greatestHipAngle);
                        EditText ankleEditText = findViewById(R.id.angleankle);
                        ankleEditText.setText("angle of ankle: " + roundedAnkleDorsiflexionAngle + " " + greatestAnkleAngle);
                        errormessage.setText("");

                        // Update Knee Message
                        if(roundedKneeFlexionAngle> 95){
                            ErrorKneeMessage.setText("");
                            drawErrorLineBetweenLandmarks(hipr, kneer);
                            ErrorKneeMessage.setText("Squat To Deep");
                        } else if (roundedKneeFlexionAngle>50 && roundedKneeFlexionAngle<80) {
                            ErrorKneeMessage.setText("");
                            drawErrorLineBetweenLandmarks(hipr, kneer);
                            ErrorKneeMessage.setText("Lower Your Hip");
                        }else {
                            drawLineBetweenLandmarks(hipr, kneer);
                            ErrorKneeMessage.setText("");
                        }

                        // Update Hip Message
                        if(roundedHipFlexionAngle>45){
                            ErrorHipMessage.setText("");
                            drawErrorLineBetweenLandmarks(shoulderr, hipr);
                            ErrorHipMessage.setText("Bend Backward");
                        } else if (roundedHipFlexionAngle<20 && roundedHipFlexionAngle>10) {
                            ErrorHipMessage.setText("");
                            drawErrorLineBetweenLandmarks(shoulderr, hipr);
                            ErrorHipMessage.setText("Bend Forward");
                        }else {
                            drawLineBetweenLandmarks(shoulderr, hipr);
                            ErrorHipMessage.setText("");
                        }

                        // Update Ankle Message
                        if(roundedAnkleDorsiflexionAngle>40){
                            ErrorAnkleMessage.setText("");
                            drawErrorLineBetweenLandmarks(kneer, ankler);
                            ErrorAnkleMessage.setText("Knee Falling Over Toe");
                        }else {
                            drawLineBetweenLandmarks(kneer, ankler);
                            ErrorAnkleMessage.setText("");
                        }

                    } else {
                        errormessage.setText("Some landmarks are missing for squat detection");
                    }

                    bitmap4DisplayArrayList.clear();
                    bitmap4DisplayArrayList.add(bitmapArrayList.get(0));
                    bitmap4Save = bitmapArrayList.get(bitmapArrayList.size()-1);
                    bitmapArrayList.clear();
                    bitmapArrayList.add(bitmap4Save);
                    poseArrayList.clear();
                    isRunning = false;
                }

                if (poseArrayList.size() == 0 && bitmapArrayList.size() >= 1 && !isRunning) {
                    RunMlkit.run();
                    isRunning = true;
                }
                if (bitmap4DisplayArrayList.size() >= 1) {
                    display.getBitmap(bitmap4DisplayArrayList.get(0));
                }
                imageProxy.close();
            }
        });

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }
    private int getState(int kneeAngle) {
        int knee = 0;
        if (0 <= kneeAngle && kneeAngle <= 32) {
            knee = 1;
        } else if (35 <= kneeAngle && kneeAngle <= 65) {
            knee = 2;
        } else if (70 <= kneeAngle && kneeAngle <= 95) {
            knee = 3;
        }
        return knee;
    }
    public static void drawDottedLine(Bitmap bitmap, double[] lmCoord, double start, double end, int color) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(2);
        int pixStep = 0;
        for (int i = (int) start; i <= end; i += 8) {
            canvas.drawCircle((float) lmCoord[0], i + pixStep, 2, paint);
        }
    }
    public static double findAngle(double[] p1, double[] p2, double[] refPt) {
        double[] p1Ref = { p1[0] - refPt[0], p1[1] - refPt[1] };
        double[] p2Ref = { p2[0] - refPt[0], p2[1] - refPt[1] };
        double cosTheta = dotProduct(p1Ref, p2Ref) / (1.0 * norm(p1Ref) * norm(p2Ref));
        double theta = Math.acos(clamp(cosTheta, -1.0, 1.0));
        return Math.toDegrees(theta);
    }
    public static double dotProduct(double[] vec1, double[] vec2) {
        return vec1[0] * vec2[0] + vec1[1] * vec2[1];
    }
    public static double norm(double[] vec) {
        return Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1]);
    }
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    private void drawLineBetweenLandmarks(PoseLandmark landmark1, PoseLandmark landmark2) {
        if (landmark1 != null && landmark2 != null) {
            canvas.drawLine(
                    landmark1.getPosition().x,
                    landmark1.getPosition().y,
                    landmark2.getPosition().x,
                    landmark2.getPosition().y,
                    paint
            );
        }
    }
    private void drawErrorLineBetweenLandmarks(PoseLandmark landmark1, PoseLandmark landmark2) {
        if (landmark1 != null && landmark2 != null) {
            canvas.drawLine(
                    landmark1.getPosition().x,
                    landmark1.getPosition().y,
                    landmark2.getPosition().x,
                    landmark2.getPosition().y,
                    ErrorPaint
            );
        }
    }
    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }
    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }
    private void openUserFeedbackFragment() {
        // Create a new instance of the UserFeedback fragment
        UserFeedback userFeedbackFragment = new UserFeedback();

        // Begin a fragment transaction
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, userFeedbackFragment) // Replace fragment_container with the ID of your fragment container layout
                .addToBackStack(null) // Add the transaction to the back stack
                .commit();
    }
}