package ca.pedab.datacraver;

import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseEntity implements Parcelable {

	// ------------------------------ [ BASEENTITY VARIABLES ]
	protected String id;
	protected Map<String, Object> additional_params;
	
	
	
	
	// ------------------------------ [ BASEENTITY CONSTRUCTORS ]
	public BaseEntity() { }
	
	public BaseEntity(Parcel in) {
		
		readFromParcel(in);
	}
	
	
	
	


	// ------------------------------ [ BASEENTITY GETTERS ]
	public String getId() {
		
		return id;
	}
	
	public Map<String, Object> getAdditionalParams() {
		
		return additional_params;
	}

	
	
	
	// ------------------------------ [ BASEENTITY SETTERS ]
	public void setId(String id) {
		
		this.id = id;
	}
	
	public void setAdditionalParams(Map<String, Object> additional_params) {
		
		this.additional_params = additional_params;
	}




	// ------------------------------ [ PARCELABLE METHODS ]
	@Override
	public int describeContents() {
		
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeString(id);
		dest.writeMap(additional_params);
	}
	
	protected void readFromParcel(Parcel in) {
		
		id = in.readString();
		in.readMap(additional_params, Map.class.getClassLoader());
	}
}
