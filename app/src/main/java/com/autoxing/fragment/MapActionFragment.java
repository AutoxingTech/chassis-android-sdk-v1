package com.autoxing.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.autoxing.controller.R;
import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.bean.MoveDirection;
import com.autoxing.robot_core.util.CommonCallback;
import com.autoxing.robot_core.util.ThreadPoolUtil;

public class MapActionFragment extends Fragment implements View.OnTouchListener {

    private View mLayout = null;
    private ImageButton mUp;
    private ImageButton mRight;
    private ImageButton mLeft;
    private ImageButton mDown;

    private boolean mCanContinue = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.map_action_layout, container, false);
            initView(mLayout);
            setListener();
        } else {
            ViewGroup viewGroup = (ViewGroup) mLayout.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mLayout);
            }
        }

        return mLayout;
    }

    private void initView(View view) {
        mUp = view.findViewById(R.id.ibtn_up);
        mRight = view.findViewById(R.id.ibtn_right);
        mLeft = view.findViewById(R.id.ibtn_left);
        mDown = view.findViewById(R.id.ibtn_down);
    }

    private void setListener() {
        mUp.setOnTouchListener(this);
        mRight.setOnTouchListener(this);
        mLeft.setOnTouchListener(this);
        mDown.setOnTouchListener(this);
    }

    private void moveWithAction(MoveDirection direction) {
        mCanContinue = false;
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                boolean succ = AXRobotPlatform.getInstance().moveWithAction(direction);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!succ) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("failed to ");
                            sb.append(direction.toString().toLowerCase().replace("_", " "));
                            Toast.makeText(getActivity(), sb.toString(), 1200).show();
                        } else {
                            mCanContinue = true;
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE && mCanContinue) {
            switch (v.getId()) {
                case R.id.ibtn_up:
                    moveWithAction(MoveDirection.GO_FORWARD);
                    break;
                case R.id.ibtn_left:
                    moveWithAction(MoveDirection.TURN_LEFT);
                    break;
                case R.id.ibtn_right:
                    moveWithAction(MoveDirection.TURN_RIGHT);
                    break;
                case R.id.ibtn_down:
                    moveWithAction(MoveDirection.GO_BACKWARD);
                    break;
                default:
                    break;
            }
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mCanContinue = true;
        }
        return false;
    }
}
