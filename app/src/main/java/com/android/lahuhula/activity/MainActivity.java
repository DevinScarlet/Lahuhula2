package com.android.lahuhula.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lahuhula.R;
import com.android.lahuhula.fragment.FriendsFragment;
import com.android.lahuhula.fragment.HuhuDataFragment;
import com.android.lahuhula.fragment.HuhuFragment;
import com.android.lahuhula.fragment.MySelfFragment;
import com.android.lahuhula.fragment.RealPeopleProductFragment;
import com.android.lahuhula.util.JsonUtils;
import com.android.lahuhula.util.JsonUtils.JsonResultData;
import com.android.lahuhula.util.NetworkUtils;
import com.android.lahuhula.view.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    //private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ImageView mLogoLayout;
    private LinearLayout mMainLayout;
    private ViewPager mViewPager;
    private AlertDialog mLoginDialog;
    private HuhuFragment mHuhuFragment;
    private FriendsFragment mFriendsFragment;
    private HuhuDataFragment mHuhuDataFragment;
    private RealPeopleProductFragment mRealPeopleProductFragment;
    private MySelfFragment mMySelfFragment;
    private FloatingActionButton mLoginBtn;
    private Toast mToast = null;
    private ArrayList<TextView> mEntryPointTextView = new ArrayList<TextView>();

    private static final int MSG_UPDATE_USER_LOGIN_SUCCESS_RESULT = 0x20;
    private static final int MSG_UPDATE_USER_LOGIN_ERROR_RESULT = 0x21;

    private static final int REQUEST_CODE_PERMISSION_STORAGE = 0x31;
    private static final int REQUEST_CODE_PERMISSION_CONTACTS = 0x32;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 0x33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        setupView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
    }

    private void setupView() {
        mLoginBtn = (FloatingActionButton) findViewById(R.id.fab);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                showLoginDialog();
            }
        });

        initFragment();
        initEntryPointView();

        mLogoLayout = (ImageView) findViewById(R.id.logo_content_layout);
        mLogoLayout.setVisibility(View.VISIBLE);
        mMainLayout = (LinearLayout) findViewById(R.id.main_content_layout);
        mMainLayout.setVisibility(View.GONE);
        mLoginBtn.setVisibility(View.GONE);
        mHandler.postDelayed(mUpdateViewVisibility, 3000);
    }

    private Runnable mUpdateViewVisibility = new Runnable() {
        @Override
        public void run() {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mLogoLayout.setVisibility(View.GONE);
            mMainLayout.setVisibility(View.VISIBLE);
            if (UserInfo.isUserLogin(MainActivity.this)) {
                mLoginBtn.setVisibility(View.GONE);
            } else {
                mLoginBtn.setVisibility(View.VISIBLE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int hasStoragePermision = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                Log.d("zhangtao", "hasStoragePermision:" + hasStoragePermision);
                if (hasStoragePermision != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSION_STORAGE);
                    return;
                }

                int hasContactsPermision = checkSelfPermission(Manifest.permission.READ_CONTACTS);
                Log.d("zhangtao", "hasContactsPermision:" + hasContactsPermision);
                if (hasContactsPermision != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_PERMISSION_CONTACTS);
                    return;
                }

                int hasLocationPermision = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                Log.d("zhangtao", "hasLocationPermision:" + hasLocationPermision);
                if (hasLocationPermision != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_PERMISSION_LOCATION);
                    return;
                }
            }
        }
    };

    private void initFragment() {
        if (mHuhuFragment == null) {
            mHuhuFragment = new HuhuFragment();
        }
        if (mFriendsFragment == null) {
            mFriendsFragment = new FriendsFragment();
        }
        if (mHuhuDataFragment == null) {
            mHuhuDataFragment = new HuhuDataFragment();
        }
        if (mRealPeopleProductFragment == null) {
            mRealPeopleProductFragment = new RealPeopleProductFragment();
        }
        if (mMySelfFragment == null) {
            mMySelfFragment = new MySelfFragment();
        }

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mHuhuFragment, "huhufragment").commit();
        //getFragmentManager().beginTransaction().add(R.id.fragment_container, mHuhuFragment, "huhufragment").commit();
    }

    private void initEntryPointView() {
        mEntryPointTextView.clear();
        TextView homePage = (TextView) findViewById(R.id.home_page);
        homePage.setOnClickListener(mViewListener);
        homePage.setTextColor(getResources().getColor(R.color.color_entry_point_focus));
        TextView friends = (TextView) findViewById(R.id.friends);
        friends.setOnClickListener(mViewListener);
        TextView huhuData = (TextView) findViewById(R.id.huhu_data);
        huhuData.setOnClickListener(mViewListener);
        TextView realPeopleProduct = (TextView) findViewById(R.id.real_people_product);
        realPeopleProduct.setOnClickListener(mViewListener);
        TextView mySelf = (TextView) findViewById(R.id.my_self);
        mySelf.setOnClickListener(mViewListener);
        mEntryPointTextView.add(homePage);
        mEntryPointTextView.add(friends);
        mEntryPointTextView.add(huhuData);
        mEntryPointTextView.add(realPeopleProduct);
        mEntryPointTextView.add(mySelf);
    }

    private void updateEntryPointTextViewColor(View view) {
        TextView curView = (TextView) view;
        for (TextView v : mEntryPointTextView) {
            v.setTextColor(getResources().getColor(R.color.color_entry_point_normal));
            if (v == curView) {
                v.setTextColor(getResources().getColor(R.color.color_entry_point_focus));
            }
        }
    }

    private View.OnClickListener mViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.home_page:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            mHuhuFragment, "huhufragment").commitAllowingStateLoss();
                    break;
                case R.id.friends:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            mFriendsFragment, "mfriendsFragment").commitAllowingStateLoss();
                    break;
                case R.id.huhu_data:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            mHuhuDataFragment, "mHuhuDataFragment").commitAllowingStateLoss();
                    break;
                case R.id.real_people_product:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            mRealPeopleProductFragment, "mRealPeopleProductFragment").commitAllowingStateLoss();
                    break;
                case R.id.my_self:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            mMySelfFragment, "mMySelfFragment").commitAllowingStateLoss();
                    break;
            }
            updateEntryPointTextViewColor(view);
        }
    };

    private void showLoginDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_login, null);
        mLoginDialog = new AlertDialog.Builder(this)
                .setView(view)
                .show();
        mLoginDialog.setCanceledOnTouchOutside(false);
        ImageView iv = (ImageView) view.findViewById(R.id.login_cancel);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginDialog.dismiss();
            }
        });
        TextView register = (TextView) view.findViewById(R.id.phone_register_btn);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entryPhoneRegisterActivity();
            }
        });

        TextView forgetPassword = (TextView) view.findViewById(R.id.forget_password_btn);
        forgetPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                entryForgetPasswordActivity();
            }
        });

        final EditText loginAccount = (EditText) view.findViewById(R.id.login_account);
        final EditText loginPassword = (EditText) view.findViewById(R.id.login_password);

        Button loginBtn = (Button) view.findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtils.isNetworkAvailable(MainActivity.this)) {
                    final String account = loginAccount.getText().toString();
                    final String password = loginPassword.getText().toString();
                    if (TextUtils.isEmpty(account)) {
                        showToast(MainActivity.this, "用户名不能为空");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        showToast(MainActivity.this, "密码不能为空");
                        return;
                    }

                    new Thread() {
                        @Override
                        public void run() {
                            userLoginPost(getUserLoginInfo(account, password));
                        }
                    }.start();
                }
            }
        });
    }

    private void userLoginPost(String arg) {
        JsonResultData result = JsonUtils.loginHttpPost(JsonUtils.USER_LOGIN_URI, arg);
        mHandler.removeMessages(MSG_UPDATE_USER_LOGIN_ERROR_RESULT);
        mHandler.removeMessages(MSG_UPDATE_USER_LOGIN_SUCCESS_RESULT);
        if (result.isSuccess()) {
            mHandler.sendEmptyMessage(MSG_UPDATE_USER_LOGIN_SUCCESS_RESULT);
            saveLoginUserInfo(result.getResultString());
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_USER_LOGIN_ERROR_RESULT, result.getResultString()));
        }
    }

    private String getUserLoginInfo(String userName, String password) {
        JSONObject json = new JSONObject();
        try {
            json.put("vc_nickname", userName);
            json.put("vc_psd", password);
        } catch (JSONException ex) {
            return null;
        }
        return json.toString();
    }

    private void saveLoginUserInfo(String userInfo) {
        new SaveUserInfoTask(userInfo).execute();
    }

    private class SaveUserInfoTask extends AsyncTask<Void, Void, Void> {
        private String mUserInfo;

        public SaveUserInfoTask(String userInfo) {
            mUserInfo = userInfo;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Map<String, String> xml = JsonUtils.decodeJsonUtilXml(mUserInfo);
            if (xml == null) {
                return null;
            }
            String info = xml.get(JsonUtils.RESULT_TAG);
            if (TextUtils.isEmpty(info)) {
                return null;
            }

            try {
                JSONObject jsonObject = new JSONObject(info);
                JSONArray jsonArray = (JSONArray) jsonObject.get("UserInfo");
                JSONObject object = jsonArray.getJSONObject(0);
                UserInfo.saveUserInfoToSharedPreferences(MainActivity.this, object);
            } catch (JSONException je) {
                Log.e(TAG, "saveUserInfo je:" + je);
            }
            return null;
        }
    }

    private void entryPhoneRegisterActivity() {
        startActivity(new Intent(this, RegisterInfoActivity.class));
    }

    private void entryForgetPasswordActivity() {
        startActivity(new Intent(this, ForgetPasswordActivity.class));
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_USER_LOGIN_SUCCESS_RESULT:
                    mLoginDialog.dismiss();
                    mLoginBtn.setVisibility(View.GONE);
                    break;
                case MSG_UPDATE_USER_LOGIN_ERROR_RESULT:
                    showToast(MainActivity.this, msg.obj.toString());
                    break;
            }
        }
    };

    private void showToast(Context context, String msg) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
}
