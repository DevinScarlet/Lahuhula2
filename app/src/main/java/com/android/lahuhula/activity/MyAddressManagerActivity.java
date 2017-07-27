package com.android.lahuhula.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lahuhula.R;
import com.android.lahuhula.bean.MyAddress;
import com.android.lahuhula.util.PreferenceUtils;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/3/21.
 */
public class MyAddressManagerActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_ADD_ADDRESS = 200;
    public static final int RESULT_CODE_ADD_ADDRESS = 300;
    private static final String TAG = MyAddressManagerActivity.class.getSimpleName();

    private ListView mAddressListView;
    private AddressAdapter mAdapter;
    private Context mContext;
    private ArrayList<MyAddress> mAddressList;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.address_manager_activity);
        initActionBar();
        setupView();
    }

    private void initActionBar() {
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupView() {
        mContext = this;
        mAddressListView = (ListView) findViewById(R.id.address_list);
        mAddressListView.setDividerHeight(20);
        //获取sp中存储的本地地址
        mAddressList = PreferenceUtils.getCachedAddress();
        Log.i("zhangtt", "setupView==" + mAddressList.toString());
        mAdapter = new AddressAdapter(mAddressList);
        mAddressListView.setAdapter(mAdapter);
        findViewById(R.id.add_address_btn).setOnClickListener(mAddAddressListener);
    }

    private View.OnClickListener mAddAddressListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(mContext, AddAddressActivity.class), REQUEST_CODE_ADD_ADDRESS);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_ADDRESS) {
            if (resultCode == RESULT_CODE_ADD_ADDRESS) {
                //获取sp中存储的本地地址
                ArrayList<MyAddress> addressList = PreferenceUtils.getCachedAddress();
                mAdapter.refreshData(addressList);
            }
        }
    }


    class AddressAdapter extends BaseAdapter {
        private ArrayList<MyAddress> addressList;

        public AddressAdapter(ArrayList<MyAddress> list) {
            this.addressList = list;
        }

        public void refreshData(ArrayList<MyAddress> refreshData) {
            addressList.clear();
            addressList.addAll(refreshData);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (null != addressList) {
                return addressList.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHold viewHold = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.address_list_cell_layout, null);
                viewHold = new ViewHold();
                viewHold.nameView = (TextView) convertView.findViewById(R.id.address_name);
                viewHold.phoneNumberView = (TextView) convertView.findViewById(R.id.address_phone_number);
                viewHold.addressView = (TextView) convertView.findViewById(R.id.address_detail);
                viewHold.checkBox = (CheckBox) convertView.findViewById(R.id.address_checkbox);
                viewHold.defaultTextView = (TextView) convertView.findViewById(R.id.address_default_text);
                viewHold.editView = (TextView) convertView.findViewById(R.id.address_edit);
                viewHold.deleteView = (TextView) convertView.findViewById(R.id.address_delete);
                convertView.setTag(viewHold);
            } else {
                viewHold = (ViewHold) convertView.getTag();
            }
            MyAddress myAddress = addressList.get(position);
            viewHold.nameView.setText(myAddress.getUserName().toString().trim());
            viewHold.phoneNumberView.setText(myAddress.getUserNum().toString().trim());
            viewHold.addressView.setText(myAddress.getAddress().toString().trim());
            viewHold.checkBox.setChecked(Boolean.parseBoolean(myAddress.getDefaultAddress()));

            viewHold.checkBox.setTag(position);
            viewHold.checkBox.setOnClickListener(mCheckBoxTouchListener);
            viewHold.editView.setOnClickListener(mEditViewClickListener);
            viewHold.editView.setTag(position);
            viewHold.deleteView.setOnClickListener(mDeleteViewClickListener);
            viewHold.deleteView.setTag(position);

            return convertView;
        }
    }

    public class ViewHold {
        TextView nameView;
        TextView phoneNumberView;
        TextView addressView;
        CheckBox checkBox;
        TextView defaultTextView;
        TextView editView;
        TextView deleteView;
    }

    private View.OnClickListener mCheckBoxTouchListener = new CheckBox.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckBox cb = (CheckBox) view;
            int tag = (int) view.getTag();
            ArrayList<MyAddress> mlist = PreferenceUtils.getCachedAddress();
            //判断是否选中
            if (cb.isChecked()) {
                //没有没选中
                mlist.get(tag).setIsDefaultAddress("true");
                for (int i = 0; i < mlist.size(); i++) {
                    if (i != tag) {
                        mlist.get(i).setIsDefaultAddress("false");
                    }
                }
                PreferenceUtils.setCachedAddress(mlist);
            } else {
                //删除数据，刷新界面
                for (int i = 0; i < mlist.size(); i++) {
                    mlist.get(i).setIsDefaultAddress("false");
                }
                PreferenceUtils.setCachedAddress(mlist);
            }
            mAdapter.refreshData(mlist);
        }
    };

    private View.OnClickListener mEditViewClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            int tag = (int) view.getTag();
        }
    };

    private View.OnClickListener mDeleteViewClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            int tag = (int) view.getTag();
            showDeleteDialog(tag);
        }
    };

    private void showDeleteDialog(final int pos) {
        Dialog dialog = new AlertDialog.Builder(this)
                .setMessage("确认要删除该地址吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //删除数据，刷新界面
                        if (pos == 0 && mAddressList.size() < 2) {
                            //设置缓存
                            PreferenceUtils.setCachedAddress(null);
                            mAdapter.refreshData(new ArrayList<MyAddress>());
                            Log.i("zhangtt", "showDeleteDialog=0" + PreferenceUtils.getCachedAddress().toString());
                        } else {
                            mAddressList.remove(pos);
                            //设置缓存
                            PreferenceUtils.setCachedAddress(mAddressList);
                            ArrayList<MyAddress> addresses = PreferenceUtils.getCachedAddress();
                            mAdapter.refreshData(addresses);
                            Log.i("zhangtt", "showDeleteDialog!==0" + PreferenceUtils.getCachedAddress().toString());
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(mContext, "取消", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
