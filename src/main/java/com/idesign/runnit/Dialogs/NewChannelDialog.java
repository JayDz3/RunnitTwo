package com.idesign.runnit.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.idesign.runnit.R;


public class NewChannelDialog extends DialogFragment
{
  private AlertDialog.Builder builder;
  private ChannelDialogListener mListener;
  private EditText editText;

  @Override
  @NonNull
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.channel_dialog, null);
    editText = view.findViewById(R.id.channel_dialog_edit_text);

    builder.setView(view)
    .setPositiveButton(R.string.confirm, (dialog, which) -> confirm(which))
    .setNegativeButton(R.string.cancel, (dialog, which) -> mListener.onCancel(which));
    return builder.create();
  }

  public void confirm(int which)
  {
    String name = editText.getText().toString();
    mListener.onConfirm(which, name);
  }

  @Override
  public void onAttach(Context context)
  {
    super.onAttach(context);
    try {
      mListener = (ChannelDialogListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " Must implement Channel Dialog Listener");
    }
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
    mListener = null;
  }

  public interface ChannelDialogListener {
    void onConfirm(int which, String name);
    void onCancel(int which);
  }
}
