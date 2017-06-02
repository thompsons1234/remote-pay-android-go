package com.clover.remote.client.lib.example.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;

/**
 * Created by Avdhesh Akhani on 1/12/17.
 */

public class DialogHelper {


    public static Dialog createMessageDialog(Context context, String title, String message, String btnName, DialogInterface.OnClickListener listener)
    {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(btnName, listener);
        dialog = builder.create();

        return dialog;
    }

    public static Dialog createConfirmDialog(Context context, String title, String message, String positiveBtnName,
                                             String negativeBtnName, DialogInterface.OnClickListener positiveBtnListener,
                                             DialogInterface.OnClickListener negativeBtnListener)
    {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveBtnName, positiveBtnListener);
        builder.setNegativeButton(negativeBtnName, negativeBtnListener);
        dialog = builder.create();

        return dialog;
    }

    public static ProgressDialog showProgressDialog(Context context, String title, String message, boolean isCancelable, String btnName,DialogInterface.OnClickListener listener){
        ProgressDialog m_Dialog = new ProgressDialog(context);
        m_Dialog.setMessage(message);
        m_Dialog.setTitle(title);
        m_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_Dialog.setIndeterminate(true);
        if (isCancelable){
            m_Dialog.setCancelable(isCancelable);
            m_Dialog.setButton(DialogInterface.BUTTON_NEGATIVE,btnName,listener);
        }

        return m_Dialog;
    }


    public static AlertDialog createAlertDialog(Context context, String title, String message, String btnName, DialogInterface.OnClickListener listener)
    {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(btnName, listener);
        dialog = builder.create();

        return dialog;
    }

}
