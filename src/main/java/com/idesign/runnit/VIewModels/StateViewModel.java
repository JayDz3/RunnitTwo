package com.idesign.runnit.VIewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.idesign.runnit.Items.StateEmitter;

public class StateViewModel extends ViewModel
{
  private final MutableLiveData<StateEmitter> mStateEmitter = new MutableLiveData<>();

  public LiveData<StateEmitter> getStateEmitter()
  {
    if (mStateEmitter.getValue() == null)
    {
      StateEmitter emitter = new StateEmitter();
      emitter.setFragmentState(-1);
      mStateEmitter.setValue(emitter);
    }
    return mStateEmitter;
  }

  private void setEmitter(StateEmitter emitter)
  {
    mStateEmitter.setValue(emitter);
  }

  public void setFragmentState(int state)
  {
    if (mStateEmitter.getValue() != null)
      mStateEmitter.getValue().setFragmentState(state);
    setEmitter(getStateEmitter().getValue());
  }
}
