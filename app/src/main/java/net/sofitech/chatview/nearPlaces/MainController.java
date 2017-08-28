package net.sofitech.chatview.nearPlaces;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public interface MainController {

  void getVenuesData(String area, final List venuesList, final RecyclerView.Adapter venuesRecyclerAdapter, Context mainContext);
}
