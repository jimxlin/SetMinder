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

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

public class EditTimeDialogFragment extends DialogFragment {
    private NumberPicker pickerMinutes;
    private NumberPicker pickerSeconds;

    public interface EditTimeDialogListener {
        void onFinishEditTimeDialog(long inputSeconds);
    }

    public EditTimeDialogFragment(){
        // Empty constructor required for DialogFragment
    }

    public static EditTimeDialogFragment newInstance(long time) {
        EditTimeDialogFragment frag = new EditTimeDialogFragment();
        Bundle args = new Bundle();
        args.putLong("time", time);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.miljin.setminder.R.layout.fragment_edit_time_dialog, container);
        long time = getArguments().getLong("time");

        pickerMinutes = (NumberPicker) view.findViewById(com.miljin.setminder.R.id.pick_minutes);
        pickerMinutes.setMaxValue(60);
        pickerMinutes.setMinValue(0);
        pickerMinutes.setValue((int) time/60);
        pickerMinutes.setFormatter(new NumberPicker.Formatter() {
            public String format(int i){
                return String.format("%02d", i);
            }
        });

        pickerSeconds = (NumberPicker) view.findViewById(com.miljin.setminder.R.id.pick_seconds);
        pickerSeconds.setMaxValue(59);
        pickerSeconds.setMinValue(0);
        pickerSeconds.setValue((int) time%60);
        pickerSeconds.setFormatter(new NumberPicker.Formatter() {
            public String format(int i){
                return String.format("%02d", i);
            }
        });

        Button btnSubmit = (Button) view.findViewById(com.miljin.setminder.R.id.submit_time_button);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditTimeDialogListener listener = (EditTimeDialogListener) getActivity();
                listener.onFinishEditTimeDialog(pickerMinutes.getValue() * 60 + pickerSeconds.getValue());
                dismiss();
            }
        });
        getDialog().setTitle(getResources().getString(com.miljin.setminder.R.string.edit_time_title));
        return view;
    }
}
