package com.elephant.ediyou.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.elephant.ediyou.R;
import com.elephant.ediyou.imagecache.ImageUtils;
import com.elephant.ediyou.imagecache2.ImageFetcher;
import com.elephant.ediyou.imagecache2.ImageWorker;

public class MainHomeBannerFragment extends Fragment {
	private static final String IMAGE_DATA_EXTRA = "extra_image_data";
	private static final String TEXT_DATA_EXTRA = "extra_text_data";
	private String mImageUrl;
	private String title;
	private ImageView mImageView;
	private TextView tvTitle;
	private OnClickListener onClickListener;
	private ImageFetcher mImageFetcher;
	
	  public static MainHomeBannerFragment newInstance(String imageUrl,String title) {
	        final MainHomeBannerFragment f = new MainHomeBannerFragment();

	        final Bundle args = new Bundle();
	        args.putString(IMAGE_DATA_EXTRA, imageUrl);
	        args.putString(TEXT_DATA_EXTRA, title);
	        f.setArguments(args);

	        return f;
	    }

	    /**
	     * Empty constructor as per the Fragment documentation
	     */
	    public MainHomeBannerFragment() {}

	    /**
	     * Populate image using a url from extras, use the convenience factory method
	     * {@link PassionDetailFragment#newInstance(String)} to create this fragment.
	     */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
	        title = getArguments() != null ? getArguments().getString(TEXT_DATA_EXTRA) : null;
	    }

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        // Inflate and locate the main ImageView
	        final View v = inflater.inflate(R.layout.events_item, container, false);
	        mImageView = (ImageView) v.findViewById(R.id.ivEventImage);
	        tvTitle = (TextView) v.findViewById(R.id.tvEventName);
	        mImageView.setTag(mImageUrl);
	        tvTitle.setText(title);
	        return v;
	    }

	    @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
	        super.onActivityCreated(savedInstanceState);
	        if(onClickListener!=null){
	        	mImageView.setOnClickListener(onClickListener);
	        }

	        // Use the parent activity to load the image asynchronously into the ImageView (so a single
	        // cache can be used over all pages in the ViewPager
	        if (MainHomeActivity.class.isInstance(getActivity())) {
	            mImageFetcher = ((MainHomeActivity) getActivity()).getImageFetcher();
	            mImageFetcher.loadImage(mImageUrl, mImageView);
	        }

	        // Pass clicks on the ImageView to the parent activity to handle
	        if (OnClickListener.class.isInstance(getActivity()) && ImageUtils.hasHoneycomb()) {
	            mImageView.setOnClickListener((OnClickListener) getActivity());
	        }
	    }

	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	        if (mImageView != null) {
	            // Cancel any pending image work
	            ImageWorker.cancelWork(mImageView);
	            mImageView.setImageDrawable(null);
	        }
	    }
	    
	    
		public void setOnClickListener(OnClickListener onClickListener) {
			this.onClickListener = onClickListener;
		}
		
}
