/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2018 Adobe
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
package com.adobe.cq.wcm.core.components.internal.models.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.factory.ModelFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.internal.Utils;
import com.adobe.cq.wcm.core.components.models.Image;
import com.adobe.cq.wcm.core.components.models.ListItem;
import com.adobe.cq.wcm.core.components.models.Teaser;
import com.day.cq.commons.DownloadResource;
import com.day.cq.commons.ImageResource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.designer.Style;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static com.adobe.cq.wcm.core.components.internal.Utils.ID_SEPARATOR;

@Model(adaptables = SlingHttpServletRequest.class, adapters = {Teaser.class, ComponentExporter.class}, resourceType = TeaserImpl.RESOURCE_TYPE)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME , extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class TeaserImpl extends AbstractImageDelegatingModel implements Teaser {

    public final static String RESOURCE_TYPE = "core/wcm/components/teaser/v1/teaser";

    private String pretitle;
    private String title;
    private String description;
    private String linkURL;
    private String titleType;
    private boolean actionsEnabled = false;
    private boolean titleHidden = false;
    private boolean descriptionHidden = false;
    private boolean imageLinkHidden = false;
    private boolean pretitleHidden = false;
    private boolean titleLinkHidden = false;
    private boolean titleFromPage = false;
    private boolean descriptionFromPage = false;
    private List<ListItem> actions = new ArrayList<>();
    private final List<String> hiddenImageResourceProperties = new ArrayList<String>() {{
        add(JcrConstants.JCR_TITLE);
        add(JcrConstants.JCR_DESCRIPTION);
    }};

    @ScriptVariable
    private Component component;

    @Inject
    private Resource resource;

    @ScriptVariable
    private PageManager pageManager;

    @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
    @JsonIgnore
    @Nullable
    protected Style currentStyle;

    @Self
    private SlingHttpServletRequest request;

    @OSGiService
    private ModelFactory modelFactory;

    private Page targetPage;

    /**
     * The image src.
     */
    private String imageSrc;

    @PostConstruct
    private void initModel() {
        ValueMap properties = resource.getValueMap();
        actionsEnabled = properties.get(Teaser.PN_ACTIONS_ENABLED, actionsEnabled);

        populateStyleProperties();

        titleFromPage = properties.get(Teaser.PN_TITLE_FROM_PAGE, titleFromPage);
        descriptionFromPage = properties.get(Teaser.PN_DESCRIPTION_FROM_PAGE, descriptionFromPage);

        if (actionsEnabled) {
            hiddenImageResourceProperties.add(ImageResource.PN_LINK_URL);
            populateActions();
            if (!actions.isEmpty()) {
                ListItem firstAction = actions.get(0);
                if (firstAction != null) {
                    linkURL = firstAction.getURL();
                    targetPage = pageManager.getPage(firstAction.getPath());
                }
            }
        } else {
            linkURL = properties.get(ImageResource.PN_LINK_URL, String.class);
            targetPage = pageManager.getPage(linkURL);
            if (targetPage != null) {
                linkURL = Utils.getURL(request, targetPage);
            }
        }

        if (!pretitleHidden) {
            pretitle = properties.get("pretitle", String.class);
        }

        if (!titleHidden) {
            if (titleFromPage) {
                if (targetPage != null) {
                    title = StringUtils.defaultIfEmpty(targetPage.getPageTitle(), targetPage.getTitle());
                } else if (actionsEnabled && !actions.isEmpty()) {
                    title = actions.get(0).getTitle();
                }
            } else {
                title = properties.get(JcrConstants.JCR_TITLE, String.class);
            }
        }

        if (!descriptionHidden) {
            if (descriptionFromPage) {
                if (targetPage != null) {
                    description = targetPage.getDescription();
                }
            } else {
                description = properties.get(JcrConstants.JCR_DESCRIPTION, String.class);
            }
        }

        if (this.hasImage()) {
            this.setImageResource(component, request.getResource(), hiddenImageResourceProperties);
        }
    }

    /**
     * Check if the teaser has an image.
     *
     * The teaser has an image if the `{@value DownloadResource#PN_REFERENCE}` property is set and the value
     * resolves to a resource; or if the `{@value DownloadResource#NN_FILE} child resource exists.
     *
     * @return True if the teaser has an image, false if it does not.
     */
    private boolean hasImage() {
        return Optional.ofNullable(this.resource.getValueMap().get(DownloadResource.PN_REFERENCE, String.class))
            .map(request.getResourceResolver()::getResource)
            .orElseGet(() -> request.getResource().getChild(DownloadResource.NN_FILE)) != null;
    }

    private void populateStyleProperties() {
        if (currentStyle != null) {
            pretitleHidden = currentStyle.get(Teaser.PN_PRETITLE_HIDDEN, pretitleHidden);
            titleHidden = currentStyle.get(Teaser.PN_TITLE_HIDDEN, titleHidden);
            descriptionHidden = currentStyle.get(Teaser.PN_DESCRIPTION_HIDDEN, descriptionHidden);
            titleType = currentStyle.get(Teaser.PN_TITLE_TYPE, titleType);
            imageLinkHidden = currentStyle.get(Teaser.PN_IMAGE_LINK_HIDDEN, imageLinkHidden);
            titleLinkHidden = currentStyle.get(Teaser.PN_TITLE_LINK_HIDDEN, titleLinkHidden);
            if (imageLinkHidden) {
                hiddenImageResourceProperties.add(ImageResource.PN_LINK_URL);
            }
            if (currentStyle.get(Teaser.PN_ACTIONS_DISABLED, false)) {
                actionsEnabled = false;
            }
        }
    }

    private void populateActions() {
        Resource actionsNode = resource.getChild(Teaser.NN_ACTIONS);
        if (actionsNode != null) {
            for (Resource actionRes : actionsNode.getChildren()) {
                actions.add(new Action(actionRes, this.getId()));
            }
        }
    }

    @Override
    public boolean isActionsEnabled() {
        return actionsEnabled;
    }

    @Override
    public List<ListItem> getActions() {
        return Collections.unmodifiableList(actions);
    }

    @Override
    public String getLinkURL() {
        return linkURL;
    }

    /**
     * Get the image path.
     *
     * Note: This method exists only for JSON model.
     *
     * @return The image src path if it exists, null if it does not.
     */
    @JsonProperty(value = "imagePath")
    public String getImagePath() {
        if (imageSrc == null) {
            this.imageSrc = Optional.ofNullable(this.getImageResource())
                .map(imageResource -> this.modelFactory.getModelFromWrappedRequest(this.request, imageResource, Image.class))
                .map(Image::getSrc)
                .orElse(null);
        }
        return this.imageSrc;
    }

    @Override
    public boolean isImageLinkHidden() {
        return imageLinkHidden;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getPretitle() {
        return pretitle;
    }

    @Override
    public boolean isTitleLinkHidden() {
        return titleLinkHidden;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getTitleType() {
        Utils.Heading heading = Utils.Heading.getHeading(titleType);
        if (heading != null) {
            return heading.getElement();
        }
        return null;
    }

    @NotNull
    @Override
    public String getExportedType() {
        return request.getResource().getResourceType();
    }

    /*
     * DataLayerProvider implementation of field getters
     */

    @Override
    public String getDataLayerTitle() {
        return getTitle();
    }

    @Override
    public String getDataLayerLinkUrl() {
        return getLinkURL();
    }

    @Override
    public String getDataLayerDescription() {
        return getDescription();
    }


    @JsonIgnoreProperties({"path", "description", "lastModified", "name"})
    public class Action extends AbstractListItemImpl implements ListItem {

        private static final String CTA_ID_PREFIX = "cta";

        private final Resource ctaResource;
        private final String ctaTitle;
        private final String ctaUrl;
        private final String ctaPath;
        private final Page ctaPage;
        private final String ctaParentId;
        private String ctaId;

        private Action(final Resource actionRes, final String parentId) {
            super(parentId, actionRes);
            ctaParentId = parentId;
            ctaResource = actionRes;
            ValueMap ctaProperties = actionRes.getValueMap();
            ctaTitle = ctaProperties.get(PN_ACTION_TEXT, String.class);
            ctaUrl = ctaProperties.get(PN_ACTION_LINK, String.class);
            ctaPath = actionRes.getPath();
            if (ctaUrl != null && ctaUrl.startsWith("/")) {
                ctaPage = pageManager.getPage(ctaUrl);
            } else {
                ctaPage = null;
            }
        }

        @Nullable
        @Override
        public String getTitle() {
            return ctaTitle;
        }

        @Nullable
        @Override
        public String getPath() {
            return ctaUrl;
        }

        @Nullable
        @Override
        public String getURL() {
            if (ctaPage != null) {
                return Utils.getURL(request, ctaPage);
            } else {
                return ctaUrl;
            }
        }

        @Nullable
        @Override
        public String getId() {
            if (ctaId == null) {
                if (ctaResource != null) {
                    ValueMap properties = ctaResource.getValueMap();
                    ctaId = properties.get(com.adobe.cq.wcm.core.components.models.Component.PN_ID, String.class);
                }
                if (StringUtils.isEmpty(ctaId)) {
                    String prefix = StringUtils.join(ctaParentId, ID_SEPARATOR, CTA_ID_PREFIX);
                    ctaId = Utils.generateId(prefix, ctaPath);
                } else {
                    ctaId = StringUtils.replace(StringUtils.normalizeSpace(StringUtils.trim(ctaId)), " ", ID_SEPARATOR);
                }
            }
            return ctaId;
        }

        /*
         * DataLayerProvider implementation of field getters
         */

        @Override
        public String getDataLayerLinkUrl() {
            return getURL();
        }

        @Override
        public String getDataLayerTitle() {
            return getTitle();
        }
    }
}
