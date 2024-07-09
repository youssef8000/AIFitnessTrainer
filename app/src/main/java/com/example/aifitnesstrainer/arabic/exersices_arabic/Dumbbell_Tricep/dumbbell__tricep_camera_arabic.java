package com.example.aifitnesstrainer.arabic.exersices_arabic.Dumbbell_Tricep;

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
import android.media.MediaPlayer;
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
import com.example.aifitnesstrainer.UserFeedback;
import com.example.aifitnesstrainer.arabic.Feedback_arabic;
import com.example.aifitnesstrainer.arabic.exersices_arabic.Lateral_raise.view_LateralRaise_camera_arabic;
import com.example.aifitnesstrainer.exersices.Dumbbell_Tricep.dumbbell__tricep_camera;
import com.example.aifitnesstrainer.exersices.Lateral_raise.view_LateralRaise_camera;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class dumbbell__tricep_camera_arabic extends AppCompatActivity {
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
    ProgressBar circularProgressBar;
    TextToSpeech speak;
    boolean feedbackSpoken = false;
    boolean complete_exercise = false;

    Bitmap bitmap4Save;
    ArrayList<Bitmap> bitmapArrayList = new ArrayList<>();
    ArrayList<Bitmap> bitmap4DisplayArrayList = new ArrayList<>();
    ArrayList<Pose> poseArrayList = new ArrayList<>();
    boolean isRunning = false;
    List<Integer> elbow_RAngles = new ArrayList<>();
    List<Integer> elbow_LAngles = new ArrayList<>();
    List<Integer> hip_shoulder_RAngles = new ArrayList<>();
    List<Integer> hip_shoulder_LAngles = new ArrayList<>();
    List<Integer> lateralRightState = new ArrayList<>();
    List<Integer> lateralLeftState = new ArrayList<>();
    List<String> userFeedback = new ArrayList<>();
    List<String> userFeedback_arabic = new ArrayList<>();

    int current_score=0;
    int incorrect_score=0;
    int correct_score=0;
    MediaPlayer player;
    @ExperimentalGetImage
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dumbbell_tricep_camera_arabic);
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
                player= MediaPlayer.create(dumbbell__tricep_camera_arabic.this,R.raw.start_training);
                player.start() ;
                cameraProviderFuture.addListener(() -> {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        bindPreview(cameraProvider);
                    } catch (ExecutionException | InterruptedException e) {
                        // No errors need to be handled for this Future.
                    }
                }, ContextCompat.getMainExecutor(getApplicationContext()));
                if (!allPermissionsGranted()) {
                    getRuntimePermissions();
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

                    PoseLandmark elbowr13 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_ELBOW);
                    PoseLandmark wristr15 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_WRIST);
                    PoseLandmark shoulderr11 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
                    PoseLandmark elbowr14 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
                    PoseLandmark wristr16 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_WRIST);
                    PoseLandmark shoulderr12 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
                    PoseLandmark hipr23 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.LEFT_HIP);
                    PoseLandmark hipr24 = poseArrayList.get(0).getPoseLandmark(PoseLandmark.RIGHT_HIP);

                    EditText errormessage = findViewById(R.id.errorEditText);
                    EditText ErrorElbowMessage = findViewById(R.id.elboeError);
                    EditText ErrorHipMessage = findViewById(R.id.hipError);

                    if (shoulderr12 != null && elbowr14 != null && wristr16 != null && shoulderr11 != null && elbowr13 != null
                            && wristr15 != null &&hipr23 !=null && hipr24 !=null) {
                        drawLineBetweenLandmarks(shoulderr12, elbowr14);
                        drawLineBetweenLandmarks(elbowr14, wristr16);
                        drawLineBetweenLandmarks(shoulderr11, elbowr13);
                        drawLineBetweenLandmarks(elbowr13, wristr15);
                        drawLineBetweenLandmarks(shoulderr12, hipr24);
                        drawLineBetweenLandmarks(shoulderr11, hipr23);
                        drawLineBetweenLandmarks(shoulderr11, shoulderr12);
                        drawLineBetweenLandmarks(hipr23, hipr24);

//---------------------------------------------------------------------------------------------------------------
//                      äÌíÈ ÇáÒæÇíÇ
                        Point leftHipCoord = new Point(hipr23.getPosition().x, hipr23.getPosition().y);
                        Point leftWristCoord = new Point(wristr15.getPosition().x, wristr15.getPosition().y);
                        Point leftShldrCoord = new Point(shoulderr11.getPosition().x, shoulderr11.getPosition().y);
                        Point leftelbowCoord = new Point(elbowr13.getPosition().x, elbowr13.getPosition().y);

                        Point rightHipCoord = new Point(hipr24.getPosition().x, hipr24.getPosition().y);
                        Point rightWristCoord = new Point(wristr16.getPosition().x, wristr16.getPosition().y);
                        Point rightShldrCoord = new Point(shoulderr12.getPosition().x, shoulderr12.getPosition().y);
                        Point rightelbowCoord = new Point(elbowr14.getPosition().x, elbowr14.getPosition().y);

                        double leftHipWristShldrAngle = findAngle(leftHipCoord, leftWristCoord, leftShldrCoord);
                        double rightHipWristShldrAngle  = findAngle(rightHipCoord, rightWristCoord, rightShldrCoord);
                        double leftshldrwristelbowangle  = findAngle(leftShldrCoord, leftWristCoord, leftelbowCoord);
                        double rightshldrwristelbowangle = findAngle(rightShldrCoord, rightWristCoord, rightelbowCoord);
//---------------------------------------------------------------------------------------------------------------

                        int shoulderR = (int) Math.round(rightHipWristShldrAngle);
                        int shoulderl = (int) Math.round(leftHipWristShldrAngle);
                        int elbow_angleR = (int) Math.round(rightshldrwristelbowangle);
                        int elbow_angleL = (int) Math.round(leftshldrwristelbowangle);

                        elbow_RAngles.add(elbow_angleR);
                        elbow_LAngles.add(elbow_angleL);
                        hip_shoulder_RAngles.add(shoulderR);
                        hip_shoulder_LAngles.add(shoulderl);

                        Collections.sort(elbow_RAngles);
                        int greatestelbow_RAngle = !elbow_RAngles.isEmpty() ? elbow_RAngles.get(0) : 0;

                        Collections.sort(elbow_LAngles);
                        int greatestelbow_LAngle = !elbow_LAngles.isEmpty() ? elbow_LAngles.get(0) : 0;

                        int current_state_Right = getStateRight(elbow_angleL);
                        int current_state_Left = getStateLeft(elbow_angleR);

                        if (!lateralRightState.contains(current_state_Right)) {
                            lateralRightState.add(current_state_Right);
                        }
                        Collections.sort(lateralRightState, Collections.reverseOrder());

                        if (!lateralLeftState.contains(current_state_Left)) {
                            lateralLeftState.add(current_state_Left);
                        }
                        Collections.sort(lateralLeftState, Collections.reverseOrder());

                        if((lateralRightState.get(0)==3 && current_state_Right==1
                                &&lateralLeftState.get(0)==3 && current_state_Left==1 && !complete_exercise)
                                ||(lateralRightState.get(0)==2 && current_state_Right==1
                                &&lateralLeftState.get(0)==2 && current_state_Left==1 && !complete_exercise)){

                            int previous_score=current_score;
                            current_score++;
                            int new_score=current_score;

                            if(greatestelbow_LAngle< 150 && greatestelbow_LAngle > 80 && shoulderR > 140){
                                incorrect_score++;
                                userFeedback.add("Your Left Elbow angle is wrong,because your Left hand is raised");
                                userFeedback_arabic.add("زاوية كوعك الأيسر خاطئة لأن يدك اليسرى مرفوعة");

                                player=MediaPlayer.create(dumbbell__tricep_camera_arabic.this,R.raw.incorrect_move);
                                player.start();
                            } else if (greatestelbow_RAngle<150 && greatestelbow_RAngle > 80 && shoulderl > 140) {
                                incorrect_score++;
                                userFeedback.add("Your Right Elbow angle is wrong,because your Right hand is raised");
                                userFeedback_arabic.add("زاوية كوعك الأيمن خاطئة لأن يدك اليمنى مرفوعة");
                                player=MediaPlayer.create(dumbbell__tricep_camera_arabic.this,R.raw.incorrect_move);
                                player.start();
                            } else if(greatestelbow_LAngle < 40 && shoulderR > 140){
                                incorrect_score++;
                                userFeedback.add("Your Left Elbow angle is wrong,because your Left hand is too low");
                                userFeedback_arabic.add("زاوية كوعك الأيسر خاطئة لأن يدك اليسرى منخفضة جدًا");
                                player=MediaPlayer.create(dumbbell__tricep_camera_arabic.this,R.raw.incorrect_move);
                                player.start();
                            } else if (greatestelbow_RAngle < 40 && shoulderl > 140) {
                                incorrect_score++;
                                userFeedback.add("Your Right Elbow angle is wrong,because your Right hand is too low");
                                userFeedback_arabic.add("زاوية كوعك الأيمن خاطئة لأن يدك اليمنى منخفضة جدًا");
                                player=MediaPlayer.create(dumbbell__tricep_camera_arabic.this,R.raw.incorrect_move);
                                player.start();
                            }
                            else{
                                correct_score++;
                                player=MediaPlayer.create(dumbbell__tricep_camera_arabic.this,R.raw.correct_move);
                                player.start();
                            }

                            if (new_score >previous_score) {
                                // Clear all lists
                                elbow_LAngles.clear();
                                elbow_RAngles.clear();
                                hip_shoulder_LAngles.clear();
                                hip_shoulder_RAngles.clear();
                                lateralRightState.clear();
                                lateralLeftState.clear();
                                lateralRightState.add(0);
                                lateralLeftState.add(0);
                            }
                        }

                        goalEditText.setText("هدفك: "+current_score+" / "+goal);
                        correct_scoree.setText("الأعداد الصحيحه: "+correct_score);
                        incorrect_scoree.setText("الأعداد الخاطئه: "+incorrect_score);

                        if(current_score==goal && !feedbackSpoken){

                            player=MediaPlayer.create(dumbbell__tricep_camera_arabic.this,R.raw.check_feedback);
                            player.start();
                            feedbackSpoken = true;
                            complete_exercise=true;
                            String email = userEmail.toString();
                            String ex_name = lastUserGoal.getname().toString();
                            int goall = Integer.parseInt(goalEditText.getText().toString().split(" / ")[1]);
                            int correctScore = Integer.parseInt(correct_scoree.getText().toString().split(": ")[1]);
                            int incorrectScore = Integer.parseInt(incorrect_scoree.getText().toString().split(": ")[1]);
                            double accuracy = (double) correctScore / (correctScore + incorrectScore);
                            String workoutFeedback = TextUtils.join(", ", userFeedback);
                            String workoutFeedback_arabic = TextUtils.join(", ", userFeedback_arabic);
                            SendMail(email, userName, ex_name, goall, correctScore, incorrectScore, accuracy, workoutFeedback_arabic);
                            boolean inserted = databaseHelper.insertuserfeedback(email, ex_name, goall, correctScore, incorrectScore, accuracy, workoutFeedback);
                            if (inserted) {
                                Toast.makeText(dumbbell__tricep_camera_arabic.this, "تستطيع أن ترى ملاحظتنا على التمرين.", Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(getApplicationContext(), Feedback_arabic.class);
                                        startActivity(intent);
                                    }
                                }, 2000); // 2000 milliseconds = 2 seconds delay
                            } else {
                                Toast.makeText(dumbbell__tricep_camera_arabic.this, "Failed Inserted", Toast.LENGTH_SHORT).show();
                            }
                        }
                        finish_letralRaise.setOnClickListener(new View.OnClickListener() {
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
                                    Toast.makeText(dumbbell__tricep_camera_arabic.this, "تستطيع أن ترى ملاحظتنا على التمرين.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), Feedback_arabic.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(dumbbell__tricep_camera_arabic.this, "Failed Inserted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        errormessage.setText("");

                        if(elbow_angleR < 40){
                            ErrorElbowMessage.setText("ارفع يدك اليسرى");
                            drawErrorLineBetweenLandmarks(elbowr14, wristr16);
                        } else if (elbow_angleR <150 && elbow_angleR > 80) {
                            ErrorElbowMessage.setText("اخفض يدك اليسرى");
                            drawErrorLineBetweenLandmarks(elbowr14, wristr16);
                        } else {
                            drawLineBetweenLandmarks(elbowr14, wristr16);
                            ErrorElbowMessage.setText("");
                        }

                        if (elbow_angleL < 40) {
                            ErrorHipMessage.setText("ارفع يدك اليمنى");
                            drawErrorLineBetweenLandmarks(elbowr13, wristr15);
                        }else if (elbow_angleL <150 && elbow_angleL > 80) {
                            ErrorHipMessage.setText("اخفض يدك اليمنى");
                            drawErrorLineBetweenLandmarks(elbowr13, wristr15);
                        } else {
                            drawLineBetweenLandmarks(elbowr13, wristr15);
                            ErrorHipMessage.setText("");
                        }

                    } else {
                        errormessage.setText("بعض العلامات البارزة مفقودة لاكتشاف Dumbbell Tricep");
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
        String subject = "ملاحظاتنا حول تدريبك لهذا التمرين";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("ar"));
        DecimalFormat df = new DecimalFormat("#.#", symbols);
        String message = "عزيزي " + userName
                + "\nلقد انتهيت من تمرينك وهذه ملاحظاتنا حول التدريب"
                + "\nاسم التمرين: " + ex_name
                + "\nهدفك: " + goal
                + "\nعدد التكرارات الصحيحة: " + correctScore
                + "\nعدد التكرارات الخاطئة: " + incorrectScore
                + "\nملاحظاتنا: " + workoutFeedback
                + "\nدقتك: " + df.format(accuracy*100) + "%"
                + "\nاستمر وحافظ على أدائك الجيد.";
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }
    private int getStateRight(int shoulderRAngle) {
        int shoulderRight = 0;
        if ( shoulderRAngle < 180&&shoulderRAngle >= 130) {
            shoulderRight = 1;
        } else if (shoulderRAngle < 130 && shoulderRAngle >=80) {
            shoulderRight = 2;
        } else if ( shoulderRAngle <80&&shoulderRAngle >20) {
            shoulderRight = 3;
        }
        return shoulderRight;
    }
    private int getStateLeft(int shoulderLAngle) {
        int shoulderLeft = 0;
        if ( shoulderLAngle <180 && shoulderLAngle>=130) {
            shoulderLeft = 1;
        } else if ( shoulderLAngle< 130  && shoulderLAngle>=80) {
            shoulderLeft = 2;
        } else if ( shoulderLAngle<80 && shoulderLAngle>20) {
            shoulderLeft = 3;
        }
        return shoulderLeft;
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