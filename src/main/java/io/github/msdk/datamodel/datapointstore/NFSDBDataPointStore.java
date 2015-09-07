/* 
 * (C) Copyright 2015 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.datamodel.datapointstore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nfsdb.journal.factory.JournalFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.peaklists.FeatureDataPointList;
import io.github.msdk.datamodel.rawdata.ChromatogramDataPointList;
import io.github.msdk.datamodel.rawdata.MsSpectrumDataPointList;

/**
 * A DataPointStore implementation that stores the data points in memory. Use
 * with caution. When DataPointLists are stored or retrieved, they are not
 * referenced but copied, so the original list can be used for other purpose.
 * 
 * The methods of this class are synchronized, therefore it can be safely used
 * by multiple threads.
 */
class NFSDBDataPointStore implements DataPointStore {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private HashMap<Object, Object> storageMap = new HashMap<>();

    private int lastStorageId = 0;
    private final File tmpDataFileName;
    
    NFSDBDataPointStore() throws MSDKException {

        try {
            tmpDataFileName = Files.createTempDirectory("msdk_nfsdb").toFile();           

            logger.debug("Initializing a new NFSDB journal factory in "
                    + tmpDataFileName);

            JournalFactory factory = new JournalFactory(tmpDataFileName);


        } catch (IOException e) {
            throw new MSDKException(e);
        }

    }

    
    /**
     * Stores new array of data points.
     * 
     * @return Storage ID for the newly stored data.
     */
    @Override
    public @Nonnull Integer storeDataPoints(
            @Nonnull MsSpectrumDataPointList dataPoints) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        // Clone the given list for storage
        final MsSpectrumDataPointList newList = MSDKObjectBuilder
                .getSpectrumDataPointList();
        newList.copyFrom(dataPoints);

        // Save the reference to the new list
        synchronized (storageMap) {
            // Increase the storage ID
            lastStorageId++;
            storageMap.put(lastStorageId, newList);
        }

        return lastStorageId;
    }

    /**
     * Stores new array of data points.
     * 
     * @return Storage ID for the newly stored data.
     */
    @Override
    public @Nonnull Integer storeDataPoints(
            @Nonnull ChromatogramDataPointList dataPoints) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        // Clone the given list for storage
        final ChromatogramDataPointList newList = MSDKObjectBuilder
                .getChromatogramDataPointList();
        newList.copyFrom(dataPoints);

        // Save the reference to the new list
        synchronized (storageMap) {
            // Increase the storage ID
            lastStorageId++;
            storageMap.put(lastStorageId, newList);
        }

        return lastStorageId;
    }

    /**
     * Stores new array of data points.
     * 
     * @return Storage ID for the newly stored data.
     */
    @Override
    public @Nonnull Integer storeDataPoints(
            @Nonnull FeatureDataPointList dataPoints) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        // Clone the given list for storage
        final FeatureDataPointList newList = MSDKObjectBuilder
                .getFeatureDataPointList();
        newList.copyFrom(dataPoints);

        // Save the reference to the new list
        synchronized (storageMap) {
            // Increase the storage ID
            lastStorageId++;
            storageMap.put(lastStorageId, newList);
        }

        return lastStorageId;
    }

    /**
     * Reads the data points associated with given ID.
     */
    @Override
    synchronized public void readDataPoints(@Nonnull Object ID,
            @Nonnull MsSpectrumDataPointList list) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        if (!storageMap.containsKey(ID))
            throw new IllegalArgumentException(
                    "ID " + ID + " not found in storage");

        // Get the stored DataPointList
        final Object storedObject = storageMap.get(ID);
        if (!(storedObject instanceof MsSpectrumDataPointList))
            throw new MSDKRuntimeException(
                    "Object stored under ID " + ID + " is not of correct type");
        final MsSpectrumDataPointList storedList = (MsSpectrumDataPointList) storedObject;

        // Copy data
        list.copyFrom(storedList);

    }

    /**
     * Reads the data points associated with given ID.
     */
    @Override
    synchronized public void readDataPoints(@Nonnull Object ID,
            @Nonnull ChromatogramDataPointList list) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        if (!storageMap.containsKey(ID))
            throw new IllegalArgumentException(
                    "ID " + ID + " not found in storage");

        // Get the stored DataPointList
        final Object storedObject = storageMap.get(ID);
        if (!(storedObject instanceof ChromatogramDataPointList))
            throw new MSDKRuntimeException(
                    "Object stored under ID " + ID + " is not of correct type");
        final ChromatogramDataPointList storedList = (ChromatogramDataPointList) storedObject;

        // Copy data
        list.copyFrom(storedList);

    }

    /**
     * Reads the data points associated with given ID.
     */
    @Override
    synchronized public void readDataPoints(@Nonnull Object ID,
            @Nonnull FeatureDataPointList list) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        if (!storageMap.containsKey(ID))
            throw new IllegalArgumentException(
                    "ID " + ID + " not found in storage");

        // Get the stored DataPointList
        final Object storedObject = storageMap.get(ID);
        if (!(storedObject instanceof FeatureDataPointList))
            throw new MSDKRuntimeException(
                    "Object stored under ID " + ID + " is not of correct type");
        final FeatureDataPointList storedList = (FeatureDataPointList) storedObject;

        // Copy data
        list.copyFrom(storedList);

    }

    /**
     * Remove data associated with given storage ID.
     */
    @Override
    synchronized public void removeDataPoints(@Nonnull Object ID) {

        if (storageMap == null)
            throw new IllegalStateException("This object has been disposed");

        storageMap.remove(ID);
    }

    @Override
    synchronized public void dispose() {
        storageMap = null;
    }

}
