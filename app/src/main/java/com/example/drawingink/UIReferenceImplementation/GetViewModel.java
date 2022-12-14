package com.example.drawingink.UIReferenceImplementation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class GetViewModel extends AndroidViewModel {
    private List<ConvertList> convertList = new ArrayList<>();
    private MutableLiveData<Resource<List<ConvertList>>> convertListLive = new MutableLiveData<>();

    private Boolean isconvert;
    private MutableLiveData<Resource<Boolean>> isconvertLive = new MutableLiveData<>();

    public GetViewModel(@NonNull Application application) {
        super(application);
    }

    public void setConvertList(List<ConvertList> convertList) {
        this.convertList = convertList;
        this.convertListLive.postValue(Resource.success(convertList));
    }

    public void ResetConvertList() {
        this.convertListLive.postValue(Resource.reset(null));
    }

    public MutableLiveData<Resource<List<ConvertList>>> getConvertListLive() {
        return convertListLive;
    }

    public void setIsconvert(Boolean isconvert) {
        this.isconvert = isconvert;
        this.isconvertLive.postValue(Resource.success(isconvert));
    }

    public void ResetIsconvert() {

        this.isconvertLive.postValue(Resource.reset(null));
    }


    public MutableLiveData<Resource<Boolean>> getConvertLive() {
        return isconvertLive;
    }
}
