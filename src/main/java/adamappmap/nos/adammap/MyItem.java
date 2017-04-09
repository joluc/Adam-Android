package adamappmap.nos.adammap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Joluc on 09.04.17.
 */

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private final int EQ = 0;
    private String Idenfikation;
    private boolean STATE;
    private String stationnumber;



    public MyItem(double lat, double lng, int EQ) {
        EQ = EQ;
        mPosition = new LatLng(lat, lng);
    }

    public void setIdenfikation(String idenfikation) {
        Idenfikation = idenfikation;
    }
    public String getIdenfikation()
    {
        return Idenfikation;
    }
    public boolean getState()
    {
        return STATE;
    }
    public String getstationnumer()
    {
        return stationnumber;
    }
    public void setgetstationnumer(String stationnumbere)
    {
        stationnumber = stationnumbere;
    }
    public void setSTATE(boolean STATED)
    {
        STATE = STATED;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public int getEQ() {
        return EQ;
    }
    public MyItem getMarker() {
        return this;

    }

}
