package com.example.aifitnesstrainer.exersices.Lunge;

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
import android.graphics.PointF;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class view_lunge_camera extends AppCompatActivity {
    DatabaseHelper databaseHelper;
    Button finish_squat;
    int PERMISSION_REQUESTS = 1;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
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
    ProgressBar circularProgressBar;
    Bitmap bitmap4Save;
    ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    ArrayList<Bitmap> bitmap4DisplayArrayList = new ArrayList<>();
    ArrayList<Pose> poseArrayList = new ArrayList<>();
    boolean isRunning = false;
    List<Integer> kneeAngles = new ArrayList<>();
    List<Integer> kneelAngles = new ArrayList<>();
    List<Integer> seqState = new ArrayList<>();
    List<String> userFeedback = new ArrayList<>();
    TextToSpeech speak;
    boolean feedbackSpoken = false;
    boolean complete_exercise = false;
    int current_score=0;
    int incorrect_score=0;
    int correct_score=0;
    int right_current_score=0;
    int right_incorrect_score=0;
    int right_correct_score=0;
    int left_current_score=0;
    int left_incorrect_score=0;
    int left_correct_score=0;
    @ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lunge_camera);
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
        finish_squat= findViewById(R.id.finish);
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
                }            }
        }.start();
    }

    Runnable KeyPointsDetection = new Runnable() {
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
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
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
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_PINKY ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_PINKY ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_INDEX ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_INDEX ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_THUMB ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_THUMB ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_HEEL ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_HEEL ||
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
                    EditText goalright = findViewById(R.id.goalright);
                    EditText rightcorrect_scoree = findViewById(R.id.rightcorrect_score);
                    EditText rightincorrect_scoree = findViewById(R.id.rightincorrect_score);
                    EditText goalleft = findViewById(R.id.goalleft);
                    EditText leftcorrect_scoree = findViewById(R.id.leftcorrect_score);
                    EditText leftincorrect_scoree = findViewById(R.id.leftincorrect_score);
                    int goal = lastUserGoal.getgoal();

                    PoseLandmark elbowr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_ELBOW);
                    PoseLandmark wristr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_WRIST);
                    PoseLandmark shoulderr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                    PoseLandmark hipr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_HIP);
                    PoseLandmark kneer = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_KNEE);
                    PoseLandmark ankler = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_ANKLE);
                    PoseLandmark footr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
                    PoseLandmark elbowl = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
                    PoseLandmark wristl = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_WRIST);
                    PoseLandmark shoulderl = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                    PoseLandmark hipl = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_HIP);
                    PoseLandmark kneel = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_KNEE);
                    PoseLandmark anklel = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
                    PoseLandmark footl = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);

                    EditText errormessage = findViewById(R.id.errorEditText);
                    EditText ErrorKneeMessage = findViewById(R.id.kneeError);
                    EditText ErrorHipMessage = findViewById(R.id.hipError);

                    if (hipr != null && kneer != null && ankler != null && shoulderr != null && wristr != null
                            && elbowr != null&& footr != null) {
                        drawLineBetweenLandmarks(wristr, elbowr);
                        drawLineBetweenLandmarks(elbowr, shoulderr);
                        drawLineBetweenLandmarks(shoulderr, hipr);
                        drawLineBetweenLandmarks(hipr, kneer);
                        drawLineBetweenLandmarks(kneer, ankler);
                        drawLineBetweenLandmarks(ankler,footr);
                        drawLineBetweenLandmarks(shoulderr,shoulderl);
                        drawLineBetweenLandmarks(wristl,elbowl);
                        drawLineBetweenLandmarks(elbowl,shoulderl);
                        drawLineBetweenLandmarks(shoulderl,hipl);
                        drawLineBetweenLandmarks(hipl,kneel);
                        drawLineBetweenLandmarks(kneel,anklel);
                        drawLineBetweenLandmarks(anklel,footl);

                        float[] hipCoord = { hipr.getPosition().x, hipr.getPosition().y };
                        float[] kneeCoord = { kneer.getPosition().x, kneer.getPosition().y };
                        float[] ankleCoord = { ankler.getPosition().x, ankler.getPosition().y };
                        double kneeAngleDegrees = CalculateAngle(hipCoord, kneeCoord, ankleCoord);
                        int roundedKneeFlexionAngle = (int) Math.round(kneeAngleDegrees);
                        kneeAngles.add(roundedKneeFlexionAngle);
                        Collections.sort(kneeAngles);
                        int smallestKneeAngle = !kneeAngles.isEmpty() ? kneeAngles.get(0) : 0;

                        float[] hiplCoord = { hipl.getPosition().x, hipl.getPosition().y };
                         float[] kneelCoord = { kneel.getPosition().x, kneel.getPosition().y };
                        float[] anklelCoord = { anklel.getPosition().x, anklel.getPosition().y };
                        double kneelAngle = CalculateAngle(hiplCoord,kneelCoord, anklelCoord);
                        int roundedKneel = (int) Math.round(kneelAngle);
                        kneelAngles.add(roundedKneel);
                        Collections.sort(kneelAngles);
                        int smallestKneelAngle = !kneelAngles.isEmpty() ? kneelAngles.get(0) : 0;

                        int current_state = getState(roundedKneeFlexionAngle);
                        if (!seqState.contains(current_state)) {
                            seqState.add(current_state);
                        }
                        Collections.sort(seqState, Collections.reverseOrder());
                        CountRepetitions(smallestKneeAngle,smallestKneelAngle, current_state, complete_exercise);

                        goalEditText.setText("Goal: "+current_score+" / "+goal);

                        goalright.setText("Right Goal: "+right_current_score+" / "+goal);
                        rightcorrect_scoree.setText("Correct: "+right_correct_score);
                        rightincorrect_scoree.setText("InCorrect: "+right_incorrect_score);

                        goalleft.setText("Left Goal: "+left_current_score+" / "+goal);
                        leftcorrect_scoree.setText("Correct: "+left_correct_score);
                        leftincorrect_scoree.setText("InCorrect: "+left_incorrect_score);

                        if(current_score==goal && !feedbackSpoken){
                            finish_squat.setVisibility(View.VISIBLE);
                            speak.speak("You finish your exercise please press the button to see feedback about your moves",TextToSpeech.QUEUE_ADD,null);
                            feedbackSpoken = true;
                            complete_exercise=true;
                        }
                        finish_squat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String email = userEmail.toString();
                                String ex_name = lastUserGoal.getname().toString();
                                int goal = Integer.parseInt(goalEditText.getText().toString().split(" / ")[1]);
                                int rightcorrectScore = Integer.parseInt(rightcorrect_scoree.getText().toString().split(": ")[1]);
                                int leftcorrectScore = Integer.parseInt(leftcorrect_scoree.getText().toString().split(": ")[1]);
                                int correctScore = (rightcorrectScore+leftcorrectScore)/2;
                                int rightincorrectScore = Integer.parseInt(rightincorrect_scoree.getText().toString().split(": ")[1]);
                                int leftincorrectScore = Integer.parseInt(leftincorrect_scoree.getText().toString().split(": ")[1]);
                                int incorrectScore = (rightincorrectScore+leftincorrectScore)/2;
                                double accuracy = (double) correctScore / (correctScore + incorrectScore);
                                String workoutFeedback = TextUtils.join(", ", userFeedback);
                                SendMail(email, userName, ex_name, goal, correctScore, incorrectScore,
                                        rightcorrectScore, leftcorrectScore,rightincorrectScore, leftincorrectScore,accuracy, workoutFeedback);
                                boolean inserted = databaseHelper.insertuserfeedback(email, ex_name, goal, correctScore, incorrectScore, accuracy, workoutFeedback);
                                if (inserted) {
                                    Toast.makeText(view_lunge_camera.this, "you can see feedback on the exercise.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Feedback.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(view_lunge_camera.this, "Failed Inserted", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                        errormessage.setText("");
                        EditText Kneangle = findViewById(R.id.kneeangle);
                        Kneangle.setText("right Knee: "+smallestKneeAngle+" / "+roundedKneeFlexionAngle
                                +" / "+current_state
                                +" / "+seqState.get(0));
                        EditText hipangle = findViewById(R.id.hipangle);
                        hipangle.setText("left Knee: "+smallestKneelAngle+" /: "+roundedKneel);

                        DetectMovement(roundedKneeFlexionAngle,roundedKneel,hipr, kneer, hipl, kneel,
                                ErrorKneeMessage, ErrorHipMessage);

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
                    KeyPointsDetection.run();
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
    private void SendMail(String email, String userName,String ex_name, int goal, int correctScore, int incorrectScore,
                          int rightcorrectScore,int leftcorrectScore,int rightincorrectScore,int leftincorrectScore,
                          double accuracy, String workoutFeedback) {
        String subject = "Feedback about your training for this exercise" + ex_name;
        String message = "Dear, " + userName
                + "\nYou've finished your workout and here are our training notes for this workout"
                + "\nExercise Name: " + ex_name
                + "\nYour Goal: " + goal
                + "\nYour Correct repetition at right leg: " + rightcorrectScore
                + "\nYour Incorrect repetition at right leg: " + rightincorrectScore
                + "\nYour Correct repetition at left leg: " + leftcorrectScore
                + "\nYour Incorrect repetition at left leg: " + leftincorrectScore
                + "\nOur Feedback: " + workoutFeedback
                + "\nYour Accuracy: " + (accuracy*100)+"%"
                + "\nKeep going and do your best.";
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }
    private int getState(int kneeAngle) {
        int knee = 0;
        if (180 >= kneeAngle && kneeAngle >= 121) {
            knee = 1;
        } else if (120 >= kneeAngle && kneeAngle >= 50) {
            knee = 2;
        }
        return knee;
    }
    private void DetectMovement(int roundedKneeFlexionAngle, int roundedKneel,
                                PoseLandmark hipr, PoseLandmark kneer, PoseLandmark hipl, PoseLandmark kneel,
                                EditText ErrorKneeMessage, EditText ErrorleftkneeMessage) {
        // Update Knee Message
        if (roundedKneeFlexionAngle < 70) {
            ErrorKneeMessage.setText("right knee angle is too deep");
            drawErrorLineBetweenLandmarks(hipr, kneer);
        } else if (roundedKneeFlexionAngle > 120 && roundedKneeFlexionAngle < 160) {
            ErrorKneeMessage.setText("Lower Your Hip");
            drawErrorLineBetweenLandmarks(hipr, kneer);
        } else {
            ErrorKneeMessage.setText("");
            drawLineBetweenLandmarks(hipr, kneer);
        }
        // Update Knee Message
        if (roundedKneel < 70) {
            ErrorleftkneeMessage.setText("left knee angle is too deep");
            drawErrorLineBetweenLandmarks(hipl, kneel);
        } else if (roundedKneel > 120 && roundedKneel < 160 ) {
            ErrorleftkneeMessage.setText("Lower Your Hip");
            drawErrorLineBetweenLandmarks(hipl, kneel);
        } else {
            ErrorleftkneeMessage.setText("");
            drawLineBetweenLandmarks(hipl, kneel);
        }
    }
    private void CountRepetitions(int smallestKneeAngle, int smallestKneeLAngle,int current_state, boolean complete_exercise) {
        boolean leftcounted = true;
        boolean rightcounted = true;
        boolean counted = false;
        int rightprevious_score = right_current_score;
        int leftprevious_score = left_current_score;
        if ((seqState.get(0) == 2 && current_state == 1 && !complete_exercise)) {

            if (smallestKneeAngle < 70 && smallestKneeLAngle>70 &&rightcounted) {
                right_incorrect_score++;
                userFeedback.add("Your right knee angle is too deep.");
                speak.speak("This is an incorrect move because your right knee angle is too deep", TextToSpeech.QUEUE_FLUSH, null);
                right_current_score++;
                rightcounted=false;
                leftcounted = true;
                counted=true;

            } else if (smallestKneeAngle > 120 && smallestKneeLAngle<120 &&rightcounted) {
                right_incorrect_score++;
                userFeedback.add("Lower your hip to correct the right knee angle.");
                speak.speak("This is an incorrect move because Lower your hip to correct the right knee angle", TextToSpeech.QUEUE_FLUSH, null);
                right_current_score++;
                rightcounted=false;
                leftcounted = true;
                counted=true;
             } else if(smallestKneeAngle < 120 && smallestKneeAngle > 70 && smallestKneeLAngle>100 &&rightcounted) {
                right_correct_score++;
                speak.speak("This is a correct move", TextToSpeech.QUEUE_FLUSH, null);
                right_current_score++;
                rightcounted=false;
                leftcounted = true;
                counted=true;
            }

            if (smallestKneeLAngle < 70  && smallestKneeAngle>70 &&leftcounted) {
                left_incorrect_score++;
                left_current_score++;
                userFeedback.add("Your left knee angle is too deep.");
                speak.speak("This is an incorrect move because your left knee angle is too deep", TextToSpeech.QUEUE_FLUSH, null);
                leftcounted = false;
                rightcounted=true;
                counted=true;

            } else if (smallestKneeLAngle > 120 && smallestKneeAngle < 120 &&leftcounted) {
                left_incorrect_score++;
                left_current_score++;
                userFeedback.add("Lower your hip to correct the left knee angle.");
                speak.speak("This is an incorrect move because Lower your hip to correct the left knee angle", TextToSpeech.QUEUE_FLUSH, null);
                leftcounted = false;
                rightcounted=true;

            } else if(smallestKneeLAngle < 120 && smallestKneeLAngle > 70 && smallestKneeAngle>100 &&leftcounted){
                left_correct_score++;
                left_current_score++;
                speak.speak("This is a correct move", TextToSpeech.QUEUE_FLUSH, null);
                leftcounted = false;
                rightcounted=true;
                counted=true;

            }
            int right_new_score = right_current_score;
            int leftnew_score = left_current_score;
            if ((leftnew_score > leftprevious_score) || (right_new_score > rightprevious_score)) {
                // Clear all lists
                kneeAngles.clear();
                kneelAngles.clear();
                seqState.clear();
                seqState.add(0);
            }
            if(right_current_score>=0 && left_current_score>=0 && right_current_score==left_current_score&&counted){
                current_score++;
                counted=false;
            }
        }
    }
    public static double CalculateAngle(float[] point1, float[] point2, float[] point3) {
        PointF p1 = new PointF(point1[0], point1[1]);
        PointF p2 = new PointF(point2[0], point2[1]);
        PointF p3 = new PointF(point3[0], point3[1]);
        double angleInRad = Math.atan2(p3.y - p2.y, p3.x - p2.x)
                - Math.atan2(p1.y - p2.y, p1.x - p2.x);
        double angleInDeg = Math.abs(Math.toDegrees(angleInRad));
        angleInDeg = angleInDeg <= 180 ? angleInDeg : 360 - angleInDeg;

        return angleInDeg;
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