package com.example.webservicetracku;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.webservicetracku.database.entities.User;

import java.util.List;

public class Dialog_Choose_User extends DialogFragment {

    List<User> user;
    List<String> userEmail;
    String[] name;
    String[] emails = {"1","2","3","4","5"};
    ArrayAdapter<String> users;

    public interface DialogChooserInterface{
        void OnClickListener(View view, int position, long id, String user);
    }

    DialogChooserInterface dialogChooserInterface;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_user,null);

        ListView listView = v.findViewById(R.id.listUser);


        /*name = new String[user.size()];
        for(int i=0; i<user.size();i++){
            name[i]=user.get(i).email;
        }*/

        users = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,userEmail);
        listView.setAdapter(users);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialogChooserInterface.OnClickListener(view, position, id, userEmail.get(position));
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(v);

        return builder.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            dialogChooserInterface = (DialogChooserInterface) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
