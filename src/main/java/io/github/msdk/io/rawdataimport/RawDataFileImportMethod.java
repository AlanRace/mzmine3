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

package io.github.msdk.io.rawdataimport;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.filetypedetection.FileTypeDetectionMethod;
import io.github.msdk.io.rawdataimport.mzdata.MzDataFileImportMethod;
import io.github.msdk.io.rawdataimport.mzml.MzMLFileImportMethod;
import io.github.msdk.io.rawdataimport.mzxml.MzXMLFileImportMethod;
import io.github.msdk.io.rawdataimport.netcdf.NetCDFFileImportMethod;

/**
 * This class detects the type of the given data file using the
 * FileTypeDetectionAlgorithm and then imports the raw data by performing the
 * right import algorithm.
 */
public class RawDataFileImportMethod implements MSDKMethod<RawDataFile> {

    private final @Nonnull File sourceFile;
    private final @Nonnull DataPointStore dataStore;

    private RawDataFile result;
    private boolean canceled = false;
    MSDKMethod<RawDataFile> parser = null;

    /**
     * 
     * @param sourceFile
     */
    public RawDataFileImportMethod(@Nonnull File sourceFile,
            @Nonnull DataPointStore dataStore) {
        this.sourceFile = sourceFile;
        this.dataStore = dataStore;
    }

    /**
     * @throws MSDKException
     */
    @Override
    public RawDataFile execute() throws MSDKException {

        FileTypeDetectionMethod typeDetector = new FileTypeDetectionMethod(
                sourceFile);
        FileType fileType = typeDetector.execute();

        if (fileType == null)
            throw new MSDKException("Unknown file type of file " + sourceFile);

        if (canceled)
            return null;

        switch (fileType) {
        case MZML:
            parser = new MzMLFileImportMethod(sourceFile);
            break;
        case MZXML:
            parser = new MzXMLFileImportMethod(sourceFile);
            break;
        case MZDATA:
            parser = new MzDataFileImportMethod(sourceFile, dataStore);
            break;
        case NETCDF:
            parser = new NetCDFFileImportMethod(sourceFile, dataStore);
            break;
        default:
            throw new MSDKException("Unsupported file type (" + fileType
                    + ") of file " + sourceFile);
        }

        result = parser.execute();
        return result;

    }

    @Override
    public Float getFinishedPercentage() {
        if (parser == null)
            return null;
        return parser.getFinishedPercentage();
    }

    @Override
    @Nullable
    public RawDataFile getResult() {
        return result;
    }

    @Override
    public void cancel() {
        canceled = true;
        if (parser != null)
            parser.cancel();
    }

}