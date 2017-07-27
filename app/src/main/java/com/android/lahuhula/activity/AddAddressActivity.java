package com.android.lahuhula.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.lahuhula.R;
import com.android.lahuhula.bean.MyAddress;
import com.android.lahuhula.util.PreferenceUtils;
import com.android.lahuhula.util.ProvinceCityUtils;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/3/22.
 */

public class AddAddressActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PICK_CONTACT = 100;
    private EditText mAddressName;
    private EditText mPhoneNumber;
    private Spinner mProvinceSpinner;
    private Spinner mCitySpinner;
    private EditText mDetailAddress;
    private int mProvinceIndex;
    private int mCityIndex;
    ArrayAdapter<String> mProvinceAdapter = null;  //省级适配器
    ArrayAdapter<String> mCityAdapter = null;    //地级适配器
    static int mProvincePosition = 0;
    private String[] mProvince = new String[31];
    private String[][] mCities = ProvinceCityUtils.CITY_ARRAY;
    private int mCityPos;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.add_address_activity);
        initActionBar();
        initData();
        setupView();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initData() {
        for (int i = 0; i < ProvinceCityUtils.PROVINCE_ARRAY.length; i++) {
            mProvince[i] = ProvinceCityUtils.PROVINCE_ARRAY[i];
        }
    }

    private void setupView() {
        findViewById(R.id.add_address_plus).setOnClickListener(this);
        mAddressName = (EditText) findViewById(R.id.add_address_name);
        mPhoneNumber = (EditText) findViewById(R.id.add_address_phone_number);
        mDetailAddress = (EditText) findViewById(R.id.add_address_detail);
        mAddressName.setOnClickListener(this);
        mPhoneNumber.setOnClickListener(this);
        mDetailAddress.setOnClickListener(this);

        setSpinner();
    }

    private void setSpinner() {
        mProvinceSpinner = (Spinner) findViewById(R.id.add_address_province);
        mCitySpinner = (Spinner) findViewById(R.id.add_address_city);

        mProvinceAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mProvince);
        mProvinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProvinceSpinner.setAdapter(mProvinceAdapter);
        mProvinceSpinner.setSelection(0, true);

        mCityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mCities[0]);
        mCityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCitySpinner.setAdapter(mCityAdapter);
        mCitySpinner.setSelection(0, true);
        //省级下拉框监听
        mProvinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                mCityAdapter = new ArrayAdapter<String>(AddAddressActivity.this, android.R.layout.simple_spinner_item, mCities[position]);
                mCityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mCitySpinner.setAdapter(mCityAdapter);
                mProvincePosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //地级下拉监听
        mCitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                Log.d("zhangtao", "mCities[" + mProvincePosition + "][" + position + "]:" + mCities[mProvincePosition][position]);
               /* countyAdapter = new ArrayAdapter<String>(DisplayCity.this,
                        android.R.layout.simple_spinner_item, county[mProvincePosition][position]);
                countySpinner.setAdapter(countyAdapter);*/
                mCityPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.add_address_plus:
                startContactSelectionActivity();
                break;
            case R.id.add_address_name:
                break;
            case R.id.add_address_phone_number:
                break;
            case R.id.add_address_detail:
                break;
        }
    }

    private void startContactSelectionActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
    }

    //收货地址确定
    private void confirmAddAddress() {
        //这里本应该上传服务器，现在存储在本地
        //获取收货人姓名
        String mName = mAddressName.getText().toString().trim();
        String mPhone = mPhoneNumber.getText().toString().trim();
        String mPro = mProvince[mProvincePosition];
        String mCity = mCities[mProvincePosition][mCityPos];
        String mDetail = mDetailAddress.getText().toString().trim();

        if (TextUtils.isEmpty(mName) || TextUtils.isEmpty(mPhone) || TextUtils.isEmpty(mPro) || TextUtils.isEmpty(mCity) || TextUtils.isEmpty(mDetail)) {
            Toast.makeText(this, "请填写完整地址信息", Toast.LENGTH_SHORT).show();
        } else {
            //存储地址
            MyAddress address = new MyAddress();
            address.setAddress(mPro + " " + mCity + " " + mDetail);
            address.setUserName(mName);
            address.setUserNum(mPhone);
            address.setIsDefaultAddress("false");
            ArrayList<MyAddress> addressList = PreferenceUtils.getCachedAddress();
            addressList.add(address);
            Log.i("onActivityResult", "quding" + addressList.toString());
            PreferenceUtils.setCachedAddress(addressList);
            setResult(MyAddressManagerActivity.RESULT_CODE_ADD_ADDRESS);
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null && data.getData() != null) {
                        Uri uri = data.getData();
                        String[] contacts = getPhoneContacts(uri);
                        mAddressName.setText(contacts[0]);
                        mPhoneNumber.setText(contacts[1]);
                    }
                }
                break;
        }
    }

    private String[] getPhoneContacts(Uri uri) {
        String[] contact = new String[2];
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            //取得联系人姓名
            contact[0] = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            //取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
            if (phone != null) {
                phone.moveToFirst();
                contact[1] = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            phone.close();
            cursor.close();
        } else {
            return null;
        }
        return contact;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_address_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.confirm_add_address:
                confirmAddAddress();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
