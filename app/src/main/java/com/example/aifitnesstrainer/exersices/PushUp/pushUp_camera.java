package com.example.aifitnesstrainer.exersices.PushUp;

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
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.aifitnesstrainer.DatabaseHelper;
import com.example.aifitnesstrainer.Display;
import com.example.aifitnesstrainer.Feedback;
import com.example.aifitnesstrainer.JavaMailAPI;
import com.example.aifitnesstrainer.R;
import com.example.aifitnesstrainer.User;
import com.example.aifitnesstrainer.user_goal;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import org.opencv.core.Point;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class pushUp_camera extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    Button finish_letralRaise;
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
    boolean complete_exercise = false;
    boolean feedbackSpoken = false;
    TextToSpeech speak;
    ProgressBar circularProgressBar;
    List<Integer> elbow_RAngles = new ArrayList<>();
    List<Integer> hip_shoulder_RAngles = new ArrayList<>();
    List<Integer> lateralLeftState = new ArrayList<>();
    List<String> userFeedback = new ArrayList<>();
    int current_score=0;
    int incorrect_score=0;
    int correct_score=0;
    @ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_up_camera);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView = findViewById(R.id.preview);
        display = findViewById(R.id.display);
        circularProgressBar = findViewById(R.id.circularProgressBar);

        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(10);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5);

        ErrorPaint.setColor(Color.RED);
        ErrorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        ErrorPaint.setStrokeWidth(5);
        finish_letralRaise= findViewById(R.id.finish);
        speak=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    speak.setLanguage(Locale.ENGLISH);
                }
            }
        });
        startCircularTimer();
    }
    @SuppressLint("ObjectAnimatorBinding")
    @ExperimentalGetImage
    private void startCircularTimer() {
        TextView countdownTextView = findViewById(R.id.countdownTextView);
        RelativeLayout timer = findViewById(R.id.timer);
        circularProgressBar.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(circularProgressBar, "progress", 100, 0).setDuration(5000).start();
        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                countdownTextView.setText(String.valueOf(secondsRemaining));
            }
            public void onFinish() {
                timer.setVisibility(View.INVISIBLE);
                speak.speak("Let's start training",TextToSpeech.QUEUE_FLUSH,null);
                cameraProviderFuture.addListener(() -> {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        poseDetection(cameraProvider);
                    } catch (ExecutionException | InterruptedException e) {
                        // No errors need to be handled for this Future.
                    }
                }, ContextCompat.getMainExecutor(getApplicationContext()));
                if (!allPermissionsGranted()) {
                    OpenCamera();
                }
            }
        }.start();
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
    void poseDetection(@NonNull ProcessCameraProvider cameraProvider) {
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
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_SHOULDER ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_ELBOW ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_WRIST ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_HIP ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_KNEE ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_ANKLE ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_PINKY ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_PINKY ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_INDEX ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_INDEX ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_THUMB ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_THUMB ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_HEEL ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_HEEL ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_FOOT_INDEX ||
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
                    User user = databaseHelper.getUserByEmail(userEmail);
                    String userName=user.getname();
                    user_goal lastUserGoal = databaseHelper.getUsergoalByEmail(userEmail);
                    EditText goalEditText = findViewById(R.id.goalEditText);
                    EditText correct_scoree = findViewById(R.id.correct_score);
                    EditText incorrect_scoree = findViewById(R.id.incorrect_score);
                    int goal = lastUserGoal.getgoal();

                    PoseLandmark shoulderr12 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                    PoseLandmark elbowr14 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_ELBOW);
                    PoseLandmark wristr16 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_WRIST);
                    PoseLandmark hipr24 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_HIP);
                    PoseLandmark knee26 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_KNEE);
                    PoseLandmark ankle28 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_ANKLE);

                    EditText errormessage = findViewById(R.id.errorEditText);
                    EditText ErrorshoulderMessage = findViewById(R.id.hipError);
                    EditText shoulderMessage = findViewById(R.id.ankleError);

                    if (shoulderr12 != null && elbowr14 != null && wristr16 != null
                            && hipr24 !=null && knee26 !=null && ankle28 !=null) {
                        drawLineBetweenLandmarks(shoulderr12, elbowr14);
                        drawLineBetweenLandmarks(elbowr14, wristr16);
                        drawLineBetweenLandmarks(shoulderr12, hipr24);
                        drawLineBetweenLandmarks(hipr24, knee26);
                        drawLineBetweenLandmarks(knee26, ankle28);

                        Point rightHipCoord = new Point(hipr24.getPosition().x, hipr24.getPosition().y);
                        Point rightWristCoord = new Point(wristr16.getPosition().x, wristr16.getPosition().y);
                        Point rightShldrCoord = new Point(shoulderr12.getPosition().x, shoulderr12.getPosition().y);
                        Point rightelbowCoord = new Point(elbowr14.getPosition().x, elbowr14.getPosition().y);
                        Point rightkneeCoord = new Point(knee26.getPosition().x, knee26.getPosition().y);
                        Point rightankleCoord = new Point(ankle28.getPosition().x, ankle28.getPosition().y);

                        double rightHipWristShldrAngle  = findAngle(rightHipCoord, rightWristCoord, rightShldrCoord);
                        double rightshldrwristelbowangle = findAngle(rightShldrCoord, rightWristCoord, rightelbowCoord);
                        double rightknee = findAngle(rightHipCoord, rightankleCoord, rightkneeCoord);
                        double righthip = findAngle(rightShldrCoord, rightkneeCoord, rightHipCoord);

                        int shoulderR = (int) Math.round(rightHipWristShldrAngle);
                        int kneeR = (int) Math.round(rightknee);
                        int elbow_angleR = (int) Math.round(rightshldrwristelbowangle);

                        elbow_RAngles.add(elbow_angleR);
                        Collections.sort(elbow_RAngles);
                        int greatestelbowRAngle = !elbow_RAngles.isEmpty() ? elbow_RAngles.get(0) : 0;

                        hip_shoulder_RAngles.add(shoulderR);
                        Collections.sort(hip_shoulder_RAngles, Collections.reverseOrder());

                        int current_state_Left = getStateLeft(elbow_angleR,shoulderR);
                        if (!lateralLeftState.contains(current_state_Left)) {
                            lateralLeftState.add(current_state_Left);
                        }
                        Collections.sort(lateralLeftState, Collections.reverseOrder());

                        if ((lateralLeftState.get(0) == 3 && current_state_Left == 1 && !complete_exercise
                                && shoulderR>60) ||
                                (lateralLeftState.get(0) == 2 && current_state_Left == 1 && !complete_exercise
                                        && shoulderR>60)) {

                            int previous_score=current_score;
                            current_score++;
                            int new_score=current_score;

                            if(greatestelbowRAngle < 60 ){
                                incorrect_score++;
                                userFeedback.add("Your Elbow angle is wrong,please your arm is too deep.");
                                speak.speak("This is an incorrect move because your Elbow angle is wrong", TextToSpeech.QUEUE_FLUSH, null);

                            } else if (greatestelbowRAngle>100 && greatestelbowRAngle<150) {
                                incorrect_score++;
                                userFeedback.add("Your Elbow angle is wrong,please your arm is raised.");
                                speak.speak("This is an incorrect move because your Elbow angle is wrong", TextToSpeech.QUEUE_FLUSH, null);
                            } else{
                                correct_score++;
                                speak.speak("This is a correct move", TextToSpeech.QUEUE_FLUSH, null);
                            }

                            if (new_score > previous_score) {
                                // Clear all lists
                                elbow_RAngles.clear();
                                hip_shoulder_RAngles.clear();
                                lateralLeftState.clear();
                                lateralLeftState.add(0);
                            }
                        }
                        goalEditText.setText("Goal: "+current_score+" / "+goal);
                        correct_scoree.setText("Correct: "+correct_score);
                        incorrect_scoree.setText("InCorrect: "+incorrect_score);

                        if(current_score==goal && !feedbackSpoken){
                            speak.speak("You finish your exercise please check our feedback",TextToSpeech.QUEUE_ADD,null);
                            feedbackSpoken = true;
                            complete_exercise=true;
                            String email = userEmail.toString();
                            String ex_name = lastUserGoal.getname().toString();
                            int goall = Integer.parseInt(goalEditText.getText().toString().split(" / ")[1]);
                            int correctScore = Integer.parseInt(correct_scoree.getText().toString().split(": ")[1]);
                            int incorrectScore = Integer.parseInt(incorrect_scoree.getText().toString().split(": ")[1]);
                            double accuracy = (double) correctScore / (correctScore + incorrectScore);
                            String workoutFeedback = TextUtils.join(", ", userFeedback);
                            SendMail(email, userName, ex_name, goall, correctScore, incorrectScore, accuracy, workoutFeedback);
                            boolean inserted = databaseHelper.insertuserfeedback(email, ex_name, goall, correctScore, incorrectScore, accuracy, workoutFeedback);
                            if (inserted) {
                                Toast.makeText(pushUp_camera.this, "you can see feedback on the exercise.", Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(getApplicationContext(), Feedback.class);
                                        startActivity(intent);
                                    }
                                }, 2000); // 2000 milliseconds = 2 seconds delay
                            } else {
                                Toast.makeText(pushUp_camera.this, "Failed Inserted", Toast.LENGTH_SHORT).show();
                            }

                        }

