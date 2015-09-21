/*
 * Copyright 2006-2015 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package io.github.msdk.centroiding.recursive;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.util.MsScanUtil;

public class RecursiveCentroidingMethod implements MSDKMethod<MsScan> {

    private final @Nonnull MsScan inputScan;
    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull Float noiseLevel;
    private final @Nonnull Range<Double> mzPeakWidthRange;

    private float methodProgress = 0f;
    private MsScan newScan;

    public RecursiveCentroidingMethod(@Nonnull MsScan inputScan,
            @Nonnull DataPointStore dataPointStore, @Nonnull Float noiseLevel,
            @Nonnull Range<Double> mzPeakWidthRange) {
        this.inputScan = inputScan;
        this.dataPointStore = dataPointStore;
        this.noiseLevel = noiseLevel;
        this.mzPeakWidthRange = mzPeakWidthRange;
    }

    @Override
    public MsScan execute() throws MSDKException {

        // Copy all scan properties
        this.newScan = MsScanUtil.clone(dataPointStore, inputScan, false);

        // Create data structures
        final MsSpectrumDataPointList inputDataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        final MsSpectrumDataPointList newDataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();

        // Load data points
        inputScan.getDataPoints(inputDataPoints);

        // If there are no data points, just return the scan
        if (inputDataPoints.getSize() == 0) {
            newScan.setDataPoints(inputDataPoints);
            methodProgress = 1f;
            return newScan;
        }

        // Run the recursive search algorithm
        recursiveThreshold(inputDataPoints, newDataPoints, 0,
                inputDataPoints.getSize() - 1, noiseLevel, 0);

        // Store the new data points
        newScan.setDataPoints(newDataPoints);

        // Finish
        methodProgress = 1f;
        return newScan;

    }

    /**
     * This function searches for maxima from given part of a spectrum
     */
    private int recursiveThreshold(MsSpectrumDataPointList inputDataPoints,
            MsSpectrumDataPointList newDataPoints, int startInd, int stopInd,
            double currentNoiseLevel, int recuLevel) {

        final double mzBuffer[] = inputDataPoints.getMzBuffer();
        final float intensityBuffer[] = inputDataPoints.getIntensityBuffer();

        int peakStartInd, peakStopInd, peakMaxInd;
        double peakWidthMZ;

        for (int ind = startInd; ind < stopInd; ind++) {

            double localMinimum = Double.MAX_VALUE;

            // Ignore intensities below curentNoiseLevel
            if (intensityBuffer[ind] <= currentNoiseLevel)
                continue;

            // Add initial point of the peak
            peakStartInd = ind;
            peakMaxInd = peakStartInd;

            // While peak is on
            while ((ind < stopInd)
                    && (intensityBuffer[ind] > currentNoiseLevel)) {

                final boolean isLocalMinimum = (intensityBuffer[ind
                        - 1] > intensityBuffer[ind])
                        && (intensityBuffer[ind] < intensityBuffer[ind + 1]);

                // Check if this is the minimum point of the peak
                if (isLocalMinimum && (intensityBuffer[ind] < localMinimum))
                    localMinimum = intensityBuffer[ind];

                // Check if this is the maximum point of the peak
                if (intensityBuffer[ind] > intensityBuffer[peakMaxInd])
                    peakMaxInd = ind;

                ind++;
            }

            // Add ending point of the peak
            peakStopInd = ind;

            peakWidthMZ = mzBuffer[peakStopInd] - mzBuffer[peakStartInd];

            // Verify width of the peak
            if (mzPeakWidthRange.contains(peakWidthMZ)) {

                // Declare a new MzPeak with intensity equal to max intensity
                // data point
                newDataPoints.add(mzBuffer[peakMaxInd],
                        intensityBuffer[peakMaxInd]);

                if (recuLevel > 0) {
                    // return stop index and beginning of the next peak
                    return ind;
                }
            }

            // If the peak is still too big applies the same method until find a
            // peak of the right size
            if (peakWidthMZ > mzPeakWidthRange.upperEndpoint()) {
                if (localMinimum < Double.MAX_VALUE) {
                    ind = recursiveThreshold(inputDataPoints, newDataPoints,
                            peakStartInd, peakStopInd, localMinimum,
                            recuLevel + 1);
                }

            }

        }

        // return stop index
        return stopInd;

    }

    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return methodProgress;
    }

    @Override
    @Nullable
    public MsScan getResult() {
        return newScan;
    }

    @Override
    public void cancel() {
        // This method is too fast to be canceled
    }

}
