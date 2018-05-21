package baby.watching.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import baby.watching.R;
import baby.watching.loc.AppConstants;
import baby.watching.model.NotificationBean;
import baby.watching.view.MyProgressView;

import static baby.watching.loc.AppConstants.animateProgress;

/**
 * Created by mohit.soni on 27-Dec-17.
 */

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {
    private static String TAG = "RecycleAdapter";
    List<NotificationBean> data;
    Context context;
    HashMap<Integer,Boolean> checkAnimation = new HashMap<>();
    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView noti_icon;
        MyProgressView myProgressView1;
        RelativeLayout rlBadge;
        TextView tvBadge;

        public ViewHolder(View v){
            super(v);
            noti_icon = (ImageView) v.findViewById(R.id.noti_icon);
            myProgressView1 = (MyProgressView) v.findViewById(R.id.arc_one);
            rlBadge = (RelativeLayout) v.findViewById(R.id.rlBadge);
            tvBadge = (TextView)v.findViewById(R.id.tvBadge);
        }
    }

    public RecycleAdapter(Context context,List<NotificationBean> data){
        this.data = data;
        this.context = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.noti_icon,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotificationBean statusBarNotification = data.get(position);
        try {
            Drawable icon = context.getPackageManager().getApplicationIcon(statusBarNotification.getPackageName());
            holder.noti_icon.setBackground(icon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(statusBarNotification.getCount() > 1){
            holder.rlBadge.setVisibility(View.VISIBLE);
            holder.tvBadge.setText(statusBarNotification.getCount()+"");
        }

        holder.myProgressView1.setStartingDegree(270);
        holder.myProgressView1.setStrokeWidth(2);

        animateProgress(holder.myProgressView1, 100);

        // animate icon
        Animation ani = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.fade_in);
        ani.setDuration(AppConstants.ANIMATION_SPEED + 1000);
        ani.setInterpolator(new AccelerateInterpolator());
        holder.noti_icon.setAnimation(ani);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    public void log(String msg){
        Log.i(TAG,msg);
    }
}
