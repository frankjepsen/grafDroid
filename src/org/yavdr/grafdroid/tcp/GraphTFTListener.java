package org.yavdr.grafdroid.tcp;

public interface GraphTFTListener {
    public void callCompleted(GraphTFTHeader header, byte[] msg);
}
