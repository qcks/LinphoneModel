package org.linphone;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.linphone.Utils.AppLog;
import org.linphone.core.LinphoneAccountCreator;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.mediastream.Log;

/**
 * Created by qckiss on 2017/8/18.
 */
public class LinphoneActivity extends AppCompatActivity{

    private static LinphoneActivity instance;

    private LinphoneCoreListenerBase mListener;
    private LinphoneAddress address;
    private LinphoneAccountCreator accountCreator;
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

//        mPrefs = LinphonePreferences.instance();
//        status.enableSideMenu(false);

        accountCreator = LinphoneCoreFactory.instance().createAccountCreator(LinphoneManager.getLc(), LinphonePreferences.instance().getXmlrpcUrl());
        accountCreator.setDomain(getResources().getString(R.string.default_domain));
//        accountCreator.setListener(this);
        initLogin();
//        mListener = new LinphoneCoreListenerBase() {
//
//            @Override
//            public void configuringStatus(LinphoneCore lc, final LinphoneCore.RemoteProvisioningState state, String message) {
//                if (progress != null) progress.dismiss();
//                if (state == LinphoneCore.RemoteProvisioningState.ConfiguringSuccessful) {
//                    goToLinphoneActivity();
//                } else if (state == LinphoneCore.RemoteProvisioningState.ConfiguringFailed) {
//                    Toast.makeText(AssistantActivity.instance(), getString(R.string.remote_provisioning_failure), Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage) {
//                if (remoteProvisioningInProgress) {
//                    if (progress != null) progress.dismiss();
//                    if (state == LinphoneCore.RegistrationState.RegistrationOk) {
//                        remoteProvisioningInProgress = false;
//                        success();
//                    }
//                } else if (accountCreated && !newAccount){
//                    if (address != null && address.asString().equals(cfg.getAddress().asString()) ) {
//                        if (state == LinphoneCore.RegistrationState.RegistrationOk) {
//                            if (progress != null) progress.dismiss();
//                            if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
//                                accountCreator.isAccountUsed();
//                            }
//                        } else if (state == LinphoneCore.RegistrationState.RegistrationFailed) {
//                            if (progress != null) progress.dismiss();
//                            if (dialog == null || !dialog.isShowing()) {
//                                dialog = createErrorDialog(cfg, smessage);
//                                dialog.show();
//                            }
//                        } else if(!(state == LinphoneCore.RegistrationState.RegistrationProgress)) {
//                            if (progress != null) progress.dismiss();
//                        }
//                    }
//                }
//            }
//        };
        instance = this;
    }

    private void initLogin() {
        AppLog.d("登录initLogin");
        Bundle extras = new Bundle();
        extras.putString("Phone", null);
        extras.putString("Dialcode", null);
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
        } catch (LinphoneCoreException e) {
            Log.e(e);
            AppLog.e("LinphoneCoreException");
        }
    }
}
