package com.example.flixter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.flixter.databinding.ActivityMovieDetailsBinding;
import com.example.flixter.models.Movie;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;

public class MovieDetailsActivity extends AppCompatActivity {
    final String TAG = "MovieDetailsActivity";
    Movie movie;

    ImageView tvPoster;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    TextView releaseDate;

    Context context;

    String videoKey;
    String site;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMovieDetailsBinding binding = ActivityMovieDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); //R.layout.activity_movie_details

        context = this;
        tvPoster = binding.tvPoster;
        tvTitle = binding.tvTitle;
        tvOverview = binding.tvOverview;
        rbVoteAverage = binding.rbVoteAverage;
        releaseDate = binding.releaseDate;


        movie = Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));

        try {
            Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

            tvTitle.setText(movie.getTitle());
            tvOverview.setText(movie.getOverview());
            rbVoteAverage.setRating(movie.getVoteAverage().floatValue()/2.0f);
            releaseDate.setText(movie.getReleaseDate());
        } catch (NullPointerException e) {
            Log.w("MovieDetailsActivity", "Can't find movie for new Activity");
        }

        //Sets up images
        final int placeholder = R.drawable.flicks_backdrop_placeholder;
        final String imageURL = movie.getBackdropPath();
        if (this.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            Glide.with(this).
                    load(movie.getPosterPath()).
                    transform(new RoundedCorners(20)).
                    placeholder(R.drawable.flicks_movie_placeholder).
                    into(binding.imageView);
        }

        Glide.with(this).
                load(imageURL).
                transform(new RoundedCorners(20)).
                placeholder(placeholder).
                into(tvPoster);

        // Get Video Key
        final String videoURL = "https://api.themoviedb.org/3/movie/"
                + movie.getId() + "/videos?api_key=" + getString(R.string.tmdb_api_key)
                + "&language=en-US";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(videoURL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, videoURL + " : onSuccess");
                JSONObject jsonObject = json.jsonObject;
                Log.i(TAG,"Video information: " + jsonObject.toString());
                try {
                    JSONArray resultsArr = jsonObject.getJSONArray("results");
                    Log.i(TAG, "Video results: " + resultsArr);

                    JSONObject results = resultsArr.getJSONObject(0);
                    site = results.getString("site");
                    videoKey = results.getString("key");
                    Log.d(TAG, "Video key: " + videoKey + " & Site: " + site);

                    Glide.with(context).
                            load("https://img.youtube.com/vi/" + videoKey
                                    + "/0.jpg").
                            transform(new RoundedCorners(20)).
                            placeholder(placeholder).
                            into(tvPoster);
                } catch (JSONException e) {
                    Log.e(TAG, "Error getting results for Movie Trailer");
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers,
                                  String response, Throwable throwable) {
                Log.d(TAG, videoURL + " :" + "failure");
            }
        });

        tvPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create Intent
                Intent intent = new Intent(context, MovieTrailerActivity.class);
                intent.putExtra("videoKey", videoKey);
                intent.putExtra("site", site);
                context.startActivity(intent);
            }
        });
    }
}
