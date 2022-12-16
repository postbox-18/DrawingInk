package com.example.drawingink;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drawingink.UIReferenceImplementation.ConvertList;

import java.util.ArrayList;
import java.util.List;

public class AdapterText extends RecyclerView.Adapter<AdapterText.ViewHolder> {

    private String TAG = "AdapterText";
    private final Context mContext;
    private List<ConvertList> convertList = new ArrayList<>();


    public AdapterText(MainActivity context, List<ConvertList> convertList) {
        this.mContext = context;
        this.convertList= convertList;

    }

    @NonNull
    @Override
    public AdapterText.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.adapter_text, parent, false);
        return new AdapterText.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterText.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final ConvertList convertList1=convertList.get(position);

        holder.text.setText(convertList1.getText());
        holder.date.setText(convertList1.getDate());

    }



    @Override
    public int getItemCount() {
        return convertList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text,date;



        ViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            date= itemView.findViewById(R.id.date);



        }
    }


}

