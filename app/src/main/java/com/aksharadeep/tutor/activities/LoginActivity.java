package com.aksharadeep.tutor.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.firebase.FirebaseManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AksharaDeepaPref";

    // ── Screens ──
    private LinearLayout layoutMethodChoice;   // screen 0: pick Email or Phone
    private LinearLayout layoutEmail;          // screen 1: email login/register
    private LinearLayout layoutPhoneEnter;     // screen 2: enter phone number
    private LinearLayout layoutOtpVerify;      // screen 3: enter OTP

    // ── Email fields ──
    private EditText etEmail, etPassword, etRegName, etRegClass, etRegEmail, etRegPass;
    private LinearLayout layoutEmailLogin, layoutEmailRegister;

    // ── Phone fields ──
    private EditText etPhone, etOtp;
    private TextView tvResend, tvOtpTimer, tvOtpHint;
    private Button btnSendOtp, btnVerifyOtp, btnResend;

    // ── Common ──
    private ProgressBar progressBar;
    private FirebaseManager fb;

    // ── OTP state ──
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private CountDownTimer otpTimer;
    private static final int OTP_TIMEOUT = 60; // seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fb = FirebaseManager.getInstance();
        fb.initAnalytics(this);

        if (fb.isLoggedIn()) { goToMain(); return; }

        setContentView(R.layout.activity_login);
        initViews();
        showScreen(0); // Start at method choice screen
    }

    private void initViews() {
        // Screens
        layoutMethodChoice  = findViewById(R.id.layout_method_choice);
        layoutEmail         = findViewById(R.id.layout_email);
        layoutPhoneEnter    = findViewById(R.id.layout_phone_enter);
        layoutOtpVerify     = findViewById(R.id.layout_otp_verify);
        progressBar         = findViewById(R.id.progress_login);

        // Email sub-layouts
        layoutEmailLogin    = findViewById(R.id.layout_email_login);
        layoutEmailRegister = findViewById(R.id.layout_email_register);

        // Email fields
        etEmail    = findViewById(R.id.et_login_username);
        etPassword = findViewById(R.id.et_login_password);
        etRegName  = findViewById(R.id.et_reg_name);
        etRegClass = findViewById(R.id.et_reg_class);
        etRegEmail = findViewById(R.id.et_reg_username);
        etRegPass  = findViewById(R.id.et_reg_password);

        // Phone fields
        etPhone      = findViewById(R.id.et_phone);
        etOtp        = findViewById(R.id.et_otp);
        tvResend     = findViewById(R.id.tv_resend);
        tvOtpTimer   = findViewById(R.id.tv_otp_timer);
        tvOtpHint    = findViewById(R.id.tv_otp_hint);
        btnSendOtp   = findViewById(R.id.btn_send_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        btnResend    = findViewById(R.id.btn_resend_otp);

        // ── Method choice buttons ──
        Button btnChooseEmail = findViewById(R.id.btn_choose_email);
        Button btnChoosePhone = findViewById(R.id.btn_choose_phone);
        btnChooseEmail.setOnClickListener(v -> showScreen(1));
        btnChoosePhone.setOnClickListener(v -> showScreen(2));

        // ── Email section ──
        Button btnLogin    = findViewById(R.id.btn_login);
        Button btnRegister = findViewById(R.id.btn_register);
        TextView tvGoReg   = findViewById(R.id.tv_go_register);
        TextView tvGoLogin = findViewById(R.id.tv_go_login);
        TextView tvBackEmail = findViewById(R.id.tv_back_from_email);

        btnLogin.setOnClickListener(v -> handleEmailLogin());
        btnRegister.setOnClickListener(v -> handleEmailRegister());
        tvGoReg.setOnClickListener(v -> {
            layoutEmailLogin.setVisibility(View.GONE);
            layoutEmailRegister.setVisibility(View.VISIBLE);
        });
        tvGoLogin.setOnClickListener(v -> {
            layoutEmailRegister.setVisibility(View.GONE);
            layoutEmailLogin.setVisibility(View.VISIBLE);
        });
        tvBackEmail.setOnClickListener(v -> showScreen(0));

        // ── Phone section ──
        TextView tvBackPhone = findViewById(R.id.tv_back_from_phone);
        TextView tvBackOtp   = findViewById(R.id.tv_back_from_otp);
        tvBackPhone.setOnClickListener(v -> showScreen(0));
        tvBackOtp.setOnClickListener(v -> {
            if (otpTimer != null) otpTimer.cancel();
            showScreen(2);
        });

        btnSendOtp.setOnClickListener(v -> sendOtp());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        btnResend.setOnClickListener(v -> resendOtp());
    }

    // ── Show a specific screen, hide others ──
    private void showScreen(int screen) {
        layoutMethodChoice.setVisibility(screen == 0 ? View.VISIBLE : View.GONE);
        layoutEmail.setVisibility(screen == 1 ? View.VISIBLE : View.GONE);
        layoutPhoneEnter.setVisibility(screen == 2 ? View.VISIBLE : View.GONE);
        layoutOtpVerify.setVisibility(screen == 3 ? View.VISIBLE : View.GONE);
    }

    // ══════════════════════════════════════
    // EMAIL LOGIN
    // ══════════════════════════════════════
    private void handleEmailLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show(); return;
        }
        showLoading(true);
        fb.getAuth().signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(r -> {
                showLoading(false);
                fb.logLogin("email");
                Toast.makeText(this, "Welcome back! 👋", Toast.LENGTH_SHORT).show();
                goToMain();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void handleEmailRegister() {
        String name  = etRegName.getText().toString().trim();
        String cls   = etRegClass.getText().toString().trim();
        String email = etRegEmail.getText().toString().trim();
        String pass  = etRegPass.getText().toString().trim();
        if (TextUtils.isEmpty(name))  { etRegName.setError("Enter name"); return; }
        if (TextUtils.isEmpty(cls))   { etRegClass.setError("Enter class/school"); return; }
        if (TextUtils.isEmpty(email)) { etRegEmail.setError("Enter email"); return; }
        if (pass.length() < 6)        { etRegPass.setError("Min 6 characters"); return; }

        showLoading(true);
        fb.getAuth().createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener(result -> {
                FirebaseUser user = result.getUser();
                if (user == null) return;
                saveProfileAndGoMain(user.getUid(), name, cls);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    // ══════════════════════════════════════
    // PHONE OTP LOGIN
    // ══════════════════════════════════════
    private void sendOtp() {
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            etPhone.setError("Enter valid 10-digit number"); return;
        }

        // Auto-add India country code if not present
        if (!phone.startsWith("+")) phone = "+91" + phone;

        showLoading(true);
        btnSendOtp.setEnabled(false);

        String finalPhone = phone;
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(fb.getAuth())
            .setPhoneNumber(finalPhone)
            .setTimeout((long) OTP_TIMEOUT, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    // Auto-verification (some devices do this automatically)
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "✅ Auto-verified!", Toast.LENGTH_SHORT).show();
                    signInWithCredential(credential);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    showLoading(false);
                    btnSendOtp.setEnabled(true);
                    Toast.makeText(LoginActivity.this,
                        "❌ Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(String vId, PhoneAuthProvider.ForceResendingToken token) {
                    showLoading(false);
                    verificationId = vId;
                    resendToken    = token;

                    // Show OTP screen
                    showScreen(3);
                    if (tvOtpHint != null)
                        tvOtpHint.setText("OTP sent to " + finalPhone + "\nEnter the 6-digit code below:");
                    startOtpTimer();
                    Toast.makeText(LoginActivity.this,
                        "📱 OTP sent to " + finalPhone, Toast.LENGTH_LONG).show();
                }
            }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp() {
        String code = etOtp.getText().toString().trim();
        if (code.length() != 6) {
            etOtp.setError("Enter 6-digit OTP"); return;
        }
        if (verificationId == null) {
            Toast.makeText(this, "Please request OTP first", Toast.LENGTH_SHORT).show(); return;
        }
        showLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void resendOtp() {
        if (resendToken == null) { sendOtp(); return; }
        String phone = etPhone.getText().toString().trim();
        if (!phone.startsWith("+")) phone = "+91" + phone;

        showLoading(true);
        btnResend.setEnabled(false);
        String finalPhone = phone;

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(fb.getAuth())
            .setPhoneNumber(finalPhone)
            .setTimeout((long) OTP_TIMEOUT, TimeUnit.SECONDS)
            .setActivity(this)
            .setForceResendingToken(resendToken)
            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    showLoading(false);
                    signInWithCredential(credential);
                }
                @Override
                public void onVerificationFailed(FirebaseException e) {
                    showLoading(false);
                    btnResend.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "❌ " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                @Override
                public void onCodeSent(String vId, PhoneAuthProvider.ForceResendingToken token) {
                    showLoading(false);
                    verificationId = vId;
                    resendToken    = token;
                    startOtpTimer();
                    Toast.makeText(LoginActivity.this, "📱 OTP resent!", Toast.LENGTH_SHORT).show();
                }
            }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        showLoading(true);
        fb.getAuth().signInWithCredential(credential)
            .addOnSuccessListener(result -> {
                showLoading(false);
                if (otpTimer != null) otpTimer.cancel();
                FirebaseUser user = result.getUser();
                fb.logLogin("phone");

                // Check if new user — save profile
                boolean isNew = result.getAdditionalUserInfo() != null
                        && result.getAdditionalUserInfo().isNewUser();
                if (isNew && user != null) {
                    // New phone user — use phone number as name placeholder
                    String phone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "Student";
                    saveProfileAndGoMain(user.getUid(), "Student", "Class 10");
                } else {
                    Toast.makeText(this, "✅ Login successful!", Toast.LENGTH_SHORT).show();
                    goToMain();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "❌ Wrong OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    // ── OTP Countdown Timer ──
    private void startOtpTimer() {
        if (otpTimer != null) otpTimer.cancel();
        btnResend.setEnabled(false);
        tvResend.setVisibility(View.GONE);

        otpTimer = new CountDownTimer(OTP_TIMEOUT * 1000L, 1000) {
            public void onTick(long ms) {
                int secs = (int)(ms / 1000);
                if (tvOtpTimer != null)
                    tvOtpTimer.setText("Resend OTP in " + secs + "s");
            }
            public void onFinish() {
                if (tvOtpTimer != null) tvOtpTimer.setText("");
                if (tvResend != null)   tvResend.setVisibility(View.VISIBLE);
                btnResend.setEnabled(true);
            }
        }.start();
    }

    // ── Save profile to Firestore + SharedPrefs ──
    private void saveProfileAndGoMain(String uid, String name, String cls) {
        java.util.Map<String, Object> profile = new java.util.HashMap<>();
        profile.put("name", name);
        profile.put("class", cls);
        profile.put("createdAt", com.google.firebase.Timestamp.now());

        fb.getFirestore().collection("users").document(uid).set(profile);

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
            .putString("studentName", name)
            .putString("studentClass", cls)
            .apply();

        showLoading(false);
        Toast.makeText(this, "Welcome, " + name + "! 🎉", Toast.LENGTH_SHORT).show();
        goToMain();
    }

    private void showLoading(boolean show) {
        if (progressBar != null)
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void goToMain() {
        FirebaseUser user = fb.getCurrentUser();
        if (user != null) {
            fb.getFirestore().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String n = doc.getString("name");
                        String c = doc.getString("class");
                        if (n != null)
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                .putString("studentName", n)
                                .putString("studentClass", c != null ? c : "Class 10")
                                .apply();
                    }
                    startActivity(new Intent(this, SplashActivity.class)); finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, SplashActivity.class)); finish();
                });
        } else {
            startActivity(new Intent(this, SplashActivity.class)); finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null) otpTimer.cancel();
    }
}
