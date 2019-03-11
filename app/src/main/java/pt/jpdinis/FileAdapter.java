package pt.jpdinis;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {
    private String[] mDataset;
    private int position;

    public FileAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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
        String ext = mDataset[position].substring(mDataset[position].lastIndexOf('.') + 1);

        final MyViewHolder myViewHolder = holder;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(myViewHolder.getPosition());
                return false;
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.download) {
                    MainActivity.download(v);
                } else {
                    MainActivity.upload(v);
                }
            }
        });

        holder.imageView.setBackgroundResource(R.drawable.cloudfile);

        if (ext.equals("txt")) {
            holder.imageView.setBackgroundResource(R.drawable.document);
        }

        if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif") || ext.equals("webp") || ext.equals("bmp") || ext.equals("tiff")) {
            holder.imageView.setBackgroundResource(R.drawable.image);
        }

        if (ext.equals("rar") || ext.equals("7z") || ext.equals("zip") || ext.equals("tar") || ext.equals("gz") || ext.equals("tgz")) {
            holder.imageView.setBackgroundResource(R.drawable.archive);
        }

        if (ext.equals("pdf")) {
            holder.imageView.setBackgroundResource(R.drawable.pdf);
        }

        if (ext.equals("exe")) {
            holder.imageView.setBackgroundResource(R.drawable.exe);
        }
    }

    @Override
    public void onViewRecycled(MyViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    public void addItem(String item) {
        ArrayList<String> dataset = new ArrayList<>();
        dataset.addAll(Arrays.asList(mDataset));
        dataset.add(item);
        this.mDataset = dataset.toArray(new String[0]);
        this.notifyDataSetChanged();
    }

    public void removeItem(String item) {
        ArrayList<String> dataset = new ArrayList<>();
        dataset.addAll(Arrays.asList(mDataset));
        dataset.remove(item);
        this.mDataset = dataset.toArray(new String[0]);
        this.notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataset == null) {
            return 0;
        } else {
            return mDataset.length;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public ImageView imageView;
        public TextView textView;

        public MyViewHolder(LinearLayout v) {
            super(v);
            imageView = v.findViewById(R.id.imageView2);
            textView = v.findViewById(R.id.textView);
            v.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (MainActivity.download) {
                menu.add(Menu.NONE, R.id.downloadMenuOption,
                        Menu.NONE, R.string.download);
                MenuItem download = menu.findItem(R.id.downloadMenuOption);
                download.setVisible(true);
            } else {
                menu.add(Menu.NONE, R.id.uploadMenuOption,
                        Menu.NONE, R.string.upload);
                MenuItem upload = menu.findItem(R.id.uploadMenuOption);
                upload.setVisible(true);
            }

            menu.add(Menu.NONE, R.id.deleteMenuOption,
                    Menu.NONE, R.string.delete);

        }
    }
}
