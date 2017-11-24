package org.linphone;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.linphone.Utils.AppLog;
import org.linphone.core.LinphoneAccountCreator;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Log;
import org.linphone.my.MyAddress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by qckiss on 2017/8/18.
 */
public class LinphoneActivity extends AppCompatActivity{

    private static LinphoneActivity instance;

    private LinphoneCoreListenerBase mListener;
    private LinphoneAddress address;
    private LinphoneAccountCreator accountCreator;
    private boolean alreadyAcceptedOrDeniedCall = true;
    private LinphoneCall mCall;

    private TextView mStates_tv;
    private Button mPutThrough_bt;
    private Button mCallUp_bt;
    private TextView mMessage_tv;
    private Button mSendText_bt,mSendImg_bt;

    static final boolean isInstanciated() {
        return instance != null;
    }

    public static final LinphoneActivity instance() {
        if (instance != null)
            return instance;
        throw new RuntimeException("LinphoneActivity not instantiated yet");
    }
    public void displayCustomToast(final String message, final int duration) {
        LayoutInflater inflater = getLayoutInflater();
//        View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastRoot));

//        TextView toastText = (TextView) layout.findViewById(R.id.toastMessage);
//        toastText.setText(message);
        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
//        toast.setView(layout);
        toast.show();
    }
    public Dialog displayDialog(String text){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Drawable d = new ColorDrawable(ContextCompat.getColor(this, R.color.colorC));
        d.setAlpha(200);
//        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(d);

//        TextView customText = (TextView) dialog.findViewById(R.id.customText);
//        customText.setText(text);
        return dialog;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linphone);
        initViews();
//        mPrefs = LinphonePreferences.instance();
//        status.enableSideMenu(false);

