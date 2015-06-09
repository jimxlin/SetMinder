/*
 * Copyright (c) 2015 Jim X. Lin
 *
 * This file is part of SetMinder.
 *
 *  SetMinder is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SetMinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SetMinder.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.miljin.setminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

public class ResetAlertDialogFragment extends DialogFragment {

    public interface ResetAlertListener {
        void onResetOKButton();
        void onResetCancelButton();
    }

    private ResetAlertListener mListener;

    public ResetAlertDialogFragment() {
        // Required empty public constructor
    }

    public static ResetAlertDialogFragment newInstance() {
        ResetAlertDialogFragment frag = new ResetAlertDialogFragment();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(com.miljin.setminder.R.string.alert_reset);
        mListener = (ResetAlertListener) getActivity();

        alertDialogBuilder.setPositiveButton(com.miljin.setminder.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onResetOKButton();
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton(com.miljin.setminder.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onResetCancelButton();
                dialog.dismiss();
            }
        });

        Dialog dialog = alertDialogBuilder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getWindow().getAttributes();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTextSize(20);

        return dialog;
    }
}
