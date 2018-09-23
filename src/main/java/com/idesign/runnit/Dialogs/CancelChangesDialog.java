package com.idesign.runnit.Dialogs;

import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.idesign.runnit.R;

public class CancelChangesDialog extends DialogFragment
{
  private AlertDialog.Builder builder;
  private CancelChangesListener mListener;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(R.string.cancel_changes_dialog_title)
    .setPositiveButton(R.string.yes, (dialog, which) -> confirm(which))
    .setNegativeButton(R.string.no, (dialog, which) -> cancel(which));
    return builder.create();
  }

  public void confirm(int which)
  {
    mListener.confirmNoChanges();
  }
  public void cancel(int which)
  {
    mListener.cancelNoChanges();
  }

  @Override
  public void onAttach(Context context)
  {
    super.onAttach(context);
    try {
      mListener = (CancelChangesListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " Must implement listener");
    }
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
    mListener.onDialogDismiss();
    mListener = null;
  }

  public interface CancelChangesListener {
    void confirmNoChanges();
    void cancelNoChanges();
    void onDialogDismiss();
  }
}
