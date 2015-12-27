package free.rm.skytube.gui.fragments;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import free.rm.skytube.R;
import free.rm.skytube.businessobjects.VideoStream.StreamMetaData;
import free.rm.skytube.businessobjects.VideoStream.ParseStreamMetaData;
import free.rm.skytube.businessobjects.VideoStream.StreamMetaDataList;
import free.rm.skytube.gui.activities.YouTubePlayerActivity;

/**
 * A fragment that holds a standalone YouTube player.
 */
public class YouTubePlayerFragment extends FragmentEx {

	private String			videoId = null;
	private VideoView		videoView = null;
	private MediaController	mediaController = null;

	private static final String TAG = YouTubePlayerFragment.class.getSimpleName();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_youtube_player, container, false);

		if (videoId == null) {
			videoId = getActivity().getIntent().getExtras().getString(YouTubePlayerActivity.VIDEO_ID);

			videoView = (VideoView) view.findViewById(R.id.video_view);
			// play the video once its loaded
			videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				public void onPrepared(MediaPlayer mediaPlayer) {
					videoView.start();
				}
			});

			mediaController = new MediaController(getActivity());
			videoView.setMediaController(mediaController);

			new GetStreamTask(videoId).execute();
		}

		return view;
	}


	/**
	 * Given a video ID, it will asynchronously get a list of streams (supplied by YouTube) and then
	 * it asks the videoView to start playing a stream.
	 */
	private class GetStreamTask extends AsyncTask<Void, Exception, StreamMetaDataList> {

		/** Video ID */
		private String videoId;


		public GetStreamTask(String videoId) {
			this.videoId = videoId;
		}


		@Override
		protected StreamMetaDataList doInBackground(Void... param) {
			ParseStreamMetaData	ex = new ParseStreamMetaData(videoId);
			StreamMetaDataList 	streamMetaDataList = null;

			try {
				streamMetaDataList = ex.getStreamMetaDataList();
			} catch (Exception e) {
				// inform the user that an exception has been caught
				publishProgress(e);
				Log.e(TAG, "An error has occurred while getting video metadata/streams for video with id=" + videoId, e);
			}

			return streamMetaDataList;
		}


		@Override
		protected void onProgressUpdate(Exception... exception) {
			Toast.makeText(YouTubePlayerFragment.this.getActivity(),
					String.format(getActivity().getString(R.string.error_get_video_streams), videoId),
					Toast.LENGTH_LONG).show();
		}


		@Override
		protected void onPostExecute(StreamMetaDataList streamMetaDataList) {
			if (streamMetaDataList == null  ||  streamMetaDataList.size() <= 0) {
				String error = String.format(getActivity().getString(R.string.error_video_streams_empty), videoId);

				Toast.makeText(YouTubePlayerFragment.this.getActivity(),
						error,
						Toast.LENGTH_LONG).show();

			} else {
				Log.i(TAG, streamMetaDataList.toString());

				// TODO get stream based on user preferences!
				StreamMetaData desiredStream = streamMetaDataList.getDesiredStream();
				Log.i(TAG, ">> PLAYING: " + desiredStream);
				videoView.setVideoURI(desiredStream.getUri());
			}
		}
	}

}