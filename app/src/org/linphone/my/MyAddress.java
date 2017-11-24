package org.linphone.my;

import org.linphone.LinphoneManager;

/**
 * Created by qckiss on 2017/11/22.
 */

public class MyAddress implements LinphoneManager.AddressType {
    private String text;
    private String displayedName;
    @Override
    public void setText(CharSequence s) {
        text = s.toString();
    }

    @Override
    public CharSequence getText() {
        return text;
    }

    @Override
    public void setDisplayedName(String s) {
        displayedName = s;
    }

    @Override
    public String getDisplayedName() {
        return displayedName;
    }
}
