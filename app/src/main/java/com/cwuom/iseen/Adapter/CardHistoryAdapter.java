package com.cwuom.iseen.Adapter;

import static android.content.Context.MODE_PRIVATE;
import static com.cwuom.iseen.Util.UtilMethod.showDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cwuom.iseen.Dao.CardDao;
import com.cwuom.iseen.Dao.UserDao;
import com.cwuom.iseen.Entity.EntityCard;
import com.cwuom.iseen.Entity.EntityCardHistory;
import com.cwuom.iseen.InitDataBase.InitCardDataBase;
import com.cwuom.iseen.InitDataBase.InitUserDataBase;
import com.cwuom.iseen.R;
import com.cwuom.iseen.Util.API.Ark.ArkAPIReq;
import com.cwuom.iseen.Util.API.Ark.ArkApiCallback;
import com.cwuom.iseen.Util.UtilMethod;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.interfaces.BaseDialog;
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import io.noties.markwon.Markwon;

/*
 * This software is provided for educational purposes only and should not be used for commercial or illegal activities.
 * Please respect the original author's work by retaining their information intact.
 * If you make modifications to this code, you can repackage it accordingly.
 *
 * Original Author: cwuom
 * Date: 2024.3.1
 *
 * Instructions:
 * 1. Make necessary modifications.
 * 2. Rebuild the app.
 * 3. Retain this header.
 *
 * Thank you!
 */

/**
 * 卡片历史记录适配器
 * ----------------------
 * @author cwuom
 * 下列源码支持变动后重新打包，在变动不大的情况下，请尽量保留作者的信息！
 * 请勿用于商业用途和非法用途。
 * ----------------------
 * */

public class CardHistoryAdapter extends RecyclerView.Adapter<CardHistoryAdapter.ViewHolder> {
    ArrayList<EntityCardHistory> list;
    Context context;
    int deletePos;
    int refreshPos;
    final Handler handler = new Handler();

    InitCardDataBase initCardDataBase;
    InitUserDataBase initUserDataBase;
    CardDao cardDao;
    CountListen countListen;
    ActionListen actionListen;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    UserDao userDao;

    public CardHistoryAdapter(ArrayList<EntityCardHistory> list, Context context, CountListen countListen, ActionListen actionListen) {
        this.list = list;
        this.context = context;
        this.countListen = countListen;
        this.actionListen = actionListen;
        initCardDataBase = UtilMethod.getInstance(context);
        cardDao = initCardDataBase.cardDao();
    }

