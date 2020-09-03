/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2020 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.wcm.core.components.internal.servlets;

import java.awt.*;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.imaging.Imaging;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.Rendition;


public class EnhancedRendition extends ResourceWrapper implements Rendition {

    private static final Logger LOG = LoggerFactory.getLogger(EnhancedRendition.class);
    private static final Pattern pattern = Pattern.compile(".*\\.(?<width>\\d+)\\.(?<height>\\d+)\\..*");
    private static final String WIDTH_GROUP = "width";
    private static final String HEIGHT_GROUP = "height";

    private Rendition rendition;
    private Dimension dimension;
    private boolean dimensionProcessed = false;

    public EnhancedRendition(@NotNull Rendition rendition) {
        super(rendition);
        this.rendition = rendition;
    }

    @Nullable
    public Dimension getDimension() {
        if (!dimensionProcessed) {
            if (DamConstants.ORIGINAL_FILE.equals(getName())) {
                // Original asset
                try {
                    Asset asset = getAsset();
                    int width = Integer.parseInt(asset.getMetadataValue(DamConstants.TIFF_IMAGEWIDTH));
                    int height = Integer.parseInt(asset.getMetadataValue(DamConstants.TIFF_IMAGELENGTH));
                    dimension = new Dimension(width, height);
                } catch (NumberFormatException nfex) {
                    LOG.error("Cannot parse original asset dimensions", nfex);
                }
            } else if (getProperties().containsKey(DamConstants.TIFF_IMAGEWIDTH) && getProperties().containsKey(DamConstants.TIFF_IMAGELENGTH)) {
                // Use dimensions from rendition metadata
                try {
                    int width = Integer.parseInt(getProperties().get(DamConstants.TIFF_IMAGEWIDTH, String.class));
                    int height = Integer.parseInt(getProperties().get(DamConstants.TIFF_IMAGELENGTH, String.class));
                    dimension = new Dimension(width, height);
                } catch (NumberFormatException nfex) {
                    LOG.error("Cannot parse rendition dimensions from metadata", nfex);
                }
            } else {
                Matcher matcher = pattern.matcher(getName());
                if (matcher.matches()) {
                    // Try to load image to determine size, if not too large
                    try {
                        int width = Integer.parseInt(matcher.group(WIDTH_GROUP));
                        int height = Integer.parseInt(matcher.group(HEIGHT_GROUP));
                        dimension = new Dimension(width, height);
                    } catch (NumberFormatException nfex) {
                        LOG.error("Cannot parse rendition dimensions from name", nfex);
                    }
                } else if (rendition.getSize() < (AdaptiveImageServlet.DEFAULT_MAX_SIZE ^ 2)) {
                    // Try to load image to determine size, if not too large
                    try {
                        dimension = Imaging.getImageSize(getStream(), getName());
                    } catch (Exception e) {
                        LOG.error("Cannot get rendition dimension from stream", e);
                    }
                }
            }
            dimensionProcessed = true;
        }
        return dimension;
    }

    @Override
    public String getMimeType() {
        return rendition.getMimeType();
    }

    @Override
    public ValueMap getProperties() {
        return rendition.getProperties();
    }

    @Override
    public long getSize() {
        return rendition.getSize();
    }

    @Override
    public InputStream getStream() {
        return rendition.getStream();
    }

    @Override
    public Asset getAsset() {
        return rendition.getAsset();
    }
}
