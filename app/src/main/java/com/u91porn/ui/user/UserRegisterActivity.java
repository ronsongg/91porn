package com.u91porn.ui.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.orhanobut.logger.Logger;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.u91porn.MyApplication;
import com.u91porn.R;
import com.u91porn.data.NoLimit91PornServiceApi;
import com.u91porn.ui.MvpActivity;
import com.u91porn.ui.main.MainActivity;
import com.u91porn.utils.AppManager;
import com.u91porn.utils.Constants;
import com.u91porn.utils.DialogUtils;
import com.u91porn.utils.RandomIPAdderssUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rx_cache2.Reply;

public class UserRegisterActivity extends MvpActivity<UserView, UserPresenter> implements UserView {
    private static final String TAG = UserRegisterActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_account)
    EditText etAccount;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_password_one)
    EditText etPasswordOne;
    @BindView(R.id.et_password_two)
    EditText etPasswordTwo;
    @BindView(R.id.et_captcha)
    EditText etCaptcha;
    @BindView(R.id.wb_captcha)
    SimpleDraweeView wbCaptcha;
    @BindView(R.id.bt_user_signup)
    Button btUserSignup;
    private NoLimit91PornServiceApi noLimit91PornServiceApi = MyApplication.getInstace().getNoLimit91PornService();
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setContentInsetStartWithNavigation(0);
        setTitle("用户注册");
        loadCaptcha();

        btUserSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etAccount.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String passwordOne = etPasswordOne.getText().toString().trim();
                String passwordTwo = etPasswordTwo.getText().toString().trim();
                String captcha = etCaptcha.getText().toString().trim();
                register(username, email, passwordOne, passwordTwo, captcha);
            }
        });
        wbCaptcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCaptcha();
            }
        });
        alertDialog = DialogUtils.initLodingDialog(this, "注册中，请稍后...");
    }

    /**
     * 跳转主界面
     */
    private void startMain() {
        List<Class<?>> classList = new ArrayList<>();
        classList.add(MainActivity.class);
        classList.add(UserLoginActivity.class);
        AppManager.getAppManager().finishActivity(classList);
        Intent intent = new Intent(this, MainActivity.class);
        startActivityWithAnimotion(intent);
        finish();
    }

    private void register(String username, String email, String passwordOne, String passwordTwo, String captcha) {
        if (TextUtils.isEmpty(username)) {
            showMessage("用户名不能为空");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            showMessage("邮箱不能为空");
            return;
        }
        if (TextUtils.isEmpty(passwordOne)) {
            showMessage("密码不能为空");
            return;
        }
        if (TextUtils.isEmpty(passwordTwo)) {
            showMessage("确认密码不能为空");
            return;
        }
        if (TextUtils.isEmpty(captcha)) {
            showMessage("验证码不能为空");
            return;
        }
        if (!passwordOne.equals(passwordTwo)) {
            showMessage("密码不一致，请检查");
            return;
        }
        String next = "";
        String fingerprint = "";
        String vip = "";
        String actionSignup = "Sign Up";
        String submitX = "45";
        String submitY = "13";
        String ipAddress = RandomIPAdderssUtils.getRandomIPAdderss();
        presenter.register(next, username, passwordOne, passwordTwo, email, captcha, fingerprint, vip, actionSignup, submitX, submitY, ipAddress);
    }

    @NonNull
    @Override
    public UserPresenter createPresenter() {

        return new UserPresenter(noLimit91PornServiceApi);
    }

    /**
     * 加载验证码，目前似乎是非必须，不填也是可以登录的
     */
    private void loadCaptcha() {
        String url;
        if (TextUtils.isEmpty(MyApplication.getInstace().getHost())) {
            url = Constants.BASE_URL + "captcha2.php";
        } else {
            url = MyApplication.getInstace().getHost() + "captcha2.php";
        }

        Logger.t(TAG).d("验证码链接：" + url);
        Uri uri = Uri.parse(url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();

        imagePipeline.evictFromCache(uri);
        wbCaptcha.setImageURI(uri);

        //创建DraweeController
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                //加载的图片URI地址
                .setUri(uri)
                //设置点击重试是否开启
                .setTapToRetryEnabled(true)
                //设置旧的Controller
                .setOldController(wbCaptcha.getController())
                //构建
                .build();

        //设置DraweeController
        wbCaptcha.setController(controller);
    }

    @Override
    public void loginSuccess() {

    }

    @Override
    public void loginError(String message) {

    }

    @Override
    public void registerSuccess() {
        startMain();
        showMessage("注册成功");
    }

    @Override
    public void registerFailure(String message) {
        showMessage(message);
    }

    @Override
    public String getErrorMessage(Throwable e, boolean pullToRefresh) {
        return null;
    }

    @Override
    public void showError(Throwable e, boolean pullToRefresh) {

    }

    @Override
    public void showLoading(boolean pullToRefresh) {
        if (alertDialog == null) {
            return;
        }
        alertDialog.show();
    }

    @Override
    public void showContent() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void showMessage(String msg) {
        super.showMessage(msg);
    }

    @Override
    public LifecycleTransformer<Reply<String>> bindView() {
        return bindToLifecycle();
    }
}