    @NonNull
    @Override
    public CardHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardHistoryAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        sharedPreferences = context.getSharedPreferences("config", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initUserDataBase = UtilMethod.getInstance_user(context.getApplicationContext());

        userDao = initUserDataBase.userDao();

        if (position == holder.getLayoutPosition()){
            holder.tv_card_head_title.setText(list.get(position).getCardHeadTitle());
            holder.tv_card_head_sub.setText(list.get(position).getCardHeadSubtitle());
            holder.tv_card_content_title.setText(list.get(position).getCardNote());
            final Markwon markwon = Markwon.create(context);
            markwon.setMarkdown(holder.tv_card_content_callback, list.get(position).getCardContentCallback());
            holder.tv_card_create_time.setText(list.get(position).getCardCreateTime());

            holder.delete.setOnClickListener(v -> new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("确认删除么？")
                    .setMessage("删除此条卡片后在本地不可恢复，但服务器上的文件不会一并清除。您仍有权限可通过指定网页链接对其进行访问！")
                    .setNeutralButton("手滑了..", null)
                    .setPositiveButton("确认删除", (dialog, which) -> {
                        deletePos = holder.getLayoutPosition();
                        deleteCard();
                        countListen.countListen(list.size());
                    })
                    .show());

            holder.btn_more_actions.setOnClickListener(v -> {
                int pos = holder.getLayoutPosition();
                EntityCard entityCard = cardDao.getCardByID(list.get(pos).getHistoryCardId());
                BottomMenu.show(new String[]{"复制卡片代码", "跳转到监听地址", "后台自动监听"})
                        .setOnIconChangeCallBack(new OnIconChangeCallBack(true) {
                            @Override
                            public int getIcon(BaseDialog dialog, int index, String menuText) {
                                switch (index){
                                    case 0:
                                        return R.drawable.baseline_file_copy_24;
                                    case 1:
                                        return R.drawable.gotobrowser;
                                    case 2:
                                        return R.drawable.baseline_auto_awesome_24;
                                }
                                return 0;
                            }
                        })
                        .setOnMenuItemClickListener((dialog, text, index) -> {
                            actionListen.actionListen(index, entityCard, list.get(pos), pos);
                            return false;
                        });
            });

            holder.btn_get.setOnClickListener(v -> {
                refreshPos = holder.getLayoutPosition();
                refreshCard();
            });

            holder.tv_card_content_callback.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    EntityCard entityCard = cardDao.getCardByID(list.get(pos).getHistoryCardId());
                    BottomMenu.show(new String[]{"复制全部内容", "解析内容(复制部分内容)"})
                            .setOnIconChangeCallBack(new OnIconChangeCallBack(true) {
                                @Override
                                public int getIcon(BaseDialog dialog, int index, String menuText) {
                                    switch (menuText) {
                                        case "复制全部内容":
                                            return R.drawable.baseline_file_copy_24;
                                        case "解析内容(复制部分内容)":
                                            return R.drawable.baseline_analytics_24;
                                    }
                                    return 0;
                                }
                            })
                            .setOnMenuItemClickListener((dialog, text, index) -> {
                                actionListen.actionListen(-index-3, entityCard, list.get(pos), pos);
                                return false;
                            });
                    return true;
                }
            });
        }
    }

    @SuppressLint("RestrictedApi")
    private void refreshCard() {
        EntityCard entityCard = cardDao.getCardByID(list.get(refreshPos).getHistoryCardId());

        ArkAPIReq.getArkListenerReturn(
                entityCard.getCardListenerUrl(),
                context.getApplicationContext(),
                true,
                new ArkApiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e("OkHttp GET请求成功", result);
                        actionListen.actionListen(-2, null, null, 0);
                        changeItemCallback(result);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (e instanceof SocketTimeoutException) {
                                showDialog("请求超时！", "无法与服务器建立连接，请检查网络后重试！", context);
                            } else if (e instanceof ConnectException) {
                                showDialog("请求连接异常！", "无法正确读取通讯数据，请检查网络后重试！", context);
                            } else {
                                showDialog("请求错误！", e.getMessage(), context);
                            }
                        });
                    }
                }
        );
        actionListen.actionListen(-1, null, null, 0);
    }

    void changeItemCallback(String res){
        handler.post(() -> {
            list.get(refreshPos).setCardContentCallback(res);
            notifyItemChanged(refreshPos);
        });
    }

    private void deleteCard() {
        notifyItemRemoved(deletePos);
        cardDao.deleteCardById(list.get(deletePos).getHistoryCardId());
        list.remove(deletePos);
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_card_head_title;
        TextView tv_card_head_sub;
        TextView tv_card_content_title;
        TextView tv_card_content_callback;
        TextView tv_card_create_time;
        Button delete;
        Button btn_get;
        Button btn_more_actions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_card_head_title = itemView.findViewById(R.id.card_head_title);
            tv_card_head_sub = itemView.findViewById(R.id.card_head_sub);
            tv_card_content_title = itemView.findViewById(R.id.card_content_title);
            tv_card_content_callback = itemView.findViewById(R.id.card_content_callback);
            tv_card_create_time = itemView.findViewById(R.id.card_create_time);
            delete = itemView.findViewById(R.id.del);
            btn_get = itemView.findViewById(R.id.get);
            btn_more_actions = itemView.findViewById(R.id.more_actions);
        }

    }

    public interface CountListen{
        void countListen(int count);
    }

    public interface ActionListen{
        @SuppressLint("NotConstructor")
        void actionListen(int action, EntityCard entityCard, EntityCardHistory entityCardHistory, int pos);
    }

}
