package example.prgguru.com.songswiper;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import at.markushi.ui.CircleButton;
import example.prgguru.com.songswiper.data.UserProvider;
import example.prgguru.com.songswiper.model.MOVIESONGS;

public class MainActivity extends AppCompatActivity {
    public ArrayList<MOVIESONGS> moviesongsArrayList;
    public SharedPreferences.Editor editormainactivity;
    public SharedPreferences sharedPreferences;
    public RecyclerView recyclerView;
    public MovieSongsRecyclerViewAdapter adapter;
    private ProgressDialog dialog;
    public CircleButton likeImageView;
    public CircleButton deleteImageView;
    private AlertDialog.Builder alertDialog;
    private AlertDialog dialogAlert;
    private int edit_position;
    private View view;

    private Paint p = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SONGS");

        dialog = new ProgressDialog(this);

        final SharedPreferences insertDataFirstTime = this.getSharedPreferences("insertDataFirstTime", Context.MODE_PRIVATE);

        editormainactivity = insertDataFirstTime.edit();

        if(insertDataFirstTime.getBoolean("insertDataFirstTime",false) ==  false) {
            editormainactivity.putBoolean("insertDataFirstTime", true);
            editormainactivity.commit();

            new AsyncListViewLoaderTiled().execute("Load Data for first time");


        }

