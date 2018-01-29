package com.ccccat.www.msgs;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.jude.easyrecyclerview.EasyRecyclerView;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jxl.Sheet;
import jxl.Workbook;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener ,EasyPermissions.PermissionCallbacks {
    public static String TAG = "SMSMANAGER";
    private Button btn_improt, btn_send_more,btn_improt2,jump2setting;
   // private EditText phoneEt, contextEt;
    private EasyRecyclerView rv_list;
    private ProgressDialog progressDialog;
    private List<Person> personList;
    private PersonAdapter mAdapter;
    private TextView tips,pro;
    private RadioGroup rg_all;
    private int IDs = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        personList = new ArrayList<>();
        mAdapter = new PersonAdapter();
       // btn_send = (Button) this.findViewById(R.id.btn_send);
        tips = (TextView) this.findViewById(R.id.tips);
        pro= (TextView) this.findViewById(R.id.pro);
        btn_send_more = (Button) this.findViewById(R.id.btn_send_more);
        btn_improt = (Button) this.findViewById(R.id.btn_improt);
        btn_improt2= (Button) this.findViewById(R.id.btn_improt2);
        jump2setting= (Button) this.findViewById(R.id.jump2setting);
      //  phoneEt = (EditText) this.findViewById(R.id.phoneNumberEt);
      //  contextEt = (EditText) this.findViewById(R.id.contextEt);
        rg_all = (RadioGroup) this.findViewById(R.id.rg_all);
        rv_list = (EasyRecyclerView) this.findViewById(R.id.rv_list);
        progressDialog = new ProgressDialog(this);
        //btn_send.setOnClickListener(this);
        btn_improt.setOnClickListener(this);
        btn_improt2.setOnClickListener(this);
        btn_send_more.setOnClickListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rv_list.setLayoutManager(mLayoutManager);
        rv_list.setItemAnimator(new DefaultItemAnimator());
        rv_list.setAdapter(mAdapter);
        rg_all.setOnCheckedChangeListener(this);
        requestPermission();
        jump2setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goAppDetailSettingIntent(MainActivity.this);
            }
        });

    }
    String[] perms = {Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE};

    void requestPermission(){

        if (EasyPermissions.hasPermissions(this, perms)) {
            //...
        } else {
            //...
            EasyPermissions.requestPermissions(this, "需要权限",
                    111, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    /**
     * 获取 excel 表格中的数据,不能在主线程中调用
     */
    private ArrayList<Person> getXlsData(String filePath, int index) {
        ArrayList<Person> persons = new ArrayList<>();
        try {
            InputStream is = new FileInputStream(filePath);
            Workbook workbook = Workbook.getWorkbook(is);
            Sheet sheet = workbook.getSheet(index);
            int sheetRows = sheet.getRows();
            for (int i = 0; i < sheetRows; i++) {
                Person person = new Person();
                person.setUserName(sheet.getCell(0, i).getContents());
                person.setPhoneNumber(sheet.getCell(1, i).getContents());
                person.setMsgContent(sheet.getCell(2, i).getContents());
                persons.add(person);
            }
            workbook.close();
        } catch (Exception e) {
            Log.d(TAG, "数据读取错误=" + e);
        }
        return persons;
    }
    int total=0;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 导入Excel表格
            case R.id.btn_improt:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
                break;
            case R.id.btn_improt2:
              //  String ss=getRealPathFromUri(mPath.trim());
                new ExcelDataLoader().execute(mPath);
                break;
            // 发送
          /*  case R.id.btn_send:
                String phone = phoneEt.getText().toString().trim();
                String context = contextEt.getText().toString().trim();

                if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(context)) {
                    Toast.makeText(MainActivity.this, "号码或内容不能为空！", Toast.LENGTH_SHORT);
                    return;
                }
                sendSms(IDs,phone,context);

                phoneEt.setText("");
                contextEt.setText("");
                Toast.makeText(getApplicationContext(), "发送完毕", Toast.LENGTH_SHORT).show();
                break;*/
            // 批量发送
            case R.id.btn_send_more:
                pro.setVisibility(View.VISIBLE);

                total=personList.size();
                i=0;
               /* for (Person item : personList){
                    sendSms(IDs,item.getPhoneNumber(),item.getMsgContent());
                    // 停顿1s
                    i=i+1;
                    pro.setText("开始发送第"+i+"条，共"+total+"条");
                }*/
                new MyThread().start();
                Toast.makeText(getApplicationContext(), "正在发送,请稍后...", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            pro.setText("共" + total + "条" + "已发送"+i+"条");
        }
    };
    int i=0;
    Person item;
    private class MyThread extends Thread{
        @Override
        public void run() {

            for ( i=0;i<personList.size();i++){
                //sendMessag(phones.get(i),content,i+1,phones.size());
                item =  personList.get(i);
                sendSms(IDs,item.getPhoneNumber(),item.getMsgContent());
                try {

                    Thread.sleep(500);
                    Message message=new Message();
                    message.what=1;
                    handler.sendMessage(message);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void setupData(List<Person> persons) {
        personList.clear();
        personList.addAll(persons);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_one:
                IDs = 0;
                break;
            case R.id.rb_two:
                IDs = 1;
                break;
        }
    }



    // 异步获取Excel数据信息
    private class ExcelDataLoader extends AsyncTask<String, Void, ArrayList<Person>> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Excel数据导入中,请稍后......");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected ArrayList<Person> doInBackground(String... params) {
            return getXlsData(params[0], 0);
        }

        @Override
        protected void onPostExecute(ArrayList<Person> persons) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (persons != null && persons.size() > 0) {
                // 列表显示数据
                setupData(persons);
            } else {
                // 加载失败
                Toast.makeText(MainActivity.this, "数据加载失败！", Toast.LENGTH_SHORT);
            }
        }
    }

    private class PersonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        class TextViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name, tv_number, tv_content;
            LinearLayout ll_index;

            public TextViewHolder(View itemView) {
                super(itemView);
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                tv_number = (TextView) itemView.findViewById(R.id.tv_number);
                tv_content = (TextView) itemView.findViewById(R.id.tv_content);
                ll_index = (LinearLayout) itemView.findViewById(R.id.ll_index);
            }
        }

        @Override
        public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TextViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_person, parent, false));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof TextViewHolder) {
                ((TextViewHolder) holder).tv_name.setText(personList.get(position).getUserName());
                ((TextViewHolder) holder).tv_number.setText(personList.get(position).getPhoneNumber());
                ((TextViewHolder) holder).tv_content.setText(personList.get(position).getMsgContent());
                ((TextViewHolder) holder).ll_index.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                     //   phoneEt.setText(personList.get(position).getPhoneNumber());
                        //contextEt.setText(personList.get(position).getMsgContent());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return personList.size();
        }
    }

    // 获取本地Excel信息
    private String  mPath="";
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                String path = uri.getPath().toString();
                String ss="";
                try{
                   ss =getRealPathFromUri(this,uri);
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "地址转换出火锅了", Toast.LENGTH_SHORT);
                    ss=path;
                }

                tips.setText(ss);
                mPath=ss;
                // 执行Excel数据导入
            //    new ExcelDataLoader().execute(path.trim());
            }
        }
    }
   String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }catch (Exception e){
            Toast.makeText(MainActivity.this, "地址转换出锅了", Toast.LENGTH_SHORT);
            return contentUri.getPath().toString();
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public void sendSMS(String phoneNumber,String message){
        //获取短信管理器
        android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
        //拆分短信内容（手机短信长度限制）
        List<String> divideContents = smsManager.divideMessage(message);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, null, null);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void sendSms(final int which,String phone,String context) {
        SubscriptionInfo sInfo = null;

        final SubscriptionManager sManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        List<SubscriptionInfo> list = sManager.getActiveSubscriptionInfoList();

        if (list.size() == 2) {
            // 双卡
            sInfo = list.get(which);
        } else {
            // 单卡
            sInfo = list.get(0);
        }

        if (sInfo != null) {
            int subId = sInfo.getSubscriptionId();
           // SmsManager manager = SmsManager.getSmsManagerForSubscriptionId(subId);
            android.telephony.SmsManager manager = android.telephony.SmsManager.getDefault();
            if (!TextUtils.isEmpty(phone)) {
                ArrayList<String> messageList =manager.divideMessage(context);

                for(String text:messageList){
                   // manager.sendTextMessage(phone, null, text, null, null);
                    sendSMS2(phone,text);

                }

               /* for (int i = 0; i < messageList.size(); i++) {
                    Intent itSend = new Intent(SENT_SMS_ACTION);
                    itSend.putExtra(KEY_PHONENUM, contactList.get(i));
                    PendingIntent mSendPI = PendingIntent.getBroadcast(getApplicationContext(), i/××requestCode××/, itSend, PendingIntent.FLAG_ONE_SHOT/××flag××/);//这里requestCode和flag的设置很重要，影响数据KEY_PHONENUM的传递。
                    String content = mContext.getString(R.string.test);
                    smsManager.sendTextMessage(contactList.get(i), null, content, mSendPI, null);
                }*/
                Toast.makeText(this, "信息正在发送，请稍候", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(this, "无法正确的获取SIM卡信息，请稍候重试",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void sendSMS2(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
      //  sss(phoneNumber,message);
    }

    void sss(String phone,String text){
        Uri smstoUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_VIEW,smstoUri);
        intent.putExtra("address",phone); // 没有电话号码的话为默认的，即显示的时候是为空的
        intent.putExtra("sms_body",text); // 设置发送的内容
        intent.setType("vnd.android-dir/mms-sms");
        startActivity(intent);
    }

    public static void goAppDetailSettingIntent(Context context){
        Intent localIntent=new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT>=9){
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package",context.getPackageName(),null));
        }else if(Build.VERSION.SDK_INT<=8){
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.setting.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName",context.getPackageName());
        }
        context.startActivity(localIntent);
    }

/*    public int sendSMS3(String sms, List<SendItemBean> list) {
        int size = 0;
        SmsManager smsManager = SmsManager.getDefault();
        Iterator<SendItemBean> it = list.iterator();
        Intent sendIT = new Intent(SMS_SEND_ACTIOIN);
        //注册广播
        PendingIntent sendPI = PendingIntent.getBroadcast(ctx, 0, sendIT, 0);
        //遍历电话号码
        while (it.hasNext()) {
            SendItemBean item = it.next();
            String[] user = item.getUserMsg().split(":");
            String name = user[0];
            String strSms = name + sms;
            SendSMSReceiver.setName(strSms);
            //短信发送
            smsManager.sendTextMessage(user[1], null, strSms, sendPI, null);
            size = item.getId() + 1;
        }
        return size;
    }*/
}