package com.example.aifitnesstrainer.exersices.ShoulderPress;

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
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.aifitnesstrainer.DatabaseHelper;
import com.example.aifitnesstrainer.Display;
import com.example.aifitnesstrainer.Feedback;
import com.example.aifitnesstrainer.JavaMailAPI;
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
import com.example.aifitnesstrainer.R;
import org.opencv.core.Point;

public class ShoulderPress_camera extends AppCompatActivity {

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
    List<Integer> rightShoulderAngles = new ArrayList<>();
    List<Integer> leftShoulderAngles = new ArrayList<>();
    List<Integer> rightElbowAngles = new ArrayList<>();
    List<Integer> leftElbowAngles = new ArrayList<>();
    List<Integer> seqStateLeft = new ArrayList<>();
    List<Integer> seqStateRight = new ArrayList<>();
    List<String> userFeedback = new ArrayList<>();
    TextToSpeech speak;
    boolean feedbackSpoken = false;
    boolean complete_exercise = false;
    int current_score=0;
    int incorrect_score=0;
    int correct_score=0;
    @ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shoulder_press_camera);
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
                    getRuntimePermissions();
                }            }
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
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_KNEE ||
                                poseLandmark.getLandmarkType() == PoseLandmark.RIGHT_KNEE ||
                                poseLandmark.getLandmarkType() == PoseLandmark.LEFT_ANKLE ||
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

                    // For the Left side
                    PoseLandmark elbow = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_ELBOW);
                    PoseLandmark wrist = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_WRIST);
                    PoseLandmark shoulder = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                    PoseLandmark hip = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_HIP);

                    // For the right side
                    PoseLandmark elbowr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
                    PoseLandmark wristr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_WRIST);
                    PoseLandmark shoulderr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                    PoseLandmark hipr = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_HIP);

                    EditText errormessage = findViewById(R.id.errorEditText);
                    EditText ErrorelbowLMessage = findViewById(R.id.shoulderError);
                    EditText ErrorelbowRMessage = findViewById(R.id.hipError);
                    EditText ErrorShoulderLMessage = findViewById(R.id.elbowError);
                    EditText ErrorShoulderRMessage = findViewById(R.id.wristError);

                    if (shoulder != null && elbow != null && wrist != null
                            && shoulderr != null && elbowr != null && wristr != null && hip != null && hipr != null) {
                        // Draw lines for the left side
                        drawLineBetweenLandmarks(wrist, elbow);
                        drawLineBetweenLandmarks(elbow, shoulder);
                        drawLineBetweenLandmarks(shoulder, hip);
                        // Draw lines for the right side
                        drawLineBetweenLandmarks(wristr, elbowr);
                        drawLineBetweenLandmarks(elbowr, shoulderr);
                        drawLineBetweenLandmarks(shoulderr, hipr);

                        drawLineBetweenLandmarks(shoulderr, shoulder);
                        drawLineBetweenLandmarks(hip, hipr);

                        Point leftHipCoord = new Point(hipr.getPosition().x, hipr.getPosition().y);
                        Point leftWristCoord = new Point(wristr.getPosition().x, wristr.getPosition().y);
                        Point leftShldrCoord = new Point(shoulderr.getPosition().x, shoulderr.getPosition().y);
                        Point leftelbowCoord = new Point(elbowr.getPosition().x, elbowr.getPosition().y);

                        Point rightHipCoord = new Point(hip.getPosition().x, hip.getPosition().y);
                        Point rightWristCoord = new Point(wrist.getPosition().x, wrist.getPosition().y);
                        Point rightShldrCoord = new Point(shoulder.getPosition().x, shoulder.getPosition().y);
                        Point rightelbowCoord = new Point(elbow.getPosition().x, elbow.getPosition().y);

                        double leftHipWristShldrAngle = findAngle(leftHipCoord, leftWristCoord, leftShldrCoord);
                        double rightHipWristShldrAngle  = findAngle(rightHipCoord, rightWristCoord, rightShldrCoord);
                        double leftshldrwristelbowangle  = findAngle(leftShldrCoord, leftWristCoord, leftelbowCoord);
                        double rightshldrwristelbowangle = findAngle(rightShldrCoord, rightWristCoord, rightelbowCoord);

                        int shoulderR = (int) Math.round(rightHipWristShldrAngle);
                        int shoulderl = (int) Math.round(leftHipWristShldrAngle);
                        int elbow_angleR = (int) Math.round(rightshldrwristelbowangle);
                        int elbow_angleL = (int) Math.round(leftshldrwristelbowangle);


                        EditText errorposition=findViewById(R.id.errorposition);
                        if (shoulderR<90 &&shoulderl<90)
                        {
                            errorposition.setText("Please raise your hand to make your elbow and shoulder in a straight line to take the correct position for the exercise.");
                        }else {
                            errorposition.setText("");
                        }

                        leftShoulderAngles.add(shoulderl);
                        leftElbowAngles.add(elbow_angleL);
                        rightShoulderAngles.add(shoulderR);
                        rightElbowAngles.add(elbow_angleR);

                        // For the left side
                        Collections.sort(rightElbowAngles, Collections.reverseOrder());
                        int greatestelbow_RAngle = !rightElbowAngles.isEmpty() ? rightElbowAngles.get(0) : 0;

                        Collections.sort(leftElbowAngles, Collections.reverseOrder());
                        int greatestelbow_LAngle = !leftElbowAngles.isEmpty() ? leftElbowAngles.get(0) : 0;

                        Collections.sort(rightShoulderAngles, Collections.reverseOrder());
                        int greatesthip_shoulder_RAngle = !rightShoulderAngles.isEmpty() ? rightShoulderAngles.get(0) : 0;

                        Collections.sort(leftShoulderAngles, Collections.reverseOrder());
                        int greatesthip_shoulder_LAngle = !leftShoulderAngles.isEmpty() ? leftShoulderAngles.get(0) : 0;

                        int currentStateLeft = getStateLeft(elbow_angleL,shoulderl);
                        int currentStateRight = getStateRight(elbow_angleR,shoulderR);
                        if (!seqStateLeft.contains(currentStateLeft) ) {
                            seqStateLeft.add(currentStateLeft);
                        }
                        Collections.sort(seqStateLeft, Collections.reverseOrder());

                        if (!seqStateRight.contains(currentStateLeft) ) {
                            seqStateRight.add(currentStateRight);
                        }
                        Collections.sort(seqStateRight, Collections.reverseOrder());

                        // For the left side
                        if ((seqStateLeft.get(0) == 3 && currentStateLeft == 1 && !complete_exercise
                                &&seqStateRight.get(0)==3 && currentStateLeft==1) ||
                                (seqStateLeft.get(0) == 2 && currentStateLeft == 1 && !complete_exercise
                                        &seqStateRight.get(0)==2 && currentStateLeft==1 )) {

                            int previous_score = current_score;
                            current_score++;
                            if (greatesthip_shoulder_LAngle < 170 || greatesthip_shoulder_LAngle < 140) {
                                incorrect_score++;
                                userFeedback.add("Keep your shoulders stable and avoid excessive arching or shrugging.");
                                speak.speak("This is an incorrect move because your left shoulder position is incorrect.", TextToSpeech.QUEUE_FLUSH, null);
                            }else if (greatesthip_shoulder_RAngle < 170 || greatesthip_shoulder_RAngle < 140) {
                                incorrect_score++;
                                userFeedback.add("Keep your shoulders stable and avoid excessive arching or shrugging.");
                                speak.speak("This is an incorrect move because your right shoulder position is incorrect.", TextToSpeech.QUEUE_FLUSH, null);
                            }
                            else if ((greatestelbow_LAngle < 155 && shoulderl>130) || (greatestelbow_LAngle < 85 && shoulderl>130)) {
                                incorrect_score++;
                                userFeedback.add("Keep your elbows close to your body and don't lock them out at the top.");
                                speak.speak("This is an incorrect move because your left elbow position is incorrect.", TextToSpeech.QUEUE_FLUSH, null);
                            } else if ((greatestelbow_RAngle < 155 && shoulderR>130) || (greatestelbow_RAngle < 85 && shoulderR>130)) {
                                incorrect_score++;
                                userFeedback.add("Keep your elbows close to your body and don't lock them out at the top.");
                                speak.speak("This is an incorrect move because your right elbow position is incorrect.", TextToSpeech.QUEUE_FLUSH, null);
                            }else {
                                correct_score++;
                                speak.speak("This is a correct move.", TextToSpeech.QUEUE_FLUSH, null);
                            }
                            int new_score = current_score;

                            if (new_score >previous_score) {
                                leftShoulderAngles.clear();
                                rightShoulderAngles.clear();
                                rightElbowAngles.clear();
                                leftElbowAngles.clear();
                                seqStateLeft.clear();
                                seqStateLeft.add(0);
                                seqStateRight.clear();
                                seqStateRight.add(0);
                            }
                        }

                        goalEditText.setText("Goal: "+current_score+" / "+goal);
                        correct_scoree.setText("Correct: "+correct_score);
                        incorrect_scoree.setText("InCorrect: "+incorrect_score);
                        if(current_score==goal && !feedbackSpoken){
//                            finish_squat.setVisibility(View.VISIBLE);
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
                                sendMail(email, userName, ex_name, goall, correctScore, incorrectScore, accuracy, workoutFeedback);
                            boolean inserted = databaseHelper.insertuserfeedback(email, ex_name, goall, correctScore, incorrectScore, accuracy, workoutFeedback);
                            if (inserted) {
                                Toast.makeText(ShoulderPress_camera.this, "you can see feedback on the exercise.", Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(getApplicationContext(), Feedback.class);
                                        startActivity(intent);
                                    }
                                }, 2000); // 2000 milliseconds = 2 seconds delay
                            } else {
                                Toast.makeText(ShoulderPress_camera.this, "Failed Inserted", Toast.LENGTH_SHORT).show();
                            }

                        }
                        errormessage.setText("");

                        // Update ElbowLeft Message
                        if (elbow_angleL < 155 && elbow_angleL > 140 ) {
                            ErrorelbowLMessage.setText("");
                            drawErrorLineBetweenLandmarks(wrist, elbow);
                            ErrorelbowLMessage.setText("Avoid excessive Elbow flexion");
                        } else if (elbow_angleL < 80 ) {
                            ErrorelbowLMessage.setText("");
                            drawErrorLineBetweenLandmarks(wrist, elbow);
                            ErrorelbowLMessage.setText("Increase Elbow flexion");
                        } else {
                            // If the Elbow angle is within acceptable range
                            drawLineBetweenLandmarks(wrist, elbow);
                            ErrorelbowLMessage.setText("");
                        }

                        // Update ElbowRight Message
                        if (elbow_angleR < 155 && elbow_angleR > 140) {
                            ErrorelbowRMessage.setText("");
                            drawErrorLineBetweenLandmarks(wristr, elbowr);
                            ErrorelbowRMessage.setText("Avoid excessive Elbow flexion");
                        } else if ( elbow_angleR < 80 ) {
                            // If the Elbow angle indicates insufficient shoulder flexion
                            ErrorelbowRMessage.setText("");
                            drawErrorLineBetweenLandmarks(wristr, elbowr);
                            ErrorelbowRMessage.setText("Increase Elbow flexion");
                        } else {
                            drawLineBetweenLandmarks(wristr, elbowr);
                            ErrorelbowRMessage.setText("");
                        }

                        // Update ShoulderLeft Message
                        if (shoulderl < 170 && shoulderl > 150) {
                            // If the shoulder angle indicates excessive shoulder flexion
                            ErrorShoulderLMessage.setText("");
                            drawErrorLineBetweenLandmarks(elbow, shoulder);
                            ErrorShoulderLMessage.setText("Avoid excessive shoulder flexion");
                        } else if ( shoulderl < 135 && shoulderl > 50 ) {
                            // If the Elbow angle indicates insufficient shoulder flexion
                            ErrorShoulderLMessage.setText("");
                            drawErrorLineBetweenLandmarks(elbow, shoulder);
                            ErrorShoulderLMessage.setText("Increase shoulder flexion");
                        } else {
                            drawLineBetweenLandmarks(elbow, shoulder);
                            ErrorShoulderLMessage.setText("");
                        }

                        // Update ShoulderRight Message
                        if (shoulderR < 170 && shoulderR > 150 ) {
                            ErrorShoulderRMessage.setText("");
                            drawErrorLineBetweenLandmarks(elbowr, shoulderr);
                            ErrorShoulderRMessage.setText("Avoid excessive shoulder flexion");
                        } else if ( shoulderR < 135 && shoulderR > 50 ) {
                            ErrorShoulderRMessage.setText("");
                            drawErrorLineBetweenLandmarks(elbowr, shoulderr);
                            ErrorShoulderRMessage.setText("Increase shoulder flexion");
                        } else {
                            drawLineBetweenLandmarks(elbowr, shoulderr);
                            ErrorShoulderRMessage.setText("");
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
    private void sendMail(String email, String userName,String ex_name, int goal, int correctScore, int incorrectScore, double accuracy, String workoutFeedback) {
        String subject = "Feedback about your training for this exercise";
        String message = "Dear, " + userName
                + "\nYou've finished your workout and here are our training notes for this workout"
                + "\nExercise Name: " + ex_name
                + "\nYour Goal: " + goal
                + "\nYour Correct repetition: " + correctScore
                + "\nYour Incorrect repetition: " + incorrectScore
                + "\nOur Feedback: " + workoutFeedback
                + "\nYour Accuracy: " + accuracy
                + "\nKeep going and do your best.";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }
    private int getStateRight(int elbow_angleR , int shoulder_angleR) {
        int elbowRight = 0;
        if ( elbow_angleR >=80 && elbow_angleR <120 && shoulder_angleR>130) {
            elbowRight = 1;
        } else if (elbow_angleR >=120 && elbow_angleR <150 && shoulder_angleR>130) {
            elbowRight = 2;
        } else if ( elbow_angleR >=150 && elbow_angleR <180 && shoulder_angleR>130) {
            elbowRight = 3;
        }
        return elbowRight;
    }
    private int getStateLeft(int elbow_angleL , int shoulder_angleL) {
        int elbowLeft = 0;
        if ( elbow_angleL>=80 && elbow_angleL<120 && shoulder_angleL>130) {
            elbowLeft = 1;
        } else if ( elbow_angleL>=120 && elbow_angleL<150 && shoulder_angleL>130) {
            elbowLeft = 2;
        } else if ( elbow_angleL>=150 && elbow_angleL<180 && shoulder_angleL>130) {
            elbowLeft = 3;
        }
        return elbowLeft;
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
}