        initViews();
        initDialog();



        
    }

    private void initViews(){
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        moviesongsArrayList = BrowseMovieSongs.getSongsInDirectory(this);
        adapter = new MovieSongsRecyclerViewAdapter(this,moviesongsArrayList );
        recyclerView.setAdapter(adapter);
        initSwipe();

    }

    private void initSwipe(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                    removeView();
                    edit_position = position;
                    dialogAlert = alertDialog.show();

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background, p);

                    } else {
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, p);

                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    private void removeView(){
        if(view.getParent()!=null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }
    private void initDialog(){
        alertDialog = new AlertDialog.Builder(this);
        view = getLayoutInflater().inflate(R.layout.dialog_layout,null);
        alertDialog.setCancelable(false);
        alertDialog.setView(view);
        alertDialog.setTitle("LIKE/DELETE THIS SONG");
        likeImageView = (CircleButton) view.findViewById(R.id.likeButton);
        deleteImageView = (CircleButton) view.findViewById(R.id.deleteButton);

        alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    adapter.notifyDataSetChanged();
                    dialogAlert.dismiss();


                }

                return true;
            }
        });

        likeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.hyperspace_out));
                ContentValues values = new ContentValues();

                values.put(UserProvider._SONG_LIKED,"1");
                String where = UserProvider._MOVIE_ID + " =? AND "+ UserProvider._SONG_ID +" =?";
                String[] args = {String.valueOf(moviesongsArrayList.get(edit_position).getMid()),String.valueOf(moviesongsArrayList.get(edit_position).getSid())};
                ContentResolver resolver = getApplicationContext().getContentResolver();

                int rowsupdated = resolver.update(UserProvider.CONTENT_URI_MOVIES,values,where,args);

                Toast.makeText(getApplicationContext(),"You Liked this song",Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                dialogAlert.dismiss();

            }
        });

        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                v.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.hyperspace_out));
                ContentValues values = new ContentValues();

                values.put(UserProvider._SONG_DELETE,"1");
                String where = UserProvider._MOVIE_ID + " =? AND "+ UserProvider._SONG_ID +" =?";
                String[] args = {String.valueOf(moviesongsArrayList.get(edit_position).getMid()),String.valueOf(moviesongsArrayList.get(edit_position).getSid())};
                ContentResolver resolver = getApplicationContext().getContentResolver();

                int rowsupdated = resolver.update(UserProvider.CONTENT_URI_MOVIES,values,where,args);
                adapter.removeAt(edit_position);
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(),"You Deleted this song",Toast.LENGTH_SHORT).show();
                dialogAlert.dismiss();

            }
        });

        dialogAlert = alertDialog.create();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class MovieSongsRecyclerViewAdapter
            extends RecyclerView.Adapter<MovieSongsRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<MOVIESONGS> mValues;
        private Context mContext;
        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
            public String mBoundString;

            public final View mView;

            public final TextView mSongNameTextView;
            public final TextView mMovieNameTextView;
            public ViewHolder(View view) {
                super(view);
                mView = view;

                mSongNameTextView = (TextView) view.findViewById(R.id.textSongName);
                mMovieNameTextView = (TextView) view.findViewById(R.id.textMovieName);


            }

            @Override
            public String toString() {
                return super.toString() + " '" + mSongNameTextView.getText();
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            }
        }

        public MOVIESONGS getValueAt(int position) {
            return mValues.get(position);
        }

        public MovieSongsRecyclerViewAdapter(Context context, List<MOVIESONGS> items) {
            mValues = items;
            mContext = context;

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_moviesongs_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            String sname = mValues.get(position).getSname();
            String mname = mValues.get(position).getMname();
            holder.mSongNameTextView.setText(sname);
            holder.mMovieNameTextView.setText(mname);
            holder.mMovieNameTextView.setTypeface(null, Typeface.ITALIC);



        }

        @Override
        public int getItemCount() {
            if(mValues != null && mValues.size() > 0)
                return mValues.size();
            else
                return 0;
        }

        public void removeAt(int position) {
            mValues.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mValues.size());
        }


    }

    private class AsyncListViewLoaderTiled extends AsyncTask<String, Void, Void> {
        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        InputStreamReader inputStream = null;
        String result = "";
        String listName;

        @Override
        protected void onPostExecute(Void xyz) {
            super.onPostExecute(xyz);

            if ((dialog != null) && dialog.isShowing()) {
                dialog.dismiss();


                recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
                recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
                moviesongsArrayList = BrowseMovieSongs.getSongsInDirectory(getApplicationContext());
                adapter = new MovieSongsRecyclerViewAdapter(MainActivity.this,moviesongsArrayList );
                recyclerView.setAdapter(adapter);
                initViews();
                initDialog();
            }

            }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Fetching Movies and Songs for the first time");
            dialog.setProgressStyle(ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
            dialog.setCancelable(false);
            dialog.show();


        }

        @Override
        protected Void doInBackground(String... params) {

            ContentValues values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,1);
            values.put(UserProvider._MOVIE_NAME, "Azhar");
            values.put(UserProvider._SONG_ID, 1);
            values.put(UserProvider._SONG_NAME, "BOL DO NA ZARA");
            values.put(UserProvider._SONG_LIKED, "0");
            values.put(UserProvider._SONG_DELETE, "0");

            Uri uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID, 1);
            values.put(UserProvider._MOVIE_NAME, "Azhar");
            values.put(UserProvider._SONG_ID, 2);
            values.put(UserProvider._SONG_NAME,"ITNI MOHABBAT");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,1);
            values.put(UserProvider._MOVIE_NAME, "Azhar");
            values.put(UserProvider._SONG_ID, 3);
            values.put(UserProvider._SONG_NAME,"OYE OYE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,1);
            values.put(UserProvider._MOVIE_NAME, "Azhar");
            values.put(UserProvider._SONG_ID, 4);
            values.put(UserProvider._SONG_NAME,"TU HI NAA JAANE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,1);
            values.put(UserProvider._MOVIE_NAME, "Azhar");
            values.put(UserProvider._SONG_ID, 5);
            values.put(UserProvider._SONG_NAME,"JEETNE KE LIYE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,2);
            values.put(UserProvider._MOVIE_NAME, "SARABJIT");
            values.put(UserProvider._SONG_ID, 1);
            values.put(UserProvider._SONG_NAME,"SALAMAT");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,2);
            values.put(UserProvider._MOVIE_NAME, "SARABJIT");
            values.put(UserProvider._SONG_ID, 2);
            values.put(UserProvider._SONG_NAME,"DARD");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,2);
            values.put(UserProvider._MOVIE_NAME, "SARABJIT");
            values.put(UserProvider._SONG_ID, 3);
            values.put(UserProvider._SONG_NAME,"RABBAs");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,3);
            values.put(UserProvider._MOVIE_NAME, "SULTAN");
            values.put(UserProvider._SONG_ID, 1);
            values.put(UserProvider._SONG_NAME,"BABY KO BASS");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,3);
            values.put(UserProvider._MOVIE_NAME, "SULTAN");
            values.put(UserProvider._SONG_ID, 2);
            values.put(UserProvider._SONG_NAME,"JAG GHOOMEYA");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,3);
            values.put(UserProvider._MOVIE_NAME, "SULTAN");
            values.put(UserProvider._SONG_ID, 3);
            values.put(UserProvider._SONG_NAME,"440 VOLT");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,3);
            values.put(UserProvider._MOVIE_NAME, "SULTAN");
            values.put(UserProvider._SONG_ID, 4);
            values.put(UserProvider._SONG_NAME,"SULTAN");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,3);
            values.put(UserProvider._MOVIE_NAME, "SULTAN");
            values.put(UserProvider._SONG_ID, 5);
            values.put(UserProvider._SONG_NAME,"SACCHI MUCCHI");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,3);
            values.put(UserProvider._MOVIE_NAME, "SULTAN");
            values.put(UserProvider._SONG_ID, 6);
            values.put(UserProvider._SONG_NAME,"BULLEYA");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,4);
            values.put(UserProvider._MOVIE_NAME, "TE3N");
            values.put(UserProvider._SONG_ID, 1);
            values.put(UserProvider._SONG_NAME,"HAQ HAI");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,4);
            values.put(UserProvider._MOVIE_NAME, "TE3N");
            values.put(UserProvider._SONG_ID, 2);
            values.put(UserProvider._SONG_NAME,"KYUN RE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,4);
            values.put(UserProvider._MOVIE_NAME, "TE3N");
            values.put(UserProvider._SONG_ID, 3);
            values.put(UserProvider._SONG_NAME,"GRAHAN");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,4);
            values.put(UserProvider._MOVIE_NAME, "TE3N");
            values.put(UserProvider._SONG_ID, 4);
            values.put(UserProvider._SONG_NAME,"ROOTHA");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,4);
            values.put(UserProvider._MOVIE_NAME, "TE3N");
            values.put(UserProvider._SONG_ID, 5);
            values.put(UserProvider._SONG_NAME,"SACCHI MUCCHI");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,4);
            values.put(UserProvider._MOVIE_NAME, "TE3N");
            values.put(UserProvider._SONG_ID, 6);
            values.put(UserProvider._SONG_NAME,"KYUN RE (VERSION) 1");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,5);
            values.put(UserProvider._MOVIE_NAME, "UDTA PUNJAB");
            values.put(UserProvider._SONG_ID, 1);
            values.put(UserProvider._SONG_NAME,"CHITTA VE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,5);
            values.put(UserProvider._MOVIE_NAME, "UDTA PUNJAB");
            values.put(UserProvider._SONG_ID, 2);
            values.put(UserProvider._SONG_NAME,"IKK KUDI");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,5);
            values.put(UserProvider._MOVIE_NAME, "DA DA DASSE");
            values.put(UserProvider._SONG_ID, 3);
            values.put(UserProvider._SONG_NAME,"GRAHAN");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,5);
            values.put(UserProvider._MOVIE_NAME, "UDTA PUNJAB");
            values.put(UserProvider._SONG_ID, 4);
            values.put(UserProvider._SONG_NAME,"UD-DAA PUNJAB");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,5);
            values.put(UserProvider._MOVIE_NAME, "UDTA PUNJAB");
            values.put(UserProvider._SONG_ID, 5);
            values.put(UserProvider._SONG_NAME,"HASS NACHLE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,5);
            values.put(UserProvider._MOVIE_NAME, "UDTA PUNJAB");
            values.put(UserProvider._SONG_ID, 6);
            values.put(UserProvider._SONG_NAME,"VADIYA");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,6);
            values.put(UserProvider._MOVIE_NAME, "ABCD 2");
            values.put(UserProvider._SONG_ID, 1);
            values.put(UserProvider._SONG_NAME,"BEZUBAN PHIR SE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,6);
            values.put(UserProvider._MOVIE_NAME, "ABCD 2");
            values.put(UserProvider._SONG_ID, 2);
            values.put(UserProvider._SONG_NAME,"SUN SATHIYA");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,6);
            values.put(UserProvider._MOVIE_NAME, "ABCD 2");
            values.put(UserProvider._SONG_ID, 3);
            values.put(UserProvider._SONG_NAME,"CHUNAR");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,6);
            values.put(UserProvider._MOVIE_NAME, "ABCD 2");
            values.put(UserProvider._SONG_ID, 4);
            values.put(UserProvider._SONG_NAME,"HAPPY BIRTHDAY");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,6);
            values.put(UserProvider._MOVIE_NAME, "ABCD 2");
            values.put(UserProvider._SONG_ID, 5);
            values.put(UserProvider._SONG_NAME,"IF YOU HOLD MY HAND");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,6);
            values.put(UserProvider._MOVIE_NAME, "ABCD 2");
            values.put(UserProvider._SONG_ID, 6);
            values.put(UserProvider._SONG_NAME,"NAACH MERI JAAN");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,7);
            values.put(UserProvider._MOVIE_NAME, "AASHIQUI 2");
            values.put(UserProvider._SONG_ID, 1);
            values.put(UserProvider._SONG_NAME,"SUN RAHA HAI NA");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,7);
            values.put(UserProvider._MOVIE_NAME, "AASHIQUI 2");
            values.put(UserProvider._SONG_ID, 2);
            values.put(UserProvider._SONG_NAME,"CHAHUN MAI YA NA");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,7);
            values.put(UserProvider._MOVIE_NAME, "AASHQUI 2");
            values.put(UserProvider._SONG_ID, 3);
            values.put(UserProvider._SONG_NAME,"HUM MAR JAYENGE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,7);
            values.put(UserProvider._MOVIE_NAME, "AASHIQUI 2");
            values.put(UserProvider._SONG_ID, 4);
            values.put(UserProvider._SONG_NAME,"MERI AASHIQUI");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,7);
            values.put(UserProvider._MOVIE_NAME, "AASHIQUI 2");
            values.put(UserProvider._SONG_ID, 5);
            values.put(UserProvider._SONG_NAME,"PIYA AAYE NA");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,7);
            values.put(UserProvider._MOVIE_NAME, "AASHIQUI 2");
            values.put(UserProvider._SONG_ID, 6);
            values.put(UserProvider._SONG_NAME,"AASAN NAHI YAHA");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,8);
            values.put(UserProvider._MOVIE_NAME, "AIRLIFT");
            values.put(UserProvider._SONG_ID, 1);
            values.put(UserProvider._SONG_NAME,"SOCH NA SAKE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,8);
            values.put(UserProvider._MOVIE_NAME, "AIRLIFT");
            values.put(UserProvider._SONG_ID, 2);
            values.put(UserProvider._SONG_NAME,"DIL CHEEZ TUJHE DEDI");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,8);
            values.put(UserProvider._MOVIE_NAME, "AIRLIFT");
            values.put(UserProvider._SONG_ID, 3);
            values.put(UserProvider._SONG_NAME,"MERE NACHAN TU");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,8);
            values.put(UserProvider._MOVIE_NAME, "AIRLIFT");
            values.put(UserProvider._SONG_ID, 4);
            values.put(UserProvider._SONG_NAME,"TU BHOOLA JSIE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);


            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,8);
            values.put(UserProvider._MOVIE_NAME, "AIRLIFT");
            values.put(UserProvider._SONG_ID, 5);
            values.put(UserProvider._SONG_NAME,"SOCH NA SAKE REPRISE");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            values = new ContentValues();

            values.put(UserProvider._MOVIE_ID,8);
            values.put(UserProvider._MOVIE_NAME, "AIRLIFT");
            values.put(UserProvider._SONG_ID, 6);
            values.put(UserProvider._SONG_NAME,"DIL CHEEZ TUJHE DEDI REMIX");
            values.put(UserProvider._SONG_LIKED,"0");
            values.put(UserProvider._SONG_DELETE,"0");

            uri = getApplicationContext().getContentResolver().insert(
                    UserProvider.CONTENT_URI_MOVIES, values);

            return null;

        }
    }


    
}
