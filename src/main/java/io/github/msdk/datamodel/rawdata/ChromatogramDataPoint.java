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

package io.github.msdk.datamodel.rawdata;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A single data point of a mass chromatogram (a pair of rt and intensity values).
 * For convenience, this interface is immutable, so it can be passed by
 * reference and safely used by multiple threads.
 */
@Immutable
public interface ChromatogramDataPoint {

    /**
     * Returns the rt value of this data point.
     * 
     * @return Data point rt
     */
    @Nonnull
    Double getRt();

    /**
     * Returns the intensity value of this data point.
     * 
     * @return Data point intensity.
     */
    @Nonnull
    Float getIntensity();

}
