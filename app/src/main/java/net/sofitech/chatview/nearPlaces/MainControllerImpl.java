package net.sofitech.chatview.nearPlaces;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class MainControllerImpl implements MainController {

  private ProgressDialog loadingDialog;

  public void getVenuesData(String area, final List venuesList, final RecyclerView.Adapter venuesRecyclerAdapter, Context mainContext) {

    final Context context = mainContext;

    loadingDialog = new ProgressDialog(context);
    loadingDialog.setMessage(getStringResourceByName("venue_search_loading",context));
    loadingDialog.setIndeterminate(false);
    loadingDialog.setCancelable(false);
    loadingDialog.show();

    RequestQueue requestQueue = Volley.newRequestQueue(context);
    String foursquareRequestUrl = null;
    try {
      foursquareRequestUrl = "https://api.foursquare.com/v2/venues/explore?client_id=YFRT0F0EKMLCUYYQO04YWQMUN15MXZH2KEXU5ZGN2D1QV0CF&client_secret=I2NQKHQPPL3DCCBTWU4PP4ZF2WJMGYZEOXIVXSHF0K330RJA&v=20130815%20&near=" + URLEncoder.encode(area, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      Toast.makeText(context, getStringResourceByName("venue_search_invalid_error",context), Toast.LENGTH_SHORT).show();
    }

    JsonObjectRequest jsObjRequest = new JsonObjectRequest
        (Request.Method.GET, foursquareRequestUrl, null, new Response.Listener<JSONObject>() {

          @Override
          public void onResponse(JSONObject response) {
            loadingDialog.dismiss();
            try {
              venuesList.clear();
            //  Log.e("Response",response.toString());
              JSONArray items = response.getJSONObject("response").getJSONArray("groups").getJSONObject(0).getJSONArray("items");
              for (int i = 0; i < items.length(); i++) {
                JSONObject venuesJson = items.getJSONObject(i).getJSONObject("venue");
                venuesList.add(new VenueModel(
                    venuesJson.getString("name"),
                    venuesJson.getString("rating"),
                    venuesJson.getJSONArray("categories"),
                    venuesJson.getJSONObject("location")
                ));
              }
            } catch (JSONException e) {
              venuesList.clear();
              Toast.makeText(context, getStringResourceByName("venue_search_invalid_error",context), Toast.LENGTH_SHORT).show();
            }
            venuesRecyclerAdapter.notifyDataSetChanged();
          }
        }, new Response.ErrorListener() {

          @Override
          public void onErrorResponse(VolleyError error) {
            if (error instanceof NetworkError) {
              loadingDialog.dismiss();
              Toast.makeText(context, getStringResourceByName("no_inernet_connection",context), Toast.LENGTH_SHORT).show();
            } else {
              loadingDialog.dismiss();
              Toast.makeText(context, getStringResourceByName("venue_search_loading_error",context), Toast.LENGTH_SHORT).show();
            }
          }
        });
    jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
        5000,
        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    requestQueue.add(jsObjRequest);
  }

  private String getStringResourceByName(String aString, Context Context) {
    String packageName = Context.getPackageName();
    int resId = Context.getResources().getIdentifier(aString, "string", packageName);
    return Context.getString(resId);
  }
}
