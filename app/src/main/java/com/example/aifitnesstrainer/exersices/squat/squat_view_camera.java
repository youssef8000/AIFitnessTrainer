package com.example.aifitnesstrainer.exersices.squat;

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

public class squat_view_camera extends AppCompatActivity {
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
    List<Integer> hipAngles = new ArrayList<>();
    List<Integer> ankleAngles = new ArrayList<>();
    List<Integer> seqState = new ArrayList<>();
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
        setContentView(R.layout.activity_squat_view_camera);
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
                        ImageAnalysis(cameraProvider);
                    } catch (ExecutionException | InterruptedException e) {
                        // No errors need to be handled for this Future.
                    }
                }, ContextCompat.getMainExecutor(getApplicationContext()));
                if (!allPermissionsGranted()) {
                    OpenCamera();
                }            }
        }.start();
    }
    Runnable poseDetection  = new Runnable() {
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
    void ImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
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

                        double[] noseCoord = { poseArrayList.get(0).getPoseLandmark(PoseLandmark.NOSE).getPosition().x,
                                poseArrayList.get(0).getPoseLandmark(PoseLandmark.NOSE).getPosition().y };
                        double[] shoulderLCoord = { poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().x,
                                poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().y };

                        double kneeAngleDegrees = CalculateAngle(hipCoord, new double[]{ kneeCoord[0], 0 }, kneeCoord);
                        double hipAngleDegrees = CalculateAngle(shoulderCoord, new double[]{ hipCoord[0], 0 }, hipCoord);
                        double ankleAngleDegrees = CalculateAngle(kneeCoord, new double[]{ ankleCoord[0], 0 }, ankleCoord);
                        double nose_angle=CalculateAngle(shoulderLCoord,shoulderCoord,noseCoord);

                        int roundedKneeFlexionAngle = (int) Math.round(kneeAngleDegrees);
                        int roundedHipFlexionAngle = (int) Math.round(hipAngleDegrees);
                        int roundedAnkleDorsiflexionAngle = (int) Math.round(ankleAngleDegrees);

                        int nosey = (int) Math.round(nose_angle);

                        EditText nosemessage=findViewById(R.id.errornose);
                        if (nosey>35)
                        {
                            nosemessage.setText("The camera is not aligned correctly, please stand to the right side of the camera.");
                        }else {
                            nosemessage.setText("");
                        }

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
                        CountRepetitions(greatestKneeAngle, greatestHipAngle, greatestAnkleAngle, current_state, complete_exercise);

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
                            SendMail(email, userName, ex_name, goall, correctScore, incorrectScore, accuracy, workoutFeedback);
                            boolean inserted = databaseHelper.insertuserfeedback(email, ex_name, goall, correctScore, incorrectScore, accuracy, workoutFeedback);
                            if (inserted) {
                                Toast.makeText(squat_view_camera.this, "you can see feedback on the exercise.", Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(getApplicationContext(), Feedback.class);
                                        startActivity(intent);
                                    }
                                }, 2000); // 2000 milliseconds = 2 seconds delay
                            } else {
                                Toast.makeText(squat_view_camera.this, "Failed Inserted", Toast.LENGTH_SHORT).show();
                            }
                        }

                        errormessage.setText("");
                        DetectMovement(roundedKneeFlexionAngle, roundedHipFlexionAngle, roundedAnkleDorsiflexionAngle,
                                hipr, kneer, shoulderr, ankler,
                                ErrorKneeMessage, ErrorHipMessage, ErrorAnkleMessage);

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
                    poseDetection .run();
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
    private void DetectMovement(int roundedKneeFlexionAngle, int roundedHipFlexionAngle, int roundedAnkleDorsiflexionAngle,
                                PoseLandmark hipr, PoseLandmark kneer, PoseLandmark shoulderr, PoseLandmark ankler,
                                EditText ErrorKneeMessage, EditText ErrorHipMessage, EditText ErrorAnkleMessage) {
        // Update Knee Message
        if (roundedKneeFlexionAngle > 95) {
            ErrorKneeMessage.setText("Squat To Deep");
            drawErrorLineBetweenLandmarks(hipr, kneer);
        } else if (roundedKneeFlexionAngle > 50 && roundedKneeFlexionAngle < 80) {
            ErrorKneeMessage.setText("Lower Your Hip");
            drawErrorLineBetweenLandmarks(hipr, kneer);
        } else {
            ErrorKneeMessage.setText("");
            drawLineBetweenLandmarks(hipr, kneer);
        }
        // Update Hip Message
        if (roundedHipFlexionAngle > 45) {
            ErrorHipMessage.setText("Bend Backward");
            drawErrorLineBetweenLandmarks(shoulderr, hipr);
        } else if (roundedHipFlexionAngle < 20 && roundedHipFlexionAngle > 10) {
            ErrorHipMessage.setText("Bend Forward");
            drawErrorLineBetweenLandmarks(shoulderr, hipr);
        } else {
            ErrorHipMessage.setText("");
            drawLineBetweenLandmarks(shoulderr, hipr);
        }
        // Update Ankle Message
        if (roundedAnkleDorsiflexionAngle > 40) {
            ErrorAnkleMessage.setText("Knee Falling Over Toe");
            drawErrorLineBetweenLandmarks(kneer, ankler);
        } else {
            ErrorAnkleMessage.setText("");
            drawLineBetweenLandmarks(kneer, ankler);
        }
    }
    private void CountRepetitions(int greatestKneeAngle, int greatestHipAngle, int greatestAnkleAngle,
                                  int current_state, boolean complete_exercise) {
        if ((seqState.get(0) == 3 && current_state == 1 && !complete_exercise) ||
                (seqState.get(0) == 2 && current_state == 1 && !complete_exercise)) {
            int previous_score = current_score;
            current_score++;
            int new_score = current_score;
            if (greatestKneeAngle > 95) {
                incorrect_score++;
                userFeedback.add("Your knee angle is too deep.");
                speak.speak("This is an incorrect move because your knee angle is too deep", TextToSpeech.QUEUE_FLUSH, null);
            } else if (greatestKneeAngle > 50 && greatestKneeAngle < 80) {
                incorrect_score++;
                userFeedback.add("Lower your hip to correct the knee angle.");
                speak.speak("This is an incorrect move because your hip is higher than your knee", TextToSpeech.QUEUE_FLUSH, null);
            } else if (greatestHipAngle > 45) {
                incorrect_score++;
                userFeedback.add("You are bending forward.");
                speak.speak("This is an incorrect move because You are bending forward", TextToSpeech.QUEUE_FLUSH, null);
            } else if (greatestHipAngle < 20 && greatestHipAngle > 10) {
                incorrect_score++;
                userFeedback.add("You are bending backward.");
                speak.speak("This is an incorrect move because You are bending backward", TextToSpeech.QUEUE_FLUSH, null);
            } else if (greatestAnkleAngle > 40) {
                incorrect_score++;
                userFeedback.add("Your knee is falling over your toe.");
                speak.speak("This is an incorrect move because Your knee is falling over your toe", TextToSpeech.QUEUE_FLUSH, null);
            } else {
                correct_score++;
                speak.speak("This is a correct move", TextToSpeech.QUEUE_FLUSH, null);
            }

            if (new_score > previous_score) {
                // Clear all lists
                kneeAngles.clear();
                hipAngles.clear();
                ankleAngles.clear();
                seqState.clear();
                seqState.add(0);
            }
        }
    }
    public static double CalculateAngle(double[] p1, double[] p2, double[] refPt) {
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