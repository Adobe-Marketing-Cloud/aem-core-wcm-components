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
package com.adobe.cq.wcm.core.extensions.amp.internal.models.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.factory.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.internal.services.pdfviewer.PdfViewerCaConfig;
import com.adobe.cq.wcm.core.extensions.amp.internal.AmpUtil;
import com.adobe.cq.wcm.core.extensions.amp.internal.ClientlibCaConfig;
import com.adobe.cq.wcm.core.extensions.amp.models.AmpPage;
import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.day.cq.wcm.api.Page;

import static com.adobe.cq.wcm.core.extensions.amp.internal.AmpUtil.AMP_SELECTOR;
import static com.adobe.cq.wcm.core.extensions.amp.internal.AmpUtil.DOT;
import static com.day.cq.wcm.foundation.List.URL_EXTENSION;

@Model(adaptables = SlingHttpServletRequest.class,
    adapters = {AmpPage.class})
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
    extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class AmpPageImpl implements AmpPage {

    private static final Logger LOG = LoggerFactory.getLogger(AmpPageImpl.class);

    private static final String NN_CQ_CLIENT_LIBRARY_FOLDER = "cq:ClientLibraryFolder";

    @Self
    private SlingHttpServletRequest request;

    @SlingObject
    protected Resource resource;

    @ScriptVariable
    private Page currentPage;

    @ScriptVariable
    private ResourceResolver resolver;

    @OSGiService
    private ResourceResolverFactory resolverFactory;

    @OSGiService
    private ModelFactory modelFactory;

    @OSGiService
    private HtmlLibraryManager htmlLibraryManager;

    private Map<String, String> pageLinkAttrs;
    private Set<String> headlibIncludes;
    private Set<String> resourceTypes;
    private Map<String, ClientLibrary> allLibraries;
    private String ampMode;
    private Boolean ampSelector;
    private String customHeadlibIncludeScript = ClientlibCaConfig.DEFAULT_CUSTOM_HEADLIB_INCLUDE_SCRIPT;
    private String clientlibRegex = ClientlibCaConfig.DEFAULT_CLIENTLIB_REGEX;

    @PostConstruct
    protected void init() {
        ampMode = AmpUtil.getAmpMode(request);
        ConfigurationBuilder cb = resource.adaptTo(ConfigurationBuilder.class);
        if (cb != null) {
            ClientlibCaConfig caConfig = cb.as(ClientlibCaConfig.class);
            customHeadlibIncludeScript = caConfig.customHeadlibIncludeScript();
            clientlibRegex = caConfig.clientlibRegex();
        }
    }

    private Resource getHeadlibResourceSuperType(ResourceResolver resolver, String resourceType, String headLibRelPath) {
        Resource resource = AmpUtil.resolveResource(resolver, resourceType);
        if (resource == null) {
            LOG.debug("Can't access resource from resource type {}.", resourceType);
            return null;
        }
        // Get resource superType path from the resource type.
        String superTypePath = resource.getResourceSuperType();
        if (superTypePath == null) {
            LOG.trace("No resource superType from resource type {}.", resourceType);
            return null;
        }
        // Get headLibResource from resource superType.
        Resource headLibResource = AmpUtil.resolveResource(resolver, superTypePath + headLibRelPath);
        // Return next superType or headLibResource.
        return headLibResource == null ?
            getHeadlibResourceSuperType(resolver, superTypePath, headLibRelPath) :
            headLibResource;
    }

    @Override
    public Map<String, String> getPageLinkAttrs() {
        if (pageLinkAttrs == null) {
            pageLinkAttrs = new HashMap<>();
            String relValue;
            String hrefValue;
            if (!isAmpSelector() && ampMode.equals(AmpUtil.PAIRED_AMP)) {
                relValue = "amphtml";
                hrefValue = currentPage.getPath() + DOT + AMP_SELECTOR + URL_EXTENSION;
            } else {
                relValue = "canonical";
                hrefValue = currentPage.getPath() + URL_EXTENSION;
            }

            pageLinkAttrs.put("rel", relValue);
            pageLinkAttrs.put("href", hrefValue);
        }
        return pageLinkAttrs;
    }

    @Override
    public Set<String> getComponentsResourceTypes() {
        if (resourceTypes == null) {
            resourceTypes = AmpUtil.getPageResourceTypes(currentPage, request, modelFactory);
        }
        return resourceTypes;
    }


    @Override
    public List<String> getHeadlibIncludes() {
        if (headlibIncludes == null) {
            headlibIncludes = new HashSet<>();
            if (isAmpSelector()) {
                try (ResourceResolver resolver = getClientlibResourceResolver()) {
                    Set<String> resourceTypes = getComponentsResourceTypes();

                    // Last part of any headlib path.
                    String headLibRelPath = "/" + customHeadlibIncludeScript;

                    // Iterate through each resource type and read its AMP headlib.
                    for (String resourceType : resourceTypes) {

                        // Resolve the resource type's AMP headlib.
                        Resource headLibResource;
                        String headLibPath = resourceType + headLibRelPath;
                        headLibResource = AmpUtil.resolveResource(resolver, headLibPath);
                        if (headLibResource == null) {
                            LOG.trace("No custom headlib for resource type {}.", resourceType);

                            // Get headLibResource from resource superType.
                            headLibResource = getHeadlibResourceSuperType(resolver, resourceType, headLibRelPath);
                            if (headLibResource == null) {
                                LOG.trace("No custom headlib for resource superType from resource type {}.", resourceType);
                                continue;
                            }
                        }
                        headlibIncludes.add(headLibResource.getPath());
                    }
                } catch (LoginException e) {
                    LOG.error("Unable to get the service resource resolver.", e);
                }
            }
        }
        return new ArrayList<>(headlibIncludes);
    }

    @Override
    public Set<String> getClientlibCategories() {
        allLibraries = htmlLibraryManager.getLibraries();
        Collection<ClientLibrary> libraries = new LinkedList<>();
        for (String resourceType : getComponentsResourceTypes()) {
            Resource componentRes = resolver.getResource(resourceType);
            addClientLibraries(componentRes, libraries);
        }
        Set<String> clientlibCategories = new HashSet<>();
        for (ClientLibrary library : libraries) {
            for (String category : library.getCategories()) {
                Pattern pattern = Pattern.compile(clientlibRegex);
                if (pattern.matcher(category).matches()) {
                    clientlibCategories.add(category);
                }
            }
        }
        return clientlibCategories;
    }

    private void addClientLibraries(Resource componentRes, Collection<ClientLibrary> libraries) {
        if (componentRes == null) {
            return;
        }
        String componentType = componentRes.getResourceType();
        if (StringUtils.equals(componentType, NN_CQ_CLIENT_LIBRARY_FOLDER)) {
            ClientLibrary library = allLibraries.get(componentRes.getPath());
            if (library != null) {
                libraries.add(library);
            }
        }
        Iterable<Resource> childComponents = componentRes.getChildren();
        for (Resource child : childComponents) {
            addClientLibraries(child, libraries);
        }
    }

    @Override
    public boolean isAmpSelector() {
        return Arrays.asList(request.getRequestPathInfo().getSelectors()).contains(AMP_SELECTOR);
    }

    @Override
    public boolean isAmpEnabled() {
        return ampMode.equals(AmpUtil.PAIRED_AMP) || ampMode.equals(AmpUtil.AMP_ONLY);
    }

    private ResourceResolver getClientlibResourceResolver() throws LoginException {
        return resolverFactory.getServiceResourceResolver(
                Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, AmpUtil.CLIENTLIB_SUBSERVICE));
    }
}