        accountCreator = LinphoneCoreFactory.instance().createAccountCreator(LinphoneManager.getLc(), LinphonePreferences.instance().getXmlrpcUrl());
        accountCreator.setDomain(getResources().getString(R.string.default_domain));
//        accountCreator.setListener(this);
        initLogin();
        mListener = new LinphoneCoreListenerBase(){
            @Override
            public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr, LinphoneChatMessage message) {
//                displayMissedChats(getUnreadMessageCount());
                AppLog.i("接受到消息"+message.toString());
                getChatList();
            }

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig proxy, LinphoneCore.RegistrationState state, String smessage) {
                if (state.equals(LinphoneCore.RegistrationState.RegistrationCleared)) {
                    if (lc != null) {
                        LinphoneAuthInfo authInfo = lc.findAuthInfo(proxy.getIdentity(), proxy.getRealm(), proxy.getDomain());
                        if (authInfo != null)
                            lc.removeAuthInfo(authInfo);
                    }
                }

//                refreshAccounts();

//                if(getResources().getBoolean(R.bool.use_phone_number_validation)) {
//                    if (state.equals(LinphoneCore.RegistrationState.RegistrationOk)) {
//                        LinphoneManager.getInstance().isAccountWithAlias();
//                    }
//                }

//                if(state.equals(LinphoneCore.RegistrationState.RegistrationFailed) && newProxyConfig) {
//                    newProxyConfig = false;
//                    if (proxy.getError() == Reason.BadCredentials) {
//                        //displayCustomToast(getString(R.string.error_bad_credentials), Toast.LENGTH_LONG);
//                    }
//                    if (proxy.getError() == Reason.Unauthorized) {
//                        displayCustomToast(getString(R.string.error_unauthorized), Toast.LENGTH_LONG);
//                    }
//                    if (proxy.getError() == Reason.IOError) {
//                        displayCustomToast(getString(R.string.error_io_error), Toast.LENGTH_LONG);
//                    }
//                }
            }

            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {
                if (state == LinphoneCall.State.IncomingReceived) {
                    AppLog.d("收到来电");
                    upState("收到来电");
//                    startActivity(new Intent(LinphoneActivity.instance(), CallIncomingActivity.class));
                    initLinphoneCall();
                } else if (state == LinphoneCall.State.OutgoingInit || state == LinphoneCall.State.OutgoingProgress) {
                    AppLog.d("state=OutgoingProgress");
//                    startActivity(new Intent(LinphoneActivity.instance(), CallOutgoingActivity.class));
                } else if (state == LinphoneCall.State.CallEnd || state == LinphoneCall.State.Error || state == LinphoneCall.State.CallReleased) {
                    AppLog.d("state=CallEnd");
//                    resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
                }

                int missedCalls = LinphoneManager.getLc().getMissedCallsCount();
                AppLog.d("MissedCallsCount丢失的数量="+missedCalls);
//                displayMissedCalls(missedCalls);
            }
        };

        instance = this;
    }

    private void initViews() {
        mStates_tv = (TextView) findViewById(R.id.states_tv);
        mPutThrough_bt = (Button) findViewById(R.id.putThrough_bt);
        mCallUp_bt = (Button) findViewById(R.id.callUp_bt);
        mMessage_tv = (TextView) findViewById(R.id.message_tv);
        mSendText_bt = (Button) findViewById(R.id.sendText_bt);
        mSendImg_bt = (Button) findViewById(R.id.sendImg_bt);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.putThrough_bt:
                        answer();//接电话
                        break;
                    case R.id.callUp_bt://打电话
                        MyAddress myAddress = new MyAddress();
                        myAddress.setDisplayedName("");//显示昵称
                        myAddress.setText("sip:1003@10.0.0.99");
                        LinphoneManager.getInstance().newOutgoingCall(myAddress);
                        break;
                    case R.id.sendText_bt:
                        sendTextMessage("发文字aaaaa");//发文字
                        break;
                    case R.id.sendImg_bt:
                        answer();//发图片
                        break;
                }
            }
        };
        mCallUp_bt.setOnClickListener(listener);
        mPutThrough_bt.setOnClickListener(listener);
        mSendText_bt.setOnClickListener(listener);
        mSendImg_bt.setOnClickListener(listener);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!LinphoneService.isReady()) {
            AppLog.w("LinphoneService掉了");
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }
    }

    @Override
    protected void onPause() {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }
        super.onPause();
    }

    private void initLogin() {
        AppLog.d("登录initLogin");
//        Bundle extras = new Bundle();
//        extras.putString("Phone", null);
//        extras.putString("Dialcode", null);
        accountCreator = LinphoneCoreFactory.instance().createAccountCreator(LinphoneManager.getLc(), LinphonePreferences.instance().getXmlrpcUrl());
        accountCreator.setListener(new LinphoneAccountCreator.LinphoneAccountCreatorListener() {
            @Override
            public void onAccountCreatorIsAccountUsed(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

                AppLog.d("开启 onAccountCreatorIsAccountUsed");
            }

            @Override
            public void onAccountCreatorAccountCreated(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

            }

            @Override
            public void onAccountCreatorAccountActivated(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

            }

            @Override
            public void onAccountCreatorAccountLinkedWithPhoneNumber(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

            }

            @Override
            public void onAccountCreatorPhoneNumberLinkActivated(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

            }

            @Override
            public void onAccountCreatorIsAccountActivated(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

            }

            @Override
            public void onAccountCreatorPhoneAccountRecovered(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

            }

            @Override
            public void onAccountCreatorIsAccountLinked(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

            }

            @Override
            public void onAccountCreatorIsPhoneNumberUsed(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

            }

            @Override
            public void onAccountCreatorPasswordUpdated(LinphoneAccountCreator accountCreator, LinphoneAccountCreator.Status status) {

            }
        });

        LinphoneAddress.TransportType transport;
//        if(transports.getCheckedRadioButtonId() == R.id.transport_udp){
            transport = LinphoneAddress.TransportType.LinphoneTransportUdp;
//        } else {
//            if(transports.getCheckedRadioButtonId() == R.id.transport_tcp){
//                transport = LinphoneAddress.TransportType.LinphoneTransportTcp;
//            } else {
//                transport = LinphoneAddress.TransportType.LinphoneTransportTls;
//            }
//        }
        String name = "userc";
        String pwd = "123";
        String domain = "10.0.0.99";
        AppLog.d("开始登录name ="+ name + "pwd=" + pwd + "domain=" + domain);
//        AssistantActivity.instance().genericLogIn(name, pwd, null, domain, transport);
        genericLogIn(name, pwd, null, domain, transport);

    }
    boolean accountCreated = false;
    public void genericLogIn(String username, String password, String prefix, String domain, LinphoneAddress.TransportType transport) {

        if (accountCreated) {
//            retryLogin(username, password, prefix, domain, transport);
        } else {
            logIn(username, password, null, prefix, domain, transport, false);
        }
    }
    private void logIn(String username, String password, String ha1, String prefix, String domain, LinphoneAddress.TransportType transport, boolean sendEcCalibrationResult) {
        saveCreatedAccount(username, password, ha1, prefix, domain, transport);
    }

    private LinphonePreferences mPrefs;
    public void saveCreatedAccount(String username, String password, String prefix, String ha1, String domain, LinphoneAddress.TransportType transport) {
        if (accountCreated)
            return;

        AppLog.d("开始saveCreatedAccount");
        mPrefs = LinphonePreferences.instance();
        username = LinphoneUtils.getDisplayableUsernameFromAddress(username);
        domain = LinphoneUtils.getDisplayableUsernameFromAddress(domain);

        String identity = "sip:" + username + "@" + domain;
        try {
            address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
        } catch (LinphoneCoreException e) {
            Log.e(e);
            AppLog.e("LinphoneCoreException");
        }

        boolean isMainAccountLinphoneDotOrg = domain.equals(getString(R.string.default_domain));
        LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setHa1(ha1)
                .setPassword(password);

        if(prefix != null){
            builder.setPrefix(prefix);
        }

        if (isMainAccountLinphoneDotOrg) {//是linphone的sip服务器
            if (false) {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
            }
            else {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTls);
            }

            builder.setExpires("604800")
                    .setAvpfEnabled(true)
                    .setAvpfRRInterval(3)
                    .setQualityReportingCollector("sip:voip-metrics@sip.linphone.org")
                    .setQualityReportingEnabled(true)
                    .setQualityReportingInterval(180)
                    .setRealm("sip.linphone.org")
                    .setNoDefault(false);

//            mPrefs.enabledFriendlistSubscription(getResources().getBoolean(R.bool.use_friendlist_subscription));

//            mPrefs.setStunServer(getString(R.string.default_stun));
            mPrefs.setIceEnabled(true);

            accountCreator.setPassword(password);
            accountCreator.setHa1(ha1);
            accountCreator.setUsername(username);
        } else {//自己的sip服务器
            AppLog.d("自己的sip服务器");
            String forcedProxy = "";
            if (!TextUtils.isEmpty(forcedProxy)) {
                builder.setProxy(forcedProxy)
                        .setOutboundProxyEnabled(true)
                        .setAvpfRRInterval(5);
            }

            if(transport != null) {
                builder.setTransport(transport);
            }
        }

        if (getResources().getBoolean(R.bool.enable_push_id)) {
            AppLog.d("开启消息推送");
            String regId = mPrefs.getPushNotificationRegistrationID();
            String appId = getString(R.string.push_sender_id);
            if (regId != null && mPrefs.isPushNotificationEnabled()) {
                AppLog.d("开启消息推送1");
                String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
                builder.setContactParameters(contactInfos);
            }
        }

        try {
            AppLog.d("开始saveNewAccount");
            builder.saveNewAccount();
//            if(!newAccount) {
//                displayRegistrationInProgressDialog();
//            }
            accountCreated = true;
            upState("用户已上线");
        } catch (LinphoneCoreException e) {
            Log.e(e);
            AppLog.e("LinphoneCoreException");
        }
    }

    private void upState(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mStates_tv!=null)
                    mStates_tv.setText(state);
            }
        });
    }


    private void initLinphoneCall() {
        mCall = null;
        // Only one call ringing at a time is allowed每次只能打一个电话
        if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
            List<LinphoneCall> calls = LinphoneUtils.getLinphoneCalls(LinphoneManager.getLc());
            for (LinphoneCall call : calls) {
                if (LinphoneCall.State.IncomingReceived == call.getState()) {
                    mCall = call;
                    AppLog.d("得到了call");
                    break;
                }
            }
        }
        if (mCall == null) {
            //The incoming call no longer exists.
            Log.d("Couldn't find incoming call");
            finish();
            return;
        }

        LinphoneAddress address = mCall.getRemoteAddress();
        LinphoneContact contact = ContactsManager.getInstance().findContactFromAddress(address);
        if (contact != null) {
//            LinphoneUtils.setImagePictureFromUri(this, contactPicture, contact.getPhotoUri(), contact.getThumbnailUri());
//            name.setText(contact.getFullName());
            AppLog.d("得到call name="+contact.getFullName());
        } else {
//            name.setText(LinphoneUtils.getAddressDisplayName(address));
            AppLog.d("得到call elsename="+LinphoneUtils.getAddressDisplayName(address));
        }
