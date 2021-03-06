package ca.barelabs.baretask;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Bundle;

@TargetApi(11)
public class RetainedNativeFragment extends Fragment {

    public static final String KILLED_TASK_IDS_KEY = RetainedNativeFragment.class.getName() + ".killedTaskIds";

    private final TaskManager mManager = new TaskManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        // If this is being called with a Bundle then the activity was killed by the OS and re-created
        if(savedInstanceState != null) {
            mManager.setKilledTaskIds(savedInstanceState.getLongArray(KILLED_TASK_IDS_KEY));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mManager.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLongArray(KILLED_TASK_IDS_KEY, mManager.getTaskIds());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mManager.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManager.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mManager.onDetach();
    }

    public TaskManager getTaskManager() {
        return mManager;
    }
}
