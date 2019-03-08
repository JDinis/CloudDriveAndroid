package pt.jpdinis;

import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {
    private String[] mDataset;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public MyViewHolder(LinearLayout v) {
            super(v);
            imageView = v.findViewById(R.id.imageView2);
            textView = v.findViewById(R.id.textView);
        }
    }

    public FileAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    @Override
    public FileAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filelistitem, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(mDataset[position]);
        String ext = mDataset[position].substring(mDataset[position].lastIndexOf('.')+1);

        holder.imageView.setBackgroundResource(R.drawable.cloudfile);

        if(ext.equals("txt")){
            holder.imageView.setBackgroundResource(R.drawable.document);
        }

        if(ext.equals("png")||ext.equals("jpg")||ext.equals("jpeg")||ext.equals("gif")||ext.equals("webp")||ext.equals("bmp")||ext.equals("tiff")){
            holder.imageView.setBackgroundResource(R.drawable.image);
        }

        if(ext.equals("rar")||ext.equals("7z")||ext.equals("zip")||ext.equals("tar")||ext.equals("gz")||ext.equals("tgz")){
            holder.imageView.setBackgroundResource(R.drawable.archive);
        }

        if(ext.equals("pdf")){
            holder.imageView.setBackgroundResource(R.drawable.pdf);
        }

        if(ext.equals("exe")){
            holder.imageView.setBackgroundResource(R.drawable.exe);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