//        number.setText(address.asStringUriOnly());
        AppLog.d("得到call name="+address.asStringUriOnly());
        mStates_tv.setText("来自"+contact.getFullName()+"的来电");
        alreadyAcceptedOrDeniedCall = false;
    }
    /**
     * 接电话
     */
    private void answer() {
        if (alreadyAcceptedOrDeniedCall) {
            return;
        }
        alreadyAcceptedOrDeniedCall = true;

        LinphoneCallParams params = LinphoneManager.getLc().createCallParams(mCall);

        boolean isLowBandwidthConnection = !LinphoneUtils.isHighBandwidthConnection(LinphoneService.instance().getApplicationContext());

        if (params != null) {
            params.enableLowBandwidth(isLowBandwidthConnection);
        }else {
            Log.e("Could not create call params for call");
        }

        if (params == null || !LinphoneManager.getInstance().acceptCallWithParams(mCall, params)) {
            // the above method takes care of Samsung Galaxy S
            Toast.makeText(this, R.string.couldnt_accept_call, Toast.LENGTH_LONG).show();
        } else {
            if (!LinphoneActivity.isInstanciated()) {
                return;
            }
            LinphoneManager.getInstance().routeAudioToReceiver();
            upState("已接通，通话中");
//            LinphoneActivity.instance().startIncallActivity(mCall);
        }
    }

    boolean newChatConversation = true;
    private void sendTextMessage(String messageToSend) {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        boolean isNetworkReachable = lc == null ? false : lc.isNetworkReachable();
        LinphoneAddress lAddress = null;

        //Start new conversation in fast chat
        if(newChatConversation && chatRoom == null) {
//            String address = searchContactField.getText().toString().toLowerCase(Locale.getDefault());
            String address = "1003";
            if (address != null && !address.equals("")) {
                initChatRoom(address);
            }
        }
        if (chatRoom != null && messageToSend != null && messageToSend.length() > 0 && isNetworkReachable) {
            LinphoneChatMessage message = chatRoom.createLinphoneChatMessage(messageToSend);
            chatRoom.sendChatMessage(message);
            lAddress = chatRoom.getPeerAddress();

            if (LinphoneActivity.isInstanciated()) {
//                LinphoneActivity.instance().onMessageSent(sipUri, messageToSend);
            }

            message.setListener(LinphoneManager.getInstance());
            if (newChatConversation) {
//                exitNewConversationMode(lAddress.asStringUriOnly());
                initChatRoom(lAddress.asStringUriOnly());
            } else {
//                adapter.addMessage(message);
            }

            Log.i("Sent message current status: " + message.getStatus());
        } else if (!isNetworkReachable && LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().displayCustomToast(getString(R.string.error_network_unreachable), Toast.LENGTH_LONG);
        }
    }

    private LinphoneChatRoom chatRoom;
    public void initChatRoom(String sipUri) {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();

        LinphoneAddress lAddress = null;
        if (sipUri == null) {
//            contact = null; // Tablet rotation issue
//            initNewChatConversation();
        } else {
            try {
                lAddress = lc.interpretUrl(sipUri);
            } catch (Exception e) {
                //TODO Error popup and quit chat
            }

            if (lAddress != null) {
                chatRoom = lc.getChatRoom(lAddress);
                chatRoom.markAsRead();
//                LinphoneActivity.instance().updateMissedChatCount();
////                contact = ContactsManager.getInstance().findContactFromAddress(lAddress);
//                if (chatRoom != null) {
//                    searchContactField.setVisibility(View.GONE);
//                    resultContactsSearch.setVisibility(View.GONE);
//                    displayChatHeader(lAddress);
//                    displayMessageList();
//                }
            }
        }
    }

    public List<String> getChatList() {
        ArrayList<String> chatList = new ArrayList<String>();

        LinphoneChatRoom[] chats = LinphoneManager.getLc().getChatRooms();
        List<LinphoneChatRoom> rooms = new ArrayList<LinphoneChatRoom>();

        for (LinphoneChatRoom chatroom : chats) {
            if (chatroom.getHistorySize() > 0) {
                rooms.add(chatroom);
            }
        }

        if (rooms.size() > 1) {
            Collections.sort(rooms, new Comparator<LinphoneChatRoom>() {
                @Override
                public int compare(LinphoneChatRoom a, LinphoneChatRoom b) {
                    LinphoneChatMessage[] messagesA = a.getHistory(1);
                    LinphoneChatMessage[] messagesB = b.getHistory(1);
                    long atime = messagesA[0].getTime();
                    long btime = messagesB[0].getTime();

                    if (atime > btime)
                        return -1;
                    else if (btime > atime)
                        return 1;
                    else
                        return 0;
                }
            });
        }

        for (LinphoneChatRoom chatroom : rooms) {
            chatList.add(chatroom.getPeerAddress().asStringUriOnly());
        }
        AppLog.d("接收到的消息="+new Gson().toJson(chatList));
        return chatList;
    }
    private void sendImageMessage(String path, int imageSize) {//发送图片消息
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        boolean isNetworkReachable = lc == null ? false : lc.isNetworkReachable();

        if(newChatConversation && chatRoom == null) {
            String address = "1003";
            if (address != null && !address.equals("")) {
                initChatRoom(address);
            }
        }

        if (chatRoom != null && path != null && path.length() > 0 && isNetworkReachable) {
            try {
                Bitmap bm = BitmapFactory.decodeFile(path);
                if (bm != null) {
                    FileUploadPrepareTask task = new FileUploadPrepareTask(LinphoneActivity.this, path, imageSize);
                    task.execute(bm);
                } else {
                    Log.e("Error, bitmap factory can't read " + path);
                }
            } catch (RuntimeException e) {
                Log.e("Error, not enough memory to create the bitmap");
            }
        } else if (!isNetworkReachable && LinphoneActivity.isInstanciated()) {
            LinphoneActivity.instance().displayCustomToast(getString(R.string.error_network_unreachable), Toast.LENGTH_LONG);
        }
    }
    private ByteArrayInputStream mUploadingImageStream;
    private static final int SIZE_MAX = 2048;
    class FileUploadPrepareTask extends AsyncTask<Bitmap, Void, byte[]> {
        private String path;
        private ProgressDialog progressDialog;

        public FileUploadPrepareTask(Context context, String fileToUploadPath, int size) {
            path = fileToUploadPath;

            progressDialog = new ProgressDialog(context);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.processing_image));
            progressDialog.show();
        }

        @Override
        protected byte[] doInBackground(Bitmap... params) {
            Bitmap bm = params[0];

            if (bm.getWidth() >= bm.getHeight() && bm.getWidth() > SIZE_MAX) {
                bm = Bitmap.createScaledBitmap(bm, SIZE_MAX, (SIZE_MAX * bm.getHeight()) / bm.getWidth(), false);
            } else if (bm.getHeight() >= bm.getWidth() && bm.getHeight() > SIZE_MAX) {
                bm = Bitmap.createScaledBitmap(bm, (SIZE_MAX * bm.getWidth()) / bm.getHeight(), SIZE_MAX, false);
            }

            // Rotate the bitmap if possible/needed, using EXIF data
            try {
                if (path != null) {
                    ExifInterface exif = new ExifInterface(path);
                    int pictureOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                    Matrix matrix = new Matrix();
                    if (pictureOrientation == 6) {
                        matrix.postRotate(90);
                    } else if (pictureOrientation == 3) {
                        matrix.postRotate(180);
                    } else if (pictureOrientation == 8) {
                        matrix.postRotate(270);
                    }
                    bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                }
            } catch (Exception e) {
                Log.e(e);
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            String extension = LinphoneUtils.getExtensionFromFileName(path);
            if (extension != null && extension.toLowerCase(Locale.getDefault()).equals("png")) {
                bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
            } else {
                bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }
            byte[] byteArray = stream.toByteArray();
            return byteArray;
        }

        @Override
        protected void onPostExecute(byte[] result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            mUploadingImageStream = new ByteArrayInputStream(result);

            String fileName = path.substring(path.lastIndexOf("/") + 1);
            String extension = LinphoneUtils.getExtensionFromFileName(fileName);
            LinphoneContent content = LinphoneCoreFactory.instance().createLinphoneContent("image", extension, result, null);
            content.setName(fileName);

            LinphoneChatMessage message = chatRoom.createFileTransferMessage(content);
            message.setListener(LinphoneManager.getInstance());
            message.setAppData(path);

            LinphoneManager.getInstance().setUploadPendingFileMessage(message);
            LinphoneManager.getInstance().setUploadingImageStream(mUploadingImageStream);

            chatRoom.sendChatMessage(message);
//            adapter.addMessage(message);
        }
    }
}
