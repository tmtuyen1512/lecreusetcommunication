package com.delfi.xmobile.app.lecreusetcommunication.model;

import android.content.Context;
import android.widget.EditText;

import com.delfi.xmobile.app.lecreusetcommunication.ModuleApp;
import com.delfi.xmobile.lib.xcore.template_v2.interfaces.AItem;
import com.delfi.xmobile.lib.xcore.template_v2.model.Element;

import java.util.HashMap;
import java.util.List;

/**
 * Created by USER on 05/22/2020.
 */
public abstract class BaseItem extends AItem {

    @Override
    public Context getAppContext() {
        return ModuleApp.getInstance();
    }

    @Override
    public String getRecordStringOutputFile() {
        return null;
    }

    @Override
    public String getConfirmDeleteString() {
        return null;
    }

    @Override
    public String getConfirmMultiDeleteString(List list) {
        return null;
    }

    @Override
    public List<Element> getElementTemplate() {
        return null;
    }

    @Override
    public void onUpdateValue(HashMap<String, String> values) {

    }

    @Override
    public String validateRules(EditText edInput, String key) {
        return null;
    }

    @Override
    public String validateRules_Async(Context context, EditText editText) {
        return null;
    }
}
