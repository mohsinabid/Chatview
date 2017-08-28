package net.sofitech.chatview.nearPlaces;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.sofitech.chatview.R;

import java.util.List;

public class VenueRecyclerAdapter extends RecyclerView.Adapter<VenueRecyclerAdapter.ViewHolder> {
  public List<VenueModel> mVenues;
  private Context mContext;

  public VenueRecyclerAdapter(List<VenueModel> VenueList, Context context) {
    mVenues = VenueList;
    mContext = context;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.venue_recycler_row, parent, false);
    return new ViewHolder(rowView,mVenues);
  }

  @Override
  public int getItemCount() {
    return mVenues.size();
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {

    final VenueModel selectedVenue = mVenues.get(position);
    final ViewHolder finalHolder = holder;


    holder.VenueName.setText(selectedVenue.getVenueName());
    holder.VenueRating.setText(selectedVenue.getCategoriesName());
    Picasso.with(mContext).load(selectedVenue.getVenueIcon())
        //attempt to load the image from cache
        .networkPolicy(NetworkPolicy.OFFLINE).fit().into(finalHolder.VenueIcon, new Callback() {
      @Override
      public void onSuccess() {

      }

      @Override
      public void onError() {
        //Try again online if cache failed
        Picasso.with(mContext)
            .load(selectedVenue.getVenueIcon())
            .into(finalHolder.VenueIcon, new Callback() {
              @Override
              public void onSuccess() {}

              @Override
              public void onError() {}
            });
      }
    });

  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView VenueIcon;
    TextView VenueName, VenueRating;

    public ViewHolder(View itemView, final List<VenueModel> venueModels) {
      super(itemView);

      VenueName = (TextView) itemView.findViewById(R.id.venue_name);
      VenueRating = (TextView) itemView.findViewById(R.id.venue_rating);
      VenueIcon = (ImageView) itemView.findViewById(R.id.venue_icon);

//      itemView.setOnClickListener(new View.OnClickListener(){
//        @Override
//        public void onClick(View v) {
//          // get position
//          int pos = getAdapterPosition();
//
//          // check if item still exists
//          if(pos != RecyclerView.NO_POSITION){
//            VenueModel clickedDataItem = venueModels.get(pos);
//            Toast.makeText(v.getContext(), "You clicked " + clickedDataItem.getCategoriesName(), Toast.LENGTH_SHORT).show();
//          }
//        }
//      });

    }
  }
}