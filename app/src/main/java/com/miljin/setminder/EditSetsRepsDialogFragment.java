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

public class EditSetsRepsDialogFragment extends DialogFragment {
    private NumberPicker pickerSets;
    private NumberPicker pickerReps;

    public interface EditSetsRepsDialogListener {
        void onFinishEditSetsRepsDialog(int[] inputSetsReps);
    }

    public EditSetsRepsDialogFragment(){
        // Empty constructor required for DialogFragment
    }

    public static EditSetsRepsDialogFragment newInstance(int[] setsReps) {
        EditSetsRepsDialogFragment frag = new EditSetsRepsDialogFragment();
        Bundle args = new Bundle();
        args.putIntArray("setsReps", setsReps);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.miljin.setminder.R.layout.fragment_edit_sets_reps_dialog, container);
        int[] setsReps = getArguments().getIntArray("setsReps");

        pickerSets = (NumberPicker) view.findViewById(com.miljin.setminder.R.id.pick_sets);
        pickerSets.setMaxValue(99);
        pickerSets.setMinValue(0);
        pickerSets.setValue(setsReps[0]);

        pickerReps = (NumberPicker) view.findViewById(com.miljin.setminder.R.id.pick_reps);
        pickerReps.setMaxValue(99);
        pickerReps.setMinValue(0);
        pickerReps.setValue(setsReps[1]);

        Button btnSubmit = (Button) view.findViewById(com.miljin.setminder.R.id.submit_sets_reps_button);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] setsReps = new int[] {pickerSets.getValue(), pickerReps.getValue()};
                EditSetsRepsDialogListener listener = (EditSetsRepsDialogListener) getActivity();
                listener.onFinishEditSetsRepsDialog(setsReps);
                dismiss();
            }
        });
        getDialog().setTitle(getResources().getString(com.miljin.setminder.R.string.edit_sets_reps_title));
        return view;
    }
}