//                        shoulderMessage.setText("shoulder angle: " +shoulderR + "/ knee angle:"+ kneeR);
                        errormessage.setText("");

                        // Update Elbow Message
                        if(elbow_angleR < 150 && elbow_angleR > 100){
                            ErrorshoulderMessage.setText("your arm is raised,please lower your arm.");
                            drawErrorLineBetweenLandmarks(elbowr14, wristr16);
                        } else if(elbow_angleR < 60 ){
                            ErrorshoulderMessage.setText("your arm is too deep,please raise your arm.");
                            drawErrorLineBetweenLandmarks(elbowr14, wristr16);
                        } else {
                            drawLineBetweenLandmarks(elbowr14, wristr16);
                            ErrorshoulderMessage.setText("");
                        }
                    } else {
                        errormessage.setText("Some landmarks are missing for lateral raise detection");
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
    private void SendMail(String email, String userName,String ex_name, int goal, int correctScore, int incorrectScore, double accuracy, String workoutFeedback) {
        String subject = "Feedback about your training for this exercise";
        String message = "Dear, " + userName
                + "\nYou've finished your workout and here are our training notes for this workout"
                + "\nExercise Name: " + ex_name
                + "\nYour Goal: " + goal
                + "\nYour Correct repetition: " + correctScore
                + "\nYour Incorrect repetition: " + incorrectScore
                + "\nOur Feedback: " + workoutFeedback
                + "\nYour Accuracy: " + (accuracy*100)+"%"
                + "\nKeep going and do your best.";
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }
    private int getStateLeft(int elbowRAngle ,int shoulderRAngle) {
        int elbowRight = 0;
        if ( elbowRAngle < 180 && elbowRAngle >= 150 && shoulderRAngle > 65 && shoulderRAngle < 91) {
            elbowRight = 1;
        } else if ( elbowRAngle < 150 && elbowRAngle >= 100 && shoulderRAngle > 65 && shoulderRAngle < 91) {
            elbowRight = 2;
        } else if ( elbowRAngle < 100 && elbowRAngle >= 60 && shoulderRAngle > 65 && shoulderRAngle < 91) {
            elbowRight = 3;
        }
        return elbowRight;
    }
    public static double findAngle(Point p1, Point p2, Point refPt) {
        double p1RefX = p1.x - refPt.x;
        double p1RefY = p1.y - refPt.y;
        double p2RefX = p2.x - refPt.x;
        double p2RefY = p2.y - refPt.y;
        double cosTheta = (p1RefX * p2RefX + p1RefY * p2RefY) /
                (Math.sqrt(p1RefX * p1RefX + p1RefY * p1RefY) * Math.sqrt(p2RefX * p2RefX + p2RefY * p2RefY));
        double theta = Math.acos(Math.min(Math.max(cosTheta, -1.0), 1.0));
        double degree = Math.toDegrees(theta);
        return degree;
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
    private void OpenCamera() {
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
}